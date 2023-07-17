package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVMetadataDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ConfigurationmanagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDockerRegistryClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYamlContextParam;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYmlApi;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.PomXML;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.interfaces.IApiDefinitionValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IBatchScheduleService;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.ILibraryValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.PatternMatcher;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ProjectFileReader;
import com.bbva.enoa.utils.schedulerparserlib.Parser;
import com.bbva.enoa.utils.schedulerparserlib.exceptions.SchedulerParserException;
import com.bbva.enoa.utils.schedulerparserlib.parameters.BuilderCreator;
import com.bbva.enoa.utils.schedulerparserlib.parameters.Parameters;
import com.bbva.enoa.utils.schedulerparserlib.processor.ParsedInfo;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public class ProjectFileValidatorImplTest
{
    // JAVA API
    // Valid
    protected static final String CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_VALID = "classpath:novayml/nova_java_spring_boot_security_api.yml";
    protected static final String CLASSPATH_POM_JAVA_SPRING_BOOT_VALID = "classpath:poms/pom_java_spring_boot_security.xml";
    protected static final String CLASSPATH_APPLICATION_JAVA_SPRING_BOOT_VALID = "classpath:application/application_java_spring_boot_security.yml";
    protected static final String CLASSPATH_BOOTSTRAP_JAVA_SPRING_BOOT_VALID = "classpath:bootstrap/bootstrap_java_spring_boot_security.yml";
    protected static final String CLASSPATH_BOOTSTRAP_DEFAULT_INVALID = "classpath:bootstrap/bootstrap_default_invalid.yml";

    protected static final String CLASSPATH_NOVAYML_LIBRARY_JAVA_NOVA_YML = "classpath:novayml/nova_library_java.yml";

    // AsyncApis validations
    protected static final String CLASSPATH_POM_JAVA_SPRING_BOOT_API_VALID = "classpath:poms/pom_java_spring_boot_api.xml";
    protected static final String CLASSPATH_POM_JAVA_SPRING_BOOT_DAEMON_VALID = "classpath:poms/pom_java_spring_boot_api_daemon.xml";
    protected static final String CLASSPATH_POM_JAVA_SPRING_BOOT_BATCH_VALID = "classpath:poms/pom_java_spring_boot_api_batch.xml";
    protected static final String CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_ASYNC_VALID = "classpath:novayml/nova_java_spring_boot_security_api_async.yml";
    protected static final String CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_API_WITHOUT_API_INVALID = "classpath:novayml/nova_java_spring_boot_api_without_api.yml";
    protected static final String CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_API_REST_WITHOUT_API_INVALID = "classpath:novayml/nova_java_spring_boot_api_rest_without_api.yml";
    protected static final String CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_DAEMON_WITH_SYNC_API_INVALID = "classpath:novayml/nova_java_spring_boot_daemon_with_sync_api.yml";
    protected static final String CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_DAEMON_WITH_BACKTOFRONT_API_INVALID = "classpath:novayml/nova_java_spring_boot_daemon_with_backtofront_api.yml";
    protected static final String CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_BATCH_WITH_SYNC_API_INVALID = "classpath:novayml/nova_java_spring_boot_batch_with_sync_api.yml";
    protected static final String CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_BATCH_WITH_BACKTOFRONT_API_INVALID = "classpath:novayml/nova_java_spring_boot_batch_with_backtofront_api.yml";
    protected static final String CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_BATCH_WITH_BACKTOBACK_API_INVALID = "classpath:novayml/nova_java_spring_boot_batch_with_backtoback_api.yml";

    protected static final String CLASSPATH_NOVA_CDN_THIN2_WITH_BACKTOBACK_API_INVALID = "classpath:novayml/nova_cdn_thin2_backtoback.yml";

    // Invalid service type
    protected static final String CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_INVALID_SERVICE_TYPE =
            "classpath:novayml/nova_java_spring_boot_security_invalid_service_type" + ".yml";
    // Invalid
    protected static final String CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_INVALID = "classpath:novayml/nova_java_spring_boot_security_invalid" + ".yml";
    protected static final String CLASSPATH_POM_JAVA_SPRING_BOOT_INVALID = "classpath:poms/pom_java_spring_boot_security_invalid.xml";
    protected static final String CLASSPATH_APPLICATION_JAVA_SPRING_BOOT_INVALID = "classpath:application/application_java_spring_boot_security_invalid.yml";
    protected static final String CLASSPATH_BOOTSTRAP_JAVA_SPRING_BOOT_INVALID =
            "classpath:bootstrap/bootstrap_java_spring_boot_security_invalid" + ".yml";

    // BATCH SPRING
    // valid
    protected static final String CLASSPATH_NOVA_YML_BATCH_SPRING_VALID = "classpath:novayml/nova_batch_spring_security.yml";
    protected static final String CLASSPATH_POM_BATCH_SPRING_VALID = "classpath:poms/pom_batch_spring_security.xml";
    protected static final String CLASSPATH_APPLICATION_BATCH_SPRING_VALID = "classpath:application/application_batch_spring_security.yml";
    protected static final String CLASSPATH_BOOTSTRAP_BATCH_SPRING_VALID = "classpath:bootstrap/bootstrap_batch_spring_security.yml";

    // Invalid
    protected static final String CLASSPATH_NOVA_YML_BATCH_SPRING_INVALID = "classpath:novayml/nova_batch_spring_security_invalid.yml";

    // BATCH CLOUD TASK
    protected static final String CLASSPATH_NOVA_YML_BATCH_CLOUD_TASK_VALID = "classpath:novayml/nova_batch_cloud_task_security.yml";
    protected static final String CLASSPATH_POM_BATCH_CLOUD_TASK_VALID = "classpath:poms/pom_batch_cloud_task_security.xml";
    protected static final String CLASSPATH_APPLICATION_BATCH_CLOUD_TASK_VALID = "classpath:application/application_batch_cloud_task_security.yml";
    protected static final String CLASSPATH_BOOTSTRAP_BATCH_CLOUD_TASK_VALID = "classpath:bootstrap/bootstrap_batch_cloud_task_security.yml";

    // BATCH SCHEDULER
    protected static final String CLASSPATH_SCHEDULER_YML_BATCH_VALID = "classpath:scheduleryml/scheduler_batch_limit_valid.yml";
    protected static final String CLASSPATH_SCHEDULER_YML_BATCH_LIMIT = "classpath:scheduleryml/scheduler_batch_limit_max.yml";

    // NODE THIN2
    // Valid
    protected static final String CLASSPATH_NOVA_CDN_THIN2_VALID = "classpath:novayml/nova_cdn_thin2_security.yml";
    protected static final String CLASSPATH_PACKAGE_JSON_CDN_THIN2_VALID = "classpath:packagejson/package_cdn_thin2_security.json";
    protected static final String CLASSPATH_PACKAGE_JSON_CDN_KARMA_CORRECT = "classpath:packagejson/correctKarmaDependency.json";
    protected static final String CLASSPATH_PACKAGE_JSON_CDN_KARMA_INVALID = "classpath:packagejson/invalidKarmaDependency.json";

    // Invalid
    protected static final String CLASSPATH_NOVA_CDN_THIN2_INVALID = "classpath:novayml/nova_cdn_thin2_security_invalid.yml";
    protected static final String CLASSPATH_PACKAGE_JSON_CDN_THIN2_INVALID = "classpath:packagejson/package_cdn_thin2_security_invalid" + ".json";

    /**
     * types of service  allowed by nova.yml
     */
    private enum NovaServiceType
    {
        API, BATCH, CDN, DAEMON, LIBRARY;

        /**
         * Allowed Service Types for API Rest
         */
        public static final ServiceType[] SERVICE_TYPES_BY_API = new ServiceType[]{ServiceType.API_REST_JAVA_SPRING_BOOT, ServiceType.API_JAVA_SPRING_BOOT, ServiceType.API_REST_NODE_JS_EXPRESS, ServiceType.API_REST_PYTHON_FLASK};
        /**
         * Allowed Service Types for Batch
         */
        public static final ServiceType[] SERVICE_TYPES_BY_BATCH = new ServiceType[]{ServiceType.BATCH_JAVA_SPRING_BATCH, ServiceType.BATCH_JAVA_SPRING_CLOUD_TASK, ServiceType.BATCH_PYTHON, ServiceType.BATCH_SCHEDULER_NOVA};
        /**
         * Allowed Service Types for CDN
         */
        public static final ServiceType[] SERVICE_TYPES_BY_CDN = new ServiceType[]{ServiceType.CDN_ANGULAR_THIN2, ServiceType.CDN_ANGULAR_THIN3, ServiceType.CDN_POLYMER_CELLS};
        /**
         * Allowed Service Types for DAEMON
         */
        public static final ServiceType[] SERVICE_TYPES_BY_DAEMON = new ServiceType[]{ServiceType.DAEMON_JAVA_SPRING_BOOT};
        /**
         * Allowed Service Types for Library
         */
        public static final ServiceType[] SERVICE_TYPES_BY_LIBRARY = new ServiceType[]{ServiceType.LIBRARY_JAVA, ServiceType.LIBRARY_NODE, ServiceType.LIBRARY_PYTHON, ServiceType.LIBRARY_THIN2, ServiceType.LIBRARY_TEMPLATE};

        public static ServiceType getRandomServiceTypeBy(NovaServiceType nst)
        {
            ServiceType[] randoms;
            switch (nst)
            {
                case API:
                    randoms = SERVICE_TYPES_BY_API;
                    break;
                case BATCH:
                    randoms = SERVICE_TYPES_BY_BATCH;
                    break;
                case CDN:
                    randoms = SERVICE_TYPES_BY_CDN;
                    break;
                case DAEMON:
                    randoms = SERVICE_TYPES_BY_DAEMON;
                    break;
                case LIBRARY:
                    randoms = SERVICE_TYPES_BY_LIBRARY;
                    break;
                default:
                    randoms = new ServiceType[]{ServiceType.NOVA};
            }

            return randoms[RandomUtils.nextInt(0, randoms.length)];
        }

        public String getType()
        {
            return this.name().replace("_", " ");
        }
    }

    @Mock
    private IDockerRegistryClient iDockerRegistryClient;

    @Mock
    private IApiDefinitionValidator apiResolverDefinitionValidator;

    @Mock
    private ConfigurationmanagerClient configurationmanagerClient;

    @Mock
    private ILibraryValidator libraryValidator;

    @Mock
    private IBatchScheduleService batchScheduleService;

    @InjectMocks
    private ProjectFileValidatorImpl projectFileValidator;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        ReflectionTestUtils.setField(this.projectFileValidator, "currentNovaVersion", "3");
        ReflectionTestUtils.setField(this.projectFileValidator, "novaPackaging", "jar");
        ReflectionTestUtils.setField(this.projectFileValidator, "uuaaLength", 4);
        ReflectionTestUtils.setField(this.projectFileValidator, "novaParentVersion", "1.1.0");
        ReflectionTestUtils.setField(this.projectFileValidator, "novaYmlVersion", "18.04");
        ReflectionTestUtils.setField(this.projectFileValidator, "javaVersion", "1.8");
        ReflectionTestUtils.setField(this.projectFileValidator, "angularVersion", "1.0");
        ReflectionTestUtils.setField(this.projectFileValidator, "pluginOutputDirectory", "./dist");
        ReflectionTestUtils.setField(this.projectFileValidator, "minimumMultiJdkJavaVersion", "11");
    }

    @Test
    public void validateProjectFilesDependencyNovaYml()
    {

        NovaYml novaYml = new NovaYml();
        novaYml.setApplicationName("testDependency");
        novaYml.setLanguage("JAVA_SPRING_BOOT");
        novaYml.setLanguageVersion("1.8.121");
        novaYml.setName("testDependency");
        novaYml.setNovaVersion("18.04");
        novaYml.setProjectName("testDependency");
        novaYml.setServiceType(ServiceType.DEPENDENCY.getServiceType());
        novaYml.setUuaa("ENVM");
        novaYml.setVersion("1.0.0");
        novaYml.setValid(false);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setServiceType(ServiceType.DEPENDENCY.getServiceType());
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);
        validatorInputs.setNovaYml(novaYml);
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());
        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        Assertions.assertEquals(0, serviceDto.getValidationErrors().length);

    }

    @Test
    void validateProjectSchedule_OK() throws SchedulerParserException, IOException
    {

        NovaYml novaYml = new NovaYml();
        novaYml.setApplicationName("testDependency");
        novaYml.setLanguage("JAVA_SPRING_BOOT");
        novaYml.setLanguageVersion("1.8.121");
        novaYml.setName("testDependency");
        novaYml.setNovaVersion("18.04");
        novaYml.setProjectName("testDependency");
        novaYml.setServiceType(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());
        novaYml.setUuaa("ENVM");
        novaYml.setVersion("1.0.0");
        novaYml.setValid(false);

        List<NovaYamlContextParam> contextParams = new ArrayList<>();
        NovaYamlContextParam novaYamlContextParam = new NovaYamlContextParam();
        novaYamlContextParam.setName("trigger");
        novaYamlContextParam.setDefaultValue("trigger");
        novaYamlContextParam.setDescription("Prueba");
        novaYamlContextParam.setType("STRING");
        contextParams.add(novaYamlContextParam);
        novaYml.setContextParams(contextParams);

        when(batchScheduleService.getSchedulerYmlStringFile(any(ReleaseVersionService.class))).thenReturn(getSchedulerYmlFileString(CLASSPATH_SCHEDULER_YML_BATCH_VALID));

        Parameters parameters = BuilderCreator.newParametersBuilder().setFileContentWithExtension(getSchedulerYmlFileString(CLASSPATH_SCHEDULER_YML_BATCH_VALID), "yml").build();
        ParsedInfo parsedInfo = new Parser(null, parameters).parse();

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setServiceType(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);
        validatorInputs.setNovaYml(novaYml);
        validatorInputs.setParsedInfo(parsedInfo);
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());
        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        Assertions.assertEquals(0, serviceDto.getValidationErrors().length);

    }

    @Test
    void validateProjectSchedule_limit() throws SchedulerParserException, IOException
    {

        NovaYml novaYml = new NovaYml();
        novaYml.setApplicationName("testDependency");
        novaYml.setLanguage("JAVA_SPRING_BOOT");
        novaYml.setLanguageVersion("1.8.121");
        novaYml.setName("testDependency");
        novaYml.setNovaVersion("18.04");
        novaYml.setProjectName("testDependency");
        novaYml.setServiceType(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());
        novaYml.setUuaa("ENVM");
        novaYml.setVersion("1.0.0");
        novaYml.setValid(false);

        List<NovaYamlContextParam> contextParams = new ArrayList<>();
        NovaYamlContextParam novaYamlContextParam = new NovaYamlContextParam();
        novaYamlContextParam.setName("trigger");
        novaYamlContextParam.setDefaultValue("trigger");
        novaYamlContextParam.setDescription("Prueba");
        novaYamlContextParam.setType("STRING");
        contextParams.add(novaYamlContextParam);
        novaYml.setContextParams(contextParams);

        when(batchScheduleService.getSchedulerYmlStringFile(any(ReleaseVersionService.class))).thenReturn(getSchedulerYmlFileString(CLASSPATH_SCHEDULER_YML_BATCH_LIMIT));

        Parameters parameters = BuilderCreator.newParametersBuilder().setFileContentWithExtension(getSchedulerYmlFileString(CLASSPATH_SCHEDULER_YML_BATCH_LIMIT), "yml").build();
        ParsedInfo parsedInfo = new Parser(null, parameters).parse();

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setServiceType(ServiceType.BATCH_SCHEDULER_NOVA.getServiceType());
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);
        validatorInputs.setNovaYml(novaYml);
        validatorInputs.setParsedInfo(parsedInfo);
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());
        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        Assertions.assertEquals(1, serviceDto.getValidationErrors().length);

    }

    private String getSchedulerYmlFileString(String inputPathFile) throws IOException
    {
        File file = ResourceUtils.getFile(inputPathFile);
        FileInputStream fis = new FileInputStream(file);
        byte[] scheduler = fis.readAllBytes();
        fis.close();

        return new String(scheduler, StandardCharsets.UTF_8);
    }

    @Test
    public void validateProjectFilesDependencyNovaYmlInvalidUUAA()
    {

        NovaYml novaYml = new NovaYml();
        novaYml.setApplicationName("testDependency");
        novaYml.setLanguage("JAVA_SPRING_BOOT");
        novaYml.setLanguageVersion("1.8.121");
        novaYml.setName("testDependency");
        novaYml.setNovaVersion("18.04");
        novaYml.setProjectName("testDependency");
        novaYml.setServiceType(ServiceType.DEPENDENCY.getServiceType());
        novaYml.setUuaa("ENVM");
        novaYml.setVersion("1.0.0");
        novaYml.setValid(false);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setServiceType(ServiceType.DEPENDENCY.getServiceType());
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);
        validatorInputs.setNovaYml(novaYml);
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());
        this.projectFileValidator.validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest",
                buildProduct().setUuaa("xxxx"), "subsystemTest");

        Assertions.assertEquals(1, serviceDto.getValidationErrors().length);
        Assertions.assertEquals(Constants.INVALID_UUAA_NAME, serviceDto.getValidationErrors()[0].getCode());

    }

    @Test
    public void validateProjectFilesDependencyNovaYmlConsumesApi()
    {

        NovaYml novaYml = new NovaYml();
        novaYml.setApplicationName("testDependency");
        novaYml.setLanguage("JAVA_SPRING_BOOT");
        novaYml.setLanguageVersion("1.8.121");
        novaYml.setName("testDependency");
        novaYml.setNovaVersion("18.04");
        novaYml.setProjectName("testDependency");
        novaYml.setServiceType(ServiceType.DEPENDENCY.getServiceType());
        novaYml.setUuaa("ENVM");
        novaYml.setVersion("1.0.0");
        novaYml.setValid(false);
        novaYml.setApiConsumed(Collections.singletonList(new NovaYmlApi()));

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setServiceType(ServiceType.DEPENDENCY.getServiceType());
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);
        validatorInputs.setNovaYml(novaYml);
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());
        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        Assertions.assertEquals(1, serviceDto.getValidationErrors().length);
        Assertions.assertEquals(Constants.DEPENDENCY_CONSUMING_API, serviceDto.getValidationErrors()[0].getCode());

    }

    @Test
    public void validateProjectFilesDependencyNovaYmlServesApi()
    {

        NovaYml novaYml = new NovaYml();
        novaYml.setApplicationName("testDependency");
        novaYml.setLanguage("JAVA_SPRING_BOOT");
        novaYml.setLanguageVersion("1.8.121");
        novaYml.setName("testDependency");
        novaYml.setNovaVersion("18.04");
        novaYml.setProjectName("testDependency");
        novaYml.setServiceType(ServiceType.DEPENDENCY.getServiceType());
        novaYml.setUuaa("ENVM");
        novaYml.setVersion("1.0.0");
        novaYml.setValid(false);
        novaYml.setApiServed(Collections.singletonList(new NovaYmlApi()));

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setServiceType(ServiceType.DEPENDENCY.getServiceType());
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);
        validatorInputs.setNovaYml(novaYml);
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());
        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        Assertions.assertEquals(1, serviceDto.getValidationErrors().length);
        Assertions.assertEquals(Constants.DAEMON_DEFINING_API, serviceDto.getValidationErrors()[0].getCode());

    }

    @Test
    public void validateProjectFilesDependencyOldVersion()
    {

        NovaYml novaYml = new NovaYml();
        novaYml.setApplicationName("testDependency");
        novaYml.setLanguage("JAVA_SPRING_BOOT");
        novaYml.setLanguageVersion("1.8.121");
        novaYml.setName("testDependency");
        novaYml.setNovaVersion("18.04");
        novaYml.setProjectName("testDependency");
        novaYml.setServiceType(ServiceType.DEPENDENCY.getServiceType());
        novaYml.setUuaa("ENVM");
        novaYml.setVersion("1.0.0");
        novaYml.setValid(false);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setServiceType(ServiceType.DEPENDENCY.getServiceType());
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(false);
        validatorInputs.setNovaYml(novaYml);
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());
        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        Assertions.assertEquals(0, serviceDto.getValidationErrors().length);

    }


    @Test
    public void validateServiceFilesInvalidBatchSpring() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_POM_BATCH_SPRING_VALID);
        byte[] pom = new FileInputStream(file).readAllBytes();

        file = ResourceUtils.getFile(CLASSPATH_APPLICATION_BATCH_SPRING_VALID);
        byte[] application = new FileInputStream(file).readAllBytes();

        file = ResourceUtils.getFile(CLASSPATH_BOOTSTRAP_BATCH_SPRING_VALID);
        byte[] bootstrap = new FileInputStream(file).readAllBytes();

        file = ResourceUtils.getFile(CLASSPATH_NOVA_YML_BATCH_SPRING_INVALID);
        byte[] novaFile = new FileInputStream(file).readAllBytes();

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);

        ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);
        ProjectFileReader.scanApplication("somePath", application, validatorInputs);
        ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

        List<String> applicationFiles = new ArrayList<>();
        applicationFiles.add("application.yml");
        validatorInputs.getApplication().setApplicationFiles(applicationFiles);
        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getPom().getGroupId());
        serviceDto.setDescription(validatorInputs.getPom().getDescription());
        serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
        serviceDto.setFinalName(validatorInputs.getPom().getFinalName());
        serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        serviceDto.setFolder("");
        serviceDto.setProjectDefinitionUrl("");
        serviceDto.setHasForcedCompilation(true);

        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        Assertions.assertEquals(16, serviceDto.getValidationErrors().length);

        PomXML.clearArtifactIdList();
    }

    @Test
    public void validateServiceFilesWithouApiValidJavaSpringBootAsyncApi() throws Exception
    {
        ValidationErrorDto[] validationErrors = validateServiceFilesYmlApiValidations(
                CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_ASYNC_VALID,
                CLASSPATH_POM_JAVA_SPRING_BOOT_API_VALID);

        Assertions.assertEquals(0, validationErrors.length);
    }

    @Test
    public void validateServiceFilesWithouApiInvalidJavaSpringBootApi() throws Exception
    {
        ValidationErrorDto[] validationErrors = validateServiceFilesYmlApiValidations(
                CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_API_WITHOUT_API_INVALID,
                CLASSPATH_POM_JAVA_SPRING_BOOT_API_VALID);

        Arrays.stream(validationErrors).forEach(System.out::println);

        Assertions.assertEquals(1, validationErrors.length);
        Assertions.assertEquals(Constants.NO_API_DEFINED, validationErrors[0].getCode());

    }

    @Test
    public void validateServiceFilesWithouApiInvalidJavaSpringBootApiRest() throws Exception
    {
        ValidationErrorDto[] validationErrors = validateServiceFilesYmlApiValidations(
                CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_API_REST_WITHOUT_API_INVALID,
                CLASSPATH_POM_JAVA_SPRING_BOOT_API_VALID);

        Assertions.assertEquals(1, validationErrors.length);
        Assertions.assertEquals(Constants.NO_API_DEFINED, validationErrors[0].getCode());
    }

    @Test
    public void validateServiceFilesDaemonWithSyncApi() throws Exception
    {
        ValidationErrorDto[] validationErrors = validateServiceFilesYmlApiValidations(
                CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_DAEMON_WITH_SYNC_API_INVALID,
                CLASSPATH_POM_JAVA_SPRING_BOOT_DAEMON_VALID);

        Assertions.assertEquals(1, validationErrors.length);
        Assertions.assertEquals(Constants.DAEMON_DEFINING_API, validationErrors[0].getCode());
    }

    @Test
    public void validateServiceFilesDaemonWithBackToFront() throws Exception
    {
        ValidationErrorDto[] validationErrors = validateServiceFilesYmlApiValidations(
                CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_DAEMON_WITH_BACKTOFRONT_API_INVALID,
                CLASSPATH_POM_JAVA_SPRING_BOOT_DAEMON_VALID);

        Assertions.assertEquals(1, validationErrors.length, "There was more errors: " + Arrays.stream(validationErrors).map(s -> s.getMessage()).collect(Collectors.toList()));
        Assertions.assertEquals(Constants.DAEMON_DEFINING_API, validationErrors[0].getCode());
    }

    @Test
    public void validateServiceFilesBatchWithSyncApi() throws Exception
    {
        ValidationErrorDto[] validationErrors = validateServiceFilesYmlApiValidations(
                CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_BATCH_WITH_SYNC_API_INVALID,
                CLASSPATH_POM_JAVA_SPRING_BOOT_BATCH_VALID);

        Assertions.assertEquals(1, validationErrors.length);
        Assertions.assertEquals(Constants.DAEMON_DEFINING_API, validationErrors[0].getCode());
    }

    @Test
    public void validateServiceFilesBatchWithBackToFront() throws Exception
    {
        ValidationErrorDto[] validationErrors = validateServiceFilesYmlApiValidations(
                CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_BATCH_WITH_BACKTOFRONT_API_INVALID,
                CLASSPATH_POM_JAVA_SPRING_BOOT_BATCH_VALID);

        Assertions.assertEquals(1, validationErrors.length);
        Assertions.assertEquals(Constants.DAEMON_DEFINING_API, validationErrors[0].getCode());
    }

    @Test
    public void validateServiceFilesBatchWithBackToBack() throws Exception
    {
        ValidationErrorDto[] validationErrors = validateServiceFilesYmlApiValidations(
                CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_BATCH_WITH_BACKTOBACK_API_INVALID,
                CLASSPATH_POM_JAVA_SPRING_BOOT_BATCH_VALID);

        Assertions.assertEquals(1, validationErrors.length);
        Assertions.assertEquals(Constants.BATCH_CDN_DEFINING_BACKTOBACK_API, validationErrors[0].getCode());
    }

    @Test
    public void validateServiceFilesWithBootstrapError() throws Exception
    {
        ValidationErrorDto[] validationErrors = validateServiceFilesYmlApiValidationsErrors(
                CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_ASYNC_VALID,
                CLASSPATH_POM_JAVA_SPRING_BOOT_API_VALID);

        Assertions.assertEquals(1, validationErrors.length);
        Assertions.assertEquals(Constants.INVALID_BOOTSTRAP_FILE, validationErrors[0].getCode());
    }

    private ValidationErrorDto[] validateServiceFilesYmlApiValidations(String novayml, String pomxml) throws Exception
    {

        File file = ResourceUtils.getFile(pomxml);
        FileInputStream fis = new FileInputStream(file);
        byte[] pom = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_APPLICATION_JAVA_SPRING_BOOT_VALID);
        fis = new FileInputStream(file);
        byte[] application = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_BOOTSTRAP_JAVA_SPRING_BOOT_VALID);
        fis = new FileInputStream(file);
        byte[] bootstrap = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(novayml);
        fis = new FileInputStream(file);
        byte[] novaFile = fis.readAllBytes();
        fis.close();

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);

        ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);
        ProjectFileReader.scanApplication("somePath", application, validatorInputs);
        ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

        List<String> applicationFiles = new ArrayList<>();
        applicationFiles.add("application.yml");
        validatorInputs.getApplication().setApplicationFiles(applicationFiles);
        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getPom().getGroupId());
        serviceDto.setDescription(validatorInputs.getPom().getDescription());
        serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
        serviceDto.setFinalName(validatorInputs.getPom().getFinalName());
        serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        serviceDto.setFolder("");
        serviceDto.setProjectDefinitionUrl("");
        serviceDto.setHasForcedCompilation(true);

        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");
        PomXML.clearArtifactIdList();
        return serviceDto.getValidationErrors();
    }

    private ValidationErrorDto[] validateServiceFilesYmlApiValidationsErrors(String novayml, String pomxml) throws Exception
    {

        File file = ResourceUtils.getFile(pomxml);
        FileInputStream fis = new FileInputStream(file);
        byte[] pom = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_APPLICATION_JAVA_SPRING_BOOT_VALID);
        fis = new FileInputStream(file);
        byte[] application = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_BOOTSTRAP_DEFAULT_INVALID);
        fis = new FileInputStream(file);
        byte[] bootstrap = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(novayml);
        fis = new FileInputStream(file);
        byte[] novaFile = fis.readAllBytes();
        fis.close();

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);

        ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);
        ProjectFileReader.scanApplication("somePath", application, validatorInputs);
        ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

        List<String> applicationFiles = new ArrayList<>();
        applicationFiles.add("application.yml");
        validatorInputs.getApplication().setApplicationFiles(applicationFiles);
        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getPom().getGroupId());
        serviceDto.setDescription(validatorInputs.getPom().getDescription());
        serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
        serviceDto.setFinalName(validatorInputs.getPom().getFinalName());
        serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        serviceDto.setFolder("");
        serviceDto.setProjectDefinitionUrl("");
        serviceDto.setHasForcedCompilation(true);

        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");
        PomXML.clearArtifactIdList();
        return serviceDto.getValidationErrors();
    }

    @Test
    public void validateServiceFilesCdnWithBackToBack() throws Exception
    {
        byte[] pom = new byte[0];
        byte[] application = new byte[0];
        byte[] bootstrap = new byte[0];

        File file = ResourceUtils.getFile(CLASSPATH_NOVA_CDN_THIN2_WITH_BACKTOBACK_API_INVALID);
        FileInputStream fis = new FileInputStream(file);
        byte[] novaFile = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_PACKAGE_JSON_CDN_THIN2_VALID);
        fis = new FileInputStream(file);
        byte[] packageJson = fis.readAllBytes();
        fis.close();

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);

        ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);
        ProjectFileReader.scanApplication("somePath", application, validatorInputs);
        ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);
        ProjectFileReader.scanPackageJson(file.getPath(), packageJson, validatorInputs);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

        List<String> applicationFiles = new ArrayList<>();
        applicationFiles.add("application.yml");
        validatorInputs.getApplication().setApplicationFiles(applicationFiles);
        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getNovaYml().getName());
        serviceDto.setDescription("");
        serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
        serviceDto.setFinalName("");
        serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        serviceDto.setFolder("");
        serviceDto.setProjectDefinitionUrl("");
        serviceDto.setHasForcedCompilation(true);

        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        Assertions.assertEquals(1, serviceDto.getValidationErrors().length);
        Assertions.assertEquals(Constants.BATCH_CDN_DEFINING_BACKTOBACK_API, serviceDto.getValidationErrors()[0].getCode());

        PomXML.clearArtifactIdList();
    }

    @Test
    public void validateServiceFilesInvalidJavaSpringBoot() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_POM_JAVA_SPRING_BOOT_INVALID);
        FileInputStream fis = new FileInputStream(file);
        byte[] pom = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_APPLICATION_JAVA_SPRING_BOOT_INVALID);
        fis = new FileInputStream(file);
        byte[] application = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_BOOTSTRAP_JAVA_SPRING_BOOT_INVALID);
        fis = new FileInputStream(file);
        byte[] bootstrap = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_INVALID);
        fis = new FileInputStream(file);
        byte[] novaFile = fis.readAllBytes();
        fis.close();

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);

        ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);
        ProjectFileReader.scanApplication("somePath", application, validatorInputs);
        ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

        List<String> applicationFiles = new ArrayList<>();
        applicationFiles.add("application.yml");
        validatorInputs.getApplication().setApplicationFiles(applicationFiles);
        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getPom().getGroupId());
        serviceDto.setDescription(validatorInputs.getPom().getDescription());
        serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
        serviceDto.setFinalName(validatorInputs.getPom().getFinalName());
        serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        serviceDto.setFolder("");
        serviceDto.setProjectDefinitionUrl("");
        serviceDto.setHasForcedCompilation(true);

        PomXML.addArtifactId(validatorInputs.getPom().getGroupId(), validatorInputs.getPom().getArtifactId());

        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        Assertions.assertEquals(25, serviceDto.getValidationErrors().length);

        PomXML.clearArtifactIdList();
    }

    @Test
    public void validateServiceFilesInvalidNodeThin2() throws Exception
    {
        byte[] pom = new byte[0];
        byte[] application = new byte[0];
        byte[] bootstrap = new byte[0];

        File file = ResourceUtils.getFile(CLASSPATH_NOVA_CDN_THIN2_INVALID);
        FileInputStream fis = new FileInputStream(file);
        byte[] novaFile = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_PACKAGE_JSON_CDN_THIN2_INVALID);
        fis = new FileInputStream(file);
        byte[] packageJson = fis.readAllBytes();
        fis.close();

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);

        ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);
        ProjectFileReader.scanApplication("somePath", application, validatorInputs);
        ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);
        ProjectFileReader.scanPackageJson(file.getPath(), packageJson, validatorInputs);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

        List<String> applicationFiles = new ArrayList<>();
        applicationFiles.add("application.yml");
        validatorInputs.getApplication().setApplicationFiles(applicationFiles);
        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getNovaYml().getName());
        serviceDto.setDescription("");
        serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
        serviceDto.setFinalName("");
        serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        serviceDto.setFolder("");
        serviceDto.setProjectDefinitionUrl("");
        serviceDto.setHasForcedCompilation(true);

        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        Assertions.assertEquals(16, serviceDto.getValidationErrors().length);

        PomXML.clearArtifactIdList();
    }

    @Test
    public void validateServiceFilesInvalidServiceType() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_POM_JAVA_SPRING_BOOT_INVALID);
        FileInputStream fis = new FileInputStream(file);
        byte[] pom = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_APPLICATION_JAVA_SPRING_BOOT_INVALID);
        fis = new FileInputStream(file);
        byte[] application = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_BOOTSTRAP_JAVA_SPRING_BOOT_INVALID);
        fis = new FileInputStream(file);
        byte[] bootstrap = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_INVALID_SERVICE_TYPE);
        fis = new FileInputStream(file);
        byte[] novaFile = fis.readAllBytes();
        fis.close();

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);

        ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);
        ProjectFileReader.scanApplication("somePath", application, validatorInputs);
        ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

        List<String> applicationFiles = new ArrayList<>();
        applicationFiles.add("application.yml");
        validatorInputs.getApplication().setApplicationFiles(applicationFiles);
        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getPom().getGroupId());
        serviceDto.setDescription(validatorInputs.getPom().getDescription());
        serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
        serviceDto.setFinalName(validatorInputs.getPom().getFinalName());
        serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        serviceDto.setFolder("");
        serviceDto.setProjectDefinitionUrl("");
        serviceDto.setHasForcedCompilation(true);

        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        PomXML.clearArtifactIdList();

        Assertions.assertEquals(1, serviceDto.getValidationErrors().length);

    }

    @Test
    public void validateServiceFilesValidBatchCloudTask() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_POM_BATCH_CLOUD_TASK_VALID);
        FileInputStream fis = new FileInputStream(file);
        byte[] pom = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_APPLICATION_BATCH_CLOUD_TASK_VALID);
        fis = new FileInputStream(file);
        byte[] application = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_BOOTSTRAP_BATCH_CLOUD_TASK_VALID);
        fis = new FileInputStream(file);
        byte[] bootstrap = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_NOVA_YML_BATCH_CLOUD_TASK_VALID);
        fis = new FileInputStream(file);
        byte[] novaFile = fis.readAllBytes();
        fis.close();

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);

        ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);
        ProjectFileReader.scanApplication("somePath", application, validatorInputs);
        ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

        List<String> applicationFiles = new ArrayList<>();
        applicationFiles.add("application.yml");
        validatorInputs.getApplication().setApplicationFiles(applicationFiles);
        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getPom().getGroupId());
        serviceDto.setDescription(validatorInputs.getPom().getDescription());
        serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
        serviceDto.setFinalName(validatorInputs.getPom().getFinalName());
        serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        serviceDto.setFolder("");
        serviceDto.setProjectDefinitionUrl("");
        serviceDto.setHasForcedCompilation(true);

        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        Assertions.assertEquals(1, serviceDto.getValidationErrors().length);

        PomXML.clearArtifactIdList();
    }

    @Test
    public void validateServiceFilesValidBatchSpring() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_POM_BATCH_SPRING_VALID);
        FileInputStream fis = new FileInputStream(file);
        byte[] pom = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_APPLICATION_BATCH_SPRING_VALID);
        fis = new FileInputStream(file);
        byte[] application = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_BOOTSTRAP_BATCH_SPRING_VALID);
        fis = new FileInputStream(file);
        byte[] bootstrap = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_NOVA_YML_BATCH_SPRING_VALID);
        fis = new FileInputStream(file);
        byte[] novaFile = fis.readAllBytes();
        fis.close();

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);

        ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);
        ProjectFileReader.scanApplication("somePath", application, validatorInputs);
        ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

        List<String> applicationFiles = new ArrayList<>();
        applicationFiles.add("application.yml");
        validatorInputs.getApplication().setApplicationFiles(applicationFiles);
        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getPom().getGroupId());
        serviceDto.setDescription(validatorInputs.getPom().getDescription());
        serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
        serviceDto.setFinalName(validatorInputs.getPom().getFinalName());
        serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        serviceDto.setFolder("");
        serviceDto.setProjectDefinitionUrl("");
        serviceDto.setHasForcedCompilation(true);

        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        Assertions.assertEquals(1, serviceDto.getValidationErrors().length, "There was more errors: " + Arrays.stream(serviceDto.getValidationErrors()).map(s -> s.getMessage()).collect(Collectors.toList()));

        PomXML.clearArtifactIdList();
    }

    @Test
    public void validateServiceFilesValidJavaSpringBoot() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_POM_JAVA_SPRING_BOOT_VALID);
        FileInputStream fis = new FileInputStream(file);
        byte[] pom = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_APPLICATION_JAVA_SPRING_BOOT_VALID);
        fis = new FileInputStream(file);
        byte[] application = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_BOOTSTRAP_JAVA_SPRING_BOOT_VALID);
        fis = new FileInputStream(file);
        byte[] bootstrap = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_VALID);
        fis = new FileInputStream(file);
        byte[] novaFile = fis.readAllBytes();
        fis.close();

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);

        ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);
        ProjectFileReader.scanApplication("somePath", application, validatorInputs);
        ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

        List<String> applicationFiles = new ArrayList<>();
        applicationFiles.add("application.yml");
        validatorInputs.getApplication().setApplicationFiles(applicationFiles);
        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getPom().getGroupId());
        serviceDto.setDescription(validatorInputs.getPom().getDescription());
        serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
        serviceDto.setFinalName(validatorInputs.getPom().getFinalName());
        serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        serviceDto.setFolder("");
        serviceDto.setProjectDefinitionUrl("");
        serviceDto.setHasForcedCompilation(true);

        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "2.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        Assertions.assertEquals(1, serviceDto.getValidationErrors().length);

        PomXML.clearArtifactIdList();
    }

    @Test
    public void validateServiceFilesValidNodeThin2() throws Exception
    {
        byte[] pom = new byte[0];
        byte[] application = new byte[0];
        byte[] bootstrap = new byte[0];

        File file = ResourceUtils.getFile(CLASSPATH_NOVA_CDN_THIN2_VALID);
        FileInputStream fis = new FileInputStream(file);
        byte[] novaFile = fis.readAllBytes();
        fis.close();

        file = ResourceUtils.getFile(CLASSPATH_PACKAGE_JSON_CDN_THIN2_VALID);
        fis = new FileInputStream(file);
        byte[] packageJson = fis.readAllBytes();
        fis.close();

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);

        ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);
        ProjectFileReader.scanApplication("somePath", application, validatorInputs);
        ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);
        ProjectFileReader.scanPackageJson(file.getPath(), packageJson, validatorInputs);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

        List<String> applicationFiles = new ArrayList<>();
        applicationFiles.add("application.yml");
        validatorInputs.getApplication().setApplicationFiles(applicationFiles);
        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getNovaYml().getName());
        serviceDto.setDescription("");
        serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
        serviceDto.setFinalName("");
        serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        serviceDto.setFolder("");
        serviceDto.setProjectDefinitionUrl("");
        serviceDto.setHasForcedCompilation(true);

        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        Assertions.assertEquals(0, serviceDto.getValidationErrors().length);

        PomXML.clearArtifactIdList();
    }

    @Nested
    class validatePackageJsonDependencyVersion
    {
        @Test
        @DisplayName("Validate correct minimum version")
        public void validateMinimumVersion() throws Exception
        {
            byte[] pom = new byte[0];
            byte[] application = new byte[0];
            byte[] bootstrap = new byte[0];

            File file = ResourceUtils.getFile(CLASSPATH_NOVA_CDN_THIN2_VALID);
            FileInputStream fis = new FileInputStream(file);
            byte[] novaFile = fis.readAllBytes();
            fis.close();

            file = ResourceUtils.getFile(CLASSPATH_PACKAGE_JSON_CDN_KARMA_CORRECT);
            fis = new FileInputStream(file);
            byte[] packageJson = fis.readAllBytes();
            fis.close();

            ValidatorInputs validatorInputs = new ValidatorInputs();
            validatorInputs.setLatestVersion(true);

            ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
            ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);
            ProjectFileReader.scanApplication("somePath", application, validatorInputs);
            ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);
            ProjectFileReader.scanPackageJson(file.getPath(), packageJson, validatorInputs);

            NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
            serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

            List<String> applicationFiles = new ArrayList<>();
            applicationFiles.add("application.yml");
            validatorInputs.getApplication().setApplicationFiles(applicationFiles);
            serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
            serviceDto.setGroupId(validatorInputs.getNovaYml().getName());
            serviceDto.setDescription("");
            serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
            serviceDto.setFinalName("");
            serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
            serviceDto.setIsService(true);
            serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
            serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
            serviceDto.setMetadata(new RVMetadataDTO[0]);

            serviceDto.setFolder("");
            serviceDto.setProjectDefinitionUrl("");
            serviceDto.setHasForcedCompilation(true);

            projectFileValidator
                    .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

            Assertions.assertEquals(0, serviceDto.getValidationErrors().length);

            PomXML.clearArtifactIdList();
        }

        @Test
        @DisplayName("Validate incorrect minimum version")
        public void validateMinimumVersionKO() throws Exception
        {
            byte[] pom = new byte[0];
            byte[] application = new byte[0];
            byte[] bootstrap = new byte[0];

            File file = ResourceUtils.getFile(CLASSPATH_NOVA_CDN_THIN2_VALID);
            FileInputStream fis = new FileInputStream(file);
            byte[] novaFile = fis.readAllBytes();
            fis.close();

            file = ResourceUtils.getFile(CLASSPATH_PACKAGE_JSON_CDN_KARMA_INVALID);
            fis = new FileInputStream(file);
            byte[] packageJson = fis.readAllBytes();
            fis.close();

            ValidatorInputs validatorInputs = new ValidatorInputs();
            validatorInputs.setLatestVersion(true);

            ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
            ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);
            ProjectFileReader.scanApplication("somePath", application, validatorInputs);
            ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);
            ProjectFileReader.scanPackageJson(file.getPath(), packageJson, validatorInputs);

            NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
            serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

            List<String> applicationFiles = new ArrayList<>();
            applicationFiles.add("application.yml");
            validatorInputs.getApplication().setApplicationFiles(applicationFiles);
            serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
            serviceDto.setGroupId(validatorInputs.getNovaYml().getName());
            serviceDto.setDescription("");
            serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
            serviceDto.setFinalName("");
            serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
            serviceDto.setIsService(true);
            serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
            serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
            serviceDto.setMetadata(new RVMetadataDTO[0]);

            serviceDto.setFolder("");
            serviceDto.setProjectDefinitionUrl("");
            serviceDto.setHasForcedCompilation(true);

            projectFileValidator
                    .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

            Assertions.assertEquals(1, serviceDto.getValidationErrors().length);
            Assertions.assertEquals(Constants.PackageJsonDependencies.INVALID_DEPENDENCY_VERSION, serviceDto.getValidationErrors()[0].getCode());
            PomXML.clearArtifactIdList();
        }
    }


    @Test
    public void validateValidVersionMajorMinorFix() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_POM_JAVA_SPRING_BOOT_VALID);
        FileInputStream fis = new FileInputStream(file);
        byte[] pom = new byte[(int) file.length()];
        // Reads up to certain bytes of data from this input stream into an array of bytes.
        fis.read(pom);

        file = ResourceUtils.getFile(CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_VALID);
        fis = new FileInputStream(file);
        byte[] novaFile = new byte[(int) file.length()];
        // Reads up to certain bytes of data from this input stream into an array of bytes.
        fis.read(novaFile);

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);

        ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());


        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getPom().getGroupId());
        serviceDto.setDescription(validatorInputs.getPom().getDescription());
        serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
        serviceDto.setFinalName(validatorInputs.getPom().getFinalName());
        serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        serviceDto.setFolder("");
        serviceDto.setProjectDefinitionUrl("");
        serviceDto.setHasForcedCompilation(true);

        Assertions.assertTrue(PatternMatcher.customPattern(com.bbva.enoa.platformservices.coreservice.common.Constants.PATTERN_VERSION_REGEX, validatorInputs.getPom().getVersion()));

        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        List<ValidationErrorDto> errorList = new ArrayList<>(Arrays.asList(serviceDto.getValidationErrors()));
        ValidationErrorDto pomVersionError = new ValidationErrorDto();
        pomVersionError.setCode(Constants.INVALID_POM_VERSION);
        pomVersionError.setMessage(Constants.INVALID_POM_VERSION_MSG);

        Assertions.assertFalse(errorList.contains(pomVersionError));

        PomXML.clearArtifactIdList();


    }

    @Test
    public void validateInvalidVersionMajorMinorFix() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_POM_JAVA_SPRING_BOOT_INVALID);
        FileInputStream fis = new FileInputStream(file);
        byte[] pom = new byte[(int) file.length()];
        // Reads up to certain bytes of data from this input stream into an array of bytes.
        fis.read(pom);

        file = ResourceUtils.getFile(CLASSPATH_NOVA_YML_JAVA_SPRING_BOOT_VALID);
        fis = new FileInputStream(file);
        byte[] novaFile = new byte[(int) file.length()];
        // Reads up to certain bytes of data from this input stream into an array of bytes.
        fis.read(novaFile);

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);

        ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);

        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());


        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getPom().getGroupId());
        serviceDto.setDescription(validatorInputs.getPom().getDescription());
        serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
        serviceDto.setFinalName(validatorInputs.getPom().getFinalName());
        serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        serviceDto.setFolder("");
        serviceDto.setProjectDefinitionUrl("");
        serviceDto.setHasForcedCompilation(true);

        Assertions.assertFalse(PatternMatcher.customPattern(com.bbva.enoa.platformservices.coreservice.common.Constants.PATTERN_VERSION_REGEX, validatorInputs.getPom().getVersion()));

        this.projectFileValidator
                .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");

        List<ValidationErrorDto> errorList = new ArrayList<>(Arrays.asList(serviceDto.getValidationErrors()));
        ValidationErrorDto pomVersionError = new ValidationErrorDto();
        pomVersionError.setCode(Constants.INVALID_POM_VERSION);
        pomVersionError.setMessage(Constants.INVALID_POM_VERSION_MSG);

        Assertions.assertTrue(errorList.contains(pomVersionError));

        PomXML.clearArtifactIdList();
    }

    @Test
    public void validateValidNonEPhoenixPomVersionsApiRest()
    {
        validateValidNonEPhoenixPomVersions(ServiceType.API_REST_JAVA_SPRING_BOOT.getServiceType());
    }

    @Test
    public void validateValidNonEPhoenixPomVersionsApi()
    {
        validateValidNonEPhoenixPomVersions(ServiceType.API_JAVA_SPRING_BOOT.getServiceType());
    }

    private void validateValidNonEPhoenixPomVersions(String serviceType)
    {

        NovaYml novaYml = new NovaYml();
        novaYml.setApplicationName("testDependency");
        novaYml.setLanguage("JAVA_SPRING_BOOT");
        novaYml.setLanguageVersion("1.8.121");
        novaYml.setName("testDependency");
        novaYml.setNovaVersion("18.04");
        novaYml.setProjectName("testDependency");
        novaYml.setServiceType(serviceType);
        novaYml.setUuaa("ENVM");
        novaYml.setVersion("1.0.0");
        novaYml.setValid(false);
        novaYml.setApiServed(Collections.singletonList(new NovaYmlApi()));

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);
        validatorInputs.setLatestVersion(true);
        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getPom().getGroupId());
        serviceDto.setDescription(validatorInputs.getPom().getDescription());
        serviceDto.setServiceName(validatorInputs.getNovaYml().getName());
        serviceDto.setFinalName(validatorInputs.getPom().getFinalName());
        serviceDto.setServiceType(validatorInputs.getNovaYml().getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setUuaa(validatorInputs.getNovaYml().getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        List<ValidationErrorDto> errorList = new ArrayList<>();
        ValidationErrorDto pomVersionError = new ValidationErrorDto();
        pomVersionError.setCode(Constants.INVALID_POM_VERSION);
        pomVersionError.setMessage(Constants.INVALID_POM_VERSION_MSG);

        for (int major = 0; major < 1000; major += 100)
        {

            for (int minor = 0; minor < 1000; minor += 99)
            {

                for (int fix = 0; fix < 1000; fix += 100)
                {
                    String version = major + "." + minor + "." + fix;
                    validatorInputs.getPom().setVersion(version);
                    this.projectFileValidator
                            .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");
                    errorList.addAll(Arrays.asList(serviceDto.getValidationErrors()));

                }
            }
        }

        Assertions.assertFalse(errorList.contains(pomVersionError));
    }

    @Test
    public void validateInvalidNonEPhoenixPomVersionsApiRest()
    {
        validateInvalidNonEPhoenixPomVersions(ServiceType.API_REST_JAVA_SPRING_BOOT.getServiceType());
    }

    @Test
    public void validateInvalidNonEPhoenixPomVersionsApi()
    {
        validateInvalidNonEPhoenixPomVersions(ServiceType.API_JAVA_SPRING_BOOT.getServiceType());
    }

    private void validateInvalidNonEPhoenixPomVersions(String serviceType)
    {

        NovaYml novaYml = new NovaYml();
        novaYml.setApplicationName("testDependency");
        novaYml.setLanguage("JAVA_SPRING_BOOT");
        novaYml.setLanguageVersion("1.8.121");
        novaYml.setName("testDependency");
        novaYml.setNovaVersion("18.04");
        novaYml.setProjectName("testDependency");
        novaYml.setServiceType(serviceType);
        novaYml.setUuaa("ENVM");
        novaYml.setVersion("1.0.0");
        novaYml.setValid(false);
        novaYml.setApiServed(Collections.singletonList(new NovaYmlApi()));

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        validatorInputs.setLatestVersion(true);
        NewReleaseVersionServiceDto serviceDto = new NewReleaseVersionServiceDto();
        serviceDto.setArtifactId(validatorInputs.getNovaYml().getName());

        serviceDto.setVersion(validatorInputs.getNovaYml().getVersion());
        serviceDto.setGroupId(validatorInputs.getPom().getGroupId());
        serviceDto.setDescription(validatorInputs.getPom().getDescription());
        serviceDto.setServiceName(novaYml.getName());
        serviceDto.setFinalName(validatorInputs.getPom().getFinalName());
        serviceDto.setServiceType(novaYml.getServiceType());
        serviceDto.setIsService(true);
        serviceDto.setNovaVersion(novaYml.getVersion());
        serviceDto.setUuaa(novaYml.getUuaa());
        serviceDto.setMetadata(new RVMetadataDTO[0]);

        List<ValidationErrorDto> errorList = new ArrayList<>();
        ValidationErrorDto pomVersionError = new ValidationErrorDto();
        pomVersionError.setCode(Constants.INVALID_POM_VERSION);
        pomVersionError.setMessage(Constants.INVALID_POM_VERSION_MSG);

        validatorInputs.getPom().setVersion("1.2.a");
        ArrayList<String> invalidVersions = new ArrayList<>(
                Arrays.asList("11.2.a2", "1.1a.22", "a.222.22", "1111.2.3", "11.2222.22", "11.11.1111", "1.0"));

        for (String version : invalidVersions)
        {
            validatorInputs.getPom().setVersion(version);
            this.projectFileValidator
                    .validateServiceProjectFiles(validatorInputs, SubsystemType.NOVA, serviceDto, 1, "1.0.0", "xe00000", "releaseTest", buildProduct(), "subsystemTest");
            errorList.addAll(Arrays.asList(serviceDto.getValidationErrors()));
        }

        Assertions.assertEquals(7, errorList.stream().filter(error -> error.getCode().equals(Constants.INVALID_POM_VERSION)).count());
    }

    private Product buildProduct()
    {
        Product product = new Product();
        product.setUuaa("ENVM");
        return product;
    }
}
