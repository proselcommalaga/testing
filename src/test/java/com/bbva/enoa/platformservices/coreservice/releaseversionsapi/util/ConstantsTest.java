package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

public class ConstantsTest
{
    @Test
    public void constructor() throws Exception
    {

        Constructor<Constants> constantsConstructor = Constants.class.getDeclaredConstructor();
        constantsConstructor.setAccessible(true);
        Constants constants = constantsConstructor.newInstance();
        Assertions.assertNotNull(constants);
    }
}