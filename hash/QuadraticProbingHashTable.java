package hash;

public class QuadraticProbingHashTable<K, V> extends OpenAddressingHashTable<K, V> {

    public QuadraticProbingHashTable(int capacity) {
        this(capacity, 0.5);
    }

    public QuadraticProbingHashTable(int capacity, double maxLoadFactor) {
        super(capacity, maxLoadFactor);
    }

    protected int probe(int home, int i, K key) {
        long offset = (long) i * i;
        return (int) ((home + offset) % capacity());
    }
}
