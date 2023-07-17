package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVMetadataDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.SubsystemTagDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.PomXML;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IProjectFileValidator;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces.IServiceDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ErrorListUtils;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ProjectFileReader;
import com.bbva.enoa.utils.schedulerparserlib.Parser;
import com.bbva.enoa.utils.schedulerparserlib.exceptions.SchedulerParserException;
import com.bbva.enoa.utils.schedulerparserlib.parameters.BuilderCreator;
import com.bbva.enoa.utils.schedulerparserlib.parameters.Parameters;
import com.bbva.enoa.utils.schedulerparserlib.parameters.ParametersNova;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.GROUP_ID_PREFIX;

/**
 * Service DTO builder
 * Builds the DTO of a service
 */
@Service
@Slf4j
public class ServiceDtoBuilderImpl implements IServiceDtoBuilder
{
    /**
     * Client of the VCS API.
     */
    @Autowired
    private IVersioncontrolsystemClient iVersioncontrolsystemClient;
    /**
     * Validates if a service is NOVA compliant.
     */
    @Autowired
    private IProjectFileValidator iProjectFileValidator;

    @Override
    public List<NewReleaseVersionServiceDto> buildServicesFromSubsystemTag(
            int repoId,
            SubsystemTagDto tag,
            SubsystemType subsystemNovaType,
            final String ivUser,
            final String releaseName, Product product,
            String subsystemName)
    {
        //List of services.
        List<NewReleaseVersionServiceDto> services = new ArrayList<>();

        log.info("Getting project paths from repo: {}, tag: {}", repoId, tag.getTagName());
        // Get names of projects that have a pom.xml, from Product VCS repo and tag.
        List<String> projectNames = this.iVersioncontrolsystemClient.getProjectsPathsFromRepoTag(repoId, tag.getTagName());
        log.info("Projects: {}", projectNames);
        if (projectNames.isEmpty())
        {
            log.warn("[{}] -> [{}]: There are no projects in repo: [{}] and tag: [{}], aborting",
                    Constants.SUBSYSTEM_DTO_BUILDER, "buildServicesFromSubsystemTag", repoId, tag);
        }
        else
        {
            // Get dependency file
            this.validateDependencyFile(repoId, tag, projectNames);
            // For each one:
            for (String name : projectNames)
            {
                // Build each service info from VCS.
                NewReleaseVersionServiceDto service = this.serviceDtoBuilder(repoId, tag, name, subsystemNovaType,
                        ivUser, releaseName, product, subsystemName);

                // Add it.
                services.add(service);
            }
            // Clear artifactId names, this list is used to check if all artifact Ids are unique
            PomXML.clearArtifactIdList();
        }
        // Return as an array.
        return services;
    }

    /**
     * Builds a NewReleaseVersionServiceDto from the data of its pom.xml.
     * <p>
     * This includes:
     * <p>
     * - Maven coordinates.
     * - Service type in NOVA.
     * - Properties.
     * - Metadata.
     *
     * @param releaseName     Name of the release
     * @param validatorInputs validator inputs with the pom.xml and nova.yml parameters
     * @return NewReleaseVersionServiceDto.
     */
    public NewReleaseVersionServiceDto buildServiceGeneralInfo(String releaseName, ValidatorInputs validatorInputs)
    {
        NewReleaseVersionServiceDto service = null;
        if (validatorInputs.getPom() != null)
        {
            // Create the service DTO
            if (validatorInputs.isLatestVersion())
            {
                service = this.createServiceFromNovaYml(releaseName, validatorInputs);
            }
            else if (validatorInputs.getPom().isPomValid())
            {
                service = this.createServiceFromPom(validatorInputs.getPom());
            }
        }

        // And return the service.
        return service;
    }

