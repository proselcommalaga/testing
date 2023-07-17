package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransferConfigHistorySnapshotDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransferConfigsStatisticsSummaryDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransfersInstancesStatisticsSummaryDTO;

public interface IFileTransferStatisticsClient
{

    /**
     * Get statistics related to the File Transfer Configurations stored in the platform, such as the total number or their status.
     * The results can be filtered by Environment and UUAA.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @return A FTMFileTransferConfigsStatisticsSummaryDTO.
     */
    FTMFileTransferConfigsStatisticsSummaryDTO getFileTransferConfigsSummary(String environment, String uuaa);

    /**
     * Get statistics related to the File Transfer Instances stored in the platform, such as the total number or their status.
     * The results can be filtered by Environment and UUAA.
     *
     * @param environment Filter the results for a specific Environment. If it's equals to "ALL", or it's null, or it's empty, no Environment filtering is applied.
     * @param uuaa        Filter the results for a specific UUAA. If it's equals to "ALL", or it's null, or it's empty, no UUAA filtering is applied.
     * @return A FTMFileTransfersInstancesStatisticsSummaryDTO.
     */
    FTMFileTransfersInstancesStatisticsSummaryDTO getFileTransferInstancesSummary(String environment, String uuaa);

    /**
     * Gets an array of DTOs having file transfers information for statistic history loading.
     *
     * @return an array of DTOs having file transfers information for statistic history loading.
     */
    FTMFileTransferConfigHistorySnapshotDTO[] getFileTransferConfigsHistorySnapshot();
}
