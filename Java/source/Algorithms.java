import java.util.*;

/** Stateless implementations of the eight assignment commands. */
public final class Algorithms {
    private Algorithms() {}

    static Map<String, Object> lcs(Map<String, Object> input) {
        String a = (String) input.get("a"), b = (String) input.get("b");
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                    ? dp[i - 1][j - 1] + 1 : Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }
        StringBuilder reversed = new StringBuilder(dp[n][m]);
        int i = n, j = m;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) {
                reversed.append(a.charAt(i - 1)); i--; j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--; // Deterministic tie: discard from a.
            } else {
                j--;
            }
        }
        return Map.of("length", dp[n][m], "sequence", reversed.reverse().toString());
    }

    static Map<String, Object> merge(Map<String, Object> input) {
        List<?> sizes = (List<?>) input.get("sizes");
        int n = sizes.size();
        List<List<Integer>> plan = new ArrayList<>();
        if (n < 2) return Map.of("cost", 0L, "plan", plan);
        long[] prefix = new long[n + 1];
        for (int i = 0; i < n; i++) prefix[i + 1] = prefix[i] + ((Number) sizes.get(i)).longValue();
        long[][] dp = new long[n][n];
        int[][] split = new int[n][n];
        for (int len = 2; len <= n; len++) {
            for (int l = 0; l + len <= n; l++) {
                int r = l + len - 1;
                dp[l][r] = Long.MAX_VALUE;
                for (int k = l; k < r; k++) {
                    long cost = dp[l][k] + dp[k + 1][r] + prefix[r + 1] - prefix[l];
                    if (cost < dp[l][r]) { dp[l][r] = cost; split[l][r] = k; }
                }
            }
        }
        // Explicit postorder: children are current blocks before their parent merge.
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{0, n - 1, 0});
        while (!stack.isEmpty()) {
            int[] frame = stack.pop();
            int l = frame[0], r = frame[1];
            if (l == r) continue;
            int k = split[l][r];
            if (frame[2] == 1) plan.add(List.of(l, k, r));
            else {
                stack.push(new int[]{l, r, 1});
                stack.push(new int[]{k + 1, r, 0});
                stack.push(new int[]{l, k, 0});
            }
        }
        return Map.of("cost", dp[0][n - 1], "plan", plan);
    }

    static Map<String, Object> tree(Map<String, Object> input) {
        int n = ((Number) input.get("n")).intValue();
        List<Integer> selected = new ArrayList<>();
        if (n == 0) return Map.of("weight", 0L, "selected", selected);
        List<?> weights = (List<?>) input.get("weights"), edges = (List<?>) input.get("edges");
        int[] degree = new int[n];
        for (Object edge : edges) {
            List<?> e = (List<?>) edge;
            degree[((Number) e.get(0)).intValue()]++;
            degree[((Number) e.get(1)).intValue()]++;
        }
        int[][] adj = new int[n][];
        for (int v = 0; v < n; v++) adj[v] = new int[degree[v]];
        Arrays.fill(degree, 0);
        for (Object edge : edges) {
            List<?> e = (List<?>) edge;
            int u = ((Number) e.get(0)).intValue(), v = ((Number) e.get(1)).intValue();
            adj[u][degree[u]++] = v; adj[v][degree[v]++] = u;
        }
        int[] parent = new int[n], order = new int[n];
        Arrays.fill(parent, -1);
        int size = 1; order[0] = 0;
        for (int i = 0; i < size; i++) {
            int v = order[i];
            for (int u : adj[v]) if (u != parent[v]) {
                parent[u] = v; order[size++] = u;
            }
        }
        long[] take = new long[n], skip = new long[n];
        for (int i = n - 1; i >= 0; i--) {
            int v = order[i]; take[v] = ((Number) weights.get(v)).longValue();
            for (int u : adj[v]) if (parent[u] == v) {
                take[v] += skip[u]; skip[v] += Math.max(take[u], skip[u]);
            }
        }
        boolean[] chosen = new boolean[n];
        for (int v : order) {
            chosen[v] = (parent[v] == -1 || !chosen[parent[v]]) && take[v] > skip[v];
            if (chosen[v]) selected.add(v); // Ties choose skip, including zero-weight vertices.
        }
        return Map.of("weight", Math.max(take[0], skip[0]), "selected", selected);
    }

    static Map<String, Object> naive(Map<String, Object> input) {
        String text = (String) input.get("text"), pattern = (String) input.get("pattern");
        int n = text.length(), m = pattern.length();
        List<Integer> positions = new ArrayList<>();
        for (int start = 0; start <= n - m; start++) {
            int j = 0;
            while (j < m && text.charAt(start + j) == pattern.charAt(j)) j++;
            if (j == m) positions.add(start);
        }
        return Map.of("positions", positions);
    }

    private static List<Integer> boundaries(int n) {
        List<Integer> result = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) result.add(i);
        return result;
    }

    static Map<String, Object> kmp(Map<String, Object> input) {
        String text = (String) input.get("text"), pattern = (String) input.get("pattern");
        int n = text.length(), m = pattern.length();
        if (m == 0) return Map.of("positions", boundaries(n));
        List<Integer> positions = new ArrayList<>();
        if (m > n) return Map.of("positions", positions);
        int[] pi = new int[m];
        for (int i = 1, j = 0; i < m; i++) {
            while (j > 0 && pattern.charAt(i) != pattern.charAt(j)) j = pi[j - 1];
            if (pattern.charAt(i) == pattern.charAt(j)) j++;
            pi[i] = j;
        }
        for (int i = 0, j = 0; i < n; i++) {
            while (j > 0 && text.charAt(i) != pattern.charAt(j)) j = pi[j - 1];
            if (text.charAt(i) == pattern.charAt(j)) j++;
            if (j == m) { positions.add(i - m + 1); j = pi[j - 1]; }
        }
        return Map.of("positions", positions);
    }

    static Map<String, Object> rk(Map<String, Object> input) {
        String text = (String) input.get("text"), pattern = (String) input.get("pattern");
        int n = text.length(), m = pattern.length();
        if (m == 0) return Map.of("positions", boundaries(n));
        List<Integer> positions = new ArrayList<>();
        if (m > n) return Map.of("positions", positions);
        final long base = 31, mod = 101;
        long high = 1, target = 0, window = 0;
        for (int j = 0; j < m; j++) {
            target = (target * base + pattern.charAt(j)) % mod;
            window = (window * base + text.charAt(j)) % mod;
            if (j < m - 1) high = high * base % mod;
        }
        for (int start = 0; start <= n - m; start++) {
            if (window == target) {
                int j = 0;
                while (j < m && text.charAt(start + j) == pattern.charAt(j)) j++;
                if (j == m) positions.add(start);
            }
            if (start < n - m) {
                window = ((window - text.charAt(start) * high) % mod + mod) % mod;
                window = (window * base + text.charAt(start + m)) % mod;
            }
        }
        return Map.of("positions", positions);
    }

    static Map<String, Object> suffix(Map<String, Object> input) {
        String text = (String) input.get("text");
        int n = text.length();
        if (n == 0) return Map.of("sa", new int[0], "lcp", new int[0], "repeated", "");
        Integer[] sa = new Integer[n];
        int[] ranks = new int[n];
        for (int i = 0; i < n; i++) { sa[i] = i; ranks[i] = text.charAt(i); }
        for (int width = 1; width < n; width *= 2) {
            final int k = width;
            final int[] old = ranks;
            Arrays.sort(sa, (a, b) -> {
                int first = Integer.compare(old[a], old[b]);
                return first != 0 ? first : Integer.compare(a + k < n ? old[a + k] : -1,
                                                            b + k < n ? old[b + k] : -1);
            });
            ranks = new int[n];
            for (int i = 1; i < n; i++) {
                int a = sa[i - 1], b = sa[i];
                boolean different = old[a] != old[b]
                    || (a + k < n ? old[a + k] : -1) != (b + k < n ? old[b + k] : -1);
                ranks[b] = ranks[a] + (different ? 1 : 0);
            }
            if (ranks[sa[n - 1]] == n - 1) break;
        }
        int[] inverse = new int[n], lcp = new int[n];
        for (int i = 0; i < n; i++) inverse[sa[i]] = i;
        int best = 0, bestStart = 0;
        for (int i = 0, h = 0; i < n; i++) {
            int rank = inverse[i];
            if (rank == 0) { h = 0; continue; }
            int j = sa[rank - 1];
            while (i + h < n && j + h < n && text.charAt(i + h) == text.charAt(j + h)) h++;
            lcp[rank] = h;
            if (h > best) { best = h; bestStart = i; }
            if (h > 0) h--;
        }
        return Map.of("sa", sa, "lcp", lcp, "repeated", text.substring(bestStart, bestStart + best));
    }

    static Map<String, Object> aho(Map<String, Object> input) {
        List<?> patterns = (List<?>) input.get("patterns"), texts = (List<?>) input.get("texts");
        int p = patterns.size();
        List<long[]> counts = new ArrayList<>();
        if (p == 0 || texts.isEmpty()) {
            for (Object ignored : texts) counts.add(new long[p]);
            return Map.of("counts", counts);
        }
        final int alphabet = 95;
        // Allocate transitions only for actual trie states; duplicates share terminals.
        List<int[]> next = new ArrayList<>(); next.add(new int[alphabet]);
        int[] terminal = new int[p];
        for (int i = 0; i < p; i++) {
            String pattern = (String) patterns.get(i); int state = 0;
            for (int j = 0; j < pattern.length(); j++) {
                int c = pattern.charAt(j) - 32;
                int[] row = next.get(state);
                if (row[c] == 0) { row[c] = next.size(); next.add(new int[alphabet]); }
                state = row[c];
            }
            terminal[i] = state;
        }
        int states = next.size();
        int[] fail = new int[states], bfs = new int[states];
        int head = 0, tail = 0;
        for (int child : next.get(0)) if (child != 0) bfs[tail++] = child;
        while (head < tail) {
            int v = bfs[head++];
            int[] row = next.get(v), fallback = next.get(fail[v]);
            for (int c = 0; c < alphabet; c++) {
                int child = row[c];
                if (child != 0) { fail[child] = fallback[c]; bfs[tail++] = child; }
                else row[c] = fallback[c];
            }
        }
        for (Object value : texts) {
            String text = (String) value;
            long[] visits = new long[states];
            int state = 0;
            for (int i = 0; i < text.length(); i++) {
                state = next.get(state)[text.charAt(i) - 32]; visits[state]++;
            }
            for (int i = tail - 1; i >= 0; i--) {
                int v = bfs[i]; visits[fail[v]] += visits[v];
            }
            long[] row = new long[p];
            for (int i = 0; i < p; i++) row[i] = visits[terminal[i]];
            counts.add(row);
        }
        return Map.of("counts", counts);
    }
}
