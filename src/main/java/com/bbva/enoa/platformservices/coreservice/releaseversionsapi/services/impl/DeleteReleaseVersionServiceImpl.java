package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.IApiManagerService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.impl.QualityManagerService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IBatchScheduleService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IDeleteReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Delete Release Version Service
 */
@Service
@Slf4j
public class DeleteReleaseVersionServiceImpl implements IDeleteReleaseVersionService
{
    @Autowired
    private IReleaseVersionValidator iReleaseVersionValidator;

    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    @Autowired
    private QualityManagerService qualityManagerService;

    @Autowired
    private IBatchScheduleService batchScheduleService;

    @Autowired
    private ILibraryManagerService iLibraryManagerService;

    @Autowired
    private IApiManagerService apiManagerService;

    @Override
    @Transactional
    public synchronized void deleteReleaseVersion(String ivUser, Integer releaseVersionId)
    {
        // Get the release version.
        ReleaseVersion releaseVersion = this.releaseVersionRepository.findById(releaseVersionId).orElseThrow(() ->
        {
            throw new NovaException(ReleaseVersionError.getNoSuchReleaseVersionError(releaseVersionId));
        });

        // Check if the Release Version is storaged and does not have plans
        this.iReleaseVersionValidator.checkReleaseVersionHasPlan(releaseVersion);

        // check if the Release Version has any Log Event
        this.iReleaseVersionValidator.checkReleaseVersionHasLogEvent(releaseVersion);

        // Delete SQA
        this.qualityManagerService.removeQualityInfo(releaseVersion);

        // Delete Batch scheduler data
        this.batchScheduleService.deleteBatchScheduleServices(releaseVersion);

        // Delete libraries that a release use
        this.iLibraryManagerService.removeLibrariesUsages(releaseVersion, Constants.BUILD.toUpperCase());

        // Delete subsystems with type LIBRARY
        this.iLibraryManagerService.removeLibraries(releaseVersion);

        // All release version api versions are recovered before the release version deletion to avoid hibernate conflicts
        // Stream<ApiVersion<?,?,?>> rvApiVersion =  releaseVersion.getAllApiVersions();

        // Delete the version.
        this.releaseVersionRepository.delete(releaseVersion);

        // Updates state of the api version in the RV
        this.apiManagerService.refreshUnimplementedApiVersionsState(releaseVersion);

    }
}
