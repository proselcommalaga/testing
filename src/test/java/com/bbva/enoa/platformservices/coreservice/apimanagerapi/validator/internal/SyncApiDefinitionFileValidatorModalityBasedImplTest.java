package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.DefinitionFileException;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.common.util.SwaggerConverter;
import io.swagger.models.*;
import io.swagger.models.parameters.Parameter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.yaml.snakeyaml.error.YAMLException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SyncApiDefinitionFileValidatorModalityBasedImplTest
{
    private static final String DASHBOARD_URL = "http://url/";

    @Mock
    private SwaggerConverter swaggerConverter;

    private SyncApiDefinitionFileValidatorModalityBasedImpl syncApiDefinitionFileValidatorModalityBased;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        syncApiDefinitionFileValidatorModalityBased = new SyncApiDefinitionFileValidatorModalityBasedImpl(DASHBOARD_URL, swaggerConverter);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                swaggerConverter
        );
    }

    @Nested
    class ParseAndValidate
    {
        String content = "content";

        @ParameterizedTest
        @EnumSource(ApiType.class)
        @DisplayName("Parse and validate -> definition file exception")
        public void yamlDefinitionError(ApiType apiType)
        {
            var product = Mockito.mock(Product.class);
            when(swaggerConverter.parseSwaggerFromString(any())).thenThrow(YAMLException.class);

            DefinitionFileException exception = null;
            try
            {
                syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
            }
            catch (DefinitionFileException ex)
            {
                exception = ex;
            }

            assertNotNull(exception);
            assertEquals(1, exception.getErrorList().size());
            assertTrue(exception.getErrorList().get(0).startsWith("Yaml exception: error parsing the Yaml file"));
            verify(swaggerConverter).parseSwaggerFromString(content);
        }

        @ParameterizedTest
        @EnumSource(ApiType.class)
        @DisplayName("Parse and validate -> definition file empty: null definition")
        public void nullDefinition(ApiType apiType)
        {
            var product = Mockito.mock(Product.class);
            when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(null);

            DefinitionFileException exception = null;
            try
            {
                syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
            }
            catch (DefinitionFileException ex)
            {
                exception = ex;
            }

            assertNotNull(exception);
            assertEquals(1, exception.getErrorList().size());
            assertTrue(exception.getErrorList().get(0).startsWith("Swagger parse error: the swagger content or swagger file not found"));
            verify(swaggerConverter).parseSwaggerFromString(content);
        }

        @Nested
        class ValidateInfoSwagger
        {
            @ParameterizedTest
            @EnumSource(ApiType.class)
            @DisplayName("Parse and validate -> no swagger info error")
            public void nullInfo(ApiType apiType)
            {
                var product = Mockito.mock(Product.class);
                var swagger = Mockito.mock(Swagger.class);

                when(swagger.getInfo()).thenReturn(null);
                when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                DefinitionFileException exception = null;
                try
                {
                    syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                }
                catch (DefinitionFileException ex)
                {
                    exception = ex;
                }

                assertNotNull(exception);
                assertFalse(exception.getErrorList().isEmpty());
                assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: Info not found")));
                verify(swaggerConverter).parseSwaggerFromString(content);
            }

            @Nested
            class ValidateInfoTitle
            {
                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> no swagger title error")
                public void nullTitle(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    when(swagger.getInfo()).thenReturn(info);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: Info -> Title not found")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> swagger title exceeds the max length")
                public void titleMaxLenght(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    when(swagger.getInfo()).thenReturn(info);
                    when(info.getTitle()).thenReturn(StringUtils.repeat("x", Constants.MAX_NAME_LENGTH+1));
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Too long: Info -> Title length is larger")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> invalid swagger title")
                public void invalidTitle(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    when(swagger.getInfo()).thenReturn(info);
                    when(info.getTitle()).thenReturn("nombre API invalido");
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Invalid format: Info -> Title only can contain")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }
            }

            @Nested
            class ValidateInfoDescription
            {
                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> no swagger title error")
                public void nullDescription(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    when(swagger.getInfo()).thenReturn(info);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: Info -> Description not found")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> swagger description exceeds the max length")
                public void descriptionMaxLenght(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    when(swagger.getInfo()).thenReturn(info);
                    when(info.getDescription()).thenReturn(StringUtils.repeat("x", Constants.MAX_DESCRIPTION_LENGTH+1));
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Too long: Info -> Description length is larger")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }
            }

            @Nested
            class ValidateInfoExternalApiParam
            {
                @ParameterizedTest
                @EnumSource(value = ApiType.class, names = {"NOT_GOVERNED", "GOVERNED", "EXTERNAL"})
                @DisplayName("Parse and validate -> no swagger extensions error for external apis")
                public void nullExtensionsExternalApiParam(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    when(swagger.getInfo()).thenReturn(info);
                    when(swagger.getVendorExtensions()).thenReturn(null);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: any NOVA extensions found.")));
                    assertEquals((int) exception.getErrorList().stream().filter(error -> error.startsWith("Swagger error: any NOVA extensions found.")).count(), 2);
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @EnumSource(value = ApiType.class, names = {"NOT_GOVERNED", "GOVERNED", "EXTERNAL"})
                @DisplayName("Parse and validate -> no swagger external api param error")
                public void nullExternalApiParam(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    // Adding the bussiness unit to avoid the check
                    var extensions = new HashMap<String, Object>();
                    extensions.put("business-unit", "JGMV");

                    var vendorExtensionMap = new HashMap<String, Object>();
                    vendorExtensionMap.put("x-generator-properties", extensions);

                    when(swagger.getInfo()).thenReturn(info);
                    when(swagger.getVendorExtensions()).thenReturn(vendorExtensionMap);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    // If external api type, check the mandatory param, in other case, its the correct way
                    if(apiType.isExternal()){
                        assertEquals((int) exception.getErrorList().stream().filter(error -> error.startsWith("Swagger error: 'x-external-api' is a mandatory parameter for external apis.")).count(), 1);
                    }
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @EnumSource(value = ApiType.class, names = {"EXTERNAL"})
                @DisplayName("Parse and validate -> invalid external api param error")
                public void invalidExternalApiParamExternal(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);
                    var extensions = new HashMap<String, Object>();
                    extensions.put("business-unit", "JGMV");
                    extensions.put("x-external-api", "false");
                    var vendorExtensionMap = new HashMap<String, Object>();
                    vendorExtensionMap.put("x-generator-properties", extensions);

                    when(product.getUuaa()).thenReturn("JGMV");
                    when(swagger.getInfo()).thenReturn(info);
                    when(swagger.getVendorExtensions()).thenReturn(vendorExtensionMap);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: 'x-external-api' is a mandatory parameter for external apis.")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @EnumSource(value = ApiType.class, names = {"NOT_GOVERNED", "GOVERNED"})
                @DisplayName("Parse and validate -> invalid external api param error")
                public void invalidExternalApiParamNoExternal(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);
                    var extensions = new HashMap<String, Object>();
                    extensions.put("business-unit", "otherUUAA");
                    extensions.put("x-external-api", "true");
                    var vendorExtensionMap = new HashMap<String, Object>();
                    vendorExtensionMap.put("x-generator-properties", extensions);

                    when(product.getUuaa()).thenReturn("JGMV");
                    when(swagger.getInfo()).thenReturn(info);
                    when(swagger.getVendorExtensions()).thenReturn(vendorExtensionMap);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: x-external-api is a forbidden parameter for not external apis.")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

            }


            @Nested
            class ValidateInfoBusinessUnit
            {
                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> no swagger extensions error")
                public void nullExtensions(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    when(swagger.getInfo()).thenReturn(info);
                    when(swagger.getVendorExtensions()).thenReturn(null);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: any NOVA extensions found")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> no swagger business unit error")
                public void nullBusinessUnit(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    when(swagger.getInfo()).thenReturn(info);
                    when(swagger.getVendorExtensions()).thenReturn(Collections.emptyMap());
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: business-unit -> business-unit not found")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @EnumSource(value = ApiType.class, names = {"NOT_GOVERNED", "GOVERNED"})
                @DisplayName("Parse and validate -> invalid business unit error")
                public void invalidBusinessUnit(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);
                    var extensions = new HashMap<String, Object>();
                    extensions.put("business-unit", "otherUUAA");
                    var vendorExtensionMap = new HashMap<String, Object>();
                    vendorExtensionMap.put("x-generator-properties", extensions);

                    when(product.getUuaa()).thenReturn("JGMV");
                    when(swagger.getInfo()).thenReturn(info);
                    when(swagger.getVendorExtensions()).thenReturn(vendorExtensionMap);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: business-unit -> business-unit do not match")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

            }

            @Nested
            class ValidateInfoVersion
            {
                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> no swagger version error")
                public void nullVersion(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    when(swagger.getInfo()).thenReturn(info);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: Info -> Version not found")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> swagger version exceeds the max length")
                public void versionMaxLenght(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    when(swagger.getInfo()).thenReturn(info);
                    when(info.getVersion()).thenReturn(StringUtils.repeat("x", Constants.MAX_NAME_LENGTH+1));
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Too long: Info -> Version length is larger")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @CsvSource({"NOT_GOVERNED, 1.0", "GOVERNED, 1.0.b", "EXTERNAL, 1.0.1102"})
                @DisplayName("Parse and validate -> invalid swagger version error")
                public void invalidVersion(ApiType apiType, String apiVersion)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    when(swagger.getInfo()).thenReturn(info);
                    when(info.getVersion()).thenReturn(apiVersion);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Invalid format: Info -> Version only can contain this set")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

            }

            @Nested
            class ValidateInfoContact
            {
                @ParameterizedTest
                @EnumSource(value = ApiType.class, names = {"NOT_GOVERNED", "GOVERNED"})
                @DisplayName("Parse and validate -> no swagger contact error")
                public void nullContact(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    when(swagger.getInfo()).thenReturn(info);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: Info -> Contact not found.")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @Nested
                class ContactName
                {
                    @ParameterizedTest
                    @EnumSource(value = ApiType.class, names = {"NOT_GOVERNED", "GOVERNED"})
                    @DisplayName("Parse and validate -> no swagger contact name error")
                    public void nullContactName(ApiType apiType)
                    {
                        var product = Mockito.mock(Product.class);
                        var swagger = Mockito.mock(Swagger.class);
                        var info = Mockito.mock(Info.class);
                        var contact = Mockito.mock(Contact.class);

                        when(swagger.getInfo()).thenReturn(info);
                        when(info.getContact()).thenReturn(contact);
                        when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                        DefinitionFileException exception = null;
                        try
                        {
                            syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                        }
                        catch (DefinitionFileException ex)
                        {
                            exception = ex;
                        }

                        assertNotNull(exception);
                        assertFalse(exception.getErrorList().isEmpty());
                        assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: Info -> Contact -> Name")));
                        verify(swaggerConverter).parseSwaggerFromString(content);
                    }

                    @ParameterizedTest
                    @EnumSource(value = ApiType.class, names = {"NOT_GOVERNED", "GOVERNED"})
                    @DisplayName("Parse and validate -> invalid swagger contact name")
                    public void invalidContactName(ApiType apiType)
                    {
                        var product = Mockito.mock(Product.class);
                        var swagger = Mockito.mock(Swagger.class);
                        var info = Mockito.mock(Info.class);
                        var contact = Mockito.mock(Contact.class);

                        when(swagger.getInfo()).thenReturn(info);
                        when(info.getContact()).thenReturn(contact);
                        when(contact.getName()).thenReturn("contactName");
                        when(product.getName()).thenReturn("otherContactName");
                        when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                        DefinitionFileException exception = null;
                        try
                        {
                            syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                        }
                        catch (DefinitionFileException ex)
                        {
                            exception = ex;
                        }

                        assertNotNull(exception);
                        assertFalse(exception.getErrorList().isEmpty());
                        assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: Info -> Contact -> Name")));
                        verify(swaggerConverter).parseSwaggerFromString(content);
                    }
                }

                @Nested
                class ContactURL
                {
                    @ParameterizedTest
                    @EnumSource(value = ApiType.class, names = {"NOT_GOVERNED", "GOVERNED"})
                    @DisplayName("Parse and validate -> no swagger contact url error")
                    public void nullContactURL(ApiType apiType)
                    {
                        var product = Mockito.mock(Product.class);
                        var swagger = Mockito.mock(Swagger.class);
                        var info = Mockito.mock(Info.class);
                        var contact = Mockito.mock(Contact.class);

                        when(swagger.getInfo()).thenReturn(info);
                        when(info.getContact()).thenReturn(contact);
                        when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                        DefinitionFileException exception = null;
                        try
                        {
                            syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                        }
                        catch (DefinitionFileException ex)
                        {
                            exception = ex;
                        }

                        assertNotNull(exception);
                        assertFalse(exception.getErrorList().isEmpty());
                        assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: Info -> Contact -> URL")));
                        verify(swaggerConverter).parseSwaggerFromString(content);
                    }

                    @ParameterizedTest
                    @EnumSource(value = ApiType.class, names = {"NOT_GOVERNED", "GOVERNED"})
                    @DisplayName("Parse and validate -> invalid swagger contact url")
                    public void invalidContactURL(ApiType apiType)
                    {
                        var product = Mockito.mock(Product.class);
                        var swagger = Mockito.mock(Swagger.class);
                        var info = Mockito.mock(Info.class);
                        var contact = Mockito.mock(Contact.class);

                        when(swagger.getInfo()).thenReturn(info);
                        when(info.getContact()).thenReturn(contact);
                        when(contact.getUrl()).thenReturn("noProductURL");
                        when(product.getName()).thenReturn("productName");
                        when(product.getId()).thenReturn(909);
                        when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                        DefinitionFileException exception = null;
                        try
                        {
                            syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                        }
                        catch (DefinitionFileException ex)
                        {
                            exception = ex;
                        }

                        assertNotNull(exception);
                        assertFalse(exception.getErrorList().isEmpty());
                        assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: Info -> Contact -> URL")));
                        verify(swaggerConverter).parseSwaggerFromString(content);
                    }
                }

                @Nested
                class ContactEmail
                {
                    @ParameterizedTest
                    @EnumSource(value = ApiType.class, names = {"NOT_GOVERNED", "GOVERNED"})
                    @DisplayName("Parse and validate -> no swagger contact email error")
                    public void nullContactEmail(ApiType apiType)
                    {
                        var product = Mockito.mock(Product.class);
                        var swagger = Mockito.mock(Swagger.class);
                        var info = Mockito.mock(Info.class);
                        var contact = Mockito.mock(Contact.class);

                        when(swagger.getInfo()).thenReturn(info);
                        when(info.getContact()).thenReturn(contact);
                        when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                        DefinitionFileException exception = null;
                        try
                        {
                            syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                        }
                        catch (DefinitionFileException ex)
                        {
                            exception = ex;
                        }

                        assertNotNull(exception);
                        assertFalse(exception.getErrorList().isEmpty());
                        assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: Info -> Contact -> Email")));
                        verify(swaggerConverter).parseSwaggerFromString(content);
                    }

                    @ParameterizedTest
                    @EnumSource(value = ApiType.class, names = {"NOT_GOVERNED", "GOVERNED"})
                    @DisplayName("Parse and validate -> invalid swagger contact email")
                    public void invalidContactEmail(ApiType apiType)
                    {
                        var product = Mockito.mock(Product.class);
                        var swagger = Mockito.mock(Swagger.class);
                        var info = Mockito.mock(Info.class);
                        var contact = Mockito.mock(Contact.class);

                        when(swagger.getInfo()).thenReturn(info);
                        when(info.getContact()).thenReturn(contact);
                        when(contact.getEmail()).thenReturn("noProductEmail");
                        when(product.getEmail()).thenReturn("productEmail");
                        when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                        DefinitionFileException exception = null;
                        try
                        {
                            syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                        }
                        catch (DefinitionFileException ex)
                        {
                            exception = ex;
                        }

                        assertNotNull(exception);
                        assertFalse(exception.getErrorList().isEmpty());
                        assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: Info -> Contact -> Email")));
                        verify(swaggerConverter).parseSwaggerFromString(content);
                    }
                }
            }

            @Nested
            class ValidateInfoScheme
            {
                @ParameterizedTest
                @EnumSource(value = ApiType.class, names = {"NOT_GOVERNED", "GOVERNED"})
                @DisplayName("Parse and validate -> no swagger scheme error")
                public void nullScheme(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    when(swagger.getInfo()).thenReturn(info);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);
                    when(swagger.getSchemes()).thenReturn(null);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: Schemes -> schemes field is mandatory")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @CsvSource({"NOT_GOVERNED, WS", "GOVERNED, WSS"})
                @DisplayName("Parse and validate -> invalid swagger scheme")
                public void invalidScheme(ApiType apiType, Scheme scheme)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class);
                    var info = Mockito.mock(Info.class);

                    when(swagger.getInfo()).thenReturn(info);
                    when(swagger.getSchemes()).thenReturn(List.of(scheme));
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: Schemes -> schemes field values")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }
            }
        }

        @Nested
        class ValidatePathSwagger
        {
            @ParameterizedTest
            @EnumSource(ApiType.class)
            @DisplayName("Parse and validate -> no swagger paths error")
            public void nullPaths(ApiType apiType)
            {
                var product = Mockito.mock(Product.class);
                var swagger = Mockito.mock(Swagger.class);

                when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                DefinitionFileException exception = null;
                try
                {
                    syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                }
                catch (DefinitionFileException ex)
                {
                    exception = ex;
                }

                assertNotNull(exception);
                assertFalse(exception.getErrorList().isEmpty());
                assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: no path found")));
                verify(swaggerConverter).parseSwaggerFromString(content);
            }

            @ParameterizedTest
            @EnumSource(ApiType.class)
            @DisplayName("Parse and validate -> empty swagger paths error")
            public void emptyPaths(ApiType apiType)
            {
                var product = Mockito.mock(Product.class);
                var swagger = Mockito.mock(Swagger.class);

                when(swagger.getPaths()).thenReturn(Collections.emptyMap());
                when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                DefinitionFileException exception = null;
                try
                {
                    syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                }
                catch (DefinitionFileException ex)
                {
                    exception = ex;
                }

                assertNotNull(exception);
                assertFalse(exception.getErrorList().isEmpty());
                assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: no path found")));
                verify(swaggerConverter).parseSwaggerFromString(content);
            }

            @Nested
            class ValidateOperationMap
            {
                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> null swagger paths operation map error")
                public void nullOperationMap(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class, RETURNS_DEEP_STUBS);

                    var paths = new HashMap<String, Path>();
                    paths.put("path", new Path());

                    when(swagger.getPaths()).thenReturn(paths);
                    when(swagger.getPath("path").getOperationMap()).thenReturn(null);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: no method defined in path")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> empty swagger paths operation map error")
                public void emptyOperationMap(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class, RETURNS_DEEP_STUBS);

                    var paths = new HashMap<String, Path>();
                    paths.put("path", new Path());

                    when(swagger.getPaths()).thenReturn(paths);
                    when(swagger.getPath("path").getOperationMap()).thenReturn(Collections.emptyMap());
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: no method defined in path")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> null swagger path operation description error")
                public void nullOperationDescription(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class, RETURNS_DEEP_STUBS);
                    var operation = Mockito.mock(Operation.class);

                    var paths = new HashMap<String, Path>();
                    paths.put("path", new Path());
                    var operationMap = new HashMap<HttpMethod, Operation>();
                    operationMap.put(HttpMethod.GET, operation);

                    when(swagger.getPaths()).thenReturn(paths);
                    when(swagger.getPath("path").getOperationMap()).thenReturn(operationMap);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: description not defined")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> max length swagger path operation description error")
                public void maxLengthOperationDescription(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class, RETURNS_DEEP_STUBS);
                    var operation = Mockito.mock(Operation.class);

                    var paths = new HashMap<String, Path>();
                    paths.put("path", new Path());
                    var operationMap = new HashMap<HttpMethod, Operation>();
                    operationMap.put(HttpMethod.GET, operation);

                    when(swagger.getPaths()).thenReturn(paths);
                    when(swagger.getPath("path").getOperationMap()).thenReturn(operationMap);
                    when(operation.getDescription()).thenReturn(StringUtils.repeat("x", Constants.MAX_NAME_LENGTH + 1));
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Too long: description length for")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }
                
                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> null swagger path operation id error")
                public void nullOperationId(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class, RETURNS_DEEP_STUBS);
                    var operation = Mockito.mock(Operation.class);

                    var paths = new HashMap<String, Path>();
                    paths.put("path", new Path());
                    var operationMap = new HashMap<HttpMethod, Operation>();
                    operationMap.put(HttpMethod.GET, operation);

                    when(swagger.getPaths()).thenReturn(paths);
                    when(swagger.getPath("path").getOperationMap()).thenReturn(operationMap);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: operationId not defined")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @ParameterizedTest
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> null swagger path operation responses error")
                public void nullOperationResponses(ApiType apiType)
                {
                    var product = Mockito.mock(Product.class);
                    var swagger = Mockito.mock(Swagger.class, RETURNS_DEEP_STUBS);
                    var operation = Mockito.mock(Operation.class);

                    var paths = new HashMap<String, Path>();
                    paths.put("path", new Path());
                    var operationMap = new HashMap<HttpMethod, Operation>();
                    operationMap.put(HttpMethod.GET, operation);

                    when(swagger.getPaths()).thenReturn(paths);
                    when(swagger.getPath("path").getOperationMap()).thenReturn(operationMap);
                    when(operation.getResponses()).thenReturn(null);
                    when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                    DefinitionFileException exception = null;
                    try
                    {
                        syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertFalse(exception.getErrorList().isEmpty());
                    assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: responses not defined for path")));
                    verify(swaggerConverter).parseSwaggerFromString(content);
                }

                @Nested
                class ValidateOperationParameters
                {
                    @ParameterizedTest
                    @EnumSource(ApiType.class)
                    @DisplayName("Parse and validate -> null swagger path operation parameter name error")
                    public void nullParameterName(ApiType apiType)
                    {
                        var product = Mockito.mock(Product.class);
                        var swagger = Mockito.mock(Swagger.class, RETURNS_DEEP_STUBS);
                        var operation = Mockito.mock(Operation.class);
                        var parameter = Mockito.mock(Parameter.class);

                        var paths = new HashMap<String, Path>();
                        paths.put("path", new Path());
                        var operationMap = new HashMap<HttpMethod, Operation>();
                        operationMap.put(HttpMethod.GET, operation);

                        when(swagger.getPaths()).thenReturn(paths);
                        when(swagger.getPath("path").getOperationMap()).thenReturn(operationMap);
                        when(operation.getParameters()).thenReturn(List.of(parameter));
                        when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                        DefinitionFileException exception = null;
                        try
                        {
                            syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                        }
                        catch (DefinitionFileException ex)
                        {
                            exception = ex;
                        }

                        assertNotNull(exception);
                        assertFalse(exception.getErrorList().isEmpty());
                        assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: there is a missing parameter name for method")));
                        verify(swaggerConverter).parseSwaggerFromString(content);
                    }

                    @ParameterizedTest
                    @EnumSource(ApiType.class)
                    @DisplayName("Parse and validate -> swagger path operation path parameter not required error")
                    public void pathParameterNotRequired(ApiType apiType)
                    {
                        var product = Mockito.mock(Product.class);
                        var swagger = Mockito.mock(Swagger.class, RETURNS_DEEP_STUBS);
                        var operation = Mockito.mock(Operation.class);
                        var parameter = Mockito.mock(Parameter.class);

                        var paths = new HashMap<String, Path>();
                        paths.put("path", new Path());
                        var operationMap = new HashMap<HttpMethod, Operation>();
                        operationMap.put(HttpMethod.GET, operation);

                        when(swagger.getPaths()).thenReturn(paths);
                        when(swagger.getPath("path").getOperationMap()).thenReturn(operationMap);
                        when(operation.getParameters()).thenReturn(List.of(parameter));
                        when(parameter.getName()).thenReturn("xxx");
                        when(parameter.getIn()).thenReturn("path");
                        when(parameter.getRequired()).thenReturn(false);
                        when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

                        DefinitionFileException exception = null;
                        try
                        {
                            syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                        }
                        catch (DefinitionFileException ex)
                        {
                            exception = ex;
                        }

                        assertNotNull(exception);
                        assertFalse(exception.getErrorList().isEmpty());
                        assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith("Swagger error: parameter")));
                        verify(swaggerConverter).parseSwaggerFromString(content);
                    }
                }
            }

        }

        @ParameterizedTest
        @EnumSource(ApiType.class)
        @DisplayName("Parse and validate -> ok")
        public void ok(ApiType apiType) throws DefinitionFileException
        {
            var uuaa = "JGMV";
            var productId = 909;
            var productName = "productName";
            var productEmail = "product@email.es";
            var product = Mockito.mock(Product.class);
            var swagger = Mockito.mock(Swagger.class, RETURNS_DEEP_STUBS);
            var info = Mockito.mock(Info.class);
            var contact = Mockito.mock(Contact.class);
            var operation = Mockito.mock(Operation.class);
            var parameter = Mockito.mock(Parameter.class);

            var extensions = new HashMap<String, Object>();
            extensions.put("business-unit", uuaa);
            if(apiType.isExternal()){extensions.put("x-external-api", true);}
            var vendorExtensionMap = new HashMap<String, Object>();
            vendorExtensionMap.put("x-generator-properties", extensions);

            var paths = new HashMap<String, Path>();
            paths.put("path", new Path());
            var operationMap = new HashMap<HttpMethod, Operation>();
            operationMap.put(HttpMethod.GET, operation);

            when(swagger.getInfo()).thenReturn(info);
            when(swagger.getVendorExtensions()).thenReturn(vendorExtensionMap);
            when(swagger.getSchemes()).thenReturn(List.of(Scheme.HTTP, Scheme.HTTPS));
            when(swagger.getPaths()).thenReturn(paths);
            when(swagger.getPath("path").getOperationMap()).thenReturn(operationMap);
            when(product.getId()).thenReturn(productId);
            when(product.getUuaa()).thenReturn(uuaa);
            when(product.getName()).thenReturn(productName);
            when(product.getEmail()).thenReturn(productEmail);
            when(info.getTitle()).thenReturn("title");
            when(info.getDescription()).thenReturn("description");
            when(info.getVersion()).thenReturn("1.0.0");
            when(info.getContact()).thenReturn(contact);
            when(contact.getName()).thenReturn(productName);
            when(contact.getUrl()).thenReturn(DASHBOARD_URL + Constants.URL_PATH + productName + Constants.PATH_SLASH + productId);
            when(contact.getEmail()).thenReturn(productEmail);
            when(operation.getDescription()).thenReturn("operationDescription");
            when(operation.getOperationId()).thenReturn("operationId");
            when(operation.getResponses()).thenReturn(Collections.emptyMap());
            when(operation.getParameters()).thenReturn(List.of(parameter));
            when(parameter.getName()).thenReturn("xxx");
            when(parameter.getIn()).thenReturn("path");
            when(parameter.getRequired()).thenReturn(true);
            when(swaggerConverter.parseSwaggerFromString(any())).thenReturn(swagger);

            Swagger ret = syncApiDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);

            assertEquals(swagger, ret);
            verify(swaggerConverter).parseSwaggerFromString(content);
        }

    }

    @Nested
    class IsModalitySupported
    {
        @Test
        @DisplayName("Is modality supported -> SYNC")
        public void sync()
        {
            assertTrue(syncApiDefinitionFileValidatorModalityBased.isModalitySupported(ApiModality.SYNC));
        }

        @ParameterizedTest
        @EnumSource(value = ApiModality.class, names = {"ASYNC_BACKTOBACK", "ASYNC_BACKTOFRONT"})
        @DisplayName("Is modality supported -> ASYNC")
        public void async(ApiModality modality)
        {
            assertFalse(syncApiDefinitionFileValidatorModalityBased.isModalitySupported(modality));
        }
    }
}
