package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceAllowedJdkParameterValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface DeploymentServiceAllowedJdkParameterValueRepository extends JpaRepository<DeploymentServiceAllowedJdkParameterValue, Integer>
{
    /**
     * Returns a list of assigned JVM parameters for a given deployment service.
     *
     * @param deploymentServiceId The deployment service id.
     * @return A list of assigned JVM parameters for a given deployment service.
     */
    List<DeploymentServiceAllowedJdkParameterValue> findByDeploymentServiceId(Integer deploymentServiceId);

    /**
     * Deletes all assigned JVM parameters for a given deployment services list.
     *
     * @param deploymentServiceIds The deployment service ids.
     */
    @Modifying
    @Query("delete from com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceAllowedJdkParameterValue paramValue " +
            "where paramValue.id.deploymentServiceId in :deploymentServiceIds")
    void deleteByDeploymentServiceIds(Set<Integer> deploymentServiceIds);
}
