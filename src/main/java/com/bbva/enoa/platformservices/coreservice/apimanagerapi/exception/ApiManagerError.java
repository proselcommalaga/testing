package com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception;

import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants.ApiManagerErrors;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.exception.CommonError;
import com.bbva.enoa.utils.codegeneratorutils.annotations.ExposeErrorCodes;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

@ExposeErrorCodes
public class ApiManagerError
{
    private static final String CLASS_NAME = "ApiManagerError";

    public static String getClassName()
    {
        return CLASS_NAME;
    }

    public static NovaError getUnexpectedError()
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.UNEXPECTED_ERROR_CODE, ApiManagerErrors.UNEXPECTED_ERROR_MSG, Constants.MSG_CONTACT_NOVA, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.FATAL);
    }

    public static NovaError getProductNotFoundError(Integer productId)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.PRODUCT_NOT_FOUND_ERROR_CODE,
                MessageFormat.format("Product with ID {0} not found", productId), "Introduce a valid product ID", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getApiVersionNotFoundError(Integer apiVersionId)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_VERSION_NOT_FOUND_ERROR_CODE,
                MessageFormat.format("Api version with ID {0} not found", apiVersionId),
                "Introduce a valid api version ID", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getApiNotFoundError(Integer apiId)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_NOT_FOUND_ERROR_CODE,
                MessageFormat.format("Api with ID {0} not found", apiId),
                "Introduce a valid api ID", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getApiNotFoundError(final Integer productId, final String apiName, final String uuaa)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_NOT_FOUND_ERROR_CODE,
                MessageFormat.format("Api with for product {0}, apiName {1} and uuaa {2} not found", productId, apiName, uuaa),
                "Introduce a valid api", HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getTaskInfoError()
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.REQUEST_CREATION_POLICIES_PARAMETERS_ERROR,
                "The parameters for creating the to do task 'created policies' product id or nova api id is null",
                "Refresh the form and try to request this creation policies to do task again",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    public static NovaError getTaskInfoDocumentsNotProvidedError()
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.REQUEST_CREATION_POLICIES_DOCUMENTS_ARE_MANDATORY_ERROR,
                "Solutions Architect documentation and Security documentation are mandatory",
                "Please provide the required documentation",
                HttpStatus.BAD_REQUEST,
                ErrorMessageType.ERROR);
    }

    public static NovaError getInvalidApiForProductError(Integer productId, Integer apiId)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_NOT_BELONGS_TO_PRODUCT_ERROR_CODE,
                MessageFormat.format("The Api with ID {0} does not belong to the product with ID {1}", apiId, productId), "Introduce an api that exists in the product", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getInvalidNewApiError(String apiName, String version)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_ALREADY_EXISTS_ERROR_CODE,
                MessageFormat.format("The API {0} with version {1} is already registered", apiName, version), "Check the API's name and version", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getInvalidApiTypeError(final String apiName, final String oldApiType, final String newApiType)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_INVALID_TYPE_ERROR_CODE,
                MessageFormat.format("The API {0} already exists with type {1}. Cannot upload the same API with type {2}", apiName
                        , oldApiType, newApiType), MessageFormat.format("Upload the API with type {0}", oldApiType), HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getInvalidApiBasePathError(final String apiName, final String oldBasePath)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_INVALID_BASEPATH_ERROR_CODE,
                MessageFormat.format("The API {0} already exists with basepath {1}. Cannot change basepath due to security issues. Try uploading the API" +
                                "again with basepath {1} or rename the API", apiName
                        , oldBasePath), MessageFormat.format("Upload the API with basepath {0}", oldBasePath), HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getServiceNotFoundError(Integer serviceId)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.SERVICE_NOT_FOUND_ERROR_CODE,
                MessageFormat.format("Service with ID {0} not found", serviceId), "Introduce a valid service ID", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getSwaggerToJsonError(String uuaa, String apiName, String version)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.SWAGGER_DOWNLOAD_ERROR_CODE,
                MessageFormat.format("Error writing the stored Swagger for UUAA {0}" +
                        " with apiName {1} and version {2} into a file", uuaa, apiName, version), Constants.MSG_CONTACT_NOVA, HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getInvalidFileFormatError(String format)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.INVALID_FILE_FORMAT_ERROR_CODE,
                MessageFormat.format("The requested file format {0} is not supported", format), "Introduce a valid format", HttpStatus.BAD_REQUEST, ErrorMessageType.WARNING);
    }

    public static NovaError getCreateRegisterError(String uuaa, String basePath, String apiName)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.REGISTER_API_ERROR_CODE,
                MessageFormat.format("There was an error registering api {0} for uuaa {1} with base path {2}", apiName, uuaa, basePath),
                Constants.MSG_CONTACT_NOVA, HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getRemoveRegisterError(String uuaa, String basePath, String apiName)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.REMOVE_REGISTERED_API_ERROR_CODE,
                MessageFormat.format("There was an error removing the register of api {0} for uuaa {1} with base path {2}", apiName, uuaa, basePath),
                Constants.MSG_CONTACT_NOVA, HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getCreateApiTaskError(String uuaa, String basePath, String apiName)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.CREATE_POLICIES_TASK_ERROR_CODE,
                MessageFormat.format("There was an error creating policies task for api {0} for uuaa {1} with base path {2}", apiName, uuaa, basePath),
                Constants.MSG_CONTACT_NOVA, HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getRecoverApiTaskError(String uuaa, String basePath, String apiName)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.GET_POLICIES_TASK_ERROR_CODE,
                MessageFormat.format("There was an error recovering policies task for api {0} for uuaa {1} with base path {2}", apiName, uuaa, basePath),
                Constants.MSG_CONTACT_NOVA, HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getRecoverApiTaskListError()
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.GET_POLICIES_TASK_LIST_ERROR_CODE,
                "There was an error recovering list of policies task from Todo Task Service",
                Constants.MSG_CONTACT_NOVA, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getDeleteApiTaskListError(String uuaa, String basePath, String apiName)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.REMOVE_POLICIES_TASK_LIST_ERROR_CODE,
                MessageFormat.format("There was an error removing policies task for api {0} for uuaa {1} with base path {2}", apiName, uuaa, basePath),
                Constants.MSG_CONTACT_NOVA, HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getPlanApiDetailNotFoundError(Integer planId)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_DETAIL_PLAN_NOT_FOUND_ERROR_CODE,
                MessageFormat.format("There was an error recovering api detail for plan with id {0}. The deployment plan not found", planId),
                "This deployment plan has been moved or deleted. Please, refresh and update the page for loading the current deployment plan into the environment",
                HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getSavePlanProfilingNotFoundError(Integer planId)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.SAVE_PLAN_PROFILING_PLAN_NOT_FOUND_ERROR_CODE,
                MessageFormat.format("There was an error saving api profiling for plan with id {0}. The deployment plan not found", planId),
                "This deployment plan has been moved or deleted. Please, refresh and update the page for loading the current deployment plan into the environment",
                HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getPlanProfileNotFoundError(Integer planId)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.PLAN_PROFILE_NOT_FOUND_ERROR_CODE,
                MessageFormat.format("There was an error saving api profiling for plan with id {0}. Plan profile not found", planId),
                Constants.MSG_CONTACT_NOVA, HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getApiMethodNotFoundError(Integer planId, Integer methodId)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_METHOD_NOT_FOUND_ERROR_CODE,
                MessageFormat.format("There was an error saving api profiling for plan with id {0}. Nova api method with id {1} not found", planId, methodId),
                Constants.MSG_CONTACT_NOVA, HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getApiMethodProfileNotFoundError(Integer planId, Integer methodId)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_METHOD_PROFILE_NOT_FOUND_ERROR_CODE,
                MessageFormat.format("There was an error saving api profiling for plan with id {0}. Api method profile for nova api method with id {1} not found", planId, methodId),
                Constants.MSG_CONTACT_NOVA, HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getApiMethodProfileNotFoundError(Integer planProfileId, String apiEndpoint, String endpointVerb)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_METHOD_PROFILE_NOT_FOUND_ERROR_CODE,
                MessageFormat.format("The api method profile for plan profile {0} with endpoint {1} and verb {2} does not exists", planProfileId, apiEndpoint, endpointVerb),
                Constants.MSG_CONTACT_XMAS, HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getCesRoleNotFoundError(Integer planId, Integer roleId)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.CES_ROLE_NOT_FOUND_ERROR_CODE,
                MessageFormat.format("There was an error saving api profiling for plan with id {0}. Ces Role with id {1} not found", planId, roleId),
                Constants.MSG_CONTACT_NOVA, HttpStatus.NOT_FOUND, ErrorMessageType.ERROR);
    }

    public static NovaError getRolesFromCESError(String uuaa, String environment)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.GET_ROLES_FROM_CES_ERROR_CODE,
                MessageFormat.format("Error calling CES to recover roles from uuaa: {0} in environment {1}", uuaa, environment),
                Constants.MSG_CONTACT_NOVA, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getApiPoliciesFromXMASError(String uuaa, String basePath, String apiName)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.GET_API_POLICIES_FROM_XMAS_ERROR_CODE,
                MessageFormat.format("Error calling XMAS to recover policies for api {0} for uuaa {1} with basepath {2}", apiName, uuaa, basePath),
                Constants.MSG_CONTACT_NOVA, HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
    }

    public static NovaError getPoliciesTodoTaskPending(String uuaa, String apiName)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.POLICIES_TODO_TASK_PENDING,
                MessageFormat.format("A todo task already exists in pending state for the uuaa {0} and API {1}", uuaa, apiName),
                Constants.MSG_CONTACT_NOVA, HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    public static NovaError getApiMethodProfileRolesNotFound(Integer planProfileId, Integer apiMethodProfileId)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_METHOD_PROFILE_ROLES_NOT_FOUND_ERROR_CODE,
                MessageFormat.format("The api method profile with id {0} for plan profile {1} does not have any role association", planProfileId, apiMethodProfileId),
                Constants.MSG_CONTACT_NOVA, HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

    /**
     * Get an error when a DocSystem with the given parameters is not found.
     *
     * @param docSystemId       The given DocSystem's ID.
     * @param docSystemCategory The given DocSystem's category.
     * @param docSystemType     The given DocSystem's type.
     * @return The error.
     */
    public static NovaError getDocSystemNotFoundError(Integer docSystemId, DocumentCategory docSystemCategory, DocumentType docSystemType)
    {
        NovaError commonError = CommonError.getDocSystemNotFoundError(CLASS_NAME, docSystemId, docSystemCategory, docSystemType);
        commonError.setErrorCode(ApiManagerErrors.DOC_SYSTEM_NOT_FOUND_ERROR_CODE);
        return commonError;
    }

    /**
     * Get an error when api modality is invalid.
     *
     * @param rawApiModality         The given Api Scope
     * @param validApiModalityValues The valid Api Scope values.
     * @return The error.
     */
    public static NovaError getApiModalityError(final String rawApiModality, final String validApiModalityValues)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_MODALITY_ERROR_CODE,
                MessageFormat.format("The api modality [{0}] is not valid. Valid values are: [{1}]", rawApiModality, validApiModalityValues),
                Constants.MSG_CONTACT_NOVA, HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    /**
     * Get an error when api type is invalid.
     *
     * @param rawApiType         The given Api Scope
     * @param validApiTypeValues The valid Api Scope values.
     * @return The error.
     */
    public static NovaError getApiTypeError(final String rawApiType, final String validApiTypeValues)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_TYPE_ERROR_CODE,
                MessageFormat.format("The api type [{0}] is not valid. Valid values are: [{1}]", rawApiType, validApiTypeValues),
                Constants.MSG_CONTACT_NOVA, HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    /**
     * Get an error when task status is invalid.
     *
     * @param rawTaskStatus         The given task status
     * @param validTaskStatusValues The valid task status values.
     * @return The error.
     */
    public static NovaError getToDoTaskStatusError(final String rawTaskStatus, final String validTaskStatusValues)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.TASK_STATUS_ERROR_CODE,
                MessageFormat.format("The task status [{0}] is not valid. Valid values are: [{1}]", rawTaskStatus, validTaskStatusValues),
                Constants.MSG_CONTACT_NOVA, HttpStatus.BAD_REQUEST, ErrorMessageType.ERROR);
    }

    public static NovaError getForbiddenError()
    {
        return new NovaError(CLASS_NAME, "USER-002", "The current user does not have permission for this operation", "Use a user wiht higher privilegies or request permission for your user", HttpStatus.FORBIDDEN, ErrorMessageType.ERROR);
    }

    /**
     * Get an error when the operation has violated a database integrity constraint
     *
     * @param productId   Product ID
     * @param errorDetail Error cause detail
     * @return The error.
     */
    public static NovaError getDataBaseApiError(Integer productId, String errorDetail)
    {
        return new NovaError(CLASS_NAME, ApiManagerErrors.API_DATA_BASE_ERROR_CODE,
                MessageFormat.format("Error saving API information in database for the product {0}. The operation has violated a database integrity constraint: {1}", productId, errorDetail),
                Constants.MSG_CONTACT_NOVA, HttpStatus.CONFLICT, ErrorMessageType.ERROR);
    }

}

