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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.scijava.events.Subscription;
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
				inject(s);
				// NB: a service's lifetime is the context's, so its handlers need
				// no cleanup by the author: disposing the context closes the bus.
				subscribe(s);
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

	/**
	 * Fills in the {@link Dependency}-annotated fields of the given object.
	 * <p>
	 * Services and plugins the context creates are injected automatically; this
	 * is for objects it did not create.
	 * </p>
	 *
	 * @param target the object whose dependencies to fill in
	 * @throws ServiceException if a required dependency is missing, or the
	 *           target's package is not open to this module
	 */
	public void inject(final Object target) {
		if (target == null) throw new NullPointerException("target");
		for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
			for (final Field field : c.getDeclaredFields()) {
				final Dependency dependency = field.getAnnotation(Dependency.class);
				if (dependency == null) continue;
				injectField(target, field, dependency);
			}
		}
	}

	/**
	 * Subscribes the {@link EventHandler}-annotated methods of the given object
	 * to this context's {@link EventBus}.
	 * <p>
	 * Services are subscribed automatically when the context creates them, and
	 * unsubscribed when it is disposed. This is for everything else: a plugin
	 * may be created and discarded many times in a session, so its handlers are
	 * the caller's to manage.
	 * </p>
	 *
	 * @param target the object whose handlers to subscribe
	 * @return a subscription per handler; close them when the object is done
	 * @throws ServiceException if a handler is malformed, or the target's
	 *           package is not open to this module
	 */
	public List<Subscription> subscribe(final Object target) {
		if (target == null) throw new NullPointerException("target");
		final List<Subscription> subscriptions = new ArrayList<>();
		for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
			for (final Method method : c.getDeclaredMethods()) {
				final EventHandler handler = method.getAnnotation(EventHandler.class);
				if (handler == null) continue;
				subscriptions.add(subscribeHandler(target, method, handler));
			}
		}
		return subscriptions;
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

	/** Fills in one annotated field. */
	private void injectField(final Object target, final Field field,
		final Dependency dependency)
	{
		final Object value;
		try {
			value = resolve(field.getType());
		}
		catch (final NoSuchServiceException exc) {
			if (!dependency.required()) return;
			throw new ServiceException("Cannot satisfy " + target.getClass()
				.getName() + "." + field.getName() + ": no service of type " + field
					.getType().getName(), exc);
		}
		try {
			// NB: deep reflection, so the target's package must be open to this
			// module. `opens` is not `exports`: the package stays invisible to
			// ordinary callers.
			field.setAccessible(true);
			field.set(target, value);
		}
		catch (final RuntimeException | IllegalAccessException exc) {
			throw new ServiceException("Cannot inject " + target.getClass()
				.getName() + "." + field.getName() + //
				". Does its module declare `opens " + target.getClass()
					.getPackageName() + " to org.scijava.context;`?", exc);
		}
	}

	/** Subscribes one annotated method. */
	private Subscription subscribeHandler(final Object target,
		final Method method, final EventHandler handler)
	{
		final Class<?>[] params = method.getParameterTypes();
		if (params.length != 1) {
			throw new ServiceException("An @EventHandler takes exactly one" + //
				" parameter, the event: " + method.getDeclaringClass().getName() + //
				"." + method.getName() + " takes " + params.length);
		}
		try {
			// NB: the same deep reflection, and the same `opens`, that
			// @Dependency needs.
			method.setAccessible(true);
		}
		catch (final RuntimeException exc) {
			throw new ServiceException("Cannot subscribe " + method
				.getDeclaringClass().getName() + "." + method.getName() + //
				". Does its module declare `opens " + method.getDeclaringClass()
					.getPackageName() + " to org.scijava.context;`?", exc);
		}
		return subscribeTyped(params[0], handler.priority(), target, method);
	}

	/** Captures the event type, so that the bus sees a typed subscription. */
	private <E> Subscription subscribeTyped(final Class<E> eventType,
		final double priority, final Object target, final Method method)
	{
		return events.subscribe(eventType, priority, event -> {
			try {
				method.invoke(target, event);
			}
			catch (final IllegalAccessException exc) {
				throw new ServiceException("Cannot invoke handler " + method
					.getDeclaringClass().getName() + "." + method.getName(), exc);
			}
			catch (final InvocationTargetException exc) {
				// NB: unwrap, so subscribers see what the handler actually threw
				// rather than a reflection wrapper.
				final Throwable cause = exc.getCause();
				if (cause instanceof RuntimeException) throw (RuntimeException) cause;
				if (cause instanceof Error) throw (Error) cause;
				throw new ServiceException("Handler failed: " + method
					.getDeclaringClass().getName() + "." + method.getName(), cause);
			}
		});
	}

	/** Resolves what to inject for a field of the given type. */
	private Object resolve(final Class<?> type) {
		if (type == Context.class) return this;
		if (type == EventBus.class) return events;
		if (Service.class.isAssignableFrom(type)) {
			return service(type.asSubclass(Service.class));
		}
		throw new NoSuchServiceException(type);
	}

	/**
	 * Builds the index-backed plugin discoverer.
	 * <p>
	 * NB: which type a plugin provides has to be answerable from the index
	 * alone, hence the function: reading {@code type()} from the recorded
	 * annotation loads the <em>declared</em> type, never the implementation.
	 * </p>
	 */
	private IndexDiscoverer<Plugin> createPluginDiscoverer() {
		return new IndexDiscoverer<>(Plugin.class, //
			item -> item.annotation().type().getName(), //
			item -> attrsOf(item.annotation()), //
			item -> item.annotation().priority(), //
			Thread.currentThread().getContextClassLoader(), //
			this::instantiate);
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
	private Object instantiate(final Class<?> type) {
		final Object plugin;
		try {
			plugin = type.getDeclaredConstructor().newInstance();
		}
		catch (final ReflectiveOperationException exc) {
			throw new ServiceException("Cannot construct plugin: " + //
				type.getName() + ". Does its module declare `opens " + type
					.getPackageName() + " to org.scijava.context;`?", exc);
		}
		inject(plugin);
		return plugin;
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
