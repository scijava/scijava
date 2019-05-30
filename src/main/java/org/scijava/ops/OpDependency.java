
package org.scijava.ops;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotates a helper op as a field that should be auto injected.*/
@Repeatable(OpDependencies.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface OpDependency {

	/** The name of the Op to inject. */
	String name();
}
