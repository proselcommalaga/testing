package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.asyncapi.v2.model.AsyncAPI;
import com.asyncapi.v2.model.Reference;
import com.asyncapi.v2.model.channel.ChannelItem;
import com.asyncapi.v2.model.channel.message.Message;
import com.asyncapi.v2.model.channel.operation.Operation;
import com.asyncapi.v2.model.component.Components;
import com.asyncapi.v2.model.schema.Schema;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.NovaCoreTestUtils;
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

import static com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal.AsyncBackToBackDefinitionFileValidatorModalityBasedImpl.COMPONENTS_MESSAGES_REF;
import static com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal.AsyncBackToBackDefinitionFileValidatorModalityBasedImpl.ERROR_HEAD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AsyncBackToBackDefinitionFileValidatorModalityBasedImplTest
{
    @Mock
    private AsyncApiValidationUtils asyncApiValidationUtils;
    @InjectMocks
    private AsyncBackToBackDefinitionFileValidatorModalityBasedImpl asyncBackToBackDefinitionFileValidatorModalityBased;

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
                    () -> asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType)
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
                    asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
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
                    asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
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
                asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
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
                asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
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
                    asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                }
                catch (DefinitionFileException ex)
                {
                    exception = ex;
                }

                assertNotNull(exception);
                assertEquals(1, exception.getErrorList().size());
                assertTrue(exception.getErrorList().get(0).startsWith(ERROR_HEAD+ "Definition must contain one, and only one channel"));
                verify(asyncApiValidationUtils).parseDefinitionFile(content);
                verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
            }

            @ParameterizedTest(name = "[{index}] apiType: {0}")
            @EnumSource(ApiType.class)
            @DisplayName("Parse and validate -> no unique channel validation error")
            void noUniqueChannelError(ApiType apiType) throws DefinitionFileException
            {
                var product = Mockito.mock(Product.class);
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                var asyncAPI = Mockito.mock(AsyncAPI.class);
                var components = buildComponents();
                var channels = new HashMap<String, ChannelItem>();
                channels.put("channel1", new ChannelItem());
                channels.put("channel2", new ChannelItem());

                when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                when(asyncAPI.getChannels()).thenReturn(channels);
                when(asyncAPI.getComponents()).thenReturn(components);

                when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                DefinitionFileException exception = null;
                try
                {
                    asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                }
                catch (DefinitionFileException ex)
                {
                    exception = ex;
                }

                assertNotNull(exception);
                assertTrue(exception.getErrorList().size() > 1);
                assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith(ERROR_HEAD+ "Definition must contain one")));
                verify(asyncApiValidationUtils).parseDefinitionFile(content);
                verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
            }

            @ParameterizedTest(name = "[{index}] apiType: {0}")
            @EnumSource(ApiType.class)
            @DisplayName("Parse and validate -> bidirectional channel validation error")
            void bidirectionalChannelError(ApiType apiType) throws DefinitionFileException
            {
                var product = Mockito.mock(Product.class);
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                var asyncAPI = Mockito.mock(AsyncAPI.class);
                var components = buildComponents();
                var channel = new ChannelItem();
                channel.setPublish(new Operation());
                channel.setSubscribe(new Operation());
                var channels = new HashMap<String, ChannelItem>();
                channels.put("channel1", new ChannelItem());

                when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                when(asyncAPI.getChannels()).thenReturn(channels);
                when(asyncAPI.getComponents()).thenReturn(components);

                when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                DefinitionFileException exception = null;
                try
                {
                    asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                }
                catch (DefinitionFileException ex)
                {
                    exception = ex;
                }

                assertNotNull(exception);
                assertTrue(exception.getErrorList().size() > 1);
                assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith(ERROR_HEAD+ "Channel must be only a")));
                verify(asyncApiValidationUtils).parseDefinitionFile(content);
                verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
            }

            @ParameterizedTest(name = "[{index}] apiType: {0}")
            @EnumSource(ApiType.class)
            @DisplayName("Parse and validate -> empty channel operation validation error")
            void emptyChannelOperationError(ApiType apiType) throws DefinitionFileException
            {
                var product = Mockito.mock(Product.class);
                var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                var asyncAPI = Mockito.mock(AsyncAPI.class);
                var components = buildComponents();
                var channel = new ChannelItem();
                channel.setPublish(new Operation());
                var channels = new HashMap<String, ChannelItem>();
                channels.put("channel1", new ChannelItem());

                when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                when(asyncAPI.getChannels()).thenReturn(channels);
                when(asyncAPI.getComponents()).thenReturn(components);

                when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
                when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                DefinitionFileException exception = null;
                try
                {
                    asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                }
                catch (DefinitionFileException ex)
                {
                    exception = ex;
                }

                assertNotNull(exception);
                assertTrue(exception.getErrorList().size() > 1);
                assertTrue(exception.getErrorList().stream().anyMatch(error -> error.startsWith(ERROR_HEAD+ "Only one operationId is")));
                verify(asyncApiValidationUtils).parseDefinitionFile(content);
                verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
            }

            @Nested
            class ChannelsMessage
            {
                String channelMessageName = "channelMessageName";
                String schemaObjectName = "schemaObjectName";

                @Nested
                class ComponentsValidation
                {
                    @ParameterizedTest(name = "[{index}] apiType: {0}")
                    @EnumSource(ApiType.class)
                    @DisplayName("Parse and validate -> null components validation error")
                    void nullComponentsError(ApiType apiType) throws DefinitionFileException
                    {
                        var product = Mockito.mock(Product.class);
                        var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                        var asyncAPI = Mockito.mock(AsyncAPI.class);
                        var channels = buildChannels();

                        when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                        when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                        when(asyncAPI.getChannels()).thenReturn(channels);
                        when(asyncAPI.getComponents()).thenReturn(null);

                        when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                        when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                        when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
                        when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                        DefinitionFileException exception = null;
                        try
                        {
                            asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                        }
                        catch (DefinitionFileException ex)
                        {
                            exception = ex;
                        }

                        assertNotNull(exception);
                        assertEquals(1, exception.getErrorList().size());
                        assertTrue(exception.getErrorList().get(0).startsWith(ERROR_HEAD + "Definition must contain one component"));
                        verify(asyncApiValidationUtils).parseDefinitionFile(content);
                        verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                        verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                        verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
                    }

                    @ParameterizedTest(name = "[{index}] apiType: {0}")
                    @EnumSource(ApiType.class)
                    @DisplayName("Parse and validate -> empty components validation error")
                    void emptyComponentsError(ApiType apiType) throws DefinitionFileException
                    {
                        var product = Mockito.mock(Product.class);
                        var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                        var asyncAPI = Mockito.mock(AsyncAPI.class);
                        var components = Mockito.mock(Components.class);
                        var channels = buildChannels();

                        when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                        when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                        when(asyncAPI.getChannels()).thenReturn(channels);
                        when(asyncAPI.getComponents()).thenReturn(components);
                        when(components.getSchemas()).thenReturn(null);

                        when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                        when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                        when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
                        when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                        DefinitionFileException exception = null;
                        try
                        {
                            asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                        }
                        catch (DefinitionFileException ex)
                        {
                            exception = ex;
                        }

                        assertNotNull(exception);
                        assertEquals(1, exception.getErrorList().size());
                        assertTrue(exception.getErrorList().get(0).startsWith(ERROR_HEAD + "Definition must contain one component"));
                        verify(asyncApiValidationUtils).parseDefinitionFile(content);
                        verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                        verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                        verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
                    }

                    @ParameterizedTest(name = "[{index}] apiType: {0}")
                    @EnumSource(ApiType.class)
                    @DisplayName("Parse and validate -> empty schema components validation error")
                    void emptySchemaComponentsError(ApiType apiType) throws DefinitionFileException
                    {
                        var product = Mockito.mock(Product.class);
                        var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                        var asyncAPI = Mockito.mock(AsyncAPI.class);
                        var components = Mockito.mock(Components.class);
                        var channels = buildChannels();

                        when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                        when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                        when(asyncAPI.getChannels()).thenReturn(channels);
                        when(asyncAPI.getComponents()).thenReturn(components);
                        when(components.getSchemas()).thenReturn(Collections.emptyMap());

                        when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                        when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                        when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
                        when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                        DefinitionFileException exception = null;
                        try
                        {
                            asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                        }
                        catch (DefinitionFileException ex)
                        {
                            exception = ex;
                        }

                        assertNotNull(exception);
                        assertEquals(1, exception.getErrorList().size());
                        assertTrue(exception.getErrorList().get(0).startsWith(ERROR_HEAD + "Definition must contain one component"));
                        verify(asyncApiValidationUtils).parseDefinitionFile(content);
                        verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                        verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                        verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
                    }
                }


                @Nested
                class ChannelRef
                {
                    @ParameterizedTest(name = "[{index}] apiType: {0}")
                    @EnumSource(ApiType.class)
                    @DisplayName("Parse and validate -> operation message (channels.<channel>.<operation>.<message>) ref does not match with any message definition")
                    void operationMessageRefError(ApiType apiType) throws DefinitionFileException
                    {
                        var product = Mockito.mock(Product.class);
                        var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                        var asyncAPI = Mockito.mock(AsyncAPI.class);
                        var channels = buildChannels();

                        var schema = new Schema();
                        var schemas = new HashMap<String, Object>();
                        schemas.put(schemaObjectName, schema);

                        var messagePayload = new Schema();
                        messagePayload.setRef("#/components/schemas/" + schemaObjectName);
                        var message = new Message();
                        message.setPayload(messagePayload);
                        var messages = new HashMap<String, Object>();
                        messages.put(channelMessageName + "DIFFERENT", message);

                        var components = new Components();
                        components.setSchemas(schemas);
                        components.setMessages(messages);


                        when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                        when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                        when(asyncAPI.getChannels()).thenReturn(channels);
                        when(asyncAPI.getComponents()).thenReturn(components);

                        when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                        when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                        when(asyncApiValidationUtils.validateInfo(any(), any(),any())).thenReturn(Collections.emptyList());
                        when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                        DefinitionFileException exception = null;
                        try
                        {
                            asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                        }
                        catch (DefinitionFileException ex)
                        {
                            exception = ex;
                        }

                        assertNotNull(exception);
                        assertEquals(1, exception.getErrorList().size());
                        assertTrue(exception.getErrorList().get(0).startsWith(ERROR_HEAD + "Channel message reference"));
                        verify(asyncApiValidationUtils).parseDefinitionFile(content);
                        verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                        verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                        verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
                    }

                    @ParameterizedTest(name = "[{index}] apiType: {0}")
                    @EnumSource(ApiType.class)
                    @DisplayName("Parse and validate -> message definition (components.messages) ref does not match with any schema definition")
                    void messageDefinitionRefError(ApiType apiType) throws DefinitionFileException
                    {
                        var product = Mockito.mock(Product.class);
                        var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                        var asyncAPI = Mockito.mock(AsyncAPI.class);
                        var channels = buildChannels();

                        var schema = new Schema();
                        var schemas = new HashMap<String, Object>();
                        schemas.put(schemaObjectName, schema);

                        var messagePayload = new Schema();
                        messagePayload.setRef("#/components/schemas/" + schemaObjectName + "DIFFERENT");
                        var message = new Message();
                        message.setPayload(messagePayload);
                        var messages = new HashMap<String, Object>();
                        messages.put(channelMessageName, message);

                        var components = new Components();
                        components.setSchemas(schemas);
                        components.setMessages(messages);

                        when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                        when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                        when(asyncAPI.getChannels()).thenReturn(channels);
                        when(asyncAPI.getComponents()).thenReturn(components);

                        when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                        when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                        when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
                        when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                        DefinitionFileException exception = null;
                        try
                        {
                            asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                        }
                        catch (DefinitionFileException ex)
                        {
                            exception = ex;
                        }

                        assertNotNull(exception);
                        assertEquals(1, exception.getErrorList().size());
                        assertTrue(exception.getErrorList().get(0).startsWith(ERROR_HEAD + "components.messages payload"));
                        verify(asyncApiValidationUtils).parseDefinitionFile(content);
                        verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                        verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                        verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
                    }

                    @ParameterizedTest(name = "[{index}] apiType: {0}")
                    @EnumSource(ApiType.class)
                    @DisplayName("Parse and validate -> message definition ref OK")
                    void ok(ApiType apiType) throws DefinitionFileException
                    {
                        var product = Mockito.mock(Product.class);
                        var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                        var asyncAPI = Mockito.mock(AsyncAPI.class);
                        var channels = buildChannels();

                        var schema = new Schema();
                        var schemas = new HashMap<String, Object>();
                        schemas.put(schemaObjectName, schema);

                        var messagePayload = new Schema();
                        messagePayload.setRef("#/components/schemas/" + schemaObjectName);
                        var message = new Message();
                        message.setPayload(messagePayload);
                        var messages = new HashMap<String, Object>();
                        messages.put(channelMessageName, message);

                        var components = new Components();
                        components.setSchemas(schemas);
                        components.setMessages(messages);

                        when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                        when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                        when(asyncAPI.getChannels()).thenReturn(channels);
                        when(asyncAPI.getComponents()).thenReturn(components);

                        when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                        when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                        when(asyncApiValidationUtils.validateInfo(any(), any(), any())).thenReturn(Collections.emptyList());
                        when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                        NovaAsyncAPI ret = asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);

                        assertEquals(novaAsyncAPI, ret);
                        verify(asyncApiValidationUtils).parseDefinitionFile(content);
                        verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                        verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                        verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
                    }
                }

                @ParameterizedTest(name = "[{index}] apiType: {0}")
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> multiple messages definition error")
                void multipleMessagesDefinitionError(ApiType apiType) throws DefinitionFileException
                {
                    var product = Mockito.mock(Product.class);
                    var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                    var asyncAPI = Mockito.mock(AsyncAPI.class);
                    var channels = buildValidChannels();
                    var components = buildComponents();
                    var messages = new HashMap<String, Object>();
                    messages.put("message1", new Message());
                    messages.put("message2", new Message());
                    components.setMessages(messages);

                    when(novaAsyncAPI.getAsyncAPI()).thenReturn(asyncAPI);
                    when(asyncAPI.getAsyncapi()).thenReturn(apiVersionRaw);
                    when(asyncAPI.getChannels()).thenReturn(channels);
                    when(asyncAPI.getComponents()).thenReturn(components);

                    when(asyncApiValidationUtils.parseDefinitionFile(any())).thenReturn(novaAsyncAPI);
                    when(asyncApiValidationUtils.validateAsyncSpecVersion(any())).thenReturn(Collections.emptyList());
                    when(asyncApiValidationUtils.validateInfo(any(), any(),any())).thenReturn(Collections.emptyList());
                    when(asyncApiValidationUtils.validateForbiddenNodes(any())).thenReturn(Collections.emptyList());

                    DefinitionFileException exception = null;
                    try
                    {
                        asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertEquals(1, exception.getErrorList().size());
                    assertTrue(exception.getErrorList().get(0).startsWith(ERROR_HEAD+ "Definition must have only one"));
                    verify(asyncApiValidationUtils).parseDefinitionFile(content);
                    verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                    verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                    verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
                }

                @ParameterizedTest(name = "[{index}] apiType: {0}")
                @EnumSource(ApiType.class)
                @DisplayName("Parse and validate -> channel operation refers to non exists message definition")
                void operationRefersToNonExistsMessageError(ApiType apiType) throws DefinitionFileException
                {
                    var product = Mockito.mock(Product.class);
                    var novaAsyncAPI = Mockito.mock(NovaAsyncAPI.class);
                    var asyncAPI = Mockito.mock(AsyncAPI.class);
                    var components = buildComponents();

                    var operation = new Operation();
                    operation.setOperationId("operation");
                    operation.setMessage(new Reference());
                    var channel = new ChannelItem();
                    channel.setPublish(operation);
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

                    DefinitionFileException exception = null;
                    try
                    {
                        asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);
                    }
                    catch (DefinitionFileException ex)
                    {
                        exception = ex;
                    }

                    assertNotNull(exception);
                    assertEquals(1, exception.getErrorList().size());
                    assertTrue(exception.getErrorList().get(0).startsWith(ERROR_HEAD+ "channels.<channelName>.<publish|subscribe>.message must"));
                    verify(asyncApiValidationUtils).parseDefinitionFile(content);
                    verify(asyncApiValidationUtils).validateAsyncSpecVersion(apiVersionRaw);
                    verify(asyncApiValidationUtils).validateInfo(novaAsyncAPI, product, apiType);
                    verify(asyncApiValidationUtils).validateForbiddenNodes(asyncAPI);
                }

                private Map<String, ChannelItem> buildChannels()
                {
                    var operation = new Operation();
                    operation.setOperationId("operation");
                    operation.setMessage(new Reference(COMPONENTS_MESSAGES_REF + channelMessageName));
                    var channel = new ChannelItem();
                    channel.setPublish(operation);
                    var channels = new HashMap<String, ChannelItem>();
                    channels.put("channel1", channel);

                    return channels;
                }
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
                    () -> asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType)
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

            NovaAsyncAPI ret = asyncBackToBackDefinitionFileValidatorModalityBased.parseAndValidate(content, product, apiType);

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
            var operation = new Operation();
            operation.setOperationId("operation");
            operation.setMessage("message");
            var channel = new ChannelItem();
            channel.setPublish(operation);
            var channels = new HashMap<String, ChannelItem>();
            channels.put("channel1", channel);

            return channels;
        }

    }

    @Nested
    class IsModalitySupported
    {
        @Test
        @DisplayName("Is modality supported -> ASYNC_BACKTOBACK")
        void backToBack()
        {
            assertTrue(asyncBackToBackDefinitionFileValidatorModalityBased.isModalitySupported(ApiModality.ASYNC_BACKTOBACK));
        }

        @ParameterizedTest
        @EnumSource(value = ApiModality.class, names = {"SYNC", "ASYNC_BACKTOFRONT"})
        @DisplayName("Is modality supported -> false")
        void others(ApiModality modality)
        {
            assertFalse(asyncBackToBackDefinitionFileValidatorModalityBased.isModalitySupported(modality));
        }
    }
}
