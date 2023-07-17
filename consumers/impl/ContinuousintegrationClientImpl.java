package com.bbva.enoa.platformservices.coreservice.consumers.impl;


import com.bbva.enoa.apirestgen.continuousintegrationapi.client.feign.nova.rest.IRestHandlerContinuousintegrationapi;
import com.bbva.enoa.apirestgen.continuousintegrationapi.client.feign.nova.rest.IRestListenerContinuousintegrationapi;
import com.bbva.enoa.apirestgen.continuousintegrationapi.client.feign.nova.rest.impl.RestHandlerContinuousintegrationapi;
import com.bbva.enoa.apirestgen.continuousintegrationapi.model.*;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorService;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorSubsystem;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.behavior.enumerates.BehaviorVersionStatus;
import com.bbva.enoa.datamodel.model.common.enumerates.AsyncStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.util.QualityUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IContinuousintegrationClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.impl.QualityManagerService;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util.QualityConstants;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.GraphNode;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.IDependencyResolutionService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ProjectFileReader;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.exceptions.StatisticsError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Frontend for the Continuous Integration API client.
 * Created by xe52580 on 15/02/2017.
 */
@Slf4j
@Service
public class ContinuousintegrationClientImpl implements IContinuousintegrationClient
{

    private static final Logger LOG = LoggerFactory
            .getLogger(ContinuousintegrationClientImpl.class);
    private static final String BUILD_SUBSYSTEMS = "buildSubsystems";
    private static final String BUILD_EPHOENIX_SUBSYTEM_JOB = "buildEphoenixSubsystemJob";
    private static final String CREATE_JOB_PARAMETERS_FOR_EPHOENIX_JOB = "createJobParametersForEphoenixJob";

    private static final int JOB_ERROR_COMPILATION_ID = -1;
    private static final String SONAR_BRANCH_TEMPLATE = "version-%SERVICE_VERSION%";
    private static final String EPHOENIX_SONAR_BRANCH_TEMPLATE = "version-%MODULE_VERSION%";
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");


    /**
     * IRest Handler Continuous integration api
     */
    @Autowired
    private IRestHandlerContinuousintegrationapi restInterface;

    /**
     * Version control system client
     */
    @Autowired
    private IVersioncontrolsystemClient vcsClient;

    @Autowired
    private IDependencyResolutionService depResolvService;
    /**
     * To do task service
     */
    @Autowired
    private TodoTaskServiceClient todoTaskServiceClient;

    @Autowired
    private QualityManagerService qualityManagerService;

    /**
     * Tools service
     */
    @Autowired
    private IToolsClient toolsClient;

    /**
     * API services.
     */
    private RestHandlerContinuousintegrationapi restHandler;

