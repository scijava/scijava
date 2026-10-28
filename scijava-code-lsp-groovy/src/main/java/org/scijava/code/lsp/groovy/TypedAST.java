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

import groovy.lang.GroovyClassLoader;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.codehaus.groovy.transform.stc.TypeCheckingExtension;
import org.scijava.util.Types;

/**
 * The typed syntax tree of a Groovy script, up to the caret. Useful for
 * answering LSP-related questions.
 * <p>
 * Built by Groovy's own compiler: the script is
 * parsed and its names resolved, then it is run through the static type
 * checker, which infers the types of its expressions.
 * </p>
 * <p>
 * Scripts being edited are usually incomplete, and usually dynamically typed,
 * so compilation errors are expected and ignored: the tree has whatever types
 * the compiler could work out, and none elsewhere. Only a syntax error leaves
 * no tree; see {@link #isParsed()}.
 * </p>
 * <p>
 * The source <em>actually parsed</em> is the provided source, with the
 * following modifications:
 * <ol>
 * <li>Text after the caret is omitted</li>
 * <li>The identifier {@link #CARET} (see {@link #markCaret}) is inserted
 * 		at the caret position, so that it can be found in the syntax tree
 * </li>
 * <li>Any brackets left open are closed to pass parsing</li>
 * </ol>
 * </p>
 *
 * @author Gabriel Selzer
 */
final class TypedAST {

	/** Identifier marking the caret position in the source. */
	static final String CARET = "__scijava_caret__";

	/** Cache recent trees for each source file. */
	private static final Map<String, TypedAST> CACHE =
		new LinkedHashMap<String, TypedAST>(16, 0.75f, true)
		{

			@Override
			protected boolean removeEldestEntry(
				final Map.Entry<String, TypedAST> eldest)
			{
				return size() > 16;
			}
		};

	private final SourceUnit source;
	private final ModuleNode module;
	private final ClassLoader loader;

	private TypedAST(final SourceUnit source, final ClassLoader loader) {
		this.source = source;
		this.module = source.getAST();
		this.loader = loader;
	}

	// -- Source preparation --

	/**
	 * Marks the caret: the script up to the caret, then {@link #CARET}.
	 *
	 * @param text The script up to the caret, without the partial token being
	 *          completed.
	 */
	static String markCaret(final String text) {
		return text + CARET;
	}

	/**
	 * Closes the brackets left open at the end of {@code text}, appending the
	 * closing ones (e.g. {@code foo(bar.} becomes {@code foo(bar.)}), so that
	 * it parses.
	 *
	 * @return The text, closed; or null if it ends inside a string or comment.
	 */
	static String closeBrackets(final String text) {
		final Deque<Character> open = new ArrayDeque<>();
		int i = 0;
		final int n = text.length();
		while (i < n) {
			final char c = text.charAt(i);
			if (text.startsWith("//", i)) {
				final int end = text.indexOf('\n', i);
				if (end < 0) return null;
				i = end + 1;
			}
			else if (text.startsWith("/*", i)) {
				final int end = text.indexOf("*/", i + 2);
				if (end < 0) return null;
				i = end + 2;
			}
			else if (text.startsWith("'''", i) || text.startsWith("\"\"\"", i)) {
				final int end = text.indexOf(text.substring(i, i + 3), i + 3);
				if (end < 0) return null;
				i = end + 3;
			}
			else if (c == '\'' || c == '"') {
				int j = i + 1;
				while (j < n && text.charAt(j) != c && text.charAt(j) != '\n') {
					if (text.charAt(j) == '\\') j++;
					j++;
				}
				if (j >= n || text.charAt(j) != c) return null;
				i = j + 1;
			}
			else {
				if (c == '(') open.push(')');
				else if (c == '[') open.push(']');
				else if (c == '{') open.push('}');
				else if ((c == ')' || c == ']' || c == '}') && !open.isEmpty() && open
					.peek() == c) open.pop();
				i++;
			}
		}
		final StringBuilder sb = new StringBuilder(text);
		for (final char c : open) {
			sb.append(c == '}' ? "\n}" : String.valueOf(c));
		}
		return sb.toString();
	}

	// -- Building --

	/**
	 * Builds the typed syntax tree of the given source (its caret marked, its
	 * brackets closed), or reuses a recent one.
	 */
	static TypedAST of(final String code, final ClassLoader loader) {
		synchronized (CACHE) {
			final TypedAST cached = CACHE.get(code);
			if (cached != null) return cached;
		}
		final TypedAST ast = build(code, loader);
		synchronized (CACHE) {
			CACHE.put(code, ast);
		}
		return ast;
	}

