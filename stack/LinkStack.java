package stack;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import list.LinkedList;

public class LinkStack<T> extends Stack<T> {
    private final LinkedList<T> nodes = new LinkedList<>();

    public int getSize() {
        return nodes.getSize();
    }

    public void push(T element) {
        nodes.addFirst(element);
    }

    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return nodes.deleteAt(0);
    }

    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return nodes.getHead().getData();
    }

    public Iterable<T> getStorage() {
        return nodes;
    }

    public List<T> toList() {
        List<T> topToBottom = new ArrayList<>(getSize());
        for (T element : nodes) {
            topToBottom.add(element);
        }
        return topToBottom;
    }
}
