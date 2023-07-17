package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.DeploymentBrokerProperty;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationRevision;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationValue;
import com.bbva.enoa.datamodel.model.config.enumerates.ManagementType;
import com.bbva.enoa.datamodel.model.connector.entities.DeploymentConnectorProperty;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.deployment.entities.*;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.common.util.JvmJdkConfigurationChecker;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentBroker;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentPlanCloner;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.PlanProfilingUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Clones a {@link DeploymentPlan} to another {@link Environment}.
 */
@Service
@Slf4j
public class DeploymentPlanClonerImpl implements IDeploymentPlanCloner
{

    /**
     * entity manager
     */
    private final EntityManager entityManager;

    /**
     * filesystem API repository
     */
    private final FilesystemRepository filesystemsApiRepository;

    /**
     * logical connector repository
     */
    private final LogicalConnectorRepository logicalConnectorRepository;

    /**
     * configuration revision repository
     */
    private final ConfigurationRevisionRepository confRevisionRepo;

    /**
     * scheduler manager client
     */
    private final ISchedulerManagerClient schedulerManagerClient;

    private final PlanProfilingUtils planProfilingUtils;

    /**
     * utils
     */
    private final DeploymentUtils deploymentUtils;

    private final JvmJdkConfigurationChecker jvmJdkConfigurationChecker;
    /**
     * Broker repository
     */
    private BrokerRepository brokerRepository;
    /**
     * Deployment Broker
     */
    private IDeploymentBroker deploymentBroker;

    /**
     * Default constructor by params
     *
     * @param entityManager              entity manager
     * @param filesystemsApiRepository   filesysmte repository
     * @param logicalConnectorRepository logical connector repository
     * @param confRevisionRepo           configuration revision repository
     * @param schedulerManagerClient     scheduler manager client
     * @param planProfilingUtils         plan profiling utils
     * @param deploymentUtils            deployment utils
     * @param jvmJdkConfigurationChecker JDK parameters checker
     * @param brokerRepository           The broker repository
     * @param deploymentBroker           The deployment Broker
     */
    @Autowired
    public DeploymentPlanClonerImpl(final EntityManager entityManager, final FilesystemRepository filesystemsApiRepository,
                                    final LogicalConnectorRepository logicalConnectorRepository,
                                    final ConfigurationRevisionRepository confRevisionRepo,
                                    final ISchedulerManagerClient schedulerManagerClient,
                                    final PlanProfilingUtils planProfilingUtils,
                                    final DeploymentUtils deploymentUtils,
                                    final JvmJdkConfigurationChecker jvmJdkConfigurationChecker,
                                    final BrokerRepository brokerRepository,
                                    final IDeploymentBroker deploymentBroker)
    {
        this.entityManager = entityManager;
        this.filesystemsApiRepository = filesystemsApiRepository;
        this.logicalConnectorRepository = logicalConnectorRepository;
        this.confRevisionRepo = confRevisionRepo;
        this.schedulerManagerClient = schedulerManagerClient;
        this.planProfilingUtils = planProfilingUtils;
        this.deploymentUtils = deploymentUtils;
        this.jvmJdkConfigurationChecker = jvmJdkConfigurationChecker;
        this.brokerRepository = brokerRepository;
        this.deploymentBroker = deploymentBroker;
    }

