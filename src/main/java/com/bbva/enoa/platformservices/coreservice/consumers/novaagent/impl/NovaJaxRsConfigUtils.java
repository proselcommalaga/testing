package com.bbva.enoa.platformservices.coreservice.consumers.novaagent.impl;

import com.bbva.kltt.apirest.generator.lib.commons.client.request.NovaRequestConfig;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersOutput;
import com.bbva.kltt.apirest.generator.lib.commons.uri.NovaSchemesValues;

/**
 * The type Abstract jax rs client.
 */
public class NovaJaxRsConfigUtils
{
    /**
     * Default client timeout in milliseconds.
     */
    public static final int DEFAULT_CLIENT_TIMEOUT = 15000;

    /**
     * Build a minimum metadata that includes at least the iv user header
     * This method can be extended if we want include more metadata info
     *
     * @return a NovaMetadata instance
     */
    public NovaMetadata buildMetadata()
    {
        NovaMetadata metadata = new NovaMetadata();

        NovaImplicitHeadersOutput implicitHeadersOutput = new NovaImplicitHeadersOutput();

        metadata.setNovaImplicitHeadersOutput(implicitHeadersOutput);

        return metadata;
    }

    /**
     * Build a minimum request config.
     * Always creates a http scheme value
     *
     * @return a NovaRequestConfig instance
     */
    public NovaRequestConfig buildRequestConfig()
    {
        // Set one of the "schemes" defined
        final NovaSchemesValues scheme = NovaSchemesValues.http;

        // Generate instance of RequestConfig
        final NovaRequestConfig requestConfig = new NovaRequestConfig();
        requestConfig.setNovaScheme(scheme);

        return requestConfig;
    }
}
