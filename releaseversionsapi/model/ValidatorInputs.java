package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;


import com.bbva.enoa.utils.schedulerparserlib.processor.ParsedInfo;

/**
 * Store all the file and variables under validation
 */
public class ValidatorInputs
{
    private boolean latestVersion;

    /**
     * Bootstrap file
     */
    private BootstrapYaml bootstrap = new BootstrapYaml();

    /**
     * Application file
     */
    private ApplicationYml application = new ApplicationYml();

    /**
     * nova.yml file
     */
    private NovaYml novaYml = new NovaYml();

    /**
     * Dockerfile file
     */
    private DockerFile dockerfile = new DockerFile();

    /**
     * Pom
     */
    private PomXML pom = new PomXML();

    /**
     * Python requirements file
     */
    private PythonRequirements requirements = new PythonRequirements();

    /**
     * Parsed Info (from scheduler yml in the batch scheduler service)
     */
    private ParsedInfo parsedInfo = null;

    /**
     * Scheduler yml message
     */
    private String schedulerYmlMessage = "";

    /**
     * PackageJson for Node Projects
     */
    private PackageJsonFile packageJson = new PackageJsonFile();

    // Getters and Setters
    public NovaYml getNovaYml()
    {
        return novaYml;
    }

    public void setNovaYml(NovaYml novaYml)
    {
        this.novaYml = novaYml;
    }

    public BootstrapYaml getBootstrap()
    {
        return bootstrap;
    }

    public ApplicationYml getApplication()
    {
        return application;
    }

    public void setBootstrap(BootstrapYaml bootstrap)
    {
        this.bootstrap = bootstrap;
    }

    public void setApplication(ApplicationYml application)
    {
        this.application = application;
    }

    public DockerFile getDockerfile()
    {
        return dockerfile;
    }

    public void setDockerfile(DockerFile dockerfile)
    {
        this.dockerfile = dockerfile;
    }

    public PomXML getPom()
    {
        return pom;
    }

    public void setPom(PomXML pom)
    {
        this.pom = pom;
    }

    public PythonRequirements getRequirements()
    {
        return requirements;
    }

    public void setRequirements(PythonRequirements requirements)
    {
        this.requirements = requirements;
    }

    public PackageJsonFile getPackageJson()
    {
        return packageJson;
    }

    public void setPackageJson(PackageJsonFile packageJson)
    {
        this.packageJson = packageJson;
    }

    /**
     * True for security version, false for previous versions
     *
     * @return latestVersion
     */
    public boolean isLatestVersion()
    {
        return latestVersion;
    }

    public void setLatestVersion(boolean latestVersion)
    {
        this.latestVersion = latestVersion;
    }

    public ParsedInfo getParsedInfo()
    {
        return this.parsedInfo;
    }

    public void setParsedInfo(final ParsedInfo parsedInfo)
    {
        this.parsedInfo = parsedInfo;
    }

    public String getSchedulerYmlMessage()
    {
        return schedulerYmlMessage;
    }

    public void setSchedulerYmlMessage(String schedulerYmlMessage)
    {
        this.schedulerYmlMessage = schedulerYmlMessage;
    }

    @Override
    public String toString()
    {
        return "ValidatorInputs{" +
                "latestVersion=" + latestVersion +
                ", bootstrap=" + bootstrap +
                ", application=" + application +
                ", novaYml=" + novaYml +
                ", dockerfile=" + dockerfile +
                ", pom=" + pom +
                ", requirements=" + requirements +
                ", parsedInfo=" + parsedInfo +
                ", schedulerYmlMessage='" + schedulerYmlMessage + '\'' +
                ", packageJson=" + packageJson +
                '}';
    }
}


