package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASRequestAlertsDTO;
import com.bbva.enoa.apirestgen.schedulecontrolmapi.model.ScheduleRequest;
import com.bbva.enoa.apirestgen.schedulecontrolmapi.model.ScheduleRequestDocument;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationRevision;
import com.bbva.enoa.datamodel.model.config.entities.PropertyDefinition;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentNova;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceAllowedJdkParameterValue;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceFilesystem;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.entities.NovaDeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentPriority;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentType;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.AllowedJdk;
import com.bbva.enoa.datamodel.model.release.entities.AllowedJdkParameterProduct;
import com.bbva.enoa.datamodel.model.release.entities.CPD;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.todotask.entities.ConfigurationTask;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTask;
import com.bbva.enoa.datamodel.model.todotask.entities.DeploymentTypeChangeTask;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.entities.ToDoTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
public class MocksAndUtils
{

	private static final AtomicInteger atomicId = new AtomicInteger(RandomUtils.nextInt(1, 1000));

	private static int getId()
	{
		return atomicId.getAndIncrement();
	}

	private static <T extends ToDoTask> List<T> createToDoTaskList(T task,
			ToDoTaskType taskTypeDTO,ToDoTaskStatus toDoTaskStatus)
	{
		var deploymentTaskList = new ArrayList<T>();
		task.setId(getId());
		task.setTaskType(taskTypeDTO);
		task.setStatus(toDoTaskStatus);
		deploymentTaskList.add(task);
		return deploymentTaskList;
	}

	public static PropertyDefinition createPropertyDefinition(ReleaseVersionService rvs)
	{
		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setId(getId());
		propertyDefinition.setName("PropertyName"+getId());
		propertyDefinition.setService(rvs);

		return propertyDefinition;
	}

	public static List<DeploymentTask> createDeploymentTaskList(
			ToDoTaskType taskTypeDTO, ToDoTaskStatus toDoTaskStatus)
	{
		return createToDoTaskList(new DeploymentTask(), taskTypeDTO, toDoTaskStatus);
	}

	public static List<ConfigurationTask> createConfigurationTaskList(
			ToDoTaskType taskTypeDTO, ToDoTaskStatus toDoTaskStatus)
	{
		return createToDoTaskList(new ConfigurationTask(), taskTypeDTO, toDoTaskStatus);
	}

	public static List<DeploymentTypeChangeTask>  createDeploymentTypeChangeTaskList(
			ToDoTaskType toDoTaskType, ToDoTaskStatus toDoTaskStatus)
	{
		return createToDoTaskList(new DeploymentTypeChangeTask(), toDoTaskType, toDoTaskStatus);
	}

	public static List<ManagementActionTask> createManagementActionTaskList(
			ToDoTaskType toDoTaskType, ToDoTaskStatus toDoTaskStatus)
	{
		return createToDoTaskList(new ManagementActionTask(), toDoTaskType, toDoTaskStatus);
	}

	public static ASRequestAlertsDTO createASRequestAlertsDTO()
	{
		var aSRequestAlertsDTO = new ASRequestAlertsDTO();
		aSRequestAlertsDTO.setHaveAlerts(false);
		return aSRequestAlertsDTO;
	}

	public static DeploymentNova createDeploymentNova(
			Integer undeployPlanId, DeploymentPriority deploymentPriority)
	{
		DeploymentNova nova = new DeploymentNova ();
		nova.setDeploymentDateTime (new Date());
		nova.setUndeployRelease(undeployPlanId);
		nova.setPriorityLevel(deploymentPriority);
		nova.setDeploymentList(
				ThreadLocalRandom.current().ints( 10 ).boxed().map(Objects::toString).collect(Collectors.joining(","))
		);
		nova.setGcspPass(ThreadLocalRandom.current().nextInt());
		return nova;
	}

	public static DeploymentPlan createDeploymentPlanWithNova(DeploymentPriority deploymentPriority)
	{
		var undeployPlanId = ThreadLocalRandom.current().nextInt();
		DeploymentPlan plan = createDeploymentPlan(
				false, false, false, Environment.PRO);
		plan.setNova(createDeploymentNova(undeployPlanId, deploymentPriority));
		return plan;
	}

	private static CPD createCPD()
	{
		var cpd = new CPD();
		cpd.setName("Name"+getId());
		return cpd;
	}

