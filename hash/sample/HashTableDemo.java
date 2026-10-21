package hash.sample;

import hash.ChainingHashTable;
import hash.DoubleHashingHashTable;
import hash.LinearProbingHashTable;
import hash.OpenAddressingHashTable;
import hash.QuadraticProbingHashTable;

public class HashTableDemo {

    private static final int[] KEYS = {89, 18, 49, 58, 69};

    public static void main(String[] args) {
        insertAll("Linear probing, h(x) = x mod 10", new LinearProbingHashTable<>(10));
        insertAll("Quadratic probing, h(x) = x mod 10", new QuadraticProbingHashTable<>(10));
        insertAll("Double hashing, h'(x) = 7 - x mod 7", new DoubleHashingHashTable<>(10, 7));
        searchWithLinearProbing();
        deleteWithLinearProbing();
        rehashWhenTooFull();
        separateChaining();
        quadraticProbesModEleven();
    }

    private static void insertAll(String title, OpenAddressingHashTable<Integer, String> table) {
        System.out.println("## " + title);
        for (int key : KEYS) {
            table.put(key, "v" + key);
            System.out.println("put " + key + "  home " + table.hash(key) + "  probes " + table.lastProbes() + "  " + table);
        }
        System.out.println();
    }

    private static void searchWithLinearProbing() {
        System.out.println("## Search with linear probing");
        LinearProbingHashTable<Integer, String> table = new LinearProbingHashTable<>(10);
        for (int key : KEYS) {
            table.put(key, "v" + key);
        }
        for (int key : new int[] {69, 79}) {
            String value = table.get(key);
            System.out.println("get " + key + "  probes " + table.lastProbes() + "  " + (value == null ? "not found" : "found " + value));
        }
        System.out.println();
    }

    private static void deleteWithLinearProbing() {
        System.out.println("## Deletion with linear probing");
        LinearProbingHashTable<Integer, String> table = new LinearProbingHashTable<>(10, 0.9);
        int[] keys = {1, 4, 2, 14, 11};
        String[] names = {"A1", "A4", "A2", "B4", "B1"};
        for (int i = 0; i < keys.length; i++) {
            table.put(keys[i], names[i]);
        }
        System.out.println("after put A1 A4 A2 B4 B1  " + table);
        System.out.println("remove 4 -> " + table.remove(4) + "  " + table);
        String b4 = table.get(14);
        System.out.println("get 14  probes " + table.lastProbes() + "  found " + b4);
        System.out.println("remove 2 -> " + table.remove(2) + "  " + table);
        String b1 = table.get(11);
        System.out.println("get 11  probes " + table.lastProbes() + "  found " + b1);
        table.put(24, "C4");
        System.out.println("put 24  probes " + table.lastProbes() + "  " + table);
        System.out.println();
    }

    private static void rehashWhenTooFull() {
        System.out.println("## Rehashing, linear probing, max load factor 0.5");
        LinearProbingHashTable<Integer, String> table = new LinearProbingHashTable<>(7);
        for (int key : new int[] {15, 22, 8, 1}) {
            int before = table.capacity();
            table.put(key, "v" + key);
            System.out.println("put " + key + "  probes " + table.lastProbes() + "  capacity " + before + " -> " + table.capacity() + "  " + table);
        }
        System.out.println();
    }

    private static void separateChaining() {
        System.out.println("## Separate chaining, h(x) = x mod 10");
        ChainingHashTable<Integer, String> table = new ChainingHashTable<>(10, 1.0);
        int[] keys = {5, 2, 3, 15, 9, 12, 19, 22};
        String[] names = {"A5", "A2", "A3", "B5", "A9", "B2", "B9", "C2"};
        for (int i = 0; i < keys.length; i++) {
            table.put(keys[i], names[i]);
        }
        System.out.print(table);
        System.out.println("load factor " + table.loadFactor());
        System.out.println();
    }

    private static void quadraticProbesModEleven() {
        System.out.println("## Quadratic probing from home 3, M = 11");
        StringBuilder probes = new StringBuilder();
        for (int i = 0; i < 11; i++) {
            probes.append(i == 0 ? "" : " ").append((3 + i * i) % 11);
        }
        System.out.println(probes);
    }
}
