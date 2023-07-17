package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.librarymanagerapi.client.feign.nova.rest.IRestHandlerLibrarymanagerapi;
import com.bbva.enoa.apirestgen.librarymanagerapi.client.feign.nova.rest.IRestListenerLibrarymanagerapi;
import com.bbva.enoa.apirestgen.librarymanagerapi.client.feign.nova.rest.impl.RestHandlerLibrarymanagerapi;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsByServiceDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryNovaYmlRequirementDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryPublicationDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryValidationErrorDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsageDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsedLibrariesDTO;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ILibraryManagerClient;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.exceptions.LibraryManagerError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYmlRequirement;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ProjectFileReader;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * LibraryManagerClientImpl
 */
@Service
@Slf4j
public class LibraryManagerClientImpl implements ILibraryManagerClient
{
    /**
     * RestHandler interface for LibraryManagerAPI
     */
    private final IRestHandlerLibrarymanagerapi restInterface;

    /**
     * Own rest handler implementation for LibraryManager
     */
    private RestHandlerLibrarymanagerapi restHandler;

    @Autowired
    public LibraryManagerClientImpl(final IRestHandlerLibrarymanagerapi restInterface)
    {
        this.restInterface = restInterface;
    }

    @Override
    @PostConstruct
    public void init()
    {
        this.restHandler = new RestHandlerLibrarymanagerapi(this.restInterface);
    }

    @Override
    public void storeRequirements(final ReleaseVersionService releaseVersionService)
    {
        log.debug("[{}] -> [{}]: Storing requirements for service [{}]", Constants.LIBRARY_MANAGER_CLIENT,
                Constants.STORE_REQUIREMENTS, releaseVersionService.getFinalName());

        // An alternative would be to publish everything when the release version is created
        final ValidatorInputs inputs = new ValidatorInputs();
        ProjectFileReader.scanNovaYml(releaseVersionService.getFolder(), releaseVersionService.getNovaYml().getContents().getBytes(), inputs);

        List<NovaYmlRequirement> requirementsList = inputs.getNovaYml().getRequirements();
        final LMLibraryRequirementDTO[] requirementDTOs = requirementsList.stream().map(requirement -> {
            LMLibraryRequirementDTO requirementDTO = new LMLibraryRequirementDTO();
            requirementDTO.setRequirementName(requirement.getName());
            requirementDTO.setRequirementType(requirement.getType());
            requirementDTO.setRequirementValue(requirement.getValue());
            requirementDTO.setRequirementDescription(requirement.getDescription());
            return requirementDTO;
        }).toArray(LMLibraryRequirementDTO[]::new);

        final LMLibraryRequirementsDTO lmLibraryRequirementsDTO = new LMLibraryRequirementsDTO();
        lmLibraryRequirementsDTO.setReleaseVersionServiceId(releaseVersionService.getId());
        lmLibraryRequirementsDTO.setFullName(releaseVersionService.getFinalName());
        lmLibraryRequirementsDTO.setRequirements(requirementDTOs);

        this.restHandler.storeRequirements(new IRestListenerLibrarymanagerapi()
        {
            @Override
            public void storeRequirements()
            {
                log.debug("[{}] -> [{}]: Requirements for service [{}] stored",
                        Constants.LIBRARY_MANAGER_CLIENT, Constants.STORE_REQUIREMENTS,
                        releaseVersionService.getFinalName());
            }

            @Override
            public void storeRequirementsErrors(final Errors outcome)
            {
                log.error("[{}] -> [{}]: Error response in invocation to LibraryManager (storeRequirements): {}",
                        Constants.LIBRARY_MANAGER_CLIENT, Constants.STORE_REQUIREMENTS, outcome.getBodyExceptionMessage());
                throw new NovaException(LibraryManagerError.getUnableStoreRequirementsError(), outcome);
            }
        }, lmLibraryRequirementsDTO, releaseVersionService.getId());
    }

