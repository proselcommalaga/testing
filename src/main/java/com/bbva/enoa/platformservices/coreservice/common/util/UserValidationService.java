package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.apirestgen.usersadminapi.model.UATeamUser;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.common.exception.CommonError;
import com.bbva.enoa.platformservices.coreservice.common.interfaces.ExceptionProcessor;
import com.bbva.enoa.platformservices.coreservice.common.Constants.CommonErrorConstants;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IUserAdminClient;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.platformservices.coreservice.productsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Class that allows validating a user existance and obtaining its data.
 *
 * @author XE63267
 */
@Service
public class UserValidationService
{

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(UserValidationService.class);

    /**
     * Users client
     */
    private final IProductUsersClient usersClient;

    /**
     * Users admin client
     */
    private final IUserAdminClient userAdminClient;

    /**
     * Constructor
     * @param usersClient           Client user client
     * @param userAdminClient       user admin client
     */
    @Autowired
    public UserValidationService(IProductUsersClient usersClient, IUserAdminClient userAdminClient) {
        this.usersClient = usersClient;
        this.userAdminClient = userAdminClient;

    }


    private USUserDTO doGetUser(String userCode) throws Errors
    {
        LOG.debug("[Tools API]->[doGetUser]:Recovering information for user [{}]", userCode);
        USUserDTO usUser = this.usersClient.getUser(userCode, new Errors());
        LOG.debug("[Tools API]->[doGetUser]:Recovered information for user [{}]", userCode);
        return usUser;
    }

    private int[] doGetProductsByUser(String userCode)
    {
        LOG.debug("[Tools API]->[doGetProductsByUser]: Recovering products where user [{}] is member", userCode);
        int[] productIdList = this.usersClient.getProductsByUser(userCode);
        LOG.debug("[Tools API]->[doGetProductsByUser]: Recovered products where user [{}] is member", userCode);
        return productIdList;
    }

    private List<UATeamUser> doGetUsersByRole(String roleType) throws NovaException
    {
        LOG.debug("[UserValidationService]->[doGetUsersByRole]: Recovering team users by role [{}]", roleType);
        List<UATeamUser> teamUsers;
        try {
            teamUsers = userAdminClient.getTeamMembers(roleType);
        } catch (Errors errors) {
            throw new NovaException(CommonError.getCallingUserAdminapiError(CommonErrorConstants.COMMON_ERROR_CLASS_NAME));
        }
        LOG.debug("[UserValidationService]->[doGetUsersByRole]: Recovered team users [{}] by role [{}]",teamUsers.toString(), roleType);
        return teamUsers;
    }

    /**
     * Recovers a portal user
     *
     * @param userCode the user
     * @return the user
     * @throws NovaException in case of error
     */
    public USUserDTO getUser(String userCode) throws NovaException
    {
        try
        {
            return doGetUser(userCode);
        }
        catch (Errors e)
        {
            LOG.error("[{}]->[getUser]: Error recovering user with id: [{}]", Constants.USER_VALIDATION_SERVICE, userCode);

            LOG.debug("[{}]->[getUser]: Exception error code: [{}]. Generating concrete NovaException.", Constants.USER_VALIDATION_SERVICE, e.getMessages().get(0).getCode());
            String message;
            if (!Constants.USER_NOT_EXISTS_ERROR.equalsIgnoreCase(e.getMessages().get(0).getCode()))
            {
                message = String.format( "[%s]->[validateUser]: the user code: [%s] does not exists into NOVA BBDD.", Constants.USER_VALIDATION_SERVICE, userCode);
                LOG.error(message);
                throw new NovaException(ProductsAPIError.getUserCodeDoesNotExistError(), message);
            }
            else
            {
                message = String.format( "[%s]->[validateUser]: Error obtaining user information for user with user code: [%s].", Constants.USER_VALIDATION_SERVICE, userCode);
                LOG.error(message);
                throw new NovaException(ProductsAPIError.getUnexpectedError());
            }

        }
    }

    /**
     * Recovers products id where user is member
     *
     * @param userCode  the user
     * @param processor exception processor
     * @param <T>       parameter inherit exception
     * @return the user
     * @throws T in case of error
     */
    public <T extends Throwable> Optional<int[]> getProductsByUser(String userCode, ExceptionProcessor<T> processor) throws T
    {
        try
        {
            return Optional.ofNullable(doGetProductsByUser(userCode));
        }
        catch (NovaException e)
        {
            if (!ProductsAPIError.getUserCodeDoesNotExistError().equals(e.getNovaError()))
            {
                processor.process(e);
            }
            return Optional.empty();
        }
    }

    /**
     * Gets User and validates it exists
     *
     * @param userCode  Code of the user to get
     * @param exception Exception to throw in case it does not exist
     * @param <T>       parameter
     * @return The obtained user
     * @throws T In cas user does not exists or error returned by UserService.
     */
    public <T extends Throwable> USUserDTO validateAndGet(String userCode, T exception) throws T
    {
        try
        {
            USUserDTO user = doGetUser(userCode);
            if (user == null)
            {
                throw exception;

            }
            else
            {
                return user;
            }
        }
        catch (Errors e)
        {
            throw new NovaException(ProductsAPIError.getCallToUsersApiError());
        }

    }

    /**
     * Return all user codes by role type in uppercase
     *
     * @param roleType role type to extract users
     * @return user codes by team in uppercase
     * @throws NovaException nova exception
     */
    public List<String> getUserCodesByTeam(RoleType roleType) throws NovaException
    {
        List<UATeamUser> teamUsers = this.doGetUsersByRole(roleType.getType());
        return teamUsers.stream().map(user -> user.getUserCode().toUpperCase()).collect(Collectors.toList());
    }
}
