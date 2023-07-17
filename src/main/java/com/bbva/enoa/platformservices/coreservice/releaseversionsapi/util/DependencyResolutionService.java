package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Utility class for resolving dependency order
 * 
 * @author Victor Bazaga Diaz
 *
 */
@Component
public class DependencyResolutionService implements IDependencyResolutionService
{

    /**
     * Get order in which dependency must be resolved/compiled from a Set of
     * nodes and its dependencies
     * 
     * @param graph
     *            Set of nodes with its dependencies
     * @return Ordered List of node names
     */
    @Override
    public List<String> getDependencyResolutionOrder(final Set<GraphNode> graph)
    {

        Set<GraphNode> currentGraph = graph;
        List<String> orderedNodeNames = new ArrayList<>();
        while (!currentGraph.isEmpty())
        {
            Set<GraphNode> outputGraph = new HashSet<>();

            for (GraphNode node : currentGraph)
            {

                // Dependencies on nodes already added to orderedDeps are
                // ignored
                node.getDependencies().removeAll(orderedNodeNames);
                // If no dependency left
                if (node.getDependencies().isEmpty())
                {
                    // Add to orderedNodes
                    orderedNodeNames.add(node.getNodeName());

                }
                else
                {
                    // Continue processing on next iteration
                    outputGraph.add(node);
                }
            }
            // If this iteration didnt found any leaf it means a loop is present
            if (graph.size() == outputGraph.size())
            {
                throw new IllegalArgumentException("Loop found in graph");
            }
            currentGraph = outputGraph;
        }
        return orderedNodeNames;
    }
}
