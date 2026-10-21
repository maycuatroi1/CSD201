package hash;

public class DoubleHashingHashTable<K, V> extends OpenAddressingHashTable<K, V> {

    private final int q;

    public DoubleHashingHashTable(int capacity, int q) {
        this(capacity, q, 0.5);
    }

    public DoubleHashingHashTable(int capacity, int q, double maxLoadFactor) {
        super(capacity, maxLoadFactor);
        if (q < 2 || q >= capacity) {
            throw new IllegalArgumentException("2 <= q < capacity");
        }
        this.q = q;
    }

    public int step(K key) {
        return q - Math.floorMod(key.hashCode(), q);
    }

    protected int probe(int home, int i, K key) {
        long offset = (long) i * step(key);
        return (int) ((home + offset) % capacity());
    }
}
