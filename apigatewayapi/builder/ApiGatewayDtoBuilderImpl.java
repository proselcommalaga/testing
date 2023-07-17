package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.*;
import com.bbva.enoa.core.novabootstarter.util.ServiceNamingUtils;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorService;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.profile.entities.CesRole;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils.ApiGatewayDtoBuilderUtils;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.ApiGatewayUtils;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.EtherUtils;
import com.bbva.enoa.platformservices.coreservice.common.model.PublicationParams;
import com.bbva.enoa.platformservices.coreservice.common.model.ServicePublicationParams;
import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.Constants.ENTITY_TYPE_BEHAVIOR;
import static com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.Constants.ENTITY_TYPE_DEPLOYMENT;

/**
 * Class that builds Dtos to communicate with API Gateway Services
 */
@Service
@AllArgsConstructor
public class ApiGatewayDtoBuilderImpl implements IApiGatewayDtoBuilder
{

    private static final Logger LOG = LoggerFactory.getLogger(ApiGatewayDtoBuilderImpl.class);
    private final EtherUtils etherUtils;
    private final ApiGatewayUtils utils;
    private final ApiGatewayDtoBuilderUtils builderUtils;

    @Override
    @Transactional
    public AGMCreatePublicationDTO buildCreatePublicationDto(final PublicationParams publicationParams)
    {
        LOG.debug("[{}] -> [buildCreatePublicationDto]: Begin {}: [{}]", Constants.API_DTO_BUILDER_IMPL, publicationParams.getEntityType().toLowerCase(), publicationParams.getId());

        AGMCreatePublicationDTO createPublicationDTO = new AGMCreatePublicationDTO();
        this.builderUtils.fillPublicationDetailInfo(createPublicationDTO, publicationParams.getPlatform(), publicationParams.getReleaseVersion());

        List<AGMServiceDTO> swaggerServices = new ArrayList<>();

        for (ServicePublicationParams servicePublication : publicationParams.getServicePublicationParamsList())
        {
            // only microgateway services
            this.builderUtils.buildServiceDto(publicationParams, servicePublication).ifPresent(swaggerServices::add);
        }

        createPublicationDTO.setServices(swaggerServices.toArray(AGMServiceDTO[]::new));

        LOG.debug("[{}] -> [buildApiPublicationDto]: End {} [{}]", Constants.API_DTO_BUILDER_IMPL, publicationParams.getEntityType().toLowerCase(), publicationParams.getId());

        return createPublicationDTO;
    }

    @Override
    public AGMRemovePublicationDTO buildRemovePublicationDetailDto(DeploymentPlan deploymentPlan)
    {

        AGMRemovePublicationDTO removePublicationDTO = new AGMRemovePublicationDTO();
        this.builderUtils.fillPublicationDetailInfo(removePublicationDTO, deploymentPlan.getSelectedDeploy(), deploymentPlan.getReleaseVersion());

        return removePublicationDTO;
    }

