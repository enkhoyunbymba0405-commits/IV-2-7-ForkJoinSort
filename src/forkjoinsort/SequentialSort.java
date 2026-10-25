package forkjoinsort;

/**
 * Threshold-оос доош хэмжээтэй муж дээр ажиллах ЭНГИЙН алгоритм.
 * Сурах бичиг: "when the list to be sorted falls within some threshold
 * value ... directly apply a simple algorithm such as the Selection or
 * Insertion sort."
 */
public final class SequentialSort {

    private SequentialSort() {}

    /** Insertion sort, [lo, hi) муж дээр. Жижиг массивт Quicksort-оос хурдан. */
    public static <T extends Comparable<? super T>> void insertionSort(T[] a, int lo, int hi) {
        for (int i = lo + 1; i < hi; i++) {
            T key = a[i];
            int j = i - 1;
            while (j >= lo && a[j].compareTo(key) > 0) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    /**
     * Жишиг (baseline): НЭГ урсгалаар, параллель хувилбартай ЯГ ижил
     * threshold-оор mergesort хийнэ. Ингэснээр хурдасгалтын харьцуулалт нь
     * зөвхөн ПАРАЛЛЕЛЬЧЛАЛЫН нөлөөг хэмжинэ (алгоритмын ялгаа нөлөөлөхгүй).
     */
    public static <T extends Comparable<? super T>> void mergesort(T[] a, T[] tmp, int lo, int hi, int threshold) {
        if (hi - lo <= Math.max(1, threshold)) { insertionSort(a, lo, hi); return; }
        int mid = lo + (hi - lo) / 2;
        mergesort(a, tmp, lo, mid, threshold);
        mergesort(a, tmp, mid, hi, threshold);
        merge(a, tmp, lo, mid, hi);
    }

    /** Хоёр эрэмбэлэгдсэн хэсгийг нэгтгэнэ. tmp нь a-тай ижил урттай туслах массив. */
    public static <T extends Comparable<? super T>> void merge(T[] a, T[] tmp, int lo, int mid, int hi) {
        if (a[mid - 1].compareTo(a[mid]) <= 0) return;   // аль хэдийн эрэмбэлэгдсэн
        System.arraycopy(a, lo, tmp, lo, hi - lo);
        int i = lo, j = mid, k = lo;
        while (i < mid && j < hi)
            a[k++] = (tmp[i].compareTo(tmp[j]) <= 0) ? tmp[i++] : tmp[j++];
        while (i < mid) a[k++] = tmp[i++];
        while (j < hi)  a[k++] = tmp[j++];
    }

    /**
     * ГУРВАН ТАЛТ хуваалт (Dutch national flag, Dijkstra).
     * Буцаана: {lt, gt} бөгөөд
     *     a[lo..lt)  <  pivot
     *     a[lt..gt]  == pivot      <- эдгээр нь ЭЦСИЙН байрандаа суусан
     *     a(gt..hi)  >  pivot
     *
     * Яагаад хоёр талт биш гурван талт вэ: давхардсан утга их байх үед
     * (ALL_EQUAL, FEW_UNIQUE) энгийн Lomuto хуваалт нь нэг тал руугаа
     * бүрэн налж, рекурсийн гүн O(n) болж стек халина. Гурван талт хуваалт
     * ижил утгуудыг НЭГ алхамд хаядаг тул энэ эвдрэлийг арилгана.
     */
    public static <T extends Comparable<? super T>> int[] partition3(T[] a, int lo, int hi) {
        T pivot = medianOfThree(a, lo, hi);
        // Dijkstra-гийн гурван талт гүйлт. ЧУХАЛ: pivot-ыг урьдчилан ХӨДӨЛГӨХГҮЙ.
        // (Хэрэв урьдчилж swap хийвэл эрэмбэлэгдсэн өгөгдөл дээр хамгийн бага
        //  элемент хэсгийн төгсгөлд үлдэж, тэр хэв маяг түвшин бүрт давхарлан
        //  медиан-3 сонголтыг эвдэж, рекурсийн гүн O(n) болдог.)
        int lt = lo, i = lo, gt = hi - 1;
        while (i <= gt) {
            int cmp = a[i].compareTo(pivot);
            if (cmp < 0)      swap(a, lt++, i++);
            else if (cmp > 0) swap(a, i, gt--);
            else              i++;
        }
        return new int[]{lt, gt};
    }

    /**
     * Pivot сонголт: ГУРВАН БАЙРЛАЛЫН медиан утга, байрлалуудыг нь
     * (lo, hi)-ээс гаргасан хэш-ээр сонгоно.
     *
     * Яагаад хэш вэ? Тогтмол байрлал (lo, дунд, hi-1) ашиглавал эрэмбэлэгдсэн
     * өгөгдөл дээр гурван талт гүйлтийн swap-ууд "эрэмбэлэгдсэн + хамгийн бага
     * нь төгсгөлд" гэсэн хэв маягийг үүсгэж, тэр нь түвшин бүрт ДАВХАРЛАН
     * рекурсийн гүнийг O(n) болгодог. Хэш нь өгөгдлийн байрлалтай ямар ч
     * хамааралгүй тул энэ давхарлалтыг таслана.
     *
     * Хэш нь зөвхөн lo, hi-ээс хамаарах тул үр дүн ДАВТАГДАНА (тогтмол seed).
     */
    static <T extends Comparable<? super T>> T medianOfThree(T[] a, int lo, int hi) {
        int span = hi - lo;
        if (span < 8) return a[lo + span / 2];
        T x = a[pick(lo, hi, 1)], y = a[pick(lo, hi, 2)], z = a[pick(lo, hi, 3)];
        if (x.compareTo(y) <= 0) {
            if (y.compareTo(z) <= 0) return y;
            return (x.compareTo(z) <= 0) ? z : x;
        } else {
            if (x.compareTo(z) <= 0) return x;
            return (y.compareTo(z) <= 0) ? z : y;
        }
    }

    /** SplitMix64 маягийн хэш: (lo, hi, k) -> [lo, hi) доторх байрлал. */
    private static int pick(int lo, int hi, int k) {
        long h = lo * 0x9E3779B97F4A7C15L ^ hi * 0xBF58476D1CE4E5B9L ^ k * 0x94D049BB133111EBL;
        h ^= (h >>> 31); h *= 0x7FB5D329728EA185L; h ^= (h >>> 29);
        return lo + (int) Math.floorMod(h, (long) (hi - lo));
    }

    /**
     * Нэг урсгалт гурван талт quicksort, ДАВТАЛТААР (tail-call elimination).
     * Параллель рекурсийн гүн хэт гүнзгийрэхэд энэ рүү шилжинэ --- стек
     * хамгийн ихдээ O(log n) гүнтэй байна.
     */
    public static <T extends Comparable<? super T>> void quicksort3(T[] a, int lo, int hi, int threshold) {
        while (hi - lo > Math.max(1, threshold)) {
            int[] r = partition3(a, lo, hi);
            int leftSize = r[0] - lo, rightSize = hi - (r[1] + 1);
            if (leftSize < rightSize) {         // БАГА талыг рекурсээр, ИХ талыг давталтаар
                quicksort3(a, lo, r[0], threshold);
                lo = r[1] + 1;
            } else {
                quicksort3(a, r[1] + 1, hi, threshold);
                hi = r[0];
            }
        }
        insertionSort(a, lo, hi);
    }

    public static <T> void swap(T[] a, int i, int j) { T t = a[i]; a[i] = a[j]; a[j] = t; }
}
