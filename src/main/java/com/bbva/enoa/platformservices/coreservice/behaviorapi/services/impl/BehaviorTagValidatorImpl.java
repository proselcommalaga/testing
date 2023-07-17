package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.*;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.exceptions.BehaviorError;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorServiceDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorTagValidator;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.PomXML;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.enumerates.ValidationStatus;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IProjectFileValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ErrorListUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Methods necessaries to validate a tag
 */
@Slf4j
@Service
public class BehaviorTagValidatorImpl implements IBehaviorTagValidator
{
    @Value("${nova.validations.maxServicesInSubsystem}")
    private int maxServices;

    /**
     * Service DTO builder
     */
    private final IBehaviorServiceDtoBuilder serviceDtoBuilder;

    /**
     * Validates if a service is NOVA compliant.
     */
    private final IProjectFileValidator iProjectFileValidator;

    /**
     * Release version repository
     */
    private final ReleaseVersionRepository releaseVersionRepository;

    @Autowired
    public BehaviorTagValidatorImpl(final IBehaviorServiceDtoBuilder serviceDtoBuilder,
                                    final IProjectFileValidator iProjectFileValidator,
                                    final ReleaseVersionRepository releaseVersionRepository)
    {
        this.serviceDtoBuilder = serviceDtoBuilder;
        this.iProjectFileValidator = iProjectFileValidator;
        this.releaseVersionRepository = releaseVersionRepository;
    }

    @Override
    public List<BVServiceValidationDTO> buildTagValidationByService(final String ivUser, final Product product, final BVTagDTO tagDTO, final BVSubsystemTagDTO subsystemTagRequest, final List<String> projectFiles)
    {
        List<BVServiceValidationDTO> bvServiceValidationDTOList = new ArrayList<>();

        for (String projectName : projectFiles)
        {
            BVServiceValidationDTO serviceValidationDTO = new BVServiceValidationDTO();

            // Read Action. Get the validation parameters from files that are being validated
            ValidatorInputs validatorInputs = this.serviceDtoBuilder.buildValidationFileInputs(projectName, subsystemTagRequest.getRepoId(), tagDTO.getTagName());

            // Parse files and build DTO service with its data
            BVServiceInfoDTO bvServiceInfoDTO = this.serviceDtoBuilder.buildServiceGeneralInfo(validatorInputs);

            if (bvServiceInfoDTO == null)
            {
                BVServiceInfoDTO serviceInfoError = new BVServiceInfoDTO();

                serviceInfoError.setServiceName(Constants.INVALID_SERVICE_NAME);

                BVErrorDTO bvErrorDTO = new BVErrorDTO();

                bvErrorDTO.setStatus(ValidationStatus.ERROR.name());
                bvErrorDTO.setCode(Constants.INVALID_SERVICE_NAME_CODE);
                bvErrorDTO.setMessage(Constants.INVALID_SERVICE_NAME_MSG);

                serviceValidationDTO.setService(serviceInfoError);
                serviceValidationDTO.setError(new BVErrorDTO[]{bvErrorDTO});
            }
            else
            {
                this.serviceDtoBuilder.buildServiceGenerics(tagDTO.getTagUrl(), projectName, bvServiceInfoDTO);

                // Create list of validation errors.
                List<ValidationErrorDto> errorList = new ArrayList<>();

                if (!bvServiceInfoDTO.getReleaseVersion().isEmpty())
                {
                    // Check if release version declared exists in the product
                    this.validateBehaviorReleaseVersion(errorList, bvServiceInfoDTO, product.getId());
                }

                // Check validation.
                this.iProjectFileValidator.validateBehaviorServiceProjectFiles(errorList, validatorInputs, bvServiceInfoDTO, subsystemTagRequest.getRepoId(),
                        tagDTO.getTagName(), ivUser, product, subsystemTagRequest.getSubsystemName(), subsystemTagRequest.getBehaviorVersionName());

                // Finally, build BVServiceValidationDTO from BVServiceInfoDTO
                serviceValidationDTO = this.buildBVServiceValidationDTO(bvServiceInfoDTO);
            }

            bvServiceValidationDTOList.add(serviceValidationDTO);

            // Clear artifactId names, this list is used to check if all artifact Ids are unique
            PomXML.clearArtifactIdList();
        }

        return bvServiceValidationDTOList;
    }

