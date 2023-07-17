package com.bbva.enoa.platformservices.coreservice.common.builders;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.BasicAlertInfoDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;

/**
 * This interface will be on charge of converting the {@link ASBasicAlertInfoDTO} received from the platform-health-monitor into a {@link BasicAlertInfoDTO}.
 *
 * @implNote We need this interface because we don't receive the alert associated service from the platform-health-monitor, so we need to callback to the batch-manager, or the scheduler-manager, or... to find it.
 * @author XE93759
 */
public interface IBasicAlertDTOBuilder
{
    /**
     * This method is on charge of choosing which implementation is associated with the type passed by parameter.
     *
     * @param alertType type associated to the alert
     * @return true on the supported implementation, and vice versa.
     */
    Boolean isSupported(final String alertType);

    /**
     * This method is on charge of converting the DTO received from the platform-health-monitor into the core-service DTO.
     *
     * @param deploymentPlan {@link DeploymentPlan} associated to the alert
     * @param asBasicAlertInfoDTO {@link ASBasicAlertInfoDTO}  received from the platform-health-monitor
     * @return the new {@link BasicAlertInfoDTO} filled with the received info
     */
    BasicAlertInfoDTO convert(final DeploymentPlan deploymentPlan, final ASBasicAlertInfoDTO asBasicAlertInfoDTO);
}
