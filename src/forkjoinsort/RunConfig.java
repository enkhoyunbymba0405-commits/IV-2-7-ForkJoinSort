package forkjoinsort;

/** Нэг туршилтын бүх тохиргоо. UI эндээс уншиж, энд бичнэ. */
public class RunConfig {

    public enum Algorithm {
        MERGESORT("Mergesort (fork-join)"),
        QUICKSORT("Quicksort (fork-join)");
        public final String label;
        Algorithm(String l) { this.label = l; }
        @Override public String toString() { return label; }
    }

    public int size = 64;
    public int threshold = 8;
    public int parallelism = Runtime.getRuntime().availableProcessors();
    public Algorithm algorithm = Algorithm.MERGESORT;
    public DataGenerator.Pattern pattern = DataGenerator.Pattern.RANDOM;
    public long seed = 42L;
    public Integer[] customInput = null;   // null биш бол size/pattern-ыг дарна
    public boolean trace = true;

    /** Хүчингүй утгыг ОЙЛГОМЖТОЙ мэдээлнэ (шаардлага 3). */
    public void validate() {
        if (customInput == null && size < 0)
            throw new IllegalArgumentException("Массивын хэмжээ сөрөг байж болохгүй: " + size);
        if (threshold < 1)
            throw new IllegalArgumentException(
                "Threshold хамгийн багадаа 1 байна (одоо " + threshold + "). "
              + "0 эсвэл сөрөг утга нь хязгааргүй рекурс үүсгэнэ.");
        if (parallelism < 1)
            throw new IllegalArgumentException(
                "Урсгалын тоо хамгийн багадаа 1 байна (одоо " + parallelism + ").");
        if (parallelism > 256)
            throw new IllegalArgumentException(
                "Урсгалын тоо 256-аас их байх нь утгагүй (одоо " + parallelism + ").");
    }

    public RunConfig copy() {
        RunConfig c = new RunConfig();
        c.size = size; c.threshold = threshold; c.parallelism = parallelism;
        c.algorithm = algorithm; c.pattern = pattern; c.seed = seed;
        c.customInput = customInput; c.trace = trace;
        return c;
    }
}
