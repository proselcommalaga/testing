package com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils;

import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PlatformDiscoveryConfigRepository;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EtherUtilsTest
{
    public static final String ETHER_PROTOCOL = "etherProtocol";
    public static final String BASE_DOMAIN = "baseDomain";
    @Mock
    private PlatformDiscoveryConfigRepository platformDiscoveryRepository;

    private EtherUtils etherUtils;

    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        this.etherUtils = new EtherUtils(platformDiscoveryRepository, ETHER_PROTOCOL);
    }

    @AfterEach
    public void verifyInteractions()
    {
        verifyNoMoreInteractions(
                platformDiscoveryRepository
        );
    }

    @Nested
    class GetEAEServiceURL
    {
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String      groupId     = "groupId";
        final String serviceName = "serviceName";
        final String releaseName = "releaseName";
        final String serviceVersion = "1.0.0";

        @Test
        @DisplayName("Get EAE service URL -> OK")
        public void ok()
        {
            var baseDomain = "__baseDomain__";

            when(platformDiscoveryRepository.findByNameAndEnvironmentAndPlatform(anyString(), any(), any())).thenReturn(baseDomain);

            String ret = etherUtils.getEAEServiceURL(environment.getEnvironment(), groupId, serviceName, releaseName, serviceVersion);

            verify(platformDiscoveryRepository).findByNameAndEnvironmentAndPlatform(BASE_DOMAIN, environment.getEnvironment(), Platform.ETHER);

            assertEquals(
                    String.format(
                            "%s://%s",
                            ETHER_PROTOCOL,
                            String.format("%s-%s-%s-%s-%s", groupId, serviceName, releaseName, environment, "1").toLowerCase()
                    ).concat(".").concat(baseDomain).toLowerCase()
                    , ret
            );
        }

    }


}