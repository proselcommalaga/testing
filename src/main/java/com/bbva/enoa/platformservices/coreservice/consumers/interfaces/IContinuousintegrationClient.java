package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.continuousintegrationapi.model.CIJenkinsBuildSnapshotDTO;
import com.bbva.enoa.apirestgen.continuousintegrationapi.model.CIJobDTO;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;

import javax.annotation.PostConstruct;

/**
 * Frontend for the Continuous Integration API client.
 * Created by xe52580 on 15/02/2017.
 */
public interface IContinuousintegrationClient
{
    /**
     * Init the handler and listener.
     */
    @PostConstruct
    void init();

    /**
     * Build any subsystems type. (NOVA and EPHOENIX subsystems)
     *
     * @param product        - the product associated to the release.
     * @param releaseVersion - the product releaseVersion
     * @param ivUser         - user that generated the call
     * @return - true if at least one subsystem was built, false otherwise
     */
    boolean buildSubsystems(Product product, ReleaseVersion releaseVersion, String ivUser);

    /**
     * Build BEHAVIOR_TEST subsystems type.
     *
     * @param product         - the product associated to the version.
     * @param behaviorVersion - the product behaviorVersion
     * @param ivUser          - user that generated the call
     * @return - true if at least one subsystem was built, false otherwise
     */
    boolean buildBehaviorSubsystems(Product product, BehaviorVersion behaviorVersion, String ivUser);

    /**
     * Get statistics related to the Build Jobs stored in the platform, such as the total number or their Status.
     * The results can be filtered by UUAA and Type.
     *
     * @param daysAgo Filter the Jobs this number of days ago.
     * @param jobType Filter the results for a specific Type. If its equals to "ALL", or it's null, or it's empty, no status filtering is applied.
     * @param uuaa    Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @return An array of CIJobDTO.
     */
    CIJobDTO[] getJobsSinceDaysAgo(Integer daysAgo, String jobType, String uuaa);

    /**
     * Gets an array of DTOs with information to saved in statistic history loading job, for Jenkins compilations info.
     *
     * @return an array of DTOs with Jenkins compilations history info.
     */
    CIJenkinsBuildSnapshotDTO[] getBuildsSnapshots();
}
