package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.common.entities.LobFile;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ReleaseVersionServiceRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.SyncApiRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.SyncApiVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.SwaggerConverter;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IVersioncontrolsystemClient;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYmlApi;
import io.swagger.models.Swagger;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public class SyncApiDefinitionValidatorImplTest
{

    @InjectMocks
    private SyncApiDefinitionValidatorImpl syncApiDefinitionValidator;

    @Mock
    private SyncApiRepository syncApiRepository;

    @Mock
    private SyncApiVersionRepository syncApiVersionRepository;

    @Mock
    private IVersioncontrolsystemClient versionControlSystemClient;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ReleaseVersionServiceRepository releaseVersionServiceRepository;

    @Mock
    private SwaggerConverter swaggerConverter;

    @BeforeEach
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    @Tag("happy_path")
    @DisplayName("Is this bean a validator?")
    public void testIsValidator()
    {
        Assertions.assertTrue(this.syncApiDefinitionValidator.isValidator());
    }

    @Test
    @Tag("happy_path")
    @DisplayName("Has this nova.yml valid APIs?")
    public void testValidAPIs() throws IOException
    {
        // given
        NovaYml novaYml = Mockito.mock(NovaYml.class);
        NewReleaseVersionServiceDto newReleaseVersionServiceDto = Mockito.mock(NewReleaseVersionServiceDto.class);
        int repoId = 1;
        String tag = "test";
        String releaseName = "testing_release";

        String folder = "test_folder";
        String api = "test_api";

        Resource stateFile = new ClassPathResource("api/swagger_validVersion.yml");
        String contentYaml = Files.readString(Path.of(stateFile.getFile().getPath()), StandardCharsets.UTF_8);


        NovaYmlApi novaYmlApi1 = Mockito.mock(NovaYmlApi.class);
        NovaYmlApi novaYmlApi2 = Mockito.mock(NovaYmlApi.class);
        List<NovaYmlApi> novaYmlApiList1 = List.of(novaYmlApi1, novaYmlApi2);

        NovaYmlApi novaYmlApi3 = Mockito.mock(NovaYmlApi.class);
        NovaYmlApi novaYmlApi4 = Mockito.mock(NovaYmlApi.class);
        List<NovaYmlApi> novaYmlApiList2 = List.of(novaYmlApi3, novaYmlApi4);

        NovaYmlApi novaYmlApi5 = Mockito.mock(NovaYmlApi.class);
        NovaYmlApi novaYmlApi6 = Mockito.mock(NovaYmlApi.class);
        List<NovaYmlApi> novaYmlApiList3 = List.of(novaYmlApi5, novaYmlApi6);

        Product product = Mockito.mock(Product.class);
        SyncApiVersion apiVersion = Mockito.mock(SyncApiVersion.class);
        Swagger swagger = Mockito.mock(Swagger.class);

        // when
        Mockito.when(novaYml.getApiServed()).thenReturn(novaYmlApiList1);
        Mockito.when(novaYml.getApiConsumed()).thenReturn(novaYmlApiList2);
        Mockito.when(novaYml.getApiExternal()).thenReturn(novaYmlApiList3);
        Stream.of(novaYmlApiList1, novaYmlApiList2, novaYmlApiList3).parallel().flatMap(List::stream).forEach(novaYmlApi -> {
            Mockito.when(novaYmlApi.getApi()).thenReturn(api);
            Mockito.when(novaYmlApi.getBackwardCompatibleVersions()).thenReturn(List.of(api));
            Mockito.when(novaYmlApi.getConsumedApi()).thenReturn(List.of(api));
        });

        Mockito.when(novaYmlApi1.getExternalApi()).thenReturn(List.of(api));

        Mockito.when(newReleaseVersionServiceDto.getFolder()).thenReturn(folder);
        Mockito.when(newReleaseVersionServiceDto.getUuaa()).thenReturn("test");
        Mockito.when(this.versionControlSystemClient.getSwaggerFromProject(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString())).thenReturn(contentYaml.getBytes());
        Mockito.when(this.productRepository.findOneByUuaaIgnoreCase(Mockito.anyString())).thenReturn(Optional.of(product));
        Mockito.when(this.syncApiVersionRepository.findByProductIdAndApiNameAndVersion(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString())).thenReturn(apiVersion);
        Mockito.when(this.syncApiVersionRepository.findByApiNameAndVersionAndUuaaAndExternalFalse(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(apiVersion);

        Mockito.when(this.productRepository.findOneByUuaaIgnoreCase(Mockito.anyString())).thenReturn(Optional.of(product));
        Mockito.when(product.getId()).thenReturn(1);

        LobFile lobFile = Mockito.mock(LobFile.class);
        Mockito.when(lobFile.getContents()).thenReturn(contentYaml);
        Mockito.when(apiVersion.getDefinitionFile()).thenReturn(lobFile);

        SyncApi syncApi = Mockito.mock(SyncApi.class);
        Mockito.when(apiVersion.getApi()).thenReturn(syncApi);
        Mockito.when(syncApi.getProduct()).thenReturn(product);

        Mockito.when(swaggerConverter.parseSwaggerFromString(Mockito.anyString())).thenReturn(swagger);
        Mockito.when(syncApi.getApiModality()).thenReturn(ApiModality.SYNC);

        Mockito.when(syncApiVersionRepository.findByApiNameAndVersionAndUuaaAndProductId(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt())).thenReturn(apiVersion);




        // then
        Assertions.assertDoesNotThrow(() -> this.syncApiDefinitionValidator.validateAndAssociateApi(novaYml, newReleaseVersionServiceDto, repoId, tag, releaseName));

    }



}
