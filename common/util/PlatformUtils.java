package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.util.EtherServiceNamingUtils;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.bbva.enoa.platformservices.coreservice.common.Constants.ATENEA_LOG_ENDPOINT;
import static com.bbva.enoa.platformservices.coreservice.common.Constants.ATENEA_METRICS_ENDPOINT;

public final class PlatformUtils
{
    /**
     * Private constructor
     */
    private PlatformUtils()
    {
        // nothing to do
    }

    public static List<Platform> getPlatformsValidToDeploy()
    {
        return Arrays.stream(Platform.values()).filter(Platform::getIsValidToDeploy).collect(Collectors.toList());
    }

    public static List<Platform> getPlatformsValidToLogging()
    {
        return Arrays.stream(Platform.values()).filter(Platform::getIsValidToLogging).collect(Collectors.toList());
    }



    public static Platform getSelectedDeployForReleaseInEnvironment(final Release release, final Environment environment)
    {
        final Platform selectedDeploy;

        switch (environment)
        {
            case LAB_PRE:
            case STAGING_PRE:
            case PRECON:
            case PRE:
                selectedDeploy = release.getSelectedDeployPre();
                break;
            case LAB_PRO:
            case STAGING_PRO:
            case PORTAL:
            case PRO:
                selectedDeploy = release.getSelectedDeployPro();
                break;
            case LOCAL:
            case LAB_INT:
            case STAGING_INT:
            case INT:
            default:
                selectedDeploy = release.getSelectedDeployInt();
                break;
        }

        return selectedDeploy;
    }

    public static Platform getSelectedLoggingForReleaseInEnvironment(final Release release, final Environment environment)
    {
        final Platform selectedLogging;

        switch (environment)
        {
            case LAB_PRE:
            case STAGING_PRE:
            case PRECON:
            case PRE:
                selectedLogging = release.getSelectedLoggingPre();
                break;
            case LAB_PRO:
            case STAGING_PRO:
            case PORTAL:
            case PRO:
                selectedLogging = release.getSelectedLoggingPro();
                break;
            case LOCAL:
            case LAB_INT:
            case STAGING_INT:
            case INT:
            default:
                selectedLogging = release.getSelectedLoggingInt();
                break;
        }

        return selectedLogging;
    }

    public static String getSelectedDeployNSForProductInEnvironment(final Product product, final Environment environment)
    {
        final String selectedNSDeploy;

        switch (environment)
        {
            case LAB_PRE:
            case STAGING_PRE:
            case PRECON:
            case PRE:
                selectedNSDeploy = product.getEtherNsPre();
                break;
            case LAB_PRO:
            case STAGING_PRO:
            case PORTAL:
            case PRO:
                selectedNSDeploy = product.getEtherNsPro();
                break;
            case LOCAL:
            case LAB_INT:
            case STAGING_INT:
            case INT:
            default:
                selectedNSDeploy = product.getEtherNsInt();
                break;
        }

        return selectedNSDeploy;
    }

    public static boolean isInstanceDeployedInNova(final DeploymentInstance instance)
    {
        return Platform.NOVA == instance.getService().getDeploymentSubsystem().getDeploymentPlan().getSelectedDeploy();
    }

    public static boolean isServiceDeployedInEther(final DeploymentService service)
    {
        return isSubsystemDeployedInEther(service.getDeploymentSubsystem());
    }

    public static boolean isSubsystemDeployedInEther(final DeploymentSubsystem subsystem)
    {
        return isPlanDeployedInEther(subsystem.getDeploymentPlan());
    }

    public static boolean isPlanDeployedInEther(final DeploymentPlan plan)
    {
        return Platform.ETHER == plan.getSelectedDeploy();
    }

    public static boolean isPlanLoggingInEther(final DeploymentPlan plan)
    {
        return Platform.ETHER == plan.getSelectedLogging() ||
                Platform.NOVAETHER == plan.getSelectedLogging();
    }

    private static String getAteneaURL(final Map<String, String> ateneaBaseUrlByEnvironment, final Environment environment)
    {
        final String ateneaBaseUrl;

        switch (environment)
        {
            case LAB_PRE:
            case STAGING_PRE:
            case PRECON:
            case PRE:
                ateneaBaseUrl = ateneaBaseUrlByEnvironment.get(Environment.PRE.getEnvironment());
                break;
            case LAB_PRO:
            case STAGING_PRO:
            case PORTAL:
            case PRO:
                ateneaBaseUrl = ateneaBaseUrlByEnvironment.get(Environment.PRO.getEnvironment());
                break;
            case LAB_INT:
            case STAGING_INT:
            case INT:
            case LOCAL:
            default:
                ateneaBaseUrl = ateneaBaseUrlByEnvironment.get(Environment.INT.getEnvironment());
                break;
        }

        return ateneaBaseUrl;
    }

