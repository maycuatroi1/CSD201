package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KruskalMST<T> {

    private final List<Edge<T>> treeEdges = new ArrayList<>();
    private final Map<T, T> parent = new HashMap<>();
    private final Map<T, Integer> rank = new HashMap<>();
    private int weight;

    public KruskalMST(AbstractGraph<T> graph) {
        this(graph, new GraphAlgorithmObserver<T>() { });
    }

    public KruskalMST(AbstractGraph<T> graph, GraphAlgorithmObserver<T> observer) {
        for (T vertex : graph.getVertices()) {
            parent.put(vertex, vertex);
            rank.put(vertex, 0);
        }
        List<Edge<T>> lightestFirst = new ArrayList<>(graph.getEdges());
        lightestFirst.sort((first, second) -> Integer.compare(first.getWeight(), second.getWeight()));
        observer.edgesSorted(new ArrayList<>(lightestFirst));
        for (Edge<T> edge : lightestFirst) {
            if (union(edge.getSrc(), edge.getDest())) {
                treeEdges.add(edge);
                weight += edge.getWeight();
                observer.edgeAdded(edge);
            } else {
                observer.edgeSkipped(edge);
            }
        }
    }

    public List<Edge<T>> getEdges() {
        return new ArrayList<>(treeEdges);
    }

    public int getWeight() {
        return weight;
    }

    private T find(T vertex) {
        T above = parent.get(vertex);
        if (above.equals(vertex)) {
            return vertex;
        }
        T root = find(above);
        parent.put(vertex, root);
        return root;
    }

    private boolean union(T first, T second) {
        T firstRoot = find(first);
        T secondRoot = find(second);
        if (firstRoot.equals(secondRoot)) {
            return false;
        }
        int firstRank = rank.get(firstRoot);
        int secondRank = rank.get(secondRoot);
        if (firstRank < secondRank) {
            parent.put(firstRoot, secondRoot);
        } else if (firstRank > secondRank) {
            parent.put(secondRoot, firstRoot);
        } else {
            parent.put(secondRoot, firstRoot);
            rank.put(firstRoot, firstRank + 1);
        }
        return true;
    }
}
