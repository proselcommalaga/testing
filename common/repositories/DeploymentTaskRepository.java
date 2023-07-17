package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author xe30000
 */
@Transactional( readOnly = true )
public interface DeploymentTaskRepository extends JpaRepository<DeploymentTask, Integer >
{
    /**
     * Return the number of PENDING or PENDING_ERROR DeploymentTask
     *
     * @param deploymentId {@link DeploymentPlan} ID
     * @return the number of PENDING DeploymentTask
     */
    @Query(" select count (*) from DeploymentTask "
            + " where "
            + " (status = 'PENDING' or status = 'PENDING_ERROR')"
            + " and "
            + " deploymentPlan.id = :deploymentId"
    )
    long countPendingAll(@Param("deploymentId") final Integer deploymentId);


    /**
     * Return the number of PENDING or PENDING_ERROR {@link DeploymentTask}
     * of a given {@link DeploymentPlan}.
     *
     * @param deploymentId {@link DeploymentPlan} ID
     * @return the number of PENDING or PENDING_ERROR DeploymentTask
     */
    @Query(
        "select case when count ( dt.id ) > 0 then true else false end "
        + " from DeploymentTask dt where "
        + " ( dt.status = 'PENDING' or dt.status = 'PENDING_ERROR' ) "
        + " and dt.deploymentPlan.id = :deploymentId "
    )
    boolean planHasPendingDeploymentTasks(@Param("deploymentId") final Integer deploymentId);


    /**
     * Return all PENDING or PENDING_ERROR {@link DeploymentTask}
     * of a given {@link DeploymentPlan}.
     *
     * @param deploymentId {@link DeploymentPlan} ID
     * @return the number of PENDING or PENDING_ERROR DeploymentTask
     */
    @Query(
        "from DeploymentTask dt where "+
        " ( dt.status = 'PENDING' or dt.status = 'PENDING_ERROR' ) "+
        " and dt.deploymentPlan.id = :deploymentId "
    )
    List<DeploymentTask> planPendingDeploymentTasks(@Param("deploymentId") final Integer deploymentId);

    /**
     * Find by deployment plan id
     *
     * @param id id
     * @return list of deployment tasks for the given deployment plan id
     */
    List<DeploymentTask> findByDeploymentPlanId(Integer id);
}
