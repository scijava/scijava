/*-
 * #%L
 * Groovy code intelligence for SciJava, via Groovy's own compiler.
 * %%
 * Copyright (C) 2026 SciJava developers.
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

package org.scijava.code.lsp.groovy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import groovy.lang.GroovySystem;
import groovy.lang.MetaMethod;
import groovyjarjarantlr4.v4.runtime.Vocabulary;

import org.apache.groovy.parser.antlr4.GroovyLexer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.control.ResolveVisitor;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionItemLabelDetails;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.TextEdit;
import org.scijava.code.lsp.jvm.Callable;
import org.scijava.code.lsp.jvm.ClassIndex;
import org.scijava.code.lsp.Environment;
import org.scijava.code.lsp.jvm.Members;
import org.scijava.code.lsp.Positions;
import org.scijava.code.lsp.jvm.AbstractLanguageServer;
import org.scijava.code.lsp.jvm.Signatures;
import org.scijava.util.Types;

/**
 * An in-process language server for Groovy, driven by Groovy's own compiler
 * (see {@link TypedAST}).
 *
 * @author Gabriel Selzer
 */
public class GroovyLanguageServer extends AbstractLanguageServer {

	private static final Set<String> KEYWORDS = keywords();

	/** Packages Groovy imports by default, e.g. "java.util." */
	private static final List<String> DEFAULT_IMPORTS = Arrays.asList(
		ResolveVisitor.DEFAULT_IMPORTS);

	/** Upper bound on class-name suggestions, which can be very numerous. */
	private static final int MAX_CLASSES = 500;

	/** An import statement being typed: "import a.b.C". */
	private static final Pattern IMPORT = Pattern.compile(
		"^\\s*import\\s+(static\\s+)?([\\w$.]*)$");

	/** The identifier (possibly empty) being typed at the caret. */
	private static final Pattern IDENTIFIER = Pattern.compile("[\\w$]*$");

	/**
	 * A number literal followed by a dot, e.g. "1."
	 * (There's nothing to autocomplete when typing out a number)
	 */
	private static final Pattern NUMBER_DOT = Pattern.compile(
		"(?<![\\w$])\\d+\\.\\s*$");

	/** A package name followed by a dot, e.g. "java.util." */
	private static final Pattern QUALIFIER = Pattern.compile(
		"((?:[a-z_][\\w$]*\\.)+)\\s*$");

	static {
		// Warm the class index in the background, as completion needs it.
		new Thread(ClassIndex::ensureCache, "Groovy-ClassIndex-warmup").start();
	}

	/**
	 * @param environment Where the scripts run (e.g. a live interpreter).
	 */
	public GroovyLanguageServer(final Environment environment) {
		super(environment);
	}


	@Override
	protected CompletionList complete(final Document doc, final int caret) {
		final String text = doc.text();
		final String line = doc.lineTo(caret);
		final List<CompletionItem> none = new ArrayList<>();

		final Matcher imp = IMPORT.matcher(line);
		if (imp.find()) {
			if (imp.group(1) != null) return list(doc, caret, caret, none);
			final String token = imp.group(2);
			return list(doc, caret - token.length(), caret, importCompletions(
				token));
		}

		final Matcher id = IDENTIFIER.matcher(line);
		id.find();
		final String prefix = id.group();
		final int replaceStart = caret - prefix.length();
		final String before = line.substring(0, line.length() - prefix.length());
		final boolean decimal = NUMBER_DOT.matcher(before).find() && (prefix
			.isEmpty() || Character.isDigit(prefix.charAt(0)));
		final boolean member = before.trim().endsWith(".") && !before.trim()
			.endsWith("..") && !decimal;
		if (!member && prefix.isEmpty()) {
			return list(doc, caret, caret, argumentChoices(doc, caret, line));
		}
		if (!member && Character.isDigit(prefix.charAt(0))) {
			return list(doc, caret, caret, none);
		}

		final TypedAST ast = typedAST(text.substring(0, replaceStart), line
			.substring(0, line.length() - prefix.length()));
		if (ast == null) return list(doc, caret, caret, none); // string, comment

		final List<CompletionItem> items = new ArrayList<>();
		if (member) items.addAll(memberCompletions(ast, before, prefix));
		else {
			items.addAll(variableCompletions(ast.variablesAtCaret(), prefix));
			items.addAll(keywordCompletions(prefix));
			items.addAll(classCompletions(doc, prefix));
		}
		return list(doc, replaceStart, caret, items);
	}

