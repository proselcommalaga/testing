package com.bbva.enoa.platformservices.coreservice.brokersapi.enums;

import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum BrokerOperation
{
    CREATE("CREATE", ActivityAction.CREATED),
    START("START", ActivityAction.STARTED),
    STOP("STOP", ActivityAction.STOPPED),
    RESTART("RESTART", ActivityAction.RESTARTED),
    DELETE("DELETE", ActivityAction.DELETED);

    @Getter
    private String operation;

    @Getter
    private ActivityAction activityAction;
}
