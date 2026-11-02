package text.sample;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import text.BruteForceStringFinder;
import text.KMPStringFinder;
import text.StringFinder;

public class StringFinderDemo {

    private static final int LINES = 2000;
    private static final int LINE_LENGTH = 79;
    private static final int B_COLUMN = 40;
    private static final List<Integer> LINES_WITH_B = Arrays.asList(499, 1199, 1899);

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            byte[] bytes = Files.readAllBytes(Paths.get(args[0]));
            compare(args[0], new String(bytes, StandardCharsets.UTF_8), args[1]);
            return;
        }
        compare("abcabaabcabac", "abcabaabcabac", "abaa");
        compare("aaaaaaaa", "aaaaaaaa", "aaab");
        compare("10101011", "10101011", "101011");
        compare("1010001010110", "1010001010110", "101011");
        compare(LINES + " lines of A with three B", worstCaseText(), "AAAAAAAAAAAAAAAAAAAB");
    }

    public static String worstCaseText() {
        StringBuilder text = new StringBuilder();
        for (int line = 0; line < LINES; line++) {
            for (int column = 0; column < LINE_LENGTH; column++) {
                text.append(LINES_WITH_B.contains(line) && column == B_COLUMN ? 'B' : 'A');
            }
            text.append('\n');
        }
        return text.toString();
    }

    private static void compare(String title, String text, String search) {
        System.out.println(title + " / " + search);
        System.out.println("LPS: " + Arrays.toString(new KMPStringFinder().computeLPSArray(search)));
        for (StringFinder finder : new StringFinder[] {new BruteForceStringFinder(), new KMPStringFinder()}) {
            List<Integer> indices = finder.findString(text, search);
            System.out.println(finder.getClass().getSimpleName() + ": found at " + indices
                    + ", compares: " + finder.getCompareCount());
        }
        System.out.println();
    }
}
