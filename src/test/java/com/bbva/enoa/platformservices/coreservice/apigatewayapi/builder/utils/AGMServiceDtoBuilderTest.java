package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMConsumedApiDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMServedApiDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AGMServiceDtoBuilderTest
{
    @Test
    @DisplayName("Build -> OK")
    public void build()
    {
        var location = "location";
        var platform = "platform";
        var serviceId = 909;

        var servedApi = buildAGMServedApiDTO();
        var asyncApi = buildAGMServedApiDTO();
        var consumedApi = buildAGMConsumedApiDTO();
        var externalApi = buildAGMConsumedApiDTO();
        var externalNovaApi = buildAGMConsumedApiDTO();

        var builder = AGMServiceDtoBuilder.getInstance(location, serviceId, platform);
        builder.addServedApi(servedApi);
        builder.addAsyncApi(asyncApi);
        builder.addConsumedApis(consumedApi);
        builder.addExternalConsumedApis(externalApi);
        builder.addExternalNovaConsumedApis(externalNovaApi);

        var ret = builder.build();

        assertEquals(location, ret.getLocation());
        assertEquals(platform, ret.getPlatform());
        assertEquals(serviceId, ret.getServiceId());
        assertEquals(1, ret.getServedApis().length);
        assertEquals(servedApi, ret.getServedApis()[0]);
        assertEquals(1, ret.getAsyncApis().length);
        assertEquals(asyncApi, ret.getAsyncApis()[0]);
        assertEquals(1, ret.getConsumedApis().length);
        assertEquals(consumedApi, ret.getConsumedApis()[0]);
        assertEquals(1, ret.getExternalConsumedApis().length);
        assertEquals(externalApi, ret.getExternalConsumedApis()[0]);
        assertEquals(1, ret.getExternalNovaConsumedApis().length);
        assertEquals(externalNovaApi, ret.getExternalNovaConsumedApis()[0]);
    }

    private AGMServedApiDTO buildAGMServedApiDTO()
    {
        var dto = new AGMServedApiDTO();
        dto.fillRandomly(4, false, 0, 4);
        return dto;
    }

    private AGMConsumedApiDTO buildAGMConsumedApiDTO()
    {
        var dto = new AGMConsumedApiDTO();
        dto.fillRandomly(4, false, 0, 4);
        return dto;
    }

}