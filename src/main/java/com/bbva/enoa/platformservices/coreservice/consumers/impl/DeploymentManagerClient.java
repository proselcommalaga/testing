package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.deploymentmanagerapi.client.feign.nova.rest.IRestHandlerDeploymentmanagerapi;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.client.feign.nova.rest.IRestListenerDeploymentmanagerapi;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.client.feign.nova.rest.impl.RestHandlerDeploymentmanagerapi;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployInstanceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeploySubsystemStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.HostMemoryHistoryItemDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.ReplacePlanInfoDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.InstanceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.StatusDTO;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.datamodel.model.common.entities.AbstractEntity;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.platformservices.coreservice.common.util.CallbackService;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDeploymentManagerClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.exceptions.StatisticsError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Deployment manager client
 */
@Slf4j
@Service
public class DeploymentManagerClient implements IDeploymentManagerClient
{
    @Autowired
    private IRestHandlerDeploymentmanagerapi iRestHandlerDeploymentmanagerapi;

    /**
     * API services.
     */
    private RestHandlerDeploymentmanagerapi restHandlerDeploymentmanagerapi;

    /**
     * Callback service
     */
    @Autowired
    private CallbackService callbackService;

    /**
     * Init the handler and listener.
     */
    @PostConstruct
    public void init()
    {
        this.restHandlerDeploymentmanagerapi = new RestHandlerDeploymentmanagerapi(this.iRestHandlerDeploymentmanagerapi);
    }

