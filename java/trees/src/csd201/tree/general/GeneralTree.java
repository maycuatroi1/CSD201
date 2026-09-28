package csd201.tree.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public final class GeneralTree<E> {

    public static final class Node<E> {
        private final E element;
        private final Node<E> parent;
        private final List<Node<E>> children = new ArrayList<>();

        private Node(E element, Node<E> parent) {
            this.element = element;
            this.parent = parent;
        }

        public E element() {
            return element;
        }
    }

    private Node<E> root;
    private int size;

    public Node<E> addRoot(E element) {
        if (root != null) {
            throw new IllegalStateException("The tree already has a root");
        }
        root = new Node<>(element, null);
        size = 1;
        return root;
    }

    public Node<E> addChild(Node<E> parent, E element) {
        Node<E> child = new Node<>(element, parent);
        parent.children.add(child);
        size++;
        return child;
    }

    public Node<E> root() {
        if (root == null) {
            throw new NoSuchElementException("The tree is empty");
        }
        return root;
    }

    public Optional<Node<E>> parent(Node<E> node) {
        return Optional.ofNullable(node.parent);
    }

    public List<Node<E>> children(Node<E> node) {
        return Collections.unmodifiableList(node.children);
    }

    public int numChildren(Node<E> node) {
        return node.children.size();
    }

    public boolean isRoot(Node<E> node) {
        return node == root;
    }

    public boolean isLeaf(Node<E> node) {
        return numChildren(node) == 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int depth(Node<E> node) {
        if (isRoot(node)) {
            return 0;
        }
        return 1 + depth(node.parent);
    }

    public int height(Node<E> node) {
        int height = 0;
        for (Node<E> child : node.children) {
            height = Math.max(height, 1 + height(child));
        }
        return height;
    }

    public int height() {
        return isEmpty() ? -1 : height(root);
    }

    public List<E> preOrder() {
        List<E> visited = new ArrayList<>();
        if (!isEmpty()) {
            preOrder(root, visited);
        }
        return visited;
    }

    public List<E> postOrder() {
        List<E> visited = new ArrayList<>();
        if (!isEmpty()) {
            postOrder(root, visited);
        }
        return visited;
    }

    private void preOrder(Node<E> node, List<E> visited) {
        visited.add(node.element);
        for (Node<E> child : node.children) {
            preOrder(child, visited);
        }
    }

    private void postOrder(Node<E> node, List<E> visited) {
        for (Node<E> child : node.children) {
            postOrder(child, visited);
        }
        visited.add(node.element);
    }
}
