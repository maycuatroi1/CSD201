package queue;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;
import java.util.RandomAccess;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import list.Node;
import stack.Stack;

public class HogwartsVisualizer {

    static final int LIMIT = 400;
    static final String[] NUMERALS = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

    private static final ArrayList<Story> STORIES = new ArrayList<>();
    private static boolean opened;

    private HogwartsVisualizer() {
    }

    public static <T> Queue<T> watch(Queue<T> queue) {
        if (queue instanceof WatchedQueue) {
            return queue;
        }
        Story story = begin(Shape.QUEUE, queue, queue.getSize());
        story.start(queue::getSize, queue::getStorage);
        return new WatchedQueue<>(queue, story);
    }

    public static <T> Stack<T> watch(Stack<T> stack) {
        if (stack instanceof WatchedStack) {
            return stack;
        }
        Story story = begin(Shape.STACK, stack, stack.getSize());
        story.start(stack::getSize, stack::getStorage);
        return new WatchedStack<>(stack, story);
    }

    private static synchronized Story begin(Shape shape, Object structure, int size) {
        if (size != 0) {
            throw new IllegalArgumentException("watch() needs an empty " + shape.name().toLowerCase()
                    + ": wrap it right after new, before the first call");
        }
        if (STORIES.size() == NUMERALS.length) {
            throw new IllegalStateException("keys 1-9 pick a story: 9 at most");
        }
        StackTraceElement site = callSite();
        Story story = new Story(shape, structure.getClass().getSimpleName(), STORIES.size(),
                site == null ? null : site.getFileName(), site == null ? 0 : site.getLineNumber());
        STORIES.add(story);
        if (!opened) {
            opened = true;
            open();
        }
        return story;
    }

    static synchronized ArrayList<Story> stories() {
        return new ArrayList<>(STORIES);
    }

