package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

/**
 * Error code with all the internal errors for the service.
 * <p>
 *
 * @author BBVA
 */
@ExposeErrorCodes
public class ReleaseVersionError
{
    private static final String CLASS_NAME = "ReleaseVersionError";
    public static final String UNEXPECTED_ERROR_MSG = "Unexpected internal error";
    public static final String UNEXPECTED_ERROR_CODE = "RELEASEVERSIONS-000";

    private ReleaseVersionError()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static NovaError getUnexpectedError()
    {
        return new NovaError(CLASS_NAME, UNEXPECTED_ERROR_CODE, ReleaseVersionError.UNEXPECTED_ERROR_MSG, Constants.CONTACT_NOVA, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.FATAL);
    }

    public static NovaError getReleaseVersionNameDuplicatedError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-001", "There is a release version with the same name in the release", "Try with another release version name", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getNullProductSubsystemIdError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-002", "A null id of a product subsystem has been passed in creation of a Release version", Constants.CONTACT_NOVA, HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getProductSubsystemNotFoundError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-003", "Missed product subsystem in creation of a Release version", Constants.CONTACT_NOVA, HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getVCSApiFailedError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-004", Constants.VCS_API_FAILED_MSG, Constants.VCS_API_FAILED_ACTION, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }


    public static NovaError getNullSubsystemStatusError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-006", "A null status has been passed in call to subsystemBuildStatus", Constants.CONTACT_NOVA, HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getNoSuchReleaseVersionError(final Integer releaseVersionId)
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-007",
                Constants.NO_SUCH_RELEASE_VERSION_ACTION + " ID: [" + releaseVersionId + "]",
                Constants.NO_SUCH_RELEASE_VERSION_MSG,
                HttpStatus.NOT_FOUND,
                ErrorMessageType.WARNING);
    }

    public static NovaError getNoSuchReleaseError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-008", Constants.NO_SUCH_RELEASE_MSG, Constants.NO_SUCH_RELEASE_ACTION, HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getDeleteDeployedReleaseError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-009", Constants.DELETE_DEPLOYED_RELEASE_MSG, Constants.DELETE_DEPLOYED_RELEASE_ACTION, HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getDuplicatdApiError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-011", "Invalid release version. A served API is duplicated", "Please check the implemented APIs", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getUnexpectedErrorInVCSRequestError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-012", "Error returned by Version Control System Service when trying to get the template", "Check the VCS Service", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getReleaseWithNoSubsystemsError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-015", "Tried to add a relase version to a release of a product with no subsystems", "First of all add subsystems to the product", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getUTF8NotSupportedError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-017", "Found a UTF-8 not supported error when communicating with the CI server", "Check supported formats on the CI server", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getFileError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-018", "Error reading file", "Check dependencies file", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getFolderNotFoundError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-019", "Service folder not found", "Check dependencies file and service folders", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }


    public static NovaError getEphoenixServiceWithNoMetadataError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-021", "Tried to add a ephoenix service without ePhoenix metadata", "Check pom.xml of Service", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchReleaseVersionSubsystemError(final Integer subsystemId)
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-022",
                "Release Version Subsystem with id: [" + subsystemId + "] not found", "Could have been deleted the release version subsystem by the user before waiting to finished the Jenkins JOB related.",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.WARNING);
    }

    public static NovaError getStorageDeployedReleaseError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-023", Constants.STORAGE_DEPLOYED_RELEASE_MSG, Constants.STORAGE_DEPLOYED_RELEASE_ACTION, HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getReleaseStatusBuildingError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-024", Constants.RELEASE_STATUS_BUILDING_MSG, Constants.RELEASE_STATUS_BUILDING_ACTION, HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getMaxVersionsLimitError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-025", "The maximum limit of release versions has been reached", "Archive an old release version", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getNoSuchJiraProjectKeyError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-026", "The product does not have a Jira Project Key", "Contact to JIRA team for getting a Jira project key and set it into this NOVA product", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getMaxVersionCompilingError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-027", "The maximum limit of compiling versions has been reached", "Please wait till the compiling process ends", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getDependencyGraphContainsCycleError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-028", "The dependencies indicated in nova.yml files of one of the subsystems creates a cycle", "Review dependencies node on nova.yml the services in this subsystem to remove this cycle", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getDependencyNotFoundError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-029", "The indicated dependencies where not found in subsystem", "Review specified dependency names in the nova.yml", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getDuplicatedServiceNameError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-030", "Invalid release version. A service name is duplicated", "Please check the service names", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getNoSuchSchedulerYmlError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-031", "Scheduler.yml not found in the release version", "Review if the Release version service has been deleted", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getBatchServiceNotFoundError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-032", "The batch service name was not found in any service of the subsystem", "Review batch service names in your subsystem and create a new release version that includes these batch services names from scheduler.yml", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getSchedulerYmlError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-033", "The scheduler.yml of batch scheduler service contains some error", "Review the scheduler.yml file and try to create the release version again", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getBatchSchedulerSaveError(String errorDetails)
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-034", "An error occurred when saving batch schedule service" + (errorDetails != null ? ": " + errorDetails : ""), "Check the scheduler.yml and nova.yml files and try again", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getBatchScheluderDeleteError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-035", "The batch schedule service have not been deleted from BBDD", "Review the Scheduler Manager Service if is running", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.CRITICAL);
    }

    public static NovaError getBatchServiceDuplicatedError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-038", "There are duplicated batch service in the Batch scheduler", "Review the service name of the batch service or the name of the batch in the scheduler.yml", HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    public static NovaError getForbiddenError()
    {
        return new NovaError(CLASS_NAME, "USER-002", "The current user does not have permission for this operation", "Use a user with higher privileges or request permission for your user", HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    public static NovaError getGroupIdNotFoundError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-039", "The group id of the service does not found. Is null or empty.", "Review the nova.yml or pom.xml and set the group id or equivalent value.", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getArtifactIdNotFoundError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-040", "The artifact id of the service does not found. Is null or empty.", "Review the nova.yml or pom.xml and set the artifact id or equivalent value.", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }
    public static NovaError getVersionNotFoundError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-041", "The version of the service does not found. Is null or empty.", "Review the nova.yml or pom.xml and set the version or equivalent value.", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }
    public static NovaError getReleaseNameNotFoundError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-042", "The release version name of the service does not found. Is null or empty.", "Contact the Nova Admin team", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }
    public static NovaError getServiceNameNotFoundError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-043", "The service name of the service does not found. Is null or empty.", "Review the nova.yml or pom.xml and set the service name or equivalent value.", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getServiceTypeNotFoundError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-044", "The service type of the service does not found. Is null or empty.", "Review the nova.yml or pom.xml and set the service type or equivalent value.", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getMultitagNotAllowedError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-045", "Multitag not allowed for given subsystem type.", "Change your subsystem type to valid one and try again.", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getReleaseVersionWithoutSubsystem()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-046", "Release version without subsystem", "Review your release version or your subsystem and try again.", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }
    public static NovaError getSubsystemWithoutServicesError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-047", "empty subsystem selected to  empty", "Review subsystems selected into Release Version and try again.", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getJiraOptionsError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-048", "Invalid options in JIRA request", "Contact to JIRA team for review product UUAA existence. In case of the problem persist, contact with NOVA team", HttpStatus.FORBIDDEN, ErrorMessageType.WARNING);
    }

    public static NovaError getSubsystemTagWithoutServicesError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-049", "In the TAGs selected, there is not any NOVA project with NOVA services type in the subsystem",
                "Please, review the TAG of this subsystem and ensure that contains some project following the NOVA service conditions. You can use NOVA CLI tool for generating a NOVA Service/project scaffold." +
                        "You can find all info about generating NOVA Service into NOVA Site codelabs: https://platform.bbva.com/codelabs/nova/Codelabs#/", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getNoSuchProductError(String uuaa)
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-050", String.format("Product [%s] not found", uuaa), "Please usa a valid product", HttpStatus.NOT_FOUND, ErrorMessageType.FATAL);
    }

    public static NovaError getValidationUnexpectedError()
    {
        return new NovaError(CLASS_NAME, "RELEASEVERSIONS-051", "Unexpected error when validation nova.yml.",
                "Contact with Nova support.", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }
}
