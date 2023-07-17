package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.impl;

import com.bbva.enoa.apirestgen.filesystemsapi.model.FSFilesystemAlertInfoDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemAlert;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions.FilesystemsError;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemAlertRepositoryManager;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsAlertBuilder;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsAlertService;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsValidator;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants.FilesystemPermissionsConstants;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Filesystems alert service.
 */
@Service
public class FilesystemsAlertServiceImpl implements IFilesystemsAlertService
{
    /**
     * Unauthorized exception
     */
    static final NovaException PERMISSIONS_EXCEPTION = new NovaException(FilesystemsError.getForbiddenError(), FilesystemsError.getForbiddenError().toString());

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemsAlertServiceImpl.class);

    /**
     * Filesystem Builder
     */
    private final IFilesystemsAlertBuilder filesystemsAlertBuilder;

    /**
     * Filesystem Builder
     */
    private final FilesystemRepository filesystemRepository;

    /**
     * Filesystem repository manager
     */
    private final IFilesystemAlertRepositoryManager filesystemAlertRepositoryManager;

    /**
     * Filesystem Validator
     */
    private final IFilesystemsValidator filesystemsValidator;

    /**
     * User service client
     */
    private final IProductUsersClient usersClient;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    /**
     * Instantiates a new Filesystems alert service.
     *
     * @param filesystemsAlertBuilder          the filesystems alert builder
     * @param filesystemRepository             the filesystem repository
     * @param filesystemAlertRepositoryManager the filesystem alert repository manager
     * @param usersClient                      the users client
     * @param filesystemsValidator             the filesystems validator
     * @param novaActivityEmitter              NovaActivity emitter
     */
    @Autowired
    public FilesystemsAlertServiceImpl(IFilesystemsAlertBuilder filesystemsAlertBuilder, FilesystemRepository filesystemRepository, IFilesystemAlertRepositoryManager filesystemAlertRepositoryManager, final IProductUsersClient usersClient,
                                       final IFilesystemsValidator filesystemsValidator, final INovaActivityEmitter novaActivityEmitter)
    {
        this.filesystemsAlertBuilder = filesystemsAlertBuilder;
        this.filesystemRepository = filesystemRepository;
        this.filesystemAlertRepositoryManager = filesystemAlertRepositoryManager;
        this.usersClient = usersClient;
        this.filesystemsValidator = filesystemsValidator;
        this.novaActivityEmitter = novaActivityEmitter;
    }

    @Transactional(readOnly = true)
    @Override
    public FSFilesystemAlertInfoDto getFilesystemAlertConfiguration(final Integer filesystemId) throws NovaException
    {
        if (filesystemId == null)
        {
            String message = "[FilesystemsAlertServiceImpl] -> [getFilesystemAlertConfiguration]: the filesystem Id couldn't be null";
            LOG.error(message);
            throw new NovaException(FilesystemsError.getNoSuchFilesystemError(filesystemId), message);
        }
        else
        {
            LOG.debug("[FilesystemsAlertServiceImpl] -> [getFilesystemAlertConfiguration]: obtaining filesystem alert configuration info for the filesystem with id [{}]", filesystemId);

            //Get the filesystem alert or throw exception in case not available
            FilesystemAlert filesystemAlert = this.filesystemAlertRepositoryManager.findFilesystemAlertFromFilesystemId(filesystemId).orElseThrow(() ->
            {
                String message = MessageFormat.format("[FilesystemsAlertServiceImpl] -> [getFilesystemAlertConfiguration]: the filesystem alert does not exists for filesystem id: {0}.", filesystemId);
                throw new NovaException(FilesystemsError.getNotFSAlertError(filesystemId), message);
            });

            LOG.debug("[FilesystemsAlertServiceImpl] -> [getFilesystemAlertConfiguration]: the filesystem alert configuration: [{}] has been obtained.", filesystemAlert.getId());

            // Build a filesystem DTO from filesystem
            FSFilesystemAlertInfoDto filesystemAlertDto = this.filesystemsAlertBuilder.buildFilesystemsAlertInfoDTO(filesystemAlert);
            LOG.debug("[FilesystemsAlertServiceImpl] -> [getFilesystemAlertConfiguration]: built filesystem alert DTO: [{}] from filesystemId [{}]. \nReturning it...", filesystemAlertDto, filesystemId);
            return filesystemAlertDto;
        }
    }

    @Transactional
    @Override
    public void updateFilesystemAlertConfiguration(String ivUser, FSFilesystemAlertInfoDto filesystemAlertInfoDto, Integer filesystemId) throws NovaException
    {
        LOG.debug("[FilesystemsAlertServiceImpl] -> [updateFilesystemAlertConfiguration]: Generating the filesystem alert from DTO received: [{}], for the filesystem with Id: [{}]", filesystemAlertInfoDto.toString(), filesystemId);
        // Validate the filesystem
        Filesystem filesystem = this.filesystemsValidator.validateAndGetFilesystem(filesystemId);

        // Check if the user has permission
        this.usersClient.checkHasPermission(ivUser, FilesystemPermissionsConstants.FILESYSTEM_MODIFY_ALERT_PERMISSION, filesystem.getEnvironment(), filesystem.getProduct().getId(), PERMISSIONS_EXCEPTION);
        LOG.debug("[FilesystemsAlertServiceImpl] -> [updateFilesystemAlertConfiguration]: Obtaining old filesystem alert configuration info for the filesystem with id [{}]", filesystemId);

        // Validate product has remedy group if Patrol is being activated
        if (productPatrolIsActive(filesystemAlertInfoDto) && !productHasRemedyGroup(filesystem.getProduct()))
        {
            throw new NovaException(FilesystemsError.generateProductWithoutRemedyGroupError());
        }

        //Get the old filesystem alert to check the non change fields, or else throw an exception
        FilesystemAlert filesystemAlertToUpdate = this.filesystemAlertRepositoryManager.findFilesystemAlertFromFilesystemId(filesystemId).orElseThrow(() -> {
            String message = MessageFormat.format("[FilesystemsAlertServiceImpl] -> [updateFilesystemAlertConfiguration]: The filesystem alert does note exists for filesystem id: {0}.", filesystemId);
            throw new NovaException(FilesystemsError.getNotFSAlertError(filesystemId), message);
        });

        //Building the new alert, merging the old and new data
        LOG.debug("[FilesystemsAlertServiceImpl] -> [updateFilesystemAlertConfiguration]: Building updated filesystem alert configuration info for the filesystem with id [{}]", filesystemId);
        filesystemAlertToUpdate = this.filesystemsAlertBuilder.buildFilesystemAlert(filesystemAlertInfoDto, filesystemAlertToUpdate);
        try
        {
            //Updating in repository
            LOG.debug("[FilesystemsAlertServiceImpl] -> [updateFilesystemAlertConfiguration]: Persisting the filesystem alert: [{}]", filesystemAlertToUpdate.toString());
            this.filesystemAlertRepositoryManager.updateFilesystemAlert(filesystemAlertToUpdate);
        }
        catch (RuntimeException exception)
        {
            LOG.error("[FilesystemsAlertServiceImpl] -> [updateFilesystemAlertConfiguration]: The database transaction register a problem saving the filesystem alert referred to filesystem with id: [{}]", filesystemId);
            throw new NovaException(FilesystemsError.getDatabaseFSAlertError(filesystemId));
        }

        // Emit Edit Log Event Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(filesystem.getProduct().getId(), ActivityScope.FILESYSTEM_EVENT, ActivityAction.EDITED)
                .entityId(filesystemId)
                .environment(filesystem.getEnvironment())
                .addParam("fileSystemType", filesystem.getType().getFileSystemType())
                .addParam("landingZonePath", filesystem.getLandingZonePath())
                .addParam("fileSystemName", filesystem.getName())
                .addParam("changeType", "updateFilesystemAlertConfiguration")
                .addParam("alertPercent", filesystemAlertInfoDto.getAlertPercentage() == null ? Constants.NOT_AVAILABLE_VALUE : String.valueOf(filesystemAlertInfoDto.getAlertPercentage()))
                .addParam("isAlertEnable", filesystemAlertInfoDto.getIsActive() == null ? Constants.NOT_AVAILABLE_VALUE : String.valueOf(filesystemAlertInfoDto.getIsActive()))
                .addParam("timeBetweenAlerts", filesystemAlertInfoDto.getTimeBetweenAlerts() == null ? Constants.NOT_AVAILABLE_VALUE : String.valueOf(filesystemAlertInfoDto.getTimeBetweenAlerts()))
                .addParam("isMailEnable", filesystemAlertInfoDto.getIsMail() == null ? Constants.NOT_AVAILABLE_VALUE : String.valueOf(filesystemAlertInfoDto.getIsMail()))
                .addParam("emailAddresses", filesystemAlertInfoDto.getEmailAddresses() == null ? Constants.NOT_AVAILABLE_VALUE : filesystemAlertInfoDto.getEmailAddresses())
                .build());
    }

    @Transactional(readOnly = true)
    @Override
    public FSFilesystemAlertInfoDto[] getAllFilesystemAlertConfigurations(String ivUser, Integer productId, String environment) throws NovaException
    {
        LOG.debug("[FilesystemsAlertServiceImpl] -> [getAllFilesystemAlertConfigurations]: Getting the Filesystem alerts, for the product with Id: [{}], in the environment [{}]",
                productId, StringUtils.upperCase(environment));

        List<FSFilesystemAlertInfoDto> fsFilesystemAlertInfoArrayDtoToReturn;

        //Checking the received values to validate them
        if (productId == null || environment == null || (!environment.equals(Environment.INT.getEnvironment()) && !environment.equals(Environment.PRE.getEnvironment()) && !environment.equals(Environment.PRO.getEnvironment())))
        {
            throw new NovaException(FilesystemsError.getBadArgumentsError(), "[FilesystemsAlertServiceImpl] -> [getAllFilesystemAlertConfigurations]: The values defined in the request are not valid.");
        }
        else
        {
            LOG.debug("[FilesystemsAlertServiceImpl] -> [updateFilesystemAlertConfiguration]: Obtaining filesystem ids, to check alert data configuration info for the environment [{}], and productId [{}]",
                    environment.toUpperCase(), productId);

            List<Filesystem> filesystemListToCheck = this.filesystemRepository.findByProductIdAndEnvironmentOrderByCreationDateDesc(productId, environment);

            if (filesystemListToCheck == null)
            {
                String message = MessageFormat.format("[FilesystemsAlertServiceImpl] -> [getAllFilesystemAlertConfigurations]: An error occurred checking the information in the database" +
                        "for productId [{0}] and environment [{1}].", productId, Environment.valueOf(environment));
                throw new NovaException(FilesystemsError.getDatabaseFSError(), message);
            }
            else if (filesystemListToCheck.isEmpty())
            {
                //In case we obtain an empty list, return an empty response
                return new FSFilesystemAlertInfoDto[0];
            }
            else
            {
                LOG.debug("[FilesystemsAlertServiceImpl] -> [updateFilesystemAlertConfiguration]: Generating the array of FSFilesystemAlertInfo to return.");

                fsFilesystemAlertInfoArrayDtoToReturn = generateNewFSFilesystemAlertInfoDtoArrayFromFsList(filesystemListToCheck);
            }
        }

        return fsFilesystemAlertInfoArrayDtoToReturn.toArray(new FSFilesystemAlertInfoDto[0]);

    }


    /**
     * Method used for obtain all the FSAlerts from a list of filesystems provided.
     * It goes along the list, check the permissions, and obtain from database the
     * alert configuration info associated to the filesystem Id
     *
     * @param filesystemListToCheck filesystem list to get alerts
     * @return not null list of FSFilesystemAlertInfoDto found
     */
    private List<FSFilesystemAlertInfoDto> generateNewFSFilesystemAlertInfoDtoArrayFromFsList(List<Filesystem> filesystemListToCheck)
    {

        LOG.debug("[FilesystemsAlertServiceImpl] -> [getAllFilesystemAlertConfigurations]: Obtain and generate the alert config DTO to send, from the filesystems list provided: [{}]", filesystemListToCheck);

        List<FSFilesystemAlertInfoDto> fsFilesystemAlertInfoDto = new ArrayList<>();

        filesystemListToCheck.forEach((filesystem) -> {

            // Validate the filesystem
            Filesystem filesystemValidated = this.filesystemsValidator.validateAndGetFilesystem(filesystem.getId());

            //Obtaining the filesystem alert configuration from the FilesystemId
            FilesystemAlert filesystemAlertChecked = this.filesystemAlertRepositoryManager.findFilesystemAlertFromFilesystemId(filesystemValidated.getId()).orElseThrow(() ->
            {
                String message = MessageFormat.format("[FilesystemsAlertServiceImpl] -> [generateNewFSFilesystemAlertInfoDtoArrayFromFsList]: An error occurred checking the information in the database" +
                        "for FilesystemId: {0}.", filesystemValidated.getId());
                throw new NovaException(FilesystemsError.getNotFSAlertError(filesystemValidated.getId()), message);

            });

            //Adding to the return list, the DTO built on the filesystemsAlertBuilder
            fsFilesystemAlertInfoDto.add(this.filesystemsAlertBuilder.buildFilesystemsAlertInfoDTO(filesystemAlertChecked));

        });


        return fsFilesystemAlertInfoDto;

    }

    private boolean productPatrolIsActive(FSFilesystemAlertInfoDto filesystemAlertInfoDto)
    {
        return filesystemAlertInfoDto.getIsActive() && filesystemAlertInfoDto.getIsPatrol();
    }

    private boolean productHasRemedyGroup(Product product)
    {
        return product.getRemedySupportGroup() != null && !product.getRemedySupportGroup().isEmpty();
    }


}
