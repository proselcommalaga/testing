package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.bbva.enoa.datamodel.model.api.entities.SyncApi;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.SyncApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.ApiManagerError;
import com.bbva.enoa.platformservices.coreservice.common.repositories.SyncApiRepository;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class SyncApiManagerValidatorModalityBasedImpl
        implements IApiManagerValidatorModalityBased<SyncApi, SyncApiVersion, SyncApiImplementation>
{
    private final SyncApiRepository syncApiRepository;

    @Override
    public SyncApi findAndValidateOrPersistIfMissing(final SyncApi api)
    {
        SyncApi previousApi = this.syncApiRepository.findByProductIdAndNameAndUuaa(
                api.getProduct().getId(), api.getName(), api.getUuaa()
        );

        if (previousApi != null)
        {
            if (previousApi.getType() != api.getType())
            {
                throw new NovaException(ApiManagerError.getInvalidApiTypeError(api.getName(),
                        previousApi.getType().getApiType(), api.getType().getApiType()));
            }
            if (!previousApi.getBasePathSwagger().equals(api.getBasePathSwagger()))
            {
                throw new NovaException(ApiManagerError.getInvalidApiBasePathError(api.getName(),
                        previousApi.getBasePathSwagger()));
            }
            return previousApi;
        }
        else
        {
            return this.syncApiRepository.save(api);
        }
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return ApiModality.SYNC == modality;
    }
}
