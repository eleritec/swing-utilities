package net.eleritec.swing.util.event;

import static net.eleritec.swing.util.Utils.asList;
import static net.eleritec.swing.util.Utils.map;
import static net.eleritec.swing.util.event.EventTypes.ActionEvents.PERFORMED;
import static net.eleritec.swing.util.event.EventTypes.DocumentEvents.CHANGED;
import static net.eleritec.swing.util.event.EventTypes.DocumentEvents.INSERTED;
import static net.eleritec.swing.util.event.EventTypes.DocumentEvents.REMOVED;
import static net.eleritec.swing.util.event.EventTypes.KeyEvents.TYPED;
import static net.eleritec.swing.util.event.EventTypes.MouseEvents.CLICKED;
import static net.eleritec.swing.util.event.EventTypes.MouseEvents.DRAGGED;
import static net.eleritec.swing.util.event.EventTypes.MouseEvents.ENTERED;
import static net.eleritec.swing.util.event.EventTypes.MouseEvents.EXITED;
import static net.eleritec.swing.util.event.EventTypes.MouseEvents.MOVED;
import static net.eleritec.swing.util.event.EventTypes.MouseEvents.PRESSED;
import static net.eleritec.swing.util.event.EventTypes.MouseEvents.RELEASED;
import static net.eleritec.swing.util.event.EventTypes.MouseEvents.WHEEL;
import static net.eleritec.swing.util.event.EventTypes.WindowEvents.ACTIVATED;
import static net.eleritec.swing.util.event.EventTypes.WindowEvents.CLOSED;
import static net.eleritec.swing.util.event.EventTypes.WindowEvents.CLOSING;
import static net.eleritec.swing.util.event.EventTypes.WindowEvents.DEACTIVATED;
import static net.eleritec.swing.util.event.EventTypes.WindowEvents.DEICONIFIED;
import static net.eleritec.swing.util.event.EventTypes.WindowEvents.FOCUS_GAINED;
import static net.eleritec.swing.util.event.EventTypes.WindowEvents.FOCUS_LOST;
import static net.eleritec.swing.util.event.EventTypes.WindowEvents.ICONIFIED;
import static net.eleritec.swing.util.event.EventTypes.WindowEvents.OPENED;
import static net.eleritec.swing.util.event.EventTypes.WindowEvents.STATE_CHANGED;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import net.eleritec.swing.util.Utils;
import net.eleritec.swing.util.event.EventTypes.ActionEvents;
import net.eleritec.swing.util.event.EventTypes.DocumentEvents;
import net.eleritec.swing.util.event.EventTypes.FocusEvents;
import net.eleritec.swing.util.event.EventTypes.KeyEvents;
import net.eleritec.swing.util.event.EventTypes.MouseEvents;
import net.eleritec.swing.util.event.EventTypes.WindowEvents;

public class EventListeners {

	public static void onAction(Object source, Consumer<ActionEvent> listener) {
		actions(source).onAction(listener).listen();
	}
	
	public static void onAction(Object source, Runnable listener) {
		onAction(source, e->listener.run());
	}
	
	public static void onDocument(Object source, Consumer<DocumentEvent> listener, DocumentEvents...types) {
		document(source).onEvent(listener, types).listen();
	}
	
	public static void onDocument(Object source, Runnable listener, DocumentEvents...types) {
		onDocument(source, e->listener.run(), types);
	}
	
	public static void onMouse(Component component, Consumer<MouseEvent> listener, MouseEvents...types) {
		mouse(component).onMouse(listener, types).listen();
	}
	
	public static void onMouse(Component component, Runnable listener, MouseEvents...types) {
		onMouse(component, e->listener.run(), types);
	}
	
	public static void onKey(Component component, Consumer<KeyEvent> listener, KeyEvents...types) {
		keys(component).onKey(listener, types).listen();
	}
	
	public static void onKey(Component component, Runnable listener, KeyEvents...types) {
		onKey(component, e->listener.run(), types);
	}
	
	public static void onMouseWheel(Component component, Consumer<MouseWheelEvent> listener) {
		mouse(component).onWheel(listener).listen();
	}
	
	public static void onMouseWheel(Component component, Runnable listener) {
		onMouseWheel(component, e->listener.run());
	}
	
	public static void onWindow(Window window, Consumer<WindowEvent> listener, WindowEvents...types) {
		window(window).onWindow(listener, types).listen();
	}
	