    @Override
    public void removeRequirements(final ReleaseVersionService releaseVersionService)
    {
        log.debug("[{}] -> [{}]: Removing requirements for service [{}]", Constants.LIBRARY_MANAGER_CLIENT,
                Constants.REMOVE_REQUIREMENTS, releaseVersionService.getFinalName());

        this.restHandler.removeRequirements(new IRestListenerLibrarymanagerapi()
        {
            @Override
            public void removeRequirements()
            {
                log.debug("[{}] -> [{}]: Requirements for service [{}] removed", Constants.LIBRARY_MANAGER_CLIENT,
                        Constants.REMOVE_REQUIREMENTS, releaseVersionService.getFinalName());
            }

            @Override
            public void removeRequirementsErrors(final Errors outcome)
            {
                log.error("[{}] -> [{}]: Error response in invocation to LibraryManager (removeRequirements): {}",
                        Constants.LIBRARY_MANAGER_CLIENT, Constants.REMOVE_REQUIREMENTS, outcome.getBodyExceptionMessage());
                throw new NovaException(LibraryManagerError.getUnableRemoveRequirementsError(), outcome);
            }

        }, releaseVersionService.getId());
    }

    @Override
    public LMLibraryRequirementsDTO getRequirements(Integer releaseVersionServiceId)
    {
        log.debug("[{}] -> [{}]: Retrieving requirements for service [{}]", Constants.LIBRARY_MANAGER_CLIENT,
                Constants.GET_REQUIREMENTS, releaseVersionServiceId);

        SingleApiClientResponseWrapper<LMLibraryRequirementsDTO> response = new SingleApiClientResponseWrapper<>();
        this.restHandler.getRequirements(new IRestListenerLibrarymanagerapi()
        {
            @Override
            public void getRequirements(LMLibraryRequirementsDTO outcome)
            {
                log.debug("[{}] -> [{}]: Requirements for service [{}] retrieved", Constants.LIBRARY_MANAGER_CLIENT,
                        Constants.GET_REQUIREMENTS, releaseVersionServiceId);
                response.set(outcome);
            }

            @Override
            public void getRequirementsErrors(final Errors outcome)
            {
                log.error("[{}] -> [{}]: Error response in invocation to LibraryManager (removeRequirements): {}",
                        Constants.LIBRARY_MANAGER_CLIENT, Constants.GET_REQUIREMENTS, outcome.getBodyExceptionMessage());
                throw new NovaException(LibraryManagerError.getUnableGetRequirementsError(), outcome);
            }

        }, releaseVersionServiceId);

        return response.get();
    }

    @Override
    public LMLibraryRequirementsDTO getRequirementsByFullName(String libraryFullName)
    {
        log.debug("[{}] -> [{}]: Retrieving requirements for library [{}]", Constants.LIBRARY_MANAGER_CLIENT,
                Constants.GET_REQUIREMENTS_BY_FULL_NAME, libraryFullName);

        SingleApiClientResponseWrapper<LMLibraryRequirementsDTO> response = new SingleApiClientResponseWrapper<>();
        this.restHandler.getLibraryRequirements(new IRestListenerLibrarymanagerapi()
        {
            @Override
            public void getLibraryRequirements(LMLibraryRequirementsDTO outcome)
            {
                log.debug("[{}] -> [{}]: Requirements for library [{}] retrieved", Constants.LIBRARY_MANAGER_CLIENT,
                        Constants.GET_REQUIREMENTS_BY_FULL_NAME, libraryFullName);
                response.set(outcome);
            }

            @Override
            public void getLibraryRequirementsErrors(final Errors outcome)
            {
                log.error("[{}] -> [{}]: Error response in invocation to LibraryManager (getLibraryRequirements): {}",
                        Constants.LIBRARY_MANAGER_CLIENT, Constants.GET_REQUIREMENTS_BY_FULL_NAME, outcome.getBodyExceptionMessage());
                throw new NovaException(LibraryManagerError.getUnableGetRequirementsError(), outcome);
            }
        }, libraryFullName);

        return response.get();
    }

