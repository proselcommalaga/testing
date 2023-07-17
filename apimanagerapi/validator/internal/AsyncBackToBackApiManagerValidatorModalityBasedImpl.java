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
public class AsyncBackToBackApiManagerValidatorModalityBasedImpl
        implements IApiManagerValidatorModalityBased<AsyncBackToBackApi, AsyncBackToBackApiVersion, AsyncBackToBackApiImplementation>
{
    private final AsyncBackToBackApiRepository asyncBackToBackApiRepository;

    @Override
    public AsyncBackToBackApi findAndValidateOrPersistIfMissing(final AsyncBackToBackApi api)
    {
        AsyncBackToBackApi previousApi = this.asyncBackToBackApiRepository.findByProductIdAndNameAndUuaa(
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
            return this.asyncBackToBackApiRepository.save(api);
        }
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.ASYNC_BACKTOBACK == modality;
    }
}
