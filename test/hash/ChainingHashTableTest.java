package hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class ChainingHashTableTest {

    private static ChainingHashTable<Integer, String> slideTable() {
        ChainingHashTable<Integer, String> table = new ChainingHashTable<>(10, 1.0);
        int[] keys = {5, 2, 3, 15, 9, 12, 19, 22};
        String[] names = {"A5", "A2", "A3", "B5", "A9", "B2", "B9", "C2"};
        for (int i = 0; i < keys.length; i++) {
            table.put(keys[i], names[i]);
        }
        return table;
    }

    @Test
    public void collidingKeysShareOneChainInInsertionOrder() {
        ChainingHashTable<Integer, String> table = slideTable();
        assertEquals(Arrays.asList(2, 12, 22), table.chain(2));
        assertEquals(Arrays.asList(3), table.chain(3));
        assertEquals(Arrays.asList(5, 15), table.chain(5));
        assertEquals(Arrays.asList(9, 19), table.chain(9));
        assertEquals(Collections.<Integer>emptyList(), table.chain(0));
        assertEquals(0.8, table.loadFactor(), 1e-9);
    }

    @Test
    public void getAndRemoveWalkOnlyOneChain() {
        ChainingHashTable<Integer, String> table = slideTable();
        assertEquals("B2", table.get(12));
        assertEquals("B2", table.remove(12));
        assertEquals(Arrays.asList(2, 22), table.chain(2));
        assertNull(table.get(12));
        assertNull(table.remove(12));
        assertEquals(7, table.size());
    }

    @Test
    public void rehashKeepsEveryEntry() {
        ChainingHashTable<Integer, String> table = new ChainingHashTable<>(5);
        for (int key = 0; key < 20; key++) {
            table.put(key, "v" + key);
        }
        assertTrue(table.capacity() > 5);
        assertTrue(table.loadFactor() <= 0.75);
        for (int key = 0; key < 20; key++) {
            assertEquals("v" + key, table.get(key));
        }
    }
}
