package com.bbva.enoa.platformservices.coreservice.docsystemsapi.exceptions;

import com.bbva.enoa.platformservices.coreservice.docsystemsapi.util.Constants.DocSystemsErrors;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import com.google.common.base.Strings;
import org.springframework.http.HttpStatus;

@ExposeErrorCodes
public class DocSystemErrorCode
{

    /**
     * Gets unexpected error
     *
     * @return the Nova Error instance
     */
    public static NovaError getUnexpectedError()
    {
        return getUnexpectedError(null);
    }

    /**
     * Gets unexpected error, adding some extra detail to it.
     *
     * @param errorDetail Detailed information about the error.
     * @return the Nova Error instance
     */
    public static NovaError getUnexpectedError(String errorDetail)
    {
        return new NovaError(
                DocSystemsErrors.DOC_SYSTEM_ERROR_CODE_CLASS_NAME,
                DocSystemsErrors.UNEXPECTED_ERROR_CODE,
                "Unexpected internal error" + (!Strings.isNullOrEmpty(errorDetail) ? ": " + errorDetail : ""),
                DocSystemsErrors.MSG_CONTACT_NOVA,
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorMessageType.FATAL);
    }

    /**
     * Gets no product error
     *
     * @return the Nova Error instance
     */
    public static NovaError getNoSuchProductError()
    {
        return new NovaError(
                DocSystemsErrors.DOC_SYSTEM_ERROR_CODE_CLASS_NAME,
                DocSystemsErrors.NO_SUCH_PRODUCT_ERROR_CODE,
                "The given product doesn't exist",
                "Check the product",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets no doc system error
     *
     * @param docSystemId The non-existing ID of the DocSystem
     * @return the Nova Error instance
     */
    public static NovaError getNoSuchDocSystemError(final Integer docSystemId)
    {
        return new NovaError(
                DocSystemsErrors.DOC_SYSTEM_ERROR_CODE_CLASS_NAME,
                DocSystemsErrors.NO_SUCH_DOC_SYSTEM_ERROR_CODE,
                String.format("There is no documentation system with ID [%s]", docSystemId),
                "Check the ID",
                HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    /**
     * Gets forbidden error
     *
     * @return the Nova Error instance
     */
    public static NovaError getForbiddenError()
    {
        return new NovaError(
                DocSystemsErrors.DOC_SYSTEM_ERROR_CODE_CLASS_NAME, DocSystemsErrors.FORBIDDEN_ERROR_CODE,
                "The current user does not have permission for this operation",
                "Use a user with higher privileges or request permission for your user",
                HttpStatus.FORBIDDEN,
                ErrorMessageType.ERROR);
    }

    /**
     * Gets repeated doc system error. A doc system is considered as repeated if there is already one with the same name, category and product.
     *
     * @param systemName       Doc system's name
     * @param documentCategory Doc system's category.
     * @param productId        Doc system's product ID.
     * @return The Nova Error instance.
     */
    public static NovaError getRepeatedDocSystemError(final String systemName, final String documentCategory, final Integer productId)
    {
        return new NovaError(
                DocSystemsErrors.DOC_SYSTEM_ERROR_CODE_CLASS_NAME, DocSystemsErrors.REPEATED_DOC_SYSTEM_ERROR_CODE,
                String.format("A document of type [%s] and name [%s] already exists for the product [%d]", documentCategory, systemName, productId),
                "Choose another name or verify the document type",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR
        );
    }

    /**
     * Get an error .
     *
     * @param fieldName  The name of the field.
     * @param fieldValue The value of the field.
     * @return NovaError
     */
    public static NovaError getInvalidFieldError(final String fieldName, final String fieldValue)
    {
        return new NovaError(
                DocSystemsErrors.DOC_SYSTEM_ERROR_CODE_CLASS_NAME,
                DocSystemsErrors.INVALID_FIELD_ERROR_CODE,
                String.format("Field [%s] cannot be [%s]", fieldName, fieldValue),
                "Please provide a valid value",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR
        );
    }

    /**
     * Get an error in case the name of the DocSystem cannot be used because it's a reserved one.
     *
     * @param docSystemName The invalid DocSystem's name
     * @return NovaError
     */
    public static NovaError getDocSystemNameIsReservedError(final String docSystemName)
    {
        return new NovaError(
                DocSystemsErrors.DOC_SYSTEM_ERROR_CODE_CLASS_NAME,
                DocSystemsErrors.DOC_SYSTEM_NAME_RESERVED_ERROR_CODE,
                String.format("Name [%s] cannot be used, because it's reserved", docSystemName),
                "Please use a different one",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR
        );
    }

    /**
     * Get an error in case a DocSystem is not allowed to be removed.
     *
     * @param docSystemId The ID of the DocSystem not allowed to be removed.
     * @param reason      Why the DocSystem is not allowed to be removed.
     * @return NovaError
     */
    public static NovaError getDocSystemRemovalNotAllowedError(final Integer docSystemId, final String reason)
    {
        return new NovaError(
                DocSystemsErrors.DOC_SYSTEM_ERROR_CODE_CLASS_NAME,
                DocSystemsErrors.DOC_SYSTEM_REMOVAL_NOT_ALLOWED_ERROR_CODE,
                String.format("Document [%d] is not allowed to be removed due to: %s", docSystemId, reason),
                DocSystemsErrors.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR
        );
    }

    /**
     * Get an error when an end-user tries to create or update a doc system with a category not allowed for end-users.
     *
     * @param documentCategory The category.
     * @return NovaError.
     */
    public static NovaError getDocSystemCategoryCreationOrUpdateNotAllowedForEndUsersError(String documentCategory)
    {
        return new NovaError(
                DocSystemsErrors.DOC_SYSTEM_ERROR_CODE_CLASS_NAME,
                DocSystemsErrors.DOC_SYSTEM_CATEGORY_CREATION_OR_UPDATE_NOT_ALLOWED_FOR_END_USERS_ERROR_CODE,
                String.format("Category [%s] not allowed", documentCategory),
                "Please use a different one",
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR
        );
    }

    /**
     * Get an error in case a DocSystem is not allowed to be updated.
     *
     * @param docSystemId The ID of the DocSystem not allowed to be updated.
     * @param reason      Why the DocSystem is not allowed to be updated.
     * @return NovaError
     */
    public static NovaError getDocSystemUpdateNotAllowedError(final Integer docSystemId, final String reason)
    {
        return new NovaError(
                DocSystemsErrors.DOC_SYSTEM_ERROR_CODE_CLASS_NAME,
                DocSystemsErrors.DOC_SYSTEM_UPDATE_NOT_ALLOWED_ERROR_CODE,
                String.format("Document [%d] is not allowed to be updated due to: %s", docSystemId, reason),
                DocSystemsErrors.MSG_CONTACT_NOVA,
                HttpStatus.CONFLICT,
                ErrorMessageType.ERROR
        );
    }

    /**
     * Gets no doc category error
     *
     * @param docSyCategory The non-existing category of DocSystem
     * @return the Nova Error instance
     */
    public static NovaError getNoSuchDocSystemError(final String docSyCategory)
    {
        return new NovaError(
                DocSystemsErrors.DOC_SYSTEM_ERROR_CODE_CLASS_NAME,
                DocSystemsErrors.NO_SUCH_DOC_SYSTEM_CATEGORY_ERROR_CODE,
                String.format("There is no documentation category with name [%s]", docSyCategory),
                "Check the name of category",
                HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    /**
     * Gets repeated doc system error. A doc system is considered as repeated if there is already one with the same url and category.
     *
     * @param url              Doc system's url
     * @param documentCategory Doc system's category.
     * @return The Nova Error instance.
     */
    public static NovaError getRepeatedFileDocSystemError(final String url, final String documentCategory)
    {
        return new NovaError(
                DocSystemsErrors.DOC_SYSTEM_ERROR_CODE_CLASS_NAME, DocSystemsErrors.REPEATED_URL_DOC_SYSTEM_ERROR_CODE,
                String.format("A document of type [%s] and url [%s] already exists", documentCategory, url),
                "Choose another document that has not been added",
                HttpStatus.CONFLICT,
                ErrorMessageType.WARNING
        );
    }
}
