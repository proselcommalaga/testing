package com.bbva.enoa.platformservices.coreservice.common.view;


import com.bbva.enoa.core.novabootstarter.enumerate.Environment;
import com.bbva.enoa.core.novabootstarter.enumerate.ServiceType;
import com.bbva.enoa.core.novabootstarter.enumerate.SubsystemType;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Product CSV
 */
@Getter
@Setter
public class ProductCSV
{
    /**
     * Product  name
     */
    private String name;

    /**
     * Product  uuaa
     */
    private String uuaa;


    /**
     * Product  description
     */
    private String description;

    /**
     * deployment_plan environment
     */
    private Environment environment;

    /**
     * Release name
     */
    private String releasename;

    /**
     * Release version Name
     */
    private String versionName;


    /**
     * executation Plan date
     */
    private Calendar entryDate;
    /**
     * Subsystem Name
     */
    private String subsystemname;
    /**
     * Subsystem Type
     */
    private SubsystemType subsystemType;
    /**
     * Service Name
     */
    private String serviceName;

    /**
     * Service Type
     */
    private ServiceType serviceType;
    /**
     * Number of instances
     */
    private int numberOfInstances;
    /**
     * Number of CPUs
     */
    private double numCPU;
    /**
     * Ram
     */
    private int ramMB;

    /**
     * Subsystem identifier
     */
    private int subsystemId;

    /**
     * Get entry date
     *
     * @return entry date
     */
    public String getEntryDate()
    {
        String formattedEntryDate = "";
        if (this.entryDate != null)
        {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            formattedEntryDate = sdf.format(this.entryDate.getTime());
        }
        return formattedEntryDate;
    }

    /**
     * Get number of instances
     *
     * @return number of instances
     */
    public String getNumberOfInstances()
    {
        return String.valueOf(this.numberOfInstances);
    }

    /**
     * Get num CPU
     *
     * @return num cpu
     */
    public String getNumCPU()
    {
        return String.valueOf(this.numCPU);
    }

    /**
     * Get ram mb
     *
     * @return ram mb
     */
    public String getRamMB()
    {
        return String.valueOf(this.ramMB);
    }

    @Override
    public String toString()
    {

        return name + ";" + uuaa + ";" + description + ";" + environment + ";" + releasename + ";" + versionName + ";" + this.getEntryDate() + ";"
                + subsystemname + ";" + subsystemType + ";" + serviceName + ";" + serviceType + ";" + this.getNumberOfInstances() + ";" + this
                .getNumCPU() + ";" + this.getRamMB();

    }

    public String[] getCSVValues()
    {
        return new String[]{name, uuaa, description, environment.getEnvironment(), releasename, versionName, this.getEntryDate(),
                subsystemname, subsystemType.name(), serviceName, serviceType.name(), this.getNumberOfInstances(), this.getNumCPU(), this.getRamMB()};
    }

    public ProductCSV(String name, String uuaa, String description, Environment environment, String releasename, String versionName, Calendar entryDate, String serviceName, ServiceType serviceType, int numberOfInstances, double numCPU, int ramMB,
                      int subsystemId)
    {
        this.name = name;
        this.uuaa = uuaa;
        this.description = description;
        this.environment = environment;
        this.releasename = releasename;
        this.versionName = versionName;
        this.entryDate = entryDate;
        this.serviceName = serviceName;
        this.serviceType = serviceType;
        this.numberOfInstances = numberOfInstances;
        this.numCPU = numCPU;
        this.ramMB = ramMB;
        this.subsystemId = subsystemId;
    }

    public ProductCSV(String name, String uuaa, String description, String environment, String releasename, String versionName, Calendar entryDate, String serviceName, String serviceType, int numberOfInstances, double numCPU, int ramMB,
                      int subsystemId)
    {
        this.name = name;
        this.uuaa = uuaa;
        this.description = description;
        this.environment = Environment.valueOf(environment);
        this.releasename = releasename;
        this.versionName = versionName;
        this.entryDate = entryDate;
        this.serviceName = serviceName;
        this.serviceType = ServiceType.valueOf(serviceType);
        this.numberOfInstances = numberOfInstances;
        this.numCPU = numCPU;
        this.ramMB = ramMB;
        this.subsystemId = subsystemId;
    }
}
