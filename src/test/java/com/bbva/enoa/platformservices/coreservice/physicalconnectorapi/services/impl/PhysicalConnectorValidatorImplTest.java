package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.impl;

import com.bbva.enoa.apirestgen.physicalconnectorapi.model.ConnectorPortInfo;
import com.bbva.enoa.apirestgen.physicalconnectorapi.model.NewPhysicalConnectorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorTypeProperty;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnectorPort;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.connector.enumerates.PhysicalConnectorStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CPDRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConnectorTypePropertyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConnectorTypeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.LogicalConnectorRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ManagementActionTaskRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PhysicalConnectorPortRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PhysicalConnectorRepository;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PhysicalConnectorValidatorImplTest
{

    @Mock
    private PhysicalConnectorRepository physicalConnectorRepository;
    @Mock
    private ConnectorTypeRepository connectorTypeRepository;
    @Mock
    private CPDRepository cpdRepository;
    @Mock
    private ConnectorTypePropertyRepository connectorTypePropertyRepository;
    @Mock
    private ManagementActionTaskRepository managementActionTaskRepository;
    @Mock
    private LogicalConnectorRepository logicalConnectorRepository;
    @Mock
    private PhysicalConnectorPortRepository physicalConnectorPortRepository;
    @InjectMocks
    private PhysicalConnectorValidatorImpl physicalConnectorValidator;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void validateAndGetPhysicalConnector()
    {
        PhysicalConnector physicalConnector = new PhysicalConnector();
        when(this.physicalConnectorRepository.findById(1)).thenReturn(Optional.of(physicalConnector));
        PhysicalConnector response = this.physicalConnectorValidator.validateAndGetPhysicalConnector(1);
        assertEquals(physicalConnector, response);
        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateAndGetPhysicalConnector(2)
        );
    }

    @Test
    public void validateConnectorTypePropertyName()
    {
        ConnectorTypeProperty property = new ConnectorTypeProperty();
        property.setName("TYPE.Property2");
        ConnectorType connectorType = new ConnectorType();
        connectorType.setName("Type");
        connectorType.getConnectorTypeProperties().add(property);
        this.physicalConnectorValidator.validateConnectorTypePropertyName(connectorType, "Property");

        property.setName("TYPE.Property");
        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateConnectorTypePropertyName(connectorType, "Property")
        );
    }

    @Test
    public void validateConnectorTypeProperty()
    {
        ConnectorTypeProperty connectorTypeProperty = new ConnectorTypeProperty();
        when(this.connectorTypePropertyRepository.findById(1)).thenReturn(Optional.of(connectorTypeProperty));
        ConnectorTypeProperty response = this.physicalConnectorValidator.validateConnectorTypeProperty(1);
        assertEquals(connectorTypeProperty, response);
        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateConnectorTypeProperty(2)
        );
    }

    @Test
    public void validateConnectorType()
    {
        ConnectorType connectorType = new ConnectorType();
        when(this.connectorTypeRepository.findByName("Type")).thenReturn(connectorType);
        ConnectorType response = this.physicalConnectorValidator.validateConnectorType("Type");
        assertEquals(connectorType, response);
        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateConnectorType("Type2")
        );
    }

    @Test
    public void validateConnectorType1()
    {
        ConnectorType connectorType = new ConnectorType();
        when(this.connectorTypeRepository.findById(1)).thenReturn(Optional.of(connectorType));
        ConnectorType response = this.physicalConnectorValidator.validateConnectorType(1);
        assertEquals(connectorType, response);
        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateConnectorType(2)
        );
    }
    @Test
    public void validatePhysicalConnectorName()
    {
        PhysicalConnector physicalConnector = new PhysicalConnector();
        String response = this.physicalConnectorValidator.validatePhysicalConnectorName("Name", "INT");
        assertEquals("Name", response);
        when(this.physicalConnectorRepository.findByEnvironmentAndName(Environment.INT.getEnvironment(), "Name")).thenReturn(physicalConnector);
        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validatePhysicalConnectorName("Name", "INT")
        );
    }

    @Test
    public void validatePortRange()
    {
        this.physicalConnectorValidator.validatePortRange("30000");
        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validatePortRange("-30000")
        );

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validatePortRange("aaa")
        );
    }

    @Test
    public void isConnectorTypeRemovable()
    {
        List<LogicalConnector> logicalConnectorList = new ArrayList<>();
        when(this.logicalConnectorRepository.findByConnectorTypeName("Type")).thenReturn(logicalConnectorList);
        List<PhysicalConnector> physicalConnectorList = new ArrayList<>();
        when(this.physicalConnectorRepository.findByConnectorTypeName("Type")).thenReturn(physicalConnectorList);
        this.physicalConnectorValidator.isConnectorTypeRemovable("Type");
        logicalConnectorList.add(new LogicalConnector());

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.isConnectorTypeRemovable("Type")
        );
    }

    @Test
    public void isConnectorTypeRemovableError()
    {
        List<LogicalConnector> logicalConnectorList = new ArrayList<>();
        when(this.logicalConnectorRepository.findByConnectorTypeName("Type")).thenReturn(logicalConnectorList);
        List<PhysicalConnector> physicalConnectorList = new ArrayList<>();
        when(this.physicalConnectorRepository.findByConnectorTypeName("Type")).thenReturn(physicalConnectorList);
        this.physicalConnectorValidator.isConnectorTypeRemovable("Type");
        physicalConnectorList.add(new PhysicalConnector());

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.isConnectorTypeRemovable("Type")
        );
    }

    @Test
    public void validateIfConnectorsCanBeAssociated()
    {
        ConnectorType connectorType = new ConnectorType();
        connectorType.setName("Type");
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setConnectorType(connectorType);
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        PhysicalConnector physicalConnector = new PhysicalConnector();
        physicalConnector.setPhysicalConnectorStatus(PhysicalConnectorStatus.CREATED);
        physicalConnector.setConnectorType(connectorType);
        physicalConnector.setEnvironment(Environment.INT.getEnvironment());
        PhysicalConnectorPort physicalConnectorPort = new PhysicalConnectorPort();
        this.physicalConnectorValidator.validateIfConnectorsCanBeAssociated(logicalConnector, physicalConnector, physicalConnectorPort);
        physicalConnector.setPhysicalConnectorStatus(PhysicalConnectorStatus.PENDING_PORT);

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateIfConnectorsCanBeAssociated(logicalConnector, physicalConnector, physicalConnectorPort)
        );
    }

    @Test
    public void validateIfConnectorsCanBeAssociatedError()
    {
        ConnectorType connectorType = new ConnectorType();
        connectorType.setName("Type");
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATE_ERROR);
        logicalConnector.setConnectorType(connectorType);
        PhysicalConnector physicalConnector = new PhysicalConnector();
        physicalConnector.setPhysicalConnectorStatus(PhysicalConnectorStatus.CREATED);
        physicalConnector.setConnectorType(connectorType);
        PhysicalConnectorPort physicalConnectorPort = new PhysicalConnectorPort();

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateIfConnectorsCanBeAssociated(logicalConnector, physicalConnector, physicalConnectorPort)
        );
    }

    @Test
    public void validateIfConnectorsCanBeAssociatedError2()
    {
        ConnectorType connectorType = new ConnectorType();
        connectorType.setName("Type");
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setConnectorType(connectorType);
        PhysicalConnector physicalConnector = new PhysicalConnector();
        physicalConnector.setPhysicalConnectorStatus(PhysicalConnectorStatus.CREATED);
        physicalConnector.setConnectorType(connectorType);
        PhysicalConnectorPort physicalConnectorPort = new PhysicalConnectorPort();
        logicalConnector.setPhysicalConnector(physicalConnector);
        logicalConnector.setPhysicalConnectorPort(physicalConnectorPort);

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateIfConnectorsCanBeAssociated(logicalConnector, physicalConnector, physicalConnectorPort)
        );
    }

    @Test
    public void validateIfConnectorsCanBeAssociatedError3()
    {
        ConnectorType connectorType = new ConnectorType();
        connectorType.setName("Type");
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setConnectorType(connectorType);
        PhysicalConnector physicalConnector = new PhysicalConnector();
        physicalConnector.setPhysicalConnectorStatus(PhysicalConnectorStatus.CREATED);
        physicalConnector.setConnectorType(connectorType);
        PhysicalConnectorPort physicalConnectorPort = new PhysicalConnectorPort();
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        physicalConnector.setEnvironment(Environment.PRE.getEnvironment());

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateIfConnectorsCanBeAssociated(logicalConnector, physicalConnector, physicalConnectorPort)
        );
    }

    @Test
    public void validateIfConnectorsCanBeAssociatedError4()
    {
        ConnectorType connectorType = new ConnectorType();
        connectorType.setName("Type");
        ConnectorType connectorType2 = new ConnectorType();
        connectorType2.setName("Type2");
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setConnectorType(connectorType);
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        PhysicalConnector physicalConnector = new PhysicalConnector();
        physicalConnector.setPhysicalConnectorStatus(PhysicalConnectorStatus.CREATED);
        physicalConnector.setConnectorType(connectorType2);
        physicalConnector.setEnvironment(Environment.INT.getEnvironment());
        PhysicalConnectorPort physicalConnectorPort = new PhysicalConnectorPort();

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateIfConnectorsCanBeAssociated(logicalConnector, physicalConnector, physicalConnectorPort)
        );
    }

    @Test
    public void validateAndGetPhysicalConnectorPort()
    {
        PhysicalConnectorPort physicalConnectorPort = new PhysicalConnectorPort();
        when(this.physicalConnectorPortRepository.findById(1)).thenReturn(Optional.of(physicalConnectorPort));
        PhysicalConnectorPort response = this.physicalConnectorValidator.validateAndGetPhysicalConnectorPort(1);
        assertEquals(physicalConnectorPort, response);

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateAndGetPhysicalConnectorPort(2)
        );
    }

    @Test
    public void validateIfConnectorsCanBeDisassociated()
    {
        Product product = new Product();
        PhysicalConnector physicalConnector = new PhysicalConnector();
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setProduct(product);
        logicalConnector.setPhysicalConnector(physicalConnector);
        this.physicalConnectorValidator.validateIfConnectorsCanBeDisassociated(logicalConnector, physicalConnector);

        logicalConnector.setPhysicalConnector(null);
        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateIfConnectorsCanBeDisassociated(logicalConnector, physicalConnector)
        );
    }

    @Test
    public void validateIfConnectorsCanBeDisassociatedWithDeployedLogicalConector()
    {
        validateIfConnectorsCanBeDisassociatedWithSelectedLogicalConector(DeploymentStatus.DEPLOYED);
    }

    @Test
    public void validateIfConnectorsCanBeDisassociatedWithDefinitionLogicalConector()
    {
        validateIfConnectorsCanBeDisassociatedWithSelectedLogicalConector(DeploymentStatus.DEFINITION);
    }

    @Test
    public void validateIfConnectorsCanBeDisassociatedWithStoragedLogicalConector()
    {
        validateIfConnectorsCanBeDisassociatedWithSelectedLogicalConector(DeploymentStatus.STORAGED);
    }

    private void validateIfConnectorsCanBeDisassociatedWithSelectedLogicalConector(DeploymentStatus deploymentStatus)
    {
        Product product = new Product();
        PhysicalConnector physicalConnector = new PhysicalConnector();
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setProduct(product);
        logicalConnector.setPhysicalConnector(physicalConnector);

        DeploymentService deploymentService = new DeploymentService();
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setStatus(deploymentStatus);

        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);

        logicalConnector.getDeploymentServices().add(deploymentService);

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateIfConnectorsCanBeDisassociated(logicalConnector, physicalConnector)
        );
    }

    @Test
    public void validateIfConnectorsCanBeDisassociatedWithPendingCheckPropertyTodoTask()
    {
        Product product = new Product();
        PhysicalConnector physicalConnector = new PhysicalConnector();
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setProduct(product);
        logicalConnector.setPhysicalConnector(physicalConnector);

        ManagementActionTask managementActionTask = new ManagementActionTask();
        List<ManagementActionTask> checkConnectorPropertiesTaskList = Arrays.asList(managementActionTask);
        when(this.managementActionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(any(), any(), any()))
                .thenReturn(checkConnectorPropertiesTaskList);

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateIfConnectorsCanBeDisassociated(logicalConnector, physicalConnector)
        );
    }

    @Test
    public void validateAndGetLogicalConnector()
    {
        LogicalConnector logicalConnector = new LogicalConnector();
        when(this.logicalConnectorRepository.findById(1)).thenReturn(Optional.of(logicalConnector));
        LogicalConnector response = this.physicalConnectorValidator.validateAndGetLogicalConnector(1);
        assertEquals(logicalConnector, response);

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateAndGetLogicalConnector(2)
        );
    }

    @Test
    public void validateNewPhysicalConnectorPort()
    {
        PhysicalConnector physicalConnector = new PhysicalConnector();
        ConnectorPortInfo connectorPortInfo = new ConnectorPortInfo();
        connectorPortInfo.setInputPort(2);
        PhysicalConnectorPort physicalConnectorPort = new PhysicalConnectorPort();
        physicalConnectorPort.setPortName("Name");
        physicalConnectorPort.setInputPort(1);
        physicalConnector.getConnectorPortList().add(physicalConnectorPort);
        this.physicalConnectorValidator.validateNewPhysicalConnectorPort(physicalConnector, connectorPortInfo);
        connectorPortInfo.setPortName("Name");

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateNewPhysicalConnectorPort(physicalConnector, connectorPortInfo)
        );
    }

    @Test
    public void validateNewPhysicalConnectorPortError()
    {
        PhysicalConnector physicalConnector = new PhysicalConnector();
        ConnectorPortInfo connectorPortInfo = new ConnectorPortInfo();
        connectorPortInfo.setInputPort(2);
        PhysicalConnectorPort physicalConnectorPort = new PhysicalConnectorPort();
        physicalConnectorPort.setPortName("Name");
        physicalConnectorPort.setInputPort(2);
        physicalConnector.getConnectorPortList().add(physicalConnectorPort);

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateNewPhysicalConnectorPort(physicalConnector, connectorPortInfo)
        );
    }

    @Test
    public void validateNewConnectorType()
    {
        this.physicalConnectorValidator.validateNewConnectorType("Type");
        ConnectorType connectorType = new ConnectorType();
        when(this.connectorTypeRepository.findByName("TYPE")).thenReturn(connectorType);

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validateNewConnectorType("Type")
        );
    }

    @Test
    public void validatePhysicalConnectorCanBeDeleted()
    {
        PhysicalConnector physicalConnector = new PhysicalConnector();
        this.physicalConnectorValidator.validatePhysicalConnectorCanBeDeleted(physicalConnector);
        physicalConnector.getLogicalConnectors().add(new LogicalConnector());

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validatePhysicalConnectorCanBeDeleted(physicalConnector)
        );
    }

    @Test
    public void validatePhysicalConnectorCanBeDeletedWithDeletionPhysicalConnectorTask()
    {
        PhysicalConnector physicalConnector = new PhysicalConnector();
        physicalConnector.getLogicalConnectors().add(new LogicalConnector());

        ManagementActionTask managementActionTask = new ManagementActionTask();
        List<ManagementActionTask> checkConnectorPropertiesTaskList = Arrays.asList(managementActionTask);
        when(this.managementActionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(any(), any(), any()))
                .thenReturn(checkConnectorPropertiesTaskList);

        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.validatePhysicalConnectorCanBeDeleted(physicalConnector)
        );
    }

    @Test
    public void checkPendingDeletePhysicalConnector()
    {
        PhysicalConnector physicalConnector = new PhysicalConnector();
        physicalConnector.setId(1);
        List<ManagementActionTask> checkDeletionPhysicalConnectorTaskList = new ArrayList<>();
        when(this.managementActionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(physicalConnector.getId(),
                ToDoTaskType.DELETION_PHYSICAL_CONNECTOR_REQUEST, List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR))).thenReturn
                (checkDeletionPhysicalConnectorTaskList);

        Integer response = this.physicalConnectorValidator.checkPendingDeletePhysicalConnector(physicalConnector);

        assertNull(response);

        ManagementActionTask task = new ManagementActionTask();
        task.setId(1);
        checkDeletionPhysicalConnectorTaskList.add(task);
        Integer response2 = this.physicalConnectorValidator.checkPendingDeletePhysicalConnector(physicalConnector);
        assertEquals(1, response2.intValue());
    }

    @Test
    public void checkCreationLogicalConnectorTodoTask()
    {
        Product product = new Product();
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setId(1);
        logicalConnector.setProduct(product);
        boolean response = this.physicalConnectorValidator.checkCreationLogicalConnectorTodoTask(logicalConnector);
        assertFalse(response);
        List<ManagementActionTask> checkConnectorPropertiesTaskList = new ArrayList<>();
        when(this.managementActionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(logicalConnector.getId(),
                ToDoTaskType.CREATION_LOGICAL_CONNECTOR_REQUEST, List.of(ToDoTaskStatus.DONE))).thenReturn(checkConnectorPropertiesTaskList);
        checkConnectorPropertiesTaskList.add(new ManagementActionTask());
        boolean response2 = this.physicalConnectorValidator.checkCreationLogicalConnectorTodoTask(logicalConnector);
        assertTrue(response2);
    }

    @Test
    public void checkNewPhysicalConnectorInputWhenNull()
    {
        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.checkNewPhysicalConnectorInput(null)
        );
    }

    @Test
    public void checkNewPhysicalConnectorInputWhenEnvironmentNull()
    {
        NewPhysicalConnectorDto newPhysicalConnectorDto = new NewPhysicalConnectorDto();
        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.checkNewPhysicalConnectorInput(newPhysicalConnectorDto)
        );
    }

    @Test
    public void checkNewPhysicalConnectorInputWhenConnectorTypeNull()
    {
        NewPhysicalConnectorDto newPhysicalConnectorDto = new NewPhysicalConnectorDto();
        newPhysicalConnectorDto.setEnvironment("env");
        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.checkNewPhysicalConnectorInput(newPhysicalConnectorDto)
        );
    }

    @Test
    public void checkNewPhysicalConnectorInputWhenPhysicalConnectorNameNull()
    {
        NewPhysicalConnectorDto newPhysicalConnectorDto = new NewPhysicalConnectorDto();
        newPhysicalConnectorDto.setEnvironment("env");
        newPhysicalConnectorDto.setConnectorType("connectorType");
        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.checkNewPhysicalConnectorInput(newPhysicalConnectorDto)
        );
    }

    @Test
    public void checkNewPhysicalConnectorInputWhenVirtualIpNull()
    {
        NewPhysicalConnectorDto newPhysicalConnectorDto = new NewPhysicalConnectorDto();
        newPhysicalConnectorDto.setEnvironment("env");
        newPhysicalConnectorDto.setConnectorType("connectorType");
        newPhysicalConnectorDto.setPhysicalConnectorName("physicalConnectorName");
        assertThrows(NovaException.class, () ->
                this.physicalConnectorValidator.checkNewPhysicalConnectorInput(newPhysicalConnectorDto)
        );
    }

    @Test
    public void checkNewPhysicalConnectorInputOk()
    {
        NewPhysicalConnectorDto newPhysicalConnectorDto = new NewPhysicalConnectorDto();
        newPhysicalConnectorDto.setEnvironment("env");
        newPhysicalConnectorDto.setConnectorType("connectorType");
        newPhysicalConnectorDto.setPhysicalConnectorName("physicalConnectorName");
        newPhysicalConnectorDto.setVirtualIp("virtualIp");

        this.physicalConnectorValidator.checkNewPhysicalConnectorInput(newPhysicalConnectorDto);
    }

}