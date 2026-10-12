package stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.EmptyStackException;
import org.junit.Test;

public abstract class StackContract {

    abstract Stack<Integer> newStack();

    @Test
    public void thePredictionFromTheSlides() {
        Stack<Integer> stack = newStack();
        stack.push(10);
        stack.push(20);
        int a = stack.pop();
        stack.push(30);
        int b = stack.peek();
        assertEquals(20, a);
        assertEquals(30, b);
        assertEquals(Arrays.asList(30, 10), stack.toList());
        assertEquals(2, stack.getSize());
    }

    @Test
    public void peekReadsAndPopRemoves() {
        Stack<Integer> stack = newStack();
        stack.push(10);
        assertEquals(Integer.valueOf(10), stack.peek());
        assertEquals(Integer.valueOf(10), stack.peek());
        assertEquals(1, stack.getSize());
        assertEquals(Integer.valueOf(10), stack.pop());
        assertTrue(stack.isEmpty());
    }

    @Test
    public void popReturnsTheElementsInReverseOrder() {
        Stack<Integer> stack = newStack();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertEquals(Integer.valueOf(3), stack.pop());
        assertEquals(Integer.valueOf(2), stack.pop());
        assertEquals(Integer.valueOf(1), stack.pop());
        assertTrue(stack.isEmpty());
        stack.push(4);
        assertFalse(stack.isEmpty());
        assertEquals(Integer.valueOf(4), stack.peek());
    }

    @Test(expected = EmptyStackException.class)
    public void popOnAnEmptyStackThrows() {
        newStack().pop();
    }

    @Test(expected = EmptyStackException.class)
    public void peekOnAnEmptyStackThrows() {
        newStack().peek();
    }

    @Test
    public void toStringListsFromTopToBottom() {
        Stack<Integer> stack = newStack();
        assertEquals("[]", stack.toString());
        stack.push(10);
        stack.push(30);
        assertEquals("[30, 10]", stack.toString());
    }
}
