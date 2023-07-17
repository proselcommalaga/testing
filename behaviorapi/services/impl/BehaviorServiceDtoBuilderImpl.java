package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.impl;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVBehaviorParamsDTO;
import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceInfoDTO;
import com.bbva.enoa.apirestgen.behaviormanagerapi.model.BMBehaviorParamsDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces.IBehaviorServiceDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.PomXML;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.ProjectFileReader;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;

/**
 * Service DTO builder
 * Builds the DTO of a service
 */
@Service
@Slf4j
public class BehaviorServiceDtoBuilderImpl implements IBehaviorServiceDtoBuilder
{
    /**
     * Client of the VCS API.
     */
    private final IVersioncontrolsystemClient iVersioncontrolsystemClient;


    /**
     * @param iVersioncontrolsystemClient version control system client
     */
    @Autowired
    public BehaviorServiceDtoBuilderImpl(final IVersioncontrolsystemClient iVersioncontrolsystemClient)
    {
        this.iVersioncontrolsystemClient = iVersioncontrolsystemClient;
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

        this.readAndScanProjectFiles(projectPath, repoId, tag, pomFile, novaFile, validatorInputs);

        return validatorInputs;
    }

    @Override
    public BVServiceInfoDTO buildServiceGeneralInfo(final ValidatorInputs validatorInputs)
    {
        BVServiceInfoDTO service = null;
        if (validatorInputs.getPom() != null)
        {
            service = this.createServiceFromNovaYml(validatorInputs);
        }

        // And return the service.
        return service;
    }

    @Override
    public void buildServiceGenerics(final String tagUrl, final String projectPath, final BVServiceInfoDTO serviceDto)
    {
        serviceDto.setFolder(projectPath);
        ServiceType serviceType = ServiceType.getValueOf(serviceDto.getServiceType());

        switch (serviceType)
        {
            case BUSINESS_LOGIC_BEHAVIOR_TEST_JAVA:
                // Set the URL to the project definition file - aka pom.xml
                // in the repo and tag used in the behavior version.
                serviceDto.setProjectDefinitionUrl(tagUrl + Constants.SEPARATOR + projectPath + Constants.SEPARATOR +
                        Constants.POM_XML);
                serviceDto.setNovaYamlUrl(tagUrl + Constants.SEPARATOR + projectPath + Constants.SEPARATOR +
                        Constants.NOVA_YML);
                break;
            default:
                // Set the URL to the project definition file - aka pom.xml
                // in the repo and tag used in the behavior version.
                serviceDto.setProjectDefinitionUrl(tagUrl + Constants.SEPARATOR + projectPath + Constants.SEPARATOR +
                        Constants.POM_XML);
                break;
        }

        // Force compilation
        serviceDto.setHasForcedCompilation(true);
    }

    @Override
    public BMBehaviorParamsDTO buildBMBehaviorParamsDTO(final BVBehaviorParamsDTO behaviorParamsDTO)
    {
        BMBehaviorParamsDTO bmBehaviorParamsDTO = new BMBehaviorParamsDTO();

        bmBehaviorParamsDTO.setReleaseVersionId(behaviorParamsDTO.getReleaseVersionId());
        bmBehaviorParamsDTO.setTags(behaviorParamsDTO.getTags());

        return bmBehaviorParamsDTO;
    }


    ///////////////////////////////////////////////   PRIVATE METHODS   \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

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
    private void readAndScanProjectFiles(String projectPath, int repoId, String tag, byte[] pomFile, byte[] novaFile,
                                         ValidatorInputs validatorInputs)
    {
        validatorInputs.setLatestVersion(true);

        // Obtaining validation inputs from POM.yml
        ProjectFileReader.scanNovaYml(projectPath, novaFile, validatorInputs);

        // Get the service type from nova.yml
        ServiceType serviceType = ServiceType.getValueOf(validatorInputs.getNovaYml().getServiceType());

        switch (serviceType)
        {
            case BUSINESS_LOGIC_BEHAVIOR_TEST_JAVA:
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
                // byte[] bootstrapFile = this.iVersioncontrolsystemClient.getBootstrapFromProject(projectPath, repoId, tag);
                // ProjectFileReader.scanBootstrap(projectPath, bootstrapFile, validatorInputs);

                break;
            default:
                log.error("[{}] -> [{}]: Service type from nova.yml invalid. Received value: [{}]", Constants.SUBSYSTEM_DTO_BUILDER, "newReadAndScanProjectFiles", serviceType);
                validatorInputs.getNovaYml().setServiceType("INVALID");
                break;
        }

        // Get resource files in a project
        List<String> treeFiles = this.iVersioncontrolsystemClient.getFilesFromTreeDirectory(projectPath, repoId, tag);
        ProjectFileReader.scanResourceTreeFiles(treeFiles, validatorInputs);

    }

    /**
     * Creates a new BVServiceInfoDTO from a pom.xml and nova.yml
     *
     * @param validatorInputs validator inputs with the pom.xml and nova.yml parameters
     * @return the behavior service dto
     */
    private BVServiceInfoDTO createServiceFromNovaYml(ValidatorInputs validatorInputs)
    {
        // Create the DTO.
        BVServiceInfoDTO service = new BVServiceInfoDTO();

        PomXML pomXML = validatorInputs.getPom();
        NovaYml novaYml = validatorInputs.getNovaYml();

        switch (ServiceType.getValueOf(novaYml.getServiceType()))
        {
            case BUSINESS_LOGIC_BEHAVIOR_TEST_JAVA:
                // Set final name.
                service.setFinalName(this.getFinalNameFromPom(pomXML));
                // Group ID.
                service.setGroupId(validatorInputs.getPom().getGroupId());
                break;
            default:
        }

        // Extract artifact ID.
        service.setArtifactId(novaYml.getName());

        // Version.
        service.setVersion(novaYml.getVersion());

        // Add description service from nova yml.
        service.setDescription(novaYml.getDescription());

        // Add name service from nova yml.
        service.setServiceName(novaYml.getName());

        // Add release version to testing from nova yml.
        service.setReleaseVersion(novaYml.getReleaseVersion());

        // Add behavior tags to testing from nova yml.
        service.setTags(novaYml.getTags());

        // Add NOVA properties.
        this.buildServiceInfo(service, validatorInputs);

        return service;
    }

    /**
     * @param pom
     * @return
     */
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
     * @param service
     * @param validatorInputs
     */
    private void buildServiceInfo(final BVServiceInfoDTO service, final ValidatorInputs validatorInputs)
    {
        // Set NOVA type.
        final NovaYml novaYml = validatorInputs.getNovaYml();
        String novaType = novaYml.getServiceType();
        if (!StringUtils.isEmpty(novaType))
        {
            // Set type.
            service.setServiceType(novaType.toUpperCase(Locale.getDefault()));
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

        final String testFramework = novaYml.getTestFramework();
        if (testFramework != null && !"".equals(testFramework))
        {
            service.setTestFramework(testFramework);
            service.setTestFrameworkVersion(novaYml.getTestFrameworkVersion());
        }
    }

}