    private static void open() {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("(no display, so no animation)");
            return;
        }
        SwingUtilities.invokeLater(() -> {
            Screen screen = new Screen(new Stage());
            JFrame frame = new JFrame("Hogwarts: Queue and Stack");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(screen);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            screen.requestFocusInWindow();
            screen.start();
        });
    }

    static StackTraceElement callSite() {
        String me = HogwartsVisualizer.class.getName();
        for (StackTraceElement e : new Throwable().getStackTrace()) {
            String c = e.getClassName();
            if (!c.equals(me) && !c.startsWith(me + "$")) {
                return e;
            }
        }
        return null;
    }

    static String clean(String s) {
        if (s == null) {
            return "";
        }
        String t = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .replace('đ', 'd').replace('Đ', 'D').replaceAll("\\s+", " ").trim().toUpperCase();
        return t.length() > 120 ? t.substring(0, 119) + "." : t;
    }

    enum Shape { QUEUE, STACK }

    enum Op { START, ENQUEUE, DEQUEUE, PUSH, POP, PEEK, EMPTY }

    static final class WatchedQueue<T> extends Queue<T> {
        private final Queue<T> real;
        private final Story story;

        WatchedQueue(Queue<T> real, Story story) {
            this.real = real;
            this.story = story;
        }

        public int getSize() {
            return real.getSize();
        }

        public void enqueue(T element) {
            story.call(Op.ENQUEUE, element, () -> {
                real.enqueue(element);
                return null;
            });
        }

        public T dequeue() {
            return story.call(Op.DEQUEUE, null, real::dequeue);
        }

        public T peek() {
            return story.call(Op.PEEK, null, real::peek);
        }

        public boolean isEmpty() {
            return story.call(Op.EMPTY, null, real::isEmpty);
        }

        public Iterable<T> getStorage() {
            return real.getStorage();
        }

        public List<T> toList() {
            return real.toList();
        }

        public String toString() {
            return real.toString();
        }
    }

    static final class WatchedStack<T> extends Stack<T> {
        private final Stack<T> real;
        private final Story story;

        WatchedStack(Stack<T> real, Story story) {
            this.real = real;
            this.story = story;
        }

        public int getSize() {
            return real.getSize();
        }

        public void push(T element) {
            story.call(Op.PUSH, element, () -> {
                real.push(element);
                return null;
            });
        }

        public T pop() {
            return story.call(Op.POP, null, real::pop);
        }

        public T peek() {
            return story.call(Op.PEEK, null, real::peek);
        }

        public boolean isEmpty() {
            return story.call(Op.EMPTY, null, real::isEmpty);
        }

        public Iterable<T> getStorage() {
            return real.getStorage();
        }

        public List<T> toList() {
            return real.toList();
        }

        public String toString() {
            return real.toString();
        }
    }

    static final class Token {
        final int id;
        final Object value;
        final String text;
        final String name;
        final String detail;
        final int look;
        final int house;

        Token(int id, Object value, String text, String name, String detail, int look, int house) {
            this.id = id;
            this.value = value;
            this.text = text;
            this.name = name;
            this.detail = detail;
            this.look = look;
            this.house = house;
        }
    }

    static final class Inside {
        final String error;
        final boolean linked;
        final String kind;
        final int total;
        final int[] cells;
        final String[] labels;
        final boolean[] live;
        final boolean[] broken;
        final int first;
        final int last;
        final int tail;
        final int lost;

        Inside(String error) {
            this(error, false, "", 0, new int[0], new String[0], new boolean[0], new boolean[0], -1, -1, -1, -1);
        }

        Inside(String error, boolean linked, String kind, int total, int[] cells, String[] labels, boolean[] live,
                boolean[] broken, int first, int last, int tail, int lost) {
            this.error = error;
            this.linked = linked;
            this.kind = kind;
            this.total = total;
            this.cells = cells;
            this.labels = labels;
            this.live = live;
            this.broken = broken;
            this.first = first;
            this.last = last;
            this.tail = tail;
            this.lost = lost;
        }
    }

    static final class Step {
        final Op op;
        final int[] line;
        final int token;
        final int held;
        final String result;
        final String error;
        final String message;
        final String warning;
        final Inside inside;
        final int size;
        final String file;
        final int lineNo;

        Step(Op op, int[] line, int token, int held, String result, String error, String message, String warning,
                Inside inside, int size, String file, int lineNo) {
            this.op = op;
            this.line = line;
            this.token = token;
            this.held = held;
            this.result = result;
            this.error = error;
            this.message = message;
            this.warning = warning;
            this.inside = inside;
            this.size = size;
            this.file = file;
            this.lineNo = lineNo;
        }
    }

    static final class Story {
        static final int CELLS = 64;
        final Shape shape;
        final String type;
        final int number;
        final String file;
        final int line;
        final Thread owner = Thread.currentThread();
        private IntSupplier sizer;
        private Supplier<Iterable<?>> lister;
        private final ArrayList<Step> steps = new ArrayList<>();
        private final ArrayList<Token> tokens = new ArrayList<>();
        private final IdentityHashMap<Object, Integer> latest = new IdentityHashMap<>();
        private final ArrayList<Integer> model = new ArrayList<>();
        private String element = "";
        private int peak;
        private boolean cut;

        Story(Shape shape, String type, int number, String file, int line) {
            this.shape = shape;
            this.type = clean(type);
            this.number = number;
            this.file = file;
            this.line = line;
        }

        synchronized int size() {
            return steps.size();
        }

        synchronized Step step(int i) {
            return steps.get(i);
        }

        synchronized Token token(int id) {
            return tokens.get(id);
        }

        synchronized int peak() {
            return peak;
        }

        synchronized boolean cut() {
            return cut;
        }

        synchronized String title() {
            return element.isEmpty() ? type : type + "<" + element + ">";
        }

        boolean queue() {
            return shape == Shape.QUEUE;
        }

        synchronized void start(IntSupplier sizer, Supplier<Iterable<?>> lister) {
            this.sizer = sizer;
            this.lister = lister;
            steps.add(new Step(Op.START, new int[0], -1, -1, "", null, "", null, inside(), 0, file, line));
        }

        synchronized <R> R call(Op op, Object arg, Supplier<R> work) {
            if (steps.size() >= LIMIT) {
                cut = true;
                return work.get();
            }
            StackTraceElement site = callSite();
            boolean adds = op == Op.ENQUEUE || op == Op.PUSH;
            int added = adds ? token(arg) : -1;
            int end = queue() ? 0 : model.size() - 1;
            int expected = model.isEmpty() ? -1 : model.get(end);
            R result = null;
            Throwable failure = null;
            try {
                result = work.get();
            } catch (RuntimeException | Error e) {
                failure = e;
            }
            int involved = adds ? added : expected;
            int held = -1;
            String warning = null;
            if (failure == null) {
                if (adds) {
                    model.add(added);
                } else if (op == Op.EMPTY) {
                    if (Boolean.TRUE.equals(result) != model.isEmpty()) {
                        warning = model.isEmpty() ? "BUT NOTHING IS " + place()
                                : "BUT " + model.size() + (model.size() == 1 ? " IS " : " ARE ") + place();
                    }
                } else {
                    warning = check(result, expected);
                    if (op != Op.PEEK && expected >= 0) {
                        model.remove(end);
                        held = expected;
                    }
                }
            }
            int size = -1;
            String sizeError = null;
            try {
                size = sizer.getAsInt();
            } catch (RuntimeException e) {
                sizeError = clean(e.getClass().getSimpleName());
            }
            if (failure == null && warning == null && sizeError != null) {
                warning = "BUT GETSIZE() THREW " + sizeError;
            } else if (failure == null && warning == null && size != model.size()) {
                warning = "BUT GETSIZE() SAYS " + size + ", NOT " + model.size();
            }
            Inside in = inside();
            if (failure == null && warning == null && in.lost >= 0) {
                warning = "BUT " + tokens.get(in.lost).text + " IS NO LONGER INSIDE " + type;
            }
            peak = Math.max(peak, model.size());
            int[] now = new int[model.size()];
            for (int k = 0; k < now.length; k++) {
                now[k] = model.get(k);
            }
            steps.add(new Step(op, now, involved, held, failure == null ? label(result) : "",
                    failure == null ? null : clean(failure.getClass().getSimpleName()),
                    failure == null ? "" : clean(failure.getMessage()), warning, in, size,
                    site == null ? null : site.getFileName(), site == null ? 0 : site.getLineNumber()));
            if (failure instanceof RuntimeException) {
                throw (RuntimeException) failure;
            }
            if (failure != null) {
                throw (Error) failure;
            }
            return result;
        }

        String place() {
            return queue() ? "IN LINE" : "ON THE TROLLEY";
        }

        private int token(Object value) {
            String text = clean(String.valueOf(value));
            String name = text;
            String detail = "";
            int p = text.indexOf(" (");
            if (p > 0 && text.endsWith(")")) {
                name = text.substring(0, p);
                detail = text.substring(p + 2, text.length() - 1);
            }
            if (element.isEmpty() && value != null) {
                element = clean(value.getClass().getSimpleName());
            }
            int id = tokens.size();
            tokens.add(new Token(id, value, text, name, detail, Math.floorMod(text.hashCode(), 997),
                    Math.floorMod(name.hashCode() * 31 + 17, 4)));
            if (value != null) {
                latest.put(value, id);
            }
            return id;
        }

        private String check(Object result, int expected) {
            if (expected < 0) {
                return result == null ? null : "BUT THE " + (queue() ? "QUEUE" : "STACK") + " WAS EMPTY";
            }
            Object want = tokens.get(expected).value;
            if (result == want || result != null && result.equals(want)) {
                return null;
            }
            return "BUT THE " + (queue() ? "FRONT OF THE LINE" : "TOP TRUNK") + " WAS " + tokens.get(expected).text;
        }

        private String label(Object v) {
            if (v == null) {
                return "NULL";
            }
            if (v instanceof Boolean) {
                return v.toString().toUpperCase();
            }
            Integer id = latest.get(v);
            return id != null ? tokens.get(id).text : clean(String.valueOf(v));
        }

        private Inside inside() {
            Iterable<?> raw;
            try {
                raw = lister.get();
            } catch (RuntimeException e) {
                return new Inside("GETSTORAGE() THREW " + clean(e.getClass().getSimpleName()));
            }
            if (raw == null) {
                return new Inside("GETSTORAGE() RETURNED NULL");
            }
            ArrayList<Object> values = new ArrayList<>();
            boolean linked = raw instanceof list.List;
            boolean[] broken = new boolean[0];
            int tail = -1;
            int total = 0;
            String kind;
            try {
                if (linked) {
                    list.List<?> chain = (list.List<?>) raw;
                    ArrayList<Node<?>> nodes = new ArrayList<>();
                    IdentityHashMap<Object, Boolean> seen = new IdentityHashMap<>();
                    for (Node<?> n = chain.getHead(); n != null && seen.put(n, true) == null && nodes.size() < CELLS;
                            n = n.getNext()) {
                        nodes.add(n);
                        values.add(n.getData());
                    }
                    total = nodes.size();
                    broken = new boolean[Math.max(0, total - 1)];
                    for (int i = 0; i + 1 < total; i++) {
                        broken[i] = nodes.get(i + 1).getPrev() != nodes.get(i);
                    }
                    tail = chain.getTail() == null ? -3 : -1;
                    for (int i = 0; i < total; i++) {
                        if (nodes.get(i) == chain.getTail()) {
                            tail = i;
                        }
                    }
                    kind = article(clean(raw.getClass().getSimpleName()));
                } else {
                    for (Object o : raw) {
                        if (values.size() < CELLS) {
                            values.add(o);
                        }
                        if (++total > 100000) {
                            break;
                        }
                    }
                    kind = raw instanceof RandomAccess ? "AN ARRAY OF " + total
                            : article(clean(raw.getClass().getSimpleName())) + " OF " + total;
                }
            } catch (RuntimeException e) {
                return new Inside("WALKING GETSTORAGE() THREW " + clean(e.getClass().getSimpleName()));
            }
            int n = values.size();
            int k = model.size();
            int[] cells = new int[n];
            String[] labels = new String[n];
            for (int i = 0; i < n; i++) {
                Object v = values.get(i);
                Integer id = v == null ? null : latest.get(v);
                cells[i] = v == null ? -1 : id == null ? -2 : id;
                labels[i] = v == null ? "NULL" : id == null ? clean(String.valueOf(v)) : tokens.get(id).name;
            }
            boolean[] live = new boolean[n];
            int[] at = match(values, !linked && n == total);
            int first = -1;
            int last = -1;
            if (at != null) {
                for (int j = 0; j < k; j++) {
                    live[at[j]] = true;
                }
                first = at[0];
                last = at[k - 1];
            } else {
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < k && !live[i]; j++) {
                        live[i] = values.get(i) != null && values.get(i) == tokens.get(model.get(j)).value;
                    }
                }
            }
            int lost = -1;
            for (int j = 0; j < k && lost < 0 && n == total; j++) {
                boolean found = false;
                for (int i = 0; i < n && !found; i++) {
                    found = values.get(i) == tokens.get(model.get(j)).value;
                }
                lost = found ? -1 : model.get(j);
            }
            return new Inside(null, linked, kind, total, cells, labels, live, broken, first, last, tail, lost);
        }

        private int[] match(ArrayList<Object> values, boolean circular) {
            int n = values.size();
            int k = model.size();
            if (k == 0 || k > n) {
                return null;
            }
            for (int dir = 1; dir >= -1; dir -= 2) {
                for (int f = 0; f < n; f++) {
                    int[] at = new int[k];
                    boolean ok = true;
                    for (int j = 0; j < k && ok; j++) {
                        int idx = f + dir * j;
                        if (circular) {
                            idx = Math.floorMod(idx, n);
                        }
                        ok = idx >= 0 && idx < n && values.get(idx) == tokens.get(model.get(j)).value;
                        at[j] = idx;
                    }
                    if (ok) {
                        return at;
                    }
                }
            }
            return null;
        }

        static String article(String word) {
            return ("AEIOU".indexOf(word.isEmpty() ? 'X' : word.charAt(0)) >= 0 ? "AN " : "A ") + word;
        }
    }

    static final class Pix {
        static final int W = 320, H = 180;
        private static final Map<Character, Integer> GLYPHS = new HashMap<>();

        static {
            String[] defs = {
                "A010101111101101", "B110101110101110", "C011100100100011", "D110101101101110",
                "E111100110100111", "F111100110100100", "G011100101101011", "H101101111101101",
                "I111010010010111", "J001001001101010", "K101101110101101", "L100100100100111",
                "M101111111101101", "N110101101101101", "O010101101101010", "P110101110100100",
                "Q010101101110011", "R110101110101101", "S011100010001110", "T111010010010010",
                "U101101101101111", "V101101101101010", "W101101111111101", "X101101010101101",
                "Y101101010010010", "Z111001010100111", "0111101101101111", "1010110010010111",
                "2110001010100111", "3110001010001110", "4101101111001001", "5111100110001110",
                "6011100110101010", "7111001010010010", "8010101010101010", "9111101111001110",
                " 000000000000000", ".000000000000010", ",000000000010100", ":000010000010000",
                ";000010000010100", "-000000111000000", "+000010111010000", "=000111000111000",
                "<001010100010001", ">100010001010100", "(010100100100010", ")010001001001010",
                "[110100100100110", "]011001001001011", "/001001010100100", "!010010010000010",
                "?110001010000010", "'010010000000000", "|010010010010010", "_000000000000111",
                "#101111101111101", "*000101010101000", "\"101101000000000", "&010101010101011",
                "%101001010100101", "@111101111100111",
            };
            for (String d : defs) {
                GLYPHS.put(d.charAt(0), Integer.parseInt(d.substring(1), 2));
            }
        }

        final BufferedImage image = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        final int[] px = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        boolean ghost;

        void set(int x, int y, int c) {
            if (x < 0 || y < 0 || x >= W || y >= H || (ghost && ((x + y) & 1) != 0)) {
                return;
            }
            px[y * W + x] = c;
        }

        void rect(int x, int y, int w, int h, int c) {
            for (int j = y; j < y + h; j++) {
                for (int i = x; i < x + w; i++) {
                    set(i, j, c);
                }
            }
        }

        void line(int x0, int y0, int x1, int y1, int c) {
            int dx = Math.abs(x1 - x0), dy = -Math.abs(y1 - y0), sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1, err = dx + dy;
            while (true) {
                set(x0, y0, c);
                if (x0 == x1 && y0 == y1) {
                    return;
                }
                int e2 = 2 * err;
                if (e2 >= dy) { err += dy; x0 += sx; }
                if (e2 <= dx) { err += dx; y0 += sy; }
            }
        }

        void disc(int cx, int cy, int r, int c) {
            for (int y = -r; y <= r; y++) {
                for (int x = -r; x <= r; x++) {
                    if (x * x + y * y <= r * r + r) {
                        set(cx + x, cy + y, c);
                    }
                }
            }
        }

        void darken(int y0, int y1) {
            for (int i = y0 * W; i < (y1 + 1) * W; i++) {
                px[i] = (px[i] >> 1) & 0x7f7f7f;
            }
        }

        int text(String s, int x, int y, int c, int scale) {
            for (char ch : s.toUpperCase().toCharArray()) {
                int bits = GLYPHS.getOrDefault(ch, GLYPHS.get('?'));
                for (int i = 0; i < 15; i++) {
                    if (((bits >> (14 - i)) & 1) != 0) {
                        rect(x + (i % 3) * scale, y + (i / 3) * scale, scale, scale, c);
                    }
                }
                x += 4 * scale;
            }
            return x;
        }

        static int width(String s, int scale) {
            return s.isEmpty() ? 0 : (s.length() * 4 - 1) * scale;
        }
    }

    static final class Stage {
        static final int W = Pix.W, H = Pix.H;
        static final int TOP = 24, PANEL = 124, BOTTOM = 152;
        static final int FLOOR = 105, LINE_X = 44, LINE_END = 292, DOOR_X = 308, EXIT_X = -12;
        static final int STOOL_X = 24, SEAT = 96, HAT_REST = 83, HAT_LIFT = 63, HAT_HEAD = 73;
        static final int STACK_X = 32, TRUNK_W = 76, BASE = 99, ROOF = 28, HOVER_X = 110, HOVER_Y = 52;
        static final int HELD = -1, ABSENT = -2;
        static final double CARD_TIME = 3.2, HOLD = 4.5;
        static final double[] SPEEDS = {0.25, 0.5, 1, 2, 4};

        static final int INK = 0x0c0a12, PARCH = 0xf1e3c2, DIM = 0x8f8170, FAINT = 0x5d5262,
                GOLD = 0xf2c14e, BRONZE = 0xc8893a, TEAL = 0x7fe0cf, RED = 0xff6a4a, LUMOS = 0xfff6c8,
                MAP = 0xd9c49a, MAP_LT = 0xe6d4ac, MAP_DK = 0xc4ad80, MAP_EDGE = 0x8a7048, MAP_INK = 0x3a2414,
                MAP_FAINT = 0xa08a62, MAP_RED = 0xa02a1a, MAP_FILL = 0xf3e6c4, MAP_GOLD = 0xf0c050;
        static final String[] HOUSES = {"GRYFFINDOR", "SLYTHERIN", "RAVENCLAW", "HUFFLEPUFF"};
        static final int[] HOUSE_MAIN = {0xa3262a, 0x1f6b3a, 0x2a4a8a, 0xe0b52a};
        static final int[] HOUSE_TRIM = {0xe8b13a, 0xb8c0c8, 0xb07a3a, 0x2a2420};
        static final int[] HOUSE_TEXT = {0xff7a5a, 0x6ad08a, 0x8ab0ff, 0xffd84a};
        static final int[] HAIR = {0x2a1a12, 0x6a3a1a, 0xd8b050, 0xb0441a, 0x111018};
        static final int[] SKIN = {0xf0c8a0, 0xd8a070, 0xa06a40, 0x6a4028};
        static final int ROBE = 0x1c1a24, ROBE_LT = 0x3a3446, SHIRT = 0xe8e0d0, TIE = 0x8a8a90, LEGS = 0x3a3440,
                SHOES = 0x0e0c10, EYES = 0x1a1018;
        static final int[] TRUNK = {0x6a3a1e, 0x6a1e22, 0x24402a, 0x1e2a48, 0x5a4630};
        static final int[] TRUNK_LT = {0x8a5230, 0x8a3034, 0x34583a, 0x2e3e64, 0x7a6244};
        static final int BRASS = 0xd8a840, STRAP = 0x2a1608, TAG = 0xe8d8b0, TAG_INK = 0x2a1a0e;
        static final int[] CEILING = {0x07081a, 0x0c0e26, 0x141432, 0x1c1a3e};
        static final int[] STONE = {0x332d3f, 0x37314a, 0x2f2a3a};
        static final int[] BRICK = {0x7a3426, 0x6e2e22, 0x84402c};
        static final int[][] BAYER = {{0, 8, 2, 10}, {12, 4, 14, 6}, {3, 11, 1, 9}, {15, 7, 13, 5}};

        static final String[] STUDENT = {
            "..hhhhh..",
            ".hhhhhhh.",
            ".hfffffh.",
            ".fefffef.",
            ".fffffff.",
            "..fffff..",
            "..rwtwr..",
            ".rrrtrrr.",
            "rrrrtrrrr",
            "frrrtrrrf",
            ".rrrrrrr.",
            ".rrrrrrr.",
            ".RrrrrrR.",
            "..ll.ll..",
            "..ll.ll..",
            ".sss.sss.",
        };
        static final String[] WALK = {
            "..hhhhh..",
            ".hhhhhhh.",
            ".hfffffh.",
            ".fefffef.",
            ".fffffff.",
            "..fffff..",
            "..rwtwr..",
            ".rrrtrrr.",
            "rrrrtrrrr",
            "frrrtrrrf",
            ".rrrrrrr.",
            ".rrrrrrr.",
            ".RrrrrrR.",
            "..ll.ll..",
            ".ll...ll.",
            "ss.....ss",
        };
        static final String[] HAT = {
            "......kk.......",
            ".....kbbk......",
            "......kbbk.....",
            ".......kbk.....",
            "......kbbbk....",
            ".....kbbpbbk...",
            ".....kdbbbdk...",
            "....kbbbbbbbk..",
            "....kbddddbbk..",
            "...kbbbbbbbbbk.",
            ".kkbbbpbbbbbbkk",
            "kbbbbbbbbbbbbbk",
            ".kkkkkkkkkkkkk.",
        };

        static final class Walker {
            final int id;
            final double x;
            final boolean sitting;
            final boolean moving;
            final boolean sorted;
            final boolean glow;

            Walker(int id, double x, boolean sitting, boolean moving, boolean sorted, boolean glow) {
                this.id = id;
                this.x = x;
                this.sitting = sitting;
                this.moving = moving;
                this.sorted = sorted;
                this.glow = glow;
            }
        }

        static final class Crate {
            final int id;
            final double x;
            final double y;
            final boolean moving;
            final boolean parked;

            Crate(int id, double x, double y, boolean moving, boolean parked) {
                this.id = id;
                this.x = x;
                this.y = y;
                this.moving = moving;
                this.parked = parked;
            }
        }

        static final class Seg {
            final String text;
            final int color;

            Seg(String text, int color) {
                this.text = text;
                this.color = color;
            }
        }

        final Pix pix = new Pix();
        final int[] hall = new int[W * H];
        final int[] platform = new int[W * H];
        final ArrayList<int[]> stars = new ArrayList<>();
        final ArrayList<int[]> candles = new ArrayList<>();

        Story story;
        int storyIndex, from, to, speed = 1;
        double progress = 1, time, wait, card = CARD_TIME, finale;
        boolean playing;

        Stage() {
            Random art = new Random(7);
            for (int i = 0; i < 45; i++) {
                stars.add(new int[] {art.nextInt(W), TOP + 1 + art.nextInt(24), art.nextInt(100), art.nextBoolean() ? 0xd8d0f0 : 0x8a80b0});
            }
            for (int i = 0; i < 16; i++) {
                candles.add(new int[] {36 + art.nextInt(256), 29 + art.nextInt(16), art.nextInt(100)});
            }
            paintHall();
            System.arraycopy(pix.px, 0, hall, 0, hall.length);
            paintPlatform();
            System.arraycopy(pix.px, 0, platform, 0, platform.length);
            story = stories().get(0);
        }


        int last() {
            return story.size() - 1;
        }

        boolean waiting() {
            return story.owner.isAlive();
        }

        boolean done() {
            return to == last() && progress >= 1 && !waiting();
        }

        void go(int k) {
            k = Math.max(0, Math.min(last(), k));
            if (k == to) {
                return;
            }
            from = to;
            to = k;
            progress = 0;
            wait = 0;
        }

        void open(int index) {
            storyIndex = index;
            story = stories().get(index);
            from = to = 0;
            progress = 1;
            wait = finale = 0;
            card = CARD_TIME;
            playing = false;
        }

        double anim() {
            return Math.min(1.3, 0.65 / SPEEDS[speed]);
        }

        double period() {
            return Math.max(anim(), 1 / SPEEDS[speed]);
        }

        void key(int code, char ch) {
            int count = stories().size();
            if (code == KeyEvent.VK_SPACE) {
                if (card > 0) {
                    card = 0;
                    playing = true;
                    wait = 0;
                } else if (done()) {
                    from = to = 0;
                    progress = 1;
                    wait = finale = 0;
                    playing = true;
                } else {
                    playing = !playing;
                }
            } else if (code == KeyEvent.VK_RIGHT) {
                card = 0;
                playing = false;
                go(to + 1);
            } else if (code == KeyEvent.VK_LEFT) {
                card = 0;
                playing = false;
                go(to - 1);
            } else if (code == KeyEvent.VK_N) {
                open((storyIndex + 1) % count);
            } else if (code == KeyEvent.VK_R) {
                open(storyIndex);
            } else {
                int digit = code >= KeyEvent.VK_1 && code <= KeyEvent.VK_9 ? code - KeyEvent.VK_1
                        : code >= KeyEvent.VK_NUMPAD1 && code <= KeyEvent.VK_NUMPAD9 ? code - KeyEvent.VK_NUMPAD1 : -1;
                if (digit >= 0 && digit < count) {
                    open(digit);
                }
                if (ch == '+' || ch == '=') {
                    speed = Math.min(SPEEDS.length - 1, speed + 1);
                }
                if (ch == '-' || ch == '_') {
                    speed = Math.max(0, speed - 1);
                }
            }
        }

        void update(double dt) {
            time += dt;
            wait += dt;
            if (progress < 1) {
                progress = Math.min(1, progress + dt / anim());
            }
            if (card > 0) {
                card -= dt;
                if (card <= 0) {
                    card = 0;
                    playing = true;
                    wait = 0;
                }
            } else if (playing && progress >= 1 && wait >= period() && to < last()) {
                go(to + 1);
            }
            finale = done() ? finale + dt : 0;
            if (playing && finale > HOLD && storyIndex + 1 < stories().size()) {
                open(storyIndex + 1);
            }
        }


        BufferedImage render() {
            Step a = story.step(from);
            Step b = story.step(to);
            Step shown = progress >= 0.5 ? b : a;
            if (story.queue()) {
                hallScene(a, b, progress, shown);
            } else {
                platformScene(a, b, progress, shown);
            }
            drawInside(shown);
            drawFrame(shown);
            if (card > 0) {
                drawCard();
            }
            return pix.image;
        }

        static double clamp01(double v) {
            return Math.max(0, Math.min(1, v));
        }

        static double smooth(double u) {
            return u * u * (3 - 2 * u);
        }

        static double lerp(double a, double b, double u) {
            return a + (b - a) * u;
        }

        static int clamp(int v, int lo, int hi) {
            return Math.max(lo, Math.min(hi, v));
        }

        static int dither(int[] stops, double u, int x, int y) {
            double f = clamp01(u) * (stops.length - 1);
            int i = Math.min((int) f, stops.length - 2);
            return (f - i) * 16 > BAYER[y & 3][x & 3] + 0.5 ? stops[i + 1] : stops[i];
        }

        static int where(Step s, int id) {
            if (s.held == id) {
                return HELD;
            }
            for (int k = 0; k < s.line.length; k++) {
                if (s.line[k] == id) {
                    return k;
                }
            }
            return ABSENT;
        }

        static ArrayList<Integer> cast(Step a, Step b) {
            ArrayList<Integer> ids = new ArrayList<>();
            for (Step s : new Step[] {a, b}) {
                for (int id : s.line) {
                    if (!ids.contains(id)) {
                        ids.add(id);
                    }
                }
                if (s.held >= 0 && !ids.contains(s.held)) {
                    ids.add(s.held);
                }
            }
            return ids;
        }


        void paintHall() {
            Arrays.fill(pix.px, INK);
            for (int y = TOP; y < 50; y++) {
                for (int x = 0; x < W; x++) {
                    pix.px[y * W + x] = dither(CEILING, (y - TOP) / 26.0, x, y);
                }
            }
            for (int y = 50; y < 101; y++) {
                int row = (y - 50) / 6, off = (row & 1) * 5;
                for (int x = 0; x < W; x++) {
                    boolean mortar = (y - 50) % 6 == 5 || (x + off) % 10 == 9;
                    pix.px[y * W + x] = mortar ? 0x221e2b : STONE[Math.floorMod(row * 31 + (x + off) / 10 * 17, 3)];
                }
            }
            pix.rect(0, 50, W, 1, 0x4a4258);
            pix.rect(0, 51, W, 1, 0x3e3850);
            for (int cx : new int[] {96, 160, 224}) {
                window(cx);
            }
            for (int i = 0; i < 4; i++) {
                banner(64 + 64 * i, i);
            }
            for (int y = 101; y < PANEL; y++) {
                for (int x = 0; x < W; x++) {
                    int r = (y - 101) / 7;
                    boolean seam = (y - 101) % 7 == 6 || (x + r * 9) % 18 == 17;
                    pix.px[y * W + x] = seam ? 0x1c1824 : ((x / 18 + r) & 1) == 0 ? 0x2c2736 : 0x29242f;
                }
            }
            for (int y = 101; y <= 107; y++) {
                for (int x = 0; x < 297; x++) {
                    pix.set(x, y, y == 101 || y == 107 ? 0x8a6a2a : (x + y) % 6 == 0 ? 0x521c26 : 0x4a1820);
                }
            }
            door();
            pix.rect(STOOL_X - 7, SEAT, 15, 2, 0x8a5a2e);
            pix.rect(STOOL_X - 7, SEAT, 15, 1, 0xa87440);
            pix.rect(STOOL_X - 6, SEAT + 2, 2, FLOOR - SEAT - 1, 0x6a4020);
            pix.rect(STOOL_X + 5, SEAT + 2, 2, FLOOR - SEAT - 1, 0x6a4020);
            pix.rect(STOOL_X - 1, SEAT + 2, 2, FLOOR - SEAT - 2, 0x5a3418);
            pix.rect(STOOL_X - 5, 102, 11, 1, 0x5a3418);
            paintPanel();
        }

        void window(int cx) {
            for (int y = 55; y <= 92; y++) {
                int hw = y < 63 ? (int) Math.round(6 * Math.sin((y - 55) / 8.0 * Math.PI / 2)) : 6;
                for (int x = cx - hw; x <= cx + hw; x++) {
                    int dx = x - cx;
                    boolean frame = Math.abs(dx) == hw || y == 55 || y == 92;
                    boolean lead = Math.floorMod(dx + y, 4) == 0 || Math.floorMod(dx - y, 4) == 0;
                    pix.set(x, y, frame ? 0x4a4258 : lead ? 0x101830 : dither(new int[] {0x3a5a9a, 0x2a4a8a, 0x1c2c5e}, (y - 55) / 37.0, x, y));
                }
            }
        }

        void banner(int cx, int house) {
            pix.rect(cx - 8, 52, 17, 1, 0x9a7a3a);
            for (int y = 53; y <= 76; y++) {
                for (int x = cx - 6; x <= cx + 6; x++) {
                    int dx = Math.abs(x - cx);
                    if (y > 72 && dx < (y - 72) * 2) {
                        continue;
                    }
                    pix.set(x, y, dx == 6 || y == 53 ? HOUSE_TRIM[house] : HOUSE_MAIN[house]);
                }
            }
            for (int dy = -2; dy <= 2; dy++) {
                for (int dx = -(2 - Math.abs(dy)); dx <= 2 - Math.abs(dy); dx++) {
                    pix.set(cx + dx, 62 + dy, HOUSE_TRIM[house]);
                }
            }
        }

        void door() {
            for (int y = 58; y <= 107; y++) {
                int hw = y < 68 ? (int) Math.round(11 * Math.sin((y - 58) / 10.0 * Math.PI / 2)) : 11;
                for (int x = DOOR_X - hw - 2; x <= DOOR_X + hw + 2; x++) {
                    int dx = x - DOOR_X;
                    int c;
                    if (Math.abs(dx) > hw) {
                        c = 0x4a4258;
                    } else if (dx == 0) {
                        c = 0x24140a;
                    } else if (y == 74 || y == 75 || y == 95 || y == 96) {
                        c = Math.floorMod(x, 4) == 0 ? 0x6a6a74 : 0x2a2a30;
                    } else {
                        c = Math.floorMod(dx, 3) == 0 ? 0x3a2010 : 0x4e2e18;
                    }
                    pix.set(x, y, c);
                }
            }
            pix.set(DOOR_X - 3, 85, BRASS);
            pix.set(DOOR_X + 3, 85, BRASS);
        }

        void paintPanel() {
            for (int y = PANEL; y < BOTTOM; y++) {
                for (int x = 0; x < W; x++) {
                    int h = Math.floorMod(x * 73 + y * 151 + x * y, 97);
                    int c = h < 6 ? MAP_DK : h < 14 ? MAP_LT : MAP;
                    if (y == PANEL || y == BOTTOM - 1 || x == 0 || x == W - 1) {
                        c = MAP_EDGE;
                    }
                    pix.px[y * W + x] = c;
                }
            }
        }

        void paintPlatform() {
            Arrays.fill(pix.px, INK);
            for (int y = TOP; y < 58; y++) {
                for (int x = 0; x < W; x++) {
                    pix.px[y * W + x] = (y % 8 == 3 || x % 16 == 0) ? 0x1e2638 : dither(new int[] {0x0e1220, 0x161c2c}, (y - TOP) / 34.0, x, y);
                }
            }
            for (int k = 0; k <= 1; k++) {
                pix.line(3, 57 - k, 160, 29 - k, 0x0a0c14);
                pix.line(160, 29 - k, 316, 57 - k, 0x0a0c14);
            }
            for (int x = 15; x < 310; x += 12) {
                int y = x <= 160 ? 57 - (x - 3) * 28 / 157 : 29 + (x - 160) * 28 / 156;
                pix.rect(x, y, 1, 57 - y, 0x0a0c14);
            }
            pix.rect(0, 57, W, 3, 0x232838);
            for (int x = 3; x < W; x += 8) {
                pix.set(x, 58, 0x3a4258);
            }
            for (int y = 60; y < 100; y++) {
                int row = (y - 60) / 4, off = (row & 1) * 4;
                for (int x = 0; x < W; x++) {
                    boolean mortar = (y - 60) % 4 == 3 || (x + off) % 8 == 7;
                    pix.px[y * W + x] = mortar ? 0x3a1c16 : BRICK[Math.floorMod(row * 7 + (x + off) / 8 * 13, 3)];
                }
            }
            pix.rect(0, TOP, 4, 76, 0x232838);
            pix.rect(1, TOP, 1, 76, 0x323a4c);
            pix.rect(W - 4, TOP, 4, 76, 0x232838);
            pix.rect(W - 3, TOP, 1, 76, 0x323a4c);
            for (int y = 24; y < 30; y += 2) {
                pix.set(122, y, 0x6a6a72);
                pix.set(178, y, 0x6a6a72);
            }
            pix.rect(116, 30, 69, 12, 0xb08a3a);
            pix.rect(117, 31, 67, 10, 0x14141c);
            pix.text("PLATFORM 9 3/4", 123, 33, PARCH, 1);
            locomotive();
            for (int y = 100; y < PANEL; y++) {
                for (int x = 0; x < W; x++) {
                    boolean seam = (y - 100) % 8 == 7 || (x + ((y - 100) / 8) * 11) % 24 == 23;
                    pix.px[y * W + x] = seam ? 0x36333e : ((x / 24 + (y - 100) / 8) & 1) == 0 ? 0x4e4a56 : 0x46424e;
                }
            }
            for (int x = 0; x < W; x++) {
                pix.set(x, 101, (x / 4 & 1) == 0 ? 0xc8a030 : 0x8a7024);
            }
            pix.rect(STACK_X - 2, 100, TRUNK_W + 4, 2, 0x7a7e88);
            pix.rect(STACK_X - 2, 100, TRUNK_W + 4, 1, 0x9a9ea8);
            pix.rect(27, 69, 2, 33, 0x7a7e88);
            pix.rect(22, 68, 7, 2, 0x8a2020);
            for (int cx : new int[] {STACK_X + 6, STACK_X + TRUNK_W - 6}) {
                pix.disc(cx, 104, 2, 0x1a1a20);
                pix.set(cx, 104, 0x6a6e78);
            }
            paintPanel();
        }

        void locomotive() {
            int red = 0xb0161e, redLt = 0xd83a3a, redDk = 0x7a0e12, black = 0x1c1a22, gold = 0xe0b040;
            pix.rect(204, 78, 86, 15, red);
            pix.rect(204, 78, 86, 2, redLt);
            pix.rect(204, 91, 86, 2, redDk);
            for (int x : new int[] {222, 248, 274}) {
                pix.rect(x, 78, 2, 15, gold);
            }
            pix.rect(196, 76, 10, 19, black);
            for (int a = 0; a < 24; a++) {
                double t = a * Math.PI / 12;
                pix.set(201 + (int) Math.round(Math.cos(t) * 4), 85 + (int) Math.round(Math.sin(t) * 4), 0x34323c);
            }
            pix.set(201, 85, gold);
            pix.rect(208, 64, 6, 14, black);
            pix.rect(206, 62, 10, 2, 0x2a2832);
            pix.rect(238, 72, 8, 6, gold);
            pix.rect(239, 71, 6, 1, gold);
            pix.rect(288, 60, 32, 3, black);
            pix.rect(288, 63, 32, 36, red);
            pix.rect(288, 63, 2, 36, redLt);
            pix.rect(294, 68, 12, 10, gold);
            pix.rect(295, 69, 10, 8, 0xffd27a);
            pix.text("5972", 293, 84, gold, 1);
            pix.rect(194, 93, 126, 2, black);
            pix.rect(190, 91, 6, 7, red);
            pix.rect(187, 93, 3, 2, 0x8a8a90);
            for (int cx : new int[] {236, 256, 276}) {
                pix.disc(cx, 93, 6, 0x14121a);
                for (int a = 0; a < 32; a++) {
                    double t = a * Math.PI / 16;
                    pix.set(cx + (int) Math.round(Math.cos(t) * 5.5), 93 + (int) Math.round(Math.sin(t) * 5.5), red);
                }
                for (int a = 0; a < 4; a++) {
                    double t = a * Math.PI / 4;
                    pix.line(cx - (int) Math.round(Math.cos(t) * 4), 93 - (int) Math.round(Math.sin(t) * 4),
                            cx + (int) Math.round(Math.cos(t) * 4), 93 + (int) Math.round(Math.sin(t) * 4), red);
                }
                pix.set(cx, 93, gold);
            }
            pix.disc(212, 95, 4, 0x14121a);
            pix.set(212, 95, gold);
            pix.rect(236, 95, 40, 1, 0x9a9aa4);
        }


        int slotWidth() {
            return clamp((LINE_END - LINE_X) / Math.max(1, story.peak()), 18, 32);
        }

        double hallX(int p, int other, int sw, int visible) {
            if (p >= 0) {
                return p < visible ? LINE_X + sw * p + sw / 2.0 : DOOR_X + 30;
            }
            if (p == HELD) {
                return STOOL_X;
            }
            return other == HELD ? EXIT_X : DOOR_X;
        }

        static double[] hallWindow(int pa, int pb, boolean sorting, boolean unsorting) {
            if (pa >= 0 && pb >= 0) {
                return sorting ? new double[] {0.4, 1} : unsorting ? new double[] {0, 0.6} : new double[] {0, 1};
            }
            if (pa >= 0 && pb == HELD) {
                return new double[] {0, 0.5};
            }
            if (pa == HELD && pb >= 0) {
                return new double[] {0.45, 1};
            }
            if (pa == ABSENT && pb >= 0) {
                return new double[] {0.05, 1};
            }
            if (pa >= 0) {
                return new double[] {0, 0.95};
            }
            if (pa == HELD) {
                return new double[] {0, 0.6};
            }
            return new double[] {0.4, 1};
        }

        void hallScene(Step a, Step b, double t, Step shown) {
            System.arraycopy(hall, 0, pix.px, 0, hall.length);
            for (int[] s : stars) {
                if (Math.sin(time * (1 + s[2] % 3) + s[2]) > -0.5) {
                    pix.set(s[0], s[1], s[3]);
                }
            }
            for (int[] c : candles) {
                int y = c[1] + (int) Math.round(Math.sin(time * 1.3 + c[2]));
                pix.rect(c[0], y, 1, 3, 0xe8dcc0);
                pix.set(c[0], y - 1, ((int) (time * 9 + c[2]) & 1) == 0 ? 0xffd27a : 0xffa040);
                if (((int) (time * 4 + c[2]) % 5) == 0) {
                    pix.set(c[0], y - 2, 0xff8a3a);
                }
            }
            owl(38);

            int sw = slotWidth();
            int visible = (LINE_END - LINE_X) / sw;
            boolean sorting = false, unsorting = false, traffic = false;
            for (int id : cast(a, b)) {
                int pa = where(a, id), pb = where(b, id);
                sorting |= pa >= 0 && pb == HELD;
                unsorting |= pa == HELD && pb >= 0;
                traffic |= pa >= 0 && pb == ABSENT || pa == ABSENT && pb >= 0;
            }
            boolean failedIn = b.error != null && b.op == Op.ENQUEUE && b.token >= 0;
            double open = traffic || failedIn ? Math.sin(Math.PI * t) : 0;
            if (open > 0.1) {
                for (int y = 60; y <= 106; y++) {
                    int hw = (int) Math.round(11 * open);
                    for (int x = DOOR_X - hw; x <= DOOR_X + hw; x++) {
                        pix.set(x, y, (x + y) % 5 == 0 ? 0xf0b860 : 0xffd890);
                    }
                }
            }

            ArrayList<Walker> walkers = new ArrayList<>();
            for (int id : cast(a, b)) {
                int pa = where(a, id), pb = where(b, id);
                double x0 = hallX(pa, pb, sw, visible), x1 = hallX(pb, pa, sw, visible);
                double[] win = hallWindow(pa, pb, sorting, unsorting);
                double u = smooth(clamp01((t - win[0]) / (win[1] - win[0])));
                boolean sitting = pb == HELD && u >= 1 || pa == HELD && u <= 0;
                boolean moving = u > 0 && u < 1 && Math.abs(x1 - x0) > 0.5;
                boolean sorted = b.held == id && t >= 0.75 || a.held == id && pb == ABSENT;
                boolean glow = b.op == Op.PEEK && b.error == null && b.token == id && t > 0.25;
                walkers.add(new Walker(id, lerp(x0, x1, u), sitting, moving, sorted, glow));
            }
            for (Walker w : walkers) {
                if (!w.sitting) {
                    student(w);
                }
            }
            for (Walker w : walkers) {
                if (w.sitting) {
                    student(w);
                }
            }
            if (failedIn) {
                double target = b.line.length < visible ? LINE_X + sw * b.line.length + sw / 2.0 : DOOR_X - 12;
                double u = smooth(clamp01(t / 0.6));
                if (t < 0.6 || t < 0.85 && ((int) (time * 20) & 1) == 0) {
                    pix.ghost = t > 0.6;
                    student(new Walker(b.token, lerp(DOOR_X, target, u), false, u < 1, false, false));
                    pix.ghost = false;
                }
                if (t > 0.55) {
                    smoke((int) target, FLOOR - 8, clamp01((t - 0.55) / 0.45));
                }
            }

            double hy = hatY(a, b, t);
            boolean floating = hy != HAT_REST && hy != HAT_HEAD;
            boolean talking = b.held >= 0 && t > 0.65 && ((int) (time * 6) & 1) == 0;
            hat(hy + (floating ? Math.sin(time * 3) : 0), talking);
            if (b.held >= 0 && t >= 0.75) {
                Token tok = story.token(b.held);
                String cry = HOUSES[tok.house] + "!";
                int x = clamp(STOOL_X - Pix.width(cry, 1) / 2, 2, W - 2 - Pix.width(cry, 1));
                pix.text(cry, x + 1, 66, INK, 1);
                pix.text(cry, x, 65, HOUSE_TEXT[tok.house], 1);
            }

            if (b.error != null && !failedIn && t > 0.2) {
                Walker hit = null;
                for (Walker w : walkers) {
                    if (w.id == b.token) {
                        hit = w;
                    }
                }
                smoke(hit == null ? STOOL_X : (int) Math.round(hit.x), hit == null ? SEAT - 4 : FLOOR - 9, clamp01((t - 0.2) / 0.8));
            }
            boolean done = done();
            for (Walker w : walkers) {
                int cx = (int) Math.round(w.x);
                if (cx > W + 8) {
                    continue;
                }
                Token tok = story.token(w.id);
                int chars = w.sitting || where(b, w.id) < 0 ? 10 : Math.max(1, (sw - 1) / 4);
                boolean hot = w.glow || b.token == w.id && b.op == Op.ENQUEUE && t >= 0.5;
                int c = hot ? GOLD : w.sorted ? HOUSE_TEXT[tok.house] : PARCH;
                label(fit(tok.name, chars), cx, 110, c);
                label(fit(tok.detail, chars), cx, 116, DIM);
                if (w.glow) {
                    for (int k = 0; k < 4; k++) {
                        sparkle(cx - 6 + (k * 5) % 13, 86 + (k * 7) % 9, time * 3 + k, LUMOS);
                    }
                }
                if (done && !w.sitting && where(b, w.id) >= 0) {
                    for (int k = 0; k < 2; k++) {
                        sparkle(cx - 6 + (w.id * 7 + k * 5) % 13, 84 + (w.id * 3 + k * 11) % 10, time * 2 + w.id + k, GOLD);
                    }
                }
            }
            if (shown.line.length > 0) {
                double fx = -1, bx = -1;
                for (Walker w : walkers) {
                    if (w.id == shown.line[0]) {
                        fx = w.x;
                    }
                    if (w.id == shown.line[shown.line.length - 1]) {
                        bx = w.x;
                    }
                }
                if (fx >= 0 && fx < W) {
                    marker("FRONT", (int) Math.round(fx), shown.line.length == 1 ? 74 : 80, GOLD);
                }
                if (bx >= 0 && bx < W) {
                    marker("BACK", (int) Math.round(bx), 80, TEAL);
                }
                if (shown.line.length > visible) {
                    label("+" + (shown.line.length - visible), DOOR_X - 16, 80, DIM);
                }
            }
        }

        double hatY(Step a, Step b, double t) {
            boolean wasOn = a.held >= 0, nowOn = b.held >= 0;
            if (nowOn && a.held != b.held) {
                double start = wasOn ? HAT_HEAD : HAT_REST;
                if (t < 0.15) {
                    return start;
                }
                if (t < 0.4) {
                    return lerp(start, HAT_LIFT, smooth((t - 0.15) / 0.25));
                }
                if (t < 0.5) {
                    return HAT_LIFT;
                }
                if (t < 0.65) {
                    return lerp(HAT_LIFT, HAT_HEAD, smooth((t - 0.5) / 0.15));
                }
                return HAT_HEAD;
            }
            if (wasOn && !nowOn) {
                if (t < 0.2) {
                    return lerp(HAT_HEAD, HAT_LIFT, smooth(t / 0.2));
                }
                if (t < 0.35) {
                    return HAT_LIFT;
                }
                if (t < 0.6) {
                    return lerp(HAT_LIFT, HAT_REST, smooth((t - 0.35) / 0.25));
                }
                return HAT_REST;
            }
            return nowOn ? HAT_HEAD : HAT_REST;
        }

        void student(Walker w) {
            Token tok = story.token(w.id);
            int[] pal = new int[128];
            Arrays.fill(pal, -1);
            pal['h'] = HAIR[tok.look % HAIR.length];
            pal['f'] = SKIN[tok.look / 5 % SKIN.length];
            pal['e'] = EYES;
            pal['r'] = ROBE;
            pal['R'] = w.sorted ? HOUSE_MAIN[tok.house] : ROBE_LT;
            pal['w'] = SHIRT;
            pal['t'] = w.sorted ? HOUSE_MAIN[tok.house] : TIE;
            pal['l'] = LEGS;
            pal['s'] = SHOES;
            boolean stride = w.moving && ((int) (time * 8) & 1) == 1;
            String[] rows = stride ? WALK : STUDENT;
            int count = w.sitting ? 13 : 16;
            int x0 = (int) Math.round(w.x) - 4;
            int y0 = w.sitting ? SEAT - 13 : FLOOR - 15 - (stride ? 1 : 0);
            if (w.glow) {
                halo(rows, count, x0, y0, LUMOS);
            }
            blit(rows, count, x0, y0, pal);
        }

        void hat(double y, boolean talking) {
            int[] pal = new int[128];
            Arrays.fill(pal, -1);
            pal['k'] = 0x24160c;
            pal['b'] = 0x7a5232;
            pal['p'] = 0x5e3e24;
            pal['d'] = 0x3a2414;
            pal['o'] = 0x0a0606;
            String[] rows = HAT.clone();
            if (talking) {
                rows[8] = rows[8].replace('d', 'o');
                rows[9] = "...kbbbooobbbk.";
            }
            blit(rows, rows.length, STOOL_X - 7, (int) Math.round(y), pal);
        }

        void blit(String[] rows, int count, int x0, int y0, int[] pal) {
            for (int r = 0; r < count; r++) {
                String row = rows[r];
                for (int c = 0; c < row.length(); c++) {
                    int col = pal[row.charAt(c)];
                    if (col >= 0) {
                        pix.set(x0 + c, y0 + r, col);
                    }
                }
            }
        }

        void halo(String[] rows, int count, int x0, int y0, int color) {
            for (int r = 0; r < count; r++) {
                for (int c = 0; c < rows[r].length(); c++) {
                    if (rows[r].charAt(c) != '.') {
                        pix.set(x0 + c - 1, y0 + r, color);
                        pix.set(x0 + c + 1, y0 + r, color);
                        pix.set(x0 + c, y0 + r - 1, color);
                        pix.set(x0 + c, y0 + r + 1, color);
                    }
                }
            }
        }

        void marker(String word, int cx, int y, int color) {
            label(word, cx, y, color);
            pix.rect(cx - 1, y + 7, 3, 1, color);
            pix.set(cx, y + 8, color);
        }


        int pitch() {
            return clamp((BASE - ROOF) / Math.max(1, story.peak()), 7, 12);
        }

        double[] trunkSpot(Step s, int p, int other, int pitch, int levels) {
            if (p >= 0) {
                int lv = p - Math.max(0, s.line.length - levels);
                return lv < 0 ? null : new double[] {STACK_X, BASE - (lv + 1) * pitch + 1};
            }
            if (p == HELD) {
                return new double[] {HOVER_X, HOVER_Y};
            }
            return other == HELD ? new double[] {W + 20, -24} : new double[] {STACK_X, 0};
        }

        static double[] path(double[] p0, double[] mid, double[] p1, double u, double split) {
            if (u < split) {
                double v = smooth(u / split);
                return new double[] {lerp(p0[0], mid[0], v), lerp(p0[1], mid[1], v)};
            }
            double v = smooth((u - split) / (1 - split));
            return new double[] {lerp(mid[0], p1[0], v), lerp(mid[1], p1[1], v)};
        }

        void platformScene(Step a, Step b, double t, Step shown) {
            System.arraycopy(platform, 0, pix.px, 0, platform.length);
            steam();
            owl(44);
            int p = pitch(), th = p - 1, levels = (BASE - ROOF) / p;
            ArrayList<Crate> crates = new ArrayList<>();
            for (int id : cast(a, b)) {
                int pa = where(a, id), pb = where(b, id);
                double[] p0 = trunkSpot(a, pa, pb, p, levels), p1 = trunkSpot(b, pb, pa, p, levels);
                if (p0 == null && p1 == null) {
                    continue;
                }
                p0 = p0 == null ? p1 : p0;
                p1 = p1 == null ? p0 : p1;
                double[] xy;
                double u;
                if (pa >= 0 && pb == HELD) {
                    u = clamp01(t);
                    xy = path(p0, new double[] {p0[0], Math.max(TOP + 2, p0[1] - 16)}, p1, u, 0.35);
                } else if (pa == HELD && pb >= 0) {
                    u = clamp01(t);
                    xy = path(p0, new double[] {p1[0], Math.max(TOP + 2, p1[1] - 16)}, p1, u, 0.65);
                } else {
                    double[] win = pa == HELD ? new double[] {0, 0.7} : pb == HELD ? new double[] {0.3, 1} : new double[] {0, 1};
                    u = smooth(clamp01((t - win[0]) / (win[1] - win[0])));
                    xy = new double[] {lerp(p0[0], p1[0], u), lerp(p0[1], p1[1], u)};
                }
                boolean moving = u > 0 && u < 1 && (p0[0] != p1[0] || p0[1] != p1[1]);
                if (moving && pa == ABSENT) {
                    xy[0] += Math.sin(u * Math.PI * 3) * 2 * Math.sin(u * Math.PI);
                }
                crates.add(new Crate(id, xy[0], xy[1], moving, pb == HELD && u >= 1));
            }
            for (int pass = 0; pass < 2; pass++) {
                for (Crate c : crates) {
                    if ((c.moving || c.parked) == (pass == 1)) {
                        boolean glow = b.op == Op.PEEK && b.error == null && b.token == c.id && t > 0.25;
                        trunk(story.token(c.id), (int) Math.round(c.x), (int) Math.round(c.y), th, glow);
                        if (c.moving || glow) {
                            for (int k = 0; k < 4; k++) {
                                sparkle((int) c.x - 4 + (k * 23 + c.id * 7) % (TRUNK_W + 8), (int) c.y - 3 + (k * 5) % (th + 6),
                                        time * 3 + k, glow ? LUMOS : GOLD);
                            }
                        }
                    }
                }
            }
            for (Crate c : crates) {
                if (c.parked) {
                    label("RETURNED", (int) Math.round(c.x) + TRUNK_W / 2, (int) Math.round(c.y) + th + 3, DIM);
                }
            }
            if (b.error != null && b.op == Op.PUSH && b.token >= 0) {
                double ty = BASE - (Math.min(b.line.length, levels - 1) + 1) * p + 1;
                double u = smooth(clamp01(t / 0.6));
                if (t < 0.6 || t < 0.85 && ((int) (time * 20) & 1) == 0) {
                    pix.ghost = t > 0.6;
                    trunk(story.token(b.token), STACK_X, (int) Math.round(lerp(0, ty, u)), th, false);
                    pix.ghost = false;
                }
                if (t > 0.55) {
                    smoke(STACK_X + TRUNK_W / 2, (int) ty + th / 2, clamp01((t - 0.55) / 0.45));
                }
            } else if (b.error != null && t > 0.2) {
                double sy = BASE - 4;
                for (Crate c : crates) {
                    if (c.id == b.token) {
                        sy = c.y + th / 2.0;
                    }
                }
                smoke(STACK_X + TRUNK_W / 2, (int) sy, clamp01((t - 0.2) / 0.8));
            }
            if (shown.line.length > 0) {
                int topId = shown.line[shown.line.length - 1];
                for (Crate c : crates) {
                    if (c.id == topId && !c.parked) {
                        int y = (int) Math.round(c.y) + (th - 5) / 2;
                        label("TOP", 13, y, GOLD);
                        pix.rect(20, y + 2, 4, 1, GOLD);
                        pix.set(23, y + 1, GOLD);
                        pix.set(23, y + 3, GOLD);
                    }
                }
                int hidden = shown.line.length - levels;
                if (hidden > 0) {
                    label("+" + hidden + " BELOW", STACK_X + TRUNK_W / 2, BASE + 4, DIM);
                }
            }
            if (done()) {
                for (Crate c : crates) {
                    if (!c.parked) {
                        for (int k = 0; k < 2; k++) {
                            sparkle((int) c.x + (c.id * 29 + k * 37) % TRUNK_W, (int) c.y - 2 + (k * 7) % (th + 4), time * 2 + c.id + k, GOLD);
                        }
                    }
                }
            }
        }

        void trunk(Token tok, int x0, int y0, int h, boolean glow) {
            int body = TRUNK[tok.look % TRUNK.length], lid = TRUNK_LT[tok.look % TRUNK.length];
            int w = TRUNK_W;
            pix.rect(x0 - 1, y0 - 1, w + 2, h + 2, glow ? LUMOS : 0x120a06);
            pix.rect(x0, y0, w, h, body);
            pix.rect(x0, y0, w, 1, lid);
            if (h >= 9) {
                pix.rect(x0, y0 + 2, w, 1, (body >> 1) & 0x7f7f7f);
            }
            pix.rect(x0 + 5, y0, 2, h, STRAP);
            pix.rect(x0 + w - 7, y0, 2, h, STRAP);
            for (int[] c : new int[][] {{0, 0}, {w - 2, 0}, {0, h - 2}, {w - 2, h - 2}}) {
                pix.rect(x0 + c[0], y0 + c[1], 2, 2, BRASS);
            }
            String text = fit(tok.text, 14);
            int tw = Pix.width(text, 1);
            int tx = x0 + (w - tw) / 2;
            if (h >= 8) {
                int ty = y0 + (h - 7) / 2 + (h >= 9 ? 1 : 0);
                pix.rect(tx - 2, ty, tw + 4, 7, TAG);
                pix.text(text, tx, ty + 1, TAG_INK, 1);
            } else {
                pix.text(text, tx, y0 + (h - 5) / 2, PARCH, 1);
            }
        }

        void steam() {
            for (int i = 0; i < 7; i++) {
                double age = (time * 0.35 + i / 7.0) % 1;
                int cx = (int) Math.round(211 + age * 44 + Math.sin(time + i) * 2);
                int cy = (int) Math.round(61 - age * 28);
                int r = (int) Math.round(2 + age * 8);
                for (int dy = -r; dy <= r; dy++) {
                    for (int dx = -r; dx <= r; dx++) {
                        int x = cx + dx, y = cy + dy;
                        if (dx * dx + dy * dy <= r * r && (1 - age) * 16 > BAYER[y & 3][x & 3] && y >= TOP) {
                            pix.set(x, y, dy < 0 ? 0xe4e8ee : 0xb4b8c4);
                        }
                    }
                }
            }
        }


        void owl(int baseY) {
            double x = -10 + ((time + 5) % 17) * 26;
            if (x > W + 10) {
                return;
            }
            int cx = (int) Math.round(x), y = baseY + (int) Math.round(Math.sin(time * 2) * 2);
            boolean up = ((int) (time * 6) & 1) == 0;
            int c = 0xf4f4f0, d = 0x9aa0a8;
            pix.rect(cx - 1, y, 3, 3, c);
            pix.set(cx - 1, y, 0xe0a020);
            pix.set(cx + 1, y, 0xe0a020);
            pix.set(cx - 2, y, c);
            pix.set(cx + 2, y, c);
            int dy = up ? -1 : 1;
            pix.set(cx - 2, y + dy, c);
            pix.set(cx + 2, y + dy, c);
            pix.set(cx - 3, y + 2 * dy, d);
            pix.set(cx + 3, y + 2 * dy, d);
        }

        void sparkle(int x, int y, double phase, int c) {
            int s = Math.floorMod((int) (phase * 3), 4);
            if (s == 3) {
                return;
            }
            pix.set(x, y, c);
            if (s >= 1) {
                pix.set(x - 1, y, c);
                pix.set(x + 1, y, c);
                pix.set(x, y - 1, c);
                pix.set(x, y + 1, c);
            }
            if (s == 2) {
                pix.set(x - 2, y, c);
                pix.set(x + 2, y, c);
                pix.set(x, y - 2, c);
                pix.set(x, y + 2, c);
            }
        }

        void smoke(int cx, int cy, double u) {
            double r = 3 + 9 * u;
            for (int dy = (int) -r; dy <= r; dy++) {
                for (int dx = (int) -r; dx <= r; dx++) {
                    double d = Math.hypot(dx, dy);
                    int x = cx + dx, y = cy + dy;
                    if (d <= r && (1 - u) * 18 * (1 - d / (r + 1)) > BAYER[y & 3][x & 3] && y >= TOP && y < PANEL) {
                        pix.set(x, y, d < r * 0.5 ? 0xff8a3a : 0xc8302a);
                    }
                }
            }
        }

        static String fit(String s, int max) {
            if (s.length() <= max) {
                return s;
            }
            return max < 2 ? s.substring(0, Math.max(0, max)) : s.substring(0, max - 1) + ".";
        }

        void label(String s, int cx, int y, int c) {
            int x = cx - Pix.width(s, 1) / 2;
            pix.text(s, x + 1, y + 1, INK, 1);
            pix.text(s, x, y, c, 1);
        }


        void drawInside(Step s) {
            Inside in = s.inside;
            String head = "INSIDE " + story.type + ": GETSTORAGE() IS " + in.kind;
            String size = s.size < 0 ? "GETSIZE() FAILED" : "GETSIZE() = " + s.size;
            int sx = W - 6 - Pix.width(size, 1);
            pix.text(fit(in.error == null ? head : "INSIDE " + story.type, (sx - 12) / 4), 6, PANEL + 3, MAP_INK, 1);
            pix.text(size, sx, PANEL + 3, s.size != s.line.length ? MAP_RED : MAP_INK, 1);
            if (in.error != null) {
                pix.text(fit(in.error, 76), 6, PANEL + 13, MAP_RED, 1);
                return;
            }
            int y = PANEL + 10;
            int total = in.total;
            if (total == 0) {
                boolean stray = in.linked && in.tail != -3;
                String none = !in.linked ? "NO CELLS AT ALL" : stray ? "HEAD = NULL, BUT TAIL STILL POINTS AT A NODE"
                        : "HEAD = NULL, TAIL = NULL";
                pix.text(none, (W - Pix.width(none, 1)) / 2, y + 3, stray ? MAP_RED : MAP_FAINT, 1);
                return;
            }
            int gap = in.linked ? 9 : 1;
            int cw = clamp((W - 16 - gap * (total - 1)) / total, 12, 30);
            int fit = (W - 16 + gap) / (cw + gap);
            boolean more = total > fit;
            int count = Math.min(more ? fit - 1 : total, in.cells.length);
            int span = count * cw + (count - 1) * gap + (more ? gap + 14 : 0);
            int x0 = (W - span) / 2;
            for (int i = 0; i < count; i++) {
                int x = x0 + i * (cw + gap);
                cell(x, y, cw, in, i, s);
                String mark = mark(in, i);
                String below = mark != null ? mark : in.linked ? "" : String.valueOf(i);
                if (Pix.width(below, 1) > cw && mark != null) {
                    below = mark.substring(0, 1);
                }
                pix.text(below, x + (cw - Pix.width(below, 1)) / 2, y + 12, mark != null ? MAP_RED : MAP_FAINT, 1);
                if (in.linked && (i < count - 1 || more)) {
                    boolean back = i >= in.broken.length || !in.broken[i];
                    int c = back ? MAP_INK : MAP_RED;
                    int ax0 = x + cw + 1, ax1 = x + cw + gap - 2;
                    pix.rect(ax0, y + 4, ax1 - ax0 + 1, 1, c);
                    pix.set(ax1 - 1, y + 3, c);
                    pix.set(ax1 - 1, y + 5, c);
                    if (back) {
                        pix.set(ax0 + 1, y + 3, c);
                        pix.set(ax0 + 1, y + 5, c);
                    }
                }
            }
            if (more) {
                pix.text("+" + (total - count), x0 + count * (cw + gap) + 1, y + 3, MAP_FAINT, 1);
            }
        }

        void cell(int x, int y, int w, Inside in, int i, Step s) {
            int id = in.cells[i];
            boolean live = in.live[i];
            boolean hot = live && id >= 0 && id == s.token;
            if (live) {
                pix.rect(x, y, w, 10, MAP_INK);
                pix.rect(x + 1, y + 1, w - 2, 8, hot ? MAP_GOLD : MAP_FILL);
            } else {
                for (int k = 0; k < w; k += 2) {
                    pix.set(x + k, y, MAP_FAINT);
                    pix.set(x + k, y + 9, MAP_FAINT);
                }
                for (int k = 0; k < 10; k += 2) {
                    pix.set(x, y + k, MAP_FAINT);
                    pix.set(x + w - 1, y + k, MAP_FAINT);
                }
            }
            String text = fit(in.labels[i], Math.max(1, (w - 1) / 4));
            if (id == -1 && Pix.width(text, 1) > w - 2) {
                text = "-";
            }
            pix.text(text, x + (w - Pix.width(text, 1)) / 2, y + 3, live ? MAP_INK : MAP_FAINT, 1);
        }

        String mark(Inside in, int i) {
            if (in.linked) {
                boolean head = i == 0, tail = i == in.tail || in.tail < 0 && i == in.total - 1 && in.total > 0;
                String t = i == in.tail ? "TAIL" : "TAIL?";
                return head && tail ? "H+T" : head ? "HEAD" : tail ? t : null;
            }
            if (story.queue()) {
                return i == in.first && i == in.last ? "F+B" : i == in.first ? "FRONT" : i == in.last ? "BACK" : null;
            }
            return i == in.last ? "TOP" : null;
        }


        String where(Step s) {
            if (s.file == null) {
                return "";
            }
            return (s.file.equals(story.file) ? "LINE " : clean(s.file) + ":") + s.lineNo + "  ";
        }

        static ArrayList<Seg> say(String text, int color) {
            ArrayList<Seg> line = new ArrayList<>();
            line.add(new Seg(text, color));
            return line;
        }

        static ArrayList<Seg> segs(Object... parts) {
            ArrayList<Seg> line = new ArrayList<>();
            for (int i = 0; i < parts.length; i += 2) {
                line.add(new Seg((String) parts[i], (Integer) parts[i + 1]));
            }
            return line;
        }

        ArrayList<ArrayList<Seg>> narrate(Step s) {
            ArrayList<ArrayList<Seg>> lines = new ArrayList<>();
            boolean queue = story.queue();
            if (done() && finale > 1.2) {
                lines.add(segs("MISCHIEF MANAGED.  ", GOLD, summary(), PARCH));
                ArrayList<Story> all = stories();
                if (story.cut()) {
                    lines.add(say("RECORDING STOPPED AFTER " + LIMIT + " CALLS", RED));
                } else if (storyIndex + 1 < all.size()) {
                    String next = "STORY " + NUMERALS[storyIndex + 1] + ", " + all.get(storyIndex + 1).title();
                    lines.add(playing ? say(next + ", BEGINS IN " + (int) Math.ceil(HOLD - finale), DIM)
                            : say("PRESS N FOR " + next, DIM));
                } else {
                    lines.add(say("PRESS SPACE TO WATCH AGAIN", DIM));
                }
                return lines;
            }
            if (s.op == Op.START) {
                lines.add(segs(where(s), BRONZE, "A NEW, EMPTY " + story.title(), PARCH));
                lines.add(say(queue ? "THE GREAT HALL WAITS FOR THE FIRST-YEARS" : "AN EMPTY TROLLEY WAITS ON PLATFORM 9 3/4", DIM));
                return lines;
            }
            Token tok = s.token >= 0 ? story.token(s.token) : null;
            boolean adds = s.op == Op.ENQUEUE || s.op == Op.PUSH;
            String call = s.op == Op.ENQUEUE ? "ENQUEUE(" + tok.text + ")" : s.op == Op.PUSH ? "PUSH(" + tok.text + ")"
                    : s.op == Op.DEQUEUE ? "DEQUEUE()" : s.op == Op.POP ? "POP()" : s.op == Op.PEEK ? "PEEK()" : "ISEMPTY()";
            if (s.error != null) {
                lines.add(segs(where(s), BRONZE, call, PARCH, " THREW ", RED, s.error, RED));
                String why = !s.message.isEmpty() ? s.message
                        : tok == null && !adds ? "THE " + (queue ? "QUEUE" : "STACK") + " WAS EMPTY" : "NOTHING CHANGED";
                lines.add(say(why, DIM));
                return lines;
            }
            lines.add(adds ? segs(where(s), BRONZE, call, PARCH)
                    : segs(where(s), BRONZE, call, PARCH, " RETURNS ", DIM, s.result, GOLD));
            lines.add(s.warning != null ? say(s.warning, RED) : flavor(s, tok));
            return lines;
        }

        ArrayList<Seg> flavor(Step s, Token tok) {
            int n = s.line.length;
            switch (s.op) {
                case ENQUEUE:
                    return segs(tok.name, GOLD, " JOINS THE BACK OF THE LINE", DIM);
                case DEQUEUE:
                    return tok == null ? say("NOBODY IS IN LINE, SO THERE IS NOTHING TO RETURN", DIM)
                            : segs("FIRST IN, FIRST OUT. THE SORTING HAT CRIES ", DIM, HOUSES[tok.house] + "!", HOUSE_TEXT[tok.house]);
                case PUSH:
                    return segs("WINGARDIUM LEVIOSA! ", DIM, tok.name, GOLD, " FLOATS ONTO THE TOP", DIM);
                case POP:
                    return tok == null ? say("THE TROLLEY IS EMPTY, SO THERE IS NOTHING TO RETURN", DIM)
                            : segs("ACCIO! LAST IN, FIRST OUT: ", DIM, tok.name, GOLD, " LEAVES THE TOP", DIM);
                case PEEK:
                    if (tok == null) {
                        return say(story.queue() ? "NOBODY IS IN LINE" : "THE TROLLEY IS EMPTY", DIM);
                    }
                    return say(story.queue() ? "LUMOS! THE FRONT OF THE LINE LIGHTS UP AND STAYS"
                            : "LUMOS! THE TOP TRUNK LIGHTS UP AND STAYS ON THE PILE", DIM);
                default:
                    if (story.queue()) {
                        return say(n == 0 ? "NOBODY IS IN LINE" : n + " STILL IN LINE", DIM);
                    }
                    return say(n == 0 ? "THE TROLLEY IS EMPTY" : n + (n == 1 ? " TRUNK" : " TRUNKS") + " ON THE TROLLEY", DIM);
            }
        }

        String summary() {
            int[] counts = new int[Op.values().length];
            for (int i = 1; i <= last(); i++) {
                counts[story.step(i).op.ordinal()]++;
            }
            StringBuilder sb = new StringBuilder();
            for (Op op : Op.values()) {
                if (op != Op.START && counts[op.ordinal()] > 0) {
                    sb.append(sb.length() == 0 ? "" : ", ").append(counts[op.ordinal()]).append(' ')
                            .append(op == Op.EMPTY ? "ISEMPTY" : op.name());
                }
            }
            int left = story.step(last()).line.length;
            sb.append(sb.length() == 0 ? "" : ". ").append(left).append(" LEFT ").append(story.place());
            return sb.toString();
        }

        void line(ArrayList<Seg> segs, int y) {
            int room = (W - 8) / 4;
            ArrayList<Seg> kept = new ArrayList<>();
            for (Seg s : segs) {
                if (room <= 0) {
                    break;
                }
                String text = s.text.length() > room ? fit(s.text, room) : s.text;
                room -= text.length();
                kept.add(new Seg(text, s.color));
            }
            int width = 0;
            for (Seg s : kept) {
                width += s.text.length() * 4;
            }
            int x = (W - width + 1) / 2;
            for (Seg s : kept) {
                x = pix.text(s.text, x, y, s.color, 1);
            }
        }

        void trim(int y) {
            pix.rect(0, y, W, 1, 0x9a7a3a);
            pix.rect(0, y + 1, W, 2, 0x4a1420);
            for (int x = 2; x < W; x += 8) {
                pix.set(x, y + 1, 0xe8b13a);
                pix.set(x + 4, y + 2, 0x9a7a3a);
            }
            pix.rect(0, y + 3, W, 1, 0x6a4a20);
        }

        void drawFrame(Step shown) {
            pix.rect(0, 0, W, TOP, INK);
            pix.rect(0, BOTTOM, W, H - BOTTOM, INK);
            trim(20);
            trim(BOTTOM);
            int count = stories().size();
            pix.text("HOGWARTS: QUEUE AND STACK", 6, 4, GOLD, 1);
            String which = "STORY " + NUMERALS[storyIndex] + " OF " + NUMERALS[count - 1] + ": " + story.title();
            which = fit(which, 44);
            pix.text(which, W - 6 - Pix.width(which, 1), 4, PARCH, 1);
            pix.text(story.queue() ? "THE SORTING CEREMONY: FIRST IN, FIRST OUT" : "PLATFORM 9 3/4: LAST IN, FIRST OUT", 6, 12, DIM, 1);
            double sp = SPEEDS[speed];
            String state = card > 0 ? ">" : done() ? "END" : to == last() && waiting() ? "WAITING FOR MAIN" : playing ? ">" : "||";
            String stats = "STEP " + to + "/" + last() + "  " + (sp < 1 ? (sp == 0.5 ? "0.5" : "0.25") : String.valueOf((int) sp)) + "/S  " + state;
            pix.text(stats, W - 6 - Pix.width(stats, 1), 12, DIM, 1);

            ArrayList<ArrayList<Seg>> lines = narrate(shown);
            line(lines.get(0), 158);
            line(lines.get(1), 165);
            ArrayList<Seg> keys = segs("SPACE", BRONZE, " PLAY/PAUSE   ", DIM, "<- ->", BRONZE, " STEP   ", DIM);
            if (count > 1) {
                keys.addAll(segs("N", BRONZE, " NEXT STORY   ", DIM));
            }
            keys.addAll(segs("+ -", BRONZE, " SPEED   ", DIM, "R", BRONZE, " REPLAY", DIM));
            line(keys, 173);
        }

        void drawCard() {
            boolean edge = CARD_TIME - card < 0.25 || card < 0.4;
            pix.darken(TOP, BOTTOM - 1);
            if (edge) {
                return;
            }
            pix.darken(TOP, BOTTOM - 1);
            boolean queue = story.queue();
            center("STORY " + NUMERALS[storyIndex], 36, DIM, 1);
            center(queue ? "THE SORTING CEREMONY" : "PLATFORM 9 3/4", 46, GOLD, 2);
            center(queue ? "A QUEUE: FIRST IN, FIRST OUT" : "A STACK: LAST IN, FIRST OUT", 64, PARCH, 1);
            center(queue ? "ENQUEUE JOINS THE BACK, DEQUEUE TAKES THE FRONT"
                    : "PUSH ADDS ON TOP, POP TAKES THE TOP", 74, DIM, 1);
            String from = story.file == null ? "" : " FROM " + clean(story.file) + " LINE " + story.line;
            center(fit("WATCHING " + story.title() + from, 76), 92, DIM, 1);
            center("EVERY STEP YOU SEE IS ONE CALL YOUR CODE MADE", 102, BRONZE, 1);
            center("THE PARCHMENT BELOW SHOWS WHAT GETSTORAGE() REALLY HOLDS", 112, BRONZE, 1);
        }

        void center(String s, int y, int c, int scale) {
            pix.text(s, (W - Pix.width(s, scale)) / 2, y, c, scale);
        }
    }

    static final class Screen extends JPanel {
        private static final long serialVersionUID = 1L;
        private final transient Stage stage;
        private long last;

        Screen(Stage stage) {
            this.stage = stage;
            setPreferredSize(new Dimension(Pix.W * 4, Pix.H * 4));
            setBackground(Color.BLACK);
            setFocusable(true);
            addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                    }
                    stage.key(e.getKeyCode(), e.getKeyChar());
                }
            });
        }

        void start() {
            last = System.nanoTime();
            new Timer(15, e -> {
                long now = System.nanoTime();
                stage.update(Math.min(0.05, (now - last) / 1e9));
                last = now;
                repaint();
            }).start();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage image = stage.render();
            double s = Math.min(getWidth() / (double) Pix.W, getHeight() / (double) Pix.H);
            if (s >= 1) {
                s = Math.floor(s);
            }
            int w = (int) (Pix.W * s), h = (int) (Pix.H * s);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(image, (getWidth() - w) / 2, (getHeight() - h) / 2, w, h, null);
        }
    }
}