	public static void onWindow(Window window, Runnable listener, WindowEvents...types) {
		onWindow(window, e->listener.run(), types);
	}
	
	public static void onFocus(Component component, Consumer<FocusEvent> listener, FocusEvents...types) {
		focus(component).onFocus(listener, types).listen();
	}
	
	public static void onFocus(Component component, Runnable listener, FocusEvents...types) {
		onFocus(component, e->listener.run(), types);
	}
	
	public static KeyListeners keys(Component...sources) {
		return new KeyListeners(sources);
	}
	
	public static MouseListeners mouse(Component...sources) {
		return new MouseListeners(sources);
	}
	
	public static WindowListeners window(Window...sources) {
		return new WindowListeners(sources);
	}
	
	public static FocusListeners focus(Component...sources) {
		return new FocusListeners(sources);
	}
	
	public static ActionListeners actions(Object...sources) {
		return new ActionListeners(sources);
	}
	
	public static DocumentListeners document(Object...sources) {
		return new DocumentListeners(sources);
	}
	
	public static interface ActionSource { void addActionListener(ActionListener listener); }
	
	public static class ActionListeners extends EventSubscriptionBuilder<EventTypes.ActionEvents, ActionListener, ActionSource> {
		
		public ActionListeners(Object...sources) {
			super(ActionEvents.class, getDuckTypes(ActionSource.class, sources));
		}
		
		@SafeVarargs
		public final ActionListeners onAction(Consumer<ActionEvent>...handlers) {
			return (ActionListeners)register(Arrays.asList(handlers), PERFORMED);
		}
		
		@SafeVarargs
		public final ActionListeners onAction(Runnable...handlers) {
			List<Consumer<ActionEvent>> listeners = Arrays.stream(handlers)
					.map(h->(Consumer<ActionEvent>)e->h.run()).collect(Collectors.toList());
			return (ActionListeners)register(listeners, PERFORMED);
		}

		@Override
		protected void bind(ActionSource source, ActionListener adapter) {
			bind(source::addActionListener, adapter, PERFORMED);		
		}
	}
	
	public static interface DocumentSource { Document getDocument(); }
	
	public static class DocumentListeners extends EventSubscriptionBuilder<DocumentEvents, DocumentListener, DocumentSource> {
		
		public DocumentListeners(Object...docSources) {
			super(DocumentEvents.class, asList(map(docSources, s->toDocumentSource(s))));
		}
		
		private static DocumentSource toDocumentSource(Object source) {
			if(source instanceof Document) {
				return DuckType.createWrapper(source, DocumentSource.class);
			}
			return getDuckType(DocumentSource.class, source);
		}
		
		@SafeVarargs
		public final DocumentListeners onEvent(Consumer<DocumentEvent> handler, DocumentEvents...types) {
			return (DocumentListeners)register(handler, types);
		}

		@Override
		protected void bind(DocumentSource source, DocumentListener adapter) {
			bind(source.getDocument()::addDocumentListener, adapter, INSERTED, REMOVED, CHANGED);	
		}
	}
	
	public static interface MouseAdapter extends MouseListener, MouseWheelListener, MouseMotionListener {}

	public static class MouseListeners extends EventSubscriptionBuilder<MouseEvents, MouseAdapter, Component>{

		public MouseListeners(Component...components) {
			super(MouseEvents.class, components);
		}
		
		public MouseListeners onMouse(Consumer<MouseEvent> handler, MouseEvents...types) {
			return (MouseListeners)register(handler, t->t!=WHEEL, types);
		}
		
		public MouseListeners onWheel(Consumer<MouseWheelEvent> handler) {
			return (MouseListeners)register(handler, WHEEL);
		}

		@Override
		protected void bind(Component component, MouseAdapter adapter) {
			bind(component::addMouseListener, adapter, CLICKED, PRESSED, RELEASED, ENTERED, EXITED);
			bind(component::addMouseMotionListener, adapter, MOVED, DRAGGED);
			bind(component::addMouseWheelListener, adapter, WHEEL);
		}
	}
	
	public static class KeyListeners extends EventSubscriptionBuilder<KeyEvents, KeyListener, Component> {
		public KeyListeners(Component...sources) {
			super(KeyEvents.class, sources);
		}
		
		public KeyListeners onKey(Consumer<KeyEvent> handler, KeyEvents...types) {
			return (KeyListeners)register(handler, types);
		}

