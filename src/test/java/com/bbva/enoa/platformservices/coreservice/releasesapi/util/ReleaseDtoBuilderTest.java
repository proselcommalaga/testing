package com.bbva.enoa.platformservices.coreservice.releasesapi.util;

import com.bbva.enoa.apirestgen.releasesapi.model.*;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.common.enumerates.AsyncStatus;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.*;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


public class ReleaseDtoBuilderTest
{
    @InjectMocks
    private ReleaseDtoBuilder releaseDtoBuilder;

    @Mock
    private VersionPlanDtoBuilder versionPlanDtoBuilder;

    @Mock
    private ProductRepository productRepository;

    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void buildReleaseInfoListNoProduct()
    {
        Assertions.assertThrows(NovaException.class,
                () -> this.releaseDtoBuilder.buildReleaseInfoList(0));
    }

    @Test
    void buildReleaseInfoListNoReleases()
    {
        //Given
        Product product = new Product();
        product.setId(1);
        product.setReleases(Collections.emptyList());

        //when
        Mockito.when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        //then
        Assertions.assertNotNull(this.releaseDtoBuilder.buildReleaseInfoList(product.getId()));
    }

    @Test
    void buildReleaseInfoListOneReleaseNoRV()
    {
        //Given
        Product product = new Product();
        product.setId(RandomUtils.nextInt(0, 1000));

        Release expectedRelease = this.generateRandomRelease(product);
        product.setReleases(List.of(expectedRelease));

        //when
        Mockito.when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        //then
        RELReleaseInfo[] releaseInfoArray = this.releaseDtoBuilder.buildReleaseInfoList(product.getId());

        Assertions.assertNotNull(releaseInfoArray);
        Assertions.assertEquals(1, releaseInfoArray.length);

        this.assertionsForReleaseInfo(expectedRelease, releaseInfoArray[0]);
    }


    @Test
    void buildReleaseInfoListSeveralReleaseNoRV()
    {
        //Given
        Product product = new Product();
        product.setId(RandomUtils.nextInt(0, 1000));
        int size = 6;
        List<Release> releases = new ArrayList<>(size);
        for (int i = 0; i < size; i++)
        {
            releases.add(generateRandomRelease(product));
        }
        product.setReleases(releases);


        //when
        Mockito.when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        //then
        RELReleaseInfo[] releaseInfoArray = this.releaseDtoBuilder.buildReleaseInfoList(product.getId());

        Assertions.assertNotNull(releaseInfoArray);
        Assertions.assertEquals(releases.size(), releaseInfoArray.length);

        Release expected;
        RELReleaseInfo current;
        for (int i = 0; i < size; i++)
        {
            expected = releases.get(i);
            current = releaseInfoArray[i];

            this.assertionsForReleaseInfo(expected, current);
        }
    }

    @Test
    void buildReleaseInfoListOneReleaseOneRVNoServices()
    {

        //Given
        Product product = new Product();
        product.setId(RandomUtils.nextInt(0, 1000));

        Release expectedRelease = generateRandomRelease(product);
        product.setReleases(List.of(expectedRelease));

        ReleaseVersion expectedRV = generateRandomReleaseVersion(expectedRelease);
        expectedRelease.getReleaseVersions().add(expectedRV);

        //when
        Mockito.when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        //then
        RELReleaseInfo[] releaseInfoArray = this.releaseDtoBuilder.buildReleaseInfoList(product.getId());

        Assertions.assertNotNull(releaseInfoArray);
        Assertions.assertEquals(1, releaseInfoArray.length);
        RELReleaseInfo current = releaseInfoArray[0];

        this.assertionsForReleaseInfo(expectedRelease, current);

        for (RELReleaseVersionInfo rvinfo : current.getReleaseVersions())
        {
            this.assertionsForReleaseVersionInfo(expectedRV, rvinfo);
        }
    }


