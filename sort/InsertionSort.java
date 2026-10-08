package sort;

public class InsertionSort<T extends Comparable<T>> extends Sorter<T> {
    public void sort(T[] arr) {
        int n = arr.length;
        for (int i = 1; i < n; i++) {
            T key = arr[i];
            int j = i - 1;
            while (j >= 0 && compare(arr[j], key) > 0) {
                set(arr, j + 1, arr[j]);
                j--;
            }
            set(arr, j + 1, key);
        }
    }
}
