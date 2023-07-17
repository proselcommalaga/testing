package com.bbva.enoa.platformservices.coreservice.servicerunnerapi.utils;


import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertNotNull;


public class ServiceRunnerImplConstantsTest
{
    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void constructor() throws Exception
    {
        Constructor<ServiceRunnerConstants> constantsConstructor = ServiceRunnerConstants.class.getDeclaredConstructor();
        constantsConstructor.setAccessible(true);
        ServiceRunnerConstants constants = constantsConstructor.newInstance();
        assertNotNull(constants);
    }

}