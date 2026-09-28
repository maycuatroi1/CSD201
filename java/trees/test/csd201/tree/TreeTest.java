package csd201.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import csd201.tree.avl.AvlTree;
import csd201.tree.bst.BinarySearchTree;
import csd201.tree.general.GeneralTree;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

public class TreeTest {

    @Test
    public void generalAndBinaryTreesShareTheTreeOperations() {
        GeneralTree<Integer> general = new GeneralTree<>();
        GeneralTree.Node<Integer> root = general.addRoot(8);
        GeneralTree.Node<Integer> three = general.addChild(root, 3);
        general.addChild(root, 10);
        general.addChild(three, 1);
        general.addChild(three, 6);

        assertSameShape(general);
        assertSameShape(BinarySearchTree.of(Arrays.asList(8, 3, 10, 1, 6)));
        assertSameShape(AvlTree.of(Arrays.asList(8, 3, 10, 1, 6)));
    }

    @Test
    public void everyEmptyTreeHasSizeZeroAndHeightMinusOne() {
        assertEmpty(new GeneralTree<Integer>());
        assertEmpty(new BinarySearchTree<Integer>());
        assertEmpty(new AvlTree<Integer>());
    }

    private static void assertSameShape(Tree<Integer> tree) {
        assertEquals(5, tree.size());
        assertEquals(2, tree.height());
        assertEquals(Arrays.asList(8, 3, 1, 6, 10), tree.preOrder());
        assertEquals(Arrays.asList(1, 6, 3, 10, 8), tree.postOrder());
        assertEquals(Arrays.asList(8, 3, 10, 1, 6), tree.breadthFirst());
    }

    private static void assertEmpty(Tree<Integer> tree) {
        assertTrue(tree.isEmpty());
        assertEquals(0, tree.size());
        assertEquals(-1, tree.height());
        assertEquals(Collections.emptyList(), tree.preOrder());
        assertEquals(Collections.emptyList(), tree.postOrder());
        assertEquals(Collections.emptyList(), tree.breadthFirst());
    }
}
