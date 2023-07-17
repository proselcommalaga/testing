package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.StatusDTO;
import com.bbva.enoa.apirestgen.ethermanagerapi.model.EtherDeploymentDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.entities.NovaDeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentAction;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.common.util.ProfilingUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDeploymentManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IEtherManagerClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentReplacePlanServiceImpl;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IRepositoryManagerService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.PlanProfilingUtils;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by xe56809 on 14/03/2018.
 */
class DeploymentManagerServiceImplTest
{
    @Mock
    private IDeploymentManagerClient deploymentManagerClient;
    @Mock
    private DeploymentUtils deploymentUtils;
    @Mock
    private IEtherManagerClient etherManagerClient;
    @Mock
    private IRepositoryManagerService repositoryManagerService;

    @Mock
    private IDeploymentReplacePlanServiceImpl deploymentReplacePlanService;
    @Mock
    private IApiGatewayService apiGatewayService;
    @Mock
    private ProfilingUtils profilingUtils;
    @Mock
    private PlanProfilingUtils planProfilingUtils;

    @InjectMocks
    private DeploymentManagerServiceImpl deploymentmanagerServiceImpl;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void startService() throws Exception
    {
        //Given
        DeploymentPlan deploymentPlan = new DeploymentPlan();

        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);

        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);

        //Then
        this.deploymentmanagerServiceImpl.startService(deploymentService);
        verify(this.deploymentManagerClient, times(1)).startService(deploymentService);
    }

    @Test
    void stopService() throws Exception
    {
        //Given
        DeploymentPlan deploymentPlan = new DeploymentPlan();

        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);

        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);

        //Then
        this.deploymentmanagerServiceImpl.stopService(deploymentService);
        verify(this.deploymentManagerClient, times(1)).stopService(deploymentService);
    }

    @Test
    void startInstance() throws Exception
    {
        //Given
        DeploymentPlan deploymentPlan = new DeploymentPlan();

        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);

        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);

        DeploymentInstance deploymentInstance = new NovaDeploymentInstance();
        deploymentInstance.setService(deploymentService);

        //Then
        this.deploymentmanagerServiceImpl.startInstance(deploymentInstance);
        verify(this.deploymentManagerClient, times(1)).startInstance(deploymentInstance);
    }

    @Test
    void stopInstance() throws Exception
    {
        //Given
        DeploymentPlan deploymentPlan = new DeploymentPlan();

        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);

        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);

        DeploymentInstance deploymentInstance = new NovaDeploymentInstance();
        deploymentInstance.setService(deploymentService);

        //Then
        this.deploymentmanagerServiceImpl.stopInstance(deploymentInstance);
        verify(this.deploymentManagerClient, times(1)).stopInstance(deploymentInstance);
    }

    @Test
    void startSubsystem() throws Exception
    {
        //Given
        DeploymentPlan deploymentPlan = new DeploymentPlan();

        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);

        //Then
        this.deploymentmanagerServiceImpl.startSubsystem(deploymentSubsystem);
        verify(this.deploymentManagerClient, times(1)).startSubsystem(deploymentSubsystem);
    }

    @Test
    void stopSubsystem() throws Exception
    {
        //Given
        StatusDTO number = new StatusDTO();
        number.setRunning(1);

        DeploymentPlan plan = new DeploymentPlan();

        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(plan);

        //Then
        this.deploymentmanagerServiceImpl.stopSubsystem(deploymentSubsystem);
        verify(this.deploymentManagerClient, times(1)).stopSubsystem(deploymentSubsystem);
    }

    @Test
    void startPlan() throws Exception
    {
        //Given
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        //Then
        this.deploymentmanagerServiceImpl.startPlan(deploymentPlan);
        verify(this.deploymentManagerClient, times(1)).startPlan(deploymentPlan);
    }

    @Test
    void stopPlan() throws Exception
    {
        //Given
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        //Then
        this.deploymentmanagerServiceImpl.stopPlan(deploymentPlan);
        verify(this.deploymentManagerClient, times(1)).stopPlan(deploymentPlan);
    }

    @Test
    void restartPlan() throws Exception
    {
        //Given
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        //Then
        this.deploymentmanagerServiceImpl.restartPlan(deploymentPlan);
        verify(this.deploymentManagerClient, times(1)).restartPlan(deploymentPlan);
    }

    @Test
    void restartInstance() throws Exception
    {
        //Given
        DeploymentPlan deploymentPlan = new DeploymentPlan();

        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);

        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);

        DeploymentInstance deploymentInstance = new NovaDeploymentInstance();
        deploymentInstance.setService(deploymentService);

        //Then
        this.deploymentmanagerServiceImpl.restartInstance(deploymentInstance);
        verify(this.deploymentManagerClient, times(1)).restartInstance(deploymentInstance);
    }

    @Test
    void restartSubsystem() throws Exception
    {
        //Given
        DeploymentPlan deploymentPlan = new DeploymentPlan();

        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);

        //Then
        this.deploymentmanagerServiceImpl.restartSubsystem(deploymentSubsystem);
        verify(this.deploymentManagerClient, times(1)).restartSubsystem(deploymentSubsystem);
    }

    @Test
    void restartService() throws Exception
    {
        //Given
        DeploymentPlan deploymentPlan = new DeploymentPlan();

        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
        deploymentSubsystem.setDeploymentPlan(deploymentPlan);

        DeploymentService deploymentService = new DeploymentService();
        deploymentService.setDeploymentSubsystem(deploymentSubsystem);

        //Then
        this.deploymentmanagerServiceImpl.restartService(deploymentService);
        verify(this.deploymentManagerClient, times(1)).restartService(deploymentService);
    }

    @Test
    void deployPlanEther()
    {
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setSelectedDeploy(Platform.ETHER);

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();

        when(this.deploymentUtils.buildEtherDeploymentDTO((DeploymentPlan) any(), anyString(), anyString())).thenReturn(etherDeploymentDTO);
        when(this.etherManagerClient.deployEtherPlan(any())).thenReturn(true);

        boolean resultDeployPlan = this.deploymentmanagerServiceImpl.deployPlan(deploymentPlan);
        Assertions.assertEquals(true, resultDeployPlan);
        Assertions.assertEquals(DeploymentStatus.DEPLOYED, deploymentPlan.getStatus());
        Assertions.assertEquals(DeploymentAction.DEPLOYING, deploymentPlan.getAction());

        verify(this.deploymentUtils, times(1)).buildEtherDeploymentDTO((DeploymentPlan) any(), anyString(),anyString());
        verify(this.etherManagerClient, times(1)).deployEtherPlan(any());
        verify(this.repositoryManagerService, times(1)).savePlan(deploymentPlan);
    }

    @Test
    void deployPlanEtherFalseResult()
    {
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setSelectedDeploy(Platform.ETHER);

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();

        when(this.deploymentUtils.buildEtherDeploymentDTO((DeploymentPlan) any(), anyString(), anyString())).thenReturn(etherDeploymentDTO);
        when(this.etherManagerClient.deployEtherPlan(any())).thenReturn(false);

        boolean resultDeployPlan = this.deploymentmanagerServiceImpl.deployPlan(deploymentPlan);
        Assertions.assertEquals(false, resultDeployPlan);

        verify(this.deploymentUtils, times(1)).buildEtherDeploymentDTO((DeploymentPlan) any(), anyString(),anyString());
        verify(this.etherManagerClient, times(1)).deployEtherPlan(any());
        verify(this.repositoryManagerService, never()).savePlan(deploymentPlan);
    }

    @Test
    void deployPlanEtherThrowException()
    {
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setSelectedDeploy(Platform.ETHER);

        EtherDeploymentDTO etherDeploymentDTO = new EtherDeploymentDTO();

        when(this.deploymentUtils.buildEtherDeploymentDTO((DeploymentPlan) any(), anyString(), anyString())).thenReturn(etherDeploymentDTO);
        when(this.etherManagerClient.deployEtherPlan(any())).thenThrow(NovaException.class);

        assertThrows(NovaException.class, () ->  this.deploymentmanagerServiceImpl.deployPlan(deploymentPlan));
        Assertions.assertEquals(DeploymentStatus.DEFINITION, deploymentPlan.getStatus());
        Assertions.assertEquals(DeploymentAction.READY, deploymentPlan.getAction());

        verify(this.deploymentUtils, times(1)).buildEtherDeploymentDTO((DeploymentPlan) any(), anyString(),anyString());
        verify(this.etherManagerClient, times(1)).deployEtherPlan(any());
        verify(this.repositoryManagerService, times(1)).savePlan(deploymentPlan);
    }

    @Test
    void deployPlan()
    {
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(1);
        deploymentPlan.setSelectedDeploy(Platform.NOVA);

        when(this.deploymentManagerClient.deployPlan(anyInt())).thenReturn(true);

        boolean resultDeployPlan = this.deploymentmanagerServiceImpl.deployPlan(deploymentPlan);
        Assertions.assertEquals(true, resultDeployPlan);

        verify(this.deploymentManagerClient, times(1)).deployPlan(anyInt());
    }

    @Test
    void deployPlanThrowException()
    {
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(1);
        deploymentPlan.setSelectedDeploy(Platform.NOVA);

        when(this.deploymentManagerClient.deployPlan(anyInt())).thenThrow(NovaException.class);

        assertThrows(NovaException.class, () ->  this.deploymentmanagerServiceImpl.deployPlan(deploymentPlan));
        Assertions.assertEquals(DeploymentStatus.DEFINITION, deploymentPlan.getStatus());
        Assertions.assertEquals(DeploymentAction.READY, deploymentPlan.getAction());

        verify(this.deploymentManagerClient, times(1)).deployPlan(anyInt());
        verify(this.repositoryManagerService, times(1)).savePlan(deploymentPlan);
    }


    @Test
    void replacePlanTest()
    {
        DeploymentPlan plan = mock(DeploymentPlan.class);
        when(plan.getId()).thenReturn(1);
        DeploymentPlan newPlan = mock(DeploymentPlan.class);
        when(newPlan.getId()).thenReturn(2);

        when(newPlan.getEnvironment()).thenReturn(Environment.INT.getEnvironment());
        when(plan.getEnvironment()).thenReturn(Environment.INT.getEnvironment());

        when(plan.getSelectedDeploy()).thenReturn(Platform.NOVA);
        when(newPlan.getSelectedDeploy()).thenReturn(Platform.NOVA);

        Map<String, List<DeploymentService>> updateServiceMap = new HashMap<>();
        List<DeploymentService> deploymentServiceList = new ArrayList<>();
        updateServiceMap.put(DeploymentConstants.UPDATE_INSTANCE, deploymentServiceList);

        when(deploymentReplacePlanService.getUncommonReplacePlan(1, 2)).thenReturn(updateServiceMap);

        ReflectionTestUtils.setField(this.deploymentmanagerServiceImpl, "cesEnabled", false);

        boolean waited = true;
        when(this.deploymentManagerClient.replacePlan(1, 2, null,
                null, null)).thenReturn(true);

        boolean res = this.deploymentmanagerServiceImpl.replacePlan(plan, newPlan);

        Assert.assertEquals(waited, res);

        verify(apiGatewayService, times(1)).updatePublication(null, null, newPlan, plan);

        verify(this.apiGatewayService, times(1)).generateDockerKey(null, "INT");
        verify(this.apiGatewayService, times(1)).generateDockerKey(updateServiceMap.get(DeploymentConstants.UPDATE_INSTANCE), "INT");
        verify(this.repositoryManagerService, times(1)).savePlan(newPlan);
    }


    @Test
    void replacePlanCesEnabledTest()
    {
        DeploymentPlan plan = mock(DeploymentPlan.class);
        when(plan.getId()).thenReturn(1);
        DeploymentPlan newPlan = mock(DeploymentPlan.class);
        when(newPlan.getId()).thenReturn(2);

        when(newPlan.getEnvironment()).thenReturn(Environment.INT.getEnvironment());
        when(plan.getEnvironment()).thenReturn(Environment.INT.getEnvironment());

        when(plan.getSelectedDeploy()).thenReturn(Platform.NOVA);
        when(newPlan.getSelectedDeploy()).thenReturn(Platform.NOVA);

        Map<String, List<DeploymentService>> updateServiceMap = new HashMap<>();
        List<DeploymentService> deploymentServiceList = new ArrayList<>();
        updateServiceMap.put(DeploymentConstants.UPDATE_INSTANCE, deploymentServiceList);

        when(deploymentReplacePlanService.getUncommonReplacePlan(1, 2)).thenReturn(updateServiceMap);

        ReflectionTestUtils.setField(this.deploymentmanagerServiceImpl, "cesEnabled", true);

        ReleaseVersion releaseVersion = mock(ReleaseVersion.class);
        when(plan.getReleaseVersion()).thenReturn(releaseVersion);
        Release release = mock(Release.class);
        when(releaseVersion.getRelease()).thenReturn(release);
        Product product = mock(Product.class);
        when(release.getProduct()).thenReturn(product);
        when(product.getUuaa()).thenReturn("KKPF");


        when(profilingUtils.isPlanExposingApis(newPlan)).thenReturn(true);

        boolean waited = true;
        when(this.deploymentManagerClient.replacePlan(1, 2, null,
                null, null)).thenReturn(true);

        boolean res = this.deploymentmanagerServiceImpl.replacePlan(plan, newPlan);

        Assert.assertEquals(waited, res);

        verify(apiGatewayService, times(1)).updatePublication(null, null, newPlan, plan);

        verify(planProfilingUtils, times(1)).checkPlanProfileChange(newPlan);
        verify(this.apiGatewayService, times(1)).generateDockerKey(null, "INT");
        verify(this.apiGatewayService, times(1)).generateDockerKey(updateServiceMap.get(DeploymentConstants.UPDATE_INSTANCE), "INT");
        verify(this.repositoryManagerService, times(1)).savePlan(newPlan);


    }
}