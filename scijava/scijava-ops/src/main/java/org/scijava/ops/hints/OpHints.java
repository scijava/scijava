
package org.scijava.ops.hints;

/**
 * An annotation used to record the hints that apply to a particular Op.
 * 
 * @author Gabriel Selzer
 */
public @interface OpHints {

	String[] hints() default {};
}
