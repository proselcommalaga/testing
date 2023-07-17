package com.bbva.enoa.platformservices.coreservice.apimanagerapi.listener;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiErrorList;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiMethodProfileDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPlanDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiUploadRequestDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiVersionDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.PolicyTaskReplyParametersDTO;
import com.bbva.enoa.apirestgen.apimanagerapi.model.TaskInfoDto;
import com.bbva.enoa.apirestgen.apimanagerapi.server.spring.nova.rest.IRestListenerApimanagerapi;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.IApiManagerService;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants.ApiManagerErrors;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Api manager Service
 */
@Service
public class ListenerApimanager implements IRestListenerApimanagerapi
{

    /**
     * Physical connector service
     */
    private final IApiManagerService apiManagerService;

    /**
     * Dependency injection constructor
     *
     * @param apiManagerService api manager service
     */
    @Autowired
    public ListenerApimanager(final IApiManagerService apiManagerService)
    {
        this.apiManagerService = apiManagerService;
    }

    @Override
    @LogAndTrace(apiName = Constants.LISTENER_API_NAME, runtimeExceptionErrorCode = ApiManagerErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ApiDto[] getProductApis(NovaMetadata metadata, final Integer productId) throws Errors
    {
        return this.apiManagerService.getProductApis(productId);
    }

    @Override
    @LogAndTrace(apiName = Constants.LISTENER_API_NAME, runtimeExceptionErrorCode = ApiManagerErrors.UNEXPECTED_ERROR_CODE)
    public ApiErrorList uploadProductApis(final NovaMetadata novaMetadata, final ApiUploadRequestDto upload, final Integer productId) throws Errors
    {
        return this.apiManagerService.uploadProductApis(upload, productId);
    }

    @Override
    @LogAndTrace(apiName = Constants.LISTENER_API_NAME, runtimeExceptionErrorCode = ApiManagerErrors.UNEXPECTED_ERROR_CODE)
    public ApiErrorList deleteProductApi(final NovaMetadata novaMetadata, final Integer productId, final Integer apiVersionId) throws Errors
    {
        return this.apiManagerService.deleteProductApi(productId, apiVersionId);
    }

    @Override
    @LogAndTrace(apiName = Constants.LISTENER_API_NAME, runtimeExceptionErrorCode = ApiManagerErrors.UNEXPECTED_ERROR_CODE)
    public String[] getApiStatus(NovaMetadata novaMetadata) throws Errors
    {
        return this.apiManagerService.getApiStatus();
    }

    @Override
    @LogAndTrace(apiName = Constants.LISTENER_API_NAME, runtimeExceptionErrorCode = ApiManagerErrors.UNEXPECTED_ERROR_CODE)
    public byte[] downloadProductApi(final NovaMetadata novaMetadata, final String format, final Integer apiVersionId, final Integer productId, final String downloadType) throws Errors
    {
        return this.apiManagerService.downloadProductApi(productId, apiVersionId, format, downloadType);
    }

    @Override
    @LogAndTrace(apiName = Constants.LISTENER_API_NAME, runtimeExceptionErrorCode = ApiManagerErrors.UNEXPECTED_ERROR_CODE)
    public void onPolicyTaskReply(NovaMetadata novaMetadata, PolicyTaskReplyParametersDTO onPolicyTaskParameters) throws Errors
    {
        this.apiManagerService.onPolicyTaskReply(onPolicyTaskParameters);
    }

    @Override
    @LogAndTrace(apiName = Constants.LISTENER_API_NAME, runtimeExceptionErrorCode = ApiManagerErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public ApiPlanDetailDto getPlanApiDetailList(final NovaMetadata novaMetadata, final Integer planId) throws Errors
    {
        return this.apiManagerService.getPlanApiDetailList(planId);
    }

    @Override
    @LogAndTrace(apiName = Constants.LISTENER_API_NAME, runtimeExceptionErrorCode = ApiManagerErrors.UNEXPECTED_ERROR_CODE)
    public String exportProductApi(final NovaMetadata novaMetadata, final String format, final Integer apiVersionId, final Integer productId, final String downloadType) throws Errors
    {
        return new String(this.apiManagerService.downloadProductApi(productId, apiVersionId, format, downloadType));
    }

    @Override
    @LogAndTrace(apiName = Constants.LISTENER_API_NAME, runtimeExceptionErrorCode = ApiManagerErrors.UNEXPECTED_ERROR_CODE)
    public ApiDetailDto getApiDetail(final NovaMetadata novaMetadata, final Integer productId, final Integer apiId) throws Errors
    {
        return this.apiManagerService.getApiDetail(apiId, productId);
    }

    @Override
    @LogAndTrace(apiName = Constants.LISTENER_API_NAME, runtimeExceptionErrorCode = ApiManagerErrors.UNEXPECTED_ERROR_CODE)
    public ApiVersionDetailDto getApiVersionDetail(final NovaMetadata novaMetadata, final Integer productId, final Integer apiVersionId,final String filterByDeploymentStatus
    ) throws Errors
    {
        return this.apiManagerService.getApiVersionDetail(apiVersionId, productId,filterByDeploymentStatus);
    }

    @Override
    @LogAndTrace(apiName = Constants.LISTENER_API_NAME, runtimeExceptionErrorCode = ApiManagerErrors.UNEXPECTED_ERROR_CODE)
    public void createPolicyTask(final NovaMetadata metadata, final TaskInfoDto taskInfo) throws Errors
    {
        this.apiManagerService.createApiTask(taskInfo);
    }

    @Override
    @LogAndTrace(apiName = Constants.LISTENER_API_NAME, runtimeExceptionErrorCode = ApiManagerErrors.UNEXPECTED_ERROR_CODE)
    public String[] getApiTypes(NovaMetadata novaMetadata) throws Errors
    {
        return this.apiManagerService.getApiTypes();
    }

    @Override
    @LogAndTrace(apiName = Constants.LISTENER_API_NAME, runtimeExceptionErrorCode = ApiManagerErrors.UNEXPECTED_ERROR_CODE)
    public void savePlanApiProfile(final NovaMetadata novaMetadata, final ApiMethodProfileDto[] planProfileInfo, final Integer planId) throws Errors
    {
        this.apiManagerService.savePlanApiProfile(planProfileInfo, planId);
    }
}
