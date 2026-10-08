package sort.sample;

import sort.BubbleSort;
import sort.InsertionSort;
import sort.Sorter;

public class SortingDemo {
    public static void main(String[] args) {
        Student[] arr = {
            new Student("Alice", 22),
            new Student("Bob", 20),
            new Student("Charlie", 22),
            new Student("Alice", 21)
        };

        Book[] books = {
            new Book("The Great Gatsby", "F. Scott Fitzgerald", 1925),
            new Book("To Kill a Mockingbird", "Harper Lee", 1960),
            new Book("1984", "George Orwell", 1949),
            new Book("The Great Gatsby", "F. Scott Fitzgerald", 1924)
        };

        Sorter<Student> sorter = new BubbleSort<>();
        sorter.sort(arr);

        Sorter<Book> bookSorter = new InsertionSort<>();
        bookSorter.sort(books);

        System.out.println("Sorted array:");
        for (Student s : arr) {
            System.out.print(s.getName() + " (" + s.getAge() + ") ");
        }
        System.out.println("\nCompares: " + sorter.getCompareCount() + ", swaps: " + sorter.getSwapCount()
                + ", moves: " + sorter.getMoveCount());

        System.out.println("Sorted books:");
        for (Book b : books) {
            System.out.print(b.getTitle() + " by " + b.getAuthor() + " (" + b.getYear() + ") ");
        }
        System.out.println("\nCompares: " + bookSorter.getCompareCount() + ", swaps: " + bookSorter.getSwapCount()
                + ", moves: " + bookSorter.getMoveCount());
    }
}