	public static DeploymentPlan createDeploymentPlan(
			boolean addFilesystem, boolean addConnector, boolean isMultiJdk,
			Environment environment)
	{
		DeploymentPlan plan = new DeploymentPlan();
		List<DeploymentSubsystem> subsystemList = new ArrayList<>();
		ReleaseVersion releaseVersion = createReleaseVersion();
		subsystemList.add(createDeploymentSubsystem(addFilesystem, addConnector, isMultiJdk, releaseVersion));
		plan.setDeploymentSubsystems(subsystemList);
		plan.setEnvironment(environment.getEnvironment());
		plan.setId(getId());
		ConfigurationRevision revision = new ConfigurationRevision();
		revision.setId(getId());
		plan.setCurrentRevision(revision);
		plan.setReleaseVersion(releaseVersion);
		plan.setExecutionDate(Calendar.getInstance());
		plan.setUndeploymentDate(Calendar.getInstance());
		plan.setStatus(DeploymentStatus.DEFINITION);
		plan.setRejectionMessage("reject message");
		plan.setConfigurationTask( createDeploymentTaskList(
				ToDoTaskType.CHECK_ENVIRONMENT_VARS, ToDoTaskStatus.PENDING).get(0));
		plan.setCpdInPro(createCPD());

		for(var subsystem: subsystemList)
		{
			subsystem.setDeploymentPlan(plan);
		}

		return plan;
	}

	public static TOSubsystemDTO createTOSubsystemDTO()
	{
		int id = getId();
		TOSubsystemDTO productSubsystem = new TOSubsystemDTO();
		productSubsystem.setSubsystemName("ProductSubsystem"+id);
		productSubsystem.setSubsystemId(id);
		return productSubsystem;
	}

	private static DeploymentSubsystem createDeploymentSubsystem(
			boolean addFilesystem, boolean addConnector, boolean isMultiJdk, ReleaseVersion releaseVersion)
	{
		DeploymentSubsystem subsystem = new DeploymentSubsystem();
		subsystem.setId(getId());
		subsystem.setSubsystem(createReleaseVersionSubsystem(releaseVersion));
		List<DeploymentService> serviceList = new ArrayList<>();
		serviceList.add(createDeploymentService(addFilesystem, addConnector, isMultiJdk, releaseVersion));
		subsystem.setDeploymentServices(serviceList);

		for(var deploymentService: serviceList)
		{
			deploymentService.setDeploymentSubsystem(subsystem);
		}

		return subsystem;
	}

	private static Filesystem createFilesystem()
	{
		var filesystem = new FilesystemNova();
		filesystem.setId(getId());
		filesystem.setName("name"+getId());
		return filesystem;
	}

	private static void addFilesystemToService(DeploymentService deploymentService)
	{
		List<DeploymentServiceFilesystem> deploymentServiceFilesystems = new ArrayList<>();
		DeploymentServiceFilesystem deploymentServiceFilesystem = new DeploymentServiceFilesystem();

		deploymentServiceFilesystem.setFilesystem(createFilesystem());

		deploymentServiceFilesystems.add(deploymentServiceFilesystem);
		deploymentService.setDeploymentServiceFilesystems(deploymentServiceFilesystems);
	}

	private static void addConnectorToService(DeploymentService deploymentService)
	{
		List<LogicalConnector> logicalConnectorList = new ArrayList<>();
		var logicalConnector = new LogicalConnector();
		logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
		var docSystem = new DocSystem();
		logicalConnector.setId(getId());
		logicalConnector.setMsaDocument(docSystem);
		logicalConnector.setEnvironment(Environment.PRO.getEnvironment());
		logicalConnectorList.add(logicalConnector);
		deploymentService.setLogicalConnectors(logicalConnectorList);
	}

	private static void addParamValues(DeploymentService deploymentService)
	{
		List<DeploymentServiceAllowedJdkParameterValue> values = new ArrayList<>();
		var value = new DeploymentServiceAllowedJdkParameterValue();

		var allowed = new AllowedJdkParameterProduct();
		allowed.setId(getId());
		value.setAllowedJdkParameterProduct(allowed);

		values.add(value);
		deploymentService.setParamValues(values);
	}

	private static ScheduleRequestDocument createScheduleRequestDocument()
	{
		ScheduleRequestDocument scheduleRequestDocument = new ScheduleRequestDocument();
		scheduleRequestDocument.setId(getId());
		scheduleRequestDocument.setSystemName("systemName"+getId());
		return scheduleRequestDocument;
	}

