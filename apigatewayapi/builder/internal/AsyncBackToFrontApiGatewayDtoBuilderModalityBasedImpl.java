package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.internal;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMServedApiDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMServedEndpointDto;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils.AGMServiceDtoBuilder;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@AllArgsConstructor
public class AsyncBackToFrontApiGatewayDtoBuilderModalityBasedImpl implements
        IApiGatewayDtoBuilderModalityBased<AsyncBackToFrontApi, AsyncBackToFrontApiVersion, AsyncBackToFrontApiImplementation>
{
    @Override
    public void addServedApiToServiceDto(final AsyncBackToFrontApiImplementation servedApiImplementation, final AGMServiceDtoBuilder serviceDTOBuilder)
    {
        serviceDTOBuilder.addAsyncApi(buildServedApiDto(servedApiImplementation.getApiVersion()));
    }

    private AGMServedApiDTO buildServedApiDto(final AsyncBackToFrontApiVersion apiVersion)
    {
        AGMServedApiDTO servedApiDTO = new AGMServedApiDTO();

        servedApiDTO.setUuaa(apiVersion.getApi().getUuaa());
        servedApiDTO.setApiName(apiVersion.getApi().getName());
        servedApiDTO.setApiVersion(apiVersion.getVersion());
        servedApiDTO.setBasepath(apiVersion.getApi().getBasePathSwagger());

        servedApiDTO.setEndpoints(apiVersion.getApiMethods().stream().map(x -> {
            AGMServedEndpointDto endpointDto = new AGMServedEndpointDto();
            endpointDto.setMethod(x.getVerb().getVerb());
            endpointDto.setPath(x.getEndpoint());
            return endpointDto;
        }).toArray(AGMServedEndpointDto[]::new));

        // swagger string to Base64
        String originalInput = apiVersion.getDefinitionFile().getContents();
        String encodedStringSwagger = Base64.getEncoder().encodeToString(originalInput.getBytes());
        servedApiDTO.setDefinitionFileContent(encodedStringSwagger);

        return servedApiDTO;
    }

    @Override
    public void addConsumedApiToServiceDto(final AsyncBackToFrontApiVersion consumedApiVersion, final String deploymentMode, final String environment, final AGMServiceDtoBuilder serviceDTOBuilder)
    {
        //do nothing
    }


    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.ASYNC_BACKTOFRONT == modality;
    }
}
