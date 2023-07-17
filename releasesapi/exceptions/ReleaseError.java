package com.bbva.enoa.platformservices.coreservice.releasesapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseConstants;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

/**
 * Error code with all the internal errors for the service.
 */
@ExposeErrorCodes
public class ReleaseError
{

    public static String getClassName()
    {
        return ReleaseConstants.ReleaseErrors.CLASS_NAME;
    }

    /**
     * Unexpected error
     *
     * @return Nova Error
     */
    public static NovaError getUnexpectedError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_UNEXPECTED_INTERNAL_ERROR,
                ReleaseConstants.ReleaseErrors.MSG_UNEXPECTED_ERROR,
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * No Product
     *
     * @return Nova Error
     */
    public static NovaError getNoSuchProductError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_PRODUCT_DOESNT_EXIST,
                "The given product doesn't exist",
                "Check the product",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * No such Release
     *
     * @return Nova Error
     */
    public static NovaError getNoSuchReleaseError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_NO_SUCH_RELEASE,
                ReleaseConstants.ReleaseErrors.MSG_NO_SUCH_RELEASE,
                "Check if the release exists in NOVA DB",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * Tried to delete Release with ReleaseVersions
     *
     * @return Nova Error
     */
    public static NovaError getReleaseWithVersionsError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_REMOVE_RELEASE_WITH_VERSION,
                "Tried to remove a release with versions",
                ReleaseConstants.ReleaseErrors.MSG_RELEASE_WITH_VERSIONS,
                HttpStatus.UNPROCESSABLE_ENTITY,
                ErrorMessageType.ERROR);
    }

    /**
     * Tried to create Release with same name as otherç
     *
     * @return Nova Error
     */
    public static NovaError getReleaseNameDuplicatedError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_RELEASE_NAME_DUPLICATED,
                "There is already a release with the same name",
                ReleaseConstants.ReleaseErrors.MSG_RELEASE_NAME_DUPLICATED,
                HttpStatus.CONFLICT,
                ErrorMessageType.WARNING);
    }

    /**
     * Release name invalid
     *
     * @return Nova Error
     */
    public static NovaError getReleaseNameInvalidError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_SPECIAL_CHARACTERS,
                "Release name is invalid. Please, do not use special characters",
                "Check release name",
                HttpStatus.CONFLICT,
                ErrorMessageType.WARNING);
    }

    /**
     * Invalid date format
     *
     * @return Nova Error
     */
    public static NovaError getInvalidDateFormatError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_DATE_NOT_VALID,
                "Format date not valid",
                "Check the date format",
                HttpStatus.CONFLICT,
                ErrorMessageType.WARNING);
    }

    /**
     * PERMISSIONS
     *
     * @return Nova Error
     */
    public static NovaError getForbiddenError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_USER_WITHOUT_PERMISSIONS,
                "The current user does not have permission for this operation",
                "Use a user with higher privileges or request permission for your user",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Invalid Deployment Platform value
     *
     * @return the invalid deployment platform error
     */
    public static NovaError getInvalidDeploymentPlatform()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.INVALID_DEPLOYMENT_PLATFORM,
                "Deployment Platform value is invalid",
                "Configure a valid Deployment Platform:" +
                        PlatformUtils.getPlatformsValidToDeploy(),
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Invalid Deployment Logging value
     *
     * @return the invalid logging platform error
     */
    public static NovaError getInvalidLoggingPlatform()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.INVALID_LOGGING_PLATFORM,
                "Logging Platform value is invalid",
                "Configure a valid Logging Platform:" +
                        PlatformUtils.getPlatformsValidToLogging(),
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Default auto manage in PRE disabled
     *
     * @return Nova Error
     */
    public static NovaError getDefaultAutoManageInPreError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_DEFAULT_AUTOMANAGE_IN_PRE_DISABLED,
                "Default auto manage in PRE disabled",
                "Enable auto manage in PRE",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.WARNING);
    }

    /**
     * Default auto manage in PRO disabled
     *
     * @return Nova Error
     */
    public static NovaError getDefaultAutoManageInProError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_DEFAULT_AUTOMANAGE_IN_PRO_DISABLED,
                "Default auto manage in PRO disabled",
                "Enable auto manage in PRO",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Default auto deploy in PRE disabled
     *
     * @return Nova Error
     */
    public static NovaError getDefaultAutoDeployInPreError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_DEFAULT_AUTODEPLOY_IN_PRE_DISABLED,
                "Default auto deploy in PRE disabled",
                "Enable auto deploy in PRE",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Default auto deploy in PRO disabled
     *
     * @return Nova Error
     */
    public static NovaError getDefaultAutoDeployInProError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_DEFAULT_AUTODEPLOY_IN_PRO_DISABLED,
                "Default auto deploy in PRO disabled",
                "Enable auto deploy in PRO",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Deployment Type invalid
     *
     * @return Nova Error
     */
    public static NovaError getInvalidDeploymentTypeError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_INVALID_DEPLOYMENT_TYPE,
                "Deployment type cannot be null",
                "Check if deployment Type is a valid type (On demand, Nova planned or Planned)",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Environment invalid error
     *
     * @return Nova Error
     */
    public static NovaError getInvalidEnvironmentError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_INVALID_ENVIRONMENT_TYPE,
                "Invalid environment value",
                "Check if environment has a valid value (INT, PRE, PRO)",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Environment invalid error
     *
     * @param status   the status of the release version
     * @param statuses the statuses supported by release version status
     * @return Nova Error
     */
    public static NovaError getInvalidReleaseVersionStatus(final String status, final String statuses)
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.CODE_INVALID_RELEASE_STATUS_VALUE,
                "Invalid status value provided: [" + status + "] of the release version status filter",
                "The values supported are: [" + statuses + "]",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Not configured passed JDK for passed JVM error
     *
     * @param releaseVersionServiceId The release version id.
     * @param jvmVersion              The JVM version.
     * @param jdkVersion              The JDK version
     * @return Nova Error
     */
    public static NovaError getInvalidJvmJdkCombinationError(final int releaseVersionServiceId, final String jvmVersion, final String jdkVersion)
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME,
                ReleaseConstants.ReleaseErrors.INVALID_JVM_JDK_VERSIONS,
                "Release version service with id [" + releaseVersionServiceId + "] has no configured JDK [" + jdkVersion + "] for JVM [" + jvmVersion + "]",
                "Please, check given JDK and JVM duple is properly configurated in database",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * NovaError when trying to read JDK for services java greater or equal than 11, and no JDK is found
     *
     * @return a NovaError
     */
    public static NovaError getEmptyJdkError()
    {
        return new NovaError(ReleaseConstants.ReleaseErrors.CLASS_NAME, ReleaseConstants.ReleaseErrors.EMPTY_JDK,
                "No JDK version has been found in configuration.",
                "Check nova.yml file in your service and make sure that a valid jdkVersion property is set.",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }
}