    @Override
    @Transactional
    public AGMUpdatePublicationDTO buildUpdatePublicationDto(final List<DeploymentService> createdServices, final List<DeploymentService> removedServices, final DeploymentPlan newPlan, final DeploymentPlan oldPlan)
    {

        LOG.debug("[{}] -> [buildUpdatePublicationDto]: Begin deploymentPlan: [{}]", Constants.API_DTO_BUILDER_IMPL, newPlan.getId());

        AGMUpdatePublicationDTO updatePublicationDTO = new AGMUpdatePublicationDTO();
        // This is done for DRY
        this.builderUtils.fillPublicationDetailInfo(updatePublicationDTO, newPlan.getSelectedDeploy(), newPlan.getReleaseVersion());

        List<DeploymentService> batchServicesCreated = this.builderUtils.getAllBatchesFromPlan(newPlan);
        for (DeploymentService batchServiceCreated : batchServicesCreated)
        {
            if (!createdServices.contains(batchServiceCreated))
            {
                createdServices.add(batchServiceCreated);
            }
        }

        List<DeploymentService> batchServicesRemoved = this.builderUtils.getAllBatchesFromPlan(oldPlan);
        for (DeploymentService batchServiceRemoved : batchServicesRemoved)
        {
            if (!removedServices.contains(batchServiceRemoved))
            {
                removedServices.add(batchServiceRemoved);
            }
        }

        List<AGMServiceDTO> swaggerServices = new ArrayList<>();
        // Process for each service the configuration
        for (DeploymentService deploymentService : createdServices)
        {
            // only microgateway services
            this.builderUtils.buildServiceDto(this.buildPublicationParams(newPlan), this.buildServicePublicationParams(deploymentService.getService())).ifPresent(swaggerServices::add);
        }

        updatePublicationDTO.setAddedServices(swaggerServices.toArray(AGMServiceDTO[]::new));
        updatePublicationDTO.setRemovedServicesIds(removedServices.stream().mapToInt(x -> x.getService().getId()).toArray());
        updatePublicationDTO.setRemovedServicesLocation(
                removedServices.stream()
                        .map(x -> this.utils.buildServiceLocation(
                                x.getService(),
                                newPlan.getSelectedDeploy().name(),
                                newPlan.getReleaseVersion().getRelease().getName(),
                                newPlan.getEnvironment())
                        ).toArray(String[]::new));

        LOG.debug("[{}] -> [buildApiPublicationDto]: End: [{}]", Constants.API_DTO_BUILDER_IMPL, newPlan.getId());

        return updatePublicationDTO;
    }

    @Override
    @Transactional
    public AGMDockerServiceDTO buildDockerServiceDto(final DeploymentService deploymentService)
    {

        AGMDockerServiceDTO dockerServiceDto = new AGMDockerServiceDTO();

        dockerServiceDto.setUuaa(deploymentService.getService().getVersionSubsystem().getReleaseVersion().getRelease().getProduct().getUuaa());
        dockerServiceDto.setReleaseName(
                deploymentService.getService().getVersionSubsystem().getReleaseVersion().getRelease().getName());

        if (Platform.NOVA.name().equals(deploymentService.getDeploymentSubsystem().getDeploymentPlan().getSelectedDeploy().name()))
        {
            dockerServiceDto
                    .setServiceName(ServiceNamingUtils.getNovaServiceName(deploymentService.getService().getGroupId(),
                            deploymentService.getService().getArtifactId(), deploymentService.getDeploymentSubsystem()
                                    .getSubsystem().getReleaseVersion().getRelease().getName(),
                            deploymentService.getService().getVersion().split("\\.")[0]));
            dockerServiceDto.setPlatform(Platform.NOVA.name());
        }
        else
        {
            dockerServiceDto.setServiceName(this.etherUtils.getEAEServiceURL(
                    deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment(),
                    deploymentService.getService().getGroupId(), deploymentService.getService().getArtifactId(),
                    deploymentService.getService().getVersionSubsystem().getReleaseVersion().getRelease().getName(), deploymentService.getService().getVersion()));
            dockerServiceDto.setPlatform(Platform.ETHER.name());
        }

        return dockerServiceDto;
    }

    @Override
    @Transactional
    public AGMDockerServiceDTO buildDockerServiceDto(final BehaviorService behaviorService)
    {

        AGMDockerServiceDTO dockerServiceDto = new AGMDockerServiceDTO();

        dockerServiceDto.setUuaa(behaviorService.getReleaseVersion().getRelease().getProduct().getUuaa());
        dockerServiceDto.setReleaseName(behaviorService.getReleaseVersion().getRelease().getName());

        dockerServiceDto
                .setServiceName(ServiceNamingUtils.getNovaServiceName(behaviorService.getGroupId(),
                        behaviorService.getArtifactId(), behaviorService.getReleaseVersion().getRelease().getName(),
                        behaviorService.getVersion().split("\\.")[0]));
        dockerServiceDto.setPlatform(Platform.NOVA.name());


        return dockerServiceDto;
    }

