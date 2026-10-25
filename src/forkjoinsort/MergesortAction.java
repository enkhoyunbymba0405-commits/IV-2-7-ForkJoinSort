package forkjoinsort;

import java.util.concurrent.RecursiveAction;

/**
 * Fork-Join Mergesort.
 *
 * Сурах бичиг: эрэмбэлэх ажил нь утга БУЦААДАГГҮЙ (массивыг газар дээр нь
 * өөрчилнө) тул RecursiveTask биш, RecursiveAction-ийг өргөтгөнө.
 *
 * Ажлын дараалал:  хуваах (хямд) -> fork -> join -> НЭГТГЭХ (O(n), цуваа)
 */
public class MergesortAction<T extends Comparable<? super T>> extends RecursiveAction {

    private final T[] a, tmp;
    private final int lo, hi, depth, parentId, threshold;
    private final TraceRecorder tr;

    public MergesortAction(T[] a, T[] tmp, int lo, int hi, int threshold,
                           TraceRecorder tr, int parentId, int depth) {
        this.a = a; this.tmp = tmp; this.lo = lo; this.hi = hi;
        this.threshold = threshold; this.tr = tr;
        this.parentId = parentId; this.depth = depth;
    }

    @Override
    protected void compute() {
        TraceNode node = tr.open(parentId, lo, hi, depth, a);
        int myId = (node == null) ? -1 : node.id;

        if (hi - lo <= threshold) {                 // ===== НАВЧ =====
            SequentialSort.insertionSort(a, lo, hi);
            tr.close(node, true, a);
            return;
        }

        int mid = lo + (hi - lo) / 2;               // үргэлж ТЭНЦҮҮ хуваагдана
        invokeAll(                                   // = fork + compute + join
            new MergesortAction<>(a, tmp, lo,  mid, threshold, tr, myId, depth + 1),
            new MergesortAction<>(a, tmp, mid, hi,  threshold, tr, myId, depth + 1));

        SequentialSort.merge(a, tmp, lo, mid, hi);  // нэгтгэх нь ПАРАЛЛЕЛЬЧЛАГДАХГҮЙ
        tr.close(node, false, a);
    }
}
