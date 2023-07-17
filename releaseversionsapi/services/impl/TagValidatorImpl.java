package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.*;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.releasesapi.exceptions.ReleaseError;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.PomXML;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.enumerates.ValidationStatus;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IProjectFileValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IServiceDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.ITagValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Methods necessaries to validate a tag
 */
@Slf4j
@Service
public class TagValidatorImpl implements ITagValidator
{
    @Value("${nova.validations.maxServicesInSubsystem}")
    private int maxServices;

    /**
     * Release repository
     */
    private final ReleaseRepository releaseRepository;

    /**
     * Client of the VCS API.
     */
    private final IVersioncontrolsystemClient iVersioncontrolsystemClient;

    /**
     * Service DTO builder
     */
    private final IServiceDtoBuilder serviceDtoBuilder;

    /**
     * Validates if a service is NOVA compliant.
     */
    private final IProjectFileValidator iProjectFileValidator;

    private final ReleaseValidator releaseValidator;

    @Autowired
    public TagValidatorImpl(final ReleaseRepository releaseRepository, final IVersioncontrolsystemClient iVersioncontrolsystemClient, final IServiceDtoBuilder serviceDtoBuilder,
                            final IProjectFileValidator iProjectFileValidator, final ReleaseValidator releaseValidator)
    {
        this.releaseRepository = releaseRepository;
        this.serviceDtoBuilder = serviceDtoBuilder;
        this.iVersioncontrolsystemClient = iVersioncontrolsystemClient;
        this.iProjectFileValidator = iProjectFileValidator;
        this.releaseValidator = releaseValidator;
    }

    @Override
    public RVErrorDTO[] buildTagValidation(final RVTagDTO tagDTO, final RVSubsystemBaseDTO subsystemDTO, final List<RVServiceValidationDTO> serviceValidationDTOList, final List<String> projectFiles)
    {
        // Create list of validation errors.
        List<RVErrorDTO> errorList = new ArrayList<>();

        // [1] Validate if given tags contain services. Get projects name that have a pom.xml

        if (!projectFiles.isEmpty())
        {
            log.warn("[{}] -> [{}]: There are no projects in repo: [{}] and tag: [{}], aborting", Constants.SUBSYSTEM_DTO_BUILDER, "buildServicesFromSubsystemTag",
                    subsystemDTO.getRepoId(), tagDTO);
        }
        else
        {
            // [1a] Validate dependency file
            errorList.addAll(this.validateDependencyFile(subsystemDTO.getRepoId(), tagDTO, projectFiles));

            // [1b] Validate if given tag contain services.

            if (serviceValidationDTOList.isEmpty())
            {
                RVErrorDTO error = new RVErrorDTO();

                error.setStatus(ValidationStatus.ERROR.name());
                error.setCode(Constants.NO_SERVICES_IN_TAG);
                error.setMessage(Constants.NO_SERVICES_IN_TAG_MSG);

                errorList.add(error);

                log.error("[{}] -> [{}]: Tag [{}] on subsystem [{}] has failed validation: [{}]", Constants.TAG_VALIDATOR, "buildTagValidation",
                        tagDTO.getTagName(), subsystemDTO.getSubsystemName(), Constants.NO_SERVICES_IN_TAG_MSG);

                throw new NovaException(ReleaseVersionError.getSubsystemTagWithoutServicesError(),
                        "Tag [" + tagDTO.getTagName() + "] on subsystem [" + subsystemDTO.getSubsystemName() + "] has no services.");
            }
        }

        // [2] Validate if allowed number of services on a subsystem is not exceeded in given tag
        if (serviceValidationDTOList.size() > maxServices)
        {
            RVErrorDTO error = new RVErrorDTO();

            error.setStatus(ValidationStatus.ERROR.name());
            error.setCode(Constants.MAX_SERVICES_EXCEEDED);
            error.setMessage(Constants.MAX_SERVICES_EXCEEDED_MSG);

            errorList.add(error);

            log.warn("[{}] -> [{}]: Tag [{}] on subsystem [{}] has failed validation: [{}]", Constants.TAG_VALIDATOR, "buildTagValidation",
                    tagDTO.getTagName(), subsystemDTO.getSubsystemName(), Constants.MAX_SERVICES_EXCEEDED_MSG);
        }

        // [3] Validate specific validations for subsystem type
        SubsystemType subsystemType = SubsystemType.getValueOf(subsystemDTO.getSubsystemType());

        RVErrorDTO error = null;

        switch (subsystemType)
        {
            case EPHOENIX:
                error = this.validateEPhoenixSubsystem(serviceValidationDTOList);
                break;
            case LIBRARY:
                error = this.validateLibrarySubsystem(serviceValidationDTOList);
                break;
            case BEHAVIOR_TEST:
                error = this.validateBehaviorSubsystem(serviceValidationDTOList);
                break;
            case NOVA:
            case FRONTCAT:
                break;
            default:
                error = new RVErrorDTO();

                error.setStatus(ValidationStatus.ERROR.name());
                error.setCode(Constants.WRONG_SUBSYSTEM);
                error.setMessage(Constants.WRONG_SUBSYSTEM_MSG);
        }

        // In case of error, add to list
        if (error != null)
        {
            errorList.add(error);
        }

        return errorList.toArray(new RVErrorDTO[0]);
    }

