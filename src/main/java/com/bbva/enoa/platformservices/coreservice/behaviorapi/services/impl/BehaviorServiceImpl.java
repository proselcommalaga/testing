package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.*;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBCheckExecutionInfo;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.datamodel.model.behavior.entities.*;
import com.bbva.enoa.datamodel.model.behavior.enumerates.BehaviorConfigurationStatus;
import com.bbva.enoa.datamodel.model.behavior.enumerates.BehaviorVersionStatus;
import com.bbva.enoa.datamodel.model.common.enumerates.AsyncStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.exceptions.BehaviorError;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.*;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductBudgetsClientImpl;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.interfaces.IQualityManagerService;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util.QualityConstants;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.enumerates.ValidationStatus;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Behavior Version Service
 * Process all the logic of the Behavior API
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BehaviorServiceImpl implements IBehaviorService
{
    /**
     * Permission exception
     */
    private static final NovaException PERMISSION_DENIED =
            new NovaException(ReleaseVersionError.getForbiddenError(), ReleaseVersionError.getForbiddenError().toString());
    private static final String BEHAVIOR_VERSION_NAME = "behaviorVersionName";
    private static final String BEHAVIOR_VERSION_DESCRIPTION = "behaviorVersionDescription";

    /**
     * Product repository
     */
    private final ProductRepository productRepository;

    /**
     * User service client
     */
    private final IProductUsersClient usersClient;

    /**
     * Behavior version Validator
     */
    private final IBehaviorVersionValidator behaviorVersionValidator;

    /**
     * Behavior version DTO builder
     */
    private final IBehaviorVersionDtoBuilder behaviorVersionDtoBuilder;

    /**
     * Behavior service repository
     */
    private final BehaviorServiceRepository behaviorServiceRepository;

    /**
     * Client of the VCS API.
     */
    private final IVersioncontrolsystemClient versionControlSystemClient;

    /**
     * Behavior tag Validator
     */
    private final IBehaviorTagValidator behaviorTagValidator;

    /**
     * Behavior version entity builder
     */
    private final IBehaviorVersionEntityBuilderService behaviorVersionEntityBuilderService;

    /**
     * Behavior version DTO builder
     */
    private final IBehaviorVersionDtoBuilderService iBehaviorVersionDtoBuilderService;

    /**
     * Nova activity emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    /**
     * Behavior subsystem repository.
     */
    private final BehaviorSubsystemRepository behaviorSubsystemRepository;

    /**
     * Behavior version repository.
     */
    private final BehaviorVersionRepository behaviorVersionRepository;

    /**
     * behavior service configuration repository
     */
    private final BehaviorServiceConfigurationRepository behaviorServiceConfigurationRepository;

    /**
     * Product budgets client
     */
    private final IProductBudgetsClientImpl productBudgetsClient;

    /**
     * Tools client
     */
    private final IToolsClient toolsClient;

    /**
     * Quality Manager Service
     */
    private final IQualityManagerService qualityManagerService;

    /**
     * behavior instance repository
     */
    private final BehaviorInstanceRepository behaviorInstanceRepository;


    @Override
    public BVRequestDTO newBehaviorVersionRequest(final String ivUser, final Integer productId)
    {
        // Check if given user has permission to current action
        Product product = productRepository.fetchById(productId);
        this.behaviorVersionValidator.checkProductExistence(product);
        this.usersClient.checkHasPermission(ivUser, Constants.CREATE_BEHAVIOR_PERMISSION, product.getId(), PERMISSION_DENIED);

        // Check if product has subsystems - if not, version cannot be created.
        this.behaviorVersionValidator.checkProductSubsystems(product.getId());

        // Check number of existing behavior version
        this.behaviorVersionValidator.checkMaxBehaviorVersions(product.getId(), product.getReleaseSlots());

        // Check if exist a current behavior version compiling
        this.behaviorVersionValidator.checkCompilingBehaviorVersions(product.getId());

        // Build request object
        return this.behaviorVersionDtoBuilder.build(product);
    }

    @Override
    public BVValidationResponseDTO validateRequestTag(final String ivUser, final Integer productId, final BVSubsystemTagDTO subsystemTagRequest)
    {
        BVValidationResponseDTO validationResponseDTO = new BVValidationResponseDTO();

        BVTagResponseDTO tagValidationResponse = this.validateTag(ivUser, productId, subsystemTagRequest);

        BVTagResponseDTO[] tag = new BVTagResponseDTO[1];
        tag[0] = tagValidationResponse;
        validationResponseDTO.setTagResponse(tag);
        validationResponseDTO.setStatus(this.getValidationStatus(tagValidationResponse));

        return validationResponseDTO;
    }

    @Override
    public void buildBehaviorVersion(final BVBehaviorVersionDTO behaviorVersionDto, final String ivUser, final Integer productId)
    {
        // Validate that given behavior version can be created
        Product product = this.validateBehaviorVersionCreation(ivUser, behaviorVersionDto.getVersionName(), productId);

        // [1] Create the new behavior version.
        BehaviorVersion behaviorVersion = behaviorVersionEntityBuilderService.buildBehaviorVersionEntityFromDto(
                product, behaviorVersionDto, ivUser, this.usersClient.isPlatformAdmin(ivUser));

        // [2] Call the Configuration Manager to process the template files.
        this.iBehaviorVersionDtoBuilderService.processTemplates(behaviorVersion, ivUser);

        // [4] Build the subsystem of the behavior version.
        this.iBehaviorVersionDtoBuilderService.buildSubsystems(product, behaviorVersion, ivUser);

        // [5] Insert new Behavior version cost information
        this.productBudgetsClient.insertNewBehaviorVersionCostInformation(product, behaviorVersion);

        // Emit Add behavior Version Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(behaviorVersion.getProduct().getId(), ActivityScope.BEHAVIOR_VERSION, ActivityAction.CREATED)
                .entityId(behaviorVersion.getId())
                .addParam(BEHAVIOR_VERSION_NAME, behaviorVersion.getVersionName())
                .addParam(BEHAVIOR_VERSION_DESCRIPTION, behaviorVersion.getDescription())
                .build());
    }

    @Override
    public BVBehaviorVersionSummaryInfoDTO[] getAllBehaviorVersions(Integer productId, String status)
    {
        // Check if product exists
        Product product = productRepository.fetchById(productId);
        this.behaviorVersionValidator.checkProductExistence(product);

        List<BehaviorVersion> behaviorVersions;

        if (Strings.isNullOrEmpty(status))
        {
            log.debug("[BehaviorServiceImpl] -> [getAllBehaviorVersions]: finding all behavior version associated to product id: [{}]", productId);
            // Not filter, get all behavior versions
            behaviorVersions = product.getBehaviorVersions();
        }
        else
        {
            log.debug("[BehaviorServiceImpl] -> [getAllBehaviorVersions]: finding all behavior versions associated to product id: [{}] filter by status: [{}]", productId, status);

            // Filter by behavior version status
            BehaviorVersionStatus behaviorVersionStatus;
            try
            {
                behaviorVersionStatus = BehaviorVersionStatus.valueOf(status);
            }
            catch (IllegalArgumentException e)
            {
                throw new NovaException(BehaviorError.getInvalidBehaviorVersionStatus(status, Arrays.toString(BehaviorVersionStatus.values())));
            }

            behaviorVersions = product.getBehaviorVersions().stream().filter(x -> x.getStatus() == behaviorVersionStatus).collect(Collectors.toList());
        }

        this.sortBehaviourVersions(behaviorVersions);

        log.debug("[BehaviorServiceImpl] -> [getAllBehaviorVersions]: found the following behavior version: [{}]", behaviorVersions);
        return this.behaviorVersionDtoBuilder.buildDtoFromBehaviorVersionList(behaviorVersions);
    }


    @Override
    public BVBehaviorVersionSummaryInfoDTO getBehaviorVersion(final Integer behaviorVersionId)
    {
        // Check if the behavior version does exist.
        BehaviorVersion behaviorVersion = this.behaviorVersionValidator.checkBehaviorVersionExistence(behaviorVersionId);

        // Build request object
        return this.behaviorVersionDtoBuilder.buildBehaviorVersionDTO(behaviorVersion);
    }

    @Override
    public void deleteBehaviorVersion(String ivUser, Integer behaviorVersionId)
    {
        // Check behaviorVersion existence and delete
        this.behaviorVersionValidator.checkBehaviorVersionToDelete(ivUser, behaviorVersionId);
    }

    @Override
    @Transactional
    public void updateBehaviorTestSubsystemBuildStatus(String ivUser, Integer behaviorSubsystemId, BVBehaviorSubsystemBuildStatus behaviorSubsystemBuildStatus)
    {
        log.debug("[{}]  -> [updateBehaviorTestSubsystemBuildStatus]: Updating all behavior subsystems",
                this.getClass().getSimpleName());

        BehaviorSubsystem behaviorSubsystem = behaviorSubsystemRepository.findById(behaviorSubsystemId).orElseThrow(() ->
        {
            throw new NovaException(BehaviorError.getNoSuchBehaviorSubsystemError());
        });

        AsyncStatus newStatus = getBehaviorTestSubsystemStatus(behaviorSubsystemBuildStatus.getStatus());
        behaviorSubsystem.setStatus(newStatus);
        behaviorSubsystem.setStatusMessage(behaviorSubsystemBuildStatus.getJenkinsJobMessage());

        BehaviorSubsystem savedBehaviorSubsystem = behaviorSubsystemRepository.saveAndFlush(behaviorSubsystem);

        BehaviorVersion behaviorVersion = behaviorSubsystem.getBehaviorVersion();

        if (AsyncStatus.ERROR == savedBehaviorSubsystem.getStatus() && behaviorVersion.getStatus() != BehaviorVersionStatus.ERRORS)
        {
            TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(savedBehaviorSubsystem.getSubsystemId());

            behaviorVersion.setStatus(BehaviorVersionStatus.ERRORS);
            behaviorVersion.setStatusDescription("Error generando el subsistema " + subsystemDTO.getSubsystemName()
                    + " con Tag: " + savedBehaviorSubsystem.getTagName() + ". Fallos al compilar el job de jenkins. Detalles: " + behaviorSubsystemBuildStatus.getJenkinsJobMessage());

            // Save the behavior version
            this.behaviorVersionRepository.saveAndFlush(behaviorVersion);
            log.debug("[{}] -> [{}]: Saved behavior version. The subsystemId [{}] is on status ERROR. (Compiled to ERROR). Set behavior version name : [{}] to error. Behavior version status: [{}]",
                    this.getClass().getSimpleName(), "updateBehaviorTestSubsystemBuildStatus", savedBehaviorSubsystem.getId(), behaviorVersion.getVersionName(), behaviorVersion.getStatusDescription());

        }
        else if (AsyncStatus.DONE == savedBehaviorSubsystem.getStatus() && behaviorVersion.getStatus() == BehaviorVersionStatus.BUILDING)
        {
            log.debug("[{}] -> [{}]: checking all subsystem for this behavior version id: [{}] behavior version name: [{}]",
                    this.getClass().getSimpleName(), "updateIfAllDone", behaviorVersion.getId(), behaviorVersion.getVersionName());

            boolean allDone = this.isAllDone(behaviorVersion);

            if (!allDone)
            {
                log.debug("[{}] -> [{}]: Not updated. There are subsystem for finishing on this behavior version id: [{}] behavior version name: [{}]. Waiting to finish the all subsystem. Behavior version status: [{}] Continue...",
                        this.getClass().getSimpleName(), "updateIfAllDone", behaviorVersion.getId(), behaviorVersion.getVersionName(), behaviorVersion.getStatus());
                return;
            }

            // All subsystems finished ok
            log.debug("[{}] -> [{}]: all subsystem for this behavior version id: [{}] behavior version name: [{}] has finished. Checking SQA status and creating Issue Tracker...",
                    this.getClass().getSimpleName(), "updateIfAllDone", behaviorVersion.getId(), behaviorVersion.getVersionName());

            // Check quality state for the behavior version
            String qualityState = qualityManagerService.checkBehaviorVersionQualityState(behaviorVersion, true);

            if (QualityConstants.SQA_NOT_AVAILABLE.equals(qualityState))
            {
                log.error("[{}] -> [{}]: Error occurred while getting quality for behavior version id: [{}], behavior version name: [{}]",
                        this.getClass().getSimpleName(), "updateIfAllDone", behaviorVersion.getId(), behaviorVersion.getVersionName());

                behaviorVersion.setStatus(BehaviorVersionStatus.ERRORS);
                behaviorVersion.setStatusDescription("Se ha producido un error al recopilar la calidad de los servicios. Por favor, vuelva a intentarlo más tarde o contacte con el equipo NOVA.");
            }
            else
            {
                behaviorVersion.setStatus(BehaviorVersionStatus.READY_TO_DEPLOY);
                behaviorVersion.setStatusDescription("Los subsistemas han sido compilados satisfactoriamente. Behavior version lista para configurar.");
                behaviorVersion.setQualityValidation(QualityConstants.SQA_OK.equals(qualityState));
            }

            // Save the behavior version
            this.behaviorVersionRepository.saveAndFlush(behaviorVersion);
            log.debug("[{}] -> [{}]: Saved the behavior version id: [{}] release version name: [{}] has been updated. All subsystem has finished. The release version status is: [{}]",
                    this.getClass().getSimpleName(), "updateIfAllDone", behaviorVersion.getId(), behaviorVersion.getVersionName(), behaviorVersion.getStatus());
        }
    }

    /**
     * Check behavior budgets boolean. It only needs to fill information needed in budgets service to check the budget
     *
     * @param behaviorServiceId the behavior service id
     * @return the boolean
     */
    public Boolean checkBehaviorBudgets(Integer behaviorServiceId)
    {
        BehaviorService behaviorService = this.behaviorServiceRepository.findById(behaviorServiceId).orElseThrow(
                () -> new NovaException(BehaviorError.getBadParametersError(behaviorServiceId)));

        PBCheckExecutionInfo infoAboutExecution = new PBCheckExecutionInfo();
        infoAboutExecution.setBehaviorServiceId(behaviorServiceId);
        infoAboutExecution.setBehaviorVersionId(behaviorService.getBehaviorSubsystem().getBehaviorVersion().getId());
        infoAboutExecution.setProductId(behaviorService.getBehaviorSubsystem().getBehaviorVersion().getProduct().getId());

        // Get the latest defined configuration to obtain the last budget info
        BehaviorServiceConfiguration behaviorServiceConfiguration =
                this.behaviorServiceConfigurationRepository
                        .findFirstByBehaviorServiceIdOrderByLastModifiedDesc(behaviorServiceId)
                        .orElseThrow(() -> new NovaException(BehaviorError.getBadParametersError(behaviorServiceId)));

        // This field is important, due to defines the cost to add and determines if the cost exceed the budget
        infoAboutExecution.setPackIdConfigured(behaviorServiceConfiguration.getHardwarePack().getId());

        return this.productBudgetsClient.checkBehaviorBudgets(infoAboutExecution);
    }

    @Override
    @Transactional
    public BVBehaviorInstanceDTO getBehaviorInstance(final Integer behaviorInstanceId)
    {
        log.debug("[{}]  -> [getBehaviorInstance]: Get behavior instance info for id [{}]",
                this.getClass().getSimpleName(), behaviorInstanceId);

        BehaviorInstance behaviorInstance = this.behaviorInstanceRepository.findById(behaviorInstanceId)
                .orElseThrow(() -> new NovaException(BehaviorError.getBehaviorInstanceByIdNotFoundError(behaviorInstanceId)));

        return this.behaviorVersionDtoBuilder.buildDTOFromBehaviorInstance(behaviorInstance);
    }

    /**
     * Transform the jenkins job group status (this status can be SUCCESS or FAILED) to AsyncStatus
     *
     * @param jenkinsJobGroupStatus - jenkins job group status
     */
    private AsyncStatus getBehaviorTestSubsystemStatus(final String jenkinsJobGroupStatus)
    {
        if (com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.SUCCESS.equalsIgnoreCase(jenkinsJobGroupStatus))
        {
            return AsyncStatus.DONE;
        }
        else
        {
            return AsyncStatus.ERROR;
        }
    }

    ////////////////////////////////////////  PRIVATE  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Validates a specific tag
     *
     * @param ivUser              BBVA user code
     * @param productId           product identifier
     * @param subsystemTagRequest DTO with validation info
     * @return validated tag object
     */
    private BVTagResponseDTO validateTag(String ivUser, Integer productId, BVSubsystemTagDTO subsystemTagRequest)
    {
        BVTagResponseDTO bvTagResponseDTO = new BVTagResponseDTO();
        BVTagDTO tagDTO = subsystemTagRequest.getTags()[0];

        //Get project names
        List<String> projectNames = this.versionControlSystemClient.getProjectsPathsFromRepoTag(subsystemTagRequest.getRepoId(), tagDTO.getTagName());

        //Get DTO list with all services validated
        Product product = productRepository.fetchById(productId);
        List<BVServiceValidationDTO> bvServiceValidationDTOList = this.behaviorTagValidator.buildTagValidationByService(ivUser, product, tagDTO, subsystemTagRequest, projectNames);

        bvTagResponseDTO.setTag(tagDTO);
        bvTagResponseDTO.setServiceValidation(bvServiceValidationDTOList.toArray(new BVServiceValidationDTO[0]));
        bvTagResponseDTO.setGeneral(this.behaviorTagValidator.buildTagValidation(tagDTO, subsystemTagRequest, bvServiceValidationDTOList, projectNames));

        return bvTagResponseDTO;
    }

    /**
     * Validates the status of the tag verifying all the services
     *
     * @param bvTagResponseDTO tag dto
     * @return status
     */
    private String getValidationStatus(BVTagResponseDTO bvTagResponseDTO)
    {
        String status;
        if (this.isAnyServiceWithValidationError(bvTagResponseDTO))
        {
            status = ValidationStatus.ERROR.name();
        }
        else if (this.isAnyServiceWithWarningError(bvTagResponseDTO))
        {
            status = ValidationStatus.WARNING.name();
        }
        else
        {
            status = ValidationStatus.OK.name();
        }

        return status;
    }

    /**
     * Validates if there is any service with ERROR status
     *
     * @param bvTagResponseDTO tag dto
     * @return validation result
     */
    private boolean isAnyServiceWithValidationError(BVTagResponseDTO bvTagResponseDTO)
    {
        return Arrays.stream(bvTagResponseDTO.getServiceValidation())
                .flatMap(service -> Arrays.stream(service.getError()))
                .anyMatch(error -> error.getStatus().equals(ValidationStatus.ERROR.name()));
    }

    /**
     * Validates if there is any service with WARNING status
     *
     * @param bvTagResponseDTO tag dto
     * @return validation result
     */
    private boolean isAnyServiceWithWarningError(BVTagResponseDTO bvTagResponseDTO)
    {
        return Arrays.stream(bvTagResponseDTO.getServiceValidation())
                .flatMap(service -> Arrays.stream(service.getError()))
                .anyMatch(error -> error.getStatus().equals(ValidationStatus.WARNING.name()));
    }

    /**
     * Validate that given behavior version can be created
     *
     * @param ivUser      BBVA user code
     * @param versionName behavior version name
     * @return the product
     */
    private Product validateBehaviorVersionCreation(String ivUser, String versionName, Integer productId)
    {
        // Check if given user has permission to current action
        Product product = productRepository.fetchById(productId);
        this.behaviorVersionValidator.checkProductExistence(product);
        this.usersClient.checkHasPermission(ivUser, Constants.CREATE_BEHAVIOR_PERMISSION, product.getId(), PERMISSION_DENIED);

        // Check if product has subsystems - if not, version cannot be created.
        this.behaviorVersionValidator.checkProductSubsystems(product.getId());

        // Check if there is a behavior version with the same name.
        this.behaviorVersionValidator.existsBehaviorVersionWithSameName(product, versionName);

        // Check number of existing behavior version
        this.behaviorVersionValidator.checkMaxBehaviorVersions(product.getId(), product.getReleaseSlots());

        // Check if exist a current behavior version compiling
        this.behaviorVersionValidator.checkCompilingBehaviorVersions(product.getId());

        return product;
    }

    /**
     * Sort behavior versions by version name
     *
     * @param behaviorVersions behavior versions list
     */
    private void sortBehaviourVersions(List<BehaviorVersion> behaviorVersions)
    {
        behaviorVersions.sort(Comparator.comparing(BehaviorVersion::getVersionName));
    }

    /**
     * Is all done
     *
     * @param behaviorVersion behavior version
     * @return all done
     */
    private boolean isAllDone(BehaviorVersion behaviorVersion)
    {
        boolean allDone = true;
        for (BehaviorSubsystem subsystemI : behaviorVersion.getSubsystems())
        {
            log.debug("[{}] -> [{}]: checking status fo subsystem id: [{}] subsystem compilation job name: [{}]",
                    this.getClass().getSimpleName(), "isAllDone", subsystemI.getId(), subsystemI.getCompilationJobName());
            if (subsystemI.getStatus() != AsyncStatus.DONE)
            {
                allDone = false;
                log.debug("[{}] -> [{}]: the subsystem id: [{}] subsystem compilation job name: [{}], status: [{}] has been to FALSE",
                        this.getClass().getSimpleName(), "isAllDone", subsystemI.getId(),
                        subsystemI.getCompilationJobName(), subsystemI.getStatus());
                break;
            }
            log.debug("[{}] -> [{}]: Checked status of the subsystem id: [{}] subsystem compilation job name: [{}] status: [{}]",
                    this.getClass().getSimpleName(), "isAllDone", subsystemI.getId(),
                    subsystemI.getCompilationJobName(), subsystemI.getStatus());
        }

        return allDone;
    }
}
