package list;

public final class Node<T> {
    private final T data;
    private Node<T> next;
    private Node<T> prev;

    Node(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public Node<T> getNext() {
        return next;
    }

    public Node<T> getPrev() {
        return prev;
    }

    void setNext(Node<T> next) {
        this.next = next;
    }

    void setPrev(Node<T> prev) {
        this.prev = prev;
    }
}
