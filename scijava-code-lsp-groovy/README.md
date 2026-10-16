# SciJava Code LSP Groovy

Groovy code intelligence for SciJava: an in-process language server for
`groovy` (`GroovyLanguageServer`, offered to `scijava-code-lsp` by
`GroovyServerPlugin`), driven by Groovy's own compiler, which types the code
statically as far as it can (see `TypedAST`). It completes names in scope,
members of objects and classes (Java's, by reflection), class names (adding
their imports) and keywords; and it gives signature help, hover and the
compiler's problems.

It knows plain Groovy only. SciJava's Groovy scripts become plain Groovy as
scripting-groovy's dialect says (their `#@` parameters declared); for code
typed into a running interpreter, its bindings are declared too, as Java
objects of their values' types.
