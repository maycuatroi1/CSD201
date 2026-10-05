package graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public abstract class AbstractGraph<T> {

    public abstract void addVertex(T vertex);

    public abstract void addEdge(T src, T dest, int weight);

    public abstract boolean hasVertex(T vertex);

    public abstract List<T> getVertices();

    public abstract List<Edge<T>> getEdges();

    public abstract List<T> getNeighbors(T vertex);

    public List<T> depthFirstTraversal(T startVertex) {
        requireVertex(startVertex);
        List<T> order = new ArrayList<>();
        depthFirst(startVertex, new HashSet<T>(), order);
        return order;
    }

    public List<T> breadthFirstTraversal(T startVertex) {
        requireVertex(startVertex);
        List<T> order = new ArrayList<>();
        Set<T> discovered = new HashSet<>();
        Queue<T> queue = new ArrayDeque<>();
        discovered.add(startVertex);
        queue.add(startVertex);
        while (!queue.isEmpty()) {
            T vertex = queue.remove();
            order.add(vertex);
            for (T neighbor : getNeighbors(vertex)) {
                if (!discovered.contains(neighbor)) {
                    discovered.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return order;
    }

    protected final void requireVertex(T vertex) {
        if (!hasVertex(vertex)) {
            throw new IllegalArgumentException("Unknown vertex: " + vertex);
        }
    }

    private void depthFirst(T vertex, Set<T> visited, List<T> order) {
        visited.add(vertex);
        order.add(vertex);
        for (T neighbor : getNeighbors(vertex)) {
            if (!visited.contains(neighbor)) {
                depthFirst(neighbor, visited, order);
            }
        }
    }
}
