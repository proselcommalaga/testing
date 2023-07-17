package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.interfaces;

import com.bbva.enoa.apirestgen.servicerunnerapi.model.TodoTaskResponseDTO;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;

/**
 * Runner Service
 */
public interface IRunnerApiService
{
    /**
     * Stop batch schedule instance.
     *
     * @param scheduleInstanceId with the scheduler instance id
     * @param deploymentPlanId   with the deployment plan id
     * @param ivUser             with the iv user
     * @return todotask response dto
     */
    TodoTaskResponseDTO stopBatchScheduleInstance(final Integer scheduleInstanceId, final Integer deploymentPlanId, final String ivUser);

    /**
     * On service runner task reply.
     *
     * @param taskId    with the task id
     * @param newStatus with the new status
     * @throws Errors error
     */
    void onServiceRunnerTaskReply(final Integer taskId, final String newStatus) throws Errors;

    /**
     * Resume batch schedule instance.
     *
     * @param scheduleInstanceId with the scheduler instance id
     * @param deploymentPlanId   with the deployment plan id
     * @param ivUser             with the iv user
     * @return the todotask response dto
     */
    TodoTaskResponseDTO resumeBatchScheduleInstance(final Integer scheduleInstanceId, final Integer deploymentPlanId, final String ivUser);

    /**
     * Pause batch schedule instance.
     *
     * @param scheduleInstanceId with the scheduler instance id
     * @param deploymentPlanId   with the deployment plan id
     * @param ivUser             with the iv user
     * @return the todotask response dto
     */
    TodoTaskResponseDTO pauseBatchScheduleInstance(final Integer scheduleInstanceId, final Integer deploymentPlanId, final String ivUser);

    /**
     * Stop subsystem todotask response dto.
     *
     * @param subsystemId with the subsystem id
     * @param ivUser      with the iv user
     * @return the todotask response dto
     */
    TodoTaskResponseDTO stopSubsystem(final Integer subsystemId, final String ivUser);

    /**
     * Start plan todotask response dto.
     *
     * @param planId with the plan id
     * @param ivUser with the iv user
     * @return the todotask response dto
     * @throws Errors error
     */
    TodoTaskResponseDTO startPlan(final Integer planId, final String ivUser) throws Errors;

    /**
     * Start batch schedule instance.
     *
     * @param releaseVersionServiceId with the release version service id
     * @param deploymentPlanId        with the deployment plan id
     * @param ivUser                  with the iv user
     * @return todotask response dto
     */
    TodoTaskResponseDTO startBatchScheduleInstance(final Integer releaseVersionServiceId, final Integer deploymentPlanId, final String ivUser);

    /**
     * Start batch schedule.
     *
     * @param serviceId with the service id
     * @param ivUser    with the iv user
     * @return the todotask response dto
     */
    TodoTaskResponseDTO startBatchSchedule(final Integer serviceId, final String ivUser);

    /**
     * Restart plan todotask response dto.
     *
     * @param planId with the plan id
     * @param ivUser with the iv user
     * @return the todotask response dto
     * @throws Errors error
     */
    TodoTaskResponseDTO restartPlan(final Integer planId, final String ivUser) throws Errors;

    /**
     * Start instance todotask response dto.
     *
     * @param instanceId with the instance id
     * @param ivUser     with the iv user
     * @return the todotask response dto
     * @throws Errors error
     */
    TodoTaskResponseDTO startInstance(final Integer instanceId, final String ivUser) throws Errors;

    /**
     * Restart subsystem todotask response dto.
     *
     * @param subsystemId with the subsystem id
     * @param ivUser      with the iv user
     * @return the todotask response dto
     * @throws Errors error
     */
    TodoTaskResponseDTO restartSubsystem(final Integer subsystemId, final String ivUser) throws Errors;

    /**
     * Restart service todotask response dto.
     *
     * @param serviceId with the deployment service id
     * @param ivUser    with the iv user
     * @return the todotask response dto
     * @throws Errors error
     */
    TodoTaskResponseDTO restartService(final Integer serviceId, final String ivUser) throws Errors;

    /**
     * Start subsystem todotask response dto.
     *
     * @param subsystemId with the subsystem id
     * @param ivUser      with the iv user
     * @return the todotask response dto
     * @throws Errors error
     */
    TodoTaskResponseDTO startSubsystem(final Integer subsystemId, final String ivUser) throws Errors;

    /**
     * Restart instance todotask response dto.
     *
     * @param instanceId with the instance id
     * @param ivUser     with the iv user
     * @return the todotask response dto
     * @throws Errors error
     */
    TodoTaskResponseDTO restartInstance(final Integer instanceId, final String ivUser) throws Errors;

    /**
     * Stop instance todotask response dto.
     *
     * @param instanceId with the instance id
     * @param ivUser     with the iv user
     * @return the todotask response dto
     */
    TodoTaskResponseDTO stopInstance(final Integer instanceId, final String ivUser);

    /**
     * Stop plan todotask response dto.
     *
     * @param planId with the plan id
     * @param ivUser with the iv user
     * @return todotask response dto
     */
    TodoTaskResponseDTO stopPlan(final Integer planId, final String ivUser);

    /**
     * Stop service todotask response dto.
     *
     * @param serviceId with the service id
     * @param ivUser    with the iv user
     * @return the todotask response dto
     */
    TodoTaskResponseDTO stopService(final Integer serviceId, final String ivUser);

    /**
     * Stop batch schedule.
     *
     * @param serviceId with the service id
     * @param ivUser    with the iv user
     * @return the todotask response dto
     */
    TodoTaskResponseDTO stopBatchSchedule(final Integer serviceId, final String ivUser);

    /**
     * Start service todotask response dto.
     *
     * @param serviceId with the service id
     * @param ivUser    with the iv user
     * @return the todotask response dto
     * @throws Errors error
     */
    TodoTaskResponseDTO startService(final Integer serviceId, final String ivUser) throws Errors;

    /**
     * Get the values we can filter by when obtaining instances statistics.
     *
     * @return An array containing the values we can filter by when obtaining instances statistics.
     */
    String[] getInstancesStatuses();

    /**
     * Restart ephoenix instance by Control-M planification
     * Some projects have the requirement of restart periodically them ePhoenix instances.
     *
     *
     * @param ivUser        the iv user
     * @param environment   the environment
     * @param hostname      the hostname
     * @param productName   the productName
     * @param releaseName   the release name
     * @param subsystemName the subsystem name
     * @param serviceName   the service name
     * @return the deployment instance id of te instance restarted
     */
    Integer restartEphoenixInstanceByPlanification(final String ivUser, final String environment,final String hostname,final String productName,final String releaseName,final String subsystemName,final String serviceName) throws Errors;
}
