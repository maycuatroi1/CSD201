package csd201.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class BinaryTreeTest {

    private final BinaryTree<Integer> tree = treeOf(1, 2, 3, 4, 5, 6, 7);

    @Test
    public void buildTreeFillsLevelByLevelFromLeftToRight() {
        assertEquals("1(2(4,5),3(6,7))", tree.toString());
        assertEquals(2, tree.height());
    }

    @Test
    public void buildTreeReplacesThePreviousContent() {
        tree.buildTree(Arrays.asList(9, 8));
        assertEquals("9(8,-)", tree.toString());
    }

    @Test
    public void insertTakesTheFirstFreeSlotInLevelOrder() {
        BinaryTree<Integer> growing = treeOf(1, 2, 3);
        assertTrue(growing.insert(4));
        assertTrue(growing.insert(5));
        assertEquals("1(2(4,5),3)", growing.toString());
        assertEquals(2, growing.height());
    }

    @Test
    public void searchVisitsNodesInPreOrderUntilFound() {
        Position<Integer> position = tree.search(3);
        assertEquals(Integer.valueOf(3), position.getNode().getData());
        assertEquals(5, tree.getSearchCount());
        assertTrue(position.getParent().isRoot());
        assertTrue(position.isRightChild());
    }

    @Test
    public void searchForAMissingValueVisitsEveryNode() {
        assertTrue(tree.search(42).isEmpty());
        assertEquals(7, tree.getSearchCount());
    }

    @Test
    public void removeReplacesTheValueWithTheDeepestNode() {
        assertTrue(tree.remove(2));
        assertEquals("1(7(4,5),3(6,-))", tree.toString());
        assertEquals(6, tree.size());
        assertEquals(2, tree.height());
    }

    @Test
    public void removeTheDeepestNodeItself() {
        assertTrue(tree.remove(7));
        assertEquals("1(2(4,5),3(6,-))", tree.toString());
    }

    @Test
    public void removeTheOnlyNode() {
        BinaryTree<Integer> single = treeOf(1);
        assertTrue(single.remove(1));
        assertTrue(single.isEmpty());
        assertEquals(-1, single.height());
    }

    @Test
    public void removeAMissingValueChangesNothing() {
        assertFalse(tree.remove(42));
        assertEquals(7, tree.size());
    }

    @Test
    public void siblingIsTheOtherChildOfTheParent() {
        assertEquals(Integer.valueOf(5), tree.getSibling(tree.search(4)).getNode().getData());
        assertEquals(Integer.valueOf(2), tree.getSibling(tree.search(3)).getNode().getData());
        assertTrue(tree.getSibling(tree.search(1)).isEmpty());
        tree.remove(7);
        assertTrue(tree.getSibling(tree.search(6)).isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void nullElementsAreRejected() {
        tree.insert(null);
    }

    @Test(timeout = 5000)
    public void buildTreeIsLinear() {
        List<Integer> elements = new ArrayList<>();
        for (int i = 0; i < 200_000; i++) {
            elements.add(i);
        }
        BinaryTree<Integer> large = new BinaryTree<>();
        large.buildTree(elements);
        assertEquals(200_000, large.size());
        assertEquals(17, large.height());
    }

    private static BinaryTree<Integer> treeOf(Integer... elements) {
        BinaryTree<Integer> binaryTree = new BinaryTree<>();
        binaryTree.buildTree(Arrays.asList(elements));
        return binaryTree;
    }
}
