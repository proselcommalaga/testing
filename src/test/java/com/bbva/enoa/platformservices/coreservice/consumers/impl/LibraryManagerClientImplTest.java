package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.librarymanagerapi.client.feign.nova.rest.IRestHandlerLibrarymanagerapi;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsByServiceDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryEnvironmentsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryNovaYmlRequirementDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryPublicationDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryRequirementsDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMLibraryValidationErrorDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsageDTO;
import com.bbva.enoa.apirestgen.librarymanagerapi.model.LMUsedLibrariesDTO;
import com.bbva.enoa.datamodel.model.common.entities.LobFile;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersionService;
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
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@ExtendWith(MockitoExtension.class)
class LibraryManagerClientImplTest
{
    @Mock
    private IRestHandlerLibrarymanagerapi restHandler;
    @Mock
    private Logger log;
    @InjectMocks
    private LibraryManagerClientImpl client;

    @BeforeEach
    public void init() throws IllegalAccessException, NoSuchFieldException
    {
        MockitoAnnotations.initMocks(LogsClient.class);
        client.init();

        Field field = client.getClass().getDeclaredField("log");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(client, log);
    }

    @Test
    public void when_store_requirements_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.storeRequirements(Mockito.any(LMLibraryRequirementsDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.storeRequirements(getReleaseVersionServiceWithNovaYml()));
    }

    @Test
    public void when_store_requirements_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.storeRequirements(Mockito.any(LMLibraryRequirementsDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.storeRequirements(getReleaseVersionServiceWithNovaYml());

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void when_remove_requirements_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.removeRequirements(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.removeRequirements(getReleaseVersionServiceWithNovaYml()));
    }

    @Test
    public void when_remove_requirements_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.removeRequirements(Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.removeRequirements(getReleaseVersionServiceWithNovaYml());

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void when_get_requirements_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getRequirements(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getRequirements(1));
    }

    @Test
    public void when_get_requirements_returns_ok_response_then_return_result()
    {
        LMLibraryRequirementsDTO dto = DummyConsumerDataGenerator.getDummyLMLibraryRequirementsDto();
        Mockito.when(restHandler.getRequirements(Mockito.anyInt())).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        LMLibraryRequirementsDTO result = client.getRequirements(1);

        Assertions.assertEquals(dto, result);
    }

    @Test
    public void when_get_requirements_by_full_name_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getLibraryRequirements(Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getRequirementsByFullName("A"));
    }

    @Test
    public void when_get_requirements_by_full_name_returns_ok_response_then_return_result()
    {
        LMLibraryRequirementsDTO dto = DummyConsumerDataGenerator.getDummyLMLibraryRequirementsDto();
        Mockito.when(restHandler.getLibraryRequirements(Mockito.anyString())).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        LMLibraryRequirementsDTO result = client.getRequirementsByFullName("A");

        Assertions.assertEquals(dto, result);
    }

