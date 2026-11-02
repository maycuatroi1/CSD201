package text;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import text.sample.StringFinderDemo;

public class StringFinderTest {

    private static final String SLIDE_PATTERN = "101011";

    private static StringFinder[] everyFinder() {
        return new StringFinder[] {new BruteForceStringFinder(), new KMPStringFinder()};
    }

    private static List<Integer> positionsByIndexOf(String text, String search) {
        List<Integer> positions = new ArrayList<>();
        int found = text.indexOf(search);
        while (found >= 0) {
            positions.add(found);
            found = text.indexOf(search, found + 1);
        }
        return positions;
    }

    private static String randomString(Random random, int length, int alphabetSize) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append((char) ('a' + random.nextInt(alphabetSize)));
        }
        return text.toString();
    }

    @Test
    public void everyFinderGivesTheSlideAnswers() {
        for (StringFinder finder : everyFinder()) {
            assertEquals(Arrays.asList(3), finder.findString("abcabaabcabac", "abaa"));
            assertEquals(Arrays.asList(2), finder.findString("10101011", SLIDE_PATTERN));
            assertEquals(Arrays.asList(6), finder.findString("1010001010110", SLIDE_PATTERN));
        }
    }

    @Test
    public void lpsMatchesTheSlideTable() {
        assertArrayEquals(new int[] {0, 0, 1, 2, 3, 1}, new KMPStringFinder().computeLPSArray(SLIDE_PATTERN));
    }

    @Test
    public void bruteForceRepeatsFourComparisonsAtEachOfFiveStarts() {
        BruteForceStringFinder finder = new BruteForceStringFinder();
        assertEquals(Collections.emptyList(), finder.findString("aaaaaaaa", "aaab"));
        assertEquals(5 * 4, finder.getCompareCount());
    }

    @Test
    public void kmpComparesOnlyNewPairsAfterTheShift() {
        KMPStringFinder finder = new KMPStringFinder();
        finder.findString("10101011", SLIDE_PATTERN);
        int buildingLps = 7;
        int firstAttempt = 6;
        int afterKeepingThree = 3;
        assertEquals(buildingLps + firstAttempt + afterKeepingThree, finder.getCompareCount());
    }

    @Test
    public void overlappingMatchesAreAllReported() {
        for (StringFinder finder : everyFinder()) {
            assertEquals(Arrays.asList(0, 1, 2), finder.findString("AAAA", "AA"));
            assertEquals(Arrays.asList(0, 2), finder.findString("ABABAB", "ABAB"));
        }
    }

    @Test
    public void emptySearchMatchesEveryPosition() {
        for (StringFinder finder : everyFinder()) {
            assertEquals(Arrays.asList(0, 1, 2, 3), finder.findString("abc", ""));
        }
    }

    @Test
    public void searchLongerThanTextFindsNothing() {
        for (StringFinder finder : everyFinder()) {
            assertEquals(Collections.emptyList(), finder.findString("abc", "abcd"));
        }
    }

    @Test
    public void everyFinderAgreesWithIndexOfOnRandomStrings() {
        Random random = new Random(201);
        for (int round = 0; round < 20000; round++) {
            int alphabetSize = 1 + random.nextInt(3);
            String text = randomString(random, random.nextInt(60), alphabetSize);
            String search = randomString(random, 1 + random.nextInt(8), alphabetSize);
            List<Integer> expected = positionsByIndexOf(text, search);
            for (StringFinder finder : everyFinder()) {
                assertEquals(text + " / " + search, expected, finder.findString(text, search));
            }
        }
    }

    @Test
    public void kmpStaysWithinTwoNPlusTwoMComparisons() {
        Random random = new Random(201);
        for (int round = 0; round < 20000; round++) {
            int alphabetSize = 1 + random.nextInt(3);
            String text = randomString(random, random.nextInt(60), alphabetSize);
            String search = randomString(random, 1 + random.nextInt(8), alphabetSize);
            KMPStringFinder finder = new KMPStringFinder();
            finder.findString(text, search);
            assertTrue(text + " / " + search, finder.getCompareCount() <= 2 * text.length() + 2 * search.length());
        }
    }

    @Test
    public void worstCaseTextGivesTheClassNumbers() {
        String text = StringFinderDemo.worstCaseText();
        String search = "AAAAAAAAAAAAAAAAAAAB";
        List<Integer> expected = Arrays.asList(39941, 95941, 151941);

        BruteForceStringFinder bruteForce = new BruteForceStringFinder();
        assertEquals(expected, bruteForce.findString(text, search));
        assertEquals(2819240, bruteForce.getCompareCount());

        KMPStringFinder kmp = new KMPStringFinder();
        assertEquals(expected, kmp.findString(text, search));
        assertTrue(kmp.getCompareCount() <= 2 * text.length() + 2 * search.length());
    }
}
