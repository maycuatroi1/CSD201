package graph;

final class QueueEntry<E> implements Comparable<QueueEntry<E>> {

    private final E item;
    private final int key;
    private final int sequence;

    QueueEntry(E item, int key, int sequence) {
        this.item = item;
        this.key = key;
        this.sequence = sequence;
    }

    E getItem() {
        return item;
    }

    int getKey() {
        return key;
    }

    public int compareTo(QueueEntry<E> other) {
        if (key != other.key) {
            return Integer.compare(key, other.key);
        }
        return Integer.compare(sequence, other.sequence);
    }
}
