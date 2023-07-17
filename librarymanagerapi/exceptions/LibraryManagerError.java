package com.bbva.enoa.platformservices.coreservice.librarymanagerapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.util.Constants.LibraryError;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;


/**
 * LibraryManagerError class
 */
@ExposeErrorCodes
public class LibraryManagerError
{

    /**
     * Unexpected error
     * @return an exception by unexpected error
     */
    public static NovaError getUnexpectedError()
    {
        return new NovaError( LibraryError.CLASS_NAME,
                LibraryError.UNEXPECTED_ERROR ,
                LibraryError.UNEXPECTED_ERROR_MSG,
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * Unable to store requirement
     * @return a NovaError
     */
    public static NovaError getUnableStoreRequirementsError()
    {
        return new NovaError( LibraryError.CLASS_NAME,
                LibraryError.UNABLE_STORE_REQUIREMENTS,
                "Unable to store library requirements",
                LibraryError.REQUIREMENTS_ERROR_ACTION,
                HttpStatus.UNPROCESSABLE_ENTITY,
                ErrorMessageType.ERROR);
    }

    /**
     * Unable to remove requirement
     * @return a NovaError
     */
    public static NovaError getUnableRemoveRequirementsError()
    {
        return new NovaError( LibraryError.CLASS_NAME,
                LibraryError.UNABLE_REMOVE_REQUIREMENTS,
                "Unable to remove library requirements",
                LibraryError.REQUIREMENTS_ERROR_ACTION,
                HttpStatus.UNPROCESSABLE_ENTITY,
                ErrorMessageType.ERROR);
    }

    /**
     * Unable to get requirement
     * @return a NovaError
     */
    public static NovaError getUnableGetRequirementsError()
    {
        return new NovaError( LibraryError.CLASS_NAME,
                LibraryError.UNABLE_GET_REQUIREMENTS,
                "Unable to retrieve library requirements",
                LibraryError.REQUIREMENTS_ERROR_ACTION,
                HttpStatus.UNPROCESSABLE_ENTITY,
                ErrorMessageType.ERROR);
    }

    /**
     * Invalid use of a library into an Environment
     * @param environment Environment where libraries are not published yet
     * @return a NovaError
     */
    public static NovaError getInvalidUseOfLibraryInEnvironmentError(final String environment)
    {
        return new NovaError( LibraryError.CLASS_NAME,
                LibraryError.INVALID_USE_OF_LIBRARIES_IN_ENVIRONMENT,
                "Services using unpublished version of libraries in " + environment ,
                "Review version of published libraries in " + environment,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Unable to get libraries used for a service
     * @return a NovaError
     */
    public static NovaError getUnableGetLibrariesOfService()
    {
        return new NovaError( LibraryError.CLASS_NAME,
                LibraryError.UNABLE_GET_LIBRARIES_OF_SERVICE,
                "Unable to get libraries used by service",
                LibraryError.REVIEW_LIBRARIES_BY_SERVICE,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Unable to save libraries used for a service
     * @return a NovaError
     */
    public static NovaError getUnableSaveLibrariesOfService()
    {
        return new NovaError( LibraryError.CLASS_NAME,
                LibraryError.UNABLE_SAVE_LIBRARIES_OF_SERVICE,
                "Unable to save libraries used by service",
                LibraryError.REVIEW_LIBRARIES_BY_SERVICE,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Unable to update libraries used for a service
     * @return a NovaError
     */
    public static NovaError getUnableUpdateUsedLibrariesOfService()
    {
        return new NovaError( LibraryError.CLASS_NAME,
                LibraryError.UNABLE_SAVE_LIBRARIES_OF_SERVICE,
                "Unable to update libraries used by service",
                LibraryError.REVIEW_LIBRARIES_BY_SERVICE,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Unable to publish a library into an environment
     * @param environment environment where library is not able to be published
     * @return a NovaError
     */
    public static NovaError getUnablePublishOnEnvironment(final String environment)
    {
        return new NovaError( LibraryError.CLASS_NAME,
                LibraryError.UNABLE_PUBLISH_ON_ENVIRONMENT,
                "Unable to publish library in " + environment + "environment",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Unable to get services using a library
     * @return a NovaError
     */
    public static NovaError getUnableGetServicesUsingLibrary()
    {
        return new NovaError( LibraryError.CLASS_NAME,
                LibraryError.UNABLE_GET_SERVICES_USING_LIBRARY,
                "Unable to get services using library",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Unable to remove a library
     * @return a NovaError
     */
    public static NovaError getUnableRemoveLibrary()
    {
        return new NovaError( LibraryError.CLASS_NAME,
                LibraryError.UNABLE_REMOVE_LIBRARY,
                "Unable to remove library",
                "Library cannot be removed if there is any Service using it",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Unable to remove usage of libraries
     * @param environment environment
     * @return a NovaError
     */
    public static NovaError getUnableRemoveUsedLibraries(final String environment)
    {
        return new NovaError( LibraryError.CLASS_NAME,
                LibraryError.UNABLE_REMOVE_USAGES,
                "Unable to remove usage of libraries of a service in [" + environment + "] environment",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }

    /**
     * Unable to validate requirements
     * @return a NovaError
     */
    public static NovaError postUnableToValidateRequirements()
    {
        return new NovaError( LibraryError.CLASS_NAME,
                LibraryError.UNABLE_TO_VALIDATE_REQUIREMENTS,
                LibraryError.UNABLE_TO_VALIDATE_REQUIREMENTS_MSG,
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR);
    }
}
