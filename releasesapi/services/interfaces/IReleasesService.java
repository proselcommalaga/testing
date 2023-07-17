package com.bbva.enoa.platformservices.coreservice.releasesapi.services.interfaces;

import com.bbva.enoa.apirestgen.releasesapi.model.NewReleaseRequest;
import com.bbva.enoa.apirestgen.releasesapi.model.RELReleaseInfo;
import com.bbva.enoa.apirestgen.releasesapi.model.ReleaseConfigDto;
import com.bbva.enoa.apirestgen.releasesapi.model.ReleaseDto;
import com.bbva.enoa.apirestgen.releasesapi.model.ReleaseDtoByEnvironment;
import com.bbva.enoa.apirestgen.releasesapi.model.ReleaseVersionInListDto;
import com.bbva.enoa.platformservices.coreservice.releasesapi.util.ReleaseConstants;

public interface IReleasesService
{

    /**
     * Gets the release info
     *
     * @param releaseId - The release id
     * @return - a ReleaseDto object with information of the release
     */
    ReleaseDto releaseInfo(Integer releaseId);

    /**
     * @param ivUser        logged user
     * @param releaseConfig release config to update
     * @param releaseId     id of the release to update
     * @param environment   environment where the release will be updated
     */
    void updateReleaseConfig(String ivUser, ReleaseConfigDto releaseConfig,
                             Integer releaseId, ReleaseConstants.ENVIRONMENT environment);

    /**
     * Return an array of {@code ReleaseVersionInListDto} with the release versions of the release passed as argument.
     *
     * @param releaseId - Release id
     * @param status    the status of the release version to filter by. Optional
     * @return - an array of ReleaseVersionInListDtos of the release
     */
    ReleaseVersionInListDto[] getReleaseVersions(Integer releaseId, String status);

    /**
     * Return a list of Release info with the release version and services info
     *
     * @param productId Product Id
     * @return List of Releases of the selected product
     */
    RELReleaseInfo[] getAllReleasesAndServices(Integer productId);

    /**
     * Get all.
     *
     * @param ivUser    user
     * @param productId The ID of the Product the Releases belong to.
     * @return Success
     */
    ReleaseDto[] getProductReleases(String ivUser, Integer productId);

    /**
     * Delete one.
     *
     * @param ivUser    User code
     * @param releaseId The ID of the Release to delete.
     */
    void deleteRelease(String ivUser, Integer releaseId);

    /**
     * Returns the list of available CPDs by the selected environment
     *
     * @param environment environment to select the cpd
     * @return a List with the active cpds
     */
    String[] getCpds(String environment);

    /**
     * Create a new one.
     *
     * @param ivUser       User code
     * @param releaseToAdd Release to create and add to a Product.
     */
    void createRelease(String ivUser, NewReleaseRequest releaseToAdd);

    /**
     * Get all CPDS
     *
     * @return An Array of available CPDs
     */
    String[] getCpdsHistorical();

    /**
     * Return a list of Release info with the release version and services info
     *
     * @param productId Product Id
     * @return List of Releases of the selected product
     */
    Integer getReleasesMaxVersions(Integer productId);


    ReleaseDtoByEnvironment[] getReleasesProductConfiguration(String ivUser, Integer productId, String environment);

}
