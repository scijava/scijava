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
   │  ScriptSession: a script's text, and offsets into it;
   │  shows its servers the script as plain code (ScriptDocument)
   ▼
 LanguageServerService ──► the ScriptDialect of the script's language:
   │                       the plain code's LSP language, and where it runs
   │  servers of the session's own, one per environment (RoutingLanguageServer),
   │  from the LanguageServerPlugin serving the LSP language
   ├──► in-process servers (e.g. Groovy, Jython), built on AbstractLanguageServer
   └──► server processes (e.g. jedi-language-server), kept by ServerProcesses
```

It does three things: the first for everyone, the others for those writing
servers.

## 1. Brokering between scripts and servers (`org.scijava.code.lsp`)

The core. Two kinds of plugins meet here, so that script languages need not
offer servers, nor servers know SciJava scripts:

* A `ScriptDialect` turns the scripts of a SciJava script language into plain
  code of an LSP language (e.g. Appose-Python scripts into `python`): it is
  what a script language contributes (e.g. scripting-groovy's, for Groovy
  scripts).
* A `LanguageServerPlugin` offers lsp4j `LanguageServer`s for an LSP language,
  by its id (e.g. `groovy`): e.g. `scijava-code-lsp-groovy`'s. Each language
  is served by the highest-priority plugin supporting it. A server combining
  several sources (e.g. Jython's own analysis and jedi's) combines them
  itself.

Clients ask about a script through a `ScriptSession` from the service: it
keeps its servers' copy of the script up to date, names it (its file's URI,
or an `untitled:` one), takes offsets into its text (see `Positions`), and
reports the problems found in it. Behind it are servers of its own, from the
plugin serving its dialect's language: one per environment the script runs
in, as the dialect says, asked before each request, since that may change as
the script is edited (e.g. an Appose environment it declares; see
`RoutingLanguageServer`). For code typed into a running interpreter, the
servers learn the interpreter's state through their `Environment`. Tools that
ask once (e.g. to check a script) can wait for a session's problems
(`diagnostics(timeout)`). For code typed into an interpreter whose language
has no dialect, or no servers, a session completes the interpreter's
variables.

Those wanting a server itself, for plain code of a language, get one via
`LanguageServerService.launch`, by LSP language id (e.g. `groovy`, or
`python`): in-process, or a server process (see below), whichever the plugins
offer, analyzing scripts as running in the environment given. (A server may
analyze each document in an environment of its own, based on that one: e.g.
`scijava-code-lsp-jedi`'s, with the packages a script declares added.)

A SciJava script is not quite plain code of its language: its `#@`
parameters define variables before it runs. `LanguageServerService.scriptParameters`
parses them; a `ScriptDocument` is the script as plain code, with them
declared with their run-time types, as its `ScriptDialect` says. A session
shows its servers the script so, and maps positions in their answers back.

## 2. Running server processes, and routing (`org.scijava.code.lsp.server`)

For servers that are not Java in the SciJava process (e.g. jedi-language-server,
for Python). A `ServerLauncher` says how to start one for an environment: the
process, speaking LSP over its standard streams, and the options and
workspace root to initialize it with. `ServerProcesses` keeps them: started
when first needed, one per environment, restarted if they stop, and stopped
when idle, or when its owner (e.g. a `LanguageServerPlugin`, when disposed)
shuts them down; each seen as a `LanguageServer` of its own.
`scijava-code-lsp-jedi`'s plugin serves `python` this way.

`RoutingLanguageServer` sends each document to a server of where its script
runs, as that changes (e.g. as the packages it declares do): sessions use it,
and so may servers.

## 3. A toolkit for in-process servers (`org.scijava.code.lsp.jvm`)

For servers of JVM languages, written in Java (e.g. Groovy's and Jython's):
`AbstractLanguageServer`, a base class keeping the open documents and turning
requests into questions about a document and an offset; `Members`,
completions for Java classes' members by reflection; `Callable`, something to
call as a completion snippet; `Signatures`, rating overloads against the
arguments typed; and `ClassIndex`, the classes on the classpath, with their
javadoc.

## Who uses what

|                         | 1 | 2 | 3 | offers                                 |
|-------------------------|---|---|---|----------------------------------------|
| script-editor           | ✓ |   |   | (a client)                             |
| scripting-groovy        | ✓ |   |   | a dialect, of Groovy scripts           |
| scripting-jython        | ✓ |   |   | a dialect, of Jython scripts           |
| scripting-appose-python | ✓ |   |   | a dialect, of Appose-Python scripts    |
| code-lsp-groovy         | ✓ |   | ✓ | servers of `groovy`                    |
| code-lsp-jython         | ✓ |   | ✓ | servers of `jython`, building on `python` |
| code-lsp-jedi           | ✓ | ✓ |   | servers of `python`                    |

The toolkits (`org.scijava.code.lsp.server` and `org.scijava.code.lsp.jvm`)
depend only on `org.scijava.code.lsp`. The module exports all three; its
implementation (`org.scijava.code.lsp.impl`) is internal.
