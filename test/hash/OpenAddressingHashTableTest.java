package hash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class OpenAddressingHashTableTest {

    private static final int[] KEYS = {89, 18, 49, 58, 69};

    private static void putAll(OpenAddressingHashTable<Integer, String> table, int... keys) {
        for (int key : keys) {
            table.put(key, "v" + key);
        }
    }

    @Test
    public void linearProbingPlacesTheSlideKeys() {
        OpenAddressingHashTable<Integer, String> table = new LinearProbingHashTable<>(10);
        putAll(table, KEYS);
        assertEquals(Arrays.asList(49, 58, 69, null, null, null, null, null, 18, 89), table.slots());
        assertEquals(Arrays.asList(9, 0, 1, 2), table.lastProbes());
    }

    @Test
    public void quadraticProbingPlacesTheSlideKeys() {
        OpenAddressingHashTable<Integer, String> table = new QuadraticProbingHashTable<>(10);
        putAll(table, KEYS);
        assertEquals(Arrays.asList(49, null, 58, 69, null, null, null, null, 18, 89), table.slots());
    }

    @Test
    public void doubleHashingPlacesTheSlideKeys() {
        DoubleHashingHashTable<Integer, String> table = new DoubleHashingHashTable<>(10, 7);
        putAll(table, KEYS);
        assertEquals(Arrays.asList(69, null, null, 58, null, null, 49, null, 18, 89), table.slots());
        assertEquals(7, table.step(49));
        assertEquals(5, table.step(58));
        assertEquals(1, table.step(69));
    }

    @Test
    public void quadraticProbingReachesOnlySixSlotsOfElevenFromHomeThree() {
        QuadraticProbingHashTable<Integer, String> table = new QuadraticProbingHashTable<>(11);
        Set<Integer> reached = new HashSet<>();
        for (int i = 0; i < 11; i++) {
            reached.add(table.probe(3, i, null));
        }
        assertEquals(new HashSet<>(Arrays.asList(3, 4, 7, 1, 8, 6)), reached);
    }

    @Test
    public void searchStopsAtTheFirstEmptySlot() {
        OpenAddressingHashTable<Integer, String> table = new LinearProbingHashTable<>(10);
        putAll(table, KEYS);
        assertEquals("v69", table.get(69));
        assertEquals(Arrays.asList(9, 0, 1, 2), table.lastProbes());
        assertNull(table.get(79));
        assertEquals(Arrays.asList(9, 0, 1, 2, 3), table.lastProbes());
    }

    @Test
    public void deletedSlotKeepsLaterKeysReachable() {
        OpenAddressingHashTable<Integer, String> table = new LinearProbingHashTable<>(10, 0.9);
        String[] names = {"A1", "A4", "A2", "B4", "B1"};
        int[] keys = {1, 4, 2, 14, 11};
        for (int i = 0; i < keys.length; i++) {
            table.put(keys[i], names[i]);
        }
        assertEquals(Arrays.asList(null, 1, 2, 11, 4, 14, null, null, null, null), table.slots());
        assertEquals("A4", table.remove(4));
        assertTrue(table.isDeleted(4));
        assertEquals("B4", table.get(14));
        assertEquals("A2", table.remove(2));
        assertEquals("B1", table.get(11));
        assertEquals(Arrays.asList(1, 2, 3), table.lastProbes());
        assertEquals(3, table.size());
    }

    @Test
    public void insertionReusesTheFirstDeletedSlot() {
        OpenAddressingHashTable<Integer, String> table = new LinearProbingHashTable<>(10, 0.9);
        putAll(table, 1, 4, 2, 14, 11);
        table.remove(4);
        table.put(24, "C4");
        assertEquals(Integer.valueOf(24), table.slots().get(4));
        assertFalse(table.isDeleted(4));
        assertEquals(Arrays.asList(4, 5, 6), table.lastProbes());
    }

    @Test
    public void putReplacesTheValueOfAnExistingKey() {
        OpenAddressingHashTable<Integer, String> table = new LinearProbingHashTable<>(10);
        assertNull(table.put(18, "first"));
        assertEquals("first", table.put(18, "second"));
        assertEquals("second", table.get(18));
        assertEquals(1, table.size());
    }

    @Test
    public void rehashGrowsToAPrimeAtLeastTwiceTheCapacity() {
        OpenAddressingHashTable<Integer, String> table = new LinearProbingHashTable<>(7);
        putAll(table, 15, 22, 8);
        assertEquals(7, table.capacity());
        table.put(1, "v1");
        assertEquals(17, table.capacity());
        List<Integer> slots = table.slots();
        assertEquals(Integer.valueOf(1), slots.get(1));
        assertEquals(Integer.valueOf(22), slots.get(5));
        assertEquals(Integer.valueOf(8), slots.get(8));
        assertEquals(Integer.valueOf(15), slots.get(15));
        assertEquals(4, table.size());
    }

    @Test
    public void negativeHashCodesStayInsideTheTable() {
        OpenAddressingHashTable<Integer, String> table = new LinearProbingHashTable<>(10);
        table.put(-3, "minus three");
        assertEquals(7, table.hash(-3));
        assertEquals("minus three", table.get(-3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullKeyIsRejected() {
        new LinearProbingHashTable<>(10).put(null, "x");
    }
}
