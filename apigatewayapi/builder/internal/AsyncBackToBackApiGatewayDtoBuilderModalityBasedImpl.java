package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.internal;

import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils.AGMServiceDtoBuilder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class AsyncBackToBackApiGatewayDtoBuilderModalityBasedImpl implements
        IApiGatewayDtoBuilderModalityBased<AsyncBackToBackApi, AsyncBackToBackApiVersion, AsyncBackToBackApiImplementation>
{
    @Override
    public void addServedApiToServiceDto(final AsyncBackToBackApiImplementation servedApiImplementation, final AGMServiceDtoBuilder serviceDTOBuilder)
    {
        //do nothing
    }

    @Override
    public void addConsumedApiToServiceDto(final AsyncBackToBackApiVersion consumedApiVersion, final String deploymentMode, final String environment, final AGMServiceDtoBuilder serviceDTOBuilder)
    {
        //do nothing
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.ASYNC_BACKTOBACK == modality;
    }
}
