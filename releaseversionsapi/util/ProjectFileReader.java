package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util;

import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.release.enumerates.ServiceLanguage;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.*;
import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.scanner.ScannerException;

import java.io.*;
import java.util.*;

/**
 * Reader for validation files.
 * Transform a byte[] into the corresponding object
 */
@Slf4j
public final class ProjectFileReader
{

    public static final String SWAGGERS_PATH_TEMPLATE = "Swaggers path: [{}]";
    private static final Logger LOG = LoggerFactory.getLogger(ProjectFileReader.class);
    private static final String DESCRIPTION = "description";
    private static final String STRING = "STRING";
    private static final String INTEGER = "INTEGER";
    private static final String BOOLEAN = "BOOLEAN";
    private static final String MANDATORY = "mandatory";
    private static final String DEFAULT_VERSION = "0.0.0";
    private static final String ENOA_PLUGIN_GROUP_ARTIFACT_ID = "com.bbva.enoa.starter:plugin";

    /**
     * Private constructor
     */
    private ProjectFileReader()
    {

    }

    /**
     * Reads the input data and fulfill the fields in validatorInput
     *
     * @param applicationFile Application file
     * @param validatorInputs Validation variables
     * @param projectPath     the project path
     */
    public static void scanApplication(final String projectPath, byte[] applicationFile, ValidatorInputs validatorInputs)
    {
        Yaml yaml = new Yaml();
        Map<String, Object> map = new LinkedHashMap<>();
        ApplicationYml applicationYml = new ApplicationYml();


        if (applicationFile != null && (applicationFile.length > 0))
        {
            try
            {
                String s = new String(applicationFile);
                s = s.replace("@", "");
                map.putAll(yaml.load(s));

                LOG.debug("[ProjectFileReader] -> [scanApplication]: Application.yml content: [{}]", map);

                scanApplicationServerPort(projectPath, map, applicationYml);

                scanApplicationEndPoint(projectPath, map, applicationYml);
            }
            catch (YAMLException | NullPointerException e)
            {
                LOG.debug("[ProjectFileReader] -> [scanApplication]: There was some problem parsing the application.yml from project path: [{}]", projectPath);
                LOG.trace("[ProjectFileReader] -> [scanApplication]: Application.yml parse exception: ", e);
                applicationYml.setYamlException(e.getMessage());
            }
        }
        else
        {
            LOG.debug("[ProjectFileReader] -> [scanApplication]: Application.yml does not found in the project path: [{}]", projectPath);
        }

        buildApplicationValidatingParams(validatorInputs, applicationYml);
        LOG.debug("[ProjectFileReader] -> [scanApplication]: Server port: [{}], end point restart: [{}], end point shutdown: [{}], end point logfile: [{}]",
                applicationYml.getServerPort(), applicationYml.getEndpointRestart(),
                applicationYml.getEndpointShutdown(), applicationYml.getEndpointLogFile());
    }

    /**
     * Reads the input data and fulfill the fields in validatorInput
     *
     * @param bootstrapFile   Bootstrap file
     * @param validatorInputs Validation variables
     * @param projectPath     project Path
     */
    public static void scanBootstrap(final String projectPath, byte[] bootstrapFile, ValidatorInputs validatorInputs)
    {
        if ((bootstrapFile != null) && (bootstrapFile.length > 0))
        {
            String activeProfile = "";
            validatorInputs.getBootstrap().setValid(true);
            try
            {
                Yaml yaml = new Yaml();
                InputStream input = new ByteArrayInputStream(bootstrapFile);
                Map<String, Object> map = new LinkedHashMap<>();
                map.putAll((Map<String, Object>) yaml.load(input));

                LOG.debug("[ProjectFileReader] -> [scanBootstrap]: Bootstrap.yml content: [{}]", map);

                Map<String, Object> mapDeep1 = (Map<String, Object>) map.get("spring");
                Map<String, Object> mapDeep2 = (Map<String, Object>) mapDeep1.get("profiles");
                activeProfile = (String) mapDeep2.get("active");

                buildBootstrapValidatingParams(validatorInputs, activeProfile);
                LOG.debug("[ProjectFileReader] -> [scanBootstrap]: Spring profiles active: [{}]", activeProfile);
            }
            catch (ClassCastException | NullPointerException e)
            {
                validatorInputs.getBootstrap().setValid(false);
                LOG.debug("[ProjectFileReader] -> [scanBootstrap]: There was some problem parsing the bootstrap.yml from project path: [{}]", projectPath);
                LOG.trace("[ProjectFileReader] -> [scanBootstrap]: Bootstrap.yml parse exception: ", e);
            }
            catch (ScannerException e)
            {
                LOG.error("[ProjectFileReader] -> [scanBootstrap]: [ProjectFileReader] -> [scanBootstrap]: error reading bootstrap file.");
                validatorInputs.getBootstrap().setValid(false);
            }
        }
        else
        {
            LOG.debug("[ProjectFileReader] -> [scanBootstrap]: Bootstrap.yml does not found in the project path: [{}]", projectPath);
        }
    }

    /**
     * Reads the input data and fulfill the fields in validatorInput
     *
     * @param dockerfile      Dockerfile
     * @param validatorInputs Validation variables
     * @param projectPath     project path
     */
    public static void scanDockerFiles(final String projectPath, byte[] dockerfile, ValidatorInputs validatorInputs)
    {
        if ((dockerfile != null) && (dockerfile.length > 0))
        {
            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(dockerfile)));

