
package org.scijava.struct;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultStructInstance<O> implements StructInstance<O> {

	private final Struct struct;
	private final O object;

	private final LinkedHashMap<String, MemberInstance<?>> memberMap;

	public DefaultStructInstance(final Struct struct, final O object) {
		this.struct = struct;
		this.object = object;
		memberMap = new LinkedHashMap<>();
		for (final Member<?> member : struct.members()) {
			memberMap.put(member.getKey(), member.createInstance(object));
		}
	}
	
	@Override
	public List<MemberInstance<?>> members() {
		return memberMap.values().stream().collect(Collectors.toList());
	}

	@Override
	public Struct struct() {
		return struct;
	}

	@Override
	public O object() {
		return object;
	}

	@Override
	public MemberInstance<?> member(final String key) {
		return memberMap.get(key);
	}
}
