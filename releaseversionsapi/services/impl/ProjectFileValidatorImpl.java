package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceInfoDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVValidationErrorDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVMetadataDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.core.novabootstarter.util.ServiceNamingUtils;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.enumerates.ServiceLanguage;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.apidefinitionvalidations.interfaces.IApiDefinitionBehaviorValidator;
import com.bbva.enoa.platformservices.coreservice.common.repositories.AllowedJdkRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.QualityUtils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ConfigurationmanagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.QualityAssuranceClientImpl;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IDockerRegistryClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.*;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.interfaces.IApiDefinitionValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.ILibraryValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IProjectFileValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.*;
import com.bbva.enoa.utils.schedulerparserlib.processor.ParsedInfo;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.bbva.enoa.core.novabootstarter.enumerate.ServiceType.*;


/**
 * Validator of services by the NOVA rules.
 * <p>
 * Created by xe52580 on 17/02/2017.
 */
@Slf4j
@Service
public class ProjectFileValidatorImpl implements IProjectFileValidator
{
    /**
     * NOVA version
     */
    @Value("${nova.version:3}")
    private String currentNovaVersion;

    /**
     * UUAA length
     */
    @Value("${nova.validations.uuaaMaxLength:4}")
    private int uuaaLength;

    /**
     * Definition of Node.js versions supported by NOVA in a comma separated
     * list.
     */
    @Value("${nova.node.versions:6}")
    private String novaNodeVersions;

    /**
     * NOVA packaging
     */
    @Value("${nova.packaging:jar}")
    private String novaPackaging;

    /**
     * NOVA parent version
     */
    @Value("${nova.parentVersion:1.1}")
    private String novaParentVersion;

    /**
     * NOVA plugin version
     */
    @Value("${nova.pluginVersion:4}")
    private String novaPluginVersion;

    /**
     * NOVA version
     */
    @Value("${nova.minimumNovaVersion:18}")
    private String novaYmlVersion;

    /**
     * Java version
     */
    @Value("${nova.minimumJavaVersion:1.8}")
    private String javaVersion;

    /**
     * FrontCat Java version
     */
    @Value("${nova.minimumFrontCatJavaVersion:1.7}")
    private String frontcatJavaVersion;

    /**
     * Java version
     */
    @Value("${nova.minimumNodeJsVersion:8}")
    private String nodeJsVersion;

    /**
     * Angular version
     */
    @Value("${nova.minimumAngularVersion:0}")
    private String angularVersion;

    /**
     * Python minimum supported version
     */
    @Value("${nova.minimumPythonVersion:3.7}")
    private String pythonVersion;

    /**
     * Template version
     */
    @Value("${nova.minimumTemplateVersion:1.0}")
    private String templateVersion;

    /**
     * NOVA plugin version
     */
    @Value("${nova.pluginOutputDirectory:./dist}")
    private String pluginOutputDirectory;

    @Value("${nova.minimumMultiJdkJavaVersion:11}")
    private String minimumMultiJdkJavaVersion;

    /**
     * configuration manager client
     */
    @Autowired
    private ConfigurationmanagerClient configurationmanagerClient;

    /**
     * Docker registry client
     */
    @Autowired
    private IDockerRegistryClient iDockerRegistryClient;

    /**
     * Definition validator
     */
    @Autowired
    private IApiDefinitionValidator apiResolverDefinitionValidator;

    /**
     * Definition behavior validator
     */
    @Autowired
    private IApiDefinitionBehaviorValidator apiDefinitionBehaviorValidator;

    /**
     * Library validator
     */
    @Autowired
    private ILibraryValidator libraryValidator;

    /**
     * Quality assurance client
     */
    @Autowired
    private QualityAssuranceClientImpl qualityAssuranceClient;

    @Autowired
    private AllowedJdkRepository allowedJdkRepository;

    @Override
    public void validateServiceProjectFiles(ValidatorInputs validatorInputs, final SubsystemType subsystemNovaType,
                                            NewReleaseVersionServiceDto newReleaseVersionServiceDto, final int repoId, final String tag, final String ivUser,
                                            final String releaseName, Product product, String subsystemName)
    {
        // 1. Create list of validation errors.
        List<ValidationErrorDto> errorList = new ArrayList<>();

        // 2. Check service type and depending on the service type, make specific validations
        if (this.hasValidSubsystemAndServiceType(newReleaseVersionServiceDto, errorList, subsystemNovaType))
        {
            // 3. Check all the validation inputs for generic files
            this.validateProjectFiles(validatorInputs, newReleaseVersionServiceDto, errorList, repoId, tag, releaseName, product);

            // 4. Validate, depending on the service type, TEMPLATE( NOVA...) or
            if (!validatorInputs.isLatestVersion())
            {
                this.validateSpecialFilesByServiceType(newReleaseVersionServiceDto, newReleaseVersionServiceDto.getFolder(), repoId, tag, ivUser,
                        errorList);
            }

            // 5. Validate pom.xml y nova.yml has the same version
            this.validateSameVersion(newReleaseVersionServiceDto.getServiceName(), newReleaseVersionServiceDto.getServiceType(),
                    validatorInputs.getNovaYml(), validatorInputs.getPom(), errorList);

            // 6. Validate pom.xml and nova.yml service name
            this.validateSameServiceName(newReleaseVersionServiceDto.getServiceName(), newReleaseVersionServiceDto.getServiceType(),
                    validatorInputs.getNovaYml(), validatorInputs.getPom(), errorList);
        }

        // 7. Check novaYmlProperties if is the latest version.
        if (validatorInputs.isLatestVersion())
        {
            this.isServiceNovaYmlPropertiesValid(newReleaseVersionServiceDto.getServiceName(), newReleaseVersionServiceDto.getFolder(), repoId, tag, newReleaseVersionServiceDto.getServiceType(), ivUser, errorList);
        }

        // 8. Set the validation results to the new release version service
        newReleaseVersionServiceDto.setValidationErrors(errorList.toArray(new ValidationErrorDto[0]));

        // 9. Check if Container Image already exist in registry if exist sets force compilation to false.
        this.checkImageInRegistry(ivUser, newReleaseVersionServiceDto, releaseName);

        // 10. Check if the last sonar analysis was performed with sonar 5
        this.checkLastSonarAnalysis(newReleaseVersionServiceDto, subsystemName, subsystemNovaType);
    }

    @Override
    public void validateBehaviorServiceProjectFiles(List<ValidationErrorDto> errorList, final ValidatorInputs validatorInputs, final BVServiceInfoDTO bvServiceInfoDTO,
                                                    final int repoId, final String tag, final String ivUser,
                                                    final Product product, final String subsystemName, final String behaviorVersionName)
    {
        // 1. Check all the validation inputs for generic files
        this.validateBehaviorProjectFiles(validatorInputs, bvServiceInfoDTO, errorList, repoId, tag, product);

        // 2. Validate pom.xml y nova.yml has the same version
        this.validateSameVersion(bvServiceInfoDTO.getServiceName(), bvServiceInfoDTO.getServiceType(),
                validatorInputs.getNovaYml(), validatorInputs.getPom(), errorList);

        // 3. Validate pom.xml and nova.yml service name
        this.validateSameServiceName(bvServiceInfoDTO.getServiceName(), bvServiceInfoDTO.getServiceType(),
                validatorInputs.getNovaYml(), validatorInputs.getPom(), errorList);

        // 4. Check novaYmlProperties if is the latest version.
        this.isBehaviorServiceNovaYmlPropertiesValid(bvServiceInfoDTO.getServiceName(), bvServiceInfoDTO.getFolder(),
                repoId, tag, bvServiceInfoDTO.getServiceType(), ivUser, errorList);

        // 5. Set the validation results to the new release version service
        bvServiceInfoDTO.setValidationErrors(errorList.stream().map(error -> {
            BVValidationErrorDto bvValidationErrorDto = new BVValidationErrorDto();
            bvValidationErrorDto.setCode(error.getCode());
            bvValidationErrorDto.setMessage(error.getMessage());
            return bvValidationErrorDto;
        }).toArray(BVValidationErrorDto[]::new));

        // 6. Check if Container Image already exist in registry if exist sets force compilation to false.
        this.checkBehaviorImageInRegistry(ivUser, bvServiceInfoDTO, behaviorVersionName);

        // 7. Check if the last sonar analysis was performed with sonar 5
        this.behaviorCheckLastSonarAnalysis(bvServiceInfoDTO, subsystemName);


    }

