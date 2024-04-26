import json
import statistics

import plotly.graph_objects as go
import plotly.io as io

# This script parses JMH benchmarking results into charts developed using plot.ly (https://plotly.com/)
# It expects JMH benchmark results be dumped to a file "scijava-ops-benchmark_results.json", within its directory.

# If you'd like to add a plotly chart, add an entry to the following list.

A = "<b style=\"color:black\">[<b style=\"color:#009E73\">A</b>]</b>"
C = "<b style=\"color:black\">[<b style=\"color:#E69F00\">C</b>]</b>"
AC = "<b style=\"color:black\">[<b style=\"color:#CC79A7\">AC</b>]</b>"
figures = [
    {
        "name": "BenchmarkMatching",
        "title": "Basic Op Matching Performance",
        "bars": {
            "noOps": "Static Method",
            "noOpsAdapted": f"Static Method {A}",
            "sjOps": "SciJava Ops",
            "sjOpsAdapted": f"SciJava Ops {A}"
        }
    },
    {
        "name": "BenchmarkCaching",
        "title": "Caching Effects on Op Matching Performance",
        "bars": {
            "noOps": "Static Method",
            "sjOps": "SciJava Ops",
            "sjOpsWithCache": "SciJava Ops (cached)"
        }
    },
    {
        "name": "BenchmarkConversion",
        "title": "Parameter Conversion Performance",
        "bars": {
            "noOpsConverted": f"Static Method {C}",
            "noOpsAdaptedAndConverted": f"Static Method {AC}",
            "sjOpsConverted": f"SciJava Ops {C}",
            "sjOpsConvertedAndAdapted": f"SciJava Ops {AC}"
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
            "noOpsAdapted": f"Static Method {A}",
            "noOpsConverted": f"Static Method {C}",
            "noOpsAdaptedAndConverted": f"Static Method {AC}",
            "sjOpsWithCache": "SciJava Ops (cached)",
            "sjOps": "SciJava Ops",
            "sjOpsAdapted": f"SciJava Ops {A}",
            "sjOpsConverted": f"SciJava Ops {C}",
            "sjOpsConvertedAndAdapted": f"SciJava Ops {AC}",
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
    percentiles = row["primaryMetric"]["scorePercentiles"]
    minmax = [percentiles["0.0"], percentiles["100.0"]]
    results[test] = {"score": score, "minmax": minmax}

# Build charts and dump them to JSON.
for figure in figures:
    name = figure["name"]
    print(f"Generating figure for {name}", end="")

    x = []
    y = []
    error_y = []
    error_y_minus = []

    # Add each benchmark in the class
    for test, label in figure["bars"].items():
        print(f".", end="")
        result = results[test]
        x.append(label)
        y.append(result["score"])
        error_y.append(result["minmax"][1] - result["score"])
        error_y_minus.append(result["score"] - result["minmax"][0])

    # Create a bar chart
    fig = go.Figure()
    fig.add_bar(
        x=x,
        y=y,
        error_y=dict(type='data', array=error_y, arrayminus=error_y_minus),
    )
    fig.update_layout(
        title_text=figure["title"] + f"<br><sup style=\"color: gray\">{A}=Adaptation, {C}=Conversion, {AC}=Adaptation & Conversion</sup>",
        yaxis_title="<b>Performance (&mu;s/execution)</b>"
    )

    # Convert to JSON and dump
    with open(f"images/{name}.json", "w") as f:
        f.write(io.to_json(fig))

    print()

print("Done!")
