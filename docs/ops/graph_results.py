import json
import math
import statistics

import plotly.graph_objects as go
import plotly.io as io
from plotly.subplots import make_subplots

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
            "sjOpsAdapted": f"SciJava Ops {A}",
            "sjOps": "SciJava Ops",
            "noOpsAdapted": f"Static Method {A}",
            "noOps": "Static Method",
        }
    },
    {
        "name": "BenchmarkCaching",
        "title": "Caching Effects on Op Matching Performance",
        "bars": {
            "sjOpsWithCache": "SciJava Ops (cached)",
            "sjOps": "SciJava Ops",
            "noOps": "Static Method",
        }
    },
    {
        "name": "BenchmarkConversion",
        "title": "Parameter Conversion Performance",
        "bars": {
            "sjOpsConvertedAndAdapted": f"SciJava Ops {AC}",
            "sjOpsConverted": f"SciJava Ops {C}",
            "noOpsAdaptedAndConverted": f"Static Method {AC}",
            "noOpsConverted": f"Static Method {C}",
        }
    },
    {
        "name": "BenchmarkFrameworks",
        "title": "Algorithm Execution Performance by Framework",
        "bars": {
            "ijOps": "ImageJ Ops",
            "sjOps": "SciJava Ops",
            "noOps": "Static Method",
        }
    },
    {
        "name": "BenchmarkCombined",
        "title": "Combined Performance Metrics",
        "bars": {
            "ijOps": "ImageJ Ops",
            "sjOpsConvertedAndAdapted": f"SciJava Ops {AC}",
            "sjOpsConverted": f"SciJava Ops {C}",
            "sjOpsAdapted": f"SciJava Ops {A}",
            "sjOps": "SciJava Ops",
            "sjOpsWithCache": "SciJava Ops (cached)",
            "noOpsAdaptedAndConverted": f"Static Method {AC}",
            "noOpsConverted": f"Static Method {C}",
            "noOpsAdapted": f"Static Method {A}",
            "noOps": "Static Method",
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

    labels = []
    values = []
    errors = []

    # Add each benchmark in the class
    for test, label in figure["bars"].items():
        print(".", end="")
        result = results[test]
        labels.append(label)
        score = result["score"]
        values.append(score)
        error = [result["minmax"][1] - score, score - result["minmax"][0]]
        errors.append(error)

    # Create a subplot with shared y-axis
    fig = make_subplots(rows=1, cols=2, shared_yaxes=True, horizontal_spacing=0.02)

    # Add log scale bars (left side)
    fig.add_trace(
        go.Bar(
            y=labels,
            x=values,
            orientation='h',
            error_x=dict(type='data', symmetric=False, array=[e[0] for e in errors], arrayminus=[e[1] for e in errors]),
            name="Log Scale",
            marker_color='blue'
        ),
        row=1, col=1
    )

    # Add linear scale bars (right side)
    fig.add_trace(
        go.Bar(
            y=labels,
            x=values,
            orientation='h',
            error_x=dict(type='data', symmetric=False, array=[e[0] for e in errors], arrayminus=[e[1] for e in errors]),
            name="Linear Scale",
            marker_color='red'
        ),
        row=1, col=2
    )

    # Update layout
    fig.update_layout(
        title_text=figure["title"] + f"<br><sup style=\"color: gray\">{A}=Adaptation, {C}=Conversion, {AC}=Adaptation & Conversion</sup>",
        barmode='relative',
        yaxis=dict(title=""),
        #xaxis=dict(title="Log Scale (μs/execution)", type="log"),
        xaxis=dict(
            title="Log Scale (μs/execution)",
            type="log",
            range=[min(values), max(values)],
        ),
        xaxis2=dict(title="Linear Scale (μs/execution)"),
        height=max(500, 50 * len(labels)),  # Adjust height based on number of bars
        showlegend=False
    )

    # Add a vertical line at x=0
    fig.add_vline(x=0, line_width=2, line_color="black")

    # Reverse the log scale axis
    fig.update_xaxes(autorange="reversed", row=1, col=1)

    # Convert to JSON and dump
    with open(f"images/{name}.json", "w") as f:
        f.write(io.to_json(fig))

    print()

print("Done!")
