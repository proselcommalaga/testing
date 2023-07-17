package com.bbva.enoa.platformservices.coreservice.common.enums;

public enum BatchSchedulerInstanceStatus
{
    RESUME_INSTANCE("RESUME"),

    STOP_INSTANCE("STOP"),

    PAUSE_INSTANCE("PAUSE"),

    START_INSTANCE("START");

    private final String typeInstance;

    BatchSchedulerInstanceStatus(String typeInstance)
    {
        this.typeInstance = typeInstance;
    }

    public String getTypeInstance()
    {
        return typeInstance;
    }
}
