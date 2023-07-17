package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.apidefinitionvalidations.impl;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.NewReleaseVersionServiceDto;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiDTO;
import com.bbva.enoa.apirestgen.releaseversionsapi.model.ValidationErrorDto;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.VersioncontrolsystemClientImpl;
import com.bbva.enoa.platformservices.coreservice.releaseversionsapi.model.NovaYml;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util.Constants.API_PATH_NOT_FOUND;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractAsyncApiDefinitionBehaviorValidatorImplTest
{
    private static final String TAG_NAME = "tagName";
    private static final String FOLDER = "folder";
    private static final byte[] RAW_ASYNC_API = "rawAsyncApi".getBytes(StandardCharsets.UTF_8);
    private static final int REPO_ID = 100;

    private static final String API_NAME = "apiName";
    private static final String UUAA = "uuaa";
    private static final String PRODUCT_NAME = "productName";
    private static final int PRODUCT_ID = 1;
    private static final String API_VERSION = "apiVersion";
    private static final String API_DESCRITPION = "apiDescription";

    @Mock
    private VersioncontrolsystemClientImpl versioncontrolsystemClient;

    private class ConcreteAsyncApiDefinitionValidatorAsServerApiImpl<A, AV, AI>
            extends ConcreteAsyncApiDefinitionValidatorAsConsumerApiImpl
    {
        /**
         * Instantiates a new Asyncapi validator.
         *
         * @param versioncontrolsystemClient
         */
        ConcreteAsyncApiDefinitionValidatorAsServerApiImpl(VersioncontrolsystemClientImpl versioncontrolsystemClient)
        {
            super(versioncontrolsystemClient);
        }

        @Override
        boolean isImplementedAsServed(NovaYml novaYml, ApiVersion apiVersion)
        {
            return true;
        }
    }

    private class ConcreteAsyncApiDefinitionValidatorAsConsumerApiImpl<A, AV, AI>
            extends AbstractAsyncApiDefinitionValidatorImpl
    {

        ApiVersion apiVersion =
                mock(ApiVersion.class);

        /**
         * Instantiates a new Asyncapi validator.
         *
         * @param versioncontrolsystemClient
         */
        ConcreteAsyncApiDefinitionValidatorAsConsumerApiImpl(VersioncontrolsystemClientImpl versioncontrolsystemClient)
        {
            super(versioncontrolsystemClient);
        }

        @Override
        ApiVersion apiVersionByMd5Hash(byte[] rawAsyncAPIfromVCS)
        {
            return apiVersion;
        }

        @Override
        RVApiDTO buildRVApiDtoSpecificInformation(RVApiDTO rvApiDTO, ApiVersion apiVersion)
        {
            return rvApiDTO;
        }

        @Override
        List<ValidationErrorDto> validateWholeApiSet(NewReleaseVersionServiceDto newReleaseVersionServiceDto)
        {
            return Collections.emptyList();
        }

        @Override
        Set<String> getAsyncAPIDefinitionList(NovaYml novaYml)
        {
            return Set.of("asyncApiPath");
        }

        @Override
        boolean isImplementedAsServed(NovaYml novaYml, ApiVersion apiVersion)
        {
            return false;
        }

        @Override
        List<ValidationErrorDto> specificValidationsApi(
                NewReleaseVersionServiceDto newReleaseVersionServiceDto, ApiVersion apiVersion, String asyncApiPath,
                String tagName)
        {
            return Collections.emptyList();
        }

    }

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void validateAndAssociateAsyncApiImplementedAsConsumerOk()
    {
        // Instantiate validator
        ConcreteAsyncApiDefinitionValidatorAsConsumerApiImpl<Api, ApiVersion, ApiImplementation>
                validatorAsConsumerApi = new ConcreteAsyncApiDefinitionValidatorAsConsumerApiImpl<>(versioncontrolsystemClient);

        //Test
        NewReleaseVersionServiceDto newReleaseVersionServiceDto = validateAndAssociateAsyncApiImplementedOk(validatorAsConsumerApi);

        //Validations ApisServed
        Assertions.assertEquals(0, newReleaseVersionServiceDto.getApisServed().length);
        Assertions.assertEquals(1, newReleaseVersionServiceDto.getApisConsumed().length);
        compareRVApiDTO(newReleaseVersionServiceDto.getApisConsumed()[0]);
    }

    @Test
    public void validateAndAssociateAsyncApiImplementedAsServedOk()
    {
        // Instantiate validator
        ConcreteAsyncApiDefinitionValidatorAsServerApiImpl<Api, ApiVersion, ApiImplementation>
                validatorAsServerApi = new ConcreteAsyncApiDefinitionValidatorAsServerApiImpl<>(versioncontrolsystemClient);

        //Test
        NewReleaseVersionServiceDto newReleaseVersionServiceDto = validateAndAssociateAsyncApiImplementedOk(validatorAsServerApi);

        //Validations ApisConsumed
        Assertions.assertEquals(0, newReleaseVersionServiceDto.getApisConsumed().length);
        Assertions.assertEquals(1, newReleaseVersionServiceDto.getApisServed().length);
        compareRVApiDTO(newReleaseVersionServiceDto.getApisServed()[0]);
    }

    @Test
    public void validateAndAssociateAsyncApiApiVersionNull()
    {
        ConcreteAsyncApiDefinitionValidatorAsServerApiImpl<Api, ApiVersion, ApiImplementation> validatorAsServerApi =
                new ConcreteAsyncApiDefinitionValidatorAsServerApiImpl<>(versioncontrolsystemClient);
        validatorAsServerApi.apiVersion = null;

        //When
        NewReleaseVersionServiceDto newReleaseVersionServiceDto =
                new NewReleaseVersionServiceDto();
        NovaYml novaYml = mock(NovaYml.class);
        newReleaseVersionServiceDto.setFolder(FOLDER);
        when(versioncontrolsystemClient.getFileFromProject(any(), eq(REPO_ID), eq(TAG_NAME)))
                .thenReturn(RAW_ASYNC_API);

        // Then
        List<ValidationErrorDto> errorDtoList =
                validatorAsServerApi.validateAndAssociateAsyncApi(newReleaseVersionServiceDto, REPO_ID, TAG_NAME, novaYml);

        //Validate
        Assertions.assertEquals(1, errorDtoList.size());
        Assertions.assertEquals(API_PATH_NOT_FOUND, API_PATH_NOT_FOUND, errorDtoList.get(0).getCode());
    }

    private NewReleaseVersionServiceDto validateAndAssociateAsyncApiImplementedOk(
            ConcreteAsyncApiDefinitionValidatorAsConsumerApiImpl validator)
    {
        //vars

        //mocks
        NewReleaseVersionServiceDto newReleaseVersionServiceDto =
                new NewReleaseVersionServiceDto();
        NovaYml novaYml = mock(NovaYml.class);
        populateApiVersion(validator.apiVersion);

        //When
        newReleaseVersionServiceDto.setFolder(FOLDER);
        when(versioncontrolsystemClient.getFileFromProject(any(), eq(REPO_ID), eq(TAG_NAME)))
                .thenReturn(RAW_ASYNC_API);

        // Then
        List<ValidationErrorDto> errorDtoList =
                validator.validateAndAssociateAsyncApi(newReleaseVersionServiceDto, REPO_ID, TAG_NAME, novaYml);

        // Validations
        Assertions.assertTrue(errorDtoList.isEmpty());

        return newReleaseVersionServiceDto;
    }

    private void compareRVApiDTO(RVApiDTO rvApiDTOResult)
    {
        Assertions.assertEquals(API_NAME, rvApiDTOResult.getApiName());

        Assertions.assertEquals(UUAA, rvApiDTOResult.getUuaa());
        Assertions.assertEquals(PRODUCT_NAME, rvApiDTOResult.getProduct());
        Assertions.assertEquals(PRODUCT_ID, rvApiDTOResult.getProductId().longValue());

        Assertions.assertEquals(API_VERSION, rvApiDTOResult.getVersion());
        Assertions.assertEquals(API_DESCRITPION, rvApiDTOResult.getDescription());
    }

    private void populateApiVersion(ApiVersion apiVersionMock)
    {
        Product product = createProduct();

        Api api = mock(Api.class);
        when(api.getName()).thenReturn(API_NAME);
        when(api.getProduct()).thenReturn(product);
        when(apiVersionMock.getApi()).thenReturn(api);
        when(apiVersionMock.getVersion()).thenReturn(API_VERSION);
        when(apiVersionMock.getDescription()).thenReturn(API_DESCRITPION);
        when(api.getApiModality()).thenReturn(ApiModality.ASYNC_BACKTOFRONT);
    }

    private Product createProduct()
    {
        Product product = mock(Product.class);
        when(product.getUuaa()).thenReturn(UUAA);
        when(product.getName()).thenReturn(PRODUCT_NAME);
        when(product.getId()).thenReturn(PRODUCT_ID);
        return product;
    }
}
