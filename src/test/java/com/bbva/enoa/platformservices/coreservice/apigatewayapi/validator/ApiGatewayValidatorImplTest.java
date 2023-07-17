package com.bbva.enoa.platformservices.coreservice.apigatewayapi.validator;

import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApiGatewayValidatorImplTest
{

    private ApiGatewayValidatorImpl apiGatewayValidator = new ApiGatewayValidatorImpl();

    @Nested
    class CheckReleaseVersionHasRESTServices
    {

        @Test
        @DisplayName("Check release version has REST services -> Empty subsystem list")
        public void noSubsystem()
        {

            var releaseVersion = new ReleaseVersion();

            boolean result = apiGatewayValidator.checkReleaseVersionHasRESTServices(releaseVersion);

            assertFalse(result);
        }

        @Test
        @DisplayName("Check release version has REST services -> Empty services list")
        public void noServices()
        {
            var releaseVersion = new ReleaseVersion();
            releaseVersion.setSubsystems(List.of(new ReleaseVersionSubsystem()));

            boolean result = apiGatewayValidator.checkReleaseVersionHasRESTServices(releaseVersion);

            assertFalse(result);
        }

        @ParameterizedTest(name = "[{index}] serviceType: {0}")
        @ArgumentsSource(NoMicrogatewayServiceTypeArgumentsProvider.class)
        @DisplayName("Check release version has REST services -> Services without microgateway")
        public void serviceTypeNotRest(ServiceType serviceType)
        {
            var releaseVersion = new ReleaseVersion();
            var releaseVersionSubsystem = new ReleaseVersionSubsystem();
            var releaseVersionService = new ReleaseVersionService();
            releaseVersionService.setServiceType(serviceType.getServiceType());
            releaseVersionSubsystem.setServices(List.of(releaseVersionService));
            releaseVersion.setSubsystems(List.of(releaseVersionSubsystem));

            boolean result = apiGatewayValidator.checkReleaseVersionHasRESTServices(releaseVersion);

            assertFalse(result);
        }

        @ParameterizedTest(name = "[{index}] serviceType: {0}")
        @ArgumentsSource(MicrogatewayServiceTypeArgumentsProvider.class)
        @DisplayName("Check release version has REST services -> Services with microgateway")
        public void ok(ServiceType serviceType)
        {
            var releaseVersion = new ReleaseVersion();
            var releaseVersionSubsystem = new ReleaseVersionSubsystem();
            var releaseVersionService = new ReleaseVersionService();
            releaseVersionService.setServiceType(serviceType.getServiceType());
            releaseVersionSubsystem.setServices(List.of(releaseVersionService));
            releaseVersion.setSubsystems(List.of(releaseVersionSubsystem));

            boolean result = apiGatewayValidator.checkReleaseVersionHasRESTServices(releaseVersion);

            assertTrue(result);
        }

    }

    @Nested
    class CheckReleaseVersionServiceHasMicrogateway
    {
        @ParameterizedTest(name = "[{index}] serviceType: {0}")
        @ArgumentsSource(NoMicrogatewayServiceTypeArgumentsProvider.class)
        @DisplayName("Check release version service has microgateway -> False")
        public void checkFalse(ServiceType serviceType)
        {
            var releaseVersionService = new ReleaseVersionService();
            releaseVersionService.setServiceType(serviceType.getServiceType());

            boolean result = apiGatewayValidator.checkReleaseVersionServiceHasMicroGateway(releaseVersionService.getServiceType());

            assertFalse(result);
        }

        @ParameterizedTest(name = "[{index}] serviceType: {0}")
        @ArgumentsSource(MicrogatewayServiceTypeArgumentsProvider.class)
        @DisplayName("Check release version service has microgateway -> True")
        public void checkTrue(ServiceType serviceType)
        {
            var releaseVersionService = new ReleaseVersionService();
            releaseVersionService.setServiceType(serviceType.getServiceType());

            boolean result = apiGatewayValidator.checkReleaseVersionServiceHasMicroGateway(releaseVersionService.getServiceType());

            assertTrue(result);
        }
    }

    private static class NoMicrogatewayServiceTypeArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception
        {
            return Arrays.stream(ServiceType.values()).filter(serviceType -> !serviceType.isMicrogateway()).map(Arguments::of);
        }
    }

    private static class MicrogatewayServiceTypeArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception
        {
            return Arrays.stream(ServiceType.values()).filter(ServiceType::isMicrogateway).map(Arguments::of);
        }
    }

}