	/**
	 * Builds the typed syntax tree of the given source, with Groovy's
	 * compiler: parses it, resolves its names, and runs the static type checker
	 * over it (see {@link #typeCheck}), typing what it can; but makes no
	 * bytecode.
	 */
	private static TypedAST build(final String code,
		final ClassLoader loader)
	{
		final CompilerConfiguration config = new CompilerConfiguration();
		// NB: Collect all errors, rather than stopping at the first few.
		config.setTolerance(Integer.MAX_VALUE);
		final GroovyClassLoader gcl = new GroovyClassLoader(loader, config);
		try {
			final CompilationUnit unit = new CompilationUnit(config, null, gcl);
			final SourceUnit source = unit.addSource("CompletionScript.groovy",
				code);
			try {
				// Parse, resolve names and canonicalize; stop before bytecode.
				unit.compile(Phases.CANONICALIZATION);
			}
			catch (final RuntimeException | LinkageError exc) {
				// NB: Compilation errors are expected. Use what we have.
			}
			final ModuleNode module = source.getAST();
			if (module != null) {
				// NB: Unresolvable classes stop compilation before the type checker
				// would run, and dynamic code fails it, so run it ourselves.
				for (final ClassNode c : module.getClasses()) {
					try {
						typeCheck(source, c);
					}
					catch (final RuntimeException | LinkageError | AssertionError exc) {
						// NB: Keep whatever types were inferred before the failure.
					}
				}
			}
			return new TypedAST(source, loader);
		}
		finally {
			try {
				gcl.close();
			}
			catch (final IOException exc) {
				// NB: Nothing to clean up, then.
			}
		}
	}

	/**
	 * Runs the static type checker over a class of the script, typing the
	 * script's bindings (variables assigned without a declaration, e.g.
	 * {@code z = "abc"}), which it would leave untyped: each use of one has the
	 * type last assigned to it, in source order (the source ends at the caret).
	 */
	private static void typeCheck(final SourceUnit source, final ClassNode c) {
		final Map<String, ClassNode> bindings = new HashMap<>();
		final StaticTypeCheckingVisitor checker = new StaticTypeCheckingVisitor(
			source, c)
		{

			@Override
			public void visitBinaryExpression(final BinaryExpression e) {
				super.visitBinaryExpression(e);
				if (!isBindingAssignment(e)) return;
				final String name = ((VariableExpression) e.getLeftExpression())
					.getName();
				final ClassNode type = getType(e.getRightExpression());
				if (type == null || "java.lang.Object".equals(type.getName())) {
					bindings.remove(name);
				}
				else bindings.put(name, type);
			}
		};
		checker.addTypeCheckingExtension(new TypeCheckingExtension(checker) {

			@Override
			public boolean handleUnresolvedVariableExpression(
				final VariableExpression v)
			{
				final ClassNode type = bindings.get(v.getName());
				if (type == null) return false;
				storeType(v, type);
				return true;
			}
		});
		checker.visitClass(c);
	}

	/** True iff the expression assigns a script binding, e.g. {@code z = 1}. */
	private static boolean isBindingAssignment(final BinaryExpression e) {
		return e.getOperation().getType() == org.codehaus.groovy.syntax.Types.ASSIGN &&
			e.getLeftExpression() instanceof VariableExpression &&
			((VariableExpression) e.getLeftExpression())
				.getAccessedVariable() instanceof DynamicVariable;
	}

	/** A problem the compiler found in the source. */
	static final class Problem {

		/** Where it starts and ends: 1-based lines and columns. */
		final int line, column, endLine, endColumn;

		final String message;

		/** Whether it may yet work at run time (else it fails for sure). */
		final boolean warning;

		private Problem(final SyntaxException e, final String message,
			final boolean warning)
		{
			line = e.getStartLine();
			column = e.getStartColumn();
			endLine = e.getEndLine();
			endColumn = e.getEndColumn();
			this.message = message;
			this.warning = warning;
		}
	}

	/** The type checker's messages, e.g. about a call it cannot resolve. */
	private static final String TYPE_CHECKING = "[Static type checking] - ";

