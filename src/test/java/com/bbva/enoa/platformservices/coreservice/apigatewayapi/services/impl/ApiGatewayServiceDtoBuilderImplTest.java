//package com.bbva.enoa.platformservices.coreservice.apigatewayapi.services.impl;
//
//import com.bbva.enoa.apirestgen.apigatewaymanagerapi.model.AGMCreateProfilingDTO;
//import com.bbva.enoa.datamodel.model.api.entities.ApiMethod;
//import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
//import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
//import com.bbva.enoa.datamodel.model.api.enumerates.Verb;
//import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
//import com.bbva.enoa.datamodel.model.product.entities.Product;
//import com.bbva.enoa.datamodel.model.profile.entities.ApiMethodProfile;
//import com.bbva.enoa.datamodel.model.profile.entities.CesRole;
//import com.bbva.enoa.datamodel.model.profile.entities.PlanProfile;
//import com.bbva.enoa.datamodel.model.release.entities.Release;
//import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
//import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionServiceRepository;
//import com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils.DeploymentUtils;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//
//import java.util.Collections;
//
//public class ApiGatewayServiceDtoBuilderImplTest
//{
//
//    protected static final String SWAGGER = "classpath:api/swagger.yaml";
//    @Mock
//    private ReleaseVersionServiceRepository repo;
//
//    @InjectMocks
//    private ApiGatewayServiceDtoBuilderImpl builder;
//
//    @Mock
//    private DeploymentUtils deploymentUtils;
//
//
//    @BeforeEach
//    public void setUp() throws Exception
//    {
//        MockitoAnnotations.initMocks(this);
//    }
//
//    @Test
//    public void test_buildCreateProfilingDto_ok()
//    {
//        String uuaa = "JGMV";
//        String releaseName = "releaseName";
//        String basePath = "basePath";
//        String endpoint = "endpoint";
//        Verb verb = Verb.POST;
//        String rol = "ROL";
//        String endpointKey = String.format("%s:%s:%s%s", releaseName, verb.getVerb(), basePath, endpoint);
//
//        ApiMethod novaApiMethod = new ApiMethod()
//                .setVerb(verb)
//                .setEndpoint(endpoint)
//                .setApiVersion(
//                        new ApiVersion()
//                                .setSyncApi(
//                                        new SyncApi().setBasePathSwagger(basePath)
//                                )
//                );
//
//        DeploymentPlan deploymentPlan = new DeploymentPlan()
//                .setReleaseVersion(
//                        new ReleaseVersion()
//                                .setRelease(
//                                        new Release()
//                                                .setName(releaseName)
//                                                .setProduct(
//                                                        new Product()
//                                                                .setUuaa(uuaa)
//                                                )
//                                )
//                )
//                .setPlanProfiles(
//                        Collections.singletonList(
//                                new PlanProfile()
//                                        .setApiMethodProfiles(
//                                                Collections.singletonList(
//                                                        new ApiMethodProfile()
//                                                                .setApiMethod(novaApiMethod)
//                                                                .setRoles(
//                                                                        Collections.singleton(
//                                                                                new CesRole()
//                                                                                        .setRol(rol)
//                                                                        )
//                                                                )
//                                                )
//                                        )
//                        )
//                );
//
//        AGMCreateProfilingDTO ret = this.builder.buildCreateProfilingDto(deploymentPlan);
//
//        Assertions.assertEquals(uuaa, ret.getUuaa());
//        Assertions.assertEquals(1, ret.getProfilingInfo().length);
//        Assertions.assertEquals(endpointKey, ret.getProfilingInfo()[0].getValue());
//        Assertions.assertEquals(endpointKey, ret.getProfilingInfo()[0].getName());
//        Assertions.assertEquals(1, ret.getProfilingInfo()[0].getRoles().length);
//        Assertions.assertEquals(rol, ret.getProfilingInfo()[0].getRoles()[0]);
//
//        verifyAllMocks();
//    }
//
//    @Test
//    public void test_buildRemoveProfilingDto_ok()
//    {
//        String uuaa = "JGMV";
//        String releaseName = "releaseName";
//        String basePath = "basePath";
//        String endpoint = "endpoint";
//        Verb verb = Verb.POST;
//        CesRole cesRole = new CesRole();
//        String endpointKey = String.format("%s:%s:%s%s", releaseName, verb.getVerb(), basePath, endpoint);
//
//        ApiMethod novaApiMethod = new ApiMethod()
//                .setVerb(verb)
//                .setEndpoint(endpoint)
//                .setApiVersion(
//                        new ApiVersion()
//                                .setSyncApi(new SyncApi().setBasePathSwagger(basePath))
//                );
//
//        DeploymentPlan deploymentPlan = new DeploymentPlan()
//                .setReleaseVersion(
//                        new ReleaseVersion()
//                                .setRelease(
//                                        new Release()
//                                                .setName(releaseName)
//                                                .setProduct(
//                                                        new Product()
//                                                                .setUuaa(uuaa)
//                                                )
//                                )
//                )
//                .setPlanProfiles(
//                        Collections.singletonList(
//                                new PlanProfile()
//                                        .setApiMethodProfiles(
//                                                Collections.singletonList(
//                                                        new ApiMethodProfile()
//                                                                .setApiMethod(novaApiMethod).setRoles(Collections.singleton(cesRole))
//                                                )
//                                        )
//                        )
//                );
//
//        AGMCreateProfilingDTO ret = this.builder.buildCreateProfilingDto(deploymentPlan);
//
//        Assertions.assertEquals(uuaa, ret.getUuaa());
//        Assertions.assertEquals(1, ret.getProfilingInfo().length);
//        Assertions.assertEquals(endpointKey, ret.getProfilingInfo()[0].getValue());
//        Assertions.assertEquals(endpointKey, ret.getProfilingInfo()[0].getName());
//
//        verifyAllMocks();
//    }
//
//
//    private void verifyAllMocks()
//    {
//        Mockito.verifyNoMoreInteractions(this.deploymentUtils, this.repo);
//    }
//
////    @Test
////    public void buildApiPublicationDto()
////    {
////
////        ReleaseVersion releaseVersion = new ReleaseVersion();
////        releaseVersion.setVersionName(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        Release release = new Release();
////        release.setName(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        releaseVersion.setRelease(release);
////
////        List<ReleaseVersionSubsystem> subsystems = new ArrayList<ReleaseVersionSubsystem>();
////        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
////        List<ReleaseVersionService> services = new ArrayList<ReleaseVersionService>();
////        ReleaseVersionService service = buildReleaseVersionService().get(0);
////        service.setFinalName(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        service.setGroupId(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        service.setArtifactId(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        service.setVersion("1.0.1");
////        service.setServiceType(ServiceType.API_REST_JAVA_SPRING_BOOT);
////        services.add(service);
////        releaseVersionSubsystem.setServices(services);
////        subsystems.add(releaseVersionSubsystem);
////        releaseVersion.setSubsystems(subsystems);
////
////        ApiPublicationDto result = this.builder.buildApiPublicationDto(releaseVersion);
////
////        assertNotNull(result);
////        assertEquals(releaseVersion.getVersionName(), result.getRelease().getReleaseVersion());
////        assertTrue(result.getServices().length > 0);
////        assertNotNull(result.getServices()[0].getServiceName());
////
////        assertEquals(ServiceNamingUtils.getNovaServiceName(service.getGroupId(), service.getArtifactId(),
////                release.getName(), service.getVersion().split("\\.")[0]), result.getServices()[0].getServiceName());
////
////    }
////
////    @Test
////    public void buildApisDtoList() throws IOException
////    {
////
////        when(repo.findAllDeployedImplementations(anyInt())).thenReturn(buildReleaseVersionService());
////
////        List<NovaApiImplementation> novaApisServerList = new ArrayList<>();
////        NovaApiImplementation apiImplementation = new NovaApiImplementation();
////        novaApisServerList.add(apiImplementation);
////        NovaApi novaApi = new NovaApi();
////        novaApi.setApiName(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        novaApi.setVersion(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        novaApi.setBasePathNovaApi(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        novaApi.setBasePathSwagger(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        Product product = new Product();
////        product.setUuaa("AAAA");
////        novaApi.setProduct(product);
////
////        File file = ResourceUtils.getFile(SWAGGER);
////        FileInputStream fis = new FileInputStream(file);
////        byte[] swagger = new byte[(int) file.length()];
////        // Reads up to certain bytes of data from this input stream into an array of bytes.
////        fis.read(swagger);
////        LobFile lobFile = new LobFile();
////        lobFile.setId(1);
////        lobFile.setUrl("URL");
////        lobFile.setName("LobFileName");
////        lobFile.setContents(new String(swagger));
////        novaApi.setSwaggerFile(lobFile);
////
////        apiImplementation.setNovaApi(novaApi);
////        List<NovaApi> consumedApis = new ArrayList<>();
////        NovaApi novaApiConsumedBy = new NovaApi();
////        novaApiConsumedBy.setProduct(product);
////        novaApiConsumedBy.setBasePathNovaApi(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        novaApiConsumedBy.setBasePathSwagger(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        consumedApis.add(novaApiConsumedBy);
////        apiImplementation.setConsumedApis(consumedApis);
////
////        ApisDto[] result = this.builder.buildApisDtoList(novaApisServerList);
////
////        assertNotNull(result);
////        assertTrue(result.length > 0);
////        assertEquals(novaApi.getApiName(), result[0].getApiName());
////        assertEquals(novaApi.getVersion(), result[0].getApiVersion());
////        assertEquals(novaApi.getBasePathNovaApi(), result[0].getPath());
////
////        assertNotNull(result[0].getExternalEndpoint());
////        assertTrue(result[0].getExternalEndpoint().length > 0);
////
////        assertEquals(novaApiConsumedBy.getBasePathNovaApi(), result[0].getExternalEndpoint()[0].getPath());
////    }
////
////    @Test
////    public void buildDockerServiceDto()
////    {
////
////        ReleaseVersion releaseVersion = new ReleaseVersion();
////        releaseVersion.setVersionName(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        Release release = new Release();
////        release.setName(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        releaseVersion.setRelease(release);
////        releaseVersion.setVersionName("releaseVersion");
////
////        List<ReleaseVersionSubsystem> subsystems = new ArrayList<ReleaseVersionSubsystem>();
////        ReleaseVersionSubsystem releaseVersionSubsystem = new ReleaseVersionSubsystem();
////        List<ReleaseVersionService> services = new ArrayList<ReleaseVersionService>();
////        ReleaseVersionService service = buildReleaseVersionService().get(0);
////        service.setFinalName(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        service.setGroupId(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        service.setArtifactId(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        service.setVersion("1.0.1");
////        service.setServiceType(ServiceType.API_REST_JAVA_SPRING_BOOT);
////        services.add(service);
////        ReleaseVersionSubsystem versionSubsystem = new ReleaseVersionSubsystem();
////        versionSubsystem.setReleaseVersion(releaseVersion);
////        service.setVersionSubsystem(versionSubsystem);
////        releaseVersionSubsystem.setServices(services);
////        subsystems.add(releaseVersionSubsystem);
////        releaseVersion.setSubsystems(subsystems);
////        DeploymentService deploymentService = new DeploymentService();
////        deploymentService.setService(service);
////        DeploymentSubsystem deploymentSubsystem = new DeploymentSubsystem();
////        ReleaseVersionSubsystem subsystem = new ReleaseVersionSubsystem();
////        subsystem.setReleaseVersion(releaseVersion);
////        deploymentSubsystem.setSubsystem(subsystem);
////        deploymentService.setDeploymentSubsystem(deploymentSubsystem);
////
////        DockerServiceDto result = this.builder.buildDockerServiceDto(deploymentService);
////
////        assertNotNull(result);
////        assertEquals(ServiceNamingUtils.getNovaServiceName(service.getGroupId(), service.getArtifactId(),
////                release.getName(), service.getVersion().split("\\.")[0]), result.getServiceName());
////    }
////
////    @Test
////    public void buildServiceDto()
////    {
////        ReleaseVersionService releaseVersionService = buildReleaseVersionService().get(0);
////        releaseVersionService.setFinalName(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        releaseVersionService.setServiceType(ServiceType.API_REST_JAVA_SPRING_BOOT);
////        List<NovaApiImplementation> novaApisServerList = new ArrayList<>();
////        NovaApiImplementation apiImplementation = new NovaApiImplementation();
////        NovaApi novaApi = new NovaApi();
////        novaApi.setApiName(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        novaApi.setVersion(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        apiImplementation.setNovaApi(novaApi);
////        List<NovaApi> consumedApis = new ArrayList<>();
////        NovaApi novaApiConsumedBy = new NovaApi();
////        novaApiConsumedBy.setBasePathNovaApi(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        novaApiConsumedBy.setBasePathSwagger(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        consumedApis.add(novaApiConsumedBy);
////        apiImplementation.setConsumedApis(consumedApis);
////        releaseVersionService.setServers(novaApisServerList);
////        releaseVersionService.setGroupId(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        releaseVersionService.setArtifactId(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        releaseVersionService.setVersion("1.0.1");
////        releaseVersionService.setServiceType(ServiceType.API_REST_JAVA_SPRING_BOOT);
////
////        ServiceDto result = this.builder.buildServiceDto(releaseVersionService, "UnaRElease");
////
////        assertNotNull(result);
////        assertNotNull(result.getServiceName());
////        assertEquals(ServiceNamingUtils.getNovaServiceName(releaseVersionService.getGroupId(),
////                releaseVersionService.getArtifactId(), "UnaRElease",
////                releaseVersionService.getVersion().split("\\.")[0]), result.getServiceName());
////
////    }
////
////    @Test
////    public void buildServiceDto_null()
////    {
////
////        ReleaseVersionService releaseVersionService = null;
////        ServiceDto result = this.builder.buildServiceDto(releaseVersionService, null);
////
////        assertNull(result);
////
////    }
////
////    @Before
////    public void setUp()
////    {
////
////        MockitoAnnotations.initMocks(this);
////        ReflectionTestUtils.setField(this.builder, "defaultIp", "0.0.0.0");
////        ReflectionTestUtils.setField(this.deploymentUtils, "etherBaseDomain", "es-work-01.ether.iaas.igrupobbva");
////    }
////
////    @Test
////    public void testBuildReleaseDetailDto()
////    {
////
////        ReleaseVersion releaseVersion = new ReleaseVersion();
////        releaseVersion.setVersionName(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        Release release = new Release();
////        release.setName(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        Product product = new Product();
////        product.setUuaa(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        release.setProduct(product);
////        releaseVersion.setRelease(release);
////
////        ReleaseDetailDto actualResult = this.builder.buildReleaseDetailDto(releaseVersion);
////
////        assertNotNull(actualResult);
////
////        assertEquals(releaseVersion.getRelease().getName(), actualResult.getRelease());
////        assertEquals(releaseVersion.getVersionName(), actualResult.getReleaseVersion());
////        assertEquals(releaseVersion.getRelease().getProduct().getUuaa(), actualResult.getUuaa());
////
////    }
////
////    @Test
////    public void testBuildReleaseDetailDto_Null()
////    {
////
////        ReleaseDetailDto actualResult = this.builder.buildReleaseDetailDto(null);
////
////        assertNull(actualResult);
////
////    }
////
////    @Test
////    public void testBuildReleaseDetailDto_NullProduct()
////    {
////
////        ReleaseVersion releaseVersion = new ReleaseVersion();
////        releaseVersion.setVersionName(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        Release release = new Release();
////        release.setName(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////        releaseVersion.setRelease(release);
////
////        ReleaseDetailDto actualResult = this.builder.buildReleaseDetailDto(releaseVersion);
////
////        assertNotNull(actualResult);
////
////        assertEquals(releaseVersion.getRelease().getName(), actualResult.getRelease());
////        assertEquals(releaseVersion.getVersionName(), actualResult.getReleaseVersion());
////        assertNull(actualResult.getUuaa());
////    }
////
////    @Test
////    public void testBuildReleaseDetailDto_NullRelease()
////    {
////
////        ReleaseVersion releaseVersion = new ReleaseVersion();
////        releaseVersion.setVersionName(APIRestGeneratorRandomUtils.getXmlValidString(1).trim());
////
////        ReleaseDetailDto actualResult = this.builder.buildReleaseDetailDto(releaseVersion);
////
////        assertNotNull(actualResult);
////
////        assertNull(actualResult.getRelease());
////        assertEquals(releaseVersion.getVersionName(), actualResult.getReleaseVersion());
////        assertNull(actualResult.getUuaa());
////    }
////
////    private List<ReleaseVersionService> buildReleaseVersionService()
////    {
////        List<ReleaseVersionService> releaseVersionServiceList = new ArrayList<>();
////        ReleaseVersionService releaseVersionService = new ReleaseVersionService();
////        releaseVersionService.setArtifactId("artifact");
////        releaseVersionService.setGroupId("group");
////        releaseVersionService.setVersion("1.2.3");
////        ReleaseVersionSubsystem versionSubsystem = new ReleaseVersionSubsystem();
////        ReleaseVersion releaseVersion = new ReleaseVersion();
////        releaseVersion.setVersionName("releaseVersion");
////        Release release = new Release();
////        release.setName("release");
////        releaseVersion.setRelease(release);
////        versionSubsystem.setReleaseVersion(releaseVersion);
////        releaseVersionService.setVersionSubsystem(versionSubsystem);
////        releaseVersionServiceList.add(releaseVersionService);
////        return releaseVersionServiceList;
////    }
//
//}
