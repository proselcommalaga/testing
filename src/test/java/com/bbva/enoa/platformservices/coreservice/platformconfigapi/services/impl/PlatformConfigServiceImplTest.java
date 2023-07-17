package com.bbva.enoa.platformservices.coreservice.platformconfigapi.services.impl;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PlatformDiscoveryConfigRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PlatformPropertyRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformConfigServiceImplTest
{
    @Mock
    private PlatformDiscoveryConfigRepository platformDiscoveryRepository;

    @Mock
    private PlatformPropertyRepository platformPropertyRepository;

    @InjectMocks
    private PlatformConfigServiceImpl platformConfigService;

    @Test
    public void testGetPlatformDiscoveryConfig()
    {
        // given
        final String                        name        = RandomStringUtils.randomAlphabetic(8);
        final Environment                   environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final Platform platform    = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)];

        final String value = RandomStringUtils.randomAlphabetic(4);
        when(platformDiscoveryRepository.findByNameAndEnvironmentAndPlatform(eq(name), eq(environment.getEnvironment()), eq(platform))).thenReturn(value);

        // when
        final String response = platformConfigService.getPlatformDiscoveryConfig(name, environment.name(), platform.name());

        // then
        assertEquals(value, response);
    }

    @Test
    public void testGetPlatformProperty()
    {
        // given
        final String name = RandomStringUtils.randomAlphabetic(8);

        final String value = RandomStringUtils.randomAlphabetic(4);
        when(platformPropertyRepository.findByPropertyName(eq(name))).thenReturn(value);

        // when
        final String response = platformConfigService.getPlatformProperty(name);

        // then
        assertEquals(value, response);
    }
}