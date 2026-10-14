package graph;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class MinimumSpanningTreeTest {

    private static final List<String> SAMPLE_VERTICES = Arrays.asList("A", "B", "C", "D", "E", "F");
    private static final List<Integer> ISLAND_VERTICES = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);

    @Test
    public void primGrowsTheSampleTreeFromA() {
        for (AbstractGraph<String> graph : sampleGraphs()) {
            PrimMST<String> prim = new PrimMST<>(graph, "A");
            assertEquals("[A-B(7), A-C(9), C-F(2), F-E(9), E-D(6)]", prim.getEdges().toString());
            assertEquals(33, prim.getWeight());
            assertEquals(Arrays.asList("A", "B", "C", "F", "E", "D"), prim.getVertices());
        }
    }

    @Test
    public void primFromAnotherStartFindsTheSameTreeInAnotherOrder() {
        for (AbstractGraph<String> graph : sampleGraphs()) {
            PrimMST<String> prim = new PrimMST<>(graph, "D");
            assertEquals("[D-E(6), E-F(9), F-C(2), C-A(9), A-B(7)]", prim.getEdges().toString());
            assertEquals(33, prim.getWeight());
        }
    }

    @Test
    public void kruskalTakesTheSampleEdgesLightestFirst() {
        for (AbstractGraph<String> graph : sampleGraphs()) {
            KruskalMST<String> kruskal = new KruskalMST<>(graph);
            assertEquals("[C-F(2), D-E(6), A-B(7), A-C(9), E-F(9)]", kruskal.getEdges().toString());
            assertEquals(33, kruskal.getWeight());
        }
    }

    @Test
    public void bothAlgorithmsSpanTheNineVertexGraphWithWeight82() {
        for (AbstractGraph<Integer> graph : nineVertexGraphs()) {
            PrimMST<Integer> prim = new PrimMST<>(graph, 0);
            assertEquals("[0-1(8), 1-4(9), 0-2(12), 2-3(14), 3-5(8), 5-7(11), 7-8(9), 8-6(11)]",
                    prim.getEdges().toString());
            assertEquals(82, prim.getWeight());
            KruskalMST<Integer> kruskal = new KruskalMST<>(graph);
            assertEquals("[0-1(8), 3-5(8), 1-4(9), 7-8(9), 5-7(11), 6-8(11), 0-2(12), 2-3(14)]",
                    kruskal.getEdges().toString());
            assertEquals(82, kruskal.getWeight());
        }
    }

    @Test
    public void primReachesOnlyTheComponentOfTheStartVertex() {
        PrimMST<String> prim = new PrimMST<>(twoIslands(), "P");
        assertEquals("[P-Q(1), Q-R(4)]", prim.getEdges().toString());
        assertEquals(5, prim.getWeight());
        assertEquals(Arrays.asList("P", "Q", "R"), prim.getVertices());
    }

    @Test
    public void kruskalBuildsAForestOnADisconnectedGraph() {
        KruskalMST<String> kruskal = new KruskalMST<>(twoIslands());
        assertEquals("[P-Q(1), S-T(2), Q-R(4)]", kruskal.getEdges().toString());
        assertEquals(7, kruskal.getWeight());
    }

    @Test
    public void equalWeightsAreTriedInTheOrderTheRepresentationListsThem() {
        AbstractGraph<Character> list = new AdjacencyListGraph<>(Arrays.asList('a', 'b', 'c'));
        AbstractGraph<Character> matrix = new AdjacencyMatrixGraph<>(Arrays.asList('a', 'b', 'c'));
        for (AbstractGraph<Character> graph : Arrays.asList(list, matrix)) {
            graph.addEdge('a', 'c', 1);
            graph.addEdge('a', 'b', 1);
            graph.addEdge('b', 'c', 5);
        }
        assertEquals("[a-c(1), a-b(1)]", new PrimMST<>(list, 'a').getEdges().toString());
        assertEquals("[a-b(1), a-c(1)]", new PrimMST<>(matrix, 'a').getEdges().toString());
        assertEquals("[a-c(1), a-b(1)]", new KruskalMST<>(list).getEdges().toString());
        assertEquals("[a-b(1), a-c(1)]", new KruskalMST<>(matrix).getEdges().toString());
        assertEquals(2, new PrimMST<>(matrix, 'a').getWeight());
        assertEquals(2, new KruskalMST<>(list).getWeight());
    }

    @Test
    public void aSelfLoopNeverJoinsTheTree() {
        AbstractGraph<Character> graph = new AdjacencyListGraph<>(Arrays.asList('a', 'b'));
        graph.addEdge('a', 'a', 1);
        graph.addEdge('a', 'b', 5);
        assertEquals("[a-b(5)]", new PrimMST<>(graph, 'a').getEdges().toString());
        assertEquals("[a-b(5)]", new KruskalMST<>(graph).getEdges().toString());
        assertEquals(5, new KruskalMST<>(graph).getWeight());
    }

    @Test
    public void oneVertexHasAnEmptyTreeOfWeightZero() {
        AbstractGraph<Character> graph = new AdjacencyListGraph<>(Arrays.asList('a'));
        PrimMST<Character> prim = new PrimMST<>(graph, 'a');
        assertEquals(Collections.emptyList(), prim.getEdges());
        assertEquals(0, prim.getWeight());
        assertEquals(Arrays.asList('a'), prim.getVertices());
        assertEquals(Collections.emptyList(), new KruskalMST<>(graph).getEdges());
        assertEquals(0, new KruskalMST<>(graph).getWeight());
    }

    @Test(expected = IllegalArgumentException.class)
    public void primRejectsAnUnknownStartVertex() {
        new PrimMST<>(twoIslands(), "Z");
    }

    private static List<AbstractGraph<String>> sampleGraphs() {
        return Arrays.asList(connectSample(new AdjacencyListGraph<String>(SAMPLE_VERTICES)),
                connectSample(new AdjacencyMatrixGraph<String>(SAMPLE_VERTICES)));
    }

    private static AbstractGraph<String> connectSample(AbstractGraph<String> graph) {
        graph.addEdge("A", "B", 7);
        graph.addEdge("A", "C", 9);
        graph.addEdge("B", "C", 10);
        graph.addEdge("B", "D", 15);
        graph.addEdge("C", "D", 11);
        graph.addEdge("D", "E", 6);
        graph.addEdge("A", "F", 14);
        graph.addEdge("C", "F", 2);
        graph.addEdge("E", "F", 9);
        return graph;
    }

    private static List<AbstractGraph<Integer>> nineVertexGraphs() {
        return Arrays.asList(connectNineVertices(new AdjacencyListGraph<Integer>(ISLAND_VERTICES)),
                connectNineVertices(new AdjacencyMatrixGraph<Integer>(ISLAND_VERTICES)));
    }

    private static AbstractGraph<Integer> connectNineVertices(AbstractGraph<Integer> graph) {
        graph.addEdge(0, 1, 8);
        graph.addEdge(0, 2, 12);
        graph.addEdge(1, 2, 13);
        graph.addEdge(1, 3, 25);
        graph.addEdge(1, 4, 9);
        graph.addEdge(2, 3, 14);
        graph.addEdge(3, 4, 20);
        graph.addEdge(3, 5, 8);
        graph.addEdge(4, 5, 19);
        graph.addEdge(2, 6, 21);
        graph.addEdge(3, 6, 12);
        graph.addEdge(3, 7, 12);
        graph.addEdge(5, 7, 11);
        graph.addEdge(3, 8, 16);
        graph.addEdge(6, 8, 11);
        graph.addEdge(7, 8, 9);
        return graph;
    }

    private static AbstractGraph<String> twoIslands() {
        AbstractGraph<String> graph = new AdjacencyListGraph<>(Arrays.asList("P", "Q", "R", "S", "T"));
        graph.addEdge("P", "Q", 1);
        graph.addEdge("Q", "R", 4);
        graph.addEdge("S", "T", 2);
        return graph;
    }
}
