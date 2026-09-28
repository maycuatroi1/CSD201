package csd201.tree;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class BinarySearchTree<T extends Comparable<T>> extends BinaryTree<T> {

    public void buildTree(List<T> elements) {
        setRoot(null);
        for (T element : elements) {
            insert(element);
        }
    }

    public boolean insert(T element) {
        Objects.requireNonNull(element, "element");
        Position<T> position = findPosition(element);
        if (!position.isEmpty()) {
            return false;
        }
        replace(position, new Node<>(element));
        updateHeightsFrom(position.getParent());
        return true;
    }

    public Position<T> search(T value) {
        return findPosition(value);
    }

    public boolean remove(T value) {
        return deleteByCopying(value);
    }

    public boolean deleteByCopying(T value) {
        Position<T> position = findPosition(value);
        if (position.isEmpty()) {
            return false;
        }
        Node<T> node = position.getNode();
        if (node.getLeft() != null && node.getRight() != null) {
            Position<T> predecessor = rightmost(position.left());
            node.setData(predecessor.getNode().getData());
            replace(predecessor, predecessor.getNode().getLeft());
            updateHeightsFrom(predecessor.getParent());
        } else {
            replace(position, node.getLeft() != null ? node.getLeft() : node.getRight());
            updateHeightsFrom(position.getParent());
        }
        return true;
    }

    public boolean deleteByMerging(T value) {
        Position<T> position = findPosition(value);
        if (position.isEmpty()) {
            return false;
        }
        Node<T> node = position.getNode();
        if (node.getLeft() == null) {
            replace(position, node.getRight());
            updateHeightsFrom(position.getParent());
            return true;
        }
        Position<T> predecessor = rightmost(position.left());
        predecessor.getNode().setRight(node.getRight());
        replace(position, node.getLeft());
        updateHeightsFrom(predecessor);
        return true;
    }

    public void mergeTrees(Tree<T> otherTree) {
        for (T element : otherTree.preOrder()) {
            insert(element);
        }
    }

    public void balance() {
        List<T> sortedElements = inOrder();
        setRoot(null);
        insertMiddleFirst(sortedElements, 0, sortedElements.size() - 1);
    }

    public T min() {
        Node<T> current = nonEmptyRoot();
        while (current.getLeft() != null) {
            current = current.getLeft();
        }
        return current.getData();
    }

    public T max() {
        Node<T> current = nonEmptyRoot();
        while (current.getRight() != null) {
            current = current.getRight();
        }
        return current.getData();
    }

    protected final Position<T> findPosition(T value) {
        resetSearchCount();
        Position<T> position = Position.root(getRoot());
        while (!position.isEmpty()) {
            countComparison();
            int comparison = value.compareTo(position.getNode().getData());
            if (comparison == 0) {
                return position;
            }
            position = comparison < 0 ? position.left() : position.right();
        }
        return position;
    }

    private Position<T> rightmost(Position<T> start) {
        Position<T> position = start;
        while (position.getNode().getRight() != null) {
            position = position.right();
        }
        return position;
    }

    private void insertMiddleFirst(List<T> sortedElements, int first, int last) {
        if (first > last) {
            return;
        }
        int middle = (first + last) / 2;
        insert(sortedElements.get(middle));
        insertMiddleFirst(sortedElements, first, middle - 1);
        insertMiddleFirst(sortedElements, middle + 1, last);
    }

    private Node<T> nonEmptyRoot() {
        if (isEmpty()) {
            throw new NoSuchElementException("The tree is empty");
        }
        return getRoot();
    }
}
