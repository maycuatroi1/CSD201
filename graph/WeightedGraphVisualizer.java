package graph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

public class WeightedGraphVisualizer<T> {

    public enum Algorithm {
        DIJKSTRA("Dijkstra shortest paths"),
        PRIM("Prim minimum spanning tree"),
        KRUSKAL("Kruskal minimum spanning tree");

        private final String label;

        Algorithm(String label) {
            this.label = label;
        }

        public String toString() {
            return label;
        }
    }

    enum Mark {
        NONE,
        QUEUED,
        TREE,
        SKIPPED
    }

    static final class Step<T> {
        final String title;
        final String detail;
        final int current;
        final T vertex;
        final Mark[] marks;
        final Set<Integer> fresh;
        final Set<T> done;
        final Map<T, Integer> distance;
        final Map<T, Integer> previous;
        final List<String> queue;
        final int weight;

        Step(String title, String detail, int current, T vertex, Mark[] marks, Set<Integer> fresh,
             Set<T> done, Map<T, Integer> distance, Map<T, Integer> previous, List<String> queue, int weight) {
            this.title = title;
            this.detail = detail;
            this.current = current;
            this.vertex = vertex;
            this.marks = marks;
            this.fresh = fresh;
            this.done = done;
            this.distance = distance;
            this.previous = previous;
            this.queue = queue;
            this.weight = weight;
        }
    }

    static final class Replay<T> {
        final Algorithm algorithm;
        final T start;
        final List<T> vertices;
        final List<Edge<T>> edges;
        final List<Step<T>> steps;

        Replay(Algorithm algorithm, T start, List<T> vertices, List<Edge<T>> edges, List<Step<T>> steps) {
            this.algorithm = algorithm;
            this.start = start;
            this.vertices = vertices;
            this.edges = edges;
            this.steps = steps;
        }
    }

    static <T> Replay<T> record(AbstractGraph<T> graph, Algorithm algorithm, T start) {
        List<T> vertices = graph.getVertices();
        Recorder<T> recorder = new Recorder<>(algorithm, vertices, graph.getEdges());
        if (vertices.isEmpty()) {
            return recorder.fail("The graph has no vertices.", null);
        }
        if (algorithm == Algorithm.KRUSKAL) {
            start = null;
        } else if (start == null) {
            start = vertices.get(0);
        }
        try {
            switch (algorithm) {
                case DIJKSTRA:
                    return recorder.finish(dijkstraSummary(new DijkstraShortestPath<>(graph, start, recorder), vertices), start);
                case PRIM:
                    return recorder.finish(primSummary(new PrimMST<>(graph, start, recorder), vertices.size(), start), start);
                default:
                    return recorder.finish(kruskalSummary(new KruskalMST<>(graph, recorder), recorder), null);
            }
        } catch (IllegalArgumentException e) {
            return recorder.fail(e.getMessage(), start);
        }
    }

    private static <T> List<String> dijkstraSummary(DijkstraShortestPath<T> sp, List<T> vertices) {
        List<String> lines = new ArrayList<>();
        lines.add("Shortest distances from " + sp.getSource() + ":");
        for (T v : vertices) {
            lines.add(sp.hasPathTo(v)
                    ? String.format("  %-4s %4d   %s", v, sp.distanceTo(v), arrows(sp.pathTo(v)))
                    : String.format("  %-4s %4s   no path", v, "∞"));
        }
        lines.add("Click a vertex to draw its path.");
        return lines;
    }

    private static <T> List<String> primSummary(PrimMST<T> mst, int vertexCount, T start) {
        List<String> lines = new ArrayList<>();
        lines.add("Minimum spanning tree from " + start + ": " + mst.getEdges().size()
                + " edges, weight " + mst.getWeight() + ".");
        lines.add("  " + labels(mst.getEdges()));
        if (mst.getEdges().size() < vertexCount - 1) {
            lines.add("The graph is not connected: Prim spans only the component of " + start + ".");
        }
        return lines;
    }

    private static <T> List<String> kruskalSummary(KruskalMST<T> mst, Recorder<T> recorder) {
        List<String> lines = new ArrayList<>();
        lines.add("Minimum spanning tree: " + mst.getEdges().size() + " edges, weight " + mst.getWeight() + ".");
        lines.add("  " + labels(mst.getEdges()));
        int trees = recorder.treeCount();
        if (trees > 1) {
            lines.add("The graph has " + trees + " components, so this is a minimum spanning forest.");
        }
        return lines;
    }

    private static final class Waiting<T> {
        final Edge<T> edge;
        final T vertex;
        final int key;

        Waiting(Edge<T> edge, T vertex, int key) {
            this.edge = edge;
            this.vertex = vertex;
            this.key = key;
        }
    }

    static final class Recorder<T> implements GraphAlgorithmObserver<T> {
        private final Algorithm algorithm;
        private final List<T> vertices;
        private final List<Edge<T>> edges;
        private final Map<List<T>, Integer> index = new HashMap<>();
        private final List<Step<T>> steps = new ArrayList<>();

