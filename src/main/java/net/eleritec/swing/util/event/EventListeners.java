package net.eleritec.swing.util.event;

import static net.eleritec.swing.util.Utils.asList;
import static net.eleritec.swing.util.Utils.filter;
import static net.eleritec.swing.util.Utils.guard;
import static net.eleritec.swing.util.Utils.map;
import static net.eleritec.swing.util.event.EventTypes.ActionEvents.PERFORMED;
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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import net.eleritec.swing.util.DuckType;
import net.eleritec.swing.util.event.EventTypes.ActionEvents;
import net.eleritec.swing.util.event.EventTypes.ComponentEvents;
import net.eleritec.swing.util.event.EventTypes.DocumentEvents;
import net.eleritec.swing.util.event.EventTypes.FocusEvents;
import net.eleritec.swing.util.event.EventTypes.HierarchyEvents;
import net.eleritec.swing.util.event.EventTypes.KeyEvents;
import net.eleritec.swing.util.event.EventTypes.MouseEvents;
import net.eleritec.swing.util.event.EventTypes.WindowEvents;

public class EventListeners {

	public static void onAction(Object source, Consumer<ActionEvent> listener) {
		actionEvents(source).onAction(listener).listen();
	}
	
	public static void onAction(Object source, Runnable listener) {
		onAction(source, e->listener.run());
	}
	
	public static void onDocument(Object source, Consumer<DocumentEvent> listener, DocumentEvents...types) {
		documentEvents(source).onEvent(listener, types).listen();
	}
	
	public static void onDocument(Object source, Runnable listener, DocumentEvents...types) {
		onDocument(source, e->listener.run(), types);
	}
	
	public static void onMouse(Component component, Consumer<MouseEvent> listener, MouseEvents...types) {
		mouseEvents(component).onMouse(listener, types).listen();
	}
	
	public static void onMouse(Component component, Runnable listener, MouseEvents...types) {
		onMouse(component, e->listener.run(), types);
	}
		
	public static void onMouseWheel(Component component, Consumer<MouseWheelEvent> listener) {
		mouseEvents(component).onWheel(listener).listen();
	}
	
	public static void onMouseWheel(Component component, Runnable listener) {
		onMouseWheel(component, e->listener.run());
	}
	
	public static void onKey(Component component, Consumer<KeyEvent> listener, KeyEvents...types) {
		keyEvents(component).onKey(listener, types).listen();
	}
	
	public static void onKey(Component component, Runnable listener, KeyEvents...types) {
		onKey(component, e->listener.run(), types);
	}
	
	public static void onWindow(Window window, Consumer<WindowEvent> listener, WindowEvents...types) {
		windowEvents(window).onWindow(listener, types).listen();
	}
	
	public static void onWindow(Window window, Runnable listener, WindowEvents...types) {
		onWindow(window, e->listener.run(), types);
	}
	
	public static void onFocus(Component component, Consumer<FocusEvent> listener, FocusEvents...types) {
		focusEvents(component).onFocus(listener, types).listen();
	}
	
	public static void onFocus(Component component, Runnable listener, FocusEvents...types) {
		onFocus(component, e->listener.run(), types);
	}
	
	public static void onComponent(Component component, Consumer<ComponentEvent> listener, ComponentEvents...types) {
		componentEvents(component).onChange(listener, types).listen();
	}
	
	public static void onComponent(Component component, Runnable listener, ComponentEvents...types) {
		onComponent(component, e->listener.run(), types);
	}
	
	@SafeVarargs
	public static void onHierarchy(Component source, Consumer<HierarchyEvent> listener, Predicate<HierarchyEvent>...filters) {
		Consumer<HierarchyEvent> handler = guard(listener, filters);
		hierarchyEvents(source).onChange(handler).listen();
	}
	
	@SafeVarargs
	public static void onHierarchy(Component source, Runnable listener, Predicate<HierarchyEvent>...filters) {
		onHierarchy(source, e->listener.run(), filters);
	}

	
	public static KeyListeners keyEvents(Component...sources) {
		return new KeyListeners(sources);
	}
	
	public static MouseListeners mouseEvents(Component...sources) {
		return new MouseListeners(sources);
	}
	
