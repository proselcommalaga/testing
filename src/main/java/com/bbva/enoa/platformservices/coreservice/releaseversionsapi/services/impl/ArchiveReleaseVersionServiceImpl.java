package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IArchiveReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Archive release version service
 */
@Service
@Slf4j
public class ArchiveReleaseVersionServiceImpl implements IArchiveReleaseVersionService
{
    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    @Autowired
    private IReleaseVersionValidator iReleaseVersionValidator;

    @Override
    @Transactional
    public void archiveReleaseVersion(String ivUser, Integer versionId)
    {
        log.debug("[{}] -> [{}]: Begin archiveReleaseVersion [{}] ", Constants.ARCHIVE_RELEASE_VERSION_SERVICE, "archiveReleaseVersion",
                versionId);

        // Get the release version.
        ReleaseVersion releaseVersion = this.releaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new NovaException(ReleaseVersionError.getNoSuchReleaseError()));

        // Check whether the version can be stored or not.
        this.iReleaseVersionValidator.checkIfReleaseVersionCanBeStored(versionId, releaseVersion);

        // Check that the RV is not compiling
        this.iReleaseVersionValidator.checkRelaseVersionStatusNotCompiling(releaseVersion);

        // Change the status ReleaseVersionStatus
        releaseVersion.setStatusDescription(ReleaseVersionStatus.STORAGED.toString());
        releaseVersion.setStatus(ReleaseVersionStatus.STORAGED);
        this.releaseVersionRepository.save(releaseVersion);
    }

}
