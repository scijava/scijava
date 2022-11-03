
package org.scijava.ops.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

/**
 * Annotates an op declared as a {@link Field} in an {@link OpCollection}.
 * 
 * @author Gabriel Selzer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OpField {

	String names();

	// the names of the parameters (inputs and outputs) that will appear in a call
	// to help().
	// TODO: add default names support in OpFieldInfo
	String[] params() default "";

	/**
	 * Returns the priority of this Op By default, Ops have a priority of 0.0
	 * (corresponding to org.scijava.priority.Priority.NORMAL
	 *
	 * @return the priority of the Op
	 */
	double priority() default 0.0;

}