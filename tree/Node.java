package tree;

public final class Node<T> {
    private T data;
    private Node<T> left;
    private Node<T> right;
    private int height;

    Node(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public Node<T> getLeft() {
        return left;
    }

    public Node<T> getRight() {
        return right;
    }

    public int getHeight() {
        return height;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    void setData(T data) {
        this.data = data;
    }

    void setLeft(Node<T> left) {
        this.left = left;
    }

    void setRight(Node<T> right) {
        this.right = right;
    }

    void updateHeight() {
        height = 1 + Math.max(heightOf(left), heightOf(right));
    }

    static int heightOf(Node<?> node) {
        return node == null ? -1 : node.height;
    }
}
