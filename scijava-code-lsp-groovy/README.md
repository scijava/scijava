# SciJava Code LSP Groovy

An in-process language server for `groovy` (`GroovyLanguageServer`, offered to `scijava-code-lsp` by `GroovyServerPlugin`), driven by Groovy's own compiler, which types the code statically as far as it can (see `TypedAST`).

It knows plain Groovy only. To use this language server with SciJava scripts, see [scripting-groovy](https://github.com/scijava/scripting-groovy).

## Example

```java
Context context = new Context(LanguageServerService.class);
// (a) Start the server: the plugin offering "groovy".
LanguageServer server = context.service(LanguageServerService.class)
	.launch("groovy", new Environment());
server.initialize(new InitializeParams()).get();
server.initialized(new InitializedParams());

// (b) Ask it about a script: open it, then ask, e.g. for completions.
String script = "import java.util.ArrayList\na = new ArrayList<>()\na.ge";
server.getTextDocumentService().didOpen(new DidOpenTextDocumentParams(
	new TextDocumentItem("untitled:/example.groovy", "groovy", 1, script)));
server.getTextDocumentService().completion(new CompletionParams(
	new TextDocumentIdentifier("untitled:/example.groovy"),
	Positions.position(script, script.length()))).get(); // get, getClass, getFirst, getLast, getAt, ...

context.dispose(); // Stops the server, too.
```
