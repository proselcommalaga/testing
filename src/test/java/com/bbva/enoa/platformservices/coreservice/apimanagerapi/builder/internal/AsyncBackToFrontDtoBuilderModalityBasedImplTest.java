package com.bbva.enoa.platformservices.coreservice.apimanagerapi.builder.internal;

import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDetailDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiDocument;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiMethodDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPlanDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiPoliciesDto;
import com.bbva.enoa.apirestgen.apimanagerapi.model.ApiVersionDto;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskKeyDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.api.entities.ApiSecurityPolicy;
import com.bbva.enoa.datamodel.model.api.entities.ApiSecurityPolicyId;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToFrontApiVersion;
import com.bbva.enoa.datamodel.model.api.entities.SecurityPolicy;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiPolicyStatus;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiState;
import com.bbva.enoa.datamodel.model.api.enumerates.SecurityPolicyType;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class AsyncBackToFrontDtoBuilderModalityBasedImplTest
{
    @Mock
    private TodoTaskServiceClient todoTaskClient;

    @Mock
    private IProfilableDtoBuilder profilableDtoBuilder;

    @InjectMocks
    private AsyncBackToFrontDtoBuilderModalityBasedImpl asyncBackToFrontDtoBuilderModalityBased;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                todoTaskClient,
                profilableDtoBuilder
        );
    }

    @Nested
    class BuildApiDetailDto
    {
        @Test
        @DisplayName("ApiDetailDto without pending policies is built successfully")
        public void buildWithoutPendingPolicies()
        {
            final AsyncBackToFrontApi api = buildAsyncBackToFrontApiWithoutPendingPolicies();

            final ApiDetailDto apiDetailDto = asyncBackToFrontDtoBuilderModalityBased.buildApiDetailDto(api);

            assertCommonApi(apiDetailDto, api);

            assertNull(apiDetailDto.getPendingTaskId());
        }

        @Test
        @DisplayName("ApiDetailDto with pending policy and pending TODOTask is built successfully")
        public void buildWithPendingPolicyAndPendingTask()
        {
            final AsyncBackToFrontApi api = buildAsyncBackToFrontApiWithPendingPolicy();

            final ApiTaskDTO pendingTaskDto = new ApiTaskDTO();
            pendingTaskDto.setId(RandomUtils.nextInt(0, 1000));
            pendingTaskDto.setStatus(ToDoTaskStatus.PENDING.name());

            when(todoTaskClient.getApiTask(any())).thenReturn(pendingTaskDto);

            final ApiDetailDto apiDetailDto = asyncBackToFrontDtoBuilderModalityBased.buildApiDetailDto(api);

            assertCommonApi(apiDetailDto, api);

            assertEquals(pendingTaskDto.getId(), apiDetailDto.getPendingTaskId());

            final ArgumentCaptor<ApiTaskKeyDTO> pendingTaskDtoCaptor = ArgumentCaptor.forClass(ApiTaskKeyDTO.class);
            verify(todoTaskClient).getApiTask(pendingTaskDtoCaptor.capture());

            assertEquals(api.getUuaa(), pendingTaskDtoCaptor.getValue().getUuaa());
            assertEquals(api.getName(), pendingTaskDtoCaptor.getValue().getApiName());
            assertEquals(api.getBasePathSwagger(), pendingTaskDtoCaptor.getValue().getBasePath());
        }

        @Test
        @DisplayName("ApiDetailDto with pending policy and not pending TODOTask is built successfully")
        public void buildWithPendingPolicyAndNotPendingTask()
        {
            final AsyncBackToFrontApi api = buildAsyncBackToFrontApiWithPendingPolicy();

            final ApiTaskDTO pendingTaskDto = new ApiTaskDTO();
            pendingTaskDto.setId(RandomUtils.nextInt(0, 1000));
            pendingTaskDto.setStatus(ToDoTaskStatus.PENDING_ERROR.name());

            when(todoTaskClient.getApiTask(any())).thenReturn(pendingTaskDto);

            final ApiDetailDto apiDetailDto = asyncBackToFrontDtoBuilderModalityBased.buildApiDetailDto(api);

            assertCommonApi(apiDetailDto, api);

            assertNull(apiDetailDto.getPendingTaskId());

            final ArgumentCaptor<ApiTaskKeyDTO> pendingTaskDtoCaptor = ArgumentCaptor.forClass(ApiTaskKeyDTO.class);
            verify(todoTaskClient).getApiTask(pendingTaskDtoCaptor.capture());

            assertEquals(api.getUuaa(), pendingTaskDtoCaptor.getValue().getUuaa());
            assertEquals(api.getName(), pendingTaskDtoCaptor.getValue().getApiName());
            assertEquals(api.getBasePathSwagger(), pendingTaskDtoCaptor.getValue().getBasePath());
        }

        private AsyncBackToFrontApi buildAsyncBackToFrontApiWithoutPendingPolicies()
        {
            final Integer apiId = RandomUtils.nextInt(0, 10000);
            final Integer securityPolicyId = RandomUtils.nextInt(0, 10000);
            final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];

            final AsyncBackToFrontApiVersion apiVersion = new AsyncBackToFrontApiVersion();
            apiVersion.setId(RandomUtils.nextInt(0, 1000));
            apiVersion.setVersion(RandomStringUtils.randomAlphanumeric(6));
            apiVersion.setApiState(ApiState.values()[RandomUtils.nextInt(0, ApiState.values().length)]);

            final ArrayList<AsyncBackToFrontApiVersion> apiVersions = new ArrayList<>();
            apiVersions.add(apiVersion);

            final SecurityPolicy securityPolicy = new SecurityPolicy();
            securityPolicy.setId(securityPolicyId);
            securityPolicy.setDisplayedName(RandomStringUtils.randomAlphanumeric(50));
            securityPolicy.setSecurityPolicyType(SecurityPolicyType.values()[RandomUtils.nextInt(0, SecurityPolicyType.values().length)]);

            final ApiSecurityPolicy apiSecurityPolicy = new ApiSecurityPolicy();
            apiSecurityPolicy.setId(new ApiSecurityPolicyId(apiId, securityPolicyId, environment.getEnvironment()));
            apiSecurityPolicy.setSecurityPolicy(securityPolicy);

            final ArrayList<ApiSecurityPolicy> apiSecurityPolicies = new ArrayList<>();
            apiSecurityPolicies.add(apiSecurityPolicy);

            final AsyncBackToFrontApi api = new AsyncBackToFrontApi();
            api.setId(apiId);
            api.setApiVersions(apiVersions);
            api.setUuaa(RandomStringUtils.randomAlphanumeric(4).toUpperCase());
            api.setName(RandomStringUtils.randomAlphanumeric(25));
            api.setBasePathSwagger(RandomStringUtils.randomAlphanumeric(10, 50));

            final List<ApiPolicyStatus> notPendingPolicyStatus = Stream.of(ApiPolicyStatus.values())
                    .filter(aps -> aps != ApiPolicyStatus.PENDING)
                    .collect(Collectors.toList());

            api.setPolicyStatus(notPendingPolicyStatus.get(RandomUtils.nextInt(0, notPendingPolicyStatus.size())));
            api.setApiSecurityPolicies(apiSecurityPolicies);
            api.setMsaDocument(buildRandomDocument());
            api.setAraDocument(buildRandomDocument());

            return api;
        }

        private AsyncBackToFrontApi buildAsyncBackToFrontApiWithPendingPolicy()
        {
            final AsyncBackToFrontApi api = buildAsyncBackToFrontApiWithoutPendingPolicies();

            api.setPolicyStatus(ApiPolicyStatus.PENDING);

            return api;
        }

        private DocSystem buildRandomDocument()
        {
            final DocSystem doc = new DocSystem();
            doc.setId(RandomUtils.nextInt(0, 1000));
            doc.setSystemName(RandomStringUtils.randomAlphanumeric(0, 100));
            doc.setUrl(RandomStringUtils.randomAlphanumeric(0, 255));

            return doc;
        }

        private void assertCommonApi(final ApiDetailDto apiDetailDto, final AsyncBackToFrontApi api)
        {
            assertEquals(1, apiDetailDto.getVersions().length);

            final AsyncBackToFrontApiVersion apiVersion = api.getApiVersions().get(0);
            final ApiVersionDto apiVersionDetailDto = apiDetailDto.getVersions()[0];

            assertEquals(apiVersion.getId(), apiVersionDetailDto.getId());
            assertEquals(apiVersion.getVersion(), apiVersionDetailDto.getVersion());
            assertEquals(apiVersion.getApiState().getApiState(), apiVersionDetailDto.getStatus());

            final ApiSecurityPolicy apiSecurityPolicy = api.getApiSecurityPolicies().get(0);
            final ApiPoliciesDto apiPolicyDto = apiDetailDto.getPolicies()[0];

            assertEquals(apiSecurityPolicy.getSecurityPolicy().getDisplayedName(), apiPolicyDto.getName());
            assertEquals(apiSecurityPolicy.getSecurityPolicy().getSecurityPolicyType().getPolicyType(), apiPolicyDto.getType());
            assertEquals(apiSecurityPolicy.getId().getEnvironment(), apiPolicyDto.getEnvironment());

            final DocSystem msaDocument = api.getMsaDocument();
            final ApiDocument msaDocumentDto = apiDetailDto.getMsaDocument();

            assertDocument(msaDocument, msaDocumentDto);

            final DocSystem araDocument = api.getAraDocument();
            final ApiDocument araDocumentDto = apiDetailDto.getAraDocument();

            assertDocument(araDocument, araDocumentDto);
        }

        private void assertDocument(final DocSystem expectedDocument, final ApiDocument actualDocument)
        {
            assertEquals(expectedDocument.getId(), actualDocument.getId());
            assertEquals(expectedDocument.getSystemName(), actualDocument.getSystemName());
            assertEquals(expectedDocument.getUrl(), actualDocument.getUrl());
        }
    }

    @Nested
    class BuildApiPlanDto
    {
        @Test
        @DisplayName("ApiPlanDto with policies is build successfully")
        public void buildWithPolicies()
        {
            final AsyncBackToFrontApiVersion apiVersion = buildAsyncBackToFrontApiVersion(true);

            final String releaseName = RandomStringUtils.randomAlphanumeric(0, 50);
            final PlanProfile planProfile = new PlanProfile();

            final ApiMethodDto[] apiMethodDto = new ApiMethodDto[0];

            when(profilableDtoBuilder.buildApiMethodDtoArray(apiVersion, releaseName, planProfile))
                    .thenReturn(apiMethodDto);

            final ApiPlanDto apiPlanDto = asyncBackToFrontDtoBuilderModalityBased.buildApiPlanDto(apiVersion, releaseName, planProfile);

            assertEquals(apiVersion.getApi().getName() + ":" + apiVersion.getVersion(), apiPlanDto.getName());
            assertTrue(apiPlanDto.getPolicies());

            verify(profilableDtoBuilder).buildApiMethodDtoArray(apiVersion, releaseName, planProfile);
        }

        @Test
        @DisplayName("ApiPlanDto without policies is build successfully")
        public void buildWithoutPolicies()
        {
            final AsyncBackToFrontApiVersion apiVersion = buildAsyncBackToFrontApiVersion(false);

            final String releaseName = RandomStringUtils.randomAlphanumeric(0, 50);
            final PlanProfile planProfile = new PlanProfile();

            final ApiMethodDto[] apiMethodDto = new ApiMethodDto[0];

            when(profilableDtoBuilder.buildApiMethodDtoArray(any(), any(), any()))
                    .thenReturn(apiMethodDto);

            final ApiPlanDto apiPlanDto = asyncBackToFrontDtoBuilderModalityBased.buildApiPlanDto(apiVersion, releaseName, planProfile);

            assertEquals(apiVersion.getApi().getName() + ":" + apiVersion.getVersion(), apiPlanDto.getName());
            assertFalse(apiPlanDto.getPolicies());

            verify(profilableDtoBuilder).buildApiMethodDtoArray(apiVersion, releaseName, planProfile);
        }

        private AsyncBackToFrontApiVersion buildAsyncBackToFrontApiVersion(final boolean apiWithPolicies)
        {
            final AsyncBackToFrontApi api = new AsyncBackToFrontApi();
            api.setName(RandomStringUtils.randomAlphanumeric(25));

            if (apiWithPolicies)
            {
                api.setPolicyStatus(ApiPolicyStatus.ESTABLISHED);
            }
            else
            {
                final List<ApiPolicyStatus> notEstablishedPoliciesStatus = Stream.of(ApiPolicyStatus.values())
                        .filter(aps -> ApiPolicyStatus.ESTABLISHED != aps)
                        .collect(Collectors.toList());

                api.setPolicyStatus(notEstablishedPoliciesStatus.get(RandomUtils.nextInt(0, notEstablishedPoliciesStatus.size())));
            }

            final AsyncBackToFrontApiVersion apiVersion = new AsyncBackToFrontApiVersion();
            apiVersion.setVersion(RandomStringUtils.randomAlphanumeric(6));
            apiVersion.setApi(api);

            return apiVersion;
        }
    }

    @Nested
    class IsModalitySupported
    {
        @Test
        @DisplayName("Is modality supported -> ASYNC_BACKTOFRONT")
        public void backToFront()
        {
            assertTrue(asyncBackToFrontDtoBuilderModalityBased.isModalitySupported(ApiModality.ASYNC_BACKTOFRONT));
        }

        @ParameterizedTest
        @EnumSource(value = ApiModality.class, names = {"SYNC", "ASYNC_BACKTOBACK"})
        @DisplayName("Is modality supported -> false")
        public void others(ApiModality modality)
        {
            assertFalse(asyncBackToFrontDtoBuilderModalityBased.isModalitySupported(modality));
        }
    }

}