    public void buildServiceGenerics(String[] modules, String tagUrl, String projectPath, NewReleaseVersionServiceDto serviceDto)
    {
        // Set the folder the service sources are in the repo.
        serviceDto.setFolder(projectPath);
        ServiceType serviceType = ServiceType.getValueOf(serviceDto.getServiceType());
        switch (serviceType)
        {
            case API_REST_JAVA_SPRING_BOOT:
            case API_JAVA_SPRING_BOOT:
            case BATCH_JAVA_SPRING_CLOUD_TASK:
            case BATCH_JAVA_SPRING_BATCH:
            case FRONTCAT_JAVA_SPRING_MVC:
            case FRONTCAT_JAVA_J2EE:
            case DAEMON_JAVA_SPRING_BOOT:
            case LIBRARY_JAVA:
                // Set the URL to the project definition file - aka pom.xml
                // in the repo and tag used in the release version.
                serviceDto.setProjectDefinitionUrl(tagUrl + Constants.SEPARATOR + projectPath + Constants.SEPARATOR +
                        Constants.POM_XML);
                serviceDto.setNovaYamlUrl(tagUrl + Constants.SEPARATOR + projectPath + Constants.SEPARATOR +
                        Constants.NOVA_YML);
                break;
            case CDN_ANGULAR_THIN2:
            case CDN_ANGULAR_THIN3:
            case API_REST_NODE_JS_EXPRESS:
            case LIBRARY_THIN2:
            case LIBRARY_NODE:
                // Set the URL to the project definition file - aka package.json
                // in the repo and tag used in the release version.
                serviceDto.setProjectDefinitionUrl(tagUrl + Constants.SEPARATOR + projectPath + Constants.SEPARATOR +
                        Constants.PACKAGE_JSON);
                serviceDto.setModules(modules);
                serviceDto.setNovaYamlUrl(tagUrl + Constants.SEPARATOR + projectPath + Constants.SEPARATOR +
                        Constants.NOVA_YML);
                break;
            case CDN_POLYMER_CELLS:
            case LIBRARY_TEMPLATE:
                // Cells project are defined only by the nova.yml
                serviceDto.setNovaYamlUrl(tagUrl + Constants.SEPARATOR + projectPath + Constants.SEPARATOR +
                        Constants.NOVA_YML);
                break;
            case API_REST_PYTHON_FLASK:
            case BATCH_PYTHON:
            case LIBRARY_PYTHON:
                serviceDto.setProjectDefinitionUrl(tagUrl + Constants.SEPARATOR + projectPath + Constants.SEPARATOR +
                        Constants.PYTHON_REQUIREMENTS);
                serviceDto.setModules(modules);
                serviceDto.setNovaYamlUrl(tagUrl + Constants.SEPARATOR + projectPath + Constants.SEPARATOR +
                        Constants.NOVA_YML);
                break;
            case BATCH_SCHEDULER_NOVA:
                // Cells project are defined only by the nova.yml
                serviceDto.setNovaYamlUrl(tagUrl + Constants.SEPARATOR + projectPath + Constants.SEPARATOR +
                        Constants.NOVA_YML);
                serviceDto.setProjectDefinitionUrl(tagUrl + Constants.SEPARATOR + projectPath + Constants.SEPARATOR +
                        Constants.SCHEDULER_YML);
                break;
            default:
                // Set the URL to the project definition file - aka pom.xml
                // in the repo and tag used in the release version.
                serviceDto.setProjectDefinitionUrl(tagUrl + Constants.SEPARATOR + projectPath + Constants.SEPARATOR +
                        Constants.POM_XML);
        }

        // By default, the force compilation is set to true except BATCH SCHEDULER Service
        serviceDto.setHasForcedCompilation(ServiceType.BATCH_SCHEDULER_NOVA != serviceType);
    }

    ///////////////////////////////////////////////   PRIVATE METHODS   \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    /**
     *
     */
    private void buildServiceInfo(final NewReleaseVersionServiceDto service, final ValidatorInputs validatorInputs)
    {
        // Set NOVA type.
        final NovaYml novaYml = validatorInputs.getNovaYml();
        String novaType = novaYml.getServiceType();
        if (!StringUtils.isEmpty(novaType))
        {
            // Set type.
            service.setServiceType(novaType.toUpperCase(Locale.getDefault()));
            this.setIsService(service);
        }
        else
        {
            // Modules without nova.type will be treated as dependencies.
            service.setServiceType(ServiceType.DEPENDENCY.name());
            service.setIsService(false);
        }

        // Set NOVA version.
        String novaVersion = novaYml.getNovaVersion();
        service.setNovaVersion(novaVersion);

        // Set service UUAA.
        String serviceUuaa = novaYml.getUuaa();
        service.setUuaa(serviceUuaa);

        final String jdkVersion = novaYml.getJdkVersion();
        if (jdkVersion != null && !"".equals(jdkVersion))
        {
            service.setJdkVersion(jdkVersion);
            service.setJvmVersion(novaYml.getLanguageVersion());
        }
    }