    /**
     * buildReleaseInfoList only has to return RV if ReleaseVersionStatus is READY_TO_DEPLOY.
     * In other case, it has no return RV
     */
    @Test
    void buildReleaseInfoListOneReleaseNoRVByNoReadyToDeploy()
    {
        //Given
        Product product = new Product();
        product.setId(RandomUtils.nextInt(0, 1000));

        Release expectedRelease = generateRandomRelease(product);
        product.setReleases(List.of(expectedRelease));

        ReleaseVersion expectedRV = generateRandomReleaseVersion(expectedRelease);
        expectedRelease.getReleaseVersions().add(expectedRV);

        //when
        Mockito.when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        for (ReleaseVersionStatus status : ReleaseVersionStatus.values())
        {
            expectedRV.setStatus(status);
            //then
            RELReleaseInfo[] releaseInfoArray = this.releaseDtoBuilder.buildReleaseInfoList(product.getId());

            Assertions.assertNotNull(releaseInfoArray);
            Assertions.assertEquals(1, releaseInfoArray.length);
            RELReleaseInfo current = releaseInfoArray[0];

            this.assertionsForReleaseInfo(expectedRelease, current);

            if (status == ReleaseVersionStatus.READY_TO_DEPLOY)
            {
                Assertions.assertEquals(expectedRelease.getReleaseVersions().size(), current.getReleaseVersions().length);

                ReleaseVersion rvExpected;
                RELReleaseVersionInfo rvInfo;
                for (int i = 0; i < current.getReleaseVersions().length; i++)
                {
                    rvExpected = expectedRelease.getReleaseVersions().get(i);
                    rvInfo = current.getReleaseVersions()[i];
                    this.assertionsForReleaseVersionInfo(rvExpected, rvInfo);
                }
            }
            else
            {
                Assertions.assertEquals(0, current.getReleaseVersions().length, "ReleaseVersion has status different to Ready_to_deploy. This RV has to be ignored");
            }
        }

    }

    @Test
    void buildReleaseInfoListOneReleaseOneRVOneService()
    {

        //Given
        Product product = new Product();
        product.setId(RandomUtils.nextInt(0, 1000));

        Release expectedRelease = generateRandomRelease(product);
        product.setReleases(List.of(expectedRelease));

        ReleaseVersion expectedRV = generateRandomReleaseVersion(expectedRelease);
        expectedRelease.getReleaseVersions().add(expectedRV);

        ReleaseVersionSubsystem subsystem = generateRandomReleaseVersionSubsystem(expectedRV);
        expectedRV.getSubsystems().add(subsystem);
        ReleaseVersionService service = generateRandomReleaseVersionServiceBy(subsystem);
        subsystem.getServices().add(service);

        //when
        Mockito.when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        //then
        RELReleaseInfo[] releaseInfoArray = this.releaseDtoBuilder.buildReleaseInfoList(product.getId());

        Assertions.assertNotNull(releaseInfoArray);
        Assertions.assertEquals(1, releaseInfoArray.length);
        RELReleaseInfo current = releaseInfoArray[0];

        this.assertionsForReleaseInfo(expectedRelease, current);

        for (RELReleaseVersionInfo rvinfo : current.getReleaseVersions())
        {
            this.assertionsForReleaseVersionInfo(expectedRV, rvinfo);
            for (RELServiceInfo serviceInfo : rvinfo.getServices())
            {
                this.assertionsForServiceInfo(service, serviceInfo);
            }
        }
    }

    @Test
    void buildReleaseInfoListOneReleaseSeveralRV()
    {

        Product product = new Product();
        product.setId(RandomUtils.nextInt(0, 1000));

        Release expectedRelease = generateRandomRelease(product);
        product.setReleases(List.of(expectedRelease));

        int numRV = RandomUtils.nextInt(3, 6);
        int numRVS;
        ReleaseVersion releaseVersion;
        ReleaseVersionSubsystem subsystem;
        ReleaseVersionService service;
        for (int i = 0; i < numRV; i++)
        {
            releaseVersion = generateRandomReleaseVersion(expectedRelease);
            expectedRelease.getReleaseVersions().add(releaseVersion);

            subsystem = generateRandomReleaseVersionSubsystem(releaseVersion);
            releaseVersion.getSubsystems().add(subsystem);

            numRVS = RandomUtils.nextInt(1, 5);
            for (int j = 0; j < numRVS; j++)
            {
                service = generateRandomReleaseVersionServiceBy(subsystem);
                service.setServiceName(service.getServiceName() + "-" + j);
                subsystem.getServices().add(service);
            }
        }

        //when
        Mockito.when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        //then
        RELReleaseInfo[] releaseInfoArray = this.releaseDtoBuilder.buildReleaseInfoList(product.getId());

        Assertions.assertNotNull(releaseInfoArray);
        Assertions.assertEquals(1, releaseInfoArray.length);
        RELReleaseInfo current = releaseInfoArray[0];

        this.assertionsForReleaseInfo(expectedRelease, current);

        RELReleaseVersionInfo rvinfo;
        ReleaseVersion expectedRV;

        for (int i = 0; i < current.getReleaseVersions().length; i++)
        {
            expectedRV = expectedRelease.getReleaseVersions().get(i);
            rvinfo = current.getReleaseVersions()[i];

            this.assertionsForReleaseVersionInfo(expectedRV, rvinfo);
            this.assertionsForListOfServicesAndServicesInfo(expectedRV, rvinfo);
        }
    }

