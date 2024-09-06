This directory houses the online docs for core SciJava components. Each subfolder is its own ReadTheDocs site.

## Building the docs

If this is your first time building the docs on this machine, create the needed environment with:

```shell
mamba env create -f environment.yml
```

Subsequently, every time you want to build, run the following commands:

```
mamba activate scijava-docs
make clean html && python -m http.server
```

If all goes well, you'll see output ending like:
```
The HTML pages are in _build/scijava-ops/html.
Serving HTTP on 0.0.0.0 port 8000 (http://0.0.0.0:8000/) ...
```

To view the built site, open the HTTP link in your browser of choice, then navigate to the stated subdirectory.

## Adding a new ReadTheDocs page for a project in this repository

We use [`sphinx-multiproject`](https://sphinx-multiproject.readthedocs.io/en/latest/index.html) to build multiple RTD sites from within a single repository. To add a new site within this repository, take the following steps:

### Create a new subfolder for the site's documents

See the existing `ops` folder for a template.

### Add relevant sections to this folder's `conf.py`

Specifically, you'll want to add an entry to the `multiproject_projects` dictionary. Again, you can copy and edit the `ops` entry.

### Create a new project on `ReadTheDocs`

You'll want to take the following steps:
1. Choose to import a project manually
2. Set the repository URL to the GitHub of this project
3. In the `Admin` section of the newly created site, find the `Advanced Settings` tab and change the `Path for .readthedocs.yaml` to `docs/.readthedocs.yaml`
4. In the `Admin` section of the newly created site, find the `Environment Variables` tab and add a new variable, mapping the `PROJECT` environment variable to the newly added key in the `multiproject_projects` dictionary.

After that, you should have a new site ready to go!
