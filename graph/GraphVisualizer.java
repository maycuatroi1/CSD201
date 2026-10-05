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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

public class GraphVisualizer<T> {
    public enum Traversal { DFS, BFS }

    private static final int RADIUS = 22;
    private static final int GROW_MS = 450;
    private static final Color EDGE = new Color(0x9AA5B1);
    private static final Color EDGE_SOFT = new Color(0xCDD3DA);
    private static final Color EDGE_FADED = new Color(0xE1E5EA);
    private static final Color HIGHLIGHT = new Color(0xE8590C);
    private static final Color HALO = new Color(0xE8, 0x59, 0x0C, 50);
    private static final Color TEXT = new Color(0x1F2933);
    private static final Color[] PALETTE = {
        new Color(0x4C6EF5), new Color(0x2F9E44), new Color(0xAE3EC9),
        new Color(0x1098AD), new Color(0xF08C00), new Color(0xE03131),
        new Color(0x5C940D), new Color(0x862E9C)
    };

    private final List<T> vertices;
    private final List<Edge<T>> edges;
    private final boolean weighted;
    private final Map<T, List<Edge<T>>> incident = new HashMap<>();
    private final Map<T, Set<T>> neighbors = new HashMap<>();
    private final Map<T, Integer> degree = new HashMap<>();
    private final Map<T, Integer> componentOf = new HashMap<>();
    private final List<List<T>> components = new ArrayList<>();
    private final Map<T, Point2D.Double> position = new HashMap<>();

    private final List<T> order;
    private final Map<T, Integer> visitIndex = new HashMap<>();
    private final Traversal requested;
    private final Traversal kind;
    private final boolean alsoFitsBfs;
    private final Map<T, T> parent;
    private int step;
    private boolean playing;
    private boolean animateStep;
    private long stepStartedAt;
    private long waitStartedAt;

    private final Canvas canvas = new Canvas();
    private final JTable table;
    private final JTextArea details = textArea();
    private final JTextArea traversalText = textArea();
    private final JCheckBox showWeights = new JCheckBox("Show weights");
    private final JCheckBox showDegrees = new JCheckBox("Show degrees", true);
    private final JButton playButton = new JButton("Play");
    private final JSlider delay = new JSlider(200, 2000, 1000);
    private final Timer ticker = new Timer(16, ev -> tick());
    private T selected;
    private T dragging;
    private final JPanel panel = new JPanel(new BorderLayout());

    public GraphVisualizer(AbstractGraph<T> graph) {
        this(graph, Collections.<T>emptyList(), null);
    }

    public GraphVisualizer(AbstractGraph<T> graph, List<T> traversal, Traversal kind) {
        Set<T> vertexSet = new LinkedHashSet<>(graph.getVertices());
        edges = graph.getEdges();
        for (Edge<T> e : edges) {
            vertexSet.add(e.getSrc());
            vertexSet.add(e.getDest());
        }
        vertices = new ArrayList<>(vertexSet);
        weighted = edges.stream().anyMatch(e -> e.getWeight() != 1);

        for (T v : vertices) {
            incident.put(v, new ArrayList<>());
            neighbors.put(v, new LinkedHashSet<>());
            degree.put(v, 0);
        }
        for (Edge<T> e : edges) {
            incident.get(e.getSrc()).add(e);
            neighbors.get(e.getSrc()).add(e.getDest());
            neighbors.get(e.getDest()).add(e.getSrc());
            degree.merge(e.getSrc(), 1, Integer::sum);
            if (!e.getSrc().equals(e.getDest())) {
                incident.get(e.getDest()).add(e);
            }
            degree.merge(e.getDest(), 1, Integer::sum);
        }
        findComponents();
        circleLayout();

        order = new ArrayList<>(traversal);
        for (int i = 0; i < order.size(); i++) {
            T v = order.get(i);
            if (!neighbors.containsKey(v)) {
                throw new IllegalArgumentException("Traversal visits a vertex that is not in the graph: " + v);
            }
            if (visitIndex.put(v, i) != null) {
                throw new IllegalArgumentException("Traversal visits " + v + " twice");
            }
        }
        requested = kind;
        Traversal found = null;
        Map<T, T> tree = null;
        if (!order.isEmpty()) {
            for (Traversal candidate : kind == null ? Traversal.values() : new Traversal[] {kind}) {
                tree = treeEdges(candidate);
                if (tree != null) {
                    found = candidate;
                    break;
                }
            }
        }
        this.kind = found;
        parent = tree == null ? Collections.<T, T>emptyMap() : tree;
        alsoFitsBfs = kind == null && found == Traversal.DFS && treeEdges(Traversal.BFS) != null;

        table = new JTable(vertexTableModel());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);
        int[] widths = {55, 55, 190, 50};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.getSelectionModel().addListSelectionListener(ev -> {
            int row = table.getSelectedRow();
            if (!ev.getValueIsAdjusting() && row >= 0) {
                select(vertices.get(table.convertRowIndexToModel(row)));
            }
        });

