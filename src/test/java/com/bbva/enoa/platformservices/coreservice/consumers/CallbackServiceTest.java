package com.bbva.enoa.platformservices.coreservice.consumers;

import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.CallbackInfoDto;
import com.bbva.enoa.platformservices.coreservice.common.util.CallbackService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;

/**
 * Created by xe56809 on 13/03/2018.
 */
public class CallbackServiceTest
{
    @InjectMocks
    private CallbackService callbackService;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(this.callbackService, "applicationName", "ApplicationName");
    }

    @Test
    public void buildCallback() throws Exception
    {
        //Then
        CallbackInfoDto response = this.callbackService.buildCallback("success", "error");
        assertEquals("success", response.getSuccessCallback().getUrl());
        assertEquals("error", response.getErrorCallback().getUrl());
    }

}