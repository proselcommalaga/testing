package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.qualityassuranceapi.client.feign.nova.rest.IRestHandlerQualityassuranceapi;
import com.bbva.enoa.apirestgen.qualityassuranceapi.model.*;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.CodeAnalysis;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.PerformanceReport;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.PlanPerformanceReport;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util.QualityConstants;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class QualityAssuranceClientImplTest
{
    @Mock
    private IErrorTaskManager errorTaskMgr;
    @Mock
    private IRestHandlerQualityassuranceapi restHandler;
    @Mock
    private Logger log;
    @InjectMocks
    private QualityAssuranceClientImpl client;

    @BeforeEach
    public void init() throws IllegalAccessException, NoSuchFieldException
    {
        MockitoAnnotations.initMocks(QualityAssuranceClientImpl.class);
        client.init();

        Field field = client.getClass().getDeclaredField("LOGGER");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(client, log);
    }

    @Test
    public void when_request_subsystem_code_analysis_returns_ko_response_and_there_are_no_error_messages_then_throw_generic_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.setMessages(Collections.emptyList());
        Mockito.when(restHandler.requestSubsystemCodeAnalysis(Mockito.any(QASubsystemCodeAnalysisRequest.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.requestSubsystemCodeAnalysis(new QASubsystemCodeAnalysisRequest(), 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(QualityConstants.QualityManagerErrors.QA_NOT_AVAILABLE_ERROR_CODE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_request_subsystem_code_analysis_returns_ko_response_with_another_error_message_then_throw_generic_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode("AAAA");
        Mockito.when(restHandler.requestSubsystemCodeAnalysis(Mockito.any(QASubsystemCodeAnalysisRequest.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.requestSubsystemCodeAnalysis(new QASubsystemCodeAnalysisRequest(), 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(QualityConstants.QualityManagerErrors.QA_NOT_AVAILABLE_ERROR_CODE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_request_subsystem_code_analysis_returns_ko_response_with_maximum_subsystem_code_analysis_error_message_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(QualityConstants.QualityManagerErrors.QA_MAXIMUM_SUBSYSTEM_CODE_ANALYSIS_ERROR_CODE);
        Mockito.when(restHandler.requestSubsystemCodeAnalysis(Mockito.any(QASubsystemCodeAnalysisRequest.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.requestSubsystemCodeAnalysis(new QASubsystemCodeAnalysisRequest(), 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(QualityConstants.QualityManagerErrors.QA_MAXIMUM_SUBSYSTEM_CODE_ANALYSIS_ERROR_CODE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_request_subsystem_code_analysis_returns_ko_response_with_subsystem_already_building_error_message_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(QualityConstants.QualityManagerErrors.QA_SUBSYSTEM_ALREADY_BUILDING_ERROR_CODE);
        Mockito.when(restHandler.requestSubsystemCodeAnalysis(Mockito.any(QASubsystemCodeAnalysisRequest.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.requestSubsystemCodeAnalysis(new QASubsystemCodeAnalysisRequest(), 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(QualityConstants.QualityManagerErrors.QA_SUBSYSTEM_ALREADY_BUILDING_ERROR_CODE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_request_subsystem_code_analysis_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.requestSubsystemCodeAnalysis(Mockito.any(QASubsystemCodeAnalysisRequest.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.requestSubsystemCodeAnalysis(new QASubsystemCodeAnalysisRequest(), 1);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_request_ephoenix_subsystem_code_analysis_returns_ko_response_and_there_are_no_error_messages_then_throw_generic_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.setMessages(Collections.emptyList());
        Mockito.when(restHandler.requestEphoenixSubsystemCodeAnalysis(Mockito.any(QAEphoenixSubsystemCodeAnalysisRequest.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.requestEphoenixSubsystemCodeAnalysis(new QAEphoenixSubsystemCodeAnalysisRequest(), 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(QualityConstants.QualityManagerErrors.QA_NOT_AVAILABLE_ERROR_CODE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_request_ephoenix_ssubsystem_code_analysis_returns_ko_response_with_another_error_message_then_throw_generic_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode("AAAA");
        Mockito.when(restHandler.requestEphoenixSubsystemCodeAnalysis(Mockito.any(QAEphoenixSubsystemCodeAnalysisRequest.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.requestEphoenixSubsystemCodeAnalysis(new QAEphoenixSubsystemCodeAnalysisRequest(), 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(QualityConstants.QualityManagerErrors.QA_NOT_AVAILABLE_ERROR_CODE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_request_ephoenix_ssubsystem_code_analysis_returns_ko_response_with_maximum_subsystem_code_analysis_error_message_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(QualityConstants.QualityManagerErrors.QA_MAXIMUM_SUBSYSTEM_CODE_ANALYSIS_ERROR_CODE);
        Mockito.when(restHandler.requestEphoenixSubsystemCodeAnalysis(Mockito.any(QAEphoenixSubsystemCodeAnalysisRequest.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.requestEphoenixSubsystemCodeAnalysis(new QAEphoenixSubsystemCodeAnalysisRequest(), 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(QualityConstants.QualityManagerErrors.QA_MAXIMUM_SUBSYSTEM_CODE_ANALYSIS_ERROR_CODE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_request_ephoenix_ssubsystem_code_analysis_returns_ko_response_with_subsystem_already_building_error_message_then_throw_specific_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        errors.getMessages().get(0).setCode(QualityConstants.QualityManagerErrors.QA_SUBSYSTEM_ALREADY_BUILDING_ERROR_CODE);
        Mockito.when(restHandler.requestEphoenixSubsystemCodeAnalysis(Mockito.any(QAEphoenixSubsystemCodeAnalysisRequest.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR));

        try
        {
            client.requestEphoenixSubsystemCodeAnalysis(new QAEphoenixSubsystemCodeAnalysisRequest(), 1);
        }
        catch (NovaException e)
        {
            Assertions.assertEquals(QualityConstants.QualityManagerErrors.QA_SUBSYSTEM_ALREADY_BUILDING_ERROR_CODE, e.getErrorCode().getErrorCode());
            return;
        }

        Assertions.fail("Unexpected end of test.");
    }

    @Test
    public void when_request_ephoenix_ssubsystem_code_analysis_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.requestEphoenixSubsystemCodeAnalysis(Mockito.any(QAEphoenixSubsystemCodeAnalysisRequest.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.requestEphoenixSubsystemCodeAnalysis(new QAEphoenixSubsystemCodeAnalysisRequest(), 1);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_set_performance_report_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.setPerformanceReport(Mockito.any(QAPerformanceReport.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.setPerformanceReport(new QAPerformanceReport(), 1, 1));
    }

    @Test
    public void when_set_performance_report_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.setPerformanceReport(Mockito.any(QAPerformanceReport.class), Mockito.anyInt(), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.setPerformanceReport(new QAPerformanceReport(), 1, 1);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt(), Mockito.any(QAPerformanceReport.class));
    }

    @Test
    public void when_get_performance_reports_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getPerformanceReports(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getPerformanceReports(1));
    }

    @Test
    public void when_get_performance_reports_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.getPerformanceReports(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyQAPlanPerformanceReports(), HttpStatus.OK));

        PlanPerformanceReport[] result = client.getPerformanceReports(1);

        Assertions.assertEquals(1, result.length);
        PlanPerformanceReport firstResult = result[0];
        Assertions.assertEquals(1, firstResult.getPlanId());
        PerformanceReport performanceReport = firstResult.getPerformanceReport();
        Assertions.assertEquals("DESCRIPTION", performanceReport.getDescription());
        Assertions.assertEquals("LINK", performanceReport.getLink());
        Assertions.assertEquals(1000L, performanceReport.getCreationDate());
        Assertions.assertEquals(1, performanceReport.getRiskLevel());
    }

    @Test
    public void when_delete_performance_report_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.deletePerformanceReport(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.deletePerformanceReport(1));
    }

    @Test
    public void when_delete_performance_report_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.deletePerformanceReport(Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.deletePerformanceReport(1);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_delete_performance_reports_by_release_version_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.deletePerformanceReportsByReleaseVersion(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.deletePerformanceReportsByReleaseVersion(1));
    }

    @Test
    public void when_delete_performance_reports_by_release_version_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.deletePerformanceReportsByReleaseVersion(Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.deletePerformanceReportsByReleaseVersion(1);

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_remove_code_analyses_returns_ko_response_then_create_error_task()
    {
        Mockito.when(restHandler.removeCodeAnalyses(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        client.removeCodeAnalyses(1);

        Mockito.verify(errorTaskMgr, Mockito.times(1)).createErrorTask(Mockito.isNull(), Mockito.any(NovaError.class), Mockito.anyString(), Mockito.any(ToDoTaskType.class), Mockito.any(Errors.class));
    }

    @Test
    public void when_remove_code_analyses_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.removeCodeAnalyses(Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.removeCodeAnalyses(1);

        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_get_code_analyses_by_release_version_service_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getCodeAnalysesByReleaseVersionService(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getCodeAnalysesByReleaseVersionService(1));
    }

    @Test
    public void when_get_code_analyses_by_release_version_service_returns_ok_response_then_log_debug()
    {
        QACodeAnalysis[] qaCodeAnalyses = DummyConsumerDataGenerator.getDummyQACodeAnalysisArray();
        Mockito.when(restHandler.getCodeAnalysesByReleaseVersionService(Mockito.anyInt())).thenReturn(new ResponseEntity<>(qaCodeAnalyses, HttpStatus.OK));

        CodeAnalysis[] result = client.getCodeAnalysesByReleaseVersionService(1);

        Assertions.assertEquals(1, result.length);
        CodeAnalysis[] expectedCodeAnalysis = DummyConsumerDataGenerator.convertToCodeAnalysisArray(qaCodeAnalyses);
        Assertions.assertEquals(expectedCodeAnalysis[0], result[0]);
    }

    @Test
    public void when_create_code_analysis_returns_ko_response_then_return_null()
    {
        Mockito.when(restHandler.createCodeAnalysisForReleaseVersionService(Mockito.any(QACodeAnalysisRequest.class), Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        QACodeAnalysis result = client.createCodeAnalysis(1, new QACodeAnalysisRequest());

        Assertions.assertNull(result);
    }

    @Test
    public void when_create_code_analysis_returns_ok_response_then_return_result()
    {
        QACodeAnalysis qaCodeAnalysis = DummyConsumerDataGenerator.getDummyQACodeAnalysisArray()[0];
        Mockito.when(restHandler.createCodeAnalysisForReleaseVersionService(Mockito.any(QACodeAnalysisRequest.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(qaCodeAnalysis, HttpStatus.OK));

        QACodeAnalysis result = client.createCodeAnalysis(1, new QACodeAnalysisRequest());

        Assertions.assertEquals(qaCodeAnalysis, result);
    }

    @Test
    public void when_get_subsystem_code_analyses_since_days_ago_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getSubsystemCodeAnalysesSinceDaysAgo(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getSubsystemCodeAnalysesSinceDaysAgo(2, "A"));
    }

    @Test
    public void when_get_subsystem_code_analyses_since_days_ago_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getSubsystemCodeAnalysesSinceDaysAgo(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyQASubsystemCodeAnalysisArray(), HttpStatus.OK));

        QASubsystemCodeAnalysis[] result = client.getSubsystemCodeAnalysesSinceDaysAgo(2, "A");

        Assertions.assertEquals(1, result.length);
        QASubsystemCodeAnalysis firstResult = result[0];
        Assertions.assertEquals(1, firstResult.getId());
        Assertions.assertEquals(1, firstResult.getSubsystemId());
        Assertions.assertEquals(1, firstResult.getJenkinsJobId());
        Assertions.assertEquals(1000L, firstResult.getCreationDate());
        Assertions.assertEquals("BRANCH", firstResult.getBranch());
        Assertions.assertEquals("ERROR_MESSAGE", firstResult.getErrorMessage());
        Assertions.assertEquals("SQA_STATE", firstResult.getSqaState());
        Assertions.assertEquals("STATUS", firstResult.getStatus());
        QACodeAnalysis[] codeAnalyses = firstResult.getCodeAnalyses();
        Assertions.assertEquals(1, codeAnalyses.length);
        Assertions.assertEquals(DummyConsumerDataGenerator.getDummyQACodeAnalysisArray()[0], codeAnalyses[0]);
    }

    @Test
    public void when_check_if_exist_and_higher_sonar_5_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.checkIfExistAndHigherSonar5(Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.checkIfExistAndHigherSonar5("A", "B"));
    }

    @Test
    public void when_check_if_exist_and_higher_sonar_5_returns_ok_response_with_true_result_then_return_true()
    {
        Mockito.when(restHandler.checkIfExistAndHigherSonar5(Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK));

        boolean result = client.checkIfExistAndHigherSonar5("A", "B");

        Assertions.assertTrue(result);
    }

    @Test
    public void when_check_if_exist_and_higher_sonar_5_returns_ok_response_with_false_result_then_return_false()
    {
        Mockito.when(restHandler.checkIfExistAndHigherSonar5(Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity<>(Boolean.FALSE, HttpStatus.OK));

        boolean result = client.checkIfExistAndHigherSonar5("A", "B");

        Assertions.assertFalse(result);
    }

    @Test
    public void when_get_report_by_link_returns_ko_response_then_return_null()
    {
        Mockito.when(restHandler.getPerformanceReportsByLink(Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getPerformanceReportsByLink("link"));
    }

    @Test
    public void when_get_report_by_link_returns_ok_response_then_return_result()
    {
        QAPlanPerformanceReport planPerformanceReport = DummyConsumerDataGenerator.getDummyQAPlanPerformanceReports()[0];
        Mockito.when(restHandler.getPerformanceReportsByLink(Mockito.anyString())).thenReturn(new ResponseEntity<>(planPerformanceReport, HttpStatus.OK));

        QAPlanPerformanceReport result = client.getPerformanceReportsByLink("link");

        Assertions.assertEquals(planPerformanceReport, result);
    }
}