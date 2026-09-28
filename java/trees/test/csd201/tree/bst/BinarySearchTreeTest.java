package csd201.tree.bst;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        assertEquals(Arrays.asList(8, 3, 10, 1, 6, 14, 4, 7, 13), tree.breadthFirst());
    }

    @Test
    public void sizeHeightMinMax() {
        assertEquals(9, tree.size());
        assertEquals(3, tree.height());
        assertEquals(Integer.valueOf(1), tree.min());
        assertEquals(Integer.valueOf(14), tree.max());
    }

    @Test
    public void search() {
        assertTrue(tree.contains(7));
        assertTrue(tree.contains(13));
        assertFalse(tree.contains(5));
    }

    @Test
    public void duplicateKeyIsNotInserted() {
        assertFalse(tree.insert(6));
        assertEquals(9, tree.size());
    }

    @Test
    public void newKeyBecomesALeaf() {
        BinarySearchTree<Integer> growing = treeOf(15, 4, 20, 17);
        assertTrue(growing.insert(19));
        assertEquals("15(4,20(17(-,19),-))", growing.toString());
    }

    @Test
    public void sameKeysInDifferentOrderGiveDifferentShapes() {
        assertEquals("K(B(-,D),P(M,R))", treeOf('K', 'B', 'P', 'D', 'M', 'R').toString());
        assertEquals("B(-,K(D,P(M,R)))", treeOf('B', 'K', 'D', 'P', 'M', 'R').toString());
        assertEquals(5, treeOf('B', 'D', 'K', 'M', 'P', 'R').height());
    }

    @Test
    public void deleteLeafAndNodeWithOneChild() {
        BinarySearchTree<Integer> leaf = treeOf(15, 4, 20, 1, 16);
        leaf.deleteByMerging(16);
        assertEquals("15(4(1,-),20)", leaf.toString());

        BinarySearchTree<Integer> oneChild = treeOf(15, 4, 20, 1, 16);
        oneChild.deleteByCopying(20);
        assertEquals("15(4(1,-),16)", oneChild.toString());
    }

    @Test
    public void deleteByMergingHangsRightSubtreeUnderPredecessor() {
        BinarySearchTree<Integer> merged = treeOf(15, 10, 30, 5, 11, 20, 40, 12);
        assertTrue(merged.deleteByMerging(15));
        assertEquals("10(5,11(-,12(-,30(20,40))))", merged.toString());
        assertEquals(4, merged.height());
    }

    @Test
    public void deleteByCopyingReplacesKeyWithPredecessor() {
        BinarySearchTree<Integer> copied = treeOf(15, 10, 30, 5, 11, 20, 40, 12);
        assertTrue(copied.deleteByCopying(15));
        assertEquals("12(10(5,11),30(20,40))", copied.toString());
        assertEquals(2, copied.height());
    }

    @Test
    public void bothDeletionsAgreeWhenPredecessorIsTheLeftChild() {
        BinarySearchTree<Integer> merged = treeOf(15, 10, 30, 5, 20, 40, 4, 7);
        BinarySearchTree<Integer> copied = treeOf(15, 10, 30, 5, 20, 40, 4, 7);
        merged.deleteByMerging(15);
        copied.deleteByCopying(15);
        assertEquals("10(5(4,7),30(20,40))", merged.toString());
        assertEquals(merged.toString(), copied.toString());
    }

    @Test
    public void deletingAMissingKeyChangesNothing() {
        assertFalse(tree.deleteByMerging(5));
        assertFalse(tree.deleteByCopying(5));
        assertEquals(9, tree.size());
    }

    @Test
    public void deletingTheOnlyNodeEmptiesTheTree() {
        BinarySearchTree<Integer> single = treeOf(42);
        single.deleteByCopying(42);
        assertTrue(single.isEmpty());
        assertEquals("-", single.toString());
    }

    @Test
    public void balanceInsertsTheMiddleKeyFirst() {
        BinarySearchTree<Integer> unbalanced = treeOf(5, 1, 9, 8, 7, 0, 2, 3, 4, 6);
        unbalanced.balance();
        assertEquals("4(1(0,2(-,3)),7(5(-,6),8(-,9)))", unbalanced.toString());
        assertEquals(3, unbalanced.height());
    }

    @Test
    public void emptyTree() {
        BinarySearchTree<Integer> empty = new BinarySearchTree<>();
        assertEquals(0, empty.size());
        assertEquals(-1, empty.height());
        assertEquals(Collections.emptyList(), empty.breadthFirst());
        assertFalse(empty.contains(1));
    }

    @Test(expected = NoSuchElementException.class)
    public void emptyTreeHasNoMinimum() {
        new BinarySearchTree<Integer>().min();
    }

    @Test(expected = NullPointerException.class)
    public void nullKeyIsRejected() {
        new BinarySearchTree<Integer>().insert(null);
    }

    @Test
    public void behavesLikeASortedSet() {
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
        }
        assertEquals(new ArrayList<>(expected), subject.inOrder());
    }

    private static BinarySearchTree<Integer> treeOf(Integer... keys) {
        return BinarySearchTree.of(Arrays.asList(keys));
    }

    private static BinarySearchTree<Character> treeOf(Character... keys) {
        return BinarySearchTree.of(Arrays.asList(keys));
    }
}
