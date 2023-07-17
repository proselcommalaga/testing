package com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder.internal;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPlanDto;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Primary
public class ResolverDtoBuilderModalityBasedImpl<A extends Api<A, AV, AI>, AV extends ApiVersion<A, AV, AI>, AI extends ApiImplementation<A, AV, AI>>
        implements IDtoBuilderModalityBased<A, AV, AI>
{

    private final Map<ApiModality, IDtoBuilderModalityBased<A, AV, AI>> dtoBuilderMap = new HashMap<>();

    @Autowired
    public ResolverDtoBuilderModalityBasedImpl(
            final List<IDtoBuilderModalityBased<A, AV, AI>> dtoBuilderList)
    {
        for (IDtoBuilderModalityBased<A, AV, AI> dtoBuilder : dtoBuilderList)
        {
            ApiModality modality = Arrays.stream(ApiModality.values())
                    .filter(dtoBuilder::isModalitySupported)
                    .findAny().orElse(null);
            if (modality != null)
            {
                dtoBuilderMap.put(modality, dtoBuilder);
            }
        }
    }

    @Override
    public ApiPlanDto buildApiPlanDto(final AV apiVersion, final String releaseName, final PlanProfile planProfile)
    {
        return this.getModalityBasedApiDetailDtoBuilder(apiVersion.getApiModality())
                .buildApiPlanDto(apiVersion, releaseName, planProfile);
    }

    @Override
    public ApiDetailDto buildApiDetailDto(final A api)
    {
        return this.getModalityBasedApiDetailDtoBuilder(api.getApiModality())
                .buildApiDetailDto(api);
    }

    @Override
    public boolean isModalitySupported(ApiModality modality)
    {
        return dtoBuilderMap.containsKey(modality);
    }

    private IDtoBuilderModalityBased<A, AV, AI> getModalityBasedApiDetailDtoBuilder(final ApiModality modality)
    {
        return Optional.ofNullable(this.dtoBuilderMap.get(modality))
                .orElseThrow(() -> new NotImplementedException(String.format("There is no an IApiDetailDtoBuilderModalityBased implemented for the modality %s", modality)));
    }
}
