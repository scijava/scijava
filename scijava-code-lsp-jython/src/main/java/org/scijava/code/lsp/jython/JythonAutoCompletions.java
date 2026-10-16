/*-
 * #%L
 * Jython code intelligence for SciJava, via Jython's own parser.
 * %%
 * Copyright (C) 2020 - 2025 SciJava developers.
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
import java.lang.reflect.Parameter;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.python.indexer.types.NModuleType;
import org.scijava.code.lsp.jvm.ClassIndex;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.TextEdit;
import org.scijava.code.lsp.jvm.Callable;

/**
 * The Jython completion engine: given the code before the caret it analyzes the
 * Jython AST (via {@link JythonScriptParser}) and produces completions (LSP
 * {@link CompletionItem}s; methods and constructors as {@link Callable}s),
 * plus the scope's variables by type, for arguments. Class-name and import
 * completions (including auto-import {@link TextEdit}s) are produced via
 * {@link ClassIndex}.
 *
 * @author Albert Cardona
 * @author Curtis Rueden
 */
public class JythonAutoCompletions {

	private static final Pattern
		nameToken = Pattern.compile("^(|.*?[ \\t,\\[=\\(+\\-*/%<>!:]+)([a-zA-Z_][a-zA-Z0-9_]*)$"),
		invocation = Pattern.compile("^(.*?[ \\t]+|)([a-zA-Z_][a-zA-Z0-9_]+\\()$"),
		dotNameToken = Pattern.compile("^(.*?[ \\t]+|)([a-zA-Z0-9_\\.\\[\\](){}]+)\\.([a-zA-Z0-9_]*)$"),
		assign = Pattern.compile("^([ \\t]*)(([a-zA-Z_][a-zA-Z0-9_ \\t,]*)[ \\t]+=[ \\t]+(.*))$"),
		endingCode = Pattern.compile("^([ \\t]*)[^#]*?(.*?)[ \\t]*:[ \\t]*(#.*|)[\\n]*$"),
		sysPathAppend = Pattern.compile("sys.path.append[ \\t]*[(][ \\t]*['\"](.*?)['\"][ \\t]*[)]"),
		importPkg = Pattern.compile("^(import|from)[ \\t]+([a-zA-Z_][a-zA-Z0-9._]*)$"),
		importMember = Pattern.compile("^from[ \\t]+([a-z_][a-zA-Z0-9_.]*)[ \\t]+import[ \\t]*([a-zA-Z0-9_]*)$"),
		// Import/class-name discovery patterns (matched against already-typed text).
		fromImport = Pattern.compile("^((from|import)[ \\t]+)([a-zA-Z_][a-zA-Z0-9._]*)$"),
		fastImport = Pattern.compile("^(from[ \\t]+)([a-zA-Z_][a-zA-Z0-9._]*)[ \\t]+$"),
		importStatement = Pattern.compile("^((from[ \\t]+([a-zA-Z0-9._]+)[ \\t]+|[ \\t]*)import[ \\t]+)([a-zA-Z0-9_., \\t]*)$"),
		simpleClassName = Pattern.compile("^(.*[ \\t]+|)([A-Z_][a-zA-Z0-9_]+)$");

	/** Python 2 (Jython) keywords. */
	private static final List<String> KEYWORDS = Arrays.asList("and", "as",
		"assert", "break", "class", "continue", "def", "del", "elif", "else",
		"except", "exec", "finally", "for", "from", "global", "if", "import", "in",
		"is", "lambda", "not", "or", "pass", "print", "raise", "return", "try",
		"while", "with", "yield", "None", "True", "False");

	private final JythonImportFormat formatter = new JythonImportFormat();

	/** Python standard library module names, discovered from the jython jar. */
	private static final List<String> jython_jar_modules = findJythonModules();

	/**
	 * Result of a completion query: the suggestions, plus the analyzed scope's
	 * variables by type, and argument type resolution.
	 */
	public static final class Result {

		public final List<CompletionItem> completions;
		/** The variables of a type (e.g. {@code double}) in scope; or null. */
		public final Function<String, List<String>> variablesOfType;
		/** The type (name) of an expression in the analyzed scope, or null. */
		public final Function<String, String> types;

		Result(final List<CompletionItem> completions,
			final Function<String, List<String>> variablesOfType,
			final Function<String, String> types)
		{
			this.completions = completions;
			this.variablesOfType = variablesOfType;
			this.types = types;
		}
	}

