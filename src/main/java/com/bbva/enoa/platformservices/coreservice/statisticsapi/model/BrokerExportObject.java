package com.bbva.enoa.platformservices.coreservice.statisticsapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Class for store information of the brokers
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BrokerExportObject
{
    /**
     * Broker id
     **/
    private Integer id;

    /**
     * UUAA
     */
    private String uuaa;

    /**
     * Environment
     */
    private String environment;

    /**
     * Broker name
     */
    private String name;

    /**
     * Type
     */
    private String type;

    /**
     * Platform
     */
    private String platform;

    /**
     * Status
     */
    private String status;

    /**
     * Number of nodes
     **/
    private Integer numberOfNodes;

    /**
     * Cpu
     **/
    private Double cpu;

    /**
     * Memory
     **/
    private Integer memory;

    /**
     * Creation date
     **/
    private String creationDate;


}

