package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.filetransferadmin.client.feign.nova.rest.IRestHandlerFiletransferadmin;
import com.bbva.enoa.apirestgen.filetransferadmin.client.feign.nova.rest.IRestListenerFiletransferadmin;
import com.bbva.enoa.apirestgen.filetransferadmin.client.feign.nova.rest.impl.RestHandlerFiletransferadmin;
import com.bbva.enoa.apirestgen.filetransferadmin.model.FTATransferRelatedInfoAdminDTO;
import com.bbva.enoa.apirestgen.filetransferadmin.model.FileTransferConfigAdminDTO;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.exception.CommonError;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IFileTransferAdminClient;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class FileTransferAdminClientImpl implements IFileTransferAdminClient
{

    public static final String STATUSES_DELIMITER = ":";

    /**
     * REST handler interface
     */
    @Autowired
    private IRestHandlerFiletransferadmin iRestHandlerFiletransferadmin;

    /**
     * REST handler
     */
    private RestHandlerFiletransferadmin restHandlerFiletransferadmin;

    /**
     * Initialize the REST handler
     */
    @PostConstruct
    public void init()
    {
        this.restHandlerFiletransferadmin = new RestHandlerFiletransferadmin(this.iRestHandlerFiletransferadmin);
    }

    @Override
    public FileTransferConfigAdminDTO[] findFileTransferConfigByProductId(Integer productId)
    {
        return this.findFileTransferConfig(productId, null, null, null, false);
    }

    @Override
    public FileTransferConfigAdminDTO[] findFileTransferConfig(Integer productId, String fileTransferConfigName, String environment,
                                                               List<String> statuses, boolean getPendingTodoTasks)
    {
        SingleApiClientResponseWrapper<FileTransferConfigAdminDTO[]> response = new SingleApiClientResponseWrapper<>();

        String statusesAsString = statuses != null ? String.join(STATUSES_DELIMITER, statuses) : null;

        log.info("[{}] -> [findFileTransferConfig]: finding File Transfer Configurations Summary for product [{}], File Transfer Config name [{}], environment [{}]," +
                " statuses [{}] and getting pending TODO tasks [{}]", this.getClass().getSimpleName(), productId, fileTransferConfigName, environment, statusesAsString, getPendingTodoTasks);
        this.restHandlerFiletransferadmin.findFileTransferConfig(new IRestListenerFiletransferadmin()
        {
            @Override
            public void findFileTransferConfig(FileTransferConfigAdminDTO[] outcome)
            {
                log.info("[{}] -> [findFileTransferConfig]: successfully called File Transfer Configurations Summary for product [{}], File Transfer Config name [{}], environment [{}]," +
                        " statuses [{}] and getting pending TODO tasks [{}]", this.getClass().getSimpleName(), productId, fileTransferConfigName, environment, statusesAsString, getPendingTodoTasks);
                response.set(outcome);
            }

            @Override
            public void findFileTransferConfigErrors(Errors outcome)
            {
                String detail = "";
                if (outcome != null)
                {
                    Optional<ErrorMessage> firstErrorMessage = outcome.getFirstErrorMessage();
                    if (firstErrorMessage.isPresent())
                    {
                        detail = firstErrorMessage.get().getMessage();
                    }
                }
                log.error("[{}] -> [findFileTransferConfig]: Error trying to get File Transfer Configurations Summary for product [{}], File Transfer Config name [{}], environment [{}]," +
                        " statuses [{}] and getting pending TODO tasks [{}]: [{}]", this.getClass().getSimpleName(), productId, fileTransferConfigName, environment, statusesAsString, getPendingTodoTasks, detail);
                throw new NovaException(CommonError.getErrorCallingFileTransferAdminApi(this.getClass().getSimpleName(), detail));
            }
        }, fileTransferConfigName, statusesAsString, productId, environment, getPendingTodoTasks);

        return response.get();
    }


    @Override
    public FTATransferRelatedInfoAdminDTO[] getFileTransfersInformation(String environment, String uuaa, String filesystemName)
    {
        log.debug("[{}] -> [getFileTransfersInformation]: Calling File Transfer Service to obtain the information about product in environment transfers. Product uuaa [{}], environment [{}], " +
                        "filesystem name [{}]",
                this.getClass().getSimpleName(), uuaa, environment, filesystemName);

        SingleApiClientResponseWrapper<FTATransferRelatedInfoAdminDTO[]> response = new SingleApiClientResponseWrapper<>();

        this.restHandlerFiletransferadmin.getFileTransfersInformation(new IRestListenerFiletransferadmin()
        {
            @Override
            public void getFileTransfersInformation(FTATransferRelatedInfoAdminDTO[] outcome)
            {
                log.info("[{}] -> [getFileTransfersInformation]: successfully called FSManager transfer information for product [{}], environment [{}] and  filesystem name [{}]. Obtained [{}]",
                        this.getClass().getSimpleName(), uuaa, environment, filesystemName, outcome);
                response.set(outcome);
            }

            @Override
            public void getFileTransfersInformationErrors(Errors outcome)
            {

                log.error("[{}] -> [findFileTransferConfig]: Error trying to get transfer related to filesystem for product [{}], environment [{}], filesystem name [{}]", this.getClass().getSimpleName(), uuaa,
                        environment, filesystemName);

                // If we recive an error from the api, check the information about it, in case non info received the error is unexpected
                throw new NovaException(CommonError.getErrorCallingFileTransferAdminApi(this.getClass().getSimpleName(),
                        (outcome != null && outcome.getFirstErrorMessage().isPresent())
                        ? outcome.getFirstErrorMessage().get().getMessage()
                        : Constants.UNEXPECTED_ERROR_DEFINITION));
            }
        }, environment, uuaa, filesystemName);

        return response.get();
    }

}
