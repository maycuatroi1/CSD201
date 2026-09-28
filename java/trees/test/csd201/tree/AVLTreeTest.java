package csd201.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.Test;

public class AVLTreeTest {

    @Test
    public void balanceFactorIsRightHeightMinusLeftHeight() {
        AVLTree<Integer> tree = treeOf(44, 17, 78, 32, 50, 88, 48, 62);
        assertEquals("44(17(-,32),78(50(48,62),88))", tree.toString());
        assertEquals("44(1) 17(1) 32(0) 78(-1) 50(0) 48(0) 62(0) 88(0)", balanceFactors(tree));
    }

    @Test
    public void singleRotations() {
        assertEquals("20(10,30)", treeOf(30, 20, 10).toString());
        assertEquals("20(10,30)", treeOf(10, 20, 30).toString());
    }

    @Test
    public void doubleRotationWhenSignsDiffer() {
        AVLTree<Integer> tree = treeOf(44, 17, 78, 32, 50, 88, 48, 62);
        assertTrue(tree.insert(54));
        assertEquals("44(17(-,32),62(50(48,54),78(-,88)))", tree.toString());
        assertEquals("44(1) 17(1) 32(0) 62(0) 50(0) 48(0) 54(0) 78(1) 88(0)", balanceFactors(tree));
    }

    @Test
    public void removalRebalancesOnTheWayUp() {
        AVLTree<Integer> tree = treeOf(44, 17, 62, 32, 50, 78, 48, 54, 88);
        assertTrue(tree.remove(32));
        assertEquals("62(44(17,50(48,54)),78(-,88))", tree.toString());
        assertEquals("62(-1) 44(1) 17(0) 50(0) 48(0) 54(0) 78(1) 88(0)", balanceFactors(tree));
    }

    @Test
    public void bothDeletionMethodsKeepTheTreeBalanced() {
        AVLTree<Integer> merged = treeOf(20, 10, 30, 5, 15, 25, 35, 1);
        AVLTree<Integer> copied = treeOf(20, 10, 30, 5, 15, 25, 35, 1);
        assertTrue(merged.deleteByMerging(20));
        assertTrue(copied.deleteByCopying(20));
        assertEquals("15(5(1,10),30(25,35))", merged.toString());
        assertEquals(merged.toString(), copied.toString());
    }

    @Test
    public void mixedRotationsFromTheExercise() {
        AVLTree<Integer> tree = treeOf(30, 20, 10, 25, 28, 27, 5, 4);
        assertEquals("25(10(5(4,-),20),28(27,30))", tree.toString());
        assertEquals(3, tree.height());
    }

    @Test
    public void sortedInputStaysLogarithmic() {
        assertEquals("3(1(0,2),7(5(4,6),8(-,9)))", treeOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9).toString());
    }

    @Test
    public void searchComparesAtMostHeightPlusOneNodes() {
        AVLTree<Integer> tree = treeOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertFalse(tree.search(9).isEmpty());
        assertEquals(tree.height() + 1, tree.getSearchCount());
    }

    @Test
    public void duplicateAndMissingKeysChangeNothing() {
        AVLTree<Integer> tree = treeOf(2, 1, 3);
        assertFalse(tree.insert(2));
        assertFalse(tree.remove(9));
        assertEquals("2(1,3)", tree.toString());
    }

    @Test(expected = NoSuchElementException.class)
    public void balanceFactorOfAMissingKey() {
        treeOf(1, 2, 3).balanceFactor(9);
    }

    @Test
    public void staysBalancedUnderRandomInsertionsAndRemovals() {
        Random random = new Random(201);
        AVLTree<Integer> tree = new AVLTree<>();
        TreeSet<Integer> expected = new TreeSet<>();
        for (int step = 0; step < 3000; step++) {
            int key = random.nextInt(300);
            if (random.nextBoolean()) {
                assertEquals(expected.add(key), tree.insert(key));
            } else {
                assertEquals(expected.remove(key), tree.remove(key));
            }
            assertBalanced(tree);
            assertEquals(BinarySearchTreeTest.recomputedHeight(tree.getRoot()), tree.height());
        }
        assertEquals(new ArrayList<>(expected), tree.inOrder());
    }

    @Test(timeout = 5000)
    public void insertionIsLogarithmic() {
        int size = 100_000;
        AVLTree<Integer> tree = new AVLTree<>();
        for (int key = 0; key < size; key++) {
            tree.insert(key);
        }
        double log2 = Math.log(size + 2) / Math.log(2);
        assertEquals(size, tree.size());
        assertTrue("height " + tree.height(), tree.height() <= 1.44 * log2);
    }

    private static void assertBalanced(AVLTree<Integer> tree) {
        for (int key : tree.preOrder()) {
            int balance = tree.balanceFactor(key);
            assertTrue("bf(" + key + ") = " + balance, balance >= -1 && balance <= 1);
        }
    }

    private static String balanceFactors(AVLTree<Integer> tree) {
        return tree.preOrder().stream()
                .map(key -> key + "(" + tree.balanceFactor(key) + ")")
                .collect(Collectors.joining(" "));
    }

    private static AVLTree<Integer> treeOf(Integer... keys) {
        AVLTree<Integer> avlTree = new AVLTree<>();
        avlTree.buildTree(Arrays.asList(keys));
        return avlTree;
    }
}
