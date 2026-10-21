package hash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class OpenAddressingHashTable<K, V> {

    private final Entry<K, V> deleted = new Entry<>(null, null);
    private final double maxLoadFactor;
    private final List<Integer> lastProbes = new ArrayList<>();
    private List<Entry<K, V>> table;
    private int size;
    private int deletedCount;

    protected OpenAddressingHashTable(int capacity, double maxLoadFactor) {
        if (capacity < 1 || maxLoadFactor <= 0 || maxLoadFactor >= 1) {
            throw new IllegalArgumentException("capacity >= 1 and 0 < maxLoadFactor < 1");
        }
        this.maxLoadFactor = maxLoadFactor;
        this.table = emptyTable(capacity);
    }

    protected abstract int probe(int home, int i, K key);

    public int hash(K key) {
        return Math.floorMod(key.hashCode(), table.size());
    }

    public int capacity() {
        return table.size();
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public double loadFactor() {
        return (double) size / table.size();
    }

    public List<Integer> lastProbes() {
        return Collections.unmodifiableList(lastProbes);
    }

    public V get(K key) {
        int slot = find(key);
        return slot < 0 ? null : table.get(slot).getValue();
    }

    public boolean containsKey(K key) {
        return find(key) >= 0;
    }

    public V put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("null key");
        }
        int home = hash(key);
        int available = -1;
        for (int i = 0; i < table.size(); i++) {
            int slot = nextSlot(home, i, key);
            Entry<K, V> entry = table.get(slot);
            if (entry == null || entry == deleted) {
                if (available < 0) {
                    available = slot;
                }
                if (entry == null) {
                    break;
                }
            } else if (entry.getKey().equals(key)) {
                return entry.setValue(value);
            }
        }
        if (available < 0) {
            rehash();
            return put(key, value);
        }
        if (table.get(available) == deleted) {
            deletedCount--;
        }
        table.set(available, new Entry<>(key, value));
        size++;
        if (size + deletedCount > maxLoadFactor * table.size()) {
            rehash();
        }
        return null;
    }

    public V remove(K key) {
        int slot = find(key);
        if (slot < 0) {
            return null;
        }
        V old = table.get(slot).getValue();
        table.set(slot, deleted);
        size--;
        deletedCount++;
        return old;
    }

    public List<K> slots() {
        List<K> keys = new ArrayList<>();
        for (Entry<K, V> entry : table) {
            keys.add(entry == null ? null : entry.getKey());
        }
        return keys;
    }

    public boolean isDeleted(int slot) {
        return table.get(slot) == deleted;
    }

    public String toString() {
        StringBuilder text = new StringBuilder("[");
        for (int slot = 0; slot < table.size(); slot++) {
            Entry<K, V> entry = table.get(slot);
            text.append(slot == 0 ? "" : ", ");
            text.append(entry == null ? "-" : entry == deleted ? "DEL" : String.valueOf(entry.getKey()));
        }
        return text.append("]").toString();
    }

    private int find(K key) {
        int home = hash(key);
        for (int i = 0; i < table.size(); i++) {
            int slot = nextSlot(home, i, key);
            Entry<K, V> entry = table.get(slot);
            if (entry == null) {
                return -1;
            }
            if (entry != deleted
                    && entry.getKey().equals(key)) {
                return slot;
            }
        }
        return -1;
    }

    private int nextSlot(int home, int i, K key) {
        if (i == 0) {
            lastProbes.clear();
        }
        int slot = probe(home, i, key);
        lastProbes.add(slot);
        return slot;
    }

    private void rehash() {
        List<Entry<K, V>> old = table;
        table = emptyTable(nextPrime(2 * old.size()));
        size = 0;
        deletedCount = 0;
        for (Entry<K, V> entry : old) {
            if (entry != null && entry != deleted) {
                insertAfterRehash(entry);
            }
        }
    }

    private void insertAfterRehash(Entry<K, V> entry) {
        int home = hash(entry.getKey());
        for (int i = 0; i < table.size(); i++) {
            int slot = probe(home, i, entry.getKey());
            if (table.get(slot) == null) {
                table.set(slot, entry);
                size++;
                return;
            }
        }
        throw new IllegalStateException("no free slot after rehash");
    }

    private List<Entry<K, V>> emptyTable(int capacity) {
        return new ArrayList<>(Collections.<Entry<K, V>>nCopies(capacity, null));
    }

    static int nextPrime(int n) {
        int candidate = Math.max(n, 2);
        while (!isPrime(candidate)) {
            candidate++;
        }
        return candidate;
    }

    static boolean isPrime(int n) {
        if (n < 2) {
            return false;
        }
        for (int d = 2; (long) d * d <= n; d++) {
            if (n % d == 0) {
                return false;
            }
        }
        return true;
    }
}
