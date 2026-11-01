# Assignment 4

Dynamic Programming and String Processing — Design and Analysis of Algorithms.

Language: **Java 17**.

## Algorithms

- LCS with subsequence reconstruction
- Minimum-cost merging of adjacent blocks
- Maximum-weight independent set on a tree
- Naive search, KMP and exact Rabin–Karp
- Suffix array and Kasai LCP
- Aho–Corasick

## Run

Install JDK 17+ and Python 3.12+. Python is used by the launcher and tests;
the algorithm implementations are in `Java/source`.

Run these commands from the project folder:

```sh
python3 tools/run.py --setup
python3 tools/run.py --input examples/lcs.jsonl
```

The program reads one JSON request per line and returns one JSON result per line.
Other input examples are in `examples/`.

## Tests

```sh
python3 tools/run.py --check
python3 tests/student/test_algorithms.py --stress
```

GitHub Actions runs both test suites on Java 17.

Algorithm explanations, complexity and search timings are in [ANALYSIS.md](ANALYSIS.md).

Based on the [assignment template](https://github.com/RZhaniya/daa-assignment-4-archive).
