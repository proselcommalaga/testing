package com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConstantsTest
{
    @Test
    void constructor() throws Exception
    {
        Constructor<Constants> constantsConstructor = Constants.class.getDeclaredConstructor();
        constantsConstructor.setAccessible(true);
        Constants constants = constantsConstructor.newInstance();
        assertNotNull(constants);
    }

    @Test
    void constructorApiManagerErrors() throws Exception
    {
        Constructor<Constants.ApiManagerErrors> constantsConstructor = Constants.ApiManagerErrors.class.getDeclaredConstructor();
        constantsConstructor.setAccessible(true);
        Constants.ApiManagerErrors constants = constantsConstructor.newInstance();
        assertNotNull(constants);
    }


}