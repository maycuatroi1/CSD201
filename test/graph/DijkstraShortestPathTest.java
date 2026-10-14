package graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class DijkstraShortestPathTest {

    @Test
    public void distancesFromAOnTheSampleGraph() {
        for (AbstractGraph<String> graph : SampleWeightedGraphs.bothRepresentations()) {
            DijkstraShortestPath<String> paths = new DijkstraShortestPath<>(graph, "A");
            assertEquals(0, paths.distanceTo("A"));
            assertEquals(7, paths.distanceTo("B"));
            assertEquals(9, paths.distanceTo("C"));
            assertEquals(20, paths.distanceTo("D"));
            assertEquals(20, paths.distanceTo("E"));
            assertEquals(11, paths.distanceTo("F"));
        }
    }

    @Test
    public void theShortestPathFromAToEGoesThroughCAndF() {
        for (AbstractGraph<String> graph : SampleWeightedGraphs.bothRepresentations()) {
            DijkstraShortestPath<String> paths = new DijkstraShortestPath<>(graph, "A");
            assertEquals(Arrays.asList("A", "C", "F", "E"), paths.pathTo("E"));
            assertEquals(Arrays.asList("A", "C", "D"), paths.pathTo("D"));
            assertEquals(Arrays.asList("A"), paths.pathTo("A"));
            assertEquals("A", paths.getSource());
        }
    }

    @Test
    public void twoEdgesCanBeatOneHeavierEdge() {
        AbstractGraph<String> graph = new AdjacencyListGraph<>(Arrays.asList("A", "B", "C"));
        graph.addEdge("A", "C", 10);
        graph.addEdge("A", "B", 3);
        graph.addEdge("B", "C", 4);
        DijkstraShortestPath<String> paths = new DijkstraShortestPath<>(graph, "A");
        assertEquals(7, paths.distanceTo("C"));
        assertEquals(Arrays.asList("A", "B", "C"), paths.pathTo("C"));
    }

    @Test
    public void aVertexInAnotherComponentHasNoPath() {
        DijkstraShortestPath<String> paths = new DijkstraShortestPath<>(SampleWeightedGraphs.twoIslands(), "P");
        assertTrue(paths.hasPathTo("R"));
        assertFalse(paths.hasPathTo("S"));
        assertEquals(Integer.MAX_VALUE, paths.distanceTo("S"));
        assertEquals(Collections.emptyList(), paths.pathTo("S"));
        assertEquals(5, paths.distanceTo("R"));
    }

    @Test
    public void zeroWeightEdgesAreFine() {
        AbstractGraph<String> graph = new AdjacencyListGraph<>(Arrays.asList("A", "B"));
        graph.addEdge("A", "B", 0);
        DijkstraShortestPath<String> paths = new DijkstraShortestPath<>(graph, "A");
        assertEquals(0, paths.distanceTo("B"));
        assertEquals(Arrays.asList("A", "B"), paths.pathTo("B"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void aNegativeWeightIsRejected() {
        AbstractGraph<String> graph = new AdjacencyListGraph<>(Arrays.asList("A", "B"));
        graph.addEdge("A", "B", -1);
        new DijkstraShortestPath<>(graph, "A");
    }

    @Test(expected = IllegalArgumentException.class)
    public void anUnknownSourceIsRejected() {
        new DijkstraShortestPath<>(SampleWeightedGraphs.list(), "Z");
    }
}
