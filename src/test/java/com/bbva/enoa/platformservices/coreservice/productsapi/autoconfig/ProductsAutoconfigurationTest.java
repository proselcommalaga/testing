package com.bbva.enoa.platformservices.coreservice.productsapi.autoconfig;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.Charset;
import java.util.Random;

public class ProductsAutoconfigurationTest
{

    @InjectMocks
    private ProductsAutoconfiguration autoconfig;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }


    private byte[] generateRandomCharset(Integer length){

        byte[] charsetRandom = new byte[length]; // length is bounded by length
        new Random().nextBytes(charsetRandom);
        return charsetRandom;
    }
    /**
     * @return
     */
    private ProductProperties generateProperties()
    {

        ProductProperties proProperties = new ProductProperties();
        String generatedString = new String(this.generateRandomCharset(7), Charset.forName("UTF-8"));

        proProperties.setCdpInt(generatedString);
        proProperties.setCdpPre(generatedString);
        proProperties.setCdpPro(generatedString);

        return proProperties;
    }

    private Exception generateException(){

        Exception exception = new Exception("Esta excepcion es utilizada para el mockeo en los test");

        return exception;
    }

    @Test
    public void testPostConstruct()
    {

        // Generate mock properties
        ProductProperties proProperties = this.generateProperties();

        // Set autoconfig bean value
        ReflectionTestUtils.setField(this.autoconfig, "properties", proProperties, ProductProperties.class);

        // Call
        this.autoconfig.postConstruct();


    }

    @Test
    public void testPostConstruct_noProperties()
    {
        // Genera propiedades para Mock testing vacias
        ProductProperties productProperties = new ProductProperties();

        ReflectionTestUtils.setField(this.autoconfig, "properties", productProperties, ProductProperties.class);

        // Call method to test
        this.autoconfig.postConstruct();

    }

    @Test
    public void testPostConstruct_emptyProperties(){
        // Case not properties bean set
        Assertions.assertThrows(java.lang.NullPointerException.class, ()-> this.autoconfig.postConstruct());
    }
}
