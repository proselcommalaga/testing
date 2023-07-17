package com.bbva.enoa.platformservices.coreservice.logicalconnectorapi.services.impl;

import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentConnectorPropertyRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.LogicalConnectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LogicalConnectorRepositoriesServiceImplTest
{
    @Mock
    private DeploymentConnectorPropertyRepository deploymentConnectorPropertyRepository;
    @Mock
    private LogicalConnectorRepository logicalConnectorRepository;
    @InjectMocks
    private LogicalConnectorRepositoriesServiceImpl logicalConnectorRepositoriesService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void deleteDeploymentConnectorPropertyList()
    {
        LogicalConnectorProperty logicalConnectorProperty = new LogicalConnectorProperty();
        logicalConnectorProperty.setId(1);
        List<LogicalConnectorProperty> properties = new ArrayList<>();
        properties.add(logicalConnectorProperty);
        this.logicalConnectorRepositoriesService.deleteDeploymentConnectorPropertyList(properties);
        verify(this.deploymentConnectorPropertyRepository, times(1)).deleteByLogicalConnectorPropertyId(1);
    }

    @Test
    public void deleteLogicalConnector()
    {
        LogicalConnector logicalConnector = new LogicalConnector();
        this.logicalConnectorRepositoriesService.deleteLogicalConnector(logicalConnector);
        verify(this.logicalConnectorRepository, times(1)).delete(logicalConnector);
    }

    @Test
    public void deleteLogicalConnectorOfDeploymentService()
    {
        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        DeploymentService service1 = new DeploymentService();
        service1.setService(releaseVersionService);
        DeploymentService service2 = new DeploymentService();
        service2.setService(releaseVersionService);
        List<DeploymentService> serviceList = new ArrayList<>();
        serviceList.add(service1);
        serviceList.add(service2);
        LogicalConnector logicalConnector = new LogicalConnector();
        logicalConnector.setName("Connector");
        service1.getLogicalConnectors().add(logicalConnector);
        this.logicalConnectorRepositoriesService.deleteLogicalConnectorOfDeploymentService(serviceList, logicalConnector);
        assertTrue(service1.getLogicalConnectors().isEmpty());
    }
}