/*
 * #%L
 * An application container: services, discovered and wired.
 * %%
 * Copyright (C) 2026 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.Set;
import java.util.stream.Collectors;

import org.scijava.discovery.Discovery;
import org.scijava.events.EventBus;
import org.scijava.index.IndexDiscoverer;
import org.scijava.spi.Disposable;

/**
 * An application container: a set of {@link Service}s, discovered and wired,
 * with one {@link EventBus} of their own.
 * <p>
 * Services are created lazily. Asking for one constructs it, initializes it,
 * and constructs whatever it asks for in turn; a service nobody wants is never
 * built. This differs from SciJava Common, which instantiated every service it
 * could find when the context was created.
 * </p>
 * <p>
 * Several contexts may exist at once and are wholly independent: separate
 * services, separate event buses. Nothing here is static.
 * </p>
 *
 * @author Curtis Rueden
 */
public class Context implements Disposable, AutoCloseable {

	private final List<Provider<Service>> providers;

	/** Services by the type they were requested as. */
	private final Map<Class<?>, Service> byRequestedType = new LinkedHashMap<>();

	/** Services in creation order, so that disposal can reverse it. */
	private final List<Service> created = new ArrayList<>();

	/** Services given explicitly to {@link #of}, not yet matched to a type. */
	private final List<Service> explicit = new ArrayList<>();

	/** Services already initialized, or in the middle of initializing. */
	private final Set<Service> initialized = Collections.newSetFromMap(
		new IdentityHashMap<>());

	private final EventBus events = EventBus.create();

	/** Plugin discovery, from the annotation index. Built on first use. */
	private IndexDiscoverer<Plugin> pluginDiscoverer;

	private boolean disposed;

	private Context(final List<Provider<Service>> providers) {
		this.providers = providers;
	}

	/**
	 * Creates a context over the services available to the runtime.
	 *
	 * @return the new context
	 */
	public static Context create() {
		// NB: the lookup is on Service itself, which this module declares `uses`
		// for. See Service's documentation for why it cannot be otherwise.
		return new Context(ServiceLoader.load(Service.class).stream() //
			.collect(Collectors.toList()));
	}

	/**
	 * Creates a context containing exactly the given services, bypassing
	 * discovery. Useful for tests and for embedding.
	 *
	 * @param services the services this context will contain
	 * @return the new context
	 */
	public static Context of(final Service... services) {
		final Context context = new Context(Collections.emptyList());
		// NB: register everything before initializing anything. A service may
		// depend on one declared after it, and initialization is where
		// dependencies are acquired.
		context.explicit.addAll(List.of(services));
		return context;
	}

	/**
	 * Gets the service of the given type, creating it if necessary.
	 *
	 * @param <S> the service type
	 * @param type the interface (or class) the service provides
	 * @return the service
	 * @throws NoSuchServiceException if no service provides that type
	 */
	public <S extends Service> S service(final Class<S> type) {
		return optionalService(type).orElseThrow( //
			() -> new NoSuchServiceException(type));
	}

	/**
	 * Gets the service of the given type, creating it if necessary.
	 *
	 * @param <S> the service type
	 * @param type the interface (or class) the service provides
	 * @return the service, or empty if none provides that type
	 */
	public <S extends Service> Optional<S> optionalService(final Class<S> type) {
		if (type == null) throw new NullPointerException("type");
		synchronized (this) {
			checkNotDisposed();
			final Service existing = byRequestedType.get(type);
			if (existing != null) {
				// NB: this may be a service still in the middle of initializing,
				// when two services depend on each other. See initialize below.
				return Optional.of(type.cast(existing));
			}
			final Optional<S> service = findService(type);
			service.ifPresent(s -> {
				// NB: publish before initializing, so that two services depending
				// on each other resolve instead of recursing.
				byRequestedType.put(type, s);
				initialize(s);
				// NB: record for disposal only once initialization has finished.
				// A service's dependencies finish first, so they land earlier in
				// this list, and disposing in reverse therefore tears down each
				// service before the ones it depends on. Recording on the way in
				// would order them exactly backwards.
				if (!created.contains(s)) created.add(s);
			});
			return service;
		}
	}

	/**
	 * Lists the plugins of the given type.
	 * <p>
	 * The result describes what is available without loading any plugin class:
	 * each {@link Discovery} reports its implementation class name, its
	 * {@link Plugin} metadata and its priority, and loads or constructs the
	 * class only when asked to. Building a menu therefore costs no class
	 * loading at all.
	 * </p>
	 * <p>
	 * Plugins come back highest priority first.
	 * </p>
	 *
	 * @param <P> the extension type
	 * @param type the type of extension wanted
	 * @return the plugins of that type, highest priority first
	 */
	public <P> List<Discovery<P>> plugins(final Class<P> type) {
		if (type == null) throw new NullPointerException("type");
		synchronized (this) {
			checkNotDisposed();
			if (pluginDiscoverer == null) pluginDiscoverer = createPluginDiscoverer();
		}
		final List<Discovery<P>> plugins = new ArrayList<>(pluginDiscoverer
			.discover(type));
		plugins.sort(Comparator.comparingDouble(Discovery<P>::priority).reversed());
		return plugins;
	}

	/** Gets this context's event bus. */
	public EventBus events() {
		return events;
	}

	/**
	 * Gets the services initialized so far, in the order they finished
	 * initializing. A service that failed to initialize is not among them, and
	 * is not disposed.
	 */
	public synchronized List<Service> services() {
		return List.copyOf(created);
	}