    @Test
    void buildReleaseInfoListSeveralReleaseSeveralRV()
    {

        Product product = new Product();
        product.setId(RandomUtils.nextInt(0, 1000));
        List<Release> releasesList = new ArrayList<>();
        product.setReleases(releasesList);


        int releaseNumber = RandomUtils.nextInt(3, 6);
        int numRV;
        int numRVS;
        Release release;
        ReleaseVersion releaseVersion;
        ReleaseVersionSubsystem subsystem;
        ReleaseVersionService service;

        for (int r = 0; r < releaseNumber; r++)
        {
            release = generateRandomRelease(product);
            releasesList.add(release);

            numRV = RandomUtils.nextInt(3, 6);
            for (int i = 0; i < numRV; i++)
            {
                releaseVersion = generateRandomReleaseVersion(release);
                release.getReleaseVersions().add(releaseVersion);

                subsystem = generateRandomReleaseVersionSubsystem(releaseVersion);
                releaseVersion.getSubsystems().add(subsystem);

                numRVS = RandomUtils.nextInt(1, 5);
                for (int j = 0; j < numRVS; j++)
                {
                    service = generateRandomReleaseVersionServiceBy(subsystem);
                    service.setServiceName(service.getServiceName() + "-" + j);
                    subsystem.getServices().add(service);
                }
            }
        }

        //when
        Mockito.when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        //then
        RELReleaseInfo[] releaseInfoArray = this.releaseDtoBuilder.buildReleaseInfoList(product.getId());

        Assertions.assertNotNull(releaseInfoArray);
        Assertions.assertEquals(releasesList.size(), releaseInfoArray.length);


        Release expectedRelease;
        RELReleaseInfo currentReleaseInfo;
        for (int r = 0; r < releaseInfoArray.length; r++)
        {
            expectedRelease = releasesList.get(r);
            currentReleaseInfo = releaseInfoArray[r];
            this.assertionsForReleaseInfo(expectedRelease, currentReleaseInfo);

            RELReleaseVersionInfo rvinfo;
            ReleaseVersion expectedRV;

            for (int i = 0; i < currentReleaseInfo.getReleaseVersions().length; i++)
            {
                expectedRV = expectedRelease.getReleaseVersions().get(i);
                rvinfo = currentReleaseInfo.getReleaseVersions()[i];

                this.assertionsForReleaseVersionInfo(expectedRV, rvinfo);
                this.assertionsForListOfServicesAndServicesInfo(expectedRV, rvinfo);
            }
        }
    }

    @Test
    void createEntityFromDtoBadFormatFrom()
    {
        ConfigPeriodDto periodDto = new ConfigPeriodDto();

        Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
        periodDto.setDateFrom("20200801-12:00:00");
        periodDto.setDateTo(DateTimeFormatter.ISO_INSTANT.format(instant));

        Assertions.assertThrows(NovaException.class,
                () -> releaseDtoBuilder.createEntityFromDto(periodDto));
    }

    @Test
    void createEntityFromDtoBadFormatTo()
    {
        ConfigPeriodDto periodDto = new ConfigPeriodDto();

        Instant instant = Instant.ofEpochMilli(System.currentTimeMillis());
        periodDto.setDateTo("20200801-12:00:00");
        periodDto.setDateFrom(DateTimeFormatter.ISO_INSTANT.format(instant));

        Assertions.assertThrows(NovaException.class,
                () -> releaseDtoBuilder.createEntityFromDto(periodDto));
    }

    @Test
    void createEntityFromDto()
    {
        ConfigPeriodDto periodDto = new ConfigPeriodDto();

        Instant from = Instant.ofEpochMilli(System.currentTimeMillis() - RandomUtils.nextInt(1000, 100000));
        Instant to = Instant.ofEpochMilli(System.currentTimeMillis() + RandomUtils.nextInt(1000, 100000));

        periodDto.setDateFrom(DateTimeFormatter.ISO_INSTANT.format(from));
        periodDto.setDateTo(DateTimeFormatter.ISO_INSTANT.format(to));

        TimeInterval timeInterval = releaseDtoBuilder.createEntityFromDto(periodDto);

        Assertions.assertNotNull(timeInterval);
        Assertions.assertNotNull(timeInterval.getStart());
        Assertions.assertNotNull(timeInterval.getEnd());

        Date expectedFrom = Date.from(from.atZone(ZoneId.systemDefault()).toInstant());
        Date expectedTo = Date.from(to.atZone(ZoneId.systemDefault()).toInstant());
        Assertions.assertEquals(expectedFrom, timeInterval.getStart());
        Assertions.assertEquals(expectedTo, timeInterval.getEnd());
    }

