package com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASRequestAlertsDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.model.BasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FSDeploymentServiceDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FileTransferDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemConfigurationDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.LandingZoneDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.PendingTaskDto;
import com.bbva.enoa.apirestgen.filesystemsapi.model.RequestAlertsDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novaheader.context.NovaContext;
import com.bbva.enoa.datamodel.model.datastorage.entities.Brick;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemAlert;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemEpsilonEther;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemEther;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.FilesystemType;
import com.bbva.enoa.datamodel.model.datastorage.enumerates.LandingZoneStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceFilesystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.resource.entities.FilesystemPack;
import com.bbva.enoa.datamodel.model.todotask.entities.FilesystemTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.utils.HibernateUtils;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IProductBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.FilesystemTaskRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IAlertServiceApiClient;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.exceptions.FilesystemsError;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemAlertRepositoryManager;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsBuilder;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


/**
 * Filesystem Builder
 */
@Service
public class FilesystemsBuilderImpl implements IFilesystemsBuilder
{
    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(FilesystemsBuilderImpl.class);
    /**
     * Filesystem repository
     */
    private final FilesystemRepository filesystemRepository;
    /**
     * Filesystem task repository
     */
    private final FilesystemTaskRepository filesystemTaskRepository;
    /**
     * ProductBudgets service
     */
    private final IProductBudgetsService productBudgetsService;

    /**
     * Manage validation utils
     */
    private final ManageValidationUtils manageValidationUtils;

    /**
     * Nova Context for getting user code
     */
    private final NovaContext novaContext;

    /**
     * Alert service client
     */
    private final IAlertServiceApiClient alertServiceClient;

    /**
     * Filesystem Alert repository manager
     */
    private final IFilesystemAlertRepositoryManager filesystemAlertRepositoryManager;

    /**
     * Constructor
     *
     * @param filesystemRepository             filesystemRepository
     * @param productBudgetsService            productBudgetsService
     * @param manageValidationUtils            the manage validation utils
     * @param novaContext                      the nova context
     * @param filesystemTaskRepository         filesystem task repository
     * @param alertServiceClient               the alert service client
     * @param filesystemAlertRepositoryManager the filesystem alert repository manager
     */
    @Autowired
    public FilesystemsBuilderImpl(final FilesystemRepository filesystemRepository,
                                  final IProductBudgetsService productBudgetsService,
                                  final ManageValidationUtils manageValidationUtils,
                                  final NovaContext novaContext,
                                  final FilesystemTaskRepository filesystemTaskRepository,
                                  final IAlertServiceApiClient alertServiceClient,
                                  final IFilesystemAlertRepositoryManager filesystemAlertRepositoryManager)
    {
        this.filesystemRepository = filesystemRepository;
        this.productBudgetsService = productBudgetsService;
        this.manageValidationUtils = manageValidationUtils;
        this.novaContext = novaContext;
        this.filesystemTaskRepository = filesystemTaskRepository;
        this.alertServiceClient = alertServiceClient;
        this.filesystemAlertRepositoryManager = filesystemAlertRepositoryManager;
    }

    /////////////////////////////////// IMPLEMENTATION ///////////////////////////////////////

    @Override
    public FilesystemDto[] buildFilesystemDTOArray(final List<Filesystem> filesystemList)
    {
        LOG.trace("[FilesystemsAPI] -> [buildFilesystemDTOArray]: building a filesystemList DTO array from the filesystem list: [{}]", filesystemList);

        FilesystemDto[] filesystemDtoArray = new FilesystemDto[filesystemList.size()];

        int i = 0;
        for (Filesystem filesystem : filesystemList)
        {
            filesystemDtoArray[i] = this.buildFilesystemDTO(filesystem);
            i++;
        }

        LOG.trace("[FilesystemsAPI] -> [buildFilesystemDTOArray]: built the following filesystemList DTO array: [{}]", Arrays.toString(filesystemDtoArray));

        return filesystemDtoArray;
    }

