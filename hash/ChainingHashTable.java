package hash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ChainingHashTable<K, V> {

    private final double maxLoadFactor;
    private List<LinkedList<Entry<K, V>>> buckets;
    private int size;

    public ChainingHashTable(int capacity) {
        this(capacity, 0.75);
    }

    public ChainingHashTable(int capacity, double maxLoadFactor) {
        if (capacity < 1 || maxLoadFactor <= 0) {
            throw new IllegalArgumentException("capacity >= 1 and maxLoadFactor > 0");
        }
        this.maxLoadFactor = maxLoadFactor;
        this.buckets = emptyBuckets(capacity);
    }

    public int hash(K key) {
        return Math.floorMod(key.hashCode(), buckets.size());
    }

    public int capacity() {
        return buckets.size();
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public double loadFactor() {
        return (double) size / buckets.size();
    }

    public V get(K key) {
        for (Entry<K, V> entry : buckets.get(hash(key))) {
            if (entry.getKey().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public boolean containsKey(K key) {
        for (Entry<K, V> entry : buckets.get(hash(key))) {
            if (entry.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public V put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("null key");
        }
        LinkedList<Entry<K, V>> chain = buckets.get(hash(key));
        for (Entry<K, V> entry : chain) {
            if (entry.getKey().equals(key)) {
                return entry.setValue(value);
            }
        }
        chain.addLast(new Entry<>(key, value));
        size++;
        if (size > maxLoadFactor * buckets.size()) {
            rehash();
        }
        return null;
    }

    public V remove(K key) {
        LinkedList<Entry<K, V>> chain = buckets.get(hash(key));
        for (Entry<K, V> entry : chain) {
            if (entry.getKey().equals(key)) {
                chain.remove(entry);
                size--;
                return entry.getValue();
            }
        }
        return null;
    }

    public List<K> chain(int index) {
        List<K> keys = new ArrayList<>();
        for (Entry<K, V> entry : buckets.get(index)) {
            keys.add(entry.getKey());
        }
        return keys;
    }

    public String toString() {
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < buckets.size(); index++) {
            text.append(index).append(':');
            for (Entry<K, V> entry : buckets.get(index)) {
                text.append(' ').append(entry.getKey());
            }
            text.append('\n');
        }
        return text.toString();
    }

    private void rehash() {
        List<LinkedList<Entry<K, V>>> old = buckets;
        buckets = emptyBuckets(OpenAddressingHashTable.nextPrime(2 * old.size()));
        for (LinkedList<Entry<K, V>> chain : old) {
            for (Entry<K, V> entry : chain) {
                buckets.get(hash(entry.getKey())).addLast(entry);
            }
        }
    }

    private List<LinkedList<Entry<K, V>>> emptyBuckets(int capacity) {
        List<LinkedList<Entry<K, V>>> result = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            result.add(new LinkedList<>());
        }
        return Collections.unmodifiableList(result);
    }
}
