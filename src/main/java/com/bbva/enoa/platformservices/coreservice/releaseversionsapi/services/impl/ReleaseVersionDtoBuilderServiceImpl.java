package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.issuetrackerapi.model.IssueTrackerItem;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsedLibrariesDTO;
import com.bbva.enoa.apirestgen.mailserviceapi.model.MailInstance;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.common.entities.LobFile;
import com.bbva.enoa.datamodel.model.common.enumerates.AsyncStatus;
import com.bbva.enoa.datamodel.model.config.enumerates.ManagementType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.brokersapi.services.interfaces.IBrokerPropertiesConfiguration;
import com.bbva.enoa.platformservices.coreservice.common.model.param.ServiceOperationParams;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PropertyDefinitionRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ConfigurationmanagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.MailServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IContinuousintegrationClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.interfaces.IQualityManagerService;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util.QualityConstants;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IBatchScheduleService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IIssueTrackerService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionDtoBuilderService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IReleaseVersionValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ProjectFileReader;
import com.bbva.enoa.utils.clientsutils.services.interfaces.IErrorTaskManager;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xe30000 on 21/02/2017.
 */
@RefreshScope
@Service
@Slf4j
public class ReleaseVersionDtoBuilderServiceImpl implements IReleaseVersionDtoBuilderService
{

    private static final String SUBSYSTEM_BUILDSTATUS = "subsystemBuildStatus";

    /**
     * NOVA email
     */
    @Value("${nova.alert.email:enoa.cib@bbva.com}")
    private String email;

    @Value("${nova.issuetracker.enabled:false}")
    private boolean isIssueTrackerEnabled;

    /**
     * User
     */
    @Value("${nova.alert.user:IMM0589}")
    private String user;

    /**
     * Mail service client
     */
    @Autowired
    private MailServiceClient mailServiceClient;

    /**
     * Release version repository
     */
    @Autowired
    private ReleaseVersionRepository releaseVersionRepository;

    /**
     * Continuous integration client
     */
    @Autowired
    private IContinuousintegrationClient iContinuousintegrationClient;

    /**
     * Quality information service
     */
    @Autowired
    private IQualityManagerService qualityManagerService;

    /**
     * Property definition repository
     */
    @Autowired
    private PropertyDefinitionRepository propertyDefinitionRepository;

    @Autowired
    private ConfigurationmanagerClient configurationmanagerClient;

    /**
     * Issue tracker service
     */
    @Autowired
    private IIssueTrackerService iIssueTrackerService;

    /**
     * Service for creating task error
     */
    @Autowired
    private IErrorTaskManager errorTaskManager;

    /**
     * To do task service client
     */
    @Autowired
    private TodoTaskServiceClient todoTaskServiceClient;

    /**
     * Tools service client
     */
    @Autowired
    private IToolsClient toolsClient;

    /**
     * Release version subsystem repository
     */
    @Autowired
    private ReleaseVersionSubsystemRepository releaseVersionSubsystemRepository;

    /**
     * Release version validator
     */
    @Autowired
    private IReleaseVersionValidator releaseVersionValidator;

    /**
     * version control client
     */
    @Autowired
    private IVersioncontrolsystemClient versioncontrolsystemClient;

    /**
     * Mail
     */
    @Autowired
    private MailServiceClient mailService;

    /**
     * Scheduler Manager implementation
     */
    @Autowired
    private IBatchScheduleService batchScheduleService;

    @Autowired
    private ILibraryManagerService libraryManagerService;

    @Autowired
    private IBrokerPropertiesConfiguration brokerPropertiesConfiguration;

