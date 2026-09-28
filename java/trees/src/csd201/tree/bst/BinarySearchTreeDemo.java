package csd201.tree.bst;

import java.util.Arrays;
import java.util.List;

public final class BinarySearchTreeDemo {

    private BinarySearchTreeDemo() {
    }

    public static void main(String[] args) {
        section("Insert 8 3 10 1 6 14 4 7 13");
        BinarySearchTree<Integer> tree = BinarySearchTree.of(Arrays.asList(8, 3, 10, 1, 6, 14, 4, 7, 13));
        print("shape", tree);
        print("pre-order", tree.preOrder());
        print("in-order", tree.inOrder());
        print("post-order", tree.postOrder());
        print("breadth-first", tree.breadthFirst());
        print("size", tree.size());
        print("height", tree.height());
        print("min", tree.min());
        print("max", tree.max());

        section("Search");
        print("contains(7)", tree.contains(7));
        print("contains(5)", tree.contains(5));

        section("Insert 15 4 20 17 19, then 17 again");
        BinarySearchTree<Integer> growing = new BinarySearchTree<>();
        for (int key : Arrays.asList(15, 4, 20, 17, 19, 17)) {
            boolean inserted = growing.insert(key);
            print("insert(" + key + ")", inserted + "  " + growing);
        }

        section("Delete 15: merging vs copying");
        List<Integer> keys = Arrays.asList(15, 10, 30, 5, 11, 20, 40, 12);
        BinarySearchTree<Integer> merged = BinarySearchTree.of(keys);
        BinarySearchTree<Integer> copied = BinarySearchTree.of(keys);
        print("before", merged + "  height " + merged.height());
        merged.deleteByMerging(15);
        copied.deleteByCopying(15);
        print("by merging", merged + "  height " + merged.height());
        print("by copying", copied + "  height " + copied.height());

        section("Balance 5 1 9 8 7 0 2 3 4 6");
        BinarySearchTree<Integer> unbalanced = BinarySearchTree.of(Arrays.asList(5, 1, 9, 8, 7, 0, 2, 3, 4, 6));
        print("before", unbalanced + "  height " + unbalanced.height());
        unbalanced.balance();
        print("after", unbalanced + "  height " + unbalanced.height());
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("== " + title);
    }

    private static void print(String label, Object value) {
        System.out.printf("%-14s %s%n", label, value);
    }
}
