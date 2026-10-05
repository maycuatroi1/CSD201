package graph.sample;

import graph.AbstractGraph;
import graph.AdjacencyListGraph;
import graph.AdjacencyMatrixGraph;
import graph.Edge;
import graph.GraphVisualizer;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.List;

public class SampleGraphDemo {

    private static final List<Character> VERTICES = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g');
    private static final String[] EDGES = {"a-c", "a-d", "a-f", "b-d", "b-e", "c-f", "d-e", "d-f"};

    public static void main(String[] args) {
        AbstractGraph<Character> list = connect(new AdjacencyListGraph<>(VERTICES));
        AbstractGraph<Character> matrix = connect(new AdjacencyMatrixGraph<>(VERTICES));
        print("Adjacency list", list);
        System.out.println();
        print("Adjacency matrix", matrix);
        if (!GraphicsEnvironment.isHeadless()) {
            GraphVisualizer.show(list, "DFS from a", list.depthFirstTraversal('a'), GraphVisualizer.Traversal.DFS);
            GraphVisualizer.show(list, "BFS from a", list.breadthFirstTraversal('a'), GraphVisualizer.Traversal.BFS);
        }
    }

    private static AbstractGraph<Character> connect(AbstractGraph<Character> graph) {
        for (String edge : EDGES) {
            graph.addEdge(edge.charAt(0), edge.charAt(2), 1);
        }
        return graph;
    }

    private static void print(String title, AbstractGraph<Character> graph) {
        List<Edge<Character>> edges = graph.getEdges();
        System.out.println(title + ": " + graph.getVertices().size() + " vertices, " + edges.size() + " edges");
        for (Edge<Character> edge : edges) {
            System.out.println("  " + edge.getSrc() + " - " + edge.getDest());
        }
        for (Character vertex : graph.getVertices()) {
            System.out.println("  neighbors of " + vertex + ": " + graph.getNeighbors(vertex));
        }
        System.out.println("DFS from a: " + graph.depthFirstTraversal('a'));
        System.out.println("BFS from a: " + graph.breadthFirstTraversal('a'));
        System.out.println("DFS from g: " + graph.depthFirstTraversal('g'));
    }
}
