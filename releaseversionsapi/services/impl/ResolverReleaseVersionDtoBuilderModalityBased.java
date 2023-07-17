package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiDTO;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionDtoBuilderModalityBased;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Primary
public class ResolverReleaseVersionDtoBuilderModalityBased<A extends Api<A, AV, AI>, AV extends ApiVersion<A, AV, AI>, AI extends ApiImplementation<A, AV, AI>>
        implements IReleaseVersionDtoBuilderModalityBased<A, AV, AI>
{

    private final Map<ApiModality, IReleaseVersionDtoBuilderModalityBased<A, AV, AI>> dtoBuilderMap = new HashMap<>();

    @Autowired
    public ResolverReleaseVersionDtoBuilderModalityBased(
            final List<IReleaseVersionDtoBuilderModalityBased<A, AV, AI>> dtoBuilders
    )
    {
        for (IReleaseVersionDtoBuilderModalityBased<A, AV, AI> dtoBuilder : dtoBuilders)
        {
            Arrays.stream(ApiModality.values())
                    .filter(dtoBuilder::isModalitySupported)
                    .findAny()
                    .ifPresent(modality -> this.dtoBuilderMap.put(modality, dtoBuilder));
        }
    }

    @Override
    public RVApiDTO buildRVApiDTO(AI apiImplementation)
    {
        return this.getModalityBasedDtoBuilder(apiImplementation.getApiModality())
                .buildRVApiDTO(apiImplementation);
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return this.dtoBuilderMap.containsKey(modality);
    }

    private IReleaseVersionDtoBuilderModalityBased<A, AV, AI> getModalityBasedDtoBuilder(final ApiModality modality)
    {
        return Optional.ofNullable(this.dtoBuilderMap.get(modality))
                .orElseThrow(() -> new NotImplementedException(String.format("There is no an IReleaseVersionDtoBuilderModalityBased implemented for the modality %s", modality)));
    }
}
