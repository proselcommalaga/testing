package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;

/**
 * Created by xe30000 on 21/02/2017.
 */
public interface IReleaseVersionDtoBuilderService
{

    /**
     * Set build jenkinsJobGroupStatus.
     * Check if the release version is completely built.
     * If all subsystesms are built, check quality validation
     *
     * @param subsystemId                Subsystem ID
     * @param jenkinsJobGroupMessageInfo message info with the results of the all jenkins job of hte group
     * @param jenkinsJobGroupStatus      Status of the jenkins job that represent a group
     * @param ivUser                     user requester
     */
    void subsystemBuildStatus(Integer subsystemId, String jenkinsJobGroupMessageInfo,
                              String jenkinsJobGroupStatus, String ivUser);

    /**
     * Build the subsystem of the product version.
     *
     * @param product        product associated to the release
     * @param releaseVersion Releaseversion
     * @param ivUser         user that generate the problem
     */
    void buildSubsystems(Product product, ReleaseVersion releaseVersion, String ivUser);

    /**
     * Process template
     *
     * @param releaseVersion {@link ReleaseVersion}
     * @param ivUser         user requester
     */
    void processTemplates(ReleaseVersion releaseVersion, String ivUser);
}
