package com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal;

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
import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiVersion;
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
import com.bbva.enoa.platformservices.coreservice.common.repositories.SyncApiRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.SyncApiVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.SwaggerConverter;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IApiGatewayManagerClient;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Builds and stores new API entries into the database
 */
@Service
public class SyncApiManagerServiceModalityBasedImpl implements IApiManagerServiceModalityBased<SyncApi, SyncApiVersion, SyncApiImplementation>
{

    private static final Logger LOG = LoggerFactory.getLogger(SyncApiManagerServiceModalityBasedImpl.class);
    public static final String API_NAME_PARAM = "apiName";
    public static final String CONNECTOR_SDA_PARAM = "connectorSolutionsArchitectDocumentation";
    public static final String CONNECTOR_MSA_PARAM = "connectorSecurityDocumentation";
    public static final String BASE_PATH_PARAM = "basePath";
    public static final String TODO_TYPE_PARAM = "TodoTaskTypeCreated";
    public static final String TODO_ID_PARAM = "TodoTaskId";
    private final SyncApiRepository apiRepository;
    private final SyncApiVersionRepository apiVersionRepository;
    private final INovaActivityEmitter novaActivityEmitter;
    private final IApiManagerValidator apiManagerValidatorService;
    private final IApiGatewayManagerClient apiGatewayManagerClient;
    private final TodoTaskServiceClient todoTaskClient;
    private final NovaContext novaContext;
    private final IDefinitionFileValidatorModalityBased<Swagger> definitionFileValidator;
    private final SecurizableApiManagerCommonService securizableApiManagerCommonService;
    private final ApiUtils apiUtils;
    private final SwaggerConverter swaggerConverter;

    @Autowired
    public SyncApiManagerServiceModalityBasedImpl(
            final SyncApiRepository apiRepository, final SyncApiVersionRepository apiVersionRepository,
            final INovaActivityEmitter novaActivityEmitter,
            final IApiManagerValidator apiManagerValidatorService,
            final IApiGatewayManagerClient apiGatewayManagerClient, final TodoTaskServiceClient todoTaskClient,
            final NovaContext novaContext,
            @Qualifier(value = "syncApiDefinitionFileValidatorModalityBasedImpl") final IDefinitionFileValidatorModalityBased<Swagger> definitionFileValidator,
            final SecurizableApiManagerCommonService securizableApiManagerCommonService,
            final ApiUtils apiUtils,
            final SwaggerConverter swaggerConverter)
    {
        this.apiRepository = apiRepository;
        this.apiVersionRepository = apiVersionRepository;
        this.novaActivityEmitter = novaActivityEmitter;
        this.apiManagerValidatorService = apiManagerValidatorService;
        this.apiGatewayManagerClient = apiGatewayManagerClient;
        this.todoTaskClient = todoTaskClient;
        this.novaContext = novaContext;
        this.definitionFileValidator = definitionFileValidator;
        this.securizableApiManagerCommonService = securizableApiManagerCommonService;
        this.apiUtils = apiUtils;
        this.swaggerConverter = swaggerConverter;
    }

