package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * For mapping the file bootstrap content under validation
 */
public class BootstrapYaml
{
    @JsonProperty("active_profile")
    private String activeProfile;
    private boolean valid;

    public boolean isValid()
    {
        return valid;
    }

    public void setValid(final boolean valid)
    {
        this.valid = valid;
    }



    public String getActiveProfile()
    {
        return activeProfile;
    }

    public void setActiveProfile(String activeProfile)
    {
        this.activeProfile = activeProfile;
    }
}
