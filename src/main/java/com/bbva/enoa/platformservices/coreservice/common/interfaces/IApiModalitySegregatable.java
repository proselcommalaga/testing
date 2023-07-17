package com.bbva.enoa.platformservices.coreservice.common.interfaces;

import com.bbva.enoa.datamodel.model.api.enumerates.ApiModality;

public interface IApiModalitySegregatable
{
    boolean isModalitySupported(final ApiModality modality);
}
