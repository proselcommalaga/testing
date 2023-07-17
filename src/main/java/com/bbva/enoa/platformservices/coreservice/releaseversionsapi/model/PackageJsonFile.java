package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

import java.util.ArrayList;
import java.util.List;

/**
 * For mapping the package.json file content under validation
 *
 * @author xe63185
 */
public class PackageJsonFile
{

    private String name;
    private String version;
    /**
     * List of package.json dependencies
     */
    private List<String> dependencies;

    /**
     * List of package.json dependencies
     */
    private List<String> devDependencies;

    /**
     * get dependencies
     *
     * @return List Dependencies from package.json
     */
    public List<String> getDependencies()
    {
        if (this.dependencies == null)
        {
            this.dependencies = new ArrayList<>();
        }
        return List.copyOf(dependencies);
    }

    /**
     * Set dependencies
     *
     * @param dependencies List of dependencies
     */
    public void setDependencies(List<String> dependencies)
    {

        this.dependencies = List.copyOf(dependencies);
    }

    /**
     * Add new dependency
     *
     * @param dependency add new dependency
     */
    public void addDependency(String dependency)
    {

        if (this.dependencies == null)
        {
            this.dependencies = new ArrayList<>();
        }

        this.dependencies.add(dependency);

    }

    /**
     * get devDependencies
     *
     * @return List Dependencies from package.json
     */
    public List<String> getDevDependencies()
    {
        if (this.devDependencies == null)
        {
            this.devDependencies = new ArrayList<>();
        }
        return List.copyOf(devDependencies);
    }

    /**
     * Set devDependencies
     *
     * @param devDependencies List of dependencies
     */
    public void setDevDependencies(List<String> devDependencies)
    {

        this.devDependencies = List.copyOf(devDependencies);
    }

    /**
     * Add new devDependency
     *
     * @param devDependencies add new dependency
     */
    public void addDevDependency(String devDependencies)
    {

        if (this.devDependencies == null)
        {
            this.devDependencies = new ArrayList<>();
        }

        this.devDependencies.add(devDependencies);

    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }
}
