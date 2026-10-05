package graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class GraphTest {

    private static final List<Character> VERTICES = Arrays.asList('a', 'b', 'c', 'd', 'e', 'f', 'g');
    private static final String[] EDGES = {"a-c", "a-d", "a-f", "b-d", "b-e", "c-f", "d-e", "d-f"};

    @Test
    public void bothRepresentationsListEveryEdgeOnce() {
        String expected = "[a-c(1), a-d(1), a-f(1), b-d(1), b-e(1), c-f(1), d-e(1), d-f(1)]";
        assertEquals(expected, sampleList().getEdges().toString());
        assertEquals(expected, sampleMatrix().getEdges().toString());
    }

    @Test
    public void bothRepresentationsKeepTheVertexOrder() {
        assertEquals(VERTICES, sampleList().getVertices());
        assertEquals(VERTICES, sampleMatrix().getVertices());
    }

    @Test
    public void bothRepresentationsTraverseTheSampleGraphTheSameWay() {
        assertTraversals(sampleList());
        assertTraversals(sampleMatrix());
    }

    @Test
    public void isolatedVertexHasNoNeighbors() {
        assertEquals(Collections.emptyList(), sampleList().getNeighbors('g'));
        assertEquals(Collections.emptyList(), sampleMatrix().getNeighbors('g'));
        assertEquals(Arrays.asList('g'), sampleList().breadthFirstTraversal('g'));
        assertEquals(Arrays.asList('g'), sampleMatrix().breadthFirstTraversal('g'));
    }

    @Test
    public void listKeepsNeighborsInInsertionOrderMatrixInVertexOrder() {
        List<Character> vertices = Arrays.asList('a', 'b', 'c');
        AbstractGraph<Character> list = new AdjacencyListGraph<>(vertices);
        AbstractGraph<Character> matrix = new AdjacencyMatrixGraph<>(vertices);
        for (AbstractGraph<Character> graph : Arrays.asList(list, matrix)) {
            graph.addEdge('a', 'c', 1);
            graph.addEdge('a', 'b', 1);
        }
        assertEquals(Arrays.asList('c', 'b'), list.getNeighbors('a'));
        assertEquals(Arrays.asList('b', 'c'), matrix.getNeighbors('a'));
        assertEquals(Arrays.asList('a', 'c', 'b'), list.depthFirstTraversal('a'));
        assertEquals(Arrays.asList('a', 'b', 'c'), matrix.depthFirstTraversal('a'));
    }

    @Test
    public void addingAnEdgeAgainReplacesItsWeight() {
        assertReplacesWeight(new AdjacencyListGraph<>(Arrays.asList('a', 'b')));
        assertReplacesWeight(new AdjacencyMatrixGraph<>(Arrays.asList('a', 'b')));
    }

    @Test
    public void selfLoopIsOneEdge() {
        assertSelfLoop(new AdjacencyListGraph<>(Arrays.asList('a')));
        assertSelfLoop(new AdjacencyMatrixGraph<>(Arrays.asList('a')));
    }

    @Test
    public void addVertexKeepsExistingEdgesAndIgnoresDuplicates() {
        assertAddVertex(sampleList());
        assertAddVertex(sampleMatrix());
    }

    @Test
    public void duplicateVerticesInTheConstructorAreIgnored() {
        List<Character> vertices = Arrays.asList('a', 'b', 'a');
        assertEquals(Arrays.asList('a', 'b'), new AdjacencyListGraph<>(vertices).getVertices());
        assertEquals(Arrays.asList('a', 'b'), new AdjacencyMatrixGraph<>(vertices).getVertices());
    }

    @Test(expected = IllegalArgumentException.class)
    public void listRejectsAnEdgeToAnUnknownVertex() {
        sampleList().addEdge('a', 'z', 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void matrixRejectsAnEdgeToAnUnknownVertex() {
        sampleMatrix().addEdge('a', 'z', 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void listRejectsAnUnknownStartVertex() {
        sampleList().depthFirstTraversal('z');
    }

    @Test(expected = IllegalArgumentException.class)
    public void matrixRejectsAnUnknownStartVertex() {
        sampleMatrix().breadthFirstTraversal('z');
    }

    private static AbstractGraph<Character> sampleList() {
        return connect(new AdjacencyListGraph<>(VERTICES));
    }

    private static AbstractGraph<Character> sampleMatrix() {
        return connect(new AdjacencyMatrixGraph<>(VERTICES));
    }

    private static AbstractGraph<Character> connect(AbstractGraph<Character> graph) {
        for (String edge : EDGES) {
            graph.addEdge(edge.charAt(0), edge.charAt(2), 1);
        }
        return graph;
    }

    private static void assertTraversals(AbstractGraph<Character> graph) {
        assertEquals(Arrays.asList('a', 'b', 'e', 'f'), graph.getNeighbors('d'));
        assertEquals(Arrays.asList('a', 'c', 'f', 'd', 'b', 'e'), graph.depthFirstTraversal('a'));
        assertEquals(Arrays.asList('a', 'c', 'd', 'f', 'b', 'e'), graph.breadthFirstTraversal('a'));
        assertEquals(Arrays.asList('e', 'b', 'd', 'a', 'c', 'f'), graph.depthFirstTraversal('e'));
        assertEquals(Arrays.asList('e', 'b', 'd', 'a', 'f', 'c'), graph.breadthFirstTraversal('e'));
    }

    private static void assertReplacesWeight(AbstractGraph<Character> graph) {
        graph.addEdge('a', 'b', 4);
        graph.addEdge('b', 'a', 7);
        assertEquals("[a-b(7)]", graph.getEdges().toString());
        assertEquals(Arrays.asList('b'), graph.getNeighbors('a'));
    }

    private static void assertSelfLoop(AbstractGraph<Character> graph) {
        graph.addEdge('a', 'a', 2);
        assertEquals("[a-a(2)]", graph.getEdges().toString());
        assertEquals(Arrays.asList('a'), graph.getNeighbors('a'));
        assertEquals(Arrays.asList('a'), graph.depthFirstTraversal('a'));
    }

    private static void assertAddVertex(AbstractGraph<Character> graph) {
        assertFalse(graph.hasVertex('h'));
        graph.addVertex('h');
        graph.addVertex('a');
        assertTrue(graph.hasVertex('h'));
        assertEquals(8, graph.getVertices().size());
        assertEquals(8, graph.getEdges().size());
        graph.addEdge('g', 'h', 3);
        assertEquals(Arrays.asList('g', 'h'), graph.depthFirstTraversal('g'));
        assertEquals(Arrays.asList('a', 'c', 'f', 'd', 'b', 'e'), graph.depthFirstTraversal('a'));
    }
}
