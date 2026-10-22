package hash;

public final class HashFunctions {

    private HashFunctions() {
    }

    public static int division(long x, int m) {
        return (int) Math.floorMod(x, (long) m);
    }

    public static int shiftFolding(long x, int partLength, int m) {
        return fold(x, partLength, m, false);
    }

    public static int boundaryFolding(long x, int partLength, int m) {
        return fold(x, partLength, m, true);
    }

    private static int fold(long x, int partLength, int m, boolean reverseEveryOther) {
        String digits = String.valueOf(x);
        long sum = 0;
        for (int start = 0, part = 0; start < digits.length(); start += partLength, part++) {
            String piece = digits.substring(start, Math.min(start + partLength, digits.length()));
            if (reverseEveryOther && part % 2 == 1) {
                piece = new StringBuilder(piece).reverse().toString();
            }
            sum += Long.parseLong(piece);
        }
        return division(sum, m);
    }

    public static int midSquare(long x, int width, int m) {
        String square = String.valueOf(x * x);
        int start = (square.length() - width) / 2;
        return division(Long.parseLong(square.substring(start, start + width)), m);
    }

    public static int extraction(long x, int front, int back, int m) {
        String digits = String.valueOf(x);
        String kept = digits.substring(0, front) + digits.substring(digits.length() - back);
        return division(Long.parseLong(kept), m);
    }

    public static int radixTransformation(long x, int radix, int m) {
        return division(Long.parseLong(Long.toString(x, radix)), m);
    }

    public static int sumOfCodes(String s) {
        int sum = 0;
        for (int i = 0; i < s.length(); i++) {
            sum += s.charAt(i);
        }
        return sum;
    }

    public static int polynomialHash(String s) {
        int h = 0;
        for (int i = 0; i < s.length(); i++) {
            h = 31 * h + s.charAt(i);
        }
        return h;
    }

    public static int mad(int hashCode, int a, int b, int p, int m) {
        return division(Math.floorMod((long) a * hashCode + b, (long) p), m);
    }
}
