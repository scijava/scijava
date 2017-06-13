
package org.scijava.struct;

/**
 * Utility functions for working with {@link org.scijava.struct} classes.
 *
 * @author Curtis Rueden
 */
public final class Structs {

	/**
	 * Determines whether the given item is structured (i.e., contains nested
	 * items).
	 *
	 * @param item The item to check for nested structure.
	 * @return True iff the given item has nested fields.
	 */
	public static boolean isStructured(final StructItem<?> item) {
		return item instanceof StructInfo;
	}

	/**
	 * For a structured item, retrieves a {@link StructInfo} describing those
	 * nested items.
	 *
	 * @param item The item from which to extract nested items.
	 * @return The item as a {@link StructInfo}, or null if the given item does
	 *         not contain nested fields.
	 * @see #isStructured(StructItem)
	 */
	public static StructInfo<StructItem<?>> infoOf(final StructItem<?> item) {
		if (!isStructured(item)) return null; // not a structured item
		@SuppressWarnings("unchecked")
		final StructInfo<StructItem<?>> info = (StructInfo<StructItem<?>>) item;
		return info;
	}
}
