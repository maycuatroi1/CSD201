package csd201.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public abstract class Tree<E> {

    protected abstract TreeNode<E> root();

    public boolean isEmpty() {
        return root() == null;
    }

    public int size() {
        return isEmpty() ? 0 : subtreeSize(root());
    }

    public int height() {
        return isEmpty() ? -1 : subtreeHeight(root());
    }

    public List<E> preOrder() {
        List<E> visited = new ArrayList<>();
        if (!isEmpty()) {
            preOrder(root(), visited);
        }
        return visited;
    }

    public List<E> postOrder() {
        List<E> visited = new ArrayList<>();
        if (!isEmpty()) {
            postOrder(root(), visited);
        }
        return visited;
    }

    public List<E> breadthFirst() {
        List<E> visited = new ArrayList<>();
        Queue<TreeNode<E>> queue = new ArrayDeque<>();
        if (!isEmpty()) {
            queue.add(root());
        }
        while (!queue.isEmpty()) {
            TreeNode<E> node = queue.remove();
            visited.add(node.element());
            queue.addAll(node.children());
        }
        return visited;
    }

    protected int subtreeHeight(TreeNode<E> node) {
        int height = 0;
        for (TreeNode<E> child : node.children()) {
            height = Math.max(height, 1 + subtreeHeight(child));
        }
        return height;
    }

    private int subtreeSize(TreeNode<E> node) {
        int size = 1;
        for (TreeNode<E> child : node.children()) {
            size += subtreeSize(child);
        }
        return size;
    }

    private void preOrder(TreeNode<E> node, List<E> visited) {
        visited.add(node.element());
        for (TreeNode<E> child : node.children()) {
            preOrder(child, visited);
        }
    }

    private void postOrder(TreeNode<E> node, List<E> visited) {
        for (TreeNode<E> child : node.children()) {
            postOrder(child, visited);
        }
        visited.add(node.element());
    }
}
