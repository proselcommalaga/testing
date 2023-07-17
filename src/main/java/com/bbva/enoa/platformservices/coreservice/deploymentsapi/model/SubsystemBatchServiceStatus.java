package com.bbva.enoa.platformservices.coreservice.deploymentsapi.model;

import com.bbva.enoa.apirestgen.batchmanagerapi.model.DeploymentServiceBatchStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Class for store all information about all deployment service of batch status of the subsystem
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SubsystemBatchServiceStatus
{
    /**
     * Deployment Subsystem id
     **/
    private Integer deploymentSubsystemId;

    /**
     * Deployment service batch status list
     **/
    private DeploymentServiceBatchStatus[] deploymentServiceBatchStatusList;
}
