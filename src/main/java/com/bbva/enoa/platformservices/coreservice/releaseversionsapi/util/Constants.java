package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Constants for Release Version API
 */
public final class Constants
{
    /**
     * Port type
     */
    public enum PORT_TYPE
    {
        TCP, UDP
    }

    /**
     * Property management
     */
    public enum PROPERTY_MANAGEMENT
    {
        SERVICE, LIBRARY, ENVIRONMENT, BROKER
    }

    /**
     * Property type
     */
    public enum PROPERTY_TYPE
    {
        STRING, JSON, XML
    }

    /**
     * Property scope
     * Only used on NOVA Libraries
     */
    public enum PROPERTY_SCOPE
    {
        GLOBAL, SERVICE
    }

    /**
     * Supported NOVA Library requirement names
     */
    public enum REQUIREMENT_NAME
    {
        CONNECTORS, CPU, FILE_SYSTEM, MEMORY, PRECONF, USES_C, PORT, NAMESPACE, PORT_RANGE
    }

    /**
     * Library build usage. It is used when service uses a library in the compilation step (when create a new release version).
     */
    public static final String LIBRARY_BUILD_USAGE = "BUILD";

    /**
     * Supported requirement names of type installation
     */
    public enum INSTALLATION_REQUIREMENT_NAME
    {
        PRECONF, USES_C, DOCKER_STEPS, PORT, SUPPORTED_JDK_VERSIONS, MINIMUM_LANGUAGE_VERSION, MAXIMUM_LANGUAGE_VERSION, PORT_RANGE
    }

    /**
     * Supported requirement names of type installation
     */
    public enum RESOURCE_REQUIREMENT_NAME
    {
        CONNECTORS, CPU, FILE_SYSTEM, MEMORY, INSTANCES_NUMBER, JVM, JVM_PARAMETER
    }

    /**
     * Supported NOVA Library requirement types
     */
    public enum REQUIREMENT_TYPE
    {
        INSTALLATION, RESOURCE, ENVIRONMENT_VARIABLE
    }


    /**
     * Constans of the Release versions API
     */
    public static final String RELEASE_VERSIONS_API_NAME = "ReleaseVersionsAPI";
    public static final String GROUP_ID_PREFIX = "com.bbva.";
    ///////////////////////////////////////// API METHODS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    /**
     * Constans of the Release versions API
     */
    public static final String SUBSYSTEM_BUILD_STATUS = "subsystemBuildStatus";
    /**
     * Constans of the Release versions API
     */
    public static final String RELEASE_VERSION_VALIDATOR = "ReleaseVersionValidatorImpl";
    ///////////////////////////////////////// API IMPLEMENTATIONS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    public static final String SUBSYSTEM_VALIDATOR = "SubsystemValidatorImpl";
    public static final String TAG_VALIDATOR = "TagValidatorImpl";
    public static final String RELEASE_VERSION_ENTITY_BUILDER_SERVICE = "ReleaseVersionEntityBuilderServiceImpl";

    public static final String RELEASE_VERSION_DTO_BUILDER_SERVICE = "ReleaseVersionDtoBuilderServiceImpl";
    public static final String VERSION_CONTROL_SYSTEM_CLIENT = "VersioncontrolsystemAPI Client";
    public static final String CONTINUOUS_INTEGRATION_CLIENT = "ContinuousIntegrationAPI Client";
    ///////////////////////////////////////// API CONSUMED \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    public static final String DOCKER_REGISTRY_CLIENT = "DockerRegistryClientImpl";
    public static final String LIBRARY_MANAGER_CLIENT = "LibraryManagerClientImpl";
    public static final String SCHEDULER_MANAGER_CLIENT = "SchedulerManagerClientImpl";
    public static final String RELEASE_VERSION_SERVICE = "ReleaseVersionServiceImpl";
    public static final String ARCHIVE_RELEASE_VERSION_SERVICE = "ArchiveReleaseVersionServiceImpl";
    public static final String ISSURE_TRACKER_SERVICE = "IssueTrackerServiceImpl";
    public static final String NEW_RELEASE_VERSION_BUILDER = "NewReleaseVersionDtoBuilderImpl";
    public static final String PROJECT_FILE_VALIDATOR = "ProjectFileValidatorImpl";
    public static final String RELEASE_VERSION_DTO_BUILDER = "ReleaseVersionDtoBuilderImpl";
    public static final String SUBSYSTEM_DTO_BUILDER = "SubsystemDtoBuilderImpl";

    public static final String API_GATEWAY_CLIENT = "ApiGatewayClientImpl";

    public static final String API_GATEWAY_SERVICE = "ApiGatewayServicesImpl";

    /////////////////////////////////////// Project File Validator \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Status name for jenkins job group when is success
     */
    public static final String SUCCESS = "SUCCESS";

    ///////////////////////////////// Release version Dto builder service \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    /* Active profile tag */
    public static final String ACTIVE_PROFILE = "${SPRING_PROFILES_ACTIVE:LOCAL}";

    public static final String VALIDATE_NON_EPHOENIX_POM = "validateNonEPhoenixPom";
    public static final String VALIDATE_APPLICATION = "validateApplication";
    /**
     * Artifact ID is duplicated
     */
    public static final String ARTIFACT_ID_NAME_ERROR = "__ARTIFACT__ID__NAME__ERROR__";
    public static final String SUPPORTED_VERSIONS = "supportedVersions";

    /**
     * Literal for pakcage.json import
     */
    public static final String DEPENDENCIES_NAME = "dependencies";
    public static final String DEV_DEPENDENCIES_NAME = "devDependencies";
    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String INVALID = "INVALID";
    /* Application yml filename */
    public static final String APPLICATION_YML = "application.yml";
    /* Server port tag */
    public static final String SERVER_PORT = "${SERVER_PORT:";
    /* NOVA port tag (services with security) */
    public static final String NOVA_PORT = "${NOVA_PORT:";
    /* Application local ymnl filename */
    public static final String APPLICATION_LOCAL_YML = "application-LOCAL.yml";

    /* Application tag */
    public static final String APPLICATION = "application";

    /* False */
    public static final String FALSE = "false";

    public static final String ENCRYPT = "encrypt";
    public static final String NO_UUAA_POM_MSG = "Error in pom.xml file: the property <uuaa.name> is missing.";
    /**
     * Template.yml path for service such as Ephoenix and Node
     */
    public static final String EPHOENIX_OR_NODE_TEMPLATE_YML_PATH = "/template.yml";
    /**
     * POM without UUAA.
     */
    public static final String NO_UUAA_CODE = "NO_UUAA";

    /**
     * nova.yml without UUAA.
     */
    public static final String NO_NOVA_UUAA_CODE = "NO_NOVA_UUAA";
    public static final String NO_NOVA_UUAA_POM_MSG = "Error in nova.yml file: the property <uuaa> is not set";

    /**
     * nova.yml without behavior tags to test
     */
    public static final String NO_BEHAVIOR_RELEASE_VERSION_CODE = "NO_NOVA_BEHAVIOR_RELEASE_VERSION";
    public static final String NO_BEHAVIOR_RELEASE_VERSION_MSG = "Error in nova.yml file: the property <behavior.releaseVersion> is not set";

    /**
     *
     */
    public static final String NO_MODULES = "NO_MODULES";

    public static final String MACHINES = "machines";
    /**
     * UUAA is not uuaaLength sized.
     */
    public static final String WRONG_SIZED_UUAA_CODE = "WRONG_SIZED_UUAA_CODE";

