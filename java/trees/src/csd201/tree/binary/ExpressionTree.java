package csd201.tree.binary;

import java.util.function.DoubleBinaryOperator;

public final class ExpressionTree extends BinaryTree<String> {

    public enum Operator {
        ADD("+", (left, right) -> left + right),
        SUBTRACT("-", (left, right) -> left - right),
        MULTIPLY("*", (left, right) -> left * right),
        DIVIDE("/", (left, right) -> left / right);

        private final String symbol;
        private final DoubleBinaryOperator function;

        Operator(String symbol, DoubleBinaryOperator function) {
            this.symbol = symbol;
            this.function = function;
        }

        private double apply(double left, double right) {
            return function.applyAsDouble(left, right);
        }
    }

    private static final class Node implements BinaryNode<String> {
        private final String element;
        private final Operator operator;
        private final Node left;
        private final Node right;

        private Node(String element, Operator operator, Node left, Node right) {
            this.element = element;
            this.operator = operator;
            this.left = left;
            this.right = right;
        }

        public String element() {
            return element;
        }

        public Node left() {
            return left;
        }

        public Node right() {
            return right;
        }
    }

    private final Node root;

    private ExpressionTree(Node root) {
        this.root = root;
    }

    public static ExpressionTree number(int value) {
        return new ExpressionTree(new Node(String.valueOf(value), null, null, null));
    }

    public static ExpressionTree operation(ExpressionTree left, Operator operator, ExpressionTree right) {
        return new ExpressionTree(new Node(operator.symbol, operator, left.root, right.root));
    }

    public double evaluate() {
        return evaluate(root);
    }

    public String toInfix() {
        return infix(root);
    }

    protected BinaryNode<String> root() {
        return root;
    }

    private static double evaluate(Node node) {
        if (node.isLeaf()) {
            return Double.parseDouble(node.element);
        }
        double left = evaluate(node.left);
        double right = evaluate(node.right);
        return node.operator.apply(left, right);
    }

    private static String infix(Node node) {
        if (node.isLeaf()) {
            return node.element;
        }
        return "(" + infix(node.left) + " " + node.element + " " + infix(node.right) + ")";
    }
}
