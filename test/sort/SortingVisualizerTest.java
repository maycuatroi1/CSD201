package sort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;
import org.junit.Test;

public class SortingVisualizerTest {

    @Test
    public void everyVoyageEndsWithEveryShipLiveInItsSortedSlot() {
        Random random = new Random(7);
        for (SortingVisualizer.Chapter chapter : everyAlgorithm().chapters) {
            for (int round = 0; round < 20; round++) {
                Integer[] data = new Integer[2 + random.nextInt(12)];
                for (int i = 0; i < data.length; i++) {
                    data[i] = random.nextInt(10);
                }
                SortingVisualizer.Voyage voyage = SortingVisualizer.voyage(chapter, data, v -> v.toString(), v -> "");
                List<SortingVisualizer.Step> steps = voyage.steps;
                SortingVisualizer.Step end = steps.get(steps.size() - 1);
                assertFalse(chapter.title, voyage.cut);
                assertEquals(chapter.title, 0, voyage.bypassed);
                assertEquals(chapter.title, SortingVisualizer.Kind.END, end.kind);
                for (int s = 0; s < data.length; s++) {
                    assertEquals(chapter.title, s, end.live[end.slots[s]]);
                    if (s > 0) {
                        assertTrue(chapter.title, voyage.items[end.slots[s - 1]].height
                                <= voyage.items[end.slots[s]].height);
                    }
                }
            }
        }
    }

    @Test
    public void insertionSortHoldsTheKeyAndMergeSortLiftsHalvesIntoTheScratchArray() {
        Integer[] data = {5, 3, 8, 1, 9, 2};
        SortingVisualizer visualizer = everyAlgorithm();
        assertTrue(anyStepWhere(visualizer.chapters.get(2), data, -1, -1));
        assertTrue(anyStepWhere(visualizer.chapters.get(4), data, data.length, 2 * data.length - 1));
    }

    @Test
    public void anArrayIsCopiedWhenItIsAdded() {
        Integer[] data = {3, 1, 2};
        SortingVisualizer visualizer = everyAlgorithm();
        visualizer.addArray("as integers", data, v -> v.toString(), v -> "");
        data[0] = 0;
        SortingVisualizer.Fleet fleet = visualizer.fleets.get(visualizer.fleets.size() - 1);
        SortingVisualizer.Voyage voyage = fleet.launch.apply(visualizer.chapters.get(0), new Random(1));
        assertEquals("3", voyage.items[0].name);
    }

    private static boolean anyStepWhere(SortingVisualizer.Chapter chapter, Integer[] data, int low, int high) {
        SortingVisualizer.Voyage voyage = SortingVisualizer.voyage(chapter, data, v -> v.toString(), v -> "");
        for (SortingVisualizer.Step step : voyage.steps) {
            for (int at : step.live) {
                if (at >= low && at <= high) {
                    return true;
                }
            }
        }
        return false;
    }

    private static SortingVisualizer everyAlgorithm() {
        SortingVisualizer visualizer = new SortingVisualizer();
        visualizer.addAlgorithm(BubbleSort::new, "bubble");
        visualizer.addAlgorithm(SelectionSort::new, "selection");
        visualizer.addAlgorithm(InsertionSort::new, "insertion");
        visualizer.addAlgorithm(QuickSort::new, "quick");
        visualizer.addAlgorithm(MergeSort::new, "merge");
        return visualizer;
    }
}
