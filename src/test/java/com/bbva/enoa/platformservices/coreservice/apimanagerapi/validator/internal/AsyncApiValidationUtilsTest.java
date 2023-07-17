package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.asyncapi.v2.model.AsyncAPI;
import com.asyncapi.v2.model.info.Contact;
import com.asyncapi.v2.model.info.Info;
import com.asyncapi.v2.model.server.Server;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.NovaCoreTestUtils;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.DefinitionFileException;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.utils.Constants;
import com.bbva.enoa.platformservices.coreservice.common.model.NovaAsyncAPI;
import com.bbva.enoa.platformservices.coreservice.common.util.NovaCustomObjectMapperFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AsyncApiValidationUtilsTest
{

    @Mock
    private NovaCustomObjectMapperFactory novaCustomObjectMapperFactory;
    @InjectMocks
    private AsyncApiValidationUtils asyncApiValidationUtils;

    private final String productName = "product";
    private final String productEmail = "swagger@bbva.com";
    private final Integer productId = 1;


    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        NovaCoreTestUtils.instantiateTestEnvironmentVariables(Map.of("nova.url", "http://url/"));
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                novaCustomObjectMapperFactory
        );
    }

    @Nested
    class ParseDefinitionFile
    {
        String content = "content";

        @Test
        @DisplayName("Parse definition file -> NovaAsyncAPI definition exception")
        void novaAsyncAPIDefinitionException() throws JsonProcessingException
        {
            var objectMapper = Mockito.mock(ObjectMapper.class);

            when(novaCustomObjectMapperFactory.create(any(YAMLFactory.class))).thenReturn(objectMapper);
            when(objectMapper.readValue(anyString(), any(Class.class))).thenThrow(JsonProcessingException.class);

            assertThrows(
                    DefinitionFileException.class,
                    () -> asyncApiValidationUtils.parseDefinitionFile(content)
            );

            verify(novaCustomObjectMapperFactory).create(any(YAMLFactory.class));
            verify(objectMapper).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            verify(objectMapper).readValue(content, NovaAsyncAPI.class);
            verifyNoMoreInteractions(objectMapper);
        }

        @Test
        @DisplayName("Parse definition file -> AsyncAPI definition exception")
        void asyncAPIDefinitionException() throws JsonProcessingException
        {
            var objectMapper = Mockito.mock(ObjectMapper.class);
            var novaAsyncAPI = new NovaAsyncAPI();

            when(novaCustomObjectMapperFactory.create(any(YAMLFactory.class))).thenReturn(objectMapper);
            when(objectMapper.readValue(anyString(), any(Class.class)))
                    .thenReturn(novaAsyncAPI)
                    .thenThrow(JsonProcessingException.class);

            assertThrows(
                    DefinitionFileException.class,
                    () -> asyncApiValidationUtils.parseDefinitionFile(content)
            );

            verify(novaCustomObjectMapperFactory).create(any(YAMLFactory.class));
            verify(objectMapper).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            verify(objectMapper).readValue(content, NovaAsyncAPI.class);
            verify(objectMapper).readValue(content, AsyncAPI.class);
            verifyNoMoreInteractions(objectMapper);
        }

        @Test
        @DisplayName("Parse definition file -> ok")
        public void ok() throws JsonProcessingException, DefinitionFileException
        {
            var objectMapper = Mockito.mock(ObjectMapper.class);
            var novaAsyncAPI = new NovaAsyncAPI();
            var asyncAPI = new AsyncAPI();

            when(novaCustomObjectMapperFactory.create(any(YAMLFactory.class))).thenReturn(objectMapper);
            when(objectMapper.readValue(anyString(), any(Class.class)))
                    .thenReturn(novaAsyncAPI)
                    .thenReturn(asyncAPI);

            NovaAsyncAPI ret = asyncApiValidationUtils.parseDefinitionFile(content);

            assertEquals(novaAsyncAPI, ret);
            assertEquals(asyncAPI, novaAsyncAPI.getAsyncAPI());
            verify(novaCustomObjectMapperFactory).create(any(YAMLFactory.class));
            verify(objectMapper).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            verify(objectMapper).readValue(content, NovaAsyncAPI.class);
            verify(objectMapper).readValue(content, AsyncAPI.class);
            verifyNoMoreInteractions(objectMapper);
        }
    }

    @Nested
    class ValidateAsyncSpecVersion
    {
        @ParameterizedTest
        @DisplayName("Validate async spec version -> invalid major version error")
        @ValueSource(strings = {"1.0.2","0.0.2","1.2.0"})
        void invalidMajorError(String version)
        {
            List<String> errors = asyncApiValidationUtils.validateAsyncSpecVersion(version);

            assertEquals(1, errors.size());
            assertTrue(errors.get(0).startsWith("Invalid version"));
        }

        @ParameterizedTest
        @DisplayName("Validate async spec version -> invalid format error")
        @ValueSource(strings = {"2.0","2.0.beta","2.12.1234"})
        void invalidFormatError(String version)
        {
            List<String> errors = asyncApiValidationUtils.validateAsyncSpecVersion(version);

            assertEquals(1, errors.size());
            assertTrue(errors.get(0).startsWith("Invalid format in version: Version"));
        }

        @ParameterizedTest
        @DisplayName("Validate async spec version -> OK")
        @ValueSource(strings = {"2.0.2","2.0.0","2.12.123"})
        public void ok(String version)
        {
            List<String> errors = asyncApiValidationUtils.validateAsyncSpecVersion(version);

            assertEquals(0, errors.size());
        }

    }

    @Nested
    class ValidateInfo
    {

        ApiType apiType = ApiType.GOVERNED;

        @Test
        @DisplayName("Validate info -> null error")
        void nullInfoError()
        {
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
            var product = Mockito.mock(Product.class);

            when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(null);

            List<String> ret = asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType);

            assertEquals(1, ret.size());
            assertTrue(ret.get(0).startsWith("Info not found"));
        }

        @Nested
        class ValidateInfoTitle
        {
            @Test
            @DisplayName("Validate info title -> null error")
            void nullError()
            {
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
                var product = Mockito.mock(Product.class);
                var info = Mockito.mock(Info.class);

                when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);

                List<String> ret = asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType);
                assertTrue(ret.size() > 1);
                assertTrue(ret.stream().anyMatch(e -> e.startsWith("Info Title not found")));
            }

            @Test
            @DisplayName("Validate info title -> max lenght error")
            void maxLengthError()
            {
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
                var product = Mockito.mock(Product.class);
                var info = Mockito.mock(Info.class);

                when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);
                when(info.getTitle()).thenReturn(StringUtils.repeat("x", Constants.MAX_NAME_LENGTH + 1));

                List<String> ret = asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType);
                assertTrue(ret.size() > 1);
                assertTrue(ret.stream().anyMatch(e -> e.startsWith("Too long Info Title")));
            }

            @ParameterizedTest(name = "[{index}] title: {0}")
            @DisplayName("Validate info title -> invalid format error")
            @ValueSource(strings = {"invalid title", "inv@lid_title", "1nv4l1d#t1tl3"})
            void invalidFormatError(String title)
            {
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
                var product = Mockito.mock(Product.class);
                var info = Mockito.mock(Info.class);

                when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);
                when(info.getTitle()).thenReturn(title);

                List<String> ret = asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType);
                assertTrue(ret.size() > 1);
                assertTrue(ret.stream().anyMatch(e -> e.startsWith("Invalid format Info Title")));
            }
        }

        @Nested
        class ValidateInfoDescription
        {
            @Test
            @DisplayName("Validate info description -> null error")
            void nullError()
            {
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
                var product = Mockito.mock(Product.class);
                var info = Mockito.mock(Info.class);

                when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);

                List<String> ret = asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType);
                assertTrue(ret.size() > 1);
                assertTrue(ret.stream().anyMatch(e -> e.startsWith("Info Description not found")));
            }

            @Test
            @DisplayName("Validate info description -> max lenght error")
            void maxLengthError()
            {
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
                var product = Mockito.mock(Product.class);
                var info = Mockito.mock(Info.class);

                when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);
                when(info.getDescription()).thenReturn(StringUtils.repeat("x", Constants.MAX_DESCRIPTION_LENGTH + 1));

                List<String> ret = asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType);
                assertTrue(ret.size() > 1);
                assertTrue(ret.stream().anyMatch(e -> e.startsWith("Too long Info Description")));
            }
        }

        @Nested
        class ValidateInfoBusinessUnit
        {
            @ParameterizedTest
            @NullAndEmptySource
            @DisplayName("Validate info business unit -> empty error")
            void emptyError(String businessUnit)
            {
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
                var product = Mockito.mock(Product.class);
                var info = Mockito.mock(Info.class);

                when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);
                when(novaAsyncAPI.getXBusinessUnit()).thenReturn(businessUnit);

                List<String> ret = asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType);
                assertTrue(ret.size() > 1);
                assertTrue(ret.stream().anyMatch(e -> e.startsWith("Info business-unit (UUAA) is mandatory")));
            }

            @Test
            @DisplayName("Validate info business unit -> invalid error")
            public void invalidError()
            {
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
                var product = Mockito.mock(Product.class);
                var info = Mockito.mock(Info.class);

                when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);
                when(novaAsyncAPI.getXBusinessUnit()).thenReturn("invalidBusinessUnit");
                when(product.getUuaa()).thenReturn("JGMV");

                List<String> ret = asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType);
                assertTrue(ret.size() > 1);
                assertTrue(ret.stream().anyMatch(e -> e.startsWith("Info Asyncapi business-unit do not match")));
            }

        }

        @Nested
        class ValidateInfoVersion
        {
            @Test
            @DisplayName("Validate info version -> null error")
            public void nullError()
            {
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
                var product = Mockito.mock(Product.class);
                var info = Mockito.mock(Info.class);

                when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);

                List<String> ret = asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType);
                assertTrue(ret.size() > 1);
                assertTrue(ret.stream().anyMatch(e -> e.startsWith("Info Version not found")));
            }

            @Test
            @DisplayName("Validate info version -> max lenght error")
            public void maxLengthError()
            {
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
                var product = Mockito.mock(Product.class);
                var info = Mockito.mock(Info.class);

                when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);
                when(info.getVersion()).thenReturn(StringUtils.repeat("x", Constants.MAX_NAME_LENGTH + 1));

                List<String> ret = asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType);
                assertTrue(ret.size() > 1);
                assertTrue(ret.stream().anyMatch(e -> e.startsWith("Too long Info Version")));
            }

            @ParameterizedTest(name = "[{index}] version: {0}")
            @DisplayName("Validate info version -> invalid format error")
            @ValueSource(strings = {"1.2", "1.0.beta", "1.12.1254"})
            void invalidFormatError(String version)
            {
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
                var product = Mockito.mock(Product.class);
                var info = Mockito.mock(Info.class);

                when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);
                when(info.getVersion()).thenReturn(version);

                List<String> ret = asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType);
                assertTrue(ret.size() > 1);
                assertTrue(ret.stream().anyMatch(e -> e.startsWith("Invalid format Info Version")));
            }
        }

        @Test
        @DisplayName("Validate info -> OK")
        void ok()
        {
            var uuaa = "JGMV";
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
            var product = Mockito.mock(Product.class);
            var info = Mockito.mock(Info.class);
            var contact = Mockito.mock(Contact.class);

            when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);
            when(info.getTitle()).thenReturn("title");
            when(info.getDescription()).thenReturn("description");
            when(info.getDescription()).thenReturn("description");
            when(info.getVersion()).thenReturn("1.0.0");
            when(novaAsyncAPI.getXBusinessUnit()).thenReturn(uuaa);
            when(product.getUuaa()).thenReturn(uuaa);
            when(product.getName()).thenReturn(productName);
            when(product.getId()).thenReturn(productId);
            when(product.getEmail()).thenReturn(productEmail);
            when(info.getContact()).thenReturn(contact);
            fillValidContact(contact);

            List<String> ret = asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType);

            assertEquals(0, ret.size(), "There are errors on validation: " + ret);
        }
    }

    @Nested
    class ValidateForbiddenNodes
    {
        @Test
        @DisplayName("Validate forbidden nodes -> error")
        void error()
        {
            var asyncAPI = Mockito.mock(AsyncAPI.class);
            var servers = new HashMap<String, Server>();
            servers.put("xxx", new Server());

            when(asyncAPI.getServers()).thenReturn(servers);

            List<String> ret = asyncApiValidationUtils.validateForbiddenNodes(asyncAPI);

            assertEquals(1, ret.size());
            assertTrue(ret.get(0).startsWith("AsyncApi servers node is forbidden"));

        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Validate forbidden nodes -> ok")
        void ok(Map<String, Server> servers)
        {
            var asyncAPI = Mockito.mock(AsyncAPI.class);

            when(asyncAPI.getServers()).thenReturn(servers);

            List<String> ret = asyncApiValidationUtils.validateForbiddenNodes(asyncAPI);

            assertEquals(0, ret.size());

        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_METHOD)
    class IsContactValid
    {
        @Test
        @DisplayName("(IsContactValid) -> Contact blank")
        void testParseAndValidateContactEmpty()
        {
            // given
            var uuaa = "JGMV";
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
            var product = Mockito.mock(Product.class);
            var info = Mockito.mock(Info.class);
            var apiType = ApiType.GOVERNED;
            var contact = Mockito.mock(Contact.class);


            // when
            when(product.getName()).thenReturn(productName);
            when(product.getId()).thenReturn(productId);
            when(product.getEmail()).thenReturn(productEmail);
            when(product.getUuaa()).thenReturn(uuaa);

            when(info.getContact()).thenReturn(contact);
            when(info.getTitle()).thenReturn("title");
            when(info.getDescription()).thenReturn("description");
            when(info.getDescription()).thenReturn("description");
            when(info.getVersion()).thenReturn("1.0.0");

            when(novaAsyncAPI.getXBusinessUnit()).thenReturn(uuaa);
            when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);

            // then
            var definitionFileException = Assertions.assertDoesNotThrow(() -> asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType));
            Assertions.assertEquals(3, definitionFileException.size(), "We have more than three error: " + definitionFileException);
        }

        @Test
        @DisplayName("(IsContactValid) -> Contact null")
        void testParseAndValidateContactNull()
        {

            // given
            var uuaa = "JGMV";
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
            var product = Mockito.mock(Product.class);
            var info = Mockito.mock(Info.class);
            var apiType = ApiType.GOVERNED;

            // when
            when(product.getName()).thenReturn(productName);
            when(product.getId()).thenReturn(productId);
            when(product.getEmail()).thenReturn(productEmail);
            when(product.getUuaa()).thenReturn(uuaa);

            when(info.getTitle()).thenReturn("title");
            when(info.getDescription()).thenReturn("description");
            when(info.getDescription()).thenReturn("description");
            when(info.getVersion()).thenReturn("1.0.0");

            when(novaAsyncAPI.getXBusinessUnit()).thenReturn(uuaa);
            when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);

            // then
            var definitionFileException = Assertions.assertDoesNotThrow(() -> asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType));
            Assertions.assertEquals(1, definitionFileException.size(), "We have more than one error: " + definitionFileException);
            Assertions.assertTrue(definitionFileException.contains("Swagger error: Info -> Contact not found. Please add the contact field to the info field and fulfill it."));
        }

        @Test
        @DisplayName("(IsContactValid) -> Contact with only email filled")
        void testParseAndValidateContactOnlyEmail()
        {

            // given
            var uuaa = "JGMV";
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
            var product = Mockito.mock(Product.class);
            var info = Mockito.mock(Info.class);
            var apiType = ApiType.GOVERNED;
            var contact = Mockito.mock(Contact.class);


            // when
            when(product.getName()).thenReturn(productName);
            when(product.getId()).thenReturn(productId);
            when(product.getEmail()).thenReturn(productEmail);
            when(product.getUuaa()).thenReturn(uuaa);

            when(info.getContact()).thenReturn(contact);
            when(info.getTitle()).thenReturn("title");
            when(info.getDescription()).thenReturn("description");
            when(info.getDescription()).thenReturn("description");
            when(info.getVersion()).thenReturn("1.0.0");

            when(contact.getEmail()).thenReturn(productEmail);

            when(novaAsyncAPI.getXBusinessUnit()).thenReturn(uuaa);
            when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);

            // then
            var definitionFileException = Assertions.assertDoesNotThrow(() -> asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType));
            Assertions.assertEquals(2, definitionFileException.size(), "We have more than two errors: " + definitionFileException);
        }

        @Test
        @DisplayName("(IsContactValid) -> Contact with a invalid URL")
        void testParseAndValidateContactWithInvalidURL()
        {
            // given
            var uuaa = "JGMV";
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
            var product = Mockito.mock(Product.class);
            var info = Mockito.mock(Info.class);
            var apiType = ApiType.GOVERNED;
            var contact = Mockito.mock(Contact.class);


            // when
            when(product.getName()).thenReturn(productName);
            when(product.getId()).thenReturn(productId);
            when(product.getEmail()).thenReturn(productEmail);
            when(product.getUuaa()).thenReturn(uuaa);

            when(info.getContact()).thenReturn(contact);
            when(info.getTitle()).thenReturn("title");
            when(info.getDescription()).thenReturn("description");
            when(info.getDescription()).thenReturn("description");
            when(info.getVersion()).thenReturn("1.0.0");

            when(contact.getEmail()).thenReturn(productEmail);
            when(contact.getName()).thenReturn(productName);
            when(contact.getUrl()).thenReturn("invalidURL");

            when(novaAsyncAPI.getXBusinessUnit()).thenReturn(uuaa);
            when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);

            // then
            var definitionFileException = Assertions.assertDoesNotThrow(() -> asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType));
            Assertions.assertEquals(1, definitionFileException.size(), "We have more than one error: " + definitionFileException);
        }

        @Test
        @DisplayName("(IsContactValid) -> Contact with only name filled")
        void testParseAndValidateContactOnlyName()
        {
            // given
            var uuaa = "JGMV";
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
            var product = Mockito.mock(Product.class);
            var info = Mockito.mock(Info.class);
            var apiType = ApiType.GOVERNED;
            var contact = Mockito.mock(Contact.class);


            // when
            when(product.getName()).thenReturn(productName);
            when(product.getId()).thenReturn(productId);
            when(product.getEmail()).thenReturn(productEmail);
            when(product.getUuaa()).thenReturn(uuaa);

            when(info.getContact()).thenReturn(contact);
            when(info.getTitle()).thenReturn("title");
            when(info.getDescription()).thenReturn("description");
            when(info.getDescription()).thenReturn("description");
            when(info.getVersion()).thenReturn("1.0.0");

            when(contact.getName()).thenReturn(productName);

            when(novaAsyncAPI.getXBusinessUnit()).thenReturn(uuaa);
            when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);

            // then
            var definitionFileException = Assertions.assertDoesNotThrow(() -> asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType));
            Assertions.assertEquals(2, definitionFileException.size(), "We have more than two errors: " + definitionFileException);
        }

        @Test
        @DisplayName("(IsContactValid) -> ok")
        void testParseAndValidateContact() throws IOException
        {
            // given
            var uuaa = "JGMV";
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
            var product = Mockito.mock(Product.class);
            var info = Mockito.mock(Info.class);
            var apiType = ApiType.GOVERNED;
            var contact = Mockito.mock(Contact.class);


            // when
            when(product.getName()).thenReturn(productName);
            when(product.getId()).thenReturn(productId);
            when(product.getEmail()).thenReturn(productEmail);
            when(product.getUuaa()).thenReturn(uuaa);

            when(info.getContact()).thenReturn(contact);
            when(info.getTitle()).thenReturn("title");
            when(info.getDescription()).thenReturn("description");
            when(info.getDescription()).thenReturn("description");
            when(info.getVersion()).thenReturn("1.0.0");

            when(contact.getName()).thenReturn(productName);
            when(contact.getEmail()).thenReturn(productEmail);
            when(contact.getUrl()).thenReturn("http://url/#/products/product/1");

            when(novaAsyncAPI.getXBusinessUnit()).thenReturn(uuaa);
            when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);

            // then
            var definitionFileException = Assertions.assertDoesNotThrow(() -> asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType));
            Assertions.assertTrue(definitionFileException.isEmpty(), "We have errors: " + definitionFileException);
        }

        @Test
        @DisplayName("(IsContactValid) -> Valid External Contact")
        void testParseAndValidateContactExternalAPI()
        {
            // given
            var uuaa = "JGMV";
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
            var product = Mockito.mock(Product.class);
            var info = Mockito.mock(Info.class);
            var apiType = ApiType.EXTERNAL;
            var contact = Mockito.mock(Contact.class);


            // when
            when(product.getName()).thenReturn(productName);
            when(product.getId()).thenReturn(productId);
            when(product.getEmail()).thenReturn(productEmail);
            when(product.getUuaa()).thenReturn(uuaa);

            when(info.getContact()).thenReturn(contact);
            when(info.getTitle()).thenReturn("title");
            when(info.getDescription()).thenReturn("description");
            when(info.getDescription()).thenReturn("description");
            when(info.getVersion()).thenReturn("1.0.0");

            when(contact.getName()).thenReturn(productName);
            when(contact.getEmail()).thenReturn(productEmail);
            when(contact.getUrl()).thenReturn("http://www.google.es");

            when(novaAsyncAPI.getXBusinessUnit()).thenReturn(uuaa);
            when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);

            // then
            var definitionFileException = Assertions.assertDoesNotThrow(() -> asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType));
            Assertions.assertTrue(definitionFileException.isEmpty(), "We have errors: " + definitionFileException);
        }

        @Test
        @DisplayName("(IsContactValid) -> Invalid External URL Contact")
        void testParseAndValidateInvalidContactExternalAPI()
        {

            // given
            var uuaa = "JGMV";
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class, RETURNS_DEEP_STUBS);
            var product = Mockito.mock(Product.class);
            var info = Mockito.mock(Info.class);
            var apiType = ApiType.GOVERNED;
            var contact = Mockito.mock(Contact.class);


            // when
            when(product.getName()).thenReturn(productName);
            when(product.getId()).thenReturn(productId);
            when(product.getEmail()).thenReturn(productEmail);
            when(product.getUuaa()).thenReturn(uuaa);

            when(info.getContact()).thenReturn(contact);
            when(info.getTitle()).thenReturn("title");
            when(info.getDescription()).thenReturn("description");
            when(info.getDescription()).thenReturn("description");
            when(info.getVersion()).thenReturn("1.0.0");

            when(contact.getName()).thenReturn(productName);
            when(contact.getEmail()).thenReturn(productEmail);
            when(contact.getUrl()).thenReturn("invalidURL");

            when(novaAsyncAPI.getXBusinessUnit()).thenReturn(uuaa);
            when(novaAsyncAPI.getAsyncAPI().getInfo()).thenReturn(info);

            // then
            var definitionFileException = Assertions.assertDoesNotThrow(() -> asyncApiValidationUtils.validateInfo(novaAsyncAPI, product, apiType));
            Assertions.assertEquals(1, definitionFileException.size(), "We have more than one error: " + definitionFileException);
        }
    }

    /**
     * Fill with valid Contact info
     *
     * @param contact mock associated to the contact field
     */
    private static void fillValidContact(Contact contact)
    {
        when(contact.getEmail()).thenReturn("swagger@bbva.com");
        when(contact.getName()).thenReturn("product");
        when(contact.getUrl()).thenReturn("http://url/#/products/product/1");
    }

}
