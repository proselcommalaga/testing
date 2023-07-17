package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Nova.yml context param (only used by batch scheduler services)
 */
@Getter
@Setter
public class NovaYamlContextParam
{
    /**
     * Name of the context param
     */
    private String name;
    /**
     * Type. Could be and INTEGER, BOOLEAN or STRING
     */
    private String type;
    /**
     * Description of the context param
     */
    private String description;
    /**
     * Default value
     */
    private String defaultValue;
}
