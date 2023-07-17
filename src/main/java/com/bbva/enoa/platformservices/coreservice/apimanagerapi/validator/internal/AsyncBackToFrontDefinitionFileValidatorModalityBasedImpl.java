package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.asyncapi.v2.model.channel.ChannelItem;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.DefinitionFileException;
import com.bbva.enoa.platformservices.coreservice.common.model.NovaAsyncAPI;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class AsyncBackToFrontDefinitionFileValidatorModalityBasedImpl implements IDefinitionFileValidatorModalityBased<NovaAsyncAPI>
{
    protected static final String ERROR_HEAD = "BackToFront asyncapi yml definition -> ";
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
        return ApiModality.ASYNC_BACKTOFRONT == modality;
    }

    private List<String> validateChannels(final NovaAsyncAPI novaAsyncAPI)
    {
        Map<String, ChannelItem> channels = novaAsyncAPI.getAsyncAPI().getChannels();
        List<String> errorList = new ArrayList<>();
        // This check is done in the actual Asyncapi Model with @NotNull, but it is not deleted because the Asyncapi model
        // could change in a future release and delete the check
        if (channels == null || channels.isEmpty())
        {
            errorList.add("Definition must contain one or more channels. Channel not found.");
        }
        return errorList;
    }

}