    @Test
    void buildReleaseDtoFromEntity()
    {
        Release release = generateRandomRelease(new Product());
        release.setCreationDate(Calendar.getInstance());

        //given Logging type
        release.setSelectedLoggingInt(Platform.NOVAETHER);
        release.setSelectedLoggingPre(Platform.NOVA);
        release.setSelectedLoggingPro(Platform.ETHER);

        release.setSelectedDeployInt(Platform.NOVA);
        release.setSelectedDeployPre(Platform.NOVA);
        release.setSelectedDeployPro(Platform.ETHER);

        ReleaseDto rDto = releaseDtoBuilder.buildReleaseDtoFromEntity(release);
        Assertions.assertNotNull(rDto);

        Assertions.assertEquals(release.getId(), rDto.getId());
        Assertions.assertEquals(release.getName(), rDto.getReleaseName());
        Assertions.assertEquals(release.getDescription(), rDto.getDescription());
        Assertions.assertEquals(release.getCreationDate().getTimeInMillis(), rDto.getCreationDate());
        Assertions.assertNotNull(rDto.getReleaseConfig());

        //Check configuration
        ReleaseConfigDto configDto = rDto.getReleaseConfig();
        Assertions.assertNotNull(configDto.getIntConfig());
        Assertions.assertNotNull(configDto.getPreConfig());
        Assertions.assertNotNull(configDto.getProConfig());
    }

    @Test
    void buildReleaseDtoFromEntityConfiguration()
    {
        Release release = generateRandomRelease(new Product());
        release.setCreationDate(Calendar.getInstance());
        release.setDeploymentTypeInPro(DeploymentType.ON_DEMAND);

        ReleaseDto rDto;


        for (Platform loggingType : Arrays.stream(Platform.values()).filter(Platform::getIsValidToLogging).collect(Collectors.toList()))
        {
            //given Logging type
            release.setSelectedLoggingInt(loggingType);
            release.setSelectedLoggingPre(loggingType);
            release.setSelectedLoggingPro(loggingType);

            for (Platform dpdt :  Arrays.stream(Platform.values()).filter(Platform::getIsValidToDeploy).collect(Collectors.toList()))
            {
                //given
                release.setSelectedDeployInt(dpdt);
                release.setSelectedDeployPre(dpdt);
                release.setSelectedDeployPro(dpdt);

                rDto = releaseDtoBuilder.buildReleaseDtoFromEntity(release);
                Assertions.assertNotNull(rDto);

                Assertions.assertEquals(release.getId(), rDto.getId());
                Assertions.assertEquals(release.getName(), rDto.getReleaseName());
                Assertions.assertEquals(release.getDescription(), rDto.getDescription());
                Assertions.assertNotNull(rDto.getReleaseConfig());

                //Check configuration
                ReleaseConfigDto configDto = rDto.getReleaseConfig();
                Assertions.assertNotNull(configDto.getIntConfig());
                Assertions.assertNotNull(configDto.getPreConfig());
                Assertions.assertNotNull(configDto.getProConfig());

                //INT...
                ReleaseEnvConfigDto config = configDto.getIntConfig();
                Assertions.assertNotNull(config.getSelectedPlatforms());
                Assertions.assertEquals(release.getSelectedLoggingInt().name(), config.getSelectedPlatforms().getLoggingPlatform());
                Assertions.assertEquals(release.getSelectedDeployInt().name(), config.getSelectedPlatforms().getDeploymentPlatform());

                ManagementConfigDto managementConfigDto = configDto.getIntConfig().getManagementConfig();
                Assertions.assertNull(managementConfigDto);


                //PRE...
                config = configDto.getPreConfig();
                Assertions.assertNotNull(config.getSelectedPlatforms());
                Assertions.assertEquals(release.getSelectedLoggingPre().name(), config.getSelectedPlatforms().getLoggingPlatform());
                Assertions.assertEquals(release.getSelectedDeployPre().name(), config.getSelectedPlatforms().getDeploymentPlatform());

                managementConfigDto = configDto.getPreConfig().getManagementConfig();
                Assertions.assertNotNull(managementConfigDto);

                //PRO...
                config = configDto.getPreConfig();
                Assertions.assertNotNull(config.getSelectedPlatforms());
                Assertions.assertEquals(release.getSelectedLoggingPre().name(), config.getSelectedPlatforms().getLoggingPlatform());
                Assertions.assertEquals(release.getSelectedDeployPre().name(), config.getSelectedPlatforms().getDeploymentPlatform());

                managementConfigDto = configDto.getProConfig().getManagementConfig();
                Assertions.assertNotNull(managementConfigDto);
                Assertions.assertEquals(release.getDeploymentTypeInPro().name(), managementConfigDto.getDeploymentType());
            }
        }
    }

