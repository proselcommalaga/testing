package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.qualityassuranceapi.client.feign.nova.rest.IRestHandlerQualityassuranceapi;
import com.bbva.enoa.apirestgen.qualityassuranceapi.client.feign.nova.rest.IRestListenerQualityassuranceapi;
import com.bbva.enoa.apirestgen.qualityassuranceapi.client.feign.nova.rest.impl.RestHandlerQualityassuranceapi;
import com.bbva.enoa.apirestgen.qualityassuranceapi.model.*;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.*;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IQualityAssuranceClient;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.exception.QualityManagerErrorCode;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util.QualityConstants;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.exceptions.StatisticsError;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Quality Assurance Client Service
 *
 * @author XE56809
 */
@Service
public class QualityAssuranceClientImpl implements IQualityAssuranceClient
{
    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(QualityAssuranceClientImpl.class);
    private static final List<String> SUBSYSTEM_ANALYSIS_ERROR_CODES = List.of(QualityConstants.QualityManagerErrors.QA_MAXIMUM_SUBSYSTEM_CODE_ANALYSIS_ERROR_CODE, QualityConstants.QualityManagerErrors.QA_SUBSYSTEM_ALREADY_BUILDING_ERROR_CODE);

    /**
     * Error task manager
     */
    @Autowired
    private IErrorTaskManager errorTaskMgr;

    /**
     * Attribute - Rest Handler - Interface
     */
    @Autowired
    private IRestHandlerQualityassuranceapi iRestHandler;

    /**
     * Handler of the client of the API
     */
    private RestHandlerQualityassuranceapi restHandler;

    /**
     * Constructor. Init the listeners.
     */
    @PostConstruct
    public void init()
    {
        // Task service client handler and listener
        this.restHandler = new RestHandlerQualityassuranceapi(this.iRestHandler);
    }

    @Override
    public void requestSubsystemCodeAnalysis(QASubsystemCodeAnalysisRequest subsystemCodeAnalysisRequestInfo, Integer subsystemId)
    {
        LOGGER.debug("[QualityAssuranceAPI Client] -> [requestSubsystemCodeAnalysis] Requesting code analysis for subsystem [{}]", subsystemId);
        this.restHandler.requestSubsystemCodeAnalysis(new IRestListenerQualityassuranceapi()
        {
            @Override
            public void requestSubsystemCodeAnalysis()
            {
                LOGGER.debug("[QualityAssuranceAPI Client] -> [requestSubsystemCodeAnalysis] Requested successfully code analysis for subsystem [{}]", subsystemId);
            }

            @Override
            public void requestSubsystemCodeAnalysisErrors(final Errors outcome)
            {
                LOGGER.error("[QualityAssuranceAPI Client] -> [requestSubsystemCodeAnalysis] Error requesting code analysis for subsystem [{}] - subsystem info: [{}]. Error message: [{}]", subsystemId, subsystemCodeAnalysisRequestInfo, outcome.getBodyExceptionMessage());
                throwExceptionFrom(outcome);
            }
        }, subsystemCodeAnalysisRequestInfo, subsystemId);
    }

    @Override
    public void requestEphoenixSubsystemCodeAnalysis(QAEphoenixSubsystemCodeAnalysisRequest subsystemCodeAnalysisRequestInfo, Integer subsystemId)
    {
        LOGGER.debug("[QualityAssuranceAPI Client] -> [requestEphoenixSubsystemCodeAnalysis] Requesting code analysis for ePhoenix subsystem [{}]", subsystemId);
        this.restHandler.requestEphoenixSubsystemCodeAnalysis(new IRestListenerQualityassuranceapi()
        {
            @Override
            public void requestEphoenixSubsystemCodeAnalysis()
            {
                LOGGER.debug("[QualityAssuranceAPI Client] -> [requestEphoenixSubsystemCodeAnalysis] Requested successfully code analysis for ePhoenix subsystem [{}]", subsystemId);
            }

            @Override
            public void requestEphoenixSubsystemCodeAnalysisErrors(final Errors outcome)
            {
                LOGGER.error("[QualityAssuranceAPI Client] -> [requestEphoenixSubsystemCodeAnalysis] Error requesting code analysis for ePhoenix subsystem id: [{}] - subsystem info: [{}]. Error message: [{}]", subsystemId, subsystemCodeAnalysisRequestInfo, outcome.getBodyExceptionMessage());
                throwExceptionFrom(outcome);
            }
        }, subsystemCodeAnalysisRequestInfo, subsystemId);
    }

