# SciJava Code LSP

Code intelligence for SciJava scripts (completions, signature help, hover and
diagnostics), in the terms of the
[Language Server Protocol](https://microsoft.github.io/language-server-protocol/),
via [lsp4j](https://github.com/eclipse-lsp4j/lsp4j).

## Language servers

Every source of code intelligence is an lsp4j `LanguageServer`: in the
SciJava process (e.g. Groovy's and Jython's, often built on
`ScriptLanguageServer`), or a proxy to a server process (e.g.
jedi-language-server, for Python run via Appose).

`LanguageServerPlugin`s offer servers per script language, and say where a
script runs (an `Environment`: interpreter, search paths, classpath, and a
running interpreter's state, if any). The `LanguageServerService` hands
callers (e.g. the Script Editor) one `MergedLanguageServer` per language,
standing for all of its plugins' servers: it opens, changes and closes each
document on every server for that script, and merges their answers.
Completions that take longer than a moment come later, as an
`UpdatingCompletionList`'s update; signatures can be rated against the
arguments typed (`RatedSignatureInformation`). Tools that ask once (e.g. to
check a script) can call `LanguageServerService.diagnose`.

Servers see a script as plain code of its language (a `ScriptDocument`: its
`#@` parameters declared with their run-time types, per a `ScriptDialect`);
`TransformingLanguageServer` does this for servers that know the language but
not SciJava, and maps positions back.

Server processes are started by `LspServers` (one per environment scripts run
in), restarted if they stop, and stopped when idle; an `LspServerSpec` says
how to start one, and `LspServerPlugin`s offer them per LSP language id
(`LanguageServerService.launch`).

## Being a language server

`org.scijava.code.lsp.server.Main` runs a language server for SciJava
scripts over standard input and output: the `LanguageServerService`'s
servers, for every script language they support (by file extension). Editors
outside SciJava can use it, e.g. VS Code via a generic language client
extension, configured to run:

```
java -cp "<Fiji>/jars/*;<Fiji>/plugins/*" org.scijava.code.lsp.server.Main
```

(with `:` instead of `;` on Linux and macOS) for the file types of interest
(e.g. `*.py` for Jython, `*.groovy`).
