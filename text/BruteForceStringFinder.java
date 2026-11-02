package text;

import java.util.ArrayList;
import java.util.List;

public class BruteForceStringFinder extends StringFinder {

    public List<Integer> findString(String text, String search) {
        List<Integer> result = new ArrayList<>();
        for (int start = 0; start <= text.length() - search.length(); start++) {
            int matched = 0;
            while (matched < search.length() && sameChar(text.charAt(start + matched), search.charAt(matched))) {
                matched++;
            }
            if (matched == search.length()) {
                result.add(start);
            }
        }
        return result;
    }
}
