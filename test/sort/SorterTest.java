package sort;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import sort.sample.Student;

public class SorterTest {

    @Test
    public void everySorterSortsRandomArraysWithRepeatedValues() {
        Random random = new Random(201);
        for (int length = 0; length <= 20; length++) {
            Integer[] data = new Integer[length];
            for (int i = 0; i < length; i++) {
                data[i] = random.nextInt(8);
            }
            Integer[] expected = data.clone();
            Arrays.sort(expected);
            for (Sorter<Integer> sorter : everySorter()) {
                Integer[] actual = data.clone();
                sorter.sort(actual);
                assertArrayEquals(sorter.getClass().getSimpleName(), expected, actual);
            }
        }
    }

    @Test
    public void everySorterSortsSortedAndReversedArrays() {
        Integer[] sorted = {1, 2, 3, 4, 5, 6, 7};
        Integer[] reversed = {7, 6, 5, 4, 3, 2, 1};
        for (Sorter<Integer> sorter : everySorter()) {
            Integer[] actual = reversed.clone();
            sorter.sort(actual);
            assertArrayEquals(sorter.getClass().getSimpleName(), sorted, actual);
        }
        for (Sorter<Integer> sorter : everySorter()) {
            Integer[] actual = sorted.clone();
            sorter.sort(actual);
            assertArrayEquals(sorter.getClass().getSimpleName(), sorted, actual);
        }
    }

    @Test
    public void studentsAreSortedByNameThenAge() {
        for (Sorter<Student> sorter : Arrays.<Sorter<Student>>asList(new BubbleSort<Student>(),
                new SelectionSort<Student>(), new InsertionSort<Student>(), new QuickSort<Student>(),
                new MergeSort<Student>())) {
            Student[] students = {new Student("Alice", 22), new Student("Bob", 20), new Student("Charlie", 22),
                new Student("Alice", 21)};
            sorter.sort(students);
            StringBuilder order = new StringBuilder();
            for (Student s : students) {
                order.append(s.getName()).append(s.getAge()).append(' ');
            }
            assertEquals("Alice21 Alice22 Bob20 Charlie22 ", order.toString());
        }
    }

    @Test
    public void bubbleInsertionAndMergeSortKeepEqualElementsInOrder() {
        for (Sorter<Card> sorter : Arrays.<Sorter<Card>>asList(new BubbleSort<Card>(), new InsertionSort<Card>(),
                new MergeSort<Card>())) {
            Card[] cards = {new Card(3, 'a'), new Card(1, 'b'), new Card(3, 'c'), new Card(2, 'd'), new Card(1, 'e'),
                new Card(3, 'f')};
            sorter.sort(cards);
            StringBuilder suits = new StringBuilder();
            for (Card card : cards) {
                suits.append(card.suit);
            }
            assertEquals(sorter.getClass().getSimpleName(), "bedacf", suits.toString());
        }
    }

    @Test
    public void sortedInputNeedsNoSwapAndNoShift() {
        Integer[] sorted = {1, 2, 3, 4, 5};

        BubbleSort<Integer> bubble = new BubbleSort<>();
        bubble.sort(sorted.clone());
        assertEquals(4, bubble.getCompareCount());
        assertEquals(0, bubble.getSwapCount());

        SelectionSort<Integer> selection = new SelectionSort<>();
        selection.sort(sorted.clone());
        assertEquals(10, selection.getCompareCount());
        assertEquals(0, selection.getSwapCount());

        InsertionSort<Integer> insertion = new InsertionSort<>();
        insertion.sort(sorted.clone());
        assertEquals(4, insertion.getCompareCount());
        assertEquals(4, insertion.getMoveCount());
    }

    @Test
    public void eachSorterKeepsItsOwnCounters() {
        BubbleSort<Integer> first = new BubbleSort<>();
        first.sort(new Integer[] {3, 2, 1});
        BubbleSort<Integer> second = new BubbleSort<>();
        second.sort(new Integer[] {1, 3, 2});
        assertEquals(3, first.getSwapCount());
        assertEquals(1, second.getSwapCount());
    }

    @Test
    public void theObserverSeesEveryCountedOperation() {
        for (Sorter<Integer> sorter : everySorter()) {
            CountingObserver observer = new CountingObserver();
            sorter.setObserver(observer);
            sorter.sort(new Integer[] {5, 3, 8, 1, 9, 2, 7, 3, 6});
            String name = sorter.getClass().getSimpleName();
            assertEquals(name, sorter.getCompareCount(), observer.compares);
            assertEquals(name, sorter.getSwapCount(), observer.swaps);
            assertEquals(name, sorter.getMoveCount(), observer.moves);
        }
    }

    private static List<Sorter<Integer>> everySorter() {
        return Arrays.<Sorter<Integer>>asList(new BubbleSort<Integer>(), new SelectionSort<Integer>(),
                new InsertionSort<Integer>(), new QuickSort<Integer>(), new MergeSort<Integer>());
    }

    private static final class Card implements Comparable<Card> {
        private final int rank;
        private final char suit;

        Card(int rank, char suit) {
            this.rank = rank;
            this.suit = suit;
        }

        public int compareTo(Card other) {
            return Integer.compare(rank, other.rank);
        }
    }

    private static final class CountingObserver implements Sorter.Observer<Integer> {
        private int compares;
        private int swaps;
        private int moves;

        public void compared(Integer a, Integer b, int result) {
            compares++;
        }

        public void swapped(Integer[] arr, int i, int j) {
            swaps++;
        }

        public void written(Integer[] arr, int i) {
            moves++;
        }

        public void copied(Integer[] from, Integer[] to, int lo, int hi) {
            moves += hi - lo + 1;
        }
    }
}