    public static final String WRONG_SIZED_UUAA_CODE_MSG = "Error in pom.xml file: the property <uuaa.name> length is not valid";
    /**
     * UUAA is not uuaaLength sized.
     */
    public static final String WRONG_SIZED_NOVA_UUAA_CODE = "WRONG_SIZED_NOVA_UUAA_CODE";
    public static final String WRONG_SIZED_NOVA_UUAA_CODE_MSG = "Error in nova.yml file: the property <service.uuaa> length is not valid";
    /**
     * There is no groupId.
     */
    public static final String NO_GROUP_ID_CODE = "NO_GROUP_ID";
    public static final String NO_GROUP_ID_MSG = "Error in pom.xml file: the property <groupId> is missing.";
    /**
     * groupId is not well formed.
     */
    public static final String INVALID_GROUP_ID_CODE = "INVALID_GROUP_ID";
    public static final String INVALID_GROUP_ID_MSG = "Error in pom.xml file: the property <groupId> contains spaces or special "
            + "invalid characters.";
    /**
     * Wrong template.
     */
    public static final String WRONG_TEMPLATE_CODE = "WRONG_TEMPLATE";
    public static final String WRONG_TEMPLATE_MSG = "Error in template.xml file: invalid format. Several yaml format error found.";
    /**
     * Services uses a not supported Node.js version.
     */
    public static final String NOT_SUPPORTED_NODE_VERSION = "NOT_SUPPORTED_NODE_VERSION";
    public static final String NOT_SUPPORTED_NODE_VERSION_MSG = "Error in pom.xml file: the property <node.version>  does not "
            + "match with the current NOVA NODE version. It must be greater or equal than [6] and less or equal than [8].";
    /**
     * Services uses a not supported Node.js version.
     */
    public static final String MISSING_NODE_VERSION = "MISSING_NODE_VERSION";
    public static final String MISSING_NODE_VERSION_MSG = "Error in pom.xml file: the property <node.version> is missing.";
    /**
     * Service has more application files than required
     */
    public static final String INVALID_APPLICATION_FILE = "INVALID_APPLICATION_FILE";
    public static final String INVALID_APPLICATION_FILE_MSG = "Error while validating properties files from the resources folder. There are more "
            + "application-[*].yml files than allowed or other not supported property files. NOVA just supports an application.yml file and application-LOCAL.yml file.";
    /**
     * Service does not have an application file
     */
    public static final String MISSING_APPLICATION_FILE = "MISSING_APPLICATION_FILE";
    public static final String MISSING_APPLICATION_FILE_MSG = "Error while validating application.yml file. The application.yml file is missing in "
            + "the resource folder.";
    /**
     * Service application.yml is not parseable
     */
    public static final String YAML_EXCEPTION = "YAML_EXCEPTION";
    public static final String YAML_EXCEPTION_MSG = "Error parsing application.yml file: ";

    /**
     * Service application.yml file does not have port value
     */
    public static final String MISSING_PORT = "INVALID_PORT";
    public static final String MISSING_SERVER_PORT_MSG = "Error in application.yml file. The property <server.port> is incorrect or missing. "
            + "The property must be set as: [server.port: ${SERVER_PORT:37000}]";
    public static final String MISSING_NOVA_PORT_MSG = "Error in application.yml file. The property <server.port> is incorrect or missing. "
            + "The property must be set as: [server.port: ${NOVA_PORT:37000}]";
    /**
     * Service bootstrap.yml file has an invalid active profile
     */
    public static final String INVALID_ACTIVE_PROFILE = "INVALID_ACTIVE_PROFILE";
    public static final String INVALID_ACTIVE_PROFILE_MSG = "Error in bootstrap.yml file. The property <spring.profiles.active> does "
            + "not have the appropriate name/value. NOVA only supports [LOCAL] for naming the active profile property.";

    /**
     * Service bootstrap.yml file has an invalid active profile
     */
    public static final String INVALID_BOOTSTRAP_FILE = "INVALID_BOOTSTRAP_FILE";
    /**
     * Bootstrap error message
     */
    public static final String INVALID_BOOTSTRAP_FILE_MSG = "Error in bootstrap.yml file. The file does not have the correct format." +
            " Maybe the file does not have the correct indentation, forbidden characters or extra lines. Please check it.";

    /**
     * Service application.yml restart property value is not valid
     */
    public static final String INVALID_PROPERTY_RESTART = "INVALID_PROPERTY_RESTART";
    public static final String INVALID_PROPERTY_RESTART_MSG = "Error in application.yml file. The property <endpoints.restart> is "
            + "enabled or is missing. NOVA does not support it, this property must be set as [false].";
    /**
     * Service application.yml shutdown property value is not valid
     */
    public static final String INVALID_PROPERTY_SHUTDOWN = "INVALID_PROPERTY_SHUTDOWN";
    public static final String INVALID_PROPERTY_SHUTDOWN_MSG = "Error in application.yml file. The property <endpoints.shutdown> is "
            + "enabled or is missing. NOVA does not support it, this property must be set as [false].";
    /**
     * Service application.yml logfile property value is not valid
     */
    public static final String INVALID_PROPERTY_LOGFILE = "INVALID_PROPERTY_LOGFILE";
    public static final String INVALID_PROPERTY_LOGFILE_MSG = "Error in application.yml file. The property <endpoints.logfile.enabled> is set"
            + "to [true] or is not defined. This property must be defined and set to [false] in the application.yml of the service.";
    /**
     * Service dockerfile has been edited
     */
    public static final String DOCKFILE_EDITED = "DOCKFILE_EDITED";
    public static final String DOCKFILE_EDITED_MSG = "Error while validating dockerfile. The Dockerfile cannot be edited. It is "
            + "recommended to generate the Dockerfile using the NOVA Plugin with a nova plugin version greater or equal than 4.0.0";
    /**
     * Service group id has an invalid value
     */
    public static final String INVALID_POM_GROUP_ID = "INVALID_POM_GROUP_ID";
    public static final String INVALID_POM_GROUP_ID_MSG = "Error in pom.xml file. The property <groupId> has an invalid value. This "
            + "property must follow the pattern: [com.bbva.uuaa]";
    /**
     * Service artifact id has an invalid value
     */
    public static final String INVALID_POM_ARTIFACT_ID = "INVALID_POM_ARTIFACT_ID";
    public static final String INVALID_POM_ARTIFACT_ID_MSG = "Error in pom.xml file. The property <artifactId> has an invalid value. This"
            + " property must follow the pattern: [^[a-zA-Z0-9]+$]";
    /**
     * Service version has an invalid value
     */
    public static final String INVALID_POM_VERSION = "INVALID_POM_VERSION";
    public static final String INVALID_POM_VERSION_MSG = "Error in pom.xml file. The property <version> has an invalid "
            + "value. This property must follow the pattern major.minor.fix with at most 3 digits for each section. For example 2.11.135 .";
    /**
     * Service packaging has an invalid value
     */
    public static final String INVALID_POM_PACKAGING = "INVALID_POM_PACKAGING";
    public static final String INVALID_POM_PACKAGING_MSG = "Error in pom.xml file. The property <packaging> has an "
            + "invalid value. This valued must be set as: [jar]";
    /**
     * Service parent version has an invalid value
     */
    public static final String INVALID_POM_PARENT_VERSION = "INVALID_POM_PARENT_VERSION";
    public static final String INVALID_POM_PARENT_VERSION_MSG = "Error in pom.xml file. The property <parent> has a invalid version value."
            + " The NOVA parent version must be greater than [1.1.0]";
    /**
     * Service nova version has an invalid value
     */
    public static final String INVALID_POM_NOVA_VERSION = "INVALID_POM_NOVA_VERSION";
    public static final String INVALID_POM_NOVA_VERSION_MSG = "Error in pom.xml file: the property <nova.version> doesn't use the "
            + "current NOVA version. It must be greater or equal than [3]";
    /**
     * Service final name has an invalid value
     */
    public static final String INVALID_POM_FINAL_NAME = "INVALID_POM_FINAL_NAME";
    public static final String INVALID_POM_FINAL_NAME_MSG = "Error in pom.xml file: the property <finalName> in the <build> section is missing.";
    /**
     * Service plugin nova starter version has an invalid value
     */
    public static final String INVALID_POM_PLUGIN_NOVA_STARTER_VERSION = "INVALID_POM_PLUGIN_NOVA_STARTER_VERSION";
    public static final String INVALID_POM_PLUGIN_NOVA_STARTER_VERSION_MSG = "Error in pom.xml file. The NOVA plugin has an invalid version value"
            + ". It must be greater or equal than [4.0.0].";
    /**
     * Services artifacts IDs are not unique
     */
    public static final String INVALID_POM_ARTIFACT_NOT_UNIQUE = "INVALID_POM_ARTIFACT_NOT_UNIQUE";
    public static final String INVALID_POM_ARTIFACT_NOT_UNIQUE_MSG = "Conflict in pom.xml file. The property <artifactId> is not "
            + "unique for all service subsystem. Review the artifactId from all NOVA projects and ensure this property is unique.";
    public static final String NO_MODULES_MSG = "Service modules are undefined, check the parent POM.xml or the package.json file";
    /**
     *
     */

