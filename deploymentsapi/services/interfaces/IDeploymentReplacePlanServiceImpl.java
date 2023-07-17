package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;

import java.util.List;
import java.util.Map;

public interface IDeploymentReplacePlanServiceImpl
{
    /**
     *
     * @param planId deployment plan id
     * @param newPlanId new deployment plan id
     * @return a map
     */
    Map<String, List<DeploymentService>> getUncommonReplacePlan(Integer planId, Integer newPlanId);
}
