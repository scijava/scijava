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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.antlr.runtime.tree.CommonTree;
import org.python.antlr.PythonTree;
import org.python.antlr.ast.Assign;
import org.python.antlr.ast.Attribute;
import org.python.antlr.ast.BinOp;
import org.python.antlr.ast.Call;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.Expr;
import org.python.antlr.ast.Expression;
import org.python.antlr.ast.For;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.If;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.Name;
import org.python.antlr.ast.Num;
import org.python.antlr.ast.Return;
import org.python.antlr.ast.Str;
import org.python.antlr.ast.TryExcept;
import org.python.antlr.ast.TryFinally;
import org.python.antlr.ast.Tuple;
import org.python.antlr.ast.While;
import org.python.antlr.ast.With;
import org.python.antlr.ast.Yield;
import org.python.antlr.ast.alias;
import org.python.antlr.ast.arguments;
import org.python.antlr.base.expr;
import org.python.antlr.base.mod;
import org.python.core.CompileMode;
import org.python.core.CompilerFlags;
import org.python.core.ParserFacade;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.indexer.types.NModuleType;

public class JythonScriptParser
{	
	/**
	 * Parse valid jython code.
	 * 
	 * @param code
	 * 
	 * @return The top-level {@link Scope}, which is empty (see {@link Scope#isEmpty()}) when the code has errors and can't be parsed by {@link ParserFacade#parse(String, CompileMode, String, CompilerFlags)}.
	 */
	static public Scope parseAST(final String code) {
		return parseAST(code, Collections.emptyMap());
	}

	/**
	 * Parse valid jython code within a top-level {@link Scope} that already
	 * defines the given variables, such as script parameters injected by the
	 * framework or the bindings of a live interpreter. Assignments in the code
	 * take precedence over these.
	 * 
	 * @param code
	 * @param predefined Variables defined before the code runs.
	 * 
	 * @return The top-level {@link Scope}, holding only the predefined variables when the code has errors and can't be parsed by {@link ParserFacade#parse(String, CompileMode, String, CompilerFlags)}.
	 */
	static public Scope parseAST(final String code, final Map<String, DotAutocompletions> predefined) {
		// The code includes from beginning of the file until the point at which an autocompletion is requested.
		// Therefore, remove the last line, which would fail to parse because it is incomplete
		final Scope scope = new Scope(null);
		scope.vars.putAll(predefined);
		try {
			final mod m = ParserFacade.parse(code, CompileMode.exec, "<none>", new CompilerFlags());
			if (null != m.getChildren()) parseNode(scope, m.getChildren(), null);
		} catch (Throwable t) {
			JythonDev.printError(t);
		}
		return scope;
	}

	/**
	 * Parse a {@code List} of {@code PythonTree} instances, each representing a python statement
	 * including {@code ImportFrom, Assign, FunctionDef, ClassDef}.
	 * 
	 * @param children The list of statements.
	 * @param parent The {@code Scope} that contains these statements.
	 * @param className Indicates whether the containing {@code Scope} is a python class definition, otherwise null.
	 * @return A new {@code Scope} containing {@code DotAutocompletions} to represent each statement.
	 */
	static public Scope parseNode(final List<PythonTree> children, final Scope parent, final String className) {
		if (null == children) return parent; // will return null if parent is null
		final Scope scope = new Scope(parent, className);
		parseNode(scope, children, className);
		return scope;
	}

	static public void parseNode(final Scope scope, final List<PythonTree> children, final String className) {
		
		for (final PythonTree child : children) {
			JythonDev.printTrace(child.getClass());
			
			if (child instanceof ImportFrom)
				scope.imports.putAll(parseImportFromStatement( (ImportFrom)child ));
			else if (child instanceof Import)
				scope.imports.putAll(parseImportStatement((Import)child));
			else if (child instanceof Assign)
				scope.vars.putAll(parseAssignStatement( (Assign)child, scope ));
			else if (child instanceof FunctionDef)
				parseFunctionDef((FunctionDef)child, scope);
			else if (child instanceof ClassDef)
				parseClassDef((ClassDef)child, scope);
			else if (child instanceof Expr)
				parseExpr((Expr)child, scope);
			else if (child instanceof If || child instanceof For || child instanceof While
					|| child instanceof TryExcept || child instanceof TryFinally || child instanceof With)
				// no new scope in if/for/while/with/try/ statements in python
				parseNode(scope, child.getChildren(), null);
			else
				JythonDev.printTrace("IGNORING child: " + child + " -- " + (null != child.getChildren() ?
						String.join("::", child.getChildren().stream().map(c -> c.toString()).collect(Collectors.toList()))
						: ""));
		}
	}
	
