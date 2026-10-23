# SciJava Code LSP Jedi

Python code intelligence for SciJava scripts: [jedi-language-server](https://github.com/pappasam/jedi-language-server) (built on [Jedi](https://jedi.readthedocs.io/)), offered to `scijava-code-lsp` for `python` by `PythonServerPlugin`, made to work well in editors:

* Completions are asked about a word's first character only, and reused as the word is typed on; questions no longer of interest are cancelled; and the first completions of a word get their signatures, as snippets with their parameters and types.

Script languages whose scripts become plain Python (e.g. scripting-appose-python's) get it this way; and servers building on Python's (e.g. scijava-code-lsp-jython's, beside Jython's own analysis).

The server runs as a process, one per environment scripts run in (see `JediLauncher`), in a small environment of its own (`code-lsp-jedi`: Python, Jedi, jedi-language-server, and NumPy), which [Appose](https://github.com/apposed/appose) builds next to its other environments the first time the server is needed; that takes a while only then. Its error output goes to `code-lsp-jedi-server.log`, next to the environment.

Each server analyzes scripts in exactly the environment it is given: its Python (if any; otherwise the server's own) and extra search paths (e.g. stubs of Java packages).

## Example

```java
Context context = new Context(LanguageServerService.class);
// (a) Start the server: the plugin offering "python", for scripts run by the
// server's own Python (or, e.g., new Environment(python, paths, null)).
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

`JediLifecycleIntegrationTest` does all this (and asks for hover too), then checks that disposing the context stops the server's process.
