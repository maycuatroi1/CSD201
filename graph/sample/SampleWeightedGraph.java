package graph.sample;

import graph.AbstractGraph;
import graph.AdjacencyListGraph;
import java.util.Arrays;
import java.util.List;

public final class SampleWeightedGraph {

    private SampleWeightedGraph() {
    }

    public static List<String> vertices() {
        return Arrays.asList("A", "B", "C", "D", "E", "F");
    }

    public static AbstractGraph<String> build() {
        return connect(new AdjacencyListGraph<String>(vertices()));
    }

    public static AbstractGraph<String> connect(AbstractGraph<String> graph) {
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

    public static AbstractGraph<String> twoIslands() {
        AbstractGraph<String> graph = new AdjacencyListGraph<>(Arrays.asList("P", "Q", "R", "S", "T"));
        graph.addEdge("P", "Q", 1);
        graph.addEdge("Q", "R", 4);
        graph.addEdge("S", "T", 2);
        return graph;
    }
}
