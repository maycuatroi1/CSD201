package csd201.tree;

import java.util.NoSuchElementException;
import java.util.Objects;

public class AVLTree<T extends Comparable<T>> extends BinarySearchTree<T> {

    public boolean insert(T element) {
        Objects.requireNonNull(element, "element");
        if (!findPosition(element).isEmpty()) {
            return false;
        }
        setRoot(insertInto(getRoot(), element));
        return true;
    }

    public boolean remove(T value) {
        if (findPosition(value).isEmpty()) {
            return false;
        }
        setRoot(removeFrom(getRoot(), value));
        return true;
    }

    public boolean deleteByCopying(T value) {
        return remove(value);
    }

    public boolean deleteByMerging(T value) {
        return remove(value);
    }

    public int balanceFactor(T value) {
        Position<T> position = findPosition(value);
        if (position.isEmpty()) {
            throw new NoSuchElementException("Not in the tree: " + value);
        }
        return balanceOf(position.getNode());
    }

    private Node<T> insertInto(Node<T> node, T element) {
        if (node == null) {
            return new Node<>(element);
        }
        if (element.compareTo(node.getData()) < 0) {
            node.setLeft(insertInto(node.getLeft(), element));
        } else {
            node.setRight(insertInto(node.getRight(), element));
        }
        return rebalance(node);
    }

    private Node<T> removeFrom(Node<T> node, T value) {
        int comparison = value.compareTo(node.getData());
        if (comparison < 0) {
            node.setLeft(removeFrom(node.getLeft(), value));
        } else if (comparison > 0) {
            node.setRight(removeFrom(node.getRight(), value));
        } else if (node.getLeft() == null) {
            return node.getRight();
        } else if (node.getRight() == null) {
            return node.getLeft();
        } else {
            T predecessor = rightmostData(node.getLeft());
            node.setData(predecessor);
            node.setLeft(removeFrom(node.getLeft(), predecessor));
        }
        return rebalance(node);
    }

    private Node<T> rebalance(Node<T> node) {
        node.updateHeight();
        int balance = balanceOf(node);
        if (balance < -1) {
            if (balanceOf(node.getLeft()) > 0) {
                node.setLeft(rotateLeft(node.getLeft()));
            }
            return rotateRight(node);
        }
        if (balance > 1) {
            if (balanceOf(node.getRight()) < 0) {
                node.setRight(rotateRight(node.getRight()));
            }
            return rotateLeft(node);
        }
        return node;
    }

    private Node<T> rotateLeft(Node<T> node) {
        Node<T> newRoot = node.getRight();
        node.setRight(newRoot.getLeft());
        newRoot.setLeft(node);
        node.updateHeight();
        newRoot.updateHeight();
        return newRoot;
    }

    private Node<T> rotateRight(Node<T> node) {
        Node<T> newRoot = node.getLeft();
        node.setLeft(newRoot.getRight());
        newRoot.setRight(node);
        node.updateHeight();
        newRoot.updateHeight();
        return newRoot;
    }

    private int balanceOf(Node<T> node) {
        return Node.heightOf(node.getRight()) - Node.heightOf(node.getLeft());
    }

    private T rightmostData(Node<T> node) {
        Node<T> current = node;
        while (current.getRight() != null) {
            current = current.getRight();
        }
        return current.getData();
    }
}
