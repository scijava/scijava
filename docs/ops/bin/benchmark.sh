#!/bin/bash

set -e
set -o pipefail

if ! command -v mamba >/dev/null 2>&1
then
  echo 'Please install mamba before running this script.'
  exit 1
fi

SCRIPT_PATH=$(dirname "$(realpath -s "$0")")
DOCS_OPS_PATH="$SCRIPT_PATH/.."
INC_PATH="$DOCS_OPS_PATH/../../"
BENCHMARKS_PATH="$INC_PATH/scijava-ops-benchmarks"
BENCH_OUT_FILE=scijava-ops-benchmarks_results.json
BENCH_OUT_PATH=$(realpath -s "$DOCS_OPS_PATH/$BENCH_OUT_FILE")

if [ -f "$BENCH_OUT_PATH" ]
then
  echo 'Graphing existing benchmark results from:'
  echo "    $BENCH_OUT_PATH"
  echo 'To rerun benchmarks, delete this file first.'
else
  echo
  echo '=== BUILDING THE CODE ==='
  cd "$INC_PATH"
  mvn -Denforcer.skip -Dinvoker.skip -Dmaven.test.skip -P benchmarks clean install -pl scijava-ops-benchmarks -am | grep '\(Building.*[0-9]\]\|ERROR\)'

  echo
  echo '=== COPYING DEPENDENCIES ==='
  cd "$BENCHMARKS_PATH"
  {
    mvn dependency:copy-dependencies
    mvn dependency:copy -Dartifact=org.slf4j:slf4j-simple:1.7.36 -DoutputDirectory=target/dependency
  } | grep Copying | while read line
  do
    stdbuf -o0 echo -n '.'
  done
  echo

  # NB: Use the version of Java pointed to by the JAVA_HOME variable, if any.
  test -x "$JAVA_HOME/bin/java" && JAVA="$JAVA_HOME/bin/java" || JAVA=java

  echo
  echo '=== RUNNING BENCHMARKS ==='
  cd "$DOCS_OPS_PATH"
  "$JAVA" \
    -cp "$BENCHMARKS_PATH/target/*:$BENCHMARKS_PATH/target/dependency/*" \
    --add-opens=java.base/java.io=ALL-UNNAMED \
    org.openjdk.jmh.Main \
    -rf json -rff "$BENCH_OUT_FILE" \
    -r 5
fi

cd "$DOCS_OPS_PATH"
envPath=.benchmark-env
pythonPath="$envPath/bin/python"
if [ ! -x "$pythonPath" ]
then
  echo
  echo '=== CREATING CONDA ENVIRONMENT ==='
  mamba create -y -p "$envPath" python=3.10 plotly=5.19.0
fi

echo
echo '=== GRAPHING THE RESULTS ==='
"$pythonPath" graph_results.py
