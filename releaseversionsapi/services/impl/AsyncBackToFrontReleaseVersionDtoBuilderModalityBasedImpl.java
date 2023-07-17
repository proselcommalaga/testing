package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiConsumedBy;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiDTO;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiPolicyStatus;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionDtoBuilderModalityBased;
import org.springframework.stereotype.Component;

@Component
public class AsyncBackToFrontReleaseVersionDtoBuilderModalityBasedImpl
        implements IReleaseVersionDtoBuilderModalityBased<AsyncBackToFrontApi, AsyncBackToFrontApiVersion, AsyncBackToFrontApiImplementation>
{
    @Override
    public RVApiDTO buildRVApiDTO(final AsyncBackToFrontApiImplementation apiImplementation)
    {
        AsyncBackToFrontApiVersion apiVersion = apiImplementation.getApiVersion();
        AsyncBackToFrontApi api = apiVersion.getApi();
        RVApiDTO rvApiDto = new RVApiDTO();
        rvApiDto.setId(apiVersion.getId());
        rvApiDto.setApiName(api.getName());
        rvApiDto.setDescription(apiVersion.getDescription());
        rvApiDto.setVersion(apiVersion.getVersion());
        rvApiDto.setServiceId(apiImplementation.getService().getId());
        rvApiDto.setProduct(api.getProduct().getName());
        rvApiDto.setProductId(api.getProduct().getId());
        rvApiDto.setModality(apiImplementation.getApiModality().name());
        // consumedApis by Api is Not supported for asyncBackToFrontApis
        rvApiDto.setConsumedApis(new RVApiConsumedBy[0]);
        if (apiImplementation.getImplementedAs().equals(ImplementedAs.EXTERNAL))
        {
            rvApiDto.setExternal(true);
            rvApiDto.setUuaa(api.getUuaa());
        }
        else
        {
            rvApiDto.setExternal(false);
            rvApiDto.setUuaa(api.getProduct().getUuaa());
        }
        rvApiDto.setPoliciesConfigured(ApiPolicyStatus.arePoliciesSet(api.getPolicyStatus()));
        return rvApiDto;
    }


    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.ASYNC_BACKTOFRONT == modality;
    }
}
