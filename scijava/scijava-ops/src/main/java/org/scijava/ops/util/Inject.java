package org.scijava.ops.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.scijava.command.Command;
import org.scijava.ops.OpDependencyMember;
import org.scijava.ops.struct.ClassOpDependencyMemberParser;
import org.scijava.ops.struct.ClassParameterMemberParser;
import org.scijava.struct.ItemIO;
import org.scijava.struct.Struct;
import org.scijava.struct.StructInstance;
import org.scijava.struct.ValueAccessibleMemberInstance;

public class Inject {

	private Inject() {
		// NB: Prevent instantiation of utility class.
	}

	public static class Structs {
		private Structs() {
		}
		
		public static boolean isInjectable(final StructInstance<?> instance) {
			// HACK: Exclude Op dependencies since they were already injected when
			// constructing the instance.
			return !filterAccessibles(getAccessibles(instance), mi -> !(mi
				.member() instanceof OpDependencyMember)).isEmpty();
		}
		
		public static void inputs(StructInstance<?> instance, Object... objs) {
			unsafe(filterAccessibles(getAccessibles(instance), m -> {
				ItemIO ioType = m.member().getIOType();
				return EnumSet.of(ItemIO.INPUT, ItemIO.CONTAINER, ItemIO.MUTABLE).contains(ioType);
			}), objs);
		}

		public static void outputs(StructInstance<?> instance, Object... objs) {
			unsafe(filterAccessibles(getAccessibles(instance), m -> {
				ItemIO ioType = m.member().getIOType();
				return EnumSet.of(ItemIO.OUTPUT, ItemIO.CONTAINER, ItemIO.MUTABLE).contains(ioType);
			}), objs);
		}

		public static void all(StructInstance<?> instance, Object... objs) {
			unsafe(getAccessibles(instance), objs);
		}

		private static void unsafe(List<ValueAccessibleMemberInstance<?>> accessibles, Object... objs) {
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

		private static List<ValueAccessibleMemberInstance<?>> getAccessibles(StructInstance<?> instance) {
			return instance.members().stream() //
					.filter(ValueAccessibleMemberInstance.class::isInstance) //
					.map(m -> (ValueAccessibleMemberInstance<?>)m) //
					.collect(Collectors.toList());
		}

		private static List<ValueAccessibleMemberInstance<?>> filterAccessibles(
				List<ValueAccessibleMemberInstance<?>> accessibles,
				Predicate<? super ValueAccessibleMemberInstance<?>> condition) {
			return accessibles.stream().filter(condition) //
					.collect(Collectors.toList());
		}
	}

	public static class Commands {
		private Commands() {
		}

		public static void inputs(Command command, Object... objs) {
			Structs.inputs(commandToStructInstance(command), objs);
		}

		public static void outputs(Command command, Object... objs) {
			Structs.outputs(commandToStructInstance(command), objs);
		}

		public static void all(Command command, Object... objs) {
			Structs.all(commandToStructInstance(command), objs);
		}

		public static StructInstance<Command> commandToStructInstance(Command command) {
			Struct s = org.scijava.struct.Structs.from(command.getClass(),
				new ArrayList<>(), new ClassParameterMemberParser(),
				new ClassOpDependencyMemberParser());
			return s.createInstance(command);
		}
	}
}
