package graph;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class PrimMST<T> {

    private final List<Edge<T>> treeEdges = new ArrayList<>();
    private final Set<T> inTree = new LinkedHashSet<>();
    private final PriorityQueue<QueueEntry<Edge<T>>> fringe = new PriorityQueue<>();
    private int weight;
    private int queued;

    public PrimMST(AbstractGraph<T> graph, T start) {
        if (!graph.hasVertex(start)) {
            throw new IllegalArgumentException("Unknown vertex: " + start);
        }
        addToTree(graph, start);
        while (!fringe.isEmpty()) {
            Edge<T> lightest = fringe.remove().getItem();
            if (!inTree.contains(lightest.getDest())) {
                treeEdges.add(lightest);
                weight += lightest.getWeight();
                addToTree(graph, lightest.getDest());
            }
        }
    }

    public List<Edge<T>> getEdges() {
        return new ArrayList<>(treeEdges);
    }

    public int getWeight() {
        return weight;
    }

    public List<T> getVertices() {
        return new ArrayList<>(inTree);
    }

    private void addToTree(AbstractGraph<T> graph, T vertex) {
        inTree.add(vertex);
        for (Edge<T> edge : graph.getIncidentEdges(vertex)) {
            if (!inTree.contains(edge.getDest())) {
                fringe.add(new QueueEntry<>(edge, edge.getWeight(), queued));
                queued++;
            }
        }
    }
}
