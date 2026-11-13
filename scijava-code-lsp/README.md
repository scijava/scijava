# SciJava Code LSP

Manages code intelligence through the [Language Server Protocol](https://microsoft.github.io/language-server-protocol/), based upon the [lsp4j](https://github.com/eclipse-lsp4j/lsp4j) library.

# Running Language Servers

`scijava-code-lsp` provides a `LanguageServerService`, allowing callers to find and launch `lsp4j` `LanguageServer` objects for a given programming language. Once created, these `LanguageServer` objects can be passed to any downstream application that knows how to use such an object.

# Interrogating SciJava Scripts

SciJava Scripts *cannot* simply be handed off to a `LanguageServer` for tasks like auto-completion: SciJava Script parameters, as well as Appose-Python environment information (and maybe other things in the future) must be converted to a representation that a `LanguageServer` can understand. To that end, `scijava-code-lsp` offers the `ScriptSession` class, for synchronizing a SciJava script with a `LanguageServer` as the script is edited: clients only ever deal with the script's own text and offsets, and servers only ever see plain code of their language.
