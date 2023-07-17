package com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.impl;

import com.bbva.enoa.apirestgen.deploymentsapi.model.DeploymentMigrationDto;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.config.entities.ConfigurationValue;
import com.bbva.enoa.datamodel.model.config.entities.PropertyDefinition;
import com.bbva.enoa.datamodel.model.config.enumerates.ManagementType;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnector;
import com.bbva.enoa.datamodel.model.connector.entities.LogicalConnectorProperty;
import com.bbva.enoa.datamodel.model.connector.enumerates.LogicalConnectorStatus;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.product.entities.DocSystem;
import com.bbva.enoa.platformservices.coreservice.common.util.JvmJdkConfigurationChecker;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ToolsClient;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.services.interfaces.IDeploymentBroker;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentConstants;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
import com.bbva.enoa.utils.clientsutils.services.interfaces.INovaActivityEmitter;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DeploymentMigratorTest
{

    @Mock
    private EntityManager entityManager;
    @Mock
    private ToolsClient toolsClient;
    @Mock
    private JvmJdkConfigurationChecker jvmJdkConfigurationChecker;
    @Mock
    private INovaActivityEmitter novaActivityEmitter;
    @Mock
    private IDeploymentBroker deploymentBroker;
    @Mock
    private DeploymentUtils deploymentUtils;
    @InjectMocks
    private DeploymentMigratorImpl deploymentMigrator;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Nested
    class TestMigratePlan
    {


        private List<DeploymentPlan> commonPreparePlans(
                boolean hasFilesystem, boolean hasConnector, boolean isMultiJdk)
        {
            // Source, plan 1
            DeploymentPlan origPlan = MocksAndUtils.createDeploymentPlan(
                    hasFilesystem, hasConnector, isMultiJdk, Environment.PRO);
            DeploymentSubsystem commonSubsystem = origPlan.getDeploymentSubsystems().get(0);
            TOSubsystemDTO productSubsystem1 = MocksAndUtils.createTOSubsystemDTO();

            //Target
            DeploymentPlan targetPlan = MocksAndUtils.createDeploymentPlan(
                    hasFilesystem, hasConnector, isMultiJdk, Environment.PRO);
            //El plan 2 tiene otro subsystem nuevo.
            DeploymentSubsystem subsystem0Plan2 = targetPlan.getDeploymentSubsystems().get(0);
            //El plan 2 tiene el mismo subsystem que plan 1, que es donde se copiarán las propiedades
            //Se clona el subsistema, pero se sobreesce el plan con targetPlan
            DeploymentSubsystem commonSubsystemCopy = SerializationUtils.clone(commonSubsystem);
            commonSubsystemCopy.setDeploymentPlan(targetPlan);
            targetPlan.getDeploymentSubsystems().add(commonSubsystemCopy);
            TOSubsystemDTO productSubsystem2 = MocksAndUtils.createTOSubsystemDTO();

            //Prepare mocks
            when(toolsClient.getSubsystemById(commonSubsystem.getSubsystem().getSubsystemId())).
                    thenReturn(productSubsystem1);
            when(toolsClient.getSubsystemById(subsystem0Plan2.getSubsystem().getSubsystemId())).
                    thenReturn(productSubsystem2);
            when(jvmJdkConfigurationChecker.isMultiJdk(any())).thenReturn(isMultiJdk);

            return Arrays.asList(new DeploymentPlan[]{origPlan, targetPlan});
        }

        private DeploymentMigrationDto commonVerifications(DeploymentMigrationDto response,
                                                           int entityManagerTimes, DeploymentPlan targetPlan)
        {
            //Verify
            assertTrue(Arrays.stream(response.getSubsystems()).anyMatch(
                    s -> "MIGRATED".equals(s.getStatus())
            ));

            Assertions.assertEquals(targetPlan.getId(), response.getPlanId());

            for (var deploymenSubsystem : targetPlan.getDeploymentSubsystems())
            {
                Assertions.assertTrue(
                        Arrays.stream(response.getSubsystems()).anyMatch(
                                ss -> ss.getSubsystemId().equals(deploymenSubsystem.getId())
                        )
                );
            }

            verify(entityManager, times(entityManagerTimes)).persist(any());
            verify(novaActivityEmitter).emitNewActivity(any());

            return response;
        }

        private DeploymentMigrationDto commonMigratePlanTest(
                boolean hasFilesystem, boolean hasConnector, boolean isMultiJdk, int entityManagerTimes)
        {
            List<DeploymentPlan> plans = commonPreparePlans(hasFilesystem, hasConnector, isMultiJdk);
            DeploymentPlan origPlan = plans.get(0);
            DeploymentPlan targetPlan = plans.get(1);

            //Execute
            DeploymentMigrationDto response = deploymentMigrator.migratePlan(origPlan, targetPlan);

            return commonVerifications(response, entityManagerTimes, targetPlan);
        }

        @Test
        void migratePlan()
        {
            boolean hasFilesystem = false;
            boolean hasConnector = false;
            boolean isMultiJdk = false;
            int entityManagerTimes = 3;

            commonMigratePlanTest(hasFilesystem, hasConnector, isMultiJdk, entityManagerTimes);
        }

        @Test
        void migratePlanWithFilesystems()
        {
            boolean hasFilesystem = true;
            boolean hasConnector = false;
            boolean isMultiJdk = false;
            int entityManagerTimes = 3;

            DeploymentMigrationDto response = commonMigratePlanTest(hasFilesystem, hasConnector, isMultiJdk, entityManagerTimes);

            assertTrue(Arrays.stream(response.getSubsystems()).
                    flatMap(ss -> Arrays.stream(
                            ss.getServices())
                    ).
                    anyMatch(serv -> "MIGRATED".equals(serv.getFilesystemStatus()))
            );
        }

        @Test
        void migratePlanWithConnector()
        {
            boolean hasFilesystem = false;
            boolean hasConnector = true;
            boolean isMultiJdk = false;
            int entityManagerTimes = 3;

            DeploymentMigrationDto response = commonMigratePlanTest(hasFilesystem, hasConnector, isMultiJdk, entityManagerTimes);

            assertTrue(Arrays.stream(response.getSubsystems()).
                    flatMap(ss -> Arrays.stream(
                            ss.getServices())
                    ).
                    anyMatch(serv -> "MIGRATED".equals(serv.getLogicalConnectorStatus()))
            );
        }

        @Test
        void migratePlanMultiJdk()
        {
            boolean hasFilesystem = false;
            boolean hasConnector = false;
            boolean isMultiJdk = true;
            int entityManagerTimes = 4;

            commonMigratePlanTest(hasFilesystem, hasConnector, isMultiJdk, entityManagerTimes);
        }

        @Test
        void migratePlanCurrentRevisionNull()
        {
            boolean hasFilesystem = false;
            boolean hasConnector = false;
            boolean isMultiJdk = false;

            // Source, plan 1
            DeploymentPlan plan1 = MocksAndUtils.createDeploymentPlan(
                    hasFilesystem, hasConnector, isMultiJdk, Environment.PRO);
            DeploymentSubsystem common_subsystem = plan1.getDeploymentSubsystems().get(0);
            TOSubsystemDTO productSubsystem1 = MocksAndUtils.createTOSubsystemDTO();
            plan1.setCurrentRevision(null);

            //Target
            DeploymentPlan plan2 = MocksAndUtils.createDeploymentPlan(
                    hasFilesystem, hasConnector, isMultiJdk, Environment.PRO);
            //El plan 2 tiene otro subsystem nuevo.
            DeploymentSubsystem subsystem0_plan2 = plan2.getDeploymentSubsystems().get(0);
            //El plan 2 tiene el mismo subsystem que plan 1, que es donde se copiarán las propiedades
            plan2.getDeploymentSubsystems().add(common_subsystem);
            TOSubsystemDTO productSubsystem2 = MocksAndUtils.createTOSubsystemDTO();

            //Prepare mocks
            when(toolsClient.getSubsystemById(common_subsystem.getSubsystem().getSubsystemId())).
                    thenReturn(productSubsystem1);
            when(toolsClient.getSubsystemById(subsystem0_plan2.getSubsystem().getSubsystemId())).
                    thenReturn(productSubsystem2);

            //Execute
            assertThrows(NovaException.class, () ->
                    deploymentMigrator.migratePlan(plan1, plan2)
            );
        }

        @Test
        void commonMigratePlanTest()
        {
            boolean hasFilesystem = true;
            boolean hasConnector = true;
            boolean isMultiJdk = true;
            int entityManagerTimes = 4;

            List<DeploymentPlan> plans = commonPreparePlans(hasFilesystem, hasConnector, isMultiJdk);
            DeploymentPlan origPlan = plans.get(0);
            DeploymentPlan targetPlan = plans.get(1);

            //Set Properties
            origPlan.getDeploymentSubsystems().forEach(
                    deploymentsusbsystem -> deploymentsusbsystem.getDeploymentServices().forEach(
                            deploymentService -> {

                                PropertyDefinition pd = MocksAndUtils.createPropertyDefinition(deploymentService.getService());
                                deploymentService.getService().
                                        setProperties(List.of(pd));

                                ConfigurationValue cv1 = populateConfigurationPlan(origPlan, pd);
                                ConfigurationValue cv2 = populateConfigurationPlan(targetPlan, pd);
                                //Test that before the test, the configurations are different
                                Assertions.assertNotEquals(cv1, cv2);
                            }
                    )
            );

            //Execute
            DeploymentMigrationDto response = deploymentMigrator.migratePlan(origPlan, targetPlan);

            commonVerifications(response, entityManagerTimes, targetPlan);

            Assertions.assertNotNull(response);
            //After the test, the configurations are the same
            Assertions.assertEquals(origPlan.getCurrentRevision().getConfigurations().get(0).getValue(),
                    targetPlan.getCurrentRevision().getConfigurations().get(0).getValue());
        }

        @Test
        void migratePlanPlatformHasChangedTest()
        {
            boolean hasFilesystem = false;
            boolean hasConnector = false;
            boolean isMultiJdk = false;
            int entityManagerTimes = 3;

            List<DeploymentPlan> plans = commonPreparePlans(hasFilesystem, hasConnector, isMultiJdk);
            DeploymentPlan origPlan = plans.get(0);
            DeploymentPlan targetPlan = plans.get(1);

            // Changed platform for deploy
            origPlan.setSelectedDeploy(Platform.ETHER);
            targetPlan.setSelectedDeploy(Platform.NOVA);

            // Changed environment
            origPlan.setEnvironment(Environment.PRE.getEnvironment());
            targetPlan.setEnvironment(Environment.PRE.getEnvironment());

            //Execute
            DeploymentMigrationDto response = deploymentMigrator.migratePlan(origPlan, targetPlan);

            //Verify
            Assertions.assertNotNull(response);
            assertTrue(Arrays.stream(response.getSubsystems()).anyMatch(
                    s -> "PENDING".equals(s.getStatus())
            ));

            verify(entityManager, times(entityManagerTimes)).persist(any());
            verify(novaActivityEmitter).emitNewActivity(any());
        }

        @Test
        void migratePlanPlatformHasChangedTestAndTargetEnvironmentIsPro()
        {
            boolean hasFilesystem = false;
            boolean hasConnector = false;
            boolean isMultiJdk = false;

            List<DeploymentPlan> plans = commonPreparePlans(hasFilesystem, hasConnector, isMultiJdk);
            DeploymentPlan origPlan = plans.get(0);
            DeploymentPlan targetPlan = plans.get(1);

            // Changed platform for deploy
            origPlan.setSelectedDeploy(Platform.ETHER);
            targetPlan.setSelectedDeploy(Platform.NOVA);

            //Execute
            NovaException exception = assertThrows(NovaException.class, () -> deploymentMigrator.migratePlan(origPlan, targetPlan));

            //Verify
            assertEquals(DeploymentConstants.DeployErrors.PLATFORM_CONFLICT, exception.getErrorCode().getErrorCode());
        }

        @Test
        void migratePlanWithEphoenixService()
        {
            boolean hasFilesystem = false;
            boolean hasConnector = false;
            boolean isMultiJdk = false;
            int entityManagerTimes = 3;

            List<DeploymentPlan> plans = commonPreparePlans(hasFilesystem, hasConnector, isMultiJdk);
            DeploymentPlan origPlan = plans.get(0);
            DeploymentPlan targetPlan = plans.get(1);

            origPlan.getDeploymentSubsystems().forEach(
                    deploymentsusbsystem -> deploymentsusbsystem.getDeploymentServices().forEach(
                            deploymentService -> deploymentService.getService().setServiceType(ServiceType.EPHOENIX_ONLINE.getServiceType())
                    )
            );

            targetPlan.getDeploymentSubsystems().forEach(
                    deploymentsusbsystem -> deploymentsusbsystem.getDeploymentServices().forEach(
                            deploymentService -> deploymentService.getService().setServiceType(ServiceType.EPHOENIX_ONLINE.getServiceType())
                    )
            );

            // When
            when(deploymentUtils.getNumberOfInstancesForEphoenixService(any(), any())).thenReturn(2);

            // Execute
            DeploymentMigrationDto response = deploymentMigrator.migratePlan(origPlan, targetPlan);

            // Verify
            Assertions.assertNotNull(response);
            commonVerifications(response, entityManagerTimes, targetPlan);
        }

        private ConfigurationValue populateConfigurationPlan(DeploymentPlan plan, PropertyDefinition pd)
        {
            ConfigurationValue cv = new ConfigurationValue();
            cv.setDefinition(pd);
            cv.setValue("ConfigurationValue" + ThreadLocalRandom.current().nextInt());
            plan.getCurrentRevision().getConfigurations().add(cv);
            return cv;
        }


        @Nested
        class TestCopyConfiguration
        {
            @Test
            void testNullCurrentRevision()
            {
                DeploymentPlan originPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);
                originPlan.setCurrentRevision(null);
                DeploymentPlan targetPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);

                DeploymentService originService = originPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
                DeploymentService targetService = targetPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

                NovaException exception = assertThrows(NovaException.class, () -> deploymentMigrator.copyConfiguration(originService, targetService));

                assertEquals(DeploymentConstants.DeployErrors.PLAN_WITHOUT_CURRENT_REVISION, exception.getErrorCode().getErrorCode());
            }

            @Test
            void testNoPropertiesToCopy()
            {
                DeploymentPlan originPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);
                DeploymentPlan targetPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);

                DeploymentService originService = originPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
                DeploymentService targetService = targetPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

                List<String> nonMatchingProperties = deploymentMigrator.copyConfiguration(originService, targetService);

                assertTrue(nonMatchingProperties.isEmpty());
            }

            @Test
            void testNotCommonConnector()
            {
                DeploymentPlan originPlan = MocksAndUtils.createDeploymentPlan(false, true, false, Environment.INT);
                DeploymentPlan targetPlan = MocksAndUtils.createDeploymentPlan(false, true, false, Environment.INT);

                DeploymentService originService = originPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
                DeploymentService targetService = targetPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

                List<String> nonMatchingProperties = deploymentMigrator.copyConfiguration(originService, targetService);

                assertTrue(nonMatchingProperties.isEmpty());
            }

            @Test
            void testCopyConnectorProperties()
            {
                DeploymentPlan originPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);
                DeploymentPlan targetPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);

                DeploymentService originService = originPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
                DeploymentService targetService = targetPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

                LogicalConnector logicalConnector = new LogicalConnector();
                logicalConnector.setLogicalConnectorStatus(LogicalConnectorStatus.CREATED);
                logicalConnector.setId(RandomUtils.nextInt());
                logicalConnector.setMsaDocument(new DocSystem());
                logicalConnector.setEnvironment(Environment.INT.getEnvironment());
                originService.setLogicalConnectors(List.of(logicalConnector));
                targetService.setLogicalConnectors(List.of(logicalConnector));

                LogicalConnectorProperty logicalConnectorProperty1 = new LogicalConnectorProperty();
                LogicalConnectorProperty logicalConnectorProperty2 = new LogicalConnectorProperty();
                logicalConnector.setLogConnProp(List.of(logicalConnectorProperty1, logicalConnectorProperty2));


                List<String> nonMatchingProperties = deploymentMigrator.copyConfiguration(originService, targetService);

                assertTrue(nonMatchingProperties.isEmpty());
                assertEquals(2, targetPlan.getCurrentRevision().getDeploymentConnectorProperties().size());
            }

            @Test
            void testCopyProperties()
            {
                DeploymentPlan originPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);
                DeploymentPlan targetPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);

                DeploymentService originService = originPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
                DeploymentService targetService = targetPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

                PropertyDefinition property1 = MocksAndUtils.createPropertyDefinition(originService.getService());
                originService.getService().setProperties(List.of(property1));
                targetService.getService().setProperties(List.of(property1));

                ConfigurationValue originValue = populateConfigurationPlan(originPlan, property1);
                originValue.setValue("value1");
                ConfigurationValue targetValue = populateConfigurationPlan(targetPlan, property1);


                List<String> nonMatchingProperties = deploymentMigrator.copyConfiguration(originService, targetService);

                assertTrue(nonMatchingProperties.isEmpty());
                assertEquals("value1", targetValue.getValue());
            }

            @Test
            void testNonMatchingProperties()
            {
                DeploymentPlan originPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);
                DeploymentPlan targetPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);

                DeploymentService originService = originPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
                DeploymentService targetService = targetPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

                PropertyDefinition property1 = MocksAndUtils.createPropertyDefinition(originService.getService());
                PropertyDefinition property2 = MocksAndUtils.createPropertyDefinition(originService.getService());
                property2.setName("property2Name");
                originService.getService().setProperties(List.of(property1));
                targetService.getService().setProperties(List.of(property1, property2));

                ConfigurationValue originValue = populateConfigurationPlan(originPlan, property1);
                originValue.setValue("value1");
                ConfigurationValue targetValue = populateConfigurationPlan(targetPlan, property1);
                populateConfigurationPlan(targetPlan, property2);


                List<String> nonMatchingProperties = deploymentMigrator.copyConfiguration(originService, targetService);

                assertEquals(1, nonMatchingProperties.size());
                assertEquals("property2Name", nonMatchingProperties.get(0));
                assertEquals("value1", targetValue.getValue());
            }

            @Test
            void testNoCopyBrokerProperties()
            {
                DeploymentPlan originPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);
                DeploymentPlan targetPlan = MocksAndUtils.createDeploymentPlan(false, false, false, Environment.INT);

                DeploymentService originService = originPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);
                DeploymentService targetService = targetPlan.getDeploymentSubsystems().get(0).getDeploymentServices().get(0);

                PropertyDefinition property1 = MocksAndUtils.createPropertyDefinition(originService.getService());
                PropertyDefinition property2 = MocksAndUtils.createPropertyDefinition(originService.getService());
                property1.setManagement(ManagementType.SERVICE);
                property2.setManagement(ManagementType.BROKER);
                originService.getService().setProperties(List.of(property1, property2));
                targetService.getService().setProperties(List.of(property1, property2));

                ConfigurationValue originValue = populateConfigurationPlan(originPlan, property1);
                ConfigurationValue originValue2 = populateConfigurationPlan(originPlan, property2);
                originValue.setValue("value1");
                originValue2.setValue("value2");
                ConfigurationValue targetValue = populateConfigurationPlan(targetPlan, property1);
                ConfigurationValue targetValue2 = populateConfigurationPlan(targetPlan, property2);
                targetValue.setValue("targetvalue1");
                targetValue2.setValue("targetvalue2");


                List<String> nonMatchingProperties = deploymentMigrator.copyConfiguration(originService, targetService);

                assertTrue(nonMatchingProperties.isEmpty());
                assertEquals("value1", targetValue.getValue());
                assertEquals("targetvalue2", targetValue2.getValue());
            }

        }
    }
}