    @Test
    void buildReleaseDtoFromEntityManagementConfiguration()
    {
        Release release = generateRandomRelease(new Product());
        release.setCreationDate(Calendar.getInstance());

        ReleaseDto rDto;

        TimeInterval timeInterval = new TimeInterval();

        //PRE
        LocalDateTime ldt = LocalDateTime.now();
        timeInterval.setStart(Date.from(ldt.minusDays(RandomUtils.nextInt(1, 30)).atZone(ZoneId.systemDefault()).toInstant()));
        timeInterval.setEnd(Date.from(ldt.minusDays(RandomUtils.nextInt(1, 30)).atZone(ZoneId.systemDefault()).toInstant()));
        release.setAutomanageInPre(timeInterval);
        timeInterval.setStart(Date.from(ldt.minusDays(RandomUtils.nextInt(1, 30)).atZone(ZoneId.systemDefault()).toInstant()));
        timeInterval.setEnd(Date.from(ldt.minusDays(RandomUtils.nextInt(1, 30)).atZone(ZoneId.systemDefault()).toInstant()));
        release.setAutodeployInPre(timeInterval);

        //PRO
        timeInterval.setStart(Date.from(ldt.minusDays(RandomUtils.nextInt(1, 30)).atZone(ZoneId.systemDefault()).toInstant()));
        timeInterval.setEnd(Date.from(ldt.minusDays(RandomUtils.nextInt(1, 30)).atZone(ZoneId.systemDefault()).toInstant()));
        release.setAutoManageInPro(timeInterval);
        timeInterval = new TimeInterval();
        timeInterval.setStart(Date.from(ldt.minusDays(RandomUtils.nextInt(1, 30)).atZone(ZoneId.systemDefault()).toInstant()));
        timeInterval.setEnd(Date.from(ldt.minusDays(RandomUtils.nextInt(1, 30)).atZone(ZoneId.systemDefault()).toInstant()));
        release.setAutodeployInPro(timeInterval);

        for (DeploymentType deploymentType : DeploymentType.values())
        {
            release.setDeploymentTypeInPro(deploymentType);

            rDto = releaseDtoBuilder.buildReleaseDtoFromEntity(release);
            Assertions.assertNotNull(rDto);

            Assertions.assertEquals(release.getId(), rDto.getId());
            Assertions.assertEquals(release.getName(), rDto.getReleaseName());
            Assertions.assertEquals(release.getDescription(), rDto.getDescription());
            Assertions.assertNotNull(rDto.getReleaseConfig());

            //Check configuration
            ReleaseConfigDto configDto = rDto.getReleaseConfig();
            Assertions.assertNotNull(configDto.getIntConfig());
            Assertions.assertNotNull(configDto.getPreConfig());
            Assertions.assertNotNull(configDto.getProConfig());

            //INT...
            ManagementConfigDto managementConfigDto = configDto.getIntConfig().getManagementConfig();
            Assertions.assertNull(managementConfigDto);

            //PRE...
            managementConfigDto = configDto.getPreConfig().getManagementConfig();
            Assertions.assertNotNull(managementConfigDto);
            Assertions.assertEquals(release.getAutomanageInPre().getStart().toInstant().toString(), managementConfigDto.getAutoManage().getDateFrom());
            Assertions.assertEquals(release.getAutomanageInPre().getEnd().toInstant().toString(), managementConfigDto.getAutoManage().getDateTo());
            Assertions.assertEquals(release.getAutodeployInPre().getStart().toInstant().toString(), managementConfigDto.getAutoDeploy().getDateFrom());
            Assertions.assertEquals(release.getAutodeployInPre().getEnd().toInstant().toString(), managementConfigDto.getAutoDeploy().getDateTo());

            //PRO...
            managementConfigDto = configDto.getProConfig().getManagementConfig();
            Assertions.assertNotNull(managementConfigDto);
            Assertions.assertEquals(release.getAutoManageInPro().getStart().toInstant().toString(), managementConfigDto.getAutoManage().getDateFrom());
            Assertions.assertEquals(release.getAutoManageInPro().getEnd().toInstant().toString(), managementConfigDto.getAutoManage().getDateTo());
            Assertions.assertEquals(release.getAutodeployInPro().getStart().toInstant().toString(), managementConfigDto.getAutoDeploy().getDateFrom());
            Assertions.assertEquals(release.getAutodeployInPro().getEnd().toInstant().toString(), managementConfigDto.getAutoDeploy().getDateTo());
            Assertions.assertEquals(deploymentType.name(), managementConfigDto.getDeploymentType());

        }
    }


