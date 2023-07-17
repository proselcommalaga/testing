package com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.impl;

import com.bbva.enoa.apirestgen.qualityassuranceapi.model.*;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.*;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.SubsystemTagDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorService;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorSubsystem;
import com.bbva.enoa.datamodel.model.behavior.entities.BehaviorVersion;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.ChangeType;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentCategory;
import com.bbva.enoa.datamodel.model.product.enumerates.DocumentType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.common.Constants;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DocSystemRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.QualityUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IQualityAssuranceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.exception.QualityManagerErrorCode;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.services.interfaces.IQualityManagerService;
import com.bbva.enoa.platformservices.coreservice.qualitymanagerapi.util.QualityConstants;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IServiceDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.GraphNode;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.IDependencyResolutionService;
import com.bbva.enoa.utils.clientsutils.consumers.interfaces.IUsersClient;
import com.bbva.enoa.utils.clientsutils.model.GenericActivity;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityAction;
import com.bbva.enoa.utils.enrollednovaactivities.enums.ActivityScope;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Service for getting quality information
 */
@Service
public class QualityManagerService implements IQualityManagerService
{

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(QualityManagerService.class);


    private static final NovaException PERMISSION_DENIED = new NovaException(QualityManagerErrorCode.getForbiddenError(), QualityManagerErrorCode.getForbiddenError().toString());

    /**
     * Release Version Repository
     */
    private final ReleaseVersionRepository releaseVersionRepository;

    /**
     * Quality Assurance Client Service
     */
    private final IQualityAssuranceClient qualityAssuranceClient;

    /**
     * Version control system Client
     */
    private final IVersioncontrolsystemClient versionControlSystemClient;

    /**
     * Product repository
     */
    private final DeploymentPlanRepository planRepository;

    /**
     * User service client
     */
    private final IUsersClient usersService;

    /**
     * Service dto builder
     */
    private final IServiceDtoBuilder serviceDtoBuilder;

    /**
     * Tools service client
     */
    private final IToolsClient toolsService;

    /**
     * Product repository
     */
    private final ProductRepository productRepository;

    /**
     * DocSystem repository
     */
    private final DocSystemRepository docSystemRepo;

    /**
     * Dependency resolution service
     */
    private final IDependencyResolutionService dependencyResolutionService;

    /**
     * Nova activities emitter
     */
    private final INovaActivityEmitter novaActivityEmitter;

    /**
     * Deployment utils
     */
    private final DeploymentUtils deploymentUtils;

    /**
     * Constructor
     *
     * @param releaseVersionRepository    release version repository
     * @param planRepository              deployment plan repository
     * @param qualityAssuranceClient      quality assurance dependency
     * @param versionControlSystemClient  VCS dependency
     * @param usersService                Users dependency
     * @param serviceDtoBuilder           Service DTO builder
     * @param iToolsClient                Tools dependency
     * @param productRepository           Product repository
     * @param docSystemRepo               DocSystem repository
     * @param dependencyResolutionService Dependency resolution service
     * @param novaActivityEmitter         NovaActivity emitter
     */
    @Autowired
    public QualityManagerService(final ReleaseVersionRepository releaseVersionRepository, final DeploymentPlanRepository planRepository,
                                 final IQualityAssuranceClient qualityAssuranceClient, final IVersioncontrolsystemClient versionControlSystemClient, final IUsersClient usersService,
                                 final IServiceDtoBuilder serviceDtoBuilder, final IToolsClient iToolsClient, final ProductRepository productRepository, DocSystemRepository docSystemRepo,
                                 final IDependencyResolutionService dependencyResolutionService, final INovaActivityEmitter novaActivityEmitter, final DeploymentUtils deploymentUtils)
    {
        this.releaseVersionRepository = releaseVersionRepository;
        this.planRepository = planRepository;
        this.qualityAssuranceClient = qualityAssuranceClient;
        this.versionControlSystemClient = versionControlSystemClient;
        this.usersService = usersService;
        this.serviceDtoBuilder = serviceDtoBuilder;
        this.toolsService = iToolsClient;
        this.productRepository = productRepository;
        this.docSystemRepo = docSystemRepo;
        this.dependencyResolutionService = dependencyResolutionService;
        this.novaActivityEmitter = novaActivityEmitter;
        this.deploymentUtils = deploymentUtils;
    }

