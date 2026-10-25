package forkjoinsort;

import java.util.List;
import java.util.Map;

/** Нэг туршилтын үр дүн: эрэмбэлсэн массив, мод, хэмжилтүүд. */
public class RunResult {
    public Integer[] original;
    public Integer[] sorted;
    public List<TraceNode> nodes;
    public RunConfig config;

    public long parallelNanos;      // fork-join хувилбарын хугацаа
    public long sequentialNanos;    // нэг урсгалт жишиг
    public boolean correct;         // Arrays.sort-той тулгасан үр дүн
    public int nodeCount, leafCount, maxDepth;
    public Map<String, Integer> perThread;   // ажилчин урсгал бүрийн зангилааны тоо

    public double speedup() {
        return parallelNanos == 0 ? 0 : (double) sequentialNanos / parallelNanos;
    }
    public static String ms(long nanos) {
        return String.format("%.3f мс", nanos / 1_000_000.0);
    }
}
