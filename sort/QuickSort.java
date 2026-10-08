package sort;

public class QuickSort<T extends Comparable<T>> extends Sorter<T> {
    public void sort(T[] arr) {
        quickSort(arr, 0, arr.length - 1);
    }

    private void quickSort(T[] arr, int lo, int hi) {
        if (lo >= hi) {
            return;
        }
        int p = partition(arr, lo, hi);
        quickSort(arr, lo, p - 1);
        quickSort(arr, p + 1, hi);
    }

    private int partition(T[] arr, int lo, int hi) {
        focus(lo, hi);
        pivot(hi);
        T pivotValue = arr[hi];
        int i = lo;
        for (int j = lo; j < hi; j++) {
            if (compare(arr[j], pivotValue) < 0) {
                swap(arr, i, j);
                i++;
            }
        }
        swap(arr, i, hi);
        return i;
    }
}
