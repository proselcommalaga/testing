package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVApiDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVBehaviorVersionDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVSubsystemDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.util.ServiceNamingUtils;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiState;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorService;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorSubsystem;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorTag;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.behavior.enumerates.BehaviorVersionStatus;
import com.bbva.enoa.datamodel.model.common.entities.LobFile;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.internal.IApiManagerServiceModalityBased;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.exceptions.BehaviorError;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorVersionEntityBuilderService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ApiVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDockerRegistryClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
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
 * Build a new {@link BehaviorVersion} entity.
 */
@Service
@Slf4j
@AllArgsConstructor
public class BehaviorVersionEntityBuilderServiceImpl implements IBehaviorVersionEntityBuilderService
{
    private static final String POM_FILENAME = "pom.xml";
    private final IToolsClient toolsService;
    private final ApiVersionRepository apiVersionRepository;
    private final EntityManager entityManager;
    private final IVersioncontrolsystemClient vcsClient;
    private final IDockerRegistryClient dockerRegistryClient;
    private final IApiManagerServiceModalityBased apiManagerServiceModalityBased;
    private final ReleaseVersionRepository releaseVersionRepository;


    @Override
    @Transactional
    public BehaviorVersion buildBehaviorVersionEntityFromDto(Product product, BVBehaviorVersionDTO behaviorVersionDto, final String ivUser, final boolean isIvUserPlatformAdmin)
    {
        // Add subsystems from dto
        this.validateBehaviorVersionSubsystemAndServices(behaviorVersionDto);

        // Create the new behavior version with basic information
        BehaviorVersion behaviorVersion = BehaviorVersionEntityBuilderServiceImpl.buildBasicBehaviorVersion(behaviorVersionDto.getDescription(), behaviorVersionDto.getVersionName());

        // Add behavior version subsystems to behavior version
        List<BehaviorSubsystem> behaviorSubsystems = this.buildBehaviorVersionSubsystem(behaviorVersionDto, behaviorVersion, product, ivUser, isIvUserPlatformAdmin);
        behaviorVersion.setSubsystems(behaviorSubsystems);

        behaviorVersion.setStatus(BehaviorVersionStatus.ERRORS);

        // Add the Behavior version to the product
        behaviorVersion.setProduct(product);
        product.getBehaviorVersions().add(behaviorVersion);

        // Persist data
        this.entityManager.flush();
        return behaviorVersion;
    }

    @Override
    @Transactional
    public BehaviorSubsystem buildBehaviorVersionSubsystemFromDTO(BVSubsystemDTO bvSubsystemDTO, final Set<String> serviceVersionSet, Product product,
                                                                  final String versionName, final String ivUser, final boolean isIvUserPlatformAdmin)
    {
        BehaviorSubsystem behaviorSubsystem = new BehaviorSubsystem();

        // Copy all properties shared by DTO and entity.
        BeanUtils.copyProperties(bvSubsystemDTO, behaviorSubsystem);

        behaviorSubsystem.setTagName(bvSubsystemDTO.getTag().getTagName());
        behaviorSubsystem.setTagUrl(bvSubsystemDTO.getTag().getTagUrl());

        Integer subsystemId = bvSubsystemDTO.getSubsystem().getId();

        BehaviorVersionEntityBuilderServiceImpl.validateSubsystemId(subsystemId);

        TOSubsystemDTO subsystem = this.toolsService.getSubsystemById(subsystemId);
        this.checkProductSubsystem(subsystem);
        behaviorSubsystem.setSubsystemId(subsystemId);

        // Services
        BVServiceDTO[] dtoServices = BehaviorVersionEntityBuilderServiceImpl.getFilteredServicesToBuild(bvSubsystemDTO, serviceVersionSet);
        this.buildServicesNotNull(behaviorSubsystem, dtoServices, product, versionName, ivUser, isIvUserPlatformAdmin);

        return behaviorSubsystem;
    }


