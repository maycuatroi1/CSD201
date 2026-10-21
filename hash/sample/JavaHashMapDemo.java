package hash.sample;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class JavaHashMapDemo {

    public static void main(String[] args) {
        System.out.println("## HashSet");
        duplicateWords("i came i saw i conquered");
        System.out.println();
        System.out.println("## HashMap");
        Map<String, Integer> counts = wordFrequencies("the quick brown fox jumps over the lazy dog the fox");
        System.out.println("sorted by key: " + new TreeMap<>(counts));
        System.out.println();
        System.out.println("## StudentId as a key");
        lookUpByStudentId();
    }

    private static void duplicateWords(String text) {
        Set<String> words = new HashSet<>();
        for (String word : text.split(" ")) {
            if (!words.add(word)) {
                System.out.println("Duplicate: " + word);
            }
        }
        System.out.println("distinct: " + words);
    }

    private static Map<String, Integer> wordFrequencies(String text) {
        Map<String, Integer> counts = new HashMap<>();
        for (String word : text.split(" ")) {
            int count = counts.getOrDefault(word, 0);
            counts.put(word, count + 1);
        }
        System.out.println(counts);
        System.out.println("the -> " + counts.get("the"));
        System.out.println("cat -> " + counts.get("cat"));
        return counts;
    }

    private static void lookUpByStudentId() {
        Map<StudentId, String> names = new HashMap<>();
        names.put(new StudentId("HE180001"), "An");
        names.put(new StudentId("HE180002"), "Binh");
        StudentId typedAgain = new StudentId("HE180001");
        System.out.println(typedAgain + " -> " + names.get(typedAgain));
        names.put(new StudentId("HE180002"), "Binh Nguyen");
        System.out.println("size after a second put of HE180002: " + names.size());
    }
}