        showWeights.setSelected(weighted);
        showWeights.addActionListener(ev -> canvas.repaint());
        showDegrees.addActionListener(ev -> canvas.repaint());
        JButton reset = new JButton("Reset layout");
        reset.addActionListener(ev -> {
            circleLayout();
            canvas.repaint();
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(showWeights);
        toolbar.add(showDegrees);
        toolbar.add(reset);
        if (!order.isEmpty()) {
            addReplayControls(toolbar);
        }

        JTextArea summary = textArea();
        summary.setText(summary());
        details.setText("Click a vertex to see its connections.");
        details.setRows(order.isEmpty() ? 9 : 5);
        traversalText.setRows(8);
        traversalText.setLineWrap(true);
        traversalText.setWrapStyleWord(true);

        JPanel bottom = new JPanel(new BorderLayout(0, 8));
        if (!order.isEmpty()) {
            bottom.add(titled(new JScrollPane(traversalText), "Traversal"), BorderLayout.NORTH);
        }
        bottom.add(titled(new JScrollPane(details), "Selected vertex"), BorderLayout.CENTER);

        JPanel side = new JPanel(new BorderLayout(0, 8));
        side.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        side.setPreferredSize(new Dimension(360, 0));
        side.add(titled(summary, "Graph"), BorderLayout.NORTH);
        side.add(titled(new JScrollPane(table), "Vertices"), BorderLayout.CENTER);
        side.add(bottom, BorderLayout.SOUTH);

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(canvas, BorderLayout.CENTER);
        panel.add(side, BorderLayout.EAST);

        if (!order.isEmpty()) {
            setStep(0, false);
            setPlaying(true);
        }
    }

    public static <T> void show(AbstractGraph<T> graph, String title) {
        show(graph, title, Collections.<T>emptyList(), null);
    }

    public static <T> void show(AbstractGraph<T> graph) {
        show(graph, "Graph Visualizer");
    }

    public static <T> void show(AbstractGraph<T> graph, String title, List<T> traversal) {
        show(graph, title, traversal, null);
    }

    public static <T> void show(AbstractGraph<T> graph, String title, List<T> traversal, Traversal kind) {
        List<T> copy = new ArrayList<>(traversal);
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new GraphVisualizer<>(graph, copy, kind).getPanel());
            frame.setSize(1100, 760);
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
        });
    }

    public JPanel getPanel() {
        return panel;
    }

    private void findComponents() {
        for (T start : vertices) {
            if (componentOf.containsKey(start)) {
                continue;
            }
            List<T> members = new ArrayList<>();
            Deque<T> queue = new ArrayDeque<>();
            queue.add(start);
            componentOf.put(start, components.size());
            while (!queue.isEmpty()) {
                T v = queue.poll();
                members.add(v);
                for (T next : neighbors.get(v)) {
                    if (!componentOf.containsKey(next)) {
                        componentOf.put(next, components.size());
                        queue.add(next);
                    }
                }
            }
            members.sort(Comparator.comparingInt(vertices::indexOf));
            components.add(members);
        }
    }

    private String summary() {
        int n = vertices.size();
        int m = edges.size();
        long totalWeight = 0;
        for (Edge<T> e : edges) {
            totalWeight += e.getWeight();
        }
        int degreeSum = 0;
        int minDegree = Integer.MAX_VALUE;
        int maxDegree = 0;
        List<T> isolated = new ArrayList<>();
        List<T> odd = new ArrayList<>();
        for (T v : vertices) {
            int d = degree.get(v);
            degreeSum += d;
            minDegree = Math.min(minDegree, d);
            maxDegree = Math.max(maxDegree, d);
            if (d == 0) {
                isolated.add(v);
            }
            if (d % 2 == 1) {
                odd.add(v);
            }
        }
        double density = n < 2 ? 0 : 2.0 * m / ((double) n * (n - 1));

        StringBuilder sb = new StringBuilder();
        sb.append("Vertices      ").append(n).append('\n');
        sb.append("Edges         ").append(m).append('\n');
        if (weighted) {
            sb.append("Total weight  ").append(totalWeight).append('\n');
        }
        sb.append(String.format("Density       %.2f%n", density));
        if (n > 0) {
            sb.append("Degree        min ").append(minDegree).append(", max ").append(maxDegree)
              .append(", sum ").append(degreeSum).append(" = 2 x ").append(m).append('\n');
        }
        sb.append("Components    ").append(components.size())
          .append(components.size() == 1 ? " (connected)" : " (not connected)").append('\n');
        sb.append("Isolated      ").append(isolated.isEmpty() ? "none" : join(isolated)).append('\n');
        sb.append("Odd degree    ").append(odd.isEmpty() ? "none" : join(odd)).append('\n');
        sb.append("Euler         ").append(eulerStatus(odd));
        return sb.toString();
    }

    private String eulerStatus(List<T> odd) {
        if (edges.isEmpty()) {
            return "no edges";
        }
        long componentsWithEdges = components.stream()
            .filter(c -> c.stream().anyMatch(v -> degree.get(v) > 0))
            .count();
        if (componentsWithEdges > 1) {
            return "none (edges in " + componentsWithEdges + " components)";
        }
        if (odd.isEmpty()) {
            return "cycle exists";
        }
        if (odd.size() == 2) {
            return "path " + odd.get(0) + " -> " + odd.get(1) + ", no cycle";
        }
        return "none (" + odd.size() + " odd vertices)";
    }

    private DefaultTableModel vertexTableModel() {
        DefaultTableModel model = new DefaultTableModel(
                new Object[] {"Vertex", "Degree", "Neighbors", "Comp."}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (T v : vertices) {
            model.addRow(new Object[] {v, degree.get(v), neighborText(v), "#" + (componentOf.get(v) + 1)});
        }
        return model;
    }

    private String neighborText(T v) {
        List<String> parts = new ArrayList<>();
        for (Edge<T> e : incident.get(v)) {
            parts.add(weighted ? other(e, v) + " (" + e.getWeight() + ")" : String.valueOf(other(e, v)));
        }
        return parts.isEmpty() ? "-" : String.join(", ", parts);
    }

    private void select(T v) {
        if (Objects.equals(selected, v)) {
            return;
        }
        selected = v;
        if (v == null) {
            table.clearSelection();
            details.setText("Click a vertex to see its connections.");
        } else {
            int row = table.convertRowIndexToView(vertices.indexOf(v));
            table.setRowSelectionInterval(row, row);
            table.scrollRectToVisible(table.getCellRect(row, 0, true));
            details.setText(vertexDetails(v));
            details.setCaretPosition(0);
        }
        canvas.repaint();
    }

    private String vertexDetails(T v) {
        int d = degree.get(v);
        int c = componentOf.get(v);
        StringBuilder sb = new StringBuilder();
        sb.append("Vertex     ").append(v).append('\n');
        sb.append("Degree     ").append(d).append(d % 2 == 0 ? " (even)" : " (odd)").append('\n');
        sb.append("Component  #").append(c + 1).append(": ").append(join(components.get(c))).append('\n');
        if (!order.isEmpty()) {
            Integer i = visitIndex.get(v);
            sb.append("Visit      ").append(i == null ? "never" : "#" + (i + 1));
            if (parent.containsKey(v)) {
                sb.append(" (from ").append(parent.get(v)).append(')');
            }
            sb.append('\n');
        }
        sb.append("Edges").append(incident.get(v).isEmpty() ? "      none" : "").append('\n');
        for (Edge<T> e : incident.get(v)) {
            sb.append("  ").append(v).append(" - ").append(other(e, v))
              .append("   weight ").append(e.getWeight()).append('\n');
        }
        return sb.toString();
    }

    private Map<T, T> treeEdges(Traversal k) {
        Map<T, T> tree = new HashMap<>();
        Set<T> seen = new HashSet<>();
        Deque<T> pending = new ArrayDeque<>();
        for (T v : order) {
            while (!pending.isEmpty()) {
                T u = k == Traversal.DFS ? pending.peekLast() : pending.peekFirst();
                if (neighbors.get(u).contains(v)) {
                    tree.put(v, u);
                    break;
                }
                for (T w : neighbors.get(u)) {
                    if (!seen.contains(w)) {
                        return null;
                    }
                }
                if (k == Traversal.DFS) {
                    pending.pollLast();
                } else {
                    pending.pollFirst();
                }
            }
            seen.add(v);
            pending.addLast(v);
        }
        return tree;
    }

    private void addReplayControls(JPanel toolbar) {
        JButton restart = new JButton("Restart");
        restart.addActionListener(ev -> {
            setPlaying(false);
            setStep(0, false);
        });
        JButton prev = new JButton("Prev");
        prev.addActionListener(ev -> {
            setPlaying(false);
            setStep(step - 1, false);
        });
        JButton next = new JButton("Next");
        next.addActionListener(ev -> {
            setPlaying(false);
            setStep(step + 1, true);
        });
        playButton.addActionListener(ev -> setPlaying(!playing));

        JLabel delayLabel = new JLabel("1.0 s");
        delay.setPreferredSize(new Dimension(130, delay.getPreferredSize().height));
        delay.addChangeListener(ev -> delayLabel.setText(String.format("%.1f s", delay.getValue() / 1000.0)));

        toolbar.add(Box.createHorizontalStrut(16));
        toolbar.add(restart);
        toolbar.add(prev);
        toolbar.add(playButton);
        toolbar.add(next);
        toolbar.add(Box.createHorizontalStrut(8));
        toolbar.add(new JLabel("Delay"));
        toolbar.add(delay);
        toolbar.add(delayLabel);
    }

    private void setStep(int s, boolean animate) {
        step = Math.max(0, Math.min(order.size(), s));
        animateStep = animate;
        stepStartedAt = System.nanoTime();
        waitStartedAt = stepStartedAt;
        traversalText.setText(traversalReport());
        traversalText.setCaretPosition(0);
        ticker.start();
        canvas.repaint();
    }

    private void setPlaying(boolean on) {
        if (on && step == order.size()) {
            setStep(0, false);
        }
        playing = on;
        playButton.setText(on ? "Pause" : "Play");
        if (on) {
            waitStartedAt = System.nanoTime();
            ticker.start();
        }
    }

    private void tick() {
        if (playing && (System.nanoTime() - waitStartedAt) / 1_000_000 >= delay.getValue()) {
            setStep(step + 1, true);
            if (step == order.size()) {
                setPlaying(false);
            }
        }
        if (!playing && progress() >= 1) {
            ticker.stop();
        }
        canvas.repaint();
    }

    private double progress() {
        if (!animateStep) {
            return 1;
        }
        double ms = (System.nanoTime() - stepStartedAt) / 1e6;
        return Math.min(1, ms / Math.min(GROW_MS, delay.getValue() * 0.6));
    }

    private String traversalReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(kind == null ? "Visit order" : kind.name()).append(" from ").append(order.get(0)).append('\n');
        if (kind == null) {
            sb.append("Note       not a valid ").append(requested == null ? "DFS or BFS" : requested.name())
              .append(" order, no tree edges\n");
        } else if (alsoFitsBfs) {
            sb.append("Note       also a valid BFS order\n");
        }
        sb.append("Step       ").append(step).append(" of ").append(order.size()).append('\n');
        if (step > 0) {
            T current = order.get(step - 1);
            sb.append("Visit      ").append(current);
            if (parent.containsKey(current)) {
                sb.append(" from ").append(parent.get(current));
            } else if (step > 1) {
                sb.append(" (new start)");
            }
            sb.append('\n');
            if (kind == Traversal.DFS) {
                LinkedList<T> stack = new LinkedList<>();
                for (T v = current; v != null; v = parent.get(v)) {
                    stack.addFirst(v);
                }
                sb.append("Stack      ").append(join(stack)).append("  (top: ").append(current).append(")\n");
            } else if (kind == Traversal.BFS) {
                List<T> queue = new ArrayList<>();
                for (T v : order.subList(step, order.size())) {
                    if (parent.containsKey(v) && visitIndex.get(parent.get(v)) < step) {
                        queue.add(v);
                    }
                }
                sb.append("Queue      ").append(queue.isEmpty() ? "empty" : join(queue)).append("  (front first)\n");
            }
        }
        sb.append("Order      ").append(step == 0 ? "-" : join(order.subList(0, step))).append('\n');
        if (step == order.size()) {
            List<T> missed = new ArrayList<>();
            for (T v : vertices) {
                if (!visitIndex.containsKey(v)) {
                    missed.add(v);
                }
            }
            sb.append("Not reached ").append(missed.isEmpty() ? "none" : join(missed));
        }
        return sb.toString();
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

    private class Canvas extends JPanel {
        private static final long serialVersionUID = 1L;

        Canvas() {
            setBackground(Color.WHITE);
            MouseAdapter mouse = new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    dragging = vertexAt(e.getPoint());
                    select(dragging);
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
                    T v = vertexAt(e.getPoint());
                    setCursor(Cursor.getPredefinedCursor(v == null ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
                    setToolTipText(v == null ? null : v + ": degree " + degree.get(v));
                }
            };
            addMouseListener(mouse);
            addMouseMotionListener(mouse);
        }

        public void removeNotify() {
            ticker.stop();
            super.removeNotify();
        }

        private int margin() {
            return RADIUS * 2 + 12;
        }

        private Point toScreen(T v) {
            Point2D.Double p = position.get(v);
            int m = margin();
            return new Point((int) Math.round(m + p.x * (getWidth() - 2 * m)),
                             (int) Math.round(m + p.y * (getHeight() - 2 * m)));
        }

        private Point2D.Double toUnit(Point p) {
            int m = margin();
            double x = (p.x - m) / (double) Math.max(1, getWidth() - 2 * m);
            double y = (p.y - m) / (double) Math.max(1, getHeight() - 2 * m);
            return new Point2D.Double(Math.max(0, Math.min(1, x)), Math.max(0, Math.min(1, y)));
        }

        private T vertexAt(Point p) {
            for (int i = vertices.size() - 1; i >= 0; i--) {
                T v = vertices.get(i);
                if (toScreen(v).distance(p) <= RADIUS) {
                    return v;
                }
            }
            return null;
        }

        private boolean isActive(Edge<T> e) {
            return selected == null || e.getSrc().equals(selected) || e.getDest().equals(selected);
        }

        private boolean isActive(T v) {
            return selected == null || v.equals(selected) || neighbors.get(selected).contains(v);
        }

        private boolean replaying() {
            return selected == null && !order.isEmpty();
        }

        private boolean isCurrent(T v) {
            return replaying() && step > 0 && order.get(step - 1).equals(v);
        }

        private double fade(T v) {
            if (selected != null) {
                return isActive(v) ? 0 : 0.75;
            }
            if (order.isEmpty()) {
                return 0;
            }
            int i = visitIndex.getOrDefault(v, Integer.MAX_VALUE);
            if (i >= step) {
                return 0.75;
            }
            return i == step - 1 ? 0.75 * (1 - ease(progress())) : 0;
        }

        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (replaying()) {
                for (Edge<T> e : edges) {
                    drawEdge(g, e, EDGE_SOFT, 1.5f);
                }
                for (int i = 0; i < step; i++) {
                    T v = order.get(i);
                    if (parent.containsKey(v)) {
                        drawTreeEdge(g, parent.get(v), v, i == step - 1 ? ease(progress()) : 1);
                    }
                }
            } else {
                for (Edge<T> e : edges) {
                    if (!isActive(e)) {
                        drawEdge(g, e, EDGE_FADED, 1.5f);
                    }
                }
                for (Edge<T> e : edges) {
                    if (isActive(e)) {
                        drawEdge(g, e, selected == null ? EDGE : HIGHLIGHT, selected == null ? 2f : 3.5f);
                    }
                }
            }
            if (showWeights.isSelected()) {
                for (Edge<T> e : edges) {
                    drawWeight(g, e);
                }
            }
            for (T v : vertices) {
                drawVertex(g, v);
            }
            if (!order.isEmpty()) {
                drawCaption(g);
            }
            if (vertices.isEmpty()) {
                g.setColor(TEXT);
                g.drawString("The graph has no vertices.", 20, 30);
            }
            g.dispose();
        }

        private void drawEdge(Graphics2D g, Edge<T> e, Color color, float width) {
            Point a = toScreen(e.getSrc());
            Point b = toScreen(e.getDest());
            g.setColor(color);
            g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (e.getSrc().equals(e.getDest())) {
                g.drawOval(a.x - RADIUS / 2, a.y - RADIUS * 2, RADIUS, (int) (RADIUS * 1.6));
            } else {
                g.drawLine(a.x, a.y, b.x, b.y);
            }
        }

        private void drawTreeEdge(Graphics2D g, T from, T to, double t) {
            Point a = toScreen(from);
            Point b = toScreen(to);
            double len = a.distance(b);
            if (t <= 0 || len <= 2 * RADIUS) {
                return;
            }
            double ux = (b.x - a.x) / len;
            double uy = (b.y - a.y) / len;
            double sx = a.x + ux * RADIUS;
            double sy = a.y + uy * RADIUS;
            double reach = (len - 2 * RADIUS) * t;
            double ex = sx + ux * reach;
            double ey = sy + uy * reach;

            g.setColor(HIGHLIGHT);
            g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            if (reach > 8) {
                g.draw(new Line2D.Double(sx, sy, ex - ux * 8, ey - uy * 8));
            }
            Path2D.Double head = new Path2D.Double();
            head.moveTo(ex, ey);
            head.lineTo(ex - ux * 13 - uy * 6.5, ey - uy * 13 + ux * 6.5);
            head.lineTo(ex - ux * 13 + uy * 6.5, ey - uy * 13 - ux * 6.5);
            head.closePath();
            g.fill(head);
        }

        private void drawWeight(Graphics2D g, Edge<T> e) {
            Point a = toScreen(e.getSrc());
            Point b = toScreen(e.getDest());
            int x = (a.x + b.x) / 2;
            int y = e.getSrc().equals(e.getDest()) ? a.y - RADIUS * 2 : (a.y + b.y) / 2;
            String text = String.valueOf(e.getWeight());
            g.setFont(getFont().deriveFont(Font.PLAIN, 12f));
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(text) + 8;
            int h = fm.getHeight();
            g.setColor(Color.WHITE);
            g.fillRoundRect(x - w / 2, y - h / 2, w, h, 8, 8);
            g.setColor(isActive(e) ? TEXT : EDGE_FADED.darker());
            g.drawRoundRect(x - w / 2, y - h / 2, w, h, 8, 8);
            g.drawString(text, x - fm.stringWidth(text) / 2, y - h / 2 + fm.getAscent());
        }

        private void drawVertex(Graphics2D g, T v) {
            Point p = toScreen(v);
            Color fill = blend(PALETTE[componentOf.get(v) % PALETTE.length], Color.WHITE, fade(v));
            boolean current = isCurrent(v);
            if (current) {
                int r = RADIUS + 9;
                g.setColor(HALO);
                g.fillOval(p.x - r, p.y - r, 2 * r, 2 * r);
            }
            g.setColor(fill);
            g.fillOval(p.x - RADIUS, p.y - RADIUS, 2 * RADIUS, 2 * RADIUS);
            boolean ring = v.equals(selected) || current;
            g.setStroke(new BasicStroke(ring ? 4f : 2f));
            g.setColor(ring ? HIGHLIGHT : Color.WHITE);
            g.drawOval(p.x - RADIUS, p.y - RADIUS, 2 * RADIUS, 2 * RADIUS);

            String label = String.valueOf(v);
            g.setFont(getFont().deriveFont(Font.BOLD, 15f));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(Color.WHITE);
            g.drawString(label, p.x - fm.stringWidth(label) / 2, p.y + fm.getAscent() / 2 - 2);

            if (showDegrees.isSelected()) {
                drawBadge(g, String.valueOf(degree.get(v)), p.x + (int) (RADIUS * 0.75),
                          p.y - (int) (RADIUS * 0.75), Color.WHITE, fill, TEXT);
            }
            Integer i = visitIndex.get(v);
            if (i != null && i < step) {
                drawBadge(g, String.valueOf(i + 1), p.x - (int) (RADIUS * 0.75),
                          p.y + (int) (RADIUS * 0.75), HIGHLIGHT, Color.WHITE, Color.WHITE);
            }
        }

        private void drawBadge(Graphics2D g, String text, int x, int y, Color fill, Color border, Color ink) {
            int r = 9;
            g.setFont(getFont().deriveFont(Font.BOLD, 11f));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(fill);
            g.fillOval(x - r, y - r, 2 * r, 2 * r);
            g.setStroke(new BasicStroke(1.5f));
            g.setColor(border);
            g.drawOval(x - r, y - r, 2 * r, 2 * r);
            g.setColor(ink);
            g.drawString(text, x - fm.stringWidth(text) / 2, y + fm.getAscent() / 2 - 1);
        }

        private void drawCaption(Graphics2D g) {
            String name = kind == null ? "Visit order" : kind.name();
            g.setColor(TEXT);
            g.setFont(getFont().deriveFont(Font.BOLD, 15f));
            g.drawString(name + " from " + order.get(0) + ": step " + step + " of " + order.size(), 12, 22);
            g.setFont(getFont().deriveFont(Font.PLAIN, 14f));
            g.drawString("Visited: " + (step == 0 ? "-" : join(order.subList(0, step))), 12, getHeight() - 12);
        }
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

    private static double ease(double t) {
        return t * t * (3 - 2 * t);
    }

    private static Color blend(Color a, Color b, double t) {
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                         (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                         (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t));
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
}
