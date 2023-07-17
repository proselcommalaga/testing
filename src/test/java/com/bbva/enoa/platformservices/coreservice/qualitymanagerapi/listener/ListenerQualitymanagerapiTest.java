package com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.listener;

import com.bbva.enoa.apirestgen.qualitymanagerapi.model.PerformanceReport;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.interfaces.IQualityManagerService;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListenerQualitymanagerapiTest
{
    public static final String IV_USER = "CODE";
    private final NovaMetadata novaMetadata = new NovaMetadata();

    @Mock
    private IQualityManagerService qualityManagerService;

    @InjectMocks
    private ListenerQualitymanagerapi listenerQualitymanagerapi;

    @BeforeEach
    public void setUp() throws Exception
    {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", Collections.singletonList(IV_USER));
        novaMetadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getReleaseVersionCodeAnalyses() throws Errors
    {
        int releaseVersionId = RandomUtils.nextInt(0,1000);
        listenerQualitymanagerapi.getReleaseVersionCodeAnalyses(novaMetadata, releaseVersionId);
        Mockito.verify(qualityManagerService, Mockito.only()).getReleaseVersionCodeAnalyses(releaseVersionId);
    }

    @Test
    void getCodeAnalysisStatuses() throws Errors
    {
        int[] releaseVersionIdList = new int[]{RandomUtils.nextInt(0,1000),RandomUtils.nextInt(1000,10000)} ;
        listenerQualitymanagerapi.getCodeAnalysisStatuses(novaMetadata, releaseVersionIdList);
        Mockito.verify(qualityManagerService, Mockito.only()).getCodeAnalysisStatuses(releaseVersionIdList);
    }

    @Test
    void requestSubsystemCodeAnalysis() throws Errors
    {
        int subsystemId = RandomUtils.nextInt(1,1000);
        String branchName = "branch-" + RandomStringUtils.randomAlphabetic(10);
        listenerQualitymanagerapi.requestSubsystemCodeAnalysis(novaMetadata, subsystemId, branchName);
        Mockito.verify(this.qualityManagerService, Mockito.only()).requestSubsystemCodeAnalysis(IV_USER, subsystemId, branchName);
    }

    @Test
    void setPerformanceReport() throws Errors
    {
        int planId = RandomUtils.nextInt(1,1000);
        PerformanceReport performanceReport = new PerformanceReport();
        listenerQualitymanagerapi.setPerformanceReport(novaMetadata,performanceReport, planId);
        Mockito.verify(this.qualityManagerService, Mockito.only()).setPerformanceReport(IV_USER, planId, performanceReport);
    }

    @Test
    void deletePerformanceReport() throws Errors
    {
        int planId = RandomUtils.nextInt(1,1000);
        listenerQualitymanagerapi.deletePerformanceReport(novaMetadata, planId);
        Mockito.verify(this.qualityManagerService, Mockito.only()).deletePerformanceReport(IV_USER, planId);
    }

    @Test
    void getPerformanceReports() throws Errors
    {
        int releaseVersionId = RandomUtils.nextInt(1,1000);
        listenerQualitymanagerapi.getPerformanceReports(novaMetadata, releaseVersionId);
        Mockito.verify(this.qualityManagerService, Mockito.only()).getPerformanceReports(releaseVersionId);
    }
}