    public static final String INVALID_SERVICE_TYPE = "INVALID_SERVICE_TYPE";
    public static final String INVALID_SERVICE_TYPE_MSG = "Error with service type: the service doesn't have a valid NOVA or EPHOENIX "
            + "type. Check if the service type is an old type like [NOVA], [NOVA BATCH], [NOVA SPRING BATCH], [DEPENDENCY], [THIN2], "
            + "[EPHOENIX BATCH], [EPHOENIX ONLINE], [NODE], [FRONTCAT] OR a new type like [API REST], [BATCH], [CDN] or [LIBRARY] with "
            + "the language corresponding [JAVA - SPRING BOOT], [JAVA - SPRING CLOUD TASK], [JAVA - SPRING BATCH], [ANGULAR THIN2], "
            + "[ANGULAR THIN3] or [JAVA - SPRING MVC] is missing or is correctly written";
    /**
     *
     */
    public static final String INVALID_SERVICE_SUBSYSTEM_TYPE = "INVALID_SERVICE_SUBSYSTEM_TYPE";
    public static final String INVALID_SERVICE_SUBSYSTEM_TYPE_MSG = "Error in nova.yml file: the service type is a valid one, but it "
            + "does NOT match the subsystem type. Service type must be a NOVA type for NOVA subsystems and a EPHOENIX type for"
            + " EPHOENIX subsystems";
    /**
     *
     */
    public static final String INVALID_DEPENDENCIES = "INVALID_DEPENDENCIES";
    public static final String INVALID_DEPENDENCIES_MSG = "Error in package.json file: the dependencies field of the package.json is "
            + "missing or empty";
    /**
     *
     */
    public static final String INVALID_PACKAGE_NAME = "INVALID_PACKAGE_NAME";
    public static final String INVALID_PACKAGE_NAME_MSG = "Error in package.json file: the package.json name field and nova.yml name "
            + "field must be equal";
    /**
     *
     */
    public static final String INVALID_PACKAGE_NULL_NAME = "INVALID_PACKAGE_NULL_NAME";
    public static final String INVALID_PACKAGE_NULL_NAME_MSG = "Error in package.json file: the package.json name field must be defined";
    /**
     *
     */
    public static final String NOT_FOUND_INIT_CONTEXT_PARAM = "NOT_FOUND_INIT_CONTEXT_PARAM";
    public static final String NOT_FOUND_INIT_CONTEXT_PARAM_MSG = "Error in scheduler.yml file: the related context params from INIT section have not been found into nova.yml context params section";
    /**
     *
     */
    public static final String NOT_FOUND_STEP_CONTEXT_PARAM = "NOT_FOUND_STEP_CONTEXT_PARAM";
    public static final String NOT_FOUND_STEP_CONTEXT_PARAM_MSG = "Error in scheduler.yml file: the related context params from STEP section have not been found into nova.yml context params section";
    /**
     *
     */
    public static final String INVALID_PACKAGE_VERSION = "INVALID_PACKAGE_VERSION";
    public static final String INVALID_PACKAGE_VERSION_MSG = "Error in package.json file: the package.json version field and nova.yml "
            + "version field must be equal";
    /**
     *
     */
    public static final String INVALID_PACKAGE_NULL_VERSION = "INVALID_PACKAGE_NULL_VERSION";

    public static final String INVALID_PACKAGE_NULL_VERSION_MSG = "Error in package.json file: the package.json version field must be "
            + "defined";
    /**
     *
     */
    public static final String PYTHON_FLASK_BASE_SERVICE_NAME = "nova-base-service";
    public static final String PYTHON_FLASK_BASE_SERVICE_REGEX = "nova-base-service==.*";
    public static final String INVALID_REQUIREMENTS_DEPENDENCIES = "INVALID_REQUIREMENTS_DEPENDENCIES";
    public static final String INVALID_API_REST_PYTHON_FLASK_REQUIREMENTS_DEPENDENCIES_MSG = "Error in requirements.in file: " + PYTHON_FLASK_BASE_SERVICE_NAME
            + " must be included as a dependency on NOVA Python Flask services";

    public static final String PYTHON_BATCH_BASE_SERVICE_NAME = "nova-base-batch";
    public static final String PYTHON_BATCH_BASE_SERVICE_REGEX = "nova-base-batch==.*";
    public static final String INVALID_BATCH_PYTHON_REQUIREMENTS_DEPENDENCIES_MSG = "Error in requirements.in file: " + PYTHON_BATCH_BASE_SERVICE_NAME
            + " must be included as a dependency on NOVA Python Flask services";

    public static final String NO_REQUIREMENTS_DEFINED = "NO_REQUIREMENTS_DEFINED";
    public static final String NO_REQUIREMENTS_DEFINED_MSG = "Missing requirements.in: python services must have a requirements.in file";
    /**
     *
     */
    public static final String NO_API_DEFINED = "NO_API_DEFINED";
    public static final String NO_API_DEFINED_MSG = "Error in nova.yml file: for API services, at least one swagger or asyncapi definition server must be "
            + "defined in nova.yml file";
    public static final String API_DEFINED_FORBIDDEN = "API_DEFINED_FORBIDDEN";
    public static final String API_DEFINED_FORBIDDEN_MSG = "Error in nova.yml file: for this service type, sync or BackToFrontAPIs are forbidden.";

    /**
     *
     */
    public static final String DAEMON_DEFINING_API = "DAEMON_DEFINING_API";
    public static final String DAEMON_DEFINING_API_MSG = "Error in nova.yml file: a service of type daemon/batch/dependency cannot define an api (Sync or BackToFront)";

    public static final String BATCH_CDN_DEFINING_BACKTOBACK_API = "BATCH_CDN_DEFINING_BACKTOBACK_API";
    public static final String BATCH_CDN_DEFINING_BACKTOBACK_API_MSG = "Error in nova.yml file: a service of type batch/CDN cannot define a BackToBack api";

    /**
     *
     */
    public static final String DEPENDENCY_CONSUMING_API = "DEPENDENCY_CONSUMING_API";
    public static final String DEPENDENCY_CONSUMING_API_MSG = "Error in nova.yml file: a service of type dependency cannot consume an api";

    /**
     *
     */
    public static final String INVALID_PORT_NAME = "INVALID_PORT_NAME";
    public static final String INVALID_PORT_NAME_MSG = "Error in nova.yml file: if a port is defined, the port name must be defined too.";
    /**
     *
     */
    public static final String INVALID_PORT_TYPE = "INVALID_PORT_TYPE";
    public static final String INVALID_PORT_TYPE_MSG = "Error in nova.yml file: if a port is defined, the port type must be defined as "
            + "[TCP] or [UDP]";
    /**
     *
     */
    public static final String INVALID_PORT_INSIDE_PORT = "INVALID_PORT_INSIDE_PORT";
    public static final String INVALID_PORT_INSIDE_PORT_MSG = "Error in nova.yml file: if a port is defined, the port inside port must be"
            + " defined too.";
    /**
     *
     */
    public static final String INVALID_PROPERTY_NAME = "INVALID_PROPERTY_NAME";
    public static final String INVALID_PROPERTY_NAME_MSG = "Error in nova.yml file: if a property is defined, the property name must be "
            + "defined too.";
    /**
     *
     */
    public static final String INVALID_CONTEXT_PARAM_NAME = "INVALID_CONTEXT_PARAM_NAME";
    public static final String INVALID_CONTEXT_PARAM_MSG = "Error in nova.yml file: if a context param is defined, the name must be "
            + "defined and is mandatory.";
    /**
     *
     */
    public static final String INVALID_PROPERTY_ENCRYPT = "INVALID_PROPERTY_ENCRYPT";
    public static final String INVALID_PROPERTY_ENCRYPT_MSG = "Error in nova.yml file: if a property is defined, the property encrypt "
            + "must be undefined (true) or defined with a valid value, [True] or [False].";

    /**
     *
     */
    public static final String INVALID_PROPERTY_DESCRIPTION = "INVALID_PROPERTY_DESCRIPTION";
    public static final String INVALID_PROPERTY_DESCRIPTION_MSG = "Error in nova.yml file: if a property is defined, the property description "
            + "must be defined or defined with a valid string value.";

    /**
     *
     */
    public static final String INVALID_CONTEXT_DEFAULT_VALUE = "INVALID_CONTEXT_DEFAULT_VALUE";
    public static final String INVALID_CONTEXT_DEFAULT_VALUE_MSG = "Error in nova.yml file: if a context param is defined, the default value must be established and "
            + "value must not be empty.";
    /**
     *
     */
    public static final String INVALID_CONTEXT_PARAM_DESCRIPTION = "INVALID_CONTEXT_PARAM_DESCRIPTION";
    public static final String INVALID_CONTEXT_PARAM_DESCRIPTION_MSG = "Error in nova.yml file: if a context param is defined, the context param description "
            + "must be defined with a value. Thi value could be empty.";
    /**
     *
     */
    public static final String INVALID_PROPERTY_MANAGEMENT = "INVALID_PROPERTY_MANAGEMENT";
    public static final String INVALID_PROPERTY_MANAGEMENT_MSG = "Error in nova.yml file: if a property is defined, the property "
            + "management must be defined as [SERVICE] or [ENVIRONMENT]";

    /**
     *
     */
    public static final String INVALID_LIBRARY_PROPERTY_MANAGEMENT = "INVALID_PROPERTY_MANAGEMENT";
    public static final String INVALID_LIBRARY_PROPERTY_MANAGEMENT_MSG = "Error in nova.yml file: if a property is defined, the property "
            + "management must be defined as [SERVICE], [LIBRARY] or [ENVIRONMENT]";

