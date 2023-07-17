package com.bbva.enoa.platformservices.coreservice.common.builders.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.BasicAlertInfoDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.platformservices.coreservice.common.builders.IBasicAlertDTOBuilder;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Optional;

@Log4j2
public abstract class AbstractDefaultBasicAlertDTOBuilderImpl implements IBasicAlertDTOBuilder
{
    @Override
    public BasicAlertInfoDTO convert(final DeploymentPlan deploymentPlan, final ASBasicAlertInfoDTO asBasicAlertInfoDTO)
    {
        BasicAlertInfoDTO basicAlertInfoDTO = new BasicAlertInfoDTO();
        basicAlertInfoDTO.setAlertId(asBasicAlertInfoDTO.getAlertId());
        basicAlertInfoDTO.setAlertType(asBasicAlertInfoDTO.getAlertType());
        basicAlertInfoDTO.setAlertRelatedId(asBasicAlertInfoDTO.getAlertRelatedId());
        basicAlertInfoDTO.setStatus(asBasicAlertInfoDTO.getStatus());
        basicAlertInfoDTO.setAlertRelatedServiceId(this.findRelatedService(deploymentPlan, asBasicAlertInfoDTO));
        return basicAlertInfoDTO;
    }

    /**
     * This method is in charge of searching the service that is associated to the optional alert related id
     *
     * @param deploymentPlan the {@link DeploymentPlan} that we want to send
     * @param asBasicAlertInfoDTO the {@link ASBasicAlertInfoDTO} alert dto associated with the deployment plan
     * @return the service identifier found
     */
    protected Integer findRelatedService(final DeploymentPlan deploymentPlan, final ASBasicAlertInfoDTO asBasicAlertInfoDTO)
    {
        log.debug("[DefaultBasicAlertDTOBuilderImpl] - [findRelatedService]: Finding the service associated to the alert [{}]", asBasicAlertInfoDTO.getAlertId());

        // getting the alert related id
        var alertRelatedId = Optional.ofNullable(asBasicAlertInfoDTO.getAlertRelatedId()).map(Integer::parseInt).orElse(null);

        // finding the associated deployment instance
        final var optInstance = deploymentPlan.getDeploymentSubsystems()
                .stream()
                .map(DeploymentSubsystem::getDeploymentServices)
                .flatMap(Collection::stream)
                .map(DeploymentService::getInstances)
                .flatMap(Collection::stream)
                .filter(deploymentInstance -> deploymentInstance.getId().equals(alertRelatedId))
                .findAny();

        // mapping into its service
        final var optRelatedServiceId = optInstance.map(deploymentInstance -> deploymentInstance.getService().getId());

        // logging if the service was present or not
        optRelatedServiceId.ifPresentOrElse(serviceId -> log.debug("[DefaultBasicAlertDTOBuilderImpl] - [findRelatedService]: Found the service [{}] associated to the alert [{}]", serviceId, asBasicAlertInfoDTO.getAlertId()),
                () -> log.warn("[DefaultBasicAlertDTOBuilderImpl] - [findRelatedService]: There is no associated service to the alert [{}]", asBasicAlertInfoDTO.getAlertId()));


        // if not present we will send a null value
        return optRelatedServiceId.orElse(null);
    }

}
