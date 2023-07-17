package com.bbva.enoa.platformservices.coreservice.common.view;

import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by xe56809 on 13/03/2018.
 */
public class ProductCSVTest
{

    @Test
    public void getMethodsWithApiRest() throws Exception
    {
        getMethods(ServiceType.API_REST_JAVA_SPRING_BOOT);
    }
    @Test
    public void getMethodsWithApi() throws Exception
    {
        getMethods(ServiceType.API_JAVA_SPRING_BOOT);
    }

    private void getMethods(ServiceType serviceType) throws Exception
    {
        // Given
        ProductCSV productCSV = new ProductCSV("Name", "UUAA", "Description",
                Environment.LOCAL, "Release", "Version", Calendar.getInstance(),
                "Service", serviceType, 1, 1.0, 1024, 1);
        productCSV.setName("Name1");
        productCSV.setUuaa("UUAA1");
        productCSV.setDescription("Description1");
        productCSV.setEnvironment(Environment.LAB_INT);
        productCSV.setReleasename("Release1");
        productCSV.setVersionName("Version1");
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, 3, 13);
        productCSV.setEntryDate(calendar);
        productCSV.setSubsystemname("Subsystem1");
        productCSV.setSubsystemType(SubsystemType.EPHOENIX);
        productCSV.setServiceName("Service1");
        productCSV.setServiceType(ServiceType.EPHOENIX_ONLINE);
        productCSV.setNumberOfInstances(2);
        productCSV.setNumCPU(2.0);
        productCSV.setRamMB(2048);

        //Then
        assertEquals("Name1", productCSV.getName());
        assertEquals("UUAA1", productCSV.getUuaa());
        assertEquals("Description1", productCSV.getDescription());
        assertEquals(Environment.LAB_INT, productCSV.getEnvironment());
        assertEquals("Release1", productCSV.getReleasename());
        assertEquals("Version1", productCSV.getVersionName());
        assertTrue(productCSV.getEntryDate().contains("2016"));
        assertEquals("Subsystem1", productCSV.getSubsystemname());
        assertEquals(SubsystemType.EPHOENIX, productCSV.getSubsystemType());
        assertEquals("Service1", productCSV.getServiceName());
        assertEquals(ServiceType.EPHOENIX_ONLINE, productCSV.getServiceType());
        assertEquals("2", productCSV.getNumberOfInstances());
        assertEquals("2.0", productCSV.getNumCPU());
        assertEquals("2048", productCSV.getRamMB());
        assertEquals("Name1;UUAA1;Description1;LAB_INT;Release1;Version1;13/04/2016;Subsystem1;EPHOENIX;Service1;EPHOENIX_ONLINE;2;2.0;2048",
                productCSV.toString());
    }

}