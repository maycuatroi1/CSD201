package list;

public class LinkedList<T> extends List<T> {
    public void append(T element) {
        Node<T> node = new Node<>(element);
        if (isEmpty()) {
            head = node;
        } else {
            tail.setNext(node);
            node.setPrev(tail);
        }
        tail = node;
        size++;
    }

    public void addFirst(T element) {
        insertAt(0, element);
    }

    public void addLast(T element) {
        append(element);
    }

    public Node<T> getByIndex(int index) {
        checkIndex(index);
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        return current;
    }

    public void insertAt(int index, T element) {
        if (index == size) {
            append(element);
            return;
        }
        Node<T> next = getByIndex(index);
        Node<T> previous = next.getPrev();
        Node<T> node = new Node<>(element);
        node.setPrev(previous);
        node.setNext(next);
        if (previous == null) {
            head = node;
        } else {
            previous.setNext(node);
        }
        next.setPrev(node);
        size++;
    }

    public T deleteAt(int index) {
        Node<T> node = getByIndex(index);
        Node<T> previous = node.getPrev();
        Node<T> next = node.getNext();
        if (previous == null) {
            head = next;
        } else {
            previous.setNext(next);
        }
        if (next == null) {
            tail = previous;
        } else {
            next.setPrev(previous);
        }
        node.setPrev(null);
        node.setNext(null);
        size--;
        return node.getData();
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }
}
