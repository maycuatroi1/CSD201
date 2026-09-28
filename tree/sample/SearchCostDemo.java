package tree.sample;

import tree.AVLTree;
import tree.BinarySearchTree;
import tree.BinaryTree;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class SearchCostDemo {

    private static final int SIZE = 1000;
    private static final List<Integer> TARGETS = Arrays.asList(1, 500, 1000, 1001);

    private SearchCostDemo() {
    }

    public static void main(String[] args) {
        List<Integer> sorted = new ArrayList<>();
        for (int key = 1; key <= SIZE; key++) {
            sorted.add(key);
        }
        List<Integer> shuffled = new ArrayList<>(sorted);
        Collections.shuffle(shuffled, new Random(201));

        System.out.println("Comparisons needed to search for " + TARGETS + " (1001 is missing)");
        report("sorted", sorted);
        report("shuffled", shuffled);
    }

    private static void report(String order, List<Integer> keys) {
        List<BinaryTree<Integer>> trees = Arrays.asList(
                new BinaryTree<Integer>(), new BinarySearchTree<Integer>(), new AVLTree<Integer>());
        for (BinaryTree<Integer> tree : trees) {
            tree.buildTree(keys);
            StringBuilder line = new StringBuilder();
            for (int target : TARGETS) {
                tree.search(target);
                line.append(String.format("%6d", tree.getSearchCount()));
            }
            System.out.printf("%-9s %-17s height %4d  %s%n", order, tree.getClass().getSimpleName(), tree.height(), line);
        }
    }
}