	/**
	 * Gets the problems the compiler found that would also trouble the script
	 * when it runs: syntax errors and unresolvable classes; and, as warnings,
	 * methods and properties the type checker cannot find on a known type
	 * (e.g. {@code "abc".fooBar()}). The type checker's other complaints, e.g.
	 * about dynamically typed code, are left out: Groovy resolves those when
	 * the script runs.
	 */
	List<Problem> problems() {
		final List<Problem> problems = new ArrayList<>();
		final List<? extends Message> errors = source.getErrorCollector()
			.getErrors();
		if (errors == null) return problems;
		for (final Message m : errors) {
			if (!(m instanceof SyntaxErrorMessage)) continue;
			final SyntaxException e = ((SyntaxErrorMessage) m).getCause();
			final String message = e.getOriginalMessage() == null ? null : e
				.getOriginalMessage().trim();
			if (message == null || message.contains(CARET)) continue;
			if (!message.startsWith(TYPE_CHECKING)) {
				problems.add(new Problem(e, message, false));
				continue;
			}
			final String complaint = message.substring(TYPE_CHECKING.length())
				.replace(" Please check if the declared type is correct and if the " +
					"method exists.", "");
			if (isKnownTypeMissing(complaint)) {
				problems.add(new Problem(e, complaint, true));
			}
		}
		return problems;
	}

	/**
	 * True iff the type checker's complaint is that a known, specific type
	 * lacks a method or property: rather than, e.g., that a variable's type is
	 * unknown ({@code Object}), or that the script calls methods it does not
	 * declare (which bindings may provide when it runs).
	 */
	private static boolean isKnownTypeMissing(final String complaint) {
		final String type;
		if (complaint.startsWith("Cannot find matching method ")) {
			final String call = complaint.substring(
				"Cannot find matching method ".length());
			final int hash = call.indexOf('#');
			if (hash < 0) return false;
			type = call.substring(0, hash);
		}
		else if (complaint.startsWith("No such property: ")) {
			final int at = complaint.indexOf(" for class: ");
			if (at < 0) return false;
			type = complaint.substring(at + " for class: ".length()).trim();
		}
		else return false;
		return type.contains(".") && !type.equals("java.lang.Object") && !type
			.startsWith("groovy.lang.");
	}

	/** True iff the source parsed (a syntax error leaves no tree). */
	boolean isParsed() {
		return module != null;
	}

	/**
	 * Gets the expression whose member is being completed, e.g. {@code img} in
	 * {@code img.CARET}, or null if the caret is not after a dot.
	 */
	Expression receiverAtCaret() {
		final Expression[] found = { null };
		visit(new Visitor() {

			@Override
			public void visitPropertyExpression(final PropertyExpression e) {
				if (CARET.equals(e.getPropertyAsString())) found[0] = e
					.getObjectExpression();
				super.visitPropertyExpression(e);
			}

			@Override
			public void visitMethodCallExpression(final MethodCallExpression e) {
				if (CARET.equals(e.getMethodAsString()) && !e.isImplicitThis()) {
					found[0] = e.getObjectExpression();
				}
				super.visitMethodCallExpression(e);
			}
		});
		return found[0];
	}