    @Override
    @Transactional
    public void requestSubsystemCodeAnalysis(final String ivUser, final Integer subsystemId, final String branchName)
    {
        LOG.debug("[QualityManagerService] -> [requestSubsystemCodeAnalysis]: Requesting code analysis for branch: [{}] in subsystem: [{}]", branchName, subsystemId);

        // Encode branchName
        String branchNameEncode = validateAndEncodeBranchName(branchName);

        // Get subsystem
        TOSubsystemDTO subsystem = this.toolsService.getSubsystemById(subsystemId);

        if (subsystem == null)
        {
            throw new NovaException(QualityManagerErrorCode.getInvalidSubsystemError(), "Cannot find specified subsystem");
        }

        // Check Permission by product id (Note = resource Id == product Id)
        Product product = this.productRepository.findById(subsystem.getResourceId()).orElseThrow(() -> {
            String errorMessage = MessageFormat.format("Product id {0} associated to subsystem id {1} not found", subsystem.getResourceId(), subsystemId);
            throw new NovaException(QualityManagerErrorCode.getProductNotFoundError(), errorMessage);
        });

        this.usersService.checkHasPermission(ivUser, QualityConstants.CHECK_QUALITY_PERMISSION, product.getId(), PERMISSION_DENIED);

        // If it is ephoenix type, call ephoenix specific quality.
        if (subsystem.getSubsystemType().equals(SubsystemType.EPHOENIX.getType()))
        {
            QAEphoenixSubsystemCodeAnalysisRequest subsystemCodeAnalysisRequest = this.createEphoenixSubsystemCodeAnalysisRequest(subsystem, branchName, product);
            this.qualityAssuranceClient.requestEphoenixSubsystemCodeAnalysis(subsystemCodeAnalysisRequest, subsystemId);
        }
        else
        {
            SubsystemType subsystemType = SubsystemType.getValueOf(subsystem.getSubsystemType());
            QAServiceDTO[] projectList = this.retrieveListOfNovaProjects(subsystem.getRepoId(), branchNameEncode, subsystemType, product, subsystem.getSubsystemName());

            QASubsystemCodeAnalysisRequest subsystemCodeAnalysisRequest = new QASubsystemCodeAnalysisRequest();
            subsystemCodeAnalysisRequest.setBranchName(branchNameEncode);
            subsystemCodeAnalysisRequest.setSubsystemName(subsystem.getSubsystemName());
            subsystemCodeAnalysisRequest.setSubsystemType(subsystem.getSubsystemType());
            subsystemCodeAnalysisRequest.setUuaa(product.getUuaa());
            subsystemCodeAnalysisRequest.setServices(projectList);
            subsystemCodeAnalysisRequest.setSonarQualityGate(QualityUtils.getSonarQualityGate(product.getQualityLevel()));

            this.qualityAssuranceClient.requestSubsystemCodeAnalysis(subsystemCodeAnalysisRequest, subsystemId);
        }

        // Emit Start Quality Activity
        this.novaActivityEmitter.emitNewActivity(new GenericActivity
                .Builder(product.getId(), ActivityScope.QUALITY, ActivityAction.STARTED)
                .entityId(subsystemId)
                .addParam("branchName", branchNameEncode)
                .addParam("SubsystemName", subsystem.getSubsystemName())
                .addParam("SubsystemType", subsystem.getSubsystemType())
                .build());

        LOG.debug("[QualityManagerService] -> [requestSubsystemCodeAnalysis]: Requested code analysis job for branch: [{}] in subsystem: [{}]", branchName, subsystemId);
    }

    @Override
    public ReleaseVersionCodeAnalyses getReleaseVersionCodeAnalyses(final Integer releaseVersionId)
    {
        LOG.debug("[QualityManagerService] -> [getReleaseVersionCodeAnalysesWithChecks]: getting code analyses for release version id: [{}]", releaseVersionId);

        // Check if the release version exists
        ReleaseVersion releaseVersion = this.releaseVersionRepository.findById(releaseVersionId).orElseThrow(() -> {
            throw new NovaException(QualityManagerErrorCode.getInvalidReleaseVersionError());
        });

        // Check if release version is in valid state
        ReleaseVersionStatus releaseVersionStatus = releaseVersion.getStatus();
        if (releaseVersionStatus == ReleaseVersionStatus.BUILDING || releaseVersionStatus == ReleaseVersionStatus.ERRORS)
        {
            ReleaseVersionCodeAnalyses response = new ReleaseVersionCodeAnalyses();
            response.setSqaState(QualityConstants.SQA_NOT_AVAILABLE);
            response.setSubsystems(new SubsystemCodeAnalyses[0]);
            return response;
        }

        // Initial SQA state
        String sqaState = QualityConstants.SQA_OK;

        //Subsystems
        List<SubsystemCodeAnalyses> subsystemCodeAnalysesList = new ArrayList<>();
        for (ReleaseVersionSubsystem subsystem : releaseVersion.getSubsystems())
        {
            TOSubsystemDTO subsystemDTO = this.toolsService.getSubsystemById(subsystem.getSubsystemId());

            SubsystemCodeAnalyses subsystemCodeAnalyses = new SubsystemCodeAnalyses();
            subsystemCodeAnalyses.setSubsystemType(subsystemDTO.getSubsystemType());
            subsystemCodeAnalyses.setSubsystemName(subsystemDTO.getSubsystemName());
            subsystemCodeAnalyses.setTag(subsystem.getTagName());

            List<ServiceCodeAnalyses> qualityServiceList = new ArrayList<>();
            String sqaSubsystemState = this.getSubsystemState(subsystem, qualityServiceList);
            subsystemCodeAnalyses.setSqaState(sqaSubsystemState);
            sqaState = this.checkState(sqaSubsystemState, sqaState);
            subsystemCodeAnalyses.setServices(qualityServiceList.toArray(new ServiceCodeAnalyses[0]));
            subsystemCodeAnalysesList.add(subsystemCodeAnalyses);
        }

        ReleaseVersionCodeAnalyses response = new ReleaseVersionCodeAnalyses();
        response.setSqaState(sqaState);
        response.setSubsystems(subsystemCodeAnalysesList.toArray(new SubsystemCodeAnalyses[0]));

        return response;
    }

