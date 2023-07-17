package com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder.internal;

import com.bbva.enoa.apirestgen.apimanagerapi.model.*;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Builder for api detail dto model
 */
@Component
@AllArgsConstructor
public class AsyncBackToBackDtoBuilderModalityBasedImpl implements IDtoBuilderModalityBased<AsyncBackToBackApi, AsyncBackToBackApiVersion, AsyncBackToBackApiImplementation>
{

    @Override
    public ApiDetailDto buildApiDetailDto(final AsyncBackToBackApi api)
    {
        ApiDetailDto apiDetailDto = new ApiDetailDto();
        apiDetailDto.setVersions(this.buildApiVersionDtoArrayFromApiVersionList(api.getApiVersions()));
        return apiDetailDto;
    }

    private ApiVersionDto[] buildApiVersionDtoArrayFromApiVersionList(List<AsyncBackToBackApiVersion> apiVersionList)
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
    public ApiPlanDto buildApiPlanDto(final AsyncBackToBackApiVersion apiVersion, final String releaseName, final PlanProfile planProfile)
    {
        AsyncBackToBackApi api = apiVersion.getApi();
        ApiPlanDto apiPlanDto = new ApiPlanDto();
        apiPlanDto.setName(api.getName() + ":" + apiVersion.getVersion());
        return apiPlanDto;
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.ASYNC_BACKTOBACK == modality;
    }
}
