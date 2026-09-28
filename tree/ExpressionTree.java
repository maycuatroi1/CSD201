package tree;

import java.util.function.DoubleBinaryOperator;

public final class ExpressionTree extends AbstractTree<String> {

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

        private static Operator fromSymbol(String symbol) {
            for (Operator operator : values()) {
                if (operator.symbol.equals(symbol)) {
                    return operator;
                }
            }
            throw new IllegalArgumentException("Unknown operator: " + symbol);
        }

        private double apply(double left, double right) {
            return function.applyAsDouble(left, right);
        }
    }

    private ExpressionTree(Node<String> root) {
        setRoot(root);
    }

    public static ExpressionTree number(int value) {
        return new ExpressionTree(new Node<>(String.valueOf(value)));
    }

    public static ExpressionTree operation(ExpressionTree left, Operator operator, ExpressionTree right) {
        Node<String> node = new Node<>(operator.symbol);
        node.setLeft(left.getRoot());
        node.setRight(right.getRoot());
        node.updateHeight();
        return new ExpressionTree(node);
    }

    public double evaluate() {
        return evaluate(getRoot());
    }

    public String toInfix() {
        return infix(getRoot());
    }

    private static double evaluate(Node<String> node) {
        if (node.isLeaf()) {
            return Double.parseDouble(node.getData());
        }
        double left = evaluate(node.getLeft());
        double right = evaluate(node.getRight());
        return Operator.fromSymbol(node.getData()).apply(left, right);
    }

    private static String infix(Node<String> node) {
        if (node.isLeaf()) {
            return node.getData();
        }
        return "(" + infix(node.getLeft()) + " " + node.getData() + " " + infix(node.getRight()) + ")";
    }
}