    @Override
    public void subsystemBuildStatus(final Integer releaseVersionSubsystemId, final String jenkinsJobGroupMessageInfo, final String jenkinsJobGroupStatus, final String ivUser)
    {
        ReleaseVersionSubsystem releaseVersionSubsystem = this.releaseVersionSubsystemRepository.findById(releaseVersionSubsystemId).orElseThrow(() ->
        {
            throw new NovaException(ReleaseVersionError.getNoSuchReleaseVersionSubsystemError(releaseVersionSubsystemId));
        });

        // Set the status and status message to the (NOVA or EPHOENIX) Release Version Subsystem
        AsyncStatus newStatus = this.getReleaseVersionSubsystemStatus(jenkinsJobGroupStatus);
        releaseVersionSubsystem.setStatus(newStatus);
        releaseVersionSubsystem.setStatusMessage(jenkinsJobGroupMessageInfo);

        // Save and flush the changes into release Version Subsystem
        ReleaseVersionSubsystem savedReleaseVersionSubsystem = this.releaseVersionValidator.saveAndFlushReleaseVersionSubsystem(releaseVersionSubsystem);

        // Check subsystems
        this.checkSubsystems(ivUser, savedReleaseVersionSubsystem, jenkinsJobGroupMessageInfo);
    }

    @Override
    public void buildSubsystems(final Product product, final ReleaseVersion releaseVersion, final String ivUser)
    {
        // Check and add Batch scheduler services (this services does not have to be compiled in Jenkins
        this.batchScheduleService.addBatchSchedulServices(releaseVersion);

        // Build the subsystem of the product version
        this.iContinuousintegrationClient.buildSubsystems(product, releaseVersion, ivUser);
    }

    @Override
    public void processTemplates(final ReleaseVersion releaseVersion, final String ivUser)
    {
        log.debug("[{}] -> [{}]: process templates for release version [{}] - [{}] ", Constants.RELEASE_VERSIONS_API_NAME, "processTemplates", releaseVersion.getRelease().getName(), releaseVersion.getVersionName());
        for (ReleaseVersionSubsystem releaseVersionSubsystem : releaseVersion.getSubsystems())
        {
            // Process for each service the configuration
            for (ReleaseVersionService releaseVersionService : releaseVersionSubsystem.getServices())
            {
                if (ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(releaseVersionService.getServiceType()))
                {
                    log.trace("[{}] -> [{}]: ignored Batch scheduler service. [{}]. It does not have properties. ", Constants.RELEASE_VERSIONS_API_NAME, "processTemplates",
                            releaseVersionService.getServiceName());
                }
                else
                {
                    ValidatorInputs validatorInputs = scanNovaYml(releaseVersionService);

                    // Set amd save libraries used by given service in case of exists
                    List<LMUsedLibrariesDTO> librariesList = this.setAndSaveUsedLibraries(validatorInputs, releaseVersionService.getId());

                    // Set and save the Spring cloud Spring properties related to brokers
                    Map<String, Map<String, String>> extraPropertiesMap = this.getReleaseVersionBrokerPropertiesMap(validatorInputs, releaseVersionService);

                    // Set and save the property definition list
                    this.setAndSavePropertyDefinition(releaseVersionSubsystem, releaseVersionService, ServiceType.valueOf(releaseVersionService.getServiceType()), librariesList, extraPropertiesMap, ivUser);
                }
            }
        }
    }


    //////////////////////////////////////////   PRIVATE METHODS   \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * @param validatorInputs nova yaml scanned
     * @return Map with SCS properties associated to broker
     */
    private Map<String, Map<String, String>> getReleaseVersionBrokerPropertiesMap(final ValidatorInputs validatorInputs, final ReleaseVersionService releaseVersionService)
    {
        log.debug("[ReleaseVersionDtoBuilderServiceImpl] -> [getReleaseVersionBrokerPropertiesMap]: Getting brokerPropertyMap input parameters:  asyncApiBackToBack: [{}], releaseVersionService: [{}].", validatorInputs.getNovaYml().getAsyncapisBackToBack(), releaseVersionService.getServiceName());
        Map<String, String> brokerPropertiesMap = new HashMap<>();
        // check if has back to back apis
        if (validatorInputs.getNovaYml() != null
                && validatorInputs.getNovaYml().getAsyncapisBackToBack() != null
                && !validatorInputs.getNovaYml().getAsyncapisBackToBack().getAsyncApis().isEmpty()
        )
        {
            // Accumulate all properties in one single map
            brokerPropertiesMap = this.brokerPropertiesConfiguration.getBrokerSCSPropertiesDependentOnReleaseVersionService(releaseVersionService);
        }
        log.debug("[ReleaseVersionDtoBuilderServiceImpl] -> [getReleaseVersionBrokerPropertiesMap]: BrokerPropertyMap for releaseVersionService: [{}]. Result is: [{}]", releaseVersionService.getServiceName(), brokerPropertiesMap);
        Map<String, Map<String, String>> propertiesMapByType = new HashMap<>();
        propertiesMapByType.put(ManagementType.BROKER.name(), brokerPropertiesMap);

        return propertiesMapByType;
    }


