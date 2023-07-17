package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.*;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.core.novabootstarter.util.ServiceNamingUtils;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiState;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.common.entities.LobFile;
import com.bbva.enoa.datamodel.model.release.entities.*;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal.IApiManagerServiceModalityBased;
import com.bbva.enoa.platformservices.coreservice.common.repositories.AllowedJdkRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ApiVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.Utils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDockerRegistryClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionEntityBuilderService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.google.common.base.Strings;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.io.File.separator;

/**
 * Build a new {@link ReleaseVersion} entity.
 */
@Service
@Slf4j
@AllArgsConstructor
public class ReleaseVersionEntityBuilderServiceImpl implements IReleaseVersionEntityBuilderService
{
    private static final String POM_FILENAME = "pom.xml";
    private static final String PACKAGE_FILENAME = "package.json";
    private final IToolsClient toolsService;
    private final ApiVersionRepository apiVersionRepository;
    private final EntityManager entityManager;
    private final IVersioncontrolsystemClient vcsClient;
    private final IDockerRegistryClient dockerRegistryClient;
    private final AllowedJdkRepository allowedJdkRepository;
    private final IApiManagerServiceModalityBased apiManagerServiceModalityBased;
    private final Utils utils;

    @Override
    @Transactional
    public ReleaseVersion buildReleaseVersionEntityFromDto(Release release, final RVReleaseVersionDTO versionVersionToAdd, final String ivUser, final boolean isIvUserPlatformAdmin)
    {
        // Validate release version APIs
        this.checkReleaseVersionApis(versionVersionToAdd);

        // Validate that the RV doesn't contain duplicate service names
        checkUniqueServiceNames(versionVersionToAdd);

        // Create the new release version
        ReleaseVersion releaseVersion = this.buildReleaseVersionEntityFromDto(versionVersionToAdd, release.getName(), ivUser, isIvUserPlatformAdmin);

        releaseVersion.setRelease(release);
        releaseVersion.setStatus(ReleaseVersionStatus.ERRORS);

        // Add the release version to the release
        releaseVersion.setRelease(release);
        release.getReleaseVersions().add(releaseVersion);

        // Persist data
        this.entityManager.flush();
        return releaseVersion;
    }


    @Override
    @Transactional
    public ReleaseVersion buildReleaseVersionEntityFromDto(Release release, final RVVersionDTO versionDTO, final String ivUser, final boolean isIvUserPlatformAdmin)
    {
        // Validate release version APIs
        this.checkReleaseVersionApis(versionDTO);

        // Add subsystems from dto
        this.validateReleaseVersionSubsystemAndServices(versionDTO);

        // Create the new release version with basic information
        ReleaseVersion releaseVersion = ReleaseVersionEntityBuilderServiceImpl.buildBasicReleaseVersion(versionDTO.getDescription(), versionDTO.getVersionName());

        // Add release version subsystems to release version
        List<ReleaseVersionSubsystem> versionSubsystemList = this.buildReleaseVersionSubsystem(release, versionDTO, releaseVersion, ivUser, isIvUserPlatformAdmin);
        releaseVersion.setSubsystems(versionSubsystemList);

        releaseVersion.setRelease(release);
        releaseVersion.setStatus(ReleaseVersionStatus.ERRORS);

        // Add the release version to the release
        releaseVersion.setRelease(release);
        release.getReleaseVersions().add(releaseVersion);

        // Persist data
        this.entityManager.flush();
        return releaseVersion;
    }

    @Override
    @Transactional
    public ReleaseVersionSubsystem buildReleaseVersionSubsystemFromDTO(RVReleaseVersionSubsystemDTO dto, final String releaseName, final String ivUser, final boolean isIvUserPlatformAdmin)
    {
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();

        // Copy all properties shared by DTO and entity.
        BeanUtils.copyProperties(dto, releaseVersionSubsystem);

        releaseVersionSubsystem.setTagName(dto.getTagName());
        releaseVersionSubsystem.setTagUrl(dto.getTagUrl());

        Integer subsystemId = dto.getProductSubsystemId();

        ReleaseVersionEntityBuilderServiceImpl.validateSubsystemId(subsystemId);
        TOSubsystemDTO subsystem = this.toolsService.getSubsystemById(subsystemId);
        this.checkProductSubsystem(subsystem);
        releaseVersionSubsystem.setSubsystemId(subsystemId);

        // Services
        RVReleaseVersionServiceDTO[] dtoServices = dto.getServices();
        this.buildServicesNotNull(releaseName, releaseVersionSubsystem, dtoServices, ivUser, isIvUserPlatformAdmin);

        return releaseVersionSubsystem;
    }

    @Override
    @Transactional
    public ReleaseVersionSubsystem buildReleaseVersionSubsystemFromDTO(RVVersionSubsystemDTO versionSubsystemDTO, final String releaseName,
                                                                       final Set<String> serviceVersionSet, final String ivUser, final boolean isIvUserPlatformAdmin)
    {
        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();

        // Copy all properties shared by DTO and entity.
        BeanUtils.copyProperties(versionSubsystemDTO, releaseVersionSubsystem);

        releaseVersionSubsystem.setTagName(versionSubsystemDTO.getTag().getTagName());
        releaseVersionSubsystem.setTagUrl(versionSubsystemDTO.getTag().getTagUrl());

        Integer subsystemId = versionSubsystemDTO.getSubsystem().getId();

        ReleaseVersionEntityBuilderServiceImpl.validateSubsystemId(subsystemId);
        TOSubsystemDTO subsystem = this.toolsService.getSubsystemById(subsystemId);
        this.checkProductSubsystem(subsystem);
        releaseVersionSubsystem.setSubsystemId(subsystemId);

        // Services
        RVReleaseVersionServiceDTO[] dtoServices = ReleaseVersionEntityBuilderServiceImpl.getFilteredServicesToBuild(versionSubsystemDTO, serviceVersionSet);
        this.buildServicesNotNull(releaseName, releaseVersionSubsystem, dtoServices, ivUser, isIvUserPlatformAdmin);

        return releaseVersionSubsystem;
    }

