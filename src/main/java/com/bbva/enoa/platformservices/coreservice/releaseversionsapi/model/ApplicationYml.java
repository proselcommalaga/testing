package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * For mapping the file application content under validation
 */
public class ApplicationYml
{
    private List<String> applicationFiles;

    private String serverPort;

    private String endpointRestart;

    private String endpointShutdown;

    private String endpointLogFile;

    private String yamlException;

    /**
     * Empty, not null constructor
     */
    public ApplicationYml()
    {
        this.applicationFiles = new ArrayList<>();
        this.serverPort = "";
        this.endpointRestart = "";
        this.endpointShutdown = "";
        this.endpointLogFile = "";
        this.yamlException = "";
    }

    // Getter and Setter methods

    /**
     * Get the list of file names of the application.yml
     *
     * @return List of file names
     */
    public List<String> getApplicationFiles()
    {
        List<String> returnList = new LinkedList<>();
        returnList.addAll(this.applicationFiles);
        return returnList;
    }

    /**
     * Set a list of file names of the application.yml
     *
     * @param applicationFiles List of file names
     */
    public void setApplicationFiles(List<String> applicationFiles)
    {
        this.applicationFiles = new LinkedList<>();
        this.applicationFiles.addAll(applicationFiles);
    }

    /**
     * Get the server port value
     *
     * @return server port value
     */
    public String getServerPort()
    {
        return serverPort;
    }

    /**
     * Set the server port value
     *
     * @param serverPort server port value
     */
    public void setServerPort(String serverPort)
    {
        this.serverPort = serverPort;
    }

    /**
     * Get the endpoint restart value
     *
     * @return endpoint restart value
     */
    public String getEndpointRestart()
    {
        return endpointRestart;
    }

    /**
     * Set the endpoint restart value
     *
     * @param endpointRestart endpoint restart value
     */
    public void setEndpointRestart(String endpointRestart)
    {
        this.endpointRestart = endpointRestart;
    }

    /**
     * Get the endpoint shutdown value
     *
     * @return endpoint shutdown value
     */
    public String getEndpointShutdown()
    {
        return endpointShutdown;
    }

    /**
     * Set the endpoint shutdown value
     *
     * @param endpointShutdown endpoint shutdown value
     */
    public void setEndpointShutdown(String endpointShutdown)
    {
        this.endpointShutdown = endpointShutdown;
    }

    /**
     * Get the endpoint logfile value
     *
     * @return endpoint logfile value
     */
    public String getEndpointLogFile()
    {
        return endpointLogFile;
    }

    /**
     * Set the endpoint logfile value
     *
     * @param endpointLogFile endpoint logfile value
     */
    public void setEndpointLogFile(String endpointLogFile)
    {
        this.endpointLogFile = endpointLogFile;
    }

    /**
     * Whether application.yml cannot be read or not
     *
     * @return yamlException
     */
    public String getYamlException()
    {
        return yamlException;
    }

    /**
     * Set yamlException
     *
     * @param yamlException new yamlException
     */
    public void setYamlException(String yamlException)
    {
        this.yamlException = yamlException;
    }
}

