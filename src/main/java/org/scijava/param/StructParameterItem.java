
package org.scijava.param;

import java.lang.reflect.Type;
import java.util.List;

import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInfo;
import org.scijava.struct.StructItem;

/**
 * {@link ParameterItem} with nested fields, implementing {@link StructInfo}.
 *
 * @author Curtis Rueden
 * @param <T> Type of the item.
 */
public class StructParameterItem<T> implements ParameterItem<T>,
	StructInfo<StructItem<?>>
{

	private ParameterItem<T> item;
	private StructInfo<StructItem<?>> info;

	public StructParameterItem(final ParameterItem<T> item,
		final StructInfo<StructItem<?>> info)
	{
		this.item = item;
		this.info = info;
	}

	// -- StructParameterItem methods --

	public ParameterItem<T> unwrap() {
		return item;
	}

	// -- StructInfo methods --

	@Override
	public List<StructItem<?>> items() {
		return info.items();
	}

	@Override
	public <C> Struct<C> structOf(final C o) {
		return info.structOf(o);
	}

	// -- StructItem methods --

	@Override
	public String getKey() {
		return item.getKey();
	}

	@Override
	public Type getType() {
		return item.getType();
	}

	@Override
	public ItemIO getIOType() {
		return item.getIOType();
	}

	@Override
	public Class<T> getRawType() {
		return item.getRawType();
	}

	// -- ParameterItem methods --

	@Override
	public ItemVisibility getVisibility() {
		return item.getVisibility();
	}

	@Override
	public boolean isAutoFill() {
		return item.isAutoFill();
	}

	@Override
	public boolean isRequired() {
		return item.isRequired();
	}

	@Override
	public boolean isInput() {
		return item.isInput();
	}

	@Override
	public boolean isPersisted() {
		return item.isPersisted();
	}

	@Override
	public boolean isOutput() {
		return item.isOutput();
	}

	@Override
	public String getPersistKey() {
		return item.getPersistKey();
	}

	@Override
	public String getInitializer() {
		return item.getInitializer();
	}

	@Override
	public String getValidater() {
		return item.getValidater();
	}

	@Override
	public String getCallback() {
		return item.getCallback();
	}

	@Override
	public String getWidgetStyle() {
		return item.getWidgetStyle();
	}

	@Override
	public Object getDefaultValue() {
		return item.getDefaultValue();
	}

	@Override
	public Object getMinimumValue() {
		return item.getMinimumValue();
	}

	@Override
	public Object getMaximumValue() {
		return item.getMaximumValue();
	}

	@Override
	public Object getSoftMinimum() {
		return item.getSoftMinimum();
	}

	@Override
	public Object getSoftMaximum() {
		return item.getSoftMaximum();
	}

	@Override
	public Object getStepSize() {
		return item.getStepSize();
	}

	@Override
	public List<Object> getChoices() {
		return item.getChoices();
	}

	@Override
	public String getLabel() {
		return item.getLabel();
	}

	@Override
	public String getDescription() {
		return item.getDescription();
	}

	@Override
	public boolean has(String key) {
		return item.has(key);
	}

	@Override
	public String get(String key) {
		return item.get(key);
	}
}
