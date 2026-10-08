package sort.sample;

import sort.BubbleSort;
import sort.InsertionSort;
import sort.MergeSort;
import sort.QuickSort;
import sort.SelectionSort;
import sort.SortingVisualizer;

public class SortingVisualizerDemo {
    public static void main(String[] args) {
        Student[] students = {
            new Student("Alice", 22),
            new Student("Bob", 20),
            new Student("Charlie", 22),
            new Student("Alice", 21),
            new Student("Andrew", 21),
            new Student("Cal", 21),
            new Student("Alice", 23),
            new Student("Bob", 23),
            new Student("Cal", 23)
        };

        Book[] books = {
            new Book("The Great Gatsby", "F. Scott Fitzgerald", 1925),
            new Book("To Kill a Mockingbird", "Harper Lee", 1960),
            new Book("1984", "George Orwell", 1949),
            new Book("The Great Gatsby", "F. Scott Fitzgerald", 1924)
        };

        SortingVisualizer visualizer = new SortingVisualizer();

        visualizer.addAlgorithm(BubbleSort::new, "Neighbors trade places; the largest drifts to the end");
        visualizer.addAlgorithm(SelectionSort::new, "Scan for the smallest, then swap it into place");
        visualizer.addAlgorithm(InsertionSort::new, "Lift the key, shift the larger ships, set it down");
        visualizer.addAlgorithm(QuickSort::new, "Pick a pivot; smaller left, larger right; repeat each side");
        visualizer.addAlgorithm(MergeSort::new, "Split in halves until each ship sails alone, then merge");

        visualizer.addArray("by name, then age", students, Student::getName, s -> String.valueOf(s.getAge()));
        visualizer.addArray("by title, author, year", books, Book::getTitle, b -> String.valueOf(b.getYear()));
        visualizer.addRandomIntegers(12);

        visualizer.open();
    }
}
