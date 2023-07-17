package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author xe30000
 */
@Transactional(readOnly = true)
public interface ManagementActionTaskRepository extends JpaRepository<ManagementActionTask, Integer>
{

    /**
     * Find by related id, task type and toDoTask Status List
     *
     * @param relatedId          related id
     * @param taskType           task type
     * @param toDoTaskStatusList to Do Task Status List
     * @return list of management action tasks
     */
    List<ManagementActionTask> findByRelatedIdAndTaskTypeAndStatusIn(@Param("relatedId") final Integer relatedId,
                                                                     @Param("taskType") final ToDoTaskType taskType,
                                                                     @Param("toDoTaskStatusList") final List<ToDoTaskStatus> toDoTaskStatusList);

    /**
     * Find by related id
     *
     * @param id id
     * @return list of deployment tasks for the given deployment plan id
     */
    List<ManagementActionTask> findByRelatedId(Integer id);

    /**
     * Find by related id and to do task status list
     *
     * @param relatedId          the related id
     * @param toDoTaskStatusList to Do Task Status List
     * @return list of management tasks for the given related id and task status list
     */
    List<ManagementActionTask> findByRelatedIdAndStatusIn(@Param("relatedId") final Integer relatedId, @Param("toDoTaskStatusList") final List<ToDoTaskStatus> toDoTaskStatusList);
}
