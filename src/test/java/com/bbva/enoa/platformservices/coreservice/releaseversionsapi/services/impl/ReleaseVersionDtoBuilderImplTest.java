package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsageDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.*;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.JvmJdkConfigurationChecker;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IVersionPlanDtoBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.mockito.Mockito.when;


public class ReleaseVersionDtoBuilderImplTest
{
    @Mock
    private IVersionPlanDtoBuilder iVersionPlanDtoBuilder;
    @Mock
    private IToolsClient toolsClient;
    @Mock
    private ILibraryManagerService iLibraryManagerService;
    @Mock
    private ReleaseVersionServiceRepository releaseVersionServiceRepository;
    @Mock
    private JvmJdkConfigurationChecker jvmJdkConfigurationChecker;
    @InjectMocks
    private ReleaseVersionDtoBuilderImpl releaseVersionDtoBuilderImpl;

    @BeforeEach
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void libraryThatServicesUseTest()
    {
        //GIVEN
        ReleaseVersion releaseVersion = generateRandomReleaseVersion();

        List<ReleaseVersionService> libraries = new ArrayList<>();

        for(int i = 0; i < 1; i++)
        {
            ReleaseVersionService l = generateRandomRVService();
            l.setServiceType(ServiceType.LIBRARY_JAVA.getServiceType());
            Product p = generateRandomProduct();
            Release r = generateRandomRelease();
            r.setProduct(p);
            ReleaseVersion rv = generateRandomReleaseVersion();
            rv.setRelease(r);
            ReleaseVersionSubsystem rvsub = generateRandomRVSubsystem();
            rvsub.setReleaseVersion(rv);
            l.setVersionSubsystem(rvsub);
            libraries.add(l);
        }

        LMLibraryEnvironmentsDTO[] lmLibraries = generateLMLibraries(libraries.toArray(new ReleaseVersionService[0]));

        //WHEN
        for(ReleaseVersionSubsystem releaseVersionSubsystem : releaseVersion.getSubsystems())
        {
            TOSubsystemDTO toSubsystemDTO = new TOSubsystemDTO();
            toSubsystemDTO.setSubsystemId(releaseVersionSubsystem.getSubsystemId());

            when(this.toolsClient.getSubsystemById(releaseVersionSubsystem.getSubsystemId())).thenReturn(toSubsystemDTO);

            for(ReleaseVersionService releaseVersionService : releaseVersionSubsystem.getServices())
            {
                when(this.iLibraryManagerService.getUsedLibrariesByService(releaseVersionService.getId(), "BUILD")).thenReturn(lmLibraries);

                for(ReleaseVersionService lib : libraries)
                {
                    when(this.releaseVersionServiceRepository.findById(lib.getId())).thenReturn(Optional.of(lib));
                }
            }
        }

        //THEN
        RVReleaseVersionDTO response = this.releaseVersionDtoBuilderImpl.build(releaseVersion);

        for (RVReleaseVersionSubsystemDTO releaseVersionSubsystemDto : response.getSubsystems())
        {
            for (RVReleaseVersionServiceDTO releaseVersionServiceDto : releaseVersionSubsystemDto.getServices())
            {
                for(RVLibrariesDTO librariesDTO : releaseVersionServiceDto.getLibrariesUsed())
                {
                    Assertions.assertEquals(librariesDTO.getFullName(), lmLibraries[0].getFullName());
                    Assertions.assertEquals(librariesDTO.getInte(), lmLibraries[0].getInte());
                    Assertions.assertEquals(librariesDTO.getPre(), lmLibraries[0].getPre());
                    Assertions.assertEquals(librariesDTO.getPro(), lmLibraries[0].getPro());
                    Assertions.assertEquals(librariesDTO.getRelease(), libraries.get(0).getVersionSubsystem().getReleaseVersion().getRelease().getName());
                    Assertions.assertEquals(librariesDTO.getUuaa(), libraries.get(0).getVersionSubsystem().getReleaseVersion().getRelease().getProduct().getUuaa());
                }
            }
        }
    }