    @Test
    public void when_get_library_environments_returns_ko_response_then_return_empty_result()
    {
        Mockito.when(restHandler.getPublishedEnvironments(Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        LMLibraryEnvironmentsDTO result = client.getLibraryEnvironments("A");

        Assertions.assertFalse(result.getInte());
        Assertions.assertFalse(result.getPre());
        Assertions.assertFalse(result.getPro());
        Assertions.assertEquals(0, result.getReleaseVersionServiceId());
        Assertions.assertEquals("", result.getFullName());
    }

    @Test
    public void when_get_library_environments_returns_ok_response_then_return_result()
    {
        LMLibraryEnvironmentsDTO dto = DummyConsumerDataGenerator.getDummyLMLibraryEnvironmentsDto();
        Mockito.when(restHandler.getPublishedEnvironments(Mockito.anyString())).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        LMLibraryEnvironmentsDTO result = client.getLibraryEnvironments("A");

        Assertions.assertEquals(dto, result);
    }

    @Test
    public void when_get_libraries_that_service_use_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getLibrariesByRVService(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getLibrariesThatServiceUse(1, "A"));
    }

    @Test
    public void when_get_libraries_that_service_use_returns_ok_response_then_return_result()
    {
        LMLibraryEnvironmentsDTO[] dtos = new LMLibraryEnvironmentsDTO[]{DummyConsumerDataGenerator.getDummyLMLibraryEnvironmentsDto()};
        Mockito.when(restHandler.getLibrariesByRVService(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        LMLibraryEnvironmentsDTO[] result = client.getLibrariesThatServiceUse(1, "A");

        Assertions.assertEquals(dtos.length, result.length);
        Assertions.assertEquals(dtos[0], result[0]);
    }

    @Test
    public void when_create_used_libraries_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.createUsedLibraries(Mockito.any(), Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.createUsedLibraries(1, new String[]{"A"}));
    }

    @Test
    public void when_create_used_libraries_returns_ok_response_then_return_result()
    {
        LMUsedLibrariesDTO[] dtos = DummyConsumerDataGenerator.getDummyLMUsedLibrariesDtos();
        Mockito.when(restHandler.createUsedLibraries(Mockito.any(), Mockito.anyInt())).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        LMUsedLibrariesDTO[] result = client.createUsedLibraries(1, new String[]{"A"});

        Assertions.assertEquals(dtos.length, result.length);
        Assertions.assertEquals(dtos[0], result[0]);
    }

    @Test
    public void when_update_used_libraries_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.updateUsedLibraries(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.updateUsedLibraries(1, "A"));
    }

    @Test
    public void when_update_used_libraries_returns_ok_response_then_return_result()
    {
        Mockito.when(restHandler.updateUsedLibraries(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.updateUsedLibraries(1, "A");

        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_publish_library_on_environment_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.publishLibraryOnEnvironment(Mockito.any(LMLibraryPublicationDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.publishLibraryOnEnvironment(1, "A"));
    }

    @Test
    public void when_publish_library_on_environment_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.publishLibraryOnEnvironment(Mockito.any(LMLibraryPublicationDTO.class), Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.publishLibraryOnEnvironment(1, "A");

        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void when_get_services_using_library_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getServicesUsingLibrary(Mockito.anyInt(), Mockito.any())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getServicesUsingLibrary(1, new String[]{"A"}));
    }

    @Test
    public void when_get_services_using_library_returns_ok_response_then_return_result()
    {
        int[] expected = new int[]{1, 2, 3};
        Mockito.when(restHandler.getServicesUsingLibrary(Mockito.anyInt(), Mockito.any())).thenReturn(new ResponseEntity<>(expected, HttpStatus.OK));

        int[] result = client.getServicesUsingLibrary(1, new String[]{"A"});

        Assertions.assertEquals(expected, result);
    }

    @Test
    public void when_remove_library_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.removeLibrary(Mockito.anyInt())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.removeLibrary(1));
    }

    @Test
    public void when_remove_library_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.removeLibrary(Mockito.anyInt())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.removeLibrary(1);

        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
    }

