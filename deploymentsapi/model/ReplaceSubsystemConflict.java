package com.bbva.enoa.platformservices.coreservice.deploymentsapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for store all information about trying to replace a deployment plan over other deployment plan
 * Store the information about the subsytem
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ReplaceSubsystemConflict
{
    /** Subsystem name **/
    private String subsystemName;

    /** Subsystem tag **/
    private String tagName;

    /** Action to make whit this subsystem: can be: KEEP THE SAME VERSIONS - UPDATE TAG TO - UPDATE INSTANCES TO - REMOVE - CREATE **/
    private String action;

    /** Replace service conflict list **/
    private List<ReplaceServiceConflict> replaceServiceConflictList = new ArrayList<>();
}
