package com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.internal;

import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.platformservices.coreservice.apigatewayapi.builder.utils.AGMServiceDtoBuilder;
import com.bbva.enoa.platformservices.coreservice.common.interfaces.IApiModalitySegregatable;

public interface IApiGatewayDtoBuilderModalityBased<A extends Api<A, AV, AI>, AV extends ApiVersion<A, AV, AI>, AI extends ApiImplementation<A, AV, AI>>
        extends IApiModalitySegregatable
{

    void addServedApiToServiceDto(final AI servedApiImplementation, final AGMServiceDtoBuilder serviceDTOBuilder);

    void addConsumedApiToServiceDto(final AV consumedApiVersion, final String deploymentMode, final String environment, final AGMServiceDtoBuilder serviceDTOBuilder);
}
