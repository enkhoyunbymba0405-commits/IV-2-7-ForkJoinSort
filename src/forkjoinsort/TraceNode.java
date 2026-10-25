package forkjoinsort;

/**
 * Рекурсийн модны НЭГ зангилаа = compute() функцийн нэг дуудлага.
 * UI нь эдгээр зангилаануудаас модыг барина.
 */
public class TraceNode {
    public final int id;
    public final int parentId;      // -1 бол үндэс
    public final int lo, hi;        // [lo, hi) муж
    public final int depth;
    public volatile boolean leaf;   // threshold хүрч шууд эрэмбэлсэн үү
    public volatile String thread;  // аль ажилчин урсгал гүйцэтгэсэн
    public volatile long forkNanos; // хэзээ эхэлсэн (эхлэлээс хойш)
    public volatile long doneNanos; // хэзээ дууссан
    public volatile String before;  // жижиг муж бол утгуудын хуулбар
    public volatile String after;

    public TraceNode(int id, int parentId, int lo, int hi, int depth) {
        this.id = id; this.parentId = parentId;
        this.lo = lo; this.hi = hi; this.depth = depth;
    }

    public int size() { return hi - lo; }

    @Override public String toString() {
        return "#" + id + " [" + lo + "," + hi + ") n=" + size()
             + (leaf ? " навч" : "") + " " + thread;
    }
}
