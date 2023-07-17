package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.common.entities.LobFile;
import com.bbva.enoa.datamodel.model.common.enumerates.AsyncStatus;
import com.bbva.enoa.datamodel.model.config.enumerates.ManagementType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerPropertiesConfiguration;
import com.bbva.enoa.platformservices.coreservice.common.model.param.ServiceOperationParams;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ConfigurationmanagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IContinuousintegrationClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.interfaces.IQualityManagerService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IAsyncApiService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IBatchScheduleService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import static org.mockito.Mockito.*;

class ReleaseVersionDtoBuilderServiceImplTest
{
    protected static final String CLASSPATH_NOVA_ASYNCAPI_VALID = "classpath:novayml/nova_java_spring_boot_asyncapi.yml";


    @Mock
    private IContinuousintegrationClient iContinuousintegrationClient;
    @Mock
    private ConfigurationmanagerClient configurationmanagerClient;
    @Mock
    private ToolsClient toolsClient;
    @Mock
    private IVersioncontrolsystemClient versioncontrolsystemClient;
    @Mock
    private ILibraryManagerService libraryManagerService;
    @Mock
    private IAsyncApiService asyncApiService;
    @Mock
    private IBatchScheduleService batchScheduleService;
    @Mock
    private IReleaseVersionValidator releaseVersionValidator;
    @Mock
    private ReleaseVersionSubsystemRepository releaseVersionSubsystemRepository;
    @Mock
    private ReleaseVersionRepository releaseVersionRepository;
    @Mock
    private IQualityManagerService qualityManagerService;
    @Mock
    private IBrokerPropertiesConfiguration brokerPropertiesConfiguration;
    @InjectMocks
    private ReleaseVersionDtoBuilderServiceImpl releaseVersionDtoBuilderService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(this.releaseVersionDtoBuilderService, "isIssueTrackerEnabled", true);
    }

    @Test
    void buildSubsystems()
    {
        Product product = new Product();
        ReleaseVersion releaseVersion = new ReleaseVersion();

        doNothing().when(this.batchScheduleService).addBatchSchedulServices(any(ReleaseVersion.class));
        this.releaseVersionDtoBuilderService.buildSubsystems(product, releaseVersion, "CODE");
        verify(this.iContinuousintegrationClient, times(1)).buildSubsystems(product, releaseVersion, "CODE");
    }


    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class SubsystemBuildStatus
    {
        @Test
        @DisplayName("(SubsystemBuildStatus) -> Is the happy path correct?")
        void subsystemBuildStatusHappyPath()
        {
            // given
            ReleaseVersionSubsystem releaseVersionSubsystem = mock(ReleaseVersionSubsystem.class);
            final Integer releaseVersionSubsystemId = 1;
            final String jenkinsJobGroupMessageInfo = "Group Message Information";
            final String jenkinsJobGroupStatus = Constants.SUCCESS;
            final String ivUser = "XE00000";
            ReleaseVersion releaseVersion = mock(ReleaseVersion.class);
            Release release = mock(Release.class);
            Product product = mock(Product.class);

            // when
            when(releaseVersionSubsystemRepository.findById(anyInt())).thenReturn(Optional.of(releaseVersionSubsystem));
            when(releaseVersionValidator.saveAndFlushReleaseVersionSubsystem(any())).thenReturn(releaseVersionSubsystem);
            when(releaseVersionSubsystem.getId()).thenReturn(1);
            when(releaseVersionSubsystem.getStatus()).thenReturn(AsyncStatus.DONE);
            when(releaseVersionSubsystem.getSubsystemId()).thenReturn(1);

            when(releaseVersionRepository.releaseVersionOfSubsystem(anyInt())).thenReturn(releaseVersion);
            when(releaseVersion.getStatus()).thenReturn(ReleaseVersionStatus.BUILDING);
            when(releaseVersion.getSubsystems()).thenReturn(List.of(releaseVersionSubsystem));

            doNothing().when(releaseVersionValidator).checkReleaseVersionExistance(any());

            when(releaseVersion.getRelease()).thenReturn(release);
            when(release.getProduct()).thenReturn(product);
            when(product.getCreateJiraIssues()).thenReturn(false);


            // then
            Assertions.assertDoesNotThrow(() -> releaseVersionDtoBuilderService.subsystemBuildStatus(releaseVersionSubsystemId, jenkinsJobGroupMessageInfo, jenkinsJobGroupStatus, ivUser));
        }

        @Test
        @DisplayName("(SubsystemBuildStatus) -> Exception on not Release Version found?")
        void subsystemBuildStatusNotFound()
        {
            // given
            ReleaseVersionSubsystem releaseVersionSubsystem = mock(ReleaseVersionSubsystem.class);
            ReleaseVersion releaseVersion = mock(ReleaseVersion.class);
            final Integer releaseVersionSubsystemId = 1;
            final String jenkinsJobGroupMessageInfo = "Group Message Information";
            final String jenkinsJobGroupStatus = Constants.SUCCESS;
            final String ivUser = "XE00000";

            // when
            when(releaseVersionSubsystemRepository.findById(anyInt())).thenReturn(Optional.empty());

            // then
            Assertions.assertThrows(NovaException.class, () -> releaseVersionDtoBuilderService.subsystemBuildStatus(releaseVersionSubsystemId, jenkinsJobGroupMessageInfo, jenkinsJobGroupStatus, ivUser));
        }

        @Test
        @DisplayName("(SubsystemBuildStatus) -> AsyncStatus.ERROR and release version with errors?")
        void subsystemCheckWithSubsystemErrors()
        {
            // given
            ReleaseVersionSubsystem releaseVersionSubsystem = mock(ReleaseVersionSubsystem.class);
            ReleaseVersion releaseVersion = mock(ReleaseVersion.class);
            final Integer releaseVersionSubsystemId = 1;
            final String jenkinsJobGroupMessageInfo = "Group Message Information";
            final String jenkinsJobGroupStatus = Constants.SUCCESS;
            final String ivUser = "XE00000";

            TOSubsystemDTO toSubsystemDTO = mock(TOSubsystemDTO.class);

            // when
            when(releaseVersionSubsystemRepository.findById(anyInt())).thenReturn(Optional.of(releaseVersionSubsystem));
            when(releaseVersionValidator.saveAndFlushReleaseVersionSubsystem(any())).thenReturn(releaseVersionSubsystem);
            when(releaseVersionRepository.releaseVersionOfSubsystem(anyInt())).thenReturn(releaseVersion);
            doNothing().when(releaseVersionValidator).checkReleaseVersionExistance(any());

            when(releaseVersionSubsystem.getId()).thenReturn(1);
            when(releaseVersionSubsystem.getStatus()).thenReturn(AsyncStatus.ERROR);
            when(releaseVersionSubsystem.getSubsystemId()).thenReturn(1);

            when(releaseVersion.getStatus()).thenReturn(ReleaseVersionStatus.BUILDING);
            when(releaseVersion.getSubsystems()).thenReturn(List.of(releaseVersionSubsystem));

            when(toolsClient.getSubsystemById(anyInt())).thenReturn(toSubsystemDTO);

            when(releaseVersionValidator.saveAndFlushReleaseVersion(any())).thenReturn(releaseVersion);

            // then
            Assertions.assertDoesNotThrow(() -> releaseVersionDtoBuilderService.subsystemBuildStatus(releaseVersionSubsystemId, jenkinsJobGroupMessageInfo, jenkinsJobGroupStatus, ivUser));
        }

        @Test
        @DisplayName("(SubsystemBuildStatus) -> Release version in COMPILING ERROR")
        void subsystemCheckWithCompilingError()
        {
            // given
            ReleaseVersionSubsystem releaseVersionSubsystem = mock(ReleaseVersionSubsystem.class);
            ReleaseVersion releaseVersion = mock(ReleaseVersion.class);
            final Integer releaseVersionSubsystemId = 1;
            final String jenkinsJobGroupMessageInfo = "Group Message Information";
            final String jenkinsJobGroupStatus = Constants.SUCCESS;
            final String ivUser = "XE00000";

            // when
            when(releaseVersionSubsystemRepository.findById(anyInt())).thenReturn(Optional.of(releaseVersionSubsystem));
            when(releaseVersionValidator.saveAndFlushReleaseVersionSubsystem(any())).thenReturn(releaseVersionSubsystem);
            when(releaseVersionRepository.releaseVersionOfSubsystem(anyInt())).thenReturn(releaseVersion);
            doNothing().when(releaseVersionValidator).checkReleaseVersionExistance(any());

            when(releaseVersionSubsystem.getId()).thenReturn(1);
            when(releaseVersionSubsystem.getStatus()).thenReturn(AsyncStatus.ERROR);

            when(releaseVersion.getStatus()).thenReturn(ReleaseVersionStatus.ERRORS);
            when(releaseVersion.getSubsystems()).thenReturn(List.of(releaseVersionSubsystem));

            // then
            Assertions.assertDoesNotThrow(() -> releaseVersionDtoBuilderService.subsystemBuildStatus(releaseVersionSubsystemId, jenkinsJobGroupMessageInfo, jenkinsJobGroupStatus, ivUser));
        }
    }

    @Test
    void processTemplates()
    {
        ReleaseVersionService releaseVersionService1 = new ReleaseVersionService();
        releaseVersionService1.setServiceType(ServiceType.NOVA.getServiceType());
        releaseVersionService1.setFolder("folder");
        releaseVersionService1.setNovaYml(new LobFile());
        ReleaseVersionService releaseVersionService2 = new ReleaseVersionService();
        releaseVersionService2.setServiceType(ServiceType.EPHOENIX_ONLINE.getServiceType());
        releaseVersionService2.setFolder("folder");
        releaseVersionService2.setNovaYml(new LobFile());
        ReleaseVersionService releaseVersionService3 = new ReleaseVersionService();
        releaseVersionService3.setServiceType(ServiceType.DEPENDENCY.getServiceType());
        releaseVersionService3.setFolder("folder");
        releaseVersionService3.setNovaYml(new LobFile());
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        releaseVersionSubsystem.getServices().add(releaseVersionService1);
        releaseVersionSubsystem.getServices().add(releaseVersionService2);
        releaseVersionSubsystem.getServices().add(releaseVersionService3);
        Release release = new Release();
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        releaseVersion.getSubsystems().add(releaseVersionSubsystem);
        TOSubsystemDTO productSubsystem = new TOSubsystemDTO();
        releaseVersionSubsystem.setSubsystemId(1);
        when(this.toolsClient.getSubsystemById(1)).thenReturn(productSubsystem);
        Assertions.assertDoesNotThrow(() -> this.releaseVersionDtoBuilderService.processTemplates(releaseVersion, "CODE"));
    }

    @Test
    void processTemplatesNoNovaYml()
    {
        ReleaseVersionService rvs = new ReleaseVersionService();
        rvs.setFolder("folder");
        rvs.setNovaYml(null);
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        releaseVersionSubsystem.getServices().add(rvs);
        Release release = new Release();
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setId(RandomUtils.nextInt(0, 9999));
        releaseVersion.setRelease(release);
        releaseVersion.getSubsystems().add(releaseVersionSubsystem);
        TOSubsystemDTO productSubsystem = new TOSubsystemDTO();

        //No nova.yml no has to produce any error processing templates
        int subsystemId = 0;
        EnumSet<ServiceType> set = EnumSet.complementOf(EnumSet.of(ServiceType.BATCH_SCHEDULER_NOVA, ServiceType.INVALID));
        for (ServiceType serviceType : set)
        {
            Mockito.reset(toolsClient);
            releaseVersionSubsystem.setSubsystemId(++subsystemId);
            rvs.setServiceType(serviceType.getServiceType());
            when(this.toolsClient.getSubsystemById(subsystemId)).thenReturn(productSubsystem);
            this.releaseVersionDtoBuilderService.processTemplates(releaseVersion, "CODE");
            Mockito.verify(libraryManagerService, Mockito.never()).saveUsedLibraries(any(), any());
            Mockito.verify(toolsClient, Mockito.only()).getSubsystemById(subsystemId);
        }

        //nova.yml but.... no content has to no fail
        rvs.setNovaYml(new LobFile());
        for (ServiceType serviceType : set)
        {
            Mockito.reset(toolsClient);
            releaseVersionSubsystem.setSubsystemId(++subsystemId);
            rvs.setServiceType(serviceType.getServiceType());
            when(this.toolsClient.getSubsystemById(subsystemId)).thenReturn(productSubsystem);
            this.releaseVersionDtoBuilderService.processTemplates(releaseVersion, "CODE");
            Mockito.verify(libraryManagerService, Mockito.never()).saveUsedLibraries(any(), any());
            Mockito.verify(toolsClient, Mockito.only()).getSubsystemById(subsystemId);
        }
    }

    @Test
    @Disabled("Pending to review why is failing.")
    void processTemplatesWithBrokerProperties_ok() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_NOVA_ASYNCAPI_VALID);
        FileInputStream fis = new FileInputStream(file);
        byte[] novayml = new byte[(int) file.length()];
        fis.read(novayml);

        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setServiceType(ServiceType.API_REST_JAVA_SPRING_BOOT.getServiceType());
        releaseVersionService.setFolder("folder");
        releaseVersionService.setId(1);
        releaseVersionService.setServiceName("serviceName");
        LobFile novaLobFile = new LobFile();
        novaLobFile.setContents(new String(novayml));
        releaseVersionService.setNovaYml(novaLobFile);

        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
        releaseVersionSubsystem.setTagName("tag");
        releaseVersionSubsystem.getServices().add(releaseVersionService);
        releaseVersionSubsystem.setSubsystemId(5);
        releaseVersionSubsystem.setTagUrl("tag URL");

        Release release = new Release();
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        releaseVersion.getSubsystems().add(releaseVersionSubsystem);

        releaseVersionService.setVersionSubsystem(releaseVersionSubsystem);

        TOSubsystemDTO productSubsystem = new TOSubsystemDTO();
        productSubsystem.setRepoId(4);

        Set<String> asyncapiList = new HashSet<>();
        asyncapiList.add("src/main/resources/backToBackApi1.yml");
        asyncapiList.add("src/main/resources/backToBackApi2.yml");

        Map<String, String> brokerProperties = new HashMap<>();
        brokerProperties.put("spring.cloud.stream.bindings.myChannel1Operation-out-0.destination","abcd_mychannel1_v_1");
        brokerProperties.put("spring.cloud.stream.bindings.myChannel2Operation-out-0.destination","abcd_mychannel2_v_1");
        brokerProperties.put("spring.cloud.function.definition","myChannel2Operation;myChannel1Operation");
        Map<String, Map<String, String>> propertiesByTypeMap = new HashMap<>();
        propertiesByTypeMap.put(ManagementType.BROKER.name(), brokerProperties);


        when(this.asyncApiService.getReleaseVersionServiceBrokerProperties(releaseVersionService)).thenReturn(brokerProperties);
        when(this.toolsClient.getSubsystemById(5)).thenReturn(productSubsystem);

        // call method function
        this.releaseVersionDtoBuilderService.processTemplates(releaseVersion, "CODE");

        // verify
        Mockito.verify(libraryManagerService, Mockito.never()).saveUsedLibraries(any(), any());
        Mockito.verify(toolsClient, Mockito.only()).getSubsystemById(5);
        ServiceOperationParams params = (new ServiceOperationParams.ServiceOperationParamsBuilder()).modulePath("folder").tag("tag").versionControlServiceId(4).ivUser("CODE").serviceType(ServiceType.API_REST_JAVA_SPRING_BOOT.name()).releaseVersionServiceId(1).releaseVersionServiceName("serviceName").libraries(new ArrayList<>()).extraProperties(propertiesByTypeMap).build();
        Mockito.verify(configurationmanagerClient, times(1)).setPropertiesDefinitionService(params);
    }

}