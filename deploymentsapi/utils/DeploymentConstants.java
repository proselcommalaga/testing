package com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils;

import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;

import java.util.List;

/**
 * Permissions for deployment operations
 *
 * @author XE63267
 */
public class DeploymentConstants
{
    public static final String EDIT_DEPLOYMENT_PERMISSION = "EDIT_DEPLOYMENT";
    public static final String DELETE_DEPLOYMENT_PERMISSION = "DELETE_DEPLOYMENT";
    public static final String EDIT_DEPLOYMENT_TYPE_PERMISSION = "EDIT_DEPLOYMENT_TYPE";
    public static final String CREATE_DEPLOYMENT_PERMISSION = "CREATE_DEPLOYMENT";
    public static final String ARCHIVE_DEPLOYMENT_PERMISSION = "ARCHIVE_DEPLOYMENT";
    public static final String DEPLOY_DEPLOYMENT_PERMISSION = "DEPLOY_DEPLOYMENT";
    public static final String REQUEST_DEPLOYMENT_PROPERTIES_PERMISSION = "REQUEST_DEPLOYMENT_PROPERTIES";
    public static final String UNDEPLOY_DEPLOYMEMT_PERMISSION = "UNDEPLOY_DEPLOYMENT";
    public static final String PROMOTE_PLAN_PERMISSION = "PROMOTE_PLAN";
    public static final String COPY_PLAN_PERMISSION = "COPY_PLAN";
    public static final String NEW_DEPLOYMENT_PERMISSION = "NEW_DEPLOYMENT";
    public static final String MANAGE_DEPLOY = "MANAGE_DEPLOY";
    public static final String UNSCHEDULE_PLAN = "UNSCHEDULE_PLAN";

    /**
     * Available statueses for Alert service client
     */
    public static final String OPEN_ALERT = "OPEN";


    /**
     * Success status for services
     */
    public static final String DEPLOY_SERVICE_STATUS_SUCCESS = "SUCCESS";

    /**
     * Default value of pagesize
     */
    public static final int DEFAULT_PAGE_SIZE = 10;
    /**
     * Default value of pageNumber
     */
    public static final int DEFAULT_PAGE_NUMBER = 0;

    /**
     * Max number of instances for Batch
     */
    public static final int BATCH_MAX_INSTANCES = 1;

    /**
     * Max number of instances for EPhoenix
     */
    public static final int EPHOENIX_INSTANCES = 1;

    /**
     * Max number of chars to search
     **/
    public static final int MAX_LENGTH_SEARCH_TEXT = 100;


    /**
     * Name of API DeploymentManager
     */
    public static final String DEPLOYMENT_API_NAME = "DeploymentsApi";
    /**
     * Name of API DeploymentmanagercallbackAPI
     */
    public static final String DEPLOYMENT_CALLBACK_API_NAME = "Deploymentmanagercallback";


    public static final String[] DATE_FORMAT_PATTERNS = {"yyyy-MM-dd'T'HH:mm:ss.SSSZ"};

    public static final String LINE_SEPARATOR_PROPERTY = "line.separator";

    public static final String KO = "KO";
    public static final String DEPLOY = "DEPLOY";
    public static final String LOGGING = "LOGGING";

    /**
     * Message to show when a pending task is automatically closed because his plan has been deployed"
     **/
    public static final String CLOSE_PENDING_TASK_MESSAGE = "Cerrado automáticamente debido a que el plan asociado ha sido replegado";

    /**
     * Actions not allowed to check the status of the Ether Services
     */
    public static final List<DeploymentAction> ACTION_NOT_ALLOWED = List.of(DeploymentAction.DEPLOYING, DeploymentAction.UNDEPLOYING);

    /**
     * JMX parameter name
     */
    public static final String JMX_PARAMS_NAME = "JmxParams";

    /**
     * Replace action error class
     */
    public static final class ReplaceActionErrors
    {
        /**
         * Action keep message
         */
        public static final String ACTION_KEEP = "KEEP THE SAME VERSIONS";

        /**
         * Action update message
         */
        public static final String ACTION_UPDATE_TAG = "UPDATE TAG TO ";