    private void throwExceptionFrom(Errors outcome)
    {
        Optional<ErrorMessage> firstErrorMessage = outcome.getFirstErrorMessage();
        ErrorMessage errorMessage = firstErrorMessage.orElseGet(() -> {
            ErrorMessage errorMsg = new ErrorMessage();
            errorMsg.setCode("");
            return errorMsg;
        });
        String code = errorMessage.getCode();
        if (!SUBSYSTEM_ANALYSIS_ERROR_CODES.contains(code))
        {
            throw new NovaException(QualityManagerErrorCode.getQANotAvailableError());
        }
        if (SUBSYSTEM_ANALYSIS_ERROR_CODES.get(0).equalsIgnoreCase(code))
        {
            throw new NovaException(QualityManagerErrorCode.getMaximumSubsystemCodeAnalysisError());
        }
        if (SUBSYSTEM_ANALYSIS_ERROR_CODES.get(1).equalsIgnoreCase(code))
        {
            throw new NovaException(QualityManagerErrorCode.getSubsystemAlreadyBuildingError());
        }
    }

    @Override
    public void setPerformanceReport(final QAPerformanceReport qaPerformanceReport, final Integer planId, final Integer releaseVersionId)
    {
        LOGGER.debug("[QualityAssuranceClientService] -> [setPerformanceReport]: setting performance report for plan [{}] to performance report: [{}]", planId, qaPerformanceReport);

        this.restHandler.setPerformanceReport(new IRestListenerQualityassuranceapi()
        {
            @Override
            public void setPerformanceReport()
            {
                LOGGER.debug("[QualityAssuranceClientService] -> [setPerformanceReport]: set successfully performance report for plan [{}] and performance report [{}]", planId, qaPerformanceReport);
            }

            @Override
            public void setPerformanceReportErrors(final Errors outcome)
            {
                LOGGER.error("[QualityAssuranceClientService] -> [setPerformanceReport]: there was an error setting performance report. Deployment plan id: [{}] - release version id: [{}] - Error message: [{}]", planId, releaseVersionId, outcome.getBodyExceptionMessage());
                throw new NovaException(QualityManagerErrorCode.getQANotAvailableError());
            }
        }, qaPerformanceReport, planId, releaseVersionId);
    }