    /**
     * Read and fulfill the values under validation into a buildValidationFileInputs object
     *
     * @param projectPath Project Path
     * @param repoId      Repository Id
     * @param tag         Tag
     * @return validationInput with the values to be validated
     */
    public ValidatorInputs buildValidationFileInputs(String projectPath, int repoId, String tag)
    {
        ValidatorInputs validatorInputs = new ValidatorInputs();

        // Get nova.yml from service, repo and tag
        byte[] novaFile = this.iVersioncontrolsystemClient.getNovaYmlFromProject(projectPath, repoId, tag);

        // Get pom.xml from service, repo and tag.
        byte[] pomFile = this.iVersioncontrolsystemClient.getPomFromProject(projectPath, repoId, tag);

        // Check if nova.yml exists. If it exists is an up to date version
        if (novaFile == null || novaFile.length < 1)
        {
            // Pom.yml is a mandatory file, there is nothing to do without it
            if (pomFile == null || pomFile.length == 0)
            {
                log.warn("[{}] -> [{}]: Project: [{}] has no pom.xml file, aborting", Constants.SUBSYSTEM_DTO_BUILDER,
                        "buildValidationFileInputs", projectPath);
                return validatorInputs;
            }
            this.oldReadAndScanProjectFiles(projectPath, repoId, tag, pomFile, validatorInputs);
        }
        else
        {
            this.newReadAndScanProjectFiles(projectPath, repoId, tag, pomFile, novaFile, validatorInputs);
        }
        return validatorInputs;
    }

    /**
     * Creates a new ReleaseServiceDto from a pom.xml and nova.yml
     *
     * @param releaseName     Name of the release
     * @param validatorInputs validator inputs with the pom.xml and nova.yml parameters
     * @return the release service dto
     */
    private NewReleaseVersionServiceDto createServiceFromNovaYml(String releaseName, ValidatorInputs validatorInputs)
    {
        // Create the DTO.
        NewReleaseVersionServiceDto service = new NewReleaseVersionServiceDto();

        PomXML pomXML = validatorInputs.getPom();
        NovaYml novaYml = validatorInputs.getNovaYml();
        String uuaa = novaYml.getUuaa().toLowerCase();
        String name = novaYml.getName().toLowerCase();
        switch (ServiceType.getValueOf(novaYml.getServiceType()))
        {
            case API_REST_JAVA_SPRING_BOOT:
            case API_JAVA_SPRING_BOOT:
            case BATCH_JAVA_SPRING_CLOUD_TASK:
            case BATCH_JAVA_SPRING_BATCH:
            case DAEMON_JAVA_SPRING_BOOT:
            case FRONTCAT_JAVA_SPRING_MVC:
            case FRONTCAT_JAVA_J2EE:
            case DEPENDENCY:
                // Set final name.
                service.setFinalName(this.getFinalNameFromPom(pomXML));
                // Group ID.
                service.setGroupId(validatorInputs.getPom().getGroupId());
                break;
            case CDN_ANGULAR_THIN2:
            case CDN_ANGULAR_THIN3:
            case CDN_POLYMER_CELLS:
            case API_REST_NODE_JS_EXPRESS:
            case API_REST_PYTHON_FLASK:
            case BATCH_PYTHON:
            case BATCH_SCHEDULER_NOVA:
                // Set final name.
                service.setFinalName(GROUP_ID_PREFIX + uuaa + "-" + name);
                // Group ID.
                service.setGroupId(GROUP_ID_PREFIX + uuaa);
                break;
            case LIBRARY_JAVA:
            case LIBRARY_THIN2:
            case LIBRARY_NODE:
            case LIBRARY_PYTHON:
            case LIBRARY_TEMPLATE:
                service.setFinalName(uuaa + "-" + releaseName.toLowerCase()
                        + "-" + name + "-" + novaYml
                        .getVersion());
                // Group ID.
                service.setGroupId(GROUP_ID_PREFIX + uuaa);
                break;
            default:
        }

        // Extract artifact ID.
        service.setArtifactId(novaYml.getName());

        // Version.
        service.setVersion(novaYml.getVersion());

        // Add description service from nova yml.
        service.setDescription(novaYml.getDescription());

        // Add name service from pom.
        if (novaYml.getName() != null)
        {
            service.setServiceName(novaYml.getName());
        }
        else
        {
            service.setServiceName(pomXML.getArtifactId());
        }

        // Add NOVA properties.
        this.buildServiceInfo(service, validatorInputs);

        return service;
    }