    ////////////////////////////////////////  PRIVATE  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Method to check subsystems and services into BVBehaviorVersionDTO
     * This method check
     * <li> there is almost one subsystem selected </li>
     * <li> subsystem are not repeated</li>
     * <li> services are not empty </li>
     * <li> service name is unique between different subsystems </li>
     *
     * @param behaviorVersionDto versionDto to validate
     */
    private void validateBehaviorVersionSubsystemAndServices(BVBehaviorVersionDTO behaviorVersionDto)
    {
        if (behaviorVersionDto.getSubsystems() == null || behaviorVersionDto.getSubsystems().length == 0)
        {
            log.error("[{}] -> [{}]: Error adding subsystems, the behavior version [{}] with id [{}] does not have subsystems",
                    "BehaviorVersionEntityBuilderServiceImpl", "validateBehaviorVersionSubsystemAndServices", behaviorVersionDto.getVersionName(), behaviorVersionDto.getId());

            throw new NovaException(BehaviorError.getBehaviorVersionWithoutSubsystem(),
                    "Creating a new Behavior Version without subsystems! ");
        }

        final Map<String, String> subsystemByServiceName = new HashMap<>();

        for (BVSubsystemDTO bvSubsystemDTO : behaviorVersionDto.getSubsystems())
        {
            //service name has to be unique per subsystem
            BehaviorVersionEntityBuilderServiceImpl.validateUniqueServiceNames(behaviorVersionDto.getVersionName(), bvSubsystemDTO.getSubsystem().getSubsystemName(),
                    bvSubsystemDTO.getServices(), subsystemByServiceName);
        }
    }

    /**
     * Check if a service is unique by its name inside a behavior version, or it is inside the same subsystem
     *
     * @param bvName                     Behavior Version name checking
     * @param subsystemName              subsystem name where subsystem is
     * @param services                   list of services included in the subsystem to check
     * @param subsystemNameByServiceName map with relation between services and his subsystem
     */
    private static void validateUniqueServiceNames(final String bvName, final String subsystemName,
                                                   final BVServiceDTO[] services, final Map<String, String> subsystemNameByServiceName)
    {
        if (services == null)
        {
            throw new NovaException(BehaviorError.getSubsystemWithoutServicesError(),
                    "Invalid behavior version [" + bvName + "]: Subsystem [" + subsystemName + " has no services.");
        }

        for (BVServiceDTO service : services)
        {
            //Service can have same name if subsystem is the same
            if (subsystemNameByServiceName.containsKey(service.getServiceName())
                    && !StringUtils.equals(subsystemName, subsystemNameByServiceName.get(service.getServiceName())))
            {
                throw new NovaException(BehaviorError.getDuplicatedServiceNameError(),
                        "Error adding behavior version name [" + bvName + "]service names within a behavior version must be unique. [" +
                                service.getServiceName() + "] is duplicated in [" + subsystemName +
                                "] and [" + subsystemNameByServiceName.get(service.getServiceName()) + ']');
            }
            subsystemNameByServiceName.putIfAbsent(service.getServiceName(), subsystemName);
        }
    }

    /**
     * Create behavior version object with basic info
     *
     * @param description behavior version description
     * @param versionName behavior version name
     * @return new behavior version
     */
    private static BehaviorVersion buildBasicBehaviorVersion(final String description, final String versionName)
    {
        BehaviorVersion behaviorVersion = new BehaviorVersion();

        behaviorVersion.setDescription(description);
        behaviorVersion.setVersionName(versionName);

        return behaviorVersion;
    }

    /**
     * Build a BehaviorSubsystem by a BVBehaviorVersionDTO
     *
     * @param behaviorVersionDto DTO with data to create BehaviorVersion
     * @param behaviorVersion    BehaviorVersion created
     * @param ivUser             user
     * @param isPlatformAdmin    {@code true} if user has platform admin as role, {@code false} in other case
     * @return a non-null list of BehaviorSubsystems
     */
    private List<BehaviorSubsystem> buildBehaviorVersionSubsystem(BVBehaviorVersionDTO behaviorVersionDto, BehaviorVersion behaviorVersion, Product product, final String ivUser, final boolean isPlatformAdmin)
    {
        List<BehaviorSubsystem> versionSubsystemList = new ArrayList<>();

        Set<String> serviceVersionSet = new HashSet<>();
        for (BVSubsystemDTO bvSubsystemDTO : behaviorVersionDto.getSubsystems())
        {
            BehaviorSubsystem versionSubsystem = this.buildBehaviorVersionSubsystemFromDTO(bvSubsystemDTO, serviceVersionSet, product, behaviorVersion.getVersionName(), ivUser, isPlatformAdmin);
            versionSubsystem.setBehaviorVersion(behaviorVersion);

            versionSubsystemList.add(versionSubsystem);
        }

        return versionSubsystemList;
    }


