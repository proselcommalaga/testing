package com.bbva.enoa.platformservices.coreservice.releasesapi.services.impl;

import com.bbva.enoa.apirestgen.releasesapi.model.*;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.ProductStatus;
import com.bbva.enoa.datamodel.model.release.entities.*;
import com.bbva.enoa.datamodel.model.release.enumerates.ConfigurationType;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CPDRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.releasesapi.exceptions.ReleaseError;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseConstants;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseValidator;
import com.bbva.enoa.utils.clientsutils.model.Activity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ReleasesServiceImplTest
{
    @InjectMocks
    private ReleasesServiceImpl releasesServiceImpl;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private ReleaseDtoBuilder dtoBuilder;

    @Mock
    private ReleaseValidator validator;

    @Mock
    private CPDRepository cpdRepository;

    @Mock
    private IProductUsersClient usersService;

    @Mock
    private INovaActivityEmitter novaActivityEmitter;

    private final String ivUser = "CODE";

    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getCpdsByWrongEnv()
    {
        when(this.cpdRepository.getByEnvironment(Mockito.any())).thenReturn(Collections.emptyList());
        String[] response = this.releasesServiceImpl.getCpds("wrong");
        Assertions.assertNotNull(response);
        Assertions.assertEquals(0, response.length);
    }

    @Test
    public void getCpds()
    {
        CPD cpd = new CPD();
        cpd.setName("CPD");
        List<CPD> cpds = List.of(cpd);
        when(this.cpdRepository.getByEnvironment(Mockito.any())).thenReturn(cpds);
        for (Environment environment : Environment.values())
        {
            String[] response = this.releasesServiceImpl.getCpds(environment.getEnvironment());
            Assertions.assertEquals(1, response.length);
            Assertions.assertEquals(cpds.get(0).getName(), response[0]);
        }
    }

    @Test
    public void getSeveralCpdsByEnv()
    {
        CPD cpd1 = new CPD();
        cpd1.setName("CPD1");
        CPD cpd2 = new CPD();
        cpd2.setName("CPD2");
        CPD cpd3 = new CPD();
        cpd3.setName("CPD3");
        List<CPD> cpds = List.of(cpd1, cpd2, cpd3);
        when(this.cpdRepository.getByEnvironment(Mockito.any())).thenReturn(cpds);
        for (Environment environment : Environment.values())
        {
            String[] response = this.releasesServiceImpl.getCpds(Environment.INT.getEnvironment());
            Assertions.assertEquals(cpds.size(), response.length);
        }
    }

    @Test
    public void createReleaseNoProduct()
    {
        //Given
        NewReleaseRequest releaseRequest = new NewReleaseRequest();
        releaseRequest.setProductId(1);
        releaseRequest.setReleaseName("name");
        releaseRequest.setDescription("description");

        //When
        when(this.productRepository.findById(releaseRequest.getProductId())).thenReturn(Optional.empty());
        doCallRealMethod().when(validator).checkProductExistence(eq(releaseRequest.getProductId()), any());

        //Then
        Assertions.assertThrows(NovaException.class, () -> this.releasesServiceImpl.createRelease(this.ivUser, releaseRequest));
    }

    @Test
    public void createReleaseWrongReleaseName()
    {
        //Given
        NewReleaseRequest releaseRequest = new NewReleaseRequest();
        releaseRequest.setProductId(1);
        releaseRequest.setReleaseName("wrong name");
        releaseRequest.setDescription("description");

        Product product = new Product();
        product.setName("product1");
        product.setId(releaseRequest.getProductId());

        //When
        when(this.productRepository.findById(releaseRequest.getProductId())).thenReturn(Optional.of(product));
        doCallRealMethod().when(validator).checkReleaseName(anyString());

        //Then
        Assertions.assertThrows(NovaException.class, () -> this.releasesServiceImpl.createRelease(this.ivUser, releaseRequest));
    }

    @Test
    public void createReleaseSameName()
    {
        //Given
        NewReleaseRequest releaseRequest = new NewReleaseRequest();
        releaseRequest.setProductId(1);
        releaseRequest.setReleaseName("name");
        releaseRequest.setDescription("description");

        Product product = new Product();
        product.setName("product1");
        product.setId(releaseRequest.getProductId());

        //When
        when(this.productRepository.findById(releaseRequest.getProductId())).thenReturn(Optional.of(product));
        doThrow(new NovaException(ReleaseError.getReleaseNameDuplicatedError()))
                .when(validator).existsReleaseWithSameName(releaseRequest.getProductId(), releaseRequest.getReleaseName());

        //Then
        Assertions.assertThrows(NovaException.class, () -> this.releasesServiceImpl.createRelease(this.ivUser, releaseRequest));
    }

    @Test
    public void createReleaseNoPermissions() throws Exception
    {
        NewReleaseRequest releaseRequest = new NewReleaseRequest();
        releaseRequest.setProductId(1);
        releaseRequest.setReleaseName("name");
        releaseRequest.setDescription("description");

        Product product = new Product();
        product.setName("product1");
        product.setId(releaseRequest.getProductId());

        //When
        when(this.productRepository.findById(releaseRequest.getProductId())).thenReturn(Optional.of(product));
        doThrow(new NovaException(ReleaseError.getForbiddenError()))
                .when(usersService)
                .checkHasPermission(eq(ivUser), eq(ReleaseConstants.CREATE_RELEASE_PERMISSION), eq(releaseRequest.getProductId()), any(Exception.class));

        //Then
        Assertions.assertThrows(NovaException.class, () -> this.releasesServiceImpl.createRelease(this.ivUser, releaseRequest));
    }


    @Test
    public void createReleaseInProductWithoutReleases()
    {
        NewReleaseRequest releaseRequest = new NewReleaseRequest();
        releaseRequest.setProductId(RandomUtils.nextInt(0, 100));
        releaseRequest.setReleaseName("releaseName");
        releaseRequest.setDescription("Release description");

        Product product = createEmptyRandomProduct(releaseRequest.getProductId());
        Assertions.assertTrue(product.getReleases().isEmpty());

        for (int i = 0; i < 16; i++)
        {
            Mockito.reset(novaActivityEmitter);
            product.setDefaultAutodeployInPre((i & 1) != 0);
            product.setDefaultAutodeployInPro((i & (1L << 1)) != 0);
            product.setDefaultAutomanageInPre((i & (1L << 2)) != 0);
            product.setDefaultAutomanageInPro((i & (1L << 3)) != 0);

            //When
            when(this.productRepository.findById(releaseRequest.getProductId())).thenReturn(Optional.of(product));
            this.releasesServiceImpl.createRelease(this.ivUser, releaseRequest);

            //Then
            this.assertionsForLastReleaseByProduct(product, releaseRequest, i + 1);

            //activity
            verify(novaActivityEmitter, times(1)).emitNewActivity(any());
        }
    }

    @Test
    void getCpdsHistoricalEmpty()
    {
        when(cpdRepository.findAllByOrderByNameAsc()).thenReturn(Collections.emptyList());
        Assertions.assertNotNull(this.releasesServiceImpl.getCpdsHistorical());
    }

    @Test
    void getCpdsHistorical()
    {
        List<CPD> cpds = new ArrayList<>();
        CPD cpd;
        for (Environment environment : Environment.values())
        {
            cpd = new CPD();
            cpd.setEnvironment(environment.getEnvironment());
            cpd.setId(environment.ordinal());
            cpd.setName("cpd-" + environment);
            cpd.setRegistry("registry");
            cpd.setLabel("label-" + environment);
            cpd.setFilesystem("filesystem-" + environment);
            cpd.setElasticSearchCPDName("ES-" + environment);
            cpd.setAddress("address-" + environment);
            cpd.setActive(true);
            cpds.add(cpd);
        }

        when(cpdRepository.findAllByOrderByNameAsc()).thenReturn(cpds);
        String[] current = this.releasesServiceImpl.getCpdsHistorical();
        Assertions.assertNotNull(current);
        Assertions.assertEquals(cpds.size(), current.length);
        for (int i = 0; i < current.length; i++)
        {
            Assertions.assertEquals(cpds.get(i).getName(), current[i]);
        }
    }

    @Test
    public void releaseInfo()
    {
        int id = RandomUtils.nextInt(1, 1000);
        Release release = new Release();
        release.setId(id);
        ReleaseDto releaseDto = new ReleaseDto();
        releaseDto.setId(id);

        when(this.validator.checkReleaseExistence(id)).thenReturn(release);
        when(this.dtoBuilder.buildReleaseDtoFromEntity(release)).thenReturn(releaseDto);
        ReleaseDto response = this.releasesServiceImpl.releaseInfo(id);
        Assertions.assertEquals(releaseDto, response);
        Mockito.verify(validator, times(1)).checkReleaseExistence(Mockito.eq(id));
        Mockito.verify(dtoBuilder, times(1)).buildReleaseDtoFromEntity(Mockito.eq(release));
    }

    @Test
    void getAllReleasesAndServicesCatchRuntime()
    {
        int id = -1;
        when(dtoBuilder.buildReleaseInfoList(id)).thenThrow(new RuntimeException());
        Assertions.assertThrows(LogAndTraceException.class, () -> releasesServiceImpl.getAllReleasesAndServices(id));
    }

    @Test
    void getAllReleasesAndServices()
    {
        int id = RandomUtils.nextInt(1, 100);
        RELReleaseInfo[] relReleaseInfos = new RELReleaseInfo[]{new RELReleaseInfo()};
        when(dtoBuilder.buildReleaseInfoList(id)).thenReturn(relReleaseInfos);
        RELReleaseInfo[] current = releasesServiceImpl.getAllReleasesAndServices(id);
        Assertions.assertEquals(relReleaseInfos, current);
        verify(dtoBuilder, times(1)).buildReleaseInfoList(id);
    }

    @Test
    void getProductReleasesCatchRuntimeException()
    {
        int id = -1;
        when(productRepository.findById(id)).thenThrow(new RuntimeException());
        // release
        Assertions.assertThrows(LogAndTraceException.class,
                () -> releasesServiceImpl.getProductReleases(ivUser, id),
                "ReleaseServiceImpl has to catch RuntimeException and throw a LogAndTraceException");
    }

    @Test
    void getProductReleasesNoProduct()
    {
        int id = -1;
        when(productRepository.findById(id)).thenReturn(Optional.empty());
        Mockito.doCallRealMethod().when(validator).checkProductExistence(id, null);
        // release
        Assertions.assertThrows(LogAndTraceException.class,
                () -> releasesServiceImpl.getProductReleases(ivUser, id),
                "ReleaseServiceImpl has to catch exception of validation");
    }

    @Test
    void getProductReleases()
    {
        int id = RandomUtils.nextInt(1, 1000);
        Product product = new Product();
        List<Release> releases = Collections.emptyList();
        product.setReleases(releases);
        product.setId(id);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        releasesServiceImpl.getProductReleases(ivUser, id);
        verify(dtoBuilder, times(1)).buildDtoArrayFromEntityList(releases);
    }

    @Test
    void updateReleaseConfigSelectedPlatformsINT()
    {
        //check if is INT,
        String ivUser = "user";
        Product product = new Product();
        product.setId(RandomUtils.nextInt(1, 1000));

        int releaseId = RandomUtils.nextInt(1, 1000);
        Release release = new Release();
        release.setId(releaseId);
        release.setSelectedLoggingInt(Platform.NOVAETHER);
        release.setSelectedDeployInt(Platform.NOVA);
        release.setProduct(product);
        product.setReleases(List.of(release));
        List<PlatformConfig> platformConfigList = new ArrayList<>();
        PlatformConfig platformConfigDeployNova = new PlatformConfig();
        platformConfigDeployNova.setPlatform(Platform.NOVA);
        platformConfigDeployNova.setIsDefault(true);
        platformConfigDeployNova.setEnvironment(Environment.INT.getEnvironment());
        platformConfigDeployNova.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployNova.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployNova);
        PlatformConfig platformConfigDeployEther = new PlatformConfig();
        platformConfigDeployEther.setPlatform(Platform.ETHER);
        platformConfigDeployEther.setIsDefault(false);
        platformConfigDeployEther.setEnvironment(Environment.INT.getEnvironment());
        platformConfigDeployEther.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployEther.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployEther);
        PlatformConfig platformConfigDeployAws = new PlatformConfig();
        platformConfigDeployAws.setPlatform(Platform.AWS);
        platformConfigDeployAws.setIsDefault(false);
        platformConfigDeployAws.setEnvironment(Environment.INT.getEnvironment());
        platformConfigDeployAws.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployAws.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployAws);

        PlatformConfig platformConfigLoggingNova = new PlatformConfig();
        platformConfigLoggingNova.setPlatform(Platform.NOVA);
        platformConfigLoggingNova.setIsDefault(true);
        platformConfigLoggingNova.setEnvironment(Environment.INT.getEnvironment());
        platformConfigLoggingNova.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingNova.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingNova);
        PlatformConfig platformConfigLoggingEther = new PlatformConfig();
        platformConfigLoggingEther.setPlatform(Platform.ETHER);
        platformConfigLoggingEther.setIsDefault(false);
        platformConfigLoggingEther.setEnvironment(Environment.INT.getEnvironment());
        platformConfigLoggingEther.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingEther.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingEther);
        PlatformConfig platformConfigLoggingAws = new PlatformConfig();
        platformConfigLoggingAws.setPlatform(Platform.AWS);
        platformConfigLoggingAws.setIsDefault(false);
        platformConfigLoggingAws.setEnvironment(Environment.INT.getEnvironment());
        platformConfigLoggingAws.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingAws.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingAws);

        product.setPlatformConfigList(platformConfigList);

        ReleaseEnvConfigDto intConfig = new ReleaseEnvConfigDto();
        intConfig.setSelectedPlatforms(new SelectedPlatformsDto());
        intConfig.setManagementConfig(new ManagementConfigDto());
        ReleaseConfigDto releaseConfig = new ReleaseConfigDto();
        releaseConfig.setIntConfig(intConfig);

        for (Platform deployType : Arrays.stream(Platform.values()).filter(Platform::getIsValidToDeploy).collect(Collectors.toList()))
        {
            intConfig.getSelectedPlatforms().setDeploymentPlatform(deployType.name());
            for (Platform loggingType : Arrays.stream(Platform.values()).filter(Platform::getIsValidToLogging).collect(Collectors.toList()))
            {
                intConfig.getSelectedPlatforms().setLoggingPlatform(loggingType.name());
                Mockito.reset(validator);
                Mockito.reset(novaActivityEmitter);
                when(this.validator.checkReleaseExistence(releaseId)).thenReturn(release);
                when(validator.checkInputDeploymentPlatformValue(product.getDeployPlatformsAvailableByEnv(Environment.INT.getEnvironment()), intConfig, release.getSelectedDeployInt(),
                        Environment.INT.name())).thenReturn(deployType);
                when(validator.checkInputLoggingPlatformValue(product.getLoggingPlatformsAvailableByEnv(Environment.INT.getEnvironment()), intConfig,
                        release.getSelectedLoggingInt(), deployType, Environment.INT.name())).thenReturn(loggingType);

                releasesServiceImpl.updateReleaseConfig(ivUser, releaseConfig, releaseId, ReleaseConstants.ENVIRONMENT.INT);
                verify(this.validator, times(1)).checkReleaseExistence(releaseId);
                verify(this.validator, times(1)).checkProductExistence(product);
                verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(Activity.class));

                Assertions.assertEquals(deployType, release.getSelectedDeployInt());
                Assertions.assertEquals(loggingType, release.getSelectedLoggingInt());
            }
        }
    }

    @Test
    void updateReleaseConfigSelectedPlatformsPRE()
    {
        //check if is PRE,
        Product product = new Product();
        product.setId(RandomUtils.nextInt(1, 1000));

        int releaseId = RandomUtils.nextInt(1, 1000);
        Release release = new Release();
        release.setId(releaseId);
        release.setSelectedLoggingPre(Platform.NOVAETHER);
        release.setSelectedDeployPre(Platform.NOVA);
        release.setProduct(product);

        product.setReleases(List.of(release));
        List<PlatformConfig> platformConfigList = new ArrayList<>();
        PlatformConfig platformConfigDeployNova = new PlatformConfig();
        platformConfigDeployNova.setPlatform(Platform.NOVA);
        platformConfigDeployNova.setIsDefault(true);
        platformConfigDeployNova.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigDeployNova.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployNova.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployNova);
        PlatformConfig platformConfigDeployEther = new PlatformConfig();
        platformConfigDeployEther.setPlatform(Platform.ETHER);
        platformConfigDeployEther.setIsDefault(false);
        platformConfigDeployEther.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigDeployEther.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployEther.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployEther);
        PlatformConfig platformConfigDeployAws = new PlatformConfig();
        platformConfigDeployAws.setPlatform(Platform.AWS);
        platformConfigDeployAws.setIsDefault(false);
        platformConfigDeployAws.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigDeployAws.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployAws.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployAws);

        PlatformConfig platformConfigLoggingNova = new PlatformConfig();
        platformConfigLoggingNova.setPlatform(Platform.NOVA);
        platformConfigLoggingNova.setIsDefault(true);
        platformConfigLoggingNova.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigLoggingNova.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingNova.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingNova);
        PlatformConfig platformConfigLoggingEther = new PlatformConfig();
        platformConfigLoggingEther.setPlatform(Platform.ETHER);
        platformConfigLoggingEther.setIsDefault(false);
        platformConfigLoggingEther.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigLoggingEther.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingEther.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingEther);
        PlatformConfig platformConfigLoggingAws = new PlatformConfig();
        platformConfigLoggingAws.setPlatform(Platform.AWS);
        platformConfigLoggingAws.setIsDefault(false);
        platformConfigLoggingAws.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigLoggingAws.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingAws.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingAws);

        product.setPlatformConfigList(platformConfigList);
        product.setDefaultAutomanageInPre(false);
        product.setDefaultAutodeployInPre(false);

        ReleaseEnvConfigDto preConfig = new ReleaseEnvConfigDto();
        preConfig.setSelectedPlatforms(new SelectedPlatformsDto());
        preConfig.setManagementConfig(new ManagementConfigDto());
        ReleaseConfigDto releaseConfig = new ReleaseConfigDto();
        releaseConfig.setPreConfig(preConfig);

        for (Platform deployType : Arrays.stream(Platform.values()).filter(Platform::getIsValidToDeploy).collect(Collectors.toList()))
        {
            preConfig.getSelectedPlatforms().setDeploymentPlatform(deployType.name());
            for (Platform loggingType : Arrays.stream(Platform.values()).filter(Platform::getIsValidToLogging).collect(Collectors.toList()))
            {
                preConfig.getSelectedPlatforms().setLoggingPlatform(loggingType.name());
                Mockito.reset(validator);
                Mockito.reset(novaActivityEmitter);
                when(this.validator.checkReleaseExistence(releaseId)).thenReturn(release);
                when(validator.checkInputDeploymentPlatformValue(product.getDeployPlatformsAvailableByEnv(Environment.PRE.getEnvironment()), preConfig, release.getSelectedDeployPre(),
                        Environment.PRE.name())).thenReturn(deployType);
                when(validator.checkInputLoggingPlatformValue(product.getLoggingPlatformsAvailableByEnv(Environment.PRE.getEnvironment()), preConfig,
                        release.getSelectedLoggingPre(), deployType, Environment.PRE.name())).thenReturn(loggingType);
                releasesServiceImpl.updateReleaseConfig(ivUser, releaseConfig, releaseId, ReleaseConstants.ENVIRONMENT.PRE);

                //verify 2 times (one by updateSelectedPlatforms and another by updateReleaseManagementInPre)
                verify(this.validator, times(2)).checkReleaseExistence(releaseId);
                verify(this.validator, times(2)).checkProductExistence(product);
                verify(this.novaActivityEmitter, times(2)).emitNewActivity(any(Activity.class));

                Assertions.assertEquals(deployType, release.getSelectedDeployPre());
                Assertions.assertEquals(loggingType, release.getSelectedLoggingPre());
            }
        }
    }

    @Test
    void updateReleaseConfigSelectedPlatformsPRO()
    {
        //check if is PRO,
        Product product = new Product();
        product.setId(RandomUtils.nextInt(1, 1000));

        int releaseId = RandomUtils.nextInt(1, 1000);
        Release release = new Release();
        release.setId(releaseId);
        release.setSelectedLoggingPro(Platform.NOVAETHER);
        release.setSelectedDeployPro(Platform.NOVA);
        release.setProduct(product);

        product.setReleases(List.of(release));
        List<PlatformConfig> platformConfigList = new ArrayList<>();
        PlatformConfig platformConfigDeployNova = new PlatformConfig();
        platformConfigDeployNova.setPlatform(Platform.NOVA);
        platformConfigDeployNova.setIsDefault(true);
        platformConfigDeployNova.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigDeployNova.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployNova.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployNova);
        PlatformConfig platformConfigDeployEther = new PlatformConfig();
        platformConfigDeployEther.setPlatform(Platform.ETHER);
        platformConfigDeployEther.setIsDefault(false);
        platformConfigDeployEther.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigDeployEther.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployEther.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployEther);
        PlatformConfig platformConfigDeployAws = new PlatformConfig();
        platformConfigDeployAws.setPlatform(Platform.AWS);
        platformConfigDeployAws.setIsDefault(false);
        platformConfigDeployAws.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigDeployAws.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployAws.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployAws);

        PlatformConfig platformConfigLoggingNova = new PlatformConfig();
        platformConfigLoggingNova.setPlatform(Platform.NOVA);
        platformConfigLoggingNova.setIsDefault(true);
        platformConfigLoggingNova.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigLoggingNova.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingNova.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingNova);
        PlatformConfig platformConfigLoggingEther = new PlatformConfig();
        platformConfigLoggingEther.setPlatform(Platform.ETHER);
        platformConfigLoggingEther.setIsDefault(false);
        platformConfigLoggingEther.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigLoggingEther.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingEther.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingEther);
        PlatformConfig platformConfigLoggingAws = new PlatformConfig();
        platformConfigLoggingAws.setPlatform(Platform.AWS);
        platformConfigLoggingAws.setIsDefault(false);
        platformConfigLoggingAws.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigLoggingAws.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingAws.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingAws);

        product.setPlatformConfigList(platformConfigList);
        product.setDefaultAutomanageInPro(false);
        product.setDefaultAutodeployInPro(false);

        ReleaseEnvConfigDto proConfig = new ReleaseEnvConfigDto();
        proConfig.setSelectedPlatforms(new SelectedPlatformsDto());

        ManagementConfigDto managementConfigPro = new ManagementConfigDto();
        managementConfigPro.setDeploymentType(DeploymentType.NOVA_PLANNED.getType());
        proConfig.setManagementConfig(managementConfigPro);

        ReleaseConfigDto releaseConfig = new ReleaseConfigDto();
        releaseConfig.setProConfig(proConfig);

        for (Platform deployType : Arrays.stream(Platform.values()).filter(Platform::getIsValidToDeploy).collect(Collectors.toList()))
        {
            proConfig.getSelectedPlatforms().setDeploymentPlatform(deployType.name());
            for (Platform loggingType : Arrays.stream(Platform.values()).filter(Platform::getIsValidToLogging).collect(Collectors.toList()))
            {
                proConfig.getSelectedPlatforms().setLoggingPlatform(loggingType.name());
                Mockito.reset(validator);
                Mockito.reset(novaActivityEmitter);
                when(this.validator.checkReleaseExistence(releaseId)).thenReturn(release);
                when(validator.checkInputDeploymentPlatformValue(product.getDeployPlatformsAvailableByEnv(Environment.PRO.getEnvironment()), proConfig,
                        release.getSelectedDeployPro(), Environment.PRO.name())).thenReturn(deployType);
                when(validator.checkInputLoggingPlatformValue(product.getLoggingPlatformsAvailableByEnv(Environment.PRO.getEnvironment()), proConfig,
                        release.getSelectedLoggingPro(), deployType, Environment.PRO.name())).thenReturn(loggingType);
                releasesServiceImpl.updateReleaseConfig(ivUser, releaseConfig, releaseId, ReleaseConstants.ENVIRONMENT.PRO);

                //verify 2 times (one by updateSelectedPlatforms and another by updateReleaseManagement)
                verify(this.validator, times(2)).checkReleaseExistence(releaseId);
                verify(this.validator, times(2)).checkProductExistence(product);
                verify(this.novaActivityEmitter, times(2)).emitNewActivity(any(Activity.class));

                Assertions.assertEquals(deployType, release.getSelectedDeployPro());
                Assertions.assertEquals(loggingType, release.getSelectedLoggingPro());
            }
        }
    }

    // TODO@async: arreglar el test
