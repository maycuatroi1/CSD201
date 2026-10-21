package hash;

public class LinearProbingHashTable<K, V> extends OpenAddressingHashTable<K, V> {

    public LinearProbingHashTable(int capacity) {
        this(capacity, 0.5);
    }

    public LinearProbingHashTable(int capacity, double maxLoadFactor) {
        super(capacity, maxLoadFactor);
    }

    protected int probe(int home, int i, K key) {
        return (home + i) % capacity();
    }
}
