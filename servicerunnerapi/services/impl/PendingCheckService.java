package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.services.impl;


import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ManagementActionTaskRepository;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.exceptions.ServiceRunnerError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for checking pending tasks in Instance, Service, Subsystem and Plan
 */
@Slf4j
@Service
public class PendingCheckService
{
    /**
     * Management action task repository
     */
    @Autowired
    private ManagementActionTaskRepository actionTaskRepository;

    /**
     * Check pending tasks to start, restart or stop a DeploymentPlan.
     *
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @param plan             deployment plan
     * @throws NovaException if there are any pending tasks
     */
    public void checkPendingTasks(DeploymentPlan plan, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {
        log.debug("[PendingCheckService] -> [checkPendingTasks]: plan ID: " + plan.getId());
        this.checkPlan(plan, toDoTaskTypeList);
        List<ToDoTaskType> subsystemTypes = new ArrayList<>();
        subsystemTypes.add(ToDoTaskType.SUBSYSTEM_START);
        subsystemTypes.add(ToDoTaskType.SUBSYSTEM_RESTART);
        subsystemTypes.add(ToDoTaskType.SUBSYSTEM_STOP);
        this.checkChildren(plan, subsystemTypes);
    }

    /**
     * Check pending tasks to start, restart or stop a DeploymentSubsystem
     *
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @param subsystem        deployment plan
     * @throws NovaException if there are any pending tasks
     */
    public void checkPendingTasks(DeploymentSubsystem subsystem, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {
        log.debug("[PendingCheckService] -> [checkPendingTasks]: subsystem ID: " + subsystem.getId());
        // Check if another subsystem todotask is open for the same subsystem
        this.checkSubsystem(subsystem, toDoTaskTypeList);
        // Check if one of his children services have pending todotask
        List<ToDoTaskType> serviceTypes = new ArrayList<>();
        serviceTypes.add(ToDoTaskType.SERVICE_START);
        serviceTypes.add(ToDoTaskType.SERVICE_RESTART);
        serviceTypes.add(ToDoTaskType.SERVICE_STOP);
        this.checkChildren(subsystem, serviceTypes);
        // Check if another todotask is open for related plan
        List<ToDoTaskType> releaseTypes = new ArrayList<>();
        releaseTypes.add(ToDoTaskType.RELEASE_START);
        releaseTypes.add(ToDoTaskType.RELEASE_RESTART);
        releaseTypes.add(ToDoTaskType.RELEASE_STOP);
        this.checkParent(subsystem.getDeploymentPlan(), releaseTypes);
    }

    /**
     * Check pending tasks to start, restart or stop a DeploymentService.
     *
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @param service          deployment instance
     * @throws NovaException if there are any pending tasks
     */
    public void checkPendingTasks(DeploymentService service, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {
        log.debug("[PendingCheckService] -> [checkPendingTasks]: service ID: " + service.getId());
        this.checkService(service, toDoTaskTypeList);
        List<ToDoTaskType> instanceTypes = new ArrayList<>();
        instanceTypes.add(ToDoTaskType.CONTAINER_START);
        instanceTypes.add(ToDoTaskType.CONTAINER_RESTART);
        instanceTypes.add(ToDoTaskType.CONTAINER_STOP);
        this.checkChildren(service, instanceTypes);
        List<ToDoTaskType> subsystemTypes = new ArrayList<>();
        subsystemTypes.add(ToDoTaskType.SUBSYSTEM_START);
        subsystemTypes.add(ToDoTaskType.SUBSYSTEM_RESTART);
        subsystemTypes.add(ToDoTaskType.SUBSYSTEM_STOP);
        this.checkParent(service.getDeploymentSubsystem(), subsystemTypes);
    }

    /**
     * Check pending tasks to start, restart or stop a DeploymentInstance
     *
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @param instance         deployment instance
     * @throws NovaException if there are any pending tasks
     */
    public void checkPendingTasks(DeploymentInstance instance, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {
        log.debug("[PendingCheckService] -> [checkPendingTasks]: instance ID: " + instance.getId());
        this.checkInstance(instance, toDoTaskTypeList);
        List<ToDoTaskType> serviceTypes = new ArrayList<>();
        serviceTypes.add(ToDoTaskType.SERVICE_START);
        serviceTypes.add(ToDoTaskType.SERVICE_RESTART);
        serviceTypes.add(ToDoTaskType.SERVICE_STOP);
        this.checkParent(instance.getService(), serviceTypes);
    }

    /**
     * Check pending tasks to pause, restart or stop a Scheduler manager instance
     *
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @param instance         scheduler manager instance
     * @throws NovaException if there are any pending tasks
     */
    public void checkPendingTasks(Integer instance, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {
        log.debug("[PendingCheckService] -> [checkPendingTasks]: scheduler manager instance ID: " + instance);
        this.checkInstance(instance, toDoTaskTypeList);
    }

    ///////////////////// Private methods

    /**
     * Check plan
     *
     * @param plan             plan
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @throws NovaException when action has already been requested
     */
    private void checkPlan(DeploymentPlan plan, List<ToDoTaskType> toDoTaskTypeList) throws
            NovaException
    {
        List<ManagementActionTask> managementActionTaskList = new ArrayList<>();
        for (ToDoTaskType toDoTaskType : toDoTaskTypeList)
        {
            managementActionTaskList.addAll(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(plan.getId(), toDoTaskType,
                    List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)));
        }
        if (!managementActionTaskList.isEmpty())
        {
            log.debug("[PendingCheckService]->[checkPlan]: Plan with id {} has {} pending tasks", plan.getId(), managementActionTaskList
                    .size());
            throw new NovaException(ServiceRunnerError.getPendingActionTaskError(), "Plan has an action already requested (start, stop " +
                    "or restart)");
        }
    }

    /**
     * Check subsystem
     *
     * @param subsystem        subsystem
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @throws NovaException when action has already been requested
     */
    private void checkSubsystem(DeploymentSubsystem subsystem, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {
        List<ManagementActionTask> managementActionTaskList = new ArrayList<>();
        for (ToDoTaskType toDoTaskType : toDoTaskTypeList)
        {
            managementActionTaskList.addAll(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(subsystem.getId(), toDoTaskType,
                    List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)));
        }
        if (!managementActionTaskList.isEmpty())
        {
            log.debug("[PendingCheckService]->[checkSubsytem]: Subsystem with id {} has {} pending tasks", subsystem.getId(),
                    managementActionTaskList.size());
            throw new NovaException(ServiceRunnerError.getPendingActionTaskError(), "Subsystem has an action already requested (start, " +
                    "stop or restart)");
        }
    }

    /**
     * Check service
     *
     * @param service          service
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @throws NovaException when action has already been requested
     */
    private void checkService(DeploymentService service, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {
        List<ManagementActionTask> managementActionTaskList = new ArrayList<>();
        for (ToDoTaskType toDoTaskType : toDoTaskTypeList)
        {
            managementActionTaskList.addAll(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(
                    service.getId(), toDoTaskType, List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)));
        }
        if (!managementActionTaskList.isEmpty())
        {
            log.debug("[PendingCheckService]->[checkService]: Service with id {} has {} pending tasks", service.getId(),
                    toDoTaskTypeList.size());
            throw new NovaException(ServiceRunnerError.getPendingActionTaskError(), "Service has an action already requested (start, " +
                    "restart or stop)");
        }
    }

    /**
     * Check instance
     *
     * @param instance         instance
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @throws NovaException when logs have been are already requested
     */
    private void checkInstance(DeploymentInstance instance, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {

        List<ManagementActionTask> managementActionTaskList = new ArrayList<>();
        for (ToDoTaskType toDoTaskType : toDoTaskTypeList)
        {
            managementActionTaskList.addAll(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(
                    instance.getId(), toDoTaskType, List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)));
        }
        if (!managementActionTaskList.isEmpty())
        {
            log.debug("[PendingCheckService]->[checkInstance]: Instance with id {} has {} pending tasks", instance.getId(),
                    managementActionTaskList.size());
            throw new NovaException(ServiceRunnerError.getPendingActionTaskError(), "Instance has an action already requested (start, " +
                    "restart or stop)");
        }
    }

    /**
     * Check instance
     *
     * @param instance         instance
     * @param toDoTaskTypeList task type list: pause, restart or stop
     * @throws NovaException when logs have been are already requested
     */
    private void checkInstance(Integer instance, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {

        List<ManagementActionTask> managementActionTaskList = new ArrayList<>();
        for (ToDoTaskType toDoTaskType : toDoTaskTypeList)
        {
            managementActionTaskList.addAll(this.actionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(
                    instance, toDoTaskType, List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR)));
        }
        if (!managementActionTaskList.isEmpty())
        {
            log.debug("[PendingCheckService]->[checkInstance]: Instance with id {} has {} pending tasks", instance,
                    managementActionTaskList.size());
            throw new NovaException(ServiceRunnerError.getPendingActionTaskError(), "Instance has an action already requested (pause, " +
                    "restart or stop)");
        }
    }

    /**
     * Check plan children for start, restart or stop actions
     *
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @param plan             plan
     * @throws NovaException when logs have been are already requested
     */
    private void checkChildren(DeploymentPlan plan, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {
        List<ToDoTaskType> serviceTypes = new ArrayList<>();
        serviceTypes.add(ToDoTaskType.SERVICE_RESTART);
        serviceTypes.add(ToDoTaskType.SERVICE_STOP);
        serviceTypes.add(ToDoTaskType.SERVICE_START);
        for (DeploymentSubsystem subsystem : plan.getDeploymentSubsystems())
        {
            this.checkSubsystem(subsystem, toDoTaskTypeList);
            this.checkChildren(subsystem, serviceTypes);
        }
    }

    /**
     * Check subsystem for start, restart or stop actions
     *
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @param subsystem        subsystem
     * @throws NovaException when logs have been are already requested
     */
    private void checkChildren(DeploymentSubsystem subsystem, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {
        List<ToDoTaskType> instanceTypes = new ArrayList<>();
        instanceTypes.add(ToDoTaskType.CONTAINER_START);
        instanceTypes.add(ToDoTaskType.CONTAINER_RESTART);
        instanceTypes.add(ToDoTaskType.CONTAINER_STOP);
        for (DeploymentService service : subsystem.getDeploymentServices())
        {
            this.checkService(service, toDoTaskTypeList);
            this.checkChildren(service, instanceTypes);
        }
    }

    /**
     * Check service for start, restart or stop action
     *
     * @param service          service
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @throws NovaException when logs have been are already requested
     */
    private void checkChildren(DeploymentService service, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {
        for (DeploymentInstance instance : service.getInstances())
        {
            this.checkInstance(instance, toDoTaskTypeList);
        }
    }

    /**
     * Check parent plan for start, restart or stop action
     *
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @param plan             plan
     * @throws NovaException when logs have been are already requested
     */
    private void checkParent(DeploymentPlan plan, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {
        this.checkPlan(plan, toDoTaskTypeList);
    }

    /**
     * Check parent subsystem for start, restart or stop action
     *
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @param subsystem        subsystem
     * @throws NovaException when logs have been are already requested
     */
    private void checkParent(DeploymentSubsystem subsystem, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {
        this.checkSubsystem(subsystem, toDoTaskTypeList);
        List<ToDoTaskType> planTypes = new ArrayList<>();
        planTypes.add(ToDoTaskType.RELEASE_START);
        planTypes.add(ToDoTaskType.RELEASE_RESTART);
        planTypes.add(ToDoTaskType.RELEASE_STOP);
        this.checkParent(subsystem.getDeploymentPlan(), planTypes);
    }

    /**
     * Check parent service for start, restart or stop action
     *
     * @param toDoTaskTypeList task type list: start, restart or stop
     * @param service          service
     * @throws NovaException when logs have been are already requested
     */
    private void checkParent(DeploymentService service, List<ToDoTaskType> toDoTaskTypeList) throws NovaException
    {
        this.checkService(service, toDoTaskTypeList);
        List<ToDoTaskType> subsystemTypes = new ArrayList<>();
        subsystemTypes.add(ToDoTaskType.SUBSYSTEM_START);
        subsystemTypes.add(ToDoTaskType.SUBSYSTEM_RESTART);
        subsystemTypes.add(ToDoTaskType.SUBSYSTEM_STOP);
        this.checkParent(service.getDeploymentSubsystem(), subsystemTypes);
    }
}