		@Override
		protected void bind(Component source, KeyListener adapter) {
			bind(source::addKeyListener, adapter, TYPED, KeyEvents.PRESSED, KeyEvents.RELEASED);
		}		
	}
	
	public static interface WindowAdapter extends WindowListener, WindowStateListener, WindowFocusListener {}
	
	public static class WindowListeners extends EventSubscriptionBuilder<WindowEvents, WindowAdapter, Window> {

		public WindowListeners(Window...windows) {
			super(WindowEvents.class, windows);
		}
		
		public WindowListeners onWindow(Consumer<WindowEvent> handler, WindowEvents...types) {
			return (WindowListeners)register(handler, types);
		}
		
		@Override
		protected void bind(Window window, WindowAdapter adapter) {
			bind(window::addWindowListener, adapter, OPENED, CLOSING, CLOSED, ICONIFIED, DEICONIFIED, ACTIVATED, DEACTIVATED);
			bind(window::addWindowStateListener, adapter, STATE_CHANGED);
			bind(window::addWindowFocusListener, adapter, FOCUS_GAINED, FOCUS_LOST);
		}
	}
	
	public static class FocusListeners extends EventSubscriptionBuilder<FocusEvents, FocusListener, Component> {
		
		public FocusListeners(Component...sources) {
			super(FocusEvents.class, sources);
		}
		
		public FocusListeners onFocus(Consumer<FocusEvent> handler, FocusEvents...types) {
			return (FocusListeners)register(handler, types);
		}

		@Override
		protected void bind(Component source, FocusListener adapter) {
			bind(source::addFocusListener, adapter, FocusEvents.GAINED, FocusEvents.LOST);
		}
	}

	
	public static abstract class EventSubscriptionBuilder<T extends EventTypes, L extends EventListener, S> {

		private Class<T> eventClass;
		private Map<T, List<?>> handlers = new HashMap<>();
		private List<S> sources = new ArrayList<S>();
		
		protected abstract void bind(S source, L adapter);	
		
		protected EventSubscriptionBuilder(Class<T> eventClass, Collection<S> sources) {
			if(!eventClass.isEnum() || eventClass.getEnumConstants().length==0) {
				throw new IllegalArgumentException("'eventClass' parameter must be a type of Enum with at least one constant.");
			}
			this.eventClass = eventClass;
			addSources(sources);
		}
		
		protected EventSubscriptionBuilder(Class<T> eventClass, S[] sources) {
			this(eventClass, Arrays.asList(sources));
		}
		
		@SafeVarargs
		protected final <E> EventSubscriptionBuilder<T, L, S> register(Consumer<E> handler, T...types) {
			return register(handler, null, types);
		}
		
		@SafeVarargs
		protected final <E> EventSubscriptionBuilder<T, L, S> register(Collection<Consumer<E>> handlers, T...types) {
			handlers.forEach(h->register(h, types));
			return this;
		}
		
		@SafeVarargs
		protected final <E> EventSubscriptionBuilder<T, L, S> register(Collection<Consumer<E>> handlers, Predicate<T> filter, T...types) {
			handlers.forEach(h->register(h, filter, types));
			return this;
		}
		
		@SafeVarargs
		protected final <E> EventSubscriptionBuilder<T, L, S> register(Consumer<E> handler, Predicate<T> filter, T...types) {
			if(handler!=null) {
				Utils.filter(types, filter).forEach(type->addEventHandler(handler, type));
			}
			return this;
		}
		
		private <E> void addEventHandler(Consumer<E> handler, T type) {
			List<Consumer<E>> handlers = getHandlers(type, getEventClass(type));
			handlers.add(handler);
		}
		
		@SuppressWarnings("unchecked")
		private <E> Class<E> getEventClass(T type) {
			return (Class<E>) type.getEventType();
		}
		
		@SuppressWarnings("unchecked")
		protected <E> List<Consumer<E>> getHandlers(T type, Class<E> eventType) {
			return (List<Consumer<E>>) handlers.computeIfAbsent(type, t->new ArrayList<>());
		}
		
		public void listen() {
			L adapter = createListener();
			sources.forEach(s->bind(s, adapter));
		}
		
		@SafeVarargs
		protected final <H> void bind(Consumer<H> subscriber, H handler, T...types) {
			if(Arrays.stream(types).anyMatch(this::hasHandlers)) {
				subscriber.accept(handler);
			}
		}
		
		private boolean hasHandlers(T type) {
			return !getHandlers(type, type.getEventType()).isEmpty();
		}
		
