package com.bbva.enoa.platformservices.coreservice.deploymentsapi.callback;


import com.bbva.enoa.platformservices.coreservice.apimanagerapi.services.IApiManagerService;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentSubsystemRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ConfigurationmanagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.TodoTaskServiceClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.listener.ListenerDeploymentmanagercallbackapi;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl.DeployerServiceImpl;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentManagerCallbackApiService;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListenerDeploymentmanagercallbackapiTest
{
    @Mock
    private IApiManagerService apiManagerService;
    @Mock
    private IDeploymentManagerCallbackApiService deploymentManagerCallbackApiService;
    @Mock
    private DeployerServiceImpl deployerService;
    @Mock
    private DeploymentServiceRepository deploymentServiceRepository;
    @Mock
    private DeploymentSubsystemRepository deploymentSubsystemRepository;
    @Mock
    private IBudgetsService budgetsService;
    @Mock
    private TodoTaskServiceClient todoTaskServiceClient;
    @Mock
    private ConfigurationmanagerClient configurationmanagerClient;
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @InjectMocks
    private ListenerDeploymentmanagercallbackapi listenerDeploymentmanagercallbackapi;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void promotePlan() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.promotePlan(getNovaMetadata(), 1);
    }

    @Test
    public void startInstanceError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.startInstanceError(getNovaMetadata(), 1);
    }

    @Test
    public void restartInstanceError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.restartInstanceError(getNovaMetadata(), 1);
    }

    @Test
    public void stopPlanError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.stopPlanError(getNovaMetadata(), 1);
    }

    @Test
    public void removeService() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.removeService(getNovaMetadata(), 1);
    }

    @Test
    public void deployService() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.deployService(getNovaMetadata(), 1);
    }

    @Test
    public void stopServiceError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.stopServiceError(getNovaMetadata(), 1);
    }

    @Test
    public void deployPlan() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.deployPlan(getNovaMetadata(), 1);
    }

    @Test
    public void promotePlanError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.promotePlanError(getNovaMetadata(), 1);
    }

    @Test
    public void startSubsystem() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.startSubsystem(getNovaMetadata(), 1);
    }

    @Test
    public void restartSubsystem() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.restartSubsystem(getNovaMetadata(), 1);
    }

    @Test
    public void startSubsystemError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.startSubsystemError(getNovaMetadata(), 1);
    }

    @Test
    public void restartSubsystemError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.restartSubsystemError(getNovaMetadata(), 1);
    }

    @Test
    public void startPlanError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.startPlanError(getNovaMetadata(), 1);
    }

    @Test
    public void restartPlanError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.restartPlanError(getNovaMetadata(), 1);
    }

    @Test
    public void stopPlan() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.stopPlan(getNovaMetadata(), 1);
    }

    @Test
    public void stopSubsystemError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.stopSubsystemError(getNovaMetadata(), 1);
    }

    @Test
    public void deploySubsystem() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.deploySubsystem(getNovaMetadata(), 1);
    }

    @Test
    public void deploySubsystemError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.deploySubsystemError(getNovaMetadata(), 1);
    }

    @Test
    public void removeServiceError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.removeServiceError(getNovaMetadata(), 1);
    }

    @Test
    public void deployServiceError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.deployServiceError(getNovaMetadata(), 1);
    }

    @Test
    public void stopSubsystem() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.stopSubsystem(getNovaMetadata(), 1);
    }

    @Test
    public void startPlan() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.startPlan(getNovaMetadata(), 1);
    }

    @Test
    public void restartPlan() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.restartPlan(getNovaMetadata(), 1);
    }

    @Test
    public void replacePlan() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.replacePlan(getNovaMetadata(), 1, 2);
    }

    @Test
    public void stopInstanceError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.stopInstanceError(getNovaMetadata(), 1);
    }

    @Test
    public void deployPlanError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.deployPlanError(getNovaMetadata(), 1);
    }

    @Test
    public void startInstance() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.startInstance(getNovaMetadata(), 1);
    }

    @Test
    public void restartInstance() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.restartInstance(getNovaMetadata(), 1);
    }

    @Test
    public void removeSubsystem() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.removeSubsystem(getNovaMetadata(), 1);
    }

    @Test
    public void replacePlanError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.replacePlanError(getNovaMetadata(), 1, 2);
    }

    @Test
    public void removePlan() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.removePlan(getNovaMetadata(), 1);
    }

    @Test
    public void removeSubsystemError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.removeSubsystemError(getNovaMetadata(), 1);
    }

    @Test
    public void startServiceError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.startServiceError(getNovaMetadata(), 1);
    }

    @Test
    public void restartServiceError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.restartServiceError(getNovaMetadata(), 1);
    }

    @Test
    public void removePlanError() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.removePlanError(getNovaMetadata(), 1);
    }

    @Test
    public void stopInstance() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.stopInstance(getNovaMetadata(), 1);
    }

    @Test
    public void stopService() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.stopService(getNovaMetadata(), 1);
    }

    @Test
    public void startService() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.startService(getNovaMetadata(), 1);
    }

    @Test
    public void restartService() throws Exception
    {
        this.listenerDeploymentmanagercallbackapi.restartService(getNovaMetadata(), 1);
    }
    
    private static NovaMetadata getNovaMetadata(){
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", Arrays.asList(""));
        NovaMetadata metadata = new NovaMetadata();
        metadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));
        return metadata;
    }
}
