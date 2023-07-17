package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceApiConsumedBy;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceApiDTO;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiPolicyStatus;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorVersionDtoBuilderModalityBased;
import org.springframework.stereotype.Component;

@Component
public class AsyncBackToFrontBehaviorVersionDtoBuilderModalityBasedImpl
        implements IBehaviorVersionDtoBuilderModalityBased<AsyncBackToFrontApi, AsyncBackToFrontApiVersion, AsyncBackToFrontApiImplementation>
{
    @Override
    public BVServiceApiDTO buildBVServiceApiDTO(final AsyncBackToFrontApiImplementation apiImplementation)
    {
        AsyncBackToFrontApiVersion apiVersion = apiImplementation.getApiVersion();
        AsyncBackToFrontApi api = apiVersion.getApi();
        BVServiceApiDTO bvServiceApiDTO = new BVServiceApiDTO();
        bvServiceApiDTO.setId(apiVersion.getId());
        bvServiceApiDTO.setApiName(api.getName());
        bvServiceApiDTO.setDescription(apiVersion.getDescription());
        bvServiceApiDTO.setVersion(apiVersion.getVersion());
        bvServiceApiDTO.setBehaviorServiceId(apiImplementation.getBehaviorService().getId());
        bvServiceApiDTO.setProduct(api.getProduct().getName());
        bvServiceApiDTO.setProductId(api.getProduct().getId());
        bvServiceApiDTO.setModality(apiImplementation.getApiModality().name());
        // consumedApis by Api is Not supported for asyncBackToFrontApis
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
        bvServiceApiDTO.setPoliciesConfigured(ApiPolicyStatus.arePoliciesSet(api.getPolicyStatus()));
        return bvServiceApiDTO;
    }


    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.ASYNC_BACKTOFRONT == modality;
    }
}
