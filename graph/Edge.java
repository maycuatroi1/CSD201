package graph;

public final class Edge<T> {
    private final T src;
    private final T dest;
    private final int weight;

    public Edge(T src, T dest, int weight) {
        this.src = src;
        this.dest = dest;
        this.weight = weight;
    }

    public T getSrc() {
        return src;
    }

    public T getDest() {
        return dest;
    }

    public int getWeight() {
        return weight;
    }

    public String toString() {
        return src + "-" + dest + "(" + weight + ")";
    }
}
