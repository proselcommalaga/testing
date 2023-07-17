package com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiErrorList;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiUploadRequestDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.TaskInfoDto;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiChannel;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.*;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorService;
import com.bbva.enoa.datamodel.model.common.entities.LobFile;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.ApiManagerError;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.DefinitionFileException;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.ApiUtils;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.IApiManagerValidator;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal.IDefinitionFileValidatorModalityBased;
import com.bbva.enoa.platformservices.coreservice.common.model.NovaAsyncAPI;
import com.bbva.enoa.platformservices.coreservice.common.util.Utils;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Implementation of interface for a NewApiBuildService Factory to validate BackToBack Asyncapi definitions
 *
 * @author BBVA - XE84890 - 02/07/2021
 */
@Service
@Slf4j
public class AsyncBackToBackApiManagerServiceModalityBasedImpl implements IApiManagerServiceModalityBased<AsyncBackToBackApi, AsyncBackToBackApiVersion, AsyncBackToBackApiImplementation>
{

    public static final String API_NAME_PARAM = "apiName";
    private static final Logger LOG = LoggerFactory.getLogger(AsyncBackToBackApiManagerServiceModalityBasedImpl.class);
    private final IApiManagerValidator apiManagerValidator;
    private final IDefinitionFileValidatorModalityBased<NovaAsyncAPI> definitionFileValidator;
    private final INovaActivityEmitter novaActivityEmitter;
    private final Utils utils;
    private final ApiUtils apiUtils;

