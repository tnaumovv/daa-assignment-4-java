# Assignment 4 — Dynamic Programming and String Processing

## Environment and reproducible commands

Selected language: **Java**, Java 17 source/API target (`javac --release 17`).
Local measurements: OpenJDK 27+35-2325, OpenJDK 64-Bit Server VM, macOS 15.7.1,
Apple M2 (aarch64), 8 GiB RAM; launcher Python 3.13.1. Java heap: 384 MiB.
GitHub Actions uses Temurin Java 17 and Python 3.12 to build and test the Java solution.
Local measurements are not measurements of the official isolated grading host.

Run these commands from the repository root:

```sh
python3 tools/run.py --setup
python3 tools/run.py --check
python3 tools/run.py --input examples/lcs.jsonl
python3 tests/student/test_algorithms.py --stress
javac --release 17 -encoding UTF-8 -cp Java/build -d Java/build tests/student/SearchBenchmark.java
java -Xmx384m -cp Java/build SearchBenchmark
```

The launcher builds all `Java/source/*.java` into `Java/build` before executing
`java -Xmx384m -cp Java/build Main`. The benchmark command assumes a launcher/test
build has run. No external libraries are required. The existing Main/Json transport
is preserved, including its compiler warning about an unchecked JSON map cast.
Each input line produces exactly one JSON object; algorithm methods use fresh local
state. All input positions are zero-based. Only printable ASCII is in the domain.

## 1. LCS

`dp[i][j]` is the maximum common subsequence length of `a[0..i)` and `b[0..j)`.
The zero row and column represent an empty prefix, so they contain zero.
If the last characters agree, `dp[i][j] = 1 + dp[i-1][j-1]`. Otherwise it is
`max(dp[i-1][j], dp[i][j-1])`. Increasing i and j makes both dependencies ready.

For equal last characters an optimal subsequence can use that final matching pair:
removing it leaves a common subsequence of the shorter prefixes. Appending it to
an optimal shorter solution attains the bound. When characters differ, any common
subsequence must omit at least one of these last positions; the two predecessor
states cover all possibilities. Induction over prefix sizes proves optimality.

Reconstruction starts at `(a.length,b.length)`. Equal characters are appended in
reverse order, and both indices decrease. On mismatch, follow a predecessor with
the same optimum value; ties discard the last character of a. Reverse the collected
characters once. Each chosen character respects the order in both strings and the
number chosen is `dp[n][m]`. Alternative tie choices are also valid.

Time and auxiliary memory: O((n+1)(m+1)); reconstruction O(n+m), with O(min(n,m)+1)
output space. Input is O(n+m+1). Empty strings return `0,""`.
A test using `tea_time` and `team` detects confusing subsequences with substrings;
`ab`/`ba` checks acceptance of either optimal witness.

## 2. Ordered adjacent-block merging

`dp[l][r]` is the minimum cost to combine original blocks l through r. A singleton
costs zero. Prefix sums give `sum(l..r)` in constant time. For each length ≥2:

`dp[l][r] = min over l <= k < r of (dp[l][k] + dp[k+1][r] + prefix[r+1]-prefix[l])`.

Any valid final merge joins two adjacent intervals with exactly one such split k.
Its child costs are at least their respective optima. Conversely, optimal child
plans followed by that final merge are feasible and attain the candidate cost.
Taking the minimum proves the recurrence, by induction on interval length. Shorter
intervals are computed first. All sums and costs are `long`, including prefix sums.

Store the first minimizing split. An explicit stack visits left and right child
plans before emitting `[l,k,r]`. Thus the two child intervals already exist when
the parent is emitted; indices always refer to the original array. A full binary
merge tree with n leaves has n−1 merges. Empty/singleton arrays return zero and no
operations; zeros and tied minima are permitted.

For n≥1: O(n³) time, O(n²) auxiliary memory, O(n) input and output; empty input O(1).
`[1,100,1]` costs 203 and prevents merging arbitrary nonadjacent small blocks.
Four blocks of 10^9 cost 8×10^9, exposing 32-bit overflow. Tests replay every plan
against a dictionary of currently existing intervals and recompute its cost.

