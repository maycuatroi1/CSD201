package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdjacencyMatrixGraph<T> extends AbstractGraph<T> {

    private final List<T> vertices = new ArrayList<>();
    private final Map<T, Integer> indexes = new HashMap<>();
    private int[][] weights;

    public AdjacencyMatrixGraph(List<T> vertices) {
        for (T vertex : vertices) {
            if (!indexes.containsKey(vertex)) {
                register(vertex);
            }
        }
        weights = new int[this.vertices.size()][this.vertices.size()];
    }

    public void addVertex(T vertex) {
        if (hasVertex(vertex)) {
            return;
        }
        register(vertex);
        int size = vertices.size();
        int[][] grown = new int[size][size];
        for (int i = 0; i < weights.length; i++) {
            System.arraycopy(weights[i], 0, grown[i], 0, weights.length);
        }
        weights = grown;
    }

    public void addEdge(T src, T dest, int weight) {
        int srcIndex = indexOf(src);
        int destIndex = indexOf(dest);
        weights[srcIndex][destIndex] = weight;
        weights[destIndex][srcIndex] = weight;
    }

    public boolean hasVertex(T vertex) {
        return indexes.containsKey(vertex);
    }

    public List<T> getVertices() {
        return new ArrayList<>(vertices);
    }

    public List<Edge<T>> getEdges() {
        List<Edge<T>> edges = new ArrayList<>();
        for (int i = 0; i < weights.length; i++) {
            for (int j = i; j < weights.length; j++) {
                if (weights[i][j] != 0) {
                    edges.add(new Edge<>(vertices.get(i), vertices.get(j), weights[i][j]));
                }
            }
        }
        return edges;
    }

    public List<T> getNeighbors(T vertex) {
        int row = indexOf(vertex);
        List<T> neighbors = new ArrayList<>();
        for (int column = 0; column < weights.length; column++) {
            if (weights[row][column] != 0) {
                neighbors.add(vertices.get(column));
            }
        }
        return neighbors;
    }

    private void register(T vertex) {
        indexes.put(vertex, vertices.size());
        vertices.add(vertex);
    }

    private int indexOf(T vertex) {
        requireVertex(vertex);
        return indexes.get(vertex);
    }
}
