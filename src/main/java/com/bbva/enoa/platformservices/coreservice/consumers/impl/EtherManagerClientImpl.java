package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.CallbackInfoDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.StatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.client.feign.nova.rest.IRestHandlerEthermanagerapi;
import com.bbva.enoa.apirestgen.ethermanagerapi.client.feign.nova.rest.IRestListenerEthermanagerapi;
import com.bbva.enoa.apirestgen.ethermanagerapi.client.feign.nova.rest.impl.RestHandlerEthermanagerapi;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.*;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.core.novabootstarter.util.EtherServiceNamingUtils;
import com.bbva.enoa.datamodel.model.user.entities.ExternalUserPermission;
import com.bbva.enoa.platformservices.coreservice.common.util.CallbackService;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IEtherManagerClient;
import com.bbva.enoa.platformservices.coreservice.etherapi.exceptions.EtherError;
import com.bbva.enoa.platformservices.coreservice.externaluserpermissionapi.exceptions.ExternalUserPermissionError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * Ether Manager API Client
 */
@Service
public class EtherManagerClientImpl implements IEtherManagerClient
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(EtherManagerClientImpl.class);

    /**
     * Rest interface
     */
    private final IRestHandlerEthermanagerapi iRestHandlerEthermanagerapi;
    /**
     * Callback service
     */
    private final CallbackService callbackService;
    /**
     * API service
     */
    private RestHandlerEthermanagerapi restHandlerEthermanagerapi;

    @Autowired
    public EtherManagerClientImpl(final CallbackService callbackService, IRestHandlerEthermanagerapi iRestHandlerEthermanagerapi)
    {

        this.callbackService = callbackService;
        this.iRestHandlerEthermanagerapi = iRestHandlerEthermanagerapi;
    }

    /**
     * Init the handler and listener.
     */
    @PostConstruct
    public void init()
    {
        this.restHandlerEthermanagerapi = new RestHandlerEthermanagerapi(this.iRestHandlerEthermanagerapi);
    }

    @Override
    public boolean deployEtherPlan(EtherDeploymentDTO etherDeploymentDTO)
    {
        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();
        Integer deploymentId = etherDeploymentDTO.getDeploymentId();

        LOG.debug("[EtherManagerAPI Client] -> [deployEtherPlan]: Calling Ether Manager Service to deployPlan {} ", deploymentId);

        this.restHandlerEthermanagerapi.deployService(new IRestListenerEthermanagerapi()
        {
            @Override
            public void deployService()
            {
                LOG.debug("[EtherManagerAPI Client] -> [deployService]: Launched deploying plan [{}]", deploymentId);
                response.set(true);
            }

            @Override
            public void deployServiceErrors(Errors outcome)
            {
                LOG.error("[EtherManagerAPI Client] -> [deployService]: Error deploying plan [{}]. Error message: [{}]", deploymentId, outcome.getFirstErrorMessage());
                response.set(false);
            }
        }, this.addCallbackToEtherDeployment(etherDeploymentDTO, CallbackService.CALLBACK_PLAN + deploymentId + CallbackService.DEPLOY,
                CallbackService.CALLBACK_PLAN + deploymentId + CallbackService.DEPLOY_ERROR));

        if (Boolean.FALSE.equals(response.get()))
        {
            LOG.debug("[EtherManagerAPI Client] -> [deployEtherPlan]: deploying plan: [{}] was failed. Client response: [{}]", deploymentId, response.get());
            throw new NovaException(EtherError.getDeployEtherFromDeploymentManagerError(deploymentId));
        }
        return response.get();
    }

    @Override
    public boolean configureServicesForLogging(EtherDeploymentDTO etherDeploymentDTO)
    {
        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();
        Integer deploymentId = etherDeploymentDTO.getDeploymentId();

        LOG.debug("[EtherManagerAPI Client] -> [configureServicesForLogging]: Calling Ether Manager Service to configure services of plan {} to log/trace in Ether ...", deploymentId);

        this.restHandlerEthermanagerapi.configureServicesForLogging(new IRestListenerEthermanagerapi()
        {
            @Override
            public void configureServicesForLogging()
            {
                LOG.debug("[EtherManagerAPI Client] -> [configureServicesForLogging]: Services of plan [{}] configured successfully to log/trace in Ether", deploymentId);
                response.set(true);
            }

            @Override
            public void configureServicesForLoggingErrors(Errors outcome)
            {
                LOG.error("[EtherManagerAPI Client] -> [configureServicesForLogging]: Error configuring services of plan [{}] to log/trace in Ether. Error message: [{}]", deploymentId, outcome.getBodyExceptionMessage());
                response.set(false);
            }
        }, etherDeploymentDTO);

        return response.get();
    }

    @Override
    public boolean removeEtherPlan(EtherDeploymentDTO etherDeploymentDTO)
    {
        Integer deploymentId = etherDeploymentDTO.getDeploymentId();
        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        LOG.debug("[EtherManagerAPI Client] -> [removeEtherPlan]: Calling Ether Manager Service to removePlan [{}]", deploymentId);

        this.restHandlerEthermanagerapi.removeService(new IRestListenerEthermanagerapi()
        {
            @Override
            public void removeService()
            {
                LOG.debug("[EtherManagerAPI Client] -> [removeEtherPlan]: Launched removing plan [{}]", deploymentId);
                response.set(true);
            }

            @Override
            public void removeServiceErrors(Errors outcome)
            {
                LOG.error("[EtherManagerAPI Client] -> [removeEtherPlan]: Error removing plan [{}]. Error: {}", deploymentId, outcome.getBodyExceptionMessage());
                response.set(false);
            }
        }, this.addCallbackToEtherDeployment(etherDeploymentDTO, CallbackService.CALLBACK_DELETE_PLAN + deploymentId,
                CallbackService.CALLBACK_PLAN + deploymentId + CallbackService.REMOVE_ERROR));

        return response.get();
    }

    @Override
    public boolean replaceEtherPlan(final EtherDeploymentDTO oldEtherDeploymentDTO, final EtherDeploymentDTO newEtherDeploymentDTO)
    {
        Integer oldDeploymentId = oldEtherDeploymentDTO.getDeploymentId();
        Integer newDeploymentId = newEtherDeploymentDTO.getDeploymentId();
        LOG.debug("[EtherManagerAPI Client] -> [replaceEtherPlan]: Calling Ether Manager Service to replacePlan [{}] "
                + "with new plan [{}]", oldDeploymentId, newDeploymentId);

        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        //Create RQ
        EtherRedeploymentDTO redeployment = new EtherRedeploymentDTO();
        redeployment.setOldServices(new EtherDeploymentDTO(oldEtherDeploymentDTO));

        this.addCallbackToEtherDeployment(redeployment.getOldServices(),
                CallbackService.CALLBACK_PLAN + oldDeploymentId + CallbackService.REPLACE + newDeploymentId,
                CallbackService.CALLBACK_PLAN + oldDeploymentId + CallbackService.REPLACE + newDeploymentId + CallbackService.REPLACE_ERROR);

        redeployment.setNewServices(new EtherDeploymentDTO(newEtherDeploymentDTO));
        this.addCallbackToEtherDeployment(redeployment.getNewServices(),
                CallbackService.CALLBACK_PLAN + oldDeploymentId + CallbackService.REPLACE + newDeploymentId,
                CallbackService.CALLBACK_PLAN + oldDeploymentId + CallbackService.REPLACE + newDeploymentId + CallbackService.REPLACE_ERROR);

        this.restHandlerEthermanagerapi.redeployService(new IRestListenerEthermanagerapi()
        {
            @Override
            public void redeployService()
            {
                LOG.debug("[EtherManagerAPI Client] -> [replaceEtherPlan]: Replacing plan [{}] with new plan [{}]", oldDeploymentId, newDeploymentId);
                response.set(true);
            }

            @Override
            public void redeployServiceErrors(Errors outcome)
            {
                LOG.error("[EtherManagerAPI Client] -> [replaceEtherPlan]: Error replacing plan [{}] with new plan" + " [{}]. Error message: [{}]", oldDeploymentId, newDeploymentId, outcome.getBodyExceptionMessage());
                response.set(false);
            }
        }, redeployment);

        return response.get();
    }

    @Override
    public ServiceStatusDTO getServiceStatus(final EtherDeploymentDTO etherDeploymentDTO)
    {
        Integer deploymentServiceId = etherDeploymentDTO.getEtherServices()[0].getDeploymentServiceId();
        LOG.debug("[EtherManagerAPI Client] -> [getServiceStatus]: Calling Ether manager to get instances of service [{}] with ether deployment DTO: [{}]", deploymentServiceId, etherDeploymentDTO);

        SingleApiClientResponseWrapper<EtherStatusDTO> response = new SingleApiClientResponseWrapper<>();
        response.set(this.createEmptyServiceNumber());

        this.restHandlerEthermanagerapi.getServiceStatus(new IRestListenerEthermanagerapi()
        {
            @Override
            public void getServiceStatus(EtherStatusDTO outcome)
            {
                LOG.debug("[EtherManagerAPI Client] -> [getServiceStatus]: Launched to get container of service [{}] with parameters: [{}]", deploymentServiceId, etherDeploymentDTO);
                response.set(outcome);
            }

            @Override
            public void getServiceStatusErrors(Errors outcome)
            {
                LOG.error("[EtherManagerAPI Client] -> [getServiceStatus]: Error getting running instances for service: [{}]. Error: {}", deploymentServiceId, outcome.getBodyExceptionMessage());
            }
        }, etherDeploymentDTO);

        return this.buildServiceStatus(response.get(), deploymentServiceId);
    }

    @Override
    public boolean readyToDeploy(final CheckReadyToDeployRequestDTO checkReadyToDeployRequestDTO)
    {
        LOG.debug("[EtherManagerAPI Client] -> [getServiceStatus]: Checking if ether resources are ready for deployment [{}]", checkReadyToDeployRequestDTO);

        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();
        response.set(Boolean.FALSE);

        this.restHandlerEthermanagerapi.readyToDeploy(new IRestListenerEthermanagerapi()
        {
            @Override
            public void readyToDeploy(Boolean outcome)
            {
                LOG.debug("[EtherManagerAPI Client] -> [readyToDeploy]: Checked if ether resources  [{}] are ready for deployment", checkReadyToDeployRequestDTO);
                response.set(outcome);
            }

            @Override
            public void readyToDeployErrors(Errors outcome)
            {
                LOG.error("[EtherManagerAPI Client] -> [readyToDeploy]: Error checking if ether resources are ready for deployment. Error: {}", outcome.getBodyExceptionMessage());
            }
        }, checkReadyToDeployRequestDTO);

        return response.get();
    }

    @Override
    public StatusDTO getDeploymentStatus(final EtherDeploymentDTO etherDeploymentDTO)
    {
        Integer deploymentPlanId = etherDeploymentDTO.getDeploymentId();

        LOG.debug("[EtherManagerAPI Client] -> [getDeploymentStatus]: Calling Ether manager to get instances of deployment [{}]", deploymentPlanId);

        SingleApiClientResponseWrapper<EtherStatusDTO> response = new SingleApiClientResponseWrapper<>();
        response.set(this.createEmptyServiceNumber());

        this.restHandlerEthermanagerapi.getDeploymentStatus(new IRestListenerEthermanagerapi()
        {
            @Override
            public void getDeploymentStatus(EtherStatusDTO outcome)
            {
                LOG.debug("[EtherManagerAPI Client] -> [getDeploymentStatus]: Launched to get container of deployment [{}]", deploymentPlanId);
                response.set(outcome);
            }

            @Override
            public void getDeploymentStatusErrors(Errors outcome)
            {
                LOG.error("[EtherManagerAPI Client] -> [getDeploymentStatus]: Error getting running instances for deployment: [{}]. Error: {}", deploymentPlanId, outcome.getBodyExceptionMessage());
            }
        }, etherDeploymentDTO);

        return this.buildServiceStatus(response.get());
    }

    @Override
    public EtherSubsystemStatusDTO[] getSubsystemStatus(final EtherSubsystemDTO[] etherSubsystemDTOArray)
    {
        LOG.debug("[EtherManagerAPI Client] -> [getSubsystemStatus]: Calling Ether manager to get instances of {} subsystems", etherSubsystemDTOArray.length);

        SingleApiClientResponseWrapper<EtherSubsystemStatusDTO[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerEthermanagerapi.getSubsystemStatus(new IRestListenerEthermanagerapi()
        {
            @Override
            public void getSubsystemStatus(EtherSubsystemStatusDTO[] outcome)
            {
                LOG.debug("[EtherManagerAPI Client] -> [getSubsystemStatus]: Launched to get services of {} subsystems", etherSubsystemDTOArray.length);
                response.set(outcome);
            }

            @Override
            public void getSubsystemStatusErrors(Errors outcome)
            {
                LOG.error("[EtherManagerAPI Client] -> [getSubsystemStatus]: Error getting running services. Error: {}", outcome.getBodyExceptionMessage());
                response.set(new EtherSubsystemStatusDTO[0]);
            }
        }, etherSubsystemDTOArray);

        return response.get();
    }

    @Override
    public void startEtherService(EtherDeploymentDTO etherDeploymentDTO)
    {
        Integer numberOfServices = etherDeploymentDTO.getEtherServices().length;
        Integer deploymentId = etherDeploymentDTO.getDeploymentId();
        LOG.debug("[EtherManagerAPI Client] -> [startEtherService]: Calling Ether manager to start {} services of deployment with id {} ", numberOfServices, deploymentId);

        this.restHandlerEthermanagerapi.startService(new IRestListenerEthermanagerapi()
        {
            @Override
            public void startService()
            {
                LOG.debug("[EtherManagerAPI Client] -> [startService]: Launched starting {} services of deployment with id {} ", numberOfServices, deploymentId);
            }

            @Override
            public void startServiceErrors(Errors outcome)
            {
                LOG.error("[EtherManagerAPI Client] -> [startService]: Launched starting {} services of deployment with id [{}]. Error message: [{}]", numberOfServices, deploymentId, outcome.getBodyExceptionMessage());
            }
        }, etherDeploymentDTO);
    }

    @Override
    public void stopEtherService(EtherDeploymentDTO etherDeploymentDTO)
    {
        Integer numberOfServices = etherDeploymentDTO.getEtherServices().length;
        Integer deploymentId = etherDeploymentDTO.getDeploymentId();
        LOG.debug("[EtherManagerAPI Client] -> [stopEtherService]: Calling Ether manager to stop {} services of deployment with id {} ", numberOfServices, deploymentId);

        this.restHandlerEthermanagerapi.stopService(new IRestListenerEthermanagerapi()
        {
            @Override
            public void stopService()
            {
                LOG.debug("[EtherManagerAPI Client] -> [stopService]: Launched stoping {} services of deployment with id {} ", numberOfServices, deploymentId);
            }

            @Override
            public void stopServiceErrors(Errors outcome)
            {
                LOG.error("[EtherManagerAPI Client] -> [stopService]: Launched stoping {} services of deployment with id {} . Error: {}", numberOfServices, deploymentId, outcome.getBodyExceptionMessage());
            }
        }, etherDeploymentDTO);
    }

    @Override
    public void restartEtherService(EtherDeploymentDTO etherDeploymentDTO)
    {
        Integer numberOfServices = etherDeploymentDTO.getEtherServices().length;
        Integer deploymentId = etherDeploymentDTO.getDeploymentId();
        LOG.debug("[EtherManagerAPI Client] -> [restartEtherService]: Calling Ether manager to restart {} services of deployment with id {} ", numberOfServices, deploymentId);

        this.restHandlerEthermanagerapi.restartService(new IRestListenerEthermanagerapi()
        {
            @Override
            public void restartService()
            {
                LOG.debug("[EtherManagerAPI Client] -> [restartService]: Launched restarting {} services of deployment with id {} ", numberOfServices, deploymentId);
            }

            @Override
            public void restartServiceErrors(Errors outcome)
            {
                LOG.error("[EtherManagerAPI Client] -> [restartService]: Launched restarting {} services of deployment with id {} . Error: {}", numberOfServices, deploymentId, outcome.getBodyExceptionMessage());
            }
        }, etherDeploymentDTO);
    }

    @Override
    public EtherManagerConfigStatusDTO checkEtherResourcesStatus(final CheckEtherResourcesStatusRequestDTO etherCheckConfigurationRequest)
    {
        LOG.debug("[EtherManagerAPI Client] -> [checkEtherResourcesStatus]: Checking status of Ether resources [{}]", etherCheckConfigurationRequest);

        SingleApiClientResponseWrapper<EtherManagerConfigStatusDTO> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerEthermanagerapi.checkEtherResourcesStatus(new IRestListenerEthermanagerapi()
        {
            @Override
            public void checkEtherResourcesStatus(final EtherManagerConfigStatusDTO outcome)
            {
                LOG.debug("[EtherManagerAPI Client] -> [checkEtherResourcesStatus]: Checked status of Ether resources [{}]", etherCheckConfigurationRequest);
                response.set(outcome);
            }

            @Override
            public void checkEtherResourcesStatusErrors(final Errors outcome)
            {
                final String errorMsg = String.format("[EtherManagerAPI Client] -> [checkEtherResourcesStatus]: Error checking status of Ether resources [%s]" +
                        ". Error: [%s]", etherCheckConfigurationRequest, outcome.getMessage());
                throw new NovaException(EtherError.getCheckingEtherResourcesStatusError(), errorMsg);
            }
        }, etherCheckConfigurationRequest);

        return response.get();
    }

    @Override
    public EtherManagerConfigStatusDTO configureEtherInfrastructure(final EtherConfigurationRequestDTO etherManagerConfigurationRequest)
    {
        String environment = etherManagerConfigurationRequest.getEnvironment();
        String productName = etherManagerConfigurationRequest.getProductName();
        String namespace = etherManagerConfigurationRequest.getNamespace();
        LOG.debug("[EtherManagerAPI Client] -> [configureEtherInfrastructure]: Calling Ether manager to configure "
                + "the product [{}], environment [{}] and namespace [{}] ", productName, environment, namespace);

        SingleApiClientResponseWrapper<EtherManagerConfigStatusDTO> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerEthermanagerapi.configureEtherInfrastructure(new IRestListenerEthermanagerapi()
        {
            @Override
            public void configureEtherInfrastructure(EtherManagerConfigStatusDTO outcome)
            {
                LOG.debug("[EtherManagerAPI Client] -> [configureEtherInfrastructure]: "
                                + "Launched to configure ether namespace "
                                + "for product [{}], environment [{}] and namespace [{}] ",
                        productName, environment, namespace);
                response.set(outcome);
            }

            @Override
            public void configureEtherInfrastructureErrors(Errors outcome)
            {
                String errorMsg = "[EtherManagerAPI Client] -> [configureEtherInfrastructureError]: Error launching request to "
                        + "configure ether, "
                        + "product [" + productName + "], environment [" + environment + "] and namespace [" + namespace + "]."
                        + "Received response: [{}]" + outcome.getMessages();
                throw new NovaException(EtherError.getEtherManagerCallError(), errorMsg);
            }
        }, etherManagerConfigurationRequest);

        return response.get();
    }

    @Override
    public void addUsersToGroup(final EtherUserManagementDTO etherUserManagementDTO)
    {
        if (etherUserManagementDTO.getUsers().length > 0)
        {
            final String commaSeparatedUsers = String.join(", ", etherUserManagementDTO.getUsers());
            LOG.debug("[EtherManagerAPI Client] -> [addUsersToGroup]: Adding users {} to Ether group [{}]",
                    commaSeparatedUsers, etherUserManagementDTO.getGroupId());

            this.restHandlerEthermanagerapi.addUsersToGroup(new IRestListenerEthermanagerapi()
            {
                @Override
                public void addUsersToGroup()
                {
                    LOG.debug("[EtherManagerAPI Client] -> [addUsersToGroup]: Added users {} to Ether group [{}]",
                            commaSeparatedUsers, etherUserManagementDTO.getGroupId());
                }

                @Override
                public void addUsersToGroupErrors(Errors outcome)
                {
                    LOG.warn("[EtherManagerAPI Client] -> [addUsersToGroup]: Error adding users {} to Ether group [{}]. Error: [{}]",
                            commaSeparatedUsers, etherUserManagementDTO.getGroupId(), outcome.getMessage());

                    throw new NovaException(EtherError.getAddingUsersError(etherUserManagementDTO.getGroupId()));
                }
            }, etherUserManagementDTO);
        }
    }

    @Override
    public void removeUsersFromGroup(final EtherUserManagementDTO etherUserManagementDTO)
    {
        if (etherUserManagementDTO.getUsers().length > 0)
        {
            final String commaSeparatedUsers = String.join(", ", etherUserManagementDTO.getUsers());
            LOG.debug("[EtherManagerAPI Client] -> [removeUsersFromGroup]: Removing users {} from Ether group [{}]",
                    commaSeparatedUsers, etherUserManagementDTO.getGroupId());

            this.restHandlerEthermanagerapi.removeUsersFromGroup(new IRestListenerEthermanagerapi()
            {
                @Override
                public void removeUsersFromGroup()
                {
                    LOG.debug("[EtherManagerAPI Client] -> [removeUsersFromGroup]: Removed users {} from Ether group [{}]",
                            commaSeparatedUsers, etherUserManagementDTO.getGroupId());
                }

                @Override
                public void removeUsersFromGroupErrors(Errors outcome)
                {
                    LOG.warn("[EtherManagerAPI Client] -> [removeUsersFromGroup]: Error removing users {} from Ether group [{}]. Error: [{}]",
                            commaSeparatedUsers, etherUserManagementDTO.getGroupId(), outcome.getMessage());

                    throw new NovaException(EtherError.getRemovingUsersError(etherUserManagementDTO.getGroupId()));
                }
            }, etherUserManagementDTO);
        }
    }

    @Override
    public void setPermission(final ExternalUserPermission externalUserPermission, final String namespace, final String environment)
    {
        // parse the permission
        final EtherPermissionDTO etherPermissionDTO = buildEtherPermissionDTO(externalUserPermission);

        LOG.debug("[EtherManagerAPI Client] -> [setPermission]: Setting the permission [{}] in namespace [{}] of environment [{}]",
                etherPermissionDTO, namespace, environment);

        this.restHandlerEthermanagerapi.setPermission(new IRestListenerEthermanagerapi()
        {
            @Override
            public void setPermission()
            {
                LOG.debug("[EtherManagerAPI Client] -> [setPermission]: Permission [{}] created in namespace [{}] of environment [{}]",
                        etherPermissionDTO, namespace, environment);
            }

            @Override
            public void setPermissionErrors(final Errors outcome)
            {
                LOG.error("[EtherManagerAPI Client] -> [setPermission]: Error creating the the permission [{}] in namespace [{}] of environment [{}]. Error: [{}]",
                        etherPermissionDTO, namespace, environment, outcome.getMessage());

                throw new NovaException(ExternalUserPermissionError.getCreatingPermissionError(), outcome);
            }
        }, etherPermissionDTO, namespace, environment);
    }

    @Override
    public void unsetPermission(final ExternalUserPermission externalUserPermission, final String namespace, final String environment)
    {
        // parse the permission
        final EtherPermissionDTO etherPermissionDTO = buildEtherPermissionDTO(externalUserPermission);

        LOG.debug("[EtherManagerAPI Client] -> [unsetPermission]: Unsetting the permission [{}] in namespace [{}] of environment [{}]",
                etherPermissionDTO, namespace, environment);

        this.restHandlerEthermanagerapi.unsetPermission(new IRestListenerEthermanagerapi()
        {
            @Override
            public void unsetPermission()
            {
                LOG.debug("[EtherManagerAPI Client] -> [unsetPermission]: Permission [{}] deleted in namespace [{}] of environment [{}]",
                        etherPermissionDTO, namespace, environment);
            }

            @Override
            public void unsetPermissionErrors(final Errors outcome)
            {
                LOG.debug("[EtherManagerAPI Client] -> [unsetPermission]: Error deleting the the permission [{}] in namespace [{}] of environment [{}]. Error: [{}]",
                        etherPermissionDTO, namespace, environment, outcome.getMessage());

                throw new NovaException(ExternalUserPermissionError.getDeletingPermissionError(), outcome);
            }
        }, etherPermissionDTO, namespace, environment);
    }

    /**
     * Add callback to ether deployment DTO
     *
     * @param etherDeploymentDTO deployment data transfer object
     * @param successUrl         success url path
     * @param errorUrl           error url path
     * @return an EtherDeploymentDTO object
     */
    private EtherDeploymentDTO addCallbackToEtherDeployment(EtherDeploymentDTO etherDeploymentDTO, final String successUrl, final String errorUrl)
    {
        CallbackInfoDto callbackInfoDto = this.callbackService.buildCallback(successUrl, errorUrl);

        EtherResponseDTO etherResponseDTO = new EtherResponseDTO();
        EtherCallbackDTO errorEtherCallbackDto = new EtherCallbackDTO();
        EtherCallbackDTO successEtherCallbackDto = new EtherCallbackDTO();

        BeanUtils.copyProperties(callbackInfoDto.getErrorCallback(), errorEtherCallbackDto);
        BeanUtils.copyProperties(callbackInfoDto.getSuccessCallback(), successEtherCallbackDto);

        etherResponseDTO.setError(errorEtherCallbackDto);
        etherResponseDTO.setSuccess(successEtherCallbackDto);

        etherDeploymentDTO.setEtherCallback(etherResponseDTO);

        return etherDeploymentDTO;
    }

    /**
     * Create empty service number object
     *
     * @return empty service number object
     */
    private EtherStatusDTO createEmptyServiceNumber()
    {
        EtherStatusDTO result = new EtherStatusDTO();

        result.setRunningContainers(0);
        result.setTotalContainers(0);

        return result;
    }

    private ServiceStatusDTO buildServiceStatus(final EtherStatusDTO etherServiceStatusDTO, final Integer serviceId)
    {
        LOG.debug("[EtherManagerAPI Client] -> [buildServiceStatus]: Building DTO with number of containers etherServiceStatusDTO {}", etherServiceStatusDTO);

        ServiceStatusDTO serviceStatusDTO = new ServiceStatusDTO();

        StatusDTO statusDTO = new StatusDTO();
        statusDTO.setTotal(etherServiceStatusDTO.getTotalContainers());
        statusDTO.setRunning(etherServiceStatusDTO.getRunningContainers());

        serviceStatusDTO.setServiceId(serviceId);
        serviceStatusDTO.setStatus(statusDTO);

        return serviceStatusDTO;
    }

    private StatusDTO buildServiceStatus(final EtherStatusDTO etherServiceStatusDTO)
    {
        LOG.debug("[EtherManagerAPI Client] -> [buildServiceStatus]: Building DTO with number of containers etherServiceStatusDTO {}", etherServiceStatusDTO);

        StatusDTO statusDTO = new StatusDTO();

        statusDTO.setTotal(etherServiceStatusDTO.getTotalContainers());
        statusDTO.setRunning(etherServiceStatusDTO.getRunningContainers());

        return statusDTO;
    }

    private EtherPermissionDTO buildEtherPermissionDTO(final ExternalUserPermission externalUserPermission)
    {
        final EtherPermissionDTO etherPermissionDTO = new EtherPermissionDTO();
        etherPermissionDTO.setPermission(externalUserPermission.getPermission());
        etherPermissionDTO.setConsumer(externalUserPermission.getConsumer());
        etherPermissionDTO.setService(externalUserPermission.getService());
        etherPermissionDTO.setResourceType(externalUserPermission.getResourceType());
        etherPermissionDTO.setType(externalUserPermission.getPermissionType());

        if ("MonitorResource".equalsIgnoreCase(externalUserPermission.getResourceType()))
        {
            final String[] resourceItems = externalUserPermission.getResources().split("-");

            // update resource with the monitor resource id
            etherPermissionDTO.setResources(new String[] { EtherServiceNamingUtils.getMRMonitoredResourceId(resourceItems[0], resourceItems[1])});
        }
        else
        {
            etherPermissionDTO.setResources(externalUserPermission.getResources() == null ? new String[0] : externalUserPermission.getResources().split(","));
        }

        return etherPermissionDTO;
    }
}
