package com.bbva.enoa.platformservices.coreservice.productsapi.services.impl.dummy;

import com.bbva.enoa.apirestgen.productsapi.model.ProductServiceAssignedResourcesReportDTO;
import com.bbva.enoa.apirestgen.productsapi.model.ProductsAssignedResourcesReport;
import com.bbva.enoa.apirestgen.toolsapi.model.TOSubsystemDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DummyProductAssignedResourcesGenerator
{
    private static final Object[][] DUMMY_RESULT_SET = new Object[][]{
            {
                    null, null, null, null, null, null, 8D, BigDecimal.valueOf(14L), BigDecimal.valueOf(3072L), BigInteger.valueOf(7L)
            },
            {
                    "Flow Manager", "NoQualityRelesae", "Pomodoro", Timestamp.valueOf("2020-12-03 13:23:03"), "subsystem_98227", "alertgenerator", 0.5D, BigDecimal.valueOf(1L), BigDecimal.valueOf(512L), BigInteger.ONE
            },
            {
                    "Flow Manager", "QualityRelease", "LaMancha", Timestamp.valueOf("2020-12-03 13:23:06"), "subsystem_98206", "flowdaemon", 1D, BigDecimal.valueOf(2L), BigDecimal.valueOf(512L), BigInteger.ONE
            },
            {
                    "Hazelcast by NOVA", "hazelcastInstances", "v2.1.2", Timestamp.valueOf("2020-11-30 20:17:51"), "subsystem_96670", "client", 0.5D, BigDecimal.valueOf(2L), BigDecimal.valueOf(512L), BigInteger.ONE
            },
            {
                    "Hazelcast by NOVA", "hazelcastInstances", "v2.1.2", Timestamp.valueOf("2020-11-30 20:17:51"), "subsystem_96670", "cluster", 1D, BigDecimal.valueOf(1L), BigDecimal.valueOf(512L), BigInteger.ONE
            },
            {
                    "Hazelcast by NOVA", "hazelcastInstances", "v2.1.2", Timestamp.valueOf("2020-11-30 20:17:51"), "subsystem_96670", "eurekahazelcast", 0.5D, BigDecimal.valueOf(1L), BigDecimal.valueOf(512L), BigInteger.ONE
            },
            {
                    "Hazelcast by NOVA", "hazelcastInstances", "v2.1.2", Timestamp.valueOf("2020-11-30 20:17:51"), "subsystem_96672", "client", 0.5D, BigDecimal.valueOf(1L), BigDecimal.valueOf(512L), BigInteger.ONE
            },
            {
                    "Hazelcast by NOVA", "hazelcastInstances", "v2.1.2", Timestamp.valueOf("2020-11-30 20:17:51"), "subsystem_96672", "cluster", 0.5D, BigDecimal.valueOf(1L), BigDecimal.valueOf(512L), BigInteger.ONE
            },
            {
                    "Hazelcast by NOVA", "hazelcastInstances", "v2.1.2", Timestamp.valueOf("2020-11-30 20:17:51"), "subsystem_96672", "eurekahazelcast", 0.5D, BigDecimal.valueOf(1L), BigDecimal.valueOf(512L), BigInteger.ONE
            },
            {
                    "Hazelcast by NOVA", "hazelcastInstances", "v2.1.2", Timestamp.valueOf("2020-11-30 20:17:51"), "subsystem_96672", "client", 0.5D, BigDecimal.valueOf(1L), BigDecimal.valueOf(512L), BigInteger.ONE
            },
            {
                    "JCAS", "filetransfer", "RVFileTransfer", Timestamp.valueOf("2020-03-19 19:53:31"), "subsystem_34876", "filetransfer", 0.5D, BigDecimal.valueOf(1L), BigDecimal.valueOf(512L), BigInteger.ONE
            },
            {
                    "ServicesFix", "ultimate", "hostname3.0.6_batch", Timestamp.valueOf("2020-11-05 18:54:09"), "subsystem_86554", "hostnamebatch", 0.5D, BigDecimal.valueOf(1L), BigDecimal.valueOf(512L), BigInteger.ONE
            },
            {
                    "Servicio de transcferencia", "filetransfer", "RVFileTransfer", Timestamp.valueOf("2020-03-19 18:08:47"), "subsystem_34802", "filetransfer", 0.5D, BigDecimal.valueOf(1L), BigDecimal.valueOf(512L), BigInteger.ONE
            }
    };

    private DummyProductAssignedResourcesGenerator()
    {

    }

    public static List<Object[]> getDummyDatabaseResults()
    {
        return Arrays.asList(DUMMY_RESULT_SET);
    }

    public static Object[] getRecordAtPosition(final int position)
    {
        return DUMMY_RESULT_SET[position];
    }

    public static List<TOSubsystemDTO> getDummySubsystems()
    {
        final Set<String> subsystemNames = Arrays.stream(DUMMY_RESULT_SET).filter(e -> e[0] != null).map(e -> (String) e[4]).collect(Collectors.toSet());
        List<TOSubsystemDTO> toSubsystemDTOS = new ArrayList<>();
        for (String subsystemName : subsystemNames)
        {
            TOSubsystemDTO dto = new TOSubsystemDTO();
            dto.setSubsystemId(Integer.valueOf(subsystemName.split("_")[1]));
            dto.setSubsystemName(subsystemName);
            toSubsystemDTOS.add(dto);
        }
        return toSubsystemDTOS;
    }

    public static ProductsAssignedResourcesReport getDummyProductsAssignedResourcesReport()
    {
        ProductsAssignedResourcesReport dto = new ProductsAssignedResourcesReport();
        dto.setServices(getDummyProductServiceAssignedResourcesReportDtos());
        Object[] totalInformationRecord = DUMMY_RESULT_SET[0];
        dto.setTotalCPUs((Double) totalInformationRecord[6]);
        dto.setTotalInstances(((BigDecimal) totalInformationRecord[7]).longValue());
        dto.setTotalMemory(((BigDecimal) totalInformationRecord[8]).doubleValue() / 1000);
        dto.setTotalElementsCount(((BigInteger) totalInformationRecord[9]).intValue());
        int length = DUMMY_RESULT_SET.length;
        dto.setTotalServices((long) length);
        return dto;
    }

    public static ProductServiceAssignedResourcesReportDTO[] getDummyProductServiceAssignedResourcesReportDtos()
    {
        ProductServiceAssignedResourcesReportDTO[] dtos = new ProductServiceAssignedResourcesReportDTO[DUMMY_RESULT_SET.length];
        for (int i = 1; i < DUMMY_RESULT_SET.length; i++)
        {
            Object[] currentResultSet = DUMMY_RESULT_SET[i];
            ProductServiceAssignedResourcesReportDTO dto = new ProductServiceAssignedResourcesReportDTO();
            dto.setProductName((String) currentResultSet[0]);
            dto.setReleaseName((String) currentResultSet[1]);
            dto.setReleaseVersionName((String) currentResultSet[2]);
            final Timestamp executionDate = (Timestamp) currentResultSet[3];
            Date date = new Date();
            date.setTime(executionDate.getTime());
            dto.setExecutionDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date));
            dto.setSubsystemName((String) currentResultSet[4]);
            final String serviceName = (String) currentResultSet[5];
            dto.setServiceName(serviceName);
            final double cpus = (Double) currentResultSet[6];
            dto.setCpus(cpus);
            final long instances = ((BigDecimal) currentResultSet[7]).longValue();
            dto.setInstances(instances);
            final double memory = ((BigDecimal) currentResultSet[8]).doubleValue() / 1000;
            dto.setMemory(memory);
            dtos[i] = dto;
        }
        return dtos;
    }
}
