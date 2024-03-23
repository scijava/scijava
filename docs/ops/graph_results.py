import json

import plotly.graph_objects as go
import plotly.io as io

# This script parses JMH benchmarking results into charts developed using plot.ly (https://plotly.com/)
# It currently develops one boxplot PER class, with each JMH benchmark method represented as a separate boxplot.
# It expects JMH benchmark results be dumped to a file "scijava-ops-benchmark_results.txt", within its directory.

# If you'd like to add a title to the plotly charts, add an entry to the following dict.
#
# The key should be the simple name of the class containing the JMH benchmark
# and the value should be the title of the chart
figure_titles = {
    "BenchmarkFrameworks" : "Algorithm Execution Performance by Framework",
    "BenchmarkCaching" : "Caching Effects on Op Matching Performance",
    "BenchmarkConversion": "Parameter Conversion Performance",
    "BenchmarkMatching": "Basic Op Matching Performance",
}

# If you'd like to alias a particular test in the chart categories, add an entry to the following dict.
#
# The key should be the JMH benchmark method name, and the value should be the alias
benchmark_categories = {
    "imageJOps" : "ImageJ Ops",
    "sciJavaOps": "SciJava Ops",
    "runStatic" : "Static Method",
    "runOp" : "Op Execution",
    "runOpCached": "Op Execution (cached)",
    "runOpConverted": "Op Execution (converted)",
    "runOpConvertedAdapted": "Op Execution (converted + adapted)",
    "runOpAdapted": "Op Execution (adapted)",
}

# Read in the benchmark results
with open("scijava-ops-benchmarks_results.txt") as f:
    lines = f.readlines()

# Keep only the lines containing our desired results
for i in range(len(lines) - 1, 0, -1):
    if (lines[i].startswith("Benchmark ")):
        lines = lines[i+1:]
        break

# Build a map of results by benchmark class
benchmark_classes = {}
for line in lines:
    words = line.split()
    test = words[0]
    last_period = test.rfind('.')
    cls = test[:last_period]
    test = test[last_period+1:]

    if cls not in benchmark_classes:
        benchmark_classes[cls] = {}
        
    benchmark_classes[cls][test] = words[1:]

# For each class, build a chart and dump it to JSON
for cls, data in benchmark_classes.items():
    period_pos = cls.rfind(".")
    if period_pos > -1:
        cls = cls[period_pos+1:]
    x = []
    y = []
    error_y = []

    # Add each benchmark in the class
    for method, line in data.items():
        method = benchmark_categories.get(method, method)
        x.append(method)
        y.append(float(line[2]))
        error_y.append(float(line[4]))
    # Create a bar chart
    fig = go.Figure()
    fig.add_bar(
        x=x,
        y=y,
        error_y=dict(type='data', array=error_y),
    )
    fig.update_layout(
        title_text=figure_titles.get(cls, "TODO: Add title"),
        yaxis_title="Performance (s/op)"
    )

    # Convert to JSON and dump
    with open(f"images/{cls}.json", "w") as f:
        f.write(io.to_json(fig))
