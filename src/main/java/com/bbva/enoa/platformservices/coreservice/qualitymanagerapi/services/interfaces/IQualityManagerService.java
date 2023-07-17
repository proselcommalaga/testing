package com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.interfaces;

import com.bbva.enoa.apirestgen.qualitymanagerapi.model.CodeAnalysisStatus;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.PerformanceReport;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.PlanPerformanceReport;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.ReleaseVersionCodeAnalyses;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;

/**
 * Service for getting quality information
 */
public interface IQualityManagerService
{
    /**
     * Get release version SQA information
     *
     * @param releaseVersionId release version id
     * @return code analyses
     */
    ReleaseVersionCodeAnalyses getReleaseVersionCodeAnalyses(final Integer releaseVersionId);

    /**
     * For each release version, get whether it satisfies static code analysis
     *
     * @param releaseVersionIdList The list of release version IDs
     * @return List of code analysis status
     */
    CodeAnalysisStatus[] getCodeAnalysisStatuses(int[] releaseVersionIdList);

    /**
     * Checks quality state for all services in the release version
     * returns NOT_AVAILABLE if there is a problem getting quality state for one service
     * returns OK if all services pass quality
     * returns ERROR if, at least, one service does not pass quality
     *
     * @param releaseVersion      release version
     * @param includesNotCompiled if process not compiled services or not
     * @return quality state (OK, ERROR or NOT_AVAILABLE)
     */
    String checkReleaseVersionQualityState(ReleaseVersion releaseVersion, boolean includesNotCompiled);

    /**
     * Checks quality state for all services in the release version
     * returns NOT_AVAILABLE if there is a problem getting quality state for one service
     * returns OK if all services pass quality
     * returns ERROR if, at least, one service does not pass quality
     *
     * @param behaviorVersion     behavior version
     * @param includesNotCompiled if process not compiled services or not
     * @return quality state (OK, ERROR or NOT_AVAILABLE)
     */
    String checkBehaviorVersionQualityState(BehaviorVersion behaviorVersion, boolean includesNotCompiled);

    /**
     * Get release version SQA information
     *
     * @param ivUser            user
     * @param planId            plan id
     * @param performanceReport Performance report dto
     */
    void setPerformanceReport(final String ivUser, final Integer planId, final PerformanceReport performanceReport);

    /**
     * Delete a performance report
     *
     * @param ivUser user
     * @param planId plan Id
     */
    void deletePerformanceReport(final String ivUser, final Integer planId);

    /**
     * Get performance report
     *
     * @param releaseVersionId release version id
     * @return list of performance reports
     */
    PlanPerformanceReport[] getPerformanceReports(final Integer releaseVersionId);


    /**
     * Remove quality information
     *
     * @param releaseVersion release version
     */
    void removeQualityInfo(final ReleaseVersion releaseVersion);

    /**
     * Request code analysis of services in a subsystem
     *
     * @param ivUser      user
     * @param subsystemId subsystem id
     * @param branchName  branch name
     */
    void requestSubsystemCodeAnalysis(String ivUser, Integer subsystemId, String branchName);
}
