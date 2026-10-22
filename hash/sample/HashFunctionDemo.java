package hash.sample;

import hash.HashFunctions;

public class HashFunctionDemo {

    public static void main(String[] args) {
        System.out.println("division       89 mod 10 = " + HashFunctions.division(89, 10));
        System.out.println("division       -3 mod 10 = " + HashFunctions.division(-3, 10));
        System.out.println("shift folding  72320354121324 -> " + HashFunctions.shiftFolding(72320354121324L, 3, 1000));
        System.out.println("boundary fold  72320354121324 -> " + HashFunctions.boundaryFolding(72320354121324L, 3, 1000));
        System.out.println("mid-square     3121 -> " + HashFunctions.midSquare(3121, 3, 1000));
        System.out.println("extraction     123456789 -> " + HashFunctions.extraction(123456789, 2, 2, 10000));
        System.out.println("radix 9        345 -> " + HashFunctions.radixTransformation(345, 9, 1000));
        System.out.println();
        for (String word : new String[] {"stop", "tops", "pots", "spot"}) {
            int code = HashFunctions.polynomialHash(word);
            System.out.println(word + "  sum " + HashFunctions.sumOfCodes(word) + "  hashCode " + code
                    + "  mod 11 = " + HashFunctions.division(code, 11) + "  String.hashCode " + word.hashCode());
        }
        System.out.println();
        for (int key : new int[] {30, 70, 110, 250, 480, 520, 640, 900}) {
            System.out.println(key + "  mod 10 = " + HashFunctions.division(key, 10)
                    + "  MAD = " + HashFunctions.mad(key, 3, 7, 109, 10));
        }
    }
}
