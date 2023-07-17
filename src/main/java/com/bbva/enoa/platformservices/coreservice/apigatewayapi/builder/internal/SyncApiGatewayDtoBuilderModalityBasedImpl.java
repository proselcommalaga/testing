package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.internal;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMServedApiDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMServedEndpointDto;
import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils.AGMServiceDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionServiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

@Component
@AllArgsConstructor
public class SyncApiGatewayDtoBuilderModalityBasedImpl implements IApiGatewayDtoBuilderModalityBased<SyncApi, SyncApiVersion, SyncApiImplementation>
{
    private final SecurizableApiGatewayDtoBuilderCommon securizableApiGatewayDtoBuilderCommon;
    private final ReleaseVersionServiceRepository releaseVersionServiceRepo;

    @Override
    public void addServedApiToServiceDto(final SyncApiImplementation servedApiImplementation, final AGMServiceDtoBuilder serviceDTOBuilder)
    {
        serviceDTOBuilder.addServedApi(this.buildServedApiDto(servedApiImplementation.getApiVersion()));
        if (!servedApiImplementation.getBackwardCompatibleApis().isEmpty())
        {
            servedApiImplementation.getBackwardCompatibleApis()
                    .forEach(backwardApiVersion -> serviceDTOBuilder.addServedApi(buildServedApiDto(backwardApiVersion)));
        }
    }

    private AGMServedApiDTO buildServedApiDto(final SyncApiVersion apiVersion)
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
    public void addConsumedApiToServiceDto(final SyncApiVersion consumedApiVersion, final String configuredDeploymentInfrastructure, final String environment, final AGMServiceDtoBuilder serviceDTOBuilder)
    {
        if (ApiType.EXTERNAL == consumedApiVersion.getApi().getType())
        {
            serviceDTOBuilder.addExternalConsumedApis(this.securizableApiGatewayDtoBuilderCommon.buildExternalConsumedApi(consumedApiVersion));
        }
        else
        {
            List<ReleaseVersionService> releaseVersionServiceList = this.releaseVersionServiceRepo.findAllDeployedImplementations(consumedApiVersion.getId());
            for (ReleaseVersionService releaseVersionService : releaseVersionServiceList)
            {
                if (this.securizableApiGatewayDtoBuilderCommon.isServiceConfiguredToBeDeployedOnInfrastructure(configuredDeploymentInfrastructure, releaseVersionService, environment))
                {
                    serviceDTOBuilder.addConsumedApis(this.securizableApiGatewayDtoBuilderCommon.buildConsumedApi(consumedApiVersion, releaseVersionService, configuredDeploymentInfrastructure, environment));
                }
                else
                {
                    serviceDTOBuilder.addExternalNovaConsumedApis(this.securizableApiGatewayDtoBuilderCommon.buildNovaExternalConsumedApi(consumedApiVersion, releaseVersionService));
                }
            }
        }
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.SYNC == modality;
    }
}