        private final Mark[] marks;
        private final Set<T> done = new LinkedHashSet<>();
        private final Map<T, Integer> distance = new HashMap<>();
        private final Map<T, Integer> previous = new HashMap<>();
        private final List<Waiting<T>> queue = new ArrayList<>();
        private final List<Edge<T>> sorted = new ArrayList<>();
        private int tried;
        private int weight;

        private boolean open;
        private String title;
        private StringBuilder detail;
        private int current;
        private T vertex;
        private Set<Integer> fresh;
        private List<String> queuedNow;

        Recorder(Algorithm algorithm, List<T> vertices, List<Edge<T>> edges) {
            this.algorithm = algorithm;
            this.vertices = vertices;
            this.edges = edges;
            for (int i = 0; i < edges.size(); i++) {
                Edge<T> e = edges.get(i);
                index.put(Arrays.asList(e.getSrc(), e.getDest()), i);
                index.put(Arrays.asList(e.getDest(), e.getSrc()), i);
            }
            marks = new Mark[edges.size()];
            Arrays.fill(marks, Mark.NONE);
        }

        public void started(T start) {
            begin("Start at " + start, -1, start);
            if (algorithm == Algorithm.DIJKSTRA) {
                distance.put(start, 0);
                enqueue(new Waiting<T>(null, start, 0));
                say("dist[" + start + "] = 0; every other distance is ∞ for now.");
                say("Queue (" + start + ", 0).");
            } else {
                done.add(start);
                say("The tree is just " + start + " for now.");
            }
        }

        public void edgesSorted(List<Edge<T>> edges) {
            begin("Sort the edges by weight", -1, null);
            sorted.addAll(edges);
            say("Try the edges from lightest to heaviest.");
            say("Every vertex starts as a tree of its own.");
            say("Keep an edge when its ends are in different trees.");
        }

        public void edgeQueued(Edge<T> edge) {
            int i = indexOf(edge);
            marks[i] = Mark.QUEUED;
            fresh.add(i);
            enqueue(new Waiting<>(edge, null, edge.getWeight()));
            queuedNow.add(label(edge));
        }

        public void edgeAdded(Edge<T> edge) {
            int i = indexOf(edge);
            T a = edge.getSrc();
            T b = edge.getDest();
            if (algorithm == Algorithm.PRIM) {
                begin("Add " + label(edge), i, b);
                dequeueEdge(i);
                say(label(edge) + " is the lightest edge in the queue,");
                say("and " + b + " is not in the tree yet: add the edge and " + b + ".");
                done.add(b);
            } else {
                begin("Add " + label(edge), i, null);
                tried++;
                say(a + " is in " + braces(treeOf(a)) + ", " + b + " is in " + braces(treeOf(b)) + ".");
                say("Different trees: add the edge, the two trees become one.");
            }
            marks[i] = Mark.TREE;
            weight += edge.getWeight();
            say("Tree weight " + weight + ".");
        }

        public void edgeSkipped(Edge<T> edge) {
            int i = indexOf(edge);
            T a = edge.getSrc();
            T b = edge.getDest();
            begin("Skip " + label(edge), i, null);
            if (algorithm == Algorithm.PRIM) {
                dequeueEdge(i);
                say(label(edge) + " is the lightest edge in the queue,");
                say("but " + b + " joined the tree after the edge was queued.");
            } else {
                tried++;
                say(a + " and " + b + " are both in " + braces(treeOf(a)) + ".");
            }
            say("The edge would close a cycle: skip it.");
            marks[i] = Mark.SKIPPED;
        }

        public void vertexSettled(T v, int d) {
            begin("Settle " + v + " at distance " + d, previous.containsKey(v) ? previous.get(v) : -1, v);
            dequeueVertex(v, d);
            done.add(v);
            say("(" + v + ", " + d + ") is the smallest entry in the queue: take it out.");
            say("No shorter path to " + v + " can turn up, so dist[" + v + "] = " + d + " is final.");
            say("Then relax the edges from " + v + " to vertices that are not settled (if any).");
        }

        public void staleEntrySkipped(T v, int d) {
            begin("Skip old entry (" + v + ", " + d + ")", -1, v);
            dequeueVertex(v, d);
            say("(" + v + ", " + d + ") is the smallest entry in the queue: take it out.");
            say(v + " was settled at distance " + distance.get(v) + " already;");
            say("this entry was queued before a shorter path was found. Skip it.");
        }