    @Override
    public List<RVServiceValidationDTO> buildTagValidationByService(final String ivUser, final Integer releaseId, final RVTagDTO tagDTO, final RVSubsystemBaseDTO subsystemDTO, final List<String> projectFiles)
    {
        List<RVServiceValidationDTO> rvServiceValidationDTOList = new ArrayList<>();
        // Get release
        Release release = this.releaseRepository.findById(releaseId)
                .orElseThrow(() -> new NovaException(ReleaseError.getNoSuchReleaseError(), "[ReleasesAPI] -> [checkReleaseExistence]: the release id: [" + releaseId + "] does not exists into NOVA BBDD"));

        // Check if the Release does exist.
        this.releaseValidator.checkReleaseExistence(release);
        for (String projectName : projectFiles)
        {
            RVServiceValidationDTO serviceValidationDTO = new RVServiceValidationDTO();

            // Read Action. Get the validation parameters from files that are being validated
            ValidatorInputs validatorInputs = this.serviceDtoBuilder.buildValidationFileInputs(projectName, subsystemDTO.getRepoId(), tagDTO.getTagName());

            //Parse pom.xml and build DTO service with its data
            NewReleaseVersionServiceDto releaseVersionServiceDto = this.serviceDtoBuilder.buildServiceGeneralInfo(release.getName(), validatorInputs);

            if (releaseVersionServiceDto == null)
            {
                RVServiceDTO rvServiceDTO = new RVServiceDTO();

                rvServiceDTO.setServiceName(Constants.INVALID_SERVICE_NAME);
                rvServiceDTO.setIsService(false);

                RVErrorDTO rvErrorDTO = new RVErrorDTO();

                rvErrorDTO.setStatus(ValidationStatus.ERROR.name());
                rvErrorDTO.setCode(Constants.INVALID_SERVICE_NAME_CODE);
                rvErrorDTO.setMessage(Constants.INVALID_SERVICE_NAME_MSG);

                serviceValidationDTO.setService(rvServiceDTO);
                serviceValidationDTO.setError(new RVErrorDTO[]{rvErrorDTO});
            }
            else
            {
                this.serviceDtoBuilder.buildServiceGenerics(validatorInputs.getPackageJson().getDependencies().toArray(new String[0]), tagDTO.getTagUrl(), projectName, releaseVersionServiceDto);
                // Legacy
                if (!validatorInputs.isLatestVersion() && ServiceType.NODE.getServiceType().equals(releaseVersionServiceDto.getServiceType()))
                {
                    // If service is based on Node.js, set the URL to the package.json file.
                    // Set the URL to the project definition file - aka pom.xml.
                    releaseVersionServiceDto.setNodePackageUrl(tagDTO.getTagUrl() + Constants.SEPARATOR + projectName + Constants.SEPARATOR +
                            Constants.PACKAGE_JSON);

                    releaseVersionServiceDto.setModules(validatorInputs.getPackageJson().getDependencies().toArray(new String[0]));
                }

                // Check validation.
                this.iProjectFileValidator.validateServiceProjectFiles(validatorInputs, SubsystemType.getValueOf(subsystemDTO.getSubsystemType()), releaseVersionServiceDto, subsystemDTO.getRepoId(),
                        tagDTO.getTagName(), ivUser, release.getName(), release.getProduct(), subsystemDTO.getSubsystemName());

                // Finally, build RVServiceValidationDTO from NewReleaseVersionServiceDto

                serviceValidationDTO = this.buildRVServiceValidationDTO(releaseVersionServiceDto);

                // check the length of the service_release < 32 to avoid ETHER problems
                if (release.getName().length() + projectName.length() > 31)
                {
                    final RVErrorDTO errorDTO = new RVErrorDTO();

                    errorDTO.setStatus(ValidationStatus.WARNING.name());
                    errorDTO.setCode(Constants.WRONG_NAME);
                    errorDTO.setMessage(Constants.WRONG_NAME_MSG);

                    serviceValidationDTO.setError((RVErrorDTO[]) ArrayUtils.add(serviceValidationDTO.getError(), errorDTO));
                }

                if (ServiceType.EPHOENIX_BATCH.name().equalsIgnoreCase(releaseVersionServiceDto.getServiceType())
                        || ServiceType.EPHOENIX_ONLINE.name().equalsIgnoreCase(releaseVersionServiceDto.getServiceType()))
                {
                    boolean hasEphoenixDevelopmentEnvironment = Arrays.asList(releaseVersionServiceDto.getMetadata())
                            .stream()
                            .anyMatch(metadata -> Constants.EPHOENIX_DEVELOPMENT_ENVIRONMENT.equalsIgnoreCase(metadata.getKey()) &&
                                    Constants.TRUE.equalsIgnoreCase(metadata.getValue()));

                    boolean hasEphoenixDevelopmentEnvironmentPromotable = Arrays.asList(releaseVersionServiceDto.getMetadata())
                            .stream()
                            .anyMatch(metadata -> Constants.EPHOENIX_DEVELOPMENT_ENVIRONMENT_PROMOTION.equalsIgnoreCase(metadata.getKey()) &&
                                    Constants.TRUE.equalsIgnoreCase(metadata.getValue()));

                    if (hasEphoenixDevelopmentEnvironment && !hasEphoenixDevelopmentEnvironmentPromotable)
                    {
                        //If the ephoenix is for development environment and it cannot be promotable we must warn to the user
                        final RVErrorDTO errorDTO = new RVErrorDTO();

                        errorDTO.setStatus(ValidationStatus.WARNING.name());
                        errorDTO.setCode(Constants.WARNING_EPHOENIX_DEVELOPMENT_ENVIRONMENT);
                        errorDTO.setMessage(Constants.WARNING_EPHOENIX_DEVELOPMENT_ENVIRONMENT_MSG);

                        serviceValidationDTO.setError((RVErrorDTO[]) ArrayUtils.add(serviceValidationDTO.getError(), errorDTO));
                    }
                }
            }

            rvServiceValidationDTOList.add(serviceValidationDTO);

            // Clear artifactId names, this list is used to check if all artifact Ids are unique
            PomXML.clearArtifactIdList();
        }


        return rvServiceValidationDTOList;
    }

