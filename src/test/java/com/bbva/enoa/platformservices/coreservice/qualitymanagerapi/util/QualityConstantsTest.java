package com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;


public class QualityConstantsTest
{
    @Test
    public void constructor() throws Exception
    {
        Constructor<QualityConstants> constantsConstructor = QualityConstants.class.getDeclaredConstructor();
        constantsConstructor.setAccessible(true);
        QualityConstants constants = constantsConstructor.newInstance();
        Assertions.assertNotNull(constants);
    }
}