package csd201.tree.sample;

import csd201.tree.AVLTree;
import csd201.tree.BinarySearchTree;
import java.util.Arrays;
import java.util.stream.Collectors;

public final class AVLTreeDemo {

    private AVLTreeDemo() {
    }

    public static void main(String[] args) {
        section("Insert 54: double rotation");
        AVLTree<Integer> tree = new AVLTree<>();
        tree.buildTree(Arrays.asList(44, 17, 78, 32, 50, 88, 48, 62));
        show("before", tree);
        tree.insert(54);
        show("after", tree);

        section("Delete 32: single rotation");
        tree.remove(32);
        show("after", tree);

        section("Insert 0 1 2 3 4 5 6 7 8 9 in order");
        AVLTree<Integer> avl = new AVLTree<>();
        BinarySearchTree<Integer> bst = new BinarySearchTree<>();
        for (int key = 0; key <= 9; key++) {
            avl.insert(key);
            bst.insert(key);
        }
        show("AVL", avl);
        System.out.printf("%-7s %s  height %d%n", "BST", bst, bst.height());
        avl.printTree();
    }

    private static void show(String label, AVLTree<Integer> tree) {
        System.out.printf("%-7s %s  height %d%n", label, tree, tree.height());
        System.out.printf("%-7s %s%n", "", balanceFactors(tree));
    }

    private static String balanceFactors(AVLTree<Integer> tree) {
        return tree.preOrder().stream()
                .map(key -> key + "(" + tree.balanceFactor(key) + ")")
                .collect(Collectors.joining(" "));
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("== " + title);
    }
}
