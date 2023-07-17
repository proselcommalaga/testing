package com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces;

import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsByServiceDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsageDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsedLibrariesDTO;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;

import java.util.List;

/**
 * Library Manager service interface
 */
public interface ILibraryManagerService
{
    /**
     * Store NOVA libraries requirements - if any.
     *
     * @param releaseVersion release version
     */
    void storeNovaLibrariesRequirements(ReleaseVersion releaseVersion);

    /**
     * Remove NOVA libraries requirements - if any.
     *
     * @param releaseVersion release version
     */
    void removeNovaLibrariesRequirements(ReleaseVersion releaseVersion);

    /**
     * Retrieve NOVA library requirements.
     *
     * @param releaseVersionService release version
     * @return list of requirements stored for the given service
     */
    LMLibraryRequirementsDTO getNovaLibraryRequirements(ReleaseVersionService releaseVersionService);

    /**
     * Publish a library on environment
     *
     * @param releaseVersionService Release version service
     * @param deploymentPlan        Plan
     * @param ivUser                bbva usercode
     */
    void publishLibraryOnEnvironment(final String ivUser, final ReleaseVersionService releaseVersionService, final DeploymentPlan deploymentPlan);

    /**
     * Retrieve NOVA library requirements.
     *
     * @param libraryFullName library full name
     * @return list of requirements stored for the given service
     */
    LMLibraryRequirementsDTO getNovaLibraryRequirementsByFullName(final String libraryFullName);

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
     * @param rvsServiceId RVS identifier
     * @param usage        usage
     * @return Array with libraries full name
     */
    LMLibraryEnvironmentsDTO[] getUsedLibrariesByService(final Integer rvsServiceId, final String usage);

    /**
     * Gets libraries that a list of services use
     *
     * @param rvsServiceIdList RVS identifier list
     * @param usage            usage
     * @return Array with libraries full name
     */
    List<LMLibraryEnvironmentsDTO> getUsedLibraries(final List<Integer> rvsServiceIdList, final String usage);

    /**
     * Gets libraries that a service uses with the service id that consumes the library
     *
     * @param rvsServiceIdList RVS identifier list
     * @param usage            usage
     * @return Array with libraries full name
     */
    List<LMLibraryEnvironmentsByServiceDTO> getUsedLibrariesByServices(final List<Integer> rvsServiceIdList, final String usage);

    /**
     * Save libraries that a RVS uses
     *
     * @param rvsServiceId RVS identifier
     * @param libraries    library fullname list
     * @return array with libraries
     */
    List<LMUsedLibrariesDTO> saveUsedLibraries(final Integer rvsServiceId, final String[] libraries);

    /**
     * Removes libraies of a release version
     *
     * @param releaseVersion Release version
     */
    void removeLibraries(final ReleaseVersion releaseVersion);

    /**
     * Removes all usages of libraries by release version and environment
     *
     * @param releaseVersion Release version
     * @param environment    Environment
     */
    void removeLibrariesUsages(final ReleaseVersion releaseVersion, final String environment);

    /**
     * Given a deployment plan, it checks if his services using libraries and this are ready to use (published)
     *
     * @param plan        DeploymentPlan with services to promote, deploy, copy, ...
     * @param environment next environment
     */
    void checkPublishedLibrariesByDeploymentPlan(final DeploymentPlan plan, final String environment);

    /**
     * Given a deployment subsystem, it checks if his services using libraries and this are ready to use (published)
     *
     * @param deploymentSubsystem DeploymentPlan with services to promote, deploy, copy, ...
     * @param environment         environment
     */
    void checkPublishedLibrariesByDeploymentSubsystem(final DeploymentSubsystem deploymentSubsystem, final String environment);


    /**
     * Given a deployment service, it checks if his services using libraries and this are ready to use (published)
     *
     * @param deploymentService DeploymentPlan with services to promote, deploy, copy, ...
     * @param environment       environment
     */
    void checkPublishedLibrariesByDeploymentService(final DeploymentService deploymentService, final String environment);
    /**
     * Get all usages of library
     *
     * @param rvsLibraryId  release version service of library
     * @return  array of usages
     */
    LMUsageDTO[] getLibraryUsages(final Integer rvsLibraryId);

    /**
     * Get all requirements of libraries used by an array of services
     *
     * @param releaseVersionServiceIdArray release version service ids of service
     * @return DTO with minimum requirements
     */
    LMLibraryRequirementsDTO[] getAllRequirementsOfUsedLibraries(int[] releaseVersionServiceIdArray);

}