        public void edgeRelaxed(Edge<T> edge, int newDistance, boolean improved) {
            int i = indexOf(edge);
            T u = edge.getSrc();
            T v = edge.getDest();
            Integer old = distance.get(v);
            begin("Relax " + label(edge), i, v);
            say("dist[" + u + "] + " + edge.getWeight() + " = " + distance.get(u) + " + " + edge.getWeight()
                    + " = " + newDistance);
            if (improved) {
                say("is less than dist[" + v + "] = " + (old == null ? "∞" : old) + ".");
                say("Shorter path: dist[" + v + "] = " + newDistance + ", prev[" + v + "] = " + u + ".");
                say("Queue (" + v + ", " + newDistance + ").");
                Integer replaced = previous.put(v, i);
                if (replaced != null) {
                    marks[replaced] = Mark.NONE;
                }
                marks[i] = Mark.TREE;
                distance.put(v, newDistance);
                enqueue(new Waiting<T>(null, v, newDistance));
            } else {
                say("is not less than dist[" + v + "] = " + old + ": no change.");
            }
        }

        Replay<T> finish(List<String> summary, T start) {
            begin("Done", -1, null);
            for (String line : summary) {
                say(line);
            }
            commit();
            return new Replay<>(algorithm, start, vertices, edges, steps);
        }

        Replay<T> fail(String message, T start) {
            steps.clear();
            open = false;
            begin("Cannot run " + algorithm, -1, null);
            say(message);
            commit();
            return new Replay<>(algorithm, start, vertices, edges, steps);
        }

        int treeCount() {
            return trees(vertices, edges, marks).size();
        }

        private void begin(String title, int current, T vertex) {
            commit();
            open = true;
            this.title = title;
            this.current = current;
            this.vertex = vertex;
            detail = new StringBuilder();
            fresh = new LinkedHashSet<>();
            queuedNow = new ArrayList<>();
        }

        private void say(String line) {
            if (detail.length() > 0) {
                detail.append('\n');
            }
            detail.append(line);
        }

        private void commit() {
            if (!open) {
                return;
            }
            if (!queuedNow.isEmpty()) {
                say("Queue its edges to vertices outside the tree:");
                say("  " + String.join(", ", queuedNow) + ".");
            }
            steps.add(new Step<>(title, detail.toString(), current, vertex, marks.clone(), fresh,
                    new LinkedHashSet<>(done), new HashMap<>(distance), new HashMap<>(previous),
                    queueLines(), weight));
            open = false;
        }

        private void enqueue(Waiting<T> entry) {
            int at = queue.size();
            while (at > 0 && queue.get(at - 1).key > entry.key) {
                at--;
            }
            queue.add(at, entry);
        }

