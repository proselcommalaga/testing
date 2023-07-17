package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.*;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiImplementation;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal.IApiManagerServiceModalityBased;
import com.bbva.enoa.platformservices.coreservice.common.repositories.AllowedJdkRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ApiVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.Utils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDockerRegistryClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

class ReleaseVersionEntityBuilderServiceImplTest
{

    @InjectMocks
    private ReleaseVersionEntityBuilderServiceImpl releaseVersionEntityBuilderService;

    @Mock private IToolsClient toolsService;
    @Mock private ApiVersionRepository apiVersionRepository;
    @Mock private EntityManager entityManager;
    @Mock private IVersioncontrolsystemClient vcsClient;
    @Mock private IDockerRegistryClient dockerRegistryClient;
    @Mock private AllowedJdkRepository allowedJdkRepository;
    @Mock private IApiManagerServiceModalityBased apiManagerServiceModalityBased;
    @Mock private Utils utils;


    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    void tearDown()
    {
        verifyNoMoreInteractions(
                this.allowedJdkRepository,
                this.toolsService,
                this.apiVersionRepository,
                this.entityManager,
                this.dockerRegistryClient
        );
    }

    @Test
    void buildReleaseVersionEntityFromDto()
    {
        // given
        Release release = mock(Release.class);
        RVVersionDTO versionDTO = mock(RVVersionDTO.class);
        final String ivUser = "XE00000";
        final boolean isIvUserPlatformAdmin = true;

        // when
        mockReleaseVersionApis(versionDTO);
        validateReleaseVersionSubsystemAndServices(versionDTO);
        when(release.getName()).thenReturn("releaseName");
        when(versionDTO.getDescription()).thenReturn("description");
        when(versionDTO.getVersionName()).thenReturn("versionName");
        Arrays.stream(versionDTO.getSubsystems()).forEach(rvVersionSubsystemDTO -> {
            buildReleaseVersionSubsystemFromDTO(rvVersionSubsystemDTO, release.getName(), new HashSet<>(), ivUser, isIvUserPlatformAdmin);
        });

        // then
        Assertions.assertDoesNotThrow(() -> this.releaseVersionEntityBuilderService.buildReleaseVersionEntityFromDto(release, versionDTO, ivUser, isIvUserPlatformAdmin));
        verify(this.toolsService, times(2)).getSubsystemById(1);
        verify(this.entityManager, times(1)).flush();
        verify(vcsClient, times(1)).getFileFromProject(any(), anyInt(), any());
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class BuildReleaseVersionEntityFromDto
    {
        @Test
        @Tag("BuildReleaseVersionEntityFromDto")
        @DisplayName("(BuildReleaseVersionEntityFromDto) -> Is the happy path working?")
        void testHappyPath()
        {
            // given
            Release release = mock(Release.class);
            RVReleaseVersionDTO releaseVersionDto = mock(RVReleaseVersionDTO.class);
            final String ivUser = "XE00000";
            final boolean isIvUserPlatformAdmin = true;

            RVReleaseVersionSubsystemDTO rvReleaseVersionSubsystemDTO = mock(RVReleaseVersionSubsystemDTO.class);
            RVReleaseVersionServiceDTO rvReleaseVersionServiceDTO = mock(RVReleaseVersionServiceDTO.class);
            RVApiDTO rvApiDTO = mock(RVApiDTO.class);
            TOSubsystemDTO toSubsystemDTO = mock(TOSubsystemDTO.class);

            // when
            when(release.getReleaseVersions()).thenReturn(new ArrayList<>());
            when(release.getName()).thenReturn("releaseName");

            when(releaseVersionDto.getSubsystems()).thenReturn(new RVReleaseVersionSubsystemDTO[]{rvReleaseVersionSubsystemDTO});
            when(releaseVersionDto.getVersionName()).thenReturn("versionName");

            when(rvReleaseVersionSubsystemDTO.getServices()).thenReturn(new RVReleaseVersionServiceDTO[]{rvReleaseVersionServiceDTO});
            when(rvReleaseVersionSubsystemDTO.getProductSubsystemName()).thenReturn("productSubsystemName");

            when(rvReleaseVersionServiceDTO.getApisServed()).thenReturn(new RVApiDTO[]{rvApiDTO});
            when(rvReleaseVersionServiceDTO.getGroupId()).thenReturn("groupId");
            when(rvReleaseVersionServiceDTO.getArtifactId()).thenReturn("artifactId");
            when(rvReleaseVersionServiceDTO.getVersion()).thenReturn("version");
            when(rvReleaseVersionServiceDTO.getServiceName()).thenReturn("serviceName");
            when(rvReleaseVersionServiceDTO.getServiceType()).thenReturn(ServiceType.NOVA.getServiceType());

            when(rvApiDTO.getId()).thenReturn(1);
            when(rvApiDTO.getConsumedApis()).thenReturn(new RVApiConsumedBy[]{});

            when(toolsService.getSubsystemById(anyInt())).thenReturn(toSubsystemDTO);
            when(apiManagerServiceModalityBased.createApiImplementation(any(), any(), any())).thenReturn(new AsyncBackToBackApiImplementation());
            when(utils.streamOfNullable(any(), any())).thenReturn(Stream.empty());
            when(utils.listOfNullable(any())).thenReturn(List.of());

            // then
            Assertions.assertDoesNotThrow(() -> releaseVersionEntityBuilderService.buildReleaseVersionEntityFromDto(release, releaseVersionDto, ivUser, isIvUserPlatformAdmin));
            verify(entityManager, times(1)).flush();
            verify(toolsService, times(2)).getSubsystemById(anyInt());
            verify(apiVersionRepository, times(1)).findById(anyInt());
            verify(vcsClient, times(1)).getFileFromProject(any(), anyInt(), any());
        }
    }

    /**
     * Generate every mock needed for the private method checkReleaseVersionApis. This method will check the happy path.
     */
    private static void mockReleaseVersionApis(RVVersionDTO rv)
    {
        // given
        RVVersionSubsystemDTO subsystem = mock(RVVersionSubsystemDTO.class);
        RVReleaseVersionServiceDTO service = mock(RVReleaseVersionServiceDTO.class);
        RVApiDTO apiServed = mock(RVApiDTO.class);

        // when
        when(rv.getSubsystems()).thenReturn(new RVVersionSubsystemDTO[]{subsystem});
        when(subsystem.getServices()).thenReturn(new RVReleaseVersionServiceDTO[]{service});
        when(service.getApisServed()).thenReturn(new RVApiDTO[]{apiServed});
        when(apiServed.getId()).thenReturn(1);
    }

    /**
     * Generate every mock needed for the private method validateReleaseVersionSubsystemAndServices. This method will check the happy path.
     */
    private void validateReleaseVersionSubsystemAndServices(RVVersionDTO rv)
    {
        // given
        RVVersionSubsystemDTO subsystem = mock(RVVersionSubsystemDTO.class);
        RVSubsystemBaseDTO subsystemBaseDTO = mock(RVSubsystemBaseDTO.class);
        RVReleaseVersionServiceDTO service = mock(RVReleaseVersionServiceDTO.class);


        // when
        when(rv.getSubsystems()).thenReturn(new RVVersionSubsystemDTO[]{subsystem});
        when(rv.getVersionName()).thenReturn("rvVersion");
        when(subsystem.getSubsystem()).thenReturn(subsystemBaseDTO);
        when(subsystem.getServices()).thenReturn(new RVReleaseVersionServiceDTO[]{service});
        when(service.getGroupId()).thenReturn("groupId");
        when(service.getArtifactId()).thenReturn("artifactId");
        when(service.getVersion()).thenReturn("version");
        when(service.getServiceName()).thenReturn("serviceName");
        when(service.getServiceType()).thenReturn(ServiceType.NOVA.getServiceType());

        // then
        checkVersionSubsystemUniqueOrLibrary(rv.getVersionName(), subsystem.getSubsystem(), new HashMap<>());
        checkUniqueServiceNames(rv.getVersionName(), subsystem.getSubsystem().getSubsystemName(), subsystem.getServices(), new HashMap<>());

    }

    /**
     * Generate every mock needed for the private method checkVersionSubsystemUniqueOrLibrary. This method will check the happy path.
     *
     * @param versionName release version name
     * @param subsystem subsystem contained in the release version
     * @param subsystemMap empty map to check if there is multi-tag
     */
    private void checkVersionSubsystemUniqueOrLibrary(String versionName, RVSubsystemBaseDTO subsystem, Map<String, RVSubsystemBaseDTO> subsystemMap)
    {
        // when
        when(subsystem.getSubsystemName()).thenReturn("subsystem");
        when(subsystem.getSubsystemType()).thenReturn(SubsystemType.NOVA.getType());
    }

    private void checkUniqueServiceNames(String rvName, String subsystemName,
                                         RVReleaseVersionServiceDTO[] services, Map<String, String> subsystemNameByServiceName)
    {
        Arrays.stream(services).sequential().forEach(service -> when(service.getServiceName()).thenReturn("serviceName"));
    }

    private void buildReleaseVersionSubsystemFromDTO(RVVersionSubsystemDTO versionSubsystemDTO,
                                                     String releaseName, Set<String> serviceVersionSet, String ivUser, boolean isIvUserPlatformAdmin)
    {
        // given
        RVTagDTO rvTagDTO = mock(RVTagDTO.class);
        TOSubsystemDTO tOSubsystemDTO = mock(TOSubsystemDTO.class);

        // when
        when(versionSubsystemDTO.getTag()).thenReturn(rvTagDTO);
        Optional.ofNullable(versionSubsystemDTO.getSubsystem()).ifPresentOrElse(rvSubsystemBaseDTO -> when(rvSubsystemBaseDTO.getId()).thenReturn(1), () -> {
            RVSubsystemBaseDTO rvSubsystemBaseDTO = mock(RVSubsystemBaseDTO.class);
            when(versionSubsystemDTO.getSubsystem()).thenReturn(rvSubsystemBaseDTO);
            when(rvSubsystemBaseDTO.getId()).thenReturn(1);
        });
        when(this.toolsService.getSubsystemById(anyInt())).thenReturn(tOSubsystemDTO);

    }


}