package text;

import java.util.ArrayList;
import java.util.List;

public class KMPStringFinder extends StringFinder {

    public int[] computeLPSArray(String pattern) {
        int[] lps = new int[pattern.length()];
        int matched = 0;
        int k = 1;
        while (k < pattern.length()) {
            if (sameChar(pattern.charAt(k), pattern.charAt(matched))) {
                matched++;
                lps[k] = matched;
                k++;
            } else if (matched > 0) {
                matched = lps[matched - 1];
            } else {
                k++;
            }
        }
        return lps;
    }

    public List<Integer> findString(String text, String search) {
        List<Integer> result = new ArrayList<>();
        int[] lps = computeLPSArray(search);
        int start = 0;
        int matched = 0;
        while (start <= text.length() - search.length()) {
            while (matched < search.length() && sameChar(text.charAt(start + matched), search.charAt(matched))) {
                matched++;
            }
            if (matched == search.length()) {
                result.add(start);
            }
            if (matched == 0) {
                start++;
            } else {
                int keep = lps[matched - 1];
                int delta = matched - keep;
                start += delta;
                matched = keep;
            }
        }
        return result;
    }
}
