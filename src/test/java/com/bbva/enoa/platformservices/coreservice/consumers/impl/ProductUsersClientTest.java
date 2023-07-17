package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.usersapi.client.feign.nova.rest.IRestHandlerUsersapi;
import com.bbva.enoa.apirestgen.usersapi.model.USProductUserDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
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
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ProductUsersClientTest
{
    private static final String VALIDATE_TEAM_USER_ERROR = "USER-006";
    public static final String USER_NOT_IN_TEAM_ROLE_ERROR_CODE = "PRODUCTS-028";
    public static final String USERS_API_INVOCATION_ERROR_CODE = "PRODUCTS-019";
    @Mock
    private IRestHandlerUsersapi restHandler;
    @Mock
    private Logger log;
    @InjectMocks
    private ProductUsersClient client;

    @BeforeEach
    public void init() throws IllegalAccessException, NoSuchFieldException
    {
        MockitoAnnotations.initMocks(ProductUsersClient.class);
        client.init();

        Field field = client.getClass().getDeclaredField("LOG");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(client, log);
    }

    @Test
    public void when_delete_product_users_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.deleteAllUsersFromProduct(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.deleteProductUsers("A", 1));
    }

    @Test
    public void when_delete_product_users_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.deleteAllUsersFromProduct(Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.deleteProductUsers("A", 1);

        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_get_product_users_by_team_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getProductUsersByTeam(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getProductUsersByTeam(1, "A"));
    }

    @Test
    public void when_get_product_users_by_team_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getProductUsersByTeam(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyUSUserDtos(), HttpStatus.OK));

        List<USUserDTO> result = client.getProductUsersByTeam(1, "A");

        Assertions.assertEquals(1, result.size());
        USUserDTO firstResult = result.get(0);
        Assertions.assertEquals("USERNAME", firstResult.getUserName());
        Assertions.assertEquals("SURNAME1", firstResult.getSurname1());
        Assertions.assertEquals("SURNAME2", firstResult.getSurname2());
        Assertions.assertTrue(firstResult.getActive());
        Assertions.assertEquals("USERCODE", firstResult.getUserCode());
        Assertions.assertEquals("EMAIL", firstResult.getEmail());
        String[] teams = firstResult.getTeams();
        Assertions.assertEquals(2, teams.length);
        Assertions.assertEquals("TEAM1", teams[0]);
        Assertions.assertEquals("TEAM2", teams[1]);
    }

    @Test
    public void when_add_user_to_product_returns_ko_response_with_validate_response_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(VALIDATE_TEAM_USER_ERROR);
        Mockito.when(restHandler.createProductMember(Mockito.any(USProductUserDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.addUserToProduct(DummyConsumerDataGenerator.getDummyProductUserDto(), "A", 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(USER_NOT_IN_TEAM_ROLE_ERROR_CODE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_add_user_to_product_returns_ko_response_with_other_error_code_then_throw_generic_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(USER_NOT_IN_TEAM_ROLE_ERROR_CODE);
        Mockito.when(restHandler.createProductMember(Mockito.any(USProductUserDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.addUserToProduct(DummyConsumerDataGenerator.getDummyProductUserDto(), "A", 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(USERS_API_INVOCATION_ERROR_CODE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_add_user_to_product_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.createProductMember(Mockito.any(USProductUserDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.addUserToProduct(DummyConsumerDataGenerator.getDummyProductUserDto(), "A", 1);

        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_remove_user_from_product_returns_ko_response_with_user_is_unique_produt_owner_response_error_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(ProductsAPIError.USER_IS_UNIQUE_PRODUCT_OWNER_ERROR_CODE);
        Mockito.when(restHandler.deleteUserFromProduct(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.removeUserFromProduct("A", 1, DummyConsumerDataGenerator.getDummyProductUserDto());
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(ProductsAPIError.USER_IS_UNIQUE_PRODUCT_OWNER_ERROR_CODE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_remove_user_from_product_returns_ko_response_with_other_error_code_then_throw_generic_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(USER_NOT_IN_TEAM_ROLE_ERROR_CODE);
        Mockito.when(restHandler.deleteUserFromProduct(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.removeUserFromProduct("A", 1, DummyConsumerDataGenerator.getDummyProductUserDto());
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(USERS_API_INVOCATION_ERROR_CODE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_remove_user_from_product_returns_ko_response_without_error_message_then_throw_generic_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.setMessages(Collections.emptyList());
        Mockito.when(restHandler.deleteUserFromProduct(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.removeUserFromProduct("A", 1, DummyConsumerDataGenerator.getDummyProductUserDto());
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(USERS_API_INVOCATION_ERROR_CODE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_remove_user_from_product_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.deleteUserFromProduct(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.removeUserFromProduct("A", 1, DummyConsumerDataGenerator.getDummyProductUserDto());

        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_get_products_by_user_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getProductsByUser(Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getProductsByUser("A"));
    }

    @Test
    public void when_get_products_by_user_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getProductsByUser(Mockito.anyString())).thenReturn(new ResponseEntity<>(new int[]{1, 2, 3}, HttpStatus.OK));

        int[] result = client.getProductsByUser("A");

        Assertions.assertEquals(3, result.length);
        Assertions.assertEquals(1, result[0]);
        Assertions.assertEquals(2, result[1]);
        Assertions.assertEquals(3, result[2]);
    }
}