package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentChange;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @author xe30000
 */
public interface DeploymentChangeRepository extends JpaRepository<DeploymentChange, Integer>
{
    /**
     * Gets history of the plans
     *
     * @param pagination Pagination information
     * @param deploymentId the deployment id
     * @return Page of obtained data.
     */
    @Query("SELECT deploymentChange "
    	 + "FROM DeploymentPlan deploymentPlan JOIN deploymentPlan.changes deploymentChange "
    	 + "WHERE deploymentPlan.id = :deploymentId "
    	 + "ORDER BY deploymentChange.id DESC")
    Page<DeploymentChange> getHistory(@Param("deploymentId") final Integer deploymentId, Pageable pagination) ;

    @Query("SELECT deploymentChange "
            + "FROM DeploymentPlan deploymentPlan JOIN deploymentPlan.changes deploymentChange "
            + "WHERE deploymentPlan.status = :deploymentPlanStatus ")
    List<DeploymentChange> getDeploymentChangeByDeploymentPlanStatus(@Param("deploymentPlanStatus") final DeploymentStatus deploymentPlanStatus);
}