## 3. Maximum-weight independent set on a tree

Root at vertex 0. Iteratively construct parent pointers and a parent-before-child
order. Reverse this order to compute:

- `take[v] = weights[v] + sum(skip[u])` over children u.
- `skip[v] = sum(max(take[u],skip[u]))` over children u.

Leaf states are weight and zero. If v is selected, every child must be skipped;
if v is skipped, each child subtree can independently choose its better state.
There are no edges between child subtrees in a tree. This exhausts all feasible
choices, so induction from leaves proves both state meanings. The result is the
maximum of the two root states. Rooting changes the decomposition, not the feasible
sets or optimum.

Reconstruct in parent-before-child order. Select v exactly when its parent is not
selected and `take[v] > skip[v]`. Ties choose skip. A selected parent forces skip;
otherwise choose an optimal child state. This is feasible and attains the root
value without duplicate vertices. All-negative weights give the empty set, weight
zero. The empty tree is handled before allocating adjacency lists.

Time O(n+1); auxiliary storage O(n+1), including integer adjacency arrays, parent,
order, long DP arrays and selected flags. Input/output O(n+1). No recursive call is
used: a 100000-vertex path cannot overflow the Java call stack. A path of that size
with weights 10^9 returns 50000000000000; a star returns 99999000000000.

## 4. Exact single-pattern search

The shared result is every occurrence start in increasing order, including overlaps.
An empty pattern matches n+1 boundaries; a pattern longer than the text has none.
No production implementation uses built-in substring search. All three are checked
against a separate Python slicing oracle, not merely against each other.

### Naive

Try starts 0 through n−m inclusive. Compare characters left-to-right until mismatch
or m matches. A start is emitted exactly if every character agrees. Each possible
start is visited once, proving completeness, uniqueness and order. When m=0 the
comparison loop is empty and all n+1 starts are emitted; when m>n no start is tried.
Time O(nm+n+1); O(1) auxiliary and O(z+1) output space; input O(n+m+1).
`aaaaa`/`aaa` returns `[0,1,2]`, detecting both skipped overlaps and last-start errors.

### KMP

`pi[i]` is the length of the longest proper prefix of `pattern[0..i]` also appearing
as its suffix. Construct it left-to-right. On mismatch, repeatedly set j to
`pi[j-1]`; when characters match, increase j. Prefixes omitted by fallback cannot
match because they are not borders of the already matched prefix.

During scanning, j characters of the pattern match a suffix of the consumed text.
A mismatch follows the same border chain; the text index never decreases. At j=m,
emit `i-m+1` then set j to `pi[m-1]` to retain overlaps. This invariant proves that
all and only matches are emitted. Handle m=0 and m>n before any pattern indexing.
The total fallback movement is bounded by total forward movement, yielding
O(n+m+z+1) time, O(m+1) auxiliary and O(z+1) output memory.

### Exact Rabin–Karp

Use base 31, modulus 101 and raw ASCII codes. For a width-m window its leftmost
character has power m−1. Form target and first-window hashes by repeated multiply
and add. Maintain `high = 31^(m-1) mod 101`. To slide one position, subtract
`outgoing*high`, normalize with `(remainder+101)%101`, multiply by 31 and add the
incoming character, reducing modulo 101 again. The rolling polynomial identity
preserves the hash invariant. Arithmetic is `long`.

Unequal hashes exclude equality. Equal hashes only identify candidates: compare
all actual characters before emitting a position. Thus collisions cannot produce
false matches. `ab` and `dj` both hash to 75, but `abdj`/`ab` returns only `[0]`.
Early handling of empty and oversized patterns avoids invalid windows.

For 1≤m≤n and c candidate windows: O(n+m+c*m) time. The general bound including
empty inputs is O(nm+n+m+1); deterministic linear worst-case time is NOT claimed.
The rolling version uses O(1) auxiliary space and O(z+1) output space. Dense genuine
matches require verification too, even without false hash collisions.

## 5. Suffix array and Kasai LCP

