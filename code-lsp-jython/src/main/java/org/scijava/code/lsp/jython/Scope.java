/*-
 * #%L
 * Jython language support for SciJava Script Editor.
 * %%
 * Copyright (C) 2020 - 2024 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.scijava.code.lsp.jython;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.python.indexer.Indexer;
import org.python.indexer.types.NModuleType;



class Scope {
	final Scope parent;
	final String className; // if any
	final private List<Scope> children = new ArrayList<>();
	final HashMap<String, DotAutocompletions> imports = new HashMap<>();
	final HashMap<String, DotAutocompletions> vars = new HashMap<>();
	
	/** Access to jython's builtins (functions in the global scope) and default modules (array, itertools, csv, etc.)
	 *  as well as to an other user-defined modules.
	 */
	static final Indexer indexer = new Indexer();
	
	static private Thread module_watcher;
	static private WatchService watcher;
	static private Hashtable<WatchKey, Path> keys = new Hashtable<>();
	
	static {
		try {
			watcher = FileSystems.getDefault().newWatchService();
			module_watcher = new Thread() {
				@Override
				public void run() {
					while (true) {
						if (isInterrupted()) return;
						WatchKey key = null;
						try {
							key = watcher.take(); // waits until there is an event
						} catch (InterruptedException x) {
							return;
						}
						if (keys.containsKey(key)) {
							// One of the files changed: unload all, given that parent modules would have been loaded as well
							// and it gets complicated quickly to find out which need to be reloaded and which don't.
							keys.clear();
							synchronized (indexer) {
								indexer.clearModuleTable();
							}
						}
					}
				}
			};
			module_watcher.setPriority(Thread.NORM_PRIORITY);
			module_watcher.start();
		} catch (Exception e ){
			// NB: Then, python modules are not reloaded as they change.
		}
	}
	
	/**
	 * Load a python module and watch its file, if any.
	 * When the file is updated or deleted, all loaded modules will be removed from the cache,
	 * because the loading of a module may trigger the loading of a parent module
	 * or additional modules that it links to.
	 * 
	 * @param qname
	 * @return The python module.
	 */
	static NModuleType loadPythonModule(final String qname) {
		synchronized (indexer) {
			NModuleType mod = null;
			try {
				mod = indexer.loadModule(qname);
				if (null == mod) return null;
			} catch (Exception e) {
				return null;
			}
			try {
				// Watch ALL THE FILES in the containing directly
				final String qname_slash = qname.replace(".", "/");
				final String filepath = indexer.getLoadedFiles().stream()
						.filter(s -> s.endsWith("/" + qname_slash + ".py") || s.endsWith("/" + qname_slash + "/__init__.py")).findFirst().orElse(null);
				if (null != filepath) {
					final Path path = new File(filepath).getParentFile().toPath(); // watching directories
					final WatchKey key = path.register(watcher,
							StandardWatchEventKinds.ENTRY_MODIFY,
							StandardWatchEventKinds.ENTRY_DELETE);
					keys.put(key, path);
				}
			} catch (Exception e) {
				// NB: Nothing to complete from it.
			}
			return mod;
		}
	}
	
	public Scope(final Scope parent) {
		this(parent, null);
	}
	
	public Scope(final Scope parent, final String className) {
		this.parent = parent;
		if (null != parent) {
			parent.children.add(this);
		}
		this.className = className;
	}
	
	public boolean isEmpty() {
		return imports.isEmpty() && vars.isEmpty();
	}
	
	public DotAutocompletions find(final String name, final DotAutocompletions default_value) {
		Scope scope = this;
		while (null != scope) {
			DotAutocompletions da = scope.vars.get(name);
			if (null == da)
				da = scope.imports.get(name);
			if (null != da)
				return da;
			scope = scope.parent;
		}
		// Check python builtins
		final String builtin_className = "__builtin__." + name + "."; // e.g. __builtin__.str.join
		final List<CompletionText> dotAutocompletions = indexer.getBindings().keySet().stream()
				.filter(s -> s.startsWith(builtin_className))
				.map(s -> new CompletionText(s.substring(builtin_className.length())))
				.collect(Collectors.toList());
		if (!dotAutocompletions.isEmpty())
			return new ClassDotAutocompletions(name, Collections.emptyList(), Collections.emptyList(), dotAutocompletions, this);
		
		return default_value;
	}
	
	public List<String> findStartsWith(final String name) {
		final List<String> completions = new ArrayList<>();
		Scope scope = this;
		while (null != scope) {
			for (final String varName: scope.vars.keySet()) {
				if (varName.startsWith(name)) completions.add(varName);
			}
			for (final String importName: scope.imports.keySet()) {
				if (importName.startsWith(name)) completions.add(importName);
			}
			for (String builtinName: indexer.getBindings().keySet()) {
				if (builtinName.startsWith("__builtin__."))
					builtinName = builtinName.substring(12); // without the "__builtin__." prefix
				if (builtinName.startsWith(name)) completions.add(builtinName);
			}
			scope = scope.parent;
		}
		return completions;
	}
	
	/**
	 * Return a table of names vs classnames, sorted by name. The classname is
	 * non-null only for names bound to a class (e.g. an imported Java class), whose
	 * constructors can then be offered; it is null for variables, functions and
	 * python builtins. Names in inner scopes shadow those of outer scopes.
	 * 
	 * @param name
	 * @return
	 */
	public Map<String, String> findStartsWith2(final String name) {
		final Map<String, String> completions = new TreeMap<>();
		Scope scope = this;
		while (null != scope) {
			for (final Map.Entry<String, DotAutocompletions> e: scope.vars.entrySet()) {
				if (e.getKey().startsWith(name)) completions.putIfAbsent(e.getKey(), classnameOf(e.getValue()));
			}
			for (final Map.Entry<String, DotAutocompletions> e: scope.imports.entrySet()) {
				if (e.getKey().startsWith(name)) completions.putIfAbsent(e.getKey(), classnameOf(e.getValue()));
			}
			scope = scope.parent;
		}
		for (String builtinName: indexer.getBindings().keySet()) {
			if (builtinName.startsWith("__builtin__."))
				builtinName = builtinName.substring(12); // without the "__builtin__." prefix
			// Skip members of builtin types, e.g. "tuple.__len__"
			if (builtinName.indexOf('.') >= 0) continue;
			if (builtinName.startsWith(name)) completions.putIfAbsent(builtinName, null);
		}
		return completions;
	}

	/** The class a name refers to, if it names a class rather than an instance. */
	private static String classnameOf(final DotAutocompletions da) {
		return da instanceof StaticDotAutocompletions ? da.getClassname() : null;
	}
	
	/** Find vars by type, recursively upstream the nested scopes, listing first those of the innermost scope.
	 *
	 * @param clazz
	 */
	public Stream<String> findVarsByType(final String type, final Class<?> clazz) {
		Stream<String> varNames = new ArrayList<String>().stream();
		Scope scope = this;
		while (null != scope) {
			varNames = Stream.concat(varNames,
					scope.vars.entrySet().stream()
						.filter(e -> {
							try {
								if (e.getKey().startsWith("____")) return false; // injected variables in JythonAutoCompletions.completionsFor
								final String classname = e.getValue().getClassname();
								if (null == classname) return false;
								if (type.equals(classname)) {
									return true;
								}
								// Handle compatible numeric arguments
								if (Number.class.isAssignableFrom(clazz)) {
									// Python only has long or float
									if (classname.equals("float")) {
										return true; // all numeric types will fit
									}
									if (classname.equals("long")) {
										// or "long", but that was exact-matched earlier
										return type.equals("int")
												|| type.equals("short")
												|| type.equals("byte")
												|| clazz.isAssignableFrom(Long.class)
												|| clazz.isAssignableFrom(Integer.class)
												|| clazz.isAssignableFrom(Short.class)
												|| clazz.isAssignableFrom(Byte.class);
									}
								}
								// Fix class when it's a primitive number
								Class<?> c = null;
								switch (classname) {
								case "long": c = Long.class; break;
								case "float": c = Float.class; break;
								default: c = Class.forName(classname);
								}
								// Search for subclass or interface
								return clazz.isAssignableFrom(c);
								
								// TODO: search for methods of non-matching classes that return a matching type
								
							} catch (ClassNotFoundException e1) {
								// NB: Nothing to complete from it.
							}
							return false;
						})
						.map(e -> e.getKey()));
			scope = scope.parent;
		}
		return varNames;
	}
	
	
	public HashMap<String, DotAutocompletions> getImports() {
		final HashMap<String, DotAutocompletions> imports = new HashMap<>(this.imports);
		Scope parent = this.parent;
		while (parent != null) {
			imports.putAll(parent.getImports());
			parent = parent.parent;
		}
		return imports;
	}
	
	public HashMap<String, DotAutocompletions> getVars() {
		final HashMap<String, DotAutocompletions> vars = new HashMap<>(this.vars);
		Scope parent = this.parent;
		while (parent != null) {
			vars.putAll(parent.getVars());
			parent = parent.parent;
		}
		return vars;
	}
	
	public boolean isClass() {
		return this.className != null;
	}
	
	public Scope getLast() {
		if (children.isEmpty()) return this;
		return children.get(children.size() -1).getLast();
	}
	
	public void print(final String indent) {
		if ("" == indent) {
			System.out.println("scope global:");
		}
		System.out.println(indent + "available imports: " + String.join(", ", this.getImports().keySet()));
		System.out.println(indent + "declared imports:");
		for (final Map.Entry<String, DotAutocompletions> e: imports.entrySet())
			System.out.println(indent + "  import :: " + e.getKey() + " --> " + e.getValue());
		System.out.println(indent + "available vars: " + String.join(", ", this.getVars().keySet()));
		System.out.println(indent + "declared vars:");
		for (final Map.Entry<String, DotAutocompletions> e: vars.entrySet()) {
			System.out.println(indent + "  var :: " + e.getKey() + " = " + e.getValue());
		}
		
		int i = 0;
		for (final Scope child: children) {
			System.out.println(indent + "scope[" + (i++) + "]:");
			child.print(indent + "  ");
		}
	}
}
