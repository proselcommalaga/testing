package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that models pip-tools' requirements.in file content.
 *
 * @author XE59105
 */
public class PythonRequirements
{

    /**
     * Contains the frozen list of dependencies.
     */
    private List<String> dependencies;

    /**
     * Get the frozen dependencies list.
     *
     * @return List List of frozen dependencies on requirements file
     */
    public List<String> getDependencies()
    {
        return List.copyOf(dependencies);
    }

    /**
     * Set the frozen dependencies list.
     *
     * @param dependencies List of frozen dependencies on requirements file
     */
    public void setDependencies(List<String> dependencies)
    {
        this.dependencies = List.copyOf(dependencies);
    }

    /**
     * Adds a dependency to the frozen dependencies list.
     *
     * @param dependency dependency to add to the list.
     */
    public void addDependency(String dependency)
    {
        if (this.dependencies == null)
        {
            this.dependencies = new ArrayList<>();
        }
        this.dependencies.add(dependency);
    }
}