The array contains exactly the n nonempty suffix starts. Initialize ranks to ASCII
codes. In a width-k round, comparison-sort starts by `(rank[i],rank[i+k])`, using −1
for a missing second half. −1 places a shorter equal-prefix suffix first. Assign
new equal ranks exactly to equal pairs, then double k. Inductively, the old ranks
order length-k prefixes and pairs order length-2k prefixes. Once all ranks are
unique, or the compared width covers every suffix, the order is the full suffix
order. No suffix strings or sentinel suffixes are materialized. A singleton needs
no sorting round; the empty case returns empty arrays and string.

`Arrays.sort(Integer[], comparator)` is comparison sorting with O(n log(n+1)) time
per round and O(n) worst-case auxiliary storage. There are O(log(n+1)) rounds:
O(n log²(n+1)) construction time and O(n+1) auxiliary space, including boxed starts,
old/new ranks and sorting buffers. Input and output each use O(n+1) space.

Kasai builds inverse positions. For each original start i, compare with the suffix
immediately preceding it in suffix order, beginning with a retained common length
h. After removing the first character, a previous common prefix can shrink by at
most one; any suffix between the shifted pair in sorted order shares at least that
prefix. Consequently h−1 is a safe lower bound for the next comparison. For rank 0,
set h=0. Store the resulting h at `lcp[rank]`, then decrease h by one if positive.
Total increases and decreases are O(n), giving O(n) time and O(n) auxiliary memory.
`lcp[0]=0`; each later entry compares `sa[i-1]` and `sa[i]`.

Any substring shared by two suffixes is shared by adjacent suffixes somewhere
between them in sorted order. Therefore maximum adjacent LCP length is the longest
repeat length. Save a start whenever a strictly larger LCP is found and extract
that one substring at the end. Ties can choose any optimum; overlaps are allowed.
`banana` gives `[5,3,1,0,4,2]`, `[0,1,3,0,0,2]`, and `ana`.

## 6. Aho–Corasick

Insert dictionary patterns into a trie. A state represents a dictionary prefix;
duplicate entries share the terminal state, while a separate terminal array keeps
one entry per original dictionary position. Each actual trie state has a dense
95-element transition row. Missing transitions initially use zero (root).

Build failure links in BFS order. A failure link identifies the longest proper
suffix of the state's word that is a trie prefix. Root children fail to root.
For a child on c, its failure is the completed c-transition of the parent's failure
state. Fill absent transitions with those of that failure state. BFS guarantees
that shorter states' transitions are ready. Thus every text transition maintains
the longest suffix of consumed text that is a trie prefix.

For each independent text, reset state and allocate fresh long visit counters.
Increment the reached state for each character. Traverse nonroot states in reverse
BFS order, adding `visits[v]` into `visits[fail[v]]`. Every visit corresponds to a
match ending there for every terminal suffix ancestor. Propagating once along each
failure edge accumulates precisely these suffix counts, including overlaps, without
enumerating individual matches or copying inherited output lists. Report visits
at the original terminal indices; duplicate columns therefore have equal counts.

Empty dictionaries yield one empty row per text; no texts yields no rows. An empty
text yields p zeros. Let L be total pattern length, S actual trie states including
root, p pattern count and q text count. Build O(L+95S+p); query each text in
O(|text|+S+p); auxiliary space O(95S+p). Input is O(L+sum|text|+p+q+1), output
O(q*(p+1)+1). Long counters are used. Early empty cases can take less work.
The `he,she,he` example checks failure suffixes, duplicates and repeated text resets.

## Own tests and observed results

`tests/student/test_algorithms.py` uses seed 20261101. Tiny LCS answers enumerate
subsequences; merge answers enumerate all adjacent merge choices; tree answers
enumerate independent subsets. Search uses direct slicing, suffix uses explicitly
sorted small suffix strings and direct LCP, and Aho uses direct per-pattern matching.
These intentionally simple oracles are test code only. Witness validators accept
alternative optima, replay merge operations and check tree independence and totals.

