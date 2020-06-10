# code-merge ![CI status](https://github.com/rock3r/code-merge/workflows/Check%20with%20Gradle/badge.svg)
A tool to simplify merging code changes from a separate codebase.

The tool naïvely traverses an input folder and tries to find the same files (based on matching FQNs) in a different
folder, regardless of where these files actually are located inside this second folder. It then generates a list of
files that have changed, that have been added, and removed, aiding the job for example of merging in changes from a
3rd party repository (or aggregated repositories, such as in the case of AOSP) into yours.

The tool is not doing any diffing itself, but rather checking file checksums to determine changes. The tool only
supports Java and Kotlin source files (`.java`, `.kt`).

Note: this tool is not aiming at performance, it's rather brute-force-y at the moment. If you want to improve its
processing speed, feel free to open a PR.