    @Override
    public AGMCreateProfilingDTO buildCreateProfilingDto(final DeploymentPlan deploymentPlan)
    {

        Release release = deploymentPlan.getReleaseVersion().getRelease();

        AGMCreateProfilingDTO createProfilingDTO = new AGMCreateProfilingDTO();
        createProfilingDTO.setUuaa(release.getProduct().getUuaa());
        createProfilingDTO.setProfilingInfo(
                deploymentPlan.getPlanProfiles().stream()
                        .flatMap(planProfile -> planProfile.getApiMethodProfiles().stream())
                        .filter(apiMethodProfile -> apiMethodProfile.getRoles() != null && !apiMethodProfile.getRoles().isEmpty())
                        .map(apiMethodProfile -> {
                            String serviceName = this.builderUtils.buildProfilingResourceValue(
                                    release.getName(), apiMethodProfile.getApiMethod()
                            );
                            AGMProfilingInfoDTO infoDTO = new AGMProfilingInfoDTO();
                            infoDTO.setName(serviceName);
                            infoDTO.setValue(serviceName);
                            infoDTO.setRoles(apiMethodProfile.getRoles().stream().map(CesRole::getRol).toArray(String[]::new));
                            return infoDTO;
                        })
                        .toArray(AGMProfilingInfoDTO[]::new)
        );
        return createProfilingDTO;
    }

    @Override
    public AGMRemoveProfilingDTO buildRemoveProfilingDto(final DeploymentPlan deploymentPlan)
    {

        Release release = deploymentPlan.getReleaseVersion().getRelease();

        AGMRemoveProfilingDTO removeProfilingDTO = new AGMRemoveProfilingDTO();
        removeProfilingDTO.setUuaa(release.getProduct().getUuaa());
        removeProfilingDTO.setResources(
                deploymentPlan.getPlanProfiles().stream()
                        .flatMap(planProfile -> planProfile.getApiMethodProfiles().stream())
                        .filter(apiMethodProfile -> apiMethodProfile.getRoles() != null && !apiMethodProfile.getRoles().isEmpty())
                        .map(apiMethodProfile -> {
                            String serviceName = this.builderUtils.buildProfilingResourceValue(
                                    release.getName(), apiMethodProfile.getApiMethod()
                            );
                            AGMProfilingResourceDTO resourceDTO = new AGMProfilingInfoDTO();
                            resourceDTO.setName(serviceName);
                            resourceDTO.setValue(serviceName);
                            return resourceDTO;
                        })
                        .toArray(AGMProfilingResourceDTO[]::new)
        );
        return removeProfilingDTO;
    }

    @Override
    public PublicationParams buildPublicationParams(final DeploymentPlan deploymentPlan)
    {
        PublicationParams publicationParams = new PublicationParams();

        publicationParams.setId(deploymentPlan.getId());
        publicationParams.setPlatform(deploymentPlan.getSelectedDeploy());
        publicationParams.setReleaseName(deploymentPlan.getReleaseVersion().getRelease().getName());
        publicationParams.setEnvironment(deploymentPlan.getEnvironment());
        publicationParams.setReleaseVersion(deploymentPlan.getReleaseVersion());
        publicationParams.setEntityType(ENTITY_TYPE_DEPLOYMENT);

        // Process for each service the configuration
        List<ServicePublicationParams> servicePublicationList = deploymentPlan.getReleaseVersion().getSubsystems().stream()
                .flatMap(releaseVersionSubsystem -> releaseVersionSubsystem.getServices().stream())
                .map(this::buildServicePublicationParams).collect(Collectors.toList());

        publicationParams.setServicePublicationParamsList(servicePublicationList);

        return publicationParams;
    }

