package tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class TreeTest {

    private static final List<Integer> KEYS = Arrays.asList(8, 3, 10, 1, 6);

    @Test
    public void everyBinaryTreeSharesTheTreeOperations() {
        assertSameShape(build(new BinaryTree<Integer>()));
        assertSameShape(build(new BinarySearchTree<Integer>()));
        assertSameShape(build(new AVLTree<Integer>()));
    }

    @Test
    public void everyEmptyTreeHasSizeZeroAndHeightMinusOne() {
        assertEmpty(new BinaryTree<Integer>());
        assertEmpty(new BinarySearchTree<Integer>());
        assertEmpty(new AVLTree<Integer>());
    }

    @Test
    public void printTreeDrawsTopDownWithSlashes() {
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        tree.buildTree(Arrays.asList(8, 3, 10));
        String newLine = System.lineSeparator();
        assertEquals(" 8_" + newLine + "/  \\" + newLine + "3 10" + newLine, tree.render());
    }

    @Test
    public void printTreeOfAnEmptyTree() {
        assertEquals("(empty tree)" + System.lineSeparator(), new BinaryTree<Integer>().render());
    }

    private static Tree<Integer> build(BinaryTree<Integer> tree) {
        tree.buildTree(KEYS);
        return tree;
    }

    private static void assertSameShape(Tree<Integer> tree) {
        assertEquals("8(3(1,6),10)", tree.toString());
        assertEquals(5, tree.size());
        assertEquals(2, tree.height());
        assertEquals(Arrays.asList(8, 3, 1, 6, 10), tree.preOrder());
        assertEquals(Arrays.asList(1, 3, 6, 8, 10), tree.inOrder());
        assertEquals(Arrays.asList(1, 6, 3, 10, 8), tree.postOrder());
        assertEquals(Arrays.asList(8, 3, 10, 1, 6), tree.levelOrder());
    }

    private static void assertEmpty(Tree<Integer> tree) {
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertEquals(-1, tree.height());
        assertEquals("-", tree.toString());
        assertEquals(Collections.emptyList(), tree.preOrder());
        assertEquals(Collections.emptyList(), tree.inOrder());
        assertEquals(Collections.emptyList(), tree.postOrder());
        assertEquals(Collections.emptyList(), tree.levelOrder());
        assertTrue(tree.search(1).isEmpty());
    }
}
