package queue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Test;

public class ArrayQueueTest {

    @Test
    public void thePredictionFromTheSlides() {
        ArrayQueue<String> orders = new ArrayQueue<>(new String[5]);
        orders.enqueue("A");
        orders.enqueue("B");
        String a = orders.dequeue();
        orders.enqueue("C");
        String b = orders.peek();
        assertEquals("A", a);
        assertEquals("B", b);
        assertEquals(2, orders.getSize());
        assertEquals(Arrays.asList("B", "C"), orders.toList());
    }

    @Test
    public void oneArrayTwoViews() {
        ArrayQueue<String> orders = fullRing();
        assertEquals(Arrays.asList("F", "G", "C", "D", "E"), slots(orders));
        assertEquals(Arrays.asList("C", "D", "E", "F", "G"), orders.toList());
        assertEquals("[C, D, E, F, G]", orders.toString());
        assertEquals(5, orders.getSize());
    }

    @Test
    public void aRejectedEnqueueLeavesTheQueueUnchanged() {
        ArrayQueue<String> orders = fullRing();
        try {
            orders.enqueue("H");
            fail();
        } catch (IllegalStateException expected) {
        }
        assertEquals(Arrays.asList("F", "G", "C", "D", "E"), slots(orders));
        assertEquals(Arrays.asList("C", "D", "E", "F", "G"), orders.toList());
        assertEquals("C", orders.peek());
    }

    @Test(expected = IllegalStateException.class)
    public void capacityOneTakesOnlyOneElement() {
        ArrayQueue<String> orders = new ArrayQueue<>(new String[1]);
        orders.enqueue("A");
        orders.enqueue("B");
    }

    @Test
    public void theNextSlotWrapsWithModulo() {
        ArrayQueue<String> orders = new ArrayQueue<>(new String[5]);
        for (String order : new String[] {"A", "B", "C", "D"}) {
            orders.enqueue(order);
            orders.dequeue();
        }
        orders.enqueue("E");
        orders.enqueue("F");
        orders.enqueue("G");
        assertEquals(Arrays.asList("F", "G", null, null, "E"), slots(orders));
        assertEquals(Arrays.asList("E", "F", "G"), orders.toList());
    }

    @Test
    public void dequeueAndPeekOnAnEmptyQueueReturnNull() {
        ArrayQueue<String> orders = new ArrayQueue<>(new String[3]);
        assertNull(orders.dequeue());
        assertNull(orders.peek());
        assertTrue(orders.isEmpty());
    }

    @Test
    public void peekKeepsTheSizeAndTheContents() {
        ArrayQueue<String> orders = new ArrayQueue<>(new String[3]);
        orders.enqueue("A");
        orders.enqueue("B");
        assertEquals("A", orders.peek());
        assertEquals(2, orders.getSize());
        assertEquals(Arrays.asList("A", "B"), orders.toList());
    }

    @Test
    public void afterDrainingTheNextElementIsTheFront() {
        ArrayQueue<String> orders = new ArrayQueue<>(new String[3]);
        orders.enqueue("A");
        orders.dequeue();
        orders.enqueue("B");
        assertEquals("B", orders.peek());
        assertEquals(Arrays.asList("B"), orders.toList());
    }

    @Test
    public void dequeueClearsTheSlotItLeaves() {
        ArrayQueue<String> orders = new ArrayQueue<>(new String[3]);
        orders.enqueue("A");
        orders.enqueue("B");
        orders.dequeue();
        assertEquals(Arrays.asList(null, "B", null), slots(orders));
    }

    @Test(expected = NullPointerException.class)
    public void nullElementsAreRejected() {
        new ArrayQueue<>(new String[3]).enqueue(null);
    }

    private static ArrayQueue<String> fullRing() {
        ArrayQueue<String> orders = new ArrayQueue<>(new String[5]);
        for (String order : new String[] {"A", "B", "C", "D", "E"}) {
            orders.enqueue(order);
        }
        orders.dequeue();
        orders.dequeue();
        orders.enqueue("F");
        orders.enqueue("G");
        return orders;
    }

    private static ArrayList<String> slots(Queue<String> queue) {
        ArrayList<String> slots = new ArrayList<>();
        for (String slot : queue.getStorage()) {
            slots.add(slot);
        }
        return slots;
    }
}