    @Override
    public PlanPerformanceReport[] getPerformanceReports(final Integer releaseVersionId)
    {
        LOGGER.debug("[QualityAssuranceClient] -> [getPerformanceReports]: getting performance reports for release version id [{}]", releaseVersionId);

        SingleApiClientResponseWrapper<PlanPerformanceReport[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandler.getPerformanceReports(new IRestListenerQualityassuranceapi()
        {
            @Override
            public void getPerformanceReports(final QAPlanPerformanceReport[] outcome)
            {
                LOGGER.debug("[QualityAssuranceClient] -> [getPerformanceReports]: got successfully performance reports for release version id [{}]", releaseVersionId);
                response.set(convertToPlanPerformanceReportArray(outcome));
            }

            @Override
            public void getPerformanceReportsErrors(final Errors outcome)
            {
                LOGGER.error("[QualityAssuranceClientService] -> [getPerformanceReports]: cannot get the performance report. Release version id: [{}] - Error message: [{}]", releaseVersionId, outcome.getBodyExceptionMessage());
                throw new NovaException(QualityManagerErrorCode.getQANotAvailableError());
            }
        }, releaseVersionId);

        return response.get();
    }

    @Override
    public void deletePerformanceReport(final Integer planId)
    {
        LOGGER.debug("[QualityAssuranceClientService] -> [deletePerformanceReport]: deleting performance report for plan [{}]", planId);

        this.restHandler.deletePerformanceReport(new IRestListenerQualityassuranceapi()
        {
            @Override
            public void deletePerformanceReport()
            {
                LOGGER.debug("[QualityAssuranceClientService] -> [deletePerformanceReport]: deleted successfully performance report for plan [{}]", planId);
            }

            @Override
            public void deletePerformanceReportErrors(final Errors outcome)
            {
                LOGGER.error("[QualityAssuranceClientService] -> [deletePerformanceReportErrors]: cannot delete the performance report. Deployment plan id: [{}] -  Error message: [{}]", planId, outcome.getBodyExceptionMessage());
                throw new NovaException(QualityManagerErrorCode.getQANotAvailableError());
            }
        }, planId);
    }

    @Override
    public void deletePerformanceReportsByReleaseVersion(final Integer releaseVersionId)
    {
        LOGGER.debug("[QualityAssuranceClientService] -> [deletePerformanceReportByReleaseVersion]: deleting performance report for release version [{}]", releaseVersionId);

        this.restHandler.deletePerformanceReportsByReleaseVersion(new IRestListenerQualityassuranceapi()
        {
            @Override
            public void deletePerformanceReportsByReleaseVersion()
            {
                LOGGER.debug("[QualityAssuranceClientService] -> [deletePerformanceReportsByReleaseVersion]: deleted successfully all performance reports for release version [{}]", releaseVersionId);
            }

            @Override
            public void deletePerformanceReportsByReleaseVersionErrors(final Errors outcome)
            {
                LOGGER.error("[QualityAssuranceClientService] -> [deletePerformanceReportsByReleaseVersion]: cannot delete the performance report by release version id: [{}]. Error message: [{}]", releaseVersionId, outcome.getBodyExceptionMessage());
                throw new NovaException(QualityManagerErrorCode.getQANotAvailableError());
            }
        }, releaseVersionId);
    }

    @Override
    public void removeCodeAnalyses(final Integer releaseVersionServiceId)
    {
        final AtomicReference<Errors> response = new AtomicReference<>();
        this.restHandler.removeCodeAnalyses(new IRestListenerQualityassuranceapi()
        {
            @Override
            public void removeCodeAnalyses()
            {
                LOGGER.debug("[QualityAssuranceClientService] -> [removeCodeAnalysis]: the Quality Assurance API service response was success for release version service id: [{}]", releaseVersionServiceId);
            }

            @Override
            public void removeCodeAnalysesErrors(final Errors outcome)
            {
                LOGGER.error("[QualityAssuranceClientService] -> [removeCodeAnalysis]: error trying to connect to Quality Assurance API to remove analysis for release version service id [{}] and error: ", releaseVersionServiceId, outcome);
                response.set(outcome);
            }
        }, releaseVersionServiceId);

        if (response.get() != null)
        {
            // Create an error task
            String message = "[QualityAssuranceClientService] -> [removeCodeAnalysis]: Error trying to connect to Quality Assurance to remove analysis for release version service id" + releaseVersionServiceId + ". Error message: " + response.get().getMessage();
            LOGGER.error(message);
            errorTaskMgr.createErrorTask(null, QualityManagerErrorCode.getQANotAvailableError(), message, ToDoTaskType.INTERNAL_ERROR, response.get());
        }
    }

    @Override
    public CodeAnalysis[] getCodeAnalysesByReleaseVersionService(final Integer releaseVersionServiceId)
    {
        LOGGER.debug("[QualityAssuranceClient] -> [getCodeAnalysesByReleaseVersionService]: getting code analyses for release version service id [{}]", releaseVersionServiceId);

        AtomicReference<CodeAnalysis[]> response = new AtomicReference<>();
        this.restHandler.getCodeAnalysesByReleaseVersionService(new IRestListenerQualityassuranceapi()
        {
            @Override
            public void getCodeAnalysesByReleaseVersionService(final QACodeAnalysis[] outcome)
            {
                LOGGER.debug("[QualityAssuranceClient] -> [getCodeAnalysesByReleaseVersionService]: obtained successfully code analyses for release version service id [{}]: [{}]",
                        releaseVersionServiceId, Arrays.toString(outcome));
                response.set(convertToCodeAnalysisArray(outcome));
            }

            @Override
            public void getCodeAnalysesByReleaseVersionServiceErrors(final Errors outcome)
            {
                LOGGER.error("[QualityAssuranceClient] -> [getCodeAnalysesByReleaseVersionService]: error getting code analyses for release version service id [{}]. Error message: [{}]",
                        releaseVersionServiceId, outcome.getBodyExceptionMessage());
                throw new NovaException(QualityManagerErrorCode.getQANotAvailableError());
            }
        }, releaseVersionServiceId);

        return response.get();
    }

    @Override
    public QACodeAnalysis createCodeAnalysis(final Integer releaseVersionServiceId, final QACodeAnalysisRequest qaCodeAnalysisRequest)
    {
        LOGGER.debug("[QualityAssuranceClient] -> [createCodeAnalysis]: creating code analyses for release version service id [{}]", releaseVersionServiceId);

        AtomicReference<QACodeAnalysis> response = new AtomicReference<>();

        this.restHandler.createCodeAnalysisForReleaseVersionService(new IRestListenerQualityassuranceapi()
        {
            @Override
            public void createCodeAnalysisForReleaseVersionService(final QACodeAnalysis outcome)
            {
                LOGGER.debug("[QualityAssuranceClient] -> [createCodeAnalysis]: created successfully code analyses for release version service id [{}]",
                        releaseVersionServiceId);
                response.set(outcome);
            }

            @Override
            public void createCodeAnalysisForReleaseVersionServiceErrors(final Errors outcome)
            {
                LOGGER.warn("[QualityAssuranceClient] -> [createCodeAnalysis]: error getting code analyses for release version service id [{}]. QACodeAnalysis request: [{}] - Error message: [{}]",
                        releaseVersionServiceId, qaCodeAnalysisRequest, outcome.getBodyExceptionMessage());
                response.set(null);
            }
        }, qaCodeAnalysisRequest, releaseVersionServiceId);

        return response.get();
    }

    @Override
    public QACodeAnalysis createBehaviorCodeAnalysis(final Integer behaviorVersionServiceId, final QACodeAnalysisRequest qaCodeAnalysisRequest)
    {
        LOGGER.debug("[createBehaviorCodeAnalysis] -> [createBehaviorCodeAnalysis]: creating code analyses for behavior version service id [{}]", behaviorVersionServiceId);

        AtomicReference<QACodeAnalysis> response = new AtomicReference<>();

        this.restHandler.createCodeAnalysisForBehaviorVersionService(new IRestListenerQualityassuranceapi()
        {
            @Override
            public void createCodeAnalysisForBehaviorVersionService(final QACodeAnalysis outcome)
            {
                LOGGER.debug("[QualityAssuranceClient] -> [createBehaviorCodeAnalysis]: created successfully code analyses for behavior version service id [{}]",
                        behaviorVersionServiceId);
                response.set(outcome);
            }

            @Override
            public void createCodeAnalysisForBehaviorVersionServiceErrors(final Errors outcome)
            {
                LOGGER.warn("[QualityAssuranceClient] -> [createBehaviorCodeAnalysis]: error getting code analyses for behavior version service id [{}]. QACodeAnalysis request: [{}] - Error message: [{}]",
                        behaviorVersionServiceId, qaCodeAnalysisRequest, outcome.getBodyExceptionMessage());
                response.set(null);
            }
        }, qaCodeAnalysisRequest, behaviorVersionServiceId);

        return response.get();
    }

    @Override
    public QAPlanPerformanceReport getPerformanceReportsByLink(final String link)
    {
        LOGGER.debug("[QualityAssuranceClient] -> [getPerformanceReportsByLink]: getting performance report of link [{}]", link);

        SingleApiClientResponseWrapper<QAPlanPerformanceReport> response = new SingleApiClientResponseWrapper<>();

        this.restHandler.getPerformanceReportsByLink(new IRestListenerQualityassuranceapi()
        {
            @Override
            public void getPerformanceReportsByLink(final QAPlanPerformanceReport outcome)
            {
                LOGGER.debug("[QualityAssuranceClient] -> [getPerformanceReportsByLink]: got successfully performance report of link [{}]", link);
                response.set(outcome);
            }

            @Override
            public void getPerformanceReportsByLinkErrors(final Errors outcome)
            {
                LOGGER.error("[QualityAssuranceClient] -> [getPerformanceReportsByLink]: cannot get the performance report. Link: [{}] - Error message: [{}]", link, outcome.getBodyExceptionMessage());
                throw new NovaException(QualityManagerErrorCode.getQANotAvailableError());
            }
        }, link);

        return response.get();
    }

    @Override
    public QASubsystemCodeAnalysis[] getSubsystemCodeAnalysesSinceDaysAgo(Integer daysAgo, String status)
    {
        SingleApiClientResponseWrapper<QASubsystemCodeAnalysis[]> response = new SingleApiClientResponseWrapper<>();

        LOGGER.info("[QualityAssuranceClientImpl] -> [getSubsystemCodeAnalysesSinceDaysAgo]: getting Subsystem Code Analyses since [{}] days ago, with Status [{}]", daysAgo, status);

        this.restHandler.getSubsystemCodeAnalysesSinceDaysAgo(new IRestListenerQualityassuranceapi()
        {
            @Override
            public void getSubsystemCodeAnalysesSinceDaysAgo(QASubsystemCodeAnalysis[] outcome)
            {
                LOGGER.info("[QualityAssuranceClientImpl] -> [getSubsystemCodeAnalysesSinceDaysAgo]: successfully got Subsystem Code Analyses since [{}] days ago, with Status [{}]", daysAgo, status);
                response.set(outcome);
            }

            @Override
            public void getSubsystemCodeAnalysesSinceDaysAgoErrors(Errors outcome)
            {
                LOGGER.error("[QualityAssuranceClientImpl] -> [getSubsystemCodeAnalysesSinceDaysAgo]: error trying to get Subsystem Code Analyses since [{}] days ago, with Status [{}]: {}", daysAgo, status, outcome.getBodyExceptionMessage());
                throw new NovaException(StatisticsError.getContinuousIntegrationError(), outcome);
            }

        }, daysAgo, status);

        return response.get();
    }

    @Override
    public boolean checkIfExistAndHigherSonar5(String projectName, String serviceVersion)
    {
        LOGGER.info("[QualityAssuranceClient] -> [checkIfExistAndHigherSonar5]: check exist and higher than sonar 5 for project name [{}] and service version [{}]",
                projectName, serviceVersion);

        AtomicReference<Boolean> response = new AtomicReference<>();
        this.restHandler.checkIfExistAndHigherSonar5(new IRestListenerQualityassuranceapi()
        {
            @Override
            public void checkIfExistAndHigherSonar5(Boolean outcome)
            {
                LOGGER.info("[QualityAssuranceClientImpl] -> [checkIfExistAndHigherSonar5]: successfully check exist and higher than sonar 5 for project name [{}] and service name [{}]",
                        projectName, serviceVersion);
                response.set(outcome);
            }

            @Override
            public void checkIfExistAndHigherSonar5Errors(Errors outcome)
            {
                LOGGER.error("[QualityAssuranceClientImpl] -> [checkIfExistAndHigherSonar5]: error trying to check exist and higher than sonar 5 for project name [{}] and service name [{}]. Error: {} ",
                        projectName, serviceVersion, outcome.getBodyExceptionMessage());
                throw new NovaException(StatisticsError.getSonarVersionError(), outcome);
            }
        }, serviceVersion, projectName);

        return response.get();
    }

    /////////////////////////////////////////// PRIVATE METHODS /////////////////////////////////

    private PlanPerformanceReport[] convertToPlanPerformanceReportArray(QAPlanPerformanceReport[] performanceReports)
    {
        return Arrays.stream(performanceReports).map(qaPlanPerformanceReport -> {
            PlanPerformanceReport planPerformanceReport = new PlanPerformanceReport();
            planPerformanceReport.setPlanId(qaPlanPerformanceReport.getPlanId());

            QAPerformanceReport qaPerformanceReport = qaPlanPerformanceReport.getPerformanceReport();
            PerformanceReport performanceReport = new PerformanceReport();
            performanceReport.setRiskLevel(qaPerformanceReport.getRiskLevel());
            performanceReport.setLink(qaPerformanceReport.getLink());
            performanceReport.setDescription(qaPerformanceReport.getDescription());
            performanceReport.setCreationDate(qaPerformanceReport.getCreationDate());
            planPerformanceReport.setPerformanceReport(performanceReport);
            return planPerformanceReport;
        }).toArray(PlanPerformanceReport[]::new);
    }

    /**
     * Convert array of QACodeAnalysis to array of CodeAnalysis
     *
     * @param qaArray array of QACodeAnalysis
     * @return array of CodeAnalysis
     */
    private CodeAnalysis[] convertToCodeAnalysisArray(final QACodeAnalysis[] qaArray)
    {
        List<CodeAnalysis> qualityAnalysisList = new ArrayList<>();
        if (qaArray != null)
        {
            for (QACodeAnalysis qaCodeAnalysis : qaArray)
            {
                CodeAnalysis qualityAnalysis = new CodeAnalysis();

                qualityAnalysis.setId(qaCodeAnalysis.getId());
                qualityAnalysis.setSqaState(qaCodeAnalysis.getSqaState());
                qualityAnalysis.setSonarVersion(qaCodeAnalysis.getSonarVersion());
                qualityAnalysis.setAnalysisDate(qaCodeAnalysis.getAnalysisDate());
                qualityAnalysis.setProjectId(qaCodeAnalysis.getProjectId());
                qualityAnalysis.setProjectName(qaCodeAnalysis.getProjectName());
                qualityAnalysis.setLanguage(qaCodeAnalysis.getLanguage());
                qualityAnalysis.setProjectUrl(qaCodeAnalysis.getProjectUrl());
                qualityAnalysis.setSonarState(qaCodeAnalysis.getSonarState());
                qualityAnalysis.setIndicatorsState(convertQAIndicatorsState(qaCodeAnalysis.getIndicatorsState()));
                qualityAnalysis.setHasSonarReport(qaCodeAnalysis.getHasSonarReport());

                qualityAnalysisList.add(qualityAnalysis);
            }
        }

        return qualityAnalysisList.toArray(new CodeAnalysis[0]);
    }


    private CodeAnalysisIndicators convertQAIndicatorsState(QAIC qaIndicatorsState)
    {
        if (qaIndicatorsState == null)
        {
            return new CodeAnalysisIndicators();
        }

        CodeAnalysisIndicators indicators = new CodeAnalysisIndicators();
        indicators.setIccm(convertQAIndicatorDetail(qaIndicatorsState.getIccm()));
        indicators.setIcdc(convertQAIndicatorDetail(qaIndicatorsState.getIcdc()));
        indicators.setIcpu(convertQAIndicatorDetail(qaIndicatorsState.getIcpu()));
        indicators.setIcr(convertQAIndicatorDetail(qaIndicatorsState.getIcr()));

        return indicators;
    }

    private CodeAnalysisIndicatorDetail convertQAIndicatorDetail(QAICDetail indicatorDetail)
    {
        CodeAnalysisIndicatorDetail analysisIndicatorDetail = new CodeAnalysisIndicatorDetail();
        analysisIndicatorDetail.setValue(indicatorDetail.getValue());
        analysisIndicatorDetail.setLimit(indicatorDetail.getLimit());

        return analysisIndicatorDetail;
    }
}