    @Transactional(readOnly = true)
    @Override
    public FilesystemDto buildFilesystemDTO(final Filesystem filesystem)
    {
        // Copy properties.
        FilesystemDto filesystemDto = new FilesystemDto();
        BeanUtils.copyProperties(filesystem, filesystemDto);

        filesystemDto.setProductId(filesystem.getProduct().getId());
        filesystemDto.setFilesystemDescription(filesystem.getDescription());

        // Build the pack.
        FilesystemPack pack = filesystem.getFilesystemPack();
        filesystemDto.setFilesystemPackSize(pack.getSizeMB());
        filesystemDto.setFilesystemPackCode(pack.getCode());
        filesystemDto.setFilesystemPackPrice(this.productBudgetsService.getFilesystemPackInfo(pack.getId()).getPackPrice());

        if (filesystem.getType() == FilesystemType.FILESYSTEM)
        {
            FilesystemNova fsNova = HibernateUtils.getFSNovaFromHibernateProxy(filesystem);
            filesystemDto.setVolumeId(fsNova.getVolumeId());
            filesystemDto.setNfs(fsNova.getNfs());

            // Get a list of gluster node name using the filesystem
            Set<Brick> brickSet = fsNova.getBricks();
            filesystemDto.setBriks(brickSet.stream().map(glusterNode -> glusterNode.getGlusterNode().getHostName()).toArray(String[]::new));
        }
        else if (filesystem.getType() == FilesystemType.FILESYSTEM_EPSILON_ETHER)
        {
            FilesystemEpsilonEther fsEpsilonEther;
            if (filesystem instanceof HibernateProxy)
            {
                fsEpsilonEther = (FilesystemEpsilonEther) ((HibernateProxy) filesystem).getHibernateLazyInitializer().getImplementation();
            }
            else
            {
                fsEpsilonEther = (FilesystemEpsilonEther) filesystem;
            }

            final FilesystemConfigurationDto[] configurationDtoArray = new FilesystemConfigurationDto[7];
            configurationDtoArray[0] = buildConfigurationDTO("transition", String.valueOf(fsEpsilonEther.getEpsilonProperties().getTransition()));
            configurationDtoArray[1] = buildConfigurationDTO("expiration", String.valueOf(fsEpsilonEther.getEpsilonProperties().getExpiration()));
            configurationDtoArray[2] = buildConfigurationDTO("backend", fsEpsilonEther.getEpsilonProperties().getBackend());
            configurationDtoArray[3] = buildConfigurationDTO("store", fsEpsilonEther.getEpsilonProperties().getStore());
            configurationDtoArray[4] = buildConfigurationDTO("psiTopic", fsEpsilonEther.getEpsilonProperties().getPsiTopic());
            configurationDtoArray[5] = buildConfigurationDTO("limitNSSize", fsEpsilonEther.getEpsilonProperties().getLimitNSSize()+" MB");
            configurationDtoArray[6] = buildConfigurationDTO("namespace", fsEpsilonEther.getEtherNs());

            filesystemDto.setConfiguration(configurationDtoArray);

            // set the size
            filesystemDto.setFilesystemPackSize(fsEpsilonEther.getEpsilonProperties().getLimitNSSize());
        }

        filesystemDto.setEnvironment(filesystem.getEnvironment());
        filesystemDto.setFilesystemType(filesystem.getType().getFileSystemType());

        String environmentFolder = "ei";

        switch (Environment.valueOf(filesystem.getEnvironment()))
        {
            case INT:
                environmentFolder = "ei";
                break;
            case PRE:
                environmentFolder = "pp";
                break;
            case PRO:
                environmentFolder = "pr";
                break;
        }

        final String pathPattern = "/usr/local/{0}/nova{1}{2}{3}";

        // the transfer dto
        final FileTransferDto transferDto = new FileTransferDto();

        // Landing zone.
        if (filesystem.getType() == FilesystemType.FILESYSTEM)
        {
            transferDto.setLandingZonePath(filesystem.getLandingZonePath());
            transferDto.setLandingZonePathIncoming(MessageFormat.format(pathPattern, environmentFolder, "/landingzone", filesystem.getLandingZonePath(), "/incoming/"));
            transferDto.setLandingZonePathOutgoing(MessageFormat.format(pathPattern, environmentFolder, "/outgoing", filesystem.getLandingZonePath(), "/outgoing/"));
            transferDto.setLandingZonePathTracesIncoming(MessageFormat.format(pathPattern, environmentFolder, "/filewatcher", filesystem.getLandingZonePath(), "/incoming/"));
            transferDto.setLandingZonePathTracesOutgoing(MessageFormat.format(pathPattern, environmentFolder, "/filewatcher", filesystem.getLandingZonePath(), "/outgoing/"));
        }
        else if (filesystem.getType() == FilesystemType.FILESYSTEM_ETHER)
        {
            FilesystemEther fsEther;
            if (filesystem instanceof HibernateProxy)
            {
                fsEther = (FilesystemEther) ((HibernateProxy) filesystem).getHibernateLazyInitializer().getImplementation();
            }
            else
            {
                fsEther = (FilesystemEther) filesystem;
            }

            transferDto.setLandingZoneURL(fsEther.getEtherLandingZoneUrl());
            transferDto.setLandingZonePath(fsEther.getLandingZonePath());
            transferDto.setLandingZonePathIncoming(MessageFormat.format(pathPattern, environmentFolder, "/etherlandingzoneincoming", filesystem.getLandingZonePath(), "/"));
            transferDto.setLandingZonePathOutgoing(MessageFormat.format(pathPattern, environmentFolder, "/etherlandingzoneoutgoing", filesystem.getLandingZonePath(), "/"));
            transferDto.setLandingZonePathTracesIncoming(MessageFormat.format(pathPattern, environmentFolder, "/etherlandingzonetraces", filesystem.getLandingZonePath(), "/"));
        }
        else if (filesystem.getType() == FilesystemType.FILESYSTEM_EPSILON_ETHER)
        {
            transferDto.setLandingZonePath(filesystem.getLandingZonePath());
            transferDto.setLandingZonePathIncoming("");
            transferDto.setLandingZonePathOutgoing("");
            transferDto.setLandingZonePathTracesIncoming("");
        }
        filesystemDto.setFileTransferProperties(transferDto);

        // List of landing zone hosts.
        if (filesystem.getFilesystemLandingZones() != null)
        {
            LandingZoneDto[] landingZones = filesystem.getFilesystemLandingZones()
                    .stream()
                    // Exclude filesystemLandingZones in status ARCHIVED
                    .filter(fslz -> fslz.getLandingZoneStatus() != LandingZoneStatus.ARCHIVED)
                    // Transform remaining filesystemLandingZones to LandingZoneDto
                    .map(fslz -> {
                        LandingZoneDto newLandingZoneDto = new LandingZoneDto();
                        newLandingZoneDto.setHostport(fslz.getLandingZone().getHostPort());
                        newLandingZoneDto.setStatus(fslz.getLandingZoneStatus().name());

                        return newLandingZoneDto;
                    })
                    // Collect into an array
                    .toArray(LandingZoneDto[]::new);

            if (landingZones.length > 0)
            {
                transferDto.setLandingZones(landingZones);
            }
        }

        filesystemDto.setCreationDate(filesystem.getCreationDate().getTimeInMillis());

        if (filesystem.getDeletionDate() != null)
        {
            filesystemDto.setDeletionDate(filesystem.getDeletionDate().getTimeInMillis());
        }

        if (filesystem.getLastModified() != null)
        {
            filesystemDto.setLastModified(filesystem.getLastModified().getTimeInMillis());
        }

        filesystemDto.setFilesystemDescription(filesystem.getDescription());
        filesystemDto.setFilesystemName(filesystem.getName());
        filesystemDto.setStatus(filesystem.getFilesystemStatus().name());

        // Get a list of services using the filesystem.
        filesystemDto.setServices(this.buildServiceDtos(filesystem.getDeploymentServiceFilesystems()));

        if (filesystem.getType() == FilesystemType.FILESYSTEM_EPSILON_ETHER)
        {
            // epsilon filesystem can not be archived
            filesystemDto.setCanBeArchived(false);

            // if the fs is linked can not be deleted
            filesystemDto.setCanBeDeleted(true);
        }
        else
        {
            // Can be archived if not used on a deployed plan.
            filesystemDto.setCanBeArchived(!filesystemRepository.filesystemIsUsedOnDeployedPlan(filesystem.getId()));

            // Can be deleted if not used on any plan.
            filesystemDto.setCanBeDeleted(!filesystemRepository.filesystemIsUsed(filesystem.getId()));
        }

        // By default, can be manage by user
        filesystemDto.setCanBeManagedByUser(true);
        // Check all services where file system is used
        for (DeploymentServiceFilesystem deploymentServiceFilesystem : filesystem.getDeploymentServiceFilesystems())
        {
            // If the plan of the service can not be manage by user and status plan is deployed -> File system can not be manage by user
            if (deploymentServiceFilesystem.getDeploymentService().getDeploymentSubsystem().getDeploymentPlan().getStatus().equals(DeploymentStatus.DEPLOYED) &&
                    !this.manageValidationUtils.checkIfPlanCanBeManagedByUser(this.novaContext.getIvUser(), deploymentServiceFilesystem.getDeploymentService().getDeploymentSubsystem().getDeploymentPlan()))
            {
                filesystemDto.setCanBeManagedByUser(false);
            }
        }

        // Adding the filesystem task
        List<FilesystemTask> filesystemTaskList = this.filesystemTaskRepository.findByFilesystemId(filesystem.getId());

        List<PendingTaskDto> pendingTaskDtoList = new ArrayList<>();

        filesystemTaskList.stream().filter(filesystemTask -> filesystemTask.getStatus() == ToDoTaskStatus.PENDING || filesystemTask.getStatus() == ToDoTaskStatus.PENDING_ERROR).forEach(filesystemTask ->
        {
            PendingTaskDto pendingTaskDto = new PendingTaskDto();
            pendingTaskDto.setAssignedRole(filesystemTask.getAssignedGroup().name());
            pendingTaskDto.setIsTaskOfError(filesystemTask.getTaskType().isTaskOfError());
            pendingTaskDto.setTodoTaskId(filesystemTask.getId());
            pendingTaskDto.setTodoTaskType(filesystemTask.getTaskType().name());
            pendingTaskDtoList.add(pendingTaskDto);
        });

        filesystemDto.setHasPendingTask(pendingTaskDtoList.toArray(new PendingTaskDto[0]));

        // Add Pending Alerts
        filesystemDto.setHasPendingAlerts(this.buildAlertInfoInformation(filesystem));

        LOG.debug("[FilesystemsAPI] -> [buildFilesystemDTO]: added the following number: [{}] of pending filesystem task: [{}]", pendingTaskDtoList.size(), pendingTaskDtoList);

        // Finally, return.
        LOG.trace("[FilesystemsAPI] -> [buildFilesystemDTO]: built filesystem with values: [{}]", filesystemDto);
        return filesystemDto;
    }

