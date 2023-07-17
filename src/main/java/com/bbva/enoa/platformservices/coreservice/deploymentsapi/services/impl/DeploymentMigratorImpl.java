package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentMigrationDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentMigrationServiceDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentMigrationSubsystemDto;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.DeploymentBrokerProperty;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationRevision;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationValue;
import com.bbva.enoa.datamodel.model.config.entities.PropertyDefinition;
import com.bbva.enoa.datamodel.model.config.enumerates.ManagementType;
import com.bbva.enoa.datamodel.model.connector.entities.DeploymentConnectorProperty;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentGcsp;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentNova;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceAllowedJdkParameterValue;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceAllowedJdkParameterValueId;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceFilesystem;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceFilesystemId;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.common.util.JvmJdkConfigurationChecker;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentBroker;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentMigrator;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.PlanProfilingUtils;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Migrates a {@link DeploymentPlan} to a new one of the same
 * or a different {@link ReleaseVersion}, copying common services and
 * properties from the original plan to the new, if the service
 * have the same version.
 */
@Slf4j
@Transactional
@Service
public class DeploymentMigratorImpl implements IDeploymentMigrator
{
    /**
     * entity manager
     */
    private final EntityManager entityManager;

    /**
     * tools client
     */
    private final IToolsClient toolsClient;

    /**
     * scheduler manager client
     */
    private final ISchedulerManagerClient schedulerManagerClient;

    /**
     * Plan profiling Utils
     */
    private final PlanProfilingUtils planProfilingUtils;

    /**
     * utils
     */
    private final DeploymentUtils deploymentUtils;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    private final JvmJdkConfigurationChecker jvmJdkConfigurationChecker;

    private final IDeploymentBroker deploymentBroker;

    /**
     * Default constructor by params
     *
     * @param entityManager              entity manager
     * @param toolsClient                tools client
     * @param schedulerManagerClient     scheduler manager client
     * @param planProfilingUtils         plan profiling utils
     * @param deploymentUtils            deployment utils
     * @param novaActivityEmitter        NovaActivity emitter
     * @param jvmJdkConfigurationChecker JVM JDK configuration checker
     * @param deploymentBroker           The deploymentBroker
     */
    @Autowired
    public DeploymentMigratorImpl(final EntityManager entityManager, final IToolsClient toolsClient,
                                  final ISchedulerManagerClient schedulerManagerClient,
                                  final PlanProfilingUtils planProfilingUtils,
                                  final DeploymentUtils deploymentUtils,
                                  final INovaActivityEmitter novaActivityEmitter,
                                  final JvmJdkConfigurationChecker jvmJdkConfigurationChecker,
                                  final IDeploymentBroker deploymentBroker)
    {
        this.entityManager = entityManager;
        this.toolsClient = toolsClient;
        this.schedulerManagerClient = schedulerManagerClient;
        this.planProfilingUtils = planProfilingUtils;
        this.deploymentUtils = deploymentUtils;
        this.novaActivityEmitter = novaActivityEmitter;
        this.jvmJdkConfigurationChecker = jvmJdkConfigurationChecker;
        this.deploymentBroker = deploymentBroker;
    }

    /**
     * If the target service will be deployed in NOVA platform, the product is configured on MultiCPD mode, the number of service's instances is odd and the service type is a running one,
     * we need to reset the target instances (ONLY on {@link Environment#PRE}).
     *
     * @param deploymentService       the {@link DeploymentService} that we want to copy
     * @param targetDestinationDeploy the {@link Platform} where we want to copy the new {@link DeploymentService}
     * @return TRUE if we need to reset the instances
     */
    private static Boolean isInstanceNumberIncompatibleInMultiCPDMode(final DeploymentService deploymentService, final Platform targetDestinationDeploy)
    {
        final var isNova = targetDestinationDeploy.equals(Platform.NOVA);
        final var isProductOnMultiCPD = deploymentService.getDeploymentSubsystem().getSubsystem().getReleaseVersion().getRelease().getProduct().getMultiCPDInPro();
        final var canCopyInstances = ((deploymentService.getNumberOfInstances() % 2) == 0);
        final var isIntEnvironment = deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment().equals(Environment.INT.getEnvironment());

        final var serviceType = ServiceType.valueOf(deploymentService.getService().getServiceType());
        final var isRunningService = Boolean.FALSE.equals(serviceType.isBatch())
                && Boolean.FALSE.equals(ServiceType.isLibrary(serviceType))
                && ServiceType.BATCH_SCHEDULER_NOVA != serviceType;

        return Boolean.TRUE.equals(isNova)
                && Boolean.TRUE.equals(isProductOnMultiCPD)
                && Boolean.TRUE.equals(isRunningService)
                && Boolean.FALSE.equals(canCopyInstances)
                && Boolean.FALSE.equals(isIntEnvironment);
    }