    private ValidatorInputs scanNovaYml(final ReleaseVersionService releaseVersionService)
    {
        final LobFile novayml = releaseVersionService.getNovaYml();
        final ValidatorInputs inputs = new ValidatorInputs();
        if (ServiceType.valueOf(releaseVersionService.getServiceType()).isNovaYml() && novayml != null && novayml.getContents() != null)
        {
            ProjectFileReader.scanNovaYml(releaseVersionService.getFolder(), novayml.getContents().getBytes(), inputs);
        }
        return inputs;
    }

    /**
     * Check if all subsystem is already finished
     *
     * @param ivUser                       the user requester to create the release version
     * @param savedReleaseVersionSubsystem saved release version subsystem
     * @param jenkinsJobGroupMessageInfo   jenkins job group message info*
     */
    private void checkSubsystems(final String ivUser, final ReleaseVersionSubsystem savedReleaseVersionSubsystem, final String jenkinsJobGroupMessageInfo)
    {
        log.debug("[{}] -> [{}]: searching release version of subsystemId [{}] ...", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, SUBSYSTEM_BUILDSTATUS, savedReleaseVersionSubsystem.getId());
        ReleaseVersion releaseVersion = this.releaseVersionRepository.releaseVersionOfSubsystem(savedReleaseVersionSubsystem.getId());
        log.debug("[{}] -> [{}]: release Version of subsystemId [{}] found: [{}]", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, SUBSYSTEM_BUILDSTATUS, savedReleaseVersionSubsystem.getId(), releaseVersion.getVersionName());

        // Check the release version
        this.releaseVersionValidator.checkReleaseVersionExistance(releaseVersion);

        if (AsyncStatus.ERROR == savedReleaseVersionSubsystem.getStatus() && releaseVersion.getStatus() != ReleaseVersionStatus.ERRORS)
        {
            TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(savedReleaseVersionSubsystem.getSubsystemId());

            releaseVersion.setStatus(ReleaseVersionStatus.ERRORS);
            releaseVersion.setStatusDescription("Error generando el subsistema " + subsystemDTO.getSubsystemName()
                    + " con Tag: " + savedReleaseVersionSubsystem.getTagName() + ". Fallos al compilar el job de jenkins. Detalles: " + jenkinsJobGroupMessageInfo);

            // Save the release version
            this.releaseVersionValidator.saveAndFlushReleaseVersion(releaseVersion);

            this.updateIfAllFinishedWithError(releaseVersion);

            log.debug("[{}] -> [{}]: Saved release version. The subsystemId [{}] is on status ERROR. (Compiled to ERROR). Set Release version name : [{}] to error. Release version status: [{}]",
                    Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "setReleaseVersionStatusAndDescription", savedReleaseVersionSubsystem.getId(), releaseVersion.getVersionName(), releaseVersion.getStatusDescription());

        }
        else if (AsyncStatus.DONE == savedReleaseVersionSubsystem.getStatus() && releaseVersion.getStatus() == ReleaseVersionStatus.BUILDING)
        {
            this.updateIfAllDone(releaseVersion, ivUser);
        }
        else
        {
            this.updateIfAllFinishedWithError(releaseVersion);

            log.debug("[{}] -> [{}]: Release Version name :[{}] - status: [{}] OR Subsystem id: [{}] status [{}] is on ERROR state. Ignore this last update and continue. The release version will be set to COMPILING ERROR.",
                    Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "checkSubsystems", releaseVersion.getVersionName(), releaseVersion.getStatus(), savedReleaseVersionSubsystem.getId(), savedReleaseVersionSubsystem.getStatus());

        }
    }

