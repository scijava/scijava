package org.scijava.ops.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.scijava.command.Command;
import org.scijava.param.ParameterStructs;
import org.scijava.param.ValidityException;
import org.scijava.struct.StructInstance;
import org.scijava.struct.ValueAccessibleMemberInstance;

public class Inject {

	private Inject() {
		// NB: Prevent instantiation of utility class.
	}

	public static class Structs {
		private Structs() {
		}

		public static void unsafe(StructInstance<?> instance, Object... objs) {
			unsafe(filterAccessible(instance), objs);
		}

		public static void unsafe(List<ValueAccessibleMemberInstance<?>> accessibles, Object... objs) {
			if (accessibles.size() != objs.length) {
				throw new IllegalArgumentException("The number of provided instances to inject: " + accessibles.size()
						+ " does not match " + "the number of provided objects: " + objs.length);
			}
			int i = 0;
			for (ValueAccessibleMemberInstance<?> accessible : accessibles) {
				try {
					accessible.set(objs[i]);
				} catch (Exception e) {
					throw new IllegalArgumentException("Can't inject member with type: '"
							+ accessible.member().getType() + "' with object of type: '" + objs[i].getClass() + "'", e);
				}
				i++;
			}
		}

		private static List<ValueAccessibleMemberInstance<?>> filterAccessible(StructInstance<?> instance) {
			return StreamSupport //
					.stream(instance.spliterator(), false) //
					.filter(ValueAccessibleMemberInstance.class::isInstance) //
					.map(m -> (ValueAccessibleMemberInstance<?>)m) //
					.collect(Collectors.toList());
		}
	}

	public static class Commands {
		private Commands() {
		}

		public static void unsafe(Command command, Object... objs) {
			StructInstance<Command> instance = null;
			try {
				instance = ParameterStructs.create(command);
			} catch (ValidityException e) {
				throw new IllegalArgumentException("Can't inject command", e);
			}
			Structs.unsafe(instance, objs);
		}
	}
}