    @Override
    public DeploymentMigrationDto migratePlan(final DeploymentPlan originalPlan, final DeploymentPlan targetPlan)
    {
        log.debug("Migrating data from plan {} to plan {}", originalPlan.getId(), targetPlan.getId());

        // Initialize the migration result.
        DeploymentMigrationDto migrationDto = this.initResults(targetPlan);

        // Set original plan as the parent.
        targetPlan.setParent(originalPlan);

        targetPlan.setAction(DeploymentAction.READY);

        if (originalPlan.getEnvironment().equals(Environment.PRO.getEnvironment()))
        {
            targetPlan.setGcsp(new DeploymentGcsp());
            targetPlan.setNova(new DeploymentNova());
        }
        targetPlan.setDeploymentTypeInPro(originalPlan.getReleaseVersion().getRelease().getDeploymentTypeInPro());

        // Set Multi/Mono CPD value (always set the value from product configuration value)
        targetPlan.setMultiCPDInPro(originalPlan.getReleaseVersion().getRelease().getProduct().getMultiCPDInPro());
        targetPlan.setCpdInPro(originalPlan.getReleaseVersion().getRelease().getProduct().getCPDInPro());

        final Platform selectedDeploy = PlatformUtils
                .getSelectedDeployForReleaseInEnvironment(originalPlan.getReleaseVersion().getRelease(), Environment.valueOf(originalPlan.getEnvironment()));
        final String etherNSDeploy = PlatformUtils
                .getSelectedDeployNSForProductInEnvironment(originalPlan.getReleaseVersion().getRelease().getProduct(), Environment.valueOf(originalPlan.getEnvironment()));
        targetPlan.setSelectedDeploy(selectedDeploy);
        targetPlan.setEtherNs(etherNSDeploy);

        final Platform selectedLogging = PlatformUtils
                .getSelectedLoggingForReleaseInEnvironment(originalPlan.getReleaseVersion().getRelease(), Environment.valueOf(originalPlan.getEnvironment()));
        targetPlan.setSelectedLogging(selectedLogging);

        if (!originalPlan.getPlanProfiles().isEmpty())
        {
            targetPlan.setPlanProfiles(this.planProfilingUtils.copyPlanProfile(originalPlan, targetPlan));
        }

        // Save the plan.
        entityManager.persist(targetPlan);

        // Copy plan structure.
        this.copySubsystems(originalPlan, targetPlan, migrationDto);

        // Copy deployment context params of batch schedule service
        this.copyDeploymentContextParams(originalPlan, targetPlan, migrationDto);

        // Emit Copy Deployment Plan Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(originalPlan.getReleaseVersion().getRelease().getProduct().getId(), ActivityScope.DEPLOYMENT_PLAN, ActivityAction.COPIED)
                .entityId(originalPlan.getId())
                .environment(originalPlan.getEnvironment())
                .addParam("releaseVersionId", originalPlan.getReleaseVersion().getId())
                .addParam("deploymentStatus", originalPlan.getStatus().getDeploymentStatus())
                .addParam("releaseVersionName", originalPlan.getReleaseVersion().getVersionName())
                .addParam("releaseName", originalPlan.getReleaseVersion().getRelease().getName())
                .build());

        log.debug("Built migration result with this data: \n{}", migrationDto);

