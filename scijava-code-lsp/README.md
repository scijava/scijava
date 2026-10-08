# SciJava Code LSP

Code intelligence for SciJava scripts (completions, signature help, hover and
diagnostics), in the terms of the
[Language Server Protocol](https://microsoft.github.io/language-server-protocol/).

[lsp4j](https://github.com/eclipse-lsp4j/lsp4j) supplies the protocol: its
types, the `LanguageServer` and `LanguageClient` interfaces, and JSON-RPC over
a pair of streams. This module is the client side that lsp4j leaves to its
users (deciding which servers answer for a script, keeping them in sync with
it, and starting server processes), plus what is particular to SciJava
scripts.

```
 client (e.g. Script Editor)
   │  ScriptSession: a script's text, and offsets into it
   ▼
 LanguageServerService ──► the LanguageServerPlugins serving the language
   │  one server standing for all of them (merged answers)
   ├──► in-process servers (e.g. Groovy, Jython), built on ScriptLanguageServer
   └──► server processes (e.g. jedi-language-server), via LspServerSpec
          seeing the script as plain code (TransformingLanguageServer)
```

It does five things, each in a package of its own (the first and the last
share one).

## 1. Brokering between scripts and servers (`org.scijava.code.lsp`)

The core. `LanguageServerPlugin`s offer lsp4j `LanguageServer`s for script
languages, and say where a script runs (an `Environment`: interpreter, search
paths, classpath, and a running interpreter's state, if any). For each
script, the `LanguageServerService` gathers every server of its language, in
its environment, behind one server: it opens, changes and closes the script
on each, merges their answers, and moves the script to other servers should
where it runs change.

Clients ask about a script through a `ScriptSession` from the service: it
keeps the servers' copy of the script up to date, names it (its file's URI,
or an `untitled:` one), takes offsets into its text (see `Positions`), and
reports the problems found in it. Tools that ask once (e.g. to check a
script) can call `LanguageServerService.diagnose`. For code typed into an
interpreter whose language no plugin serves, a session completes the
interpreter's variables.

## 2. Running server processes (`org.scijava.code.lsp.launch`)

For servers that are not Java in the SciJava process (e.g. jedi-language-server,
for Python). An `LspServerPlugin` offers an `LspServerSpec` for an LSP
language id (e.g. `python`), saying how to start a server for an environment
and returning its streams (an `LspConnection`). Plugins get such a server via
`LanguageServerService.launch`: started when first needed, one per
environment, restarted if it stops, and stopped when idle.

## 3. Translating SciJava scripts (`org.scijava.code.lsp.script`)

A SciJava script is not quite plain code of its language: its `#@`
parameters define variables before it runs. `LanguageServerService.scriptParameters`
parses them; a `ScriptDocument` is the script as plain code, with them
declared with their run-time types, as its language's `ScriptDialect` says.
`TransformingLanguageServer` shows servers that know the language but not
SciJava (e.g. jedi) this text, and maps positions in their answers back.
In-process servers use `ScriptDocument` too, to know the parameters.

## 4. A toolkit for in-process servers (`org.scijava.code.lsp.jvm`)

For servers of JVM languages, written in Java (e.g. Groovy's and Jython's):
`ScriptLanguageServer`, a base class keeping the open documents and turning
requests into questions about a document and an offset; `Members`,
completions for Java classes' members by reflection; `Callable`, something to
call as a completion snippet; `Signatures`, rating overloads against the
arguments typed; and `ClassIndex`, the classes on the classpath, with their
javadoc.

## 5. Additions to LSP (`org.scijava.code.lsp`)

For in-process use, where both sides are SciJava's (over JSON-RPC, they
degrade to plain LSP): `UpdatingCompletionList`, completions answered at once
with better ones to follow (e.g. a slower server's); and
`RatedSignatureInformation`, a signature rated by how well the arguments
typed so far fit it.

## Who uses what

|                         | 1 | 2 | 3 | 4 | 5 |
|-------------------------|---|---|---|---|---|
| script-editor           | ✓ |   |   |   | ✓ |
| scripting-groovy        | ✓ |   | ✓ | ✓ | ✓ |
| scripting-jython        | ✓ | ✓ | ✓ | ✓ | ✓ |
| scripting-appose-python | ✓ | ✓ | ✓ |   |   |

The specialized packages depend only on `org.scijava.code.lsp`, never on each
other. The module exports all four; its implementation
(`org.scijava.code.lsp.impl`) is internal.
