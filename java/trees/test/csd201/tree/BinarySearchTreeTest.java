package csd201.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.TreeSet;
import org.junit.Test;

public class BinarySearchTreeTest {

    private final BinarySearchTree<Integer> tree = treeOf(8, 3, 10, 1, 6, 14, 4, 7, 13);

    @Test
    public void shapeFollowsInsertionOrder() {
        assertEquals("8(3(1,6(4,7)),10(-,14(13,-)))", tree.toString());
    }

    @Test
    public void traversals() {
        assertEquals(Arrays.asList(8, 3, 1, 6, 4, 7, 10, 14, 13), tree.preOrder());
        assertEquals(Arrays.asList(1, 3, 4, 6, 7, 8, 10, 13, 14), tree.inOrder());
        assertEquals(Arrays.asList(1, 4, 7, 6, 3, 13, 14, 10, 8), tree.postOrder());
        assertEquals(Arrays.asList(8, 3, 10, 1, 6, 14, 4, 7, 13), tree.levelOrder());
    }

    @Test
    public void sizeHeightMinMax() {
        assertEquals(9, tree.size());
        assertEquals(3, tree.height());
        assertEquals(Integer.valueOf(1), tree.min());
        assertEquals(Integer.valueOf(14), tree.max());
    }

    @Test
    public void searchFollowsOnePathFromTheRoot() {
        Position<Integer> found = tree.search(7);
        assertEquals(Integer.valueOf(7), found.getNode().getData());
        assertEquals(4, tree.getSearchCount());
        assertTrue(found.isRightChild());
        assertEquals(Integer.valueOf(6), found.getParent().getNode().getData());
    }

    @Test
    public void missingValueReturnsTheEmptyPositionWhereItWouldBeInserted() {
        Position<Integer> missing = tree.search(5);
        assertTrue(missing.isEmpty());
        assertEquals(4, tree.getSearchCount());
        assertTrue(missing.isRightChild());
        assertEquals(Integer.valueOf(4), missing.getParent().getNode().getData());
    }

    @Test
    public void duplicateIsNotInserted() {
        assertFalse(tree.insert(6));
        assertEquals(9, tree.size());
    }

    @Test
    public void newKeyBecomesALeaf() {
        BinarySearchTree<Integer> growing = treeOf(15, 4, 20, 17);
        assertTrue(growing.insert(19));
        assertEquals("15(4,20(17(-,19),-))", growing.toString());
        assertEquals(3, growing.height());
    }

    @Test
    public void deleteByMergingHangsTheRightSubtreeUnderThePredecessor() {
        BinarySearchTree<Integer> merged = treeOf(15, 10, 30, 5, 11, 20, 40, 12);
        assertTrue(merged.deleteByMerging(15));
        assertEquals("10(5,11(-,12(-,30(20,40))))", merged.toString());
        assertEquals(4, merged.height());
    }

    @Test
    public void deleteByCopyingReplacesTheKeyWithThePredecessor() {
        BinarySearchTree<Integer> copied = treeOf(15, 10, 30, 5, 11, 20, 40, 12);
        assertTrue(copied.deleteByCopying(15));
        assertEquals("12(10(5,11),30(20,40))", copied.toString());
        assertEquals(2, copied.height());
    }

    @Test
    public void removeDeletesByCopying() {
        BinarySearchTree<Integer> removed = treeOf(15, 10, 30, 5, 11, 20, 40, 12);
        assertTrue(removed.remove(15));
        assertEquals("12(10(5,11),30(20,40))", removed.toString());
    }

    @Test
    public void deleteLeafAndNodeWithOneChild() {
        BinarySearchTree<Integer> leaf = treeOf(15, 4, 20, 1, 16);
        leaf.deleteByMerging(16);
        assertEquals("15(4(1,-),20)", leaf.toString());
        assertEquals(2, leaf.height());

        BinarySearchTree<Integer> oneChild = treeOf(15, 4, 20, 1, 16);
        oneChild.deleteByCopying(20);
        assertEquals("15(4(1,-),16)", oneChild.toString());
    }

    @Test
    public void deletingAMissingKeyChangesNothing() {
        assertFalse(tree.deleteByMerging(5));
        assertFalse(tree.deleteByCopying(5));
        assertFalse(tree.remove(5));
        assertEquals(9, tree.size());
    }

    @Test
    public void deletingTheOnlyNodeEmptiesTheTree() {
        BinarySearchTree<Integer> single = treeOf(42);
        assertTrue(single.remove(42));
        assertTrue(single.isEmpty());
        assertEquals("-", single.toString());
    }

    @Test
    public void mergeTreesInsertsEveryElementOfTheOtherTree() {
        BinarySearchTree<Integer> other = treeOf(12, 2, 20);
        tree.mergeTrees(other);
        assertEquals(Arrays.asList(1, 2, 3, 4, 6, 7, 8, 10, 12, 13, 14, 20), tree.inOrder());
    }

    @Test
    public void balanceInsertsTheMiddleKeyFirst() {
        BinarySearchTree<Integer> unbalanced = treeOf(5, 1, 9, 8, 7, 0, 2, 3, 4, 6);
        unbalanced.balance();
        assertEquals("4(1(0,2(-,3)),7(5(-,6),8(-,9)))", unbalanced.toString());
        assertEquals(3, unbalanced.height());
    }

    @Test(expected = NoSuchElementException.class)
    public void emptyTreeHasNoMinimum() {
        new BinarySearchTree<Integer>().min();
    }

    @Test
    public void heightsStayCorrectUnderRandomInsertionsAndDeletions() {
        Random random = new Random(201);
        BinarySearchTree<Integer> subject = new BinarySearchTree<>();
        TreeSet<Integer> expected = new TreeSet<>();
        for (int step = 0; step < 3000; step++) {
            int key = random.nextInt(300);
            int action = random.nextInt(3);
            if (action == 0) {
                assertEquals(expected.add(key), subject.insert(key));
            } else if (action == 1) {
                assertEquals(expected.remove(key), subject.deleteByMerging(key));
            } else {
                assertEquals(expected.remove(key), subject.deleteByCopying(key));
            }
            assertEquals(recomputedHeight(subject.getRoot()), subject.height());
        }
        assertEquals(new ArrayList<>(expected), subject.inOrder());
    }

    @Test(timeout = 30_000)
    public void sortedInputMakesAChainWithoutStackOverflow() {
        BinarySearchTree<Integer> chain = new BinarySearchTree<>();
        int size = 20_000;
        for (int key = 1; key <= size; key++) {
            chain.insert(key);
        }
        assertEquals(size - 1, chain.height());
        assertEquals(size, chain.size());
        assertEquals(size, chain.inOrder().size());
        assertEquals(size, chain.preOrder().size());
        assertEquals(size, chain.postOrder().size());
        assertFalse(chain.search(size).isEmpty());
        assertEquals(size, chain.getSearchCount());
        assertTrue(chain.remove(1));
        assertEquals(size - 2, chain.height());
    }

    static int recomputedHeight(Node<Integer> node) {
        if (node == null) {
            return -1;
        }
        return 1 + Math.max(recomputedHeight(node.getLeft()), recomputedHeight(node.getRight()));
    }

    private static BinarySearchTree<Integer> treeOf(Integer... keys) {
        BinarySearchTree<Integer> binarySearchTree = new BinarySearchTree<>();
        binarySearchTree.buildTree(Arrays.asList(keys));
        return binarySearchTree;
    }
}