        return migrationDto;
    }

    /**
     * Copy deployment context params of the batch shedule service of the plan
     *
     * @param originalPlan the original plan to be copied
     * @param planCopied   the new plan to create and set the context params
     * @param migrationDto migrated services between plans
     */
    private void copyDeploymentContextParams(final DeploymentPlan originalPlan, final DeploymentPlan planCopied, DeploymentMigrationDto migrationDto)
    {
        List<ReleaseVersionService> originalPlanBatchSchedulerList = originalPlan.getReleaseVersion().getSubsystems().stream()
                .flatMap(releaseVersionSubsystem -> releaseVersionSubsystem.getServices().stream())
                .filter(releaseVersionService -> ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(releaseVersionService.getServiceType())).collect(Collectors.toList());

        List<Integer> copiedBatchSchedulerIdList = new ArrayList<>();

        for (ReleaseVersionService originalBatchScheduler : originalPlanBatchSchedulerList)
        {
            Arrays.stream(migrationDto.getSubsystems()).forEach(subsystem -> Arrays.stream(subsystem.getServices())
                    .filter(service -> originalBatchScheduler.getId().equals(service.getOriginalReleaseVersionServiceId()))
                    .forEach(service -> {
                        this.schedulerManagerClient.copyDeploymentPlanContextParams(service.getOriginalReleaseVersionServiceId(), originalPlan.getId(), planCopied.getId(),
                                originalPlan.getEnvironment(), service.getTargetReleaseVersionServiceId());
                        copiedBatchSchedulerIdList.add(service.getTargetReleaseVersionServiceId());
                    }));
        }

        // Batch scheduler services existing in now plan but not in the old one
        List<ReleaseVersionService> newPlanNewBatchSchedulerList = planCopied.getReleaseVersion().getSubsystems().stream()
                .flatMap(releaseVersionSubsystem -> releaseVersionSubsystem.getServices().stream())
                .filter(releaseVersionService -> ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(releaseVersionService.getServiceType())
                        && !copiedBatchSchedulerIdList.contains(releaseVersionService.getId())).collect(Collectors.toList());

        newPlanNewBatchSchedulerList.forEach(releaseVersionService ->
                this.schedulerManagerClient.createDeploymentPlanContextParams(releaseVersionService.getId(), planCopied.getId(), planCopied.getEnvironment()));
    }

    /**
     * Init results.
     *
     * @param targetPlan target plan
     * @return dto
     */
    private DeploymentMigrationDto initResults(final DeploymentPlan targetPlan)
    {
        DeploymentMigrationDto migrationDto = new DeploymentMigrationDto();

        migrationDto.setPlanId(targetPlan.getId());
        migrationDto.setSubsystems(new DeploymentMigrationSubsystemDto[targetPlan.getDeploymentSubsystems().size()]);


        int i = 0;
        for (DeploymentSubsystem subsystem : targetPlan.getDeploymentSubsystems())
        {
            TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(subsystem.getSubsystem().getSubsystemId());

            migrationDto.getSubsystems()[i] = new DeploymentMigrationSubsystemDto();
            migrationDto.getSubsystems()[i].setStatus(MigrationStatus.PENDING.name());
            migrationDto.getSubsystems()[i].setSubsystemId(subsystem.getId());
            migrationDto.getSubsystems()[i].setSubsystemName(subsystemDTO.getSubsystemName());

            migrationDto.getSubsystems()[i].setServices(new DeploymentMigrationServiceDto[subsystem.getDeploymentServices().size()]);

            int j = 0;
            for (DeploymentService service : subsystem.getDeploymentServices())
            {
                migrationDto.getSubsystems()[i].getServices()[j] = new DeploymentMigrationServiceDto();
                migrationDto.getSubsystems()[i].getServices()[j].setStatus(MigrationStatus.PENDING.name());
                migrationDto.getSubsystems()[i].getServices()[j].setId(service.getId());
                migrationDto.getSubsystems()[i].getServices()[j].setArtifactId(service.getService().getArtifactId());
                migrationDto.getSubsystems()[i].getServices()[j].setGroupId(service.getService().getGroupId());
                migrationDto.getSubsystems()[i].getServices()[j].setVersion(service.getService().getVersion());
                migrationDto.getSubsystems()[i].getServices()[j].setServiceName(service.getService().getServiceName());
                migrationDto.getSubsystems()[i].getServices()[j].setTargetReleaseVersionServiceId(service.getService().getId());

                j++;
            }

            i++;
        }

        return migrationDto;
    }

    /**
     * Copy subsystems
     *
     * @param originalPlan original plan
     * @param targetPlan   target plan
     * @param migrationDto dto
     */
    private void copySubsystems(final DeploymentPlan originalPlan, final DeploymentPlan targetPlan, final DeploymentMigrationDto migrationDto)
    {
        // For each subsystem:
        int i = 0;
        for (DeploymentSubsystem targetSubsystem : targetPlan.getDeploymentSubsystems())
        {
            // If target subsystem does exist on original plan:
            if (subsystemIsInPlan(targetSubsystem, originalPlan))
            {
                // Copy services.
                this.copyServices(getSubsystemFromPlan(targetSubsystem, originalPlan), targetSubsystem, migrationDto.getSubsystems()[i]);
            }

            // Jump to next subsystem on target plan.
            i++;
        }
    }

    /**
     * Copy services
     *
     * @param originalSubsystem     original subsystem
     * @param targetSubsystem       target subsystem
     * @param migrationSubsystemDto dto
     */
    private void copyServices(DeploymentSubsystem originalSubsystem, DeploymentSubsystem targetSubsystem, DeploymentMigrationSubsystemDto migrationSubsystemDto)
    {

        // For each service:
        int i = 0;
        for (DeploymentService targetService : targetSubsystem.getDeploymentServices())
        {
            log.info("Evaluating service: [{}] {}.{}", targetService.getService().getId(), targetService.getService().getGroupId(), targetService.getService().getArtifactId());

            // If target service does exist on original subsystem:
            if (serviceIsInSubsystem(targetService, originalSubsystem))
            {
                // Get the original service.
                DeploymentService originalService = this.getServiceFromSubsystem(targetService, originalSubsystem);

                final MigrationStatus serviceMigrationStatus = this.copyServiceToTarget(originalService, targetService, migrationSubsystemDto.getServices()[i]);

                // Add service to list of copied services.
                migrationSubsystemDto.getServices()[i].setStatus(serviceMigrationStatus.name());

                // Mark subsystem as migrated.
                migrationSubsystemDto.setStatus(MigrationStatus.MIGRATED.name());

            }
            // If service does not exist on original subsystem, skip it:
            else
            {
                log.debug("Service [{}] {}.{} does not exist on release version of original plan, skipping it", targetService.getService().getId(), targetService.getService().getGroupId(),
                        targetService.getService().getArtifactId());
            }

            // Jump to next service on target plan.
            i++;
        }
    }

    /**
     * Copies a {@link DeploymentService} data to another,
     * including its configuration.
     *
     * @param originalService {@link DeploymentService}
     * @param targetService   {@link DeploymentService}
     */
    private MigrationStatus copyServiceToTarget(DeploymentService originalService, DeploymentService targetService, DeploymentMigrationServiceDto migrationServiceDto)
    {
        final ReleaseVersionService originalReleaseVersionService = originalService.getService();
        log.debug("Migrating data from service {}.{} [{}] to service [{}]", originalReleaseVersionService.getGroupId(), originalReleaseVersionService.getArtifactId(), originalReleaseVersionService.getId(), targetService.getService().getId());

        log.trace("Original service {}.{} [{}] data: \n{}", originalReleaseVersionService.getGroupId(), originalReleaseVersionService.getArtifactId(), originalReleaseVersionService.getId(),
                originalService.toJSON());

        MigrationStatus serviceMigrationStatus = MigrationStatus.MIGRATED;

        final Platform originalDestinationDeploy = originalService.getDeploymentSubsystem().getDeploymentPlan().getSelectedDeploy();
        final Platform targetDestinationDeploy = targetService.getDeploymentSubsystem().getDeploymentPlan().getSelectedDeploy();

        boolean platformHasChanged = originalDestinationDeploy != targetDestinationDeploy;
        final Environment targetEnvironment = Environment.valueOf(targetService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment());
        boolean targetEnvironmentIsPro = Environment.PRO.equals(targetEnvironment) || Environment.LAB_PRO.equals(targetEnvironment) || Environment.STAGING_PRO.equals(targetEnvironment);

        if (platformHasChanged && targetEnvironmentIsPro)
        {
            throw new NovaException(DeploymentError.getPlatformConflictError());
        }

        // Hardware & Instances.
        if (platformHasChanged)
        {
            migrationServiceDto.setHardwareStatus(HardwareMigrationStatus.PENDING_CROSSPLATFORM.name());
            serviceMigrationStatus = MigrationStatus.PENDING;
        }
        else
        {
            targetService.setHardwarePack(originalService.getHardwarePack());
            migrationServiceDto.setHardwareStatus(HardwareMigrationStatus.MIGRATED.name());

            // For EPhoenix services, get number of instances from deployment_label and don't validate MultiCPD configuration
            if (ServiceType.valueOf(targetService.getService().getServiceType()).isEPhoenix())
            {
                targetService.setNumberOfInstances(this.deploymentUtils.getNumberOfInstancesForEphoenixService(
                        targetEnvironment.getEnvironment(), targetService.getService().getFinalName()));
            }
            else
            {
                targetService.setNumberOfInstances(originalService.getNumberOfInstances());
                // If the target service will be deployed in NOVA platform, the product is configured on MultiCPD and the number of service's instances is odd,
                // we need to reset the target instances
                if (Boolean.TRUE.equals(isInstanceNumberIncompatibleInMultiCPDMode(originalService, targetDestinationDeploy)))
                {
                    targetService.setNumberOfInstances(0);
                    serviceMigrationStatus = MigrationStatus.PENDING;
                    migrationServiceDto.setHardwareStatus(HardwareMigrationStatus.PENDING_INSTANCES.name());
                }
            }
        }

        // If there are filesystems assigned:
        entityManager.persist(targetService);

        if (platformHasChanged)
        {
            migrationServiceDto.setFilesystemStatus(FilesystemMigrationStatus.PENDING_CROSSPLATFORM.name());
            serviceMigrationStatus = MigrationStatus.PENDING;
        }
        else
        {
            List<DeploymentServiceFilesystem> originalDeploymentServiceFilesystems = originalService.getDeploymentServiceFilesystems();
            if (originalDeploymentServiceFilesystems != null)
            {
                List<DeploymentServiceFilesystem> targetDeploymentServiceFilesystemList = new ArrayList<>();
                FilesystemMigrationStatus filesystemMigrationStatus = FilesystemMigrationStatus.MIGRATED;
                for (DeploymentServiceFilesystem originalDeploymentServiceFilesystem : originalDeploymentServiceFilesystems)
                {
                    Filesystem originalFilesystem = originalDeploymentServiceFilesystem.getFilesystem();

                    DeploymentServiceFilesystem targetDeploymentServiceFilesystem = new DeploymentServiceFilesystem();
                    targetDeploymentServiceFilesystem.setId(new DeploymentServiceFilesystemId(targetService.getId(), originalFilesystem.getId()));
                    targetDeploymentServiceFilesystem.setDeploymentService(targetService);
                    targetDeploymentServiceFilesystem.setFilesystem(originalFilesystem);
                    targetDeploymentServiceFilesystem.setVolumeBind(originalDeploymentServiceFilesystem.getVolumeBind());

                    // If filesystem is archived, copy it but set status to pending:
                    if (FilesystemStatus.ARCHIVED == originalFilesystem.getFilesystemStatus())
                    {
                        targetDeploymentServiceFilesystemList.add(targetDeploymentServiceFilesystem);
                        filesystemMigrationStatus = FilesystemMigrationStatus.MIGRATED_ARCHIVED;
                    }
                    // Not deleted filesystems will be copied as they are:
                    else if (originalFilesystem.getDeletionDate() == null)
                    {
                        targetDeploymentServiceFilesystemList.add(targetDeploymentServiceFilesystem);
                    }
                    // In any other case:
                    else
                    {
                        filesystemMigrationStatus = FilesystemMigrationStatus.PENDING_OTHER;
                    }
                }
                targetService.setDeploymentServiceFilesystems(targetDeploymentServiceFilesystemList);
                migrationServiceDto.setFilesystemStatus(filesystemMigrationStatus.name());

                if (filesystemMigrationStatus != FilesystemMigrationStatus.MIGRATED)
                {
                    serviceMigrationStatus = MigrationStatus.PENDING;
                }
            }
        }

        targetService.setMemoryFactor(originalService.getMemoryFactor());

        // Check if there is a logical connector assigned
        List<LogicalConnector> logicalConnectorOriginalList = originalService.getLogicalConnectors();

        if (logicalConnectorOriginalList != null)
        {
            List<LogicalConnector> logicalConnectorTargetList = new ArrayList<>();
            ResourceMigrationStatus resourceMigrationStatus = ResourceMigrationStatus.MIGRATED;

            for (LogicalConnector logicalConnector : logicalConnectorOriginalList)
            {
                // If logical connector is archived, copy it but set status to pending:
                if (LogicalConnectorStatus.ARCHIVED == logicalConnector.getLogicalConnectorStatus())
                {
                    logicalConnectorTargetList.add(logicalConnector);
                    resourceMigrationStatus = ResourceMigrationStatus.MIGRATED_ARCHIVED;
                }
                // Not deleted logical connector will be copied as they are:
                else if (logicalConnector.getDeletionDate() == null)
                {
                    logicalConnectorTargetList.add(logicalConnector);
                }
                // In any other case:
                else
                {
                    resourceMigrationStatus = ResourceMigrationStatus.PENDING_OTHER;
                }
            }

            migrationServiceDto.setLogicalConnectorStatus(resourceMigrationStatus.name());
            targetService.setLogicalConnectors(logicalConnectorTargetList);

            if (resourceMigrationStatus != ResourceMigrationStatus.MIGRATED)
            {
                serviceMigrationStatus = MigrationStatus.PENDING;
            }
        }

        // Copy Brokers
        ResourceMigrationStatus brokerMigrationStatus = this.migrateBrokersFromOriginalToTargetDeploymentService(originalService, targetService);
        migrationServiceDto.setBrokerStatus(brokerMigrationStatus.name());
        if (brokerMigrationStatus != ResourceMigrationStatus.MIGRATED)
        {
            serviceMigrationStatus = MigrationStatus.PENDING;
        }


        final boolean isMultiJdk = jvmJdkConfigurationChecker.isMultiJdk(originalReleaseVersionService);
        if (isMultiJdk)
        {
            final List<DeploymentServiceAllowedJdkParameterValue> originalJdkParamValues = originalService.getParamValues();
            if (log.isTraceEnabled())
            {
                log.trace("[DeploymentMigratorImpl][copyServiceToTarget]: Copying " + originalJdkParamValues.size() + " JDK parameters to target deployment service...");
            }
            if (originalJdkParamValues != null)
            {
                List<DeploymentServiceAllowedJdkParameterValue> targetJdkParamValues = new ArrayList<>(originalJdkParamValues.size());
                for (DeploymentServiceAllowedJdkParameterValue originalParamValue : originalJdkParamValues)
                {
                    DeploymentServiceAllowedJdkParameterValue targetParamValue = new DeploymentServiceAllowedJdkParameterValue();
                    targetParamValue.setDeploymentService(targetService);
                    targetParamValue.setAllowedJdkParameterProduct(originalParamValue.getAllowedJdkParameterProduct());
                    DeploymentServiceAllowedJdkParameterValueId targetParamId = new DeploymentServiceAllowedJdkParameterValueId();
                    targetParamId.setDeploymentServiceId(targetService.getId());
                    targetParamId.setAllowedJdkParameterProductId(originalParamValue.getAllowedJdkParameterProduct().getId());
                    targetParamValue.setId(targetParamId);
                    targetJdkParamValues.add(targetParamValue);
                }
                targetService.setParamValues(targetJdkParamValues);
            }

            entityManager.persist(targetService);
        }

        // Copy configurations.
        List<String> mismatchedProperties = this.copyConfiguration(originalService, targetService);

        if (!mismatchedProperties.isEmpty())
        {
            serviceMigrationStatus = MigrationStatus.CONFLICT;
            migrationServiceDto.setMismatchedPropertiesList(mismatchedProperties.toArray(new String[0]));
        }

        entityManager.persist(targetService);

        log.trace("Final target service {}.{} [{}] data: \n{}", targetService.getService().getGroupId(), targetService.getService().getArtifactId(), targetService.getService().getId(),
                targetService.toJSON());

        migrationServiceDto.setOriginalReleaseVersionServiceId(originalReleaseVersionService.getId());

        return serviceMigrationStatus;
    }

    private ResourceMigrationStatus migrateBrokersFromOriginalToTargetDeploymentService(final DeploymentService originalService, final DeploymentService targetService)
    {
        // Check if there is brokers assigned
        ResourceMigrationStatus resourceMigrationStatus = ResourceMigrationStatus.MIGRATED;
        List<Broker> brokerOriginalList = originalService.getBrokers();
        boolean implementsAnyApiBackToBack = targetService.getService().getApiImplementations().stream().anyMatch(apiImplementation -> ApiModality.ASYNC_BACKTOBACK.equals(apiImplementation.getApiModality()));
        if (brokerOriginalList != null && !brokerOriginalList.isEmpty())
        {
            if (implementsAnyApiBackToBack)
            {
                List<Broker> brokerTargetList = new ArrayList<>(brokerOriginalList);
                targetService.setBrokers(brokerTargetList);

                resourceMigrationStatus = ResourceMigrationStatus.MIGRATED;
            }
            else
            {
                resourceMigrationStatus = ResourceMigrationStatus.PENDING_OTHER;
            }
        }

        return resourceMigrationStatus;
    }

    private void updateBrokersPropertiesToDeploymentService(final DeploymentService targetService)
    {
        ConfigurationRevision targetConfigurationRevision = targetService.getDeploymentSubsystem().getDeploymentPlan().getCurrentRevision();

        List<DeploymentBrokerProperty> deploymentBrokerProperties = this.deploymentBroker.createAndPersistBrokerPropertiesOfDeploymentService(targetService, targetConfigurationRevision, targetService.getBrokers());

        targetService.getDeploymentBrokerProperties().addAll(deploymentBrokerProperties);
        targetConfigurationRevision.getDeploymentBrokerProperties().addAll(deploymentBrokerProperties);
    }

    /**
     * Copy configuration from a service to another.
     *
     * @param originalService - source Service
     * @param targetService   - Target Service
     * @return not matching properties list
     */
    public List<String> copyConfiguration(final DeploymentService originalService, DeploymentService targetService)
    {
        log.debug("Migrating configuration from service {}.{} [{}] to service [{}]", originalService.getService().getGroupId(), originalService.getService().getArtifactId(), originalService.getService().getId(), targetService.getService().getId());

        DeploymentPlan originalPlan = originalService.getDeploymentSubsystem().getDeploymentPlan();
        DeploymentPlan targetPlan = targetService.getDeploymentSubsystem().getDeploymentPlan();

        // Check original current revision.
        if (originalPlan.getCurrentRevision() == null)
        {
            // All plans must have a current revision.
            log.error("Trying to copy the configuration of a deployment plan [{}] without current revision.", originalPlan.getId());
            throw new NovaException(DeploymentError.getPlanWithoutCurrentVersionError(), "");
        }

        // Get all the original properties (broker properties are excluded because those associated to the new release version must be kept)
        List<PropertyDefinition> originalPropertiesToCopy = originalService.getService().getProperties().stream()
                .filter(propertyDefinition -> propertyDefinition.getManagement() != ManagementType.BROKER)
                .collect(Collectors.toList());

        // Copy from original to target service.
        log.debug("Original Properties to copy -> [{}]", originalPropertiesToCopy);

        for (PropertyDefinition property : originalPropertiesToCopy)
        {
            ConfigurationValue originalValue = getServicePropertyFromPlan(property, originalPlan);
            ConfigurationValue targetValue = getServicePropertyFromPlan(property, targetPlan);

            log.debug("Original value: [{}]", originalValue);
            log.debug("Target value: [{}]", targetValue);

            // In case of match, set the value
            if (originalValue != null && targetValue != null)
            {
                targetValue.setValue(originalValue.getValue());
            }
        }

        // Copy logical connector properties to deployment connector property list of the configuration revision
        for (LogicalConnector logicalConnector : originalService.getLogicalConnectors())
        {
            if (targetService.getLogicalConnectors().contains(logicalConnector))
            {
                log.debug("Logical connector original: [{}] is contained in the logical connector target list: [{}]. Thus, this properties will be included.", logicalConnector.getName(), targetService.getLogicalConnectors());

                // Get the configuration revision of the target service
                ConfigurationRevision targetConfigurationRevision = targetService.getDeploymentSubsystem().getDeploymentPlan().getCurrentRevision();

                for (LogicalConnectorProperty logicalConnectorProperty : logicalConnector.getLogConnProp())
                {
                    DeploymentConnectorProperty deploymentConnectorProperty = new DeploymentConnectorProperty();

                    deploymentConnectorProperty.setLogicalConnectorProperty(logicalConnectorProperty);
                    deploymentConnectorProperty.setDeploymentService(targetService);
                    deploymentConnectorProperty.setRevision(targetConfigurationRevision);

                    targetConfigurationRevision.getDeploymentConnectorProperties().add(deploymentConnectorProperty);
                }
            }
            else
            {
                log.debug("Logical connector original: [{}] is not contained in the logical connector target list: [{}]. Thus, any properties will be included into configuration revision",
                        logicalConnector.getName(), targetService.getLogicalConnectors());
            }
        }

        // create and copy brokers properties
        this.updateBrokersPropertiesToDeploymentService(targetService);

        // Not found target property in origin, set in mismatched properties list
        List<String> notMatchingProperties = targetService.getService().getProperties()
                .stream()
                .filter(propertyDefinition -> propertyDefinition.getManagement() != ManagementType.BROKER)
                .filter(propertyDefinition -> originalPropertiesToCopy.stream().noneMatch(propertyDefinition::isMatchingProperty))
                .map(PropertyDefinition::getName).collect(Collectors.toList());

        log.debug("[Deployments API] -> [copyConfiguration]: {} properties not found to copy. Values: [{}]", notMatchingProperties.size(), notMatchingProperties);

        return notMatchingProperties;
    }

    /**
     * Check if subsystem is in the given plan
     *
     * @param subsystem subsystem
     * @param plan      plan
     * @return true if it is in the plan
     */
    private boolean subsystemIsInPlan(DeploymentSubsystem subsystem, DeploymentPlan plan)
    {
        return plan.getDeploymentSubsystems().stream().anyMatch(s -> s.getSubsystem().getSubsystemId().equals(subsystem.getSubsystem().getSubsystemId()));
    }

    /**
     * Get subsystem from plan
     *
     * @param subsystem subsystem
     * @param plan      plan
     * @return subsystem
     */
    private DeploymentSubsystem getSubsystemFromPlan(DeploymentSubsystem subsystem, DeploymentPlan plan)
    {
        Optional<DeploymentSubsystem> match = plan.getDeploymentSubsystems().stream().filter(s -> s.getSubsystem().getSubsystemId().equals(subsystem.getSubsystem().getSubsystemId())).findFirst();

        return match.orElse(null);
    }

    /**
     * Check if service is in subsystem
     *
     * @param service   service
     * @param subsystem subsystem
     * @return true if it is in the subsystem
     */
    private boolean serviceIsInSubsystem(DeploymentService service, DeploymentSubsystem subsystem)
    {
        return subsystem.getDeploymentServices().stream().anyMatch(
                // Group and artifact IDs identifies a service but can have a different version.
                s -> s.getService().getGroupId().equals(service.getService().getGroupId()) && s.getService().getArtifactId().equals(service.getService().getArtifactId()));
    }

    /**
     * Get service from susbsystem
     *
     * @param service   service
     * @param subsystem subsystem
     * @return dweployment service
     */
    private DeploymentService getServiceFromSubsystem(DeploymentService service, DeploymentSubsystem subsystem)
    {
        Optional<DeploymentService> match = subsystem.getDeploymentServices().stream().filter(s -> s.getService().getGroupId().equals(service.getService().getGroupId()) && s.getService().getArtifactId().equals(service.getService().getArtifactId())).findFirst();

        return match.orElse(null);
    }

    /**
     * Get service property value from plan.
     *
     * @param property property
     * @param plan     plan
     * @return configuration value
     */
    private ConfigurationValue getServicePropertyFromPlan(final PropertyDefinition property, final DeploymentPlan plan)
    {
        Optional<ConfigurationValue> match = plan.getCurrentRevision().getConfigurations().stream()
                .filter(s -> s.getDefinition().isMatchingProperty(property) && s.getDefinition().getService().getServiceName().equals(property.getService().getServiceName()))
                .findFirst();

        return match.orElse(null);
    }

    /**
     * Migration status
     */
    public enum MigrationStatus
    {
        PENDING, MIGRATED, CONFLICT
    }

    /**
     * Filesystem Migration status
     */
    public enum FilesystemMigrationStatus
    {
        PENDING_CROSSPLATFORM, PENDING_OTHER, MIGRATED, MIGRATED_ARCHIVED
    }

    public enum HardwareMigrationStatus
    {
        PENDING_CROSSPLATFORM, MIGRATED, PENDING_INSTANCES
    }

    /**
     * Resource Migration status
     */
    public enum ResourceMigrationStatus
    {
        PENDING_OTHER, MIGRATED, MIGRATED_ARCHIVED
    }
}
