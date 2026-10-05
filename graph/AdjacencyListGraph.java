package graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdjacencyListGraph<T> extends AbstractGraph<T> {

    private final Map<T, List<Edge<T>>> adjacency = new LinkedHashMap<>();

    public AdjacencyListGraph(List<T> vertices) {
        for (T vertex : vertices) {
            adjacency.putIfAbsent(vertex, new ArrayList<Edge<T>>());
        }
    }

    public void addVertex(T vertex) {
        adjacency.putIfAbsent(vertex, new ArrayList<Edge<T>>());
    }

    public void addEdge(T src, T dest, int weight) {
        requireVertex(src);
        requireVertex(dest);
        putEdge(src, dest, weight);
        putEdge(dest, src, weight);
    }

    public boolean hasVertex(T vertex) {
        return adjacency.containsKey(vertex);
    }

    public List<T> getVertices() {
        return new ArrayList<>(adjacency.keySet());
    }

    public List<Edge<T>> getEdges() {
        List<Edge<T>> edges = new ArrayList<>();
        Set<T> finished = new HashSet<>();
        for (T vertex : adjacency.keySet()) {
            for (Edge<T> edge : adjacency.get(vertex)) {
                if (!finished.contains(edge.getDest())) {
                    edges.add(edge);
                }
            }
            finished.add(vertex);
        }
        return edges;
    }

    public List<T> getNeighbors(T vertex) {
        requireVertex(vertex);
        List<T> neighbors = new ArrayList<>();
        for (Edge<T> edge : adjacency.get(vertex)) {
            neighbors.add(edge.getDest());
        }
        return neighbors;
    }

    private void putEdge(T src, T dest, int weight) {
        List<Edge<T>> edges = adjacency.get(src);
        Edge<T> edge = new Edge<>(src, dest, weight);
        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i).getDest().equals(dest)) {
                edges.set(i, edge);
                return;
            }
        }
        edges.add(edge);
    }
}
