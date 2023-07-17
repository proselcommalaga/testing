package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * For mapping the file nova.yml content under validation
 */
@EqualsAndHashCode
@Getter
@Setter
public class NovaYml
{
    private boolean valid;
    private String uuaa = "";
    private String description = "";
    //Old artifact Id
    private String name = "";
    private String version = "";
    private String serviceType = "";
    private String language = "";
    private String languageVersion = "";
    private String jdkVersion = "";
    private String novaVersion = "";
    private List<String> build;
    private List<String> machines;
    private List<NovaYmlApi> apiServed;
    private List<NovaYmlApi> apiConsumed;
    private List<NovaYmlApi> apiExternal;

    private NovaYmlAsyncApi asyncapisBackToBack;
    private NovaYmlAsyncApi asyncapisBackToFront;

    private List<NovaYamlProperty> properties;
    private List<NovaYmlRequirement> requirements;
    private List<NovaYmlPort> ports;
    private List<String> dependencies;
    /**
     * Libraries into nova.yml used by service
     */
    private List<String> libraries;

    // Only for frontcat nova.yml
    /**
     * Frontcat specific info into nova.yml
     */
    private String junction;
    private String contextPath;
    private boolean networkHostEnabled;

    // Only for Cells nova.yml
    private String projectName = "";
    private String applicationName = "";

    // Only by Batch scheduler services
    private List<NovaYamlContextParam> contextParams;
    private List<NovaYamlInputParams> inputParams;
    private List<NovaYamlOutputParams> outputParams;

    //Only for Behavior test
    private String testFramework = "";
    private String testFrameworkVersion = "";
    private String releaseVersion = "";
    private String tags = "";

    /**
     * Empty, not null constructor
     */
    public NovaYml()
    {
        this.apiServed = new ArrayList<>();
        this.apiConsumed = new ArrayList<>();
        this.apiExternal = new ArrayList<>();
        this.build = new ArrayList<>();
        this.machines = new ArrayList<>();
        this.properties = new ArrayList<>();
        this.requirements = new ArrayList<>();
        this.ports = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.libraries = new ArrayList<>();
        // Only for the batch scheduler
        this.inputParams = new ArrayList<>();
        this.outputParams = new ArrayList<>();
        this.contextParams = new ArrayList<>();
    }
}