    @Override
    @Transactional
    public DeploymentPlan clonePlanToEnvironment(final DeploymentPlan originalPlan, final Environment environment)
    {
        log.debug("Cloning original deployment plan {} into a new deployment plan on environment {}", originalPlan.getId(), environment);

        DeploymentPlan clonePlan = new DeploymentPlan();

        if (environment == Environment.PRO)
        {
            clonePlan.setGcsp(new DeploymentGcsp());
            clonePlan.setNova(new DeploymentNova());
        }

        // Copy basic data.
        clonePlan.setReleaseVersion(originalPlan.getReleaseVersion());

        // Set original plan as the parent.
        clonePlan.setParent(originalPlan);

        // Set the environment.
        clonePlan.setEnvironment(environment.getEnvironment());

        // Set the deployment plan type in pro ( on demand, nova planned or planned )
        if (environment == Environment.PRO)
        {
            clonePlan.setDeploymentTypeInPro(originalPlan.getReleaseVersion().getRelease().getDeploymentTypeInPro());
            clonePlan.setMultiCPDInPro(originalPlan.getReleaseVersion().getRelease().getProduct().getMultiCPDInPro());
            clonePlan.setCpdInPro(originalPlan.getReleaseVersion().getRelease().getProduct().getCPDInPro());
        }
        else
        {
            clonePlan.setDeploymentTypeInPro(originalPlan.getDeploymentTypeInPro());
            clonePlan.setMultiCPDInPro(originalPlan.getMultiCPDInPro());
            clonePlan.setCpdInPro(originalPlan.getCpdInPro());
        }

        final Platform selectedDeploy = PlatformUtils
                .getSelectedDeployForReleaseInEnvironment(originalPlan.getReleaseVersion().getRelease(), environment);
        clonePlan.setSelectedDeploy(selectedDeploy);
        clonePlan.setEtherNs(PlatformUtils.getSelectedDeployNSForProductInEnvironment(
                originalPlan.getReleaseVersion().getRelease().getProduct(), environment));

        final Platform selectedLogging = PlatformUtils
                .getSelectedLoggingForReleaseInEnvironment(originalPlan.getReleaseVersion().getRelease(), environment);
        clonePlan.setSelectedLogging(selectedLogging);

        // Save the plan.
        entityManager.persist(clonePlan);

        // Copy plan structure.
        this.copySubsystems(originalPlan, clonePlan, environment);

        if (!originalPlan.getPlanProfiles().isEmpty())
        {
            clonePlan.addPlanProfile(this.planProfilingUtils.createPlanProfile(clonePlan));
        }

        // Copy the current configuration from the original plan.
        this.copyCurrentConfigurationRevision(originalPlan, clonePlan);

        // Promote Deployment batch scheduler services
        this.promoteDeploymentContextParams(originalPlan, clonePlan);

        return clonePlan;
    }

    /**
     * Promote the deployment context params from old deployment plan to new deployment plan only for batch scheduler services
     *
     * @param originalPlan the original deployment plan associated to the batch scheduler service
     * @param clonePlan    the clone deployment plan associated to the batch scheduler service
     */
    private void promoteDeploymentContextParams(final DeploymentPlan originalPlan, final DeploymentPlan clonePlan)
    {

        originalPlan.getReleaseVersion().getSubsystems()
                .stream()
                .flatMap(releaseVersionSubsystem -> releaseVersionSubsystem.getServices().stream())
                .filter(releaseVersionService -> ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(releaseVersionService.getServiceType()))
                .forEachOrdered(releaseVersionService -> this.schedulerManagerClient.copyDeploymentPlanContextParams(releaseVersionService.getId(), originalPlan.getId(), clonePlan.getId(),
                        clonePlan.getEnvironment(), releaseVersionService.getId()));
    }


    private void copySubsystems(
            DeploymentPlan originalPlan,
            DeploymentPlan targetPlan,
            Environment environment)
    {
        // For each subsystem:
        for (DeploymentSubsystem originalSubsystem : originalPlan.getDeploymentSubsystems())
        {
            // Copy basic data.
            DeploymentSubsystem cloneSubsystem = new DeploymentSubsystem();
            cloneSubsystem.setSubsystem(originalSubsystem.getSubsystem());
            cloneSubsystem.setDeploymentPlan(targetPlan);
            targetPlan.getDeploymentSubsystems().add(cloneSubsystem);
            entityManager.persist(cloneSubsystem);

            // Copy the services - different behaviour by environment.
            if (Environment.PRE == environment)
            {
                this.copyServicesToPre(originalSubsystem, cloneSubsystem);
            }
            else if (Environment.PRO == environment)
            {
                this.copyServicesToPro(originalSubsystem, cloneSubsystem);
            }
        }
    }

