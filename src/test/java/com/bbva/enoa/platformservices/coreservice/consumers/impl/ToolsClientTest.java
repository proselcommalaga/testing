package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.toolsapi.client.feign.nova.rest.IRestHandlerToolsapi;
import com.bbva.enoa.apirestgen.toolsapi.model.TOProductUserDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemsCombinationDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
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
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ToolsClientTest
{
    @Mock
    private IRestHandlerToolsapi restHandler;
    @Mock
    private Logger log;
    @InjectMocks
    private ToolsClient client;

    @BeforeEach
    public void before() throws Exception
    {
        MockitoAnnotations.initMocks(ToolsClient.class);
        this.client.initRestHandler();

        Field field = client.getClass().getDeclaredField("LOG");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(client, log);
    }

    @Test
    public void when_add_external_tools_to_product_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.addTools(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.addExternalToolsToProduct(3));
    }

    @Test
    public void when_add_external_tools_to_product_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.addTools(Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.addExternalToolsToProduct(3);

        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_remove_external_tools_from_product_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.removeTools(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.removeExternalToolsFromProduct(3));
    }

    @Test
    public void when_remove_external_tools_from_product_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.removeTools(Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.removeExternalToolsFromProduct(3);

        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_add_user_tool_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.addUserTool(Mockito.any(TOProductUserDTO.class))).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.addUserTool(DummyConsumerDataGenerator.getDummyTOProductUserDto()));
    }

    @Test
    public void when_add_user_tool_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.addUserTool(Mockito.any(TOProductUserDTO.class))).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.addUserTool(DummyConsumerDataGenerator.getDummyTOProductUserDto());

        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_remove_user_tool_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.removeUserTool(Mockito.any(TOProductUserDTO.class), Mockito.anyBoolean())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.removeUserTool(DummyConsumerDataGenerator.getDummyTOProductUserDto(), false));
    }

    @Test
    public void when_remove_user_tool_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.removeUserTool(Mockito.any(TOProductUserDTO.class), Mockito.anyBoolean())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.removeUserTool(DummyConsumerDataGenerator.getDummyTOProductUserDto(), false);

        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_get_subsystem_by_id_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getSubsystemById(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getSubsystemById(1));
    }

    @Test
    public void when_get_subsystem_by_id_returns_ok_response_then_log_debug()
    {
        TOSubsystemDTO dto = DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.NOVA);
        Mockito.when(restHandler.getSubsystemById(Mockito.anyInt())).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        TOSubsystemDTO result = client.getSubsystemById(1);

        Assertions.assertEquals(dto, result);
    }

    @Test
    public void when_get_subsystem_by_repository_id_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getSubsystemByRepositoryId(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getSubsystemByRepositoryId(1));
    }

    @Test
    public void when_get_subsystem_by_repository_id_returns_ok_response_then_log_debug()
    {
        TOSubsystemDTO dto = DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.NOVA);
        Mockito.when(restHandler.getSubsystemByRepositoryId(Mockito.anyInt())).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        TOSubsystemDTO result = client.getSubsystemByRepositoryId(1);

        Assertions.assertEquals(dto, result);
    }

    @Test
    public void when_get_subsystem_by_product_and_name_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getSubsystemByNameAndProduct(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getSubsystemByProductAndName("A", 1));
    }

    @Test
    public void when_get_subsystem_by_product_and_name_returns_ok_response_then_log_debug()
    {
        TOSubsystemDTO dto = DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.NOVA);
        Mockito.when(restHandler.getSubsystemByNameAndProduct(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        TOSubsystemDTO result = client.getSubsystemByProductAndName("A", 1);

        Assertions.assertEquals(dto, result);
    }

    @Test
    public void when_get_product_subsystems_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getSubsystems(Mockito.anyInt(), Mockito.anyBoolean())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getProductSubsystems(1, false));
    }

    @Test
    public void when_get_product_subsystems_returns_ok_response_then_log_debug()
    {
        TOSubsystemDTO[] dtos = new TOSubsystemDTO[]{DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.NOVA)};
        Mockito.when(restHandler.getSubsystems(Mockito.anyInt(), Mockito.anyBoolean())).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        List<TOSubsystemDTO> result = client.getProductSubsystems(1, false);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dtos[0], result.get(0));
    }

    @Test
    public void when_get_all_subsystems_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getAllSubsystems()).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getAllSubsystems());
    }

    @Test
    public void when_get_all_subsystems_returns_ok_response_then_log_debug()
    {
        TOSubsystemDTO[] dtos = new TOSubsystemDTO[]{DummyConsumerDataGenerator.getDummyTOSubystemDTO(SubsystemType.NOVA)};
        Mockito.when(restHandler.getAllSubsystems()).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        List<TOSubsystemDTO> result = client.getAllSubsystems();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(dtos[0], result.get(0));
    }

    @Test
    public void when_get_subsystems_history_snapshot_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getSubsystemsHistorySnapshot()).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getSubsystemsHistorySnapshot());
    }

    @Test
    public void when_get_subsystems_history_snapshot_returns_ok_response_then_log_debug()
    {
        TOSubsystemsCombinationDTO[] dtos = DummyConsumerDataGenerator.getDummyTOSubsystemsCombinationDtos();
        Mockito.when(restHandler.getSubsystemsHistorySnapshot()).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        TOSubsystemsCombinationDTO[] result = client.getSubsystemsHistorySnapshot();

        Assertions.assertEquals(1, result.length);
        Assertions.assertEquals(dtos[0], result[0]);
    }
}