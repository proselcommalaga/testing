package com.bbva.enoa.platformservices.coreservice.productsapi.autoconfig;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Class for properties
 */
@Getter
@Setter
@ConfigurationProperties(prefix = com.bbva.enoa.platformservices.coreservice.productsapi.autoconfig.ProductProperties.PREFIX)
public class ProductProperties
{
    /**
     * Properties prefix
     */
    public static final String PREFIX = "nova.products.cpd";

    /**
     *  cpd Int : TC_INT
     */
    private String cdpInt;

    /**
     *  cpd Int : TC_PRE
     */
    private String cdpPre;

    /**
     *  cpd Int : TC_PRO
     */
    private String cdpPro;

}
