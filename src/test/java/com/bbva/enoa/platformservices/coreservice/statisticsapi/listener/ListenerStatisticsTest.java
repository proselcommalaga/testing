package com.bbva.enoa.platformservices.coreservice.statisticsapi.listener;

import com.bbva.enoa.apirestgen.statisticsapi.model.*;
import com.bbva.enoa.datamodel.model.statistic.enumerates.StatisticParamName;
import com.bbva.enoa.platformservices.coreservice.productsapi.enums.Environment;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.services.interfaces.IServiceStatisticsapi;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListenerStatisticsTest
{
    @Mock
    private IServiceStatisticsapi statisticsService;

    @InjectMocks
    private ListenerStatisticsapi listenerStatisticsapi;

    @Test
    void testGetProductsNumber() throws Errors
    {
        final Long expectedResponse = RandomUtils.nextLong();

        // given
        when(this.statisticsService.getProductsNumber()).thenReturn(expectedResponse);

        // when
        final Long response = this.listenerStatisticsapi.getProductsNumber(getNovaMetadata());

        // then
        assertEquals(expectedResponse, response);
    }

    @Test
    void testGetProductsNumberException()
    {
        // given
        when(this.statisticsService.getProductsNumber()).thenThrow(new RuntimeException("Error"));

        // then
        assertThrows(RuntimeException.class, () -> this.listenerStatisticsapi.getProductsNumber(getNovaMetadata()));
    }

    @Test
    void testGetFileTransfersSummary() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final FileTransfersSummaryDTO fileTransfersSummaryDTO = new FileTransfersSummaryDTO();
        fileTransfersSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getFileTransfersSummary(eq(environment), eq(uuaa))).thenReturn(fileTransfersSummaryDTO);

        // when
        final FileTransfersSummaryDTO response = this.listenerStatisticsapi.getFileTransfersSummary(getNovaMetadata(), environment, uuaa);

        // then
        assertEquals(fileTransfersSummaryDTO, response);
    }

    @Test
    void testGetFileTransfersSummaryExport() throws Errors
    {
        // given
        final String format = RandomStringUtils.randomAlphabetic(9);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getFileTransfersSummaryExport(eq(environment), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getFileTransfersSummaryExport(getNovaMetadata(), format, environment, uuaa);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testDeployedServicesHistorical() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String type = RandomStringUtils.randomAlphabetic(9);
        final String language = RandomStringUtils.randomAlphabetic(9);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String platform = RandomStringUtils.randomAlphabetic(6);
        final String category = RandomStringUtils.randomAlphabetic(6);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getDeployedServicesHistorical(eq(startDate), eq(endDate), eq(environment), eq(platform), eq(language), eq(type), eq(uuaa), eq(category))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getDeployedServicesHistorical(getNovaMetadata(), environment, endDate, uuaa, language, type, category, startDate, platform);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testDeployedServicesHistoricalExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String language = RandomStringUtils.randomAlphabetic(9);
        final String type = RandomStringUtils.randomAlphabetic(9);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String platform = RandomStringUtils.randomAlphabetic(6);
        final String format = RandomStringUtils.randomAlphabetic(9);
        StatisticParamName[] statisticParamNames = new StatisticParamName[]{StatisticParamName.DEPLOYED_SERVICES_TYPE, StatisticParamName.DEPLOYED_SERVICES_LANGUAGE};
        final String category = statisticParamNames[RandomUtils.nextInt(0, statisticParamNames.length)].name();

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getDeployedServicesHistoricalExport(eq(startDate), eq(endDate), eq(environment), eq(platform), eq(language), eq(type), eq(uuaa), eq(format), eq(category))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getDeployedServicesHistoricalExport(getNovaMetadata(), environment, endDate, uuaa, format, language, type, category, startDate, platform);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetFilesystemsSummary() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String status = RandomStringUtils.randomAlphabetic(3);

        final FilesystemsSummaryDTO filesystemsSummaryDTO = new FilesystemsSummaryDTO();
        filesystemsSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getFilesystemsSummary(eq(environment), eq(uuaa), eq(status))).thenReturn(filesystemsSummaryDTO);

        // when
        final FilesystemsSummaryDTO response = this.listenerStatisticsapi.getFilesystemsSummary(getNovaMetadata(), environment, uuaa, status);

        // then
        assertEquals(filesystemsSummaryDTO, response);
    }

    @Test
    void testGetFilesystemsSummaryExport() throws Errors
    {
        // given
        final String format = RandomStringUtils.randomAlphabetic(9);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String status = RandomStringUtils.randomAlphabetic(3);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getFilesystemsSummaryExport(eq(environment),eq(uuaa), eq(status), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getFilesystemsSummaryExport(getNovaMetadata(), format, environment, uuaa, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetUsersHistorical() throws Errors
    {
        // given
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String role = RandomStringUtils.randomAlphabetic(9);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalPoint stHistoricalPoint = new STHistoricalPoint();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalPoint[] stHistoricalPointsArray = new STHistoricalPoint[]{stHistoricalPoint};
        when(this.statisticsService.getUsersHistorical(eq(startDate), eq(endDate), eq(role), eq(uuaa))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalPoint[] response = this.listenerStatisticsapi.getUsersHistorical(getNovaMetadata(), role, endDate, uuaa, startDate);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetUsersHistoricalExport() throws Errors
    {
        // given
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String role = RandomStringUtils.randomAlphabetic(9);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getUsersHistoricalExport(eq(startDate), eq(endDate), eq(role), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getUsersHistoricalExport(getNovaMetadata(), format, role, endDate, uuaa, startDate);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetAlertsSummary() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String type = RandomStringUtils.randomAlphabetic(3);

        final AlertsSummaryDTO alertsSummaryDTO = new AlertsSummaryDTO();
        alertsSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getAlertsSummary(eq(environment), eq(type), eq(uuaa))).thenReturn(alertsSummaryDTO);

        // when
        final AlertsSummaryDTO response = this.listenerStatisticsapi.getAlertsSummary(getNovaMetadata(), environment, type, uuaa);

        // then
        assertEquals(alertsSummaryDTO, response);
    }

    @Test
    void testGetAlertsSummaryExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String type = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getAlertsSummaryExport(eq(environment),eq(type), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getAlertsSummaryExport(getNovaMetadata(), format, environment, type, uuaa);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetInstancesSummary() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(3);
        final String status = RandomStringUtils.randomAlphabetic(3);

        final InstancesSummaryDTO instancesSummaryDTO = new InstancesSummaryDTO();
        instancesSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getInstancesSummary(eq(environment), eq(uuaa), eq(platform), eq(status))).thenReturn(instancesSummaryDTO);

        // when
        final InstancesSummaryDTO response = this.listenerStatisticsapi.getInstancesSummary(getNovaMetadata(), environment, uuaa, platform, status);

        // then
        assertEquals(instancesSummaryDTO, response);
    }

    @Test
    void testGetInstancesSummaryExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(3);
        final String status = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getInstancesSummaryExport(eq(environment), eq(uuaa), eq(platform), eq(status), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getInstancesSummaryExport(getNovaMetadata(), format, environment, uuaa, platform, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetServicesSummary() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(3);

        final ServicesSummaryDTO servicesSummaryDTO = new ServicesSummaryDTO();
        servicesSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getServicesSummary(eq(environment), eq(uuaa), eq(platform))).thenReturn(servicesSummaryDTO);

        // when
        final ServicesSummaryDTO response = this.listenerStatisticsapi.getServicesSummary(getNovaMetadata(), environment, uuaa, platform);

        // then
        assertEquals(servicesSummaryDTO, response);
    }

    @Test
    void testGetServicesSummaryExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getServicesSummaryExport(eq(environment), eq(uuaa), eq(platform), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getServicesSummaryExport(getNovaMetadata(), format, environment, uuaa, platform);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetReleaseVersionsHistorical() throws Errors
    {
        // given
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String status = RandomStringUtils.randomAlphabetic(9);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getReleaseVersionsHistorical(eq(startDate), eq(endDate), eq(status), eq(uuaa))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getReleaseVersionsHistorical(getNovaMetadata(), endDate, uuaa, startDate, status);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetReleaseVersionsHistoricalExport() throws Errors
    {
        // given
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String status = RandomStringUtils.randomAlphabetic(9);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(10);
        when(this.statisticsService.getReleaseVersionsHistoricalExport(eq(startDate), eq(endDate), eq(status), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getReleaseVersionsHistoricalExport(getNovaMetadata(), format, endDate, uuaa, startDate, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetCategoriesHistorical() throws Errors
    {
        // given
        final String type = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalPoint stHistoricalPoint = new STHistoricalPoint();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalPoint[] stHistoricalPointsArray = new STHistoricalPoint[]{stHistoricalPoint};
        when(this.statisticsService.getCategoriesHistorical(eq(startDate), eq(endDate), eq(type))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalPoint[] response = this.listenerStatisticsapi.getCategoriesHistorical(getNovaMetadata(), type, endDate, startDate);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetCategoriesHistoricalExport() throws Errors
    {
        // given
        final String type = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(10);
        when(this.statisticsService.getCategoriesHistoricalExport(eq(startDate), eq(endDate), eq(type), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getCategoriesHistoricalExport(getNovaMetadata(), format, type, endDate, startDate);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetMemoryHistorical() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String unit = RandomStringUtils.randomAlphabetic(4);
        final String cpd = RandomStringUtils.randomAlphabetic(7);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalPoint stHistoricalPoint = new STHistoricalPoint();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalPoint[] stHistoricalPointsArray = new STHistoricalPoint[]{stHistoricalPoint};
        when(this.statisticsService.getMemoryHistorical(eq(startDate), eq(endDate), eq(cpd), eq(environment), eq(unit))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalPoint[] response = this.listenerStatisticsapi.getMemoryHistorical(getNovaMetadata(), environment, unit, endDate, cpd, startDate);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetMemoryHistoricalExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String unit = RandomStringUtils.randomAlphabetic(4);
        final String cpd = RandomStringUtils.randomAlphabetic(7);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(13);
        when(this.statisticsService.getMemoryHistoricalExport(eq(startDate), eq(endDate), eq(cpd), eq(environment), eq(unit), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getMemoryHistoricalExport(getNovaMetadata(), environment, unit, endDate, cpd, format, startDate);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetServicesNumber() throws Errors
    {
        final Long expectedResponse = RandomUtils.nextLong();

        // given
        when(this.statisticsService.getServicesNumber()).thenReturn(expectedResponse);

        // when
        final Long response = this.listenerStatisticsapi.getServicesNumber(getNovaMetadata());

        // then
        assertEquals(expectedResponse, response);
    }

    @Test
    void testGetServicesNumberException()
    {
        // given
        when(this.statisticsService.getServicesNumber()).thenThrow(new RuntimeException("Error"));

        // then
        assertThrows(RuntimeException.class, () -> this.listenerStatisticsapi.getServicesNumber(getNovaMetadata()));
    }

    @Test
    void testGetApisNumber() throws Errors
    {
        final Long expectedResponse = RandomUtils.nextLong();

        // given
        when(this.statisticsService.getApisNumberInPro()).thenReturn(expectedResponse);

        // when
        final Long response = this.listenerStatisticsapi.getApisNumber(getNovaMetadata());

        // then
        assertEquals(expectedResponse, response);
    }

    @Test
    void testGetApisNumberException()
    {
        // given
        when(this.statisticsService.getApisNumberInPro()).thenThrow(new RuntimeException("Error"));

        // then
        assertThrows(RuntimeException.class, () -> this.listenerStatisticsapi.getApisNumber(getNovaMetadata()));
    }

    @Test
    void testBatchExecutionsSummary() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(3);
        final String origin = RandomStringUtils.randomAlphabetic(7);

        final BatchExecutionsSummaryDTO batchExecutionsSummaryDTO = new BatchExecutionsSummaryDTO();
        batchExecutionsSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getBatchExecutionsSummary(eq(environment), eq(uuaa), eq(platform), eq(origin))).thenReturn(batchExecutionsSummaryDTO);

        // when
        final BatchExecutionsSummaryDTO response = this.listenerStatisticsapi.getBatchExecutionsSummary(getNovaMetadata(), environment, uuaa, platform, origin);

        // then
        assertEquals(batchExecutionsSummaryDTO, response);
    }

    @Test
    void testBatchExecutionsSummaryExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(3);
        final String origin = RandomStringUtils.randomAlphabetic(7);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getBatchExecutionsSummaryExport(eq(environment),eq(uuaa), eq(platform), eq(origin), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getBatchExecutionsSummaryExport(getNovaMetadata(), format, environment, uuaa, platform, origin);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetErrorCodes() throws Errors
    {
        final String errorCodes = RandomStringUtils.randomAlphabetic(7);

        // given
        when(this.statisticsService.getErrorCodes()).thenReturn(errorCodes);

        // when
        final String response = this.listenerStatisticsapi.getErrorCodes(getNovaMetadata());

        // then
        assertEquals(errorCodes, response);
    }

    @Test
    void testGetErrorCodesException()
    {
        // given
        when(this.statisticsService.getErrorCodes()).thenThrow(new RuntimeException("Error"));

        // then
        assertThrows(RuntimeException.class, () -> this.listenerStatisticsapi.getErrorCodes(getNovaMetadata()));
    }

    @Test
    void testGetUsersNumber() throws Errors
    {
        final Long expectedResponse = RandomUtils.nextLong();

        // given
        when(this.statisticsService.getUsersNumber()).thenReturn(expectedResponse);

        // when
        final Long response = this.listenerStatisticsapi.getUsersNumber(getNovaMetadata());

        // then
        assertEquals(expectedResponse, response);
    }

    @Test
    void testGetUsersNumberException()
    {
        // given
        when(this.statisticsService.getUsersNumber()).thenThrow(new RuntimeException("Error"));

        // then
        assertThrows(RuntimeException.class, () -> this.listenerStatisticsapi.getUsersNumber(getNovaMetadata()));
    }

    @Test
    void testGetSubsystemsSummary() throws Errors
    {
        // given
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final SubsystemSummaryDTO subsystemSummaryDTO = new SubsystemSummaryDTO();
        subsystemSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getSubsystemsSummary(eq(uuaa))).thenReturn(subsystemSummaryDTO);

        // when
        final SubsystemSummaryDTO response = this.listenerStatisticsapi.getSubsystemsSummary(getNovaMetadata(), uuaa);

        // then
        assertEquals(subsystemSummaryDTO, response);
    }

    @Test
    void testGetSubsystemsSummaryExport() throws Errors
    {
        // given
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getSubsystemsSummaryExport(eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getSubsystemsSummaryExport(getNovaMetadata(), format, uuaa);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetApisHistorical() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String type = RandomStringUtils.randomAlphabetic(7);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getApisHistorical(eq("ALL"), eq(environment), eq(endDate), eq(uuaa), eq(type), eq(startDate))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getApisHistorical(getNovaMetadata(), environment, endDate, uuaa, "ALL", type, startDate);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetApisHistoricalExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String type = RandomStringUtils.randomAlphabetic(7);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(13);
        when(this.statisticsService.getApisHistoricalExport(eq(startDate), eq(endDate), eq(type), eq(uuaa), eq(environment), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getApisHistoricalExport(getNovaMetadata(), environment, endDate, uuaa, format, type, startDate);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetQualityAnalysesSummary() throws Errors
    {
        // given
        final QualityAnalysesSummaryDTO subsystemSummaryDTO = new QualityAnalysesSummaryDTO();
        subsystemSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getQualityAnalysesSummary()).thenReturn(subsystemSummaryDTO);

        // when
        final QualityAnalysesSummaryDTO response = this.listenerStatisticsapi.getQualityAnalysesSummary(getNovaMetadata());

        // then
        assertEquals(subsystemSummaryDTO, response);
    }

    @Test
    void testGetQualityAnalysesSummaryExport() throws Errors
    {
        // given
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getQualityAnalysesSummaryExport(format)).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getQualityAnalysesSummaryExport(getNovaMetadata(), format);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetProductsHistorical() throws Errors
    {
        // given
        final String type = RandomStringUtils.randomAlphabetic(7);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalPoint stHistoricalPoint = new STHistoricalPoint();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalPoint[] stHistoricalPointsArray = new STHistoricalPoint[]{stHistoricalPoint};
        when(this.statisticsService.getProductsHistorical(eq(startDate), eq(endDate), eq(type))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalPoint[] response = this.listenerStatisticsapi.getProductsHistorical(getNovaMetadata(), type, endDate, startDate);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetProductsHistoricalExport() throws Errors
    {
        // given
        final String type = RandomStringUtils.randomAlphabetic(7);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(13);
        when(this.statisticsService.getProductsHistoricalExport(eq(startDate), eq(endDate), eq(type), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getProductsHistoricalExport(getNovaMetadata(), format, type, endDate, startDate);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetSubsystemsHistorical() throws Errors
    {
        // given
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String type = RandomStringUtils.randomAlphabetic(7);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getSubsystemsHistorical(eq(startDate), eq(endDate), eq(type), eq(uuaa))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getSubsystemsHistorical(getNovaMetadata(), type, endDate, uuaa, startDate);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetSubsystemsHistoricalExport() throws Errors
    {
        // given
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String type = RandomStringUtils.randomAlphabetic(7);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(13);
        when(this.statisticsService.getSubsystemsHistoricalExport(eq(startDate), eq(endDate), eq(type), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getSubsystemsHistoricalExport(getNovaMetadata(), format, type, endDate, uuaa, startDate);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetFileTransfersHistorical() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String status = RandomStringUtils.randomAlphabetic(8);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getFiletransfersHistorical(eq(startDate), eq(endDate), eq(environment), eq(status), eq(uuaa))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getFiletransfersHistorical(getNovaMetadata(), environment, endDate, uuaa, startDate, status);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetFileTransfersHistoricalExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String status = RandomStringUtils.randomAlphabetic(8);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(18);
        when(this.statisticsService.getFiletransfersHistoricalExport(eq(startDate), eq(endDate), eq(environment), eq(status), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getFiletransfersHistoricalExport(getNovaMetadata(), environment, endDate, uuaa, format, startDate, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetAdoptionLevelHistorical() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String platform = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getAdoptionLevelHistorical(eq(startDate), eq(endDate), eq(environment), eq(platform))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getAdoptionLevelHistorical(getNovaMetadata(), environment, endDate, startDate, platform);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetAdoptionLevelHistoricalExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String platform = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(18);
        when(this.statisticsService.getAdoptionLevelHistoricalExport(eq(startDate), eq(endDate), eq(environment), eq(platform), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getAdoptionLevelHistoricalExport(getNovaMetadata(), format, environment, endDate, startDate, platform);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetInstancesHistorical() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String type = RandomStringUtils.randomAlphabetic(8);
        final String platform = RandomStringUtils.randomAlphabetic(5);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getInstancesHistorical(eq(startDate), eq(endDate), eq(environment), eq(platform), eq(type), eq(uuaa))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getInstancesHistorical(getNovaMetadata(), environment, endDate, uuaa, type, startDate, platform);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetInstancesHistoricalExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String type = RandomStringUtils.randomAlphabetic(8);
        final String platform = RandomStringUtils.randomAlphabetic(5);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(5);
        when(this.statisticsService.getInstancesHistoricalExport(eq(startDate), eq(endDate), eq(environment), eq(platform), eq(type), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getInstancesHistoricalExport(getNovaMetadata(), environment, endDate, uuaa, format, type, startDate, platform);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetBatchInstancesHistorical() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String status = RandomStringUtils.randomAlphabetic(8);
        final String platform = RandomStringUtils.randomAlphabetic(5);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getBatchExecutionsHistorical(eq(startDate), eq(endDate), eq(environment), eq(platform), eq(uuaa), eq(status))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getBatchInstancesHistorical(getNovaMetadata(), environment, endDate, uuaa, startDate, platform, status);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetBatchInstancesHistoricalExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String status = RandomStringUtils.randomAlphabetic(8);
        final String platform = RandomStringUtils.randomAlphabetic(5);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(15);
        when(this.statisticsService.getBatchExecutionsHistoricalExport(eq(startDate), eq(endDate), eq(environment), eq(platform), eq(uuaa), eq(status), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getBatchInstancesHistoricalExport(getNovaMetadata(), environment, endDate, uuaa, format, startDate, platform, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetStorageHistorical() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String property = RandomStringUtils.randomAlphabetic(8);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getStorageHistorical(eq(startDate), eq(endDate), eq(environment), eq(property), eq(uuaa))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getStorageHistorical(getNovaMetadata(), property, environment, endDate, uuaa, startDate);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetStorageHistoricalExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String property = RandomStringUtils.randomAlphabetic(8);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(8);
        when(this.statisticsService.getStorageHistoricalExport(eq(startDate), eq(endDate), eq(environment), eq(property), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getStorageHistoricalExport(getNovaMetadata(), environment, endDate, uuaa, property, format, startDate);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetCompilationHistorical() throws Errors
    {
        // given
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String type = RandomStringUtils.randomAlphabetic(8);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getCompilationsHistorical(eq(startDate), eq(endDate), eq(status), eq(type), eq(uuaa))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getCompilationsHistorical(getNovaMetadata(), type, endDate, uuaa, startDate, status);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetCompilationHistoricalExport() throws Errors
    {
        // given
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String type = RandomStringUtils.randomAlphabetic(8);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(18);
        when(this.statisticsService.getCompilationsHistoricalExport(eq(startDate), eq(endDate), eq(status), eq(type), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getCompilationsHistoricalExport(getNovaMetadata(), endDate, uuaa, format, type, startDate, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetDeployedPlansHistorical() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getDeployedPlansHistorical(eq(startDate), eq(endDate), eq(environment), eq(status), eq(uuaa))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getDeployedPlansHistorical(getNovaMetadata(), environment, endDate, uuaa, startDate, status);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetDeployedPlansHistoricalExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(13);
        when(this.statisticsService.getDeployedPlansHistoricalExport(eq(startDate), eq(endDate), eq(environment), eq(status), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getDeployedPlansHistoricalExport(getNovaMetadata(), environment, endDate, uuaa, format, startDate, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetApisSummary() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String functionality = "SYNC";
        final ApiSummaryDTO apiSummaryDTO = new ApiSummaryDTO();
        apiSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getApisSummary(eq(environment), eq(functionality), eq(uuaa))).thenReturn(apiSummaryDTO);

        // when
        final ApiSummaryDTO response = this.listenerStatisticsapi.getApisSummary(getNovaMetadata(), environment, functionality, uuaa);

        // then
        assertEquals(apiSummaryDTO, response);
    }

    @Test
    void testGetApisSummaryExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getApisSummaryExport(eq(environment), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getApisSummaryExport(getNovaMetadata(), format, environment, uuaa);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetProductsSummary() throws Errors
    {
        // given
        final ProductSummaryDTO productSummaryDTO = new ProductSummaryDTO();
        productSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getProductsSummary()).thenReturn(productSummaryDTO);

        // when
        final ProductSummaryDTO response = this.listenerStatisticsapi.getProductsSummary(getNovaMetadata());

        // then
        assertEquals(productSummaryDTO, response);
    }

    @Test
    void testGetProductsSummaryExport() throws Errors
    {
        // given
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getProductsSummaryExport(eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getProductsSummaryExport(getNovaMetadata(), format);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetFilesystemsHistorical() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String type = RandomStringUtils.randomAlphabetic(9);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getFilesystemsHistorical(eq(startDate), eq(endDate), eq(environment), eq(status), eq(type), eq(uuaa))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getFilesystemsHistorical(getNovaMetadata(), environment, endDate, uuaa, type, startDate, status);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetFilesystemsHistoricalExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String type = RandomStringUtils.randomAlphabetic(9);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(13);
        when(this.statisticsService.getFilesystemsHistoricalExport(eq(startDate), eq(endDate), eq(environment), eq(status), eq(type), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getFilesystemsHistoricalExport(getNovaMetadata(), environment, endDate, uuaa, format, type, startDate, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetTodoTasksHistorical() throws Errors
    {
        // given
        final String role = RandomStringUtils.randomAlphabetic(6);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String type = RandomStringUtils.randomAlphabetic(9);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalPoint stHistoricalPoint = new STHistoricalPoint();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalPoint[] stHistoricalPointsArray = new STHistoricalPoint[]{stHistoricalPoint};
        when(this.statisticsService.getTodotasksHistorical(eq(startDate), eq(endDate), eq(status), eq(type), eq(role), eq(uuaa))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalPoint[] response = this.listenerStatisticsapi.getTodotasksHistorical(getNovaMetadata(), role, endDate, uuaa, type, startDate, status);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetTodoTasksHistoricalExport() throws Errors
    {
        // given
        final String role = RandomStringUtils.randomAlphabetic(6);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String type = RandomStringUtils.randomAlphabetic(9);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(13);
        when(this.statisticsService.getTodotasksHistoricalExport(eq(startDate), eq(endDate), eq(status), eq(type), eq(role), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getTodotasksHistoricalExport(getNovaMetadata(), role, endDate, uuaa, format, type, startDate, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetBudgetsSummary() throws Errors
    {
        // given
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final BudgetSummaryDTO budgetSummaryDTO = new BudgetSummaryDTO();
        budgetSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getBudgetsSummary(eq(uuaa), eq(status))).thenReturn(budgetSummaryDTO);

        // when
        final BudgetSummaryDTO response = this.listenerStatisticsapi.getBudgetsSummary(getNovaMetadata(), uuaa, status);

        // then
        assertEquals(budgetSummaryDTO, response);
    }

    @Test
    void testGetBudgetsSummaryExport() throws Errors
    {
        // given
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getBudgetsSummaryExport(eq(uuaa), eq(status), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getBudgetsSummaryExport(getNovaMetadata(), format, uuaa, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetConnectorsSummary() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final ConnectorsSummaryDTO connectorsSummaryDTO = new ConnectorsSummaryDTO();
        connectorsSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getConnectorsSummary(eq(environment), eq(uuaa), eq(status))).thenReturn(connectorsSummaryDTO);

        // when
        final ConnectorsSummaryDTO response = this.listenerStatisticsapi.getConnectorsSummary(getNovaMetadata(), environment, uuaa, status);

        // then
        assertEquals(connectorsSummaryDTO, response);
    }

    @Test
    void testGetConnectorsSummaryExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getConnectorsSummaryExport(eq(environment), eq(uuaa), eq(status), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getConnectorsSummaryExport(getNovaMetadata(), format, environment, uuaa, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetBrokersSummary() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(7);

        final BrokersSummaryDTO brokersSummaryDTO = new BrokersSummaryDTO();
        brokersSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getBrokersSummary(eq(environment), eq(uuaa), eq(platform), eq(status))).thenReturn(brokersSummaryDTO);

        // when
        final BrokersSummaryDTO response = this.listenerStatisticsapi.getBrokersSummary(getNovaMetadata(), environment, uuaa, platform, status);

        // then
        assertEquals(brokersSummaryDTO, response);
    }

    @Test
    void testGetBrokersSummaryExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String format = RandomStringUtils.randomAlphabetic(9);
        final String platform = RandomStringUtils.randomAlphabetic(7);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getBrokersSummaryExport(eq(environment), eq(uuaa), eq(platform), eq(status), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getBrokersSummaryExport(getNovaMetadata(), format, environment, uuaa, platform, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetNovaCoinsByAvailability() throws Errors
    {
        // given
        final String budgetType = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final AvailabilityNovaCoinsDTO availabilityNovaCoinsDTO = new AvailabilityNovaCoinsDTO();
        availabilityNovaCoinsDTO.fillRandomly(2, false, 1, 2);
        final AvailabilityNovaCoinsDTO[] availabilityNovaCoinsDTOArray = new AvailabilityNovaCoinsDTO[]{availabilityNovaCoinsDTO};
        when(this.statisticsService.getNovaCoinsByAvailability(eq(uuaa), eq(budgetType))).thenReturn(availabilityNovaCoinsDTOArray);

        // when
        final AvailabilityNovaCoinsDTO[] response = this.listenerStatisticsapi.getNovaCoinsByAvailability(getNovaMetadata(), budgetType, uuaa);

        // then
        assertArrayEquals(availabilityNovaCoinsDTOArray, response);
    }

    @Test
    void testGetNovaCoinsByAvailabilityExport() throws Errors
    {
        // given
        final String budgetType = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getNovaCoinsByAvailabilityExport(eq(uuaa), eq(budgetType), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getNovaCoinsByAvailabilityExport(getNovaMetadata(), budgetType, format, uuaa);

        // then
        assertArrayEquals(historicalBytes, response);
    }

    @Test
    void testBuildJobsSummary() throws Errors
    {
        // given
        final String jobType = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final BuildJobSummaryDTO buildJobSummaryDTO = new BuildJobSummaryDTO();
        buildJobSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getBuildJobsSummary(eq(jobType), eq(uuaa))).thenReturn(buildJobSummaryDTO);

        // when
        final BuildJobSummaryDTO response = this.listenerStatisticsapi.getBuildJobsSummary(getNovaMetadata(), jobType, uuaa);

        // then
        assertEquals(buildJobSummaryDTO, response);
    }

    @Test
    void testBuildJobsSummaryExport() throws Errors
    {
        // given
        final String jobType = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getBuildJobsSummaryExport(eq(jobType), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getBuildJobsSummaryExport(getNovaMetadata(), format, jobType, uuaa);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetFileTransfersInstancesSummary() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final FileTransfersInstancesSummaryDTO fileTransfersInstancesSummaryDTO = new FileTransfersInstancesSummaryDTO();
        fileTransfersInstancesSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getFileTransfersInstancesSummary(eq(environment), eq(uuaa))).thenReturn(fileTransfersInstancesSummaryDTO);

        // when
        final FileTransfersInstancesSummaryDTO response = this.listenerStatisticsapi.getFileTransfersInstancesSummary(getNovaMetadata(), environment, uuaa);

        // then
        assertEquals(fileTransfersInstancesSummaryDTO, response);
    }

    @Test
    void testGetFileTransfersInstancesSummaryExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getFileTransfersInstancesSummaryExport(eq(environment), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getFileTransfersInstancesSummaryExport(getNovaMetadata(), format, environment, uuaa);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetBatchSchedulerExecutionsSummary() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(7);

        final BatchSchedulerExecutionsSummaryDTO batchSchedulerExecutionsSummaryDTO = new BatchSchedulerExecutionsSummaryDTO();
        batchSchedulerExecutionsSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getBatchSchedulerExecutionsSummary(eq(environment), eq(uuaa), eq(platform))).thenReturn(batchSchedulerExecutionsSummaryDTO);

        // when
        final BatchSchedulerExecutionsSummaryDTO response = this.listenerStatisticsapi.getBatchSchedulerExecutionsSummary(getNovaMetadata(), environment, uuaa, platform);

        // then
        assertEquals(batchSchedulerExecutionsSummaryDTO, response);
    }

    @Test
    void testGetBatchSchedulerExecutionsSummaryExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(7);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getBatchSchedulerExecutionsSummaryExport(eq(environment), eq(uuaa), eq(platform), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getBatchSchedulerExecutionsSummaryExport(getNovaMetadata(), format, environment, uuaa, platform);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetTodoTaskSummary() throws Errors
    {
        // given
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String status = RandomStringUtils.randomAlphabetic(7);

        final TODOTaskSummaryDTO todoTaskSummaryDTO = new TODOTaskSummaryDTO();
        todoTaskSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getTodotasksSummary(eq(uuaa), eq(status))).thenReturn(todoTaskSummaryDTO);

        // when
        final TODOTaskSummaryDTO response = this.listenerStatisticsapi.getTodotasksSummary(getNovaMetadata(), uuaa, status);

        // then
        assertEquals(todoTaskSummaryDTO, response);
    }

    @Test
    void testGetTodoTaskSummaryExport() throws Errors
    {
        // given
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String status = RandomStringUtils.randomAlphabetic(7);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getTodotasksSummaryExport(eq(uuaa), eq(status), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getTodotasksSummaryExport(getNovaMetadata(), format, uuaa, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetHardwareHistorical() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String property = RandomStringUtils.randomAlphabetic(9);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getHardwareHistorical(eq(startDate), eq(endDate), eq(environment), eq(property), eq(uuaa))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getHardwareHistorical(getNovaMetadata(), property, environment, endDate, uuaa, startDate);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetHardwareHistoricalExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String property = RandomStringUtils.randomAlphabetic(9);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(13);
        when(this.statisticsService.getHardwareHistoricalExport(eq(startDate), eq(endDate), eq(environment), eq(property), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getHardwareHistoricalExport(getNovaMetadata(), environment, endDate, uuaa, property, format, startDate);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetConnectorsHistorical() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String type = RandomStringUtils.randomAlphabetic(9);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String status = RandomStringUtils.randomAlphabetic(7);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getConnectorsHistorical(eq(startDate), eq(endDate), eq(environment), eq(status), eq(type), eq(uuaa))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getConnectorsHistorical(getNovaMetadata(), environment, endDate, uuaa, type, startDate, status);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetConnectorsHistoricalExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String type = RandomStringUtils.randomAlphabetic(9);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String status = RandomStringUtils.randomAlphabetic(7);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getConnectorsHistoricalExport(eq(startDate), eq(endDate), eq(environment), eq(status), eq(type), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getConnectorsHistoricalExport(getNovaMetadata(), environment, endDate, uuaa, format, type, startDate, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetBrokersHistorical() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String type = RandomStringUtils.randomAlphabetic(9);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String status = RandomStringUtils.randomAlphabetic(7);
        final String platform = RandomStringUtils.randomAlphabetic(7);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[] { stHistoricalPoint };
        when(this.statisticsService.getBrokersHistorical(eq(startDate), eq(endDate), eq(environment), eq(status), eq(type), eq(uuaa), eq(platform))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getBrokersHistorical(getNovaMetadata(), environment, endDate, uuaa, type, startDate, platform, status);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetBrokersHistoricalExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String type = RandomStringUtils.randomAlphabetic(9);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String status = RandomStringUtils.randomAlphabetic(7);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String platform = RandomStringUtils.randomAlphabetic(7);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getBrokersHistoricalExport(eq(startDate), eq(endDate), eq(environment), eq(status), eq(type), eq(uuaa), eq(platform), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getBrokersHistoricalExport(getNovaMetadata(), environment, endDate, uuaa, format, type, startDate, platform, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetReleaseVersionsSummary() throws Errors
    {
        // given
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(7);
        final String status = RandomStringUtils.randomAlphabetic(5);

        final ReleaseVersionSummaryDTO releaseVersionSummaryDTO = new ReleaseVersionSummaryDTO();
        releaseVersionSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getReleaseVersionsSummary(eq(uuaa), eq(platform), eq(status))).thenReturn(releaseVersionSummaryDTO);

        // when
        final ReleaseVersionSummaryDTO response = this.listenerStatisticsapi.getReleaseVersionsSummary(getNovaMetadata(), uuaa, platform, status);

        // then
        assertEquals(releaseVersionSummaryDTO, response);
    }

    @Test
    void testGetReleaseVersionsSummaryExport() throws Errors
    {
        // given
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(7);
        final String status = RandomStringUtils.randomAlphabetic(5);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getReleaseVersionsSummaryExport(eq(uuaa), eq(platform), eq(status), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getReleaseVersionsSummaryExport(getNovaMetadata(), format, uuaa, platform, status);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetUsersSummary() throws Errors
    {
        // given
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final UserSummaryDTO userSummaryDTO = new UserSummaryDTO();
        userSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getUsersSummary(eq(uuaa))).thenReturn(userSummaryDTO);

        // when
        final UserSummaryDTO response = this.listenerStatisticsapi.getUsersSummary(getNovaMetadata(), uuaa);

        // then
        assertEquals(userSummaryDTO, response);
    }

    @Test
    void testGetUsersSummaryExport() throws Errors
    {
        // given
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getUsersSummaryExport(eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getUsersSummaryExport(getNovaMetadata(), format, uuaa);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetDeploymentPlansSummary() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(7);

        final DeploymentPlansSummaryDTO deploymentPlansSummaryDTO = new DeploymentPlansSummaryDTO();
        deploymentPlansSummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getDeploymentPlansSummary(eq(environment), eq(uuaa), eq(platform))).thenReturn(deploymentPlansSummaryDTO);

        // when
        final DeploymentPlansSummaryDTO response = this.listenerStatisticsapi.getDeploymentPlansSummary(getNovaMetadata(), environment, uuaa, platform);

        // then
        assertEquals(deploymentPlansSummaryDTO, response);
    }

    @Test
    void testGetDeploymentPlansSummaryExport() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(7);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getDeploymentPlansSummaryExport(eq(environment), eq(uuaa), eq(platform), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getDeploymentPlansSummaryExport(getNovaMetadata(), format, environment, uuaa, platform);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetCategoriesSummary() throws Errors
    {
        // given
        final String categoryName = RandomStringUtils.randomAlphabetic(4);

        final CategorySummaryDTO categorySummaryDTO = new CategorySummaryDTO();
        categorySummaryDTO.fillRandomly(2, false, 1, 2);
        when(this.statisticsService.getCategoriesSummary(eq(categoryName))).thenReturn(categorySummaryDTO);

        // when
        final CategorySummaryDTO response = this.listenerStatisticsapi.getCategoriesSummary(getNovaMetadata(), categoryName);

        // then
        assertEquals(categorySummaryDTO, response);
    }

    @Test
    void testGetCategoriesSummaryExport() throws Errors
    {
        // given
        final String categoryName = RandomStringUtils.randomAlphabetic(4);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getCategoriesSummaryExport(eq(categoryName), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getCategoriesSummaryExport(getNovaMetadata(), format, categoryName);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void testGetCloudProductsSummary() throws Errors
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalSerie stHistoricalPoint = new STHistoricalSerie();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalSerie[] stHistoricalPointsArray = new STHistoricalSerie[]{stHistoricalPoint};
        when(this.statisticsService.getCloudProductsSummary(startDate, endDate, environment, uuaa)).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalSerie[] response = this.listenerStatisticsapi.getCloudProductsSummary(getNovaMetadata(), environment, endDate, uuaa, startDate);

        // then
        assertEquals(stHistoricalPointsArray, response);
    }

    @Test
    void testGetCloudProductsSummaryExport() throws Errors
    {
        // given
        final String format = RandomStringUtils.randomAlphabetic(9);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final byte[] historicalBytes = RandomUtils.nextBytes(6);
        when(this.statisticsService.getCloudProductsSummaryExport(eq(startDate), eq(endDate), eq(environment), eq(uuaa), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getCloudProductsSummaryExport(getNovaMetadata(), format, environment, endDate, uuaa, startDate);

        // then
        assertEquals(historicalBytes, response);
    }

    @Test
    void getUsersConnectedHistorical() throws Errors
    {
        // given
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);

        final STHistoricalPoint stHistoricalPoint = new STHistoricalPoint();
        stHistoricalPoint.fillRandomly(2, false, 1, 2);
        final STHistoricalPoint[] stHistoricalPointsArray = new STHistoricalPoint[]{stHistoricalPoint};
        when(this.statisticsService.getUsersConnectedHistorical(eq(startDate), eq(endDate))).thenReturn(stHistoricalPointsArray);

        // when
        final STHistoricalPoint[] response = this.listenerStatisticsapi.getUsersConnectedHistorical(getNovaMetadata(), endDate, startDate);

        // then
        assertArrayEquals(stHistoricalPointsArray, response);
    }

    @Test
    void getUsersConnectedHistoricalExport() throws Errors
    {
        // given
        final String endDate = RandomStringUtils.randomAlphabetic(3);
        final String startDate = RandomStringUtils.randomAlphabetic(3);
        final String format = RandomStringUtils.randomAlphabetic(9);

        final byte[] historicalBytes = RandomUtils.nextBytes(13);
        when(this.statisticsService.getUsersConnectedHistoricalExport(eq(startDate), eq(endDate), eq(format))).thenReturn(historicalBytes);

        // when
        final byte[] response = this.listenerStatisticsapi.getUsersConnectedHistoricalExport(getNovaMetadata(), format, endDate, startDate);

        // then
        assertEquals(historicalBytes, response);
    }

    private static NovaMetadata getNovaMetadata()
    {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", List.of(RandomStringUtils.randomAlphabetic(9)));
        NovaMetadata metadata = new NovaMetadata();
        metadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));
        return metadata;
    }
}
