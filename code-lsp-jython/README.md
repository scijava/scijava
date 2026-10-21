# SciJava Code LSP Jython

Jython code intelligence for SciJava: an in-process language server for
`jython` (`JythonLanguageServer`, offered to `scijava-code-lsp` by
`JythonLanguageServerPlugin`), driven by Jython's own parser (see
`JythonAutoCompletions`). It completes Java classes, their members and
constructors, and the variables of the script and of a running interpreter;
and it gives signature help and hover.

If a Python language server is offered (as `python`, e.g. jedi-language-server
by `scijava-code-lsp-jedi`), it runs beside (see `PythonCompanion`): for Python modules, and for Java
classes, from Python stubs of the Java packages a script imports (see
`JavaStubs`). Its completions follow Jython's own.

It knows plain Jython only. SciJava's Jython scripts become plain Jython as
scripting-jython's dialect says (their `#@` parameters declared).
