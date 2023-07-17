package com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal;

import com.asyncapi.v2.model.AsyncAPI;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMRegisterApiDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMRemoveApiDTO;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiErrorList;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiUploadRequestDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.TaskInfoDto;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskCreationDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskKeyDTO;
import com.bbva.enoa.core.novaheader.context.NovaContext;
import com.bbva.enoa.datamodel.model.api.entities.ApiMethod;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.*;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorService;
import com.bbva.enoa.datamodel.model.common.entities.LobFile;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.ApiManagerError;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.DefinitionFileException;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.ApiUtils;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.IApiManagerValidator;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal.IDefinitionFileValidatorModalityBased;
import com.bbva.enoa.platformservices.coreservice.common.model.NovaAsyncAPI;
import com.bbva.enoa.platformservices.coreservice.common.repositories.AsyncBackToFrontApiRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.Utils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IApiGatewayManagerClient;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AsyncBackToFrontApiManagerServiceModalityBasedImpl implements IApiManagerServiceModalityBased<AsyncBackToFrontApi, AsyncBackToFrontApiVersion, AsyncBackToFrontApiImplementation>
{
    private static final Logger LOG = LoggerFactory.getLogger(AsyncBackToFrontApiManagerServiceModalityBasedImpl.class);
    protected static final String ASYNC_BACKTOFRONT_BASEPATH = "novastomp";
    public static final String API_NAME_PARAM = "apiName";
    public static final String CONNECTOR_SDA_PARAM = "connectorSolutionsArchitectDocumentation";
    public static final String CONNECTOR_MSA_PARAM = "connectorSecurityDocumentation";
    public static final String BASE_PATH_PARAM = "basePath";
    public static final String TODO_TYPE_PARAM = "TodoTaskTypeCreated";
    public static final String TODO_ID_PARAM = "TodoTaskId";
    private final IApiGatewayManagerClient apiGatewayManagerClient;
    private final TodoTaskServiceClient todoTaskClient;
    private final INovaActivityEmitter novaActivityEmitter;
    private final NovaContext novaContext;
    private final AsyncBackToFrontApiRepository asyncBackToFrontApiRepository;
    private final IDefinitionFileValidatorModalityBased<NovaAsyncAPI> definitionFileValidator;
    private final IApiManagerValidator apiManagerValidator;
    private final SecurizableApiManagerCommonService securizableApiManagerCommonService;
    private final Utils utils;
    private final ApiUtils apiUtils;

    @Autowired
    public AsyncBackToFrontApiManagerServiceModalityBasedImpl(
            final IApiGatewayManagerClient apiGatewayManagerClient, final TodoTaskServiceClient todoTaskClient,
            final INovaActivityEmitter novaActivityEmitter, final NovaContext novaContext,
            final AsyncBackToFrontApiRepository asyncBackToFrontApiRepository,
            @Qualifier(value = "asyncBackToFrontDefinitionFileValidatorModalityBasedImpl") final IDefinitionFileValidatorModalityBased<NovaAsyncAPI> definitionFileValidator,
            final IApiManagerValidator apiManagerValidator,
            final SecurizableApiManagerCommonService securizableApiManagerCommonService,
            final Utils utils,
            final ApiUtils apiUtils)
    {
        this.apiGatewayManagerClient = apiGatewayManagerClient;
        this.todoTaskClient = todoTaskClient;
        this.novaActivityEmitter = novaActivityEmitter;
        this.novaContext = novaContext;
        this.asyncBackToFrontApiRepository = asyncBackToFrontApiRepository;
        this.definitionFileValidator = definitionFileValidator;
        this.apiManagerValidator = apiManagerValidator;
        this.securizableApiManagerCommonService = securizableApiManagerCommonService;
        this.utils = utils;
        this.apiUtils = apiUtils;
    }

    @Override
    @Transactional
    public ApiErrorList uploadApi(final ApiUploadRequestDto apiUploadRequest, final Integer productId)
    {
        Product product = this.apiManagerValidator.checkProductExistence(productId);

        if (!EnumUtils.isValidEnum(ApiType.class, apiUploadRequest.getApiType()))
        {
            final NovaError novaError = ApiManagerError.getApiTypeError(apiUploadRequest.getApiType(), Arrays.toString(ApiType.values()));
            throw new NovaException(novaError, novaError.toString());
        }

        ApiErrorList apiErrorList = new ApiErrorList();
        try
        {
            String decodedContent = this.apiUtils.decodeBase64(apiUploadRequest.getFile());
            ApiType apiType = ApiType.valueOf(apiUploadRequest.getApiType());
            NovaAsyncAPI asyncApi = this.definitionFileValidator.parseAndValidate(
                    decodedContent, product, ApiType.valueOf(apiUploadRequest.getApiType())
            );
            this.buildAndSave(asyncApi, decodedContent, product, apiType);
            apiErrorList.setErrorList(new String[0]);
        }
        catch (DefinitionFileException ex)
        {
            apiErrorList.setErrorList(ex.getErrorArray());
        }
        return apiErrorList;
    }

    /**
     * Builds a {@link AsyncBackToFrontApiVersion} and {@link AsyncBackToFrontApi} and stores it in database
     *
     * @param novaAsyncApi AsyncApi content
     * @param content      File content
     * @param product      Nova product
     * @param apiType      API type
     */
    private void buildAndSave(final NovaAsyncAPI novaAsyncApi, final String content, final Product product, final ApiType apiType)
    {
        AsyncBackToFrontApi api = new AsyncBackToFrontApi();
        AsyncBackToFrontApiVersion apiVersion = new AsyncBackToFrontApiVersion();
        AsyncAPI asyncAPI = novaAsyncApi.getAsyncAPI();
        String apiTitle = asyncAPI.getInfo().getTitle();
        String apiVersionPlain = asyncAPI.getInfo().getVersion();
        api.setBasePathSwagger(this.apiUtils.ensureStartingSlash(ASYNC_BACKTOFRONT_BASEPATH));
        api.setProduct(product);
        api.setName(apiTitle);

        if (apiType.isExternal())
        {
            api.setUuaa(novaAsyncApi.getXBusinessUnit().toUpperCase());
            apiVersion.setBasePathXmas(
                    this.apiUtils.buildXmasApiBasePath(
                            api.getUuaa(), apiTitle, apiVersionPlain
                    )
            );
        }
        else
        {
            api.setUuaa(product.getUuaa());
            apiVersion.setBasePathXmas(
                    this.apiUtils.buildXmasApiBasePath(
                            product.getUuaa(), apiTitle, apiVersionPlain
                    )
            );
        }

        // Setting policies status before trying to recover the stored sync api in order to override it in case the api already existed
        api.setPolicyStatus(ApiPolicyStatus.PENDING);
        api.setType(apiType);
        api = this.apiManagerValidator.findAndValidateOrPersistIfMissing(api);
        // Checks if the version is already stored before setting all its properties
        this.apiManagerValidator.assertVersionOfApiNotExists(api, apiVersionPlain);

        apiVersion.setDescription(asyncAPI.getInfo().getDescription());
        apiVersion.setVersion(apiVersionPlain);
        apiVersion.setApiState(ApiState.DEFINITION);

        // Replace all carrier return to avoid conflicts resolving md5
        String contentWithoutCarrierReturn = this.utils.unifyCRLF2LF(content);
        apiVersion.setDefinitionFile(new LobFile(apiTitle.trim() + ".yml", null, contentWithoutCarrierReturn));
        apiVersion.setDefinitionFileHash(this.utils.calculeMd5Hash(contentWithoutCarrierReturn.getBytes(StandardCharsets.UTF_8)));

        // todo@async: ver como sacar esto a estaticos
        List<ApiMethod> novaApiMethodList = new ArrayList<>();
        ApiMethod apiMethod = new ApiMethod();
        apiMethod.setEndpoint("/info");
        apiMethod.setDescription("Handshake endpoint");
        apiMethod.setVerb(Verb.GET);
        apiMethod.setApiVersion(apiVersion);
        novaApiMethodList.add(apiMethod);

        apiMethod = new ApiMethod();
        apiMethod.setEndpoint("/{id}/{subId}/eventsource");
        apiMethod.setDescription("SSE connection establishment endpoint");
        apiMethod.setVerb(Verb.GET);
        apiMethod.setApiVersion(apiVersion);
        novaApiMethodList.add(apiMethod);

        apiMethod = new ApiMethod();
        apiMethod.setEndpoint("/{id}/{subId}/xhr_send");
        apiMethod.setDescription("Info receiver endpoint");
        apiMethod.setVerb(Verb.POST);
        apiMethod.setApiVersion(apiVersion);
        novaApiMethodList.add(apiMethod);

        apiVersion.setApiMethods(novaApiMethodList);
        apiVersion.setApi(api);
        api.getApiVersions().add(apiVersion);

        switch (apiType)
        {
            case NOT_GOVERNED:
                // Emit Add Ungoverned Api Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(product.getId(), ActivityScope.UNGOVERNED_API, ActivityAction.ADDED)
                        .entityId(apiVersion.getId())
                        .addParam(API_NAME_PARAM, api.getName())
                        .build());
                break;
            case GOVERNED:
                // Emit Add Governed Api Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(product.getId(), ActivityScope.GOVERNED_API, ActivityAction.ADDED)
                        .entityId(apiVersion.getId())
                        .addParam(API_NAME_PARAM, api.getName())
                        .build());
                break;
            case EXTERNAL:
                // Emit Add Third Party Api Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(product.getId(), ActivityScope.THIRD_PARTY_API, ActivityAction.ADDED)
                        .entityId(apiVersion.getId())
                        .addParam(API_NAME_PARAM, api.getName())
                        .build());
                break;
            default:
                LOG.debug("Not supported Api Type: {}", apiType);
        }
    }

    @Override
    @Transactional
    public void createApiTask(final TaskInfoDto taskInfoDto, final AsyncBackToFrontApi api)
    {
        String ivUser = this.novaContext.getIvUser();
        final ApiTaskKeyDTO apiTaskKeyDTO = new ApiTaskKeyDTO();
        apiTaskKeyDTO.setUuaa(api.getUuaa());
        apiTaskKeyDTO.setApiName(api.getName());
        apiTaskKeyDTO.setBasePath(api.getBasePathSwagger());

        ApiTaskDTO apiTaskDTO = this.todoTaskClient.getApiTask(apiTaskKeyDTO);
        if (apiTaskDTO != null && (Constants.TODO_TASK_PENDING_STATUS.equals(apiTaskDTO.getStatus()) || Constants.TODO_TASK_PENDING_ERROR_STATUS.equals(apiTaskDTO.getStatus())))
        {
            LOG.error("[ApiManagerServiceImpl] -> [createApiTask]: A todo task exist with status [{}] for API with id [{}] ", apiTaskDTO.getStatus(), taskInfoDto.getNovaApiId());
            throw new NovaException(ApiManagerError.getPoliciesTodoTaskPending(api.getUuaa(), api.getName()), "A todo task already exists in pending state");
        }

        DocSystem araDocument = this.apiManagerValidator.validateAndGetDocument(taskInfoDto.getAraDocumentId(), DocumentCategory.ARA, DocumentType.FILE);
        DocSystem msaDocument = this.apiManagerValidator.validateAndGetDocument(taskInfoDto.getMsaDocumentId(), DocumentCategory.MSA, DocumentType.FILE);
        api.setAraDocument(araDocument);
        api.setMsaDocument(msaDocument);

        if (ApiType.EXTERNAL != api.getType())
        {
            this.registerApi(api);
        }

        ApiTaskCreationDTO apiTaskCreationDTO = new ApiTaskCreationDTO();
        apiTaskCreationDTO.setApiKey(apiTaskKeyDTO);
        apiTaskCreationDTO.setProductId(taskInfoDto.getProductId());
        apiTaskCreationDTO.setTaskType(ToDoTaskType.API_ESTABLISH_POLICIES.name());
        apiTaskCreationDTO.setUserCodeCreationTask(ivUser);
        apiTaskCreationDTO.setLinkARA(araDocument.getUrl());
        apiTaskCreationDTO.setLinkMSA(msaDocument.getUrl());
        apiTaskCreationDTO.setHasFrontConsumers(taskInfoDto.getHasFrontConsumers());
        apiTaskCreationDTO.setHasBackConsumers(taskInfoDto.getHasBackConsumers());

        Integer todoTaskId = this.todoTaskClient.createApiTask(apiTaskCreationDTO);

        api.setPolicyStatus(ApiPolicyStatus.PENDING);

        // Finally, create send request policies activities
        if (todoTaskId != Constants.TODO_TASK_ALREADY_CREATED_CODE)
        {

            switch (api.getType())
            {
                case NOT_GOVERNED:
                    // Emit Send Request Policies Request Ungoverned Api Activity
                    this.novaActivityEmitter.emitNewActivity(new GenericActivity
                            .Builder(api.getProduct().getId(), ActivityScope.UNGOVERNED_API, ActivityAction.SEND_REQUEST_POLICIES_REQUEST)
                            .entityId(api.getId())
                            .addParam(API_NAME_PARAM, api.getName())
                            .addParam(CONNECTOR_SDA_PARAM, msaDocument.getUrl())
                            .addParam(CONNECTOR_MSA_PARAM, araDocument.getUrl())
                            .addParam(BASE_PATH_PARAM, api.getBasePathSwagger())
                            .addParam(TODO_TYPE_PARAM, ToDoTaskType.API_ESTABLISH_POLICIES.name())
                            .addParam(TODO_ID_PARAM, todoTaskId)
                            .build());
                    break;
                case GOVERNED:
                    // Emit Send Request Policies Request Governed Api Activity
                    this.novaActivityEmitter.emitNewActivity(new GenericActivity
                            .Builder(api.getProduct().getId(), ActivityScope.GOVERNED_API, ActivityAction.SEND_REQUEST_POLICIES_REQUEST)
                            .entityId(api.getId())
                            .addParam(API_NAME_PARAM, api.getName())
                            .addParam(CONNECTOR_SDA_PARAM, msaDocument.getUrl())
                            .addParam(CONNECTOR_MSA_PARAM, araDocument.getUrl())
                            .addParam(BASE_PATH_PARAM, api.getBasePathSwagger())
                            .addParam(TODO_TYPE_PARAM, ToDoTaskType.API_ESTABLISH_POLICIES.name())
                            .addParam(TODO_ID_PARAM, todoTaskId)
                            .build());
                    break;
                case EXTERNAL:
                    // Emit Send Request Policies Request Third Party Api Activity
                    this.novaActivityEmitter.emitNewActivity(new GenericActivity
                            .Builder(api.getProduct().getId(), ActivityScope.THIRD_PARTY_API, ActivityAction.SEND_REQUEST_POLICIES_REQUEST)
                            .entityId(api.getId())
                            .addParam(API_NAME_PARAM, api.getName())
                            .addParam(CONNECTOR_SDA_PARAM, msaDocument.getUrl())
                            .addParam(CONNECTOR_MSA_PARAM, araDocument.getUrl())
                            .addParam(BASE_PATH_PARAM, api.getBasePathSwagger())
                            .addParam(TODO_TYPE_PARAM, ToDoTaskType.API_ESTABLISH_POLICIES.name())
                            .addParam(TODO_ID_PARAM, todoTaskId)
                            .build());
                    break;
                default:
                    LOG.debug("Not supported Api Type: {}", api.getType().getApiType());
            }
        }
        else
        {
            LOG.warn("[ApiManagerServiceImpl] -> [createApiTask]: the to do task is already created: [{}]. Activity will not created ", Constants.TODO_TASK_ALREADY_CREATED_CODE);
        }
    }

    @Override
    public void onPolicyTaskReply(final ToDoTaskStatus todoTaskStatus, final AsyncBackToFrontApi api)
    {
        if (ToDoTaskStatus.DONE == todoTaskStatus)
        {
            // Update with ESTABLISHED status
            api.setPolicyStatus(ApiPolicyStatus.ESTABLISHED);
            this.securizableApiManagerCommonService.addPoliciesToApi(api);

            // register activity done
            this.securizableApiManagerCommonService.emitNewPoliciesActivityResponse(todoTaskStatus, api, ActivityAction.POLICIES_REQUEST_DONE);
        }
        else if (ToDoTaskStatus.REJECTED == todoTaskStatus)
        {
            // Update with REJECTED status
            api.setPolicyStatus(ApiPolicyStatus.REJECTED);

            // register activity rejected
            this.securizableApiManagerCommonService.emitNewPoliciesActivityResponse(todoTaskStatus, api, ActivityAction.POLICIES_REQUEST_REJECTED);
        }
    }

    private void registerApi(final AsyncBackToFrontApi api)
    {
        final AGMRegisterApiDTO registerApiDTO = new AGMRegisterApiDTO();
        registerApiDTO.setApiName(api.getName());
        registerApiDTO.setBasepath(api.getBasePathSwagger());
        registerApiDTO.setUuaa(api.getUuaa());
        registerApiDTO.setApiType(api.getType().getApiType());
        registerApiDTO.setApiModality(api.getApiModality().getModality());

        this.apiGatewayManagerClient.createRegister(registerApiDTO);
    }

    @Override
    public void removeApiRegistration(final AsyncBackToFrontApi api)
    {
        final AGMRemoveApiDTO removeApiDTO = new AGMRemoveApiDTO();
        removeApiDTO.setApiName(api.getName());
        removeApiDTO.setBasepath(api.getBasePathSwagger());
        removeApiDTO.setUuaa(api.getUuaa());

        this.apiGatewayManagerClient.removeRegister(removeApiDTO);
    }

    @Override
    public void deleteApiTodoTasks(final AsyncBackToFrontApi api)
    {
        final ApiTaskKeyDTO apiTaskKeyDTO = new ApiTaskKeyDTO();
        apiTaskKeyDTO.setUuaa(api.getUuaa());
        apiTaskKeyDTO.setApiName(api.getName());
        apiTaskKeyDTO.setBasePath(api.getBasePathSwagger());

        this.todoTaskClient.deleteApiTasks(apiTaskKeyDTO);
    }

    @Override
    public AsyncBackToFrontApiImplementation createApiImplementation(final AsyncBackToFrontApiVersion apiVersion, final ReleaseVersionService releaseVersionService, final ImplementedAs implementedAs)
    {
        AsyncBackToFrontApiImplementation apiImplementation = new AsyncBackToFrontApiImplementation();
        apiImplementation.setApiVersion(apiVersion);
        apiImplementation.setService(releaseVersionService);
        apiImplementation.setImplementedAs(implementedAs);
        return apiImplementation;
    }

    @Override
    public AsyncBackToFrontApiImplementation createBehaviorApiImplementation(final AsyncBackToFrontApiVersion apiVersion, final BehaviorService behaviorService, final ImplementedAs implementedAs)
    {
        AsyncBackToFrontApiImplementation apiImplementation = new AsyncBackToFrontApiImplementation();
        apiImplementation.setApiVersion(apiVersion);
        apiImplementation.setBehaviorService(behaviorService);
        apiImplementation.setImplementedAs(implementedAs);
        return apiImplementation;
    }

    @Override
    public AsyncBackToFrontApiImplementation setConsumedApisByServedApi(final AsyncBackToFrontApiImplementation servedApi, final List<Integer> consumedApis)
    {
        // Not supported or necessary for asyncBackToBackApis
        return servedApi;
    }

    @Override
    public AsyncBackToFrontApiImplementation setBackwardCompatibleVersionsOfServedApi(final AsyncBackToFrontApiImplementation servedApi, final List<String> backwardCompatibleVersions)
    {
        // Not supported or necessary for asyncBackToBackApis
        return servedApi;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadProductApi(final AsyncBackToFrontApiVersion apiVersion, final String format, final String downloadType)
    {
        switch (format)
        {
            case "YAML":
            case "YML":
                return apiVersion.getDefinitionFile().getContents().getBytes();
            default:
                String message = String.format("[%s] -> [%s]: Invalid format for async back to front API [%s]", Constants
                        .DOWNLOAD_API_SERVICE_IMPL, "formatterContent", format);
                throw new NovaException(ApiManagerError.getInvalidFileFormatError(format), message);
        }
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.ASYNC_BACKTOFRONT == modality;
    }

    @Override
    public List<AsyncBackToFrontApi> getApisUsingMsaDocument(final Integer msaDocumentId)
    {
        return asyncBackToFrontApiRepository.findByMsaDocumentId(msaDocumentId);
    }

    @Override
    public List<AsyncBackToFrontApi> getApisUsingAraDocument(final Integer araDocumentId)
    {
        return asyncBackToFrontApiRepository.findByAraDocumentId(araDocumentId);
    }
}
