package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.services.intefaces;

import com.bbva.enoa.apirestgen.releaseversionsapi.model.RVApiDTO;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;

public interface IReleaseVersionDtoBuilderModalityBased<A extends Api<A, AV, AI>, AV extends ApiVersion<A, AV, AI>, AI extends ApiImplementation<A, AV, AI>>
{
    RVApiDTO buildRVApiDTO(AI apiImplementation);

    boolean isModalitySupported(ApiModality modality);
}