    /**
     * Creates a new ReleaseServiceDto from a pom.xml
     *
     * @param pom pom.xml model
     * @return the release service dto
     */
    private NewReleaseVersionServiceDto createServiceFromPom(PomXML pom)
    {
        // Create the DTO.
        NewReleaseVersionServiceDto service = new NewReleaseVersionServiceDto();

        // Extract artifact ID.
        service.setArtifactId(pom.getArtifactId());

        // Version.
        service.setVersion(pom.getVersion());

        // Group ID.
        service.setGroupId(pom.getGroupId());

        // Add description service from pom.
        service.setDescription(pom.getDescription());

        // Add name service from pom.
        if (StringUtils.isEmpty(pom.getName()))
        {
            service.setServiceName(pom.getArtifactId());
        }
        else
        {
            service.setServiceName(pom.getName());
        }

        // Set final name.
        service.setFinalName(this.getFinalNameFromPom(pom));


        // Add NOVA properties.
        this.processProperties(service, pom.getProperties());

        // Extract modules if needed.
        if ((ServiceType.EPHOENIX_BATCH.name().equalsIgnoreCase(service.getServiceType())) ||
                (ServiceType.EPHOENIX_ONLINE.name().equalsIgnoreCase(service.getServiceType())))
        {
            // Process ephoenix modules.
            this.processModules(service, pom.getModules());

        }
        return service;
    }

    private String getFinalNameFromPom(PomXML pom)
    {
        if (Strings.isNullOrEmpty(pom.getFinalName()))
        {
            return pom.getGroupId() + '-' + pom.getArtifactId();
        }
        else
        {
            return pom.getFinalName();
        }
    }

    /**
     * Extracts one property, creates a new metadata object
     * and stores it into the metadata list.
     *
     * @param metadataList    List of MetadataDto.
     * @param modelProperties pom properties.
     * @param propertyName    property name.
     */
    private void extractProperty(
            List<RVMetadataDTO> metadataList,
            Properties modelProperties,
            final String propertyName)
    {
        // Check for property name.
        if (StringUtils.hasText(modelProperties.getProperty(propertyName)))
        {
            // Add metadata.
            RVMetadataDTO metadataDto = new RVMetadataDTO();
            metadataDto.setKey(propertyName);
            metadataDto.setValue(modelProperties.getProperty(propertyName));
            metadataList.add(metadataDto);
        }
    }