	/**
	 * Computes completions for a caret positioned at the end of {@code lastLine},
	 * which follows {@code codeWithoutLastLine} in the script. {@code
	 * alreadyEnteredText} is the text the editor will replace.
	 */
	public Result completionsFor(final String codeWithoutLastLine,
		final String lastLine, final String alreadyEnteredText)
	{
		return completionsFor(codeWithoutLastLine, lastLine, alreadyEnteredText,
			Collections.emptyMap());
	}

	/**
	 * As {@link #completionsFor(String, String, String)}, for a script in which
	 * the given variables are already defined before the code runs (e.g. script
	 * parameters, or the bindings of a live interpreter).
	 */
	public Result completionsFor(final String codeWithoutLastLine,
		final String lastLine, final String alreadyEnteredText,
		final Map<String, DotAutocompletions> predefined)
	{
		final List<CompletionItem> completions = new ArrayList<>();
		final int crop = lastLine.length() - alreadyEnteredText.length();

		// 1) AST-based completions (names, members, constructors, invocations).
		final Scope scope = astCompletions(codeWithoutLastLine, lastLine, crop,
			predefined, completions);

		// 2) Import and class-name discovery, always appended.
		importCompletions(alreadyEnteredText, codeWithoutLastLine + lastLine,
			completions);

		if (scope == null) return new Result(dedupe(completions), null, null);
		return new Result(dedupe(completions), type -> variablesOfType(scope, type),
			expression -> JythonScriptParser.typeOf(expression, scope));
	}

	// -- AST-based completions --

