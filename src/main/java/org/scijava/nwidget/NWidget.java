package org.scijava.nwidget;

import org.scijava.struct.MemberInstance;

public interface NWidget<T> {
	MemberInstance<T> member();
}
