package com.bbva.enoa.platformservices.coreservice.filesystemsapi.listener;

import com.bbva.enoa.apirestgen.filesystemsapi.model.CreateNewFilesystemDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FFilesystemRelatedTransferInfoDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileLocationModel;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFileModelPaged;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemAlertInfoDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemUsage;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemConfigurationDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemTypeDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemsUsageReportDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.server.spring.nova.rest.IRestListenerFilesystemsapi;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsAlertService;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsService;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemsErrorConstants;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemsLiteralsConstants;
import com.bbva.enoa.utils.codegeneratorutils.metadata.MetadataUtils;
import com.bbva.enoa.utils.logutils.annotations.LogAndTrace;
import com.bbva.enoa.utils.logutils.exception.LogAndTraceException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

/**
 * Listener of the Filesystems API
 */
@Service
public class ListenerFilesystemsapi implements IRestListenerFilesystemsapi
{
    /**
     * Log of the class
     */
    private static final Logger LOG = LoggerFactory.getLogger(ListenerFilesystemsapi.class);

    /**
     * Filesystem service
     */
    private final IFilesystemsService filesystemsService;

    /**
     * Filesystem service
     */
    private final IFilesystemsAlertService filesystemsAlertService;

    /**
     * Constructor
     *
     * @param filesystemsService      filesystemsService
     * @param filesystemsAlertService file system alert service
     */
    @Autowired
    public ListenerFilesystemsapi(final IFilesystemsService filesystemsService, final IFilesystemsAlertService filesystemsAlertService)
    {
        this.filesystemsService = filesystemsService;
        this.filesystemsAlertService = filesystemsAlertService;
    }
    /////////////////////////////// IMPLEMENTATIONS //////////////////////////////////////////////////////////////////

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    @Override
    public FilesystemDto getFilesystem(final NovaMetadata novaMetadata, final Integer filesystemId) throws Errors
    {
        FilesystemDto filesystemDto = this.filesystemsService.getFilesystem(filesystemId);
        LOG.debug("[FilesystemsAPI] -> [getFilesystem]: Got the filesystem [{}]", filesystemDto);
        return filesystemDto;
    }

