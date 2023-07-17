package com.bbva.enoa.platformservices.coreservice.behaviorapi.services.interfaces;

import com.bbva.enoa.apirestgen.behaviorapi.model.BVServiceApiDTO;
import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;

public interface IBehaviorVersionDtoBuilderModalityBased<A extends Api<A, AV, AI>, AV extends ApiVersion<A, AV, AI>, AI extends ApiImplementation<A, AV, AI>>
{
    BVServiceApiDTO buildBVServiceApiDTO(AI apiImplementation);

    boolean isModalitySupported(ApiModality modality);
}
