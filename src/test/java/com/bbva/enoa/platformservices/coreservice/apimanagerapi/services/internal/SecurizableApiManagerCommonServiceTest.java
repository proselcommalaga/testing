package com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMApiDetailDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMPoliciesDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMPoliciesResponseDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiSecurityPolicy;
import com.bbva.enoa.datamodel.model.api.entities.ISecurizableApi;
import com.bbva.enoa.datamodel.model.api.entities.SecurityPolicy;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ApiSecurityPolicyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.SecurityPolicyRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.Utils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IApiGatewayManagerClient;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SecurizableApiManagerCommonServiceTest
{
    @Mock
    private IApiGatewayManagerClient apiGatewayManagerClient;
    @Mock
    private SecurityPolicyRepository securityPolicyRepository;
    @Mock
    private ApiSecurityPolicyRepository apiSecurityPolicyRepository;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @Mock
    private Utils utils;
    @InjectMocks
    private SecurizableApiManagerCommonService securizableApiManagerCommonService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                apiGatewayManagerClient,
                securityPolicyRepository,
                apiSecurityPolicyRepository,
                novaActivityEmitter,
                utils
        );
    }

    @Nested
    class AddPoliciesToApi
    {
        @DisplayName("Add policies to API -> ok")
        @Test
        public void ok()
        {
            var apiId = 909;
            var securityPolicyId = 2408;
            var apiName = "apiName";
            var uuaa = "JGMV";
            var basePath = "basePath";
            var env = Environment.LAB_INT;
            var policyName = "policyXXX";
            var policiesNameArray = Arrays.array(policyName);

            var securizableApi = Mockito.mock(ISecurizableApi.class);
            var securizableApiAsApi = Mockito.mock(Api.class);
            var policiesResponseDTO = Mockito.mock(AGMPoliciesResponseDTO.class);
            var policiesDTO = Mockito.mock(AGMPoliciesDTO.class);
            var securityPolicy = Mockito.mock(SecurityPolicy.class);

            when(securizableApi.getName()).thenReturn(apiName);
            when(securizableApi.getUuaa()).thenReturn(uuaa);
            when(securizableApi.getBasePathSwagger()).thenReturn(basePath);
            when(securizableApi.getId()).thenReturn(apiId);
            when(securizableApi.asApi()).thenReturn(securizableApiAsApi);
            when(policiesResponseDTO.getPolicies()).thenReturn(Arrays.array(policiesDTO));
            when(policiesDTO.getPolicies()).thenReturn(policiesNameArray);
            when(policiesDTO.getEnvironmnet()).thenReturn(env.getEnvironment());
            when(securityPolicy.getId()).thenReturn(securityPolicyId);

            when(apiGatewayManagerClient.getPolicies(any())).thenReturn(policiesResponseDTO);
            when(utils.streamOfNullable(any())).thenReturn(Stream.of(policyName));
            when(securityPolicyRepository.findByCode(any())).thenReturn(securityPolicy);
            when(apiSecurityPolicyRepository.save(any())).then(i -> i.getArgument(0));

            securizableApiManagerCommonService.addPoliciesToApi(securizableApi);

            var agmApiDetailDTOArgumentCaptor = ArgumentCaptor.forClass(AGMApiDetailDTO.class);
            var apiSecurityPolicyArgumentCaptor = ArgumentCaptor.forClass(ApiSecurityPolicy.class);
            verify(apiGatewayManagerClient).getPolicies(agmApiDetailDTOArgumentCaptor.capture());
            verify(utils).streamOfNullable(policiesNameArray);
            verify(securityPolicyRepository).findByCode(policyName);
            verify(apiSecurityPolicyRepository).save(apiSecurityPolicyArgumentCaptor.capture());

            AGMApiDetailDTO agmApiDetailDTOCaptured = agmApiDetailDTOArgumentCaptor.getValue();
            assertEquals(apiName, agmApiDetailDTOCaptured.getApiName());
            assertEquals(uuaa, agmApiDetailDTOCaptured.getUuaa());
            assertEquals(basePath, agmApiDetailDTOCaptured.getBasepath());
            ApiSecurityPolicy apiSecurityPolicy = apiSecurityPolicyArgumentCaptor.getValue();
            assertEquals(securityPolicy, apiSecurityPolicy.getSecurityPolicy());
            assertEquals(securizableApiAsApi, apiSecurityPolicy.getApi());
            assertEquals(apiId, apiSecurityPolicy.getId().getApiId());
            assertEquals(securityPolicyId, apiSecurityPolicy.getId().getSecurityPolicyId());
            assertEquals(env.getEnvironment(), apiSecurityPolicy.getId().getEnvironment());
        }

    }

    @Nested
    class EmitNewPoliciesActivityResponse
    {

        @ParameterizedTest(name = "[{index}] apiType:{0}, taskStatus:{1}")
        @ArgumentsSource(EmitNewPoliciesActivityResponseArgumentsProvider.class)
        @DisplayName("Emit new policies activity response -> ok")
        public void ok(ApiType apiType, ToDoTaskStatus taskStatus) throws JsonProcessingException
        {
            var productId = 2408;
            var apiId = 909;
            var apiName = "apiName";
            var basePath = "basePath";
            var securizableApi = Mockito.mock(ISecurizableApi.class, RETURNS_DEEP_STUBS);
            var activityAction = Mockito.mock(ActivityAction.class);

            when(securizableApi.getType()).thenReturn(apiType);
            when(securizableApi.getProduct().getId()).thenReturn(productId);
            when(securizableApi.getId()).thenReturn(apiId);
            when(securizableApi.getName()).thenReturn(apiName);
            when(securizableApi.getBasePathSwagger()).thenReturn(basePath);

            securizableApiManagerCommonService.emitNewPoliciesActivityResponse(taskStatus, securizableApi, activityAction);

            var genericActivityArgumentCaptor = ArgumentCaptor.forClass(GenericActivity.class);
            verify(novaActivityEmitter).emitNewActivity(genericActivityArgumentCaptor.capture());
            GenericActivity genericActivityCaptured = genericActivityArgumentCaptor.getValue();
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>(){};
            Map<String, Object> params = new ObjectMapper().readValue(genericActivityCaptured.getSerializedStringParams(), typeRef);
            assertEquals(productId, genericActivityCaptured.getProductId());
            assertEquals(activityAction, genericActivityCaptured.getAction());
            assertEquals(apiId, genericActivityCaptured.getEntityId());
            assertEquals(3, params.size());
            assertEquals(apiName, params.get("apiName"));
            assertEquals(basePath, params.get("basePathSwagger"));
            assertEquals(taskStatus.toString(), params.get("todoTaskStatus"));
            switch (apiType)
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

    static class EmitNewPoliciesActivityResponseArgumentsProvider implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception
        {
            return Stream.of(
                    Arguments.of(ApiType.NOT_GOVERNED, ToDoTaskStatus.DONE),
                    Arguments.of(ApiType.NOT_GOVERNED, ToDoTaskStatus.ERROR),
                    Arguments.of(ApiType.NOT_GOVERNED, ToDoTaskStatus.PENDING),
                    Arguments.of(ApiType.NOT_GOVERNED, ToDoTaskStatus.PENDING_ERROR),
                    Arguments.of(ApiType.NOT_GOVERNED, ToDoTaskStatus.REJECTED),
                    Arguments.of(ApiType.GOVERNED, ToDoTaskStatus.DONE),
                    Arguments.of(ApiType.GOVERNED, ToDoTaskStatus.ERROR),
                    Arguments.of(ApiType.GOVERNED, ToDoTaskStatus.PENDING),
                    Arguments.of(ApiType.GOVERNED, ToDoTaskStatus.PENDING_ERROR),
                    Arguments.of(ApiType.GOVERNED, ToDoTaskStatus.REJECTED),
                    Arguments.of(ApiType.EXTERNAL, ToDoTaskStatus.DONE),
                    Arguments.of(ApiType.EXTERNAL, ToDoTaskStatus.ERROR),
                    Arguments.of(ApiType.EXTERNAL, ToDoTaskStatus.PENDING),
                    Arguments.of(ApiType.EXTERNAL, ToDoTaskStatus.PENDING_ERROR),
                    Arguments.of(ApiType.EXTERNAL, ToDoTaskStatus.REJECTED)
            );
        }
    }


}
