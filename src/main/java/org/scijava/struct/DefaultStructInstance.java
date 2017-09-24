
package org.scijava.struct;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class DefaultStructInstance<O> implements StructInstance<O> {

	private final Struct struct;
	private final O object;

	private final TreeMap<String, MemberInstance<?>> memberMap;

	public DefaultStructInstance(final Struct struct, final O object) {
		this.struct = struct;
		this.object = object;
		memberMap = new TreeMap<>();
		for (final Member<?> member : struct.members()) {
			memberMap.put(member.getKey(), createMemberInstance(member));
		}
		// TODO see about making this for loop into a stream collect whatever
//		memberMap = struct.members().stream().collect(
//			Collectors.toMap(Member::getKey, member -> createMemberInstance(member)));
	}
	
	private <T> MemberInstance<T> createMemberInstance(Member<T> member) {
		// FIXME how to instantiate?
		// START HERE - this is bad
		return new ValueAccessibleMemberInstance<>((Member<T> & ValueAccessible<T>) member, object());
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
