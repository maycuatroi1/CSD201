package graph.sample;

import graph.AbstractGraph;
import graph.AdjacencyMatrixGraph;
import graph.DijkstraShortestPath;
import graph.WeightedGraphVisualizer;
import java.awt.GraphicsEnvironment;
import java.util.List;

public class ShortestPathDemo {

    public static void main(String[] args) {
        AbstractGraph<String> sample = SampleWeightedGraph.build();
        System.out.println("Sample graph: " + sample.getVertices().size() + " vertices, "
                + sample.getEdges().size() + " edges " + sample.getEdges());
        print("Dijkstra from A", new DijkstraShortestPath<>(sample, "A"), sample.getVertices());
        System.out.println("Sample PE Question 3 f2 asks for the path from A to E: distance 20 along "
                + new DijkstraShortestPath<>(sample, "A").pathTo("E") + ".");
        System.out.println("A-C-F-E costs 9 + 2 + 9 = 20, while the two-edge path A-F-E costs 14 + 9 = 23.");
        System.out.println();

        AbstractGraph<String> matrix = SampleWeightedGraph.connect(
                new AdjacencyMatrixGraph<String>(SampleWeightedGraph.vertices()));
        System.out.println("Adjacency matrix, distance from A to E: "
                + new DijkstraShortestPath<>(matrix, "A").distanceTo("E"));
        System.out.println();

        AbstractGraph<String> twoIslands = SampleWeightedGraph.twoIslands();
        System.out.println("Two islands: " + twoIslands.getEdges());
        print("Dijkstra from P", new DijkstraShortestPath<>(twoIslands, "P"), twoIslands.getVertices());

        if (!GraphicsEnvironment.isHeadless()) {
            WeightedGraphVisualizer.show(sample, "Dijkstra from A", WeightedGraphVisualizer.Algorithm.DIJKSTRA,
                    "A", "E");
        }
    }

    private static void print(String title, DijkstraShortestPath<String> paths, List<String> vertices) {
        System.out.println(title + ":");
        for (String vertex : vertices) {
            System.out.println(paths.hasPathTo(vertex)
                    ? String.format("  to %-4s distance %4d  path %s", vertex, paths.distanceTo(vertex),
                            paths.pathTo(vertex))
                    : String.format("  to %-4s no path", vertex));
        }
    }
}
