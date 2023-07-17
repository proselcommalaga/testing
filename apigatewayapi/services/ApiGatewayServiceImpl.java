package com.bbva.enoa.platformservices.coreservice.apigatewayapi.services;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.*;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.util.ServiceNamingUtils;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorServiceConfiguration;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.IApiGatewayDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.exception.ApiGatewayError;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils.EtherUtils;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.validator.IApiGatewayValidator;
import com.bbva.enoa.platformservices.coreservice.common.model.PublicationParams;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BehaviorServiceConfigurationRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IApiGatewayManagerClient;
import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API Gateway services
 */
@Service
@AllArgsConstructor
public class ApiGatewayServiceImpl implements IApiGatewayService
{

    private static final Logger LOG = LoggerFactory.getLogger(ApiGatewayServiceImpl.class);
    private final IApiGatewayDtoBuilder apiGatewayServiceDtoBuilder;
    private final IApiGatewayValidator apiGatewayValidator;
    private final IApiGatewayManagerClient apiGatewayManagerClient;
    private final EtherUtils etherUtils;
    private final DeploymentPlanRepository deploymentPlanRepository;
    private final BehaviorServiceConfigurationRepository behaviorServiceConfigurationRepository;

    @Override
    public void createPublication(final DeploymentPlan deploymentPlan)
    {
        LOG.debug("[{}] -> [createPublication]: Starting to register plan with id: [{}]", Constants.API_GATEWAY_SERVICE, deploymentPlan.getId());

        PublicationParams publicationParams = this.apiGatewayServiceDtoBuilder.buildPublicationParams(deploymentPlan);
        AGMCreatePublicationDTO createPublicationDTO = this.apiGatewayServiceDtoBuilder.buildCreatePublicationDto(publicationParams);

        this.createPublication(createPublicationDTO, deploymentPlan.getEnvironment());

        LOG.debug("[{}] -> [createPublication]: Successfully published plan with id: [{}]", Constants.API_GATEWAY_SERVICE, deploymentPlan.getId());
    }

    @Override
    public void createPublication(final BehaviorServiceConfiguration bsConfiguration)
    {
        LOG.debug("[{}] -> [createPublication]: Starting to register plan with id: [{}]", Constants.API_GATEWAY_SERVICE, bsConfiguration.getId());

        PublicationParams publicationParams = this.apiGatewayServiceDtoBuilder.buildPublicationParams(bsConfiguration.getBehaviorService());
        AGMCreatePublicationDTO createPublicationDTO = this.apiGatewayServiceDtoBuilder.buildCreatePublicationDto(publicationParams);

        this.createPublication(createPublicationDTO, Environment.PRE.name());

        LOG.debug("[{}] -> [createPublication]: Successfully published plan with id: [{}]", Constants.API_GATEWAY_SERVICE, bsConfiguration.getId());
    }

    @Override
    public void removePublication(final DeploymentPlan deploymentPlan)
    {

        LOG.debug("[{}] -> [removePublication]: Starting to removing publication for plan with id: [{}]", Constants.API_GATEWAY_SERVICE, deploymentPlan.getId());

        // Transforms release version into ReleaseDetailDto
        AGMRemovePublicationDTO removePublicationDTO = this.apiGatewayServiceDtoBuilder.buildRemovePublicationDetailDto(deploymentPlan);

        // Call API Gateway client
        this.apiGatewayManagerClient.removePublication(removePublicationDTO, deploymentPlan.getEnvironment());

        LOG.debug("[{}] -> [removePublication]: Successfully removed publication for plan with id: [{}]", Constants.API_GATEWAY_SERVICE, deploymentPlan.getId());

    }

    @Override
    public void updatePublication(final List<DeploymentService> createdServices, final List<DeploymentService> removedServices, final DeploymentPlan newPlan, final DeploymentPlan oldPlan)
    {

        LOG.debug("[{}] -> [updatePublication]: Starting to replacing plan in environment [{}]. New plan id: [{}]", Constants.API_GATEWAY_SERVICE,
                newPlan.getEnvironment(), newPlan.getId());

        // Validate if there are services to register (batches always have to be redeployed)
        if (createdServices.isEmpty() && removedServices.isEmpty() && !this.hasPlanAnyBatchService(newPlan))
        {
            LOG.warn("[{}] -> [updatePublication]: there are not services to update.", Constants.API_GATEWAY_SERVICE);
        }
        else
        {
            AGMUpdatePublicationDTO updatePublicationDTO = this.apiGatewayServiceDtoBuilder.buildUpdatePublicationDto(createdServices, removedServices, newPlan, oldPlan);

            LOG.debug("[{}] -> [updatePublication]: AGMUpdatePublicationDTO object created: [{}], ready for call to updatePublication", Constants.API_GATEWAY_SERVICE, updatePublicationDTO);

            if (ArrayUtils.isNotEmpty(updatePublicationDTO.getAddedServices()) || ArrayUtils.isNotEmpty(updatePublicationDTO.getRemovedServicesIds()))
            {
                this.apiGatewayManagerClient.updatePublication(updatePublicationDTO, newPlan.getEnvironment());
            }

            LOG.debug("[{}] -> [updatePublication]: Successfully updated publication for plan with id: [{}]", Constants.API_GATEWAY_SERVICE, newPlan.getId());
        }

    }

