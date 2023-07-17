package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

/**
 * Nova.yml output params for the batch
 */
public class NovaYamlOutputParams
{
    private String name;
    private String type;
    private String description;
    private Boolean mandatory;

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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Boolean getMandatory()
    {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory)
    {
        this.mandatory = mandatory;
    }
}
