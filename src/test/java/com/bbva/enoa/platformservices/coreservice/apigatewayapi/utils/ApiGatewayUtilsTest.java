package com.bbva.enoa.platformservices.coreservice.apigatewayapi.utils;

import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApiGatewayUtilsTest
{
    @Mock
    private EtherUtils etherUtils;
    @InjectMocks
    private ApiGatewayUtils apiGatewayUtils;

    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyInteractions()
    {
        verifyNoMoreInteractions(
                etherUtils
        );
    }

    @Nested
    class BuildServiceLocation
    {
        String releaseName = "releaseName";
        String environment = "environment";
        String artifactId = "artifactId";
        String groupId = "groupId";
        String version = "1.0.0";

        @Test
        @DisplayName("Build service location -> invalid infrastructure ok")
        public void invalid()
        {
            var releaseVersionService = Mockito.mock(ReleaseVersionService.class);

            String ret = apiGatewayUtils.buildServiceLocation(releaseVersionService, "INVALID_INFRA", releaseName, environment);

            assertEquals("", ret);
        }

        @Test
        @DisplayName("Build service location -> NOVA ok")
        public void nova()
        {
            var releaseVersionService = Mockito.mock(ReleaseVersionService.class);

            when(releaseVersionService.getGroupId()).thenReturn(groupId);
            when(releaseVersionService.getArtifactId()).thenReturn(artifactId);
            when(releaseVersionService.getVersion()).thenReturn(version);

            String ret = apiGatewayUtils.buildServiceLocation(releaseVersionService, Platform.NOVA.name(), releaseName, environment);

            assertEquals("groupid-artifactid-releasename-1", ret);

        }

        @Test
        @DisplayName("Build service location -> ETHER ok")
        public void ether()
        {
            var serviceLocation = "serviceLocation";
            var releaseVersionService = Mockito.mock(ReleaseVersionService.class);

            when(releaseVersionService.getGroupId()).thenReturn(groupId);
            when(releaseVersionService.getArtifactId()).thenReturn(artifactId);
            when(releaseVersionService.getVersion()).thenReturn(version);

            when(etherUtils.getEAEServiceURL(any(), any(), any(), any(), any())).thenReturn(serviceLocation);

            String ret = apiGatewayUtils.buildServiceLocation(releaseVersionService, Platform.ETHER.name(), releaseName, environment);

            verify(etherUtils).getEAEServiceURL(environment, groupId, artifactId, releaseName, version);

            assertEquals(serviceLocation, ret);

        }
    }
}