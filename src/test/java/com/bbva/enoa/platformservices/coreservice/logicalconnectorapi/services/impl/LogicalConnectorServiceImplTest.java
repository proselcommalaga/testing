package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.impl;

import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LogicalConnectorDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.NewLogicalConnectorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorTypeProperty;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnectorPort;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.connector.enumerates.PhysicalConnectorStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConnectorTypeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentChangeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.LogicalConnectorPropertyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.LogicalConnectorRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.UserValidationService;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IProductUsersClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.exceptions.DeploymentError;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces.ILogicalConnectorBuilder;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces.ILogicalConnectorRepositoriesService;
import com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.interfaces.ILogicalConnectorValidator;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LogicalConnectorServiceImplTest
{
    @Mock
    private LogicalConnectorRepository logicalConnectorRepository;
    @Mock
    private ConnectorTypeRepository connectorTypeRepository;
    @Mock
    private ILogicalConnectorValidator iLogicalConnectorValidator;
    @Mock
    private ILogicalConnectorBuilder iLogicalConnectorBuilder;
    @Mock
    private TodoTaskServiceClient todoTaskServiceClient;
    @Mock
    private LogicalConnectorPropertyRepository logicalConnectorPropertyRepository;
    @Mock
    private ILogicalConnectorRepositoriesService iLogicalConnectorRepositoriesService;
    @Mock
    private DeploymentChangeRepository deploymentChangeRepository;
    @Mock
    private IProductUsersClient usersService;
    @Mock
    private UserValidationService userValidationService;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;

    @InjectMocks
    private LogicalConnectorServiceImpl logicalConnectorService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(this.logicalConnectorService, "connectionTestTimeout", 5000);
    }

    @Test
    public void requestProperties()
    {
        Product product = new Product();
        ConnectorType connectorType = new ConnectorType();
        connectorType.setName("Type");
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setName("Name");
        logicalConnector.setProduct(product);
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setConnectorType(connectorType);
        when(this.iLogicalConnectorValidator.validateAndGetLogicalConnector(1)).thenReturn(logicalConnector);
        when(this.todoTaskServiceClient.createManagementTask(any(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        doNothing().when(this.novaActivityEmitter).emitNewActivity(any());

        Integer response = this.logicalConnectorService.requestProperties(1, "CODE");

        assertEquals(1, response.intValue());

        doThrow(new NovaException(DeploymentError.getUnexpectedError(), "")).when(this.todoTaskServiceClient).createManagementTask(any(), any(), any(), any(), any(), any(), any());

        assertThrows(NovaException.class, () ->
            this.logicalConnectorService.requestProperties(1, "CODE")
        );
    }

    @Test
    public void getAllFromProduct()
    {
        LogicalConnectorDto[] logicalConnectorDtoArray = new LogicalConnectorDto[0];
        when(this.iLogicalConnectorBuilder.buildLogicalConnectorDtoList(any())).thenReturn(logicalConnectorDtoArray);
        LogicalConnectorDto[] response1 = this.logicalConnectorService.getAllFromProduct(1, null, null);
        assertEquals(logicalConnectorDtoArray, response1);
        verify(this.logicalConnectorRepository, times(1)).findByProductId(1);
        LogicalConnectorDto[] response2 = this.logicalConnectorService.getAllFromProduct(1, null, "INT");
        assertEquals(logicalConnectorDtoArray, response2);
        verify(this.logicalConnectorRepository, times(1)).findByProductIdAndEnvironment(1, Environment.INT.getEnvironment());
        LogicalConnectorDto[] response3 = this.logicalConnectorService.getAllFromProduct(1, "Type", null);
        assertEquals(logicalConnectorDtoArray, response3);
        verify(this.logicalConnectorRepository, times(1)).findByProductIdAndConnectorTypeName(1, "Type");
        LogicalConnectorDto[] response4 = this.logicalConnectorService.getAllFromProduct(1, "Type", "INT");
        assertEquals(logicalConnectorDtoArray, response4);
        verify(this.logicalConnectorRepository, times(1)).findByProductIdAndEnvironmentAndConnectorTypeName(1, Environment.INT.getEnvironment(), "Type");
    }

    @Test
    public void deleteLogicalConnector()
    {
        PhysicalConnectorPort physicalConnectorPort = new PhysicalConnectorPort();
        PhysicalConnector physicalConnector = new PhysicalConnector();
        physicalConnector.setPhysicalConnectorStatus(PhysicalConnectorStatus.CREATED);
        Product product = new Product();
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        DeploymentPlan plan = new DeploymentPlan();
        plan.setReleaseVersion(releaseVersion);
        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setDeploymentPlan(plan);
        DeploymentService service = new DeploymentService();
        service.setService(releaseVersionService);
        service.setDeploymentSubsystem(subsystem);
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setProduct(product);
        ConnectorType type = new ConnectorType();
        logicalConnector.setConnectorType(type);
        logicalConnector.setPhysicalConnector(physicalConnector);
        logicalConnector.setPhysicalConnectorPort(physicalConnectorPort);
        logicalConnector.getDeploymentServices().add(service);
        when(this.iLogicalConnectorValidator.validateAndGetLogicalConnector(1)).thenReturn(logicalConnector);

        LogicalConnector response = this.logicalConnectorService.deleteLogicalConnector(1, "CODE");
        assertEquals(logicalConnector, response);
    }

    @Test
    public void deleteLogicalConnectorWithArchivedStatus()
    {
        Product product = new Product();
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.ARCHIVED);
        logicalConnector.setProduct(product);
        when(this.iLogicalConnectorValidator.validateAndGetLogicalConnector(1)).thenReturn(logicalConnector);

        assertThrows(NovaException.class, () ->
            this.logicalConnectorService.deleteLogicalConnector(1, "CODE")
        );
    }

    @Test
    public void deleteLogicalConnectorWithDeployedDeploymentPlan()
    {
        deleteLogicalConnectorWithVariableDeploymentPlanWithNovaException(DeploymentStatus.DEPLOYED);
    }

    @Test
    public void deleteLogicalConnectorWithScheduledDeploymentPlan()
    {
        deleteLogicalConnectorWithVariableDeploymentPlanWithNovaException(DeploymentStatus.SCHEDULED);
    }

    private void deleteLogicalConnectorWithVariableDeploymentPlanWithNovaException(DeploymentStatus status)
    {
        Product product = new Product();
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        DeploymentPlan plan = new DeploymentPlan();
        plan.setStatus(status);
        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setDeploymentPlan(plan);
        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setService(releaseVersionService);
        deploymentService.setDeploymentSubsystem(subsystem);
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setProduct(product);
        ConnectorType type = new ConnectorType();
        logicalConnector.setConnectorType(type);
        logicalConnector.getDeploymentServices().add(deploymentService);
        when(this.iLogicalConnectorValidator.validateAndGetLogicalConnector(1)).thenReturn(logicalConnector);

        assertThrows(NovaException.class, () ->
                this.logicalConnectorService.deleteLogicalConnector(1, "CODE")
        );
    }

    @Test
    public void archiveLogicalConnector()
    {
        PhysicalConnectorPort physicalConnectorPort = new PhysicalConnectorPort();
        PhysicalConnector physicalConnector = new PhysicalConnector();
        physicalConnector.setPhysicalConnectorStatus(PhysicalConnectorStatus.CREATED);
        Product product = new Product();
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        DeploymentPlan plan = new DeploymentPlan();
        plan.setStatus(DeploymentStatus.STORAGED);
        plan.setReleaseVersion(releaseVersion);
        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setDeploymentPlan(plan);
        DeploymentService service = new DeploymentService();
        service.setService(releaseVersionService);
        service.setDeploymentSubsystem(subsystem);
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setProduct(product);
        ConnectorType type = new ConnectorType();
        logicalConnector.setConnectorType(type);
        logicalConnector.setPhysicalConnector(physicalConnector);
        logicalConnector.setPhysicalConnectorPort(physicalConnectorPort);
        logicalConnector.getDeploymentServices().add(service);
        when(this.iLogicalConnectorValidator.validateAndGetLogicalConnector(1)).thenReturn(logicalConnector);
        when(this.logicalConnectorRepository.saveAndFlush(logicalConnector)).thenReturn(logicalConnector);
        doNothing().when(this.novaActivityEmitter).emitNewActivity(any());

        LogicalConnector response = this.logicalConnectorService.archiveLogicalConnector(1, "CODE");

        assertEquals(logicalConnector, response);
        verify(this.iLogicalConnectorValidator).validateAndGetLogicalConnector(1);
        verify(this.logicalConnectorRepository).saveAndFlush(logicalConnector);
        verify(this.novaActivityEmitter).emitNewActivity(any());
    }

    private LogicalConnectorStatus getLogicalConnectorStatusWithNotParameterValue(LogicalConnectorStatus forbiddenStatus)
    {
        LogicalConnectorStatus lce = null;
        while (lce == forbiddenStatus)
        {
            lce = LogicalConnectorStatus.values()[RandomUtils.nextInt(0, LogicalConnectorStatus.values().length)];
        }
        return lce;
    }

    @Test
    public void archiveLogicalConnectorWithNotCreatedStatus()
    {
        LogicalConnectorStatus lce = getLogicalConnectorStatusWithNotParameterValue(LogicalConnectorStatus.CREATED);

        Product product = new Product();
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        logicalConnector.setLogicalConnectorStatus(lce);
        logicalConnector.setProduct(product);
        when(this.iLogicalConnectorValidator.validateAndGetLogicalConnector(1)).thenReturn(logicalConnector);

        assertThrows(NovaException.class, () ->
                this.logicalConnectorService.archiveLogicalConnector(1, "CODE")
        );
    }

    @Test
    public void archiveLogicalConnectorWithEmptyDeploymentService()
    {

        Product product = new Product();
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setProduct(product);
        when(this.iLogicalConnectorValidator.validateAndGetLogicalConnector(1)).thenReturn(logicalConnector);

        assertThrows(NovaException.class, () ->
                this.logicalConnectorService.archiveLogicalConnector(1, "CODE")
        );
    }

    @Test
    public void archiveLogicalConnectorWithDeployedDeploymentService()
    {

        Product product = new Product();
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setProduct(product);

        DeploymentPlan plan = new DeploymentPlan();
        plan.setStatus(DeploymentStatus.DEPLOYED);
        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setDeploymentPlan(plan);
        DeploymentService service = new DeploymentService();
        service.setService(new ReleaseVersionService());
        service.setDeploymentSubsystem(subsystem);
        logicalConnector.getDeploymentServices().add(service);

        when(this.iLogicalConnectorValidator.validateAndGetLogicalConnector(1)).thenReturn(logicalConnector);

        assertThrows(NovaException.class, () ->
                this.logicalConnectorService.archiveLogicalConnector(1, "CODE")
        );
    }

    @Test
    public void archiveLogicalConnectorWithAllPlansInDefinitio()
    {

        Product product = new Product();
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setProduct(product);

        for (int i=0; i < RandomUtils.nextInt(0, 10); i++)
        {
            DeploymentPlan plan = new DeploymentPlan();
            plan.setStatus(DeploymentStatus.DEFINITION);
            DeploymentSubsystem subsystem = new DeploymentSubsystem();
            subsystem.setDeploymentPlan(plan);
            DeploymentService service = new DeploymentService();
            service.setService(new ReleaseVersionService());
            service.setDeploymentSubsystem(subsystem);

            logicalConnector.getDeploymentServices().add(service);
        }

        when(this.iLogicalConnectorValidator.validateAndGetLogicalConnector(1)).thenReturn(logicalConnector);

        assertThrows(NovaException.class, () ->
                this.logicalConnectorService.archiveLogicalConnector(1, "CODE")
        );
    }

    @Test
    public void restoreLogicalConnector()
    {
        PhysicalConnectorPort physicalConnectorPort = new PhysicalConnectorPort();
        PhysicalConnector physicalConnector = new PhysicalConnector();
        physicalConnector.setPhysicalConnectorStatus(PhysicalConnectorStatus.CREATED);
        Product product = new Product();
        product.setId(RandomUtils.nextInt());
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        DeploymentPlan plan = new DeploymentPlan();
        plan.setStatus(DeploymentStatus.STORAGED);
        plan.setReleaseVersion(releaseVersion);
        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setDeploymentPlan(plan);
        DeploymentService service = new DeploymentService();
        service.setService(releaseVersionService);
        service.setDeploymentSubsystem(subsystem);
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        logicalConnector.setProduct(product);
        ConnectorType type = new ConnectorType();
        logicalConnector.setConnectorType(type);
        logicalConnector.setPhysicalConnector(physicalConnector);
        logicalConnector.setPhysicalConnectorPort(physicalConnectorPort);
        logicalConnector.getDeploymentServices().add(service);
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.ARCHIVED);
        when(this.iLogicalConnectorValidator.validateAndGetLogicalConnector(1)).thenReturn(logicalConnector);
        when(this.logicalConnectorRepository.saveAndFlush(logicalConnector)).thenReturn(logicalConnector);
        doNothing().when(this.novaActivityEmitter).emitNewActivity(any());

        LogicalConnector response = this.logicalConnectorService.restoreLogicalConnector(1, "CODE");
        assertEquals(logicalConnector, response);

        verify(this.iLogicalConnectorValidator).validateAndGetLogicalConnector(1);
        verify(this.logicalConnectorRepository).saveAndFlush(logicalConnector);
        verify(this.novaActivityEmitter).emitNewActivity(any());
    }

    @Test
    public void restoreLogicalConnectorWithArchivedStatus()
    {
        LogicalConnectorStatus lce = getLogicalConnectorStatusWithNotParameterValue(LogicalConnectorStatus.ARCHIVED);

        Product product = new Product();
        product.setId(RandomUtils.nextInt());
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        logicalConnector.setProduct(product);
        logicalConnector.setLogicalConnectorStatus(lce);
        when(this.iLogicalConnectorValidator.validateAndGetLogicalConnector(1)).thenReturn(logicalConnector);
        when(this.logicalConnectorRepository.saveAndFlush(logicalConnector)).thenReturn(logicalConnector);
        doNothing().when(this.novaActivityEmitter).emitNewActivity(any());

        assertThrows(NovaException.class, () ->
            this.logicalConnectorService.restoreLogicalConnector(1, "CODE")
        );
    }

    @Test
    public void getConnectorTypes()
    {
        ConnectorType connectorType = new ConnectorType();
        connectorType.setName("Name");
        List<ConnectorType> connectorTypeList = new ArrayList<>();
        connectorTypeList.add(connectorType);
        when(this.connectorTypeRepository.findAll()).thenReturn(connectorTypeList);
        String[] response1 = this.logicalConnectorService.getConnectorTypes();
        assertEquals("Name", response1[0]);
        when(this.connectorTypeRepository.findAll()).thenReturn(new ArrayList<>());
        String[] response2 = this.logicalConnectorService.getConnectorTypes();
        assertEquals(0, response2.length);
    }

    @Test
    public void testLogicalConnector()
    {
        PhysicalConnectorPort physicalConnectorPort = new PhysicalConnectorPort();
        PhysicalConnector physicalConnector = new PhysicalConnector();
        physicalConnector.setVirtualIp("IP");
        physicalConnector.setPhysicalConnectorStatus(PhysicalConnectorStatus.CREATED);
        physicalConnector.setVaguadaVirtualIp("IP2");
        Product product = new Product();
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        DeploymentPlan plan = new DeploymentPlan();
        plan.setStatus(DeploymentStatus.STORAGED);
        plan.setReleaseVersion(releaseVersion);
        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setDeploymentPlan(plan);
        DeploymentService service = new DeploymentService();
        service.setService(releaseVersionService);
        service.setDeploymentSubsystem(subsystem);
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setProduct(product);
        ConnectorType type = new ConnectorType();
        logicalConnector.setConnectorType(type);
        logicalConnector.setPhysicalConnector(physicalConnector);
        logicalConnector.setPhysicalConnectorPort(physicalConnectorPort);
        logicalConnector.getDeploymentServices().add(service);
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.ARCHIVED);
        logicalConnector.setName("Name");
        when(this.iLogicalConnectorValidator.validateAndGetLogicalConnector(1)).thenReturn(logicalConnector);

        String response = this.logicalConnectorService.testLogicalConnector(1, "CODE");

        assertTrue(response.contains("Conexión fallida"));
    }

    @Test
    public void getLogicalConnector()
    {
        Product product = new Product();
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setProduct(product);
        LogicalConnectorDto dto = new LogicalConnectorDto();
        when(this.iLogicalConnectorBuilder.buildLogicalConnectorDto(logicalConnector, "CODE")).thenReturn(dto);
        when(this.iLogicalConnectorValidator.validateAndGetLogicalConnector(1)).thenReturn(logicalConnector);
        LogicalConnectorDto response = this.logicalConnectorService.getLogicalConnector(1, "CODE");
        assertEquals(dto, response);
    }

    @Test
    public void createLogicalConnector()
    {
        LogicalConnector logicalConnector = initLogicalConnector();
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());

        when(this.iLogicalConnectorValidator.validateAndGetProduct(1)).thenReturn(logicalConnector.getProduct());
        when(this.todoTaskServiceClient.createManagementTask(any(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        when(this.iLogicalConnectorValidator.validateConnectorType("Type")).thenReturn(new ConnectorType());
        when(this.iLogicalConnectorValidator.validateLogicalConnectorName(any(), any(), any())).thenReturn("Name");
        when(this.logicalConnectorRepository.saveAndFlush(any())).thenReturn(logicalConnector);
        doNothing().when(this.novaActivityEmitter).emitNewActivity(any());


        NewLogicalConnectorDto dto = new NewLogicalConnectorDto();
        dto.setEnvironment("INT");
        dto.setLogicalConnectorName("Name");
        dto.setConnectorType("Type");
        dto.setCpdName("TC");
        dto.setLogicalConnectorDescription("Desc");

        Integer response = this.logicalConnectorService.createLogicalConnector(dto, 1, "CODE");

        assertEquals(1, response.intValue());

        verify(this.iLogicalConnectorValidator).validateAndGetProduct(1);
        verify(this.todoTaskServiceClient).createManagementTask(any(), any(), any(), any(), any(), any(), any());
        verify(this.iLogicalConnectorValidator).validateConnectorType("Type");
        verify(this.iLogicalConnectorValidator).validateLogicalConnectorName(any(), any(), any());
        verify(this.logicalConnectorRepository).saveAndFlush(any());
        verify(this.novaActivityEmitter).emitNewActivity(any());
    }


    @Test
    public void createLogicalConnectorWithProperties()
    {
        LogicalConnector logicalConnector = initLogicalConnector();
        logicalConnector.setEnvironment(Environment.PRO.getEnvironment());

        ConnectorType validateConnectorType = new ConnectorType();
        int numProperties = RandomUtils.nextInt(0, 10);
        validateConnectorType.setConnectorTypeProperties(createConnectorTypePropertyList(numProperties));

        when(this.iLogicalConnectorValidator.validateAndGetProduct(1)).thenReturn(logicalConnector.getProduct());
        when(this.todoTaskServiceClient.createManagementTask(any(), any(), any(), any(), any(), any(), any())).thenReturn(1);
        when(this.iLogicalConnectorValidator.validateConnectorType("Type")).thenReturn(validateConnectorType);
        when(this.iLogicalConnectorValidator.validateLogicalConnectorName(any(), any(), any())).thenReturn("Name");
        when(this.logicalConnectorRepository.saveAndFlush(any())).thenReturn(logicalConnector);
        doNothing().when(this.novaActivityEmitter).emitNewActivity(any());


        NewLogicalConnectorDto dto = new NewLogicalConnectorDto();
        dto.setEnvironment("INT");
        dto.setLogicalConnectorName("Name");
        dto.setConnectorType("Type");
        dto.setCpdName("TC");
        dto.setLogicalConnectorDescription("Desc");

        Integer response = this.logicalConnectorService.createLogicalConnector(dto, 1, "CODE");

        assertEquals(1, response.intValue());

        verify(this.iLogicalConnectorValidator).validateAndGetProduct(1);
        verify(this.todoTaskServiceClient).createManagementTask(any(), any(), any(), any(), any(), any(), any());
        verify(this.iLogicalConnectorValidator).validateConnectorType("Type");
        verify(this.iLogicalConnectorValidator).validateLogicalConnectorName(any(), any(), any());
        verify(this.logicalConnectorRepository).saveAndFlush(any());
        verify(this.novaActivityEmitter).emitNewActivity(any());

        verify(this.logicalConnectorPropertyRepository, times(numProperties)).saveAndFlush(any(LogicalConnectorProperty.class));
    }

    private LogicalConnector initLogicalConnector()
    {
        PhysicalConnectorPort physicalConnectorPort = new PhysicalConnectorPort();
        PhysicalConnector physicalConnector = new PhysicalConnector();
        physicalConnector.setVirtualIp("IP");
        physicalConnector.setPhysicalConnectorStatus(PhysicalConnectorStatus.CREATED);
        Product product = new Product();
        product.setId(1);
        Release release = new Release();
        release.setProduct(product);
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setRelease(release);
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        DeploymentPlan plan = new DeploymentPlan();
        plan.setStatus(DeploymentStatus.STORAGED);
        plan.setReleaseVersion(releaseVersion);
        DeploymentSubsystem subsystem = new DeploymentSubsystem();
        subsystem.setDeploymentPlan(plan);
        DeploymentService service = new DeploymentService();
        service.setService(releaseVersionService);
        service.setDeploymentSubsystem(subsystem);
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setProduct(product);
        ConnectorType type = new ConnectorType();
        logicalConnector.setConnectorType(type);
        logicalConnector.setPhysicalConnector(physicalConnector);
        logicalConnector.setPhysicalConnectorPort(physicalConnectorPort);
        logicalConnector.getDeploymentServices().add(service);
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.ARCHIVED);
        logicalConnector.setName("Name");
        return logicalConnector;
    }

    private List<ConnectorTypeProperty> createConnectorTypePropertyList(int numProperties)
    {
        List<ConnectorTypeProperty> connectorTypePropertyList = new ArrayList<>();
        for (int i=0; i< numProperties; i++)
        {
            ConnectorTypeProperty connectorTypeProperty = new ConnectorTypeProperty();
            connectorTypeProperty.setCpdName(RandomStringUtils.randomAlphabetic(10));
            connectorTypePropertyList.add(connectorTypeProperty);
        }
        return connectorTypePropertyList;
    }

    @Test
    public void isLogicalConnectorFrozen()
    {
        Integer id = RandomUtils.nextInt(1, 10);
        LogicalConnector logicalConnector = new LogicalConnector();
        Optional optional = Optional.of(logicalConnector);
        when(this.logicalConnectorRepository.findById(id)).thenReturn(optional);
        when(this.iLogicalConnectorValidator.isLogicalConnectorFrozen(logicalConnector)).thenReturn(true);

        Boolean result = this.logicalConnectorService.isLogicalConnectorFrozen(id);
        assertTrue(result);
    }

    @Test
    public void isLogicalConnectorFrozenNovaException()
    {
        Integer id = RandomUtils.nextInt(1, 10);
        Optional optional = Optional.empty();
        when(this.logicalConnectorRepository.findById(id)).thenReturn(optional);

        assertThrows(NovaException.class, () ->
            this.logicalConnectorService.isLogicalConnectorFrozen(id)
        );
    }

    @Test
    public void getLogicalConnectorsStatuses()
    {
        String[] result = this.logicalConnectorService.getLogicalConnectorsStatuses();
        assertNotNull(result);
    }
}