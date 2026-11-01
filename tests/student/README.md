# Student tests and benchmarks

From the repository root:

```sh
python3 tests/student/test_algorithms.py --stress
```

The runner compiles the Java solution, sends two identical mixed batches (2222
requests), and compares against independent tiny exhaustive/direct oracles.
It validates non-unique witnesses by their semantics. `--stress` adds 11 full-domain
known-answer fixtures, each with the launcher's 20-second timeout and 384 MiB heap.
Without that flag, only the small mixed batch runs. Python 3.12+ and JDK 17+ suffice.
The seed is 20261101. Test assertions must remain enabled (do not use Python `-O`).

`results/test-evidence.json` records representative input, expected and actual
results. Saved text logs are local runs; GitHub Actions independently reruns the
student suite on Java 17. The supplied public tests and validators are unchanged.

To measure search implementations after building:

```sh
javac --release 17 -encoding UTF-8 -cp Java/build -d Java/build tests/student/SearchBenchmark.java
java -Xmx384m -cp Java/build SearchBenchmark
```

The benchmark outputs CSV: random/repetitive cases at three sizes, five raw
microsecond-per-call samples, medians and correctness confirmation. It uses
200 warm-up calls then 100 calls per sample. See ANALYSIS.md for methodology,
machine information, actual data and limitations. Saved results are evidence,
not timing assertions or performance thresholds.
