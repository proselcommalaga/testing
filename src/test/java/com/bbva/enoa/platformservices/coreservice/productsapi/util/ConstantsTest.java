package com.bbva.enoa.platformservices.coreservice.productsapi.util;


import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertNotNull;

public class ConstantsTest
{
    @Test
    public void constructor() throws Exception
    {

        Constructor<Constants> constantsConstructor = Constants.class.getDeclaredConstructor();
        constantsConstructor.setAccessible(true);
        Constants constants = constantsConstructor.newInstance();
        assertNotNull(constants);
    }
}