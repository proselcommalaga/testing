package com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentPlanCardServiceStatusDto;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

class DeploymentServiceAccountableTypeProviderTest
{
    private static final String ONLINE_TYPE = "online";
    private static final String PRESENTATION_TYPE = "presentation";
    private static final String DAEMON_TYPE = "daemon";
    private static final String EPHOENIX_ONLINE_TYPE = "ephoenix_online";
    private static final String EPHOENIX_BATCH_TYPE = "ephoenix_batch";
    private static final String FRONTCAT_TYPE = "frontcat";
    private static final String[] AGGREGATED_TYPES = new String[]{ONLINE_TYPE, PRESENTATION_TYPE, DAEMON_TYPE, EPHOENIX_ONLINE_TYPE, EPHOENIX_BATCH_TYPE, FRONTCAT_TYPE};
    private final DeploymentServiceAccountableTypeProvider provider = new DeploymentServiceAccountableTypeProvider();

    @Test
    public void when_ask_for_service_type_empty_counter_map_then_return_empty_counter_map()
    {
        Map<String, DeploymentPlanCardServiceStatusDto> result = provider.getServiceTypeCounterEmptyMap();

        Assertions.assertEquals(AGGREGATED_TYPES.length, result.size());
        Set<String> resultKeys = result.keySet();
        for (String aggregatedType : AGGREGATED_TYPES)
        {
            Assertions.assertTrue(resultKeys.contains(aggregatedType));
            DeploymentPlanCardServiceStatusDto dto = result.get(aggregatedType);
            Assertions.assertEquals(0, dto.getTotal());
            Assertions.assertEquals(0, dto.getRunning());
        }
    }

    @Test
    public void when_getting_classified_type_from_null_type_then_return_null()
    {
        Assertions.assertNull(provider.getAccountableTypeFrom(null));
    }

    @Test
    public void when_getting_classified_type_from_empty_type_then_return_null()
    {
        Assertions.assertNull(provider.getAccountableTypeFrom(""));
    }

    @Test
    public void when_getting_classified_type_from_unknown_type_then_return_null()
    {
        Assertions.assertNull(provider.getAccountableTypeFrom("AAA"));
    }

    @Test
    public void when_getting_classified_type_from_known_type_then_return_resultWithApiRest()
    {
        when_getting_classified_type_from_known_type_then_return_result(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void when_getting_classified_type_from_known_type_then_return_resultWithApi()
    {
        when_getting_classified_type_from_known_type_then_return_result(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void when_getting_classified_type_from_known_type_then_return_result(ServiceType serviceType)
    {
        String result = provider.getAccountableTypeFrom(serviceType.name());

        Assertions.assertEquals(ONLINE_TYPE, result);
    }

    @Test
    public void when_check_if_ignored_type_is_accountable_then_return_false()
    {
        Assertions.assertFalse(provider.isAccountableType(ServiceType.DEPENDENCY.name()));
    }

    @Test
    public void when_check_if_known_type_is_accountable_then_return_trueWithApiRest()
    {
        when_check_if_known_type_is_accountable_then_return_true(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void when_check_if_known_type_is_accountable_then_return_trueWithApi()
    {
        when_check_if_known_type_is_accountable_then_return_true(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void when_check_if_known_type_is_accountable_then_return_true(ServiceType serviceType)
    {
        Assertions.assertTrue(provider.isAccountableType(serviceType.name()));
    }

    @Test
    public void when_check_if_null_type_is_valid_then_return_false()
    {
        Assertions.assertFalse(provider.isValidType(null));
    }

    @Test
    public void when_check_if_empty_type_is_valid_then_return_false()
    {
        Assertions.assertFalse(provider.isValidType(""));
    }

    @Test
    public void when_check_if_unknown_type_is_valid_then_return_false()
    {
        Assertions.assertFalse(provider.isValidType("PAPA"));
    }

    @Test
    public void when_check_if_known_type_is_valid_then_return_trueWithApiRest()
    {
        when_check_if_known_type_is_valid_then_return_true(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void when_check_if_known_type_is_valid_then_return_trueWithApi()
    {
        when_check_if_known_type_is_valid_then_return_true(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void when_check_if_known_type_is_valid_then_return_true(ServiceType serviceType)
    {
        Assertions.assertTrue(provider.isValidType(serviceType.name()));
    }
}