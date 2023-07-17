package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.versioncontrolsystemapi.client.feign.nova.rest.IRestHandlerVersioncontrolsystemapi;
import com.bbva.enoa.apirestgen.versioncontrolsystemapi.model.VCSProject;
import com.bbva.enoa.apirestgen.versioncontrolsystemapi.model.VCSTag;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class VersioncontrolsystemClientImplTest
{
    private static final String DUMMY = "DUMMY";
    @Mock
    private IRestHandlerVersioncontrolsystemapi restHandler;
    @InjectMocks
    private VersioncontrolsystemClientImpl client;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        this.client.init();
    }

    @Test
    public void when_get_tags_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getTags(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getTags(1));
    }

    @Test
    public void when_get_tags_returns_ok_response_then_return_result()
    {
        VCSTag[] tags = DummyConsumerDataGenerator.getDummyVCSTags();
        Mockito.when(restHandler.getTags(Mockito.anyInt())).thenReturn(new ResponseEntity<>(tags, HttpStatus.OK));

        List<VCSTag> result = client.getTags(1);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(tags[0], result.get(0));
    }

    @Test
    public void when_get_projects_paths_from_repo_tag_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getModulePaths(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getProjectsPathsFromRepoTag(1, "A"));
    }

    @Test
    public void when_get_projects_paths_from_repo_tag_returns_ok_response_then_return_result()
    {
        String[] paths = new String[]{"A", "B", "C"};
        Mockito.when(restHandler.getModulePaths(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>(paths, HttpStatus.OK));

        List<String> result = client.getProjectsPathsFromRepoTag(1, "A");

        Assertions.assertEquals(Arrays.asList(paths), result);
    }

    @Test
    public void when_get_files_from_tree_directory_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getTreeFiles(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getFilesFromTreeDirectory("B", 1, "A"));
    }

    @Test
    public void when_get_files_from_tree_directory_returns_ok_response_then_return_result()
    {
        String[] files = new String[]{"A", "B", "C"};
        Mockito.when(restHandler.getTreeFiles(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity<>(files, HttpStatus.OK));

        List<String> result = client.getFilesFromTreeDirectory("B", 1, "A");

        Assertions.assertEquals(Arrays.asList(files), result);
    }

    @Test
    public void when_get_pom_from_project_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.readPom(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getPomFromProject("B", 1, "A"));
    }

    @Test
    public void when_get_pom_from_project_directory_returns_ok_response_then_return_result()
    {
        byte[] bytes = "DUMMY".getBytes(StandardCharsets.UTF_8);
        Mockito.when(restHandler.readPom(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity<>(bytes, HttpStatus.OK));

        byte[] result = client.getPomFromProject("B", 1, "A");

        Assertions.assertEquals(bytes, result);
    }

    @Test
    public void when_get_nova_yml_from_project_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.readNovaYml(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getNovaYmlFromProject("B", 1, "A"));
    }

    @Test
    public void when_get_nova_yml_from_project_directory_returns_ok_response_then_return_result()
    {
        byte[] bytes = "DUMMY".getBytes(StandardCharsets.UTF_8);
        Mockito.when(restHandler.readNovaYml(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity<>(bytes, HttpStatus.OK));

        byte[] result = client.getNovaYmlFromProject("B", 1, "A");

        Assertions.assertEquals(bytes, result);
    }

    @Test
    public void when_get_swagger_from_project_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.readSwagger(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getSwaggerFromProject("B", 1, "A"));
    }

    @Test
    public void when_get_swagger_from_project_directory_returns_ok_response_then_return_result()
    {
        byte[] bytes = "DUMMY".getBytes(StandardCharsets.UTF_8);
        Mockito.when(restHandler.readSwagger(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity<>(bytes, HttpStatus.OK));

        byte[] result = client.getSwaggerFromProject("B", 1, "A");

        Assertions.assertEquals(bytes, result);
    }

    @Test
    public void when_get_package_json_from_project_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.readPackageJson(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getPackageJsonFromProject("B", 1, "A"));
    }

    @Test
    public void when_get_package_json_from_project_directory_returns_ok_response_then_return_result()
    {
        byte[] bytes = DUMMY.getBytes(StandardCharsets.UTF_8);
        Mockito.when(restHandler.readPackageJson(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity<>(bytes, HttpStatus.OK));

        byte[] result = client.getPackageJsonFromProject("B", 1, "A");

        Assertions.assertEquals(bytes, result);
    }

    @Test
    public void when_get_bootstrap_from_project_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.readBootstrap(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getBootstrapFromProject("B", 1, "A"));
    }

    @Test
    public void when_get_bootstrap_from_project_directory_returns_ok_response_then_return_result()
    {
        byte[] bytes = DUMMY.getBytes(StandardCharsets.UTF_8);
        Mockito.when(restHandler.readBootstrap(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity<>(bytes, HttpStatus.OK));

        byte[] result = client.getBootstrapFromProject("B", 1, "A");

        Assertions.assertEquals(bytes, result);
    }

    @Test
    public void when_get_dockerfile_from_project_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.readDockerfile(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getDockerfileFromProject("B", 1, "A"));
    }

    @Test
    public void when_get_dockerfile_from_project_directory_returns_ok_response_then_return_result()
    {
        byte[] bytes = DUMMY.getBytes(StandardCharsets.UTF_8);
        Mockito.when(restHandler.readDockerfile(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity<>(bytes, HttpStatus.OK));

        byte[] result = client.getDockerfileFromProject("B", 1, "A");

        Assertions.assertEquals(bytes, result);
    }

    @Test
    public void when_get_application_from_project_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.readApplication(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getApplicationFromProject("B", 1, "A"));
    }

    @Test
    public void when_get_application_from_project_directory_returns_ok_response_then_return_result()
    {
        byte[] bytes = DUMMY.getBytes(StandardCharsets.UTF_8);
        Mockito.when(restHandler.readApplication(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity<>(bytes, HttpStatus.OK));

        byte[] result = client.getApplicationFromProject("B", 1, "A");

        Assertions.assertEquals(bytes, result);
    }

    @Test
    public void when_get_file_from_project_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.readFile(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getFileFromProject("B", 1, "A"));
    }

    @Test
    public void when_get_file_from_project_directory_returns_ok_response_then_return_result()
    {
        byte[] bytes = DUMMY.getBytes(StandardCharsets.UTF_8);
        Mockito.when(restHandler.readFile(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity<>(bytes, HttpStatus.OK));

        byte[] result = client.getFileFromProject("B", 1, "A");

        Assertions.assertEquals(bytes, result);
    }

    @Test
    public void when_get_dependencies_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.readFile(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getDependencies(1, "A"));
    }

    @Test
    public void when_get_dependencies_returns_null_response_then_return_empty_result()
    {
        Mockito.when(restHandler.readFile(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        List<String> result = client.getDependencies(1, "A");

        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void when_get_dependencies_returns_empty_response_then_return_empty_result()
    {
        Mockito.when(restHandler.readFile(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity<>(new byte[0], HttpStatus.OK));

        List<String> result = client.getDependencies(1, "A");

        Assertions.assertEquals(0, result.size());
    }

    //TODO how to force an IOEXception while reading bytes?

    @Test
    public void when_get_dependencies_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.readFile(Mockito.any(VCSProject.class))).thenReturn(new ResponseEntity<>("ONE\nTWO\nTHREE".getBytes(StandardCharsets.UTF_8), HttpStatus.OK));

        List<String> result = client.getDependencies(1, "A");
        Assertions.assertEquals(List.of("ONE", "TWO", "THREE"), result);
    }

}