    /**
     *
     */
    public static final String INVALID_CONTEXT_PARAM_TYPE = "INVALID_CONTEXT_PARAM_TYPE";
    public static final String INVALID_CONTEXT_PARAM_TYPE_MSG = "Error in nova.yml file: if a context param is defined, the context param "
            + "type must be defined as [INTEGER], [STRING] or [BOOLEAN]";
    /**
     *
     */
    public static final String INVALID_PROPERTY_TYPE = "INVALID_PROPERTY_TYPE";
    /**
     *
     */
    public static final String INVALID_PORTS_BATCH_SCHEDULER = "INVALID_PORTS_BATCH_SCHEDULER";
    public static final String INVALID_PROPERTY_TYPE_MSG = "Error in nova.yml file: if a property is defined, the property "
            + "type must be defined as [STRING], [JSON] or [XML]";
    /**
     *
     */
    public static final String INVALID_PROPERTY_SCOPE = "INVALID_PROPERTY_SCOPE";
    public static final String INVALID_PROPERTY_SCOPE_MSG = "Error in nova.yml file: if a property is defined, the property "
            + "scope must be either [GLOBAL] or [SERVICE]";
    /**
     *
     */
    public static final String INVALID_REQUIREMENT_VALUE = "INVALID_REQUIREMENT_VALUE";
    public static final String INVALID_CONNECTORS_REQUIREMENT_VALUE_MSG = "Error in nova.yml file: if a connector requirement is defined, the valid values are: ";
    public static final String INVALID_CONNECTOR_REQUIREMENT_ACTUAL_VALUE_MSG = ". Actual invalid requirement connector value: ";
    /**
     *
     */
    public static final String INVALID_PORTS_THIN = "INVALID_PORTS_THIN";
    public static final String INVALID_PORTS_THIN_MSG = "Error in nova.yml file: node thin services cannot have ports defined";
    /**
     *
     */
    public static final String INVALID_BUILD_THIN = "INVALID_BUILD_THIN";
    public static final String INVALID_BUILD_THIN_MSG = "Error in nova.yml file: node thin services cannot have build defined";
    /**
     *
     */
    public static final String INVALID_MACHINES_THIN = "INVALID_MACHINES_THIN";
    public static final String INVALID_MACHINES_THIN_MSG = "Error in nova.yml file: node thin services cannot have machines defined";
    /**
     *
     */
    public static final String INVALID_DEPENDENCIES_THIN = "INVALID_DEPENDENCIES_THIN";
    public static final String INVALID_DEPENDENCIES_THIN_MSG = "Error in nova.yml file: node thin services cannot have dependencies "
            + "defined. Use package.json file to define the dependencies.";
    /**
     *
     */
    public static final String INVALID_PORTS_CELLS = "INVALID_PORTS_CELLS";
    public static final String INVALID_PORTS_CELLS_MSG = "Error in nova.yml file: cells services cannot have ports defined";
    public static final String INVALID_PORTS_BATCH_SCHEDULER_MSG = "Error in nova.yml file: batch scheduler services cannot have ports defined";
    /**
     *
     */
    public static final String INVALID_PROPERTIES_CELLS = "INVALID_PROPERTIES_CELLS";
    /**
     *
     */
    public static final String INVALID_PROPERTIES_BATCH_SCHEDULER = "INVALID_PROPERTIES_BATCH_SCHEDULER";
    public static final String INVALID_PROPERTIES_CELLS_MSG = "Error in nova.yml file: cells services cannot have properties defined";
    public static final String INVALID_PROPERTIES_BATCH_SCHEDULER_MSG = "Error in nova.yml file: batch scheduler services cannot have properties defined";
    /**
     *
     */
    public static final String INVALID_BUILD_CELLS = "INVALID_BUILD_CELLS";
    /**
     *
     */
    public static final String INVALID_BUILD_BATCH_SCHEDULER = "INVALID_BUILD_BATCH_SCHEDULER";
    public static final String INVALID_BUILD_CELLS_MSG = "Error in nova.yml file: cells services cannot have build defined";
    public static final String INVALID_BUILD_BATCH_SCHEDULER_MSG = "Error in nova.yml file: batch scheduler services cannot have build defined";
    /**
     *
     */
    public static final String INVALID_MACHINES_CELLS = "INVALID_MACHINES_CELLS";
    /**
     *
     */
    public static final String INVALID_MACHINES_BATCH_SCHEDULER = "INVALID_MACHINES_BATCH_SCHEDULER";
    public static final String INVALID_MACHINES_CELLS_MSG = "Error in nova.yml file: cells services cannot have machines defined";
    public static final String INVALID_MACHINES_BATCH_SCHEDULER_MSG = "Error in nova.yml file: batch scheduler services cannot have machines defined";
    /**
     *
     */
    public static final String INVALID_DEPENDENCIES_CELLS = "INVALID_DEPENDENCIES_CELLS";
    /**
     *
     */
    public static final String INVALID_DEPENDENCIES_BATCH_SCHEDULER = "INVALID_DEPENDENCIES_BATCH_SCHEDULER";
    public static final String INVALID_DEPENDENCIES_CELLS_MSG = "Error in nova.yml file: cells services cannot have dependencies defined.";
    public static final String INVALID_DEPENDENCIES_BATCH_SCHEDULER_MSG = "Error in nova.yml file: batch scheduler services cannot have dependencies defined.";
    /**
     *
     */
    public static final String INVALID_CONTEXT_PARAMS = "INVALID_CONTEXT_PARAMS";
    public static final String INVALID_CONTEXT_PARAMS_MSG = "Error in nova.yml file: context params are not allowed in this kind of service.";
    /**
     *
     */
    public static final String INVALID_EMPTY_CONTEXT_PARAMS = "INVALID_EMPTY_CONTEXT_PARAMS";
    public static final String INVALID_EMPTY_CONTEXT_PARAMS_MSG = "Error in nova.yml file: context params with at least trigger param are mandatory in this type of service.";
    /**
     *
     */
    public static final String INVALID_INPUT_PARAMS = "INVALID_INPUT_PARAMS";
    public static final String INVALID_INPUT_PARAMS_MSG = "Error in nova.yml file: input params are not allowed in this kind of service.";
    /**
     *
     */
    public static final String INVALID_OUTPUT_PARAMS = "INVALID_OUTPUT_PARAMS";
    public static final String INVALID_OUTPUT_PARAMS_MSG = "Error in nova.yml file: output params are not allowed in this kind of service.";
    /**
     *
     */
    public static final String INVALID_APPLICATION_NAME_CELLS = "INVALID_APPLICATION_NAME_CELLS";
    public static final String INVALID_APPLICATION_NAME_CELLS_MSG = "Error in nova.yml file: cells services must have a valid applicationName value.";
    /**
     *
     */
    public static final String INVALID_PROJECT_NAME_CELLS = "INVALID_PROJECT_NAME_CELLS";
    public static final String INVALID_PROJECT_NAME_CELLS_MSG = "Error in nova.yml file: cells services must have a valid projectName value.";

    /**
     * Scheduler yml file
     */
    public static final String SCHEDULER_YML = "scheduler.yml";

