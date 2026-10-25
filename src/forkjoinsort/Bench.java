package forkjoinsort;

/** Threshold муруй ба хурдасгалтын хэмжилт. Тайлангийн 8-р хэсэгт ашиглана. */
public final class Bench {

    private Bench() {}

    public static void run() {
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("# Цөмийн тоо: " + cores);

        System.out.println();
        System.out.println("## Туршилт A — threshold-ийн нөлөө (n = 1,000,000, RANDOM)");
        System.out.println("algorithm,threshold,parallel_ms,sequential_ms,speedup,nodes");
        for (int t : new int[]{1, 10, 50, 100, 500, 1_000, 5_000}) {
            for (RunConfig.Algorithm alg : RunConfig.Algorithm.values()) {
                RunConfig c = new RunConfig();
                c.size = 1_000_000; c.threshold = t; c.algorithm = alg;
                c.parallelism = cores; c.trace = false;
                RunResult r = warmAndRun(c);
                System.out.printf("%s,%d,%.1f,%.1f,%.2f,%d%n",
                        alg == RunConfig.Algorithm.MERGESORT ? "mergesort" : "quicksort",
                        t, r.parallelNanos / 1e6, r.sequentialNanos / 1e6, r.speedup(), r.nodeCount);
            }
        }

        System.out.println();
        System.out.println("## Туршилт B — урсгалын тооны нөлөө (n = 1,000,000, threshold = 100)");
        System.out.println("algorithm,threads,parallel_ms,speedup");
        for (int p : new int[]{1, 2, 4, 8, 16}) {
            if (p > cores * 2) break;
            for (RunConfig.Algorithm alg : RunConfig.Algorithm.values()) {
                RunConfig c = new RunConfig();
                c.size = 1_000_000; c.threshold = 100; c.algorithm = alg;
                c.parallelism = p; c.trace = false;
                RunResult r = warmAndRun(c);
                System.out.printf("%s,%d,%.1f,%.2f%n",
                        alg == RunConfig.Algorithm.MERGESORT ? "mergesort" : "quicksort",
                        p, r.parallelNanos / 1e6, r.speedup());
            }
        }

        System.out.println();
        System.out.println("## Туршилт C — өгөгдлийн хэв маягийн нөлөө (n = 500,000, threshold = 100)");
        System.out.println("algorithm,pattern,parallel_ms,max_depth,nodes");
        for (DataGenerator.Pattern pat : DataGenerator.Pattern.values()) {
            for (RunConfig.Algorithm alg : RunConfig.Algorithm.values()) {
                RunConfig c = new RunConfig();
                c.size = 500_000; c.threshold = 100; c.algorithm = alg;
                c.pattern = pat; c.parallelism = cores; c.trace = true;
                RunResult r = warmAndRun(c);
                System.out.printf("%s,%s,%.1f,%d,%d%n",
                        alg == RunConfig.Algorithm.MERGESORT ? "mergesort" : "quicksort",
                        pat.name(), r.parallelNanos / 1e6, r.maxDepth, r.nodeCount);
            }
        }
    }

    /** JIT халаах: эхний удаад Java маш удаан ажилладаг тул 2 удаа ажиллуулна. */
    private static RunResult warmAndRun(RunConfig c) {
        RunConfig warm = c.copy();
        warm.size = Math.min(c.size, 200_000);
        warm.trace = false;
        SortRunner.run(warm);
        return SortRunner.run(c);
    }
}
