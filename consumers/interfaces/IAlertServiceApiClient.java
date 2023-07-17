package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;


import com.bbva.enoa.apirestgen.alertserviceapi.model.ASAlertDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASRequestAlertsDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.alertservice.DeploymentScheduleDTO;

/**
 * The interface Alert service client.
 */
public interface IAlertServiceApiClient
{

    /**
     * Check the deploy plan alerts
     *
     * @param deployPlanId The id for the deploy plan to check
     * @param stateList    the state list
     * @return ASDeplyPlanAlertInfoDTO the DTO with the info for the deploy plan alerts
     */
    ASRequestAlertsDTO checkDeployPlanAlertInfo(Integer deployPlanId, String[] stateList);

    /**
     * Gets alerts by related id and status.
     *
     * @param relatedIds the related ids
     * @param productId  the product id
     * @param uuaa       the uuaa
     * @param stateList  the state list
     * @return the alerts by relatd id and status
     */
    ASRequestAlertsDTO getAlertsByRelatedIdAndStatus(String[] relatedIds, Integer productId, String uuaa, String stateList);

    /**
     * Get the list of Product Alerts since the specified number of days ago, and apply some other optional filters.
     *
     * @param daysAgo     Filter the Product Alerts this number of days ago.
     * @param environment Filter the Tasks only in this environment. If it's "ALL" or not present, don't apply this filter.
     * @param type        Filter the Tasks only of this type. If it's "ALL" or not present, don't apply this filter.
     * @param uuaa        Filter the Tasks only in this UUAA. If it's "ALL" or not present, don't apply this filter.
     * @return An array of ASBasicAlertInfoDTO.
     */
    ASBasicAlertInfoDTO[] getProductAlertsSinceDaysAgo(Integer daysAgo, String environment, String type, String uuaa);

    /**
     * This method is in charge of sending the alert creation petition to the AlertService
     *
     * @param deploymentScheduleDTO the {@link DeploymentScheduleDTO} with the associated error data
     */
    void registerProductAlert(final DeploymentScheduleDTO deploymentScheduleDTO);

    /**
     * Register a generic alert into alert service
     *
     * @param alertDTO alert info to be registered
     */
    void registerGenericAlert(final ASAlertDTO alertDTO);

    /**
     * Close every OPEN and OPENING alert related with the {@link DeploymentPlan} passed by parameter
     *
     * @param deploymentPlan the {@link DeploymentPlan} that we want to close its alerts
     */
    void closePlanRelatedAlerts(final DeploymentPlan deploymentPlan);

    /**
     * Close every OPEN and OPENING alert related to broker or broker nodes
     *
     * @param relatedId brokerId or nodeId-brokerId
     */
    void closeBrokerRelatedAlerts(final String relatedId);
}