    public static final String INVALID_NOVA_VERSION = "INVALID_NOVA_VERSION";
    public static final String INVALID_NOVA_VERSION_MSG = "Error in nova.yml file: The property <version> has an invalid version value."
            + " The NOVA version must be greater or equal than [18.04]";
    /**
     *
     */
    public static final String INVALID_SERVICE_NAME_CODE = "INVALID_SERVICE_NAME_CODE";
    public static final String INVALID_SERVICE_NAME_MSG = "Error in pom.xml: Service name not found or null";
    /**
     *
     */
    public static final String INVALID_NOVA_SERVICE_NAME = "INVALID_NOVA_SERVICE_NAME";
    public static final String INVALID_NOVA_SERVICE_NAME_MSG = "Error in nova.yml file: The property <name> is invalid. This property "
            + "must follow the pattern: [^[a-zA-Z0-9]+$]\"";
    /**
     *
     */
    public static final String INVALID_JAVA_LANGUAGE_VERSION = "INVALID_JAVA_LANGUAGE_VERSION";
    public static final String INVALID_JAVA_LANGUAGE_VERSION_MSG = "Error in nova.yml file: The property <languageVersion> has an invalid"
            + "version value. The NOVA language version for JAVA must be greater or equal than [1.8]. Apply \"\" to the language version";
    /**
     *
     */
    public static final String INVALID_FRONTCAT_JAVA_LANGUAGE_VERSION = "INVALID_FRONTCAT JAVA_LANGUAGE_VERSION";
    public static final String INVALID_FRONTCAT_JAVA_LANGUAGE_VERSION_MSG = "Error in nova.yml file: The property <languageVersion> has an invalid "
            + "version value. The NOVA language version for JAVA must be greater or equal than [1.7]. Apply \"\" to the language version";
    /**
     *
     */
    public static final String INVALID_NODE_LANGUAGE_VERSION = "INVALID_NODE_LANGUAGE_VERSION";
    public static final String INVALID_NODE_LANGUAGE_VERSION_MSG = "Error in nova.yml file: The property <languageVersion> has an invalid"
            + "version value. The NOVA language version for NODE must be greater or equal than [8]. Apply \"\" to the language version";
    /**
     *
     */
    public static final String INVALID_ANGULAR_LANGUAGE_VERSION = "INVALID_ANGULAR_LANGUAGE_VERSION";
    public static final String INVALID_ANGULAR_LANGUAGE_VERSION_MSG = "Error in nova.yml file: The property <languageVersion> has an "
            + "invalid version value. The NOVA language version for Angular service must be latest";
    /**
     *
     */
    public static final String INVALID_PYTHON_LANGUAGE_VERSION = "INVALID_PYTHON_LANGUAGE_VERSION";
    public static final String INVALID_PYTHON_LANGUAGE_VERSION_MSG = "Error in nova.yml file: The property <languageVersion> has an invalid"
            + "version value. The NOVA language version for PYTHON must be greater or equal than [3.7]. Apply \"\" to the language version";
    /**
     *
     */
    public static final String INVALID_TEMPLATE_LANGUAGE_VERSION = "INVALID_TEMPLATE_LANGUAGE_VERSION";
    public static final String INVALID_TEMPLATE_LANGUAGE_VERSION_MSG = "Error in nova.yml file: The property <languageVersion> has an invalid"
            + "version value. The NOVA language version for TEMPLATE must be greater or equal than [0.1]. Apply \"\" to the language version";
    /**
     *
     */
    public static final String INVALID_LANGUAGE = "INVALID_LANGUAGE";
    public static final String INVALID_LANGUAGE_MSG = "Error in nova.yml file: The property <language> has an invalid value.The property "
            + "<language> must be defined as [JAVA - SPRING BOOT], [JAVA - SPRING CLOUD TASK], [JAVA - SPRING BATCH] or [ANGULAR THIN2]";
    /**
     *
     */
    public static final String INVALID_OUTPUT_DIRECTORY = "INVALID_OUTPUT_DIRECTORY";
    public static final String INVALID_OUTPUT_DIRECTORY_MSG = "Error in pom.xml file: the property   "
            + "                <configuration>\n" + "                    <outputDirectory>./dist</outputDirectory>\n"
            + "                </configuration>\n" + "            </plugin> must exist and have the shown value";
    public static final String DEPENDENCY_NOT_FOUND = "DEPENDENCY_NOT_FOUND ";
    public static final String DEPENDENCY_NOT_FOUND_MSG = "Error in dependencies.txt. Current repository does not contain the following dependency: ";
    /* Latest */
    public static final String LATEST = "LATEST";

    /**
     *
     */
    public static final String INVALID_LIBRARY_PROPERTY = "INVALID_LIBRARY_PROPERTY";
    public static final String INVALID_LIBRARY_PROPERTY_MSG = "Error in nova.yml file: if a property management is defined with [SERVICE] value, the scope "
            + "cannot be with [GLOBAL] value";

    /**
     *
     */
    public static final String INVALID_FRONTCAT_JUNCTION = "INVALID_FRONTCAT_JUNCTION";
    public static final String INVALID_FRONTCAT_JUNCTION_MSG = "Error in nova.yml file: Frontcat services must have junction name define.";


    /**
     *
     */
    public static final String INVALID_FRONTCAT_CONTEXTPATH = "INVALID_FRONTCAT_CONTEXTPATH";
    public static final String INVALID_FRONTCAT_CONTEXTPATH_MSG = "Error in nova.yml file: Frontcat services must have contextpath parameter defined.";

    /**
     * Service releaseVersion has an invalid value
     */
    public static final String INVALID_RELEASE_VERSION_FOR_BEHAVIOR_TEST = "INVALID_RELEASE_VERSION_FOR_BEHAVIOR_TEST";
    public static final String INVALID_RELEASE_VERSION_FOR_BEHAVIOR_TEST_MSG = "Error in nova.yml file: The release name version declared in the yml does not exist in the product";

    /**
     * Generic error message.
     */
    public static final String LOG_ERROR_MSG = "Service: [{}] has failed validation: [{}]";

    public static final String GET_FILE_FROM_PROJECT = "getFileFromProject";
    /**
     * Generic final name using the maven language
     */
    public static final String FINAL_NAME_WITH_MAVEN_VARIABLES = "${project.groupId}-${project.artifactId}";

    /**
     * Max service exceeded
     */
    public static final String MAX_SERVICES_EXCEEDED = "MAX_SERVICES_EXCEEDED";

    /**
     * wrong service version
     */
    public static final String WRONG_SERVICE_VERSION_CODE = "WRONG_SERVICE_VERSION";

    /**
     * Max repetition batch
     */
    public static final String INVALID_BATCH_REPEAT = "INVALID_BATCH_REPEAT";
    public static final String INVALID_BATCH_REPEAT_MSG = "Error in scheduler.yml file: batch with serviceName: '";


    ///////////////////////////////////// SUBSYSTEM DTO BUILDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    public static final String WRONG_SERVICE_VERSION_MSG = "Error in nova.yml: Current service version does not match with pom.xml";
    /**
     * wrong service name
     */
    public static final String WRONG_SERVICE_NAME_CODE = "WRONG_SERVICE_NAME";
    public static final String SCHEDULER_FILE_ERRORS = "SCHEDULER_FILE_ERRORS";
    public static final String WRONG_SERVICE_NAME_MSG = "Error in nova.yml: Current service name does not match with pom.xml";
    public static final String TREE = "/tree/";

    /**
     * wrong library name code
     */
    public static final String WRONG_LIBRARY_NAME_CODE = "WRONG_LIBRARY_NAME";
    /**
     * wrong library name message
     */
    public static final String WRONG_LIBRARY_NAME_MSG = "Error in pom.xml: Current library artifactId does not match with name from nova.yml";
    /**
     * wrong library version code
     */
    public static final String WRONG_LIBRARY_VERSION_CODE = "WRONG_LIBRARY_VERSION";
    /**
     * wrong library version message
     */
    public static final String WRONG_LIBRARY_VERSION_MSG = "Error in pom.xml: Current library version does not match with nova.yml";

    ///////////////////////////////////// PROJECT FILE READER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    public static final String BUILD = "build";
    public static final String PORTS = "ports";
    public static final String CONSUMED = "consumed";
    public static final String SERVED = "served";
    public static final String LIBRARIES = "libraries";
    public static final String FRONTCAT = "frontcat";
    public static final String CONTEXT_PARAMS = "contextParams";
    public static final String INPUT_PARAMS = "inputParams";
    public static final String OUTPUT_PARAMS = "outputParams";
    public static final String DEFINITION = "definition";
    public static final String REQUIREMENTS = "requirements";

    public static final String EXTERNAL = "consumedExternal";
    public static final String SUPPORTED_VERSION_API_NOT_FOUND = "SUPPORTED_VERSION_API_NOT_FOUND";

    ////////////////////////////////////// SERVICE DTO BUILDER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Service type.
     */
    public static final String UUAA = "uuaa.name";
    /**
     * Service type.
     */
    public static final String NOVA_TYPE = "nova.type";
    /**
     * Service type.
     */
    public static final String NOVA_VERSION = "nova.version";
    /*
     * Thin2 version.
     */
    public static final String THIN2_VERSION = "thin2.version";
    /*
     * Node.js version.
     */
    public static final String NODE_JS_VERSION = "nova.node.version";
    /**
     * Ephoenix version.
     */
    public static final String EPHOENIX_VERSION = "ephoenix.version";
    /**
     * Ephoenix instance uuaas.
     */
    public static final String EPHOENIX_INSTANCE_UUAAS = "ephoenix.instance.uuaas";
    /**
     * Ephoenix instance user.
     */
    public static final String EPHOENIX_INSTANCE_USER = "ephoenix.instance.user";
    /**
     * Ephoenix deployment.
     */
    public static final String EPHOENIX_DEPLOYMENT = "ephoenix.deployment.line";
    /**
     * Ephoenix instance port.
     */
    public static final String EPHOENIX_INSTANCE_PORT = "ephoenix.instance.port";
    /**
     * Ephoenix deployment environment.
     */
    public static final String EPHOENIX_DEVELOPMENT_ENVIRONMENT = "ephoenix.development.environment";
    /**
     * Ephoenix deployment environment promotion.
     */
    public static final String EPHOENIX_DEVELOPMENT_ENVIRONMENT_PROMOTION = "ephoenix.development.environment.promotion";

