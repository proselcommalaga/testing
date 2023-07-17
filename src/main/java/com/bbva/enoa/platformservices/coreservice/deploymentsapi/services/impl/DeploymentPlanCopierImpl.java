package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationRevision;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationValue;
import com.bbva.enoa.datamodel.model.connector.entities.DeploymentConnectorProperty;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceAllowedJdkParameterValue;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceAllowedJdkParameterValueId;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceFilesystem;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceFilesystemId;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConfigurationRevisionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.common.util.JvmJdkConfigurationChecker;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentPlanCopier;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.PlanProfilingUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Copies a {@link DeploymentPlan} to a new one.
 */
@Slf4j
@Service
public class DeploymentPlanCopierImpl implements IDeploymentPlanCopier
{
    /**
     * Entity manager
     */
    private final EntityManager entityManager;

    /**
     * Configuration Revision repository
     */
    private final ConfigurationRevisionRepository confRevisionRepo;

    /**
     * Plan profiling Utils
     */
    private final PlanProfilingUtils planProfilingUtils;

    /**
     * scheduler manager client
     */
    private final ISchedulerManagerClient schedulerManagerClient;

    private final JvmJdkConfigurationChecker jvmJdkConfigurationChecker;

    /**
     * Constructor by params
     *
     * @param entityManager          entity manager
     * @param confRevisionRepo       configuration revision repo
     * @param planProfilingUtils     plan profiling utils
     * @param schedulerManagerClient scheduler manager client
     * @param jvmJdkConfigurationChecker        JDK parameters validator
     */
    @Autowired
    public DeploymentPlanCopierImpl(final EntityManager entityManager, final ConfigurationRevisionRepository confRevisionRepo,
                                    final PlanProfilingUtils planProfilingUtils, final ISchedulerManagerClient schedulerManagerClient, final JvmJdkConfigurationChecker jvmJdkConfigurationChecker)
    {
        this.entityManager = entityManager;
        this.confRevisionRepo = confRevisionRepo;
        this.planProfilingUtils = planProfilingUtils;
        this.schedulerManagerClient = schedulerManagerClient;
        this.jvmJdkConfigurationChecker = jvmJdkConfigurationChecker;
    }


    @Override
    @Transactional
    public DeploymentPlan copyPlan(final DeploymentPlan originalPlan)
    {
        log.debug("Copying original deployment plan {} into a new deployment plan", originalPlan.getId());

        DeploymentPlan planCopied = new DeploymentPlan();

        // Copy basic data.
        planCopied.setReleaseVersion(originalPlan.getReleaseVersion());

        // Set original plan as the parent.
        planCopied.setParent(originalPlan);

        // Set the environment.
        planCopied.setEnvironment(originalPlan.getEnvironment());

        // Set deployment type
        planCopied.setDeploymentTypeInPro(originalPlan.getDeploymentTypeInPro());

        // Set Multi/Mono CPD value (always set the value from product configuration value)
        planCopied.setMultiCPDInPro(originalPlan.getReleaseVersion().getRelease().getProduct().getMultiCPDInPro());
        planCopied.setCpdInPro(originalPlan.getReleaseVersion().getRelease().getProduct().getCPDInPro());

        final Platform selectedDeploy = PlatformUtils
                .getSelectedDeployForReleaseInEnvironment(originalPlan.getReleaseVersion().getRelease(), Environment.valueOf(originalPlan.getEnvironment()));
        planCopied.setSelectedDeploy(selectedDeploy);
        planCopied.setEtherNs(originalPlan.getEtherNs());
        final Platform selectedLogging = PlatformUtils
                .getSelectedLoggingForReleaseInEnvironment(originalPlan.getReleaseVersion().getRelease(), Environment.valueOf(originalPlan.getEnvironment()));
        planCopied.setSelectedLogging(selectedLogging);

        if (originalPlan.getPlanProfiles().size() > 0)
        {
            planCopied.setPlanProfiles(this.planProfilingUtils.copyPlanProfile(originalPlan, planCopied));
        }

        // Save the plan.
        entityManager.persist(planCopied);

        // Copy plan structure.
        this.copySubsystems(originalPlan, planCopied);

        // Copy the current configuration from the original plan.
        this.copyCurrentConfigurationRevision(originalPlan, planCopied);

        // Copy deployment context params of batch schedule service
        this.copyDeploymentContextParams(originalPlan, planCopied);

        return planCopied;
    }

