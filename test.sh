#!/bin/bash
# Бүх тестийг ажиллуулна. Бүгд давбал exit code 0.
set -e
cd "$(dirname "$0")"
[ -d out ] || ./build.sh
exec java -Xss16m -Djava.awt.headless=true -cp out TestSuite
