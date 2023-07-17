package com.bbva.enoa.platformservices.coreservice.externaluserpermissionapi.exceptions;

import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.externaluserpermissionapi.util.ExternalUserConstants;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

@ExposeErrorCodes
public final class ExternalUserPermissionError
{
    private ExternalUserPermissionError()
    {
        // nothing to do
    }

    /**
     * Forbidden error
     *
     * @return Nova Error
     */
    public static NovaError getForbiddenError()
    {
        return new NovaError(ExternalUserConstants.Errors.CLASS_NAME,
                ExternalUserConstants.Errors.FORBIDDEN_ERROR,
                "This user does not have permission to perform this action",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.FORBIDDEN,
                ErrorMessageType.FATAL);
    }

    /**
     * Bad Request error
     *
     * @param property The property
     * @param value    The value
     * @return Nova Error
     */
    public static NovaError getBadRequestError(final String property, final String value)
    {
        return new NovaError(ExternalUserConstants.Errors.CLASS_NAME,
                ExternalUserConstants.Errors.BAD_REQUEST_ERROR,
                String.format("The field [%s] does not have the correct value [%s]", property, value),
                "Please, review every field",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.FATAL);
    }

    /**
     * Product not found error
     *
     * @param productId The product id
     * @return Nova Error
     */
    public static NovaError getProductNotFoundError(final Integer productId)
    {
        return new NovaError(ExternalUserConstants.Errors.CLASS_NAME,
                ExternalUserConstants.Errors.PRODUCT_NOT_FOUND_ERROR,
                String.format("The product id [%d] was not found in NOVA database", productId),
                "Review product id is correct",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }

    /**
     * Creating permission error
     *
     * @return Nova Error
     */
    public static NovaError getCreatingPermissionError()
    {
        return new NovaError(ExternalUserConstants.Errors.CLASS_NAME,
                ExternalUserConstants.Errors.CREATING_PERMISSION_ERROR,
                "There was an error creating the permission",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }

    /**
     * Deleting permission error
     *
     * @return Nova Error
     */
    public static NovaError getDeletingPermissionError()
    {
        return new NovaError(ExternalUserConstants.Errors.CLASS_NAME,
                ExternalUserConstants.Errors.DELETING_PERMISSION_ERROR,
                "There was an error deleting the permission",
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }

    /**
     * Platform not supported error
     *
     * @param platform the platform
     * @return Nova Error
     */
    public static NovaError getPlatformNotSupportedError(final Platform platform)
    {
        return new NovaError(ExternalUserConstants.Errors.CLASS_NAME,
                ExternalUserConstants.Errors.PLATFORM_NOT_SUPPORTED_ERROR,
                String.format("The platform [%s] is not supported", platform),
                Constants.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.ERROR);
    }

    /**
     * Permission not found error
     *
     * @param permissionId id of permission
     * @return Nova Error
     */
    public static NovaError getPermissionNotFoundError(final Integer permissionId)
    {
        return new NovaError(ExternalUserConstants.Errors.CLASS_NAME,
                ExternalUserConstants.Errors.PERMISSION_NOT_FOUND_ERROR,
                String.format("The permission id [%d] was not found in NOVA database", permissionId),
                "Review permission id is correct",
                HttpStatus.NOT_FOUND,
                ErrorMessageType.ERROR);
    }
}
