package com.bbva.enoa.platformservices.coreservice.common.builders.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Set;

@Log4j2
@Component
public class DefaultBasicAlertDTOBuilderImpl extends AbstractDefaultBasicAlertDTOBuilderImpl
{
    /**
     * This is a blacklist. Every alert code on the set won't be associated with this implementation
     */
    private final Set<String> nonSupportedAlerts = Set.of("APP_BATCH_FAILURE", "APP_BATCH_INTERRUPTED", "APP_SCHEDULE_FAILURE");

    @Override
    public Boolean isSupported(final String alertType)
    {
        return Boolean.FALSE.equals(this.nonSupportedAlerts.contains(alertType));
    }
}
