package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;


import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGProductBudgetsDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.HostMemoryInfo;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemsUsageReportDTO;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBProductsUsedResourcesReportDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductsAssignedResourcesReport;
import com.bbva.enoa.apirestgen.productsapi.model.ProductsFilesystemsUsageReportDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductsHostReportDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductsHostsReportDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductsUsedResourcesReportDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductReportRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.util.ExportDataUtils;
import com.bbva.enoa.platformservices.coreservice.common.view.ProductCSV;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ReportDeploymentManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsService;
import com.bbva.enoa.platformservices.coreservice.productsapi.autoconfig.ProductProperties;
import com.bbva.enoa.platformservices.coreservice.productsapi.exception.ProductsAPIError;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.impl.mapper.ProductsAssignedResourcesReportMapper;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.interfaces.IProductReportService;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.bbva.enoa.platformservices.coreservice.common.util.ExportDataUtils.CSV_SEPARATOR;

/**
 * Service for generating reports
 */
@Service
public class ProductReportService implements IProductReportService
{
    /**
     * Log
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProductReportService.class);
    /**
     * Header CSV
     */
    private static final String[] HEADER_CSV = new String[]{"NAME", "UUAA", "DESCRIPTION", "ENVIRONMENT", "RELEASE_NAME", "VERSION_NAME", "EXECUTION_DATE", "SUBSYSTEM_NAME",
            "SUBSYSTEM_TYPE", "SERVICE_NAME", "SERVICE_TYPE", "INSTANCES", "NUM_CPU", "RAM_MB"};

    /**
     * Header CSV
     */
    private static final String[] HEADER_RESOURCES = new String[]{"Nombre del producto", "UUAA", "Entorno", "Presupuesto HW", "Presupuesto HW disponible", "Memoria pendiente (GB)"};

    /**
     * Header CSV
     */
    private static final String[] HEADER_HOST = new String[]{"Máquina", "IP", "Memoria Total (GB)", "Memoria Asignada (GB)", "Memoria Asignada (%)"};

    private final ProductReportRepository productReportRepository;
    private final ProductRepository productRepository;
    private final IBudgetsService budgetsService;
    private final ReportDeploymentManagerClient reportDeploymentManagerClient;
    private final ProductProperties productProperties;
    private final IToolsClient toolsClient;
    private final ProductsAssignedResourcesReportMapper serviceReportMapper;
    private final IFilesystemsService filesystemsService;

    @Autowired
    public ProductReportService(ProductReportRepository productReportRepository, ProductRepository productRepository, IBudgetsService budgetsService, ReportDeploymentManagerClient reportDeploymentManagerClient,
                                ProductProperties productProperties, IToolsClient toolsClient, ProductsAssignedResourcesReportMapper serviceReportMapper, IFilesystemsService filesystemsService)
    {
        this.productReportRepository = productReportRepository;
        this.productRepository = productRepository;
        this.budgetsService = budgetsService;
        this.reportDeploymentManagerClient = reportDeploymentManagerClient;
        this.productProperties = productProperties;
        this.toolsClient = toolsClient;
        this.serviceReportMapper = serviceReportMapper;
        this.filesystemsService = filesystemsService;
    }

    @Override
    public ProductsUsedResourcesReportDTO getProductsUsedResourcesReport(String environment, String uuaa)
    {
        String filteredUuaaValue = uuaa != null && !"".equals(uuaa) && !"ALL".equals(uuaa) ? uuaa.toUpperCase() : null;
        Long[] productIdsByUuaa = this.productRepository.findProductIdsByUuaa(filteredUuaaValue);
        final long[] productIds = productIdsByUuaa.length > 0 ? Arrays.stream(productIdsByUuaa).mapToLong(e -> e).toArray() : new long[]{-1L};
        String filteredEnvironment = environment != null && !"".equals(environment) && !"ALL".equals(environment) ? environment : "";
        final PBProductsUsedResourcesReportDTO productBudgetDto = this.budgetsService.getProductsUsedResourcesReport(productIds, filteredEnvironment);
        ProductsUsedResourcesReportDTO mappedDto = new ProductsUsedResourcesReportDTO();
        mappedDto.setAvailableHW(productBudgetDto.getAvailableHW());
        mappedDto.setAvailableMemory(productBudgetDto.getAvailableMemory());
        mappedDto.setTotalHW(productBudgetDto.getTotalHW());
        mappedDto.setUsedHW(productBudgetDto.getUsedHW());
        return mappedDto;
    }