	static public void parseExpr(final Expr child, final Scope scope) {
		// child.getText() shows child is the base
		JythonDev.printTrace("Expr: " + child.getText() + ", " + child.getInternalValue() + ", " + child.getValue() + ", children: " + String.join(", ", child.getChildren().stream().map(PythonTree::toString).collect(Collectors.toList())));
	}

	/**
	 * Parse import statements, considering aliases.
	 * There can be more than one if e.g. commas were used, as in "from ij import IJ, ImageJ".
	 * @param im
	 * @return A map of simple class names or their aliases as keys versus a {@code DotAutocompletions} as value. 
	 */
	static public Map<String, DotAutocompletions> parseImportFromStatement(final ImportFrom im) {
		final Map<String, DotAutocompletions> classes = new HashMap<>();
		final String module = im.getModule().toString();
		for (int i=0; i<im.getNames().__len__(); ++i) {
			final String alias = im.getInternalNames().get(i).getAsname().toString(); // alias: as name
			final String simpleClassName = im.getInternalNames().get(i).getInternalName(); // class name
			classes.put("None" == alias ? simpleClassName : alias, new StaticDotAutocompletions(module + "." + simpleClassName));
		}
		return classes;
	}
	
	static public Map<String, DotAutocompletions> parseImportStatement(final Import im) {
		final Map<String, DotAutocompletions> classes = new HashMap<>();
		final List<alias> aliases = im.getInternalNames();
		for (final alias a: aliases) {
			final String as = a.getInternalAsname();
			final String name = a.getInternalName();
			classes.put(null == as || "None" == as ? name : as, new StaticDotAutocompletions(name));
		}
		return classes;
	}
	
	static private DotAutocompletions maybeStaticToDot(final PythonTree node, final DotAutocompletions da) {
		JythonDev.printTrace("children count:" + node.getChildCount() + ", children: " + (null != node.getChildren() ? node.getChildren().stream().map(c -> c.toString()).collect(Collectors.toList()) : ""));
		if (node.getChildCount() > 0 && da instanceof StaticDotAutocompletions) {
			// It's a right expression (a constructor invocation assigned to a variable on the left) so the left is an instance of the class
			return new VarDotAutocompletions(da.getClassname());
		}
		return da;
	}
	
