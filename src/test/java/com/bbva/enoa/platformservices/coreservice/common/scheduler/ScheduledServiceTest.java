package com.bbva.enoa.platformservices.coreservice.common.scheduler;


import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.release.entities.ServiceExecutionHistory;
import com.bbva.enoa.datamodel.model.release.entities.ServiceExecutionHistoryId;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ServiceExecutionHistoryRepository;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentsService;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IRepositoryManagerService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
public class ScheduledServiceTest
{
    @Mock
    private ServiceExecutionHistoryRepository serviceExecutionHistoryRepository;
    @Mock
    private IRepositoryManagerService repositoryManagerService;
    @Mock
    private IDeploymentsService deploymentsService;
    @InjectMocks
    private ScheduledService service;

    @BeforeEach
    public void init()
    {
        MockitoAnnotations.initMocks(ScheduledService.class);
        ReflectionTestUtils.setField(service, "serviceExecutionHistoryDaysSaved", 30);
        ReflectionTestUtils.setField(service, "undeployedPlanDaysSaved", 40);
    }

    @Test
    public void when_no_old_service_executions_are_found_then_dont_call_delete_operation()
    {
        Mockito.when(serviceExecutionHistoryRepository.findByLastExecutionBefore(Mockito.any(Calendar.class))).thenReturn(Collections.emptyList());

        service.cleanServiceExecutionHistory();

        Mockito.verify(serviceExecutionHistoryRepository, Mockito.times(0)).deleteAll(Mockito.anyIterable());
    }

    @Test
    public void when_no_running_service_executions_are_found_then_dont_remove_any_execution_from_old_executions()
    {
        List<ServiceExecutionHistory> serviceExecutionHistories = new ArrayList<>(1);
        serviceExecutionHistories.add(getDummyExecution("final", "1.0.0", Calendar.getInstance(), 1234));
        Mockito.when(serviceExecutionHistoryRepository.findByLastExecutionBefore(Mockito.any(Calendar.class))).thenReturn(serviceExecutionHistories);

        service.cleanServiceExecutionHistory();

        Mockito.verify(serviceExecutionHistoryRepository, Mockito.times(1)).deleteAll(serviceExecutionHistories);
    }

    @Test
    public void when_any_running_service_executions_is_found_then_exclude_running_execution_from_old_executions()
    {
        List<ServiceExecutionHistory> serviceExecutionHistories = new ArrayList<>(2);
        Calendar calendar = Calendar.getInstance();
        serviceExecutionHistories.add(getDummyExecution("final", "1.0.0", calendar, null));
        serviceExecutionHistories.add(getDummyExecution("final2", "1.1.0", calendar, 123));
        Mockito.when(serviceExecutionHistoryRepository.findByLastExecutionBefore(Mockito.any(Calendar.class))).thenReturn(serviceExecutionHistories);
        Mockito.when(serviceExecutionHistoryRepository.findStillRunningServices(Mockito.anyList(), Mockito.anyBoolean())).thenReturn(serviceExecutionHistories.subList(1, 2));

        service.cleanServiceExecutionHistory();

        Mockito.verify(serviceExecutionHistoryRepository, Mockito.times(1)).saveAll(Mockito.anyIterable());
        Mockito.verify(serviceExecutionHistoryRepository, Mockito.times(1)).deleteAll(serviceExecutionHistories.subList(0, 1));
    }

    @Test
    public void when_no_deployment_plans_are_found_then_dont_call_archive_plan_operation()
    {
        Mockito.when(repositoryManagerService.findByStatusAndEnvironmentAndUndeploymentDateNotNull(eq(DeploymentStatus.UNDEPLOYED), any())).thenReturn(Collections.emptyList());

        service.storageUndeployedPlan();

        Mockito.verify(deploymentsService, Mockito.times(0)).archivePlan(anyInt());
    }

    @Test
    public void when_no_deployment_plan_before_purge_time_are_not_found_then_dont_archive_any_deployment_plan()
    {
        final List<DeploymentPlan> deploymentPlanList = new ArrayList<>(1);
        deploymentPlanList.add(getDummyDeploymentPlan(Calendar.getInstance()));
        Mockito.when(repositoryManagerService.findByStatusAndEnvironmentAndUndeploymentDateNotNull(eq(DeploymentStatus.UNDEPLOYED), eq(Environment.INT))).thenReturn(deploymentPlanList);

        service.storageUndeployedPlan();

        Mockito.verify(deploymentsService, Mockito.times(0)).archivePlan(anyInt());
    }

