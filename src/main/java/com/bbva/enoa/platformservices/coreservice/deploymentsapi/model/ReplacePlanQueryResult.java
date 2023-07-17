package com.bbva.enoa.platformservices.coreservice.deploymentsapi.model;

import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceFilesystem;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for mappings release version service and deployment service when replace a plan
 */
public class ReplacePlanQueryResult
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
    private List<DeploymentServiceFilesystem> filesystems;

    /**
     * List of logical connector
     */
    @Getter
    @Setter
    private List<LogicalConnector> logicalConnectorList;

    /**
     * Configuration value
     */
    @Getter
    @Setter
    private String configurationValue;

    /**
     * Property definition
     */
    @Getter
    @Setter
    private String propertyDefinitionName;

    /**
     * Number of Instances
     */
    @Getter
    @Setter
    private Integer numberOfInstance;

    /**
     * Memory percentage
     */
    @Getter
    @Setter
    private Integer memoryFactor;

    /**
     * Constructor
     *
     * @param deploymentService      deployment service
     * @param serviceName            service name
     * @param serviceVersion         service version
     * @param hardwarePack           hardware pack
     * @param filesystems            filesystems
     * @param propertyDefinitionName property name
     * @param configurationValue     property value
     * @param numberOfInstance       number of instance
     * @param memoryFactor           memory factor
     */
    public ReplacePlanQueryResult(final DeploymentService deploymentService, final String serviceName, final String serviceVersion, final HardwarePack hardwarePack, final List<DeploymentServiceFilesystem> filesystems,
                                  final String propertyDefinitionName, final String configurationValue, final Integer numberOfInstance, final Integer memoryFactor)
    {
        this.deploymentService = deploymentService;
        this.serviceName = serviceName;
        this.serviceVersion = serviceVersion;
        this.hardwarePack = hardwarePack;
        this.filesystems = filesystems;
        this.logicalConnectorList = new ArrayList<>(deploymentService.getLogicalConnectors());
        this.configurationValue = configurationValue;
        this.propertyDefinitionName = propertyDefinitionName;
        this.numberOfInstance = numberOfInstance;
        this.memoryFactor = memoryFactor;
    }
}
