package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl.mapper;

import com.bbva.enoa.apirestgen.productsapi.model.ProductServiceAssignedResourcesReportDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.mapper.EntityDtoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductServiceAssignedResourcesReportDtoMapper implements EntityDtoMapper<ProductServiceAssignedResourcesReportDTO[], List<Object[]>>
{
    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceAssignedResourcesReportDtoMapper.class);
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private final IToolsClient toolsClient;

    @Autowired
    public ProductServiceAssignedResourcesReportDtoMapper(IToolsClient toolsClient)
    {
        this.toolsClient = toolsClient;
    }

    @Override
    public ProductServiceAssignedResourcesReportDTO[] fromEntityToDto(List<Object[]> entity)
    {
        Map<Integer, String> subsystemsMap = getSubsystemsMap();

        LOG.debug("[ProductServiceAssignedResourcesReportMapper] -> [fromEntityToDto]: Proceeding to map database results to ProductServiceAssignedResourcesReportDTO, size {}", entity.size());
        ProductServiceAssignedResourcesReportDTO[] services = new ProductServiceAssignedResourcesReportDTO[entity.size()];
        for (int i = 0; i < entity.size(); i++)
        {
            Object[] currentRecord = entity.get(i);
            ProductServiceAssignedResourcesReportDTO service = new ProductServiceAssignedResourcesReportDTO();
            service.setProductName((String) currentRecord[0]);
            service.setReleaseName((String) currentRecord[1]);
            service.setReleaseVersionName((String) currentRecord[2]);
            Object executionDateColumn = currentRecord[3];
            if (executionDateColumn != null)
            {
                final Timestamp executionDate = (Timestamp) currentRecord[3];
                Date date = new Date();
                date.setTime(executionDate.getTime());
                service.setExecutionDate(new SimpleDateFormat(DATE_FORMAT_PATTERN).format(date));
            }
            Integer subsystemId = (Integer) currentRecord[4];
            service.setSubsystemName(subsystemsMap.get(subsystemId));
            final String serviceName = (String) currentRecord[5];
            service.setServiceName(serviceName);
            final double cpus = (Double) currentRecord[6];
            service.setCpus(cpus);
            final long instances = ((BigDecimal) currentRecord[7]).longValue();
            service.setInstances(instances);
            final double memory = ((BigDecimal) currentRecord[8]).doubleValue() / 1000;
            service.setMemory(memory);
            services[i] = service;
        }

        LOG.debug("[ProductServiceAssignedResourcesReportMapper] -> [fromEntityToDto]: Finished database results mapping to ProductServiceAssignedResourcesReportDTO");
        return services;
    }

    private Map<Integer, String> getSubsystemsMap()
    {
        LOG.debug("[ProductServiceAssignedResourcesReportMapper] -> [getSubsystemsMap]: Obtaining all subsystems for mapping ProductServiceAssignedResourcesReportDto.");
        final List<TOSubsystemDTO> subsystems = this.toolsClient.getAllSubsystems();
        LOG.debug("[ProductServiceAssignedResourcesReportMapper] -> [getSubsystemsMap]: Obtained all subsystems for mapping ProductServiceAssignedResourcesReportDto: [{}]", subsystems);
        return subsystems.stream().collect(Collectors.toMap(TOSubsystemDTO::getSubsystemId, TOSubsystemDTO::getSubsystemName));
    }
}
