package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleInstanceDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.SMBatchSchedulerExecutionsSummaryDTO;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.common.enums.BatchSchedulerInstanceStatus;
import com.bbva.enoa.platformservices.coreservice.common.enums.DeploymentBatchScheduleStatus;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;

import java.util.Date;
import java.util.Map;

/**
 * Feign Client for communication with Scheduler Manager.
 */
public interface ISchedulerManagerClient
{
    /**
     * Save a Batch scheduler service into the Scheduler manager (calling Scheduler manager client API) in the BBDD
     *
     * @param batchSchedulerServiceId   the release version service associate to the service
     * @param schedulerYmlFile          scheduler yml in string format
     * @param batchSchedulerServiceName batch scheduler name = release version service name
     * @param novaYmlFile               the nova yml in string format
     * @param batchIdServiceNameMap     map with batch id and batch name to schedule
     * @throws NovaException if errors from Scheduler manager service
     */
    void saveBatchSchedulerService(int batchSchedulerServiceId, byte[] schedulerYmlFile, String batchSchedulerServiceName, String novaYmlFile, Map<Integer, String> batchIdServiceNameMap);

    /**
     * Get a deployment batch schedule instance
     *
     * @param releaseVersionServiceId The release version service id for the scheduler
     * @param deploymentPlanId        the deployment Plan Id
     * @return a deployment batch schedule DTO
     */
    DeploymentBatchScheduleDTO getDeploymentBatchSchedule(Integer releaseVersionServiceId, Integer deploymentPlanId);

    /**
     * Remove a Batch schedule from Scheduler Manager in BBDD
     *
     * @param batchSchedulerServiceId the release version service id associated
     * @throws NovaException if errors from Scheduler manager service
     */
    void removeBatchSchedulerService(int batchSchedulerServiceId);

    /**
     * Create the deployment params associated to batch scheduler service and deployment plan
     *
     * @param releaseVersionServiceId the release version service id
     * @param deploymentPlanId        the deployment plan id
     * @param environment             the environment
     * @throws NovaException if errors from Scheduler manager service
     */
    void createDeploymentPlanContextParams(int releaseVersionServiceId, int deploymentPlanId, String environment);

    /**
     * Create a deployment batch schedule associated to batch scheduler service and deployment plan
     *
     * @param releaseVersionServiceId the release version service id
     * @param deploymentPlanId        the deployment plan id
     * @param releaseSubsystemId      releaseSubsystemId
     * @param environment             the environment
     * @throws NovaException if errors from Scheduler manager service
     */
    void createDeploymentBatchSchedule(int releaseVersionServiceId, int deploymentPlanId, int releaseSubsystemId, String environment);

    /**
     * Undeploy a deployment batch schedule of the batch schedule service associated
     *
     * @param releaseVersionServiceId       the release version service id
     * @param deploymentPlanId              the deployment plan id
     * @param deploymentBatchScheduleStatus the state of the deployment batch schedule service. Could be: ENABLED, DISABLED or UNDEPLOYED
     * @throws NovaException if errors from Scheduler manager service
     */
    void updateDeploymentBatchSchedule(int releaseVersionServiceId, int deploymentPlanId, DeploymentBatchScheduleStatus deploymentBatchScheduleStatus);

    /**
     * Remove a deployment batch schedule service associted to deployment plan
     *
     * @param releaseVersionServiceId the release version service id
     * @param deploymentPlanId        the deployment plan id
     * @throws NovaException if errors from Scheduler manager service
     */
    void removeDeploymentBatchSchedule(int releaseVersionServiceId, int deploymentPlanId);

    /**
     * Remove a deployment plan context params of the batch schedule service
     *
     * @param deploymentPlanId the deployment plan id
     * @throws NovaException if errors from Scheduler manager service
     */
    void removeDeploymentPlanContextParams(int deploymentPlanId);

    /**
     * Copy a deployment params associated to batch scheduler service and deployment plan
     *
     * @param originalReleaseVersionServiceId the release version service id
     * @param oldDeploymentPlanId             old deployment plan id to be copied
     * @param newDeploymentPlanId             new deployment plan to create the context params copied
     * @param environment                     the environment
     * @param targetReleaseVersionServiceId   release version service id to copy to (only applies when migrating a plan)
     * @throws NovaException if errors from Scheduler manager service
     */
    void copyDeploymentPlanContextParams(final int originalReleaseVersionServiceId, final int oldDeploymentPlanId, final int newDeploymentPlanId, final String environment, final Integer targetReleaseVersionServiceId);

    /**
     * Change the state of a scheduler instance (START, STOP, PAUSE, RESUME)
     *
     * @param batchScheduleInstanceId The id for the schedule instance
     * @param instanceStateType       The state (START, STOP, PAUSE, RESUME)
     * @return the batch schdule instance modified
     * @throws NovaException when error occurs
     */
    DeploymentBatchScheduleInstanceDTO stateBatchScheduleInstance(final Integer batchScheduleInstanceId, BatchSchedulerInstanceStatus instanceStateType);

    /**
     * Starts a scheduler instance
     *
     * @param releaseVersionServiceId The release version service id for the scheduler
     * @param deploymentPlanId        The deployment plan id for the schedule
     *                                If everything is ok - OK response
     */
    void startScheduleInstance(final Integer releaseVersionServiceId, final Integer deploymentPlanId);

    /**
     * Schedule a nova planned schedule
     *
     * @param deploymentPlan the deployment plan
     * @return true if the deployment plan is scheduled
     * @throws NovaException when error occurs
     */
    boolean scheduleDeployment(final DeploymentPlan deploymentPlan);

    /**
     * UnSchedule a nova planned schedule
     *
     * @param deploymentPlanId the deployment Plan Id
     * @throws NovaException when error occurs
     */
    void unscheduleDeployment(Integer deploymentPlanId);

    /**
     * Gets the batch scheduler service instances list
     *
     * @param releaseVersionService The release version service filter as batch scheduler service
     * @param deploymentPlan        The deployment plan where the release version service (batch scheduler service) is included
     * @return batch scheduler service instance list
     */
    DeploymentBatchScheduleInstanceDTO[] getDeploymentBatchScheduleInstances(ReleaseVersionService releaseVersionService, DeploymentPlan deploymentPlan);

    /**
     * Checks if a given date is disabled for deployment for specific UUAA
     *
     * @param date     Candidate for deployment date
     * @param uuaa     Specific UUAA
     * @param platform Platform where deployment will occur
     * @return true if given date is disabled for deployment for specific UUAA
     */
    boolean isDisabledDateForDeploy(final Date date, final String uuaa, final Platform platform);

    /**
     * Gets summarized info for scheduled batch instances.
     *
     * @param deploymentPlanIds An array of deployment plan ids related to deployment batch instances to be summarized.
     * @return A DTO with summarized info for deployment batch instances.
     */
    SMBatchSchedulerExecutionsSummaryDTO getBatchSchedulerExecutionsSummary(int[] deploymentPlanIds);

    /**
     * Get a deployment batch schedule instance by id
     *
     * @param deploymentBatchScheduleInstanceId the deployment batch schedule instance id
     * @return a DeploymentBatchScheduleInstanceDTO
     */
    DeploymentBatchScheduleInstanceDTO getDeploymentBatchScheduleInstanceById(final Integer deploymentBatchScheduleInstanceId);
}
