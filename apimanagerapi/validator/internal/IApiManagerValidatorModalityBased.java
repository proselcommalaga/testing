package com.bbva.enoa.platformservices.coreservice.apimanagerapi.validator.internal;

import com.bbva.enoa.datamodel.model.api.entities.Api;
import com.bbva.enoa.datamodel.model.api.entities.ApiImplementation;
import com.bbva.enoa.datamodel.model.api.entities.ApiVersion;
import com.bbva.enoa.platformservices.coreservice.common.interfaces.IApiModalitySegregatable;

/**
 * Interface for Api manager repository validator service
 *
 * @author BBVA - XE72018 - 18/06/2018
 */
public interface IApiManagerValidatorModalityBased<A extends Api<A, AV, AI>, AV extends ApiVersion<A, AV, AI>, AI extends ApiImplementation<A, AV, AI>>
        extends IApiModalitySegregatable
{

    /**
     * Checks if the Api already existed in database and validate it or persist it if missing
     *
     * @param api the api being uploaded
     * @return the stored Sync Api if exists, the new sync api if it does not
     */
    A findAndValidateOrPersistIfMissing(final A api);

}
