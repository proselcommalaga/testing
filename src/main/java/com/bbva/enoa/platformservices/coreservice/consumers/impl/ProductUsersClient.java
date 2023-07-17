package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.usersapi.client.feign.nova.rest.IRestListenerUsersapi;
import com.bbva.enoa.apirestgen.usersapi.model.USProductUserDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.utils.clientsutils.consumers.impl.UsersClientImpl;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the client for usersservice
 *
 * @author vbazagad
 */
@Service("coreServiceUsersClient")
@Primary
public class ProductUsersClient extends UsersClientImpl implements IProductUsersClient
{
    private static final Logger LOG = LoggerFactory.getLogger(ProductUsersClient.class);

    private static final String VALIDATE_TEAM_USER_ERROR = "USER-006";

    @Override
    public void deleteProductUsers(String ivUser, Integer productId)
    {
        this.restUserApiHandler.deleteAllUsersFromProduct(
                new IRestListenerUsersapi()
                {
                    @Override
                    public void deleteAllUsersFromProduct()
                    {
                        LOG.debug("[UsersAPI Client] -> [deleteAllUsersFromProduct]: Successfully deleted users of product with id [{}]", productId);
                    }

                    @Override
                    public void deleteAllUsersFromProductErrors(Errors outcome)
                    {
                        LOG.error("[UsersAPI Client] -> [deleteAllUsersFromProduct]: there was an error deleting all users from product. Error message: [{}]", outcome.getBodyExceptionMessage());
                        throw new NovaException(ProductsAPIError.getCallToUsersApiError(), outcome,
                                "[UsersAPI Client] -> [deleteAllUsersFromProduct]: Error trying to call 'deleteProductUsers' for product " +
                                        productId);
                    }
                }
                , productId);
    }

    @Override
    public List<USUserDTO> getProductUsersByTeam(Integer productId, String role)
    {
        SingleApiClientResponseWrapper<USUserDTO[]> response = new SingleApiClientResponseWrapper<>();

        this.restUserApiHandler.getProductUsersByTeam(
                new IRestListenerUsersapi()
                {
                    @Override
                    public void getProductUsersByTeam(USUserDTO[] outcome)
                    {
                        LOG.debug("[UsersAPI Client] -> [getProductUsersByTeam]: Successfully recovered product owners of product with id [{}]", productId);
                        response.set(outcome);
                    }

                    @Override
                    public void getProductUsersByTeamErrors(Errors outcome)
                    {
                        LOG.error("[UsersAPI Client] -> [getProductUsersByTeam]: there was an error getting all product users from product. Error message: [{}]", outcome.getBodyExceptionMessage());
                        throw new NovaException(ProductsAPIError.getCallToUsersApiError(), outcome,
                                "[UsersAPI Client] -> [getProductUsersByTeam]: Error trying to call 'getProductOwners' for product " +
                                        productId);
                    }
                }
                , productId, role);

        return Arrays.asList(response.get());
    }

    @Override
    public void addUserToProduct(USProductUserDTO usProductUser, String ivUser, Integer productId)
    {
        this.restUserApiHandler.createProductMember(
                new IRestListenerUsersapi()
                {
                    @Override
                    public void createProductMember()
                    {
                        LOG.debug("[UsersAPI Client] -> [createProductMember]: Successfully added user [{}] as [{}] of product with id [{}]", usProductUser.getUserCode(),
                                usProductUser.getTeamCode(), productId);
                    }

                    @Override
                    public void createProductMemberErrors(Errors outcome)
                    {
                        LOG.error("[UsersAPI Client] -> [addUserToProduct]: there was an error adding a user to product. Error message: [{}]", outcome.getBodyExceptionMessage());
                        String errorCode = "";
                        if (outcome.getMessages() != null && !outcome.getMessages().isEmpty())
                        {
                            errorCode = outcome.getMessages().get(0).getCode();
                        }
                        if (VALIDATE_TEAM_USER_ERROR.equals(errorCode))
                        {
                            throw new NovaException(ProductsAPIError.getUserIsNotInTeamRoleError(), outcome,
                                    "[UsersAPI Client] -> [createProductMember]: User " + usProductUser.getUserCode() +
                                            " is not in the team for the role " + usProductUser.getTeamCode());
                        }
                        else
                        {
                            throw new NovaException(ProductsAPIError.getCallToUsersApiError(), outcome,
                                    "[UsersAPI Client] -> [createProductMember]: Error trying to call 'addUserToProduct' for product " +
                                            productId + " with user " + usProductUser.getUserCode());
                        }
                    }
                }
                , usProductUser, productId);
    }

    @Override
    public void removeUserFromProduct(String ivUser, Integer productId, USProductUserDTO usProductUser)
    {
        this.restUserApiHandler.deleteUserFromProduct(
                new IRestListenerUsersapi()
                {
                    @Override
                    public void deleteUserFromProduct()
                    {
                        LOG.debug("[UsersAPI Client] -> [removeUserFromProduct]: Successfully removed user [{}] from product with id [{}]", usProductUser.getUserCode(), productId);
                    }

                    @Override
                    public void deleteUserFromProductErrors(Errors outcome)
                    {
                        LOG.error("[UsersAPI Client] -> [removeUserFromProduct]: there was an error removing a user to product. Error message: [{}]", outcome.getBodyExceptionMessage());

                        Optional<ErrorMessage> firstErrorMessage = outcome.getFirstErrorMessage();
                        if (firstErrorMessage.isPresent()
                                && ProductsAPIError.USER_IS_UNIQUE_PRODUCT_OWNER_ERROR_CODE.equals(firstErrorMessage.get().getCode()))
                        {
                            throw new NovaException(ProductsAPIError.getUniqueProductOwnerError(usProductUser.getUserCode(), productId));
                        }
                        else
                        {
                            throw new NovaException(ProductsAPIError.getCallToUsersApiError(), outcome,
                                    "[UsersAPI Client] -> [removeUserFromProduct]: Error trying to call 'removeUserFromProduct' for product " +
                                            productId + " with user " + usProductUser.getUserCode());
                        }
                    }
                }
                , productId, usProductUser.getUserCode());
    }

    @Override
    public int[] getProductsByUser(String userCode)
    {
        SingleApiClientResponseWrapper<int[]> response = new SingleApiClientResponseWrapper<>();

        this.restUserApiHandler.getProductsByUser(
                new IRestListenerUsersapi()
                {
                    @Override
                    public void getProductsByUser(int[] outcome)
                    {

                        LOG.debug("[UsersAPI Client] -> [getProductsByUser]: Received from UsersAPI value: {}",
                                Arrays.toString(outcome));
                        response.set(outcome);
                    }

                    @Override
                    public void getProductsByUserErrors(Errors outcome)
                    {
                        throw new NovaException(ProductsAPIError.getCallToUsersApiError(), outcome,
                                "[UsersAPI Client] -> [getProductsByUser]: Error trying to call 'getProductsByUser' for user:" + userCode);
                    }
                }
                , userCode);

        return response.get();
    }
}