    /**
     * Update status if all subsystems are done
     *
     * @param releaseVersion release version
     * @param ivUser         user requester
     */
    private void updateIfAllDone(final ReleaseVersion releaseVersion, final String ivUser)
    {
        log.debug("[{}] -> [{}]: checking all subsystem for this release version id: [{}] release version name: [{}]", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "updateIfAllDone", releaseVersion.getId(), releaseVersion.getVersionName());

        boolean allDone = this.isAllDone(releaseVersion);

        if (!allDone)
        {
            log.debug("[{}] -> [{}]: Not updated. There are subsystem for finishing on this release version id: [{}] release version name: [{}]. Waiting to finish the all subsystem. Release version status: [{}] Continue...",
                    Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "updateIfAllDone", releaseVersion.getId(), releaseVersion.getVersionName(), releaseVersion.getStatus());
            return;
        }

        // All subsystems finished ok
        log.debug("[{}] -> [{}]: all subsystem for this release version id: [{}] release version name: [{}] has finished. Checking SQA status and creating Issue Tracker...", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "updateIfAllDone", releaseVersion.getId(), releaseVersion.getVersionName());

        // Check quality state for the release version
        String qualityState = qualityManagerService.checkReleaseVersionQualityState(releaseVersion, true);

        if (QualityConstants.SQA_NOT_AVAILABLE.equals(qualityState))
        {
            log.error("[{}] -> [{}]: [ReleaseVersionAPI] -> [updateIfAllDone]: Error occurred while getting quality for release version id: [{}], release version name: [{}]", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "updateIfAllDone", releaseVersion.getId(), releaseVersion.getVersionName());

            releaseVersion.setStatus(ReleaseVersionStatus.ERRORS);
            releaseVersion.setStatusDescription("Se ha producido un error al recopilar la calidad de los servicios. Por favor, vuelva a intentarlo más tarde o contacte con el equipo NOVA.");
        }
        else if (QualityConstants.SQA_ERROR_PROCESSING_MODULE.equals(qualityState))
        {
            log.error("[{}] -> [{}]: Error obtained processing quality for release version id: [{}], release version name: [{}]", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "updateIfAllDone",
                    releaseVersion.getId(), releaseVersion.getVersionName());

            releaseVersion.setStatus(ReleaseVersionStatus.ERRORS);
            releaseVersion.setStatusDescription("Se ha producido un error procesando la calidad de los servicios. Por favor, revise los informes generados o contacte con el equipo NOVA.");
        }
        else
        {
            releaseVersion.setStatus(ReleaseVersionStatus.READY_TO_DEPLOY);
            releaseVersion.setStatusDescription("Los subsistemas han sido compilados satisfactoriamente. Release version lista para desplegar.");

            releaseVersion.setQualityValidation(QualityConstants.SQA_OK.equals(qualityState));
        }

        // Once the ReleaseVersion was built, we are going to create a DeploymentRequest JIRA, so call to IssueTracker.
        if (this.isIssueTrackerEnabled)
        {
            this.createDeploymentRequest(releaseVersion, ivUser);
        }
        else
        {
            log.debug("[{}] -> [{}]: [ReleaseVersionAPI] -> [updateIfAllDone]: Issue tracker is not enabled. Not create issue key for release: [{}]. Continue", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "updateIfAllDone", releaseVersion.getVersionName());
        }

        // Save the release version
        this.releaseVersionValidator.saveAndFlushReleaseVersion(releaseVersion);
        log.debug("[{}] -> [{}]: Saved the release version id: [{}] release version name: [{}] has been updated. All subsystem has finished. The release version status is: [{}]",
                Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "updateIfAllDone", releaseVersion.getId(), releaseVersion.getVersionName(), releaseVersion.getStatus());
    }

