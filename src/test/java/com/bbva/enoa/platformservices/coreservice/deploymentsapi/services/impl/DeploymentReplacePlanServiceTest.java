package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentServiceFilesystem;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.release.entities.JdkParameter;
import com.bbva.enoa.datamodel.model.release.entities.JdkParameterType;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.JdkParameterRepository;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.model.ReplacePlanQueryResult;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.model.ReplaceServiceInfo;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.services.interfaces.ILibraryManagerService;
import org.apache.commons.collections.list.AbstractLinkedList;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.management.relation.RoleList;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class DeploymentReplacePlanServiceTest
{

    @Mock
    private EntityManager entityManager;

    @Mock
    private DeploymentServiceRepository deploymentServiceRepository;

    @Mock
    private ILibraryManagerService libraryManagerService;

    @Mock
    private JdkParameterRepository jdkParameterRepository;

    @InjectMocks
    private DeploymentReplacePlanService deploymentReplacePlanService;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getUncommonReplacePlanOnlyNumberInstance()
    {
        Query queryOld = mock(Query.class);
        Query queryNew = mock(Query.class);
        when(entityManager.createNativeQuery("SELECT DS.ID, PD.NAME, CV.VALUE FROM DEPLOYMENT_PLAN DP INNER JOIN DEPLOYMENT_SUBSYSTEM DSB ON DP.ID = DSB.DEPLOYMENT_PLAN_ID INNER JOIN DEPLOYMENT_SERVICE DS ON DSB.ID = DS.DEPLOYMENT_SUBSYSTEM_ID INNER JOIN RELEASE_VERSION_SERVICE RVS ON DS.SERVICE_ID = RVS.ID LEFT JOIN CONFIGURATION_REVISION CR ON DP.ID=CR.DEPLOYMENT_PLAN_ID LEFT JOIN PROPERTY_DEFINITION PD ON RVS.ID = PD.SERVICE_ID LEFT JOIN CONFIGURATION_VALUE CV ON CR.ID = CV.REVISION_ID AND PD.ID = CV.DEFINITION_ID WHERE DP.ID =2 ORDER BY DS.ID ASC "))
                .thenReturn(queryNew);
        when(entityManager.createNativeQuery("SELECT DS.ID, PD.NAME, CV.VALUE FROM DEPLOYMENT_PLAN DP INNER JOIN DEPLOYMENT_SUBSYSTEM DSB ON DP.ID = DSB.DEPLOYMENT_PLAN_ID INNER JOIN " +
                "DEPLOYMENT_SERVICE DS ON DSB.ID = DS.DEPLOYMENT_SUBSYSTEM_ID INNER JOIN RELEASE_VERSION_SERVICE RVS ON DS.SERVICE_ID = RVS.ID LEFT JOIN CONFIGURATION_REVISION CR ON DP.ID=CR.DEPLOYMENT_PLAN_ID LEFT JOIN PROPERTY_DEFINITION PD ON RVS.ID = PD.SERVICE_ID LEFT JOIN CONFIGURATION_VALUE CV ON CR.ID = CV.REVISION_ID AND PD.ID = CV.DEFINITION_ID WHERE DP.ID =1 ORDER BY DS.ID ASC "))
                .thenReturn(queryOld);

        List<Object[]> oldReplacePlanQueryResultList = new ArrayList<>();
        oldReplacePlanQueryResultList.add(new Object[]{1, "propertyDefinitionName", "configurationValue"});
        List<Object[]> newReplacePlanQueryResultList = new ArrayList<>();
        newReplacePlanQueryResultList.add(new Object[]{2, "propertyDefinitionName", "configurationValue"});

        when(queryOld.getResultList()).thenReturn(oldReplacePlanQueryResultList);
        when(queryNew.getResultList()).thenReturn(newReplacePlanQueryResultList);


        DeploymentService deploymentServiceNew = mock(DeploymentService.class);
        DeploymentService deploymentServiceOld = mock(DeploymentService.class);

        when(this.deploymentServiceRepository.getOne(1)).thenReturn(deploymentServiceOld);
        when(this.deploymentServiceRepository.getOne(2)).thenReturn(deploymentServiceNew);

        ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
        when(deploymentServiceOld.getService()).thenReturn(releaseVersionService);
        when(releaseVersionService.getFinalName()).thenReturn("FinalName");
        when(releaseVersionService.getVersion()).thenReturn("Version");
        HardwarePack hardwarePack = mock(HardwarePack.class);
        when(deploymentServiceOld.getHardwarePack()).thenReturn(hardwarePack);
        List<DeploymentServiceFilesystem> deploymentServiceFilesystemsList = new ArrayList<>();
        when(deploymentServiceOld.getDeploymentServiceFilesystems()).thenReturn(deploymentServiceFilesystemsList);
        when(deploymentServiceOld.getNumberOfInstances()).thenReturn(1);
        when(deploymentServiceOld.getMemoryFactor()).thenReturn(5);

        ReleaseVersionService releaseVersionServiceNew = mock(ReleaseVersionService.class);
        when(deploymentServiceNew.getService()).thenReturn(releaseVersionServiceNew);
        when(releaseVersionServiceNew.getFinalName()).thenReturn("FinalName");
        when(releaseVersionServiceNew.getVersion()).thenReturn("Version");
        when(deploymentServiceNew.getHardwarePack()).thenReturn(hardwarePack);
        when(deploymentServiceNew.getDeploymentServiceFilesystems()).thenReturn(deploymentServiceFilesystemsList);
        when(deploymentServiceNew.getNumberOfInstances()).thenReturn(2);
        when(deploymentServiceNew.getMemoryFactor()).thenReturn(5);


        //getDeploymentService().getDeploymentSubsystem().getDeploymentPlan().getEnvironment()
        DeploymentSubsystem deploymentSubsystemNew = mock(DeploymentSubsystem.class);
        when(deploymentServiceNew.getDeploymentSubsystem()).thenReturn(deploymentSubsystemNew);
        DeploymentPlan deploymentPlanNew = mock(DeploymentPlan.class);
        when(deploymentSubsystemNew.getDeploymentPlan()).thenReturn(deploymentPlanNew);
        when(deploymentPlanNew.getEnvironment()).thenReturn(Environment.INT.getEnvironment());


        DeploymentSubsystem deploymentSubsystemOld = mock(DeploymentSubsystem.class);
        when(deploymentServiceOld.getDeploymentSubsystem()).thenReturn(deploymentSubsystemOld);
        DeploymentPlan deploymentPlanOld = mock(DeploymentPlan.class);
        when(deploymentSubsystemOld.getDeploymentPlan()).thenReturn(deploymentPlanOld);
        when(deploymentPlanOld.getEnvironment()).thenReturn(Environment.INT.getEnvironment());

        when(jdkParameterRepository.findByDeploymentService(any())).thenReturn(new ArrayList<>());

        Map<String, List<DeploymentService>> res = this.deploymentReplacePlanService.getUncommonReplacePlan(1, 2);

        Assert.assertEquals(4, res.size());
        Assert.assertEquals(0, res.get(DeploymentConstants.CREATE_SERVICE).size());
        Assert.assertEquals(0, res.get(DeploymentConstants.REMOVE_SERVICE).size());
        Assert.assertEquals(0, res.get(DeploymentConstants.RESTART_SERVICE).size());
        Assert.assertEquals(1, res.get(DeploymentConstants.UPDATE_INSTANCE).size());

        Assert.assertEquals(deploymentServiceNew, res.get(DeploymentConstants.UPDATE_INSTANCE).get(0));

    }

    @Test
    void getUncommonReplacePlanChangeNameVersion()
    {
        Query queryOld = mock(Query.class);
        Query queryNew = mock(Query.class);
        when(entityManager.createNativeQuery("SELECT DS.ID, PD.NAME, CV.VALUE FROM DEPLOYMENT_PLAN DP INNER JOIN DEPLOYMENT_SUBSYSTEM DSB ON DP.ID = DSB.DEPLOYMENT_PLAN_ID INNER JOIN DEPLOYMENT_SERVICE DS ON DSB.ID = DS.DEPLOYMENT_SUBSYSTEM_ID INNER JOIN RELEASE_VERSION_SERVICE RVS ON DS.SERVICE_ID = RVS.ID LEFT JOIN CONFIGURATION_REVISION CR ON DP.ID=CR.DEPLOYMENT_PLAN_ID LEFT JOIN PROPERTY_DEFINITION PD ON RVS.ID = PD.SERVICE_ID LEFT JOIN CONFIGURATION_VALUE CV ON CR.ID = CV.REVISION_ID AND PD.ID = CV.DEFINITION_ID WHERE DP.ID =2 ORDER BY DS.ID ASC "))
                .thenReturn(queryNew);
        when(entityManager.createNativeQuery("SELECT DS.ID, PD.NAME, CV.VALUE FROM DEPLOYMENT_PLAN DP INNER JOIN DEPLOYMENT_SUBSYSTEM DSB ON DP.ID = DSB.DEPLOYMENT_PLAN_ID INNER JOIN " +
                "DEPLOYMENT_SERVICE DS ON DSB.ID = DS.DEPLOYMENT_SUBSYSTEM_ID INNER JOIN RELEASE_VERSION_SERVICE RVS ON DS.SERVICE_ID = RVS.ID LEFT JOIN CONFIGURATION_REVISION CR ON DP.ID=CR.DEPLOYMENT_PLAN_ID LEFT JOIN PROPERTY_DEFINITION PD ON RVS.ID = PD.SERVICE_ID LEFT JOIN CONFIGURATION_VALUE CV ON CR.ID = CV.REVISION_ID AND PD.ID = CV.DEFINITION_ID WHERE DP.ID =1 ORDER BY DS.ID ASC "))
                .thenReturn(queryOld);

        List<Object[]> oldReplacePlanQueryResultList = new ArrayList<>();
        oldReplacePlanQueryResultList.add(new Object[]{1, "propertyDefinitionName", "configurationValue"});
        List<Object[]> newReplacePlanQueryResultList = new ArrayList<>();
        newReplacePlanQueryResultList.add(new Object[]{2, "propertyDefinitionName", "configurationValue"});

        when(queryOld.getResultList()).thenReturn(oldReplacePlanQueryResultList);
        when(queryNew.getResultList()).thenReturn(newReplacePlanQueryResultList);


        DeploymentService deploymentServiceNew = mock(DeploymentService.class);
        DeploymentService deploymentServiceOld = mock(DeploymentService.class);

        when(this.deploymentServiceRepository.getOne(1)).thenReturn(deploymentServiceOld);
        when(this.deploymentServiceRepository.getOne(2)).thenReturn(deploymentServiceNew);

        ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
        when(deploymentServiceOld.getService()).thenReturn(releaseVersionService);
        when(releaseVersionService.getFinalName()).thenReturn("FinalName");
        when(releaseVersionService.getVersion()).thenReturn("Version");
        HardwarePack hardwarePack = mock(HardwarePack.class);
        when(deploymentServiceOld.getHardwarePack()).thenReturn(hardwarePack);
        List<DeploymentServiceFilesystem> deploymentServiceFilesystemsList = new ArrayList<>();
        when(deploymentServiceOld.getDeploymentServiceFilesystems()).thenReturn(deploymentServiceFilesystemsList);
        when(deploymentServiceOld.getNumberOfInstances()).thenReturn(1);
        when(deploymentServiceOld.getMemoryFactor()).thenReturn(5);

        ReleaseVersionService releaseVersionServiceNew = mock(ReleaseVersionService.class);
        when(deploymentServiceNew.getService()).thenReturn(releaseVersionServiceNew);
        when(releaseVersionServiceNew.getFinalName()).thenReturn("FinalName");
        when(releaseVersionServiceNew.getVersion()).thenReturn("VersionNEW");
        when(deploymentServiceNew.getHardwarePack()).thenReturn(hardwarePack);
        when(deploymentServiceNew.getDeploymentServiceFilesystems()).thenReturn(deploymentServiceFilesystemsList);
        when(deploymentServiceNew.getNumberOfInstances()).thenReturn(2);
        when(deploymentServiceNew.getMemoryFactor()).thenReturn(5);


        //getDeploymentService().getDeploymentSubsystem().getDeploymentPlan().getEnvironment()
        DeploymentSubsystem deploymentSubsystemNew = mock(DeploymentSubsystem.class);
        when(deploymentServiceNew.getDeploymentSubsystem()).thenReturn(deploymentSubsystemNew);
        DeploymentPlan deploymentPlanNew = mock(DeploymentPlan.class);
        when(deploymentSubsystemNew.getDeploymentPlan()).thenReturn(deploymentPlanNew);
        when(deploymentPlanNew.getEnvironment()).thenReturn(Environment.INT.getEnvironment());


        DeploymentSubsystem deploymentSubsystemOld = mock(DeploymentSubsystem.class);
        when(deploymentServiceOld.getDeploymentSubsystem()).thenReturn(deploymentSubsystemOld);
        DeploymentPlan deploymentPlanOld = mock(DeploymentPlan.class);
        when(deploymentSubsystemOld.getDeploymentPlan()).thenReturn(deploymentPlanOld);
        when(deploymentPlanOld.getEnvironment()).thenReturn(Environment.INT.getEnvironment());

        Map<String, List<DeploymentService>> res = this.deploymentReplacePlanService.getUncommonReplacePlan(1, 2);

        Assert.assertEquals(4, res.size());
        Assert.assertEquals(1, res.get(DeploymentConstants.CREATE_SERVICE).size());
        Assert.assertEquals(1, res.get(DeploymentConstants.REMOVE_SERVICE).size());
        Assert.assertEquals(0, res.get(DeploymentConstants.RESTART_SERVICE).size());
        Assert.assertEquals(0, res.get(DeploymentConstants.UPDATE_INSTANCE).size());

        Assert.assertEquals(deploymentServiceNew, res.get(DeploymentConstants.CREATE_SERVICE).get(0));
        Assert.assertEquals(deploymentServiceOld, res.get(DeploymentConstants.REMOVE_SERVICE).get(0));
    }


    @Test
    void getUncommonReplacePlanChangeFinalName()
    {
        Query queryOld = mock(Query.class);
        Query queryNew = mock(Query.class);
        when(entityManager.createNativeQuery("SELECT DS.ID, PD.NAME, CV.VALUE FROM DEPLOYMENT_PLAN DP INNER JOIN DEPLOYMENT_SUBSYSTEM DSB ON DP.ID = DSB.DEPLOYMENT_PLAN_ID INNER JOIN DEPLOYMENT_SERVICE DS ON DSB.ID = DS.DEPLOYMENT_SUBSYSTEM_ID INNER JOIN RELEASE_VERSION_SERVICE RVS ON DS.SERVICE_ID = RVS.ID LEFT JOIN CONFIGURATION_REVISION CR ON DP.ID=CR.DEPLOYMENT_PLAN_ID LEFT JOIN PROPERTY_DEFINITION PD ON RVS.ID = PD.SERVICE_ID LEFT JOIN CONFIGURATION_VALUE CV ON CR.ID = CV.REVISION_ID AND PD.ID = CV.DEFINITION_ID WHERE DP.ID =2 ORDER BY DS.ID ASC "))
                .thenReturn(queryNew);
        when(entityManager.createNativeQuery("SELECT DS.ID, PD.NAME, CV.VALUE FROM DEPLOYMENT_PLAN DP INNER JOIN DEPLOYMENT_SUBSYSTEM DSB ON DP.ID = DSB.DEPLOYMENT_PLAN_ID INNER JOIN " +
                "DEPLOYMENT_SERVICE DS ON DSB.ID = DS.DEPLOYMENT_SUBSYSTEM_ID INNER JOIN RELEASE_VERSION_SERVICE RVS ON DS.SERVICE_ID = RVS.ID LEFT JOIN CONFIGURATION_REVISION CR ON DP.ID=CR.DEPLOYMENT_PLAN_ID LEFT JOIN PROPERTY_DEFINITION PD ON RVS.ID = PD.SERVICE_ID LEFT JOIN CONFIGURATION_VALUE CV ON CR.ID = CV.REVISION_ID AND PD.ID = CV.DEFINITION_ID WHERE DP.ID =1 ORDER BY DS.ID ASC "))
                .thenReturn(queryOld);

        List<Object[]> oldReplacePlanQueryResultList = new ArrayList<>();
        oldReplacePlanQueryResultList.add(new Object[]{1, "propertyDefinitionName", "configurationValue"});
        List<Object[]> newReplacePlanQueryResultList = new ArrayList<>();
        newReplacePlanQueryResultList.add(new Object[]{2, "propertyDefinitionName", "configurationValue"});

        when(queryOld.getResultList()).thenReturn(oldReplacePlanQueryResultList);
        when(queryNew.getResultList()).thenReturn(newReplacePlanQueryResultList);


        DeploymentService deploymentServiceNew = mock(DeploymentService.class);
        DeploymentService deploymentServiceOld = mock(DeploymentService.class);

        when(this.deploymentServiceRepository.getOne(1)).thenReturn(deploymentServiceOld);
        when(this.deploymentServiceRepository.getOne(2)).thenReturn(deploymentServiceNew);

        ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
        when(deploymentServiceOld.getService()).thenReturn(releaseVersionService);
        when(releaseVersionService.getFinalName()).thenReturn("FinalName");
        when(releaseVersionService.getVersion()).thenReturn("Version");
        HardwarePack hardwarePack = mock(HardwarePack.class);
        when(deploymentServiceOld.getHardwarePack()).thenReturn(hardwarePack);
        List<DeploymentServiceFilesystem> deploymentServiceFilesystemsList = new ArrayList<>();
        when(deploymentServiceOld.getDeploymentServiceFilesystems()).thenReturn(deploymentServiceFilesystemsList);
        when(deploymentServiceOld.getNumberOfInstances()).thenReturn(1);
        when(deploymentServiceOld.getMemoryFactor()).thenReturn(5);

        ReleaseVersionService releaseVersionServiceNew = mock(ReleaseVersionService.class);
        when(deploymentServiceNew.getService()).thenReturn(releaseVersionServiceNew);
        when(releaseVersionServiceNew.getFinalName()).thenReturn("FinalNameNEW");
        when(releaseVersionServiceNew.getVersion()).thenReturn("Version");
        when(deploymentServiceNew.getHardwarePack()).thenReturn(hardwarePack);
        when(deploymentServiceNew.getDeploymentServiceFilesystems()).thenReturn(deploymentServiceFilesystemsList);
        when(deploymentServiceNew.getNumberOfInstances()).thenReturn(2);
        when(deploymentServiceNew.getMemoryFactor()).thenReturn(5);


        //getDeploymentService().getDeploymentSubsystem().getDeploymentPlan().getEnvironment()
        DeploymentSubsystem deploymentSubsystemNew = mock(DeploymentSubsystem.class);
        when(deploymentServiceNew.getDeploymentSubsystem()).thenReturn(deploymentSubsystemNew);
        DeploymentPlan deploymentPlanNew = mock(DeploymentPlan.class);
        when(deploymentSubsystemNew.getDeploymentPlan()).thenReturn(deploymentPlanNew);
        when(deploymentPlanNew.getEnvironment()).thenReturn(Environment.INT.getEnvironment());


        DeploymentSubsystem deploymentSubsystemOld = mock(DeploymentSubsystem.class);
        when(deploymentServiceOld.getDeploymentSubsystem()).thenReturn(deploymentSubsystemOld);
        DeploymentPlan deploymentPlanOld = mock(DeploymentPlan.class);
        when(deploymentSubsystemOld.getDeploymentPlan()).thenReturn(deploymentPlanOld);
        when(deploymentPlanOld.getEnvironment()).thenReturn(Environment.INT.getEnvironment());

        Map<String, List<DeploymentService>> res = this.deploymentReplacePlanService.getUncommonReplacePlan(1, 2);

        Assert.assertEquals(4, res.size());
        Assert.assertEquals(1, res.get(DeploymentConstants.CREATE_SERVICE).size());
        Assert.assertEquals(1, res.get(DeploymentConstants.REMOVE_SERVICE).size());
        Assert.assertEquals(0, res.get(DeploymentConstants.RESTART_SERVICE).size());
        Assert.assertEquals(0, res.get(DeploymentConstants.UPDATE_INSTANCE).size());

        Assert.assertEquals(deploymentServiceNew, res.get(DeploymentConstants.CREATE_SERVICE).get(0));
        Assert.assertEquals(deploymentServiceOld, res.get(DeploymentConstants.REMOVE_SERVICE).get(0));
    }



    @Test
    void getUncommonReplacePlanChangePropertyDefinitionName()
    {
        Query queryOld = mock(Query.class);
        Query queryNew = mock(Query.class);
        when(entityManager.createNativeQuery("SELECT DS.ID, PD.NAME, CV.VALUE FROM DEPLOYMENT_PLAN DP INNER JOIN DEPLOYMENT_SUBSYSTEM DSB ON DP.ID = DSB.DEPLOYMENT_PLAN_ID INNER JOIN DEPLOYMENT_SERVICE DS ON DSB.ID = DS.DEPLOYMENT_SUBSYSTEM_ID INNER JOIN RELEASE_VERSION_SERVICE RVS ON DS.SERVICE_ID = RVS.ID LEFT JOIN CONFIGURATION_REVISION CR ON DP.ID=CR.DEPLOYMENT_PLAN_ID LEFT JOIN PROPERTY_DEFINITION PD ON RVS.ID = PD.SERVICE_ID LEFT JOIN CONFIGURATION_VALUE CV ON CR.ID = CV.REVISION_ID AND PD.ID = CV.DEFINITION_ID WHERE DP.ID =2 ORDER BY DS.ID ASC "))
                .thenReturn(queryNew);
        when(entityManager.createNativeQuery("SELECT DS.ID, PD.NAME, CV.VALUE FROM DEPLOYMENT_PLAN DP INNER JOIN DEPLOYMENT_SUBSYSTEM DSB ON DP.ID = DSB.DEPLOYMENT_PLAN_ID INNER JOIN " +
                "DEPLOYMENT_SERVICE DS ON DSB.ID = DS.DEPLOYMENT_SUBSYSTEM_ID INNER JOIN RELEASE_VERSION_SERVICE RVS ON DS.SERVICE_ID = RVS.ID LEFT JOIN CONFIGURATION_REVISION CR ON DP.ID=CR.DEPLOYMENT_PLAN_ID LEFT JOIN PROPERTY_DEFINITION PD ON RVS.ID = PD.SERVICE_ID LEFT JOIN CONFIGURATION_VALUE CV ON CR.ID = CV.REVISION_ID AND PD.ID = CV.DEFINITION_ID WHERE DP.ID =1 ORDER BY DS.ID ASC "))
                .thenReturn(queryOld);

        List<Object[]> oldReplacePlanQueryResultList = new ArrayList<>();
        oldReplacePlanQueryResultList.add(new Object[]{1, "propertyDefinitionName", "configurationValue"});
        List<Object[]> newReplacePlanQueryResultList = new ArrayList<>();
        newReplacePlanQueryResultList.add(new Object[]{2, "propertyDefinitionNameNew", "configurationValue"});

        when(queryOld.getResultList()).thenReturn(oldReplacePlanQueryResultList);
        when(queryNew.getResultList()).thenReturn(newReplacePlanQueryResultList);


        DeploymentService deploymentServiceNew = mock(DeploymentService.class);
        DeploymentService deploymentServiceOld = mock(DeploymentService.class);

        when(this.deploymentServiceRepository.getOne(1)).thenReturn(deploymentServiceOld);
        when(this.deploymentServiceRepository.getOne(2)).thenReturn(deploymentServiceNew);

        ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
        when(deploymentServiceOld.getService()).thenReturn(releaseVersionService);
        when(releaseVersionService.getFinalName()).thenReturn("FinalName");
        when(releaseVersionService.getVersion()).thenReturn("Version");
        HardwarePack hardwarePack = mock(HardwarePack.class);
        when(deploymentServiceOld.getHardwarePack()).thenReturn(hardwarePack);
        List<DeploymentServiceFilesystem> deploymentServiceFilesystemsList = new ArrayList<>();
        when(deploymentServiceOld.getDeploymentServiceFilesystems()).thenReturn(deploymentServiceFilesystemsList);
        when(deploymentServiceOld.getNumberOfInstances()).thenReturn(1);
        when(deploymentServiceOld.getMemoryFactor()).thenReturn(5);

        ReleaseVersionService releaseVersionServiceNew = mock(ReleaseVersionService.class);
        when(deploymentServiceNew.getService()).thenReturn(releaseVersionServiceNew);
        when(releaseVersionServiceNew.getFinalName()).thenReturn("FinalName");
        when(releaseVersionServiceNew.getVersion()).thenReturn("Version");
        when(deploymentServiceNew.getHardwarePack()).thenReturn(hardwarePack);
        when(deploymentServiceNew.getDeploymentServiceFilesystems()).thenReturn(deploymentServiceFilesystemsList);
        when(deploymentServiceNew.getNumberOfInstances()).thenReturn(2);
        when(deploymentServiceNew.getMemoryFactor()).thenReturn(5);


        //getDeploymentService().getDeploymentSubsystem().getDeploymentPlan().getEnvironment()
        DeploymentSubsystem deploymentSubsystemNew = mock(DeploymentSubsystem.class);
        when(deploymentServiceNew.getDeploymentSubsystem()).thenReturn(deploymentSubsystemNew);
        DeploymentPlan deploymentPlanNew = mock(DeploymentPlan.class);
        when(deploymentSubsystemNew.getDeploymentPlan()).thenReturn(deploymentPlanNew);
        when(deploymentPlanNew.getEnvironment()).thenReturn(Environment.INT.getEnvironment());


        DeploymentSubsystem deploymentSubsystemOld = mock(DeploymentSubsystem.class);
        when(deploymentServiceOld.getDeploymentSubsystem()).thenReturn(deploymentSubsystemOld);
        DeploymentPlan deploymentPlanOld = mock(DeploymentPlan.class);
        when(deploymentSubsystemOld.getDeploymentPlan()).thenReturn(deploymentPlanOld);
        when(deploymentPlanOld.getEnvironment()).thenReturn(Environment.INT.getEnvironment());

        when(jdkParameterRepository.findByDeploymentService(any())).thenReturn(new ArrayList<>());
        Map<String, List<DeploymentService>> res = this.deploymentReplacePlanService.getUncommonReplacePlan(1, 2);

        Assert.assertEquals(4, res.size());
        Assert.assertEquals(0, res.get(DeploymentConstants.CREATE_SERVICE).size());
        Assert.assertEquals(0, res.get(DeploymentConstants.REMOVE_SERVICE).size());
        //siguiendo la politica que existia, se debe reiniciar por el cambio de propiedades y posteriormente crear las nuevas instancias
        Assert.assertEquals(1, res.get(DeploymentConstants.RESTART_SERVICE).size());
        Assert.assertEquals(1, res.get(DeploymentConstants.UPDATE_INSTANCE).size());

        Assert.assertEquals(deploymentServiceOld, res.get(DeploymentConstants.RESTART_SERVICE).get(0));
        Assert.assertEquals(deploymentServiceNew, res.get(DeploymentConstants.UPDATE_INSTANCE).get(0));
    }



    @Test
    void getUncommonReplacePlanChangeMemoryFactor()
    {
        Query queryOld = mock(Query.class);
        Query queryNew = mock(Query.class);
        when(entityManager.createNativeQuery("SELECT DS.ID, PD.NAME, CV.VALUE FROM DEPLOYMENT_PLAN DP INNER JOIN DEPLOYMENT_SUBSYSTEM DSB ON DP.ID = DSB.DEPLOYMENT_PLAN_ID INNER JOIN DEPLOYMENT_SERVICE DS ON DSB.ID = DS.DEPLOYMENT_SUBSYSTEM_ID INNER JOIN RELEASE_VERSION_SERVICE RVS ON DS.SERVICE_ID = RVS.ID LEFT JOIN CONFIGURATION_REVISION CR ON DP.ID=CR.DEPLOYMENT_PLAN_ID LEFT JOIN PROPERTY_DEFINITION PD ON RVS.ID = PD.SERVICE_ID LEFT JOIN CONFIGURATION_VALUE CV ON CR.ID = CV.REVISION_ID AND PD.ID = CV.DEFINITION_ID WHERE DP.ID =2 ORDER BY DS.ID ASC "))
                .thenReturn(queryNew);
        when(entityManager.createNativeQuery("SELECT DS.ID, PD.NAME, CV.VALUE FROM DEPLOYMENT_PLAN DP INNER JOIN DEPLOYMENT_SUBSYSTEM DSB ON DP.ID = DSB.DEPLOYMENT_PLAN_ID INNER JOIN " +
                "DEPLOYMENT_SERVICE DS ON DSB.ID = DS.DEPLOYMENT_SUBSYSTEM_ID INNER JOIN RELEASE_VERSION_SERVICE RVS ON DS.SERVICE_ID = RVS.ID LEFT JOIN CONFIGURATION_REVISION CR ON DP.ID=CR.DEPLOYMENT_PLAN_ID LEFT JOIN PROPERTY_DEFINITION PD ON RVS.ID = PD.SERVICE_ID LEFT JOIN CONFIGURATION_VALUE CV ON CR.ID = CV.REVISION_ID AND PD.ID = CV.DEFINITION_ID WHERE DP.ID =1 ORDER BY DS.ID ASC "))
                .thenReturn(queryOld);

        List<Object[]> oldReplacePlanQueryResultList = new ArrayList<>();
        oldReplacePlanQueryResultList.add(new Object[]{1, "propertyDefinitionName", "configurationValue"});
        List<Object[]> newReplacePlanQueryResultList = new ArrayList<>();
        newReplacePlanQueryResultList.add(new Object[]{2, "propertyDefinitionName", "configurationValue"});

        when(queryOld.getResultList()).thenReturn(oldReplacePlanQueryResultList);
        when(queryNew.getResultList()).thenReturn(newReplacePlanQueryResultList);


        DeploymentService deploymentServiceNew = mock(DeploymentService.class);
        DeploymentService deploymentServiceOld = mock(DeploymentService.class);

        when(this.deploymentServiceRepository.getOne(1)).thenReturn(deploymentServiceOld);
        when(this.deploymentServiceRepository.getOne(2)).thenReturn(deploymentServiceNew);

        ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
        when(deploymentServiceOld.getService()).thenReturn(releaseVersionService);
        when(releaseVersionService.getFinalName()).thenReturn("FinalName");
        when(releaseVersionService.getVersion()).thenReturn("Version");
        HardwarePack hardwarePack = mock(HardwarePack.class);
        when(deploymentServiceOld.getHardwarePack()).thenReturn(hardwarePack);
        List<DeploymentServiceFilesystem> deploymentServiceFilesystemsList = new ArrayList<>();
        when(deploymentServiceOld.getDeploymentServiceFilesystems()).thenReturn(deploymentServiceFilesystemsList);
        when(deploymentServiceOld.getNumberOfInstances()).thenReturn(1);
        when(deploymentServiceOld.getMemoryFactor()).thenReturn(5);

        ReleaseVersionService releaseVersionServiceNew = mock(ReleaseVersionService.class);
        when(deploymentServiceNew.getService()).thenReturn(releaseVersionServiceNew);
        when(releaseVersionServiceNew.getFinalName()).thenReturn("FinalName");
        when(releaseVersionServiceNew.getVersion()).thenReturn("Version");
        when(deploymentServiceNew.getHardwarePack()).thenReturn(hardwarePack);
        when(deploymentServiceNew.getDeploymentServiceFilesystems()).thenReturn(deploymentServiceFilesystemsList);
        when(deploymentServiceNew.getNumberOfInstances()).thenReturn(2);
        when(deploymentServiceNew.getMemoryFactor()).thenReturn(6);


        //getDeploymentService().getDeploymentSubsystem().getDeploymentPlan().getEnvironment()
        DeploymentSubsystem deploymentSubsystemNew = mock(DeploymentSubsystem.class);
        when(deploymentServiceNew.getDeploymentSubsystem()).thenReturn(deploymentSubsystemNew);
        DeploymentPlan deploymentPlanNew = mock(DeploymentPlan.class);
        when(deploymentSubsystemNew.getDeploymentPlan()).thenReturn(deploymentPlanNew);
        when(deploymentPlanNew.getEnvironment()).thenReturn(Environment.INT.getEnvironment());


        DeploymentSubsystem deploymentSubsystemOld = mock(DeploymentSubsystem.class);
        when(deploymentServiceOld.getDeploymentSubsystem()).thenReturn(deploymentSubsystemOld);
        DeploymentPlan deploymentPlanOld = mock(DeploymentPlan.class);
        when(deploymentSubsystemOld.getDeploymentPlan()).thenReturn(deploymentPlanOld);
        when(deploymentPlanOld.getEnvironment()).thenReturn(Environment.INT.getEnvironment());

        Map<String, List<DeploymentService>> res = this.deploymentReplacePlanService.getUncommonReplacePlan(1, 2);

        Assert.assertEquals(4, res.size());
        Assert.assertEquals(1, res.get(DeploymentConstants.CREATE_SERVICE).size());
        Assert.assertEquals(1, res.get(DeploymentConstants.REMOVE_SERVICE).size());
        //siguiendo la politica que existia, se debe reiniciar por el cambio de propiedades y posteriormente crear las nuevas instancias
        Assert.assertEquals(0, res.get(DeploymentConstants.RESTART_SERVICE).size());
        Assert.assertEquals(0, res.get(DeploymentConstants.UPDATE_INSTANCE).size());

        Assert.assertEquals(deploymentServiceOld, res.get(DeploymentConstants.REMOVE_SERVICE).get(0));
        Assert.assertEquals(deploymentServiceNew, res.get(DeploymentConstants.CREATE_SERVICE).get(0));
    }



    @Test
    void getUncommonReplacePlanChangeFileSystem()
    {
        Query queryOld = mock(Query.class);
        Query queryNew = mock(Query.class);
        when(entityManager.createNativeQuery("SELECT DS.ID, PD.NAME, CV.VALUE FROM DEPLOYMENT_PLAN DP INNER JOIN DEPLOYMENT_SUBSYSTEM DSB ON DP.ID = DSB.DEPLOYMENT_PLAN_ID INNER JOIN DEPLOYMENT_SERVICE DS ON DSB.ID = DS.DEPLOYMENT_SUBSYSTEM_ID INNER JOIN RELEASE_VERSION_SERVICE RVS ON DS.SERVICE_ID = RVS.ID LEFT JOIN CONFIGURATION_REVISION CR ON DP.ID=CR.DEPLOYMENT_PLAN_ID LEFT JOIN PROPERTY_DEFINITION PD ON RVS.ID = PD.SERVICE_ID LEFT JOIN CONFIGURATION_VALUE CV ON CR.ID = CV.REVISION_ID AND PD.ID = CV.DEFINITION_ID WHERE DP.ID =2 ORDER BY DS.ID ASC "))
                .thenReturn(queryNew);
        when(entityManager.createNativeQuery("SELECT DS.ID, PD.NAME, CV.VALUE FROM DEPLOYMENT_PLAN DP INNER JOIN DEPLOYMENT_SUBSYSTEM DSB ON DP.ID = DSB.DEPLOYMENT_PLAN_ID INNER JOIN " +
                "DEPLOYMENT_SERVICE DS ON DSB.ID = DS.DEPLOYMENT_SUBSYSTEM_ID INNER JOIN RELEASE_VERSION_SERVICE RVS ON DS.SERVICE_ID = RVS.ID LEFT JOIN CONFIGURATION_REVISION CR ON DP.ID=CR.DEPLOYMENT_PLAN_ID LEFT JOIN PROPERTY_DEFINITION PD ON RVS.ID = PD.SERVICE_ID LEFT JOIN CONFIGURATION_VALUE CV ON CR.ID = CV.REVISION_ID AND PD.ID = CV.DEFINITION_ID WHERE DP.ID =1 ORDER BY DS.ID ASC "))
                .thenReturn(queryOld);

        List<Object[]> oldReplacePlanQueryResultList = new ArrayList<>();
        oldReplacePlanQueryResultList.add(new Object[]{1, "propertyDefinitionName", "configurationValue"});
        List<Object[]> newReplacePlanQueryResultList = new ArrayList<>();
        newReplacePlanQueryResultList.add(new Object[]{2, "propertyDefinitionName", "configurationValue"});

        when(queryOld.getResultList()).thenReturn(oldReplacePlanQueryResultList);
        when(queryNew.getResultList()).thenReturn(newReplacePlanQueryResultList);


        DeploymentService deploymentServiceNew = mock(DeploymentService.class);
        DeploymentService deploymentServiceOld = mock(DeploymentService.class);

        when(this.deploymentServiceRepository.getOne(1)).thenReturn(deploymentServiceOld);
        when(this.deploymentServiceRepository.getOne(2)).thenReturn(deploymentServiceNew);

        ReleaseVersionService releaseVersionService = mock(ReleaseVersionService.class);
        when(deploymentServiceOld.getService()).thenReturn(releaseVersionService);
        when(releaseVersionService.getFinalName()).thenReturn("FinalName");
        when(releaseVersionService.getVersion()).thenReturn("Version");
        HardwarePack hardwarePack = mock(HardwarePack.class);
        when(deploymentServiceOld.getHardwarePack()).thenReturn(hardwarePack);
        List<DeploymentServiceFilesystem> deploymentServiceFilesystemsListOld = new ArrayList<>();

        DeploymentServiceFilesystem deploymentServiceFilesystemOld = mock(DeploymentServiceFilesystem.class);
        deploymentServiceFilesystemsListOld.add(deploymentServiceFilesystemOld);
        when(deploymentServiceFilesystemOld.getVolumeBind()).thenReturn("volumeBind1");
        Filesystem filesystemOld = mock(Filesystem.class);
        when(deploymentServiceFilesystemOld.getFilesystem()).thenReturn(filesystemOld);
        when(filesystemOld.getId()).thenReturn(1);

        DeploymentServiceFilesystem deploymentServiceFilesystemOld2 = mock(DeploymentServiceFilesystem.class);
        deploymentServiceFilesystemsListOld.add(deploymentServiceFilesystemOld2);
        Filesystem filesystemOld2 = mock(Filesystem.class);
        when(deploymentServiceFilesystemOld2.getFilesystem()).thenReturn(filesystemOld2);
        when(filesystemOld2.getId()).thenReturn(2);

        when(deploymentServiceOld.getDeploymentServiceFilesystems()).thenReturn(deploymentServiceFilesystemsListOld);
        when(deploymentServiceOld.getNumberOfInstances()).thenReturn(1);
        when(deploymentServiceOld.getMemoryFactor()).thenReturn(5);

        ReleaseVersionService releaseVersionServiceNew = mock(ReleaseVersionService.class);
        when(deploymentServiceNew.getService()).thenReturn(releaseVersionServiceNew);
        when(releaseVersionServiceNew.getFinalName()).thenReturn("FinalName");
        when(releaseVersionServiceNew.getVersion()).thenReturn("Version");
        when(deploymentServiceNew.getHardwarePack()).thenReturn(hardwarePack);
        List<DeploymentServiceFilesystem> deploymentServiceFilesystemsListNew = new ArrayList<>();
        DeploymentServiceFilesystem deploymentServiceFilesystemNew = mock(DeploymentServiceFilesystem.class);
        deploymentServiceFilesystemsListNew.add(deploymentServiceFilesystemNew);

        when(deploymentServiceFilesystemNew.getVolumeBind()).thenReturn("volumeBind1");
        Filesystem filesystemNew = mock(Filesystem.class);
        when(deploymentServiceFilesystemNew.getFilesystem()).thenReturn(filesystemNew);
        when(filesystemNew.getId()).thenReturn(1);

        DeploymentServiceFilesystem deploymentServiceFilesystemNew2 = mock(DeploymentServiceFilesystem.class);
        deploymentServiceFilesystemsListNew.add(deploymentServiceFilesystemNew2);
        Filesystem filesystemNew2 = mock(Filesystem.class);
        when(deploymentServiceFilesystemNew2.getFilesystem()).thenReturn(filesystemNew2);
        when(filesystemNew2.getId()).thenReturn(22);



        when(deploymentServiceNew.getDeploymentServiceFilesystems()).thenReturn(deploymentServiceFilesystemsListNew);
        when(deploymentServiceNew.getNumberOfInstances()).thenReturn(2);
        when(deploymentServiceNew.getMemoryFactor()).thenReturn(5);


        //getDeploymentService().getDeploymentSubsystem().getDeploymentPlan().getEnvironment()
        DeploymentSubsystem deploymentSubsystemNew = mock(DeploymentSubsystem.class);
        when(deploymentServiceNew.getDeploymentSubsystem()).thenReturn(deploymentSubsystemNew);
        DeploymentPlan deploymentPlanNew = mock(DeploymentPlan.class);
        when(deploymentSubsystemNew.getDeploymentPlan()).thenReturn(deploymentPlanNew);
        when(deploymentPlanNew.getEnvironment()).thenReturn(Environment.INT.getEnvironment());


        DeploymentSubsystem deploymentSubsystemOld = mock(DeploymentSubsystem.class);
        when(deploymentServiceOld.getDeploymentSubsystem()).thenReturn(deploymentSubsystemOld);
        DeploymentPlan deploymentPlanOld = mock(DeploymentPlan.class);
        when(deploymentSubsystemOld.getDeploymentPlan()).thenReturn(deploymentPlanOld);
        when(deploymentPlanOld.getEnvironment()).thenReturn(Environment.INT.getEnvironment());

        Map<String, List<DeploymentService>> res = this.deploymentReplacePlanService.getUncommonReplacePlan(1, 2);

        Assert.assertEquals(4, res.size());
        Assert.assertEquals(1, res.get(DeploymentConstants.CREATE_SERVICE).size());
        Assert.assertEquals(1, res.get(DeploymentConstants.REMOVE_SERVICE).size());
        //siguiendo la politica que existia, se debe reiniciar por el cambio de propiedades y posteriormente crear las nuevas instancias
        Assert.assertEquals(0, res.get(DeploymentConstants.RESTART_SERVICE).size());
        Assert.assertEquals(0, res.get(DeploymentConstants.UPDATE_INSTANCE).size());

        Assert.assertEquals(deploymentServiceOld, res.get(DeploymentConstants.REMOVE_SERVICE).get(0));
        Assert.assertEquals(deploymentServiceNew, res.get(DeploymentConstants.CREATE_SERVICE).get(0));
    }


    //this is not a good test because use reflection for test a private method but is more
    // clear than doing in the public method
    @Test
    public void isChangeJVM_notParam() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        ReplaceServiceInfo oldReplaceServiceInfo = mock(ReplaceServiceInfo.class);
        ReplaceServiceInfo newReplaceServiceInfo = mock(ReplaceServiceInfo.class);

        when(oldReplaceServiceInfo.getMemoryFactor()).thenReturn(50, 70);
        when(newReplaceServiceInfo.getMemoryFactor()).thenReturn(60, 70);

        Method methodIsChangeJVM = DeploymentReplacePlanService.class.getDeclaredMethod(
                "isChangeJVM", ReplaceServiceInfo.class, ReplaceServiceInfo.class);

        methodIsChangeJVM.setAccessible(true);

        boolean res = (Boolean) methodIsChangeJVM.invoke(deploymentReplacePlanService,
                newReplaceServiceInfo, oldReplaceServiceInfo);
        Assert.assertTrue(res);


        DeploymentService oldDeploymentService = mock(DeploymentService.class);
        when(oldDeploymentService.getId()).thenReturn(1);
        when(oldReplaceServiceInfo.getDeploymentService()).thenReturn(oldDeploymentService);
        DeploymentService newDeploymentService = mock(DeploymentService.class);
        when(newDeploymentService.getId()).thenReturn(2);
        when(newReplaceServiceInfo.getDeploymentService()).thenReturn(newDeploymentService);

        List<JdkParameter> oldJdkParamList = new ArrayList<>();
        List<JdkParameter> newJdkParamList = new ArrayList<>();

        //both list null
        when(jdkParameterRepository.findByDeploymentService(1))
                .thenReturn(null);
        when(jdkParameterRepository.findByDeploymentService(2))
                .thenReturn(null);
        res = (Boolean) methodIsChangeJVM.invoke(deploymentReplacePlanService,
                newReplaceServiceInfo, oldReplaceServiceInfo);
        Assert.assertFalse(res);


        when(jdkParameterRepository.findByDeploymentService(1))
                .thenReturn( null);
        when(jdkParameterRepository.findByDeploymentService(2))
                .thenReturn( newJdkParamList);
        res = (Boolean) methodIsChangeJVM.invoke(deploymentReplacePlanService,
                newReplaceServiceInfo, oldReplaceServiceInfo);
        Assert.assertTrue(res);


        when(jdkParameterRepository.findByDeploymentService(1))
                .thenReturn(oldJdkParamList);
        when(jdkParameterRepository.findByDeploymentService(2))
                .thenReturn(null);
        res = (Boolean) methodIsChangeJVM.invoke(deploymentReplacePlanService,
                newReplaceServiceInfo, oldReplaceServiceInfo);
        Assert.assertTrue(res);


        when(jdkParameterRepository.findByDeploymentService(1))
                .thenReturn(oldJdkParamList);
        when(jdkParameterRepository.findByDeploymentService(2))
                .thenReturn(newJdkParamList);
        res = (Boolean) methodIsChangeJVM.invoke(deploymentReplacePlanService,
                newReplaceServiceInfo, oldReplaceServiceInfo);
        Assert.assertFalse(res);
    }

    
    //this is not a good test because use reflection for test a private method but is more
    // clear than doing in the public method
    @Test
    public void isChangeJVM_Params() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        ReplaceServiceInfo oldReplaceServiceInfo = mock(ReplaceServiceInfo.class);
        ReplaceServiceInfo newReplaceServiceInfo = mock(ReplaceServiceInfo.class);

        when(oldReplaceServiceInfo.getMemoryFactor()).thenReturn(70);
        when(newReplaceServiceInfo.getMemoryFactor()).thenReturn(70);

        Method methodIsChangeJVM = DeploymentReplacePlanService.class.getDeclaredMethod(
                "isChangeJVM", ReplaceServiceInfo.class, ReplaceServiceInfo.class);

        methodIsChangeJVM.setAccessible(true);


        DeploymentService oldDeploymentService = mock(DeploymentService.class);
        when(oldDeploymentService.getId()).thenReturn(1);
        when(oldReplaceServiceInfo.getDeploymentService()).thenReturn(oldDeploymentService);
        DeploymentService newDeploymentService = mock(DeploymentService.class);
        when(newDeploymentService.getId()).thenReturn(2);
        when(newReplaceServiceInfo.getDeploymentService()).thenReturn(newDeploymentService);

        List<JdkParameter> oldJdkParamList = new ArrayList<>();
        List<JdkParameter> newJdkParamList = new ArrayList<>();

        JdkParameter oldJdkParameter1 = mock(JdkParameter.class);
        JdkParameter oldJdkParameter2 = mock(JdkParameter.class);
        JdkParameter newJdkParameter1 = mock(JdkParameter.class);
        JdkParameter newJdkParameter2 = mock(JdkParameter.class);

        oldJdkParamList.add(oldJdkParameter1);
        newJdkParamList.add(newJdkParameter1);
        newJdkParamList.add(newJdkParameter2);

        when(jdkParameterRepository.findByDeploymentService(1))
                .thenReturn(oldJdkParamList);
        when(jdkParameterRepository.findByDeploymentService(2))
                .thenReturn(newJdkParamList);
        boolean res = (Boolean) methodIsChangeJVM.invoke(deploymentReplacePlanService,
                newReplaceServiceInfo, oldReplaceServiceInfo);
        Assert.assertTrue(res);

        //valida la conversion a Set
        oldJdkParamList.add(oldJdkParameter1);
        res = (Boolean) methodIsChangeJVM.invoke(deploymentReplacePlanService,
                newReplaceServiceInfo, oldReplaceServiceInfo);
        Assert.assertTrue(res);


        oldJdkParamList.add(oldJdkParameter2);
        when(newJdkParameter1.toString()).thenReturn("newJdkParameter1");
        when(oldJdkParameter1.toString()).thenReturn("oldJdkParameter1");
        when(newJdkParameter2.toString()).thenReturn("newJdkParameter2");
        when(oldJdkParameter2.toString()).thenReturn("oldJdkParameter2");



        oldJdkParamList.add(oldJdkParameter1);
        res = (Boolean) methodIsChangeJVM.invoke(deploymentReplacePlanService,
                newReplaceServiceInfo, oldReplaceServiceInfo);
        Assert.assertTrue(res);


        when(newJdkParameter1.toString()).thenReturn("jdkParameter1");
        when(oldJdkParameter1.toString()).thenReturn("jdkParameter1");
        when(newJdkParameter2.toString()).thenReturn("jdkParameter2");
        when(oldJdkParameter2.toString()).thenReturn("jdkParameter2");

        oldJdkParamList.clear();
        oldJdkParamList.add(newJdkParameter1);
        oldJdkParamList.add(newJdkParameter2);
        res = (Boolean) methodIsChangeJVM.invoke(deploymentReplacePlanService,
                newReplaceServiceInfo, oldReplaceServiceInfo);
        Assert.assertFalse(res);
    }
}