    @Test
    void buildDtoArrayFromEntityListEmpty()
    {
        Assertions.assertNotNull(releaseDtoBuilder.buildDtoArrayFromEntityList(Collections.emptyList()));
    }

    @Test
    void buildDtoArrayFromEntityList()
    {
        List<Release> releases = new ArrayList<>();
        Release release;
        ReleaseDto[] current;
        ReleaseConfigDto config;
        for (int i = 0; i < 10; i++)
        {
            release = generateRandomRelease(new Product());
            releases.add(release);
            current = releaseDtoBuilder.buildDtoArrayFromEntityList(releases);
            Assertions.assertNotNull(current);
            Assertions.assertEquals(releases.size(), current.length);

            //compare last of them...
            Assertions.assertEquals(release.getId(), current[i].getId());
            Assertions.assertEquals(release.getName(), current[i].getReleaseName());
            Assertions.assertEquals(release.getDescription(), current[i].getDescription());
            Assertions.assertNotNull(current[i].getReleaseConfig());
        }
    }

// /**
//     * Build DTO for ReleaseVersion.
//     *
//     * @param entity ReleaseVersion.
//     * @return ReleaseVersionInListDto
//     */
//    private ReleaseVersionInListDto buildDtoFromEntity(ReleaseVersion entity)
//    {
//        if (entity == null)
//        {
//            return null;
//        }
//
//        ReleaseVersionInListDto dto = new ReleaseVersionInListDto();
//
//        dto.setId(entity.getId());
//        dto.setReleaseVersionName(entity.getVersionName());
//        dto.setDescription(entity.getDescription());
//        dto.setStatus(entity.getStatus().name());
//        dto.setStatusDescription(entity.getStatusDescription());
//
//        if (entity.getCreationDate() != null)
//        {
//            dto.setCreationDate(entity.getCreationDate().getTimeInMillis());
//        }
//        if (entity.getDeletionDate() != null)
//        {
//            dto.setDeletionDate(entity.getDeletionDate().getTimeInMillis());
//        }
//
//        // Set the deployments on each environment if any.
//
//        // INT
//        dto.setDeploymentOnInt(
//                this.versionPlanDtoBuilder.build(
//                        entity.getId(),
//                        Environment.INT,
//                        DeploymentStatus.DEPLOYED));
//
//        // PRE
//        dto.setDeploymentOnPre(
//                this.versionPlanDtoBuilder.build(
//                        entity.getId(),
//                        Environment.PRE,
//                        DeploymentStatus.DEPLOYED));
//
//        // PRO
//        dto.setDeploymentOnPro(
//                this.versionPlanDtoBuilder.build(
//                        entity.getId(),
//                        Environment.PRO,
//                        DeploymentStatus.DEPLOYED));
//
//        // And return.
//        return dto;
//    }


    @Test
    void buildDtoFromReleaseVersionListEmpty()
    {
        Assertions.assertNotNull(releaseDtoBuilder.buildDtoFromReleaseVersionList(Collections.emptyList()));
    }

