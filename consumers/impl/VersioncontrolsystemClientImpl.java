package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.versioncontrolsystemapi.client.feign.nova.rest.IRestHandlerVersioncontrolsystemapi;
import com.bbva.enoa.apirestgen.versioncontrolsystemapi.client.feign.nova.rest.IRestListenerVersioncontrolsystemapi;
import com.bbva.enoa.apirestgen.versioncontrolsystemapi.client.feign.nova.rest.impl.RestHandlerVersioncontrolsystemapi;
import com.bbva.enoa.apirestgen.versioncontrolsystemapi.model.VCSProject;
import com.bbva.enoa.apirestgen.versioncontrolsystemapi.model.VCSTag;
import com.bbva.enoa.core.novabootstarter.consumers.SingleApiClientResponseWrapper;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.exceptions.ReleaseVersionError;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Frontend for the Version Control System API client.
 */
@Slf4j
@Service
public class VersioncontrolsystemClientImpl implements IVersioncontrolsystemClient
{

    @Autowired
    IRestHandlerVersioncontrolsystemapi restInterface;

    /**
     * API consumer.
     */
    private RestHandlerVersioncontrolsystemapi restHandler;

    @Override
    @PostConstruct
    public void init()
    {

        this.restHandler = new RestHandlerVersioncontrolsystemapi(this.restInterface);
    }

