package com.bbva.enoa.platformservices.coreservice.brokersapi.enums;

public enum BrokerUsageType
{
    PUBLISHER("PUBLISHER"),

    CONSUMER("CONSUMER"),

    PUBLISHER_CONSUMER("PUBLISHER_AND_CONSUMER");

    private final String usageType;

    BrokerUsageType(String usageType)
    {
        this.usageType = usageType;
    }

    public String getUsageType()
    {
        return usageType;
    }
}
