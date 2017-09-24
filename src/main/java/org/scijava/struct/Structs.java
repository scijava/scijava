
package org.scijava.struct;

public final class Structs {

	private Structs() {
		// NB: Prevent instantiation of utility class.
	}

	public static <O> StructInstance<O> instance(final Struct struct,
		final O object)
	{
		return new DefaultStructInstance<>(struct, object);
	}

	public static StructInstance<?> expand(final StructInstance<?> parent,
		final String key)
	{
		return expand(parent.member(key));
	}

	public static <T> StructInstance<T> expand(
		final MemberInstance<T> memberInstance)
	{
		if (!memberInstance.member().isStruct()) return null;
		return instance(memberInstance.member().childStruct(), //
			memberInstance.get());
	}
}