    @Override
    public void validateAllowedMultitag(RVValidationDTO rvValidationDTO)
    {
        SubsystemType subsystemType = SubsystemType.getValueOf(rvValidationDTO.getSubsystem().getSubsystemType());

        if (!SubsystemType.LIBRARY.equals(subsystemType) && rvValidationDTO.getTags().length > 1)
        {
            log.error("[{}] -> [validateAllowedMultitag]: Multitag action not allowed for subsystem type [{}]", Constants.TAG_VALIDATOR, subsystemType);

            throw new NovaException(ReleaseVersionError.getMultitagNotAllowedError(),
                    "Multitag action not allowed for subsystem type " + subsystemType + ".");
        }
    }


//////////////////////////////////////////////  PRIVATE  \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Validate dependency file
     *
     * @param repoId       repository id
     * @param rvTagDTO     tag
     * @param projectNames project names
     */
    private List<RVErrorDTO> validateDependencyFile(int repoId, RVTagDTO rvTagDTO, List<String> projectNames)
    {
        List<RVErrorDTO> errorList = new ArrayList<>();

        List<String> dependencies = this.iVersioncontrolsystemClient.getDependencies(repoId, rvTagDTO.getTagName());
        for (String dependency : dependencies)
        {
            if (!projectNames.contains(dependency))
            {
                RVErrorDTO error = new RVErrorDTO();

                error.setStatus(ValidationStatus.ERROR.name());
                error.setCode(Constants.DEPENDENCY_NOT_FOUND);
                error.setMessage(Constants.DEPENDENCY_NOT_FOUND_MSG + dependency);

                errorList.add(error);
            }
        }

        return errorList;
    }