	@Override
	protected SignatureHelp signatureHelp(final Document doc, final int caret) {
		final String line = doc.lineTo(caret);
		final CallContext call = callContext(doc, caret, line);
		if (call == null) return null;
		final List<Callable> callables = callables(call.ast, call.call);
		if (callables.isEmpty()) return null;
		// NB: Reflection lists methods in no particular order: sort them by
		// their parameter types, so that equally good fits come out the same.
		callables.sort(Comparator.comparing(Callable::parameterTypes));

		// The types of the arguments before the caret's (which is being typed).
		final List<String> argTypes = new ArrayList<>();
		for (int i = 0; i < call.call.arguments.size(); i++) {
			argTypes.add(i == call.call.active ? null : call.ast.typeName(
				call.call.arguments.get(i)));
		}
		final ClassLoader loader = loader();
		return new SignatureHelp(Signatures.rateAll(callables, argTypes, (a,
			p) -> Signatures.fit(a, p, loader)), 0, call.call.active);
	}

	/**
	 * Finds the problems Groovy's compiler and type checker would report
	 * that also trouble the script when it runs (see
	 * {@link TypedAST#problems()}).
	 */
	@Override
	protected List<Diagnostic> diagnose(final Document doc) {
		final String text = doc.text();
		final TypedAST ast = TypedAST.of(text, loader());
		final List<Diagnostic> found = new ArrayList<>();
		for (final TypedAST.Problem p : ast.problems()) {
			final int start = offset(text, p.line, p.column);
			int end = offset(text, p.endLine, p.endColumn);
			// NB: At least one character, on the line where it starts.
			final int lineEnd = text.indexOf('\n', start);
			if (end <= start) end = Math.min(start + 1, text.length());
			if (lineEnd >= 0 && end > lineEnd) end = Math.max(lineEnd, start);
			found.add(diagnostic(doc, start, end, p.warning
				? DiagnosticSeverity.Warning : DiagnosticSeverity.Error, p.message,
				"groovy"));
		}
		return found;
	}

	// -- Helper methods --

	/** A call around the caret, as analyzed. */
	private static final class CallContext {

		private final TypedAST ast;
		private final TypedAST.Call call;

		private CallContext(final TypedAST ast,
			final TypedAST.Call call)
		{
			this.ast = ast;
			this.call = call;
		}
	}

	/** Analyzes the call around the caret, or returns null if none. */
	private CallContext callContext(final Document doc, final int caret,
		final String line)
	{
		final String text = doc.text();
		// NB: The argument being typed, if a name, is replaced by the marker.
		final Matcher id = IDENTIFIER.matcher(line);
		id.find();
		final String prefix = id.group();
		final int start = caret - prefix.length();
		final TypedAST ast = typedAST(text.substring(0, start), line.substring(0,
			line.length() - prefix.length()));
		if (ast == null) return null; // string or comment
		final TypedAST.Call call = ast.callAtCaret();
		return call == null ? null : new CallContext(ast, call);
	}