    @Test
    public void librariesThatServicesUseTest()
    {
        //GIVEN
        ReleaseVersion releaseVersion = generateRandomReleaseVersion();

        List<ReleaseVersionService> libraries = new ArrayList<>();

        for(int i = 0; i < RandomUtils.nextInt(1, 10); i++)
        {
            ReleaseVersionService l = generateRandomRVService();
            l.setServiceType(ServiceType.LIBRARY_JAVA.getServiceType());
            Product p = generateRandomProduct();
            Release r = generateRandomRelease();
            r.setProduct(p);
            ReleaseVersion rv = generateRandomReleaseVersion();
            rv.setRelease(r);
            ReleaseVersionSubsystem rvsub = generateRandomRVSubsystem();
            rvsub.setReleaseVersion(rv);
            l.setVersionSubsystem(rvsub);
            libraries.add(l);
        }

        LMLibraryEnvironmentsDTO[] lmLibraries = generateLMLibraries(libraries.toArray(new ReleaseVersionService[0]));

        //WHEN
        for(ReleaseVersionSubsystem releaseVersionSubsystem : releaseVersion.getSubsystems())
        {
            TOSubsystemDTO toSubsystemDTO = new TOSubsystemDTO();
            toSubsystemDTO.setSubsystemId(releaseVersionSubsystem.getSubsystemId());

            when(this.toolsClient.getSubsystemById(releaseVersionSubsystem.getSubsystemId())).thenReturn(toSubsystemDTO);

            for(ReleaseVersionService releaseVersionService : releaseVersionSubsystem.getServices())
            {
                when(this.iLibraryManagerService.getUsedLibrariesByService(releaseVersionService.getId(), "BUILD")).thenReturn(lmLibraries);

                for(ReleaseVersionService lib : libraries)
                {
                    when(this.releaseVersionServiceRepository.findById(lib.getId())).thenReturn(Optional.of(lib));
                }
            }
        }

        //THEN
        RVReleaseVersionDTO response = this.releaseVersionDtoBuilderImpl.build(releaseVersion);

        for (RVReleaseVersionSubsystemDTO releaseVersionSubsystemDto : response.getSubsystems())
        {
            for (RVReleaseVersionServiceDTO releaseVersionServiceDto : releaseVersionSubsystemDto.getServices())
            {
                Assertions.assertTrue(releaseVersionServiceDto.getLibrariesUsed().length > 0);
                Assertions.assertEquals(releaseVersionServiceDto.getLibrariesUsed().length, libraries.size());
                Assertions.assertFalse(releaseVersionServiceDto.getLibraryUsedBy().length > 0);
            }
        }
    }

    @Test
    public void serviceThatUsesGivenLibrariesTest()
    {
        //GIVEN
        ReleaseVersion rv = generateRandomReleaseVersion();

        List<ReleaseVersionService> services = new ArrayList<>();
        List<LMUsageDTO> usages = new ArrayList<>();

        for(int i = 0; i < 1; i++)
        {
            ReleaseVersionService service = generateRandomRVService();
            Product product = generateRandomProduct();
            Release release = generateRandomRelease();
            release.setProduct(product);
            ReleaseVersion releaseVersion = generateRandomReleaseVersion();
            releaseVersion.setRelease(release);
            ReleaseVersionSubsystem releaseVersionSubsystem = generateRandomRVSubsystem();
            releaseVersionSubsystem.setReleaseVersion(releaseVersion);
            service.setVersionSubsystem(releaseVersionSubsystem);

            LMUsageDTO usage = new LMUsageDTO();
            usage.setServiceId(service.getId());
            usage.setUsage(generateRandomLibraryUsage());
            services.add(service);
            usages.add(usage);
        }


        //WHEN
        for(ReleaseVersionSubsystem releaseVersionSubsystem : rv.getSubsystems())
        {
            TOSubsystemDTO toSubsystemDTO = new TOSubsystemDTO();
            toSubsystemDTO.setSubsystemId(releaseVersionSubsystem.getSubsystemId());

            when(this.toolsClient.getSubsystemById(releaseVersionSubsystem.getSubsystemId())).thenReturn(toSubsystemDTO);

            for(ReleaseVersionService releaseVersionService : releaseVersionSubsystem.getServices())
            {
                releaseVersionService.setServiceType(ServiceType.LIBRARY_JAVA.getServiceType());

                for(LMUsageDTO lmUsageDTO : usages)
                {
                    lmUsageDTO.setRvslibraryId(releaseVersionService.getId());
                }

                when(this.iLibraryManagerService.getLibraryUsages(releaseVersionService.getId())).thenReturn(usages.toArray(new LMUsageDTO[usages.size()]));

                for(ReleaseVersionService service : services)
                {
                    when(this.releaseVersionServiceRepository.findById(service.getId())).thenReturn(Optional.of(service));
                }
            }
        }

        //THEN
        RVReleaseVersionDTO response = this.releaseVersionDtoBuilderImpl.build(rv);

        for (RVReleaseVersionSubsystemDTO releaseVersionSubsystemDto : response.getSubsystems())
        {
            for (RVReleaseVersionServiceDTO releaseVersionServiceDto : releaseVersionSubsystemDto.getServices())
            {
                for(RVLibraryUsedByDTO service : releaseVersionServiceDto.getLibraryUsedBy())
                {
                    Assertions.assertEquals(service.getUuaa(), services.get(0).getVersionSubsystem().getReleaseVersion().getRelease().getProduct().getUuaa());
                    Assertions.assertEquals(service.getRelease(), services.get(0).getVersionSubsystem().getReleaseVersion().getRelease().getName());
                    Assertions.assertEquals(service.getReleaseVersion(), services.get(0).getVersionSubsystem().getReleaseVersion().getVersionName());
                    Assertions.assertEquals(service.getService(), services.get(0).getServiceName());

                    for(String usage : service.getEnvironments())
                    {
                        Assertions.assertEquals(usages.get(0).getUsage(), usage);
                    }
                }
            }
        }
    }