    @Override
    public BVErrorDTO[] buildTagValidation(final BVTagDTO tagDTO, final BVSubsystemTagDTO subsystemTagRequest, final List<BVServiceValidationDTO> serviceValidationDTOList, final List<String> projectFiles)
    {
        // Create list of validation errors.
        List<BVErrorDTO> errorList = new ArrayList<>();

        // [1] Validate if given tags contain services. Get projects name that have a pom.xml
        if (projectFiles.isEmpty() && serviceValidationDTOList.isEmpty())
        {
            BVErrorDTO error = new BVErrorDTO();

            error.setStatus(ValidationStatus.ERROR.name());
            error.setCode(Constants.NO_SERVICES_IN_TAG);
            error.setMessage(Constants.NO_SERVICES_IN_TAG_MSG);

            errorList.add(error);

            log.error("[{}] -> [{}]: Tag [{}] on subsystem [{}] has failed validation: [{}]", "BehaviorTagValidatorImpl", "buildTagValidation",
                    tagDTO.getTagName(), subsystemTagRequest.getSubsystemName(), Constants.NO_SERVICES_IN_TAG_MSG);

            throw new NovaException(BehaviorError.getSubsystemTagWithoutServicesError(),
                    "Tag [" + tagDTO.getTagName() + "] on subsystem [" + subsystemTagRequest.getSubsystemName() + "] has no services.");
        }

        // [2] Validate if allowed number of services on a subsystem is not exceeded in given tag
        if (serviceValidationDTOList.size() > maxServices)
        {
            BVErrorDTO error = new BVErrorDTO();

            error.setStatus(ValidationStatus.ERROR.name());
            error.setCode(Constants.MAX_SERVICES_EXCEEDED);
            error.setMessage(Constants.MAX_SERVICES_EXCEEDED_MSG);

            errorList.add(error);

            log.warn("[{}] -> [{}]: Tag [{}] on subsystem [{}] has failed validation: [{}]", Constants.TAG_VALIDATOR, "buildTagValidation",
                    tagDTO.getTagName(), subsystemTagRequest.getSubsystemName(), Constants.MAX_SERVICES_EXCEEDED_MSG);
        }

        // [3] Validate specific validations for subsystem type
        BVErrorDTO error = this.validateBehaviorSubsystem(serviceValidationDTOList);

        // In case of error, add to list
        if (error != null)
        {
            errorList.add(error);
        }

        return errorList.toArray(new BVErrorDTO[0]);
    }


//////////////////////////////////////////////  PRIVATE  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Build a BVServiceValidationDTO from BVServiceInfoDTO
     *
     * @param bvServiceInfoDTO behavior service DTO
     * @return BVServiceValidationDTO
     */
    private BVServiceValidationDTO buildBVServiceValidationDTO(final BVServiceInfoDTO bvServiceInfoDTO)
    {
        log.debug("[BehaviorTagValidatorImpl] -> [buildBVServiceValidationDTO]: Build new BVServiceValidationDTO from BVServiceInfoDTO [{}]", bvServiceInfoDTO);

        BVServiceValidationDTO serviceValidationDTO = new BVServiceValidationDTO();

        serviceValidationDTO.setError(this.buildBVErrorDTOArray(bvServiceInfoDTO));
        serviceValidationDTO.setService(bvServiceInfoDTO);

        log.debug("[BehaviorTagValidatorImpl] -> [buildBVServiceValidationDTO]: Built BVServiceValidationDTO from BVServiceInfoDTO. Result: [{}]", serviceValidationDTO);
        return serviceValidationDTO;
    }

    /**
     * Build a BVErrorDTO array from BVServiceInfoDTO
     *
     * @param bvServiceInfoDTO behavior service DTO
     * @return BVErrorDTO array
     */
    private BVErrorDTO[] buildBVErrorDTOArray(final BVServiceInfoDTO bvServiceInfoDTO)
    {
        List<BVErrorDTO> bvErrorDTOList = new ArrayList<>();

        for (BVValidationErrorDto validationErrorDto : bvServiceInfoDTO.getValidationErrors())
        {
            BVErrorDTO bvErrorDTO = new BVErrorDTO();

            BeanUtils.copyProperties(validationErrorDto, bvErrorDTO);
            bvErrorDTO.setStatus(ValidationStatus.ERROR.name());

            bvErrorDTOList.add(bvErrorDTO);
        }

        log.debug("[BehaviorTagValidatorImpl] -> [buildBVErrorDTOArray]: Built BVErrorDTO array from BVServiceInfoDTO. Result: [{}]", bvErrorDTOList);

        return bvErrorDTOList.toArray(new BVErrorDTO[0]);
    }

    /**
     * Validate a Behavior subsystem
     *
     * @param serviceValidationDTOList service list
     * @return dto with error
     */
    private BVErrorDTO validateBehaviorSubsystem(final List<BVServiceValidationDTO> serviceValidationDTOList)
    {
        BVErrorDTO error = null;

        // Validate if services are allowed in LIBRARY subsystem
        List<BVServiceValidationDTO> servicesNotAllowed = serviceValidationDTOList
                .stream()
                .filter(dto -> isServiceNotAllowedForBehaviorSubsystem(dto.getService().getServiceType()))
                .collect(Collectors.toList());

        if (!servicesNotAllowed.isEmpty())
        {
            error = new BVErrorDTO();

            error.setStatus(ValidationStatus.ERROR.name());
            error.setCode(Constants.WRONG_BEHAVIOR_SUBSYSTEM);
            error.setMessage(Constants.WRONG_BEHAVIOR_SUBSYSTEM_MSG);
        }
        return error;
    }

    /**
     * Identify if service is do not allowed in LIBRARY subsystem
     *
     * @param serviceType the {@link ServiceType} name that we want to check
     * @return boolean to indicate if service is not allowed
     */
    private static boolean isServiceNotAllowedForBehaviorSubsystem(final String serviceType)
    {
        return serviceType == null ||
                !ServiceType.isBehaviorTestService(serviceType);
    }

    /**
     * Validate if release version name exists in the product
     *
     * @param errorList        error list
     * @param bvServiceInfoDTO behavior service DTO
     */
    private void validateBehaviorReleaseVersion(List<ValidationErrorDto> errorList, final BVServiceInfoDTO bvServiceInfoDTO, final Integer productId)
    {
        if (this.releaseVersionRepository.findByProductIdAndVersionName(productId, bvServiceInfoDTO.getReleaseVersion()) == null)
        {
            ErrorListUtils.addError(errorList, bvServiceInfoDTO.getServiceName(), Constants.INVALID_RELEASE_VERSION_FOR_BEHAVIOR_TEST, Constants.INVALID_RELEASE_VERSION_FOR_BEHAVIOR_TEST_MSG);
        }
    }

}