    @Override
    public String checkReleaseVersionQualityState(ReleaseVersion releaseVersion, boolean includesNotCompiled)
    {
        String result = QualityConstants.SQA_OK;

        String uuaa = releaseVersion.getRelease().getProduct().getUuaa();

        for (ReleaseVersionSubsystem releaseVersionSubsystem : releaseVersion.getSubsystems())
        {
            TOSubsystemDTO subsystemDTO = this.toolsService.getSubsystemById(releaseVersionSubsystem.getSubsystemId());

            for (ReleaseVersionService service : releaseVersionSubsystem.getServices())
            {
                if (QualityUtils.hasQualityAnalysis(ServiceType.valueOf(service.getServiceType())))
                {
                    if (Boolean.TRUE.equals(service.getHasForceCompilation()) || (includesNotCompiled && Boolean.FALSE.equals(service.getHasForceCompilation())))
                    {
                        String serviceResult = this.checkReleaseVersionServiceQualityState(service, uuaa, subsystemDTO);
                        result = this.updateQualityResult(result, serviceResult);
                    }
                }
            }
        }

        return result;
    }

    @Override
    public String checkBehaviorVersionQualityState(BehaviorVersion behaviorVersion, boolean includesNotCompiled)
    {
        String result = QualityConstants.SQA_OK;

        String uuaa = behaviorVersion.getProduct().getUuaa();

        for (BehaviorSubsystem behaviorSubsystem : behaviorVersion.getSubsystems())
        {
            TOSubsystemDTO subsystemDTO = this.toolsService.getSubsystemById(behaviorSubsystem.getSubsystemId());

            for (BehaviorService service : behaviorSubsystem.getServices())
            {
                if (QualityUtils.hasQualityAnalysis(ServiceType.valueOf(service.getServiceType())))
                {
                    if (Boolean.TRUE.equals(service.getForceCompilation()) || (includesNotCompiled && Boolean.FALSE.equals(service.getForceCompilation())))
                    {
                        String serviceResult = this.checkBehaviorVersionServiceQualityState(service, uuaa, subsystemDTO);
                        result = this.updateQualityResult(result, serviceResult);
                    }
                }
            }
        }
        return result;
    }

    private String checkReleaseVersionServiceQualityState(ReleaseVersionService service, String uuaa, TOSubsystemDTO subsystemDTO)
    {
        QACodeAnalysisRequest request = new QACodeAnalysisRequest();
        request.setServiceName(service.getServiceName());
        request.setServiceType(service.getServiceType());

        // Ephoenix
        if (ServiceType.valueOf(service.getServiceType()).isEPhoenix())
        {

            // Case ephoenix with no modules
            String result = QualityConstants.SQA_OK;

            for (String module : service.getEphoenixData().getModules())
            {
                // Path to POM (UUAA+module)
                final String projectPath = uuaa +
                        module.replace("..", "");

                // Get module version from pom
                String moduleVersion = getModuleVersionFromPom(projectPath, subsystemDTO.getRepoId(), service.getVersionSubsystem().getTagName());

                // In case the module version was incompatible, return error state for quality
                if (null == moduleVersion || Constants.INCOMPATIBLE_EPHOENIX_MODULE_VERSION_CODE.equals(moduleVersion))
                {
                    result = QualityConstants.SQA_ERROR_PROCESSING_MODULE;
                }
                else
                {
                    // For each module, if version is correct, set the version and sonar project name to check quality
                    request.setServiceVersion(moduleVersion);
                    request.setSonarProjectName(getEphoenixSonarProjectName(module, uuaa, subsystemDTO));

                    QACodeAnalysis codeAnalysis = qualityAssuranceClient.createCodeAnalysis(service.getId(), request);
                    String moduleQualityResult = (codeAnalysis == null) ? QualityConstants.SQA_NOT_AVAILABLE : codeAnalysis.getSqaState();
                    result = this.updateQualityResult(result, moduleQualityResult);
                }
            }

            return result;
        }

        // No Ephoenix
        request.setSonarProjectName(getNovaSonarProjectName(service.getServiceName(), uuaa, subsystemDTO));
        request.setServiceVersion(service.getVersion());
        QACodeAnalysis codeAnalysis = qualityAssuranceClient.createCodeAnalysis(service.getId(), request);
        return (codeAnalysis == null) ? QualityConstants.SQA_NOT_AVAILABLE : codeAnalysis.getSqaState();
    }

    private String checkBehaviorVersionServiceQualityState(BehaviorService service, String uuaa, TOSubsystemDTO subsystemDTO)
    {
        QACodeAnalysisRequest request = new QACodeAnalysisRequest();
        request.setServiceName(service.getServiceName());
        request.setServiceType(service.getServiceType());
        request.setSonarProjectName(getNovaSonarProjectName(service.getServiceName(), uuaa, subsystemDTO));
        request.setServiceVersion(service.getVersion());
        QACodeAnalysis codeAnalysis = qualityAssuranceClient.createBehaviorCodeAnalysis(service.getId(), request);
        return (codeAnalysis == null) ? QualityConstants.SQA_NOT_AVAILABLE : codeAnalysis.getSqaState();
    }