    ////////////////////////////////////////  PRIVATE  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


    /**
     * If the release version has subsystems, adds them to the list
     *
     * @param dto                   release version dto (with the subsystems info)
     * @param releaseName           name of the release
     * @param releaseVersion        release version
     * @param subsystems            list of the subsystems
     * @param ivUser                the user requester
     * @param isIvUserPlatformAdmin said if the user requester is platform admin
     */
    private void addSubsystems(RVReleaseVersionDTO dto, String releaseName, ReleaseVersion releaseVersion, List<ReleaseVersionSubsystem> subsystems, final String ivUser, final boolean isIvUserPlatformAdmin)
    {
        if (dto.getSubsystems() == null)
        {
            log.error("[{}] -> [{}]: Error adding subsystems, the release version [{}] with id [{}] does not have subsystems", Constants
                    .RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "addSubsystems", dto.getVersionName(), dto.getId());
            throw new NovaException(ReleaseVersionError.getUnexpectedError(),
                    "Creating a new Release Version without subsystems! ");
        }
        else
        {
            this.addSubsystemsFromDto(dto, releaseName, releaseVersion, subsystems, ivUser, isIvUserPlatformAdmin);
        }
    }

    /**
     * Set each subsystem in the dto to the subsystem list
     *
     * @param dto                   release version dto (with the subsystems info)
     * @param releaseName           name of the release
     * @param releaseVersion        release version
     * @param subsystems            list of the subsystems
     * @param ivUser                the user requester
     * @param isIvUserPlatformAdmin said if the user requester is platform admin
     */
    private void addSubsystemsFromDto(final RVReleaseVersionDTO dto, final String releaseName, final ReleaseVersion releaseVersion, final List<ReleaseVersionSubsystem> subsystems, final String ivUser, final boolean isIvUserPlatformAdmin)
    {
        // Add release version subsystems to release version
        for (RVReleaseVersionSubsystemDTO subsystemDto : dto.getSubsystems())
        {
            ReleaseVersionSubsystem subsystem = this.buildReleaseVersionSubsystemFromDTO(subsystemDto, releaseName, ivUser, isIvUserPlatformAdmin);
            subsystem.setReleaseVersion(releaseVersion);
            subsystems.add(subsystem);
        }
    }

    /**
     * Build a ephoenix services from a release version service dto
     *
     * @param serviceDto Release version service DTO
     * @return a {@code EphoenixService} entity
     * @throws NovaException Ephoenix services with no metadata
     */
    private EphoenixService buildEphoenixService(final RVReleaseVersionServiceDTO serviceDto)
    {
        EphoenixService ephoenixService = new EphoenixService();

        ephoenixService.setInstanceName(serviceDto.getArtifactId());

        // Metadata
        if (serviceDto.getMetadata() == null || serviceDto.getMetadata().length == 0)
        {
            log.error("[{}] -> [{}]: Error building ephoenix service, the service metadata is NULL or empty", Constants
                    .RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "buildEphoenixService");
            throw new NovaException(ReleaseVersionError.getEphoenixServiceWithNoMetadataError(),
                    "Missed metadata info in ephoenix service!");
        }
        for (RVMetadataDTO metadata : serviceDto.getMetadata())
        {
            switch (metadata.getKey())
            {
                case Constants.EPHOENIX_DEPLOYMENT:
                    ephoenixService.setDeploymentLine(metadata.getValue());
                    break;
                case Constants.EPHOENIX_VERSION:
                    ephoenixService.setEphoenixVersion(metadata.getValue());
                    break;
                case Constants.UUAA:
                    ephoenixService.setInstanceUuaa(metadata.getValue());
                    break;
                case Constants.EPHOENIX_INSTANCE_USER:
                    ephoenixService.setInstanceUser(metadata.getValue());
                    break;
                case Constants.EPHOENIX_INSTANCE_UUAAS:
                    String uuaas = metadata.getValue();
                    List<String> items = Arrays.asList(uuaas.split("\\s*,\\s*"));
                    ephoenixService.setUuaas(items);
                    break;
                case Constants.EPHOENIX_INSTANCE_PORT:
                    ephoenixService.setInstancePort(metadata.getValue());
                    break;
                case Constants.EPHOENIX_DEVELOPMENT_ENVIRONMENT:
                    ephoenixService.setDevelopmentEnvironment(Constants.TRUE.equalsIgnoreCase(metadata.getValue()));
                    break;
                case Constants.EPHOENIX_DEVELOPMENT_ENVIRONMENT_PROMOTION:
                    ephoenixService.setDevelopmentEnvironmentPromotion(Constants.TRUE.equalsIgnoreCase(metadata.getValue()));
                    break;
                default:
                    break;
            }
        }

        // Modules  - (A ephoenix service without modules is feasible, it does not give errors)
        if (serviceDto.getModules() != null && serviceDto.getModules().length > 0)
        {
            ephoenixService.setModules(Arrays.asList(serviceDto.getModules()));
        }

        return ephoenixService;
    }

