package csd201.tree.sample;

import csd201.tree.AVLTree;
import csd201.tree.BinarySearchTree;
import csd201.tree.BinaryTree;
import csd201.tree.Position;
import java.util.Arrays;
import java.util.List;

public final class StudentTreeDemo {

    private StudentTreeDemo() {
    }

    public static void main(String[] args) {
        List<Student> students = Arrays.asList(
                new Student("Alice", 1),
                new Student("Bob", 2),
                new Student("Charlie", 34),
                new Student("An", 43),
                new Student("Tuan", 25),
                new Student("Linh", 199),
                new Student("Minh", 42));

        show("BinaryTree", new BinaryTree<>(), students);
        show("BinarySearchTree", new BinarySearchTree<>(), students);
        show("AVLTree", new AVLTree<>(), students);
    }

    private static void show(String title, BinaryTree<Student> tree, List<Student> students) {
        System.out.println("== " + title);
        tree.buildTree(students);
        tree.printTree();
        System.out.println("pre-order    " + tree.preOrder());
        System.out.println("in-order     " + tree.inOrder());
        System.out.println("post-order   " + tree.postOrder());
        System.out.println("level-order  " + tree.levelOrder());
        System.out.println("size " + tree.size() + ", height " + tree.height());

        Position<Student> position = tree.search(Student.withId(42));
        if (position.isEmpty()) {
            System.out.println("search 42: not found after " + tree.getSearchCount() + " comparisons");
        } else {
            System.out.println("search 42: found " + position.getNode().getData()
                    + " after " + tree.getSearchCount() + " comparisons");
        }

        tree.remove(Student.withId(43));
        System.out.println("after remove 43:");
        tree.printTree();
        System.out.println();
    }
}
