package graph.sample;

import graph.AbstractGraph;
import graph.AdjacencyListGraph;
import graph.AdjacencyMatrixGraph;
import graph.Edge;
import graph.GraphVisualizer;
import graph.KruskalMST;
import graph.PrimMST;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MinimumSpanningTreeDemo {

    public static void main(String[] args) {
        AbstractGraph<String> sample = sampleGraph(new AdjacencyListGraph<String>(
                Arrays.asList("A", "B", "C", "D", "E", "F")));
        print("Sample graph", sample);
        PrimMST<String> primFromA = new PrimMST<>(sample, "A");
        PrimMST<String> primFromD = new PrimMST<>(sample, "D");
        KruskalMST<String> kruskal = new KruskalMST<>(sample);
        print("Prim from A", primFromA.getEdges(), primFromA.getWeight());
        print("Prim from D", primFromD.getEdges(), primFromD.getWeight());
        print("Kruskal", kruskal.getEdges(), kruskal.getWeight());
        System.out.println("The three trees hold the same edges, found in a different order.");
        System.out.println();

        AbstractGraph<String> matrix = sampleGraph(new AdjacencyMatrixGraph<String>(
                Arrays.asList("A", "B", "C", "D", "E", "F")));
        System.out.println("Adjacency matrix, Prim from A: weight " + new PrimMST<>(matrix, "A").getWeight()
                + ", Kruskal: weight " + new KruskalMST<>(matrix).getWeight());
        System.out.println();

        AbstractGraph<String> twoIslands = new AdjacencyListGraph<>(Arrays.asList("P", "Q", "R", "S", "T"));
        twoIslands.addEdge("P", "Q", 1);
        twoIslands.addEdge("Q", "R", 4);
        twoIslands.addEdge("S", "T", 2);
        print("Two islands", twoIslands);
        PrimMST<String> primFromP = new PrimMST<>(twoIslands, "P");
        KruskalMST<String> forest = new KruskalMST<>(twoIslands);
        print("Prim from P", primFromP.getEdges(), primFromP.getWeight());
        System.out.println("  Prim reaches " + primFromP.getVertices() + " only, the component of P.");
        print("Kruskal", forest.getEdges(), forest.getWeight());
        System.out.println("  Kruskal spans every component, so the result is a forest of 2 trees.");

        if (!GraphicsEnvironment.isHeadless()) {
            GraphVisualizer.show(sample, "Sample graph");
            GraphVisualizer.show(treeOf(sample.getVertices(), primFromA.getEdges()), "Prim from A");
        }
    }

    private static AbstractGraph<String> sampleGraph(AbstractGraph<String> graph) {
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

    private static AbstractGraph<String> treeOf(List<String> vertices, List<Edge<String>> edges) {
        AbstractGraph<String> tree = new AdjacencyListGraph<>(new ArrayList<>(vertices));
        for (Edge<String> edge : edges) {
            tree.addEdge(edge.getSrc(), edge.getDest(), edge.getWeight());
        }
        return tree;
    }

    private static void print(String title, AbstractGraph<String> graph) {
        System.out.println(title + ": " + graph.getVertices().size() + " vertices, "
                + graph.getEdges().size() + " edges " + graph.getEdges());
    }

    private static void print(String title, List<Edge<String>> edges, int weight) {
        System.out.println(title + ": weight " + weight + ", " + edges.size() + " edges " + edges);
    }
}
