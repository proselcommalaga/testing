package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsByServiceDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryNovaYmlRequirementDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryValidationErrorDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsageDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsedLibrariesDTO;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;

import javax.annotation.PostConstruct;

/**
 * Library Manager Client interface
 */
public interface ILibraryManagerClient
{
    /**
     * Init the handler and listener.
     */
    @PostConstruct
    void init();

    /**
     * Store the given requirements for the given library service.
     *
     * @param releaseVersionService release version service data.
     */
    void storeRequirements(final ReleaseVersionService releaseVersionService);

    /**
     * Remove existing stored requirements for the given library service.
     *
     * @param releaseVersionService release version service data.
     */
    void removeRequirements(final ReleaseVersionService releaseVersionService);

    /**
     * Retrieve existing requirements for the given library service.
     *
     * @param releaseVersionServiceId release version service unique identifier.
     * @return list of requirements stored for the given service
     */
    LMLibraryRequirementsDTO getRequirements(Integer releaseVersionServiceId);

    /**
     * Pubish a library on environment
     *
     * @param releaseVersionServiceId Release version service identifier
     * @param environment             Environment
     */
    void publishLibraryOnEnvironment(final Integer releaseVersionServiceId, final String environment);

    /**
     * Retrieve existing requirements for the given library full name.
     *
     * @param libraryFullName Library full name
     * @return LMLibraryRequirementsDTO
     */
    LMLibraryRequirementsDTO getRequirementsByFullName(final String libraryFullName);

    /**
     * Gets all environments where given library is published
     *
     * @param libraryFullName Library full name
     * @return LMLibraryEnvironmentsDTO
     */
    LMLibraryEnvironmentsDTO getLibraryEnvironments(final String libraryFullName);

    /**
     * Gets libraries that a service uses
     *
     * @param rvsServiceId rvsService identifier
     * @param usage        environment where service use the library
     * @return used library dto
     */
    LMLibraryEnvironmentsDTO[] getLibrariesThatServiceUse(final Integer rvsServiceId, String usage);

    /**
     * Save libraries that a service uses
     *
     * @param rvsServiceId rvsService identifier
     * @param libraries    libraries that service uses
     * @return array with libraries
     */
    LMUsedLibrariesDTO[] createUsedLibraries(final Integer rvsServiceId, String[] libraries);

    /**
     * Save libraries that a service uses
     *
     * @param rvsServiceId rvsService identifier
     * @param environment  libraries that service uses
     */
    void updateUsedLibraries(final Integer rvsServiceId, String environment);

    /**
     * Get services identifier using library
     *
     * @param rvsLibraryId release version services for library
     * @param environment  environment
     * @return services identifiers array
     */
    int[] getServicesUsingLibrary(final Integer rvsLibraryId, final String[] environment);

    /**
     * Removes a library by release version service identifier
     *
     * @param rvsLibraryId release version service identifier of library
     */
    void removeLibrary(final Integer rvsLibraryId);

    /**
     * Removes all usages of libraries by service and environment
     *
     * @param rvsServiceId Release version service identifier of a service
     * @param environment  Environment
     */
    void removeLibrariesUsages(final Integer rvsServiceId, final String environment);

    /**
     * Check if a service uses libraries and if they are published in the desired environment
     *
     * @param rvsServiceId service identified by release version service id
     * @param environment  environment as String to check libraries
     * @return <code>true</code> if everything is ok
     * <code>false</code> if service contains almost one library unpublished into environment
     */
    boolean checkUsedLibrariesAvailability(final int rvsServiceId, final String environment);

    /**
     * Get all usages of library given release version service of the library
     *
     * @param rvsLibraryId      release version service of library
     * @return                  array with usages
     */
    LMUsageDTO[] getLibraryUsages(final Integer rvsLibraryId);

    /**
     * Gets libraries that a service uses
     *
     * @param rvsServiceIdArray rvsService identifier
     * @param usage             environment where service use the library
     * @return used library dto
     */
    LMLibraryEnvironmentsDTO[] getUsedLibraries(final int[] rvsServiceIdArray, String usage);

    /**
     * Gets libraries that a service uses with the service id that consumes the library
     *
     * @param rvsServiceIdArray rvsService identifier
     * @param usage             environment where service use the library
     * @return used library dto
     */
    LMLibraryEnvironmentsByServiceDTO[] getUsedLibrariesByServices(final int[] rvsServiceIdArray, String usage);

    /**
     * Get all requirements of libraries used by an array of services
     *
     * @param releaseVersionServiceIdArray release version service ids of services
     * @return DTO with minimum requirements
     */
    LMLibraryRequirementsDTO[] getAllRequirementsOfUsedLibraries(int[] releaseVersionServiceIdArray);

    /**
     * Validate that all the requirements configured in a nova.yml are valid
     *
     * @param requirements requirements in the nova.yml
     * @return Errors reported from LibraryManager
     */
    LMLibraryValidationErrorDTO[] validateNovaYmlRequirements(LMLibraryNovaYmlRequirementDTO[] requirements);
}
