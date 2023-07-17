package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl.mapper;

import com.bbva.enoa.apirestgen.productsapi.model.ProductsAssignedResourcesReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.bbva.enoa.platformservices.coreservice.productsapi.services.impl.dummy.DummyProductAssignedResourcesGenerator.getDummyDatabaseResults;
import static com.bbva.enoa.platformservices.coreservice.productsapi.services.impl.dummy.DummyProductAssignedResourcesGenerator.getDummyProductServiceAssignedResourcesReportDtos;
import static com.bbva.enoa.platformservices.coreservice.productsapi.services.impl.dummy.DummyProductAssignedResourcesGenerator.getDummyProductsAssignedResourcesReport;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductsAssignedResourcesReportMapperTest
{
    @Mock
    private ProductServiceAssignedResourcesReportDtoMapper serviceDtoMapper;
    @InjectMocks
    private ProductsAssignedResourcesReportMapper mapper;

    @Test
    @DisplayName("When database resultset is received for mapping to ProductsAssignedResourcesReport then return mapped dto.")
    void testFromEntityToDto01()
    {
        when(serviceDtoMapper.fromEntityToDto(anyList())).thenReturn(getDummyProductServiceAssignedResourcesReportDtos());

        ProductsAssignedResourcesReport results = mapper.fromEntityToDto(getDummyDatabaseResults());

        ProductsAssignedResourcesReport expected = getDummyProductsAssignedResourcesReport();
        assertArrayEquals(expected.getServices(), results.getServices());
        assertEquals(expected.getTotalMemory(), results.getTotalMemory());
        assertEquals(expected.getTotalCPUs(), results.getTotalCPUs());
        assertEquals(expected.getTotalInstances(), results.getTotalInstances());
        assertEquals(expected.getTotalServices() - 1, results.getTotalServices());
        assertEquals(expected.getTotalElementsCount(), results.getTotalElementsCount());

    }
}
