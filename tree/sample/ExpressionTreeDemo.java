package tree.sample;

import static tree.ExpressionTree.Operator.ADD;
import static tree.ExpressionTree.Operator.DIVIDE;
import static tree.ExpressionTree.Operator.MULTIPLY;
import static tree.ExpressionTree.Operator.SUBTRACT;
import static tree.ExpressionTree.number;
import static tree.ExpressionTree.operation;

import tree.ExpressionTree;
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

        expression.printTree();
        print("infix", expression.toInfix());
        print("pre-order", join(expression.preOrder()));
        print("in-order", join(expression.inOrder()));
        print("post-order", join(expression.postOrder()));
        print("level-order", join(expression.levelOrder()));
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
