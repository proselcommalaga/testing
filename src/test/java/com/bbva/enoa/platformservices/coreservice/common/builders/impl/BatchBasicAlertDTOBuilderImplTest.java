package com.bbva.enoa.platformservices.coreservice.common.builders.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.SpecificInstance;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.BatchManagerClient;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchBasicAlertDTOBuilderImplTest
{
    private BatchBasicAlertDTOBuilderImpl batchBasicAlertDTOBuilder;
    @Mock
    private BatchManagerClient batchManagerClient;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
        this.batchBasicAlertDTOBuilder = new BatchBasicAlertDTOBuilderImpl(batchManagerClient);
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class FindRelatedService
    {
        @Test
        @DisplayName("(findRelatedService) -> is the happy path working?")
        void findRelatedService()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var asBasicAlertInfoDTO = mock(ASBasicAlertInfoDTO.class);
            var specificInstance = mock(SpecificInstance.class);

            // when
            when(asBasicAlertInfoDTO.getAlertRelatedId()).thenReturn("123");
            when(deploymentPlan.getEnvironment()).thenReturn(Environment.PRE.getEnvironment());
            when(batchManagerClient.getInstanceById(any(), any())).thenReturn(specificInstance);
            when(specificInstance.getServiceId()).thenReturn(1L);

            // then
            var result = Assertions.assertDoesNotThrow(() -> batchBasicAlertDTOBuilder.findRelatedService(deploymentPlan, asBasicAlertInfoDTO));
            Assertions.assertEquals(1, result);
        }

        @Test
        @DisplayName("(findRelatedService) -> is the method throwing (shouldn't it) if the client retrieve a null value?")
        void findRelatedServiceWithoutSpecificInstance()
        {
            // given
            var deploymentPlan = mock(DeploymentPlan.class);
            var asBasicAlertInfoDTO = mock(ASBasicAlertInfoDTO.class);

            // when
            when(asBasicAlertInfoDTO.getAlertRelatedId()).thenReturn("123");
            when(deploymentPlan.getEnvironment()).thenReturn(Environment.PRE.getEnvironment());
            when(batchManagerClient.getInstanceById(any(), any())).thenReturn(null);

            // then
            var result = Assertions.assertDoesNotThrow(() -> batchBasicAlertDTOBuilder.findRelatedService(deploymentPlan, asBasicAlertInfoDTO));
            Assertions.assertNull(result, "It's impossible to return a value!");
        }
    }


    @Test
    @DisplayName("(isSupported) -> is the happy path working?")
    void isSupported()
    {
        Assertions.assertDoesNotThrow(() -> batchBasicAlertDTOBuilder.isSupported("APP_BATCH_FAILURE"), "The BatchBasicAlertDTOBuilderImpl must be associated to the APP_BATCH_FAILURE code");
        Assertions.assertDoesNotThrow(() -> batchBasicAlertDTOBuilder.isSupported("APP_BATCH_INTERRUPTED"), "The BatchBasicAlertDTOBuilderImpl must be associated to the APP_BATCH_INTERRUPTED code");
    }
}