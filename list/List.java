package list;

import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class List<T> implements Iterable<T> {
    Node<T> head;
    Node<T> tail;
    int size;

    public abstract void append(T element);

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Node<T> getHead() {
        return head;
    }

    public Node<T> getTail() {
        return tail;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Node<T> current = head;

            public boolean hasNext() {
                return current != null;
            }

            public T next() {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                T data = current.getData();
                current = current.getNext();
                return data;
            }
        };
    }

    public String toString() {
        StringBuilder text = new StringBuilder("[");
        for (Node<T> node = head; node != null; node = node.getNext()) {
            text.append(node.getData());
            if (node.getNext() != null) {
                text.append(" <-> ");
            }
        }
        return text.append("]").toString();
    }
}
