
package org.scijava.widget;

import java.util.Arrays;

import org.scijava.param.ParameterMember;
import org.scijava.struct.Member;
import org.scijava.struct.MemberInstance;

/** Utility class for working with {@link Widget}s. */
public final class Widgets {

	private Widgets() {
		// NB: Prevent instantiation of utility class.
	}

	public static ParameterMember<?> param(final Widget widget) {
		return param(widget.model());
	}

	public static <T> ParameterMember<T> param(final MemberInstance<T> model) {
		final Member<T> member = model.member();
		return member instanceof ParameterMember ? //
			(ParameterMember<T>) member : null;
	}

	public static String label(final Widget widget) {
		if (param(widget) != null) {
			final String label = param(widget).getLabel();
			if (label != null && !label.isEmpty()) return label;
		}

		final String name = widget.model().member().getKey();
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	public static String description(final Widget widget) {
		return (param(widget) == null) ? null : param(widget).getDescription();
	}

	public static String style(final Widget widget) {
		return param(widget) == null ? null : param(widget).getWidgetStyle();
	}

	public static boolean isStyle(final Widget widget, final String style) {
		final String widgetStyle = style(widget);
		if (widgetStyle == null) return false;
		return Arrays.asList(widgetStyle.split(",")).contains(style);
	}

	public static int intProperty(final Widget widget, final String propKey,
		final int defaultValue)
	{
		if (param(widget) == null) return defaultValue;
		final Integer value = s2i(param(widget).get(propKey));
		return value == null ? defaultValue : value;
	}

	public static Object minimum(final Widget widget) {
		return param(widget) == null ? null : param(widget).getMinimumValue();
	}

	public static Object maximum(final Widget widget) {
		return param(widget) == null ? null : param(widget).getMaximumValue();
	}

	public static Object softMinimum(final Widget widget) {
		return param(widget) == null ? null : param(widget).getSoftMinimum();
	}

	public static Object softMaximum(final Widget widget) {
		return param(widget) == null ? null : param(widget).getSoftMaximum();
	}

	public static Object stepSize(final Widget widget) {
		return param(widget) == null ? null : param(widget).getStepSize();
	}

	// -- Helper methods --

	private static Integer s2i(final String s) {
		if (s == null) return null;
		try {
			return Integer.parseInt(s);
		}
		catch (final NumberFormatException exc) {
			return null;
		}
	}
}