	/**
	 * Parse an assignment (an equal sign) to find out the class of the left side (the variable)
	 * by asking the right side about what it is or returns.
	 * There can be more than one variable when using deconstruction statements like e.g. "width, height = imp.getWidth(), imp.getHeight()".
	 * 
	 * @param assign
	 * @param scope
	 * @return A map of variable names as keys versus {@code DotAutocompletions} as values.
	 */
	static public Map<String, DotAutocompletions> parseAssignStatement(final Assign assign, final Scope scope) {
		final Map<String, DotAutocompletions> assigns = new HashMap<>();
		//final expr right = assign.getInternalValue(); // strangely this works
		final PythonTree right = assign.getChildren().get(1);
		final PythonTree left = assign.getChildren().get(0);
		if (null != left.getChildren() && left.getChildren().size() > 1
				&& (right instanceof Tuple || right instanceof org.python.antlr.ast.List)) { // TODO are there any other possible?
			for (int i=0; i<right.getChildren().size(); ++i) {
				final CommonTree ct = left.getChildren().get(i).getNode();
				if (null == ct) {
					JythonDev.printTrace("null for left: '" + left + "'" + " at child node " + i);
					continue;
				}
				final String name = ct.toString();
				final DotAutocompletions val = maybeStaticToDot(right, parseRight(right.getChildren().get(i), scope));
				if (null != val) assigns.put(name, val);
			}
		} else {
			// Left is a Name: simple assignment e.g. "one = 1"
			if ( left instanceof Name ) {
				assigns.put(((Name)left).getInternalId(), maybeStaticToDot(right, parseRight(right, scope)));
				return assigns;
			}
			// Assignment to an attribute
			// Handle left when it's e.g. self.width = 10 which creates a new member in "self".
			// Will have to be recursive, as it could be multiple dereferences, e.g. self.volume.name = "that"
			// Has to: find out what the base is (e.g. 'self') and add, as an expansion of it, the attribute (e.g. "width")
			// with the assigned class (e.g. "PyInteger" for "10"), and add names, if not there yet, to the appropriate lists for autocompletion.
			PyObject leftn = left;
			final ArrayList<Attribute> attrs = new ArrayList<>();
			while (leftn instanceof Attribute) {
				final Attribute attr = (Attribute)leftn;
				attrs.add(attr);
				leftn = attr.getValue();
			}
			if (leftn instanceof Name) {
				String varName = ((Name)leftn).getInternalId();
				Collections.reverse(attrs);
				Scope scopeC = scope;
				for (final Attribute attr: attrs) {
					final DotAutocompletions ac = scopeC.find(varName, DotAutocompletions.EMPTY); // in the first iteration it finds the completions for the base Name
					if (ac instanceof ClassDotAutocompletions) {
						final ClassDotAutocompletions cda = (ClassDotAutocompletions)ac;
						varName = attr.getInternalAttrName().getInternalId(); // in the first iteration becomes the name of the first Attribute
						// Add the name of the Attribute to the list of expansions for the prior varName
						scopeC = cda.scope; // prepare scope for next iteration
						//scopeC.vars.put(varName, cda); // Is this needed? I think it isn't
						cda.put(new CompletionText(varName)); // add varName (e.g. "width") as a possible expansion for the prior varName (e.g. "self").
					} else {
						// Don't know how to handle e.g. self.doThis().that = 10 because for "doThis()" there would be a class return type stored 
						break;
					}
				}
				return assigns;
			}
		}
 
		return assigns;
	}
	
	/**
	 * Adds a child Scope to the given parent Scope, and also a variable to the parent scope
	 * with no class, just for the function name. Then populates the child scope.
	 * 
	 * @param fn
	 * @param parent
	 */
	static public void parseFunctionDef(final FunctionDef fn, final Scope parent) {
		// Get the function name
		final String name = fn.getInternalName();
		// Get the list of argument names, if any
		arguments args = fn.getInternalArgs();
		final List<String> argumentNames = args != null && args.getChildren() != null ? // why oh why not return an empty List<PythonTree>
				args.getChildren().stream().map(arg -> arg.getNode().toString()).collect(Collectors.toList())
				: Collections.emptyList();
		// Parse the function body
		final List<PythonTree> children = fn.getChildren();
		if (null == children) return;
		final Scope fn_scope = new Scope(parent, null);
		// Add arguments to the scope -- must be done BEFORE parseNode
		for (final String arg: argumentNames) {
			// Empty. For the first argument ("self" or similar) will be replaced later if it's part of a class definition.
			fn_scope.vars.put(arg, new ClassDotAutocompletions("<unknown>", Collections.emptyList(), Collections.emptyList(), 
					new ArrayList<CompletionText>(), fn_scope));
		}
		parseNode(fn_scope, fn.getChildren(), null);
		// Get the return type, if any
		final PythonTree last = fn.getChildren().get(fn.getChildCount() -1);
		final String returnClassName = last instanceof Return ? parseRight(last.getChildren().get(0), fn_scope).toString() : null;
		parent.vars.put(name, new DefVarDotAutocompletions(name, returnClassName, argumentNames, fn_scope));
	}
	
