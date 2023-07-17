package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils;

/**
 * ServiceRunner service permissions
 *
 * @author XE63267
 */
public class ServiceRunnerConstants {

    /**
     * Literal for api name and method of Service runner API
     */
    public static final String SERVICE_RUNNER_API_NAME = "ServiceRunnerAPI";

    /**
     * The constant START_PLAN_PERMISSION.
     */
    public static final String START_PLAN_PERMISSION = "START_PLAN";
    /**
     * The constant START_SUBSYSTEM_PERMISSION.
     */
    public static final String START_SUBSYSTEM_PERMISSION = "START_SUBSYSTEM";
    /**
     * The constant START_SERVICE_PERMISSION.
     */
    public static final String START_SERVICE_PERMISSION = "START_SERVICE";
    /**
     * The constant START_INSTANCE_PERMISSION.
     */
    public static final String START_INSTANCE_PERMISSION = "START_INSTANCE";
    /**
     * The constant RESTART_PLAN_PERMISSION.
     */
    public static final String RESTART_PLAN_PERMISSION = "RESTART_PLAN";
    /**
     * The constant RESTART_SUBSYSTEM_PERMISSION.
     */
    public static final String RESTART_SUBSYSTEM_PERMISSION = "RESTART_SUBSYSTEM";
    /**
     * The constant RESTART_SERVICE_PERMISSION.
     */
    public static final String RESTART_SERVICE_PERMISSION = "RESTART_SERVICE";
    /**
     * The constant RESTART_INSTANCE_PERMISSION.
     */
    public static final String RESTART_INSTANCE_PERMISSION = "RESTART_INSTANCE";
    /**
     * The constant STOP_PLAN_PERMISSION.
     */
    public static final String STOP_PLAN_PERMISSION = "STOP_PLAN";
    /**
     * The constant STOP_SUBSYSTEM_PERMISSION.
     */
    public static final String STOP_SUBSYSTEM_PERMISSION = "STOP_SUBSYSTEM";
    /**
     * The constant STOP_SERVICE_PERMISSION.
     */
    public static final String STOP_SERVICE_PERMISSION = "STOP_SERVICE";
    /**
     * The constant STOP_INSTANCE_PERMISSION.
     */
    public static final String STOP_INSTANCE_PERMISSION = "STOP_INSTANCE";
    /**
     * The constant START_BATCH_SCHEDULE_PERMISSION.
     */
    public static final String START_BATCH_SCHEDULE_PERMISSION = "START_BATCH_SCHEDULE";
    /**
     * The constant STOP_BATCH_SCHEDULE_PERMISSION.
     */
    public static final String STOP_BATCH_SCHEDULE_PERMISSION = "STOP_BATCH_SCHEDULE";
    /**
     * The constant START_BATCH_SCHEDULE_INSTANCE_PERMISSION.
     */
    public static final String START_BATCH_SCHEDULE_INSTANCE_PERMISSION = "START_BATCH_SCHEDULE_INSTANCE";
    /**
     * The constant PAUSE_BATCH_SCHEDULE_INSTANCE_PERMISSION.
     */
    public static final String PAUSE_BATCH_SCHEDULE_INSTANCE_PERMISSION = "PAUSE_BATCH_SCHEDULE_INSTANCE";
    /**
     * The constant RESUME_BATCH_SCHEDULE_INSTANCE_PERMISSION.
     */
    public static final String RESUME_BATCH_SCHEDULE_INSTANCE_PERMISSION = "RESUME_BATCH_SCHEDULE_INSTANCE";
    /**
     * The constant STOP_BATCH_SCHEDULE_INSTANCE_PERMISSION.
     */
    public static final String STOP_BATCH_SCHEDULE_INSTANCE_PERMISSION = "STOP_BATCH_SCHEDULE_INSTANCE";

    /**
     * The type Service runner errors.
     */
    public static final class ServiceRunnerErrors
    {

        /**
         * ServiceRunnerErrors class name for exception
         */
        public static final String CLASS_NAME = "ServiceRunnerErrors";

        /**
         * Unexpected error code
         */
        public static final String UNEXPECTED_ERROR = "SERVICERUNNER-000";

        /**
         * The user who invokes de operation is not a member of the product.
         */
        public static final String INVALID_USER = "SERVICERUNNER-001";

        /**
         * The user who invokes de operation is not present in Nova Data Base
         */
        public static final String NO_SUCH_USER = "SERVICERUNNER-002";

        /**
         * The instance is not present in Nova Data Base
         */
        public static final String NO_SUCH_INSTANCE = "SERVICERUNNER-003";

        /**
         * The service is not present in Nova Data Base
         */
        public static final String NO_SUCH_SERVICE = "SERVICERUNNER-004";

        /**
         * The Subsystem is not present in Nova Data Base
         */
        public static final String NO_SUCH_SUBSYSTEM = "SERVICERUNNER-005";

        /**
         * The Deployment plan is not present in Nova Data Base
         */
        public static final String NO_SUCH_DEPLOYMENT = "SERVICERUNNER-006";

