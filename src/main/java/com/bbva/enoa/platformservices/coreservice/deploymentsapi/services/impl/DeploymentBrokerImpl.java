package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceBrokerDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceDto;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerProperty;
import com.bbva.enoa.datamodel.model.broker.entities.DeploymentBrokerProperty;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationRevision;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerPropertiesConfiguration;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerPropertyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerRepository;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentBroker;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Interface to manage brokers in deployments plans, services or instances
 */
@Service
@Slf4j
public class DeploymentBrokerImpl implements IDeploymentBroker
{

    private static final String CLASS_NAME = "DeploymentBrokerImpl";

    /**
     * Broker repository
     */
    private final BrokerRepository brokerRepository;

    /**
     * broker properties configuration
     */
    private final IBrokerPropertiesConfiguration brokerPropertiesConfiguration;
    /**
     * broker property repository
     */
    private final BrokerPropertyRepository brokerPropertyRepository;

    /**
     * Instantiates a new Deployment broker.
     *  @param brokerRepository              the broker repository
     * @param brokerPropertiesConfiguration the broker properties configuration
     * @param brokerPropertyRepository      the broker property repository
     */
    @Autowired
    public DeploymentBrokerImpl(final BrokerRepository brokerRepository,
                                final IBrokerPropertiesConfiguration brokerPropertiesConfiguration,
                                final BrokerPropertyRepository brokerPropertyRepository)
    {
        this.brokerRepository = brokerRepository;
        this.brokerPropertiesConfiguration = brokerPropertiesConfiguration;
        this.brokerPropertyRepository = brokerPropertyRepository;
    }

    @Override
    public DeploymentServiceBrokerDTO[] buildDeploymentServiceBrokerDTOsFromDeploymentServiceEntity(final DeploymentService deploymentService)
    {
        log.debug("[{}] -> [BuildDeploymentServiceBrokerDTOs]: Retrieving deploymentServiceBrokerDTOs from deploymentService entity: [{}]", CLASS_NAME, deploymentService);
        List<DeploymentServiceBrokerDTO> deploymentServiceBrokerDTOs = new ArrayList<>();
        if(deploymentService.getBrokers() != null  && !deploymentService.getBrokers().isEmpty()){
             deploymentServiceBrokerDTOs = deploymentService.getBrokers()
                    .stream()
                    .map(this::buildDeploymentServiceBrokerDTO)
                    .collect(Collectors.toList());
        }
        log.debug("[{}] -> [BuildDeploymentServiceBrokerDTOs]: Retrieved deploymentServiceBrokerDTOs: [{}] from deploymentService entity: [{}]", CLASS_NAME, deploymentServiceBrokerDTOs, deploymentService);
        return deploymentServiceBrokerDTOs.toArray(new DeploymentServiceBrokerDTO[0]);
    }

    @Override
    @Transactional
    public List<Broker> getBrokersEntitiesFromDeploymentServiceDTO(final DeploymentServiceDto deploymentServiceDto)
    {
        log.debug("[{}] -> [getBrokersEntitiesFromDeploymentServiceDTO]: Retrieving Broker list from deploymentServiceDto: [{}]", CLASS_NAME, deploymentServiceDto);
        List<Broker> brokerList = new ArrayList<>();
        if (deploymentServiceDto.getBrokers() != null)
        {
            brokerList = Arrays.stream(deploymentServiceDto.getBrokers())
                    .map(this::getBrokerFromDeploymentServiceBrokerDTO)
                    .collect(Collectors.toList());
        }

        log.debug("[{}] -> [getBrokersEntitiesFromDeploymentServiceDTO]: Retrieved Broker list: [{}] from deploymentServiceDto: [{}]", CLASS_NAME, brokerList, deploymentServiceDto);

        return brokerList;
    }

    @Override
    public List<GenericActivity> getActivityAttachedDeploymentServiceBrokerChange(final DeploymentService deploymentService, final DeploymentServiceDto deploymentServiceDto)
    {
        List<GenericActivity> brokerActivitiesToEmit = new ArrayList<>();

        getActivityBrokerAdded(deploymentService, deploymentServiceDto).ifPresent(brokerActivitiesToEmit::add);
        getActivityBrokerEliminated(deploymentService, deploymentServiceDto).ifPresent(brokerActivitiesToEmit::add);

        return brokerActivitiesToEmit;
    }