	/**
	 * In a call's arguments, with nothing typed yet: the variables in scope,
	 * those fitting the parameter first (as preselected).
	 */
	private List<CompletionItem> argumentChoices(final Document doc,
		final int caret, final String line)
	{
		if (Signatures.callStart(doc.text(), caret) < 0) return new ArrayList<>();
		final CallContext call = callContext(doc, caret, line);
		if (call == null) return new ArrayList<>();
		final Set<Class<?>> targets = new HashSet<>();
		for (final Callable c : callables(call.ast, call.call)) {
			if (call.call.active >= c.params().size()) continue;
			final String type = c.params().get(call.call.active).type();
			final Class<?> t = type == null ? null : Types.box(TypedAST.load(
				type, loader()));
			if (t != null && t != Object.class) targets.add(t);
		}
		final Map<String, Class<?>> vars = new LinkedHashMap<>();
		final Map<String, ClassNode> local = call.ast.variablesAtCaret();
		final List<String> order = new ArrayList<>(local.keySet());
		Collections.reverse(order);
		for (final String name : order) {
			vars.put(name, Types.box(call.ast.toClass(local.get(name))));
		}
		final List<CompletionItem> fitting = new ArrayList<>();
		final List<CompletionItem> others = new ArrayList<>();
		for (final Map.Entry<String, Class<?>> v : vars.entrySet()) {
			final Class<?> c = Types.box(v.getValue());
			final boolean fits = c != null && targets.stream().anyMatch(t -> t
				.isAssignableFrom(c));
			final CompletionItem item = variable(v.getKey(), c == null ? null
				: typeName(c));
			if (fits) {
				item.setPreselect(fitting.isEmpty());
				fitting.add(item);
			}
			else others.add(item);
		}
		fitting.addAll(others);
		return fitting;
	}

	/**
	 * Builds the typed syntax tree of the script up to the caret. If it does not
	 * parse (e.g. due to a syntax error above the caret), falls back to the
	 * caret's line alone.
	 *
	 * @return The tree, or null if the caret is in a string or comment.
	 */
	private TypedAST typedAST(final String textBefore,
		final String lineBefore)
	{
		final String code = TypedAST.closeBrackets(TypedAST.markCaret(
			textBefore));
		if (code == null) return null;
		final TypedAST ast = TypedAST.of(code, loader());
		if (ast.isParsed()) return ast;
		final String lineCode = TypedAST.closeBrackets(TypedAST.markCaret(
			lineBefore));
		return lineCode == null ? ast : TypedAST.of(lineCode, loader());
	}

	private List<CompletionItem> memberCompletions(
		final TypedAST ast, final String before, final String prefix)
	{
		final Expression receiver = ast.receiverAtCaret();
		final ClassNode type = TypedAST.typeOf(receiver);
		if (type == null) {
			// Perhaps a package name, e.g. "java.util."
			final Matcher q = QUALIFIER.matcher(before);
			if (q.find()) return packageMemberCompletions(q.group(1), prefix);
			return new ArrayList<>();
		}
		final boolean staticOnly = receiver instanceof ClassExpression;
		if (type.redirect().isPrimaryClassNode()) {
			return scriptClassMembers(ast, type.redirect(), prefix, staticOnly);
		}
		final Class<?> c = Types.box(ast.toClass(type));
		if (c == null) return new ArrayList<>();
		final List<CompletionItem> out = sorted(Members.of(c, prefix,
			staticOnly));
		if (!staticOnly) {
			for (final Callable m : gdkMethods(c, prefix)) out.add(m.toItem());
		}
		return out;
	}

	/** Members of a class defined in the script itself. */
	private List<CompletionItem> scriptClassMembers(
		final TypedAST ast, final ClassNode type, final String prefix,
		final boolean staticOnly)
	{
		final List<CompletionItem> out = new ArrayList<>();
		for (final PropertyNode p : type.getProperties()) {
			if (staticOnly && !p.isStatic() || !matches(p.getName(), prefix)) {
				continue;
			}
			out.add(field(p.getName(), p.getType().toString(false)));
		}
		for (final FieldNode f : type.getFields()) {
			if (!f.isPublic() || f.isSynthetic() || staticOnly && !f.isStatic() ||
				!matches(f.getName(), prefix) || type.getProperty(f
					.getName()) != null) continue;
			out.add(field(f.getName(), f.getType().toString(false)));
		}
		for (final Callable m : scriptMethods(type, prefix, staticOnly, false)) {
			out.add(m.toItem());
		}
		final List<CompletionItem> sorted = sorted(out);
		// Plus what it inherits from (compiled) superclasses.
		final Class<?> superclass = ast.toClass(type.getSuperClass());
		if (superclass != null && superclass != Object.class) {
			sorted.addAll(sorted(Members.of(superclass, prefix, staticOnly)));
		}
		return sorted;
	}

