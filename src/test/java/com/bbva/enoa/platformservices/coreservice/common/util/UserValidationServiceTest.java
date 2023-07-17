package com.bbva.enoa.platformservices.coreservice.common.util;
/*
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class UserValidationServiceTest
{

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    @Mock
    private IProductUsersClient usersClient;
    @InjectMocks
    private UserValidationService userValidationService;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getUser() throws Exception
    {
        //Given
        USUserDTO usUser = new USUserDTO();
        usUser.setUserCode("CODE");
        when(this.usersClient.getUser(any(), any(Errors.class))).thenReturn(usUser);

        //When
        this.userValidationService.getUser("CODE", t ->
        {
            throw new NovaException(ProductsAPIError.getUnexpectedError(), t);
        }).orElseThrow(() ->
                new NovaException(ProductsAPIError.getUserCodeDoesNotExistError(), "Error"));

        //Then
        assertEquals("CODE", usUser.getUserCode());

        //Given
        final Errors errors = new Errors() ;

        final List<ErrorMessage> messages = new ArrayList<>() ;
        messages.add(new ErrorMessage("CODE")) ;

        errors.setMessages(messages) ;

        when(this.usersClient.getUser(any(), any(Errors.class))).thenThrow(errors);

        //Then
        exception.expect(NovaException.class);
        this.userValidationService.getUser("CODE", t ->
        {
            throw new NovaException(ProductsAPIError.getUnexpectedError(), t);
        }).orElseThrow(() ->
                new NovaException(ProductsAPIError.getUserCodeDoesNotExistError(), "Error"));

    }

    @Test
    public void getProductsByUser()
    {
        //Given
        when(this.usersClient.getProductsByUser(any())).thenReturn(new int[]{1, 2});

        //When
        int[] response = this.userValidationService.getProductsByUser("CODE", t ->
        {
            throw new NovaException(ProductsAPIError.getUnexpectedError(), t);
        }).orElseThrow(() ->
                new NovaException(ProductsAPIError.getUserCodeDoesNotExistError(), "Error"));

        //Then
        assertEquals(1, response[0]);
        assertEquals(2, response[1]);

        //Given
        when(this.usersClient.getProductsByUser("CODE")).thenThrow(new NovaException(ProductsAPIError.getUnexpectedError(), "Error"));

        //Then
        exception.expect(NovaException.class);
        int[] response2 = this.userValidationService.getProductsByUser("CODE", t ->
        {
            throw new NovaException(ProductsAPIError.getUnexpectedError(), t);
        }).orElseThrow(() ->
                new NovaException(ProductsAPIError.getUserCodeDoesNotExistError(), "Error"));
    }

    @Test
    public void validateAndGet() throws Exception
    {
        //Given
        USUserDTO usUser = new USUserDTO();
        usUser.setUserCode("CODE");
        when(this.usersClient.getUser(any(), any(Errors.class))).thenReturn(usUser);

        //When
        USUserDTO response = this.userValidationService.validateAndGet("CODE", new RuntimeException());

        //Then
        assertEquals("CODE", response.getUserCode());

        //Given
        when(this.usersClient.getUser(any(), any(Errors.class))).thenReturn(null);

        exception.expect(NovaException.class);
        USUserDTO response2 = this.userValidationService.validateAndGet("CODE", new NovaException(ProductsAPIError.getUnexpectedError(), "Error"));
    }
}
*/
