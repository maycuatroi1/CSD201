package sort;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class SortingVisualizer {

    static final String[] NUMERALS = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

    final List<Chapter> chapters = new ArrayList<>();
    final List<Fleet> fleets = new ArrayList<>();

    public interface Factory {
        <T extends Comparable<T>> Sorter<T> create();
    }

    public void addAlgorithm(Factory factory, String epithet) {
        if (chapters.size() == NUMERALS.length) {
            throw new IllegalStateException("keys 1-9 pick an algorithm: 9 at most");
        }
        Sorter<Integer> probe = factory.create();
        String name = probe.getClass().getSimpleName();
        chapters.add(new Chapter(name.replaceAll("([a-z])([A-Z])", "$1 $2").toUpperCase(),
                name.toUpperCase() + ".JAVA", epithet.toUpperCase(), factory));
    }

    public <T extends Comparable<T>> void addArray(String order, T[] data, Function<T, String> name,
            Function<T, String> detail) {
        String type = data.getClass().getComponentType().getSimpleName().toUpperCase();
        T[] unsorted = data.clone();
        fleets.add(new Fleet(type, type + "[] " + order.toUpperCase(),
                (chapter, rnd) -> voyage(chapter, unsorted, name, detail)));
    }

    public void addRandomIntegers(int count) {
        if (count < 2 || count > 16) {
            throw new IllegalArgumentException("2 to 16 ships fit on screen");
        }
        fleets.add(new Fleet("INTEGER", count + " RANDOM INTEGERS (R FOR A NEW CREW)", (chapter, rnd) -> {
            Integer[] crew = new Integer[count];
            for (int i = 0; i < count; i++) {
                crew[i] = 10 + rnd.nextInt(90);
            }
            return voyage(chapter, crew, v -> v.toString(), v -> "");
        }));
    }

    public void open() {
        if (chapters.isEmpty() || fleets.isEmpty()) {
            throw new IllegalStateException("add at least one algorithm and one array before open()");
        }
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("(no display, so no animation)");
            return;
        }
        Stage stage = new Stage(System.nanoTime(), new ArrayList<>(chapters), new ArrayList<>(fleets));
        SwingUtilities.invokeLater(() -> {
            Screen screen = new Screen(stage);
            JFrame frame = new JFrame("A Sorting Odyssey");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(screen);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            screen.requestFocusInWindow();
            screen.start();
        });
    }

    static final class Chapter {
        final String title;
        final String file;
        final String epithet;
        final Factory factory;

        Chapter(String title, String file, String epithet, Factory factory) {
            this.title = title;
            this.file = file;
            this.epithet = epithet;
            this.factory = factory;
        }
    }

    static final class Fleet {
        final String type;
        final String blurb;
        final BiFunction<Chapter, Random, Voyage> launch;

        Fleet(String type, String blurb, BiFunction<Chapter, Random, Voyage> launch) {
            this.type = type;
            this.blurb = blurb;
            this.launch = launch;
        }
    }

    static final class Item {
        final String name;
        final String detail;
        final String said;
        final double height;

        Item(String name, String detail, String said, double height) {
            this.name = name;
            this.detail = detail;
            this.said = said;
            this.height = height;
        }
    }

    enum Kind { START, COMPARE, SWAP, WRITE, COPY, SPLIT, FOCUS, PIVOT, END }

    static final class Step {
        final Kind kind;
        final int[] slots;
        final int[] live;
        final boolean[] gaps;
        final boolean[] settled;
        final int hover;
        final int a;
        final int b;
        final int cmp;
        final int from;
        final int lo;
        final int mid;
        final int hi;
        final int pivot;
        final int compares;
        final int swaps;
        final int moves;

        Step(Kind kind, int[] slots, int[] live, boolean[] gaps, boolean[] settled, int hover, int a, int b,
                int cmp, int from, int lo, int mid, int hi, int pivot, int compares, int swaps, int moves) {
            this.kind = kind;
            this.slots = slots;
            this.live = live;
            this.gaps = gaps;
            this.settled = settled;
            this.hover = hover;
            this.a = a;
            this.b = b;
            this.cmp = cmp;
            this.from = from;
            this.lo = lo;
            this.mid = mid;
            this.hi = hi;
            this.pivot = pivot;
            this.compares = compares;
            this.swaps = swaps;
            this.moves = moves;
        }
    }

    static final class Voyage {
        final Chapter chapter;
        final Item[] items;
        final List<Step> steps;
        final boolean cut;
        final int maxGaps;
        final int bypassed;

        Voyage(Chapter chapter, Item[] items, List<Step> steps, boolean cut, int maxGaps, int bypassed) {
            this.chapter = chapter;
            this.items = items;
            this.steps = steps;
            this.cut = cut;
            this.maxGaps = maxGaps;
            this.bypassed = bypassed;
        }
    }

    static final class Tracked implements Comparable<Tracked> {
        final int id;
        final int[][] order;
        final int[] calls;

        Tracked(int id, int[][] order, int[] calls) {
            this.id = id;
            this.order = order;
            this.calls = calls;
        }

        public int compareTo(Tracked other) {
            calls[0]++;
            return order[id][other.id];
        }
    }

    static final class Recorder implements Sorter.Observer<Tracked> {
        static final int LIMIT = 4000;
        static final int HELD = -1;
        final Tracked[] main;
        final Sorter<Tracked> sorter;
        final int n;
        final int[] live;
        final boolean[] splits;
        final boolean[] settled;
        final Map<Long, Integer> mids = new HashMap<>();
        final List<Step> steps = new ArrayList<>();
        int[] seen;
        int hover = -1;
        int lo = -1;
        int mid = -1;
        int hi = -1;
        int pivot = -1;

        Recorder(Tracked[] main, Sorter<Tracked> sorter) {
            this.main = main;
            this.sorter = sorter;
            n = main.length;
            live = new int[n];
            for (int i = 0; i < n; i++) {
                live[main[i].id] = i;
            }
            splits = new boolean[n];
            settled = new boolean[n];
            add(Kind.START, -1, -1, 0, 0);
        }

        void add(Kind kind, int a, int b, int cmp, int from) {
            if (kind != Kind.END && steps.size() > LIMIT) {
                throw new Overflow();
            }
            seen = new int[n];
            for (int i = 0; i < n; i++) {
                seen[i] = main[i] == null ? -1 : main[i].id;
            }
            boolean[] gaps = splits.clone();
            for (int s = 1; s < n; s++) {
                if (seen[s - 1] >= 0 && settled[seen[s - 1]] || seen[s] >= 0 && settled[seen[s]]) {
                    gaps[s] = true;
                }
            }
            steps.add(new Step(kind, seen, live.clone(), gaps, settled.clone(), hover, a, b, cmp, from, lo, mid, hi,
                    pivot, sorter.getCompareCount(), sorter.getSwapCount(), sorter.getMoveCount()));
        }

        public void compared(Tracked a, Tracked b, int result) {
            add(Kind.COMPARE, a.id, b.id, Integer.signum(result), 0);
        }

        public void swapped(Tracked[] arr, int i, int j) {
            int offset = arr == main ? 0 : n;
            live[arr[i].id] = offset + i;
            live[arr[j].id] = offset + j;
            add(Kind.SWAP, arr[i].id, arr[j].id, 0, 0);
        }

        public void written(Tracked[] arr, int i) {
            int v = arr[i].id;
            int from = live[v];
            if (arr == main) {
                int old = seen[i];
                if (old >= 0 && old != v && live[old] == i) {
                    live[old] = HELD;
                }
                if (from >= 0 && from < n && from != i) {
                    hover = from;
                }
                live[v] = i;
            } else {
                live[v] = n + i;
            }
            add(Kind.WRITE, v, -1, 0, from);
        }

        public void copied(Tracked[] src, Tracked[] dst, int lo, int hi) {
            for (int k = lo; k <= hi; k++) {
                live[dst[k].id] = dst == main ? k : n + k;
            }
            for (int s = lo + 1; s <= hi; s++) {
                splits[s] = false;
            }
            focus(lo, mids.getOrDefault(key(lo, hi), -1), hi);
            add(Kind.COPY, -1, -1, 0, 0);
        }

        public void split(int lo, int mid, int hi) {
            splits[mid + 1] = true;
            mids.put(key(lo, hi), mid);
            focus(lo, mid, hi);
            add(Kind.SPLIT, -1, -1, 0, 0);
        }

        public void focused(int lo, int hi) {
            int done = settle();
            focus(lo, -1, hi);
            add(Kind.FOCUS, -1, done, 0, 0);
        }

        public void pivoted(int p) {
            pivot = main[p].id;
            add(Kind.PIVOT, pivot, -1, 0, 0);
        }

        void focus(int lo, int mid, int hi) {
            this.lo = lo;
            this.mid = mid;
            this.hi = hi;
        }

        static long key(int lo, int hi) {
            return (long) lo << 32 | hi;
        }

        int settle() {
            int done = pivot;
            if (pivot >= 0) {
                settled[pivot] = true;
            }
            pivot = -1;
            return done;
        }

        void finish() {
            settle();
            focus(-1, -1, -1);
            Arrays.fill(splits, false);
            Arrays.fill(settled, false);
            add(Kind.END, -1, -1, 0, 0);
        }

        static final class Overflow extends RuntimeException {
            private static final long serialVersionUID = 1L;
        }
    }

    static <T extends Comparable<T>> Voyage voyage(Chapter chapter, T[] data, Function<T, String> name,
            Function<T, String> detail) {
        int n = data.length;
        int[][] order = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                order[i][j] = Integer.signum(data[i].compareTo(data[j]));
            }
        }
        Integer[] byValue = new Integer[n];
        for (int i = 0; i < n; i++) {
            byValue[i] = i;
        }
        Arrays.sort(byValue, (x, y) -> order[x][y]);
        int[] rank = new int[n];
        int top = 0;
        for (int k = 1; k < n; k++) {
            if (order[byValue[k]][byValue[k - 1]] != 0) {
                top++;
            }
            rank[byValue[k]] = top;
        }
        Item[] items = new Item[n];
        for (int i = 0; i < n; i++) {
            String label = name.apply(data[i]);
            String more = detail.apply(data[i]);
            items[i] = new Item(label, more, more.isEmpty() ? label : label + " (" + more + ")",
                    top == 0 ? 1 : rank[i] / (double) top);
        }

        int[] calls = new int[1];
        Tracked[] arr = new Tracked[n];
        for (int i = 0; i < n; i++) {
            arr[i] = new Tracked(i, order, calls);
        }
        Sorter<Tracked> sorter = chapter.factory.create();
        Recorder recorder = new Recorder(arr, sorter);
        sorter.setObserver(recorder);
        boolean cut = false;
        try {
            sorter.sort(arr);
        } catch (Recorder.Overflow e) {
            cut = true;
        }
        recorder.finish();

        int maxGaps = 0;
        for (Step st : recorder.steps) {
            int g = 0;
            for (boolean b : st.gaps) {
                if (b) {
                    g++;
                }
            }
            maxGaps = Math.max(maxGaps, g);
        }
        return new Voyage(chapter, items, recorder.steps, cut, maxGaps, calls[0] - sorter.getCompareCount());
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
                "6011100110101010", "7111001010010010", "8010101010101010", "9010101011001110",
                " 000000000000000", ".000000000000010", ",000000000010100", ":000010000010000",
                ";000010000010100", "-000000111000000", "+000010111010000", "=000111000111000",
                "<001010100010001", ">100010001010100", "(010100100100010", ")010001001001010",
                "[110100100100110", "]011001001001011", "/001001010100100", "!010010010000010",
                "?110001010000010", "'010010000000000", "|010010010010010", "_000000000000111",
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
        static final int TOP = 27, HORIZON = 90, WATERLINE = 127, BOTTOM = 150;
        static final int MARGIN = 8, GAP = 6, SPREAD = 3, MIN_SAIL = 10, MAX_SAIL = 53, LIFT = 15;
        static final int AUX_BASE = 62;
        static final double AUX_SCALE = 0.4;
        static final int SUN_X = 238, SUN_Y = HORIZON + 4, SUN_R = 16;
        static final double CARD_TIME = 3.4;
        static final double[] SPEEDS = {0.5, 1, 2, 3, 5, 8, 12};

        static final int INK = 0x0a0710, PARCH = 0xf0e2c4, DIM = 0x8c7a6c, FAINT = 0x6a5a6e,
                BRONZE = 0xd08a3a, BRONZE_DK = 0x6a4020, TERRA = 0xc2552a, GOLD = 0xffcf4a, TEAL = 0x7fe0cf,
                EMBER = 0xff9a6a, PURPLE = 0xc58ae0, LAUREL = 0xb6d47a,
                HULL = 0x1b0d10, DECK = 0x46241a, MAST = 0x4a2a18, YARD = 0x5e3820, OUTLINE = 0x24101c,
                EYE = 0xf4ead2, FOAM = 0xf2dcc0, SPRAY = 0x5f9fb8, SPRAY_LT = 0xbfe3e0, OAR = 0x6a4426,
                ROPE = 0x3a1e2a;
        static final int[] SAIL = {0xeadcbc, 0xbfa984, 0xb4462a}, SAIL_A = {GOLD, 0xd0902a, 0x8a3a12},
                SAIL_B = {TEAL, 0x3fa598, 0x1d5a5a}, SAIL_SWAP = {EMBER, 0xd0603e, 0x6a1e12},
                SAIL_PIVOT = {PURPLE, 0x8e52b0, 0x3e1a52}, SAIL_LAUREL = {LAUREL, 0x7a9a48, 0x34501e},
                SAIL_DIM = {0x8a7c84, 0x6a5e6c, 0x5a4a5a}, SAIL_HOME = {0xf5c242, 0xc98b22, 0xfff0b8},
                SAIL_GHOST = {0x7a6a80, 0x5a4a64, 0x5a4a64};
        static final int[] SKY = {0x0c0a1c, 0x161032, 0x24143e, 0x3a1844, 0x581e46, 0x7e2a44, 0xa83c40, 0xd25a3c, 0xf08a44};
        static final int[] SEA = {0x7a2a48, 0x5a2042, 0x42183c, 0x2e1234, 0x1e0c28, 0x14081c};
        static final int[] SUN = {0xfff1c4, 0xffd27a, 0xffa04a};
        static final int[][] BAYER = {{0, 8, 2, 10}, {12, 4, 14, 6}, {3, 11, 1, 9}, {15, 7, 13, 5}};
        static final int[][] HULL_S = {{2, 1}, {1, 1}, {1, 0}, {2, 1}, {3, 2}};
        static final int[][] HULL_L = {{2, 2}, {1, 1}, {1, 0}, {1, 0}, {2, 1}, {3, 2}, {5, 4}};
        static final int[][] STERN_S = {{1, 0}, {0, -1}, {0, -2}, {1, -3}, {2, -3}, {3, -2}};
        static final int[][] STERN_L = {{1, 0}, {0, -1}, {-1, -2}, {-1, -3}, {-1, -4}, {0, -5}, {1, -5}, {2, -5}, {3, -4}, {3, -3}};
        static final int[][] PROW_S = {{-1, 0}, {0, -1}, {1, -2}};
        static final int[][] PROW_L = {{-1, 0}, {0, -1}, {1, -2}, {1, -3}, {2, -4}};
        static final String[] MEANDER = {"#####.", "#...#.", "#.###.", "#.#...", "#.####"};

        static final class Glint {
            final double x;
            final int y;
            final int len;
            final double speed;
            final double freq;
            final double phase;
            final int color;

            Glint(double x, int y, int len, double speed, double freq, double phase, int color) {
                this.x = x;
                this.y = y;
                this.len = len;
                this.speed = speed;
                this.freq = freq;
                this.phase = phase;
                this.color = color;
            }
        }

        static final class Cloud {
            final double x;
            final int y;
            final int len;
            final int thick;
            final double speed;

            Cloud(double x, int y, int len, int thick, double speed) {
                this.x = x;
                this.y = y;
                this.len = len;
                this.thick = thick;
                this.speed = speed;
            }
        }

        static final class Sprite {
            final int id;
            final int slot;
            final double x;
            final double y;
            final double scale;
            final double dx;
            final boolean ghost;
            final boolean lifted;
            final boolean held;

            Sprite(int id, int slot, double x, double y, double scale, double dx, boolean ghost, boolean lifted,
                    boolean held) {
                this.id = id;
                this.slot = slot;
                this.x = x;
                this.y = y;
                this.scale = scale;
                this.dx = dx;
                this.ghost = ghost;
                this.lifted = lifted;
                this.held = held;
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
        final Random rnd;
        final int[] backdrop = new int[W * H];
        final int[] island = new int[W * H];
        final List<Glint> stars = new ArrayList<>(), waves = new ArrayList<>(), glare = new ArrayList<>(),
                birds = new ArrayList<>();
        final List<Cloud> clouds = new ArrayList<>();
        int apexX, apexY;

        final List<Chapter> chapters;
        final List<Fleet> fleets;
        int chapterIndex, fleetIndex;
        Voyage voyage;
        double slotW;
        long crewSeed;
        int from, to, speed = 3;
        double progress = 1, time, wait, card, finale;
        boolean playing;

        Stage(long seed, List<Chapter> chapters, List<Fleet> fleets) {
            this.chapters = chapters;
            this.fleets = fleets;
            rnd = new Random(seed);
            crewSeed = rnd.nextLong();
            paintBackdrop();
            paintIsland();
            scatter(new Random(7));
            reset(true);
        }

        void reset(boolean withCard) {
            voyage = fleet().launch.apply(chapter(), new Random(crewSeed));
            slotW = (W - 2.0 * MARGIN - GAP * voyage.maxGaps) / voyage.items.length;
            from = to = 0;
            progress = 1;
            wait = finale = 0;
            card = withCard ? CARD_TIME : 0;
            playing = !withCard;
        }

        Chapter chapter() {
            return chapters.get(chapterIndex);
        }

        Fleet fleet() {
            return fleets.get(fleetIndex);
        }

        int last() {
            return voyage.steps.size() - 1;
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

        double moveTime() {
            return Math.min(0.45, 0.8 / SPEEDS[speed]);
        }

        void key(int code, char ch) {
            if (code == KeyEvent.VK_SPACE) {
                if (card > 0) {
                    card = 0;
                    playing = true;
                } else if (to == last()) {
                    go(0);
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
            } else if (code == KeyEvent.VK_D) {
                fleetIndex = (fleetIndex + 1) % fleets.size();
                reset(true);
            } else if (code == KeyEvent.VK_R) {
                crewSeed = rnd.nextLong();
                reset(false);
            } else {
                int digit = code >= KeyEvent.VK_1 && code <= KeyEvent.VK_9 ? code - KeyEvent.VK_1
                        : code >= KeyEvent.VK_NUMPAD1 && code <= KeyEvent.VK_NUMPAD9 ? code - KeyEvent.VK_NUMPAD1 : -1;
                if (digit >= 0 && digit < chapters.size()) {
                    chapterIndex = digit;
                    reset(true);
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
                progress = Math.min(1, progress + dt / moveTime());
            }
            if (card > 0) {
                card -= dt;
                if (card <= 0) {
                    card = 0;
                    playing = true;
                    wait = 0;
                }
            } else if (playing && progress >= 1 && wait >= 1 / SPEEDS[speed]) {
                if (to < last()) {
                    go(to + 1);
                } else {
                    playing = false;
                }
            }
            finale = to == last() && progress >= 1 && !voyage.cut ? finale + dt : 0;
        }

        static int dither(int[] stops, double u, int x, int y) {
            double f = Math.max(0, Math.min(1, u)) * (stops.length - 1);
            int i = Math.min((int) f, stops.length - 2);
            return (f - i) * 16 > BAYER[y & 3][x & 3] + 0.5 ? stops[i + 1] : stops[i];
        }

        void paintBackdrop() {
            Arrays.fill(backdrop, INK);
            for (int y = TOP; y <= BOTTOM; y++) {
                for (int x = 0; x < W; x++) {
                    int c;
                    if (y < HORIZON) {
                        c = dither(SKY, Math.pow((y - TOP) / (double) (HORIZON - TOP), 1.7), x, y);
                    } else if (y == HORIZON) {
                        c = 0xc85a46;
                    } else {
                        c = dither(SEA, Math.pow((y - HORIZON) / (double) (BOTTOM - HORIZON), 0.7), x, y);
                    }
                    backdrop[y * W + x] = c;
                }
            }
            for (int y = SUN_Y - SUN_R - 10; y < HORIZON; y++) {
                for (int x = SUN_X - SUN_R - 10; x <= SUN_X + SUN_R + 10; x++) {
                    double d = Math.hypot(x - SUN_X, y - SUN_Y);
                    if (d <= SUN_R) {
                        backdrop[y * W + x] = dither(SUN, (y - (SUN_Y - SUN_R)) / (double) (HORIZON - SUN_Y + SUN_R), x, y);
                    } else if (d <= SUN_R + 10 && (1 - (d - SUN_R) / 10) * 9 > BAYER[y & 3][x & 3]) {
                        backdrop[y * W + x] = 0xf59a52;
                    }
                }
            }
        }

        void paintIsland() {
            Arrays.fill(island, -1);
            for (int x = 256; x < W; x++) {
                int h = (int) Math.round(7 * Math.sin(Math.PI * (x - 256) / 64.0) + 1.2 * Math.sin(x * 0.9));
                for (int y = HORIZON - h; y <= HORIZON; y++) {
                    island[y * W + x] = 0x4a1c3e;
                }
            }
            int[] h = new int[W];
            for (int x = 12; x < 160; x++) {
                double v = 25 * Math.exp(-Math.pow((x - 66) / 24.0, 2)) + 11 * Math.exp(-Math.pow((x - 124) / 16.0, 2))
                        + 1.5 * Math.sin(x * 0.8) - 1;
                h[x] = (int) Math.round(Math.min(18, v));
            }
            int tx = 59, by = HORIZON - 19;
            for (int x = tx - 2; x <= tx + 16; x++) {
                h[x] = 18;
            }
            for (int x = 12; x < 160; x++) {
                for (int y = HORIZON - h[x]; y <= HORIZON && h[x] >= 0; y++) {
                    island[y * W + x] = y == HORIZON - h[x] && h[x + 1] < h[x] ? 0x6a2a44 : 0x1e0e22;
                }
            }
            int stone = 0x1e0e22;
            row(tx - 1, tx + 15, by, stone);
            row(tx, tx + 14, by - 1, stone);
            for (int c = 1; c <= 13; c += 3) {
                for (int y = by - 6; y <= by - 2; y++) {
                    island[y * W + tx + c] = stone;
                }
            }
            row(tx, tx + 14, by - 7, stone);
            row(tx, tx + 14, by - 8, stone);
            row(tx + 1, tx + 13, by - 9, stone);
            row(tx + 3, tx + 11, by - 10, stone);
            row(tx + 5, tx + 9, by - 11, stone);
            row(tx + 7, tx + 7, by - 12, stone);
            apexX = tx + 7;
            apexY = by - 12;
        }

        private void row(int x0, int x1, int y, int c) {
            for (int x = x0; x <= x1; x++) {
                island[y * W + x] = c;
            }
        }

        void scatter(Random art) {
            for (int i = 0; i < 40; i++) {
                stars.add(new Glint(art.nextInt(W), TOP + 1 + art.nextInt(24), 1, 0, 1 + art.nextDouble() * 3,
                        art.nextDouble() * 7, art.nextBoolean() ? 0xd8d0f0 : 0x8a80b0));
            }
            for (int i = 0; i < 170; i++) {
                double depth = Math.pow(art.nextDouble(), 1.4);
                int y = HORIZON + 2 + (int) (depth * (BOTTOM - HORIZON - 3));
                int color = depth < 0.25 ? 0xb04a5a : depth < 0.6 ? 0x8a3a64 : 0x5a2a5c;
                waves.add(new Glint(art.nextDouble() * W, y, 1 + (int) (depth * 6 * art.nextDouble()), 1 + depth * 6,
                        0.5 + art.nextDouble() * 1.5, art.nextDouble() * 7, color));
            }
            for (int i = 0; i < 60; i++) {
                double depth = Math.pow(art.nextDouble(), 1.2);
                int y = HORIZON + 1 + (int) (depth * (WATERLINE + 3 - HORIZON));
                int len = 1 + art.nextInt(2 + (int) (depth * 4));
                double x = SUN_X + art.nextGaussian() * (2 + depth * 14) - len / 2.0;
                glare.add(new Glint(x, y, len, 0, 2 + art.nextDouble() * 4, art.nextDouble() * 7,
                        depth < 0.35 ? 0xffd27a : 0xf08a4a));
            }
            clouds.add(new Cloud(20, 36, 70, 2, 3));
            clouds.add(new Cloud(180, 44, 96, 3, 2));
            clouds.add(new Cloud(90, 56, 60, 2, 4));
            clouds.add(new Cloud(10, 66, 44, 2, 3));
            clouds.add(new Cloud(200, 74, 80, 2, 5));
            clouds.add(new Cloud(250, 82, 54, 1, 6));
            birds.add(new Glint(40, 46, 0, 9, 5, 0, 0x1a0c1c));
            birds.add(new Glint(52, 51, 0, 9, 4.4, 1.3, 0x1a0c1c));
            birds.add(new Glint(200, 40, 0, 6, 5.5, 0.6, 0x24101e));
        }

        BufferedImage render() {
            System.arraycopy(backdrop, 0, pix.px, 0, backdrop.length);
            drawSky();
            drawSea();
            Step shown = progress >= 0.5 ? voyage.steps.get(to) : voyage.steps.get(from);
            drawFleet(shown);
            drawFrame(shown);
            if (card > 0) {
                drawCard();
            }
            return pix.image;
        }

        void drawSky() {
            for (Glint s : stars) {
                if (Math.sin(time * s.freq + s.phase) > -0.6) {
                    pix.set((int) s.x, s.y, s.color);
                }
            }
            for (Cloud c : clouds) {
                int x = (int) ((c.x + time * c.speed) % (W + c.len)) - c.len;
                boolean low = c.y > 62;
                for (int r = 0; r < c.thick; r++) {
                    int inset = (c.thick - 1 - r) * 3;
                    boolean rim = r == c.thick - 1;
                    int color = low ? (rim ? 0xe0704a : 0x5a1e3c) : (rim ? 0x5c2850 : 0x2c1438);
                    pix.rect(x + inset, c.y + r, c.len - 2 * inset, 1, color);
                }
            }
            for (Glint b : birds) {
                int x = (int) ((b.x + time * b.speed) % (W + 10)) - 5, y = b.y;
                boolean up = ((int) (time * b.freq + b.phase) & 1) == 0;
                pix.set(x, y + (up ? 1 : -1), b.color);
                pix.set(x - 1, y, b.color);
                pix.set(x + 1, y, b.color);
                pix.set(x - 2, y + (up ? -1 : 1), b.color);
                pix.set(x + 2, y + (up ? -1 : 1), b.color);
            }
            for (int i = TOP * W; i <= HORIZON * W + W - 1; i++) {
                if (island[i] != -1) {
                    pix.px[i] = island[i];
                }
            }
            if (finale > beaconTime()) {
                drawBeacon();
            }
        }

        double beaconTime() {
            return 0.3 + voyage.items.length * 0.08;
        }

        void drawBeacon() {
            for (int dy = -8; dy <= 3; dy++) {
                for (int dx = -7; dx <= 7; dx++) {
                    double d = Math.hypot(dx, dy + 2);
                    int x = apexX + dx, y = apexY + dy;
                    if (d > 3 && d < 7 && (1 - (d - 3) / 4) * 8 > BAYER[y & 3][x & 3] && island[y * W + x] == -1) {
                        pix.set(x, y, 0xc8503a);
                    }
                }
            }
            for (int dx = -1; dx <= 1; dx++) {
                int h = (dx == 0 ? 3 : 1) + (int) (Math.abs(Math.sin(time * 11 + dx * 1.7)) * 2.5);
                for (int k = 0; k < h; k++) {
                    pix.set(apexX + dx, apexY - 1 - k, k == 0 ? 0xe0402a : k < h - 1 ? 0xff8a3a : 0xffd27a);
                }
            }
        }

        void drawSea() {
            double span = W + 12;
            for (Glint g : waves) {
                if (Math.sin(time * g.freq + g.phase) < -0.3) {
                    continue;
                }
                int x = (int) (((g.x - time * g.speed) % span + span) % span) - 6;
                pix.rect(x, g.y, g.len, 1, g.color);
            }
            for (Glint g : glare) {
                if (Math.sin(time * g.freq + g.phase) > 0) {
                    pix.rect((int) Math.round(g.x + Math.sin(time * 1.3 + g.phase)), g.y, g.len, 1, g.color);
                }
            }
        }

        double slotX(Step st, int s) {
            int n = st.slots.length, before = 0, total = 0;
            for (int k = 1; k < n; k++) {
                if (!st.gaps[k]) {
                    continue;
                }
                total++;
                if (k <= s) {
                    before++;
                }
            }
            return (W - n * slotW - GAP * total) / 2.0 + slotW * (s + 0.5) + GAP * before;
        }

        double slotAt(Step a, Step b, double e, int s) {
            return slotX(a, s) + (slotX(b, s) - slotX(a, s)) * e;
        }

        double[] place(Step st, int id) {
            int at = st.live[id], n = st.slots.length;
            if (at == Recorder.HELD) {
                return new double[] {slotX(st, Math.max(0, st.hover)), WATERLINE - LIFT, 1};
            }
            if (at >= n) {
                int k = at - n;
                double spread = st.mid < 0 || k < st.lo || k > st.hi ? 0 : k <= st.mid ? -SPREAD : SPREAD;
                return new double[] {slotX(st, k) + spread, AUX_BASE, AUX_SCALE};
            }
            return new double[] {slotX(st, at), WATERLINE, 1};
        }

        List<Sprite> sprites(Step before, Step after, double e) {
            List<Sprite> out = new ArrayList<>();
            int n = after.slots.length;
            for (int id = 0; id < n; id++) {
                double[] p0 = place(before, id), p1 = place(after, id);
                double dx = p1[0] - p0[0];
                double arc = Math.sin(Math.PI * e) * Math.min(7, Math.abs(dx) / 5) * -Math.signum(dx);
                int at = after.live[id];
                boolean inArr = at >= 0 && at < n;
                out.add(new Sprite(id, inArr ? at : -1, p0[0] + dx * e, p0[1] + (p1[1] - p0[1]) * e + arc,
                        p0[2] + (p1[2] - p0[2]) * e, dx, false, !inArr, at == Recorder.HELD));
            }
            for (int s = 0; s < n; s++) {
                int id = after.slots[s];
                if (id >= 0 && after.live[id] != s) {
                    out.add(new Sprite(id, s, slotAt(before, after, e, s), WATERLINE, 1, 0, true, false, false));
                }
            }
            return out;
        }

        static double ease(double t) {
            return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
        }

        int sailH(int id) {
            return MIN_SAIL + (int) Math.round(voyage.items[id].height * (MAX_SAIL - MIN_SAIL));
        }

        int hullW() {
            return Math.max(9, Math.min(44, (int) slotW - 4));
        }

        int[] dims(Sprite s) {
            int hw = Math.max(9, (int) Math.round(hullW() * s.scale));
            int sh = Math.max(3, (int) Math.round(sailH(s.id) * s.scale));
            return new int[] {hw, hw >= 26 ? HULL_L.length : HULL_S.length, Math.max(3, (int) (hw * 0.52)) | 1, sh};
        }

        int base(Sprite s) {
            double bob = s.held ? Math.sin(time * 2.6 + s.id) * 1.2 : Math.sin(time * 1.8 + s.id * 1.3) * 0.7;
            return (int) Math.round(s.y + bob);
        }

        int mastTop(Sprite s) {
            int[] d = dims(s);
            return base(s) - d[1] - 3 - d[3];
        }

        double homeAt(int slot) {
            return 0.3 + slot * 0.08;
        }

        int[] palette(Sprite s, Step shown) {
            if (s.ghost) {
                return SAIL_GHOST;
            }
            if (finale > 0 && s.slot >= 0 && finale > homeAt(s.slot)) {
                return SAIL_HOME;
            }
            int id = s.id;
            if (shown.kind == Kind.COMPARE && id == shown.a) {
                return SAIL_A;
            }
            if (shown.kind == Kind.COMPARE && id == shown.b) {
                return SAIL_B;
            }
            if (shown.kind == Kind.SWAP && (id == shown.a || id == shown.b)) {
                return SAIL_SWAP;
            }
            if (shown.kind == Kind.WRITE && id == shown.a) {
                return SAIL_A;
            }
            if (id == shown.pivot) {
                return SAIL_PIVOT;
            }
            if (shown.settled[id]) {
                return SAIL_LAUREL;
            }
            if (shown.lo >= 0) {
                int at = shown.live[id];
                int n = shown.slots.length;
                int k = at == Recorder.HELD ? shown.hover : at >= n ? at - n : at;
                if (k < shown.lo || k > shown.hi) {
                    return SAIL_DIM;
                }
            }
            return SAIL;
        }

        void drawFleet(Step shown) {
            List<Step> steps = voyage.steps;
            Step before = steps.get(from), after = steps.get(to);
            double e = ease(progress);
            List<Sprite> sprites = sprites(before, after, e);
            sprites.sort(Comparator.comparingInt((Sprite s) -> s.ghost ? 0 : s.lifted ? 2 : 1)
                    .thenComparingDouble(s -> s.y));
            for (Sprite s : sprites) {
                drawSprite(s, shown);
            }

            int chars = Math.max(1, ((int) slotW - 5) / 4);
            for (Sprite s : sprites) {
                if (s.held) {
                    continue;
                }
                Item item = voyage.items[s.id];
                int[] pal = palette(s, shown);
                int c = pal == SAIL ? PARCH : pal == SAIL_GHOST || pal == SAIL_DIM ? FAINT : pal[0];
                int cx = (int) Math.round(s.x);
                if (s.lifted) {
                    if (Math.abs(s.y - AUX_BASE) < 3) {
                        label(fit(item.name, chars), cx, AUX_BASE + 4, c);
                    }
                    continue;
                }
                label(fit(item.name, chars), cx, WATERLINE + 7, c);
                label(fit(item.detail, chars), cx, WATERLINE + 13, s.ghost ? FAINT : DIM);
            }
            for (int s = 0; s < voyage.items.length; s++) {
                label("[" + s + "]", (int) Math.round(slotAt(before, after, e, s)), WATERLINE + 19, 0x6a5a6a);
            }
            if (shown.lo >= 0) {
                drawRange(before, after, e, shown);
            }
            if (shown.kind == Kind.COMPARE) {
                drawConnector(sprites, shown);
            }
        }

        void drawRange(Step before, Step after, double e, Step shown) {
            double half = slotW / 2;
            int x0 = (int) Math.round(slotAt(before, after, e, shown.lo) - half) + 1;
            int x1 = (int) Math.round(slotAt(before, after, e, shown.hi) + half) - 1;
            int y = TOP;
            pix.rect(x0, y, x1 - x0 + 1, 1, BRONZE);
            pix.rect(x0, y, 1, 3, BRONZE);
            pix.rect(x1, y, 1, 3, BRONZE);
            if (shown.mid >= 0) {
                int mx = (int) Math.round((slotAt(before, after, e, shown.mid) + slotAt(before, after, e, shown.mid + 1)) / 2);
                pix.rect(mx, y, 1, 4, PARCH);
            }
        }

        void drawSprite(Sprite s, Step shown) {
            int[] pal = palette(s, shown);
            int[] d = dims(s);
            int cx = (int) Math.round(s.x), base = base(s), hw = d[0];
            boolean moving = progress < 1 && Math.abs(s.dx) > 0.5;
            if (s.held) {
                spout(cx, base, hw);
            } else if (s.lifted && Math.abs(s.y - AUX_BASE) < 3) {
                river(cx, base, hw);
            }
            pix.ghost = s.ghost;
            ship(cx, base, hw, d[1], d[2], d[3], pal, moving);
            pix.ghost = false;
            if (moving && !s.lifted) {
                for (int k = 1; k <= 7; k++) {
                    if (((k + (int) (time * 20)) & 1) == 0) {
                        pix.set(cx - (int) Math.signum(s.dx) * (hw / 2 + k), base + 1 + (k > 4 ? 1 : 0), FOAM);
                    }
                }
            }
            if (s.held) {
                String key = "KEY";
                pix.text(key, cx - Pix.width(key, 1) / 2, (base + WATERLINE) / 2, INK, 1);
            }
            if (finale > 0 && s.slot >= 0 && !s.ghost) {
                double t = finale - homeAt(s.slot);
                int top = base - d[1] - d[3], half = d[2] / 2;
                if (t > 0 && t < 0.35 && ((int) (t * 20) & 1) == 0) {
                    pix.set(cx - half - 3, top + 2, 0xfff8e0);
                    pix.set(cx + half + 3, top + 5, 0xfff8e0);
                    pix.set(cx, top - 6, 0xfff8e0);
                }
            }
        }

        void ship(int cx, int base, int hullW, int hullH, int sailW, int sailH, int[] pal, boolean rowing) {
            boolean large = hullH == HULL_L.length;
            int[][] rows = large ? HULL_L : HULL_S;
            int x0 = cx - hullW / 2, x1 = x0 + hullW - 1, d = base - hullH + 1;
            int sTop = d - 1 - sailH, sBot = d - 2, sx0 = cx - sailW / 2, mt = sTop - 3;

            pix.line(cx, mt + 1, x0 + 1, d - 1, ROPE);
            pix.line(cx, mt + 1, x1, d - 1, ROPE);
            pix.rect(cx, mt, 1, d - mt, MAST);
            pix.rect(sx0 - 1, sTop - 1, sailW + 2, sailH + 2, OUTLINE);
            pix.rect(sx0, sTop, sailW, sailH, pal[0]);
            pix.rect(sx0, sTop, 1, sailH, pal[1]);
            for (int y = sTop + 5; y < sBot - 2; y += 5) {
                pix.rect(sx0 + 1, y, sailW - 1, 1, pal[1]);
            }
            if (sailH >= 6) {
                pix.rect(sx0, sTop + 1, sailW, 1, pal[2]);
            }
            if (sailH > 14) {
                pix.rect(sx0, sBot - 1, sailW, 1, pal[2]);
            }
            if (sailW >= 11 && sailH >= 18) {
                int my = (sTop + sBot) / 2;
                pix.set(cx, my - 1, pal[2]);
                pix.set(cx - 1, my, pal[2]);
                pix.set(cx + 1, my, pal[2]);
                pix.set(cx, my + 1, pal[2]);
            }
            pix.rect(sx0 - 2, sTop - 1, sailW + 4, 1, YARD);
            int flap = (int) (time * 6) & 1;
            pix.set(cx + 1, mt, pal[2]);
            pix.set(cx + 2, mt + flap, pal[2]);
            pix.set(cx + 3, mt, pal[2]);

            for (int r = 0; r < rows.length; r++) {
                pix.rect(x0 + rows[r][0], d + r, hullW - rows[r][0] - rows[r][1], 1, r == 0 ? DECK : r == 2 ? TERRA : HULL);
            }
            for (int[] p : large ? STERN_L : STERN_S) {
                pix.set(x0 + p[0], d + p[1], HULL);
            }
            for (int[] p : large ? PROW_L : PROW_S) {
                pix.set(x1 + p[0], d + p[1], HULL);
            }
            int ramRow = rows.length - 2;
            pix.rect(x1 - rows[ramRow][1], d + ramRow, rows[ramRow][1] + (large ? 4 : 3), 1, BRONZE);
            pix.set(x1 - 3, d + 1, EYE);
            if (large) {
                pix.set(x1 - 5, d + 1, EYE);
                for (int x = x0 + 4; x <= x1 - 6; x += 3) {
                    pix.set(x, d + 4, DECK);
                }
            }

            int stroke = rowing ? (int) (time * 8) & 1 : 0;
            for (int ox = x0 + 4; ox <= x1 - 6; ox += 3) {
                pix.set(ox - stroke, base + 1, OAR);
                pix.set(ox - 1 - stroke, base + 2, OAR);
            }
            int churn = (int) (time * 6);
            for (int x = x0 + 1; x <= x1 + 3; x++) {
                if ((x + churn) % 5 == 0) {
                    pix.set(x, base + 1, FOAM);
                }
            }
        }

        void river(int cx, int base, int hw) {
            int t = (int) (time * 8);
            for (int x = cx - hw / 2 - 2; x <= cx + hw / 2 + 3; x++) {
                pix.set(x, base + 1, Math.floorMod(x + t, 4) == 0 ? FOAM : SPRAY_LT);
                pix.set(x, base + 2, Math.floorMod(x - t, 3) == 0 ? SPRAY_LT : SPRAY);
            }
        }

        void spout(int cx, int base, int hullW) {
            int half = Math.max(2, hullW / 2 - 3), t = (int) (time * 12);
            for (int y = base + 1; y <= WATERLINE + 1; y++) {
                for (int x = cx - half; x <= cx + half; x++) {
                    int k = Math.floorMod(x * 7 + y * 13 + t, 5);
                    pix.set(x, y, k == 0 ? FOAM : k < 3 ? SPRAY_LT : SPRAY);
                }
            }
            for (int i = -half - 3; i <= half + 3; i++) {
                if (((i + t) & 3) == 0) {
                    pix.set(cx + i, WATERLINE + 1, FOAM);
                }
            }
        }

        void drawConnector(List<Sprite> sprites, Step shown) {
            Sprite sa = null, sb = null;
            for (Sprite s : sprites) {
                if (s.ghost) {
                    continue;
                }
                if (s.id == shown.a) {
                    sa = s;
                }
                if (s.id == shown.b) {
                    sb = s;
                }
            }
            if (sa == null || sb == null) {
                return;
            }
            int ax = (int) Math.round(sa.x), bx = (int) Math.round(sb.x), at = mastTop(sa), bt = mastTop(sb);
            int top = Math.min(at, bt);
            for (Sprite s : sprites) {
                if (!s.ghost && s.x >= Math.min(ax, bx) - 1 && s.x <= Math.max(ax, bx) + 1) {
                    top = Math.min(top, mastTop(s));
                }
            }
            int y = Math.max(TOP + 4, top - 6);
            for (int yy = y + 1; yy < at - 1; yy += 2) {
                pix.set(ax, yy, GOLD);
            }
            for (int yy = y + 1; yy < bt - 1; yy += 2) {
                pix.set(bx, yy, TEAL);
            }
            for (int x = Math.min(ax, bx); x <= Math.max(ax, bx); x += 2) {
                pix.set(x, y, PARCH);
            }
            int op = ax <= bx ? shown.cmp : -shown.cmp;
            int mx = (ax + bx) / 2;
            pix.rect(mx - 3, y - 4, 7, 9, INK);
            pix.text(op > 0 ? ">" : op < 0 ? "<" : "=", mx - 1, y - 2, PARCH, 1);
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

        void center(String s, int y, int c, int scale) {
            pix.text(s, (W - Pix.width(s, scale)) / 2, y, c, scale);
        }

        static int width(List<Seg> segs) {
            return Pix.width(segs.stream().map(seg -> seg.text).reduce("", String::concat), 1);
        }

        void line(List<Seg> segs, int y) {
            int x = (W - width(segs)) / 2;
            for (Seg s : segs) {
                x = pix.text(s.text, x, y, s.color, 1);
            }
        }

        static List<Seg> say(String text, int color) {
            return Collections.singletonList(new Seg(text, color));
        }

        void meander(int y) {
            for (int x = 0; x < W; x++) {
                for (int r = 0; r < MEANDER.length; r++) {
                    if (MEANDER[r].charAt(x % 6) == '#') {
                        pix.set(x, y + r, BRONZE_DK);
                    }
                }
            }
        }

        static String range(int lo, int hi) {
            return "[" + lo + ".." + hi + "]";
        }

        List<List<Seg>> narrate(Step st) {
            Item[] items = voyage.items;
            int n = items.length;
            if (st.kind == Kind.START) {
                return Arrays.asList(say("THE FLEET LIES UNSORTED", PARCH), say("PRESS SPACE TO SET SAIL", DIM));
            }
            if (st.kind == Kind.COMPARE) {
                Item a = items[st.a];
                Item b = items[st.b];
                String rel = st.cmp > 0 ? "> 0" : st.cmp < 0 ? "< 0" : "= 0";
                String verb = st.cmp > 0 ? " BELONGS AFTER " : st.cmp < 0 ? " BELONGS BEFORE " : " TIES WITH ";
                List<Seg> two = new ArrayList<>(Arrays.asList(new Seg(a.said, GOLD), new Seg(verb, DIM),
                        new Seg(b.said, TEAL)));
                if (st.live[st.a] == Recorder.HELD || st.live[st.b] == Recorder.HELD) {
                    two.add(new Seg("  - KEY HELD", DIM));
                    if (width(two) > W - 8) {
                        two.remove(two.size() - 1);
                    }
                }
                return Arrays.asList(Arrays.asList(new Seg("COMPARE(", PARCH), new Seg(a.said, GOLD),
                        new Seg(", ", PARCH), new Seg(b.said, TEAL), new Seg(")  " + rel, PARCH)), two);
            }
            if (st.kind == Kind.SWAP) {
                int i = st.live[st.a] % n;
                int j = st.live[st.b] % n;
                return Arrays.asList(say("SWAP ARR[" + i + "] AND ARR[" + j + "]", PARCH),
                        Arrays.asList(new Seg(items[st.a].said, EMBER), new Seg("  TRADES PLACES WITH  ", DIM),
                                new Seg(items[st.b].said, EMBER)));
            }
            if (st.kind == Kind.WRITE) {
                int k = st.live[st.a];
                int from = st.from;
                String target = k < n ? "ARR[" + k + "]" : "AUX[" + (k - n) + "]";
                String how = from == Recorder.HELD ? "THE KEY IS SET DOWN IN ITS PLACE"
                        : from >= n ? "TAKEN BACK FROM AUX[" + (from - n) + "]"
                        : from == k ? "ALREADY IN PLACE"
                        : from == k - 1 ? "SHIFTED ONE SLOT RIGHT"
                        : from == k + 1 ? "SHIFTED ONE SLOT LEFT"
                        : "MOVED FROM ARR[" + from + "]";
                boolean keyHeld = false;
                for (int at : st.live) {
                    keyHeld |= at == Recorder.HELD;
                }
                return Arrays.asList(Arrays.asList(new Seg(target + " = ", PARCH), new Seg(items[st.a].said, GOLD)),
                        say(how + (keyHeld ? "  - KEY HELD" : ""), DIM));
            }
            if (st.kind == Kind.COPY) {
                String what = st.mid < 0 ? "" : "MERGE THE SORTED HALVES " + range(st.lo, st.mid) + " AND "
                        + range(st.mid + 1, st.hi) + ": SMALLER FRONT COMES DOWN FIRST";
                return Arrays.asList(say("COPY ARR" + range(st.lo, st.hi) + " INTO AUX", PARCH), say(what, DIM));
            }
            if (st.kind == Kind.SPLIT) {
                return Arrays.asList(say("SPLIT ARR" + range(st.lo, st.hi) + " AT MID = " + st.mid, PARCH),
                        say("LEFT " + range(st.lo, st.mid) + "   RIGHT " + range(st.mid + 1, st.hi), DIM));
            }
            if (st.kind == Kind.FOCUS) {
                List<Seg> two = st.b < 0 ? say("", DIM)
                        : Arrays.asList(new Seg("PIVOT ", DIM), new Seg(items[st.b].said, LAUREL),
                                new Seg(" IS IN ITS FINAL PLACE", DIM));
                return Arrays.asList(say("WORK ON ARR" + range(st.lo, st.hi) + " ONLY", PARCH), two);
            }
            if (st.kind == Kind.PIVOT) {
                return Arrays.asList(Arrays.asList(new Seg("PIVOT = ARR[" + st.live[st.a] + "] = ", PARCH),
                        new Seg(items[st.a].said, PURPLE)), say("EVERY OTHER SHIP IN THE RANGE IS COMPARED WITH IT", DIM));
            }
            if (voyage.cut) {
                return Arrays.asList(say("THE VOYAGE WAS HALTED AFTER " + Recorder.LIMIT + " STEPS", TERRA),
                        say("DOES THE SORTER EVER STOP?", DIM));
            }
            String counts = st.compares + " COMPARES, " + st.swaps + " SWAPS, " + st.moves + " MOVES";
            if (voyage.bypassed > 0) {
                counts = voyage.bypassed + " COMPARETO() CALLS WENT AROUND SORTER.COMPARE()";
            }
            return Arrays.asList(say("HOMECOMING: THE FLEET SAILS IN ORDER", GOLD),
                    say(counts, voyage.bypassed > 0 ? TERRA : PARCH));
        }

        void drawFrame(Step shown) {
            pix.rect(0, 0, W, TOP, INK);
            pix.rect(0, BOTTOM + 1, W, H - BOTTOM - 1, INK);
            meander(21);
            meander(152);

            pix.text("A SORTING ODYSSEY", 6, 5, PARCH, 1);
            String book = "BOOK " + NUMERALS[chapterIndex] + ": " + chapter().title;
            pix.text(book, W - 6 - Pix.width(book, 1), 5, BRONZE, 1);
            pix.text("FLEET: " + fleet().type + "[" + voyage.items.length + "]", 6, 13, DIM, 1);
            double sp = SPEEDS[speed];
            String stats = "STEP " + to + "/" + last() + "  COMPARES " + shown.compares + "  SWAPS " + shown.swaps
                    + "  MOVES " + shown.moves + "  " + (sp < 1 ? "0.5" : String.valueOf((int) sp)) + "/S "
                    + (playing || card > 0 ? " >" : " ||");
            pix.text(stats, W - 6 - Pix.width(stats, 1), 13, DIM, 1);

            List<List<Seg>> lines = narrate(shown);
            line(lines.get(0), 159);
            line(lines.get(1), 166);
            line(Arrays.asList(new Seg("SPACE", BRONZE), new Seg(" PLAY/PAUSE   ", DIM), new Seg("<- ->", BRONZE),
                    new Seg(" STEP   ", DIM), new Seg("1-" + chapters.size(), BRONZE), new Seg(" BOOK   ", DIM),
                    new Seg("D", BRONZE), new Seg(" FLEET   ", DIM), new Seg("R", BRONZE), new Seg(" RESTART   ", DIM),
                    new Seg("+ -", BRONZE), new Seg(" SPEED", DIM)), 173);
        }

        void drawCard() {
            boolean edge = CARD_TIME - card < 0.25 || card < 0.4;
            pix.darken(TOP, BOTTOM);
            if (edge) {
                return;
            }
            pix.darken(TOP, BOTTOM);
            int y = 38;
            center("A SORTING ODYSSEY", y, DIM, 1);
            center("BOOK " + NUMERALS[chapterIndex], y + 10, BRONZE, 2);
            center(chapter().title, y + 24, PARCH, 2);
            center(chapter().epithet, y + 42, PARCH, 1);
            center("FLEET: " + fleet().blurb, y + 58, DIM, 1);
            center("SAILING ON " + chapter().file, y + 66, DIM, 1);
            center("EVERY STEP YOU SEE IS ONE CALL INTO SORTER.JAVA", y + 82, BRONZE, 1);
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
