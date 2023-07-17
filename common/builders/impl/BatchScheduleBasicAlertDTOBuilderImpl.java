package com.bbva.enoa.platformservices.coreservice.common.builders.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleInstanceDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Log4j2
@Component
public class BatchScheduleBasicAlertDTOBuilderImpl extends AbstractDefaultBasicAlertDTOBuilderImpl
{
    private final ISchedulerManagerClient schedulerManagerClient;

    /**
     * This is a whitelist. These alert codes will be associated with this implementation
     */
    private final Set<String> supportedAlerts = Set.of("APP_SCHEDULE_FAILURE");

    @Autowired
    public BatchScheduleBasicAlertDTOBuilderImpl(final ISchedulerManagerClient schedulerManagerClient)
    {
        this.schedulerManagerClient = schedulerManagerClient;
    }

    @Override
    protected Integer findRelatedService(final DeploymentPlan deploymentPlan, final ASBasicAlertInfoDTO asBasicAlertInfoDTO)
    {
        log.debug("[BatchScheduleBasicAlertDTOBuilderImpl] - [findRelatedService]: Finding the service associated to the alert [{}]", asBasicAlertInfoDTO.getAlertId());

        // getting the alert related id
        var alertRelatedId = Optional.ofNullable(asBasicAlertInfoDTO.getAlertRelatedId()).map(Integer::parseInt).orElseGet(() -> {
            log.debug("[BatchScheduleBasicAlertDTOBuilderImpl] - [findRelatedService]: The alert {} has any related id. Returning the identifier -1 for null safe behavior", asBasicAlertInfoDTO.getAlertId());
            return -1;
        });

        // calling to the scheduler manager service for the schedule instance associated to the alert
        final var optReleaseVersionServiceId = Optional.ofNullable(schedulerManagerClient.getDeploymentBatchScheduleInstanceById(alertRelatedId))
                .map(DeploymentBatchScheduleInstanceDTO::getReleaseVersionServiceId);

        // getting the deployment service associated to the release version service
        final var optDeploymentServiceId = optReleaseVersionServiceId
                        .flatMap(releaseVersionServiceId -> deploymentPlan.getDeploymentSubsystems().stream()
                        .map(DeploymentSubsystem::getDeploymentServices)
                        .flatMap(Collection::stream)
                        .filter(deploymentService -> deploymentService.getService().getId().equals(releaseVersionServiceId))
                        .map(DeploymentService::getId)
                        .findAny());

        // logging if the service was present or not
        optDeploymentServiceId.ifPresentOrElse(serviceId -> log.debug("[BatchScheduleBasicAlertDTOBuilderImpl] - [findRelatedService]: Found the service [{}] associated to the alert [{}]", serviceId, asBasicAlertInfoDTO.getAlertId()),
                () -> log.warn("[BatchScheduleBasicAlertDTOBuilderImpl] - [findRelatedService]: There is no associated service to the alert [{}]", asBasicAlertInfoDTO.getAlertId()));


        return optDeploymentServiceId.orElse(null);
    }

    @Override
    public Boolean isSupported(final String alertType)
    {
        return this.supportedAlerts.contains(alertType);
    }
}
