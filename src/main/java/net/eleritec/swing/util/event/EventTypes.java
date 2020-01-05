package net.eleritec.swing.util.event;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.util.EventListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.eleritec.swing.util.event.EventListeners.MouseAdapter;
import net.eleritec.swing.util.event.EventListeners.WindowAdapter;

public interface EventTypes {
	
	default Class<?> getEventType() {
		return TypeMetadata.getEntry(this).eventTypes.get();
	}
	
	default String getHandlerName() {
		return TypeMetadata.getEntry(this).handlerName;
	}
	
	default Class<? extends EventListener> getHandlerInterface() {
		return TypeMetadata.getEntry(this).handlerInterface;
	}	
	
	public static <T extends EventTypes> T getByHandler(Class<T> type, String handlerName) {
		return TypeMetadata.getByHandler(type, handlerName);
	}
	
	public static enum MouseEvents implements EventTypes {
		CLICKED("mouseClicked"), PRESSED("mousePressed"), RELEASED("mouseReleased"), 
		ENTERED("mouseEntered"), EXITED("mouseExited"), DRAGGED("mouseDragged"), 
		MOVED("mouseMoved"), WHEEL("mouseWheelMoved");
		private MouseEvents(String handlerName) {
			TypeMetadata.register(this, handlerName, MouseAdapter.class, this::eventType);
		}		
		private Class<?> eventType() {
			return this==WHEEL? MouseWheelEvent.class: MouseEvent.class;
		}		
	}
	
	public static enum KeyEvents implements EventTypes {
		TYPED("keyTyped"), PRESSED("keyPressed"), RELEASED("keyReleased");		
		private KeyEvents(String handlerName) {
			TypeMetadata.register(this, handlerName, KeyListener.class, KeyEvent.class);
		}
	}
	
	public static enum ActionEvents implements EventTypes {
		PERFORMED("actionPerformed");		
		private ActionEvents(String handlerName) {
			TypeMetadata.register(this, handlerName, ActionListener.class, ActionEvent.class);
		}
	}
	
	public static enum WindowEvents implements EventTypes {
		OPENED("windowOpened"), CLOSING("windowClosing"), CLOSED("windowClosed"), 
		ICONIFIED("windowIconified"), DEICONIFIED("windowDeiconified"), ACTIVATED("windowActivated"), 
		DEACTIVATED("windowDeactivated"), STATE_CHANGED("windowStateChanged"), 
		FOCUS_GAINED("windowGainedFocus"), FOCUS_LOST("windowLostFocus");		
		private WindowEvents(String handlerName) {
			TypeMetadata.register(this, handlerName, WindowAdapter.class, WindowEvent.class);
		}
	}
	
	public static enum DocumentEvents implements EventTypes {
		INSERTED("insertUpdate"), REMOVED("removeUpdate"), CHANGED("changedUpdate");		
		private DocumentEvents(String handlerName) {
			TypeMetadata.register(this, handlerName, DocumentListener.class, DocumentEvent.class);
		}
	}
	
	public static enum FocusEvents implements EventTypes {
		GAINED("focusGained"), LOST("focusLost");
		private FocusEvents(String handlerName) {
			TypeMetadata.register(this, handlerName, FocusListener.class, FocusEvent.class);
		}
	}
	

	
	static class TypeMetadata<T extends EventTypes> {		
		private static final Map<Class<? extends EventTypes>, TypeMetadata<?>> METADATA_BY_TYPE = new ConcurrentHashMap<>();
		private Class<T> type;
		private Map<T, EntryMetadata<T>> entries = new ConcurrentHashMap<>();
		private Map<String, EntryMetadata<T>> entriesByHandler = new ConcurrentHashMap<>();
		
		@SuppressWarnings("unchecked")
		private static <T extends EventTypes> TypeMetadata<T> get(T enumValue) {
			return (TypeMetadata<T>) get(enumValue.getClass());
		}
		
		private static <T extends EventTypes> EntryMetadata<T> getEntry(T enumValue) {
			return get(enumValue).entries.get(enumValue);
		}
		
		@SuppressWarnings("unchecked")
		private static <T extends EventTypes> TypeMetadata<T> get(Class<T> enumType) {
			return (TypeMetadata<T>) METADATA_BY_TYPE.computeIfAbsent(enumType, t->new TypeMetadata<>(t));
		}

		private TypeMetadata(Class<T> type) {
			this.type = type;
		}
		
		Class<T> getType() {
			return type;
		}

		private void initialize(T enumValue, String handlerName, Class<? extends EventListener> handlerInterface, Supplier<Class<?>> eventTypes) {
			EntryMetadata<T> metadata = new EntryMetadata<>(enumValue, handlerName, handlerInterface, eventTypes);
			entries.put(enumValue, metadata);
			entriesByHandler.put(metadata.handlerName, metadata);
		}
		
		private static <T extends EventTypes> void register(T enumValue, String handlerName, Class<? extends EventListener> handlerInterface, Supplier<Class<?>> eventTypes) {
			get(enumValue).initialize(enumValue, handlerName, handlerInterface, eventTypes);
		}
		
		private static <T extends EventTypes> void register(T enumValue, String handlerName, Class<? extends EventListener> handlerInterface, Class<?> eventType) {
			register(enumValue, handlerName, handlerInterface, ()->eventType);
		}		
		
		private static <T extends EventTypes> T getByHandler(Class<T> type, String handlerName) {
			TypeMetadata<T> typeMeta = type==null || handlerName==null? null: get(type);
			EntryMetadata<T> entryMeta = typeMeta==null? null: typeMeta.entriesByHandler.get(handlerName);
			return entryMeta==null? null: entryMeta.entry;
		}
	}
	
	static class EntryMetadata<T extends EventTypes> {
		private T entry;
		private String handlerName;
		private Class<? extends EventListener> handlerInterface;
		private Supplier<Class<?>> eventTypes;
		
		private EntryMetadata(T entry, String handlerName, Class<? extends EventListener> handlerInterface, Supplier<Class<?>> eventTypes) {
			this.entry = entry;
			this.handlerName = handlerName;
			this.handlerInterface = handlerInterface;
			this.eventTypes = eventTypes;
		}
	}
}