	/**
	 * Adds an entry to the parent scope with the python classname, e.g. "Volume", with its parameters (from its __init__ method if any),
	 * including as well the methods and fields from any superclass,
	 * and adds another entry in the class scope for "self" with all the class method names.
	 * 
	 * @param c
	 * @param parent
	 */
	static public void parseClassDef(final ClassDef c, final Scope parent) {
		final String pyClassname = c.getInternalName();
		final Scope class_scope = parseNode(c.getChildren(), parent, pyClassname);
		// Methods of the class
		final List<CompletionText> classDotAutocompletions = new ArrayList<>();
		// Iterate vars of the scope, which are those of the class only
		for (final DotAutocompletions da: class_scope.vars.values()) {
			if (da instanceof DefVarDotAutocompletions) {
				final DefVarDotAutocompletions dda = (DefVarDotAutocompletions)da;
				classDotAutocompletions.add(new CompletionText(dda.fnName));
			}
		}
		// Superclasses
		final List<String> superclassNames = new ArrayList<>();
		for (final expr e: c.getInternalBases()) {
			final DotAutocompletions da = parent.find(e.getText(), null);
			if (null == da || null == da.getClassname())
				JythonDev.print("Could not find completions and className for " + e.getText());
			else
				superclassNames.add(da.getClassname());
		}
		// Search for the constructor __init__ if any to get the constructor parameters
		final List<String> argumentNames = new ArrayList<>();
		final ClassDotAutocompletions cda = new ClassDotAutocompletions(pyClassname, superclassNames, argumentNames, classDotAutocompletions, class_scope);
		for (final PythonTree child: c.getChildren()) {
			if (!(child instanceof FunctionDef)) continue;
			final FunctionDef fn = (FunctionDef)child;
			final List<PythonTree> args = fn.getInternalArgs().getChildren();
			if (args.size() > 0) {
				// Populate class constructor argument list by reading them from the __init__ method
				if ("__init__".equals(fn.getInternalName())) {
					// Add all arguments except the first one, which is the internal reference conventionally named "self"
					argumentNames.addAll(args.subList(1, args.size()).stream()
						.map(arg -> arg.getNode().toString()).collect(Collectors.toList()));
				}
				// Add completions to the first argument (generally "self")
				// TODO check annotations, shouldn't add them if the function is static
				final DefVarDotAutocompletions fnda = (DefVarDotAutocompletions)class_scope.vars.get(fn.getInternalName());
				final DotAutocompletions argda = fnda.scope.vars.get(args.get(0).getNode().toString());
				if (argda instanceof ClassDotAutocompletions) {
					// Replace the autocompletions for the first argument with cda plus whichever autocompletions it accumulated within the function definition.
					((ClassDotAutocompletions)argda).mutateIntoPlus(cda);
				}
				//.scope.vars.put(args.get(0).getNode().toString(), cda);
			}
		}
		
		// Add to the parent scope for expansion of the constructor name plus parameters 
		parent.vars.put(pyClassname, cda);
	}
	
	/** Discover the class returned by the right statement in an assignment.
	 * 
	 * @param right
	 * @param scope
	 */
	/**
	 * Infers the type of a single expression, such as a function argument, in
	 * the given scope.
	 * 
	 * @param expression The source text of the expression.
	 * @param scope The scope in which to resolve names.
	 * @return A class name, a primitive type name (e.g. "long" for an integer
	 *         literal), or null if unknown.
	 */
	static public String typeOf(final String expression, final Scope scope) {
		try {
			final mod m = ParserFacade.parse(expression, CompileMode.eval, "<none>", new CompilerFlags());
			if (!(m instanceof Expression)) return null;
			return parseRight(((Expression) m).getInternalBody(), scope).getClassname();
		} catch (final Throwable t) {
			// Incomplete or invalid expression.
			return null;
		}
	}

