package queue;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.List;
import org.junit.Test;
import stack.LinkStack;
import stack.Stack;

public class HogwartsVisualizerTest {

    @Test
    public void theInsideOfAFullRingMarksFrontAndBack() {
        ArrayQueue<String> real = new ArrayQueue<>(new String[5]);
        HogwartsVisualizer.Story story = story(HogwartsVisualizer.Shape.QUEUE, real);
        Queue<String> orders = new HogwartsVisualizer.WatchedQueue<>(real, story);
        for (String order : new String[] {"A", "B", "C", "D", "E"}) {
            orders.enqueue(order);
        }
        orders.dequeue();
        orders.dequeue();
        orders.enqueue("F");
        orders.enqueue("G");

        assertEquals(Arrays.asList("C", "D", "E", "F", "G"), orders.toList());
        assertEquals(10, story.size());
        HogwartsVisualizer.Step last = story.step(story.size() - 1);
        assertEquals(HogwartsVisualizer.Op.ENQUEUE, last.op);
        assertNull(last.warning);
        assertEquals(5, last.size);
        assertFalse(last.inside.linked);
        assertArrayEquals(new String[] {"F", "G", "C", "D", "E"}, last.inside.labels);
        assertEquals(2, last.inside.first);
        assertEquals(1, last.inside.last);
    }

    @Test
    public void aQueueThatServesTheNewestFirstIsCaught() {
        BackwardsQueue real = new BackwardsQueue();
        HogwartsVisualizer.Story story = story(HogwartsVisualizer.Shape.QUEUE, real);
        Queue<String> orders = new HogwartsVisualizer.WatchedQueue<>(real, story);
        orders.enqueue("A");
        orders.enqueue("B");
        assertEquals("B", orders.dequeue());
        assertEquals("BUT THE FRONT OF THE LINE WAS A", story.step(story.size() - 1).warning);
    }

    @Test
    public void linkedStorageIsDrawnAsNodesFromHeadToTail() {
        LinkStack<String> real = new LinkStack<>();
        HogwartsVisualizer.Story story = story(HogwartsVisualizer.Shape.STACK, real);
        Stack<String> trolley = new HogwartsVisualizer.WatchedStack<>(real, story);
        trolley.push("A");
        trolley.push("B");
        HogwartsVisualizer.Step last = story.step(story.size() - 1);
        assertNull(last.warning);
        assertTrue(last.inside.linked);
        assertArrayEquals(new String[] {"B", "A"}, last.inside.labels);
        assertEquals(1, last.inside.tail);
    }

    @Test
    public void anExceptionIsRecordedAndThrownAgain() {
        LinkStack<String> real = new LinkStack<>();
        HogwartsVisualizer.Story story = story(HogwartsVisualizer.Shape.STACK, real);
        Stack<String> trolley = new HogwartsVisualizer.WatchedStack<>(real, story);
        try {
            trolley.pop();
            fail();
        } catch (EmptyStackException expected) {
        }
        assertEquals(HogwartsVisualizer.Op.POP, story.step(1).op);
        assertEquals("EMPTYSTACKEXCEPTION", story.step(1).error);
    }

    @Test(expected = IllegalArgumentException.class)
    public void watchNeedsAnEmptyQueue() {
        ArrayQueue<String> orders = new ArrayQueue<>(new String[3]);
        orders.enqueue("A");
        HogwartsVisualizer.watch(orders);
    }

    private static HogwartsVisualizer.Story story(HogwartsVisualizer.Shape shape, Queue<String> queue) {
        HogwartsVisualizer.Story story = new HogwartsVisualizer.Story(shape, "test", 0, null, 0);
        story.start(queue::getSize, queue::getStorage);
        return story;
    }

    private static HogwartsVisualizer.Story story(HogwartsVisualizer.Shape shape, Stack<String> stack) {
        HogwartsVisualizer.Story story = new HogwartsVisualizer.Story(shape, "test", 0, null, 0);
        story.start(stack::getSize, stack::getStorage);
        return story;
    }

    static final class BackwardsQueue extends Queue<String> {
        private final LinkStack<String> newestFirst = new LinkStack<>();

        public int getSize() {
            return newestFirst.getSize();
        }

        public void enqueue(String element) {
            newestFirst.push(element);
        }

        public String dequeue() {
            return newestFirst.isEmpty() ? null : newestFirst.pop();
        }

        public String peek() {
            return newestFirst.isEmpty() ? null : newestFirst.peek();
        }

        public Iterable<String> getStorage() {
            return newestFirst.getStorage();
        }

        public List<String> toList() {
            return newestFirst.toList();
        }
    }
}
