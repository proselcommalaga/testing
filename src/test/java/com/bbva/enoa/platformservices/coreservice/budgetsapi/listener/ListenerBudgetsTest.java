package com.bbva.enoa.platformservices.coreservice.budgetsapi.listener;

import com.bbva.enoa.apirestgen.budgetsapi.model.*;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.exceptions.BudgetsError;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.NovaMetadata;
import com.bbva.kltt.apirest.generator.lib.commons.metadata.headers.NovaImplicitHeadersInput;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


class ListenerBudgetsTest
{
    @Mock
    private IBudgetsService budgetsService;

    @InjectMocks
    private ListenerBudgets listenerBudgets;
    
    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void calculateFinalDate() throws Exception
    {
        //Given
        DateObject startDate = new DateObject();
        startDate.setDateValue("2018-03-12");
        startDate.setDuration(10L);
        DateObject endDate = new DateObject();
        endDate.setDateValue("2018-03-22");
        when(this.budgetsService.calculateFinalDate(startDate)).thenReturn(endDate);

        //Then
        DateObject response = this.listenerBudgets.calculateFinalDate(getNovaMetadata(), startDate);
        assertEquals(endDate, response);

        //Given
        when(this.budgetsService.calculateFinalDate(startDate)).thenThrow(new NovaException(BudgetsError.getUnexpectedError(), "Error"));

        //Then
        assertThrows(NovaException.class, () -> this.listenerBudgets.calculateFinalDate(getNovaMetadata(), startDate));
    }

    @Test
    void getProductServicesSummary() throws Exception
    {
        //Given
        BUDGServiceSummaryItem item = new BUDGServiceSummaryItem();
        item.setStatus("Status");
        List<BUDGServiceSummaryItem> items = new ArrayList<>();
        items.add(item);
        when(this.budgetsService.getProductServicesSummary(1)).thenReturn(items);

        //Then
        BUDGServiceSummaryItem[] response = this.listenerBudgets.getProductServicesSummary(getNovaMetadata(), 1);
        assertEquals("Status", response[0].getStatus());

        //Given
        when(this.budgetsService.getProductServicesSummary(2)).thenThrow(new NovaException(BudgetsError.getUnexpectedError(), "Error"));
    }

    @Test
    void checkDeployabilityStatus() throws Exception
    {
        //Given
        when(this.budgetsService.checkDeploymentPlanDeployabilityStatus(1)).thenReturn(true);

        //Then
        Boolean response = this.listenerBudgets.checkDeployabilityStatus(getNovaMetadata(), 1);
        Assertions.assertTrue(response);

        //Given
        when(this.budgetsService.checkDeploymentPlanDeployabilityStatus(2)).thenThrow(new NovaException(BudgetsError.getUnexpectedError(), "Error"));
    }

    @Test
    void getServiceDetail() throws Exception
    {
        //Given
        BUDGServiceDetail serviceDetail = new BUDGServiceDetail();
        serviceDetail.setInitiativeName("Name");
        when(this.budgetsService.getServiceDetail(1L)).thenReturn(serviceDetail);

        //Then
        BUDGServiceDetail response = this.listenerBudgets.getServiceDetail(getNovaMetadata(), 1L);
        assertEquals("Name", response.getInitiativeName());

        //Given
        when(this.budgetsService.getServiceDetail(2L)).thenThrow(new NovaException(BudgetsError.getUnexpectedError(), "Error"));

        //Then
        assertThrows(NovaException.class, () -> this.listenerBudgets.getServiceDetail(getNovaMetadata(), 2L));
    }

    @Test
    void updateProductService() throws Exception
    {
        //Given
        BUDGUpdatedService updatedService = new BUDGUpdatedService();
        updatedService.setUpdatedStartDate("2018-03-12");

        //Then
        this.listenerBudgets.updateProductService(getNovaMetadata(), updatedService, 1L);
        verify(this.budgetsService, times(1)).updateService(updatedService, 1L, "");

        //Given
        doThrow(new NovaException(BudgetsError.getUnexpectedError(), "Error")).when(this.budgetsService).updateService(updatedService, 1L, "");

        //Then
        assertThrows(NovaException.class, () -> this.listenerBudgets.updateProductService(getNovaMetadata(), updatedService, 1L));
    }

    @Test
    void getProductServicesDetail() throws Exception
    {
        //Given
        BUDGServiceDetailItem item = new BUDGServiceDetailItem();
        item.setStatus("Status");
        List<BUDGServiceDetailItem> items = new ArrayList<>();
        items.add(item);
        when(this.budgetsService.getProductServicesDetail(1)).thenReturn(items);

        //Then
        BUDGServiceDetailItem[] response = this.listenerBudgets.getProductServicesDetail(getNovaMetadata(), 1);
        assertEquals("Status", response[0].getStatus());

        //Given
        when(this.budgetsService.getProductServicesDetail(2)).thenThrow(new NovaException(BudgetsError.getUnexpectedError(), "Error"));
    }

    @Test
    void getProductBudgets() throws Exception
    {
        //Given
        BUDGProductBudgetsDTO productBudgetsDTO = new BUDGProductBudgetsDTO();
        BUDGBudgetDTO budgetDTO = new BUDGBudgetDTO();
        budgetDTO.setAvailableAmount(100.0);
        productBudgetsDTO.setHardwareBudget(budgetDTO);
        when(this.budgetsService.getProductBudgets(1, "LOCAL")).thenReturn(productBudgetsDTO);

        //Then
        BUDGProductBudgetsDTO response = this.listenerBudgets.getProductBudgets(getNovaMetadata(), 1, "LOCAL");
        assertEquals(productBudgetsDTO, response);

        //Given
        when(this.budgetsService.getProductBudgets(2, "LOCAL")).thenThrow(new NovaException(BudgetsError.getUnexpectedError(), "Error"));
    }

    private static NovaMetadata getNovaMetadata(){
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("iv-user", Collections.singletonList(""));
        NovaMetadata metadata = new NovaMetadata();
        metadata.setNovaImplicitHeadersInput(new NovaImplicitHeadersInput(headers.entrySet()));
        return metadata;
    }
}
