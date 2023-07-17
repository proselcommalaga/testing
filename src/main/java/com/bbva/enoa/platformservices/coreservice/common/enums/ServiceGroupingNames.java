package com.bbva.enoa.platformservices.coreservice.common.enums;

import lombok.Getter;

/**
 * Labels grouping different service types for statistics frontend.
 */
public enum ServiceGroupingNames
{
    API(true),
    BATCH(false),
    DAEMON(true),
    CDN(true),
    BATCH_SCHEDULER(false),
    DEPENDENCY(false),
    LIBRARY(false),
    EPHOENIX_ONLINE(true),
    EPHOENIX_BATCH(true),
    FRONTCAT(true),
    BEHAVIOR_TEST(false),
    INVALID(false);

    @Getter
    private boolean isDeployed;

    ServiceGroupingNames(boolean isDeployed)
    {
        this.isDeployed = isDeployed;
    }
}
