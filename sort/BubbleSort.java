package sort;

public class BubbleSort<T extends Comparable<T>> extends Sorter<T> {
    public void sort(T[] arr) {
        boolean isSwapped = false;
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            isSwapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                if (compare(arr[j], arr[j + 1]) > 0) {
                    swap(arr, j, j + 1);
                    isSwapped = true;
                }
            }
            if (!isSwapped) {
                break;
            }
        }
    }
}
