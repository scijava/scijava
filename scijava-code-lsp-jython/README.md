# SciJava Code LSP Jython

A language server for `jython`, backed by an (external) Python language server, for use with `scijava-code-lsp`.

It knows plain Jython only. SciJava's Jython scripts become plain Jython as scripting-jython's dialect says (their `#@` parameters declared).

## Example

```java
Context context = new Context(LanguageServerService.class);
// (a) Start the server: the plugin offering "jython".
LanguageServer server = context.service(LanguageServerService.class)
	.launch("jython", new Environment());
server.initialize(new InitializeParams()).get();
server.initialized(new InitializedParams());

// (b) Ask it about a script: open it, then ask, e.g. for completions.
String script = "from java.util import ArrayList\na = ArrayList()\na.ge";
server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
	new TextDocumentItem("untitled:/example.py", "jython", 1, script)));
server.getTextDocumentService().completion(new CompletionParams(
	new TextDocumentIdentifier("untitled:/example.py"),
	Positions.position(script, script.length()))).get(); // get, getClass, getFirst, getLast

context.dispose(); // Stops the server, too.
```
