package com.bbva.enoa.platformservices.coreservice.apimanagerapi.services;

import com.bbva.enoa.apirestgen.apimanagerapi.model.*;
import com.bbva.enoa.datamodel.model.api.entities.*;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiState;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.profile.entities.ApiMethodProfile;
import com.bbva.enoa.datamodel.model.profile.entities.CesRole;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder.IDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal.IApiManagerServiceModalityBased;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.IApiManagerValidator;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.common.util.ProfilingUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl.RepositoryManagerServiceImpl;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.PlanProfilingUtils;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiManagerServiceImplTest
{
    @Mock
    private IApiManagerServiceModalityBased apiManagerServiceModalityBased;
    @Mock
    private IDtoBuilder dtoBuilder;
    @Mock
    private IApiManagerValidator apiManagerValidator;
    @Mock
    private RepositoryManagerServiceImpl repositoryManagerService;
    @Mock
    private PlanProfileRepository planProfileRepository;
    @Mock
    private ApiMethodRepository apiMethodRepository;
    @Mock
    private ApiMethodProfileRespository apiMethodProfileRespository;
    @Mock
    private CesRoleRepository cesRoleRepository;
    @Mock
    private ApiRepository apiRepository;
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private ApiVersionRepository apiVersionRepository;
    @Mock
    private ApiImplementationRepository apiImplementationRepository;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @Mock
    private ProfilingUtils profilingUtils;
    @Mock
    private PlanProfilingUtils planProfilingUtils;
    @InjectMocks
    private ApiManagerServiceImpl apiManagerService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                apiManagerServiceModalityBased,
                dtoBuilder,
                apiManagerValidator,
                repositoryManagerService,
                planProfileRepository,
                apiMethodRepository,
                apiMethodProfileRespository,
                cesRoleRepository,
                apiRepository,
                deploymentPlanRepository,
                apiVersionRepository,
                apiImplementationRepository,
                novaActivityEmitter,
                profilingUtils,
                planProfilingUtils
        );
    }

    @Nested
    class GetProductApis
    {
        @Test
        @DisplayName("Get product APIs -> null arguments")
        public void nullArgs()
        {
            when(dtoBuilder.buildApiDtoArray(any())).thenReturn(new ApiDto[0]);

            ApiDto[] retValue = apiManagerService.getProductApis(null);

            verify(dtoBuilder).buildApiDtoArray(null);
            assertEquals(0, retValue.length);
        }

        @Test
        @DisplayName("Get product APIs -> no APIs found for product")
        public void emptyResult()
        {
            Integer productId = 909;
            when(dtoBuilder.buildApiDtoArray(any())).thenReturn(new ApiDto[0]);

            ApiDto[] retValue = apiManagerService.getProductApis(productId);

            verify(dtoBuilder).buildApiDtoArray(productId);
            assertEquals(0, retValue.length);
        }

        @Test
        @DisplayName("Get product APIs -> OK")
        public void ok()
        {
            Integer productId = 909;
            when(dtoBuilder.buildApiDtoArray(any())).thenReturn(new ApiDto[]{new ApiDto()});

            ApiDto[] retValue = apiManagerService.getProductApis(productId);

            verify(dtoBuilder).buildApiDtoArray(productId);
            assertEquals(1, retValue.length);
        }
    }

    @Nested
    class UploadProductApis
    {
        private final Integer productId = 2408;

        @Nested
        class InputsError
        {
            @DisplayName("Upload product APIs -> api type error")
            @ParameterizedTest(name = "[{index}] type -> {0}")
            @NullAndEmptySource
            @ValueSource(strings = "xxxx")
            public void apiTypeError(String apiType)
            {
                ApiUploadRequestDto apiUploadRequestDto = new ApiUploadRequestDto();
                apiUploadRequestDto.setApiType(apiType);
                apiUploadRequestDto.setApiModality(ApiModality.SYNC.getModality());

                assertThrows(
                        NovaException.class,
                        () -> apiManagerService.uploadProductApis(apiUploadRequestDto, productId)
                );
            }

            @DisplayName("Upload product APIs -> api modality error")
            @ParameterizedTest(name = "[{index}] modality -> {0}")
            @NullAndEmptySource
            @ValueSource(strings = "xxxx")
            public void apiModalityError(String apiModality)
            {
                ApiUploadRequestDto apiUploadRequestDto = new ApiUploadRequestDto();
                apiUploadRequestDto.setApiType(ApiType.GOVERNED.getApiType());
                apiUploadRequestDto.setApiModality(apiModality);

                assertThrows(
                        NovaException.class,
                        () -> apiManagerService.uploadProductApis(apiUploadRequestDto, productId)
                );
            }
        }

        @ParameterizedTest(name = "[{index}] type-> {0}, modality -> {1}")
        @ArgumentsSource(UploadProductApisTypeModalityCartesianProductArgumentsSource.class)
        @DisplayName("Upload product APIs -> permission error")
        public void permissionError(ApiType apiType, ApiModality modality)
        {
            ApiUploadRequestDto apiUploadRequestDto = new ApiUploadRequestDto();
            apiUploadRequestDto.setApiType(apiType.getApiType());
            apiUploadRequestDto.setApiModality(modality.getModality());
            ApiErrorList apiErrorList = new ApiErrorList();
            apiErrorList.fillRandomly(4, false, 1, 3);

            doThrow(NovaException.class).when(apiManagerValidator).checkUploadApiPermission(any(), anyInt());

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.uploadProductApis(apiUploadRequestDto, productId)
            );

            verify(apiManagerValidator).checkUploadApiPermission(apiType, productId);
        }

        @ParameterizedTest(name = "[{index}] type-> {0}, modality -> {1}")
        @ArgumentsSource(UploadProductApisTypeModalityCartesianProductArgumentsSource.class)
        @DisplayName("Upload product APIs -> dataIntegrityDatabaseError")
        public void dataIntegrityDatabaseError(ApiType apiType, ApiModality modality)
        {
            ApiUploadRequestDto apiUploadRequestDto = new ApiUploadRequestDto();
            apiUploadRequestDto.setApiType(apiType.getApiType());
            apiUploadRequestDto.setApiModality(modality.getModality());

            when(apiManagerServiceModalityBased.uploadApi(any(), anyInt())).thenThrow(DataIntegrityViolationException.class);

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.uploadProductApis(apiUploadRequestDto, productId));

            verify(apiManagerValidator).checkUploadApiPermission(apiType, productId);
            verify(apiManagerServiceModalityBased, times(1)).uploadApi(apiUploadRequestDto, productId);
        }

        @ParameterizedTest(name = "[{index}] type-> {0}, modality -> {1}")
        @ArgumentsSource(UploadProductApisTypeModalityCartesianProductArgumentsSource.class)
        @DisplayName("Upload product APIs -> OK")
        public void ok(ApiType apiType, ApiModality modality)
        {
            ApiUploadRequestDto apiUploadRequestDto = new ApiUploadRequestDto();
            apiUploadRequestDto.setApiType(apiType.getApiType());
            apiUploadRequestDto.setApiModality(modality.getModality());

            ApiErrorList apiErrorList = new ApiErrorList();
            apiErrorList.fillRandomly(4, false, 1, 3);

            when(apiManagerServiceModalityBased.uploadApi(any(), anyInt())).thenReturn(apiErrorList);

            ApiErrorList retValue = apiManagerService.uploadProductApis(apiUploadRequestDto, productId);

            verify(apiManagerValidator).checkUploadApiPermission(apiType, productId);
            verify(apiManagerServiceModalityBased).uploadApi(apiUploadRequestDto, productId);
            assertEquals(apiErrorList, retValue);
        }
    }

    @Nested
    class DeleteProductApi
    {
        private final Integer productId = 2408;
        private final Integer apiVersionId = 909;

        @Test
        @DisplayName("Delete product APIs -> input error")
        public void inputError()
        {
            when(apiManagerValidator.checkApiVersionExistence(anyInt(), anyInt())).thenThrow(NovaException.class);

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.deleteProductApi(productId, apiVersionId)
            );
            verify(apiManagerValidator).checkApiVersionExistence(productId, apiVersionId);
        }

        @Test
        @DisplayName("Delete product APIs -> permission error")
        public void permissionError()
        {
            SyncApiVersion apiVersion = new SyncApiVersion();
            SyncApi api = new SyncApi();
            api.setType(ApiType.GOVERNED);
            apiVersion.setApi(api);
            when(apiManagerValidator.checkApiVersionExistence(anyInt(), anyInt())).then(i -> apiVersion);
            doThrow(NovaException.class).when(apiManagerValidator).checkDeleteApiPermission(any(), anyInt());

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.deleteProductApi(productId, apiVersionId)
            );
            verify(apiManagerValidator).checkApiVersionExistence(productId, apiVersionId);
            verify(apiManagerValidator).checkDeleteApiPermission(ApiType.GOVERNED, productId);
        }

        @Test
        @DisplayName("Delete product APIs -> delete errors")
        public void notDeleteableError()
        {
            SyncApiVersion apiVersion = new SyncApiVersion();
            SyncApi api = new SyncApi();
            api.setType(ApiType.GOVERNED);
            apiVersion.setApi(api);
            ApiErrorList apiErrorList = new ApiErrorList();
            apiErrorList.setErrorList(new String[]{"Error"});
            when(apiManagerValidator.checkApiVersionExistence(anyInt(), anyInt())).then(i -> apiVersion);
            when(apiManagerValidator.isApiVersionErasable(any())).thenReturn(apiErrorList);

            ApiErrorList ret = apiManagerService.deleteProductApi(productId, apiVersionId);

            verify(apiManagerValidator).checkApiVersionExistence(productId, apiVersionId);
            verify(apiManagerValidator).checkDeleteApiPermission(ApiType.GOVERNED, productId);
            verify(apiManagerValidator).isApiVersionErasable(apiVersion);
            assertEquals(apiErrorList, ret);
        }

        @Nested
        class OK
        {
            @ParameterizedTest(name = "[{index}] apiType {0}")
            @DisplayName("Delete last product APIs -> OK")
            @EnumSource(ApiType.class)
            public void lastApiVersionOk(ApiType apiType) throws JsonProcessingException
            {
                SyncApiVersion apiVersion = new SyncApiVersion();
                apiVersion.setId(apiVersionId);
                SyncApi api = new SyncApi();
                api.setType(apiType);
                api.setName("Daniela");
                apiVersion.setApi(api);
                api.getApiVersions().add(apiVersion);
                ApiErrorList apiErrorList = new ApiErrorList();
                apiErrorList.setErrorList(new String[]{});

                when(apiManagerValidator.checkApiVersionExistence(anyInt(), anyInt())).then(i -> apiVersion);
                when(apiManagerValidator.isApiVersionErasable(any())).thenReturn(apiErrorList);

                ApiErrorList retValue = apiManagerService.deleteProductApi(productId, apiVersionId);

                ArgumentCaptor<GenericActivity> genericActivityArgumentCaptor = ArgumentCaptor.forClass(GenericActivity.class);
                verify(apiManagerValidator).checkApiVersionExistence(productId, apiVersionId);
                verify(apiManagerValidator).checkDeleteApiPermission(apiType, productId);
                verify(apiManagerValidator).isApiVersionErasable(apiVersion);
                if (ApiType.EXTERNAL != api.getType())
                {
                    verify(apiManagerServiceModalityBased).removeApiRegistration(api);
                }
                verify(apiManagerServiceModalityBased).deleteApiTodoTasks(api);
                verify(apiRepository).delete(api);
                verify(novaActivityEmitter).emitNewActivity(genericActivityArgumentCaptor.capture());
                assertEquals(apiErrorList, retValue);
                GenericActivity genericActivityCaptured = genericActivityArgumentCaptor.getValue();
                TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>()
                {
                };
                Map<String, Object> params = new ObjectMapper().readValue(genericActivityCaptured.getSerializedStringParams(), typeRef);
                assertEquals(productId, genericActivityCaptured.getProductId());
                assertEquals(ActivityAction.ELIMINATED, genericActivityCaptured.getAction());
                assertEquals(apiVersionId, genericActivityCaptured.getEntityId());
                assertEquals(1, params.size());
                assertEquals(api.getName(), params.get("apiName"));
                switch (api.getType())
                {
                    case NOT_GOVERNED:
                        assertEquals(ActivityScope.UNGOVERNED_API, genericActivityCaptured.getScope());
                        break;
                    case GOVERNED:
                        assertEquals(ActivityScope.GOVERNED_API, genericActivityCaptured.getScope());
                        break;
                    case EXTERNAL:
                        assertEquals(ActivityScope.THIRD_PARTY_API, genericActivityCaptured.getScope());
                        break;
                }
            }

            @ParameterizedTest(name = "[{index}] apiType {0}")
            @DisplayName("Delete not last product APIs -> OK")
            @EnumSource(ApiType.class)
            public void notLastApiVersionOk(ApiType apiType) throws JsonProcessingException
            {
                SyncApiVersion apiVersion = new SyncApiVersion();
                SyncApiVersion otherApiVersion = new SyncApiVersion();
                apiVersion.setId(apiVersionId);
                SyncApi api = new SyncApi();
                api.setType(apiType);
                api.setName("Daniela");
                apiVersion.setApi(api);
                api.getApiVersions().add(apiVersion);
                api.getApiVersions().add(otherApiVersion);
                ApiErrorList apiErrorList = new ApiErrorList();
                apiErrorList.setErrorList(new String[]{});

                when(apiManagerValidator.checkApiVersionExistence(anyInt(), anyInt())).then(i -> apiVersion);
                when(apiManagerValidator.isApiVersionErasable(any())).thenReturn(apiErrorList);

                ApiErrorList retValue = apiManagerService.deleteProductApi(productId, apiVersionId);

                ArgumentCaptor<GenericActivity> genericActivityArgumentCaptor = ArgumentCaptor.forClass(GenericActivity.class);
                verify(apiManagerValidator).checkApiVersionExistence(productId, apiVersionId);
                verify(apiManagerValidator).checkDeleteApiPermission(apiType, productId);
                verify(apiManagerValidator).isApiVersionErasable(apiVersion);
                verify(novaActivityEmitter).emitNewActivity(genericActivityArgumentCaptor.capture());
                assertEquals(apiErrorList, retValue);
                assertEquals(1, api.getApiVersions().size());
                assertEquals(otherApiVersion, api.getApiVersions().get(0));
                GenericActivity genericActivityCaptured = genericActivityArgumentCaptor.getValue();
                TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>()
                {
                };
                Map<String, Object> params = new ObjectMapper().readValue(genericActivityCaptured.getSerializedStringParams(), typeRef);
                assertEquals(productId, genericActivityCaptured.getProductId());
                assertEquals(ActivityAction.ELIMINATED, genericActivityCaptured.getAction());
                assertEquals(apiVersionId, genericActivityCaptured.getEntityId());
                assertEquals(1, params.size());
                assertEquals(api.getName(), params.get("apiName"));
                switch (api.getType())
                {
                    case NOT_GOVERNED:
                        assertEquals(ActivityScope.UNGOVERNED_API, genericActivityCaptured.getScope());
                        break;
                    case GOVERNED:
                        assertEquals(ActivityScope.GOVERNED_API, genericActivityCaptured.getScope());
                        break;
                    case EXTERNAL:
                        assertEquals(ActivityScope.THIRD_PARTY_API, genericActivityCaptured.getScope());
                        break;
                }
            }
        }


    }

    @Nested
    class DownloadProductApi
    {
        Integer productId = 2408;
        Integer apiVersionId = 909;
        String format = "format";
        String downloadType = "downloadType";

        @Test
        @DisplayName("Download product API -> API version does not exist error")
        public void apiVersionExistenceError()
        {
            when(apiManagerValidator.checkApiVersionExistence(anyInt(), anyInt())).thenThrow(NovaException.class);

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.downloadProductApi(productId, apiVersionId, format, downloadType)
            );
            verify(apiManagerValidator).checkApiVersionExistence(productId, apiVersionId);
        }

        @Test
        @DisplayName("Download product API -> OK")
        public void ok()
        {
            SyncApiVersion apiVersion = new SyncApiVersion();
            byte[] expected = new byte[]{2, 3, 4};

            when(apiManagerValidator.checkApiVersionExistence(anyInt(), anyInt())).then(i -> apiVersion);
            when(apiManagerServiceModalityBased.downloadProductApi(any(), any(), any())).thenReturn(expected);

            byte[] retValue = apiManagerService.downloadProductApi(productId, apiVersionId, format, downloadType);
            verify(apiManagerValidator).checkApiVersionExistence(productId, apiVersionId);
            verify(apiManagerServiceModalityBased).downloadProductApi(apiVersion, format, downloadType);
            assertArrayEquals(expected, retValue);
        }
    }

    @Nested
    class CreateApiTask
    {
        @Test
        @DisplayName("Create API task -> taskDTO parameters error")
        public void taskDtoParametersError()
        {
            TaskInfoDto taskInfoDto = this.buildTaskInfoDto();
            doThrow(NovaException.class).when(apiManagerValidator).checkTaskDtoParameters(any());

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.createApiTask(taskInfoDto)
            );

            verify(apiManagerValidator).checkTaskDtoParameters(taskInfoDto);
        }

        @Test
        @DisplayName("Create API task -> permission error")
        public void permissionError()
        {
            TaskInfoDto taskInfoDto = this.buildTaskInfoDto();
            doThrow(NovaException.class).when(apiManagerValidator).checkCreatePolicyTaskPermission(any());

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.createApiTask(taskInfoDto)
            );

            verify(apiManagerValidator).checkTaskDtoParameters(taskInfoDto);
            verify(apiManagerValidator).checkCreatePolicyTaskPermission(taskInfoDto);
        }

        @Test
        @DisplayName("Create API task -> API existence error")
        public void apiExistenceError()
        {
            TaskInfoDto taskInfoDto = this.buildTaskInfoDto();
            when(apiManagerValidator.checkApiExistence(anyInt(), anyInt())).thenThrow(NovaException.class);

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.createApiTask(taskInfoDto)
            );

            verify(apiManagerValidator).checkTaskDtoParameters(taskInfoDto);
            verify(apiManagerValidator).checkCreatePolicyTaskPermission(taskInfoDto);
            verify(apiManagerValidator).checkApiExistence(taskInfoDto.getNovaApiId(), taskInfoDto.getProductId());
        }

        @Test
        @DisplayName("Create API task -> OK")
        public void ok()
        {
            TaskInfoDto taskInfoDto = this.buildTaskInfoDto();
            Api<?, ?, ?> api = this.buildApi();

            when(apiManagerValidator.checkApiExistence(anyInt(), anyInt())).then(i -> api);

            apiManagerService.createApiTask(taskInfoDto);

            verify(apiManagerValidator).checkTaskDtoParameters(taskInfoDto);
            verify(apiManagerValidator).checkCreatePolicyTaskPermission(taskInfoDto);
            verify(apiManagerValidator).checkApiExistence(taskInfoDto.getNovaApiId(), taskInfoDto.getProductId());
            verify(apiManagerServiceModalityBased).createApiTask(taskInfoDto, api);
        }

        private TaskInfoDto buildTaskInfoDto()
        {
            TaskInfoDto taskInfoDto = new TaskInfoDto();
            taskInfoDto.fillRandomly(4, false, 1, 3);
            return taskInfoDto;
        }

        private Api<?, ?, ?> buildApi()
        {
            SyncApi syncApi = new SyncApi();
            syncApi.setId(610);
            return syncApi;
        }

    }

    @Nested
    class OnPolicyTaskReply
    {
        Integer productId = 2408;
        String apiName = "Daniela";
        String uuaa = "JGMV";

        @DisplayName("On policy task reply -> task status error")
        @ParameterizedTest(name = "[{index}] type -> {0}")
        @NullAndEmptySource
        @ValueSource(strings = "xxxx")
        public void taskStatusError(String taskStatus)
        {
            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.onPolicyTaskReply(getDummyPolicyTaskReplyParametersDto(taskStatus, apiName, uuaa, productId))
            );
        }

        @DisplayName("On policy task reply -> API not found error")
        @ParameterizedTest(name = "[{index}] taskStatus -> {0}")
        @EnumSource(value = ToDoTaskStatus.class)
        public void apiNotFoundError(ToDoTaskStatus taskStatus)
        {
            when(apiRepository.findByProductIdAndNameAndUuaa(anyInt(), any(), any())).thenReturn(null);

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.onPolicyTaskReply(getDummyPolicyTaskReplyParametersDto(taskStatus.name(), apiName, uuaa, productId))
            );

            verify(apiRepository).findByProductIdAndNameAndUuaa(productId, apiName, uuaa);
        }

        @DisplayName("On policy task reply -> OK")
        @ParameterizedTest(name = "[{index}] taskStatus -> {0}")
        @EnumSource(value = ToDoTaskStatus.class)
        public void ok(ToDoTaskStatus taskStatus)
        {
            Api<?, ?, ?> api = this.buildApi();
            when(apiRepository.findByProductIdAndNameAndUuaa(anyInt(), any(), any())).then(i -> api);

            apiManagerService.onPolicyTaskReply(getDummyPolicyTaskReplyParametersDto(taskStatus.name(), apiName, uuaa, productId));

            verify(apiRepository).findByProductIdAndNameAndUuaa(productId, apiName, uuaa);
            verify(apiRepository, times(1)).save(any(Api.class));
            verify(apiManagerServiceModalityBased).onPolicyTaskReply(any(ToDoTaskStatus.class), any(Api.class));
        }

        private Api<?, ?, ?> buildApi()
        {
            SyncApi syncApi = new SyncApi();
            syncApi.setId(610);
            return syncApi;
        }
    }

    @Nested
    class SavePlanApiProfile
    {
        private final Integer planId = 909;

        @Test
        @DisplayName("Save plan API profile -> no deployment plan found")
        public void noDeploymentPlan()
        {
            when(repositoryManagerService.findPlan(anyInt())).thenReturn(null);

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.savePlanApiProfile(new ApiMethodProfileDto[]{new ApiMethodProfileDto()}, planId)
            );

            verify(repositoryManagerService).findPlan(planId);
        }

        @Test
        @DisplayName("Save plan API profile -> no plan profile found")
        public void noPlanProfile()
        {
            DeploymentPlan deploymentPlan = new DeploymentPlan();

            when(repositoryManagerService.findPlan(anyInt())).thenReturn(deploymentPlan);
            when(planProfileRepository.findByDeploymentPlan(any())).thenReturn(null);

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.savePlanApiProfile(new ApiMethodProfileDto[]{new ApiMethodProfileDto()}, planId)
            );

            verify(repositoryManagerService).findPlan(planId);
            verify(planProfileRepository).findByDeploymentPlan(deploymentPlan);
        }

        @Test
        @DisplayName("Save plan API profile -> no method found")
        public void noMethod()
        {
            ApiMethodProfileDto apiMethodProfileDto = new ApiMethodProfileDto();
            apiMethodProfileDto.fillRandomly(4, false, 1, 3);
            DeploymentPlan deploymentPlan = new DeploymentPlan();
            PlanProfile planProfile = new PlanProfile();

            when(repositoryManagerService.findPlan(anyInt())).thenReturn(deploymentPlan);
            when(planProfileRepository.findByDeploymentPlan(any())).thenReturn(planProfile);
            when(apiMethodRepository.findById(anyInt())).thenReturn(Optional.empty());

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.savePlanApiProfile(new ApiMethodProfileDto[]{apiMethodProfileDto}, planId)
            );

            verify(repositoryManagerService).findPlan(planId);
            verify(planProfileRepository).findByDeploymentPlan(deploymentPlan);
            verify(apiMethodRepository).findById(apiMethodProfileDto.getMethodId());
        }

        @Test
        @DisplayName("Save plan API profile -> no method profile found")
        public void noMethodProfile()
        {
            ApiMethodProfileDto apiMethodProfileDto = new ApiMethodProfileDto();
            apiMethodProfileDto.fillRandomly(4, false, 1, 3);
            DeploymentPlan deploymentPlan = new DeploymentPlan();
            PlanProfile planProfile = new PlanProfile();
            ApiMethod apiMethod = new ApiMethod();

            when(repositoryManagerService.findPlan(anyInt())).thenReturn(deploymentPlan);
            when(planProfileRepository.findByDeploymentPlan(any())).thenReturn(planProfile);
            when(apiMethodRepository.findById(anyInt())).thenReturn(Optional.of(apiMethod));
            when(apiMethodProfileRespository.findByPlanProfileAndApiMethod(any(), any())).thenReturn(null);

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.savePlanApiProfile(new ApiMethodProfileDto[]{apiMethodProfileDto}, planId)
            );

            verify(repositoryManagerService).findPlan(planId);
            verify(planProfileRepository).findByDeploymentPlan(deploymentPlan);
            verify(apiMethodRepository).findById(apiMethodProfileDto.getMethodId());
            verify(apiMethodProfileRespository).findByPlanProfileAndApiMethod(planProfile, apiMethod);
        }

        @Test
        @DisplayName("Save plan API profile -> no CES role found")
        public void noCESRole()
        {
            ApiMethodProfileDto apiMethodProfileDto = new ApiMethodProfileDto();
            apiMethodProfileDto.fillRandomly(4, false, 1, 3);
            DeploymentPlan deploymentPlan = new DeploymentPlan();
            PlanProfile planProfile = new PlanProfile();
            ApiMethod apiMethod = new ApiMethod();
            ApiMethodProfile apiMethodProfile = new ApiMethodProfile();

            when(repositoryManagerService.findPlan(anyInt())).thenReturn(deploymentPlan);
            when(planProfileRepository.findByDeploymentPlan(any())).thenReturn(planProfile);
            when(apiMethodRepository.findById(anyInt())).thenReturn(Optional.of(apiMethod));
            when(apiMethodProfileRespository.findByPlanProfileAndApiMethod(any(), any())).thenReturn(apiMethodProfile);
            when(cesRoleRepository.findById(anyInt())).thenReturn(Optional.empty());

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.savePlanApiProfile(new ApiMethodProfileDto[]{apiMethodProfileDto}, planId)
            );

            verify(repositoryManagerService).findPlan(planId);
            verify(planProfileRepository).findByDeploymentPlan(deploymentPlan);
            verify(apiMethodRepository).findById(apiMethodProfileDto.getMethodId());
            verify(apiMethodProfileRespository).findByPlanProfileAndApiMethod(planProfile, apiMethod);
            verify(cesRoleRepository).findById(apiMethodProfileDto.getAssociatedRolesId()[0]);
        }

        @Test
        @DisplayName("Save plan API profile -> ok")
        public void ok()
        {
            ApiMethodProfileDto apiMethodProfileDto = new ApiMethodProfileDto();
            apiMethodProfileDto.fillRandomly(4, false, 1, 3);
            DeploymentPlan deploymentPlan = new DeploymentPlan();
            PlanProfile planProfile = new PlanProfile();
            ApiMethod apiMethod = new ApiMethod();
            ApiMethodProfile apiMethodProfile = new ApiMethodProfile();

            when(repositoryManagerService.findPlan(anyInt())).thenReturn(deploymentPlan);
            when(planProfileRepository.findByDeploymentPlan(any())).thenReturn(planProfile);
            when(apiMethodRepository.findById(anyInt())).thenReturn(Optional.of(apiMethod));
            when(apiMethodProfileRespository.findByPlanProfileAndApiMethod(any(), any())).thenReturn(apiMethodProfile);
            when(cesRoleRepository.findById(anyInt())).then(i -> {
                CesRole cesRole = new CesRole();
                cesRole.setId(i.getArgument(0));
                // To force the not equals comparation for the set
                cesRole.setUuaa(cesRole.getId().toString());
                return Optional.of(cesRole);
            });

            apiManagerService.savePlanApiProfile(new ApiMethodProfileDto[]{apiMethodProfileDto}, planId);

            verify(repositoryManagerService).findPlan(planId);
            verify(planProfileRepository).findByDeploymentPlan(deploymentPlan);
            verify(apiMethodRepository).findById(apiMethodProfileDto.getMethodId());
            verify(apiMethodProfileRespository).findByPlanProfileAndApiMethod(planProfile, apiMethod);
            verify(cesRoleRepository).findById(apiMethodProfileDto.getAssociatedRolesId()[0]);
            Arrays.stream(apiMethodProfileDto.getAssociatedRolesId()).sequential().forEach(id -> verify(cesRoleRepository).findById(id));

            assertEquals(apiMethodProfileDto.getAssociatedRolesId().length, apiMethodProfile.getRoles().size());
            assertTrue(
                    Arrays.stream(apiMethodProfileDto.getAssociatedRolesId()).allMatch(
                            id -> apiMethodProfile.getRoles().stream().anyMatch(cesRole -> id == cesRole.getId())
                    )
            );
        }
    }

    @Nested
    class GetPlanApiDetailList
    {
        @Test
        @DisplayName("Get APIs detail list of a plan -> Plan not found")
        void notFound()
        {
            when(repositoryManagerService.findPlan(anyInt())).thenReturn(null);

            assertThrows(NovaException.class, () -> apiManagerService.getPlanApiDetailList(1));
            verify(repositoryManagerService).findPlan(anyInt());
        }

        @Test
        @DisplayName("Get APIs detail list of a plan -> Plan without APIs")
        void withoutApis()
        {
            Integer planId = 2408;
            DeploymentPlan deploymentPlan = new DeploymentPlan();
            ApiPlanDetailDto apiPlanDetailDto = new ApiPlanDetailDto();

            when(repositoryManagerService.findPlan(anyInt())).thenReturn(deploymentPlan);
            when(profilingUtils.isPlanExposingApis(any())).thenReturn(false);

            ApiPlanDetailDto result = apiManagerService.getPlanApiDetailList(planId);

            assertEquals(apiPlanDetailDto, result);
            verify(repositoryManagerService).findPlan(planId);
            verify(profilingUtils).isPlanExposingApis(deploymentPlan);
        }

        @Nested
        public class OK
        {
            @Test
            @DisplayName("Get APIs detail list of a plan -> Plan with empty plan profiles")
            public void withEmptyPlanProfiles()
            {
                String uuaa = "JGMV";
                String env = "prod";
                Integer planId = 2408;
                DeploymentPlan deploymentPlan = Mockito.mock(DeploymentPlan.class, Mockito.RETURNS_DEEP_STUBS);
                PlanProfile planProfile = new PlanProfile();
                ApiPlanDetailDto apiPlanDetailDto = new ApiPlanDetailDto();
                CesRole[] roles = new CesRole[]{};

                when(deploymentPlan.getReleaseVersion().getRelease().getProduct().getUuaa()).thenReturn(uuaa);
                when(deploymentPlan.getEnvironment()).thenReturn(env);
                when(deploymentPlan.getPlanProfiles()).thenReturn(Collections.emptyList());

                when(repositoryManagerService.findPlan(anyInt())).thenReturn(deploymentPlan);
                when(profilingUtils.isPlanExposingApis(any())).thenReturn(true);
                when(planProfilingUtils.createPlanProfile(any())).thenReturn(planProfile);
                when(profilingUtils.updateRoles(anyString(), anyString())).thenReturn(roles);
                when(dtoBuilder.buildApiPlanDetailDto(any(), any())).thenReturn(apiPlanDetailDto);

                ApiPlanDetailDto result = apiManagerService.getPlanApiDetailList(planId);

                assertEquals(apiPlanDetailDto, result);
                verify(repositoryManagerService).findPlan(planId);
                verify(profilingUtils).isPlanExposingApis(deploymentPlan);
                verify(planProfilingUtils).createPlanProfile(deploymentPlan);
                verify(deploymentPlan).addPlanProfile(planProfile);
                verify(profilingUtils).updateRoles(uuaa, env);
                verify(dtoBuilder).buildApiPlanDetailDto(deploymentPlan, roles);
            }

            @Test
            @DisplayName("Get APIs detail list of a plan -> Plan with filled plan profiles")
            public void withFilledPlanProfiles()
            {
                String uuaa = "JGMV";
                String env = "prod";
                Integer planId = 2408;
                DeploymentPlan deploymentPlan = Mockito.mock(DeploymentPlan.class, Mockito.RETURNS_DEEP_STUBS);
                ApiPlanDetailDto apiPlanDetailDto = new ApiPlanDetailDto();
                CesRole[] roles = new CesRole[]{};

                when(deploymentPlan.getReleaseVersion().getRelease().getProduct().getUuaa()).thenReturn(uuaa);
                when(deploymentPlan.getEnvironment()).thenReturn(env);
                when(deploymentPlan.getPlanProfiles()).thenReturn(List.of(new PlanProfile()));

                when(repositoryManagerService.findPlan(anyInt())).thenReturn(deploymentPlan);
                when(profilingUtils.isPlanExposingApis(any())).thenReturn(true);
                when(profilingUtils.updateRoles(anyString(), anyString())).thenReturn(roles);
                when(dtoBuilder.buildApiPlanDetailDto(any(), any())).thenReturn(apiPlanDetailDto);

                ApiPlanDetailDto result = apiManagerService.getPlanApiDetailList(planId);

                assertEquals(apiPlanDetailDto, result);
                verify(repositoryManagerService).findPlan(planId);
                verify(profilingUtils).isPlanExposingApis(deploymentPlan);
                verify(profilingUtils).updateRoles(uuaa, env);
                verify(dtoBuilder).buildApiPlanDetailDto(deploymentPlan, roles);
            }

        }
    }

    @Nested
    class GetApiStatus
    {
        @Test
        @DisplayName("Get API status -> OK")
        public void ok()
        {
            String[] retValue = apiManagerService.getApiStatus();

            assertArrayEquals(Arrays.stream(ApiState.values()).map(Enum::toString).sorted().toArray(String[]::new), retValue);
        }
    }

    @Nested
    class GetApiTypes
    {
        @Test
        @DisplayName("Get API types -> OK")
        public void ok()
        {
            String[] retValue = apiManagerService.getApiTypes();

            assertArrayEquals(Arrays.stream(ApiType.values()).map(Enum::toString).sorted().toArray(String[]::new), retValue);
        }
    }

    @Nested
    class GetApiDetail
    {
        Integer apiId = 909;
        Integer productId = 2408;

        @Test
        @DisplayName("Get API detail -> API not exists")
        public void apiNotExistsError()
        {
            when(apiManagerValidator.checkApiExistence(anyInt(), anyInt())).thenThrow(NovaException.class);

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.getApiDetail(apiId, productId)
            );
            verify(apiManagerValidator).checkApiExistence(apiId, productId);

        }

        @Test
        @DisplayName("Get API detail -> OK")
        public void ok()
        {
            Integer apiId = 909;
            Integer productId = 2408;
            Api<?, ?, ?> api = this.buildApi();
            ApiDetailDto apiDetailDto = new ApiDetailDto();
            apiDetailDto.fillRandomly(4, false, 1, 4);

            when(apiManagerValidator.checkApiExistence(anyInt(), anyInt())).then(i -> api);
            when(dtoBuilder.buildApiDetailDto(any())).thenReturn(apiDetailDto);

            ApiDetailDto retValue = apiManagerService.getApiDetail(apiId, productId);

            verify(apiManagerValidator).checkApiExistence(apiId, productId);
            verify(dtoBuilder).buildApiDetailDto(api);
            assertEquals(apiDetailDto, retValue);
        }

        private Api<?, ?, ?> buildApi()
        {
            SyncApi syncApi = new SyncApi();
            syncApi.setId(610);
            return syncApi;
        }
    }

    @Nested
    class GetApiVersionDetail
    {
        Integer apiVersionId = 909;
        Integer productId = 2408;

        @Test
        @DisplayName("Get API version detail -> API version not exists")
        public void apiVersionNotExistsError()
        {
            when(apiManagerValidator.checkApiVersionExistence(anyInt(), anyInt())).thenThrow(NovaException.class);

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.getApiVersionDetail(apiVersionId, productId, "all")
            );

            assertThrows(
                    NovaException.class,
                    () -> apiManagerService.getApiVersionDetail(apiVersionId, productId, "deployed")
            );
            verify(apiManagerValidator, times(2)).checkApiVersionExistence(productId, apiVersionId);

        }

        @Test
        @DisplayName("Get API version detail -> OK")
        public void ok()
        {
            ApiVersion<?, ?, ?> apiVersion = this.buildApiVersion();
            ApiVersionDetailDto apiVersionDetailDto = new ApiVersionDetailDto();
            apiVersionDetailDto.fillRandomly(4, false, 1, 4);

            when(apiManagerValidator.checkApiVersionExistence(anyInt(), anyInt())).then(i -> apiVersion);
            when(dtoBuilder.buildApiVersionDetailDto(any(), anyString())).thenReturn(apiVersionDetailDto);

            ApiVersionDetailDto retValue = apiManagerService.getApiVersionDetail(apiVersionId, productId, "all");
            ApiVersionDetailDto retValueDeployed = apiManagerService.getApiVersionDetail(apiVersionId, productId, "deployed");

            verify(apiManagerValidator, times(2)).checkApiVersionExistence(productId, apiVersionId);
            verify(dtoBuilder).buildApiVersionDetailDto(apiVersion, "all");
            verify(dtoBuilder).buildApiVersionDetailDto(apiVersion, "deployed");
            assertEquals(apiVersionDetailDto, retValue);
            assertEquals(apiVersionDetailDto, retValueDeployed);

        }

        private ApiVersion<?, ?, ?> buildApiVersion()
        {
            SyncApiVersion apiVersion = new SyncApiVersion();
            apiVersion.setId(610);
            return apiVersion;
        }
    }

    @Nested
    class RefreshDeployedApiVersionsState
    {
        Integer planId = 2408;

        @Test
        @DisplayName(value = "Refresh deployed API version state -> do nothing")
        public void doNothing()
        {
            when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.empty());

            apiManagerService.refreshDeployedApiVersionsState(planId);

            verify(deploymentPlanRepository).findById(planId);
        }

        @Test
        @DisplayName(value = "Refresh deployed API version state -> OK")
        public void ok()
        {
            DeploymentPlan deploymentPlan = Mockito.mock(DeploymentPlan.class, Mockito.RETURNS_DEEP_STUBS);
            ApiImplementation<?, ?, ?> apiImplementation = this.buildApiImplementation();

            when(deploymentPlan.getReleaseVersion().getApiImplementations()).thenReturn(Stream.of(apiImplementation));

            when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan));

            apiManagerService.refreshDeployedApiVersionsState(planId);

            verify(deploymentPlanRepository).findById(planId);
            assertEquals(ApiState.DEPLOYED, apiImplementation.getApiVersion().getApiState());
        }

        ApiImplementation<?, ?, ?> buildApiImplementation()
        {
            SyncApiVersion apiVersion = new SyncApiVersion();
            apiVersion.setApiState(ApiState.IMPLEMENTED);
            SyncApiImplementation apiImplementation = new SyncApiImplementation();
            apiImplementation.setApiVersion(apiVersion);
            return apiImplementation;
        }
    }

    @Nested
    class RefreshUndeployedApiVersionsState
    {
        Integer planId = 2408;

        @Test
        @DisplayName(value = "Refresh undeployed API version state -> do nothing")
        public void doNothing()
        {
            when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.empty());

            apiManagerService.refreshUndeployedApiVersionsState(planId);

            verify(deploymentPlanRepository).findById(planId);
        }

        @Test
        @DisplayName(value = "Refresh undeployed API version state -> api version deployed in other plan")
        public void apiVersionDeployedInOtherPlanOK()
        {
            DeploymentPlan deploymentPlan = Mockito.mock(DeploymentPlan.class, Mockito.RETURNS_DEEP_STUBS);
            ApiVersion<?, ?, ?> apiVersion = this.buildApiVersion(deploymentPlan, DeploymentStatus.DEPLOYED);

            when(deploymentPlan.getReleaseVersion().getApiImplementations()).then(i -> apiVersion.getApiImplementations().stream());
            when(deploymentPlan.getStatus()).thenReturn(DeploymentStatus.DEPLOYED);

            when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan));

            apiManagerService.refreshUndeployedApiVersionsState(planId);

            verify(deploymentPlanRepository).findById(planId);
            assertEquals(ApiState.DEPLOYED, apiVersion.getApiState());
        }

        @Test
        @DisplayName(value = "Refresh undeployed API version state -> api version NOT deployed in other plan")
        public void apiVersionNotDeployedInOtherPlanOK()
        {
            DeploymentPlan deploymentPlan = Mockito.mock(DeploymentPlan.class, Mockito.RETURNS_DEEP_STUBS);
            ApiVersion<?, ?, ?> apiVersion = this.buildApiVersion(deploymentPlan, DeploymentStatus.UNDEPLOYED);

            when(deploymentPlan.getReleaseVersion().getApiImplementations()).then(i -> apiVersion.getApiImplementations().stream());
            when(deploymentPlan.getStatus()).thenReturn(DeploymentStatus.DEPLOYED);

            when(deploymentPlanRepository.findById(anyInt())).thenReturn(Optional.of(deploymentPlan));

            apiManagerService.refreshUndeployedApiVersionsState(planId);

            verify(deploymentPlanRepository).findById(planId);
            assertEquals(ApiState.IMPLEMENTED, apiVersion.getApiState());
        }

        ApiVersion<?, ?, ?> buildApiVersion(final DeploymentPlan actualPlan, final DeploymentStatus statusOtherPlans)
        {
            var otherDeploymentPlan = new DeploymentPlan();
            otherDeploymentPlan.setStatus(statusOtherPlans);
            var releaseVersion = new ReleaseVersion();
            releaseVersion.setDeployments(List.of(actualPlan, otherDeploymentPlan));
            var releaseVersionSubsystem = new ReleaseVersionSubsystem();
            releaseVersionSubsystem.setReleaseVersion(releaseVersion);
            var releaseVersionService = new ReleaseVersionService();
            releaseVersionService.setVersionSubsystem(releaseVersionSubsystem);

            var apiVersion = new SyncApiVersion();
            apiVersion.setApiState(ApiState.DEPLOYED);
            var apiImplementation = new SyncApiImplementation();
            apiImplementation.setApiVersion(apiVersion);
            apiImplementation.setService(releaseVersionService);

            apiVersion.getApiImplementations().add(apiImplementation);

            return apiVersion;
        }
    }

    @Nested
    class RefreshUnimplementedApiVersionsState
    {

        @Test
        @DisplayName(value = "Refresh API version state when a release version is undeployed -> ok")
        public void ok()
        {
            int apiVersionId = 909;
            ApiImplementation<?, ?, ?> apiImplementation = this.buildApiImplementation(apiVersionId);
            ReleaseVersion releaseVersion = Mockito.mock(ReleaseVersion.class, RETURNS_DEEP_STUBS);

            when(releaseVersion.getApiImplementations()).thenReturn(Stream.of(apiImplementation));
            when(apiVersionRepository.findAllNotImplemented(any())).thenReturn(List.of(apiImplementation.getApiVersion()));

            apiManagerService.refreshUnimplementedApiVersionsState(releaseVersion);

            verify(apiVersionRepository).findAllNotImplemented(List.of(apiVersionId));
            assertEquals(ApiState.DEFINITION, apiImplementation.getApiVersion().getApiState());
        }

        @Test
        @DisplayName(value = "Refresh API version state when a release version is deleted -> do nothing because the version is already implemented by other release")
        public void apiVersionImplementedByOther()
        {
            int apiVersionId = 909;
            ApiImplementation<?, ?, ?> apiImplementation = this.buildApiImplementation(apiVersionId);
            ReleaseVersion releaseVersion = Mockito.mock(ReleaseVersion.class, RETURNS_DEEP_STUBS);

            when(releaseVersion.getApiImplementations()).thenReturn(Stream.of(apiImplementation));
            when(apiVersionRepository.findAllNotImplemented(any())).thenReturn(Collections.emptyList());

            apiManagerService.refreshUnimplementedApiVersionsState(releaseVersion);

            verify(apiVersionRepository).findAllNotImplemented(List.of(apiVersionId));
            assertEquals(ApiState.IMPLEMENTED, apiImplementation.getApiVersion().getApiState());
        }

        ApiImplementation<?, ?, ?> buildApiImplementation(final int apiVersionId)
        {
            var apiVersion = new SyncApiVersion();
            apiVersion.setId(apiVersionId);
            apiVersion.setApiState(ApiState.IMPLEMENTED);

            var apiImplementation = new SyncApiImplementation();
            apiImplementation.setApiVersion(apiVersion);

            return apiImplementation;
        }
    }

    @Nested
    class GetApisUsingMsaDocument
    {
        @Test
        @DisplayName(value = "Get APIs using MSA -> OK")
        public void ok()
        {
            Integer msaDocumentId = 909;
            when(apiManagerServiceModalityBased.getApisUsingMsaDocument(anyInt())).thenReturn(Collections.emptyList());

            List<? extends Api<?, ?, ?>> ret = apiManagerService.getApisUsingMsaDocument(msaDocumentId);

            verify(apiManagerServiceModalityBased).getApisUsingMsaDocument(msaDocumentId);
            assertEquals(0, ret.size());
        }

    }

    @Nested
    class GetApisUsingAraDocument
    {
        @Test
        @DisplayName(value = "Get APIs using ARA -> OK")
        public void ok()
        {
            Integer araDocumentId = 909;
            when(apiManagerServiceModalityBased.getApisUsingAraDocument(anyInt())).thenReturn(Collections.emptyList());

            List<? extends Api<?, ?, ?>> ret = apiManagerService.getApisUsingAraDocument(araDocumentId);

            verify(apiManagerServiceModalityBased).getApisUsingAraDocument(araDocumentId);
            assertEquals(0, ret.size());
        }

    }


    private static class UploadProductApisTypeModalityCartesianProductArgumentsSource implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context)
        {
            return Stream.of(
                    Arguments.arguments(ApiType.EXTERNAL, ApiModality.ASYNC_BACKTOBACK),
                    Arguments.arguments(ApiType.EXTERNAL, ApiModality.ASYNC_BACKTOFRONT),
                    Arguments.arguments(ApiType.EXTERNAL, ApiModality.SYNC),
                    Arguments.arguments(ApiType.NOT_GOVERNED, ApiModality.ASYNC_BACKTOBACK),
                    Arguments.arguments(ApiType.NOT_GOVERNED, ApiModality.ASYNC_BACKTOFRONT),
                    Arguments.arguments(ApiType.NOT_GOVERNED, ApiModality.SYNC),
                    Arguments.arguments(ApiType.GOVERNED, ApiModality.ASYNC_BACKTOBACK),
                    Arguments.arguments(ApiType.GOVERNED, ApiModality.ASYNC_BACKTOFRONT),
                    Arguments.arguments(ApiType.GOVERNED, ApiModality.SYNC)
            );
        }
    }

    private PolicyTaskReplyParametersDTO getDummyPolicyTaskReplyParametersDto(String taskStatus, String apiName, String uuaa, Integer productId)
    {
        PolicyTaskReplyParametersDTO parametersDTO = new PolicyTaskReplyParametersDTO();
        parametersDTO.setApiTaskId(1);
        parametersDTO.setProductId(productId);
        parametersDTO.setApiName(apiName);
        parametersDTO.setUuaa(uuaa);
        parametersDTO.setTaskStatus(taskStatus);
        parametersDTO.setBasePath("");
        return parametersDTO;
    }
}