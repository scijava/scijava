# Configuration file for the Sphinx documentation builder.
#
# This file only contains a selection of the most common options. For a full
# list see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- General configuration ---------------------------------------------------
import sys
import os
from multiproject.utils import get_project

sys.path.insert(0, os.path.abspath('.'))

# Add any Sphinx extension module names here, as strings. They can be
# extensions coming with Sphinx (named 'sphinx.ext.*') or your custom
# ones.
extensions = [
    "multiproject",
    "sphinx.ext.intersphinx",
    "sphinx.ext.autodoc",
    "sphinx.ext.viewcode",
    "sphinx_search.extension",
    "sphinx_tabs.tabs",
    "sphinx_charts.charts",
    "sphinx_copybutton",
    "myst_nb",
    # NB: Needed for newer sphinx versions - see
    # https://github.com/thclark/sphinx-charts/issues/23
    "sphinxcontrib.jquery"
]


multiproject_projects = {
    "ops": {
        "path": "ops/doc/",
        "config": {
            "project": "SciJava Ops documentation",
            "html_title": "SciJava Ops documentation",
        },
    },
}

docset = get_project(multiproject_projects)

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

# -- Custom Lexers -----------------------------------------------------------

from lexers import SciJavaGroovyLexer

def setup(app):
    app.add_lexer('scijava-groovy', SciJavaGroovyLexer)

# -- Options for HTML output -------------------------------------------------

# The theme to use for HTML and HTML Help pages.  See the documentation for
# a list of builtin themes.
#
html_theme = "sphinx_rtd_theme"

# Add any paths that contain custom static files (such as style sheets) here,
# relative to this directory. They are copied after the builtin static files,
# so a file named "default.css" will overwrite the builtin "default.css".
html_static_path = []

# Add the SciJava ops logo
html_logo = "ops/images/scijava-ops.svg"
html_theme_options = {
    "logo_only": True,
    "display_version": False,
}