    @Override
    public byte[] getProductsUsedResourcesReportExport(final String environment, final String uuaa, final String format)
    {
        final String filteredUuaaValue = uuaa != null && !"".equals(uuaa) && !"ALL".equals(uuaa) ? uuaa.toUpperCase() : null;

        final Environment filteredEnvironment = environment != null && !"".equals(environment) && !"ALL".equals(environment) ? Environment.valueOf(environment.toUpperCase()) : null;

        final List<Product> productList;
        if (filteredUuaaValue == null)
        {
            productList = this.productRepository.findAll();
        }
        else
        {
            productList = this.productRepository.findByUuaa(filteredUuaaValue);
        }

        final List<String[]> valueList = new ArrayList<>();
        if (filteredEnvironment == null)
        {
            valueList.addAll(getProductResourcesReportByEnvironment(productList, Environment.INT.getEnvironment()));
            valueList.addAll(getProductResourcesReportByEnvironment(productList, Environment.PRE.getEnvironment()));
            valueList.addAll(getProductResourcesReportByEnvironment(productList, Environment.PRO.getEnvironment()));
        }
        else
        {
            valueList.addAll(getProductResourcesReportByEnvironment(productList, filteredEnvironment.name()));
        }

        return ExportDataUtils.exportValuesTo(format, HEADER_RESOURCES, valueList);
    }

    @Override
    public ProductsFilesystemsUsageReportDTO getProductsFilesytemsUsageReport(String environment, String uuaa)
    {
        Integer productId = this.productRepository.findProductIdByUuaa(uuaa);
        FilesystemsUsageReportDTO filesystemsUsageReportDTO = this.filesystemsService.getFilesystemsUsageReport(productId, environment);
        return buildDTO(filesystemsUsageReportDTO);
    }

    @Override
    public byte[] getProductsFilesytemsUsageReportExport(final String environment, final String uuaa, final String format)
    {
        final ProductsFilesystemsUsageReportDTO usageReportDTO = this.getProductsFilesytemsUsageReport(environment, uuaa);

        final String[] values = new String[]{String.valueOf(usageReportDTO.getTotalStorageAssigned()),
                String.valueOf(usageReportDTO.getTotalStorageAssignedPercentage()), String.valueOf(usageReportDTO.getTotalStorageAvailable()),
                String.valueOf(usageReportDTO.getTotalStorageAvailablePercentage())};

        return ExportDataUtils.exportValuesTo(format, new String[]{"STORAGE ASSIGNED", "STORAGE ASSIGNED %", "STORAGE AVAILABLE", "STORAGE AVAILABLE %"},
                Collections.singletonList(values));
    }

    @Override
    public ProductsHostsReportDTO getProductsHostsReport(String environment, String cpd)
    {
        final String cluster = getClusterNameFrom(environment);
        final HostMemoryInfo[] productsHostsReport = this.reportDeploymentManagerClient.getProductsHostsReport(cluster, cpd);
        return this.mapToProductsHostsReportDTO(productsHostsReport);
    }

    @Override
    public byte[] getProductsHostsReportExport(final String environment, final String cpd, final String format)
    {
        final List<String[]> valueList = new ArrayList<>();
        String response;
        for (String cluster : new String[]{productProperties.getCdpInt(), productProperties.getCdpPre(), productProperties.getCdpPro()})
        {
            response = reportDeploymentManagerClient.reportHost(cluster);

            if (response != null && !"".equals(response))
            {
                valueList.add(response.split(CSV_SEPARATOR));
            }
        }

        return ExportDataUtils.exportValuesTo(format, HEADER_HOST, valueList);
    }

