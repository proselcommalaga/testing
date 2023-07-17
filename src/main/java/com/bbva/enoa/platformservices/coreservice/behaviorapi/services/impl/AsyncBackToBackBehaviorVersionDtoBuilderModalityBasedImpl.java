package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceApiConsumedBy;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceApiDTO;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorVersionDtoBuilderModalityBased;
import org.springframework.stereotype.Component;

@Component
public class AsyncBackToBackBehaviorVersionDtoBuilderModalityBasedImpl
        implements IBehaviorVersionDtoBuilderModalityBased<AsyncBackToBackApi, AsyncBackToBackApiVersion, AsyncBackToBackApiImplementation>
{
    @Override
    public BVServiceApiDTO buildBVServiceApiDTO(final AsyncBackToBackApiImplementation apiImplementation)
    {
        AsyncBackToBackApiVersion apiVersion = apiImplementation.getApiVersion();
        AsyncBackToBackApi api = apiVersion.getApi();
        BVServiceApiDTO bvServiceApiDTO = new BVServiceApiDTO();
        bvServiceApiDTO.setId(apiVersion.getId());
        bvServiceApiDTO.setApiName(api.getName());
        bvServiceApiDTO.setDescription(apiVersion.getDescription());
        bvServiceApiDTO.setVersion(apiVersion.getVersion());
        bvServiceApiDTO.setBehaviorServiceId(apiImplementation.getService().getId());
        bvServiceApiDTO.setProduct(api.getProduct().getName());
        bvServiceApiDTO.setProductId(api.getProduct().getId());
        bvServiceApiDTO.setModality(apiImplementation.getApiModality().name());
        // consumedApis by Api is Not supported for asyncBackToBackApis
        bvServiceApiDTO.setConsumedApis(new BVServiceApiConsumedBy[0]);
        if (apiImplementation.getImplementedAs().equals(ImplementedAs.EXTERNAL))
        {
            bvServiceApiDTO.setExternal(true);
            bvServiceApiDTO.setUuaa(api.getUuaa());
        }
        else
        {
            bvServiceApiDTO.setExternal(false);
            bvServiceApiDTO.setUuaa(api.getProduct().getUuaa());
        }
        return bvServiceApiDTO;
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.ASYNC_BACKTOBACK == modality;
    }
}
