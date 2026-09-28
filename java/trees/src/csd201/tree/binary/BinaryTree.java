package csd201.tree.binary;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public abstract class BinaryTree<E> {

    protected abstract BinaryNode<E> root();

    public boolean isEmpty() {
        return root() == null;
    }

    public int size() {
        return size(root());
    }

    public int height() {
        return height(root());
    }

    public List<E> preOrder() {
        List<E> visited = new ArrayList<>();
        preOrder(root(), visited);
        return visited;
    }

    public List<E> inOrder() {
        List<E> visited = new ArrayList<>();
        inOrder(root(), visited);
        return visited;
    }

    public List<E> postOrder() {
        List<E> visited = new ArrayList<>();
        postOrder(root(), visited);
        return visited;
    }

    public List<E> breadthFirst() {
        List<E> visited = new ArrayList<>();
        Queue<BinaryNode<E>> queue = new ArrayDeque<>();
        if (!isEmpty()) {
            queue.add(root());
        }
        while (!queue.isEmpty()) {
            BinaryNode<E> node = queue.remove();
            visited.add(node.element());
            if (node.left() != null) {
                queue.add(node.left());
            }
            if (node.right() != null) {
                queue.add(node.right());
            }
        }
        return visited;
    }

    public String toString() {
        return shape(root());
    }

    private void preOrder(BinaryNode<E> node, List<E> visited) {
        if (node == null) {
            return;
        }
        visited.add(node.element());
        preOrder(node.left(), visited);
        preOrder(node.right(), visited);
    }

    private void inOrder(BinaryNode<E> node, List<E> visited) {
        if (node == null) {
            return;
        }
        inOrder(node.left(), visited);
        visited.add(node.element());
        inOrder(node.right(), visited);
    }

    private void postOrder(BinaryNode<E> node, List<E> visited) {
        if (node == null) {
            return;
        }
        postOrder(node.left(), visited);
        postOrder(node.right(), visited);
        visited.add(node.element());
    }

    private int size(BinaryNode<E> node) {
        if (node == null) {
            return 0;
        }
        return 1 + size(node.left()) + size(node.right());
    }

    private int height(BinaryNode<E> node) {
        if (node == null) {
            return -1;
        }
        return 1 + Math.max(height(node.left()), height(node.right()));
    }

    private String shape(BinaryNode<E> node) {
        if (node == null) {
            return "-";
        }
        if (node.isLeaf()) {
            return String.valueOf(node.element());
        }
        return node.element() + "(" + shape(node.left()) + "," + shape(node.right()) + ")";
    }
}
