package hash;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HashFunctionsTest {

    @Test
    public void divisionKeepsNegativeKeysInsideTheTable() {
        assertEquals(9, HashFunctions.division(89, 10));
        assertEquals(7, HashFunctions.division(-3, 10));
    }

    @Test
    public void foldingMatchesTheSlideExample() {
        assertEquals(704, HashFunctions.shiftFolding(72320354121324L, 3, 1000));
        assertEquals(902, HashFunctions.boundaryFolding(72320354121324L, 3, 1000));
    }

    @Test
    public void midSquareExtractionAndRadixMatchTheSlideExamples() {
        assertEquals(406, HashFunctions.midSquare(3121, 3, 1000));
        assertEquals(1289, HashFunctions.extraction(123456789, 2, 2, 10000));
        assertEquals(423, HashFunctions.radixTransformation(345, 9, 1000));
    }

    @Test
    public void polynomialHashIsStringHashCodeAndSeparatesAnagrams() {
        for (String word : new String[] {"stop", "tops", "pots", "spot", "cat", "Aa", "BB"}) {
            assertEquals(word, word.hashCode(), HashFunctions.polynomialHash(word));
        }
        assertEquals(98262, HashFunctions.polynomialHash("cat"));
        assertEquals(HashFunctions.sumOfCodes("stop"), HashFunctions.sumOfCodes("pots"));
        assertEquals(6, HashFunctions.division(HashFunctions.polynomialHash("stop"), 11));
        assertEquals(3, HashFunctions.division(HashFunctions.polynomialHash("pots"), 11));
    }

    @Test
    public void madSpreadsKeysThatAllEndInZero() {
        int[] keys = {30, 70, 110, 250, 480, 520, 640, 900};
        int[] expected = {7, 8, 0, 3, 0, 1, 4, 1};
        for (int i = 0; i < keys.length; i++) {
            assertEquals(0, HashFunctions.division(keys[i], 10));
            assertEquals(expected[i], HashFunctions.mad(keys[i], 3, 7, 109, 10));
        }
    }
}
