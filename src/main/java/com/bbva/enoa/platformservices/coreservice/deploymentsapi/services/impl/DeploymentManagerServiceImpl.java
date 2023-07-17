package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployInstanceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeploySubsystemStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ServiceStatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherDeploymentDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.common.util.CallbackService;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.common.util.ProfilingUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDeploymentManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IEtherManagerClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentManagerService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentReplacePlanServiceImpl;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IRepositoryManagerService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.PlanProfilingUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Deployment manager client
 */
@Slf4j
@Service
public class DeploymentManagerServiceImpl implements IDeploymentManagerService
{
    /**
     * Deployment
     */
    private final IDeploymentManagerClient deploymentManagerClient;
    /**
     * Ether manager API client
     */
    private final IEtherManagerClient etherManagerClient;

    /**
     * Deployment utils service
     */
    private final DeploymentUtils deploymentUtils;

    /**
     * Deployment replace plan service
     */
    private final IDeploymentReplacePlanServiceImpl deploymentReplacePlanService;

    /**
     * api gateway service
     */
    private final IApiGatewayService apiGatewayService;

    /**
     * plan profiling utils
     */
    private final PlanProfilingUtils planProfilingUtils;

    /**
     * profiling utils
     */
    private final ProfilingUtils profilingUtils;


    /**
     * repository manager service
     */
    private final IRepositoryManagerService repositoryManagerService;

    @Value("${nova.gatewayServices.cesProfilingEnabled:true}")
    private Boolean cesEnabled;

    /*
     * KKPF is NOVA product for CES. We cannot publish its own profiling because profiling publication is done in
     * the same service that is being deployed. It would always fail.
     */
    @Value("${nova.gatewayServices.cesUuaa:KKPF}")
    private String cesUuaa;

    /**
     * Dependency injection constructor
     *
     * @param deploymentManagerClient      deployment manager client
     * @param etherManagerClient           ether manager api client
     * @param deploymentUtils              deployment utils
     * @param repositoryManagerService     repositoryManagerService
     * @param apiGatewayService            apigatewayapi
     * @param deploymentReplacePlanService deploymentReplacePlanService
     * @param planProfilingUtils           planProfilingUtils
     * @param profilingUtils               profilingUtils
     */
    @Autowired
    public DeploymentManagerServiceImpl(final IDeploymentManagerClient deploymentManagerClient, final IEtherManagerClient etherManagerClient, final DeploymentUtils deploymentUtils, final IRepositoryManagerService repositoryManagerService,
                                        final IApiGatewayService apiGatewayService, final IDeploymentReplacePlanServiceImpl deploymentReplacePlanService, final PlanProfilingUtils planProfilingUtils,
                                        final ProfilingUtils profilingUtils)
    {
        this.etherManagerClient = etherManagerClient;
        this.deploymentUtils = deploymentUtils;
        this.deploymentManagerClient = deploymentManagerClient;
        this.repositoryManagerService = repositoryManagerService;
        this.apiGatewayService = apiGatewayService;
        this.deploymentReplacePlanService = deploymentReplacePlanService;
        this.planProfilingUtils = planProfilingUtils;
        this.profilingUtils = profilingUtils;
    }

    //////////////////////////////////////////////////////// IMPLEMENTATIONS /////////////////////////////////////////

    @Override
    public boolean deployPlan(final DeploymentPlan plan)
    {
        try
        {
            if (PlatformUtils.isPlanDeployedInEther(plan))
            {
                boolean etherDeployResult = this.etherManagerClient.deployEtherPlan(this.deploymentUtils.buildEtherDeploymentDTO(plan, CallbackService.DEPLOY, CallbackService.DEPLOY_ERROR));

                // If the deployment is success, save status and action
                if (etherDeployResult)
                {
                    //Update deployment plan status
                    plan.setStatus(DeploymentStatus.DEPLOYED);
                    plan.setAction(DeploymentAction.DEPLOYING);

                    this.repositoryManagerService.savePlan(plan);
                }

                return etherDeployResult;
            }
            else
            {
                configureServicesForLoggingInEther(plan);
                return this.deploymentManagerClient.deployPlan(plan.getId());
            }
        }
        catch (NovaException e)
        {
            //Update deployment plan status
            plan.setStatus(DeploymentStatus.DEFINITION);
            plan.setAction(DeploymentAction.READY);
            this.repositoryManagerService.savePlan(plan);
            throw e;
        }
    }