    /**
     * * Read the project files and scan them to find the required parameters for validation
     * with the new set up
     *
     * @param projectPath     Project Path
     * @param repoId          Repository Id
     * @param tag             Tag
     * @param pomFile         Pom.xml file
     * @param novaFile        nova.yml file
     * @param validatorInputs validationInput with the values to be validated
     */
    private void newReadAndScanProjectFiles(String projectPath, int repoId, String tag, byte[] pomFile, byte[] novaFile,
                                            ValidatorInputs validatorInputs)
    {
        validatorInputs.setLatestVersion(true);

        // Obtaining validation inputs from POM.yml
        ProjectFileReader.scanNovaYml(projectPath, novaFile, validatorInputs);

        // Get the service type from nova.yml
        ServiceType serviceType = ServiceType.getValueOf(validatorInputs.getNovaYml().getServiceType());
        switch (serviceType)
        {
            case API_REST_JAVA_SPRING_BOOT:
            case API_JAVA_SPRING_BOOT:
            case BATCH_JAVA_SPRING_BATCH:
            case BATCH_JAVA_SPRING_CLOUD_TASK:
            case DAEMON_JAVA_SPRING_BOOT:
            case FRONTCAT_JAVA_SPRING_MVC:
            case FRONTCAT_JAVA_J2EE:
            case DEPENDENCY:
            case LIBRARY_JAVA:
                // Pom.yml is a mandatory file, there is nothing to do without it
                if (pomFile == null || pomFile.length == 0)
                {
                    log.error("[{}] -> [{}]: Project: [{}] has no pom.xml file, aborting", Constants.SUBSYSTEM_DTO_BUILDER, "newReadAndScanProjectFiles", projectPath);
                    return;
                }

                // Obtaining validation inputs from POM.yml
                ProjectFileReader.scanPom(projectPath, pomFile, validatorInputs);

                // Get application.yml from service, repo and tag.
                byte[] applicationFile = this.iVersioncontrolsystemClient.getApplicationFromProject(projectPath, repoId, tag);
                ProjectFileReader.scanApplication(projectPath, applicationFile, validatorInputs);

                // Get bootstrap.xml from service, repo and tag.
                byte[] bootstrapFile = this.iVersioncontrolsystemClient.getBootstrapFromProject(projectPath, repoId, tag);
                ProjectFileReader.scanBootstrap(projectPath, bootstrapFile, validatorInputs);

                break;
            case CDN_ANGULAR_THIN2:
            case CDN_ANGULAR_THIN3:
            case API_REST_NODE_JS_EXPRESS:
            case LIBRARY_THIN2:
            case LIBRARY_NODE:
                // Get package.json from service, repo and tag.
                byte[] packageJsonFile = this.iVersioncontrolsystemClient.getPackageJsonFromProject(projectPath, repoId, tag);

                // package.json is a mandatory file, there is nothing to do without it
                if (packageJsonFile == null || packageJsonFile.length == 0)
                {
                    log.warn("[{}] -> [{}]: Project: [{}] has no package.json file, aborting", Constants.SUBSYSTEM_DTO_BUILDER,
                            "newReadAndScanProjectFiles", projectPath);
                    return;
                }

                ProjectFileReader.scanPackageJson(projectPath, packageJsonFile, validatorInputs);
                break;
            case API_REST_PYTHON_FLASK:
            case BATCH_PYTHON:
            case LIBRARY_PYTHON:
                // Fetch requirements from service, repo and tag.
                byte[] pythonRequirementsFile = this.iVersioncontrolsystemClient.getFileFromProject(projectPath + "/" + Constants.PYTHON_REQUIREMENTS, repoId, tag);

                // requirements file is mandatory for Python projects.
                // Generate an error if the project has no requirements file
                if (pythonRequirementsFile == null || pythonRequirementsFile.length == 0)
                {
                    log.warn("[{}] -> [{}]: Project: [{}] has no requirements.in file, aborting", Constants.SUBSYSTEM_DTO_BUILDER,
                            "newReadAndScanProjectFiles", projectPath);
                    return;
                }

                ProjectFileReader.scanPythonRequirementsFile(pythonRequirementsFile, validatorInputs);
                break;
            case CDN_POLYMER_CELLS:
            case LIBRARY_TEMPLATE:
                // Cells projects only have nova.yml so we do nothing here
                break;
            case BATCH_SCHEDULER_NOVA:
                // Get the scheduler yml from TAG (use version control system
                String schedulerPath = projectPath + Constants.SEPARATOR + "scheduler.yml";
                byte[] schedulerFile = this.iVersioncontrolsystemClient.getFileFromProject(schedulerPath, repoId, tag);

                if (schedulerFile != null && schedulerFile.length > 0)
                {
                    try
                    {
                        // Parse nova yml file
                        String novaFileString = new String(novaFile, StandardCharsets.UTF_8);
                        ParametersNova parametersNova = BuilderCreator.newParametersBuilderNova().setFileContentWithExtension(novaFileString, "yml").build();

                        // Parse scheduler yml file
                        String schedulerFileString = new String(schedulerFile, StandardCharsets.UTF_8);
                        Parameters parameters = BuilderCreator.newParametersBuilder().setFileContentWithExtension(schedulerFileString, "yml").build();

                        validatorInputs.setParsedInfo(new Parser(parametersNova, parameters).parse());
                    }
                    catch (SchedulerParserException e)
                    {
                        log.error("[{}] -> [{}]: the scheduler yml is not valid, aborting. Reason: [{}]", Constants.SUBSYSTEM_DTO_BUILDER, "newReadAndScanProjectFiles", e.getMessage());
                        log.debug("[{}] -> [{}]: the scheduler parser error: [{}]", Constants.SUBSYSTEM_DTO_BUILDER, "newReadAndScanProjectFiles", e);
                        validatorInputs.setSchedulerYmlMessage("Error in the file scheduler.yml: the file scheduler.yml has the following problems: " + e.getMessage());
                    }
                }
                else
                {
                    validatorInputs.setSchedulerYmlMessage("Error in the file scheduler.yml: the file scheduler.yml does not exist or is incomplete (bad format).");
                }

                break;
            default:
                log.error("[{}] -> [{}]: Service type from nova.yml invalid. Received value: [{}]", Constants.SUBSYSTEM_DTO_BUILDER, "newReadAndScanProjectFiles", serviceType);
                validatorInputs.getNovaYml().setServiceType("INVALID");
                break;
        }

        // Get resource files in a project
        List<String> treeFiles = this.iVersioncontrolsystemClient.getFilesFromTreeDirectory(projectPath, repoId, tag);
        ProjectFileReader.scanResourceTreeFiles(treeFiles, validatorInputs);

        // Get application.yml from service, repo and tag.
        byte[] applicationFile = this.iVersioncontrolsystemClient.getApplicationFromProject(projectPath, repoId, tag);
        ProjectFileReader.scanApplication(projectPath, applicationFile, validatorInputs);

        // Get bootstrap.xml from service, repo and tag.
        byte[] bootstrapFile = this.iVersioncontrolsystemClient.getBootstrapFromProject(projectPath, repoId, tag);
        ProjectFileReader.scanBootstrap(projectPath, bootstrapFile, validatorInputs);

    }

