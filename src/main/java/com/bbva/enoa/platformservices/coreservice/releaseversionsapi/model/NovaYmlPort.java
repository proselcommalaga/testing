package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

/**
 * Model of the ports of the nova.yml ports
 */
public class NovaYmlPort
{
    private String name;
    private Integer insidePort;
    private Integer outsidePort;
    private String type;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getInsidePort()
    {
        return insidePort;
    }

    public void setInsidePort(Integer insidePort)
    {
        this.insidePort = insidePort;
    }

    public Integer getOutsidePort()
    {
        return outsidePort;
    }

    public void setOutsidePort(Integer outsidePort)
    {
        this.outsidePort = outsidePort;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
