package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.exception.CommonError;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Class related to manage releases, plans, subsystems, services and instances flows
 */
@Service
public class ManageValidationUtils
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(ManageValidationUtils.class);

    /**
     * Users service client
     */
    private final IProductUsersClient usersService;

    /**
     * User validation service
     */
    private final UserValidationService userValidationService;

    /**
     * Mange Validations Utils Constructor
     *
     * @param usersService          user service
     * @param userValidationService user validation service
     */
    @Autowired
    public ManageValidationUtils(IProductUsersClient usersService, UserValidationService userValidationService)
    {
        this.usersService = usersService;
        this.userValidationService = userValidationService;
    }

    /**
     * Check if deploymentPlan can be managed by user, manage is checked in;
     * - Update deploymentPlan properties
     * - manage connectors
     * - manage filesystems
     *
     * @param ivUserToCheck  user to check permissions
     * @param deploymentPlan deploymentPlan to be managed
     * @return true if deploymentPlan can be managed by logged user
     */
    public Boolean checkIfPlanCanBeManagedByUser(final String ivUserToCheck, final DeploymentPlan deploymentPlan)
    {
        LOG.debug("[ManageValidationUtils] -> [checkPlanCanBeManaged]: checking if user: [{}] have permissions to manage the deploymentPlan: [{}]", ivUserToCheck, deploymentPlan.getId());
        String ivUser = this.checkIvUser(ivUserToCheck);

        boolean canBeManagedByUser = false;
        if (!deploymentPlan.getStatus().equals(DeploymentStatus.DEPLOYED))
        {
            LOG.debug("[ManageValidationUtils] -> [checkPlanCanBeManaged]: the deploymentPlan [{}] can be managed because its status is [{}]", deploymentPlan.getId(), deploymentPlan.getStatus());
            canBeManagedByUser = true;
        }
        else
        {
            Release release = deploymentPlan.getReleaseVersion().getRelease();
            switch (Environment.valueOf(deploymentPlan.getEnvironment()))
            {
                case INT:
                    // All users can manage plans in INT
                    LOG.debug("[ManageValidationUtils] -> [checkPlanCanBeManaged]: Plan [{}] can be manage by user [{}] in environment [{}]", deploymentPlan.getId(), ivUser, deploymentPlan.getEnvironment());
                    canBeManagedByUser = true;
                    break;
                case PRE:
                    // TRUE if Release is automanage in Pre OR user belong to SQA or PAdmin teams
                    List<String> sqaUserCodes = userValidationService.getUserCodesByTeam(RoleType.SQA_ADMIN);

                    if (release.isAutomanageInPre() ||
                            (usersService.isPlatformAdmin(ivUser) || sqaUserCodes.contains(ivUser.toUpperCase())))
                    {
                        LOG.debug("[ManageValidationUtils] -> [checkPlanCanBeManaged]: Plan [{}] can be manage by user [{}] in environment [{}]", deploymentPlan.getId(), ivUser, deploymentPlan.getEnvironment());
                        canBeManagedByUser = true;
                    }
                    break;
                case PRO:
                    // TRUE if Release is automanage in Pro OR user belong to SS or PAdmin teams
                    List<String> serviceUserCodes = userValidationService.getUserCodesByTeam(RoleType.SERVICE_SUPPORT);
                    if (release.isEnabledAutomanageInPro() ||
                            (usersService.isPlatformAdmin(ivUser) || (serviceUserCodes.contains(ivUser.toUpperCase()))))
                    {
                        LOG.debug("[ManageValidationUtils] -> [checkPlanCanBeManaged]: Plan [{}] can be manage by user [{}] in environment [{}]", deploymentPlan.getId(), ivUser, deploymentPlan.getEnvironment());
                        canBeManagedByUser = true;
                    }
                    break;
                default:
                    LOG.error("[ManageValidationUtils] -> [checkPlanCanBeManaged]: Unknown environment [{}]", deploymentPlan.getEnvironment());
                    throw new NovaException(CommonError.getWrongEnvironmentError(Constants.CommonErrorConstants.COMMON_ERROR_CLASS_NAME, deploymentPlan.getEnvironment()));
            }
            if (!canBeManagedByUser)
            {
                LOG.debug("[ManageValidationUtils] -> [checkPlanCanBeManaged]: User code: [{}] don`t have enough permissions to manage the deploymentPlan id: [{}] in environment: [{}]",
                        ivUser, deploymentPlan.getId(), deploymentPlan.getEnvironment());
            }
            LOG.debug("[ManageValidationUtils] -> [checkPlanCanBeManaged]: checked if user [{}] have permissions to manage the deploymentPlan id: [{}]: deploymentPlan can be managed by user: [{}]", ivUser, deploymentPlan.getId(), canBeManagedByUser);
        }
        return canBeManagedByUser;
    }

    /**
     * Check if deployed service can be managed by user, manage is checked in;
     * - Stop, start, restart instances, services or plans
     *
     * @param ivUserToCheck  user to check permissions
     * @param deploymentPlan deploymentPlan to be managed
     * @return true if deploymentPlan can be managed by logged user
     */
    public Boolean checkIfServiceActionCanBeManagedByUser(String ivUserToCheck, DeploymentPlan deploymentPlan)
    {
        LOG.debug("[ManageValidationUtils] -> [checkIfServiceActionCanBeManagedByUser]: checking if user: [{}] have permissions to manage the deploymentPlan [{}]", ivUserToCheck, deploymentPlan.getId());
        String ivUser = this.checkIvUser(ivUserToCheck);

        boolean canBeManagedByUser = false;
        if (!deploymentPlan.getStatus().equals(DeploymentStatus.DEPLOYED))
        {
            LOG.debug("[ManageValidationUtils] -> [checkIfServiceActionCanBeManagedByUser]: the deploymentPlan [{}] can be managed because its status is [{}]", deploymentPlan.getId(), deploymentPlan.getStatus());
            canBeManagedByUser = true;
        }
        else
        {
            Release release = deploymentPlan.getReleaseVersion().getRelease();
            switch (Environment.valueOf(deploymentPlan.getEnvironment()))
            {
                case INT:
                    // All users can manage plans in INT
                    LOG.debug("[ManageValidationUtils] -> [checkIfServiceActionCanBeManagedByUser]: Plan [{}] can be manage by user [{}] in environment [{}]", deploymentPlan.getId(), ivUser, deploymentPlan.getEnvironment());
                    canBeManagedByUser = true;
                    break;
                case PRE:
                    // TRUE if Release is automanage in Pre OR user belong to SQA or PAdmin teams
                    List<String> sqaUserCodes = userValidationService.getUserCodesByTeam(RoleType.SQA_ADMIN);

                    if (release.isAutomanageInPre() ||
                            (usersService.isPlatformAdmin(ivUser) || sqaUserCodes.contains(ivUser.toUpperCase())))
                    {
                        LOG.debug("[ManageValidationUtils] -> [checkIfServiceActionCanBeManagedByUser]: Plan [{}] can be manage by user [{}] in environment [{}]", deploymentPlan.getId(), ivUser, deploymentPlan.getEnvironment());
                        canBeManagedByUser = true;
                    }
                    break;
                case PRO:
                    // TRUE if user belong to SS, PAdmin or is the Product Owner.
                    List<String> serviceUserCodes = userValidationService.getUserCodesByTeam(RoleType.SERVICE_SUPPORT);
                    List<USUserDTO> productOwners = usersService.getProductUsersByTeam(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId(), RoleType.PRODUCT_OWNER.name());
                    boolean isProductOwner = productOwners.stream().anyMatch(users -> users.getUserCode().equalsIgnoreCase(ivUser));
                    if (usersService.isPlatformAdmin(ivUser) || serviceUserCodes.contains(ivUser.toUpperCase()) || isProductOwner)
                    {
                        LOG.debug("[ManageValidationUtils] -> [checkIfServiceActionCanBeManagedByUser]: Plan [{}] can be manage by user [{}] in environment [{}]", deploymentPlan.getId(), ivUser, deploymentPlan.getEnvironment());
                        canBeManagedByUser = true;
                    }
                    break;
                default:
                    LOG.error("[ManageValidationUtils] -> [checkIfServiceActionCanBeManagedByUser]: Unknown environment [{}]", deploymentPlan.getEnvironment());
                    throw new NovaException(CommonError.getWrongEnvironmentError(Constants.CommonErrorConstants.COMMON_ERROR_CLASS_NAME, deploymentPlan.getEnvironment()));
            }
            if (!canBeManagedByUser)
            {
                LOG.debug("[ManageValidationUtils] -> [checkIfServiceActionCanBeManagedByUser]: User code: [{}] don`t have enough permissions to manage the deploymentPlan id: [{}] in environment: [{}]",
                        ivUser, deploymentPlan.getId(), deploymentPlan.getEnvironment());
            }
            LOG.debug("[ManageValidationUtils] -> [checkIfServiceActionCanBeManagedByUser]: checked if user [{}] have permissions to manage the deploymentPlan id: [{}]: deploymentPlan can be managed by user: [{}]", ivUser, deploymentPlan.getId(), canBeManagedByUser);
        }
        return canBeManagedByUser;
    }

    ////////////////////////////////////// PRIVATE METHODS ///////////////////////////////

    /**
     * Check the iv user to avoid null pointer or iv user null when the iv user provides from nova context null
     * In this case, we understand that the user is the NOVA User
     *
     * @param ivUserToCheck the iv-user to check
     * @return a iv-user resolved
     */
    private String checkIvUser(final String ivUserToCheck)
    {
        String ivUser = Constants.IMMUSER;

        if (Strings.isNullOrEmpty(ivUserToCheck))
        {
            LOG.debug("[ManageValidationUtils] -> [checkIvUser]: the ivUser to check is null. Replaced by IMMUSER");
        }
        else
        {
            ivUser = ivUserToCheck;
        }

        LOG.debug("[ManageValidationUtils] -> [checkIvUser]: the ivUser obtained is: [{}]", ivUser);

        return ivUser;
    }

    /**
     * Check if broker can be managed by user, manage is checked in;
     * * - Stop, start, restart broker or broker nodes
     *
     * @param ivUserToCheck
     * @param broker
     * @return
     */
    @Transactional(readOnly = true)
    public Boolean checkIfBrokerActionCanBeManagedByUser(String ivUserToCheck, Broker broker)
    {
        LOG.debug("[ManageValidationUtils] -> [checkIfBrokerActionCanBeManagedByUser]: checking if user: [{}] have permissions to manage the broker [{}]", ivUserToCheck, broker.getId());
        String ivUser = this.checkIvUser(ivUserToCheck);

        if (!broker.getEnvironment().equals(Environment.PRO.name()))
        {
            LOG.debug("[ManageValidationUtils] -> [checkIfServiceActionCanBeManagedByUser]: the broker [{}] can be managed because its environment is [{}]", broker.getId(), broker.getEnvironment());
            return true;
        }
        if (!broker.getDeploymentServices().stream().distinct().anyMatch(deploymentService -> deploymentService.getDeploymentSubsystem().getDeploymentPlan().getStatus().equals(DeploymentStatus.DEPLOYED)))
        {
            LOG.debug("[ManageValidationUtils] -> [checkIfServiceActionCanBeManagedByUser]: Broker [{}] has not deployments plans in status deployed associated, can be manage by user [{}] in environment [{}]", broker.getId(), ivUser, broker.getEnvironment());
            return true;
        }
        // Check user belongs to Platform Admin or is the Product Owner.
        List<USUserDTO> productOwners = usersService.getProductUsersByTeam(broker.getProduct().getId(), RoleType.PRODUCT_OWNER.name());
        boolean isProductOwner = productOwners.stream().anyMatch(users -> users.getUserCode().equalsIgnoreCase(ivUser));
        if (usersService.isPlatformAdmin(ivUser) || isProductOwner)
        {
            LOG.debug("[ManageValidationUtils] -> [checkIfServiceActionCanBeManagedByUser]: Broker [{}] can be manage by user [{}] in environment [{}]", broker.getId(), ivUser, broker.getEnvironment());
            return true;
        }

        return false;
    }
}
