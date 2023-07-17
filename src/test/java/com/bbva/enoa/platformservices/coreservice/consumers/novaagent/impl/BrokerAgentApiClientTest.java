package com.bbva.enoa.platformservices.coreservice.consumers.novaagent.impl;

import com.bbva.enoa.apirestgen.brokeragentapi.client.jaxrs.rest.IRestHandlerBrokeragentapi;
import com.bbva.enoa.apirestgen.brokeragentapi.model.ChannelsActionDTO;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces.Callable;
import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class BrokerAgentApiClientTest
{

    @Mock
    IRestHandlerBrokeragentapi iRestHandlerBrokeragentapi;

    @Mock
    BrokerAgentCaller novaAgentCaller;

    @InjectMocks
    BrokerAgentApiClient brokerAgentApiClient;

    @BeforeEach
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    void verifyMocks()
    {
        verifyNoMoreInteractions(iRestHandlerBrokeragentapi);
        verifyNoMoreInteractions(novaAgentCaller);
    }

    @Nested
    class createChannels
    {
        Environment environment = Environment.PRE;
        ChannelsActionDTO channelsActionDTO = generateChannelsActionDTO();
        NovaJaxRsConfigUtils config = new NovaJaxRsConfigUtils();

        @Test
        void ok() throws Exception
        {
            Mockito.doAnswer(invocationOnMock -> {
                invocationOnMock.getArgument(1, Callable.class).call(config, iRestHandlerBrokeragentapi);
                return null;
            }).when(novaAgentCaller).callOnEnvironment(any(), any());

            brokerAgentApiClient.createChannels(environment.name(), channelsActionDTO);
            verify(novaAgentCaller).callOnEnvironment(eq(environment.name()), any());
            verify(iRestHandlerBrokeragentapi).createChannels(any(), any(), eq(channelsActionDTO));
        }
    }

    private ChannelsActionDTO generateChannelsActionDTO()
    {
        ChannelsActionDTO channelsActionDTO = new ChannelsActionDTO();

        return channelsActionDTO;
    }
}