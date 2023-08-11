package org.scijava.ops.api;

import org.scijava.struct.ItemIO;
import org.scijava.struct.Member;

import java.util.ArrayList;
import java.util.List;

/**
 * Static utility class for formatting descriptions of Ops
 */
public final class OpDescription {

    /**
     * Generates a basic {@link String} describing the given {@link OpInfo}
     *
     * @param info the {@link OpInfo} of interest
     * @return a descriptor for this {@link OpInfo}
     */
    public static String basic(final OpInfo info) {
        return basic(info, null);
    }

    /**
     * Writes a basic {@link String} describing the {@link OpInfo} of interest
     * <b>with a particular {@link Member} highlighted</b>.
     *
     * @param info the {@link OpInfo} of interest
     * @param special the {@link Member} to highlight
     * @return a descriptor for this {@link OpInfo}
     */
    public static String basic(final OpInfo info, final Member<?> special) {
        final StringBuilder sb = new StringBuilder();
        final List<String> names = info.names();
        sb.append(names.get(0)).append("(\n\t Inputs:\n");
        List<Member<?>> containers = new ArrayList<>();
        for (final Member<?> arg : info.inputs()) {
            if (arg.getIOType() == ItemIO.INPUT) appendParam(sb, arg, special);
            else containers.add(arg);
        }
        if (containers.isEmpty()) {
            sb.append("\t Outputs:\n");
            appendParam(sb, info.output(), special);
        } else {
            sb.append("\t Containers (I/O):\n");
            containers.forEach(c -> appendParam(sb, c, special));
        }
        sb.append(")\n");
        if (names.size() > 1) {
            sb.append("Aliases: [");
            sb.append(String.join(", ", names.subList(1, names.size())));
            sb.append("]\n");
        }
        return sb.toString();
    }

    /**
     * Appends a {@link Member} to the {@link StringBuilder} writing the Op
     * string.
     *
     * @param sb      the {@link StringBuilder}
     * @param arg     the {@link Member} being appended to {@code sb}
     * @param special the {@link Member} to highlight
     */
    private static void appendParam(final StringBuilder sb, final Member<?> arg,
                             final Member<?> special) {
        if (arg == special) sb.append("==> \t"); // highlight special item
        else sb.append("\t\t");
        sb.append(arg.getType().getTypeName());
        sb.append(" ");
        sb.append(arg.getKey());
        if (!arg.isRequired()) sb.append("?");
        if (!arg.getDescription().isEmpty()) {
            sb.append(" -> ");
            sb.append(arg.getDescription());
        }
        sb.append("\n");
    }
}
