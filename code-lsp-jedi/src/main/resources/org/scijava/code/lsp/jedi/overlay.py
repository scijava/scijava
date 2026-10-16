###
# #%L
# Python code intelligence for SciJava, via jedi-language-server.
# %%
# Copyright (C) 2026 SciJava developers.
# %%
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# 
# 1. Redistributions of source code must retain the above copyright notice,
#    this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
#    this list of conditions and the following disclaimer in the documentation
#    and/or other materials provided with the distribution.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.
# #L%
###
# Installs, for code completion only, the packages a script declares but its
# environment lacks (e.g. ones just added to its dependencies), so that Jedi
# knows them before the environment is (re)built.
#
# Runs on jedi-language-server's own Python, whose environment includes uv.
# Installs into a separate directory (an "overlay", which Jedi adds to its
# search path), never into the script's environment itself; and only the
# missing packages themselves (--no-deps): their dependencies usually are
# installed.
#
# Input, as JSON on standard input:
#
#   python       -- the Python executable of the script's environment, or
#                   null if it is not known (then all dependencies are
#                   missing, installed for this Python, which Jedi then
#                   analyzes against)
#   dependencies -- the script's dependencies (PEP 508 requirement strings)
#   overlay      -- the directory to install into
#
# Output, on its last line: the overlay directory, or nothing if nothing is
# missing.

import json as _sj_json
import os as _sj_os
import re as _sj_re
import shutil as _sj_shutil
import subprocess as _sj_subprocess
import sys as _sj_sys

_sj_input = _sj_json.loads(_sj_sys.stdin.read())
python = _sj_input["python"]
dependencies = _sj_input["dependencies"]
overlay = _sj_input["overlay"]

_SJ_INSTALLED = (
    "import importlib.metadata as m, json, re\n"
    "print(json.dumps(sorted({re.sub(r'[-_.]+', '-', (d.metadata['Name'] or '')"
    ".lower()) for d in m.distributions()})))"
)


def _sj_name(requirement):
    """The normalized distribution name of a PEP 508 requirement string."""
    m = _sj_re.match(r"\s*([A-Za-z0-9][A-Za-z0-9._-]*)", requirement)
    return _sj_re.sub(r"[-_.]+", "-", m.group(1).lower()) if m else None


def _sj_overlay():
    target = python or _sj_sys.executable
    installed = set()
    if python:
        result = _sj_subprocess.run(
            [python, "-c", _SJ_INSTALLED],
            capture_output=True,
            text=True,
            timeout=120,
        )
        if result.returncode == 0:
            installed = set(_sj_json.loads(result.stdout))
    missing = [
        r for r in dependencies if _sj_name(r) and _sj_name(r) not in installed
    ]

    if not missing:
        _sj_shutil.rmtree(overlay, ignore_errors=True)
        return None

    marker = _sj_os.path.join(overlay, ".scijava-overlay.json")
    wanted = {"python": target, "requirements": sorted(missing)}
    try:
        with open(marker, encoding="utf-8") as f:
            if _sj_json.load(f) == wanted:
                return overlay  # Already up to date.
    except (OSError, ValueError):
        pass

    from uv import find_uv_bin

    # Install next to the overlay, then swap it in, so that Jedi never sees a
    # half-installed overlay.
    staging = overlay + ".new"
    _sj_shutil.rmtree(staging, ignore_errors=True)
    result = _sj_subprocess.run(
        [find_uv_bin(), "pip", "install", "--target", staging, "--no-deps",
         "--python", target, *missing],
        capture_output=True,
        text=True,
        timeout=600,
    )
    if result.returncode != 0:
        _sj_shutil.rmtree(staging, ignore_errors=True)
        raise RuntimeError("uv pip install failed:\n" + result.stderr)
    with open(_sj_os.path.join(staging, ".scijava-overlay.json"), "w",
              encoding="utf-8") as f:
        _sj_json.dump(wanted, f)
    _sj_shutil.rmtree(overlay, ignore_errors=True)
    _sj_os.replace(staging, overlay)
    return overlay


print(_sj_overlay() or "")