        /**
         * Action update message
         */
        public static final String ACTION_UPDATE_INSTANCES = "UPDATE INSTANCES TO ";

        /**
         * Action remove message
         */
        public static final String ACTION_REMOVE = "REMOVE";

        /**
         * Action remove message
         */
        public static final String ACTION_CREATE = "CREATE";
    }

    /**
     * Change Hardware pack, filesystem or Connectors
     */
    public static final String CREATE_SERVICE = "CREATE_SERVICE";
    public static final String REMOVE_SERVICE = "REMOVE_SERVICE";
    public static final String RESTART_SERVICE = "RESTART_SERVICE";
    public static final String UPDATE_INSTANCE = "UPDATE_INSTANCE";

    /**
     * Deployments API errors code
     */
    public static final class DeployErrors
    {
        /**
         * deployment error class name
         */
        public static final String CLASS_NAME = "DeploymentError";

        //--------------------------------
        //      Error codes
        //--------------------------------

        /**
         * Unexpected error
         */
        public static final String UNEXPECTED_ERROR = "DEPLOY-000";

        /**
         * No deploy: Release version with errors error code
         */
        public static final String WRONG_RELEASE_VERSION_ERROR = "DEPLOY-003";

        /**
         * Release version not ready to deploy error code
         */
        public static final String NOT_READY_RELEASE_VERSION = "DEPLOY-004";

        /**
         * Deployment plan not found error code
         */
        public static final String NO_SUCH_DEPLOYMENT = "DEPLOY-005";

        /**
         * Trying to update a deployed plan error code
         */
        public static final String TRIED_TO_UPDATE_DEPLOYED_PLAN = "DEPLOY-006";

        /**
         * Error code wrong To Do task type
         */
        public static final String WRONG_TASK_TYPE = "DEPLOY-007";

        /**
         * Communication error with To Do Task service
         */
        public static final String ERROR_IN_CREATETASK = "DEPLOY-008";

        /**
         * error code by Trying to create a new task when there is a previous task of same type pending to answer
         */
        public static final String ALREADY_PENDINGTASK_IN_CREATETASK = "DEPLOY-009";

        /**
         * there are not slots available for deploying new plan
         */
        public static final String NO_SLOTS = "DEPLOY-013";

        /**
         * plan cannot be deployed because of its budget error code
         */
        public static final String BUDGET_ERROR = "DEPLOY-015";

        /**
         * plan not ready for deploying error code
         */
        public static final String PLAN_NOT_READY = "DEPLOY-016";

        /**
         * deployment service not found error code
         */
        public static final String NO_SUCH_SERVICE = "DEPLOY-018";

        /**
         * invalid copy for a plan from INT to wrong environment
         */
        public static final String TRIED_TO_PROMOTE_INT_PLAN_TO_WRONG_ENVIRONMENT = "DEPLOY-019";

        /**
         * invalid copy for a plan from PRE to wrong environment
         */
        public static final String TRIED_TO_PROMOTE_PRE_PLAN_TO_WRONG_ENVIRONMENT = "DEPLOY-020";

        /**
         * promote not deployed plan error code
         */
        public static final String TRIED_TO_PROMOTE_NOT_DEPLOYED_PLAN = "DEPLOY-021";

        /**
         * delete a not archived plan error code
         */
        public static final String TRIED_TO_DELETE_DEPLOYED_PLAN = "DEPLOY-022";

        /**
         * Error saving configuration into configuration manager
         */
        public static final String ERROR_SAVING_CONFIGURATIONS = "DEPLOY-024";

        /**
         * There's already an unresolved task of deployment type change
         */
        public static final String DEPLOYMENT_TYPE_CHANGE_PENDING = "DEPLOY-026";

        /**
         * There's already an unresolved task of deployment request on the environment error code
         */
        public static final String DEPLOYMENT_REQUEST_PENDING = "DEPLOY-027";

        /**
         * There's already an unresolved task of undeployment request on the environment error code
         */
        public static final String UNDEPLOYMENT_REQUEST_PENDING = "DEPLOY-028";

