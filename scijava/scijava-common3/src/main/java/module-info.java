/**
 * SciJava Common3 Module
 * <p>
 * NB some IDEs (e.g. Intellij) complain about the module name
 * 'org.scijava.common3' due to the terminating digit. The warning is intended
 * to discourage versioning in the module name, and thus does not pertain to
 * this module. From
 * https://docs.oracle.com/javase/specs/jls/se16/html/jls-6.html#jls-6.1:
 * </p>
 * <p>
 * "The name of a module should correspond to the name of its principal
 * exported package. If a module does not have such a package, or if for legacy
 * reasons it must have a name that does not correspond to one of its exported
 * packages, then its name should still start with the reversed form of an
 * Internet domain with which its author is associated."
 * </p>
 */
open module org.scijava.common3 {
	exports org.scijava.common3;
	exports org.scijava.common3.validity;
}
