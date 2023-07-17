package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

import lombok.EqualsAndHashCode;

/**
 * Nova.yml property
 */
@EqualsAndHashCode
public class NovaYamlProperty
{

    private String name;
    private String type;
    private String scope;
    private String management;
    private Boolean encrypt;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getScope() { return scope; }

    public void setScope(String scope) { this.scope = scope; }

    public String getManagement()
    {
        return management;
    }

    public void setManagement(String management)
    {
        this.management = management;
    }

    public Boolean getEncrypt()
    {
        return encrypt;
    }

    public void setEncrypt(Boolean encrypt)
    {
        this.encrypt = encrypt;
    }

}