    @Override
    @Transactional
    public List<DeploymentBrokerProperty> createAndPersistBrokerPropertiesOfDeploymentService(DeploymentService deploymentService, ConfigurationRevision configurationRevision, final List<Broker> brokerList)
    {
        List<DeploymentBrokerProperty> deploymentBrokerProperties = new ArrayList<>();
        // Generate, persist brokerProperties for this deployment service and this broker.
        brokerList.forEach(broker -> {
            List<BrokerProperty> brokerPropertyList = this.getBrokerPropertiesForDeploymentService(deploymentService, broker);
            brokerPropertyList.forEach(brokerProperty -> {
                // Save new generated property in database
                this.brokerPropertyRepository.save(brokerProperty);
                // Asocio la propiedad del broker a la relacion entre broker deploymentservice y revision
                DeploymentBrokerProperty deploymentBrokerProperty = new DeploymentBrokerProperty();
                deploymentBrokerProperty.setBrokerProperty(brokerProperty);
                deploymentBrokerProperty.setDeploymentService(deploymentService);
                deploymentBrokerProperty.setRevision(configurationRevision);
                deploymentBrokerProperties.add(deploymentBrokerProperty);
            });
        });

        return  deploymentBrokerProperties;
    }

    /**
     * Get the list of properties Definitions that have to been associate to the deployment Service, this properties
     * are needed at runtime for the deploymenService to connect to the borker, and create correctly the pipes associated to that
     * deployment service in a broker
     *
     * @param deploymentService the deployment service
     * @param broker        the broker list attached to a deploymentService
     * @return broker properties for deployment service
     */
    private List<BrokerProperty> getBrokerPropertiesForDeploymentService(final DeploymentService deploymentService, final Broker broker)
    {
        List<BrokerProperty> brokerPropertyList = new ArrayList<>();
        brokerPropertyList.addAll(this.brokerPropertiesConfiguration.getBrokerSCSPropertiesDependentOnBroker(broker));
        brokerPropertyList.addAll(this.brokerPropertiesConfiguration.getBrokerSCSPropertiesDependentOnServiceAndBroker(deploymentService.getService(), broker));
        return brokerPropertyList;
    }

    /**
     * Get activity if a broker is eliminated in a given deploymentService
     *
     * @param deploymentService    the deploymentService is being configured
     * @param deploymentServiceDto the deploymentService that have the new ifo about the deploymentService
     * @return Optional of generic activity
     */
    private Optional<GenericActivity> getActivityBrokerEliminated(final DeploymentService deploymentService, final DeploymentServiceDto deploymentServiceDto)
    {
        List<String> brokersNamesInEntityButNotInDto = new ArrayList<>();
        GenericActivity genericActivitiesToEmit = null;

        // If DTO null, all brokers have been eliminated
        if(deploymentServiceDto.getBrokers() == null) {
            // Add all
            brokersNamesInEntityButNotInDto = deploymentService.getBrokers().stream().map(Broker::getName).collect(Collectors.toList());
        }
        else if(deploymentService.getBrokers() != null && !deploymentService.getBrokers().isEmpty())
        {
            // Add only brokers that exist in entity and not in dto
            brokersNamesInEntityButNotInDto = deploymentService.getBrokers().stream()
                    .filter(broker -> Arrays.stream(deploymentServiceDto.getBrokers()).noneMatch(depSerBrokerDto ->
                        depSerBrokerDto.getBrokerId() != null && depSerBrokerDto.getBrokerId().equals(broker.getId())
                    ))
                    .map(Broker::getName)
                    .collect(Collectors.toList());
        }
        if (!brokersNamesInEntityButNotInDto.isEmpty())
        {
            log.debug("[{}] -> [emitActivityBrokerEliminated]: brokers eliminated in deploymentService:[{}]: Brokers names: [{}]", CLASS_NAME, deploymentService.getId(), brokersNamesInEntityButNotInDto);
            genericActivitiesToEmit = this.getActivityToEmmit(deploymentService, brokersNamesInEntityButNotInDto, "Brokers unattached");
        }
        return Optional.ofNullable(genericActivitiesToEmit);
    }

