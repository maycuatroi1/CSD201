package csd201.tree.bst;

import csd201.tree.binary.BinaryNode;
import csd201.tree.binary.BinaryTree;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public final class BinarySearchTree<E extends Comparable<E>> extends BinaryTree<E> {

    private static final class Node<E> implements BinaryNode<E> {
        private E key;
        private Node<E> left;
        private Node<E> right;

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

    private static final class Location<E> {
        private final Node<E> parent;
        private final Node<E> node;

        private Location(Node<E> parent, Node<E> node) {
            this.parent = parent;
            this.node = node;
        }
    }

    private Node<E> root;

    public static <E extends Comparable<E>> BinarySearchTree<E> of(Collection<E> keys) {
        BinarySearchTree<E> tree = new BinarySearchTree<>();
        for (E key : keys) {
            tree.insert(key);
        }
        return tree;
    }

    public boolean contains(E key) {
        return locate(key).node != null;
    }

    public boolean insert(E key) {
        Objects.requireNonNull(key, "key");
        Location<E> location = locate(key);
        if (location.node != null) {
            return false;
        }
        Node<E> node = new Node<>(key);
        if (location.parent == null) {
            root = node;
        } else if (key.compareTo(location.parent.key) < 0) {
            location.parent.left = node;
        } else {
            location.parent.right = node;
        }
        return true;
    }

    public boolean deleteByMerging(E key) {
        Location<E> location = locate(key);
        Node<E> node = location.node;
        if (node == null) {
            return false;
        }
        replaceChild(location.parent, node, merge(node.left, node.right));
        return true;
    }

    public boolean deleteByCopying(E key) {
        Location<E> location = locate(key);
        Node<E> node = location.node;
        if (node == null) {
            return false;
        }
        if (node.left != null && node.right != null) {
            copyPredecessorInto(node);
        } else {
            replaceChild(location.parent, node, node.left != null ? node.left : node.right);
        }
        return true;
    }

    public void balance() {
        List<E> sortedKeys = inOrder();
        root = null;
        insertMiddleFirst(sortedKeys, 0, sortedKeys.size() - 1);
    }

    public E min() {
        return leftmost(nonEmptyRoot()).key;
    }

    public E max() {
        return rightmost(nonEmptyRoot()).key;
    }

    protected BinaryNode<E> root() {
        return root;
    }

    private Location<E> locate(E key) {
        Node<E> parent = null;
        Node<E> current = root;
        while (current != null) {
            int comparison = key.compareTo(current.key);
            if (comparison == 0) {
                return new Location<>(parent, current);
            }
            parent = current;
            current = comparison < 0 ? current.left : current.right;
        }
        return new Location<>(parent, null);
    }

    private Node<E> merge(Node<E> left, Node<E> right) {
        if (left == null) {
            return right;
        }
        rightmost(left).right = right;
        return left;
    }

    private void copyPredecessorInto(Node<E> node) {
        Node<E> parent = node;
        Node<E> predecessor = node.left;
        while (predecessor.right != null) {
            parent = predecessor;
            predecessor = predecessor.right;
        }
        node.key = predecessor.key;
        replaceChild(parent, predecessor, predecessor.left);
    }

    private void replaceChild(Node<E> parent, Node<E> child, Node<E> replacement) {
        if (parent == null) {
            root = replacement;
        } else if (parent.left == child) {
            parent.left = replacement;
        } else {
            parent.right = replacement;
        }
    }

    private void insertMiddleFirst(List<E> sortedKeys, int first, int last) {
        if (first > last) {
            return;
        }
        int middle = (first + last) / 2;
        insert(sortedKeys.get(middle));
        insertMiddleFirst(sortedKeys, first, middle - 1);
        insertMiddleFirst(sortedKeys, middle + 1, last);
    }

    private Node<E> nonEmptyRoot() {
        if (root == null) {
            throw new NoSuchElementException("The tree is empty");
        }
        return root;
    }

    private Node<E> leftmost(Node<E> node) {
        Node<E> current = node;
        while (current.left != null) {
            current = current.left;
        }
        return current;
    }

    private Node<E> rightmost(Node<E> node) {
        Node<E> current = node;
        while (current.right != null) {
            current = current.right;
        }
        return current;
    }
}