	/** Adds AST-based completions, returning the analyzed scope (or null). */
	private Scope astCompletions(String codeWithoutLastLine,
		final String lastLine, final int crop,
		final Map<String, DotAutocompletions> predefined,
		final List<CompletionItem> completions)
	{
		// Precondition: can't expand when empty or ending with any of "[]{},; ".
		if (lastLine.isEmpty()) return null;
		final char lastChar = lastLine.charAt(lastLine.length() - 1);
		if ("[]{},; ".indexOf(lastChar) > -1) return null;

		// If the prior line ends with ':', append a "pass" to make valid code.
		boolean add_pass = false;
		if (codeWithoutLastLine.endsWith("\n")) {
			final int priorLineBreak = codeWithoutLastLine.lastIndexOf('\n',
				codeWithoutLastLine.length() - 2);
			final String endingLine = codeWithoutLastLine.substring(priorLineBreak +
				1);
			final Matcher me = endingCode.matcher(endingLine);
			if (me.find()) {
				codeWithoutLastLine = codeWithoutLastLine.substring(0, priorLineBreak +
					1) + me.group(1) + me.group(2);
				add_pass = true;
			}
		}

		// Register any sys.path.append paths so custom modules can be found.
		try {
			final Matcher mpath = sysPathAppend.matcher(codeWithoutLastLine);
			while (mpath.find()) {
				final File path = new File(mpath.group(1));
				if (path.exists() && path.isDirectory() && !Scope.indexer
					.getLoadPath().stream().anyMatch(s -> path.equals(new File(s))))
				{
					Scope.indexer.addPath(path.getAbsolutePath());
				}
			}
		}
		catch (final Exception e) {
			JythonDev.print("Failed to add path from sys.path.append expression.", e);
		}

		// A python module import: "import x" or "from x".
		final Matcher mi = importPkg.matcher(lastLine);
		if (mi.find()) {
			moduleNameCompletions(mi.group(1), mi.group(2), completions);
			return null;
		}

		// A module member: "from x import y".
		final Matcher mm = importMember.matcher(lastLine);
		if (mm.find()) {
			moduleMemberCompletions(mm.group(1), mm.group(2) == null ? "" : mm.group(
				2), completions);
			return null;
		}

		// A plain name (variable, function, class, keyword) being typed.
		final Matcher m1 = nameToken.matcher(lastLine);
		if (m1.find()) {
			final Scope scope = JythonScriptParser.parseAST(codeWithoutLastLine,
				predefined)
				.getLast();
			final String seed = m1.group(2);
			final String pre = crop > -1 && crop < m1.group(1).length() ? m1.group(1)
				.substring(crop) : "";
			final String head = lastLine.substring(0, lastLine.length() - seed
				.length());
			final Map<String, String> names = scope.findStartsWith2(seed);
			for (final Map.Entry<String, String> e : names.entrySet()) {
				completions.add(item((head + e.getKey()).substring(crop),
					CompletionItemKind.Variable, null));
				final String classname = e.getValue();
				if (null != classname) {
					// A class: also offer its constructors.
					try {
						final Class<?> c = Class.forName(classname);
						for (final java.lang.reflect.Constructor<?> cons : c
							.getConstructors())
						{
							completions.add(makeDotCompletion(pre, seed,
								new CompletionText(e.getKey(), c, cons)));
						}
					}
					catch (final ClassNotFoundException | LinkageError exc) {
						JythonDev.printTrace("Can't load class: " + classname);
					}
				}
			}
			for (final String keyword : KEYWORDS) {
				if (keyword.startsWith(seed) && !keyword.equals(seed)) {
					completions.add(item((head + keyword).substring(crop),
						CompletionItemKind.Keyword, null));
				}
			}
			return scope;
		}

		// A function/constructor invocation: "name(".
		final Matcher m1c = invocation.matcher(lastLine);
		if (m1c.find()) {
			final String name = m1c.group(2).substring(0, m1c.group(2).length() - 1);
			final Scope scope = JythonScriptParser.parseAST(codeWithoutLastLine,
				predefined)
				.getLast();
			final DotAutocompletions da = scope.find(name, DotAutocompletions.EMPTY);
			if (da instanceof ConstructorAutocompletions) {
				for (final CompletionText ct : da.get()) {
					completions.add(makeDotCompletion("", name, ct));
				}
				return scope;
			}
			// Otherwise fall through to dot-name handling below.
		}

		// A field/method expansion: "var.seed" or "expr().seed".
		final Matcher m2 = dotNameToken.matcher(lastLine);
		if (m2.find()) {
			final String seed = m2.group(3); // can be empty
			final String code, varName;
			final Matcher m3 = assign.matcher(lastLine);
			if (m3.find()) {
				final String[] assignment = lastLine.split("=");
				final String[] names = assignment[0].split(",");
				varName = names[names.length - 1].trim();
				code = codeWithoutLastLine + lastLine.substring(0, lastLine.length() -
					1 - seed.length());
			}
			else {
				int start = 0;
				while (Character.isWhitespace(lastLine.charAt(start++)));
				--start;
				String suffix = "";
				if (add_pass) {
					add_pass = false;
					suffix = ":\n  ";
				}
				varName = "____GRAB____";
				code = codeWithoutLastLine + (add_pass ? ": pass" : "") + suffix +
					lastLine.substring(0, start) + varName + " = " + lastLine.substring(
						start, lastLine.length() - 1 - seed.length());
			}
			final Scope scope = JythonScriptParser.parseAST(code, predefined);
			final DotAutocompletions da = scope.getLast().find(varName,
				DotAutocompletions.EMPTY);
			final String fullPre = lastLine.substring(crop);
			final String pre = fullPre.substring(0, fullPre.lastIndexOf(seed));
			final String lowerCaseSeed = seed.toLowerCase();

			final List<CompletionItem> list = da.get().stream().filter(s -> s
				.getReplacementText().toLowerCase().contains(lowerCaseSeed)).map(
					s -> makeDotCompletion(pre, lowerCaseSeed, s)).collect(Collectors
						.toList());
			sortCompletions(list, seed);
			completions.addAll(list);
			return scope.getLast();
		}

		return null;
	}

	private void moduleNameCompletions(final String first, final String pkgName,
		final List<CompletionItem> completions)
	{
		jython_jar_modules.stream().filter(s -> s.startsWith(pkgName)).forEach(s -> //
		completions.add(documented(item(first + " " + s + (first.equals("from")
			? " import " : ""), CompletionItemKind.Module, null),
			"Python standard library module")));
		final String pkgNameFile = pkgName.replace('.', '/');
		for (final String dir : Scope.indexer.getLoadPath()) {
			try {
				Files.walk(new File(dir).toPath(), FileVisitOption.FOLLOW_LINKS) //
					.map(path -> path.toFile().getAbsolutePath()) //
					.filter(s -> s.startsWith(dir + pkgNameFile) && s.endsWith(".py")) //
					.map(s -> (s.endsWith("__init__.py") //
						? s.substring(dir.length(), s.length() - 12) //
						: s.substring(dir.length(), s.length() - 3)).replace('/', '.')) //
					.forEach(s -> completions.add(documented(item(first + " " + s +
						(first.equals("from") ? " import " : ""), CompletionItemKind.Module,
						null), "Custom python module")));
			}
			catch (final Exception e) {
				JythonDev.print("Failed to read jython module file.", e);
			}
		}
	}