    //////////////////////////////////////////////   PRIVATE METHODS   \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     * Check if application.yml is unique in the resources' folder,
     * application-LOCAL.yml is also valid.
     *
     * @param validatorInputs Input values to validateServiceProjectFiles
     * @param serviceName     Service name
     * @param errorList       Error list
     */
    private void checkApplicationYmlInResourceFolder(ValidatorInputs validatorInputs, String serviceName,
                                                     List<ValidationErrorDto> errorList)
    {

        for (String fileName : validatorInputs.getApplication().getApplicationFiles())
        {
            if (!(fileName.equals(Constants.APPLICATION_YML) || fileName.equals(Constants.APPLICATION_LOCAL_YML)) && fileName
                    .startsWith(Constants.APPLICATION))
            {
                ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_APPLICATION_FILE,
                        Constants.INVALID_APPLICATION_FILE_MSG + " - '" + fileName + "'");
            }
        }
    }

    private void checkDockerFile(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, ServiceType serviceType,
                                 List<ValidationErrorDto> errorList)
    {

        log.debug("[{}] -> [{}]: CheckDockerFile: GET Dockerfile: [{}]", Constants.PROJECT_FILE_VALIDATOR, "validateDockerFile",
                validatorInputs.getDockerfile());

        if (serviceType.equals(NOVA) || serviceType.equals(NOVA_BATCH) || serviceType.equals(NOVA_SPRING_BATCH) || serviceType.equals(THIN2))
        {
            List<String> validContent = validatorInputs.getDockerfile().getDockerfileTemplateForNovaBatchAndThin2(validatorInputs);
            List<String> content = validatorInputs.getDockerfile().getDockerfileContent();
            if (!content.equals(validContent))
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.DOCKFILE_EDITED, Constants.DOCKFILE_EDITED_MSG);
            }

        }
        else if (serviceType.equals(NODE))
        {
            List<String> validContent = validatorInputs.getDockerfile().getDockerfileTemplateForNode();
            List<String> content = validatorInputs.getDockerfile().getDockerfileContent();
            for (int i = 0; i < validContent.size(); i++)
            {
                if (!PatternMatcher.customPattern(validContent.get(i), content.get(i)))
                {
                    ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.DOCKFILE_EDITED, Constants.DOCKFILE_EDITED_MSG);
                }
            }
        }
    }

    /**
     * Validate if container Image already exist in registry, if the image exist
     * the compilation is not forced, In case the image does not exist the
     * compilation must be forced
     *
     * @param ivUser      Name of User
     * @param releaseName Name of the release
     * @param dto         NewReleaseServiceDto
     */
    private void checkImageInRegistry(String ivUser, NewReleaseVersionServiceDto dto, String releaseName)
    {
        if (ServiceType.BATCH_SCHEDULER_NOVA.getServiceType().equalsIgnoreCase(dto.getServiceType()))
        {
            log.debug("[{}] service type - [{}] service name does not generate registry image. Continue", dto.getServiceType(), dto.getServiceName());
            dto.setIsImageInRegistry(false);
            dto.setHasForcedCompilation(false);
        }
        else
        {
            // Get Image name
            String nameImage = dto.getGroupId() + "/" + dto.getArtifactId() + "-" + releaseName + ":" + dto.getVersion();

            boolean existImage = this.iDockerRegistryClient.isImageInRegistry(ivUser, nameImage.toLowerCase());

            // Verifies if image exist in registry
            dto.setIsImageInRegistry(existImage);

            // If image exist in registry the compilation is not Forced, the user must decide.
            dto.setHasForcedCompilation(!existImage);
        }
    }

    /**
     * Validate if container Image already exist in registry, if the image exist
     * the compilation is not forced, In case the image does not exist the
     * compilation must be forced
     *
     * @param ivUser Name of User
     * @param dto    NewReleaseServiceDto
     */
    private void checkBehaviorImageInRegistry(String ivUser, BVServiceInfoDTO dto, String behaviorVersionName)
    {
        // Get Image name
        String nameImage = dto.getGroupId() + "/" + dto.getArtifactId() + "-" + behaviorVersionName + ":" + dto.getVersion();

        boolean existImage = this.iDockerRegistryClient.isImageInRegistry(ivUser, nameImage.toLowerCase());

        // Verifies if image exist in registry
        dto.setIsImageInRegistry(existImage);

        // If image exist in registry the compilation is not Forced, the user must decide.
        dto.setHasForcedCompilation(!existImage);
    }

    /**
     * Validate subsystem type
     *
     * @param newReleaseVersionServiceDto Release version service with service type info
     * @param subsystemType               subsystem type
     * @return true if type is valid
     */
    private boolean hasValidServiceType(NewReleaseVersionServiceDto newReleaseVersionServiceDto, final SubsystemType subsystemType)
    {

        boolean valid = true;
        ServiceType serviceType = ServiceType.getValueOf(newReleaseVersionServiceDto.getServiceType());
        // If subsystem type is NOVA, service can be NOVA or Thin2.

        if (SubsystemType.NOVA.equals(subsystemType) || SubsystemType.FRONTCAT.equals(subsystemType))
        {
            valid = !serviceType.isEPhoenix() && !ServiceType.isLibrary(serviceType);
        }
        else if (SubsystemType.LIBRARY.equals(subsystemType))
        {
            valid = ServiceType.isLibrary(serviceType) || DEPENDENCY.equals(serviceType);
        }
        else if (SubsystemType.EPHOENIX.equals(subsystemType))
        {
            valid = serviceType.isEPhoenix();
        }

        return valid;
    }

    /**
     * Checks if the service has a valid type in the context of its subsystem.
     * <p>
     * Allowed types:
     * <p>
     * - Subsystem with NOVA type: - NOVA, NOVA_BATCH, NOVA_SPRING_BATCH, THIN2,
     * DEPENDENCY, NODE_JS
     * <p>
     * - Subsystem with EPHOENIX type: - EPHOENIX_BATCH - EPHOENIX_ONLINE
     * <p>
     * - Any others: - The same type a its subsystem.
     *
     * @param errorList     List error will be added to.
     * @param subsystemType Type of subsystem the service belongs to.
     */
    private boolean hasValidSubsystemAndServiceType(NewReleaseVersionServiceDto newReleaseVersionServiceDto, List<ValidationErrorDto> errorList,
                                                    final SubsystemType subsystemType)
    {
        boolean results = true;

        if (ServiceType.INVALID == ServiceType.getValueOf(newReleaseVersionServiceDto.getServiceType()))
        {
            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), Constants.INVALID_SERVICE_TYPE,
                    Constants.INVALID_SERVICE_TYPE_MSG);
            results = false;
        }
        else if (!this.hasValidServiceType(newReleaseVersionServiceDto, subsystemType))
        {
            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), Constants.INVALID_SERVICE_SUBSYSTEM_TYPE,
                    Constants.INVALID_SERVICE_SUBSYSTEM_TYPE_MSG);
            results = false;
        }

        return results;
    }


    /**
     * Check novaYml file.
     *
     * @param serviceName                   service name
     * @param projectPath                   project path
     * @param versionControlSystemProjectId version Control System Proyect id
     * @param tag                           tag name
     * @param serviceType                   the service type for validating the template.yml
     * @param ivUser                        requester user
     * @param errorList                     error list
     */
    private void isServiceNovaYmlPropertiesValid(final String serviceName, final String projectPath,
                                                 final int versionControlSystemProjectId, final String tag, final String serviceType, final String ivUser,
                                                 List<ValidationErrorDto> errorList)
    {
        if (configurationmanagerClient.validateServiceNovaYmlFile(
                serviceName, projectPath, tag, versionControlSystemProjectId, serviceType, ivUser, errorList))
        {
            log.debug("[{}] -> [{}]: novaYml OK", Constants.PROJECT_FILE_VALIDATOR, "isServiceNovaYmlPropertiesValid");
        }
        else
        {
            log.warn("[{}] -> [{}]: Invalid novaYml", Constants.PROJECT_FILE_VALIDATOR, "isServiceNovaYmlPropertiesValid");
        }
    }

    /**
     * Check novaYml file.
     *
     * @param serviceName                   service name
     * @param projectPath                   project path
     * @param versionControlSystemProjectId version Control System Proyect id
     * @param tag                           tag name
     * @param serviceType                   the service type for validating the template.yml
     * @param ivUser                        requester user
     * @param errorList                     error list
     */
    private void isBehaviorServiceNovaYmlPropertiesValid(final String serviceName, final String projectPath,
                                                         final int versionControlSystemProjectId, final String tag, final String serviceType, final String ivUser,
                                                         List<ValidationErrorDto> errorList)
    {
        if (configurationmanagerClient.validateBehaviorServiceNovaYmlFile(
                serviceName, projectPath, tag, versionControlSystemProjectId, serviceType, ivUser, errorList))
        {
            log.debug("[{}] -> [{}]: novaYml OK", Constants.PROJECT_FILE_VALIDATOR, "isBehaviorServiceNovaYmlPropertiesValid");
        }
        else
        {
            log.warn("[{}] -> [{}]: Invalid novaYml", Constants.PROJECT_FILE_VALIDATOR, "isBehaviorServiceNovaYmlPropertiesValid");
        }
    }


    /*
     * Checks if a Node.js based service uses the NOVA supported version.
     */
    private void usesSupportedNodeVersion(NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList)
    {

        // Only for Node.js based services.
        // IMP: Service type is different from the enum name.
        if (!ServiceType.NODE.getServiceType().equalsIgnoreCase(dto.getServiceType()))
        {
            return;
        }

        // Get the Node.js field if present.
        Optional<RVMetadataDTO> match = Arrays.stream(dto.getMetadata()).filter(s -> s.getKey().equalsIgnoreCase(Constants.NODE_JS_VERSION))
                .findFirst();

        RVMetadataDTO serviceNodeVersion = match.orElse(null);

        // If it has a version:
        if (serviceNodeVersion != null)
        {
            // Get the major version from the whole version number.
            String majorVersion = serviceNodeVersion.getValue().split("\\.")[0];

            boolean usesSupportedVersion = false;

            String[] supportedVersions = novaNodeVersions.split(",");
            supportedVersions = StringUtils.trimArrayElements(supportedVersions);

            for (String supportedVersion : supportedVersions)
            {
                if (supportedVersion.equalsIgnoreCase(majorVersion))
                {
                    usesSupportedVersion = true;
                    break;
                }
            }

            if (!usesSupportedVersion)
            {
                // Wrong version.
                ErrorListUtils
                        .addError(errorList, dto.getServiceName(), Constants.NOT_SUPPORTED_NODE_VERSION, Constants.NOT_SUPPORTED_NODE_VERSION_MSG);
            }
        }
        // If it has not, throw error:
        else
        {
            // No version present.
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.MISSING_NODE_VERSION, Constants.MISSING_NODE_VERSION_MSG);
        }
    }

    /**
     * Validate the specific parameters of nova.yml of a api rest or batch project
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param errorList       List of validation errors.
     */
    private void validateApiRestAndBatchNovaYml(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList)
    {
        // Ports
        this.validateApiRestAndBatchNovaYmlPorts(validatorInputs, dto, errorList);
        // Properties
        this.validateNovaYmlProperties(validatorInputs, dto, errorList);

        // Check language and jdk version if the service type is of JAVA Language
        if (ServiceType.isJdkSelectable(ServiceType.getValueOf(dto.getServiceType())))
        {
            this.validateLanguageAndJdkVersions(validatorInputs, dto, errorList);
        }
    }

    /**
     * Validate language version and jdk version from nova.yml
     * - language version: if it is lower than 11, it can only be java 8, so the flag jdk version must be null even the nova.yml contains any value (because jdk version does not apply for java 8)
     * - jdk version: if value is greater or equal than 11, both jdk version and language version, must exist and be related into data base (coreservice: table - allowed_jdk_jvm_versions
     *
     * @param validatorInputs
     * @param newReleaseVersionServiceDto
     * @param errorList
     */
    private void validateLanguageAndJdkVersions(final ValidatorInputs validatorInputs, final NewReleaseVersionServiceDto newReleaseVersionServiceDto, final List<ValidationErrorDto> errorList)
    {
        final String languageVersion = validatorInputs.getNovaYml().getLanguageVersion();
        final String jdkVersion = validatorInputs.getNovaYml().getJdkVersion();

        // Check and validate language version
        if (Strings.isNullOrEmpty(languageVersion))
        {
            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), Constants.INVALID_JVM_VERSION, Constants.INVALID_JVM_VERSION_MSG);
        }
        else
        {
            // Get the mayor version from language version to know the current java version
            int majorVersion = -1;
            try
            {
                majorVersion = Integer.parseInt(languageVersion.split("\\.")[0]);
            }
            catch (NumberFormatException e)
            {
                ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), Constants.INVALID_JVM_VERSION, Constants.INVALID_JVM_VERSION_MSG);
            }

            // If java version (mayor number) is lower than minimum jdk version supported, set to null the tag jdkVersion
            if (majorVersion < Integer.parseInt(this.minimumMultiJdkJavaVersion))
            {
                // Ensure that the flag jdk version will be null
                validatorInputs.getNovaYml().setJdkVersion(null);
            }
            else
            {
                if (Strings.isNullOrEmpty(jdkVersion))
                {
                    ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), Constants.INVALID_JDK_VERSION, Constants.INVALID_JDK_VERSION_MSG);
                }
                else
                {
                    if (this.allowedJdkRepository.countByJvmVersionAndJdk(languageVersion, jdkVersion) == 0)
                    {
                        ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), Constants.INVALID_JVM_JDK_DUPLE, Constants.INVALID_JVM_JDK_DUPLE_MSG);
                    }
                }
            }
        }
    }

    private void validateApiRestAndBatchNovaYmlPorts(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto,
                                                     List<ValidationErrorDto> errorList)
    {
        // Ports
        for (NovaYmlPort port : validatorInputs.getNovaYml().getPorts())
        {
            // Property name
            if (port.getName() == null || port.getName().equals(""))
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PORT_NAME, Constants.INVALID_PORT_NAME_MSG);
            }

            // Property management
            if (port.getType() == null || !EnumUtils.isValidEnum(Constants.PORT_TYPE.class, port.getType()))
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PORT_TYPE, Constants.INVALID_PORT_TYPE_MSG);
            }

            // Property management
            if (port.getInsidePort() == 0)
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PORT_INSIDE_PORT, Constants.INVALID_PORT_INSIDE_PORT_MSG);
            }
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
        log.info("[ProjectFileValidatorImpl] -> [validateNovaYmlProperties] -> propertiesList: [{}]", validatorInputs.getNovaYml().getProperties());
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
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PROPERTY_MANAGEMENT, Constants.INVALID_PROPERTY_MANAGEMENT_MSG);
            }

            // Property type
            if (property.getType() == null || !EnumUtils.isValidEnum(Constants.PROPERTY_TYPE.class, property.getType()))
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PROPERTY_TYPE, Constants.INVALID_PROPERTY_TYPE_MSG);
            }

            // Property scope
            if (ServiceType.isLibrary(ServiceType.getValueOf(dto.getServiceType())))
            {
                if (property.getScope() == null || !EnumUtils.isValidEnum(Constants.PROPERTY_SCOPE.class, property.getScope()))
                {
                    ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PROPERTY_SCOPE, Constants.INVALID_PROPERTY_SCOPE_MSG);
                }
            }
        }
    }

    private void validateBatchSchedulerNovaYml(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList)
    {
        // Properties
        if (!validatorInputs.getNovaYml().getProperties().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PROPERTIES_BATCH_SCHEDULER,
                    Constants.INVALID_PROPERTIES_BATCH_SCHEDULER_MSG);
        }

        // Ports
        if (!validatorInputs.getNovaYml().getPorts().isEmpty())
        {
            ErrorListUtils
                    .addError(errorList, dto.getServiceName(), Constants.INVALID_PORTS_BATCH_SCHEDULER, Constants.INVALID_PORTS_BATCH_SCHEDULER_MSG);
        }

        // Build
        if (!validatorInputs.getNovaYml().getBuild().isEmpty())
        {
            ErrorListUtils
                    .addError(errorList, dto.getServiceName(), Constants.INVALID_BUILD_BATCH_SCHEDULER, Constants.INVALID_BUILD_BATCH_SCHEDULER_MSG);
        }

        // Machines
        if (!validatorInputs.getNovaYml().getMachines().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_MACHINES_BATCH_SCHEDULER,
                    Constants.INVALID_MACHINES_BATCH_SCHEDULER_MSG);
        }

        // Dependencies
        if (!validatorInputs.getNovaYml().getDependencies().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_DEPENDENCIES_BATCH_SCHEDULER,
                    Constants.INVALID_DEPENDENCIES_BATCH_SCHEDULER_MSG);
        }

        // Context params are mandatory (at least trigger)
        if (validatorInputs.getNovaYml().getContextParams().isEmpty())
        {
            ErrorListUtils
                    .addError(errorList, dto.getServiceName(), Constants.INVALID_EMPTY_CONTEXT_PARAMS, Constants.INVALID_EMPTY_CONTEXT_PARAMS_MSG);
        }
        else
        {
            // Check trigger context param
            if (validatorInputs.getNovaYml().getContextParams().stream().noneMatch(
                    novaYamlContextParam -> !Strings.isNullOrEmpty(novaYamlContextParam.getName()) && "trigger"
                            .equalsIgnoreCase(novaYamlContextParam.getName())))
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_EMPTY_CONTEXT_PARAMS,
                        Constants.INVALID_EMPTY_CONTEXT_PARAMS_MSG);
            }

            // Check context parameters
            for (NovaYamlContextParam novaYamlContextParam : validatorInputs.getNovaYml().getContextParams())
            {
                // context parameter name
                if (Strings.isNullOrEmpty(novaYamlContextParam.getName()))
                {
                    ErrorListUtils
                            .addError(errorList, dto.getServiceName(), Constants.INVALID_CONTEXT_PARAM_NAME, Constants.INVALID_CONTEXT_PARAM_MSG);
                }

                // context parameter description
                if (novaYamlContextParam.getDescription() == null)
                {
                    ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_CONTEXT_PARAM_DESCRIPTION,
                            Constants.INVALID_CONTEXT_PARAM_DESCRIPTION_MSG);
                }

                // context parameter type
                if (novaYamlContextParam.getType() == null || !EnumUtils.isValidEnum(Constants.PARAM_TYPE.class, novaYamlContextParam.getType()))
                {
                    ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_CONTEXT_PARAM_TYPE,
                            Constants.INVALID_CONTEXT_PARAM_TYPE_MSG);
                }

                // context parameter default value
                if (Strings.isNullOrEmpty(novaYamlContextParam.getDefaultValue()))
                {
                    ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_CONTEXT_DEFAULT_VALUE,
                            Constants.INVALID_CONTEXT_DEFAULT_VALUE_MSG);
                }
            }
        }
    }

    /**
     * Validates the application values
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param serviceType     Service type
     * @param errorList       List of validation errors.
     */
    private void validateApplication(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, ServiceType serviceType, List<ValidationErrorDto> errorList)
    {

        log.debug("[{}] -> [{}]: ValidateApplication: GET application: [{}]", Constants.PROJECT_FILE_VALIDATOR, Constants.VALIDATE_NON_EPHOENIX_POM, validatorInputs.getApplication());

        // Validate the existence of the application.yml file. There can only
        // exist the application.yml and the application-LOCAL.yml.
        this.validateApplicationExistence(validatorInputs, dto.getServiceName(), errorList);

        //Validtae YAML Exceptions during parsing
        if (!this.validateApplicationException(validatorInputs, dto.getServiceName(), errorList))
        {
            //Server port value must be $SERVER_PORT
            this.validateApplicationPort(validatorInputs, dto.getServiceName(), serviceType, errorList);

            // This validation is only for JAVA version older than java 11 (it means, this validation is only for java 8)
            if (validatorInputs.getNovaYml() != null
                    && !Strings.isNullOrEmpty(validatorInputs.getNovaYml().getLanguageVersion())
                    && validatorInputs.getNovaYml().getLanguageVersion().startsWith(this.javaVersion))
            {
                // Properties restart must be set as false
                this.validateApplicationRestart(validatorInputs, dto, errorList);

                // Properties shutdown must be set as false
                this.validateApplicationShutdown(validatorInputs, dto, errorList);

                // Properties logfile must be set as false
                this.validateApplicationLog(validatorInputs, dto.getServiceName(), errorList);
            }
        }
    }

    /**
     * Validates the application values
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param serviceType     Service type
     * @param errorList       List of validation errors.
     */
    private void validateBehaviorApplication(ValidatorInputs validatorInputs, BVServiceInfoDTO dto, ServiceType serviceType, List<ValidationErrorDto> errorList)
    {

        log.debug("[{}] -> [{}]: ValidateApplication: GET application: [{}]", Constants.PROJECT_FILE_VALIDATOR, Constants.VALIDATE_NON_EPHOENIX_POM, validatorInputs.getApplication());

        // Validate the existence of the application.yml file. There can only
        // exist the application.yml and the application-LOCAL.yml.
        this.validateApplicationExistence(validatorInputs, dto.getServiceName(), errorList);

        //Validate YAML Exceptions during parsing
        if (!this.validateApplicationException(validatorInputs, dto.getServiceName(), errorList))
        {
            //Server port value must be $SERVER_PORT
            this.validateApplicationPort(validatorInputs, dto.getServiceName(), serviceType, errorList);
        }
    }

    /**
     * Validates the application values
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param serviceType     Service type
     * @param errorList       List of validation errors.
     */
    private void validateApplicationOldServices(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, ServiceType serviceType, List<ValidationErrorDto> errorList)
    {

        log.debug("[{}] -> [{}]: ValidateApplication: GET application: [{}]", Constants.PROJECT_FILE_VALIDATOR, Constants.VALIDATE_NON_EPHOENIX_POM, validatorInputs.getApplication());

        // Validate the existence of the application.yml file. There can only
        // exist the application.yml and the application-LOCAL.yml.
        this.validateApplicationExistence(validatorInputs, dto.getServiceName(), errorList);

        //Validtae YAML Exceptions during parsing
        if (!this.validateApplicationException(validatorInputs, dto.getServiceName(), errorList))
        {
            //Server port value must be $SERVER_PORT
            this.validateApplicationPort(validatorInputs, dto.getServiceName(), serviceType, errorList);

            // Properties restart must be set as false
            this.validateApplicationRestart(validatorInputs, dto, errorList);

            // Properties shutdown must be set as false
            this.validateApplicationShutdown(validatorInputs, dto, errorList);

            // Properties logfile must be set as false
            this.validateApplicationLog(validatorInputs, dto.getServiceName(), errorList);
        }
    }

    /**
     * Validate application exception if application.yml cannot be read
     *
     * @param validatorInputs validator inputs
     * @param serviceName     service name
     * @param errorList       list of error
     * @return result of validation
     */
    private boolean validateApplicationException(ValidatorInputs validatorInputs, String serviceName, List<ValidationErrorDto> errorList)
    {
        boolean error = false;
        //Yaml exception
        if (!StringUtils.isEmpty(validatorInputs.getApplication().getYamlException()))
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.YAML_EXCEPTION, Constants.YAML_EXCEPTION_MSG + validatorInputs.getApplication().getYamlException());
            error = true;
        }
        return error;
    }

    /**
     * Validate application existence
     *
     * @param validatorInputs validator inputs
     * @param serviceName     service name
     * @param errorList       list of error
     */
    private void validateApplicationExistence(ValidatorInputs validatorInputs, String serviceName, List<ValidationErrorDto> errorList)
    {
        // Validate the existence of the application.yml file. There can only
        // exist the application.yml and the application-LOCAL.yml.
        if (!validatorInputs.getApplication().getApplicationFiles().contains(Constants.APPLICATION_YML))
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.MISSING_APPLICATION_FILE, Constants.MISSING_APPLICATION_FILE_MSG);
        }
        else
        {
            this.checkApplicationYmlInResourceFolder(validatorInputs, serviceName, errorList);
        }
    }

    /**
     * Validate application log
     *
     * @param validatorInputs validator inputs
     * @param serviceName     service name
     * @param errorList       list of error
     */
    private void validateApplicationLog(ValidatorInputs validatorInputs, String serviceName, List<ValidationErrorDto> errorList)
    {
        // Properties logfile must be set as false
        String logFile = validatorInputs.getApplication().getEndpointLogFile();
        log.debug("[{}] -> [{}]: Get the logfile property: [{}]", Constants.PROJECT_FILE_VALIDATOR, Constants.VALIDATE_APPLICATION, logFile);

        if (logFile != null && !logFile.equalsIgnoreCase(Constants.FALSE))

        {
            log.debug("[{}] -> [{}]: EndPoint logfile: [{}]", Constants.PROJECT_FILE_VALIDATOR, Constants.VALIDATE_APPLICATION, logFile);
            ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_PROPERTY_LOGFILE, Constants.INVALID_PROPERTY_LOGFILE_MSG);
        }
    }

    /**
     * Validate application port
     *
     * @param validatorInputs validator inputs
     * @param serviceName     service name
     * @param errorList       list of error
     */
    private void validateApplicationPort(ValidatorInputs validatorInputs, String serviceName, ServiceType serviceType,
                                         List<ValidationErrorDto> errorList)
    {
        //Server port: (services without security)
        if (serviceType.equals(NOVA) && (validatorInputs.getApplication().getServerPort() == null || !validatorInputs.getApplication().getServerPort()
                .startsWith(Constants.SERVER_PORT)))
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.MISSING_PORT, Constants.MISSING_SERVER_PORT_MSG);
        }
        // NOVA port: services with security
        if ((serviceType.equals(API_REST_NODE_JS_EXPRESS) || ServiceType.isJavaApiServiceType(serviceType)) && (
                validatorInputs.getApplication().getServerPort() == null || !validatorInputs.getApplication().getServerPort()
                        .startsWith(Constants.NOVA_PORT)))
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.MISSING_PORT, Constants.MISSING_NOVA_PORT_MSG);
        }
    }

    private void validateApplicationRestart(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList)
    {
        // Properties restart must be set as false
        String restart = validatorInputs.getApplication().getEndpointRestart();
        log.debug("[{}] -> [{}]: Get the restart property: [{}]", Constants.PROJECT_FILE_VALIDATOR, Constants.VALIDATE_APPLICATION, restart);

        if (restart != null && !restart.equalsIgnoreCase(Constants.FALSE))

        {
            log.debug("[{}] -> [{}]: EndPoint restart: [{}]", Constants.PROJECT_FILE_VALIDATOR, Constants.VALIDATE_APPLICATION, restart);
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PROPERTY_RESTART, Constants.INVALID_PROPERTY_RESTART_MSG);
        }
    }

    private void validateApplicationShutdown(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList)
    {
        // Properties shutdown must be set as false
        String shutdown = validatorInputs.getApplication().getEndpointShutdown();
        log.debug("[{}] -> [{}]: Get the shutdown property: [{}]", Constants.PROJECT_FILE_VALIDATOR, Constants.VALIDATE_APPLICATION, shutdown);

        if (shutdown != null && !shutdown.equalsIgnoreCase(Constants.FALSE))

        {
            log.debug("[{}] -> [{}]: EndPoint shutdown: [{}]", Constants.PROJECT_FILE_VALIDATOR, Constants.VALIDATE_APPLICATION, shutdown);
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PROPERTY_SHUTDOWN, Constants.INVALID_PROPERTY_SHUTDOWN_MSG);
        }
    }

    /**
     * Validates the bootstrap values
     *
     * @param validatorInputs Validation variables
     * @param serviceName     service name
     * @param errorList       List of validation errors.
     */
    private void validateBootstrap(ValidatorInputs validatorInputs, String serviceName, List<ValidationErrorDto> errorList)
    {
        log.debug("[{}] -> [{}]: ValidateBootstrap: GET bootstrap: [{}]", Constants.PROJECT_FILE_VALIDATOR, "validateBootstrap",
                validatorInputs.getBootstrap());

        // Validate the spring profile active property. It must be set as Local.
        if (validatorInputs.getBootstrap().isValid())
        {
            if (validatorInputs.getBootstrap().getActiveProfile() != null && !validatorInputs.getBootstrap().getActiveProfile().equals(Constants.ACTIVE_PROFILE))
            {
                ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_ACTIVE_PROFILE, Constants.INVALID_ACTIVE_PROFILE_MSG);
            }
        }
        else
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_BOOTSTRAP_FILE, Constants.INVALID_BOOTSTRAP_FILE_MSG);
        }

    }

    /**
     * Validate the specific parameters of nova.yml of a thin2 project
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param errorList       List of validation errors.
     */
    private void validateCellsNovaYml(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList)
    {
        // Ports
        if (!validatorInputs.getNovaYml().getPorts().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PORTS_CELLS, Constants.INVALID_PORTS_CELLS_MSG);
        }

        // Properties
        if (!validatorInputs.getNovaYml().getProperties().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PROPERTIES_CELLS, Constants.INVALID_PROPERTIES_CELLS_MSG);
        }

        // Build
        if (!validatorInputs.getNovaYml().getBuild().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_BUILD_CELLS, Constants.INVALID_BUILD_CELLS_MSG);
        }

        // Machines
        if (!validatorInputs.getNovaYml().getMachines().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_MACHINES_CELLS, Constants.INVALID_MACHINES_CELLS_MSG);
        }

        // Dependencies
        if (!validatorInputs.getNovaYml().getDependencies().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_DEPENDENCIES_CELLS, Constants.INVALID_DEPENDENCIES_CELLS_MSG);
        }

        // ApplicationName
        if (validatorInputs.getNovaYml().getApplicationName() == null || validatorInputs.getNovaYml().getApplicationName().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_APPLICATION_NAME_CELLS,
                    Constants.INVALID_APPLICATION_NAME_CELLS_MSG);
        }

        // Project Name
        if (validatorInputs.getNovaYml().getProjectName() == null || validatorInputs.getNovaYml().getProjectName().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PROJECT_NAME_CELLS, Constants.INVALID_PROJECT_NAME_CELLS_MSG);
        }

        // Context params
        if (!validatorInputs.getNovaYml().getContextParams().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_CONTEXT_PARAMS, Constants.INVALID_CONTEXT_PARAMS_MSG);
        }
        // Input params
        if (!validatorInputs.getNovaYml().getInputParams().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_INPUT_PARAMS, Constants.INVALID_INPUT_PARAMS_MSG);
        }
        // Output params
        if (!validatorInputs.getNovaYml().getOutputParams().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_OUTPUT_PARAMS, Constants.INVALID_OUTPUT_PARAMS_MSG);
        }
        // CDN can't define an BackToBack api
        if (ApiValidationUtils.hasNovaymlBackToBack(validatorInputs.getNovaYml()))
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.BATCH_CDN_DEFINING_BACKTOBACK_API,
                    Constants.BATCH_CDN_DEFINING_BACKTOBACK_API_MSG);
        }
    }

    /**
     * Validates the dockerfile values
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param serviceType     Service type
     * @param errorList       List of validaiton errors.
     */
    private void validateDockerFile(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, ServiceType serviceType,
                                    List<ValidationErrorDto> errorList)
    {

        log.debug("[{}] -> [{}]: ValidateDockerFile: GET Dockerfile: [{}]", Constants.PROJECT_FILE_VALIDATOR, "validateDockerFile",
                validatorInputs.getDockerfile());

        if (validatorInputs.getDockerfile().getDockerfileContent().isEmpty())
        {
            log.debug("Dockerfile not found");
        }
        else if (validatorInputs.getDockerfile().getDockerfileContent().size() != 4)
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.DOCKFILE_EDITED, Constants.DOCKFILE_EDITED_MSG);
        }
        else
        {
            checkDockerFile(validatorInputs, dto, serviceType, errorList);
        }
    }

    /**
     * Validates the general nova.yml parameters for any type of service
     *
     * @param validatorInputs Validation variables
     * @param serviceName     service name
     * @param serviceType     service type
     * @param product         to validate
     * @param errorList       List of validation errors.
     */
    private void validateGeneralNovaYml(ValidatorInputs validatorInputs, String serviceName, String serviceType, List<ValidationErrorDto> errorList,
                                        Product product)
    {
        // Nova Version
        if (ComparisonUtils.compareVersion(validatorInputs.getNovaYml().getNovaVersion(), novaYmlVersion, serviceName, "NOVA yml version", errorList) == 0)
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_NOVA_VERSION, Constants.INVALID_NOVA_VERSION_MSG);
        }

        // UUAA
        if (validatorInputs.getNovaYml().getUuaa().length() != uuaaLength)
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.WRONG_SIZED_NOVA_UUAA_CODE, Constants.WRONG_SIZED_NOVA_UUAA_CODE_MSG);
        }

        if (StringUtils.isEmpty(validatorInputs.getNovaYml().getUuaa()))
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.NO_NOVA_UUAA_CODE, Constants.NO_NOVA_UUAA_POM_MSG);
        }

        if (!validatorInputs.getNovaYml().getUuaa().equalsIgnoreCase(product.getUuaa()))
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_UUAA_NAME, Constants.INVALID_UUAA_MSG);
        }

        // Service name
        if (!PatternMatcher.patternazAZ09(validatorInputs.getNovaYml().getName()))
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_NOVA_SERVICE_NAME, Constants.INVALID_NOVA_SERVICE_NAME_MSG);
        }

        // Behavior ReleaseVersion
        if (ServiceType.isBehaviorTestService(serviceType))
        {
            if (StringUtils.isEmpty(validatorInputs.getNovaYml().getReleaseVersion()))
            {
                ErrorListUtils.addError(errorList, serviceName, Constants.NO_BEHAVIOR_RELEASE_VERSION_CODE, Constants.NO_BEHAVIOR_RELEASE_VERSION_MSG);
            }
        }

        // Language and language version
        switch (ServiceLanguage.getValueOf(validatorInputs.getNovaYml().getLanguage()))
        {
            case JAVA_SPRING_CLOUD_TASK:
            case JAVA_SPRING_BOOT:
            case JAVA_SPRING_BATCH:
            case JAVA:
                if (ComparisonUtils.compareVersion(validatorInputs.getNovaYml().getLanguageVersion(), this.javaVersion, serviceName, "JAVA version", errorList) == 0)
                {
                    ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_JAVA_LANGUAGE_VERSION,
                            Constants.INVALID_JAVA_LANGUAGE_VERSION_MSG);
                }
                break;
            case JAVA_SPRING_MVC:
            case JAVA_J2EE:
                if (ComparisonUtils.compareVersion(validatorInputs.getNovaYml().getLanguageVersion(), this.frontcatJavaVersion, serviceName, "JAVA version", errorList) == 0)
                {
                    ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_FRONTCAT_JAVA_LANGUAGE_VERSION,
                            Constants.INVALID_FRONTCAT_JAVA_LANGUAGE_VERSION_MSG);
                }
                break;
            case POLYMER_CELLS:
            case ANGULAR_THIN2:
            case ANGULAR_THIN3:
            case THIN2:
                if (!validatorInputs.getNovaYml().getLanguageVersion().equalsIgnoreCase(Constants.LATEST))
                {
                    ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_ANGULAR_LANGUAGE_VERSION,
                            Constants.INVALID_ANGULAR_LANGUAGE_VERSION_MSG);
                }
                break;
            case NODE_JS_EXPRESS:
            case NODE:
                if (ComparisonUtils.compareVersion(validatorInputs.getNovaYml().getLanguageVersion(), nodeJsVersion, serviceName, "NODE Version", errorList) == 0)
                {
                    ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_NODE_LANGUAGE_VERSION,
                            Constants.INVALID_NODE_LANGUAGE_VERSION_MSG);
                }
                break;
            case PYTHON_FLASK:
            case PYTHON:
                if (ComparisonUtils.compareVersion(validatorInputs.getNovaYml().getLanguageVersion(), pythonVersion, serviceName, "PYTHON version", errorList) == 0)
                {
                    ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_PYTHON_LANGUAGE_VERSION,
                            Constants.INVALID_PYTHON_LANGUAGE_VERSION_MSG);
                }
                break;
            case NOVA:
                log.debug("[{}] services type does not have language and language version. Continue.", serviceType);
                break;
            case TEMPLATE:
                if (ComparisonUtils.compareVersion(validatorInputs.getNovaYml().getLanguageVersion(), templateVersion, serviceName, "Language version", errorList) == 0)
                {
                    ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_TEMPLATE_LANGUAGE_VERSION,
                            Constants.INVALID_TEMPLATE_LANGUAGE_VERSION_MSG);
                }
                break;
            default:
                ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_LANGUAGE, Constants.INVALID_LANGUAGE_MSG);
        }
    }

    /**
     * Validation for pom.xml of any projects except EPhoenix
     *
     * @param validatorInputs inputs
     * @param dto             dto
     * @param errorList       error list
     */
    private void validateNonEPhoenixPom(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList)
    {
        String uuaaName;
        String novaVersion;

        // The latest version have some parameters on the nova.yml file instead of pom.xml
        if (validatorInputs.isLatestVersion())
        {
            uuaaName = validatorInputs.getNovaYml().getUuaa();
            // Plugin output directory
            if (validatorInputs.getPom().getPlugin() == null || !validatorInputs.getPom().getPlugin().equals(pluginOutputDirectory))
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_OUTPUT_DIRECTORY, Constants.INVALID_OUTPUT_DIRECTORY_MSG);
            }
        }
        else
        {
            uuaaName = validatorInputs.getPom().getUuaaName();
            novaVersion = validatorInputs.getPom().getNovaVersion();

            // UUAA
            if (StringUtils.isEmpty(validatorInputs.getPom().getUuaaName()))
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.NO_UUAA_CODE, Constants.NO_UUAA_POM_MSG);
            }

            if (validatorInputs.getPom().getUuaaName().length() != uuaaLength)
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.WRONG_SIZED_UUAA_CODE, Constants.WRONG_SIZED_UUAA_CODE_MSG);
            }

            // Plugin NOVA Starter version
            if (ComparisonUtils.compareVersion(validatorInputs.getPom().getPlugin(), novaPluginVersion, dto.getServiceName(), "pom version", errorList) == 0)
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_POM_PLUGIN_NOVA_STARTER_VERSION,
                        Constants.INVALID_POM_PLUGIN_NOVA_STARTER_VERSION_MSG);
            }
            // Nova version
            if (ComparisonUtils.compareVersion(novaVersion, currentNovaVersion, dto.getServiceName(), "NOVA Version", errorList) == 0)
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_POM_NOVA_VERSION, Constants.INVALID_POM_NOVA_VERSION_MSG);
            }
        }

        // Literals: Group Id, UUAA, artifact id, packaging, final name
        validateNonEPhoenixPomLiterals(validatorInputs, dto.getServiceName(), errorList, uuaaName);

        // Versions: Version service, parent version
        validateNonEPhoenixPomVersions(validatorInputs, dto.getServiceName(), errorList);

        // Node version
        this.usesSupportedNodeVersion(dto, errorList);
    }

    /**
     * Validate any non EPhoenix literals (group Id, UUAA, artifact id, packaging and final name) of the pom.xml
     *
     * @param validatorInputs validator inputs
     * @param serviceName     service name
     * @param errorList       list of error
     * @param uuaaName        uuaa
     */
    private void validateNonEPhoenixPomLiterals(ValidatorInputs validatorInputs, String serviceName, List<ValidationErrorDto> errorList,
                                                String uuaaName)
    {
        // Group Id
        if (StringUtils.isEmpty(validatorInputs.getPom().getGroupId()))
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.NO_GROUP_ID_CODE, Constants.NO_GROUP_ID_MSG);
        }
        else
        {
            if (!validatorInputs.getPom().getGroupId().toLowerCase().startsWith(Constants.GROUP_ID_PREFIX + uuaaName.toLowerCase()))
            {
                ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_POM_GROUP_ID, Constants.INVALID_POM_GROUP_ID_MSG);
            }

            if (!validatorInputs.getPom().getGroupId().matches(ServiceNamingUtils.REGEX_GROUP_ID))
            {
                ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_GROUP_ID_CODE, Constants.INVALID_GROUP_ID_MSG);
            }
        }

        // Artifact Id
        if (!PatternMatcher.patternazAZ09(validatorInputs.getPom().getArtifactId()))
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_POM_ARTIFACT_ID, Constants.INVALID_POM_ARTIFACT_ID_MSG);
        }

        // Packaging
        if (!novaPackaging.equals(validatorInputs.getPom().getPackaging()))
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_POM_PACKAGING, Constants.INVALID_POM_PACKAGING_MSG);
        }

        // Final Name
        String finalName = validatorInputs.getPom().getGroupId() + "-" + validatorInputs.getPom().getArtifactId();
        log.debug("[{}] -> [{}]: Final name detected: [{}]", Constants.PROJECT_FILE_VALIDATOR, Constants.VALIDATE_NON_EPHOENIX_POM, finalName);

        if (!finalName.equals(validatorInputs.getPom().getFinalName()))
        {
            log.debug("[{}] -> [{}]: Get final name from pom: [{}]", Constants.PROJECT_FILE_VALIDATOR, Constants.VALIDATE_NON_EPHOENIX_POM,
                    validatorInputs.getPom().getFinalName());
            ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_POM_FINAL_NAME, Constants.INVALID_POM_FINAL_NAME_MSG);
        }
        else
        {
            log.debug("[{}] -> [{}]: Final name OK. [{}]", Constants.PROJECT_FILE_VALIDATOR, Constants.VALIDATE_NON_EPHOENIX_POM, finalName);
        }

        // Unique artifact Id
        for (Map.Entry<String, Set<String>> entry : PomXML.getArtifactIds().entrySet())
        {
            if (entry.getValue().contains(Constants.ARTIFACT_ID_NAME_ERROR))
            {
                ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_POM_ARTIFACT_NOT_UNIQUE,
                        Constants.INVALID_POM_ARTIFACT_NOT_UNIQUE_MSG);
            }
        }
    }

    /**
     * Validate any non EPhoenix versions (version service, parent version and nova version) of the pom.xml
     *
     * @param validatorInputs validator inputs
     * @param serviceName     service name
     * @param errorList       list of error
     */
    private void validateNonEPhoenixPomVersions(ValidatorInputs validatorInputs, String serviceName, List<ValidationErrorDto> errorList)
    {
        // Parent version
        if (ComparisonUtils.compareVersion(validatorInputs.getPom().getParentVersion(), novaParentVersion, serviceName, "pom parent version", errorList) == 0)
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_POM_PARENT_VERSION, Constants.INVALID_POM_PARENT_VERSION_MSG);
        }

        // Version
        if (validatorInputs.getPom().getVersion() == null || !PatternMatcher.customPattern(com.bbva.enoa.platformservices.coreservice.common.Constants.PATTERN_VERSION_REGEX, validatorInputs.getPom().getVersion()))
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.INVALID_POM_VERSION, Constants.INVALID_POM_VERSION_MSG);
        }
    }

    /**
     * Validation of Nova.yml
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param errorList       List of validation errors.
     * @param product         product to validate
     */
    private void validateNovaYml(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList, int repoId,
                                 String tag, String releaseName, Product product)
    {
        switch (ServiceType.getValueOf(dto.getServiceType()))
        {
            case API_REST_JAVA_SPRING_BOOT:
            case API_JAVA_SPRING_BOOT:
            case API_REST_NODE_JS_EXPRESS:
            case API_REST_PYTHON_FLASK:
                this.validateApiRestAndBatchNovaYml(validatorInputs, dto, errorList);
                // API REST | API application must have at least one swagger or asyncapi backToFront that defines the service
                if (ApiValidationUtils.hasNotNovaymlAnApi(validatorInputs.getNovaYml()))
                {
                    ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.NO_API_DEFINED, Constants.NO_API_DEFINED_MSG);
                }
                break;
            case CDN_ANGULAR_THIN2:
            case CDN_ANGULAR_THIN3:
                this.validateThinNovaYml(validatorInputs, dto, errorList);
                break;
            case CDN_POLYMER_CELLS:
                this.validateCellsNovaYml(validatorInputs, dto, errorList);
                break;
            case DEPENDENCY:
                // Dependency  services can't consume an API
                if (!validatorInputs.getNovaYml().getApiConsumed().isEmpty())
                {
                    ErrorListUtils
                            .addError(errorList, dto.getServiceName(), Constants.DEPENDENCY_CONSUMING_API, Constants.DEPENDENCY_CONSUMING_API_MSG);
                }
                this.validateApiRestAndBatchNovaYml(validatorInputs, dto, errorList);
                // Dependency  services service can't define an api
                if (!validatorInputs.getNovaYml().getApiServed().isEmpty())
                {
                    ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.DAEMON_DEFINING_API, Constants.DAEMON_DEFINING_API_MSG);
                }
                break;
            case LIBRARY_JAVA:
                // Validate specific parameters for NOVA libraries
                this.libraryValidator.validateNovaLibraryNovaYml(validatorInputs, dto, errorList);
                // Validate pom.xml & nova.yml has the same values
                this.libraryValidator.validateNovaLibraryByPomXml(validatorInputs, dto, errorList);
                //Validate nova.yml changed requirements or properties incase of same fullname
                this.libraryValidator.validateNovaLibraryNewVersion(validatorInputs, dto, errorList);

                break;
            case LIBRARY_THIN2:
            case LIBRARY_NODE:
            case LIBRARY_PYTHON:
            case LIBRARY_TEMPLATE:
                // Validate specific parameters for NOVA libraries
                this.libraryValidator.validateNovaLibraryNovaYml(validatorInputs, dto, errorList);
                //Validate nova.yml changed requirements or properties incase of same fullname
                this.libraryValidator.validateNovaLibraryNewVersion(validatorInputs, dto, errorList);

                break;
            case BATCH_JAVA_SPRING_CLOUD_TASK:
            case BATCH_JAVA_SPRING_BATCH:
            case BATCH_PYTHON:
                // Batchs can't define an BackToBack api
                if (ApiValidationUtils.hasNovaymlBackToBack(validatorInputs.getNovaYml()))
                {
                    ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.BATCH_CDN_DEFINING_BACKTOBACK_API,
                            Constants.BATCH_CDN_DEFINING_BACKTOBACK_API_MSG);
                }
            case DAEMON_JAVA_SPRING_BOOT:
                this.validateApiRestAndBatchNovaYml(validatorInputs, dto, errorList);
                // Daemon and Batchs service can't define a Sync or BackToFront api
                if (ApiValidationUtils.hasNovaymlAnyApi(validatorInputs.getNovaYml()))
                {
                    ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.DAEMON_DEFINING_API,
                            Constants.DAEMON_DEFINING_API_MSG);
                }
                break;
            case FRONTCAT_JAVA_SPRING_MVC:
            case FRONTCAT_JAVA_J2EE:
                // Validate specific parameters for frontcat services ( Junction and contextPath properties are mandatory)
                this.validateFrontcatNovaYml(validatorInputs, dto, errorList);
                // Validate novaYml properties.
                this.validateNovaYmlProperties(validatorInputs, dto, errorList);
                break;
            case BATCH_SCHEDULER_NOVA:
                this.validateBatchSchedulerNovaYml(validatorInputs, dto, errorList);
                break;
            default:
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_SERVICE_TYPE, Constants.INVALID_SERVICE_TYPE_MSG);
        }

        // Validate all yml (Swagger & AsyncAPI) definitions inside the nova.yml
        errorList.addAll(
                this.apiResolverDefinitionValidator.validateAndAssociateApi(validatorInputs.getNovaYml(), dto, repoId, tag, releaseName)
        );

        this.libraryValidator.validateLibrariesFromNovaYml(validatorInputs, dto, tag, releaseName, errorList);
        this.validateGeneralNovaYml(validatorInputs, dto.getServiceName(), dto.getServiceType(), errorList, product);
    }

    /**
     * Validation of Nova.yml for behavior service
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param errorList       List of validation errors.
     * @param product         product to validate
     */
    private void validateBehaviorNovaYml(ValidatorInputs validatorInputs, BVServiceInfoDTO dto, List<ValidationErrorDto> errorList, int repoId,
                                         String tag, Product product)
    {
        // Validate all yml (Swagger & AsyncAPI) definitions inside the nova.yml
        errorList.addAll(
                this.apiDefinitionBehaviorValidator.validateAndAssociateApiBehavior(validatorInputs.getNovaYml(), dto, repoId, tag)
        );

        this.validateGeneralNovaYml(validatorInputs, dto.getServiceName(), dto.getServiceType(), errorList, product);
    }


    /**
     * Validates the nova.yml for Frontcat services
     * Mandatory to check that frontcat junction and contextPath are set.
     * Necessary to create the path url of application.
     * <p>
     * example:
     * frontcat:!
     * junction: FCAT
     * contextPath: qwje_multiweb_visor_01
     *
     * @param validatorInputs             Validation variables
     * @param newReleaseVersionServiceDto dto
     * @param errorList                   List of validation errors
     */
    private void validateFrontcatNovaYml(final ValidatorInputs validatorInputs, NewReleaseVersionServiceDto newReleaseVersionServiceDto, final List<ValidationErrorDto> errorList)
    {

        log.debug("[{}] -> [validateFrontcatNovaYml]: validating NovaYml for frontcat services, mandatory frontcat.junction: value[{}], and frontcat.contextPath: value [{}]", Constants.PROJECT_FILE_VALIDATOR, validatorInputs.getNovaYml().getJunction(), validatorInputs.getNovaYml().getContextPath());

        if (validatorInputs.getNovaYml().getJunction() == null || validatorInputs.getNovaYml().getJunction().isEmpty())
        {
            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), Constants.INVALID_FRONTCAT_JUNCTION, Constants.INVALID_FRONTCAT_JUNCTION_MSG);
        }

        if (validatorInputs.getNovaYml().getContextPath() == null || validatorInputs.getNovaYml().getContextPath().isEmpty())
        {
            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), Constants.INVALID_FRONTCAT_CONTEXTPATH, Constants.INVALID_FRONTCAT_CONTEXTPATH_MSG);
        }
    }

    /**
     * Validates the package.json values
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param errorList       List of validation errors.
     */
    private void validatePackageJson(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList)
    {

        log.debug("[{}] -> [{}]: validatePackageJson: GET package.json: [{}]", Constants.PROJECT_FILE_VALIDATOR, "validatePackageJson",
                validatorInputs.getPackageJson());

        // Dependencies
        if (validatorInputs.getPackageJson().getDependencies() != null && !validatorInputs.getPackageJson().getDependencies().isEmpty())
        {
            //Validate specific dependencies and versions.
            this.validateDependenciesVersions(dto.getServiceName(), validatorInputs.getPackageJson(), errorList);
        }
        else
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_DEPENDENCIES, Constants.INVALID_DEPENDENCIES_MSG);
        }

        // Name
        if (validatorInputs.getPackageJson().getName() == null || validatorInputs.getPackageJson().getName().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PACKAGE_NULL_NAME, Constants.INVALID_PACKAGE_NULL_NAME_MSG);
        }
        else if (validatorInputs.getPackageJson().getName() != null && validatorInputs.getNovaYml().getName() != null && !validatorInputs
                .getPackageJson().getName().equals(validatorInputs.getNovaYml().getName()))
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PACKAGE_NAME, Constants.INVALID_PACKAGE_NAME_MSG);
        }

        // Version
        if (validatorInputs.getPackageJson().getVersion() == null || validatorInputs.getPackageJson().getVersion().isEmpty())

        {
            ErrorListUtils
                    .addError(errorList, dto.getServiceName(), Constants.INVALID_PACKAGE_NULL_VERSION, Constants.INVALID_PACKAGE_NULL_VERSION_MSG);
        }
        else if (validatorInputs.getPackageJson().getVersion() != null && validatorInputs.getNovaYml().getVersion() != null && !validatorInputs
                .getPackageJson().getVersion().equals(validatorInputs.getNovaYml().getVersion()))
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PACKAGE_VERSION, Constants.INVALID_PACKAGE_VERSION_MSG);
        }
    }

    /**
     * Method that validate Service dependencies and added errors if its needed
     *
     * @param serviceName     serviceName
     * @param packageJsonFile packageJsonFile
     * @param errorList       errorList
     */
    private void validateDependenciesVersions(String serviceName, PackageJsonFile packageJsonFile, List<ValidationErrorDto> errorList)
    {
        //Constants Map with all dependencies with mandatory version in NOVA.
        Map<String, List<String>> dependenciesMap = Constants.PackageJsonDependencies.DEPENDENCY_VERSION_MAP;

        log.debug("[ProjectFileValidatorImpl] -> [validateDependenciesVersions] -> wanted dependency [{}]", dependenciesMap.toString());

        // list with all service dependencies
        List<String> allProjectDependencies = new ArrayList<>();
        allProjectDependencies.addAll(packageJsonFile.getDevDependencies());
        allProjectDependencies.addAll(packageJsonFile.getDependencies());

        for (String name : dependenciesMap.keySet())
        {
            //true if service pass the dependencies versions validations.
            boolean hasMinimumVersion = false;

            //first, check if the given dependency is on devDependencies Tag
            if (!packageJsonFile.getDevDependencies().isEmpty())
            {
                for (String stringValue : dependenciesMap.get(name))
                {
                    // compare dependency
                    hasMinimumVersion = hasMinimumVersion || this.isMinimumVersionNumber(name, stringValue, allProjectDependencies);
                }
            }

            //add error to list
            if (!hasMinimumVersion)
            {
                String errorMessage = Constants.PackageJsonDependencies.INVALID_DEPENDENCY_MSG + "[" + name + "]."
                        + Constants.PackageJsonDependencies.CORRECT_VERSION_MSG + "[" + dependenciesMap.get(name) + "]";
                ErrorListUtils.addError(errorList, "", Constants.PackageJsonDependencies.INVALID_DEPENDENCY_VERSION, errorMessage);

                log.error("[ProjectFileValidatorImpl] -> [validateDependenciesVersions] -> " +
                        "dependency [{}] does not satisfy the mandatory version [{}] in the service [{}] at package.json file", name, dependenciesMap.get(name), serviceName);
            }
        }
    }

    /**
     * Method to check dependency name, and then, version number.
     *
     * @param dependencyName    dependencyName
     * @param dependencyVersion dependencyVersion
     * @param dependenciesList  dependenciesList
     * @return boolean value. True if the list contains the specific dependency with the version
     */
    private boolean isMinimumVersionNumber(final String dependencyName, final String dependencyVersion, final List<String> dependenciesList)
    {
        boolean result =
                dependenciesList.stream().anyMatch(
                        dependency ->
                                dependencyName.equals(dependency.split(":")[0].replaceAll("[^a-zA-Z0-9]", ""))
                                        && Integer.parseInt(dependency.split(":")[1].replaceAll("[^0-9]", "")) >= Integer.parseInt(dependencyVersion.replace(".", "")));

        log.debug("[ProjectFileValidatorImpl] -> [isMinimumVersionNumber] -> checking dependency [{}] - version [{}] = [{}]", dependencyName, dependencyVersion, result);

        return result;
    }

    /**
     * Validates python requirements file content
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param errorList       List of validation errors.
     */
    private void validatePythonRequirements(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList)
    {
        if (log.isDebugEnabled())
        {
            log.debug("[{}] -> [{}]: validatePythonRequirements: GET requirements.in: [{}]", Constants.PROJECT_FILE_VALIDATOR, "validatePackageJson",
                    validatorInputs.getRequirements());
        }

        // Dependencies
        final List<String> dependencies = validatorInputs.getRequirements().getDependencies();

        if (dependencies == null || dependencies.isEmpty())
        {
            if (ServiceType.API_REST_PYTHON_FLASK.getServiceType().equals(dto.getServiceType()) || ServiceType.BATCH_PYTHON.getServiceType()
                    .equals(dto.getServiceType()))
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.NO_REQUIREMENTS_DEFINED, Constants.NO_REQUIREMENTS_DEFINED_MSG);
            }
            else
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_DEPENDENCIES, Constants.INVALID_DEPENDENCIES_MSG);
            }
        }
        else if (ServiceType.API_REST_PYTHON_FLASK.getServiceType().equals(dto.getServiceType()))
        {
            final boolean hasBaseDependency = dependencies.stream()
                    .anyMatch(dependency -> dependency.matches(Constants.PYTHON_FLASK_BASE_SERVICE_REGEX));

            if (!hasBaseDependency)
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_REQUIREMENTS_DEPENDENCIES,
                        Constants.INVALID_API_REST_PYTHON_FLASK_REQUIREMENTS_DEPENDENCIES_MSG);
            }
        }
        else if (ServiceType.BATCH_PYTHON.getServiceType().equals(dto.getServiceType()))
        {
            final boolean hasBaseDependency = dependencies.stream()
                    .anyMatch(dependency -> dependency.matches(Constants.PYTHON_BATCH_BASE_SERVICE_REGEX));

            if (!hasBaseDependency)
            {
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_REQUIREMENTS_DEPENDENCIES,
                        Constants.INVALID_BATCH_PYTHON_REQUIREMENTS_DEPENDENCIES_MSG);
            }
        }
    }

    /**
     * Validates the pom values
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param errorList       List of validaiton errors.
     */
    private void validatePom(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList)
    {
        log.debug("[{}] -> [{}]: ValidatePom: GET pom: [{}]", Constants.PROJECT_FILE_VALIDATOR, "validatePom", validatorInputs.getPom());

        this.validateNonEPhoenixPom(validatorInputs, dto, errorList);
    }

    /**
     * Validates the pom values
     *
     * @param validatorInputs Validation variables
     * @param dto             The Behavior service DTO.
     * @param errorList       List of validation errors.
     */
    private void validateBehaviorPom(ValidatorInputs validatorInputs, BVServiceInfoDTO dto, List<ValidationErrorDto> errorList)
    {
        log.debug("[{}] -> [{}]: ValidatePom: GET pom: [{}]", Constants.PROJECT_FILE_VALIDATOR, "validatePom", validatorInputs.getPom());

        String uuaaName = validatorInputs.getNovaYml().getUuaa();
        // Plugin output directory
        if (validatorInputs.getPom().getPlugin() == null || !validatorInputs.getPom().getPlugin().equals(pluginOutputDirectory))
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_OUTPUT_DIRECTORY, Constants.INVALID_OUTPUT_DIRECTORY_MSG);
        }

        // Literals: Group Id, UUAA, artifact id, packaging, final name
        validateNonEPhoenixPomLiterals(validatorInputs, dto.getServiceName(), errorList, uuaaName);

        // Versions: Version service, parent version
        validateNonEPhoenixPomVersions(validatorInputs, dto.getServiceName(), errorList);
    }

    /**
     * Check if the variables introduce to be validated are correct
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param errorList       List of validation errors.
     * @param product         product
     */
    private void validateProjectFiles(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList,
                                      int repoId, String tag, String releaseName, Product product)
    {
        if (validatorInputs.isLatestVersion())
        {
            // Get the service type
            ServiceType serviceType = ServiceType.getValueOf(dto.getServiceType());

            // Validator Nova.yml
            this.validateNovaYml(validatorInputs, dto, errorList, repoId, tag, releaseName, product);

            switch (serviceType)
            {
                case API_REST_JAVA_SPRING_BOOT:
                case API_JAVA_SPRING_BOOT:
                case BATCH_JAVA_SPRING_CLOUD_TASK:
                case BATCH_JAVA_SPRING_BATCH:
                case DAEMON_JAVA_SPRING_BOOT:
                    // Pom
                    this.validatePom(validatorInputs, dto, errorList);

                    // Application
                    this.validateApplication(validatorInputs, dto, serviceType, errorList);

                    // Bootstrap and Bootstrap Test
                    this.validateBootstrap(validatorInputs, dto.getServiceName(), errorList);
                    break;
                case CDN_ANGULAR_THIN2:
                case CDN_ANGULAR_THIN3:
                case API_REST_NODE_JS_EXPRESS:
                    this.validatePackageJson(validatorInputs, dto, errorList);
                    break;
                case API_REST_PYTHON_FLASK:
                case BATCH_PYTHON:
                    this.validatePythonRequirements(validatorInputs, dto, errorList);
                    break;
                case DEPENDENCY:
                case CDN_POLYMER_CELLS:
                case LIBRARY_JAVA:
                case LIBRARY_NODE:
                case LIBRARY_PYTHON:
                case LIBRARY_THIN2:
                case LIBRARY_TEMPLATE:
                    // 11/10/19 - Dun - Double check for the libraries use case:
                    // We are unsure whether there'll be base dependencies to validate on libraries or not

                    // There is nothing to validate into cells/dependency
                    // services
                    // There is nothing to validate into cells/dependency services
                    break;
                case BATCH_SCHEDULER_NOVA:
                    this.validateSchedulerYml(validatorInputs, dto, errorList, product);
                    break;
                case FRONTCAT_JAVA_SPRING_MVC:
                case FRONTCAT_JAVA_J2EE:
                    break;
                default:
                    ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_SERVICE_TYPE, Constants.INVALID_SERVICE_TYPE_MSG);
            }
        }
        else
        {
            // Get the service type
            ServiceType serviceType = ServiceType.getValueOf(dto.getServiceType());

            if (!serviceType.isEPhoenix() && serviceType != DEPENDENCY)
            {

                // Validators
                // Bootstrap and Bootstrap Test
                this.validateBootstrap(validatorInputs, dto.getServiceName(), errorList);

                // Application
                this.validateApplicationOldServices(validatorInputs, dto, serviceType, errorList);

                // Dockerfile
                this.validateDockerFile(validatorInputs, dto, serviceType, errorList);

                // Pom
                this.validatePom(validatorInputs, dto, errorList);
            }
        }
    }

    /**
     * Check if the variables introduce to be validated are correct
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param errorList       List of validation errors.
     * @param product         product to validate
     */
    private void validateBehaviorProjectFiles(ValidatorInputs validatorInputs, BVServiceInfoDTO dto, List<ValidationErrorDto> errorList,
                                              int repoId, String tag, Product product)
    {
        // Get the service type
        ServiceType serviceType = ServiceType.getValueOf(dto.getServiceType());

        // Validator Nova.yml
        this.validateBehaviorNovaYml(validatorInputs, dto, errorList, repoId, tag, product);

        switch (serviceType)
        {
            case BUSINESS_LOGIC_BEHAVIOR_TEST_JAVA:
                this.validateBehaviorPom(validatorInputs, dto, errorList);

                // Application
                this.validateBehaviorApplication(validatorInputs, dto, serviceType, errorList);

                // Bootstrap and Bootstrap Test
                // this.validateBootstrap(validatorInputs, dto.getServiceName(), errorList);
                break;
            default:
                ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_SERVICE_TYPE, Constants.INVALID_SERVICE_TYPE_MSG);
        }

    }

    /**
     * @param validatorInputs             Input values to validateServiceProjectFiles
     * @param errorList                   Error list
     * @param newReleaseVersionServiceDto release version service dto
     * @param product                     product to validate
     */
    private void validateSchedulerYml(ValidatorInputs validatorInputs, final NewReleaseVersionServiceDto newReleaseVersionServiceDto,
                                      List<ValidationErrorDto> errorList, Product product)
    {
        int batchLimitRepeat = product.getBatchLimitRepeat();
        ParsedInfo parsedInfo = validatorInputs.getParsedInfo();
        if (parsedInfo == null)
        {
            ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), Constants.SCHEDULER_FILE_ERRORS,
                    validatorInputs.getSchedulerYmlMessage());
        }
        else
        {
            List<String> novaYmlContextParamNames = new ArrayList<>();
            List<String> schedulerYmlRelatedParamNames = new ArrayList<>();

            // [1] Fill the nova yml context params names
            for (NovaYamlContextParam novaYamlContextParam : validatorInputs.getNovaYml().getContextParams())
            {
                novaYmlContextParamNames.add(novaYamlContextParam.getName());
            }

            // [2] Review all related context params from INIT and find them into nova yml
            if (!parsedInfo.getInit().getTriggers().stream().flatMap(t -> t.getRelatedContexParams().stream())
                    .allMatch(novaYmlContextParamNames::contains))
            {
                ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), Constants.NOT_FOUND_INIT_CONTEXT_PARAM,
                        Constants.NOT_FOUND_INIT_CONTEXT_PARAM_MSG);
            }

            // [3] Fill the scheduler yml related context params from STEPS (input params and output params related context param)
            parsedInfo.getSteps().getStepsMap().entrySet().stream()
                    .flatMap(entry -> entry.getValue().getJobs().stream())
                    .forEachOrdered(job ->
                    {
                        // NOVA-3430: new scheduler.yml syntax  /  extract "INPUT"/"OUTPUT" to constants
                        schedulerYmlRelatedParamNames.addAll(getRelatedContextParams(job.getInputVars(), "INPUT"));
                        schedulerYmlRelatedParamNames.addAll(getRelatedContextParams(job.getOutputVars(), "OUTPUT"));
                    });

            // [4] Find all context params (from nova.yml) into related context param list (from scheduler.yml)
            if (schedulerYmlRelatedParamNames.isEmpty())
            {
                log.debug("[ProjectFileValidatorImpl] -> [validateSchedulerYml]: the scheduler yml related context params is empty");
            }
            else
            {
                if (schedulerYmlRelatedParamNames.stream().noneMatch(novaYmlContextParamNames::contains))
                {
                    ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), Constants.NOT_FOUND_STEP_CONTEXT_PARAM, Constants.NOT_FOUND_STEP_CONTEXT_PARAM_MSG);
                }
            }

            // [5] Validate Schedule repeat batch
            this.validateSchedulerBatchRepeat(parsedInfo, errorList, newReleaseVersionServiceDto, batchLimitRepeat);


        }
    }

    /**
     * Validate if scheduler.yml contains the same batch repeated more than n times
     *
     * @param parsedInfo                  Parsed Info (from scheduler yml in the batch scheduler service)
     * @param errorList                   list of errors
     * @param newReleaseVersionServiceDto release version service dto
     * @param batchLimitRepeat            limit of batch service repetition
     */
    private void validateSchedulerBatchRepeat(final ParsedInfo parsedInfo,
                                              List<ValidationErrorDto> errorList, final NewReleaseVersionServiceDto newReleaseVersionServiceDto, final int batchLimitRepeat)
    {
        List<String> batchNamesList = new ArrayList<>();

        parsedInfo.getSteps()
                .getStepsMap()
                .entrySet()
                .stream()
                .flatMap(entry -> entry.getValue().getJobs().stream())
                .forEachOrdered(job ->
                {
                    if (job.getType().equalsIgnoreCase("batch"))
                    {
                        batchNamesList.add(job.getServiceName());
                    }
                });

        Set<String> set = new HashSet<>(batchNamesList);
        for (String batchName : set)
        {
            if (Collections.frequency(batchNamesList, batchName) > batchLimitRepeat)
            {
                ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), Constants.INVALID_BATCH_REPEAT,
                        Constants.INVALID_BATCH_REPEAT_MSG + batchName + "' is used " + batchNamesList.size() + " times. No more than " + batchLimitRepeat + " ocurrences are allowed");
            }
        }

    }


    /**
     * Extract related context param names from input/output jobParams
     *
     * @param paramsMap input/output jobParams map
     * @param type      type (input/output) of the job param to extract context param name
     * @return list of related context param names
     */
    private List<String> getRelatedContextParams(HashMap<String, String> paramsMap, String type)
    {
        List<String> contextParamsList = new ArrayList<>();

        for (Map.Entry<String, String> mapEntry : paramsMap.entrySet())
        {
            // Obtain jobParam/contextParam names from yml
            String contextName;

            // extract to constants
            if ("INPUT".equals(type))
            {
                String[] auxContextName = mapEntry.getValue().split("ctx\\.");
                contextName = auxContextName[1];
            }
            else  // OUTPUT
            {
                String[] auxContextName = mapEntry.getKey().split("ctx\\.");
                contextName = auxContextName[1];
            }

            contextParamsList.add(contextName);
        }

        return contextParamsList;
    }

    /**
     * Validate if nova.yml and pom.xml has the same service name
     *
     * @param serviceName        service name
     * @param serviceTypeVersion service type
     * @param novaYml            nova yml
     * @param pomXML             pom xml
     * @param errorList          list of errors
     */
    private void validateSameServiceName(final String serviceName, final String serviceTypeVersion, final NovaYml novaYml, final PomXML pomXML,
                                         List<ValidationErrorDto> errorList)
    {
        ServiceType serviceType = ServiceType.getValueOf(serviceTypeVersion);
        if (serviceType.isPomXml() && serviceType.isNovaYml() && !novaYml.getName().equals(pomXML.getName()))
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.WRONG_SERVICE_NAME_CODE,
                    Constants.WRONG_SERVICE_NAME_MSG);
        }
    }

    /**
     * Validate if nova.yml and pom.xml has the same version
     *
     * @param serviceName        service name
     * @param serviceTypeVersion service type
     * @param novaYml            nova yml
     * @param pomXML             pom xml
     * @param errorList          list of errors
     */
    private void validateSameVersion(final String serviceName, final String serviceTypeVersion, final NovaYml novaYml, final PomXML pomXML,
                                     List<ValidationErrorDto> errorList)
    {
        ServiceType serviceType = ServiceType.getValueOf(serviceTypeVersion);

        if (serviceType.isPomXml() && serviceType.isNovaYml() && !novaYml.getVersion().equals(pomXML.getVersion()))
        {
            ErrorListUtils.addError(errorList, serviceName, Constants.WRONG_SERVICE_VERSION_CODE,
                    Constants.WRONG_SERVICE_VERSION_MSG);
        }
    }

    /**
     * Validate, depending on the service type, TEMPLATE( NOVA...) or modules
     *
     * @param newReleaseVersionServiceDto newReleaseVersionServiceDto
     * @param projectPath                 Path of the project
     * @param repoId                      Id of the repository in gitlab
     * @param tag                         Tag
     * @param ivUser                      iv-User
     * @param errorList                   List of error
     */
    private void validateSpecialFilesByServiceType(NewReleaseVersionServiceDto newReleaseVersionServiceDto, String projectPath, int repoId,
                                                   String tag, String ivUser, List<ValidationErrorDto> errorList)
    {
        ServiceType serviceType = ServiceType.getValueOf(newReleaseVersionServiceDto.getServiceType());
        switch (serviceType)
        {
            // For NOVA, NOVA BATCH, NOVA SPRING BATCH and THIN2 service type the template is validated
            case NOVA:
            case NOVA_BATCH:
            case THIN2:
            case NOVA_SPRING_BATCH:
                if (Boolean.TRUE.equals(newReleaseVersionServiceDto.getIsService()))
                {
                    this.isServiceNovaYmlPropertiesValid(newReleaseVersionServiceDto.getServiceName(), projectPath, repoId, tag, serviceType.name(), ivUser, errorList);
                }
                break;
            // For EPHOENIX BATCH and ONLINE and NODE service type the template and modules are validated
            case EPHOENIX_BATCH:
            case EPHOENIX_ONLINE:
            case NODE:
                if (Boolean.TRUE.equals(newReleaseVersionServiceDto.getIsService()))
                {
                    this.isServiceNovaYmlPropertiesValid(newReleaseVersionServiceDto.getServiceName(), projectPath + Constants.EPHOENIX_OR_NODE_TEMPLATE_YML_PATH, repoId, tag,
                            serviceType.name(), ivUser, errorList);
                }
                if (newReleaseVersionServiceDto.getModules() == null || newReleaseVersionServiceDto.getModules().length == 0)
                {
                    ErrorListUtils.addError(errorList, newReleaseVersionServiceDto.getServiceName(), Constants.NO_MODULES, Constants.NO_MODULES_MSG);
                }
                break;
            // DEPENDENCY does not have template or any other special file
            case DEPENDENCY:
            case FRONTCAT_JAVA_SPRING_MVC:
            case FRONTCAT_JAVA_J2EE:
                break;
            default:
                log.debug("[{}] -> [{}]: This service type: [{}] - release version " + "service dto: [{}] does not have template.yml file.",
                        Constants.PROJECT_FILE_VALIDATOR, "validateSpecialFilesByServiceType", serviceType, newReleaseVersionServiceDto);
                break;
        }
    }

    /**
     * Validate the specific parameters of nova.yml of a thin2 or thin3 project
     *
     * @param validatorInputs Validation variables
     * @param dto             The service DTO.
     * @param errorList       List of validation errors.
     */
    private void validateThinNovaYml(ValidatorInputs validatorInputs, NewReleaseVersionServiceDto dto, List<ValidationErrorDto> errorList)
    {
        // Ports
        if (!validatorInputs.getNovaYml().getPorts().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_PORTS_THIN, Constants.INVALID_PORTS_THIN_MSG);
        }

        // Build
        if (!validatorInputs.getNovaYml().getBuild().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_BUILD_THIN, Constants.INVALID_BUILD_THIN_MSG);
        }

        // Machines
        if (!validatorInputs.getNovaYml().getMachines().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_MACHINES_THIN, Constants.INVALID_MACHINES_THIN_MSG);
        }

        // Dependencies
        if (!validatorInputs.getNovaYml().getDependencies().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_DEPENDENCIES_THIN, Constants.INVALID_DEPENDENCIES_THIN_MSG);
        }

        // Context params
        if (!validatorInputs.getNovaYml().getContextParams().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_CONTEXT_PARAMS, Constants.INVALID_CONTEXT_PARAMS_MSG);
        }

        // Input params
        if (!validatorInputs.getNovaYml().getInputParams().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_INPUT_PARAMS, Constants.INVALID_INPUT_PARAMS_MSG);
        }

        // Output params
        if (!validatorInputs.getNovaYml().getOutputParams().isEmpty())
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.INVALID_OUTPUT_PARAMS, Constants.INVALID_OUTPUT_PARAMS_MSG);
        }
        // CDN can't define an BackToBack api
        if (ApiValidationUtils.hasNovaymlBackToBack(validatorInputs.getNovaYml()))
        {
            ErrorListUtils.addError(errorList, dto.getServiceName(), Constants.BATCH_CDN_DEFINING_BACKTOBACK_API,
                    Constants.BATCH_CDN_DEFINING_BACKTOBACK_API_MSG);
        }
        // Properties
        this.validateNovaYmlProperties(validatorInputs, dto, errorList);

    }

    /**
     * Check if the last sonar analysis was performed with sonar 5
     *
     * @param dto           DTO from a ReleaseVersionService
     * @param subsystemName Subsystem name
     * @param subsystemType Subsystem type
     */
    private void checkLastSonarAnalysis(NewReleaseVersionServiceDto dto, String subsystemName, SubsystemType subsystemType)
    {
        log.debug("[ProjectFileValidatorImpl] -> [checkLastSonarAnalysis]: Check last sonar analysis");

        if (!Boolean.TRUE.equals(dto.getIsImageInRegistry()) || !QualityUtils.hasQualityAnalysis(ServiceType.getValueOf(dto.getServiceType())))
        {
            dto.setHasForcedCompilationNewSonarVersion(false);
            return;
        }

        String sonarProjectName;
        if (SubsystemType.EPHOENIX.equals(subsystemType))
        {
            // Skip forcing compilation for Ephoenix subsystem
            dto.setHasForcedCompilationNewSonarVersion(false);
            return;
        }
        else
        {
            // Build name for Case NOVA
            sonarProjectName = dto.getServiceName() + "_" + dto.getUuaa().toUpperCase() + "_" + subsystemName.toLowerCase();
        }

        // Call quality assurance client to get sonar version
        boolean sonarVersionHigher5 = qualityAssuranceClient.checkIfExistAndHigherSonar5(sonarProjectName, dto.getVersion());

        if (sonarVersionHigher5)
        {
            dto.setHasForcedCompilationNewSonarVersion(false);
        }
        else
        {
            dto.setHasForcedCompilation(true);
            dto.setHasForcedCompilationNewSonarVersion(true);
        }
    }

    /**
     * Check if the last sonar analysis was performed with sonar 5
     *
     * @param dto           DTO from a Behavior Service
     * @param subsystemName Subsystem name
     */
    private void behaviorCheckLastSonarAnalysis(BVServiceInfoDTO dto, String subsystemName)
    {
        log.debug("[ProjectFileValidatorImpl] -> [checkLastSonarAnalysis]: Check last sonar analysis");

        if (!Boolean.TRUE.equals(dto.getIsImageInRegistry()) || !QualityUtils.hasQualityAnalysis(ServiceType.getValueOf(dto.getServiceType())))
        {
            dto.setHasForcedCompilationNewSonarVersion(false);
            return;
        }

        // Call quality assurance client to get sonar version
        String sonarProjectName = dto.getServiceName() + "_" + dto.getUuaa().toUpperCase() + "_" + subsystemName.toLowerCase();
        boolean sonarVersionHigher5 = qualityAssuranceClient.checkIfExistAndHigherSonar5(sonarProjectName, dto.getVersion());

        if (sonarVersionHigher5)
        {
            dto.setHasForcedCompilationNewSonarVersion(false);
        }
        else
        {
            dto.setHasForcedCompilation(true);
            dto.setHasForcedCompilationNewSonarVersion(true);
        }
    }
}
