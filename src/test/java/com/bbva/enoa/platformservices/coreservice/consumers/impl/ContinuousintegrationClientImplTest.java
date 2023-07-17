package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.continuousintegrationapi.client.feign.nova.rest.IRestHandlerContinuousintegrationapi;
import com.bbva.enoa.apirestgen.continuousintegrationapi.model.CIEphoenixJobParametersDTO;
import com.bbva.enoa.apirestgen.continuousintegrationapi.model.CIJenkinsBuildSnapshotDTO;
import com.bbva.enoa.apirestgen.continuousintegrationapi.model.CIJobDTO;
import com.bbva.enoa.apirestgen.continuousintegrationapi.model.CINovaJobParametersDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.common.enumerates.AsyncStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.impl.QualityManagerService;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util.QualityConstants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ContinuousintegrationClientImplTest
{
    private static final String AVAILABLE_QUALITY_DESCRIPTION = "Los subsistemas han sido compilados satisfactoriamente. Release version lista para desplegar.";
    private static final String NOT_AVAILABLE_QUALITY_DESCRIPTION = "Se ha producido un error al recopilar la calidad de los servicios. Por favor, vuelva a intentarlo más tarde o contacte con el equipo NOVA.";
    @Mock
    private IRestHandlerContinuousintegrationapi restInterface;
    @Mock
    private IVersioncontrolsystemClient vcsClient;
    @Mock
    private TodoTaskServiceClient todoTaskServiceClient;
    @Mock
    private QualityManagerService qualityManagerService;
    @Mock
    private IToolsClient toolsClient;
    @InjectMocks
    private ContinuousintegrationClientImpl client;

    @BeforeEach
    public void init()
    {
        MockitoAnnotations.initMocks(ContinuousintegrationClientImpl.class);
        client.init();
    }

    @Test
    public void when_no_subsystems_are_found_and_quality_is_ok_then_set_status_ready_to_deploy_and_quality_validation_true_dont_build_subsystems_and_return_false()
    {
        Mockito.when(qualityManagerService.checkReleaseVersionQualityState(Mockito.any(ReleaseVersion.class), Mockito.anyBoolean())).thenReturn(QualityConstants.SQA_OK);

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        releaseVersion.setSubsystems(Collections.emptyList());
        final boolean result = client.buildSubsystems(new Product(), releaseVersion, "");

        Assertions.assertFalse(result);
        Assertions.assertEquals(ReleaseVersionStatus.READY_TO_DEPLOY, releaseVersion.getStatus());
        Assertions.assertEquals(AVAILABLE_QUALITY_DESCRIPTION, releaseVersion.getStatusDescription());
        Assertions.assertTrue(releaseVersion.getQualityValidation());
    }

    @Test
    public void when_no_subsystems_are_found_and_quality_is_not_ok_then_set_status_ready_to_deploy_and_quality_validation_false_and_dont_build_subsystems_and_return_false()
    {
        Mockito.when(qualityManagerService.checkReleaseVersionQualityState(Mockito.any(ReleaseVersion.class), Mockito.anyBoolean())).thenReturn(QualityConstants.SQA_ERROR);

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        releaseVersion.setSubsystems(Collections.emptyList());
        final boolean result = client.buildSubsystems(new Product(), releaseVersion, "");

        Assertions.assertFalse(result);
        Assertions.assertEquals(ReleaseVersionStatus.READY_TO_DEPLOY, releaseVersion.getStatus());
        Assertions.assertEquals(AVAILABLE_QUALITY_DESCRIPTION, releaseVersion.getStatusDescription());
        Assertions.assertFalse(releaseVersion.getQualityValidation());
    }

    @Test
    public void when_no_subsystems_are_found_and_quality_is_not_available_then_set_status_to_errors_and_dont_build_subsystems_and_return_false()
    {
        Mockito.when(qualityManagerService.checkReleaseVersionQualityState(Mockito.any(ReleaseVersion.class), Mockito.anyBoolean())).thenReturn(QualityConstants.SQA_NOT_AVAILABLE);

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        releaseVersion.setSubsystems(Collections.emptyList());
        final boolean result = client.buildSubsystems(new Product(), releaseVersion, "");

        Assertions.assertFalse(result);
        Assertions.assertEquals(ReleaseVersionStatus.ERRORS, releaseVersion.getStatus());
        Assertions.assertEquals(NOT_AVAILABLE_QUALITY_DESCRIPTION, releaseVersion.getStatusDescription());
    }

    @Test
    public void when_subsystems_compilation_throws_exception_then_set_release_version_status_to_errors_and_throw_exception()
    {
        Mockito.when(toolsClient.getSubsystemById(Mockito.anyInt())).thenThrow(DummyConsumerDataGenerator.getDummyNovaException());

        final ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        try
        {
            client.buildSubsystems(new Product(), releaseVersion, "");
        }
        catch (NovaException e)
        {
            Assertions.assertEquals("ERROR-000", e.getErrorCode().getErrorCode());
            Assertions.assertEquals(ReleaseVersionStatus.ERRORS, releaseVersion.getStatus());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_is_nova_subsystem_and_some_vcs_dependency_has_unknown_folder_then_throw_exception_on_creating_job_parameters()
    {
        Mockito.when(toolsClient.getSubsystemById(Mockito.anyInt())).thenReturn(DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.NOVA));
        Mockito.when(vcsClient.getDependencies(Mockito.anyInt(), Mockito.anyString())).thenReturn(List.of("A", "B"));

        final ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        try
        {
            client.buildSubsystems(new Product(), releaseVersion, "");
        }
        catch (NovaException e)
        {
            Assertions.assertEquals("RELEASEVERSIONS-019", e.getErrorCode().getErrorCode());
            Assertions.assertEquals(ReleaseVersionStatus.ERRORS, releaseVersion.getStatus());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_nova_subsystems_compilation_has_errors_then_return_true_with_error_status_on_release_version_and_subsystems()
    {
        Mockito.when(toolsClient.getSubsystemById(Mockito.anyInt())).thenReturn(DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.NOVA));
        Mockito.when(vcsClient.getDependencies(Mockito.anyInt(), Mockito.anyString())).thenReturn(List.of("FOLDER"));
        Mockito.when(restInterface.buildNovaJob(Mockito.any(CINovaJobParametersDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyCIResponseDTO("Message", "-1"), HttpStatus.OK));

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        final boolean result = client.buildSubsystems(new Product(), releaseVersion, "");

        Assertions.assertTrue(result);
        Assertions.assertEquals(ReleaseVersionStatus.ERRORS, releaseVersion.getStatus());
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            Assertions.assertEquals(AsyncStatus.ERROR, subsystem.getStatus());
        }
    }

    @Test
    public void when_nova_subsystems_compilation_has_not_ok_response_then_return_true_with_error_status_on_release_version_and_subsystems()
    {
        Mockito.when(toolsClient.getSubsystemById(Mockito.anyInt())).thenReturn(DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.NOVA));
        Mockito.when(vcsClient.getDependencies(Mockito.anyInt(), Mockito.anyString())).thenReturn(List.of("FOLDER"));
        Mockito.when(restInterface.buildNovaJob(Mockito.any(CINovaJobParametersDTO.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        final boolean result = client.buildSubsystems(new Product(), releaseVersion, "");

        Assertions.assertTrue(result);
        Assertions.assertEquals(ReleaseVersionStatus.ERRORS, releaseVersion.getStatus());
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            Assertions.assertEquals(AsyncStatus.ERROR, subsystem.getStatus());
        }
    }

    @Test
    public void when_nova_subsystem_compilation_is_ok_and_has_no_prior_compilation_then_return_true_with_building_status_on_release_version_and_doing_status_on_subsystems()
    {
        Mockito.when(toolsClient.getSubsystemById(Mockito.anyInt())).thenReturn(DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.NOVA));
        Mockito.when(vcsClient.getDependencies(Mockito.anyInt(), Mockito.anyString())).thenReturn(List.of("FOLDER"));
        Mockito.when(restInterface.buildNovaJob(Mockito.any(CINovaJobParametersDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyCIResponseDTO("Message".repeat(20), "1"), HttpStatus.OK));

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        final boolean result = client.buildSubsystems(new Product(), releaseVersion, "");

        Assertions.assertTrue(result);
        Assertions.assertEquals(ReleaseVersionStatus.BUILDING, releaseVersion.getStatus());
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            Assertions.assertEquals(AsyncStatus.DOING, subsystem.getStatus());
        }
    }

    @Test
    public void when_nova_subsystem_has_not_available_quality_status_then_return_false_with_errors_status_on_release_version_and_done_status_on_subsystems()
    {
        Mockito.when(toolsClient.getSubsystemById(Mockito.anyInt())).thenReturn(DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.NOVA));
        Mockito.when(vcsClient.getDependencies(Mockito.anyInt(), Mockito.anyString())).thenReturn(List.of("FOLDER"));
        Mockito.when(qualityManagerService.checkReleaseVersionQualityState(Mockito.any(ReleaseVersion.class), Mockito.anyBoolean())).thenReturn(QualityConstants.SQA_NOT_AVAILABLE);

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        ReleaseVersionSubsystem releaseVersionSubsystem = releaseVersion.getSubsystems().get(0);
        ReleaseVersionService releaseVersionService = releaseVersionSubsystem.getServices().get(0);
        releaseVersionService.setServiceType(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());
        Product product = new Product();
        product.setUuaa("AAAA");

        final boolean result = client.buildSubsystems(product, releaseVersion, "");

        Assertions.assertFalse(result);
        Assertions.assertEquals(ReleaseVersionStatus.ERRORS, releaseVersion.getStatus());
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            Assertions.assertEquals(AsyncStatus.DONE, subsystem.getStatus());
        }
    }

    @Test
    public void when_nova_subsystem_has_ok_quality_status_then_return_false_with_errors_status_on_release_version_and_done_status_on_subsystems()
    {
        Mockito.when(toolsClient.getSubsystemById(Mockito.anyInt())).thenReturn(DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.NOVA));
        Mockito.when(vcsClient.getDependencies(Mockito.anyInt(), Mockito.anyString())).thenReturn(List.of("FOLDER"));
        Mockito.when(qualityManagerService.checkReleaseVersionQualityState(Mockito.any(ReleaseVersion.class), Mockito.anyBoolean())).thenReturn(QualityConstants.SQA_OK);

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        ReleaseVersionSubsystem releaseVersionSubsystem = releaseVersion.getSubsystems().get(0);
        ReleaseVersionService releaseVersionService = releaseVersionSubsystem.getServices().get(0);
        releaseVersionService.setServiceType(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());
        Product product = new Product();
        product.setUuaa("AAAA");

        final boolean result = client.buildSubsystems(product, releaseVersion, "");

        Assertions.assertFalse(result);
        Assertions.assertEquals(ReleaseVersionStatus.READY_TO_DEPLOY, releaseVersion.getStatus());
        Assertions.assertTrue(releaseVersion.getQualityValidation());
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            Assertions.assertEquals(AsyncStatus.DONE, subsystem.getStatus());
        }
    }

    @Test
    public void when_nova_subsystem_has_error_quality_status_then_return_false_with_errors_status_on_release_version_and_done_status_on_subsystems()
    {
        Mockito.when(toolsClient.getSubsystemById(Mockito.anyInt())).thenReturn(DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.NOVA));
        Mockito.when(vcsClient.getDependencies(Mockito.anyInt(), Mockito.anyString())).thenReturn(List.of("FOLDER"));
        Mockito.when(qualityManagerService.checkReleaseVersionQualityState(Mockito.any(ReleaseVersion.class), Mockito.anyBoolean())).thenReturn(QualityConstants.SQA_ERROR);

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        ReleaseVersionSubsystem releaseVersionSubsystem = releaseVersion.getSubsystems().get(0);
        ReleaseVersionService releaseVersionService = releaseVersionSubsystem.getServices().get(0);
        releaseVersionService.setServiceType(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());
        Product product = new Product();
        product.setUuaa("AAAA");

        final boolean result = client.buildSubsystems(product, releaseVersion, "");

        Assertions.assertFalse(result);
        Assertions.assertEquals(ReleaseVersionStatus.READY_TO_DEPLOY, releaseVersion.getStatus());
        Assertions.assertFalse(releaseVersion.getQualityValidation());
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            Assertions.assertEquals(AsyncStatus.DONE, subsystem.getStatus());
        }
    }

    @Test
    public void when_ephoenix_subsystem_compilation_is_ok_then_return_true_with_building_status_on_release_version_and_doing_status_on_subsystems()
    {
        Mockito.when(toolsClient.getSubsystemById(Mockito.anyInt())).thenReturn(DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.EPHOENIX));
        Mockito.when(restInterface.buildEphoenixJob(Mockito.any(CIEphoenixJobParametersDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyCIMultiResponseDTO(new String[]{"1"}), HttpStatus.OK));

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        Product product = new Product();
        product.setUuaa("AAAA");

        final boolean result = client.buildSubsystems(product, releaseVersion, "");

        Assertions.assertTrue(result);
        Assertions.assertEquals(ReleaseVersionStatus.BUILDING, releaseVersion.getStatus());
        Assertions.assertFalse(releaseVersion.getQualityValidation());
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            Assertions.assertEquals(AsyncStatus.DOING, subsystem.getStatus());
        }
    }

    @Test
    public void when_ephoenix_subsystem_compilation_is_not_ok_then_return_true_with_errors_status_on_release_version_and_error_status_on_subsystems()
    {
        Mockito.when(toolsClient.getSubsystemById(Mockito.anyInt())).thenReturn(DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.EPHOENIX));
        Mockito.when(restInterface.buildEphoenixJob(Mockito.any(CIEphoenixJobParametersDTO.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        Product product = new Product();
        product.setUuaa("AAAA");
        product.setId(1);

        final boolean result = client.buildSubsystems(product, releaseVersion, "");

        Assertions.assertTrue(result);
        Assertions.assertEquals(ReleaseVersionStatus.ERRORS, releaseVersion.getStatus());
        Assertions.assertFalse(releaseVersion.getQualityValidation());
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            Assertions.assertEquals(AsyncStatus.ERROR, subsystem.getStatus());
        }

        Mockito.verify(todoTaskServiceClient, Mockito.times(1)).createGenericTask(Mockito.anyString(), Mockito.isNull(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_ephoenix_subsystem_has_no_available_quality_then_return_false_with_errors_status_on_release_version_and_doing_status_on_subsystems()
    {
        Mockito.when(toolsClient.getSubsystemById(Mockito.anyInt())).thenReturn(DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.EPHOENIX));
        Mockito.when(qualityManagerService.checkReleaseVersionQualityState(Mockito.any(ReleaseVersion.class), Mockito.anyBoolean())).thenReturn(QualityConstants.SQA_NOT_AVAILABLE);

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        releaseVersion.getSubsystems().get(0).getServices().get(0).setHasForceCompilation(Boolean.FALSE);
        Product product = new Product();
        product.setUuaa("AAAA");

        final boolean result = client.buildSubsystems(product, releaseVersion, "");

        Assertions.assertFalse(result);
        Assertions.assertEquals(ReleaseVersionStatus.ERRORS, releaseVersion.getStatus());
        Assertions.assertFalse(releaseVersion.getQualityValidation());
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            Assertions.assertEquals(AsyncStatus.DONE, subsystem.getStatus());
        }
    }

    @Test
    public void when_ephoenix_subsystem_has_ok_quality_then_return_false_with_ready_to_deploy_status_on_release_version_and_done_status_on_subsystems()
    {
        Mockito.when(toolsClient.getSubsystemById(Mockito.anyInt())).thenReturn(DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.EPHOENIX));
        Mockito.when(qualityManagerService.checkReleaseVersionQualityState(Mockito.any(ReleaseVersion.class), Mockito.anyBoolean())).thenReturn(QualityConstants.SQA_OK);

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        releaseVersion.getSubsystems().get(0).getServices().get(0).setHasForceCompilation(Boolean.FALSE);
        Product product = new Product();
        product.setUuaa("AAAA");

        final boolean result = client.buildSubsystems(product, releaseVersion, "");

        Assertions.assertFalse(result);
        Assertions.assertEquals(ReleaseVersionStatus.READY_TO_DEPLOY, releaseVersion.getStatus());
        Assertions.assertTrue(releaseVersion.getQualityValidation());
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            Assertions.assertEquals(AsyncStatus.DONE, subsystem.getStatus());
        }
    }

    @Test
    public void when_ephoenix_subsystem_has_error_quality_then_return_false_with_ready_to_deploy_status_on_release_version_and_done_status_on_subsystems()
    {
        Mockito.when(toolsClient.getSubsystemById(Mockito.anyInt())).thenReturn(DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.EPHOENIX));
        Mockito.when(qualityManagerService.checkReleaseVersionQualityState(Mockito.any(ReleaseVersion.class), Mockito.anyBoolean())).thenReturn(QualityConstants.SQA_ERROR);

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        releaseVersion.getSubsystems().get(0).getServices().get(0).setHasForceCompilation(Boolean.FALSE);
        Product product = new Product();
        product.setUuaa("AAAA");

        final boolean result = client.buildSubsystems(product, releaseVersion, "");

        Assertions.assertFalse(result);
        Assertions.assertEquals(ReleaseVersionStatus.READY_TO_DEPLOY, releaseVersion.getStatus());
        Assertions.assertFalse(releaseVersion.getQualityValidation());
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            Assertions.assertEquals(AsyncStatus.DONE, subsystem.getStatus());
        }
    }

    @Test
    public void errorOnModulePomVersionEphoenix()
    {
        Mockito.when(toolsClient.getSubsystemById(Mockito.anyInt())).thenReturn(DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.EPHOENIX));
        Mockito.when(qualityManagerService.checkReleaseVersionQualityState(Mockito.any(ReleaseVersion.class), Mockito.anyBoolean())).thenReturn(QualityConstants.SQA_ERROR_PROCESSING_MODULE);

        ReleaseVersion releaseVersion = DummyConsumerDataGenerator.getDummyReleaseVersion();
        releaseVersion.getSubsystems().get(0).getServices().get(0).setHasForceCompilation(Boolean.FALSE);
        Product product = new Product();
        product.setUuaa("AAAA");

        final boolean result = client.buildSubsystems(product, releaseVersion, "");

        Assertions.assertFalse(result);
        Assertions.assertEquals(ReleaseVersionStatus.ERRORS, releaseVersion.getStatus());
        Assertions.assertFalse(releaseVersion.getQualityValidation());
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            Assertions.assertEquals(AsyncStatus.DONE, subsystem.getStatus());
        }
    }


    @Test
    public void when_get_jobs_since_days_ago_returns_error_then_throw_exception()
    {
        Mockito.when(restInterface.getJobsSinceDaysAgo(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getJobsSinceDaysAgo(1, "A", "A"));
    }

    @Test
    public void when_get_jobs_since_days_ago_returns_ok_then_return_response_body()
    {
        Mockito.when(restInterface.getJobsSinceDaysAgo(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyCIJobsDtoArray(), HttpStatus.OK));

        CIJobDTO[] result = client.getJobsSinceDaysAgo(1, "A", "A");

        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals("Status", result[0].getStatus());
        Assertions.assertEquals(1, result[0].getId());
    }

    @Test
    public void when_get_build_snapshots_returns_error_then_throw_exception()
    {
        Mockito.when(restInterface.getJenkinsBuildsHistorySnapshot()).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getBuildsSnapshots());
    }

    @Test
    public void when_get_build_snapshots_returns_ok_then_return_response_body()
    {
        Mockito.when(restInterface.getJenkinsBuildsHistorySnapshot()).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyBuildSnapshots(), HttpStatus.OK));

        CIJenkinsBuildSnapshotDTO[] result = client.getBuildsSnapshots();

        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals("Status", result[0].getStatus());
        Assertions.assertEquals("BuildType", result[0].getBuildType());
        Assertions.assertEquals("UUAA", result[0].getUuaa());
        Assertions.assertEquals(1, result[0].getValue());
    }
}