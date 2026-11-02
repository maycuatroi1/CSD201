package text;

import java.util.List;

public abstract class StringFinder {

    private int compareCount = 0;

    public abstract List<Integer> findString(String text, String search);

    protected boolean sameChar(char a, char b) {
        compareCount++;
        return a == b;
    }

    public int getCompareCount() {
        return compareCount;
    }
}