    @Override
    public boolean replacePlan(DeploymentPlan plan, DeploymentPlan newPlan) throws NovaException
    {
        if (plan.getSelectedDeploy().equals(newPlan.getSelectedDeploy()))
        {
            Map<String, List<DeploymentService>> updateServiceMap =
                    this.deploymentReplacePlanService.getUncommonReplacePlan(plan.getId(), newPlan.getId());

            log.debug("Creating update publication for new Plan: [{}]", newPlan.getId());

            this.apiGatewayService.updatePublication(updateServiceMap.get(DeploymentConstants.CREATE_SERVICE),
                    updateServiceMap.get(DeploymentConstants.REMOVE_SERVICE),
                    newPlan, plan);

            log.debug("Created update publication for new Plan: [{}]", newPlan.getId());

            log.debug("Generating dockerKeys for new Plan: [{}]", newPlan.getId());

            if(log.isDebugEnabled())
            {

                log.debug("generateDockerKey for CREATE_SERVICE: {} ", updateServiceMap.getOrDefault(DeploymentConstants.CREATE_SERVICE, new ArrayList<>()).stream()
                        .map(d -> d.getService().toString() + ";" + d.toString())
                        .collect(Collectors.joining("|")));
                log.debug("generateDockerKey for UPDATE_INSTANCE: {} ", updateServiceMap.getOrDefault(DeploymentConstants.UPDATE_INSTANCE, new ArrayList<>()).stream()
                        .map(d -> d.getService().toString() + ";" + d.toString())
                        .collect(Collectors.joining("|")));
            }
            this.apiGatewayService.generateDockerKey(updateServiceMap.get(DeploymentConstants.CREATE_SERVICE), newPlan.getEnvironment());

            this.apiGatewayService.generateDockerKey(updateServiceMap.get(DeploymentConstants.UPDATE_INSTANCE), newPlan.getEnvironment());

            log.debug("Generated dockerKeys for new Plan: [{}]", newPlan.getId());

            log.debug("Checking plan profile Plan: [{}]", newPlan.getId());

            if (this.cesEnabled
                    && !plan.getReleaseVersion().getRelease().getProduct().getUuaa().equalsIgnoreCase(this.cesUuaa)
                    && this.profilingUtils.isPlanExposingApis(newPlan))
            {
                log.debug("Calling plan profiling utils to check plan profile: [{}]", newPlan.getId());
                this.planProfilingUtils.checkPlanProfileChange(newPlan);
            }

            log.debug("Checked plan profile Plan: [{}]", newPlan.getId());

            log.debug("Saving Plan: [{}]", newPlan.getId());

            this.repositoryManagerService.savePlan(newPlan);

            log.debug("Saved Plan: [{}]", newPlan.getId());

            if (PlatformUtils.isPlanDeployedInEther(plan))
            {
                return this.etherManagerClient.replaceEtherPlan(
                        this.deploymentUtils.buildEtherDeploymentDTO(plan, CallbackService.REPLACE, CallbackService.REPLACE_ERROR),
                        this.deploymentUtils.buildEtherDeploymentDTO(newPlan, CallbackService.REPLACE, CallbackService.REPLACE_ERROR));
            }
            else
            {
                configureServicesForLoggingInEther(newPlan);

                log.debug("Calling deploymentManager to replacePlan: [{}]", newPlan.getId());
                return this.deploymentManagerClient.replacePlan(plan.getId(), newPlan.getId(), updateServiceMap.get(DeploymentConstants.CREATE_SERVICE),
                        updateServiceMap.get(DeploymentConstants.REMOVE_SERVICE), updateServiceMap.get(DeploymentConstants.RESTART_SERVICE));
            }
        }
        else
        {
            throw new NovaException(DeploymentError.getReplacePlanChangingDeploymentPlatformError(newPlan.getId(), newPlan.getSelectedDeploy().name(), plan.getId(), plan.getSelectedDeploy().name()));
        }
    }

    @Override
    public void promotePlan(DeploymentPlan plan, DeploymentPlan newPlan)
    {
        if (PlatformUtils.isPlanDeployedInEther(plan) || newPlan.getEnvironment().equals(Environment.PRE))
        {
            log.debug("[DeploymentManagerServiceImpl]-> [promotePlan]: No action required to promote plan");
        }
        else
        {
            this.deploymentManagerClient.promotePlan(plan.getId(), newPlan.getId());
        }
    }

    @Override
    public void startInstance(final DeploymentInstance instance)
    {
        if (PlatformUtils.isInstanceDeployedInNova(instance))
        {
            this.deploymentManagerClient.startInstance(instance);
        }
    }