    private String getModuleVersionFromPom(final String projectPath, final int repoId, final String tag)
    {
        // Get the pom file of the module
        byte[] bytesOfPom = this.versionControlSystemClient.getPomFromProject(projectPath, repoId, tag);

        // Build a reader for the pom.xml file.
        final MavenXpp3Reader mavenReader = new MavenXpp3Reader();

        final String pomVersion;
        try
        {
            // Read and parse de pom.xml file.
            Model model = mavenReader.read(new ByteArrayInputStream(bytesOfPom));

            LOG.debug("Pom content: [{}]", model);
            pomVersion = model.getVersion();

            // The pom version must be present and not empty, otherwise an exception is thrown
            if (null == pomVersion || pomVersion.isEmpty())
            {
                LOG.error("There was an error with the version from pom.xml file in project path: [{}], pom version value was [{}]", projectPath, pomVersion);
                return Constants.INCOMPATIBLE_EPHOENIX_MODULE_VERSION_CODE;
            }
        }
        catch (XmlPullParserException | IOException | NullPointerException e)
        {
            LOG.error("There was and error parsing pom.xml file from project path: [{}]", projectPath, e);

            throw new NovaException(QualityManagerErrorCode.getEphoenixPomNotFoundError(projectPath));
        }

        return pomVersion;
    }

    private String updateQualityResult(String currentResult, String newResult)
    {
        if (QualityConstants.SQA_NOT_AVAILABLE.equals(currentResult) || QualityConstants.SQA_NOT_AVAILABLE.equals(newResult))
        {
            return QualityConstants.SQA_NOT_AVAILABLE;
        }
        if (QualityConstants.SQA_ERROR.equals(currentResult) || QualityConstants.SQA_ERROR.equals(newResult))
        {
            return QualityConstants.SQA_ERROR;
        }
        if (QualityConstants.SQA_ERROR_PROCESSING_MODULE.equals(currentResult) || QualityConstants.SQA_ERROR_PROCESSING_MODULE.equals(newResult))
        {
            return QualityConstants.SQA_ERROR_PROCESSING_MODULE;
        }

        return QualityConstants.SQA_OK;
    }

    @Override
    public CodeAnalysisStatus[] getCodeAnalysisStatuses(int[] releaseVersionIdList)
    {
        List<CodeAnalysisStatus> codeAnalysisStatusList = new ArrayList<>();
        if (releaseVersionIdList != null)
        {
            for (int releaseVersionId : IntStream.of(releaseVersionIdList).distinct().toArray())
            {
                this.releaseVersionRepository.findById(releaseVersionId).ifPresent(releaseVersion -> {
                    CodeAnalysisStatus codeAnalysisStatus = new CodeAnalysisStatus();
                    codeAnalysisStatus.setReleaseVersionId(releaseVersionId);
                    if (releaseVersion.getQualityValidation() == null)
                    {
                        codeAnalysisStatus.setCodeAnalysisStatus(QualityConstants.SQA_NOT_AVAILABLE);
                    }
                    else
                    {
                        codeAnalysisStatus.setCodeAnalysisStatus(Boolean.TRUE.equals(releaseVersion.getQualityValidation()) ? QualityConstants.SQA_OK : QualityConstants.SQA_ERROR);
                    }
                    codeAnalysisStatusList.add(codeAnalysisStatus);
                });
            }
        }
        return codeAnalysisStatusList.toArray(new CodeAnalysisStatus[0]);
    }

    @Override
    @Transactional
    public void removeQualityInfo(final ReleaseVersion releaseVersion)
    {
        // Remove code analysis for each service in release version
        for (ReleaseVersionSubsystem releaseVersionSubsystem : releaseVersion.getSubsystems())
        {
            releaseVersionSubsystem.getServices().stream()
                    .filter(releaseVersionService -> QualityUtils.hasQualityAnalysis(ServiceType.valueOf(releaseVersionService.getServiceType())))
                    .forEach(releaseVersionService -> qualityAssuranceClient.removeCodeAnalyses(releaseVersionService.getId()));
        }

        // Remove performance reports
        this.qualityAssuranceClient.deletePerformanceReportsByReleaseVersion(releaseVersion.getId());
    }

    @Override
    public void setPerformanceReport(final String ivUser, final Integer planId, final PerformanceReport performanceReport)
    {
        LOG.debug("[QualityManagerService] -> [setPerformanceReport]: setting performance report for plan id: [{}] requested from ivUser: [{}]", planId, ivUser);

        // Check permissions
        this.usersService.checkHasPermission(ivUser, QualityConstants.SET_PERFORMANCE_REPORT_PERMISSION, PERMISSION_DENIED);

        // Get plan
        DeploymentPlan plan = this.planRepository.findById(planId).orElseThrow(() -> {
            throw new NovaException(QualityManagerErrorCode.getInvalidPlanError(planId));
        });

        // Check plan is deployed on PRE
        if (!(plan.getStatus() == DeploymentStatus.DEPLOYED && Environment.PRE.getEnvironment().equals(plan.getEnvironment())))
        {
            throw new NovaException(QualityManagerErrorCode.getPlanNotDeployedOnPreError());
        }

        // Create a performance report dto
        QAPerformanceReport qaPerformanceReport = new QAPerformanceReport();
        qaPerformanceReport.setLink(performanceReport.getLink());
        qaPerformanceReport.setRiskLevel(performanceReport.getRiskLevel());
        qaPerformanceReport.setDescription(performanceReport.getDescription());

        // Call Quality Assurance API
        this.qualityAssuranceClient.setPerformanceReport(qaPerformanceReport, planId, plan.getReleaseVersion().getId());

        // Get doc system and update the associated field to true
        Optional<DocSystem> optional = this.docSystemRepo.findByUrlAndCategoryAndType(performanceReport.getLink(), DocumentCategory.PERFORMANCE_REPORTS, DocumentType.FILE);
        if (optional.isPresent())
        {
            DocSystem docSystem = optional.get();
            docSystem.setAssociated(true);
            docSystemRepo.save(docSystem);
        }

        // Finally add an entry to the plan history.
        this.deploymentUtils.addHistoryEntry(ChangeType.ADD_DOCUMENTATION, plan, ivUser, "Añadido informe de rendiemiento");
    }

