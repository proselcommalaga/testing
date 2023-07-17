package com.bbva.enoa.platformservices.coreservice.platformconfigapi.listener;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.platformservices.coreservice.platformconfigapi.services.interfaces.IPlatformConfigService;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListenerPlatformConfigApiTest
{
    @Mock
    private IPlatformConfigService platformConfigService;

    @InjectMocks
    private ListenerPlatformConfigApi listenerPlatformConfigApi;

    @Test
    public void testGetPlatformProperty() throws Errors
    {
        // given
        final NovaMetadata novaMetadata = Mockito.mock(NovaMetadata.class);
        final String name = RandomStringUtils.randomAlphabetic(8);

        final String value = RandomStringUtils.randomAlphabetic(4);
        when(platformConfigService.getPlatformProperty(eq(name))).thenReturn(value);

        // when
        final String response = listenerPlatformConfigApi.getPlatformProperty(novaMetadata, name);

        // then
        assertEquals(value, response);
    }

    @Test
    public void testConfigureDiscoverPlatform() throws Errors
    {
        // given
        final NovaMetadata novaMetadata = Mockito.mock(NovaMetadata.class);
        final String name = RandomStringUtils.randomAlphabetic(8);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String platform = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)].name();

        final String value = RandomStringUtils.randomAlphabetic(4);
        when(platformConfigService.getPlatformDiscoveryConfig(eq(name), eq(environment), eq(platform))).thenReturn(value);

        // when
        final String response = listenerPlatformConfigApi.configureDiscoverPlatform(novaMetadata, name, environment, platform);

        // then
        assertEquals(value, response);
    }
}