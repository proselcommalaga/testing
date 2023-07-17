package com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder.internal;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDocument;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPlanDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPoliciesDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiVersionDto;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskKeyDTO;
import com.bbva.enoa.datamodel.model.api.entities.ApiSecurityPolicy;
import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiPolicyStatus;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Builder for api detail dto model
 */
@Component
@AllArgsConstructor
public class SyncDtoBuilderModalityBasedImpl implements IDtoBuilderModalityBased<SyncApi, SyncApiVersion, SyncApiImplementation>
{

    private final TodoTaskServiceClient todoTaskClient;
    private final IProfilableDtoBuilder profilableDtoBuilder;

    @Override
    public ApiDetailDto buildApiDetailDto(final SyncApi api)
    {
        ApiDetailDto apiDetailDto = new ApiDetailDto();
        apiDetailDto.setVersions(this.buildApiVersionDtoArrayFromApiVersionList(api.getApiVersions()));
        apiDetailDto.setPendingTaskId(this.getPendingTaskForApi(api));
        apiDetailDto.setPolicies(this.buildPoliciesDtoArrayFromSyncApiSecurityPoliciesList(api.getApiSecurityPolicies()));
        apiDetailDto.setMsaDocument(this.buildApiDocument(api.getMsaDocument()));
        apiDetailDto.setAraDocument(this.buildApiDocument(api.getAraDocument()));
        apiDetailDto.setHasBackConsumers(api.getHasBackConsumers());
        apiDetailDto.setHasFrontConsumers(api.getHasFrontConsumers());

        return apiDetailDto;
    }

    private ApiVersionDto[] buildApiVersionDtoArrayFromApiVersionList(List<SyncApiVersion> apiVersionList)
    {
        return apiVersionList.stream().map(apiVersion -> {
            ApiVersionDto apiVersionDto = new ApiVersionDto();
            apiVersionDto.setId(apiVersion.getId());
            apiVersionDto.setVersion(apiVersion.getVersion());
            apiVersionDto.setStatus(apiVersion.getApiState().getApiState());
            return apiVersionDto;
        }).toArray(ApiVersionDto[]::new);
    }

    @Override
    public ApiPlanDto buildApiPlanDto(final SyncApiVersion apiVersion, final String releaseName, final PlanProfile planProfile)
    {
        SyncApi api = apiVersion.getApi();
        ApiPlanDto apiPlanDto = new ApiPlanDto();
        apiPlanDto.setName(api.getName() + ":" + apiVersion.getVersion());
        apiPlanDto.setPolicies(ApiPolicyStatus.arePoliciesSet(api.getPolicyStatus()));
        apiPlanDto.setMethods(this.profilableDtoBuilder.buildApiMethodDtoArray(apiVersion, releaseName, planProfile));
        return apiPlanDto;
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.SYNC == modality;
    }

    /**
     * Build an ApiDocument DTO from a DocSystem entity.
     *
     * @param documentEntity The given DocSystem entity.
     * @return The ApiDocument DTO, or null if the DocSystem entity is null.
     */
    private ApiDocument buildApiDocument(DocSystem documentEntity)
    {
        if (documentEntity != null)
        {
            ApiDocument apiDocument = new ApiDocument();
            apiDocument.setId(documentEntity.getId());
            apiDocument.setSystemName(documentEntity.getSystemName());
            apiDocument.setUrl(documentEntity.getUrl());
            return apiDocument;
        }
        else
        {
            return null;
        }
    }

    private Integer getPendingTaskForApi(final SyncApi syncApi)
    {
        if (ApiPolicyStatus.PENDING.equals(syncApi.getPolicyStatus()))
        {
            final ApiTaskKeyDTO apiTaskKeyDTO = new ApiTaskKeyDTO();
            apiTaskKeyDTO.setUuaa(syncApi.getUuaa());
            apiTaskKeyDTO.setApiName(syncApi.getName());
            apiTaskKeyDTO.setBasePath(syncApi.getBasePathSwagger());

            final ApiTaskDTO apiTaskDTO = this.todoTaskClient.getApiTask(apiTaskKeyDTO);

            if (apiTaskDTO != null && ToDoTaskStatus.PENDING.name().equals(apiTaskDTO.getStatus()))
            {
                return apiTaskDTO.getId();
            }
        }

        return null;
    }

    private ApiPoliciesDto[] buildPoliciesDtoArrayFromSyncApiSecurityPoliciesList(List<ApiSecurityPolicy> apiSecurityPolicyList)
    {
        return apiSecurityPolicyList.stream().map(syncApiSecurityPolicy -> {
            ApiPoliciesDto apiPoliciesDto = new ApiPoliciesDto();
            apiPoliciesDto.setName(syncApiSecurityPolicy.getSecurityPolicy().getDisplayedName());
            apiPoliciesDto.setType(syncApiSecurityPolicy.getSecurityPolicy().getSecurityPolicyType().getPolicyType());
            apiPoliciesDto.setEnvironment(syncApiSecurityPolicy.getId().getEnvironment());
            return apiPoliciesDto;
        }).toArray(ApiPoliciesDto[]::new);
    }
}
