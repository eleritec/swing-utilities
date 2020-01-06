package net.eleritec.swing.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.eleritec.swing.util.event.EventListeners;

public class DuckType {

	public static <I> I getDuckType(Class<I> interfaceType, Object target) {
		return getDuckType(interfaceType, target, true);
	}
	
	public static <I> I getDuckType(Class<I> interfaceType, Object target, Map<Method, InvocationHandler> defaults) {
		return getDuckType(interfaceType, target, true, defaults);
	}
	
	public static <I> I getDuckType(Class<I> interfaceType, Object target, boolean strict) {
		return getDuckType(interfaceType, target, strict, null);
	}
	
	@SuppressWarnings("unchecked")
	public static <I> I getDuckType(Class<I> interfaceType, Object target, boolean strict, Map<Method, InvocationHandler> defaults) {
		if(target!=null && interfaceType.isAssignableFrom(target.getClass())) {
			return (I)target;
		}
		
		final boolean valid = DuckType.isDuckType(target, interfaceType);
		MethodTable methodTable = defaults==null || valid? null: new MethodTable(defaults.keySet());
		
		return (I)Proxy.newProxyInstance(DuckType.class.getClassLoader(), new Class[] {interfaceType}, new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				if(strict && !valid) {
					return null;
				}
				
				if(!valid && methodTable!=null) {
					Method tMethod = methodTable.getMethod(method);
					InvocationHandler defaultHandler = tMethod==null? null: defaults.get(tMethod);
					if(defaultHandler!=null) {
						return defaultHandler.invoke(this, tMethod, args);
					}
				}
				
				return DuckType.invoke(target, method, args);
			}
		});	
	}
	
	private static Object invoke(Object target, Method approximate, Object[] args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method tMethod = DuckType.findMethod(target, approximate.getName(), approximate.getParameterTypes());
		return tMethod==null? null: tMethod.invoke(target, args);
	}
	
	public static boolean isDuckType(Object target, Class<?> interfaceType) {
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
	
	public static Method findMethod(Object target, String name, Class<?>... params) {
		return target==null? null: findMethod(target.getClass(), name, params);
	}

	public static Method findMethod(Class<?> clazz, String name, Class<?>... params) {
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
	
	private static boolean isMatch(Method approximate, Method target) {
		if(!Objects.equals(approximate.getName(), target.getName())) {
			return false;
		}
		
		Class<?>[] aParams = approximate.getParameterTypes();
		Class<?>[] tParams = target.getParameterTypes();
		if(aParams.length!=tParams.length) {
			return false;
		}
		
		for(int i=0; i<aParams.length; i++) {
			if(!tParams[i].isAssignableFrom(aParams[i])) {
				return false;
			}
		}
		
		return true;
	}

	@SuppressWarnings("unchecked")
	public static <T, I> I createWrapper(T item, Class<I> wrapperInterface) {
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
	
	private static class MethodTable {
		private Map<String, List<Method>> methods = new HashMap<String, List<Method>>();
		
		private MethodTable(Method...methods) {
			Arrays.stream(methods).forEach(m->add(m));
		}
		
		private MethodTable(Collection<Method> methods) {
			methods.forEach(m->add(m));
		}

		private List<Method> getMethods(String name) {
			return methods.computeIfAbsent(name, k->new ArrayList<>());
		}
		
		private Method getMethod(Method approximate) {
			for(Method candidate: getMethods(approximate.getName())) {
				if(isMatch(approximate, candidate)) {
					return candidate;
				}
			}
			return null;
		}
		
		private void add(Method method) {
			getMethods(method.getName()).add(method);
		}
		
	}
}

