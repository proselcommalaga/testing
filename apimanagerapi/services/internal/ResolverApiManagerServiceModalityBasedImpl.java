package com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiErrorList;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiUploadRequestDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.TaskInfoDto;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Interface for a NewApiBuildService Factory
 *
 * @author BBVA - XE84890 - 02/07/2021
 */
@Service
@Primary
public class ResolverApiManagerServiceModalityBasedImpl<A extends Api<A, AV, AI>, AV extends ApiVersion<A, AV, AI>, AI extends ApiImplementation<A, AV, AI>>
        implements IApiManagerServiceModalityBased<A, AV, AI>
{
    private final Map<ApiModality, IApiManagerServiceModalityBased<A, AV, AI>> builderServiceMap = new HashMap<>();

    @Autowired
    public ResolverApiManagerServiceModalityBasedImpl(
            final List<IApiManagerServiceModalityBased<A, AV, AI>> builderServices
    )
    {
        for (IApiManagerServiceModalityBased<A, AV, AI> builderService : builderServices)
        {
            Arrays.stream(ApiModality.values())
                    .filter(builderService::isModalitySupported)
                    .findAny()
                    .ifPresent(modality -> this.builderServiceMap.put(modality, builderService));
        }
    }

    @Override
    public ApiErrorList uploadApi(final ApiUploadRequestDto upload, final Integer productId)
    {
        return this.getModalityBasedApiManagerService(ApiModality.valueOf(upload.getApiModality()))
                .uploadApi(upload, productId);
    }

    @Override
    public void createApiTask(final TaskInfoDto taskInfoDto, final A api)
    {
        this.getModalityBasedApiManagerService(api.getApiModality())
                .createApiTask(taskInfoDto, api);
    }

    @Override
    public void onPolicyTaskReply(final ToDoTaskStatus toDoTaskStatus, A api)
    {
        this.getModalityBasedApiManagerService(api.getApiModality())
                .onPolicyTaskReply(toDoTaskStatus, api);
    }

    @Override
    public void removeApiRegistration(final A api)
    {
        this.getModalityBasedApiManagerService(api.getApiModality())
                .removeApiRegistration(api);
    }

    @Override
    public void deleteApiTodoTasks(final A api)
    {
        this.getModalityBasedApiManagerService(api.getApiModality())
                .deleteApiTodoTasks(api);
    }

    @Override
    // TODO@async: evaluar sacar a agregador
    public List<A> getApisUsingMsaDocument(final Integer msaDocumentId)
    {
        return this.builderServiceMap.values().stream()
                .flatMap(service -> service.getApisUsingMsaDocument(msaDocumentId).stream())
                .collect(Collectors.toList());
    }

    @Override
    // TODO@async: evaluar sacar a agregador
    public List<A> getApisUsingAraDocument(final Integer araDocumentId)
    {
        return this.builderServiceMap.values().stream()
                .flatMap(service -> service.getApisUsingAraDocument(araDocumentId).stream())
                .collect(Collectors.toList());
    }

    @Override
    public AI createApiImplementation(final AV apiVersion, final ReleaseVersionService releaseVersionService, final ImplementedAs implementedAs)
    {
        return this.getModalityBasedApiManagerService(apiVersion.getApiModality())
                .createApiImplementation(apiVersion, releaseVersionService, implementedAs);
    }

    @Override
    public AI createBehaviorApiImplementation(final AV apiVersion, final BehaviorService behaviorService, final ImplementedAs implementedAs)
    {
        return this.getModalityBasedApiManagerService(apiVersion.getApiModality())
                .createBehaviorApiImplementation(apiVersion, behaviorService, implementedAs);
    }

    @Override
    public AI setConsumedApisByServedApi(final AI servedApi, final List<Integer> consumedApis)
    {
        return this.getModalityBasedApiManagerService(servedApi.getApiModality())
                .setConsumedApisByServedApi(servedApi, consumedApis);
    }

    @Override
    public AI setBackwardCompatibleVersionsOfServedApi(final AI servedApi, final List<String> backwardCompatibleVersions)
    {
        return this.getModalityBasedApiManagerService(servedApi.getApiModality())
                .setBackwardCompatibleVersionsOfServedApi(servedApi, backwardCompatibleVersions);
    }

    @Override
    public byte[] downloadProductApi(final AV apiVersion, final String format, final String downloadType)
    {
        return this.getModalityBasedApiManagerService(apiVersion.getApiModality())
                .downloadProductApi(apiVersion, format, downloadType);
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return this.builderServiceMap.containsKey(modality);
    }

    private IApiManagerServiceModalityBased<A, AV, AI> getModalityBasedApiManagerService(final ApiModality modality)
    {
        return Optional.ofNullable(this.builderServiceMap.get(modality))
                .orElseThrow(() -> new NotImplementedException(String.format("There is no an IApiManagerServiceModalityBased implemented for the modality %s", modality)));
    }
}