    public static final String TRUE = "true";

    public static final String INVALID_SERVICE_NAME = "Service with errors parsing pom.xml";
    public static final String SEPARATOR = "/";
    public static final String POM_XML = "pom.xml";
    public static final String NOVA_YML = "nova.yml";

    public static final String PYTHON_REQUIREMENTS = "requirements.in";

    /**
     * Param type (just for Batch scheduler or batch services)
     */
    public enum PARAM_TYPE
    {
        INTEGER, BOOLEAN, STRING;
    }

    ///////////////////////////////////// SUBSYSTEM VALIDATOR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    public static final String PACKAGE_JSON = "package.json";
    public static final String SCHUEDULER_YML = "scheduler.yml";
    public static final String MAX_SERVICES_EXCEEDED_MSG = "Tag has more than the allowed services.";
    /**
     * Service without tag
     */
    public static final String NO_SERVICES_IN_TAG = "NO_SERVICES_IN_TAG";
    public static final String NO_SERVICES_IN_TAG_MSG = "El tag no contiene ningún servicio.";

    /**
     * NOVA library subsystem that contains services other than libraries
     */
    public static final String WRONG_LIBRARY_SUBSYSTEM = "WRONG_LIBRARY_SUBSYSTEM";
    public static final String WRONG_LIBRARY_SUBSYSTEM_MSG = "A NOVA library subsystem can only contain NOVA library services.";

    /**
     * EPHOENIX subsystem
     */
    public static final String WRONG_EPHOENIX_SUBSYSTEM = "WRONG_EPHOENIX_SUBSYSTEM";
    public static final String WRONG_EPHOENIX_SUBSYSTEM_MSG = "Un subsistema ePhoenix únicamente puede contener servicios ePhoenix";

    /**
     * INVALID subsystem
     */
    public static final String WRONG_SUBSYSTEM = "WRONG_SUBSYSTEM_TYPE";
    public static final String WRONG_SUBSYSTEM_MSG = "El tipo de subsistema indicado es inválido.";

    /**
     * Name too long
     */
    public static final String WRONG_NAME = "WRONG_SUBSYSTEM_NAME";
    public static final String WRONG_NAME_MSG = "El nombre del servicio más el de release supera el límite de 32 caracteres. Este nombre de servicio no es compatible para ser desplegado en la plataforma ETHER. Por favor, modifique el nombre del servicio o release para que la suma de ambos no superen los 32 caracteres";

    /**
     * Development environment for ePhoenix
     */
    public static final String WARNING_EPHOENIX_DEVELOPMENT_ENVIRONMENT = "EPHOENIX_DEVELOPMENT_ENVIRONMENT";
    public static final String WARNING_EPHOENIX_DEVELOPMENT_ENVIRONMENT_MSG = "La release contiene un servicio de tipo ePhoenix para desplegar en desarrollo y que no podrá ser promocionado a preproducción o producción. Además el groupId ha sido modificado añadiendo \".des\"";

    /**
     * NOVA library subsystem that contains services other than libraries
     */
    public static final String WRONG_BEHAVIOR_SUBSYSTEM = "WRONG_BEHAVIOR_SUBSYSTEM";
    public static final String WRONG_BEHAVIOR_SUBSYSTEM_MSG = "A NOVA behavior subsystem can only contain NOVA behavior services.";

    ////////////////////////////////////////// Version Control System Client \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Generic tag error message.
     */
    public static final String TAG_LOG_ERROR_MSG = "Tag [{}] has failed validation: [{}]";
    public static final String VCS_ERROR_TAGS_MSG = "Received status: {} trying to request getTags() to VCS API";
    public static final String VCS_ERROR_PATHS_MSG = "Received status: {} trying to request getProjectsPathsFromRepoTag() to VCS API";
    public static final String VCS_ERROR_TREE_FILES_MSG = "Received status: {} trying to request getFilesFromTreeDirectory() to VCS API";
    public static final String VCS_ERROR_POM_MSG = "Received status: {} trying to request getPomFromProject() to VCS API";
    public static final String VCS_ERROR_NOVA_YML_MSG = "Received status: {} trying to request getNovaYmlFromProject() to VCS API";
    public static final String VCS_ERROR_SWAGGER_MSG = "Received status: {} trying to request getSwaggerFromProject() to VCS API";
    public static final String VCS_ERROR_PACKAGEJSON_MSG = "Received status: {} trying to request getPackageJsonFromProject() to VCS API";
    public static final String VCS_ERROR_BOOTSTRAP_MSG = "Received status: {} trying to request getBootstrapFromProject() to VCS API";
    public static final String VCS_ERROR_DOCKERFILE_MSG = "Received status: {} trying to request getDockerfileFromProject() to VCS API";
    public static final String VCS_ERROR_APPLICATION_MSG = "Received status: {} trying to request getApplicationFromProject() to VCS API";
    public static final String VCS_ERROR_TEMPLATE_MSG = "Received status: {} trying to request getTemplateFromProject() to VCS API";

    public static final String LOG_TEMPLATE = "[{}] -> [{}]: ";

    ///////////////////////////////////////////// LOG TEMPLATE \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    // Messages.
    public static final String CONTACT_NOVA = "Please, contact the NOVA Admin team";
    ////////////////////////////////////////////// RELEASE VERSION ERROR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    public static final String UNEXPECTED_ERROR_MSG = "Unexpected internal error";
    public static final String VCS_API_FAILED_MSG = "Communication with VCS API failed";
    public static final String VCS_API_FAILED_ACTION = "Check if VCS server is up and running";
    public static final String NO_SUCH_RELEASE_VERSION_ACTION = "The given release version couldn't be found.";
    public static final String NO_SUCH_RELEASE_VERSION_MSG = "Please, check if the release versions does exist on NOVA. It could have been deleted.";
    public static final String NO_SUCH_RELEASE_MSG = "The given release couldn't be found.";
    public static final String NO_SUCH_RELEASE_ACTION = "Check if the release does exist on NOVA.";
    public static final String DELETE_DEPLOYED_RELEASE_MSG = "Tried to delete a release version with deployments plan.";
    public static final String DELETE_DEPLOYED_RELEASE_ACTION = "Release versions have still deployment plans in some enviroment. It must not have any deployment plan in any status in any environment.";
    public static final String STORAGE_DEPLOYED_RELEASE_MSG = "Tried to store a non deployed release version.";
    public static final String STORAGE_DEPLOYED_RELEASE_ACTION = "Release versions must have been deployed at least once, or the deployed plans must be stored and"
            + " with no associated tasks in PENDING or PENDING_ERROR status.";
    public static final String RELEASE_STATUS_BUILDING_MSG = "Release Version Building, can not be Storage";

    ////////////////////////////////////////////// API ERROR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    public static final String RELEASE_STATUS_BUILDING_ACTION = "Release Version status Building, wait until compile";
    public static final String API_NOT_FOUND = "API_NOT_FOUND";
    public static final String API_NOT_FOUND_MSG = "An API could not be found, check the title and the version in the swagger info section";
    public static final String API_MODIFIED = "API_MODIFIED";
    public static final String API_MODIFIED_MSG = ": the API has been modified and does not match with the original swagger";
    public static final String API_NOT_REGISTERED = "API_NOT_REGISTERED";
    public static final String API_NOT_REGISTERED_MSG = ": the API has not been defined in NOVA. Not registered API.";
    public static final String API_PATH_NOT_FOUND = "API_PATH_NOT_FOUND";
    public static final String API_PATH_NOT_FOUND_MSG = " swagger file could not be found or it is not well formed. Check the " +
            "API's path on the nova.yaml and validate swagger file with NOVA CLI.";

    public static final String API_PATH_NOT_FOUND_ASYNC_MSG = "The Hash MD5 of the API is not found. Please, check that" +
            "the API is uploaded in the NOVA dashboard and it is not modified: ";

