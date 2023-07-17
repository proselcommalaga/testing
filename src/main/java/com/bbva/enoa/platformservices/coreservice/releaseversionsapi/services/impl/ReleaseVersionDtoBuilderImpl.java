package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsageDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVLibrariesDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVLibraryUsedByDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVMetadataDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVReleaseDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVReleaseVersionDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVReleaseVersionServiceDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVReleaseVersionSubsystemDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.release.entities.AllowedJdk;
import com.bbva.enoa.datamodel.model.release.entities.EphoenixService;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.JvmJdkConfigurationChecker;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionDtoBuilderModalityBased;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IVersionPlanDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds a DTO from a ReleaseVersion entity.
 * <p>
 * Created by xe52580 on 27/02/2017.
 */
@Slf4j
@Service
public class
ReleaseVersionDtoBuilderImpl implements IReleaseVersionDtoBuilder
{
    private final IVersionPlanDtoBuilder iIVersionPlanDtoBuilder;
    private final ILibraryManagerService iLibraryManagerService;
    private final ReleaseVersionServiceRepository releaseVersionServiceRepository;
    private final String jiraIssueURL;
    private final IToolsClient toolsClient;
    private final JvmJdkConfigurationChecker jvmJdkConfigurationChecker;
    private final IReleaseVersionDtoBuilderModalityBased releaseVersionDtoBuilderModalityBased;

    @Autowired
    public ReleaseVersionDtoBuilderImpl(
            IVersionPlanDtoBuilder iIVersionPlanDtoBuilder, ILibraryManagerService iLibraryManagerService,
            ReleaseVersionServiceRepository releaseVersionServiceRepository,
            @Value("${nova.tools.jira:https://cibproducts.grupobbva.com/JIRA/}") String jiraIssueURL,
            IToolsClient toolsClient, JvmJdkConfigurationChecker jvmJdkConfigurationChecker,
            final IReleaseVersionDtoBuilderModalityBased releaseVersionDtoBuilderModalityBased)
    {
        this.iIVersionPlanDtoBuilder = iIVersionPlanDtoBuilder;
        this.iLibraryManagerService = iLibraryManagerService;
        this.releaseVersionServiceRepository = releaseVersionServiceRepository;
        this.jiraIssueURL = jiraIssueURL;
        this.toolsClient = toolsClient;
        this.jvmJdkConfigurationChecker = jvmJdkConfigurationChecker;
        this.releaseVersionDtoBuilderModalityBased = releaseVersionDtoBuilderModalityBased;
    }

    @Override
    public RVReleaseVersionDTO build(ReleaseVersion releaseVersion)
    {
        // Add basic data, using defaults.
        RVReleaseVersionDTO releaseVersionDto = new RVReleaseVersionDTO();
        BeanUtils.copyProperties(releaseVersion, releaseVersionDto);

        // Copy different properties.
        releaseVersionDto.setStatus(releaseVersion.getStatus().name());

        // Issue Key, writes the corresponding URL with the issue key associated if it exists.
        if (releaseVersion.getIssueID() != null)
        {
            releaseVersionDto.setIssueID(this.jiraIssueURL + "browse/" + releaseVersion.getIssueID());
        }

        if (releaseVersion.getCreationDate() != null)
        {
            releaseVersionDto.setCreationDate(releaseVersion.getCreationDate().getTimeInMillis());
        }

        if (releaseVersion.getDeletionDate() != null)
        {
            releaseVersionDto.setDeletionDate(releaseVersion.getDeletionDate().getTimeInMillis());
        }

        // Add Release data.
        releaseVersionDto.setRelease(this.buildReleaseDto(releaseVersion.getRelease()));

        // Add Subsystems data, including tags, services and validation.
        releaseVersionDto.setSubsystems(this.buildSubsystems(releaseVersion.getSubsystems()));

        // Set the deployments on each environment if any.

        // INT
        releaseVersionDto.setDeploymentOnInt(
                this.iIVersionPlanDtoBuilder.build(
                        releaseVersionDto.getId(),
                        Environment.INT,
                        DeploymentStatus.DEPLOYED));

        // PRE
        releaseVersionDto.setDeploymentOnPre(
                this.iIVersionPlanDtoBuilder.build(
                        releaseVersionDto.getId(),
                        Environment.PRE,
                        DeploymentStatus.DEPLOYED));

        // PRO
        releaseVersionDto.setDeploymentOnPro(
                this.iIVersionPlanDtoBuilder.build(
                        releaseVersionDto.getId(),
                        Environment.PRO,
                        DeploymentStatus.DEPLOYED));

        log.debug("[{}] -> [{}]: Successfully built a DTO for release version [{}]",
                Constants.RELEASE_VERSION_DTO_BUILDER, "build", releaseVersion.getId());

        log.trace("[{}] -> [{}]: Release version DTO contents: [{}]",
                Constants.RELEASE_VERSION_DTO_BUILDER, "build", releaseVersionDto);

        // Return DTO.
        return releaseVersionDto;
    }


    ////////////////////////////////////////////   PRIVATE METHODS   \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


    private void addMetadataDto(List<RVMetadataDTO> metadataList, final String propertyName, final String value)
    {
        if (propertyName != null && value != null)
        {
            // Add metadata.
            RVMetadataDTO metadatum = new RVMetadataDTO();
            metadatum.setKey(propertyName);
            metadatum.setValue(value);
            metadataList.add(metadatum);
        }
    }


    /**
     * Builds a RVReleaseDto from a Release.
     *
     * @param release Release
     * @return RVReleaseDto
     */
    private RVReleaseDTO buildReleaseDto(Release release)
    {
        RVReleaseDTO dto = new RVReleaseDTO();

        // Copy all properties shared by DTO and entity.
        BeanUtils.copyProperties(release, dto);

        // Copy all the different properties.
        dto.setReleaseName(release.getName());

        return dto;
    }


    /**
     * Builds an array of ReleaseVersionServiceDto from the services
     * of a given subsystem.
     *
     * @param services List of services.
     * @return ReleaseVersionServiceDto[]
     */
    private RVReleaseVersionServiceDTO[] buildServices(List<ReleaseVersionService> services)
    {
        List<RVReleaseVersionServiceDTO> dtoServices = new ArrayList<>();
        // For each one:
        for (ReleaseVersionService service : services)
        {
            // Set service info from its pom.xml.
            RVReleaseVersionServiceDTO serviceDto = new RVReleaseVersionServiceDTO();

            // Copy properties.
            BeanUtils.copyProperties(service, serviceDto);
            serviceDto.setServiceType(service.getServiceType());

            // Project definition file - pom.xml.
            if (service.getProjectDefinitionFile() != null)
            {
                serviceDto.setProjectDefinitionUrl(service.getProjectDefinitionFile().getUrl());
                serviceDto.setProjectDefinitionFile(service.getProjectDefinitionFile().getContents());

            }

            // Project definition file - nova.yml .
            if (service.getNovaYml() != null)
            {
                serviceDto.setNovaYamlUrl(service.getNovaYml().getUrl());
            }

            final boolean isMultiJdk = jvmJdkConfigurationChecker.isMultiJdk(service);

            if (isMultiJdk)
            {
                final AllowedJdk allowedJdk = service.getAllowedJdk();
                serviceDto.setJvmVersion(allowedJdk.getJvmVersion());
                serviceDto.setJdkVersion(allowedJdk.getReadableName());
            }

            if (service.getNodePackageFile() != null)
            {
                serviceDto.setNodePackageUrl(service.getNodePackageFile().getUrl());
                serviceDto.setNodePackageFile(service.getNodePackageFile().getContents());
            }

            // Specific data for ePhoenix Services
            EphoenixService ephoenixService = service.getEphoenixData();
            if (ephoenixService != null)
            {
                // modules
                this.processModules(serviceDto, ephoenixService.getModules());
                // Add metadata.
                List<RVMetadataDTO> metadataList = new ArrayList<>();
                addMetadataDto(metadataList, Constants.EPHOENIX_INSTANCE_USER, ephoenixService.getInstanceUuaa());
                addMetadataDto(metadataList, Constants.UUAA, ephoenixService.getInstanceUser());
                addMetadataDto(metadataList, Constants.EPHOENIX_VERSION, ephoenixService.getEphoenixVersion());
                addMetadataDto(metadataList, Constants.EPHOENIX_DEPLOYMENT, ephoenixService.getDeploymentLine());
                addMetadataDto(metadataList, Constants.EPHOENIX_INSTANCE_UUAAS, toString(ephoenixService.getUuaas()));
                addMetadataDto(metadataList, Constants.EPHOENIX_INSTANCE_PORT, ephoenixService.getInstancePort());
                addMetadataDto(metadataList, Constants.EPHOENIX_DEVELOPMENT_ENVIRONMENT, ephoenixService.getDevelopmentEnvironment().toString());
                addMetadataDto(metadataList, Constants.EPHOENIX_DEVELOPMENT_ENVIRONMENT_PROMOTION, ephoenixService.getDevelopmentEnvironmentPromotion().toString());
                serviceDto.setMetadata(metadataList.toArray(new RVMetadataDTO[0]));
            }

            // Specific data for API.
            List<ApiImplementation<?, ?, ?>> novaApiClient = service.getConsumers();
            if (novaApiClient != null)
            {
                List<RVApiDTO> rvApiDtos = this.getListRVApiDto(novaApiClient);
                serviceDto.setApisConsumed(rvApiDtos.toArray(new RVApiDTO[0]));
            }


            List<ApiImplementation<?, ?, ?>> novaApisServer = service.getServers();
            if (novaApisServer != null)
            {
                List<RVApiDTO> rvApiDtos = this.getListRVApiDto(novaApisServer);
                serviceDto.setApisServed(rvApiDtos.toArray(new RVApiDTO[0]));
            }

            //Add library relations
            this.addLibraryRelations(serviceDto, service.getId());

            // Add it.
            dtoServices.add(serviceDto);
        }

        // Return as an array.
        return dtoServices.toArray(new RVReleaseVersionServiceDTO[services.size()]);
    }

    /**
     * Builds a ReleaseVersionSubsystemDto array from a list of
     * ReleaseVersionSubsystem.
     *
     * @param subsystems List of ReleaseVersionSubsystem.
     * @return ReleaseVersionSubsystemDto[].
     */
    private RVReleaseVersionSubsystemDTO[] buildSubsystems(List<ReleaseVersionSubsystem> subsystems)
    {

        log.debug("[{}] -> [{}]: Subsystems: [{}]", Constants.RELEASE_VERSION_DTO_BUILDER, "buildSubsystems", subsystems);

        // Create array of release version subsystems.
        RVReleaseVersionSubsystemDTO[] dtoSubsystems = new RVReleaseVersionSubsystemDTO[subsystems.size()];

        if (subsystems.isEmpty())
        {
            log.warn("[{}] -> [{}]: Release version has no subsystems, aborting", Constants.RELEASE_VERSION_DTO_BUILDER, "buildSubsystems");
            return new RVReleaseVersionSubsystemDTO[0];
        }

        // For each product subsystem, build a release version subsystem.
        int i = 0;
        for (ReleaseVersionSubsystem subsystem : subsystems)
        {
            TOSubsystemDTO toSubsystemDTO = this.toolsClient.getSubsystemById(subsystem.getSubsystemId());

            // Build DTO.
            dtoSubsystems[i] = new RVReleaseVersionSubsystemDTO();
            BeanUtils.copyProperties(subsystem, dtoSubsystems[i]);

            // Compilation job.
            if (!StringUtils.isEmpty(subsystem.getCompilationJobName()))
            {
                dtoSubsystems[i].setCompilationJobName(subsystem.getCompilationJobName());
                dtoSubsystems[i].setCompilationJobUrl(toSubsystemDTO.getJenkinsUrl());
            }

            // Set the product subsystem.
            dtoSubsystems[i].setProductSubsystemName(toSubsystemDTO.getSubsystemName());
            dtoSubsystems[i].setProductSubsystemId(toSubsystemDTO.getSubsystemId());
            dtoSubsystems[i].setProductSubsystemDesc(toSubsystemDTO.getDescription());
            dtoSubsystems[i].setStatus(subsystem.getStatus().name());
            dtoSubsystems[i].setProductSubsystemType(toSubsystemDTO.getSubsystemType());

            dtoSubsystems[i].setTagName(subsystem.getTagName());
            dtoSubsystems[i].setTagUrl(subsystem.getTagUrl());

            // Build the services DTO.
            dtoSubsystems[i].setServices(this.buildServices(subsystem.getServices()));

            // Go next.
            i++;
        }

        return dtoSubsystems;
    }

    /**
     * Processes List of APIS Client AND Server
     *  @param novaApis  List of Apis (Client or Server)
     *
     */
    private List<RVApiDTO> getListRVApiDto(List<ApiImplementation<?, ?, ?>> novaApis)
    {

        return novaApis.stream()
                .map(apiImplementation -> this.releaseVersionDtoBuilderModalityBased.buildRVApiDTO(apiImplementation))
                .collect(Collectors.toList());
    }

    /**
     * Processes Ephoenix modules.
     *
     * @param service service
     * @param modules pom modules
     */
    private void processModules(RVReleaseVersionServiceDTO service, List<String> modules)
    {
        if (modules != null)
        {
            service.setModules(new String[modules.size()]);

            // Iterate through modules.
            for (int i = 0; i < modules.size(); i++)
            {
                // Add module.
                service.getModules()[i] = modules.get(i);
            }
        }
    }


    /**
     * Returns a String representation of the list, with the specified separator
     *
     * @param list - the List
     * @return a String representation of the list, with the specified separator
     */
    private String toString(List<String> list)
    {
        StringBuilder sb = new StringBuilder();
        if (list != null && !list.isEmpty())
        {
            for (String item : list)
            {
                sb.append(item);
                sb.append(',');
            }
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * Add into dto the relations between services and libraries
     *
     * @param releaseVersionServiceDto Dto to fill
     * @param rvServiceId              release version service identifier (could be library or service)
     */
    private void addLibraryRelations(final RVReleaseVersionServiceDTO releaseVersionServiceDto, final Integer rvServiceId)
    {
        List<RVLibraryUsedByDTO> rvLibraryUsedByDTOList = new ArrayList<>();
        List<RVLibrariesDTO> rvLibrariesDTOList = new ArrayList<>();

        ServiceType serviceType = ServiceType.getValueOf(releaseVersionServiceDto.getServiceType());
        if (ServiceType.isLibrary(serviceType))
        {
            rvLibraryUsedByDTOList = this.getServicesThatUsesLibrary(rvServiceId);
        }
        else
        {
            rvLibrariesDTOList = this.getUsedLibraries(rvServiceId);

        }

        releaseVersionServiceDto.setLibraryUsedBy(rvLibraryUsedByDTOList.toArray(new RVLibraryUsedByDTO[0]));
        releaseVersionServiceDto.setLibrariesUsed(rvLibrariesDTOList.toArray(new RVLibrariesDTO[0]));
    }

    /**
     * Get all services that uses a specific library
     *
     * @param rvsLibraryId release version service id of library
     * @return array with all info about services that uses the library
     */
    private List<RVLibraryUsedByDTO> getServicesThatUsesLibrary(final Integer rvsLibraryId)
    {
        LMUsageDTO[] usages = this.iLibraryManagerService.getLibraryUsages(rvsLibraryId);

        Map<Integer, List<LMUsageDTO>> lmUsagesMappedByServiceId = Arrays.stream(usages)
                .collect(Collectors.groupingBy(LMUsageDTO::getServiceId));

        List<RVLibraryUsedByDTO> rvLibraryUsedByDTOList = new ArrayList<>();

        for (Map.Entry<Integer, List<LMUsageDTO>> entry : lmUsagesMappedByServiceId.entrySet())
        {
            ReleaseVersionService service = this.releaseVersionServiceRepository.findById(entry.getKey()).orElse(null);

            if (service != null)
            {
                RVLibraryUsedByDTO rvLibraryUsedByDTO = new RVLibraryUsedByDTO();

                rvLibraryUsedByDTO.setService(service.getServiceName());
                rvLibraryUsedByDTO.setReleaseVersion(service.getVersionSubsystem().getReleaseVersion().getVersionName());
                rvLibraryUsedByDTO.setRelease(service.getVersionSubsystem().getReleaseVersion().getRelease().getName());
                rvLibraryUsedByDTO.setUuaa(service.getVersionSubsystem().getReleaseVersion().getRelease().getProduct().getUuaa());

                rvLibraryUsedByDTO.setEnvironments(entry.getValue().stream().map(LMUsageDTO::getUsage).toArray(String[]::new));

                rvLibraryUsedByDTOList.add(rvLibraryUsedByDTO);
            }
        }

        return rvLibraryUsedByDTOList;
    }

    /**
     * Get all libraries used by a specific service
     *
     * @return array with all info about libraries used by service
     */
    private List<RVLibrariesDTO> getUsedLibraries(final Integer rvsServiceId)
    {
        List<RVLibrariesDTO> librariesDTOS = new ArrayList<>();

        LMLibraryEnvironmentsDTO[] lmLibraryEnvironmentsDTOS = this.iLibraryManagerService.getUsedLibrariesByService(rvsServiceId, Constants.LIBRARY_BUILD_USAGE);

        for (LMLibraryEnvironmentsDTO lmLibraryEnvironmentsDTO : lmLibraryEnvironmentsDTOS)
        {
            RVLibrariesDTO rvLibrariesDTO = new RVLibrariesDTO();

            ReleaseVersionService library = this.releaseVersionServiceRepository.findById(lmLibraryEnvironmentsDTO.getReleaseVersionServiceId())
                    .orElseThrow(() -> new NovaException(ReleaseVersionError.getNoSuchReleaseVersionError(lmLibraryEnvironmentsDTO.getReleaseVersionServiceId())));

            rvLibrariesDTO.setUuaa(library.getVersionSubsystem().getReleaseVersion().getRelease().getProduct().getUuaa());
            rvLibrariesDTO.setRelease(library.getVersionSubsystem().getReleaseVersion().getRelease().getName());
            rvLibrariesDTO.setFullName(lmLibraryEnvironmentsDTO.getFullName());
            rvLibrariesDTO.setInte(lmLibraryEnvironmentsDTO.getInte());
            rvLibrariesDTO.setPre(lmLibraryEnvironmentsDTO.getPre());
            rvLibrariesDTO.setPro(lmLibraryEnvironmentsDTO.getPro());

            librariesDTOS.add(rvLibrariesDTO);
        }

        return librariesDTOS;
    }
}