        /**
         * Tried to update service of a plan in PRO
         */
        public static final String TRIED_TO_UPDATE_SERVICE_IN_PRO = "DEPLOY-030";

        /**
         * plan without configuration revision error code
         */
        public static final String PLAN_WITHOUT_CURRENT_REVISION = "DEPLOY-034";

        /**
         * error code for  an operation which cannot be done because the product has no SQA services GB
         */
        public static final String PRODUCT_WITHOUT_SQA_CANON = "DEPLOY-036";

        /**
         * error code for an operation which cannot be done because the product has no NOVA services GB
         */
        public static final String PRODUCT_WITHOUT_NOVA_SERVICES_CANON = "DEPLOY-037";

        /**
         * Environment not found error code
         */
        public static final String ENVIRONMENT_NOT_FOUND = "DEPLOY-040";

        /**
         * Error code for trying  to promote a plan from PRE that has no filesystems in PRO
         */
        public static final String TRIED_TO_PROMOTE_PLAN_WITHOUT_FILESYSTEM_IN_PRO = "DEPLOY-041";

        /**
         * Error code for a Deployment plan belongs to a stored release versio. It is needed to restore the release version to copy the plan
         */
        public static final String STORED_RELEASE_VERSION = "DEPLOY-044";

        /**
         * Error code to indicate that Maximum number of plans reached. Store or delete plan
         */
        public static final String MAX_PLAN_REACHED = "DEPLOY-046";

        /**
         * Plan already scheduled error code
         */
        public static final String SCHEDULED_PLAN = "DEPLOY-049";

        /**
         * Error code for A selected plan to undeploy which is not a valid plan to undeploy. It is not a PRO plan or does not belongs to the selected plan
         */
        public static final String UNDEPLOY_PLAN_IS_NOT_VALID = "DEPLOY-051";

        /**
         * error code for trying to promote a plan from PRE that has no logical connector in PR
         */
        public static final String TRIED_TO_PROMOTE_PLAN_WITHOUT_LOGICAL_CONNECTOR_IN_PRO = "DEPLOY-054";

        /**
         * Error code for a selected plan to undeploy which could not be found
         */
        public static final String UNDEPLOY_PlAN_NOT_FOUND = "DEPLOY-055";

        /**
         * priority level for the schedule planning is not valid error code
         */
        public static final String PRIORITY_LEVEL_NOT_VALID = "DEPLOY-056";

        /**
         * Plan not found error code
         */
        public static final String PLAN_NOT_FOUND = "DEPLOY-058";

        /**
         * Invalid date format
         */
        public static final String INVALID_DATE_FORMAT = "DEPLOY-059";

        /**
         * Product doens not have a Jira Project key
         */
        public static final String NO_SUCH_JIRA_PROJECT_KEY = "DEPLOY-060";

        /**
         * The plan is deployed or ready to deploy
         */
        public static final String TRIED_TO_ARCHIVE_PLAN = "DEPLOY-062";

        /**
         * Plan expected deployment date is after current date
         */
        public static final String PLAN_DATE_DENIED = "DEPLOY-064";

        /**
         * Error code for trying to promote a plan where services are not be executed once
         */
        public static final String SERVICES_NOT_STARTED = "DEPLOY-065";

        /**
         * Subsystem not found error code
         */
        public static final String SUBSYSTEM_NOT_FOUND = "DEPLOY-066";

        /**
         * The context params for the batch scheduler service have not been created error code
         */
        public static final String DEPLOYMENT_CONTEXT_PARAMS_ERROR = "DEPLOY-067";

        /**
         * The deployment plan for batch scheduler service have not been created
         */
        public static final String DEPLOYMENT_BATCH_SCHEDULE_ERROR = "DEPLOY-068";

        /**
         * The deployment plan for batch scheduler service have not been created ERROR CODE
         */
        public static final String UPDATE_DEPLOYMENT_BATCH_SCHEDULE_ERROR = "DEPLOY-069";

        /**
         * The remove of deployment plan for batch scheduler service have failed ERROR CODE
         */
        public static final String REMOVE_DEPLOYMENT_BATCH_SCHEDULE_ERROR = "DEPLOY-070";