    @Test
    public void when_one_deployment_plan_before_purge_time_are_found_then_archive_only_this_deployment_plan()
    {
        final List<DeploymentPlan> deploymentPlanList = new ArrayList<>(2);
        deploymentPlanList.add(getDummyDeploymentPlan(Calendar.getInstance()));
        final Calendar beforePurgeTimeCalendar = Calendar.getInstance();
        beforePurgeTimeCalendar.add(Calendar.DAY_OF_MONTH, -50);
        deploymentPlanList.add(getDummyDeploymentPlan(beforePurgeTimeCalendar));
        Mockito.when(repositoryManagerService.findByStatusAndEnvironmentAndUndeploymentDateNotNull(eq(DeploymentStatus.UNDEPLOYED), eq(Environment.INT))).thenReturn(deploymentPlanList);

        service.storageUndeployedPlan();

        Mockito.verify(deploymentsService, Mockito.times(0)).archivePlan(deploymentPlanList.get(0).getId());
        Mockito.verify(deploymentsService, Mockito.times(1)).archivePlan(deploymentPlanList.get(1).getId());
    }

    @Test
    public void when_first_deployment_plan_throws_exception_then_the_next_deployment_plan_continues()
    {
        final Calendar beforePurgeTimeCalendar = Calendar.getInstance();
        beforePurgeTimeCalendar.add(Calendar.DAY_OF_MONTH, -50);
        final List<DeploymentPlan> deploymentPlanList = new ArrayList<>(2);
        deploymentPlanList.add(getDummyDeploymentPlan(beforePurgeTimeCalendar));
        deploymentPlanList.add(getDummyDeploymentPlan(beforePurgeTimeCalendar));
        Mockito.when(repositoryManagerService.findByStatusAndEnvironmentAndUndeploymentDateNotNull(eq(DeploymentStatus.UNDEPLOYED), eq(Environment.INT))).thenReturn(deploymentPlanList);
        Mockito.doThrow(Mockito.mock(NovaException.class)).when(deploymentsService).archivePlan(eq(deploymentPlanList.get(0).getId()));

        service.storageUndeployedPlan();

        Mockito.verify(deploymentsService, Mockito.times(1)).archivePlan(deploymentPlanList.get(0).getId());
        Mockito.verify(deploymentsService, Mockito.times(1)).archivePlan(deploymentPlanList.get(1).getId());
    }

    @Test
    public void when_first_deployment_plan_undeployment_date_is_null_then_the_next_deployment_plan_continues()
    {
        final Calendar beforePurgeTimeCalendar = Calendar.getInstance();
        beforePurgeTimeCalendar.add(Calendar.DAY_OF_MONTH, -50);
        final List<DeploymentPlan> deploymentPlanList = new ArrayList<>(2);
        deploymentPlanList.add(getDummyDeploymentPlan(null));
        deploymentPlanList.add(getDummyDeploymentPlan(beforePurgeTimeCalendar));
        Mockito.when(repositoryManagerService.findByStatusAndEnvironmentAndUndeploymentDateNotNull(eq(DeploymentStatus.UNDEPLOYED), eq(Environment.INT))).thenReturn(deploymentPlanList);
        service.storageUndeployedPlan();

        Mockito.verify(deploymentsService, Mockito.times(0)).archivePlan(deploymentPlanList.get(0).getId());
        Mockito.verify(deploymentsService, Mockito.times(1)).archivePlan(deploymentPlanList.get(1).getId());
    }

    private ServiceExecutionHistory getDummyExecution(String finalName, String version, Calendar lastExecution, Integer deploymentServiceId)
    {
        ServiceExecutionHistory serviceExecutionHistory = new ServiceExecutionHistory();
        ServiceExecutionHistoryId id = new ServiceExecutionHistoryId();
        id.setVersion(version);
        id.setEnvironment("INT");
        id.setFinalName(finalName);
        serviceExecutionHistory.setId(id);
        serviceExecutionHistory.setLastExecution(lastExecution);
        serviceExecutionHistory.setDeploymentServiceId(deploymentServiceId);
        return serviceExecutionHistory;
    }

    private DeploymentPlan getDummyDeploymentPlan(Calendar undeploymentDate)
    {
        final DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(RandomUtils.nextInt());
        deploymentPlan.setUndeploymentDate(undeploymentDate);
        return deploymentPlan;
    }
}