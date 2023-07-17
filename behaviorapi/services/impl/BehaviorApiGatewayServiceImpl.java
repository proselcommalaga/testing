package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMDockerServiceDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMDockerValidationDTO;
import com.bbva.enoa.core.novabootstarter.util.ServiceNamingUtils;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorService;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorServiceConfiguration;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.IApiGatewayDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.exception.ApiGatewayError;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.validator.IApiGatewayValidator;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BehaviorServiceConfigurationRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IApiGatewayManagerClient;
import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API Gateway services
 */
@Service
@AllArgsConstructor
public class BehaviorApiGatewayServiceImpl implements IBehaviorApiGatewayService
{

    private static final Logger LOG = LoggerFactory.getLogger(BehaviorApiGatewayServiceImpl.class);

    private final IApiGatewayDtoBuilder apiGatewayServiceDtoBuilder;
    private final IApiGatewayValidator apiGatewayValidator;
    private final IApiGatewayManagerClient apiGatewayManagerClient;
    private final BehaviorServiceConfigurationRepository behaviorServiceConfigurationRepository;

    @Override
    public void generateDockerKey(final BehaviorServiceConfiguration behaviorServiceConfiguration)
    {
        Map<String, BehaviorServiceConfiguration> dockerKeyMap = new HashMap<>();
        BehaviorService behaviorService = behaviorServiceConfiguration.getBehaviorService();

        dockerKeyMap.put(
                ServiceNamingUtils.getNovaServiceName(
                        behaviorService.getGroupId(),
                        behaviorService.getArtifactId(),
                        behaviorService.getReleaseVersion().getRelease().getName(),
                        behaviorService.getVersion().split("\\.")[0])
                , behaviorServiceConfiguration);


        // Call to Apigateway client to request the the DockerKey set
        AGMDockerValidationDTO[] dockerValidationDtos = this.clientDockerKey(behaviorService, Environment.PRE.name());
        if (dockerValidationDtos != null)
        {
            this.setDockerKeysToServices(dockerKeyMap, dockerValidationDtos);
        }

        LOG.debug("[{}}] -> [generateDockerKey]: docker key map: [{}]", this.getClass().getSimpleName(), dockerKeyMap);

        //Saving deployment plan to avoid blank dockerKeys in services
        this.behaviorServiceConfigurationRepository.saveAndFlush(behaviorServiceConfiguration);

    }

    /////////////////////// PRIVATE //////////////////////////////////

    /**
     * Builds the DTO and calls the APIgateway client to generate the DockerKey set
     *
     * @param behaviorService Deployment Service List of a Deployment Plan
     * @param environment     Environment - INT, PRE, PRO
     * @return DockerValidationDto[] list of keys for each service
     */
    private AGMDockerValidationDTO[] clientDockerKey(final BehaviorService behaviorService, final String environment)
    {

        AGMDockerValidationDTO[] dockerKeys = new AGMDockerValidationDTO[0];
        // Build list of DockerServiceDto
        List<AGMDockerServiceDTO> dockerServiceDtoList = new ArrayList<>();

        // Verify iF service has an API Type.
        if (this.apiGatewayValidator.checkReleaseVersionServiceHasMicroGateway(behaviorService.getServiceType()))
        {
            dockerServiceDtoList.add(this.apiGatewayServiceDtoBuilder.buildDockerServiceDto(behaviorService));
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
    private void setDockerKeysToServices(final Map<String, BehaviorServiceConfiguration> dockerKeyMap,
                                         final AGMDockerValidationDTO[] dockerValidationDtos)
    {
        // Sets the DockerKey for each service
        for (AGMDockerValidationDTO dockerValidationDto : dockerValidationDtos)
        {
            BehaviorServiceConfiguration behaviorServiceConfiguration = dockerKeyMap.get(dockerValidationDto.getServiceName());
            if (behaviorServiceConfiguration == null)
            {
                throw new NovaException(ApiGatewayError.getUnexpectedServiceDockerKeyError());
            }
            else
            {
                behaviorServiceConfiguration.setDockerKey(dockerValidationDto.getDockerKey());
            }
        }
    }
}