    /**
     * Copy deployment context params of the batch shedule service of the plan
     *
     * @param originalPlan the original plan to be copied
     * @param planCopied   the new plan to create and set the context params
     */
    private void copyDeploymentContextParams(final DeploymentPlan originalPlan, final DeploymentPlan planCopied)
    {

        originalPlan.getReleaseVersion().getSubsystems().stream()
                .flatMap(releaseVersionSubsystem -> releaseVersionSubsystem.getServices().stream())
                .filter(releaseVersionService -> ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(releaseVersionService.getServiceType()))
                .forEachOrdered(releaseVersionService -> this.schedulerManagerClient.copyDeploymentPlanContextParams(releaseVersionService.getId(), originalPlan.getId(), planCopied.getId(),
                        originalPlan.getEnvironment(), releaseVersionService.getId()));
    }

    private void copySubsystems(DeploymentPlan originalPlan, DeploymentPlan targetPlan)
    {
        // For each subsystem:
        for (DeploymentSubsystem originalSubsystem : originalPlan.getDeploymentSubsystems())
        {
            // Copy basic data.
            DeploymentSubsystem subsystemCopy = new DeploymentSubsystem();
            subsystemCopy.setSubsystem(originalSubsystem.getSubsystem());
            subsystemCopy.setDeploymentPlan(targetPlan);
            targetPlan.getDeploymentSubsystems().add(subsystemCopy);
            entityManager.persist(subsystemCopy);

            this.copyServices(originalSubsystem, subsystemCopy);
        }
    }


    private void copyServices(DeploymentSubsystem originalSubsystem, DeploymentSubsystem subsystemCopy)
    {
        for (DeploymentService originalService : originalSubsystem.getDeploymentServices())
        {
            // Hardware
            DeploymentService serviceCopy = new DeploymentService();
            serviceCopy.setService(originalService.getService());
            serviceCopy.setHardwarePack(originalService.getHardwarePack());

            // Instances
            serviceCopy.setNumberOfInstances(originalService.getNumberOfInstances());

            serviceCopy.setMemoryFactor(originalService.getMemoryFactor());
            // If there is logical connector assigned:
            if (originalService.getLogicalConnectors() != null)
            {
                // Note: Create new array list to avoid references to the same collection in JPA
                serviceCopy.setLogicalConnectors(new ArrayList<>(originalService.getLogicalConnectors()));
            }

            serviceCopy.setDeploymentSubsystem(subsystemCopy);
            subsystemCopy.getDeploymentServices().add(serviceCopy);

            entityManager.persist(serviceCopy);
            entityManager.flush();

            // If there are filesystems assigned
            List<DeploymentServiceFilesystem> deploymentServiceFilesystems = originalService.getDeploymentServiceFilesystems().stream().map(originalDeploymentServiceFilesystem -> {
                DeploymentServiceFilesystem deploymentServiceFilesystem = new DeploymentServiceFilesystem();
                deploymentServiceFilesystem.setId(new DeploymentServiceFilesystemId(serviceCopy.getId(), originalDeploymentServiceFilesystem.getFilesystem().getId()));
                deploymentServiceFilesystem.setDeploymentService(serviceCopy);
                deploymentServiceFilesystem.setVolumeBind(originalDeploymentServiceFilesystem.getVolumeBind());
                deploymentServiceFilesystem.setFilesystem(originalDeploymentServiceFilesystem.getFilesystem());
                return deploymentServiceFilesystem;
            }).collect(Collectors.toList());

            if (deploymentServiceFilesystems.size() > 0)
            {
                serviceCopy.setDeploymentServiceFilesystems(deploymentServiceFilesystems);
                entityManager.persist(serviceCopy);
            }

            final boolean isMultiJdk = jvmJdkConfigurationChecker.isMultiJdk(originalService.getService());
            if (isMultiJdk)
            {
                List<DeploymentServiceAllowedJdkParameterValue> originalJdkParamValues = originalService.getParamValues();
                if (originalJdkParamValues != null)
                {
                    List<DeploymentServiceAllowedJdkParameterValue> clonedJdkParamValues = new ArrayList<>(originalJdkParamValues.size());
                    for (DeploymentServiceAllowedJdkParameterValue originalParamValue : originalJdkParamValues)
                    {
                        DeploymentServiceAllowedJdkParameterValue clonedParamValue = new DeploymentServiceAllowedJdkParameterValue();
                        clonedParamValue.setDeploymentService(serviceCopy);
                        clonedParamValue.setAllowedJdkParameterProduct(originalParamValue.getAllowedJdkParameterProduct());
                        DeploymentServiceAllowedJdkParameterValueId id = new DeploymentServiceAllowedJdkParameterValueId();
                        id.setDeploymentServiceId(serviceCopy.getId());
                        id.setAllowedJdkParameterProductId(originalParamValue.getAllowedJdkParameterProduct().getId());
                        clonedParamValue.setId(id);
                        clonedJdkParamValues.add(clonedParamValue);
                    }
                    serviceCopy.setParamValues(clonedJdkParamValues);
                    entityManager.persist(serviceCopy);
                }
            }
        }
    }


