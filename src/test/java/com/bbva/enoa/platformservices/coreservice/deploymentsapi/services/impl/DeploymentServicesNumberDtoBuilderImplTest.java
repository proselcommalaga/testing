package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDeploymentManagerClient;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DeploymentServicesNumberDtoBuilderImplTest
{
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private IDeploymentManagerClient deploymentManagerClient;
    @InjectMocks
    private DeploymentServicesNumberDtoBuilderImpl deploymentServicesNumberDtoBuilder;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

}