    @Override
    public LMLibraryEnvironmentsDTO getLibraryEnvironments(String libraryFullName)
    {
        log.debug("[{}] -> [{}]: Retrieving environments where library [{}] is published", Constants.LIBRARY_MANAGER_CLIENT,
                Constants.GET_LIBRARY_ENVIRONMENTS, libraryFullName);

        SingleApiClientResponseWrapper<LMLibraryEnvironmentsDTO> response = new SingleApiClientResponseWrapper<>();
        this.restHandler.getPublishedEnvironments(new IRestListenerLibrarymanagerapi()
        {
            @Override
            public void getPublishedEnvironments(LMLibraryEnvironmentsDTO outcome)
            {
                log.debug("[{}] -> [{}]: Environments where library [{}] is published retrieved. Response [{}]", Constants.LIBRARY_MANAGER_CLIENT,
                        Constants.GET_LIBRARY_ENVIRONMENTS, libraryFullName, outcome);
                response.set(outcome);
            }

            @Override
            public void getPublishedEnvironmentsErrors(final Errors outcome)
            {
                log.error("[{}] -> [{}]: Error response in invocation to LibraryManager (getPublishedEnvironments): {}",
                        Constants.LIBRARY_MANAGER_CLIENT, Constants.GET_LIBRARY_ENVIRONMENTS, outcome.getBodyExceptionMessage());

                LMLibraryEnvironmentsDTO emptyLibrary = new LMLibraryEnvironmentsDTO();
                emptyLibrary.setFullName("");
                emptyLibrary.setPro(false);
                emptyLibrary.setPre(false);
                emptyLibrary.setInte(false);
                emptyLibrary.setReleaseVersionServiceId(0);

                response.set(emptyLibrary);
            }
        }, libraryFullName);

        return response.get();
    }

