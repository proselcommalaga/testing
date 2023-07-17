package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util;

import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYmlRequirement;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.ValidatorInputs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;


public class ProjectFileReaderTest
{
    protected static final String CLASSPATH_NOVAYML_NOVA_YML = "classpath:novayml/nova_java_spring_boot_security_api.yml";
    protected static final String CLASSPATH_NOVAYML_LIBRARY_JAVA_NOVA_YML = "classpath:novayml/nova_library_java.yml";
    protected static final String CLASSPATH_NOVAYML_SERVICE_AND_LIBS = "classpath:novayml/nova_java_spring_boot_security_with_libraries_api.yml";

    protected static final String CLASSPATH_POMS_POM_TEST_SECURITY_XML = "classpath:poms/pom_test_security.xml";
    protected static final String CLASSPATH_APPLICATION_APPLICATION_TEST_SECURITY_XML =
            "classpath:application/application_java_spring_boot_security.yml";
    protected static final String CLASSPATH_BOOTSTRAP_BOOTSTRAP_TEST_SECURITY_YML = "classpath:bootstrap/bootstrap_java_spring_boot_security.yml";
    protected static final String CLASSPATH_BOOTSTRAP_DEFAULT_INVALID = "classpath:bootstrap/bootstrap_default_invalid.yml";
    protected static final String CLASSPATH_NOVAYML_FRONTCAT = "classpath:novayml/nova_java_spring_mvc_frontcat.yml";

