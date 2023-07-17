package com.bbva.enoa.platformservices.coreservice.common;

import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertNotNull;

/**
 * Created by xe56809 on 13/03/2018.
 */
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