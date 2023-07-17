package com.bbva.enoa.platformservices.coreservice.common.builders.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleInstanceDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ISchedulerManagerClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchScheduleBasicAlertDTOBuilderImplTest
{
    private BatchScheduleBasicAlertDTOBuilderImpl batchScheduleBasicAlertDTOBuilder;
    @Mock
    private ISchedulerManagerClient schedulerManagerClient;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
        this.batchScheduleBasicAlertDTOBuilder = new BatchScheduleBasicAlertDTOBuilderImpl(this.schedulerManagerClient);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class FindRelatedService
    {
        @Test
        @DisplayName("(FindRelatedService) -> is the happy path working?")
        void findRelatedService()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var asBasicAlertInfoDTO = mock(ASBasicAlertInfoDTO.class);
            var deploymentBatchScheduleInstanceDTO = mock(DeploymentBatchScheduleInstanceDTO.class);
            var deploymentSubsystem = mock(DeploymentSubsystem.class);
            var deploymentService = mock(DeploymentService.class);
            var releaseVersionService = mock(ReleaseVersionService.class);

            // when
            when(asBasicAlertInfoDTO.getAlertRelatedId()).thenReturn("1234567");
            when(schedulerManagerClient.getDeploymentBatchScheduleInstanceById(any())).thenReturn(deploymentBatchScheduleInstanceDTO);
            when(deploymentBatchScheduleInstanceDTO.getReleaseVersionServiceId()).thenReturn(12345);
            when(deploymentPlan.getDeploymentSubsystems()).thenReturn(List.of(deploymentSubsystem));
            when(deploymentSubsystem.getDeploymentServices()).thenReturn(List.of(deploymentService));
            when(deploymentService.getService()).thenReturn(releaseVersionService);
            when(releaseVersionService.getId()).thenReturn(12345);
            when(deploymentService.getId()).thenReturn(333333);

            // then
            var result = Assertions.assertDoesNotThrow(() -> batchScheduleBasicAlertDTOBuilder.findRelatedService(deploymentPlan, asBasicAlertInfoDTO));
            Assertions.assertEquals(333333, result);
        }

        @Test
        @DisplayName("(FindRelatedService) -> is the method throwing (shouldn't it) if the client retrieve a null value?")
        void findRelatedServiceNullValue()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var asBasicAlertInfoDTO = mock(ASBasicAlertInfoDTO.class);

            // when
            when(asBasicAlertInfoDTO.getAlertRelatedId()).thenReturn("1234567");
            when(schedulerManagerClient.getDeploymentBatchScheduleInstanceById(any())).thenReturn(null);

            // then
            var result = Assertions.assertDoesNotThrow(() -> batchScheduleBasicAlertDTOBuilder.findRelatedService(deploymentPlan, asBasicAlertInfoDTO));
            Assertions.assertNull(result, "It's impossible to return a value!");
        }
    }


    @Test
    void isSupported()
    {
        Assertions.assertDoesNotThrow(() -> batchScheduleBasicAlertDTOBuilder.isSupported("APP_SCHEDULE_FAILURE"), "The BatchScheduleBasicAlertDTOBuilderImpl must be associated to the APP_SCHEDULE_FAILURE code");
    }
}