    @Autowired
    public AsyncBackToBackApiManagerServiceModalityBasedImpl(
            final IApiManagerValidator apiManagerValidator,
            @Qualifier(value = "asyncBackToBackDefinitionFileValidatorModalityBasedImpl") final IDefinitionFileValidatorModalityBased<NovaAsyncAPI> definitionFileValidator,
            final INovaActivityEmitter novaActivityEmitter,
            final Utils utils,
            final ApiUtils apiUtils)
    {
        this.apiManagerValidator = apiManagerValidator;
        this.definitionFileValidator = definitionFileValidator;
        this.novaActivityEmitter = novaActivityEmitter;
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

    @Override
    public void createApiTask(final TaskInfoDto taskInfoDto, final AsyncBackToBackApi api)
    {
        //do nothing
    }

    @Override
    public void removeApiRegistration(final AsyncBackToBackApi api)
    {
        //do nothing
    }

    @Override
    public void onPolicyTaskReply(ToDoTaskStatus todoTaskStatus, AsyncBackToBackApi api)
    {
        //do nothing
    }

    @Override
    public void deleteApiTodoTasks(final AsyncBackToBackApi api)
    {
        //do nothing
    }

    @Override
    public List<AsyncBackToBackApi> getApisUsingMsaDocument(final Integer msaDocumentId)
    {
        return Collections.emptyList();
    }

    @Override
    public List<AsyncBackToBackApi> getApisUsingAraDocument(final Integer araDocumentId)
    {
        return Collections.emptyList();
    }

    @Override
    public AsyncBackToBackApiImplementation createApiImplementation(final AsyncBackToBackApiVersion apiVersion, final ReleaseVersionService releaseVersionService, final ImplementedAs implementedAs)
    {
        AsyncBackToBackApiImplementation apiImplementation = new AsyncBackToBackApiImplementation();
        apiImplementation.setApiVersion(apiVersion);
        apiImplementation.setService(releaseVersionService);
        apiImplementation.setImplementedAs(implementedAs);
        return apiImplementation;
    }

    @Override
    public AsyncBackToBackApiImplementation createBehaviorApiImplementation(final AsyncBackToBackApiVersion apiVersion, final BehaviorService behaviorService, final ImplementedAs implementedAs)
    {
        AsyncBackToBackApiImplementation apiImplementation = new AsyncBackToBackApiImplementation();
        apiImplementation.setApiVersion(apiVersion);
        apiImplementation.setBehaviorService(behaviorService);
        apiImplementation.setImplementedAs(implementedAs);
        return apiImplementation;
    }

    @Override
    public AsyncBackToBackApiImplementation setConsumedApisByServedApi(final AsyncBackToBackApiImplementation servedApi, final List<Integer> consumedApis)
    {
        // Not supported or necessary for asyncBackToBackApis
        return servedApi;
    }

    @Override
    public AsyncBackToBackApiImplementation setBackwardCompatibleVersionsOfServedApi(final AsyncBackToBackApiImplementation servedApi, final List<String> backwardCompatibleVersions)
    {
        // Not supported or necessary for asyncBackToBackApis
        return servedApi;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadProductApi(final AsyncBackToBackApiVersion apiVersion, final String format, final String downloadType)
    {
        switch (format)
        {
            case "YAML":
            case "YML":
                return apiVersion.getDefinitionFile().getContents().getBytes();
            default:
                String message = String.format("[%s] -> [%s]: Invalid format for async back to back API [%s]", Constants
                        .DOWNLOAD_API_SERVICE_IMPL, "formatterContent", format);
                throw new NovaException(ApiManagerError.getInvalidFileFormatError(format), message);
        }
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.ASYNC_BACKTOBACK == modality;
    }

    /**
     * Builds a {@link AsyncBackToBackApiVersion} and {@link AsyncBackToBackApi} and stores it in database
     *
     * @param novaAsyncApi AsyncApi content
     * @param content      File content
     * @param product      Nova product
     * @param apiType      API type
     */
    private void buildAndSave(final NovaAsyncAPI novaAsyncApi, final String content, final Product product, final ApiType apiType)
    {
        AsyncBackToBackApi api = new AsyncBackToBackApi();
        AsyncBackToBackApiVersion apiVersion = new AsyncBackToBackApiVersion();
        String apiTitle = novaAsyncApi.getAsyncAPI().getInfo().getTitle();
        String apiVersionPlain = novaAsyncApi.getAsyncAPI().getInfo().getVersion();
        String apiDescription = novaAsyncApi.getAsyncAPI().getInfo().getDescription();
        api.setProduct(product);
        api.setName(apiTitle);
        api.setType(apiType);

        if (apiType.isExternal())
        {
            api.setUuaa(novaAsyncApi.getXBusinessUnit().toUpperCase());
        }
        else
        {
            api.setUuaa(product.getUuaa());
        }

        api = this.apiManagerValidator.findAndValidateOrPersistIfMissing(api);
        // Checks if the version is already stored before setting all its properties
        this.apiManagerValidator.assertVersionOfApiNotExists(api, apiVersionPlain);

        apiVersion.setDescription(apiDescription);
        apiVersion.setVersion(apiVersionPlain);
        apiVersion.setApiState(ApiState.DEFINITION);

        // Replace all carrier return to avoid conflicts resolving md5
        String contentWithoutCarrierReturn = this.utils.unifyCRLF2LF(content);
        apiVersion.setDefinitionFile(new LobFile(apiTitle.trim() + ".yml", null, contentWithoutCarrierReturn));
        apiVersion.setDefinitionFileHash(this.utils.calculeMd5Hash(contentWithoutCarrierReturn.getBytes(StandardCharsets.UTF_8)));

        AsyncBackToBackApiChannel asyncBackToBackApiChannel = this.buildAsyncBackToBackApiChannel(novaAsyncApi);
        apiVersion.setAsyncBackToBackApiChannel(asyncBackToBackApiChannel);
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

    /**
     * Build information about channeles for a asyncapi back to back definition
     *
     * @param novaAsyncAPI nova asyncapi
     * @return async back to back api channel
     */
    private AsyncBackToBackApiChannel buildAsyncBackToBackApiChannel(final NovaAsyncAPI novaAsyncAPI)
    {
        AsyncBackToBackApiChannel asyncBackToBackApiChannel = new AsyncBackToBackApiChannel();
        String operationId;
        String channelName;
        AsyncBackToBackChannelType channelType;

        // Check if asyncapi have at least one channel.
        // AsyncApis back to back only have ONE channel. This is mandatory.
        Optional<String> optionalChannelName = novaAsyncAPI.getAsyncAPI().getChannels().keySet().stream().findFirst();
        if (optionalChannelName.isPresent())
        {
            channelName = optionalChannelName.get();
            // Get publisher channel if exist
            if (novaAsyncAPI.getAsyncAPI().getChannels().get(channelName).getPublish() != null)
            {
                channelType = AsyncBackToBackChannelType.PUBLISH;
                operationId = Objects.requireNonNull(novaAsyncAPI.getAsyncAPI().getChannels().get(channelName).getPublish()).getOperationId();
            }
            // Get subscriber channel if exist
            else
            {
                channelType = AsyncBackToBackChannelType.SUBSCRIBE;
                operationId = Objects.requireNonNull(novaAsyncAPI.getAsyncAPI().getChannels().get(channelName).getSubscribe()).getOperationId();
            }

            // Set asyncBackToBackApiChannel params
            asyncBackToBackApiChannel.setChannelName(channelName);
            asyncBackToBackApiChannel.setChannelType(channelType);
            asyncBackToBackApiChannel.setOperationId(operationId);

            log.debug("[AsyncBackToBackApiManagerServiceModalityBasedImpl] -> [buildAsyncBackToBackApiChannel]: Built AsyncBackToBackApiChannel: [{}] for AsyncApiBackToBack: [{}]", asyncBackToBackApiChannel, novaAsyncAPI.getAsyncAPI().getInfo().getTitle());
        }
        else
        {
            log.error("[AsyncBackToBackApiManagerServiceModalityBasedImpl] -> [buildAsyncBackToBackApiChannel]: No channels founds in AsyncApiBackToBack with name: [{}]", novaAsyncAPI.getAsyncAPI().getInfo().getTitle());
        }

        return asyncBackToBackApiChannel;
    }
}
