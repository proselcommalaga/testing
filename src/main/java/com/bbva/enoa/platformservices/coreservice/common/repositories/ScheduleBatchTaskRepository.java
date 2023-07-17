package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.todotask.entities.ScheduleBatchTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * ScheduleBatchTask repository.
 */
@Transactional(readOnly = true)
public interface ScheduleBatchTaskRepository extends JpaRepository<ScheduleBatchTask, Integer>
{
    /**
     * Find last schedule task
     *
     * @param batch deployment service
     * @return last schedule batch task
     */
    @Query("select task from ScheduleBatchTask task where task.creationDate=(select max(t2.creationDate) from ScheduleBatchTask t2 where t2.batch=:batch group by t2.batch) and task.batch= :batch")
    ScheduleBatchTask findLastScheduleTask(@Param("batch") DeploymentService batch);
}
