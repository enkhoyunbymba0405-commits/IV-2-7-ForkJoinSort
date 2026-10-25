import forkjoinsort.*;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

/**
 * Тестийн багц. Ажиллуулах:
 *   javac -encoding UTF-8 -d out $(find src -name "*.java") tests/TestSuite.java
 *   java -cp out TestSuite
 * Бүх тест давбал exit code 0, эс бөгөөс 1.
 */
public class TestSuite {

    static int pass = 0, fail = 0;

    public static void main(String[] args) {
        System.setOut(new PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out),
                                      true, StandardCharsets.UTF_8));
        System.out.println("=== Fork-Join Sorting — тестийн багц ===\n");

        bookExample();
        edgeCases();
        allPatterns();
        thresholdVariants();
        parallelismVariants();
        stability();
        invalidInput();
        largeRandom();

        System.out.printf("%n=== ДҮН: %d давсан, %d унасан ===%n", pass, fail);
        System.exit(fail == 0 ? 0 : 1);
    }

    // ---------- 1. Номын жишээ (Figure 4.27) ----------
    static void bookExample() {
        section("1. Номын жишээ — Figure 4.27");
        Integer[] in = {7, 12, 19, 3, 18, 4, 2, 6, 15, 8};
        Integer[] want = {2, 3, 4, 6, 7, 8, 12, 15, 18, 19};
        for (RunConfig.Algorithm alg : RunConfig.Algorithm.values()) {
            RunConfig c = new RunConfig();
            c.customInput = in.clone(); c.threshold = 2; c.algorithm = alg;
            RunResult r = SortRunner.run(c);
            check(alg + " үр дүн", Arrays.equals(r.sorted, want),
                  Arrays.toString(r.sorted));
            check(alg + " мод үүссэн", r.nodeCount > 1, "зангилаа=" + r.nodeCount);
        }
    }

    // ---------- 2. Захын оролт ----------
    static void edgeCases() {
        section("2. Захын (boundary) оролт");
        int[] sizes = {0, 1, 2, 3, 7, 8, 9};
        for (int n : sizes)
            for (RunConfig.Algorithm alg : RunConfig.Algorithm.values()) {
                RunConfig c = new RunConfig();
                c.size = n; c.threshold = 4; c.algorithm = alg;
                RunResult r = SortRunner.run(c);
                check("n=" + n + " " + shortName(alg), r.correct, len(r));
            }
        // threshold нь массиваас ТОМ — мод зөвхөн нэг навчтай байх ёстой
        RunConfig c = new RunConfig();
        c.size = 10; c.threshold = 1000;
        RunResult r = SortRunner.run(c);
        check("threshold > n → ганц навч", r.nodeCount == 1 && r.leafCount == 1,
              "зангилаа=" + r.nodeCount);
    }

    // ---------- 3. Бүх хэв маяг ----------
    static void allPatterns() {
        section("3. Өгөгдлийн бүх хэв маяг (n = 20000)");
        for (DataGenerator.Pattern p : DataGenerator.Pattern.values())
            for (RunConfig.Algorithm alg : RunConfig.Algorithm.values()) {
                RunConfig c = new RunConfig();
                c.size = 20000; c.threshold = 64; c.pattern = p; c.algorithm = alg;
                c.trace = false;
                RunResult r = SortRunner.run(c);
                check(p.name() + " / " + shortName(alg), r.correct, len(r));
            }
    }

    // ---------- 4. Threshold хувилбарууд ----------
    static void thresholdVariants() {
        section("4. Threshold-ийн хувилбарууд (n = 5000)");
        for (int t : new int[]{1, 2, 10, 100, 5000, 20000})
            for (RunConfig.Algorithm alg : RunConfig.Algorithm.values()) {
                RunConfig c = new RunConfig();
                c.size = 5000; c.threshold = t; c.algorithm = alg; c.trace = false;
                RunResult r = SortRunner.run(c);
                check("threshold=" + t + " " + shortName(alg), r.correct, len(r));
            }
    }

    // ---------- 5. Урсгалын тоо ----------
    static void parallelismVariants() {
        section("5. Урсгалын тооны хувилбарууд (n = 20000)");
        for (int p : new int[]{1, 2, 4, 8})
            for (RunConfig.Algorithm alg : RunConfig.Algorithm.values()) {
                RunConfig c = new RunConfig();
                c.size = 20000; c.threshold = 128; c.parallelism = p;
                c.algorithm = alg; c.trace = false;
                RunResult r = SortRunner.run(c);
                check("урсгал=" + p + " " + shortName(alg), r.correct, len(r));
            }
    }

    // ---------- 6. Тогтвортой байдал: 30 санамсаргүй ажиллуулалт ----------
    static void stability() {
        section("6. 30 санамсаргүй ажиллуулалт (давтагдах чанар)");
        Random rnd = new Random(2026);
        boolean ok = true;
        StringBuilder bad = new StringBuilder();
        for (int i = 0; i < 30; i++) {
            RunConfig c = new RunConfig();
            c.size = rnd.nextInt(3000);
            c.threshold = 1 + rnd.nextInt(200);
            c.parallelism = 1 + rnd.nextInt(4);
            c.seed = rnd.nextInt(100000);
            c.algorithm = RunConfig.Algorithm.values()[rnd.nextInt(2)];
            c.pattern = DataGenerator.Pattern.values()[rnd.nextInt(5)];
            c.trace = false;
            RunResult r = SortRunner.run(c);
            if (!r.correct) { ok = false; bad.append(" n=").append(c.size).append("/t=").append(c.threshold); }
        }
        check("30/30 зөв", ok, bad.length() == 0 ? "бүгд зөв" : bad.toString());
    }

    // ---------- 7. Хүчингүй оролт ОЙЛГОМЖТОЙ алдаа өгөх ----------
    static void invalidInput() {
        section("7. Хүчингүй оролтын боловсруулалт");
        check("threshold = 0 → алдаа", throwsIAE(() -> {
            RunConfig c = new RunConfig(); c.threshold = 0; SortRunner.run(c); }), "");
        check("threshold сөрөг → алдаа", throwsIAE(() -> {
            RunConfig c = new RunConfig(); c.threshold = -5; SortRunner.run(c); }), "");
        check("урсгал = 0 → алдаа", throwsIAE(() -> {
            RunConfig c = new RunConfig(); c.parallelism = 0; SortRunner.run(c); }), "");
        check("n сөрөг → алдаа", throwsIAE(() -> {
            RunConfig c = new RunConfig(); c.size = -1; SortRunner.run(c); }), "");
        check("текст оролт \"1,abc,3\" → алдаа", throwsIAE(() ->
            DataGenerator.parse("1, abc, 3")), "");
        // зөв оролт алдаа өгөхгүй байх ёстой
        boolean fine = true;
        try { DataGenerator.parse(" 5 ; 2, 9  7 "); } catch (Exception e) { fine = false; }
        check("зөв оролт \"5 ; 2, 9  7\" → OK", fine, "");
    }

    // ---------- 8. Том санамсаргүй массив ----------
    static void largeRandom() {
        section("8. Том массив (n = 300000)");
        for (RunConfig.Algorithm alg : RunConfig.Algorithm.values()) {
            RunConfig c = new RunConfig();
            c.size = 300000; c.threshold = 1000; c.algorithm = alg; c.trace = false;
            RunResult r = SortRunner.run(c);
            check(shortName(alg) + " n=300000", r.correct,
                  String.format("%.0f мс", r.parallelNanos / 1e6));
        }
    }

    // ---------- туслах ----------
    interface Blk { void run(); }
    static boolean throwsIAE(Blk b) {
        try { b.run(); return false; }
        catch (IllegalArgumentException e) { return true; }
        catch (Exception e) { return false; }
    }
    static String shortName(RunConfig.Algorithm a) {
        return a == RunConfig.Algorithm.MERGESORT ? "merge" : "quick";
    }
    static String len(RunResult r) { return "n=" + r.sorted.length; }
    static void section(String s) { System.out.println("\n--- " + s + " ---"); }
    static void check(String name, boolean ok, String extra) {
        if (ok) { pass++; System.out.printf("  [OK]    %-34s %s%n", name, extra); }
        else    { fail++; System.out.printf("  [УНАВ]  %-34s %s%n", name, extra); }
    }
}
