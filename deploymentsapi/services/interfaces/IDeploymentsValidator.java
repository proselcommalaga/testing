package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

/**
 * DeploymentValidator interface
 */
public interface IDeploymentsValidator
{
    /**
     * Check status
     *
     * @param versionId version id
     * @param version   version
     */
    void checkReleaseVersionStatus(int versionId, ReleaseVersion version);

    /**
     * Validates if a {@link DeploymentPlan} does exist and if not,
     * throws a {@link NovaException}.
     *
     * @param plan {@link NovaException}
     */
    void checkPlanExistence(DeploymentPlan plan) throws NovaException;

    /**
     * Check deployment date
     *
     * @param plan deployment date
     */
    void checkDeploymentDate(DeploymentPlan plan);

    /**
     * Check Nova deployment date
     *
     * @param plan deployment date
     */
    void checkNovaPlannedSchedulingDate(DeploymentPlan plan);

    /**
     * Validates if a {@link DeploymentPlan} belongs to a stored release version
     *
     * @param plan {@link NovaException}
     */
    void checkReleaseVersionStored(DeploymentPlan plan);

    /**
     * Validates if a {@link DeploymentPlan} has any ePhoenix for development environment
     *
     * @param plan {@link NovaException}
     */
    void checkPlanWithEphoenixDevelopmentEnvironmentNotPromotable(DeploymentPlan plan);

    /**
     * Validates if a {@link DeploymentPlan} NOT belongs to a stored release version
     * and Only plans NOT on definition can be Storaged
     *
     * @param plan {@link NovaException}
     */
    void checkPlanNotStoragedAndNotReadyToDeploy(DeploymentPlan plan);

    /**
     * Check if all deployment service has at last one deployment instance except DEPENDENCY and LIBRARY service type
     * In NOVA mode, is mandatory that deployment (except DEPENDENCY and LIBRARY) services mast have been started to be able to promote to next environment
     * In ETHER mode, there is not deployment instance, ignored
     *
     * @param deploymentPlan the plan to check all deployment services
     */
    void checkDeploymentInstances(DeploymentPlan deploymentPlan);

    /**
     * Check if the release version (that we want to copy) has <strong>DEPLOYED</strong> or <strong>STORAGED</strong> plans in the previous environments with every service started.
     * If the release version has a  <strong>DEFINITION</strong> plan in the same environment, it will not check if the services have ever been started.
     *
     * @param releaseVersionId that we want to make the copy
     * @param environment      current environment where we want to copy the plan
     */
    void checkExistingPlansOfRV(Integer releaseVersionId, Environment environment);

    /**
     * Validate and get deployment plan by given id
     *
     * @param deploymentPlanId deployment plan
     * @return deployment plan identifier by given id
     */
    DeploymentPlan validateAndGetDeploymentPlan(int deploymentPlanId);

    /**
     * Validate and get deployment plan by given id in status DEPLOYED
     *
     * @param deploymentPlanId the deployment plan id
     * @return a deployment plan
     * @throws NovaException if error
     */
    DeploymentPlan validateAndGetDeploymentPlanDeployed(int deploymentPlanId) throws NovaException;

    /**
     * Validate and get deployment plan by given id in status DEPLOYED and action READY
     *
     * @param deploymentPlanId the deployment plan id
     * @return a deployment plan
     * @throws NovaException if error
     */
    DeploymentPlan validateAndGetDeploymentPlanToRemove(int deploymentPlanId) throws NovaException;

    /**
     * Validate and get deployment plan by given id
     *
     * @param deploymentService deployment service
     * @return deployment plan by given service
     */
    DeploymentPlan validateAndGetDeploymentPlan(DeploymentService deploymentService);

    /**
     * Validate and get deployment service by given id
     *
     * @param deploymentServiceId deployment service
     * @return deployment plan identifier by given id
     */
    DeploymentService validateAndGetDeploymentService(Integer deploymentServiceId);

    /**
     * Validate and get deployment subsystem by given id
     *
     * @param deploymentSubsystemId deployment subsystem
     * @return deployment subsystem identifier by given id
     */
    DeploymentSubsystem validateAndGetDeploymentSubsystem(int deploymentSubsystemId) throws NovaException;

    /**
     * Validate and get deployment instance by given id
     *
     * @param deploymentInstanceId the deployment instance id
     * @return a deployment instance
     * @throws NovaException if any error
     */
    DeploymentInstance validateAndGetDeploymentInstance(int deploymentInstanceId) throws NovaException;

    /**
     * If there are repeated volume binds, throws an exception.
     *
     * @param deploymentDto the deployment dto
     */
    void checkFilesystemVolumeBindsAreUnique(DeploymentDto deploymentDto);

    /**
     * If there are repeated volume binds, throws an exception.
     *
     * @param deploymentServiceDto the deployment service dto
     */
    void checkFilesystemVolumeBindsAreUnique(DeploymentServiceDto deploymentServiceDto);

    /**
     * Check all the conditions for deployment instances view
     *
     * @param deploymentService deployment service
     * @return true if the check was success, false any other case
     */
    boolean checkDeploymentInstanceView(DeploymentService deploymentService);

    /**
     * Check if a service type is batch or dependency or batch scheduler or library service
     *
     * @param serviceType the service type
     * @return true if the service type is batch or dependency or batch scheduler or library service. False any other case
     */
    boolean isBatchOrDependencyOrBatchScheduleOrLibrary(final ServiceType serviceType);

    /**
     * This method will check if the promotion is from PRE to PRO, if the promotion platform is NOVA, if the product is running in MultiCPD mode
     * and if the plan has services with an odd number of instances. This validation is needed to ensure that
     * the plan promoted has the correct number of instances configured.
     *
     * @param plan        original deployment plan
     * @throws NovaException getInvalidInstancesNumberError
     */
    void validateInstancesNumberForMultiCPD(DeploymentPlan plan);

    /**
     * Check if the original environment is PRE and the destination is PRO, and also if the PRO
     * selected deployment infrastructure is not equals to the PRE infrastructure (NOVA to/from ETHER)
     *
     * @param originalPlan deployment plan that will be promoted
     * @param environment  where the new deployment plan will go
     */
    void validateSamePlatformOnPREtoPRO(DeploymentPlan originalPlan, Environment environment);

    /**
     * Check if the product is configured in MultiCPD and the original plan is running in MonoCPD configuration.
     *
     * @param originalPlan deployment plan that will be copied
     */
    void validateSameCPDConfigOnPRO(DeploymentPlan originalPlan);
}