    public static final String ASYNCAPI_PATH_NOT_FOUND_MSG = " Asyncapi file could not be found or it is not well formed. Check the " +
            "API's path on the nova.yaml and validate swagger file with NOVA CLI.";
    public static final String API_IMPLEMENTED_TWICE = "API_IMPLEMENTED_TWICE";
    public static final String API_IMPLEMENTED_TWICE_MSG = ": is being implemented twice as a service. API implementation must be unique "
            + "for a release version.";
    public static final String API_SERVED_IN_DIFFERENT_LOCATIONS = "API_SERVED_IN_DIFFERENT_LOCATIONS";
    public static final String API_SERVED_IN_DIFFERENT_LOCATIONS_MSG = "This is not allowed. You can either upgrade api " +
            "version to match the service's major version or use this service in another release.";
    public static final String API_FROM_ANOTHER_UUAA = "API_FROM_ANOTHER_UUAA";
    public static final String API_TAG_NOT_FOUND = "API_TAG_NOT_FOUND";
    public static final String API_TAG_NOT_FOUND_MSG = " some of the mandatory TAGs of the swagger: title, version or info, could not be found, check the title and the version in the swagger info section";
    public static final String API_IMPLEMENTED_TYPE_NOT_FOUND = "API_IMPLEMENTED_TYPE_NOT_FOUND";
    public static final String API_IMPLEMENTED_TYPE_NOT_FOUND_MSG = " the implemented type of the API does not exists";
    public static final String API_INCORRECT_POLICIES = "API_WRONG_POLICIES";
    public static final String API_INCORRECT_POLICIES_MSG = " has failed to check policies. Remember you must have a policies todo task" +
            " associated to the API and the last task must have status DONE";
    public static final String SUPPORTED_VERSION_API_NOT_FOUND_MSG = "The declared supported version of an API could not be found, review the supported version section";
    public static final String SUPPORTED_VERSION_GREATER_THAN_IMPLEMENTED_VERSION = "SUPPORTED_VERSION_GREATER_THAN_IMPLEMENTED_VERSION";
    public static final String SUPPORTED_VERSION_GREATER_THAN_IMPLEMENTED_VERSION_MSG = "The declared supported version of an API is greater than the served api";
    public static final String EXCEED_SUPPORTED_VERSION_API_NUMBER = "EXCEED_SUPPORTED_VERSION_API_NUMBER";
    public static final String EXCEED_SUPPORTED_VERSION_API_NUMBER_MSG = " declared supported versions, but the maximum of supported versions allowed are ";
    public static final Integer MAXIMUM_API_SUPPORTED_VERSIONS = 3;
    public static final String UNSUPPORTED_FORMAT_VERSION = "UNSUPPORTED_FORMAT_VERSION";
    public static final String UNSUPPORTED_FORMAT_VERSION_MSG = "The version provided for the service is invalid, not number or unsupported format. Please, review the number version and ensure that is a number";

    //////////////////////////////////////////// Threads execution CONSTANTS
    //////////////////////////////////////////// /////////////////////////////////////////
    public static final String PARALLEL_EXECUTION_VALIDATION_EXCEPTION = "PARALLEL_EXECUTION_VALIDATION_EXCEPTION";

    //////////////////////////////////////////// LibraryManager Client API CONSTANTS
    //////////////////////////////////////////// /////////////////////////////////////////

    public static final String GET_REQUIREMENTS = "getRequirements";
    public static final String GET_SERVICES_USING_LIBRARIES = "getServicesUsingLibrary";
    public static final String GET_REQUIREMENTS_BY_FULL_NAME = "getRequirementsByFullName";
    public static final String GET_LIBRARY_ENVIRONMENTS = "getLibraryEnvironments";
    public static final String STORE_REQUIREMENTS = "storeRequirements";
    public static final String REMOVE_REQUIREMENTS = "removeRequirements";

    ////////////////////////////////////////////// LIBRARY ERROR \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    /**
     * library error code : service type can not use nova library
     */
    public static final String LIBRARY_SERVICE_TYPE_NOT_ALLOWED_LIBS = "LIBRARY_SERVICE_TYPE_NOT_ALLOWED_LIBS";
    /**
     * library error code : library not exists
     */
    public static final String LIBRARY_NOT_EXISTS = "LIBRARY_NOT_EXISTS";
    /**
     * Library is no declared into descriptor file (pom.xml, package.json, ...)
     */
    public static final String LIBRARY_UNDECLARED_IN_DESCRIPTOR_FILE = "LIBRARY_UNDECLARED_IN_DESCRIPTOR_FILE";
    /**
     * Library has different version into descriptor file (pom.xml, package.json, ...)
     */
    public static final String LIBRARY_VERSION_NOT_MATCH_IN_DESCRIPTOR_FILE = "LIBRARY_VERSION_NOT_MATCH_IN_DESCRIPTOR_FILE";
    /**
     * library error code : unexpected error requesting a library
     */
    public static final String LIBRARY_UNEXPECTED_ERROR_RESPONSE = "LIBRARY_UNEXPECTED_ERROR_RESPONSE";
    public static final String WRONG_LIBRARY_REQUIREMENTS = "WRONG_LIBRARY_REQUIREMENTS";
    public static final String WRONG_LIBRARY_REQUIREMENTS_MSG = "Error in nova.yml: This version of the library already exists in NOVA with " +
            "different requirements. Please, upgrade library version.";
    public static final String WRONG_LIBRARY_PROPERTIES = "WRONG_LIBRARY_PROPERTIES";
    public static final String WRONG_LIBRARY_PROPERTIES_MSG = "Error in nova.yml: This version of the library already exists in NOVA with " +
            "different requirements. Please, upgrade library version.";
    public static final String SERVICE_VERSION_DUPLICATED = "SERVICE_VERSION_DUPLICATED";
    public static final String SERVICE_VERSION_DUPLICATED_MSG = "Exist the same service version on previous tag. This version will be omitted";

    public static final String NOVA_YML_OVERRIDE = "NOVA_YML_OVERRIDE";
    public static final String NOVA_YML_OVERRIDE_MSG = "Error in nova.yml: This version of the library already exists in NOVA and changes " +
            "have been identificated. Please, upgrade library version.";

    public static final String POM_XML_OVERRIDE = "POM_XML_OVERRIDE";
    public static final String POM_XML_OVERRIDE_MSG = "Error in pom.xml: This version of the library already exists in NOVA and changes " +
            "have been identificated. Please, upgrade library version.";

    public static final String INVALID_JDK_VERSION = "INVALID_JDK_VERSION";
    public static final String INVALID_JDK_VERSION_MSG = "Error in nova.yml file: The property: <jdkVersion> property" +
            " does not found. Please, check <jdkVersion> tag exist";

    public static final String INVALID_JVM_JDK_DUPLE = "INVALID_JVM_JDK_DUPLE";
    public static final String INVALID_JVM_JDK_DUPLE_MSG = "Error in nova.yml file: The given duple <languageVersion> and <jdkVersion> tags values is not " +
            "allowed for NOVA services. Please, check that given duple is properly configured or regenerate nova.yml using NOVA Cli. " +
            "Example of supported duple: <languageVersion> = [11.0.11] and <jdkVersion> = [zulu11.48.21]";

    public static final String INVALID_JVM_VERSION = "INVALID_JVM_VERSION";
    public static final String INVALID_JVM_VERSION_MSG = "Error in nova.yml file: The property: <languageVersion> property" +
            " does not found or is wrong formatted. Please, check <languageVersion> tag exist and check if the version matches with pattern <major.minor.fix>." +
            " Minimum language version value supported is: <1.8.121>";

    ////////////////////////////////////////////// API PERMISSIONS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    public static final String API_FROM_ANOTHER_UUAA_MSG = ": the api business-unit value does not match with your product uuaa";
    public static final String CREATE_RELEASE_VERSION_PERMISSION = "CREATE_RELEASE_VERSION";
    public static final String DELETE_RELEASE_VERSION_PERMISSION = "DELETE_RELEASE_VERSION";
    public static final String EDIT_RELEASE_VERSION_PERMISSION = "EDIT_RELEASE_VERSION";

    //////////////////////////////////////////// ApiGateway Client API CONSTANTS
    //////////////////////////////////////////// /////////////////////////////////////////

    public static final String ARCHIVE_RELEASE_VERSION_PERMISSION = "ARCHIVE_RELEASE_VERSION";

    public static final String CREATE_PUBLICATION = "createPublication";

    public static final String REMOVE_PUBLICATION = "removePublication";

    public static final String UPDATE_PUBLICATION = "updatePublication";

    public static final String GENERATE_DOCKER_KEY = "generateDockerKey";

    public static final String CREATE_REGISTER = "createRegister";

    public static final String REMOVE_REGISTER = "removeRegister";

    public static final String GET_POLICIES = "getPolicies";

    public static final String GET_ROLES = "getRoles";

    public static final String CREATE_PROFILING = "createProfiling";

    public static final String REMOVE_PROFILING = "removeProfiling";


    public static final String AUTHORIZATION_HEADER = "Authorization";


    public static final String BASIC = "Basic ";
    public static final String INVALID_UUAA_NAME = "UUAA_NOT_MATCH_PRODUCT";
    public static final String INVALID_UUAA_MSG = "Invalid nova.yml file: Specified UUAA does not match UUAA of associated Product";

    public static final String PREFIX_MULTIVALUED_ENVVARS = "NOVA_APPEND_";

    /**
     * Hidden constructor by super
     */
    private Constants()
    {
        super();
    }

