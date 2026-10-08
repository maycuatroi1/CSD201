package sort;

public class MergeSort<T extends Comparable<T>> extends Sorter<T> {
    public void sort(T[] arr) {
        T[] aux = arr.clone();
        mergeSort(arr, aux, 0, arr.length - 1);
    }

    private void mergeSort(T[] arr, T[] aux, int lo, int hi) {
        if (lo >= hi) {
            return;
        }
        int mid = (lo + hi) / 2;
        split(lo, mid, hi);
        mergeSort(arr, aux, lo, mid);
        mergeSort(arr, aux, mid + 1, hi);
        merge(arr, aux, lo, mid, hi);
    }

    private void merge(T[] arr, T[] aux, int lo, int mid, int hi) {
        copy(arr, aux, lo, hi);
        int i = lo;
        int j = mid + 1;
        for (int k = lo; k <= hi; k++) {
            if (i > mid) {
                set(arr, k, aux[j++]);
            } else if (j > hi) {
                set(arr, k, aux[i++]);
            } else if (compare(aux[j], aux[i]) < 0) {
                set(arr, k, aux[j++]);
            } else {
                set(arr, k, aux[i++]);
            }
        }
    }
}
