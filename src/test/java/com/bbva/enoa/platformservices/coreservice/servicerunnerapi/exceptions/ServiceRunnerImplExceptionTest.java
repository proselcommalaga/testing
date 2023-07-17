package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.ServiceRunnerConstants;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils.enumerates.DeploymentInstanceStatus;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.StatisticsConstants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
public class ServiceRunnerImplExceptionTest
{
    @Test
    public void getErrorCodeTest() throws Exception
    {
        NovaException exception = new NovaException(ServiceRunnerError.getUnexpectedError());
        NovaError error = exception.getErrorCode();

        assertNotNull(error);
        assertEquals(ServiceRunnerError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(ServiceRunnerError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    public void getErrorCodeCustomMessageTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getUnexpectedError(), "Custom exception message");
        NovaError error = exception.getErrorCode();

        assertNotNull(error);
        assertEquals(ServiceRunnerError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(ServiceRunnerError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    public void getErrorCodeRuntimeExceptionTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getUnexpectedError(), new RuntimeException(), "Custom exception message");
        NovaError error = exception.getErrorCode();

        assertNotNull(error);
        assertEquals(ServiceRunnerError.getUnexpectedError().getErrorCode(), error.getErrorCode());
        assertEquals(ServiceRunnerError.getUnexpectedError().getErrorMessage(), error.getErrorMessage());
    }

    @Test
    public void getUnexpectedErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getUnexpectedError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.UNEXPECTED_ERROR, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, httpStatus);
        assertEquals(ErrorMessageType.FATAL, errorMessageType);



    };

    @Test
    public void getInvalidUserErrorTest()
    {

        NovaException exception = new NovaException(ServiceRunnerError.getInvalidUserError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.INVALID_USER, errorCode);
        assertEquals("Use a user with higher privileges or request permission for your user", actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getNoSuchUserErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getNoSuchUserError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.NO_SUCH_USER, errorCode);
        assertEquals("Use a user that exists in Nova Data Base", actionMessage);
        assertEquals(HttpStatus.NOT_FOUND, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getNoSuchInstanceErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getNoSuchInstanceError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.NO_SUCH_INSTANCE, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.NOT_FOUND, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getNoSuchServiceErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getNoSuchServiceError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.NO_SUCH_SERVICE, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.NOT_FOUND, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getNoSuchSubsystemErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getNoSuchSubsystemError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.NO_SUCH_SUBSYSTEM, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.NOT_FOUND, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getNoSuchDeploymentErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getNoSuchDeploymentError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.NO_SUCH_DEPLOYMENT, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.NOT_FOUND, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getStartInstanceErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getStartInstanceError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_START_INSTANCE, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.CONFLICT, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getStopInstanceErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getStopInstanceError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_STOP_INSTANCE, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.CONFLICT, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getStartServiceErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getStartServiceError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_START_SERVICE, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.CONFLICT, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getStopServiceErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getStopServiceError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_STOP_SERVICE, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.CONFLICT, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getStartSubsystemErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getStartSubsystemError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_START_SUBSYSTEM, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.CONFLICT, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getStopSubsystemErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getStopSubsystemError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_STOP_SUBSYSTEM, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.CONFLICT, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getStartPlanErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getStartPlanError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_START_PLAN, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.CONFLICT, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getStopPlanErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getStopPlanError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_STOP_PLAN, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.CONFLICT, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getPendingActionTaskErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getPendingActionTaskError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.PENDING_ACTION_TASK, errorCode);
        assertEquals("Only one action task of the same type is allowed", actionMessage);
        assertEquals(HttpStatus.CONFLICT, httpStatus);
        assertEquals(ErrorMessageType.WARNING, errorMessageType);
    };