    /////////////////////////////////// PRIVATE METHODS ///////////////////////////////////

    /**
     * Builds an array of {@link FSDeploymentServiceDto} from the {@link DeploymentService}
     * using a {@link Filesystem}.
     *
     * @param deploymentServiceFilesystems List of DeploymentServiceFilesystem
     * @return Array of DTOs.
     */
    private FSDeploymentServiceDto[] buildServiceDtos(List<DeploymentServiceFilesystem> deploymentServiceFilesystems)
    {
        List<FSDeploymentServiceDto> serviceDtos = new ArrayList<>();

        for (DeploymentServiceFilesystem deploymentServiceFilesystem : deploymentServiceFilesystems)
        {
            FSDeploymentServiceDto fsDeploymentServiceDto = new FSDeploymentServiceDto();

            ReleaseVersionService releaseVersionService = deploymentServiceFilesystem.getDeploymentService().getService();

            // Copy properties from original service.
            fsDeploymentServiceDto.setServiceName(releaseVersionService.getServiceName());
            fsDeploymentServiceDto.setVersion(releaseVersionService.getVersion());
            fsDeploymentServiceDto.setArtifactId(releaseVersionService.getArtifactId());
            fsDeploymentServiceDto.setGroupId(releaseVersionService.getGroupId());
            fsDeploymentServiceDto.setNumberOfInstances(deploymentServiceFilesystem.getDeploymentService().getNumberOfInstances());
            fsDeploymentServiceDto.setVolumeBind(deploymentServiceFilesystem.getVolumeBind());
            fsDeploymentServiceDto.setId(deploymentServiceFilesystem.getDeploymentService().getId());
            fsDeploymentServiceDto.setServiceFinalname(releaseVersionService.getFinalName());
            fsDeploymentServiceDto.setServiceType(releaseVersionService.getServiceType());
            fsDeploymentServiceDto.setDescription(releaseVersionService.getDescription());

            // Deployment deployment plan data.
            DeploymentPlan deploymentPlan = deploymentServiceFilesystem.getDeploymentService().getDeploymentSubsystem().getDeploymentPlan();

            fsDeploymentServiceDto.setPlanId(deploymentPlan.getId());
            fsDeploymentServiceDto.setPlanStatus(deploymentPlan.getStatus().name());

            // Release and version.
            fsDeploymentServiceDto.setReleaseId(deploymentPlan.getReleaseVersion().getRelease().getId());
            fsDeploymentServiceDto.setReleaseName(deploymentPlan.getReleaseVersion().getRelease().getName());
            fsDeploymentServiceDto.setReleaseVersionId(deploymentPlan.getReleaseVersion().getId());
            fsDeploymentServiceDto.setReleaseVersionName(deploymentPlan.getReleaseVersion().getVersionName());

            // Product.
            Product product = deploymentPlan.getReleaseVersion().getRelease().getProduct();
            fsDeploymentServiceDto.setProductId(product.getId());
            fsDeploymentServiceDto.setProductName(product.getName());

            // Add the DTO to the list.
            serviceDtos.add(fsDeploymentServiceDto);
        }

        LOG.trace("[FilesystemsAPI] -> [buildServiceDtos]: built list of services using the filesystem with value: [{}]", serviceDtos);

        // Return as an array.
        return serviceDtos.toArray(new FSDeploymentServiceDto[0]);
    }