	public static WindowListeners windowEvents(Window...sources) {
		return new WindowListeners(sources);
	}
	
	public static FocusListeners focusEvents(Component...sources) {
		return new FocusListeners(sources);
	}
	
	public static ActionListeners actionEvents(Object...sources) {
		return new ActionListeners(sources);
	}
	
	public static DocumentListeners documentEvents(Object...sources) {
		return new DocumentListeners(sources);
	}
	
	public static HierarchyListeners hierarchyEvents(Component...sources) {
		return new HierarchyListeners(sources);
	}
	
	public static ComponentListeners componentEvents(Component...sources) {
		return new ComponentListeners(sources);
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
			List<Consumer<ActionEvent>> listeners = asList(map(handlers, h->e->h.run()));
			return (ActionListeners)register(listeners, PERFORMED);
		}

		@Override
		protected void bind(ActionSource source, ActionListener adapter) {
			bind(source::addActionListener, adapter, ActionEvents.values());		
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
			return DuckType.getDuckType(DocumentSource.class, source);
		}
		
		@SafeVarargs
		public final DocumentListeners onEvent(Consumer<DocumentEvent> handler, DocumentEvents...types) {
			return (DocumentListeners)register(handler, types);
		}

		@Override
		protected void bind(DocumentSource source, DocumentListener adapter) {
			bind(source.getDocument()::addDocumentListener, adapter, DocumentEvents.values());	
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
		
		public KeyListeners onKey(Consumer<KeyEvent> handler, Predicate<KeyEvent> filter, KeyEvents...types) {
			return onKey(guard(handler, filter), types);
		}
		
		@SafeVarargs
		public final KeyListeners onKeyPressed(Consumer<KeyEvent> handler, Predicate<KeyEvent>...filters) {
			return onKey(guard(handler, filters), KeyEvents.PRESSED);
		}
		
		public KeyListeners onKey(Runnable handler, KeyEvents...types) {
			return onKey((evt)->handler.run(), types);
		}
		
		public KeyListeners onKey(Runnable handler, Predicate<KeyEvent> filter, KeyEvents...types) {
			return onKey((evt)->handler.run(), filter, types);
		}
		
		@SafeVarargs
		public final KeyListeners onKeyPressed(Runnable handler, Predicate<KeyEvent>...filters) {
			return onKeyPressed((evt)->handler.run(), filters);
		}

		@Override
		protected void bind(Component source, KeyListener adapter) {
			bind(source::addKeyListener, adapter, KeyEvents.values());
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
			bind(source::addFocusListener, adapter, FocusEvents.values());
		}
	}
	
	public static class HierarchyListeners extends EventSubscriptionBuilder<HierarchyEvents, HierarchyListener, Component> {
		public HierarchyListeners(Component...sources) {
			super(HierarchyEvents.class, sources);
		}
		
		@SafeVarargs
		public final HierarchyListeners onChange(Consumer<HierarchyEvent>...handlers) {
			return (HierarchyListeners)register(Arrays.asList(handlers), HierarchyEvents.CHANGED);
		}
		
		@SafeVarargs
		public final HierarchyListeners onChange(Runnable...handlers) {
			List<Consumer<HierarchyEvent>> listeners = asList(map(handlers, h->e->h.run()));
			return (HierarchyListeners)register(listeners, HierarchyEvents.CHANGED);
		}

		@Override
		protected void bind(Component source, HierarchyListener adapter) {
			bind(source::addHierarchyListener, adapter, HierarchyEvents.values());
		}
	}

	public static class ComponentListeners extends EventSubscriptionBuilder<ComponentEvents, ComponentListener, Component> {
		public ComponentListeners(Component...sources) {
			super(ComponentEvents.class, sources);
		}
		
		public ComponentListeners onChange(Consumer<ComponentEvent> handler, ComponentEvents...types) {
			return (ComponentListeners)register(handler, types);
		}

		@Override
		protected void bind(Component source, ComponentListener adapter) {
			bind(source::addComponentListener, adapter, ComponentEvents.values());
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
				filter(types, filter).forEach(type->addEventHandler(handler, type));
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
		return asList(map(targets, t->DuckType.getDuckType(interfaceType, t)));
	}


}
