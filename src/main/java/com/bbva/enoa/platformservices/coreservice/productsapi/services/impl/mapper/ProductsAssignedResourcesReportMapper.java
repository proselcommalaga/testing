package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl.mapper;

import com.bbva.enoa.apirestgen.productsapi.model.ProductServiceAssignedResourcesReportDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductsAssignedResourcesReport;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.mapper.EntityDtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Service
public class ProductsAssignedResourcesReportMapper implements EntityDtoMapper<ProductsAssignedResourcesReport, List<Object[]>>
{
    private static final Logger LOG = LoggerFactory.getLogger(ProductsAssignedResourcesReportMapper.class);
    private final ProductServiceAssignedResourcesReportDtoMapper serviceDtoMapper;

    @Autowired
    public ProductsAssignedResourcesReportMapper(ProductServiceAssignedResourcesReportDtoMapper serviceDtoMapper)
    {
        this.serviceDtoMapper = serviceDtoMapper;
    }

    @Override
    public ProductsAssignedResourcesReport fromEntityToDto(List<Object[]> entity)
    {
        ProductsAssignedResourcesReport reportDto = new ProductsAssignedResourcesReport();
        int totalElementsCount = 0;
        long totalServices = 0L;
        ProductServiceAssignedResourcesReportDTO[] services = new ProductServiceAssignedResourcesReportDTO[0];
        double totalCpus = 0D;
        long totalInstances = 0L;
        double totalMemory = 0D;
        if (!entity.isEmpty())
        {
            // Substract the record with total information. Other records are services.
            totalServices = (long) entity.size() - 1;
            if (totalServices > 0L)
            {
                LOG.debug("[ProductsAssignedResourcesReportMapper] -> [fromEntityToDto]: calling to ProductServiceAssignedResourcesReportDTO mapper.");
                services = serviceDtoMapper.fromEntityToDto(entity.subList(1, entity.size()));
                LOG.debug("[ProductsAssignedResourcesReportMapper] -> [fromEntityToDto]: finished call to ProductServiceAssignedResourcesReportDTO mapper.");
            }
            Object[] totalInformationRecord = entity.get(0);
            totalCpus = (Double) totalInformationRecord[6];
            totalInstances = ((BigDecimal) totalInformationRecord[7]).longValue();
            totalMemory = ((BigDecimal) totalInformationRecord[8]).doubleValue() / 1000;
            totalElementsCount = ((BigInteger) totalInformationRecord[9]).intValue();
        }
        reportDto.setTotalServices(totalServices);
        reportDto.setServices(services);
        LOG.debug("[ProductsHostsReportDTO] -> [getProductsAssignedResourcesReport]: Setting total CPU, instances and memory for ProductServiceAssignedResourcesReportDTO array");
        reportDto.setTotalCPUs(totalCpus);
        reportDto.setTotalInstances(totalInstances);
        reportDto.setTotalMemory((Math.round(totalMemory * 1000)) / 1000d);
        LOG.debug("[ProductsHostsReportDTO] -> [getProductsAssignedResourcesReport]: Set actual total CPU, instances and memory values for ProductServiceAssignedResourcesReportDTO array");
        reportDto.setTotalElementsCount(totalElementsCount);
        return reportDto;
    }
}
