package csd201.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

public class BinaryTree<T> extends AbstractTree<T> {

    public void buildTree(List<T> elements) {
        setRoot(null);
        List<Node<T>> nodesInLevelOrder = new ArrayList<>(elements.size());
        Queue<Node<T>> parentsWithFreeSlot = new ArrayDeque<>();
        for (T element : elements) {
            Node<T> node = new Node<>(Objects.requireNonNull(element, "element"));
            if (isEmpty()) {
                setRoot(node);
            } else {
                Node<T> parent = parentsWithFreeSlot.peek();
                if (parent.getLeft() == null) {
                    parent.setLeft(node);
                } else {
                    parent.setRight(node);
                    parentsWithFreeSlot.remove();
                }
            }
            parentsWithFreeSlot.add(node);
            nodesInLevelOrder.add(node);
        }
        for (int i = nodesInLevelOrder.size() - 1; i >= 0; i--) {
            nodesInLevelOrder.get(i).updateHeight();
        }
    }

    public boolean insert(T element) {
        Node<T> node = new Node<>(Objects.requireNonNull(element, "element"));
        if (isEmpty()) {
            setRoot(node);
            return true;
        }
        Position<T> parent = firstPositionWithFreeSlot();
        if (parent.getNode().getLeft() == null) {
            parent.getNode().setLeft(node);
        } else {
            parent.getNode().setRight(node);
        }
        updateHeightsFrom(parent);
        return true;
    }

    public boolean remove(T value) {
        Position<T> target = search(value);
        if (target.isEmpty()) {
            return false;
        }
        Position<T> deepest = deepestPosition();
        target.getNode().setData(deepest.getNode().getData());
        replace(deepest, null);
        updateHeightsFrom(deepest.getParent());
        return true;
    }

    public Position<T> getSibling(Position<T> position) {
        if (position.isRoot()) {
            return Position.notFound();
        }
        Position<T> parent = position.getParent();
        return position.isLeftChild() ? parent.right() : parent.left();
    }

    private Position<T> firstPositionWithFreeSlot() {
        Queue<Position<T>> queue = new ArrayDeque<>();
        queue.add(Position.root(getRoot()));
        while (true) {
            Position<T> position = queue.remove();
            Node<T> node = position.getNode();
            if (node.getLeft() == null || node.getRight() == null) {
                return position;
            }
            queue.add(position.left());
            queue.add(position.right());
        }
    }

    private Position<T> deepestPosition() {
        Queue<Position<T>> queue = new ArrayDeque<>();
        queue.add(Position.root(getRoot()));
        Position<T> last = null;
        while (!queue.isEmpty()) {
            last = queue.remove();
            if (last.getNode().getLeft() != null) {
                queue.add(last.left());
            }
            if (last.getNode().getRight() != null) {
                queue.add(last.right());
            }
        }
        return last;
    }
}
