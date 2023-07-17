package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.enumerates;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Tool status
 */
@Slf4j
public enum ValidationStatus
{
    OK("OK"),

    WARNING("WARNING"),

    ERROR("ERROR"),

    /**
     * Invalid resource type
     */
    INVALID("INVALID");


    /**
     * Tool status
     */
    private final String validationStatus;


    private ValidationStatus(final String toolStatus)
    {
        this.validationStatus = toolStatus;
    }

    /**
     * This method return the current external tool type
     *
     * @return current validationStatus
     */
    public String getValidationStatus()
    {
        return this.validationStatus;
    }

    /**
     * returns a Service type
     *
     * @param name name
     * @return Service type
     */
    public static ValidationStatus getValueOf(String name)
    {
        ValidationStatus resp = INVALID;
        // Default.
        try
        {
            if (!StringUtils.isEmpty(name))
            {
                resp = ValidationStatus.valueOf(name);
            }
        }
        catch (IllegalArgumentException ex)
        {
            log.warn("[ValidationStatus] -> [getValueOf]: Unknown validation status {}", name, ex);
        }
        return resp;
    }
}
