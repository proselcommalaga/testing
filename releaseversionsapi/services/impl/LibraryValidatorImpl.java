package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryNovaYmlRequirementDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryValidationErrorDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.config.enumerates.ManagementType;
import com.bbva.enoa.datamodel.model.config.enumerates.ScopeType;
import com.bbva.enoa.datamodel.model.connector.entities.ConnectorType;
import com.bbva.enoa.datamodel.model.release.entities.AllowedJdk;
import com.bbva.enoa.datamodel.model.release.entities.JdkParameter;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.AllowedJdkRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ConnectorTypeRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.JdkParameterRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionServiceRepository;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.ILibraryManagerClient;
import com.bbva.enoa.platformservices.coreservice.librarymanagerapi.exceptions.LibraryManagerError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYamlProperty;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYmlRequirement;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.PomXML;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.ILibraryValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ComparisonUtils;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ErrorListUtils;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ProjectFileReader;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.google.common.base.Strings;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.maven.model.Dependency;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.INSTALLATION_REQUIREMENT_NAME;

/**
 * Library Validator
 */
@Service
@Slf4j
public class LibraryValidatorImpl implements ILibraryValidator
{
    /**
     * Set of ServiceType allowed to declare use of nova libraries
     */
    private static final EnumSet<ServiceType> SERVICE_TYPES_WHITELIST = EnumSet
            .of(ServiceType.API_REST_JAVA_SPRING_BOOT, ServiceType.API_JAVA_SPRING_BOOT, ServiceType.API_REST_NODE_JS_EXPRESS, ServiceType.DAEMON_JAVA_SPRING_BOOT, ServiceType.NODE,
                    ServiceType.BATCH_JAVA_SPRING_CLOUD_TASK, ServiceType.BATCH_JAVA_SPRING_BATCH, ServiceType.API_REST_PYTHON_FLASK, ServiceType.BATCH_PYTHON,
                    ServiceType.FRONTCAT_JAVA_SPRING_MVC);

    /**
     * Client of LibraryManager
     */
    private final ILibraryManagerClient libraryManagerClient;

    /**
     * Release version service repository
     */
    private final ReleaseVersionServiceRepository releaseVersionServiceRepository;

    /**
     * Connector Type Repository
     */
    private final ConnectorTypeRepository connectorTypeRepository;

    /**
     * Allowed JDKs repository
     */
    private final AllowedJdkRepository allowedJdkRepository;

    /**
     * JDK parameter repository
     */
    private final JdkParameterRepository jdkParameterRepository;

    /**
     * Constructor by params
     *
     * @param libMgrClient                    client for LibraryManager
     * @param releaseVersionServiceRepository releaseVersionServiceRepository
     * @param connectorTypeRepository         connector type repository
     * @param allowedJdkRepository            allowedJdkRepository
     */
    @Autowired
    public LibraryValidatorImpl(final ILibraryManagerClient libMgrClient,
                                final ReleaseVersionServiceRepository releaseVersionServiceRepository,
                                final ConnectorTypeRepository connectorTypeRepository,
                                final AllowedJdkRepository allowedJdkRepository,
                                final JdkParameterRepository jdkParameterRepository)
    {
        this.libraryManagerClient = libMgrClient;
        this.releaseVersionServiceRepository = releaseVersionServiceRepository;
        this.connectorTypeRepository = connectorTypeRepository;
        this.allowedJdkRepository = allowedJdkRepository;
        this.jdkParameterRepository = jdkParameterRepository;
    }