    /**
     * Clone the services of a subsystem to another as destine with the PRE environment behaviour
     *
     * @param originalSubsystem a subsystem that you want to copy from
     * @param cloneSubsystem    subsystem with the same data of the original subsystem
     * @author modified by <a href="mailto:rodrigo.puerto.contractor@bbva.com">Rodrigo Puerto Pedrera</a>
     * @since on version 9.15.0 - 13/09/2021
     */
    private void copyServicesToPre(
            DeploymentSubsystem originalSubsystem,
            DeploymentSubsystem cloneSubsystem)
    {
        for (DeploymentService originalService : originalSubsystem.getDeploymentServices())
        {
            // Filesystem, brokers, logical connectors, hardware, volume bind and instances have to be lost.
            DeploymentService cloneService = new DeploymentService();
            cloneService.setService(originalService.getService());
            cloneService.setDeploymentSubsystem(cloneSubsystem);
            ServiceType serviceType = ServiceType.valueOf(originalService.getService().getServiceType());
            if (serviceType.isBatch())
            {
                cloneService.setNumberOfInstances(originalService.getNumberOfInstances());
            }

            else if(serviceType.isEPhoenix())
            {
                // Getting number of instances from deployment_label
                cloneService.setNumberOfInstances(this.deploymentUtils.getNumberOfInstancesForEphoenixService(
                        Environment.PRE.getEnvironment(), originalService.getService().getFinalName()));
            }
            cloneSubsystem.getDeploymentServices().add(cloneService);
            entityManager.persist(cloneService);
            final boolean isMultiJdk = jvmJdkConfigurationChecker.isMultiJdk(originalService.getService());
            if (isMultiJdk)
            {
                final List<DeploymentServiceAllowedJdkParameterValue> clonedParamValues = getDeploymentServiceJdkParameters(originalService, cloneService);
                cloneService.setParamValues(clonedParamValues);
                entityManager.persist(cloneService);
            }
        }
    }

