package com.bbva.enoa.platformservices.coreservice.statisticsapi.utils;


import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.platformservices.coreservice.common.enums.ServiceGroupingNames;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized service group provider, based on a (service_type, service_group) map.
 */
public final class ServiceTypeGroupProvider
{
    private static Map<String, ServiceGroupingNames> serviceTypeGroupMap;

    /**
     * Empty constructor
     */
    private ServiceTypeGroupProvider()
    {
        // empty
    }

    static
    {
        serviceTypeGroupMap = new HashMap<>(ServiceType.values().length);
        serviceTypeGroupMap.put(ServiceType.NOVA.getServiceType(), ServiceGroupingNames.API);
        serviceTypeGroupMap.put(ServiceType.API_REST_JAVA_SPRING_BOOT.getServiceType(), ServiceGroupingNames.API);
        serviceTypeGroupMap.put(ServiceType.API_JAVA_SPRING_BOOT.getServiceType(), ServiceGroupingNames.API);
        serviceTypeGroupMap.put(ServiceType.API_REST_NODE_JS_EXPRESS.getServiceType(), ServiceGroupingNames.API);
        serviceTypeGroupMap.put(ServiceType.API_REST_PYTHON_FLASK.getServiceType(), ServiceGroupingNames.API);
        serviceTypeGroupMap.put(ServiceType.NOVA_BATCH.getServiceType(), ServiceGroupingNames.BATCH);
        serviceTypeGroupMap.put(ServiceType.NOVA_SPRING_BATCH.getServiceType(), ServiceGroupingNames.BATCH);
        serviceTypeGroupMap.put(ServiceType.BATCH_JAVA_SPRING_BATCH.getServiceType(), ServiceGroupingNames.BATCH);
        serviceTypeGroupMap.put(ServiceType.BATCH_JAVA_SPRING_CLOUD_TASK.getServiceType(), ServiceGroupingNames.BATCH);
        serviceTypeGroupMap.put(ServiceType.BATCH_PYTHON.getServiceType(), ServiceGroupingNames.BATCH);
        serviceTypeGroupMap.put(ServiceType.DAEMON_JAVA_SPRING_BOOT.getServiceType(), ServiceGroupingNames.DAEMON);
        serviceTypeGroupMap.put(ServiceType.NODE.getServiceType(), ServiceGroupingNames.CDN);
        serviceTypeGroupMap.put(ServiceType.THIN2.getServiceType(), ServiceGroupingNames.CDN);
        serviceTypeGroupMap.put(ServiceType.CDN_POLYMER_CELLS.getServiceType(), ServiceGroupingNames.CDN);
        serviceTypeGroupMap.put(ServiceType.CDN_ANGULAR_THIN2.getServiceType(), ServiceGroupingNames.CDN);
        serviceTypeGroupMap.put(ServiceType.CDN_ANGULAR_THIN3.getServiceType(), ServiceGroupingNames.CDN);
        serviceTypeGroupMap.put(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType(), ServiceGroupingNames.BATCH_SCHEDULER);
        serviceTypeGroupMap.put(ServiceType.DEPENDENCY.getServiceType(), ServiceGroupingNames.DEPENDENCY);
        serviceTypeGroupMap.put(ServiceType.LIBRARY_JAVA.getServiceType(), ServiceGroupingNames.LIBRARY);
        serviceTypeGroupMap.put(ServiceType.LIBRARY_NODE.getServiceType(), ServiceGroupingNames.LIBRARY);
        serviceTypeGroupMap.put(ServiceType.LIBRARY_PYTHON.getServiceType(), ServiceGroupingNames.LIBRARY);
        serviceTypeGroupMap.put(ServiceType.LIBRARY_THIN2.getServiceType(), ServiceGroupingNames.LIBRARY);
        serviceTypeGroupMap.put(ServiceType.LIBRARY_TEMPLATE.getServiceType(), ServiceGroupingNames.LIBRARY);
        serviceTypeGroupMap.put(ServiceType.EPHOENIX_ONLINE.getServiceType(), ServiceGroupingNames.EPHOENIX_ONLINE);
        serviceTypeGroupMap.put(ServiceType.EPHOENIX_BATCH.getServiceType(), ServiceGroupingNames.EPHOENIX_BATCH);
        serviceTypeGroupMap.put(ServiceType.FRONTCAT_JAVA_SPRING_MVC.getServiceType(), ServiceGroupingNames.FRONTCAT);
        serviceTypeGroupMap.put(ServiceType.FRONTCAT_JAVA_J2EE.getServiceType(), ServiceGroupingNames.FRONTCAT);
        serviceTypeGroupMap.put(ServiceType.BUSINESS_LOGIC_BEHAVIOR_TEST_JAVA.getServiceType(), ServiceGroupingNames.BEHAVIOR_TEST);
    }

    /**
     * Given a service type, it returns the string representing the group that service type belongs to.
     *
     * @param serviceType String with service type.
     * @return A string with the service group name containing the service type.
     */
    public static String getServiceGroupNameFor(final String serviceType)
    {
        return serviceTypeGroupMap.containsKey(serviceType) ? serviceTypeGroupMap.get(serviceType).name() : null;
    }
}