    @Override
    public void validateLibrariesFromNovaYml(final ValidatorInputs validatorInputs,
                                             final NewReleaseVersionServiceDto newReleaseVersionServiceDto, final String tag, final String releaseVersionName,
                                             final List<ValidationErrorDto> errorList)
    {
        final String serviceName = newReleaseVersionServiceDto.getServiceName();
        NovaYml novaYml = validatorInputs.getNovaYml();
        if (novaYml.getLibraries() == null || novaYml.getLibraries().isEmpty())
        {
            log.debug("[LibraryValidatorImpl] -> [validateLibrariesFromNovaYml]: No libraries declared for {}", serviceName);
            return;
        }

        ServiceType type = ServiceType.getValueOf(novaYml.getServiceType());
        if (!SERVICE_TYPES_WHITELIST.contains(type))
        {
            log.warn("[LibraryValidatorImpl] -> [validateLibrariesFromNovaYml]: ServiceType {} is not allowed to use nova libraries",
                    newReleaseVersionServiceDto.getServiceType());
            ErrorListUtils.addError(errorList, serviceName, Constants.LIBRARY_SERVICE_TYPE_NOT_ALLOWED_LIBS,
                    "Service type is not allowed to use nova libraries");
            return;
        }

        try
        {
            LibraryDef libDef;
            for (String fullName : novaYml.getLibraries())
            {
                libDef = LibraryDef.fromFullName(fullName);
                boolean existsLibrary = validateLibraryExistence(libDef, serviceName, errorList);
                validateLibraryByDescriptorFile(libDef, serviceName, type, validatorInputs, errorList);
                if (existsLibrary)
                {
                    validateLibraryRequirements(fullName, serviceName, validatorInputs, errorList);
                }
            }
        }
        catch (NovaException e)
        {
            log.warn("[LibraryValidatorImpl] -> [validateLibrariesFromNovaYml]: Error validating list of libraries into nova.yml {} : {}",
                    novaYml.getLibraries(), e.getMessage());
            ErrorListUtils.addError(errorList, serviceName, e.getNovaError().getErrorCode(), e.getMessage());
        }
    }

    @Override
    public void validateNovaLibraryNovaYml(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList)
    {
        // Properties
        this.validateNovaYmlProperties(validatorInputs, dto, errorList);

        // Requirements
        validateNovaYmlRequirements(validatorInputs.getNovaYml().getRequirements(), dto.getServiceName(), errorList);

        // Validate connector requirements
        validateSupportedConnectors(validatorInputs.getNovaYml().getRequirements(), dto.getServiceName(), errorList);

        // Validate supported JDK requirement
        validateSupportedJDKs(validatorInputs.getNovaYml().getRequirements(), validatorInputs.getNovaYml().getJdkVersion(), dto.getServiceName(), errorList);

        // Validate JVM parameter requirements
        validateSupportedJvmParameters(validatorInputs.getNovaYml().getRequirements(), dto.getServiceName(), errorList);


        // Check invalid sections in nova.yml for libraries
        NovaYml nyml = validatorInputs.getNovaYml();

        //Ports  section is not allowed in a library service
        this.validateSectionIsNotDeclaredInNovaYml(nyml.getPorts(), dto.getServiceName(), nyml.getServiceType(), Constants.PORTS, errorList);
        //Build section is not allowed in a library service
        this.validateSectionIsNotDeclaredInNovaYml(nyml.getBuild(), dto.getServiceName(), nyml.getServiceType(), Constants.BUILD, errorList);
        //Machines section is not allowed in a library service
        this.validateSectionIsNotDeclaredInNovaYml(nyml.getMachines(), dto.getServiceName(), nyml.getServiceType(), Constants.MACHINES, errorList);
        //served apis section is not allowed in a library service -> for the moment libraries does not serve apis
        this.validateSectionIsNotDeclaredInNovaYml(nyml.getApiServed(), dto.getServiceName(), nyml.getServiceType(), Constants.SERVED, errorList);
        //consumed apis section is not allowed in a library service -> for the moment libraries does not consume any api
        this.validateSectionIsNotDeclaredInNovaYml(nyml.getApiConsumed(), dto.getServiceName(), nyml.getServiceType(), Constants.CONSUMED, errorList);
        //libraries section is not allowed in a library service -> for the moment a library can not use another library
        this.validateSectionIsNotDeclaredInNovaYml(nyml.getLibraries(), dto.getServiceName(), nyml.getServiceType(), Constants.LIBRARIES, errorList);
        //context params section is not allowed in a library service
        this.validateSectionIsNotDeclaredInNovaYml(nyml.getContextParams(), dto.getServiceName(), nyml.getServiceType(), Constants.CONTEXT_PARAMS, errorList);
        //inputs params section is not allowed in a library service
        this.validateSectionIsNotDeclaredInNovaYml(nyml.getInputParams(), dto.getServiceName(), nyml.getServiceType(), Constants.INPUT_PARAMS, errorList);
        //output section is not allowed in a library service
        this.validateSectionIsNotDeclaredInNovaYml(nyml.getOutputParams(), dto.getServiceName(), nyml.getServiceType(), Constants.OUTPUT_PARAMS, errorList);
    }