    /**
     * Validate an Library subsystem
     *
     * @param serviceValidationDTO service list
     * @return dto with error
     */
    private RVErrorDTO validateLibrarySubsystem(final List<RVServiceValidationDTO> serviceValidationDTO)
    {
        RVErrorDTO error = null;

        // Validate if services are allowed in LIBRARY subsystem
        List<RVServiceValidationDTO> servicesNotAllowed = serviceValidationDTO
                .stream()
                .filter(dto -> isServiceNotAllowedForLibrarySubsystem(dto.getService().getServiceType()))
                .collect(Collectors.toList());

        if (!servicesNotAllowed.isEmpty())
        {
            error = new RVErrorDTO();

            error.setStatus(ValidationStatus.ERROR.name());
            error.setCode(Constants.WRONG_LIBRARY_SUBSYSTEM);
            error.setMessage(Constants.WRONG_LIBRARY_SUBSYSTEM);
        }
        return error;
    }

    /**
     * Identify if service is do not allowed in LIBRARY subsystem
     *
     * @param serviceType the {@link ServiceType} name that we want to check
     * @return boolean to indicate if service is not allowed
     */
    private static boolean isServiceNotAllowedForLibrarySubsystem(final String serviceType)
    {
        return serviceType == null ||
                (ServiceType.getValueOf(serviceType) != ServiceType.DEPENDENCY) &&
                        !ServiceType.isLibrary(ServiceType.getValueOf(serviceType));
    }

    /**
     * Validate an EPhoenix subsystem
     *
     * @param serviceValidationDTO service list
     * @return dto with error
     */
    private RVErrorDTO validateEPhoenixSubsystem(final List<RVServiceValidationDTO> serviceValidationDTO)
    {
        RVErrorDTO error = null;

        // Validate if services are allowed in EPHOENIX environment
        List<RVServiceValidationDTO> servicesNotAllowed = serviceValidationDTO.stream().filter(dto ->
                        dto.getService().getServiceType() == null || (!dto.getService().getServiceType().equalsIgnoreCase(ServiceType.EPHOENIX_BATCH.name())
                                && !dto.getService().getServiceType().equalsIgnoreCase(ServiceType.EPHOENIX_ONLINE.name())))
                .collect(Collectors.toList());

        log.warn("[{}] -> [{}]: Servicios no permitidos{} . No puede incluirse en un subsistema Ephoenix!", Constants.RELEASE_VERSIONS_API_NAME, "validateEPhoenixSubsystem", servicesNotAllowed);

        if (!servicesNotAllowed.isEmpty())
        {
            error = new RVErrorDTO();

            error.setStatus(ValidationStatus.ERROR.name());
            error.setCode(Constants.WRONG_EPHOENIX_SUBSYSTEM);
            error.setMessage(Constants.WRONG_EPHOENIX_SUBSYSTEM_MSG);
        }

        return error;
    }

