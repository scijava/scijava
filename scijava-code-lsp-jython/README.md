# SciJava Code LSP Jython

Jython code intelligence for SciJava: a language server for `jython` (`JythonLanguageServer`, offered to `scijava-code-lsp` by `JythonLanguageServerPlugin`), which is a Python language server (jedi-language-server, by `scijava-code-lsp-jedi`) that knows Java classes from Python stubs of the Java packages a script imports (see `JavaStubs`). It completes Python and Java names and members, the variables of a running interpreter, and Java classes not imported yet (importing them as they are accepted); and it gives signature help and hover.

Like Python scripts, it needs jedi-language-server's environment, which is built (with network access) the first time it is used.

It knows plain Jython only. SciJava's Jython scripts become plain Jython as scripting-jython's dialect says (their `#@` parameters declared).
