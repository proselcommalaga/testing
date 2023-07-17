package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

import java.util.List;

/**
 * NOVA yml
 */
public class NovaYmlApi
{
    /**
     * API
     */
    private String api;
    /**
     * Consumed APIs
     */
    private List<String> consumedApi;

    /**
     * Consumed external APIs
     */
    private List<String> externalApi;

    /**
     * Backwards compatible versions
     */
    private List<String> backwardCompatibleVersions;

    /**
     * Get API
     *
     * @return api
     */
    public String getApi()
    {
        return api;
    }

    /**
     * Set API
     *
     * @param api new api
     */
    public void setApi(String api)
    {
        this.api = api;
    }

    /**
     * Get consumed APIs
     *
     * @return apis
     */
    public List<String> getConsumedApi()
    {
        return List.copyOf(consumedApi);
    }

    /**
     * Set consumed APIs
     *
     * @param consumedApi consumed apis
     */
    public void setConsumedApi(List<String> consumedApi)
    {
        this.consumedApi = List.copyOf(consumedApi);
    }

    /**
     * Get consumed external APIs
     *
     * @return apis
     */
    public List<String> getExternalApi()
    {
        return List.copyOf(externalApi);
    }

    /**
     * Set consumed APIs
     *
     * @param externalApi consumed external apis
     */
    public void setExternalApi(List<String> externalApi)
    {
        this.externalApi = List.copyOf(externalApi);
    }

    public List<String> getBackwardCompatibleVersions()
    {
        return List.copyOf(backwardCompatibleVersions);
    }

    public NovaYmlApi setBackwardCompatibleVersions(final List<String> backwardCompatibleVersions)
    {
        this.backwardCompatibleVersions = List.copyOf(backwardCompatibleVersions);
        return this;
    }
}
