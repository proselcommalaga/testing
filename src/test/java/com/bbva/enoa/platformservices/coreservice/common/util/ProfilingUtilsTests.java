//package com.bbva.enoa.platformservices.coreservice.common.util;
//
//import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
//import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
//import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
//import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
//import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
//import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
//import com.bbva.enoa.datamodel.model.profile.entities.CesRole;
//import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
//import com.bbva.enoa.platformservices.coreservice.common.repositories.CesRoleRepository;
//import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IApiGatewayManagerClient;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import java.util.*;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//import static org.mockito.Mockito.*;
//
//public class ProfilingUtilsTests
//{
//    @InjectMocks
//    private ProfilingUtils profilingUtils;
//
//    @Mock
//    private CesRoleRepository cesRoleRepository;
//
//    @Mock
//    private IApiGatewayManagerClient apiGatewayManagerClient;
//
//    @BeforeEach
//    public void setUp() throws Exception
//    {
//        MockitoAnnotations.initMocks(this);
//        ReflectionTestUtils.setField(this.profilingUtils, "cesEnabled", Boolean.TRUE);
//    }
//
//    @Test
//    public void test_updateRoles_ok_addedAndDeleted()
//    {
//        String uuaa = "JGMV";
//        String env = "PRU";
//
//        Set<CesRole> originRoles = IntStream.rangeClosed(1, 3)
//                .mapToObj(operand -> new CesRole(uuaa, env, Integer.toString(operand), new HashSet<>()))
//                .collect(Collectors.toSet());
//
//        String[] updateRoles = IntStream.rangeClosed(2, 4)
//                .mapToObj(Integer::toString)
//                .toArray(String[]::new);
//
//        when(this.cesRoleRepository.findAllByUuaaAndEnvironment(uuaa, env)).thenReturn(originRoles);
//        when(this.apiGatewayManagerClient.getRoles(uuaa, env)).thenReturn(updateRoles);
//        when(this.cesRoleRepository.saveAll(any())).then(invocationOnMock -> new ArrayList<>(invocationOnMock.getArgument(0)));
//
//        Set<CesRole> ret = Arrays.stream(this.profilingUtils.updateRoles(uuaa, env))
//                .collect(Collectors.toSet());
//
//        Assertions.assertEquals(3, ret.size());
//        Assertions.assertTrue(ret.contains(new CesRole(uuaa, env, "2", new HashSet<>())));
//        Assertions.assertTrue(ret.contains(new CesRole(uuaa, env, "3", new HashSet<>())));
//        Assertions.assertTrue(ret.contains(new CesRole(uuaa, env, "4", new HashSet<>())));
//
//        verify(this.cesRoleRepository).findAllByUuaaAndEnvironment(uuaa, env);
//        verify(this.apiGatewayManagerClient).getRoles(uuaa, env);
//        verify(this.cesRoleRepository).saveAll(
//                argThat(cesRoles -> {
//                    SingleApiClientResponseWrapper<Integer> contador = new SingleApiClientResponseWrapper<>(0);
//                    cesRoles.iterator().forEachRemaining(
//                            cesRole -> {
//                                contador.set(contador.get() + 1);
//                                Assertions.assertEquals("4", cesRole.getRol());
//                            });
//                    return contador.get() == 1;
//                })
//        );
//        verify(this.cesRoleRepository).delete(new CesRole(uuaa, env, "1", new HashSet<>()));
//        verifyAllMocks();
//    }
//
//    @Test
//    public void test_updateRoles_ok_deleted()
//    {
//        String uuaa = "JGMV";
//        String env = "PRU";
//
//        Set<CesRole> originRoles = IntStream.rangeClosed(1, 3)
//                .mapToObj(operand -> new CesRole(uuaa, env, Integer.toString(operand), new HashSet<>()))
//                .collect(Collectors.toSet());
//
//        String[] updateRoles = IntStream.rangeClosed(2, 3)
//                .mapToObj(Integer::toString)
//                .toArray(String[]::new);
//
//        when(this.cesRoleRepository.findAllByUuaaAndEnvironment(uuaa, env)).thenReturn(originRoles);
//        when(this.apiGatewayManagerClient.getRoles(uuaa, env)).thenReturn(updateRoles);
//
//        Set<CesRole> ret = Arrays.stream(this.profilingUtils.updateRoles(uuaa, env))
//                .collect(Collectors.toSet());
//
//        Assertions.assertEquals(2, ret.size());
//        Assertions.assertTrue(ret.contains(new CesRole(uuaa, env, "2", new HashSet<>())));
//        Assertions.assertTrue(ret.contains(new CesRole(uuaa, env, "3", new HashSet<>())));
//
//        verify(this.cesRoleRepository).findAllByUuaaAndEnvironment(uuaa, env);
//        verify(this.apiGatewayManagerClient).getRoles(uuaa, env);
//        verify(this.cesRoleRepository).delete(new CesRole(uuaa, env, "1", new HashSet<>()));
//        verifyAllMocks();
//    }
//
//    @Test
//    public void test_updateRoles_ok_added()
//    {
//        String uuaa = "JGMV";
//        String env = "PRU";
//
//        Set<CesRole> originRoles = IntStream.rangeClosed(1, 3)
//                .mapToObj(operand -> new CesRole(uuaa, env, Integer.toString(operand), new HashSet<>()))
//                .collect(Collectors.toSet());
//
//        String[] updateRoles = IntStream.rangeClosed(1, 4)
//                .mapToObj(Integer::toString)
//                .toArray(String[]::new);
//
//        when(this.cesRoleRepository.findAllByUuaaAndEnvironment(uuaa, env)).thenReturn(originRoles);
//        when(this.apiGatewayManagerClient.getRoles(uuaa, env)).thenReturn(updateRoles);
//        when(this.cesRoleRepository.saveAll(any())).then(invocationOnMock -> new ArrayList<>(invocationOnMock.getArgument(0)));
//
//        Set<CesRole> ret = Arrays.stream(this.profilingUtils.updateRoles(uuaa, env))
//                .collect(Collectors.toSet());
//
//        Assertions.assertEquals(4, ret.size());
//        Assertions.assertTrue(ret.contains(new CesRole(uuaa, env, "1", new HashSet<>())));
//        Assertions.assertTrue(ret.contains(new CesRole(uuaa, env, "2", new HashSet<>())));
//        Assertions.assertTrue(ret.contains(new CesRole(uuaa, env, "3", new HashSet<>())));
//        Assertions.assertTrue(ret.contains(new CesRole(uuaa, env, "4", new HashSet<>())));
//
//        verify(this.cesRoleRepository).findAllByUuaaAndEnvironment(uuaa, env);
//        verify(this.apiGatewayManagerClient).getRoles(uuaa, env);
//        verify(this.cesRoleRepository).saveAll(
//                argThat(cesRoles -> {
//                    SingleApiClientResponseWrapper<Integer> contador = new SingleApiClientResponseWrapper<>(0);
//                    cesRoles.iterator().forEachRemaining(
//                            cesRole -> {
//                                contador.set(contador.get() + 1);
//                                Assertions.assertEquals("4", cesRole.getRol());
//                            });
//                    return contador.get() == 1;
//                })
//        );
//        verifyAllMocks();
//    }
//
//    @Test
//    public void test_updateRoles_ok_noChanges()
//    {
//        String uuaa = "JGMV";
//        String env = "PRU";
//
//        Set<CesRole> originRoles = IntStream.rangeClosed(1, 3)
//                .mapToObj(operand -> new CesRole(uuaa, env, Integer.toString(operand), new HashSet<>()))
//                .collect(Collectors.toSet());
//
//        String[] updateRoles = IntStream.rangeClosed(1, 3)
//                .mapToObj(Integer::toString)
//                .toArray(String[]::new);
//
//        when(this.cesRoleRepository.findAllByUuaaAndEnvironment(uuaa, env)).thenReturn(originRoles);
//        when(this.apiGatewayManagerClient.getRoles(uuaa, env)).thenReturn(updateRoles);
//
//        Set<CesRole> ret = Arrays.stream(this.profilingUtils.updateRoles(uuaa, env))
//                .collect(Collectors.toSet());
//
//        Assertions.assertEquals(3, ret.size());
//        Assertions.assertTrue(ret.contains(new CesRole(uuaa, env, "1", new HashSet<>())));
//        Assertions.assertTrue(ret.contains(new CesRole(uuaa, env, "2", new HashSet<>())));
//        Assertions.assertTrue(ret.contains(new CesRole(uuaa, env, "3", new HashSet<>())));
//
//        verify(this.cesRoleRepository).findAllByUuaaAndEnvironment(uuaa, env);
//        verify(this.apiGatewayManagerClient).getRoles(uuaa, env);
//        verifyAllMocks();
//    }
//
//    @Test
//    public void test_isPlanExposingApis_true()
//    {
//        DeploymentPlan deploymentPlan = new DeploymentPlan()
//                .setDeploymentSubsystems(Collections.singletonList(
//                                new DeploymentSubsystem()
//                                        .setDeploymentServices(Collections.singletonList(
//                                                        new DeploymentService()
//                                                                .setService(
//                                                                        new ReleaseVersionService()
//                                                                                .setApiImplementations(Collections.singletonList(
//                                                                                                new ApiImplementation()
//                                                                                                        .setImplementedAs(ImplementedAs.SERVED)
//                                                                                        )
//                                                                                )
//                                                                )
//                                                )
//                                        )
//                        )
//                );
//
//        boolean ret = this.profilingUtils.isPlanExposingApis(deploymentPlan);
//
//        Assertions.assertTrue(ret);
//        verifyAllMocks();
//    }
//
//    @Test
//    public void test_isPlanExposingApis_false()
//    {
//        DeploymentPlan deploymentPlan = new DeploymentPlan()
//                .setDeploymentSubsystems(Collections.singletonList(
//                                new DeploymentSubsystem()
//                                        .setDeploymentServices(Collections.singletonList(
//                                                        new DeploymentService()
//                                                                .setService(
//                                                                        new ReleaseVersionService()
//                                                                                .setApiImplementations(Collections.singletonList(
//                                                                                                new ApiImplementation()
//                                                                                                        .setImplementedAs(ImplementedAs.CONSUMED)
//                                                                                        )
//                                                                                )
//                                                                )
//                                                )
//                                        )
//                        )
//                );
//
//        boolean ret = this.profilingUtils.isPlanExposingApis(deploymentPlan);
//
//        Assertions.assertFalse(ret);
//        verifyAllMocks();
//    }
//
//    private void verifyAllMocks()
//    {
//        verifyNoMoreInteractions(
//                this.apiGatewayManagerClient,
//                this.cesRoleRepository
//        );
//    }
//
//}