        /**
         * Error trying to start an instance
         */
        public static final String ERROR_IN_START_INSTANCE = "SERVICERUNNER-007";

        /**
         * Error trying to stop an instance
         */
        public static final String ERROR_IN_STOP_INSTANCE = "SERVICERUNNER-008";

        /**
         * Error trying to start a service
         */
        public static final String ERROR_IN_START_SERVICE = "SERVICERUNNER-009";

        /**
         * Error trying to stop a service
         */
        public static final String ERROR_IN_STOP_SERVICE = "SERVICERUNNER-010";

        /**
         * Error trying to start a subsystem
         */
        public static final String ERROR_IN_START_SUBSYSTEM = "SERVICERUNNER-011";

        /**
         * Error trying to stop a subsystem
         */
        public static final String ERROR_IN_STOP_SUBSYSTEM = "SERVICERUNNER-012";

        /**
         * Error trying to start a deployment and execution plan"
         */
        public static final String ERROR_IN_START_PLAN = "SERVICERUNNER-013";

        /**
         * Error trying to stop a deployment and execution plan
         */
        public static final String ERROR_IN_STOP_PLAN = "SERVICERUNNER-014";

        /**
         * Tried to create a task of an action when there is already one pending of the same type
         */
        public static final String PENDING_ACTION_TASK = "SERVICERUNNER-015";

        /**
         * The product does not have a Jira Project Key
         */
        public static final String NO_SUCH_JIRA_PROJECT_KEY = "SERVICERUNNER-016";

        /**
         * The plan is already frozen and user has not privileges
         */
        public static final String ERROR_ROLE_FROZEN_PLAN = "SERVICERUNNER-017";

        /**
         * The batch schedule service cannot be actived
         */
        public static final String ENABLE_BATCH_SCHEDULE_SERVICE_ERROR = "SERVICERUNNER-018";

        /**
         * The batch schedule service cannot be disabled
         */
        public static final String DISABLE_BATCH_SCHEDULE_SERVICE_ERROR = "SERVICERUNNER-019";

        /**
         * The cron expression is not valid for the batch scheduler
         */
        public static final String CRON_EXPRESSION_ERROR = "SERVICERUNNER-020";

        /**
         * The provided cron expression(s) doesn't meet NOVA requirements
         */
        public static final String CRON_EXPRESSION_REQUIREMENTS = "SERVICERUNNER-021";

        /**
         * Error trying to pause an instance
         */
        public static final String ERROR_IN_PAUSE_INSTANCE = "SERVICERUNNER-022";

        /**
         * Error trying to resume an instance
         */
        public static final String ERROR_IN_RESUME_INSTANCE = "SERVICERUNNER-023";

        /**
         * Error trying to get a release version id
         */
        public static final String NO_SUCH_RELEASE_VERSION = "SERVICERUNNER-024";

        /**
         * Error calling Batch Agent for starting batch service
         */
        public static final String ERROR_IN_BATCH_AGENT = "SERVICERUNNER-025";

        /**
         * Error calling Batch Manager for starting batch service
         */
        public static final String ERROR_IN_BATCH_MANAGER = "SERVICERUNNER-026";

        /**
         * Error calling Batch Scheduler
         */
        public static final String ERROR_IN_BATCH_SCHEDULER = "SERVICERUNNER-027";


        /**
         * Error trying to schedule a nova planned
         */
        public static final String ERROR_IN_NOVA_PLANNED_SCHEDULE = "SERVICERUNNER-028";

        /**
         * Error trying to unschedule a nova planned
         */
        public static final String ERROR_IN_NOVA_PLANNED_UNSCHEDULE = "SERVICERUNNER-029";

        /**
         * Task ID is null error code
         */
        public static final String TASK_ID_IS_NULL = "SERVICERUNNER-030";

        /**
         * Task ID is null error code
         */
        public static final String BARCH_SCHEDULE_PERMISSIONS_DENIED = "SERVICERUNNER-031";

        /** User does not have permission for the action requested */
        public static final String PERMISSION_DENIED = "USER-002";

        /** Deployment Instance Status not valid error */
        public static final String DEPLOYMENT_INSTANCE_STATUS_NOT_VALID_ERROR_CODE = "SERVICERUNNER-032";

        /**
         * Deployment schedule instance id error
         */
        public static final String DEPLOYMENT_SCHEDULE_INSTANCE_ID_ERROR_CODE = "SERVICERUNNER-033";

        /**
         * Deployment ephoenix instance restard error
         */
        public static final String EPHOENIX_INSTANCE_RESTART_FORBIDDEN_ERROR_CODE = "SERVICERUNNER-034";

        /**
         * Deployment schedule service not found error
         */
        public static final String NO_SUCH_DEPLOYMENT_SERVICE_ERROR_CODE = "SERVICERUNNER-035";

        /**
         * Invalid input paramter
         */
        public static final String INVALID_INPUT_PARAMETER_ERROR_CODE = "SERVICERUNNER-036";

    }
}
