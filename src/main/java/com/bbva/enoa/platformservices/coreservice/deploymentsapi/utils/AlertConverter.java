package com.bbva.enoa.platformservices.coreservice.deploymentsapi.utils;

import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.deploymentsapi.model.BasicAlertInfoDTO;

import javax.validation.constraints.NotNull;

public final class AlertConverter
{
    private AlertConverter()
    {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    /**
     * Convert the Health Monitor API DTO into the Core Service DTO
     *
     * @param asBasicAlertInfoDTO POJO received from the Health Monitor API
     * @return a new BasicAlertInfoDTO instance with the ASBasicAlertInfoDTO information
     */
    public static BasicAlertInfoDTO convertHealthMonitorDTOIntoCoreDTO(@NotNull final ASBasicAlertInfoDTO asBasicAlertInfoDTO)
    {
        BasicAlertInfoDTO basicAlertInfoDTO = new BasicAlertInfoDTO();
        basicAlertInfoDTO.setAlertId(asBasicAlertInfoDTO.getAlertId());
        basicAlertInfoDTO.setAlertType(asBasicAlertInfoDTO.getAlertType());
        basicAlertInfoDTO.setAlertRelatedId(asBasicAlertInfoDTO.getAlertRelatedId());
        basicAlertInfoDTO.setStatus(asBasicAlertInfoDTO.getStatus());
        return basicAlertInfoDTO;
    }
}