    @Test
    public void scanPom() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_POMS_POM_TEST_SECURITY_XML);
        FileInputStream fis = new FileInputStream(file);
        byte[] pom = new byte[(int) file.length()];
        // Reads up to certain bytes of data from this input stream into an array of bytes.
        int read = fis.read(pom);

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);
        if (read > 0)
        {
            ProjectFileReader.scanPom(file.getPath(), pom, validatorInputs);
        }

        Assertions.assertTrue(validatorInputs.isLatestVersion());
        Assertions.assertEquals("Api mu chola en JAVA", validatorInputs.getPom().getDescription());
        Assertions.assertEquals("API en java spring", validatorInputs.getPom().getName());
        Assertions.assertEquals("com.bbva.envm", validatorInputs.getPom().getGroupId());
        Assertions.assertEquals("javaapi", validatorInputs.getPom().getArtifactId());
        Assertions.assertEquals("1.0.0", validatorInputs.getPom().getVersion());
        Assertions.assertEquals("jar", validatorInputs.getPom().getPackaging());
        String finalName = validatorInputs.getPom().getGroupId() + "-" + validatorInputs.getPom().getArtifactId();
        Assertions.assertEquals(validatorInputs.getPom().getFinalName(), finalName);
        Assertions.assertEquals("./dist", validatorInputs.getPom().getPlugin());
        Assertions.assertTrue(validatorInputs.getPom().isPomValid());
        Assertions.assertFalse(validatorInputs.getPom().getDependencies().isEmpty());
    }

    @Test
    public void scanApplication() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_APPLICATION_APPLICATION_TEST_SECURITY_XML);
        FileInputStream fis = new FileInputStream(file);
        byte[] application = new byte[(int) file.length()];
        // Reads up to certain bytes of data from this input stream into an array of bytes.
        int read = fis.read(application);

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);
        if (read > 0)
        {
            ProjectFileReader.scanApplication("some path", application, validatorInputs);
        }

        Assertions.assertFalse(Boolean.parseBoolean(validatorInputs.getApplication().getEndpointLogFile()));
        Assertions.assertFalse(Boolean.parseBoolean(validatorInputs.getApplication().getEndpointShutdown()));
        Assertions.assertFalse(Boolean.parseBoolean(validatorInputs.getApplication().getEndpointRestart()));
        Assertions.assertTrue(validatorInputs.getApplication().getApplicationFiles().isEmpty());
        Assertions.assertEquals("${NOVA_PORT:8080}", validatorInputs.getApplication().getServerPort());
    }

    @Test
    public void scanBootstrap() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_BOOTSTRAP_BOOTSTRAP_TEST_SECURITY_YML);
        FileInputStream fis = new FileInputStream(file);
        byte[] bootstrap = new byte[(int) file.length()];
        // Reads up to certain bytes of data from this input stream into an array of bytes.
        int read = fis.read(bootstrap);

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);
        if (read > 0)
        {
            ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);
        }

        Assertions.assertEquals("${SPRING_PROFILES_ACTIVE:LOCAL}", validatorInputs.getBootstrap().getActiveProfile());
    }

    @Test
    public void scanBootstrapWithErrors() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_BOOTSTRAP_DEFAULT_INVALID);
        FileInputStream fis = new FileInputStream(file);
        byte[] bootstrap = new byte[(int) file.length()];
        // Reads up to certain bytes of data from this input stream into an array of bytes.
        int read = fis.read(bootstrap);

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);
        if (read > 0)
        {
            ProjectFileReader.scanBootstrap("someProjectPath", bootstrap, validatorInputs);
        }

        Assertions.assertFalse(validatorInputs.getBootstrap().isValid());
    }


    @Test
    public void scanNovaYml() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_NOVAYML_NOVA_YML);
        FileInputStream fis = new FileInputStream(file);
        byte[] novaFile = new byte[(int) file.length()];
        // Reads up to certain bytes of data from this input stream into an array of bytes.
        int read = fis.read(novaFile);

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);
        if (read > 0)
        {
            ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        }

        Assertions.assertTrue(validatorInputs.getNovaYml().isValid());
        Assertions.assertEquals("ENVM", validatorInputs.getNovaYml().getUuaa());
        Assertions.assertEquals("javaapi", validatorInputs.getNovaYml().getName());
        Assertions.assertEquals("1.0.0", validatorInputs.getNovaYml().getVersion());
        Assertions.assertEquals("API_JAVA_SPRING_BOOT", validatorInputs.getNovaYml().getServiceType());
        Assertions.assertEquals("JAVA_SPRING_BOOT", validatorInputs.getNovaYml().getLanguage());
        Assertions.assertEquals("1.8.121", validatorInputs.getNovaYml().getLanguageVersion());
        Assertions.assertEquals("18.04", validatorInputs.getNovaYml().getNovaVersion());
        Assertions.assertNotNull(validatorInputs.getNovaYml().getBuild());
        Assertions.assertEquals(3, validatorInputs.getNovaYml().getMachines().size());
        Assertions.assertEquals("src/main/resources/api1.yml", validatorInputs.getNovaYml().getApiServed().get(0).getApi());
        Assertions.assertEquals("service.log", validatorInputs.getNovaYml().getProperties().get(0).getName());
        Assertions.assertEquals("STRING", validatorInputs.getNovaYml().getProperties().get(0).getType());
        Assertions.assertEquals("SERVICE", validatorInputs.getNovaYml().getProperties().get(0).getManagement());
        Assertions.assertFalse(validatorInputs.getNovaYml().getProperties().get(0).getEncrypt());
        Assertions.assertEquals("Fix", validatorInputs.getNovaYml().getPorts().get(0).getName());
        Assertions.assertEquals(8080, validatorInputs.getNovaYml().getPorts().get(0).getInsidePort().intValue());
        Assertions.assertEquals(37495, validatorInputs.getNovaYml().getPorts().get(0).getOutsidePort().intValue());
        Assertions.assertEquals("TCP", validatorInputs.getNovaYml().getPorts().get(0).getType());
        Assertions.assertEquals("service-name-001", validatorInputs.getNovaYml().getDependencies().get(0));
    }


    @Test
    public void scanLibNovaYml() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_NOVAYML_LIBRARY_JAVA_NOVA_YML);
        FileInputStream fis = new FileInputStream(file);
        byte[] novaFile = new byte[(int) file.length()];
        // Reads up to certain bytes of data from this input stream into an array of bytes.
        int read = fis.read(novaFile);

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);
        if (read > 0)
        {
            ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        }


        Assertions.assertTrue(validatorInputs.getNovaYml().isValid());
        Assertions.assertEquals("JLIB", validatorInputs.getNovaYml().getUuaa());
        Assertions.assertEquals("java-lib", validatorInputs.getNovaYml().getName());
        Assertions.assertEquals("1.0.0", validatorInputs.getNovaYml().getVersion());
        Assertions.assertEquals("LIBRARY_JAVA", validatorInputs.getNovaYml().getServiceType());
        Assertions.assertEquals("JAVA", validatorInputs.getNovaYml().getLanguage());
        Assertions.assertEquals("1.8.121", validatorInputs.getNovaYml().getLanguageVersion());
        Assertions.assertEquals("18.04", validatorInputs.getNovaYml().getNovaVersion());
        Assertions.assertNotNull(validatorInputs.getNovaYml().getBuild());
        Assertions.assertNotNull(validatorInputs.getNovaYml().getDependencies());
        Assertions.assertNotNull(validatorInputs.getNovaYml().getProperties());
        Assertions.assertNotNull(validatorInputs.getNovaYml().getPorts());
        Assertions.assertNotNull(validatorInputs.getNovaYml().getApiServed());
        Assertions.assertNotNull(validatorInputs.getNovaYml().getRequirements());

        Assertions.assertEquals("service.log", validatorInputs.getNovaYml().getProperties().get(0).getName());
        Assertions.assertEquals("STRING", validatorInputs.getNovaYml().getProperties().get(0).getType());
        Assertions.assertEquals("SERVICE", validatorInputs.getNovaYml().getProperties().get(0).getManagement());
        Assertions.assertFalse(validatorInputs.getNovaYml().getProperties().get(0).getEncrypt());

        Assertions.assertFalse(validatorInputs.getNovaYml().getRequirements().isEmpty());

        for (NovaYmlRequirement requirement : validatorInputs.getNovaYml().getRequirements())
        {
            Assertions.assertNotNull(Constants.REQUIREMENT_NAME.valueOf(requirement.getName().toUpperCase()));
            Assertions.assertNotNull(requirement.getValue());
        }
    }


    @Test
    public void scanNovaYmlWitLibraries() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_NOVAYML_SERVICE_AND_LIBS);
        FileInputStream fis = new FileInputStream(file);
        byte[] novaFile = new byte[(int) file.length()];
        // Reads up to certain bytes of data from this input stream into an array of bytes.
        int read = fis.read(novaFile);

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);

        if (read > 0)
        {
            ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        }

        Assertions.assertTrue(validatorInputs.getNovaYml().isValid());
        Assertions.assertEquals("ZYWX", validatorInputs.getNovaYml().getUuaa());
        Assertions.assertEquals("usinglibs", validatorInputs.getNovaYml().getName());
        Assertions.assertEquals("2.1.3", validatorInputs.getNovaYml().getVersion());
        Assertions.assertEquals("API_JAVA_SPRING_BOOT", validatorInputs.getNovaYml().getServiceType());
        Assertions.assertEquals("JAVA_SPRING_BOOT", validatorInputs.getNovaYml().getLanguage());
        Assertions.assertEquals("1.8.121", validatorInputs.getNovaYml().getLanguageVersion());
        Assertions.assertEquals("19.04", validatorInputs.getNovaYml().getNovaVersion());
        Assertions.assertNotNull(validatorInputs.getNovaYml().getBuild());
        Assertions.assertEquals(3, validatorInputs.getNovaYml().getMachines().size());
        Assertions.assertEquals("src/main/resources/api1.yml", validatorInputs.getNovaYml().getApiServed().get(0).getApi());
        Assertions.assertEquals("service.log", validatorInputs.getNovaYml().getProperties().get(0).getName());
        Assertions.assertEquals("STRING", validatorInputs.getNovaYml().getProperties().get(0).getType());
        Assertions.assertEquals("SERVICE", validatorInputs.getNovaYml().getProperties().get(0).getManagement());
        Assertions.assertFalse(validatorInputs.getNovaYml().getProperties().get(0).getEncrypt());

        Assertions.assertFalse(validatorInputs.getNovaYml().getLibraries().isEmpty());
        Assertions.assertEquals(3, validatorInputs.getNovaYml().getLibraries().size());
        Assertions.assertEquals("uuaa-release-service0-version0", validatorInputs.getNovaYml().getLibraries().get(0));
        Assertions.assertEquals("uuaa-release-service1-version0", validatorInputs.getNovaYml().getLibraries().get(1));
        Assertions.assertEquals("uuaa-release-service2-version0", validatorInputs.getNovaYml().getLibraries().get(2));


    }

    @Test
    public void scanFrontcatNovaYml() throws Exception
    {
        File file = ResourceUtils.getFile(CLASSPATH_NOVAYML_FRONTCAT);
        FileInputStream fis = new FileInputStream(file);
        byte[] novaFile = new byte[(int) file.length()];
        // Reads up to certain bytes of data from this input stream into an array of bytes.
        int read = fis.read(novaFile);

        ValidatorInputs validatorInputs = new ValidatorInputs();
        validatorInputs.setLatestVersion(true);
        if (read > 0)
        {
            ProjectFileReader.scanNovaYml(file.getPath(), novaFile, validatorInputs);
        }


        Assertions.assertTrue(validatorInputs.getNovaYml().isValid());
        Assertions.assertEquals("KXWB", validatorInputs.getNovaYml().getUuaa());
        Assertions.assertEquals("oportunidadesprivilegiadas", validatorInputs.getNovaYml().getName());
        Assertions.assertEquals("1.0.11", validatorInputs.getNovaYml().getVersion());
        Assertions.assertEquals("FRONTCAT_JAVA_SPRING_MVC", validatorInputs.getNovaYml().getServiceType());
        Assertions.assertEquals("JAVA_SPRING_MVC", validatorInputs.getNovaYml().getLanguage());
        Assertions.assertEquals("1.8.121", validatorInputs.getNovaYml().getLanguageVersion());
        Assertions.assertEquals("19.04", validatorInputs.getNovaYml().getNovaVersion());
        Assertions.assertNotNull(validatorInputs.getNovaYml().getBuild());
        Assertions.assertNotNull(validatorInputs.getNovaYml().getDependencies());
        Assertions.assertNotNull(validatorInputs.getNovaYml().getProperties());
        Assertions.assertNotNull(validatorInputs.getNovaYml().getPorts());
        Assertions.assertNotNull(validatorInputs.getNovaYml().getApiServed());
        Assertions.assertNotNull(validatorInputs.getNovaYml().getRequirements());

        Assertions.assertEquals("service.log", validatorInputs.getNovaYml().getProperties().get(0).getName());
        Assertions.assertEquals("STRING", validatorInputs.getNovaYml().getProperties().get(0).getType());
        Assertions.assertEquals("SERVICE", validatorInputs.getNovaYml().getProperties().get(0).getManagement());
        Assertions.assertFalse(validatorInputs.getNovaYml().getProperties().get(0).getEncrypt());

        // Frontcat properties
        Assertions.assertEquals("KXWB", validatorInputs.getNovaYml().getJunction());
        Assertions.assertEquals("kxwb_mult_web_servicios_01", validatorInputs.getNovaYml().getContextPath());
        Assertions.assertTrue(validatorInputs.getNovaYml().isNetworkHostEnabled());

    }

}
