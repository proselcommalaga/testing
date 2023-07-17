package com.bbva.enoa.platformservices.coreservice.packsapi.listener;

import com.bbva.enoa.apirestgen.packsapi.model.BrokerPackDto;
import com.bbva.enoa.apirestgen.packsapi.model.FilesystemPackDto;
import com.bbva.enoa.apirestgen.packsapi.model.HardwarePackDto;
import com.bbva.enoa.apirestgen.packsapi.server.spring.nova.rest.IRestListenerPacksapi;
import com.bbva.enoa.platformservices.coreservice.packsapi.services.interfaces.IPacksService;
import com.bbva.enoa.platformservices.coreservice.packsapi.util.Constants;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Listener to packsapi.
 */
@Slf4j
@Service
public class ListenerPacksapi implements IRestListenerPacksapi
{
    /**
     * parck service
     */
    private final IPacksService packsService;

    /**
     * @param packsService Packs service
     */
    @Autowired
    public ListenerPacksapi(final IPacksService packsService)
    {
        this.packsService = packsService;
    }


    @Override
    @LogAndTrace(apiName = Constants.PACKS_API_NAME, runtimeExceptionErrorCode = Constants.PackConstantErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public HardwarePackDto getOneHardwarePack(final NovaMetadata novaMetadata, final Integer packId) throws Errors
    {
        return this.packsService.getOneHardwarePack(packId);
    }

    @Override
    @LogAndTrace(apiName = Constants.PACKS_API_NAME, runtimeExceptionErrorCode = Constants.PackConstantErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public FilesystemPackDto[] getFilesystemPacks(final NovaMetadata novaMetadata, final String filesystemType) throws Errors
    {
        return this.packsService.getFilesystemPacks(filesystemType);
    }

    @Override
    @LogAndTrace(apiName = Constants.PACKS_API_NAME, runtimeExceptionErrorCode = Constants.PackConstantErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public HardwarePackDto[] getHardwarePacks(final NovaMetadata novaMetadata, final Boolean allStoredPacks, final String hardwarePackType, final String jvmVersion) throws Errors
    {
        //William
        log.debug("Parametros", allStoredPacks, hardwarePackType, jvmVersion );
        return allStoredPacks ? this.packsService.getAllHardwarePacks() : this.packsService.getHardwarePacks(jvmVersion, hardwarePackType);
    }

    @Override
    @LogAndTrace(apiName = Constants.PACKS_API_NAME, runtimeExceptionErrorCode = Constants.PackConstantErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public FilesystemPackDto getOneFilesystemPack(final NovaMetadata novaMetadata, final Integer packId)
    {
        return this.packsService.getOneFilesystemPack(packId);
    }

    @Override
    @LogAndTrace(apiName = Constants.PACKS_API_NAME, runtimeExceptionErrorCode = Constants.PackConstantErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BrokerPackDto[] getBrokerPacks(NovaMetadata novaMetadata, String hardwarePackType) throws Errors
    {
        return this.packsService.getBrokerPacks(hardwarePackType);
    }

    @Override
    @LogAndTrace(apiName = Constants.PACKS_API_NAME, runtimeExceptionErrorCode = Constants.PackConstantErrors.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    public BrokerPackDto getOneBrokerPack(NovaMetadata novaMetadata, Integer packId) throws Errors {
        return this.packsService.getOneBrokerPack(packId);
    }

}
