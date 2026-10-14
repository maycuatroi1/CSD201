package graph;

import java.util.Arrays;
import java.util.List;

final class SampleWeightedGraphs {

    static final List<String> VERTICES = Arrays.asList("A", "B", "C", "D", "E", "F");
    static final List<Integer> NINE_VERTICES = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8);

    private SampleWeightedGraphs() {
    }

    static AbstractGraph<String> list() {
        return connect(new AdjacencyListGraph<String>(VERTICES));
    }

    static AbstractGraph<String> matrix() {
        return connect(new AdjacencyMatrixGraph<String>(VERTICES));
    }

    static List<AbstractGraph<String>> bothRepresentations() {
        return Arrays.asList(list(), matrix());
    }

    static List<AbstractGraph<Integer>> bothNineVertexRepresentations() {
        return Arrays.asList(connectNine(new AdjacencyListGraph<Integer>(NINE_VERTICES)),
                connectNine(new AdjacencyMatrixGraph<Integer>(NINE_VERTICES)));
    }

    static AbstractGraph<String> twoIslands() {
        AbstractGraph<String> graph = new AdjacencyListGraph<>(Arrays.asList("P", "Q", "R", "S", "T"));
        graph.addEdge("P", "Q", 1);
        graph.addEdge("Q", "R", 4);
        graph.addEdge("S", "T", 2);
        return graph;
    }

    private static AbstractGraph<String> connect(AbstractGraph<String> graph) {
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

    private static AbstractGraph<Integer> connectNine(AbstractGraph<Integer> graph) {
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
}
