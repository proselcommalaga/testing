package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.interfaces;

import com.bbva.enoa.apirestgen.servicerunnerapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;

public interface IServiceRunner
{
    /**
     * Starts the given instance.
     *
     * @param userCode - {@code PortalUser} who invokes the operation
     * @param instance - DeploymentInstance to start
     * @throws Errors error
     */
    void startInstance(String userCode, DeploymentInstance instance) throws Errors;

    /**
     * Stops the given instance.
     *
     * @param userCode - {@code PortalUser} who invokes the operation
     * @param instance - DeploymentInstance to stop
     */
    void stopInstance(String userCode, DeploymentInstance instance);

    /**
     * Starts all instances of a single service.
     *
     * @param userCode - {@code PortalUser} who invokes the operation
     * @param service  - {@code DeploymentService} to start
     * @throws Errors error
     */
    void startService(String userCode, DeploymentService service) throws Errors;

    /**
     * Stops all instances of a single service.
     *
     * @param userCode - {@code PortalUser} who invokes the operation
     * @param service  - {@code DeploymentService} to stop
     */
    void stopService(String userCode, DeploymentService service);

    /**
     * Starts a whole subsystem: all of its services and the instances belonging to them.
     *
     * @param userCode  - {@code PortalUser} who invokes the operation
     * @param subsystem - {@code DeploymentSubsystem} to start
     * @throws Errors error
     */
    void startSubsystem(String userCode, DeploymentSubsystem subsystem) throws Errors;

    /**
     * Stops a whole subsystem: all of its services and the instances belonging
     * to them.
     *
     * @param userCode  - {@code PortalUser} who invokes the operation
     * @param subsystem - {@code DeploymentSubsystem} to stop
     */
    void stopSubsystem(String userCode, DeploymentSubsystem subsystem);

    /**
     * Starts a whole {@link DeploymentPlan}: all of its subsystems, services
     * and instances.
     *
     * @param userCode       - {@code PortalUser} who invokes the operation
     * @param deploymentPlan - {@code DeploymentPlan} to start
     * @throws Errors error
     */
    void startDeploymentPlan(String userCode, DeploymentPlan deploymentPlan) throws Errors;

    /**
     * Stops a whole {@link DeploymentPlan}: all of its subsystems, services and
     * instances.
     *
     * @param userCode       - {@code PortalUser} who invokes the operation
     * @param deploymentPlan - {@code DeploymentPlan} to stop
     */
    void stopDeploymentPlan(String userCode, DeploymentPlan deploymentPlan);

    /**
     * Creates a task to request an action over a {@link DeploymentInstance} on {@link Environment}.PRO.
     *
     * @param userCode user
     * @param instance {@link DeploymentInstance}
     * @param type     {@link ToDoTaskType}
     * @return Task ID.
     * @throws NovaException error
     */
    TodoTaskResponseDTO createProductionTask(String userCode, DeploymentInstance instance, ToDoTaskType type)
            throws NovaException;

    /**
     * Creates a task to request an action over a {@link DeploymentService} on {@link Environment}.PRO.
     *
     * @param userCode user
     * @param service  {@link DeploymentService}
     * @param type     {@link ToDoTaskType}
     * @return Task ID.
     * @throws NovaException error
     */
    TodoTaskResponseDTO createProductionTask(String userCode, DeploymentService service, ToDoTaskType type)
            throws NovaException;

    /**
     * Creates a task to request an action over a {@link DeploymentSubsystem} on {@link Environment}.PRO.
     *
     * @param userCode  user
     * @param subsystem {@link DeploymentSubsystem}
     * @param type      {@link ToDoTaskType}
     * @return Task ID.
     * @throws NovaException error
     */
    TodoTaskResponseDTO createProductionTask(String userCode, DeploymentSubsystem subsystem, ToDoTaskType type)
            throws NovaException;