    @Override
    public void deletePerformanceReport(final String ivUser, final Integer planId)
    {
        LOG.debug("[QualityManagerService] -> [deletePerformanceReport]: deleting performance report for plan id: [{}] requested from ivUser: [{}]", planId, ivUser);

        // Check permissions
        this.usersService.checkHasPermission(ivUser, QualityConstants.SET_PERFORMANCE_REPORT_PERMISSION, PERMISSION_DENIED);

        // Check plan exists
        DeploymentPlan plan = this.planRepository.findById(planId).orElseThrow(() -> {
            throw new NovaException(QualityManagerErrorCode.getInvalidPlanError(planId));
        });

        // Call Quality Assurance API
        this.qualityAssuranceClient.deletePerformanceReport(planId);

        // Finally add an entry to the plan history.
        this.deploymentUtils.addHistoryEntry(ChangeType.DELETE_DOCUMENTATION, plan, ivUser, "Eliminado informe de rendiemiento");
    }

    @Override
    public PlanPerformanceReport[] getPerformanceReports(final Integer releaseVersionId)
    {
        // Check release version exists
        this.releaseVersionRepository.findById(releaseVersionId).orElseThrow(() -> {
            throw new NovaException(QualityManagerErrorCode.getInvalidReleaseVersionError());
        });

        return this.qualityAssuranceClient.getPerformanceReports(releaseVersionId);
    }

    ///////////////////////////////// PRIVATE METHODS ////////////////////////////////////////////////////

    private String getEphoenixSonarProjectName(String module, String uuaa, TOSubsystemDTO subsystemDTO)
    {
        String subsystemNameInLowerCase = subsystemDTO.getSubsystemName().toLowerCase();
        String moduleName = QualityUtils.getModuleName(module);

        return MessageFormat.format("{0}_{1}_{2}", moduleName, uuaa.toUpperCase(), subsystemNameInLowerCase);
    }

    private String getNovaSonarProjectName(String serviceName, String uuaa, TOSubsystemDTO subsystemDTO)
    {
        String subsystemNameInLowerCase = subsystemDTO.getSubsystemName().toLowerCase();

        return MessageFormat.format("{0}_{1}_{2}", serviceName, uuaa.toUpperCase(), subsystemNameInLowerCase);
    }

    private void addProject(List<QAServiceDTO> projectList, NewReleaseVersionServiceDto serviceDto)
    {
        QAServiceDTO project = new QAServiceDTO();
        project.setName(serviceDto.getServiceName());
        project.setFolder(serviceDto.getFolder());
        project.setType(serviceDto.getServiceType());
        project.setVersion(serviceDto.getVersion());

        projectList.add(project);
    }

    private void buildDefaultOrder(List<NewReleaseVersionServiceDto> services, Integer repoId, String branchName, List<QAServiceDTO> projectList)
    {
        // Add dependencies ordered according dependencies specified in nova.yml
        Map<String, NewReleaseVersionServiceDto> dependencies = services.stream()
                .filter(it -> ServiceType.DEPENDENCY.getServiceType().equals(it.getServiceType()))
                .collect(Collectors.toMap(NewReleaseVersionServiceDto::getServiceName, it -> it));

        if (!dependencies.isEmpty())
        {
            Set<GraphNode> graph = new HashSet<>();
            for (NewReleaseVersionServiceDto service : dependencies.values())
            {
                List<String> dependenciesFromNovaYml = this.getDependenciesFromNovaYml(service.getFolder(), repoId, branchName);

                List<String> notFoundDependencies = dependenciesFromNovaYml.stream()
                        .filter(dependencyFromNovaYml -> !dependencies.containsKey(dependencyFromNovaYml))
                        .collect(Collectors.toList());

                if (!notFoundDependencies.isEmpty())
                {
                    throw new NovaException(ReleaseVersionError.getDependencyNotFoundError(),
                            "The following dependencies " + notFoundDependencies + " specified in the service " + service.getServiceName() + " doesn't exist in the subsystem");
                }

                graph.add(new GraphNode(service.getServiceName(), new HashSet<>(dependenciesFromNovaYml)));
            }

            try
            {
                List<String> orderedNodeNames = dependencyResolutionService.getDependencyResolutionOrder(graph);
                orderedNodeNames.stream().map(dependencies::get)
                        .forEachOrdered((service) -> this.addProject(projectList, service));
            }
            catch (IllegalArgumentException e)
            {
                LOG.error("Dependency tree forms a cycle", e);
                throw new NovaException(ReleaseVersionError.getDependencyGraphContainsCycleError(), e.getMessage());
            }
        }

        // Add rest of services
        services.stream().filter(service -> !ServiceType.DEPENDENCY.getServiceType().equals(service.getServiceType())).
                forEach(service -> this.addProject(projectList, service));
    }

