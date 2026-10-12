package stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.junit.Test;

public class ArrayStackTest extends StackContract {

    Stack<Integer> newStack() {
        return new ArrayStack<>(new Integer[4]);
    }

    @Test(expected = IllegalStateException.class)
    public void pushOnAFullStackThrows() {
        ArrayStack<Integer> stack = new ArrayStack<>(new Integer[2]);
        stack.push(10);
        stack.push(20);
        stack.push(30);
    }

    @Test
    public void aRejectedPushLeavesTheStackUnchanged() {
        ArrayStack<Integer> stack = new ArrayStack<>(new Integer[2]);
        stack.push(10);
        stack.push(20);
        try {
            stack.push(30);
            fail();
        } catch (IllegalStateException expected) {
        }
        assertEquals(Arrays.asList(20, 10), stack.toList());
        assertEquals(2, stack.getSize());
    }

    @Test
    public void popClearsTheSlotItLeaves() {
        ArrayStack<Integer> stack = new ArrayStack<>(new Integer[3]);
        stack.push(10);
        stack.push(20);
        stack.pop();
        assertEquals(Arrays.asList(10, null, null), slots(stack));
    }

    @Test
    public void theStackDoesNotShareTheArrayItWasGiven() {
        Integer[] storage = new Integer[3];
        ArrayStack<Integer> stack = new ArrayStack<>(storage);
        stack.push(10);
        assertNull(storage[0]);
        assertEquals(3, stack.getCapacity());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void theStorageViewIsReadOnly() {
        ArrayStack<Integer> stack = new ArrayStack<>(new Integer[3]);
        stack.push(10);
        Iterator<Integer> slots = stack.getStorage().iterator();
        slots.next();
        slots.remove();
    }

    private static ArrayList<Integer> slots(Stack<Integer> stack) {
        ArrayList<Integer> slots = new ArrayList<>();
        for (Integer slot : stack.getStorage()) {
            slots.add(slot);
        }
        return slots;
    }
}
