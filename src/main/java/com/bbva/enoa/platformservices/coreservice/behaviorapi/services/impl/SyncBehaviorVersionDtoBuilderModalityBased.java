package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceApiConsumedBy;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceApiDTO;
import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiPolicyStatus;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorVersionDtoBuilderModalityBased;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SyncBehaviorVersionDtoBuilderModalityBased
        implements IBehaviorVersionDtoBuilderModalityBased<SyncApi, SyncApiVersion, SyncApiImplementation>
{
    @Override
    public BVServiceApiDTO buildBVServiceApiDTO(final SyncApiImplementation apiImplementation)
    {
        SyncApiVersion apiVersion = apiImplementation.getApiVersion();
        SyncApi api = apiVersion.getApi();
        BVServiceApiDTO bvServiceApiDTO = new BVServiceApiDTO();
        bvServiceApiDTO.setId(apiVersion.getId());
        bvServiceApiDTO.setApiName(api.getName());
        bvServiceApiDTO.setDescription(apiVersion.getDescription());
        bvServiceApiDTO.setVersion(apiVersion.getVersion());
        bvServiceApiDTO.setBehaviorServiceId(apiImplementation.getBehaviorService().getId());
        bvServiceApiDTO.setProduct(api.getProduct().getName());
        bvServiceApiDTO.setProductId(api.getProduct().getId());
        bvServiceApiDTO.setModality(apiImplementation.getApiModality().name());
        // consumedApis by Api is ONLY supported for syncApis
        bvServiceApiDTO.setConsumedApis(mapConsumedApis(apiImplementation.getConsumedApis()));
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

    private BVServiceApiConsumedBy[] mapConsumedApis(List<SyncApiVersion> consumedApis)
    {
        List<BVServiceApiConsumedBy> consumedApiDtos = new ArrayList<>();
        for (SyncApiVersion apiVersion : consumedApis)
        {
            SyncApi api = apiVersion.getApi();
            BVServiceApiConsumedBy dto = new BVServiceApiConsumedBy();
            dto.setApiName(api.getName());
            dto.setVersion(apiVersion.getVersion());
            dto.setBasePath(apiVersion.getBasePathXmas());
            dto.setDescription(apiVersion.getDescription());
            dto.setId(apiVersion.getId());
            dto.setProduct(api.getProduct().getName());
            dto.setProductId(api.getProduct().getId());
            dto.setUuaa(api.getUuaa());
            if (ApiType.EXTERNAL == api.getType())
            {
                dto.setBasePath(api.getBasePathSwagger());
                dto.setExternal(true);
            }
            else
            {
                dto.setBasePath(apiVersion.getBasePathXmas());
                dto.setExternal(false);
            }
            dto.setPoliciesConfigured(ApiPolicyStatus.arePoliciesSet(api.getPolicyStatus()));
            consumedApiDtos.add(dto);
        }
        return consumedApiDtos.toArray(new BVServiceApiConsumedBy[0]);
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.SYNC == modality;
    }
}
