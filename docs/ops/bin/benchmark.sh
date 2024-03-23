#!/bin/bash

conda init

# Get the path to the script
SCRIPT_PATH=$(dirname "$(realpath -s "$0")")
DOCS_OPS_PATH="$SCRIPT_PATH/.."
INC_PATH="$DOCS_OPS_PATH/../../"
BENCHMARKS_PATH="$INC_PATH/scijava-ops-benchmarks"

BENCH_OUT_FILE=scijava-ops-benchmarks_results.txt

cd "$INC_PATH"
mvn clean install -pl scijava-ops-benchmarks -am

cd "$BENCHMARKS_PATH"
mvn dependency:copy-dependencies

cd "$DOCS_OPS_PATH"
conda env create -f "environment.yml"
java -cp "$BENCHMARKS_PATH/target/scijava-ops-benchmarks-0-SNAPSHOT.jar:$BENCHMARKS_PATH/target/dependency/*" org.openjdk.jmh.Main -o $BENCH_OUT_FILE

source activate ops-docs
python graph_results.py
source deactivate