    @Override
    public List<VCSTag> getTags(int repoId)
    {

        List<VCSTag> tagList = new ArrayList<>();

        this.restHandler.getTags(
                // Implement anonymous listener just for this method.
                new IRestListenerVersioncontrolsystemapi()
                {
                    // On success.
                    @Override
                    public void getTags(VCSTag[] outcome)
                    {
                        // Set result to external var.
                        tagList.addAll(Arrays.asList(outcome));
                    }

                    // On error
                    @Override
                    public void getTagsErrors(Errors outcome)
                    {
                        log.error(Constants.LOG_TEMPLATE + Constants.VCS_ERROR_TAGS_MSG,
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getTags");
                        throw new NovaException(ReleaseVersionError.getVCSApiFailedError(),
                                Constants.VCS_API_FAILED_MSG);
                    }
                }, repoId);

        log.debug("[{}] -> [{}]: Got tags for repo: {} from VCS API {}", Constants.VERSION_CONTROL_SYSTEM_CLIENT,
                "getTags", repoId, tagList);

        return tagList;
    }

    @Override
    public List<String> getProjectsPathsFromRepoTag(int repoId, String tag)
    {

        List<String> modulePaths = new ArrayList<>();
        this.restHandler.getModulePaths(new IRestListenerVersioncontrolsystemapi()
        {
            @Override
            public void getModulePaths(String[] outcome)
            {
                ArrayList<String> paths = new ArrayList<>(Arrays.asList(outcome));
                modulePaths.addAll(paths);
            }

            @Override
            public void getModulePathsErrors(Errors outcome)
            {
                log.error(Constants.LOG_TEMPLATE + Constants.VCS_ERROR_PATHS_MSG,
                        Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getProjectsPathsFromRepoTag", outcome.getStatus());

                throw new NovaException(ReleaseVersionError.getVCSApiFailedError(),
                        Constants.VCS_API_FAILED_MSG);
            }
        }, repoId, tag);

        log.debug("[{}] -> [{}]: Got project paths for repo: {} from VCS API {}",
                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getProjectsPathsFromRepoTag", repoId, modulePaths);

        return modulePaths;
    }

    @Override
    public List<String> getFilesFromTreeDirectory(String projectPath, int repoId, String tag)
    {

        // Build repo data.
        VCSProject vcsProject = new VCSProject();
        vcsProject.setModulePath(projectPath);
        vcsProject.setId(repoId);
        vcsProject.setTag(tag);
        List<String> modulePaths = new ArrayList<>();

        this.restHandler.getTreeFiles(

                // Implement anonymous listener just for this method.
                new IRestListenerVersioncontrolsystemapi()
                {

                    // On success.
                    @Override
                    public void getTreeFiles(String[] outcome)
                    {

                        ArrayList<String> paths = new ArrayList<>(Arrays.asList(outcome));
                        modulePaths.addAll(paths);
                    }

                    // On error.
                    @Override
                    public void getTreeFilesErrors(Errors outcome)
                    {

                        log.error(Constants.LOG_TEMPLATE + Constants.VCS_ERROR_TREE_FILES_MSG,
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getFilesFromTreeDirectory");

                        throw new NovaException(ReleaseVersionError.getVCSApiFailedError(),
                                Constants.VCS_API_FAILED_MSG);
                    }
                }, vcsProject);

        log.debug("[{}] -> [{}]: Got project paths for repo: {} from VCS API {}",
                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getFilesFromTreeDirectory", repoId, modulePaths);

        return modulePaths;
    }

    @Override
    public byte[] getPomFromProject(String projectPath, int repoId, String tag)
    {
        // Build repo data.
        VCSProject vcsProject = new VCSProject();
        vcsProject.setModulePath(projectPath);
        vcsProject.setId(repoId);
        vcsProject.setTag(tag);

        // Fake list to get access to upper class primitive type var from inner class.
        final SingleApiClientResponseWrapper<byte[]> response = new SingleApiClientResponseWrapper<>();

        // Do the request to the API.
        this.restHandler.readPom(

                // Implement anonymous listener just for this method.
                new IRestListenerVersioncontrolsystemapi()
                {

                    // On success.
                    @Override
                    public void readPom(byte[] outcome)
                    {
                        log.debug("[{}] -> [{}]: Retrieved pom file for project: {} from VCS API", Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getPomFromProject", projectPath);
                        response.set(outcome);
                    }

                    // On error.
                    @Override
                    public void readPomErrors(Errors outcome)
                    {

                        log.error(Constants.LOG_TEMPLATE + Constants.VCS_ERROR_POM_MSG,
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getPomFromProject");

                        throw new NovaException(ReleaseVersionError.getVCSApiFailedError(),
                                Constants.VCS_API_FAILED_MSG);
                    }
                }, vcsProject);

        return response.get();
    }

    @Override
    public byte[] getNovaYmlFromProject(String projectPath, int repoId, String tag)
    {

        // Build repo data.
        VCSProject vcsProject = new VCSProject();
        vcsProject.setModulePath(projectPath);
        vcsProject.setId(repoId);
        vcsProject.setTag(tag);

        // Fake list to get access to upper class primitive type var from inner class.
        final SingleApiClientResponseWrapper<byte[]> response = new SingleApiClientResponseWrapper<>();

        // Do the request to the API.
        this.restHandler.readNovaYml(

                // Implement anonymous listener just for this method.
                new IRestListenerVersioncontrolsystemapi()
                {

                    // On success.
                    @Override
                    public void readNovaYml(byte[] outcome)
                    {

                        log.debug("[{}] -> [{}]: Retrieved nova.yml file for project: {} from VCS API",
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getNovaYmlFromProject", projectPath);
                        response.set(outcome);
                    }

                    // On error.
                    @Override
                    public void readNovaYmlErrors(Errors outcome)
                    {
                        log.error(Constants.LOG_TEMPLATE + Constants.VCS_ERROR_NOVA_YML_MSG,
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getNovaYmlFromProject", outcome.getStatus());

                        throw new NovaException(ReleaseVersionError.getVCSApiFailedError(),
                                Constants.VCS_API_FAILED_MSG);
                    }
                }, vcsProject);

        return response.get();
    }

    @Override
    public byte[] getSwaggerFromProject(String pathFile, int repoId, String tagName)
    {
        // Build repo data.
        VCSProject vcsProject = new VCSProject();
        vcsProject.setModulePath(pathFile);
        vcsProject.setId(repoId);
        vcsProject.setTag(tagName);

        // Fake list to get access to upper class primitive type var from inner class.
        final SingleApiClientResponseWrapper<byte[]> response = new SingleApiClientResponseWrapper<>();

        log.info("[{}] -> [getSwaggerFromProject]: Calling version control system api with VCSProject: [{}]",
                Constants.VERSION_CONTROL_SYSTEM_CLIENT, vcsProject);
        // Do the request to the API.
        this.restHandler.readSwagger(

                // Implement anonymous listener just for this method.
                new IRestListenerVersioncontrolsystemapi()
                {
                    // On success.
                    @Override
                    public void readSwagger(byte[] outcome)
                    {
                        log.debug("[{}] -> [getSwaggerFromProject]: Retrieved swagger file for project: {} from VCS API",
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, pathFile);
                        response.set(outcome);
                    }

                    // On error.
                    @Override
                    public void readSwaggerErrors(Errors outcome)
                    {
                        log.error(Constants.LOG_TEMPLATE + Constants.VCS_ERROR_SWAGGER_MSG, Constants.VERSION_CONTROL_SYSTEM_CLIENT,
                                "getSwaggerFromProject");

                        throw new NovaException(
                                ReleaseVersionError.getVCSApiFailedError(),
                                Constants.VCS_API_FAILED_MSG);
                    }
                },
                vcsProject);

        return response.get();
    }

    @Override
    public byte[] getPackageJsonFromProject(String projectPath, int repoId, String tag)
    {

        // Build repo data.
        VCSProject vcsProject = new VCSProject();
        vcsProject.setModulePath(projectPath);
        vcsProject.setId(repoId);
        vcsProject.setTag(tag);

        // Fake list to get access to upper class primitive type var from inner class.
        final SingleApiClientResponseWrapper<byte[]> response = new SingleApiClientResponseWrapper<>();

        // Do the request to the API.
        this.restHandler.readPackageJson(

                // Implement anonymous listener just for this method.
                new IRestListenerVersioncontrolsystemapi()
                {

                    // On success.
                    @Override
                    public void readPackageJson(byte[] outcome)
                    {

                        log.debug("[{}] -> [{}]: Retrieved package json file for project: {} from VCS API",
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getPackageJsonFromProject", projectPath);
                        response.set(outcome);
                    }

                    // On error.
                    @Override
                    public void readPackageJsonErrors(Errors outcome)
                    {

                        log.error(Constants.LOG_TEMPLATE + Constants.VCS_ERROR_PACKAGEJSON_MSG,
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getPackageJsonFromProject");

                        throw new NovaException(ReleaseVersionError.getVCSApiFailedError(),
                                Constants.VCS_API_FAILED_MSG);
                    }
                }, vcsProject);

        return response.get();
    }

    @Override
    public byte[] getBootstrapFromProject(String projectPath, int repoId, String tag)
    {

        // Build repo data.
        VCSProject vcsProject = new VCSProject();
        vcsProject.setModulePath(projectPath);
        vcsProject.setId(repoId);
        vcsProject.setTag(tag);

        // Fake list to get access to upper class primitive type var from inner class.
        final SingleApiClientResponseWrapper<byte[]> response = new SingleApiClientResponseWrapper<>();

        // Do the request to the API.
        this.restHandler.readBootstrap(

                // Implement anonymous listener just for this method.
                new IRestListenerVersioncontrolsystemapi()
                {

                    // On success.
                    @Override
                    public void readBootstrap(byte[] outcome)
                    {

                        log.debug("[{}] -> [{}]: Retrieved bootstrap file for project: {} from VCS API",
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getBootstrapFromProject", projectPath);
                        response.set(outcome);
                    }

                    // On error.
                    @Override
                    public void readBootstrapErrors(Errors outcome)
                    {

                        log.error(Constants.LOG_TEMPLATE + Constants.VCS_ERROR_BOOTSTRAP_MSG,
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getBootstrapFromProject");

                        throw new NovaException(ReleaseVersionError.getVCSApiFailedError(),
                                Constants.VCS_API_FAILED_MSG);
                    }
                }, vcsProject);

        return response.get();
    }

    @Override
    public byte[] getDockerfileFromProject(String projectPath, int repoId, String tag)
    {

        // Build repo data.
        VCSProject vcsProject = new VCSProject();
        vcsProject.setModulePath(projectPath);
        vcsProject.setId(repoId);
        vcsProject.setTag(tag);

        // Fake list to get access to upper class primitive type var from inner class.
        final SingleApiClientResponseWrapper<byte[]> response = new SingleApiClientResponseWrapper<>();

        // Do the request to the API.
        this.restHandler.readDockerfile(

                // Implement anonymous listener just for this method.
                new IRestListenerVersioncontrolsystemapi()
                {

                    // On success.
                    @Override
                    public void readDockerfile(byte[] outcome)
                    {

                        log.debug("[{}] -> [{}]: Retrieved Dockerfile for project: {} from VCS API",
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getDockerfileFromProject", projectPath);
                        response.set(outcome);
                    }

                    // On error.
                    @Override
                    public void readDockerfileErrors(Errors outcome)
                    {

                        log.error(Constants.LOG_TEMPLATE + Constants.VCS_ERROR_DOCKERFILE_MSG,
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getDockerfileFromProject");

                        throw new NovaException(ReleaseVersionError.getVCSApiFailedError(),
                                Constants.VCS_API_FAILED_MSG);
                    }
                }, vcsProject);

        return response.get();
    }

    @Override
    public byte[] getApplicationFromProject(String projectPath, int repoId, String tag)
    {

        // Build repo data.
        VCSProject vcsProject = new VCSProject();
        vcsProject.setModulePath(projectPath);
        vcsProject.setId(repoId);
        vcsProject.setTag(tag);

        // Fake list to get access to upper class primitive type var from inner class.
        final SingleApiClientResponseWrapper<byte[]> response = new SingleApiClientResponseWrapper<>();

        // Do the request to the API.
        this.restHandler.readApplication(

                // Implement anonymous listener just for this method.
                new IRestListenerVersioncontrolsystemapi()
                {

                    // On success.
                    @Override
                    public void readApplication(byte[] outcome)
                    {

                        log.debug("[{}] -> [{}]: Retrieved application file for project: {} from VCS API",
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getApplicationFromProject", projectPath);
                        response.set(outcome);
                    }

                    // On error.
                    @Override
                    public void readApplicationErrors(Errors outcome)
                    {

                        log.error(Constants.LOG_TEMPLATE + Constants.VCS_ERROR_APPLICATION_MSG,
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getApplicationFromProject");

                        throw new NovaException(ReleaseVersionError.getVCSApiFailedError(),
                                Constants.VCS_API_FAILED_MSG);
                    }
                }, vcsProject);

        return response.get();
    }

    @Override
    public byte[] getFileFromProject(final String projectPath, final int repoId, final String tag)
    {

        log.debug("[{}] -> [{}]: getFileFromProject: projectPath= {} , repoId= {} , tag= {}",
                Constants.VERSION_CONTROL_SYSTEM_CLIENT, Constants.GET_FILE_FROM_PROJECT, projectPath, repoId, tag);

        // Build repo data.
        VCSProject vcsProject = new VCSProject();
        vcsProject.setModulePath(projectPath);
        vcsProject.setId(repoId);
        vcsProject.setTag(tag);

        // Init result to 0 by default.
        final SingleApiClientResponseWrapper<byte[]> response = new SingleApiClientResponseWrapper<>();

        // Do the request to the API.
        this.restHandler.readFile(
                // Implement anonymous listener just for this method.
                new IRestListenerVersioncontrolsystemapi()
                {
                    // On success.
                    @Override
                    public void readFile(byte[] outcome)
                    {

                        log.debug("[{}] -> [{}]: Retrieved file for project: {} from VCS API",
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, Constants.GET_FILE_FROM_PROJECT, projectPath);
                        response.set(outcome);
                    }

                    // On error.
                    @Override
                    public void readFileErrors(Errors outcome)
                    {

                        log.error(Constants.LOG_TEMPLATE + Constants.VCS_ERROR_TEMPLATE_MSG,
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, Constants.GET_FILE_FROM_PROJECT);
                        throw new NovaException(ReleaseVersionError.getUnexpectedErrorInVCSRequestError(), outcome,
                                "Error calling VCS service to get the file.");
                    }
                }, vcsProject);

        return response.get();
    }

    @Override
    public List<String> getDependencies(int repoId, String tag)
    {
        List<String> dependencies = new ArrayList<>();
        byte[] byteArray = this.getDependenciesByteArray(repoId, tag);
        if (byteArray != null && byteArray.length > 0)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(byteArray), StandardCharsets.UTF_8));
            dependencies = this.getOrderList(reader);
        }
        return dependencies;
    }

    /////////////////////////////////////////////////// PRIVATE METHODS ////////////////////////////////////////////////

    /**
     * Get byte array from version control system
     *
     * @param repoId repository id
     * @param tag    tag
     * @return byte array
     */
    private byte[] getDependenciesByteArray(int repoId, String tag)
    {

        // Build repo data.
        VCSProject vcsProject = new VCSProject();
        vcsProject.setModulePath("dependencies.txt");
        vcsProject.setId(repoId);
        vcsProject.setTag(tag);

        // Fake list to get access to upper class primitive type var from inner class.
        final SingleApiClientResponseWrapper<byte[]> response = new SingleApiClientResponseWrapper<>();

        // Do the request to the API.
        this.restHandler.readFile(

                // Implement anonymous listener just for this method.
                new IRestListenerVersioncontrolsystemapi()
                {

                    // On success.
                    @Override
                    public void readFile(byte[] outcome)
                    {

                        log.debug("[{}] -> [{}]: Retrieved file for repo: {} from VCS API {}",
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getDependencies", repoId, outcome);
                        response.set(outcome);
                    }

                    // On error.
                    @Override
                    public void readFileErrors(Errors outcome)
                    {

                        log.error("[{}] -> [{}]: Received status: {} trying to request getFile() to VCS API",
                                Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getDependencies");

                        throw new NovaException(ReleaseVersionError.getVCSApiFailedError(),
                                Constants.VCS_API_FAILED_MSG);
                    }
                }, vcsProject);

        return response.get();
    }

    /**
     * Get order list
     *
     * @param reader buffered reader
     * @return list of folders
     */
    private List<String> getOrderList(BufferedReader reader)
    {
        List<String> lines = new ArrayList<>();
        try
        {
            this.addLines(reader, lines);
        }
        catch (IOException e)
        {
            //Error reading file
            log.error("[{}] -> [{}]: Error reading file", Constants.VERSION_CONTROL_SYSTEM_CLIENT, "getOrderList", e);
            throw new NovaException(ReleaseVersionError.getFileError(), e, "Error reading dependencies file");
        }
        return lines;
    }

    /**
     * Add lines
     *
     * @param reader reader
     * @param lines  list of lines
     * @throws IOException on file error
     */
    private void addLines(BufferedReader reader, List<String> lines) throws IOException
    {
        try (reader)
        {
            for (String line = reader.readLine(); line != null; line = reader.readLine())
            {
                lines.add(line);
            }
        }
    }
}
