package com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.EtherDeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.NovaDeploymentInstance;
import com.bbva.enoa.platformservices.coreservice.common.util.PlatformUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.autoconfig.NovaMonitoringProperties;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.autoconfig.NovaToolsProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Utilities for using the NOVA monitoring service.
 */
@Service
public class MonitoringUtils
{
    @Value("${nova.mappings.baseUrl.pro}")
    private String proBaseUrlNova;

    @Autowired
    private NovaToolsProperties novaToolsProperties;

    @Autowired
    private NovaMonitoringProperties novaMonitoringProperties;

    /**
     * Get the URL for accessing the NOVA monitoring service
     * to get the metrics of all host dedicated to products
     * on a given {@link DeploymentPlan} limited to today.
     *
     * @param deploymentPlan {@link DeploymentPlan}
     * @return The URL
     */
    public String getMonitoringUrlForProducts(final DeploymentPlan deploymentPlan)
    {
        final String monitoringUrl;

        final Platform selectedDestinationPlatform = Optional.ofNullable(deploymentPlan.getSelectedDeploy()).orElse(Platform.NOVA);
        final String selectedEnvironment = Optional.ofNullable(deploymentPlan.getEnvironment()).orElse(Environment.INT.getEnvironment());

        if (Platform.ETHER.equals(selectedDestinationPlatform))
        {
            monitoringUrl = PlatformUtils.buildAteneaMetricURL(novaToolsProperties.getAtenea(), Environment.valueOf(selectedEnvironment), deploymentPlan.getEtherNs());
        }
        else
        {
            String monitoringProductUrl = novaMonitoringProperties.getProducts().get(Environment.valueOf(selectedEnvironment));

            monitoringProductUrl = monitoringProductUrl.replace("{environment}", selectedEnvironment.toUpperCase());

            final String uuaa = deploymentPlan.getReleaseVersion().getRelease().getProduct().getUuaa().toUpperCase();

            monitoringProductUrl = monitoringProductUrl.replace("{uuaa}", uuaa.toLowerCase());

            monitoringUrl = this.proBaseUrlNova + monitoringProductUrl;
        }

        return monitoringUrl;
    }

    /**
     * Get the URL for accessing the NOVA monitoring service
     * to get the metrics of a given {@link NovaDeploymentInstance} limited to today.
     *
     * @param deploymentService  {@link DeploymentService}
     * @param deploymentInstance {@link NovaDeploymentInstance}
     * @return The URL
     */
    public String getMonitoringUrlForInstance(final DeploymentService deploymentService, final NovaDeploymentInstance deploymentInstance)
    {
        final String selectedEnvironment = Optional.ofNullable(deploymentService.getDeploymentSubsystem().getDeploymentPlan().getEnvironment()).orElse(Environment.INT.getEnvironment());

        String monitoringContainerUrl = novaMonitoringProperties.getContainers().get(Environment.valueOf(selectedEnvironment));

        monitoringContainerUrl = monitoringContainerUrl.replace("{environment}", selectedEnvironment.toUpperCase());
        monitoringContainerUrl = monitoringContainerUrl.replace("{uuaa}", deploymentService.getDeploymentSubsystem().getDeploymentPlan().getReleaseVersion().getRelease().getProduct().getUuaa().toLowerCase());
        monitoringContainerUrl = monitoringContainerUrl.replace("{container}", deploymentInstance.getContainerName());

        return proBaseUrlNova + monitoringContainerUrl;
    }

    /**
     * Get the URL for accessing the NOVA monitoring service
     * to get the metrics of a given {@link EtherDeploymentInstance} limited to today.
     *
     * @param deploymentService {@link DeploymentService}
     * @return The URL
     */
    public String getMonitoringUrlForInstance(final DeploymentService deploymentService)
    {
        // get deployment plan
        final DeploymentPlan deploymentPlan = deploymentService.getDeploymentSubsystem().getDeploymentPlan();

        return PlatformUtils.buildAteneaMetricURL(novaToolsProperties.getAtenea(), Environment.valueOf(deploymentPlan.getEnvironment()), deploymentPlan.getEtherNs());
    }

    /**
     * Get the URL for accessing the NOVA monitoring service
     * to get the metrics of a given {@link BrokerNode}.
     *
     * @param brokerNode {@link BrokerNode}
     * @return The URL or null if no broker nodes
     */
    public String getMonitoringUrlForBrokerNode(final BrokerNode brokerNode)
    {
        final Environment selectedEnvironment = Environment.valueOf(brokerNode.getBroker().getEnvironment());

        String monitoringContainerUrl = novaMonitoringProperties.getContainers().get(selectedEnvironment);
        String uuaa = brokerNode.getBroker().getProduct().getUuaa().toLowerCase();

        monitoringContainerUrl = monitoringContainerUrl.replace("{uuaa}", uuaa)
                .replace("{container}", brokerNode.getContainerName());

        return proBaseUrlNova + monitoringContainerUrl;
    }

    /**
     * Build a monitoring URL for an instance.
     *
     * @param containerName Container name.
     * @param uuaa          Product UUAA.
     * @return Monitoring URL (Grafana).
     */
    public String getMonitoringUrlForInstance(String containerName, String uuaa)
    {
        String monitoringContainerUrl = novaMonitoringProperties.getContainers().get(Environment.PRE);

        monitoringContainerUrl = monitoringContainerUrl.replace("{uuaa}", uuaa).replace("{container}", containerName);

        return proBaseUrlNova + monitoringContainerUrl;
    }
}
