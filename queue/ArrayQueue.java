package queue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ArrayQueue<T> extends Queue<T> {
    private final T[] elements;
    private int front;
    private int size;

    public ArrayQueue(T[] storage) {
        elements = storage.clone();
        Arrays.fill(elements, null);
    }

    public int getSize() {
        return size;
    }

    public int getCapacity() {
        return elements.length;
    }

    public void enqueue(T element) {
        Objects.requireNonNull(element);
        if (size == elements.length) {
            throw new IllegalStateException("Queue is full");
        }
        elements[(front + size) % elements.length] = element;
        size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            return null;
        }
        T first = elements[front];
        elements[front] = null;
        front = (front + 1) % elements.length;
        size--;
        return first;
    }

    public T peek() {
        return isEmpty() ? null : elements[front];
    }

    public Iterable<T> getStorage() {
        return Collections.unmodifiableList(Arrays.asList(elements));
    }

    public List<T> toList() {
        List<T> frontToBack = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            frontToBack.add(elements[(front + i) % elements.length]);
        }
        return frontToBack;
    }
}