    /**
     * Update status if all subsystems are finished and, at least, one of them with error
     *
     * @param releaseVersion release version
     */
    private void updateIfAllFinishedWithError(final ReleaseVersion releaseVersion)
    {
        log.debug("[{}] -> [{}]: checking all subsystem for this release version id: [{}] release version name: [{}]", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "updateIfAllDoneWithError", releaseVersion.getId(), releaseVersion.getVersionName());

        boolean allFinished = releaseVersion.getSubsystems().stream()
                .allMatch(subsystem -> subsystem.getStatus() == AsyncStatus.DONE || subsystem.getStatus() == AsyncStatus.ERROR);

        if (!allFinished)
        {
            log.debug("[{}] -> [{}]: Not updated. There are subsystem for finishing on this release version id: [{}] release version name: [{}]. Waiting to finish the all subsystem. Release version status: [{}] Continue...",
                    Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "updateIfAllDoneWithError", releaseVersion.getId(), releaseVersion.getVersionName(), releaseVersion.getStatus());
            return;
        }

        // All subsystems finished ok
        log.debug("[{}] -> [{}]: all subsystem for this release version id: [{}] release version name: [{}] has finished. Checking SQA status ...", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "updateIfAllDoneWithError", releaseVersion.getId(), releaseVersion.getVersionName());

        // Check quality state for the release version
        qualityManagerService.checkReleaseVersionQualityState(releaseVersion, false);
    }

    /**
     * Create deployment request
     *
     * @param releaseVersion release version
     * @param ivUser         user code
     */
    private void createDeploymentRequest(ReleaseVersion releaseVersion, String ivUser)
    {
        Product product = releaseVersion.getRelease().getProduct();

        if (!product.getCreateJiraIssues())
        {
            log.info("[{}] -> [createDeploymentRequest]: UUAA: [{}] has been excluded. This UUAA will not generate or work with JIRA.", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, product.getUuaa());
        }
        else
        {
            IssueTrackerItem[] issueTrackerItems = this.iIssueTrackerService.createDeploymentRequest(releaseVersion, ivUser);

            if (issueTrackerItems == null || issueTrackerItems.length == 0)
            {
                String mensajeGenerico = this.getGenericMessage(releaseVersion.getVersionName(), product.getName());
                String mensajeDevOps = this.getDevOpsMessage(product.getName());

                // Issue tracker had problems to create the issue key
                log.error("[{}] -> [createDeploymentRequest]: Issue tracker had problems to create the issue key for product [{}] and uuaa [{}]",
                        Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, product.getName(), product.getUuaa());

                // Send an email to Product for reporting issue
                if ("ERROR".equalsIgnoreCase(product.getDesBoard()))
                {
                    log.debug("[{}] -> [createDeploymentRequest]: Sending mail [{}]", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, mensajeDevOps);
                    MailInstance mailInstance = new MailInstance();
                    mailInstance.setTo(new String[]{product.getEmail(), this.email});
                    String subject = "Identificador del proyecto en JIRA es: ERROR";
                    mailInstance.setSubject(subject);
                    mailInstance.setBody(mensajeGenerico + mensajeDevOps);
                    this.mailServiceClient.sendAdminMail(mailInstance);
                    log.debug("[{}] -> [createDeploymentRequest]: Mail sent", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE);
                }
            }
            else
            {
                // Set the Issue key to the release version
                releaseVersion.setIssueID(issueTrackerItems[0].getProjectKey());

                log.debug("[{}] -> [createDeploymentRequest]: [ReleaseVersionAPI] -> [createDeploymentRequest]: Created issue key: [{}] for release version: [{}]",
                        Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, issueTrackerItems[0].getProjectKey(),
                        releaseVersion.getVersionName());
            }
        }
    }

    /**
     * Is all done
     * >
     *
     * @param releaseVersion release version
     * @return all done
     */
    private boolean isAllDone(ReleaseVersion releaseVersion)
    {
        boolean allDone = true;
        for (ReleaseVersionSubsystem subsystemI : releaseVersion.getSubsystems())
        {
            log.debug("[{}] -> [{}]: checking status fo subsystem id: [{}] subsystem compilation job name: [{}]", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "isAllDone", subsystemI.getId(), subsystemI.getCompilationJobName());
            if (subsystemI.getStatus() != AsyncStatus.DONE)
            {
                allDone = false;
                log.debug("[{}] -> [{}]: the subsystem id: [{}] subsystem compilation job name: [{}], status: [{}] has been to FALSE", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "isAllDone", subsystemI.getId(),
                        subsystemI.getCompilationJobName(), subsystemI.getStatus());
                break;
            }

            log.debug("[{}] -> [{}]: Checked status of the subsystem id: [{}] subsystem compilation job name: [{}] status: [{}]", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "isAllDone", subsystemI.getId(),
                    subsystemI.getCompilationJobName(), subsystemI.getStatus());
        }

        return allDone;
    }

