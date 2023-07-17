package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PhysicalConnectorPortActionTest
{

    @Test
    public void getActionName()
    {
        String action = PhysicalConnectorPortAction.CREATE.getActionName();
        assertEquals("CREATE", action);
    }
}