    /**
     * Read the project files and scan them to find the required parameters for validation
     * with the old set up
     *
     * @param projectPath     Project Path
     * @param repoId          Repository Id
     * @param tag             Tag
     * @param pomFile         Pom.xml file
     * @param validatorInputs validationInput with the values to be validated
     */
    private void oldReadAndScanProjectFiles(String projectPath, int repoId, String tag, byte[] pomFile, ValidatorInputs validatorInputs)
    {
        validatorInputs.setLatestVersion(false);
        // Obtaining validation inputs from POM.yml
        ProjectFileReader.scanPom(projectPath, pomFile, validatorInputs);

        // Get resource files in a project
        List<String> treeFiles = this.iVersioncontrolsystemClient.getFilesFromTreeDirectory(projectPath, repoId, tag);
        ProjectFileReader.scanResourceTreeFiles(treeFiles, validatorInputs);

        // Get application.yml from service, repo and tag.
        byte[] applicationFile = this.iVersioncontrolsystemClient.getApplicationFromProject(projectPath, repoId, tag);
        ProjectFileReader.scanApplication(projectPath, applicationFile, validatorInputs);

        // Get package.json from service, repo and tag.
        byte[] packageJsonFile = this.iVersioncontrolsystemClient.getPackageJsonFromProject(projectPath, repoId, tag);
        ProjectFileReader.scanPackageJson(projectPath, packageJsonFile, validatorInputs);

        // Get bootstrap.xml from service, repo and tag.
        byte[] bootstrapFile = this.iVersioncontrolsystemClient.getBootstrapFromProject(projectPath, repoId, tag);
        ProjectFileReader.scanBootstrap(projectPath, bootstrapFile, validatorInputs);

        // Get dockerfile.xml from service, repo and tag.
        byte[] dockerFile = this.iVersioncontrolsystemClient.getDockerfileFromProject(projectPath, repoId, tag);
        ProjectFileReader.scanDockerFiles(projectPath, dockerFile, validatorInputs);
    }

    /**
     * This method processes Ephoenix metadata.
     *
     * @param metadataList    List of service metadata.
     * @param modelProperties pom properties.
     */
    private void processEphoenixMetadata(
            List<RVMetadataDTO> metadataList,
            final Properties modelProperties)
    {
        // Ephoenix version.
        this.extractProperty(metadataList, modelProperties, Constants.EPHOENIX_VERSION);

        // Instance uuaa.
        this.extractProperty(metadataList, modelProperties, Constants.UUAA);

        // Instance uuaas.
        this.extractProperty(metadataList, modelProperties, Constants.EPHOENIX_INSTANCE_UUAAS);

        // Instance user.
        this.extractProperty(metadataList, modelProperties, Constants.EPHOENIX_INSTANCE_USER);

        // Deployment line.
        this.extractProperty(metadataList, modelProperties, Constants.EPHOENIX_DEPLOYMENT);

        // Instance port.
        this.extractProperty(metadataList, modelProperties, Constants.EPHOENIX_INSTANCE_PORT);

        // Deployment environment.
        this.extractProperty(metadataList, modelProperties, Constants.EPHOENIX_DEVELOPMENT_ENVIRONMENT);

        // Deployment environment promotion.
        this.extractProperty(metadataList, modelProperties, Constants.EPHOENIX_DEVELOPMENT_ENVIRONMENT_PROMOTION);
    }