//    @Test
    void updateReleaseConfigManagementConfigPRE() throws ParseException
    {
        //check if is PRE,
        Product product = new Product();
        product.setId(RandomUtils.nextInt(1, 1000));

        int releaseId = RandomUtils.nextInt(1, 1000);
        Release release = new Release();
        release.setId(releaseId);
        release.setSelectedLoggingPre(Platform.NOVAETHER);
        release.setSelectedDeployPre(Platform.NOVA);
        release.setProduct(product);

        product.setReleases(List.of(release));
        List<PlatformConfig> platformConfigList = new ArrayList<>();
        PlatformConfig platformConfigDeployNova = new PlatformConfig();
        platformConfigDeployNova.setPlatform(Platform.NOVA);
        platformConfigDeployNova.setIsDefault(true);
        platformConfigDeployNova.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigDeployNova.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployNova.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployNova);
        PlatformConfig platformConfigDeployEther = new PlatformConfig();
        platformConfigDeployEther.setPlatform(Platform.ETHER);
        platformConfigDeployEther.setIsDefault(false);
        platformConfigDeployEther.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigDeployEther.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployEther.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployEther);
        PlatformConfig platformConfigDeployAws = new PlatformConfig();
        platformConfigDeployAws.setPlatform(Platform.AWS);
        platformConfigDeployAws.setIsDefault(false);
        platformConfigDeployAws.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigDeployAws.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployAws.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployAws);

        PlatformConfig platformConfigLoggingNova = new PlatformConfig();
        platformConfigLoggingNova.setPlatform(Platform.NOVA);
        platformConfigLoggingNova.setIsDefault(true);
        platformConfigLoggingNova.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigLoggingNova.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingNova.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingNova);
        PlatformConfig platformConfigLoggingEther = new PlatformConfig();
        platformConfigLoggingEther.setPlatform(Platform.ETHER);
        platformConfigLoggingEther.setIsDefault(false);
        platformConfigLoggingEther.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigLoggingEther.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingEther.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingEther);
        PlatformConfig platformConfigLoggingAws = new PlatformConfig();
        platformConfigLoggingAws.setPlatform(Platform.AWS);
        platformConfigLoggingAws.setIsDefault(false);
        platformConfigLoggingAws.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigLoggingAws.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingAws.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingAws);

        product.setPlatformConfigList(platformConfigList);
        product.setDefaultAutomanageInPre(true);
        product.setDefaultAutodeployInPre(true);

        //Return this expected Release
        when(this.validator.checkReleaseExistence(releaseId)).thenReturn(release);

        ReleaseEnvConfigDto preConfig = new ReleaseEnvConfigDto();

        SelectedPlatformsDto selectedPlatformsDto = new SelectedPlatformsDto();
        selectedPlatformsDto.setDeploymentPlatform(Platform.NOVA.name());
        selectedPlatformsDto.setLoggingPlatform(Platform.NOVA.name());
        preConfig.setSelectedPlatforms(selectedPlatformsDto);

        ManagementConfigDto managementConfigDto = new ManagementConfigDto();
        preConfig.setManagementConfig(managementConfigDto);
        ReleaseConfigDto releaseConfig = new ReleaseConfigDto();
        releaseConfig.setPreConfig(preConfig);

        //First Try -> release.getAutomanageInPre() == null && release.getAutoDeployInPre() == null
        //          ==> empty TimeInterval (start is null and end is null)
        releasesServiceImpl.updateReleaseConfig(ivUser, releaseConfig, releaseId, ReleaseConstants.ENVIRONMENT.PRE);
        Assertions.assertNotNull(release.getAutomanageInPre());
        Assertions.assertNotNull(release.getAutodeployInPre());
        Assertions.assertNull(release.getAutomanageInPre().getStart());
        Assertions.assertNull(release.getAutomanageInPre().getEnd());
        Assertions.assertNull(release.getAutodeployInPre().getStart());
        Assertions.assertNull(release.getAutodeployInPre().getEnd());


        //Now, with automanage and autodeploy
        managementConfigDto.setAutoManage(createRandomConfigPeriodDto());
        managementConfigDto.setAutoDeploy(createRandomConfigPeriodDto());

        //Prepare TimeInterval
        DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        TimeInterval expectedAutoManagerTI = new TimeInterval();
        expectedAutoManagerTI.setStart(simpleDateFormat.parse(managementConfigDto.getAutoManage().getDateFrom()));
        expectedAutoManagerTI.setEnd(simpleDateFormat.parse(managementConfigDto.getAutoManage().getDateTo()));
        when(dtoBuilder.createEntityFromDto(managementConfigDto.getAutoManage())).thenReturn(expectedAutoManagerTI);

        TimeInterval expectedAutoDeployTI = new TimeInterval();
        expectedAutoDeployTI.setStart(simpleDateFormat.parse(managementConfigDto.getAutoDeploy().getDateFrom()));
        expectedAutoDeployTI.setEnd(simpleDateFormat.parse(managementConfigDto.getAutoDeploy().getDateTo()));
        when(dtoBuilder.createEntityFromDto(managementConfigDto.getAutoDeploy())).thenReturn(expectedAutoDeployTI);

        releasesServiceImpl.updateReleaseConfig(ivUser, releaseConfig, releaseId, ReleaseConstants.ENVIRONMENT.PRE);

        //verify
        Assertions.assertEquals(expectedAutoManagerTI, release.getAutomanageInPre());
        Assertions.assertEquals(expectedAutoDeployTI, release.getAutodeployInPre());
    }


    @Test
    void updateReleaseConfigManagementConfigPRO() throws ParseException
    {
        //check if is PRO,
        Product product = new Product();
        product.setId(RandomUtils.nextInt(1, 1000));

        int releaseId = RandomUtils.nextInt(1, 1000);
        Release release = new Release();
        release.setId(releaseId);
        release.setSelectedLoggingPro(Platform.NOVAETHER);
        release.setSelectedDeployPro(Platform.NOVA);
        release.setProduct(product);

        product.setReleases(List.of(release));
        List<PlatformConfig> platformConfigList = new ArrayList<>();
        PlatformConfig platformConfigDeployNova = new PlatformConfig();
        platformConfigDeployNova.setPlatform(Platform.NOVA);
        platformConfigDeployNova.setIsDefault(true);
        platformConfigDeployNova.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigDeployNova.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployNova.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployNova);
        PlatformConfig platformConfigDeployEther = new PlatformConfig();
        platformConfigDeployEther.setPlatform(Platform.ETHER);
        platformConfigDeployEther.setIsDefault(false);
        platformConfigDeployEther.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigDeployEther.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployEther.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployEther);
        PlatformConfig platformConfigDeployAws = new PlatformConfig();
        platformConfigDeployAws.setPlatform(Platform.AWS);
        platformConfigDeployAws.setIsDefault(false);
        platformConfigDeployAws.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigDeployAws.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployAws.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployAws);

        PlatformConfig platformConfigLoggingNova = new PlatformConfig();
        platformConfigLoggingNova.setPlatform(Platform.NOVA);
        platformConfigLoggingNova.setIsDefault(true);
        platformConfigLoggingNova.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigLoggingNova.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingNova.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingNova);
        PlatformConfig platformConfigLoggingEther = new PlatformConfig();
        platformConfigLoggingEther.setPlatform(Platform.ETHER);
        platformConfigLoggingEther.setIsDefault(false);
        platformConfigLoggingEther.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigLoggingEther.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingEther.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingEther);
        PlatformConfig platformConfigLoggingAws = new PlatformConfig();
        platformConfigLoggingAws.setPlatform(Platform.AWS);
        platformConfigLoggingAws.setIsDefault(false);
        platformConfigLoggingAws.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigLoggingAws.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingAws.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingAws);

        product.setPlatformConfigList(platformConfigList);
        product.setDefaultAutomanageInPro(true);
        product.setDefaultAutodeployInPro(true);

        //Return this expected Release
        when(this.validator.checkReleaseExistence(releaseId)).thenReturn(release);

        ReleaseEnvConfigDto proConfig = new ReleaseEnvConfigDto();

        SelectedPlatformsDto selectedPlatformsDto = new SelectedPlatformsDto();
        selectedPlatformsDto.setDeploymentPlatform(Platform.NOVA.name());
        selectedPlatformsDto.setLoggingPlatform(Platform.NOVA.name());
        proConfig.setSelectedPlatforms(selectedPlatformsDto);

        ManagementConfigDto managementConfigDto = new ManagementConfigDto();
        managementConfigDto.setDeploymentType(DeploymentType.ON_DEMAND.getType());
        proConfig.setManagementConfig(managementConfigDto);
        ReleaseConfigDto releaseConfig = new ReleaseConfigDto();
        releaseConfig.setProConfig(proConfig);

        //First Try -> release.getAutomanageInPro() == null && release.getAutoDeployInPro() == null
        //          ==> empty TimeInterval (start is null and end is null)
        releasesServiceImpl.updateReleaseConfig(ivUser, releaseConfig, releaseId, ReleaseConstants.ENVIRONMENT.PRO);
        Assertions.assertNotNull(release.getAutoManageInPro());
        Assertions.assertNotNull(release.getAutodeployInPro());
        Assertions.assertNull(release.getAutoManageInPro().getStart());
        Assertions.assertNull(release.getAutoManageInPro().getEnd());
        Assertions.assertNull(release.getAutodeployInPro().getStart());
        Assertions.assertNull(release.getAutodeployInPro().getEnd());


        //Now, with automanage and autodeploy
        managementConfigDto.setAutoManage(createRandomConfigPeriodDto());
        managementConfigDto.setAutoDeploy(createRandomConfigPeriodDto());

        //Prepare TimeInterval
        DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        TimeInterval expectedAutoManagerTI = new TimeInterval();
        expectedAutoManagerTI.setStart(simpleDateFormat.parse(managementConfigDto.getAutoManage().getDateFrom()));
        expectedAutoManagerTI.setEnd(simpleDateFormat.parse(managementConfigDto.getAutoManage().getDateTo()));
        when(dtoBuilder.createEntityFromDto(managementConfigDto.getAutoManage())).thenReturn(expectedAutoManagerTI);

        TimeInterval expectedAutoDeployTI = new TimeInterval();
        expectedAutoDeployTI.setStart(simpleDateFormat.parse(managementConfigDto.getAutoDeploy().getDateFrom()));
        expectedAutoDeployTI.setEnd(simpleDateFormat.parse(managementConfigDto.getAutoDeploy().getDateTo()));
        when(dtoBuilder.createEntityFromDto(managementConfigDto.getAutoDeploy())).thenReturn(expectedAutoDeployTI);

        releasesServiceImpl.updateReleaseConfig(ivUser, releaseConfig, releaseId, ReleaseConstants.ENVIRONMENT.PRO);

        //verify
        Assertions.assertEquals(expectedAutoManagerTI, release.getAutoManageInPro());
        Assertions.assertEquals(expectedAutoDeployTI, release.getAutodeployInPro());
    }


    @Test
    void deleteReleaseNoFound() throws Exception
    {
        int releaseId = -1;
        //simulate release is not found by exception
        Mockito.doThrow(NovaException.class).when(this.validator).checkReleaseExistence(releaseId);
        // Elevate exception
        Assertions.assertThrows(NovaException.class, () -> releasesServiceImpl.deleteRelease(ivUser, releaseId));
        verify(usersService, Mockito.never()).checkHasPermission(eq(ivUser), eq(ReleaseConstants.DELETE_RELEASE_PERMISSION), anyInt(), any(Exception.class));
    }

    @Test
    void deleteReleaseNoEmpty() throws Exception
    {
        //If you try to remove a non empty release ---> exception
        int releaseId = RandomUtils.nextInt(0, 1000);
        int productId = RandomUtils.nextInt(0, 1000);
        Product product = new Product();
        product.setId(productId);
        Release release = new Release();
        release.setProduct(product);
        release.setId(releaseId);
        product.setReleases(List.of(release));
        ReleaseVersion rv = new ReleaseVersion();
        release.setReleaseVersions(List.of(rv));

        //Return this expected Release
        when(this.validator.checkReleaseExistence(releaseId)).thenReturn(release);

        Assertions.assertThrows(NovaException.class, () -> releasesServiceImpl.deleteRelease(ivUser, releaseId));

        verify(this.validator, times(1)).checkReleaseExistence(releaseId);
        verify(this.validator, times(1)).checkProductExistence(product);
        verify(usersService, times(1)).checkHasPermission(eq(ivUser), eq(ReleaseConstants.DELETE_RELEASE_PERMISSION), anyInt(), any(Exception.class));

        //check NovaException values
        try
        {
            releasesServiceImpl.deleteRelease(ivUser, releaseId);
        }
        catch (Exception e)
        {
            Assertions.assertTrue(e instanceof NovaException);
            NovaError novaError = ((NovaException) e).getNovaError();
            Assertions.assertNotNull(novaError);
            Assertions.assertTrue(e.getMessage().contains(String.valueOf(releaseId)));
            Assertions.assertEquals(ReleaseConstants.ReleaseErrors.CODE_REMOVE_RELEASE_WITH_VERSION, novaError.getErrorCode());
            Assertions.assertNotNull(novaError.getErrorMessage());
            Assertions.assertNotNull(novaError.getActionMessage());
            Assertions.assertEquals(ErrorMessageType.ERROR, novaError.getErrorMessageType());
            Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, novaError.getHttpStatus());
        }
    }

    @Test
    void deleteRelease() throws Exception
    {
        //If you try to remove a non empty release ---> exception
        int releaseId = RandomUtils.nextInt(0, 1000);
        int productId = RandomUtils.nextInt(0, 1000);
        Product product = new Product();
        product.setId(productId);
        Release release = new Release();
        release.setProduct(product);
        release.setId(releaseId);
        List<Release> releases = new ArrayList<>();
        releases.add(release);
        product.setReleases(releases);

        //Return this expected Release
        when(this.validator.checkReleaseExistence(releaseId)).thenReturn(release);

        releasesServiceImpl.deleteRelease(ivUser, releaseId);

        verify(this.validator, times(1)).checkReleaseExistence(releaseId);
        verify(this.validator, times(1)).checkProductExistence(product);
        verify(usersService, times(1)).checkHasPermission(eq(ivUser), eq(ReleaseConstants.DELETE_RELEASE_PERMISSION), anyInt(), any(Exception.class));
        Assertions.assertTrue(product.getReleases().isEmpty());
        verify(this.novaActivityEmitter, times(1)).emitNewActivity(any(Activity.class));

    }

    @Test
    void getReleaseVersionsNoRV()
    {
        //If you try to remove a non empty release ---> exception
        String status = "";
        int releaseId = RandomUtils.nextInt(0, 1000);
        Release release = new Release();
        release.setId(releaseId);
        release.setReleaseVersions(Collections.emptyList());

        //Return this expected Release
        when(this.validator.checkReleaseExistence(releaseId)).thenReturn(release);

        final ArgumentCaptor<List<ReleaseVersion>> rvListCaptor
                = ArgumentCaptor.forClass((Class) List.class);

        ReleaseVersionInListDto[] garbage = releasesServiceImpl.getReleaseVersions(releaseId, status);
        Mockito.verify(dtoBuilder).buildDtoFromReleaseVersionList(rvListCaptor.capture());
        List<ReleaseVersion> capturedList = rvListCaptor.getValue();
        Assertions.assertNotNull(capturedList);
        Assertions.assertTrue(capturedList.isEmpty());
    }

    @Test
    void getReleaseVersionsNoStatus()
    {
        String status = "";
        int releaseId = RandomUtils.nextInt(0, 1000);
        Release release = new Release();
        release.setId(releaseId);
        List<ReleaseVersion> releaseVersionList = new ArrayList<>();
        release.setReleaseVersions(releaseVersionList);

        final ArgumentCaptor<List<ReleaseVersion>> rvListCaptor
                = ArgumentCaptor.forClass((Class) List.class);

        //Return this expected Release
        when(this.validator.checkReleaseExistence(releaseId)).thenReturn(release);

        // status as null & NO RVs
        ReleaseVersionInListDto[] garbage = releasesServiceImpl.getReleaseVersions(releaseId, null);
        Mockito.verify(dtoBuilder).buildDtoFromReleaseVersionList(rvListCaptor.capture());
        List<ReleaseVersion> capturedList = rvListCaptor.getValue();
        Assertions.assertNotNull(capturedList);
        Assertions.assertTrue(capturedList.isEmpty());

        // status as empty & NO RVs
        Mockito.reset(dtoBuilder);
        garbage = releasesServiceImpl.getReleaseVersions(releaseId, "");
        Mockito.verify(dtoBuilder).buildDtoFromReleaseVersionList(rvListCaptor.capture());
        capturedList = rvListCaptor.getValue();
        Assertions.assertNotNull(capturedList);
        Assertions.assertTrue(capturedList.isEmpty());

        // A release version for each status
        ReleaseVersionInListDto rvdto;
        ReleaseVersion rv;
        int id = 0;
        for (ReleaseVersionStatus rvstatus : ReleaseVersionStatus.values())
        {
            rv = new ReleaseVersion();
            rv.setId(++id);
            rv.setVersionName("v1.0." + rv.getId());
            rv.setStatus(rvstatus);
            releaseVersionList.add(rv);

            //status null  --> complete list
            Mockito.reset(dtoBuilder);
            garbage = releasesServiceImpl.getReleaseVersions(releaseId, null);
            Mockito.verify(dtoBuilder).buildDtoFromReleaseVersionList(rvListCaptor.capture());
            capturedList = rvListCaptor.getValue();
            Assertions.assertNotNull(capturedList);
            Assertions.assertEquals(releaseVersionList.size(), capturedList.size());
            Assertions.assertEquals(releaseVersionList, capturedList);
            //List is sorted by vName, get last because it has to be last
            Assertions.assertEquals(rvstatus, capturedList.get(capturedList.size() - 1).getStatus());

            //status empty --> complete list
            Mockito.reset(dtoBuilder);
            garbage = releasesServiceImpl.getReleaseVersions(releaseId, "");
            Mockito.verify(dtoBuilder).buildDtoFromReleaseVersionList(rvListCaptor.capture());
            capturedList = rvListCaptor.getValue();
            Assertions.assertNotNull(capturedList);
            Assertions.assertEquals(releaseVersionList.size(), capturedList.size());
            Assertions.assertEquals(releaseVersionList, capturedList);
            //List is sorted by vName, get last because it has to be last
            Assertions.assertEquals(rvstatus, capturedList.get(capturedList.size() - 1).getStatus());
        }
    }

    @Test
    void getReleaseVersionsByStatus()
    {
        //If you try to remove a non empty release ---> exception
        int releaseId = RandomUtils.nextInt(0, 1000);
        Release release = new Release();
        release.setId(releaseId);
        List<ReleaseVersion> releaseVersionList = new ArrayList<>();
        release.setReleaseVersions(releaseVersionList);

        //Return this expected Release
        when(this.validator.checkReleaseExistence(releaseId)).thenReturn(release);
        final ArgumentCaptor<List<ReleaseVersion>> rvListCaptor
                = ArgumentCaptor.forClass((Class) List.class);

        //Return this expected Release
        when(this.validator.checkReleaseExistence(releaseId)).thenReturn(release);

        // A release version for each status
        List<ReleaseVersion> capturedList;
        ReleaseVersionInListDto[] garbage;
        ReleaseVersion rv;
        int id = 0;
        for (ReleaseVersionStatus rvstatus : ReleaseVersionStatus.values())
        {
            rv = new ReleaseVersion();
            rv.setId(++id);
            rv.setVersionName("v1.0." + rv.getId());
            rv.setStatus(rvstatus);
            releaseVersionList.add(rv);

            //filtered by status
            Mockito.reset(dtoBuilder);
            garbage = releasesServiceImpl.getReleaseVersions(releaseId, rvstatus.name());
            Mockito.verify(dtoBuilder).buildDtoFromReleaseVersionList(rvListCaptor.capture());
            capturedList = rvListCaptor.getValue();
            Assertions.assertNotNull(capturedList);

            //as we are creating one RV by each RVStatus, the result filtered by status has to be 1!
            Assertions.assertEquals(1, capturedList.size());
            //List has one unique element, and it has to be our current rv
            Assertions.assertEquals(rv, capturedList.get(0));
        }
    }

    @Test
    public void getReleasesMaxVersions()
    {
        Product product = new Product();
        product.setId(1);

        //When
        when(this.productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        //verify
        product.setReleaseSlots(1);
        Assertions.assertEquals(releasesServiceImpl.getReleasesMaxVersions(product.getId()), 6);
        //verify
        product.setReleaseSlots(2);
        Assertions.assertEquals(releasesServiceImpl.getReleasesMaxVersions(product.getId()), 6);
        //verify
        product.setReleaseSlots(3);
        Assertions.assertEquals(releasesServiceImpl.getReleasesMaxVersions(product.getId()), 8);
        //verify
        product.setReleaseSlots(4);
        Assertions.assertEquals(releasesServiceImpl.getReleasesMaxVersions(product.getId()), 10);
        //verify
        product.setReleaseSlots(5);
        Assertions.assertNotEquals(releasesServiceImpl.getReleasesMaxVersions(product.getId()), 12);
    }

    /**
     * Make assertions to last release of the product (by create release test)
     */
    private void assertionsForLastReleaseByProduct(Product product, NewReleaseRequest releaseRequest, int expectedSize)
    {
        Assertions.assertFalse(product.getReleases().isEmpty());
        Assertions.assertEquals(expectedSize, product.getReleases().size());
        Release current = product.getReleases().get(expectedSize - 1);

        Assertions.assertEquals(product, current.getProduct(), "The product of release is not equals");
        Assertions.assertEquals(releaseRequest.getReleaseName(), current.getName(), "The name of release is not equals");
        Assertions.assertEquals(releaseRequest.getDescription(), current.getDescription(), "description of release is not equals");

        //Assert info from Product
        Assertions.assertEquals(product, current.getProduct(), "Product of release is not equals");
        //Int
        Assertions.assertEquals(product.getDefaultPlatformByConfigTypeAndEnv(ConfigurationType.DEPLOY, Environment.INT.getEnvironment()), current.getSelectedDeployInt());
        Assertions.assertEquals(product.getDefaultPlatformByConfigTypeAndEnv(ConfigurationType.LOGGING, Environment.INT.getEnvironment()), current.getSelectedLoggingInt());
        //PRE
        Assertions.assertEquals(product.getDefaultPlatformByConfigTypeAndEnv(ConfigurationType.DEPLOY, Environment.PRE.getEnvironment()), current.getSelectedDeployPre());
        Assertions.assertEquals(product.getDefaultPlatformByConfigTypeAndEnv(ConfigurationType.LOGGING, Environment.PRE.getEnvironment()), current.getSelectedLoggingPre());

        if (product.getDefaultAutodeployInPre())
        {
            Assertions.assertNotNull(current.getAutodeployInPre());
            Assertions.assertNotNull(current.getAutodeployInPre().getStart());
        }
        if (product.getDefaultAutomanageInPre())
        {
            Assertions.assertNotNull(current.getAutomanageInPre());
            Assertions.assertNotNull(current.getAutomanageInPre().getStart());
        }
        //PRO
        Assertions.assertEquals(product.getDefaultPlatformByConfigTypeAndEnv(ConfigurationType.DEPLOY, Environment.PRO.getEnvironment()), current.getSelectedDeployPro());
        Assertions.assertEquals(product.getDefaultPlatformByConfigTypeAndEnv(ConfigurationType.LOGGING, Environment.PRO.getEnvironment()), current.getSelectedLoggingPro());
        Assertions.assertEquals(product.getDefaultDeploymentTypeInPro(), current.getDeploymentTypeInPro());
        if (product.getDefaultAutodeployInPro())
        {
            Assertions.assertNotNull(current.getAutodeployInPro());
            Assertions.assertNotNull(current.getAutodeployInPro().getStart());
        }
        if (product.getDefaultAutomanageInPro())
        {
            Assertions.assertNotNull(current.getAutoManageInPro());
            Assertions.assertNotNull(current.getAutoManageInPro().getStart());
        }
    }

    /**
     * Create a Product with randoms values and empty collections
     *
     * @param productId id of the product
     * @return a complete initialized product
     */
    private Product createEmptyRandomProduct(int productId)
    {

        String key = RandomStringUtils.randomAlphabetic(10);
        Product product = new Product();
        product.setId(productId);
        product.setName("product" + key);
        product.setUuaa(RandomStringUtils.randomAlphabetic(4));
        product.setDescription("description: " + key);
        product.setCPDInPro(getRandomCPD(RandomUtils.nextInt(0, 100)));
        product.setCriticalityLevel(RandomUtils.nextInt(0, 5));
        product.setQualityLevel(RandomUtils.nextInt(0, 5));
        product.setDesBoard("desboard-" + key);
        product.setDevelopment(RandomUtils.nextInt(0, 1));
        product.setType("NOVA");
        product.setRemedySupportGroup("remedy-" + key);
        product.setPhone(RandomStringUtils.randomNumeric(5));
        product.setImage("logo");
        product.setProductStatus(ProductStatus.READY);
        product.setDefaultAutodeployInPre(RandomUtils.nextBoolean());
        product.setDefaultAutodeployInPro(RandomUtils.nextBoolean());
        product.setDefaultAutomanageInPre(RandomUtils.nextBoolean());
        product.setDefaultAutomanageInPro(RandomUtils.nextBoolean());

        product.setDefaultDeploymentTypeInPro(DeploymentType.values()[RandomUtils.nextInt(0, DeploymentType.values().length)]);

        List<Platform> platformsListToDeploy = Arrays.stream(Platform.values()).filter(Platform::getIsValidToDeploy).collect(Collectors.toList());
        Platform dpdt = platformsListToDeploy.get(RandomUtils.nextInt(0, platformsListToDeploy.size()));
        List<PlatformConfig> platformConfigList = new ArrayList<>();
        PlatformConfig platformConfigDeployInt = new PlatformConfig();
        platformConfigDeployInt.setPlatform(dpdt);
        platformConfigDeployInt.setIsDefault(true);
        platformConfigDeployInt.setEnvironment(Environment.INT.getEnvironment());
        platformConfigDeployInt.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployInt.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployInt);
        PlatformConfig platformConfigDeployPre = new PlatformConfig();
        platformConfigDeployPre.setPlatform(dpdt);
        platformConfigDeployPre.setIsDefault(true);
        platformConfigDeployPre.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigDeployPre.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployPre.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployPre);
        PlatformConfig platformConfigDeployPro = new PlatformConfig();
        platformConfigDeployPro.setPlatform(dpdt);
        platformConfigDeployPro.setIsDefault(true);
        platformConfigDeployPro.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigDeployPro.setConfigurationType(ConfigurationType.DEPLOY);
        platformConfigDeployPro.setProductId(product.getId());
        platformConfigList.add(platformConfigDeployPro);


        List<Platform> platformsListToLogging = Arrays.stream(Platform.values()).filter(Platform::getIsValidToLogging).collect(Collectors.toList());
        Platform dplt = platformsListToLogging.get(RandomUtils.nextInt(0, platformsListToLogging.size()));
        PlatformConfig platformConfigLoggingInt = new PlatformConfig();
        platformConfigLoggingInt.setPlatform(dplt);
        platformConfigLoggingInt.setIsDefault(true);
        platformConfigLoggingInt.setEnvironment(Environment.INT.getEnvironment());
        platformConfigLoggingInt.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingInt.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingInt);
        PlatformConfig platformConfigLoggingPre = new PlatformConfig();
        platformConfigLoggingPre.setPlatform(dplt);
        platformConfigLoggingPre.setIsDefault(true);
        platformConfigLoggingPre.setEnvironment(Environment.PRE.getEnvironment());
        platformConfigLoggingPre.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingPre.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingPre);
        PlatformConfig platformConfigLoggingPro = new PlatformConfig();
        platformConfigLoggingPro.setPlatform(dplt);
        platformConfigLoggingPro.setIsDefault(true);
        platformConfigLoggingPro.setEnvironment(Environment.PRO.getEnvironment());
        platformConfigLoggingPro.setConfigurationType(ConfigurationType.LOGGING);
        platformConfigLoggingPro.setProductId(product.getId());
        platformConfigList.add(platformConfigLoggingPro);

        product.setPlatformConfigList(platformConfigList);