    /**
     * Gets libraries that a service uses
     *
     * @param rvsServiceId rvsService identifier
     * @return used library dto
     */
    @Override
    public LMLibraryEnvironmentsDTO[] getLibrariesThatServiceUse(Integer rvsServiceId, String usage)
    {
        log.debug("[{}] -> [getLibrariesThatServiceUse]: Getting libraries that RVS [{}] uses", Constants.LIBRARY_MANAGER_CLIENT, rvsServiceId);

        SingleApiClientResponseWrapper<LMLibraryEnvironmentsDTO[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandler.getLibrariesByRVService(new IRestListenerLibrarymanagerapi()
        {
            /**
             * Successful call
             *
             * @param outcome outcome
             */
            @Override
            public void getLibrariesByRVService(LMLibraryEnvironmentsDTO[] outcome)
            {
                log.debug("[{}] -> [getLibrariesByRVService]: The release version service [{}] uses this libraries: [{}]", Constants.LIBRARY_MANAGER_CLIENT, rvsServiceId, outcome);

                response.set(outcome);
            }

            /**
             * Common error call - Errors
             *
             * @param outcome outcome
             */
            @Override
            public void getLibrariesByRVServiceErrors(final Errors outcome)
            {
                log.error("[{}] -> [getLibrariesByRVService]: Error response in invocation to LibraryManager (getLibrariesByRVService):{}",
                        Constants.LIBRARY_MANAGER_CLIENT, outcome.getBodyExceptionMessage());
                throw new NovaException(LibraryManagerError.getUnableGetLibrariesOfService(), outcome);
            }
        }, rvsServiceId, usage);

        return response.get();
    }

    /**
     * Save libraries that a service uses
     *
     * @param rvsServiceId rvsService identifier
     * @param libraries    libraries that service uses
     * @return array with libraries list
     */
    @Override
    public LMUsedLibrariesDTO[] createUsedLibraries(Integer rvsServiceId, String[] libraries)
    {
        log.debug("[{}] -> [createUsedLibraries]: Saving libraries [{}] that RVS [{}] uses", Constants.LIBRARY_MANAGER_CLIENT, libraries, rvsServiceId);

        SingleApiClientResponseWrapper<LMUsedLibrariesDTO[]> usedLibraries = new SingleApiClientResponseWrapper<>();

        this.restHandler.createUsedLibraries(new IRestListenerLibrarymanagerapi()
        {
            @Override
            public void createUsedLibraries(LMUsedLibrariesDTO[] outcome)
            {
                log.debug("[{}] -> [saveUsedLibraries]: Saved libraries: [{}] that RVS [{}] uses", Constants.LIBRARY_MANAGER_CLIENT, libraries, rvsServiceId);
                usedLibraries.set(outcome);
            }

            @Override
            public void createUsedLibrariesErrors(final Errors outcome)
            {
                log.error("[{}] -> [saveUsedLibraries]: Error response in invocation to LibraryManager (saveUsedLibraries): {}",
                        Constants.LIBRARY_MANAGER_CLIENT, outcome.getBodyExceptionMessage());
                throw new NovaException(LibraryManagerError.getUnableSaveLibrariesOfService(), outcome);
            }

        }, libraries, rvsServiceId);

        return usedLibraries.get();
    }

    /**
     * Save libraries that a service uses
     *
     * @param rvsServiceId rvsService identifier
     * @param environment  environment
     */
    @Override
    public void updateUsedLibraries(Integer rvsServiceId, String environment)
    {
        log.debug("[{}] -> [createUsedLibraries]: Saving libraries that RVS [{}] uses in [{}] environment", Constants.LIBRARY_MANAGER_CLIENT, rvsServiceId, environment);

        this.restHandler.updateUsedLibraries(new IRestListenerLibrarymanagerapi()
        {
            @Override
            public void updateUsedLibraries()
            {
                log.debug("[{}] -> [saveUsedLibraries]: Saved libraries: [{}] that RVS [{}] uses", Constants.LIBRARY_MANAGER_CLIENT, environment, rvsServiceId);
            }

            @Override
            public void updateUsedLibrariesErrors(final Errors outcome)
            {
                log.error("[{}] -> [saveUsedLibraries]: Error response in invocation to LibraryManager (saveUsedLibraries): {}",
                        Constants.LIBRARY_MANAGER_CLIENT, outcome.getBodyExceptionMessage());
                throw new NovaException(LibraryManagerError.getUnableUpdateUsedLibrariesOfService(), outcome);
            }

        }, rvsServiceId, environment);

    }

    /**
     * Pubish a library on environment
     *
     * @param releaseVersionServiceId Release version service identifier
     * @param environment             Environment
     */
    @Override
    public void publishLibraryOnEnvironment(Integer releaseVersionServiceId, String environment)
    {
        log.debug("[{}] -> [publishLibraryOnEnvironment]: Publishing the library with the release version service id [{}] on [{}]", Constants.LIBRARY_MANAGER_CLIENT, releaseVersionServiceId, environment);

        LMLibraryPublicationDTO lmLibraryPublicationDTO = new LMLibraryPublicationDTO();
        lmLibraryPublicationDTO.setEnvironment(environment);
        lmLibraryPublicationDTO.setReleaseVersionServiceId(releaseVersionServiceId);

        this.restHandler.publishLibraryOnEnvironment(new IRestListenerLibrarymanagerapi()
        {
            @Override
            public void publishLibraryOnEnvironment()
            {
                log.debug("[{}] -> [publishLibraryOnEnvironment]: Release version service [{}] published on environment [{}]", Constants.LIBRARY_MANAGER_CLIENT, releaseVersionServiceId, environment);
            }

            @Override
            public void publishLibraryOnEnvironmentErrors(final Errors outcome)
            {
                log.error("[{}] -> [publishLibraryOnEnvironment]: Error response in invocation to LibraryManager (publishLibraryOnEnvironment):{}", Constants.LIBRARY_MANAGER_CLIENT, outcome.getBodyExceptionMessage());
                throw new NovaException(LibraryManagerError.getUnablePublishOnEnvironment(environment), outcome);
            }
        }, lmLibraryPublicationDTO, releaseVersionServiceId);
    }

    @Override
    public int[] getServicesUsingLibrary(final Integer rvsLibraryId, final String[] environment)
    {
        log.debug("[{}] -> [{}]: Retrieving services using library [{}] in environments [{}]", Constants.LIBRARY_MANAGER_CLIENT, Constants.GET_SERVICES_USING_LIBRARIES, rvsLibraryId, environment);

        SingleApiClientResponseWrapper<int[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandler.getServicesUsingLibrary(new IRestListenerLibrarymanagerapi()
        {
            @Override
            public void getServicesUsingLibrary(int[] outcome)
            {
                log.debug("[{}] -> [{}]: Services using library [{}] on environment [{}] are [{}]", Constants.LIBRARY_MANAGER_CLIENT, Constants.GET_SERVICES_USING_LIBRARIES, rvsLibraryId, environment, outcome);
                response.set(outcome);
            }

            @Override
            public void getServicesUsingLibraryErrors(final Errors outcome)
            {
                log.error("[{}] -> [{}]: Error response in invocation to LibraryManager ({})", Constants.LIBRARY_MANAGER_CLIENT, Constants.GET_SERVICES_USING_LIBRARIES, Constants.GET_SERVICES_USING_LIBRARIES);
                throw new NovaException(LibraryManagerError.getUnableGetServicesUsingLibrary(), outcome);
            }
        }, rvsLibraryId, environment);

        return response.get();
    }

    @Override
    public void removeLibrary(Integer rvsLibraryId)
    {
        this.restHandler.removeLibrary(new IRestListenerLibrarymanagerapi()
        {
            @Override
            public void removeLibrary()
            {
                log.debug("[{}] -> [removeLibrary]: Removed library with release version service Id: [{}]", Constants.LIBRARY_MANAGER_CLIENT, rvsLibraryId);
            }

            @Override
            public void removeLibraryErrors(final Errors outcome)
            {
                log.error("[{}] -> [removeLibrary]: Error response in invocation to LibraryManager (removeLibrary): {}",
                        Constants.LIBRARY_MANAGER_CLIENT, outcome.getBodyExceptionMessage());
                throw new NovaException(LibraryManagerError.getUnableRemoveLibrary(), outcome);
            }
        }, rvsLibraryId);
    }

    /**
     * Removes all usages of libraries by service and environment
     *
     * @param rvsServiceId Release version service identifier of a service
     * @param environment  Environment
     */
    @Override
    public void removeLibrariesUsages(Integer rvsServiceId, String environment)
    {

        this.restHandler.removeUsedLibraries(new IRestListenerLibrarymanagerapi()
        {
            @Override
            public void removeUsedLibraries()
            {
                log.debug("[{}] -> [removeLibrariesUsages]: Removed all usages by service with release version service id [{}] on [{}] environment", Constants.LIBRARY_MANAGER_CLIENT, rvsServiceId, environment);
            }

            @Override
            public void removeUsedLibrariesErrors(final Errors outcome)
            {
                log.error("[{}] -> [removeLibrary]: Error response in invocation to LibraryManager (removeLibrary)",
                        Constants.LIBRARY_MANAGER_CLIENT, outcome);
                throw new NovaException(LibraryManagerError.getUnableRemoveUsedLibraries(environment), outcome);
            }
        }, rvsServiceId, environment);
    }

    @Override
    public boolean checkUsedLibrariesAvailability(final int rvsServiceId, final String environment)
    {
        SingleApiClientResponseWrapper<Boolean> result = new SingleApiClientResponseWrapper<>(Boolean.FALSE);
        this.restHandler.checkUsedLibrariesAvailability(new IRestListenerLibrarymanagerapi()
        {
            @Override
            public void checkUsedLibrariesAvailability(final Boolean outcome)
            {
                log.debug("[{}] -> [checkUsedLibrariesAvailability]: Libraries used by Service [{}] in [{}] are {}", Constants.LIBRARY_MANAGER_CLIENT, rvsServiceId, environment, Boolean.TRUE.equals(outcome) ? "published" : "unpublished");
                result.set(outcome);
            }

            @Override
            public void checkUsedLibrariesAvailabilityErrors(final Errors outcome)
            {
                log.error("[{}] -> [checkUsedLibrariesAvailability]: Error response in invocation to LibraryManager (checkUsedLibrariesAvailability): {}",
                        Constants.LIBRARY_MANAGER_CLIENT, outcome.getBodyExceptionMessage());
                throw new NovaException(LibraryManagerError.getUnexpectedError(), outcome);
            }
        }, rvsServiceId, environment);

        return result.get();
    }

    /**
     * Get all usages of library given release version service of the library
     *
     * @param rvsLibraryId release version service of library
     * @return array with usages
     */
    @Override
    public LMUsageDTO[] getLibraryUsages(Integer rvsLibraryId)
    {
        SingleApiClientResponseWrapper<LMUsageDTO[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandler.getLibraryUsages(new IRestListenerLibrarymanagerapi()
        {
            /**
             * Successful call
             *
             * @param outcome LMUsageDTO
             */
            @Override
            public void getLibraryUsages(LMUsageDTO[] outcome)
            {
                log.debug("[{}] -> [getLibraryUsages]: Usages of library [{}] are [{}]", Constants.LIBRARY_MANAGER_CLIENT, rvsLibraryId, outcome);
                response.set(outcome);
            }

            /**
             * Common error call - Errors
             *
             * @param outcome error received
             */
            @Override
            public void getLibraryUsagesErrors(final Errors outcome)
            {
                log.error("[{}] -> [{}]: Error response in invocation to LibraryManager ({}) : {}",
                        Constants.LIBRARY_MANAGER_CLIENT, Constants.GET_SERVICES_USING_LIBRARIES, Constants.GET_SERVICES_USING_LIBRARIES, outcome.getBodyExceptionMessage());
                throw new NovaException(LibraryManagerError.getUnableGetServicesUsingLibrary(), outcome);
            }
        }, rvsLibraryId);

        return response.get();
    }

    /**
     * Gets libraries that given services use
     *
     * @param rvsServiceIdArray rvsService identifier array
     * @return used library dto
     */
    @Override
    public LMLibraryEnvironmentsDTO[] getUsedLibraries(int[] rvsServiceIdArray, String usage)
    {
        log.debug("[{}] -> [getUsedLibraries]: Getting libraries that RVS [{}] uses", Constants.LIBRARY_MANAGER_CLIENT, rvsServiceIdArray);
        SingleApiClientResponseWrapper<LMLibraryEnvironmentsDTO[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandler.getUsedLibraries(new IRestListenerLibrarymanagerapi()
        {
            /**
             * Successful call
             *
             * @param outcome outcome
             */
            @Override
            public void getUsedLibraries(LMLibraryEnvironmentsDTO[] outcome)
            {
                log.debug("[{}] -> [getUsedLibraries]: Services [{}] uses this libraries: [{}]", Constants.LIBRARY_MANAGER_CLIENT, rvsServiceIdArray, outcome);
                response.set(outcome);
            }

            /**
             * Common error call - Errors
             *
             * @param outcome outcome
             */
            @Override
            public void getUsedLibrariesErrors(final Errors outcome)
            {
                log.error("[{}] -> [getUsedLibraries]: Error response in invocation to LibraryManager (getUsedLibraries) : {}",
                        Constants.LIBRARY_MANAGER_CLIENT, outcome.getBodyExceptionMessage());
            }
        }, rvsServiceIdArray, usage);

        return response.get();
    }

    /**
     * Gets libraries that a service uses with the service id that consumes the library
     *
     * @param rvsServiceIdArray rvsService identifier array
     * @return used library dto
     */
    @Override
    public LMLibraryEnvironmentsByServiceDTO[] getUsedLibrariesByServices(int[] rvsServiceIdArray, String usage)
    {
        log.debug("[{}] -> [getUsedLibrariesByServices]: Getting libraries that RVS [{}] uses", Constants.LIBRARY_MANAGER_CLIENT, rvsServiceIdArray);
        SingleApiClientResponseWrapper<LMLibraryEnvironmentsByServiceDTO[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandler.getUsedLibrariesByServices(new IRestListenerLibrarymanagerapi()
        {
            /**
             * Successful call
             *
             * @param outcome outcome
             */
            @Override
            public void getUsedLibrariesByServices(LMLibraryEnvironmentsByServiceDTO[] outcome)
            {
                log.debug("[{}] -> [getUsedLibrariesByServices]: Services [{}] uses this libraries: [{}]", Constants.LIBRARY_MANAGER_CLIENT, rvsServiceIdArray, outcome);
                response.set(outcome);
            }

            /**
             * Common error call - Errors
             *
             * @param outcome outcome
             */
            @Override
            public void getUsedLibrariesByServicesErrors(final Errors outcome)
            {
                log.error("[{}] -> [getUsedLibrariesByServices]: Error response in invocation to LibraryManager (getUsedLibraries) : {}",
                        Constants.LIBRARY_MANAGER_CLIENT, outcome.getBodyExceptionMessage());
            }
        }, rvsServiceIdArray, usage);

        return response.get();
    }

    /**
     * Get all requirements of libraries used by an array of services
     *
     * @param releaseVersionServiceIdArray release version service ids of services
     * @return DTO with minimum requirements
     */
    @Override
    public LMLibraryRequirementsDTO[] getAllRequirementsOfUsedLibraries(int[] releaseVersionServiceIdArray)
    {
        log.debug("[{}] -> [getAllRequirementsOfUsedLibraries]: Getting minimum requirements of "
                        + "the services/libraries with releaseVersionServiceIds: [{}]",
                Constants.LIBRARY_MANAGER_CLIENT, releaseVersionServiceIdArray);
        SingleApiClientResponseWrapper<LMLibraryRequirementsDTO[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandler.getAllRequirementsOfUsedLibraries(new IRestListenerLibrarymanagerapi()
        {
            /**
             * Successful call
             *
             * @param outcome outcome
             */
            @Override
            public void getAllRequirementsOfUsedLibraries(LMLibraryRequirementsDTO[] outcome)
            {
                log.debug("[{}] -> [getAllRequirementsOfUsedLibraries]: Minimum requirements for ids [{}]:\n[{}]",
                        Constants.LIBRARY_MANAGER_CLIENT, releaseVersionServiceIdArray, outcome);
                response.set(outcome);
            }

            /**
             * Common error call - Errors
             *
             * @param outcome outcome
             */
            @Override
            public void getAllRequirementsOfUsedLibrariesErrors(final Errors outcome)
            {
                log.error(
                        "[{}] -> [getAllRequirementsOfUsedLibraries]: Error response in invocation to LibraryManager: {}",
                        Constants.LIBRARY_MANAGER_CLIENT, outcome.getBodyExceptionMessage());
                throw new NovaException(LibraryManagerError.getUnableGetServicesUsingLibrary(), outcome);
            }
        }, releaseVersionServiceIdArray);

        return response.get();

    }

    /**
     * Validate that all the requirements configured in a nova.yml are valid
     *
     * @param requirementsDTO requirements in the nova.yml
     * @return Errors reported from LibraryManager
     */
    @Override
    public LMLibraryValidationErrorDTO[] validateNovaYmlRequirements(LMLibraryNovaYmlRequirementDTO[] requirementsDTO)
    {
        log.debug("[{}] -> [validateNovaYmlRequirements]: Validating requirements: [{}]",
                Constants.LIBRARY_MANAGER_CLIENT, requirementsDTO);
        SingleApiClientResponseWrapper<LMLibraryValidationErrorDTO[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandler.validateNovaYmlRequirements(new IRestListenerLibrarymanagerapi()
        {
            /**
             * Successful call
             *
             * @param outcome outcome
             */
            @Override
            public void validateNovaYmlRequirements(LMLibraryValidationErrorDTO[] outcome)
            {
                if (outcome.length > 0)
                {
                    log.debug("[{}] -> [validateNovaYmlRequirements]: Validation errors :\n[{}]",
                            Constants.LIBRARY_MANAGER_CLIENT, outcome);
                }
                response.set(outcome);
            }

            /**
             * Common error call - Errors
             *
             * @param outcome outcome
             */
            @Override
            public void validateNovaYmlRequirementsErrors(final Errors outcome)
            {
                log.error(
                        "[{}] -> [validateNovaYmlRequirements]: Error response in invocation to LibraryManager: {}",
                        Constants.LIBRARY_MANAGER_CLIENT, outcome.getBodyExceptionMessage());
                throw new NovaException(LibraryManagerError.postUnableToValidateRequirements(), outcome);
            }
        }, requirementsDTO);

        return response.get();

    }

}
