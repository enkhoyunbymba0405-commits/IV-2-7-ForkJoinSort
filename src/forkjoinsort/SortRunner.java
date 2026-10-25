package forkjoinsort;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

/** Тохиргоог авч туршилтыг ажиллуулаад үр дүнг буцаана. UI-аас бүрэн тусгаарлагдсан. */
public final class SortRunner {

    private SortRunner() {}

    public static RunResult run(RunConfig cfg) {
        cfg.validate();

        Integer[] base = (cfg.customInput != null)
                ? cfg.customInput.clone()
                : DataGenerator.generate(cfg.size, cfg.pattern, cfg.seed);

        RunResult r = new RunResult();
        r.config   = cfg;
        r.original = base.clone();

        // ---------- 1. Нэг урсгалт ЖИШИГ (baseline) ----------
        Integer[] seqArr = base.clone();
        Integer[] seqTmp = new Integer[seqArr.length];
        long t0 = System.nanoTime();
        if (seqArr.length > 1) {
            if (cfg.algorithm == RunConfig.Algorithm.MERGESORT)
                SequentialSort.mergesort(seqArr, seqTmp, 0, seqArr.length, cfg.threshold);
            else
                SequentialSort.quicksort3(seqArr, 0, seqArr.length, cfg.threshold);
        }
        r.sequentialNanos = System.nanoTime() - t0;

        // ---------- 2. FORK-JOIN хувилбар ----------
        Integer[] arr = base.clone();
        Integer[] tmp = new Integer[arr.length];
        TraceRecorder tr = new TraceRecorder(cfg.trace);

        ForkJoinPool pool = new ForkJoinPool(cfg.parallelism);
        try {
            t0 = System.nanoTime();
            if (arr.length > 1) {
                if (cfg.algorithm == RunConfig.Algorithm.MERGESORT)
                    pool.invoke(new MergesortAction<>(arr, tmp, 0, arr.length, cfg.threshold, tr, -1, 0));
                else
                    pool.invoke(new QuicksortAction<>(arr, 0, arr.length, cfg.threshold, tr, -1, 0));
            }
            r.parallelNanos = System.nanoTime() - t0;
        } finally {
            pool.shutdown();
        }

        // ---------- 3. ЗӨВ эсэхийг шалгах ----------
        Integer[] expect = base.clone();
        Arrays.sort(expect);
        r.correct = Arrays.equals(arr, expect);
        r.sorted  = arr;

        // ---------- 4. Модны статистик ----------
        List<TraceNode> nodes = tr.snapshotNodes();
        r.nodes = nodes;
        r.nodeCount = nodes.size();
        Map<String, Integer> per = new HashMap<>();
        int leaves = 0, maxD = 0;
        for (TraceNode n : nodes) {
            if (n.leaf) leaves++;
            if (n.depth > maxD) maxD = n.depth;
            per.merge(n.thread == null ? "?" : n.thread, 1, Integer::sum);
        }
        r.leafCount = leaves;
        r.maxDepth  = maxD;
        r.perThread = per;
        return r;
    }
}
