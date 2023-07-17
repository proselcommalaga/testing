package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.datamodel.model.monitoring.entities.LogEventService;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.LogEventServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ProductReleaseUtils;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Validator for releaseversionsapi.
 */
@Slf4j
@Service
public class ReleaseVersionValidatorImpl implements IReleaseVersionValidator
{
    /**
     * Maxmum number of release versions
     */
    private static final int MAXIMUM_SIMULTANEOUS_COMPILATIONS = 1;

    /**
     * Deployment plan repository
     */
    @Autowired
    private DeploymentPlanRepository deploymentPlanRepository;

    /**
     * Release version repository
     */
    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    /**
     * Release version subsytem repository
     */
    @Autowired
    private ReleaseVersionSubsystemRepository releaseVersionSubsystemRepository;

    /**
     * Log Event Service repository
     */
    @Autowired
    private LogEventServiceRepository logEventServiceRepository;

    @Override
    public void checkReleaseVersionExistance(ReleaseVersion releaseVersion)
    {
        if (releaseVersion == null)
        {
            log.error("[{}] -> [{}]: The ReleaseVersion does not exist", Constants.RELEASE_VERSION_VALIDATOR,
                    "checkReleaseVersionExistance");
            throw new NovaException(ReleaseVersionError.getNoSuchReleaseVersionError(-1),
                    "[ReleaseVersionValidatorImpl] -> [checkReleaseVersionExistance]: release version provided is null");
        }
    }

    @Override
    public void checkRelaseVersionStatusNotCompiling(ReleaseVersion releaseVersion)
    {
        ReleaseVersionStatus status = releaseVersion.getStatus();
        if (ReleaseVersionStatus.BUILDING == status)
        {
            log.error("[{}] -> [{}]: The ReleaseVersion status  BUILDING", Constants.RELEASE_VERSION_VALIDATOR, "checkRelaseVersionStatusNotCompiling");
            throw new NovaException(ReleaseVersionError.getReleaseStatusBuildingError(), "Release version is building");
        }
    }

    @Override
    public void existsReleaseVersionWithSameName(Release release, String releaseVersionName)
    {
        this.checkExistence(release, releaseVersionName);
    }

    @Override
    public void checkIfReleaseVersionCanBeStored(int versionId, ReleaseVersion releaseVersion)
    {
        boolean hasPlanNotStorage = this.deploymentPlanRepository.releaseVersionHasPlanNotStorage(versionId);
        boolean hasTasksAndConf = this.deploymentPlanRepository.releaseVersionHasTasks(versionId);

        if (hasPlanNotStorage || hasTasksAndConf || releaseVersion.getDeployments().isEmpty())
        {
            log.error("[{}] -> [{}]: Release version was never deployed or has associated tasks: hasPlanNotStorage - [{}], hasTaskAndConf - [{}], deploymentsEmpty - [{}]",
                    Constants.RELEASE_VERSION_VALIDATOR, "checkIfReleaseVersionCanBeStored", hasPlanNotStorage, hasTasksAndConf, releaseVersion.getDeployments().isEmpty());
            throw new NovaException(ReleaseVersionError.getStorageDeployedReleaseError(), "Release version cannot be storage due to: It was never deployed | Has associated config tasks in PENDING or PENDING_ERROR status | All deployments plans are not in STORAGE status");
        }

    }

    @Override
    public void checkMaxReleaseVersions(int productId, int releaseSlots)
    {
        // Get maximum number of release versions
        int maxVersion = ProductReleaseUtils.getMaxReleaseVersions(releaseSlots);

        if (this.releaseVersionRepository.countByProductIdAndStatusNot(productId, ReleaseVersionStatus.STORAGED) >= maxVersion)
        {
            log.error("[{}] -> [{}]: Reached the maximum of release version: Currently - [{}] vs Max admitted [{}]",
                    Constants.RELEASE_VERSION_VALIDATOR, "checkMaxReleaseVersions", this.releaseVersionRepository
                            .countByProductIdAndStatusNot(productId, ReleaseVersionStatus.STORAGED), maxVersion);
            throw new NovaException(ReleaseVersionError.getMaxVersionsLimitError(),
                    "There are too many release versions from product with id " + productId + ". Please, archive them");
        }
    }

    @Override
    public void checkCompilingReleaseVersions(int productId)
    {
        if (this.releaseVersionRepository.countByProductIdAndStatus(productId, ReleaseVersionStatus.BUILDING) >= MAXIMUM_SIMULTANEOUS_COMPILATIONS)
        {
            log.error("[{}] -> [{}]: Reached the maximum of simultaneous compilation: currently - [{}] vs Max admitted [{}] ",
                    Constants.RELEASE_VERSION_VALIDATOR, "checkCompilingReleaseVersions", this.releaseVersionRepository.countByProductIdAndStatus(productId, ReleaseVersionStatus.BUILDING)
                    , MAXIMUM_SIMULTANEOUS_COMPILATIONS);
            throw new NovaException(ReleaseVersionError.getMaxVersionCompilingError(),
                    "There are too many release versions compiling at the same time from product with id " + productId + ". Please, wait" +
                            "till the compiling process ends.");
        }
    }

