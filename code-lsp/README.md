# SciJava Code LSP

A bridge between SciJava code intelligence (`scijava-code-api`) and the
[Language Server Protocol](https://microsoft.github.io/language-server-protocol/),
in both directions.

## Using language servers

`LspClient` asks a language server about SciJava scripts, in the terms of
`scijava-code-api`: completions, signature help, hover and diagnostics, at
positions of the script as written. The server sees the script as plain code
of its language (a `ScriptDocument`: its `#@` parameters declared with their
run-time types), opened, changed and closed as the script is edited.
Answers are reused while the same question is asked again, and pending
questions are cancelled (`$/cancelRequest`) once superseded.

`LspServers` starts servers in the background (one per environment scripts
run in), restarts them if they stop, and stops them when idle. `LspServerSpec`
says how to start one; `LspServerPlugin`s offer them per language, and
`LspService` shares them between the completers of a context. For example,
`scripting-appose-python` offers jedi-language-server, which both its own
completer and Jython's use.

## Being a language server

`org.scijava.code.lsp.server.Main` runs a language server for SciJava
scripts over standard input and output: completions, signature help, hover
and diagnostics from the code completers of the SciJava context, for every
script language they support (by file extension), with SciJava's own
understanding of scripts. Editors outside SciJava can use it, e.g. VS Code
via a generic language client extension, configured to run:

```
java -cp "<Fiji>/jars/*;<Fiji>/plugins/*" org.scijava.code.lsp.server.Main
```

(with `:` instead of `;` on Linux and macOS) for the file types of interest
(e.g. `*.py` for Jython, `*.groovy`).