    @Override
    public boolean removePlan(DeploymentPlan plan)
    {
        // Ask the container cluster for the number of running instances of the service
        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();
        this.restHandlerDeploymentmanagerapi.removePlan(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void removePlan()
            {
                log.debug("[DeploymentManagerClient] -> [removePlan]: Launched undeployPlan plan {}", plan.getId());
                response.set(true);
            }

            @Override
            public void removePlanErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [removePlan]: Error removing plan [{}] : {}", plan.getId(), outcome.getBodyExceptionMessage());
                response.set(false);
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_DELETE_PLAN + plan.getId(),
                CallbackService.CALLBACK_PLAN + plan.getId() + CallbackService.REMOVE_ERROR), plan.getId());
        return response.get();
    }

    @Override
    public void restartInstance(final DeploymentInstance instance)
    {
        log.debug("[DeploymentManagerClient] -> [restartInstance]: calling Deployment Manager to start instance {}", instance.getId());
        this.restHandlerDeploymentmanagerapi.restartInstance(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void restartInstance()
            {
                log.debug("[DeploymentManagerClient] -> [restartInstance]: launched starting instance {}", instance.getId());
            }

            @Override
            public void restartInstanceErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [restartInstance]: error starting instance: {} - errorMessage: [{}]", instance.getId(), outcome.getFirstErrorMessage());
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_INSTANCE + instance.getId() + CallbackService.RESTART,
                CallbackService.CALLBACK_INSTANCE + instance.getId() + CallbackService.RESTART_ERROR), instance.getId());
    }

    @Override
    public void startInstance(final DeploymentInstance instance)
    {
        log.debug("[DeploymentManagerClient] -> [startInstance]: calling Deployment Manager to start instance {}", instance.getId());
        // Ask the container cluster to Start an instance
        this.restHandlerDeploymentmanagerapi.startInstance(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void startInstance()
            {
                log.debug("[DeploymentManagerClient] -> [startInstance]: launched starting instance {}", instance.getId());
            }

            @Override
            public void startInstanceErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [startInstance]: error starting instance: {} errorMessage: [{}]", instance.getId(), outcome.getFirstErrorMessage());
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_INSTANCE + instance.getId() + CallbackService.START,
                CallbackService.CALLBACK_INSTANCE + instance.getId() + CallbackService.START_ERROR), instance.getId());
    }

    @Override
    public void stopInstance(final DeploymentInstance instance)
    {
        log.debug("[DeploymentManagerClient] -> [stopInstance]: Calling Deployment Manager to stop instance {}", instance.getId());
        // Ask the container cluster to Stop an instance
        this.restHandlerDeploymentmanagerapi.stopInstance(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void stopInstance()
            {
                log.debug("[DeploymentManagerClient] -> [stopInstance]: Launched stopping instance {}", instance.getId());
            }

            @Override
            public void stopInstanceErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [stopInstance]: Error stopping instance: {} errorMessage: [{}]", instance.getId(), outcome.getFirstErrorMessage());
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_INSTANCE + instance.getId() + CallbackService.STOP,
                CallbackService.CALLBACK_INSTANCE + instance.getId() + CallbackService.STOP_ERROR), instance.getId());
    }

    @Override
    public void restartPlan(final DeploymentPlan plan)
    {
        log.debug("[DeploymentManagerClient] -> [restartPlan]: Calling Deployment Manager to restart plan {}", plan.getId());
        this.restHandlerDeploymentmanagerapi.restartPlan(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void restartPlan()
            {
                log.debug("[DeploymentManagerClient] -> [restartPlan]: Launched restarting plan {}", plan.getId());
            }

            @Override
            public void restartPlanErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [restartPlan]: Error restarting plan: {} errorMessage: [{}]", plan.getId(), outcome.getFirstErrorMessage());
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_PLAN + plan.getId() + CallbackService.RESTART,
                CallbackService.CALLBACK_PLAN + plan.getId() + CallbackService.RESTART_ERROR), plan.getId());
    }

    @Override
    public void startPlan(final DeploymentPlan plan)
    {
        log.debug("[DeploymentManagerClient] -> [startPlan]:Calling Deployment Manager to start plan {}", plan.getId());
        // Ask the container cluster to Start a service
        this.restHandlerDeploymentmanagerapi.startPlan(new IRestListenerDeploymentmanagerapi()
        {

            @Override
            public void startPlan()
            {
                log.debug("[DeploymentManagerClient] -> [startPlan]: Launched starting plan {}", plan.getId());
            }

            @Override
            public void startPlanErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [startPlan]: Error starting plan: {} errorMessage: [{}]", plan.getId(), outcome.getFirstErrorMessage());
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_PLAN + plan.getId() + CallbackService.START,
                CallbackService.CALLBACK_PLAN + plan.getId() + CallbackService.START_ERROR), plan.getId());
    }


    @Override
    public void stopPlan(final DeploymentPlan plan)
    {
        log.debug("[DeploymentManagerClient] -> [stopPlan]: Calling Deployment Manager to stop plan {}", plan.getId());
        // Ask the container cluster to Stop all services of a deployment plan
        this.restHandlerDeploymentmanagerapi.stopPlan(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void stopPlan()
            {
                log.debug("[DeploymentManagerClient] -> [stopPlan]: Launched stopping plan {}", plan.getId());
            }

            @Override
            public void stopPlanErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [stopPlan]: Error stopping plan: {} errorMessage: [{}]", plan.getId(), outcome.getFirstErrorMessage());
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_PLAN + plan.getId() + CallbackService.STOP,
                CallbackService.CALLBACK_PLAN + plan.getId() + CallbackService.STOP_ERROR), plan.getId());
    }

    @Override
    public boolean deployPlan(int planId)
    {
        log.debug("[DeploymentManagerClient] -> [deployPlan]: calling Deployment Manager Service to deployPlan {} ", planId);
        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();
        this.restHandlerDeploymentmanagerapi.deployPlan(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void deployPlan()
            {
                log.debug("[DeploymentManagerClient] -> [deployPlan]: Launched deploying plan: [{}]", planId);
                response.set(true);
            }

            @Override
            public void deployPlanErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [deployPlan]: Error deploying plan [{}], error message: [{}]", planId, outcome.getFirstErrorMessage());
                response.set(false);
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_PLAN + planId + CallbackService.DEPLOY, CallbackService.CALLBACK_PLAN + planId + CallbackService.DEPLOY_ERROR), planId);

        if (Boolean.FALSE.equals(response.get()))
        {
            log.debug("[DeploymentManagerClient] -> [deployPlan]: deploying plan: [{}] was failed. Client response: [{}]", planId, response.get());
            throw new NovaException(DeploymentError.getDeployFromDeploymentManagerError(planId));
        }
        return response.get();
    }

    @Override
    public void promotePlan(int planId, int newPlanId)
    {
        log.debug("[DeploymentManagerClient] -> [promotePlan]: calling Deployment Manager Service to promoting plan {}", planId);

        this.restHandlerDeploymentmanagerapi.promotePlan(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void promotePlan()
            {
                log.debug("[DeploymentManagerClient] -> [promotePlan]: Launched promoting plan {}", planId);
            }

            @Override
            public void promotePlanErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [promotePlan]: Error promoting plan {} errorMessage: [{}]", planId, outcome.getFirstErrorMessage());
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_PLAN + planId + CallbackService.PROMOTE,
                CallbackService.CALLBACK_PLAN + newPlanId + CallbackService.PROMOTE_ERROR), planId, newPlanId);
    }

    @Override
    public boolean replacePlan(int planId, int newPlanId, List<DeploymentService> servicesToCreate, List<DeploymentService> serviceToRemove, List<DeploymentService> servicesToRestart)
    {
        log.debug("[DeploymentManagerClient] -> [replacePlan]: calling Deployment Manager Service to replace plan {} with {} ", planId, newPlanId);
        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        ReplacePlanInfoDTO replacePlanInfoDTO = new ReplacePlanInfoDTO();
        replacePlanInfoDTO.setOldPlanId(planId);
        replacePlanInfoDTO.setNewPlanId(newPlanId);
        replacePlanInfoDTO.setServicesToCreate(servicesToCreate.stream().mapToInt(AbstractEntity::getId).toArray());
        List<Integer> servicesToRemoveList = serviceToRemove.stream().map(AbstractEntity::getId).collect(Collectors.toList());
        replacePlanInfoDTO.setServicesToRemove(servicesToRemoveList.stream().mapToInt(Integer::intValue).toArray());
        replacePlanInfoDTO.setServicesToRestart(servicesToRestart.stream().mapToInt(AbstractEntity::getId).
                filter(x -> !servicesToRemoveList.contains(x)).toArray());
        replacePlanInfoDTO.setCallbackInfo(this.callbackService.buildCallback(CallbackService.CALLBACK_PLAN + planId + CallbackService.REPLACE + newPlanId,
                CallbackService.CALLBACK_PLAN + planId + CallbackService.REPLACE +
                        newPlanId + CallbackService.REPLACE_ERROR));

        log.debug("[DeploymentManagerClient] -> [replacePlan]: replace plan DTO builded to replace plan {} with {}. calling... ", planId, newPlanId);

        // Ask the container cluster for the number
        // of running instances of the service
        this.restHandlerDeploymentmanagerapi.replacePlan(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void replacePlan()
            {
                log.debug("[DeploymentManagerClient] -> [replacePlan]: Launched replacing plan {} with {}", planId, newPlanId);
                response.set(true);
            }

            @Override
            public void replacePlanErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [replacePlan]: Error replacing plan {}, error: {}", planId, outcome.getBodyExceptionMessage());
                response.set(false);
            }
        }, replacePlanInfoDTO);

        log.debug("[DeploymentManagerClient] -> [replacePlan]: received response of replace plan {} with {}. calling... ", planId, newPlanId);

        return response.get();
    }

    @Override
    public void restartService(final DeploymentService service)
    {

        log.debug("[DeploymentManagerClient] -> [restartService]: Calling Deployment Manager to restart service {}", service.getId());
        this.restHandlerDeploymentmanagerapi.restartService(new IRestListenerDeploymentmanagerapi()
        {

            @Override
            public void restartService()
            {

                log.debug("[DeploymentManagerClient] -> [restartService]: Launched restarting service {}", service.getId());
            }

            @Override
            public void restartServiceErrors(Errors outcome)
            {

                log.error("[DeploymentManagerClient] -> [restartService]: Error restarting service: {} errorMessage: [{}]", service.getId(), outcome.getFirstErrorMessage());
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_SERVICE + service.getId() + CallbackService.RESTART,
                CallbackService.CALLBACK_SERVICE + service.getId() + CallbackService.RESTART_ERROR), service.getId());
    }

    @Override
    public void startService(final DeploymentService service)
    {

        log.debug("[DeploymentManagerClient] -> [startService]: Calling Deployment Manager to start service {}", service.getId());
        // Ask the container cluster to Start a service
        this.restHandlerDeploymentmanagerapi.startService(new IRestListenerDeploymentmanagerapi()
        {

            @Override
            public void startService()
            {

                log.debug("[DeploymentManagerClient] -> [startService]: Launched starting service {}", service.getId());
            }

            @Override
            public void startServiceErrors(Errors outcome)
            {

                log.error("[DeploymentManagerClient] -> [startService]: Error starting service: {} errorMessage: [{}]", service.getId(), outcome.getFirstErrorMessage());
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_SERVICE + service.getId() + CallbackService.START,
                CallbackService.CALLBACK_SERVICE + service.getId() + CallbackService.START_ERROR), service.getId());
    }

    @Override
    public void stopService(final DeploymentService service)
    {

        log.debug("[DeploymentManagerClient] -> [stopService]:Calling Deployment Manager to stop service {}", service.getId());
        // Ask the container cluster to Start a service
        this.restHandlerDeploymentmanagerapi.stopService(new IRestListenerDeploymentmanagerapi()
        {

            @Override
            public void stopService()
            {

                log.debug("[DeploymentManagerClient] -> [stopService]: Launched stopping service {}", service.getId());
            }

            @Override
            public void stopServiceErrors(Errors outcome)
            {

                log.error("[DeploymentManagerClient] -> [stopService]: Error stopping service: {} errorMessage: [{}]", service.getId(), outcome.getFirstErrorMessage());
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_SERVICE + service.getId() + CallbackService.STOP,
                CallbackService.CALLBACK_SERVICE + service.getId() + CallbackService.STOP_ERROR), service.getId());
    }

    @Override
    public ServiceStatusDTO getContainersOfService(Integer serviceId)
    {
        // Init result to 0 by default.
        final SingleApiClientResponseWrapper<DeployServiceStatusDTO> response = new SingleApiClientResponseWrapper<>();
        // Ask the container cluster for the number
        // of running instances of the service
        this.restHandlerDeploymentmanagerapi.getServiceStatus(new IRestListenerDeploymentmanagerapi()
        {

            @Override
            public void getServiceStatus(DeployServiceStatusDTO outcome)
            {

                log.trace("[DeploymentManagerClient] -> [getContainersOfService]: Response from deployment Manager API: [{}]", outcome);
                response.set(outcome);
            }

            @Override
            public void getServiceStatusErrors(Errors outcome)
            {

                log.error("[DeploymentManagerClient] -> [getContainersOfService]: Error getting running containers for service: {}", serviceId);
                DeployServiceStatusDTO result = createEmptyDeployServiceStatusDTO();
                response.set(result);
            }
        }, serviceId);
        // Return the number.
        return this.buildServiceStatusDTO(response.get());
    }

    @Override
    public StatusDTO getDeploymentPlanServicesStatus(Integer planId)
    {

        final SingleApiClientResponseWrapper<DeployStatusDTO> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerDeploymentmanagerapi.getDeploymentPlanServicesStatus(new IRestListenerDeploymentmanagerapi()
        {

            @Override
            public void getDeploymentPlanServicesStatus(DeployStatusDTO outcome)
            {

                log.debug("[DeploymentManagerClient] -> [getDeploymentPlanServicesStatus]: Launched to get services of plan [{}]", planId);
                response.set(outcome);
            }

            @Override
            public void getDeploymentPlanServicesStatusErrors(Errors outcome)
            {

                log.error("[DeploymentManagerClient] -> [getDeploymentPlanServicesStatus]: Error getting running instances for services of plan: [{}]. Error message: [{}]", planId, outcome.getFirstErrorMessage());
                DeployStatusDTO result = createEmptyDeployStatusDTO();
                response.set(result);
            }
        }, planId);
        // Return the number.
        return this.buildStatusDTO(response.get());
    }

    @Override
    public DeploySubsystemStatusDTO[] getDeploymentSubsystemServicesStatus(int[] subsystemIds)
    {
        final SingleApiClientResponseWrapper<DeploySubsystemStatusDTO[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerDeploymentmanagerapi.getSubsystemStatus(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void getSubsystemStatus(DeploySubsystemStatusDTO[] outcome)
            {
                log.debug("[DeploymentManagerClient] -> [getDeploymentSubsystemServicesStatus]: response of subsystem id array: [{}] from deployment Manager API: [{}]", Arrays.toString(subsystemIds), Arrays.toString(outcome));
                response.set(outcome);
            }

            @Override
            public void getSubsystemStatusErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [getDeploymentSubsystemServicesStatus]: error getting subsystem services status of subsystem id arrays [{}]. Error message: [{}]", Arrays.toString(subsystemIds), outcome.getBodyExceptionMessage());
                response.set(new DeploySubsystemStatusDTO[0]);
            }
        }, subsystemIds);

        return response.get();
    }

    @Override
    public DeployInstanceStatusDTO[] getAllDeploymentInstanceStatus(final Integer deploymentServiceId)
    {
        final SingleApiClientResponseWrapper<DeployInstanceStatusDTO[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerDeploymentmanagerapi.getAllDeploymentInstanceStatus(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void getAllDeploymentInstanceStatus(DeployInstanceStatusDTO[] outcome)
            {
                log.debug("[DeploymentManagerClient] -> [getAllDeploymentInstanceStatus]: response status of deployment service id: [{}] of deployment instance status from deployment Manager API: [{}]", deploymentServiceId, Arrays.toString(outcome));
                response.set(outcome);
            }

            @Override
            public void getAllDeploymentInstanceStatusErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [getAllDeploymentInstanceStatus]: error getting deployment instance status for deployment service id: [{}]. Error message: [{}]", deploymentServiceId, outcome.getBodyExceptionMessage());
                throw new NovaException(DeploymentError.getDeploymentInstanceStatusError());
            }
        }, deploymentServiceId);

        return response.get();
    }

    @Override
    public DeployInstanceStatusDTO getDeploymentInstanceStatusById(final Integer deploymentInstanceId)
    {
        final SingleApiClientResponseWrapper<DeployInstanceStatusDTO> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerDeploymentmanagerapi.getDeploymentInstanceStatusById(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void getDeploymentInstanceStatusById(DeployInstanceStatusDTO outcome)
            {
                log.debug("[DeploymentManagerClient] -> [getDeploymentInstanceStatusById]: response status of deployment instance by deployment instance id: [{}] from deployment Manager API: [{}]", deploymentInstanceId, outcome);
                response.set(outcome);
            }

            @Override
            public void getDeploymentInstanceStatusByIdErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [getDeploymentInstanceStatusById]: error getting deployment instance status by deployment instance id: [{}]. Error message: [{}]", deploymentInstanceId, outcome.getBodyExceptionMessage());
                response.set(new DeployInstanceStatusDTO());
            }
        }, deploymentInstanceId);

        return response.get();
    }

    @Override
    public DeployServiceStatusDTO getServiceStatus(Integer deploymentServiceId)
    {
        final SingleApiClientResponseWrapper<DeployServiceStatusDTO> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerDeploymentmanagerapi.getServiceStatus(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void getServiceStatus(DeployServiceStatusDTO outcome)
            {
                log.debug("[DeploymentManagerClient] -> [getServiceStatus]: response status of deployment service id: [{}] of deployment get service status from deployment Manager API: [{}]", deploymentServiceId, outcome);
                response.set(outcome);
            }

            @Override
            public void getServiceStatusErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [getServiceStatus]: error getting of deployment get service status from deployment Manager API for deployment service id: [{}]. Error message: [{}]", deploymentServiceId, outcome.getBodyExceptionMessage());
                response.set(new DeployServiceStatusDTO());
            }
        }, deploymentServiceId);

        return response.get();
    }

    @Override
    public boolean deployService(final Integer serviceId)
    {
        log.debug("[DeploymentManagerClient] -> [deployService]:Calling Deployment Manager Service to deployService({}) ", serviceId);
        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        // Call service
        this.restHandlerDeploymentmanagerapi.deployService(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void deployService()
            {
                log.debug("[DeploymentManagerClient] -> [deployService]: Launched deploying service {}", serviceId);
                response.set(true);
            }

            @Override
            public void deployServiceErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [deployService]: Error deploying service [{}] - errorMessage: [{}]", serviceId, outcome.getFirstErrorMessage());
                response.set(false);
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_SERVICE + serviceId + CallbackService.DEPLOY,
                CallbackService.CALLBACK_SERVICE + serviceId + CallbackService.DEPLOY_ERROR), serviceId);
        return response.get();
    }

    @Override
    public boolean deploySubsystem(final Integer subsystemId)
    {
        log.debug("[DeploymentManagerClient] -> [deploySubsystem]:Calling Deployment Manager Service to deploySubsystem {} ", subsystemId);
        SingleApiClientResponseWrapper<Boolean> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerDeploymentmanagerapi.deploySubsystem(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void deploySubsystem()
            {
                log.debug("[DeploymentManagerClient] -> [deploySubsystem]: Call to deployment manager done.");
                response.set(true);
            }

            @Override
            public void deploySubsystemErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [deploySubsystem]: Error deploying subsystem {} errorMessage: [{}]", subsystemId, outcome.getFirstErrorMessage());
                response.set(false);
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_SUBSYSTEM + subsystemId + CallbackService.DEPLOY,
                CallbackService.CALLBACK_SUBSYSTEM + subsystemId + CallbackService.DEPLOY_ERROR), subsystemId);
        return response.get();
    }

    @Override
    public void restartSubsystem(final DeploymentSubsystem subsystem)
    {
        log.debug("[DeploymentManagerClient] -> [restartSubsystem]: Calling Deployment Manager to restart subsystem {}", subsystem.getId());
        this.restHandlerDeploymentmanagerapi.restartSubsystem(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void restartSubsystem()
            {
                log.debug("[DeploymentManagerClient] -> [restartSubsystem]: Launched restarting subsystem {}", subsystem.getId());
            }

            @Override
            public void restartSubsystemErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [restartSubsystem]: Error restarting subsystem: {} errorMessage: [{}]", subsystem.getId(), outcome.getFirstErrorMessage());
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_SUBSYSTEM + subsystem.getId() + CallbackService.RESTART,
                CallbackService.CALLBACK_SUBSYSTEM + subsystem.getId() + CallbackService.RESTART_ERROR), subsystem.getId());
    }

    @Override
    public void startSubsystem(final DeploymentSubsystem subsystem)
    {
        log.debug("[DeploymentManagerClient] -> [startSubsystem]: Calling Deployment Manager to start subsystem {}", subsystem.getId());
        // Ask the container cluster to Start a service
        this.restHandlerDeploymentmanagerapi.startSubsystem(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void startSubsystem()
            {
                log.debug("[DeploymentManagerClient] -> [startSubsystem]: Launched starting subsystem {}", subsystem.getId());
            }

            @Override
            public void startSubsystemErrors(Errors outcome)
            {
                log.error("[DeploymentManagerClient] -> [startSubsystem]: Error starting subsystem: {} errorMessage: [{}]", subsystem.getId(), outcome.getFirstErrorMessage());
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_SUBSYSTEM + subsystem.getId() + CallbackService.START,
                CallbackService.CALLBACK_SUBSYSTEM + subsystem.getId() + CallbackService.START_ERROR), subsystem.getId());
    }

    @Override
    public void stopSubsystem(final DeploymentSubsystem subsystem)
    {
        log.debug("[DeploymentManagerClient] -> [stopSubsystem]:Calling Deployment Manager to stop subsystem {}", subsystem.getId());
        // Ask the container cluster to Stop a subsystem
        this.restHandlerDeploymentmanagerapi.stopSubsystem(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void stopSubsystem()
            {
                log.debug("[[DeploymentManagerClient] -> [stopSubsystem]: Launched stopping subsystem {}", subsystem.getId());
            }

            @Override
            public void stopSubsystemErrors(Errors outcome)
            {
                log.error("[[DeploymentManagerClient] -> [stopSubsystem]: Error stopping subsystem: {} errorMessage: [{}]", subsystem.getId(), outcome.getFirstErrorMessage());
            }
        }, this.callbackService.buildCallback(CallbackService.CALLBACK_SUBSYSTEM + subsystem.getId() + CallbackService.STOP,
                CallbackService.CALLBACK_SUBSYSTEM + subsystem.getId() + CallbackService.STOP_ERROR), subsystem.getId());
    }

    @Override
    public HostMemoryHistoryItemDTO[] getHostsMemoryHistorySnapshot()
    {
        log.debug("[DeploymentManagerClient] -> [getHostsMemoryHistorySnapshots]:Calling memory snapshot for statistic history loading.");
        SingleApiClientResponseWrapper<HostMemoryHistoryItemDTO[]> response = new SingleApiClientResponseWrapper<>();
        this.restHandlerDeploymentmanagerapi.getHostsMemoryHistorySnapshot(new IRestListenerDeploymentmanagerapi()
        {
            @Override
            public void getHostsMemoryHistorySnapshot(HostMemoryHistoryItemDTO[] outcome)
            {
                log.debug("[[DeploymentManagerClient] -> [getHostsMemoryHistorySnapshot]: getting memory snapshot for statistic history loading.");
                response.set(outcome);
            }

            @Override
            public void getHostsMemoryHistorySnapshotErrors(Errors outcome)
            {
                log.error("[[DeploymentManagerClient] -> [getHostsMemoryHistorySnapshot]: Error stopping subsystem: {}", outcome.getBodyExceptionMessage());
                throw new NovaException(StatisticsError.getUnexpectedError(), outcome);
            }
        });
        return response.get();
    }

    //////////////////////////////////// PRIVATE METHODS //////////////////////////////////////////////////////////

    private DeployServiceStatusDTO createEmptyDeployServiceStatusDTO()
    {
        DeployServiceStatusDTO deployServiceStatusDTO = new DeployServiceStatusDTO();

        deployServiceStatusDTO.setInstances(new DeployInstanceStatusDTO[0]);
        deployServiceStatusDTO.setStatus(this.createEmptyDeployStatusDTO());
        deployServiceStatusDTO.setServiceId(0);

        return deployServiceStatusDTO;
    }

    private DeployStatusDTO createEmptyDeployStatusDTO()
    {

        log.debug("[DeploymentManagerClient] -> [createEmptyDeployServiceStatusDTO]: Creating an empty DeployStatusDto object.");

        DeployStatusDTO deployStatusDTO = new DeployStatusDTO();

        deployStatusDTO.setRunning(0);
        deployStatusDTO.setTotal(0);

        return deployStatusDTO;
    }

    private ServiceStatusDTO buildServiceStatusDTO(DeployServiceStatusDTO deployServiceStatusDTO)
    {

        log.debug("[DeploymentManagerClient] -> [buildServiceStatusDTO]: Building a ServiceStatusDto object from DeployServiceStatusDto: [{}].", deployServiceStatusDTO);

        ServiceStatusDTO serviceStatusDTO = new ServiceStatusDTO();

        serviceStatusDTO.setServiceId(deployServiceStatusDTO.getServiceId());
        serviceStatusDTO.setStatus(this.buildStatusDTO(deployServiceStatusDTO.getStatus()));
        serviceStatusDTO.setInstances(this.buildInstancesStatusDTO(deployServiceStatusDTO.getInstances()));

        log.debug("[DeploymentManagerClient] -> [buildServiceStatusDTO]: built a ServiceStatusDto [{}] from DeployServiceStatusDto.", serviceStatusDTO);

        return serviceStatusDTO;
    }

    private StatusDTO buildStatusDTO(DeployStatusDTO deployStatusDTO)
    {

        StatusDTO statusDTO = new StatusDTO();

        BeanUtils.copyProperties(deployStatusDTO, statusDTO);

        return statusDTO;
    }

    private InstanceStatusDTO[] buildInstancesStatusDTO(DeployInstanceStatusDTO[] instances)
    {

        List<InstanceStatusDTO> instanceStatusDTOList = new ArrayList<>();

        for (DeployInstanceStatusDTO instance : instances)
        {
            InstanceStatusDTO instanceStatusDTO = new InstanceStatusDTO();

            instanceStatusDTO.setInstanceId(instance.getInstanceId());
            instanceStatusDTO.setStatus(this.buildStatusDTO(instance.getStatus()));

            instanceStatusDTOList.add(instanceStatusDTO);
        }
        return instanceStatusDTOList.toArray(new InstanceStatusDTO[0]);
    }
}