    @Override
    public void stopInstance(final DeploymentInstance instance)
    {
        if (PlatformUtils.isInstanceDeployedInNova(instance))
        {
            this.deploymentManagerClient.stopInstance(instance);
        }

    }

    @Override
    public void restartInstance(final DeploymentInstance instance)
    {
        if (PlatformUtils.isInstanceDeployedInNova(instance))
        {
            this.deploymentManagerClient.restartInstance(instance);
        }

    }

    @Override
    public void startService(final DeploymentService service)
    {
        if (PlatformUtils.isServiceDeployedInEther(service))
        {
            this.etherManagerClient.startEtherService(this.deploymentUtils.buildEtherDeploymentDTO(service, CallbackService.START, CallbackService.START_ERROR));
        }
        else
        {
            this.deploymentManagerClient.startService(service);
        }
    }

    @Override
    public void stopService(final DeploymentService service)
    {
        if (PlatformUtils.isServiceDeployedInEther(service))
        {
            this.etherManagerClient.stopEtherService(this.deploymentUtils.buildEtherDeploymentDTO(service, CallbackService.STOP, CallbackService.STOP_ERROR));
        }
        else
        {
            this.deploymentManagerClient.stopService(service);
        }
    }

    @Override
    public void restartService(final DeploymentService service)
    {
        if (PlatformUtils.isServiceDeployedInEther(service))
        {
            this.etherManagerClient.restartEtherService(this.deploymentUtils.buildEtherDeploymentDTO(service, CallbackService.RESTART, CallbackService.RESTART_ERROR));
        }
        else
        {
            this.deploymentManagerClient.restartService(service);
        }
    }

    @Override
    public void startSubsystem(final DeploymentSubsystem subsystem)
    {
        if (PlatformUtils.isSubsystemDeployedInEther(subsystem))
        {
            this.etherManagerClient.startEtherService(this.deploymentUtils.buildEtherDeploymentDTO(subsystem, CallbackService.START, CallbackService.START_ERROR));
        }
        else
        {
            this.deploymentManagerClient.startSubsystem(subsystem);
        }
    }

    @Override
    public void stopSubsystem(final DeploymentSubsystem subsystem)
    {
        if (PlatformUtils.isSubsystemDeployedInEther(subsystem))
        {
            this.etherManagerClient.stopEtherService(this.deploymentUtils.buildEtherDeploymentDTO(subsystem, CallbackService.STOP, CallbackService.STOP_ERROR));
        }
        else
        {
            this.deploymentManagerClient.stopSubsystem(subsystem);
        }
    }

    @Override
    public void restartSubsystem(final DeploymentSubsystem subsystem)
    {
        if (PlatformUtils.isSubsystemDeployedInEther(subsystem))
        {
            this.etherManagerClient.restartEtherService(this.deploymentUtils.buildEtherDeploymentDTO(subsystem, CallbackService.RESTART, CallbackService.RESTART_ERROR));
        }
        else
        {
            this.deploymentManagerClient.restartSubsystem(subsystem);
        }
    }

    @Override
    public void startPlan(final DeploymentPlan plan)
    {
        if (PlatformUtils.isPlanDeployedInEther(plan))
        {
            this.etherManagerClient.startEtherService(this.deploymentUtils.buildEtherDeploymentDTO(plan, CallbackService.START, CallbackService.START_ERROR));
        }
        else
        {
            this.deploymentManagerClient.startPlan(plan);
        }
    }

    @Override
    public void stopPlan(final DeploymentPlan plan)
    {
        if (PlatformUtils.isPlanDeployedInEther(plan))
        {
            this.etherManagerClient.stopEtherService(this.deploymentUtils.buildEtherDeploymentDTO(plan, CallbackService.STOP, CallbackService.STOP_ERROR));
        }
        else
        {
            this.deploymentManagerClient.stopPlan(plan);
        }
    }

    @Override
    public void restartPlan(final DeploymentPlan plan)
    {
        if (PlatformUtils.isPlanDeployedInEther(plan))
        {
            this.etherManagerClient.restartEtherService(this.deploymentUtils.buildEtherDeploymentDTO(plan, CallbackService.RESTART, CallbackService.RESTART_ERROR));
        }
        else
        {
            this.deploymentManagerClient.restartPlan(plan);
        }
    }

