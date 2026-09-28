package csd201.tree.binary;

import csd201.tree.Tree;
import java.util.ArrayList;
import java.util.List;

public abstract class BinaryTree<E> extends Tree<E> {

    protected abstract BinaryNode<E> root();

    public List<E> inOrder() {
        List<E> visited = new ArrayList<>();
        inOrder(root(), visited);
        return visited;
    }

    public String toString() {
        return shape(root());
    }

    private void inOrder(BinaryNode<E> node, List<E> visited) {
        if (node == null) {
            return;
        }
        inOrder(node.left(), visited);
        visited.add(node.element());
        inOrder(node.right(), visited);
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
