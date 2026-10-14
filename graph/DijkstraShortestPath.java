package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class DijkstraShortestPath<T> {

    private final T source;
    private final Map<T, Integer> distance = new HashMap<>();
    private final Map<T, Edge<T>> previous = new HashMap<>();

    public DijkstraShortestPath(AbstractGraph<T> graph, T source) {
        this(graph, source, new GraphAlgorithmObserver<T>() { });
    }

    public DijkstraShortestPath(AbstractGraph<T> graph, T source, GraphAlgorithmObserver<T> observer) {
        if (!graph.hasVertex(source)) {
            throw new IllegalArgumentException("Unknown vertex: " + source);
        }
        for (Edge<T> edge : graph.getEdges()) {
            if (edge.getWeight() < 0) {
                throw new IllegalArgumentException("Dijkstra needs weights >= 0, got " + edge);
            }
        }
        this.source = source;
        Set<T> settled = new HashSet<>();
        PriorityQueue<QueueEntry<T>> queue = new PriorityQueue<>();
        int queued = 0;
        distance.put(source, 0);
        queue.add(new QueueEntry<>(source, 0, queued));
        queued++;
        observer.started(source);
        while (!queue.isEmpty()) {
            QueueEntry<T> entry = queue.remove();
            T nearest = entry.getItem();
            if (settled.contains(nearest)) {
                observer.staleEntrySkipped(nearest, entry.getKey());
                continue;
            }
            settled.add(nearest);
            observer.vertexSettled(nearest, entry.getKey());
            for (Edge<T> edge : graph.getIncidentEdges(nearest)) {
                T neighbor = edge.getDest();
                if (settled.contains(neighbor)) {
                    continue;
                }
                int throughNearest = distance.get(nearest) + edge.getWeight();
                boolean improved = !distance.containsKey(neighbor) || throughNearest < distance.get(neighbor);
                if (improved) {
                    distance.put(neighbor, throughNearest);
                    previous.put(neighbor, edge);
                    queue.add(new QueueEntry<>(neighbor, throughNearest, queued));
                    queued++;
                }
                observer.edgeRelaxed(edge, throughNearest, improved);
            }
        }
    }

    public T getSource() {
        return source;
    }

    public boolean hasPathTo(T vertex) {
        return distance.containsKey(vertex);
    }

    public int distanceTo(T vertex) {
        return hasPathTo(vertex) ? distance.get(vertex) : Integer.MAX_VALUE;
    }

    public List<T> pathTo(T vertex) {
        List<T> path = new ArrayList<>();
        if (!hasPathTo(vertex)) {
            return path;
        }
        for (T at = vertex; !at.equals(source); at = previous.get(at).getSrc()) {
            path.add(at);
        }
        path.add(source);
        Collections.reverse(path);
        return path;
    }
}
