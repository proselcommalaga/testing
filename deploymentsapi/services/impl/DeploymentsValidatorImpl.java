package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceFilesystemDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentSubsystemDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.util.EnvironmentUtils;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsValidator;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.INotStartedServicesProvider;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Utility for business logic validations on {@link DeploymentPlan}s.
 */
@Slf4j
@Service
public class DeploymentsValidatorImpl implements IDeploymentsValidator
{
    /**
     * Error message
     */
    private static final String ERR_MSG = "Error caught with code {} calling {}";

    /**
     * Two number
     */
    private static final Integer TWO_NUMBER = 2;

    /**
     * Deployment plan repository
     */
    private final DeploymentPlanRepository deploymentPlanRepository;

    /**
     * Deployment subsystem repository
     */
    private final DeploymentSubsystemRepository deploymentSubsystemRepository;

    /**
     * Deployment service repository
     */
    private final DeploymentServiceRepository deploymentServiceRepository;

    /**
     * Deployment instance repository
     */
    private final DeploymentInstanceRepository deploymentInstanceRepository;

    private final INotStartedServicesProvider notStartedServicesProvider;

    private final ReleaseVersionRepository releaseVersionRepository;

    /**
     * Constructor
     *
     * @param deploymentPlanRepository      deployment plan repository
     * @param deploymentSubsystemRepository deployment subsystem repository
     * @param deploymentServiceRepository   deployment service repository
     * @param deploymentInstanceRepository  deployment instance repository
     * @param releaseVersionRepository      repository for ReleaseVersion
     */
    @Autowired
    public DeploymentsValidatorImpl(final DeploymentPlanRepository deploymentPlanRepository, final DeploymentSubsystemRepository deploymentSubsystemRepository,
                                    final DeploymentServiceRepository deploymentServiceRepository, final DeploymentInstanceRepository deploymentInstanceRepository,
                                    final INotStartedServicesProvider notStartedServicesProvider, final ReleaseVersionRepository releaseVersionRepository)
    {
        this.deploymentPlanRepository = deploymentPlanRepository;
        this.deploymentSubsystemRepository = deploymentSubsystemRepository;
        this.deploymentServiceRepository = deploymentServiceRepository;
        this.deploymentInstanceRepository = deploymentInstanceRepository;
        this.notStartedServicesProvider = notStartedServicesProvider;
        this.releaseVersionRepository = releaseVersionRepository;
    }

    /**
     * Extract the final name if the services has a runnable type.
     *
     * @param releaseVersionService which we want to check
     * @return a present value if the service has a runnable type, or empty in otherwise
     */
    private static Optional<String> getRunnableServicesVersionedFinalNames(@NotNull final ReleaseVersionService releaseVersionService)
    {
        final ServiceType serviceType = ServiceType.valueOf(releaseVersionService.getServiceType());
        final boolean isDependency = serviceType == ServiceType.DEPENDENCY;
        final boolean isLibrary = ServiceType.isLibrary(serviceType);
        final boolean isBatchSchedulerNova = serviceType == ServiceType.BATCH_SCHEDULER_NOVA;
        final boolean isRunnableType = !isDependency && !isLibrary && !isBatchSchedulerNova;
        if (isRunnableType)
        {
            return Optional.of(releaseVersionService.getFinalName() + ":" + releaseVersionService.getVersion());
        }

        return Optional.empty();
    }

    @Override
    public void checkReleaseVersionStatus(int versionId, ReleaseVersion version)
    {
        // Check the release version status:
        ReleaseVersionStatus releaseVersionStatus = version.getStatus();

        if (releaseVersionStatus == ReleaseVersionStatus.BUILDING)
        {
            log.error("Release version [" + versionId + "] is not Ready to deploy. In operation createDeployment");
            throw new NovaException(DeploymentError.getNotReadyReleaseVersionError(), "The release version [" + version.getVersionName() + "] is not ready to deploy.");
        }
        else if (releaseVersionStatus == ReleaseVersionStatus.ERRORS)
        {
            log.error("Release version [" + versionId + "] has errors and can't be deployed. In operation createDeployment");
            throw new NovaException(DeploymentError.getWrongReleaseVersionError(), "The release version [" + version.getVersionName() + "] has errors and can't be deployed.");
        }
        else if (releaseVersionStatus == ReleaseVersionStatus.STORAGED)
        {
            log.error("Trying to create a plan from a stored release version {}", versionId);
            throw new NovaException(DeploymentError.getStoredReleaseVersionError(), "");
        }
    }