    /**
     * Return a non-null list with bvSubsystemDTO filtered.
     * A service was ignored if is already present into serviceVersionSet.
     * If service is present into the set it means there is a tag which include this same version service
     *
     * @param bvSubsystemDTO    BVSubsystemDTO to validate and create its services
     * @param serviceVersionSet Set of services already present into BehaviorVersion
     * @return non-null Array with BVServiceDTO to be built
     */
    private static BVServiceDTO[] getFilteredServicesToBuild(final BVSubsystemDTO bvSubsystemDTO, final Set<String> serviceVersionSet)
    {
        final BVServiceDTO[] bvServiceDTOS = bvSubsystemDTO.getServices();
        if (bvServiceDTOS == null)
        {
            log.warn("[{}] -> [{}]: Subsystem [{}] type [{}] without services. Please check behaviorVersion!",
                    "BehaviorVersionEntityBuilderServiceImpl", "getFilteredServicesToBuild", bvSubsystemDTO.getSubsystem().getSubsystemName(), bvSubsystemDTO.getSubsystem().getSubsystemType());
            return new BVServiceDTO[0];
        }

        List<BVServiceDTO> filtered = new ArrayList<>();
        for (BVServiceDTO dto : bvServiceDTOS)
        {
            if (serviceVersionSet.contains(dto.getFinalName()))
            {
                log.debug("[{}] -> [{}]: Service [{}] from tag [{}] is already present, ignored.",
                        "BehaviorVersionEntityBuilderServiceImpl", "getFilteredServicesToBuild", dto.getFinalName(), bvSubsystemDTO.getTag().getTagName());
            }
            else
            {
                filtered.add(dto);
                serviceVersionSet.add(dto.getFinalName());
            }
        }

        return filtered.toArray(BVServiceDTO[]::new);

    }

    /**
     * Create services for the behavior version subsystem info if the service list contain something
     *
     * @param behaviorSubsystem     behavior version subsystem info
     * @param dtoServices           List of the service dto
     * @param versionName           name of behavior version
     * @param ivUser                the user requester
     * @param isIvUserPlatformAdmin said if the user requester is platform admin
     */
    private void buildServicesNotNull(final BehaviorSubsystem behaviorSubsystem, final BVServiceDTO[] dtoServices, final Product product, final String versionName,
                                      final String ivUser, final boolean isIvUserPlatformAdmin)
    {
        if (dtoServices == null || dtoServices.length == 0)
        {
            log.warn("[{}] -> [{}]: Subsystem [{}] of behavior Version [{}] is going to be ignored because does not have services. Check if services has been filtered in previous steps.",
                    "BehaviorVersionEntityBuilderServiceImpl", "buildServicesNotNull",
                    behaviorSubsystem.getSubsystemId(), versionName);
        }
        else
        {
            // Create services
            this.buildServices(behaviorSubsystem, product, versionName, dtoServices, ivUser, isIvUserPlatformAdmin);
        }
    }

    /**
     * Check if all the mandatory fields of the service Dto are completed
     *
     * @param serviceDto          the service dto to check
     * @param behaviorVersionName the behavior version name that belongs the service dto
     * @param subsystemTagName    the subsystem tag name that belongs the service dto
     */
    private static void validateServiceDto(final BVServiceDTO serviceDto, final String behaviorVersionName, final String subsystemTagName)
    {
        // Validate service dto
        if (Strings.isNullOrEmpty(serviceDto.getGroupId()))
        {
            log.error("[{}] -> [{}]: Error checking group id of the service dto: [{}] trying to create a new behavior version: [{}] of the subsystem TAG name: [{}]. Is null or empty.",
                    "BehaviorVersionEntityBuilderServiceImpl", "validateServiceDto", serviceDto.getServiceName(), behaviorVersionName, subsystemTagName);
            throw new NovaException(BehaviorError.getGroupIdNotFoundError(), "Trying to create a behavior version with a wrong/not found group id of the service");
        }

        if (Strings.isNullOrEmpty(serviceDto.getArtifactId()))
        {
            log.error("[{}] -> [{}]: Error checking artifact id of the service dto: [{}] trying to create a new behavior version: [{}] of the subsystem TAG name: [{}]. Is null or empty.",
                    "BehaviorVersionEntityBuilderServiceImpl", "validateServiceDto", serviceDto.getServiceName(), behaviorVersionName, subsystemTagName);
            throw new NovaException(BehaviorError.getArtifactIdNotFoundError(), "Trying to create a behavior version with a wrong/not found artifact id of the service");
        }

        if (Strings.isNullOrEmpty(serviceDto.getVersion()))
        {
            log.error("[{}] -> [{}]: Error checking version of the service dto: [{}] trying to create a new behavior version: [{}] of the subsystem TAG name: [{}]. Is null or empty.",
                    "BehaviorVersionEntityBuilderServiceImpl", "validateServiceDto", serviceDto.getServiceName(), behaviorVersionName, subsystemTagName);
            throw new NovaException(BehaviorError.getVersionNotFoundError(), "Trying to create a behavior version with a wrong/not found version of the service");
        }

        if (Strings.isNullOrEmpty(behaviorVersionName))
        {
            log.error("[{}] -> [{}]: Error checking behavior version name of the service dto: [{}] trying to create a new behavior version: [{}] of the subsystem TAG name: [{}]. Is null or empty.",
                    "BehaviorVersionEntityBuilderServiceImpl", "validateServiceDto", serviceDto.getServiceName(), behaviorVersionName, subsystemTagName);
            throw new NovaException(BehaviorError.getVersionNameNotFoundError(), "Trying to create a behavior version name with a wrong/not found name of the service");
        }

        if (Strings.isNullOrEmpty(serviceDto.getServiceName()))
        {
            log.error("[{}] -> [{}]: Error checking service name of the service dto: [{}] trying to create a new behavior version: [{}] of the subsystem TAG name: [{}]. Is null or empty.",
                    "BehaviorVersionEntityBuilderServiceImpl", "validateServiceDto", serviceDto.getServiceName(), behaviorVersionName, subsystemTagName);
            throw new NovaException(BehaviorError.getServiceNameNotFoundError(), "Trying to create a behavior version with a wrong/not found service name of the service");
        }

        if (Strings.isNullOrEmpty(serviceDto.getServiceType()))
        {
            log.error("[{}] -> [{}]: Error checking service type of the service dto: [{}] trying to create a new behavior version: [{}] of the subsystem TAG name: [{}]. Is null or empty.",
                    "BehaviorVersionEntityBuilderServiceImpl", "validateServiceDto", serviceDto.getServiceName(), behaviorVersionName, subsystemTagName);
            throw new NovaException(BehaviorError.getServiceTypeNotFoundError(), "Trying to create a behavior version with a wrong/not found service type of the service");
        }
    }

