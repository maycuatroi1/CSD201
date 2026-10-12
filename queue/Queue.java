package queue;

import java.util.List;

public abstract class Queue<T> {
    public abstract int getSize();

    public abstract void enqueue(T element);

    public abstract T dequeue();

    public abstract T peek();

    public abstract Iterable<T> getStorage();

    public abstract List<T> toList();

    public boolean isEmpty() {
        return getSize() == 0;
    }

    public String toString() {
        return toList().toString();
    }
}
