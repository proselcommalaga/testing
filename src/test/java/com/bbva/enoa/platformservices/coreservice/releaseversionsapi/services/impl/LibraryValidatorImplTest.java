package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryValidationErrorDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
import com.bbva.enoa.datamodel.model.release.entities.AllowedJdk;
import com.bbva.enoa.datamodel.model.release.entities.JdkParameter;
import com.bbva.enoa.platformservices.coreservice.common.repositories.AllowedJdkRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConnectorTypeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.JdkParameterRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ILibraryManagerClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYamlContextParam;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYamlInputParams;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYamlOutputParams;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYamlProperty;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYmlApi;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYmlPort;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYmlRequirement;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.PomXML;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.enumerates.EnvironmentVariableBlacklist;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.maven.model.Dependency;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class LibraryValidatorImplTest
{

    /**
     * Original Set of ServiceType allowed to declare use of nova libraries
     */
    private static final EnumSet<ServiceType> SERVICE_TYPES_WHITELIST = EnumSet
            .of(ServiceType.API_REST_JAVA_SPRING_BOOT, ServiceType.API_JAVA_SPRING_BOOT, ServiceType.API_REST_NODE_JS_EXPRESS, ServiceType.DAEMON_JAVA_SPRING_BOOT, ServiceType.NODE,
                    ServiceType.BATCH_JAVA_SPRING_CLOUD_TASK, ServiceType.BATCH_JAVA_SPRING_BATCH, ServiceType.API_REST_PYTHON_FLASK,ServiceType.BATCH_PYTHON,
                    ServiceType.FRONTCAT_JAVA_SPRING_MVC);

    private enum CONNECTORS_REQUIREMENT_VALUE
    {
        TCP, BBDD, TIBCO, COLASMQ, FIX, MONGODB, HTTP, INTERNET, BIGDATA, BATCH, MONGOLIBRARY
    }

    @Mock
    private ILibraryManagerClient libMgrClient;

    @Mock
    private ConnectorTypeRepository connectorTypeRepository;

    @Mock
    private AllowedJdkRepository allowedJdkRepository;

    @Mock
    private JdkParameterRepository jdkParameterRepository;

    /**
     * Instance to test
     */
    @InjectMocks
    private LibraryValidatorImpl instance;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        populateConnectorTypeRepositoryMock();
    }

    /**
     * types of properties allowed by nova.yml
     */
    private static final String[] PROPERTY_TYPE = new String[]{"STRING", "XML", "JSON"};

    /**
     * Scope of properties allowed by nova.yml
     */
    private static final String[] PROPERTY_SCOPE = new String[]{"SERVICE", "GLOBAL"};

    /**
     * Management of properties allowed by nova.yml
     */
    private static final String[] PROPERTY_MANAGEMENT = new String[]{"SERVICE", "LIBRARY", "ENVIRONMENT"};


    @Test
    public void validateLibrariesNoLibsApiRest(){
        validateLibrariesNoLibs(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void validateLibrariesNoLibsApi(){
        validateLibrariesNoLibs(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void validateLibrariesNoLibs(ServiceType serviceType)
    {
        NewReleaseVersionServiceDto dto = new NewReleaseVersionServiceDto();
        dto.fillRandomly(1, false, 1, 1);
        String tag = RandomStringUtils.randomAlphabetic(10);
        String releaseNameVersion = RandomStringUtils.randomAlphabetic(10);

        List<ValidationErrorDto> errors = new ArrayList<>();
        NovaYml novaYml = getNovaYMLByNewReleaseVersionServiceDto(dto, serviceType, Collections.emptyList());
        ValidatorInputs validatorInputs = createValidatorInputsByNovaYml(novaYml);

        instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
        Assertions.assertTrue(errors.isEmpty());

        novaYml.setLibraries(null);
        instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
        Assertions.assertTrue(errors.isEmpty());
    }

    @Test
    public void validateLibrariesDoesNotAffectOtherValidationsByEmptyLibsWithApiRest()
    {
        validateLibrariesDoesNotAffectOtherValidationsByEmptyLibs(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void validateLibrariesDoesNotAffectOtherValidationsByEmptyLibsWithApi()
    {
        validateLibrariesDoesNotAffectOtherValidationsByEmptyLibs(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void validateLibrariesDoesNotAffectOtherValidationsByEmptyLibs(ServiceType serviceType)
    {
        NewReleaseVersionServiceDto dto = new NewReleaseVersionServiceDto();
        dto.fillRandomly(1, false, 0, 1);
        String tag = RandomStringUtils.randomAlphabetic(10);
        String releaseNameVersion = RandomStringUtils.randomAlphabetic(10);

        ValidationErrorDto vedto = new ValidationErrorDto();
        vedto.setCode(RandomStringUtils.randomAlphabetic(5));
        vedto.setMessage(RandomStringUtils.randomAlphabetic(15));

        List<ValidationErrorDto> errors = Collections.singletonList(vedto);
        NovaYml novaYml = getNovaYMLByNewReleaseVersionServiceDto(dto, serviceType, Collections.emptyList());
        ValidatorInputs validatorInputs = createValidatorInputsByNovaYml(novaYml);

        instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
        Assertions.assertEquals(1, errors.size());
    }

    @Test
    public void validateLibrariesByServiceType()
    {
        String libFullName = "uuaa-release-service-version";
        NewReleaseVersionServiceDto dto = new NewReleaseVersionServiceDto();
        dto.fillRandomly(2, false, 0, 1);
        dto.setServiceName(RandomStringUtils.randomAlphabetic(10));
        String tag = RandomStringUtils.randomAlphabetic(10);
        String releaseNameVersion = RandomStringUtils.randomAlphabetic(10);
        List<ValidationErrorDto> errors = new ArrayList<>();
        NovaYml novaYml;
        ValidatorInputs validatorInputs;
        List<String> libraries = (Collections.singletonList(libFullName));


        LMLibraryEnvironmentsDTO libEnvDto = new LMLibraryEnvironmentsDTO();
        libEnvDto.setFullName(libFullName);
        libEnvDto.setInte(true);
        when(libMgrClient.getLibraryEnvironments(libraries.get(0))).thenReturn(libEnvDto);
        LMLibraryRequirementsDTO requirementsDTO = new LMLibraryRequirementsDTO();
        requirementsDTO.setFullName(libFullName);
        requirementsDTO.setRequirements(new LMLibraryRequirementDTO[0]);
        when(libMgrClient.getRequirementsByFullName(libFullName)).thenReturn(requirementsDTO);

        for (ServiceType serviceType : ServiceType.values())
        {
            errors.clear();
            novaYml = getNovaYMLByNewReleaseVersionServiceDto(dto, serviceType, libraries);
            dto.setServiceType(serviceType.getServiceType());
            novaYml.setServiceType(serviceType.getServiceType());
            validatorInputs = createValidatorInputsByNovaYml(novaYml);

            instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);

            if (SERVICE_TYPES_WHITELIST.contains(serviceType))
            {
                Assertions.assertTrue(errors.isEmpty(), "service type " + serviceType + " is valid to use libraries");
            }
            else
            {
                Assertions.assertFalse(errors.isEmpty(), "service type " + serviceType + " is not valid to use libraries");
                Assertions.assertTrue(errors.stream().anyMatch(e -> e.getCode().equals(Constants.LIBRARY_SERVICE_TYPE_NOT_ALLOWED_LIBS)), "service type " + serviceType + " is not valid to use libraries");
            }
        }
    }

    @Test
    public void validateLibrariesByServiceTypeAndNoDescriptor()
    {
        String libFullName = "uuaa-release-service-version";
        NewReleaseVersionServiceDto dto = new NewReleaseVersionServiceDto();
        dto.fillRandomly(2, false, 0, 1);
        dto.setServiceName(RandomStringUtils.randomAlphabetic(10));
        String tag = RandomStringUtils.randomAlphabetic(10);
        String releaseNameVersion = RandomStringUtils.randomAlphabetic(10);
        List<ValidationErrorDto> errors = new ArrayList<>();
        NovaYml novaYml;
        ValidatorInputs validatorInputs;
        List<String> libraries = (Collections.singletonList(libFullName));


        LMLibraryEnvironmentsDTO libEnvDto = new LMLibraryEnvironmentsDTO();
        libEnvDto.setFullName(libFullName);
        libEnvDto.setInte(true);
        when(libMgrClient.getLibraryEnvironments(libraries.get(0))).thenReturn(libEnvDto);
        LMLibraryRequirementsDTO requirementsDTO = new LMLibraryRequirementsDTO();
        requirementsDTO.setFullName(libFullName);
        requirementsDTO.setRequirements(new LMLibraryRequirementDTO[0]);
        when(libMgrClient.getRequirementsByFullName(libFullName)).thenReturn(requirementsDTO);

        for (ServiceType serviceType : ServiceType.values())
        {
            errors.clear();
            novaYml = getNovaYMLByNewReleaseVersionServiceDto(dto, serviceType, libraries);
            dto.setServiceType(serviceType.getServiceType());
            novaYml.setServiceType(serviceType.getServiceType());
            validatorInputs = new ValidatorInputs();
            validatorInputs.setNovaYml(novaYml);
            validatorInputs.setPom(null);
            //No descriptor loaded...
            instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);

            if (SERVICE_TYPES_WHITELIST.contains(serviceType))
            {
                if (serviceType.isPomXml())
                {
                    Assertions.assertFalse(errors.isEmpty(), "service type " + serviceType + " has to have a pom.xml");
                    Assertions.assertTrue(errors.stream().anyMatch(e -> e.getMessage().contains("pom.xml is not found")), "service type " + serviceType + " has to have a pom.xml");
                }
                else
                {
                    Assertions.assertTrue(errors.isEmpty(),"service type " + serviceType + " is valid to use libraries");
                }
            }
            else
            {
                Assertions.assertFalse(errors.isEmpty(), "service type " + serviceType + " is not valid to use libraries");
                Assertions.assertTrue(errors.stream().anyMatch(e -> e.getCode().equals(Constants.LIBRARY_SERVICE_TYPE_NOT_ALLOWED_LIBS)), "service type " + serviceType + " is not valid to use libraries");
            }
        }
    }

    @Test
    public void validateLibrariesDoesNotAffectOtherValidations()
    {
        String libFullName = "uuaa-release-service-version";

        NewReleaseVersionServiceDto dto = new NewReleaseVersionServiceDto();
        dto.fillRandomly(2, false, 0, 1);
        dto.setServiceName(RandomStringUtils.randomAlphabetic(10));
        String tag = RandomStringUtils.randomAlphabetic(10);
        String releaseNameVersion = RandomStringUtils.randomAlphabetic(10);
        List<ValidationErrorDto> errors = new ArrayList<>();
        NovaYml novaYml;
        ValidatorInputs validatorInputs;
        List<String> libraries = (Collections.singletonList(libFullName));

        LMLibraryEnvironmentsDTO libEnvDto = new LMLibraryEnvironmentsDTO();
        libEnvDto.setFullName(libFullName);
        libEnvDto.setInte(true);
        when(libMgrClient.getLibraryEnvironments(libraries.get(0))).thenReturn(libEnvDto);
        LMLibraryRequirementsDTO requirementsDTO = new LMLibraryRequirementsDTO();
        requirementsDTO.setFullName(libFullName);
        requirementsDTO.setRequirements(new LMLibraryRequirementDTO[0]);
        when(libMgrClient.getRequirementsByFullName(libFullName)).thenReturn(requirementsDTO);

        ValidationErrorDto vedto = new ValidationErrorDto();
        vedto.setCode(RandomStringUtils.randomAlphabetic(5));
        vedto.setMessage(RandomStringUtils.randomAlphabetic(15));

        for (ServiceType serviceType : ServiceType.values())
        {
            errors.clear();
            errors.add(vedto);
            novaYml = getNovaYMLByNewReleaseVersionServiceDto(dto, serviceType, libraries);
            dto.setServiceType(serviceType.getServiceType());
            novaYml.setServiceType(serviceType.getServiceType());
            validatorInputs = createValidatorInputsByNovaYml(novaYml);

            instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);

            if (SERVICE_TYPES_WHITELIST.contains(serviceType))
            {
                Assertions.assertEquals(1, errors.size());
                Assertions.assertEquals(vedto, errors.get(0));
            }
            else
            {
                Assertions.assertTrue(errors.size() > 1);
                Assertions.assertTrue(errors.stream().anyMatch(e -> e.getCode().equals(Constants.LIBRARY_SERVICE_TYPE_NOT_ALLOWED_LIBS)));
            }
        }
    }

    @Test
    public void validateLibrariesByEnvNullWithApiRest()
    {
        validateLibrariesByEnvNull(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void validateLibrariesByEnvNullWithApi()
    {
        validateLibrariesByEnvNull(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void validateLibrariesByEnvNull(ServiceType serviceType)
    {
        String libFullName = "uuaa-release-service-version";
        NewReleaseVersionServiceDto dto = new NewReleaseVersionServiceDto();
        dto.fillRandomly(1, false, 0, 1);
        String tag = RandomStringUtils.randomAlphabetic(10);
        String releaseNameVersion = RandomStringUtils.randomAlphabetic(10);

        ValidationErrorDto vedto = new ValidationErrorDto();
        vedto.setCode(RandomStringUtils.randomAlphabetic(5));
        vedto.setMessage(RandomStringUtils.randomAlphabetic(15));
        List<String> libraries = (Collections.singletonList(libFullName));
        List<ValidationErrorDto> errors = new ArrayList<>();

        NovaYml novaYml = getNovaYMLByNewReleaseVersionServiceDto(dto, serviceType, libraries);
        ValidatorInputs validatorInputs = createValidatorInputsByNovaYml(novaYml);

        instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
        Assertions.assertFalse(errors.isEmpty());
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(Constants.LIBRARY_NOT_EXISTS, errors.get(0).getCode());
    }

    @Test
    public void validateLibrariesByExceptionWithApiRest()
    {
        validateLibrariesByException(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void validateLibrariesByExceptionWithApi()
    {
        validateLibrariesByException(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void validateLibrariesByException(ServiceType serviceType)
    {
        String libFullName = "uuaa-release-service-version";
        NewReleaseVersionServiceDto dto = new NewReleaseVersionServiceDto();
        dto.fillRandomly(1, false, 0, 1);
        String tag = RandomStringUtils.randomAlphabetic(10);
        String releaseNameVersion = RandomStringUtils.randomAlphabetic(10);

        Mockito.doThrow(NovaException.class).when(libMgrClient).getLibraryEnvironments(Mockito.anyString());

        ValidationErrorDto vedto = new ValidationErrorDto();
        vedto.setCode(RandomStringUtils.randomAlphabetic(5));
        vedto.setMessage(RandomStringUtils.randomAlphabetic(15));
        List<String> libraries = (Collections.singletonList(libFullName));
        List<ValidationErrorDto> errors = new ArrayList<>();

        NovaYml novaYml = getNovaYMLByNewReleaseVersionServiceDto(dto, serviceType, libraries);
        ValidatorInputs validatorInputs = createValidatorInputsByNovaYml(novaYml);

        instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
        Assertions.assertFalse(errors.isEmpty());
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(Constants.LIBRARY_UNEXPECTED_ERROR_RESPONSE, errors.get(0).getCode());
    }

    @Test
    public void validateLibrariesByAlmostOneEnvWithApiRest()
    {
        validateLibrariesByAlmostOneEnv(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void validateLibrariesByAlmostOneEnvWithApi()
    {
        validateLibrariesByAlmostOneEnv(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void validateLibrariesByAlmostOneEnv(ServiceType serviceType)
    {
        String libFullName = "uuaa-release-service-v0";
        LMLibraryEnvironmentsDTO envDto = new LMLibraryEnvironmentsDTO();
        envDto.setFullName(libFullName);
        envDto.setReleaseVersionServiceId(RandomUtils.nextInt(0, 99999));
        when(libMgrClient.getLibraryEnvironments(libFullName)).thenReturn(envDto);
        LMLibraryRequirementsDTO requirementsDTO = new LMLibraryRequirementsDTO();
        requirementsDTO.setFullName(libFullName);
        requirementsDTO.setRequirements(new LMLibraryRequirementDTO[0]);
        when(libMgrClient.getRequirementsByFullName(libFullName)).thenReturn(requirementsDTO);

        NewReleaseVersionServiceDto dto = new NewReleaseVersionServiceDto();
        dto.fillRandomly(2, false, 0, 1);
        dto.setServiceName(RandomStringUtils.randomAlphabetic(10));
        String tag = RandomStringUtils.randomAlphabetic(10);
        String releaseNameVersion = RandomStringUtils.randomAlphabetic(10);
        List<ValidationErrorDto> errors = new ArrayList<>();
        NovaYml novaYml = getNovaYMLByNewReleaseVersionServiceDto(dto, serviceType, Collections.singletonList(libFullName));
        ValidatorInputs validatorInputs = createValidatorInputsByNovaYml(novaYml);

        for (int i = 1; i < 8; i++)
        {
            envDto.setInte((i & 1) != 0);
            envDto.setPre((i & (1L << 1)) != 0);
            envDto.setPro((i & (1L << 2)) != 0);

            System.out.println(envDto);
            instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
            Assertions.assertTrue(errors.isEmpty());
        }
    }

    @Test
    public void validateLibrariesNotInPomWithApiRest()
    {
        validateLibrariesNotInPom(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void validateLibrariesNotInPomWithApi()
    {
        validateLibrariesNotInPom(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void validateLibrariesNotInPom(ServiceType serviceType)
    {
        String libFullName = "uuaa-release-service-1.0.0";
        NewReleaseVersionServiceDto dto = new NewReleaseVersionServiceDto();
        dto.fillRandomly(1, false, 0, 1);
        String tag = RandomStringUtils.randomAlphabetic(10);
        String releaseNameVersion = RandomStringUtils.randomAlphabetic(10);

        ValidationErrorDto vedto = new ValidationErrorDto();
        vedto.setCode(RandomStringUtils.randomAlphabetic(5));
        vedto.setMessage(RandomStringUtils.randomAlphabetic(15));
        List<String> libraries = (Collections.singletonList(libFullName));
        List<ValidationErrorDto> errors = new ArrayList<>();

        NovaYml novaYml = getNovaYMLByNewReleaseVersionServiceDto(dto, serviceType, libraries);
        ValidatorInputs validatorInputs = createValidatorInputsByNovaYml(novaYml);


        LMLibraryEnvironmentsDTO libEnvironmentDto = new LMLibraryEnvironmentsDTO();
        libEnvironmentDto.setReleaseVersionServiceId(RandomUtils.nextInt(0, 10000));
        libEnvironmentDto.setFullName(libFullName);
        libEnvironmentDto.setPro(true);
        libEnvironmentDto.setPre(true);
        libEnvironmentDto.setInte(true);
        when(libMgrClient.getLibraryEnvironments(libFullName)).thenReturn(libEnvironmentDto);
        LMLibraryRequirementsDTO requirementsDTO = new LMLibraryRequirementsDTO();
        requirementsDTO.setFullName(libFullName);
        requirementsDTO.setRequirements(new LMLibraryRequirementDTO[0]);
        when(libMgrClient.getRequirementsByFullName(libFullName)).thenReturn(requirementsDTO);

        instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
        Assertions.assertTrue(errors.isEmpty());

        // pom without dependencies
        PomXML pom = new PomXML();

        pom.setGroupId("com.bbva." + novaYml.getUuaa());
        pom.setArtifactId(novaYml.getApplicationName());
        pom.setVersion(novaYml.getVersion());
        pom.setDescription(novaYml.getDescription());
        validatorInputs.setPom(pom);

        instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
        Assertions.assertFalse(errors.isEmpty());
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(Constants.LIBRARY_UNDECLARED_IN_DESCRIPTOR_FILE, errors.get(0).getCode());
    }

    @Test
    public void validateLibrariesDifferentVersionWithApiRest()
    {
        validateLibrariesDifferentVersion(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }
    @Test
    public void validateLibrariesDifferentVersionWithApi()
    {
        validateLibrariesDifferentVersion(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void validateLibrariesDifferentVersion(ServiceType serviceType)
    {
        String libFullName = "uuaa-release-service-1.0.0";
        NewReleaseVersionServiceDto dto = new NewReleaseVersionServiceDto();
        dto.fillRandomly(1, false, 0, 1);
        String tag = RandomStringUtils.randomAlphabetic(10);
        String releaseNameVersion = RandomStringUtils.randomAlphabetic(10);

        ValidationErrorDto vedto = new ValidationErrorDto();
        vedto.setCode(RandomStringUtils.randomAlphabetic(5));
        vedto.setMessage(RandomStringUtils.randomAlphabetic(15));
        List<String> libraries = (Collections.singletonList(libFullName));
        List<ValidationErrorDto> errors = new ArrayList<>();

        NovaYml novaYml = getNovaYMLByNewReleaseVersionServiceDto(dto, serviceType, libraries);
        ValidatorInputs validatorInputs = createValidatorInputsByNovaYml(novaYml);


        LMLibraryEnvironmentsDTO libEnvironmentDto = new LMLibraryEnvironmentsDTO();
        libEnvironmentDto.setReleaseVersionServiceId(RandomUtils.nextInt(0, 10000));
        libEnvironmentDto.setFullName(libFullName);
        libEnvironmentDto.setPro(true);
        libEnvironmentDto.setPre(true);
        libEnvironmentDto.setInte(true);
        when(libMgrClient.getLibraryEnvironments(libFullName)).thenReturn(libEnvironmentDto);
        LMLibraryRequirementsDTO requirementsDTO = new LMLibraryRequirementsDTO();
        requirementsDTO.setFullName(libFullName);
        requirementsDTO.setRequirements(new LMLibraryRequirementDTO[0]);
        when(libMgrClient.getRequirementsByFullName(libFullName)).thenReturn(requirementsDTO);

        instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
        Assertions.assertTrue(errors.isEmpty());

        //Bad version into library library version
        Dependency currentDependency = validatorInputs.getPom().getDependencies().get(0);
        currentDependency.setVersion("2.0.0");
        instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
        Assertions.assertFalse(errors.isEmpty());
        Assertions.assertEquals(1, errors.size(), "There is a bad version in pom.xml");
        Assertions.assertEquals(Constants.LIBRARY_VERSION_NOT_MATCH_IN_DESCRIPTOR_FILE, errors.get(0).getCode(), "There is a bad version in pom.xml");


        //Clone dependency and fix its version to validate Double dependency
        errors.clear();
        validatorInputs.getPom().addDependency(currentDependency.clone());
        currentDependency.setVersion("1.0.0");
        instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
        Assertions.assertFalse(errors.isEmpty());
        Assertions.assertEquals(1, errors.size(), "Double dependency with wrong version");
        Assertions.assertEquals(Constants.LIBRARY_VERSION_NOT_MATCH_IN_DESCRIPTOR_FILE, errors.get(0).getCode(), "Double dependency with wrong version");
    }


    @Test
    public void validateLibrariesWithLanguageVersionRequirementsWithApiRest()
    {
        validateLibrariesWithLanguageVersionRequirements(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void validateLibrariesWithLanguageVersionRequirementsWithApi()
    {
        validateLibrariesWithLanguageVersionRequirements(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void validateLibrariesWithLanguageVersionRequirements (ServiceType serviceType)
    {
        String libFullName = "uuaa-release-service-1.0.0";
        NewReleaseVersionServiceDto dto = new NewReleaseVersionServiceDto();
        dto.fillRandomly(1, false, 0, 1);
        String tag = RandomStringUtils.randomAlphabetic(10);
        String releaseNameVersion = RandomStringUtils.randomAlphabetic(10);

        List<String> libraries = Collections.singletonList(libFullName);
        List<ValidationErrorDto> errors = new ArrayList<>();

        NovaYml novaYml = getNovaYMLByNewReleaseVersionServiceDto(dto, serviceType, libraries);
        novaYml.setLanguageVersion("11.0.11");
        ValidatorInputs validatorInputs = createValidatorInputsByNovaYml(novaYml);


        LMLibraryEnvironmentsDTO libEnvironmentDto = new LMLibraryEnvironmentsDTO();
        libEnvironmentDto.setReleaseVersionServiceId(RandomUtils.nextInt(0, 10000));
        libEnvironmentDto.setFullName(libFullName);
        libEnvironmentDto.setPro(true);
        libEnvironmentDto.setPre(true);
        libEnvironmentDto.setInte(true);
        when(libMgrClient.getLibraryEnvironments(libFullName)).thenReturn(libEnvironmentDto);

        LMLibraryRequirementsDTO requirementsDTO = new LMLibraryRequirementsDTO();
        requirementsDTO.setFullName(libFullName);
        LMLibraryRequirementDTO minimum = new LMLibraryRequirementDTO();
        minimum.setRequirementName(Constants.INSTALLATION_REQUIREMENT_NAME.MINIMUM_LANGUAGE_VERSION.name());
        minimum.setRequirementType(Constants.REQUIREMENT_TYPE.INSTALLATION.name());
        minimum.setRequirementValue("11.0.7");
        LMLibraryRequirementDTO maximum = new LMLibraryRequirementDTO();
        maximum.setRequirementName(Constants.INSTALLATION_REQUIREMENT_NAME.MAXIMUM_LANGUAGE_VERSION.name());
        maximum.setRequirementType(Constants.REQUIREMENT_TYPE.INSTALLATION.name());
        maximum.setRequirementValue("12");
        requirementsDTO.setRequirements(new LMLibraryRequirementDTO[]{minimum,maximum});
        when(libMgrClient.getRequirementsByFullName(libFullName)).thenReturn(requirementsDTO);

        instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
        Assertions.assertTrue(errors.isEmpty());

        LMLibraryRequirementDTO minimum2 = new LMLibraryRequirementDTO();
        minimum2.setRequirementName(Constants.INSTALLATION_REQUIREMENT_NAME.MINIMUM_LANGUAGE_VERSION.name());
        minimum2.setRequirementType(Constants.REQUIREMENT_TYPE.INSTALLATION.name());
        minimum2.setRequirementValue("11.0.12");

        requirementsDTO.setRequirements(new LMLibraryRequirementDTO[]{minimum2,maximum});
        when(libMgrClient.getRequirementsByFullName(libFullName)).thenReturn(requirementsDTO);

        instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(Constants.INVALID_REQUIREMENT_VALUE, errors.get(0).getCode());
    }

    @Test
    public void validateLibrariesWithJdkVersionRequirementWithApiRest()
    {
        validateLibrariesWithJdkVersionRequirement(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }

    @Test
    public void validateLibrariesWithJdkVersionRequirementWithApi()
    {
        validateLibrariesWithJdkVersionRequirement(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void validateLibrariesWithJdkVersionRequirement(ServiceType serviceType)
    {
        String libFullName = "uuaa-release-service-1.0.0";
        NewReleaseVersionServiceDto dto = new NewReleaseVersionServiceDto();
        dto.fillRandomly(1, false, 0, 1);
        String tag = RandomStringUtils.randomAlphabetic(10);
        String releaseNameVersion = RandomStringUtils.randomAlphabetic(10);

        List<String> libraries = Collections.singletonList(libFullName);
        List<ValidationErrorDto> errors = new ArrayList<>();

        NovaYml novaYml = getNovaYMLByNewReleaseVersionServiceDto(dto, serviceType, libraries);
        novaYml.setJdkVersion("amazon-corretto-11.0.11.9.1");
        ValidatorInputs validatorInputs = createValidatorInputsByNovaYml(novaYml);


        LMLibraryEnvironmentsDTO libEnvironmentDto = new LMLibraryEnvironmentsDTO();
        libEnvironmentDto.setReleaseVersionServiceId(RandomUtils.nextInt(0, 10000));
        libEnvironmentDto.setFullName(libFullName);
        libEnvironmentDto.setPro(true);
        libEnvironmentDto.setPre(true);
        libEnvironmentDto.setInte(true);
        when(libMgrClient.getLibraryEnvironments(libFullName)).thenReturn(libEnvironmentDto);

        // Successful
        LMLibraryRequirementsDTO requirementsDTO = new LMLibraryRequirementsDTO();
        requirementsDTO.setFullName(libFullName);
        LMLibraryRequirementDTO jdkRequirement = new LMLibraryRequirementDTO();
        jdkRequirement.setRequirementName(Constants.INSTALLATION_REQUIREMENT_NAME.SUPPORTED_JDK_VERSIONS.name());
        jdkRequirement.setRequirementType(Constants.REQUIREMENT_TYPE.INSTALLATION.name());
        jdkRequirement.setRequirementValue("zulu11.*|amazon-corretto-11.0.11.9.1");
        requirementsDTO.setRequirements(new LMLibraryRequirementDTO[]{jdkRequirement});
        when(libMgrClient.getRequirementsByFullName(libFullName)).thenReturn(requirementsDTO);

        instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
        Assertions.assertTrue(errors.isEmpty());

        // Error
        LMLibraryRequirementDTO jdkRequirement2 = new LMLibraryRequirementDTO();
        jdkRequirement2.setRequirementName(Constants.INSTALLATION_REQUIREMENT_NAME.SUPPORTED_JDK_VERSIONS.name());
        jdkRequirement2.setRequirementType(Constants.REQUIREMENT_TYPE.INSTALLATION.name());
        jdkRequirement2.setRequirementValue("zulu11.*");
        requirementsDTO.setRequirements(new LMLibraryRequirementDTO[]{jdkRequirement2});
        when(libMgrClient.getRequirementsByFullName(libFullName)).thenReturn(requirementsDTO);

        instance.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseNameVersion, errors);
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(Constants.INVALID_REQUIREMENT_VALUE, errors.get(0).getCode());

        //Empty jdkVersion field in nova.yml
        NovaYml novaYml2 = getNovaYMLByNewReleaseVersionServiceDto(dto, serviceType, libraries);
        ValidatorInputs validatorInputs2 = createValidatorInputsByNovaYml(novaYml2);
        errors.clear();

        instance.validateLibrariesFromNovaYml(validatorInputs2, dto, tag, releaseNameVersion, errors);
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals(Constants.INVALID_REQUIREMENT_VALUE, errors.get(0).getCode());
    }

    @Test
    public void validateLibraryServiceWithInvalidSection()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        //create a valid novayml for a library
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        fillRandomlyRequirements(novaYml);
        //ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        //Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(new LMLibraryValidationErrorDTO[0]);

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        //check the file is valid until now
        Assertions.assertTrue(errorList.isEmpty());


        //Sections not allowed
        String[] sections = new String[]{Constants.BUILD, Constants.MACHINES, Constants.PORTS, Constants.CONSUMED, Constants.SERVED, Constants.LIBRARIES, Constants.CONTEXT_PARAMS, Constants.INPUT_PARAMS, Constants.OUTPUT_PARAMS};
        int size = 1 << sections.length;

        NovaYml current;
        int counter;
        for (int i = 1; i < size; i++)
        {
            counter = 0;
            errorList.clear();

            current = copyNovaYml(novaYml);
            validatorInputs.setNovaYml(current);

            for (int j = 0; j < sections.length; j++)
            {
                if ((i & (1L << j)) != 0)
                {
                    counter++;
                    fillRandomlySectionByName(current, sections[j]);
                }
            }
            instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
            Assertions.assertEquals(counter, errorList.size());
        }
    }

    @Test
    public void validateLibraryServiceWithInvalidRequirementName()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Invalid name requirements
        fillRandomRequirementsWithBadName(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(createLMLibraryValidationErrorDTOArray(1));

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is not empty
        Assertions.assertFalse(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithNullRequirementName()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Fill requirements with null name
        fillRandomRequirementsWithNullName(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(createLMLibraryValidationErrorDTOArray(1));

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is not empty
        Assertions.assertFalse(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithInvalidRequirementType()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Invalid type requirements
        fillRandomRequirementsWithBadType(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(createLMLibraryValidationErrorDTOArray(1));

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is not empty
        Assertions.assertFalse(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithBlacklistedEnvironmentVariableName()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Invalid type requirements
        fillRandomRequirementsWithBlacklistedEnvironmentVariable(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(createLMLibraryValidationErrorDTOArray(EnvironmentVariableBlacklist.values().length));

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        Assertions.assertEquals(EnvironmentVariableBlacklist.values().length, errorList.size());
    }

    @Test
    public void validateLibraryServiceWithEnvironmentVariableNameStartingWithNovaReplace()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Invalid type requirements
        fillRandomRequirementWithEnvironmentVariableStartingWithNovaAppend(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(createLMLibraryValidationErrorDTOArray(1));

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        Assertions.assertEquals(1, errorList.size());
    }

    @Test
    public void validateLibraryServiceWithValidEnvironmentVariableName()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Valid envvar requirements
        fillRequirementsWithValidEnvironmentVariable(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(new LMLibraryValidationErrorDTO[0]);

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is empty
        Assertions.assertTrue(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithNullRequirementType()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Fill requirements with null type
        fillRandomRequirementsWithNullType(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(createLMLibraryValidationErrorDTOArray(1));

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is not empty
        Assertions.assertFalse(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithInvalidRequirementsRelation()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Fill requirements with null type
        fillRandomRequirementsWithRandomRelation(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(createLMLibraryValidationErrorDTOArray(1));

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is not empty
        Assertions.assertFalse(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithInvalidRequirementNovaBoolValue()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Fill requirements with null type
        fillRandomRequirementsWithRandomValue(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(createLMLibraryValidationErrorDTOArray(1));

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is not empty
        Assertions.assertFalse(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithRequirementNumericValue()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Fill requirements with random numeric value
        fillRandomRequirementsWithRandomNumericValue(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(new LMLibraryValidationErrorDTO[0]);

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is not empty
        Assertions.assertTrue(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithInvalidRequirementNumericValue()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Fill requirements with invalid numeric value
        fillRandomRequirementsWithInvalidNumericValue(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(createLMLibraryValidationErrorDTOArray(1));

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is not empty
        Assertions.assertFalse(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithInvalidRequirementNonNumericValue()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Fill requirements with invalid numeric value
        fillRandomRequirementsWithNonNumericValue(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(createLMLibraryValidationErrorDTOArray(1));

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is not empty
        Assertions.assertFalse(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithInvalidRequirementBadDecimalValue()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Fill requirements with invalid numeric value
        fillRandomRequirementsWithBadDecimalValue(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(createLMLibraryValidationErrorDTOArray(1));

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is not empty
        Assertions.assertFalse(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithInvalidConnectorValue()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Fill requirements with invalid connector value
        fillRandomRequirementsWithRandomConnectorValue(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(createLMLibraryValidationErrorDTOArray(1));

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is not empty
        Assertions.assertFalse(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithAllConnectorsValue()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Fill requirements with invalid connector value
        fillAllConnectorRequirements(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(new LMLibraryValidationErrorDTO[0]);

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is not empty
        Assertions.assertTrue(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithAllConnectorsWithBadValue()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Fill requirements with invalid connector value
        fillAllConnectorRequirementsWithBadValues(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(new LMLibraryValidationErrorDTO[0]);

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is not empty
        Assertions.assertEquals(1, errorList.size());
        Assertions.assertTrue(errorList.get(0).getMessage().contains(Constants.INVALID_CONNECTORS_REQUIREMENT_VALUE_MSG) &&
                errorList.get(0).getMessage().contains(Constants.INVALID_CONNECTOR_REQUIREMENT_ACTUAL_VALUE_MSG));
    }

    @Test
    public void validateLibraryServiceWithOneConnectorValue()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);
        fillRandomlyProperties(novaYml);
        // Fill requirements with invalid connector value
        fillOneConnectorRequirements(novaYml);
        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(new LMLibraryValidationErrorDTO[0]);

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);
        // Check if error list is not empty
        Assertions.assertTrue(errorList.isEmpty());
    }
    @Test
    public void validateLibraryServiceWith2ValidJvmParamRequirement()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);

        // Add requirements
        List<NovaYmlRequirement> requirements = new ArrayList<>(Constants.REQUIREMENT_NAME.values().length);
        requirements.add(generateJvmParameterlRequirement("-XX:+UnlockExperimentalVMOptions -XX:+UseZGC"));
        requirements.add(generateJvmParameterlRequirement("-XX:+UseStringDeduplication"));
        novaYml.setRequirements(requirements);

        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(new LMLibraryValidationErrorDTO[0]);
        when(jdkParameterRepository.existsByName("-XX:+UnlockExperimentalVMOptions -XX:+UseZGC")).thenReturn(true);
        when(jdkParameterRepository.existsByName("-XX:+UseStringDeduplication")).thenReturn(true);

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);

        // Check if error list is not empty
        System.out.println(errorList);
        Assertions.assertTrue(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithInvalidJvmParamRequirement()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);

        // Add requirement
        List<NovaYmlRequirement> requirements = new ArrayList<>();
        requirements.add(generateJvmParameterlRequirement("-XX:Invalid"));
        novaYml.setRequirements(requirements);

        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(new LMLibraryValidationErrorDTO[0]);
        when(jdkParameterRepository.existsByName("-XX:Invalid")).thenReturn(false);

        JdkParameter automatico = new JdkParameter();
        automatico.setName("Automático");
        JdkParameter shenandoah = new JdkParameter();
        shenandoah.setName("-XX:+UseShenandoahGC");
        JdkParameter zGC= new JdkParameter();
        zGC.setName("-XX:+UnlockExperimentalVMOptions -XX:+UseZGC");

        when(jdkParameterRepository.findAll()).thenReturn(List.of(automatico, shenandoah, zGC));

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);

        // Check if error list is not empty
        Assertions.assertFalse(errorList.isEmpty());
    }
    @Test
    public void validateLibraryServiceWithValidSupportedJdkRequirement()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);

        // Add SUPPORTED_JDK_VERSIONS requirement
        List<NovaYmlRequirement> requirements = new ArrayList<>();
        requirements.add(generateSupportedJdkRequirement("zulu11.*|amazon-corretto-11.0.11.9.1"));
        novaYml.setRequirements(requirements);
        novaYml.setJdkVersion("amazon-corretto-11.0.11.9.1");

        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(new LMLibraryValidationErrorDTO[0]);

        AllowedJdk zulu = new AllowedJdk();
        zulu.setJdk("zulu11.48.21");
        AllowedJdk corretto = new AllowedJdk();
        corretto.setJdk("amazon-corretto-11.0.11.9.1");

        List<AllowedJdk> allowedJdks = Arrays.asList(zulu, corretto);

        when(allowedJdkRepository.findAll()).thenReturn(allowedJdks);

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);

        // Check there are no errors
        Assertions.assertTrue(errorList.isEmpty());
    }

    @Test
    public void validateLibraryServiceWithInvalidSupportedJdkRequirement()
    {
        NovaYml novaYml = new NovaYml();
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // Create random nova.yml with invalid requirements.
        fillRandomlyServiceDefinition(novaYml, NovaServiceType.LIBRARY);

        // Add SUPPORTED_JDK_VERSIONS requirement
        List<NovaYmlRequirement> requirements = new ArrayList<>();
        requirements.add(generateSupportedJdkRequirement("another-jdk|zulu12.*"));
        novaYml.setRequirements(requirements);
        novaYml.setJdkVersion("amazon-corretto-11.0.11.9.1");

        // ServiceDto
        NewReleaseVersionServiceDto serviceDto = getServiceDtoFromNovayml(novaYml);
        // Validator
        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setNovaYml(novaYml);

        when(libMgrClient.validateNovaYmlRequirements(any())).thenReturn(new LMLibraryValidationErrorDTO[0]);

        AllowedJdk zulu = new AllowedJdk();
        zulu.setJdk("zulu11.48.21");
        AllowedJdk corretto = new AllowedJdk();
        corretto.setJdk("amazon-corretto-11.0.11.9.1");

        List<AllowedJdk> allowedJdks = Arrays.asList(zulu, corretto);

        when(allowedJdkRepository.findAll()).thenReturn(allowedJdks);

        instance.validateNovaLibraryNovaYml(validatorInputs, serviceDto, errorList);

        // Check there are 2 errors
        Assertions.assertEquals(errorList.size(), 3);
    }

    private NovaYmlRequirement generateJvmParameterlRequirement(String value)
    {
        NovaYmlRequirement novaYmlRequirement;
        novaYmlRequirement = new NovaYmlRequirement();
        novaYmlRequirement.setName(Constants.RESOURCE_REQUIREMENT_NAME.JVM_PARAMETER.name());
        novaYmlRequirement.setValue(value);
        novaYmlRequirement.setType(Constants.REQUIREMENT_TYPE.RESOURCE.toString());
        novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));
        return novaYmlRequirement;
    }

    private NovaYmlRequirement generateSupportedJdkRequirement(String value)
    {
        NovaYmlRequirement novaYmlRequirement;
        novaYmlRequirement = new NovaYmlRequirement();
        novaYmlRequirement.setName(Constants.INSTALLATION_REQUIREMENT_NAME.SUPPORTED_JDK_VERSIONS.name());
        novaYmlRequirement.setValue(value);
        novaYmlRequirement.setType(Constants.REQUIREMENT_TYPE.INSTALLATION.toString());
        novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));
        return novaYmlRequirement;
    }

    private void populateConnectorTypeRepositoryMock()
    {
        List<ConnectorType> connectorTypeList = new ArrayList<>();
        Arrays.stream(CONNECTORS_REQUIREMENT_VALUE.values()).forEach( ct -> connectorTypeList.add(
                new ConnectorType(ct.name(), ct.toString(), null)));
        when(connectorTypeRepository.findAll()).thenReturn(connectorTypeList);
    }

    /**
     * Crate a novaYml for test
     *
     * @param dto         dto with data
     * @param serviceType service type
     * @return NovaYml with basic info
     */
    private NovaYml getNovaYMLByNewReleaseVersionServiceDto(NewReleaseVersionServiceDto dto, ServiceType serviceType, List<String> libraries)
    {
        NovaYml novaYml = new NovaYml();

        novaYml.setServiceType(serviceType.getServiceType());
        novaYml.setLanguage(serviceType.getLanguage());
        novaYml.setLanguageVersion("1.0");
        novaYml.setApplicationName(dto.getFinalName());
        novaYml.setName(dto.getServiceName());
        novaYml.setNovaVersion("19.04");
        novaYml.setProjectName(dto.getNovaServiceName());
        novaYml.setUuaa(dto.getUuaa());
        novaYml.setValid(true);
        novaYml.setDescription(dto.getDescription());

        novaYml.setLibraries(libraries);

        return novaYml;
    }

    /**
     * Crate a pom.xml from nova.yml
     *
     * @param novaYml with data
     * @return PomXML with basic info
     */
    private PomXML getPomXMLByNovaYML(final NovaYml novaYml)
    {
        PomXML pom = new PomXML();

        pom.setGroupId("com.bbva." + novaYml.getUuaa());
        pom.setArtifactId(novaYml.getApplicationName());
        pom.setVersion(novaYml.getVersion());
        pom.setDescription(novaYml.getDescription());

        LibraryValidatorImpl.LibraryDef libDef;
        Dependency dependency;
        for (String fullName : novaYml.getLibraries())
        {
            libDef = LibraryValidatorImpl.LibraryDef.fromFullName(fullName);
            dependency = new Dependency();
            dependency.setGroupId("com.bbva." + libDef.getUuaa());
            dependency.setArtifactId(libDef.getServiceName());
            dependency.setVersion(libDef.getVersion());
            pom.addDependency(dependency);
        }

        return pom;
    }

    /**
     * Create Validator Inputs with files correctly
     *
     * @param novaYml novaYml based of validator inputs and other files
     * @return ValidatorInputs well configured
     */
    private ValidatorInputs createValidatorInputsByNovaYml(final NovaYml novaYml)
    {
        ValidatorInputs validatorInputs = new ValidatorInputs();
        ServiceType serviceType = ServiceType.getValueOf(novaYml.getServiceType());
        validatorInputs.setNovaYml(novaYml);

        //has pom?
        if (serviceType.isPomXml())
        {
            validatorInputs.setPom(getPomXMLByNovaYML(novaYml));
        }


        return validatorInputs;
    }

    /**
     * Given a novayml, this method fills randomly the list of libraries
     *
     * @param novaYml novaYml to fill
     */
    private void fillRandomlyRequirements(final NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(Constants.REQUIREMENT_NAME.values().length);

        NovaYmlRequirement novaYmlRequirement;
        String value;
        String type;
        for (Constants.REQUIREMENT_NAME req : Constants.REQUIREMENT_NAME.values())
        {
            value = "";
            type = "";
            novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(req.name());
            switch (req)
            {
                case CPU:
                    value = String.valueOf(RandomUtils.nextInt(1, 8));
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case MEMORY:
                    value = String.valueOf(1 << RandomUtils.nextInt(1, 20));
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case FILE_SYSTEM:
                    value = org.apache.commons.lang.math.RandomUtils.nextBoolean() ? "Y" : "N";
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case PRECONF:
                case USES_C:
                    value = org.apache.commons.lang.math.RandomUtils.nextBoolean() ? "Y" : "N";
                    type = Constants.REQUIREMENT_TYPE.INSTALLATION.toString();
                    break;
                case PORT:
                case PORT_RANGE:
                    value = String.valueOf(1 << RandomUtils.nextInt(10000, 20000));
                    type = Constants.REQUIREMENT_TYPE.INSTALLATION.toString();
                    break;
                case CONNECTORS:
                    value = generateRandomConnector();
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
            }
            novaYmlRequirement.setValue(value);
            novaYmlRequirement.setType(type);
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    private String generateRandomConnector()
    {
        List<CONNECTORS_REQUIREMENT_VALUE> connectors = Collections.unmodifiableList(Arrays.asList(
                CONNECTORS_REQUIREMENT_VALUE.TCP, CONNECTORS_REQUIREMENT_VALUE.BBDD, CONNECTORS_REQUIREMENT_VALUE.TIBCO,
                CONNECTORS_REQUIREMENT_VALUE.COLASMQ, CONNECTORS_REQUIREMENT_VALUE.FIX,
                CONNECTORS_REQUIREMENT_VALUE.MONGODB, CONNECTORS_REQUIREMENT_VALUE.HTTP, CONNECTORS_REQUIREMENT_VALUE.INTERNET,
                CONNECTORS_REQUIREMENT_VALUE.BIGDATA, CONNECTORS_REQUIREMENT_VALUE.BATCH));
        Random r = new Random();
        return connectors.get(r.nextInt(connectors.size())).toString();
    }

    /**
     * Fill random number of requirements with invalid name
     *
     * @param novaYml nova.yml
     */
    private void fillRandomRequirementsWithBadName(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(Constants.REQUIREMENT_NAME.values().length);
        for (int i = 0; i < RandomUtils.nextInt(5, 8); i++)
        {
            NovaYmlRequirement novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(RandomStringUtils.randomAlphanumeric(10).toUpperCase());
            novaYmlRequirement.setType(randomRequirementType());
            novaYmlRequirement.setValue(RandomStringUtils.randomAlphanumeric(5));
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    /**
     * Fill random number of requirements with null name
     *
     * @param novaYml nova.yml
     */
    private void fillRandomRequirementsWithNullName(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(Constants.REQUIREMENT_NAME.values().length);
        for (int i = 0; i < RandomUtils.nextInt(5, 8); i++)
        {
            NovaYmlRequirement novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(null);
            novaYmlRequirement.setType(randomRequirementType());
            novaYmlRequirement.setValue(RandomStringUtils.randomAlphanumeric(5));
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    /**
     * Fill random number of requirements with invalid type
     *
     * @param novaYml nova.yml
     */
    private void fillRandomRequirementsWithBadType(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(Constants.REQUIREMENT_NAME.values().length);
        for (int i = 0; i < RandomUtils.nextInt(5, 8); i++)
        {
            NovaYmlRequirement novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(randomRequirementName());
            novaYmlRequirement.setType(RandomStringUtils.randomAlphanumeric(10).toUpperCase());
            novaYmlRequirement.setValue(RandomStringUtils.randomAlphanumeric(5));
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    /**
     * Fill requirements with environment variable in blacklist
     *
     * @param novaYml nova.yml
     */
    private void fillRandomRequirementsWithBlacklistedEnvironmentVariable(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(EnvironmentVariableBlacklist.values().length);
        for (EnvironmentVariableBlacklist environmentVariableBlacklist : EnvironmentVariableBlacklist.values())
        {
            NovaYmlRequirement novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(environmentVariableBlacklist.name());
            novaYmlRequirement.setType(Constants.REQUIREMENT_TYPE.ENVIRONMENT_VARIABLE.name());
            novaYmlRequirement.setValue(RandomStringUtils.randomAlphanumeric(5));
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }
        novaYml.setRequirements(requirements);
    }

    /**
     * Fill requirements with environment variable starting with NOVA_APPEND_
     *
     * @param novaYml nova.yml
     */
    private void fillRandomRequirementWithEnvironmentVariableStartingWithNovaAppend(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(1);

        NovaYmlRequirement novaYmlRequirement = new NovaYmlRequirement();
        novaYmlRequirement.setName("NOVA_APPEND_PATH");
        novaYmlRequirement.setType(Constants.REQUIREMENT_TYPE.ENVIRONMENT_VARIABLE.name());
        novaYmlRequirement.setValue(RandomStringUtils.randomAlphanumeric(5));
        novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

        requirements.add(novaYmlRequirement);
        novaYml.setRequirements(requirements);
    }

    /**
     * Fill requirements with valid environment variable names
     *
     * @param novaYml nova.yml
     */
    private void fillRequirementsWithValidEnvironmentVariable(NovaYml novaYml)
    {
        String[] validEnvironmentVariables = new String[]{"PATH", "LD_LIBRARY_PATH"};
        List<NovaYmlRequirement> requirements = new ArrayList<>(EnvironmentVariableBlacklist.values().length);
        for (String validEnvironmentVariable : validEnvironmentVariables)
        {
            NovaYmlRequirement novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(validEnvironmentVariable);
            novaYmlRequirement.setType(Constants.REQUIREMENT_TYPE.ENVIRONMENT_VARIABLE.name());
            novaYmlRequirement.setValue(RandomStringUtils.randomAlphanumeric(5));
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }
        novaYml.setRequirements(requirements);
    }

    /**
     * Fill random number of requirements with null type
     *
     * @param novaYml nova.yml
     */
    private void fillRandomRequirementsWithNullType(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(Constants.REQUIREMENT_NAME.values().length);
        for (int i = 0; i < RandomUtils.nextInt(5, 8); i++)
        {
            NovaYmlRequirement novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(randomRequirementName());
            novaYmlRequirement.setType(null);
            novaYmlRequirement.setValue(RandomStringUtils.randomAlphanumeric(5));
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    /**
     * Fill random number of requirements with null type
     *
     * @param novaYml nova.yml
     */
    private void fillRandomRequirementsWithRandomRelation(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(Constants.REQUIREMENT_NAME.values().length);
        for (int i = 0; i < 100; i++)
        {
            NovaYmlRequirement novaYmlRequirement = new NovaYmlRequirement();
            String reqType = randomRequirementType();
            String reqName = randomRequirementName();
            novaYmlRequirement.setName(reqName);
            novaYmlRequirement.setType(reqType);
            switch (Constants.REQUIREMENT_NAME.valueOf(reqName))
            {
                case CPU:
                    novaYmlRequirement.setValue(String.valueOf(RandomUtils.nextInt(1, 8)));
                    break;
                case MEMORY:
                    novaYmlRequirement.setValue(String.valueOf(RandomUtils.nextInt(1, 20)));
                    break;
                case FILE_SYSTEM:
                case PRECONF:
                case USES_C:
                    novaYmlRequirement.setValue(org.apache.commons.lang.math.RandomUtils.nextBoolean() ? "Y" : "N");
                    break;
                case CONNECTORS:
                    novaYmlRequirement.setValue(generateRandomConnector());
                    break;
            }

            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    /**
     * Fill random number of requirements with invalid value
     *
     * @param novaYml nova.yml
     */
    private void fillRandomRequirementsWithRandomValue(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(Constants.REQUIREMENT_NAME.values().length);

        NovaYmlRequirement novaYmlRequirement;
        String value;
        String type;
        for (Constants.REQUIREMENT_NAME req : Constants.REQUIREMENT_NAME.values())
        {
            value = "";
            type = "";
            novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(req.name());
            switch (req)
            {
                case CPU:
                case MEMORY:
                    value = String.valueOf(1 << RandomUtils.nextInt(1, 20));
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case FILE_SYSTEM:
                case CONNECTORS:
                    value = generateRandomConnector()+RandomStringUtils.randomAlphanumeric(1);
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case PRECONF:
                case USES_C:
                    value = RandomStringUtils.randomAlphanumeric(1);
                    type = Constants.REQUIREMENT_TYPE.INSTALLATION.toString();
                    break;
            }
            novaYmlRequirement.setValue(value);
            novaYmlRequirement.setType(type);
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    private void fillRandomRequirementsWithRandomConnectorValue(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(Constants.REQUIREMENT_NAME.values().length);

        NovaYmlRequirement novaYmlRequirement;
        String value;
        String type;
        for (Constants.REQUIREMENT_NAME req : Constants.REQUIREMENT_NAME.values())
        {
            value = "";
            type = "";
            novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(req.name());
            switch (req)
            {
                case CPU:
                case MEMORY:
                    value = String.valueOf(RandomUtils.nextInt(1, 20));
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case FILE_SYSTEM:
                case CONNECTORS:
                    value = RandomStringUtils.randomAlphanumeric(10);
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case PRECONF:
                case USES_C:
                    value = "Y";
                    type = Constants.REQUIREMENT_TYPE.INSTALLATION.toString();
                    break;
            }
            novaYmlRequirement.setValue(value);
            novaYmlRequirement.setType(type);
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    private void fillRandomRequirementsWithInvalidNumericValue(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(Constants.REQUIREMENT_NAME.values().length);

        NovaYmlRequirement novaYmlRequirement;
        String value;
        String type;
        for (Constants.REQUIREMENT_NAME req : Constants.REQUIREMENT_NAME.values())
        {
            value = "";
            type = "";
            novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(req.name());
            switch (req)
            {
                case CPU:
                case MEMORY:
                    value = String.valueOf(RandomUtils.nextInt(1, 20) * (-1));
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case FILE_SYSTEM:
                case CONNECTORS:
                    value = generateRandomConnector();
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case PRECONF:
                case USES_C:
                    value = "Y";
                    type = Constants.REQUIREMENT_TYPE.INSTALLATION.toString();
                    break;
            }
            novaYmlRequirement.setValue(value);
            novaYmlRequirement.setType(type);
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    /**
     * Fill random number of requirements with null type
     *
     * @param novaYml nova.yml
     */
    private void fillRandomRequirementsWithRandomNumericValue(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(Constants.REQUIREMENT_NAME.values().length);

        NovaYmlRequirement novaYmlRequirement;
        String value;
        String type;
        for (Constants.REQUIREMENT_NAME req : Constants.REQUIREMENT_NAME.values())
        {
            value = "";
            type = "";
            novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(req.name());
            switch (req)
            {
                case CPU:
                    value = "0.5";
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case MEMORY:
                    value = String.valueOf(RandomUtils.nextInt(1, 20));
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case FILE_SYSTEM:
                    value = org.apache.commons.lang.math.RandomUtils.nextBoolean() ? "Y" : "N";
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case CONNECTORS:
                    value = generateRandomConnector();
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case PRECONF:
                case USES_C:
                    value = org.apache.commons.lang.math.RandomUtils.nextBoolean() ? "Y" : "N";
                    type = Constants.REQUIREMENT_TYPE.INSTALLATION.toString();
                    break;
            }
            novaYmlRequirement.setValue(value);
            novaYmlRequirement.setType(type);
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    private void fillRandomRequirementsWithNonNumericValue(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(Constants.REQUIREMENT_NAME.values().length);

        NovaYmlRequirement novaYmlRequirement;
        String value;
        String type;
        for (Constants.REQUIREMENT_NAME req : Constants.REQUIREMENT_NAME.values())
        {
            value = "";
            type = "";
            novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(req.name());
            switch (req)
            {
                case CPU:
                case MEMORY:
                    value = RandomStringUtils.randomAlphanumeric(10);
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case CONNECTORS:
                    value = generateRandomConnector();
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case FILE_SYSTEM:
                case PRECONF:
                case USES_C:
                    value = org.apache.commons.lang.math.RandomUtils.nextBoolean() ? "Y" : "N";
                    type = Constants.REQUIREMENT_TYPE.INSTALLATION.toString();
                    break;
            }
            novaYmlRequirement.setValue(value);
            novaYmlRequirement.setType(type);
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    private void fillRandomRequirementsWithBadDecimalValue(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(Constants.REQUIREMENT_NAME.values().length);

        NovaYmlRequirement novaYmlRequirement;
        String value;
        String type;
        for (Constants.REQUIREMENT_NAME req : Constants.REQUIREMENT_NAME.values())
        {
            value = "";
            type = "";
            novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(req.name());
            switch (req)
            {
                case CPU:
                case MEMORY:
                    int intChunk = RandomUtils.nextInt(1, 100);
                    int decChunk = RandomUtils.nextInt(1, 9);
                    value = intChunk + "." + decChunk;
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case FILE_SYSTEM:
                    value = org.apache.commons.lang.math.RandomUtils.nextBoolean() ? "Y" : "N";
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case CONNECTORS:
                    value = generateRandomConnector();
                    type = Constants.REQUIREMENT_TYPE.RESOURCE.toString();
                    break;
                case PRECONF:
                case USES_C:
                    value = org.apache.commons.lang.math.RandomUtils.nextBoolean() ? "Y" : "N";
                    type = Constants.REQUIREMENT_TYPE.INSTALLATION.toString();
                    break;
            }
            novaYmlRequirement.setValue(value);
            novaYmlRequirement.setType(type);
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));

            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    /**
     * Generate random valid requirement name
     *
     * @return Random requirement name
     */
    private String randomRequirementName()
    {
        List<Constants.REQUIREMENT_NAME> names = Collections.unmodifiableList(Arrays.asList(Constants.REQUIREMENT_NAME.CONNECTORS, Constants.REQUIREMENT_NAME.CPU, Constants.REQUIREMENT_NAME.FILE_SYSTEM, Constants.REQUIREMENT_NAME.MEMORY,
                Constants.REQUIREMENT_NAME.PRECONF, Constants.REQUIREMENT_NAME.USES_C));
        Random r = new Random();
        return names.get(r.nextInt(names.size())).toString();
    }

    /**
     * Generate random valid requirement type
     *
     * @return Random requirement type
     */
    private String randomRequirementType()
    {
        List<Constants.REQUIREMENT_TYPE> types = Collections.unmodifiableList(Arrays.asList(Constants.REQUIREMENT_TYPE.RESOURCE, Constants.REQUIREMENT_TYPE.INSTALLATION));
        Random r = new Random();
        return types.get(r.nextInt(types.size())).toString();
    }

    private void fillRandomlySectionByName(NovaYml novaYml, String section)
    {
        switch (section)
        {
            case Constants.BUILD:
                //build is a list of string...
                novaYml.setBuild(Collections.singletonList(RandomStringUtils.randomAlphanumeric(10)));
                break;
            case Constants.DEPENDENCIES_NAME:
                //dependencies is a list of string...
                novaYml.setDependencies(Collections.singletonList(RandomStringUtils.randomAlphanumeric(10)));
                break;
            case Constants.MACHINES:
                //Machines is a list of string...
                novaYml.setMachines(Collections.singletonList(RandomStringUtils.randomAlphanumeric(10)));
                break;
            case Constants.PORTS:
                fillRandomlyPorts(novaYml);
                break;
            case Constants.CONSUMED:
                fillRandomlyConsumed(novaYml);
                break;
            case Constants.SERVED:
                fillRandomlyServed(novaYml);
                break;
            case Constants.LIBRARIES:
                fillRandomlyLibraries(novaYml);
                break;
            case Constants.CONTEXT_PARAMS:
                fillRandomlyContextParam(novaYml);
                break;
            case Constants.INPUT_PARAMS:
                fillRandomlyInputParams(novaYml);
                break;
            case Constants.OUTPUT_PARAMS:
                fillRandomlyOutputParams(novaYml);
                break;
            case Constants.REQUIREMENTS:
                fillRandomlyRequirements(novaYml);
                break;
            default:
                break;
        }
    }

    /**
     * Given a novayml, this method fills randomly the list of properties
     *
     * @param novaYml novaYml to fill
     */
    private void fillRandomlyProperties(final NovaYml novaYml)
    {
        int size = RandomUtils.nextInt(1, 10);
        List<NovaYamlProperty> properties = new ArrayList<>(size);

        NovaYamlProperty property;
        for (int i = 0; i < size; i++)
        {
            property = new NovaYamlProperty();
            property.setName(RandomStringUtils.randomAlphanumeric(10));
            property.setType(PROPERTY_TYPE[RandomUtils.nextInt(0, PROPERTY_TYPE.length)]);
            property.setEncrypt(RandomUtils.nextInt(0, 1) == 1);
            property.setManagement(PROPERTY_MANAGEMENT[RandomUtils.nextInt(1, PROPERTY_MANAGEMENT.length)]);
            property.setScope(PROPERTY_SCOPE[RandomUtils.nextInt(0, PROPERTY_SCOPE.length)]);

            properties.add(property);
        }

        novaYml.setProperties(properties);
    }


    /**
     * Given a novayml, this method fills randomly the list of libraries
     *
     * @param novaYml novaYml to fill
     */
    private void fillRandomlyLibraries(final NovaYml novaYml)
    {
        int size = RandomUtils.nextInt(1, 10);
        List<String> libraries = new ArrayList<>(size);

        String uuaa;
        String release;
        String service;
        String version;
        for (int i = 0; i < size; i++)
        {
            uuaa = RandomStringUtils.random(4, true, false);
            release = RandomStringUtils.randomAlphabetic(10);
            service = RandomStringUtils.randomAlphabetic(10);
            version = "1.0." + RandomUtils.nextInt(0, 10);
            libraries.add(uuaa + '-' + release + '-' + service + '-' + version);
        }

        novaYml.setLibraries(libraries);
    }

    private void fillRandomlyPorts(NovaYml novaYml)
    {
        int size = RandomUtils.nextInt(1, 10);
        List<NovaYmlPort> ports = new ArrayList<>(size);

        NovaYmlPort port;
        for (int i = 0; i < size; i++)
        {
            port = new NovaYmlPort();
            port.setName(RandomStringUtils.randomAlphanumeric(10));
            port.setInsidePort(RandomUtils.nextInt(10000, 35000));
            port.setOutsidePort(RandomUtils.nextInt(10000, 35000));
            port.setType(Constants.PORT_TYPE.values()[RandomUtils.nextInt(0, Constants.PORT_TYPE.values().length)].name());
            ports.add(port);
        }

        novaYml.setPorts(ports);
    }

    private void fillRandomlyConsumed(NovaYml novaYml)
    {
        int size = RandomUtils.nextInt(1, 10);
        List<NovaYmlApi> apis = new ArrayList<>(size);

        NovaYmlApi api;
        for (int i = 0; i < size; i++)
        {
            api = new NovaYmlApi();
            api.setApi(RandomStringUtils.randomAlphanumeric(10));
            api.setConsumedApi(Collections.singletonList(RandomStringUtils.randomAlphanumeric(10)));
            apis.add(api);
        }

        novaYml.setApiConsumed(apis);
    }

    private void fillRandomlyServed(NovaYml novaYml)
    {
        int size = RandomUtils.nextInt(1, 10);
        List<NovaYmlApi> apis = new ArrayList<>(size);

        NovaYmlApi api;
        for (int i = 0; i < size; i++)
        {
            api = new NovaYmlApi();
            api.setApi(RandomStringUtils.randomAlphanumeric(10));
            api.setConsumedApi(Collections.singletonList(RandomStringUtils.randomAlphanumeric(10)));
            apis.add(api);
        }

        novaYml.setApiServed(apis);
    }

    private void fillRandomlyContextParam(NovaYml novaYml)
    {
        int size = RandomUtils.nextInt(1, 10);
        List<NovaYamlContextParam> params = new ArrayList<>(size);

        NovaYamlContextParam param;
        for (int i = 0; i < size; i++)
        {
            param = new NovaYamlContextParam();
            param.setName(RandomStringUtils.randomAlphanumeric(5));
            param.setType(Constants.PARAM_TYPE.values()[RandomUtils.nextInt(0, Constants.PARAM_TYPE.values().length)].name());
            param.setDefaultValue(RandomStringUtils.randomAlphanumeric(10));
            param.setDescription(RandomStringUtils.randomAlphanumeric(10));
            params.add(param);
        }

        novaYml.setContextParams(params);
    }

    private void fillRandomlyInputParams(NovaYml novaYml)
    {
        int size = RandomUtils.nextInt(1, 10);
        List<NovaYamlInputParams> params = new ArrayList<>(size);

        NovaYamlInputParams param;
        for (int i = 0; i < size; i++)
        {
            param = new NovaYamlInputParams();
            param.setName(RandomStringUtils.randomAlphanumeric(5));
            param.setType(Constants.PARAM_TYPE.values()[RandomUtils.nextInt(0, Constants.PARAM_TYPE.values().length)].name());
            param.setMandatory(RandomUtils.nextInt(0, 2) != 0);
            param.setDescription(RandomStringUtils.randomAlphanumeric(10));
            params.add(param);
        }

        novaYml.setInputParams(params);

    }

    private void fillRandomlyOutputParams(NovaYml novaYml)
    {
        int size = RandomUtils.nextInt(1, 10);
        List<NovaYamlOutputParams> params = new ArrayList<>(size);

        NovaYamlOutputParams param;
        for (int i = 0; i < size; i++)
        {
            param = new NovaYamlOutputParams();
            param.setName(RandomStringUtils.randomAlphanumeric(5));
            param.setType(Constants.PARAM_TYPE.values()[RandomUtils.nextInt(0, Constants.PARAM_TYPE.values().length)].name());
            param.setMandatory(RandomUtils.nextInt(0, 2) != 0);
            param.setDescription(RandomStringUtils.randomAlphanumeric(10));
            params.add(param);
        }

        novaYml.setOutputParams(params);
    }

    private NovaYml copyNovaYml(NovaYml novaYml)
    {
        NovaYml copy = new NovaYml();
        copy.setUuaa(novaYml.getUuaa());
        copy.setProjectName(novaYml.getProjectName());
        copy.setName(novaYml.getName());
        copy.setDescription(novaYml.getDescription());
        copy.setVersion(novaYml.getVersion());
        copy.setServiceType(novaYml.getServiceType());
        copy.setLanguage(novaYml.getLanguage());
        copy.setLanguageVersion(novaYml.getLanguageVersion());
        copy.setNovaVersion(novaYml.getNovaVersion());
        return copy;
    }

    /**
     * Given a novayml, this method fills randomly the definition of the service
     *
     * @param novaYml novaYml to fill
     */
    private void fillRandomlyServiceDefinition(final NovaYml novaYml, final NovaServiceType nst)
    {
        novaYml.setUuaa(RandomStringUtils.random(4, true, false));
        novaYml.setProjectName(RandomStringUtils.randomAlphanumeric(15));
        novaYml.setName(RandomStringUtils.randomAlphanumeric(15));
        novaYml.setDescription(RandomStringUtils.randomAlphanumeric(50));
        novaYml.setVersion("1.0." + RandomUtils.nextInt(0, 100));

        ServiceType type = NovaServiceType.getRandomServiceTypeBy(nst);
        novaYml.setServiceType(type.getServiceType());
        novaYml.setLanguage(type.getLanguage());
        novaYml.setLanguageVersion("1.0." + RandomUtils.nextInt(0, 100));
        novaYml.setNovaVersion("1.0." + RandomUtils.nextInt(0, 100));
    }

    private NewReleaseVersionServiceDto getServiceDtoFromNovayml(NovaYml novaYml)
    {
        NewReleaseVersionServiceDto dto = new NewReleaseVersionServiceDto();

        dto.setUuaa(novaYml.getUuaa());
        dto.setServiceType(novaYml.getServiceType());
        dto.setServiceName(novaYml.getName());
        dto.setIsService(true);
        dto.setArtifactId(novaYml.getName());
        dto.setGroupId("com.bbva." + novaYml.getUuaa());
        dto.setNovaVersion(novaYml.getNovaVersion());
        dto.setVersion(novaYml.getVersion());

        if (novaYml.getServiceType().contains("LIBRARY"))
        {
            dto.setFinalName(novaYml.getUuaa() + '-' + "release" + '-' + novaYml.getName() + '-' + novaYml.getVersion());
        }
        else
        {
            dto.setFinalName(novaYml.getApplicationName());
        }
        return dto;
    }

    /**
     * types of service  allowed by nova.yml
     */
    private enum NovaServiceType
    {
        API, BATCH, CDN, DAEMON, LIBRARY;

        /**
         * Allowed Service Types for API Rest
         */
        public static final ServiceType[] SERVICE_TYPES_BY_API_REST = new ServiceType[]{ServiceType.API_REST_JAVA_SPRING_BOOT, ServiceType.API_JAVA_SPRING_BOOT, ServiceType.API_REST_NODE_JS_EXPRESS, ServiceType.API_REST_PYTHON_FLASK};
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
                    randoms = SERVICE_TYPES_BY_API_REST;
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

    private LMLibraryValidationErrorDTO[] createLMLibraryValidationErrorDTOArray(int size)
    {
        LMLibraryValidationErrorDTO[] errors = new LMLibraryValidationErrorDTO[size];
        for(int i = 0; i < size; i++)
        {
            LMLibraryValidationErrorDTO error = new LMLibraryValidationErrorDTO();
            error.setCode("code");
            error.setMessage("message");
            errors[i] = error;
        }
        return errors;
    }

    /**
     * Fill the novaYml with all the connector requirements
     *
     * @param novaYml nova.yml
     */
    private void fillAllConnectorRequirements(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(CONNECTORS_REQUIREMENT_VALUE.values().length);

        NovaYmlRequirement novaYmlRequirement;
        for (CONNECTORS_REQUIREMENT_VALUE requirementValue : CONNECTORS_REQUIREMENT_VALUE.values())
        {
            novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(Constants.REQUIREMENT_NAME.CONNECTORS.name());
            novaYmlRequirement.setValue(requirementValue.name());
            novaYmlRequirement.setType(Constants.REQUIREMENT_TYPE.RESOURCE.toString());
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));
            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    /**
     * Fill the novaYml with all the connector requirements
     *
     * @param novaYml nova.yml
     */
    private void fillAllConnectorRequirementsWithBadValues(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(CONNECTORS_REQUIREMENT_VALUE.values().length);

        NovaYmlRequirement novaYmlRequirement;
        for (CONNECTORS_REQUIREMENT_VALUE requirementValue : CONNECTORS_REQUIREMENT_VALUE.values())
        {
            novaYmlRequirement = new NovaYmlRequirement();
            novaYmlRequirement.setName(Constants.REQUIREMENT_NAME.CONNECTORS.name());
            novaYmlRequirement.setValue(requirementValue.name()+RandomStringUtils.randomAlphanumeric(2));
            novaYmlRequirement.setType(Constants.REQUIREMENT_TYPE.RESOURCE.toString());
            novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));
            requirements.add(novaYmlRequirement);
        }

        novaYml.setRequirements(requirements);
    }

    /**
     * Fill the novaYml with all the connector requirements
     *
     * @param novaYml nova.yml
     */
    private void fillOneConnectorRequirements(NovaYml novaYml)
    {
        List<NovaYmlRequirement> requirements = new ArrayList<>(CONNECTORS_REQUIREMENT_VALUE.values().length);

        NovaYmlRequirement novaYmlRequirement;

        novaYmlRequirement = new NovaYmlRequirement();
        novaYmlRequirement.setName(Constants.REQUIREMENT_NAME.CONNECTORS.name());
        novaYmlRequirement.setValue(generateRandomConnector());
        novaYmlRequirement.setType(Constants.REQUIREMENT_TYPE.RESOURCE.toString());
        novaYmlRequirement.setDescription(RandomStringUtils.randomAlphanumeric(20));
        requirements.add(novaYmlRequirement);

        novaYml.setRequirements(requirements);
    }
}