    private List<String> getDependenciesFromNovaYml(String serviceFolder, Integer repoId, String branchName)
    {
        // Get nova.yml from service, repo and tag
        byte[] novaFile = this.versionControlSystemClient.getNovaYmlFromProject(serviceFolder, repoId, branchName);

        if (novaFile != null && (novaFile.length > 0))
        {
            try
            {
                Yaml yaml = new Yaml();
                String s = new String(novaFile);
                s = s.replace("@", "");

                Map<String, Object> map = new LinkedHashMap<>(yaml.load(s));

                List<String> dependencyList = (ArrayList<String>) map.get("dependencies");
                if (dependencyList != null)
                {
                    return dependencyList;
                }

            }
            catch (YAMLException | ClassCastException e)
            {
                LOG.error("There was and error parsing nova.yml from project path: [{}]", serviceFolder);
            }
        }

        return Collections.emptyList();
    }

    private void buildFromFile(List<String> dependencies, List<NewReleaseVersionServiceDto> services, List<QAServiceDTO> projectList)
    {
        for (String serviceFolder : dependencies)
        {
            NewReleaseVersionServiceDto serviceDto = this.findService(serviceFolder, services);
            if (serviceDto != null)
            {
                this.addProject(projectList, serviceDto);
            }
        }
    }

    private void buildNovaServices(List<NewReleaseVersionServiceDto> services, List<QAServiceDTO> projectList)
    {
        services.stream().filter(service -> !ServiceType.DEPENDENCY.getServiceType().equals(service.getServiceType())).
                forEach(service -> this.addProject(projectList, service));
    }

    /**
     * Check state
     *
     * @param state        state to check
     * @param defaultValue default value
     * @return new state
     */
    private String checkState(String state, String defaultValue)
    {
        String newState = defaultValue;
        if (!QualityConstants.SQA_OK.equals(state))
        {
            newState = QualityConstants.SQA_ERROR;
        }
        return newState;
    }


    private QAEphoenixSubsystemCodeAnalysisRequest createEphoenixSubsystemCodeAnalysisRequest(TOSubsystemDTO subsystem, String branchName, Product product)
    {

        QAEphoenixSubsystemCodeAnalysisRequest ephoenixRequest = new QAEphoenixSubsystemCodeAnalysisRequest();

        ephoenixRequest.setBranchName(branchName);
        ephoenixRequest.setSubsystemName(subsystem.getSubsystemName());
        ephoenixRequest.setSubsystemType(subsystem.getSubsystemType());
        ephoenixRequest.setUuaa(product.getUuaa());
        ephoenixRequest.setSonarQualityGate(QualityUtils.getSonarQualityGate(product.getQualityLevel()));

        QAEphoenixJobParametersDTO ephoenixJobParameters = new QAEphoenixJobParametersDTO();

        List<QAEphoenixJobParameter> commonJobParameterList = new ArrayList<>();
        commonJobParameterList.add(this.createJobParameterInstance(Constants.PROJECT_UUAA_PARAM, product.getUuaa()));
        commonJobParameterList.add(this.createJobParameterInstance(Constants.SUBSYSTEM_NAME_PARAM, subsystem.getSubsystemName()));
        commonJobParameterList.add(this.createJobParameterInstance(Constants.TAG_PARAM, branchName));
        commonJobParameterList.add(this.createJobParameterInstance(Constants.RELEASE_NAME_PARAM, QualityConstants.SQA));
        commonJobParameterList.add(this.createJobParameterInstance(Constants.RELEASE_VERSION_PARAM, QualityConstants.SQA));

        QAEphoenixJobParameter[] commonJobParameterArray = commonJobParameterList.toArray(new QAEphoenixJobParameter[0]);
        ephoenixJobParameters.setJobParameters(commonJobParameterArray);


        List<NewReleaseVersionServiceDto> serviceDtoList = this.recoverListOfServices(subsystem.getRepoId(), branchName,
                SubsystemType.EPHOENIX, product, subsystem.getSubsystemName());

        List<QAEphoenixServiceDTO> ephoenixServiceList = new ArrayList<>();
        List<QAServiceDTO> serviceList = new ArrayList<>();

        for (NewReleaseVersionServiceDto service : serviceDtoList)
        {
            LOG.debug("[QualityManagerService] -> [createEphoenixSubsystemCodeAnalysisRequest]: adding the service name [{}]", service.getServiceName());

            QAEphoenixServiceDTO ephoenixService = new QAEphoenixServiceDTO();
            List<QAEphoenixJobParameter> ephoenixServiceParametersList = new ArrayList<>();

            ephoenixServiceParametersList.add(this.createJobParameterInstance(Constants.INSTANCE_NAME_PARAM, service.getArtifactId()));
            ephoenixServiceParametersList.add(this.createJobParameterInstance(Constants.INSTANCE_TYPE_PARAM, service.getServiceType().toUpperCase()));
            ephoenixServiceParametersList.add(this.createJobParameterInstance(Constants.IMAGE_NAME_PARAM, QualityConstants.SQA));
            ephoenixServiceParametersList.add(this.createJobParameterInstance(Constants.INSTANCE_UUAA_PARAM, product.getUuaa()));

            // Create the ePhoenix service parameters array
            QAEphoenixJobParameter[] ephoenixServiceParametersArray = ephoenixServiceParametersList.toArray(new QAEphoenixJobParameter[0]);
            ephoenixService.setJobParameters(ephoenixServiceParametersArray);

            LOG.debug("[QualityManagerService] -> [createEphoenixSubsystemCodeAnalysisRequest]: added the ephoenix service with the our job parameters: [{}]", Arrays.toString(ephoenixServiceParametersArray));

            ephoenixServiceList.add(ephoenixService);

            // Add module list
            for (String module : service.getModules())
            {
                QAServiceDTO moduleDTO = new QAServiceDTO();
                moduleDTO.setName(QualityUtils.getModuleName(module));
                moduleDTO.setFolder(service.getFolder());
                moduleDTO.setVersion(service.getVersion());
                moduleDTO.setType(service.getServiceType());

                serviceList.add(moduleDTO);
            }
        }

        QAEphoenixServiceDTO[] ephoenixServiceArray = ephoenixServiceList.toArray(new QAEphoenixServiceDTO[0]);
        ephoenixJobParameters.setEphoenixService(ephoenixServiceArray);

        ephoenixRequest.setJobParameters(ephoenixJobParameters);

        ephoenixRequest.setServices(serviceList.toArray(new QAServiceDTO[0]));

        return ephoenixRequest;
    }