    @Override
    public DeploySubsystemStatusDTO[] getDeploymentSubsystemServicesStatus(int[] subsystemIdList, final boolean isOrchestrationHealthy) throws NovaException
    {
        // Call deployment manager client to get all subsystem statuses from deployment subsystem id list depending on if cluster orchestration is healthy
        DeploySubsystemStatusDTO[] deploySubsystemStatusDTOArray;
        if (isOrchestrationHealthy)
        {
            deploySubsystemStatusDTOArray = this.deploymentManagerClient.getDeploymentSubsystemServicesStatus(subsystemIdList);
        }
        else
        {
            log.error("[DeploymentManagerServiceImpl] -> [getDeploymentSubsystemServicesStatus]: the cluster Orchestration is unhealthy for checking status of the subsystem ids: [{}]. Can not get deployment subsystem service status", subsystemIdList);
            throw new NovaException(DeploymentError.getDeploymentSubsystemStatusError());
        }

        if (deploySubsystemStatusDTOArray.length == 0)
        {
            throw new NovaException(DeploymentError.getDeploymentSubsystemStatusError());
        }

        return deploySubsystemStatusDTOArray;
    }

    @Override
    public CompletableFuture<DeploySubsystemStatusDTO> getAsyncDeploymentSubsystemServicesStatus(int deploymentSubsystemId, final boolean isOrchestrationHealthy) throws NovaException
    {
        return CompletableFuture.supplyAsync(() ->
        {
            // Call deployment manager client to get all subsystem statuses from deployment subsystem id list
            DeploySubsystemStatusDTO[] deploySubsystemStatusArray;
            if (isOrchestrationHealthy)
            {
                deploySubsystemStatusArray = this.deploymentManagerClient.getDeploymentSubsystemServicesStatus(new int[]{deploymentSubsystemId});
            }
            else
            {
                log.error("[DeploymentManagerServiceImpl] -> [getAsyncDeploymentSubsystemServicesStatus]: the cluster Orchestration is unhealthy for checking status of the deployment Subsystem Id: [{}]. Can not get deployment subsystem service status", deploymentSubsystemId);
                throw new NovaException(DeploymentError.getDeploymentServiceStatusError());
            }

            if (deploySubsystemStatusArray.length == 0)
            {
                throw new NovaException(DeploymentError.getDeploymentServiceStatusError());
            }

            return deploySubsystemStatusArray[0];
        });
    }

    @Override
    public DeployInstanceStatusDTO[] getAllDeploymentInstanceStatus(final Integer deploymentServiceId, final boolean isOrchestrationHealthy) throws NovaException
    {
        DeployInstanceStatusDTO[] deployInstanceStatusArray;

        if (isOrchestrationHealthy)
        {
            deployInstanceStatusArray = this.deploymentManagerClient.getAllDeploymentInstanceStatus(deploymentServiceId);
        }
        else
        {
            log.error("[DeploymentManagerClient] -> [getAllDeploymentInstanceStatus]: the cluster Orchestration is unhealthy for deployment service id: [{}]. Can not get all deployment instances status ", deploymentServiceId);
            throw new NovaException(DeploymentError.getDeploymentInstanceStatusError());
        }

        if (deployInstanceStatusArray.length == 0)
        {
            log.warn("[DeploymentManagerServiceImpl] -> [getAllDeploymentInstanceStatus]: there is any deployment instances (0) ready for getting the status for deployment service id: [{}].", deploymentServiceId);
        }

        return deployInstanceStatusArray;
    }

    @Override
    public DeployInstanceStatusDTO[] getEtherServiceDeploymentInstancesStatus(final DeploymentInstance deploymentInstance) throws NovaException
    {

        // Getting deployment service from instance received
        DeploymentService deploymentService = deploymentInstance.getService();

        return getEtherServiceDeploymentInstancesStatus(deploymentService);
    }

    @Override
    public DeployInstanceStatusDTO[] getEtherServiceDeploymentInstancesStatus(DeploymentService deploymentService) throws NovaException
    {

        // 1. Generating the Ether DTO with the deployment plan received
        EtherDeploymentDTO etherDeploymentDTO = deploymentUtils.buildEtherDeploymentDTO(deploymentService, "", "");

        // 2. Calling ether manager to obtain the status for the service info with all the instances inside
        ServiceStatusDTO deployEtherServiceStatus = this.etherManagerClient.getServiceStatus(etherDeploymentDTO);

        // 3.Initialize the list to return
        List<DeployInstanceStatusDTO> deployInstanceStatusDTOList = new ArrayList<>();

        log.debug("[DeploymentManagerServiceImpl] -> [getAllEtherDeploymentInstanceStatus]: Generating DTO from ETHER status [{}]", deployEtherServiceStatus);

        if (deployEtherServiceStatus != null && !deploymentService.getDeploymentSubsystem().getDeploymentPlan().getStatus().equals(DeploymentStatus.DEFINITION))
        {
            // 4. Build the DTO element to add it to list
            deployInstanceStatusDTOList.add(this.deploymentUtils.buildDeployInstanceStatusDTO(deployEtherServiceStatus, deploymentService));
        }

        // 5. Convert the complete list into an array to return
        DeployInstanceStatusDTO[] deployInstanceStatusArray = deployInstanceStatusDTOList.toArray(new DeployInstanceStatusDTO[0]);

        if (deployInstanceStatusArray.length == 0)
        {
            log.warn("[DeploymentManagerServiceImpl] -> [getAllEtherDeploymentInstanceStatus]: there is any deployment instances (0) ready for getting the status for deployment service id: [{}].", deploymentService.getId());
        }

        return deployInstanceStatusArray;
    }

