package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.impl;

import com.bbva.enoa.apirestgen.configurationmanagerapi.model.CMLogicalConnectorPropertyDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LogicalConnectorDto;
import com.bbva.enoa.apirestgen.logicalconnectorapi.model.LogicalConnectorPropertyDto;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novaheader.context.NovaContext;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
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
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.common.util.ManageValidationUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ConfigurationmanagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class LogicalConnectorBuilderImplTest
{
    @Mock
    private LogicalConnectorValidatorImpl logicalConnectorValidatorImpl;
    @Mock
    private ConfigurationmanagerClient configurationmanagerClient;
    @Mock
    private TodoTaskServiceClient todoTaskServiceClient;
    @Mock
    private ManageValidationUtils manageValidationUtils;
    @Mock
    private NovaContext novaContext;

    @InjectMocks
    private LogicalConnectorBuilderImpl logicalConnectorBuilder;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void buildLogicalConnectorDtoList()
    {
        List<LogicalConnector> logicalConnectorList = createLogicalConnectorList();
        LogicalConnectorDto[] response = this.logicalConnectorBuilder.buildLogicalConnectorDtoList(logicalConnectorList);
        assertEquals(1, response.length);
    }

    @Test
    public void buildLogicalConnectorDtoListWithDeploymentPlanStatusDeployedAndNotManagedByUser()
    {
        List<LogicalConnector> logicalConnectorList = createLogicalConnectorList();

        List<DeploymentService> deploymentServiceList = logicalConnectorList.get(0).getDeploymentServices();
        deploymentServiceList.get(0).getDeploymentSubsystem().
                getDeploymentPlan().setStatus(DeploymentStatus.DEPLOYED);

        when(this.novaContext.getIvUser()).thenReturn(RandomStringUtils.randomAlphabetic(10));
        when(this.manageValidationUtils.checkIfPlanCanBeManagedByUser(anyString(), any())).thenReturn(false);

        LogicalConnectorDto[] response = this.logicalConnectorBuilder.buildLogicalConnectorDtoList(logicalConnectorList);
        assertEquals(1, response.length);
        assertFalse(response[0].getCanBeManagedByUser());
    }

    @Test
    public void buildLogicalConnectorDto()
    {
        LogicalConnectorDto response = this.logicalConnectorBuilder.buildLogicalConnectorDto(createLogicalConnector(), "CODE");
        assertNotNull(response);
    }

    @Test
    public void buildLogicalConnectorDtoWithDeploymentPlanStatusDeployedAndNotManagedByUser()
    {
        LogicalConnector logicalConnector = createLogicalConnector();

        List<DeploymentService> deploymentServiceList = logicalConnector.getDeploymentServices();
        deploymentServiceList.get(0).getDeploymentSubsystem().
                getDeploymentPlan().setStatus(DeploymentStatus.DEPLOYED);

        when(this.manageValidationUtils.checkIfPlanCanBeManagedByUser(anyString(), any())).thenReturn(false);

        LogicalConnectorDto response = this.logicalConnectorBuilder.buildLogicalConnectorDto(logicalConnector, "CODE");

        assertNotNull(response);
        assertFalse(response.getCanBeManagedByUser());
    }

    @Test
    public void buildLogicalConnectorDtoWithNullPhysicalConnector()
    {
        LogicalConnector logicalConnector = createLogicalConnector();
        logicalConnector.setPhysicalConnector(null);

        LogicalConnectorDto response = this.logicalConnectorBuilder.buildLogicalConnectorDto(logicalConnector, "CODE");
        assertNotNull(response);
        assertNull(response.getPhysicalConnector());

    }

    @Test
    public void buildLogicalConnectorDtoWithNullPhysicalConnectorPort()
    {
        LogicalConnector logicalConnector = createLogicalConnector();
        logicalConnector.setPhysicalConnectorPort(null);

        LogicalConnectorDto response = this.logicalConnectorBuilder.buildLogicalConnectorDto(logicalConnector, "CODE");
        assertNotNull(response);
        assertNull(response.getPhysicalConnector().getPort());
    }

    @Test
    public void buildLogicalConnectorDtoWithConfiguration()
    {
        LogicalConnector logicalConnector = createLogicalConnector();

        CMLogicalConnectorPropertyDto[] configurationManagerDtoArray = createConfigurationManagerDtoArray();

        when(configurationmanagerClient.getLogicalConnectorPropertiesDto(anyInt())).thenReturn(configurationManagerDtoArray);

        LogicalConnectorDto response = this.logicalConnectorBuilder.buildLogicalConnectorDto(logicalConnector, "CODE");
        assertNotNull(response);
        compareLogicalConectorProperties(configurationManagerDtoArray, response.getProperties());
    }

    private void compareLogicalConectorProperties(CMLogicalConnectorPropertyDto[] configurationManagerDtoArray,
            LogicalConnectorPropertyDto[] logicalConnectorPropertyDtoArray)
    {
        assertTrue(configurationManagerDtoArray.length == logicalConnectorPropertyDtoArray.length);
        for (int i = 0; i < configurationManagerDtoArray.length; i++)
        {
            configurationManagerDtoArray[i].getInaccessible().equals(logicalConnectorPropertyDtoArray[i].getInaccessible());
            configurationManagerDtoArray[i].getPropertyScope().equals(logicalConnectorPropertyDtoArray[i].getPropertyScope());
            configurationManagerDtoArray[i].getPropertyDescription().equals(logicalConnectorPropertyDtoArray[i].getPropertyDescription());
            configurationManagerDtoArray[i].getPropertyName().equals(logicalConnectorPropertyDtoArray[i].getPropertyName());
            configurationManagerDtoArray[i].getPropertyManagement().equals(logicalConnectorPropertyDtoArray[i].getPropertyManagement());
            configurationManagerDtoArray[i].getPropertyType().equals(logicalConnectorPropertyDtoArray[i].getPropertyType());
            configurationManagerDtoArray[i].getPropertyDefaultName().equals(logicalConnectorPropertyDtoArray[i].getPropertyDefaultName());
            configurationManagerDtoArray[i].getPropertyValue().equals(logicalConnectorPropertyDtoArray[i].getPropertyValue());
            configurationManagerDtoArray[i].getLogicalConnectorId().equals(logicalConnectorPropertyDtoArray[i].getLogicalConnectorId());
            configurationManagerDtoArray[i].getId().equals(logicalConnectorPropertyDtoArray[i].getId());
            configurationManagerDtoArray[i].getPropertySecurity().equals(logicalConnectorPropertyDtoArray[i].getPropertySecurity());
        }
    }

    private CMLogicalConnectorPropertyDto[] createConfigurationManagerDtoArray()
    {
        CMLogicalConnectorPropertyDto[] configurationManagerDtoArray = new CMLogicalConnectorPropertyDto[RandomUtils.nextInt(1, 10)];
        for (int i = 0; i < configurationManagerDtoArray.length; i++)
        {
            configurationManagerDtoArray[i] = createConfigurationManagerDto();
        }

        return configurationManagerDtoArray;
    }

    private CMLogicalConnectorPropertyDto createConfigurationManagerDto()
    {
        CMLogicalConnectorPropertyDto configurationManagerDto = new CMLogicalConnectorPropertyDto();
        configurationManagerDto.setInaccessible(RandomUtils.nextBoolean());
        configurationManagerDto.setPropertyScope(RandomStringUtils.randomAlphabetic(10));
        configurationManagerDto.setPropertyDescription(RandomStringUtils.randomAlphabetic(10));
        configurationManagerDto.setPropertyName(RandomStringUtils.randomAlphabetic(10));
        configurationManagerDto.setPropertyManagement(RandomStringUtils.randomAlphabetic(10));
        configurationManagerDto.setPropertyType(RandomStringUtils.randomAlphabetic(10));
        configurationManagerDto.setPropertyDefaultName(RandomStringUtils.randomAlphabetic(10));
        configurationManagerDto.setPropertyValue(RandomStringUtils.randomAlphabetic(10));
        configurationManagerDto.setLogicalConnectorId(RandomUtils.nextInt(1, 99999));
        configurationManagerDto.setId(RandomUtils.nextInt(1, 99999));
        configurationManagerDto.setPropertySecurity(RandomUtils.nextBoolean());
        return configurationManagerDto;
    }

    private List<LogicalConnector> createLogicalConnectorList()
    {
        List<LogicalConnector> logicalConnectorList = new ArrayList<>();
        Product product = new Product();
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setProduct(product);
        ConnectorType type = new ConnectorType();
        logicalConnector.setConnectorType(type);
        logicalConnectorList.add(logicalConnector);
        logicalConnector.setDeploymentServices(createDeploymentServiceList());
        return logicalConnectorList;
    }

    private LogicalConnector createLogicalConnector()
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
        logicalConnector.setId(RandomUtils.nextInt(1, 10));
        logicalConnector.setEnvironment(Environment.INT.getEnvironment());
        logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
        logicalConnector.setProduct(product);
        ConnectorType type = new ConnectorType();
        logicalConnector.setConnectorType(type);
        logicalConnector.setPhysicalConnector(physicalConnector);
        logicalConnector.setPhysicalConnectorPort(physicalConnectorPort);
        logicalConnector.getDeploymentServices().add(service);
        return logicalConnector;
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
}