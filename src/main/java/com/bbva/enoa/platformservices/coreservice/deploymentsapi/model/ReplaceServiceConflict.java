package com.bbva.enoa.platformservices.coreservice.deploymentsapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Class for store all information about trying to replace a deployment plan over other deployment plan
 * Store all information about the service
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReplaceServiceConflict
{
    /** Service artifact id = service name **/
    private String artifactId;

    /** Service version **/
    private String version;

    /** number of instance to be deployed */
    private Integer numberOfInstances;

    /** Action to make whit this service: can be: KEEP THE SAME VERSIONS - REMOVE - CREATE **/
    private String action;
}
