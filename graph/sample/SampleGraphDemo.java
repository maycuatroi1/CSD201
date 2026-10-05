package graph.sample;

import graph.AbstractGraph;
import graph.AdjacencyListGraph;
import graph.AdjacencyMatrixGraph;
import graph.Edge;
import graph.GraphVisualizer;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SampleGraphDemo {

    private static final List<String> SAMPLE_VERTICES = Arrays.asList("a", "b", "c", "d", "e", "f", "g");
    private static final List<String> SAMPLE_EDGES =
            Arrays.asList("a-c", "a-d", "a-f", "b-d", "b-e", "c-f", "d-e", "d-f");

    public static void main(String[] args) throws IOException {
        List<String> edgeLines = args.length > 0 ? Files.readAllLines(Paths.get(args[0])) : SAMPLE_EDGES;
        List<String> vertexLines = args.length > 1 ? Files.readAllLines(Paths.get(args[1])) : SAMPLE_VERTICES;
        List<String> vertices = vertices(vertexLines, edgeLines);
        if (vertices.isEmpty()) {
            System.out.println("The graph has no vertices.");
            return;
        }
        AbstractGraph<String> list = connect(new AdjacencyListGraph<>(vertices), edgeLines);
        AbstractGraph<String> matrix = connect(new AdjacencyMatrixGraph<>(vertices), edgeLines);
        String start = vertices.get(0);
        print("Adjacency list", list, start);
        System.out.println();
        print("Adjacency matrix", matrix, start);
        if (!GraphicsEnvironment.isHeadless()) {
            GraphVisualizer.show(list, "DFS from " + start, list.depthFirstTraversal(start),
                    GraphVisualizer.Traversal.DFS);
            GraphVisualizer.show(list, "BFS from " + start, list.breadthFirstTraversal(start),
                    GraphVisualizer.Traversal.BFS);
        }
    }

    private static List<String> vertices(List<String> vertexLines, List<String> edgeLines) {
        Set<String> vertices = new LinkedHashSet<>();
        for (String line : vertexLines) {
            if (!line.trim().isEmpty()) {
                vertices.add(line.trim());
            }
        }
        for (String line : edgeLines) {
            if (!line.trim().isEmpty()) {
                vertices.addAll(Arrays.asList(ends(line)));
            }
        }
        return new ArrayList<>(vertices);
    }

    private static AbstractGraph<String> connect(AbstractGraph<String> graph, List<String> edgeLines) {
        for (String line : edgeLines) {
            if (!line.trim().isEmpty()) {
                String[] ends = ends(line);
                graph.addEdge(ends[0], ends[1], weight(line));
            }
        }
        return graph;
    }

    private static String[] ends(String edgeLine) {
        String[] ends = edgeLine.trim().split("\\s+")[0].split("-");
        if (ends.length != 2 || ends[0].isEmpty() || ends[1].isEmpty()) {
            throw new IllegalArgumentException("Expected an edge like a-c or a-c 4, got: " + edgeLine);
        }
        return ends;
    }

    private static int weight(String edgeLine) {
        String[] parts = edgeLine.trim().split("\\s+");
        return parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
    }

    private static void print(String title, AbstractGraph<String> graph, String start) {
        List<Edge<String>> edges = graph.getEdges();
        System.out.println(title + ": " + graph.getVertices().size() + " vertices, " + edges.size() + " edges");
        for (Edge<String> edge : edges) {
            System.out.println("  " + edge.getSrc() + " - " + edge.getDest() + "  weight " + edge.getWeight());
        }
        for (String vertex : graph.getVertices()) {
            System.out.println("  neighbors of " + vertex + ": " + graph.getNeighbors(vertex));
        }
        System.out.println("DFS from " + start + ": " + graph.depthFirstTraversal(start));
        System.out.println("BFS from " + start + ": " + graph.breadthFirstTraversal(start));
    }
}
