package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMPublicationDetailDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMServiceDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.api.entities.ApiMethod;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.internal.IApiGatewayDtoBuilderModalityBased;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.ApiGatewayUtils;
import com.bbva.enoa.platformservices.coreservice.common.model.PublicationParams;
import com.bbva.enoa.platformservices.coreservice.common.model.ServicePublicationParams;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ApiGatewayDtoBuilderUtils
{
    private final IApiGatewayDtoBuilderModalityBased apiGatewayDtoBuilderModalityBased;
    private final ApiGatewayUtils utils;

    public void fillPublicationDetailInfo(final AGMPublicationDetailDTO publicationDetailDTO, final Platform platform, final ReleaseVersion releaseVersion)
    {
        if (platform != null)
        {
            publicationDetailDTO.setPlatform(platform.name());
        }

        if (releaseVersion != null)
        {
            publicationDetailDTO.setRelease(releaseVersion.getRelease().getName());

            if (releaseVersion.getRelease().getProduct() != null)
            {
                publicationDetailDTO.setUuaa(releaseVersion.getRelease().getProduct().getUuaa());
            }
        }
    }

    public String buildProfilingResourceValue(String releaseName, ApiMethod apiMethod)
    {

        return String.format("%s:%s:%s%s",
                releaseName,
                apiMethod.getVerb().getVerb(),
                apiMethod.getSecurizableApiVersion().getSecurizableApi().getBasePathSwagger(),
                apiMethod.getEndpoint()
        );
    }

    public List<DeploymentService> getAllBatchesFromPlan(final DeploymentPlan newPlan)
    {
        return newPlan.getDeploymentSubsystems().stream()
                .flatMap(deploymentSubsystem -> deploymentSubsystem.getDeploymentServices().stream())
                .filter(deploymentService -> ServiceType.valueOf(deploymentService.getService().getServiceType()).isBatch()
                        && ServiceType.valueOf(deploymentService.getService().getServiceType()).isMicrogateway())
                .collect(Collectors.toList());
    }

    public Optional<AGMServiceDTO> buildServiceDto(final PublicationParams publicationParams, final ServicePublicationParams servicePublicationParams)
    {
        if (servicePublicationParams == null)
        {
            return Optional.empty();
        }

        AGMServiceDTO serviceDTO = null;
        if (ServiceType.valueOf(servicePublicationParams.getServiceType()).isMicrogateway())
        {
            serviceDTO = this._buildServiceDto(servicePublicationParams,
                    publicationParams.getPlatform().name(), publicationParams.getReleaseName(), publicationParams.getEnvironment());
        }
        return Optional.ofNullable(serviceDTO);
    }


    private AGMServiceDTO _buildServiceDto(final ServicePublicationParams servicePublicationParams, final String deploymentMode, final String releaseName, final String environment)
    {

        AGMServiceDtoBuilder serviceDTOBuilder = AGMServiceDtoBuilder.getInstance(
                this.utils.buildServiceLocationByParams(servicePublicationParams.getGroupId(), servicePublicationParams.getArtifactId(), servicePublicationParams.getVersion(), deploymentMode, releaseName, environment),
                servicePublicationParams.getVersionServiceId(),
                deploymentMode
        );

        servicePublicationParams.getServedApisList().forEach(
                ai -> this.apiGatewayDtoBuilderModalityBased.addServedApiToServiceDto(ai, serviceDTOBuilder)
        );

        servicePublicationParams.getConsumedApiList().forEach(
                av -> this.apiGatewayDtoBuilderModalityBased.addConsumedApiToServiceDto(av, deploymentMode, environment, serviceDTOBuilder)
        );

        return serviceDTOBuilder.build();
    }
}
