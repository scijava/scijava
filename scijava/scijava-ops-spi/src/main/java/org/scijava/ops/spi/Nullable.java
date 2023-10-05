package org.scijava.ops.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a parameter as nullable: Ops with nullable parameters
 * should be callable <b>with or without</b> their Nullable arguments
 * <p>
 * This annotation should only be specified on <b>one</b> of the signatures of
 * the method (only on the {@link FunctionalInterface}'s method, or only on the
 * Op implementation, etc) for purposes of simplicity and readability. Writing
 *
 * <pre>
 * public interface BiFunctionWithNullable&lt;I1, I2, I3, O&gt; extends
 * 	Functions.Arity3&lt;&gt;
 * {
 *
 * 	public O apply(I1 in1, I2 in2, @Nullable I3 in3);
 * }
 * </pre>
 *
 * and then writing an implementation
 *
 * <pre>
 * public class Impl implements BiFunctionWithNullable&lt;Double, Double, Double, Double&gt; {
 * 	public Double apply(Double in1, @Nullable Double in2, Double in3) {
 * 	...
 * 	}
 * }
 * </pre>
 *
 * is confusing and hard to read. Which parameters are nullable in this case? Is
 * it obvious that {@code in3} is nullable just by looking at {@code Impl}? For
 * this reason, it should be enforced that the annotation is only on one of the
 * method signatures.
 * <br/><br/>
 * Note also that annotations are currently (as of Java 11) not supported on lambdas.
 * Nullable parameters are supported on interfaces and class implementations (including
 * anonymous classes) but the following will not recognize the nullability of {@code in}:
 * <pre>
 * &#64;OpField(names = "nullableLambda")
 * public final Function<Integer, Float> nullableLambda = (&#64;Nullable Integer in) -> {
 * 	Integer nonNullIn = in == null ? 0 : in;
 * 	return nonNullIn + 0.5f;
 * };
 * </pre>
 *
 * See also these SO posts:
 * <a href="https://stackoverflow.com/questions/44646915/will-it-be-possible-to-annotate-lambda-expression-in-java-9">one</a>,
 * <a href="https://stackoverflow.com/questions/22375891/annotating-the-functional-interface-of-a-lambda-expression">two</a>
 *
 * @author Gabriel Selzer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Nullable {

}
