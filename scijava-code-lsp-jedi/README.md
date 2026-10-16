# SciJava Code LSP Jedi

Python code intelligence for SciJava scripts:
[jedi-language-server](https://github.com/pappasam/jedi-language-server)
(built on [Jedi](https://jedi.readthedocs.io/)), offered to
`scijava-code-lsp` for two LSP languages:

* `python`, by `PythonServerPlugin`: for editors. Completions are asked
  about a word's first character only, and reused as the word is typed on;
  questions no longer of interest are cancelled; and the first completions of
  a word get their signatures, as snippets with their parameters and types.
  Packages a script declares (in its PEP 723 `# /// script` block) but its
  environment lacks, e.g. a dependency just added, are installed for
  completion only, into a separate directory (an "overlay", which Jedi
  searches), a moment after the dependencies stop changing; so they are known
  before the script's environment is (re)built. Script languages whose
  scripts become plain Python (e.g. scripting-appose-python's) get it this
  way.
* `jedi`, by `JediServerProcessPlugin`: the server process itself, as is
  (e.g. for scijava-code-lsp-jython, beside Jython's own analysis).

The server runs in a small environment of its own (`code-lsp-jedi`: Python,
Jedi, jedi-language-server, NumPy, and uv to install overlays), which
[Appose](https://github.com/apposed/appose) builds next to its other
environments the first time the server is needed; that takes a while only
then. Its error output goes to `code-lsp-jedi-server.log`, and overlays to
`code-lsp-jedi-overlays`, next to the environment.

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

`JediLifecycleIntegrationTest` does all this with the `jedi` server (and
asks for hover too), then checks that shutting down stops the server's
process.
