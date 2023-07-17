package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiErrorList;
import com.bbva.enoa.apirestgen.apimanagerapi.model.TaskInfoDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novaheader.context.NovaContext;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal.IApiManagerValidatorModalityBased;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.docsystemsapi.services.interfaces.IDocSystemService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class ApiManagerValidatorImplTest
{
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ApiRepository apiRepository;
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private ReleaseVersionServiceRepository serviceRepository;
    @Mock
    private ApiVersionRepository apiVersionRepository;
    @Mock
    private IDocSystemService docSystemService;
    @Mock
    private ApiImplementationRepository apiImplementationRepository;
    @Mock
    private IProductUsersClient usersClient;
    @Mock
    private NovaContext novaContext;
    @Mock
    private IApiManagerValidatorModalityBased apiManagerValidatorServiceModalityBased;
    @InjectMocks
    private ApiManagerValidatorImpl apiManagerValidator;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                productRepository,
                apiRepository,
                deploymentPlanRepository,
                serviceRepository,
                apiVersionRepository,
                docSystemService,
                apiImplementationRepository,
                usersClient,
                novaContext,
                apiManagerValidatorServiceModalityBased
        );
    }
    
    @Nested
    class CheckUploadApiPermission
    {
        String ivUser = "xe72172";
        Integer productId = 2408;

        @Nested
        class NotGoberned
        {
            ApiType apiType = ApiType.NOT_GOVERNED;

            @Test
            @DisplayName("Check permission for upload NOT GOBERNED APIs -> Error")
            public void error()
            {
                when(novaContext.getIvUser()).thenReturn(ivUser);
                doThrow(NovaException.class).when(usersClient).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));

                assertThrows(
                        NovaException.class,
                        () -> apiManagerValidator.checkUploadApiPermission(apiType, productId)
                );

                verify(novaContext).getIvUser();
                verify(usersClient).checkHasPermission(ivUser, Constants.UPLOAD_API_PERMISSION, productId, ApiManagerValidatorImpl.PERMISSION_DENIED);
            }

            @Test
            @DisplayName("Check permission for upload NOT GOBERNED APIs -> OK")
            public void ok()
            {
                when(novaContext.getIvUser()).thenReturn(ivUser);

                apiManagerValidator.checkUploadApiPermission(apiType, productId);

                verify(novaContext).getIvUser();
                verify(usersClient).checkHasPermission(ivUser, Constants.UPLOAD_API_PERMISSION, productId, ApiManagerValidatorImpl.PERMISSION_DENIED);
            }
        }

        @Nested
        class Goberned
        {
            ApiType apiType = ApiType.GOVERNED;

            @Test
            @DisplayName("Check permission for upload GOBERNED APIs -> Error")
            public void error()
            {
                when(novaContext.getIvUser()).thenReturn(ivUser);
                doThrow(NovaException.class).when(usersClient).checkHasPermission(anyString(), anyString(), any(NovaException.class));

                assertThrows(
                        NovaException.class,
                        () -> apiManagerValidator.checkUploadApiPermission(apiType, productId)
                );

                verify(novaContext).getIvUser();
                verify(usersClient).checkHasPermission(ivUser, Constants.UPLOAD_GOVERNED_API_PERMISSION, ApiManagerValidatorImpl.PERMISSION_DENIED);
            }

            @Test
            @DisplayName("Check permission for upload GOBERNED APIs -> OK")
            public void ok()
            {
                when(novaContext.getIvUser()).thenReturn(ivUser);

                apiManagerValidator.checkUploadApiPermission(apiType, productId);

                verify(novaContext).getIvUser();
                verify(usersClient).checkHasPermission(ivUser, Constants.UPLOAD_GOVERNED_API_PERMISSION, ApiManagerValidatorImpl.PERMISSION_DENIED);
            }
        }

        @Nested
        class External
        {
            ApiType apiType = ApiType.EXTERNAL;

            @Test
            @DisplayName("Check permission for upload EXTERNAL APIs -> Error")
            public void error()
            {
                when(novaContext.getIvUser()).thenReturn(ivUser);
                doThrow(NovaException.class).when(usersClient).checkHasPermission(anyString(), anyString(), any(NovaException.class));

                assertThrows(
                        NovaException.class,
                        () -> apiManagerValidator.checkUploadApiPermission(apiType, productId)
                );

                verify(novaContext).getIvUser();
                verify(usersClient).checkHasPermission(ivUser, Constants.UPLOAD_EXTERNAL_API_PERMISSION, ApiManagerValidatorImpl.PERMISSION_DENIED);
            }

            @Test
            @DisplayName("Check permission for upload EXTERNAL APIs -> OK")
            public void ok()
            {
                when(novaContext.getIvUser()).thenReturn(ivUser);

                apiManagerValidator.checkUploadApiPermission(apiType, productId);

                verify(novaContext).getIvUser();
                verify(usersClient).checkHasPermission(ivUser, Constants.UPLOAD_EXTERNAL_API_PERMISSION, ApiManagerValidatorImpl.PERMISSION_DENIED);
            }
        }

    }

    @Nested
    class CheckCreatePolicyTaskPermission
    {
        String ivUser = "xe72172";
        Integer productId = 2408;
        
        @Test
        @DisplayName("Check permission to submit the create policiy task -> Error")
        public void error()
        {
            var taskInfoDto = Mockito.mock(TaskInfoDto.class, RETURNS_DEEP_STUBS);

            when(taskInfoDto.getProductId()).thenReturn(productId);

            when(novaContext.getIvUser()).thenReturn(ivUser);
            doThrow(NovaException.class).when(usersClient).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));

            assertThrows(
                    NovaException.class,
                    () -> apiManagerValidator.checkCreatePolicyTaskPermission(taskInfoDto)
            );

            verify(novaContext).getIvUser();
            verify(usersClient).checkHasPermission(ivUser, Constants.CREATE_POLICY_TASK_PERMISSION, productId, ApiManagerValidatorImpl.PERMISSION_DENIED);
        }
        
        @Test
        @DisplayName("Check permission to submit the create policiy task -> OK")
        public void ok()
        {
            var taskInfoDto = Mockito.mock(TaskInfoDto.class, RETURNS_DEEP_STUBS);

            when(taskInfoDto.getProductId()).thenReturn(productId);
            when(novaContext.getIvUser()).thenReturn(ivUser);

            apiManagerValidator.checkCreatePolicyTaskPermission(taskInfoDto);

            verify(novaContext).getIvUser();
            verify(usersClient).checkHasPermission(ivUser, Constants.CREATE_POLICY_TASK_PERMISSION, productId, ApiManagerValidatorImpl.PERMISSION_DENIED);
        }

    }

    @Nested
    class CheckDeleteApiPermission
    {
        String ivUser = "xe72172";
        Integer productId = 2408;

        @Nested
        class NotGoberned
        {
            ApiType apiType = ApiType.NOT_GOVERNED;

            @Test
            @DisplayName("Check permission for delete a NOT GOBERNED API -> Error")
            public void error()
            {
                when(novaContext.getIvUser()).thenReturn(ivUser);
                doThrow(NovaException.class).when(usersClient).checkHasPermission(anyString(), anyString(), anyInt(), any(NovaException.class));

                assertThrows(
                        NovaException.class,
                        () -> apiManagerValidator.checkDeleteApiPermission(apiType, productId)
                );

                verify(novaContext).getIvUser();
                verify(usersClient).checkHasPermission(ivUser, Constants.DELETE_API_PERMISSION, productId, ApiManagerValidatorImpl.PERMISSION_DENIED);
            }

            @Test
            @DisplayName("Check permission for delete a NOT GOBERNED API -> OK")
            public void ok()
            {
                when(novaContext.getIvUser()).thenReturn(ivUser);

                apiManagerValidator.checkDeleteApiPermission(apiType, productId);

                verify(novaContext).getIvUser();
                verify(usersClient).checkHasPermission(ivUser, Constants.DELETE_API_PERMISSION, productId, ApiManagerValidatorImpl.PERMISSION_DENIED);
            }
        }

        @Nested
        class Goberned
        {
            ApiType apiType = ApiType.GOVERNED;

            @Test
            @DisplayName("Check permission for delete a GOBERNED API -> Error")
            public void error()
            {
                when(novaContext.getIvUser()).thenReturn(ivUser);
                doThrow(NovaException.class).when(usersClient).checkHasPermission(anyString(), anyString(), any(NovaException.class));

                assertThrows(
                        NovaException.class,
                        () -> apiManagerValidator.checkDeleteApiPermission(apiType, productId)
                );

                verify(novaContext).getIvUser();
                verify(usersClient).checkHasPermission(ivUser, Constants.DELETE_GOVERNED_API_PERMISSION, ApiManagerValidatorImpl.PERMISSION_DENIED);
            }

            @Test
            @DisplayName("Check permission for delete a GOBERNED API -> OK")
            public void ok()
            {
                when(novaContext.getIvUser()).thenReturn(ivUser);

                apiManagerValidator.checkDeleteApiPermission(apiType, productId);

                verify(novaContext).getIvUser();
                verify(usersClient).checkHasPermission(ivUser, Constants.DELETE_GOVERNED_API_PERMISSION, ApiManagerValidatorImpl.PERMISSION_DENIED);
            }
        }

        @Nested
        class External
        {
            ApiType apiType = ApiType.EXTERNAL;

            @Test
            @DisplayName("Check permission for delete a EXTERNAL API -> Error")
            public void error()
            {
                when(novaContext.getIvUser()).thenReturn(ivUser);
                doThrow(NovaException.class).when(usersClient).checkHasPermission(anyString(), anyString(), any(NovaException.class));

                assertThrows(
                        NovaException.class,
                        () -> apiManagerValidator.checkDeleteApiPermission(apiType, productId)
                );

                verify(novaContext).getIvUser();
                verify(usersClient).checkHasPermission(ivUser, Constants.DELETE_EXTERNAL_API_PERMISSION, ApiManagerValidatorImpl.PERMISSION_DENIED);
            }

            @Test
            @DisplayName("Check permission for delete a EXTERNAL API -> OK")
            public void ok()
            {
                when(novaContext.getIvUser()).thenReturn(ivUser);

                apiManagerValidator.checkDeleteApiPermission(apiType, productId);

                verify(novaContext).getIvUser();
                verify(usersClient).checkHasPermission(ivUser, Constants.DELETE_EXTERNAL_API_PERMISSION, ApiManagerValidatorImpl.PERMISSION_DENIED);
            }
        }

    }

    @Nested
    class CheckProductExistence
    {
        final int productId = 909;

        @Test
        @DisplayName("Check product existence -> ok")
        void ok()
        {
            Product product = new Product();

            when(productRepository.findById(anyInt())).thenReturn(Optional.of(product));

            Product returnValue = apiManagerValidator.checkProductExistence(productId);

            verify(productRepository).findById(productId);
            assertEquals(product, returnValue);
        }

        @Test
        @DisplayName("Check product existence -> error")
        void noExistError()
        {
            when(productRepository.findById(anyInt())).thenReturn(Optional.empty());

            assertThrows(NovaException.class, () -> apiManagerValidator.checkProductExistence(productId));

            verify(productRepository).findById(productId);
        }
    }

    @Nested
    class CheckTaskDtoParameters
    {
        @ParameterizedTest
        @CsvSource({",", "909,", ",2408"})
        @DisplayName("Check taskDto parameters -> productId and apiId error")
        void productAndApiError(Integer apiId, Integer productId)
        {
            TaskInfoDto taskInfoDto = new TaskInfoDto();
            taskInfoDto.setNovaApiId(apiId);
            taskInfoDto.setProductId(productId);

            assertThrows(NovaException.class, () -> apiManagerValidator.checkTaskDtoParameters(taskInfoDto));
        }

        @DisplayName("Check taskDto parameters -> araLink and msaLink error")
        @Test
        void araLinkAndMsaLinkError()
        {
            TaskInfoDto taskInfoDto = new TaskInfoDto();
            taskInfoDto.setNovaApiId(909);
            taskInfoDto.setProductId(2408);
            taskInfoDto.setAraDocumentId(2);

            assertThrows(NovaException.class, () -> apiManagerValidator.checkTaskDtoParameters(taskInfoDto));
        }

        @Test
        @DisplayName("Check taskDto parameters -> ok")
        void ok()
        {
            TaskInfoDto taskInfoDto = new TaskInfoDto();
            taskInfoDto.setNovaApiId(909);
            taskInfoDto.setProductId(2408);
            taskInfoDto.setAraDocumentId(2);
            taskInfoDto.setMsaDocumentId(3);

            apiManagerValidator.checkTaskDtoParameters(taskInfoDto);
        }
    }

    @Nested
    class CheckApiExistence
    {
        final Integer apiId = 909;
        final Integer productId = 2408;

        @Test
        @DisplayName("Check sync API existente -> API not found")
        void apiNotFound()
        {
            when(apiRepository.findById(anyInt())).thenReturn(Optional.empty());

            assertThrows(NovaException.class, () -> apiManagerValidator.checkApiExistence(apiId, productId));

            verify(apiRepository).findById(apiId);
        }

        @Test
        @DisplayName("Check sync API existente -> API does not belong to the product")
        void apiDontBelongToProduct()
        {
            Api<?,?,?> api = Mockito.mock(Api.class, Answers.RETURNS_DEEP_STUBS);
            when(api.getProduct().getId()).thenReturn(1);

            when(apiRepository.findById(anyInt())).thenReturn(Optional.of(api));

            assertThrows(NovaException.class, () -> apiManagerValidator.checkApiExistence(apiId, productId));

            verify(apiRepository).findById(apiId);
        }

        @Test
        @DisplayName("Check sync API existente -> ok")
        void ok()
        {
            Api<?,?,?> api = Mockito.mock(Api.class, Answers.RETURNS_DEEP_STUBS);
            when(api.getProduct().getId()).thenReturn(productId);

            when(apiRepository.findById(anyInt())).thenReturn(Optional.of(api));

            Api<?,?,?> returnValue = apiManagerValidator.checkApiExistence(apiId, productId);

            verify(apiRepository).findById(apiId);
            assertEquals(api, returnValue);
        }

    }
    
    @Nested
    class AssertVersionOfApiNotExists
    {
        String version1 = "1.0.0";
        String version2 = "2.0.0";

        @Test
        @DisplayName("Assert that a version of an API does not exist -> version already exists error")
        public void apiVersionAlreadyExisitsError()
        {
            Api<?,?,?> api = Mockito.mock(Api.class);
            ApiVersion<?,?,?> apiVersion = Mockito.mock(ApiVersion.class);

            when(api.getApiVersions()).then(i -> List.of(apiVersion));
            when(apiVersion.getVersion()).thenReturn(version1);

            assertThrows(
                    NovaException.class,
                    () -> apiManagerValidator.assertVersionOfApiNotExists(api, version1)
            );
        }

        @Test
        @DisplayName("Assert that a version of an API does not exist -> ok")
        public void ok()
        {
            Api<?,?,?> api = Mockito.mock(Api.class);
            ApiVersion<?,?,?> apiVersion = Mockito.mock(ApiVersion.class);

            when(api.getApiVersions()).then(i -> List.of(apiVersion));
            when(apiVersion.getVersion()).thenReturn(version2);

            apiManagerValidator.assertVersionOfApiNotExists(api, version1);
        }

    }
    
    @Nested
    class CheckApiVersionExistence
    {
        final Integer apiId = 909;
        final Integer productId = 2408;

        @Test
        @DisplayName("Check API version existence -> API version does not exist error")
        public void noApiVersionFound()
        {
            when(apiVersionRepository.findById(anyInt())).thenReturn(Optional.empty());

            assertThrows(
                    NovaException.class,
                    () -> apiManagerValidator.checkApiVersionExistence(productId, apiId)
            );

            verify(apiVersionRepository).findById(apiId);
        }

        @Test
        @DisplayName("Check API version existence -> API version does not belong to product error")
        public void apiDontBelongToProduct()
        {
            ApiVersion<?,?,?> apiVersion = Mockito.mock(ApiVersion.class, Answers.RETURNS_DEEP_STUBS);
            when(apiVersion.getApi().getProduct().getId()).thenReturn(1);

            when(apiVersionRepository.findById(anyInt())).thenReturn(Optional.of(apiVersion));

            assertThrows(
                    NovaException.class,
                    () -> apiManagerValidator.checkApiVersionExistence(productId, apiId)
            );

            verify(apiVersionRepository).findById(apiId);
        }

        @Test
        @DisplayName("Check API version existence -> ok")
        public void ok()
        {
            ApiVersion<?,?,?> apiVersion = Mockito.mock(ApiVersion.class, Answers.RETURNS_DEEP_STUBS);
            when(apiVersion.getApi().getProduct().getId()).thenReturn(productId);

            when(apiVersionRepository.findById(anyInt())).thenReturn(Optional.of(apiVersion));

            ApiVersion<?,?,?> retValue = apiManagerValidator.checkApiVersionExistence(productId, apiId);

            verify(apiVersionRepository).findById(apiId);
            assertEquals(apiVersion, retValue);
        }

    }

    @Nested
    class FilterByProductId
    {
        final Integer productId = 909;

        @Test
        @DisplayName("Filter by productId -> product not found")
        void productNotFound()
        {
            when(productRepository.findById(anyInt())).thenReturn(Optional.empty());

            assertThrows(NovaException.class, () -> apiManagerValidator.filterByProductId(productId));

            verify(productRepository).findById(productId);
        }

        @Test
        @DisplayName("Filter by productId -> OK")
        void ok()
        {
            List<Api<?,?,?>> expectedValue = List.of(Mockito.mock(Api.class));

            when(productRepository.findById(anyInt())).thenReturn(Optional.of(new Product()));
            when(apiRepository.findAllByProductId(anyInt())).thenReturn(expectedValue);

            List<Api<?,?,?>> returnValue = apiManagerValidator.filterByProductId(productId);

            verify(productRepository).findById(productId);
            verify(apiRepository).findAllByProductId(productId);
            assertEquals(expectedValue, returnValue);
        }


    }

    @Nested
    class CheckServiceExistence
    {
        final Integer serviceId = 2408;

        @Test
        @DisplayName("Check service existence -> service not found")
        public void checkServiceExistenceException()
        {
            when(serviceRepository.findById(anyInt())).thenReturn(Optional.empty());

            assertThrows(
                    NovaException.class,
                    () -> apiManagerValidator.checkServiceExistence(serviceId)
            );

            verify(serviceRepository).findById(serviceId);
        }

        @Test
        @DisplayName("Check service existence -> OK")
        public void ok()
        {
            ReleaseVersionService releaseVersionService = new ReleaseVersionService();

            when(serviceRepository.findById(anyInt())).thenReturn(Optional.of(releaseVersionService));

            ReleaseVersionService returnValue = apiManagerValidator.checkServiceExistence(serviceId);

            verify(serviceRepository).findById(serviceId);
            assertEquals(releaseVersionService, returnValue);
        }
    }

    @Nested
    class IsApiVersionErasable
    {
        final Integer releaseVersionId = 909;

        @Test
        @DisplayName("Is sync API version erasable -> served impl deployed")
        public void apiDeployed()
        {
            ApiVersion<?,?,?> apiVersion = Mockito.mock(ApiVersion.class, Answers.RETURNS_DEEP_STUBS);
            ApiImplementation<?,?,?> apiImpl = Mockito.mock(ApiImplementation.class, Answers.RETURNS_DEEP_STUBS);

            when(apiVersion.getApiImplementations()).then(i -> List.of(apiImpl));
            when(apiImpl.getImplementedAs()).thenReturn(ImplementedAs.SERVED);
            when(apiImpl.getService().getVersionSubsystem().getReleaseVersion().getId()).thenReturn(releaseVersionId);

            when(deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(anyInt(), any(), any())).thenReturn(List.of(new DeploymentPlan()));
            when(apiImplementationRepository.findApiImplementationsBackwardCompatibleWithApiVersion(anyInt())).thenReturn(Collections.emptyList());
            when(apiImplementationRepository.findApiImplementationsConsumingApiVersion(anyInt())).thenReturn(Collections.emptyList());

            ApiErrorList retValue = apiManagerValidator.isApiVersionErasable(apiVersion);

            verify(deploymentPlanRepository).getByReleaseVersionAndEnvironmentAndStatus(releaseVersionId, Environment.INT.getEnvironment(), DeploymentStatus.DEPLOYED);
            verify(deploymentPlanRepository).getByReleaseVersionAndEnvironmentAndStatus(releaseVersionId, Environment.PRE.getEnvironment(), DeploymentStatus.DEPLOYED);
            verify(deploymentPlanRepository).getByReleaseVersionAndEnvironmentAndStatus(releaseVersionId, Environment.PRO.getEnvironment(), DeploymentStatus.DEPLOYED);
            verify(apiImplementationRepository).findApiImplementationsBackwardCompatibleWithApiVersion(apiVersion.getId());
            verify(apiImplementationRepository).findApiImplementationsConsumingApiVersion(apiVersion.getId());

            assertEquals(3, retValue.getErrorList().length);

        }

        @Test
        @DisplayName("Is sync API version erasable -> served implemented")
        public void apiImplemented()
        {
            ApiVersion<?,?,?> apiVersion = Mockito.mock(ApiVersion.class, Answers.RETURNS_DEEP_STUBS);
            ApiImplementation<?,?,?> apiImpl = Mockito.mock(ApiImplementation.class, Answers.RETURNS_DEEP_STUBS);

            when(apiVersion.getApiImplementations()).then(i -> List.of(apiImpl));
            when(apiImpl.getImplementedAs()).thenReturn(ImplementedAs.SERVED);
            when(apiImpl.getService().getVersionSubsystem().getReleaseVersion().getId()).thenReturn(releaseVersionId);

            when(deploymentPlanRepository.getByReleaseVersionAndEnvironmentAndStatus(anyInt(), any(), any())).thenReturn(Collections.emptyList());
            when(apiImplementationRepository.findApiImplementationsBackwardCompatibleWithApiVersion(anyInt())).thenReturn(Collections.emptyList());
            when(apiImplementationRepository.findApiImplementationsConsumingApiVersion(anyInt())).thenReturn(Collections.emptyList());

            ApiErrorList retValue = apiManagerValidator.isApiVersionErasable(apiVersion);

            verify(deploymentPlanRepository).getByReleaseVersionAndEnvironmentAndStatus(releaseVersionId, Environment.INT.getEnvironment(), DeploymentStatus.DEPLOYED);
            verify(deploymentPlanRepository).getByReleaseVersionAndEnvironmentAndStatus(releaseVersionId, Environment.PRE.getEnvironment(), DeploymentStatus.DEPLOYED);
            verify(deploymentPlanRepository).getByReleaseVersionAndEnvironmentAndStatus(releaseVersionId, Environment.PRO.getEnvironment(), DeploymentStatus.DEPLOYED);
            verify(apiImplementationRepository).findApiImplementationsBackwardCompatibleWithApiVersion(apiVersion.getId());
            verify(apiImplementationRepository).findApiImplementationsConsumingApiVersion(apiVersion.getId());

            assertEquals(1, retValue.getErrorList().length);

        }

        @Test
        @DisplayName("Is sync API version erasable -> served impl by another")
        public void apiImplementedByAnother()
        {
            ApiVersion<?,?,?> apiVersion = Mockito.mock(ApiVersion.class, Answers.RETURNS_DEEP_STUBS);
            ApiImplementation<?,?,?> apiImpl = Mockito.mock(ApiImplementation.class, Answers.RETURNS_DEEP_STUBS);

            when(apiVersion.getApiImplementations()).thenReturn(Collections.emptyList());
            when(apiImpl.getImplementedAs()).thenReturn(ImplementedAs.SERVED);
            when(apiImpl.getService().getVersionSubsystem().getReleaseVersion().getId()).thenReturn(releaseVersionId);

            when(apiImplementationRepository.findApiImplementationsBackwardCompatibleWithApiVersion(anyInt())).thenReturn(List.of(apiImpl));
            when(apiImplementationRepository.findApiImplementationsConsumingApiVersion(anyInt())).thenReturn(List.of(apiImpl));

            ApiErrorList retValue = apiManagerValidator.isApiVersionErasable(apiVersion);

            verify(apiImplementationRepository).findApiImplementationsBackwardCompatibleWithApiVersion(apiVersion.getId());
            verify(apiImplementationRepository).findApiImplementationsConsumingApiVersion(apiVersion.getId());

            assertEquals(2, retValue.getErrorList().length);

        }

        @Test
        @DisplayName("Is sync API version erasable -> no errors")
        public void ok()
        {
            ApiVersion<?,?,?> apiVersion = Mockito.mock(ApiVersion.class, Answers.RETURNS_DEEP_STUBS);
            ApiImplementation<?,?,?> apiImpl = Mockito.mock(ApiImplementation.class, Answers.RETURNS_DEEP_STUBS);

            when(apiVersion.getApiImplementations()).thenReturn(Collections.emptyList());
            when(apiImpl.getImplementedAs()).thenReturn(ImplementedAs.SERVED);

            when(apiImplementationRepository.findApiImplementationsBackwardCompatibleWithApiVersion(anyInt())).thenReturn(Collections.emptyList());
            when(apiImplementationRepository.findApiImplementationsConsumingApiVersion(anyInt())).thenReturn(Collections.emptyList());

            ApiErrorList retValue = apiManagerValidator.isApiVersionErasable(apiVersion);

            verify(apiImplementationRepository).findApiImplementationsBackwardCompatibleWithApiVersion(apiVersion.getId());
            verify(apiImplementationRepository).findApiImplementationsConsumingApiVersion(apiVersion.getId());

            assertEquals(0, retValue.getErrorList().length);
        }

    }
    
    @Nested
    class ValidateAndGetDocument
    {
        Integer docSystemId = 909;

        @ParameterizedTest(name = "[{index}] docCategory: {0}, docSystem: {1}")
        @DisplayName("Validate and get document -> not found error")
        @CsvSource({"GOOGLE_DRIVE,FILE","MSA,FOLDER","ARA,FILE","BATCH_SCHEDULE,FOLDER","OTHER,FILE"})
        public void notFoundError(DocumentCategory docSystemCategory, DocumentType docSystemType)
        {
            when(docSystemService.getDocSystemWithIdAndCategoryAndType(anyInt(), any(), any())).thenThrow(NovaException.class);

            assertThrows(
                    NovaException.class,
                    () -> apiManagerValidator.validateAndGetDocument(docSystemId, docSystemCategory, docSystemType)
            );

            verify(docSystemService).getDocSystemWithIdAndCategoryAndType(docSystemId, docSystemCategory, docSystemType);
        }
        
        @ParameterizedTest
        @DisplayName("Validate and get document -> OK")
        @CsvSource({"GOOGLE_DRIVE,FILE","MSA,FOLDER","ARA,FILE","BATCH_SCHEDULE,FOLDER","OTHER,FILE"})
        public void ok(DocumentCategory docSystemCategory, DocumentType docSystemType)
        {
            var docSystem = new DocSystem();
            when(docSystemService.getDocSystemWithIdAndCategoryAndType(anyInt(), any(), any())).thenReturn(Optional.of(docSystem));

            DocSystem ret = apiManagerValidator.validateAndGetDocument(docSystemId, docSystemCategory, docSystemType);

            verify(docSystemService).getDocSystemWithIdAndCategoryAndType(docSystemId, docSystemCategory, docSystemType);
            assertEquals(docSystem, ret);
        }

    }

    @Nested
    class FindAndValidateOrPersistIfMissing
    {
        @Test
        @DisplayName("Find and validate or persiste if missing -> validation error")
        public void validationError()
        {
            var api = Mockito.mock(Api.class);

            when(apiManagerValidatorServiceModalityBased.findAndValidateOrPersistIfMissing(any())).thenThrow(NovaException.class);

            assertThrows(
                    NovaException.class,
                    () -> apiManagerValidator.findAndValidateOrPersistIfMissing(api)
            );

            verify(apiManagerValidatorServiceModalityBased).findAndValidateOrPersistIfMissing(api);
        }
        
        @Test
        @DisplayName("Find and validate or persiste if missing -> validation error")
        public void ok()
        {
            var api = Mockito.mock(Api.class);
            var apiRet = Mockito.mock(Api.class);

            when(apiManagerValidatorServiceModalityBased.findAndValidateOrPersistIfMissing(any())).then(i -> apiRet);

            Api<?,?,?> ret = apiManagerValidator.findAndValidateOrPersistIfMissing(api);

            verify(apiManagerValidatorServiceModalityBased).findAndValidateOrPersistIfMissing(api);
            assertEquals(apiRet, ret);
        }

    }

    
    
    
