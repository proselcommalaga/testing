package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Primary
public class ResolverApiManagerValidatorModalityBasedImpl<A extends Api<A, AV, AI>, AV extends ApiVersion<A, AV, AI>, AI extends ApiImplementation<A, AV, AI>>
        implements IApiManagerValidatorModalityBased<A, AV, AI>
{
    private final Map<ApiModality, IApiManagerValidatorModalityBased<A, AV, AI>> validatorServiceMap = new HashMap<>();

    @Autowired
    public ResolverApiManagerValidatorModalityBasedImpl(
            final List<IApiManagerValidatorModalityBased<A, AV, AI>> validatorServices
    )
    {
        for (IApiManagerValidatorModalityBased<A, AV, AI> builderService : validatorServices)
        {
            Arrays.stream(ApiModality.values())
                    .filter(builderService::isModalitySupported)
                    .findAny()
                    .ifPresent(modality -> this.validatorServiceMap.put(modality, builderService));
        }
    }

    @Override
    public A findAndValidateOrPersistIfMissing(final A api)
    {
        return this.getApiManagerValidatorServiceModalityBased(api.getApiModality())
                .findAndValidateOrPersistIfMissing(api);
    }

    @Override
    public boolean isModalitySupported(final ApiModality modality)
    {
        return this.validatorServiceMap.containsKey(modality);
    }

    private IApiManagerValidatorModalityBased<A, AV, AI> getApiManagerValidatorServiceModalityBased(final ApiModality modality)
    {
        return Optional.ofNullable(this.validatorServiceMap.get(modality))
                .orElseThrow(() -> new NotImplementedException(String.format("There is no an IApiManagerValidatorServiceModalityBased implemented for the modality %s", modality)));
    }
}