    /**
     * Set release version status and description
     *
     * @param jenkinsJobGroupMessageInfo Jenkins group info
     * @param releaseVersionSubsystem    release version subsystem
     * @param subsystemStatus            susbsystem status
     * @param releaseVersion             release version
     */
    private void setReleaseVersionStatusAndDescription(final String jenkinsJobGroupMessageInfo,
                                                       final ReleaseVersionSubsystem releaseVersionSubsystem,
                                                       final AsyncStatus subsystemStatus, final ReleaseVersion releaseVersion)
    {
        // If the releaseVersionSubsystem was some kind of error in the compilation, set the error status and description
        if (subsystemStatus == AsyncStatus.ERROR && releaseVersion.getStatus() != ReleaseVersionStatus.ERRORS)
        {
            TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(releaseVersionSubsystem.getSubsystemId());
            releaseVersion.setStatus(ReleaseVersionStatus.ERRORS);
            releaseVersion.setStatusDescription("Error generando el subsistema " + subsystemDTO.getSubsystemName()
                    + " con Tag: " + releaseVersionSubsystem.getTagName() + ". Fallos al compilar el job de jenkins. Detalles: " + jenkinsJobGroupMessageInfo);
        }
    }

    /**
     * Transform the jenkins job group status (this status can be SUCCESS or FAILDE) to AsynStatus
     *
     * @param jenkinsJobGroupStatus - jenkins job group status
     */
    private AsyncStatus getReleaseVersionSubsystemStatus(final String jenkinsJobGroupStatus)
    {
        if (Constants.SUCCESS.equalsIgnoreCase(jenkinsJobGroupStatus))
        {
            return AsyncStatus.DONE;
        }
        else
        {
            return AsyncStatus.ERROR;
        }
    }


    /**
     * @param inputs           validator inputs, novayml scanned
     * @param releaseVersionId rv id
     * @return used libraries list
     */
    private List<LMUsedLibrariesDTO> setAndSaveUsedLibraries(final ValidatorInputs inputs, final Integer releaseVersionId)
    {
        List<LMUsedLibrariesDTO> lmUsedLibrariesDTOList = new ArrayList<>();

        if (inputs.getNovaYml() != null && !inputs.getNovaYml().getLibraries().isEmpty())
        {
            return this.libraryManagerService.saveUsedLibraries(releaseVersionId, inputs.getNovaYml().getLibraries().toArray(new String[0]));
        }

        return lmUsedLibrariesDTOList;
    }