    /**
     * Create the services and add them to the subsystem
     *
     * @param behaviorSubsystem     behavior version subsystem info
     * @param versionName           name of the behavior version
     * @param bvServiceDTOS         List of the service dto
     * @param ivUser                the user requester
     * @param isIvUserPlatformAdmin said if the user requester is platform admin
     */
    private void buildServices(BehaviorSubsystem behaviorSubsystem, final Product product, final String versionName, final BVServiceDTO[] bvServiceDTOS, final String ivUser, final boolean isIvUserPlatformAdmin)
    {
        // Services of the subsystem
        List<BehaviorService> services = behaviorSubsystem.getServices();

        for (BVServiceDTO serviceDto : bvServiceDTOS)
        {
            // Check the service Dto
            BehaviorVersionEntityBuilderServiceImpl.validateServiceDto(serviceDto, versionName, behaviorSubsystem.getTagName());

            // Create the BVServiceDTO
            BehaviorService bvService = new BehaviorService();

            // Get the service type and set into behavior version service
            ServiceType serviceType = ServiceType.valueOf(serviceDto.getServiceType());
            bvService.setServiceType(serviceType.getServiceType());

            bvService.setBehaviorSubsystem(behaviorSubsystem);
            bvService.setServiceName(serviceDto.getServiceName());

            // Generate service image name
            String serviceImageName = ServiceNamingUtils.getBehaviorImageName(serviceDto.getGroupId(), serviceDto.getArtifactId(), versionName, serviceDto.getVersion());
            bvService.setImageName(serviceImageName);

            // Check if the iv user is platform admin for knowing if compiling or not the service
            if (isIvUserPlatformAdmin)
            {
                // If user is platform admin role, set the value established from platform admin user (directly from nova dashboard form)
                bvService.setForceCompilation(serviceDto.getHasForcedCompilation());
            }
            else
            {
                // For any other users, if the image is already in Registry, this service will not be compiled. If the image does not found, service will be compiled
                bvService.setForceCompilation(!this.dockerRegistryClient.isImageInRegistry(ivUser, serviceImageName) || serviceDto.getHasForcedCompilation());
            }

            // If there is a release version name we get the entity and set it
            if (serviceDto.getReleaseVersion() != null && !serviceDto.getReleaseVersion().isEmpty())
            {
                ReleaseVersion releaseVersion = this.releaseVersionRepository.findByProductIdAndVersionName(product.getId(), serviceDto.getReleaseVersion());
                bvService.setReleaseVersion(releaseVersion);
            }

            // Set Behavior tags
            if (serviceDto.getReleaseVersion() != null && !serviceDto.getTags().isEmpty())
            {
                List<String> tags = new ArrayList<>(Arrays.asList(serviceDto.getTags().split(",")));
                List<BehaviorTag> behaviorTagList = tags.stream().map(BehaviorTag::new).collect(Collectors.toList());
                bvService.setBehaviorTagList(behaviorTagList);
            }

            bvService.setGroupId(serviceDto.getGroupId());
            bvService.setFinalName(serviceDto.getFinalName());
            bvService.setArtifactId(serviceDto.getArtifactId());
            bvService.setVersion(serviceDto.getVersion());
            bvService.setNovaVersion(serviceDto.getNovaVersion());
            bvService.setFolder(serviceDto.getFolder());
            bvService.setLanguageVersion(serviceDto.getJvmVersion());
            bvService.setJdkVersion(serviceDto.getJdkVersion());
            bvService.setTestFramework(serviceDto.getTestFramework());
            bvService.setTestFrameworkVersion(serviceDto.getTestFrameworkVersion());
            bvService.setDescription(serviceDto.getDescription());

            // Include consumed apis
            this.setConsumedApisToBehaviorVersionService(bvService, serviceDto);

            // Supports multiple filesystems always true
            TOSubsystemDTO subsystemDTO = this.toolsService.getSubsystemById(behaviorSubsystem.getSubsystemId());
            switch (serviceType)
            {
                case BUSINESS_LOGIC_BEHAVIOR_TEST_JAVA:
                    LobFile novaYmlContent = this.getLobFileFromVcs(
                            subsystemDTO.getRepoId(),
                            behaviorSubsystem.getTagName(),
                            Constants.NOVA_YML,
                            Constants.NOVA_YML,
                            behaviorSubsystem.getTagUrl(),
                            bvService.getFolder());
                    if (novaYmlContent != null)
                    {
                        bvService.setNovaYml(novaYmlContent);
                    }
                    bvService.setProjectDefinitionFile(
                            this.getLobFileFromVcs(
                                    subsystemDTO.getRepoId(),
                                    behaviorSubsystem.getTagName(),
                                    POM_FILENAME,
                                    POM_FILENAME,
                                    behaviorSubsystem.getTagUrl(),
                                    bvService.getFolder())
                    );
                    break;
                default:
                    bvService.setProjectDefinitionFile(
                            this.getLobFileFromVcs(
                                    subsystemDTO.getRepoId(),
                                    behaviorSubsystem.getTagName(),
                                    POM_FILENAME,
                                    POM_FILENAME,
                                    behaviorSubsystem.getTagUrl(),
                                    bvService.getFolder()));
            }
            // Finally, add the service to the list.
            services.add(bvService);
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
                    "BehaviorVersionEntityBuilderServiceImpl", "checkProductSubsystem");
            throw new NovaException(BehaviorError.getProductSubsystemNotFoundError(),
                    "Missed product subsystem in creation of a behavior version");
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
            log.error("[{}] -> [{}]: Error checking product subsystem id, NULL subsystem id found", "BehaviorVersionEntityBuilderServiceImpl", "checkSubsystemId");
            throw new NovaException(BehaviorError.getNullProductSubsystemIdError(), "Creating a behavior version with a wrong product subsystem id");
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
     * Create a non-null list with NovaApiImplementation as consumed api and set into BehaviorService
     *
     * @param bvService  behavior version service to add its consumed apis
     * @param serviceDto service DTO with served apis
     */
    private void setConsumedApisToBehaviorVersionService(final BehaviorService bvService, final BVServiceDTO serviceDto)
    {
        List<ApiImplementation<?, ?, ?>> consumedSyncApiList = new ArrayList<>();

        if (serviceDto.getApisConsumed() != null)
        {
            for (BVApiDTO bvApiDto : serviceDto.getApisConsumed())
            {
                ApiVersion<?, ?, ?> consumedApiVersion = this.apiVersionRepository.findById(bvApiDto.getId()).orElse(null);
                ApiImplementation<?, ?, ?> consumer = this.apiManagerServiceModalityBased.createBehaviorApiImplementation(
                        consumedApiVersion,
                        bvService,
                        bvApiDto.getExternal() ? ImplementedAs.EXTERNAL : ImplementedAs.CONSUMED);
                consumedSyncApiList.add(consumer);
                this.setImplementedApiState(consumedApiVersion);
            }
        }
        bvService.setConsumers(consumedSyncApiList);
    }

    private void setImplementedApiState(final ApiVersion<?, ?, ?> syncApiVersion)
    {
        if (syncApiVersion != null && ApiState.DEFINITION.equals(syncApiVersion.getApiState()))
        {
            syncApiVersion.setApiState(ApiState.IMPLEMENTED);
            this.apiVersionRepository.save(syncApiVersion);
        }
    }

}
