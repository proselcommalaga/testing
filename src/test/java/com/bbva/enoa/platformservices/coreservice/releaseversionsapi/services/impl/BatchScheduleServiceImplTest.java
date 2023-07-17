package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.common.entities.LobFile;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.SchedulerManagerClientImpl;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class BatchScheduleServiceImplTest
{
    @InjectMocks
    private BatchScheduleServiceImpl batchScheduleService;

    @Mock
    private SchedulerManagerClientImpl schedulerManagerClient;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    void tearDown()
    {
        verifyNoMoreInteractions(
                schedulerManagerClient
        );
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class AddBatchScheduleServices{

        @Test
        @DisplayName("(addBatchSchedulServices) -> Scheduler name is not valid")
        void addBatchSchedulServicesInvalidSchedulerName()
        {
            // given
            ReleaseVersion releaseVersion = Mockito.mock(ReleaseVersion.class);
            ReleaseVersionSubsystem releaseVersionSubsystem = Mockito.mock(ReleaseVersionSubsystem.class);
            ReleaseVersionService releaseVersionService = Mockito.mock(ReleaseVersionService.class);
            LobFile lobFile = Mockito.mock(LobFile.class);

            // when
            Mockito.when(releaseVersion.getSubsystems()).thenReturn(List.of(releaseVersionSubsystem));
            Mockito.when(releaseVersionSubsystem.getServices()).thenReturn(List.of(releaseVersionService));
            Mockito.when(releaseVersionService.getServiceType()).thenReturn(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());
            Mockito.when(releaseVersionService.getServiceName()).thenReturn("serviceName");
            Mockito.when(releaseVersionService.getProjectDefinitionFile()).thenReturn(lobFile);
            Mockito.when(lobFile.getContents()).thenReturn("definitionFile");

            // then
            NovaException ex = Assertions.assertThrows(NovaException.class, () -> batchScheduleService.addBatchSchedulServices(releaseVersion));
            Assertions.assertEquals(ReleaseVersionError.getSchedulerYmlError().getErrorCode(), ex.getNovaError().getErrorCode());
        }

        @Test
        @DisplayName("(addBatchSchedulServices) -> There are no scheduler services")
        void addBatchSchedulServicesEmptyScheduler()
        {
            // given
            ReleaseVersion releaseVersion = Mockito.mock(ReleaseVersion.class);
            ReleaseVersionSubsystem releaseVersionSubsystem = Mockito.mock(ReleaseVersionSubsystem.class);
            ReleaseVersionService releaseVersionService = Mockito.mock(ReleaseVersionService.class);
            LobFile lobFile = Mockito.mock(LobFile.class);

            // when
            Mockito.when(releaseVersion.getSubsystems()).thenReturn(List.of(releaseVersionSubsystem));
            Mockito.when(releaseVersionSubsystem.getServices()).thenReturn(List.of(releaseVersionService));
            Mockito.when(releaseVersionService.getServiceType()).thenReturn(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());
            Mockito.when(releaseVersionService.getServiceName()).thenReturn("serviceName");
            Mockito.when(releaseVersionService.getProjectDefinitionFile()).thenReturn(lobFile);
            Mockito.when(lobFile.getContents()).thenReturn("");

            // then
            NovaException ex = Assertions.assertThrows(NovaException.class, () -> batchScheduleService.addBatchSchedulServices(releaseVersion));
            Assertions.assertEquals(ReleaseVersionError.getNoSuchSchedulerYmlError().getErrorCode(), ex.getNovaError().getErrorCode());

        }

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class DeleteBatchScheduleServices
    {
        @Test
        @DisplayName("(DeleteBatchScheduleServices) -> Is the happy path working?")
        void deleteBatchScheduleServicesHappyPath()
        {
            // given
            ReleaseVersion releaseVersion = Mockito.mock(ReleaseVersion.class);
            ReleaseVersionSubsystem releaseVersionSubsystem = Mockito.mock(ReleaseVersionSubsystem.class);
            ReleaseVersionService releaseVersionService = Mockito.mock(ReleaseVersionService.class);

            // when
            Mockito.when(releaseVersion.getSubsystems()).thenReturn(List.of(releaseVersionSubsystem));
            Mockito.when(releaseVersionSubsystem.getServices()).thenReturn(List.of(releaseVersionService));
            Mockito.when(releaseVersionService.getServiceType()).thenReturn(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());
            Mockito.doNothing().when(schedulerManagerClient).removeBatchSchedulerService(Mockito.anyInt());

            // then
            Assertions.assertDoesNotThrow(() -> batchScheduleService.deleteBatchScheduleServices(releaseVersion));
            Mockito.verify(schedulerManagerClient, times(1)).removeBatchSchedulerService(Mockito.anyInt());
        }

        @Test
        @DisplayName("(DeleteBatchScheduleServices) -> Is throwing correctly the NovaException received?")
        void deleteBatchSchedulerServicesWithError()
        {
            // given
            ReleaseVersion releaseVersion = Mockito.mock(ReleaseVersion.class);
            ReleaseVersionSubsystem releaseVersionSubsystem = Mockito.mock(ReleaseVersionSubsystem.class);
            ReleaseVersionService releaseVersionService = Mockito.mock(ReleaseVersionService.class);
            String message = "[SchedulerManagerClientImpl] -> [removeBatchSchedulerService]: there was an error trying to delete a batch schedule service id: [" + 1 + "] " +
                    "from Scheduler Manager BBDD. Error message: [" + new Errors().getBodyExceptionMessage() + "]";
            NovaException ex = new NovaException(ReleaseVersionError.getBatchScheluderDeleteError(), new Errors(), message);

            // when
            Mockito.when(releaseVersion.getSubsystems()).thenReturn(List.of(releaseVersionSubsystem));
            Mockito.when(releaseVersionSubsystem.getServices()).thenReturn(List.of(releaseVersionService));
            Mockito.when(releaseVersionService.getServiceType()).thenReturn(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());
            Mockito.doThrow(ex).when(schedulerManagerClient).removeBatchSchedulerService(Mockito.anyInt());

            // then
            NovaException resultException = Assertions.assertThrows(NovaException.class, () -> batchScheduleService.deleteBatchScheduleServices(releaseVersion));
            Assertions.assertEquals(ex.getNovaError().getErrorCode(), resultException.getNovaError().getErrorCode());
            Mockito.verify(schedulerManagerClient, times(1)).removeBatchSchedulerService(Mockito.anyInt());
        }

    }

    @Test
    @DisplayName("(GetDeploymentBatchSchedule) -> Is the happy path working?")
    void getDeploymentBatchSchedule()
    {
        // given
        final Integer releaseVersionServiceId = 1;
        final Integer deploymentPlanId = 2;
        DeploymentBatchScheduleDTO deploymentBatchScheduleDTO = Mockito.mock(DeploymentBatchScheduleDTO.class);

        // when
        Mockito.when(schedulerManagerClient.getDeploymentBatchSchedule(Mockito.anyInt(), Mockito.anyInt())).thenReturn(deploymentBatchScheduleDTO);
        Mockito.when(deploymentBatchScheduleDTO.getDeploymentPlanId()).thenReturn(deploymentPlanId);
        Mockito.when(deploymentBatchScheduleDTO.getReleaseVersionServiceId()).thenReturn(releaseVersionServiceId);

        // then
        DeploymentBatchScheduleDTO result = Assertions.assertDoesNotThrow(() -> batchScheduleService.getDeploymentBatchSchedule(releaseVersionServiceId, deploymentPlanId));
        Mockito.verify(schedulerManagerClient, times(1)).getDeploymentBatchSchedule(Mockito.anyInt(), Mockito.anyInt());
        Assertions.assertEquals(releaseVersionServiceId, result.getReleaseVersionServiceId());
        Assertions.assertEquals(deploymentPlanId, result.getDeploymentPlanId());
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class GetSchedulerYmlStringFile
    {
        @Test
        @DisplayName("(GetSchedulerYmlStringFile) -> Is the happy path working?")
        void getSchedulerYmlStringFile()
        {
            // given
            ReleaseVersionService releaseVersionService = Mockito.mock(ReleaseVersionService.class);
            LobFile lobFile = Mockito.mock(LobFile.class);

            // when
            Mockito.when(releaseVersionService.getProjectDefinitionFile()).thenReturn(lobFile);

            // then
            Assertions.assertDoesNotThrow(() -> batchScheduleService.getSchedulerYmlStringFile(releaseVersionService));
        }

        @Test
        @DisplayName("(GetSchedulerYmlStringFile) -> Check if the method throws a NOVA exception on invalid definition file")
        void getSchedulerYmlStringFileInvalidDefinitionFile()
        {
            // given
            ReleaseVersionService releaseVersionService = Mockito.mock(ReleaseVersionService.class);

            // when
            Mockito.when(releaseVersionService.getProjectDefinitionFile()).thenReturn(null);

            // then
            NovaException exception = Assertions.assertThrows(NovaException.class, () -> batchScheduleService.getSchedulerYmlStringFile(releaseVersionService));
            Assertions.assertEquals(ReleaseVersionError.getNoSuchSchedulerYmlError().getErrorCode(), exception.getNovaError().getErrorCode());
        }

    }

}