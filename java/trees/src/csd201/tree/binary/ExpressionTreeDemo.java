package csd201.tree.binary;

import static csd201.tree.binary.ExpressionTree.Operator.ADD;
import static csd201.tree.binary.ExpressionTree.Operator.DIVIDE;
import static csd201.tree.binary.ExpressionTree.Operator.MULTIPLY;
import static csd201.tree.binary.ExpressionTree.Operator.SUBTRACT;
import static csd201.tree.binary.ExpressionTree.number;
import static csd201.tree.binary.ExpressionTree.operation;

import java.util.List;

public final class ExpressionTreeDemo {

    private ExpressionTreeDemo() {
    }

    public static void main(String[] args) {
        ExpressionTree quotient = operation(
                operation(operation(number(3), ADD, number(1)), MULTIPLY, number(3)),
                DIVIDE,
                operation(operation(number(9), SUBTRACT, number(5)), ADD, number(2)));
        ExpressionTree sum = operation(
                operation(number(3), MULTIPLY, operation(number(7), SUBTRACT, number(4))),
                ADD,
                number(6));
        ExpressionTree expression = operation(quotient, SUBTRACT, sum);

        print("infix", expression.toInfix());
        print("pre-order", join(expression.preOrder()));
        print("in-order", join(expression.inOrder()));
        print("post-order", join(expression.postOrder()));
        print("breadth-first", join(expression.breadthFirst()));
        print("value", String.valueOf(expression.evaluate()));
        print("size", String.valueOf(expression.size()));
        print("height", String.valueOf(expression.height()));
    }

    private static String join(List<String> tokens) {
        return String.join(" ", tokens);
    }

    private static void print(String label, String value) {
        System.out.printf("%-14s %s%n", label, value);
    }
}