    @Test
    public void when_remove_libraries_usages_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.removeUsedLibraries(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.removeLibrariesUsages(1, "A"));
    }

    @Test
    public void when_remove_libraries_usages_returns_ok_response_then_log_debug()
    {
        Mockito.when(restHandler.removeUsedLibraries(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>("", HttpStatus.OK));

        client.removeLibrariesUsages(1, "A");

        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
    }

    @Test
    public void when_check_used_libraries_availability_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.checkUsedLibrariesAvailability(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.checkUsedLibrariesAvailability(1, "A"));
    }

    @Test
    public void when_check_used_libraries_availability_returns_ok_response_with_true_result_then_return_true()
    {
        Mockito.when(restHandler.checkUsedLibrariesAvailability(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>(Boolean.TRUE, HttpStatus.OK));

        boolean result = client.checkUsedLibrariesAvailability(1, "A");

        Assertions.assertTrue(result);
    }

    @Test
    public void when_check_used_libraries_availability_returns_ok_response_with_false_result_then_return_false()
    {
        Mockito.when(restHandler.checkUsedLibrariesAvailability(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ResponseEntity<>(Boolean.FALSE, HttpStatus.OK));

        boolean result = client.checkUsedLibrariesAvailability(1, "A");

        Assertions.assertFalse(result);
    }

    @Test
    public void when_get_library_usages_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getLibraryUsages(Mockito.anyInt())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getLibraryUsages(1));
    }

    @Test
    public void when_get_library_usages_returns_ok_response_then_return_result()
    {
        LMUsageDTO[] dtos = DummyConsumerDataGenerator.getDummyLMUsageDtos();
        Mockito.when(restHandler.getLibraryUsages(Mockito.anyInt())).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        LMUsageDTO[] result = client.getLibraryUsages(1);

        Assertions.assertEquals(dtos.length, result.length);
        Assertions.assertEquals(dtos[0], result[0]);
    }

    @Test
    public void when_get_used_libraries_returns_ko_response_then_log_error()
    {
        Mockito.when(restHandler.getUsedLibraries(Mockito.any(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        client.getUsedLibraries(new int[]{1}, "A");

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyString(), Mockito.any());
    }

    @Test
    public void when_get_used_libraries_returns_ok_response_then_return_result()
    {
        LMLibraryEnvironmentsDTO[] dtos = new LMLibraryEnvironmentsDTO[]{DummyConsumerDataGenerator.getDummyLMLibraryEnvironmentsDto()};
        Mockito.when(restHandler.getUsedLibraries(Mockito.any(), Mockito.anyString())).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        LMLibraryEnvironmentsDTO[] result = client.getUsedLibraries(new int[]{1}, "A");

        Assertions.assertEquals(dtos.length, result.length);
        Assertions.assertEquals(dtos[0], result[0]);
    }

    @Test
    public void when_get_used_libraries_by_services_returns_ko_response_then_log_error()
    {
        Mockito.when(restHandler.getUsedLibrariesByServices(Mockito.any(), Mockito.anyString())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        client.getUsedLibrariesByServices(new int[]{1}, "A");

        Mockito.verify(log, Mockito.times(1)).error(Mockito.anyString(), Mockito.anyString(), Mockito.any());
    }

    @Test
    public void when_get_used_libraries_by_services_returns_ok_response_then_return_result()
    {
        LMLibraryEnvironmentsByServiceDTO[] dtos = DummyConsumerDataGenerator.getDummyLMLibraryEnvironmentsByServiceDtos();
        Mockito.when(restHandler.getUsedLibrariesByServices(Mockito.any(), Mockito.anyString())).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        LMLibraryEnvironmentsByServiceDTO[] result = client.getUsedLibrariesByServices(new int[]{1}, "A");

        Assertions.assertEquals(dtos.length, result.length);
        Assertions.assertEquals(dtos[0], result[0]);
    }

    @Test
    public void when_get_all_requirements_of_used_libraries_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.getAllRequirementsOfUsedLibraries(Mockito.any())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.getAllRequirementsOfUsedLibraries(new int[]{1}));
    }

    @Test
    public void when_get_all_requirements_of_used_libraries_returns_ok_response_then_return_result()
    {
        LMLibraryRequirementsDTO[] dtos = new LMLibraryRequirementsDTO[]{DummyConsumerDataGenerator.getDummyLMLibraryRequirementsDto()};
        Mockito.when(restHandler.getAllRequirementsOfUsedLibraries(Mockito.any())).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        LMLibraryRequirementsDTO[] result = client.getAllRequirementsOfUsedLibraries(new int[]{1});

        Assertions.assertEquals(dtos.length, result.length);
        Assertions.assertEquals(dtos[0], result[0]);
    }

    @Test
    public void when_validate_nova_yml_requirements_returns_ko_response_then_throw_exception()
    {
        Mockito.when(restHandler.validateNovaYmlRequirements(Mockito.any())).thenReturn(new ResponseEntity(DummyConsumerDataGenerator.getDummyErrors(), HttpStatus.INTERNAL_SERVER_ERROR));

        Assertions.assertThrows(NovaException.class, () -> client.validateNovaYmlRequirements(new LMLibraryNovaYmlRequirementDTO[0]));
    }

    @Test
    public void when_validate_nova_yml_requirements_returns_ok_response_with_empty_result_then_return_result()
    {
        Mockito.when(restHandler.validateNovaYmlRequirements(Mockito.any())).thenReturn(new ResponseEntity<>(new LMLibraryValidationErrorDTO[0], HttpStatus.OK));

        LMLibraryValidationErrorDTO[] result = client.validateNovaYmlRequirements(new LMLibraryNovaYmlRequirementDTO[0]);

        Assertions.assertEquals(0, result.length);
        Mockito.verify(log, Mockito.times(1)).debug(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    public void when_validate_nova_yml_requirements_returns_ok_response_with_populated_result_then_log_debug_and_return_result()
    {
        LMLibraryValidationErrorDTO[] dtos = DummyConsumerDataGenerator.getDummyLMLibraryValidationErrorDtos();
        Mockito.when(restHandler.validateNovaYmlRequirements(Mockito.any())).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        LMLibraryValidationErrorDTO[] result = client.validateNovaYmlRequirements(new LMLibraryNovaYmlRequirementDTO[0]);

        Assertions.assertEquals(1, result.length);
        Mockito.verify(log, Mockito.times(2)).debug(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    private ReleaseVersionService getReleaseVersionServiceWithNovaYml()
    {
        ReleaseVersionService releaseVersionService = DummyConsumerDataGenerator.getDummyReleaseVersionService();
        LobFile novaYml = new LobFile();
        novaYml.setName("nova.yml");
        novaYml.setId(1);
        novaYml.setUrl("URL");
        InputStream fileStream = LibraryManagerClientImpl.class.getResourceAsStream("/novayml/nova_java_spring_boot_security_with_libraries_api.yml");
        byte[] bytes = new byte[1024];
        try
        {
            fileStream.read(bytes);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        novaYml.setContents(new String(bytes));
        releaseVersionService.setNovaYml(novaYml);
        return releaseVersionService;
    }

}