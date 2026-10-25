package forkjoinsort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Олон урсгалаас зэрэг бичигддэг тул БҮХ бүтэц нь thread-safe байх ёстой.
 * Энэ нь өөрөө 6-7-р бүлгийн синхрончлолын жишээ юм:
 *   - AtomicInteger      : зангилааны дугаарыг атомикаар олгоно (цоожгүй)
 *   - ConcurrentLinkedQueue : цоожгүй (lock-free) дараалал
 */
public class TraceRecorder {

    /** Энэ хэмжээнээс жижиг мужийн утгуудыг хуулж хадгална (санах ой хэмнэнэ). */
    public static final int SNAPSHOT_MAX = 48;

    private final AtomicInteger nextId = new AtomicInteger(0);
    private final ConcurrentLinkedQueue<TraceNode> nodes = new ConcurrentLinkedQueue<>();
    private final boolean enabled;
    private final long t0;

    public TraceRecorder(boolean enabled) {
        this.enabled = enabled;
        this.t0 = System.nanoTime();
    }

    public boolean isEnabled() { return enabled; }

    public TraceNode open(int parentId, int lo, int hi, int depth, Object[] arr) {
        if (!enabled) return null;
        TraceNode n = new TraceNode(nextId.getAndIncrement(), parentId, lo, hi, depth);
        n.thread = Thread.currentThread().getName();
        n.forkNanos = System.nanoTime() - t0;
        n.before = snapshot(arr, lo, hi);
        nodes.add(n);
        return n;
    }

    public void close(TraceNode n, boolean leaf, Object[] arr) {
        if (n == null) return;
        n.leaf = leaf;
        n.doneNanos = System.nanoTime() - t0;
        n.after = snapshot(arr, n.lo, n.hi);
    }

    private static String snapshot(Object[] arr, int lo, int hi) {
        if (hi - lo > SNAPSHOT_MAX) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = lo; i < hi; i++) {
            if (i > lo) sb.append(", ");
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    /** Дуусмагц дуудна: дугаараар эрэмбэлсэн тогтвортой жагсаалт буцаана. */
    public List<TraceNode> snapshotNodes() {
        List<TraceNode> list = new ArrayList<>(nodes);
        Collections.sort(list, (a, b) -> Integer.compare(a.id, b.id));
        return list;
    }

    public int count() { return nodes.size(); }
}
