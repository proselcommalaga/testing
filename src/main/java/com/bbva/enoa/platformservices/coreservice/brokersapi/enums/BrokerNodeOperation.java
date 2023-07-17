package com.bbva.enoa.platformservices.coreservice.brokersapi.enums;

import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum BrokerNodeOperation
{
    START("START", ActivityAction.STARTED),
    STOP("STOP", ActivityAction.STOPPED),
    RESTART("RESTART", ActivityAction.RESTARTED);

    @Getter
    private String operation;

    @Getter
    private ActivityAction activityAction;
}
