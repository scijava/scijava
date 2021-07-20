package org.scijava.ops.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotates a helper op as a field that should be auto injected.*/
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER})
public @interface OpDependency {
	
	/** The name of the Op to inject. */
	String name();
	
	/** Set to false if the dependency should not be adapted */
	boolean adaptable() default true;
}
