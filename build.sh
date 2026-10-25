#!/bin/bash
# Fork-Join Sorting Application — компайл хийх
set -e
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java") tests/TestSuite.java
echo "Компайл амжилттай. Ажиллуулах:  ./run.sh"