    /**
     * Constant class with errors code for Release Version api
     */
    public static final class RVErrorsCodes
    {
        /**
         * Error code for unexpected error
         */
        public static final String UNEXPECTED_ERROR_CODE = "RELEASEVERSIONS-000";

        /**
         * Error code for duplicated name
         */
        public static final String RELEASE_VERSION_NAME_DUPLICATED_ERROR_CODE = "RELEASEVERSIONS-001";

        /**
         * Error code for null product subsystem id
         */
        public static final String NULL_PRODUCT_SUBSYSTEM_ID_ERROR_CODE = "RELEASEVERSIONS-002";

        /**
         * Error code by subsystem not found
         */
        public static final String PRODUCT_SUBSYSTEM_NOT_FOUND_ERROR_CODE = "RELEASEVERSIONS-003";

        /**
         * Error code by communication error with Version Control System
         */
        public static final String VCS_API_FAILED_ERROR_CODE = "RELEASEVERSIONS-004";

        /**
         * Error code by null status passes as argument
         */
        public static final String NULL_SUBSYSTEM_STATUS_ERROR_CODE = "RELEASEVERSIONS-006";

        /**
         * Error code by release version not found
         */
        public static final String NO_SUCH_RELEASE_VERSION_ERROR_CODE = "RELEASEVERSIONS-007";

        /**
         * Error code by Release not found
         */
        public static final String NO_SUCH_RELEASE_ERROR_CODE = "RELEASEVERSIONS-008";

        /**
         * Error code by forbidden action: delete Release Version with deployment plans
         */
        public static final String DELETE_DEPLOYED_RELEASE_ERROR_CODE = "RELEASEVERSIONS-009";

        /**
         * Error code by Duplicated Served API in a Release Version
         */
        public static final String DUPLICATED_API_ERROR_CODE = "RELEASEVERSIONS-011";

        /**
         * Error code by error returned by Version Control System
         */
        public static final String UNEXPECTED_ERROR_IN_VCS_REQUEST_ERROR_CODE = "RELEASEVERSIONS-012";

        /**
         * Error code by Release version without subsystem
         */
        public static final String RELEASE_WITH_NO_SUBSYSTEMS_ERROR = "RELEASEVERSIONS-015";

        /**
         * Error code by a UTF-8 not supported found when communication with Continuous integration Server
         */
        public static final String UTF8_NOT_SUPPORTED_ERROR_CODE = "RELEASEVERSIONS-017";

        /**
         * Error Code reading a file
         */
        public static final String FILE_READING_ERROR_CODE = "RELEASEVERSIONS-018";

        /**
         * Error Code by folder not found
         */
        public static final String FOLDER_NOT_FOUND_ERROR_CODE = "RELEASEVERSIONS-019";

        /**
         * Error Code by ephoenix service withour metadata
         */
        public static final String EPHOENIX_SERVICE_WITH_NO_METADATA_ERROR_CODE = "RELEASEVERSIONS-021";

        /**
         * Error Code by Subsystem not found
         */
        public static final String NO_SUCH_RELEASE_VERSION_SUBSYSTEM_ERROR_CODE = "RELEASEVERSIONS-022";

        /**
         * Error Code by forbidden action: store a release version not deployed
         */
        public static final String STORAGE_DEPLOYED_RELEASE_ERROR = "RELEASEVERSIONS-023";

        /**
         * Error Code by forbidden action: store a building release version
         */
        public static final String RELEASE_STATUS_BUILDING_ERROR = "RELEASEVERSIONS-024";


        /**
         * Error code by Limit of Release Versions reached
         */
        public static final String MAX_VERSIONS_LIMIT_ERROR_CODE = "RELEASEVERSIONS-025";


        /**
         * Error code by Project Key for JIRA not found
         */
        public static final String NO_SUCH_JIRA_PROJECT_KEY_ERROR_CODE = "RELEASEVERSIONS-026";

        /**
         * Error code by Max limit of Release Versions building (compiling) reached
         */
        public static final String MAX_VERSION_COMPILING_ERROR_CODE = "RELEASEVERSIONS-027";

        /**
         * Error Code by Dependency into nova.yml create a cycle
         */
        public static final String DEPENDENCY_GRAPH_CONTAINS_CYCLE_ERROR_CODE = "RELEASEVERSIONS-028";

        /**
         * Error Code by Dependency not found
         */
        public static final String DEPENDENCY_NOT_FOUND_ERROR_CODE = "RELEASEVERSIONS-029";

        /**
         * Error Code Dpublicated service name
         */
        public static final String DUPLICATED_SERVICE_NAME_ERROR_CODE = "RELEASEVERSIONS-030";


        /**
         * Error Code Scheduler.yml not found
         */
        public static final String NO_SUCH_SCHEDULER_YML_ERROR_CODE = "RELEASEVERSIONS-031";

        /**
         * Error Code batch service not found
         */
        public static final String BATCH_SERVICE_NOT_FOUND_ERROR_CODE = "RELEASEVERSIONS-032";

        /**
         * Error Code by Errors into Scheduler.yml
         */
        public static final String SCHEDULER_YML_ERROR_CODE = "RELEASEVERSIONS-033";

        /**
         * Error Code by batch schedule service do not save into DB
         */
        public static final String BATCH_SCHEDULER_SAVE_ERROR_CODE = "RELEASEVERSIONS-034";

        /**
         * Error Code by  batch schedule service do not delete from DB
         */
        public static final String BATCH_SCHELUDER_DELETE_ERROR_CODE = "RELEASEVERSIONS-035";

        /**
         * Error Code by Duplicated batch service in Batch scheduler
         */
        public static final String BATCH_SERVICE_DUPLICATED_ERROR_CODE = "RELEASEVERSIONS-038";

        /**
         * Error Code By forbidden action for user
         */
        public static final String FORBIDDEN_ERROR_CODE = "USER-002";

        /**
         * Error code by group id of a service not found in nova.yml or pom.xml
         */
        public static final String GROUP_ID_NOT_FOUND_ERROR_CODE = "RELEASEVERSIONS-039";

        /**
         * Error code by Artifact id not found in nova.yml or pom.xml
         */
        public static final String ARTIFACT_ID_NOT_FOUND_ERROR_CODE = "RELEASEVERSIONS-040";

        /**
         * Error code by version of a service not found in nova.yml or pom.xml
         */
        public static final String VERSION_NOT_FOUND_ERROR_CODE = "RELEASEVERSIONS-041";

        /**
         * Error code by release version name not found in nova.yml or pom.xml
         */
        public static final String RELEASE_NAME_NOT_FOUND_ERROR_CODE = "RELEASEVERSIONS-042";

        /**
         * Error code by service name not found in nova.yml or pom.xml
         */
        public static final String SERVICE_NAME_NOT_FOUND_ERROR_CODE = "RELEASEVERSIONS-043";

        /**
         * Error code by service type not found in nova.yml or pom.xml
         */
        public static final String SERVICE_TYPE_NOT_FOUND_ERROR_CODE = "RELEASEVERSIONS-044";

        /**
         * Error code by with a multitag over a wrong subsystem type (only library type is allowed)
         */
        public static final String MULTI_TAG_NOT_ALLOWED_ERROR_CODE = "RELEASEVERSIONS-045";

        /**
         * Error code by Release Version without subsystems
         */
        public static final String RELEASE_VERSION_WITHOUT_SUBSYSTEM = "RELEASEVERSIONS-046";

        /**
         * Error code by Subsystem without services
         */
        public static final String SUBSYSTEM_WITHOUT_SERVICES_ERROR_CODE = "RELEASEVERSIONS-047";

        /**
         * Error code by Invalid Options in JIRA request
         */
        public static final String JIRA_OPTIONS_ERROR_CODE = "RELEASEVERSIONS-048";
    }

    /**
     * Constants for dependency errors at package.json .
     */
    public static class PackageJsonDependencies
    {
        /**
         * Error code
         */
        public static final String INVALID_DEPENDENCY_VERSION = "INVALID_DEPENDENCY_VERSION";

        /**
         * Invalid message
         */
        public static final String INVALID_DEPENDENCY_MSG = "Invalid version for the dependency ";

        /**
         * Correction message
         */
        public static final String CORRECT_VERSION_MSG = " in the file package.json. Please, change the version to ";


        //                              ######### Dependency - versions List #########

        /**
         * Karma dependency name.
         */
        public static final String KARMA_NAME = "karma";

        /**
         * Karma version.
         */
        public static final String KARMA_205_VERSION = "2.0.5";

        /**
         * Map with dependencyName as a key and the correct version as a value.
         */
        public static Map<String, List<String>> DEPENDENCY_VERSION_MAP = new ConcurrentHashMap<>();

        static
        {
            DEPENDENCY_VERSION_MAP.put(KARMA_NAME, List.of(KARMA_205_VERSION));
        }

    }
}
