package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.*;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.platformservices.coreservice.releasesapi.exceptions.ReleaseError;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.enumerates.ValidationStatus;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.*;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.*;

/**
 * Release Version Service
 * Process all the logic of the Release Version API
 */
@Service
@Slf4j
public class ReleaseVersionServiceImpl implements IReleaseVersionService
{

    /**
     * Permission exception
     */
    private static final NovaException PERMISSION_DENIED =
            new NovaException(ReleaseVersionError.getForbiddenError(), ReleaseVersionError.getForbiddenError().toString());

    private static final String RELEASE_VERSION_NAME = "releaseVersionName";
    private static final String RELEASE_VERSION_DESCRIPTION = "releaseVersionDescription";
    private static final String JIRA_ISSUE_ID = "JiraIssueId";

    @Autowired
    private INewReleaseVersionDtoBuilder iNewReleaseVersionDtoBuilder;
    @Autowired
    private IReleaseVersionDtoBuilder iReleaseVersionDtoBuilder;
    @Autowired
    private ReleaseRepository releaseRepository;
    @Autowired
    private ReleaseValidator releaseValidator;
    @Autowired
    private IReleaseVersionValidator releaseVersionValidator;
    @Autowired
    private IReleaseVersionEntityBuilderService iReleaseVersionEntityBuilderService;
    @Autowired
    private IReleaseVersionDtoBuilderService iReleaseVersionDtoBuilderService;
    @Autowired
    private ISubsystemValidator iSubsystemValidator;
    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;
    @Autowired
    private ReleaseVersionSubsystemRepository releaseVersionSubsystemRepository;
    @Autowired
    private IErrorTaskManager errorTaskManager;
    @Autowired
    private IDeleteReleaseVersionService iDeleteReleaseVersionService;
    @Autowired
    private IArchiveReleaseVersionService iArchiveReleaseVersionService;
    @Autowired
    private IApiGatewayService apiGatewayServices;
    @Autowired
    private ILibraryManagerService iLibraryManagerService;
    @Autowired
    private ITagValidator tagValidator;
    @Autowired
    private ServiceDtoBuilderImpl serviceDtoBuilder;
    @Autowired
    private ReleaseVersionServiceRepository releaseVersionServiceRepository;

    /**
     * Client of the VCS API.
     */
    @Autowired
    private IVersioncontrolsystemClient iVersioncontrolsystemClient;

    /**
     * User service client
     */
    @Autowired
    private IProductUsersClient usersService;

    /**
     * Nova activities emitter
     */
    @Autowired
    private INovaActivityEmitter novaActivityEmitter;

