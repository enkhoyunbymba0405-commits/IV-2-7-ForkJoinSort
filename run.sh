#!/bin/bash
# Fork-Join Sorting Application — ажиллуулах
set -e
cd "$(dirname "$0")"
[ -d out ] || ./build.sh
exec java -Xss16m -cp out forkjoinsort.Main "$@"
