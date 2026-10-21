package hash;

public final class Entry<K, V> {

    private final K key;
    private V value;

    Entry(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }

    public String toString() {
        return key + "=" + value;
    }
}