    @Override
    public void createReleaseVersion(final RVVersionDTO versionDTO, final String ivUser, final Integer releaseId)
    {
        // Validate that given release version can be created
        Release release = this.validateReleaseVersionCreation(releaseId, ivUser, versionDTO.getVersionName());

        // [1] Create the new release version.
        ReleaseVersion releaseVersion = this.iReleaseVersionEntityBuilderService.buildReleaseVersionEntityFromDto(release, versionDTO, ivUser, this.usersService.isPlatformAdmin(ivUser));

        // [2] Call the Configuration Manager to process the template files.
        this.iReleaseVersionDtoBuilderService.processTemplates(releaseVersion, ivUser);

        // [3] Invoke the Library Manager to store the requirements if needed.
        this.iLibraryManagerService.storeNovaLibrariesRequirements(releaseVersion);

        // [4] Build the subsystem of the release version.
        this.iReleaseVersionDtoBuilderService.buildSubsystems(release.getProduct(), releaseVersion, ivUser);

        // Emit Add Release Version Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(release.getProduct().getId(), ActivityScope.RELEASE_VERSION, ActivityAction.CREATED)
                .entityId(releaseVersion.getId())
                .addParam(RELEASE_VERSION_NAME, releaseVersion.getVersionName())
                .addParam(RELEASE_VERSION_DESCRIPTION, releaseVersion.getDescription())
                .addParam(JIRA_ISSUE_ID, releaseVersion.getIssueID())
                .build());
    }

    @Override
    public void addReleaseVersion(final RVReleaseVersionDTO versionToAdd, final String ivUser, final Integer releaseId)
    {
        // Validate that release version can be added
        Release release = this.validateReleaseVersionCreation(releaseId, ivUser, versionToAdd.getVersionName());

        // [1] Create the new release version.
        ReleaseVersion releaseVersion = this.iReleaseVersionEntityBuilderService.buildReleaseVersionEntityFromDto(release, versionToAdd, ivUser, this.usersService.isPlatformAdmin(ivUser));

        // [2] Call the Configuration Manager to process the template files.
        this.iReleaseVersionDtoBuilderService.processTemplates(releaseVersion, ivUser);

        // [3] Invoke the Library Manager to store the requirements if needed.
        this.iLibraryManagerService.storeNovaLibrariesRequirements(releaseVersion);

        // [4] Build the subsystem of the release version.
        this.iReleaseVersionDtoBuilderService.buildSubsystems(release.getProduct(), releaseVersion, ivUser);
    }


    @Override
    public void subsystemBuildStatus(final String ivUser, final Integer subsystemId, final String jobName, final String jenkinsJobGroupMessageInfo, final String status)
    {
        if (status == null)
        {
            log.error("[{}] -> [{}]: Error! The subsystem status is NULL", Constants.RELEASE_VERSION_SERVICE, Constants.SUBSYSTEM_BUILD_STATUS);
            throw new NovaException(ReleaseVersionError.getNullSubsystemStatusError());
        }

        this.iReleaseVersionDtoBuilderService.subsystemBuildStatus(subsystemId, jenkinsJobGroupMessageInfo, status, ivUser);
    }

    @Override
    public void archiveReleaseVersion(String ivUser, Integer releaseVersionId)
    {
        // Check releaseVersion existence and get
        ReleaseVersion releaseVersion = this.releaseVersionRepository.findById(releaseVersionId).orElseThrow(() ->
        {
            throw new NovaException(ReleaseVersionError.getNoSuchReleaseVersionError(releaseVersionId));
        });

        // Check release existence and get
        Release release = releaseVersion.getRelease();
        this.releaseValidator.checkReleaseExistence(release);

        // Check product existence and get
        Product product = release.getProduct();
        this.releaseValidator.checkProductExistence(product);

        // Check user permissions
        this.usersService.checkHasPermission(ivUser, Constants.ARCHIVE_RELEASE_VERSION_PERMISSION, product.getId(), PERMISSION_DENIED);

        this.iArchiveReleaseVersionService.archiveReleaseVersion(ivUser, releaseVersionId);

        // Emit Archive Release Version Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(product.getId(), ActivityScope.RELEASE_VERSION, ActivityAction.ARCHIVED)
                .entityId(releaseVersionId)
                .addParam(RELEASE_VERSION_NAME, releaseVersion.getVersionName())
                .addParam(RELEASE_VERSION_DESCRIPTION, releaseVersion.getDescription())
                .addParam(JIRA_ISSUE_ID, releaseVersion.getIssueID())
                .build());
    }

    @Override
    @Transactional
    public RVReleaseVersionDTO getReleaseVersion(final String ivUser, final Integer releaseVersionId)
    {
        // Check releaseVersion existence and get
        ReleaseVersion releaseVersion = this.releaseVersionRepository.findById(releaseVersionId).orElseThrow(() ->
        {
            throw new NovaException(ReleaseVersionError.getNoSuchReleaseVersionError(releaseVersionId));
        });

        try
        {
            return this.iReleaseVersionDtoBuilder.build(releaseVersion);
        }
        catch (EntityNotFoundException e)
        {
            throw new NovaException(ReleaseVersionError.getNoSuchReleaseVersionError(releaseVersionId));
        }
    }

    @Override
    @Transactional
    public RVReleaseVersionDTO updateReleaseVersion(final String ivUser, final Integer releaseVersionId,
                                                    String description)
    {
        // Check releaseVersion existence and get
        ReleaseVersion releaseVersion = this.releaseVersionRepository.findById(releaseVersionId).orElseThrow(() ->
        {
            throw new NovaException(ReleaseVersionError.getNoSuchReleaseVersionError(releaseVersionId));
        });

        // Check release existence and get
        Release release = releaseVersion.getRelease();
        this.releaseValidator.checkReleaseExistence(release);

        // Check product existence and get
        Product product = release.getProduct();
        this.releaseValidator.checkProductExistence(product);

        this.usersService.checkHasPermission(ivUser, Constants.EDIT_RELEASE_VERSION_PERMISSION, product.getId(), PERMISSION_DENIED);

        // Update the description.
        releaseVersion.setDescription(description);

        // Persist the changes.
        this.releaseVersionRepository.save(releaseVersion);

        // Return the DTO.
        return this.iReleaseVersionDtoBuilder.build(releaseVersion);
    }

    @Override
    public synchronized void deleteReleaseVersion(String ivUser, Integer releaseVersionId)
    {
        // Check releaseVersion existence and get
        ReleaseVersion releaseVersion = this.releaseVersionRepository.findById(releaseVersionId).orElseThrow(() ->
        {
            throw new NovaException(ReleaseVersionError.getNoSuchReleaseVersionError(releaseVersionId));
        });

        // Check release existence and get
        this.releaseValidator.checkReleaseExistence(releaseVersion.getRelease());

        // Check product existence and get
        Product product = releaseVersion.getRelease().getProduct();
        this.releaseValidator.checkProductExistence(product);

        this.usersService.checkHasPermission(ivUser, Constants.DELETE_RELEASE_VERSION_PERMISSION, product.getId(), PERMISSION_DENIED);

        this.iDeleteReleaseVersionService.deleteReleaseVersion(ivUser, releaseVersionId);

        // Emit Delete Release Version Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(product.getId(), ActivityScope.RELEASE_VERSION, ActivityAction.DELETED)
                .entityId(releaseVersionId)
                .addParam(RELEASE_VERSION_NAME, releaseVersion.getVersionName())
                .addParam(RELEASE_VERSION_DESCRIPTION, releaseVersion.getDescription())
                .addParam(JIRA_ISSUE_ID, releaseVersion.getIssueID())
                .build());
    }

    @Override
    public void updateReleaseVersionIssue(String ivUser, Integer releaseVersionId, String issueId)
    {
        // Check releaseVersion existence and get
        ReleaseVersion releaseVersion = this.releaseVersionRepository.findById(releaseVersionId).orElseThrow(() ->
        {
            throw new NovaException(ReleaseVersionError.getNoSuchReleaseVersionError(releaseVersionId));
        });

        // Check release existence and get
        Release release = releaseVersion.getRelease();
        this.releaseValidator.checkReleaseExistence(release);

        // Check product existence and get
        Product product = release.getProduct();
        this.releaseValidator.checkProductExistence(product);

        // Check users permission
        this.usersService.checkHasPermission(ivUser, Constants.EDIT_RELEASE_VERSION_PERMISSION, product.getId(), PERMISSION_DENIED);

        // Issue ID. (JIRA)
        releaseVersion.setIssueID(issueId);

        // Persist the changes.
        this.releaseVersionRepository.save(releaseVersion);
    }

    @Override
    public RVRequestDTO releaseVersionRequest(final String ivUser, final Integer releaseId)
    {
        //Validate and get release
        Release release = this.releaseValidator.checkReleaseExistence(releaseId);

        // Check if release has subsystems - if not, version cannot be created.
        this.iSubsystemValidator.checkReleaseSubsystems(release);

        // Check if given user has permission to current action
        Product product = release.getProduct();
        this.releaseValidator.checkProductExistence(product);
        this.usersService.checkHasPermission(ivUser, Constants.CREATE_RELEASE_VERSION_PERMISSION, product.getId(), PERMISSION_DENIED);

        // Check number of existing release version
        this.releaseVersionValidator.checkMaxReleaseVersions(release.getProduct().getId(), product.getReleaseSlots());

        // Check if exist a current release version compiling
        this.releaseVersionValidator.checkCompilingReleaseVersions(release.getProduct().getId());

        //In case of success, build request object
        return this.iNewReleaseVersionDtoBuilder.build(product, release);
    }

    @Override
    public RVValidationResponseDTO validateAllTags(final String ivUser, final Integer releaseId, final RVValidationDTO validationDTO)
    {
        // First of all we validate if given subsystem support multi tag
        this.tagValidator.validateAllowedMultitag(validationDTO);

        RVValidationResponseDTO validationResponseDTO = new RVValidationResponseDTO();
        List<RVTagValidationDTO> tagValidationDTOList = new ArrayList<>();

        // This map contains all unique validated service (multitagmode)
        Map<String, RVTagValidationDTO> multitagValidationMap = new HashMap<>();

        for (RVTagDTO tagDTO : validationDTO.getTags())
        {
            //Validate current tag
            RVTagValidationDTO tagValidationDTO = this.validateTag(ivUser, releaseId, validationDTO, tagDTO);

            log.info("[ReleaseVersionServiceImpl] -> [validateAllTags]: After validateTags");

            //In case of multi tag, mark as warning duplicates or error when has the same version and exist differences on code. This happens when tagValidationDTO is with status OK
            if (this.isAllServiceWithValidationOK(tagValidationDTO))
            {
                for (RVServiceValidationDTO serviceValidationDTO : tagValidationDTO.getServiceValidation())
                {
                    //Get service by full name
                    String fullname = serviceValidationDTO.getService().getFinalName();
                    RVTagValidationDTO previousTag = multitagValidationMap.get(fullname);

                    if (previousTag != null)
                    {
                        //If exists in map, compare descriptor, nova yml
                        String folder = serviceValidationDTO.getService().getFolder();
                        Integer repoId = validationDTO.getSubsystem().getRepoId();

                        ValidatorInputs currentInputs = this.serviceDtoBuilder.buildValidationFileInputs(folder, repoId, tagDTO.getTagName());

                        // Get match on previous tag
                        RVServiceValidationDTO previousService = Arrays.stream(previousTag.getServiceValidation())
                                .filter(sv -> sv.getService().getFinalName().equals(fullname))
                                .findAny()
                                .orElse(new RVServiceValidationDTO());

                        ValidatorInputs previousInputs = this.serviceDtoBuilder.buildValidationFileInputs(previousService.getService().getFolder(), repoId, previousTag.getTag().getTagName());

                        // Validate if nova.yml is override
                        List<RVErrorDTO> rvErrorDTOList = new ArrayList<>();

                        if (currentInputs.getNovaYml() != null && !currentInputs.getNovaYml().equals(previousInputs.getNovaYml()))
                        {
                            RVErrorDTO novaYmlValidation = new RVErrorDTO();

                            novaYmlValidation.setStatus(ValidationStatus.ERROR.name());
                            novaYmlValidation.setCode(Constants.NOVA_YML_OVERRIDE);
                            novaYmlValidation.setMessage(Constants.NOVA_YML_OVERRIDE_MSG);

                            rvErrorDTOList.add(novaYmlValidation);
                        }

                        byte[] previousPom = this.iVersioncontrolsystemClient.getPomFromProject(previousService.getService().getFolder(), repoId, previousTag.getTag().getTagName());
                        byte[] currentPom = this.iVersioncontrolsystemClient.getPomFromProject(folder, repoId, tagDTO.getTagName());

                        if (!Arrays.equals(previousPom, currentPom))
                        {
                            RVErrorDTO pomValidation = new RVErrorDTO();

                            pomValidation.setStatus(ValidationStatus.ERROR.name());
                            pomValidation.setCode(Constants.POM_XML_OVERRIDE);
                            pomValidation.setMessage(Constants.POM_XML_OVERRIDE_MSG);

                            rvErrorDTOList.add(pomValidation);
                        }

                        //if there aren´t errors, write warning message
                        if (rvErrorDTOList.isEmpty())
                        {
                            RVErrorDTO warningDTO = new RVErrorDTO();

                            warningDTO.setStatus(ValidationStatus.WARNING.name());
                            warningDTO.setCode(Constants.SERVICE_VERSION_DUPLICATED);
                            warningDTO.setMessage(Constants.SERVICE_VERSION_DUPLICATED_MSG);

                            rvErrorDTOList.add(warningDTO);

                        }

                        List<RVErrorDTO> currentDTOList = new ArrayList<>(Arrays.asList(serviceValidationDTO.getError()));
                        currentDTOList.addAll(rvErrorDTOList);
                        serviceValidationDTO.setError(currentDTOList.toArray(new RVErrorDTO[0]));

                    }
                    else
                    {
                        //Add to map
                        multitagValidationMap.put(fullname, tagValidationDTO);
                    }

                }
            }

            tagValidationDTOList.add(tagValidationDTO);
        }

        //Clear multitag map
        multitagValidationMap.clear();

        validationResponseDTO.setTagValidation(tagValidationDTOList.toArray(new RVTagValidationDTO[0]));
        validationResponseDTO.setStatus(this.getValidationStatus(tagValidationDTOList));

        return validationResponseDTO;
    }

    @Override
    public String[] getStatuses()
    {
        return Arrays.stream(ReleaseVersionStatus.values()).map(Enum::toString).toArray(String[]::new);
    }

    /**
     * Validates a specific tag
     *
     * @param ivUser          bbva usercode
     * @param releaseId       release identifier
     * @param rvValidationDTO DTO with validation info
     * @param tagDTO          tag info
     * @return validated tag object
     */
    private RVTagValidationDTO validateTag(String ivUser, Integer releaseId, RVValidationDTO rvValidationDTO, RVTagDTO tagDTO)
    {
        RVTagValidationDTO rvTagValidationDTO = new RVTagValidationDTO();

        //Get projectnames
        List<String> projectNames = this.iVersioncontrolsystemClient.getProjectsPathsFromRepoTag(rvValidationDTO.getSubsystem().getRepoId(), tagDTO.getTagName());

        //Get DTO list with all services validated
        List<RVServiceValidationDTO> rvServiceValidationDTOList = this.tagValidator.buildTagValidationByService(ivUser, releaseId, tagDTO, rvValidationDTO.getSubsystem(), projectNames);

        rvTagValidationDTO.setTag(tagDTO);
        rvTagValidationDTO.setServiceValidation(rvServiceValidationDTOList.toArray(new RVServiceValidationDTO[0]));
        rvTagValidationDTO.setGeneral(this.tagValidator.buildTagValidation(tagDTO, rvValidationDTO.getSubsystem(), rvServiceValidationDTOList, projectNames));

        return rvTagValidationDTO;
    }

    @Override
    public NewReleaseVersionDto newReleaseVersion(final String ivUser, final Integer releaseId)
    {
        // Get release and product.
        Release release = this.releaseRepository.findById(releaseId)
                .orElseThrow(() -> new NovaException(ReleaseError.getNoSuchReleaseError(), "[ReleasesAPI] -> [checkReleaseExistence]: the release id: [" + releaseId + "] does not exists into NOVA BBDD"));

        // Check if the Release does exist.
        this.releaseValidator.checkReleaseExistence(release);

        // Check if release product has subsystems - if not, version cannot be created.
        this.iSubsystemValidator.checkReleaseSubsystems(release);

        //check permissions
        Product product = release.getProduct();
        this.releaseValidator.checkProductExistence(product);
        this.usersService.checkHasPermission(ivUser, Constants.CREATE_RELEASE_VERSION_PERMISSION, product.getId(), PERMISSION_DENIED);

        // Check number of release versions
        this.releaseVersionValidator.checkMaxReleaseVersions(release.getProduct().getId(), product.getReleaseSlots());
        this.releaseVersionValidator.checkCompilingReleaseVersions(release.getProduct().getId());

        return this.iNewReleaseVersionDtoBuilder.build(release, product, ivUser);
    }

    @Override
    public RVReleaseVersionSubsystemDTO[] getReleaseVersionSubsystems(Integer subystemId)
    {
        List<ReleaseVersionSubsystem> releaseVersionSubsystemList = this.releaseVersionSubsystemRepository.findBySubsystemId(subystemId);
        List<RVReleaseVersionSubsystemDTO> releaseVersionSubsystemDtoList = new ArrayList<>();

        for (ReleaseVersionSubsystem releaseVersionSubsystem : releaseVersionSubsystemList)
        {
            RVReleaseVersionSubsystemDTO releaseVersionSubsystemDto = new RVReleaseVersionSubsystemDTO();

            BeanUtils.copyProperties(releaseVersionSubsystem, releaseVersionSubsystemDto);

            releaseVersionSubsystemDtoList.add(releaseVersionSubsystemDto);
        }

        return releaseVersionSubsystemDtoList.toArray(new RVReleaseVersionSubsystemDTO[0]);
    }


    private String getValidationStatus(List<RVTagValidationDTO> tagValidationDTOList)
    {
        String status = "";
        if (this.isAnyServiceWithValidationError(tagValidationDTOList))
        {
            status = ValidationStatus.ERROR.name();
        }
        else if (this.isAnyServiceWithWarningError(tagValidationDTOList))
        {
            status = ValidationStatus.WARNING.name();
        }
        else
        {
            status = ValidationStatus.OK.name();
        }

        return status;
    }

    private boolean isAnyServiceWithValidationError(List<RVTagValidationDTO> tagValidationDTOList)
    {
        return tagValidationDTOList.stream()
                .flatMap(rvTagValidationDTO -> Arrays.stream(rvTagValidationDTO.getServiceValidation()))
                .flatMap(rvServiceValidationDTO -> Arrays.stream(rvServiceValidationDTO.getError()))
                .anyMatch(rvErrorDTO -> rvErrorDTO.getStatus().equals(ValidationStatus.ERROR.name()));
    }

    private boolean isAnyServiceWithWarningError(List<RVTagValidationDTO> tagValidationDTOList)
    {
        return tagValidationDTOList.stream()
                .flatMap(rvTagValidationDTO -> Arrays.stream(rvTagValidationDTO.getServiceValidation()))
                .flatMap(rvServiceValidationDTO -> Arrays.stream(rvServiceValidationDTO.getError()))
                .anyMatch(rvErrorDTO -> rvErrorDTO.getStatus().equals(ValidationStatus.WARNING.name()));
    }

    private boolean isAllServiceWithValidationOK(RVTagValidationDTO tagValidationDTO)
    {
        return Arrays.stream(tagValidationDTO.getServiceValidation())
                .flatMap(service -> Arrays.stream(service.getError()))
                .allMatch(rvErrorDTO -> rvErrorDTO.getStatus().equals(ValidationStatus.OK.name()));
    }

    /**
     * Validate that given release version can be created
     *
     * @param releaseId   release identifier
     * @param ivUser      BBVA user code
     * @param versionName release version name
     * @return release
     */
    private Release validateReleaseVersionCreation(Integer releaseId, String ivUser, String versionName)
    {
        // Check if the Release does exist.
        Release release = this.releaseRepository.findById(releaseId)
                .orElseThrow(() -> new NovaException(ReleaseError.getNoSuchReleaseError(), "[ReleasesAPI] -> [checkReleaseExistence]: the release id: [" + releaseId + "] does not exists into NOVA BBDD"));

        this.releaseValidator.checkReleaseExistence(release);

        // Check if the product exists
        Product product = release.getProduct();
        this.releaseValidator.checkProductExistence(product);
        Integer productId = product.getId();

        //check Permissions
        this.usersService.checkHasPermission(ivUser, Constants.CREATE_RELEASE_VERSION_PERMISSION, productId, PERMISSION_DENIED);

        // Check if there is a release version with the same name.
        this.releaseVersionValidator.existsReleaseVersionWithSameName(release, versionName);

        // Check if release product has subsystems - if not, version cannot be created.
        this.iSubsystemValidator.checkReleaseSubsystems(release);

        // Check number of release versions
        this.releaseVersionValidator.checkMaxReleaseVersions(release.getProduct().getId(), product.getReleaseSlots());
        this.releaseVersionValidator.checkCompilingReleaseVersions(release.getProduct().getId());

        return release;
    }

}
