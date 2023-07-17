package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiConsumedBy;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiDTO;
import com.bbva.enoa.datamodel.model.api.entities.*;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiPolicyStatus;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionDtoBuilderModalityBased;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SyncReleaseVersionDtoBuilderModalityBased
        implements IReleaseVersionDtoBuilderModalityBased<SyncApi, SyncApiVersion, SyncApiImplementation>
{
    @Override
    public RVApiDTO buildRVApiDTO(final SyncApiImplementation apiImplementation)
    {
        SyncApiVersion apiVersion = apiImplementation.getApiVersion();
        SyncApi api = apiVersion.getApi();
        RVApiDTO rvApiDto = new RVApiDTO();
        rvApiDto.setId(apiVersion.getId());
        rvApiDto.setApiName(api.getName());
        rvApiDto.setDescription(apiVersion.getDescription());
        rvApiDto.setVersion(apiVersion.getVersion());
        rvApiDto.setServiceId(apiImplementation.getService().getId());
        rvApiDto.setProduct(api.getProduct().getName());
        rvApiDto.setProductId(api.getProduct().getId());
        rvApiDto.setModality(apiImplementation.getApiModality().name());
        // consumedApis by Api is ONLY supported for syncApis
        rvApiDto.setConsumedApis(mapConsumedApis(apiImplementation.getConsumedApis()));
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

    private RVApiConsumedBy[] mapConsumedApis(List<SyncApiVersion> consumedApis)
    {
        List<RVApiConsumedBy> consumedApiDtos = new ArrayList<>();
        for (SyncApiVersion apiVersion : consumedApis)
        {
            SyncApi api = apiVersion.getApi();
            RVApiConsumedBy dto = new RVApiConsumedBy();
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
        return consumedApiDtos.toArray(new RVApiConsumedBy[0]);
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.SYNC == modality;
    }
}
