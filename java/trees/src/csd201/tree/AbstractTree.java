package csd201.tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Queue;

public abstract class AbstractTree<T> implements Tree<T> {

    private Node<T> root;
    private int searchCount;

    public Node<T> getRoot() {
        return root;
    }

    public boolean isEmpty() {
        return root == null;
    }

    public int size() {
        return levelOrder().size();
    }

    public int height() {
        return Node.heightOf(root);
    }

    public List<T> preOrder() {
        List<T> visited = new ArrayList<>();
        Deque<Node<T>> stack = new ArrayDeque<>();
        pushIfPresent(stack, root);
        while (!stack.isEmpty()) {
            Node<T> node = stack.pop();
            visited.add(node.getData());
            pushIfPresent(stack, node.getRight());
            pushIfPresent(stack, node.getLeft());
        }
        return visited;
    }

    public List<T> inOrder() {
        List<T> visited = new ArrayList<>();
        Deque<Node<T>> stack = new ArrayDeque<>();
        Node<T> current = root;
        while (current != null || !stack.isEmpty()) {
            while (current != null) {
                stack.push(current);
                current = current.getLeft();
            }
            current = stack.pop();
            visited.add(current.getData());
            current = current.getRight();
        }
        return visited;
    }

    public List<T> postOrder() {
        List<T> nodeRightLeft = new ArrayList<>();
        Deque<Node<T>> stack = new ArrayDeque<>();
        pushIfPresent(stack, root);
        while (!stack.isEmpty()) {
            Node<T> node = stack.pop();
            nodeRightLeft.add(node.getData());
            pushIfPresent(stack, node.getLeft());
            pushIfPresent(stack, node.getRight());
        }
        Collections.reverse(nodeRightLeft);
        return nodeRightLeft;
    }

    public List<T> levelOrder() {
        List<T> visited = new ArrayList<>();
        Queue<Node<T>> queue = new ArrayDeque<>();
        if (root != null) {
            queue.add(root);
        }
        while (!queue.isEmpty()) {
            Node<T> node = queue.remove();
            visited.add(node.getData());
            if (node.getLeft() != null) {
                queue.add(node.getLeft());
            }
            if (node.getRight() != null) {
                queue.add(node.getRight());
            }
        }
        return visited;
    }

    public Position<T> search(T value) {
        resetSearchCount();
        Deque<Position<T>> stack = new ArrayDeque<>();
        if (root != null) {
            stack.push(Position.root(root));
        }
        while (!stack.isEmpty()) {
            Position<T> position = stack.pop();
            Node<T> node = position.getNode();
            countComparison();
            if (node.getData().equals(value)) {
                return position;
            }
            if (node.getRight() != null) {
                stack.push(position.right());
            }
            if (node.getLeft() != null) {
                stack.push(position.left());
            }
        }
        return Position.notFound();
    }

    public int getSearchCount() {
        return searchCount;
    }

    public void printTree() {
        System.out.print(render());
    }

    public String toString() {
        return shape(root);
    }

    String render() {
        if (root == null) {
            return "(empty tree)" + System.lineSeparator();
        }
        StringBuilder text = new StringBuilder();
        for (String line : draw(root).lines) {
            text.append(line.replaceAll("\\s+$", "")).append(System.lineSeparator());
        }
        return text.toString();
    }

    protected final void setRoot(Node<T> root) {
        this.root = root;
    }

    protected final void resetSearchCount() {
        searchCount = 0;
    }

    protected final void countComparison() {
        searchCount++;
    }

    protected final void replace(Position<T> position, Node<T> replacement) {
        if (position.isRoot()) {
            setRoot(replacement);
        } else if (position.isLeftChild()) {
            position.getParent().getNode().setLeft(replacement);
        } else {
            position.getParent().getNode().setRight(replacement);
        }
    }

    protected final void updateHeightsFrom(Position<T> position) {
        for (Position<T> current = position; current != null; current = current.getParent()) {
            current.getNode().updateHeight();
        }
    }

    private void pushIfPresent(Deque<Node<T>> stack, Node<T> node) {
        if (node != null) {
            stack.push(node);
        }
    }

    private String shape(Node<T> node) {
        if (node == null) {
            return "-";
        }
        if (node.isLeaf()) {
            return String.valueOf(node.getData());
        }
        return node.getData() + "(" + shape(node.getLeft()) + "," + shape(node.getRight()) + ")";
    }

    private static final class Drawing {
        private final List<String> lines;
        private final int width;
        private final int rootColumn;

        private Drawing(List<String> lines, int width, int rootColumn) {
            this.lines = lines;
            this.width = width;
            this.rootColumn = rootColumn;
        }
    }

    private Drawing draw(Node<T> node) {
        String label = String.valueOf(node.getData());
        Drawing left = node.getLeft() == null ? null : draw(node.getLeft());
        Drawing right = node.getRight() == null ? null : draw(node.getRight());

        List<String> lines = new ArrayList<>();
        if (left == null && right == null) {
            lines.add(label);
            return new Drawing(lines, label.length(), label.length() / 2);
        }

        StringBuilder labelLine = new StringBuilder();
        StringBuilder edgeLine = new StringBuilder();
        if (left != null) {
            labelLine.append(repeat(' ', left.rootColumn + 1)).append(repeat('_', left.width - left.rootColumn - 1));
            edgeLine.append(repeat(' ', left.rootColumn)).append('/').append(repeat(' ', left.width - left.rootColumn - 1));
        }
        labelLine.append(label);
        edgeLine.append(repeat(' ', label.length()));
        if (right != null) {
            labelLine.append(repeat('_', right.rootColumn)).append(repeat(' ', right.width - right.rootColumn));
            edgeLine.append(repeat(' ', right.rootColumn)).append('\\').append(repeat(' ', right.width - right.rootColumn - 1));
        }
        lines.add(labelLine.toString());
        lines.add(edgeLine.toString());

        int leftWidth = left == null ? 0 : left.width;
        int rightWidth = right == null ? 0 : right.width;
        int rows = Math.max(left == null ? 0 : left.lines.size(), right == null ? 0 : right.lines.size());
        for (int row = 0; row < rows; row++) {
            lines.add(lineAt(left, row, leftWidth) + repeat(' ', label.length()) + lineAt(right, row, rightWidth));
        }
        return new Drawing(lines, leftWidth + label.length() + rightWidth, leftWidth + label.length() / 2);
    }

    private static String lineAt(Drawing drawing, int row, int width) {
        if (drawing != null && row < drawing.lines.size()) {
            return drawing.lines.get(row);
        }
        return repeat(' ', width);
    }

    private static String repeat(char character, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(character);
        }
        return builder.toString();
    }
}