    private String getClusterNameFrom(String environment)
    {
        String clusterName = productProperties.getCdpPro();
        if (Environment.INT.getEnvironment().equals(environment))
        {
            clusterName = productProperties.getCdpInt();
        }
        else if (Environment.PRE.getEnvironment().equals(environment))
        {
            clusterName = productProperties.getCdpPre();
        }
        return clusterName;
    }

    private ProductsHostsReportDTO mapToProductsHostsReportDTO(final HostMemoryInfo[] hostMemoryInfos)
    {
        ProductsHostsReportDTO dto = new ProductsHostsReportDTO();
        ProductsHostReportDTO[] productHostReportDTOs = new ProductsHostReportDTO[hostMemoryInfos.length];
        double aggregatedTotalMemory = 0D;
        double aggregatedUsedMemory = 0D;
        double aggregatedUsedMemoryPercentage = 0D;
        for (int i = 0; i < hostMemoryInfos.length; i++)
        {
            HostMemoryInfo hostMemoryInfo = hostMemoryInfos[i];
            ProductsHostReportDTO productHostReportDTO = getBasicProductsHostReportDTOFrom(hostMemoryInfo);
            aggregatedTotalMemory += hostMemoryInfo.getTotalMemory();
            aggregatedUsedMemory += hostMemoryInfo.getUsedMemory();
            productHostReportDTOs[i] = productHostReportDTO;
        }
        if (aggregatedTotalMemory > 0D)
        {
            aggregatedUsedMemoryPercentage = BigDecimal.valueOf(aggregatedUsedMemory).divide(BigDecimal.valueOf(aggregatedTotalMemory), RoundingMode.HALF_EVEN).multiply(BigDecimal.valueOf(100D)).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        }
        dto.setAssignedMemory(aggregatedUsedMemory);
        dto.setTotalMemory(aggregatedTotalMemory);
        dto.setAssignedMemoryPercentage(aggregatedUsedMemoryPercentage);
        dto.setMachines(productHostReportDTOs);
        return dto;
    }

    private ProductsHostReportDTO getBasicProductsHostReportDTOFrom(final HostMemoryInfo hostMemoryInfo)
    {
        ProductsHostReportDTO productHostReportDTO = new ProductsHostReportDTO();
        productHostReportDTO.setAssignedMemory(hostMemoryInfo.getUsedMemory());
        productHostReportDTO.setTotalMemory(hostMemoryInfo.getTotalMemory());
        productHostReportDTO.setAssignedMemoryPercentage(hostMemoryInfo.getPercentageUsedMemory());
        productHostReportDTO.setName(hostMemoryInfo.getHostName());
        return productHostReportDTO;
    }

    @Override
    public ProductsAssignedResourcesReport getProductsAssignedResourcesReport(String environment, String uuaa, Integer pageSize, Integer pageNumber)
    {
        try
        {
            LOG.debug("[ProductsHostsReportDTO] -> [getProductsAssignedResourcesReport]: Running for params: environment: [{}], uuaa: [{}], pageSize: [{}], pageNumber: [{}]",
                    environment, uuaa, pageSize, pageNumber);

            // Set default values
            final int DEFAULT_PAGESIZE = 10;
            final int DEFAULT_PAGENUMBER = 0;
            if (pageNumber == null || pageNumber < 0)
            {
                pageNumber = DEFAULT_PAGENUMBER;
            }
            if (pageSize == null || pageSize < 1)
            {
                pageSize = DEFAULT_PAGESIZE;
            }

            String filteredEnvironment = environment != null && !"".equals(environment) && !"ALL".equals(environment) ? environment : Environment.PRO.getEnvironment();
            String filteredUuaa = uuaa != null && !"".equals(uuaa) && !"ALL".equals(uuaa) ? uuaa : null;

            // Getting information from repositories

            LOG.debug("[ProductsHostsReportDTO] -> [getProductsAssignedResourcesReport]: Obtaining products from assigned resources report filters pageable: environment: [{}], uuaa: [{}], pageNumber: [{}], pageSize: [{}] ", filteredEnvironment, filteredUuaa, pageNumber, pageSize);
            int pageOffset = (pageNumber * pageSize);
            final List<Object[]> rows = this.productReportRepository.findProductsByAssignedResourcesReportFiltersPageable(filteredEnvironment, filteredUuaa, pageSize, pageOffset);

            // Wrapping information in DTO to return
            ProductsAssignedResourcesReport productsAssignedResourcesReport = serviceReportMapper.fromEntityToDto(rows);
            productsAssignedResourcesReport.setPageNumber(pageNumber);
            productsAssignedResourcesReport.setPageSize(pageSize);

            return productsAssignedResourcesReport;

        }
        catch (Exception e)
        {
            throw new NovaException(ProductsAPIError.getProductsAssignedResourcesReportError(e.getMessage(), uuaa, environment));
        }
    }

