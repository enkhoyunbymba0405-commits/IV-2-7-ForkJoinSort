package forkjoinsort;

public class TraceNode {
    public final int id;
    public final int parentId;     
    public final int lo, hi;       
    public final int depth;
    public volatile boolean leaf;   
    public volatile String thread;  
    public volatile long forkNanos; 
    public volatile long doneNanos; 
    public volatile String before;  
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
