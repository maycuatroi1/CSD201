package graph.sample;

import graph.AbstractGraph;
import graph.AdjacencyMatrixGraph;
import graph.Edge;
import graph.KruskalMST;
import graph.PrimMST;
import graph.WeightedGraphVisualizer;
import java.awt.GraphicsEnvironment;
import java.util.List;

public class MinimumSpanningTreeDemo {

    public static void main(String[] args) {
        AbstractGraph<String> sample = SampleWeightedGraph.build();
        print("Sample graph", sample);
        PrimMST<String> primFromA = new PrimMST<>(sample, "A");
        PrimMST<String> primFromD = new PrimMST<>(sample, "D");
        KruskalMST<String> kruskal = new KruskalMST<>(sample);
        print("Prim from A", primFromA.getEdges(), primFromA.getWeight());
        print("Prim from D", primFromD.getEdges(), primFromD.getWeight());
        print("Kruskal", kruskal.getEdges(), kruskal.getWeight());
        System.out.println("The three trees hold the same edges, found in a different order.");
        System.out.println();

        AbstractGraph<String> matrix = SampleWeightedGraph.connect(
                new AdjacencyMatrixGraph<String>(SampleWeightedGraph.vertices()));
        System.out.println("Adjacency matrix, Prim from A: weight " + new PrimMST<>(matrix, "A").getWeight()
                + ", Kruskal: weight " + new KruskalMST<>(matrix).getWeight());
        System.out.println();

        AbstractGraph<String> twoIslands = SampleWeightedGraph.twoIslands();
        print("Two islands", twoIslands);
        PrimMST<String> primFromP = new PrimMST<>(twoIslands, "P");
        KruskalMST<String> forest = new KruskalMST<>(twoIslands);
        print("Prim from P", primFromP.getEdges(), primFromP.getWeight());
        System.out.println("  Prim reaches " + primFromP.getVertices() + " only, the component of P.");
        print("Kruskal", forest.getEdges(), forest.getWeight());
        System.out.println("  Kruskal spans every component, so the result is a forest of 2 trees.");

        if (!GraphicsEnvironment.isHeadless()) {
            WeightedGraphVisualizer.show(sample, "Prim from A", WeightedGraphVisualizer.Algorithm.PRIM, "A");
            WeightedGraphVisualizer.show(sample, "Kruskal", WeightedGraphVisualizer.Algorithm.KRUSKAL, null);
        }
    }

    private static void print(String title, AbstractGraph<String> graph) {
        System.out.println(title + ": " + graph.getVertices().size() + " vertices, "
                + graph.getEdges().size() + " edges " + graph.getEdges());
    }

    private static void print(String title, List<Edge<String>> edges, int weight) {
        System.out.println(title + ": weight " + weight + ", " + edges.size() + " edges " + edges);
    }
}
