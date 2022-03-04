package org.scijava.ops.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a parameter as optional: Ops with optional parameters
 * should be callable <b>with or without</b> their Optional arguments
 * <p>
 * This annotation should only be specified on <b>one</b> of the signatures of
 * the method (only on the {@link FunctionalInterface}'s method, or only on the
 * Op implementation, etc) for purposes of simplicity and readability. Writing
 *
 * <pre>
 * public interface BiFunctionWithOptional&lt;I1, I2, I3, O&gt; extends
 * 	Functions.Arity3&lt;&gt;
 * {
 *
 * 	public O apply(I1 in1, I2 in2, @Optional I3 in3);
 * }
 * </pre>
 *
 * and then writing an implementation
 *
 * <pre>
 * public class Impl implements BiFunctionWithOptional&lt;Double, Double, Double, Double&gt; {
 * 	public Double apply(Double in1, @Optional Double in2, Double in3) {
 * 	...
 * 	}
 * }
 * </pre>
 *
 * is confusing and hard to read. Which parameters are optional in this case? Is
 * it obvious that {@code in3} is optional just by looking at {@code Impl}? For
 * this reason, it should be enforced that the annotation is only on one of the
 * method signatures.
 *
 * @author Gabriel Selzer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Optional {

}
