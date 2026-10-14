package graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import graph.WeightedGraphVisualizer.Algorithm;
import graph.WeightedGraphVisualizer.Mark;
import graph.WeightedGraphVisualizer.Replay;
import graph.WeightedGraphVisualizer.Step;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class WeightedGraphVisualizerTest {

    @Test
    public void primAddsFiveEdgesThenEmptiesTheQueue() {
        Replay<String> replay = WeightedGraphVisualizer.record(SampleWeightedGraphs.list(), Algorithm.PRIM, "A");
        assertEquals(Arrays.asList("Start at A", "Add A-B (7)", "Add A-C (9)", "Add C-F (2)", "Add F-E (9)",
                "Add E-D (6)", "Skip B-C (10)", "Skip C-D (11)", "Skip A-F (14)", "Skip B-D (15)", "Done"),
                titles(replay));
        assertEquals(33, last(replay).weight);
        assertEquals(5, treeEdges(replay));
    }

    @Test
    public void kruskalTriesEveryEdgeLightestFirst() {
        Replay<String> replay = WeightedGraphVisualizer.record(SampleWeightedGraphs.list(), Algorithm.KRUSKAL, null);
        assertEquals(Arrays.asList("Sort the edges by weight", "Add C-F (2)", "Add D-E (6)", "Add A-B (7)",
                "Add A-C (9)", "Add E-F (9)", "Skip B-C (10)", "Skip C-D (11)", "Skip A-F (14)",
                "Skip B-D (15)", "Done"), titles(replay));
        assertEquals(33, last(replay).weight);
        assertEquals(5, treeEdges(replay));
    }

    @Test
    public void dijkstraSettlesEveryVertexAndDropsTwoOldEntries() {
        Replay<String> replay = WeightedGraphVisualizer.record(SampleWeightedGraphs.list(), Algorithm.DIJKSTRA, "A");
        assertEquals(Arrays.asList("Start at A", "Settle A at distance 0", "Relax A-B (7)", "Relax A-C (9)",
                "Relax A-F (14)", "Settle B at distance 7", "Relax B-C (10)", "Relax B-D (15)",
                "Settle C at distance 9", "Relax C-D (11)", "Relax C-F (2)", "Settle F at distance 11",
                "Relax F-E (9)", "Skip old entry (F, 14)", "Settle D at distance 20", "Relax D-E (6)",
                "Settle E at distance 20", "Skip old entry (D, 22)", "Done"), titles(replay));
        assertEquals(5, treeEdges(replay));
        assertEquals(Integer.valueOf(20), last(replay).distance.get("E"));
        assertEquals(6, last(replay).done.size());
    }

    @Test
    public void primSaysWhenItCannotLeaveTheComponentOfTheStart() {
        Replay<String> replay = WeightedGraphVisualizer.record(SampleWeightedGraphs.twoIslands(),
                Algorithm.PRIM, "P");
        assertEquals(Arrays.asList("Start at P", "Add P-Q (1)", "Add Q-R (4)", "Done"), titles(replay));
        assertTrue(last(replay).detail.contains("Prim spans only the component of P"));
    }

    @Test
    public void kruskalSaysWhenTheResultIsAForest() {
        Replay<String> replay = WeightedGraphVisualizer.record(SampleWeightedGraphs.twoIslands(),
                Algorithm.KRUSKAL, null);
        assertEquals(Arrays.asList("Sort the edges by weight", "Add P-Q (1)", "Add S-T (2)", "Add Q-R (4)",
                "Done"), titles(replay));
        assertTrue(last(replay).detail.contains("2 components"));
        assertTrue(last(replay).detail.contains("minimum spanning forest"));
    }

    @Test
    public void aGraphWithoutVerticesGivesOneStepThatSaysWhy() {
        Replay<String> replay = WeightedGraphVisualizer.record(
                new AdjacencyListGraph<String>(Collections.<String>emptyList()), Algorithm.PRIM, null);
        assertEquals(Arrays.asList("Cannot run Prim minimum spanning tree"), titles(replay));
        assertEquals("The graph has no vertices.", last(replay).detail);
    }

    @Test
    public void aNegativeWeightGivesOneStepThatSaysWhy() {
        AbstractGraph<String> graph = new AdjacencyListGraph<>(Arrays.asList("A", "B"));
        graph.addEdge("A", "B", -1);
        Replay<String> replay = WeightedGraphVisualizer.record(graph, Algorithm.DIJKSTRA, "A");
        assertEquals(Arrays.asList("Cannot run Dijkstra shortest paths"), titles(replay));
        assertEquals("Dijkstra needs weights >= 0, got A-B(-1)", last(replay).detail);
    }

    private static List<String> titles(Replay<String> replay) {
        List<String> titles = new ArrayList<>();
        for (Step<String> step : replay.steps) {
            titles.add(step.title);
        }
        return titles;
    }

    private static Step<String> last(Replay<String> replay) {
        return replay.steps.get(replay.steps.size() - 1);
    }

    private static int treeEdges(Replay<String> replay) {
        int count = 0;
        for (Mark mark : last(replay).marks) {
            if (mark == Mark.TREE) {
                count++;
            }
        }
        return count;
    }
}