    /**
     * Calling Platform Health Monitor and building the alert information about every filesystem
     *
     * @param filesystem The filesystem to check
     */
    private RequestAlertsDTO buildAlertInfoInformation(Filesystem filesystem)
    {
        LOG.trace("[FilesystemsAPI] -> [buildAlertInfoInformation]: Building alert Info Information for filesystemId: [{}]", filesystem.getId());

        // New objects instantiation
        final RequestAlertsDTO requestAlertsDTOToReturn = new RequestAlertsDTO();

        FilesystemAlert filesystemAlert = this.filesystemAlertRepositoryManager.findFilesystemAlertFromFilesystemId(filesystem.getId()).orElseThrow(() ->
        {
            String message = MessageFormat.format("[FilesystemBuilderImpl] -> [buildAlertInfoInformation]: the filesystem alert does not exists for filesystem id: {0}.", filesystem.getId());
            throw new NovaException(FilesystemsError.getNotFSAlertError(filesystem.getId()), message);
        });

        //Calling the alert service to obtain the alert's information for a fs -> At the moment, only OPEN alerts needed
        ASRequestAlertsDTO asRequestAlertsDTO = this.alertServiceClient.getAlertsByRelatedIdAndStatus(new String[]{filesystemAlert.getId().toString()}, filesystem.getProduct().getId(),
                filesystem.getProduct().getUuaa(), Constants.OPEN_ALERT_STATUS);

        BasicAlertInfoDTO basicAlertInfoDTO = new BasicAlertInfoDTO();
        List<BasicAlertInfoDTO> basicAlertInfoDTOS = new ArrayList<>();
        //Copying first level information to new DTO
        BeanUtils.copyProperties(asRequestAlertsDTO, requestAlertsDTOToReturn);

        if (asRequestAlertsDTO.getBasicAlertInfo() != null)
        {
            for (ASBasicAlertInfoDTO asBasicAlertInfoDTO : asRequestAlertsDTO.getBasicAlertInfo())
            {
                //Clean old object variables set to overwrite
                basicAlertInfoDTO.clear();

                //Copying second level information to DTO
                BeanUtils.copyProperties(asBasicAlertInfoDTO, basicAlertInfoDTO);
                basicAlertInfoDTOS.add(basicAlertInfoDTO);
            }
        }

        requestAlertsDTOToReturn.setBasicAlertInfo(basicAlertInfoDTOS.toArray(BasicAlertInfoDTO[]::new));

        LOG.trace("[FilesystemsAPI] -> [buildAlertInfoInformation]: Built alert Info Information for filesystemId [{}], result: [{}]",
                filesystem.getId(), requestAlertsDTOToReturn);

        return requestAlertsDTOToReturn;
    }

    private static FilesystemConfigurationDto buildConfigurationDTO(final String key, final String value)
    {
        final FilesystemConfigurationDto configuration = new FilesystemConfigurationDto();
        configuration.setKey(key);
        configuration.setValue(value);
        return configuration;
    }
}