    private void copyCurrentConfigurationRevision(final DeploymentPlan originalPlan, DeploymentPlan planCopy)
    {
        // Create the initial default configuration.
        ConfigurationRevision revision = this.copyInitialRevision(originalPlan, planCopy);
        planCopy.getRevisions().add(revision);
        planCopy.setCurrentRevision(revision);
        entityManager.persist(planCopy);
    }


    /**
     * Copy original deployment plan Revision int new deployment initial revision
     *
     * @param originalPlan   - Orginal {@code DeploymentPlan}
     * @param deploymentCopy - New {@code DeploymentPlan}
     * @return New {@code ConfigurationRevision }
     */
    private ConfigurationRevision copyInitialRevision(final DeploymentPlan originalPlan, final DeploymentPlan deploymentCopy)
    {
        log.debug("Copying original deployment plan Revision into new deployment initial revision");

        ConfigurationRevision newRevision = new ConfigurationRevision();
        newRevision.setDescription("Initial revision for Deployment plan: " + deploymentCopy.getId());
        newRevision.setDeploymentPlan(deploymentCopy);

        this.confRevisionRepo.save(newRevision);

        List<ConfigurationValue> values = new ArrayList<>();

        // Gets the original configuration
        ConfigurationRevision originalRevision = originalPlan.getCurrentRevision();

        if (originalRevision != null)
        {
            for (ConfigurationValue sourceCfgValue : originalRevision.getConfigurations())
            {
                ConfigurationValue configurationCopy = new ConfigurationValue();
                configurationCopy.setDefinition(sourceCfgValue.getDefinition());
                configurationCopy.setRevision(newRevision);
                configurationCopy.setValue(sourceCfgValue.getValue());
                values.add(configurationCopy);
            }

            // Gets the original deployment logical connector properties
            newRevision.setDeploymentConnectorProperties(copyDeploymentConnectorProperties(originalPlan, deploymentCopy, newRevision));
        }

        newRevision.setConfigurations(values);

        return newRevision;
    }

    /**
     * Copy the deployment connector properties into Configuration Revision of the NEW deployment plan from -> Original plan -> original Configuration Revision
     *
     * @param originalPlan             the original plan
     * @param newPlan                  new deployment plan to be copied
     * @param newConfigurationRevision new configuration revision to assign to New deployment plan
     * @return a deployment connector property list
     */
    private List<DeploymentConnectorProperty> copyDeploymentConnectorProperties(final DeploymentPlan originalPlan, final DeploymentPlan newPlan, final ConfigurationRevision newConfigurationRevision)
    {
        List<DeploymentConnectorProperty> deploymentConnectorPropertyList = new ArrayList<>();

        for (DeploymentConnectorProperty oldDeploymentConnectorProperty : originalPlan.getCurrentRevision().getDeploymentConnectorProperties())
        {
            DeploymentConnectorProperty newDeploymentConnectorProperty = new DeploymentConnectorProperty();

            newDeploymentConnectorProperty.setLogicalConnectorProperty(oldDeploymentConnectorProperty.getLogicalConnectorProperty());
            newDeploymentConnectorProperty.setDeploymentService(this.matchDeploymentService(oldDeploymentConnectorProperty.getDeploymentService(), newPlan));
            newDeploymentConnectorProperty.setRevision(newConfigurationRevision);

            // Add the new Deployment Connector Property to the list
            deploymentConnectorPropertyList.add(newDeploymentConnectorProperty);
        }

        return deploymentConnectorPropertyList;
    }

    /**
     * Get a Deployment Service from new deployment plan that match with release version servico from original plan
     *
     * @param deploymentServiceOrigin the original deployment service to match
     * @param newPlan                 the new plan to find a deployment service provided
     * @return a Deployment service found or
     */
    private DeploymentService matchDeploymentService(final DeploymentService deploymentServiceOrigin, final DeploymentPlan newPlan)
    {
        DeploymentService result = null;

        for (DeploymentSubsystem deploymentSubsystem : newPlan.getDeploymentSubsystems())
        {
            result = deploymentSubsystem.getDeploymentServices().stream().filter(deploymentService -> deploymentService.getService().getId().equals(deploymentServiceOrigin.getService().getId())).findFirst().orElse(result);
        }

        if (result == null)
        {
            throw new NovaException(DeploymentError.getNoCopyDeploymentLogicalConnector(), "[DeploymentPlanCopierImpl] -> [matchDeploymentService]: the original deployment service id: ["
                    + deploymentServiceOrigin.getId() + "] have not been found into the new deployment plan id: [" + newPlan.getId() + "] to be copied.");
        }

        return result;
    }
}
