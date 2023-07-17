package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.filetransferstatisticsapi.client.feign.nova.rest.IRestHandlerFiletransferstatisticsapi;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransferConfigHistorySnapshotDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransferConfigStatisticsDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransferConfigsStatisticsSummaryDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransferInstanceStatusStatisticsDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransfersInstancesStatisticsSummaryDTO;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class FileTransferStatisticsClientImplTest
{
    @Mock
    private IRestHandlerFiletransferstatisticsapi restHandler;
    @InjectMocks
    private FileTransferStatisticsClientImpl client;

    @BeforeEach
    public void init()
    {
        MockitoAnnotations.initMocks(FileTransferStatisticsClientImpl.class);
        client.init();
    }

    @Test
    public void when_get_file_transfer_configs_statistics_summary_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getFileTransferConfigsSummary(Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getFileTransferConfigsSummary("", ""));
    }

    @Test
    public void when_get_file_transfer_configs_statistisc_summary_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getFileTransferConfigsSummary(Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyFTMFileTransferConfigsStatisticsSummaryDTO(), HttpStatus.OK));

        FTMFileTransferConfigsStatisticsSummaryDTO result = client.getFileTransferConfigsSummary("", "");

        Assertions.assertEquals(1L, result.getTotal());
        FTMFileTransferConfigStatisticsDTO[] elements = result.getElements();
        Assertions.assertEquals(1, elements.length);
        FTMFileTransferConfigStatisticsDTO firstElement = elements[0];
        Assertions.assertEquals(100L, firstElement.getTotal());
        Assertions.assertEquals("Status", firstElement.getStatus());
    }

    @Test
    public void when_get_file_transfer_instances_summary_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getFileTransfersInstancesSummary(Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getFileTransferInstancesSummary("", ""));
    }

    @Test
    public void when_get_file_transfer_configs_summary_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getFileTransfersInstancesSummary(Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyFTMFileTransfersInstancesStatisticsSummaryDTO(), HttpStatus.OK));

        FTMFileTransfersInstancesStatisticsSummaryDTO result = client.getFileTransferInstancesSummary("", "");

        Assertions.assertEquals(8L, result.getTotal());
        Assertions.assertEquals(10D, result.getGigabytesTransfered());
        Assertions.assertEquals(5L, result.getNumTransferedFiles());
        FTMFileTransferInstanceStatusStatisticsDTO[] statuses = result.getStatuses();
        Assertions.assertEquals(1, statuses.length);
        FTMFileTransferInstanceStatusStatisticsDTO firstStatus = statuses[0];
        Assertions.assertEquals("Status", firstStatus.getStatus());
        Assertions.assertEquals(10L, firstStatus.getTotal());
    }

    @Test
    public void when_get_file_transfer_configs_history_snapshot_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getFileTransferConfigsHistorySnapshot()).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getFileTransferConfigsHistorySnapshot());
    }

    @Test
    public void when_get_file_transfer_configs_history_snapshot_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.getFileTransferConfigsHistorySnapshot()).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyFTMFileTransferConfigHistorySnapshotDtos(), HttpStatus.OK));

        FTMFileTransferConfigHistorySnapshotDTO[] result = client.getFileTransferConfigsHistorySnapshot();

        Assertions.assertEquals(1L, result.length);
        FTMFileTransferConfigHistorySnapshotDTO firstResult = result[0];
        Assertions.assertEquals("STATUS", firstResult.getStatus());
        Assertions.assertEquals("INT", firstResult.getEnvironment());
        Assertions.assertEquals("UUAA", firstResult.getUuaa());
        Assertions.assertEquals(10, firstResult.getValue());
    }
}