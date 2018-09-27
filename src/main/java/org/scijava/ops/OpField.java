package org.scijava.ops;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.scijava.core.Priority;
import org.scijava.ops.core.OpCollection;

/** Annotates an op declared as a field in an {@link OpCollection}. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface OpField {

	String names();

	double priority() default Priority.NORMAL;

}
