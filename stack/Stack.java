package stack;

import java.util.List;

public abstract class Stack<T> {
    public abstract int getSize();

    public abstract void push(T element);

    public abstract T pop();

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