        /**
         * The remove of deployment context params of the plan have failed ERROR CODE
         */
        public static final String REMOVE_DEPLOYMENT_CONTEXT_PARAMS_ERROR = "DEPLOY-071";

        /**
         * The copy of deployment context params of the plan have failed ERROR CODE
         */
        public static final String COPY_DEPLOYMENT_CONTEXT_PARAMS_ERROR = "DEPLOY-072";

        /**
         * Error from ApiGateway service when generating the docker key
         */
        public static final String ERROR_GENERATING_DOCKER_KEY = "DEPLOY-073";

        /**
         * Error code for trying to get a product from NOVA DB
         */
        public static final String NO_SUCH_PRODUCT = "DEPLOY-074";

        /**
         * This validates a future deployment date while scheduling, unlike PLAN_DATE_DENIED (064), that validates a non-future deployment date while deploying
         */
        public static final String PLAN_DATE_DENIED_FOR_SCHEDULE = "DEPLOY-077";

        /**
         * Nova Planned pass has not been set or is invalid
         */
        public static final String INVALID_NOVA_PLANNED_PASS = "DEPLOY-078";

        public static final String GET_REPORT_DEPLOYMENT_MANAGER_ERROR = "DEPLOY-079";

        /**
         * Container not found error code
         */
        public static final String NO_SUCH_CONTAINER = "DEPLOY-081";

        public static final String CREATE_PUBLICATION_ERROR = "DEPLOY-082";

        public static final String UPDATE_PUBLICATION_ERROR = "DEPLOY-083";

        public static final String REMOVE_PUBLICATION_ERROR = "DEPLOY-084";

        public static final String CREATE_PROFILING_ERROR = "DEPLOY-085";

        public static final String REMOVE_PROFILING_ERROR = "DEPLOY-086";

        /**
         * Destination platform with invalid status
         */
        public static final String DESTINATION_PLATFORM_KO = "DEPLOY-087";

        /**
         * Database bad grammar error code
         */
        public static final String DB_BAD_GRAMMAR = "DEPLOY-088";

        /**
         * Search text too long error code
         */
        public static final String SEARCH_TEXT_TOO_LONG = "DEPLOY-089";

        /**
         * Invalid search error code
         */
        public static final String INVALID_SEARCH = "DEPLOY-090";

        /**
         * Task id is null error code
         */
        public static final String TASK_ID_IS_NULL = "DEPLOY-091";

        /**
         * Forced plan false
         */
        public static final String FORCE_REPLACE_PLAN = "DEPLOY-092";

        /**
         * Task id is null error code
         */
        public static final String INVALID_DEPLOYMENT_STATUS = "DEPLOY-095";

        /**
         * Promote plan error code
         */
        public static final String PROMOTE_PLAN_ERROR = "DEPLOY-096";

        /**
         * User without permission level
         */
        public static final String USER_PERMISSION_DENIED = "USER-002";

        /**
         * Filesystem not found
         */
        public static final String FILESYSTEM_NOT_FOUND = "DEPLOY-097";

        /**
         * Duplicated volume binds
         */
        public static final String DUPLICATED_VOLUME_BINDS = "DEPLOY-098";

        /**
         * Container not found error code
         */
        public static final String REPLACE_PLAN_ERROR = "DEPLOY-099";

        /**
         * Plan not found error code
         */
        public static final String DEPLOYMENT_PLAN_DEPLOYED_NOT_FOUND = "DEPLOY-100";

        /**
         * Subsystem not found error code
         */
        public static final String DEPLOYMENT_INSTANCE_NOT_FOUND = "DEPLOY-101";

        /**
         * Subsystem not found error code
         */
        public static final String SERVICE_TYPE_NOT_ALLOWED = "DEPLOY-102";

        /**
         * NovaError for trying to copy plan in pro when release platform has changed
         */
        public static final String PLATFORM_CONFLICT = "DEPLOY-103";

        /**
         * NovaError for trying to replace a plan that is not ready to be replaced
         */
        public static final String REPLACE_PLAN_CONFLICT_DEPLOYED_STATUS = "DEPLOY-104";

