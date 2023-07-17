package com.bbva.enoa.platformservices.coreservice.etherapi.services.impl;

import com.bbva.enoa.apirestgen.etherapi.model.EtherDeploymentServiceInventoryDto;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.platformservices.coreservice.etherapi.services.interfaces.IDeploymentServiceInventoryDtoBuilder;
import org.springframework.stereotype.Service;

@Service
public class DeploymentServiceInventoryDtoBuilder implements IDeploymentServiceInventoryDtoBuilder
{
    @Override
    public EtherDeploymentServiceInventoryDto build(final DeploymentService deploymentService)
    {
        final DeploymentPlan deploymentPlan = deploymentService.getDeploymentSubsystem().getDeploymentPlan();

        final EtherDeploymentServiceInventoryDto deploymentServiceInventoryDto = new EtherDeploymentServiceInventoryDto();
        deploymentServiceInventoryDto.setProductId(deploymentPlan.getReleaseVersion().getRelease().getProduct().getId());
        deploymentServiceInventoryDto.setProductName(deploymentPlan.getReleaseVersion().getRelease().getProduct().getName());
        deploymentServiceInventoryDto.setDeployNamespace(deploymentPlan.getEtherNs());
        deploymentServiceInventoryDto.setReleaseId(deploymentPlan.getReleaseVersion().getRelease().getId());
        deploymentServiceInventoryDto.setReleaseName(deploymentPlan.getReleaseVersion().getRelease().getName());
        deploymentServiceInventoryDto.setReleaseVersionId(deploymentPlan.getReleaseVersion().getId());
        deploymentServiceInventoryDto.setReleaseVersionName(deploymentPlan.getReleaseVersion().getVersionName());
        deploymentServiceInventoryDto.setReleaseServiceId(deploymentService.getService().getId());
        deploymentServiceInventoryDto.setServiceId(deploymentService.getId());
        deploymentServiceInventoryDto.setServiceName(deploymentService.getService().getServiceName());
        deploymentServiceInventoryDto.setServiceVersion(deploymentService.getService().getVersion());
        deploymentServiceInventoryDto.setServiceType(deploymentService.getService().getServiceType());
        deploymentServiceInventoryDto.setDeploymentPlanId(deploymentPlan.getId());
        deploymentServiceInventoryDto.setEnvironment(deploymentPlan.getEnvironment());

        // If a DeploymentService deployed in Ether has defined instances, it will have only 1 instance
        final DeploymentInstance deploymentInstance = getDeploymentInstance(deploymentService);

        if (deploymentInstance != null)
        {
            deploymentServiceInventoryDto.setInstanceId(deploymentInstance.getId());
            deploymentServiceInventoryDto.setInstancesStarted(deploymentInstance.getStarted());
        }

        deploymentServiceInventoryDto.setInstancesConfigured(deploymentService.getNumberOfInstances());

        return deploymentServiceInventoryDto;
    }

    private DeploymentInstance getDeploymentInstance(final DeploymentService deploymentService)
    {
        // If a DeploymentService deployed in Ether has defined instances, it will have only 1 instance
        if (deploymentService.getInstances() != null && !deploymentService.getInstances().isEmpty())
        {
            return deploymentService.getInstances().get(0);
        }
        else
        {
            return null;
        }
    }
}
