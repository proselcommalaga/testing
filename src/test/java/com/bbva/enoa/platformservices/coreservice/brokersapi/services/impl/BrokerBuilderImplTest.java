package com.bbva.enoa.platformservices.coreservice.brokersapi.services.impl;

import com.bbva.enoa.apirestgen.brokersapi.model.*;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDeploymentServiceDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerDetailsDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerNodeDTO;
import com.bbva.enoa.apirestgen.brokersapi.model.BrokerUserDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.api.entities.*;
import com.bbva.enoa.datamodel.model.api.enumerates.AsyncBackToBackChannelType;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerNode;
import com.bbva.enoa.datamodel.model.broker.entities.BrokerUser;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.BrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.QueueBrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.entities.alerts.RateThresholdBrokerAlertConfig;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerRole;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.GenericBrokerAlertType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.QueueBrokerAlertType;
import com.bbva.enoa.datamodel.model.broker.enumerates.alerts.RateThresholdBrokerAlertType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.datastorage.entities.Filesystem;
import com.bbva.enoa.datamodel.model.datastorage.entities.FilesystemNova;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.deployment.entities.NovaDeploymentInstance;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionSubsystem;
import com.bbva.enoa.datamodel.model.resource.entities.BrokerPack;
import com.bbva.enoa.datamodel.model.resource.entities.HardwarePack;
import com.bbva.enoa.datamodel.model.user.enumerates.RoleType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.enums.BrokerUsageType;
import com.bbva.enoa.platformservices.coreservice.brokersapi.model.BrokerValidatedObjects;
import com.bbva.enoa.platformservices.coreservice.common.repositories.BrokerTaskRepository;
import com.bbva.enoa.platformservices.coreservice.common.services.CipherCredentialServiceImpl;
import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.MonitoringUtils;
import com.bbva.enoa.utils.clientsutils.consumers.impl.UsersClientImpl;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerPermissions.VIEW_BROKER_ADMIN_PASSWORD;
import static com.bbva.enoa.platformservices.coreservice.brokersapi.util.BrokerConstants.BrokerPermissions.VIEW_BROKER_SERVICE_PASSWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class BrokerBuilderImplTest
{

    private static final double cpus = 2.0;
    private static final int ramInMB = 512;
    private static final Integer FS_ID = 1111;
    private static final Integer PROD_ID = 2222;
    private static final Integer HW_PACK_ID = 3333;
    private static final Integer BROKER_ID = 420;
    private static final String BROKER_NAME = "brokerName";
    private static final String BROKER_DESCRIPTION = "Broker Description";
    private static final String IV_USER = "XE70505";
    public static final int SERVICE_PORT = 6969;
    public static final String GUEST = "guest";
    public static final String MONITORING_URL = "monitoring://url";

    @Mock
    private BrokerValidatorImpl brokerValidator;

    @Mock
    private MonitoringUtils monitoringUtils;

    @Mock
    private UsersClientImpl usersService;

    @Mock
    private CipherCredentialServiceImpl cipherCredentialService;

    @InjectMocks
    private BrokerBuilderImpl brokerBuilder;

    @Mock
    private BrokerTaskRepository brokerTaskRepository;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(brokerValidator);
        verifyNoMoreInteractions(monitoringUtils);
        verifyNoMoreInteractions(cipherCredentialService);
        verifyNoMoreInteractions(usersService);
    }

    @Nested
    class validateAndBuildBrokerEntity
    {
        BrokerDTOSupplier brokerDTOSupplier = new BrokerDTOSupplier();
        BrokerSupplier brokerSupplier = new BrokerSupplier();

        @Test
        void ok()
        {
            // given
            BrokerDTO brokerDto = brokerDTOSupplier.get();
            Product product = new Product();
            product.setId(PROD_ID);
            product.setUuaa("TEST");
            Filesystem filesystem = new FilesystemNova();
            filesystem.setId(FS_ID);
            BrokerPack hardwarePack = new BrokerPack();
            hardwarePack.setId(HW_PACK_ID);
            hardwarePack.setNumCPU(cpus);
            hardwarePack.setRamMB(ramInMB);

            Broker expectedBroker = brokerSupplier.get();
            expectedBroker.setEnvironment(brokerDto.getEnvironment());
            expectedBroker.setPlatform(Platform.valueOf(brokerDto.getPlatform()));
            expectedBroker.setType(BrokerType.valueOf(brokerDto.getType()));
            BrokerUser brokerUserAdmin = getBrokerUser(BrokerRole.ADMIN);
            BrokerUser brokerUserService = getBrokerUser(BrokerRole.SERVICE);
            expectedBroker.setUsers(List.of(brokerUserAdmin, brokerUserService));
            setBrokerAlertConfig(expectedBroker);

            BrokerValidatedObjects brokerValidatedObjects = new BrokerValidatedObjects(product, filesystem, hardwarePack);

            // when
            when(brokerValidator.validateBrokerDTO(brokerDto)).thenReturn(brokerValidatedObjects);
            when(cipherCredentialService.generateEncryptedRandomPassword(anyInt())).thenReturn("guest");

            // test
            Broker actualBroker = brokerBuilder.validateAndBuildBrokerEntity(brokerDto);

            // assertions
            verify(brokerValidator).validateBrokerDTO(brokerDto);
            verify(cipherCredentialService, times(2)).generateEncryptedRandomPassword(anyInt());

            // avoid to test calendars
            actualBroker.setLastModified(null);
            actualBroker.setStatusChanged(null);
            actualBroker.getHardwarePack().setCreationDate(null);
            expectedBroker.getHardwarePack().setCreationDate(null);


            // Fixme: en jira CIBNOVAP-436 (2022.q1.sprint4)Pr arreglar este test para que valide todos los campos // añadir distintos names y password para users
//            assertEquals(expectedBroker, actualBroker);
            assertExpectedBrokerEqualsActualBroker(expectedBroker, actualBroker);
        }
    }

    @Nested
    class buildBrokerDTOFromEntity
    {
        BrokerDTOSupplier brokerDTOSupplier = new BrokerDTOSupplier();
        BrokerSupplier brokerSupplier = new BrokerSupplier();
        BrokerNodeDTOSupplier brokerNodeDTOSupplier = new BrokerNodeDTOSupplier();
        BrokerNodeSupplier brokerNodeSupplier = new BrokerNodeSupplier();

        @Test
        void ok()
        {
            // when
            Broker broker = brokerSupplier.get();
            // users
            BrokerUser brokerUserService = getBrokerUser(BrokerRole.SERVICE);
            brokerUserService.setBroker(broker);
            BrokerUser brokerUserAdmin = getBrokerUser(BrokerRole.ADMIN);
            brokerUserAdmin.setBroker(broker);
            broker.setUsers(List.of(brokerUserAdmin, brokerUserService));
            // nodes
            BrokerNode brokerNode = brokerNodeSupplier.get();
            brokerNode.setBroker(broker);
            broker.setNodes(List.of(brokerNode));
            // alert configs
            setBrokerAlertConfig(broker);

            DeploymentService deploymentService1 = this.generateDeploymentService(DeploymentStatus.DEFINITION);
            DeploymentService deploymentService2 = this.generateDeploymentService(DeploymentStatus.DEPLOYED);
            DeploymentInstance deploymentInstance21 = new NovaDeploymentInstance();
            deploymentInstance21.setStarted(true);
            deploymentService2.setInstances(List.of(deploymentInstance21));
            DeploymentService deploymentService3 = this.generateDeploymentService(DeploymentStatus.DEPLOYED);
            DeploymentInstance deploymentInstance31 = new NovaDeploymentInstance();
            deploymentInstance31.setStarted(false);
            deploymentService3.setInstances(List.of(deploymentInstance31));
            broker.setDeploymentServices(List.of(deploymentService1, deploymentService2, deploymentService3, deploymentService3));

            when(monitoringUtils.getMonitoringUrlForBrokerNode(brokerNode)).thenReturn("monitoring://url");
            when(usersService.hasPermission(IV_USER, VIEW_BROKER_ADMIN_PASSWORD)).thenReturn(true);
            when(usersService.hasPermission(IV_USER, VIEW_BROKER_SERVICE_PASSWORD, broker.getEnvironment(), PROD_ID)).thenReturn(true);
            when(cipherCredentialService.decryptPassword(anyString())).thenReturn("guest");

            // expected
            BrokerDTO expectedBrokerDto = brokerDTOSupplier.get();
            expectedBrokerDto.setPlatform(broker.getPlatform().getName());
            expectedBrokerDto.setEnvironment(broker.getEnvironment());
            expectedBrokerDto.setType(broker.getType().getType());
            expectedBrokerDto.setCreationDate(String.valueOf(broker.getCreationDate()));
            // node
            BrokerNodeDTO brokerNodeDTO = brokerNodeDTOSupplier.get();
            brokerNodeDTO.setBrokerId(expectedBrokerDto.getId());
            // users
            BrokerUserDTO brokerUserAdminDTO = getBrokerUserDTO(BrokerRole.ADMIN);
            BrokerUserDTO brokerUserServiceDTO = getBrokerUserDTO(BrokerRole.SERVICE);
            // deployment services
            BrokerDeploymentServiceDTO deploymentServiceDTO1 = getBrokerDeploymentServiceDTO(DeploymentStatus.DEFINITION);
            BrokerDeploymentServiceDTO deploymentServiceDTO2 = getBrokerDeploymentServiceDTO(DeploymentStatus.DEPLOYED);
            deploymentServiceDTO2.setIsRunning(true);
            BrokerDeploymentServiceDTO deploymentServiceDTO3 = getBrokerDeploymentServiceDTO(DeploymentStatus.DEPLOYED);
            // details
            BrokerDetailsDTO brokerDetailsDTO = new BrokerDetailsDTO();
            brokerDetailsDTO.setNodes(new BrokerNodeDTO[]{brokerNodeDTO});
            brokerDetailsDTO.setUsers(new BrokerUserDTO[]{brokerUserAdminDTO, brokerUserServiceDTO});
            brokerDetailsDTO.setDeploymentServices(new BrokerDeploymentServiceDTO[]{deploymentServiceDTO1, deploymentServiceDTO2, deploymentServiceDTO3});
            expectedBrokerDto.setDetails(brokerDetailsDTO);
            // tasks
            PendingTaskDto pendingTaskDto1 = new PendingTaskDto();
            pendingTaskDto1.setAssignedRole(RoleType.SERVICE_SUPPORT.name());
            pendingTaskDto1.setIsTaskOfError(Boolean.TRUE);
            pendingTaskDto1.setTodoTaskId(RandomUtils.nextInt(1, 1000));
            expectedBrokerDto.setHasPendingTask(new PendingTaskDto[]{pendingTaskDto1});
            PendingTaskDto pendingTaskDto2 = new PendingTaskDto();
            pendingTaskDto2.setAssignedRole(RoleType.SERVICE_SUPPORT.name());
            pendingTaskDto2.setIsTaskOfError(Boolean.TRUE);
            pendingTaskDto2.setTodoTaskId(RandomUtils.nextInt(1, 1000));
            expectedBrokerDto.setHasPendingTask(new PendingTaskDto[]{pendingTaskDto2});


            // test
            BrokerDTO actualBrokerDTO = brokerBuilder.buildBrokerDTOFromEntity(IV_USER, broker);
            // assertions
            verify(monitoringUtils).getMonitoringUrlForBrokerNode(brokerNode);
            verify(usersService).hasPermission(IV_USER, VIEW_BROKER_ADMIN_PASSWORD);
            verify(usersService).hasPermission(IV_USER, VIEW_BROKER_SERVICE_PASSWORD, broker.getEnvironment(), PROD_ID);
            verify(cipherCredentialService, times(2)).decryptPassword(anyString());
            assertExpectedBrokerDTOEqualsActualBrokerDTO(expectedBrokerDto, actualBrokerDTO);
        }

        @Test
        void ok_withoutPermissionsToSeePassword()
        {
            // when
            Broker broker = brokerSupplier.get();
            // users
            BrokerUser brokerUserService = getBrokerUser(BrokerRole.SERVICE);
            brokerUserService.setBroker(broker);
            BrokerUser brokerUserAdmin = getBrokerUser(BrokerRole.ADMIN);
            brokerUserAdmin.setBroker(broker);
            broker.setUsers(List.of(brokerUserAdmin, brokerUserService));
            // nodes
            BrokerNode brokerNode = brokerNodeSupplier.get();
            brokerNode.setBroker(broker);
            broker.setNodes(List.of(brokerNode));
            //alert configs
            setBrokerAlertConfig(broker);

            when(monitoringUtils.getMonitoringUrlForBrokerNode(brokerNode)).thenReturn("monitoring://url");
            when(usersService.hasPermission(IV_USER, VIEW_BROKER_ADMIN_PASSWORD)).thenReturn(false);
            when(usersService.hasPermission(IV_USER, VIEW_BROKER_SERVICE_PASSWORD, broker.getEnvironment(), PROD_ID)).thenReturn(false);

            // expected
            BrokerDTO expectedBrokerDto = brokerDTOSupplier.get();
            expectedBrokerDto.setPlatform(broker.getPlatform().getName());
            expectedBrokerDto.setEnvironment(broker.getEnvironment());
            expectedBrokerDto.setType(broker.getType().getType());
            expectedBrokerDto.setCreationDate(String.valueOf(broker.getCreationDate()));
            // node
            BrokerNodeDTO brokerNodeDTO = brokerNodeDTOSupplier.get();
            brokerNodeDTO.setBrokerId(expectedBrokerDto.getId());
            // users
            BrokerUserDTO brokerUserAdminDTO = getBrokerUserDTO(BrokerRole.ADMIN);
            brokerUserAdminDTO.setPassword(null); // FORCE RESULT
            BrokerUserDTO brokerUserServiceDTO = getBrokerUserDTO(BrokerRole.SERVICE);
            brokerUserServiceDTO.setPassword(null); // FORCE RESULT
            // details
            BrokerDetailsDTO brokerDetailsDTO = new BrokerDetailsDTO();
            brokerDetailsDTO.setNodes(new BrokerNodeDTO[]{brokerNodeDTO});
            brokerDetailsDTO.setUsers(new BrokerUserDTO[]{brokerUserAdminDTO, brokerUserServiceDTO});
            expectedBrokerDto.setDetails(brokerDetailsDTO);
            // tasks
            PendingTaskDto pendingTaskDto1 = new PendingTaskDto();
            pendingTaskDto1.setAssignedRole(RoleType.SERVICE_SUPPORT.name());
            pendingTaskDto1.setIsTaskOfError(Boolean.TRUE);
            pendingTaskDto1.setTodoTaskId(RandomUtils.nextInt(1, 1000));
            expectedBrokerDto.setHasPendingTask(new PendingTaskDto[]{pendingTaskDto1});
            PendingTaskDto pendingTaskDto2 = new PendingTaskDto();
            pendingTaskDto2.setAssignedRole(RoleType.SERVICE_SUPPORT.name());
            pendingTaskDto2.setIsTaskOfError(Boolean.TRUE);
            pendingTaskDto2.setTodoTaskId(RandomUtils.nextInt(1, 1000));
            expectedBrokerDto.setHasPendingTask(new PendingTaskDto[]{pendingTaskDto2});


            // test
            BrokerDTO actualBrokerDTO = brokerBuilder.buildBrokerDTOFromEntity(IV_USER, broker);
            // assertions
            verify(monitoringUtils).getMonitoringUrlForBrokerNode(brokerNode);
            verify(usersService).hasPermission(IV_USER, VIEW_BROKER_ADMIN_PASSWORD);
            verify(usersService).hasPermission(IV_USER, VIEW_BROKER_SERVICE_PASSWORD, broker.getEnvironment(), PROD_ID);
            assertExpectedBrokerDTOEqualsActualBrokerDTO(expectedBrokerDto, actualBrokerDTO);
        }

        private DeploymentService generateDeploymentService(DeploymentStatus status)
        {
            Release release = new Release();
            release.setName("releaseName");

            Product product = new Product();
            product.setName("productName");
            product.setUuaa("uuaa");
            release.setProduct(product);

            ReleaseVersion rv = new ReleaseVersion();
            rv.setVersionName("rvName");
            rv.setRelease(release);

            DeploymentPlan plan = new DeploymentPlan();
            plan.setId(111);
            plan.setStatus(status);
            plan.setReleaseVersion(rv);

            DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
            deploymentSubsystem.setDeploymentPlan(plan);

            ReleaseVersionService service = new ReleaseVersionService();
            service.setServiceName("servicename");
            service.setServers(getApiImplementation());
            service.setConsumers(getApiImplementation());
            service.setServiceType(ServiceType.API_JAVA_SPRING_BOOT.getServiceType());

            DeploymentService deploymentService = new DeploymentService();
            deploymentService.setDeploymentSubsystem(deploymentSubsystem);

            ReleaseVersionSubsystem subsystem = new ReleaseVersionSubsystem();
            subsystem.setReleaseVersion(rv);
            service.setVersionSubsystem(subsystem);
            deploymentService.setService(service);

            return deploymentService;
        }
    }

    @Nested
    class buildBasicBrokerDTOFromEntity
    {
        BrokerDTOSupplier brokerDTOSupplier = new BrokerDTOSupplier();
        BrokerSupplier brokerSupplier = new BrokerSupplier();

        @Test
        void ok()
        {
            // when
            Broker broker = brokerSupplier.get();
            // expected
            BrokerDTO expectedBrokerDto = brokerDTOSupplier.get();
            expectedBrokerDto.setPlatform(broker.getPlatform().getName());
            expectedBrokerDto.setEnvironment(broker.getEnvironment());
            expectedBrokerDto.setType(broker.getType().getType());
            expectedBrokerDto.setCreationDate(String.valueOf(broker.getCreationDate()));

            // test
            BrokerDTO actualBrokerDTO = brokerBuilder.buildBasicBrokerDTOFromEntity(broker);
            // assertions
            assertExpectedBrokerDTOEqualsActualBrokerDTO(expectedBrokerDto, actualBrokerDTO);
        }
    }

    @Nested
    class buildBasicBrokerDTOFromEntityWithoutMonitoringURL
    {
        BrokerDTOSupplier brokerDTOSupplier = new BrokerDTOSupplier();
        BrokerSupplier brokerSupplier = new BrokerSupplier();

        @Test
        void ok()
        {
            // when
            Broker broker = brokerSupplier.get();

            // expected
            BrokerDTO expectedBrokerDto = brokerDTOSupplier.get();
            expectedBrokerDto.setPlatform(broker.getPlatform().getName());
            expectedBrokerDto.setEnvironment(broker.getEnvironment());
            expectedBrokerDto.setType(broker.getType().getType());
            expectedBrokerDto.setCreationDate(String.valueOf(broker.getCreationDate()));

            // test
            BrokerDTO actualBrokerDTO = brokerBuilder.buildBasicBrokerDTOFromEntityWithoutMonitoringURL(broker);
            // assertions
            assertExpectedBrokerDTOEqualsActualBrokerDTO(expectedBrokerDto, actualBrokerDTO);
        }
    }

    private void assertExpectedBrokerEqualsActualBroker(final Broker expectedBroker, final Broker actualBroker)
    {
        assertEquals(expectedBroker.getName(), actualBroker.getName());
        assertEquals(expectedBroker.getEnvironment(), actualBroker.getEnvironment());
        assertEquals(expectedBroker.getCpu(), actualBroker.getCpu());
        assertEquals(expectedBroker.getStatus(), actualBroker.getStatus());
        assertEquals(expectedBroker.getType(), actualBroker.getType());
        assertEquals(expectedBroker.getPlatform(), actualBroker.getPlatform());
        assertEquals(expectedBroker.getFilesystem().getId(), actualBroker.getFilesystem().getId());
        assertEquals(expectedBroker.getHardwarePack().getId(), actualBroker.getHardwarePack().getId());
        assertEquals(expectedBroker.getProduct().getId(), actualBroker.getProduct().getId());
        assertEquals(expectedBroker.getUsers().size(), actualBroker.getUsers().size());
        assertEquals(expectedBroker.getNodes().size(), actualBroker.getNodes().size());
        assertEquals(1, actualBroker.getQueueAlertConfigs().size());
        assertEquals(2, actualBroker.getRateAlertConfigs().size());
        assertEquals(2, actualBroker.getGenericAlertConfigs().size());
    }

    private void assertExpectedBrokerDTOEqualsActualBrokerDTO(final BrokerDTO expectedBroker, final BrokerDTO actualBroker)
    {
        assertEquals(expectedBroker.getName(), actualBroker.getName());
        assertEquals(expectedBroker.getEnvironment(), actualBroker.getEnvironment());
        assertEquals(expectedBroker.getCpu(), actualBroker.getCpu());
        assertEquals(expectedBroker.getStatus(), actualBroker.getStatus());
        assertEquals(expectedBroker.getType(), actualBroker.getType());
        assertEquals(expectedBroker.getPlatform(), actualBroker.getPlatform());
        assertEquals(expectedBroker.getFilesystemId(), actualBroker.getFilesystemId());
        assertEquals(expectedBroker.getHardwarePackId(), actualBroker.getHardwarePackId());
        assertEquals(expectedBroker.getProductId(), actualBroker.getProductId());
        assertExpectedBrokerDetailsDTOEqualsActualBrokerDetailsDTO(expectedBroker.getDetails() != null ? expectedBroker.getDetails() : new BrokerDetailsDTO(), actualBroker.getDetails() != null ? actualBroker.getDetails() : new BrokerDetailsDTO());

    }

    private void assertExpectedBrokerDetailsDTOEqualsActualBrokerDetailsDTO(final BrokerDetailsDTO expectedDetailsDTO, final BrokerDetailsDTO actualDetailsDTO)
    {
        assertExpectedBrokerNodesDTOEqualsActualBrokerNodesDTO(expectedDetailsDTO.getNodes() != null ? expectedDetailsDTO.getNodes() : new BrokerNodeDTO[0], actualDetailsDTO.getNodes() != null ? actualDetailsDTO.getNodes() : new BrokerNodeDTO[0]);
        assertExpectedBrokerUsersDTOEqualsActualBrokerUsersDTO(expectedDetailsDTO.getUsers() != null ? expectedDetailsDTO.getUsers() : new BrokerUserDTO[0], actualDetailsDTO.getUsers() != null ? actualDetailsDTO.getUsers() : new BrokerUserDTO[0]);
        assertExpectedBrokerDeploymentServicesDTOEqualsActualBrokerDeploymentServiceDTO(expectedDetailsDTO.getDeploymentServices() != null ? expectedDetailsDTO.getDeploymentServices() : new BrokerDeploymentServiceDTO[0],
                actualDetailsDTO.getDeploymentServices() != null ?
                        actualDetailsDTO.getDeploymentServices() : new BrokerDeploymentServiceDTO[0]);
    }

    private void assertExpectedBrokerNodesDTOEqualsActualBrokerNodesDTO(final BrokerNodeDTO[] expectedNodes, final BrokerNodeDTO[] actualNodes)
    {
        assertEquals(Arrays.asList(expectedNodes), Arrays.asList(actualNodes));
    }

    private void assertExpectedBrokerUsersDTOEqualsActualBrokerUsersDTO(final BrokerUserDTO[] expectedUsers, final BrokerUserDTO[] actualUsers)
    {
        assertEquals(Arrays.asList(expectedUsers), Arrays.asList(actualUsers));
    }

    private void assertExpectedBrokerDeploymentServicesDTOEqualsActualBrokerDeploymentServiceDTO(final BrokerDeploymentServiceDTO[] expectedServices, final BrokerDeploymentServiceDTO[] actualServices)
    {
        Assertions.assertEquals(expectedServices.length, actualServices.length);
        for (int i = 0; i < expectedServices.length; i++)
        {
            assertEquals(expectedServices[i], actualServices[i]);
        }
    }

    private static class BrokerDTOSupplier implements Supplier<BrokerDTO>
    {

        @Override
        public BrokerDTO get()
        {
            BrokerDTO brokerDTO = new BrokerDTO();
            brokerDTO.setId(BROKER_ID);
            brokerDTO.setName(BROKER_NAME);
            brokerDTO.setDescription(BROKER_DESCRIPTION);
            brokerDTO.setMonitoringUrl(MONITORING_URL);
            brokerDTO.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name());
            brokerDTO.setType(BrokerType.values()[RandomUtils.nextInt(0, BrokerType.values().length)].getType());
            brokerDTO.setStatus(BrokerStatus.CREATING.getStatus());
            brokerDTO.setPlatform(Platform.values()[RandomUtils.nextInt(0, Platform.values().length)].getName());
            brokerDTO.setFilesystemId(FS_ID);
            brokerDTO.setProductId(PROD_ID);
            brokerDTO.setHardwarePackId(HW_PACK_ID);
            brokerDTO.setCpu((float) cpus);
            brokerDTO.setMemory(ramInMB);
            brokerDTO.setNumberOfNodes(1);

            return brokerDTO;
        }
    }


    private static class BrokerSupplier implements Supplier<Broker>
    {

        @Override
        public Broker get()
        {

            Broker broker = new Broker();
            Product product = new Product();
            Filesystem filesystem = new FilesystemNova();
            BrokerPack hardwarePack = new BrokerPack();
            product.setId(PROD_ID);
            filesystem.setId(FS_ID);
            hardwarePack.setId(HW_PACK_ID);
            hardwarePack.setNumCPU(cpus);
            hardwarePack.setRamMB(ramInMB);
            broker.setProduct(product);
            broker.setHardwarePack(hardwarePack);
            broker.setFilesystem(filesystem);
            broker.setCpu(cpus);
            broker.setMemory(ramInMB);
            broker.setId(BROKER_ID);
            broker.setName(BROKER_NAME);
            broker.setDescription(BROKER_DESCRIPTION);
            broker.setEnvironment(Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].getEnvironment());
            broker.setType(BrokerType.values()[RandomUtils.nextInt(0, BrokerType.values().length)]);
            broker.setStatus(BrokerStatus.CREATING);
            broker.setPlatform(Platform.values()[RandomUtils.nextInt(0, Platform.values().length)]);

            return broker;
        }
    }

    private static class BrokerNodeDTOSupplier implements Supplier<BrokerNodeDTO>
    {

        @Override
        public BrokerNodeDTO get()
        {
            BrokerNodeDTO brokerNode = new BrokerNodeDTO();
            brokerNode.setServicePort(SERVICE_PORT);
            brokerNode.setMonitoringUrl(MONITORING_URL);
            brokerNode.setStatus(BrokerStatus.RUNNING.getStatus());
            return brokerNode;
        }

    }

    private static class BrokerNodeSupplier implements Supplier<BrokerNode>
    {

        @Override
        public BrokerNode get()
        {
            BrokerNode brokerNode = new BrokerNode();
            brokerNode.setServicePort(SERVICE_PORT);
            brokerNode.setStatus(BrokerStatus.RUNNING);
            return brokerNode;
        }

    }

    public static BrokerUser getBrokerUser(BrokerRole brokerRole)
    {
        BrokerUser brokerUser = new BrokerUser();
        brokerUser.setRole(brokerRole);
        brokerUser.setPassword(GUEST);
        brokerUser.setName(GUEST);
        return brokerUser;
    }

    public static void setBrokerAlertConfig(Broker broker)
    {
        broker.setQueueAlertConfigs(List.of(getQueueBrokerAlertConfig()));
        broker.setRateAlertConfigs(List.of(getRateThresholdBrokerAlertConfig(RateThresholdBrokerAlertType.CONSUMER_RATE_BELOW),
                getRateThresholdBrokerAlertConfig(RateThresholdBrokerAlertType.PUBLISH_RATE_ABOVE)));

        broker.setGenericAlertConfigs(List.of(getGenericBrokerAlertConfig(GenericBrokerAlertType.BROKER_HEALTH),
                getGenericBrokerAlertConfig(GenericBrokerAlertType.OVERFLOWED_BROKER)));
    }

    public static QueueBrokerAlertConfig getQueueBrokerAlertConfig()
    {
        QueueBrokerAlertConfig queueBrokerAlertConfig = new QueueBrokerAlertConfig();
        queueBrokerAlertConfig.setType(QueueBrokerAlertType.LENGTH_ABOVE);
        queueBrokerAlertConfig.setSendMail(true);
        queueBrokerAlertConfig.setSendPatrol(false);
        queueBrokerAlertConfig.setActive(false);
        queueBrokerAlertConfig.setTimeBetweenNotifications(0);
        queueBrokerAlertConfig.setThresholdQueueLength(null);
        return queueBrokerAlertConfig;
    }

    public static RateThresholdBrokerAlertConfig getRateThresholdBrokerAlertConfig(RateThresholdBrokerAlertType type)
    {
        RateThresholdBrokerAlertConfig rateThresholdBrokerAlertConfig = new RateThresholdBrokerAlertConfig();
        rateThresholdBrokerAlertConfig.setType(type);
        rateThresholdBrokerAlertConfig.setSendMail(true);
        rateThresholdBrokerAlertConfig.setSendPatrol(false);
        rateThresholdBrokerAlertConfig.setActive(false);
        rateThresholdBrokerAlertConfig.setThresholdRate(null);
        rateThresholdBrokerAlertConfig.setTimeBetweenNotifications(0);
        return rateThresholdBrokerAlertConfig;
    }

    public static BrokerAlertConfig getGenericBrokerAlertConfig(GenericBrokerAlertType type)
    {
        BrokerAlertConfig brokerAlertConfig = new BrokerAlertConfig();
        brokerAlertConfig.setType(type);
        brokerAlertConfig.setSendMail(true);
        brokerAlertConfig.setSendPatrol(false);
        brokerAlertConfig.setActive(true);
        brokerAlertConfig.setTimeBetweenNotifications(0);
        return brokerAlertConfig;
    }


    public static BrokerUserDTO getBrokerUserDTO(BrokerRole brokerRole)
    {
        BrokerUserDTO brokerUser = new BrokerUserDTO();
        brokerUser.setRole(brokerRole.getRole());
        brokerUser.setPassword(GUEST);
        brokerUser.setName(GUEST);
        return brokerUser;
    }

    public static BrokerDeploymentServiceDTO getBrokerDeploymentServiceDTO(DeploymentStatus deploymentStatus)
    {
        BrokerDeploymentServiceDTO deploymentServiceDTO = new BrokerDeploymentServiceDTO();
        deploymentServiceDTO.setDeploymentPlanId(111);
        deploymentServiceDTO.setServiceType(ServiceType.API_JAVA_SPRING_BOOT.name());
        deploymentServiceDTO.setServiceName("servicename");
        deploymentServiceDTO.setDeploymentPlanStatus(deploymentStatus.name());
        deploymentServiceDTO.setReleaseName("releaseName");
        deploymentServiceDTO.setReleaseVersionName("rvName");
        deploymentServiceDTO.setIsRunning(false);
        deploymentServiceDTO.setUuaa("uuaa");
        deploymentServiceDTO.setUsageType(BrokerUsageType.PUBLISHER.name());

        return deploymentServiceDTO;
    }

    public static List<ApiImplementation<?, ?, ?>> getApiImplementation()
    {
        List<ApiImplementation<?, ?, ?>> servers = new ArrayList<>();

        AsyncBackToBackApiImplementation asyncBackToBackApiImplementation = new AsyncBackToBackApiImplementation();
        AsyncBackToBackApiVersion apiVersion = new AsyncBackToBackApiVersion();
        AsyncBackToBackApi asyncBackToBackApi = new AsyncBackToBackApi();
        AsyncBackToBackApiChannel asyncBackToBackApiChannel = new AsyncBackToBackApiChannel();
        asyncBackToBackApiImplementation.setImplementedAs(ImplementedAs.SERVED);
        apiVersion.setVersion("1.0.0");
        asyncBackToBackApi.setUuaa("uuaa");
        asyncBackToBackApiChannel.setChannelType(AsyncBackToBackChannelType.PUBLISH);
        asyncBackToBackApiChannel.setOperationId("myChannel1Operation");
        asyncBackToBackApiChannel.setChannelName("mychannel1");
        asyncBackToBackApiImplementation.setApiVersion(apiVersion);
        apiVersion.setApi(asyncBackToBackApi);
        apiVersion.setAsyncBackToBackApiChannel(asyncBackToBackApiChannel);
        servers.add(asyncBackToBackApiImplementation);

        return servers;
    }
}