    @Test
    public void servicesThatUsesGivenLibrariesTest()
    {
        //GIVEN
        ReleaseVersion rv = generateRandomReleaseVersion();

        List<ReleaseVersionService> services = new ArrayList<>();
        List<LMUsageDTO> usages = new ArrayList<>();

        for(int i = 0; i < 1; i++)
        {
            ReleaseVersionService service = generateRandomRVService();
            Product product = generateRandomProduct();
            Release release = generateRandomRelease();
            release.setProduct(product);
            ReleaseVersion releaseVersion = generateRandomReleaseVersion();
            releaseVersion.setRelease(release);
            ReleaseVersionSubsystem releaseVersionSubsystem = generateRandomRVSubsystem();
            releaseVersionSubsystem.setReleaseVersion(releaseVersion);
            service.setVersionSubsystem(releaseVersionSubsystem);

            LMUsageDTO usage = new LMUsageDTO();
            usage.setServiceId(service.getId());
            usage.setUsage(generateRandomLibraryUsage());
            services.add(service);
            usages.add(usage);
        }


        //WHEN
        for(ReleaseVersionSubsystem releaseVersionSubsystem : rv.getSubsystems())
        {
            TOSubsystemDTO toSubsystemDTO = new TOSubsystemDTO();
            toSubsystemDTO.setSubsystemId(releaseVersionSubsystem.getSubsystemId());

            when(this.toolsClient.getSubsystemById(releaseVersionSubsystem.getSubsystemId())).thenReturn(toSubsystemDTO);

            for(ReleaseVersionService releaseVersionService : releaseVersionSubsystem.getServices())
            {
                releaseVersionService.setServiceType(ServiceType.LIBRARY_JAVA.getServiceType());

                for(LMUsageDTO lmUsageDTO : usages)
                {
                    lmUsageDTO.setRvslibraryId(releaseVersionService.getId());
                }

                when(this.iLibraryManagerService.getLibraryUsages(releaseVersionService.getId())).thenReturn(usages.toArray(new LMUsageDTO[usages.size()]));

                for(ReleaseVersionService service : services)
                {
                    when(this.releaseVersionServiceRepository.findById(service.getId())).thenReturn(Optional.of(service));
                }
            }
        }

        //THEN
        RVReleaseVersionDTO response = this.releaseVersionDtoBuilderImpl.build(rv);

        for (RVReleaseVersionSubsystemDTO releaseVersionSubsystemDto : response.getSubsystems())
        {
            for (RVReleaseVersionServiceDTO releaseVersionServiceDto : releaseVersionSubsystemDto.getServices())
            {
                Assertions.assertTrue(releaseVersionServiceDto.getLibraryUsedBy().length > 0);
                Assertions.assertEquals(releaseVersionServiceDto.getLibraryUsedBy().length, services.size());
                Assertions.assertFalse(releaseVersionServiceDto.getLibrariesUsed().length > 0);
            }
        }
    }

    private static ReleaseVersion generateRandomReleaseVersion()
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();