	/** Methods of a class defined in the script: by prefix, or exact name. */
	private static List<Callable> scriptMethods(final ClassNode type,
		final String name, final boolean staticOnly, final boolean exact)
	{
		final List<Callable> out = new ArrayList<>();
		for (final MethodNode m : type.getMethods()) {
			if (!m.isPublic() || m.isSynthetic() || staticOnly && !m.isStatic() || m
				.getName().contains("$")) continue;
			if (exact ? !m.getName().equals(name) : !matches(m.getName(), name)) {
				continue;
			}
			out.add(methodNode(m.getName(), m).detail(m.getTypeDescriptor()));
		}
		return out;
	}

	/** Classes and subpackages of a package, e.g. after "java.util." */
	private static List<CompletionItem> packageMemberCompletions(
		final String qualifier, final String prefix)
	{
		if (!ClassIndex.isCacheReady()) return new ArrayList<>();
		final String pkg = qualifier.substring(0, qualifier.length() - 1);
		return ClassIndex.findClassNamesForPackage(pkg) //
			.filter(s -> s.lastIndexOf('.') == pkg.length()) //
			.map(s -> s.substring(pkg.length() + 1)) //
			.filter(s -> matches(s, prefix) && !s.contains("$")) //
			.sorted().limit(MAX_CLASSES) //
			.map(s -> item(s, CompletionItemKind.Class, pkg + "." + s)) //
			.collect(Collectors.toList());
	}

	/**
	 * The GDK methods Groovy adds to {@code c}, e.g. {@code each} and
	 * {@code collect} for collections; by prefix.
	 */
	private static List<Callable> gdkMethods(final Class<?> c,
		final String prefix)
	{
		final Map<String, Callable> out = new LinkedHashMap<>();
		for (final MetaMethod m : GroovySystem.getMetaClassRegistry()
			.getMetaClass(c).getMetaMethods())
		{
			if (m.isStatic() || !matches(m.getName(), prefix)) continue;
			final List<Callable.Param> params = new ArrayList<>();
			final Class<?>[] types = m.getNativeParameterTypes();
			for (int i = 0; i < types.length; i++) {
				params.add(new Callable.Param("arg" + i, typeName(types[i])));
			}
			final String signature = typeName(m.getReturnType()) + " " + m
				.getName() + params.stream().map(Callable.Param::type).collect(
					Collectors.joining(", ", "(", ")"));
			// NB: Groovy lists some methods more than once.
			out.putIfAbsent(signature, new Callable(m.getName(), params, typeName(m
				.getReturnType())).detail("GDK: " + signature));
		}
		final List<Callable> sorted = new ArrayList<>(out.values());
		sorted.sort(Comparator.comparing(m -> m.name().toLowerCase(Locale.ROOT)));
		return sorted;
	}

	private static List<CompletionItem> variableCompletions(
		final Map<String, ClassNode> vars, final String prefix)
	{
		final List<CompletionItem> out = new ArrayList<>();
		// Innermost (most recently declared) variables first.
		final List<String> order = new ArrayList<>(vars.keySet());
		Collections.reverse(order);
		for (final String name : order) {
			if (!matches(name, prefix) || name.equals(prefix)) continue;
			final ClassNode type = vars.get(name);
			out.add(variable(name, type != null ? type.toString(false) : "Object"));
		}
		return out;
	}

	/** Groovy's keywords */
	private static Set<String> keywords() {
		// NB: The lexer literals don't contain primitives - we add those ourselves
		final Set<String> out = new TreeSet<>(Arrays.asList("true", "false",
			"boolean", "byte", "char", "short", "int", "long", "float", "double"));
		final Vocabulary v = GroovyLexer.VOCABULARY;
		for (int i = 0; i <= v.getMaxTokenType(); i++) {
			// NB: The vocabulary has every kind of token: keywords, operators and
			// punctuation, which have fixed text (their literal name, quoted, e.g.
			// "'class'"), plus identifiers, strings and numbers, which don't
			// (null). We want just the literals that are words, i.e. keywords
			// (with hyphens, as in "non-sealed").
			final String literal = v.getLiteralName(i);
			if (literal == null) continue;
			final String word = literal.substring(1, literal.length() - 1);
			if (word.matches("[a-z][a-z-]*")) out.add(word);
		}
		return out;
	}