    @Override
    public void generateDockerKey(final DeploymentPlan deploymentPlan)
    {
        // Generation of a set of DockerKey for each DeploymentSubsystem
        for (DeploymentSubsystem deploymentSubsystem : deploymentPlan.getDeploymentSubsystems())
        {
            this.generateDockerKey(deploymentSubsystem.getDeploymentServices(), deploymentPlan.getEnvironment());
        }

        //Saving deployment plan to avoid blank dockerKeys in services
        this.deploymentPlanRepository.saveAndFlush(deploymentPlan);
    }

    @Override
    public void generateDockerKey(final List<DeploymentService> deploymentServiceList, final String environment)
    {
        List<DeploymentService> filteredServiceList = deploymentServiceList.stream().filter(x -> ServiceType.valueOf(x.getService().getServiceType()).isMicrogateway()).collect(Collectors.toList());
        Map<String, DeploymentService> dockerKeyMap = new HashMap<>();
        for (DeploymentService deploymentService : filteredServiceList)
        {
            if (Platform.NOVA.name().equals(deploymentService.getDeploymentSubsystem().getDeploymentPlan().getSelectedDeploy().name()))
            {
                dockerKeyMap.put(
                        ServiceNamingUtils.getNovaServiceName(
                                deploymentService.getService().getGroupId(),
                                deploymentService.getService().getArtifactId(),
                                deploymentService.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getRelease().getName(),
                                deploymentService.getService().getVersion().split("\\.")[0])
                        , deploymentService);
            }
            else
            {
                ReleaseVersionService releaseVersionService = deploymentService.getService();
                dockerKeyMap.put(etherUtils.getEAEServiceURL(
                                deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment(),
                                releaseVersionService.getGroupId(), releaseVersionService.getArtifactId(),
                                releaseVersionService.getVersionSubsystem().getReleaseVersion().getRelease().getName(), releaseVersionService.getVersion())
                        , deploymentService);
            }
        }

        // Call to Apigateway client to request the the DockerKey set
        AGMDockerValidationDTO[] dockerValidationDtos = this.clientDockerKey(filteredServiceList, environment);
        if (dockerValidationDtos != null)
        {
            this.setDockerKeysToServices(dockerKeyMap, dockerValidationDtos);
        }

        LOG.debug("[DeploymentsApiService] -> [generateDockerKey]: docker key map: [{}]", dockerKeyMap);

    }

    @Override
    public void createProfiling(final DeploymentPlan deploymentPlan)
    {
        LOG.debug("[{}] -> [createProfiling]: Creating profiling at CES for plan id: [{}] in environment: [{}]", Constants.API_GATEWAY_SERVICE,
                deploymentPlan.getId(), deploymentPlan.getEnvironment());

        AGMCreateProfilingDTO createProfilingDTO = this.apiGatewayServiceDtoBuilder.buildCreateProfilingDto(deploymentPlan);

        if (createProfilingDTO.getProfilingInfo().length > 0)
        {
            this.apiGatewayManagerClient.createProfiling(createProfilingDTO, deploymentPlan.getEnvironment());
            LOG.debug("[{}] -> [createProfiling]: Successfully created profiling at CES for plan id: [{}] in environment: [{}]", Constants.API_GATEWAY_SERVICE,
                    deploymentPlan.getId(), deploymentPlan.getEnvironment());
        }
        else
        {
            LOG.debug("[{}] -> [createProfiling]: No roles associated to any resource for plan id: [{}] in environment: [{}]", Constants.API_GATEWAY_SERVICE,
                    deploymentPlan.getId(), deploymentPlan.getEnvironment());
        }


    }

