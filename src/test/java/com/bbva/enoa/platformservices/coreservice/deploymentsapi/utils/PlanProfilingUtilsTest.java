package com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.datamodel.model.api.entities.*;
import com.bbva.enoa.datamodel.model.api.enumerates.ImplementedAs;
import com.bbva.enoa.datamodel.model.api.enumerates.Verb;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentService;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentSubsystem;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.profile.entities.ApiMethodProfile;
import com.bbva.enoa.datamodel.model.profile.entities.CesRole;
import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
import com.bbva.enoa.datamodel.model.profile.enumerates.ProfileStatus;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.IApiGatewayService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.DeploymentPlanRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.PlanProfileRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PlanProfilingUtilsTest {
    private static final String CES_UUAA = "KKPF";

    @InjectMocks
    private PlanProfilingUtils planProfilingUtils;
    @Mock
    private DeploymentPlanRepository deploymentPlanRepository;
    @Mock
    private IApiGatewayService apiGatewayService;
    @Mock
    private PlanProfileRepository planProfileRepository;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);

        Field field = PlanProfilingUtils.class.getDeclaredField("cesUuaa");
        field.setAccessible(true);
        field.set(this.planProfilingUtils, CES_UUAA);
    }

    @AfterEach
    public void verifyAllMocks() {
        verifyNoMoreInteractions(
                this.deploymentPlanRepository,
                this.apiGatewayService,
                this.planProfileRepository
        );
    }

    @Nested
    class GetLastDeploymentPlanWithActiveProfiling {
        @Test
        public void withPreviousPlan() {
            Environment env = Environment.LOCAL;
            String releaseName = "Daniela";

            DeploymentPlan deploymentPlan = new DeploymentPlan();

            when(deploymentPlanRepository.getByReleaseNameAndEnvironmentAndPlanProfileStatus(any(), any(), any()))
                    .thenReturn(Collections.singletonList(deploymentPlan));

            DeploymentPlan ret = planProfilingUtils.getLastDeploymentPlanWithActiveProfiling(env.getEnvironment(), releaseName);

            assertEquals(deploymentPlan, ret);
            verify(deploymentPlanRepository).getByReleaseNameAndEnvironmentAndPlanProfileStatus(releaseName, env.getEnvironment(), ProfileStatus.ACTIVE);
        }

        @Test
        public void withoutPreviousPlan() {
            Environment env = Environment.LOCAL;
            String releaseName = "Daniela";

            when(deploymentPlanRepository.getByReleaseNameAndEnvironmentAndPlanProfileStatus(any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            DeploymentPlan ret = planProfilingUtils.getLastDeploymentPlanWithActiveProfiling(env.getEnvironment(), releaseName);

            assertNull(ret);
            verify(deploymentPlanRepository).getByReleaseNameAndEnvironmentAndPlanProfileStatus(releaseName, env.getEnvironment(), ProfileStatus.ACTIVE);
        }
    }

    @Nested
    class CreatePlanProfile {
        @Test
        public void singleApiMethod() {
            ApiMethod apiMethod = new ApiMethod();
            SyncApiVersion apiVersion = new SyncApiVersion();
            apiVersion.setApiMethods(Collections.singletonList(apiMethod));
            SyncApiImplementation apiImplementation = new SyncApiImplementation();
            apiImplementation.setImplementedAs(ImplementedAs.SERVED);
            apiImplementation.setApiVersion(apiVersion);

            ReleaseVersionService releaseVersionService = new ReleaseVersionService();
            releaseVersionService.setApiImplementations(Collections.singletonList(apiImplementation));

            DeploymentPlan deploymentPlan = new DeploymentPlan()
                    .setDeploymentSubsystems(Collections.singletonList(
                                    new DeploymentSubsystem()
                                            .setDeploymentServices(Collections.singletonList(
                                                            new DeploymentService()
                                                                    .setService(releaseVersionService)
                                                    )
                                            )
                            )
                    );

            PlanProfile ret = planProfilingUtils.createPlanProfile(deploymentPlan);

            assertEquals(ProfileStatus.PENDING, ret.getStatus());
            assertEquals(deploymentPlan, ret.getDeploymentPlan());
            assertEquals(1, ret.getApiMethodProfiles().size());
            assertEquals(apiMethod, ret.getApiMethodProfiles().get(0).getApiMethod());
        }

        @Test
        public void empty() {
            SyncApiVersion apiVersion = new SyncApiVersion();
            apiVersion.setApiMethods(Collections.singletonList(new ApiMethod()));
            SyncApiImplementation apiImplementation = new SyncApiImplementation();
            apiImplementation.setImplementedAs(ImplementedAs.CONSUMED);
            apiImplementation.setApiVersion(apiVersion);

            ReleaseVersionService releaseVersionService = new ReleaseVersionService();
            releaseVersionService.setApiImplementations(Collections.singletonList(apiImplementation));

            DeploymentPlan deploymentPlan = new DeploymentPlan()
                    .setDeploymentSubsystems(Collections.singletonList(
                                    new DeploymentSubsystem()
                                            .setDeploymentServices(Collections.singletonList(
                                                            new DeploymentService()
                                                                    .setService(releaseVersionService)
                                                    )
                                            )
                            )
                    );

            PlanProfile ret = planProfilingUtils.createPlanProfile(deploymentPlan);

            assertEquals(ProfileStatus.PENDING, ret.getStatus());
            assertEquals(deploymentPlan, ret.getDeploymentPlan());
            assertEquals(0, ret.getApiMethodProfiles().size());
        }

    }

    @Nested
    class IsSameProfiling {
        @Test
        public void sameProfiling() {
            PlanProfile oldPlanProfile = new PlanProfile()
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint1")
                                                    .setVerb(Verb.POST)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    )
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint2")
                                                    .setVerb(Verb.GET)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    );

            PlanProfile newPlanProfile = new PlanProfile()
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint1")
                                                    .setVerb(Verb.POST)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    )
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint2")
                                                    .setVerb(Verb.GET)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    );


            boolean ret = planProfilingUtils.isSameProfiling(
                    new DeploymentPlan().addPlanProfile(oldPlanProfile),
                    new DeploymentPlan().addPlanProfile(newPlanProfile)
            );

            Assertions.assertTrue(ret);
        }

        @Test
        public void emptyRoles() {
            PlanProfile oldPlanProfile = new PlanProfile()
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint1")
                                                    .setVerb(Verb.POST)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>())
                    )
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint2")
                                                    .setVerb(Verb.GET)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>())
                    );

            PlanProfile newPlanProfile = new PlanProfile()
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint1")
                                                    .setVerb(Verb.POST)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>())
                    )
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint2")
                                                    .setVerb(Verb.GET)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>())
                    );


            boolean ret = planProfilingUtils.isSameProfiling(
                    new DeploymentPlan().addPlanProfile(oldPlanProfile),
                    new DeploymentPlan().addPlanProfile(newPlanProfile)
            );

            Assertions.assertTrue(ret);
        }

        @Test
        public void profilingWithDiffentRoles() {
            PlanProfile oldPlanProfile = new PlanProfile()
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint1")
                                                    .setVerb(Verb.POST)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "PRE", "ROL_3", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    )
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint2")
                                                    .setVerb(Verb.GET)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    );

            PlanProfile newPlanProfile = new PlanProfile()
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint1")
                                                    .setVerb(Verb.POST)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    )
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint2")
                                                    .setVerb(Verb.GET)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    );


            boolean ret = planProfilingUtils.isSameProfiling(
                    new DeploymentPlan().addPlanProfile(oldPlanProfile),
                    new DeploymentPlan().addPlanProfile(newPlanProfile)
            );

            Assertions.assertFalse(ret);
        }

        @Test
        public void profilingWithDifferentsEndpoints() {
            PlanProfile oldPlanProfile = new PlanProfile()
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint1")
                                                    .setVerb(Verb.POST)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    )
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint2")
                                                    .setVerb(Verb.POST)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    );

            PlanProfile newPlanProfile = new PlanProfile()
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint1")
                                                    .setVerb(Verb.POST)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    )
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint2")
                                                    .setVerb(Verb.GET)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    );


            boolean ret = planProfilingUtils.isSameProfiling(
                    new DeploymentPlan().addPlanProfile(oldPlanProfile),
                    new DeploymentPlan().addPlanProfile(newPlanProfile)
            );

            Assertions.assertFalse(ret);
        }

        @Test
        public void profilingWithDifferentBasePath() {
            PlanProfile oldPlanProfile = new PlanProfile()
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint1")
                                                    .setVerb(Verb.POST)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    )
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint2")
                                                    .setVerb(Verb.GET)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    );

            PlanProfile newPlanProfile = new PlanProfile()
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint1")
                                                    .setVerb(Verb.POST)
                                                    .setApiVersion(buildSyncApiVersion("/newBasePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    )
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint2")
                                                    .setVerb(Verb.GET)
                                                    .setApiVersion(buildSyncApiVersion("/newBasePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    );


            boolean ret = planProfilingUtils.isSameProfiling(
                    new DeploymentPlan().addPlanProfile(oldPlanProfile),
                    new DeploymentPlan().addPlanProfile(newPlanProfile)
            );

            Assertions.assertFalse(ret);
        }

        @Test
        public void differentsApiMethodProfile() {
            PlanProfile oldPlanProfile = new PlanProfile()
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint1")
                                                    .setVerb(Verb.POST)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    );

            PlanProfile newPlanProfile = new PlanProfile()
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint1")
                                                    .setVerb(Verb.POST)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    )
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint2")
                                                    .setVerb(Verb.GET)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    );


            boolean ret = planProfilingUtils.isSameProfiling(
                    new DeploymentPlan().addPlanProfile(oldPlanProfile),
                    new DeploymentPlan().addPlanProfile(newPlanProfile)
            );

            Assertions.assertFalse(ret);
        }

    }

    @Nested
    class CopyPlanProfile {
        @Test
        public void isSameProfilingSameVersionOk() {
            ReleaseVersion releaseVersion = new ReleaseVersion();
            releaseVersion.setId(2);

            PlanProfile oldPlanProfile = new PlanProfile()
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint1")
                                                    .setVerb(Verb.POST)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    )
                    .addApiMethodProfile(
                            new ApiMethodProfile()
                                    .setApiMethod(
                                            new ApiMethod()
                                                    .setEndpoint("/endpoint2")
                                                    .setVerb(Verb.GET)
                                                    .setApiVersion(buildSyncApiVersion("/basePath"))
                                    )
                                    .setRoles(new HashSet<>(
                                            Arrays.asList(
                                                    new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                    new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                            )
                                    ))
                    );

            DeploymentPlan oldDeploymentPlan = new DeploymentPlan()
                    .setReleaseVersion(releaseVersion)
                    .setPlanProfiles(Collections.singletonList(oldPlanProfile));
            DeploymentPlan newDeploymentPlan = new DeploymentPlan()
                    .setReleaseVersion(releaseVersion);

            List<PlanProfile> copiedPlanProfile = planProfilingUtils.copyPlanProfile(
                    oldDeploymentPlan,
                    newDeploymentPlan
            );

            assertEquals(oldDeploymentPlan.getPlanProfiles().size(), copiedPlanProfile.size());
            Assertions.assertTrue(
                    copiedPlanProfile.stream()
                            .allMatch(source ->
                                    source.getDeploymentPlan().equals(newDeploymentPlan) &&
                                            source.getStatus().equals(ProfileStatus.PENDING) &&
                                            oldDeploymentPlan.getPlanProfiles().stream()
                                                    .anyMatch(target ->
                                                            source.getApiMethodProfiles().stream()
                                                                    .allMatch(sourceAMP ->
                                                                            sourceAMP.getPlanProfile().equals(source) &&
                                                                                    target.getApiMethodProfiles().stream()
                                                                                            .anyMatch(targetAMP ->
                                                                                                    sourceAMP.getApiMethod().equals(targetAMP.getApiMethod()) &&
                                                                                                            sourceAMP.getRoles().equals(targetAMP.getRoles())

                                                                                            )
                                                                    )
                                                    )

                            )
            );
        }

        @Test
        public void isSameProfilingDifferentVersionWithoutCommonApisOk() {
            //Inputs
            //Se crea la API comun
            SyncApiImplementation oldApiImpl = buildApiImplementation("Old");
            SyncApiImplementation newApiImpl = buildApiImplementation("New");
            //Se crea el planProfile original
            PlanProfile oldPlanProfile = buildPlanProfile(oldApiImpl.getApiVersion().getApiMethods().get(0));

            DeploymentPlan oldDeploymentPlan = buildDeploymentPlan(1, Collections.singletonList(oldApiImpl))
                    .setPlanProfiles(Collections.singletonList(oldPlanProfile));
            DeploymentPlan newDeploymentPlan = buildDeploymentPlan(2, Collections.singletonList(newApiImpl));

            Set<ISecurizableApiVersion<?, ?, ?>> newApiVersions = extractAPIList(newDeploymentPlan);

            //Method call
            PlanProfile planProfile = planProfilingUtils.copyPlanProfile(
                    oldDeploymentPlan,
                    newDeploymentPlan
            ).get(0);

            //Assertions
            assertEquals(newDeploymentPlan, planProfile.getDeploymentPlan());
            assertEquals(ProfileStatus.PENDING, planProfile.getStatus());
            //Comprobamos que para las APIs nuevas se hayan creado todos los metodos
            assertEquals(
                    newApiVersions.stream()
                            .flatMap(securizableApiVersion -> securizableApiVersion.getApiMethods().stream())
                            .collect(Collectors.toSet()),
                    planProfile.getApiMethodProfiles().stream()
                            .map(ApiMethodProfile::getApiMethod)
                            .collect(Collectors.toSet())
            );
            //Comprobamos que para las APIs nuevas se hayan creado el perfilado sin roles de todos los metodos
            Assertions.assertTrue(
                    planProfile.getApiMethodProfiles().stream()
                            .filter(apiMethodProfile -> newApiVersions.contains(apiMethodProfile.getApiMethod().getApiVersion()))
                            .allMatch(apiMethodProfile ->
                                    apiMethodProfile.getPlanProfile().equals(planProfile) &&
                                            apiMethodProfile.getRoles().isEmpty()
                            )
            );

        }

        @Test
        public void isSameProfilingDifferentVersionWithCommonApisOk() {
            //Inputs
            //Se crea la API comun
            SyncApiImplementation commonApiImpl = buildApiImplementation("Old");
            SyncApiImplementation newApiImpl = buildApiImplementation("New");
            //Se crea el planProfile original
            PlanProfile oldPlanProfile = buildPlanProfile(commonApiImpl.getApiVersion().getApiMethods().get(0));

            DeploymentPlan oldDeploymentPlan = buildDeploymentPlan(1, Collections.singletonList(commonApiImpl))
                    .setPlanProfiles(Collections.singletonList(oldPlanProfile));
            DeploymentPlan newDeploymentPlan = buildDeploymentPlan(2, Arrays.asList(
                            commonApiImpl,
                            newApiImpl
                    )
            );

            Set<ISecurizableApiVersion<?, ?, ?>> oldApis = extractAPIList(oldDeploymentPlan);
            Set<ISecurizableApiVersion<?, ?, ?>> newApis = extractAPIList(newDeploymentPlan);
            Set<ISecurizableApiVersion<?, ?, ?>> onlyNewApis = newApis.stream()
                    .filter(novaApi -> !oldApis.contains(novaApi))
                    .collect(Collectors.toSet());
            Set<ISecurizableApiVersion<?, ?, ?>> commonApis = newApis.stream()
                    .filter(oldApis::contains)
                    .collect(Collectors.toSet());

            //Method call
            PlanProfile planProfile = planProfilingUtils.copyPlanProfile(
                    oldDeploymentPlan,
                    newDeploymentPlan
            ).get(0);

            //Assertions
            assertEquals(newDeploymentPlan, planProfile.getDeploymentPlan());
            assertEquals(ProfileStatus.PENDING, planProfile.getStatus());
            //Comprobamos que para las APIs compartidas se hayan creado todos los metodos
            assertEquals(
                    commonApis.stream()
                            .flatMap(novaApi -> novaApi.getApiMethods().stream())
                            .collect(Collectors.toSet()),
                    planProfile.getApiMethodProfiles().stream()
                            .map(ApiMethodProfile::getApiMethod)
                            .filter(apiMethod -> commonApis.contains(apiMethod.getApiVersion()))
                            .collect(Collectors.toSet())
            );
            //Comprobamos que para las APIs compartidas se haya copiado el perfilado para todos los metodos
            Assertions.assertTrue(
                    planProfile.getApiMethodProfiles().stream()
                            .filter(apiMethodProfile -> commonApis.contains(apiMethodProfile.getApiMethod().getApiVersion()))
                            .allMatch(sourceAMP ->
                                    sourceAMP.getPlanProfile().equals(planProfile) &&
                                            oldDeploymentPlan.getPlanProfiles().get(0).getApiMethodProfiles().stream()
                                                    .anyMatch(targetAMP ->
                                                            sourceAMP.getApiMethod().equals(targetAMP.getApiMethod()) &&
                                                                    sourceAMP.getRoles().equals(targetAMP.getRoles())
                                                    )
                            )
            );

            //Comprobamos que para las APIs nuevas se hayan creado todos los metodos
            assertEquals(
                    onlyNewApis.stream()
                            .flatMap(novaApi -> novaApi.getApiMethods().stream())
                            .collect(Collectors.toSet()),
                    planProfile.getApiMethodProfiles().stream()
                            .map(ApiMethodProfile::getApiMethod)
                            .filter(apiMethod -> onlyNewApis.contains(apiMethod.getApiVersion()))
                            .collect(Collectors.toSet())
            );
            //Comprobamos que para las APIs nuevas se hayan creado el perfilado sin roles de todos los metodos
            Assertions.assertTrue(
                    planProfile.getApiMethodProfiles().stream()
                            .filter(apiMethodProfile -> onlyNewApis.contains(apiMethodProfile.getApiMethod().getApiVersion()))
                            .allMatch(apiMethodProfile ->
                                    apiMethodProfile.getPlanProfile().equals(planProfile) &&
                                            apiMethodProfile.getRoles().isEmpty()
                            )
            );

        }
    }

    @Nested
    class CheckPlanProfileChange {
        @Test
        public void withoutOldPlanOk() {
            //Inputs
            String releaseName = "Daniela";
            DeploymentPlan deploymentPlan = new DeploymentPlan()
                    .setEnvironment(Environment.LOCAL.getEnvironment())
                    .setReleaseVersion(
                            new ReleaseVersion()
                                    .setRelease(
                                            new Release()
                                                    .setName(releaseName)
                                    )
                    );
            PlanProfile planProfile = new PlanProfile();

            when(deploymentPlanRepository.getByReleaseNameAndEnvironmentAndPlanProfileStatus(any(), any(), any()))
                    .thenReturn(Collections.emptyList());
            when(planProfileRepository.findByDeploymentPlan(any())).thenReturn(planProfile);

            planProfilingUtils.checkPlanProfileChange(deploymentPlan);

            assertEquals(ProfileStatus.ACTIVE, planProfile.getStatus());
            Assertions.assertNotNull(planProfile.getActivationDate());

            verify(deploymentPlanRepository).getByReleaseNameAndEnvironmentAndPlanProfileStatus(releaseName, Environment.LOCAL.getEnvironment(), ProfileStatus.ACTIVE);
            verify(apiGatewayService).createProfiling(deploymentPlan);
            verify(planProfileRepository).findByDeploymentPlan(deploymentPlan);
        }

        @Test
        public void withOldPlanSameProfilingOk() {
            //Inputs
            String releaseName = "Daniela";
            Date oldActivationDate = new Date();
            SyncApiImplementation apiImplementation = buildApiImplementation("");
            PlanProfile oldPlanProfile = buildPlanProfile(apiImplementation.getApiVersion().getApiMethods().get(0))
                    .setActivationDate(oldActivationDate);
            PlanProfile newPlanProfile = buildPlanProfile(apiImplementation.getApiVersion().getApiMethods().get(0));

            DeploymentPlan oldDeploymentPlan = new DeploymentPlan()
                    .setPlanProfiles(Collections.singletonList(oldPlanProfile));
            DeploymentPlan newDeploymentPlan = new DeploymentPlan()
                    .setEnvironment(Environment.LOCAL.getEnvironment())
                    .setReleaseVersion(
                            new ReleaseVersion()
                                    .setRelease(
                                            new Release()
                                                    .setName(releaseName)
                                    )
                    )
                    .setPlanProfiles(Collections.singletonList(newPlanProfile));


            when(deploymentPlanRepository.getByReleaseNameAndEnvironmentAndPlanProfileStatus(any(), any(), any()))
                    .thenReturn(Collections.singletonList(oldDeploymentPlan));

            when(planProfileRepository.findByDeploymentPlan(oldDeploymentPlan)).thenReturn(oldPlanProfile);
            when(planProfileRepository.findByDeploymentPlan(newDeploymentPlan)).thenReturn(newPlanProfile);

            planProfilingUtils.checkPlanProfileChange(newDeploymentPlan);

            assertEquals(ProfileStatus.DEPRECATED, oldPlanProfile.getStatus());
            assertEquals(ProfileStatus.ACTIVE, newPlanProfile.getStatus());
            assertEquals(oldActivationDate, newPlanProfile.getActivationDate());

            verify(deploymentPlanRepository).getByReleaseNameAndEnvironmentAndPlanProfileStatus(releaseName, Environment.LOCAL.getEnvironment(), ProfileStatus.ACTIVE);
            verify(planProfileRepository).findByDeploymentPlan(oldDeploymentPlan);
            verify(planProfileRepository).findByDeploymentPlan(newDeploymentPlan);
        }


        @Test
        public void withOldPlanDifferentProfilingOk() {
            String releaseName = "Daniela";
            SyncApiImplementation oldApiImplementation = buildApiImplementation("Old");
            SyncApiImplementation newApiImplementation = buildApiImplementation("New");
            PlanProfile oldPlanProfile = buildPlanProfile(oldApiImplementation.getApiVersion().getApiMethods().get(0));
            PlanProfile newPlanProfile = buildPlanProfile(newApiImplementation.getApiVersion().getApiMethods().get(0));

            DeploymentPlan oldDeploymentPlan = new DeploymentPlan()
                    .setPlanProfiles(Collections.singletonList(oldPlanProfile));
            DeploymentPlan newDeploymentPlan = new DeploymentPlan()
                    .setEnvironment(Environment.LOCAL.getEnvironment())
                    .setReleaseVersion(
                            new ReleaseVersion()
                                    .setRelease(
                                            new Release()
                                                    .setName(releaseName)
                                    )
                    )
                    .setPlanProfiles(Collections.singletonList(newPlanProfile));

            when(deploymentPlanRepository.getByReleaseNameAndEnvironmentAndPlanProfileStatus(any(), any(), any()))
                    .thenReturn(Collections.singletonList(oldDeploymentPlan));
            when(planProfileRepository.findByDeploymentPlan(oldDeploymentPlan)).thenReturn(oldPlanProfile);
            when(planProfileRepository.findByDeploymentPlan(newDeploymentPlan)).thenReturn(newPlanProfile);

            planProfilingUtils.checkPlanProfileChange(newDeploymentPlan);

            assertEquals(ProfileStatus.DEPRECATED, oldPlanProfile.getStatus());
            assertEquals(ProfileStatus.ACTIVE, newPlanProfile.getStatus());
            Assertions.assertNotNull(newPlanProfile.getActivationDate());

            verify(deploymentPlanRepository).getByReleaseNameAndEnvironmentAndPlanProfileStatus(releaseName, Environment.LOCAL.getEnvironment(), ProfileStatus.ACTIVE);
            verify(apiGatewayService).removeProfiling(oldDeploymentPlan);
            verify(planProfileRepository).findByDeploymentPlan(oldDeploymentPlan);
            verify(apiGatewayService).createProfiling(newDeploymentPlan);
            verify(planProfileRepository).findByDeploymentPlan(newDeploymentPlan);
        }

    }

    @Nested
    class CheckProProfileNeedsApproval {
        @Test
        public void cesUuaa() {
            DeploymentPlan deploymentPlan = new DeploymentPlan().setReleaseVersion(
                    new ReleaseVersion()
                            .setRelease(
                                    new Release()
                                            .setProduct(
                                                    new Product()
                                                            .setUuaa(CES_UUAA)
                                            )
                            )
            );

            boolean ret = planProfilingUtils.checkProProfileNeedsApproval(deploymentPlan);

            Assertions.assertFalse(ret);
        }

        @Test
        public void withoutPlanProfiling() {
            DeploymentPlan deploymentPlan = new DeploymentPlan()
                    .setReleaseVersion(
                            new ReleaseVersion()
                                    .setRelease(
                                            new Release()
                                                    .setProduct(
                                                            new Product()
                                                                    .setUuaa("UUAA")
                                                    )
                                    )
                    );

            boolean ret = planProfilingUtils.checkProProfileNeedsApproval(deploymentPlan);

            Assertions.assertFalse(ret);
        }

        @Test
        public void emptyProfiling() {
            PlanProfile planProfile = new PlanProfile();
            planProfile.setId(1);

            ApiMethodProfile apiMethodProfile = new ApiMethodProfile();
            ApiMethod apiMethod = new ApiMethod();
            apiMethod.setEndpoint("/myEndpoint/");
            apiMethodProfile.setApiMethod(apiMethod);

            planProfile.setApiMethodProfiles(Collections.singletonList(apiMethodProfile));

            DeploymentPlan deploymentPlan = new DeploymentPlan()
                    .setReleaseVersion(
                            new ReleaseVersion()
                                    .setRelease(
                                            new Release()
                                                    .setProduct(
                                                            new Product()
                                                                    .setUuaa("UUAA")
                                                    )
                                    )
                    )
                    .setPlanProfiles(
                            Collections.singletonList(planProfile));

            boolean ret = planProfilingUtils.checkProProfileNeedsApproval(deploymentPlan);

            Assertions.assertFalse(ret);
        }

        @Test
        public void sameProfiling() {
            //Inputs
            String releaseName = "Daniela";
            Date oldActivationDate = new Date();
            SyncApiImplementation apiImplementation = buildApiImplementation("");
            PlanProfile oldPlanProfile = buildPlanProfile(apiImplementation.getApiVersion().getApiMethods().get(0))
                    .setActivationDate(oldActivationDate);
            PlanProfile newPlanProfile = buildPlanProfile(apiImplementation.getApiVersion().getApiMethods().get(0));

            DeploymentPlan oldDeploymentPlan = new DeploymentPlan()
                    .setPlanProfiles(Collections.singletonList(oldPlanProfile));
            DeploymentPlan newDeploymentPlan = new DeploymentPlan()
                    .setEnvironment(Environment.LOCAL.getEnvironment())
                    .setReleaseVersion(
                            new ReleaseVersion()
                                    .setRelease(
                                            new Release()
                                                    .setName(releaseName)
                                                    .setProduct(
                                                            new Product()
                                                                    .setUuaa("UUAA")
                                                    )
                                    )
                    )
                    .setPlanProfiles(Collections.singletonList(newPlanProfile));

            when(deploymentPlanRepository.getByReleaseNameAndEnvironmentAndPlanProfileStatus(any(), any(), any()))
                    .thenReturn(Collections.singletonList(oldDeploymentPlan));

            boolean ret = planProfilingUtils.checkProProfileNeedsApproval(newDeploymentPlan);

            Assertions.assertFalse(ret);
            verify(deploymentPlanRepository).getByReleaseNameAndEnvironmentAndPlanProfileStatus(releaseName, Environment.LOCAL.getEnvironment(), ProfileStatus.ACTIVE);
        }

        @Test
        public void withoutOldPlan() {
            String releaseName = "Daniela";

            PlanProfile planProfile = new PlanProfile();
            planProfile.setId(1);

            ApiMethodProfile apiMethodProfile = new ApiMethodProfile();
            ApiMethod apiMethod = new ApiMethod();
            apiMethod.setEndpoint("/myEndpoint/");
            apiMethodProfile.setApiMethod(apiMethod);
            apiMethodProfile.setRoles(Collections.singleton(new CesRole()));

            planProfile.setApiMethodProfiles(Collections.singletonList(apiMethodProfile));


            DeploymentPlan deploymentPlan = new DeploymentPlan()
                    .setEnvironment(Environment.LOCAL.getEnvironment())
                    .setReleaseVersion(
                            new ReleaseVersion()
                                    .setRelease(
                                            new Release()
                                                    .setName(releaseName)
                                                    .setProduct(
                                                            new Product()
                                                                    .setUuaa("UUAA")
                                                    )
                                    )
                    ).setPlanProfiles(
                            Collections.singletonList(planProfile));

            when(deploymentPlanRepository.getByReleaseNameAndEnvironmentAndPlanProfileStatus(any(), any(), any()))
                    .thenReturn(Collections.emptyList());

            boolean ret = planProfilingUtils.checkProProfileNeedsApproval(deploymentPlan);

            Assertions.assertTrue(ret);
            verify(deploymentPlanRepository).getByReleaseNameAndEnvironmentAndPlanProfileStatus(releaseName, Environment.LOCAL.getEnvironment(), ProfileStatus.ACTIVE);
        }

        @Test
        public void differentProfiling() {
            //Inputs
            String releaseName = "Daniela";
            Date oldActivationDate = new Date();
            SyncApiImplementation oldApiImplementation = buildApiImplementation("Old");
            SyncApiImplementation newApiImplementation = buildApiImplementation("New");

            PlanProfile oldPlanProfile = buildPlanProfile(oldApiImplementation.getApiVersion().getApiMethods().get(0))
                    .setActivationDate(oldActivationDate);
            PlanProfile newPlanProfile = buildPlanProfile(newApiImplementation.getApiVersion().getApiMethods().get(0));

            DeploymentPlan oldDeploymentPlan = new DeploymentPlan()
                    .setPlanProfiles(Collections.singletonList(oldPlanProfile));
            DeploymentPlan newDeploymentPlan = new DeploymentPlan()
                    .setEnvironment(Environment.LOCAL.getEnvironment())
                    .setReleaseVersion(
                            new ReleaseVersion()
                                    .setRelease(
                                            new Release()
                                                    .setName(releaseName)
                                                    .setProduct(
                                                            new Product()
                                                                    .setUuaa("UUAA")
                                                    )
                                    )
                    )
                    .setPlanProfiles(Collections.singletonList(newPlanProfile));

            when(deploymentPlanRepository.getByReleaseNameAndEnvironmentAndPlanProfileStatus(any(), any(), any()))
                    .thenReturn(Collections.singletonList(oldDeploymentPlan));

            boolean ret = planProfilingUtils.checkProProfileNeedsApproval(newDeploymentPlan);

            Assertions.assertTrue(ret);
            verify(deploymentPlanRepository).getByReleaseNameAndEnvironmentAndPlanProfileStatus(releaseName, Environment.LOCAL.getEnvironment(), ProfileStatus.ACTIVE);
        }
    }


    /***********************************************   UTILES   *******************************************************/

    private Set<ISecurizableApiVersion<?, ?, ?>> extractAPIList(DeploymentPlan deploymentPlan) {
        return deploymentPlan.getDeploymentSubsystems().stream()
                .flatMap(sourceDS -> sourceDS.getDeploymentServices().stream())
                .flatMap(sourceDS -> sourceDS.getService().getSecurizableServers().stream())
                .map(ISecurizableApiImplementation::getSecurizableApiVersion)
                .collect(Collectors.toSet());
    }

    private SyncApiImplementation buildApiImplementation(String suffix) {
        SyncApi api = new SyncApi();
        api.setBasePathSwagger("/basePath" + suffix);

        SyncApiVersion apiVersion = new SyncApiVersion();
        apiVersion.setApi(api);
        apiVersion.setBasePathXmas("/basePathXmas" + suffix);

        ApiMethod apiMethod = new ApiMethod()
                .setEndpoint("/endpoint" + suffix)
                .setVerb(Verb.POST)
                .setApiVersion(apiVersion);

        SyncApiImplementation apiImplementation = new SyncApiImplementation();
        apiImplementation.setImplementedAs(ImplementedAs.SERVED);
        apiImplementation.setApiVersion(apiVersion);

        apiVersion.getApiMethods().add(apiMethod);
        apiVersion.getApiImplementations().add(apiImplementation);

        return apiImplementation;
    }

    private DeploymentPlan buildDeploymentPlan(int releaseVersionId, List<ApiImplementation<?, ?, ?>> ApiImplementationList) {
        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setId(releaseVersionId);

        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
        releaseVersionService.setApiImplementations(ApiImplementationList);

        return new DeploymentPlan()
                .setDeploymentSubsystems(Collections.singletonList(
                                new DeploymentSubsystem()
                                        .setDeploymentServices(Collections.singletonList(
                                                        new DeploymentService()
                                                                .setService(releaseVersionService)
                                                )
                                        )
                        )
                ).setReleaseVersion(releaseVersion);
    }

    private PlanProfile buildPlanProfile(ApiMethod apiMethod) {
        return new PlanProfile()
                .addApiMethodProfile(
                        new ApiMethodProfile()
                                .setApiMethod(apiMethod)
                                .setRoles(new HashSet<>(
                                        Arrays.asList(
                                                new CesRole("JGMV", "LOCAL", "ROL_1", new HashSet<>()),
                                                new CesRole("JGMV", "LOCAL", "ROL_2", new HashSet<>())
                                        )
                                ))
                );
    }

    private SyncApi buildSyncApi(final String basePath) {
        SyncApi syncApi = new SyncApi();
        syncApi.setBasePathSwagger(basePath);
        return syncApi;
    }

    private SyncApiVersion buildSyncApiVersion(final String basePath) {
        SyncApiVersion syncApiVersion = new SyncApiVersion();
        syncApiVersion.setApi(this.buildSyncApi(basePath));
        return syncApiVersion;
    }

}
