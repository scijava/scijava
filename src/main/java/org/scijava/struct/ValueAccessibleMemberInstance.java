package org.scijava.struct;

import org.scijava.ops.base.TypeUtils;
import org.scijava.util.Types;

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
		final Class<?> type = member().getRawType();
		if (!TypeUtils.isAssignable(value != null ? value.getClass() : null, type)) {
			throw new IllegalArgumentException("value of type " + //
				Types.name(value.getClass()) + " is not assignable to " + //
				Types.name(type));
		}
		@SuppressWarnings("unchecked")
		final T tValue = (T) value;
		access.set(tValue, object);
	}
}