	/**
	 * Gets the variables visible at the caret, with their types (null if
	 * unknown), innermost last.
	 */
	Map<String, ClassNode> variablesAtCaret() {
		final ASTNode caret = caretNode();
		if (caret == null) return Collections.emptyMap();
		final int line = caret.getLineNumber(), column = caret.getColumnNumber();
		final Map<String, ClassNode> vars = new LinkedHashMap<>();
		visit(new Visitor() {

			private boolean contains(final ASTNode node) {
				if (node.getLineNumber() < 0) return true; // synthetic: unknown span
				return before(node.getLineNumber(), node.getColumnNumber()) && //
					!before(node.getLastLineNumber(), node.getLastColumnNumber());
			}

			private boolean before(final int l, final int c) {
				return l < line || l == line && c <= column;
			}

			private void add(final Variable v) {
				if (CARET.equals(v.getName())) return;
				vars.remove(v.getName()); // NB: Re-insert, so innermost comes last.
				vars.put(v.getName(), v.isDynamicTyped() ? null : v.getOriginType());
			}

			@Override
			protected void visitConstructorOrMethod(final MethodNode node,
				final boolean isConstructor)
			{
				if (!node.isScriptBody() && !contains(node)) return;
				for (final Parameter p : node.getParameters()) add(p);
				super.visitConstructorOrMethod(node, isConstructor);
			}

			@Override
			public void visitClosureExpression(final ClosureExpression e) {
				if (!contains(e)) return;
				if (e.getParameters() != null) {
					for (final Parameter p : e.getParameters()) add(p);
				}
				super.visitClosureExpression(e);
			}

			@Override
			public void visitBlockStatement(final BlockStatement block) {
				if (contains(block)) super.visitBlockStatement(block);
			}

			@Override
			public void visitForLoop(final ForStatement loop) {
				if (!contains(loop)) return;
				final Parameter v = loop.getVariable();
				if (v != null && v != ForStatement.FOR_LOOP_DUMMY) add(v);
				super.visitForLoop(loop);
			}

			@Override
			public void visitCatchStatement(final CatchStatement statement) {
				if (!contains(statement)) return;
				add(statement.getVariable());
				super.visitCatchStatement(statement);
			}

			@Override
			public void visitDeclarationExpression(final DeclarationExpression e) {
				if (before(e.getLineNumber(), e.getColumnNumber())) {
					if (e.isMultipleAssignmentDeclaration()) {
						for (final Expression v : e.getTupleExpression()
							.getExpressions())
						{
							if (v instanceof VariableExpression) {
								addDeclared((VariableExpression) v);
							}
						}
					}
					else addDeclared(e.getVariableExpression());
				}
				super.visitDeclarationExpression(e);
			}

			@Override
			public void visitBinaryExpression(final BinaryExpression e) {
				// NB: A binding assigned, e.g. z = "abc".
				if (isBindingAssignment(e)) {
					final String name = ((VariableExpression) e.getLeftExpression())
						.getName();
					vars.remove(name);
					vars.put(name, typeOf(e.getRightExpression()));
				}
				super.visitBinaryExpression(e);
			}

			private void addDeclared(final VariableExpression v) {
				if (CARET.equals(v.getName())) return;
				vars.remove(v.getName());
				vars.put(v.getName(), typeOf(v));
			}
		});
		return vars;
	}

	/** The call whose arguments contain the caret marker. */
	static final class Call {

		/**
		 * The call: a {@link MethodCallExpression},
		 * {@link StaticMethodCallExpression} or
		 * {@link ConstructorCallExpression}.
		 */
		final Expression expression;

		/** Its arguments, up to the one containing the caret. */
		final List<Expression> arguments;

		/** Index of the argument containing the caret. */
		final int active;

		private Call(final Expression expression,
			final List<Expression> arguments, final int active)
		{
			this.expression = expression;
			this.arguments = arguments;
			this.active = active;
		}
	}

	/**
	 * Gets the innermost call whose arguments contain the caret marker, e.g.
	 * {@code max} in {@code Math.max(1.5, CARET)}; or null if there is none.
	 */
	Call callAtCaret() {
		final Call[] found = { null };
		visit(new Visitor() {

			@Override
			public void visitMethodCallExpression(final MethodCallExpression e) {
				consider(e, e.getArguments());
				super.visitMethodCallExpression(e);
			}

			@Override
			public void visitStaticMethodCallExpression(
				final StaticMethodCallExpression e)
			{
				consider(e, e.getArguments());
				super.visitStaticMethodCallExpression(e);
			}

			@Override
			public void visitConstructorCallExpression(
				final ConstructorCallExpression e)
			{
				consider(e, e.getArguments());
				super.visitConstructorCallExpression(e);
			}

			private void consider(final Expression call, final Expression args) {
				if (!(args instanceof TupleExpression)) return;
				final List<Expression> list = ((TupleExpression) args)
					.getExpressions();
				for (int i = 0; i < list.size(); i++) {
					if (!containsCaret(list.get(i))) continue;
					// NB: Visited outside in, so the innermost call is found last.
					found[0] = new Call(call, list.subList(0, i + 1), i);
					return;
				}
			}
		});
		return found[0];
	}

	/**
	 * Gets the type of an expression, e.g. an argument, as a type name for
	 * {@link org.scijava.code.lsp.jvm.Signatures}; or null if unknown.
	 */
	String typeName(final Expression e) {
		if (e instanceof GStringExpression) return String.class.getName();
		if (e instanceof ConstantExpression) {
			final Object value = ((ConstantExpression) e).getValue();
			if (value == null) return null;
			if (value instanceof Integer) return "int";
			if (value instanceof Long) return "long";
			if (value instanceof Double) return "double";
			if (value instanceof Float) return "float";
			if (value instanceof Boolean) return "boolean";
			return value.getClass().getName(); // e.g. String, BigDecimal
		}
		final Class<?> c = toClass(typeOf(e));
		if (c == null || c == Object.class) return null; // dynamic: unknown
		final String name = c.getCanonicalName();
		return name == null ? c.getName() : name;
	}

