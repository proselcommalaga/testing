package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.ServiceRunnerConstants;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.ServiceRunnerConstants.ServiceRunnerErrors;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.enumerates.DeploymentInstanceStatus;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.StatisticsConstants;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

@ExposeErrorCodes
public class ServiceRunnerError
{


    public static NovaError getUnexpectedError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.UNEXPECTED_ERROR,
               "Unexpected internal error",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    public static NovaError getInvalidUserError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.INVALID_USER,
                "The user who invokes de operation is not a member of the product",
                "Use a user with higher privileges or request permission for your user",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchUserError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.NO_SUCH_USER,
                "The user who invokes de operation is not present in Nova Data Base",
                "Use a user that exists in Nova Data Base",
                HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchInstanceError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.NO_SUCH_INSTANCE,
                "The instance is not present in Nova Data Base",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchServiceError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.NO_SUCH_SERVICE,
                "The service is not present in Nova Data Base",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchSubsystemError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.NO_SUCH_SUBSYSTEM,
                "The Subsystem is not present in Nova Data Base",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchDeploymentError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.NO_SUCH_DEPLOYMENT,
                "The Deployment plan is not present in Nova Data Base",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    public static NovaError getStartInstanceError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_START_INSTANCE,
                "Error trying to start an instance",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    public static NovaError getStopInstanceError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_STOP_INSTANCE,
                "Error trying to stop an instance",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    public static NovaError getStartServiceError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_START_SERVICE,
                "Error trying to start a service",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    public static NovaError getStopServiceError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_STOP_SERVICE,
                "Error trying to stop a service",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    public static NovaError getStartSubsystemError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_START_SUBSYSTEM,
                "Error trying to start a subsystem",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    public static NovaError getStopSubsystemError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_STOP_SUBSYSTEM,
                "Error trying to stop a subsystem",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    public static NovaError getStartPlanError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_START_PLAN,
                "Error trying to start a deployment and execution plan",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    public static NovaError getStopPlanError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_STOP_PLAN,
                "Error trying to stop a deployment and execution plan",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    public static NovaError getPendingActionTaskError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.PENDING_ACTION_TASK,
                "Tried to create a task of an action when there is already one pending of the same type",
                "Only one action task of the same type is allowed",
                HttpStatus.CONFLICT,
                ErrorMessageType.WARNING);
    }

    public static NovaError getNoSuchJiraProjectKeyError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.NO_SUCH_JIRA_PROJECT_KEY,
                "The product does not have a Jira Project Key",
                "Contact to JIRA team for getting a Jira project key and set it into this NOVA product",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getRoleFrozenPlanError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_ROLE_FROZEN_PLAN,
                "The plan is already frozen and user has not privileges",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getEnableBatchScheduleServiceError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ENABLE_BATCH_SCHEDULE_SERVICE_ERROR,
                "The batch schedule service cannot be actived",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getDisableBatchScheduleServiceError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.DISABLE_BATCH_SCHEDULE_SERVICE_ERROR,
                "The batch schedule service cannot be disabled",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.FORBIDDEN,
                ErrorMessageType.WARNING);
    }

    public static NovaError getCronExpressionError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.CRON_EXPRESSION_ERROR,
                "The cron expression is not valid for the batch scheduler",
                "The user must change the cron expression and set a valid NOVA cron expression",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.WARNING);
    }

    public static NovaError getCronExpressionRequirementsError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.CRON_EXPRESSION_REQUIREMENTS,
                "The provided cron expression(s) doesn't meet NOVA requirements",
                "Change provided cron expression(s) for this batch schedule",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.WARNING);
    }

    public static NovaError getPauseInstanceError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_PAUSE_INSTANCE,
                "Error trying to pause an instance",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getResumeInstanceError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_RESUME_INSTANCE,
                "Error trying to resume an instance",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchReleaseVersionError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.NO_SUCH_RELEASE_VERSION,
                "Error trying to get a release version id",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getBatchAgentError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_BATCH_AGENT,
                "Error calling Batch Agent for starting batch service",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getBatchManagerError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_BATCH_MANAGER,
                "Error calling Batch Manager for starting batch service",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getBatchSchedulerError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_BATCH_SCHEDULER,
                "Error calling Batch Scheduler",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getPlannedScheduleError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_NOVA_PLANNED_SCHEDULE,
                "Error trying to schedule a nova planned",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getPlannedUnScheduleError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.ERROR_IN_NOVA_PLANNED_UNSCHEDULE,
                "Error trying to unschedule a nova planned",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getForbiddenError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.PERMISSION_DENIED,
                "The current user does not have permission for this operation",
                "Use a user with higher privileges or request permission for your user",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    public static NovaError getTaskIdIsNullError()
    {
        return new NovaError(ServiceRunnerConstants.ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.TASK_ID_IS_NULL,
                "The task id can not be null if task has been generated",
                "Task id should be set if task has been generated, contact whit NOVA administrator",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    public static NovaError getBatchScheduleForbiddenError()
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.BARCH_SCHEDULE_PERMISSIONS_DENIED,
                "The current user does not have permission for this operation.",
                "Just the users assigned as PRODUCT OWNER of this product can make this operation. Review users who are Product owner in your product and contact with them to make this operation.",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Returns a NovaError indicating that the DeploymentInstanceStatus used to filter is not a valid one.
     *
     * @param deploymentInstanceStatus The non-valid Deployment Instance Status.
     * @return The NovaError.
     */
    public static NovaError getDeploymentInstanceStatusNotValidError(final String deploymentInstanceStatus)
    {
        return new NovaError(
                ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.DEPLOYMENT_INSTANCE_STATUS_NOT_VALID_ERROR_CODE,
                String.format("Deployment Instance Status [%s] is not valid.", deploymentInstanceStatus),
                String.format("Please use one of the following valid values: %s, or %s (to not filter by Deployment Instance Status)", Arrays.toString(DeploymentInstanceStatus.values()), StatisticsConstants.NO_FILTER_PARAMETER),
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING
        );
    }

    public static NovaError getDeploymentBatchScheduleInstanceError(final Integer deploymentBatchScheduleInstance, final String action, final Integer deploymentPlanId)
    {
        return new NovaError(
                ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.DEPLOYMENT_SCHEDULE_INSTANCE_ID_ERROR_CODE,
                "The deployment batch schedule instance id: [" + deploymentBatchScheduleInstance + "] provided does not found for doing action: [" + action + "]",
                "Please, refresh the page and review if the deployment batch schedule service or the deployment plan id: [" + deploymentPlanId + "] associated is still DEPLOYED",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR
        );
    }

    public static NovaError getRestartEphoenixInstanceForbiddenError(final String serviceName, final String productName)
    {
        return new NovaError(
                ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.EPHOENIX_INSTANCE_RESTART_FORBIDDEN_ERROR_CODE,
                "The service name with name : [" + serviceName + "] of product : ["+productName+"] could not been restart by control-m planification",
                "Please, check if service type is ePhoenix, this action is only available for ePhoenix service types.",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR
        );
    }

    public static NovaError getNoSuchDeploymentServiceError(String serviceName)
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.NO_SUCH_DEPLOYMENT_SERVICE_ERROR_CODE,
                "The Deployment Service ["+serviceName+"] is not present in Nova Data Base",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    public static NovaError InvalidInputParameter(final String parameter, final String invalidValue)
    {
        return new NovaError(ServiceRunnerErrors.CLASS_NAME,
                ServiceRunnerErrors.INVALID_INPUT_PARAMETER_ERROR_CODE,
                "The input parameter ["+parameter+"] has an invalid value:["+invalidValue+"]",
                "check the input parameters or contact with NOVA team",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }
}
