package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representation of a graph o fnodes and dependencies
 * 
 * @author Victor Bazaga Diaz
 *
 */
@Getter
@AllArgsConstructor
public class GraphNode
{

    String      nodeName;
    Set<String> dependencies;
}
