package com.bbva.enoa.platformservices.coreservice.etherapi.services.interfaces;

import com.bbva.enoa.apirestgen.etherapi.model.EtherDeploymentServiceInventoryDto;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;

public interface IDeploymentServiceInventoryDtoBuilder
{
    EtherDeploymentServiceInventoryDto build(DeploymentService deploymentService);
}
