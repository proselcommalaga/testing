package com.bbva.enoa.platformservices.coreservice.consumers.impl;

import com.bbva.enoa.apirestgen.alertserviceapi.client.feign.nova.rest.IRestHandlerAlertserviceapi;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASBasicAlertInfoDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASOverviewDetailsDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASOverviewFieldsDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASOverviewRootDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASPageableQueryDTO;
import com.bbva.enoa.apirestgen.alertserviceapi.model.ASRequestAlertsDTO;
import com.bbva.enoa.platformservices.coreservice.consumers.dummy.data.DummyConsumerDataGenerator;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.alertservice.AlertServiceApiClientImpl;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.exception.Errors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AlertServiceApiClientImplTest
{
    @Mock
    private IRestHandlerAlertserviceapi iRestHandler;
    @InjectMocks
    private AlertServiceApiClientImpl client;

    @BeforeEach
    public void init()
    {
        MockitoAnnotations.initMocks(AlertServiceApiClientImpl.class);
        client.init();
    }

    @Test
    public void when_check_deploy_plan_alert_info_request_has_response_then_return_response_body()
    {
        ASRequestAlertsDTO dto = getASRequestAlertsDto();
        Mockito.when(iRestHandler.existAlertsInPlan(Mockito.anyInt(), Mockito.any(String[].class))).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        ASRequestAlertsDTO result = client.checkDeployPlanAlertInfo(1, new String[]{""});

        Assertions.assertEquals(dto.getHaveAlerts(), result.getHaveAlerts());
        ASBasicAlertInfoDTO[] basicAlertInfo = result.getBasicAlertInfo();
        ASBasicAlertInfoDTO[] dtoAlertInfo = dto.getBasicAlertInfo();
        for (int i = 0; i < basicAlertInfo.length; i++)
        {
            ASBasicAlertInfoDTO resultInfo = basicAlertInfo[i];
            ASBasicAlertInfoDTO dtoInfo = dtoAlertInfo[i];
            Assertions.assertEquals(dtoInfo.getAlertId(), resultInfo.getAlertId());
            Assertions.assertEquals(dtoInfo.getAlertRelatedId(), resultInfo.getAlertRelatedId());
            Assertions.assertEquals(dtoInfo.getAlertType(), resultInfo.getAlertType());
            Assertions.assertEquals(dtoInfo.getStatus(), resultInfo.getStatus());
        }
    }

    private ASRequestAlertsDTO getASRequestAlertsDto()
    {
        ASRequestAlertsDTO dto = new ASRequestAlertsDTO();
        dto.setHaveAlerts(true);
        dto.setBasicAlertInfo(getASBasicAlertInfoDtoArray());
        return dto;
    }

    private ASBasicAlertInfoDTO[] getASBasicAlertInfoDtoArray()
    {
        ASBasicAlertInfoDTO dto = new ASBasicAlertInfoDTO();
        dto.setAlertId(1);
        dto.setAlertType("A");
        dto.setAlertRelatedId("B");
        dto.setStatus("C");
        return new ASBasicAlertInfoDTO[]{dto};
    }

    @Test
    public void when_check_deploy_plan_alert_info_request_has_error_then_return_null()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        Mockito.when(iRestHandler.existAlertsInPlan(Mockito.anyInt(), Mockito.any(String[].class))).thenReturn(new ResponseEntity(errors, HttpStatus.BAD_REQUEST));

        ASRequestAlertsDTO result = client.checkDeployPlanAlertInfo(1, new String[]{""});

        Assertions.assertNull(result);
    }

    @Test
    public void when_call_to_get_alerts_by_id_and_status_has_response_then_return_response_body()
    {
        ASOverviewRootDTO dto = getASOverviewRootDto();
        Mockito.when(iRestHandler.getPageableOverview(Mockito.any(ASPageableQueryDTO.class))).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        ASRequestAlertsDTO result = client.getAlertsByRelatedIdAndStatus(new String[]{"A"}, 1, "A", "A");

        Assertions.assertTrue(result.getHaveAlerts());
        ASBasicAlertInfoDTO[] basicAlertInfo = result.getBasicAlertInfo();
        Assertions.assertEquals(1, basicAlertInfo.length);
        ASBasicAlertInfoDTO infoDto = basicAlertInfo[0];
        ASOverviewDetailsDTO detailDto = dto.getFields()[0].getAlertsInfo()[0];
        Assertions.assertEquals(detailDto.getId(), infoDto.getAlertId());
        Assertions.assertEquals(detailDto.getStatus(), infoDto.getStatus());
        Assertions.assertEquals(detailDto.getRelatedId(), infoDto.getAlertRelatedId());
        Assertions.assertEquals(detailDto.getAlertType(), infoDto.getAlertType());
    }

    @Test
    public void when_call_to_get_alerts_by_id_and_status_has_response_with_different_uuaa_then_return_empty_alerts_body_with_flag_true()
    {
        ASOverviewRootDTO dto = getASOverviewRootDto();
        dto.getFields()[0].setUuaa("B");
        Mockito.when(iRestHandler.getPageableOverview(Mockito.any(ASPageableQueryDTO.class))).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        ASRequestAlertsDTO result = client.getAlertsByRelatedIdAndStatus(new String[]{"A"}, 1, "A", "A");

        Assertions.assertTrue(result.getHaveAlerts());
        ASBasicAlertInfoDTO[] basicAlertInfo = result.getBasicAlertInfo();
        Assertions.assertEquals(0, basicAlertInfo.length);
    }

    @Test
    public void when_call_to_alert_pageable_overview_returns_null_response_then_return_empty_alerts_body_with_flag_false()
    {
        Mockito.when(iRestHandler.getPageableOverview(Mockito.any(ASPageableQueryDTO.class))).thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        ASRequestAlertsDTO result = client.getAlertsByRelatedIdAndStatus(new String[]{"A"}, 1, "A", "A");

        Assertions.assertFalse(result.getHaveAlerts());
        Assertions.assertNull(result.getBasicAlertInfo());
    }

    @Test
    public void when_call_to_alert_pageable_overview_returns_response_with_zero_total_elements_then_return_empty_alerts_body_with_flag_false()
    {
        ASOverviewRootDTO dto = getASOverviewRootDto();
        dto.setTotalElementsCount(0L);
        Mockito.when(iRestHandler.getPageableOverview(Mockito.any(ASPageableQueryDTO.class))).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        ASRequestAlertsDTO result = client.getAlertsByRelatedIdAndStatus(new String[]{"A"}, 1, "A", "A");

        Assertions.assertFalse(result.getHaveAlerts());
        Assertions.assertNull(result.getBasicAlertInfo());
    }

    @Test
    public void when_call_to_get_alerts_by_id_and_status_has_response_without_overview_details_then_return_empty_alerts_body_with_flag_true()
    {
        ASOverviewRootDTO dto = getASOverviewRootDto();
        dto.getFields()[0].setAlertsInfo(new ASOverviewDetailsDTO[0]);
        Mockito.when(iRestHandler.getPageableOverview(Mockito.any(ASPageableQueryDTO.class))).thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

        ASRequestAlertsDTO result = client.getAlertsByRelatedIdAndStatus(new String[]{"A"}, 1, "A", "A");

        Assertions.assertTrue(result.getHaveAlerts());
        Assertions.assertEquals(0, result.getBasicAlertInfo().length);
    }

    private ASOverviewRootDTO getASOverviewRootDto()
    {
        ASOverviewRootDTO dto = new ASOverviewRootDTO();
        dto.setPageNumber(1L);
        dto.setPageSize(10L);
        dto.setElementsCount(5L);
        dto.setFields(getOverviewFields());
        dto.setTotalElementsCount(5L);
        return dto;
    }

    private ASOverviewFieldsDTO[] getOverviewFields()
    {
        ASOverviewFieldsDTO dto = new ASOverviewFieldsDTO();
        dto.setUuaa("A");
        dto.setAlertsInfo(getASOverviewDetailsDto());
        return new ASOverviewFieldsDTO[]{dto};
    }

    private ASOverviewDetailsDTO[] getASOverviewDetailsDto()
    {
        ASOverviewDetailsDTO dto = new ASOverviewDetailsDTO();
        dto.setAlertType("A");
        dto.setEnvironment("A");
        dto.setStatus("A");
        dto.setCloseDate(100L);
        dto.setId(1);
        dto.setCreationDate(100L);
        dto.setRelatedId("A");
        dto.setTitle("A");
        return new ASOverviewDetailsDTO[]{dto};
    }

    @Test
    public void when_call_to_get_alerts_by_id_and_status_request_has_error_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        Mockito.when(iRestHandler.getPageableOverview(Mockito.any(ASPageableQueryDTO.class))).thenReturn(new ResponseEntity(errors, HttpStatus.BAD_REQUEST));

        Assertions.assertThrows(NovaException.class, () -> client.getAlertsByRelatedIdAndStatus(new String[]{"A"}, 1, "A", "A"));
    }

    @Test
    public void when_get_products_days_ago_request_has_response_then_return_response_body()
    {
        ASBasicAlertInfoDTO[] dtos = getASBasicAlertInfoDtos();
        Mockito.when(iRestHandler.getProductAlertsSinceDaysAgo(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity<>(dtos, HttpStatus.OK));

        ASBasicAlertInfoDTO[] result = client.getProductAlertsSinceDaysAgo(1, "A", "A", "A");

        Assertions.assertEquals(1, result.length);
        ASBasicAlertInfoDTO firstResult = result[0];
        ASBasicAlertInfoDTO firstDto = dtos[0];
        Assertions.assertEquals(firstDto.getAlertType(), firstResult.getAlertType());
        Assertions.assertEquals(firstDto.getAlertId(), firstResult.getAlertId());
        Assertions.assertEquals(firstDto.getAlertRelatedId(), firstResult.getAlertRelatedId());
        Assertions.assertEquals(firstDto.getStatus(), firstResult.getStatus());
    }

    private ASBasicAlertInfoDTO[] getASBasicAlertInfoDtos()
    {
        ASBasicAlertInfoDTO dto = new ASBasicAlertInfoDTO();
        dto.setAlertId(1);
        dto.setAlertType("A");
        dto.setAlertRelatedId("A");
        dto.setStatus("A");
        return new ASBasicAlertInfoDTO[]{dto};
    }

    @Test
    public void when_get_products_days_ago_request_has_error_then_throw_exception()
    {
        Errors errors = DummyConsumerDataGenerator.getDummyErrors();
        Mockito.when(iRestHandler.getProductAlertsSinceDaysAgo(Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn(new ResponseEntity(errors, HttpStatus.BAD_REQUEST));

        Assertions.assertThrows(NovaException.class, () -> client.getProductAlertsSinceDaysAgo(1, "A", "A", "A"));
    }
}