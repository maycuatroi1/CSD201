package sort;

public abstract class Sorter<T extends Comparable<T>> {

    public interface Observer<T> {
        default void compared(T a, T b, int result) {
        }

        default void swapped(T[] arr, int i, int j) {
        }

        default void written(T[] arr, int i) {
        }

        default void copied(T[] from, T[] to, int lo, int hi) {
        }

        default void split(int lo, int mid, int hi) {
        }

        default void focused(int lo, int hi) {
        }

        default void pivoted(int p) {
        }
    }

    private int compareCount = 0;
    private int swapCount = 0;
    private int moveCount = 0;
    private Observer<T> observer = silent();

    private static <T> Observer<T> silent() {
        return new Observer<T>() {
        };
    }

    public void setObserver(Observer<T> observer) {
        this.observer = observer;
    }

    public abstract void sort(T[] arr);

    protected int compare(T a, T b) {
        int result = a.compareTo(b);
        compareCount++;
        observer.compared(a, b, result);
        return result;
    }

    protected void swap(T[] arr, int i, int j) {
        if (i == j) {
            return;
        }
        T temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
        swapCount++;
        observer.swapped(arr, i, j);
    }

    protected void set(T[] arr, int i, T value) {
        arr[i] = value;
        moveCount++;
        observer.written(arr, i);
    }

    protected void copy(T[] from, T[] to, int lo, int hi) {
        for (int k = lo; k <= hi; k++) {
            to[k] = from[k];
        }
        moveCount += hi - lo + 1;
        observer.copied(from, to, lo, hi);
    }

    protected void split(int lo, int mid, int hi) {
        observer.split(lo, mid, hi);
    }

    protected void focus(int lo, int hi) {
        observer.focused(lo, hi);
    }

    protected void pivot(int p) {
        observer.pivoted(p);
    }

    public int getCompareCount() {
        return compareCount;
    }

    public int getSwapCount() {
        return swapCount;
    }

    public int getMoveCount() {
        return moveCount;
    }
}
