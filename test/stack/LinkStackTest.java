package stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import list.LinkedList;
import org.junit.Test;

public class LinkStackTest extends StackContract {

    Stack<Integer> newStack() {
        return new LinkStack<>();
    }

    @Test
    public void theTopLivesAtTheHeadOfTheList() {
        LinkStack<Integer> stack = new LinkStack<>();
        stack.push(10);
        stack.push(20);
        LinkedList<Integer> nodes = (LinkedList<Integer>) stack.getStorage();
        assertEquals(Integer.valueOf(20), nodes.getHead().getData());
        assertEquals(Integer.valueOf(10), nodes.getTail().getData());
    }

    @Test
    public void poppingTheLastElementEmptiesTheList() {
        LinkStack<Integer> stack = new LinkStack<>();
        stack.push(10);
        assertEquals(Integer.valueOf(10), stack.pop());
        LinkedList<Integer> nodes = (LinkedList<Integer>) stack.getStorage();
        assertTrue(nodes.isEmpty());
        assertNull(nodes.getHead());
        assertNull(nodes.getTail());
    }
}