    /**
     * Build a RVServiceValidationDTO from NewReleaseVersionServiceDto
     *
     * @param releaseVersionServiceDto release version service DTO
     * @return RVServiceValidationDTO
     */
    private RVServiceValidationDTO buildRVServiceValidationDTO(final NewReleaseVersionServiceDto releaseVersionServiceDto)
    {
        log.debug("[ReleaseVersion Api] -> [buildRVServiceValidationDTO]: Build new RVServiceValidationDTO from NewReleaseVersionServiceDto [{}]", releaseVersionServiceDto);

        RVServiceValidationDTO serviceValidationDTO = new RVServiceValidationDTO();

        serviceValidationDTO.setError(this.builRVErrorDTOArray(releaseVersionServiceDto));
        serviceValidationDTO.setService(this.buildRVServiceDTO(releaseVersionServiceDto));

        log.debug("[ReleaseVersion Api] -> [buildRVServiceValidationDTO]: Built RVServiceValidationDTO from NewReleaseVersionServiceDto. Result: [{}]", serviceValidationDTO);
        return serviceValidationDTO;
    }

    /**
     * Build a RVServiceDTO from NewReleaseVersionServiceDto
     *
     * @param releaseVersionServiceDto release version service DTO
     * @return RVServiceDTO
     */
    private RVServiceDTO buildRVServiceDTO(final NewReleaseVersionServiceDto releaseVersionServiceDto)
    {
        RVServiceDTO rvServiceDTO = new RVServiceDTO();

        BeanUtils.copyProperties(releaseVersionServiceDto, rvServiceDTO);

        log.debug("[ReleaseVersion Api] -> [buildRVServiceDTO]: Built RVServiceDTO from NewReleaseVersionServiceDto. Result: [{}]", rvServiceDTO);

        return rvServiceDTO;
    }

    /**
     * Build a RVErrorDTO array from NewReleaseVersionServiceDto
     *
     * @param releaseVersionServiceDto release version service DTO
     * @return RVErrorDTO array
     */
    private RVErrorDTO[] builRVErrorDTOArray(final NewReleaseVersionServiceDto releaseVersionServiceDto)
    {
        List<RVErrorDTO> rvErrorDTOList = new ArrayList<>();

        for (ValidationErrorDto validationErrorDto : releaseVersionServiceDto.getValidationErrors())
        {
            RVErrorDTO rvErrorDTO = new RVErrorDTO();

            BeanUtils.copyProperties(validationErrorDto, rvErrorDTO);
            rvErrorDTO.setStatus(ValidationStatus.ERROR.name());

            rvErrorDTOList.add(rvErrorDTO);
        }

        log.debug("[ReleaseVersion Api] -> [builRVErrorDTOArray]: Built RVErrorDTO array from NewReleaseVersionServiceDto. Result: [{}]", rvErrorDTOList);

        return rvErrorDTOList.toArray(new RVErrorDTO[0]);
    }

    /**
     * Validate an Library subsystem
     *
     * @param serviceValidationDTO service list
     * @return dto with error
     */
    private RVErrorDTO validateBehaviorSubsystem(final List<RVServiceValidationDTO> serviceValidationDTO)
    {
        RVErrorDTO error = null;

        // Validate if services are allowed in LIBRARY subsystem
        List<RVServiceValidationDTO> servicesNotAllowed = serviceValidationDTO
                .stream()
                .filter(dto -> isServiceNotAllowedForBehaviorSubsystem(dto.getService().getServiceType()))
                .collect(Collectors.toList());

        if (!servicesNotAllowed.isEmpty())
        {
            error = new RVErrorDTO();

            error.setStatus(ValidationStatus.ERROR.name());
            error.setCode(Constants.WRONG_LIBRARY_SUBSYSTEM);
            error.setMessage(Constants.WRONG_LIBRARY_SUBSYSTEM);
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
}