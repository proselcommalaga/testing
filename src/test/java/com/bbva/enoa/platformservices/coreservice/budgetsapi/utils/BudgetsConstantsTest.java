package com.bbva.enoa.platformservices.coreservice.budgetsapi.utils;

import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertNotNull;

public class BudgetsConstantsTest
{
    @Test
    public void constructor() throws Exception
    {
        Constructor<BudgetsConstants> constantsConstructor = BudgetsConstants.class.getDeclaredConstructor();
        constantsConstructor.setAccessible(true);
        BudgetsConstants constants = constantsConstructor.newInstance();
        assertNotNull(constants);
    }
}