    /**
     * Method to build the Atenea Metric URL for deployment plans
     *
     * @param ateneaBaseUrlByEnvironment the Atenea Map with the base URLs
     * @param environment                the environment
     * @param namespace                  the namespace
     * @return the URL of the Atenea
     */
    public static String buildAteneaMetricURL(final Map<String, String> ateneaBaseUrlByEnvironment, final Environment environment, final String namespace)
    {
        // get the Atenea URL
        final String ateneaURL = getAteneaURL(ateneaBaseUrlByEnvironment, environment);

        // build the URL (avoiding String.format because WORK url characters)
        return MessageFormat.format(ateneaURL, ATENEA_METRICS_ENDPOINT, namespace, "");
    }

    /**
     * Method to build the Atenea Log URL for deployment plans
     *
     * @param ateneaBaseUrlByEnvironment the Atenea Map with the base URLs
     * @param environment                the environment
     * @param namespace                  the namespace
     * @param releaseName                the release name
     * @return the URL of the Atenea
     */
    public static String buildAteneaLogURL(final Map<String, String> ateneaBaseUrlByEnvironment, final Environment environment, final String namespace, final String releaseName)
    {
        // get the Atenea URL
        final String ateneaURL = getAteneaURL(ateneaBaseUrlByEnvironment, environment);

        // get MonitoredResourceId
        final String monitoredResourceId = getMRMonitoredResourceId(releaseName);

        // build the URL (avoiding String.format because WORK url characters)
        return MessageFormat.format(ateneaURL, ATENEA_LOG_ENDPOINT, namespace, ("&TFmrId=" + monitoredResourceId));
    }

    /**
     * Method to build the Atenea Log URL for deployment service
     *
     * @param ateneaBaseUrlByEnvironment the Atenea Map with the base URLs
     * @param environment                the environment
     * @param namespace                  the namespace
     * @param releaseName                the release name
     * @param serviceName                the service name
     * @return the URL of the Atenea
     */
    public static String buildAteneaLogURL(final Map<String, String> ateneaBaseUrlByEnvironment, final Environment environment, final String namespace, final String releaseName, final String serviceName)
    {
        // get the Atenea URL
        final String ateneaURL = getAteneaURL(ateneaBaseUrlByEnvironment, environment);

        // get MonitoredResourceId
        final String monitoredResourceId = EtherServiceNamingUtils.getMRMonitoredResourceId(releaseName, serviceName);

        // build the URL (avoiding String.format because WORK url characters)
        return MessageFormat.format(ateneaURL, ATENEA_LOG_ENDPOINT, namespace, ("&TFmrId=" + monitoredResourceId));
    }

    /**
     * Method to build the Atenea Log URL for deployment subsystems
     *
     * @param ateneaBaseUrlByEnvironment the Atenea Map with the base URLs
     * @param environment                the environment
     * @param namespace                  the namespace
     * @param releaseName                the release name
     * @param serviceNames               the subsystem name
     * @return the URL of the Atenea
     */
    public static String buildAteneaLogURL(final Map<String, String> ateneaBaseUrlByEnvironment, final Environment environment, final String namespace, final String releaseName, final String[] serviceNames)
    {
        final String ateneaURL;
        if (serviceNames == null || serviceNames.length == 0)
        {
            // build the URL of the deployment plan
            ateneaURL = buildAteneaLogURL(ateneaBaseUrlByEnvironment, environment, namespace, releaseName);
        }
        else if (serviceNames.length == 1)
        {
            // build the URL of the service
            ateneaURL = buildAteneaLogURL(ateneaBaseUrlByEnvironment, environment, namespace, releaseName, serviceNames[0]);
        }
        else
        {
            // get the Atenea URL
            final String baseAteneaURL = getAteneaURL(ateneaBaseUrlByEnvironment, environment);

            // get MonitoredResourceId
            final String monitoredResourceId = buildMonitoredResourceIdQuery(releaseName, serviceNames);

            // build the URL (avoiding String.format because WORK url characters)
            ateneaURL = MessageFormat.format(baseAteneaURL, ATENEA_LOG_ENDPOINT, namespace, monitoredResourceId);
        }

        return ateneaURL;
    }

    private static String getMRMonitoredResourceId(final String releaseName)
    {
        return cleanString(releaseName) + "_*";
    }

    private static String buildMonitoredResourceIdQuery(final String releaseName, final String[] serviceNames)
    {
        final StringBuilder monitoredResourceQuery = new StringBuilder();

        for (int i = 0; i < serviceNames.length; i++)
        {
            final String monitoredResourceId = EtherServiceNamingUtils.getMRMonitoredResourceId(releaseName, serviceNames[i]);

            if (i == 0)
            {
                monitoredResourceQuery.append("&TFmrId=").append(monitoredResourceId).append("'");
            }
            else if (i == (serviceNames.length - 1))
            {
                monitoredResourceQuery.append(" or mrId='").append(monitoredResourceId);
            }
            else
            {
                monitoredResourceQuery.append(" or mrId='").append(monitoredResourceId).append("'");
            }
        }

        return monitoredResourceQuery.toString();
    }

    private static String cleanString(final String rawString)
    {
        return rawString.trim().replaceAll("\\s", "").replaceAll("\\W", "").replaceAll("\\.", "-").replaceAll("_", "-").toLowerCase();
    }
}