    /**
     * Set the definition property list of the release version service depending on the service type
     *
     * @param releaseVersionSubsystem release version subsystem
     * @param releaseVersionService   release version service to set the properties
     * @param serviceType             the service type
     * @param extraProperties
     * @param ivUser                  user requester
     */
    private void setAndSavePropertyDefinition(final ReleaseVersionSubsystem releaseVersionSubsystem, final ReleaseVersionService releaseVersionService, final ServiceType serviceType,
                                              final List<LMUsedLibrariesDTO> librariesList, final Map<String, Map<String, String>> extraProperties, final String ivUser)
    {
        TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(releaseVersionSubsystem.getSubsystemId());

        String serviceName = releaseVersionService.getServiceName();
        log.debug("[{}] -> [{}]: setting the properties definition of the release version service: [{}]" +
                        " of the subsystem: [{}]. Service type: [{}] for UserCode requester: [{}]", Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE,
                "setAndSavePropertyDefinition", serviceName, subsystemDTO.getSubsystemName(),
                serviceType, ivUser);

        ServiceOperationParams.ServiceOperationParamsBuilder paramsBuilder = (new ServiceOperationParams.ServiceOperationParamsBuilder())
                .tag(releaseVersionSubsystem.getTagName()).versionControlServiceId(subsystemDTO.getRepoId())
                .ivUser(ivUser).serviceType(serviceType.name())
                .releaseVersionServiceId(releaseVersionService.getId())
                .releaseVersionServiceName(serviceName)
                .libraries(librariesList).extraProperties(extraProperties);

        String modulePath = releaseVersionService.getFolder();
        switch (serviceType)
        {
            case NOVA:
            case NOVA_BATCH:
            case NOVA_SPRING_BATCH:
            case API_REST_JAVA_SPRING_BOOT:
            case API_JAVA_SPRING_BOOT:
            case BATCH_JAVA_SPRING_CLOUD_TASK:
            case API_REST_NODE_JS_EXPRESS:
            case BATCH_JAVA_SPRING_BATCH:
            case DAEMON_JAVA_SPRING_BOOT:
            case FRONTCAT_JAVA_SPRING_MVC:
            case FRONTCAT_JAVA_J2EE:
            case THIN2:
            case CDN_ANGULAR_THIN2:
            case CDN_ANGULAR_THIN3:
            case API_REST_PYTHON_FLASK:
            case BATCH_PYTHON:
            case LIBRARY_JAVA:
            case LIBRARY_NODE:
            case LIBRARY_THIN2:
            case LIBRARY_PYTHON:
            case LIBRARY_TEMPLATE:
                paramsBuilder.modulePath(modulePath);
                this.configurationmanagerClient.setPropertiesDefinitionService(paramsBuilder.build());
                break;

            case EPHOENIX_BATCH:
            case EPHOENIX_ONLINE:
            case NODE:
                paramsBuilder.modulePath(modulePath + Constants.EPHOENIX_OR_NODE_TEMPLATE_YML_PATH);
                this.configurationmanagerClient.setPropertiesDefinitionService(paramsBuilder.build());
                break;
            default:
                log.debug("[{}] -> [{}]: the release version service: [{}] of the subsystem: [{}] is" +
                                " of Service Type: [{}] does not have properties definition - does not apply. UserCode: [{}]",
                        Constants.RELEASE_VERSION_DTO_BUILDER_SERVICE, "setAndSavePropertyDefinition", serviceName,
                        subsystemDTO.getSubsystemName(), serviceType, ivUser);
                break;
        }
    }

    private String getGenericMessage(String releaseVersionName, String productName)
    {
        return "El servicio IssueTracker ha registrado un error, por lo que no ha registrado la Release Version [" +
                releaseVersionName +
                "] en JIRA" +
                " del producto: [" + productName + "]" + "\n";
    }

    private String getDevOpsMessage(String productName)
    {

        StringBuilder mensajeDevOps = new StringBuilder();
        mensajeDevOps.append("El producto [").append(productName).append("], en el campo 'Identificador del proyecto en JIRA' tiene ERROR, ")
                .append(" Es necesario especificar en la descripción del JIRA que es para trabajar con Arquitectura NOVA.").append("\n").append("\n")
                .append(" Este problema puede haber ocurrido porque el producto se creó en la plataforma NOVA antes de que la UUAA asignada al producto existiese en Fresno CIB. ").append("\n").append("\n")
                .append(" Por favor, póngase en contacto con el equipo NOVA via Jira - PNOVA, e indicar 'Solicitud de creación/actualización' de identificador Jira para su producto. ").append("\n").append("\n")
                .append(" El procedimiento que seguirá el equipo NOVA será: ").append("\n")
                .append(" <b> 1- Creará la petición en Jira en su nombre, en esta URL: </b> ").append("\n")
                .append(" https://cibproducts.grupobbva.com/JIRA/secure/CreateIssue.jspa?pid=10305&issuetype=71&nbsp ").append("\n")
                .append(" <b> 2- Una vez resuelto el Jira por el Grupo de DevOps, se proporcionará un nuevo 'Identificador del proyecto en JIRA' para su producto. </b>").append("\n")
                .append(" <b> 3- Posteriormente, de forma manual, el equipo NOVA se lo asignarán el nuevo identificador del proyecto al Producto en el portal NOVA. </b> ").append("\n")
                .append(" <b> 4- Se lo comunicará via Jira para finalizar el procedimiento y ya podrá operar en la plataforma NOVA. </b> ");
        return mensajeDevOps.toString();
    }

}
