package tree;

import static tree.ExpressionTree.Operator.ADD;
import static tree.ExpressionTree.Operator.DIVIDE;
import static tree.ExpressionTree.Operator.MULTIPLY;
import static tree.ExpressionTree.Operator.SUBTRACT;
import static tree.ExpressionTree.number;
import static tree.ExpressionTree.operation;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class ExpressionTreeTest {

    private final ExpressionTree expression = operation(
            operation(
                    operation(operation(number(3), ADD, number(1)), MULTIPLY, number(3)),
                    DIVIDE,
                    operation(operation(number(9), SUBTRACT, number(5)), ADD, number(2))),
            SUBTRACT,
            operation(
                    operation(number(3), MULTIPLY, operation(number(7), SUBTRACT, number(4))),
                    ADD,
                    number(6)));

    @Test
    public void evaluatesInPostOrder() {
        assertEquals(-13.0, expression.evaluate(), 1e-9);
    }

    @Test
    public void infixAddsParenthesesAroundEveryOperation() {
        assertEquals("((((3 + 1) * 3) / ((9 - 5) + 2)) - ((3 * (7 - 4)) + 6))", expression.toInfix());
    }

    @Test
    public void preOrderGivesPrefixNotation() {
        assertEquals(
                Arrays.asList("-", "/", "*", "+", "3", "1", "3", "+", "-", "9", "5", "2",
                        "+", "*", "3", "-", "7", "4", "6"),
                expression.preOrder());
    }

    @Test
    public void postOrderGivesPostfixNotation() {
        assertEquals(
                Arrays.asList("3", "1", "+", "3", "*", "9", "5", "-", "2", "+", "/",
                        "3", "7", "4", "-", "*", "6", "+", "-"),
                expression.postOrder());
    }

    @Test
    public void inOrderWithoutParenthesesLosesThePrecedence() {
        assertEquals(
                Arrays.asList("3", "+", "1", "*", "3", "/", "9", "-", "5", "+", "2",
                        "-", "3", "*", "7", "-", "4", "+", "6"),
                expression.inOrder());
    }

    @Test
    public void levelOrderVisitsLevelByLevel() {
        assertEquals(
                Arrays.asList("-", "/", "+", "*", "+", "*", "6", "+", "3", "-", "2",
                        "3", "-", "3", "1", "9", "5", "7", "4"),
                expression.levelOrder());
    }

    @Test
    public void sizeAndHeight() {
        assertEquals(19, expression.size());
        assertEquals(4, expression.height());
    }

    @Test
    public void singleNumberIsALeaf() {
        ExpressionTree seven = number(7);
        assertEquals(7.0, seven.evaluate(), 1e-9);
        assertEquals("7", seven.toInfix());
        assertEquals(0, seven.height());
    }
}
