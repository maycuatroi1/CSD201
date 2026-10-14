package graph;

import java.util.List;

public interface GraphAlgorithmObserver<T> {

    default void started(T vertex) {
    }

    default void edgesSorted(List<Edge<T>> edges) {
    }

    default void edgeQueued(Edge<T> edge) {
    }

    default void edgeAdded(Edge<T> edge) {
    }

    default void edgeSkipped(Edge<T> edge) {
    }

    default void vertexSettled(T vertex, int distance) {
    }

    default void staleEntrySkipped(T vertex, int distance) {
    }

    default void edgeRelaxed(Edge<T> edge, int newDistance, boolean improved) {
    }
}
