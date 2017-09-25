package org.scijava.nwidget;

import org.scijava.struct.MemberInstance;

public abstract class NAbstractWidget implements NWidget {

	private final MemberInstance<?> memberInstance;

	public NAbstractWidget(final MemberInstance<?> memberInstance) {
		this.memberInstance = memberInstance;
	}

	@Override
	public MemberInstance<?> member() {
		return memberInstance;
	}
}
