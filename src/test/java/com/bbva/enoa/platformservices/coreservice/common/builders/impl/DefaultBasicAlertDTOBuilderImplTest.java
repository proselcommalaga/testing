package com.bbva.enoa.platformservices.coreservice.common.builders.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultBasicAlertDTOBuilderImplTest
{
    @InjectMocks
    private DefaultBasicAlertDTOBuilderImpl defaultBasicAlertDTOBuilder;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class Convert
    {
        @Test
        @DisplayName("(convert) -> is the happy path working?")
        void convert()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var asBasicAlertInfoDTO = mock(ASBasicAlertInfoDTO.class);
            var deploymentSubsystem = mock(DeploymentSubsystem.class);
            var deploymentService = mock(DeploymentService.class);
            var deploymentInstance = mock(DeploymentInstance.class);

            // when
            when(deploymentPlan.getDeploymentSubsystems()).thenReturn(List.of(deploymentSubsystem));
            when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
            when(deploymentService.getInstances()).thenReturn(List.of(deploymentInstance));

            when(asBasicAlertInfoDTO.getAlertRelatedId()).thenReturn("1");
            when(deploymentInstance.getId()).thenReturn(1);

            when(deploymentInstance.getService()).thenReturn(deploymentService);
            when(deploymentService.getId()).thenReturn(123456789);

            // then
            var result = Assertions.assertDoesNotThrow(() -> defaultBasicAlertDTOBuilder.convert(deploymentPlan, asBasicAlertInfoDTO));
            Assertions.assertEquals(123456789, result.getAlertRelatedServiceId());
        }

        @Test
        @DisplayName("(convert) -> is the method throwing (shouldn't it) if there is no instance associated?")
        void convertNoInstance()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var asBasicAlertInfoDTO = mock(ASBasicAlertInfoDTO.class);
            var deploymentSubsystem = mock(DeploymentSubsystem.class);
            var deploymentService = mock(DeploymentService.class);
            var deploymentInstance = mock(DeploymentInstance.class);

            // when
            when(deploymentPlan.getDeploymentSubsystems()).thenReturn(List.of(deploymentSubsystem));
            when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
            when(deploymentService.getInstances()).thenReturn(List.of(deploymentInstance));

            when(asBasicAlertInfoDTO.getAlertRelatedId()).thenReturn(null);
            when(deploymentInstance.getId()).thenReturn(1);

            when(deploymentInstance.getService()).thenReturn(deploymentService);
            when(deploymentService.getId()).thenReturn(123456789);

            // then
            var result = Assertions.assertDoesNotThrow(() -> defaultBasicAlertDTOBuilder.convert(deploymentPlan, asBasicAlertInfoDTO));
            Assertions.assertNull(result.getAlertRelatedServiceId());
        }
    }


    @ParameterizedTest(name = "(isSupported) -> is the default builder associated (shouldn't it) with {0}?")
    @MethodSource("supportedParams")
    @DisplayName("(isSupported) -> is the black list working?")
    void isSupported(String alertType)
    {
        var supported = Assertions.assertDoesNotThrow(() -> this.defaultBasicAlertDTOBuilder.isSupported(alertType));
        Assertions.assertFalse(supported);
    }

    private static Stream<Arguments> supportedParams()
    {
        return Stream.of(Arguments.of("APP_BATCH_FAILURE"), Arguments.of("APP_BATCH_INTERRUPTED"), Arguments.of("APP_SCHEDULE_FAILURE"));
    }
}