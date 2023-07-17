package com.bbva.enoa.platformservices.coreservice.statisticsapi.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

class BrokerExportObjectTest
{

    @Test
    void noArgumentsTest()
    {
        final int id = 1;
        final String uuaa = "uuaa";
        final String environment = "environment";
        final String name = "name";
        final String type = "type";
        final String platform = "platform";
        final String status = "status";
        final Integer numberOfNodes = 2;
        final Double cpu = 1D;
        final Integer memory = 1024;
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        final String creationDate = format1.format(new Date());

        BrokerExportObject brokerExportObject = new BrokerExportObject();
        brokerExportObject.setUuaa(uuaa);
        brokerExportObject.setId(id);
        brokerExportObject.setEnvironment(environment);
        brokerExportObject.setName(name);
        brokerExportObject.setType(type);
        brokerExportObject.setPlatform(platform);
        brokerExportObject.setStatus(status);
        brokerExportObject.setNumberOfNodes(numberOfNodes);
        brokerExportObject.setCpu(cpu);
        brokerExportObject.setMemory(memory);
        brokerExportObject.setCreationDate(creationDate);

        Assertions.assertEquals(id, brokerExportObject.getId());
        Assertions.assertEquals(uuaa, brokerExportObject.getUuaa());
        Assertions.assertEquals(environment, brokerExportObject.getEnvironment());
        Assertions.assertEquals(name, brokerExportObject.getName());
        Assertions.assertEquals(type, brokerExportObject.getType());
        Assertions.assertEquals(platform, brokerExportObject.getPlatform());
        Assertions.assertEquals(status, brokerExportObject.getStatus());
        Assertions.assertEquals(numberOfNodes, brokerExportObject.getNumberOfNodes());
        Assertions.assertEquals(cpu, brokerExportObject.getCpu());
        Assertions.assertEquals(memory, brokerExportObject.getMemory());
        Assertions.assertEquals(creationDate, brokerExportObject.getCreationDate());
    }

    @Test
    void allArgumentsTest()
    {
        final int id = 1;
        final String uuaa = "uuaa";
        final String environment = "environment";
        final String name = "name";
        final String type = "type";
        final String platform = "platform";
        final String status = "status";
        final int numberOfNodes = 2;
        final Double cpu = 1D;
        final Integer memory = 1024;
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        final String creationDate = format1.format(new Date());

        BrokerExportObject brokerExportObject = new BrokerExportObject(id, uuaa, environment, name, type, platform, status, numberOfNodes, cpu, memory, creationDate);

        Assertions.assertEquals(uuaa, brokerExportObject.getUuaa());
        Assertions.assertEquals(environment, brokerExportObject.getEnvironment());
        Assertions.assertEquals(name, brokerExportObject.getName());
        Assertions.assertEquals(type, brokerExportObject.getType());
        Assertions.assertEquals(platform, brokerExportObject.getPlatform());
        Assertions.assertEquals(status, brokerExportObject.getStatus());
        Assertions.assertEquals(numberOfNodes, brokerExportObject.getNumberOfNodes());
        Assertions.assertEquals(cpu, brokerExportObject.getCpu());
        Assertions.assertEquals(memory, brokerExportObject.getMemory());
        Assertions.assertEquals(creationDate, brokerExportObject.getCreationDate());
    }

    @Test
    void toStringTest()
    {
        final int id = 1;
        final String uuaa = "uuaa";
        final String environment = "environment";
        final String name = "name";
        final String type = "type";
        final String platform = "platform";
        final String status = "status";
        final Integer numberOfNodes = 2;
        final Double cpu = 1D;
        final Integer memory = 1024;
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        final String creationDate = format1.format(new Date());

        BrokerExportObject brokerExportObject = new BrokerExportObject(id, uuaa, environment, name, type, platform, status, numberOfNodes, cpu, memory, creationDate);


        Assertions.assertEquals(
                String.format("BrokerExportObject(id=%s, uuaa=%s, environment=%s, name=%s, type=%s, platform=%s, status=%s, numberOfNodes=%s, cpu=%s, memory=%s, creationDate=%s)",
                        id,
                        uuaa,
                        environment,
                        name,
                        type,
                        platform,
                        status,
                        numberOfNodes,
                        cpu,
                        memory,
                        creationDate),
                brokerExportObject.toString());
    }
}