                String line = reader.readLine();
                while (line != null)
                {
                    if (line.length() > 1 && !line.startsWith("#"))
                    {
                        validatorInputs.getDockerfile().addDockerfileLine(line);
                    }
                    line = reader.readLine();
                }
                reader.close();
            }
            catch (IOException e)
            {
                LOG.error("[ProjectFileReader] -> [scanDockerFiles]: Error reading Dockerfile from project path: [{}]", projectPath);
                LOG.debug("[ProjectFileReader] -> [scanDockerFiles]: Dockerfile reading file exception: ", e);
            }
        }
        else
        {
            LOG.debug("[ProjectFileReader] -> [scanDockerFiles]: Dockerfile does not found in the project path: [{}]", projectPath);
        }
    }

    /**
     * Reads the input data and fulfill the fields in validatorInput
     *
     * @param novaYmlFile     Application file
     * @param validatorInputs Validation variables
     * @param projectPath     the project path
     */
    public static void scanNovaYml(final String projectPath, byte[] novaYmlFile, ValidatorInputs validatorInputs)
    {
        Yaml yaml = new Yaml();
        Map<String, Object> map = new LinkedHashMap<>();
        NovaYml novaYml = new NovaYml();
        validatorInputs.getNovaYml().setValid(true);

        if (novaYmlFile != null && (novaYmlFile.length > 0))
        {
            try
            {
                String s = new String(novaYmlFile);
                s = s.replace("@", "");
                map.putAll(yaml.load(s));

                LOG.debug("[ProjectFileReader] -> [scanNovaYml]: nova.yml content: [{}]", map);

                scanNovaYmlService(projectPath, map, novaYml);

                scanNovaYmlSwagger(projectPath, map, novaYml);

                scanNovaYmlAsyncApi(projectPath, map, novaYml, ApiModality.ASYNC_BACKTOBACK);

                scanNovaYmlAsyncApi(projectPath, map, novaYml, ApiModality.ASYNC_BACKTOFRONT);

                scanNovaYmlBuild(projectPath, map, novaYml);

                scanNovaYmlMachines(projectPath, map, novaYml);

                scanNovaYmlProperties(projectPath, map, novaYml);

                scanNovaYmlContextParams(projectPath, map, novaYml);

                scanNovaYmlInputParams(projectPath, map, novaYml);

                scanNovaYmlOutputParams(projectPath, map, novaYml);

                scanNovaYmlRequirements(projectPath, map, novaYml);

                scanNovaYmlPorts(projectPath, map, novaYml);

                scanNovaYmlDependencies(projectPath, map, novaYml);

                scanNovaYmlLibraries(projectPath, map, novaYml);

                scanNovaYmlFrontcat(projectPath, map, novaYml);

                scanNovaYmlBehaviorTest(projectPath, map, novaYml);
            }
            catch (YAMLException | NullPointerException e)
            {
                LOG.error("[ProjectFileReader] -> [scanNovaYml]: There was and error parsing nova.yml from project path: [{}]", projectPath);
                LOG.debug("[ProjectFileReader] -> [scanNovaYml]: Nova.yml parser exception: ", e);
                validatorInputs.getNovaYml().setValid(false);
            }
        }

        setNotNullNovaYmlParameters(validatorInputs, novaYml);
    }

    /**
     * Scan Package.json file for dependencies to store in the modules section.
     *
     * @param projectPath     path to project
     * @param packageJsonFile node file
     * @param validatorInputs validator object
     */
    public static void scanPackageJson(final String projectPath, byte[] packageJsonFile, ValidatorInputs validatorInputs)
    {

        List<String> dependencies = new ArrayList<>();
        List<String> devDependencies = new ArrayList<>();
        String name = "";
        String version = "";

        if (packageJsonFile != null && packageJsonFile.length > 0)
        {
            try
            {
                // transform byte [] to string
                String packageJasonContent = new JSONObject(new String(packageJsonFile)).toString();

                // json Parser
                JsonObject jsonObject = JsonParser.parseString(packageJasonContent).getAsJsonObject();

                // Name
                name = jsonObject.get(Constants.NAME).getAsString();

                // Version
                version = jsonObject.get(Constants.VERSION).getAsString();

                // Dependencies
                // Obtain Array
                String listDependencies = jsonObject.get(Constants.DEPENDENCIES_NAME).toString();
                dependencies = new ArrayList<>(Arrays.asList(listDependencies.split(",")));

                String listDevDependencies = jsonObject.get(Constants.DEV_DEPENDENCIES_NAME).toString();
                devDependencies = new ArrayList<>(Arrays.asList(listDevDependencies.split(",")));

            }
            catch (NullPointerException | ClassCastException | JSONException e)
            {
                LOG.debug("[ProjectFileReader] -> [scanPackageJson]: There was and error parsing Package.json from project path: [{}]", projectPath);
                LOG.trace("[ProjectFileReader] -> [scanPackageJson]: Package.json parser exception: ", e);
            }
        }

        // Name
        if (name != null)
        {
            validatorInputs.getPackageJson().setName(name);
        }
        else
        {
            validatorInputs.getPackageJson().setName("");
        }

        // Version
        if (version != null)
        {
            validatorInputs.getPackageJson().setVersion(version);
        }
        else
        {
            validatorInputs.getPackageJson().setVersion("");
        }

        // Dependencies
        validatorInputs.getPackageJson().setDependencies(dependencies);
        validatorInputs.getPackageJson().setDevDependencies(devDependencies);

    }

    /**
     * Scan python requirements file for dependencies to store in the modules section.
     *
     * @param pythonRequirementsFile python requirements file
     * @param validatorInputs        validator object
     */
    public static void scanPythonRequirementsFile(byte[] pythonRequirementsFile, ValidatorInputs validatorInputs)
    {
        List<String> dependencies = new ArrayList<>();

        if (pythonRequirementsFile != null && pythonRequirementsFile.length > 0)
        {
            try
            {
                // extract dependencies set at the requirements file
                dependencies = Arrays.asList(new String(pythonRequirementsFile).split("\n"));

            }
            catch (NullPointerException | ClassCastException e)
            {
                log.warn("[ProjectFileReader] -> [scanPythonRequirementsFile]: python requirements file parsing exception. Error: ", e);
            }
        }

        validatorInputs.getRequirements().setDependencies(dependencies);
    }

    /**
     * Reads the input data and fulfill the fields in validatorInput
     *
     * @param pom             Pom file
     * @param validatorInputs Validation variables
     * @param projectPath     project path
     */
    public static void scanPom(final String projectPath, byte[] pom, ValidatorInputs validatorInputs)
    {
        // Build a reader for the pom.xml file.
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();

        validatorInputs.getPom().setPomValidation(true);

        try
        {
            // Read and parse de pom.xml file.
            Model model = mavenReader.read(new ByteArrayInputStream(pom));

            LOG.debug("[ProjectFileReader] -> [scanPom]: Pom content: [{}]", model);

            // ** Values only for service builder
            scanServiceBuilderParameters(validatorInputs, model);

            // ** Values for validation and service builder
            scanValidatingPomParameters(validatorInputs, model);
        }
        catch (XmlPullParserException | IOException | NullPointerException e)
        {
            // Don't throw exception to continue parsing other services.
            LOG.debug("[ProjectFileReader] -> [scanPom]: There was and error parsing pom.xml file from project path: [{}]", projectPath);
            LOG.trace("[ProjectFileReader] -> [scanPom]: Pom.xml file parser exception: ", e);
            validatorInputs.getPom().setPomValidation(false);
        }
    }

    /**
     * Reads the input data and fulfill the fields in validatorInput
     *
     * @param treeFiles       list of files
     * @param validatorInputs Validation variables
     */
    public static void scanResourceTreeFiles(List<String> treeFiles, ValidatorInputs validatorInputs)
    {
        if (treeFiles != null)
        {
            validatorInputs.getApplication().setApplicationFiles(treeFiles);
            LOG.debug("[ProjectFileReader] -> [scanResourceTreeFiles]: Resource files are [{}]", treeFiles);
        }
    }

    /**
     * Build the application params required for validation
     *
     * @param validatorInputs validator inputs
     * @param applicationYml  Server port, restart, shutdown and log file
     */
    private static void buildApplicationValidatingParams(ValidatorInputs validatorInputs, ApplicationYml applicationYml)
    {
        if (applicationYml.getYamlException() != null)
        {
            validatorInputs.getApplication().setYamlException(applicationYml.getYamlException());
        }
        else
        {
            validatorInputs.getApplication().setYamlException("");
        }

        if (applicationYml.getServerPort() != null)
        {
            validatorInputs.getApplication().setServerPort(applicationYml.getServerPort());
        }
        else
        {
            validatorInputs.getApplication().setServerPort("");
        }

        if (applicationYml.getEndpointRestart() != null)
        {
            validatorInputs.getApplication().setEndpointRestart(applicationYml.getEndpointRestart());
        }
        else
        {
            validatorInputs.getApplication().setEndpointRestart("");
        }

        if (applicationYml.getEndpointShutdown() != null)
        {
            validatorInputs.getApplication().setEndpointShutdown(applicationYml.getEndpointShutdown());
        }
        else
        {
            validatorInputs.getApplication().setEndpointShutdown("");
        }

        if (applicationYml.getEndpointLogFile() != null)
        {
            validatorInputs.getApplication().setEndpointLogFile(applicationYml.getEndpointLogFile());
        }
        else
        {
            validatorInputs.getApplication().setEndpointLogFile("");
        }

        LOG.debug("[ProjectFileReader] -> [buildApplicationValidatingParams]: buildApplicationValidatingParams: port: [{}], restart: [{}], shutdown: [{}] and logfile: [{}]",
                validatorInputs.getApplication().getServerPort(), validatorInputs.getApplication().getEndpointRestart(),
                validatorInputs.getApplication().getEndpointShutdown(),
                validatorInputs.getApplication().getEndpointLogFile());
    }

    /**
     * Build the bootstrap params required for validation
     *
     * @param validatorInputs validation inputs
     * @param activeProfile   active profile
     */
    private static void buildBootstrapValidatingParams(ValidatorInputs validatorInputs, String activeProfile)
    {
        if (activeProfile != null)
        {
            validatorInputs.getBootstrap().setActiveProfile(activeProfile);
        }
        else
        {
            validatorInputs.getBootstrap().setActiveProfile("");
        }
    }

    /**
     * Scan application end point
     *
     * @param map            application.yml map
     * @param applicationYml application yml object
     * @param projectPath    project Path
     */
    private static void scanApplicationEndPoint(final String projectPath, Map<String, Object> map, ApplicationYml applicationYml)
    {
        try
        {
            String enableString = "enabled";

            // End points MAP
            Map<String, Object> endpointsMap = (Map<String, Object>) map.get("endpoints");

            // Restart property
            Map<String, Object> propertyMap = (Map<String, Object>) endpointsMap.get("restart");

            if (propertyMap.get(enableString) != null)
            {
                applicationYml.setEndpointRestart(((Boolean) propertyMap.get(enableString)).toString());
            }

            // Shutdown property
            propertyMap = (Map<String, Object>) endpointsMap.get("shutdown");

            if (propertyMap.get(enableString) != null)
            {
                applicationYml.setEndpointShutdown(((Boolean) propertyMap.get(enableString)).toString());
            }

            // Logfile property
            propertyMap = (Map<String, Object>) endpointsMap.get("logfile");

            if (propertyMap.get(enableString) != null)
            {
                applicationYml.setEndpointLogFile(((Boolean) propertyMap.get(enableString)).toString());
            }
        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanApplicationEndPoint]: There was and error parsing application Enpoints and not found from project path: [{}]", projectPath);
            LOG.trace("[ProjectFileReader] -> [scanApplicationEndPoint]: Enpoints not found: ", e);
        }
    }

    /**
     * Scan application server port
     *
     * @param map            application.yml map
     * @param applicationYml application yml object
     * @param projectPath    project Path
     */
    private static void scanApplicationServerPort(final String projectPath, Map<String, Object> map, ApplicationYml applicationYml)
    {
        try
        {
            Map<String, Object> serverMap = (Map<String, Object>) map.get("server");

            LOG.debug("[ProjectFileReader] -> [scanApplicationServerPort]: Server property: [{}]", serverMap);

            applicationYml.setServerPort((String) serverMap.get("port"));
        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanApplicationServerPort]: Server port not found in the application file: [{}]", projectPath);
            LOG.trace("[ProjectFileReader] -> [scanApplicationServerPort]: Server port not found: ", e);
        }
    }

    /**
     * Scan nova.yml build
     *
     * @param map         nova.yml map
     * @param novaYml     nova yml object
     * @param projectPath project Path
     */
    private static void scanNovaYmlBuild(final String projectPath, Map<String, Object> map, NovaYml novaYml)
    {
        try
        {
            // Builds map
            if (map.get(Constants.BUILD) != null)
            {
                // builds
                novaYml.getBuild().addAll((ArrayList<String>) map.get(Constants.BUILD));
                LOG.debug("[ProjectFileReader] -> [scanNovaYmlBuild]: Builds: [{}]", map.get(Constants.BUILD));
            }
        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlBuild]: No builds could be found build: [{}], project path: [{}]", novaYml.getBuild(), projectPath);
            LOG.trace("[ProjectFileReader] -> [scanNovaYmlBuild]: No build error: ", e);
        }
    }

    /**
     * Scan nova.yml dependencies
     *
     * @param map         nova.yml map
     * @param novaYml     nova yml object
     * @param projectPath project Path
     */
    private static void scanNovaYmlDependencies(final String projectPath, Map<String, Object> map, NovaYml novaYml)
    {
        try
        {
            // Dependencies map
            novaYml.getDependencies().addAll((ArrayList<String>) map.get("dependencies"));
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlDependencies]: Dependencies: [{}]", novaYml.getDependencies());
        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlDependencies]: Dependencies could not be found, properties: [{}], project path: [{}]", novaYml.getDependencies(), projectPath);
            LOG.trace("[ProjectFileReader] -> [scanNovaYmlDependencies]: Dependencies could not be found, error: [{}]. Error: ", projectPath, e);
        }
    }

    /**
     * Scan nova.yml libraries
     *
     * @param map         nova.yml map
     * @param novaYml     nova yml object
     * @param projectPath project Path
     */
    private static void scanNovaYmlLibraries(final String projectPath, Map<String, Object> map, NovaYml novaYml)
    {
        try
        {
            // Dependencies map
            novaYml.getLibraries().addAll((ArrayList<String>) map.get(Constants.LIBRARIES));
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlLibraries]: Libraries: [{}]", novaYml.getLibraries());
        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlLibraries]: Libraries could not be found, properties: [{}], project path: [{}]", novaYml.getLibraries(), projectPath);
            LOG.trace("[ProjectFileReader] -> [scanNovaYmlLibraries]: Libraries could not be found, error: ", e);
        }
    }

    /**
     * Scan nova.yml frontcat
     *
     * @param map         nova.yml map
     * @param novaYml     nova yml object
     * @param projectPath project Path
     */
    private static void scanNovaYmlFrontcat(final String projectPath, Map<String, Object> map, NovaYml novaYml)
    {

        try
        {
            // Service MAP
            Map<String, Object> frontcatMap = (Map<String, Object>) map.get(Constants.FRONTCAT);

            LOG.debug("[ProjectFileReader] -> [scanNovaYmlFrontcat]: Frontcat property: [{}]", frontcatMap);

            // junction
            novaYml.setJunction((String) frontcatMap.get("junction"));
            log.debug("[ProjectFileReader] -> [scanNovaYmlFrontcat]: Frontcat.junction:[{}]", frontcatMap.get("junction"));

            // contextPath
            novaYml.setContextPath((String) frontcatMap.get("contextPath"));
            log.debug("[ProjectFileReader] -> [scanNovaYmlFrontcat]: Frontcat.contextPath:[{}]", frontcatMap.get("contextPath"));

            // networkHostEnabled
            novaYml.setNetworkHostEnabled((Boolean) frontcatMap.get("networkHostEnabled"));
            log.debug("[ProjectFileReader] -> [scanNovaYmlFrontcat]: Frontcat.networkHostEnabled:[{}]", frontcatMap.get("networkHostEnabled"));

        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlFrontcat]: Frontcat properties could not be found, novaYml.contextPath: [{}], novaYml.junction: [{}], novaYml.junction: [{}], project path: [{}]",
                    novaYml.getContextPath(), novaYml.getJunction(), novaYml.isNetworkHostEnabled(), projectPath);
            LOG.trace("[ProjectFileReader] -> [scanNovaYmlFrontcat]: Frontcat properties could not be found, error: ", e);
        }
    }

    /**
     * Scan nova.yml machines
     *
     * @param map         nova.yml map
     * @param novaYml     nova yml object
     * @param projectPath project Path
     */
    private static void scanNovaYmlMachines(final String projectPath, Map<String, Object> map, NovaYml novaYml)
    {
        try
        {
            // Machines map
            if (map.get(Constants.MACHINES) != null)
            {
                // Machines
                novaYml.getMachines().addAll((ArrayList<String>) map.get(Constants.MACHINES));
                LOG.debug("[ProjectFileReader] -> [scanNovaYmlMachines]: Machines: [{}]", map.get(Constants.MACHINES));
            }
        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlMachines]: No machines could be found build: [{}], project path: [{}]", novaYml.getMachines(), projectPath);
            LOG.trace("[ProjectFileReader] -> [scanNovaYmlMachines]: No machines could be found, error: ", e);
        }
    }

    /**
     * Scan nova.yml ports
     *
     * @param map         nova.yml map
     * @param novaYml     nova yml object
     * @param projectPath project Path
     */
    private static void scanNovaYmlPorts(final String projectPath, Map<String, Object> map, NovaYml novaYml)
    {
        try
        {
            // Ports map
            ArrayList<Map<String, Object>> portsMap = (ArrayList<Map<String, Object>>) map.get("ports");
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlPorts]: PortsMap: [{}]", portsMap);
            for (Map<String, Object> port : portsMap)
            {
                NovaYmlPort novaYmlPort = new NovaYmlPort();
                // Getting the port fields
                // Port name
                if (port.get("name") != null)
                {
                    novaYmlPort.setName((String) port.get("name"));
                }
                else
                {
                    novaYmlPort.setName("");
                }

                // Port type
                if (port.get("type") != null)
                {
                    novaYmlPort.setType((String) port.get("type"));
                }
                else
                {
                    novaYmlPort.setType("");
                }

                // Port inside port
                if (port.get("insidePort") != null)
                {
                    novaYmlPort.setInsidePort((Integer) port.get("insidePort"));
                }
                else
                {
                    novaYmlPort.setInsidePort(0);
                }

                // Port inside outside
                if (port.get("outsidePort") != null)
                {
                    novaYmlPort.setOutsidePort((Integer) port.get("outsidePort"));
                }
                else
                {
                    novaYmlPort.setOutsidePort(0);
                }

                // Storing the port
                novaYml.getPorts().add(novaYmlPort);
            }

        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlPorts]: No port could be found, ports: [{}], project path: [{}]", novaYml.getPorts(), projectPath);
            LOG.trace("[ProjectFileReader] -> [scanNovaYmlPorts]: No port could be found, error: ", e);
        }
    }

    /**
     * Scan nova.yml properties
     *
     * @param map         nova.yml map
     * @param novaYml     nova yml object
     * @param projectPath project Path
     */
    private static void scanNovaYmlProperties(final String projectPath, Map<String, Object> map, NovaYml novaYml)
    {
        try
        {
            // Properties map
            ArrayList<Map<String, Object>> propertiesMap = (ArrayList<Map<String, Object>>) map.get("properties");
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlProperties]: Properties: [{}]", propertiesMap);
            for (Map<String, Object> property : propertiesMap)
            {

                NovaYamlProperty novaYamlProperty = new NovaYamlProperty();
                // Getting the property fields
                scanNovaYmlProperties(property, novaYamlProperty);

                // Storing the property
                novaYml.getProperties().add(novaYamlProperty);
            }
        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlProperties]: No property could be found, properties: [{}], project path: [{}]", novaYml.getProperties(), projectPath);
            LOG.trace("[ProjectFileReader] -> [scanNovaYmlProperties]: No property could be found, error: ", e);
        }
    }

    /**
     * Scan nova.yml context params
     *
     * @param map         nova.yml map
     * @param novaYml     nova yml object
     * @param projectPath project Path
     */
    private static void scanNovaYmlContextParams(final String projectPath, Map<String, Object> map, NovaYml novaYml)
    {
        try
        {
            // Context params map
            ArrayList<Map<String, Object>> contextParamsMap = (ArrayList<Map<String, Object>>) map.get("contextParams");
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlContextParams]: ContextParams: [{}]", contextParamsMap);
            for (Map<String, Object> property : contextParamsMap)
            {
                NovaYamlContextParam novaYamlContextParam = new NovaYamlContextParam();
                // Getting the context params fields
                scanNovaYmlContextParams(property, novaYamlContextParam);

                // Storing the context params
                novaYml.getContextParams().add(novaYamlContextParam);
            }
        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlContextParams]: No context param could be found: [{}], project path: [{}]", novaYml.getContextParams(), projectPath);
            LOG.trace("[ProjectFileReader] -> [scanNovaYmlContextParams]: No context param could be found, error: ", e);
        }
    }

    /**
     * Scan nova.yml input params
     *
     * @param map         nova.yml map
     * @param novaYml     nova yml object
     * @param projectPath project Path
     */
    private static void scanNovaYmlInputParams(final String projectPath, Map<String, Object> map, NovaYml novaYml)
    {
        try
        {
            // Input params map
            ArrayList<Map<String, Object>> inputParamsMap = (ArrayList<Map<String, Object>>) map.get("inputParams");
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlInputParams]: inputParams: [{}]", inputParamsMap);
            for (Map<String, Object> property : inputParamsMap)
            {
                NovaYamlInputParams novaYamlInputParam = new NovaYamlInputParams();
                // Getting the input fields
                scanNovaYmlInputParams(property, novaYamlInputParam);

                // Storing the input params
                novaYml.getInputParams().add(novaYamlInputParam);
            }
        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlInputParams]: No input parameters could be found: [{}], project path: [{}]", novaYml.getInputParams(), projectPath);
            LOG.trace("[ProjectFileReader] -> [scanNovaYmlInputParams]: No input parameters could be found, error: ", e);
        }
    }

    /**
     * Scan nova.yml output params
     *
     * @param map         nova.yml map
     * @param novaYml     nova yml object
     * @param projectPath project Path
     */
    private static void scanNovaYmlOutputParams(final String projectPath, Map<String, Object> map, NovaYml novaYml)
    {
        try
        {
            // Output params map
            ArrayList<Map<String, Object>> outputParams = (ArrayList<Map<String, Object>>) map.get("outputParams");
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlOutputParams]: outputParams: [{}]", outputParams);
            for (Map<String, Object> property : outputParams)
            {
                NovaYamlOutputParams novaYamlOutputParam = new NovaYamlOutputParams();
                // Getting the output fields
                scanNovaYmlOutputParams(property, novaYamlOutputParam);

                // Storing the context params
                novaYml.getOutputParams().add(novaYamlOutputParam);
            }
        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlOutputParams]: No output parameters could be found: [{}], project path: [{}]", novaYml.getOutputParams(), projectPath);
            LOG.trace("[ProjectFileReader] -> [scanNovaYmlOutputParams]: No output parameters could be found, error: ", e);
        }
    }

    /**
     * Scan the nova.yml properties parameter
     *
     * @param property         map property from nova.yml properties
     * @param novaYamlProperty nova yml property
     */
    private static void scanNovaYmlProperties(Map<String, Object> property, NovaYamlProperty novaYamlProperty)
    {
        // Property name
        if (property.get("name") != null)
        {
            novaYamlProperty.setName((String) property.get("name"));
        }
        else
        {
            novaYamlProperty.setName("");
        }

        // Property management
        if (property.get("management") != null)
        {
            novaYamlProperty.setManagement((String) property.get("management"));
        }
        else
        {
            novaYamlProperty.setManagement("");
        }

        // Property encrypt
        if (property.get("encrypt") != null)
        {
            if (property.get(Constants.ENCRYPT).getClass().equals(Boolean.class))
            {
                novaYamlProperty.setEncrypt((Boolean) property.get(Constants.ENCRYPT));
            }
            else
            {
                novaYamlProperty.setEncrypt(null);
            }
        }
        else
        {
            novaYamlProperty.setEncrypt(false);
        }

        // Property type
        if (property.get("type") != null)
        {
            novaYamlProperty.setType((String) property.get("type"));
        }
        else
        {
            novaYamlProperty.setType("");
        }

        // Property scope
        if (property.get("scope") != null)
        {
            novaYamlProperty.setScope((String) property.get("scope"));
        }
        else
        {
            novaYamlProperty.setScope("");
        }
    }

    /**
     * Scan nova.yml behavior test
     *
     * @param map         nova.yml map
     * @param novaYml     nova yml object
     * @param projectPath project Path
     */
    private static void scanNovaYmlBehaviorTest(final String projectPath, final Map<String, Object> map, final NovaYml novaYml)
    {
        try
        {
            // Behavior MAP
            Map<String, Object> behaviorTestMap = (Map<String, Object>) map.get("behavior");
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlBehaviorTest]: Behavior Test properties: [{}]", behaviorTestMap);

            novaYml.setReleaseVersion((String) behaviorTestMap.get("releaseVersion"));
            novaYml.setTags((String) behaviorTestMap.get("tags"));
        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlBehaviorTest]: A behavior property could not be found in the project path: [{}]", projectPath);
            LOG.trace("[ProjectFileReader] -> [scanNovaYmlBehaviorTest]: A behavior property could not be found. Error: ", e);
        }
    }

    /**
     * Scan nova.yml requirements
     *
     * @param map     nova.yml map
     * @param novaYml nova yml object
     */
    private static void scanNovaYmlRequirements(final String projectPath, Map<String, Object> map, NovaYml novaYml)
    {
        try
        {
            // Requirements map
            List<Map<String, Object>> requirementsMap = (ArrayList<Map<String, Object>>) map.get(Constants.REQUIREMENTS);
            if (requirementsMap == null)
            {
                LOG.debug("[ProjectFileReader] -> [scanNovaYmlRequirements]: No requirement could be found [{}], project path: [{}]", novaYml.getRequirements(), projectPath);
                return;
            }

            LOG.debug("[ProjectFileReader] -> [scanNovaYmlRequirements]: Requirements: [{}]", requirementsMap);
            for (Map<String, Object> property : requirementsMap)
            {
                NovaYmlRequirement novaYmlRequirement = new NovaYmlRequirement();

                // Getting the requirement fields
                scanNovaYmlRequirementsParameters(property, novaYmlRequirement);

                // Storing the property
                novaYml.getRequirements().add(novaYmlRequirement);
            }

        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlRequirements]: No requirement could be found [{}], project path: [{}], error: ", novaYml.getRequirements(), projectPath, e);
        }
    }

    /**
     * Scan the nova.yml requirements parameter
     *
     * @param property           map property from nova.yml requirements
     * @param novaYmlRequirement nova yml requirement
     */
    private static void scanNovaYmlRequirementsParameters(Map<String, Object> property, NovaYmlRequirement novaYmlRequirement)
    {
        // Requirement name
        if (property.get("name") != null)
        {
            novaYmlRequirement.setName((String) property.get("name"));
        }
        else
        {
            novaYmlRequirement.setName("");
        }

        // Requirement type
        if (property.get("type") != null)
        {
            novaYmlRequirement.setType((String) property.get("type"));
        }
        else
        {
            novaYmlRequirement.setType("");
        }

        // Requirement value
        if (property.get("value") != null)
        {
            novaYmlRequirement.setValue((String) property.get("value"));
        }
        else
        {
            novaYmlRequirement.setValue("");
        }

        // Requirement description
        if (property.get(DESCRIPTION) != null)
        {
            novaYmlRequirement.setDescription((String) property.get(DESCRIPTION));
        }
        else
        {
            novaYmlRequirement.setDescription("");
        }
    }

    /**
     * Scan the nova.yml context parameters
     *
     * @param property             map property from nova.yml properties
     * @param novaYamlContextParam nova yml context params
     */
    private static void scanNovaYmlContextParams(Map<String, Object> property, NovaYamlContextParam novaYamlContextParam)
    {
        // Context param name
        if (property.get("name") != null)
        {
            novaYamlContextParam.setName((String) property.get("name"));
        }
        else
        {
            novaYamlContextParam.setName("");
        }

        // Context param description
        if (property.get(DESCRIPTION) != null)
        {
            novaYamlContextParam.setDescription((String) property.get(DESCRIPTION));
        }
        else
        {
            novaYamlContextParam.setDescription("");
        }

        // Context param default value
        if (property.get("defaultValue") != null)
        {
            novaYamlContextParam.setDefaultValue((String) property.get("defaultValue"));
        }
        else
        {
            novaYamlContextParam.setDefaultValue("");
        }

        // Context param type
        String contextParamTypeValue = STRING;
        String contextParamType = (String) property.get("type");

        if (STRING.equals(contextParamType) || INTEGER.equals(contextParamType) || BOOLEAN.equals(contextParamType))
        {
            contextParamTypeValue = contextParamType;
        }

        novaYamlContextParam.setType(contextParamTypeValue);
    }

    /**
     * Scan the nova.yml input parameters
     *
     * @param property            map property from nova.yml properties
     * @param novaYamlInputParams nova yml input params
     */
    private static void scanNovaYmlInputParams(Map<String, Object> property, NovaYamlInputParams novaYamlInputParams)
    {
        // Input param name
        if (property.get("name") != null)
        {
            novaYamlInputParams.setName((String) property.get("name"));
        }
        else
        {
            novaYamlInputParams.setName("");
        }

        // Input param description
        if (property.get(DESCRIPTION) != null)
        {
            novaYamlInputParams.setDescription((String) property.get(DESCRIPTION));
        }
        else
        {
            novaYamlInputParams.setDescription("");
        }

        // Input param mandatory value
        if (property.get(MANDATORY) != null)
        {
            novaYamlInputParams.setMandatory((Boolean.getBoolean((String) property.get(MANDATORY))));
        }
        else
        {
            novaYamlInputParams.setMandatory(false);
        }

        // Input param type
        String inputParamTypeValue = STRING;
        String inputParamType = (String) property.get("type");

        if (STRING.equals(inputParamType) || INTEGER.equals(inputParamType) || BOOLEAN.equals(inputParamType))
        {
            inputParamTypeValue = inputParamType;
        }

        novaYamlInputParams.setType(inputParamTypeValue);
    }

    /**
     * Scan the nova.yml output parameters
     *
     * @param property             map property from nova.yml properties
     * @param novaYamlOutputParams nova yml output params
     */
    private static void scanNovaYmlOutputParams(Map<String, Object> property, NovaYamlOutputParams novaYamlOutputParams)
    {
        // Output param name
        if (property.get("name") != null)
        {
            novaYamlOutputParams.setName((String) property.get("name"));
        }
        else
        {
            novaYamlOutputParams.setName("");
        }

        // Output param description
        if (property.get(DESCRIPTION) != null)
        {
            novaYamlOutputParams.setDescription((String) property.get(DESCRIPTION));
        }
        else
        {
            novaYamlOutputParams.setDescription("");
        }

        // Output param mandatory value
        if (property.get(MANDATORY) != null)
        {
            novaYamlOutputParams.setMandatory((Boolean.getBoolean((String) property.get(MANDATORY))));
        }
        else
        {
            novaYamlOutputParams.setMandatory(false);
        }

        // Output param type
        String outputParamTypeValue = STRING;
        String outputParamType = (String) property.get("type");

        if (STRING.equals(outputParamType) || INTEGER.equals(outputParamType) || BOOLEAN.equals(outputParamType))
        {
            outputParamTypeValue = outputParamType;
        }

        novaYamlOutputParams.setType(outputParamTypeValue);
    }

    /**
     * Scan nova.yml service
     *
     * @param map         nova.yml map
     * @param novaYml     nova yml object
     * @param projectPath project Path
     */
    private static void scanNovaYmlService(final String projectPath, Map<String, Object> map, NovaYml novaYml)
    {
        try
        {
            // Service MAP
            Map<String, Object> serviceMap = (Map<String, Object>) map.get("service");

            LOG.debug("[ProjectFileReader] -> [scanNovaYmlService]: Service property: [{}]", serviceMap);

            // UUAA
            novaYml.setUuaa((String) serviceMap.get("uuaa"));

            // Name
            novaYml.setName((String) serviceMap.get("name"));

            // Description
            novaYml.setDescription((String) serviceMap.get(DESCRIPTION));

            // Type
            novaYml.setServiceType(((String) serviceMap.get("type")).replace(" ", "_").toUpperCase());

            // Language and Language version(excepting for BATCH SCHEDULER NOVA type due it has not set language from NOVA type)
            String language = (String) serviceMap.get("language");

            if (!Strings.isNullOrEmpty(language))
            {
                novaYml.setLanguage(language.replace(".", "_").toUpperCase()
                        // Removes hyphen (-) from language
                        .replace("-", "")
                        // Removes double spaces ( ) from language
                        .replace("  ", " ")
                        // Change space ( ) to underscore (_) in the service type name
                        .replace(" ", "_"));

                // Language version
                novaYml.setLanguageVersion((String) serviceMap.get("languageVersion"));
            }
            else
            {
                // Batch scheduler service does not have any language. Set default language: NOVA
                novaYml.setLanguage(ServiceLanguage.NOVA.getServiceLanguage());
            }

            if (serviceMap.containsKey("jdkVersion"))
            {
                novaYml.setJdkVersion((String) serviceMap.get("jdkVersion"));
            }

            // Nova version
            novaYml.setNovaVersion((String) serviceMap.get("novaVersion"));

            // Version
            novaYml.setVersion((String) serviceMap.get("version"));

            // ApplicationName (only for cells services)
            novaYml.setApplicationName((String) serviceMap.get("applicationName"));

            // ProjectName (only for cells services)
            novaYml.setProjectName((String) serviceMap.get("projectName"));

            // Test Framework (only for behavior test)
            novaYml.setTestFramework((String) serviceMap.get("testFramework"));

            // Test Framework Version (only for behavior test)
            novaYml.setTestFrameworkVersion((String) serviceMap.get("testFrameworkVersion"));
        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlService]: A service property could not be found in the project path: [{}]", projectPath);
            LOG.trace("[ProjectFileReader] -> [scanNovaYmlService]: A service property could not be found. Error: ", e);
        }
    }

    /**
     * Scan nova.yml swagger
     *
     * @param map         nova.yml map
     * @param novaYml     nova yml object
     * @param projectPath project Path
     */
    private static void scanNovaYmlSwagger(final String projectPath, Map<String, Object> map, NovaYml novaYml)
    {
        try
        {
            // Apis map
            Map<String, Object> apisMap = (Map<String, Object>) map.get("apis");
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlSwagger]: Apis property: [{}]", apisMap);

            // Swaggers array list
            ArrayList<Map<String, Object>> swaggers = (ArrayList<Map<String, Object>>) apisMap.get("served");
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlSwagger]: Swaggers path: [{}]", swaggers);

            for (Map<String, Object> served : swaggers)
            {
                NovaYmlApi servedApi = new NovaYmlApi();
                if (served.get(Constants.DEFINITION) != null)
                {
                    servedApi.setApi((String) served.get(Constants.DEFINITION));
                    LOG.debug("[ProjectFileReader] -> [scanNovaYmlSwagger]: Adding swagger server: [{}]", served.get(Constants.DEFINITION));
                }
                if (served.get(Constants.CONSUMED) != null)
                {
                    servedApi.setConsumedApi((ArrayList<String>) served.get(Constants.CONSUMED));
                    LOG.debug("[ProjectFileReader] -> [scanNovaYmlSwagger]: Adding clients swagger: [{}]", served.get(Constants.CONSUMED));
                }
                else
                {
                    List<String> consumedApiList = new ArrayList<>();
                    servedApi.setConsumedApi(consumedApiList);
                }
                if (served.get(Constants.EXTERNAL) != null)
                {
                    servedApi.setExternalApi((ArrayList<String>) served.get(Constants.EXTERNAL));
                    LOG.debug("[ProjectFileReader] -> [scanNovaYmlSwagger]: Adding clients swagger: [{}]", served.get(Constants.EXTERNAL));
                }
                else
                {
                    List<String> consumedExternalApiList = new ArrayList<>();
                    servedApi.setExternalApi(consumedExternalApiList);
                }
                if (served.get(Constants.SUPPORTED_VERSIONS) != null)
                {
                    servedApi.setBackwardCompatibleVersions((ArrayList<String>) served.get(Constants.SUPPORTED_VERSIONS));
                    LOG.debug("[ProjectFileReader] -> [scanNovaYmlSwagger]: Adding backwards compatible versions: [{}]", served.get(Constants.SUPPORTED_VERSIONS));
                }
                else
                {
                    List<String> backwardCompatibleVersions = new ArrayList<>();
                    servedApi.setBackwardCompatibleVersions(backwardCompatibleVersions);
                }

                novaYml.getApiServed().add(servedApi);
            }
        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.warn("[ProjectFileReader] -> [scanNovaYmlSwagger]: Swagger served service or client could not be found service: [{}], client: [{}], project path: [{}]",
                    novaYml.getApiServed(), novaYml.getApiServed(), projectPath);
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlSwagger]: Swagger served service or client could not be found, error: ", e);
        }

        try
        {
            // Apis map
            Map<String, Object> apisMap = (Map<String, Object>) map.get("apis");
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlSwagger]: Apis property: [{}]", apisMap);

            if (apisMap.get(Constants.CONSUMED) != null)
            {
                // Swaggers array list
                for (String api : (ArrayList<String>) apisMap.get(Constants.CONSUMED))
                {
                    NovaYmlApi novaYmlApi = new NovaYmlApi();
                    novaYmlApi.setApi(api);
                    novaYml.getApiConsumed().add(novaYmlApi);
                }
                LOG.debug(SWAGGERS_PATH_TEMPLATE, apisMap.get(Constants.CONSUMED));
            }
            if (apisMap.get(Constants.EXTERNAL) != null)
            {
                // Swaggers array list
                for (String api : (ArrayList<String>) apisMap.get(Constants.EXTERNAL))
                {
                    NovaYmlApi novaYmlApi = new NovaYmlApi();
                    novaYmlApi.setApi(api);
                    novaYml.getApiExternal().add(novaYmlApi);
                }
                LOG.debug(SWAGGERS_PATH_TEMPLATE, apisMap.get(Constants.EXTERNAL));
            }

        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.warn("[ProjectFileReader] -> [scanNovaYmlSwagger]: Swagger clients could not be found clients: [{}], project path: [{}]", novaYml.getApiConsumed(), projectPath);
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlSwagger]: Swagger clients could not be found clients, error: ", e);
        }
    }

    /**
     * Scan nova.yml asyncapi definitions
     *
     * @param map         nova.yml map
     * @param novaYml     nova yml object
     * @param projectPath project Path
     * @param apiModality api modality
     */
    private static void scanNovaYmlAsyncApi(final String projectPath, Map<String, Object> map, NovaYml novaYml, ApiModality apiModality)
    {

        try
        {
            var asyncApisMap = (Map<String, Object>) map.get("asyncapis");
            // AsyncApis map
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlAsyncApi]: AsyncApis property: [{}]", asyncApisMap);
            if (ApiModality.ASYNC_BACKTOBACK.equals(apiModality))
            {


                // BackToBack
                novaYml.setAsyncapisBackToBack(
                        NovaYmlAsyncApi.getInstance(
                                (ArrayList<String>) asyncApisMap.get("backToBack")
                        ));
                LOG.debug("[ProjectFileReader] -> [scanNovaYmlAsyncApi]: AsyncApis BackToBack property: [{}]", novaYml.getAsyncapisBackToBack());
            }
            else
            {
                // BackToFront
                novaYml.setAsyncapisBackToFront(
                        NovaYmlAsyncApi.getInstance(
                                (ArrayList<String>) asyncApisMap.get("backToFront")
                        ));
                LOG.debug("[ProjectFileReader] -> [scanNovaYmlAsyncApi]: AsyncApis BackToFront property: [{}]", novaYml.getAsyncapisBackToFront());
            }


        }
        catch (ClassCastException | NullPointerException e)
        {
            LOG.debug("[ProjectFileReader] -> [scanNovaYmlAsyncApi]: AsyncApis [{}] could not be found, backToBach: [{}], backToFront: [{}], project path: [{}]", apiModality, novaYml.getAsyncapisBackToBack(), novaYml.getAsyncapisBackToFront(), projectPath);
            LOG.trace("[ProjectFileReader] -> [scanNovaYmlAsyncApi]: AsyncApis [{}] could not be found, error: ", apiModality, e);
        }

    }

    /**
     * Build the POM params required for building the service DTO
     *
     * @param validatorInputs validator inputs
     * @param model           model
     */
    private static void scanServiceBuilderParameters(ValidatorInputs validatorInputs, Model model)
    {
        // Name
        if (model.getName() != null)
        {
            validatorInputs.getPom().setName(model.getName());
        }
        else
        {
            validatorInputs.getPom().setName("");
        }

        // Description
        if (model.getDescription() != null)
        {
            validatorInputs.getPom().setDescription(model.getDescription());
        }
        else
        {
            validatorInputs.getPom().setDescription("");
        }

        // Build
        if (model.getBuild() != null)
        {
            validatorInputs.getPom().setBuild(model.getBuild());
        }
        else
        {
            validatorInputs.getPom().setBuild(null);
        }

        // Modules
        if (model.getModules() != null)
        {
            validatorInputs.getPom().setModules(model.getModules());
        }
        else
        {
            validatorInputs.getPom().setModules(null);
        }

        if (!validatorInputs.isLatestVersion())
        {
            // Properties
            if (model.getProperties() != null)
            {
                validatorInputs.getPom().setProperties(model.getProperties());
            }
            else
            {
                validatorInputs.getPom().setProperties(null);
            }
        }

        //Dependencies from Pom
        if (model.getDependencies() != null)
        {
            validatorInputs.getPom().addAllDependencies(model.getDependencies());
        }
    }

    /**
     * Scan the literal (Group Id, Artifact Id, Packaging, Final Name) parameters for validation of pom.xml
     *
     * @param validatorInputs validation inputs
     * @param model           Model of pom.xml file
     */
    private static void scanValidatingPomLiterals(ValidatorInputs validatorInputs, Model model)
    {
        // Group Id
        if (model.getGroupId() != null)
        {
            validatorInputs.getPom().setGroupId(model.getGroupId());
        }
        else
        {
            validatorInputs.getPom().setGroupId("");
        }

        // Artifact Id
        if (model.getArtifactId() != null)
        {
            validatorInputs.getPom().setArtifactId(model.getArtifactId());
        }
        else
        {
            validatorInputs.getPom().setArtifactId("");
        }
        // Packaging
        if (model.getPackaging() != null)
        {
            validatorInputs.getPom().setPackaging(model.getPackaging());
        }
        else
        {
            validatorInputs.getPom().setPackaging("");
        }

        // Final Name
        if (model.getBuild().getFinalName() != null)
        {
            String groupId = validatorInputs.getPom().getGroupId();

            String artifactId = validatorInputs.getPom().getArtifactId();

            String finalName = model.getBuild().getFinalName();

            if (finalName.equals(Constants.FINAL_NAME_WITH_MAVEN_VARIABLES))
            {

                finalName = groupId + "-" + artifactId;
            }
            // Set final name.
            validatorInputs.getPom().setFinalName(finalName);
        }
        else
        {
            validatorInputs.getPom().setFinalName("");
        }

        // Add the artifact Id to the list, to check if it is unique
        if (model.getGroupId() != null && model.getArtifactId() != null)
        {
            PomXML.addArtifactId(model.getGroupId(), model.getArtifactId());
        }
        else
        {
            PomXML.addArtifactId("", "");
        }
    }

    /**
     * Build the POM params required for validation
     *
     * @param validatorInputs validator inputs
     * @param model           model
     */
    private static void scanValidatingPomParameters(ValidatorInputs validatorInputs, Model model)
    {
        if (!validatorInputs.isLatestVersion())
        {

            // UUAA Name
            if (model.getProperties().getProperty("uuaa.name") != null)
            {
                validatorInputs.getPom().setUuaaName(model.getProperties().getProperty("uuaa.name"));
            }
            else
            {
                validatorInputs.getPom().setUuaaName("");
            }

            // Nova Version
            validatorInputs.getPom().setNovaVersion(model.getProperties().getProperty("nova.version"));

            // Plugin Nova Starter Version
            if (model.getBuild().getPluginsAsMap() != null)
            {
                if (model.getBuild().getPluginsAsMap().get(ENOA_PLUGIN_GROUP_ARTIFACT_ID) == null
                        || model.getBuild().getPluginsAsMap().get(ENOA_PLUGIN_GROUP_ARTIFACT_ID).getVersion() == null)
                {
                    validatorInputs.getPom().setPlugin("0");
                }
                else
                {
                    validatorInputs.getPom().setPlugin(
                            model.getBuild().getPluginsAsMap().get(ENOA_PLUGIN_GROUP_ARTIFACT_ID).getVersion());
                }
            }
            else
            {
                validatorInputs.getPom().setPlugin("");
            }
        }
        else
        {
            // Plugin Nova Starter Version
            if (model.getBuild().getPluginsAsMap() != null)
            {
                if (model.getBuild().getPluginsAsMap().get("org.springframework.boot:spring-boot-maven-plugin") != null)
                {
                    Xpp3Dom xpp = (Xpp3Dom) model.getBuild().getPluginsAsMap()
                            .get("org.springframework.boot:spring-boot-maven-plugin").getConfiguration();
                    if (xpp == null || xpp.getChild("outputDirectory") == null)
                    {
                        validatorInputs.getPom().setPlugin("");
                    }
                    else
                    {
                        validatorInputs.getPom().setPlugin(xpp.getChild("outputDirectory").getValue());
                    }
                }
            }
            else
            {
                validatorInputs.getPom().setPlugin("");
            }
        }

        // Literals: Group Id, Artifact Id, Packaging, Final Name
        scanValidatingPomLiterals(validatorInputs, model);
        // Version: Service Version, Parent Version
        scanValidatingPomVersion(validatorInputs, model);
    }

    /**
     * Scan the version (Service Version, Parent Version) parameters for validation of pom.xml
     *
     * @param validatorInputs validation inputs
     * @param model           Model of pom.xml file
     */
    private static void scanValidatingPomVersion(ValidatorInputs validatorInputs, Model model)
    {
        // Version
        if (model.getVersion() != null)
        {
            validatorInputs.getPom().setVersion(model.getVersion());
        }
        else
        {
            validatorInputs.getPom().setVersion("");
        }

        // Parent Version
        if (model.getParent() == null)
        {
            validatorInputs.getPom().setParentVersion(null);
        }
        else
        {
            validatorInputs.getPom().setParentVersion(model.getParent().getVersion());
        }
    }

    /**
     * Set the nova.yml parameters into the ValidatorInputs
     *
     * @param validatorInputs validator input
     * @param novaYml         Filled novaYml object
     */
    private static void setNotNullNovaYmlParameters(ValidatorInputs validatorInputs, NovaYml novaYml)
    {
        setNotNullNovaYmlParametersService(validatorInputs, novaYml);

        setNotNullNovaYmlParametersSwagger(validatorInputs, novaYml);

        setNotNullNovaYmlParametersAsyncApis(validatorInputs, novaYml);

        setNotNullNovaYmlParametersProperties(validatorInputs, novaYml);

        setNotNullNovaYmlContextParams(validatorInputs, novaYml);

        setNotNullNovaYmlInputParams(validatorInputs, novaYml);

        setNotNullNovaYmlOutputParams(validatorInputs, novaYml);

        setNotNullNovaYmlRequirements(validatorInputs, novaYml);

        setNotNullNovaYmlLibraries(validatorInputs, novaYml);

        setNotNullNovaYmlParametersFrontcat(validatorInputs, novaYml);

        setNotNullNovaYmlParametersBehavior(validatorInputs, novaYml);

        if (novaYml.getPorts() != null)
        {
            validatorInputs.getNovaYml().setPorts(novaYml.getPorts());
        }
        else
        {
            validatorInputs.getNovaYml().setPorts(new ArrayList<>());
        }

        if (novaYml.getBuild() != null)
        {
            validatorInputs.getNovaYml().setBuild(novaYml.getBuild());
        }
        else
        {
            validatorInputs.getNovaYml().setBuild(new ArrayList<>());
        }

        if (novaYml.getMachines() != null)
        {
            validatorInputs.getNovaYml().setMachines(novaYml.getMachines());
        }
        else
        {
            validatorInputs.getNovaYml().setMachines(new ArrayList<>());
        }

        if (novaYml.getDependencies() != null)
        {
            validatorInputs.getNovaYml().setDependencies(novaYml.getDependencies());
        }
        else
        {
            validatorInputs.getNovaYml().setDependencies(new ArrayList<>());
        }
    }

    private static void setNotNullNovaYmlParametersAsyncApis(final ValidatorInputs validatorInputs, final NovaYml novaYml)
    {
        if (novaYml.getAsyncapisBackToBack() != null)
        {
            validatorInputs.getNovaYml().setAsyncapisBackToBack(novaYml.getAsyncapisBackToBack());
        }

        if (novaYml.getAsyncapisBackToFront() != null)
        {
            validatorInputs.getNovaYml().setAsyncapisBackToFront(novaYml.getAsyncapisBackToFront());
        }

    }

    private static void setNotNullNovaYmlParametersFrontcat(final ValidatorInputs validatorInputs, final NovaYml novaYml)
    {
        if (novaYml.getContextPath() != null)
        {
            validatorInputs.getNovaYml().setContextPath(novaYml.getContextPath());
        }
        else
        {
            validatorInputs.getNovaYml().setContextPath("");
        }
        if (novaYml.getJunction() != null)
        {
            validatorInputs.getNovaYml().setJunction(novaYml.getJunction());
        }
        else
        {
            validatorInputs.getNovaYml().setJunction("");
        }
        if (novaYml.isNetworkHostEnabled())
        {
            validatorInputs.getNovaYml().setNetworkHostEnabled(novaYml.isNetworkHostEnabled());
        }
        else
        {
            validatorInputs.getNovaYml().setNetworkHostEnabled(false);
        }
    }

    private static void setNotNullNovaYmlParametersBehavior(final ValidatorInputs validatorInputs, final NovaYml novaYml)
    {
        if (novaYml.getReleaseVersion() == null)
        {
            validatorInputs.getNovaYml().setReleaseVersion("");
        }
        else
        {
            validatorInputs.getNovaYml().setReleaseVersion(novaYml.getReleaseVersion());
        }

        if (novaYml.getTags() == null)
        {
            validatorInputs.getNovaYml().setTags("");
        }
        else
        {
            validatorInputs.getNovaYml().setTags(novaYml.getTags());
        }
    }


    /**
     * Set the nova.yml properties parameters into the ValidatorInputs
     *
     * @param validatorInputs validator input
     * @param novaYml         Filled novaYml object
     */
    private static void setNotNullNovaYmlParametersProperties(ValidatorInputs validatorInputs, NovaYml novaYml)
    {
        if (novaYml.getProperties() != null)
        {
            validatorInputs.getNovaYml().setProperties(novaYml.getProperties());
        }
        else
        {
            validatorInputs.getNovaYml().setProperties(new ArrayList<>());
        }
    }

    /**
     * Set the nova.yml context params into the ValidatorInputs
     *
     * @param validatorInputs validator input
     * @param novaYml         Filled novaYml object
     */
    private static void setNotNullNovaYmlContextParams(ValidatorInputs validatorInputs, NovaYml novaYml)
    {
        if (novaYml.getContextParams() != null)
        {
            validatorInputs.getNovaYml().setContextParams(novaYml.getContextParams());
        }
        else
        {
            validatorInputs.getNovaYml().setContextParams(new ArrayList<>());
        }
    }

    /**
     * Set the nova.yml input params into the ValidatorInputs
     *
     * @param validatorInputs validator input
     * @param novaYml         Filled novaYml object
     */
    private static void setNotNullNovaYmlInputParams(ValidatorInputs validatorInputs, NovaYml novaYml)
    {
        if (novaYml.getInputParams() != null)
        {
            validatorInputs.getNovaYml().setInputParams(novaYml.getInputParams());
        }
        else
        {
            validatorInputs.getNovaYml().setInputParams(new ArrayList<>());
        }
    }

    /**
     * Set the nova.yml output params into the ValidatorInputs
     *
     * @param validatorInputs validator input
     * @param novaYml         Filled novaYml object
     */
    private static void setNotNullNovaYmlOutputParams(ValidatorInputs validatorInputs, NovaYml novaYml)
    {
        if (novaYml.getOutputParams() != null)
        {
            validatorInputs.getNovaYml().setOutputParams(novaYml.getOutputParams());
        }
        else
        {
            validatorInputs.getNovaYml().setOutputParams(new ArrayList<>());
        }
    }

    /**
     * Set the nova.yml service parameters into the ValidatorInputs
     *
     * @param validatorInputs validator input
     * @param novaYml         Filled novaYml object
     */
    private static void setNotNullNovaYmlParametersService(ValidatorInputs validatorInputs, NovaYml novaYml)
    {

        setNotNullNovaYmlParametersServiceInfo(validatorInputs, novaYml);

        setNotNullNovaYmlParametersServiceType(validatorInputs, novaYml);
    }

    /**
     * Set the nova.yml general definition parameters into the ValidatorInputs
     *
     * @param validatorInputs validator input
     * @param novaYml         Filled novaYml object
     */
    private static void setNotNullNovaYmlParametersServiceInfo(ValidatorInputs validatorInputs, NovaYml novaYml)
    {
        if (novaYml.getUuaa() != null)
        {
            validatorInputs.getNovaYml().setUuaa(novaYml.getUuaa());
        }
        else
        {
            validatorInputs.getNovaYml().setUuaa("");
        }
        if (novaYml.getDescription() != null)
        {
            validatorInputs.getNovaYml().setDescription(novaYml.getDescription());
        }
        else
        {
            validatorInputs.getNovaYml().setDescription("");
        }
        if (novaYml.getName() != null)
        {
            validatorInputs.getNovaYml().setName(novaYml.getName());
        }
        else
        {
            validatorInputs.getNovaYml().setName("");
        }
        if (novaYml.getVersion() != null)
        {
            validatorInputs.getNovaYml().setVersion(novaYml.getVersion());
        }
        else
        {
            validatorInputs.getNovaYml().setVersion("");
        }

        if (novaYml.getNovaVersion() != null)
        {
            validatorInputs.getNovaYml().setNovaVersion(novaYml.getNovaVersion());
        }
        else
        {
            validatorInputs.getNovaYml().setNovaVersion("");
        }

        if (novaYml.getApplicationName() != null)
        {
            validatorInputs.getNovaYml().setApplicationName(novaYml.getApplicationName());
        }
        else
        {
            validatorInputs.getNovaYml().setApplicationName("");
        }

        if (novaYml.getProjectName() != null)
        {
            validatorInputs.getNovaYml().setProjectName(novaYml.getProjectName());
        }
        else
        {
            validatorInputs.getNovaYml().setProjectName("");
        }

        if (novaYml.getTestFramework() == null)
        {
            validatorInputs.getNovaYml().setTestFramework("");
        }
        else
        {
            validatorInputs.getNovaYml().setTestFramework(novaYml.getTestFramework());
        }

        if (novaYml.getTestFrameworkVersion() == null)
        {
            validatorInputs.getNovaYml().setTestFrameworkVersion("");
        }
        else
        {
            validatorInputs.getNovaYml().setTestFrameworkVersion(novaYml.getTestFrameworkVersion());
        }
    }

    /**
     * Set the nova.yml service type and language parameters into the ValidatorInputs
     *
     * @param validatorInputs validator input
     * @param novaYml         Filled novaYml object
     */
    private static void setNotNullNovaYmlParametersServiceType(ValidatorInputs validatorInputs, NovaYml novaYml)
    {
        String language = Constants.INVALID;
        boolean existNovaYmlLanguage = novaYml.getLanguage() != null;
        if (existNovaYmlLanguage && ServiceLanguage.getValueOf(novaYml.getLanguage()) != null)
        {
            language = novaYml.getLanguage();
        }
        NovaYml validatorInputsNovaYml = validatorInputs.getNovaYml();
        validatorInputsNovaYml.setLanguage(language);

        String languageVersion = novaYml.getLanguageVersion() != null ? novaYml.getLanguageVersion() : DEFAULT_VERSION;
        validatorInputsNovaYml.setLanguageVersion(languageVersion);

        String jdkVersion = novaYml.getJdkVersion() != null ? novaYml.getJdkVersion() : DEFAULT_VERSION;
        validatorInputsNovaYml.setJdkVersion(jdkVersion);

        if (novaYml.getTestFramework() == null)
        {
            validatorInputs.getNovaYml().setTestFramework("");
        }
        else
        {
            validatorInputs.getNovaYml().setTestFramework(novaYml.getTestFramework());
        }

        if (novaYml.getTestFrameworkVersion() == null)
        {
            validatorInputs.getNovaYml().setTestFrameworkVersion("");
        }
        else
        {
            validatorInputs.getNovaYml().setTestFrameworkVersion(novaYml.getTestFrameworkVersion());
        }

        String serviceType = Constants.INVALID;
        if (novaYml.getServiceType() != null && existNovaYmlLanguage)
        {
            String serviceTypeAndLanguage = novaYml.getServiceType().toUpperCase();
            if (!novaYml.getServiceType().toUpperCase().equalsIgnoreCase(ServiceType.DEPENDENCY.getServiceType()))
            {
                serviceTypeAndLanguage = (novaYml.getServiceType().toUpperCase() + "_" + novaYml.getLanguage());
            }
            if (ServiceType.getValueOf(serviceTypeAndLanguage) != null)
            {
                serviceType = serviceTypeAndLanguage;
            }
        }
        validatorInputsNovaYml.setServiceType(serviceType);
    }

    /**
     * Set the nova.yml swagger parameters into the ValidatorInputs
     *
     * @param validatorInputs validator input
     * @param novaYml         Filled novaYml object
     */
    private static void setNotNullNovaYmlParametersSwagger(ValidatorInputs validatorInputs, NovaYml novaYml)
    {

        if (novaYml.getApiServed() != null)
        {
            validatorInputs.getNovaYml().setApiServed(novaYml.getApiServed());
        }
        else
        {
            validatorInputs.getNovaYml().setApiServed(new ArrayList<>());
        }

        if (novaYml.getApiConsumed() != null)
        {
            validatorInputs.getNovaYml().setApiConsumed(novaYml.getApiConsumed());
        }
        else
        {
            validatorInputs.getNovaYml().setApiConsumed(new ArrayList<>());
        }

        if (novaYml.getApiExternal() != null)
        {
            validatorInputs.getNovaYml().setApiExternal(novaYml.getApiExternal());
        }
        else
        {
            validatorInputs.getNovaYml().setApiExternal(new ArrayList<>());
        }
    }

    /**
     * Set the nova.yml requirements (for library service) into the ValidatorInputs
     *
     * @param validatorInputs validator input
     * @param novaYml         Filled novaYml object
     */
    private static void setNotNullNovaYmlRequirements(ValidatorInputs validatorInputs, NovaYml novaYml)
    {
        if (novaYml.getRequirements() != null)
        {
            validatorInputs.getNovaYml().setRequirements(novaYml.getRequirements());
        }
        else
        {
            validatorInputs.getNovaYml().setRequirements(new ArrayList<>());
        }
    }

    /**
     * Set the nova.yml libraries (for service which uses libraries) into the ValidatorInputs
     *
     * @param validatorInputs validator input
     * @param novaYml         Filled novaYml object
     */
    private static void setNotNullNovaYmlLibraries(ValidatorInputs validatorInputs, NovaYml novaYml)
    {
        if (novaYml.getLibraries() != null)
        {
            validatorInputs.getNovaYml().setLibraries(novaYml.getLibraries());
        }
        else
        {
            validatorInputs.getNovaYml().setLibraries(new ArrayList<>());
        }
    }

}