	private void moduleMemberCompletions(final String pkgName,
		final String member, final List<CompletionItem> completions)
	{
		final NModuleType mod = Scope.loadPythonModule(pkgName);
		if (null != mod && !mod.getTable().keySet().isEmpty()) {
			mod.getTable().keySet().stream().filter(s -> s.startsWith(member)).forEach(
				s -> completions.add(item("from " + pkgName + " import " + s,
					CompletionItemKind.Module, null)));
			return;
		}
		if (null != mod) {
			// Module exists but its __init__.py is empty: look into its folder.
			for (final String dir : Scope.indexer.getLoadPath()) {
				final File fdir = new File(dir + pkgName.replace('.', '/'));
				if (fdir.exists() && fdir.isDirectory()) {
					final String[] names = fdir.list();
					if (names == null) continue;
					for (final String filename : names) {
						if (filename.startsWith(member) && (new File(fdir.getAbsolutePath() +
							"/" + filename).isDirectory() || filename.endsWith(".py")))
						{
							completions.add(item("from " + pkgName + " import " + (filename
								.endsWith(".py") ? filename.substring(0, filename.length() - 3)
									: filename), CompletionItemKind.Module, null));
						}
					}
				}
			}
		}
	}

	// -- Import / class-name discovery (with auto-import edits) --

	private void importCompletions(final String text, final String fullCode,
		final List<CompletionItem> completions)
	{
		if (!ClassIndex.isCacheReady()) return; // don't block

		final Matcher m1 = fromImport.matcher(text);
		if (m1.find()) {
			ClassIndex.findClassNamesContaining(m1.group(3)).map(
				formatter::singleToImportStatement).forEach(s -> completions.add(
					item(s, CompletionItemKind.Module, null)));
			return;
		}
		final Matcher m1f = fastImport.matcher(text);
		if (m1f.find()) {
			ClassIndex.findClassNamesForPackage(m1f.group(2)).map(
				formatter::singleToImportStatement).forEach(s -> completions.add(
					item(s, CompletionItemKind.Module, null)));
			return;
		}
		final Matcher m2 = importStatement.matcher(text);
		if (m2.find()) {
			final String packageName = m2.group(3);
			String className = m2.group(4);
			final String[] bycomma = className.split(",");
			String precomma = "";
			if (bycomma.length > 1) {
				className = bycomma[bycomma.length - 1].trim();
				for (int i = 0; i < bycomma.length - 1; ++i)
					precomma += bycomma[0] + ", ";
			}
			final java.util.stream.Stream<String> stream;
			if (className.length() > 0) stream = ClassIndex.findClassNamesStartingWith(
				null == packageName ? className : packageName + "." + className);
			else stream = ClassIndex.findClassNamesForPackage(packageName);
			final String pre = m2.group(1) + precomma;
			stream.map(s -> s.substring(Math.max(0, s.lastIndexOf('.') + 1))).forEach(
				s -> completions.add(item(pre + s, CompletionItemKind.Module, null)));
			return;
		}
		final Matcher m3 = simpleClassName.matcher(text);
		if (m3.find()) {
			final String pre = m3.group(1);
			final List<String> classNames = ClassIndex
				.findSimpleClassNamesStartingWith(m3.group(2));
			// Closest matches first: shortest simple name, then alphabetical.
			classNames.sort(Comparator.comparingInt((String cn) -> cn.length() - cn
				.lastIndexOf('.')).thenComparing(Comparator.naturalOrder()));
			for (final String className : classNames) {
				final String simpleName = className.substring(className.lastIndexOf(
					'.') + 1);
				final String importStmt = formatter.singleToImportStatement(className);
				// Side effect on accept: insert the import near the top of the file.
				final TextEdit edit = JythonImports.autoImportEdit(fullCode, className,
					importStmt);
				final CompletionItem c = item(pre + simpleName, CompletionItemKind.Class,
					importStmt);
				if (edit != null) c.setAdditionalTextEdits(Collections.singletonList(
					edit));
				completions.add(c);
			}
		}
	}