    @Override
    public void validateNovaLibraryByPomXml(final ValidatorInputs validatorInputs, final NewReleaseVersionServiceDto dto, final List<ValidationErrorDto> errorList)
    {
        NovaYml nyml = validatorInputs.getNovaYml();
        PomXML pomXML = validatorInputs.getPom();

        //name
        if (!StringUtils.equals(nyml.getName(), pomXML.getArtifactId()))
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.WRONG_LIBRARY_NAME_CODE, Constants.WRONG_LIBRARY_NAME_MSG);
        }

        //version
        if (!StringUtils.equals(nyml.getVersion(), pomXML.getVersion()))
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.WRONG_LIBRARY_VERSION_CODE, Constants.WRONG_LIBRARY_VERSION_MSG);
        }

        //uuaa
        if (!StringUtils.startsWithIgnoreCase(pomXML.getGroupId(),Constants.GROUP_ID_PREFIX + nyml.getUuaa()))
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_POM_GROUP_ID, Constants.INVALID_POM_GROUP_ID_MSG);
        }
    }

    @Override
    @Transactional
    public void validateNovaLibraryNewVersion(ValidatorInputs currentValidatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList)
    {
        String fullname = dto.getFinalName();

        LMLibraryEnvironmentsDTO libraryEnvironmentsDTO = this.libraryManagerClient.getLibraryEnvironments(fullname);

        if (!StringUtils.isEmpty(libraryEnvironmentsDTO.getFullName()))
        {
            final ValidatorInputs validatorInputs = new ValidatorInputs();
            ReleaseVersionService releaseVersionService = this.releaseVersionServiceRepository.findById(libraryEnvironmentsDTO.getReleaseVersionServiceId()).orElse(null);

            if (releaseVersionService != null)
            {
                ProjectFileReader.scanNovaYml(releaseVersionService.getFolder(), releaseVersionService.getNovaYml().getContents().getBytes(), validatorInputs);
                NovaYml novaYml = validatorInputs.getNovaYml();
                NovaYml currentNovaYml = currentValidatorInputs.getNovaYml();

                if (!novaYml.getRequirements().equals(currentNovaYml.getRequirements()))
                {
                    ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.WRONG_LIBRARY_REQUIREMENTS, Constants.WRONG_LIBRARY_REQUIREMENTS_MSG);
                }

                if (!novaYml.getProperties().equals(currentNovaYml.getProperties()))
                {
                    ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.WRONG_LIBRARY_PROPERTIES, Constants.WRONG_LIBRARY_PROPERTIES_MSG);
                }
            }
        }
    }

    /**
     * Validate that all the requirements configured in a nova.yml are valid
     *
     * @param requirements requirements in the nova.yml
     * @param serviceName Service name
     * @param errorList error list
     */
    private void validateNovaYmlRequirements(List<NovaYmlRequirement> requirements, String serviceName, List<ValidationErrorDto> errorList)
    {
        // Create the DTO to call libraryManager
        LMLibraryNovaYmlRequirementDTO[] requirementsDTO = requirements.stream().map(r -> {
            LMLibraryNovaYmlRequirementDTO requirementDTO = new LMLibraryNovaYmlRequirementDTO();
            requirementDTO.setName(r.getName());
            requirementDTO.setDescription(r.getDescription());
            requirementDTO.setServiceName(serviceName);
            requirementDTO.setType(r.getType());
            requirementDTO.setValue(r.getValue());
            return requirementDTO;
        }).toArray(LMLibraryNovaYmlRequirementDTO[]::new);

        // Call to library Manager to validate requirements
        LMLibraryValidationErrorDTO[] errorLibraryManagerList = this.libraryManagerClient.
                validateNovaYmlRequirements(requirementsDTO);

        // Add library errors to errorList
        Arrays.stream(errorLibraryManagerList).forEach(el -> {
            ValidationErrorDto validationErrorDto = new ValidationErrorDto();
            validationErrorDto.setCode(el.getCode());
            validationErrorDto.setMessage(el.getMessage());
            errorList.add(validationErrorDto);
        });
    }

    /**
     * Method that validates value for connectors requirement
     *
     * @param requirements     Library requirements list values
     * @param serviceName      service name.
     * @param errorList        Generated errors list.
     */
    private void validateSupportedConnectors(List<NovaYmlRequirement> requirements, String serviceName, List<ValidationErrorDto> errorList)
    {
        //Get all the requirements type RESOURCE and name CONNECTORS to validate them
        List<String> connectorRequirementValues = requirements.stream().
                filter(r -> EnumUtils.isValidEnum(Constants.REQUIREMENT_TYPE.class, r.getType()) &&
                        Constants.REQUIREMENT_TYPE.RESOURCE == Constants.REQUIREMENT_TYPE.valueOf(r.getType())).
                filter(r-> EnumUtils.isValidEnum(Constants.REQUIREMENT_NAME.class, r.getName()) &&
                        Constants.REQUIREMENT_NAME.CONNECTORS == Constants.REQUIREMENT_NAME.valueOf(r.getName())).
                map(NovaYmlRequirement::getValue).
                collect(Collectors.toList());

        //Validate all the connectors
        if (!connectorRequirementValues.isEmpty())
        {
            //Search in db all the connectors.
            List<String> conectorTypeValues = connectorTypeRepository.findAll().stream().
                    map(ConnectorType::getName).collect(Collectors.toList());

            //Validate that al the required connectors are configured in the db
            if (!conectorTypeValues.containsAll(connectorRequirementValues))
            {
                log.warn("[LibraryValidatorImpl] -> [validateSupportedConnectors]: Unknown connectors in [{}]. Supported: [{}]",
                        connectorRequirementValues, conectorTypeValues);

                ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_REQUIREMENT_VALUE,
                        Constants.INVALID_CONNECTORS_REQUIREMENT_VALUE_MSG + conectorTypeValues
                                + Constants.INVALID_CONNECTOR_REQUIREMENT_ACTUAL_VALUE_MSG + connectorRequirementValues);
            }
        }
    }

    /**
     * Method that validates value for connectors requirement
     *
     * @param requirements      Library requirements list values
     * @param libraryJdkVersion JDK of the library
     * @param serviceName       Service name.
     * @param errorList         Generated errors list.
     */
    private void validateSupportedJDKs(List<NovaYmlRequirement> requirements, String libraryJdkVersion, String serviceName, List<ValidationErrorDto> errorList)
    {


        // Check each sub-pattern in the requirement matches, at least, one of the available JDKs
        requirements.stream()
                .filter(req -> Constants.INSTALLATION_REQUIREMENT_NAME.SUPPORTED_JDK_VERSIONS.name().equalsIgnoreCase(req.getName()))
                .findFirst()
                .map(NovaYmlRequirement::getValue)
                .ifPresent(jdkRequiredPattern -> {
                    // Check that the JDK of the library matches its own requirement
                    if (StringUtils.isEmpty(libraryJdkVersion) || !libraryJdkVersion.matches(transformToJavaRegex(jdkRequiredPattern)))
                    {
                        log.warn("[LibraryValidatorImpl] -> [validateSupportedJDKs]: the JDK of the library [{}] does not match its own required pattern [{}]", libraryJdkVersion, jdkRequiredPattern);

                        ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_REQUIREMENT_VALUE, "Error in nova.yml file: the JDK of the library ["
                                        + libraryJdkVersion + "] does not match its own required pattern [" + jdkRequiredPattern + "].");
                    }

                    // Get list of allowed JDKs
                    List<String> allowedJDKs = this.allowedJdkRepository.findAll().stream().map(AllowedJdk::getJdk).collect(Collectors.toList());

                    // Check each sub-pattern in the requirement matches, at least, one of the available JDKs
                     for (String jdkRequiredSubPattern : jdkRequiredPattern.split("\\|"))
                     {
                       // Check at least one JDK matches the pattern
                       if (allowedJDKs.stream().noneMatch(jdk -> jdk.matches(transformToJavaRegex(jdkRequiredSubPattern))))
                       {
                           log.warn("[LibraryValidatorImpl] -> [validateSupportedJDKs]: JDK [{}] is not in the supported list [{}]",
                                   jdkRequiredSubPattern, allowedJDKs);

                           ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_REQUIREMENT_VALUE, "Error in nova.yml file: the value [" + jdkRequiredSubPattern
                                           + "] of the SUPPORTED_JDK_VERSIONS requirement is invalid. It must be a value or a pattern that matches, at least, one of the following JDKs: " + allowedJDKs);
                       }
                     }
                });
    }

    /**
     * Validates JVM parameter requirements
     *
     * @param requirements     Library requirements list
     * @param serviceName      service name.
     * @param errorList        Generated errors list.
     */
    private void validateSupportedJvmParameters(List<NovaYmlRequirement> requirements, String serviceName, List<ValidationErrorDto> errorList)
    {
        requirements.stream()
                .filter(req -> Constants.RESOURCE_REQUIREMENT_NAME.JVM_PARAMETER.name().equalsIgnoreCase(req.getName()))
                .forEach(req -> {
                    if (!jdkParameterRepository.existsByName(req.getValue().trim()))
                    {
                        List<String> allValidJdkParameterNames = jdkParameterRepository.findAll().stream()
                                .map(JdkParameter::getName)
                                .filter(name -> !name.equalsIgnoreCase("Automático"))
                                .collect(Collectors.toList());

                        log.warn("[LibraryValidatorImpl] -> [validateSupportedJvmParameters]: Unsupported JVM parameter [{}]",
                                req.getValue().trim());

                        ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_REQUIREMENT_VALUE, "Error in nova.yml file: the value ["
                                + req.getValue().trim() + "] of a JVM_PARAMETER requirement is invalid. It must be in the following list of supported parameters: " + allValidJdkParameterNames + ".");
                    }
                });
    }


    /**
     * Validate library existence
     *
     * @param library     full name of library to check
     * @param serviceName name of the service to validate its libraries
     * @param errorList   error list
     * @return true if exists
     */
    private boolean validateLibraryExistence(final LibraryDef library, final String serviceName,
                                          final List<ValidationErrorDto> errorList)
    {
        boolean existsLibrary = true;

        try
        {
            log.debug("Check library {}", library.getFullName());

            LMLibraryEnvironmentsDTO libEnvironmentDto = libraryManagerClient.getLibraryEnvironments(library.getFullName());

            log.debug("[LibraryValidatorImpl] -> [validateLibraryExistence]: libEnvironmentDto returned -> {} ", libEnvironmentDto);

            if (libEnvironmentDto == null || StringUtils.isEmpty(libEnvironmentDto.getFullName()))
            {
                log.warn("[LibraryValidatorImpl] -> [validateLibraryExistence]: Library [{}] not exists.", library);
                ErrorListUtils.addError(errorList, serviceName, Constants.LIBRARY_NOT_EXISTS,
                        library.getFullName() + " library not exists.");
                existsLibrary = false;
            }
        }
        catch (RuntimeException e)
        {
            log.warn("[LibraryValidatorImpl] -> [validateLibraryPublishedByEnv]: Error requesting {} from LibraryManager. ",
                    library.getFullName(), e);
            ErrorListUtils.addError(errorList, serviceName, Constants.LIBRARY_UNEXPECTED_ERROR_RESPONSE,
                    "Library " + library.getFullName() + " not exists");
            existsLibrary = false;
        }

        return existsLibrary;
    }

    /**
     * Given a library, check into descriptor file of the service (pom, json, ...) if declaration of libraries is valid
     *
     * @param library         definition of the library to validate
     * @param serviceName     name of the service to validate its libraries
     * @param type            type of the service
     * @param validatorInputs validator inputs with all files associated to service definition
     * @param errorList       list of error to update in case of any invalid declaration or use into descriptor file
     */
    private void validateLibraryByDescriptorFile(final LibraryDef library, final String serviceName, final ServiceType type, final ValidatorInputs validatorInputs,
                                                 final List<ValidationErrorDto> errorList)
    {
        if (type.isPomXml())
        {
            PomXML pom = validatorInputs.getPom();
            if (pom == null)
            {
                throw new NovaException(LibraryManagerError.getUnexpectedError(), "pom.xml is not found for " + serviceName);
            }
            validateLibraryByPom(library, serviceName, pom, errorList);
        }

        // other type of descriptor files have to be added
    }

    /**
     * Given a library defined into nova.yml by the service, this method checks if library is declared as dependency inside pom.xml of the service
     *
     * @param library     library definition to check
     * @param serviceName name of the service which uses libraries
     * @param pom         pom.xml file
     * @param errorList   list of error to update if there is a problem
     */
    private void validateLibraryByPom(final LibraryDef library, final String serviceName, final PomXML pom, final List<ValidationErrorDto> errorList)
    {
        List<Dependency> dependencies = pom.getDependencies().stream()
                .filter(dependency -> StringUtils.equals(dependency.getArtifactId(), library.getServiceName()))
                .collect(Collectors.toList());

        if (dependencies.isEmpty())
        {
            log.warn("[LibraryValidatorImpl] -> [validateLibraryByPom]: Library [{}] is not defined into pom.xml", library.getFullName());

            ErrorListUtils.addError(errorList, serviceName, Constants.LIBRARY_UNDECLARED_IN_DESCRIPTOR_FILE,
                    "Library [" + library.getFullName() + "] is not defined into pom.xml");
            return;
        }

        // If a dependency is repeated (with same uuaa) it will be inform
        for (Dependency dependency : dependencies)
        {
            //Only artifactId with uuaa included
            if (StringUtils.containsIgnoreCase(dependency.getGroupId(), library.getUuaa()) && !StringUtils
                    .equals(dependency.getVersion(), library.getVersion()))
            {
                log.warn("[LibraryValidatorImpl] -> [validateLibraryByPom]: Version of library [{}] does not match in pom.xml [{}:{}:{}]",
                        library.getFullName(), dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());

                ErrorListUtils.addError(errorList, serviceName, Constants.LIBRARY_VERSION_NOT_MATCH_IN_DESCRIPTOR_FILE,
                        "Version of [" + library.getFullName() + "] does not match in pom.xml [" + dependency.getGroupId() + ':' +
                                dependency.getArtifactId() + ':' + dependency.getVersion() + ']');
            }
        }
    }

    /**
     * Validate requirements of used libraries
     *
     * @param libraryFullName library full name
     * @param serviceName service name
     * @param validatorInputs validator inputs
     * @param errorList error list to update
     */
    private void validateLibraryRequirements(final String libraryFullName, final String serviceName, final ValidatorInputs validatorInputs,
                                             final List<ValidationErrorDto> errorList)
    {
        // Get list of requirements for the library
        LMLibraryRequirementsDTO requirements = libraryManagerClient.getRequirementsByFullName(libraryFullName);

        // Validate some requirements of type INSTALLATION
        Arrays.stream(requirements.getRequirements())
                .filter(requirement -> EnumUtils.isValidEnum(INSTALLATION_REQUIREMENT_NAME.class, requirement.getRequirementName()))
                .forEach(requirement -> {
                    NovaYml novaYml = validatorInputs.getNovaYml();
                    switch (INSTALLATION_REQUIREMENT_NAME.valueOf(requirement.getRequirementName()))
                    {
                        case SUPPORTED_JDK_VERSIONS:
                            checkSupportedJdkVersionsRequirement(novaYml.getJdkVersion(), requirement.getRequirementValue(), libraryFullName, serviceName, errorList);
                            break;
                        case MINIMUM_LANGUAGE_VERSION:
                            checkMinimumLanguageRequirement(novaYml.getLanguageVersion(), requirement.getRequirementValue(), libraryFullName, serviceName, errorList);
                            break;
                        case MAXIMUM_LANGUAGE_VERSION:
                            checkMaximumLanguageRequirement(novaYml.getLanguageVersion(), requirement.getRequirementValue(), libraryFullName, serviceName, errorList);
                            break;
                        default:
                            log.error("[LibraryValidatorImpl] -> [validateLibraryRequirements]: Requirement name doesn´t match with any option");
                    }
        });
    }

    /**
     * Validate that languageVersion in nova.yml is greater or equal than minimum version required by used library
     *
     * @param languageVersionInNovaYml languageVersion field in nova.yml to check
     * @param minimumLanguageVersion minimum language version required by the used library
     * @param libraryFullName library full name
     * @param serviceName service name
     * @param errorList error list to update
     */
    private void checkMinimumLanguageRequirement(final String languageVersionInNovaYml, final String minimumLanguageVersion, final String libraryFullName,
                                                 final String serviceName,  final List<ValidationErrorDto> errorList)
    {
        try
        {
            if (ComparisonUtils.compareVersions(languageVersionInNovaYml, minimumLanguageVersion) < 0)
            {
                log.warn("[LibraryValidatorImpl] -> [checkMinimumLanguageRequirement]:  languageVersion in nova.yml [{}] is less than minimum version [{}] required by used library [{}]",
                        languageVersionInNovaYml, minimumLanguageVersion, libraryFullName);
                ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_REQUIREMENT_VALUE,
                        "Error in nova.yml file: languageVersion [" + languageVersionInNovaYml + "] is less than [" + minimumLanguageVersion +
                                "], the minimum version required by used library [" + libraryFullName + "]");
            }
        }
        catch (NumberFormatException e)
        {
            log.warn("[LibraryValidatorImpl] -> [checkMinimumLanguageRequirement]: error trying to compare the languageVersion in nova.yml [{}] or minimum version [{}] required by used library [{}]. Error message: [{}]",
                    languageVersionInNovaYml, minimumLanguageVersion, libraryFullName, e.getMessage());
            ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_REQUIREMENT_VALUE,
                    "Error in nova.yml file: languageVersion [" + languageVersionInNovaYml + "] or [" + minimumLanguageVersion +
                            "], is not correct number format, required by library [" + libraryFullName + "]");
        }
    }

    /**
     * Validate that languageVersion in nova.yml is less than maximum version required by used library
     *
     * @param languageVersionInNovaYml languageVersion field in nova.yml to check
     * @param maximumLanguageVersion maximum language version required by the used library
     * @param libraryFullName library full name
     * @param serviceName service name
     * @param errorList error list to update
     */
    private void checkMaximumLanguageRequirement(final String languageVersionInNovaYml, final String maximumLanguageVersion, final String libraryFullName,
                                                 final String serviceName,  final List<ValidationErrorDto> errorList)
    {
        try
        {
            if (ComparisonUtils.compareVersions(languageVersionInNovaYml, maximumLanguageVersion) >= 0)
            {
                log.warn("[LibraryValidatorImpl] -> [checkMaximumLanguageRequirement]:  languageVersion in nova.yml [{}] is not less than maximum version [{}] required by used library [{}]",
                        languageVersionInNovaYml, maximumLanguageVersion, libraryFullName);
                ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_REQUIREMENT_VALUE,
                        "Error in nova.yml file: languageVersion [" + languageVersionInNovaYml + "] is not less than [" + maximumLanguageVersion +
                                "], the maximum version required by used library [" + libraryFullName + "]");
            }
        }
        catch (NumberFormatException e)
        {
            log.warn("[LibraryValidatorImpl] -> [checkMaximumLanguageRequirement]: error trying to get the languageVersion in nova.yml [{}] or maximum version [{}] required by used library [{}]. Error message: [{}]",
                    languageVersionInNovaYml, maximumLanguageVersion, libraryFullName, e.getMessage());
            ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_REQUIREMENT_VALUE,
                    "Error in nova.yml file: languageVersion [" + languageVersionInNovaYml + "] or [" + maximumLanguageVersion +
                            "], is not a number format, required by used library [" + libraryFullName + "]");
        }

    }

    /**
     * Validate that jdkVersion in nova.yml matches JDK pattern required by a used library
     *
     * @param jdkVersionInNovaYml jdkVersion field in nova.yml to check
     * @param jdkVersionPattern JDK version pattern to match
     * @param libraryFullName library full name
     * @param serviceName service name
     * @param errorList error list to update
     */
    private void checkSupportedJdkVersionsRequirement(final String jdkVersionInNovaYml, final String jdkVersionPattern, final String libraryFullName,
                                                      final String serviceName,  final List<ValidationErrorDto> errorList)
    {
        // Check jdkVersion is present in nova.yml
        if (StringUtils.isEmpty(jdkVersionInNovaYml))
        {
            log.warn("[LibraryValidatorImpl] -> [checkSupportedJdkVersionsRequirement]: jdkVersion is not declared in nova.yml");
            ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_REQUIREMENT_VALUE,
                    "Error in nova.yml file: used library [" + libraryFullName + "] requires that a JDK be declared and that it matches the following pattern: [" + jdkVersionPattern+"]");
            return;
        }

        // Check jdkVersion matches required pattern
        if (!jdkVersionInNovaYml.matches(transformToJavaRegex(jdkVersionPattern)))
        {
            log.warn("[LibraryValidatorImpl] -> [checkSupportedJdkVersionsRequirement]: JDK version [{}] does not match the pattern [{}] required by used library [{}]"
                    , jdkVersionInNovaYml, jdkVersionPattern, libraryFullName);
            ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_REQUIREMENT_VALUE,
                    "Error in nova.yml file: JDK version [" + jdkVersionInNovaYml + "] is not valid. Used library [" + libraryFullName
                               + "] requires that the JDK version matches the following pattern: [" + jdkVersionPattern+"]");
        }
    }

    /**
     * Transform a human-readable pattern to a valid Regex
     *
     * @param humanReadablePattern string that can includes asterisk
     * @return valid Regex
     */
    private String transformToJavaRegex(String humanReadablePattern)
    {
        return humanReadablePattern
                .replace(".","\\.")
                .replace("*",".*");
    }

    /**
     * Validate a section of nova.yml is not declared
     *
     * @param section     section from nova.yml to check
     * @param serviceName name of the service which is validating
     * @param serviceType type of service from nova.yml
     * @param sectionName name of the section to check
     * @param errorList   error list to update
     */
    private void validateSectionIsNotDeclaredInNovaYml(final List<?> section, final String serviceName, final String serviceType, final String sectionName,
                                                       final List<ValidationErrorDto> errorList)
    {
        // Check that section is not declared
        if (section != null && !section.isEmpty())
        {
            // error code has format as "INVALID_<sectioname>_<serviceclass>
            String code = "INVALID_" + sectionName.replaceAll(" ", "_").toUpperCase() + '_' + serviceType.toUpperCase();
            String message = "Error in nova.yml file: " + serviceType + " services cannot have " + sectionName + " defined";
            ErrorListUtils.addError(errorList, serviceName, code, message);
        }
    }

    /**
     * Method that validates properties specified on the nova yml file.
     *
     * @param validatorInputs data used during the validations.
     * @param dto             NOVA service information.
     * @param errorList       generated errors list.
     */
    private void validateNovaYmlProperties(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList)
    {
        // Properties
        for (NovaYamlProperty property : validatorInputs.getNovaYml().getProperties())
        {
            // Property name
            if (Strings.isNullOrEmpty(property.getName()))
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PROPERTY_NAME, Constants.INVALID_PROPERTY_NAME_MSG);
            }

            // Property encrypt
            if (property.getEncrypt() == null)
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PROPERTY_ENCRYPT, Constants.INVALID_PROPERTY_ENCRYPT_MSG);
            }

            // Property management
            if (property.getManagement() == null || !EnumUtils.isValidEnum(Constants.PROPERTY_MANAGEMENT.class, property.getManagement()))
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_LIBRARY_PROPERTY_MANAGEMENT, Constants.INVALID_LIBRARY_PROPERTY_MANAGEMENT_MSG);
            }

            // Property type
            if (property.getType() == null || !EnumUtils.isValidEnum(Constants.PROPERTY_TYPE.class, property.getType()))
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PROPERTY_TYPE, Constants.INVALID_PROPERTY_TYPE_MSG);
            }

            // Property scope
            if (property.getScope() == null || !EnumUtils.isValidEnum(Constants.PROPERTY_SCOPE.class, property.getScope()))
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PROPERTY_SCOPE, Constants.INVALID_PROPERTY_SCOPE_MSG);
            }

            // Property management SERVICE cannot be with scope GLOBAL for libraries service
            if (ManagementType.SERVICE.name().equals(property.getManagement()) && ScopeType.GLOBAL.name().equals(property.getScope()))
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_LIBRARY_PROPERTY, Constants.INVALID_LIBRARY_PROPERTY_MSG);
            }
        }
    }

    /**
     * Internal class to validate a library
     * This class represent a nova library organizing uuaa, release, service name and version
     */
    @Builder
    @Getter
    @ToString
    static final class LibraryDef
    {
        /**
         * separator from full name
         */
        private static final char FULL_NAME_VALUE_SEPARATOR = '-';

        /**
         * Parameters inside full name
         */
        private static final int FULL_NAME_SIZE = 4;

        /**
         * Index of uuaa inside full name of library service
         */
        private static final int UUAA_INDEX = 0;
        /**
         * Index of release inside full name of library service
         */
        private static final int RELEASE_INDEX = 1;
        /**
         * Index of service name inside full name of library service
         */
        private static final int SERVICE_NAME_INDEX = 2;
        /**
         * Index of service version inside full name of library service
         */
        private static final int VERSION_INDEX = 3;

        /**
         * Full name of the library from nova.yml
         */
        private final String fullName;

        /**
         * uuaa of the library
         */
        private final String uuaa;

        /**
         * release of library
         */
        private final String release;

        /**
         * library service name
         */
        private final String serviceName;

        /**
         * version of the library
         */
        private final String version;

        /**
         * Factory method
         *
         * @return LibraryDef by full name
         */
        static LibraryDef fromFullName(final String fullName)
        {
            String[] values = StringUtils.split(fullName, FULL_NAME_VALUE_SEPARATOR);
            if (fullName == null || values.length < FULL_NAME_SIZE)
            {
                throw new NovaException(LibraryManagerError.getUnexpectedError(), "invalid full name : " + fullName);
            }

            return new LibraryDefBuilder().fullName(fullName).uuaa(values[UUAA_INDEX]).release(values[RELEASE_INDEX])
                    .serviceName(values[SERVICE_NAME_INDEX]).version(values[VERSION_INDEX]).build();
        }
    }
}
