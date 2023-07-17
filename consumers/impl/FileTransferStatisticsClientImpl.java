package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.filetransferstatisticsapi.client.feign.nova.rest.IRestHandlerFiletransferstatisticsapi;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.client.feign.nova.rest.IRestListenerFiletransferstatisticsapi;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.client.feign.nova.rest.impl.RestHandlerFiletransferstatisticsapi;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransferConfigHistorySnapshotDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransferConfigsStatisticsSummaryDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransfersInstancesStatisticsSummaryDTO;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IFileTransferStatisticsClient;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.exceptions.StatisticsError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class FileTransferStatisticsClientImpl implements IFileTransferStatisticsClient
{

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FileTransferStatisticsClientImpl.class);

    /**
     * File Transfer Statistics REST handler interface
     */
    @Autowired
    private IRestHandlerFiletransferstatisticsapi iRestHandlerFiletransferstatisticsapi;

    /**
     * File Transfer Statistics REST handler
     */
    private RestHandlerFiletransferstatisticsapi restHandlerFiletransferstatisticsapi;

    /**
     * Initialize the REST handler
     */
    @PostConstruct
    public void init()
    {
        this.restHandlerFiletransferstatisticsapi = new RestHandlerFiletransferstatisticsapi(this.iRestHandlerFiletransferstatisticsapi);
    }

    @Override
    public FTMFileTransferConfigsStatisticsSummaryDTO getFileTransferConfigsSummary(String environment, String uuaa)
    {
        SingleApiClientResponseWrapper<FTMFileTransferConfigsStatisticsSummaryDTO> response = new SingleApiClientResponseWrapper<>();

        LOG.debug("[{}] -> [getFileTransferConfigsSummary]: getting File Transfer Configurations Summary for environment [{}] and UUAA [{}]", this.getClass().getSimpleName(), environment, uuaa);
        this.restHandlerFiletransferstatisticsapi.getFileTransferConfigsSummary(new IRestListenerFiletransferstatisticsapi()
        {
            @Override
            public void getFileTransferConfigsSummary(FTMFileTransferConfigsStatisticsSummaryDTO outcome)
            {
                LOG.debug("[{}] -> [getFileTransferConfigsSummary]: successfully called File Transfer Configurations Summary for environment [{}] and UUAA [{}]", this.getClass().getSimpleName(), environment, uuaa);
                response.set(outcome);
            }

            @Override
            public void getFileTransferConfigsSummaryErrors(Errors outcome)
            {
                LOG.error("[{}] -> [getFileTransferConfigsSummary]: Error trying to get File Transfer Configurations Summary for environment [{}] and UUAA [{}]: [{}}]",
                        this.getClass().getSimpleName(), environment, uuaa, outcome.getBodyExceptionMessage());
                throw new NovaException(StatisticsError.getFileTransferStatisticsError(), outcome,
                        String.format("[%s] -> [getFileTransferConfigsSummary]: Error trying to get File Transfer Configurations Summary for environment [%s] and UUAA [%s]",
                                this.getClass().getSimpleName(), environment, uuaa));
            }
        }, environment, uuaa);

        return response.get();
    }

    @Override
    public FTMFileTransfersInstancesStatisticsSummaryDTO getFileTransferInstancesSummary(String environment, String uuaa)
    {
        SingleApiClientResponseWrapper<FTMFileTransfersInstancesStatisticsSummaryDTO> response = new SingleApiClientResponseWrapper<>();

        LOG.debug("[{}] -> [getFileTransferInstancesSummary]: getting File Transfer Instances Summary for environment [{}] and UUAA [{}]", this.getClass().getSimpleName(), environment, uuaa);
        this.restHandlerFiletransferstatisticsapi.getFileTransfersInstancesSummary(new IRestListenerFiletransferstatisticsapi()
        {
            @Override
            public void getFileTransfersInstancesSummary(FTMFileTransfersInstancesStatisticsSummaryDTO outcome)
            {
                LOG.debug("[{}] -> [getFileTransferInstancesSummary]: successfully called File Transfer Instances Summary for environment [{}] and UUAA [{}]", this.getClass().getSimpleName(), environment, uuaa);
                response.set(outcome);
            }

            @Override
            public void getFileTransfersInstancesSummaryErrors(Errors outcome)
            {
                LOG.error("[{}] -> [getFileTransferInstancesSummary]: Error trying to get File Transfer Instances Summary for environment [{}] and UUAA [{}]: [{}}]",
                        this.getClass().getSimpleName(), environment, uuaa, outcome.getBodyExceptionMessage());
                throw new NovaException(StatisticsError.getUserServiceError(), outcome,
                        String.format("[%s] -> [getFileTransferInstancesSummary]: Error trying to get File Transfer Instances Summary for environment [%s] and UUAA [%s]",
                                this.getClass().getSimpleName(), environment, uuaa));
            }
        }, environment, uuaa);

        return response.get();
    }

    @Override
    public FTMFileTransferConfigHistorySnapshotDTO[] getFileTransferConfigsHistorySnapshot()
    {
        SingleApiClientResponseWrapper<FTMFileTransferConfigHistorySnapshotDTO[]> response = new SingleApiClientResponseWrapper<>();

        LOG.info("[{}] -> [getFileTransferConfigsHistorySnapshot]: getting File Transfer Configs for statistic history loading", this.getClass().getSimpleName());
        this.restHandlerFiletransferstatisticsapi.getFileTransferConfigsHistorySnapshot(new IRestListenerFiletransferstatisticsapi()
        {
            @Override
            public void getFileTransferConfigsHistorySnapshot(FTMFileTransferConfigHistorySnapshotDTO[] outcome)
            {
                LOG.info("[{}] -> [getFileTransferConfigsHistorySnapshot]: successfully got File Transfer Configs for statistic history loading", this.getClass().getSimpleName());
                response.set(outcome);
            }

            @Override
            public void getFileTransferConfigsHistorySnapshotErrors(Errors outcome)
            {
                LOG.error("[{}] -> [getFileTransferConfigsHistorySnapshot]: Error trying to get File Transfer Configs for statistic history loading: [{}}]",
                        this.getClass().getSimpleName(), outcome.getBodyExceptionMessage());
                throw new NovaException(StatisticsError.getUserServiceError(), outcome,
                        String.format("[%s] -> [getFileTransferConfigsHistorySnapshot]: Error trying to get File Transfer Configs for statistic history loading",
                                this.getClass().getSimpleName()));
            }
        });

        return response.get();
    }
}
