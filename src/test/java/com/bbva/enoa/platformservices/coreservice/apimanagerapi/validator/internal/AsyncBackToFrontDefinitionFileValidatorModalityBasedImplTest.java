package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.asyncapi.v2.model.AsyncAPI;
import com.asyncapi.v2.model.channel.ChannelItem;
import com.asyncapi.v2.model.channel.operation.Operation;
import com.asyncapi.v2.model.component.Components;
import com.asyncapi.v2.model.schema.Schema;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.DefinitionFileException;
import com.bbva.enoa.platformservices.coreservice.common.model.NovaAsyncAPI;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal.AsyncBackToFrontDefinitionFileValidatorModalityBasedImpl.ERROR_HEAD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AsyncBackToFrontDefinitionFileValidatorModalityBasedImplTest
{
    @Mock
    private AsyncApiValidationUtils asyncApiValidationUtils;
    @InjectMocks
    private AsyncBackToFrontDefinitionFileValidatorModalityBasedImpl asyncBackToFrontDefinitionFileValidatorModalityBased;

    @BeforeEach
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @AfterEach
    public void verifyAllMocks()
    {
        verifyNoMoreInteractions(
                asyncApiValidationUtils
        );
    }

    @Nested
    class ParseAndValidate
    {
        String content = "content";
        String apiVersionRaw = "1.0.0";
        String errorMsg = "errorMsg";

        @ParameterizedTest
        @EnumSource(ApiType.class)
        @DisplayName("Parse and validate -> definition file exception")
        void yamlDefinitionError(ApiType apiType) throws DefinitionFileException
        {
            var product = Mockito.mock(Product.class);
            when(asyncApiValidationUtils.parseDefinitionFile(any())).thenThrow(DefinitionFileException.class);

            assertThrows(
                    DefinitionFileException.class,
                    () -> asyncBackToFrontDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType)
            );

            verify(asyncApiValidationUtils).parseDefinitionFile(content);
        }

        @Nested
        class EmptyDefinitionError
        {
            @ParameterizedTest
            @EnumSource(ApiType.class)
            @DisplayName("Parse and validate -> definition file empty: null definition")
            void nullDefinition(ApiType apiType) throws DefinitionFileException
            {
                var product = Mockito.mock(Product.class);
                when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(null);

                DefinitionFileException exception = null;
                try
                {
                    asyncBackToFrontDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                }
                catch (DefinitionFileException ex)
                {
                    exception = ex;
                }

                assertNotNull(exception);
                assertEquals(1, exception.getErrorList().size());
                assertTrue(exception.getErrorList().get(0).startsWith(ERROR_HEAD+ "parse error: the yml content"));
                verify(asyncApiValidationUtils).parseDefinitionFile(content);
            }

            @ParameterizedTest
            @EnumSource(ApiType.class)
            @DisplayName("Parse and validate -> definition file empty: null async API definition")
            void nullAsyncDefinition(ApiType apiType) throws DefinitionFileException
            {
                var product = Mockito.mock(Product.class);
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);

                when(novaAsyncAPI.getAsyncAPI()).thenReturn(null);

                when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);

                DefinitionFileException exception = null;
                try
                {
                    asyncBackToFrontDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                }
                catch (DefinitionFileException ex)
                {
                    exception = ex;
                }

                assertNotNull(exception);
                assertEquals(1, exception.getErrorList().size());
                assertTrue(exception.getErrorList().get(0).startsWith(ERROR_HEAD+ "parse error: the yml content"));
                verify(asyncApiValidationUtils).parseDefinitionFile(content);
            }
        }

        @ParameterizedTest(name = "[{index}] apiType: {0}")
        @EnumSource(ApiType.class)
        @DisplayName("Parse and validate -> API version validation error")
        void apiVersionError(ApiType apiType) throws DefinitionFileException
        {
            var product = Mockito.mock(Product.class);
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
            var asyncAPI = Mockito.mock(AsyncAPI.class);
            var channels = buildValidChannels();
            var components = buildComponents();

            when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
            when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
            when(asyncAPI.getChannels()).thenReturn(channels);
            when(asyncAPI.getComponents()).thenReturn(components);

            when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
            when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(List.of(errorMsg));
            when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
            when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

            DefinitionFileException exception = null;
            try
            {
                asyncBackToFrontDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
            }
            catch (DefinitionFileException ex)
            {
                exception = ex;
            }

            assertNotNull(exception);
            assertEquals(1, exception.getErrorList().size());
            assertEquals(ERROR_HEAD+ errorMsg, exception.getErrorList().get(0));
            verify(asyncApiValidationUtils).parseDefinitionFile(content);
            verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
            verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
            verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
        }


        @ParameterizedTest(name = "[{index}] apiType: {0}")
        @EnumSource(ApiType.class)
        @DisplayName("Parse and validate -> info validation error")
        void infoError(ApiType apiType) throws DefinitionFileException
        {
            var product = Mockito.mock(Product.class);
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
            var asyncAPI = Mockito.mock(AsyncAPI.class);
            var channels = buildValidChannels();
            var components = buildComponents();

            when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
            when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
            when(asyncAPI.getChannels()).thenReturn(channels);
            when(asyncAPI.getComponents()).thenReturn(components);

            when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
            when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
            when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(List.of(errorMsg));
            when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

            DefinitionFileException exception = null;
            try
            {
                asyncBackToFrontDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
            }
            catch (DefinitionFileException ex)
            {
                exception = ex;
            }

            assertNotNull(exception);
            assertEquals(1, exception.getErrorList().size());
            assertEquals(ERROR_HEAD+ errorMsg, exception.getErrorList().get(0));
            verify(asyncApiValidationUtils).parseDefinitionFile(content);
            verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
            verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
            verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
        }

        @Nested
        class Channels
        {
            @ParameterizedTest(name = "[{index}] apiType: {0}")
            @EnumSource(ApiType.class)
            @DisplayName("Parse and validate -> null channels validation error")
            void nullChannelsError(ApiType apiType) throws DefinitionFileException
            {
                var product = Mockito.mock(Product.class);
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                var asyncAPI = Mockito.mock(AsyncAPI.class);
                var components = buildComponents();

                when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                when(asyncAPI.getChannels()).thenReturn(null);
                when(asyncAPI.getComponents()).thenReturn(components);

                when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                DefinitionFileException exception = null;
                try
                {
                    asyncBackToFrontDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                }
                catch (DefinitionFileException ex)
                {
                    exception = ex;
                }

                assertNotNull(exception);
                assertEquals(1, exception.getErrorList().size());
                assertTrue(exception.getErrorList().get(0).startsWith(ERROR_HEAD + "Definition must contain one or more channels"));
                verify(asyncApiValidationUtils).parseDefinitionFile(content);
                verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
            }

            @ParameterizedTest(name = "[{index}] apiType: {0}")
            @EnumSource(ApiType.class)
            @DisplayName("Parse and validate -> empty channels validation error")
            void emptyChannelsError(ApiType apiType) throws DefinitionFileException
            {
                var product = Mockito.mock(Product.class);
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                var asyncAPI = Mockito.mock(AsyncAPI.class);
                var components = buildComponents();

                when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                when(asyncAPI.getChannels()).thenReturn(Collections.emptyMap());
                when(asyncAPI.getComponents()).thenReturn(components);

                when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                DefinitionFileException exception = null;
                try
                {
                    asyncBackToFrontDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                }
                catch (DefinitionFileException ex)
                {
                    exception = ex;
                }

                assertNotNull(exception);
                assertEquals(1, exception.getErrorList().size());
                assertTrue(exception.getErrorList().get(0).startsWith(ERROR_HEAD+ "Definition must contain one or more channels"));
                verify(asyncApiValidationUtils).parseDefinitionFile(content);
                verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
            }

            @ParameterizedTest(name = "[{index}] apiType: {0}")
            @EnumSource(ApiType.class)
            @DisplayName("Parse and validate -> channel without subscriber operation OK")
            void onlyPublishOK(ApiType apiType) throws DefinitionFileException
            {
                var product = Mockito.mock(Product.class);
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                var asyncAPI = Mockito.mock(AsyncAPI.class);
                var components = buildComponents();

                var channel = new ChannelItem();
                channel.setPublish(new Operation());
                var channels = new HashMap<String, ChannelItem>();
                channels.put("channel1", channel);

                when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                when(asyncAPI.getChannels()).thenReturn(channels);
                when(asyncAPI.getComponents()).thenReturn(components);

                when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                asyncBackToFrontDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);

                verify(asyncApiValidationUtils).parseDefinitionFile(content);
                verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
            }

            @ParameterizedTest(name = "[{index}] apiType: {0}")
            @EnumSource(ApiType.class)
            @DisplayName("Parse and validate -> channel without publisher operation OK")
            void onlySubscribeOK(ApiType apiType) throws DefinitionFileException
            {
                var product = Mockito.mock(Product.class);
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                var asyncAPI = Mockito.mock(AsyncAPI.class);
                var components = buildComponents();

                var channel = new ChannelItem();
                channel.setSubscribe(new Operation());
                var channels = new HashMap<String, ChannelItem>();
                channels.put("channel1", channel);

                when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                when(asyncAPI.getChannels()).thenReturn(channels);
                when(asyncAPI.getComponents()).thenReturn(components);

                when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                asyncBackToFrontDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);

                verify(asyncApiValidationUtils).parseDefinitionFile(content);
                verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
            }

            @ParameterizedTest(name = "[{index}] apiType: {0}")
            @EnumSource(ApiType.class)
            @DisplayName("Parse and validate -> channels with subscriber and publisher OK")
            void withSubscriberAndPublisherOK(ApiType apiType) throws DefinitionFileException
            {
                var product = Mockito.mock(Product.class);
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                var asyncAPI = Mockito.mock(AsyncAPI.class);
                var components = buildComponents();

                var channel1 = new ChannelItem();
                channel1.setPublish(new Operation());
                channel1.setSubscribe(new Operation());
                var channel2 = new ChannelItem();
                channel2.setPublish(new Operation());
                channel2.setSubscribe(new Operation());
                var channels = new HashMap<String, ChannelItem>();
                channels.put("channel1", channel1);
                channels.put("channel2", channel2);

                when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                when(asyncAPI.getChannels()).thenReturn(channels);
                when(asyncAPI.getComponents()).thenReturn(components);

                when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                NovaAsyncAPI ret = asyncBackToFrontDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);

                assertEquals(novaAsyncAPI, ret);
                verify(asyncApiValidationUtils).parseDefinitionFile(content);
                verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
            }
        }

        @ParameterizedTest(name = "[{index}] apiType: {0}")
        @EnumSource(ApiType.class)
        @DisplayName("Parse and validate -> forbidden nodes validation error")
        void forbiddenNodesError(ApiType apiType) throws DefinitionFileException
        {
            var product = Mockito.mock(Product.class);
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
            var asyncAPI = Mockito.mock(AsyncAPI.class);
            var channels = buildValidChannels();
            var components = buildComponents();

            when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
            when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
            when(asyncAPI.getChannels()).thenReturn(channels);
            when(asyncAPI.getComponents()).thenReturn(components);

            when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
            when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
            when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
            when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(List.of(errorMsg));

            assertThrows(
                    DefinitionFileException.class,
                    () -> asyncBackToFrontDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType)
            );

            verify(asyncApiValidationUtils).parseDefinitionFile(content);
            verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
            verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
            verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
        }

        @ParameterizedTest(name = "[{index}] apiType: {0}")
        @EnumSource(ApiType.class)
        @DisplayName("Parse and validate -> OK")
        void ok(ApiType apiType) throws DefinitionFileException
        {
            var product = Mockito.mock(Product.class);
            var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
            var asyncAPI = Mockito.mock(AsyncAPI.class);
            var channels = buildValidChannels();
            var components = buildComponents();

            when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
            when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
            when(asyncAPI.getChannels()).thenReturn(channels);
            when(asyncAPI.getComponents()).thenReturn(components);

            when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
            when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
            when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
            when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

            NovaAsyncAPI ret = asyncBackToFrontDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);

            assertEquals(novaAsyncAPI, ret);
            verify(asyncApiValidationUtils).parseDefinitionFile(content);
            verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
            verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
            verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
        }

        private Components buildComponents()
        {
            var components = new Components();
            var schemas = new HashMap<String, Object>();
            schemas.put("objectName", new Schema());
            components.setSchemas(schemas);
            return components;
        }

        private Map<String, ChannelItem> buildValidChannels()
        {
            var operation1 = new Operation();
            operation1.setOperationId("operation");
            operation1.setMessage("message");
            var operation2 = new Operation();
            operation2.setOperationId("operation");
            operation2.setMessage("message");
            var channel = new ChannelItem();
            channel.setPublish(operation1);
            channel.setSubscribe(operation2);
            var channels = new HashMap<String, ChannelItem>();
            channels.put("channel1", channel);

            return channels;
        }
    }

    @Nested
    class IsModalitySupported
    {
        @Test
        @DisplayName("Is modality supported -> ASYNC_BACKTOFRONT")
        void backToFront()
        {
            assertTrue(asyncBackToFrontDefinitionFileValidatorModalityBased.isModalitySupported(ApiModality.ASYNC_BACKTOFRONT));
        }

        @ParameterizedTest
        @EnumSource(value = ApiModality.class, names = {"SYNC", "ASYNC_BACKTOBACK"})
        @DisplayName("Is modality supported -> false")
        void others(ApiModality modality)
        {
            assertFalse(asyncBackToFrontDefinitionFileValidatorModalityBased.isModalitySupported(modality));
        }
    }
}
