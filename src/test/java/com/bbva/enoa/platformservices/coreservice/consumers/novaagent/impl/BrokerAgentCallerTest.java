package com.bbva.enoa.platformservices.coreservice.consumers.novaagent.impl;

import com.bbva.enoa.apirestgen.brokeragentapi.client.jaxrs.rest.IRestHandlerBrokeragentapi;
import com.bbva.enoa.apirestgen.brokeragentapi.client.jaxrs.rest.impl.RestHandlerBrokeragentapi;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.platformservices.coreservice.consumers.autoconfig.NovaAgentClientProperties;
import com.bbva.enoa.platformservices.coreservice.consumers.novaagent.interfaces.Callable;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.client.exception.request.NovaApiClientRequestException;
import com.bbva.kltt.apirest.generator.lib.commons.client.exception.response.NovaApiClientResponseException;
import com.bbva.kltt.apirest.generator.lib.commons.client.exception.response.NovaApiClientResponseTimeoutException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.google.gson.JsonSyntaxException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.List;

import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.BROKER_AGENT_ERROR;
import static com.bbva.enoa.platformservices.coreservice.consumers.novaagent.impl.BrokerAgentCallerTest.callOnEnvironment.HOST_PORT_TEST;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BrokerAgentCallerTest
{


    @Mock
    BrokerAgentCaller.BrokerAgentProvider brokerAgentProvider;

    @Mock
    NovaAgentClientProperties novaAgentClientProperties;

    @Mock
    NovaAgentUtils novaAgentUtils;

    @InjectMocks
    BrokerAgentCaller brokerAgentCaller;

    @BeforeEach
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    void verifyMocks()
    {
        verifyNoMoreInteractions(novaAgentClientProperties);
        verifyNoMoreInteractions(brokerAgentProvider);
        verifyNoMoreInteractions(novaAgentUtils);
    }

    @Nested
    class callOnEnvironment
    {
        public static final String HOST_PORT_TEST = "10.10.10.1:8080";


        @ParameterizedTest
        @EnumSource(value = Environment.class, names = {"INT", "PRE", "PRO"})
        void ok(Environment environment) throws Exception
        {
            // given
            IRestHandlerBrokeragentapi iRestHandlerBrokeragentapi = Mockito.mock(IRestHandlerBrokeragentapi.class);
            NovaAgentClientProperties.NovaAgentByEnvironment novaAgentByEnvironment = generateNovaAgentByEnvironment(environment);
            Callable<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi> methodCallable = (Callable<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi>) Mockito.mock(Callable.class);

            // when
            when(novaAgentClientProperties.getNovaAgentByEnvironment(environment.getEnvironment())).thenReturn(novaAgentByEnvironment);
            when(brokerAgentProvider.getBrokerAgentRestHandler(HOST_PORT_TEST)).thenReturn(iRestHandlerBrokeragentapi);

            // then
            brokerAgentCaller.callOnEnvironment(environment.getEnvironment(), methodCallable);

            // verify
            verify(novaAgentClientProperties).getNovaAgentByEnvironment(environment.getEnvironment());
            verify(brokerAgentProvider).getBrokerAgentRestHandler(HOST_PORT_TEST);
            verify(methodCallable).call(any(), eq(iRestHandlerBrokeragentapi));
        }

        @ParameterizedTest
        @EnumSource(value = Environment.class, names = {"INT", "PRE", "PRO"})
        @DisplayName("Test when calling brokeragentapi, the first node return an exception, but trying to the second, return a success")
        void ok_TwoNovaAgentNodes_WithFirstCallFailed(Environment environment) throws Exception
        {
            String HOST_PORT_TEST_OK = "10.0.0.0:1234";
            // given
            IRestHandlerBrokeragentapi iRestHandlerBrokeragentapiKO = Mockito.mock(IRestHandlerBrokeragentapi.class);
            IRestHandlerBrokeragentapi iRestHandlerBrokeragentapiOK = Mockito.mock(IRestHandlerBrokeragentapi.class);
            NovaAgentClientProperties.NovaAgentByEnvironment novaAgentByEnvironment = generateNovaAgentByEnvironment(environment);
            Callable<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi> methodCallable = (Callable<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi>) Mockito.mock(Callable.class);
            novaAgentByEnvironment.setHostPort(List.of(HOST_PORT_TEST, HOST_PORT_TEST_OK));

            // when
            when(novaAgentClientProperties.getNovaAgentByEnvironment(environment.getEnvironment())).thenReturn(novaAgentByEnvironment);
            when(brokerAgentProvider.getBrokerAgentRestHandler(HOST_PORT_TEST)).thenReturn(iRestHandlerBrokeragentapiKO);
            when(brokerAgentProvider.getBrokerAgentRestHandler(HOST_PORT_TEST_OK)).thenReturn(iRestHandlerBrokeragentapiOK);
            doThrow(Errors.class).when(methodCallable).call(any(), eq(iRestHandlerBrokeragentapiKO));

            // then
            brokerAgentCaller.callOnEnvironment(environment.getEnvironment(), methodCallable);

            // verify
            verify(novaAgentClientProperties).getNovaAgentByEnvironment(environment.getEnvironment());
            verify(brokerAgentProvider, times(2)).getBrokerAgentRestHandler(any());
            verify(methodCallable).call(any(), eq(iRestHandlerBrokeragentapiKO));
            verify(methodCallable).call(any(), eq(iRestHandlerBrokeragentapiOK));
        }

        @ParameterizedTest
        @EnumSource(value = Environment.class, names = {"INT", "PRE", "PRO"})
        void throw_NovaException_WhenException(Environment environment) throws Exception
        {
            ErrorMessage errorMessage = new ErrorMessage();
            errorMessage.setMessage("msg");
            errorMessage.setCode("ERROR-CODE");
            // given
            IRestHandlerBrokeragentapi iRestHandlerBrokeragentapi = Mockito.mock(IRestHandlerBrokeragentapi.class);
            NovaAgentClientProperties.NovaAgentByEnvironment novaAgentByEnvironment = generateNovaAgentByEnvironment(environment);
            Callable<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi> methodCallable = (Callable<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi>) Mockito.mock(Callable.class);

            // when
            when(novaAgentUtils.getErrorMessageFromJson(any())).thenReturn(errorMessage);
            when(novaAgentClientProperties.getNovaAgentByEnvironment(environment.getEnvironment())).thenReturn(novaAgentByEnvironment);
            when(brokerAgentProvider.getBrokerAgentRestHandler(HOST_PORT_TEST)).thenReturn(iRestHandlerBrokeragentapi);
            doThrow(Exception.class).when(methodCallable).call(any(), any());

            String env = environment.getEnvironment();
            //then
            NovaException novaException = assertThrows(NovaException.class, () -> brokerAgentCaller.callOnEnvironment(env, methodCallable));

            // verify
            verify(novaAgentClientProperties).getNovaAgentByEnvironment(environment.getEnvironment());
            verify(brokerAgentProvider).getBrokerAgentRestHandler(HOST_PORT_TEST);
            verify(novaAgentUtils).getErrorMessageFromJson(any());
            verify(methodCallable).call(any(), eq(iRestHandlerBrokeragentapi));
            assertEquals("ERROR-CODE", novaException.getErrorCode().getErrorCode());
        }

        @ParameterizedTest
        @EnumSource(value = Environment.class, names = {"INT", "PRE", "PRO"})
        void throw_NovaException_WhenNovaApiClientResponseTimeoutException(Environment environment) throws Exception
        {
            // given
            IRestHandlerBrokeragentapi iRestHandlerBrokeragentapi = Mockito.mock(IRestHandlerBrokeragentapi.class);
            NovaAgentClientProperties.NovaAgentByEnvironment novaAgentByEnvironment = generateNovaAgentByEnvironment(environment);
            Callable<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi> methodCallable = (Callable<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi>) Mockito.mock(Callable.class);

            // when
            when(novaAgentClientProperties.getNovaAgentByEnvironment(environment.getEnvironment())).thenReturn(novaAgentByEnvironment);
            when(brokerAgentProvider.getBrokerAgentRestHandler(HOST_PORT_TEST)).thenReturn(iRestHandlerBrokeragentapi);
            doThrow(JsonSyntaxException.class).when(novaAgentUtils).getErrorMessageFromJson(any());
            doThrow(NovaApiClientResponseTimeoutException.class).when(methodCallable).call(any(), any());
            String env = environment.getEnvironment();
            //then
            NovaException novaException = assertThrows(NovaException.class, () -> brokerAgentCaller.callOnEnvironment(env, methodCallable));

            // verify
            verify(novaAgentClientProperties).getNovaAgentByEnvironment(environment.getEnvironment());
            verify(brokerAgentProvider).getBrokerAgentRestHandler(HOST_PORT_TEST);
            verify(novaAgentUtils).getErrorMessageFromJson(any());
            verify(methodCallable).call(any(), eq(iRestHandlerBrokeragentapi));
            assertEquals(BROKER_AGENT_ERROR, novaException.getErrorCode().getErrorCode());
        }

        @ParameterizedTest
        @EnumSource(value = Environment.class, names = {"INT", "PRE", "PRO"})
        void throw_NovaException_WhenNovaApiClientResponseException(Environment environment) throws Exception
        {
            // given
            IRestHandlerBrokeragentapi iRestHandlerBrokeragentapi = Mockito.mock(IRestHandlerBrokeragentapi.class);
            NovaAgentClientProperties.NovaAgentByEnvironment novaAgentByEnvironment = generateNovaAgentByEnvironment(environment);
            Callable<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi> methodCallable = (Callable<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi>) Mockito.mock(Callable.class);

            // when
            when(novaAgentClientProperties.getNovaAgentByEnvironment(environment.getEnvironment())).thenReturn(novaAgentByEnvironment);
            when(brokerAgentProvider.getBrokerAgentRestHandler(HOST_PORT_TEST)).thenReturn(iRestHandlerBrokeragentapi);
            doThrow(JsonSyntaxException.class).when(novaAgentUtils).getErrorMessageFromJson(any());
            doThrow(NovaApiClientResponseException.class).when(methodCallable).call(any(), any());
            String env = environment.getEnvironment();
            //then
            NovaException novaException = assertThrows(NovaException.class, () -> brokerAgentCaller.callOnEnvironment(env, methodCallable));

            // verify
            verify(novaAgentClientProperties).getNovaAgentByEnvironment(environment.getEnvironment());
            verify(brokerAgentProvider).getBrokerAgentRestHandler(HOST_PORT_TEST);
            verify(methodCallable).call(any(), eq(iRestHandlerBrokeragentapi));
            verify(novaAgentUtils).getErrorMessageFromJson(any());
            assertEquals(BROKER_AGENT_ERROR, novaException.getErrorCode().getErrorCode());
        }

        @ParameterizedTest
        @EnumSource(value = Environment.class, names = {"INT", "PRE", "PRO"})
        void throw_NovaException_WhenNovaApiClientRequestException(Environment environment) throws Exception
        {
            // given
            IRestHandlerBrokeragentapi iRestHandlerBrokeragentapi = Mockito.mock(IRestHandlerBrokeragentapi.class);
            NovaAgentClientProperties.NovaAgentByEnvironment novaAgentByEnvironment = generateNovaAgentByEnvironment(environment);
            Callable<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi> methodCallable = (Callable<NovaJaxRsConfigUtils, IRestHandlerBrokeragentapi>) Mockito.mock(Callable.class);

            // when
            when(novaAgentClientProperties.getNovaAgentByEnvironment(environment.getEnvironment())).thenReturn(novaAgentByEnvironment);
            when(brokerAgentProvider.getBrokerAgentRestHandler(HOST_PORT_TEST)).thenReturn(iRestHandlerBrokeragentapi);
            doThrow(JsonSyntaxException.class).when(novaAgentUtils).getErrorMessageFromJson(any());
            doThrow(NovaApiClientRequestException.class).when(methodCallable).call(any(), any());
            String env = environment.getEnvironment();
            //then
            NovaException novaException = assertThrows(NovaException.class, () -> brokerAgentCaller.callOnEnvironment(env, methodCallable));

            // verify
            verify(novaAgentClientProperties).getNovaAgentByEnvironment(environment.getEnvironment());
            verify(brokerAgentProvider).getBrokerAgentRestHandler(HOST_PORT_TEST);
            verify(methodCallable).call(any(), eq(iRestHandlerBrokeragentapi));
            verify(novaAgentUtils).getErrorMessageFromJson(any());
            assertEquals(BROKER_AGENT_ERROR, novaException.getErrorCode().getErrorCode());
        }

    }


    private NovaAgentClientProperties.NovaAgentByEnvironment generateNovaAgentByEnvironment(final Environment environment)
    {
        NovaAgentClientProperties.NovaAgentByEnvironment novaAgentByEnvironment = new NovaAgentClientProperties.NovaAgentByEnvironment();
        novaAgentByEnvironment.setEnvironment(environment.getEnvironment());
        novaAgentByEnvironment.setHostPort(List.of(HOST_PORT_TEST));
        return novaAgentByEnvironment;
    }

    @Nested
    class ConnectionFactoryProviderTest
    {

        @InjectMocks
        BrokerAgentCaller.BrokerAgentProvider brokerAgentProvider;

        @BeforeEach
        void setup()
        {
            MockitoAnnotations.initMocks(this);
        }

        @Nested
        class getBrokerAgentRestHandler
        {

            @Test
            void ok() throws NoSuchFieldException, IllegalAccessException
            {
                IRestHandlerBrokeragentapi expected = new RestHandlerBrokeragentapi(HOST_PORT_TEST, NovaJaxRsConfigUtils.DEFAULT_CLIENT_TIMEOUT);
                IRestHandlerBrokeragentapi actual = brokerAgentProvider.getBrokerAgentRestHandler(HOST_PORT_TEST);
                Field privateStringField = RestHandlerBrokeragentapi.class.getDeclaredField("url");

                privateStringField.setAccessible(true);
                String expectedURL = (String) privateStringField.get(expected);
                String actualURL = (String) privateStringField.get(actual);

                assertNotNull(actual);
                assertEquals(expectedURL, actualURL);
            }
        }
    }
}