    @Override
    public void removeProfiling(final DeploymentPlan deploymentPlan)
    {
        LOG.debug("[{}] -> [removeProfiling]: Removing profiling at CES for plan id: [{}] in environment: [{}]", Constants.API_GATEWAY_SERVICE,
                deploymentPlan.getId(), deploymentPlan.getEnvironment());

        AGMRemoveProfilingDTO removeProfilingDTO = this.apiGatewayServiceDtoBuilder.buildRemoveProfilingDto(deploymentPlan);

        if (removeProfilingDTO.getResources().length > 0)
        {
            this.apiGatewayManagerClient.removeProfiling(removeProfilingDTO, deploymentPlan.getEnvironment());
            LOG.debug("[{}] -> [removeProfiling]: Successfully removed profiling at CES for plan id: [{}] in environment: [{}]", Constants.API_GATEWAY_SERVICE,
                    deploymentPlan.getId(), deploymentPlan.getEnvironment());
        }
        else
        {
            LOG.debug("[{}] -> [removeProfiling]: No resources existed for plan id: [{}] in environment: [{}]", Constants.API_GATEWAY_SERVICE,
                    deploymentPlan.getId(), deploymentPlan.getEnvironment());
        }


    }


    /////////////////////// PRIVATE //////////////////////////////////

    private void createPublication(final AGMCreatePublicationDTO createPublicationDTO, final String environment)
    {
        LOG.debug("[{}] -> [createPublication]: Starting to register new version into API Gateway", Constants.API_GATEWAY_SERVICE);

        // Validate if there are services to register
        if (createPublicationDTO.getServices() == null || createPublicationDTO.getServices().length == 0)
        {
            LOG.warn("[{}] -> [createPublication]: there are not services with APIs to register. Any API has been registered.", Constants.API_GATEWAY_SERVICE);
        }
        else
        {
            LOG.debug("[{}] -> [createPublication]: AGMCreatePublicationDTO object created: [{}], ready for call to createPublication", Constants.API_GATEWAY_SERVICE, createPublicationDTO);

            this.apiGatewayManagerClient.createPublication(createPublicationDTO, environment);
        }
        LOG.debug("[{}] -> [createPublication]: Successfully published version in API Gateway", Constants.API_GATEWAY_SERVICE);
    }

    /**
     * Builds the DTO and calls the APIgateway client to generate the DockerKey set
     *
     * @param deploymentServiceList Deployment Service List of a Deployment Plan
     * @param environment           Environment - INT, PRE, PRO
     * @return DockerValidationDto[] list of keys for each service
     */
    private AGMDockerValidationDTO[] clientDockerKey(final List<DeploymentService> deploymentServiceList, final String environment)
    {

        AGMDockerValidationDTO[] dockerKeys = new AGMDockerValidationDTO[0];
        // Build list of DockerServiceDto
        List<AGMDockerServiceDTO> dockerServiceDtoList = new ArrayList<>();
        for (DeploymentService deploymentService : deploymentServiceList)
        {
            // Verify iF service has an API Type.
            if (this.apiGatewayValidator.checkReleaseVersionServiceHasMicroGateway(deploymentService.getService().getServiceType()))
            {
                dockerServiceDtoList.add(this.apiGatewayServiceDtoBuilder.buildDockerServiceDto(deploymentService));
            }
        }

        if (!dockerServiceDtoList.isEmpty())
        {
            // Call and return of the DockerKey set
            dockerKeys = this.apiGatewayManagerClient.generateDockerKey(dockerServiceDtoList.toArray(new AGMDockerServiceDTO[0]),
                    environment);
        }
        return dockerKeys;
    }

    /**
     * Sets Docker-keys into the final service.
     *
     * @param dockerKeyMap         Map Containing the service list.
     * @param dockerValidationDtos Array list with dockerkeys per service.
     */
    private void setDockerKeysToServices(final Map<String, DeploymentService> dockerKeyMap,
                                         final AGMDockerValidationDTO[] dockerValidationDtos)
    {
        // Sets the DockerKey for each service
        for (AGMDockerValidationDTO dockerValidationDto : dockerValidationDtos)
        {
            DeploymentService deploymentService = dockerKeyMap.get(dockerValidationDto.getServiceName());
            if (deploymentService == null)
            {
                throw new NovaException(ApiGatewayError.getUnexpectedServiceDockerKeyError());
            }
            else
            {
                deploymentService.setDockerKey(dockerValidationDto.getDockerKey());
            }
        }
    }

    private boolean hasPlanAnyBatchService(DeploymentPlan deploymentPlan)
    {
        ServiceType serviceType;
        for (DeploymentSubsystem deploymentSubsystem : deploymentPlan.getDeploymentSubsystems())
        {
            for (DeploymentService deploymentService : deploymentSubsystem.getDeploymentServices())
            {
                serviceType = ServiceType.valueOf(deploymentService.getService().getServiceType());
                if (serviceType.isBatch() && serviceType.isMicrogateway())
                {
                    return true;
                }
            }
        }
        return false;
    }
}
