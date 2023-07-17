package com.bbva.enoa.platformservices.coreservice.consumers.dummy.data;

import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMPoliciesDTO;
import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMPoliciesResponseDTO;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.BatchManagerBatchExecutionDTO;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.BatchManagerBatchExecutionsSummaryDTO;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.DeploymentServiceBatchStatus;
import com.bbva.enoa.apirestgen.batchmanagerapi.model.RunningBatchs;
import com.bbva.enoa.apirestgen.configurationmanagerapi.model.CMLogicalConnectorPropertyDto;
import com.bbva.enoa.apirestgen.continuousintegrationapi.model.CIJenkinsBuildSnapshotDTO;
import com.bbva.enoa.apirestgen.continuousintegrationapi.model.CIJobDTO;
import com.bbva.enoa.apirestgen.continuousintegrationapi.model.CIMultiResponseDTO;
import com.bbva.enoa.apirestgen.continuousintegrationapi.model.CIResponseDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.CallbackDto;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.CallbackInfoDto;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployInstanceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployServiceStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeployStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.DeploySubsystemStatusDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.HostMemoryHistoryItemDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.HostMemoryInfo;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherDeploymentDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherManagerConfigStatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherServiceDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherServiceStatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherStatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherSubsystemStatusDTO;
import com.bbva.enoa.apirestgen.filesystemmanagerapi.model.FSMFileModel;
import com.bbva.enoa.apirestgen.filesystemmanagerapi.model.FSMFilesystemUsage;
import com.bbva.enoa.apirestgen.filesystemmanagerapi.model.FSMUsageReportDTO;
import com.bbva.enoa.apirestgen.filesystemmanagerapi.model.FSMUsageReportHistorySnapshotDTO;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemsUsageReportDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransferConfigHistorySnapshotDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransferConfigStatisticsDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransferConfigsStatisticsSummaryDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransferInstanceStatusStatisticsDTO;
import com.bbva.enoa.apirestgen.filetransferstatisticsapi.model.FTMFileTransfersInstancesStatisticsSummaryDTO;
import com.bbva.enoa.apirestgen.issuetrackerapi.model.IssueTrackerItem;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsByServiceDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryValidationErrorDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsageDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsedLibrariesDTO;
import com.bbva.enoa.apirestgen.qualityassuranceapi.model.QACodeAnalysis;
import com.bbva.enoa.apirestgen.qualityassuranceapi.model.QAIC;
import com.bbva.enoa.apirestgen.qualityassuranceapi.model.QAICDetail;
import com.bbva.enoa.apirestgen.qualityassuranceapi.model.QAPerformanceReport;
import com.bbva.enoa.apirestgen.qualityassuranceapi.model.QAPlanPerformanceReport;
import com.bbva.enoa.apirestgen.qualityassuranceapi.model.QASubsystemCodeAnalysis;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.CodeAnalysis;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.CodeAnalysisIndicatorDetail;
import com.bbva.enoa.apirestgen.qualitymanagerapi.model.CodeAnalysisIndicators;
import com.bbva.enoa.apirestgen.schedulecontrolmapi.model.ScheduleRequest;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentBatchScheduleInstanceDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentContextParamsHistoricInstanceDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentContextParamsInstanceDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentJobsInstanceDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DeploymentStepInstanceDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DisabledDateDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.DisabledDateExceptionDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.PendingTaskSchedule;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.SMBatchSchedulerExecutionDTO;
import com.bbva.enoa.apirestgen.schedulermanagerapi.model.SMBatchSchedulerExecutionsSummaryDTO;
import com.bbva.enoa.apirestgen.statisticsuserapi.model.TeamCountDTO;
import com.bbva.enoa.apirestgen.statisticsuserapi.model.UserProductRoleHistoryDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskCreationDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskFilterResponseDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.ApiTaskKeyDTO;
import com.bbva.enoa.apirestgen.todotaskapi.model.TaskSummary;
import com.bbva.enoa.apirestgen.todotaskapi.model.TaskSummaryList;
import com.bbva.enoa.apirestgen.todotaskapi.model.TaskUser;
import com.bbva.enoa.apirestgen.toolsapi.model.TOProductUserDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemsCombinationDTO;
import com.bbva.enoa.apirestgen.usersadminapi.model.UATeamUser;
import com.bbva.enoa.apirestgen.usersapi.model.USProductUserDTO;
import com.bbva.enoa.apirestgen.usersapi.model.USUserDTO;
import com.bbva.enoa.apirestgen.versioncontrolsystemapi.model.VCSTag;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationRevision;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentNova;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentPriority;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.EphoenixService;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.resource.entities.FilesystemPack;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaError;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessage;
import com.bbva.kltt.apirest.generator.lib.commons.exception.ErrorMessageType;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DummyConsumerDataGenerator
{
    private DummyConsumerDataGenerator()
    {
    }

    public static Errors getDummyErrors()
    {
        Errors errors = new Errors();
        ErrorMessage errorMessage = getDummyErrorMessage();
        errors.setMessages(Collections.singletonList(errorMessage));
        return errors;
    }

    public static Errors getDummyErrors(int status)
    {
        Errors errors = new Errors(status);
        ErrorMessage errorMessage = getDummyErrorMessage();
        errors.setMessages(Collections.singletonList(errorMessage));
        return errors;
    }

    private static ErrorMessage getDummyErrorMessage()
    {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage("Error");
        errorMessage.setCode("AAA-100");
        errorMessage.setType(ErrorMessageType.ERROR);
        return errorMessage;
    }

    public static AGMPoliciesResponseDTO getDummyAGMPoliciesResponseDto()
    {
        AGMPoliciesResponseDTO dto = new AGMPoliciesResponseDTO();
        AGMPoliciesDTO agmPolicies = new AGMPoliciesDTO();
        agmPolicies.setPolicies(new String[]{"A", "B"});
        agmPolicies.setEnvironmnet("A");
        dto.setPolicies(new AGMPoliciesDTO[]{agmPolicies});
        return dto;
    }

    public static DeploymentPlan getDummyDeploymentPlan()
    {
        DeploymentPlan item = new DeploymentPlan();
        item.setId(1);
        ConfigurationRevision configurationRevision = new ConfigurationRevision();
        configurationRevision.setId(1);
        item.setCurrentRevision(configurationRevision);
        item.setNova(getDummyDeploymentNova());
        item.setEnvironment(Environment.INT.getEnvironment());
        return item;
    }

    private static DeploymentNova getDummyDeploymentNova()
    {
        DeploymentNova item = new DeploymentNova();
        item.setBatch(4);
        item.setDeploymentList("1,2,3");
        item.setPriorityLevel(DeploymentPriority.PRODUCT);
        item.setUndeployRelease(2);
        item.setDeploymentDateTime(new Date());
        return item;
    }

    public static List<LMUsedLibrariesDTO> getDummyLibraries()
    {
        LMUsedLibrariesDTO dto = new LMUsedLibrariesDTO();
        dto.setUsage("A");
        dto.setFullName("A");
        dto.setInte(true);
        dto.setPre(true);
        dto.setPro(true);
        dto.setReleaseVersionServiceId(1);
        return Collections.singletonList(dto);
    }

    public static CIJenkinsBuildSnapshotDTO[] getDummyBuildSnapshots()
    {
        CIJenkinsBuildSnapshotDTO dto = new CIJenkinsBuildSnapshotDTO();
        dto.setUuaa("UUAA");
        dto.setStatus("Status");
        dto.setBuildType("BuildType");
        dto.setValue(1);
        return new CIJenkinsBuildSnapshotDTO[]{dto};
    }

    public static CIJobDTO[] getDummyCIJobsDtoArray()
    {
        CIJobDTO dto = new CIJobDTO();
        dto.setId(1);
        dto.setStatus("Status");
        return new CIJobDTO[]{dto};
    }

    public static ReleaseVersion getDummyReleaseVersion()
    {
        ReleaseVersion item = new ReleaseVersion();
        item.setVersionName("VersionName");
        item.setSubsystems(getDummySubsystems());
        item.setRelease(getDummyRelease());
        return item;
    }

    public static List<ReleaseVersionSubsystem> getDummySubsystems()
    {
        List<ReleaseVersionSubsystem> items = new ArrayList<>();
        ReleaseVersionSubsystem item = new ReleaseVersionSubsystem();
        item.setId(1);
        item.setSubsystemId(1);
        item.setTagName("TAG");
        item.setTagUrl("http://mytag.com");
        item.setServices(Collections.singletonList(getDummyReleaseVersionService()));
        items.add(item);
        return items;
    }

    public static Release getDummyRelease()
    {
        Release item = new Release();
        item.setName("Release");
        item.setProduct(getDummyProduct());
        return item;
    }

    private static Product getDummyProduct() {
        Product item = new Product();
        item.setId(1);
        item.setUuaa("UUAA");
        return item;
    }

    public static ReleaseVersionService getDummyReleaseVersionService()
    {
        ReleaseVersionService item = new ReleaseVersionService();
        item.setId(1);
        item.setHasForceCompilation(Boolean.TRUE);
        item.setServiceType(ServiceType.API_JAVA_SPRING_BOOT.getServiceType());
        item.setFolder("FOLDER");
        item.setImageName("IMAGE");
        item.setFinalName("FINAL_NAME");
        item.setEphoenixData(getDummyEphoenixService());
        return item;
    }

    public static EphoenixService getDummyEphoenixService()
    {
        EphoenixService item = new EphoenixService();
        item.setUuaas(List.of("AAAA", "BBBB", "CCCC"));
        item.setInstanceUuaa("AAAA");
        return item;
    }

    public static TOSubsystemDTO getDummyTOSubystemDTO(SubsystemType subsystemType)
    {
        TOSubsystemDTO dto = new TOSubsystemDTO();
        dto.setSubsystemType(subsystemType.getType());
        dto.setSubsystemName("NAME");
        dto.setRepoId(1);
        return dto;
    }

    public static NovaException getDummyNovaException()
    {
        final NovaError novaError = new NovaError("ProductsError", "ERROR-000", "Error in invocation to ProductTools Api", "action", HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessageType.ERROR);
        return new NovaException(novaError);
    }

    public static CIResponseDTO getDummyCIResponseDTO(final String message, final String value)
    {
        CIResponseDTO dto = new CIResponseDTO();
        dto.setMessage(message);
        dto.setValue(value);
        return dto;
    }

    public static CIMultiResponseDTO getDummyCIMultiResponseDTO(final String[] values)
    {
        CIMultiResponseDTO dto = new CIMultiResponseDTO();
        dto.setMessage("Message");
        dto.setValues(values);
        return dto;
    }

    public static DeployServiceStatusDTO getDummyDeployServiceStatusDto()
    {
        DeployServiceStatusDTO dto = new DeployServiceStatusDTO();
        dto.setServiceType("ServiceType");
        dto.setServiceId(1);
        dto.setServiceName("ServiceName");
        dto.setStatus(getDummyDeployStatusDtoForService());
        dto.setInstances(getDummyDeployInstanceStatusDtos());
        return dto;
    }

    public static DeployStatusDTO getDummyDeployStatusDtoForService()
    {
        DeployStatusDTO dto = new DeployStatusDTO();
        dto.setRunning(1);
        dto.setTotal(2);
        dto.setExited(1);
        return dto;
    }

    public static DeployInstanceStatusDTO[] getDummyDeployInstanceStatusDtos()
    {
        DeployInstanceStatusDTO dto = new DeployInstanceStatusDTO();
        dto.setStatus(getDummyDeployStatusDtoForInstance());
        dto.setInstanceId(1);
        return new DeployInstanceStatusDTO[]{dto};
    }

    public static DeployStatusDTO getDummyDeployStatusDtoForInstance()
    {
        DeployStatusDTO dto = new DeployStatusDTO();
        dto.setRunning(0);
        dto.setTotal(1);
        dto.setExited(1);
        return dto;
    }

    public static DeploySubsystemStatusDTO[] getDummyDeploySubsystemStatusDtos()
    {
        DeploySubsystemStatusDTO dto = new DeploySubsystemStatusDTO();
        dto.setSubsystemId(1);
        dto.setStatus(getDummyDeployStatusDtoForSubsystem());
        dto.setServices(new DeployServiceStatusDTO[]{getDummyDeployServiceStatusDto()});
        return new DeploySubsystemStatusDTO[]{dto};
    }

    public static DeployStatusDTO getDummyDeployStatusDtoForSubsystem()
    {
        DeployStatusDTO dto = new DeployStatusDTO();
        dto.setRunning(2);
        dto.setTotal(4);
        dto.setExited(2);
        return dto;
    }

    public static HostMemoryHistoryItemDTO[] getDummyHostMemoryHistoryItemDtos()
    {
        HostMemoryHistoryItemDTO dto = new HostMemoryHistoryItemDTO();
        dto.setCpd("CPD");
        dto.setEnvironment("INT");
        dto.setUnit("UNIT");
        dto.setValue(10D);
        return new HostMemoryHistoryItemDTO[]{dto};
    }

    public static CallbackInfoDto getDummyCallbackInfoDto()
    {
        CallbackInfoDto dto = new CallbackInfoDto();
        dto.setSuccessCallback(new CallbackDto());
        dto.setErrorCallback(new CallbackDto());
        return dto;
    }

    public static EtherDeploymentDTO getDummyEtherDeploymentDto()
    {
        EtherDeploymentDTO dto = new EtherDeploymentDTO();
        dto.setEtherServices(getDummyEtherServiceDtos());
        return dto;
    }

    public static EtherServiceDTO[] getDummyEtherServiceDtos()
    {
        EtherServiceDTO dto = new EtherServiceDTO();
        dto.setDeploymentServiceId(1);
        return new EtherServiceDTO[]{dto};
    }

    public static EtherSubsystemStatusDTO[] getDummyEtherSubsystemStatusDtos()
    {
        EtherSubsystemStatusDTO dto = new EtherSubsystemStatusDTO();
        dto.setSubsystemId(1);
        dto.setStatus(getDummyEtherStatusDto());
        dto.setServices(getDummyEtherServiceStatusDtos());
        return new EtherSubsystemStatusDTO[]{dto};
    }

    public static EtherServiceStatusDTO[] getDummyEtherServiceStatusDtos()
    {
        EtherServiceStatusDTO dto = new EtherServiceStatusDTO();
        dto.setStatus(getDummyEtherStatusDto());
        dto.setServiceId(1);
        return new EtherServiceStatusDTO[]{dto};
    }

    public static EtherStatusDTO getDummyEtherStatusDto()
    {
        EtherStatusDTO dto = new EtherStatusDTO();
        dto.setTotalContainers(3);
        dto.setRunningContainers(1);
        return dto;
    }

    public static EtherManagerConfigStatusDTO getDummyEtherManagerConfigStatusDto()
    {
        EtherManagerConfigStatusDTO dto = new EtherManagerConfigStatusDTO();
        dto.setStatus("Status");
        dto.setGroupId("GroupId");
        dto.setNamespace("NameSpace");
        dto.setErrorMessage(new String[]{"ErrorMessage"});
        return dto;
    }

    public static Filesystem getDummyFilesystem()
    {
        Filesystem item = new FilesystemNova();
        item.setId(1);
        Product product = new Product();
        product.setId(1);
        item.setProduct(product);
        item.setEnvironment(Environment.INT.getEnvironment());
        FilesystemPack filesystemPack = new FilesystemPack();
        filesystemPack.setCode("AAA");
        item.setFilesystemPack(filesystemPack);
        return item;
    }

    public static FSMFileModel[] getDummyFSMFileModels()
    {
        FSMFileModel item = new FSMFileModel();
        item.setUser("USER");
        item.setCreationDate("NOW");
        item.setGroup("GROUP");
        item.setIsFolder(Boolean.FALSE);
        item.setPermissions("PERMISSIONS");
        item.setSize(1024L);
        item.setFilename("FILENAME");
        item.setPath("PATH");
        return new FSMFileModel[]{item};
    }

    public static FSMFilesystemUsage getDummyFSMFilesystemUsage()
    {
        FSMFilesystemUsage item = new FSMFilesystemUsage();
        item.setSize("SIZE");
        item.setAvailable("AVAILABLE");
        item.setUsagePercentage(25);
        item.setUsed("USED");
        return item;
    }

    public static FSMUsageReportDTO getDummyFSMUsageReportDTO()
    {
        FSMUsageReportDTO dto = new FSMUsageReportDTO();
        dto.setTotalStorageAssigned(100D);
        dto.setTotalStorageAssignedPercentage(50D);
        dto.setTotalStorageAvailable(200D);
        dto.setTotalStorageAvailablePercentage(50D);
        return dto;
    }

    public static FSMUsageReportHistorySnapshotDTO[] getDummyFSMUsageReportHistorySnapshotDtos()
    {
        FSMUsageReportHistorySnapshotDTO dto = new FSMUsageReportHistorySnapshotDTO();
        dto.setEnvironment("INT");
        dto.setValue(10D);
        dto.setProperty("PROPERTY");
        dto.setUuaa("UUAA");
        return new FSMUsageReportHistorySnapshotDTO[]{dto};
    }

    public static FilesystemsUsageReportDTO getDummyFilesystemsUsageReportDTO()
    {
        FilesystemsUsageReportDTO dto = new FilesystemsUsageReportDTO();
        dto.setTotalStorageAssigned(100D);
        dto.setTotalStorageAssignedPercentage(50D);
        dto.setTotalStorageAvailable(200D);
        dto.setTotalStorageAvailablePercentage(50D);
        return dto;
    }

    public static FTMFileTransferConfigsStatisticsSummaryDTO getDummyFTMFileTransferConfigsStatisticsSummaryDTO()
    {
        FTMFileTransferConfigsStatisticsSummaryDTO dto = new FTMFileTransferConfigsStatisticsSummaryDTO();
        dto.setTotal(1L);
        dto.setElements(getDummyFTMFileTransferConfigStatisticsDtos());
        return dto;
    }

    public static FTMFileTransferConfigStatisticsDTO[] getDummyFTMFileTransferConfigStatisticsDtos()
    {
        FTMFileTransferConfigStatisticsDTO dto = new FTMFileTransferConfigStatisticsDTO();
        dto.setTotal(100L);
        dto.setStatus("Status");
        return new FTMFileTransferConfigStatisticsDTO[]{dto};
    }

    public static FTMFileTransfersInstancesStatisticsSummaryDTO getDummyFTMFileTransfersInstancesStatisticsSummaryDTO()
    {
        FTMFileTransfersInstancesStatisticsSummaryDTO dto = new FTMFileTransfersInstancesStatisticsSummaryDTO();
        dto.setTotal(8L);
        dto.setGigabytesTransfered(10D);
        dto.setNumTransferedFiles(5L);
        dto.setStatuses(getDummyFTMFileTransferInstanceStatusStatisticsDtos());
        return dto;
    }

    public static FTMFileTransferInstanceStatusStatisticsDTO[] getDummyFTMFileTransferInstanceStatusStatisticsDtos()
    {
        FTMFileTransferInstanceStatusStatisticsDTO dto = new FTMFileTransferInstanceStatusStatisticsDTO();
        dto.setStatus("Status");
        dto.setTotal(10L);
        return new FTMFileTransferInstanceStatusStatisticsDTO[]{dto};
    }

    public static FTMFileTransferConfigHistorySnapshotDTO[] getDummyFTMFileTransferConfigHistorySnapshotDtos()
    {
        FTMFileTransferConfigHistorySnapshotDTO dto = new FTMFileTransferConfigHistorySnapshotDTO();
        dto.setEnvironment("INT");
        dto.setStatus("STATUS");
        dto.setValue(10);
        dto.setUuaa("UUAA");
        return new FTMFileTransferConfigHistorySnapshotDTO[]{dto};
    }

    public static IssueTrackerItem[] getDummyIssueTrackerItems()
    {
        IssueTrackerItem item = new IssueTrackerItem();
        item.setProjectKey("projectkey");
        return new IssueTrackerItem[]{item};
    }

    public static USUserDTO getDummyUserDto()
    {
        USUserDTO dto = new USUserDTO();
        dto.setUserName("userName");
        dto.setSurname1("surname");
        dto.setSurname2("surname2");
        return dto;
    }

    public static USUserDTO[] getDummyUSUserDtos()
    {
        USUserDTO dto = new USUserDTO();
        dto.setUserName("USERNAME");
        dto.setSurname1("SURNAME1");
        dto.setSurname2("SURNAME2");
        dto.setActive(Boolean.TRUE);
        dto.setUserCode("USERCODE");
        dto.setEmail("EMAIL");
        dto.setTeams(new String[]{"TEAM1", "TEAM2"});
        return new USUserDTO[]{dto};
    }

    public static USProductUserDTO getDummyProductUserDto()
    {
        USProductUserDTO dto = new USProductUserDTO();
        dto.setUserCode("USER");
        dto.setTeamCode("TEAM");
        return dto;
    }

    public static QAPlanPerformanceReport[] getDummyQAPlanPerformanceReports()
    {
        QAPlanPerformanceReport item = new QAPlanPerformanceReport();
        item.setPlanId(1);
        item.setPerformanceReport(getDummyQAPerformanceReport());
        return new QAPlanPerformanceReport[]{item};
    }

    public static QAPerformanceReport getDummyQAPerformanceReport()
    {
        QAPerformanceReport item = new QAPerformanceReport();
        item.setCreationDate(1000L);
        item.setDescription("DESCRIPTION");
        item.setLink("LINK");
        item.setRiskLevel(1);
        return item;
    }

    public static QACodeAnalysis[] getDummyQACodeAnalysisArray()
    {
        QACodeAnalysis item = new QACodeAnalysis();
        item.setId(1);
        item.setAnalysisDate(1000L);
        item.setServiceType("SERVICE_TYPE");
        item.setServiceName("SERVICE_NAME");
        item.setLanguage("LANGUAGE");
        item.setHasSonarReport(Boolean.TRUE);
        item.setIndicatorsState(getDummyQAIC());
        item.setProjectId(1);
        item.setProjectName("PROJECT_NAME");
        item.setProjectUrl("PRJECT_URL");
        item.setServiceVersion("SERVICE_VERSION");
        item.setSonarState("SONAR_STATE");
        item.setSqaState("SQA_STATE");
        return new QACodeAnalysis[]{item};
    }

    private static QAIC getDummyQAIC()
    {
        QAIC item = new QAIC();
        item.setIccm(getDummyQAICDetail());
        item.setIcdc(getDummyQAICDetail());
        item.setIcpu(getDummyQAICDetail());
        item.setIcr(getDummyQAICDetail());
        return item;
    }

    private static QAICDetail getDummyQAICDetail()
    {
        QAICDetail item = new QAICDetail();
        item.setValue("VALUE");
        item.setLimit("LIMIT");
        return item;
    }

    public static CodeAnalysis[] convertToCodeAnalysisArray(final QACodeAnalysis[] qaArray)
    {
        List<CodeAnalysis> qualityAnalysisList = new ArrayList<>();
        if (qaArray != null)
        {
            for (QACodeAnalysis qaCodeAnalysis : qaArray)
            {
                CodeAnalysis qualityAnalysis = new CodeAnalysis();

                qualityAnalysis.setId(qaCodeAnalysis.getId());
                qualityAnalysis.setSqaState(qaCodeAnalysis.getSqaState());
                qualityAnalysis.setSonarVersion(qaCodeAnalysis.getSonarVersion());
                qualityAnalysis.setAnalysisDate(qaCodeAnalysis.getAnalysisDate());
                qualityAnalysis.setProjectId(qaCodeAnalysis.getProjectId());
                qualityAnalysis.setProjectName(qaCodeAnalysis.getProjectName());
                qualityAnalysis.setLanguage(qaCodeAnalysis.getLanguage());
                qualityAnalysis.setProjectUrl(qaCodeAnalysis.getProjectUrl());
                qualityAnalysis.setSonarState(qaCodeAnalysis.getSonarState());
                qualityAnalysis.setIndicatorsState(convertQAIndicatorsState(qaCodeAnalysis.getIndicatorsState()));
                qualityAnalysis.setHasSonarReport(qaCodeAnalysis.getHasSonarReport());

                qualityAnalysisList.add(qualityAnalysis);
            }
        }

        return qualityAnalysisList.toArray(new CodeAnalysis[0]);
    }

    private static CodeAnalysisIndicators convertQAIndicatorsState(QAIC qaIndicatorsState)
    {
        if (qaIndicatorsState == null)
        {
            return new CodeAnalysisIndicators();
        }

        CodeAnalysisIndicators indicators = new CodeAnalysisIndicators();
        indicators.setIccm(convertQAIndicatorDetail(qaIndicatorsState.getIccm()));
        indicators.setIcdc(convertQAIndicatorDetail(qaIndicatorsState.getIcdc()));
        indicators.setIcpu(convertQAIndicatorDetail(qaIndicatorsState.getIcpu()));
        indicators.setIcr(convertQAIndicatorDetail(qaIndicatorsState.getIcr()));

        return indicators;
    }

    private static CodeAnalysisIndicatorDetail convertQAIndicatorDetail(QAICDetail indicatorDetail)
    {
        CodeAnalysisIndicatorDetail analysisIndicatorDetail = new CodeAnalysisIndicatorDetail();
        analysisIndicatorDetail.setValue(indicatorDetail.getValue());
        analysisIndicatorDetail.setLimit(indicatorDetail.getLimit());

        return analysisIndicatorDetail;
    }

    public static QASubsystemCodeAnalysis[] getDummyQASubsystemCodeAnalysisArray()
    {
        QASubsystemCodeAnalysis item = new QASubsystemCodeAnalysis();
        item.setSubsystemId(1);
        item.setId(1);
        item.setStatus("STATUS");
        item.setCreationDate(1000L);
        item.setErrorMessage("ERROR_MESSAGE");
        item.setBranch("BRANCH");
        item.setJenkinsJobId(1);
        item.setSqaState("SQA_STATE");
        item.setCodeAnalyses(getDummyQACodeAnalysisArray());
        return new QASubsystemCodeAnalysis[]{item};
    }

    public static HostMemoryInfo[] getDummyHostMemoryInfos()
    {
        HostMemoryInfo item = new HostMemoryInfo();
        item.setCpd("CPD");
        item.setEnvironment("INT");
        item.setHostName("HOST_NAME");
        item.setUsedMemory(100D);
        item.setTotalMemory(200D);
        item.setPercentageUsedMemory(50D);
        return new HostMemoryInfo[]{item};
    }

    public static ScheduleRequest getDummyScheduleRequest()
    {
        ScheduleRequest item = new ScheduleRequest();
        item.setEnvironment("INT");
        item.setId(1L);
        item.setStatus("STATUS");
        item.setRequestDate("REQUEST_DATE");
        item.setScheduleDate("SCHEDULE_DATE");
        item.setHoldStatus("HOLD_STATUS");
        item.setIssueKey("ISSUE_KEY");
        item.setProductId(1L);
        item.setTodoTaskId(1L);
        item.setUnscheduleDate("UNSCHEDULED_DATE");
        item.setDescription("DESCRIPTION");
        return item;
    }

    public static DeploymentBatchScheduleDTO getDummyDeploymentBatchScheduleDto()
    {
        DeploymentBatchScheduleDTO dto = new DeploymentBatchScheduleDTO();
        dto.setEnvironment("INT");
        dto.setId(1);
        dto.setReleaseVersionServiceId(2);
        dto.setDeploymentPlanId(3);
        dto.setState("STATE");
        return dto;
    }

    public static DeploymentBatchScheduleInstanceDTO[] getDummyDeploymentBatchScheduleInstanceDtos()
    {
        DeploymentBatchScheduleInstanceDTO dto = new DeploymentBatchScheduleInstanceDTO();
        dto.setDeploymentPlanId(1);
        dto.setEnvironment("INT");
        dto.setId(1);
        dto.setStatus("STATUS");
        dto.setReleaseVersionServiceId(1);
        dto.setCurrentStep(1);
        dto.setEndDate("END_DATE");
        dto.setStartDate("START_DATE");
        dto.setState("STATE");
        dto.setPendingActionTask(getDummyPendingTaskSchedule());
        dto.setDeploymentScheduleContextParamsList(getDummyDeploymentContextParamsInstanceDtos());
        dto.setDeploymentScheduleStepList(getDummyDeploymentStepInstanceDtos());
        return new DeploymentBatchScheduleInstanceDTO[]{dto};
    }

    private static PendingTaskSchedule getDummyPendingTaskSchedule()
    {
        PendingTaskSchedule item = new PendingTaskSchedule();
        item.setMessage("MESSAGE");
        item.setPending(Boolean.TRUE);
        item.setTaskId(1);
        item.setTaskType("TASK_TYPE");
        return item;
    }

    private static DeploymentContextParamsInstanceDTO[] getDummyDeploymentContextParamsInstanceDtos()
    {
        DeploymentContextParamsInstanceDTO dto = new DeploymentContextParamsInstanceDTO();
        dto.setId(1);
        dto.setValue("VALUE");
        dto.setName("NAME");
        dto.setContextParamType("CONTEXT_PARAM_TYPE");
        return new DeploymentContextParamsInstanceDTO[]{dto};
    }

    private static DeploymentStepInstanceDTO[] getDummyDeploymentStepInstanceDtos()
    {
        DeploymentStepInstanceDTO dto = new DeploymentStepInstanceDTO();
        dto.setEndDate("END_DATE");
        dto.setId(1);
        dto.setName("NAME");
        dto.setStartDate("START_DATE");
        dto.setState("STATE");
        dto.setIdAssociated(1);
        dto.setStepType("STEP_TYPE");
        dto.setDeploymentScheduleJobList(getDummyDeploymentJobsInstanceDtos());
        return new DeploymentStepInstanceDTO[]{dto};
    }

    private static DeploymentJobsInstanceDTO[] getDummyDeploymentJobsInstanceDtos()
    {
        DeploymentJobsInstanceDTO dto = new DeploymentJobsInstanceDTO();
        dto.setDeploymentBatchScheduleInstanceContextHistoricList(getDummyDeploymentContextParamsHistoricInstanceDtos());
        dto.setEndDate("END_DATE");
        dto.setId(1);
        dto.setName("NAME");
        dto.setStartDate("START_DATE");
        dto.setState("STATE");
        dto.setComment("COMMENT");
        dto.setExitCode(2);
        dto.setJobType("JOB_TYPE");
        dto.setTaskId(1L);
        return new DeploymentJobsInstanceDTO[]{dto};
    }

    private static DeploymentContextParamsHistoricInstanceDTO[] getDummyDeploymentContextParamsHistoricInstanceDtos()
    {
        DeploymentContextParamsHistoricInstanceDTO dto = new DeploymentContextParamsHistoricInstanceDTO();
        dto.setId(1);
        dto.setDate("DATE");
        dto.setName("NAME");
        dto.setValueNew("VALUE_NEW");
        dto.setValueOld("VALUE_OLD");
        return new DeploymentContextParamsHistoricInstanceDTO[]{dto};
    }

    public static DisabledDateDTO[] getDummyDisabledDateDtos()
    {
        DisabledDateDTO dto = new DisabledDateDTO();
        dto.setId(1L);
        dto.setDate("DATE");
        dto.setDescription("DESCRIPTION");
        dto.setPlatform("PLATFORM");
        dto.setExceptions(getDummyDisabledDateExceptionDtos());
        return new DisabledDateDTO[]{dto};
    }

    private static DisabledDateExceptionDTO[] getDummyDisabledDateExceptionDtos()
    {
        DisabledDateExceptionDTO dto = new DisabledDateExceptionDTO();
        dto.setDescription("DESCRIPTION");
        dto.setId(1L);
        dto.setUuaa("UUAA");
        return new DisabledDateExceptionDTO[]{dto};
    }

    public static SMBatchSchedulerExecutionsSummaryDTO getDummySMBatchSchedulerExecutionsSummaryDto()
    {
        SMBatchSchedulerExecutionsSummaryDTO dto = new SMBatchSchedulerExecutionsSummaryDTO();
        dto.setTotal(1L);
        dto.setElements(getDummySMBatchSchedulerExecutionDtos());
        return dto;
    }

    private static SMBatchSchedulerExecutionDTO[] getDummySMBatchSchedulerExecutionDtos()
    {
        SMBatchSchedulerExecutionDTO dto = new SMBatchSchedulerExecutionDTO();
        dto.setStatus("STATUS");
        dto.setTotal(1L);
        return new SMBatchSchedulerExecutionDTO[]{dto};
    }

    public static ApiTaskCreationDTO getDummyApiTaskCreationDto()
    {
        ApiTaskCreationDTO dto = new ApiTaskCreationDTO();
        dto.setApiKey(getDummyApiTaskKeyDto());
        dto.getApiKey().setUuaa("UUAA");
        return dto;
    }

    public static ApiTaskFilterResponseDTO[] getDummyApiTaskFilterResponseDtos()
    {
        ApiTaskFilterResponseDTO dto = new ApiTaskFilterResponseDTO();
        dto.setApiId(1);
        dto.setApiTask(getDummyApiTaskDto());
        return new ApiTaskFilterResponseDTO[]{dto};
    }

    public static ApiTaskDTO getDummyApiTaskDto()
    {
        ApiTaskDTO dto = new ApiTaskDTO();
        dto.setApiKey(getDummyApiTaskKeyDto());
        dto.setStatus("STATUS");
        dto.setId(1);
        dto.setCreationDate(1000L);
        dto.setDescription("DESCRIPTION");
        dto.setAssignedGroup("ASSIGNED_GROUP");
        dto.setAssignedUser(getDummyTaskUser());
        dto.setClosingDate(1000L);
        dto.setClosingMotive("CLOSING_MOTIVE");
        dto.setCorrelatedLogUrl("CORRELATED_LOG_URL");
        dto.setCreationUser(getDummyTaskUser());
        dto.setHelpUrl("HELP_URL");
        dto.setIssueKey("ISSUE_KEY");
        dto.setPreviousTask(1);
        dto.setProduct(1);
        dto.setProductName("PRODUCT_NAME");
        dto.setStatusMessage("STATUS_MESSAGE");
        dto.setTaskType("TASK_TYPE");
        dto.setTraceIdUrl("TRACE_ID_URL");
        return dto;
    }

    public static ApiTaskKeyDTO getDummyApiTaskKeyDto()
    {
        ApiTaskKeyDTO dto = new ApiTaskKeyDTO();
        dto.setUuaa("UUAA");
        dto.setBasePath("BASE_PATH");
        dto.setApiName("API_NAME");
        return dto;
    }

    private static TaskUser getDummyTaskUser()
    {
        TaskUser item = new TaskUser();
        item.setActive(Boolean.TRUE);
        item.setId(2);
        item.setUserCode("USER_CODE");
        item.setEmail("EMAIL");
        item.setUserName("USER_NAME");
        item.setSurname1("SURNAME1");
        item.setSurname2("SURNAME2");
        return item;
    }

    public static TaskSummaryList getDummyTaskSummaryList()
    {
        TaskSummaryList item = new TaskSummaryList();
        item.setTotalSize(1L);
        item.setTasks(getDummyTaskSummaries());
        return item;
    }

    private static TaskSummary[] getDummyTaskSummaries()
    {
        TaskSummary item = new TaskSummary();
        item.setId(1);
        item.setTaskType("TASK_TYPE");
        item.setAssignedGroup("ASSIGNED_GROUP");
        return new TaskSummary[]{item};
    }

    public static TOProductUserDTO getDummyTOProductUserDto()
    {
        TOProductUserDTO dto = new TOProductUserDTO();
        dto.setProductId(1);
        dto.setUserCode("USER_CODE");
        return dto;
    }

    public static TOSubsystemsCombinationDTO[] getDummyTOSubsystemsCombinationDtos()
    {
        TOSubsystemsCombinationDTO dto = new TOSubsystemsCombinationDTO();
        dto.setEnvironment("INT");
        dto.setSubsystemType("SUBSYSTEM_TYPE");
        dto.setUuaa("UUAA");
        dto.setProductId(1);
        dto.setCount(1);
        return new TOSubsystemsCombinationDTO[]{dto};
    }

    public static UATeamUser[] getDummyUATeamUsers()
    {
        UATeamUser item = new UATeamUser();
        item.setActive(Boolean.TRUE);
        item.setEmail("EMAIL");
        item.setUserCode("USER_CODE");
        item.setUserName("USER_NAME");
        item.setSurname1("SURNAME1");
        item.setSurname2("SURNAME2");
        item.setRole("ROLE");
        return new UATeamUser[]{item};
    }

    public static TeamCountDTO[] getDummyTeamCountDtos()
    {
        TeamCountDTO dto = new TeamCountDTO();
        dto.setTotal(10L);
        dto.setTeam("TEAM");
        return new TeamCountDTO[]{dto};
    }

    public static UserProductRoleHistoryDTO[] getDummyUserProductRoleHistoryDtos()
    {
        UserProductRoleHistoryDTO dto = new UserProductRoleHistoryDTO();
        dto.setProductId(1);
        dto.setRole("ROLE");
        dto.setValue(1);
        dto.setUuaa("UUAA");
        return new UserProductRoleHistoryDTO[]{dto};
    }

    public static VCSTag[] getDummyVCSTags()
    {
        VCSTag item = new VCSTag();
        item.setMessage("MESSAGE");
        item.setTagName("TAG_NAME");
        return new VCSTag[]{item};
    }

    public static LMLibraryRequirementsDTO getDummyLMLibraryRequirementsDto()
    {
        LMLibraryRequirementsDTO dto = new LMLibraryRequirementsDTO();
        dto.setFullName("FULL_NAME");
        dto.setReleaseVersionServiceId(1);
        dto.setRequirements(getDummyLMLibraryRequirementDtos());
        return dto;
    }

    private static LMLibraryRequirementDTO[] getDummyLMLibraryRequirementDtos()
    {
        LMLibraryRequirementDTO dto = new LMLibraryRequirementDTO();
        dto.setRequirementDescription("REQUIREMENT_DESCRIPTION");
        dto.setRequirementName("REQUIREMENT_NAME");
        dto.setRequirementType("REQUIREMENT_TYPE");
        dto.setRequirementValue("REQUIREMENT_VALUE");
        return new LMLibraryRequirementDTO[]{dto};
    }

    public static LMUsedLibrariesDTO[] getDummyLMUsedLibrariesDtos()
    {
        LMUsedLibrariesDTO dto = new LMUsedLibrariesDTO();
        dto.setUsage("USAGE");
        dto.setFullName("FULL_NAME");
        dto.setInte(Boolean.TRUE);
        dto.setPre(Boolean.TRUE);
        dto.setPro(Boolean.TRUE);
        dto.setReleaseVersionServiceId(1);
        return new LMUsedLibrariesDTO[]{dto};
    }

    public static LMUsageDTO[] getDummyLMUsageDtos()
    {
        LMUsageDTO dto = new LMUsageDTO();
        dto.setUsage("USAGE");
        dto.setServiceId(1);
        dto.setRvslibraryId(1);
        return new LMUsageDTO[]{dto};
    }

    public static LMLibraryEnvironmentsByServiceDTO[] getDummyLMLibraryEnvironmentsByServiceDtos()
    {
        LMLibraryEnvironmentsByServiceDTO dto = new LMLibraryEnvironmentsByServiceDTO();
        dto.setReleaseVersionServiceId(1);
        dto.setLibraries(new LMLibraryEnvironmentsDTO[]{getDummyLMLibraryEnvironmentsDto()});
        return new LMLibraryEnvironmentsByServiceDTO[]{dto};
    }

    public static LMLibraryEnvironmentsDTO getDummyLMLibraryEnvironmentsDto()
    {
        LMLibraryEnvironmentsDTO dto = new LMLibraryEnvironmentsDTO();
        dto.setFullName("FULL_NAME");
        dto.setInte(Boolean.TRUE);
        dto.setPre(Boolean.FALSE);
        dto.setPro(Boolean.FALSE);
        dto.setReleaseVersionServiceId(1);
        return dto;
    }

    public static LMLibraryValidationErrorDTO[] getDummyLMLibraryValidationErrorDtos()
    {
        LMLibraryValidationErrorDTO dto = new LMLibraryValidationErrorDTO();
        dto.setMessage("MESSAGE");
        dto.setCode("CODE");
        return new LMLibraryValidationErrorDTO[]{dto};
    }

    public static DeploymentServiceBatchStatus[] getDummyDeploymentServiceBatchStatuses()
    {
        DeploymentServiceBatchStatus item = new DeploymentServiceBatchStatus();
        item.setDeploymentServiceId(1);
        item.setIsRunning(Boolean.TRUE);
        return new DeploymentServiceBatchStatus[]{item};
    }

    public static RunningBatchs getDummyRunningBatchs()
    {
        RunningBatchs item = new RunningBatchs();
        item.setRunningBatchs(1L);
        item.setServiceId(1);
        return item;
    }

    public static BatchManagerBatchExecutionsSummaryDTO getDummyBatchManagerBatchExecutionsSummaryDto()
    {
        BatchManagerBatchExecutionsSummaryDTO dto = new BatchManagerBatchExecutionsSummaryDTO();
        dto.setTotal(1L);
        dto.setPercentage(10);
        dto.setElements(getDummyBatchManagerBatchExecutionDtos());
        return dto;
    }

    private static BatchManagerBatchExecutionDTO[] getDummyBatchManagerBatchExecutionDtos()
    {
        BatchManagerBatchExecutionDTO dto = new BatchManagerBatchExecutionDTO();
        dto.setStatus("STATUS");
        dto.setTotal(1L);
        return new BatchManagerBatchExecutionDTO[]{dto};
    }

    public static Map<String, Map<String, String>> getDummyExtraProperties()
    {
        int size = 2;
        Map<String, Map<String, String>> extraPropertiesMap = new HashMap<>(size);
        for (int i = 1; i <= size; i++)
        {
            Map<String, String> propertyNameValueMap = Map.of("NAME_" + i, "VALUE_" + i);
            extraPropertiesMap.put("TYPE_" + i, propertyNameValueMap);
        }
        return extraPropertiesMap;
    }

    public static CMLogicalConnectorPropertyDto[] getCMLogicalConnectorPropertyDtos()
    {
        CMLogicalConnectorPropertyDto dto = new CMLogicalConnectorPropertyDto();
        dto.setId(1);
        dto.setLogicalConnectorId(1);
        dto.setInaccessible(false);
        dto.setPropertyName("A");
        dto.setPropertyDescription("A");
        dto.setPropertyDefaultName("A");
        dto.setPropertyManagement("A");
        dto.setPropertyScope("A");
        dto.setPropertySecurity(true);
        dto.setPropertyType("A");
        dto.setPropertyValue("A");
        return new CMLogicalConnectorPropertyDto[]{dto};
    }
}
