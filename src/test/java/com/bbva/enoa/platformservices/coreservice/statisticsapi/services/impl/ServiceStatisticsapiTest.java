package com.bbva.enoa.platformservices.coreservice.statisticsapi.services.impl;

import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBAvailabilityNovaCoinsDTO;
import com.bbva.enoa.apirestgen.productbudgetsapi.model.PBHardwareBudgetSnapshot;
import com.bbva.enoa.apirestgen.statisticsapi.model.*;
import com.bbva.enoa.apirestgen.statisticsuserapi.model.UserProductRoleHistoryDTO;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemsCombinationDTO;
import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.datamodel.model.broker.entities.Broker;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerStatus;
import com.bbva.enoa.datamodel.model.broker.enumerates.BrokerType;
import com.bbva.enoa.datamodel.model.common.enumerates.Platform;
import com.bbva.enoa.datamodel.model.product.entities.Product;
import com.bbva.enoa.datamodel.model.statistic.enumerates.StatisticParamName;
import com.bbva.enoa.datamodel.model.statistic.enumerates.StatisticType;
import com.bbva.enoa.platformservices.coreservice.budgetsapi.services.interfaces.IProductBudgetsService;
import com.bbva.enoa.platformservices.coreservice.common.enums.ServiceGroupingNames;
import com.bbva.enoa.platformservices.coreservice.common.repositories.*;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IToolsClient;
import com.bbva.enoa.platformservices.coreservice.consumers.interfaces.IUserStatisticsClient;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.ServiceTypeGroupProvider;
import com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.ValidDateRangeProvider;
import com.bbva.enoa.utils.codegeneratorutils.exception.NovaException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static com.bbva.enoa.platformservices.coreservice.common.util.ExportDataUtils.*;
import static com.bbva.enoa.platformservices.coreservice.statisticsapi.utils.StatisticsConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ServiceStatisticsapiTest
{
    @Mock
    private StatisticsRepository statisticsRepository;

    @Mock
    private StatisticRepository statisticRepository;

    @Mock
    private ReleaseVersionRepository releaseVersionRepository;

    @Mock
    private BrokerRepository brokerRepository;

    @Mock
    private IProductBudgetsService productBudgetsService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IToolsClient toolsClient;

    @Mock
    private ValidDateRangeProvider validDateRangeProvider;

    @Mock
    private IUserStatisticsClient userStatisticsClient;

    @Mock
    private ApiVersionRepository apiVersionRepository;

    @InjectMocks
    private ServiceStatisticsapi serviceStatisticsapi;

    private static List<Object[]> buildMapOfHistoricalRecords(final boolean hasDecimal)
    {
        final int size = RandomUtils.nextInt(0, 12);
        final List<Object[]> mockRecords = new ArrayList<>(size);
        Object[] objectArray;
        int month = RandomUtils.nextInt(10, 13);
        int year = RandomUtils.nextInt(2000, 2023);
        for (int i = 0; i < size; i++)
        {
            objectArray = new Object[3];
            objectArray[0] = (i + 10) + "/" + month + "/" + year;
            if (hasDecimal)
            {
                objectArray[1] = RandomUtils.nextDouble(0, 15789);
            }
            else
            {
                objectArray[1] = RandomUtils.nextLong(0, 15789);
            }
            objectArray[2] = RandomStringUtils.randomAlphabetic(5);
            mockRecords.add(objectArray);
        }

        return mockRecords;
    }

    private static List<Object[]> buildMapOfHistoricalBrokerRecords(final boolean hasDecimal)
    {
        final int size = RandomUtils.nextInt(0, 12);
        final List<Object[]> mockRecords = new ArrayList<>(size);
        Object[] objectArray;
        int month = RandomUtils.nextInt(10, 13);
        int year = RandomUtils.nextInt(2000, 2023);
        for (int i = 0; i < size; i++)
        {
            //{"FECHA", "UUAA", "ENTORNO", "TIPO", "PLATAFORMA", "ESTADO", "VALOR"}
            objectArray = new Object[7];
            objectArray[0] = (i + 10) + "/" + month + "/" + year;
            objectArray[1] = RandomStringUtils.randomAlphabetic(4);
            objectArray[2] = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
            objectArray[3] = BrokerType.values()[RandomUtils.nextInt(0, BrokerType.values().length)].name();
            objectArray[4] = Platform.values()[RandomUtils.nextInt(0, Platform.values().length)].name();
            objectArray[5] = BrokerStatus.values()[RandomUtils.nextInt(0, BrokerStatus.values().length)].name();
            if (hasDecimal)
            {
                objectArray[6] = RandomUtils.nextDouble(0, 15789);
            }
            else
            {
                objectArray[6] = RandomUtils.nextLong(0, 15789);
            }
            mockRecords.add(objectArray);
        }

        return mockRecords;
    }

    private static void assertRecordsOfCSVHistorical(final List<Object[]> mockRecords, final byte[] response, final boolean hasDecimal)
    {
        final String[] responseLines = new String(response).split(CSV_NEW_LINE);
        Assertions.assertEquals(mockRecords.size() + 1, responseLines.length);
        Assertions.assertEquals(EXPORT_HEADER[0] + CSV_SEPARATOR + EXPORT_HEADER[1], responseLines[0]);
        String value;
        for (int i = 0; i < mockRecords.size(); i++)
        {
            value = hasDecimal ? BigDecimal.valueOf((Double) mockRecords.get(i)[1]).setScale(2, RoundingMode.HALF_EVEN).toString() : String.valueOf(mockRecords.get(i)[1]);

            Assertions.assertEquals(mockRecords.get(i)[0] + CSV_SEPARATOR + value, responseLines[i + 1]);
        }
    }

    private static void assertRecordsOfCSVHistoricalCategory(final List<Object[]> mockRecords, final byte[] response, final boolean hasDecimal)
    {
        final String[] responseLines = new String(response).split(CSV_NEW_LINE);
        Assertions.assertEquals(mockRecords.size() + 1, responseLines.length);
        Assertions.assertEquals(EXPORT_HEADER_CATEGORY[0] + CSV_SEPARATOR + EXPORT_HEADER_CATEGORY[1] + CSV_SEPARATOR + EXPORT_HEADER_CATEGORY[2], responseLines[0]);

        String value;
        for (int i = 0; i < mockRecords.size(); i++)
        {
            value = hasDecimal ? BigDecimal.valueOf((Double) mockRecords.get(i)[1]).setScale(2, RoundingMode.HALF_EVEN).toString() : String.valueOf(mockRecords.get(i)[1]);

            Assertions.assertEquals(mockRecords.get(i)[0] + CSV_SEPARATOR + mockRecords.get(i)[2] + CSV_SEPARATOR + value, responseLines[i + 1]);
        }
    }

    private static void assertRecordsOfBatchCSVHistoricalCategory(final List<Object[]> mockRecords, final byte[] response, final boolean hasDecimal)
    {
        final String[] responseLines = new String(response).split(CSV_NEW_LINE);
        Assertions.assertEquals(mockRecords.size() + 5, responseLines.length);
        Assertions.assertEquals(EXPORT_HEADER_CATEGORY[0] + CSV_SEPARATOR + EXPORT_HEADER_CATEGORY[1] + CSV_SEPARATOR + EXPORT_HEADER_CATEGORY[2], responseLines[0]);

        String value;
        for (int i = 0; i < mockRecords.size() - 3; i++)
        {
            value = hasDecimal ? BigDecimal.valueOf((Double) mockRecords.get(i)[1]).setScale(2, RoundingMode.HALF_EVEN).toString() : String.valueOf(mockRecords.get(i)[1]);

            Assertions.assertEquals(mockRecords.get(i)[0] + CSV_SEPARATOR + mockRecords.get(i)[2] + CSV_SEPARATOR + value, responseLines[i + 1]);
        }
    }

    private static void assertRecordsOfCSVBrokerHistoricalCategory(final List<Object[]> mockRecords, final byte[] response, final boolean hasDecimal)
    {
        final String[] responseLines = new String(response).split(CSV_NEW_LINE);
        Assertions.assertEquals(mockRecords.size() + 1, responseLines.length);
        Assertions.assertEquals(
                EXPORT_HEADER_BROKER[0] + CSV_SEPARATOR +
                        EXPORT_HEADER_BROKER[1] + CSV_SEPARATOR +
                        EXPORT_HEADER_BROKER[2] + CSV_SEPARATOR +
                        EXPORT_HEADER_BROKER[3] + CSV_SEPARATOR +
                        EXPORT_HEADER_BROKER[4] + CSV_SEPARATOR +
                        EXPORT_HEADER_BROKER[5] + CSV_SEPARATOR +
                        EXPORT_HEADER_BROKER[6], responseLines[0]);

        String value;
        for (int i = 0; i < mockRecords.size(); i++)
        {
            value = hasDecimal ? BigDecimal.valueOf((Double) mockRecords.get(i)[6]).setScale(2, RoundingMode.HALF_EVEN).toString() : String.valueOf(mockRecords.get(i)[6]);

            Assertions.assertEquals(mockRecords.get(i)[0] + CSV_SEPARATOR + mockRecords.get(i)[1] + CSV_SEPARATOR + mockRecords.get(i)[2] + CSV_SEPARATOR +
                            mockRecords.get(i)[3] + CSV_SEPARATOR + mockRecords.get(i)[4] + CSV_SEPARATOR + mockRecords.get(i)[5] + CSV_SEPARATOR + value,
                    responseLines[i + 1]);
        }
    }

    private static void assertRecordsOfXLSXHistorical(final List<Object[]> mockRecords, final byte[] response, final boolean hasDecimal) throws Exception
    {
        final Workbook book = WorkbookFactory.create(new ByteArrayInputStream(response));
        final Sheet sheet = book.getSheet(XLSX_SHEET);

        Assertions.assertEquals(mockRecords.size() + 1, sheet.getPhysicalNumberOfRows());

        // header row
        final Row header = sheet.getRow(0);
        Assertions.assertEquals(EXPORT_HEADER[0], header.getCell(0).toString());
        Assertions.assertEquals(EXPORT_HEADER[1], header.getCell(1).toString());

        String mockValue;
        String value;
        for (int i = 0; i < mockRecords.size(); i++)
        {
            mockValue = hasDecimal ? BigDecimal.valueOf((Double) mockRecords.get(i)[1]).setScale(2, RoundingMode.HALF_EVEN).toString() : String.valueOf(mockRecords.get(i)[1]);
            value = hasDecimal ? new BigDecimal(sheet.getRow(i + 1).getCell(1).toString()).toString() : String.valueOf(sheet.getRow(i + 1).getCell(1));


            Assertions.assertEquals(mockRecords.get(i)[0], sheet.getRow(i + 1).getCell(0).toString());
            Assertions.assertEquals(mockValue, value);
        }
    }

    private static void assertRecordsOfXLSXHistoricalCategory(final List<Object[]> mockRecords, final byte[] response, final boolean hasDecimal) throws Exception
    {
        final Workbook book = WorkbookFactory.create(new ByteArrayInputStream(response));
        final Sheet sheet = book.getSheet(XLSX_SHEET);

        Assertions.assertEquals(mockRecords.size() + 1, sheet.getPhysicalNumberOfRows());

        // header row
        final Row header = sheet.getRow(0);
        Assertions.assertEquals(EXPORT_HEADER_CATEGORY[0], header.getCell(0).toString());
        Assertions.assertEquals(EXPORT_HEADER_CATEGORY[1], header.getCell(1).toString());
        Assertions.assertEquals(EXPORT_HEADER_CATEGORY[2], header.getCell(2).toString());

        String mockValue;
        String value;
        for (int i = 0; i < mockRecords.size(); i++)
        {
            mockValue = hasDecimal ? BigDecimal.valueOf((Double) mockRecords.get(i)[1]).setScale(2, RoundingMode.HALF_EVEN).toString() : String.valueOf(mockRecords.get(i)[1]);
            value = hasDecimal ? new BigDecimal(sheet.getRow(i + 1).getCell(2).toString()).toString() : String.valueOf(sheet.getRow(i + 1).getCell(2));

            Assertions.assertEquals(mockRecords.get(i)[0], sheet.getRow(i + 1).getCell(0).toString());
            Assertions.assertEquals(mockRecords.get(i)[2], sheet.getRow(i + 1).getCell(1).toString());
            Assertions.assertEquals(mockValue, value);
        }
    }

    private static void assertRecordsOfXLSXBatchHistoricalCategory(final List<Object[]> mockRecords, final byte[] response, final boolean hasDecimal) throws Exception
    {
        final Workbook book = WorkbookFactory.create(new ByteArrayInputStream(response));
        final Sheet sheet = book.getSheet(XLSX_SHEET);

        Assertions.assertEquals(mockRecords.size() + 5, sheet.getPhysicalNumberOfRows());

        // header row
        final Row header = sheet.getRow(0);
        Assertions.assertEquals(EXPORT_HEADER_CATEGORY[0], header.getCell(0).toString());
        Assertions.assertEquals(EXPORT_HEADER_CATEGORY[1], header.getCell(1).toString());
        Assertions.assertEquals(EXPORT_HEADER_CATEGORY[2], header.getCell(2).toString());

        String mockValue;
        String value;
        for (int i = 0; i < mockRecords.size(); i++)
        {
            mockValue = hasDecimal ? BigDecimal.valueOf((Double) mockRecords.get(i)[1]).setScale(2, RoundingMode.HALF_EVEN).toString() : String.valueOf(mockRecords.get(i)[1]);
            value = hasDecimal ? new BigDecimal(sheet.getRow(i + 1).getCell(2).toString()).toString() : String.valueOf(sheet.getRow(i + 1).getCell(2));

            Assertions.assertEquals(mockRecords.get(i)[0], sheet.getRow(i + 1).getCell(0).toString());
            Assertions.assertEquals(mockRecords.get(i)[2], sheet.getRow(i + 1).getCell(1).toString());
            Assertions.assertEquals(mockValue, value);
        }
    }

    private static void assertRecordsOfXLSXBrokerHistoricalCategory(final List<Object[]> mockRecords, final byte[] response, final boolean hasDecimal) throws Exception
    {
        final Workbook book = WorkbookFactory.create(new ByteArrayInputStream(response));
        final Sheet sheet = book.getSheet(XLSX_SHEET);

        Assertions.assertEquals(mockRecords.size() + 1, sheet.getPhysicalNumberOfRows());

        // header row
        final Row header = sheet.getRow(0);
        Assertions.assertEquals(EXPORT_HEADER_BROKER[0], header.getCell(0).toString());
        Assertions.assertEquals(EXPORT_HEADER_BROKER[1], header.getCell(1).toString());
        Assertions.assertEquals(EXPORT_HEADER_BROKER[2], header.getCell(2).toString());
        Assertions.assertEquals(EXPORT_HEADER_BROKER[3], header.getCell(3).toString());
        Assertions.assertEquals(EXPORT_HEADER_BROKER[4], header.getCell(4).toString());
        Assertions.assertEquals(EXPORT_HEADER_BROKER[5], header.getCell(5).toString());
        Assertions.assertEquals(EXPORT_HEADER_BROKER[6], header.getCell(6).toString());

        String mockValue;
        String value;
        for (int i = 0; i < mockRecords.size(); i++)
        {
            mockValue = hasDecimal ? BigDecimal.valueOf((Double) mockRecords.get(i)[6]).setScale(2, RoundingMode.HALF_EVEN).toString() : String.valueOf(mockRecords.get(i)[6]);
            value = hasDecimal ? new BigDecimal(sheet.getRow(i + 1).getCell(6).toString()).toString() : String.valueOf(sheet.getRow(i + 1).getCell(6));

            Assertions.assertEquals(mockRecords.get(i)[0], sheet.getRow(i + 1).getCell(0).toString());
            Assertions.assertEquals(mockRecords.get(i)[1], sheet.getRow(i + 1).getCell(1).toString());
            Assertions.assertEquals(mockRecords.get(i)[2], sheet.getRow(i + 1).getCell(2).toString());
            Assertions.assertEquals(mockRecords.get(i)[3], sheet.getRow(i + 1).getCell(3).toString());
            Assertions.assertEquals(mockRecords.get(i)[4], sheet.getRow(i + 1).getCell(4).toString());
            Assertions.assertEquals(mockRecords.get(i)[5], sheet.getRow(i + 1).getCell(5).toString());
            Assertions.assertEquals(mockValue, value);
        }
    }

    private static void assertRecordsOfHistoricalCategory(final List<Object[]> mockRecords, final STHistoricalSerie[] response, final boolean hasDecimal)
    {
        Assertions.assertEquals(mockRecords.size(), response.length);

        String value;
        String mockValue;
        for (int i = 0; i < mockRecords.size(); i++)
        {

            mockValue = hasDecimal ? BigDecimal.valueOf((Double) mockRecords.get(i)[1]).setScale(2, RoundingMode.HALF_EVEN).toString() : String.valueOf(mockRecords.get(i)[1]);
            value = BigDecimal.valueOf(response[i].getValue()).setScale(2, RoundingMode.HALF_EVEN).toString();

            Assertions.assertEquals(mockRecords.get(i)[0], response[i].getDate());
            Assertions.assertEquals(mockValue, value);
            Assertions.assertEquals(mockRecords.get(i)[2], response[i].getCategory());
        }
    }

    private static void assertRecordsOfValuesByDateToHistoricalPoint(final List<Object[]> mockRecords, final STHistoricalPoint[] response, final boolean hasDecimal)
    {
        Assertions.assertEquals(mockRecords.size(), response.length);

        String value;
        String mockValue;
        for (int i = 0; i < mockRecords.size(); i++)
        {

            mockValue = hasDecimal ? BigDecimal.valueOf((Double) mockRecords.get(i)[1]).setScale(2, RoundingMode.HALF_EVEN).toString() : String.valueOf(mockRecords.get(i)[1]);
            value = BigDecimal.valueOf(response[i].getValue()).setScale(2, RoundingMode.HALF_EVEN).toString();

            Assertions.assertEquals(mockRecords.get(i)[0], response[i].getDate());
            Assertions.assertEquals(mockValue, value);
        }
    }

    private static void assertRecordsOfCSV(final List<Object[]> mockRecords, final byte[] response, final boolean hasDecimal)
    {
        final String[] responseLines = new String(response).split(CSV_NEW_LINE);
        Assertions.assertEquals(mockRecords.size() + 1, responseLines.length);
        Assertions.assertEquals(EXPORT_HEADER_PRODUCT[0] + CSV_SEPARATOR + EXPORT_HEADER_PRODUCT[1] + CSV_SEPARATOR + EXPORT_HEADER_PRODUCT[2], responseLines[0]);
        String col2;
        String col3;
        for (int i = 0; i < mockRecords.size(); i++)
        {
            col2 = hasDecimal ? BigDecimal.valueOf((Double) mockRecords.get(i)[1]).setScale(2, RoundingMode.HALF_EVEN).toString() : String.valueOf(mockRecords.get(i)[1]);
            col3 = hasDecimal ? BigDecimal.valueOf((Double) mockRecords.get(i)[2]).setScale(2, RoundingMode.HALF_EVEN).toString() : String.valueOf(mockRecords.get(i)[2]);

            Assertions.assertEquals(mockRecords.get(i)[0] + CSV_SEPARATOR + col2 + CSV_SEPARATOR + col3, responseLines[i + 1]);
        }
    }

    private static void assertRecordsOfXLSX(final List<Object[]> mockRecords, final byte[] response, final boolean hasDecimal) throws Exception
    {
        final Workbook book = WorkbookFactory.create(new ByteArrayInputStream(response));
        final Sheet sheet = book.getSheet(XLSX_SHEET);

        Assertions.assertEquals(mockRecords.size() + 1, sheet.getPhysicalNumberOfRows());

        // header row
        final Row header = sheet.getRow(0);
        Assertions.assertEquals(EXPORT_HEADER_PRODUCT[0], header.getCell(0).toString());
        Assertions.assertEquals(EXPORT_HEADER_PRODUCT[1], header.getCell(1).toString());
        Assertions.assertEquals(EXPORT_HEADER_PRODUCT[2], header.getCell(2).toString());

        String mockValue;
        String value;
        for (int i = 0; i < mockRecords.size(); i++)
        {
            mockValue = hasDecimal ? BigDecimal.valueOf((Double) mockRecords.get(i)[1]).setScale(2, RoundingMode.HALF_EVEN).toString() : String.valueOf(mockRecords.get(i)[1]);
            value = hasDecimal ? new BigDecimal(sheet.getRow(i + 1).getCell(1).toString()).toString() : String.valueOf(sheet.getRow(i + 1).getCell(1));


            Assertions.assertEquals(mockRecords.get(i)[0], sheet.getRow(i + 1).getCell(0).toString());
            Assertions.assertEquals(mockValue, value);
        }
    }

    @BeforeEach
    void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getProductsNumber()
    {
        final Long expectedResponse = RandomUtils.nextLong();

        when(this.statisticsRepository.getCountProductsDeployed()).thenReturn(expectedResponse);
        when(this.serviceStatisticsapi.getProductsNumber()).thenReturn(expectedResponse);

        final Long response = this.serviceStatisticsapi.getProductsNumber();
        assertEquals(response, expectedResponse);
    }

    @Test
    void getServicesNumber()
    {
        final Long expectedResponse = RandomUtils.nextLong();

        when(this.statisticsRepository.getCountServicesDeployed()).thenReturn(expectedResponse);
        when(this.serviceStatisticsapi.getServicesNumber()).thenReturn(expectedResponse);

        final Long response = this.serviceStatisticsapi.getServicesNumber();
        assertEquals(response, expectedResponse);
    }

    @Test
    void getApisSummaryTest()
    {
        // given
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String functionality = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        List<Object[]> dummyResultset = new ArrayList<>();
        dummyResultset.add(new Object[]{BigInteger.ONE, "TYPE_A"});
        dummyResultset.add(new Object[]{BigInteger.TWO, "TYPE_B"});

        when(apiVersionRepository.findAllApisSummary(uuaa, environment, functionality)).thenReturn(dummyResultset);

        // when
        final ApiSummaryDTO response = this.serviceStatisticsapi.getApisSummary(environment, functionality, uuaa);

        // then
        Assertions.assertEquals(3L, response.getTotal());
        Assertions.assertEquals(dummyResultset.size(), response.getApis().length);
    }


    @Test
    void when_no_services_are_found_then_return_empty_dto()
    {
        List<Object[]> dummyResultset = new ArrayList<>();
        dummyResultset.add(new Object[]{"", BigInteger.ZERO, BigInteger.ZERO});
        Mockito.when(releaseVersionRepository.findAllNotStoragedElements()).thenReturn(dummyResultset);

        ReleaseVersionSummaryDTO releaseVersionsSummary = serviceStatisticsapi.getReleaseVersionsSummary(null, null, null);

        Assertions.assertEquals(Long.valueOf(0L), releaseVersionsSummary.getTotal());
        Assertions.assertEquals(Long.valueOf(0L), releaseVersionsSummary.getTotalServices());
        Assertions.assertEquals(0, releaseVersionsSummary.getServices().length);
    }

    @Test
    void when_every_service_type_belongs_to_distinct_service_group_then_return_populated_dto()
    {
        List<Object[]> dummyResultset = new ArrayList<>();
        dummyResultset.add(new Object[]{ServiceType.NOVA.getServiceType(), BigInteger.ONE, BigInteger.ONE});
        dummyResultset.add(new Object[]{ServiceType.LIBRARY_THIN2.getServiceType(), BigInteger.TWO, BigInteger.ONE});
        Mockito.when(releaseVersionRepository.findAllNotStoragedElements()).thenReturn(dummyResultset);

        ReleaseVersionSummaryDTO releaseVersionsSummary = serviceStatisticsapi.getReleaseVersionsSummary(null, null, null);
        List<String> expectedServicesKeys = Arrays.asList(ServiceGroupingNames.API.name(), ServiceGroupingNames.LIBRARY.name());

        Assertions.assertEquals(Long.valueOf(1L), releaseVersionsSummary.getTotal());
        Assertions.assertEquals(Long.valueOf(3L), releaseVersionsSummary.getTotalServices());
        ServiceDTO[] services = releaseVersionsSummary.getServices();
        Assertions.assertEquals(2, services.length);
        Assertions.assertNotEquals(services[0].getServiceType(), services[1].getServiceType());
        Assertions.assertTrue(expectedServicesKeys.contains(services[0].getServiceType()));
        Assertions.assertTrue(expectedServicesKeys.contains(services[1].getServiceType()));

        //The order of the services in the array is not fixed
        test_service_total(services[0]);
        test_service_total(services[1]);
    }

    private void test_service_total(ServiceDTO service)
    {
        if (ServiceGroupingNames.API.name().equals(service.getServiceType()))
        {
            Assertions.assertEquals(Long.valueOf(1L), service.getTotal());
        }
        if (ServiceGroupingNames.LIBRARY.name().equals(service.getServiceType()))
        {
            Assertions.assertEquals(Long.valueOf(2L), service.getTotal());
        }
    }

    @Test
    void when_some_service_types_belongs_to_same_service_group_then_return_grouped_dto_with_api_rest()
    {
        when_some_service_types_belongs_to_same_service_group_then_return_grouped_dto(ServiceType.API_REST_JAVA_SPRING_BOOT, ServiceGroupingNames.API);
    }

    @Test
    void when_some_service_types_belongs_to_same_service_group_then_return_grouped_dto_with_api()
    {
        when_some_service_types_belongs_to_same_service_group_then_return_grouped_dto(ServiceType.API_JAVA_SPRING_BOOT, ServiceGroupingNames.API);
    }

    private void when_some_service_types_belongs_to_same_service_group_then_return_grouped_dto(ServiceType serviceType, ServiceGroupingNames serviceGroupingNames)
    {
        List<Object[]> dummyResultset = new ArrayList<>();
        dummyResultset.add(new Object[]{ServiceType.NOVA.getServiceType(), BigInteger.ONE, BigInteger.ONE});
        dummyResultset.add(new Object[]{serviceType.getServiceType(), BigInteger.TWO, BigInteger.ONE});
        dummyResultset.add(new Object[]{ServiceType.BATCH_SCHEDULER_NOVA.getServiceType(), BigInteger.TWO, BigInteger.ONE});
        Mockito.when(releaseVersionRepository.findAllNotStoragedElements()).thenReturn(dummyResultset);

        ReleaseVersionSummaryDTO releaseVersionsSummary = serviceStatisticsapi.getReleaseVersionsSummary(null, null, null);
        List<String> expectedServicesKeys = Arrays.asList(serviceGroupingNames.name(), ServiceGroupingNames.BATCH_SCHEDULER.name());

        Assertions.assertEquals(Long.valueOf(1L), releaseVersionsSummary.getTotal());
        Assertions.assertEquals(Long.valueOf(5L), releaseVersionsSummary.getTotalServices());
        ServiceDTO[] services = releaseVersionsSummary.getServices();
        Assertions.assertEquals(2, services.length);
        Assertions.assertNotEquals(services[0].getServiceType(), services[1].getServiceType());
        Assertions.assertTrue(expectedServicesKeys.contains(services[0].getServiceType()));
        Assertions.assertEquals(Long.valueOf(3L), services[0].getTotal());
        Assertions.assertTrue(expectedServicesKeys.contains(services[1].getServiceType()));
        Assertions.assertEquals(Long.valueOf(2L), services[1].getTotal());
    }

    @Test
    void when_there_are_services_of_every_type_then_return_grouped_dto()
    {
        List<Object[]> dummyResultset = new ArrayList<>();
        ServiceType[] serviceTypeValues = ServiceType.values();
        for (ServiceType serviceType : serviceTypeValues)
        {
            if (serviceType.equals(ServiceType.INVALID))
            {
                continue;
            }
            dummyResultset.add(new Object[]{serviceType.getServiceType(), BigInteger.TWO, BigInteger.TEN});
        }
        Mockito.when(releaseVersionRepository.findAllNotStoragedElements()).thenReturn(dummyResultset);

        ReleaseVersionSummaryDTO releaseVersionsSummary = serviceStatisticsapi.getReleaseVersionsSummary(null, null, null);

        Assertions.assertEquals(Long.valueOf(10L), releaseVersionsSummary.getTotal());
        int serviceTypesLength = ServiceType.values().length - 1;
        Assertions.assertEquals(Long.valueOf(serviceTypesLength * 2L), releaseVersionsSummary.getTotalServices());
        ServiceDTO[] services = releaseVersionsSummary.getServices();
        int serviceGroupsLength = ServiceGroupingNames.values().length - 1;
        Set<String> uniqueServiceTypes = Arrays.stream(services).map(ServiceDTO::getServiceType).collect(Collectors.toSet());
        Assertions.assertEquals(serviceGroupsLength, uniqueServiceTypes.size());
        Map<String, Long> expectedServiceOccurrencesMap = getExpectedServiceOccurrencesMap();
        for (ServiceDTO service : services)
        {
            Assertions.assertEquals(expectedServiceOccurrencesMap.get(service.getServiceType()), service.getTotal());
        }
    }

    private Map<String, Long> getExpectedServiceOccurrencesMap()
    {
        Map<String, Long> serviceOccurrencesMap = new HashMap<>(ServiceGroupingNames.values().length);
        for (ServiceType serviceType : ServiceType.values())
        {
            if (ServiceType.INVALID == serviceType)
            {
                continue;
            }
            String serviceGroup = ServiceTypeGroupProvider.getServiceGroupNameFor(serviceType.getServiceType());
            if (!serviceOccurrencesMap.containsKey(serviceGroup))
            {
                serviceOccurrencesMap.put(serviceGroup, 0L);
            }
            serviceOccurrencesMap.put(serviceGroup, serviceOccurrencesMap.get(serviceGroup) + 2L);
        }
        return serviceOccurrencesMap;

    }

    private Broker createBroker(int id, Environment environment, BrokerStatus status, BrokerType type, Platform platform)
    {
        Broker broker = new Broker();
        broker.setId(id);
        broker.setProduct(new Product());
        broker.setEnvironment(environment.getEnvironment());
        broker.setStatus(status);
        broker.setType(type);
        broker.setPlatform(platform);

        return broker;
    }

    private List<Broker> createBrokersListDummy()
    {
        List<Broker> brokers = new ArrayList<>();
        brokers.add(createBroker(1, Environment.INT, BrokerStatus.RUNNING,
                BrokerType.PUBLISHER_SUBSCRIBER, Platform.NOVA));
        brokers.add(createBroker(2, Environment.INT, BrokerStatus.RUNNING,
                BrokerType.PUBLISHER_SUBSCRIBER, Platform.NOVA));

        return brokers;
    }

    @Test
    void when_user_product_role_snapshot_returns_data_then_return_grouped_dtos()
    {
        when(this.userStatisticsClient.getUserProductRoleHistorySnapshot()).thenReturn(getUserProductRoleDummyDtos());
        when(productRepository.findIdUuaaPairs()).thenReturn(getDummyProductIdUuaaPairs());

        UserProductRoleHistoryDTO[] results = serviceStatisticsapi.getUserProductRoleHistorySnapshot();

        Map<String, Integer> expectedValuesPerUuaa = Map.of("TEST", 18, "FLOW", 9);
        Assertions.assertEquals(2, results.length);
        Assertions.assertEquals(expectedValuesPerUuaa.get(results[0].getUuaa()), results[0].getValue());
        Assertions.assertEquals(expectedValuesPerUuaa.get(results[1].getUuaa()), results[1].getValue());
    }

    @Test
    void when_hardware_budget_snapshot_returns_data_then_return_properly_uuaa_grouped_data()
    {
        Mockito.when(productBudgetsService.getHardwareBudgetHistorySnapshot()).thenReturn(getDummyDtos());
        Mockito.when(productRepository.findIdUuaaPairs()).thenReturn(getDummyProductIdUuaaPairs());

        PBHardwareBudgetSnapshot[] results = serviceStatisticsapi.getHardwareBudgetHistorySnapshot();

        Map<String, Double> expectedValuesPerUuaa = Map.of("TEST", 18D, "FLOW", 9D);
        Assertions.assertEquals(2, results.length);
        Assertions.assertEquals(expectedValuesPerUuaa.get(results[0].getUuaa()), results[0].getValue());
        Assertions.assertEquals(expectedValuesPerUuaa.get(results[1].getUuaa()), results[1].getValue());
    }

    @Test
    void when_subsystem_snapshot_is_received_then_return_mapped_dtos()
    {
        Mockito.when(toolsClient.getSubsystemsHistorySnapshot()).thenReturn(getDummySubsystemsSnapshotDtos());
        Mockito.when(productRepository.findIdUuaaPairs()).thenReturn(List.of(
                new Object[]{1, "UUAA1"}
                , new Object[]{2, "UUAA1"}
                , new Object[]{3, "UUAA2"}
                , new Object[]{4, "UUAA2"}
                , new Object[]{5, "UUAA1"}
                , new Object[]{6, "UUAA2"}));

        TOSubsystemsCombinationDTO[] result = serviceStatisticsapi.getSubsystemsHistorySnapshot();
        Map<List<String>, Integer> expectedResultsMap = getExpectedResultsForAllSubsystems();

        Assertions.assertEquals(4, result.length);
        for (TOSubsystemsCombinationDTO currentResult : result)
        {
            List<String> key = List.of(currentResult.getUuaa(), currentResult.getSubsystemType());
            Assertions.assertEquals(expectedResultsMap.get(key), currentResult.getCount());
        }
    }

    @Test
    void when_subsystem_snapshot_is_received_and_some_product_id_has_no_uuaa_then_return_mapped_dtos_without_unidentified_product_id()
    {
        Mockito.when(toolsClient.getSubsystemsHistorySnapshot()).thenReturn(getDummySubsystemsSnapshotDtos());
        Mockito.when(productRepository.findIdUuaaPairs()).thenReturn(List.of(
                new Object[]{1, "UUAA1"}
                , new Object[]{2, "UUAA1"}
                , new Object[]{3, "UUAA2"}
                , new Object[]{4, "UUAA2"}
                , new Object[]{5, ""}
                , new Object[]{6, "UUAA2"}));

        TOSubsystemsCombinationDTO[] result = serviceStatisticsapi.getSubsystemsHistorySnapshot();
        Map<List<String>, Integer> expectedResultsMap = getExpectedResultsForSubsystemsWithUuaa();

        Assertions.assertEquals(3, result.length);
        for (TOSubsystemsCombinationDTO currentResult : result)
        {
            List<String> key = List.of(currentResult.getUuaa(), currentResult.getSubsystemType());
            Assertions.assertEquals(expectedResultsMap.get(key), currentResult.getCount());
        }
    }

    // No need to test when no mapped dtos are extracted from database, since
    // serviceStatisticsapi.getNovaCoinsByAvailability will always return non empty response
    @Test
    void when_nova_coins_are_extracted_from_database_then_return_mapped_dtos()
    {
        Mockito.when(productRepository.findProductIdsByUuaa(Mockito.anyString())).thenReturn(new Long[]{1L});
        PBAvailabilityNovaCoinsDTO[] dummyDtos = getDummyNovaCoinsDtos();
        Mockito.when(productBudgetsService.getNovaCoinsByAvailability(Mockito.anyInt(), Mockito.anyString())).thenReturn(dummyDtos);

        AvailabilityNovaCoinsDTO[] result = serviceStatisticsapi.getNovaCoinsByAvailability("A", "A");

        Assertions.assertEquals(1, result.length);
        AvailabilityNovaCoinsDTO firstResult = result[0];
        PBAvailabilityNovaCoinsDTO firstDto = dummyDtos[0];
        Assertions.assertEquals(firstDto.getEnvironment(), firstResult.getEnvironment());
        Assertions.assertEquals(firstDto.getAvailable(), firstResult.getAvailable());
        Assertions.assertEquals(firstDto.getUsed(), firstResult.getUsed());
    }

    @Test
    void when_passed_uuaa_is_null_then_dont_filter_by_product_id()
    {
        Mockito.when(productBudgetsService.getNovaCoinsByAvailability(Mockito.isNull(), Mockito.anyString())).thenReturn(new PBAvailabilityNovaCoinsDTO[0]);

        serviceStatisticsapi.getNovaCoinsByAvailability(null, "A");

        Mockito.verify(productRepository, Mockito.times(0)).findProductIdsByUuaa(Mockito.anyString());
    }

    @Test
    void when_passed_uuaa_is_empty_then_dont_filter_by_product_id()
    {
        Mockito.when(productBudgetsService.getNovaCoinsByAvailability(Mockito.isNull(), Mockito.anyString())).thenReturn(new PBAvailabilityNovaCoinsDTO[0]);

        serviceStatisticsapi.getNovaCoinsByAvailability("", "A");

        Mockito.verify(productRepository, Mockito.times(0)).findProductIdsByUuaa(Mockito.anyString());
    }

    @Test
    void when_passed_uuaa_is_ALL_then_dont_filter_by_product_id()
    {
        Mockito.when(productBudgetsService.getNovaCoinsByAvailability(Mockito.isNull(), Mockito.anyString())).thenReturn(new PBAvailabilityNovaCoinsDTO[0]);

        serviceStatisticsapi.getNovaCoinsByAvailability("ALL", "A");

        Mockito.verify(productRepository, Mockito.times(0)).findProductIdsByUuaa(Mockito.anyString());
    }

    @Test
    void when_passed_arguments_then_return_product_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(10);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.PRODUCTS.name()), eq(StatisticParamName.PRODUCTS_TYPE.name()), eq(type),
                ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull())).thenReturn(mockRecords);
        // when
        final STHistoricalPoint[] response = this.serviceStatisticsapi.getProductsHistorical(startDate, endDate, type);

        // then
        assertRecordsOfValuesByDateToHistoricalPoint(mockRecords, response, true);
    }

    @Test
    void when_passed_bad_format_to_export_then_throw_exception_to_user()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.PRODUCTS.name()), eq(StatisticParamName.PRODUCTS_TYPE.name()), eq(type),
                ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // then
        assertThrows(NovaException.class, () -> this.serviceStatisticsapi.getProductsHistoricalExport(startDate, endDate, type, "badFormat"));
    }

    @Test
    void when_passed_arguments_then_return_product_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.PRODUCTS.name()), eq(StatisticParamName.PRODUCTS_TYPE.name()), eq(type),
                ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getProductsHistoricalExport(startDate, endDate, type, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistorical(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_product_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.PRODUCTS.name()), eq(StatisticParamName.PRODUCTS_TYPE.name()), eq(type),
                ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getProductsHistoricalExport(startDate, endDate, type, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistorical(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_deployed_services_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(10);
        final String platform = RandomStringUtils.randomAlphabetic(6);
        final String language = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        StatisticParamName[] statisticParamNames = new StatisticParamName[]{StatisticParamName.DEPLOYED_SERVICES_TYPE, StatisticParamName.DEPLOYED_SERVICES_LANGUAGE};
        final String category = statisticParamNames[RandomUtils.nextInt(0, statisticParamNames.length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.DEPLOYED_SERVICES.name()), eq(StatisticParamName.DEPLOYED_SERVICES_ENVIRONMENT.name()),
                eq(environment), eq(StatisticParamName.DEPLOYED_SERVICES_PLATFORM.name()), eq(platform), eq(StatisticParamName.DEPLOYED_SERVICES_TYPE.name()), eq(type),
                eq(StatisticParamName.DEPLOYED_SERVICES_UUAA.name()), eq(uuaa), eq(StatisticParamName.DEPLOYED_SERVICES_LANGUAGE.name()), eq(language),
                eq(category))).thenReturn(mockRecords);

        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getDeployedServicesHistorical(startDate, endDate, environment, platform,
                language, type, uuaa, category);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_deployed_services_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String platform = RandomStringUtils.randomAlphabetic(6);
        final String language = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        StatisticParamName[] statisticParamNames = new StatisticParamName[]{StatisticParamName.DEPLOYED_SERVICES_TYPE, StatisticParamName.DEPLOYED_SERVICES_LANGUAGE};
        final String category = statisticParamNames[RandomUtils.nextInt(0, statisticParamNames.length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.DEPLOYED_SERVICES.name()), eq(StatisticParamName.DEPLOYED_SERVICES_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.DEPLOYED_SERVICES_PLATFORM.name()), eq(platform), eq(StatisticParamName.DEPLOYED_SERVICES_TYPE.name()), eq(type), eq(StatisticParamName.DEPLOYED_SERVICES_UUAA.name()), eq(uuaa), eq(StatisticParamName.DEPLOYED_SERVICES_LANGUAGE.name()), eq(language), eq(category))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getDeployedServicesHistoricalExport(startDate, endDate, environment, platform, language, type, uuaa, FORMAT_CSV, category);

        // then
        assertRecordsOfCSVHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_deployed_services_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String language = RandomStringUtils.randomAlphabetic(6);
        final String platform = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        StatisticParamName[] statisticParamNames = new StatisticParamName[]{StatisticParamName.DEPLOYED_SERVICES_TYPE, StatisticParamName.DEPLOYED_SERVICES_LANGUAGE};
        final String category = statisticParamNames[RandomUtils.nextInt(0, statisticParamNames.length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));

        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.DEPLOYED_SERVICES.name()), eq(StatisticParamName.DEPLOYED_SERVICES_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.DEPLOYED_SERVICES_PLATFORM.name()), eq(platform), eq(StatisticParamName.DEPLOYED_SERVICES_TYPE.name()), eq(type), eq(StatisticParamName.DEPLOYED_SERVICES_UUAA.name()), eq(uuaa), eq(StatisticParamName.DEPLOYED_SERVICES_LANGUAGE.name()), eq(language), eq(category))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getDeployedServicesHistoricalExport(startDate, endDate, environment, platform, language, type, uuaa, FORMAT_XLSX, category);

        // then
        assertRecordsOfXLSXHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_instances_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String platform = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.INSTANCES.name()), eq(StatisticParamName.INSTANCES_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.INSTANCES_PLATFORM.name()), eq(platform), eq(StatisticParamName.INSTANCES_TYPE.name()), eq(type), eq(StatisticParamName.INSTANCES_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.INSTANCES_TYPE.name()))).thenReturn(mockRecords);

        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getInstancesHistorical(startDate, endDate, environment, platform, type, uuaa);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }


    @Test
    void when_passed_arguments_then_return_instances_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String platform = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.INSTANCES.name()), eq(StatisticParamName.INSTANCES_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.INSTANCES_PLATFORM.name()), eq(platform), eq(StatisticParamName.INSTANCES_TYPE.name()), eq(type), eq(StatisticParamName.INSTANCES_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.INSTANCES_TYPE.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getInstancesHistoricalExport(startDate, endDate, environment, platform, type, uuaa, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_instances_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String platform = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.INSTANCES.name()), eq(StatisticParamName.INSTANCES_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.INSTANCES_PLATFORM.name()), eq(platform), eq(StatisticParamName.INSTANCES_TYPE.name()), eq(type), eq(StatisticParamName.INSTANCES_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.INSTANCES_TYPE.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getInstancesHistoricalExport(startDate, endDate, environment, platform, type, uuaa, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_categories_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.CATEGORIES.name()), eq(StatisticParamName.CATEGORIES_TYPE.name()), eq(type),
                ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final STHistoricalPoint[] response = this.serviceStatisticsapi.getCategoriesHistorical(startDate, endDate, type);

        // then
        assertRecordsOfValuesByDateToHistoricalPoint(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_categories_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.CATEGORIES.name()), eq(StatisticParamName.CATEGORIES_TYPE.name()), eq(type),
                ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getCategoriesHistoricalExport(startDate, endDate, type, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistorical(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_categories_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.CATEGORIES.name()), eq(StatisticParamName.CATEGORIES_TYPE.name()), eq(type),
                ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getCategoriesHistoricalExport(startDate, endDate, type, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistorical(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_users_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String role = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.USERS.name()), eq(StatisticParamName.USERS_ROLE.name()), eq(role),
                eq(StatisticParamName.USERS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final STHistoricalPoint[] response = this.serviceStatisticsapi.getUsersHistorical(startDate, endDate, role, uuaa);

        // then
        assertRecordsOfValuesByDateToHistoricalPoint(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_users_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String role = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.USERS.name()), eq(StatisticParamName.USERS_ROLE.name()), eq(role),
                eq(StatisticParamName.USERS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getUsersHistoricalExport(startDate, endDate, role, uuaa, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistorical(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_users_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String role = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.USERS.name()), eq(StatisticParamName.USERS_ROLE.name()), eq(role),
                eq(StatisticParamName.USERS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getUsersHistoricalExport(startDate, endDate, role, uuaa, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistorical(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_all_roles_of_users_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.USERS.name()), eq(StatisticParamName.USERS_ROLE.name()), eq("ALL"),
                eq(StatisticParamName.USERS_UUAA.name()), eq("ALL"), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getUsersHistoricalExport(startDate, endDate, null, null, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistorical(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_all_roles_of_users_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.USERS.name()), eq(StatisticParamName.USERS_ROLE.name()), eq("ALL"),
                eq(StatisticParamName.USERS_UUAA.name()), eq("ALL"), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getUsersHistoricalExport(startDate, endDate, null, null, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistorical(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_connectors_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.CONNECTORS.name()), eq(StatisticParamName.CONNECTORS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.CONNECTORS_STATUS.name()), eq(status), eq(StatisticParamName.CONNECTORS_TYPE.name()), eq(type), eq(StatisticParamName.CONNECTORS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.CONNECTORS_ENVIRONMENT.name()))).thenReturn(mockRecords);

        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getConnectorsHistorical(startDate, endDate, environment, status, type, uuaa);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_connectors_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.CONNECTORS.name()), eq(StatisticParamName.CONNECTORS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.CONNECTORS_STATUS.name()), eq(status), eq(StatisticParamName.CONNECTORS_TYPE.name()), eq(type), eq(StatisticParamName.CONNECTORS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.CONNECTORS_ENVIRONMENT.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getConnectorsHistoricalExport(startDate, endDate, environment, status, type, uuaa, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_connectors_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.CONNECTORS.name()), eq(StatisticParamName.CONNECTORS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.CONNECTORS_STATUS.name()), eq(status), eq(StatisticParamName.CONNECTORS_TYPE.name()), eq(type), eq(StatisticParamName.CONNECTORS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.CONNECTORS_ENVIRONMENT.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getConnectorsHistoricalExport(startDate, endDate, environment, status, type, uuaa, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_filesystems_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.FILESYSTEMS.name()), eq(StatisticParamName.FILESYSTEMS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.FILESYSTEMS_STATUS.name()), eq(status), eq(StatisticParamName.FILESYSTEMS_TYPE.name()), eq(type), eq(StatisticParamName.FILESYSTEMS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.FILESYSTEMS_TYPE.name()))).thenReturn(mockRecords);

        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getFilesystemsHistorical(startDate, endDate, environment, status, type, uuaa);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_filesystems_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.FILESYSTEMS.name()), eq(StatisticParamName.FILESYSTEMS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.FILESYSTEMS_STATUS.name()), eq(status), eq(StatisticParamName.FILESYSTEMS_TYPE.name()), eq(type), eq(StatisticParamName.FILESYSTEMS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.FILESYSTEMS_TYPE.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getFilesystemsHistoricalExport(startDate, endDate, environment, status, type, uuaa, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_filesystems_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.FILESYSTEMS.name()), eq(StatisticParamName.FILESYSTEMS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.FILESYSTEMS_STATUS.name()), eq(status), eq(StatisticParamName.FILESYSTEMS_TYPE.name()), eq(type), eq(StatisticParamName.FILESYSTEMS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.FILESYSTEMS_TYPE.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getFilesystemsHistoricalExport(startDate, endDate, environment, status, type, uuaa, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_subsystems_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.SUBSYSTEMS.name()), eq(StatisticParamName.SUBSYSTEMS_TYPE.name()), eq(type),
                eq(StatisticParamName.SUBSYSTEMS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.SUBSYSTEMS_TYPE.name()))).thenReturn(mockRecords);

        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getSubsystemsHistorical(startDate, endDate, type, uuaa);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_subsystems_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.SUBSYSTEMS.name()), eq(StatisticParamName.SUBSYSTEMS_TYPE.name()), eq(type),
                eq(StatisticParamName.SUBSYSTEMS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.SUBSYSTEMS_TYPE.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getSubsystemsHistoricalExport(startDate, endDate, type, uuaa, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_subsystems_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.SUBSYSTEMS.name()), eq(StatisticParamName.SUBSYSTEMS_TYPE.name()), eq(type),
                eq(StatisticParamName.SUBSYSTEMS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.SUBSYSTEMS_TYPE.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getSubsystemsHistoricalExport(startDate, endDate, type, uuaa, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_apis_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String apiFunctionality = RandomStringUtils.randomAlphabetic(6);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.APIS.name()), eq(StatisticParamName.APIS_TYPE.name()), eq(type),
                eq(StatisticParamName.APIS_UUAA.name()), eq(uuaa), eq(StatisticParamName.APIS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.APIS_DISCRIMINATOR.name()), eq(apiFunctionality), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.APIS_TYPE.name()))).thenReturn(mockRecords);

        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getApisHistorical(apiFunctionality, environment, endDate, uuaa, type, startDate);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_apis_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.APIS.name()), eq(StatisticParamName.APIS_TYPE.name()), eq(type),
                eq(StatisticParamName.APIS_UUAA.name()), eq(uuaa), eq(StatisticParamName.APIS_ENVIRONMENT.name()), eq(environment), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.APIS_TYPE.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getApisHistoricalExport(startDate, endDate, type, uuaa, environment, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_apis_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.APIS.name()), eq(StatisticParamName.APIS_TYPE.name()), eq(type),
                eq(StatisticParamName.APIS_UUAA.name()), eq(uuaa), eq(StatisticParamName.APIS_ENVIRONMENT.name()), eq(environment), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.APIS_TYPE.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getApisHistoricalExport(startDate, endDate, type, uuaa, environment, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_compilations_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String status = RandomStringUtils.randomAlphabetic(8);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.COMPILATIONS.name()), eq(StatisticParamName.COMPILATIONS_STATUS.name()), eq(status),
                eq(StatisticParamName.COMPILATIONS_TYPE.name()), eq(type), eq(StatisticParamName.COMPILATIONS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.COMPILATIONS_TYPE.name()))).thenReturn(mockRecords);

        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getCompilationsHistorical(startDate, endDate, status, type, uuaa);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_compilations_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String status = RandomStringUtils.randomAlphabetic(8);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.COMPILATIONS.name()), eq(StatisticParamName.COMPILATIONS_STATUS.name()), eq(status),
                eq(StatisticParamName.COMPILATIONS_TYPE.name()), eq(type), eq(StatisticParamName.COMPILATIONS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.COMPILATIONS_TYPE.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getCompilationsHistoricalExport(startDate, endDate, status, type, uuaa, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_compilations_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String type = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String status = RandomStringUtils.randomAlphabetic(8);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.COMPILATIONS.name()), eq(StatisticParamName.COMPILATIONS_STATUS.name()), eq(status),
                eq(StatisticParamName.COMPILATIONS_TYPE.name()), eq(type), eq(StatisticParamName.COMPILATIONS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.COMPILATIONS_TYPE.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getCompilationsHistoricalExport(startDate, endDate, status, type, uuaa, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_file_transfers_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.FILETRANSFERS.name()), eq(StatisticParamName.FILETRANSFERS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.FILETRANSFERS_STATUS.name()), eq(status), eq(StatisticParamName.FILETRANSFERS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.FILETRANSFERS_STATUS.name()))).thenReturn(mockRecords);

        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getFiletransfersHistorical(startDate, endDate, environment, status, uuaa);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_file_transfers_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.FILETRANSFERS.name()), eq(StatisticParamName.FILETRANSFERS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.FILETRANSFERS_STATUS.name()), eq(status), eq(StatisticParamName.FILETRANSFERS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.FILETRANSFERS_STATUS.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getFiletransfersHistoricalExport(startDate, endDate, environment, status, uuaa, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_file_transfers_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.FILETRANSFERS.name()), eq(StatisticParamName.FILETRANSFERS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.FILETRANSFERS_STATUS.name()), eq(status), eq(StatisticParamName.FILETRANSFERS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.FILETRANSFERS_STATUS.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getFiletransfersHistoricalExport(startDate, endDate, environment, status, uuaa, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_todo_tasks_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String role = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String status = RandomStringUtils.randomAlphabetic(5);
        final String type = RandomStringUtils.randomAlphabetic(7);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.TODOTASKS.name()), eq(StatisticParamName.TODOTASKS_STATUS.name()), eq(status),
                eq(StatisticParamName.TODOTASKS_TYPE.name()), eq(type), eq(StatisticParamName.TODOTASKS_ROLE.name()), eq(role), eq(StatisticParamName.TODOTASKS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final STHistoricalPoint[] response = this.serviceStatisticsapi.getTodotasksHistorical(startDate, endDate, status, type, role, uuaa);

        // then
        assertRecordsOfValuesByDateToHistoricalPoint(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_todo_tasks_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String role = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String status = RandomStringUtils.randomAlphabetic(5);
        final String type = RandomStringUtils.randomAlphabetic(7);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.TODOTASKS.name()), eq(StatisticParamName.TODOTASKS_STATUS.name()), eq(status),
                eq(StatisticParamName.TODOTASKS_TYPE.name()), eq(type), eq(StatisticParamName.TODOTASKS_ROLE.name()), eq(role), eq(StatisticParamName.TODOTASKS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getTodotasksHistoricalExport(startDate, endDate, status, type, role, uuaa, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistorical(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_todo_tasks_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String role = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String status = RandomStringUtils.randomAlphabetic(5);
        final String type = RandomStringUtils.randomAlphabetic(7);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.TODOTASKS.name()), eq(StatisticParamName.TODOTASKS_STATUS.name()), eq(status),
                eq(StatisticParamName.TODOTASKS_TYPE.name()), eq(type), eq(StatisticParamName.TODOTASKS_ROLE.name()), eq(role), eq(StatisticParamName.TODOTASKS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getTodotasksHistoricalExport(startDate, endDate, status, type, role, uuaa, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistorical(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_memory_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String cpd = RandomStringUtils.randomAlphabetic(6);
        final String unit = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.MEMORY.name()), eq(StatisticParamName.MEMORY_CPD.name()), eq(cpd),
                eq(StatisticParamName.MEMORY_ENVIRONMENT.name()), eq(environment), eq(StatisticParamName.MEMORY_UNIT.name()), eq(unit), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final STHistoricalPoint[] response = this.serviceStatisticsapi.getMemoryHistorical(startDate, endDate, cpd, environment, unit);

        // then
        assertRecordsOfValuesByDateToHistoricalPoint(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_memory_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String cpd = RandomStringUtils.randomAlphabetic(6);
        final String unit = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.MEMORY.name()), eq(StatisticParamName.MEMORY_CPD.name()), eq(cpd),
                eq(StatisticParamName.MEMORY_ENVIRONMENT.name()), eq(environment), eq(StatisticParamName.MEMORY_UNIT.name()), eq(unit), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getMemoryHistoricalExport(startDate, endDate, cpd, environment, unit, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistorical(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_memory_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String cpd = RandomStringUtils.randomAlphabetic(6);
        final String unit = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.MEMORY.name()), eq(StatisticParamName.MEMORY_CPD.name()), eq(cpd),
                eq(StatisticParamName.MEMORY_ENVIRONMENT.name()), eq(environment), eq(StatisticParamName.MEMORY_UNIT.name()), eq(unit), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getMemoryHistoricalExport(startDate, endDate, cpd, environment, unit, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistorical(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_hardware_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String property = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.HARDWARE.name()), eq(StatisticParamName.HARDWARE_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.HARDWARE_PROPERTY.name()), eq(property), eq(StatisticParamName.HARDWARE_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.HARDWARE_PROPERTY.name()))).thenReturn(mockRecords);

        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getHardwareHistorical(startDate, endDate, environment, property, uuaa);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_hardware_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String property = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.HARDWARE.name()), eq(StatisticParamName.HARDWARE_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.HARDWARE_PROPERTY.name()), eq(property), eq(StatisticParamName.HARDWARE_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.HARDWARE_PROPERTY.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getHardwareHistoricalExport(startDate, endDate, environment, property, uuaa, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_hardware_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String property = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.HARDWARE.name()), eq(StatisticParamName.HARDWARE_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.HARDWARE_PROPERTY.name()), eq(property), eq(StatisticParamName.HARDWARE_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.HARDWARE_PROPERTY.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getHardwareHistoricalExport(startDate, endDate, environment, property, uuaa, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_storage_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String property = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.STORAGE.name()), eq(StatisticParamName.STORAGE_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.STORAGE_PROPERTY.name()), eq(property), eq(StatisticParamName.STORAGE_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.STORAGE_PROPERTY.name()))).thenReturn(mockRecords);
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.STORAGE.name()), eq(StatisticParamName.STORAGE_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.STORAGE_PROPERTY.name()), eq(property), eq(StatisticParamName.STORAGE_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(new ArrayList<>());

        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getStorageHistorical(startDate, endDate, environment, property, uuaa);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_storage_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String property = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.STORAGE.name()), eq(StatisticParamName.STORAGE_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.STORAGE_PROPERTY.name()), eq(property), eq(StatisticParamName.STORAGE_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.STORAGE_PROPERTY.name()))).thenReturn(mockRecords);
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.STORAGE.name()), eq(StatisticParamName.STORAGE_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.STORAGE_PROPERTY.name()), eq(property), eq(StatisticParamName.STORAGE_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(new ArrayList<>());

        // when
        final byte[] response = this.serviceStatisticsapi.getStorageHistoricalExport(startDate, endDate, environment, property, uuaa, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_storage_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String property = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.STORAGE.name()), eq(StatisticParamName.STORAGE_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.STORAGE_PROPERTY.name()), eq(property), eq(StatisticParamName.STORAGE_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.STORAGE_PROPERTY.name()))).thenReturn(mockRecords);
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.STORAGE.name()), eq(StatisticParamName.STORAGE_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.STORAGE_PROPERTY.name()), eq(property), eq(StatisticParamName.STORAGE_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(new ArrayList<>());

        // when
        final byte[] response = this.serviceStatisticsapi.getStorageHistoricalExport(startDate, endDate, environment, property, uuaa, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_deployed_plans_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.DEPLOYMENT_PLANS.name()), eq(StatisticParamName.DEPLOYMENT_PLANS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.DEPLOYMENT_PLANS_STATUS.name()), eq(status), eq(StatisticParamName.DEPLOYMENT_PLANS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.DEPLOYMENT_PLANS_STATUS.name()))).thenReturn(mockRecords);

        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getDeployedPlansHistorical(startDate, endDate, environment, status, uuaa);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_deployed_plans_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.DEPLOYMENT_PLANS.name()), eq(StatisticParamName.DEPLOYMENT_PLANS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.DEPLOYMENT_PLANS_STATUS.name()), eq(status), eq(StatisticParamName.DEPLOYMENT_PLANS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.DEPLOYMENT_PLANS_STATUS.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getDeployedPlansHistoricalExport(startDate, endDate, environment, status, uuaa, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_deployed_plans_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.DEPLOYMENT_PLANS.name()), eq(StatisticParamName.DEPLOYMENT_PLANS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.DEPLOYMENT_PLANS_STATUS.name()), eq(status), eq(StatisticParamName.DEPLOYMENT_PLANS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.DEPLOYMENT_PLANS_STATUS.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getDeployedPlansHistoricalExport(startDate, endDate, environment, status, uuaa, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_release_versions_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.RELEASE_VERSIONS.name()), eq(StatisticParamName.RELEASE_VERSIONS_STATUS.name()), eq(status),
                eq(StatisticParamName.RELEASE_VERSIONS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.RELEASE_VERSIONS_STATUS.name()))).thenReturn(mockRecords);

        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getReleaseVersionsHistorical(startDate, endDate, status, uuaa);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_release_versions_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.RELEASE_VERSIONS.name()), eq(StatisticParamName.RELEASE_VERSIONS_STATUS.name()), eq(status),
                eq(StatisticParamName.RELEASE_VERSIONS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.RELEASE_VERSIONS_STATUS.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getReleaseVersionsHistoricalExport(startDate, endDate, status, uuaa, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_release_versions_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.RELEASE_VERSIONS.name()), eq(StatisticParamName.RELEASE_VERSIONS_STATUS.name()), eq(status),
                eq(StatisticParamName.RELEASE_VERSIONS_UUAA.name()), eq(uuaa), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.RELEASE_VERSIONS_STATUS.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getReleaseVersionsHistoricalExport(startDate, endDate, status, uuaa, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_batch_executions_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(5);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.BATCH_INSTANCES.name()), eq(StatisticParamName.BATCH_INSTANCES_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.BATCH_INSTANCES_PLATFORM.name()), eq(platform), eq(StatisticParamName.BATCH_INSTANCES_UUAA.name()), eq(uuaa), eq(StatisticParamName.BATCH_INSTANCES_STATUS.name()), eq(status), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.BATCH_INSTANCES_PLATFORM.name()))).thenReturn(mockRecords);

        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getBatchExecutionsHistorical(startDate, endDate, environment, platform, uuaa, status);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_batch_executions_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(5);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.BATCH_INSTANCES.name()), eq(StatisticParamName.BATCH_INSTANCES_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.BATCH_INSTANCES_PLATFORM.name()), eq(platform), eq(StatisticParamName.BATCH_INSTANCES_UUAA.name()), eq(uuaa), eq(StatisticParamName.BATCH_INSTANCES_STATUS.name()), eq(status), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.BATCH_INSTANCES_PLATFORM.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getBatchExecutionsHistoricalExport(startDate, endDate, environment, platform, uuaa, status, FORMAT_CSV);

        // then
        assertRecordsOfBatchCSVHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_batch_executions_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String status = RandomStringUtils.randomAlphabetic(6);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(5);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.BATCH_INSTANCES.name()), eq(StatisticParamName.BATCH_INSTANCES_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.BATCH_INSTANCES_PLATFORM.name()), eq(platform), eq(StatisticParamName.BATCH_INSTANCES_UUAA.name()), eq(uuaa), eq(StatisticParamName.BATCH_INSTANCES_STATUS.name()), eq(status), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(StatisticParamName.BATCH_INSTANCES_PLATFORM.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getBatchExecutionsHistoricalExport(startDate, endDate, environment, platform, uuaa, status, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXBatchHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_adoption_level_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String platform = RandomStringUtils.randomAlphabetic(5);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.ADOPTION_LEVEL.name()), eq(StatisticParamName.ADOPTION_LEVEL_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.ADOPTION_LEVEL_PLATFORM.name()), eq(platform), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(),
                eq(StatisticParamName.ADOPTION_LEVEL_PLATFORM.name()))).thenReturn(mockRecords);

        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getAdoptionLevelHistorical(startDate, endDate, environment, platform);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_adoption_level_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String platform = RandomStringUtils.randomAlphabetic(5);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.ADOPTION_LEVEL.name()), eq(StatisticParamName.ADOPTION_LEVEL_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.ADOPTION_LEVEL_PLATFORM.name()), eq(platform), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(),
                eq(StatisticParamName.ADOPTION_LEVEL_PLATFORM.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getAdoptionLevelHistoricalExport(startDate, endDate, environment, platform, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_adoption_level_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String platform = RandomStringUtils.randomAlphabetic(5);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.ADOPTION_LEVEL.name()), eq(StatisticParamName.ADOPTION_LEVEL_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.ADOPTION_LEVEL_PLATFORM.name()), eq(platform), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(),
                eq(StatisticParamName.ADOPTION_LEVEL_PLATFORM.name()))).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getAdoptionLevelHistoricalExport(startDate, endDate, environment, platform, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistoricalCategory(mockRecords, response, false);
    }

    @Test
    public void when_passed_arguments_then_return_Users_Connected_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(eq(startDate), eq(endDate))).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.USERS_CONNECTED.name()), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getUsersConnectedHistoricalExport(startDate, endDate, FORMAT_CSV);

        // then
        assertRecordsOfCSVHistorical(mockRecords, response, false);
    }

    @Test
    public void when_passed_arguments_then_return_Users_Connected_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(eq(startDate), eq(endDate))).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.USERS_CONNECTED.name()), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getUsersConnectedHistoricalExport(startDate, endDate, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXHistorical(mockRecords, response, false);
    }

    @Test
    void getBrokersSummaryNotValidFilterByEnvironment()
    {
        final String environment = "INVALID";

        NovaException exception = assertThrows(NovaException.class, () ->
                this.serviceStatisticsapi.getBrokersSummary(environment, "", "", ""));

        assertEquals(StatisticsErrors.ENVIRONMENT_NOT_VALID_ERROR_CODE, exception.getErrorCode().getErrorCode());
    }

    @Test
    void getBrokersSummaryFilterByUuaaEmptyProduct()
    {
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        List<Product> products = new ArrayList<>();
        when(this.productRepository.findByUuaa(uuaa.toUpperCase())).thenReturn(products);

        BrokersSummaryDTO brokersSummaryDTO = this.serviceStatisticsapi.getBrokersSummary("", uuaa, "", "");

        assertEquals(0, brokersSummaryDTO.getTotal());
        assertEquals(0, brokersSummaryDTO.getElements().length);
    }

    @Test
    void getBrokersSummaryFilterByUuaaManyProducts()
    {
        final String uuaa = RandomStringUtils.randomAlphabetic(4);

        List<Product> products = new ArrayList<>();
        products.add(new Product());
        products.add(new Product());
        products.add(new Product());
        when(this.productRepository.findByUuaa(uuaa.toUpperCase())).thenReturn(products);

        NovaException exception = assertThrows(NovaException.class, () ->
                this.serviceStatisticsapi.getBrokersSummary("", uuaa, "", ""));

        assertEquals(StatisticsErrors.UUAA_NOT_UNIQUE_ERROR_CODE, exception.getErrorCode().getErrorCode());
        verify(this.productRepository, times(1)).findByUuaa(anyString());
    }

    @Test
    void getBrokersSummaryNotValidFilterByStatus()
    {
        final String status = "INVALID";
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();

        NovaException exception = assertThrows(NovaException.class, () ->
                this.serviceStatisticsapi.getBrokersSummary("", "", "", status));

        assertEquals(StatisticsErrors.BROKER_STATUS_NOT_VALID_ERROR_CODE, exception.getErrorCode().getErrorCode());
    }

    @Test
    void getBrokersSummaryEmptyFilter()
    {
        List<Broker> brokers = this.createBrokersListDummy();

        when(this.brokerRepository.findAll()).thenReturn(brokers);

        BrokersSummaryDTO brokersSummaryDTO = this.serviceStatisticsapi.getBrokersSummary("", "", "", "");

        assertEquals(brokers.size(), brokersSummaryDTO.getTotal());
        verify(this.brokerRepository, times(1)).findAll();
    }

    @Test
    void getBrokersSummaryFilterByStatus()
    {
        List<Broker> brokers = this.createBrokersListDummy();

        when(this.brokerRepository.findAllBrokersSummary(ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), ArgumentMatchers.isNull(), eq(BrokerStatus.RUNNING))).thenReturn(brokers);
        BrokersSummaryDTO brokersSummaryDTO = this.serviceStatisticsapi.getBrokersSummary("", "", "", "RUNNING");

        assertEquals(brokers.size(), brokersSummaryDTO.getTotal());
        verify(this.brokerRepository, times(1)).findAllBrokersSummary(any(), any(), any(), any());
    }

    @Test
    void getBrokersSummaryFilterByUuaa()
    {
        Product product = new Product();
        product.setId(1);
        List<Product> products = new ArrayList<>();
        products.add(product);
        List<Broker> brokers = this.createBrokersListDummy();

        when(this.productRepository.findByUuaa("ENOA")).thenReturn(products);
        when(this.brokerRepository.findAllBrokersSummary(ArgumentMatchers.isNull(), eq(product.getId()),
                ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(brokers);

        BrokersSummaryDTO brokersSummaryDTO = this.serviceStatisticsapi.getBrokersSummary("", "enoa", "", "");

        assertEquals(brokers.size(), brokersSummaryDTO.getTotal());
        verify(this.productRepository, times(1)).findByUuaa(anyString());
        verify(this.brokerRepository, times(1)).findAllBrokersSummary(any(), anyInt(), any(), any());
    }

    @Test
    void getBrokersSummaryFilterByEnvironment()
    {
        List<Broker> brokers = this.createBrokersListDummy();

        when(this.brokerRepository.findAllBrokersSummary(eq(Environment.INT.getEnvironment()), ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(brokers);

        BrokersSummaryDTO brokersSummaryDTO = this.serviceStatisticsapi.getBrokersSummary("INT", "", "", "");

        assertEquals(brokers.size(), brokersSummaryDTO.getTotal());
        verify(this.brokerRepository, times(1)).findAllBrokersSummary(any(), any(), any(), any());
    }

    @Test
    void getBrokersSummaryFilterByUuaaAndByStatus()
    {
        Product product = new Product();
        product.setId(1);
        List<Product> products = new ArrayList<>();
        products.add(product);
        List<Broker> brokers = this.createBrokersListDummy();

        when(this.productRepository.findByUuaa("ENOA")).thenReturn(products);
        when(this.brokerRepository.findAllBrokersSummary(ArgumentMatchers.isNull(), eq(product.getId()),
                ArgumentMatchers.isNull(), eq(BrokerStatus.RUNNING))).thenReturn(brokers);

        BrokersSummaryDTO brokersSummaryDTO = this.serviceStatisticsapi.getBrokersSummary("", "enoa", "", "RUNNING");

        assertEquals(brokers.size(), brokersSummaryDTO.getTotal());
        verify(this.productRepository, times(1)).findByUuaa(anyString());
        verify(this.brokerRepository, times(1)).findAllBrokersSummary(any(), anyInt(), any(), any());
    }

    @Test
    void getBrokersSummaryFilterByEnvironmentAndByStatus()
    {
        List<Broker> brokers = this.createBrokersListDummy();

        when(this.brokerRepository.findAllBrokersSummary(eq(Environment.INT.getEnvironment()), ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(), eq(BrokerStatus.RUNNING))).thenReturn(brokers);
        BrokersSummaryDTO brokersSummaryDTO = this.serviceStatisticsapi.getBrokersSummary("INT", "", "", "RUNNING");

        assertEquals(brokers.size(), brokersSummaryDTO.getTotal());
        verify(this.brokerRepository, times(1)).findAllBrokersSummary(any(), any(), any(), any());
    }

    @Test
    void getBrokersSummaryFilterByEnvironmentAndByUuaa()
    {
        Product product = new Product();
        product.setId(1);
        List<Product> products = new ArrayList<>();
        products.add(product);
        List<Broker> brokers = this.createBrokersListDummy();

        when(this.productRepository.findByUuaa("ENOA")).thenReturn(products);
        when(this.brokerRepository.findAllBrokersSummary(eq(Environment.INT.getEnvironment()), eq(product.getId()),
                ArgumentMatchers.isNull(), ArgumentMatchers.isNull())).thenReturn(brokers);

        BrokersSummaryDTO brokersSummaryDTO = this.serviceStatisticsapi.getBrokersSummary("INT", "enoa", "", "");

        assertEquals(brokers.size(), brokersSummaryDTO.getTotal());
        verify(this.productRepository, times(1)).findByUuaa(anyString());
        verify(this.brokerRepository, times(1)).findAllBrokersSummary(any(), anyInt(), any(), any());
    }

    @Test
    void getBrokersSummaryAllFilters()
    {
        Product product = new Product();
        product.setId(1);
        List<Product> products = new ArrayList<>();
        products.add(product);
        List<Broker> brokers = this.createBrokersListDummy();

        when(this.productRepository.findByUuaa("ENOA")).thenReturn(products);
        when(this.brokerRepository.findAllBrokersSummary(eq(Environment.INT.getEnvironment()), eq(product.getId()),
                eq(Platform.NOVA), eq(BrokerStatus.RUNNING))).thenReturn(brokers);

        BrokersSummaryDTO brokersSummaryDTO = this.serviceStatisticsapi.getBrokersSummary("INT", "enoa", "NOVA", "RUNNING");

        assertEquals(brokers.size(), brokersSummaryDTO.getTotal());
        verify(this.productRepository, times(1)).findByUuaa(anyString());
        verify(this.brokerRepository, times(1)).findAllBrokersSummary(any(), anyInt(), any(), any());
    }

    @Test
    void getBrokersSummaryExportEmpty()
    {
        Product product = new Product();
        product.setId(1);
        List<Product> products = new ArrayList<>();
        products.add(product);
        List<Broker> brokers = new ArrayList<>();
        final List<Object[]> mockRecords = new ArrayList<>();

        when(this.productRepository.findByUuaa("ENOA")).thenReturn(products);
        when(this.brokerRepository.findAllBrokersSummary(eq(Environment.INT.getEnvironment()), eq(product.getId()),
                eq(Platform.NOVA), eq(BrokerStatus.RUNNING))).thenReturn(brokers);
        byte[] fileInBytes = this.serviceStatisticsapi.getBrokersSummaryExport("INT", "enoa", "NOVA", "RUNNING", "csv");

        verify(this.productRepository, times(1)).findByUuaa(anyString());
        verify(this.brokerRepository, times(1)).findAllBrokersSummary(any(), anyInt(), any(), any());
    }

    @Test
    void when_passed_arguments_then_return_broker_historical()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String status = BrokerStatus.values()[RandomUtils.nextInt(0, BrokerStatus.values().length)].name();
        final String type = RandomStringUtils.randomAlphabetic(10);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(6);

        final List<Object[]> mockRecords = buildMapOfHistoricalRecords(true);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.BROKERS.name()),
                eq(StatisticParamName.BROKERS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.BROKERS_STATUS.name()), eq(status),
                eq(StatisticParamName.BROKERS_TYPE.name()), eq(type),
                eq(StatisticParamName.BROKERS_UUAA.name()), eq(uuaa),
                eq(StatisticParamName.BROKERS_PLATFORM.name()), eq(platform),
                eq(StatisticParamName.BROKERS_ENVIRONMENT.name()))).thenReturn(mockRecords);
        // when
        final STHistoricalSerie[] response = this.serviceStatisticsapi.getBrokersHistorical(startDate, endDate, environment, status, type, uuaa, platform);

        // then
        assertRecordsOfHistoricalCategory(mockRecords, response, true);
    }

    @Test
    void when_passed_arguments_then_return_broker_historical_csv()
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String status = BrokerStatus.values()[RandomUtils.nextInt(0, BrokerStatus.values().length)].name();
        final String type = RandomStringUtils.randomAlphabetic(10);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(6);

        final List<Object[]> mockRecords = buildMapOfHistoricalBrokerRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getStatisticsHistoricalTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.BROKERS.name()),
                eq(StatisticParamName.BROKERS_UUAA.name()), eq(uuaa),
                eq(StatisticParamName.BROKERS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.BROKERS_TYPE.name()), eq(type),
                eq(StatisticParamName.BROKERS_PLATFORM.name()), eq(platform),
                eq(StatisticParamName.BROKERS_STATUS.name()), eq(status)
        )).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getBrokersHistoricalExport(startDate, endDate, environment, status, type, uuaa, platform, FORMAT_CSV);

        // then
        assertRecordsOfCSVBrokerHistoricalCategory(mockRecords, response, false);
    }

    @Test
    void when_passed_arguments_then_return_broker_historical_xlsx() throws Exception
    {
        // given
        final String startDate = RandomStringUtils.randomAlphabetic(11);
        final String endDate = RandomStringUtils.randomAlphabetic(10);
        final String environment = Environment.values()[RandomUtils.nextInt(0, Environment.values().length)].name();
        final String status = BrokerStatus.values()[RandomUtils.nextInt(0, BrokerStatus.values().length)].name();
        final String type = RandomStringUtils.randomAlphabetic(10);
        final String uuaa = RandomStringUtils.randomAlphabetic(4);
        final String platform = RandomStringUtils.randomAlphabetic(6);

        final List<Object[]> mockRecords = buildMapOfHistoricalBrokerRecords(false);
        when(this.validDateRangeProvider.getValidDateRange(startDate, endDate)).thenReturn(Pair.of(startDate, endDate));
        when(this.statisticRepository.getStatisticsHistoricalTotalValueBetweenDates(eq(startDate), eq(endDate), eq(StatisticType.BROKERS.name()),
                eq(StatisticParamName.BROKERS_UUAA.name()), eq(uuaa),
                eq(StatisticParamName.BROKERS_ENVIRONMENT.name()), eq(environment),
                eq(StatisticParamName.BROKERS_TYPE.name()), eq(type),
                eq(StatisticParamName.BROKERS_PLATFORM.name()), eq(platform),
                eq(StatisticParamName.BROKERS_STATUS.name()), eq(status)
        )).thenReturn(mockRecords);

        // when
        final byte[] response = this.serviceStatisticsapi.getBrokersHistoricalExport(startDate, endDate, environment, status, type, uuaa, platform, FORMAT_XLSX);

        // then
        assertRecordsOfXLSXBrokerHistoricalCategory(mockRecords, response, false);
    }

    private PBAvailabilityNovaCoinsDTO[] getDummyNovaCoinsDtos()
    {
        PBAvailabilityNovaCoinsDTO dto = new PBAvailabilityNovaCoinsDTO();
        dto.setEnvironment("INT");
        dto.setAvailable(100D);
        dto.setUsed(10D);
        return new PBAvailabilityNovaCoinsDTO[]{dto};
    }

    private Map<List<String>, Integer> getExpectedResultsForAllSubsystems()
    {
        Map<List<String>, Integer> map = new HashMap<>(12);
        map.put(List.of("UUAA2", "TYPE_A"), 5);
        map.put(List.of("UUAA2", "TYPE_B"), 5);
        map.put(List.of("UUAA1", "TYPE_B"), 3);
        map.put(List.of("UUAA1", "TYPE_A"), 5);
        return map;
    }

    private Map<List<String>, Integer> getExpectedResultsForSubsystemsWithUuaa()
    {
        Map<List<String>, Integer> map = new HashMap<>(12);
        map.put(List.of("UUAA2", "TYPE_A"), 5);
        map.put(List.of("UUAA2", "TYPE_B"), 5);
        map.put(List.of("UUAA1", "TYPE_A"), 5);
        return map;
    }

    private TOSubsystemsCombinationDTO[] getDummySubsystemsSnapshotDtos()
    {
        Integer[] productIds = new Integer[]{1, 2, 3, 4, 5, 6};
        Integer[] counts = new Integer[]{2, 3, 2, 3, 3, 5};
        String[] types = new String[]{"TYPE_A", "TYPE_A", "TYPE_B", "TYPE_B", "TYPE_B", "TYPE_A"};
        TOSubsystemsCombinationDTO[] dtos = new TOSubsystemsCombinationDTO[6];
        for (int i = 0; i < 6; i++)
        {
            TOSubsystemsCombinationDTO dto = new TOSubsystemsCombinationDTO();
            dto.setSubsystemType(types[i]);
            dto.setCount(counts[i]);
            dto.setProductId(productIds[i]);
            dtos[i] = dto;
        }
        return dtos;
    }

    private PBHardwareBudgetSnapshot[] getDummyDtos()
    {
        List<String> environments = List.of("INT", "INT", "PRE", "PRE");
        List<String> valueTypes = List.of("AAA", "AAA", "BBB", "BBB");
        List<Double> values = List.of(5D, 4D, 8D, 10D);
        PBHardwareBudgetSnapshot[] dtos = new PBHardwareBudgetSnapshot[4];
        for (int i = 0; i < 4; i++)
        {
            PBHardwareBudgetSnapshot dto = new PBHardwareBudgetSnapshot();
            dto.setEnvironment(environments.get(i));
            dto.setValueType(valueTypes.get(i));
            dto.setProductId(i + 1);
            dto.setValue(values.get(i));
            dtos[i] = dto;
        }
        return dtos;
    }

    private UserProductRoleHistoryDTO[] getUserProductRoleDummyDtos()
    {
        List<String> roles = List.of("ROLE_A", "ROLE_A", "ROLE_B", "ROLE_B");
        List<Integer> productIds = List.of(1, 2, 3, 4);
        List<String> uuaas = List.of("UUAA1", "UUAA2", "UUAA3", "UUAA4");
        List<Integer> values = List.of(5, 4, 8, 10);
        UserProductRoleHistoryDTO[] dtos = new UserProductRoleHistoryDTO[4];
        for (int i = 0; i < 4; i++)
        {
            UserProductRoleHistoryDTO dto = new UserProductRoleHistoryDTO();
            dto.setRole(roles.get(i));
            dto.setProductId(productIds.get(i));
            dto.setUuaa(uuaas.get(i));
            dto.setValue(values.get(i));
            dtos[i] = dto;
        }
        return dtos;
    }

    private List<Object[]> getDummyProductIdUuaaPairs()
    {
        return List.of(new Object[]{1, "FLOW"}, new Object[]{2, "FLOW"}, new Object[]{3, "TEST"}, new Object[]{4, "TEST"});
    }
}