    /**
     * Get activity if a broker is added in a given deploymentService
     *
     * @param deploymentService    the deploymentService is being configured
     * @param deploymentServiceDto the deploymentService that have the new ifo about the deploymentService
     * @return Optional of generic activity
     */
    private Optional<GenericActivity> getActivityBrokerAdded(final DeploymentService deploymentService, final DeploymentServiceDto deploymentServiceDto)
    {
        GenericActivity genericActivitiesToEmit = null;
        if (deploymentServiceDto.getBrokers() != null && deploymentServiceDto.getBrokers().length > 0)
        {
            List<String> brokersNamesInDTOButNotInEntity = Arrays.stream(deploymentServiceDto.getBrokers())
                    .filter(depSerBrokerDto -> deploymentService.getBrokers().stream().noneMatch(broker -> broker.getId().equals(depSerBrokerDto.getBrokerId())))
                    .map(DeploymentServiceBrokerDTO::getBrokerName)
                    .collect(Collectors.toList());
            log.debug("[{}] -> [emitActivityBrokerEliminated]: brokers added in deploymentService:[{}]: Brokers names: [{}]", CLASS_NAME, deploymentService.getId(), brokersNamesInDTOButNotInEntity);
            if (!brokersNamesInDTOButNotInEntity.isEmpty())
            {
                genericActivitiesToEmit = this.getActivityToEmmit(deploymentService, brokersNamesInDTOButNotInEntity, "Brokers attached");
            }
        }
        return Optional.ofNullable(genericActivitiesToEmit);
    }

    /**
     * Get the activity to emit, scoped in deployment service, for changes in his brokers
     *
     * @param deploymentService   The deployment service whome activity is going to be attached
     * @param changedBrokersNames The brokers that have an specific change to emit
     * @param paramNameOnBrokers  The param name relate to brokers to add on
     * @return a generic activity
     */
    private GenericActivity getActivityToEmmit(DeploymentService deploymentService, List<String> changedBrokersNames, String paramNameOnBrokers)
    {
        Integer productId = deploymentService.getService().getVersionSubsystem().getReleaseVersion().getRelease().getProduct().getId();
        log.debug("[{}] -> [getActivityToEmmit]: getting activity for deployment Service due to brokers modification, Brokers list: [{}]", CLASS_NAME, changedBrokersNames);
        return new GenericActivity
                .Builder(productId, ActivityScope.DEPLOYMENT_SERVICE, ActivityAction.CONFIGURATED)
                .entityId(deploymentService.getId())
                .environment(deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment())
                .addParam("Service name", deploymentService.getService().getServiceName())
                .addParam("Service type", deploymentService.getService().getServiceType())
                .addParam("Deployment Plan Id", deploymentService.getDeploymentSubsystem().getDeploymentPlan().getId())
                .addParam("Release Version Name", deploymentService.getService().getVersionSubsystem().getReleaseVersion().getVersionName())
                .addParam(paramNameOnBrokers, changedBrokersNames)
                .build();
    }

    private Broker getBrokerFromDeploymentServiceBrokerDTO(final DeploymentServiceBrokerDTO deploymentServiceBrokerDTO)
    {
        return this.brokerRepository.findById(deploymentServiceBrokerDTO.getBrokerId()).orElseThrow(() ->
        {
            log.error("[{}] -> [getBrokerFromDeploymentServiceBrokerDTO]: Broker with id:[{}] not found in database", CLASS_NAME, deploymentServiceBrokerDTO.getBrokerId());
            throw new NovaException(BrokerError.getBrokerNotFoundError(deploymentServiceBrokerDTO.getBrokerId()));
        });
    }

    private DeploymentServiceBrokerDTO buildDeploymentServiceBrokerDTO(final Broker broker)
    {
        log.debug("[{}] -> [buildDeploymentServiceBrokerDTO]: building deploymentServiceBrokerDTO from broker entity: [{}]", CLASS_NAME, broker);
        DeploymentServiceBrokerDTO deploymentServiceBrokerDTO = new DeploymentServiceBrokerDTO();
        deploymentServiceBrokerDTO.setBrokerId(broker.getId());
        deploymentServiceBrokerDTO.setBrokerName(broker.getName());
        deploymentServiceBrokerDTO.setBrokerType(broker.getType().name());
        log.debug("[{}] -> [buildDeploymentServiceBrokerDTO]: built deploymentServiceBrokerDTO: [{}] from broker entity: [{}]", CLASS_NAME, deploymentServiceBrokerDTO, broker);

        return deploymentServiceBrokerDTO;
    }
}
