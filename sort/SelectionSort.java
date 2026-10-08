package sort;

public class SelectionSort<T extends Comparable<T>> extends Sorter<T> {
    public void sort(T[] arr) {
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (compare(arr[j], arr[minIdx]) < 0) {
                    minIdx = j;
                }
            }
            swap(arr, i, minIdx);
        }
    }
}
