package com.bbva.enoa.platformservices.coreservice.brokersapi.util;

import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.platformservices.coreservice.brokersapi.exception.BrokerError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

/**
 * The type Broker naming convention utils.
 */
public class BrokerNamingConventionUtils
{
    private static final Logger LOG = LoggerFactory.getLogger(BrokerNamingConventionUtils.class);

    private BrokerNamingConventionUtils()
    {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets pub sub channel name.
     * For pub/sub broker with rabbitmq that represents the exchange name.
     *
     * @param asyncBackToBackApiVersion the async back to back api version
     * @return the pub sub channel name
     */
    public static String getPubSubChannelName(final AsyncBackToBackApiVersion asyncBackToBackApiVersion)
    {
        var channelNamePattern = "%s_%s_v_%s";

        if (
                asyncBackToBackApiVersion == null
                        || asyncBackToBackApiVersion.getApi() == null
                        || asyncBackToBackApiVersion.getApi().getUuaa() == null
                        || asyncBackToBackApiVersion.getVersion() == null
                        || asyncBackToBackApiVersion.getAsyncBackToBackApiChannel() == null
                        || asyncBackToBackApiVersion.getAsyncBackToBackApiChannel().getChannelName() == null
        )
        {
            LOG.error("Error creating channelName with input asyncBackToBackApiVersion: [{}]", asyncBackToBackApiVersion);
            throw new NovaException(BrokerError.getInvalidInputsForConventionNameCreation("getPubSubChannelName"));
        }
        String uuaa = asyncBackToBackApiVersion.getApi().getUuaa().toLowerCase();
        String asyncApiMajorVersion = asyncBackToBackApiVersion.getVersion().split("\\.")[0];
        String channelName = asyncBackToBackApiVersion.getAsyncBackToBackApiChannel().getChannelName();

        return String.format(channelNamePattern, uuaa, channelName, asyncApiMajorVersion);

    }

    /**
     * Gets pub sub consumer group name.
     *
     * @param uuaa        the uuaa
     * @param serviceName the service name
     * @return the pub sub consumer group name
     */
    public static String getPubSubConsumerGroupName(final String uuaa, final String serviceName)
    {
        var consumerGroupPattern = "%s_%s";
        if (uuaa == null || serviceName == null)
        {
            LOG.error("Error creating consumerGroup name with input uuaa: [{}], serviceName [{}] ", uuaa, serviceName);
            throw new NovaException(BrokerError.getInvalidInputsForConventionNameCreation("getPubSubConsumerGroupName"));
        }
        return String.format(consumerGroupPattern, uuaa.toLowerCase(Locale.ROOT), serviceName);
    }

    /**
     * Gets pub sub queue name.
     *
     * @param uuaa                      the uuaa
     * @param serviceName               the service name
     * @param asyncBackToBackApiVersion the async back to back api version
     * @return the pub sub queue name
     */
    public static String getPubSubQueueName(final String uuaa, final String serviceName, final AsyncBackToBackApiVersion asyncBackToBackApiVersion)
    {
        var queuePattern = "%s.%s";
        return String.format(queuePattern, getPubSubChannelName(asyncBackToBackApiVersion), getPubSubConsumerGroupName(uuaa, serviceName));
    }
}