    @Override
    @Transactional
    public ApiErrorList uploadApi(final ApiUploadRequestDto apiUploadRequest, final Integer productId)
    {
        Product product = this.apiManagerValidatorService.checkProductExistence(productId);

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
            Swagger swagger = this.definitionFileValidator.parseAndValidate(
                    decodedContent, product, apiType
            );
            this.buildAndSave(swagger, decodedContent, product, apiType);
            apiErrorList.setErrorList(new String[0]);
        }
        catch (DefinitionFileException ex)
        {
            apiErrorList.setErrorList(ex.getErrorArray());
        }
        return apiErrorList;
    }

    @Override
    @Transactional
    public void createApiTask(final TaskInfoDto taskInfoDto, final SyncApi api)
    {
        String ivUser = this.novaContext.getIvUser();
        final ApiTaskKeyDTO apiTaskKeyDTO = new ApiTaskKeyDTO();
        apiTaskKeyDTO.setUuaa(api.getUuaa());
        apiTaskKeyDTO.setApiName(api.getName());
        apiTaskKeyDTO.setBasePath(api.getBasePathSwagger());

        ApiTaskDTO apiTaskDTO = this.todoTaskClient.getApiTask(apiTaskKeyDTO);
        if (apiTaskDTO != null)
        {
            String apiTaskStatus = apiTaskDTO.getStatus();
            if (Constants.TODO_TASK_PENDING_STATUS.equals(apiTaskStatus) || Constants.TODO_TASK_PENDING_ERROR_STATUS.equals(apiTaskStatus))
            {
                LOG.error("[ApiManagerServiceImpl] -> [createApiTask]: A todo task exist with status [{}] for API with id [{}] ", apiTaskStatus, taskInfoDto.getNovaApiId());
                throw new NovaException(ApiManagerError.getPoliciesTodoTaskPending(api.getUuaa(), api.getName()), "A todo task already exists in pending state");
            }
        }

        DocSystem araDocument = this.apiManagerValidatorService.validateAndGetDocument(taskInfoDto.getAraDocumentId(), DocumentCategory.ARA, DocumentType.FILE);
        DocSystem msaDocument = this.apiManagerValidatorService.validateAndGetDocument(taskInfoDto.getMsaDocumentId(), DocumentCategory.MSA, DocumentType.FILE);
        api.setAraDocument(araDocument);
        api.setMsaDocument(msaDocument);

        if (!api.getType().isExternal())
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
            ActivityScope activityScope = null;
            switch (api.getType())
            {
                case NOT_GOVERNED:
                    activityScope = ActivityScope.UNGOVERNED_API;
                    break;
                case GOVERNED:
                    activityScope = ActivityScope.GOVERNED_API;
                    break;
                case EXTERNAL:
                    activityScope = ActivityScope.THIRD_PARTY_API;
                    break;
                default:
                    LOG.debug("Not supported Api Type: {}", api.getType().getApiType());
            }

            if (activityScope != null)
            {
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(api.getProduct().getId(), activityScope, ActivityAction.SEND_REQUEST_POLICIES_REQUEST)
                        .entityId(api.getId())
                        .addParam(API_NAME_PARAM, api.getName())
                        .addParam(CONNECTOR_SDA_PARAM, msaDocument.getUrl())
                        .addParam(CONNECTOR_MSA_PARAM, araDocument.getUrl())
                        .addParam(BASE_PATH_PARAM, api.getBasePathSwagger())
                        .addParam(TODO_TYPE_PARAM, ToDoTaskType.API_ESTABLISH_POLICIES.name())
                        .addParam(TODO_ID_PARAM, todoTaskId)
                        .build());
            }
        }
        else
        {
            LOG.warn("[ApiManagerServiceImpl] -> [createApiTask]: the to do task is already created: [{}]. Activity will not created ", Constants.TODO_TASK_ALREADY_CREATED_CODE);
        }
    }

    private void registerApi(final SyncApi api)
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
    public void onPolicyTaskReply(final ToDoTaskStatus todoTaskStatus, final SyncApi api)
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

    @Override
    public void removeApiRegistration(final SyncApi api)
    {
        AGMRemoveApiDTO removeApiDTO = new AGMRemoveApiDTO();
        removeApiDTO.setApiName(api.getName());
        removeApiDTO.setBasepath(api.getBasePathSwagger());
        removeApiDTO.setUuaa(api.getUuaa());
        this.apiGatewayManagerClient.removeRegister(removeApiDTO);
    }

    @Override
    public void deleteApiTodoTasks(SyncApi api)
    {
        ApiTaskKeyDTO apiTaskKeyDTO = new ApiTaskKeyDTO();
        apiTaskKeyDTO.setUuaa(api.getUuaa());
        apiTaskKeyDTO.setApiName(api.getName());
        apiTaskKeyDTO.setBasePath(api.getBasePathSwagger());

        this.todoTaskClient.deleteApiTasks(apiTaskKeyDTO);
    }

    @Override
    public List<SyncApi> getApisUsingMsaDocument(final Integer msaDocumentId)
    {
        return apiRepository.findByMsaDocumentId(msaDocumentId);
    }

    @Override
    public List<SyncApi> getApisUsingAraDocument(final Integer araDocumentId)
    {
        return apiRepository.findByAraDocumentId(araDocumentId);
    }

    @Override
    public SyncApiImplementation createApiImplementation(final SyncApiVersion apiVersion, final ReleaseVersionService releaseVersionService, final ImplementedAs implementedAs)
    {
        SyncApiImplementation apiImplementation = new SyncApiImplementation();
        apiImplementation.setApiVersion(apiVersion);
        apiImplementation.setService(releaseVersionService);
        apiImplementation.setImplementedAs(implementedAs);
        return apiImplementation;
    }

    @Override
    public SyncApiImplementation createBehaviorApiImplementation(final SyncApiVersion apiVersion, final BehaviorService behaviorService, final ImplementedAs implementedAs)
    {
        SyncApiImplementation apiImplementation = new SyncApiImplementation();
        apiImplementation.setApiVersion(apiVersion);
        apiImplementation.setBehaviorService(behaviorService);
        apiImplementation.setImplementedAs(implementedAs);
        return apiImplementation;
    }

    @Override
    public SyncApiImplementation setConsumedApisByServedApi(final SyncApiImplementation servedApi, final List<Integer> consumedApis)
    {
        for (Integer consumedApiId : consumedApis)
        {
            SyncApiVersion consumedApiVersion = this.apiVersionRepository.findById(consumedApiId).orElse(null);
            if (consumedApiVersion != null)
            {
                // Un api asyncrona no puede consumir un api sincrona y viceversa
                servedApi.getConsumedApis().add(consumedApiVersion);
                if (ApiState.DEFINITION == consumedApiVersion.getApiState())
                {
                    consumedApiVersion.setApiState(ApiState.IMPLEMENTED);
                }
            }
        }
        return servedApi;
    }

    @Override
    public SyncApiImplementation setBackwardCompatibleVersionsOfServedApi(final SyncApiImplementation servedApi, final List<String> backwardCompatibleVersions)
    {
        for (String backwardCompatibleVersion : backwardCompatibleVersions)
        {
            LOG.debug("[setBackwardCompatibleVersionsOfServedApi] -> Looking for backward compatible version [{}]", backwardCompatibleVersion);
            SyncApiVersion backwardCompatibleApiVersion =
                    this.apiVersionRepository.findByApiNameAndVersionAndUuaaAndExternalFalse(
                            servedApi.getApiVersion().getApi().getName(),
                            backwardCompatibleVersion,
                            servedApi.getApiVersion().getApi().getUuaa()
                    );
            if (backwardCompatibleApiVersion != null)
            {
                LOG.debug("[setBackwardCompatibleVersionsOfServedApi] -> Backward compatible version [{}] found with id [{}]", backwardCompatibleVersion, backwardCompatibleApiVersion.getId());
                // Check if the api version is already inserted as backward compatible
                boolean isBackwardApiAlreadyInserted = servedApi.getBackwardCompatibleApis().stream()
                        .anyMatch(backwardVersion -> backwardVersion.getId().equals(backwardCompatibleApiVersion.getId()));
                if (!isBackwardApiAlreadyInserted)
                {
                    LOG.debug("[setBackwardCompatibleVersionsOfServedApi] -> Inserting backward compatible version [{}] with id [{}] in api implementation with id [{}]", backwardCompatibleVersion, backwardCompatibleApiVersion.getId(), servedApi.getId());
                    servedApi.getBackwardCompatibleApis().add(backwardCompatibleApiVersion);
                }
                // The status must be updated even if the api version was already inserted
                if (ApiState.DEFINITION.equals(backwardCompatibleApiVersion.getApiState()))
                {
                    backwardCompatibleApiVersion.setApiState(ApiState.IMPLEMENTED);
                }
            }
        }
        return servedApi;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadProductApi(final SyncApiVersion apiVersion, final String format, final String downloadType)
    {
        String content = apiVersion.getDefinitionFile().getContents();
        Swagger swagger = this.swaggerConverter.parseSwaggerFromString(content);
        if (Constants.CONSUMED.equalsIgnoreCase(downloadType) || ApiType.EXTERNAL == apiVersion.getApi().getType())
        {
            swagger.setBasePath(apiVersion.getBasePathXmas() + apiVersion.getApi().getBasePathSwagger());
        }
        switch (format)
        {
            case "YAML":
            case "YML":
                return this.swaggerConverter.swaggerYamlToByteArray(swagger);
            case "JSON":
                return this.swaggerConverter.swaggerJsonToByteArray(swagger);
            default:
                String message = String.format("[%s] -> [%s]: Invalid format [%s]", Constants
                        .DOWNLOAD_API_SERVICE_IMPL, "formatterContent", format);
                throw new NovaException(ApiManagerError.getInvalidFileFormatError(format), message);
        }
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.SYNC == modality;
    }

    //////////////////////////////////////   PRIVATE METHODS  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Builds a {@link SyncApiVersion} and {@link SyncApi} and stores it in database
     *
     * @param swagger Swagger content
     * @param content File content
     * @param product Nova product
     * @param apiType API type
     */
    private void buildAndSave(final Swagger swagger, final String content, final Product product, final ApiType apiType)
    {
        SyncApi api = new SyncApi();
        SyncApiVersion apiVersion = new SyncApiVersion();
        String swaggerTitle = swagger.getInfo().getTitle();
        String swaggerVersion = swagger.getInfo().getVersion();
        api.setBasePathSwagger(this.apiUtils.ensureStartingSlash(swagger.getBasePath()));
        api.setProduct(product);
        api.setName(swaggerTitle);

        if (apiType.isExternal())
        {
            Map<String, Object> customProperties = (Map<String, Object>) swagger.getVendorExtensions().get("x-generator-properties");
            api.setUuaa(customProperties.get("business-unit").toString().toUpperCase());
            apiVersion.setBasePathXmas(
                    this.apiUtils.buildXmasApiBasePath(
                            api.getUuaa(), swaggerTitle, swaggerVersion
                    )
            );
        }
        else
        {
            api.setUuaa(product.getUuaa());
            apiVersion.setBasePathXmas(
                    this.apiUtils.buildXmasApiBasePath(
                            product.getUuaa(), swaggerTitle, swaggerVersion
                    )
            );
        }

        // Setting policies status before trying to recover the stored sync api in order to override it in case the api already existed
        api.setPolicyStatus(ApiPolicyStatus.PENDING);
        api.setType(apiType);

        api = this.apiManagerValidatorService.findAndValidateOrPersistIfMissing(api);

        // Checks if the version is already stored before setting all its properties
        this.apiManagerValidatorService.assertVersionOfApiNotExists(api, swaggerVersion);

        apiVersion.setDescription(swagger.getInfo().getDescription());
        apiVersion.setVersion(swaggerVersion);
        apiVersion.setDefinitionFile(new LobFile(swaggerTitle.trim() + ".yml", null, content));
        apiVersion.setApiState(ApiState.DEFINITION);

        List<ApiMethod> novaApiMethodList = new ArrayList<>();

        swagger.getPaths().entrySet().forEach(pathEntry -> {
            pathEntry.getValue().getOperationMap().entrySet().forEach(operacionEntry -> {
                HttpMethod httpMethod = operacionEntry.getKey();
                Operation operation = operacionEntry.getValue();
                LOG.debug("Swagger: [{}]", httpMethod.toString());

                ApiMethod apiMethod = new ApiMethod();
                apiMethod.setEndpoint(pathEntry.getKey());
                apiMethod.setDescription(operation.getDescription());
                apiMethod.setVerb(Verb.valueOf(httpMethod.toString()));
                apiMethod.setApiVersion(apiVersion);

                novaApiMethodList.add(apiMethod);
            });
        });

        apiVersion.setApiMethods(novaApiMethodList);
        apiVersion.setApi(api);
        api.getApiVersions().add(apiVersion);

        switch (apiType)
        {
            case NOT_GOVERNED:
                // Emit Add Ungoverned Api Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(product.getId(), ActivityScope.UNGOVERNED_API, ActivityAction.ADDED)
                        .entityId(api.getId())
                        .addParam(API_NAME_PARAM, api.getName())
                        .build());
                break;
            case GOVERNED:
                // Emit Add Governed Api Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(product.getId(), ActivityScope.GOVERNED_API, ActivityAction.ADDED)
                        .entityId(api.getId())
                        .addParam(API_NAME_PARAM, api.getName())
                        .build());
                break;
            case EXTERNAL:
                // Emit Add Third Party Api Activity
                this.novaActivityEmitter.emitNewActivity(new GenericActivity
                        .Builder(product.getId(), ActivityScope.THIRD_PARTY_API, ActivityAction.ADDED)
                        .entityId(api.getId())
                        .addParam(API_NAME_PARAM, api.getName())
                        .build());
                break;
            default:
                LOG.debug("Not supported Api Type: {}", apiType);
        }
    }

}