	private static List<CompletionItem> keywordCompletions(final String prefix) {
		return KEYWORDS.stream().filter(k -> k.startsWith(prefix) && !k.equals(
			prefix)).map(k -> item(k, CompletionItemKind.Keyword, null)).collect(
				Collectors.toList());
	}

	/** Class names starting with the prefix, imported on acceptance. */
	private static List<CompletionItem> classCompletions(final Document doc,
		final String prefix)
	{
		if (!Character.isUpperCase(prefix.charAt(0)) || !ClassIndex
			.isCacheReady()) return new ArrayList<>();
		final List<String> classNames = ClassIndex.findSimpleClassNamesStartingWith(
			prefix);
		// Closest matches first: shortest simple name, then alphabetical.
		classNames.sort(Comparator.comparingInt((String cn) -> cn.length() - cn
			.lastIndexOf('.')).thenComparing(Comparator.naturalOrder()));
		final List<CompletionItem> out = new ArrayList<>();
		for (final String className : classNames) {
			if (className.contains("$")) continue;
			final String simpleName = className.substring(className.lastIndexOf(
				'.') + 1);
			final CompletionItem item = item(simpleName, CompletionItemKind.Class,
				className);
			final int at = importOffset(doc.text(), className);
			if (at >= 0) {
				item.setAdditionalTextEdits(Collections.singletonList(insert(doc, at,
					"import " + className + "\n")));
			}
			out.add(item);
			if (out.size() >= MAX_CLASSES) break;
		}
		return out;
	}

	/**
	 * Where to import the given class: after the script's last import (or at
	 * the top); or -1 if no import is needed.
	 */
	static int importOffset(final String text, final String className) {
		final String pkg = className.substring(0, Math.max(0, className
			.lastIndexOf('.')));
		// NB: Groovy also imports BigDecimal and BigInteger, by name.
		if (DEFAULT_IMPORTS.contains(pkg + ".") || className.equals(
			"java.math.BigDecimal") || className.equals("java.math.BigInteger"))
		{
			return -1;
		}
		final Pattern imports = Pattern.compile("^\\s*import\\s+(?:static\\s+)?" +
			"([\\w$.]+?)(\\.\\*)?\\s*(?:as\\s+\\w+\\s*)?;?\\s*$", Pattern.MULTILINE);
		final Matcher m = imports.matcher(text);
		int insertAt = 0;
		while (m.find()) {
			final String imported = m.group(1);
			if (imported.equals(className) || m.group(2) != null && imported.equals(
				pkg)) return -1;
			insertAt = m.end() + 1;
		}
		if (insertAt == 0 && text.startsWith("#!")) {
			insertAt = text.indexOf('\n') + 1; // NB: After the shebang.
		}
		return Math.min(insertAt, text.length());
	}

	private static List<CompletionItem> importCompletions(final String token) {
		if (token.isEmpty() || !ClassIndex.isCacheReady()) {
			return new ArrayList<>();
		}
		return ClassIndex.findClassNamesStartingWith(token) //
			.filter(s -> !s.contains("$")) //
			.sorted().limit(MAX_CLASSES) //
			.map(s -> item(s, CompletionItemKind.Module, null)) //
			.collect(Collectors.toList());
	}

