package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util;

import java.util.List;
import java.util.Set;

/**
 * Interface for all possible implementation of dependency resolution service
 * 
 * @author Victor Bazaga Diaz
 *
 */
public interface IDependencyResolutionService
{

    /**
     * Get order in which dependency must be resolved/compiled from a Set of
     * nodes and its dependencies
     * 
     * @param graph
     *            Set of nodes with its dependencies
     * @return Ordered List of node names by its resolution order
     */
    List<String> getDependencyResolutionOrder(Set<GraphNode> graph);

}