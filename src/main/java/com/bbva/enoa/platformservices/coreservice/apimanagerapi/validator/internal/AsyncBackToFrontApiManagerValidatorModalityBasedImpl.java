package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.bbva.enoa.datamodel.model.api.entities.*;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.ApiManagerError;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class AsyncBackToFrontApiManagerValidatorModalityBasedImpl
        implements IApiManagerValidatorModalityBased<AsyncBackToFrontApi, AsyncBackToFrontApiVersion, AsyncBackToFrontApiImplementation>
{
    private final AsyncBackToFrontApiRepository asyncBackToFrontApiRepository;

    @Override
    public AsyncBackToFrontApi findAndValidateOrPersistIfMissing(final AsyncBackToFrontApi api)
    {
        AsyncBackToFrontApi previousApi = this.asyncBackToFrontApiRepository.findByProductIdAndNameAndUuaa(
                api.getProduct().getId(), api.getName(), api.getUuaa()
        );

        if (previousApi != null)
        {
            if (previousApi.getType() != api.getType())
            {
                throw new NovaException(ApiManagerError.getInvalidApiTypeError(api.getName(),
                        previousApi.getType().getApiType(), api.getType().getApiType()));
            }
            return previousApi;
        }
        else
        {
            return this.asyncBackToFrontApiRepository.save(api);
        }
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.ASYNC_BACKTOFRONT == modality;
    }
}
