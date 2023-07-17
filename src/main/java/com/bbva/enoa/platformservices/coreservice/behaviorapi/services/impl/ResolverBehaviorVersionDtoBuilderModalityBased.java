package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceApiDTO;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorVersionDtoBuilderModalityBased;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Primary
public class ResolverBehaviorVersionDtoBuilderModalityBased<A extends Api<A, AV, AI>, AV extends ApiVersion<A, AV, AI>, AI extends ApiImplementation<A, AV, AI>>
        implements IBehaviorVersionDtoBuilderModalityBased<A, AV, AI>
{

    private final Map<ApiModality, IBehaviorVersionDtoBuilderModalityBased<A, AV, AI>> dtoBuilderMap = new HashMap<>();

    @Autowired
    public ResolverBehaviorVersionDtoBuilderModalityBased(
            final List<IBehaviorVersionDtoBuilderModalityBased<A, AV, AI>> dtoBuilders
    )
    {
        for (IBehaviorVersionDtoBuilderModalityBased<A, AV, AI> dtoBuilder : dtoBuilders)
        {
            Arrays.stream(ApiModality.values())
                    .filter(dtoBuilder::isModalitySupported)
                    .findAny()
                    .ifPresent(modality -> this.dtoBuilderMap.put(modality, dtoBuilder));
        }
    }

    @Override
    public BVServiceApiDTO buildBVServiceApiDTO(AI apiImplementation)
    {
        return this.getModalityBasedDtoBuilder(apiImplementation.getApiModality())
                .buildBVServiceApiDTO(apiImplementation);
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return this.dtoBuilderMap.containsKey(modality);
    }

    private IBehaviorVersionDtoBuilderModalityBased<A, AV, AI> getModalityBasedDtoBuilder(final ApiModality modality)
    {
        return Optional.ofNullable(this.dtoBuilderMap.get(modality))
                .orElseThrow(() -> new NotImplementedException(String.format("There is no an IReleaseVersionDtoBuilderModalityBased implemented for the modality %s", modality)));
    }
}
