package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.TaskRequestDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import com.bbva.enoa.datamodel.model.todotask.entities.ToDoTask;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;

public interface ITaskProcessor
{
    /**
     * Crea una TodoTask de tipo Deployment definida por el objeto
     * taskRequestDto.
     *
     * @param taskRequestDtoArray - definición de la tarea a crear.
     * @param ivUser              - Usuario que provoca la solicitud de la tarea.
     * @param deploymentPlan      - Plan de despliegue al que va asociada la tarea.
     */
    void createConfigManagementTask(TaskRequestDTO[] taskRequestDtoArray, String ivUser, DeploymentPlan deploymentPlan);

    /**
     * Processes the response of a {@link DeploymentTask} when is closed.
     * Logic will be different depending on the task type.
     *
     * @param deploymentPlan - deployment plan
     * @param todoTaskId     - the to do task id
     * @param todoTaskStatus - the new status of the to do task. Must be one of {@code ToDoTaskStatus}
     */
    void onTaskReply(DeploymentPlan deploymentPlan, Integer todoTaskId, String todoTaskStatus);

    /**
     * Add history entry
     *
     * @param type           change type
     * @param deploymentPlan the deployment plan
     * @param userCode       user that generates the change
     * @param message        message to save the change
     */
    void addHistoryEntry(ChangeType type, DeploymentPlan deploymentPlan, String userCode, String message);

    /**
     * Processes the response of a {@link ToDoTask} when is closed.
     * Logic will be different depending on the task type.
     *
     * @param todoTaskId     - the to do task id
     * @param todoTaskStatus - the new status of the to do task. Must be one of {@code ToDoTaskStatus}
     * @throws Errors error
     */
    void onTaskReply(Integer todoTaskId, String todoTaskStatus) throws Errors;
}