	/** Gets the script's own methods (e.g. {@code def area(w, h)}) by name. */
	List<MethodNode> scriptMethods(final String name) {
		final List<MethodNode> methods = new ArrayList<>();
		if (module == null) return methods;
		for (final ClassNode c : module.getClasses()) {
			if (c.isScript()) methods.addAll(c.getMethods(name));
		}
		return methods;
	}

	/** True iff the expression is, or contains, the caret marker. */
	private boolean containsCaret(final Expression e) {
		final boolean[] found = { false };
		e.visit(new Visitor() {

			@Override
			public void visitVariableExpression(final VariableExpression v) {
				if (CARET.equals(v.getName())) found[0] = true;
			}
		});
		return found[0];
	}

	// -- Types --

	/** Gets the (inferred or declared) type of an expression, or null. */
	static ClassNode typeOf(final Expression e) {
		if (e == null) return null;
		// NB: The type checker infers Class<T> for a class reference; we want T.
		if (e instanceof ClassExpression) return e.getType();
		final Object inferred = e.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE);
		if (inferred instanceof ClassNode) return (ClassNode) inferred;
		if (e instanceof VariableExpression) {
			final Variable v = ((VariableExpression) e).getAccessedVariable();
			if (v != null && v != e && !v.isDynamicTyped()) return v.getOriginType();
			final VariableExpression ve = (VariableExpression) e;
			return ve.isDynamicTyped() ? null : ve.getOriginType();
		}
		final ClassNode type = e.getType();
		if (type == null || "java.lang.Object".equals(type.getName())) return null;
		return type;
	}

	/**
	 * Gets the Java class of a type, or null if it is unknown or defined in the
	 * script itself.
	 */
	Class<?> toClass(final ClassNode type) {
		if (type == null) return null;
		final ClassNode t = type.redirect();
		if (t.isPrimaryClassNode()) return null; // NB: Not compiled.
		try {
			return t.getTypeClass();
		}
		catch (final RuntimeException | LinkageError | AssertionError exc) {
			// NB: Not loaded yet; load it by name.
		}
		return load(t.getName(), loader);
	}

	/**
	 * Loads a type by name, e.g. {@code int}, {@code long[]}, {@code a.b.C} or
	 * {@code a.b.Outer.Inner}, or returns null if it cannot be loaded.
	 */
	static Class<?> load(final String name, final ClassLoader loader) {
		if (name == null) return null;
		final Class<?> c = Types.load(name, loader);
		if (c != null) return c;
		// NB: Perhaps a nested class, named canonically: a.b.Outer.Inner.
		final int dot = name.lastIndexOf('.');
		return dot < 0 ? null : load(name.substring(0, dot) + "$" + name
			.substring(dot + 1), loader);
	}

	// -- Helper methods --

	/** Gets the node of the caret marker. */
	private ASTNode caretNode() {
		final ASTNode[] found = { null };
		visit(new Visitor() {

			@Override
			public void visitVariableExpression(final VariableExpression e) {
				if (CARET.equals(e.getName())) found[0] = e;
				super.visitVariableExpression(e);
			}

			@Override
			public void visitPropertyExpression(final PropertyExpression e) {
				if (CARET.equals(e.getPropertyAsString())) found[0] = e.getProperty();
				super.visitPropertyExpression(e);
			}

			@Override
			public void visitMethodCallExpression(final MethodCallExpression e) {
				if (CARET.equals(e.getMethodAsString())) found[0] = e.getMethod();
				super.visitMethodCallExpression(e);
			}
		});
		return found[0];
	}

	/** Visits every class (including the script itself) of the source. */
	private void visit(final Visitor visitor) {
		if (module == null) return;
		for (final ClassNode c : module.getClasses()) {
			try {
				visitor.visitClass(c);
			}
			catch (final RuntimeException exc) {
				// NB: Malformed tree; skip this class.
			}
		}
	}

	private abstract class Visitor extends ClassCodeVisitorSupport {

		@Override
		protected SourceUnit getSourceUnit() {
			return source;
		}
	}
}
