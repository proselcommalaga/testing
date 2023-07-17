package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.impl;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novaheader.context.NovaContext;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.PhysicalConnector;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.CPD;
import com.bbva.enoa.datamodel.model.todotask.entities.ManagementActionTask;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskStatus;
import com.bbva.enoa.datamodel.model.todotask.enumerates.ToDoTaskType;
import com.bbva.enoa.platformservices.coreservice.common.repositories.CPDRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConnectorTypeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.LogicalConnectorRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ManagementActionTaskRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class LogicalConnectorValidatorImplTest
{
    @Mock
    private ProductRepository productRepository;
    @Mock
    private LogicalConnectorRepository logicalConnectorRepository;
    @Mock
    private ConnectorTypeRepository connectorTypeRepository;
    @Mock
    private CPDRepository cpdRepository;
    @Mock
    private ManagementActionTaskRepository managementActionTaskRepository;
    @Mock
    private ManageValidationUtils manageValidationUtils;
    @Mock
    private NovaContext novaContext;
    @InjectMocks
    private LogicalConnectorValidatorImpl logicalConnectorValidator;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void validateAndGetProduct()
    {
        Product product = new Product();
        when(this.productRepository.findById(1)).thenReturn(Optional.of(product));
        Product response = this.logicalConnectorValidator.validateAndGetProduct(1);
        assertEquals(product, response);
        assertThrows(NovaException.class, () ->
            this.logicalConnectorValidator.validateAndGetProduct(2)
        );
    }

    @Test
    public void validateAndGetLogicalConnector()
    {
        LogicalConnector logicalConnector = new LogicalConnector();
        when(this.logicalConnectorRepository.findById(1)).thenReturn(Optional.of(logicalConnector));
        LogicalConnector response = this.logicalConnectorValidator.validateAndGetLogicalConnector(1);
        assertEquals(logicalConnector, response);
        assertThrows(NovaException.class, () ->
            this.logicalConnectorValidator.validateAndGetLogicalConnector(2)
        );
    }

    @Test
    public void validateLogicalConnectorCreatedStatus()
    {
        PhysicalConnector physicalConnector = new PhysicalConnector();
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setPhysicalConnector(physicalConnector);
        this.logicalConnectorValidator.validateLogicalConnectorCreatedStatus(logicalConnector);
    }

    @Test
    public void validateLogicalConnectorNotCreatedStatus()
    {
        PhysicalConnector physicalConnector = new PhysicalConnector();
        LogicalConnector logicalConnector = new LogicalConnector();

        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.ARCHIVED);
        assertThrows(NovaException.class, () ->
                this.logicalConnectorValidator.validateLogicalConnectorCreatedStatus(logicalConnector)
        );
    }

    @Test
    public void validateLogicalConnectorCreatedStatusWithNullPhysicalConnector()
    {
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);

        logicalConnector.setPhysicalConnector(null);
        assertThrows(NovaException.class, () ->
                this.logicalConnectorValidator.validateLogicalConnectorCreatedStatus(logicalConnector)
        );
    }

    @Test
    public void validateLogicalConnectorName()
    {
        String response1 = this.logicalConnectorValidator.validateLogicalConnectorName("Name", "INT", 1);
        assertEquals("Name", response1);

        LogicalConnector logicalConnector = new LogicalConnector();
        when(this.logicalConnectorRepository.findByProductIdAndEnvironmentAndName(1, Environment.INT.getEnvironment(), "Name")).thenReturn(logicalConnector);

        assertThrows(NovaException.class, () ->
        this.logicalConnectorValidator.validateLogicalConnectorName("Name", "INT", 1)
        );
    }

    @Test
    public void validateConnectorType()
    {
        ConnectorType connectorType = new ConnectorType();
        when(this.connectorTypeRepository.findByName("Type")).thenReturn(connectorType);
        ConnectorType response = this.logicalConnectorValidator.validateConnectorType("Type");
        assertEquals(connectorType, response);

        assertThrows(NovaException.class, () ->
            this.logicalConnectorValidator.validateConnectorType("Type2")
        );
    }

    @Test
    public void canRequestCheckPropertiesTask()
    {
        this.logicalConnectorValidator.canRequestCheckPropertiesTask(LogicalConnectorStatus.CREATED);
        assertThrows(NovaException.class, () ->
            this.logicalConnectorValidator.canRequestCheckPropertiesTask(LogicalConnectorStatus.ARCHIVED)
        );
    }

    @Test
    public void checkPendingTodoTaskByLogicalConnector()
    {
        Product product = new Product();
        List<ManagementActionTask> creationConnectorTaskList = new ArrayList<>();
        when(this.managementActionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(1,
                ToDoTaskType.CREATION_LOGICAL_CONNECTOR_REQUEST, List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR))).thenReturn(creationConnectorTaskList);
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setId(1);
        logicalConnector.setProduct(product);
        List<ManagementActionTask> checkConnectorPropertiesTaskList = new ArrayList<>();
        when(this.managementActionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(1,
                ToDoTaskType.CHECK_LOGICAL_CONNECTOR_PROPERTIES, List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR))).thenReturn(checkConnectorPropertiesTaskList);

        this.logicalConnectorValidator.checkPendingTodoTaskByLogicalConnector(logicalConnector);

        ManagementActionTask task = new ManagementActionTask();
        checkConnectorPropertiesTaskList.add(task);
        assertThrows(NovaException.class, () ->
            this.logicalConnectorValidator.checkPendingTodoTaskByLogicalConnector(logicalConnector)
        );
    }

    @Test
    public void checkPendingPropertyTask()
    {
        Product product = new Product();
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setId(1);
        logicalConnector.setProduct(product);
        List<ManagementActionTask> checkConnectorPropertiesTaskList = new ArrayList<>();
        when(this.managementActionTaskRepository.findByRelatedIdAndTaskTypeAndStatusIn(1, ToDoTaskType.CHECK_LOGICAL_CONNECTOR_PROPERTIES,
                List.of(ToDoTaskStatus.PENDING, ToDoTaskStatus.PENDING_ERROR))).thenReturn(checkConnectorPropertiesTaskList);

        Integer response1 = this.logicalConnectorValidator.checkPendingPropertyTask(logicalConnector);
        assertNull(response1);

        ManagementActionTask task = new ManagementActionTask();
        task.setId(1);
        checkConnectorPropertiesTaskList.add(task);
        Integer response2 = this.logicalConnectorValidator.checkPendingPropertyTask(logicalConnector);
        assertEquals(1, response2.intValue());
    }

    @Test
    public void checkIfLogicalConnectorIsFrozenWithoutEnvironment()
    {
        LogicalConnector logicalConnector = new LogicalConnector();
        this.logicalConnectorValidator.checkIfLogicalConnectorIsFrozen(logicalConnector);
    }

    @Test
    public void checkIfLogicalConnectorIsFrozenWithNotPreEnvironment()
    {
        Environment env = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        while (env.equals(Environment.PRE))
        {
            env = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        }

        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(env.getEnvironment());

        this.logicalConnectorValidator.checkIfLogicalConnectorIsFrozen(logicalConnector);
    }

    @Test
    public void checkIfLogicalConnectorIsFrozenWithPreEnvironmentAllPlansManagedByUser()
    {
        //WHEN
        Environment env = Environment.PRE;

        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(env.getEnvironment());

        logicalConnector.setDeploymentServices(createDeploymentServiceList());

        when(this.novaContext.getIvUser()).thenReturn(RandomStringUtils.randomAlphabetic(10));
        when(this.manageValidationUtils.checkIfPlanCanBeManagedByUser(any(), any())).thenReturn(true);

        //THEN
        this.logicalConnectorValidator.checkIfLogicalConnectorIsFrozen(logicalConnector);
    }

    @Test
    public void checkIfLogicalConnectorIsFrozenWithPreEnvironmentAllPlansNotManagedByUserAndPlanStatusNotDeployed()
    {
        //WHEN
        Environment env = Environment.PRE;

        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(env.getEnvironment());

        //Create the deploymentServiceList with all the deploymentPlan Status != DEPLOYED
        logicalConnector.setDeploymentServices(
                changeAllDeploymentPlanStatusToNotDeployed(
                        createDeploymentServiceList()));

        when(this.novaContext.getIvUser()).thenReturn(RandomStringUtils.randomAlphabetic(10));
        when(this.manageValidationUtils.checkIfPlanCanBeManagedByUser(any(), any())).thenReturn(false);

        //THEN
        this.logicalConnectorValidator.checkIfLogicalConnectorIsFrozen(logicalConnector);
    }

    @Test
    public void checkIfLogicalConnectorIsFrozenWithPreEnvironmentAllPlansNotManagedByUserAndOnePlanStatusDeployed()
    {
        //WHEN
        Environment env = Environment.PRE;

        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(env.getEnvironment());

        //Create the deploymentServiceList with one deploymentPlan Status == DEPLOYED
        logicalConnector.setDeploymentServices(
                changeOneDeploymentPlanStatusToDeployed(
                        createDeploymentServiceList()));

        when(this.novaContext.getIvUser()).thenReturn(RandomStringUtils.randomAlphabetic(10));
        when(this.manageValidationUtils.checkIfPlanCanBeManagedByUser(any(), any())).thenReturn(false);

        //THEN
        assertThrows(NovaException.class, () ->
            this.logicalConnectorValidator.checkIfLogicalConnectorIsFrozen(logicalConnector)
        );
    }

    private List<DeploymentService> createDeploymentServiceList()
    {
        List<DeploymentService> deploymentServiceList = new ArrayList<>();

        for(int i = 0; i < RandomUtils.nextInt(1, 10); i++)
        {
            DeploymentService deploymentService = new DeploymentService();
            deploymentServiceList.add(deploymentService);

            DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
            deploymentService.setDeploymentSubsystem(deploymentSubsystem);

            DeploymentPlan deploymentPlan = new DeploymentPlan();
            deploymentSubsystem.setDeploymentPlan(deploymentPlan);
        }

        return deploymentServiceList;
    }

    private List<DeploymentService> changeAllDeploymentPlanStatusToNotDeployed(List<DeploymentService> deploymentServiceList)
    {
        deploymentServiceList.stream().
                forEach(deploymentService -> {
                    DeploymentStatus de = DeploymentStatus.values()[RandomUtils.nextInt(0, DeploymentStatus.values().length)];
                    if (de == DeploymentStatus.DEPLOYED)
                    {
                        de = DeploymentStatus.DEFINITION;
                    }
                    deploymentService.getDeploymentSubsystem().getDeploymentPlan().
                        setStatus(de);
                });
        return deploymentServiceList;
    }

    private List<DeploymentService> changeOneDeploymentPlanStatusToDeployed(List<DeploymentService> deploymentServiceList)
    {
        deploymentServiceList.get(RandomUtils.nextInt(0, deploymentServiceList.size())).getDeploymentSubsystem().
            getDeploymentPlan().setStatus(DeploymentStatus.DEPLOYED);

        return deploymentServiceList;
    }
}