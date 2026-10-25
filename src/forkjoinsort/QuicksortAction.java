package forkjoinsort;

import java.util.concurrent.RecursiveAction;

/**
 * Fork-Join Quicksort (гурван талт хуваалттай).
 *
 * Mergesort-оос ЗАРЧМЫН ялгаа:
 *   Mergesort — гол ажил нь хуваалтын ДАРАА (merge).
 *   Quicksort — гол ажил нь хуваалтын ӨМНӨ (partition). Нэгтгэх алхам БАЙХГҮЙ.
 */
public class QuicksortAction<T extends Comparable<? super T>> extends RecursiveAction {

    /** Энэ гүнээс цааш параллельчлахаа больж нэг урсгалаар дуусгана (стек хамгаалалт). */
    public static final int MAX_PARALLEL_DEPTH = 48;

    private final T[] a;
    private final int lo, hi, depth, parentId, threshold;
    private final TraceRecorder tr;

    public QuicksortAction(T[] a, int lo, int hi, int threshold,
                           TraceRecorder tr, int parentId, int depth) {
        this.a = a; this.lo = lo; this.hi = hi;
        this.threshold = threshold; this.tr = tr;
        this.parentId = parentId; this.depth = depth;
    }

    @Override
    protected void compute() {
        TraceNode node = tr.open(parentId, lo, hi, depth, a);
        int myId = (node == null) ? -1 : node.id;

        if (hi - lo <= threshold) {                       // ===== НАВЧ =====
            SequentialSort.insertionSort(a, lo, hi);
            tr.close(node, true, a);
            return;
        }
        if (depth >= MAX_PARALLEL_DEPTH) {                // ===== хамгаалалт =====
            SequentialSort.quicksort3(a, lo, hi, threshold);
            tr.close(node, true, a);
            return;
        }

        int[] r = SequentialSort.partition3(a, lo, hi);   // ЦУВАА ажил
        int ll = lo,     lr = r[0];                       // pivot-оос БАГА хэсэг
        int rl = r[1]+1, rr = hi;                         // pivot-оос ИХ хэсэг
        // r[0]..r[1] нь pivot-тай тэнцүү — аль хэдийн эцсийн байрандаа, дахин хөндөхгүй

        boolean hasL = lr - ll > 0, hasR = rr - rl > 0;
        if (hasL && hasR) {
            invokeAll(new QuicksortAction<>(a, ll, lr, threshold, tr, myId, depth + 1),
                      new QuicksortAction<>(a, rl, rr, threshold, tr, myId, depth + 1));
        } else if (hasL) {
            new QuicksortAction<>(a, ll, lr, threshold, tr, myId, depth + 1).compute();
        } else if (hasR) {
            new QuicksortAction<>(a, rl, rr, threshold, tr, myId, depth + 1).compute();
        }
        tr.close(node, false, a);
    }
}