    @Override
    public void checkPlanExistence(final DeploymentPlan plan) throws NovaException
    {
        // If the plan does not exist, throw exception.
        if (plan == null)
        {
            log.error(ERR_MSG, "checkPlanExistence", "deploymentPlan is NULL");
            throw new NovaException(DeploymentError.getNoSuchDeploymentError(), "The deployment plan is null");
        }
        else if (plan.getEnvironment() == null)
        {
            log.error(ERR_MSG, "checkPlanExistence", "deploymentPlan.getEnvironment is NULL, there is not deploymentId with ID: " + plan.getId());
            throw new NovaException(DeploymentError.getNoSuchDeploymentError(), "The deployment plan environment is null");
        }
        else
        {
            log.debug("[DeploymentsValidatorImpl] -> [checkPlanExistence]: the deployment plan [{}] is validated.", plan.getId());
        }
    }

    @Override
    public void checkDeploymentDate(DeploymentPlan plan)
    {
        if (plan != null && plan.getGcsp() != null && plan.getGcsp().getExpectedDeploymentDate() != null && plan.getDeploymentTypeInPro() == DeploymentType.PLANNED)
        {
            // Get the current date
            LocalDateTime currentDate = LocalDateTime.now();

            // Get the expected deployment date at midnight (0:00)
            LocalDateTime expectedDeploymentDate = LocalDate.ofInstant(plan.getGcsp().getExpectedDeploymentDate().toInstant(), ZoneId.systemDefault()).atStartOfDay();

            if (currentDate.isBefore(expectedDeploymentDate))
            {
                throw new NovaException(DeploymentError.getPlanDateDeniedError(plan.getId(), expectedDeploymentDate.toString(), currentDate.toString()));
            }
        }
        else if (plan != null && plan.getNova() != null && plan.getNova().getDeploymentDateTime() != null && plan.getDeploymentTypeInPro() == DeploymentType.NOVA_PLANNED
                && LocalDateTime.now().isBefore(LocalDateTime.ofInstant(plan.getNova().getDeploymentDateTime().toInstant(), ZoneId.systemDefault()).withSecond(0).withNano(0)))
        {
            throw new NovaException(DeploymentError.getPlanDateDeniedForScheduleError(plan.getId(), plan.getNova().getDeploymentDateTime().toString(), Calendar.getInstance().getTime().toString()));
        }
    }

    @Override
    public void checkNovaPlannedSchedulingDate(DeploymentPlan plan)
    {
        if (plan != null && plan.getNova() != null && plan.getNova().getDeploymentDateTime() != null && plan.getDeploymentTypeInPro() == DeploymentType.NOVA_PLANNED &&
                Calendar.getInstance().getTime().after(plan.getNova().getDeploymentDateTime()))
        {
            throw new NovaException(DeploymentError.getPlanDateDeniedForScheduleError(plan.getId(), plan.getNova().getDeploymentDateTime().toString(), Calendar.getInstance().getTime().toString()));
        }
    }

    @Override
    public void checkReleaseVersionStored(DeploymentPlan plan)
    {
        // If the plan does not exist, throw exception.
        if (plan.getReleaseVersion().getStatus() == ReleaseVersionStatus.STORAGED)
        {
            String errorMessage = "[DeploymentsValidatorImpl] -> [checkReleaseVersionStored]: Error trying to store a deployed plan id: [" + plan.getId() + "] with release version status: [" + plan.getReleaseVersion().getStatus() + "]. This is not allowed";
            log.error(errorMessage);
            throw new NovaException(DeploymentError.getStoredReleaseVersionError(), errorMessage);
        }
    }