    @Test
    void buildDtoFromReleaseVersionList()
    {
        Release release = generateRandomRelease(new Product());

        final List<ReleaseVersion> releaseVersionList = new ArrayList<>();

        int numRV = RandomUtils.nextInt(2, 6);
        ReleaseVersion expected;
        for (int i = 0; i < numRV; i++)
        {
            expected = generateRandomReleaseVersion(release);
            releaseVersionList.add(expected);
        }

        // same RELVersionPlanDto
        RELVersionPlanDto relVPDto = new RELVersionPlanDto();
        Mockito.when(versionPlanDtoBuilder.build(Mockito.anyInt(), Mockito.any(), Mockito.any())).thenReturn(relVPDto);

        ReleaseVersionInListDto[] dtos = releaseDtoBuilder.buildDtoFromReleaseVersionList(releaseVersionList);

        Assertions.assertNotNull(dtos);
        Assertions.assertEquals(numRV, dtos.length);


        ReleaseVersionInListDto current;
        for (int i = 0; i < numRV; i++)
        {
            expected = releaseVersionList.get(i);
            current = dtos[i];

            Assertions.assertEquals(expected.getId(), current.getId());
            Assertions.assertEquals(expected.getVersionName(), current.getReleaseVersionName());
            Assertions.assertEquals(expected.getDescription(), current.getDescription());
            Assertions.assertEquals(expected.getStatus().name(), current.getStatus());
            Assertions.assertEquals(expected.getStatusDescription(), current.getStatusDescription());

            Mockito.verify(this.versionPlanDtoBuilder, Mockito.times(1)).build(expected.getId(), Environment.INT, DeploymentStatus.DEPLOYED);
            Mockito.verify(this.versionPlanDtoBuilder, Mockito.times(1)).build(expected.getId(), Environment.PRE, DeploymentStatus.DEPLOYED);
            Mockito.verify(this.versionPlanDtoBuilder, Mockito.times(1)).build(expected.getId(), Environment.PRO, DeploymentStatus.DEPLOYED);
        }
    }

    //-----------------------------------------------
    //              Auxiliary methods
    //-----------------------------------------------


    /**
     * Assertions for Release
     *
     * @param release        expected release created
     * @param relReleaseInfo current RELReleaseInfo created
     */
    private void assertionsForReleaseInfo(final Release release, final RELReleaseInfo relReleaseInfo)
    {
        Assertions.assertEquals(release.getName(), relReleaseInfo.getReleaseName());
        Assertions.assertEquals(release.getId(), relReleaseInfo.getId());
        Assertions.assertEquals(release.getDescription(), relReleaseInfo.getDescription());
        Assertions.assertNotNull(relReleaseInfo.getReleaseVersions());
        long rvAsReadyToDeploy = release.getReleaseVersions()
                .stream().filter(rv -> rv.getStatus() == ReleaseVersionStatus.READY_TO_DEPLOY).count();
        Assertions.assertEquals(rvAsReadyToDeploy, relReleaseInfo.getReleaseVersions().length);
    }

    /**
     * Assertions to Release
     *
     * @param releaseVersion     expected release version
     * @param releaseVersionInfo current RELReleaseVersionInfo created
     */
    private void assertionsForReleaseVersionInfo(ReleaseVersion releaseVersion, RELReleaseVersionInfo releaseVersionInfo)
    {
        Assertions.assertEquals(releaseVersion.getId(), releaseVersionInfo.getId());
        Assertions.assertEquals(releaseVersion.getVersionName(), releaseVersionInfo.getReleaseVersionName());
        Assertions.assertEquals(releaseVersion.getDescription(), releaseVersionInfo.getDescription());
        Assertions.assertNotNull(releaseVersionInfo.getServices());

        long servicesNumber = releaseVersion.getSubsystems().stream().mapToInt(sub -> sub.getServices().size()).sum();
        Assertions.assertEquals(servicesNumber, releaseVersionInfo.getServices().length, servicesNumber + "services was expected");
    }

    /**
     * Assertios for a Service
     *
     * @param service     expected info
     * @param serviceInfo serviceInfo created by Service
     */
    private void assertionsForServiceInfo(final ReleaseVersionService service, final RELServiceInfo serviceInfo)
    {
        Assertions.assertEquals(service.getId(), serviceInfo.getId());
        Assertions.assertEquals(service.getServiceName(), serviceInfo.getServiceName());
        Assertions.assertEquals(service.getDescription(), serviceInfo.getDescription());
    }

    /**
     * Assertions to compare services created and returned into RELReleaseVersionInfo and its ReleaseVersion
     *
     * @param releaseVersion original release version
     * @param rvinfo         RELReleaseVersionInfo built by release version
     */
    private void assertionsForListOfServicesAndServicesInfo(final ReleaseVersion releaseVersion, final RELReleaseVersionInfo rvinfo)
    {
        ReleaseVersionService expectedService;
        RELServiceInfo serviceInfo;

        //Collect services into map
        final Map<String, ReleaseVersionService> serviceByName = new HashMap<>();
        for (ReleaseVersionSubsystem sub : releaseVersion.getSubsystems())
        {
            sub.getServices().forEach(service -> serviceByName.put(service.getServiceName(), service));
        }

        ReleaseVersionService expected;
        for (RELServiceInfo current : rvinfo.getServices())
        {
            expected = serviceByName.get(current.getServiceName());
            Assertions.assertNotNull(expected, "There is a RELServiceInfo with no pair");
            this.assertionsForServiceInfo(expected, current);
        }

    }

