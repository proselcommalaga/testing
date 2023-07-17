package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployInstanceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeploySubsystemStatusDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.ActionStatus;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentMigrationDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentMigrationServiceDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentMigrationSubsystemDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentPlanCardServiceStatusDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentPlanCardStatusesDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentPlanLibraryRequirementsDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentSummaryDto;
import com.bbva.enoa.apirestgen.deploymentsapi.model.LMLibraryRequirementFulfilledDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.LMLibraryRequirementsFulfilledDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.*;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementsDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceFilesystem;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.CPD;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CPDRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.JdkParameterRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ManagementActionTaskRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IEtherManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IBatchManagerService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentStatusService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentServiceAccountableTypeProvider;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeploymentsApiServiceImplTest
{
    private static final String CONNECTOR_PREFIX = "CONNECTOR_";
    private static final int CONNECTOR_NUMBER = 5;

    int DEPLOYMENT_SERVICE_ID_SIZE = 3;
    int DEPLOYMENT_SERVICE_ID_1 = 0;
    int DEPLOYMENT_SERVICE_ID_2 = 1;
    int DEPLOYMENT_SERVICE_ID_3 = 2;
    int DEPLOYMENT_PLAN_ID_SIZE = 3;
    int DEPLOYMENT_PLAN_ID_1 = 100;
    int DEPLOYMENT_PLAN_ID_2 = 101;
    int DEPLOYMENT_PLAN_ID_3 = 102;
    int DEPLOYMENT_SUBSYSTEMS_SIZE = 3;

    int GOOD_MEMORY_FACTOR = 3;
    int BAD_MEMORY_FACTOR = 25;
    int BAD_INSTANCES_NUMBER = 1;
    String PRODUCT_NAME = "TEST-PRODUCT";
    String PRODUCT_NAMESPACE = "TEST-PRODUCT-NAMESPACE";
    String GROUP_ID = "GROUP-1";
    Integer PRODUCT_ID = 1;

    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;

    @Mock
    private DeploymentServiceRepository deploymentServiceRepository;

    @Mock
    private DeploymentPlanDtoBuilderImpl planBuilder;

    @Mock
    private DeploymentsValidatorImpl deploymentsValidator;

    @Mock
    private IDeploymentsService deploymentsService;

    @Mock
    private IDeploymentStatusService statusService;

    @Mock
    private IProductUsersClient usersClient;

    @Mock
    private ILibraryManagerService libraryManagerService;

    @Mock
    private JdkParameterRepository jdkParameterRepository;

    @Mock
    private DeploymentUtils deploymentUtils;

    @Mock
    private IEtherManagerClient etherManagerClient;

    @Mock
    private IBatchManagerService batchManagerService;

    @Mock
    private CPDRepository cpdRepository;

    @Mock
    private ManagementActionTaskRepository managementActionTaskRepository;

    @Mock
    private DeploymentManagerServiceImpl deploymentManagerService;

    @Spy
    private DeploymentServiceAccountableTypeProvider deploymentServiceAccountableTypeProvider;

    @InjectMocks
    private DeploymentsApiServiceImpl deploymentsApiService;

    @BeforeEach
    public void init()
    {
        MockitoAnnotations.initMocks(DeploymentsApiServiceImpl.class);
    }

    @Test
    public void getDeploymentPlan() throws Errors
    {
        //Given
        DeploymentDto deploymentDto = new DeploymentDto();
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        when(this.deploymentPlanRepository.findById(1)).thenReturn(Optional.of(deploymentPlan));
        when(this.planBuilder.build(deploymentPlan, "IMM0589")).thenReturn(deploymentDto);

        //Then
        this.deploymentsApiService.getDeploymentPlan("IMM0589", 1);
        verify(this.deploymentsValidator, times(1)).checkPlanExistence(deploymentPlan);
        DeploymentDto response = this.planBuilder.build(deploymentPlan, "IMM0589");
        assertEquals(deploymentDto, response);

        //Given
        when(this.deploymentPlanRepository.findById(1)).thenThrow(new RuntimeException(DeploymentError.getNoSuchServiceError(1).getErrors()));

        //Then
        assertThrows(RuntimeException.class, () -> this.deploymentsApiService.getDeploymentPlan("IMM0589", 1));

    }

    @Test
    public void getDeploymentPlanDeploymentException()
    {
        //Given
        when(this.deploymentPlanRepository.findById(1)).thenThrow(new NovaException(DeploymentError.getNoSuchServiceError(1), ""));

        //Then
        assertThrows(NovaException.class, () -> this.deploymentsApiService.getDeploymentPlan("IMM0589", 1));
    }

    @Test
    public void getDeploymentPlansByEnvironment()
    {
        //Given
        List<DeploymentPlan> plans = new ArrayList<>();
        when(this.deploymentPlanRepository.getByProductAndEnvironment(1, Environment.LOCAL.getEnvironment())).thenReturn(plans);

        //Then
        List<DeploymentPlan> response = this.deploymentPlanRepository.getByProductAndEnvironment(1, Environment.LOCAL.getEnvironment());
        assertEquals(plans, response);
        this.deploymentsApiService.getDeploymentPlansByEnvironment("IMM0589", 1,
                Environment.LOCAL.getEnvironment(), "");

        //Given
        List<DeploymentPlan> plans2 = new ArrayList<>();
        when(this.deploymentPlanRepository.getByProductAndEnvironmentAndStatus(1, Environment.LOCAL.getEnvironment(), DeploymentStatus.DEPLOYED)).thenReturn(plans);

        //Then
        List<DeploymentPlan> response2 = this.deploymentPlanRepository.getByProductAndEnvironmentAndStatus(1, Environment.LOCAL.getEnvironment(), DeploymentStatus.DEPLOYED);
        assertEquals(plans2, response2);
        this.deploymentsApiService.getDeploymentPlansByEnvironment("IMM0589", 1,
                Environment.LOCAL.getEnvironment(), DeploymentStatus.DEPLOYED.getDeploymentStatus());

        //Given
        List<DeploymentPlan> plans3 = new ArrayList<>();
        DeploymentDto deploymentDto = new DeploymentDto();
        DeploymentDto[] deploymentDtos = new DeploymentDto[]{deploymentDto};
        when(this.planBuilder.build(plans3, "IMM0589")).thenReturn(deploymentDtos);

        //Then
        DeploymentDto[] response3 = this.planBuilder.build(plans3, "IMM0589");
        assertEquals(deploymentDto, response3[0]);
        this.deploymentsApiService.getDeploymentPlansByEnvironment("IMM0589", 1,
                Environment.LOCAL.getEnvironment(), DeploymentStatus.DEPLOYED.getDeploymentStatus());

        //Given
        when(this.planBuilder.build(plans, "IMM0589")).thenThrow(new NovaException(DeploymentError.getUnexpectedError(), "Error"));

        //Then
        assertThrows(NovaException.class, () -> this.deploymentsApiService.getDeploymentPlansByEnvironment("IMM0589", 1,
                Environment.LOCAL.getEnvironment(), DeploymentStatus.DEPLOYED.getDeploymentStatus()));
    }

    @Test
    public void getDeploymentPlansByEnvironmentAndFilters()
    {
        //Given
        List<DeploymentPlan> deploymentPlanList = new ArrayList<>();
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setStatus(DeploymentStatus.DEPLOYED);
        deploymentPlan.setEnvironment(Environment.LOCAL.getEnvironment());
        deploymentPlanList.add(deploymentPlan);

        DeploymentSummaryDto deploymentSummaryDto = new DeploymentSummaryDto();

        when(this.deploymentsService.getDeploymentPlansBetween(anyString(), anyInt(), isNull(), isNull(), isNull())).thenReturn(deploymentPlanList);
        when(this.planBuilder.buildSummary(any(DeploymentPlan.class), anyString())).thenReturn(deploymentSummaryDto);

        //Then
        DeploymentSummaryDto[] result = this.deploymentsApiService.getDeploymentPlansByEnvironmentAndFilters("IMM0589", 1, "Local", "",
                "", null);

        assertEquals(1, result.length);
        assertEquals(deploymentSummaryDto, result[0]);
    }

    @Test
    public void getDeploymentPlansByEnvironmentAndFilters2()
    {
        //Given
        Mockito.when(this.deploymentsService.getDeploymentPlansBetween(anyString(), anyInt(), isNull(), isNull(), isNull())).thenThrow(new RuntimeException(DeploymentError.getUnexpectedError().getErrors()));

        //Then
        assertThrows(RuntimeException.class, () -> this.deploymentsApiService.getDeploymentPlansByEnvironmentAndFilters("IMM0589", 1, "Local", "", "", "status"));
    }

    @Test
    public void getDeploymentPlansByEnvironmentRunntimeException()
    {
        //Given
        List<DeploymentPlan> plans = new ArrayList<>();
        when(this.planBuilder.build(plans, "IMM0589")).thenThrow(new RuntimeException(DeploymentError.getUnexpectedError().getErrors()));

        //Then
        assertThrows(RuntimeException.class, () -> this.deploymentsApiService.getDeploymentPlansByEnvironment("IMM0589", 1,
                Environment.LOCAL.getEnvironment(), DeploymentStatus.DEPLOYED.getDeploymentStatus()));
    }

    @Test
    public void getDeploymentPlanStatus()
    {
        //Given
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(1);
        deploymentPlan.setStatus(DeploymentStatus.DEPLOYED);
        deploymentPlan.setAction(DeploymentAction.READY);
        ActionStatus status = new ActionStatus();
        when(this.statusService.getDeploymentPlanStatus(1)).thenReturn(status);

        //Then
        ActionStatus response = this.deploymentsApiService.getDeploymentPlanStatus(1);
        assertEquals(status, response);

        //Given
        when(this.statusService.getDeploymentPlanStatus(1)).thenThrow(new RuntimeException(DeploymentError.getUnexpectedError().getErrors()));

        //Then
        assertThrows(RuntimeException.class, () -> this.deploymentsApiService.getDeploymentPlanStatus(1));
    }

    @Test
    public void getDeploymentService()
    {
        //Given
        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setId(1);
        when(this.deploymentServiceRepository.findById(1)).thenReturn(Optional.of(deploymentService));

        //Then
        this.deploymentsApiService.getDeploymentService(1, 1);
        verify(this.planBuilder, times(1)).buildDtoFromEntity(deploymentService);

        //Given
        when(this.deploymentServiceRepository.findById(2)).thenThrow(new NovaException(DeploymentError.getNoSuchServiceError(2), "Error"));

        //Then
        assertThrows(NovaException.class, () -> this.deploymentsApiService.getDeploymentService(2, 1));
    }

    @Test
    public void getDeploymentServiceRunntimeException()
    {
        //Given
        when(this.deploymentServiceRepository.findById(1)).thenThrow(new RuntimeException(DeploymentError.getUnexpectedError().getErrors()));

        //Then
        assertThrows(RuntimeException.class, () -> this.deploymentsApiService.getDeploymentService(1, 1));
    }

    @Test
    public void getDeploymentServiceStatus()
    {
        //Given
        ActionStatus status = new ActionStatus();
        when(this.statusService.getDeploymentServiceStatus(1)).thenReturn(status);

        //Then
        ActionStatus response = this.deploymentsApiService.getDeploymentServiceStatus(1);
        assertEquals(status, response);

        //Given
        when(this.statusService.getDeploymentServiceStatus(1)).thenThrow(new RuntimeException(DeploymentError.getUnexpectedError().getErrors()));

        //Then
        assertThrows(RuntimeException.class, () -> this.deploymentsApiService.getDeploymentServiceStatus(1));
    }

    @Test
    public void getDeploymentSubsystemStatus()
    {
        //Given
        ActionStatus status = new ActionStatus();
        when(this.statusService.getDeploymentSubsystemStatus(1)).thenReturn(status);

        //Then
        ActionStatus response = this.deploymentsApiService.getDeploymentSubsystemStatus(1);
        assertEquals(status, response);

        //Given
        when(this.statusService.getDeploymentSubsystemStatus(1)).thenThrow(new RuntimeException(DeploymentError.getUnexpectedError().getErrors()));

        //Then
        assertThrows(RuntimeException.class, () -> this.deploymentsApiService.getDeploymentSubsystemStatus(1));
    }

    @Test
    public void getAllRequirementsOfUsedLibrariesEmptyIdsTest()
    {
        when(this.libraryManagerService.getAllRequirementsOfUsedLibraries(any())).thenReturn(null);
        LMLibraryRequirementsFulfilledDTO[] result = this.deploymentsApiService.getAllRequirementsOfUsedLibraries(new int[0]);
        assertNotNull(result);
    }

    @Test
    public void getAllRequirementsOfUsedLibrariesNotConfiguredResources()
    {
        int[] deploymentServiceIds = {DEPLOYMENT_SERVICE_ID_1, DEPLOYMENT_SERVICE_ID_2, DEPLOYMENT_SERVICE_ID_3};
        Set<Integer> deploymentServiceIdSet = new HashSet<>(Arrays.asList(
                DEPLOYMENT_SERVICE_ID_1, DEPLOYMENT_SERVICE_ID_2, DEPLOYMENT_SERVICE_ID_3));
        List<DeploymentService> deploymentServiceList =
                createDeploymentServiceList(DEPLOYMENT_SERVICE_ID_SIZE, false, false, false, false);
        when(deploymentServiceRepository.findByIdIn(deploymentServiceIdSet)).thenReturn(deploymentServiceList);

        LMLibraryRequirementsDTO[] lmLibraryRequirementsDTOS = createLMLibraryRequirementsDTO(
                DEPLOYMENT_SERVICE_ID_SIZE);
        when(this.libraryManagerService.getAllRequirementsOfUsedLibraries(any())).thenReturn(lmLibraryRequirementsDTOS);

        LMLibraryRequirementsFulfilledDTO[] result = this.deploymentsApiService.getAllRequirementsOfUsedLibraries(deploymentServiceIds);
        assertNotNull(result);
        assertEquals(DEPLOYMENT_SERVICE_ID_SIZE, result.length);
        assertEquals(DEPLOYMENT_SERVICE_ID_1, result[0].getDeploymentServiceId().intValue());
        assertEquals(DEPLOYMENT_SERVICE_ID_2, result[1].getDeploymentServiceId().intValue());
        assertEquals(DEPLOYMENT_SERVICE_ID_3, result[2].getDeploymentServiceId().intValue());
        assertTrue(Arrays.stream(result).noneMatch(LMLibraryRequirementsFulfilledDTO::getFulfilled));
        assertTrue(Arrays.stream(result).allMatch(
                        lrs -> Arrays.stream(lrs.getRequirements()).anyMatch(req -> !req.getFulfilled())
                )
        );
    }

    @Test
    public void getAllRequirementsOfUsedLibrariesWithLibrary()
    {
        int[] deploymentServiceIds = {DEPLOYMENT_SERVICE_ID_1, DEPLOYMENT_SERVICE_ID_2, DEPLOYMENT_SERVICE_ID_3};
        Set<Integer> deploymentServiceIdSet = new HashSet<>(Arrays.asList(
                DEPLOYMENT_SERVICE_ID_1, DEPLOYMENT_SERVICE_ID_2, DEPLOYMENT_SERVICE_ID_3));
        List<DeploymentService> deploymentServiceList =
                createDeploymentServiceList(DEPLOYMENT_SERVICE_ID_SIZE, true, false, true, true);
        lenient().when(deploymentServiceRepository.findByIdIn(deploymentServiceIdSet)).thenReturn(deploymentServiceList);

        LMLibraryRequirementsDTO[] lmLibraryRequirementsDTOS = createLMLibraryRequirementsDTO(
                DEPLOYMENT_SERVICE_ID_SIZE);
        lenient().when(this.libraryManagerService.getAllRequirementsOfUsedLibraries(any())).thenReturn(lmLibraryRequirementsDTOS);

        lenient().when(this.jdkParameterRepository.existsByNameAndDeploymentService(any(), any())).thenReturn(true);

        LMLibraryRequirementsFulfilledDTO[] result = this.deploymentsApiService.getAllRequirementsOfUsedLibraries(deploymentServiceIds);
        assertNotNull(result);
        assertEquals(DEPLOYMENT_SERVICE_ID_SIZE, result.length);
        assertEquals(DEPLOYMENT_SERVICE_ID_1, result[0].getDeploymentServiceId().intValue());
        assertEquals(DEPLOYMENT_SERVICE_ID_2, result[1].getDeploymentServiceId().intValue());
        assertEquals(DEPLOYMENT_SERVICE_ID_3, result[2].getDeploymentServiceId().intValue());
        assertTrue(Arrays.stream(result).allMatch(LMLibraryRequirementsFulfilledDTO::getFulfilled));
        assertTrue(Arrays.stream(result).allMatch(
                        lrs -> Arrays.stream(lrs.getRequirements()).allMatch(LMLibraryRequirementFulfilledDTO::getFulfilled)
                )
        );
    }

    @Test
    public void getAllRequirementsOfUsedLibrariesWithLibraryAndNamespace()
    {
        Product product = new Product();
        product.setName(PRODUCT_NAME);

        CheckReadyToDeployRequestDTO checkReadyToDeployRequestDTO = new CheckReadyToDeployRequestDTO();
        checkReadyToDeployRequestDTO.setEnvironment(Environment.INT.name());
        checkReadyToDeployRequestDTO.setNamespace(PRODUCT_NAMESPACE);
        checkReadyToDeployRequestDTO.setGroupId(GROUP_ID);
        checkReadyToDeployRequestDTO.setProductName(PRODUCT_NAME);

        int[] deploymentServiceIds = {DEPLOYMENT_SERVICE_ID_1, DEPLOYMENT_SERVICE_ID_2, DEPLOYMENT_SERVICE_ID_3};
        Set<Integer> deploymentServiceIdSet = new HashSet<>(Arrays.asList(
                DEPLOYMENT_SERVICE_ID_1, DEPLOYMENT_SERVICE_ID_2, DEPLOYMENT_SERVICE_ID_3));
        List<DeploymentService> deploymentServiceList =
                createDeploymentServiceList(DEPLOYMENT_SERVICE_ID_SIZE, true, false, true, true);
        lenient().when(deploymentServiceRepository.findByIdIn(deploymentServiceIdSet)).thenReturn(deploymentServiceList);

        LMLibraryRequirementsDTO[] lmLibraryRequirementsDTOS = createLMLibraryRequirementsNamespaceDTO(
                DEPLOYMENT_SERVICE_ID_SIZE);
        lenient().when(this.libraryManagerService.getAllRequirementsOfUsedLibraries(any())).thenReturn(lmLibraryRequirementsDTOS);
        lenient().when(this.etherManagerClient.readyToDeploy(any())).thenReturn(true);


        LMLibraryRequirementsFulfilledDTO[] result = this.deploymentsApiService.getAllRequirementsOfUsedLibraries(deploymentServiceIds);
        assertNotNull(result);
        assertEquals(DEPLOYMENT_SERVICE_ID_SIZE, result.length);
        assertEquals(DEPLOYMENT_SERVICE_ID_1, result[0].getDeploymentServiceId().intValue());
        assertEquals(DEPLOYMENT_SERVICE_ID_2, result[1].getDeploymentServiceId().intValue());
        assertEquals(DEPLOYMENT_SERVICE_ID_3, result[2].getDeploymentServiceId().intValue());
        assertTrue(Arrays.stream(result).allMatch(LMLibraryRequirementsFulfilledDTO::getFulfilled));
        assertTrue(Arrays.stream(result).allMatch(
                        lrs -> Arrays.stream(lrs.getRequirements()).allMatch(LMLibraryRequirementFulfilledDTO::getFulfilled)
                )
        );
    }

    @Test
    public void getAllRequirementsOfUsedLibrariesWithEmptyResponseFromLibraryManager()
    {
        int[] deploymentServiceIds = {DEPLOYMENT_SERVICE_ID_1};
        Set<Integer> deploymentServiceIdSet = new HashSet<>(List.of(DEPLOYMENT_SERVICE_ID_1));
        List<DeploymentService> deploymentServiceList =
                createDeploymentServiceList(DEPLOYMENT_SERVICE_ID_SIZE, false, false, false, false);
        when(deploymentServiceRepository.findByIdIn(deploymentServiceIdSet)).thenReturn(deploymentServiceList);

        LMLibraryRequirementsDTO[] lmLibraryRequirementsDTOS = createEmptyLMLibraryRequirementsDTO(
                DEPLOYMENT_SERVICE_ID_SIZE);
        when(this.libraryManagerService.getAllRequirementsOfUsedLibraries(any())).thenReturn(lmLibraryRequirementsDTOS);

        LMLibraryRequirementsFulfilledDTO[] result = this.deploymentsApiService.getAllRequirementsOfUsedLibraries(deploymentServiceIds);
        assertNotNull(result);
        assertEquals(DEPLOYMENT_SERVICE_ID_SIZE, result.length);
        assertEquals(DEPLOYMENT_SERVICE_ID_1, result[0].getDeploymentServiceId().intValue());
        assertTrue(Arrays.stream(result).allMatch(LMLibraryRequirementsFulfilledDTO::getFulfilled));
        assertTrue(Arrays.stream(result).allMatch(
                        lrs -> Arrays.stream(lrs.getRequirements()).allMatch(LMLibraryRequirementFulfilledDTO::getFulfilled)
                )
        );
    }

    @Test
    public void getAllRequirementsOfUsedLibrariesWithConfiguredResources()
    {
        int[] deploymentServiceIds = {DEPLOYMENT_SERVICE_ID_1, DEPLOYMENT_SERVICE_ID_2, DEPLOYMENT_SERVICE_ID_3};
        Set<Integer> deploymentServiceIdSet = new HashSet<>(Arrays.asList(
                DEPLOYMENT_SERVICE_ID_1, DEPLOYMENT_SERVICE_ID_2, DEPLOYMENT_SERVICE_ID_3));
        List<DeploymentService> deploymentServiceList =
                createDeploymentServiceList(DEPLOYMENT_SERVICE_ID_SIZE, true, false, true, true);
        lenient().when(deploymentServiceRepository.findByIdIn(deploymentServiceIdSet)).thenReturn(deploymentServiceList);

        LMLibraryRequirementsDTO[] lmLibraryRequirementsDTOS = createLMLibraryRequirementsDTO(
                DEPLOYMENT_SERVICE_ID_SIZE);
        lenient().when(this.libraryManagerService.getAllRequirementsOfUsedLibraries(any())).thenReturn(lmLibraryRequirementsDTOS);

        lenient().when(this.jdkParameterRepository.existsByNameAndDeploymentService(any(), any())).thenReturn(true);

        LMLibraryRequirementsFulfilledDTO[] result = this.deploymentsApiService.getAllRequirementsOfUsedLibraries(deploymentServiceIds);
        assertNotNull(result);
        assertEquals(DEPLOYMENT_SERVICE_ID_SIZE, result.length);
        assertEquals(DEPLOYMENT_SERVICE_ID_1, result[0].getDeploymentServiceId().intValue());
        assertEquals(DEPLOYMENT_SERVICE_ID_2, result[1].getDeploymentServiceId().intValue());
        assertEquals(DEPLOYMENT_SERVICE_ID_3, result[2].getDeploymentServiceId().intValue());
        assertTrue(Arrays.stream(result).allMatch(LMLibraryRequirementsFulfilledDTO::getFulfilled));
        assertTrue(Arrays.stream(result).allMatch(
                        lrs -> Arrays.stream(lrs.getRequirements()).allMatch(LMLibraryRequirementFulfilledDTO::getFulfilled)
                )
        );
    }

    @Test
    public void getAllRequirementsOfUsedLibrariesWithNotFulFilledJVM()
    {
        int[] deploymentServiceIds = {DEPLOYMENT_SERVICE_ID_1, DEPLOYMENT_SERVICE_ID_2, DEPLOYMENT_SERVICE_ID_3};
        Set<Integer> deploymentServiceIdSet = new HashSet<>(Arrays.asList(
                DEPLOYMENT_SERVICE_ID_1, DEPLOYMENT_SERVICE_ID_2, DEPLOYMENT_SERVICE_ID_3));
        List<DeploymentService> deploymentServiceList =
                createDeploymentServiceList(DEPLOYMENT_SERVICE_ID_SIZE, true, false, true, false);
        when(deploymentServiceRepository.findByIdIn(deploymentServiceIdSet)).thenReturn(deploymentServiceList);

        LMLibraryRequirementsDTO[] lmLibraryRequirementsDTOS = createLMLibraryRequirementsDTO(
                DEPLOYMENT_SERVICE_ID_SIZE);
        when(this.libraryManagerService.getAllRequirementsOfUsedLibraries(any())).thenReturn(lmLibraryRequirementsDTOS);

        LMLibraryRequirementsFulfilledDTO[] result = this.deploymentsApiService.getAllRequirementsOfUsedLibraries(deploymentServiceIds);
        assertNotNull(result);
        assertEquals(DEPLOYMENT_SERVICE_ID_SIZE, result.length);
        assertEquals(DEPLOYMENT_SERVICE_ID_1, result[0].getDeploymentServiceId().intValue());
        assertEquals(DEPLOYMENT_SERVICE_ID_2, result[1].getDeploymentServiceId().intValue());
        assertEquals(DEPLOYMENT_SERVICE_ID_3, result[2].getDeploymentServiceId().intValue());
        assertTrue(Arrays.stream(result).allMatch(
                        lrs -> Arrays.stream(lrs.getRequirements()).
                                filter(req -> req.getRequirementName().equals(Constants.RESOURCE_REQUIREMENT_NAME.JVM.toString())).
                                noneMatch(LMLibraryRequirementFulfilledDTO::getFulfilled)
                )
        );
    }

    @Test
    public void getAllRequirementsOfUsedLibrariesWithNotFulFilledInstancesNumber()
    {
        int[] deploymentServiceIds = {DEPLOYMENT_SERVICE_ID_1, DEPLOYMENT_SERVICE_ID_2, DEPLOYMENT_SERVICE_ID_3};
        Set<Integer> deploymentServiceIdSet = new HashSet<>(Arrays.asList(
                DEPLOYMENT_SERVICE_ID_1, DEPLOYMENT_SERVICE_ID_2, DEPLOYMENT_SERVICE_ID_3));
        List<DeploymentService> deploymentServiceList =
                createDeploymentServiceList(DEPLOYMENT_SERVICE_ID_SIZE, true, false, false, true);
        when(deploymentServiceRepository.findByIdIn(deploymentServiceIdSet)).thenReturn(deploymentServiceList);

        LMLibraryRequirementsDTO[] lmLibraryRequirementsDTOS = createLMLibraryRequirementsDTO(
                DEPLOYMENT_SERVICE_ID_SIZE);
        when(this.libraryManagerService.getAllRequirementsOfUsedLibraries(any())).thenReturn(lmLibraryRequirementsDTOS);

        LMLibraryRequirementsFulfilledDTO[] result = this.deploymentsApiService.getAllRequirementsOfUsedLibraries(deploymentServiceIds);
        assertNotNull(result);
        assertEquals(DEPLOYMENT_SERVICE_ID_SIZE, result.length);
        assertEquals(DEPLOYMENT_SERVICE_ID_1, result[0].getDeploymentServiceId().intValue());
        assertEquals(DEPLOYMENT_SERVICE_ID_2, result[1].getDeploymentServiceId().intValue());
        assertEquals(DEPLOYMENT_SERVICE_ID_3, result[2].getDeploymentServiceId().intValue());
        assertTrue(Arrays.stream(result).allMatch(
                        lrs -> Arrays.stream(lrs.getRequirements()).
                                filter(req -> req.getRequirementName().equals(Constants.RESOURCE_REQUIREMENT_NAME.INSTANCES_NUMBER.toString())).
                                noneMatch(LMLibraryRequirementFulfilledDTO::getFulfilled)
                )
        );
    }

    @Test
    public void getServiceGroupingNames(){
        String[] allServices = this.deploymentsApiService.getServiceGroupingNames("all");
        String[] deployedServices = this.deploymentsApiService.getServiceGroupingNames("deployed");
        String[] notDeployedsServices = this.deploymentsApiService.getServiceGroupingNames("not_deployed");

        List<String> expectedDeployedGroupingNames = List.of("API", "CDN", "DAEMON", "EPHOENIX_BATCH", "EPHOENIX_ONLINE", "FRONTCAT");
        List<String> expectedNotDeployedGroupingNames = List.of("BATCH", "BATCH_SCHEDULER", "DEPENDENCY", "LIBRARY", "BEHAVIOR_TEST", "INVALID");
        List<String> expectedAllGroupingNames = ListUtils.union(expectedDeployedGroupingNames, expectedNotDeployedGroupingNames);
        expectedAllGroupingNames.remove("INVALID");

        assertThat(expectedDeployedGroupingNames, containsInAnyOrder(deployedServices));
        assertThat(expectedNotDeployedGroupingNames, containsInAnyOrder(notDeployedsServices));
        assertThat(expectedAllGroupingNames, containsInAnyOrder(allServices));

        assertThrows(NovaException.class, () -> this.deploymentsApiService.getServiceGroupingNames("error_choise"));
    }

    @Test
    public void getSubsystemTypes()
    {
        String[] allSubsystemTypes = this.deploymentsApiService.getSubsystemTypes();
        List<String> expectedSubsystemType = List.of("NOVA", "EPHOENIX", "LIBRARY", "FRONTCAT", "BEHAVIOR_TEST");
        assertThat(expectedSubsystemType, containsInAnyOrder(allSubsystemTypes));
    }

    @Test
    public void getDeploymentStatus()
    {
        String[] allDeploymentStatus = this.deploymentsApiService.getDeploymentStatus();
        List<String> expectedDeploymentStatus = List.of("DEFINITION", "REJECTED", "DEPLOYED", "UNDEPLOYED", "SCHEDULED", "STORAGED");
        assertThat(expectedDeploymentStatus, containsInAnyOrder(allDeploymentStatus));
    }

    @Test
    public void getAllRequirementsOfUsedLibrariesForPlansEmptyIdsTest()
    {
        DeploymentPlanLibraryRequirementsDTO[] result =
                this.deploymentsApiService.getAllRequirementsOfUsedLibrariesForPlans(new int[0]);
        assertNotNull(result);
    }

    @Test
    public void getAllRequirementsOfUsedLibrariesForPlansNotConfiguredResources()
    {
        int[] deploymentPlanIds = {DEPLOYMENT_PLAN_ID_1, DEPLOYMENT_PLAN_ID_2, DEPLOYMENT_PLAN_ID_3};

        Set<Integer> deploymentPlanIdSet = new HashSet<>(Arrays.asList(
                DEPLOYMENT_PLAN_ID_1, DEPLOYMENT_PLAN_ID_2, DEPLOYMENT_PLAN_ID_3));
        List<DeploymentPlan> deploymentPlanList =
                createDeploymentPlanList(DEPLOYMENT_PLAN_ID_SIZE);

        when(this.deploymentPlanRepository.findByIdIn(deploymentPlanIdSet)).thenReturn(deploymentPlanList);

        LMLibraryRequirementsDTO[] lmLibraryRequirementsDTOS = createLMLibraryRequirementsDTO(
                DEPLOYMENT_SERVICE_ID_SIZE);
        when(this.libraryManagerService.getAllRequirementsOfUsedLibraries(any())).thenReturn(lmLibraryRequirementsDTOS);

        DeploymentPlanLibraryRequirementsDTO[] result =
                this.deploymentsApiService.getAllRequirementsOfUsedLibrariesForPlans(deploymentPlanIds);
        assertNotNull(result);
        assertEquals(DEPLOYMENT_SERVICE_ID_SIZE, result.length);

        assertEquals(DEPLOYMENT_PLAN_ID_1, result[0].getDeploymentPlanId().intValue());
        assertEquals(DEPLOYMENT_PLAN_ID_2, result[1].getDeploymentPlanId().intValue());
        assertEquals(DEPLOYMENT_PLAN_ID_3, result[2].getDeploymentPlanId().intValue());

        assertTrue(Arrays.stream(result).noneMatch(DeploymentPlanLibraryRequirementsDTO::getFulfilled));
        assertTrue(Arrays.stream(result).allMatch(
                        lrs -> Arrays.stream(lrs.getDeploymentRequirementsFulfilled()).noneMatch(LMLibraryRequirementsFulfilledDTO::getFulfilled)
                )
        );
        assertTrue(Arrays.stream(result).allMatch(
                        lrs -> (Arrays.stream(lrs.getDeploymentRequirementsFulfilled()).allMatch(
                                drf -> Arrays.stream(drf.getRequirements()).anyMatch(req -> !req.getFulfilled())
                        ))
                )
        );
    }

    @Test
    public void getAllRequirementsOfUsedLibrariesWithNumberFormatExceptions()
    {
        int[] deploymentServiceIds = {DEPLOYMENT_SERVICE_ID_1};
        Set<Integer> deploymentServiceIdSet = new HashSet<>(List.of(
                DEPLOYMENT_SERVICE_ID_1));
        List<DeploymentService> deploymentServiceList =
                createDeploymentServiceList(1, true, false, true, true);
        when(deploymentServiceRepository.findByIdIn(deploymentServiceIdSet)).thenReturn(deploymentServiceList);

        LMLibraryRequirementsDTO[] lmLibraryRequirementsDTOS = createLMLibraryRequirementsWithWithNumberFormatDTO();
        when(this.libraryManagerService.getAllRequirementsOfUsedLibraries(any())).thenReturn(lmLibraryRequirementsDTOS);

        LMLibraryRequirementsFulfilledDTO[] result = this.deploymentsApiService.getAllRequirementsOfUsedLibraries(deploymentServiceIds);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(DEPLOYMENT_SERVICE_ID_1, result[0].getDeploymentServiceId().intValue());
        assertTrue(Arrays.stream(result).noneMatch(LMLibraryRequirementsFulfilledDTO::getFulfilled));
        for (LMLibraryRequirementsFulfilledDTO currentResult : result)
        {
            assertTrue(Arrays.stream(currentResult.getRequirements()).
                    noneMatch(LMLibraryRequirementFulfilledDTO::getFulfilled));
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class MigratePlan
    {

        @Test
        @DisplayName("(MigratePlan) -> is the happy path working?")
        void migratePlan() throws Exception
        {
            // Given
            DeploymentPlan deploymentPlanMock = mock(DeploymentPlan.class);
            ReleaseVersion releaseVersionMock = mock(ReleaseVersion.class);
            Release releaseMock = mock(Release.class);
            Product productMock = mock(Product.class);
            DeploymentMigrationDto deploymentMigrationDtoMock = getDummyDeploymentMigrationDto();

            // And
            when(deploymentPlanMock.getEnvironment()).thenReturn(Environment.INT.getEnvironment());
            when(deploymentPlanMock.getReleaseVersion()).thenReturn(releaseVersionMock);
            when(releaseVersionMock.getRelease()).thenReturn(releaseMock);
            when(releaseMock.getProduct()).thenReturn(productMock);
            when(productMock.getId()).thenReturn(1);
            when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlanMock));
            doNothing().when(usersClient).checkHasPermission(anyString(), any(), anyString(), anyInt(), any());
            doNothing().when(deploymentsValidator).checkExistingPlansOfRV(anyInt(), any(Environment.class));
            doNothing().when(deploymentsValidator).validateSameCPDConfigOnPRO(any(DeploymentPlan.class));
            doNothing().when(libraryManagerService).checkPublishedLibrariesByDeploymentPlan(any(DeploymentPlan.class), anyString());
            when(deploymentsService.migratePlan(anyInt(), anyInt())).thenReturn(deploymentMigrationDtoMock);
            // Then
            DeploymentMigrationDto result = assertDoesNotThrow(() -> deploymentsApiService.migratePlan("XE00004", 10, 12));
            assertEquals(deploymentMigrationDtoMock.getPlanId(), result.getPlanId());
        }

        @Test
        @DisplayName("(MigratePlan) -> migrate a deployment plan checking if it can be copied")
        void migratePlanCheckingIfItCanBeCopied() throws Exception
        {
            // Given
            DeploymentPlan deploymentPlanMock = mock(DeploymentPlan.class);
            ReleaseVersion releaseVersionMock = mock(ReleaseVersion.class);
            Release releaseMock = mock(Release.class);
            Product productMock = mock(Product.class);
            DeploymentMigrationDto deploymentMigrationDtoMock = getDummyDeploymentMigrationDto();

            // And
            when(deploymentPlanMock.getEnvironment()).thenReturn(Environment.PRO.getEnvironment());
            when(deploymentPlanMock.getReleaseVersion()).thenReturn(releaseVersionMock);
            when(releaseVersionMock.getRelease()).thenReturn(releaseMock);
            when(releaseMock.getProduct()).thenReturn(productMock);
            when(productMock.getId()).thenReturn(1);
            when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlanMock));
            doNothing().when(usersClient).checkHasPermission(anyString(), any(), anyString(), anyInt(), any());
            doNothing().when(deploymentsValidator).checkExistingPlansOfRV(anyInt(), any(Environment.class));
            doNothing().when(libraryManagerService).checkPublishedLibrariesByDeploymentPlan(any(DeploymentPlan.class), anyString());
            when(deploymentsService.migratePlan(anyInt(), anyInt())).thenReturn(deploymentMigrationDtoMock);

            // Then
            var result = assertDoesNotThrow(() -> deploymentsApiService.migratePlan("XE00004", 10, 12).getPlanId());
            assertEquals(deploymentMigrationDtoMock.getPlanId(), result);
        }

        @Test
        @DisplayName("(MigratePlan) -> does migrate a plan with no same CPD configuration PRE-PRO work?")
        void migratePlanWithDifferentCPDConfiguration()
        {
            // given
            var ivUser = "XE00000";
            var deploymentId = 1;
            var versionId = 1;
            var deploymentPlan = mock(DeploymentPlan.class);
            var releaseVersion = mock(ReleaseVersion.class);
            var release = mock(Release.class);
            var product = mock(Product.class);

            // when
            when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan));
            when(deploymentPlan.getEnvironment()).thenReturn(Environment.PRE.getEnvironment());
            when(deploymentPlan.getReleaseVersion()).thenReturn(releaseVersion);
            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getProduct()).thenReturn(product);
            when(product.getId()).thenReturn(1);
            doThrow(NovaException.class).when(deploymentsValidator).validateSameCPDConfigOnPRO(any(DeploymentPlan.class));

            // then
            assertThrows(NovaException.class, () -> deploymentsApiService.migratePlan(ivUser, deploymentId, versionId));
            verify(usersClient, times(1)).checkHasPermission(anyString(),anyString(),anyString(),anyInt(),any(NovaException.class));
            verify(deploymentsValidator, times(1)).checkExistingPlansOfRV(anyInt(), any());
            verifyNoInteractions(libraryManagerService, deploymentsService);
        }

    }

    @Test
    public void when_get_plan_card_status_from_non_existing_plan_then_throw_warning_exception()
    {
        when(deploymentsValidator.validateAndGetDeploymentPlanDeployed(anyInt())).thenThrow(new NovaException(DeploymentError.getPlanNotFoundWarning(1)));

        try
        {
            deploymentsApiService.getDeploymentPlanCardStatuses(1, "A");
        }
        catch (NovaException e)
        {
            assertEquals(DeploymentConstants.DeployErrors.PLAN_NOT_FOUND, e.getErrorCode().getErrorCode());
            return;
        }

        fail("Unexpected end of test.");
    }

    @Test
    public void when_get_plan_card_status_from_not_deployed_plan_then_throw_warning_exception()
    {
        when(deploymentsValidator.validateAndGetDeploymentPlanDeployed(anyInt())).thenThrow(new NovaException(DeploymentError.getPlanDeployedNotFoundError(1)));
        try
        {
            deploymentsApiService.getDeploymentPlanCardStatuses(1, "A");
        }
        catch (NovaException e)
        {
            assertEquals(DeploymentConstants.DeployErrors.DEPLOYMENT_PLAN_DEPLOYED_NOT_FOUND, e.getErrorCode().getErrorCode());
            return;
        }

        fail("Unexpected end of test.");
    }

    @Test
    public void when_get_plan_card_status_from_ether_deployed_plan_then_return_resultWithApiRest()
    {
        when_get_plan_card_status_from_ether_deployed_plan_then_return_result(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void when_get_plan_card_status_from_ether_deployed_plan_then_return_resultWithApi()
    {
        when_get_plan_card_status_from_ether_deployed_plan_then_return_result(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void when_get_plan_card_status_from_ether_deployed_plan_then_return_result(ServiceType serviceType)
    {
        DeploymentPlan deploymentPlan = createDeploymentPlan(1);
        deploymentPlan.setSelectedDeploy(Platform.ETHER);
        deploymentPlan.setAction(DeploymentAction.READY);
        deploymentPlan.setExecutionDate(Calendar.getInstance());
        deploymentPlan.setEnvironment(Environment.INT.getEnvironment());

        when(deploymentsValidator.validateAndGetDeploymentPlanDeployed(anyInt())).thenReturn(deploymentPlan);
        when(etherManagerClient.getSubsystemStatus(any())).thenReturn(getDummyEtherSubsystemStatusDtos());
        DeployServiceServiceStatusDtoParams[] serviceParams = new DeployServiceServiceStatusDtoParams[]{
                new DeployServiceServiceStatusDtoParams(1, serviceType.getServiceType(), 3, 2)
        };
        List<DeploySubsystemStatusDTO> subsystemStatusDtos = List.of(getDummyDeploySubsystemStatusDto(serviceParams));
        when(deploymentUtils.buildEtherSubsystemDTO(any(DeploymentPlan.class))).thenReturn(getDummyEtherSubsystemDtos(serviceType));
        when(deploymentUtils.buildDeploySubsystemStatusDtoList(any())).thenReturn(subsystemStatusDtos);
        when(batchManagerService.getRunningInstancesByDeploymentPlan(anyInt(), anyBoolean())).thenReturn(CompletableFuture.completedFuture(0));
        DeploymentPlanCardStatusesDto result = deploymentsApiService.getDeploymentPlanCardStatuses(1, "A");

        assertFalse(result.getCouldBeRestarted());
        assertFalse(result.getCouldBeStarted());
        assertFalse(result.getCouldBeStopped());
        assertNotNull(result.getExecutionDate());
        assertEquals(DeploymentAction.READY.getAction(), result.getDeploymentPlanAction());
        DeploymentPlanCardServiceStatusDto[] actualStatusesDto = result.getDeploymentPlanServicesStatuses();
        assertEquals(7, actualStatusesDto.length);
        for (DeploymentPlanCardServiceStatusDto dto : actualStatusesDto)
        {
            int running = 0;
            int total = 0;
            if ("online".equals(dto.getServiceType()))
            {
                running = 2;
                total = 3;
            }
            assertEquals(running, dto.getRunning());
            assertEquals(total, dto.getTotal());
        }
    }

    private LMLibraryRequirementsDTO[] createLMLibraryRequirementsWithWithNumberFormatDTO()
    {
        List<LMLibraryRequirementsDTO> list = new ArrayList<>();
        LMLibraryRequirementsDTO lmLibraryRequirementsDTO = new LMLibraryRequirementsDTO();
        lmLibraryRequirementsDTO.setReleaseVersionServiceId(1);
        List<LMLibraryRequirementDTO> listReq = new ArrayList<>();

        LMLibraryRequirementDTO memoryReq = new LMLibraryRequirementDTO();
        memoryReq.setRequirementType(Constants.REQUIREMENT_TYPE.RESOURCE.name());
        memoryReq.setRequirementName(Constants.RESOURCE_REQUIREMENT_NAME.MEMORY.name());
        memoryReq.setRequirementValue("A");

        LMLibraryRequirementDTO jvmReq = new LMLibraryRequirementDTO();
        jvmReq.setRequirementType(Constants.REQUIREMENT_TYPE.RESOURCE.name());
        jvmReq.setRequirementName(Constants.RESOURCE_REQUIREMENT_NAME.JVM.name());
        jvmReq.setRequirementValue("B");

        LMLibraryRequirementDTO instancesNumberReq = new LMLibraryRequirementDTO();
        instancesNumberReq.setRequirementType(Constants.REQUIREMENT_TYPE.RESOURCE.name());
        instancesNumberReq.setRequirementName(Constants.RESOURCE_REQUIREMENT_NAME.INSTANCES_NUMBER.name());
        instancesNumberReq.setRequirementValue("C");

        listReq.add(memoryReq);
        listReq.add(jvmReq);
        listReq.add(instancesNumberReq);

        lmLibraryRequirementsDTO.setRequirements(listReq.toArray(new LMLibraryRequirementDTO[0]));
        list.add(lmLibraryRequirementsDTO);

        return list.toArray(new LMLibraryRequirementsDTO[0]);
    }

    private List<DeploymentPlan> createDeploymentPlanList(int size)
    {
        List<DeploymentPlan> list = new ArrayList<>();
        for (int count = 0; count < size; count++)
        {
            DeploymentPlan dp = createDeploymentPlan(DEPLOYMENT_PLAN_ID_1 + count);
            list.add(dp);
        }
        return list;
    }

    private DeploymentPlan createDeploymentPlan(int id)
    {
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(id);
        deploymentPlan.setDeploymentSubsystems(createDeploymentSubsystems(id * 1000));
        return deploymentPlan;
    }

    private List<DeploymentSubsystem> createDeploymentSubsystems(int deploymentServiceId)
    {
        List<DeploymentSubsystem> deploymentSubsystemList = new ArrayList<>();
        for (int i = 0; i < DEPLOYMENT_SUBSYSTEMS_SIZE; i++)
        {
            DeploymentSubsystem dss = new DeploymentSubsystem();
            dss.setId((i + 1) * deploymentServiceId);
            dss.setDeploymentServices(createDeploymentServiceList(DEPLOYMENT_SERVICE_ID_SIZE, false, false,
                    false, false));
            deploymentSubsystemList.add(dss);
        }
        return deploymentSubsystemList;
    }

    private LMLibraryRequirementsDTO[] createEmptyLMLibraryRequirementsDTO(int size)
    {
        List<LMLibraryRequirementsDTO> list = new ArrayList<>();
        for (int count = 0; count < size; count++)
        {
            LMLibraryRequirementsDTO lmLibraryRequirementsDTO = new LMLibraryRequirementsDTO();
            lmLibraryRequirementsDTO.setReleaseVersionServiceId(size + count);
            lmLibraryRequirementsDTO.setRequirements(new LMLibraryRequirementDTO[0]);
            list.add(lmLibraryRequirementsDTO);
        }
        return list.toArray(new LMLibraryRequirementsDTO[0]);
    }

    private List<DeploymentService> createDeploymentServiceList(int size, boolean configureResources, boolean isLibrary,
                                                                boolean fulfillInstances, boolean fulfilljvm)
    {
        List<DeploymentService> list = new ArrayList<>();
        Product p = new Product();
        p.setId(PRODUCT_ID);
        p.setName(PRODUCT_NAME);
        for (int count = 0; count < size; count++)
        {
            DeploymentService ds = new DeploymentService();
            ds.setId(count);

            ReleaseVersionService rvs = new ReleaseVersionService();
            rvs.setId(size + count);

            DeploymentSubsystem dss = new DeploymentSubsystem();
            dss.setId(size + count);

            DeploymentPlan dp = new DeploymentPlan();
            dp.setEnvironment(Environment.INT.getEnvironment());
            dp.setId(size + count);

            ReleaseVersion rv = new ReleaseVersion();
            rv.setId(size + count);

            Release r = new Release();
            r.setId(size + count);

            r.setProduct(p);
            rv.setRelease(r);
            dp.setReleaseVersion(rv);
            dss.setDeploymentPlan(dp);
            ds.setDeploymentSubsystem(dss);

            if (isLibrary)
            {
                rvs.setServiceType(ServiceType.LIBRARY_JAVA.getServiceType());
            }
            ds.setService(rvs);
            if (configureResources)
            {
                configureResourcesToDeploymentService(ds, fulfillInstances, fulfilljvm);
            }
            list.add(ds);
        }
        return list;
    }

    private void configureResourcesToDeploymentService(DeploymentService ds, boolean fulfillInstances, boolean fulfilljvm)
    {
        HardwarePack hw = new HardwarePack();
        hw.setNumCPU(2);
        hw.setRamMB(2048);
        ds.setHardwarePack(hw);
        ds.getDeploymentServiceFilesystems().add(new DeploymentServiceFilesystem());
        for (int i = 0; i < CONNECTOR_NUMBER; i++)
        {
            LogicalConnector lc = new LogicalConnector();
            ConnectorType ct = new ConnectorType();
            ct.setName(CONNECTOR_PREFIX + i);
            lc.setConnectorType(ct);
            ds.getLogicalConnectors().add(lc);
        }
        if (fulfillInstances)
        {
            ds.setNumberOfInstances(GOOD_MEMORY_FACTOR);
        }
        else
        {
            ds.setNumberOfInstances(BAD_INSTANCES_NUMBER);
        }
        if (!fulfilljvm)
        {
            ds.setMemoryFactor(BAD_MEMORY_FACTOR);
        }
    }

    private LMLibraryRequirementsDTO[] createLMLibraryRequirementsDTO(int size)
    {
        List<LMLibraryRequirementsDTO> list = new ArrayList<>();
        for (int count = 0; count < size; count++)
        {
            LMLibraryRequirementsDTO lmLibraryRequirementsDTO = new LMLibraryRequirementsDTO();
            lmLibraryRequirementsDTO.setReleaseVersionServiceId(size + count);

            List<LMLibraryRequirementDTO> listReq = new ArrayList<>();
            for (int countReq = 0; countReq < size * 10; countReq++)
            {
                listReq.add(createLMLibraryRequirementDTO());
            }
            lmLibraryRequirementsDTO.setRequirements(listReq.toArray(new LMLibraryRequirementDTO[0]));
            list.add(lmLibraryRequirementsDTO);
        }
        return list.toArray(new LMLibraryRequirementsDTO[0]);
    }

    private LMLibraryRequirementsDTO[] createLMLibraryRequirementsNamespaceDTO(int size)
    {
        List<LMLibraryRequirementsDTO> list = new ArrayList<>();
        for (int count = 0; count < size; count++)
        {
            LMLibraryRequirementsDTO lmLibraryRequirementsDTO = new LMLibraryRequirementsDTO();
            lmLibraryRequirementsDTO.setReleaseVersionServiceId(size + count);

            List<LMLibraryRequirementDTO> listReq = new ArrayList<>();
            for (int countReq = 0; countReq < size * 3; countReq++)
            {
                listReq.add(createLMLibraryRequirementNamespaceDTO());
            }
            lmLibraryRequirementsDTO.setRequirements(listReq.toArray(new LMLibraryRequirementDTO[0]));
            list.add(lmLibraryRequirementsDTO);
        }
        return list.toArray(new LMLibraryRequirementsDTO[0]);
    }

    private LMLibraryRequirementDTO createLMLibraryRequirementDTO()
    {
        LMLibraryRequirementDTO requirementDTO = new LMLibraryRequirementDTO();
        requirementDTO.setRequirementType(Constants.REQUIREMENT_TYPE.values()
                [new Random().nextInt(Constants.REQUIREMENT_TYPE.values().length)].name());
        switch (Constants.REQUIREMENT_TYPE.valueOf(requirementDTO.getRequirementType()))
        {
            case RESOURCE:
                return fillResource(requirementDTO);
            case INSTALLATION:
                return fillInstallation(requirementDTO);
            case ENVIRONMENT_VARIABLE:
                return fillEnvVar(requirementDTO);
        }
        requirementDTO.setRequirementDescription("30");
        return requirementDTO;
    }

    private LMLibraryRequirementDTO createLMLibraryRequirementNamespaceDTO()
    {
        LMLibraryRequirementDTO requirementDTO = new LMLibraryRequirementDTO();
        requirementDTO.setRequirementType(Constants.REQUIREMENT_TYPE.INSTALLATION.name());
        requirementDTO.setRequirementName(Constants.REQUIREMENT_NAME.NAMESPACE.name());
        requirementDTO.setRequirementValue("Y");
        requirementDTO.setRequirementDescription("30");
        return requirementDTO;
    }

    private LMLibraryRequirementDTO fillResource(LMLibraryRequirementDTO requirementDTO)
    {
        requirementDTO.setRequirementName(Constants.RESOURCE_REQUIREMENT_NAME.values()
                [new Random().nextInt(Constants.RESOURCE_REQUIREMENT_NAME.values().length)].name());
        switch (Constants.RESOURCE_REQUIREMENT_NAME.valueOf(requirementDTO.getRequirementName()))
        {
            case CONNECTORS:
                requirementDTO.setRequirementValue(CONNECTOR_PREFIX + new Random().nextInt(CONNECTOR_NUMBER));
                break;
            case CPU:
                requirementDTO.setRequirementValue(
                        new Random().nextInt(2) >= 1 ? "0.5" : "1.5"
                );
                break;
            case FILE_SYSTEM:
                requirementDTO.setRequirementValue("Y");
                break;
            case MEMORY:
                requirementDTO.setRequirementValue(
                        new Random().nextInt(2) >= 1 ? "512" : "1024"
                );
                break;
            case INSTANCES_NUMBER:
                requirementDTO.setRequirementValue(
                        new Random().nextInt(2) >= 1 ? "2" : "3"
                );
                break;
            case JVM:
                requirementDTO.setRequirementValue(
                        new Random().nextInt(2) >= 1 ? "50" : "75"
                );
                break;
        }

        return requirementDTO;
    }

    private LMLibraryRequirementDTO fillInstallation(LMLibraryRequirementDTO requirementDTO)
    {
        requirementDTO.setRequirementName(Constants.INSTALLATION_REQUIREMENT_NAME.values()
                [new Random().nextInt(Constants.INSTALLATION_REQUIREMENT_NAME.values().length)].name());
        requirementDTO.setRequirementValue("Y");
        return requirementDTO;
    }

    private LMLibraryRequirementDTO fillEnvVar(LMLibraryRequirementDTO requirementDTO)
    {
        requirementDTO.setRequirementName("MyEnvVarName3");
        requirementDTO.setRequirementValue("MyEnvVarValue10");
        return requirementDTO;
    }

    private DeploymentMigrationDto getDummyDeploymentMigrationDto()
    {
        DeploymentMigrationDto dto = new DeploymentMigrationDto();
        dto.setStatus("STATUS");
        dto.setPlanId(1);
        dto.setSubsystems(getDummyDeploymentMigrationSubsystemDtos());
        return dto;
    }

    private DeploymentMigrationSubsystemDto[] getDummyDeploymentMigrationSubsystemDtos()
    {
        DeploymentMigrationSubsystemDto dto = new DeploymentMigrationSubsystemDto();
        dto.setSubsystemName("SUBSYSTEM_NAME");
        dto.setStatus("STATUS");
        dto.setSubsystemId(1);
        dto.setServices(getDummyDeploymentMigrationServiceDtos());
        return new DeploymentMigrationSubsystemDto[]{dto};
    }

    private DeploymentMigrationServiceDto[] getDummyDeploymentMigrationServiceDtos()
    {
        DeploymentMigrationServiceDto dto = new DeploymentMigrationServiceDto();
        dto.setId(1);
        dto.setArtifactId("ARTIFACT_ID");
        dto.setStatus("STATUS");
        dto.setServiceName("SERVICE_NAME");
        dto.setFilesystemStatus("FILESYSTEM_STATUS");
        dto.setGroupId("GROUP_ID");
        dto.setHardwareStatus("HARDWARE_STATUS");
        dto.setLogicalConnectorStatus("LOGICAL_CONNECTOR_STATUS");
        dto.setMismatchedPropertiesList(new String[]{"A", "B", "C"});
        dto.setOriginalReleaseVersionServiceId(1);
        dto.setTargetReleaseVersionServiceId(2);
        dto.setVersion("VERSION");
        return new DeploymentMigrationServiceDto[]{dto};
    }

    private EtherSubsystemDTO[] getDummyEtherSubsystemDtos(ServiceType serviceType)
    {
        EtherSubsystemDTO dto = new EtherSubsystemDTO();
        dto.setEnvironment("INT");
        dto.setSubsystemId(1);
        dto.setNamespace("NAMESPACE");
        dto.setProductName("PRODUCT_NAME");
        dto.setReleaseName("RELEASE_NAME");
        dto.setReleaseVersionName("RELEASE_VERSION_NAME");
        dto.setUuaa("UUAA");
        dto.setEtherServices(getDummyEtherServiceDtos(serviceType));
        return new EtherSubsystemDTO[]{dto};
    }

    private EtherServiceDTO[] getDummyEtherServiceDtos(ServiceType serviceType)
    {
        EtherServiceDTO dto = new EtherServiceDTO();
        dto.setServiceType(serviceType.getServiceType());
        dto.setAction("ACTION");
        dto.setArtifactId("ARTIFACT_ID");
        dto.setDeploymentServiceId(1);
        dto.setServiceId(1);
        dto.setDockerKey("DOCKER_KEY");
        dto.setDomainSnippet("DOMAIN_SNIPPET");
        dto.setFilesystems(getDummyFileSystemAttachDtos());
        dto.setGroupId("GROUP_ID");
        dto.setHardware(getDummyHardwareDto());
        dto.setJvmParameters("JVM_PARAMETERS");
        dto.setNumInstances(3);
        dto.setParameters("PARAMETERS");
        dto.setProtocol("PROTOCOL");
        dto.setServiceName("SERVICE_NAME");
        dto.setStatus("STATUS");
        dto.setTaskExecutionId(1L);
        dto.setVersion("VERSION");
        return new EtherServiceDTO[]{dto};
    }

    private FileSystemAttachDTO[] getDummyFileSystemAttachDtos()
    {
        FileSystemAttachDTO dto = new FileSystemAttachDTO();
        dto.setVolumeBind("VOLUME_BIND");
        dto.setVolumeId("VOLUME_ID");
        return new FileSystemAttachDTO[]{dto};
    }

    private HardwareDTO getDummyHardwareDto()
    {
        HardwareDTO dto = new HardwareDTO();
        dto.setInstance("INSTANCE");
        dto.setMemoryFactor(4);
        dto.setRamMB(1024);
        return dto;
    }

    private EtherSubsystemStatusDTO[] getDummyEtherSubsystemStatusDtos()
    {
        EtherSubsystemStatusDTO dto = new EtherSubsystemStatusDTO();
        dto.setSubsystemId(1);
        dto.setStatus(getDummyEtherStatusDto());
        dto.setServices(getDummyEtherServiceStatusDtos());
        return new EtherSubsystemStatusDTO[]{dto};
    }

    private EtherServiceStatusDTO[] getDummyEtherServiceStatusDtos()
    {
        EtherServiceStatusDTO dto = new EtherServiceStatusDTO();
        dto.setServiceId(1);
        dto.setStatus(getDummyEtherStatusDto());
        return new EtherServiceStatusDTO[]{dto};
    }

    private EtherStatusDTO getDummyEtherStatusDto()
    {
        EtherStatusDTO dto = new EtherStatusDTO();
        dto.setRunningContainers(2);
        dto.setTotalContainers(3);
        return dto;
    }

    private DeploySubsystemStatusDTO getDummyDeploySubsystemStatusDto(DeployServiceServiceStatusDtoParams[] params)
    {
        DeploySubsystemStatusDTO dto = new DeploySubsystemStatusDTO();
        dto.setSubsystemId(1);
        int total = 0;
        int running = 0;
        int numberOfServices = params.length;
        DeployServiceStatusDTO[] services = new DeployServiceStatusDTO[numberOfServices];
        for (int i = 0; i < numberOfServices; i++)
        {
            DeployServiceServiceStatusDtoParams param = params[i];
            DeployServiceStatusDTO service = getDummyDeployServiceStatusDto(param);
            DeployStatusDTO serviceStatus = service.getStatus();
            total += serviceStatus.getTotal();
            running += serviceStatus.getRunning();
            services[i] = service;
        }
        dto.setServices(services);
        dto.setStatus(getDummyDeployStatusDto(total, running));
        return dto;
    }

    private DeployServiceStatusDTO getDummyDeployServiceStatusDto(DeployServiceServiceStatusDtoParams params)
    {
        DeployServiceStatusDTO dto = new DeployServiceStatusDTO();
        dto.setServiceType(params.getServiceType());
        dto.setServiceId(params.getServiceId());
        dto.setServiceName("SERVICE_NAME_" + dto.getServiceId());
        int totalInstances = params.getTotal();
        dto.setStatus(getDummyDeployStatusDto(totalInstances, params.getRunning()));
        DeployInstanceStatusDTO[] instances = new DeployInstanceStatusDTO[totalInstances];
        for (int i = 0; i < totalInstances; i++)
        {
            instances[i] = getDummyDeployInstanceStatusDto(i + 1);
        }
        dto.setInstances(instances);
        return dto;
    }

    private DeployInstanceStatusDTO getDummyDeployInstanceStatusDto(int instanceId)
    {
        DeployInstanceStatusDTO dto = new DeployInstanceStatusDTO();
        dto.setInstanceId(instanceId);
        return dto;
    }

    private DeployStatusDTO getDummyDeployStatusDto(int total, int running)
    {
        DeployStatusDTO dto = new DeployStatusDTO();
        dto.setTotal(total);
        dto.setRunning(running);
        dto.setExited(total - running);
        return dto;
    }

    private List<CPD> getCPDSwarmTrueList()
    {
        List<CPD> cpds = new ArrayList<>();
        CPD cpd = new CPD();
        cpd.setActive(true);

        cpds.add(cpd);

        return cpds;
    }

    private List<CPD> getCPDSwarmFalseList()
    {
        List<CPD> cpds = new ArrayList<>();
        CPD cpd = new CPD();
        cpd.setActive(false);

        cpds.add(cpd);

        return cpds;
    }

    @Getter
    private static class DeployServiceServiceStatusDtoParams
    {
        private int serviceId;
        private String serviceType;
        private int total;
        private int running;

        public DeployServiceServiceStatusDtoParams(int serviceId, String serviceType, int total, int running)
        {
            this.serviceId = serviceId;
            this.serviceType = serviceType;
            this.total = total;
            this.running = running;
        }
    }
}
