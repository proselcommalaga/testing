package com.bbva.enoa.platformservices.coreservice.productsapi.autoconfig;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * Class for configuration
 */
@Configuration
@EnableConfigurationProperties(ProductProperties.class)
public class ProductsAutoconfiguration
{
    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory
            .getLogger(com.bbva.enoa.platformservices.coreservice.productsapi.autoconfig.ProductsAutoconfiguration.class);

    /**
     * Service properties
     */
    @Autowired
    private ProductProperties properties;

    /**
     * Post constructor
     */
    @PostConstruct
    public void postConstruct()
    {

        LOGGER.debug("[{}] -> [{}]: Setting up Product configuration", "ProductsAutoconfiguration", "postConstruct");

        this.analyzeCPD();

        LOGGER.debug("[{}] -> [{}]: Configuration finished", "ProductsAutoconfiguration", "postConstruct");

    }


    /**
     * Analyze server URL
     */
    private void analyzeCPD()
    {
        if (!StringUtils.hasText(this.properties.getCdpInt()))
        {
            LOGGER.warn("[{}] -> [{}]: CPD INT is missing", "ProductsAutoconfiguration", "analyzeServerURL");
        }
        if (!StringUtils.hasText(this.properties.getCdpPre()))
        {
            LOGGER.warn("[{}] -> [{}]: CPD PRE is missing", "ProductsAutoconfiguration", "analyzeServerURL");
        }
        if (!StringUtils.hasText(this.properties.getCdpPro()))
        {
            LOGGER.warn("[{}] -> [{}]: CPD PRO is missing", "ProductsAutoconfiguration", "analyzeServerURL");
        }
    }
}