The initial TODO implementation failed all 2222 mixed requests, while setup ping
passed. After implementation, **2222/2222 mixed requests**, **11 scale fixtures**,
**44/44 public cases**, and the public repeated 88-request batch passed locally.
Stored logs: [student](tests/student/results/student-tests.txt),
[public](tests/student/results/public-checks.txt), and machine-readable
[input/expected/actual examples](tests/student/results/test-evidence.json).

| Command / fixture in student script | Expected | Actual | Defect detected |
|---|---|---|---|
| LCS empty pair | length 0, empty witness | Passed | Empty table/reconstruction |
| LCS `tea_time`,`team` | 4 and `team` | Passed | Subsequence vs substring |
| LCS `ab`,`ba` | length 1, either valid witness | Passed | Incorrect tie assumptions |
| Merge `[1,100,1]` | 203, two valid merges | Passed | Ignored adjacency |
| Merge `[10^9]*4` | 8000000000, valid plan | Passed | 32-bit overflow |
| Tree tiny generated trees, n=0..10 | Exhaustive optimum and valid set | Passed | Negative/tied weights and reconstruction |
| All searches `aaaaa`,`aaa` | `[0,1,2]` | Passed | Overlap and final start |
| All searches `abc`,empty | `[0,1,2,3]` | Passed | Missing end boundary |
| RK `abdj`,`ab` | `[0]` | Passed | Unverified hash collision |
| Suffix `banana` | Known sa/lcp, `ana` | Passed | LCP direction and repeat |
| Aho `he,she,he`; two `sheshe`, empty | `[[2,2,2],[2,2,2],[0,0,0]]` | Passed | Suffix outputs, duplicate columns, reset |
| LCS 1000 identical characters | 1000-character witness | Passed | Maximum table domain |
| Merge 200 blocks of 10^9 | 1544000000000, 199 valid operations | Passed | Full interval domain and long totals |
| Tree path/star, n=100000 | 50000000000000 / 99999000000000 | Passed | Deep traversal and wide fanout |
| Naive/KMP/RK max n, repeated pattern | Every valid start | Passed | Published limits and dense matches |
| KMP n=200000, empty pattern | 200001 boundaries | Passed | Maximum empty-pattern output |
| Suffix 50000 `a` characters | descending sa, lcp=0..49999 | Passed | Repeated-rank rounds and scale |
| Aho 10000 duplicate length-10 patterns, 100 texts of length 2000 | Every count 1991 | Passed | Million-result output, no per-entry occurrence expansion |
| Aho one 100000-character pattern, 200000-character text | 100001 | Passed | Maximum trie depth |

For 200 equal merge weights, a minimum-depth full binary tree has 56 leaves at depth
7 and 144 at depth 8: total weighted depth 1544. Such a tree can preserve leaf order,
providing an independent known answer for the scale fixture. Scale fixtures check
answers and witnesses, not just successful termination. These checks provide evidence,
not a guarantee about hidden tests or an official grade.

## Search comparison

Reproduce using the benchmark commands above. Raw measurements are saved in
[search-benchmark.csv](tests/student/results/search-benchmark.csv). All methods
receive identical inputs. Random text uses `java.util.Random(20261101+n)` with
uniform draws over `abcd`; its pattern is the length-m text segment starting at
n/2, guaranteeing a match. Repetitive cases use text `a^n` and pattern `a^m`.
Sizes are n=300/1000/3000, m=n/10, inside all three input domains.

Before timing, each method's entire output is checked against Java `startsWith`
used only in the benchmark oracle. Each method/case has 200 untimed warm-up calls,
then five measured runs of 100 calls. Reported times are microseconds per call.
The boundary is identical: the complete Algorithms method, including prefix/hash
preprocessing and output allocation, but excluding compilation, JVM launch, input
creation, JSON parsing/serialization and oracle checks. A volatile sink retains
results; algorithm order rotates each measured run. GC/JIT remain enabled; this is
a simple course benchmark, not a claim of fully stabilized JMH-grade measurements.

