package stack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.List;

public class ArrayStack<T> extends Stack<T> {
    private final T[] elements;
    private int size;

    public ArrayStack(T[] storage) {
        elements = storage.clone();
        Arrays.fill(elements, null);
    }

    public int getSize() {
        return size;
    }

    public int getCapacity() {
        return elements.length;
    }

    public void push(T element) {
        if (size == elements.length) {
            throw new IllegalStateException("Stack is full");
        }
        elements[size++] = element;
    }

    public T pop() {
        T top = peek();
        elements[--size] = null;
        return top;
    }

    public T peek() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return elements[size - 1];
    }

    public Iterable<T> getStorage() {
        return Collections.unmodifiableList(Arrays.asList(elements));
    }

    public List<T> toList() {
        List<T> topToBottom = new ArrayList<>(size);
        for (int i = size - 1; i >= 0; i--) {
            topToBottom.add(elements[i]);
        }
        return topToBottom;
    }
}
