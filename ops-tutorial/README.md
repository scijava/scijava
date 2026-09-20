# SciJava Ops Tutorial

These tutorials are intended to familiarize the reader with SciJava Ops. In general,
the [Python](#running-ops-in-python) use-cases are more targeted towards users, while [Java](#running-ops-in-java) is
more developer focused.

## Running Ops in Python

### Pre-requisities
* [Maven](https://maven.apache.org/install.html)
* [Python](https://www.python.org/downloads/)
* [Conda](https://docs.conda.io/projects/conda/en/latest/user-guide/install/index.html) or [Mamba](https://github.com/conda-forge/miniforge#mambaforge)

You may wish to use ops from a [jupyter notebook](#jupyter-notebooks) or simply
a [python interpreter](#from-an-interpreter); in each case we recommend starting from the provided `environment.yml`.
To create and activate this environment, from the `scijava-ops-tutorial` directory in the terminal of your choice, run:

```bash
mamba env create
mamba activate scijava-ops
```

### Jupyter Notebooks

[Jupyter notebooks](https://jupyter.org/) are one of the easiest ways to interact with python code. The notebook we have
provided is intended to introduce you to python programming in ops. You can simply open it from the command line via:

```bash
jupyter notebook notebooks/SciJavaOps.ipynb
```

### From an Interpreter

We recommend using the [ipython](https://ipython.org/) interpreter, as it includes many conveniences by default. In
particular, tab completion is very useful when exploring available ops.

From the command line, start your interpreter in interactive mode and execute the intro script:

```bash
ipython -i scripts/ops-setup.py
```

This will create a new `ops` object in your environment that can then be used to explore available ops. The easiest way
to do this is via tab completion.

For example, `ops.<TAB>` will show all available namespaces and global ops (ops with no namespace):
![global ops tab completion](/resources/ops-dot.png)

As you go into a particular namespace you can tab-complete to see the ops therein, e.g. `ops.math.<TAB>`:
![math namespace tab completion](/resources/ops-dot-math.png)

You can get more information about the actual available implementations for a given Op, or all available ops, via
the `help` method:

```bash
# Print all available Ops
ops.help()

# Print all math.add Ops
ops.help("math.add")
```

You can also access an `OpEnvironment` via the `env` variable, for a [Java](#running-ops-in-java)-style API.

## Running Ops in Java

### Pre-requisities
* [Maven](https://maven.apache.org/install.html)

### From an IDE

The [OpsIntro](src/main/java/org/scijava/ops/tutorial/OpsIntro.java) class is designed to take a developer through the
basic concepts of the SciJava Ops framework. It includes examples of Ops calls, how to interrogate available Ops, and
additional commentary about the framework.

Moving beyond the intro, the additional classes in the [tutorial package](src/main/java/org/scijava/ops/tutorial)
provide focused guides to particular tasks or concepts.
