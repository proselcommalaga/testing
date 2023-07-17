package com.bbva.enoa.platformservices.coreservice.common.util;

import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.junit.Assert.assertNotNull;

public class MailServiceConstantsTest
{
    @Test
    public void constructor() throws Exception
    {

        Constructor<MailServiceConstants> constantsConstructor = MailServiceConstants.class.getDeclaredConstructor();
        constantsConstructor.setAccessible(true);
        MailServiceConstants constants = constantsConstructor.newInstance();
        assertNotNull(constants);
    }
}