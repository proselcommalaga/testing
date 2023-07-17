package com.bbva.enoa.platformservices.coreservice.deploymentsapi.listener;

import com.bbva.enoa.apirestgen.deploymentsapi.model.*;
import com.bbva.enoa.apirestgen.deploymentsapi.server.spring.nova.rest.IRestListenerDeploymentsapi;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsApiService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceDeploymentException;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Listener of the deploymentsapi.
 */
@Log
@Service
public class ListenerDeploymentsApi implements IRestListenerDeploymentsapi
{
    /**
     * DeploymentsApiService
     */
    private final IDeploymentsApiService deploymentsApiService;

    @Autowired
    public ListenerDeploymentsApi(final IDeploymentsApiService deploymentsApiService)
    {
        this.deploymentsApiService = deploymentsApiService;
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public DeploymentDto[] getDeploymentPlansByEnvironment(final NovaMetadata novaMetadata, final Integer productId, final String environment,
                                                           final String status) throws Errors
    {
        try
        {
            return deploymentsApiService.getDeploymentPlansByEnvironment(MetadataUtils.getIvUser(novaMetadata), productId, environment, status);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceException(e, productId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public DeploymentDtoPage getDeploymentPlansByEnvironmentPaginated(NovaMetadata novaMetadata, Integer productId, String statuses, Integer pageSize, Integer pageNumber, String environments)
            throws Errors
    {
        try
        {
            return deploymentsApiService.getDeploymentPlansByEnvironmentsAndStatuses(MetadataUtils.getIvUser(novaMetadata), productId, environments, statuses, pageSize, pageNumber);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceException(e, productId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public DeploymentServiceDto getDeploymentService(final NovaMetadata novaMetadata, final Integer serviceId, final Integer deploymentId) throws Errors
    {
        try
        {
            return this.deploymentsApiService.getDeploymentService(serviceId, deploymentId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public ServiceStatusDTO getServiceStatus(final NovaMetadata novaMetadata, final Integer subsystemId, final Integer serviceId, final Integer deploymentId, final String environment) throws Errors
    {
        try
        {
            return this.deploymentsApiService.getServiceStatus(subsystemId, serviceId, deploymentId, environment);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO changeDeploymentType(final NovaMetadata novaMetadata, final DeploymentTypeChangeDto changeType, final Integer deploymentId) throws Errors
    {
        try
        {
            return this.deploymentsApiService.changeDeploymentType(MetadataUtils.getIvUser(novaMetadata), changeType, deploymentId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public ActionStatus getDeploymentPlanStatus(final NovaMetadata novaMetadata, final Integer deploymentId) throws Errors
    {
        try
        {
            return deploymentsApiService.getDeploymentPlanStatus(deploymentId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public void promotePlanToEnvironment(final NovaMetadata novaMetadata, final Integer deploymentId, final String environment) throws Errors
    {
        try
        {
            this.deploymentsApiService.promotePlanToEnvironment(MetadataUtils.getIvUser(novaMetadata), deploymentId, environment);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public void onTaskReply(final NovaMetadata novaMetadata, final Integer taskid, final Integer deploymentId, final String newStatus) throws Errors
    {
        try
        {
            this.deploymentsApiService.onTaskReply(taskid, deploymentId, newStatus);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public DeploymentServiceDto getDeploymentServiceByName(final NovaMetadata novaMetadata, final String environment, final String serviceName,
                                                           final String subsystemName, final String releaseName, final String productName) throws Errors
    {
        return this.deploymentsApiService.getDeploymentServiceByName(environment, serviceName, subsystemName, releaseName, productName);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public String[] getLanguagesVersions(final NovaMetadata novaMetadata) throws Errors
    {
        return this.deploymentsApiService.getLanguagesVersions();
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public String[] getSubsystemTypes(NovaMetadata novaMetadata) throws Errors
    {
        return this.deploymentsApiService.getSubsystemTypes();
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public SubsystemServicesStatusDTO getDeploymentSubsystemServicesStatus(final NovaMetadata novaMetadata, final Integer subsystemId, final Integer deploymentId, final String environment) throws Errors
    {
        try
        {
            return deploymentsApiService.getDeploymentSubsystemServicesStatus(subsystemId, deploymentId, environment);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public void updateDeploymentService(final NovaMetadata novaMetadata, final DeploymentServiceDto service, final Integer serviceId, final Integer deploymentId) throws Errors
    {
        try
        {
            deploymentsApiService.updateDeploymentService(MetadataUtils.getIvUser(novaMetadata), service, serviceId, deploymentId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public void unschedule(final NovaMetadata novaMetadata, final Integer deploymentId) throws Errors
    {
        try
        {
            deploymentsApiService.unschedule(MetadataUtils.getIvUser(novaMetadata), deploymentId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public void updateDeploymentPlan(final NovaMetadata novaMetadata, final DeploymentDto plan, final Integer deploymentId) throws Errors
    {
        try
        {
            deploymentsApiService.updateDeploymentPlan(MetadataUtils.getIvUser(novaMetadata), plan, deploymentId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public void updateServiceState(final NovaMetadata novaMetadata, final ServiceStateDTO serviceState, final Integer serviceId) throws Errors
    {
        deploymentsApiService.updateServiceState(serviceState, serviceId);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public void updateSubsystemState(final NovaMetadata novaMetadata, final SubsystemStateDTO subsystemState, final Integer subsystemId) throws Errors
    {
        deploymentsApiService.updateSubsystemState(subsystemState, subsystemId);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public String[] getServiceGroupingNames(NovaMetadata novaMetadata, final String filterByDeployed) throws Errors
    {
        return this.deploymentsApiService.getServiceGroupingNames(filterByDeployed);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public void updateDeploymentState(final NovaMetadata novaMetadata, final DeploymentStateDTO deploymentState, final Integer deploymentId) throws Errors
    {
        deploymentsApiService.updateDeploymentPlanState(deploymentState, deploymentId);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public DeploymentMigrationDto migratePlan(final NovaMetadata novaMetadata, final Integer deploymentId, final Integer versionId) throws Errors
    {
        try
        {
            return deploymentsApiService.migratePlan(MetadataUtils.getIvUser(novaMetadata), deploymentId, versionId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public DeploymentChangeDtoPage getHistory(final NovaMetadata novaMetadata, final Integer deploymentId, final Long pageSize, final Long pageNumber) throws Errors
    {
        try
        {
            return deploymentsApiService.getHistory(deploymentId, pageSize, pageNumber);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public void addDeploymentChange(final NovaMetadata novaMetadata, final DeploymentChangeDto change, final Integer deploymentId) throws Errors
    {
        try
        {
            deploymentsApiService.addDeploymentChange(change, deploymentId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }


    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public void deletePlan(final NovaMetadata novaMetadata, final Integer deploymentId) throws Errors
    {
        try
        {
            deploymentsApiService.deletePlan(MetadataUtils.getIvUser(novaMetadata), deploymentId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public ActionStatus getDeploymentSubsystemStatus(final NovaMetadata novaMetadata, final Integer subsystemId) throws Errors
    {
        return deploymentsApiService.getDeploymentSubsystemStatus(subsystemId);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public DeploymentSummaryDto[] getDeploymentPlansByEnvironmentAndFilters(final NovaMetadata novaMetadata, final Integer productId, final String environment, final String endDate, final String startDate, final String status) throws Errors
    {
        try
        {
            return this.deploymentsApiService.getDeploymentPlansByEnvironmentAndFilters(MetadataUtils.getIvUser(novaMetadata), productId, environment, endDate, startDate, status);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceException(e, productId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public void archiveDeploymentPlan(final NovaMetadata novaMetadata, final Integer deploymentId) throws Errors
    {
        try
        {
            deploymentsApiService.archiveDeploymentPlan(MetadataUtils.getIvUser(novaMetadata), deploymentId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public DeploymentDtoPage getDeploymentPlansBySearchTextFilter(NovaMetadata novaMetadata, Integer productId, String statuses, Integer pageSize, String searchText, Integer pageNumber) throws Errors
    {
        try
        {
            return deploymentsApiService.getDeploymentPlansBySearchTextFilter(MetadataUtils.getIvUser(novaMetadata), productId, statuses, searchText, pageSize, pageNumber);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceException(e, productId);
        }
    }


    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public DeploymentDto copyPlan(final NovaMetadata novaMetadata, final Integer deploymentId) throws Errors
    {
        try
        {
            return deploymentsApiService.copyPlan(MetadataUtils.getIvUser(novaMetadata), deploymentId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public DeploymentDto getDeploymentPlan(final NovaMetadata novaMetadata, final Integer deploymentId) throws Errors
    {
        try
        {
            return deploymentsApiService.getDeploymentPlan(MetadataUtils.getIvUser(novaMetadata), deploymentId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(generateTodotask = false, apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO deploy(final NovaMetadata novaMetadata, final Integer deploymentId, final Boolean force) throws Errors
    {
        try
        {
            return deploymentsApiService.deploy(MetadataUtils.getIvUser(novaMetadata), deploymentId, force);
        }
        catch (RuntimeException e)
        {
            //NOTE : @LogAndTrace with generateTodoTask flag disable to avoid create 2 TodoTask
            deploymentsApiService.treatDeployError(MetadataUtils.getIvUser(novaMetadata), deploymentId, e);
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public DeploymentSummaryDto createDeploymentPlan(final NovaMetadata novaMetadata, final Integer versionId, final String environment) throws Errors
    {
        try
        {
            return deploymentsApiService.createDeploymentPlan(MetadataUtils.getIvUser(novaMetadata), versionId, environment);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, versionId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public StatusDTO getDeploymentPlanServicesStatus(final NovaMetadata novaMetadata, final Integer deploymentId, final String environment) throws Errors
    {
        try
        {
            return deploymentsApiService.getDeploymentPlanServicesStatus(deploymentId, environment);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public TaskRequestDTO[] getAllConfigurationManagement(final NovaMetadata novaMetadata, final Integer deploymentId) throws Errors
    {
        try
        {
            return deploymentsApiService.getAllConfigurationManagement(deploymentId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public void taskRequest(final NovaMetadata novaMetadata, final TaskRequestDTO[] task, final Integer deploymentId) throws Errors
    {
        try
        {
            deploymentsApiService.taskRequest(MetadataUtils.getIvUser(novaMetadata), task, deploymentId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public String[] getDeploymentStatus(NovaMetadata novaMetadata) throws Errors
    {
        return this.deploymentsApiService.getDeploymentStatus();
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public ActionStatus getDeploymentInstanceStatus(final NovaMetadata novaMetadata, final Integer instanceId) throws Errors
    {
        return deploymentsApiService.getDeploymentInstanceStatus(instanceId);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public void instanceDeployStatus(final NovaMetadata novaMetadata, final Integer instanceId, final String statusMessage, final String status) throws Errors
    {
        deploymentsApiService.instanceDeployStatus(instanceId, statusMessage, status);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public SubsystemServicesStatusDTO[] getDeploymentPlanSubsystemsServicesStatus(final NovaMetadata novaMetadata, final Integer deploymentId, final String environment) throws Errors
    {
        try
        {
            return deploymentsApiService.getDeploymentPlanSubsystemsServicesStatus(deploymentId, environment);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public ActionStatus getDeploymentServiceStatus(final NovaMetadata novaMetadata, final Integer serviceId) throws Errors
    {
        return deploymentsApiService.getDeploymentServiceStatus(serviceId);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR)
    public TodoTaskResponseDTO remove(final NovaMetadata novaMetadata, final Integer deploymentId) throws Errors
    {
        try
        {
            return deploymentsApiService.remove(MetadataUtils.getIvUser(novaMetadata), deploymentId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    /////////////////////////////////////////// Get and refresh for cards /////////////////////////////////////////

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, generateTodotask = false, debugLogLevel = true)
    public DeploymentPlanCardStatusesDto getDeploymentPlanCardStatuses(NovaMetadata novaMetadata, Integer deploymentId) throws Errors
    {
        try
        {
            return this.deploymentsApiService.getDeploymentPlanCardStatuses(deploymentId, MetadataUtils.getIvUser(novaMetadata));
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, generateTodotask = false, debugLogLevel = true)
    public DeploymentSubsystemStatusDto[] getDeploymentSubsystemServiceCardStatuses(NovaMetadata novaMetadata, Integer deploymentPlanId) throws Errors
    {
        try
        {
            return this.deploymentsApiService.getDeploymentSubsystemServiceCardStatuses(deploymentPlanId, MetadataUtils.getIvUser(novaMetadata));
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentPlanId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, generateTodotask = false, debugLogLevel = true)
    public DeploymentInstanceStatusDto[] getDeploymentInstanceCardStatuses(NovaMetadata novaMetadata, Integer deploymentServiceId) throws Errors
    {
        return this.deploymentsApiService.getDeploymentInstanceCardStatuses(deploymentServiceId, MetadataUtils.getIvUser(novaMetadata));
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, generateTodotask = false, debugLogLevel = true)
    public ActionStatusDTO getDeploymentPlanCardActionStatus(NovaMetadata novaMetadata, Integer deploymentPlanId) throws Errors
    {
        try
        {
            return this.deploymentsApiService.getDeploymentPlanCardActionStatus(deploymentPlanId);
        }
        catch (RuntimeException e)
        {
            throw new LogAndTraceDeploymentException(e, deploymentPlanId);
        }
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, generateTodotask = false, debugLogLevel = true)
    public ActionStatusDTO getDeploymentSubsystemCardActionStatus(NovaMetadata novaMetadata, Integer deploymentSubsystemId) throws Errors
    {
        return this.deploymentsApiService.getDeploymentSubsystemCardActionStatus(deploymentSubsystemId);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, generateTodotask = false, debugLogLevel = true)
    public ActionStatusDTO getDeploymentServiceCardActionStatus(NovaMetadata novaMetadata, Integer deploymentServiceId) throws Errors
    {
        return this.deploymentsApiService.getDeploymentServiceCardActionStatus(deploymentServiceId);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, generateTodotask = false, debugLogLevel = true)
    public ActionStatusDTO getDeploymentInstanceCardActionStatus(NovaMetadata novaMetadata, Integer deploymentInstanceId) throws Errors
    {
        return this.deploymentsApiService.getDeploymentInstanceCardActionStatus(deploymentInstanceId);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, generateTodotask = false, debugLogLevel = true)
    public MinimalDeploymentSubsystemStatusDto getDeploymentSubsystemCardRefresh(NovaMetadata novaMetadata, Integer deploymentSubsystemId) throws Errors
    {
        return this.deploymentsApiService.getDeploymentSubsystemCardRefresh(MetadataUtils.getIvUser(novaMetadata), deploymentSubsystemId);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, generateTodotask = false, debugLogLevel = true)
    public DeploymentServiceStatusDto getDeploymentServiceCardRefresh(NovaMetadata novaMetadata, Integer deploymentServiceId) throws Errors
    {
        return this.deploymentsApiService.getDeploymentServiceCardRefresh(MetadataUtils.getIvUser(novaMetadata), deploymentServiceId);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, generateTodotask = false, debugLogLevel = true)
    public DeploymentInstanceStatusDto getDeploymentInstanceCardRefresh(NovaMetadata novaMetadata, Integer deploymentInstanceId) throws Errors
    {
        return this.deploymentsApiService.getDeploymentInstanceCardRefresh(MetadataUtils.getIvUser(novaMetadata), deploymentInstanceId);
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public String[] getPlatforms(NovaMetadata novaMetadata) throws Errors
    {
        return this.deploymentsApiService.getPlatforms();
    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public LMLibraryRequirementsFulfilledDTO[] getAllRequirementsOfUsedLibraries(final NovaMetadata novaMetadata, int[] deploymentServiceIds) throws Errors
    {
        return this.deploymentsApiService.getAllRequirementsOfUsedLibraries(deploymentServiceIds);

    }

    @Override
    @LogAndTrace(apiName = DeploymentConstants.DEPLOYMENT_API_NAME, runtimeExceptionErrorCode = DeploymentConstants.DeployErrors.UNEXPECTED_ERROR, debugLogLevel = true)
    public DeploymentPlanLibraryRequirementsDTO[] getAllRequirementsOfUsedLibrariesForPlans(
            NovaMetadata novaMetadata, int[] deploymentIds) throws Errors
    {
        return this.deploymentsApiService.getAllRequirementsOfUsedLibrariesForPlans(deploymentIds);
    }
}