        releaseVersion.setId(RandomUtils.nextInt(1, 9999));
        releaseVersion.setVersionName(RandomStringUtils.randomAlphanumeric(20));
        releaseVersion.setDescription(RandomStringUtils.randomAlphanumeric(20));
        releaseVersion.setIssueID(RandomStringUtils.randomNumeric(5));
        releaseVersion.setQualityValidation(new Random().nextBoolean());
        releaseVersion.setStatus(generateRandomRVStatus());
        releaseVersion.setStatusDescription(RandomStringUtils.randomAlphanumeric(20));
        releaseVersion.setRelease(generateRandomRelease());

        List<ReleaseVersionSubsystem> subsystems = new ArrayList<>();
        for (int i = 0; i < 1; i++)
        {
            ReleaseVersionSubsystem releaseVersionSubsystem = generateRandomRVSubsystem();
            subsystems.add(releaseVersionSubsystem);
        }

        releaseVersion.setSubsystems(subsystems);

        return releaseVersion;
    }

    private static ReleaseVersionSubsystem generateRandomRVSubsystem()
    {
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();

        releaseVersionSubsystem.setId(RandomUtils.nextInt(1, 9999));
        releaseVersionSubsystem.setSubsystemId(RandomUtils.nextInt(1, 9999));

        List<ReleaseVersionService> services = new ArrayList<>();
        for (int i = 0; i < 2; i++)
        {
            ReleaseVersionService service = generateRandomRVService();
            services.add(service);
        }

        releaseVersionSubsystem.setServices(services);

        return releaseVersionSubsystem;
    }

    private static ReleaseVersionService generateRandomRVService()
    {
        ReleaseVersionService service = new ReleaseVersionService();

        service.setId(RandomUtils.nextInt(1, 9999));
        service.setFinalName(RandomStringUtils.randomAlphanumeric(50));
        service.setServiceName(RandomStringUtils.randomAlphanumeric(10));
        service.setServiceType(generateRandomServiceType().getServiceType());

        return service;
    }

    private static Release generateRandomRelease()
    {
        Release release = new Release();

        release.setId(RandomUtils.nextInt(1, 9999));
        release.setName(RandomStringUtils.randomAlphanumeric(10));

        return release;
    }

    private static ReleaseVersionStatus generateRandomRVStatus()
    {
        List<ReleaseVersionStatus> statuses = List.of(ReleaseVersionStatus.BUILDING, ReleaseVersionStatus.ERRORS, ReleaseVersionStatus.READY_TO_DEPLOY, ReleaseVersionStatus.STORAGED);
        Random r = new Random();
        return statuses.get(r.nextInt(statuses.size()));
    }

    private static ServiceType generateRandomServiceType()
    {
        List<ServiceType> types = List.of(ServiceType.API_REST_JAVA_SPRING_BOOT, ServiceType.BATCH_SCHEDULER_NOVA, ServiceType.BATCH_JAVA_SPRING_BATCH, ServiceType.BATCH_JAVA_SPRING_CLOUD_TASK, ServiceType.DAEMON_JAVA_SPRING_BOOT);
        Random r = new Random();
        return types.get(r.nextInt(types.size()));
    }

    private static String generateRandomLibraryUsage()
    {
        List<String> usages = List.of("BUILD", "INT", "PRE", "PRO");
        Random r = new Random();
        return usages.get(r.nextInt(usages.size()));
    }

    private static LMLibraryEnvironmentsDTO[] generateLMLibraries(ReleaseVersionService[] releaseVersionServices)
    {
        return Arrays.stream(releaseVersionServices).map(releaseVersionService -> {
            LMLibraryEnvironmentsDTO lmLibraryEnvironmentsDTO = new LMLibraryEnvironmentsDTO();
            lmLibraryEnvironmentsDTO.setFullName(releaseVersionService.getFinalName());
            lmLibraryEnvironmentsDTO.setReleaseVersionServiceId(releaseVersionService.getId());
            lmLibraryEnvironmentsDTO.setInte(new Random().nextBoolean());
            lmLibraryEnvironmentsDTO.setPre(new Random().nextBoolean());
            lmLibraryEnvironmentsDTO.setPro(new Random().nextBoolean());
            return  lmLibraryEnvironmentsDTO;
        }).toArray(LMLibraryEnvironmentsDTO[]::new);
    }

    private static Product generateRandomProduct()
    {
        Product product = new Product();
        product.setUuaa(RandomStringUtils.randomAlphabetic(4).toUpperCase());
        List<Release> releases = new ArrayList<>();
        releases.add(generateRandomRelease());
        product.setReleases(releases);
        return product;
    }
}