	/** Gets whether this context has been disposed. */
	public synchronized boolean isDisposed() {
		return disposed;
	}

	/**
	 * Disposes every service, in reverse order of creation, and closes the
	 * event bus.
	 * <p>
	 * Disposal is reversed so that a service can still reach its dependencies
	 * while shutting down. A service that throws on the way out does not
	 * prevent the others from being disposed.
	 * </p>
	 */
	@Override
	public synchronized void dispose() {
		if (disposed) return;
		disposed = true;
		final List<Throwable> failures = new ArrayList<>();
		for (int i = created.size() - 1; i >= 0; i--) {
			try {
				created.get(i).dispose();
			}
			catch (final Throwable exc) {
				failures.add(exc);
			}
		}
		created.clear();
		byRequestedType.clear();
		// NB: closing the bus is what frees services from arranging their own
		// unsubscription.
		events.close();
		if (!failures.isEmpty()) {
			final ServiceException exc = new ServiceException(failures.size() + //
				" service(s) failed to dispose", failures.get(0));
			for (int i = 1; i < failures.size(); i++)
				exc.addSuppressed(failures.get(i));
			throw exc;
		}
	}

	@Override
	public void close() {
		dispose();
	}

	// -- Helper methods --

	/**
	 * Builds the index-backed plugin discoverer.
	 * <p>
	 * NB: which type a plugin provides has to be answerable from the index
	 * alone, hence the function: reading {@code type()} from the recorded
	 * annotation loads the <em>declared</em> type, never the implementation.
	 * </p>
	 */
	private static IndexDiscoverer<Plugin> createPluginDiscoverer() {
		return new IndexDiscoverer<>(Plugin.class, //
			item -> item.annotation().type().getName(), //
			item -> attrsOf(item.annotation()), //
			item -> item.annotation().priority(), //
			Thread.currentThread().getContextClassLoader(), //
			Context::instantiate);
	}

	/**
	 * Constructs a plugin.
	 * <p>
	 * NB: this must live in <em>this</em> module. Reflective construction is
	 * checked against the module that performs it, so a plugin package opened
	 * to this container cannot be constructed by the discovery or index module
	 * on our behalf - which is why {@link Plugin} tells users to open their
	 * packages to {@code org.scijava.context}.
	 * </p>
	 */
	private static Object instantiate(final Class<?> type) {
		try {
			return type.getDeclaredConstructor().newInstance();
		}
		catch (final ReflectiveOperationException exc) {
			throw new ServiceException("Cannot construct plugin: " + //
				type.getName() + ". Does its module open the package to" + //
				" org.scijava.context?", exc);
		}
	}

	/** Flattens a plugin's metadata into the discovery's attribute map. */
	private static Map<String, String> attrsOf(final Plugin plugin) {
		final Map<String, String> attrs = new LinkedHashMap<>();
		if (!plugin.name().isEmpty()) attrs.put("name", plugin.name());
		if (!plugin.label().isEmpty()) attrs.put("label", plugin.label());
		if (!plugin.description().isEmpty()) {
			attrs.put("description", plugin.description());
		}
		for (final Attr attr : plugin.attrs())
			attrs.put(attr.name(), attr.value());
		return attrs;
	}

	/**
	 * Initializes a service, unless that is already done or under way.
	 * <p>
	 * NB: the guard is set <em>before</em> initializing, so that two services
	 * depending on each other resolve rather than recursing forever. The
	 * consequence is that a service caught in such a cycle may briefly observe
	 * a peer that has not finished initializing. Where that matters, look the
	 * peer up at point of use rather than holding it from
	 * {@link Service#initialize}.
	 * </p>
	 * <p>
	 * Cycles are resolvable here only because dependencies are acquired during
	 * initialization rather than in the constructor. Constructor injection
	 * could not resolve them at all.
	 * </p>
	 */
	private void initialize(final Service service) {
		if (!initialized.add(service)) return;
		try {
			service.initialize(this);
		}
		catch (final Throwable exc) {
			throw new ServiceException("Cannot initialize service: " + //
				service.getClass().getName(), exc);
		}
	}

	/**
	 * Finds the highest-priority service providing the given type, among those
	 * given explicitly and those discovered.
	 * <p>
	 * NB: discovered candidates are constructed in order to compare their
	 * priorities, and all but the winner are discarded uninitialized.
	 * {@link Service} requires constructors to be free of side effects for
	 * exactly this reason.
	 * </p>
	 */
	private <S extends Service> Optional<S> findService(final Class<S> type) {
		Service best = null;
		for (final Service service : explicit) {
			if (!type.isInstance(service)) continue;
			if (best == null || service.priority() > best.priority()) best = service;
		}
		for (final Provider<Service> provider : providers) {
			// NB: type() loads the class but does not construct it, so
			// implementations of other types cost nothing here.
			if (!type.isAssignableFrom(provider.type())) continue;
			final Service candidate;
			try {
				candidate = provider.get();
			}
			catch (final Throwable exc) {
				throw new ServiceException("Cannot construct service: " + //
					provider.type().getName(), exc);
			}
			if (best == null || candidate.priority() > best.priority()) best =
				candidate;
		}
		return Optional.ofNullable(type.cast(best));
	}

	private void checkNotDisposed() {
		if (disposed) {
			throw new IllegalStateException("This context has been disposed");
		}
	}
}
