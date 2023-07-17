package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMConsumedApiDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMServedApiDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMServiceDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AGMServiceDtoBuilder
{
    private final List<AGMServedApiDTO> servedApis;
    private final List<AGMServedApiDTO> asyncApis;
    private final Map<String, AGMConsumedApiDTO> consumedApis;
    private final Map<String, AGMConsumedApiDTO> externalNovaConsumedApis;
    private final Map<String, AGMConsumedApiDTO> externalConsumedApis;
    private final String location;
    private final Integer serviceId;
    private final String platform;

    private AGMServiceDtoBuilder(final String location, final Integer serviceId, final String platform)
    {
        this.location = location;
        this.serviceId = serviceId;
        this.platform = platform;
        this.servedApis = new ArrayList<>();
        this.asyncApis = new ArrayList<>();
        this.consumedApis = new HashMap<>();
        this.externalNovaConsumedApis = new HashMap<>();
        this.externalConsumedApis = new HashMap<>();
    }

    public static AGMServiceDtoBuilder getInstance(final String location, final Integer serviceId, final String platform)
    {
        return new AGMServiceDtoBuilder(location, serviceId, platform);
    }

    public AGMServiceDtoBuilder addServedApi(AGMServedApiDTO servedApi)
    {
        this.servedApis.add(servedApi);
        return this;
    }

    public AGMServiceDtoBuilder addAsyncApi(AGMServedApiDTO asyncApi)
    {
        this.asyncApis.add(asyncApi);
        return this;
    }

    public AGMServiceDtoBuilder addConsumedApis(AGMConsumedApiDTO consumedApi)
    {
        this.consumedApis.put(this.getConsumedApiEntryKey(consumedApi), consumedApi);
        return this;
    }

    public AGMServiceDtoBuilder addExternalNovaConsumedApis(AGMConsumedApiDTO externalNovaConsumedApi)
    {
        this.externalNovaConsumedApis.put(this.getConsumedApiEntryKey(externalNovaConsumedApi), externalNovaConsumedApi);
        return this;
    }

    public AGMServiceDtoBuilder addExternalConsumedApis(AGMConsumedApiDTO externalConsumedApi)
    {
        this.externalConsumedApis.put(this.getConsumedApiEntryKey(externalConsumedApi), externalConsumedApi);
        return this;
    }

    private String getConsumedApiEntryKey(AGMConsumedApiDTO consumedApi)
    {
        String entryKey = consumedApi.getUuaa() + consumedApi.getConsumedPath() + consumedApi.getLocation() + (consumedApi.getRelease() != null ? consumedApi.getRelease() : "");
        return entryKey.toLowerCase();
    }

    public AGMServiceDTO build()
    {
        AGMServiceDTO serviceDTO = new AGMServiceDTO();
        serviceDTO.setServedApis(this.servedApis.toArray(AGMServedApiDTO[]::new));
        serviceDTO.setAsyncApis(this.asyncApis.toArray(AGMServedApiDTO[]::new));
        serviceDTO.setConsumedApis(this.consumedApis.values().toArray(AGMConsumedApiDTO[]::new));
        serviceDTO.setExternalNovaConsumedApis(this.externalNovaConsumedApis.values().toArray(AGMConsumedApiDTO[]::new));
        serviceDTO.setExternalConsumedApis(this.externalConsumedApis.values().toArray(AGMConsumedApiDTO[]::new));
        serviceDTO.setLocation(this.location);
        serviceDTO.setPlatform(this.platform);
        serviceDTO.setServiceId(this.serviceId);
        return serviceDTO;
    }

}
