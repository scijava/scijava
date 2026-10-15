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
   │  shows its server the script as plain code (ScriptDocument)
   ▼
 LanguageServerService ──► the LanguageServerPlugin serving the language
   │  a server of the session's own, and its ScriptDialect
   ├──► in-process servers (e.g. Groovy, Jython), built on AbstractLanguageServer
   └──► server processes (e.g. jedi-language-server), via ServerProcessPlugin
```

It does three things.

## 1. Brokering between scripts and servers (`org.scijava.code.lsp`)

The core. `LanguageServerPlugin`s offer lsp4j `LanguageServer`s for
languages, by LSP language id (e.g. `groovy`), and so for the SciJava script
languages of those names; each language is served by the highest-priority
plugin supporting it. A server combining several sources (e.g. Jython's own analysis and
jedi's) combines them itself.

Clients ask about a script through a `ScriptSession` from the service, with
a server of its own: it keeps the server's copy of the script up to date,
names it (its file's URI, or an `untitled:` one), takes offsets into its text
(see `Positions`), and reports the problems found in it. For code typed into
a running interpreter, the server learns the interpreter's state through its
`Environment`. Tools that ask once (e.g. to check a script) can wait for a
session's problems (`diagnostics(timeout)`). For code typed into an
interpreter whose language no plugin serves, a session completes the
interpreter's variables.

Those wanting a server itself, for plain code of a language, get one via
`LanguageServerService.launch`, by LSP language id (e.g. `groovy`, or
`python`): in-process, or a server process (see below), whichever the plugins
offer, analyzing scripts as running in the environment given. (Plugins whose
scripts say where they run, which may change as they are edited, keep a
server per environment: e.g. scripting-appose-python's.)

Servers need know nothing of SciJava scripts. A SciJava script is not quite
plain code of its language: its `#@` parameters define variables before it
runs. `LanguageServerService.scriptParameters` parses them; a `ScriptDocument`
is the script as plain code, with them declared with their run-time types, as
a `ScriptDialect` says. Each `LanguageServerPlugin` names the dialect its
servers read; a session shows its server the script in that dialect, and maps
positions in the server's answers back.

## 2. Running server processes (`org.scijava.code.lsp`)

For servers that are not Java in the SciJava process (e.g. jedi-language-server,
for Python). A `ServerProcessPlugin` says how to start one for an LSP language
id (e.g. `python`) and an environment: the process, speaking LSP over its
standard streams, and the options and workspace root to initialize it with.
Plugins get such a server via `LanguageServerService.launch`, as code-lsp's
own `LanguageServerPlugin` for server processes offers it: started when first
needed, one per environment, restarted if it stops, and stopped when idle. `scijava-code-lsp-jedi` offers jedi-language-server this way.

## 3. A toolkit for in-process servers (`org.scijava.code.lsp.jvm`)

For servers of JVM languages, written in Java (e.g. Groovy's and Jython's):
`AbstractLanguageServer`, a base class keeping the open documents and turning
requests into questions about a document and an offset; `Members`,
completions for Java classes' members by reflection; `Callable`, something to
call as a completion snippet; `Signatures`, rating overloads against the
arguments typed; and `ClassIndex`, the classes on the classpath, with their
javadoc.

## Who uses what

|                         | 1 | 2 | 3 |
|-------------------------|---|---|---|
| script-editor           | ✓ |   |   |
| scripting-groovy        | ✓ |   | ✓ |
| scripting-jython        | ✓ | ✓ | ✓ |
| scripting-appose-python | ✓ | ✓ |   |
| code-lsp-jedi           |   | ✓ |   |

The toolkit (`org.scijava.code.lsp.jvm`) depends only on
`org.scijava.code.lsp`. The module exports both; its implementation
(`org.scijava.code.lsp.impl`) is internal.
