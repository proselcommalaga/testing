package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.filetransferadmin.model.FTATransferRelatedInfoAdminDTO;
import com.bbva.enoa.apirestgen.filetransferadmin.model.FileTransferConfigAdminDTO;

import java.util.List;

public interface IFileTransferAdminClient
{

    /**
     * Find File Transfer Config in a Product.
     *
     * @param productId The given Product ID.
     * @return An array of File Transfer Config.
     */
    FileTransferConfigAdminDTO[] findFileTransferConfigByProductId(Integer productId);

    /**
     * Find File Transfer Config with the given parameters.
     *
     * @param productId                 The given Product ID.
     * @param fileTransferConfigName    The given name of the File Transfer Config.
     * @param environment               The given environment.
     * @param statuses                  A list of statuses.
     * @param getPendingTodoTasks       Whether to get the pending TO-DO tasks associated to each File Transfer Config.
     * @return An array of File Transfer Config.
     */
    FileTransferConfigAdminDTO[] findFileTransferConfig(Integer productId, String fileTransferConfigName, String environment,
                                                        List<String> statuses, boolean getPendingTodoTasks);


    /**
     * Get file transfers information fta transfer related info admin dto [ ].
     *
     * @param environment    the environment
     * @param uuaa           the uuaa
     * @param filesystemName the filesystem name
     * @return the fta transfer related info admin dto [ ]
     */
    FTATransferRelatedInfoAdminDTO[] getFileTransfersInformation(String environment, String uuaa, String filesystemName);
}