        private void dequeueEdge(int edgeIndex) {
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i).edge != null && indexOf(queue.get(i).edge) == edgeIndex) {
                    queue.remove(i);
                    return;
                }
            }
        }

        private void dequeueVertex(T v, int key) {
            for (int i = 0; i < queue.size(); i++) {
                if (v.equals(queue.get(i).vertex) && queue.get(i).key == key) {
                    queue.remove(i);
                    return;
                }
            }
        }

        private List<String> queueLines() {
            List<String> lines = new ArrayList<>();
            if (algorithm == Algorithm.KRUSKAL) {
                for (int i = 0; i < sorted.size(); i++) {
                    Mark mark = marks[indexOf(sorted.get(i))];
                    String status = mark == Mark.TREE ? "added" : mark == Mark.SKIPPED ? "skipped, cycle" : "";
                    String arrow = open && current >= 0 && i == tried - 1 ? ">" : " ";
                    lines.add(String.format("%s %-12s %s", arrow, label(sorted.get(i)), status).replaceAll("\\s+$", ""));
                }
                return lines;
            }
            for (Waiting<T> w : queue) {
                if (w.edge != null) {
                    lines.add(String.format("%-12s %s", label(w.edge),
                            done.contains(w.edge.getDest()) ? "both ends in tree" : "").replaceAll("\\s+$", ""));
                } else {
                    lines.add(String.format("%-12s %s", "(" + w.vertex + ", " + w.key + ")",
                            done.contains(w.vertex) ? "old, " + w.vertex + " is settled" : "").replaceAll("\\s+$", ""));
                }
            }
            return lines;
        }

        private int indexOf(Edge<T> edge) {
            Integer i = index.get(Arrays.asList(edge.getSrc(), edge.getDest()));
            if (i == null) {
                throw new IllegalStateException("Edge not in the graph: " + edge);
            }
            return i;
        }

        private List<T> treeOf(T v) {
            for (List<T> tree : trees(vertices, edges, marks)) {
                if (tree.contains(v)) {
                    return tree;
                }
            }
            return Collections.singletonList(v);
        }
    }

    static <T> List<List<T>> trees(List<T> vertices, List<Edge<T>> edges, Mark[] marks) {
        Map<T, List<T>> adjacent = new HashMap<>();
        for (T v : vertices) {
            adjacent.put(v, new ArrayList<T>());
        }
        for (int i = 0; i < edges.size(); i++) {
            if (marks[i] == Mark.TREE) {
                adjacent.get(edges.get(i).getSrc()).add(edges.get(i).getDest());
                adjacent.get(edges.get(i).getDest()).add(edges.get(i).getSrc());
            }
        }
        List<List<T>> trees = new ArrayList<>();
        Set<T> seen = new HashSet<>();
        for (T start : vertices) {
            if (!seen.add(start)) {
                continue;
            }
            List<T> tree = new ArrayList<>();
            Deque<T> stack = new ArrayDeque<>();
            stack.push(start);
            while (!stack.isEmpty()) {
                T v = stack.pop();
                tree.add(v);
                for (T w : adjacent.get(v)) {
                    if (seen.add(w)) {
                        stack.push(w);
                    }
                }
            }
            tree.sort(Comparator.comparingInt(vertices::indexOf));
            trees.add(tree);
        }
        return trees;
    }

    private static final int RADIUS = 22;
    private static final int MARGIN = RADIUS * 2 + 16;
    private static final int TOP = 20;
    private static final int BOTTOM = 30;

    private static final Color TEXT = new Color(0x1F2933);
    private static final Color HIGHLIGHT = new Color(0xE8590C);
    private static final Color HALO = new Color(0xE8, 0x59, 0x0C, 50);
    private static final Color EDGE = new Color(0xB8C0C8);
    private static final Color TREE = new Color(0x2F9E44);
    private static final Color QUEUE = new Color(0x4C6EF5);
    private static final Color SKIP = new Color(0xE03131);
    private static final Color PATH = new Color(0xAE3EC9);
    private static final Color MUTED = new Color(0x7B8794);

    private static final Color[] PALETTE = {
        new Color(0x2F9E44), new Color(0x4C6EF5), new Color(0xAE3EC9), new Color(0x1098AD),
        new Color(0xF08C00), new Color(0x5C940D), new Color(0x862E9C), new Color(0xC2255C)
    };

    private final AbstractGraph<T> graph;
    private final List<T> vertices;
    private final Map<T, Point2D.Double> position = new HashMap<>();
    private Replay<T> replay;
    private int step;
    private T target;
    private T dragging;

    private final Canvas canvas;
    private final JComboBox<Algorithm> algorithmBox = new JComboBox<>(Algorithm.values());
    private final JComboBox<T> startBox;
    private final Controls controls = new Controls(300, 3000, 1200);
    private final JTextArea stepText = textArea();
    private final JTextArea queueText = textArea();
    private final JTextArea stateText = textArea();
    private final TitledBorder queueBorder = BorderFactory.createTitledBorder("Priority queue");
    private final JPanel panel = new JPanel(new BorderLayout());

    public WeightedGraphVisualizer(AbstractGraph<T> graph, Algorithm algorithm, T start, T target) {
        this.graph = graph;
        this.vertices = graph.getVertices();
        this.target = target;
        canvas = new Canvas();

        startBox = new JComboBox<>(new Vector<>(vertices));
        algorithmBox.setSelectedItem(algorithm);
        if (start != null && vertices.contains(start)) {
            startBox.setSelectedItem(start);
        }
        algorithmBox.addActionListener(ev -> load());
        startBox.addActionListener(ev -> load());

        JButton reset = button("Reset layout", () -> {
            canvas.circleLayout();
            canvas.repaint();
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(algorithmBox);
        toolbar.add(new JLabel("Start"));
        toolbar.add(startBox);
        toolbar.add(Box.createHorizontalStrut(12));
        controls.addTo(toolbar);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(reset);

        stepText.setRows(12);
        stepText.setLineWrap(true);
        stepText.setWrapStyleWord(true);
        stateText.setRows(11);
        stateText.setLineWrap(true);
        stateText.setWrapStyleWord(true);
        JPanel queuePane = new JPanel(new BorderLayout());
        queuePane.setBorder(queueBorder);
        queuePane.add(new JScrollPane(queueText), BorderLayout.CENTER);

        JPanel side = new JPanel(new BorderLayout(0, 8));
        side.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        side.setPreferredSize(new Dimension(390, 0));
        side.add(titled(new JScrollPane(stepText), "Step"), BorderLayout.NORTH);
        side.add(queuePane, BorderLayout.CENTER);
        side.add(titled(new JScrollPane(stateText), "State"), BorderLayout.SOUTH);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(canvas, BorderLayout.CENTER);
        panel.add(side, BorderLayout.EAST);
        controls.bindKeys(panel);
        load();
    }

    public static <T> void show(AbstractGraph<T> graph, String title, Algorithm algorithm, T start) {
        show(graph, title, algorithm, start, null);
    }

    public static <T> void show(AbstractGraph<T> graph, String title, Algorithm algorithm, T start, T target) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new WeightedGraphVisualizer<>(graph, algorithm, start, target).getPanel());
            frame.setSize(1200, 800);
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
        });
    }

    public JPanel getPanel() {
        return panel;
    }

    private void load() {
        Algorithm algorithm = algorithmBox.getItemAt(algorithmBox.getSelectedIndex());
        T start = startBox.getItemAt(startBox.getSelectedIndex());
        startBox.setEnabled(algorithm != Algorithm.KRUSKAL);
        replay = record(graph, algorithm, start);
        queueBorder.setTitle(algorithm == Algorithm.KRUSKAL ? "Edges, lightest first" : "Priority queue, front first");
        controls.reset(last(), replay.steps.size() > 1);
        canvas.repaint();
    }

    private int last() {
        return replay.steps.size() - 1;
    }

    private Step<T> shown() {
        return replay.steps.get(step);
    }

    private void showStep(int s, boolean forward) {
        step = s;
        Step<T> st = shown();
        stepText.setText("Step " + (step + 1) + " of " + replay.steps.size() + ": " + st.title + "\n\n" + st.detail);
        stepText.setCaretPosition(0);
        queueText.setText(st.queue.isEmpty() ? "empty" : String.join("\n", st.queue));
        queueText.setCaretPosition(0);
        stateText.setText(stateReport(st));
        stateText.setCaretPosition(0);
        canvas.repaint();
    }

    private String stateReport(Step<T> st) {
        StringBuilder sb = new StringBuilder();
        if (replay.algorithm == Algorithm.DIJKSTRA) {
            sb.append(String.format("%-6s %5s  %-5s%n", "Vertex", "dist", "prev"));
            for (T v : replay.vertices) {
                Integer d = st.distance.get(v);
                Integer e = st.previous.get(v);
                sb.append(String.format("%-6s %5s  %-5s %s%n", v, d == null ? "∞" : d,
                        e == null ? "-" : other(replay.edges.get(e), v), st.done.contains(v) ? "settled" : ""));
            }
            if (target != null) {
                List<T> path = pathTo(st, target);
                sb.append('\n').append("Path to ").append(target).append(": ");
                sb.append(path.isEmpty() ? "not found yet"
                        : arrows(path) + ", length " + st.distance.get(target)
                          + (st.done.contains(target) ? "" : " (may still get shorter)"));
            }
            return sb.toString();
        }
        List<Edge<T>> tree = new ArrayList<>();
        for (int i = 0; i < replay.edges.size(); i++) {
            if (st.marks[i] == Mark.TREE) {
                tree.add(replay.edges.get(i));
            }
        }
        sb.append("Tree weight  ").append(st.weight).append('\n');
        sb.append("Tree edges   ").append(tree.size()).append(" of ")
          .append(Math.max(0, replay.vertices.size() - 1)).append('\n');
        if (replay.algorithm == Algorithm.PRIM) {
            sb.append("In the tree  ").append(st.done.isEmpty() ? "-" : join(new ArrayList<>(st.done))).append('\n');
        } else {
            sb.append("Trees        ");
            for (List<T> t : trees(replay.vertices, replay.edges, st.marks)) {
                sb.append(braces(t)).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private List<T> pathTo(Step<T> st, T v) {
        List<T> path = new ArrayList<>();
        if (!st.distance.containsKey(v)) {
            return path;
        }
        for (T at = v; at != null; ) {
            path.add(0, at);
            Integer e = st.previous.get(at);
            at = e == null ? null : other(replay.edges.get(e), at);
        }
        return path;
    }

    private Set<Integer> pathEdges(Step<T> st) {
        Set<Integer> edges = new HashSet<>();
        if (replay.algorithm != Algorithm.DIJKSTRA || target == null) {
            return edges;
        }
        for (T at = target; st.previous.containsKey(at); ) {
            int e = st.previous.get(at);
            edges.add(e);
            at = other(replay.edges.get(e), at);
        }
        return edges;
    }

    private class Canvas extends JPanel {
        private static final long serialVersionUID = 1L;

        Canvas() {
            setBackground(Color.WHITE);
            circleLayout();
            MouseAdapter mouse = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    dragging = vertexAt(e.getPoint());
                    if (replay.algorithm == Algorithm.DIJKSTRA) {
                        target = dragging;
                        stateText.setText(stateReport(shown()));
                    }
                    repaint();
                }

                public void mouseReleased(MouseEvent e) {
                    dragging = null;
                }

                public void mouseDragged(MouseEvent e) {
                    if (dragging != null) {
                        position.put(dragging, toUnit(e.getPoint()));
                        repaint();
                    }
                }

                public void mouseMoved(MouseEvent e) {
                    T under = vertexAt(e.getPoint());
                    setCursor(Cursor.getPredefinedCursor(under == null
                            ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        public void removeNotify() {
            controls.stop();
            super.removeNotify();
        }

        private void circleLayout() {
            int n = vertices.size();
            for (int i = 0; i < n; i++) {
                double angle = -Math.PI / 2 + 2 * Math.PI * i / n;
                double x = n == 1 ? 0.5 : 0.5 + 0.5 * Math.cos(angle);
                double y = n == 1 ? 0.5 : 0.5 + 0.5 * Math.sin(angle);
                position.put(vertices.get(i), new Point2D.Double(x, y));
            }
        }

        private Point toScreen(T v) {
            Point2D.Double p = position.get(v);
            return new Point((int) Math.round(MARGIN + p.x * (getWidth() - 2 * MARGIN)),
                    (int) Math.round(MARGIN + TOP + p.y * (getHeight() - 2 * MARGIN - TOP - BOTTOM)));
        }

        private T vertexAt(Point p) {
            for (int i = vertices.size() - 1; i >= 0; i--) {
                if (toScreen(vertices.get(i)).distance(p) <= RADIUS) {
                    return vertices.get(i);
                }
            }
            return null;
        }

        private Point2D.Double toUnit(Point p) {
            double x = (p.x - MARGIN) / (double) Math.max(1, getWidth() - 2 * MARGIN);
            double y = (p.y - MARGIN - TOP) / (double) Math.max(1, getHeight() - 2 * MARGIN - TOP - BOTTOM);
            return new Point2D.Double(Math.max(0, Math.min(1, x)), Math.max(0, Math.min(1, y)));
        }

        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Step<T> st = shown();
            Map<T, Color> colors = vertexColors(st);
            Set<Integer> path = pathEdges(st);
            Set<Integer> tentative = new HashSet<>();
            for (Map.Entry<T, Integer> e : st.previous.entrySet()) {
                if (!st.done.contains(e.getKey())) {
                    tentative.add(e.getValue());
                }
            }

            for (int pass = 0; pass < 2; pass++) {
                for (int i = 0; i < replay.edges.size(); i++) {
                    boolean plain = st.marks[i] == Mark.NONE && i != st.current && !path.contains(i);
                    if (plain == (pass == 0)) {
                        drawEdge(g, i, st, path, tentative, colors);
                    }
                }
            }
            for (int i = 0; i < replay.edges.size(); i++) {
                drawWeight(g, i, st, path, colors);
            }
            for (T v : vertices) {
                drawVertex(g, v, st, colors);
            }
            drawCaption(g, st);
            drawLegend(g);
            g.dispose();
        }

        private Map<T, Color> vertexColors(Step<T> st) {
            Map<T, Color> colors = new HashMap<>();
            if (replay.algorithm == Algorithm.KRUSKAL) {
                for (List<T> tree : trees(replay.vertices, replay.edges, st.marks)) {
                    if (tree.size() > 1) {
                        Color c = PALETTE[vertices.indexOf(tree.get(0)) % PALETTE.length];
                        for (T v : tree) {
                            colors.put(v, c);
                        }
                    }
                }
                return colors;
            }
            for (T v : vertices) {
                if (st.done.contains(v)) {
                    colors.put(v, TREE);
                } else if (st.distance.containsKey(v)) {
                    colors.put(v, blend(TREE, Color.WHITE, 0.7));
                }
            }
            return colors;
        }

        private Color edgeColor(int i, Step<T> st, Set<Integer> path, Map<T, Color> colors) {
            if (i == st.current) {
                return HIGHLIGHT;
            }
            if (path.contains(i)) {
                return PATH;
            }
            switch (st.marks[i]) {
                case TREE:
                    Color c = colors.get(replay.edges.get(i).getSrc());
                    return replay.algorithm == Algorithm.KRUSKAL && c != null ? c : TREE;
                case QUEUED:
                    return QUEUE;
                case SKIPPED:
                    return blend(SKIP, Color.WHITE, 0.35);
                default:
                    return EDGE;
            }
        }

        private void drawEdge(Graphics2D g, int i, Step<T> st, Set<Integer> path, Set<Integer> tentative,
                              Map<T, Color> colors) {
            Edge<T> e = replay.edges.get(i);
            Mark mark = st.marks[i];
            float width = 1.5f;
            boolean dashed = false;
            if (i == st.current || path.contains(i)) {
                width = 5f;
            } else if (mark == Mark.TREE) {
                width = 4f;
                dashed = tentative.contains(i);
            } else if (mark == Mark.QUEUED) {
                width = st.fresh.contains(i) ? 3.5f : 2.5f;
                dashed = true;
            } else if (mark == Mark.SKIPPED) {
                width = 2f;
                dashed = true;
            }
            g.setColor(edgeColor(i, st, path, colors));
            g.setStroke(dashed
                    ? new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[] {9f, 7f}, 0f)
                    : new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Point a = toScreen(e.getSrc());
            Point b = toScreen(e.getDest());
            if (e.getSrc().equals(e.getDest())) {
                g.drawOval(a.x - RADIUS / 2, a.y - RADIUS * 2, RADIUS, (int) (RADIUS * 1.6));
            } else {
                g.drawLine(a.x, a.y, b.x, b.y);
            }
        }

        private void drawWeight(Graphics2D g, int i, Step<T> st, Set<Integer> path, Map<T, Color> colors) {
            Edge<T> e = replay.edges.get(i);
            Point a = toScreen(e.getSrc());
            Point b = toScreen(e.getDest());
            int x = (a.x + b.x) / 2;
            int y = e.getSrc().equals(e.getDest()) ? a.y - RADIUS * 2 : (a.y + b.y) / 2;
            boolean strong = st.marks[i] != Mark.NONE || i == st.current || path.contains(i);
            String text = String.valueOf(e.getWeight());
            g.setFont(getFont().deriveFont(strong ? Font.BOLD : Font.PLAIN, 13f));
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(text) + 10;
            int h = fm.getHeight();
            g.setColor(Color.WHITE);
            g.fillRoundRect(x - w / 2, y - h / 2, w, h, 8, 8);
            g.setStroke(new BasicStroke(strong ? 2f : 1f));
            g.setColor(strong ? edgeColor(i, st, path, colors) : EDGE);
            g.drawRoundRect(x - w / 2, y - h / 2, w, h, 8, 8);
            g.setColor(st.marks[i] == Mark.SKIPPED && i != st.current ? SKIP : TEXT);
            g.drawString(text, x - fm.stringWidth(text) / 2, y - h / 2 + fm.getAscent());
        }

        private void drawVertex(Graphics2D g, T v, Step<T> st, Map<T, Color> colors) {
            Point p = toScreen(v);
            boolean current = v.equals(st.vertex);
            if (current) {
                int r = RADIUS + 10;
                g.setColor(HALO);
                g.fillOval(p.x - r, p.y - r, 2 * r, 2 * r);
            }
            Color fill = colors.getOrDefault(v, Color.WHITE);
            g.setColor(fill);
            g.fillOval(p.x - RADIUS, p.y - RADIUS, 2 * RADIUS, 2 * RADIUS);
            boolean ring = current || v.equals(target) && replay.algorithm == Algorithm.DIJKSTRA;
            g.setStroke(new BasicStroke(ring ? 4f : 2f));
            g.setColor(ring ? (current ? HIGHLIGHT : PATH) : MUTED);
            g.drawOval(p.x - RADIUS, p.y - RADIUS, 2 * RADIUS, 2 * RADIUS);

            String label = String.valueOf(v);
            g.setFont(getFont().deriveFont(Font.BOLD, 15f));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(fill.equals(Color.WHITE) || brightness(fill) > 200 ? TEXT : Color.WHITE);
            g.drawString(label, p.x - fm.stringWidth(label) / 2, p.y + fm.getAscent() / 2 - 2);

            if (replay.algorithm == Algorithm.DIJKSTRA) {
                Integer d = st.distance.get(v);
                String text = d == null ? "∞" : String.valueOf(d);
                g.setFont(getFont().deriveFont(Font.BOLD, 13f));
                fm = g.getFontMetrics();
                int w = Math.max(fm.stringWidth(text) + 10, 22);
                int h = fm.getHeight();
                int x = p.x + RADIUS - 4;
                int y = p.y - RADIUS - h + 6;
                g.setColor(Color.WHITE);
                g.fillRoundRect(x, y, w, h, 10, 10);
                g.setStroke(new BasicStroke(st.done.contains(v) ? 2.5f : 1.2f));
                g.setColor(st.done.contains(v) ? TREE : d == null ? EDGE : MUTED);
                g.drawRoundRect(x, y, w, h, 10, 10);
                g.setColor(d == null ? MUTED : TEXT);
                g.drawString(text, x + (w - fm.stringWidth(text)) / 2, y + fm.getAscent());
            }
        }

        private void drawCaption(Graphics2D g, Step<T> st) {
            String what = replay.algorithm.toString()
                    + (replay.start == null ? "" : " from " + replay.start);
            g.setColor(TEXT);
            g.setFont(getFont().deriveFont(Font.BOLD, 15f));
            g.drawString(what + ": step " + (step + 1) + " of " + replay.steps.size() + ", " + st.title, 12, 22);
            if (replay.algorithm != Algorithm.DIJKSTRA) {
                g.setFont(getFont().deriveFont(Font.PLAIN, 14f));
                g.drawString("Tree weight " + st.weight, 12, 42);
            }
        }

        private void drawLegend(Graphics2D g) {
            List<Object[]> items = new ArrayList<>();
            items.add(new Object[] {HIGHLIGHT, false, "looked at now"});
            if (replay.algorithm == Algorithm.DIJKSTRA) {
                items.add(new Object[] {TREE, false, "best path edge, final"});
                items.add(new Object[] {TREE, true, "best path edge so far"});
                items.add(new Object[] {PATH, false, "path to the clicked vertex"});
            } else {
                items.add(new Object[] {TREE, false,
                        replay.algorithm == Algorithm.KRUSKAL ? "tree edge, one color per tree" : "tree edge"});
                if (replay.algorithm == Algorithm.PRIM) {
                    items.add(new Object[] {QUEUE, true, "in the priority queue"});
                }
                items.add(new Object[] {blend(SKIP, Color.WHITE, 0.35), true, "skipped, would close a cycle"});
            }
            g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            int x = 12;
            int y = getHeight() - 12;
            for (Object[] item : items) {
                boolean dashed = (Boolean) item[1];
                g.setColor((Color) item[0]);
                g.setStroke(dashed
                        ? new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, new float[] {6f, 5f}, 0f)
                        : new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.drawLine(x, y - 4, x + 26, y - 4);
                g.setColor(TEXT);
                String text = (String) item[2];
                g.drawString(text, x + 32, y);
                x += 32 + g.getFontMetrics().stringWidth(text) + 18;
            }
        }
    }

    private static <T> String label(Edge<T> e) {
        return e.getSrc() + "-" + e.getDest() + " (" + e.getWeight() + ")";
    }

    private static <T> String labels(List<Edge<T>> edges) {
        List<String> parts = new ArrayList<>();
        for (Edge<T> e : edges) {
            parts.add(label(e));
        }
        return parts.isEmpty() ? "none" : String.join(", ", parts);
    }

    private static <T> String braces(List<T> items) {
        return "{" + join(items) + "}";
    }

    private static <T> String arrows(List<T> path) {
        List<String> parts = new ArrayList<>();
        for (T v : path) {
            parts.add(String.valueOf(v));
        }
        return String.join(" -> ", parts);
    }

    private static int brightness(Color c) {
        return (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 1000;
    }

    private static <T> T other(Edge<T> e, T v) {
        return e.getSrc().equals(v) ? e.getDest() : e.getSrc();
    }

    private static <T> String join(List<T> items) {
        List<String> parts = new ArrayList<>();
        for (T item : items) {
            parts.add(String.valueOf(item));
        }
        return String.join(", ", parts);
    }

    private static Color blend(Color a, Color b, double t) {
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    private static JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.addActionListener(ev -> action.run());
        return button;
    }

    private static JTextArea textArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setMargin(new Insets(6, 6, 6, 6));
        return area;
    }

    private static JComponent titled(JComponent c, String title) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBorder(BorderFactory.createTitledBorder(title));
        box.add(c, BorderLayout.CENTER);
        return box;
    }

    private class Controls {

        private final JButton playButton = new JButton("Play");
        private final JSlider delay;
        private final JLabel delayLabel = new JLabel();
        private final Timer timer;
        private int at;
        private int lastStep;
        private boolean playing;

        Controls(int minDelayMs, int maxDelayMs, int delayMs) {
            delay = new JSlider(minDelayMs, maxDelayMs, delayMs);
            delay.setPreferredSize(new Dimension(130, delay.getPreferredSize().height));
            timer = new Timer(delayMs, ev -> {
                if (at < lastStep) {
                    show(at + 1, true);
                }
                if (at == lastStep) {
                    setPlaying(false);
                }
            });
            delay.addChangeListener(ev -> {
                timer.setDelay(delay.getValue());
                timer.setInitialDelay(delay.getValue());
                showDelay();
            });
            showDelay();
            playButton.addActionListener(ev -> setPlaying(!playing));
        }

        void addTo(JPanel toolbar) {
            toolbar.add(button("Restart", () -> {
                setPlaying(false);
                show(0, false);
            }));
            toolbar.add(button("Prev", () -> {
                setPlaying(false);
                show(at - 1, false);
            }));
            toolbar.add(playButton);
            toolbar.add(button("Next", () -> {
                setPlaying(false);
                show(at + 1, true);
            }));
            toolbar.add(Box.createHorizontalStrut(8));
            toolbar.add(new JLabel("Delay"));
            toolbar.add(delay);
            toolbar.add(delayLabel);
        }

        void bindKeys(JComponent component) {
            bind(component, KeyEvent.VK_LEFT, "prev step", -1);
            bind(component, KeyEvent.VK_RIGHT, "next step", 1);
        }

        void reset(int lastStep, boolean play) {
            this.lastStep = lastStep;
            setPlaying(false);
            show(0, false);
            setPlaying(play);
        }

        void stop() {
            timer.stop();
        }

        private void show(int s, boolean forward) {
            at = Math.max(0, Math.min(lastStep, s));
            showStep(at, forward);
        }

        private void setPlaying(boolean on) {
            if (on && at == lastStep) {
                show(0, false);
            }
            playing = on;
            playButton.setText(on ? "Pause" : "Play");
            if (on) {
                timer.restart();
            } else {
                timer.stop();
            }
        }

        private void showDelay() {
            delayLabel.setText(String.format("%.1f s", delay.getValue() / 1000.0));
        }

        private void bind(JComponent component, int key, String name, int direction) {
            component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke(key, 0), name);
            component.getActionMap().put(name, new AbstractAction() {
                private static final long serialVersionUID = 1L;

                public void actionPerformed(ActionEvent e) {
                    setPlaying(false);
                    show(at + direction, direction > 0);
                }
            });
        }
    }

}