    private void copyServicesToPro(
            DeploymentSubsystem originalSubsystem,
            DeploymentSubsystem cloneSubsystem)
    {
        for (DeploymentService originalService : originalSubsystem.getDeploymentServices())
        {
            // Hardware will be preserved.
            DeploymentService cloneService = new DeploymentService();
            cloneService.setService(originalService.getService());
            cloneService.setHardwarePack(originalService.getHardwarePack());

            // Instances will be preserved except for EPhoenix services
            ServiceType serviceType = ServiceType.valueOf(originalService.getService().getServiceType());
            if(serviceType.isEPhoenix())
            {
                // Getting number of instances from deployment_label
                cloneService.setNumberOfInstances(this.deploymentUtils.getNumberOfInstancesForEphoenixService(
                    Environment.PRO.getEnvironment(), originalService.getService().getFinalName()));
            }
            else
            {
                // Instances will be preserved.
                cloneService.setNumberOfInstances(originalService.getNumberOfInstances());
            }

            // Set the memory factor
            cloneService.setMemoryFactor(originalService.getMemoryFactor());

            // Set brokers without attached properties, properties will be generated and attached to deployment service when copy all properties
            copyBrokersToPro(originalService, cloneService);

            // If there is a logical connector list assigned:
            List<LogicalConnector> logicalConnectorListInPro = new ArrayList<>();

            // There must be the same logical connector in PRO for each one from PRE.
            for (LogicalConnector logicalConnector : originalService.getLogicalConnectors())
            {
                LogicalConnector logicalConnectorInPro = this.logicalConnectorRepository.findByProductIdAndEnvironmentAndConnectorTypeNameAndName(
                        originalSubsystem.getSubsystem().getReleaseVersion().getRelease().getProduct().getId(),
                        Environment.PRO.getEnvironment(),
                        logicalConnector.getConnectorType().getName(),
                        logicalConnector.getName());

                if (logicalConnectorInPro == null)
                {
                    log.error("[DeploymentsAPI] -> [copyServicesToPro]: There is not logical connector name: [{}] connector type: [{}] in PRO environment for" +
                                    "the product name: [{}]. The plan cannot be promoted before creating the same logical connector in PRO.",
                            logicalConnector.getName(), logicalConnector.getConnectorType().getName(),
                            originalSubsystem.getSubsystem().getReleaseVersion().getRelease().getProduct().getName());
                    throw new NovaException(DeploymentError.getTriedToPromotePlanWithoutLogicalConnectorInProError(),
                            DeploymentError.getTriedToPromotePlanWithoutLogicalConnectorInProError().getErrorMessage());
                }
                else
                {
                    logicalConnectorListInPro.add(logicalConnectorInPro);
                }
            }

            // Set the logical connector list
            cloneService.setLogicalConnectors(logicalConnectorListInPro);

            cloneService.setDeploymentSubsystem(cloneSubsystem);
            cloneSubsystem.getDeploymentServices().add(cloneService);

            entityManager.persist(cloneService);
            entityManager.flush();

            // If there are filesystems assigned:
            List<DeploymentServiceFilesystem> originalDeploymentServiceFilesystems = originalService.getDeploymentServiceFilesystems();
            if (originalDeploymentServiceFilesystems != null)
            {
                List<DeploymentServiceFilesystem> deploymentServiceFilesystems = new ArrayList<>();
                for (DeploymentServiceFilesystem originalDeploymentServiceFilesystem : originalDeploymentServiceFilesystems)
                {
                    // Cloned filesystems in PRO will have the same name as in PRE.
                    Filesystem filesystemInPro =
                            filesystemsApiRepository
                                    .findByProductIdAndEnvironmentAndNameOrderByCreationDateDesc(
                                            originalSubsystem.getSubsystem().getReleaseVersion().getRelease().getProduct().getId(),
                                            Environment.PRO.getEnvironment(),
                                            originalDeploymentServiceFilesystem.getFilesystem().getName());

                    if (filesystemInPro == null)
                    {
                        // There must be a filesystem in PRO for each one from PRE.
                        throw new NovaException(DeploymentError.getTriedToPromotePlanWithoutFilesystemInProError(),
                                DeploymentError.getTriedToPromotePlanWithoutFilesystemInProError().getErrorMessage());
                    }

                    DeploymentServiceFilesystem deploymentServiceFilesystem = new DeploymentServiceFilesystem();
                    deploymentServiceFilesystem.setId(new DeploymentServiceFilesystemId(cloneService.getId(), filesystemInPro.getId()));
                    deploymentServiceFilesystem.setFilesystem(filesystemInPro);
                    deploymentServiceFilesystem.setVolumeBind(originalDeploymentServiceFilesystem.getVolumeBind());
                    deploymentServiceFilesystem.setDeploymentService(cloneService);
                    deploymentServiceFilesystems.add(deploymentServiceFilesystem);
                }
                // Set the filesystems
                cloneService.setDeploymentServiceFilesystems(deploymentServiceFilesystems);
            }

            // Persist again
            entityManager.persist(cloneService);
            final boolean isMultiJdk = jvmJdkConfigurationChecker.isMultiJdk(originalService.getService());
            if (isMultiJdk)
            {
                final List<DeploymentServiceAllowedJdkParameterValue> clonedParamValues = getDeploymentServiceJdkParameters(originalService, cloneService);
                cloneService.setParamValues(clonedParamValues);
                entityManager.persist(cloneService);
            }
        }
    }

    private void copyBrokersToPro(final DeploymentService originalService, final DeploymentService cloneService)
    {
        List<Broker> brokerListInPro = new ArrayList<>();
        // If there is a broker assigned There must be the same logical connector in PRO for each one from PRE.
        for (Broker broker : originalService.getBrokers())
        {
            Broker brokerInPro = this.brokerRepository.findByProductIdAndNameAndEnvironment(broker.getProduct().getId(), broker.getName(), Environment.PRO.getEnvironment()).orElseThrow(() -> new NovaException(DeploymentError.getNoBrokerWithNameOnEnvironment(broker.getName())));
            brokerListInPro.add(brokerInPro);
        }
        cloneService.setBrokers(brokerListInPro);
    }