    @Override
    public byte[] getProductsAssignedResourcesReportExport(final String environment, final String uuaa, final String format)
    {
        final String filteredUuaaValue = uuaa != null && !"".equals(uuaa) && !"ALL".equals(uuaa) ? uuaa.toUpperCase() : null;

        final Environment filteredEnvironment = environment != null && !"".equals(environment) && !"ALL".equals(environment) ? Environment.valueOf(environment.toUpperCase()) : null;

        final List<ProductCSV> productCSVList = this.productReportRepository.findProductsCsv();

        for (ProductCSV productCSV : productCSVList)
        {
            TOSubsystemDTO subsystemDTO = this.toolsClient.getSubsystemById(productCSV.getSubsystemId());

            productCSV.setSubsystemType(SubsystemType.getValueOf(subsystemDTO.getSubsystemType()));
            productCSV.setSubsystemname(subsystemDTO.getSubsystemName());
        }

        final List<String[]> valuesList = productCSVList.stream()
                .filter(p -> filteredUuaaValue == null || filteredUuaaValue.equalsIgnoreCase(p.getUuaa()))
                .filter(p -> filteredEnvironment == null || filteredEnvironment.equals(p.getEnvironment()))
                .map(ProductCSV::getCSVValues)
                .collect(Collectors.toList());

        return ExportDataUtils.exportValuesTo(format, HEADER_CSV, valuesList);
    }

    /**
     * Get an array of Products and its resources if the Product has budget for requesting new HW
     *
     * @param productList List of Products to be processed
     * @param environment environment of the Product to query
     * @return Get an array of Products and its resources if the Product has budget for requesting new HW by environment
     */
    private List<String[]> getProductResourcesReportByEnvironment(List<Product> productList, String environment)
    {
        // for each product, call to budget
        final List<String[]> rowList = new ArrayList<>();

        String[] values;
        for (Product product : productList)
        {
            BUDGProductBudgetsDTO productBudget = this.budgetsService.getProductBudgets(product.getId(), environment);

            Double availableAmount = productBudget.getHardwareBudget().getAvailableAmount();

            if (Double.compare(availableAmount, 0.0) > 0)
            {
                values = new String[6];
                values[0] = product.getName();
                values[1] = product.getUuaa();
                values[2] = environment;
                values[3] = String.valueOf(productBudget.getHardwareBudget().getTotalAmount().intValue());
                values[4] = String.valueOf(availableAmount.intValue());

                double packRam = (availableAmount / 500) * 4;
                values[5] = String.valueOf((int) packRam);
                rowList.add(values);
            }
        }

        return rowList;
    }

    /**
     * Get a ProductsFilesystemsUsageReportDTO from FilesystemsUsageReportDTO
     *
     * @param filesystemsUsageReportDTO FilesystemsUsageReportDTO
     * @return ProductsFilesystemsUsageReportDTO
     */
    private ProductsFilesystemsUsageReportDTO buildDTO(FilesystemsUsageReportDTO filesystemsUsageReportDTO)
    {
        ProductsFilesystemsUsageReportDTO productsFilesystemsUsageReportDTO = new ProductsFilesystemsUsageReportDTO();
        BeanUtils.copyProperties(filesystemsUsageReportDTO, productsFilesystemsUsageReportDTO);
        return productsFilesystemsUsageReportDTO;
    }

}
