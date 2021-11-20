package org.scijava.ops.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.scijava.Priority;

/** Annotates an op declared as a field in an {@link OpCollection}. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface OpClass {

	String names();

	// the names of the parameters (inputs and outputs) that will appear in a call
	// to help().
	String[] params() default "";

	double priority() default Priority.NORMAL;

}
