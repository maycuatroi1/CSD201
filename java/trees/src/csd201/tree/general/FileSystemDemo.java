package csd201.tree.general;

import csd201.tree.general.GeneralTree.Node;
import java.util.Collections;

public final class FileSystemDemo {

    private static final class FileEntry {
        private final String name;
        private final int sizeKb;

        private FileEntry(String name, int sizeKb) {
            this.name = name;
            this.sizeKb = sizeKb;
        }
    }

    private FileSystemDemo() {
    }

    public static void main(String[] args) {
        GeneralTree<FileEntry> tree = new GeneralTree<>();
        Node<FileEntry> cs16 = tree.addRoot(directory("cs16/"));
        Node<FileEntry> homeworks = tree.addChild(cs16, directory("homeworks/"));
        tree.addChild(homeworks, file("h1c.doc", 3));
        tree.addChild(homeworks, file("h1nc.doc", 2));
        Node<FileEntry> programs = tree.addChild(cs16, directory("programs/"));
        tree.addChild(programs, file("DDR.java", 10));
        Node<FileEntry> stocks = tree.addChild(programs, file("Stocks.java", 25));
        tree.addChild(programs, file("Robot.java", 20));
        Node<FileEntry> todo = tree.addChild(cs16, file("todo.txt", 1));

        System.out.println("Pre-order: directory listing");
        printListing(tree, tree.root());

        System.out.println();
        System.out.println("Post-order: disk usage");
        diskUsage(tree, tree.root());

        System.out.println();
        System.out.println("size = " + tree.size() + ", height = " + tree.height());
        System.out.println("depth(Stocks.java) = " + tree.depth(stocks));
        System.out.println("depth(todo.txt) = " + tree.depth(todo) + ", isLeaf(todo.txt) = " + tree.isLeaf(todo));
        System.out.println("height(homeworks/) = " + tree.height(homeworks));
    }

    private static void printListing(GeneralTree<FileEntry> tree, Node<FileEntry> node) {
        System.out.println(indent(tree.depth(node)) + node.element().name);
        for (Node<FileEntry> child : tree.children(node)) {
            printListing(tree, child);
        }
    }

    private static int diskUsage(GeneralTree<FileEntry> tree, Node<FileEntry> node) {
        int total = node.element().sizeKb;
        for (Node<FileEntry> child : tree.children(node)) {
            total += diskUsage(tree, child);
        }
        System.out.printf("%4dK  %s%n", total, node.element().name);
        return total;
    }

    private static String indent(int depth) {
        return String.join("", Collections.nCopies(depth, "  "));
    }

    private static FileEntry directory(String name) {
        return new FileEntry(name, 0);
    }

    private static FileEntry file(String name, int sizeKb) {
        return new FileEntry(name, sizeKb);
    }
}
