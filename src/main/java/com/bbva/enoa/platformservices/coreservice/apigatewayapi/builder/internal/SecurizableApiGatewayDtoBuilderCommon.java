package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.internal;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMConsumedApiDTO;
import com.bbva.enoa.datamodel.model.api.entities.ISecurizableApiVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.ApiGatewayUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@AllArgsConstructor
class SecurizableApiGatewayDtoBuilderCommon
{
    private final ApiGatewayUtils utils;

    AGMConsumedApiDTO buildConsumedApi(final ISecurizableApiVersion<?, ?, ?> consumedApiVersion, final ReleaseVersionService releaseVersionService, final String deploymentMode, final String environment)
    {
        AGMConsumedApiDTO consumedApiDTO = new AGMConsumedApiDTO();
        consumedApiDTO.setConsumedPath(consumedApiVersion.getBasePathXmas());
        consumedApiDTO.setLocation(this.utils.buildServiceLocation(releaseVersionService, deploymentMode, releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getName(), environment));
        return this.fillCommonNovaConsumedApiInfo(consumedApiVersion, releaseVersionService, consumedApiDTO);
    }

    AGMConsumedApiDTO buildNovaExternalConsumedApi(final ISecurizableApiVersion<?, ?, ?> consumedExternalApiVersion, final ReleaseVersionService releaseVersionService)
    {
        AGMConsumedApiDTO consumedApiDTO = new AGMConsumedApiDTO();
        consumedApiDTO.setConsumedPath(consumedExternalApiVersion.getBasePathXmas());
        consumedApiDTO.setLocation(consumedExternalApiVersion.getBasePathXmas());
        // swagger string to Base64
        String originalInput = consumedExternalApiVersion.getDefinitionFile().getContents();
        String encodedStringSwagger = Base64.getEncoder().encodeToString(originalInput.getBytes());
        consumedApiDTO.setSwaggerContent(encodedStringSwagger);
        return this.fillCommonNovaConsumedApiInfo(consumedExternalApiVersion, releaseVersionService, consumedApiDTO);
    }

    AGMConsumedApiDTO buildExternalConsumedApi(ISecurizableApiVersion<?, ?, ?> externalApiVersion)
    {
        AGMConsumedApiDTO consumedApiDTO = new AGMConsumedApiDTO();
        consumedApiDTO.setConsumedPath(externalApiVersion.getBasePathXmas());
        consumedApiDTO.setLocation(externalApiVersion.getBasePathXmas());
        consumedApiDTO.setBasepath(externalApiVersion.getSecurizableApi().getBasePathSwagger());
        consumedApiDTO.setApiVersion(externalApiVersion.getVersion());
        consumedApiDTO.setApiName(externalApiVersion.getSecurizableApi().getName());
        consumedApiDTO.setUuaa(externalApiVersion.getSecurizableApi().getUuaa());
        return consumedApiDTO;
    }

    private AGMConsumedApiDTO fillCommonNovaConsumedApiInfo(final ISecurizableApiVersion<?, ?, ?> apiVersion, final ReleaseVersionService releaseVersionService, final AGMConsumedApiDTO consumedApiDTO)
    {
        consumedApiDTO.setRelease(releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getName());
        consumedApiDTO.setBasepath(apiVersion.getSecurizableApi().getBasePathSwagger());
        consumedApiDTO.setApiVersion(apiVersion.getVersion());
        consumedApiDTO.setUuaa(apiVersion.getSecurizableApi().getUuaa());
        consumedApiDTO.setApiName(apiVersion.getSecurizableApi().getName());
        return consumedApiDTO;
    }

    boolean isServiceConfiguredToBeDeployedOnInfrastructure(final String expectedInfrastucture, final ReleaseVersionService releaseVersionService, final String environment)
    {
        String configuredInfrastructure;
        switch (environment)
        {
            case "INT":
                configuredInfrastructure = releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getSelectedDeployInt().name();
                break;
            case "PRE":
                configuredInfrastructure = releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getSelectedDeployPre().name();
                break;
            default:
                configuredInfrastructure = releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getSelectedDeployPro().name();
                break;
        }

        return StringUtils.equalsIgnoreCase(expectedInfrastucture, configuredInfrastructure);

    }

}