    /**
     * @param novaMetadata with the metadata
     * @param path         Landing zone path
     * @param environment  The environment of filesystem
     * @return the filesystem id
     * @throws Errors with an occurred exception
     */
    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    @Override
    public Integer getFilesystemId(NovaMetadata novaMetadata, String path, String environment) throws Errors
    {
        return this.filesystemsService.getFilesystemId(path, environment);
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE)
    @Override
    public void createDirectory(final NovaMetadata novaMetadata, final Integer filesystemId, final String directory) throws Errors
    {
        this.filesystemsService.createDirectory(MetadataUtils.getIvUser(novaMetadata), filesystemId, directory);
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE)
    @Override
    public void deleteFilesystem(final NovaMetadata novaMetadata, final Integer filesystemId) throws Errors
    {
        this.filesystemsService.deleteFilesystem(MetadataUtils.getIvUser(novaMetadata), filesystemId);
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE)
    @Override
    public void createFilesystem(final NovaMetadata novaMetadata, final CreateNewFilesystemDto filesystemToAdd, final Integer productId) throws Errors
    {
        try
        {
            this.filesystemsService.createFilesystem(filesystemToAdd, MetadataUtils.getIvUser(novaMetadata), productId);
        }
        catch (RuntimeException exception)
        {
            throw new LogAndTraceException(exception, productId);
        }
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE)
    @Override
    public void updateFilesystemPlatformConfiguration(final NovaMetadata novaMetadata, final FilesystemConfigurationDto[] configuration, final Integer filesystemId) throws Errors
    {
        this.filesystemsService.updateFilesystemPlatformConfiguration(configuration, filesystemId, MetadataUtils.getIvUser(novaMetadata));
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE)
    @Override
    public void updateFilesystemConfiguration(final NovaMetadata novaMetadata, final FilesystemConfigurationDto[] configuration, final Integer filesystemId) throws Errors
    {
        this.filesystemsService.updateFilesystemConfiguration(configuration, filesystemId, MetadataUtils.getIvUser(novaMetadata));
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    @Override
    public String[] getFilesystemsStatuses(NovaMetadata novaMetadata) throws Errors
    {
        return this.filesystemsService.getFilesystemsStatuses();
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE)
    @Override
    public void archiveFilesystem(final NovaMetadata novaMetadata, final Integer filesystemId) throws Errors
    {
        this.filesystemsService.archiveFilesystem(MetadataUtils.getIvUser(novaMetadata), filesystemId);
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE)
    @Override
    public void downloadToPre(NovaMetadata novaMetadata, final Integer filesystemId, final String filename, final String filesystemPath) throws Errors
    {
        this.filesystemsService.downloadToPre(MetadataUtils.getIvUser(novaMetadata), filesystemId, filename, filesystemPath);
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE)
    @Override
    public void deleteFile(final NovaMetadata novaMetadata, final FSFileLocationModel data, final Integer filesystemId) throws Errors
    {
        this.filesystemsService.deleteFile(data, MetadataUtils.getIvUser(novaMetadata), filesystemId);
        LOG.debug("[FilesystemsAPI] -> [deleteFile]: Deleted file from filesystem [{}] with location model [{}]", filesystemId, data);
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE)
    @Override
    public Boolean isFilesystemFrozen(final NovaMetadata novaMetadata, final Integer filesystemId) throws Errors
    {
        return this.filesystemsService.isFilesystemFrozen(filesystemId);
    }

    @Override
    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE)
    public void updateFilesystemQuota(NovaMetadata novaMetadata, Integer filesystemId, String filesystemPackCode) throws Errors
    {
        this.filesystemsService.updateFilesystemQuota(filesystemId, filesystemPackCode, MetadataUtils.getIvUser(novaMetadata));
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    @Override
    public FilesystemDto[] getProductFilesystems(final NovaMetadata novaMetadata, final Integer productId, final String environment, final String filesystemType) throws Errors
    {
        try
        {
            FilesystemDto[] filesystemDtoArray = this.filesystemsService.getProductFilesystems(productId, environment, filesystemType);
            LOG.debug("[FilesystemsAPI] -> [getProductFilesystems]: Got for product [{}] in env [{}] the filesystems [{}]", productId, environment, filesystemDtoArray);
            return filesystemDtoArray;
        }
        catch (RuntimeException exception)
        {
            throw new LogAndTraceException(
                    exception, productId, MessageFormat.format("[FilesystemsAPI] -> [getProductFilesystems]: error unexpected with product id: {0} , environment: {1} and filesystem type: {2}", productId, environment, filesystemType));
        }
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE)
    @Override
    public void restoreFilesystem(final NovaMetadata novaMetadata, final Integer filesystemId) throws Errors
    {
        this.filesystemsService.restoreFilesystem(MetadataUtils.getIvUser(novaMetadata), filesystemId);
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    @Override
    public FilesystemTypeDto[] listFilesystemTypes(final NovaMetadata novaMetadata, final Integer productId, final String environment) throws Errors
    {
        return filesystemsService.getAvailableFilesystemTypes(productId, environment);
    }


    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    @Override
    public FSFilesystemUsage getFilesystemUsage(final NovaMetadata novaMetadata, final Integer filesystemId) throws Errors
    {
        return this.filesystemsService.getFilesystemUsage(filesystemId);
    }

    @LogAndTrace(apiName = Constants.FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    @Override
    public FSFileModelPaged getFiles(final com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata novaMetadata, final Integer filesystemId, final String filterPath, final String filename, final String field, final Integer numberPage, final Integer sizePage, final String order) throws com.bbva.kltt.apirest.generator.lib.commons.exception.Errors
    {
        return filesystemsService.getFiles(filesystemId, filterPath, filename, numberPage, sizePage, field, order);
    }

    @Override
    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_MSG, debugLogLevel = true)
    public FSFilesystemAlertInfoDto[] getEventsAlertInfoConfig(NovaMetadata novaMetadata, Integer productId, String environment) throws Errors
    {
        return this.filesystemsAlertService.getAllFilesystemAlertConfigurations(MetadataUtils.getIvUser(novaMetadata), productId, environment);
    }

    @Override
    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_MSG, debugLogLevel = true)
    public FilesystemsUsageReportDTO getFilesystemsUsageReport(NovaMetadata novaMetadata, Integer productId, String environment) throws Errors
    {
        return this.filesystemsService.getFilesystemsUsageReport(productId, environment);
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_MSG, debugLogLevel = true)
    @Override
    public FSFilesystemAlertInfoDto getFilesystemAlertInfo(final NovaMetadata novaMetadata, final Integer filesystemId) throws Errors
    {
        return this.filesystemsAlertService.getFilesystemAlertConfiguration(filesystemId);
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE)
    @Override
    public void saveFilesystemAlertInfo(final NovaMetadata novaMetadata, final FSFilesystemAlertInfoDto alertInfo, final Integer filesystemId) throws Errors
    {
        this.filesystemsAlertService.updateFilesystemAlertConfiguration(MetadataUtils.getIvUser(novaMetadata), alertInfo, filesystemId);
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE)
    @Override
    public FFilesystemRelatedTransferInfoDTO[] getFilesystemsOnlineTransfersRelated(NovaMetadata novaMetadata, Integer productId, Integer filesystemId) throws Errors
    {
        return this.filesystemsService.getFilesystemsTransferInformation(productId, filesystemId);
    }

    @LogAndTrace(apiName = FilesystemsLiteralsConstants.FILESYSTEMS_API_NAME, runtimeExceptionErrorCode = FilesystemsErrorConstants.UNEXPECTED_ERROR_CODE, debugLogLevel = true)
    @Override
    public String[] getFilesystemsTypes(NovaMetadata novaMetadata) throws Errors
    {
        return this.filesystemsService.getFilesystemsTypes();
    }
}
