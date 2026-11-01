import java.util.*;

/** Reproducible in-process comparison; compilation/JSON transport excluded equally. */
public final class SearchBenchmark {
    private static volatile Object sink;
    private static Object run(String method, Map<String, Object> input) {
        return switch (method) {
            case "naive" -> Algorithms.naive(input);
            case "kmp" -> Algorithms.kmp(input);
            default -> Algorithms.rk(input);
        };
    }
    private static String randomString(Random rng, int n) {
        StringBuilder b = new StringBuilder(n);
        for (int i = 0; i < n; i++) b.append((char) ('a' + rng.nextInt(4)));
        return b.toString();
    }
    public static void main(String[] args) {
        String[] methods = {"naive", "kmp", "rk"};
        System.out.println("# java=" + System.getProperty("java.runtime.version")
            + "; vm=" + System.getProperty("java.vm.name")
            + "; os=" + System.getProperty("os.name") + " " + System.getProperty("os.version")
            + "; arch=" + System.getProperty("os.arch"));
        System.out.println("# seed=20261101+n; 200 warm-up calls/method/case; 5 runs x 100 calls; microseconds/call; rotating method order");
        System.out.println("case,n,m,method,run1_us,run2_us,run3_us,run4_us,run5_us,median_us,correct");
        for (String kind : List.of("random", "repetitive")) for (int n : new int[]{300, 1000, 3000}) {
            int m = n / 10;
            Random rng = new Random(20261101L + n);
            String text = kind.equals("random") ? randomString(rng, n) : "a".repeat(n);
            // Random pattern is sampled from the text, guaranteeing at least one occurrence.
            String pattern = kind.equals("random") ? text.substring(n / 2, n / 2 + m) : "a".repeat(m);
            Map<String, Object> input = Map.of("text", text, "pattern", pattern);
            List<Integer> expected = new ArrayList<>();
            for (int i = 0; i <= n - m; i++) if (text.startsWith(pattern, i)) expected.add(i);
            for (String method : methods) {
                if (!run(method, input).equals(Map.of("positions", expected))) throw new AssertionError(method);
                for (int i = 0; i < 200; i++) sink = run(method, input);
            }
            double[][] times = new double[3][5];
            for (int r = 0; r < 5; r++) for (int offset = 0; offset < 3; offset++) {
                int method = (r + offset) % 3;
                long start = System.nanoTime();
                for (int i = 0; i < 100; i++) sink = run(methods[method], input);
                times[method][r] = (System.nanoTime() - start) / 100000.0;
            }
            for (int method = 0; method < 3; method++) {
                double[] sorted = times[method].clone(); Arrays.sort(sorted);
                System.out.printf(Locale.ROOT, "%s,%d,%d,%s", kind, n, m, methods[method]);
                for (double time : times[method]) System.out.printf(Locale.ROOT, ",%.3f", time);
                System.out.printf(Locale.ROOT, ",%.3f,true%n", sorted[2]);
            }
        }
    }
}
