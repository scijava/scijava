
package org.scijava.struct;

import java.lang.annotation.Target;

/**
 * A name/value attribute pair, used to extend the @{@link Parameter} annotation.
 * 
 * @author Curtis Rueden
 * @see Parameter#attrs()
 */
@Target({})
public @interface Attr {

	/** Name of the attribute. */
	String name();

	/** The attribute's value, if applicable. */
	String value() default "";
}
