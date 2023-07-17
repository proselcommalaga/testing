package com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services;

import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsByServiceDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsageDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsedLibrariesDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ILibraryManagerClient;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.impl.LibraryManagerServiceImpl;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LibraryManagerServiceImplTest
{
	@Mock
	private ILibraryManagerClient iLibraryManagerClient;

	@InjectMocks
	private LibraryManagerServiceImpl libraryManagerService;

	@BeforeEach
	public void init()
	{
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void storeNovaLibrariesRequirementsTest()
	{
		//WHEN
		ReleaseVersion releaseVersion = mock(ReleaseVersion.class);

		ReleaseVersionSubsystem subsystem = mock(ReleaseVersionSubsystem.class);
		int numSubsystems = RandomUtils.nextInt(1, 10);
		List<ReleaseVersionSubsystem> subsystems = Collections.nCopies(numSubsystems, subsystem);
		when(releaseVersion.getSubsystems()).thenReturn(subsystems);

		ReleaseVersionService service = mock(ReleaseVersionService.class);
		int numServices = RandomUtils.nextInt(1, 10);
		List<ReleaseVersionService> services = Collections.nCopies(numServices, service);
		when(subsystem.getServices()).thenReturn(services);

		when(service.getServiceType()).thenReturn(ServiceType.LIBRARY_JAVA.getServiceType());

		//THEN
		libraryManagerService.storeNovaLibrariesRequirements(releaseVersion);

		//VERIFY
		verify(this.iLibraryManagerClient, times(numSubsystems * numServices)).storeRequirements(service);
	}

	@Test
	public void removeNovaLibrariesRequirementsTest()
	{
		//WHEN
		ReleaseVersion releaseVersion = mock(ReleaseVersion.class);

		ReleaseVersionSubsystem subsystem = mock(ReleaseVersionSubsystem.class);
		int numSubsystems = RandomUtils.nextInt(1, 10);
		List<ReleaseVersionSubsystem> subsystems = Collections.nCopies(numSubsystems, subsystem);
		when(releaseVersion.getSubsystems()).thenReturn(subsystems);

		ReleaseVersionService service = mock(ReleaseVersionService.class);
		int numServices = RandomUtils.nextInt(1, 10);
		List<ReleaseVersionService> services = Collections.nCopies(numServices, service);
		when(subsystem.getServices()).thenReturn(services);

		when(service.getServiceType()).thenReturn(ServiceType.LIBRARY_JAVA.getServiceType());

		//THEN
		libraryManagerService.removeNovaLibrariesRequirements(releaseVersion);

		//VERIFY
		verify(this.iLibraryManagerClient, times(numSubsystems * numServices)).removeRequirements(service);
	}

	@Test
	public void getNovaLibraryRequirementsTest()
	{
		int id = RandomUtils.nextInt(1, 10);
		ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
		when(releaseVersionService.getId()).thenReturn(id);

		libraryManagerService.getNovaLibraryRequirements(releaseVersionService);

		verify(iLibraryManagerClient).getRequirements(id);
	}

	@Test
	public void publishLibraryOnEnvironmentTest()
	{
		int id = RandomUtils.nextInt(1, 10);
		ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
		when(releaseVersionService.getId()).thenReturn(id);

		DeploymentPlan deploymentPlan = mock(DeploymentPlan.class);
		Environment    environment    = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
		when(deploymentPlan.getEnvironment()).thenReturn(environment.getEnvironment());

		libraryManagerService.publishLibraryOnEnvironment("", releaseVersionService, deploymentPlan);

		verify(iLibraryManagerClient).publishLibraryOnEnvironment(id, environment.getEnvironment());
	}

	@Test
	public void getNovaLibraryRequirementsByFullNameTest()
	{
		String fullName = RandomStringUtils.randomAlphabetic(10);

		libraryManagerService.getNovaLibraryRequirementsByFullName(fullName);

		verify(iLibraryManagerClient).getRequirementsByFullName(fullName);
	}

	@Test
	public void getLibraryEnvironmentsTest()
	{
		String fullName = RandomStringUtils.randomAlphabetic(10);

		libraryManagerService.getLibraryEnvironments(fullName);

		verify(iLibraryManagerClient).getLibraryEnvironments(fullName);
	}

	@Test
	public void getUsedLibrariesByServiceTest()
	{
		int id = RandomUtils.nextInt(1, 10);
		String usage = RandomStringUtils.randomAlphabetic(10);

		libraryManagerService.getUsedLibrariesByService(id, usage);

		verify(iLibraryManagerClient).getLibrariesThatServiceUse(id, usage);
	}

	@Test
	public void getUsedLibrariesTest()
	{
		int[] rvsServiceIdArray = new Random().ints(10, 1, 99999).toArray();
		List<Integer> rvsServiceIdList = Arrays.stream(rvsServiceIdArray).boxed().collect(Collectors.toList());
		String usage = RandomStringUtils.randomAlphabetic(10);

		when(this.iLibraryManagerClient.getUsedLibraries(rvsServiceIdArray, usage)).thenReturn(new LMLibraryEnvironmentsDTO[0]);

		libraryManagerService.getUsedLibraries(rvsServiceIdList, usage);

		verify(iLibraryManagerClient).getUsedLibraries(rvsServiceIdArray, usage);
	}

	@Test
	public void getUsedLibrariesByServicesTest()
	{
		int[] rvsServiceIdArray = new Random().ints(10, 1, 99999).toArray();
		List<Integer> rvsServiceIdList = Arrays.stream(rvsServiceIdArray).boxed().collect(Collectors.toList());
		String usage = RandomStringUtils.randomAlphabetic(10);

		when(this.iLibraryManagerClient.getUsedLibrariesByServices(rvsServiceIdArray, usage)).thenReturn(new LMLibraryEnvironmentsByServiceDTO[0]);

		libraryManagerService.getUsedLibrariesByServices(rvsServiceIdList, usage);

		verify(iLibraryManagerClient).getUsedLibrariesByServices(rvsServiceIdArray, usage);
	}

	@Test
	public void saveUsedLibrariesTest()
	{
		int id = RandomUtils.nextInt(1, 999999);
		int size = RandomUtils.nextInt(1, 10);
		String[] libraries = new String[size];
		for (int i =0; i < size; i++)
		{
			libraries[i] = RandomStringUtils.randomAlphabetic(10);
		}

		when(this.iLibraryManagerClient.createUsedLibraries(id, libraries)).thenReturn(new LMUsedLibrariesDTO[0]);

		libraryManagerService.saveUsedLibraries(id, libraries);

		verify(iLibraryManagerClient).createUsedLibraries(id, libraries);
	}

	@Test
	public void removeLibrariesTest()
	{
		//WHEN
		ReleaseVersion releaseVersion = mock(ReleaseVersion.class);

		ReleaseVersionSubsystem subsystem = mock(ReleaseVersionSubsystem.class);
		int numSubsystems = RandomUtils.nextInt(1, 10);
		List<ReleaseVersionSubsystem> subsystems = Collections.nCopies(numSubsystems, subsystem);
		when(releaseVersion.getSubsystems()).thenReturn(subsystems);

		ReleaseVersionService service = mock(ReleaseVersionService.class);
		int numServices = RandomUtils.nextInt(1, 10);
		List<ReleaseVersionService> services = Collections.nCopies(numServices, service);
		when(subsystem.getServices()).thenReturn(services);

		when(service.getServiceType()).thenReturn(ServiceType.LIBRARY_JAVA.getServiceType());
		when(service.getId()).thenReturn(RandomUtils.nextInt(1, 10));

		//THEN
		libraryManagerService.removeLibraries(releaseVersion);

		//VERIFY
		verify(this.iLibraryManagerClient, times(numSubsystems * numServices)).removeLibrary(anyInt());
	}

	@Test
	public void removeLibrariesUsagesTest()
	{
		//WHEN
		ReleaseVersion releaseVersion = mock(ReleaseVersion.class);
		String usage = RandomStringUtils.randomAlphabetic(10);
		LMLibraryEnvironmentsDTO[] lmLibraryEnvironmentsDTOS = new LMLibraryEnvironmentsDTO[1];

		ReleaseVersionSubsystem subsystem = mock(ReleaseVersionSubsystem.class);
		int numSubsystems = RandomUtils.nextInt(1, 10);
		List<ReleaseVersionSubsystem> subsystems = Collections.nCopies(numSubsystems, subsystem);
		when(releaseVersion.getSubsystems()).thenReturn(subsystems);

		ReleaseVersionService service = mock(ReleaseVersionService.class);
		int numServices = RandomUtils.nextInt(1, 10);
		List<ReleaseVersionService> services = Collections.nCopies(numServices, service);
		when(subsystem.getServices()).thenReturn(services);

		when(iLibraryManagerClient.getLibrariesThatServiceUse(anyInt(), eq(usage))).thenReturn(lmLibraryEnvironmentsDTOS);
		when(service.getId()).thenReturn(RandomUtils.nextInt(1, 10));

		//THEN
		libraryManagerService.removeLibrariesUsages(releaseVersion, usage);

		//VERIFY
		verify(this.iLibraryManagerClient, times(numSubsystems * numServices)).removeLibrariesUsages(anyInt(), eq(usage));
	}

	@Test
	public void checkPublishedLibrariesByDeploymentPlanAllInvalidServicesTest()
	{
		//WHEN
		Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
		DeploymentPlan plan = mock(DeploymentPlan.class);

		DeploymentSubsystem subsystem = mock(DeploymentSubsystem.class);
		int numSubsystems = RandomUtils.nextInt(1, 10);
		List<DeploymentSubsystem> subsystems = Collections.nCopies(numSubsystems, subsystem);
		when(plan.getDeploymentSubsystems()).thenReturn(subsystems);

		DeploymentService service = mock(DeploymentService.class);
		int numServices = RandomUtils.nextInt(1, 10);
		List<DeploymentService> services = Collections.nCopies(numServices, service);
		when(subsystem.getDeploymentServices()).thenReturn(services);

		ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
		when(service.getService()).thenReturn(releaseVersionService);

		when(releaseVersionService.getServiceType()).thenReturn(ServiceType.DAEMON_JAVA_SPRING_BOOT.getServiceType());
		when(service.getId()).thenReturn(RandomUtils.nextInt(1, 10));

		when(this.iLibraryManagerClient.checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()))).thenReturn(false);
		//THEN
		assertThrows(NovaException.class, () ->
			libraryManagerService.checkPublishedLibrariesByDeploymentPlan(plan, environment.toString())
		);

		//VERIFY
		verify(iLibraryManagerClient, times(numSubsystems * numServices)).checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()));
	}

	@Test
	public void checkPublishedLibrariesByDeploymentPlanAllValidServicesTest()
	{
		//WHEN
		Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
		DeploymentPlan plan = mock(DeploymentPlan.class);

		DeploymentSubsystem subsystem = mock(DeploymentSubsystem.class);
		int numSubsystems = RandomUtils.nextInt(1, 10);
		List<DeploymentSubsystem> subsystems = Collections.nCopies(numSubsystems, subsystem);
		when(plan.getDeploymentSubsystems()).thenReturn(subsystems);

		DeploymentService service = mock(DeploymentService.class);
		int numServices = RandomUtils.nextInt(1, 10);
		List<DeploymentService> services = Collections.nCopies(numServices, service);
		when(subsystem.getDeploymentServices()).thenReturn(services);

		ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
		when(service.getService()).thenReturn(releaseVersionService);

		when(releaseVersionService.getServiceType()).thenReturn(ServiceType.DAEMON_JAVA_SPRING_BOOT.getServiceType());
		when(service.getId()).thenReturn(RandomUtils.nextInt(1, 10));

		when(this.iLibraryManagerClient.checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()))).thenReturn(true);

		//THEN
		libraryManagerService.checkPublishedLibrariesByDeploymentPlan(plan, environment.toString());

		//VERIFY
		verify(iLibraryManagerClient, times(numSubsystems * numServices)).checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()));
	}

	@Test
	public void checkPublishedLibrariesByDeploymentPlanAllLibrariesTest()
	{
		//WHEN
		Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
		DeploymentPlan plan = mock(DeploymentPlan.class);

		DeploymentSubsystem subsystem = mock(DeploymentSubsystem.class);
		int numSubsystems = RandomUtils.nextInt(1, 10);
		List<DeploymentSubsystem> subsystems = Collections.nCopies(numSubsystems, subsystem);
		when(plan.getDeploymentSubsystems()).thenReturn(subsystems);

		DeploymentService service = mock(DeploymentService.class);
		int numServices = RandomUtils.nextInt(1, 10);
		List<DeploymentService> services = Collections.nCopies(numServices, service);
		when(subsystem.getDeploymentServices()).thenReturn(services);

		ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
		when(service.getService()).thenReturn(releaseVersionService);

		when(releaseVersionService.getServiceType()).thenReturn(ServiceType.LIBRARY_JAVA.getServiceType());
		when(service.getId()).thenReturn(RandomUtils.nextInt(1, 10));

		when(this.iLibraryManagerClient.checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()))).thenReturn(true);

		//THEN
		libraryManagerService.checkPublishedLibrariesByDeploymentPlan(plan, environment.toString());

		//VERIFY
		verify(iLibraryManagerClient, times(0)).checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()));
	}

	@Test
	public void checkPublishedLibrariesByDeploymentSubsystemAllInvalidServicesTest()
	{
		//WHEN
		Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
		DeploymentSubsystem subsystem = mock(DeploymentSubsystem.class);

		DeploymentService service = mock(DeploymentService.class);
		int numServices = RandomUtils.nextInt(1, 10);
		List<DeploymentService> services = Collections.nCopies(numServices, service);
		when(subsystem.getDeploymentServices()).thenReturn(services);

		ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
		when(service.getService()).thenReturn(releaseVersionService);

		when(releaseVersionService.getServiceType()).thenReturn(ServiceType.DAEMON_JAVA_SPRING_BOOT.getServiceType());
		when(service.getId()).thenReturn(RandomUtils.nextInt(1, 10));

		when(this.iLibraryManagerClient.checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()))).thenReturn(false);

		//THEN
		assertThrows(NovaException.class, () ->
			libraryManagerService.checkPublishedLibrariesByDeploymentSubsystem(subsystem, environment.toString())
		);
	}

	@Test
	public void checkPublishedLibrariesByDeploymentSubsystemAllValidServicesTest()
	{
		//WHEN
		Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
		DeploymentSubsystem subsystem = mock(DeploymentSubsystem.class);

		DeploymentService service = mock(DeploymentService.class);
		int numServices = RandomUtils.nextInt(1, 10);
		List<DeploymentService> services = Collections.nCopies(numServices, service);
		when(subsystem.getDeploymentServices()).thenReturn(services);

		ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
		when(service.getService()).thenReturn(releaseVersionService);

		when(releaseVersionService.getServiceType()).thenReturn(ServiceType.DAEMON_JAVA_SPRING_BOOT.getServiceType());
		when(service.getId()).thenReturn(RandomUtils.nextInt(1, 10));

		when(this.iLibraryManagerClient.checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()))).thenReturn(true);

		//THEN
		libraryManagerService.checkPublishedLibrariesByDeploymentSubsystem(subsystem, environment.toString());

		//VERIFY
		verify(iLibraryManagerClient, times(numServices)).checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()));
	}

	@Test
	public void checkPublishedLibrariesByDeploymentSubsystemAllLibrariesTest()
	{
		//WHEN
		Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
		DeploymentSubsystem subsystem = mock(DeploymentSubsystem.class);

		DeploymentService service = mock(DeploymentService.class);
		int numServices = RandomUtils.nextInt(1, 10);
		List<DeploymentService> services = Collections.nCopies(numServices, service);
		when(subsystem.getDeploymentServices()).thenReturn(services);

		ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
		when(service.getService()).thenReturn(releaseVersionService);

		when(releaseVersionService.getServiceType()).thenReturn(ServiceType.LIBRARY_JAVA.getServiceType());
		when(service.getId()).thenReturn(RandomUtils.nextInt(1, 10));

		when(this.iLibraryManagerClient.checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()))).thenReturn(true);

		//THEN
		libraryManagerService.checkPublishedLibrariesByDeploymentSubsystem(subsystem, environment.toString());

		//VERIFY
		verify(iLibraryManagerClient, times(0)).checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()));
	}

	@Test
	public void checkPublishedLibrariesByDeploymentServiceInvalidServicesTest()
	{
		//WHEN
		Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
		DeploymentService service = mock(DeploymentService.class);

		ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
		when(service.getService()).thenReturn(releaseVersionService);

		when(releaseVersionService.getServiceType()).thenReturn(ServiceType.DAEMON_JAVA_SPRING_BOOT.getServiceType());
		when(service.getId()).thenReturn(RandomUtils.nextInt(1, 10));

		when(this.iLibraryManagerClient.checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()))).thenReturn(false);

		//THEN
		assertThrows(NovaException.class, () ->
				libraryManagerService.checkPublishedLibrariesByDeploymentService(service, environment.toString())
		);
	}

	@Test
	public void checkPublishedLibrariesByDeploymentServiceValidServiceTest()
	{
		//WHEN
		Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
		DeploymentService service = mock(DeploymentService.class);

		ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
		when(service.getService()).thenReturn(releaseVersionService);

		when(releaseVersionService.getServiceType()).thenReturn(ServiceType.DAEMON_JAVA_SPRING_BOOT.getServiceType());
		when(service.getId()).thenReturn(RandomUtils.nextInt(1, 10));

		when(this.iLibraryManagerClient.checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()))).thenReturn(true);

		//THEN
		libraryManagerService.checkPublishedLibrariesByDeploymentService(service, environment.toString());

		//VERIFY
		verify(iLibraryManagerClient).checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()));
	}

	@Test
	public void checkPublishedLibrariesByDeploymentServiceWithLibraryTest()
	{
		//WHEN
		Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
		DeploymentService service = mock(DeploymentService.class);

		ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
		when(service.getService()).thenReturn(releaseVersionService);

		when(releaseVersionService.getServiceType()).thenReturn(ServiceType.LIBRARY_JAVA.getServiceType());
		when(service.getId()).thenReturn(RandomUtils.nextInt(1, 10));

		when(this.iLibraryManagerClient.checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()))).thenReturn(true);

		//THEN
		libraryManagerService.checkPublishedLibrariesByDeploymentService(service, environment.toString());

		//VERIFY
		verify(iLibraryManagerClient, times(0)).checkUsedLibrariesAvailability(anyInt(), eq(environment.toString()));
	}

	@Test
	public void getLibraryUsagesTest()
	{
		//WHEN
		Integer id = Integer.valueOf(RandomUtils.nextInt(1, 10));
		when(this.iLibraryManagerClient.getLibraryUsages(id)).thenReturn(new LMUsageDTO[0]);

		//THEN
		libraryManagerService.getLibraryUsages(id);

		//VERIFY
		verify(iLibraryManagerClient).getLibraryUsages(id);
	}

	@Test
	public void getAllRequirementsOfUsedLibrariesTest()
	{
		//WHEN
		int[] rvsServiceIdArray = new Random().ints(10, 1, 99999).toArray();
		when(this.iLibraryManagerClient.getAllRequirementsOfUsedLibraries(rvsServiceIdArray)).thenReturn(new LMLibraryRequirementsDTO[0]);

		//THEN
		libraryManagerService.getAllRequirementsOfUsedLibraries(rvsServiceIdArray);

		//VERIFY
		verify(iLibraryManagerClient).getAllRequirementsOfUsedLibraries(rvsServiceIdArray);
	}

}