    private List<DeploymentServiceAllowedJdkParameterValue> getDeploymentServiceJdkParameters(DeploymentService originalService, DeploymentService clonedService)
    {
        final List<DeploymentServiceAllowedJdkParameterValue> originalParamValues = originalService.getParamValues();
        if (originalParamValues == null)
        {
            return Collections.emptyList();
        }
        List<DeploymentServiceAllowedJdkParameterValue> clonedParamValues = new ArrayList<>(originalParamValues.size());
        for (DeploymentServiceAllowedJdkParameterValue originalParamValue : originalParamValues)
        {
            DeploymentServiceAllowedJdkParameterValue clonedParamValue = new DeploymentServiceAllowedJdkParameterValue();
            clonedParamValue.setAllowedJdkParameterProduct(originalParamValue.getAllowedJdkParameterProduct());
            clonedParamValue.setDeploymentService(clonedService);
            DeploymentServiceAllowedJdkParameterValueId id = new DeploymentServiceAllowedJdkParameterValueId();
            id.setDeploymentServiceId(clonedService.getId());
            id.setAllowedJdkParameterProductId(clonedParamValue.getAllowedJdkParameterProduct().getId());
            clonedParamValue.setId(id);
            clonedParamValues.add(clonedParamValue);
        }
        return clonedParamValues;
    }


    private void copyCurrentConfigurationRevision(final DeploymentPlan originalPlan, DeploymentPlan clonePlan)
    {
        // Create the initial default configuration.
        ConfigurationRevision revision = this.cloneInitialRevision(originalPlan, clonePlan);
        clonePlan.getRevisions().add(revision);
        clonePlan.setCurrentRevision(revision);
        entityManager.persist(clonePlan);
    }


    /**
     * Clone original deployment plan Revision int new deployment initial revision
     *
     * @param originalPlan - Orginal {@code DeploymentPlan}
     * @param clonePlan    - New {@code DeploymentPlan}
     * @return New {@code ConfigurationRevision }
     */
    private ConfigurationRevision cloneInitialRevision(final DeploymentPlan originalPlan, final DeploymentPlan clonePlan)
    {
        log.debug("cloning original deployment plan Revision int new deployment initial revision");
        ConfigurationRevision newRevision = new ConfigurationRevision();
        newRevision.setDescription("Initial revision for Deployment plan: " + clonePlan.getId());
        newRevision.setDeploymentPlan(clonePlan);

        this.confRevisionRepo.save(newRevision);

        // Start the properties list (TEMPLATE and CONNECTORS)
        List<ConfigurationValue> values = new ArrayList<>();
        List<DeploymentConnectorProperty> deploymentConnectorPropertyList = new ArrayList<>();
        List<DeploymentBrokerProperty> deploymentBrokerPropertyList = new ArrayList<>();

        // Gets the original configuration
        ConfigurationRevision originalRevision = originalPlan.getCurrentRevision();

        if (originalRevision != null)
        {
            // Add the Configuration Value properties (properties from TEMPLATE)
            this.addConfigurationValueProperties(clonePlan, newRevision, values, originalRevision);

            // if the new plan is for Environment == PRO, add the deployment logical connector properties from PRO (In PRE the deployment connector properties will no be copied. Just )
            // if the new plan is for Environment == PRO, add the broker properties from PRE (In PRE the deployment connector properties will no be copied. User have to assign the broker in that environment )
            if (Environment.PRO.getEnvironment().equals(clonePlan.getEnvironment()))
            {
                this.addDeploymentConnectorPropertyInPro(clonePlan, newRevision, deploymentConnectorPropertyList);
                deploymentBrokerPropertyList = this.getDeploymentBrokerPropertyForPlan(clonePlan, newRevision);
            }
        }

        newRevision.setConfigurations(values);
        newRevision.setDeploymentConnectorProperties(deploymentConnectorPropertyList);
        log.debug("Created the configuration value list: [{}] and the deployment connector property list: [{}] and the deployment broker property list: [{}]  for the Configuration revision [{}]",
                values, deploymentConnectorPropertyList, deploymentBrokerPropertyList, newRevision);

        return newRevision;
    }