		protected <E> void processEvent(T type, E event) {
			Class<E> eventClass = getEventClass(type);
			if(event!=null && eventClass!=null && eventClass.isAssignableFrom(event.getClass())) {
				processEvent(getHandlers(type, eventClass), event);
			}
		}
		
		protected <E> void processEvent(List<Consumer<E>> listeners, E event) {
			listeners.forEach(l->l.accept(event));
		}
		
		protected boolean accept(S source) {
			return source!=null && !this.sources.contains(source);
		}
		
		@SafeVarargs
		public final EventSubscriptionBuilder<T, L, S> addSources(S...sources) {
			return addSources(Arrays.asList(sources));
		}
		
		public final EventSubscriptionBuilder<T, L, S> addSources(Collection<S> sources) {
			this.sources.addAll(asList(sources, this::accept));
			return this;
		}
		
		public List<S> getSources() {
			return new ArrayList<S>(sources);
		}
		
		@SuppressWarnings("unchecked")
		protected L createListener() {
			Class<L> contract = getAdapterType();
			return (L)Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {contract}, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					T type = EventTypes.getByHandler(eventClass, method.getName());
					if(type!=null) {
						Object event = args.length==0? null: args[0];
						processEvent(type, event);
					}
					return null;
				}
			});	
		}

		@SuppressWarnings("unchecked")
		public Class<L> getAdapterType() {
			return (Class<L>) eventClass.getEnumConstants()[0].getHandlerInterface();
		}
	}
	
	public static <I> List<I> getDuckTypes(Class<I> interfaceType, Object...targets) {
		return asList(map(targets, t->getDuckType(interfaceType, t)));
	}
	
	@SuppressWarnings("unchecked")
	public static <I> I getDuckType(Class<I> interfaceType, Object target) {
		if(target!=null && interfaceType.isAssignableFrom(target.getClass())) {
			return (I)target;
		}
		
		final boolean valid = DuckType.isDuckType(target, interfaceType);
		return (I)Proxy.newProxyInstance(DuckType.class.getClassLoader(), new Class[] {interfaceType}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if(valid) {
					Method tMethod = DuckType.findMethod(target, method.getName(), method.getParameterTypes());
					return tMethod.invoke(target, args);
				}
				return null;
			}
		});	
	}
	
	public static class DuckType<I> {
		
		private static boolean isDuckType(Object target, Class<?> interfaceType) {
			if(target==null || !interfaceType.isInterface()) {
				return false;
			}
			
			for(Method m: getInterfaceMethods(interfaceType)) {
				if(findMethod(target, m.getName(), m.getParameterTypes())==null) {
					return false;
				}
			}
			return true;
		}
		
		private static List<Method> getInterfaceMethods(Class<?> iface) {
			List<Method> methods = new ArrayList<Method>();
			if(!iface.isInterface()) {
				return methods;
			}
			
			methods.addAll(Arrays.asList(iface.getMethods()));
			for(Class<?> parent: iface.getInterfaces()) {
				methods.addAll(getInterfaceMethods(parent));
			}
			return methods;
		}
		
		private static Method findMethod(Object target, String name, Class<?>... params) {
			return target==null? null: findMethod(target.getClass(), name, params);
		}

		private static Method findMethod(Class<?> clazz, String name, Class<?>... params) {
			if(clazz==null || Utils.isBlank(name)) {
				return null;
			}
			
			name = name.trim();
			Method method = getMethod(clazz, name, params);
			while(method==null && clazz!=null) {
				clazz = clazz.getSuperclass();
				method = getMethod(clazz, name, params);
			}
			return method;
		}
		
		private static Method getMethod(Class<?> clazz, String name, Class<?>... params) {
			try {
				return clazz.getMethod(name, params);
			} catch (Exception e) {
				return null;
			}
		}

		@SuppressWarnings("unchecked")
		private static <T, I> I createWrapper(T item, Class<I> wrapperInterface) {
			if(wrapperInterface==null || !wrapperInterface.isInterface()) {
				throw new IllegalArgumentException("'wrapperInterface' parameters must be an interface class.");
			}
			
			ClassLoader cl = EventListeners.class.getClassLoader();
			@SuppressWarnings("rawtypes")
			Class[] interfaces = new Class[] {wrapperInterface};
			InvocationHandler handler = new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
					return item;
				}
			};		
			return (I)Proxy.newProxyInstance(cl, interfaces, handler);		
		}
	}

}
