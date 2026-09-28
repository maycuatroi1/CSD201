package csd201.tree.general;

import csd201.tree.Tree;
import csd201.tree.TreeNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class GeneralTree<E> extends Tree<E> {

    public static final class Node<E> implements TreeNode<E> {
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

        public List<Node<E>> children() {
            return Collections.unmodifiableList(children);
        }
    }

    private Node<E> root;

    public Node<E> root() {
        return root;
    }

    public Node<E> addRoot(E element) {
        if (root != null) {
            throw new IllegalStateException("The tree already has a root");
        }
        root = new Node<>(element, null);
        return root;
    }

    public Node<E> addChild(Node<E> parent, E element) {
        Node<E> child = new Node<>(element, parent);
        parent.children.add(child);
        return child;
    }

    public Optional<Node<E>> parent(Node<E> node) {
        return Optional.ofNullable(node.parent);
    }

    public List<Node<E>> children(Node<E> node) {
        return node.children();
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

    public int depth(Node<E> node) {
        if (isRoot(node)) {
            return 0;
        }
        return 1 + depth(node.parent);
    }

    public int height(Node<E> node) {
        return subtreeHeight(node);
    }
}
