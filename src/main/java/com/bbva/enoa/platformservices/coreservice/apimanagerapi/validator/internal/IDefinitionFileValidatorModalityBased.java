package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.bbva.enoa.datamodel.model.api.enumerates.ApiType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.apimanagerapi.exception.DefinitionFileException;
import com.bbva.enoa.platformservices.coreservice.common.interfaces.IApiModalitySegregatable;

public interface IDefinitionFileValidatorModalityBased<T> extends IApiModalitySegregatable
{
    T parseAndValidate(String content, Product product, ApiType apiType) throws DefinitionFileException;

}
