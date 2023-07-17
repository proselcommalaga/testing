package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import lombok.ToString;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;

import java.util.*;


/**
 * For mapping the file pom content under validation
 */
@ToString
public class PomXML
{
    /**
     * Description
     */
    private String description;

    /**
     * Service name
     */
    private String name;

    /**
     * Build parameters
     */
    private Build build;

    /**
     * Properties
     */
    private Properties properties;

    /**
     * Modules
     */
    private List<String> modules;

    /**
     * Group id
     */
    private String groupId;

    /**
     * Artifact Id
     */
    private String artifactId;

    /**
     * Version
     */
    private String version;

    /**
     * Packaging
     */
    private String packaging;

    /**
     * Parent Version
     */
    private String parentVersion;

    /**
     * UUAA Name
     */
    private String uuaaName;

    /**
     * Nova Version
     */
    private String novaVersion;

    /**
     * Final Name
     */
    private String finalName;

    /**
     * Plugin Nova Starter Version
     */
    private String plugin;

    /**
     * True if the pom is valid
     */
    private boolean pomValidation;

    /**
     * Set of artifacts Ids of the same system
     */
    private static Map<String, Set<String>> artifactIds = new HashMap<>();

    /**
     * List of dependencies inside pom
     */
    private final List<Dependency> dependencies = new ArrayList<>();

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getPackaging()
    {
        return packaging;
    }

    public void setPackaging(String packaging)
    {
        this.packaging = packaging;
    }

    public String getParentVersion()
    {
        return parentVersion;
    }

    public void setParentVersion(String parentVersion)
    {
        this.parentVersion = parentVersion;
    }

    public String getUuaaName()
    {
        return uuaaName;
    }

    public void setUuaaName(String uuaaName)
    {
        this.uuaaName = uuaaName;
    }

    public String getNovaVersion()
    {
        return novaVersion;
    }

    public void setNovaVersion(String novaVersion)
    {
        this.novaVersion = novaVersion;
    }

    public String getFinalName()
    {
        return finalName;
    }

    public void setFinalName(String finalName)
    {
        this.finalName = finalName;
    }

    public String getPlugin()
    {
        return plugin;
    }

    public void setPlugin(String plugin)
    {
        this.plugin = plugin;
    }

    public static Map<String, Set<String>> getArtifactIds()
    {
        return artifactIds;
    }

    /**
     * Resets the list of artifact ids
     */
    public static void clearArtifactIdList()
    {
        artifactIds.clear();
    }

    /**
     * True if the POM has pass all the validations, false otherwise
     *
     * @return boolean pom validation result
     */
    public boolean isPomValid()
    {
        return pomValidation;
    }

    /**
     * Add an artifact ID to the list
     *
     * @param groupId    Group Id
     * @param artifactId Artifact Id
     */
    public static void addArtifactId(String groupId, String artifactId)
    {
        Set<String> artifacts = artifactIds.get(groupId);
        if (artifacts == null)
        {
            artifacts = new HashSet<>();
        }
        if (!artifacts.add(artifactId))
        {
            artifacts.add(Constants.ARTIFACT_ID_NAME_ERROR);
        }
        artifactIds.put(groupId, artifacts);
    }

    public void setPomValidation(boolean pomValidation)
    {
        this.pomValidation = pomValidation;
    }


    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Build getBuild()
    {
        return build;
    }

    public void setBuild(Build build)
    {
        this.build = build;
    }

    public Properties getProperties()
    {
        return this.properties;
    }

    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    public List<String> getModules()
    {
        return List.copyOf(modules);
    }

    /**
     * Set modules
     *
     * @param modules modules
     */
    public void setModules(List<String> modules)
    {
        this.modules = Optional.ofNullable(modules).map(List::copyOf).orElse(new ArrayList<>());
    }

    /**
     * get the list of dependencies declared into pom as groupId:artifactId:version
     * @return non null list
     */
    public List<Dependency> getDependencies()
    {
        return List.copyOf(dependencies);
    }

    /**
     * Add a new dependency to list
     * @param dependency dependency to add
     */
    public void addDependency(final Dependency dependency)
    {
        this.dependencies.add(dependency);
    }

    /**
     * Add a all dependencies inside this list
     * @param dependencies list of dependency to add
     */
    public void addAllDependencies(final List<Dependency> dependencies)
    {
        this.dependencies.addAll(dependencies);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof PomXML))
        {
            return false;
        }
        PomXML pomXML = (PomXML) obj;

        return EqualsBuilder.reflectionEquals(this, pomXML);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }

}
