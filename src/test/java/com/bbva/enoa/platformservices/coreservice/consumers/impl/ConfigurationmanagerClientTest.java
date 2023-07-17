package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.configurationmanagerapi.client.feign.nova.rest.IRestHandlerConfigurationmanagerapi;
import com.bbva.enoa.apirestgen.configurationmanagerapi.model.CMLogicalConnectorPropertyDto;
import com.bbva.enoa.apirestgen.configurationmanagerapi.model.PropertiesFileRequest;
import com.bbva.enoa.apirestgen.configurationmanagerapi.model.PropertiesRequest;
import com.bbva.enoa.platformservices.coreservice.common.model.param.ServiceOperationParams;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator.getCMLogicalConnectorPropertyDtos;
import static com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator.getDummyDeploymentPlan;
import static com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator.getDummyErrors;
import static com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator.getDummyExtraProperties;
import static com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator.getDummyLibraries;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by xe56809 on 13/03/2018.
 */
@ExtendWith(MockitoExtension.class)
public class ConfigurationmanagerClientTest
{
    private static final String DUMMY_STRING = "A";
    private static final int DUMMY_INT = 1;
    private final NovaMetadata metadata = new NovaMetadata();
    private final ServiceOperationParams params = (new ServiceOperationParams.ServiceOperationParamsBuilder()).modulePath("A")
            .serviceType(DUMMY_STRING).releaseVersionServiceId(DUMMY_INT).releaseVersionServiceName(DUMMY_STRING)
            .versionControlServiceId(DUMMY_INT).tag(DUMMY_STRING).extraProperties(getDummyExtraProperties())
            .libraries(getDummyLibraries()).ivUser(DUMMY_STRING).build();
    @Mock
    private IRestHandlerConfigurationmanagerapi iRestHandlerConfigurationmanagerapi;
    @InjectMocks
    private ConfigurationmanagerClient configurationmanagerClient;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(ConfigurationmanagerClient.class);
        this.configurationmanagerClient.init();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", Collections.singletonList("ivUser"));
        metadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));
    }

    @Test
    public void when_call_save_current_configuration_revision_request_has_response_then_return_response_body() throws NoSuchFieldException, IllegalAccessException
    {
        Logger logger = Mockito.mock(Logger.class);
        Field log = ConfigurationmanagerClient.class.getDeclaredField("LOG");
        log.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(log, log.getModifiers() & ~Modifier.FINAL);
        log.set(configurationmanagerClient, logger);
        Mockito.when(this.iRestHandlerConfigurationmanagerapi.saveCurrentConfigurationRevision(Mockito.anyInt())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        configurationmanagerClient.saveCurrentConfigurationRevision(1);

        Mockito.verify(logger, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    public void when_call_save_current_configuration_revision_request_has_error_then_throw_exception()
    {
        Errors errors = getDummyErrors();
        Mockito.when(this.iRestHandlerConfigurationmanagerapi.saveCurrentConfigurationRevision(Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> configurationmanagerClient.saveCurrentConfigurationRevision(1));
    }

    @Test
    public void when_call_get_logical_connector_properties_request_has_response_then_return_response_body()
    {
        CMLogicalConnectorPropertyDto[] dtos = getCMLogicalConnectorPropertyDtos();
        Mockito.when(this.iRestHandlerConfigurationmanagerapi.getLogicalConnectorProperties(Mockito.anyInt())).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        CMLogicalConnectorPropertyDto[] result = configurationmanagerClient.getLogicalConnectorPropertiesDto(1);

        Assertions.assertEquals(1, result.length);
        CMLogicalConnectorPropertyDto firstResult = result[0];
        CMLogicalConnectorPropertyDto firstDto = dtos[0];
        Assertions.assertEquals(firstDto.getId(), firstResult.getId());
        Assertions.assertEquals(firstDto.getLogicalConnectorId(), firstResult.getLogicalConnectorId());
        Assertions.assertEquals(firstDto.getInaccessible(), firstResult.getInaccessible());
        Assertions.assertEquals(firstDto.getPropertyName(), firstResult.getPropertyName());
        Assertions.assertEquals(firstDto.getPropertyDescription(), firstResult.getPropertyDescription());
        Assertions.assertEquals(firstDto.getPropertyDefaultName(), firstResult.getPropertyDefaultName());
        Assertions.assertEquals(firstDto.getPropertyManagement(), firstResult.getPropertyManagement());
        Assertions.assertEquals(firstDto.getPropertyScope(), firstResult.getPropertyScope());
        Assertions.assertEquals(firstDto.getPropertySecurity(), firstResult.getPropertySecurity());
        Assertions.assertEquals(firstDto.getPropertyType(), firstResult.getPropertyType());
        Assertions.assertEquals(firstDto.getPropertyValue(), firstResult.getPropertyValue());
    }

    @Test
    public void when_call_get_logical_connector_properties_request_has_error_then_return_empty_response()
    {
        Errors errors = getDummyErrors();
        Mockito.when(this.iRestHandlerConfigurationmanagerapi.getLogicalConnectorProperties(Mockito.anyInt())).thenReturn(new ResponseEntity(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        CMLogicalConnectorPropertyDto[] result = configurationmanagerClient.getLogicalConnectorPropertiesDto(1);

        Assertions.assertEquals(0, result.length);
    }

    @Test
    public void validateServiceTemplateFile()
    {
        //Given
        when(this.iRestHandlerConfigurationmanagerapi.validateServicePropertiesFile(any())).
                thenReturn(new ResponseEntity<>(HttpStatus.OK));

        //Then
        boolean response = this.configurationmanagerClient.validateServiceTemplateFile("Path", "tag", 1, "Type", "User");
        assertTrue(response);

        //Given
        when(this.iRestHandlerConfigurationmanagerapi.validateServicePropertiesFile(any())).
                thenReturn(new ResponseEntity<>(getDummyErrors(), HttpStatus.BAD_REQUEST));

        //Then
        boolean response2 = this.configurationmanagerClient.validateServiceTemplateFile("Path", "tag", 1, "Type", "User");
        assertFalse(response2);
    }

    @Test
    public void when_call_delete_current_configuration_revision_request_has_response_then_return_true()
    {
        Mockito.when(this.iRestHandlerConfigurationmanagerapi.deleteCurrentConfigurationRevision(Mockito.anyInt())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        boolean result = configurationmanagerClient.deleteCurrentConfigurationRevision(getDummyDeploymentPlan());

        assertTrue(result);
    }

    @Test
    public void when_call_delete_current_configuration_revision_request_has_errors_then_return_false()
    {
        Errors errors = getDummyErrors();
        Mockito.when(this.iRestHandlerConfigurationmanagerapi.deleteCurrentConfigurationRevision(Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        boolean result = configurationmanagerClient.deleteCurrentConfigurationRevision(getDummyDeploymentPlan());

        assertFalse(result);
    }

    @Test
    public void when_call_validate_service_template_file_request_has_response_then_return_true()
    {
        Mockito.when(this.iRestHandlerConfigurationmanagerapi.validateServicePropertiesFile(any(PropertiesFileRequest.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        boolean result = configurationmanagerClient.validateServiceTemplateFile("A", "A", 1, "A", "A");

        assertTrue(result);
    }

    @Test
    public void when_call_validate_service_template_file_request_has_errors_then_return_false()
    {
        Errors errors = getDummyErrors();
        Mockito.when(this.iRestHandlerConfigurationmanagerapi.validateServicePropertiesFile(any(PropertiesFileRequest.class))).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        boolean result = configurationmanagerClient.validateServiceTemplateFile("A", "A", 1, "A", "A");

        assertFalse(result);
    }

    @Test
    public void when_call_set_properties_definition_service_request_has_response_then_return_response() throws NoSuchFieldException, IllegalAccessException
    {
        Logger logger = Mockito.mock(Logger.class);
        Field log = ConfigurationmanagerClient.class.getDeclaredField("LOG");
        log.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(log, log.getModifiers() & ~Modifier.FINAL);
        log.set(configurationmanagerClient, logger);
        Mockito.when(this.iRestHandlerConfigurationmanagerapi.setPropertiesDefinitionService(any(PropertiesRequest.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        configurationmanagerClient.setPropertiesDefinitionService(params);

        Mockito.verify(logger, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), any(PropertiesRequest.class));
    }

    @Test
    public void when_call_set_properties_definition_service_request_has_errors_then_return_false() throws IllegalAccessException, NoSuchFieldException
    {
        Logger logger = Mockito.mock(Logger.class);
        Field log = ConfigurationmanagerClient.class.getDeclaredField("LOG");
        log.setAccessible(true);
        Field modifiers = Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(log, log.getModifiers() & ~Modifier.FINAL);
        log.set(configurationmanagerClient, logger);
        Errors errors = getDummyErrors();
        Mockito.when(this.iRestHandlerConfigurationmanagerapi.setPropertiesDefinitionService(any(PropertiesRequest.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        configurationmanagerClient.setPropertiesDefinitionService(params);

        Mockito.verify(logger, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), any(PropertiesRequest.class), any());
    }
}