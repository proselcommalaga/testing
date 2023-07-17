package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.todotask.entities.ConfigurationTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author xe30000
 */
public interface ConfigurationTaskRepository extends JpaRepository<ConfigurationTask, Integer>
{

    // Buscar todotasks pendientes de un plan de despliegue (de producción)

    List<ConfigurationTask> findByDeploymentService(DeploymentService deploymentService);

    /**
     * Checks if the given {@link DeploymentService} has a pending {@link ConfigurationTask}
     * associated.
     *
     * @param deploymentServiceId {@link DeploymentService} ID.
     * @return True if there is a pending request of configuration change, false otherwise.
     */
    @Query(
            "select case when count ( e.id ) > 0 then true else false end " +
                    "from ConfigurationTask e " +
                    "where " +
                    "  e.deploymentService.id = :deploymentServiceId " +
                    "  and ( e.status = com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus.PENDING" +
                    "  or    e.status = com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus.PENDING_ERROR )"
    )
    boolean serviceHasPendingConfigurationTask(@Param("deploymentServiceId") int deploymentServiceId);


    /**
     * Get the pending {@link ConfigurationTask} of a {@link DeploymentService} if any.
     *
     * @param deploymentServiceId {@link DeploymentService} plan.
     * @return ConfigurationTask
     */
    @Query(
            "from ConfigurationTask e " +
                    "where " +
                    "    e.deploymentService.id = :deploymentServiceId " +
                    "and ( e.status = com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus.PENDING " +
                    "  or  e.status = com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus.PENDING_ERROR )")
    List<ConfigurationTask> getPendingConfigurationTask(@Param("deploymentServiceId") int deploymentServiceId);


}

