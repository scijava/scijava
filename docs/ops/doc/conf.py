# Configuration file for the Sphinx documentation builder.
#
# This file only contains a selection of the most common options. For a full
# list see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Path setup --------------------------------------------------------------

# If extensions (or modules to document with autodoc) are in another directory,
# add these directories to sys.path here. If the directory is relative to the
# documentation root, use os.path.abspath to make it absolute, like shown here.
#
import os
import sys

sys.path.insert(0, os.path.abspath("."))


# -- Project information -----------------------------------------------------

project = "SciJava Ops"
copyright = "2014-2024 SciJava developers"
author = "SciJava developers"


# -- General configuration ---------------------------------------------------

github_url = "https://github.com/scijava/scijava/blob/main/docs/ops/doc"

# Add any paths that contain templates here, relative to this directory.
templates_path = ["_templates"]

# List of patterns, relative to source directory, that match files and
# directories to ignore when looking for source files.
# This pattern also affects html_static_path and html_extra_path.
exclude_patterns = [
    "_build",
    "Thumbs.db",
    ".DS_Store",
    "README.md",
    "examples/README.md",
]

# -- MyST-Parser/MyST-NB configuration ---------------------------------------
myst_heading_anchors = 4
nb_execution_mode = "off"

# -- Sphinx Copy Button ------------------------------------------------------

copybutton_selector = "div:not(.no-copybutton) > div.highlight > pre"

# -- Options for HTML output -------------------------------------------------
# Add the SciJava logo
# html_logo = "doc-images/logo.svg"
# html_theme_options = {
#     "logo_only": True,
#     "display_version": False,
# }