	// -- Helpers --

	/**
	 * Removes duplicate completions (same text and parameters), keeping the first
	 * occurrence, since the AST and class-name analyses can suggest the same thing.
	 */
	private static List<CompletionItem> dedupe(
		final List<CompletionItem> completions)
	{
		final java.util.Set<String> seen = new java.util.HashSet<>();
		final List<CompletionItem> result = new ArrayList<>(completions.size());
		for (final CompletionItem c : completions) {
			final String params = c.getLabelDetails() == null ? null : c
				.getLabelDetails().getDetail();
			if (seen.add(c.getLabel() + "\u0000" + params)) result.add(c);
		}
		return result;
	}

	/** A completion of the given text. */
	private static CompletionItem item(final String text,
		final CompletionItemKind kind, final String detail)
	{
		final CompletionItem item = new CompletionItem(text);
		item.setKind(kind);
		item.setInsertText(text);
		item.setDetail(detail);
		return item;
	}

	private static CompletionItem documented(final CompletionItem item,
		final String doc)
	{
		item.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN, doc));
		return item;
	}

	/** Builds a completion from a {@link CompletionText} and prefix. */
	private static CompletionItem makeDotCompletion(final String pre,
		final String seed, final CompletionText ct)
	{
		final List<Parameter> ps = ct.getMethodArgs();
		if (null != ps && null != ct.getReturnType()) {
			String text = ct.getReplacementText();
			if (text.endsWith("()")) text = text.substring(0, text.length() - 2);
			final List<Callable.Param> params = new ArrayList<>();
			for (final Parameter p : ps) {
				params.add(new Callable.Param(p.getName(), p.getType()
					.getCanonicalName()));
			}
			return new Callable(pre + text, params, ct.getReturnType()).detail(ct
				.getSummary()).toItem();
		}
		return item(pre + ct.getReplacementText(), CompletionItemKind.Field, ct
			.getSummary());
	}

	/** The variables of the given type in scope, innermost first. */
	private static List<String> variablesOfType(final Scope scope,
		final String type)
	{
		{
			if (type == null) return Collections.emptyList();
			Class<?> clazz;
			switch (type) {
				case "int": clazz = Integer.class; break;
				case "long": clazz = Long.class; break;
				case "float": clazz = Float.class; break;
				case "double": clazz = Double.class; break;
				case "boolean": clazz = Boolean.class; break;
				case "char": clazz = Character.class; break;
				case "short": clazz = Short.class; break;
				case "byte": clazz = Byte.class; break;
				default:
					try {
						clazz = Class.forName(type);
					}
					catch (final Throwable t) {
						return Collections.emptyList();
					}
			}
			final Class<?> c = clazz;
			return scope.findVarsByType(type, c).collect(Collectors.toList());
		}
	}

	private void sortCompletions(final List<CompletionItem> completions,
		final String pre)
	{
		completions.sort(new Comparator<CompletionItem>() {

			@Override
			public int compare(final CompletionItem o1, final CompletionItem o2) {
				final int p1 = o1.getLabel().startsWith(pre) ? 0 : Integer.MAX_VALUE;
				final int p2 = o2.getLabel().startsWith(pre) ? 0 : Integer.MAX_VALUE;
				if (p1 == p2) return o1.getLabel().compareTo(o2.getLabel());
				return p1 - p2;
			}
		});
	}

	/** Locates the jython jar on the classpath and lists its Lib/*.py modules. */
	private static List<String> findJythonModules() {
		try {
			final java.security.CodeSource cs = org.python.core.PySystemState.class
				.getProtectionDomain().getCodeSource();
			if (cs == null || cs.getLocation() == null) return Collections.emptyList();
			final File jar = new File(cs.getLocation().toURI());
			if (!jar.isFile()) return Collections.emptyList();
			try (final JarFile jf = new JarFile(jar)) {
				return jf.stream().map(JarEntry::getName).filter(s -> s.startsWith(
					"Lib/") && s.endsWith(".py")).map(s -> (s.endsWith("/__init__.py") //
						? s.substring(4, s.length() - 12) //
						: s.substring(4, s.length() - 3)).replace('/', '.')).collect(
							Collectors.toList());
			}
		}
		catch (final Exception e) {
			JythonDev.print("Cannot locate the jython jar for stdlib modules", e);
			return Collections.emptyList();
		}
	}
}