	static public DotAutocompletions parseRight(final PyObject right, final Scope scope) {
		if (right instanceof Name) {
			// e.g. the name of another variable:
			// imp2 = imp
			// e.g. the name of a constructor or a function
			// ip = ByteProcessor(512, 512)
			return scope.find( ((Name)right).getInternalId(), DotAutocompletions.EMPTY);
		}
		if (right instanceof Num) {
			// e.g. return 10
			// e.g. n = 42
			final Class<?> c = ((Num)right).getInternalN().getClass() == PyInteger.class ? Long.TYPE : Double.TYPE; // TODO this creates trouble for Scope.findVarsByType and would be perhaps easier as Long and Double (non-primitive).
			return new VarDotAutocompletions(c.toString()); // becomes "long" or "double"
		}
		if (right instanceof Str) {
			// Literal String
			return new VarDotAutocompletions(String.class.getCanonicalName());
		}
		if (right instanceof Attribute) {
			// e.g. a field or a method
			// gray8 = IJ.getImage().GRAY8
			final Attribute attr = (Attribute)right;
			final DotAutocompletions da = parseRight(attr.getValue(), scope);
			// e.g. a class by its full name, after a package (or nothing known)
			// img = net.imglib2.img.Img()
			if (DotAutocompletions.EMPTY == da || da instanceof StaticDotAutocompletions) {
				final String fullName = javaClass(attr);
				if (null != fullName)
					return new StaticDotAutocompletions(fullName);
			}
			if (DotAutocompletions.EMPTY == da)
				return da;
			final String name = attr.getInternalAttr();
			final String className = da.getClassname();
			try {
				final Class<?> c = Class.forName(className);
				for (final Method m : c.getMethods())
					if (m.getName().equals(name))
						return new VarDotAutocompletions(m.getReturnType().getName());
				return new VarDotAutocompletions(c.getField(name).getType().getName());
			} catch (Exception e) {
				JythonDev.print("Could not find method or field " + name + " in class " + className, e);
			}
			// Could also be a python module, e.g. attempting to autocomplete "os.path."
			try {
				final NModuleType module = Scope.indexer.loadModule(className + "." + name); // overly expensive: any way to check it exists without loading it?
				if (null != module)
					return new StaticDotAutocompletions(className  + "." + name);
			} catch (Exception e) {
				JythonDev.printTrace("Not a python module: " + className + "." + name);
			}
		}
		if (right instanceof Call) {
			// e.g. a method call, in particular the last one in the chain
			// imp = IJ.getImage().getProcessor()
			final Call call = (Call)right;
			return parseRight(call.getFunc(), scope); // getFunc() returns an Attribute or a Name
		}
		if (right instanceof Yield) {
			final Yield yield = (Yield)right;
			return parseRight(yield.getValue(), scope);
		}
		if (right instanceof BinOp) {
			// e.g., division, multiplication, addition, subtraction
			// What actual type is returned, I don't know: python allows operator overloading.
			final BinOp binop = (BinOp)right;
			// Weak attempt at finding out what is it (will not succeed if neither is a number)
			// TODO this should be done recursively
			JythonDev.printTrace("BinOp: weak attempt at finding out what kind of number it returns.");
			// First, check if any is a number
			final Class<?> typeL = binop.getLeft().isNumberType() ? ((Num)binop.getLeft()).getInternalN().getClass() : null,
					 	   typeR = binop.getRight().isNumberType() ? ((Num)binop.getRight()).getInternalN().getClass() : null;
			if (null != typeL || null != typeR) {
				// If both are integers
				if (typeL == PyInteger.class && typeR == PyInteger.class)
					return new VarDotAutocompletions(Long.class.toString()); // long
				// If any is a float
				if (typeL == PyFloat.class || typeR == PyFloat.class)
					return new VarDotAutocompletions(Double.class.toString()); // double
			}
			// Go by whatever left is, or if empty, whatever right is: no guarantee to be correct, but can be correct often
			final DotAutocompletions da = parseRight(binop.getLeft(), scope);
			if (da != DotAutocompletions.EMPTY)
				return da;
			return parseRight(binop.getRight(), scope);
		}
		
		JythonDev.printTrace("Unsupported 'right' is: " + right + " " + (right != null ? right.getClass() : ""));
		
		return DotAutocompletions.EMPTY;
	}

	/**
	 * The Java class a dotted name (e.g. {@code java.util.Map.Entry}) is, by
	 * its full name; or null if none (e.g. it starts with a variable).
	 */
	static private String javaClass(final Attribute attr) {
		final StringBuilder name = new StringBuilder(attr.getInternalAttr());
		PyObject node = attr.getValue();
		while (node instanceof Attribute) {
			name.insert(0, ((Attribute)node).getInternalAttr() + ".");
			node = ((Attribute)node).getValue();
		}
		if (!(node instanceof Name)) return null;
		name.insert(0, ((Name)node).getInternalId() + ".");
		// NB: A package starts lowercase; a class, uppercase.
		if (!Character.isLowerCase(name.charAt(0))) return null;
		String candidate = name.toString();
		while (true) {
			try {
				return Class.forName(candidate).getName();
			} catch (ClassNotFoundException | LinkageError e) {
				// Perhaps a nested class: e.g. java.util.Map$Entry.
				final int dot = candidate.lastIndexOf('.');
				if (dot < 0 || !Character.isUpperCase(candidate.charAt(candidate.lastIndexOf('.', dot - 1) + 1))) return null;
				candidate = candidate.substring(0, dot) + "$" + candidate.substring(dot + 1);
			}
		}
	}
}
