package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.client.feign.nova.rest.IRestHandlerApigatewaymanagerapi;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMApiDetailDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMCreateProfilingDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMCreatePublicationDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMDockerServiceDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMDockerValidationDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMPoliciesDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMPoliciesResponseDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMRegisterApiDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMRemoveApiDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMRemoveProfilingDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMRemovePublicationDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMUpdatePublicationDTO;
import com.bbva.enoa.core.novaheaderserver.context.NovaRequestContext;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
class ApiGatewayManagerClientImplTest
{
    private static final String USER = "USER";
    private static final String PASSWORD = "PASSWORD";
    @Mock
    private NovaRequestContext context;
    @Mock
    private IRestHandlerApigatewaymanagerapi restHandler;
    @InjectMocks
    private ApiGatewayManagerClientImpl client;

    @BeforeEach
    public void setUp()
    {

        MockitoAnnotations.initMocks(ApiGatewayManagerClientImpl.class);
        this.client.init();
        ReflectionTestUtils.setField(this.client, "novaUser", USER);
        ReflectionTestUtils.setField(this.client, "novaPassword", PASSWORD);
    }

    @Test
    public void when_call_docker_key_generation_request_has_response_then_return_response_body()
    {
        AGMDockerValidationDTO[] dtos = getAGMDockerValidationDtos();
        Mockito.when(restHandler.generateDockerKey(Mockito.any(), Mockito.anyString())).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        AGMDockerValidationDTO[] result = client.generateDockerKey(new AGMDockerServiceDTO[0], "A");

        Assertions.assertEquals(1, result.length);
        AGMDockerValidationDTO firstResult = result[0];
        AGMDockerValidationDTO firstDto = dtos[0];
        Assertions.assertEquals(firstDto.getDockerKey(), firstResult.getDockerKey());
        Assertions.assertEquals(firstDto.getServiceName(), firstResult.getServiceName());
        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    private AGMDockerValidationDTO[] getAGMDockerValidationDtos()
    {
        AGMDockerValidationDTO dto = new AGMDockerValidationDTO();
        dto.setDockerKey("DOCKER");
        dto.setServiceName("SERVICE");
        return new AGMDockerValidationDTO[]{dto};
    }

    @Test
    public void when_call_docker_key_generation_request_has_error_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        Mockito.when(restHandler.generateDockerKey(Mockito.any(), Mockito.anyString())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.generateDockerKey(new AGMDockerServiceDTO[0], "A"));

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_create_publication_request_has_response_then_return_response_body()
    {
        Mockito.when(restHandler.createPublication(Mockito.any(AGMCreatePublicationDTO.class), Mockito.anyString())).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        client.createPublication(new AGMCreatePublicationDTO(), "A");

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_create_publication_request_has_error_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        Mockito.when(restHandler.createPublication(Mockito.any(AGMCreatePublicationDTO.class), Mockito.anyString())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.createPublication(new AGMCreatePublicationDTO(), "A"));

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_remove_publication_request_has_response_then_return_response_body()
    {
        Mockito.when(restHandler.removePublication(Mockito.any(AGMRemovePublicationDTO.class), Mockito.anyString())).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        client.removePublication(new AGMRemovePublicationDTO(), "A");

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_remove_publication_request_has_error_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        Mockito.when(restHandler.removePublication(Mockito.any(AGMRemovePublicationDTO.class), Mockito.anyString())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.removePublication(new AGMRemovePublicationDTO(), "A"));

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_update_publication_request_has_response_then_return_response_body()
    {
        Mockito.when(restHandler.updatePublication(Mockito.any(AGMUpdatePublicationDTO.class), Mockito.anyString())).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        client.updatePublication(new AGMUpdatePublicationDTO(), "A");

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_update_publication_request_has_error_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        Mockito.when(restHandler.updatePublication(Mockito.any(AGMUpdatePublicationDTO.class), Mockito.anyString())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.updatePublication(new AGMUpdatePublicationDTO(), "A"));

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_create_register_request_has_response_then_return_response_body()
    {
        Mockito.when(restHandler.createRegister(Mockito.any(AGMRegisterApiDTO.class))).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        client.createRegister(new AGMRegisterApiDTO());

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_create_register_request_has_error_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        Mockito.when(restHandler.createRegister(Mockito.any(AGMRegisterApiDTO.class))).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.createRegister(new AGMRegisterApiDTO()));

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_remove_register_request_has_response_then_return_response_body()
    {
        Mockito.when(restHandler.removeRegister(Mockito.any(AGMRemoveApiDTO.class))).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        client.removeRegister(new AGMRemoveApiDTO());

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_remove_register_request_has_error_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        Mockito.when(restHandler.removeRegister(Mockito.any(AGMRemoveApiDTO.class))).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.removeRegister(new AGMRemoveApiDTO()));

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_get_policies_request_has_response_then_return_response_body()
    {
        AGMPoliciesResponseDTO dto = DummyConsumerDataGenerator.getDummyAGMPoliciesResponseDto();
        Mockito.when(restHandler.getPolicies(Mockito.any(AGMApiDetailDTO.class))).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        AGMPoliciesResponseDTO result = client.getPolicies(new AGMApiDetailDTO());

        AGMPoliciesDTO[] dtoPolicies = dto.getPolicies();
        AGMPoliciesDTO[] resultPolicies = result.getPolicies();
        Assertions.assertEquals(dtoPolicies.length, resultPolicies.length);
        for (int i = 0; i < resultPolicies.length; i++)
        {
            AGMPoliciesDTO dtoPolicy = dtoPolicies[i];
            AGMPoliciesDTO resultPolicy = resultPolicies[i];
            Assertions.assertEquals(dtoPolicy.getEnvironmnet(), resultPolicy.getEnvironmnet());
            Assertions.assertEquals(Arrays.asList(dtoPolicy.getPolicies()), Arrays.asList(resultPolicy.getPolicies()));
        }
        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_get_policies_request_has_error_then_return_empty_response()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        Mockito.when(restHandler.getPolicies(Mockito.any(AGMApiDetailDTO.class))).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        AGMPoliciesResponseDTO result = client.getPolicies(new AGMRemoveApiDTO());

        Assertions.assertEquals(0, result.getPolicies().length);
        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_get_roles_request_has_response_then_return_response_body()
    {
        String[] roles = new String[]{"A", "B"};
        Mockito.when(restHandler.getRoles(Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity<>(roles, HttpStatus.OK));

        String[] result = client.getRoles("A", "B");

        Assertions.assertEquals(Arrays.asList(roles), Arrays.asList(result));
        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_get_roles_request_has_error_then_return_empty_response()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        Mockito.when(restHandler.getRoles(Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getRoles("A", "B"));

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_create_profiling_request_has_response_then_return_response_body()
    {
        Mockito.when(restHandler.createProfiling(Mockito.any(AGMCreateProfilingDTO.class), Mockito.anyString())).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        client.createProfiling(new AGMCreateProfilingDTO(), "A");

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_create_profiling_request_has_error_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        Mockito.when(restHandler.createProfiling(Mockito.any(AGMCreateProfilingDTO.class), Mockito.anyString())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.createProfiling(new AGMCreateProfilingDTO(), "A"));

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_remove_profiling_request_has_response_then_return_response_body()
    {
        Mockito.when(restHandler.removeProfiling(Mockito.any(AGMRemoveProfilingDTO.class), Mockito.anyString())).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        client.removeProfiling(new AGMRemoveProfilingDTO(), "A");

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }

    @Test
    public void when_call_remove_profiling_request_has_error_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        Mockito.when(restHandler.removeProfiling(Mockito.any(AGMRemoveProfilingDTO.class), Mockito.anyString())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.removeProfiling(new AGMRemoveProfilingDTO(), "A"));

        Mockito.verify(context, Mockito.times(1)).setHeader(Constants.AUTHORIZATION_HEADER, null);
    }
}