    /**
     * Processes Ephoenix modules.
     *
     * @param service service
     * @param modules pom modules
     */
    private void processModules(NewReleaseVersionServiceDto service, List<String> modules)
    {
        if (modules != null)
        {
            service.setModules(new String[modules.size()]);

            // Iterate through modules.
            for (int i = 0; i < modules.size(); i++)
            {
                // Add module.
                service.getModules()[i] = modules.get(i);
            }
        }
    }

    /**
     * Processes Node.js metadata, such as version.
     *
     * @param metadataList    List of service metadata.
     * @param modelProperties pom properties.
     */
    private void processNodeMetadata(
            List<RVMetadataDTO> metadataList,
            Properties modelProperties)
    {
        // Node.js version.
        extractProperty(metadataList, modelProperties, Constants.NODE_JS_VERSION);
    }

    /**
     * Add pom properties section, where NOVA properties are defined,
     * to the given DTO.
     *
     * @param service         Service to add properties to.
     * @param modelProperties pom properties.
     */
    private void processProperties(
            NewReleaseVersionServiceDto service,
            Properties modelProperties)
    {
        // Properties should not be null.
        if (modelProperties == null)
        {
            log.warn("[{}] -> [{}]: Service: [{}] has no properties", Constants.SUBSYSTEM_DTO_BUILDER,
                    "processProperties", service.getServiceName());
            return;
        }

        // Set NOVA type.
        String novaType = modelProperties.getProperty(Constants.NOVA_TYPE);
        if (!StringUtils.isEmpty(novaType))
        {
            // Set type.
            service.setServiceType(novaType.toUpperCase(Locale.getDefault()));
            this.setIsService(service);
        }
        else
        {
            // Modules without nova.type will be treated as dependencies.
            service.setServiceType(ServiceType.DEPENDENCY.name());
            service.setIsService(false);
        }

        // Set NOVA version.
        String novaVersion = modelProperties.getProperty(Constants.NOVA_VERSION);
        service.setNovaVersion(novaVersion);

        // Set service UUAA.
        String serviceUuaa = modelProperties.getProperty(Constants.UUAA);
        service.setUuaa(serviceUuaa);

        // If it is actually a service.
        if (service.getIsService())
        {
            List<RVMetadataDTO> metadataList = new ArrayList<>();

            if ((ServiceType.EPHOENIX_BATCH.toString().equals(service.getServiceType())) ||
                    (ServiceType.EPHOENIX_ONLINE.toString().equals(service.getServiceType())))
            {
                // Process ephoenix metadata.
                this.processEphoenixMetadata(metadataList, modelProperties);

                boolean hasEphoenixDevelopmentEnvironment = metadataList.stream()
                        .anyMatch(metadata -> Constants.EPHOENIX_DEVELOPMENT_ENVIRONMENT.equalsIgnoreCase(metadata.getKey()) &&
                                Constants.TRUE.equalsIgnoreCase(metadata.getValue()));

                boolean hasEphoenixDevelopmentEnvironmentPromotable = metadataList.stream()
                        .anyMatch(metadata -> Constants.EPHOENIX_DEVELOPMENT_ENVIRONMENT_PROMOTION.equalsIgnoreCase(metadata.getKey()) &&
                                Constants.TRUE.equalsIgnoreCase(metadata.getValue()));

                if (hasEphoenixDevelopmentEnvironment && !hasEphoenixDevelopmentEnvironmentPromotable)
                {
                    //If the ephoenix is for development environment and it cannot be promotable we must add ".des" to groupId and finalName
                    //groupId: At the end
                    //finalName: Before the character "-"
                    service.setGroupId(service.getGroupId() + ".des");
                    int index = service.getFinalName().indexOf('-');
                    StringBuilder finalName = new StringBuilder(service.getFinalName());
                    service.setFinalName(finalName.insert(index, ".des").toString());
                }
            }

            else if (ServiceType.THIN2.toString().equals(service.getServiceType()))
            {
                // Process Thin2 metadata.
                this.processThin2Metadata(metadataList, modelProperties);
            }

            else if (ServiceType.NODE.getServiceType().equals(service.getServiceType()))
            {
                // Process Thin2 metadata.
                this.processNodeMetadata(metadataList, modelProperties);
            }

            // Convert List to [] and attach it to service.
            service.setMetadata(metadataList.toArray(new RVMetadataDTO[0]));
        }

    }

