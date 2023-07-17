package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces;

import com.bbva.enoa.apirestgen.behaviorapi.model.*;

/**
 * Behavior Service
 * Process all the logic of the Behavior API
 */
public interface IBehaviorService
{

    /**
     * Builds a DTO for create a new Behavior version with data from
     * a subsystems with their tags
     *
     * @param ivUser    BBVA user code.
     * @param productId product identifier.
     * @return BVRequestDTO with the data for create a new behavior version.
     */
    BVRequestDTO newBehaviorVersionRequest(final String ivUser, final Integer productId);

    /**
     * Build a DTO with the response of validate tag
     *
     * @param ivUser         BBVA user code
     * @param productId      product identifier
     * @param versionRequest DTO with tags to validate.
     * @return BVValidationResponseDTO with all validation
     */
    BVValidationResponseDTO validateRequestTag(final String ivUser, final Integer productId, final BVSubsystemTagDTO versionRequest);

    /**
     * Stores the new behavior version in DB.
     *
     * @param behaviorVersion Version to create.
     * @param ivUser          BBVA user code.
     * @param productId       The ID of the product version.
     */
    void buildBehaviorVersion(final BVBehaviorVersionDTO behaviorVersion, final String ivUser, final Integer productId);

    /**
     * Get a detailed DTO for a behavior version.
     *
     * @param productId product identifier
     * @param status    the status of the behavior version to filter by
     * @return return all behavior versions by given product
     */
    BVBehaviorVersionSummaryInfoDTO[] getAllBehaviorVersions(final Integer productId, final String status);

    /**
     * Get a detailed DTO for a behavior version.
     *
     * @param behaviorVersionId The ID of the behavior version.
     * @return BVBehaviorVersionSummaryInfoDTO with behavior version summary info to show in the cards.
     */
    BVBehaviorVersionSummaryInfoDTO getBehaviorVersion(final Integer behaviorVersionId);

    /**
     * Delete behavior version
     *
     * @param ivUser            BBVA user code.
     * @param behaviorVersionId The ID of the behavior version.
     */
    void deleteBehaviorVersion(String ivUser, Integer behaviorVersionId);

    /**
     * Update behavior test subsystem build status.
     *
     * @param ivUser                       BBVA user code.
     * @param subsystemId                  Subsystem id.
     * @param behaviorSubsystemBuildStatus Build info.
     */
    void updateBehaviorTestSubsystemBuildStatus(final String ivUser, final Integer subsystemId, BVBehaviorSubsystemBuildStatus behaviorSubsystemBuildStatus);

    /**
     * Check behavior budgets boolean.
     *
     * @param behaviorServiceId the behavior service id
     * @return the boolean
     */
    Boolean checkBehaviorBudgets(Integer behaviorServiceId);


    /**
     * Get behavior instance by given id
     *
     * @param behaviorInstanceId behavior instance identifier
     * @return behavior instance dto info
     */
    BVBehaviorInstanceDTO getBehaviorInstance(final Integer behaviorInstanceId);

}
