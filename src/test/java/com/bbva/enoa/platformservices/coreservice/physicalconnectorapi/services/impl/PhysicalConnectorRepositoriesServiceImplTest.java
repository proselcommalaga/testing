package com.bbva.enoa.platformservices.coreservice.physicalconnectorapi.services.impl;

import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentConnectorPropertyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PhysicalConnectorRepositoriesServiceImplTest
{
    @Mock
    private DeploymentConnectorPropertyRepository deploymentConnectorPropertyRepository;
    @InjectMocks
    private PhysicalConnectorRepositoriesServiceImpl physicalConnectorRepositoriesService;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void deleteDeploymentConnectorProperty()
    {
        LogicalConnectorProperty logicalConnectorProperty = new LogicalConnectorProperty();
        logicalConnectorProperty.setId(1);
        this.physicalConnectorRepositoriesService.deleteDeploymentConnectorProperty(logicalConnectorProperty);
        verify(this.deploymentConnectorPropertyRepository, times(1)).deleteByLogicalConnectorPropertyId(1);
    }
}