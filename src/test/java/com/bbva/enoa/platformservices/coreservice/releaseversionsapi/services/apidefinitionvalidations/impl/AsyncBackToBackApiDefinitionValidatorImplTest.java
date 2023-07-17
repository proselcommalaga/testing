package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApi;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiChannel;
import com.bbva.enoa.datamodel.model.api.entities.AsyncBackToBackApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.AsyncBackToBackChannelType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ymlvalidations.AsyncBackToBackApiVersionRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.Utils;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.VersioncontrolsystemClientImpl;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYmlAsyncApi;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AsyncBackToBackApiDefinitionValidatorImplTest {

    private static final String TAG_NAME = "tagName";
    private static final String FOLDER = "folder";
    private static final String RELEASE_NAME = "release_name";
    private static final String RAW_ASYNC_API1 = "rawAsyncApi1";
    private static final String RAW_ASYNC_API2 = "rawAsyncApi2";
    private static final String RAW_ASYNC_API3 = "rawAsyncApi3";
    private static final byte[] RAW_ASYNC_API1_BYTES = RAW_ASYNC_API1.getBytes(StandardCharsets.UTF_8);
    private static final byte[] RAW_ASYNC_API2_BYTES = RAW_ASYNC_API2.getBytes(StandardCharsets.UTF_8);
    private static final byte[] RAW_ASYNC_API3_BYTES = RAW_ASYNC_API3.getBytes(StandardCharsets.UTF_8);
    private static final String API1 = "api1";
    private static final String API2 = "api2";
    private static final String API3 = "api3";
    private static final int REPO_ID = 100;
    private static final Set<String> apis = Set.of(API1, API2, API3);

    private static final String UUAA = "uuaa";
    private static final String PRODUCT_NAME_1 = "productName1";
    private static final int PRODUCT_ID_1 = 1;
    private static final String PRODUCT_NAME_2 = "productName2";
    private static final int PRODUCT_ID_2 = 2;
    private static final String PRODUCT_NAME_3 = "productName3";
    private static final int PRODUCT_ID_3 = 3;
    private static final String API_VERSION = "apiVersion";
    private static final String API_DESCRITPION = "apiDescription";
    private static final String API_NAME = "apiName";

    @Mock
    private VersioncontrolsystemClientImpl versioncontrolsystemClient;

    @Mock
    private Utils utils;

    @Mock
    private AsyncBackToBackApiVersionRepository asyncBackToBackApiVersionRepository;

    @InjectMocks
    private AsyncBackToBackApiDefinitionValidatorImpl asyncBackToBackApiDefinitionValidator;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void isValidatorTest()
    {
        Assertions.assertTrue(asyncBackToBackApiDefinitionValidator.isValidator());
    }

    @Test
    public void validateAndAssociateAsyncApiWithApiVersionNullKO() {
        //mocks
        NewReleaseVersionServiceDto newReleaseVersionServiceDto =
                new NewReleaseVersionServiceDto();
        NovaYml novaYml = mock(NovaYml.class);
        NovaYmlAsyncApi novaYmlAsyncApi = mock(NovaYmlAsyncApi.class);

        //When
        newReleaseVersionServiceDto.setFolder(FOLDER);
        when(novaYml.getAsyncapisBackToBack()).thenReturn(novaYmlAsyncApi);
        when(novaYmlAsyncApi.getAsyncApis()).thenReturn(apis);

        // Then
        List<ValidationErrorDto> errorDtoList =
                asyncBackToBackApiDefinitionValidator.validateAndAssociateApi(novaYml, newReleaseVersionServiceDto, REPO_ID, TAG_NAME, RELEASE_NAME);

        // Validations
        Assertions.assertEquals(3, errorDtoList.size());

        Assertions.assertEquals(Constants.API_PATH_NOT_FOUND, errorDtoList.get(0).getCode());
        Assertions.assertEquals(Constants.API_PATH_NOT_FOUND, errorDtoList.get(1).getCode());
        Assertions.assertEquals(Constants.API_PATH_NOT_FOUND, errorDtoList.get(2).getCode());
    }

    @Test
    public void validateAndAssociateAsyncApiImplementedAsConsumerOk() {
        //mocks
        NewReleaseVersionServiceDto newReleaseVersionServiceDto =
                new NewReleaseVersionServiceDto();
        NovaYml novaYml = mock(NovaYml.class);
        NovaYmlAsyncApi novaYmlAsyncApi = mock(NovaYmlAsyncApi.class);

        var md5API1 = "md5API1";
        var md5API2 = "md5API2";
        var md5API3 = "md5API3";

        AsyncBackToBackApi api1 = mock(AsyncBackToBackApi.class);
        AsyncBackToBackApi api2 = mock(AsyncBackToBackApi.class);
        AsyncBackToBackApi api3 = mock(AsyncBackToBackApi.class);

        AsyncBackToBackApiVersion apiVersion1 = mock(AsyncBackToBackApiVersion.class);
        AsyncBackToBackApiVersion apiVersion2 = mock(AsyncBackToBackApiVersion.class);
        AsyncBackToBackApiVersion apiVersion3 = mock(AsyncBackToBackApiVersion.class);

        AsyncBackToBackApiChannel asyncBackToBackApiChannel1 =
                new AsyncBackToBackApiChannel("operationId", "channelName", AsyncBackToBackChannelType.PUBLISH);
        AsyncBackToBackApiChannel asyncBackToBackApiChannel2 =
                new AsyncBackToBackApiChannel("operationId", "channelName", AsyncBackToBackChannelType.PUBLISH);
        AsyncBackToBackApiChannel asyncBackToBackApiChannel3 =
                new AsyncBackToBackApiChannel("operationId", "channelName", AsyncBackToBackChannelType.SUBSCRIBE);

        //When
        newReleaseVersionServiceDto.setFolder(FOLDER);
        when(novaYml.getAsyncapisBackToBack()).thenReturn(novaYmlAsyncApi);
        when(novaYmlAsyncApi.getAsyncApis()).thenReturn(apis);
        when(versioncontrolsystemClient.getFileFromProject(eq(FOLDER + "/" + API1), eq(REPO_ID), eq(TAG_NAME)))
                .thenReturn(RAW_ASYNC_API1_BYTES);
        when(versioncontrolsystemClient.getFileFromProject(eq(FOLDER + "/" + API2), eq(REPO_ID), eq(TAG_NAME)))
                .thenReturn(RAW_ASYNC_API2_BYTES);
        when(versioncontrolsystemClient.getFileFromProject(eq(FOLDER + "/" + API3), eq(REPO_ID), eq(TAG_NAME)))
                .thenReturn(RAW_ASYNC_API3_BYTES);

        when(utils.calculeMd5Hash(RAW_ASYNC_API1_BYTES)).thenReturn(md5API1);
        when(utils.calculeMd5Hash(RAW_ASYNC_API2_BYTES)).thenReturn(md5API2);
        when(utils.calculeMd5Hash(RAW_ASYNC_API3_BYTES)).thenReturn(md5API3);

        when(asyncBackToBackApiVersionRepository.
                findByDefinitionFileHash(md5API1)).thenReturn(apiVersion1);
        when(asyncBackToBackApiVersionRepository.
                findByDefinitionFileHash(md5API2)).thenReturn(apiVersion2);
        when(asyncBackToBackApiVersionRepository.
                findByDefinitionFileHash(md5API3)).thenReturn(apiVersion3);

        when(apiVersion1.getAsyncBackToBackApiChannel()).thenReturn(asyncBackToBackApiChannel1);
        when(apiVersion2.getAsyncBackToBackApiChannel()).thenReturn(asyncBackToBackApiChannel2);
        when(apiVersion3.getAsyncBackToBackApiChannel()).thenReturn(asyncBackToBackApiChannel3);

        when(apiVersion1.getApi()).thenReturn(api1);
        when(apiVersion2.getApi()).thenReturn(api2);
        when(apiVersion3.getApi()).thenReturn(api3);

        when(api1.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOBACK);
        when(api2.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOBACK);
        when(api3.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOBACK);

        populateApiVersion(apiVersion1, PRODUCT_NAME_1, PRODUCT_ID_1);
        populateApiVersion(apiVersion2, PRODUCT_NAME_2, PRODUCT_ID_2);
        populateApiVersion(apiVersion3, PRODUCT_NAME_3, PRODUCT_ID_3);

        // Then
        List<ValidationErrorDto> errorDtoList =
                asyncBackToBackApiDefinitionValidator.validateAndAssociateApi(novaYml, newReleaseVersionServiceDto, REPO_ID, TAG_NAME, RELEASE_NAME);

        // Validations
        Assertions.assertTrue(errorDtoList.isEmpty());
        Assertions.assertEquals(2, newReleaseVersionServiceDto.getApisServed().length);
        Assertions.assertEquals(1, newReleaseVersionServiceDto.getApisConsumed().length);

        RVApiDTO rvApiDTO_0 = newReleaseVersionServiceDto.getApisServed()[0];
        RVApiDTO rvApiDTO_1 = newReleaseVersionServiceDto.getApisServed()[1];
        RVApiDTO rvApiDTO_2 = newReleaseVersionServiceDto.getApisConsumed()[0];

        Assertions.assertTrue(API_NAME == rvApiDTO_0.getApiName());
        Assertions.assertTrue(API_VERSION == rvApiDTO_0.getVersion());
        Assertions.assertTrue(API_DESCRITPION == rvApiDTO_0.getDescription());
        Assertions.assertTrue(PRODUCT_NAME_1.equals(rvApiDTO_0.getProduct()) || PRODUCT_NAME_2.equals(rvApiDTO_0.getProduct()) );
        Assertions.assertTrue(PRODUCT_ID_1 == rvApiDTO_0.getProductId() || PRODUCT_ID_2 == rvApiDTO_0.getProductId());


        Assertions.assertTrue(API_NAME == rvApiDTO_1.getApiName());
        Assertions.assertTrue(API_VERSION == rvApiDTO_1.getVersion());
        Assertions.assertTrue(API_DESCRITPION == rvApiDTO_1.getDescription());
        Assertions.assertTrue(PRODUCT_NAME_1.equals(rvApiDTO_1.getProduct()) || PRODUCT_NAME_2.equals(rvApiDTO_1.getProduct()) );
        Assertions.assertTrue(PRODUCT_ID_1 == rvApiDTO_1.getProductId() || PRODUCT_ID_2 == rvApiDTO_1.getProductId());


        Assertions.assertTrue(API_NAME == rvApiDTO_2.getApiName());
        Assertions.assertTrue(API_VERSION == rvApiDTO_2.getVersion());
        Assertions.assertTrue(API_DESCRITPION == rvApiDTO_2.getDescription());
        Assertions.assertTrue(PRODUCT_NAME_3.equals(rvApiDTO_2.getProduct()));
        Assertions.assertTrue(PRODUCT_ID_3 == rvApiDTO_2.getProductId());
    }

    private ApiVersion populateApiVersion(ApiVersion apiVersionMock, String productName, int productId)
    {

        Product product = createProduct(productName, productId);

        Api api = apiVersionMock.getApi();
        when(api.getName()).thenReturn(API_NAME);
        when(api.getProduct()).thenReturn(product);
        when(apiVersionMock.getApi()).thenReturn(api);
        when(apiVersionMock.getVersion()).thenReturn(API_VERSION);
        when(apiVersionMock.getDescription()).thenReturn(API_DESCRITPION);
        return apiVersionMock;
    }

    private Product createProduct(String productName, int productId)
    {
        Product product = mock(Product.class);
        when(product.getUuaa()).thenReturn(UUAA);
        when(product.getName()).thenReturn(productName);
        when(product.getId()).thenReturn(productId
        );
        return product;
    }
}
