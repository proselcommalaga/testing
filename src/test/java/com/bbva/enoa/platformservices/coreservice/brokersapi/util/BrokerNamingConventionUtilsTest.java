package com.bbva.enoa.platformservices.coreservice.brokersapi.util;

import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiChannel;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerErrorCode.INVALID_INPUT_FOR_CONVENTION_NAME_CREATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrokerNamingConventionUtilsTest
{

    private static final String UUAA = "UUAA";
    private static final String SERVICE_NAME = "servicename";

    @Nested
    class getPubSubChannelName
    {

        @Test
        void ok()
        {
            String expected = "uuaa_nombredelchannel_v_1";
            AsyncBackToBackApiVersion asyncBackToBackApiVersion = generateAsyncBackToBackApiVersion();
            String actual = BrokerNamingConventionUtils.getPubSubChannelName(asyncBackToBackApiVersion);
            assertEquals(expected, actual);
        }

        @Test
        void throw_NovaException_whenAsyncBackToBackApiVersionIsNull()
        {
            NovaException novaException = assertThrows(NovaException.class, () -> BrokerNamingConventionUtils.getPubSubChannelName(null));
            assertEquals(INVALID_INPUT_FOR_CONVENTION_NAME_CREATION, novaException.getErrorCode().getErrorCode());
        }

        @Test
        void throw_NovaException_whenAsyncBackToBackApiVersionApiIsNull()
        {
            AsyncBackToBackApiVersion asyncBackToBackApiVersion = generateAsyncBackToBackApiVersion();
            asyncBackToBackApiVersion.setApi(null);
            NovaException novaException = assertThrows(NovaException.class, () -> BrokerNamingConventionUtils.getPubSubChannelName(asyncBackToBackApiVersion));
            assertEquals(INVALID_INPUT_FOR_CONVENTION_NAME_CREATION, novaException.getErrorCode().getErrorCode());
        }

        @Test
        void throw_NovaException_whenAsyncBackToBackApiVersionUUAAIsNull()
        {
            AsyncBackToBackApiVersion asyncBackToBackApiVersion = generateAsyncBackToBackApiVersion();
            asyncBackToBackApiVersion.getApi().setUuaa(null);
            NovaException novaException = assertThrows(NovaException.class, () -> BrokerNamingConventionUtils.getPubSubChannelName(asyncBackToBackApiVersion));
            assertEquals(INVALID_INPUT_FOR_CONVENTION_NAME_CREATION, novaException.getErrorCode().getErrorCode());
        }

        @Test
        void throw_NovaException_whenAsyncBackToBackApiVersionAPIChannelIsNull()
        {
            AsyncBackToBackApiVersion asyncBackToBackApiVersion = generateAsyncBackToBackApiVersion();
            asyncBackToBackApiVersion.setAsyncBackToBackApiChannel(null);
            NovaException novaException = assertThrows(NovaException.class, () -> BrokerNamingConventionUtils.getPubSubChannelName(asyncBackToBackApiVersion));
            assertEquals(INVALID_INPUT_FOR_CONVENTION_NAME_CREATION, novaException.getErrorCode().getErrorCode());
        }

        @Test
        void throw_NovaException_whenAsyncBackToBackApiVersionAPIChannelNameIsNull()
        {
            AsyncBackToBackApiVersion asyncBackToBackApiVersion = generateAsyncBackToBackApiVersion();
            asyncBackToBackApiVersion.getAsyncBackToBackApiChannel().setChannelName(null);
            NovaException novaException = assertThrows(NovaException.class, () -> BrokerNamingConventionUtils.getPubSubChannelName(asyncBackToBackApiVersion));
            assertEquals(INVALID_INPUT_FOR_CONVENTION_NAME_CREATION, novaException.getErrorCode().getErrorCode());
        }

    }

    @Nested
    class getPubSubConsumerGroupName
    {
        @Test
        void ok()
        {
            String expected = "uuaa_servicename";
            String actual = BrokerNamingConventionUtils.getPubSubConsumerGroupName(UUAA, SERVICE_NAME);
            assertEquals(expected, actual);
        }

        @Test
        void throw_NovaException_whenUuaaIsNull()
        {
            NovaException novaException = assertThrows(NovaException.class, () -> BrokerNamingConventionUtils.getPubSubConsumerGroupName(null, SERVICE_NAME));
            assertEquals(INVALID_INPUT_FOR_CONVENTION_NAME_CREATION, novaException.getErrorCode().getErrorCode());
        }

        @Test
        void throw_NovaException_whenServiceNameIsNull()
        {
            NovaException novaException = assertThrows(NovaException.class, () -> BrokerNamingConventionUtils.getPubSubConsumerGroupName(UUAA, null));
            assertEquals(INVALID_INPUT_FOR_CONVENTION_NAME_CREATION, novaException.getErrorCode().getErrorCode());
        }
    }

    @Nested
    class getPubSubQueueName
    {
        AsyncBackToBackApiVersion asyncBackToBackApiVersion = generateAsyncBackToBackApiVersion();

        @Test
        void ok()
        {
            String expected = "uuaa_nombredelchannel_v_1.uuaa_servicename";
            String actual = BrokerNamingConventionUtils.getPubSubQueueName(UUAA, SERVICE_NAME, asyncBackToBackApiVersion);
            assertEquals(expected,actual);
        }
    }

    private AsyncBackToBackApiVersion generateAsyncBackToBackApiVersion()
    {

        AsyncBackToBackApiVersion asyncBackToBackApiVersion = new AsyncBackToBackApiVersion();
        AsyncBackToBackApi asyncBackToBackApi = new AsyncBackToBackApi();
        asyncBackToBackApi.setUuaa(UUAA);
        asyncBackToBackApiVersion.setApi(asyncBackToBackApi);
        asyncBackToBackApiVersion.setVersion("1.0.0");
        AsyncBackToBackApiChannel asyncBackToBackApiChannel = new AsyncBackToBackApiChannel();
        asyncBackToBackApiChannel.setChannelName("nombredelchannel");
        asyncBackToBackApiVersion.setAsyncBackToBackApiChannel(asyncBackToBackApiChannel);
        return asyncBackToBackApiVersion;
    }

}