	public static ScheduleRequest createScheduleRequest()
	{
		ScheduleRequest scheduleRequest = new ScheduleRequest();
		scheduleRequest.setId(Long.valueOf(getId()));
		scheduleRequest.setScheduleRequestDocument(createScheduleRequestDocument());
		return scheduleRequest;
	}

	public static DeploymentInstance createDeploymentInstance()
	{
		NovaDeploymentInstance deploymentInstance = new NovaDeploymentInstance();
		deploymentInstance.setId(getId());
		return deploymentInstance;
	}

	private static DeploymentService createDeploymentService(
			boolean addFilesystem, boolean addConnector, boolean isMultiJdk, ReleaseVersion releaseVersion)
	{
		DeploymentService deploymentService = new DeploymentService();
		deploymentService.setId(getId());
		deploymentService.setService(createReleaseVersionService(getId(),
				createReleaseVersionSubsystem(releaseVersion)));
		if(addFilesystem)
		{
			addFilesystemToService(deploymentService);
		}
		if(addConnector)
		{
			addConnectorToService(deploymentService);
		}
		if(isMultiJdk)
		{
			addParamValues(deploymentService);
		}
		return deploymentService;
	}

	private static ReleaseVersionSubsystem createReleaseVersionSubsystem(ReleaseVersion releaseVersion)
	{
		int id = getId();
		ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
		releaseVersionSubsystem.setSubsystemId(id);
		releaseVersionSubsystem.setTagName("TAG"+id);
		releaseVersionSubsystem.setServices(createReleaseVersionServiceList(releaseVersionSubsystem));
		releaseVersionSubsystem.setReleaseVersion(releaseVersion);
		return releaseVersionSubsystem;
	}

	private static List<ReleaseVersionService> createReleaseVersionServiceList(ReleaseVersionSubsystem releaseVersionSubsystem)
	{
		List<ReleaseVersionService> releaseVersionServices = new ArrayList<>();
		releaseVersionServices.add(createReleaseVersionService(getId(), releaseVersionSubsystem));
		return releaseVersionServices;
	}

	private static List<ApiImplementation<?, ?, ?>> createApiImplementations()
	{
		List<ApiImplementation<?, ?, ?>> apis = new ArrayList<>();
		var apiImpl = new AsyncBackToBackApiImplementation();
		var apiVersion = new AsyncBackToBackApiVersion();
		var api = new AsyncBackToBackApi();
		apiVersion.setApi(api);
		apiImpl.setApiVersion(apiVersion);
		apis.add(apiImpl);
		return apis;
	}

	private static AllowedJdk createAllowedJdk()
	{
		AllowedJdk allowedJdk = new AllowedJdk();
		allowedJdk.setJvmVersion(""+getId());
		return allowedJdk;
	}

	private static ReleaseVersionService createReleaseVersionService(int id, ReleaseVersionSubsystem releaseVersionSubsystem)
	{
		ReleaseVersionService releaseVersionService = new ReleaseVersionService();
		releaseVersionService.setId(RandomUtils.nextInt());
		releaseVersionService.setArtifactId("Service"+id);
		releaseVersionService.setGroupId("Group"+id);
		releaseVersionService.setVersion(""+id);
		releaseVersionService.setServiceName("Name"+id);
		releaseVersionService.setVersionSubsystem(releaseVersionSubsystem);
		releaseVersionService.setApiImplementations(createApiImplementations());
		releaseVersionService.setAllowedJdk(createAllowedJdk());
		return releaseVersionService;
	}

	private static ReleaseVersion createReleaseVersion()
	{
		ReleaseVersion releaseVersion = new ReleaseVersion();
		List<ReleaseVersionSubsystem> releaseVersionSubsystems = new ArrayList<>();
		releaseVersionSubsystems.add(createReleaseVersionSubsystem(releaseVersion));
		releaseVersion.setSubsystems(releaseVersionSubsystems);
		releaseVersion.setRelease(createRelease());
		return releaseVersion;
	}

	private static Release createRelease()
	{
		Release release = new Release();
		release.setId(getId());
		release.setDeploymentTypeInPro(DeploymentType.ON_DEMAND);
		release.setName("name");
		Product product = new Product();
		product.setId(getId());
		product.setMultiCPDInPro(true);
		CPD cpd = new CPD();
		product.setCPDInPro(cpd);
		release.setProduct(product);
		return release;
	}
}