    private List<DeploymentBrokerProperty> getDeploymentBrokerPropertyForPlan(final DeploymentPlan clonePlan, final ConfigurationRevision newRevision)
    {
        List<DeploymentBrokerProperty> deploymentBrokerPropertyListForPlan = new ArrayList<>();
        clonePlan.getDeploymentSubsystems().forEach(
                deploymentSubsystem -> deploymentSubsystem.getDeploymentServices().forEach(deploymentService -> {
                    List<DeploymentBrokerProperty> deploymentBrokerPropertyListForService = this.deploymentBroker.createAndPersistBrokerPropertiesOfDeploymentService(deploymentService, newRevision,deploymentService.getBrokers());
                    deploymentService.getDeploymentBrokerProperties().addAll(deploymentBrokerPropertyListForService);
                    deploymentBrokerPropertyListForPlan.addAll(deploymentBrokerPropertyListForService);
                }));
        return deploymentBrokerPropertyListForPlan;
    }

    private void addConfigurationValueProperties(final DeploymentPlan clonePlan, final ConfigurationRevision newRevision, final List<ConfigurationValue> values, final ConfigurationRevision originalRevision)
    {
        // Get all the configurations value (the configuration value list)
        List<ConfigurationValue> originalConfigurationValueList = originalRevision.getConfigurations();

        // Clone the properties from 'template' - configuration properties
        log.debug("Cloning configuration for environment: [{}]", clonePlan.getEnvironment());
        for (ConfigurationValue sourceCfgValue : originalConfigurationValueList)
        {
            ConfigurationValue cloneConfiguration = new ConfigurationValue();
            cloneConfiguration.setDefinition(sourceCfgValue.getDefinition());
            cloneConfiguration.setRevision(newRevision);

            // Only copy values from configurations not environment dependant
            if (sourceCfgValue.getDefinition().getManagement() != ManagementType.ENVIRONMENT)
            {
                cloneConfiguration.setValue(sourceCfgValue.getValue());
                log.debug("Copied value of the property name: [{}]", sourceCfgValue.getDefinition().getName());
            }

            values.add(cloneConfiguration);
            log.trace("Cloned new configuration value property: [{}]", cloneConfiguration);
        }
    }

    private void addDeploymentConnectorPropertyInPro(final DeploymentPlan clonePlan, final ConfigurationRevision newRevision,
                                                     final List<DeploymentConnectorProperty> deploymentConnectorPropertyList)
    {
        for (DeploymentSubsystem deploymentSubsystem : clonePlan.getDeploymentSubsystems())
        {
            for (DeploymentService deploymentService : deploymentSubsystem.getDeploymentServices())
            {
                List<LogicalConnector> logicalConnectorList = deploymentService.getLogicalConnectors();

                if (logicalConnectorList == null)
                {
                    log.debug("No hay conectores logicos en este servicio");
                }
                else
                {
                    this.addDeploymentConnectorProperty(clonePlan, newRevision, deploymentConnectorPropertyList, deploymentService, logicalConnectorList);
                }
            }
        }
    }

    private void addDeploymentConnectorProperty(final DeploymentPlan clonePlan, final ConfigurationRevision newRevision, final List<DeploymentConnectorProperty> deploymentConnectorPropertyList, final DeploymentService deploymentService, final List<LogicalConnector> logicalConnectorList)
    {
        for (LogicalConnector logicalConnector : logicalConnectorList)
        {
            for (LogicalConnectorProperty logicalConnectorProperty : logicalConnector.getLogConnProp())
            {
                // Create the deployment definition and save it as new property from logical connector for PRE
                DeploymentConnectorProperty deploymentConnectorProperty = new DeploymentConnectorProperty();

                deploymentConnectorProperty.setLogicalConnectorProperty(logicalConnectorProperty);
                deploymentConnectorProperty.setDeploymentService(deploymentService);
                deploymentConnectorProperty.setRevision(newRevision);

                deploymentConnectorPropertyList.add(deploymentConnectorProperty);
                log.trace("[DeploymentsAPI] -> [addDeploymentConnectorPropertyInPro]: added a new deployment Connector Property: [{}] to the list" +
                        "for the configuration revision id: [{}] of the deployment plan id: [{}]", deploymentConnectorProperty, newRevision.getId(), clonePlan.getId());
            }
        }
    }
}
