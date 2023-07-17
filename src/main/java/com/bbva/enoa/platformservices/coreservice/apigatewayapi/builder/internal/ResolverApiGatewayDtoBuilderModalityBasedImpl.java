package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.internal;

import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils.AGMServiceDtoBuilder;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Primary
public class ResolverApiGatewayDtoBuilderModalityBasedImpl<A extends Api<A, AV, AI>, AV extends ApiVersion<A, AV, AI>, AI extends ApiImplementation<A, AV, AI>>
        implements IApiGatewayDtoBuilderModalityBased<A, AV, AI>
{
    private final Map<ApiModality, IApiGatewayDtoBuilderModalityBased<A, AV, AI>> dtoBuilderMap = new HashMap<>();

    @Autowired
    public ResolverApiGatewayDtoBuilderModalityBasedImpl(List<IApiGatewayDtoBuilderModalityBased<A, AV, AI>> dtoBuilders)
    {
        for (IApiGatewayDtoBuilderModalityBased<A, AV, AI> dtoBuilder : dtoBuilders)
        {
            Arrays.stream(ApiModality.values())
                    .filter(dtoBuilder::isModalitySupported)
                    .findAny()
                    .ifPresent(modality -> this.dtoBuilderMap.put(modality, dtoBuilder));
        }
    }

    @Override
    public void addServedApiToServiceDto(final AI servedApiImplementation, final AGMServiceDtoBuilder serviceDTOBuilder)
    {
        this.getModalityBasedDtoBuilder(servedApiImplementation.getApiModality())
                .addServedApiToServiceDto(servedApiImplementation, serviceDTOBuilder);
    }

    @Override
    public void addConsumedApiToServiceDto(final AV consumedApiVersion, final String deploymentMode, final String environment, final AGMServiceDtoBuilder serviceDTOBuilder)
    {
        this.getModalityBasedDtoBuilder(consumedApiVersion.getApiModality())
                .addConsumedApiToServiceDto(consumedApiVersion, deploymentMode, environment, serviceDTOBuilder);
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return this.dtoBuilderMap.containsKey(modality);
    }

    private IApiGatewayDtoBuilderModalityBased<A, AV, AI> getModalityBasedDtoBuilder(final ApiModality modality)
    {
        return Optional.ofNullable(this.dtoBuilderMap.get(modality))
                .orElseThrow(() -> new NotImplementedException(String.format("There is no an IApiGatewayDtoBuilderModalityBased implemented for the modality %s", modality)));
    }
}
