package org.scijava.struct;

import org.scijava.util.ConversionUtils;

public class ValueAccessibleMemberInstance<T> implements MemberInstance<T> {

	private Member<T> member;
	private ValueAccessible<T> access;
	private Object object;

	public <M extends Member<T> & ValueAccessible<T>> ValueAccessibleMemberInstance(
		final M member, final Object object)
	{
		this.member = member;
		this.access = member;
		this.object = object;
	}

	@Override
	public Member<T> member() {
		return member;
	}

	@Override
	public boolean isReadable() {
		return true;
	}

	@Override
	public boolean isWritable() {
		return true;
	}

	@Override
	public T get() {
		return access.get(object);
	}

	@Override
	public void set(Object value) {
		// TODO: Use generic Types assignability method.
		final Class<?> type = member().getRawType();
		if (!ConversionUtils.canAssign(value.getClass(), type)) {
			throw new IllegalArgumentException("value of type " + value.getClass()
				.getName() + " is not assignable to " + type.getName());
		}
		@SuppressWarnings("unchecked")
		final T tValue = (T) value;
		access.set(tValue, object);
	}
}