        /**
         * Disabled date for deployment error code
         */
        public static final String DISABLED_DEPLOYMENT_DATE = "DEPLOY-105";

        /**
         * deployment status error code
         */
        public static final String DEPLOYMENT_SUBSYSTEM_STATUS = "DEPLOY-106";

        /**
         * deployment status error code
         */
        public static final String DEPLOYMENT_SERVICE_STATUS = "DEPLOY-107";

        /**
         * deployment status error code
         */
        public static final String DEPLOYMENT_INSTANCE_STATUS = "DEPLOY-108";

        /**
         * Required resources for log/trace in ether could not be configured
         */
        public static final String CONFIGURE_RESOURCES_FOR_LOGGING_IN_ETHER = "DEPLOY-109";

        /**
         * Error getting disabled dates for deployment error code
         */
        public static final String ERROR_GETTING_DISABLED_DEPLOYMENT_DATES = "DEPLOY-110";

        /**
         * deployment ether status error code
         */
        public static final String DEPLOYMENT_ETHER_INSTANCE_STATUS = "DEPLOY-111";

        /**
         * deployment status error code
         */
        public static final String DEPLOYMENT_ETHER_SERVICE_STATUS = "DEPLOY-112";

        public static final String DEPLOYMENT_SERVICE_NOT_JDK_PARAMETERIZABLE = "DEPLOY-115";

        /**
         * Error code for trying to copy a plan which has some services not started
         */
        public static final String PENDING_TO_START_SERVICES_IN_COPY_OPERATION = "DEPLOY-118";

        /** Resources Have No Documentation Error **/
        public static final String RESOURCES_HAVE_NO_DOCUMENTATION = "DEPLOY-119";

        /**
         * Tried to delete deployment plan which has a child plan in process
         **/
        public static final String TRIED_TO_DELETE_PLAN_WITH_PROCESSING_CHILDREN = "DEPLOY-120";

        /**
         * Tried to copy a different RV into PRO without promoting it
         **/
        public static final String TRIED_TO_COPY_IN_PRO_WITHOUT_PROMOTING_FIRST = "DEPLOY-121";

        /**
         * NovaError for trying to replace a plan with itself
         */
        public static final String REPLACE_PLAN_CONFLICT_SAME_PLAN = "DEPLOY-122";

        /**
         * NovaError for trying to deploy a plan that is not in the expected status
         */
        public static final String REPLACE_PLAN_CONFLICT_DEPLOYING_STATUS = "DEPLOY-123";

        /**
         * Invalid instance number
         */
        public static final String INVALID_INSTANCE_NUMBER_ERROR_CODE = "DEPLOY-124";

        /**
         * Unexpected error calling scheduler manager for undeploying a plan that contais batch scheduler service
         */
        public static final String INVALID_UNDEPLOY_WITH_BATCH_SCHEDULER_SERVICES = "DEPLOY-125";

        public static final String NO_SAME_CPD_CONFIG_IN_PRO = "DEPLOY-126";

        /**
         * Impossible to promote to pro due to no exist broker with same name in the environment
         */
        public static final String IMPOSSIBLE_TO_PROMOTE_TO_PRO_DUE_TO_NO_SAME_BROKER = "DEPLOY-127";

        /**
         * NovaError for trying to deploy a plan from deployment manager
         */
        public static final String DEPLOY_PLAN_DEPLOYMENT_MANAGER_ERROR = "DEPLOY-128";

        /**
         * NovaError for trying to deploy a plan from deployment manager
         */
        public static final String PROMOTE_PLAN_DEPLOYMENT_DEVELOPMENT_EPHOENIX_NOT_PROMOTABLE = "DEPLOY-129";

        /**
         * NovaError for  choice types of services GroupingNames
         */
        public static final  String ERROR_CHOICE_TYPE_SERVICE_GROUPING_NAMES = "DEPLOY-130";

        /**
         * Error code for trying to get labels from a ephoenix service
         */
        public static final String NO_LABEL_EPHOENIX_SERVICE_ERROR = "DEPLOY-131";
    }
}