    @Override
    public PublicationParams buildPublicationParams(final BehaviorService behaviorService)
    {
        PublicationParams publicationParams = new PublicationParams();

        publicationParams.setId(behaviorService.getId());
        publicationParams.setPlatform(Platform.NOVA);
        publicationParams.setReleaseName(behaviorService.getReleaseVersion().getRelease().getName());
        publicationParams.setEnvironment(Environment.PRE.name());
        publicationParams.setReleaseVersion(behaviorService.getReleaseVersion());
        publicationParams.setEntityType(ENTITY_TYPE_BEHAVIOR);

        publicationParams.setServicePublicationParamsList(this.buildServicePublicationParamsList(behaviorService));

        return publicationParams;
    }

    @Override
    public List<ServicePublicationParams> buildServicePublicationParamsList(final BehaviorService behaviorService)
    {
        return List.of(this.buildServicePublicationParams(behaviorService));
    }


    private ServicePublicationParams buildServicePublicationParams(final ReleaseVersionService releaseVersionService)
    {
        ServicePublicationParams servicePublicationParams = new ServicePublicationParams();

        servicePublicationParams.setVersionServiceId(releaseVersionService.getId());
        servicePublicationParams.setGroupId(releaseVersionService.getGroupId());
        servicePublicationParams.setArtifactId(releaseVersionService.getArtifactId());
        servicePublicationParams.setVersion(releaseVersionService.getVersion());
        servicePublicationParams.setServiceType(releaseVersionService.getServiceType());
        servicePublicationParams.setServedApisList(releaseVersionService.getServers());
        servicePublicationParams.setConsumedApiList(releaseVersionService.getAllConsumedApiVersions());

        return servicePublicationParams;
    }

    private ServicePublicationParams buildServicePublicationParams(final ReleaseVersionService releaseVersionService, final DeploymentPlan deploymentPlan)
    {
        ServicePublicationParams servicePublicationParams = new ServicePublicationParams();

        servicePublicationParams.setVersionServiceId(releaseVersionService.getId());
        servicePublicationParams.setGroupId(releaseVersionService.getGroupId());
        servicePublicationParams.setArtifactId(releaseVersionService.getArtifactId());
        servicePublicationParams.setVersion(releaseVersionService.getVersion());
        servicePublicationParams.setServiceType(releaseVersionService.getServiceType());
        servicePublicationParams.setSelectDeploy(deploymentPlan.getSelectedDeploy());
        servicePublicationParams.setReleaseName(deploymentPlan.getReleaseVersion().getRelease().getName());
        servicePublicationParams.setProductUuaa(deploymentPlan.getReleaseVersion().getRelease().getProduct().getUuaa());
        servicePublicationParams.setEnvironment(deploymentPlan.getEnvironment());
        servicePublicationParams.setServedApisList(releaseVersionService.getServers());
        servicePublicationParams.setConsumedApiList(releaseVersionService.getAllConsumedApiVersions());

        return servicePublicationParams;
    }

    private ServicePublicationParams buildServicePublicationParams(final BehaviorService behaviorService)
    {
        ServicePublicationParams servicePublicationParams = new ServicePublicationParams();

        servicePublicationParams.setVersionServiceId(behaviorService.getId());
        servicePublicationParams.setGroupId(behaviorService.getGroupId());
        servicePublicationParams.setArtifactId(behaviorService.getArtifactId());
        servicePublicationParams.setVersion(behaviorService.getVersion());
        servicePublicationParams.setServiceType(behaviorService.getServiceType());
        servicePublicationParams.setSelectDeploy(Platform.NOVA);
        servicePublicationParams.setReleaseName(behaviorService.getReleaseVersion().getRelease().getName());
        servicePublicationParams.setProductUuaa(behaviorService.getReleaseVersion().getRelease().getProduct().getUuaa());
        servicePublicationParams.setEnvironment(Environment.PRE.name());
        servicePublicationParams.setConsumedApiList(behaviorService.getConsumers().stream().map(ApiImplementation::getApiVersion).collect(Collectors.toList()));
        servicePublicationParams.setServedApisList(new ArrayList<>());

        return servicePublicationParams;
    }

}
