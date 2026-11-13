# SciJava Code LSP ty

A language server for `python`, backed by [ty](https://docs.astral.sh/ty/), for use with `scijava-code-lsp`.

ty runs as a process per environment scripts run in, in a small environment of its own, which [Appose](https://github.com/apposed/appose) builds the first time it is needed (see `TyLauncher`).

## Example

```java
Context context = new Context(LanguageServerService.class);
// (a) Start the server: the plugin offering "python", for scripts run by the
// server's own Python (or, e.g., new Environment().withInterpreter(python)).
LanguageServer server = context.service(LanguageServerService.class)
	.launch("python", new Environment());
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
