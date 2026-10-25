package forkjoinsort;

import java.util.Random;

/** Туршилтын өгөгдөл үүсгэнэ. Нэг өгөгдлийг эх кодод ХАТУУ ТОГТООХГҮЙ. */
public final class DataGenerator {

    public enum Pattern {
        RANDOM("Санамсаргүй"),
        SORTED("Эрэмбэлэгдсэн"),
        REVERSED("Урвуу эрэмбэтэй"),
        FEW_UNIQUE("Цөөн ялгаатай утга"),
        ALL_EQUAL("Бүгд ижил");

        public final String label;
        Pattern(String l) { this.label = l; }
        @Override public String toString() { return label; }
    }

    private DataGenerator() {}

    public static Integer[] generate(int n, Pattern p, long seed) {
        if (n < 0) throw new IllegalArgumentException("n сөрөг байж болохгүй: " + n);
        Integer[] a = new Integer[n];
        Random rnd = new Random(seed);
        switch (p) {
            case RANDOM:     for (int i = 0; i < n; i++) a[i] = rnd.nextInt(n * 10 + 1); break;
            case SORTED:     for (int i = 0; i < n; i++) a[i] = i; break;
            case REVERSED:   for (int i = 0; i < n; i++) a[i] = n - i; break;
            case FEW_UNIQUE: for (int i = 0; i < n; i++) a[i] = rnd.nextInt(5); break;
            case ALL_EQUAL:  for (int i = 0; i < n; i++) a[i] = 42; break;
        }
        return a;
    }

    /** Гараас оруулсан мөрийг задлана: "7, 12, 19, 3" гэх мэт. */
    public static Integer[] parse(String text) {
        String t = text.trim();
        if (t.isEmpty()) return new Integer[0];
        String[] parts = t.split("[\\s,;]+");
        Integer[] a = new Integer[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try {
                a[i] = Integer.valueOf(parts[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Бүхэл тоо биш утга: \"" + parts[i] + "\" (" + (i + 1) + "-р байрлал)");
            }
        }
        return a;
    }
}
