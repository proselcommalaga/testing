package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.qualityassuranceapi.model.*;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.CodeAnalysis;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.PlanPerformanceReport;

public interface IQualityAssuranceClient
{
    void requestSubsystemCodeAnalysis(QASubsystemCodeAnalysisRequest subsystemCodeAnalysisRequestInfo, Integer subsystemId);

    void requestEphoenixSubsystemCodeAnalysis(QAEphoenixSubsystemCodeAnalysisRequest subsystemCodeAnalysisRequestInfo, Integer subsystemId);

    /**
     * Set performance report
     *
     * @param qaPerformanceReport performance report dto
     * @param planId              plan id
     * @param releaseVersionId    release version id
     */
    void setPerformanceReport(QAPerformanceReport qaPerformanceReport, Integer planId, Integer releaseVersionId);

    /**
     * Get list of performance reports
     *
     * @param releaseVersionId release version id
     * @return array of performance report
     */
    PlanPerformanceReport[] getPerformanceReports(Integer releaseVersionId);

    /**
     * Delete performance report by plan
     *
     * @param planId plan id
     */
    void deletePerformanceReport(Integer planId);

    /**
     * Delete all performance reports by release version
     *
     * @param releaseVersionId release version
     */
    void deletePerformanceReportsByReleaseVersion(Integer releaseVersionId);

    /**
     * Remove all code analyses of a release version service
     *
     * @param releaseVersionServiceId release version service id
     */
    void removeCodeAnalyses(Integer releaseVersionServiceId);

    /**
     * Gets code analyses by release version service
     *
     * @param releaseVersionServiceId release version service id
     * @return array of code analyses
     */
    CodeAnalysis[] getCodeAnalysesByReleaseVersionService(Integer releaseVersionServiceId);

    /**
     * Get the list of Subsystem Code Analyses created the specified number of days ago, and apply some other optional filters.
     *
     * @param daysAgo Filter the Subsystem Code Analyses this number of days ago.
     * @param status  Filter the Subsystem Code Analyses only in this status. If it's "ALL" or not present, don't apply this filter.
     * @return An array of QASubsystemCodeAnalysis.
     */
    QASubsystemCodeAnalysis[] getSubsystemCodeAnalysesSinceDaysAgo(Integer daysAgo, String status);

    /**
     * Check exist and higher than sonar 5
     *
     * @param projectName    SQA project name
     * @param serviceVersion service version
     * @return sonar version
     */
    boolean checkIfExistAndHigherSonar5(String projectName, String serviceVersion);

    /**
     * Create code analyses for a release version service. Error are ignored
     *
     * @param releaseVersionServiceId release version service id
     * @param qaCodeAnalysisRequest   code analysis creation info
     * @return qa code analysis
     */
    QACodeAnalysis createCodeAnalysis(final Integer releaseVersionServiceId, final QACodeAnalysisRequest qaCodeAnalysisRequest);

    /**
     * Create code analyses for a behavior version service. Error are ignored
     *
     * @param behaviorVersionServiceId behavior version service id
     * @param qaCodeAnalysisRequest    code analysis creation info
     * @return qa code analysis
     */
    QACodeAnalysis createBehaviorCodeAnalysis(final Integer behaviorVersionServiceId, final QACodeAnalysisRequest qaCodeAnalysisRequest);
    
    /**
     * Get performance report by document link
     *
     * @param link url of document
     * @return performance report
     */
    QAPlanPerformanceReport getPerformanceReportsByLink(String link);

}
