# Study and submission

## Practical work

Java 17 is selected in submission.json. See ANALYSIS.md for actual commands, proofs,
tests and measurements. The official README and all other template materials are
preserved. Run `python3 tools/run.py --check` and
`python3 tests/student/test_algorithms.py --stress` from this repository.

Submit the full 40-character **final implementation commit URL** and `Java` to
[Assignment 4 Online text](https://lms.astanait.edu.kz/mod/assign/view.php?id=95272).
Use the commit that has a successful Public checks run, not the template's initial
commit. The repository is public. Click Submit assignment and verify Moodle's
final status. The README states 8 November 2026, 23:59; approved extensions are in
Moodle. A ZIP is not the submission format.

## Preparation route

Open the official [interactive lesson](https://rzhaniya.github.io/daa-assignment-4-archive/)
and switch to English for defence practice. Work through the eight models, then
use the [ungraded 15-question practice](https://rzhaniya.github.io/daa-assignment-4-archive/practice.html).
Try the practice before looking at explanations. It is separate from the assessed
Moodle defence. This file does not claim you have completed it.

For each algorithm, explain these aloud without reading the code:

1. LCS: what dp[i][j] means; why mismatch takes a maximum; how backtracking handles ties.
2. Merge: why the final merge defines a split; why arbitrary two-smallest merging is invalid; why children appear first.
3. Tree: why take uses child skip; why skip uses child max; why an iterative order handles long paths.
4. Naive: final start n-m; overlaps; n+1 empty-pattern boundaries.
5. KMP: proper prefix/suffix meaning of pi; why both mismatch and full-match fallback are needed.
6. RK: remove outgoing highest-power term; normalize remainder; verify collisions; candidate-dependent complexity.
7. Suffix: rank pairs, missing half rank -1, doubling; Kasai h-1 reuse; maximum adjacent LCP and overlaps.
8. Aho: failure links, suffix matches, reverse propagation, duplicate terminal mapping, reset per text.

Small self-checks: LCS of ABCA/BAC has length 2; merging [3,1,2] costs 9;
a root weight 5 with leaf weights -2/4 has take=5, skip=4; `aba` in `ababa`
starts at 0 and 2; KMP's post-match j for `aaaa` is 3; RK must distinguish
`ab` from colliding `dj`; `banana` has longest repeat `ana`; duplicate Aho
patterns receive equal counts in separate columns. Explain each result, then
change the input and predict the new answer.

Complete the assessed defence independently, following the course instructions.