    /**
     * Create a Random Release by product
     *
     * @param product product
     * @return release
     */
    private Release generateRandomRelease(final Product product)
    {
        Release release = new Release();
        release.setId(RandomUtils.nextInt(0, 1000));
        release.setName("random-relase-" + RandomStringUtils.randomAlphabetic(10));
        release.setDescription("random release");
        release.setCreationDate(GregorianCalendar.from(ZonedDateTime.now()));
        release.setProduct(product);
        release.setReleaseVersions(new ArrayList<>());
        return release;
    }


    /**
     * Generate a random Release Version by a Release
     *
     * @param release release
     * @return a Release Version
     */
    private ReleaseVersion generateRandomReleaseVersion(final Release release)
    {
        ReleaseVersion rv = new ReleaseVersion();

        rv.setId(RandomUtils.nextInt(0, 1000));
        rv.setRelease(release);
        rv.setVersionName("v1.0." + RandomUtils.nextInt(0, 10));
        rv.setCreationDate(GregorianCalendar.from(ZonedDateTime.now()));
        rv.setDescription("random release version of " + release.getName());
        rv.setIssueID(RandomStringUtils.randomAlphabetic(10));
        rv.setQualityValidation(RandomUtils.nextBoolean());
        rv.setStatus(ReleaseVersionStatus.READY_TO_DEPLOY);
        rv.setStatusDescription(RandomStringUtils.randomAlphabetic(10));

        //empty lists
        rv.setSubsystems(new ArrayList<>());
        rv.setDeployments(new ArrayList<>());

        return rv;
    }

    /**
     * Generate a random Subsystem by a Release Version
     *
     * @param rv release version
     * @return a Release Version Subsystem
     */
    private ReleaseVersionSubsystem generateRandomReleaseVersionSubsystem(final ReleaseVersion rv)
    {
        ReleaseVersionSubsystem subsystem = new ReleaseVersionSubsystem();

        subsystem.setSubsystemId(RandomUtils.nextInt(0, 1000));
        subsystem.setReleaseVersion(rv);
        subsystem.setStatus(AsyncStatus.DONE);
        subsystem.setCompilationJobName("JOB_" + RandomStringUtils.randomAlphabetic(10));
        subsystem.setStatusMessage("STATUS_" + RandomStringUtils.randomAlphabetic(10));
        subsystem.setTagName("TAG_" + RandomStringUtils.randomAlphabetic(10));
        subsystem.setTagUrl("URL_" + RandomStringUtils.randomAlphabetic(10));

        //empty list
        subsystem.setServices(new ArrayList<>());

        return subsystem;
    }

    /**
     * Generate a random Service by a Subsystem
     *
     * @param subsystem Subsystem
     * @return a Release Version Service
     */
    private ReleaseVersionService generateRandomReleaseVersionServiceBy(final ReleaseVersionSubsystem subsystem)
    {
        ReleaseVersionService rvs = new ReleaseVersionService();

        rvs.setId(RandomUtils.nextInt(0, 1000));
        rvs.setServiceName("service_" + rvs.getId());
        rvs.setDescription("desc:" + RandomStringUtils.randomAlphabetic(10));
        rvs.setVersion("v1.0." + RandomUtils.nextInt(0, 10));
        rvs.setArtifactId("artifact_" + RandomStringUtils.randomAlphabetic(5));
        rvs.setFinalName("finalname_" + rvs.getServiceName());
        rvs.setFolder("folder");
        rvs.setGroupId("com.bbva.uuaa");
        rvs.setHasForceCompilation(RandomUtils.nextBoolean());
        rvs.setImageName("image_" + rvs.getFinalName());
        rvs.setServiceType(ServiceType.NOVA.getServiceType());
        rvs.setVersionSubsystem(subsystem);
        rvs.setNovaVersion("nova-21");


        //empty list
        rvs.setConsumers(Collections.emptyList());
        rvs.setConsumers(Collections.emptyList());
        rvs.setApiImplementations(Collections.emptyList());
        rvs.setServers(Collections.emptyList());
        rvs.setProperties(Collections.emptyList());
        rvs.setServers(Collections.emptyList());

        return rvs;
    }
}