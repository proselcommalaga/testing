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
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.DefinitionFileException;
import com.bbva.enoa.platformservices.coreservice.common.model.NovaAsyncAPI;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class AsyncBackToBackDefinitionFileValidatorModalityBasedImpl implements IDefinitionFileValidatorModalityBased<NovaAsyncAPI>
{
    private static final Logger LOG = LoggerFactory.getLogger(AsyncBackToBackDefinitionFileValidatorModalityBasedImpl.class);
    protected static final String ERROR_HEAD = "BackToBack asyncapi yml definition -> ";
    protected static final String COMPONENTS_MESSAGES_REF = "#/components/messages/";
    protected static final Pattern COMPONENTS_SCHEME_REF_PATTERN = Pattern.compile("^#/components/schemas/(?<name>.*)$");
    private final AsyncApiValidationUtils asyncApiValidationUtils;

    @Override
    public NovaAsyncAPI parseAndValidate(final String content, final Product product, final ApiType apiType) throws DefinitionFileException
    {
        List<String> errorList = new ArrayList<>();
        NovaAsyncAPI asyncApi = this.asyncApiValidationUtils.parseDefinitionFile(content);

        // Validate the definition
        if (Objects.isNull(asyncApi) || Objects.isNull(asyncApi.getAsyncAPI()))
        {
            errorList.add("parse error: the yml content or yml file not found, "
                    + "yml definition is null. Please, ensure that the yml definition exists and it has contents.");
        }
        else
        {
            // validate asyncapi version
            errorList.addAll(this.asyncApiValidationUtils.validateAsyncSpecVersion(asyncApi.getAsyncAPI().getAsyncapi()));
            // Info field validation
            errorList.addAll(this.asyncApiValidationUtils.validateInfo(asyncApi, product, apiType));

            // Validate channel
            errorList.addAll(this.validateChannels(asyncApi));

            //Check forbidden nodes
            errorList.addAll(this.asyncApiValidationUtils.validateForbiddenNodes(asyncApi.getAsyncAPI()));
        }

        if (!errorList.isEmpty())
        {
            throw new DefinitionFileException(errorList.stream().map(msg -> ERROR_HEAD + msg).collect(Collectors.toList()));
        }
        else
        {
            return asyncApi;
        }
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.ASYNC_BACKTOBACK == modality;
    }

    private List<String> validateChannels(final NovaAsyncAPI novaAsyncAPI)
    {
        Map<String, ChannelItem> channels = novaAsyncAPI.getAsyncAPI().getChannels();
        List<String> errorList = new ArrayList<>();
        if (Objects.isNull(channels))
        {
            errorList.add("Definition must contain one, and only one channel. Channel not found.");
        }
        else
        {
            if (channels.size() != 1)
            {
                errorList.add("Definition must contain one, and only one channel. Channels found: " + channels.size());
            }

            if ((channels.values().stream().filter(c -> Objects.nonNull(c.getPublish())).count() +
                    channels.values().stream().filter(c -> Objects.nonNull(c.getSubscribe())).count()) != 1)
            {
                errorList.add("Channel must be only a publisher or a subscriber.");
            }

            if ((channels.values().stream().filter(c -> Objects.nonNull(c.getPublish()) && Objects.nonNull(c.getPublish().getOperationId())).count() +
                    channels.values().stream().filter(c -> Objects.nonNull(c.getSubscribe()) && Objects.nonNull(c.getSubscribe().getOperationId())).count()) != 1)
            {
                errorList.add("Only one operationId is allowed per channel.");
            }
        }
        if (errorList.isEmpty())
        {
            // Validate channel message with components.messages & schemas
            errorList.addAll(this.validateChannelMessage(novaAsyncAPI.getAsyncAPI()));
        }
        return errorList;
    }

    /**
     * Validate the refs of the message inside channel, components message and components schema
     *
     * @param asyncAPI
     */
    private List<String> validateChannelMessage(final AsyncAPI asyncAPI)
    {
        List<String> errorList = new ArrayList<>();
        Map<String, ChannelItem> channels = asyncAPI.getChannels();
        Components components = asyncAPI.getComponents();

        if (Objects.isNull(components) || Objects.isNull(components.getSchemas()) || components.getSchemas().size() < 1
        )
        {
            errorList.add("Definition must contain one component with at least one schema.");
        }
        else
        {
            // Because of the earlier tests, the channel have just one operation
            Operation operation = Stream.concat(
                            channels.values().stream().map(ChannelItem::getPublish),
                            channels.values().stream().map(ChannelItem::getSubscribe))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .get();

            //If components.messages exists, test the $ref in channels
            if (Objects.nonNull(components.getMessages()) && components.getMessages().size() == 1)
            {
                errorList.addAll(this.validateChannelRefWithComponents(operation, components));
            }
            else if (Objects.nonNull(components.getMessages()) && components.getMessages().size() != 1)
            {
                errorList.add("Definition must have only one component message.");
            }
            //If components.messages does not exist, test the message in channels can not be a reference
            else if (operation.getMessage() instanceof Reference)
            {
                errorList.add("channels.<channelName>.<publish|subscribe>.message must "
                        + "define a message when the components.messages is not defined.");
            }
            else
            {
                // It is not a must to define the message in channels with a $ref. If it is the case, skip the validation
                LOG.info("[AsyncBackToBackDefinitionFileValidatorModalityBasedImpl] -> [validateChannelMessage]: Skip validation: The channel message or the component.message "
                        + "payload are not referenced with #ref.");
            }
        }
        return errorList;
    }

    /**
     * At this point, the channel operation message is a #ref.
     * This method validates that the #ref channels.<channel>.<operation>.message.#ref match the components.messages.<message>,
     * and the components.messages.<message>.payload.#ref match the components.schemas.<payload>
     *
     * @param operation  operation
     * @param components components
     */
    private List<String> validateChannelRefWithComponents(final Operation operation, final Components components)
    {
        List<String> errorList = new ArrayList<>();
        Map<String, Object> messages = components.getMessages();
        Map<String, Object> schemas = components.getSchemas();
        // Test if the channel message is referenced in the components message & components scheme
        // The validateChannel asserts that there are only one operation, so get it
        Object messageObject = messages.values().stream().findFirst().get();

        // If the channel is a reference to components.messages, check the reference
        // And test if the schema exists into the schemas section
        if (operation.getMessage() instanceof Reference &&
                messageObject instanceof Message &&
                ((Message) messageObject).getPayload() instanceof Schema)
        {
            Reference channelMessage = (Reference) operation.getMessage();
            Message message = (Message) messageObject;

            // Check if the channel message reference the components.messages
            String messageName = (String) messages.keySet().stream().toArray()[0];
            if (!channelMessage.getRef().equals(COMPONENTS_MESSAGES_REF + messageName))
            {
                errorList.add("Channel message reference for [" + messageName
                        + "] must exist in components.messages.");
            }

            // Check if the components.messages.{message}.payload.ref exists in components.schemas
            String messagePayloadReference = ((Schema) message.getPayload()).getRef();
            Matcher matcher = COMPONENTS_SCHEME_REF_PATTERN.matcher(messagePayloadReference);
            if(!matcher.matches() || matcher.group("name") == null || !schemas.containsKey(matcher.group("name")))
            {
                errorList.add("components.messages payload reference ["+messagePayloadReference+"] must exist in schemas.");
            }
        }
        return errorList;
    }
}
