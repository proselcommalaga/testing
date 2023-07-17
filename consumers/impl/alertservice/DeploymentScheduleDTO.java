package com.bbva.enoa.platformservices.coreservice.consumers.impl.alertservice;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeploymentScheduleDTO
{
    /**
     * This field is equal to the deployment plan id
     */
    private Integer identifier ;

    /** When the deployment attempt was done */
    private String deploymentDate ;

    /** Where the deployment plan would had been deployed */
    private String deploymentPlatform ;

    /** The failure deployment attempt associated message */
    private String error;

    /**
     * The {@link Environment} associated to the plan
     */
    private Environment environment;
}