    @Override
    public void checkPlanWithEphoenixDevelopmentEnvironmentNotPromotable(DeploymentPlan plan)
    {
        // If the plan has any ePhoenix for development environment, it cannot be promoted, throw exception.
        // get the release version service ids
        final boolean hasEphoenixDevelopmentEnvironmentNotPromotable = plan.getReleaseVersion().getAllReleaseVersionServices()
                .filter(service -> ServiceType.valueOf(service.getServiceType()).isEPhoenix())
                .map(ReleaseVersionService::getEphoenixData)
                .anyMatch(ephoenixService -> (ephoenixService.getDevelopmentEnvironment() && !ephoenixService.getDevelopmentEnvironmentPromotion()));

        if (hasEphoenixDevelopmentEnvironmentNotPromotable)
        {
            String errorMessage = "[DeploymentsValidatorImpl] -> [checkPlanWithEphoenixDevelopmentEnvironment]: Not allowed. Cannot promote due of the plan contains ePhoenix services for development environment.";
            log.error(errorMessage);
            throw new NovaException(DeploymentError.getPlanWithEphoenixDevelopmentEnvironmentNotPromotable(), errorMessage);
        }
    }

    @Override
    public void checkPlanNotStoragedAndNotReadyToDeploy(DeploymentPlan plan)
    {
        // Only plans on definition or deployed cannot be archived.
        if (plan.getStatus() == DeploymentStatus.DEFINITION || plan.getStatus() == DeploymentStatus.STORAGED || plan.getStatus() == DeploymentStatus.DEPLOYED || plan.getStatus() == DeploymentStatus.REJECTED)
        {
            String errorMessage = "[DeploymentsValidatorImpl] -> [checkPlanNotStoragedAndNotReadyToDeploy]: error trying to archive a deployed plan with id: [" + plan.getId() + "] and status: [" + plan.getStatus() + "]. This is not allowed.";
            log.error(errorMessage);
            throw new NovaException(DeploymentError.getTriedToArchivePlanError(), errorMessage);
        }
    }

    @Override
    public void checkDeploymentInstances(final DeploymentPlan deploymentPlan)
    {
        final String environment = deploymentPlan.getEnvironment();
        List<String> notStartedServices = new ArrayList<>();
        List<String> runnableServicesVersionedFinalNames = getRunnableServicesVersionedFinalNames(deploymentPlan);
        if (!runnableServicesVersionedFinalNames.isEmpty())
        {
            log.debug("[DeploymentsValidatorImpl] -> [checkDeploymentInstances]: checking service execution history for services in plan [{}].", deploymentPlan.getId());
            List<String> notStartedRunnableServiceVersionedFinalNames = notStartedServicesProvider.getNotStartedVersionedNamesFromServiceExecutionHistory(runnableServicesVersionedFinalNames, environment);
            if (!notStartedRunnableServiceVersionedFinalNames.isEmpty())
            {
                notStartedServices.addAll(notStartedRunnableServiceVersionedFinalNames);
            }

            if (!notStartedServices.isEmpty())
            {
                String notStartedServiceNames = notStartedServices.stream().map(i -> i.substring(i.indexOf("-") + 1, i.indexOf(":"))).reduce((a, b) -> a + ", " + b).orElse("");
                String errorMessage = "[DeploymentsValidatorImpl] -> [checkDeploymentInstances]: Not Allowed. The services : [" + notStartedServiceNames + "] have never been started." +
                        " Cannot promote due of is mandatory create instance of not started services of the plan.";

                throw new NovaException(DeploymentError.getNotAllStartedServicesForPromotingError(notStartedServiceNames), errorMessage);
            }
        }
        else
        {
            log.debug("[DeploymentsValidatorImpl] -> [checkDeploymentInstances]: no runnable services found in plan [{}].", deploymentPlan.getId());
        }

        log.debug("[DeploymentsValidatorImpl] -> [checkDeploymentInstances]: all services of the deployment plan id [{}] has been started at last one. Continue to promote.", deploymentPlan.getId());
    }

