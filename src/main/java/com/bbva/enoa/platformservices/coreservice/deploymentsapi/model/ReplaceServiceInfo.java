package com.bbva.enoa.platformservices.coreservice.deploymentsapi.model;

import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceFilesystem;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class to obtain all necessary data from service when replace a plan
 */
@AllArgsConstructor
@NoArgsConstructor
public class ReplaceServiceInfo
{
    /**
     * Deployment service id
     */
    @Getter
    private DeploymentService deploymentService;

    /**
     * Column final_name of Release Version Service
     */
    @Getter
    @Setter
    private String serviceName;

    /**
     * Column version of Release Version Service
     */
    @Getter
    @Setter
    private String serviceVersion;

    /**
     * Column Hardware pack
     */
    @Getter
    private HardwarePack hardwarePack;

    /**
     * Column filesystems
     */
    @Getter
    @Setter
    private List<DeploymentServiceFilesystem> filesystems = new ArrayList<>();

    /**
     * Number of Instances
     */
    @Getter
    @Setter
    private Integer numberOfInstance;

    /**
     * Memory porcentage
     */
    @Getter
    @Setter
    private Integer memoryFactor;

    /**
     * List of logical connector
     */
    @Getter
    @Setter
    private List<LogicalConnector> logicalConnectorList;


    /**
     * Map with all service properteis
     */
    @Getter
    @Setter
    private Map<String, String> servicePropertyMap;

    /**
     * Set deployment service
     *
     * @param deploymentService deployment service
     */
    public void setDeploymentService(DeploymentService deploymentService)
    {
        if (deploymentService == null)
        {
            this.deploymentService = new DeploymentService();
        }
        else
        {
            this.deploymentService = deploymentService;
        }
    }

    /**
     * Set hardware pack
     *
     * @param hardwarePack hardware pack
     */
    public void setHardwarePack(HardwarePack hardwarePack)
    {
        if (hardwarePack == null)
        {
            this.hardwarePack = new HardwarePack();
        }
        else
        {
            this.hardwarePack = hardwarePack;
        }
    }

}
