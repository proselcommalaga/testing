package com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertNotNull;

public class DeploymentConstantsTest
{
    @Test
    public void constructor() throws Exception
    {

        Constructor<DeploymentConstants> constantsConstructor = DeploymentConstants.class.getDeclaredConstructor();
        constantsConstructor.setAccessible(true);
        DeploymentConstants constants = constantsConstructor.newInstance();
        assertNotNull(constants);
    }
}