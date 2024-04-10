import json
import statistics

import plotly.graph_objects as go
import plotly.io as io

# This script parses JMH benchmarking results into charts developed using plot.ly (https://plotly.com/)
# It expects JMH benchmark results be dumped to a file "scijava-ops-benchmark_results.json", within its directory.

# If you'd like to add a plotly chart, add an entry to the following list.
figures = [
    {
        "name": "BenchmarkMatching",
        "title": "Basic Op Matching Performance",
        "bars": {
            "noOps": "Static Method",
            "noOpsAdapted": "Static Method [A]",
            "sjOps": "Op Execution",
            "sjOpsAdapted": "Op Execution [A]"
        }
    },
    {
        "name": "BenchmarkCaching",
        "title": "Caching Effects on Op Matching Performance",
        "bars": {
            "noOps": "Static Method",
            "sjOps": "Op Execution",
            "sjOpsWithCache": "Op Execution (cached)"
        }
    },
    {
        "name": "BenchmarkConversion",
        "title": "Parameter Conversion Performance",
        "bars": {
            "noOpsConverted": "Static Method [C]",
            "noOpsAdaptedAndConverted": "Static Method [A+C]",
            "sjOpsConverted": "Op Execution [C]",
            "sjOpsConvertedAndAdapted": "Op Execution [A+C]"
        }
    },
    {
        "name": "BenchmarkFrameworks",
        "title": "Algorithm Execution Performance by Framework",
        "bars": {
            "noOps": "Static Method",
            "sjOps": "SciJava Ops",
            "ijOps": "ImageJ Ops"
        }
    },
    {
        "name": "BenchmarkCombined",
        "title": "Combined Performance Metrics",
        "bars": {
            "noOps": "Static Method",
            "noOpsAdapted": "Static Method [A]",
            "noOpsConverted": "Static Method [C]",
            "noOpsAdaptedAndConverted": "Static Method [A+C]",
            "sjOpsWithCache": "SciJava Ops (with caching)",
            "sjOps": "SciJava Ops (no caching)",
            "sjOpsAdapted": "SciJava Ops [A]",
            "sjOpsConverted": "SciJava Ops [C]",
            "sjOpsConvertedAndAdapted": "SciJava Ops [A+C]",
            "ijOps": "ImageJ Ops",
        }
    }
]

# Read in the benchmark results.
with open("scijava-ops-benchmarks_results.json") as f:
    data = json.load(f)

# Construct a mapping from test method to scores.
results = {}
for row in data:
    test = row["benchmark"].split(".")[-1]
    score = row["primaryMetric"]["score"]
    error = row["primaryMetric"]["scoreError"]
    results[test] = {"score": score, "error": error}

# Build charts and dump them to JSON.
for figure in figures:
    name = figure["name"]
    print(f"Generating figure for {name}", end="")

    x = []
    y = []
    error_y = []

    # Add each benchmark in the class
    for test, label in figure["bars"].items():
        print(f".", end="")
        result = results[test]
        x.append(label)
        y.append(result["score"])
        error_y.append(result["error"])

    # Create a bar chart
    fig = go.Figure()
    fig.add_bar(
        x=x,
        y=y,
        error_y=dict(type='data', array=error_y),
    )
    fig.update_layout(
        title_text=figure["title"],
        yaxis_title="Performance (us/op)"
    )

    # Convert to JSON and dump
    with open(f"images/{name}.json", "w") as f:
        f.write(io.to_json(fig))

    print()

print("Done!")
