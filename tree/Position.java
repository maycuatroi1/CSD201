package tree;

public final class Position<T> {
    private final Node<T> node;
    private final Position<T> parent;
    private final Side side;

    private Position(Node<T> node, Position<T> parent, Side side) {
        this.node = node;
        this.parent = parent;
        this.side = side;
    }

    static <T> Position<T> root(Node<T> root) {
        return new Position<>(root, null, Side.ROOT);
    }

    static <T> Position<T> notFound() {
        return new Position<>(null, null, Side.ROOT);
    }

    Position<T> left() {
        return new Position<>(node.getLeft(), this, Side.LEFT);
    }

    Position<T> right() {
        return new Position<>(node.getRight(), this, Side.RIGHT);
    }

    public Node<T> getNode() {
        return node;
    }

    public Position<T> getParent() {
        return parent;
    }

    public boolean isEmpty() {
        return node == null;
    }

    public boolean isRoot() {
        return side == Side.ROOT;
    }

    public boolean isLeftChild() {
        return side == Side.LEFT;
    }

    public boolean isRightChild() {
        return side == Side.RIGHT;
    }
}
