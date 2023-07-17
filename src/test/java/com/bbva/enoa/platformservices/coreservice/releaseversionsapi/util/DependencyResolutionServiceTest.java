package com.bbva.enoa.platformservices.coreservice.releaseversionsapi.util;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;


public class DependencyResolutionServiceTest
{

    private IDependencyResolutionService depResolvService = new DependencyResolutionService();

    @Test
    public void testGetDependencyResolutionOrderDiamond()
    {

        HashSet<GraphNode> graph = new HashSet<GraphNode>(
                Arrays.asList(

                        new GraphNode("node1", new HashSet<>(Arrays.asList("node2","node3"))),

                        new GraphNode("node2", new HashSet<>(Arrays.asList("node4"))),


                        new GraphNode("node3", new HashSet<>(Arrays.asList("node4"))),

                        new GraphNode("node4", new HashSet<>())

                        ));
        List<String> result = depResolvService.getDependencyResolutionOrder(graph);
        Assertions.assertEquals(4, result.size());
        Map<String,Integer> positions= new HashMap<>();
        for (int i = 0; i < result.size(); i++)
        {
            positions.put(result.get(i), i);
        }
        Assertions.assertTrue(positions.get("node4") < positions.get("node3"));
        Assertions.assertTrue(positions.get("node4") < positions.get("node3"));
        Assertions.assertTrue(positions.get("node3") < positions.get("node1"));
        Assertions.assertTrue(positions.get("node2") < positions.get("node1"));

    }

    @Test
    public void testGetDependencyResolutionOrderEmtptySet()
    {

        List<String> result = depResolvService.getDependencyResolutionOrder(new HashSet<>());
        Assertions.assertEquals(0, result.size());

    }

    @Test
    public void testGetDependencyResolutionOrderHexagon()
    {

        HashSet<GraphNode> graph = new HashSet<GraphNode>(
                Arrays.asList(

                        new GraphNode("node1", new HashSet<>(Arrays.asList("node2","node3"))),

                        new GraphNode("node2", new HashSet<>(Arrays.asList("node4"))),


                        new GraphNode("node3", new HashSet<>(Arrays.asList("node5"))),

                        new GraphNode("node4", new HashSet<>(Arrays.asList("node6"))),

                        new GraphNode("node5", new HashSet<>(Arrays.asList("node6"))),

                        new GraphNode("node6", new HashSet<>())

                        ));
        List<String> result = depResolvService.getDependencyResolutionOrder(graph);
        Map<String, Integer> positions = new HashMap<>();
        for (int i = 0; i < result.size(); i++)
        {
            positions.put(result.get(i), i);
        }
        Assertions.assertEquals(6, result.size());
        Assertions.assertTrue(positions.get("node6") < positions.get("node5"));
        Assertions.assertTrue(positions.get("node6") < positions.get("node4"));
        Assertions.assertTrue(positions.get("node4") < positions.get("node2"));
        Assertions.assertTrue(positions.get("node5") < positions.get("node3"));
        Assertions.assertTrue(positions.get("node2") < positions.get("node1"));
        Assertions.assertTrue(positions.get("node3") < positions.get("node1"));

    }

    @Test
    public void testGetDependencyResolutionOrderHexagonCrossed()
    {

        HashSet<GraphNode> graph = new HashSet<GraphNode>(
                Arrays.asList(

                        new GraphNode("node1", new HashSet<>(Arrays.asList("node2","node3"))),

                        new GraphNode("node2", new HashSet<>(Arrays.asList("node4","node5"))),

                        new GraphNode("node3", new HashSet<>(Arrays.asList("node5","node4"))),

                        new GraphNode("node4", new HashSet<>(Arrays.asList("node6"))),

                        new GraphNode("node5", new HashSet<>(Arrays.asList("node6"))),

                        new GraphNode("node6", new HashSet<>())

                        ));
        Map<String, Integer> positions = new HashMap<>();
        List<String> result = depResolvService.getDependencyResolutionOrder(graph);
        for (int i = 0; i < result.size(); i++)
        {
            positions.put(result.get(i), i);
        }
        Assertions.assertEquals(6, result.size());
        Assertions.assertTrue(positions.get("node6") < positions.get("node5"));
        Assertions.assertTrue(positions.get("node6") < positions.get("node4"));
        Assertions.assertTrue(positions.get("node4") < positions.get("node2"));
        Assertions.assertTrue(positions.get("node4") < positions.get("node3"));
        Assertions.assertTrue(positions.get("node5") < positions.get("node3"));
        Assertions.assertTrue(positions.get("node5") < positions.get("node2"));
        Assertions.assertTrue(positions.get("node2") < positions.get("node1"));
        Assertions.assertTrue(positions.get("node3") < positions.get("node1"));

    }

    @Test
    public void testGetDependencyResolutionOrderHexagonLoop()
    {

        HashSet<GraphNode> graph = new HashSet<GraphNode>(
                Arrays.asList(

                        new GraphNode("node1", new HashSet<>(Arrays.asList("node2","node3"))),

                        new GraphNode("node2", new HashSet<>(Arrays.asList("node4"))),


                        new GraphNode("node3", new HashSet<>(Arrays.asList("node5"))),

                        new GraphNode("node4", new HashSet<>(Arrays.asList("node6"))),

                        new GraphNode("node5", new HashSet<>(Arrays.asList("node6"))),

                        new GraphNode("node6", new HashSet<>(Arrays.asList("node3")))

                        ));
        Map<String, Integer> positions = new HashMap<>();
        Assertions.assertThrows(IllegalArgumentException.class, () -> depResolvService.getDependencyResolutionOrder(graph));

    }

    @Test
    public void testGetDependencyResolutionOrderOneNode()
    {

        HashSet<GraphNode> graph = new HashSet<>();
        GraphNode node = new GraphNode("node1", new HashSet<>());
        graph.add(node);
        List<String> result = depResolvService.getDependencyResolutionOrder(graph);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("node1", result.get(0));

    }

    @Test
    public void testGetDependencyResolutionOrderTwoNodes()
    {

        HashSet<GraphNode> graph = new HashSet<GraphNode>(
                Arrays.asList(

                        new GraphNode("node1", new HashSet<>(Arrays.asList("node2"))),

                        new GraphNode("node2", new HashSet<>())

                        ));
        List<String> result = depResolvService.getDependencyResolutionOrder(graph);
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("node2", result.get(0));
        Assertions.assertEquals("node1", result.get(1));

    }

    @Test
    public void testGetDependencyResolutionOrderTwoNodesCycle()
    {

        HashSet<GraphNode> graph = new HashSet<GraphNode>(Arrays.asList(

                new GraphNode("node1", new HashSet<>(Arrays.asList("node2",""))),

                new GraphNode("node2", new HashSet<>(Collections.singletonList("node1")))

                ));
        Assertions.assertThrows(IllegalArgumentException.class, () -> depResolvService.getDependencyResolutionOrder(graph));

    }

}
