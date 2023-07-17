package com.bbva.enoa.platformservices.coreservice.common.builders.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.SpecificInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.BatchManagerClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Log4j2
@Component
public class BatchBasicAlertDTOBuilderImpl extends AbstractDefaultBasicAlertDTOBuilderImpl
{
    private final BatchManagerClient batchManagerClient;

    /**
     * This is a whitelist. This alert codes will be associated to this implementation
     */
    private final Set<String> supportedAlerts = Set.of("APP_BATCH_FAILURE", "APP_BATCH_INTERRUPTED");

    @Autowired
    public BatchBasicAlertDTOBuilderImpl(final BatchManagerClient batchManagerClient)
    {
        this.batchManagerClient = batchManagerClient;
    }

    @Override
    protected Integer findRelatedService(final DeploymentPlan deploymentPlan,  final ASBasicAlertInfoDTO asBasicAlertInfoDTO)
    {
        log.debug("[BatchBasicAlertDTOBuilderImpl] - [findRelatedService]: Finding the service associated to the alert [{}]", asBasicAlertInfoDTO.getAlertId());

        // getting the alert related id
        var alertRelatedId = Optional.ofNullable(asBasicAlertInfoDTO.getAlertRelatedId()).map(Integer::parseInt).orElseGet(() -> {
            log.debug("[BatchBasicAlertDTOBuilderImpl] - [findRelatedService]: The alert {} has any related id. Returning the identifier -1 for null safe behavior", asBasicAlertInfoDTO.getAlertId());
            return -1;
        });

        // requesting the instance associated to the related id
        final var optBatchInstanceServiceId = Optional.ofNullable(this.batchManagerClient.getInstanceById(alertRelatedId, deploymentPlan.getEnvironment()))
                .map(SpecificInstance::getServiceId)
                .map(Long::intValue);

        // logging if the service was present or not
        optBatchInstanceServiceId.ifPresentOrElse(serviceId -> log.debug("[BatchBasicAlertDTOBuilderImpl] - [findRelatedService]: Found the service [{}] associated to the alert [{}]", serviceId, asBasicAlertInfoDTO.getAlertId()),
                () -> log.warn("[BatchBasicAlertDTOBuilderImpl] - [findRelatedService]: There is no associated service to the alert [{}]", asBasicAlertInfoDTO.getAlertId()));

        return optBatchInstanceServiceId.orElse(null);
    }

    @Override
    public Boolean isSupported(final String alertType)
    {
        return this.supportedAlerts.contains(alertType);
    }
}