| Case | n | m | Method | Run 1 | Run 2 | Run 3 | Run 4 | Run 5 | Median | Correct |
|---|---:|---:|---|---:|---:|---:|---:|---:|---:|---|
| random | 300 | 30 | naive | 10.057 | 9.786 | 10.177 | 10.313 | 10.228 | 10.177 | true |
| random | 300 | 30 | kmp | 11.245 | 11.729 | 12.239 | 11.377 | 11.558 | 11.558 | true |
| random | 300 | 30 | rk | 8.098 | 8.073 | 8.088 | 8.183 | 8.157 | 8.098 | true |
| random | 1000 | 100 | naive | 3.018 | 2.945 | 2.900 | 2.913 | 3.137 | 2.945 | true |
| random | 1000 | 100 | kmp | 4.419 | 4.273 | 4.300 | 4.311 | 4.293 | 4.300 | true |
| random | 1000 | 100 | rk | 29.791 | 31.369 | 31.152 | 31.124 | 33.192 | 31.152 | true |
| random | 3000 | 300 | naive | 9.788 | 9.368 | 9.435 | 8.662 | 8.681 | 9.368 | true |
| random | 3000 | 300 | kmp | 13.368 | 12.795 | 13.621 | 13.102 | 13.696 | 13.368 | true |
| random | 3000 | 300 | rk | 62.446 | 62.686 | 63.085 | 61.488 | 62.211 | 62.446 | true |
| repetitive | 300 | 30 | naive | 21.558 | 18.061 | 19.006 | 18.250 | 18.714 | 18.714 | true |
| repetitive | 300 | 30 | kmp | 18.883 | 19.094 | 25.594 | 2.900 | 2.778 | 18.883 | true |
| repetitive | 300 | 30 | rk | 19.452 | 26.821 | 19.569 | 19.313 | 30.092 | 19.569 | true |
| repetitive | 1000 | 100 | naive | 188.712 | 184.749 | 192.528 | 187.566 | 187.914 | 187.914 | true |
| repetitive | 1000 | 100 | kmp | 11.796 | 11.877 | 9.250 | 9.323 | 20.082 | 11.796 | true |
| repetitive | 1000 | 100 | rk | 197.392 | 193.667 | 200.483 | 194.169 | 189.175 | 194.169 | true |
| repetitive | 3000 | 300 | naive | 1665.207 | 1667.806 | 1712.485 | 1693.791 | 2718.574 | 1693.791 | true |
| repetitive | 3000 | 300 | kmp | 27.445 | 27.470 | 26.685 | 27.654 | 39.415 | 27.470 | true |
| repetitive | 3000 | 300 | rk | 1683.804 | 1681.236 | 1704.526 | 1694.420 | 1698.453 | 1694.420 | true |

Random inputs usually reject candidates early, so naive need not be slower than
KMP at these sizes. The random n=300 times exceed some n=1000 times; later JIT
compilation and small-sample noise make this unsurprising. In the largest repetitive
case, KMP's median is 27.470 μs versus 1693.791 μs for naive and 1694.420 μs for RK.
Every repetitive window is a true match, so naive and exact RK verify m characters
per start, whereas KMP reuses borders. KMP's smallest repetitive case also shows
JIT variation within the five runs. These measurements do not prove asymptotic
bounds; those follow from the algorithms above.

Modulus 101 deliberately produces frequent collisions, adding failed candidate
verification on random inputs. This laboratory configuration is not representative
of all production hashes, which can use larger/double fingerprints. Even those
must verify candidates for deterministic exactness. The known ab/dj collision is
a correctness test, not a timing-based proof of collision resistance.

## Sources

- Official [template and README](https://github.com/RZhaniya/daa-assignment-4-archive),
  [English specification](https://github.com/RZhaniya/daa-assignment-4-archive/blob/main/docs/Assignment_4_EN.md),
  contracts, testing guide and rubric: interfaces, domains, required methods,
  grading groups, examples and supplied transport/launcher/public tests.
- Official [interactive lesson](https://rzhaniya.github.io/daa-assignment-4-archive/)
  and [ungraded practice](https://rzhaniya.github.io/daa-assignment-4-archive/practice.html):
  recurrences, invariants, reconstruction and common error explanations. Their
  template copies in docs were consulted. No assessed Moodle quiz was accessed.
