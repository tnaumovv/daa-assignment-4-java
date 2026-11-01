# Assignment 4 — Dynamic Programming and String Processing

Java 17 implementation for Design and Analysis of Algorithms.
Based on the [official assignment template](https://github.com/RZhaniya/daa-assignment-4-archive).

## Implemented commands

| Command | Algorithm |
|---|---|
| `lcs` | Prefix dynamic programming and subsequence reconstruction |
| `merge` | Interval dynamic programming and chronological adjacent-merge plan |
| `tree` | Iterative maximum-weight independent set and reconstruction |
| `naive` | Direct character-by-character search |
| `kmp` | Prefix function and overlap-preserving search |
| `rk` | Exact Rabin–Karp, base 31/modulus 101, character verification |
| `suffix` | Prefix doubling, Kasai LCP and longest repeated substring |
| `aho` | Aho–Corasick with reverse failure-link count propagation |

The program reads UTF-8 JSON Lines: one request per line and one result per line.
Empty inputs, overlapping matches, duplicate dictionary entries and repeated
requests are supported. Costs and tree totals use 64-bit integers.

## Build and run

Requirements: JDK 17+ and Python 3.12+ for the supplied launcher.
Run from the repository root:

```sh
python3 tools/run.py --setup
python3 tools/run.py --check
python3 tools/run.py --input examples/lcs.jsonl
python3 tests/student/test_algorithms.py --stress
```

`submission.json` selects Java. The launcher compiles with `--release 17`.
Public checks cover 44 cases and a repeated mixed batch. Student tests cover
2222 independently validated requests and 11 scale fixtures.
GitHub Actions runs both suites on Java 17.

## Analysis and comparison

[ANALYSIS.md](ANALYSIS.md) contains correctness arguments, reconstruction,
complexity, test evidence and measured naive/KMP/RK comparisons.
Student test sources and recorded results are in [tests/student](tests/student).

After building, reproduce the comparison with:

```sh
javac --release 17 -encoding UTF-8 -cp Java/build -d Java/build tests/student/SearchBenchmark.java
java -Xmx384m -cp Java/build SearchBenchmark
```

## Study and submission

Use the [interactive lesson](https://rzhaniya.github.io/daa-assignment-4-archive/),
[practice](https://rzhaniya.github.io/daa-assignment-4-archive/practice.html), and
[study notes](STUDY_AND_SUBMISSION.md).

Submit the exact commit URL with its full 40-character SHA and language **Java**
in [Moodle Assignment 4](https://lms.astanait.edu.kz/mod/assign/view.php?id=95272).
Check the successful GitHub Actions run for that SHA. A ZIP is not the submission
format. Complete the individual defence separately and independently.