    /**
     * Creates a new Job Parameter instance
     *
     * @param name  name of the parameter
     * @param value value of the parameter
     * @return a new Job parameter
     */
    private QAEphoenixJobParameter createJobParameterInstance(final String name, final String value)
    {
        QAEphoenixJobParameter jobParameter = new QAEphoenixJobParameter();
        jobParameter.setKey(name);
        jobParameter.setValue(value);

        LOG.debug("[QualityManagerService] -> [createJobParameter]: a new job parameter was created: [{}]", jobParameter);

        return jobParameter;
    }

    /**
     * Find service
     *
     * @param serviceFolder service folder
     * @param services      list of services
     * @return release version service
     */
    private NewReleaseVersionServiceDto findService(String serviceFolder, List<NewReleaseVersionServiceDto> services)
    {
        NewReleaseVersionServiceDto response = null;
        for (NewReleaseVersionServiceDto service : services)
        {
            if (serviceFolder.equals(service.getFolder()))
            {
                response = service;
                break;
            }
        }

        LOG.trace("[QualityManagerService] -> [findService]: the found service: [{}]", response);

        return response;
    }

    private QAServiceDTO[] getProjectList(List<NewReleaseVersionServiceDto> services, final Integer repoId, final String branchName)
    {
        List<QAServiceDTO> projectList = new ArrayList<>();

        // Get dependencies from dependencies.txt file
        List<String> dependencies = this.versionControlSystemClient.getDependencies(repoId, branchName);

        if (dependencies.isEmpty())
        {
            this.buildDefaultOrder(services, repoId, branchName, projectList);
        }
        else
        {
            // Add dependencies from dependencies.txt
            this.buildFromFile(dependencies, services, projectList);

            // Add the rest of services
            this.buildNovaServices(services, projectList);
        }

        LOG.trace("[QualityManagerService] -> [getProjectList]: the project list: [{}]", projectList);

        return projectList.toArray(new QAServiceDTO[0]);
    }

    /**
     * Get service state
     *
     * @param analysisArray analysis array
     * @return service state
     */
    private String getServiceState(CodeAnalysis[] analysisArray)
    {
        LOG.trace("[QualityManagerService] -> [getServiceState]: the analysis array to get status: [{}]", Arrays.toString(analysisArray));

        String sqaServiceState = QualityConstants.SQA_OK;
        for (CodeAnalysis analysis : analysisArray)
        {
            LOG.debug("[QualityManagerService] -> [getServiceState]: the QA analysis state is: [{}]", analysis.getSqaState());
            sqaServiceState = this.checkState(analysis.getSqaState(), sqaServiceState);
        }
        // If there are no analysis, return error
        if (analysisArray.length == 0)
        {
            LOG.warn("[QualityManagerService] -> [getServiceState]: there are no analysis, return SQA_ERROR");
            sqaServiceState = QualityConstants.SQA_ERROR;
        }
        return sqaServiceState;
    }

