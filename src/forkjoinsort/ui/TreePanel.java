package forkjoinsort.ui;

import forkjoinsort.TraceNode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * РЕКУРСИЙН МОД-ыг зурах самбар.
 *
 * Зангилаа бүр = compute() функцийн нэг дуудлага.
 *   - өндөр (y)   = рекурсийн гүн
 *   - өнгө        = АЛЬ ажилчин урсгал гүйцэтгэсэн (work stealing харагдана)
 *   - хүрээ       = навч (threshold хүрсэн) бол зузаан
 *   - шошго       = [lo, hi) муж ба хэмжээ
 */
public class TreePanel extends JPanel {

    private List<TraceNode> nodes = Collections.emptyList();
    private final Map<Integer, List<TraceNode>> children = new HashMap<>();
    private final Map<Integer, Point> pos = new HashMap<>();
    private final Map<String, Color> threadColor = new LinkedHashMap<>();
    private TraceNode root;

    /** Нэг алхам = нэг ҮЙЛ ЯВДАЛ: зангилаа FORK хийгдэв, эсвэл JOIN хийгдэж хаагдав. */
    private static final int FORK = 0, JOIN = 1;
    private static final class Ev {
        final TraceNode n; final int type; final long t;
        Ev(TraceNode n, int type, long t) { this.n = n; this.type = type; this.t = t; }
    }
    private final List<Ev> events = new ArrayList<>();
    /** 0 = харагдахгүй, 1 = нээгдсэн (ажиллаж байна), 2 = хаагдсан (join дууссан) */
    private final Map<Integer, Integer> state = new HashMap<>();

    private double zoom = 1.0;
    private int panX = 0, panY = 0;
    private int visibleCount = Integer.MAX_VALUE;   // алхамчилсан горимд
    private TraceNode selected;
    private Consumer<TraceNode> onSelect;

    private static final Color[] PALETTE = {
        new Color(0x4E79A7), new Color(0xF28E2B), new Color(0x59A14F),
        new Color(0xE15759), new Color(0x76B7B2), new Color(0xEDC948),
        new Color(0xB07AA1), new Color(0xFF9DA7), new Color(0x9C755F),
        new Color(0xBAB0AC), new Color(0x86BCB6), new Color(0xD37295)
    };

    private int nodeW = 72, nodeH = 20, levelH = 56, leafGap = 12;