    @Override
    public DeployInstanceStatusDTO getDeploymentInstanceStatusById(final Integer deploymentInstanceId, final boolean isOrchestrationHealthy) throws NovaException
    {
        DeployInstanceStatusDTO deployInstanceStatusDTO;
        if (isOrchestrationHealthy)
        {
            deployInstanceStatusDTO = this.deploymentManagerClient.getDeploymentInstanceStatusById(deploymentInstanceId);
        }
        else
        {
            deployInstanceStatusDTO = new DeployInstanceStatusDTO();
            log.warn("[DeploymentManagerServiceImpl] -> [getDeploymentInstanceStatusById]: the cluster Orchestration is unhealthy for checking status of the deployment Instance Id: [{}]. Return empty DeployInstanceStatusDTO", deploymentInstanceId);
        }

        if (deployInstanceStatusDTO.getStatus() == null)
        {
            throw new NovaException(DeploymentError.getDeploymentInstanceStatusError());
        }

        return deployInstanceStatusDTO;
    }


    @Override
    public DeployServiceStatusDTO getServiceStatus(final Integer deploymentServiceId, final boolean isOrchestrationHealthy) throws NovaException
    {
        DeployServiceStatusDTO deployServiceStatusDTO;
        if (isOrchestrationHealthy)
        {
            deployServiceStatusDTO = this.deploymentManagerClient.getServiceStatus(deploymentServiceId);
        }
        else
        {
            log.warn("[DeploymentManagerServiceImpl] -> [getServiceStatus]: the cluster Orchestration is unhealthy for checking status of the deployment Service Id: [{}]. Return empty DeployServiceStatusDTO", deploymentServiceId);
            deployServiceStatusDTO = new DeployServiceStatusDTO();
        }

        if (Strings.isNullOrEmpty(deployServiceStatusDTO.getServiceName()))
        {
            throw new NovaException(DeploymentError.getDeploymentServiceStatusError());
        }

        return deployServiceStatusDTO;
    }

    @Override
    public DeployServiceStatusDTO getEtherServiceStatus(final DeploymentService deploymentService) throws NovaException
    {

        // 1. Generating the Ether DTO with the deployment plan received
        EtherDeploymentDTO etherDeploymentDTO = deploymentUtils.buildEtherDeploymentDTO(deploymentService, "", "");

        // 2. Calling ether manager to obtain the status for the service info with all the instances inside
        ServiceStatusDTO etherServiceStatus = this.etherManagerClient.getServiceStatus(etherDeploymentDTO);

        // 3. Call Ether manager to get the status of the deploy service
        DeployServiceStatusDTO deployServiceStatusDTO = this.deploymentUtils.buildEtherDeployServiceStatusDTO(etherServiceStatus);

        // The ether instances only has serviceId and Status, not instances (Instance is a NOVA concept)
        if (null == deployServiceStatusDTO.getServiceId() || null == deployServiceStatusDTO.getStatus())
        {
            throw new NovaException(DeploymentError.getEtherDeploymentServiceStatusError());
        }

        return deployServiceStatusDTO;
    }

    /**
     * Configure services of deployment play to log/trace in ether if logging in ether has been configured/activated/selected
     * It it is the case, the required resources in Ether will be created
     *
     * @param plan Deployment plan to be deployed
     */
    private void configureServicesForLoggingInEther(final DeploymentPlan plan)
    {
        if (PlatformUtils.isPlanLoggingInEther(plan))
        {
            final boolean servicesConfiguredSuccessfully = this.etherManagerClient.configureServicesForLogging(this.deploymentUtils.buildEtherDeploymentDTO(plan));

            if (!servicesConfiguredSuccessfully)
            {
                throw new NovaException(DeploymentError.getConfigureServicesForLoggingInEtherError(plan.getId()));
            }
        }
    }
}
