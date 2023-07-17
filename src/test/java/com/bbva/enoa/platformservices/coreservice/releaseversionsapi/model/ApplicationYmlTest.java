package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ApplicationYmlTest
{

    @Test
    public void getApplicationFiles()
    {
        ApplicationYml applicationYml = new ApplicationYml();
        List<String> appFiles = new ArrayList<>();
        applicationYml.setApplicationFiles(appFiles);
        applicationYml.setEndpointLogFile("log");
        applicationYml.setEndpointRestart("restart");
        applicationYml.setEndpointShutdown("shutdown");
        applicationYml.setServerPort("port");
        applicationYml.setYamlException("Error");

        Assertions.assertEquals(appFiles, applicationYml.getApplicationFiles());
        Assertions.assertEquals("log", applicationYml.getEndpointLogFile());
        Assertions.assertEquals("restart", applicationYml.getEndpointRestart());
        Assertions.assertEquals("shutdown", applicationYml.getEndpointShutdown());
        Assertions.assertEquals("port", applicationYml.getServerPort());
        Assertions.assertEquals("Error", applicationYml.getYamlException());
    }
}