    public TreePanel() {
        setBackground(Color.WHITE);
        MouseAdapter ma = new MouseAdapter() {
            Point drag;
            @Override public void mousePressed(MouseEvent e) {
                drag = e.getPoint();
                TraceNode n = hit(e.getX(), e.getY());
                if (n != null) { selected = n; if (onSelect != null) onSelect.accept(n); repaint(); }
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (drag == null) return;
                panX += e.getX() - drag.x; panY += e.getY() - drag.y;
                drag = e.getPoint(); repaint();
            }
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                double f = (e.getWheelRotation() < 0) ? 1.1 : 1 / 1.1;
                zoom = Math.max(0.08, Math.min(4.0, zoom * f));
                repaint();
            }
        };
        addMouseListener(ma); addMouseMotionListener(ma); addMouseWheelListener(ma);
        setToolTipText("");
    }

    public void setOnSelect(Consumer<TraceNode> c) { this.onSelect = c; }

    /** Шинэ модыг ачаална. */
    public void setNodes(List<TraceNode> ns) {
        this.nodes = (ns == null) ? Collections.<TraceNode>emptyList() : ns;
        children.clear(); pos.clear(); threadColor.clear(); events.clear(); state.clear();
        root = null; selected = null; visibleCount = Integer.MAX_VALUE;
        for (TraceNode n : nodes) {
            if (n.parentId < 0) root = n;
            else children.computeIfAbsent(n.parentId, k -> new ArrayList<>()).add(n);
            threadColor.computeIfAbsent(n.thread == null ? "?" : n.thread,
                k -> PALETTE[threadColor.size() % PALETTE.length]);
        }
        for (List<TraceNode> ch : children.values())
            ch.sort(Comparator.comparingInt(x -> x.lo));
        buildEvents();
        layoutTree();
        fit();
        repaint();
    }

    /** DFS: навчдыг зүүнээс баруун тийш дараалуулж, эцгийг хүүхдүүдийн дунд тавина. */
    private void layoutTree() {
        pos.clear();
        if (root == null) return;
        int[] leafX = {0};
        assign(root, leafX);
    }

    private int assign(TraceNode n, int[] leafX) {
        List<TraceNode> ch = children.get(n.id);
        int x;
        if (ch == null || ch.isEmpty()) {
            x = leafX[0] * (nodeW + leafGap) + nodeW / 2;
            leafX[0]++;
        } else {
            int first = -1, last = -1;
            for (TraceNode c : ch) {
                int cx = assign(c, leafX);
                if (first < 0) first = cx;
                last = cx;
            }
            x = (first + last) / 2;
        }
        pos.put(n.id, new Point(x, n.depth * levelH + nodeH));
        return x;
    }

    private Dimension treeSize() {
        int maxX = 0, maxY = 0;
        for (Point p : pos.values()) { maxX = Math.max(maxX, p.x); maxY = Math.max(maxY, p.y); }
        return new Dimension(maxX + nodeW, maxY + nodeH * 3);
    }

    /** Модыг цонхонд багтаана. */
    public void fit() {
        Dimension d = treeSize();
        int w = Math.max(1, getWidth() - 20), h = Math.max(1, getHeight() - 20);
        if (d.width <= 1 || d.height <= 1) return;
        zoom = Math.max(0.08, Math.min(2.0, Math.min(w / (double) d.width, h / (double) d.height)));
        // модыг цонхны төвд байрлуулна
        panX = (int) ((getWidth()  - d.width  * zoom) / 2);
        panY = (int) ((getHeight() - d.height * zoom) / 2) + (int) (nodeH * zoom);
        repaint();
    }

    /** FORK ба JOIN үйл явдлуудыг бодит хугацаагаар эрэмбэлнэ. */
    private void buildEvents() {
        for (TraceNode n : nodes) {
            events.add(new Ev(n, FORK, n.forkNanos));
            events.add(new Ev(n, JOIN, n.doneNanos));
        }
        events.sort((a, b) -> {
            if (a.t != b.t) return Long.compare(a.t, b.t);
            if (a.type != b.type) return Integer.compare(a.type, b.type);
            return Integer.compare(a.n.id, b.n.id);
        });
    }

    /** Эхний c үйл явдлыг хэрэглэж, зангилаа бүрийн төлөвийг тооцно. */
    public void setVisibleCount(int c) {
        this.visibleCount = c;
        state.clear();
        int lim = Math.min(c, events.size());
        for (int i = 0; i < lim; i++) {
            Ev e = events.get(i);
            state.put(e.n.id, e.type == FORK ? 1 : 2);
        }
        repaint();
    }

    public int totalSteps() { return events.size(); }

    /** Тухайн алхмын тайлбар: юу, хаана, аль урсгал дээр болсныг нэг мөрөөр. */
    public String stepLabel(int k) {
        if (events.isEmpty()) return " ";
        if (k <= 0) return "Алхам 0 / " + events.size() + " — эхлэл";
        Ev e = events.get(Math.min(k, events.size()) - 1);
        String w = e.n.thread == null ? "?" : e.n.thread.replaceAll(".*-(worker-\\d+)$", "$1");
        String what;
        if (e.type == FORK)
            what = e.n.leaf ? "эрэмбэлж эхлэв (навч, insertion sort)"
                            : "FORK — хоёр дэд ажилд хуваав";
        else
            what = e.n.leaf ? "дууслаа — эцэгтээ JOIN хийхэд бэлэн"
                            : "JOIN — хоёр хүү дуусаж, merge хийв";
        return "Алхам " + Math.min(k, events.size()) + " / " + events.size()
             + " — [" + e.n.lo + ", " + e.n.hi + ") n=" + e.n.size()
             + " · " + what + " · " + w;
    }
    public Map<String, Color> threadColors() { return threadColor; }

    private TraceNode hit(int sx, int sy) {
        for (TraceNode n : nodes) {
            Point p = pos.get(n.id);
            if (p == null) continue;
            int x = (int) (p.x * zoom) + panX, y = (int) (p.y * zoom) + panY;
            int w = (int) (nodeW * zoom), h = (int) (nodeH * zoom);
            if (sx >= x - w / 2 && sx <= x + w / 2 && sy >= y - h / 2 && sy <= y + h / 2) return n;
        }
        return null;
    }

    @Override public String getToolTipText(MouseEvent e) {
        TraceNode n = hit(e.getX(), e.getY());
        if (n == null) return null;
        return "<html>#" + n.id + " [" + n.lo + ", " + n.hi + ") n=" + n.size()
             + "<br>гүн=" + n.depth + (n.leaf ? " <b>навч</b>" : "")
             + "<br>урсгал: " + n.thread + "</html>";
    }

    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintTree((Graphics2D) g, getWidth(), getHeight());
    }

    /** Алхамчилсан горимд тухайн зангилааны төлөв; эс бөгөөс бүгд хаагдсан. */
    private int stateOf(TraceNode n, boolean stepping) {
        if (!stepping) return 2;
        Integer v = state.get(n.id);
        return v == null ? 0 : v;
    }

    /** Цонхгүйгээр зураг болгон гаргахад мөн ашиглагдана (headless screenshot). */
    public void paintTree(Graphics2D g2, int W, int H) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setColor(Color.WHITE); g2.fillRect(0, 0, W, H);

        if (root == null) {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
            g2.drawString("Мод хоосон — Ажиллуулах товчийг дарна уу.", 24, 32);
            return;
        }

        // Алхамчилсан горимд зөвхөн болсон үйл явдлууд харагдана.
        // visibleCount = MAX бол бүх зангилаа ХААГДСАН төлөвтэй (ердийн дүрслэл).
        boolean stepping = visibleCount < Integer.MAX_VALUE;

        // --- ирмэгүүд ---
        g2.setStroke(new BasicStroke(Math.max(1f, (float) (1.2 * zoom))));
        for (TraceNode n : nodes) {
            if (stateOf(n, stepping) == 0) continue;
            List<TraceNode> ch = children.get(n.id);
            if (ch == null) continue;
            Point p = pos.get(n.id);
            for (TraceNode c : ch) {
                if (stateOf(c, stepping) == 0) continue;
                Point q = pos.get(c.id);
                if (p == null || q == null) continue;
                g2.setColor(new Color(0xB0, 0xB8, 0xC0));
                g2.draw(new Line2D.Double(p.x * zoom + panX, p.y * zoom + panY,
                                          q.x * zoom + panX, q.y * zoom + panY));
            }
        }

        // --- зангилаанууд ---
        int fs = (int) Math.round(9 * zoom);
        Font f = new Font("SansSerif", Font.PLAIN, Math.max(1, fs));
        g2.setFont(f);
        for (TraceNode n : nodes) {
            int st = stateOf(n, stepping);
            if (st == 0) continue;
            Point p = pos.get(n.id);
            if (p == null) continue;
            double w = nodeW * zoom, h = nodeH * zoom;
            double x = p.x * zoom + panX - w / 2, y = p.y * zoom + panY - h / 2;
            if (x + w < 0 || y + h < 0 || x > W || y > H) continue;

            Color base = threadColor.getOrDefault(n.thread == null ? "?" : n.thread, Color.GRAY);
            boolean open = (st == 1);   // нээгдсэн ч хараахан JOIN хийгдээгүй

            if (open) {                 // АЖИЛЛАЖ БАЙНА — цагаан дотортой, зузаан хүрээтэй
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Double(x, y, w, h, 6 * zoom, 6 * zoom));
                g2.setStroke(new BasicStroke(Math.max(1.8f, (float) (2.2 * zoom))));
                g2.setColor(base);
                g2.draw(new RoundRectangle2D.Double(x, y, w, h, 6 * zoom, 6 * zoom));
            } else {                    // ХААГДСАН — join дууссан
                g2.setColor(n.leaf ? base : new Color(base.getRed(), base.getGreen(), base.getBlue(), 90));
                g2.fill(new RoundRectangle2D.Double(x, y, w, h, 6 * zoom, 6 * zoom));
                g2.setStroke(new BasicStroke(n == selected ? 2.5f : (n.leaf ? 1.6f : 0.9f)));
                g2.setColor(n == selected ? Color.BLACK : base.darker());
                g2.draw(new RoundRectangle2D.Double(x, y, w, h, 6 * zoom, 6 * zoom));
            }
            if (n == selected) {
                g2.setStroke(new BasicStroke(2.5f));
                g2.setColor(Color.BLACK);
                g2.draw(new RoundRectangle2D.Double(x, y, w, h, 6 * zoom, 6 * zoom));
            }

            if (fs >= 7) {
                String s = n.size() + "";
                if (w > 54) s = "[" + n.lo + "," + n.hi + ") " + n.size();
                g2.setColor(open ? base.darker() : (n.leaf ? Color.WHITE : Color.DARK_GRAY));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(s, (float) (x + (w - fm.stringWidth(s)) / 2),
                                 (float) (y + h / 2 + fm.getAscent() / 2.0 - 1));
            }
        }
    }

    /** Цонх нээхгүйгээр PNG үүсгэнэ (тайланд зураг оруулахад). */
    public BufferedImage toImage(int w, int h) {
        setSize(w, h);
        fit();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        paintTree(g2, w, h);
        g2.dispose();
        return img;
    }
}
