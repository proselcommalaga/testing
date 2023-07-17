package com.bbva.enoa.platformservices.coreservice.brokersapi.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BrokerConstantsTest
{

    @Test
    void constructorBrokerErrorCode() throws Exception
    {
        Constructor<BrokerConstants.BrokerErrorCode> constantsConstructor = BrokerConstants.BrokerErrorCode.class.getDeclaredConstructor();
        constantsConstructor.setAccessible(true);
        BrokerConstants.BrokerErrorCode constants = constantsConstructor.newInstance();
        assertNotNull(constants);
    }

    @Test
    void constructorBrokerPermissions() throws Exception
    {
        Constructor<BrokerConstants.BrokerPermissions> constantsConstructor = BrokerConstants.BrokerPermissions.class.getDeclaredConstructor();
        constantsConstructor.setAccessible(true);
        BrokerConstants.BrokerPermissions constants = constantsConstructor.newInstance();
        assertNotNull(constants);
    }
}