    /**
     * Creates a task to request an action over a {@link DeploymentPlan} on {@link Environment}.PRO.
     *
     * @param userCode user
     * @param plan     {@link DeploymentPlan}
     * @param type     {@link ToDoTaskType}
     * @return Task ID.
     * @throws NovaException error
     */
    TodoTaskResponseDTO createProductionTask(String userCode, DeploymentPlan plan, ToDoTaskType type)
            throws NovaException;

    /**
     * Creates a task to request an action over a {@link DeploymentPlan} on {@link Environment}.PRO.
     *
     * @param userCode           user
     * @param scheduleInstanceId id of the schedule instance
     * @param plan               {@link DeploymentPlan}
     * @param type               {@link ToDoTaskType}
     * @return Task ID.
     * @throws NovaException error
     */
    TodoTaskResponseDTO createBatchScheduleServiceProductionTask(String userCode, Integer scheduleInstanceId, DeploymentPlan plan, ToDoTaskType type) throws NovaException;

    /**
     * Ejecuta una tarea de gestión aceptada.
     *
     * @param task the ManagementActionTask
     * @throws Errors error
     */
    void processTask(ManagementActionTask task) throws Errors;

    /**
     * Starts a whole {@link DeploymentPlan}: all of its subsystems, services and instances.
     *
     * @param userCode       - {@code PortalUser} who invokes the operation
     * @param deploymentPlan - {@code DeploymentPlan} to restart
     * @throws Errors error
     */
    void restartDeploymentPlan(String userCode, DeploymentPlan deploymentPlan) throws Errors;

    /**
     * Restarts the given instance.
     *
     * @param userCode - {@code PortalUser} who invokes the operation
     * @param instance - DeploymentInstance to restart
     * @throws Errors error
     */
    void restartInstance(String userCode, DeploymentInstance instance) throws Errors;

    /**
     * Restarts a whole subsystem: all of its services and the instances belonging to them.
     *
     * @param userCode  - {@code PortalUser} who invokes the operation
     * @param subsystem - {@code DeploymentSubsystem} to restart
     * @throws Errors error
     */
    void restartSubsystem(String userCode, DeploymentSubsystem subsystem) throws Errors;

    /**
     * Restarts all instances of a single service.
     *
     * @param userCode - {@code PortalUser} who invokes the operation
     * @param service  - {@code DeploymentService} to restart
     * @throws Errors error
     */
    void restartService(String userCode, DeploymentService service) throws Errors;

    /**
     * Starts an instance of a batch schedule.
     *
     * @param userCode - {@code PortalUser} who invokes the operation
     * @param service  - {@code DeploymentService} to start
     */
    void startBatchSchedule(String userCode, DeploymentService service);

    /**
     * Stops an instance of a batch schedule.
     *
     * @param userCode - {@code PortalUser} who invokes the operation
     * @param service  - {@code DeploymentService} to stop
     */
    void stopBatchSchedule(String userCode, DeploymentService service);

    /**
     * Resume  Batch Schedule  instance.
     *
     * @param userCode           - {@code PortalUser} who invokes the operation
     * @param scheduleInstanceId - scheduleInstance id to stop
     */
    void resumeBatchScheduleInstance(String userCode, Integer scheduleInstanceId);

    /**
     * Pause  Batch Schedule  instance.
     *
     * @param userCode           - {@code PortalUser} who invokes the operation
     * @param scheduleInstanceId - scheduleInstance id to stop
     */
    void pauseBatchScheduleInstance(String userCode, Integer scheduleInstanceId);

    /**
     * Stops Batch Schedule  instance.
     *
     * @param userCode           - {@code PortalUser} who invokes the operation
     * @param scheduleInstanceId - scheduleInstance id to stop
     */
    void stopBatchScheduleInstance(final String userCode, final Integer scheduleInstanceId);

    /**
     * Start batch schedule
     *
     * @param userCode          the user code
     * @param deploymentService the deploymentService
     * @param plan              deployment plan
     */
    void startBatchScheduleInstance(String userCode, DeploymentService deploymentService, DeploymentPlan plan);

}