    /**
     * Processes Thin2 metadata, such as version.
     *
     * @param metadataList    List of service metadata.
     * @param modelProperties pom properties.
     */
    private void processThin2Metadata(
            List<RVMetadataDTO> metadataList,
            Properties modelProperties)
    {
        // Thin2 version.
        extractProperty(metadataList, modelProperties, Constants.THIN2_VERSION);
    }

    /**
     * Set the service data retrieve from the project files and validateSubsystemTagDto them
     *
     * @param repoId            Repo ID.
     * @param tag               Tag.
     * @param projectPath       Name of the project.
     * @param subsystemNovaType Subsystem NOVA Type.
     * @param ivUser            user requester
     * @param releaseName       Name of the release
     * @param subsystemName     Subsystem name
     * @return NewReleaseVersionServiceDto
     */
    private NewReleaseVersionServiceDto serviceDtoBuilder(final int repoId, final SubsystemTagDto tag,
                                                          final String projectPath, final SubsystemType subsystemNovaType, final String ivUser,
                                                          final String releaseName, Product product, String subsystemName)
    {
        // Read Action. Get the validation parameters from files that are being validated
        ValidatorInputs validatorInputs = this.buildValidationFileInputs(projectPath, repoId, tag.getTagName());

        // Parse pom.xml and build a service with its data.
        NewReleaseVersionServiceDto serviceDto = this.buildServiceGeneralInfo(releaseName, validatorInputs);

        // Only null it there was an error parsing its pom.xml.
        if (serviceDto == null)
        {
            serviceDto = new NewReleaseVersionServiceDto();
            serviceDto.setServiceName(Constants.INVALID_SERVICE_NAME);
            serviceDto.setIsService(false);
            List<ValidationErrorDto> validationErrorDto = new ArrayList<>();
            ErrorListUtils.addError(validationErrorDto, "NULL", Constants.INVALID_SERVICE_NAME_CODE, Constants.INVALID_SERVICE_NAME_MSG);
            serviceDto.setValidationErrors(validationErrorDto.toArray(new ValidationErrorDto[0]));
        }
        else
        {
            this.buildServiceGenerics(validatorInputs.getPackageJson().getDependencies().toArray(new String[0]), tag.getTagUrl(), projectPath, serviceDto);

            // Legacy
            if (!validatorInputs.isLatestVersion() && ServiceType.NODE.getServiceType().equals(serviceDto.getServiceType()))
            {
                // If service is based on Node.js, set the URL to the package.json file.
                // Set the URL to the project definition file - aka pom.xml.
                serviceDto.setNodePackageUrl(tag.getTagUrl() + Constants.SEPARATOR + projectPath + Constants.SEPARATOR +
                        Constants.PACKAGE_JSON);

                serviceDto.setModules(validatorInputs.getPackageJson().getDependencies().toArray(new String[0]));
            }

            // Check validation.
            this.iProjectFileValidator.validateServiceProjectFiles(validatorInputs, subsystemNovaType, serviceDto,
                    repoId, tag.getTagName(), ivUser, releaseName, product, subsystemName);
        }

        return serviceDto;
    }

    /**
     * Set is service property
     *
     * @param service service
     */
    private void setIsService(NewReleaseVersionServiceDto service)
    {
        final ServiceType type = ServiceType.getValueOf(service.getServiceType());
        service.setIsService(!ServiceType.DEPENDENCY.equals(type) && !ServiceType.isLibrary(type));
    }

    /**
     * Validate dependency file
     *
     * @param repoId       repository id
     * @param tag          tag
     * @param projectNames project names
     */
    private void validateDependencyFile(int repoId, SubsystemTagDto tag, List<String> projectNames)
    {
        List<String> dependencies = this.iVersioncontrolsystemClient.getDependencies(repoId, tag.getTagName());
        List<ValidationErrorDto> errorList = new ArrayList<>(Arrays.asList(tag.getValidationErrors()));
        for (String dependency : dependencies)
        {
            if (!projectNames.contains(dependency))
            {
                ValidationErrorDto error = new ValidationErrorDto();
                error.setCode(Constants.DEPENDENCY_NOT_FOUND);
                error.setMessage(Constants.DEPENDENCY_NOT_FOUND_MSG + dependency);
                errorList.add(error);
            }
        }
        // Add all errors - if any - to the service.
        tag.setValidationErrors(errorList.toArray(new ValidationErrorDto[0]));
    }
}