    @Override
    public void checkExistingPlansOfRV(@NotNull final Integer releaseVersionId, @NotNull final Environment environment)
    {

        log.debug("[DeploymentsValidatorImpl] -> [checkExistingPlansOfRV]: Check if the plan had been started in previous environments");

        if (!environment.equals(Environment.INT))
        {

            final ReleaseVersion releaseVersion = this.releaseVersionRepository.findById(releaseVersionId)
                    .orElseThrow(() -> new NovaException(ReleaseVersionError.getNoSuchReleaseVersionError(releaseVersionId),
                            "The release version [" + releaseVersionId + "] not found"));

            // if there is no plan in the same environment we will check if the services have ever been started
            if (releaseVersion.getDeployments().stream().noneMatch(deploymentPlan -> deploymentPlan.getEnvironment().equals(environment.getEnvironment())))
            {
                if (environment.equals(Environment.PRO))
                {
                    final String errorMessage = "[DeploymentsValidatorImpl] -> [checkExistingPlansOfRV]: The user wants to copy a different RV in PRO environment without promoting it";
                    throw new NovaException(DeploymentError.getNotPromotingError(), errorMessage);
                }

                this.validateIfServicesHadBeenStarted(releaseVersion, EnvironmentUtils.getPrevious(environment));
            }
            else
            {
                log.debug("[DeploymentsValidatorImpl] -> [checkExistingPlansOfRV]: There's a plan in the same environment [{}], so we can copy it without checking the services", environment);
            }

        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentPlan validateAndGetDeploymentPlan(int deploymentPlanId)
    {
        return this.deploymentPlanRepository.findById(deploymentPlanId)
                .orElseThrow(() -> new NovaException(DeploymentError.getPlanNotFoundError(deploymentPlanId), "deployment plan [" + deploymentPlanId + "] not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentPlan validateAndGetDeploymentPlanDeployed(int deploymentPlanId)
    {
        DeploymentPlan deploymentPlan = this.deploymentPlanRepository.findById(deploymentPlanId)
                .orElseThrow(() -> new NovaException(DeploymentError.getPlanNotFoundWarning(deploymentPlanId)));

        if (deploymentPlan.getStatus() != DeploymentStatus.DEPLOYED)
        {
            throw new NovaException(DeploymentError.getPlanDeployedNotFoundError(deploymentPlanId));
        }

        return deploymentPlan;
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentPlan validateAndGetDeploymentPlanToRemove(int deploymentPlanId)
    {
        DeploymentPlan deploymentPlan = this.deploymentPlanRepository.findById(deploymentPlanId).orElseThrow(() -> new NovaException(DeploymentError.getPlanNotFoundWarning(deploymentPlanId)));

        if (deploymentPlan.getStatus() != DeploymentStatus.DEPLOYED && deploymentPlan.getAction() != DeploymentAction.READY)
        {
            throw new NovaException(DeploymentError.getPlanDeployedNotFoundError(deploymentPlanId));
        }

        return deploymentPlan;
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentPlan validateAndGetDeploymentPlan(DeploymentService deploymentService)
    {
        DeploymentSubsystem deploymentSubsystem = this.deploymentSubsystemRepository.findById(deploymentService.getDeploymentSubsystem().getId())
                .orElseThrow(() -> new NovaException(DeploymentError.getSubsystemNotFoundError(deploymentService.getDeploymentSubsystem().getId())));
        int deploymentPlanId = deploymentSubsystem.getDeploymentPlan().getId();
        return this.deploymentPlanRepository.findById(deploymentPlanId)
                .orElseThrow(() -> new NovaException(DeploymentError.getPlanNotFoundError(deploymentPlanId), "deploymentPlan id: [" + deploymentPlanId + "] not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentService validateAndGetDeploymentService(final Integer deploymentServiceId)
    {
        return this.deploymentServiceRepository.findById(deploymentServiceId)
                .orElseThrow(() -> new NovaException(DeploymentError.getNoSuchServiceError(deploymentServiceId), "deployment service id: [" + deploymentServiceId + "] not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentSubsystem validateAndGetDeploymentSubsystem(int deploymentSubsystemId)
    {
        return this.deploymentSubsystemRepository.findById(deploymentSubsystemId)
                .orElseThrow(() -> new NovaException(DeploymentError.getSubsystemNotFoundError(deploymentSubsystemId), "deployment subsystem id: [" + deploymentSubsystemId + "] not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public DeploymentInstance validateAndGetDeploymentInstance(int deploymentInstanceId) throws NovaException
    {
        return this.deploymentInstanceRepository.findById(deploymentInstanceId)
                .orElseThrow(() -> new NovaException(DeploymentError.getDeploymentInstanceNotFoundError(deploymentInstanceId), "deployment instance id: [" + deploymentInstanceId + "] not found"));
    }

    @Override
    public void checkFilesystemVolumeBindsAreUnique(DeploymentDto deploymentDto)
    {
        for (DeploymentSubsystemDto deploymentSubsystemDto : deploymentDto.getSubsystems())
        {
            for (DeploymentServiceDto deploymentServiceDto : deploymentSubsystemDto.getServices())
            {
                this.checkFilesystemVolumeBindsAreUnique(deploymentServiceDto);
            }
        }
    }

    @Override
    public void checkFilesystemVolumeBindsAreUnique(DeploymentServiceDto deploymentServiceDto)
    {
        long numberOfUniqueVolumeBinds = Arrays.stream(deploymentServiceDto.getFilesystems())
                .map(DeploymentServiceFilesystemDto::getVolumeBind).distinct().count();
        if (numberOfUniqueVolumeBinds != deploymentServiceDto.getFilesystems().length)
        {
            throw new NovaException(DeploymentError.getDuplicatedVolumeBindsError());
        }
    }

    @Override
    public boolean checkDeploymentInstanceView(DeploymentService deploymentService)
    {
        boolean response = true;

        ServiceType serviceType = ServiceType.valueOf(deploymentService.getService().getServiceType());
        if (this.isBatchOrDependencyOrBatchScheduleOrLibrary(serviceType))
        {
            throw new NovaException(DeploymentError.getServiceTypeError(serviceType.getServiceType()));
        }

        DeploymentPlan deploymentPlan = deploymentService.getDeploymentSubsystem().getDeploymentPlan();

        if (deploymentPlan.getStatus() != DeploymentStatus.DEPLOYED)
        {
            log.warn("[DeploymentsValidatorImpl] -> [checkDeploymentInstanceView]: the deployment plan id [{}] is not ready for checking the status. Deployment plan status plan: [{}]." +
                    "Cannot check the deployment instance view. ", deploymentPlan.getId(), deploymentPlan.getStatus());
            response = false;
        }

        return response;
    }

    @Override
    public boolean isBatchOrDependencyOrBatchScheduleOrLibrary(final ServiceType serviceType)
    {
        return serviceType.isBatch()
                || ServiceType.DEPENDENCY == serviceType
                || ServiceType.BATCH_SCHEDULER_NOVA == serviceType
                || ServiceType.isLibrary(serviceType);
    }

    @Override
    public void validateInstancesNumberForMultiCPD(final @NotNull DeploymentPlan plan)
    {
        // Just check  PRODUCTION (as environment target)
        //Remember: it is trying to promote not to deploy. In PRE, user can edit plan

        final var selectedDeploy = PlatformUtils
                .getSelectedDeployForReleaseInEnvironment(plan.getReleaseVersion().getRelease(), Environment.PRO);

        if (selectedDeploy.equals(Platform.NOVA))
        {
            if (!Environment.PRE.getEnvironment().equals(plan.getEnvironment()))
            {
                log.debug("[DeploymentsValidatorImpl] -> [validateInstancesNumberForMultiCPD]: Not checking instances number for environment: [{}].", plan.getEnvironment());
            }
            else
            {
                final var productWithMultiCPDConfig = plan.getReleaseVersion().getRelease().getProduct().getMultiCPDInPro();

                if (Boolean.TRUE.equals(productWithMultiCPDConfig))
                {

                    var wrongServices = plan.getDeploymentSubsystems()
                            .stream()
                            .map(DeploymentSubsystem::getDeploymentServices)
                            .flatMap(List::stream)
                            .filter(deploymentService -> !this.areInstancesNumberWellConfigurated(deploymentService))
                            .collect(Collectors.toList());

                    // if there is any service with wrong number of instances, then it is necessary to throw an exception
                    if (!wrongServices.isEmpty())
                    {
                        var wrongServicesNames = wrongServices.stream().map(DeploymentService::getService).map(ReleaseVersionService::getServiceName).collect(Collectors.toList());
                        throw new NovaException(DeploymentError.getInvalidInstancesNumberError(wrongServicesNames));
                    }
                }
                else
                {
                    log.debug("[DeploymentsValidatorImpl] -> [validateInstancesNumberForMultiCPD]: Not checking instances number because the product is not configured on MultiCPD mode.");
                }

            }
        }
        else
        {
            log.debug("[DeploymentsValidatorImpl] -> [validateInstancesNumberForMultiCPD]: Not checking instances number for not NOVA platform.");
        }

    }

    @Override
    public void validateSamePlatformOnPREtoPRO(DeploymentPlan originalPlan, Environment environment)
    {
        final Platform selectedDeploy = PlatformUtils
                .getSelectedDeployForReleaseInEnvironment(originalPlan.getReleaseVersion().getRelease(), environment);

        // Not allowed change deployment platform when promoting plan from PRE to PRO
        if (originalPlan.getEnvironment().equals(Environment.PRE.getEnvironment()) && environment.equals(Environment.PRO)
                && originalPlan.getSelectedDeploy() != selectedDeploy)
        {
            throw new NovaException(DeploymentError.getTriedToPromotePlanChangingDeploymentPlatform(originalPlan.getId(), originalPlan.getSelectedDeploy().toString(), selectedDeploy.toString()));
        }
    }

    /**
     * Validate multiCPD configuration on PRO except for plans with only Ephoenix services
     *
     * @param originalPlan original plan with CPD configuration
     */
    @Override
    public void validateSameCPDConfigOnPRO(final @NotNull DeploymentPlan originalPlan)
    {
        final var productInMultiCPD = originalPlan.getReleaseVersion().getRelease().getProduct().getMultiCPDInPro();
        final var deploymentPlanInMultiCPD = originalPlan.getMultiCPDInPro();

        final var onlyEphoenixServices = originalPlan.getDeploymentSubsystems().stream()
                .flatMap(deploymentSubsystem -> deploymentSubsystem.getDeploymentServices().stream())
                .allMatch(deploymentService -> ServiceType.valueOf(deploymentService.getService().getServiceType()).isEPhoenix());


        if (originalPlan.getEnvironment().equals(Environment.PRO.getEnvironment()) && Boolean.TRUE.equals(productInMultiCPD)
                && Boolean.FALSE.equals(deploymentPlanInMultiCPD) && Boolean.FALSE.equals(onlyEphoenixServices))
        {
            final var errorMessage = "[DeploymentsValidatorImpl] -> [validateSameCPDConfigOnPRO]: Not Allowed. " +
                    "The original deployment plan [" + originalPlan.getId() + "] is running in MonoCPD mode while the product has a MultiCPD mode in PRO environment.";
            throw new NovaException(DeploymentError.getNoSameCPDConfigurationInPROEnvironment(originalPlan.getId()), errorMessage);
        }
    }

    /**
     * This method will check if the deploymentPlan's services had been run in the previous environment
     *
     * @param releaseVersion      with the subsystems and services of the product (it's needed to getting the services final names)
     * @param previousEnvironment current environment of the product
     * @throws NovaException if the services had not been run
     */
    private void validateIfServicesHadBeenStarted(@NotNull final ReleaseVersion releaseVersion, @NotNull final Optional<Environment> previousEnvironment)
    {

        // Get the services final names of the runnable ones
        final List<String> runnableServicesVersionedFinalNames = releaseVersion.getSubsystems().stream()
                .map(ReleaseVersionSubsystem::getServices)
                .flatMap(List::stream)
                .map(DeploymentsValidatorImpl::getRunnableServicesVersionedFinalNames)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());

        if (!runnableServicesVersionedFinalNames.isEmpty())
        {
            // We need the previous environment because we will check if the associated services had been started in that environment (not in the current)
            previousEnvironment.ifPresentOrElse(environment -> {

                log.debug("[DeploymentsValidatorImpl] -> [checkIfServicesHadBeenStarted]: checking service execution history for services in release version [{}].", releaseVersion.getId());
                final List<String> notStartedRunnableServiceVersionedFinalNames = notStartedServicesProvider.getNotStartedVersionedNamesFromServiceExecutionHistory(runnableServicesVersionedFinalNames, environment.getEnvironment());

                if (!notStartedRunnableServiceVersionedFinalNames.isEmpty())
                {
                    final String notStartedServiceNames = notStartedRunnableServiceVersionedFinalNames.stream().map(i -> i.substring(i.indexOf("-") + 1, i.indexOf(":"))).reduce((a, b) -> a + ", " + b).orElse("");
                    final String errorMessage = "[DeploymentsValidatorImpl] -> [validateIfServicesHadBeenStarted]: Not Allowed. The services : [" + notStartedServiceNames + "] have never been started in the previous environment." +
                            " Cannot copy due of is mandatory create instance of not started services of the plan.";
                    throw new NovaException(DeploymentError.getNotAllStartedServicesForCopyError(notStartedServiceNames, environment), errorMessage);
                }
            }, () -> log.error("[DeploymentsValidatorImpl] -> [checkIfServicesHadBeenStarted]: Failed getting the previous environment. Current environment is [{}]", previousEnvironment));

        }
        else
        {
            log.debug("[DeploymentsValidatorImpl] -> [checkIfServicesHadBeenStarted]: no runnable services found in release version [{}].", releaseVersion.getId());
        }

    }

    private List<String> getRunnableServicesVersionedFinalNames(DeploymentPlan deploymentPlan)
    {
        List<String> runnableServicesVersionedFinalNames = new ArrayList<>();
        for (DeploymentSubsystem deploymentSubsystem : deploymentPlan.getDeploymentSubsystems())
        {
            for (DeploymentService deploymentService : deploymentSubsystem.getDeploymentServices())
            {
                ReleaseVersionService releaseVersionService = deploymentService.getService();
                final ServiceType serviceType = ServiceType.valueOf(releaseVersionService.getServiceType());
                final boolean isDependency = ServiceType.DEPENDENCY.getServiceType().equals(serviceType.getServiceType());
                final boolean isLibrary = ServiceType.isLibrary(serviceType);
                final boolean isBatchSchedulerNova = serviceType == ServiceType.BATCH_SCHEDULER_NOVA;
                final boolean isRunnableType = !isDependency && !isLibrary && !isBatchSchedulerNova;
                if (isRunnableType)
                {
                    runnableServicesVersionedFinalNames.add(releaseVersionService.getFinalName() + ":" + releaseVersionService.getVersion());
                }
            }
        }
        return runnableServicesVersionedFinalNames;
    }

    /**
     * Check number of instance of services when multicpd is configured
     *
     * @param deploymentService deployment service with number of instances
     */
    private Boolean areInstancesNumberWellConfigurated(final DeploymentService deploymentService)
    {
        ServiceType serviceType = ServiceType.valueOf(deploymentService.getService().getServiceType());
        if (deploymentService.getNumberOfInstances() % TWO_NUMBER == 0
                || isBatchOrDependencyOrBatchScheduleOrLibrary(serviceType)
                || serviceType.isEPhoenix())
        {
            log.debug("[DeploymentManagerAPI] -> [checkInstancesNumberForMultiCPD]: ServiceType [{}] with right instances number: [{}] for multiCPD deployment plan.",
                    deploymentService.getService().getServiceType(), deploymentService.getNumberOfInstances());
            return true;
        }
        else
        {
            return false;
        }
    }
}
