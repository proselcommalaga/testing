package com.bbva.enoa.platformservices.coreservice.common.repositories;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.release.entities.JmxParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


/**
 * JmxParameter repository
 */
public interface JmxParameterRepository extends JpaRepository<JmxParameter, Integer>
{

    Optional<JmxParameter> findByDeploymentInstance(DeploymentInstance deploymentInstance);

    @Query("select jp from JmxParameter jp inner join jp.deploymentInstance di on di.id = jp.deploymentInstance.id inner join di.service ds on ds.id = di.service.id inner join " +
            "ds.deploymentSubsystem ds2 on ds2.id = ds.deploymentSubsystem.id inner join ds2.deploymentPlan dp on dp.id = ds2.deploymentPlan.id where ds2.deploymentPlan.id =:deploymentPlanId")
    List<JmxParameter> findAllByDeploymentPlanId(@Param("deploymentPlanId") Integer deploymentPlanId);
}
