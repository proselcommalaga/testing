package com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.impl;

import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsByServiceDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsageDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsedLibrariesDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ILibraryManagerClient;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.exceptions.LibraryManagerError;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Library manager service implementation
 */
@Slf4j
@Service
public class LibraryManagerServiceImpl implements ILibraryManagerService
{

    /**
     * Client of LibraryManager
     */
    private final ILibraryManagerClient iLibraryManagerClient;

    /**
     * Constructor by params
     *
     * @param iLibraryManagerClient client of library manager
     */
    @Autowired
    public LibraryManagerServiceImpl(final ILibraryManagerClient iLibraryManagerClient)
    {
        this.iLibraryManagerClient = iLibraryManagerClient;
    }

    @Override
    public void storeNovaLibrariesRequirements(ReleaseVersion releaseVersion)
    {
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            for (ReleaseVersionService service : subsystem.getServices())
            {
                if (ServiceType.isLibrary(service.getServiceType()))
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("[{}] -> [{}]: [LibraryManagerServiceImpl] -> [storeNovaLibrariesRequirements] Storing [{}] properties.",
                                "LibraryManagerServiceImpl",
                                "storeNovaLibrariesRequirements",
                                service.getServiceName());
                    }

                    this.iLibraryManagerClient.storeRequirements(service);
                }
            }
        }
    }

    @Override
    public void removeNovaLibrariesRequirements(ReleaseVersion releaseVersion)
    {
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            for (ReleaseVersionService service : subsystem.getServices())
            {
                if (ServiceType.isLibrary(service.getServiceType()))
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("[{}] -> [{}]: [LibraryManagerServiceImpl] -> [removeNovaLibrariesRequirements] Removing [{}] properties.",
                                "LibraryManagerServiceImpl",
                                "removeNovaLibrariesRequirements",
                                service.getServiceName());
                    }

                    this.iLibraryManagerClient.removeRequirements(service);
                }
            }
        }
    }

    @Override
    public LMLibraryRequirementsDTO getNovaLibraryRequirements(ReleaseVersionService releaseVersionService)
    {
        return this.iLibraryManagerClient.getRequirements(releaseVersionService.getId());
    }

    /**
     * Publish a library on environment
     *
     * @param releaseVersionService Release version service
     * @param deploymentPlan        Environment
     */
    @Override
    public void publishLibraryOnEnvironment(String ivUser, ReleaseVersionService releaseVersionService, DeploymentPlan deploymentPlan)
    {
        this.iLibraryManagerClient.publishLibraryOnEnvironment(releaseVersionService.getId(), deploymentPlan.getEnvironment());
    }

    @Override
    public LMLibraryRequirementsDTO getNovaLibraryRequirementsByFullName(String libraryFullName)
    {
        return this.iLibraryManagerClient.getRequirementsByFullName(libraryFullName);
    }

    @Override
    public LMLibraryEnvironmentsDTO getLibraryEnvironments(String libraryFullName)
    {
        return this.iLibraryManagerClient.getLibraryEnvironments(libraryFullName);
    }

    @Override
    public LMLibraryEnvironmentsDTO[] getUsedLibrariesByService(Integer rvsServiceId, String usage)
    {
        return this.iLibraryManagerClient.getLibrariesThatServiceUse(rvsServiceId, usage);
    }

    @Override
    public List<LMLibraryEnvironmentsDTO> getUsedLibraries(List<Integer> rvsServiceIdList, String usage)
    {
        int[] rvsServiceIdArray = rvsServiceIdList.stream().mapToInt(Integer::intValue).toArray();

        log.debug("[DeploymentAPI] -> [getUsedLibraries]: Convert list of integer to int array [{}]", rvsServiceIdArray);

        return Arrays.asList(this.iLibraryManagerClient.getUsedLibraries(rvsServiceIdArray, usage));
    }

    @Override
    public List<LMLibraryEnvironmentsByServiceDTO> getUsedLibrariesByServices(List<Integer> rvsServiceIdList, String usage)
    {
        int[] rvsServiceIdArray = rvsServiceIdList.stream().mapToInt(Integer::intValue).toArray();

        log.debug("[DeploymentAPI] -> [getUsedLibrariesByServices]: Convert list of integer to int array [{}]", rvsServiceIdArray);

        return Arrays.asList(this.iLibraryManagerClient.getUsedLibrariesByServices(rvsServiceIdArray, usage));
    }

    @Override
    public List<LMUsedLibrariesDTO> saveUsedLibraries(Integer rvsServiceId, String[] libraries)
    {
        return Arrays.asList(this.iLibraryManagerClient.createUsedLibraries(rvsServiceId, libraries));
    }

    /**
     * Removes libraies of a release version
     *
     * @param releaseVersion Release version
     */
    @Override
    public void removeLibraries(ReleaseVersion releaseVersion)
    {
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            for (ReleaseVersionService service : subsystem.getServices())
            {
                if (ServiceType.isLibrary(service.getServiceType()))
                {
                    log.debug("[{}] -> [{}]: [LibraryManagerServiceImpl] -> [removeLibraries] Removing [{}] .",
                            "LibraryManagerServiceImpl",
                            "removeLibraries",
                            service.getServiceName());

                    this.iLibraryManagerClient.removeLibrary(service.getId());
                }
            }
        }
    }

    /**
     * Removes all usages of libraries by service and environment
     *
     * @param releaseVersion Release version
     * @param usage          Usage of libraries
     */
    @Override
    public void removeLibrariesUsages(ReleaseVersion releaseVersion, String usage)
    {
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            for (ReleaseVersionService service : subsystem.getServices())
            {
                if (this.iLibraryManagerClient.getLibrariesThatServiceUse(service.getId(), usage).length > 0)
                {
                    this.iLibraryManagerClient.removeLibrariesUsages(service.getId(), usage);
                }
            }
        }
    }

    @Override
    public void checkPublishedLibrariesByDeploymentPlan(final DeploymentPlan plan, final String environment)
    {
        log.debug("[LibraryManagerServiceImpl] -> [checkPublishedLibrariesByDeploymentPlan]: checking libraries for plan [{}] in [{}] ", plan.getId(), environment);

        List<String> invalidServices = new ArrayList<>();
        for (DeploymentSubsystem subsystem : plan.getDeploymentSubsystems())
        {
            for (DeploymentService service : subsystem.getDeploymentServices())
            {
                if (!ServiceType.isLibrary(service.getService().getServiceType()))
                {
                    if (!this.iLibraryManagerClient.checkUsedLibrariesAvailability(service.getService().getId(), environment))
                    {
                        invalidServices.add(service.getService().getServiceName());
                    }
                }
            }
        }

        if (!invalidServices.isEmpty())
        {
            String message = "Services with unpublished libraries in " + environment + ": " + invalidServices;
            throw new NovaException(LibraryManagerError.getInvalidUseOfLibraryInEnvironmentError(environment), message);
        }
    }

    @Override
    public void checkPublishedLibrariesByDeploymentSubsystem(final DeploymentSubsystem deploymentService, final String environment)
    {
        log.debug("[LibraryManagerServiceImpl] -> [checkPublishedLibrariesByDeploymentPlan]: checking libraries for plan [{}] in [{}] ", deploymentService.getId(), environment);

        List<String> invalidServices = new ArrayList<>();
        for (DeploymentService service : deploymentService.getDeploymentServices())
        {
            if (!ServiceType.isLibrary(service.getService().getServiceType()))
            {
                if (!this.iLibraryManagerClient.checkUsedLibrariesAvailability(service.getService().getId(), environment))
                {
                    invalidServices.add(service.getService().getServiceName());
                }
            }
        }

        if (!invalidServices.isEmpty())
        {
            String message = "Services with unpublished libraries in " + environment + ": " + invalidServices;
            throw new NovaException(LibraryManagerError.getInvalidUseOfLibraryInEnvironmentError(environment), message);
        }
    }

    @Override
    public void checkPublishedLibrariesByDeploymentService(final DeploymentService deploymentService, final String environment)
    {
        log.debug("[LibraryManagerServiceImpl] -> [checkPublishedLibrariesByDeploymentPlan]: checking libraries for plan [{}] in [{}] ", deploymentService.getId(), environment);

        if (!ServiceType.isLibrary(deploymentService.getService().getServiceType()))
        {
            if (!this.iLibraryManagerClient.checkUsedLibrariesAvailability(deploymentService.getService().getId(), environment))
            {
                String message = "Service with unpublished libraries in " + environment + ": " + deploymentService;
                throw new NovaException(LibraryManagerError.getInvalidUseOfLibraryInEnvironmentError(environment), message);
            }
        }
    }

    /**
     * Get all usages of library
     *
     * @param rvsLibraryId release version service of library
     * @return array of usages
     */
    @Override
    public LMUsageDTO[] getLibraryUsages(Integer rvsLibraryId)
    {
        return this.iLibraryManagerClient.getLibraryUsages(rvsLibraryId);
    }

    /**
     * Get all requirements of libraries used by a list of services
     *
     * @param releaseVersionServiceIdArray release version service id of service
     * @return DTO with minimum requirements
     */
    @Override
    public LMLibraryRequirementsDTO[] getAllRequirementsOfUsedLibraries(int[] releaseVersionServiceIdArray)
    {
        return this.iLibraryManagerClient.getAllRequirementsOfUsedLibraries(releaseVersionServiceIdArray);
    }
}
