package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentServiceDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.JdkTypedParametersDto;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;

public interface IDeploymentServiceJdkParametersSetter
{
    /**
     * Set or updated the jvm option parameters for deployment service
     *
     * @param deploymentService    the deployment service affected
     * @param typedParameters      jvm options parameters
     * @param deploymentServiceDto the deployment service dto
     */
    void setJvmOptionsForDeploymentService(DeploymentService deploymentService, DeploymentServiceDto deploymentServiceDto, JdkTypedParametersDto typedParameters);
}