//        AvailablePlatformDeployType apdt = AvailablePlatformDeployType.values()[RandomUtils.nextInt(0, DestinationPlatformLoggingType.values().length)];
//        product.setEnabledDeployInt(apdt);
//        product.setEnabledDeployPre(apdt);
//        product.setEnabledDeployPro(apdt);

        product.setReleases(new ArrayList<>());
        product.setCategories(new ArrayList<>());
        product.setDocSystems(new ArrayList<>());
        product.setLogicalConnectors(new ArrayList<>());
        product.setFilesystems(new ArrayList<>());

        return product;
    }

    /**
     * Create a Random CPD
     *
     * @param id id to set
     * @return cpd
     */
    private CPD getRandomCPD(final int id)
    {
        CPD cpd = new CPD();

        cpd.setId(id);
        cpd.setName(RandomStringUtils.randomAlphabetic(10));
        cpd.setActive(RandomUtils.nextBoolean());
        cpd.setAddress(RandomStringUtils.randomAlphabetic(10));
        cpd.setEnvironment(Environment.PRO.getEnvironment());
        cpd.setElasticSearchCPDName("ES-" + cpd.getName());
        cpd.setFilesystem("FS-" + cpd.getName());
        cpd.setLabel("label-" + cpd.getName());
        cpd.setRegistry("registry-" + cpd.getName());

        return cpd;
    }

    private ConfigPeriodDto createRandomConfigPeriodDto()
    {
        ConfigPeriodDto periodDto = new ConfigPeriodDto();

        Instant from = Instant.ofEpochMilli(System.currentTimeMillis() - RandomUtils.nextInt(1000, 100000));
        Instant to = Instant.ofEpochMilli(System.currentTimeMillis() + RandomUtils.nextInt(1000, 100000));

        periodDto.setDateFrom(DateTimeFormatter.ISO_INSTANT.format(from));
        periodDto.setDateTo(DateTimeFormatter.ISO_INSTANT.format(to));

        return periodDto;
    }
}