    @Override
    @Transactional
    public boolean buildSubsystems(final Product product, final ReleaseVersion releaseVersion, final String ivUser)
    {
        // Set the status of the release releaseVersion
        releaseVersion.setStatus(ReleaseVersionStatus.BUILDING);

        int alreadyCompiledSubsystems = 0;
        int totalSubsystems = releaseVersion.getSubsystems().size();

        try
        {
            // For each subsystem:
            for (ReleaseVersionSubsystem releaseVersionSubsystem : releaseVersion.getSubsystems())
            {
                TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(releaseVersionSubsystem.getSubsystemId());

                if (SubsystemType.NOVA.getType().equals(subsystemDTO.getSubsystemType())
                        || SubsystemType.FRONTCAT.getType().equals(subsystemDTO.getSubsystemType())
                        || SubsystemType.LIBRARY.getType().equals(subsystemDTO.getSubsystemType())
                        || SubsystemType.BEHAVIOR_TEST.getType().equals(subsystemDTO.getSubsystemType()))
                {
                    alreadyCompiledSubsystems = this.compileSubsystemNova(product, releaseVersion, ivUser, alreadyCompiledSubsystems, releaseVersionSubsystem);
                }
                else if (SubsystemType.EPHOENIX.getType().equals(subsystemDTO.getSubsystemType()))
                {
                    alreadyCompiledSubsystems = this.compileSubsystemPhoenix(product, releaseVersion, ivUser, alreadyCompiledSubsystems, releaseVersionSubsystem);
                }
            }
        }
        catch (NovaException exception)
        {
            releaseVersion.setStatus(ReleaseVersionStatus.ERRORS);
            releaseVersion.setDescription(exception.getNovaError().toString());
            log.error("[ContinuousIntegrationClientImpl] -> [buildSubsystems]: there was some error trying to build a subsystem for product id: [{}] and release version id: [{}]. Error message: [{}]", product.getId(), releaseVersion.getId(), exception.getNovaError());
            throw exception;
        }

        if (totalSubsystems == alreadyCompiledSubsystems)
        {
            // Set the status of the release releaseVersion
            releaseVersion.setStatus(ReleaseVersionStatus.READY_TO_DEPLOY);

            log.debug("[{}] -> [{}]: No new image will be generated for the release : [{}]. Since the version has not been updated. Release version name: [{}]",
                    com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT, BUILD_SUBSYSTEMS, ivUser, releaseVersion.getRelease().getName());

            // No subsystems were built this time, there won't be any callback from Continuous Integration. QA results must be computed.
            updateReleaseVersionQualityState(releaseVersion);
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public boolean buildBehaviorSubsystems(final Product product, final BehaviorVersion behaviorVersion, final String ivUser)
    {
        // Set the status of the release releaseVersion
        behaviorVersion.setStatus(BehaviorVersionStatus.BUILDING);

        int alreadyCompiledSubsystems = 0;
        int totalSubsystems = behaviorVersion.getSubsystems().size();

        try
        {
            for (BehaviorSubsystem behaviorSubsystem : behaviorVersion.getSubsystems())
            {
                alreadyCompiledSubsystems = this.compileBehaviorSubsystem(product, behaviorVersion, ivUser, alreadyCompiledSubsystems, behaviorSubsystem);
            }
        }
        catch (NovaException exception)
        {
            behaviorVersion.setStatus(BehaviorVersionStatus.ERRORS);
            behaviorVersion.setDescription(exception.getNovaError().toString());
            log.error("[ContinuousIntegrationClientImpl] -> [buildBehaviorSubsystems]: there was some error trying to build a subsystem for product id: [{}] and behavior version id: [{}]. Error message: [{}]", product.getId(), behaviorVersion.getId(), exception.getNovaError());
            throw exception;
        }

        if (totalSubsystems == alreadyCompiledSubsystems)
        {
            // Set the status of the release releaseVersion
            behaviorVersion.setStatus(BehaviorVersionStatus.READY_TO_DEPLOY);

            log.debug("[{}] -> [{}]: No new image will be generated for the version : [{}]. Since the version has not been updated. Behavior version name: [{}]",
                    this.getClass().getSimpleName(), "buildBehaviorSubsystems", ivUser, behaviorVersion.getVersionName());

            // No subsystems were built this time, there won't be any callback from Continuous Integration. QA results must be computed.
            updateBehaviorVersionQualityState(behaviorVersion);
            return false;
        }

        return true;
    }

    private void updateReleaseVersionQualityState(final ReleaseVersion releaseVersion)
    {
        log.debug("[{}] -> [updateReleaseVersionQualityState]: All subsystems already compiled. Updating RV Quality state.", this.getClass().getSimpleName());

        // Check quality state for the release version
        String qualityState = qualityManagerService.checkReleaseVersionQualityState(releaseVersion, true);

        if (QualityConstants.SQA_NOT_AVAILABLE.equals(qualityState))
        {
            log.error("[{}] -> [updateReleaseVersionQualityState]: Error occurred while getting quality for release version id: [{}], release version name: [{}]", this.getClass().getSimpleName(), releaseVersion.getId(), releaseVersion.getVersionName());

            releaseVersion.setStatus(ReleaseVersionStatus.ERRORS);
            releaseVersion.setStatusDescription("Se ha producido un error al recopilar la calidad de los servicios. Por favor, vuelva a intentarlo más tarde o contacte con el equipo NOVA.");
        }
        else if (QualityConstants.SQA_ERROR_PROCESSING_MODULE.equals(qualityState))
        {
            log.error("[{}] -> [updateReleaseVersionQualityState]:  Error obtained processing quality for release version id: [{}], release version name: [{}]", this.getClass().getSimpleName(), releaseVersion.getId(), releaseVersion.getVersionName());

            releaseVersion.setStatus(ReleaseVersionStatus.ERRORS);
            releaseVersion.setStatusDescription("Se ha producido un error procesando la calidad de los servicios. Por favor, revise los informes generados o contacte con el equipo NOVA.");

        }
        else
        {
            releaseVersion.setStatus(ReleaseVersionStatus.READY_TO_DEPLOY);
            releaseVersion.setStatusDescription("Los subsistemas han sido compilados satisfactoriamente. Release version lista para desplegar.");

            releaseVersion.setQualityValidation(QualityConstants.SQA_OK.equals(qualityState));
        }

        log.debug("[{}] -> [updateReleaseVersionQualityState]: updated release version id [{}], qualityValidation: [{}]", this.getClass().getSimpleName(), releaseVersion.getId(), releaseVersion.getQualityValidation());
    }

    private void updateBehaviorVersionQualityState(final BehaviorVersion behaviorVersion)
    {
        log.debug("[{}] -> [updateBehaviorVersionQualityState]: All subsystems already compiled. Updating BV Quality state.", this.getClass().getSimpleName());

        // Check quality state for the release version
        String qualityState = qualityManagerService.checkBehaviorVersionQualityState(behaviorVersion, true);

        if (QualityConstants.SQA_NOT_AVAILABLE.equals(qualityState))
        {
            log.error("[{}] -> [updateBehaviorVersionQualityState]: Error occurred while getting quality for behavior version id: [{}], behavior version name: [{}]", this.getClass().getSimpleName(), behaviorVersion.getId(), behaviorVersion.getVersionName());

            behaviorVersion.setStatus(BehaviorVersionStatus.ERRORS);
            behaviorVersion.setStatusDescription("Se ha producido un error al recopilar la calidad de los servicios. Por favor, vuelva a intentarlo más tarde o contacte con el equipo NOVA.");
        }
        else if (QualityConstants.SQA_ERROR_PROCESSING_MODULE.equals(qualityState))
        {
            log.error("[{}] -> [updateBehaviorVersionQualityState]:  Error obtained processing quality for behavior version id: [{}], behavior version name: [{}]", this.getClass().getSimpleName(), behaviorVersion.getId(), behaviorVersion.getVersionName());

            behaviorVersion.setStatus(BehaviorVersionStatus.ERRORS);
            behaviorVersion.setStatusDescription("Se ha producido un error procesando la calidad de los servicios. Por favor, revise los informes generados o contacte con el equipo NOVA.");

        }
        else
        {
            behaviorVersion.setStatus(BehaviorVersionStatus.READY_TO_DEPLOY);
            behaviorVersion.setStatusDescription("Los subsistemas han sido compilados satisfactoriamente. behavior version lista para ejecutar.");

            behaviorVersion.setQualityValidation(QualityConstants.SQA_OK.equals(qualityState));
        }

        log.debug("[{}] -> [updateBehaviorVersionQualityState]: updated behavior version id [{}], qualityValidation: [{}]", this.getClass().getSimpleName(), behaviorVersion.getId(), behaviorVersion.getQualityValidation());
    }

    @Override
    public CIJobDTO[] getJobsSinceDaysAgo(Integer daysAgo, String jobType, String uuaa)
    {
        SingleApiClientResponseWrapper<CIJobDTO[]> response = new SingleApiClientResponseWrapper<>();

        log.debug("[ContinuousintegrationClientImpl] -> [getJobsSinceDaysAgo]: getting Build Jobs since [{}] days ago, with Type [{}] and UUAA [{}]", daysAgo, jobType, uuaa);

        this.restHandler.getJobsSinceDaysAgo(new IRestListenerContinuousintegrationapi()
        {
            @Override
            public void getJobsSinceDaysAgo(CIJobDTO[] outcome)
            {
                log.debug("[ContinuousintegrationClientImpl] -> [getJobsSinceDaysAgo]: successfully got Build Jobs since [{}] days ago, with Type [{}] and UUAA [{}]", daysAgo, jobType, uuaa);
                response.set(outcome);
            }

            @Override
            public void getJobsSinceDaysAgoErrors(Errors outcome)
            {
                LOG.error("[ContinuousintegrationClientImpl] -> [getJobsSinceDaysAgo]: Error trying to get Build Jobs since [{}] days ago, with Type [{}] and UUAA [{}]: {}", daysAgo, jobType, uuaa, outcome.getBodyExceptionMessage());
                throw new NovaException(StatisticsError.getContinuousIntegrationError(), outcome);
            }

        }, daysAgo, jobType, uuaa);

        return response.get();
    }

    @Override
    public CIJenkinsBuildSnapshotDTO[] getBuildsSnapshots()
    {
        SingleApiClientResponseWrapper<CIJenkinsBuildSnapshotDTO[]> response = new SingleApiClientResponseWrapper<>();

        LOG.info("[ContinuousintegrationClientImpl] -> [getJenkinsBuildsHistorySnapshot]: getting builds snapshot for statistic history loading.");

        this.restHandler.getJenkinsBuildsHistorySnapshot(new IRestListenerContinuousintegrationapi()
        {
            @Override
            public void getJenkinsBuildsHistorySnapshot(CIJenkinsBuildSnapshotDTO[] outcome)
            {
                LOG.info("[ContinuousintegrationClientImpl] -> [getJenkinsBuildsHistorySnapshot]: successfully got builds snapshot for statistic history loading.");
                response.set(outcome);
            }

            @Override
            public void getJenkinsBuildsHistorySnapshotErrors(Errors outcome)
            {
                LOG.error("[ContinuousintegrationClientImpl] -> [getJenkinsBuildsHistorySnapshot]: Error getting builds snapshot for statistic history loading.: {}", outcome.getBodyExceptionMessage());
                throw new NovaException(StatisticsError.getContinuousIntegrationError(), outcome);
            }

        });

        return response.get();
    }

    /////////////////////////////////////////////////// PRIVATE METHODS ////////////////////////////////////////////////

    /**
     * Init the handler and listener.
     */

    @Override
    @PostConstruct
    public void init()
    {
        this.restHandler = new RestHandlerContinuousintegrationapi(this.restInterface);
    }

    /**
     * Add project to list
     *
     * @param projectList project list
     * @param service     service
     */
    private void addProject(List<CIServiceProjectDTO> projectList, ReleaseVersionService service)
    {
        //Check if image already exists in docker registry
        if (Boolean.TRUE.equals(service.getHasForceCompilation()))
        {
            CIServiceProjectDTO project = new CIServiceProjectDTO();
            project.setProjectType(service.getServiceType());
            project.setFolder(service.getFolder());
            project.setImageName(service.getImageName());
            projectList.add(project);
        }
    }

    /**
     * Add project to list
     *
     * @param projectList project list
     * @param service     service
     */
    private void addProject(List<CIServiceProjectDTO> projectList, BehaviorService service)
    {
        //Check if image already exists in docker registry
        if (Boolean.TRUE.equals(service.getForceCompilation()))
        {
            CIServiceProjectDTO project = new CIServiceProjectDTO();
            project.setProjectType(service.getServiceType());
            project.setFolder(service.getFolder());
            project.setImageName(service.getImageName());
            projectList.add(project);
        }
    }

    /**
     * Build default order
     *
     * @param services    list of services
     * @param repoId      repository id
     * @param projectList list of projects
     */
    private void buildDefaultOrder(List<ReleaseVersionService> services, Integer repoId, List<CIServiceProjectDTO>
            projectList)
    {
        log.debug("[{}] -> [{}]: Repository : {} has no dependencies file",
                com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT,
                "buildDefaultOrder", repoId);
        Map<String, ReleaseVersionService> dependencies = services.parallelStream()
                .filter(it -> ServiceType.DEPENDENCY.getServiceType().equals(it.getServiceType()))
                .collect(Collectors.toMap(ReleaseVersionService::getServiceName, it -> it));
        Set<GraphNode> graph = new HashSet<>();
        for (ReleaseVersionService service : dependencies.values())
        {
            if (service.getNovaYml() != null && service.getNovaYml().getContents() != null)
            {
                ValidatorInputs validatorInputs = new ValidatorInputs();
                ProjectFileReader.scanNovaYml(service.getFolder(), service.getNovaYml().getContents().getBytes(), validatorInputs);
                List<String> deps = validatorInputs.getNovaYml().getDependencies();
                GraphNode node = new GraphNode(service.getServiceName(), new HashSet<>(deps));
                List<String> notFoundDeps = deps.stream().filter(dep -> !dependencies.containsKey(dep))
                        .collect(Collectors.toList());
                if (!notFoundDeps.isEmpty())
                {
                    TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(service.getVersionSubsystem().getSubsystemId());
                    throw new NovaException(ReleaseVersionError.getDependencyNotFoundError(),
                            "The following dependencies " + notFoundDeps + " specified in the service "
                                    + service.getServiceName() + " doesnt exist in subsystem "
                                    + subsystemDTO.getSubsystemName());
                }
                graph.add(node);
            }
            else
            {
                graph.add(new GraphNode(service.getServiceName(), new HashSet<>()));
            }
        }
        try
        {
            List<String> orderedNodeNames = depResolvService.getDependencyResolutionOrder(graph);
            orderedNodeNames.stream().map(dependencies::get)
                    .forEachOrdered(service -> this.addProject(projectList, service));
        }
        catch (IllegalArgumentException e)
        {
            throw new NovaException(ReleaseVersionError.getDependencyGraphContainsCycleError(), e.getMessage());
        }

        int dependenciesCount = projectList.size();
        List<ReleaseVersionService> novaServices = services.parallelStream()
                .filter(it -> !ServiceType.DEPENDENCY.getServiceType().equals(it.getServiceType())).collect(Collectors.toList());

        for (ReleaseVersionService service : novaServices)
        {
            this.addProject(projectList, service);
        }
        // If all services to compile are dependences do not compile
        if (projectList.size() == dependenciesCount)
        {
            projectList.clear();
        }
    }

    /**
     * Build default order
     *
     * @param services    list of services
     * @param repoId      repository id
     * @param projectList list of projects
     */
    private void buildDefaultOrderBehavior(List<BehaviorService> services, Integer repoId, List<CIServiceProjectDTO>
            projectList)
    {
        log.debug("[{}] -> [{}]: Repository : {} has no dependencies file", "ContinuousintegrationClientImpl",
                "buildDefaultOrderBehavior", repoId);

        for (BehaviorService service : services)
        {
            this.addProject(projectList, service);
        }
    }

    /**
     * This method call the Continuous integration client service to build (compile) a EPHOENIX job type
     *
     * @param releaseVersion          - release version associated to the build
     * @param releaseVersionSubsystem - release version subsystem associated th the compilation
     * @param ephoenixJobParameters   - job parameters of the subsystem to compile
     * @param ivUser                  - user that generates the request
     * @param product                 - product associated to the release version subsystem
     */
    private void buildEphoenixSubsystemJob(final ReleaseVersion releaseVersion, final ReleaseVersionSubsystem releaseVersionSubsystem,
                                           final CIEphoenixJobParametersDTO ephoenixJobParameters, final String ivUser,
                                           final Product product)
    {
        TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(releaseVersionSubsystem.getSubsystemId());
        log.debug("[{}] -> [{}]: Calling to Continuous Integration Service to build (compile) a ephoenix Job [{}] from TAG: [{}] of repository: [{}]" +
                        "for the product name: [{}] with the following parameters: [{}] ", com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT,
                BUILD_EPHOENIX_SUBSYTEM_JOB, subsystemDTO.getSubsystemName(),
                releaseVersionSubsystem.getTagName(), releaseVersionSubsystem.getTagUrl(), product.getName(), ephoenixJobParameters);

        this.restHandler.buildEphoenixJob(new IRestListenerContinuousintegrationapi()
        {
            @Override
            public void buildEphoenixJob(final CIMultiResponseDTO outcome)
            {
                log.debug("[{}] -> [{}]: The compilation of the ePhoenix Jobs associated to the releaseVersionSubsystem [{}] has started." +
                                " Jenkins Job name: [{}] and  the Jenkis queue ids of the job list for this ePhoenix subsystem are: [{}]",
                        com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT, BUILD_EPHOENIX_SUBSYTEM_JOB,
                        subsystemDTO.getSubsystemName(), outcome.getMessage(), outcome.getValues());

                // Set the release version subsystem status to DOING
                releaseVersionSubsystem.setStatus(AsyncStatus.DOING);
                releaseVersionSubsystem.setCompilationJobName(outcome.getMessage());
            }

            @Override
            public void buildEphoenixJobErrors(Errors outcome)
            {
                log.error("[{}] -> [{}]: Release Version API-> buildEphoenixSubsystemJob: Can't start to compile the ePhoenix job with name [{}]." +
                                " Error in the Continuous Integration client service. Outcome: [{}]", com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT,
                        BUILD_EPHOENIX_SUBSYTEM_JOB, subsystemDTO.getSubsystemName(), outcome.getBodyExceptionMessage());

                // Set the Release releaseVersion releaseVersionSubsystem to status error
                releaseVersionSubsystem.setStatusMessage("Error trying to build releaseVersionSubsystem TAG '" + releaseVersionSubsystem.getTagName());
                releaseVersionSubsystem.setStatus(AsyncStatus.ERROR);

                // Set the Release releaseVersion to status ERROR
                releaseVersion.setStatus(ReleaseVersionStatus.ERRORS);
                releaseVersion.setStatusDescription("ERROR calling Continuous Integration service trying to compile the ePhoenix Job with name: " +
                        "[" + subsystemDTO.getSubsystemName() + "]. Contact with the NOVA Platform Admin");

                // Create a to do task - type COMPILE_JENKINS_JOB_ERROR
                createCompileJenkinsErrorTodoTask(subsystemDTO.getSubsystemType(), ivUser, product);
            }
        }, ephoenixJobParameters);
    }

    /**
     * Bytes to project list
     *
     * @param dependencies dependencies
     * @param services     list of services
     * @param projectList  list of projects
     */
    private void buildFromFile(List<String> dependencies, List<ReleaseVersionService> services, List<CIServiceProjectDTO>
            projectList)
    {
        for (String serviceFolder : dependencies)
        {
            ReleaseVersionService service = this.findService(serviceFolder, services);
            this.processService(projectList, serviceFolder, service);
        }
    }

    /**
     * Bytes to project list
     *
     * @param dependencies dependencies
     * @param services     list of services
     * @param projectList  list of projects
     */
    private void buildFromFileBehavior(List<String> dependencies, List<BehaviorService> services, List<CIServiceProjectDTO>
            projectList)
    {
        for (String serviceFolder : dependencies)
        {
            BehaviorService service = this.findBehaviorService(serviceFolder, services);
            this.processBehaviorService(projectList, serviceFolder, service);
        }
    }

    /**
     * Create the jenkins job view name in case of NOT compiling due to the SUBSYSTEM is already compiled
     *
     * @param subsystemId subsystem ID
     * @param product     the product name to get the UUAA
     * @return the BUILD job name
     */
    private String buildJenkinsJobViewName(final Integer subsystemId, final Product product)
    {
        TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(subsystemId);
        return String.format("%s_%s_BUILD", product.getUuaa().toUpperCase(), subsystemDTO.getSubsystemName().toUpperCase());
    }

    /**
     * Build NOVA services only
     *
     * @param services    list of services
     * @param projectList list of projects
     */
    private void buildNovaServices(List<ReleaseVersionService> services, List<CIServiceProjectDTO> projectList)
    {

        int numberOfDepencies = projectList.size();

        for (ReleaseVersionService service : services)
        {
            if (!ServiceType.DEPENDENCY.getServiceType().equals(service.getServiceType()) && !ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equals(service.getServiceType()))
            {
                this.addProject(projectList, service);
            }
        }

        // If there are only dependencies inside the project list, it will be
        // cleared
        if (projectList.size() == numberOfDepencies)
        {
            projectList.clear();
        }
    }

    /**
     * Build BEHAVIOR services only
     *
     * @param services    list of services
     * @param projectList list of projects
     */
    private void buildBehaviorServices(List<BehaviorService> services, List<CIServiceProjectDTO> projectList)
    {
        int numberOfDependencies = projectList.size();
        for (BehaviorService service : services)
        {
            this.addProject(projectList, service);
        }

        // If there are only dependencies inside the project list, it will be
        // cleared
        if (projectList.size() == numberOfDependencies)
        {
            projectList.clear();
        }
    }

    /**
     * This method call the Continuous integration service to compile a job of type NOVA subsystem
     *
     * @param releaseVersion          - release version associated to the job
     * @param releaseVersionSubsystem - release version subsystem associated to the release version
     * @param novaJobParametersDto    - parameters of the job
     * @param ivUser                  - user that generated the request
     * @param product                 - product associated to the release
     */
    private void buildNovaSubsystemsJob(final ReleaseVersion releaseVersion, final ReleaseVersionSubsystem releaseVersionSubsystem,
                                        final CINovaJobParametersDTO novaJobParametersDto, final String ivUser, final Product product)
    {
        SingleApiClientResponseWrapper<Integer> jobIdResult = new SingleApiClientResponseWrapper<>();
        SingleApiClientResponseWrapper<String> jobMessageResult = new SingleApiClientResponseWrapper<>();

        this.restHandler.buildNovaJob(new IRestListenerContinuousintegrationapi()
        {
            @Override
            public void buildNovaJob(final CIResponseDTO outcome)
            {
                log.debug("[{}] -> [{}]: BuildNovaSubsystemsJob: The NOVA releaseVersionSubsystem name [{}] with the UUAA {} has been started to " +
                                "compile. " +
                                "Jenkins Job: [{}]. Jenkins queue associted: [{}]", com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT, "buildNovaSubsystemsJob", novaJobParametersDto.getSubsystemName(),
                        novaJobParametersDto.getUuaa(), outcome.getMessage(), outcome.getValue());
                jobIdResult.set(Integer.valueOf(outcome.getValue()));
                jobMessageResult.set(outcome.getMessage());
            }

            @Override
            public void buildNovaJobErrors(Errors outcome)
            {
                log.error("BuildNovaSubsystemsJob: Can't start the building (compilation) of releaseVersionSubsystem [{}]." +
                        " Errors in Continuous Integration service. Error message: [{}].", novaJobParametersDto.getSubsystemName(), outcome.getBodyExceptionMessage());
                jobIdResult.set(JOB_ERROR_COMPILATION_ID);
                jobMessageResult.set(outcome.getBodyExceptionMessage().toString());
            }
        }, novaJobParametersDto);

        // Check length of the message
        if (jobMessageResult.get().length() > 100)
        {
            jobMessageResult.set(jobMessageResult.get().substring(0, 100));
        }
        this.evaluateResultNovaSubsystemsJob(jobIdResult.get(), releaseVersionSubsystem, novaJobParametersDto, releaseVersion, ivUser, product, jobMessageResult.get());
    }

    /**
     * This method call the Continuous integration service to compile a job of type BEHAVIOR_TEST subsystem
     *
     * @param behaviorVersion      - behavior version associated to the job
     * @param behaviorSubsystem    - behavior version subsystem associated to the release version
     * @param novaJobParametersDto - parameters of the job
     * @param ivUser               - user that generated the request
     * @param product              - product associated to the release
     */
    private void buildBehaviorSubsystemsJob(final BehaviorVersion behaviorVersion, final BehaviorSubsystem behaviorSubsystem,
                                            final CINovaJobParametersDTO novaJobParametersDto, final String ivUser, final Product product)
    {
        SingleApiClientResponseWrapper<Integer> jobIdResult = new SingleApiClientResponseWrapper<>();
        SingleApiClientResponseWrapper<String> jobMessageResult = new SingleApiClientResponseWrapper<>();

        this.restHandler.buildBehaviorTestJob(new IRestListenerContinuousintegrationapi()
        {
            @Override
            public void buildBehaviorTestJob(final CIResponseDTO outcome)
            {
                log.debug("[{}] -> [buildBehaviorTestJob]: buildBehaviorTestJob: The NOVA behaviorSubsystem name [{}] with the UUAA {} has started to compile. " +
                                "Jenkins Job: [{}]. Jenkins queue associated: [{}]",
                        com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT,
                        novaJobParametersDto.getSubsystemName(), novaJobParametersDto.getUuaa(), outcome.getMessage(), outcome.getValue());
                jobIdResult.set(Integer.valueOf(outcome.getValue()));
                jobMessageResult.set(outcome.getMessage());
            }

            @Override
            public void buildBehaviorTestJobErrors(Errors outcome)
            {
                log.error("[{}] -> [buildBehaviorTestJobErrors]: Can't start the building (compilation) of behavior test subsystem [{}]." +
                                " Errors in Continuous Integration service. Error message: [{}].",
                        com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT,
                        novaJobParametersDto.getSubsystemName(), outcome.getBodyExceptionMessage());
                jobIdResult.set(JOB_ERROR_COMPILATION_ID);
                jobMessageResult.set(outcome.getBodyExceptionMessage().toString());
            }
        }, novaJobParametersDto);

        // Check length of the message
        if (jobMessageResult.get().length() > 100)
        {
            jobMessageResult.set(jobMessageResult.get().substring(0, 100));
        }
        this.evaluateResultBehaviorSubsystemsJob(jobIdResult.get(), behaviorSubsystem, novaJobParametersDto, behaviorVersion, ivUser, product, jobMessageResult.get());
    }

    /**
     * Compile NOVA subsystem
     *
     * @param product                 product
     * @param releaseVersion          release version
     * @param ivUser                  ivUser
     * @param compiledSubsystem       number of compiled subsystems
     * @param releaseVersionSubsystem release version subsystem
     * @return number of compiled subsystems
     */
    private int compileSubsystemNova(Product product, ReleaseVersion releaseVersion, String ivUser, int compiledSubsystem, ReleaseVersionSubsystem releaseVersionSubsystem)
    {
        int newCompiledSubsystem = compiledSubsystem;
        // Create the job parameters for the NOVA subsystem type Job
        CINovaJobParametersDTO novaJobParametersDto = this.createJobParametersForNovaJob(releaseVersion, releaseVersionSubsystem, product);

        if (novaJobParametersDto.getProjects() != null && novaJobParametersDto.getProjects().length > 0)
        {
            // Start to build (compile) the NOVA subsystem Job calling the Continuous integration service
            this.buildNovaSubsystemsJob(releaseVersion, releaseVersionSubsystem, novaJobParametersDto, ivUser, product);

            log.debug("[{}] -> [{}]: the userCode [{}] has requested to build a new Image with job : [{}] from the release : [{}]. ",
                    com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT,
                    BUILD_SUBSYSTEMS, ivUser, releaseVersionSubsystem
                            .getCompilationJobName(),
                    releaseVersion.getRelease().getName());
        }
        else
        {
            newCompiledSubsystem = newCompiledSubsystem + 1;
            releaseVersionSubsystem.setStatus(AsyncStatus.DONE);
        }
        return newCompiledSubsystem;
    }

    /**
     * Compile NOVA subsystem
     *
     * @param product           product
     * @param behaviorVersion   behavior version
     * @param ivUser            ivUser
     * @param behaviorSubsystem behavior version subsystem
     * @return number of compiled subsystems
     */
    private int compileBehaviorSubsystem(Product product, BehaviorVersion behaviorVersion, String ivUser, int compiledSubsystem, BehaviorSubsystem behaviorSubsystem)
    {
        int newCompiledSubsystem = compiledSubsystem;
        // Create the job parameters for the NOVA subsystem type Job
        CINovaJobParametersDTO novaJobParametersDto = this.createJobParametersForBehaviorJob(behaviorVersion, behaviorSubsystem, product);

        if (novaJobParametersDto.getProjects() != null && novaJobParametersDto.getProjects().length > 0)
        {
            // Start to build (compile) the NOVA subsystem Job calling the Continuous integration service
            this.buildBehaviorSubsystemsJob(behaviorVersion, behaviorSubsystem, novaJobParametersDto, ivUser, product);

            log.debug("[{}] -> [{}]: the userCode [{}] has requested to build a new Image with job : [{}] from the behavior version: [{}]. ",
                    com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT,
                    BUILD_SUBSYSTEMS, ivUser, behaviorSubsystem
                            .getCompilationJobName(),
                    behaviorVersion.getVersionName());
        }
        else
        {
            newCompiledSubsystem = newCompiledSubsystem + 1;
            behaviorSubsystem.setStatus(AsyncStatus.DONE);
        }
        return newCompiledSubsystem;
    }


    /**
     * Compile ePhoenix Subsystem
     *
     * @param product                 product
     * @param releaseVersion          release version
     * @param ivUser                  ivUser
     * @param compiledSubsystem       number of compiled subsystems
     * @param releaseVersionSubsystem release version subsystem
     * @return number of compiled subsystems
     */
    private int compileSubsystemPhoenix(Product product, ReleaseVersion releaseVersion, String ivUser, int compiledSubsystem, ReleaseVersionSubsystem releaseVersionSubsystem)
    {
        int newCompiledSubsystem = compiledSubsystem;
        // Create the job parameters for the EPHOENIX subsystem type job
        CIEphoenixJobParametersDTO ephoenixJobParameters = this.createJobParametersForEphoenixJob(releaseVersion, releaseVersionSubsystem, product, ivUser);

        if (ephoenixJobParameters.getEphoenixService() != null && ephoenixJobParameters.getEphoenixService().length > 0)
        {

            // Start to build (compile) the EPHOENIX subsystem Job
            // calling the Continuous integration service
            this.buildEphoenixSubsystemJob(releaseVersion, releaseVersionSubsystem, ephoenixJobParameters,
                    ivUser, product);

            log.debug(
                    "[{}] -> [{}]: the userCode [{}] has requested to build a new Image with job : [{}] from the release : [{}]. ",
                    com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT, BUILD_SUBSYSTEMS, ivUser, releaseVersionSubsystem.getCompilationJobName(), releaseVersion.getRelease().getName());

        }
        else
        {

            newCompiledSubsystem = newCompiledSubsystem + 1;
            releaseVersionSubsystem.setStatus(AsyncStatus.DONE);

        }
        return newCompiledSubsystem;
    }

    /**
     * Create a to do task type compile jenkins job error
     *
     * @param subsystemType - type of job (NOVA or EPHOENIX)
     * @param ivUser        - user who trying to make the call
     * @param product       - product associated to the release version - subsystem
     */
    private void createCompileJenkinsErrorTodoTask(final String subsystemType, final String ivUser, final Product product)
    {
        String message = "ReleaseVersionAPI: error trying to create a new Release version of the product: ["
                + product.getName() + "]. Failed trying to call to Continuous Integration service for compiling a job of type: [" + subsystemType + "]." +
                " Review status service or Jenkins status.";

        this.todoTaskServiceClient.createGenericTask(ivUser, null, ToDoTaskType.COMPILE_JENKINS_JOB_ERROR.name(),
                RoleType.PLATFORM_ADMIN.name(), message, product.getId());
    }

    /**
     * Creates a new Job Parameter instance
     *
     * @param name  name of the parameter
     * @param value value of the parameter
     * @return a new Job parameter
     */
    private CIJobParameter createJobParameterInstance(final String name, final String value)
    {
        CIJobParameter jobParameter = new CIJobParameter();
        jobParameter.setKey(name);
        jobParameter.setValue(value);

        log.debug("[{}] -> [{}]: CreateJobParameter: a new job parameter was created: [{}]", com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT,
                "createJobParameterInstance", jobParameter);

        return jobParameter;
    }

    /**
     * Create the job parameters for compiling a job of EPHOENIX subsystem type.
     *
     * @param releaseVersion          - the ReleaseVersion
     * @param releaseVersionSubsystem - the releaseVersionSubsystem
     * @param product                 - the uuaa of the product.
     * @param ivUser                  - user that generated the call
     */
    private CIEphoenixJobParametersDTO createJobParametersForEphoenixJob(
            final ReleaseVersion releaseVersion,
            final ReleaseVersionSubsystem releaseVersionSubsystem,
            final Product product,
            final String ivUser)
    {
        TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(releaseVersionSubsystem.getSubsystemId());
        String subsystemName = subsystemDTO.getSubsystemName();

        log.debug("[{}] -> [{}]: Continuous Integration Service -> createJobParametersForEphoenixJob: generating a new ePhoenix Subsystem for " +
                        "Release releaseVersion: [{}] - Release releaseVersion releaseVersionSubsystem: [{}] - productName: [{}] - UUAA: [{}]." +
                        " Request by user: [{}]",
                com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT,
                CREATE_JOB_PARAMETERS_FOR_EPHOENIX_JOB, releaseVersion.getVersionName(), subsystemName,
                product.getName(), product.getUuaa(), ivUser);

        CIEphoenixJobParametersDTO ephoenixJobParameters = new CIEphoenixJobParametersDTO();

        // First [1]. Create the common parameters of the EPHOENIX Job and add to the instance build dto
        List<CIJobParameter> commonJobParameterList = new ArrayList<>();
        commonJobParameterList.add(this.createJobParameterInstance(Constants.RESPONSE_ID_PARAM, releaseVersionSubsystem.getId().toString()));
        commonJobParameterList.add(this.createJobParameterInstance(Constants.PROJECT_UUAA_PARAM, product.getUuaa()));
        commonJobParameterList.add(this.createJobParameterInstance(Constants.SUBSYSTEM_NAME_PARAM, subsystemName));
        commonJobParameterList.add(this.createJobParameterInstance(Constants.TAG_PARAM, releaseVersionSubsystem.getTagName()));
        // Set the release name and release version name to lowercase
        commonJobParameterList.add(this.createJobParameterInstance(Constants.RELEASE_NAME_PARAM, releaseVersion.getRelease().getName().toLowerCase()));
        commonJobParameterList.add(this.createJobParameterInstance(Constants.RELEASE_VERSION_PARAM, releaseVersion.getVersionName().toLowerCase()));

        CIJobParameter[] commonJobParameterArray = commonJobParameterList.toArray(new CIJobParameter[0]);
        ephoenixJobParameters.setJobParameters(commonJobParameterArray);
        log.debug("[{}] -> [{}]: CreateJobParametersForEphoenixJob: The common parameters of the ePhoenix Job are: [{}]",
                com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT,
                CREATE_JOB_PARAMETERS_FOR_EPHOENIX_JOB, commonJobParameterArray);

        // Second [2]. Create the ePhoenix service list and fill with ePhoenix service and the job parameters for this ePhoenix service.
        List<CIEphoenixServiceDTO> ephoenixServiceList = new ArrayList<>();
        List<ReleaseVersionService> services = releaseVersionSubsystem.getServices();

        // For each service, add the job parameter
        for (ReleaseVersionService service : services)
        {
            log.debug("[{}] -> [{}]: CreateJobParametersForEphoenixJob: adding the service name [{}]",
                    com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT,
                    CREATE_JOB_PARAMETERS_FOR_EPHOENIX_JOB, service.getServiceName());

            CIEphoenixServiceDTO ephoenixService = new CIEphoenixServiceDTO();
            List<CIJobParameter> ephoenixServiceParametersList = new ArrayList<>();

            ephoenixServiceParametersList.add(this.createJobParameterInstance(Constants.INSTANCE_NAME_PARAM, service.getEphoenixData().getInstanceName()));
            ephoenixServiceParametersList.add(this.createJobParameterInstance(Constants.INSTANCE_TYPE_PARAM, service.getServiceType().toUpperCase()));
            ephoenixServiceParametersList.add(this.createJobParameterInstance(Constants.IMAGE_NAME_PARAM, service.getImageName().toLowerCase()));
            ephoenixServiceParametersList.add(this.createJobParameterInstance(Constants.SONAR_QUALITY_GATE_PARAM, QualityUtils.getSonarQualityGate(product.getQualityLevel())));
            ephoenixServiceParametersList.add(this.createJobParameterInstance(Constants.BRANCH_SQA_PARAM, EPHOENIX_SONAR_BRANCH_TEMPLATE));

            if (service.getEphoenixData().getUuaas() != null)
            {
                String uuaas = String.join(",", service.getEphoenixData().getUuaas());
                ephoenixServiceParametersList.add(this.createJobParameterInstance(Constants.INSTANCE_UUAAS_PARAM, uuaas));
            }
            ephoenixServiceParametersList.add(this.createJobParameterInstance(Constants.INSTANCE_UUAA_PARAM, service.getEphoenixData().getInstanceUuaa()));

            // Create the ePhoenix service parameters array
            CIJobParameter[] ephoenixServiceParametersArray = ephoenixServiceParametersList.toArray(new CIJobParameter[0]);
            ephoenixService.setJobParameters(ephoenixServiceParametersArray);

            log.debug("[{}] -> [{}]: CreateJobParametersForEphoenixJob: added the ephoenix service with the our job parameters: [{}]",
                    com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT,
                    CREATE_JOB_PARAMETERS_FOR_EPHOENIX_JOB, ephoenixServiceParametersArray);

            // Validate if Image already exist in registry
            if (Boolean.TRUE.equals(service.getHasForceCompilation()))
            {
                // Add the ePhoenix service to the ePhoenix Service List
                ephoenixServiceList.add(ephoenixService);
            }
        }

        if (!ephoenixServiceList.isEmpty())
        {
            // Third [3]. Add the ephoenix service list to instance build dto
            CIEphoenixServiceDTO[] ephoenixServiceArray = ephoenixServiceList.toArray(new CIEphoenixServiceDTO[0]);
            ephoenixJobParameters.setEphoenixService(ephoenixServiceArray);
        }
        else
        {
            log.debug("[ReleaseVersionAPI] -> [createJobParametersForEphoenixJob]: the release version subsystem: [{}] is already fully compiled.", releaseVersionSubsystem.getTagName());

            // Subsystem compiled. Just add the compilation Job name
            releaseVersionSubsystem.setCompilationJobName(this.buildJenkinsJobViewName(releaseVersionSubsystem.getSubsystemId(), product));
            releaseVersionSubsystem.setStatusMessage("Subsistema ya compilado. Las imagenes de los servicios ya existen en Registry.");

            // In case of there is only one subsystem in the release version, set the release version status description
            if (releaseVersion.getSubsystems() != null && releaseVersion.getSubsystems().size() == 1)
            {
                releaseVersion.setStatusDescription("Release version compilada completamente. El subsistema ya fue compilado previamente.");
            }
        }

        // Finally [4]. Returns the ephoenix build dto
        return ephoenixJobParameters;
    }

    /**
     * Create the job parameters for compiling a job of NOVA subsystem type.
     *
     * @param releaseVersionSubsystem - the releaseVersionSubsystem
     * @param product                 - the product associated to the release.
     * @param releaseVersion          - release version associated to the build
     */
    private CINovaJobParametersDTO createJobParametersForNovaJob(final ReleaseVersion releaseVersion, final ReleaseVersionSubsystem releaseVersionSubsystem,
                                                                 final Product product)
    {
        TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(releaseVersionSubsystem.getSubsystemId());
        String subsystemName = subsystemDTO.getSubsystemName();

        log.debug("[{}] -> [{}]: CreateNovaSubsystemJobParameters: creating the NOVA Subsystem job parameters for the " +
                        "releaseVersionSubsystem name [{}]" +
                        " of the product name: [{}]", com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT, "createJobParametersForNovaJob",
                subsystemName, product.getName());

        CINovaJobParametersDTO novaJobParameters = new CINovaJobParametersDTO();
        novaJobParameters.setResponseId(releaseVersionSubsystem.getId());
        novaJobParameters.setUuaa(product.getUuaa());
        novaJobParameters.setSubsystemName(subsystemName);
        novaJobParameters.setTag(releaseVersionSubsystem.getTagName());
        novaJobParameters.setSonarBranch(SONAR_BRANCH_TEMPLATE);

        List<ReleaseVersionService> services = releaseVersionSubsystem.getServices();

        List<CIServiceProjectDTO> projectList = this.getProjectList(services, subsystemDTO.getRepoId(), releaseVersionSubsystem.getTagName());

        if (!projectList.isEmpty())
        {
            // Add Sonar quality gate to all services
            String sonarQualityGate = QualityUtils.getSonarQualityGate(product.getQualityLevel());
            projectList.forEach(service -> service.setSonarQualityDate(sonarQualityGate));

            novaJobParameters.setProjects(projectList.toArray(new CIServiceProjectDTO[0]));
        }
        else
        {
            log.debug("[ReleaseVersionAPI] -> [createJobParametersForNovaJob]: the release version subsystem: [{}] is already fully compiled.", releaseVersionSubsystem.getTagName());

            // Subsystem compiled. Just add the compilation Job name
            releaseVersionSubsystem.setCompilationJobName(this.buildJenkinsJobViewName(releaseVersionSubsystem.getSubsystemId(), product));
            releaseVersionSubsystem.setStatusMessage("Subsistema ya compilado. Las imagenes de los servicios ya existen en Registry.");

            // In case of there is only one subsystem in the release version, set the release version status description
            if (releaseVersion.getSubsystems() != null && releaseVersion.getSubsystems().size() == 1)
            {
                releaseVersion.setStatusDescription("Release version compilada completamente. El subsistema ya fue compilado previamente.");
            }
        }
        log.debug("CreateNovaSubsystemJobParameters: the NOVA job parameters created are: [{}]", novaJobParameters);
        log.debug("[{}] -> [{}]: CreateNovaSubsystemJobParameters: the NOVA job parameters created are: [{}]",
                com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT, "createJobParametersForNovaJob", novaJobParameters);

        return novaJobParameters;
    }

    /**
     * Create the job parameters for compiling a job of BEHAVIOR_TEST subsystem type.
     *
     * @param behaviorVersion   behavior version associated to the build
     * @param behaviorSubsystem the behavior subsystem
     * @param product           the product associated to the release.
     */
    private CINovaJobParametersDTO createJobParametersForBehaviorJob(final BehaviorVersion behaviorVersion, final BehaviorSubsystem behaviorSubsystem,
                                                                     final Product product)
    {
        TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(behaviorSubsystem.getSubsystemId());
        String subsystemName = subsystemDTO.getSubsystemName();

        log.debug("[{}] -> [{}]: createJobParametersForBehaviorJob: creating the NOVA Subsystem job parameters for the " +
                        "behaviorSubsystem name [{}]" +
                        " of the product name: [{}]", com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT, "createJobParametersForNovaJob",
                subsystemName, product.getName());

        CINovaJobParametersDTO novaJobParameters = new CINovaJobParametersDTO();
        novaJobParameters.setResponseId(behaviorSubsystem.getId());
        novaJobParameters.setUuaa(product.getUuaa());
        novaJobParameters.setSubsystemName(subsystemName);
        novaJobParameters.setTag(behaviorSubsystem.getTagName());
        novaJobParameters.setSonarBranch(SONAR_BRANCH_TEMPLATE);

        List<BehaviorService> services = behaviorSubsystem.getServices();

        List<CIServiceProjectDTO> projectList = this.getBehaviorProjectList(services, subsystemDTO.getRepoId());

        if (!projectList.isEmpty())
        {
            // Add Sonar quality gate to all services
            String sonarQualityGate = QualityUtils.getSonarQualityGate(product.getQualityLevel());
            projectList.forEach(service -> service.setSonarQualityDate(sonarQualityGate));

            novaJobParameters.setProjects(projectList.toArray(new CIServiceProjectDTO[0]));
        }
        else
        {
            log.debug("[ContinuousintegrationClientImpl] -> [createJobParametersForBehaviorJob]: the behavior version subsystem: [{}] is already fully compiled.", behaviorSubsystem.getTagName());

            // Subsystem compiled. Just add the compilation Job name
            behaviorSubsystem.setCompilationJobName(this.buildJenkinsJobViewName(behaviorSubsystem.getSubsystemId(), product));


            // In case of there is only one subsystem in the release version, set the release version status description
            if (behaviorVersion.getSubsystems() != null && behaviorVersion.getSubsystems().size() == 1)
            {
                behaviorVersion.setStatusDescription("Behavior version compilada completamente. El subsistema ya fue compilado previamente.");
            }
        }
        log.debug("CreateNovaSubsystemJobParameters: the NOVA job parameters created are: [{}]", novaJobParameters);
        log.debug("[{}] -> [{}]: CreateNovaSubsystemJobParameters: the NOVA job parameters created are: [{}]",
                com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT, "createJobParametersForNovaJob", novaJobParameters);

        return novaJobParameters;
    }

    /**
     * Evaluate NOVA job compilation results
     *
     * @param jobId                   the job id or the job compilation id error
     * @param releaseVersionSubsystem - release version subsystem associated th the compilation
     * @param novaJobParametersDto    - parameters of the job
     * @param releaseVersion          the release version
     * @param ivUser                  - user that generated the call
     * @param product                 - product associated to the release
     * @param resultMessage           result message of the compilation of NOVA job
     */
    private void evaluateResultNovaSubsystemsJob(final Integer jobId, final ReleaseVersionSubsystem releaseVersionSubsystem, final CINovaJobParametersDTO novaJobParametersDto,
                                                 final ReleaseVersion releaseVersion, final String ivUser, final Product product, final String resultMessage)
    {
        if (jobId == JOB_ERROR_COMPILATION_ID)
        {
            // Set the release version subsystem status to ERROR
            releaseVersionSubsystem.setStatus(AsyncStatus.ERROR);

            releaseVersionSubsystem.setStatusMessage("ERROR compiling a NOVA Subsystem Job with name: [" + novaJobParametersDto.getSubsystemName()
                    + "] and TAG: [" + releaseVersionSubsystem.getTagUrl() + "] due to: [" + resultMessage + "]");

            // Set the release version
            releaseVersion.setStatus(ReleaseVersionStatus.ERRORS);
            releaseVersion.setStatusDescription("BuildNovaSubsystemsJob: ERROR trying to build (compile) a NOVA Subsystem Job with name: [" + novaJobParametersDto.getSubsystemName()
                    + "] associated to the release version: [" + releaseVersion.getVersionName() + "]");

            TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(releaseVersionSubsystem.getSubsystemId());

            // Create the to do task, log the error and
            String todoTaskDescription = "[ContinuousIntegrationClientImpl] -> [evaluateResultNovaSubsystemsJob]: se ha producido un error intentado compilar un subsistema con los siguientes parametros."
                    + LINE_SEPARATOR
                    + " - [Release]: " + releaseVersion.getRelease().getName()
                    + LINE_SEPARATOR
                    + " - [Release Version]: " + releaseVersion.getVersionName()
                    + LINE_SEPARATOR
                    + " - [Nombre del Subsystema]: " + subsystemDTO.getSubsystemName()
                    + LINE_SEPARATOR
                    + " - [Tipo de Subsystema]: " + subsystemDTO.getSubsystemType()
                    + LINE_SEPARATOR
                    + " - [Nombre del TAG Asociado]: " + releaseVersionSubsystem.getTagName()
                    + LINE_SEPARATOR
                    + " - [Nombre del Job de Jenkins]: " + releaseVersionSubsystem.getCompilationJobName()
                    + LINE_SEPARATOR
                    + " - [Id del Job de Jenkins]: " + jobId
                    + LINE_SEPARATOR
                    + " - [Parametros]: " + novaJobParametersDto
                    + LINE_SEPARATOR
                    + " - [Estado del subsystem/Job]: " + releaseVersionSubsystem.getStatus().name()
                    + LINE_SEPARATOR
                    + " - [Primeras trazas del mensaje de error]: " + resultMessage
                    + LINE_SEPARATOR
                    + "Aunque haya compilado correctamente el JOB, puede que haya habido un error interno en la plataforma. Elimine la version de release creada e intente volver a crear una nueva version de release.";

            this.todoTaskServiceClient.createGenericTask(ivUser, null, ToDoTaskType.COMPILE_JENKINS_JOB_ERROR.name(), RoleType.PLATFORM_ADMIN.name(), todoTaskDescription, product.getId());
        }
        else
        {
            // Set the release releaseVersion values
            releaseVersionSubsystem.setStatus(AsyncStatus.DOING);
            releaseVersionSubsystem.setCompilationJobName(resultMessage);
        }
    }

    /**
     * Evaluate BEHAVIOR_TEST job compilation results
     *
     * @param jobId                the job id or the job compilation id error
     * @param behaviorSubsystem    behavior version subsystem associated th the compilation
     * @param novaJobParametersDto parameters of the job
     * @param behaviorVersion      the behavior version
     * @param ivUser               user that generated the call
     * @param product              product associated to the release
     * @param resultMessage        result message of the compilation of NOVA job
     */
    private void evaluateResultBehaviorSubsystemsJob(final Integer jobId, final BehaviorSubsystem behaviorSubsystem, final CINovaJobParametersDTO novaJobParametersDto,
                                                     final BehaviorVersion behaviorVersion, final String ivUser, final Product product, final String resultMessage)
    {
        if (jobId == JOB_ERROR_COMPILATION_ID)
        {
            // Set the release version subsystem status to ERROR
            behaviorSubsystem.setStatus(AsyncStatus.ERROR);

            behaviorSubsystem.setStatusMessage("ERROR compiling a BEHAVIOR_TEST Subsystem Job with name: [" + novaJobParametersDto.getSubsystemName()
                    + "] and TAG: [" + behaviorSubsystem.getTagUrl() + "] due to: [" + resultMessage + "]");

            // Set the release version
            behaviorVersion.setStatus(BehaviorVersionStatus.ERRORS);
            behaviorVersion.setStatusDescription("buildBehaviorSubsystemsJob: ERROR trying to build (compile) a BEHAVIOR_TEST Subsystem Job with name: [" + novaJobParametersDto.getSubsystemName()
                    + "] associated to the behavior version: [" + behaviorVersion.getVersionName() + "]");

            TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(behaviorSubsystem.getSubsystemId());

            // Create the to do task, log the error and
            String todoTaskDescription = "[ContinuousIntegrationClientImpl] -> [evaluateResultBehaviorSubsystemsJob]: se ha producido un error intentado compilar un subsistema con los siguientes parametros."
                    + LINE_SEPARATOR
                    + " - [Behavior Version]: " + behaviorVersion.getVersionName()
                    + LINE_SEPARATOR
                    + " - [Nombre del Subsistema]: " + subsystemDTO.getSubsystemName()
                    + LINE_SEPARATOR
                    + " - [Tipo de Subsistema]: " + subsystemDTO.getSubsystemType()
                    + LINE_SEPARATOR
                    + " - [Nombre del TAG Asociado]: " + behaviorSubsystem.getTagName()
                    + LINE_SEPARATOR
                    + " - [Nombre del Job de Jenkins]: " + behaviorSubsystem.getCompilationJobName()
                    + LINE_SEPARATOR
                    + " - [Id del Job de Jenkins]: " + jobId
                    + LINE_SEPARATOR
                    + " - [Parametros]: " + novaJobParametersDto
                    + LINE_SEPARATOR
                    + " - [Estado del subsystem/Job]: " + behaviorSubsystem.getStatus().name()
                    + LINE_SEPARATOR
                    + " - [Primeras trazas del mensaje de error]: " + resultMessage
                    + LINE_SEPARATOR
                    + "Aunque haya compilado correctamente el JOB, puede que haya habido un error interno en la plataforma. Elimine la version creada e intente volver a crear una nueva version.";

            this.todoTaskServiceClient.createGenericTask(ivUser, null, ToDoTaskType.COMPILE_JENKINS_JOB_ERROR.name(), RoleType.PLATFORM_ADMIN.name(), todoTaskDescription, product.getId());
        }
        else
        {
            // Set the release releaseVersion values
            behaviorSubsystem.setStatus(AsyncStatus.DOING);
            behaviorSubsystem.setCompilationJobName(resultMessage);
        }
    }


    /**
     * Find service
     *
     * @param serviceFolder service folder
     * @param services      list of services
     * @return release version service
     */
    private ReleaseVersionService findService(String serviceFolder, List<ReleaseVersionService> services)
    {
        ReleaseVersionService response = null;
        for (ReleaseVersionService service : services)
        {
            if (serviceFolder.equals(service.getFolder()))
            {
                response = service;
                break;
            }
        }
        return response;
    }

    /**
     * Find service
     *
     * @param serviceFolder service folder
     * @param services      list of services
     * @return behavior version service
     */
    private BehaviorService findBehaviorService(String serviceFolder, List<BehaviorService> services)
    {
        BehaviorService response = null;
        for (BehaviorService service : services)
        {
            if (serviceFolder.equals(service.getFolder()))
            {
                response = service;
                break;
            }
        }
        return response;
    }

    /**
     * Get project list
     *
     * @param services services
     * @param repoId   repository id
     * @param tagName  tag name
     * @return list of projects
     */
    private List<CIServiceProjectDTO> getProjectList(List<ReleaseVersionService> services,
                                                     final Integer repoId,
                                                     final String tagName)
    {
        List<CIServiceProjectDTO> projectList = new ArrayList<>();
        // Get dependencies from service, repo and tag.
        List<String> dependencies = this.vcsClient.getDependencies(repoId, tagName);
        //Check file
        if (dependencies.isEmpty())
        {
            this.buildDefaultOrder(services, repoId, projectList);
        }
        else
        {
            // Builds dependencies from file
            this.buildFromFile(dependencies, services, projectList);

            // Builds Nova Services
            this.buildNovaServices(services, projectList);
        }
        return projectList;
    }

    /**
     * Get project list
     *
     * @param services services
     * @param repoId   repository id
     * @return list of projects
     */
    private List<CIServiceProjectDTO> getBehaviorProjectList(List<BehaviorService> services, final Integer repoId)
    {
        List<CIServiceProjectDTO> projectList = new ArrayList<>();
        //Check file
        this.buildDefaultOrderBehavior(services, repoId, projectList);
        return projectList;
    }

    /**
     * Process service
     *
     * @param projectList   list of projects
     * @param serviceFolder service folder
     * @param service       service
     */
    private void processService(List<CIServiceProjectDTO> projectList, String serviceFolder,
                                ReleaseVersionService service)
    {
        if (service == null)
        {
            log.error("[{}] -> [{}]: Service folder not found {}", com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT, "processService", serviceFolder);
            throw new NovaException(ReleaseVersionError.getFolderNotFoundError(), serviceFolder);
        }
        else
        {
            this.addProject(projectList, service);
        }
    }

    /**
     * Process service
     *
     * @param projectList   list of projects
     * @param serviceFolder service folder
     * @param service       service
     */
    private void processBehaviorService(List<CIServiceProjectDTO> projectList, String serviceFolder,
                                        BehaviorService service)
    {
        if (service == null)
        {
            log.error("[{}] -> [{}]: Service folder not found {}", com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.CONTINUOUS_INTEGRATION_CLIENT, "processService", serviceFolder);
            throw new NovaException(ReleaseVersionError.getFolderNotFoundError(), serviceFolder);
        }
        else
        {
            this.addProject(projectList, service);
        }
    }

}
