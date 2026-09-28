package csd201.tree.avl;

import csd201.tree.binary.BinaryNode;
import csd201.tree.binary.BinaryTree;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class AvlTree<E extends Comparable<E>> extends BinaryTree<E> {

    private static final class Node<E> implements BinaryNode<E> {
        private E key;
        private Node<E> left;
        private Node<E> right;
        private int height;

        private Node(E key) {
            this.key = key;
        }

        public E element() {
            return key;
        }

        public Node<E> left() {
            return left;
        }

        public Node<E> right() {
            return right;
        }
    }

    private Node<E> root;

    public static <E extends Comparable<E>> AvlTree<E> of(Collection<E> keys) {
        AvlTree<E> tree = new AvlTree<>();
        for (E key : keys) {
            tree.insert(key);
        }
        return tree;
    }

    public boolean contains(E key) {
        return find(key) != null;
    }

    public boolean insert(E key) {
        Objects.requireNonNull(key, "key");
        if (contains(key)) {
            return false;
        }
        root = insert(root, key);
        return true;
    }

    public boolean delete(E key) {
        if (!contains(key)) {
            return false;
        }
        root = delete(root, key);
        return true;
    }

    public int balanceFactor(E key) {
        Node<E> node = find(key);
        if (node == null) {
            throw new NoSuchElementException("Key not found: " + key);
        }
        return balanceFactor(node);
    }

    protected BinaryNode<E> root() {
        return root;
    }

    private Node<E> find(E key) {
        Node<E> current = root;
        while (current != null) {
            int comparison = key.compareTo(current.key);
            if (comparison == 0) {
                return current;
            }
            current = comparison < 0 ? current.left : current.right;
        }
        return null;
    }

    private Node<E> insert(Node<E> node, E key) {
        if (node == null) {
            return new Node<>(key);
        }
        if (key.compareTo(node.key) < 0) {
            node.left = insert(node.left, key);
        } else {
            node.right = insert(node.right, key);
        }
        return rebalance(node);
    }

    private Node<E> delete(Node<E> node, E key) {
        int comparison = key.compareTo(node.key);
        if (comparison < 0) {
            node.left = delete(node.left, key);
        } else if (comparison > 0) {
            node.right = delete(node.right, key);
        } else if (node.left == null) {
            return node.right;
        } else if (node.right == null) {
            return node.left;
        } else {
            Node<E> predecessor = rightmost(node.left);
            node.key = predecessor.key;
            node.left = delete(node.left, predecessor.key);
        }
        return rebalance(node);
    }

    private Node<E> rebalance(Node<E> node) {
        updateHeight(node);
        int balance = balanceFactor(node);
        if (balance < -1) {
            if (balanceFactor(node.left) > 0) {
                node.left = rotateLeft(node.left);
            }
            return rotateRight(node);
        }
        if (balance > 1) {
            if (balanceFactor(node.right) < 0) {
                node.right = rotateRight(node.right);
            }
            return rotateLeft(node);
        }
        return node;
    }

    private Node<E> rotateRight(Node<E> parent) {
        Node<E> child = parent.left;
        parent.left = child.right;
        child.right = parent;
        updateHeight(parent);
        updateHeight(child);
        return child;
    }

    private Node<E> rotateLeft(Node<E> parent) {
        Node<E> child = parent.right;
        parent.right = child.left;
        child.left = parent;
        updateHeight(parent);
        updateHeight(child);
        return child;
    }

    private Node<E> rightmost(Node<E> node) {
        Node<E> current = node;
        while (current.right != null) {
            current = current.right;
        }
        return current;
    }

    private int balanceFactor(Node<E> node) {
        return heightOf(node.right) - heightOf(node.left);
    }

    private void updateHeight(Node<E> node) {
        node.height = 1 + Math.max(heightOf(node.left), heightOf(node.right));
    }

    private int heightOf(Node<E> node) {
        return node == null ? -1 : node.height;
    }
}