    /**
     * Creates a new ReleaseVersion from a ReleaseVersionDto.
     *
     * @param releaseVersionDto     DTO.
     * @param releaseName           Release name.
     * @param ivUser                the user requester
     * @param isIvUserPlatformAdmin said if the user requester is platform admin
     * @return a {@code ReleaseVersion} entity
     */
    private ReleaseVersion buildReleaseVersionEntityFromDto(RVReleaseVersionDTO releaseVersionDto, String releaseName, final String ivUser, final boolean isIvUserPlatformAdmin)
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();

        releaseVersion.setDescription(releaseVersionDto.getDescription());
        releaseVersion.setVersionName(releaseVersionDto.getVersionName());

        List<ReleaseVersionSubsystem> subsystems = new ArrayList<>();
        this.addSubsystems(releaseVersionDto, releaseName, releaseVersion, subsystems, ivUser, isIvUserPlatformAdmin);
        releaseVersion.setSubsystems(subsystems);

        return releaseVersion;
    }

    /**
     * Create release version object with basic info
     *
     * @param description release version description
     * @param versionName release version name
     * @return new release version
     */
    private static ReleaseVersion buildBasicReleaseVersion(final String description, final String versionName)
    {
        ReleaseVersion releaseVersion = new ReleaseVersion();

        releaseVersion.setDescription(description);
        releaseVersion.setVersionName(versionName);

        return releaseVersion;
    }

    /**
     * Check if all the mandatory fields of the service Dto are completed
     *
     * @param serviceDto         the service dto to check
     * @param releaseVersionName the release version name that belongs the service dto
     * @param subsystemTagName   the subsystem tag name that belongs the service dto
     */
    private static void validateServiceDto(final RVReleaseVersionServiceDTO serviceDto, final String releaseVersionName, final String subsystemTagName)
    {
        // Validate service dto
        if (Strings.isNullOrEmpty(serviceDto.getGroupId()))
        {
            log.error("[{}] -> [{}]: Error checking group id of the service dto: [{}] trying to create a new release version: [{}] of the subsystem TAG name: [{}]. Is null or empty.",
                    Constants.RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "checkServiceDto", serviceDto.getServiceName(), releaseVersionName, subsystemTagName);
            throw new NovaException(ReleaseVersionError.getGroupIdNotFoundError(), "Trying to create a release version with a wrong/not found group id of the service");
        }

        if (Strings.isNullOrEmpty(serviceDto.getArtifactId()))
        {
            log.error("[{}] -> [{}]: Error checking artifact id of the service dto: [{}] trying to create a new release version: [{}] of the subsystem TAG name: [{}]. Is null or empty.",
                    Constants.RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "checkServiceDto", serviceDto.getServiceName(), releaseVersionName, subsystemTagName);
            throw new NovaException(ReleaseVersionError.getArtifactIdNotFoundError(), "Trying to create a release version with a wrong/not found artifact id of the service");
        }

        if (Strings.isNullOrEmpty(serviceDto.getVersion()))
        {
            log.error("[{}] -> [{}]: Error checking version of the service dto: [{}] trying to create a new release version: [{}] of the subsystem TAG name: [{}]. Is null or empty.",
                    Constants.RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "checkServiceDto", serviceDto.getServiceName(), releaseVersionName, subsystemTagName);
            throw new NovaException(ReleaseVersionError.getVersionNotFoundError(), "Trying to create a release version with a wrong/not found version of the service");
        }

        if (Strings.isNullOrEmpty(releaseVersionName))
        {
            log.error("[{}] -> [{}]: Error checking release version name of the service dto: [{}] trying to create a new release version: [{}] of the subsystem TAG name: [{}]. Is null or empty.",
                    Constants.RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "checkServiceDto", serviceDto.getServiceName(), releaseVersionName, subsystemTagName);
            throw new NovaException(ReleaseVersionError.getReleaseNameNotFoundError(), "Trying to create a release version name with a wrong/not found name of the service");
        }

        if (Strings.isNullOrEmpty(serviceDto.getServiceName()))
        {
            log.error("[{}] -> [{}]: Error checking service name of the service dto: [{}] trying to create a new release version: [{}] of the subsystem TAG name: [{}]. Is null or empty.",
                    Constants.RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "checkServiceDto", serviceDto.getServiceName(), releaseVersionName, subsystemTagName);
            throw new NovaException(ReleaseVersionError.getServiceNameNotFoundError(), "Trying to create a release version with a wrong/not found service name of the service");
        }

        if (Strings.isNullOrEmpty(serviceDto.getServiceType()))
        {
            log.error("[{}] -> [{}]: Error checking service type of the service dto: [{}] trying to create a new release version: [{}] of the subsystem TAG name: [{}]. Is null or empty.",
                    Constants.RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "checkServiceDto", serviceDto.getServiceName(), releaseVersionName, subsystemTagName);
            throw new NovaException(ReleaseVersionError.getServiceTypeNotFoundError(), "Trying to create a release version with a wrong/not found service type of the service");
        }
    }

    /**
     * Create the services and add them to the subsystem
     *
     * @param releaseName               name of the release
     * @param releaseVersionSubsystem   release version subsystem info
     * @param releaseVersionServiceDtos List of the service dto
     * @param ivUser                    the user requester
     * @param isIvUserPlatformAdmin     said if the user requester is platform admin
     */
    private void buildServices(ReleaseVersionSubsystem releaseVersionSubsystem, final String releaseName, final RVReleaseVersionServiceDTO[] releaseVersionServiceDtos, final String ivUser, final boolean isIvUserPlatformAdmin)
    {
        // Services of the subsystem
        List<ReleaseVersionService> services = releaseVersionSubsystem.getServices();

        for (RVReleaseVersionServiceDTO serviceDto : releaseVersionServiceDtos)
        {
            // Check the service Dto
            ReleaseVersionEntityBuilderServiceImpl.validateServiceDto(serviceDto, releaseName, releaseVersionSubsystem.getTagName());

            // Create the ReleaseVersionDtoBuilderServiceImpl
            ReleaseVersionService releaseVersionService = new ReleaseVersionService();

            // Get the service type and set into release version service
            ServiceType serviceType = ServiceType.valueOf(serviceDto.getServiceType());
            releaseVersionService.setServiceType(serviceType.getServiceType());

            releaseVersionService.setVersionSubsystem(releaseVersionSubsystem);
            releaseVersionService.setServiceName(serviceDto.getServiceName());
            releaseVersionService.setDescription(serviceDto.getDescription());

            if (ServiceType.BATCH_SCHEDULER_NOVA == serviceType)
            {
                // Batch scheduler nova service does not have image and does not have to be compiled in Jenkins
                releaseVersionService.setImageName("");
                releaseVersionService.setHasForceCompilation(false);
            }
            else
            {
                // Generate service image name
                String serviceImageName = ServiceNamingUtils.getImageName(serviceDto.getGroupId(), serviceDto.getArtifactId(), serviceDto.getVersion(), releaseName);
                releaseVersionService.setImageName(serviceImageName);

                // Check if the iv user is platform admin for knowing if compiling or not the service
                if (isIvUserPlatformAdmin)
                {
                    // If user is platform admin role, set the value established from platform admin user (directly from nova dashboard form)
                    releaseVersionService.setHasForceCompilation(serviceDto.getHasForcedCompilation());
                }
                else
                {
                    // For any other users, if the image is already in Registry, this service will not be compiled. If the image does not found, service will be compiled
                    releaseVersionService.setHasForceCompilation(!this.dockerRegistryClient.isImageInRegistry(ivUser, serviceImageName) || serviceDto.getHasForcedCompilation());
                }
            }

            releaseVersionService.setGroupId(serviceDto.getGroupId());
            releaseVersionService.setFinalName(serviceDto.getFinalName());
            releaseVersionService.setArtifactId(serviceDto.getArtifactId());
            releaseVersionService.setVersion(serviceDto.getVersion());
            releaseVersionService.setNovaVersion(serviceDto.getNovaVersion());
            releaseVersionService.setFolder(serviceDto.getFolder());

            //Include served and consumed apis
            this.setServedApisToReleaseVersionService(releaseVersionService, serviceDto);
            this.setConsumedApisToReleaseVersionService(releaseVersionService, serviceDto);

            // Specific data for ephoenix services
            if (serviceType.isEPhoenix())
            {
                releaseVersionService.setEphoenixData(this.buildEphoenixService(serviceDto));
            }

            // Supports multiple filesystems
            releaseVersionService.setSupportsMultiFilesystem(this.supportsMultiFilesystem(serviceType));

            TOSubsystemDTO subsystemDTO = this.toolsService.getSubsystemById(releaseVersionSubsystem.getSubsystemId());
            switch (serviceType)
            {
                case API_REST_JAVA_SPRING_BOOT:
                case API_JAVA_SPRING_BOOT:
                case BATCH_JAVA_SPRING_CLOUD_TASK:
                case BATCH_JAVA_SPRING_BATCH:
                case DAEMON_JAVA_SPRING_BOOT:
                case DEPENDENCY:
                case LIBRARY_JAVA:
                case FRONTCAT_JAVA_SPRING_MVC:
                case FRONTCAT_JAVA_J2EE:
                    LobFile novaYmlContent = this.getLobFileFromVcs(
                            subsystemDTO.getRepoId(),
                            releaseVersionSubsystem.getTagName(),
                            Constants.NOVA_YML,
                            Constants.NOVA_YML,
                            releaseVersionSubsystem.getTagUrl(),
                            releaseVersionService.getFolder());
                    if (novaYmlContent != null)
                    {
                        releaseVersionService.setNovaYml(novaYmlContent);
                    }
                    // Add the pom.xml file.
                    releaseVersionService.setProjectDefinitionFile(
                            this.getLobFileFromVcs(
                                    subsystemDTO.getRepoId(),
                                    releaseVersionSubsystem.getTagName(),
                                    POM_FILENAME,
                                    POM_FILENAME,
                                    releaseVersionSubsystem.getTagUrl(),
                                    releaseVersionService.getFolder())
                    );
                    break;
                case CDN_ANGULAR_THIN2:
                case CDN_ANGULAR_THIN3:
                case API_REST_NODE_JS_EXPRESS:
                case LIBRARY_THIN2:
                case LIBRARY_NODE:
                    releaseVersionService.setNovaYml(
                            this.getLobFileFromVcs(
                                    subsystemDTO.getRepoId(),
                                    releaseVersionSubsystem.getTagName(),
                                    Constants.NOVA_YML,
                                    Constants.NOVA_YML,
                                    releaseVersionSubsystem.getTagUrl(),
                                    releaseVersionService.getFolder()));
                    // Add the project.json file.
                    releaseVersionService.setProjectDefinitionFile(
                            this.getLobFileFromVcs(
                                    subsystemDTO.getRepoId(),
                                    releaseVersionSubsystem.getTagName(),
                                    Constants.PACKAGE_JSON,
                                    Constants.PACKAGE_JSON,
                                    releaseVersionSubsystem.getTagUrl(),
                                    releaseVersionService.getFolder()));
                    break;
                case API_REST_PYTHON_FLASK:
                case BATCH_PYTHON:
                case LIBRARY_PYTHON:
                    releaseVersionService.setNovaYml(
                            this.getLobFileFromVcs(
                                    subsystemDTO.getRepoId(),
                                    releaseVersionSubsystem.getTagName(),
                                    Constants.NOVA_YML,
                                    Constants.NOVA_YML,
                                    releaseVersionSubsystem.getTagUrl(),
                                    releaseVersionService.getFolder()));
                    // Add the pom.xml file.
                    releaseVersionService.setProjectDefinitionFile(
                            this.getLobFileFromVcs(
                                    subsystemDTO.getRepoId(),
                                    releaseVersionSubsystem.getTagName(),
                                    Constants.PYTHON_REQUIREMENTS,
                                    Constants.PYTHON_REQUIREMENTS,
                                    releaseVersionSubsystem.getTagUrl(),
                                    releaseVersionService.getFolder()));
                    break;
                case CDN_POLYMER_CELLS:
                case LIBRARY_TEMPLATE:
                    releaseVersionService.setNovaYml(
                            this.getLobFileFromVcs(
                                    subsystemDTO.getRepoId(),
                                    releaseVersionSubsystem.getTagName(),
                                    Constants.NOVA_YML,
                                    Constants.NOVA_YML,
                                    releaseVersionSubsystem.getTagUrl(),
                                    releaseVersionService.getFolder()));
                    break;
                case BATCH_SCHEDULER_NOVA:
                    // Add the nova yml file
                    releaseVersionService.setNovaYml(
                            this.getLobFileFromVcs(
                                    subsystemDTO.getRepoId(),
                                    releaseVersionSubsystem.getTagName(),
                                    Constants.NOVA_YML,
                                    Constants.NOVA_YML,
                                    releaseVersionSubsystem.getTagUrl(),
                                    releaseVersionService.getFolder()));
                    // Add the scheduler file.
                    releaseVersionService.setProjectDefinitionFile(
                            this.getLobFileFromVcs(
                                    subsystemDTO.getRepoId(),
                                    releaseVersionSubsystem.getTagName(),
                                    Constants.SCHUEDULER_YML,
                                    Constants.SCHUEDULER_YML,
                                    releaseVersionSubsystem.getTagUrl(),
                                    releaseVersionService.getFolder()));
                    break;
                case NODE:
                    // Add the package.json file.
                    releaseVersionService.setNodePackageFile(
                            this.getLobFileFromVcs(
                                    subsystemDTO.getRepoId(),
                                    releaseVersionSubsystem.getTagName(),
                                    PACKAGE_FILENAME,
                                    PACKAGE_FILENAME,
                                    releaseVersionSubsystem.getTagUrl(),
                                    releaseVersionService.getFolder()));
                    break;
                default:
                    // Add the pom.xml file (old versions)
                    releaseVersionService.setProjectDefinitionFile(
                            this.getLobFileFromVcs(
                                    subsystemDTO.getRepoId(),
                                    releaseVersionSubsystem.getTagName(),
                                    POM_FILENAME,
                                    POM_FILENAME,
                                    releaseVersionSubsystem.getTagUrl(),
                                    releaseVersionService.getFolder()));
            }

            final LobFile novaYml = releaseVersionService.getNovaYml();
            if (novaYml != null)
            {
                final String contents = novaYml.getContents();
                if (contents.contains("jdkVersion"))
                {
                    String jvmVersion = contents.substring(contents.indexOf("languageVersion:") + "languageVersion:".length(), contents.indexOf('\n', contents.indexOf("languageVersion:") + "languageVersion".length())).replace("\"", "").trim();
                    String jdkVersion = contents.substring(contents.indexOf("jdkVersion:") + "jdkVersion:".length(), contents.indexOf('\n', contents.indexOf("jdkVersion:") + "jdkVersion".length())).replace("\"", "").trim();
                    final AllowedJdk allowedJdk = allowedJdkRepository.findByJvmVersionAndJdk(jvmVersion, jdkVersion);
                    releaseVersionService.setAllowedJdk(allowedJdk);
                }
            }
            // Finally, add the service to the list.
            services.add(releaseVersionService);
        }
    }

    private boolean supportsMultiFilesystem(ServiceType serviceType)
    {
        return !ServiceType.isLegacy(serviceType)
                && !serviceType.isCdn()
                && !ServiceType.isLibrary(serviceType)
                && !serviceType.isEPhoenix()
                && !ServiceType.isFrontcat(serviceType)
                && serviceType != ServiceType.DEPENDENCY;
    }


    /**
     * Create services for the release version subsystem info if the service list contain something
     *
     * @param releaseName             name of the release
     * @param releaseVersionSubsystem release version subsystem info
     * @param dtoServices             List of the service dto
     * @param ivUser                  the user requester
     * @param isIvUserPlatformAdmin   said if the user requester is platform admin
     */
    private void buildServicesNotNull(final String releaseName, final ReleaseVersionSubsystem releaseVersionSubsystem, final RVReleaseVersionServiceDTO[] dtoServices, final String ivUser, final boolean isIvUserPlatformAdmin)
    {
        if (dtoServices == null || dtoServices.length == 0)
        {
            log.warn("[{}] -> [{}]: Subsystem [{}] of Release Version [{}] is going to be ignored because does not have services. Check if services has been filtered in previous steps.",
                    Constants.RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "buildServicesNotNull",
                    releaseVersionSubsystem.getSubsystemId(), releaseName);
        }
        else
        {
            // Create services
            this.buildServices(releaseVersionSubsystem, releaseName, dtoServices, ivUser, isIvUserPlatformAdmin);
        }
    }

    /**
     * Builds a URL to a file from a project on a given tag
     * in the VCS repo.
     *
     * @param tagUrl    URL to the tag in the VCS repo.
     * @param folder    Folder of the project.
     * @param fileRoute Route to the file.
     * @return The URL.
     */
    private String buildUrl(
            String tagUrl,
            String folder,
            String fileRoute
    )
    {
        return
                tagUrl +
                        separator +
                        folder +
                        separator +
                        fileRoute;
    }

    /**
     * Check if the product subsystem exist
     *
     * @param subsystemDTO product subsystem
     */
    private void checkProductSubsystem(TOSubsystemDTO subsystemDTO)
    {
        if (subsystemDTO == null)
        {
            log.error("[{}] -> [{}]: Error checking product subsystem, the product subsystem is NULL",
                    Constants.RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "checkProductSubsystem");
            throw new NovaException(ReleaseVersionError.getProductSubsystemNotFoundError(),
                    "Missed product subsystem in creation of a Release version");
        }
    }

    /**
     * Validate release version APIs
     *
     * @param releaseVersionDto release version DTO under validation
     * @throws NovaException In case a served API is duplicated
     */
    private void checkReleaseVersionApis(RVReleaseVersionDTO releaseVersionDto)
    {
        Set<Integer> servedApiIds = new HashSet<>();
        for (RVReleaseVersionSubsystemDTO subsystemDto : releaseVersionDto.getSubsystems())
        {
            for (RVReleaseVersionServiceDTO serviceDto : subsystemDto.getServices())
            {
                this.checkDuplicatedServedApis(serviceDto.getApisServed(), servedApiIds, releaseVersionDto.getId());
            }
        }
    }

    /**
     * Validate release version APIs
     *
     * @param versionDTO release version DTO under validation
     * @throws NovaException In case a served API is duplicated
     */
    private void checkReleaseVersionApis(RVVersionDTO versionDTO)
    {
        Set<Integer> servedApiIds = new HashSet<>();
        for (RVVersionSubsystemDTO versionSubsystemDTO : versionDTO.getSubsystems())
        {
            for (RVReleaseVersionServiceDTO versionServiceDTO : versionSubsystemDTO.getServices())
            {
                //check apis not null
                if (versionServiceDTO.getApisServed() == null)
                {
                    log.debug("[{}] -> [{}]: No served apis on release version [{}], service [{}]'", Constants.RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "checkReleaseVersionServiceApis",
                            versionDTO.getId(), versionServiceDTO.getServiceName());
                }
                else
                {
                    this.checkDuplicatedServedApis(versionServiceDTO.getApisServed(), servedApiIds, versionDTO.getId());
                }
            }
        }
    }

    private void checkDuplicatedServedApis(final RVApiDTO[] rvApiDTOS, final Set<Integer> servedApiIds, final Integer rvId)
    {
        for (RVApiDTO rvApiDto : rvApiDTOS)
        {
            // fix/CIBNOVAP-1275: A BackToBack API can be served (publisher) on as many service as you want
            if(!ApiModality.ASYNC_BACKTOBACK.getModality().equals(rvApiDto.getModality()))
            {
                if (servedApiIds.contains(rvApiDto.getId()))
                {
                    log.error("[{}] -> [{}]: Error adding release version [{}] , all the served APIs in a release version must be " +
                                    "unique. API with id [{}] is duplicated.", Constants.RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "checkDuplicatedServedApis",
                            rvId, rvApiDto.getId());

                    throw new NovaException(ReleaseVersionError.getDuplicatdApiError(), "Invalid release version. A served API is duplicated");
                }
                servedApiIds.add(rvApiDto.getId());
            }
            else
            {
                log.debug("[{}] -> [{}]: Nothing to do because the API {}->{} is a backToBack one",
                        Constants.RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "checkDuplicatedServedApis",
                        rvApiDto.getId(), rvApiDto.getApiName());
            }
        }
    }

    /**
     * Check if the product subsystem id is not null
     *
     * @param productSubsId product subsystem id
     */
    private static void validateSubsystemId(Integer productSubsId)
    {
        if (productSubsId == null)
        {
            log.error("[{}] -> [{}]: Error checking product subsystem id, NULL subsystem id found", Constants.RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "checkSubsystemId");
            throw new NovaException(ReleaseVersionError.getNullProductSubsystemIdError(), "Creating a release version with a wrong product subsystem id");
        }
    }


    /**
     * Creates a new {@link LobFile} from a file retrieved from a VCS
     * repository, using the given ID, tag and relative route to file.
     *
     * @param repoId    ID from the repo.
     * @param tagName   Tag.
     * @param filename  Name of the file.
     * @param fileRoute Relative route of the file (including its name).
     * @param tagUrl    URL to the tag in the VCS repo.
     * @param folder    Of the project.
     * @return a {@code LobFile}
     */
    private LobFile getLobFileFromVcs(
            Integer repoId,
            String tagName,
            String filename,
            String fileRoute,
            String tagUrl,
            String folder)
    {
        // Build the file.
        LobFile lobfile = new LobFile();
        lobfile.setName(filename);

        // Set the URL to the file in the project.
        lobfile.setUrl(this.buildUrl(tagUrl, folder, fileRoute));
        try
        {
            // Get the contents.
            byte[] contents = this.vcsClient.getFileFromProject(folder + "/" + fileRoute, repoId, tagName);
            // And set them.
            if (contents != null)
            {
                lobfile.setContents(new String(contents, StandardCharsets.UTF_8));
            }
            else
            {
                return null;
            }

            return lobfile;
        }
        catch (NovaException e)
        {
            return null;
        }
    }


    /**
     * Create a non null list with NovaApiImplementation as served api and set into ReleaseVersionService
     *
     * @param releaseVersionService release version service to add its served apis
     * @param serviceDto            service DTO with served apis
     */
    private void setServedApisToReleaseVersionService(final ReleaseVersionService releaseVersionService, final RVReleaseVersionServiceDTO serviceDto)
    {
        List<ApiImplementation<?, ?, ?>> servedSyncApiList = new ArrayList<>();
        if (serviceDto.getApisServed() != null)
        {
            for (RVApiDTO rvApiDto : serviceDto.getApisServed())
            {
                ApiVersion<?, ?, ?> apiVersion = this.apiVersionRepository.findById(rvApiDto.getId()).orElse(null);
                ApiImplementation<?, ?, ?> server = this.apiManagerServiceModalityBased.createApiImplementation(
                        apiVersion, releaseVersionService, ImplementedAs.SERVED);
                this.apiManagerServiceModalityBased.setConsumedApisByServedApi(
                        server,
                        this.utils.streamOfNullable(rvApiDto.getConsumedApis())
                                .map(RVApiConsumedBy::getId)
                                .collect(Collectors.toList())
                );
                this.apiManagerServiceModalityBased.setBackwardCompatibleVersionsOfServedApi(
                        server,
                        this.utils.listOfNullable(rvApiDto.getSupportedVersions())
                );
                servedSyncApiList.add(server);
                this.setImplementedApiState(apiVersion);
            }
        }
        releaseVersionService.setServers(servedSyncApiList);
    }

    /**
     * Create a non null list with NovaApiImplementation as consumed api and set into ReleaseVersionService
     *
     * @param releaseVersionService release version service to add its consumed apis
     * @param serviceDto            service DTO with served apis
     */
    private void setConsumedApisToReleaseVersionService(final ReleaseVersionService releaseVersionService, final RVReleaseVersionServiceDTO serviceDto)
    {
        List<ApiImplementation<?, ?, ?>> consumedSyncApiList = new ArrayList<>();

        if (serviceDto.getApisConsumed() != null)
        {
            for (RVApiDTO rvApiDto : serviceDto.getApisConsumed())
            {
                ApiVersion<?, ?, ?> consumedApiVersion = this.apiVersionRepository.findById(rvApiDto.getId()).orElse(null);
                ApiImplementation<?, ?, ?> consumer = this.apiManagerServiceModalityBased.createApiImplementation(
                        consumedApiVersion,
                        releaseVersionService,
                        rvApiDto.getExternal() ? ImplementedAs.EXTERNAL : ImplementedAs.CONSUMED);
                consumedSyncApiList.add(consumer);
                this.setImplementedApiState(consumedApiVersion);
            }
        }
        releaseVersionService.setConsumers(consumedSyncApiList);
    }

    private void setImplementedApiState(final ApiVersion<?, ?, ?> syncApiVersion)
    {
        if (syncApiVersion != null && ApiState.DEFINITION.equals(syncApiVersion.getApiState()))
        {
            syncApiVersion.setApiState(ApiState.IMPLEMENTED);
            this.apiVersionRepository.save(syncApiVersion);
        }
    }

    /**
     * Build a ReleaseVersionSubsystem by a RVVersionDTO
     *
     * @param release         relase of this version
     * @param versionDTO      DTO with data to create ReleaseVersion
     * @param releaseVersion  ReleaseVersion created
     * @param ivUser          user
     * @param isPlatformAdmin {@code true} if user has platform admin as role, {@code false} in other case
     * @return a non null list of ReleaseVersionSubsytem
     */
    private List<ReleaseVersionSubsystem> buildReleaseVersionSubsystem(Release release, RVVersionDTO versionDTO, ReleaseVersion releaseVersion, final String ivUser, final boolean isPlatformAdmin)
    {
        List<ReleaseVersionSubsystem> versionSubsystemList = new ArrayList<>();

        //set to detect repeted services into a MultiTag release version (only Library Subsystems)
        Set<String> serviceVersionSet = new HashSet<>();
        for (RVVersionSubsystemDTO versionSubsystemDTO : versionDTO.getSubsystems())
        {
            ReleaseVersionSubsystem versionSubsystem = this.buildReleaseVersionSubsystemFromDTO(versionSubsystemDTO, release.getName(), serviceVersionSet, ivUser, isPlatformAdmin);
            versionSubsystem.setReleaseVersion(releaseVersion);

            versionSubsystemList.add(versionSubsystem);
        }

        return versionSubsystemList;
    }


    /**
     * Method to check subsystems and services into RVVersionDTO
     * This method check
     * <li> there is almost one subsystem selected </li>
     * <li> subsystem are not repeted or is library type (which allows multitag) </li>
     * <li> services are not empty </li>
     * <li> service name is unique between different substems </li>
     *
     * @param versionDTO versionDto to validate
     * @since nova 9 krypton
     */
    private void validateReleaseVersionSubsystemAndServices(RVVersionDTO versionDTO)
    {
        if (versionDTO.getSubsystems() == null || versionDTO.getSubsystems().length == 0)
        {
            log.error("[{}] -> [{}]: Error adding subsystems, the release version [{}] with id [{}] does not have subsystems", Constants
                    .RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "addSubsystems", versionDTO.getVersionName(), versionDTO.getId());

            throw new NovaException(ReleaseVersionError.getReleaseVersionWithoutSubsystem(),
                    "Creating a new Release Version without subsystems! ");
        }

        //map of subsystems to check if there is multi-tag, it is because the subsystem has type as library
        final Map<String, RVSubsystemBaseDTO> subsystemDTOMap = new HashMap<>();
        final Map<String, String> subsystemByServiceName = new HashMap<>();

        for (RVVersionSubsystemDTO versionSubsystemDTO : versionDTO.getSubsystems())
        {
            //check if subsystem is only once or is library (multi-tag)
            ReleaseVersionEntityBuilderServiceImpl.validateVersionSubsystemUniqueOrLibrary(versionDTO.getVersionName(), versionSubsystemDTO.getSubsystem(), subsystemDTOMap);

            //service name has to be unique per subsystem
            ReleaseVersionEntityBuilderServiceImpl.validateUniqueServiceNames(versionDTO.getVersionName(), versionSubsystemDTO.getSubsystem().getSubsystemName(),
                    versionSubsystemDTO.getServices(), subsystemByServiceName);
        }
    }

    /**
     * Check if subsystem is unique or has library as type
     *
     * @param versionName  name of release version checked
     * @param subsystem    subsystem to check
     * @param subsystemMap collection of subsystems already checked
     */
    private static void validateVersionSubsystemUniqueOrLibrary(final String versionName, final RVSubsystemBaseDTO subsystem,
                                                             final Map<String, RVSubsystemBaseDTO> subsystemMap)
    {
        if (subsystemMap.containsKey(subsystem.getSubsystemName())
                && !StringUtils.equals(SubsystemType.LIBRARY.getType(), subsystem.getSubsystemType()))
        {
            throw new NovaException(ReleaseVersionError.getMultitagNotAllowedError(),
                    "Error adding release version [" + versionName +
                            "], subsystem [" + subsystem.getSubsystemName() + "] with type [" +
                            subsystem.getSubsystemType() + " not allowed to multi-tag");
        }

        //put anyway
        subsystemMap.put(subsystem.getSubsystemName(), subsystem);
    }

    /**
     *  Check if the release version has services uniques
     *
     * @param releaseVersionDTO to check
     * @deprecated
     * @since 04/06/2020
     */
    @Deprecated
    private static void checkUniqueServiceNames(final RVReleaseVersionDTO releaseVersionDTO)
    {
        final Map<String, String> subsystemByServiceName = new HashMap<>();

        for (RVReleaseVersionSubsystemDTO subsystemDTO : releaseVersionDTO.getSubsystems())
        {
            //service name has to be unique per subsystem
            validateUniqueServiceNames(releaseVersionDTO.getVersionName(), subsystemDTO.getProductSubsystemName(),
                    subsystemDTO.getServices(), subsystemByServiceName);
        }
    }

    /**
     * Check if a service is unique by its name inside a Release version or it is inside the same subsytem
     *
     * @param rvName                     Release Version name checking
     * @param subsystemName              subsystem name where subsystem is
     * @param services                   list of services included in the subsystem to check
     * @param subsystemNameByServiceName map with relation between services and his subystems
     */
    private static void validateUniqueServiceNames(final String rvName, final String subsystemName,
                                                   final RVReleaseVersionServiceDTO[] services, final Map<String, String> subsystemNameByServiceName)
    {
        if (services == null)
        {
            throw new NovaException(ReleaseVersionError.getSubsystemWithoutServicesError(),
                    "Invalid release version [" + rvName + "]: Subsystem [" + subsystemName + " has no services.");
        }

        for (RVReleaseVersionServiceDTO service : services)
        {
            //Service can have same name if subsystem is the same
            if (subsystemNameByServiceName.containsKey(service.getServiceName())
                    && !StringUtils.equals(subsystemName, subsystemNameByServiceName.get(service.getServiceName())))
            {
                throw new NovaException(ReleaseVersionError.getDuplicatedServiceNameError(),
                        "Error adding release version name [" + rvName + "]service names within a release version must be unique. [" +
                                service.getServiceName() + "] is duplicated in [" + subsystemName +
                                "] and [" + subsystemNameByServiceName.get(service.getServiceName()) + ']');
            }
            subsystemNameByServiceName.putIfAbsent(service.getServiceName(), subsystemName);
        }
    }

    /**
     * Return a non null list with RVReleaseVersionServiceDTO filtered.
     * A service was ignored if is already present into serviceVersionSet.
     * If service is present into the set it means there is a tag which include this same version service
     *
     * @param rvSubsystem       RVVersionSubsystem to validate and create its services
     * @param serviceVersionSet Set of services already present into ReleaseVersion
     * @return non null Array with RVReleaseVersionServiceDTO to be built
     */
    private static RVReleaseVersionServiceDTO[] getFilteredServicesToBuild(final RVVersionSubsystemDTO rvSubsystem, final Set<String> serviceVersionSet)
    {
        final RVReleaseVersionServiceDTO[] releaseVersionServiceDTOS = rvSubsystem.getServices();
        if (releaseVersionServiceDTOS == null)
        {
            log.warn("[{}] -> [{}]: Subsystem [{}] type [{}] without services. Please check ReleaseVersion!", Constants
                    .RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "getFilteredServicesToBuild", rvSubsystem.getSubsystem().getSubsystemName(), rvSubsystem.getSubsystem().getSubsystemType());
            return new RVReleaseVersionServiceDTO[0];
        }

        List<RVReleaseVersionServiceDTO> filtered = new ArrayList<>();
        for (RVReleaseVersionServiceDTO dto : releaseVersionServiceDTOS)
        {
            if (serviceVersionSet.contains(dto.getFinalName()))
            {
                log.debug("[{}] -> [{}]: Service [{}] from tag [{}] is already present, ignored.", Constants
                        .RELEASE_VERSION_ENTITY_BUILDER_SERVICE, "getFilteredServicesToBuild", dto.getFinalName(), rvSubsystem.getTag().getTagName());
            }
            else
            {
                filtered.add(dto);
                serviceVersionSet.add(dto.getFinalName());
            }
        }

        return filtered.toArray(RVReleaseVersionServiceDTO[]::new);

    }
}
