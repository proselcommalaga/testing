package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl.mapper;

import com.bbva.enoa.apirestgen.productsapi.model.ProductServiceAssignedResourcesReportDTO;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.bbva.enoa.platformservices.coreservice.productsapi.services.impl.dummy.DummyProductAssignedResourcesGenerator.getDummySubsystems;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceAssignedResourcesReportDtoMapperTest
{
    @Mock
    private IToolsClient toolsClient;
    @InjectMocks
    private ProductServiceAssignedResourcesReportDtoMapper mapper;

    @Test
    @DisplayName("When a assigned resources recordset is passed then return mapped assigned resources dto.")
    void testFromEntityToDto01()
    {
        when(toolsClient.getAllSubsystems()).thenReturn(getDummySubsystems());

        Object[] dummyResultset = new Object[]{
                "PRODUCT_NAME",
                "RELEASE_NAME",
                "RELEASE_VERSION_NAME",
                Timestamp.valueOf("2020-12-03 13:23:03"),
                34802,
                "SERVICE_NAME",
                2D,
                BigDecimal.valueOf(2L),
                BigDecimal.valueOf(2048L)
        };
        List<Object[]> resultSets = new ArrayList<>(1);
        resultSets.add(dummyResultset);

        ProductServiceAssignedResourcesReportDTO[] result = mapper.fromEntityToDto(resultSets);

        assertEquals(1, result.length);
        ProductServiceAssignedResourcesReportDTO firstResult = result[0];
        assertEquals(dummyResultset[0], firstResult.getProductName());
        assertEquals(dummyResultset[1], firstResult.getReleaseName());
        assertEquals(dummyResultset[2], firstResult.getReleaseVersionName());
        assertEquals("2020-12-03 13:23:03", firstResult.getExecutionDate());
        assertEquals("subsystem_" + dummyResultset[4], firstResult.getSubsystemName());
        assertEquals(dummyResultset[5], firstResult.getServiceName());
        assertEquals(dummyResultset[6], firstResult.getCpus());
        assertEquals(((BigDecimal) dummyResultset[7]).longValue(), firstResult.getInstances());
        assertEquals(((BigDecimal) dummyResultset[8]).doubleValue() / 1000, firstResult.getMemory());
    }

}