	/** The signatures the call may be calling: methods or constructors. */
	private List<Callable> callables(final TypedAST ast,
		final TypedAST.Call call)
	{
		final List<Callable> out = new ArrayList<>();
		if (call.expression instanceof ConstructorCallExpression) {
			final ClassNode type = call.expression.getType().redirect();
			final Class<?> c = ast.toClass(type);
			if (c != null) out.addAll(Members.constructors(c));
			else if (type.isPrimaryClassNode()) {
				for (final ConstructorNode ctor : type.getDeclaredConstructors()) {
					out.add(methodNode(type.getNameWithoutPackage(), ctor));
				}
			}
			return out;
		}
		if (call.expression instanceof StaticMethodCallExpression) {
			final StaticMethodCallExpression e =
				(StaticMethodCallExpression) call.expression;
			return Members.methods(ast.toClass(e.getOwnerType()), e.getMethod(),
				true);
		}
		final MethodCallExpression e = (MethodCallExpression) call.expression;
		final String name = e.getMethodAsString();
		if (name == null) return out;
		if (e.isImplicitThis()) {
			// A function defined in the script, or a GDK method such as println.
			for (final MethodNode m : ast.scriptMethods(name)) {
				out.add(methodNode(name, m));
			}
			out.addAll(named(gdkMethods(Object.class, name), name));
			return out;
		}
		final Expression receiver = e.getObjectExpression();
		final ClassNode type = TypedAST.typeOf(receiver);
		if (type == null) return out;
		final boolean staticOnly = receiver instanceof ClassExpression;
		if (type.redirect().isPrimaryClassNode()) {
			return scriptMethods(type.redirect(), name, staticOnly, true);
		}
		final Class<?> c = Types.box(ast.toClass(type));
		if (c == null) return out;
		out.addAll(Members.methods(c, name, staticOnly));
		if (!staticOnly) out.addAll(named(gdkMethods(c, name), name));
		return out;
	}

	/** The callables with exactly the given name. */
	private static List<Callable> named(final List<Callable> callables,
		final String name)
	{
		return callables.stream().filter(c -> c.name().equals(name)).collect(
			Collectors.toList());
	}

	/** A callable defined in the script itself. */
	private static Callable methodNode(final String name, final MethodNode m) {
		final List<Callable.Param> params = new ArrayList<>();
		for (final org.codehaus.groovy.ast.Parameter p : m.getParameters()) {
			params.add(new Callable.Param(p.getName(), p.isDynamicTyped() ? null : p
				.getType().getName()));
		}
		return new Callable(name, params, m.getReturnType().toString(false));
	}

	private static CompletionItem item(final String text,
		final CompletionItemKind kind, final String detail)
	{
		final CompletionItem item = new CompletionItem(text);
		item.setKind(kind);
		item.setInsertText(text);
		item.setDetail(detail);
		return item;
	}

	private static CompletionItem variable(final String name,
		final String type)
	{
		return item(name, CompletionItemKind.Variable, type == null ? name : type +
			" " + name);
	}

	private static CompletionItem field(final String name, final String type) {
		final CompletionItem item = item(name, CompletionItemKind.Field, type +
			" " + name);
		final CompletionItemLabelDetails details = new CompletionItemLabelDetails();
		details.setDescription(type);
		item.setLabelDetails(details);
		return item;
	}

	private static ClassLoader loader() {
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		return loader != null ? loader : GroovyLanguageServer.class
			.getClassLoader();
	}

	/** The offset of a 1-based line and column, as Groovy reports them. */
	private static int offset(final String text, final int line,
		final int column)
	{
		return Positions.offset(text, new Position(Math.max(line - 1, 0), Math
			.max(column - 1, 0)));
	}

	private static boolean matches(final String name, final String prefix) {
		return name.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(
			Locale.ROOT));
	}

	/** Sorts completions by name, case-insensitively (stably). */
	private static List<CompletionItem> sorted(
		final List<CompletionItem> items)
	{
		final List<CompletionItem> out = new ArrayList<>(items);
		out.sort(Comparator.comparing(c -> c.getLabel().toLowerCase(
			Locale.ROOT)));
		return out;
	}

	/**
	 * Gets the name by which Groovy code refers to a class, e.g.
	 * {@code java.util.List} or {@code int[]}; or {@code Object} if it has none
	 * (e.g. an anonymous class).
	 */
	static String typeName(final Class<?> c) {
		final String name = c == null ? null : c.getCanonicalName();
		return name == null ? "Object" : name;
	}

	/** An edit importing a class (for tests). */
	static TextEdit importEdit(final Document doc, final String className) {
		final int at = importOffset(doc.text(), className);
		return at < 0 ? null : insert(doc, at, "import " + className + "\n");
	}
}
