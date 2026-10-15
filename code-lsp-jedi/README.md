# SciJava Code LSP Jedi

Python code intelligence for SciJava scripts:
[jedi-language-server](https://github.com/pappasam/jedi-language-server)
(built on [Jedi](https://jedi.readthedocs.io/)), offered to
`scijava-code-lsp` as a `ServerProcessPlugin` for the `python` language.
Script languages get it via `LanguageServerService.launch("python", ...)`:
e.g. scripting-appose-python, for Python scripts run via Appose, and
scripting-jython, beside Jython's own analysis.

The server runs in a small environment of its own (`code-lsp-jedi`: Python,
Jedi, jedi-language-server and NumPy), which [Appose](https://github.com/apposed/appose)
builds next to its other environments the first time the server is needed;
that takes a while only then. Its error output goes to
`code-lsp-jedi-server.log`, next to the environment.

Each server analyzes scripts against the environment they run in, as given
by the script language: its Python (if any; otherwise the server's own) and
extra search paths.

## Example

```java
Context context = new Context(LanguageServerService.class);
// (a) Start the server: the plugin offering "python", for scripts run by the
// server's own Python (or, e.g., new Environment(python, paths, null)).
LanguageServer server = context.service(LanguageServerService.class)
	.launch("python", Environment.NONE);
server.initialize(new InitializeParams()).get();
server.initialized(new InitializedParams());

// (b) Ask it about a script: open it, then ask, e.g. for completions.
String script = "import json\njson.du";
server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
	new TextDocumentItem("untitled:/example.py", "python", 1, script)));
server.getTextDocumentService().completion(new CompletionParams(
	new TextDocumentIdentifier("untitled:/example.py"),
	Positions.position(script, script.length()))).get(); // dump, dumps

context.dispose(); // Stops the server, too.
```

`JediLifecycleIntegrationTest` does all this (and asks for hover too), then
checks that shutting down stops the server's process.