    /**
     * Get releaseVersionSubsystem state
     *
     * @param releaseVersionSubsystem releaseVersionSubsystem
     * @param qualityServiceList      service list
     * @return releaseVersionSubsystem state
     */
    private String getSubsystemState(final ReleaseVersionSubsystem releaseVersionSubsystem, final List<ServiceCodeAnalyses> qualityServiceList)
    {
        String sqaSubsystemState = QualityConstants.SQA_OK;

        for (ReleaseVersionService releaseVersionService : releaseVersionSubsystem.getServices())
        {
            LOG.debug("[QualityManagerService] -> [getSubsystemState]: the service type is [{}] of release version service artifactId: [{}]", releaseVersionService.getServiceType(), releaseVersionService.getArtifactId());

            if (ServiceType.valueOf(releaseVersionService.getServiceType()).isEPhoenix())
            {
                // Ephoenix
                CodeAnalysis[] analysisArray = this.qualityAssuranceClient.getCodeAnalysesByReleaseVersionService(releaseVersionService.getId());
                String uuaa = releaseVersionSubsystem.getReleaseVersion().getRelease().getProduct().getUuaa();

                for (CodeAnalysis analysis : analysisArray)
                {
                    ServiceCodeAnalyses qualityService = new ServiceCodeAnalyses();
                    String moduleName = analysis.getProjectName().split("_" + uuaa + "_")[0];

                    qualityService.setServiceName(moduleName);
                    qualityService.setServiceVersion(releaseVersionService.getVersion());
                    qualityService.setServiceType(releaseVersionService.getServiceType());
                    qualityService.setAnalysis(new CodeAnalysis[]{analysis});
                    String sqaModuleState = analysis.getSqaState();
                    qualityService.setSqaState(sqaModuleState);

                    sqaSubsystemState = this.checkState(sqaModuleState, sqaSubsystemState);
                    qualityServiceList.add(qualityService);
                }
            }
            else
            {
                //NOVA
                ServiceCodeAnalyses qualityService = new ServiceCodeAnalyses();
                qualityService.setServiceName(releaseVersionService.getServiceName());
                qualityService.setServiceVersion(releaseVersionService.getVersion());
                qualityService.setServiceType(releaseVersionService.getServiceType());

                String sqaServiceState;

                if (!QualityUtils.hasQualityAnalysis(ServiceType.valueOf(releaseVersionService.getServiceType())))
                {
                    LOG.debug("[QualityManagerService] -> [getSubsystemState]: Service of type [{}] with ID [{}] doesn't have quality", releaseVersionService.getServiceType(), releaseVersionService.getId());
                    sqaServiceState = QualityConstants.SQA_OK;
                    qualityService.setAnalysis(new CodeAnalysis[0]);
                }
                else
                {
                    CodeAnalysis[] analysisArray = this.qualityAssuranceClient.getCodeAnalysesByReleaseVersionService(releaseVersionService.getId());

                    qualityService.setAnalysis(analysisArray);
                    sqaServiceState = this.getServiceState(analysisArray);
                    LOG.debug("[QualityManagerService] -> [getSubsystemState]: SQA service State [{}]", sqaServiceState);
                }
                qualityService.setSqaState(sqaServiceState);
                sqaSubsystemState = this.checkState(sqaServiceState, sqaSubsystemState);
                qualityServiceList.add(qualityService);
            }

        }
        return sqaSubsystemState;
    }

    private List<NewReleaseVersionServiceDto> recoverListOfServices(Integer repositoryId, String branchName, SubsystemType subsystemType, Product product, String subsystemName)
    {
        SubsystemTagDto tagDto = new SubsystemTagDto();
        tagDto.setTagName(branchName);
        tagDto.setValidationErrors(new ValidationErrorDto[0]);
        tagDto.setTagUrl(branchName);

        return this.serviceDtoBuilder.buildServicesFromSubsystemTag(repositoryId, tagDto, subsystemType, Constants.IMMUSER, QualityConstants.SQA, product, subsystemName);
    }

    private QAServiceDTO[] retrieveListOfNovaProjects(Integer repositoryId, String branchName, SubsystemType subsystemType, Product product, String subsystemName)
    {
        List<NewReleaseVersionServiceDto> serviceDtoList = this.recoverListOfServices(repositoryId, branchName, subsystemType, product, subsystemName);

        // Remove services that do not need or has quality analysis yet
        List<NewReleaseVersionServiceDto> filteredReleaseVersionServiceDto = serviceDtoList.stream()
                .filter(serviceDto -> QualityUtils.hasQualityAnalysis(ServiceType.getValueOf(serviceDto.getServiceType())))
                .collect(Collectors.toList());

        // Check all services are not filtered
        if (filteredReleaseVersionServiceDto.isEmpty())
        {
            throw new NovaException(QualityManagerErrorCode.getAllServiceTypesHaveNoQualityError());
        }

        // Check validation errors in services
        checkValidationErrors(filteredReleaseVersionServiceDto);

        return this.getProjectList(filteredReleaseVersionServiceDto, repositoryId, branchName);
    }

    private void checkValidationErrors(List<NewReleaseVersionServiceDto> serviceDtoList)
    {
        boolean hasErrors = serviceDtoList.stream()
                .anyMatch(serviceDto -> serviceDto.getValidationErrors().length > 0);

        if (hasErrors)
        {
            String errorsMessage = serviceDtoList.stream()
                    .filter(serviceDto -> serviceDto.getValidationErrors().length > 0)
                    .map(this::getServiceErrors)
                    .collect(Collectors.joining(". "));

            throw new NovaException(QualityManagerErrorCode.getServiceValidationError(errorsMessage));
        }
    }

    private String getServiceErrors(NewReleaseVersionServiceDto serviceDto)
    {
        String errorList = Arrays.stream(serviceDto.getValidationErrors())
                .map(ValidationErrorDto::getMessage)
                .collect(Collectors.joining("; "));

        return "In folder " + serviceDto.getFolder() + ": " + errorList;
    }


    /**
     * Encode a branch name that comes with a + character, since it is decoded as a white space.
     * To get back the + character
     *
     * @param branchName branch name
     * @return encoded branch name
     */
    private String validateAndEncodeBranchName(String branchName)
    {
        Pattern p = Pattern.compile("\\s");
        Matcher m = p.matcher(branchName);

        if (m.find())
        {
            try
            {
                branchName = URLEncoder.encode(branchName, StandardCharsets.UTF_8.toString());
            }
            catch (UnsupportedEncodingException e)
            {
                LOG.error("ERROR encoding branchName: [{}]", branchName, e);
                throw new NovaException(QualityManagerErrorCode.getUnexpectedError());
            }
        }

        return branchName;
    }


}


