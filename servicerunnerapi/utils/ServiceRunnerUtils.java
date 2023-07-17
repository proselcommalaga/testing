package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils;

import com.bbva.enoa.apirestgen.servicerunnerapi.model.TodoTaskResponseDTO;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.servicerunnerapi.exceptions.ServiceRunnerError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * The type Service runner utils.
 */
@Service
@Slf4j
public class ServiceRunnerUtils
{


    /**
     * Builder todotask response dto to do task response dto.
     *
     * @param generated    the generated
     * @param assignedRole the assigned role
     * @param taskId       the task id
     * @param toDoTaskType the to do task type
     * @return the todotask response dto
     */
    public TodoTaskResponseDTO builderTodoTaskResponseDTO(boolean generated, RoleType assignedRole, Integer taskId, ToDoTaskType toDoTaskType){

     TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();

     todoTaskResponseDTO.setGenerated(generated);
     if (generated)
     {
         if (taskId == null)
         {
             log.error("[{}] -> [buildTodoTaskResponseDTO]  : Error: Task id null when building todotaskResponseDto", this.getClass().getSimpleName());
             throw new NovaException(ServiceRunnerError.getTaskIdIsNullError());
         }

         todoTaskResponseDTO.setTodoTaskId(taskId);

         if (assignedRole!=null)
         {
             todoTaskResponseDTO.setAssignedRole(assignedRole.name());
         }
         if (toDoTaskType!=null)
         {
             todoTaskResponseDTO.setTodoTaskType(toDoTaskType.name());
         }
     }
    return  todoTaskResponseDTO;
 }

    /**
     * Builder todotask response dto todotask response dto without values.
     *
     * @param generated    the generated
     * @return the todotask response dto
     */
    public TodoTaskResponseDTO builderTodoTaskResponseDTO(boolean generated){

        TodoTaskResponseDTO todoTaskResponseDTO = new TodoTaskResponseDTO();

        todoTaskResponseDTO.setGenerated(generated);

        return  todoTaskResponseDTO;
    }

}
