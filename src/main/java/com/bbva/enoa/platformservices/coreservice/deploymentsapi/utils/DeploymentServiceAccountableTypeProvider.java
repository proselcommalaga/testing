package com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentPlanCardServiceStatusDto;

import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import org.springframework.stereotype.Service;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Class responsible of providing and validating (very basically) accountable classificated service types (online, presentation...).
 */
@Service
public final class DeploymentServiceAccountableTypeProvider
{
    private static final String ONLINE_TYPE = "online";
    private static final String PRESENTATION_TYPE = "presentation";
    private static final String DAEMON_TYPE = "daemon";
    private static final String EPHOENIX_ONLINE_TYPE = "ephoenix_online";
    private static final String EPHOENIX_BATCH_TYPE = "ephoenix_batch";
    private static final String FRONTCAT_TYPE = "frontcat";
    private static final String IGNORED_TYPE = "ignored";
    private static final String[] ACCOUNTABLE_TYPES = new String[]{ONLINE_TYPE, PRESENTATION_TYPE, DAEMON_TYPE, EPHOENIX_ONLINE_TYPE, EPHOENIX_BATCH_TYPE, FRONTCAT_TYPE};
    private static final Map<String, String> BACKEND_ACCOUNTABLE_TYPES_MAP = Map.ofEntries(
            new AbstractMap.SimpleEntry<>(ServiceType.NOVA.name(), ONLINE_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.API_REST_JAVA_SPRING_BOOT.name(), ONLINE_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.API_JAVA_SPRING_BOOT.name(), ONLINE_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.API_REST_NODE_JS_EXPRESS.name(), ONLINE_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.API_REST_PYTHON_FLASK.name(), ONLINE_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.THIN2.name(), PRESENTATION_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.NODE.name(), PRESENTATION_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.CDN_POLYMER_CELLS.name(), PRESENTATION_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.CDN_ANGULAR_THIN2.name(), PRESENTATION_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.CDN_ANGULAR_THIN3.name(), PRESENTATION_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.DAEMON_JAVA_SPRING_BOOT.name(), DAEMON_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.EPHOENIX_ONLINE.name(), EPHOENIX_ONLINE_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.EPHOENIX_BATCH.name(), EPHOENIX_BATCH_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.FRONTCAT_JAVA_SPRING_MVC.name(), FRONTCAT_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.FRONTCAT_JAVA_J2EE.name(), FRONTCAT_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.BATCH_JAVA_SPRING_BATCH.name(), IGNORED_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.NOVA_BATCH.name(), IGNORED_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.NOVA_SPRING_BATCH.name(), IGNORED_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.BATCH_PYTHON.name(), IGNORED_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.BATCH_JAVA_SPRING_CLOUD_TASK.name(), IGNORED_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.DEPENDENCY.name(), IGNORED_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.LIBRARY_JAVA.name(), IGNORED_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.LIBRARY_NODE.name(), IGNORED_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.LIBRARY_PYTHON.name(), IGNORED_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.LIBRARY_THIN2.name(), IGNORED_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.LIBRARY_TEMPLATE.name(), IGNORED_TYPE),
            new AbstractMap.SimpleEntry<>(ServiceType.BATCH_SCHEDULER_NOVA.name(), IGNORED_TYPE)
    );

    /**
     * Gets a map which keys are every accountable type and values are initialized status objects.
     *
     * @return A map which keys are every accountable type and values are initialized status objects.
     */
    public Map<String, DeploymentPlanCardServiceStatusDto> getServiceTypeCounterEmptyMap()
    {
        Map<String, DeploymentPlanCardServiceStatusDto> map = new HashMap<>(ACCOUNTABLE_TYPES.length);
        for (String accountableType : ACCOUNTABLE_TYPES)
        {
            DeploymentPlanCardServiceStatusDto dto = new DeploymentPlanCardServiceStatusDto();
            dto.setServiceType(accountableType);
            dto.setRunning(0);
            dto.setTotal(0);
            map.put(accountableType, dto);
        }
        return map;
    }

    /**
     * Returns the mapped accountable service type given a deployment service type.
     *
     * @param deploymentServiceType The deployment service type.
     * @return The mapped accountable service.
     */
    public String getAccountableTypeFrom(final String deploymentServiceType)
    {
        return deploymentServiceType != null && !deploymentServiceType.isEmpty() ? BACKEND_ACCOUNTABLE_TYPES_MAP.get(deploymentServiceType) : null;
    }

    /**
     * Returns if a given deployment service type is mapped to any accountable service type.
     *
     * @param deploymentServiceType The deployment service type.
     * @return True if deployment service type is mapped to an accountable service type; false otherwise.
     */
    public boolean isAccountableType(final String deploymentServiceType)
    {
        return !IGNORED_TYPE.equals(BACKEND_ACCOUNTABLE_TYPES_MAP.get(deploymentServiceType));
    }

    /**
     * Returns if a given deployment service type exists as an accountable types map key.
     *
     * @param deploymentServiceType The deployment service type.
     * @return True if deployment service type exists as a key; false otherwise.
     */
    public boolean isValidType(final String deploymentServiceType)
    {
        return deploymentServiceType != null
                && !deploymentServiceType.isEmpty()
                && BACKEND_ACCOUNTABLE_TYPES_MAP.containsKey(deploymentServiceType);
    }
}