    @Override
    public void checkReleaseVersionHasPlan(final ReleaseVersion releaseVersion)
    {
        if (!releaseVersion.getDeployments().isEmpty())
        {
            log.error("[{}] -> [{}]: Error trying to delete a release version: [{}] due has still deployment plans: [{}] in some environment. For removing, it must not have any deployment plan.",
                    Constants.RELEASE_VERSION_VALIDATOR, "checkReleaseVersionHasPlan", releaseVersion.getVersionName(), releaseVersion.getDeployments());
            throw new NovaException(ReleaseVersionError.getDeleteDeployedReleaseError(), "Release version cannot be deleted due to still exists deployment plans associated to this relese version.");
        }
    }

    /**
     * Check if exist any release version, and remove his relation form logeventservice table
     *
     * @param releaseVersion release version
     */
    @Override
    public void checkReleaseVersionHasLogEvent(ReleaseVersion releaseVersion)
    {
        List<ReleaseVersionSubsystem> releaseVersionSubsystemList = this.releaseVersionSubsystemRepository.findByReleaseVersionId(releaseVersion.getId());
        releaseVersionSubsystemList.stream()
                .map(ReleaseVersionSubsystem::getServices).flatMap(Collection::stream)
                .map(releaseVersionService -> this.logEventServiceRepository.findByReleaseVersionServiceId(releaseVersionService.getId()))
                .filter(logEventServiceList -> !logEventServiceList.isEmpty())
                .forEach(logEventServiceList -> {
                    log.debug("removing logEventServices [{}] from releaseVersion [{}]", logEventServiceList.toArray(new LogEventService[logEventServiceList.size()]), releaseVersion);
                    this.logEventServiceRepository.deleteAll(logEventServiceList);
                });
    }

    @Override
    @Transactional
    public ReleaseVersionSubsystem saveAndFlushReleaseVersionSubsystem(final ReleaseVersionSubsystem releaseVersionSubsystem)
    {
        log.debug("[{}] -> [saveAndFlushReleaseVersionSubsystem]: saving release version subsystem. The new status of release version subsystem: [{}] - release version subsystem id: [{}]", Constants.RELEASE_VERSION_VALIDATOR, releaseVersionSubsystem.getStatus(), releaseVersionSubsystem.getId());
        ReleaseVersionSubsystem savedReleaseVersionSubsystem = this.releaseVersionSubsystemRepository.saveAndFlush(releaseVersionSubsystem);
        log.debug("[{}] -> [saveAndFlushReleaseVersionSubsystem]: saved release version subsystem. The new status of release version subsystem: [{}] - release version subsystem id: [{}]", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, savedReleaseVersionSubsystem.getStatus(), savedReleaseVersionSubsystem.getId());

        return savedReleaseVersionSubsystem;
    }

    @Override
    @Transactional
    public ReleaseVersion saveAndFlushReleaseVersion(final ReleaseVersion releaseVersion)
    {
        log.debug("[{}] -> [saveAndFlushReleaseVersion]: saving release version. Release version id: [{}]", Constants.RELEASE_VERSION_VALIDATOR, releaseVersion.getId());
        ReleaseVersion savedReleaseVersion = this.releaseVersionRepository.saveAndFlush(releaseVersion);
        log.debug("[{}] -> [saveAndFlushReleaseVersion]: saved release version. The new status of release version subsystem: [{}] - release version id: [{}]", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, savedReleaseVersion.getStatus(), savedReleaseVersion.getId());

        return savedReleaseVersion;
    }

    /////////////////////////////////// PRIVATE METHODS ///////////////////////////////////////

    /**
     * check existence
     *
     * @param release            release
     * @param releaseVersionName release version name
     */
    private void checkExistence(Release release, String releaseVersionName)
    {
        for (ReleaseVersion releaseVersion : release.getReleaseVersions())
        {
            if (releaseVersion.getVersionName().equals(releaseVersionName))
            {
                log.error("[{}] -> [{}]: There is a release version with the name {} in the release", Constants.RELEASE_VERSION_VALIDATOR,
                        "checkExistence", releaseVersionName);
                throw new NovaException(ReleaseVersionError.getReleaseVersionNameDuplicatedError(), "Release version name is duplicated");
            }
        }
    }
}
