package forkjoinsort;

import forkjoinsort.ui.MainWindow;

import javax.swing.SwingUtilities;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Нэвтрэх цэг.
 *
 *   java -cp out forkjoinsort.Main                      # график интерфейс
 *   java -cp out forkjoinsort.Main --headless -n 1000   # дэлгэцгүй, тоон үр дүн
 *   java -cp out forkjoinsort.Main --bench              # threshold ба хурдасгалтын хүснэгт
 *   java -cp out forkjoinsort.Main --render out.png -n 64
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ROOT);
        // Кирилл үсэг консол дээр зөв гарахыг баталгаажуулна (Windows cmd дээр: chcp 65001)
        System.setOut(new PrintStream(new java.io.FileOutputStream(java.io.FileDescriptor.out),
                                      true, StandardCharsets.UTF_8));
        if (args.length == 0 || has(args, "--gui")) { gui(); return; }
        if (has(args, "--help") || has(args, "-h"))  { usage(); return; }
        if (has(args, "--bench")) { Bench.run(); return; }

        RunConfig cfg = parse(args);
        if (has(args, "--render")) {
            String out = val(args, "--render", "recursion_tree.png");
            int w = Integer.parseInt(val(args, "-w", "1600"));
            int h = Integer.parseInt(val(args, "-H", "900"));
            RunResult r = SortRunner.run(cfg);
            forkjoinsort.ui.TreePanel tp = new forkjoinsort.ui.TreePanel();
            tp.setNodes(r.nodes);
            javax.imageio.ImageIO.write(tp.toImage(w, h), "png", new java.io.File(out));
            System.out.println("Зураг хадгалагдлаа: " + out);
            System.out.println(MainWindow.summary(r));
            return;
        }
        RunResult r = SortRunner.run(cfg);
        System.out.println(MainWindow.summary(r));
        if (!r.correct) System.exit(2);
    }

    private static void gui() {
        SwingUtilities.invokeLater(() -> {
            try { javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName()); }
            catch (Exception ignored) {}
            new MainWindow().setVisible(true);
        });
    }

    static RunConfig parse(String[] a) {
        RunConfig c = new RunConfig();
        c.size        = Integer.parseInt(val(a, "-n", "1000"));
        c.threshold   = Integer.parseInt(val(a, "-t", "100"));
        c.parallelism = Integer.parseInt(val(a, "-p",
                            String.valueOf(Runtime.getRuntime().availableProcessors())));
        c.seed        = Long.parseLong(val(a, "-s", "42"));
        String alg    = val(a, "-a", "merge").toLowerCase(Locale.ROOT);
        c.algorithm   = alg.startsWith("q") ? RunConfig.Algorithm.QUICKSORT
                                            : RunConfig.Algorithm.MERGESORT;
        String pat    = val(a, "-d", "random").toUpperCase(Locale.ROOT);
        try { c.pattern = DataGenerator.Pattern.valueOf(pat); }
        catch (Exception e) {
            throw new IllegalArgumentException("Үл мэдэгдэх хэв маяг: " + pat
                + " (RANDOM, SORTED, REVERSED, FEW_UNIQUE, ALL_EQUAL)");
        }
        String input  = val(a, "-i", null);
        if (input != null) c.customInput = DataGenerator.parse(input);
        c.trace = !has(a, "--no-trace");
        return c;
    }

    static boolean has(String[] a, String f) {
        for (String x : a) if (x.equals(f)) return true;
        return false;
    }
    static String val(String[] a, String f, String def) {
        for (int i = 0; i < a.length - 1; i++) if (a[i].equals(f)) return a[i + 1];
        return def;
    }

    static void usage() {
        System.out.println(String.join("\n",
          "Fork-Join Sorting Application  (IV-2-7)",
          "",
          "  --gui                 график интерфейс (өгөгдмөл)",
          "  --headless            дэлгэцгүй ажиллуулж тоон үр дүн хэвлэнэ",
          "  --bench               threshold ба хурдасгалтын хүснэгт (CSV)",
          "  --render <file.png>   рекурсийн модыг зураг болгож хадгална",
          "",
          "  -n <тоо>     массивын хэмжээ (өгөгдмөл 1000)",
          "  -t <тоо>     threshold (өгөгдмөл 100)",
          "  -p <тоо>     урсгалын тоо (өгөгдмөл: цөмийн тоо)",
          "  -s <тоо>     seed (өгөгдмөл 42)",
          "  -a merge|quick",
          "  -d RANDOM|SORTED|REVERSED|FEW_UNIQUE|ALL_EQUAL",
          "  -i \"7,12,19,3\"  өөрийн оролт",
          "  --no-trace   модыг бүртгэхгүй (хурдны хэмжилтэд)"));
    }
}
