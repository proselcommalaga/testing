package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl;

import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGBudgetDTO;
import com.bbva.enoa.apirestgen.budgetsapi.model.BUDGProductBudgetsDTO;
import com.bbva.enoa.apirestgen.deploymentmanagerapi.model.HostMemoryInfo;
import com.bbva.enoa.apirestgen.filesystemsapi.model.FilesystemsUsageReportDTO;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBProductsUsedResourcesReportDTO;
import com.bbva.enoa.apirestgen.productsapi.model.*;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import com.bbva.enoa.datamodel.model.deployment.entities.DeploymentPlan;
import com.bbva.enoa.datamodel.model.deployment.enumerates.DeploymentStatus;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.release.entities.CPD;
import com.bbva.enoa.datamodel.model.release.entities.Release;
import com.bbva.enoa.datamodel.model.release.entities.ReleaseVersion;
import com.bbva.enoa.datamodel.model.release.enumerates.ReleaseVersionStatus;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductReportRepository;
import com.bbva.enoa.platformservices.coreservice.common.repositories.ProductRepository;
import com.bbva.enoa.platformservices.coreservice.common.view.ProductCSV;
import com.bbva.enoa.platformservices.coreservice.consumers.impl.ReportDeploymentManagerClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.filesystemsapi.services.interfaces.IFilesystemsService;
import com.bbva.enoa.platformservices.coreservice.productsapi.autoconfig.ProductProperties;
import com.bbva.enoa.platformservices.coreservice.productsapi.services.impl.mapper.ProductsAssignedResourcesReportMapper;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.bbva.enoa.platformservices.coreservice.common.util.ExportDataUtils.FORMAT_CSV;
import static com.bbva.enoa.platformservices.coreservice.productsapi.services.impl.dummy.DummyProductAssignedResourcesGenerator.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductReportServiceTest
{
    private static final double BYTES_IN_MEGABYTE = 1024D * 1024D;
    private static final double BYTES_IN_GIGABYTE = BYTES_IN_MEGABYTE * 1024D;
    private static final int PAGE_SIZE = 5;
    private static final int PAGE_NUMBER = 1;
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String[] ENVIRONMENTS = new String[]{"INT", "PRE", "PRO"};
    private static final String HEADER_RESOURCES = "Nombre del producto;UUAA;Entorno;Presupuesto HW;Presupuesto HW disponible;Memoria pendiente (GB)";

    @Mock
    private ProductReportRepository productReportRepository;

    @Mock
    private ReportDeploymentManagerClient reportDeploymentManagerClient;

    @Mock
    private ProductProperties productProperties;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IBudgetsService budgetsService;

    @Mock
    private IFilesystemsService filesystemsService;

    @Mock
    private IToolsClient toolsClient;

    @Mock
    private ProductsAssignedResourcesReportMapper mapper;

    @InjectMocks
    private ProductReportService productReportService;

    @Test
    public void getProductsAssignedResourcesReportCSVTest()
    {
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String      uuaa        = RandomStringUtils.randomAlphabetic(4);

        ProductCSV[] productCSV = new ProductCSV[]{this.generateProductCSV(uuaa, environment)};
        TOSubsystemDTO toSubsystemDTO = this.generateTOSubsystemDTO();

        when(this.toolsClient.getSubsystemById(anyInt())).thenReturn(toSubsystemDTO);
        when(this.productReportRepository.findProductsCsv()).thenReturn(Arrays.asList(productCSV));

        byte[] returned = this.productReportService.getProductsAssignedResourcesReportExport("ALL", null, FORMAT_CSV);

        verify(this.productReportRepository, times(1)).findProductsCsv();
        verify(this.toolsClient, times(productCSV.length)).getSubsystemById(productCSV[0].getSubsystemId());

        assertEquals(new String(returned), "NAME;UUAA;DESCRIPTION;ENVIRONMENT;RELEASE_NAME;VERSION_NAME;EXECUTION_DATE;SUBSYSTEM_NAME;SUBSYSTEM_TYPE;SERVICE_NAME;SERVICE_TYPE;INSTANCES;NUM_CPU;RAM_MB\n" +
                "NAME;" + uuaa + ";null;" + environment.name() + ";RELEASENAME;null;;SUBSYSTEMNAME;NOVA;SERVICENAME;NOVA;0;0.0;0\n");
    }

    @Test
    public void getProductsAssignedResourcesReportCSVWithUUAAAndEnvironmentTest()
    {
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        ProductCSV[] productCSV = new ProductCSV[]{this.generateProductCSV(uuaa, environment)};
        TOSubsystemDTO toSubsystemDTO = this.generateTOSubsystemDTO();

        when(this.toolsClient.getSubsystemById(anyInt())).thenReturn(toSubsystemDTO);
        when(this.productReportRepository.findProductsCsv()).thenReturn(Arrays.asList(productCSV));

        byte[] returned = this.productReportService.getProductsAssignedResourcesReportExport(environment.name(), uuaa, FORMAT_CSV);

        verify(this.productReportRepository, times(1)).findProductsCsv();
        verify(this.toolsClient, times(productCSV.length)).getSubsystemById(productCSV[0].getSubsystemId());

        assertEquals(new String(returned), "NAME;UUAA;DESCRIPTION;ENVIRONMENT;RELEASE_NAME;VERSION_NAME;EXECUTION_DATE;SUBSYSTEM_NAME;SUBSYSTEM_TYPE;SERVICE_NAME;SERVICE_TYPE;INSTANCES;NUM_CPU;RAM_MB\n" +
                "NAME;" + uuaa + ";null;" + environment.name() + ";RELEASENAME;null;;SUBSYSTEMNAME;NOVA;SERVICENAME;NOVA;0;0.0;0\n");
    }

    @Test
    public void getProductsAssignedResourcesReportCSVWrongSubsystemTypeTest()
    {
        final Environment environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)];
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        ProductCSV[] productCSV = new ProductCSV[]{this.generateProductCSV(uuaa, environment)};
        TOSubsystemDTO toSubsystemDTO = this.generateTOSubsystemDTO();

        when(this.productReportRepository.findProductsCsv()).thenReturn(Arrays.asList(productCSV));
        when(this.toolsClient.getSubsystemById(productCSV[0].getSubsystemId())).thenReturn(toSubsystemDTO);

        byte[] returned = this.productReportService.getProductsAssignedResourcesReportExport(null, "ALL", FORMAT_CSV);

        verify(this.productReportRepository, times(1)).findProductsCsv();
        verify(this.toolsClient, times(productCSV.length)).getSubsystemById(productCSV[0].getSubsystemId());

        assertEquals(new String(returned), "NAME;UUAA;DESCRIPTION;ENVIRONMENT;RELEASE_NAME;VERSION_NAME;EXECUTION_DATE;SUBSYSTEM_NAME;SUBSYSTEM_TYPE;SERVICE_NAME;SERVICE_TYPE;INSTANCES;NUM_CPU;RAM_MB\n" +
                "NAME;" + uuaa + ";null;" + environment.name() + ";RELEASENAME;null;;SUBSYSTEMNAME;NOVA;SERVICENAME;NOVA;0;0.0;0\n");
    }

    @Test
    public void getProductsHostsReportExport()
    {
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        when(productProperties.getCdpInt()).thenReturn("INT");
        when(productProperties.getCdpPre()).thenReturn("PRE");
        when(productProperties.getCdpPro()).thenReturn("PRO");
        when(reportDeploymentManagerClient.reportHost(eq("INT"))).thenReturn("VALUES_INT");
        when(reportDeploymentManagerClient.reportHost(eq("PRE"))).thenReturn("VALUES_PRE");
        when(reportDeploymentManagerClient.reportHost(eq("PRO"))).thenReturn("VALUES_PRO");

        byte[] result = this.productReportService.getProductsHostsReportExport(environment, uuaa, FORMAT_CSV);
        assertNotNull(result);
    }

    @Test
    public void getProductsUsedResourcesReportTest()
    {

        TOSubsystemDTO toSubsystemDTO = this.generateTOSubsystemDTO();
        PBProductsUsedResourcesReportDTO pbProductsUsedResourcesReportDTO = this.generatePBProductsUsedResourcesReportDTO();
        toSubsystemDTO.setSubsystemType("NONOVATYPE");

        when(this.productRepository.findProductIdsByUuaa(anyString())).thenReturn(new Long[]{1L});
        when(this.budgetsService.getProductsUsedResourcesReport(any(long[].class), anyString())).thenReturn(pbProductsUsedResourcesReportDTO);

        ProductsUsedResourcesReportDTO productsUsedResourcesReportDTOReturned = this.productReportService.getProductsUsedResourcesReport("PRO", "UUAA");

        verify(this.productRepository, times(1)).findProductIdsByUuaa("UUAA");
        verify(this.budgetsService, times(1)).getProductsUsedResourcesReport(new long[]{1L}, "PRO");

        assertEquals(productsUsedResourcesReportDTOReturned.getAvailableHW(), pbProductsUsedResourcesReportDTO.getAvailableHW());
        assertEquals(productsUsedResourcesReportDTOReturned.getAvailableMemory(), pbProductsUsedResourcesReportDTO.getAvailableMemory());
        assertEquals(productsUsedResourcesReportDTOReturned.getTotalHW(), pbProductsUsedResourcesReportDTO.getTotalHW());
        assertEquals(productsUsedResourcesReportDTOReturned.getUsedHW(), pbProductsUsedResourcesReportDTO.getUsedHW());

    }

    @Test
    public void when_hosts_memory_info_are_recovered_then_return_mapped_dto()
    {
        when(this.productProperties.getCdpInt()).thenReturn("TC_INT");
        when(this.reportDeploymentManagerClient.getProductsHostsReport(anyString(), anyString())).thenReturn(getDummyHostMemoryInfos());

        final ProductsHostsReportDTO mappedResults = productReportService.getProductsHostsReport(Environment.INT.getEnvironment(), "TC_INT");

        final Double totalMemory = mappedResults.getTotalMemory();
        final Double assignedMemory = mappedResults.getAssignedMemory();
        assertEquals(20D, totalMemory, 0.5D);
        assertEquals(4D + (800D * BYTES_IN_MEGABYTE / BYTES_IN_GIGABYTE), assignedMemory, 0.5D);
        assertEquals(20D, mappedResults.getAssignedMemoryPercentage(), 0.5D);
        final ProductsHostReportDTO[] machines = mappedResults.getMachines();
        assertEquals(3, machines.length);
        assertEquals(0.5D, machines[0].getAssignedMemory(), 0.1D);
        assertEquals(1D, machines[0].getTotalMemory(), 0.1D);
        assertEquals(50D, machines[0].getAssignedMemoryPercentage(), 0.1D);
        assertEquals(4D, machines[1].getAssignedMemory(), 0.1D);
        assertEquals(16D, machines[1].getTotalMemory(), 0.1D);
        assertEquals(25D, machines[1].getAssignedMemoryPercentage(), 0.1D);
        assertEquals(0.3D, machines[2].getAssignedMemory(), 0.1D);
        assertEquals(3D, machines[2].getTotalMemory(), 0.1D);
        assertEquals(10D, machines[2].getAssignedMemoryPercentage(), 0.1D);
    }

    @Test
    public void when_products_assigned_resources_are_obtained_then_return_mapped_results()
    {
        final List<Object[]> reportDatabaseResults = getDummyDatabaseResults();
        int reportDatabaseSize = reportDatabaseResults.size();
        Object[] firstRecord = reportDatabaseResults.get(0);
        double expectedTotalCpus = (Double) firstRecord[6];
        long expectedTotalInstances = ((BigDecimal) firstRecord[7]).longValue();
        double expectedTotalRam = ((BigDecimal) firstRecord[8]).doubleValue() / 1000;
        int expectedTotalElementsCount = ((BigInteger) firstRecord[9]).intValue();
        when(productReportRepository.findProductsByAssignedResourcesReportFiltersPageable(anyString(), anyString(), anyInt(), anyInt())).thenReturn(reportDatabaseResults);

        when(mapper.fromEntityToDto(anyList())).thenReturn(getDummyProductsAssignedResourcesReport());

        final ProductsAssignedResourcesReport results = productReportService.getProductsAssignedResourcesReport("", "FLOW", PAGE_SIZE, PAGE_NUMBER);

        assertEquals(expectedTotalElementsCount, results.getTotalElementsCount());
        assertEquals(expectedTotalCpus, results.getTotalCPUs());
        assertEquals(expectedTotalInstances, results.getTotalInstances().longValue());
        assertEquals(expectedTotalRam, results.getTotalMemory().doubleValue());
        assertEquals(reportDatabaseSize, results.getTotalServices().intValue());
        assertEquals(reportDatabaseSize, results.getServices().length);

        final ProductServiceAssignedResourcesReportDTO[] services = results.getServices();
        Date date = new Date();
        for (int i = 1; i < services.length; i++)
        {
            final Object[] record = getRecordAtPosition(i);
            assertEquals(record[0], services[i].getProductName());
            assertEquals(record[1], services[i].getReleaseName());
            assertEquals(record[2], services[i].getReleaseVersionName());
            date.setTime(((Timestamp) record[3]).getTime());
            assertEquals(DATE_FORMAT.format(date), services[i].getExecutionDate());
            assertEquals(record[4], services[i].getSubsystemName());
            assertEquals(record[5], services[i].getServiceName());
            assertEquals(record[6], services[i].getCpus());
            assertEquals(((BigDecimal) record[7]).longValue(), services[i].getInstances());
            assertEquals(((BigDecimal) record[8]).doubleValue() / 1000, services[i].getMemory(), 0.1D);
        }
    }

    @Test
    public void when_empty_products_assigned_resources_are_obtained_then_return_empty_mapped_results()
    {
        when(productReportRepository.findProductsByAssignedResourcesReportFiltersPageable(anyString(), anyString(), anyInt(), anyInt())).thenReturn(Collections.emptyList());

        ProductsAssignedResourcesReport expected = new ProductsAssignedResourcesReport();
        when(mapper.fromEntityToDto(anyList())).thenReturn(expected);

        final ProductsAssignedResourcesReport results = productReportService.getProductsAssignedResourcesReport("", "FLOW", PAGE_SIZE, PAGE_NUMBER);

        assertEquals(expected, results);
    }

    @Test
    public void when_error_in_products_assigned_resources_are_throw_then_return_nova_exception()
    {
        when(this.productReportRepository.findProductsByAssignedResourcesReportFiltersPageable(anyString(), anyString(), anyInt(), anyInt())).thenReturn(null);
        assertThrows(NovaException.class, () -> productReportService.getProductsAssignedResourcesReport("", "FLOW", PAGE_SIZE, PAGE_NUMBER));

    }

    @Test
    public void getAllProductExportCSVTest()
    {
        final String environment = ENVIRONMENTS[RandomUtils.nextInt(0, ENVIRONMENTS.length)];
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        when(this.productRepository.findByUuaa(anyString())).thenReturn(getDummyProducts());
        when(this.budgetsService.getProductBudgets(anyInt(), anyString())).thenReturn(getDummyBudgets());

        byte[] response = this.productReportService.getProductsUsedResourcesReportExport(environment, uuaa, FORMAT_CSV);
        String[] responseTokens = new String(response).split("\n");
        for (int i = 0; i < responseTokens.length; i++)
        {
            String trimmedToken = responseTokens[i].trim();
            if (i == 0)
            {
                assertEquals(HEADER_RESOURCES, trimmedToken);
            }
            else
            {
                final int suffix = ((i - 1) % 3) + 1;
                String expectedString = "Name_" + suffix + ";UUAA_" + suffix + ";" + environment + ";35;100;0";
                assertEquals(expectedString, trimmedToken);
            }

        }
        assertNotNull(response);
    }

    @Test
    public void getProductResourcesReportTest()
    {
        List<Product> productList = Collections.singletonList(this.generateCompleteProduct());

        BUDGProductBudgetsDTO productBudget = this.generateProductBudgetsDTO();

        when(this.productRepository.findAll()).thenReturn(productList);

        when(this.budgetsService.getProductBudgets(anyInt(), anyString())).thenReturn(productBudget);
        when(this.budgetsService.getProductBudgets(anyInt(), anyString())).thenReturn(productBudget);
        when(this.budgetsService.getProductBudgets(anyInt(), anyString())).thenReturn(productBudget);

        byte[] response = this.productReportService.getProductsUsedResourcesReportExport(null, "ALL", FORMAT_CSV);

        verify(this.productRepository, times(1)).findAll();
        verify(this.budgetsService, times(1)).getProductBudgets(1, "INT");
        verify(this.budgetsService, times(1)).getProductBudgets(1, "PRE");
        verify(this.budgetsService, times(1)).getProductBudgets(1, "PRO");

        assertNotNull(response);
        assertEquals("Nombre del producto;UUAA;Entorno;Presupuesto HW;Presupuesto HW disponible;Memoria pendiente (GB)\n" + "NAME;null;INT;100;10;0\n" + "NAME;null;PRE;100;10;0\n" + "NAME;null;PRO;100;10;0", new String(response).trim());
    }

    @Test
    public void getProductResourcesReportWithUUAAAndEnvironmentTest()
    {
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String uuaa = RandomStringUtils.randomAlphabetic(4).toUpperCase();

        List<Product> productList = Collections.singletonList(this.generateCompleteProduct());

        BUDGProductBudgetsDTO productBudget = this.generateProductBudgetsDTO();

        when(this.productRepository.findByUuaa(eq(uuaa))).thenReturn(productList);

        when(this.budgetsService.getProductBudgets(anyInt(), eq(environment))).thenReturn(productBudget);

        byte[] response = this.productReportService.getProductsUsedResourcesReportExport(environment, uuaa, FORMAT_CSV);

        verify(this.productRepository, times(1)).findByUuaa(eq(uuaa));
        verify(this.budgetsService, times(1)).getProductBudgets(eq(1), eq(environment));

        assertNotNull(response);
        assertEquals("Nombre del producto;UUAA;Entorno;Presupuesto HW;Presupuesto HW disponible;Memoria pendiente (GB)\n" + "NAME;null;" + environment + ";100;10;0", new String(response).trim());
    }

    @Test
    public void getProductsFilesytemsUsageReportTest()
    {
        FilesystemsUsageReportDTO filesystemsUsageReportDTO = this.generateFilesystemsUsageReportDTO();

        when(this.productRepository.findProductIdByUuaa(anyString())).thenReturn(1);
        when(this.filesystemsService.getFilesystemsUsageReport(anyInt(), anyString())).thenReturn(filesystemsUsageReportDTO);

        ProductsFilesystemsUsageReportDTO productsFilesystemsUsageReportDTO
                = this.productReportService.getProductsFilesytemsUsageReport("PRO", "UUAA");

        assertNotNull(productsFilesystemsUsageReportDTO);
        assertEquals(filesystemsUsageReportDTO.getTotalStorageAssigned(), productsFilesystemsUsageReportDTO.getTotalStorageAssigned());
        assertEquals(filesystemsUsageReportDTO.getTotalStorageAssignedPercentage(), productsFilesystemsUsageReportDTO.getTotalStorageAssignedPercentage());
        assertEquals(filesystemsUsageReportDTO.getTotalStorageAvailable(), productsFilesystemsUsageReportDTO.getTotalStorageAvailable());
        assertEquals(filesystemsUsageReportDTO.getTotalStorageAvailablePercentage(), productsFilesystemsUsageReportDTO.getTotalStorageAvailablePercentage());
    }

    private FilesystemsUsageReportDTO generateFilesystemsUsageReportDTO()
    {
        FilesystemsUsageReportDTO filesystemsUsageReportDTO = new FilesystemsUsageReportDTO();
        filesystemsUsageReportDTO.fillRandomly(2, false, 0, 3);


        return filesystemsUsageReportDTO;

    }


    private HostMemoryInfo[] getDummyHostMemoryInfos()
    {
        HostMemoryInfo[] hostMemoryInfos = new HostMemoryInfo[3];
        HostMemoryInfo hostMemoryInfo = new HostMemoryInfo();
        hostMemoryInfo.setHostName("host1");
        hostMemoryInfo.setTotalMemory(1D);
        hostMemoryInfo.setUsedMemory(0.5D);
        hostMemoryInfo.setPercentageUsedMemory(50D);
        hostMemoryInfos[0] = hostMemoryInfo;
        hostMemoryInfo = new HostMemoryInfo();
        hostMemoryInfo.setHostName("host2");
        hostMemoryInfo.setTotalMemory(16D);
        hostMemoryInfo.setUsedMemory(4D);
        hostMemoryInfo.setPercentageUsedMemory(25D);
        hostMemoryInfos[1] = hostMemoryInfo;
        hostMemoryInfo = new HostMemoryInfo();
        hostMemoryInfo.setHostName("host3");
        hostMemoryInfo.setTotalMemory(3D);
        hostMemoryInfo.setUsedMemory(0.3D);
        hostMemoryInfo.setPercentageUsedMemory(10D);
        hostMemoryInfos[2] = hostMemoryInfo;
        return hostMemoryInfos;
    }

    private ProductCSV generateProductCSV(final String uuaa, final Environment environment)
    {
        ProductCSV productToReturn = new ProductCSV("null", "null", "null", null, "null", "null", null, "null", ServiceType.NOVA, 0, 0, 0, 0);
        productToReturn.setName("NAME");
        productToReturn.setReleasename("RELEASENAME");
        productToReturn.setEnvironment(environment);
        productToReturn.setServiceName("SERVICENAME");
        productToReturn.setUuaa(uuaa);
        productToReturn.setSubsystemId(1);
        productToReturn.setSubsystemname("SUBSYSTEMNAME");
        productToReturn.setSubsystemType(SubsystemType.EPHOENIX);
        return productToReturn;
    }

    private TOSubsystemDTO generateTOSubsystemDTO()
    {
        TOSubsystemDTO toSubsystemDTO = new TOSubsystemDTO();
        toSubsystemDTO.fillRandomly(2, false, 0, 3);
        toSubsystemDTO.setSubsystemType("NOVA");
        toSubsystemDTO.setSubsystemName("SUBSYSTEMNAME");

        return toSubsystemDTO;
    }

    private PBProductsUsedResourcesReportDTO generatePBProductsUsedResourcesReportDTO()
    {
        PBProductsUsedResourcesReportDTO pbProductsUsedResourcesReportDTO = new PBProductsUsedResourcesReportDTO();
        pbProductsUsedResourcesReportDTO.fillRandomly(2, false, 0, 3);

        return pbProductsUsedResourcesReportDTO;
    }

    private Product generateCompleteProduct()
    {
        // Product generated with release, release version and plan related

        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.setId(1);
        deploymentPlan.setStatus(DeploymentStatus.DEPLOYED);
        List<DeploymentPlan> dpList = new ArrayList<>();
        dpList.add(deploymentPlan);

        ReleaseVersion releaseVersion = new ReleaseVersion();
        releaseVersion.setId(2);
        releaseVersion.setVersionName("VersionNameTest");
        releaseVersion.setStatus(ReleaseVersionStatus.READY_TO_DEPLOY);
        releaseVersion.setDeployments(dpList);
        List<ReleaseVersion> rvList = new ArrayList<>();
        rvList.add(releaseVersion);

        Release release = new Release();
        release.setId(3);
        release.setName("ReleaseName");
        release.setReleaseVersions(rvList);
        List<Release> rList = new ArrayList<>();
        rList.add(release);

        Product productToReturn = new Product();
        productToReturn.setName("NAME");
        productToReturn.setId(1);
        productToReturn.setCPDInPro(this.generateCpd());
        productToReturn.setCriticalityLevel(0);
        productToReturn.setImage("IMAGE");
        productToReturn.setReleases(rList);
        release.setProduct(productToReturn);


        return productToReturn;
    }

    private CPD generateCpd()
    {

        CPD cpd = new CPD();
        cpd.setId(1);
        cpd.setActive(true);
        cpd.setAddress("ADRESS");
        cpd.setElasticSearchCPDName("CPDNAME");
        cpd.setEnvironment(Environment.PRO.getEnvironment());
        cpd.setFilesystem("FILESYSTEM");
        cpd.setLabel("LABEL");
        cpd.setName("NAME");
        cpd.setRegistry("REGISTRY");


        return cpd;
    }

    private BUDGProductBudgetsDTO generateProductBudgetsDTO()
    {
        BUDGProductBudgetsDTO productBudget = new BUDGProductBudgetsDTO();
        productBudget.fillRandomly(2, false, 0, 3);
        BUDGBudgetDTO budget = new BUDGBudgetDTO();
        budget.fillRandomly(2, false, 0, 3);
        budget.setAvailableAmount(10.0);
        budget.setTotalAmount(100.0);
        productBudget.setHardwareBudget(budget);

        return productBudget;
    }

    private List<Product> getDummyProducts()
    {
        List<Product> products = new ArrayList<>();
        for (int i = 0; i < 3; i++)
        {
            Product product = new Product();
            product.setId((i + 1));
            product.setUuaa("UUAA_" + (i + 1));
            product.setName("Name_" + (i + 1));
            products.add(product);
        }
        return products;
    }

    private BUDGProductBudgetsDTO getDummyBudgets()
    {
        BUDGProductBudgetsDTO dto = new BUDGProductBudgetsDTO();
        BUDGBudgetDTO hardwareBudget = new BUDGBudgetDTO();
        hardwareBudget.setAvailableAmount(100D);
        hardwareBudget.setTotalAmount(35D);
        dto.setHardwareBudget(hardwareBudget);
        return dto;
    }
}