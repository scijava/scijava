package org.scijava.ops.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.scijava.Priority;

/** Annotates an op declared as a field in an {@link OpCollection}. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OpField {

	String names();

	// the names of the parameters (inputs and outputs) that will appear in a call
	// to help().
	//TODO: add default names support in OpFieldInfo
	String[] params() default "";

	double priority() default Priority.NORMAL;

}
