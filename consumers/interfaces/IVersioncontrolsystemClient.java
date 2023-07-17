package com.bbva.enoa.platformservices.coreservice.consumers.interfaces;

import com.bbva.enoa.apirestgen.versioncontrolsystemapi.model.VCSTag;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Frontend for the Version Control System API client.
 */
public interface IVersioncontrolsystemClient
{

    /**
     * Init the handler and listener.
     */
    @PostConstruct
    void init();

    /**
     * Gets all the tags from a VCS repository.
     *
     * @param repoId
     *            ID of a VCS repository.
     * @return Array of tags.
     */
    List<VCSTag> getTags(int repoId);

    /**
     * Gets all the paths for a subsystem services that have a pom.xml.
     *
     * @param repoId Repo ID.
     * @param tag    Tag.
     * @return List of paths for each project.
     */
    List<String> getProjectsPathsFromRepoTag(int repoId, String tag);

    /**
     * Get resource file names contented in a tree for a service, given its path in the git repository, its
     * ID and tag.
     *
     * @param projectPath
     *            Project of service in the repo.
     * @param repoId
     *            Repo ID.
     * @param tag
     *            Tag.
     * @return List with file names.
     */
    List<String> getFilesFromTreeDirectory(String projectPath, int repoId, String tag);

    /**
     * Get the pom.xml file contents for a service, given its path in the git repository, its ID and tag.
     *
     * @param projectPath
     *            Project of service in the repo.
     * @param repoId
     *            Repo ID.
     * @param tag
     *            Tag.
     * @return byte[] with contents of pom.xml.
     */
    byte[] getPomFromProject(String projectPath, int repoId, String tag);

    /**
     * Get the nova.yml file contents for a service, given its path in the git repository, its ID and tag.
     *
     * @param projectPath
     *            Project of service in the repo.
     * @param repoId
     *            Repo ID.
     * @param tag
     *            Tag.
     * @return byte[] with contents of nova.yml.
     */
    byte[] getNovaYmlFromProject(String projectPath, int repoId, String tag);

    /**
     * Get the nova.yml file contents for a service, given its path in the
     * git repository, its ID and tag.
     *
     * @param filePath Project of service in the repo and the path of the file.
     * @param repoId   Repo ID.
     * @param tag      Tag.
     * @return byte[] with contents of swagger file.
     */
    byte[] getSwaggerFromProject(String filePath, int repoId, String tag);

    /**
     * Get the package.json file contents for a service, given its path in the git repository, its ID and
     * tag.
     *
     * @param projectPath
     *            Project of service in the repo.
     * @param repoId
     *            Repo ID.
     * @param tag
     *            Tag.
     * @return byte[] with contents of bootstrap.xml.
     */
    byte[] getPackageJsonFromProject(String projectPath, int repoId, String tag);

    /**
     * Get the bootstrap.xml file contents for a service, given its path in the git repository, its ID and
     * tag.
     *
     * @param projectPath
     *            Project of service in the repo.
     * @param repoId
     *            Repo ID.
     * @param tag
     *            Tag.
     * @return byte[] with contents of bootstrap.xml.
     */
    byte[] getBootstrapFromProject(String projectPath, int repoId, String tag);

    /**
     * Get the Dockerfile contents for a service, given its path in the git repository, its ID and tag.
     *
     * @param projectPath
     *            Project of service in the repo.
     * @param repoId
     *            Repo ID.
     * @param tag
     *            Tag.
     * @return byte[] with contents of Dockerfile.
     */
    byte[] getDockerfileFromProject(String projectPath, int repoId, String tag);

    /**
     * Get the application.yml file contents for a service, given its path in the git repository, its ID and
     * tag.
     *
     * @param projectPath
     *            Project of service in the repo.
     * @param repoId
     *            Repo ID.
     * @param tag
     *            Tag.
     * @return byte[] with contents of application.yml.
     */
    byte[] getApplicationFromProject(String projectPath, int repoId, String tag);

    /**
     * Get the file contents for a service, given its path in the git repository, its ID and tag.
     *
     * @param projectPath
     *            Project of service in the repo.
     * @param repoId
     *            Repo ID.
     * @param tag
     *            Tag.
     * @return byte[]
     */
    byte[] getFileFromProject(String projectPath, int repoId, String tag);

    /**
     * Get dependencies file from project
     *
     * @param repoId
     *            Repo ID.
     * @param tag
     *            Tag.
     * @return string list with contents of dependencies.txt
     */
    List<String> getDependencies(int repoId, String tag);

}