    @Test
    public void getNoSuchJiraProjectKeyErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getNoSuchJiraProjectKeyError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.NO_SUCH_JIRA_PROJECT_KEY, errorCode);
        assertEquals("Contact to JIRA team for getting a Jira project key and set it into this NOVA product", actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getRoleFrozenPlanErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getRoleFrozenPlanError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_ROLE_FROZEN_PLAN, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getEnableBatchScheduleServiceErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getEnableBatchScheduleServiceError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ENABLE_BATCH_SCHEDULE_SERVICE_ERROR, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getDisableBatchScheduleServiceErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getDisableBatchScheduleServiceError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.DISABLE_BATCH_SCHEDULE_SERVICE_ERROR, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.WARNING, errorMessageType);
    };

    @Test
    public void getCronExpressionErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getCronExpressionError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.CRON_EXPRESSION_ERROR, errorCode);
        assertEquals("The user must change the cron expression and set a valid NOVA cron expression", actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.WARNING, errorMessageType);
    };

    @Test
    public void getCronExpressionRequirementsErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getCronExpressionRequirementsError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.CRON_EXPRESSION_REQUIREMENTS, errorCode);
        assertEquals("Change provided cron expression(s) for this batch schedule", actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.WARNING, errorMessageType);
    };

    @Test
    public void getPauseInstanceErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getPauseInstanceError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_PAUSE_INSTANCE, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getResumeInstanceErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getResumeInstanceError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_RESUME_INSTANCE, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getNoSuchReleaseVersionErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getNoSuchReleaseVersionError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.NO_SUCH_RELEASE_VERSION, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getBatchAgentErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getBatchAgentError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_BATCH_AGENT, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getBatchManagerErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getBatchManagerError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_BATCH_MANAGER, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getBatchSchedulerErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getBatchSchedulerError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_BATCH_SCHEDULER, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getPlannedScheduleErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getPlannedScheduleError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_NOVA_PLANNED_SCHEDULE, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getPlannedUnScheduleErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getPlannedUnScheduleError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.ERROR_IN_NOVA_PLANNED_UNSCHEDULE, errorCode);
        assertEquals(Constants.MSG_CONTACT_NOVA, actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getForbiddenErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getForbiddenError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.PERMISSION_DENIED, errorCode);
        assertEquals("Use a user with higher privileges or request permission for your user", actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getTaskIdIsNullErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getTaskIdIsNullError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.TASK_ID_IS_NULL, errorCode);
        assertEquals("Task id should be set if task has been generated, contact whit NOVA administrator", actionMessage);
        assertEquals(HttpStatus.BAD_REQUEST, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getBatchScheduleForbiddenErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getBatchScheduleForbiddenError());

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.BARCH_SCHEDULE_PERMISSIONS_DENIED, errorCode);
        assertEquals("Just the users assigned as PRODUCT OWNER of this product can make this operation. Review users who are Product owner in " +
                "your product and contact with them to make this operation.", actionMessage);
        assertEquals(HttpStatus.FORBIDDEN, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };

    @Test
    public void getDeploymentInstanceStatusNotValidErrorTest()
    {
        NovaException exception = new NovaException(ServiceRunnerError.getDeploymentInstanceStatusNotValidError(new String("VALUE1")));

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.DEPLOYMENT_INSTANCE_STATUS_NOT_VALID_ERROR_CODE, errorCode);
        assertEquals(String.format("Please use one of the following valid values: %s, or %s (to not filter by Deployment Instance Status)", Arrays.toString(DeploymentInstanceStatus.values()), StatisticsConstants.NO_FILTER_PARAMETER), actionMessage);
        assertEquals(HttpStatus.BAD_REQUEST, httpStatus);
        assertEquals(ErrorMessageType.WARNING, errorMessageType);
    };


    @Test
    public void getDeploymentBatchScheduleInstanceErrorTest()
    {
        final Integer deploymeBatchSchedulerInstance = 1;
        final String action = "ACTION1";
        final Integer deploymentPlanId = 1;
        NovaException exception = new NovaException(ServiceRunnerError.getDeploymentBatchScheduleInstanceError(deploymeBatchSchedulerInstance, action, deploymentPlanId));

        NovaError error         = exception.getErrorCode();
        String errorCode        = exception.getNovaError().getErrorCode();
        String actionMessage    = exception.getNovaError().getActionMessage();
        HttpStatus httpStatus   = exception.getNovaError().getHttpStatus();
        ErrorMessageType errorMessageType = exception.getNovaError().getErrorMessageType();

        assertNotNull(error);
        assertNotNull(errorCode);
        assertNotNull(actionMessage);
        assertNotNull(httpStatus);
        assertNotNull(errorMessageType);

        assertNotNull(error);

        assertEquals(ServiceRunnerConstants.ServiceRunnerErrors.DEPLOYMENT_SCHEDULE_INSTANCE_ID_ERROR_CODE, errorCode);
        assertEquals(String.format("Please, refresh the page and review if the deployment batch schedule service or the deployment plan id: [" + deploymentPlanId + "] associated is still DEPLOYED"), actionMessage);
        assertEquals(HttpStatus.BAD_REQUEST, httpStatus);
        assertEquals(ErrorMessageType.ERROR, errorMessageType);
    };




}