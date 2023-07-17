package com.bbva.enoa.platformservices.coreservice.common.util;

import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.release.entities.AllowedJdk;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JvmJdkConfigurationCheckerTest
{
    private JvmJdkConfigurationChecker checker;

    @Before
    public void init()
    {
        checker = new JvmJdkConfigurationChecker("11");
    }

    @Test
    public void when_service_type_is_not_allowed_to_select_jdk_then_return_not_appliable_for_jdk_parametersWithApiRest()
    {
        when_service_type_is_not_allowed_to_select_jdk_then_return_not_appliable_for_jdk_parameters(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }
    @Test
    public void when_service_type_is_not_allowed_to_select_jdk_then_return_not_appliable_for_jdk_parametersWithApi()
    {
        when_service_type_is_not_allowed_to_select_jdk_then_return_not_appliable_for_jdk_parameters(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void when_service_type_is_not_allowed_to_select_jdk_then_return_not_appliable_for_jdk_parameters(ServiceType serviceType)
    {
        final ReleaseVersionService dummyReleaseVersionService = getDummyReleaseVersionService("1.8.121", "dummy", serviceType);
        dummyReleaseVersionService.setServiceType(ServiceType.BATCH_PYTHON.getServiceType());
        final boolean result = checker.isMultiJdk(dummyReleaseVersionService);

        Assert.assertFalse(result);
    }

    @Test
    public void when_service_is_java_lower_than_11_then_return_not_appliable_for_jdk_parametersWithApiRest()
    {
        when_service_is_java_lower_than_11_then_return_not_appliable_for_jdk_parameters(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void when_service_is_java_lower_than_11_then_return_not_appliable_for_jdk_parametersWithApi()
    {
        when_service_is_java_lower_than_11_then_return_not_appliable_for_jdk_parameters(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void when_service_is_java_lower_than_11_then_return_not_appliable_for_jdk_parameters(ServiceType serviceType)
    {
        final boolean result = checker.isMultiJdk(getDummyReleaseVersionService("1.8.121", "dummy", serviceType));

        Assert.assertFalse(result);
    }

    @Test
    public void when_service_is_not_java_then_return_not_appliable_for_jdk_parametersWithApiRest()
    {
        when_service_is_not_java_then_return_not_appliable_for_jdk_parameters(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void when_service_is_not_java_then_return_not_appliable_for_jdk_parametersWithApi()
    {
        when_service_is_not_java_then_return_not_appliable_for_jdk_parameters(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void when_service_is_not_java_then_return_not_appliable_for_jdk_parameters(ServiceType serviceType)
    {
        final ReleaseVersionService dummyReleaseVersionService = getDummyReleaseVersionService("", "", serviceType);
        dummyReleaseVersionService.setAllowedJdk(null);
        final boolean result = checker.isMultiJdk(dummyReleaseVersionService);

        Assert.assertFalse(result);
    }

    @Test
    public void when_service_is_valid_java_then_return_service_is_appliable_for_jdk_parametersWithApiRest()
    {
        when_service_is_valid_java_then_return_service_is_appliable_for_jdk_parameters(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void when_service_is_valid_java_then_return_service_is_appliable_for_jdk_parametersWithApi()
    {
        when_service_is_valid_java_then_return_service_is_appliable_for_jdk_parameters(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void when_service_is_valid_java_then_return_service_is_appliable_for_jdk_parameters(ServiceType serviceType)
    {
        final boolean result = checker.isMultiJdk(getDummyReleaseVersionService("11.0.7", "dummy", serviceType));

        Assert.assertTrue(result);
    }

    private ReleaseVersionService getDummyReleaseVersionService(String jvmVersion, String jdkVersion, ServiceType serviceType)
    {
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setId(1);
        releaseVersionService.setAllowedJdk(getDummyAllowedJdk(jvmVersion, jdkVersion));
        releaseVersionService.setServiceType(serviceType.getServiceType());
        return releaseVersionService;
    }

    private AllowedJdk getDummyAllowedJdk(String jvmVersion, String jdkVersion)
    {
        AllowedJdk allowedJdk = new AllowedJdk();
        allowedJdk.setJvmVersion(jvmVersion);
        allowedJdk.setJdk(jdkVersion);
        return allowedJdk;
    }

}