//    @Nested
//    class CheckApiVersionCreation
//    {
//        @Test
//        @DisplayName("Check sync API version creation -> version already exists error")
//        public void versionExistsError()
//        {
//            String newVersion = "1.0.2";
//            SyncApi syncApi = new SyncApi().setApiVersions(
//                    Arrays.asList(
//                            new ApiVersion().setVersion("1.0.0"),
//                            new ApiVersion().setVersion("1.0.1"),
//                            new ApiVersion().setVersion(newVersion)
//                    )
//            );
//
//            assertThrows(
//                    NovaException.class,
//                    () -> apiManagerValidator.checkSyncApiVersionCreation(syncApi, newVersion)
//            );
//        }
//
//        @Test
//        @DisplayName("Check sync API version creation -> ok")
//        public void ok()
//        {
//            String newVersion = "1.0.2";
//            SyncApi syncApi = new SyncApi().setApiVersions(
//                    Arrays.asList(
//                            new ApiVersion().setVersion("1.0.0"),
//                            new ApiVersion().setVersion("1.0.1")
//                    )
//            );
//
//            apiManagerValidator.checkSyncApiVersionCreation(syncApi, newVersion);
//        }
//    }

//    @Nested
//    class CheckApiCreation
//    {
//        @Test
//        @DisplayName("Check sync API creation -> no previous API exists")
//        public void noPreviousApi()
//        {
//            SyncApi syncApi = this.buildSyncApi();
//
//            when(apiRepository.findByProductIdAndNameAndUuaa(anyInt(), anyString(), anyString())).thenReturn(null);
//            when(apiRepository.saveAndFlush(any())).thenReturn(syncApi);
//
//            SyncApi retValue = apiManagerValidator.checkSyncApiCreation(syncApi, null);
//
//            verify(apiRepository).findByProductIdAndNameAndUuaa(syncApi.getProduct().getId(), syncApi.getName(), syncApi.getUuaa());
//            verify(apiRepository).saveAndFlush(syncApi);
//            assertEquals(syncApi, retValue);
//        }
//
//        @Test
//        @DisplayName("Check sync API creation -> exists previous API but with different type")
//        public void previousApiDifferentType()
//        {
//            SyncApi syncApi = this.buildSyncApi();
//            SyncApi previousSyncApi = this.buildSyncApi().setType(ApiType.EXTERNAL);
//
//            when(apiRepository.findByProductIdAndNameAndUuaa(anyInt(), anyString(), anyString())).thenReturn(previousSyncApi);
//
//            assertThrows(
//                    NovaException.class,
//                    () -> apiManagerValidator.checkSyncApiCreation(syncApi, ApiType.GOVERNED.getApiType())
//
//            );
//
//            verify(apiRepository).findByProductIdAndNameAndUuaa(syncApi.getProduct().getId(), syncApi.getName(), syncApi.getUuaa());
//        }
//
//        @Test
//        @DisplayName("Check sync API creation -> exists previous API but with different base path")
//        public void previousApiDifferentBaseapath()
//        {
//            ApiType apiType = ApiType.NOT_GOVERNED;
//            SyncApi syncApi = this.buildSyncApi().setBasePathSwagger("/leo");
//            SyncApi previousSyncApi = this.buildSyncApi().setType(apiType).setBasePathSwagger("/daniela");
//
//            when(apiRepository.findByProductIdAndNameAndUuaa(anyInt(), anyString(), anyString())).thenReturn(previousSyncApi);
//
//            assertThrows(
//                    NovaException.class,
//                    () -> apiManagerValidator.checkSyncApiCreation(syncApi, apiType.getApiType())
//
//            );
//
//            verify(apiRepository).findByProductIdAndNameAndUuaa(syncApi.getProduct().getId(), syncApi.getName(), syncApi.getUuaa());
//        }
//
//        @Test
//        @DisplayName("Check sync API creation -> exists previous API with same type and base path")
//        public void previousApi()
//        {
//            ApiType apiType = ApiType.NOT_GOVERNED;
//            String basePath = "/leo";
//            SyncApi syncApi = this.buildSyncApi().setBasePathSwagger(basePath);
//            SyncApi previousSyncApi = this.buildSyncApi().setType(apiType).setBasePathSwagger(basePath).setName("1.0.1");
//
//            when(apiRepository.findByProductIdAndNameAndUuaa(anyInt(), anyString(), anyString())).thenReturn(previousSyncApi);
//
//            SyncApi retValue = apiManagerValidator.checkSyncApiCreation(syncApi, apiType.getApiType());
//
//            verify(apiRepository).findByProductIdAndNameAndUuaa(syncApi.getProduct().getId(), syncApi.getName(), syncApi.getUuaa());
//            assertEquals(previousSyncApi, retValue);
//        }
//
//        private SyncApi buildSyncApi()
//        {
//            Product product = new Product();
//            product.setId(909);
//            return new SyncApi().setProduct(product).setName("1.0.0").setUuaa("JGMV");
//        }
//
//    }
}