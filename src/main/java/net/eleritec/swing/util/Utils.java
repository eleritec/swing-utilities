package net.eleritec.swing.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

	public static boolean isBlank(String text) {
		return text==null || text.trim().isEmpty();
	}
	
	public static <T> T get(List<T> items, int index) {
		return items!=null && index < items.size() && index >= 0? items.get(index): null;
	}
	
	public static <T> List<T> asList(Stream<T> stream) {
		return stream.collect(Collectors.toList());
	}
	
	public static <T> List<T> asList(Collection<T> collection, Predicate<T> filter) {
		return asList(filter(collection, filter));
	}
	
	public static <T> List<T> asList(T[] array, Predicate<T> filter) {
		return asList(filter(array, filter));
	}
	
	public static <T, R> Stream<R> map(T[] array, Function<T, R> tx) {
		return Arrays.stream(array).map(tx);
	}
	
	public static <T, R> Stream<R> map(Collection<T> collection, Function<T, R> tx) {
		return collection.stream().map(tx);
	}
	
	public static <T> Stream<T> filter(T[] array, Predicate<T> filter) {
		return Arrays.stream(array).filter(filter==null? Utils::always: filter);
	}
	
	public static <T> Stream<T> filter(Collection<T> collection, Predicate<T> filter) {
		return collection.stream().filter(filter==null? Utils::always: filter);
	}
	
	public static <T> boolean always(T t) {
		return true;
	}
	
	public static <T> int getMax(T[] array, Function<T, Integer> tx) {
		return getMax(Arrays.asList(array), tx);
	}

	public static <T> int getMax(Collection<T> collection, Function<T, Integer> tx) {
		return map(collection, tx).reduce(0, (a, b)->Math.max(a, b));
	}
	
	public static void notifyAll(Object monitor) {
		if(monitor!=null) {
			synchronized(monitor) {
				monitor.notifyAll();
			}
		}
	}
	
	public static void waitFor(Object monitor) {
		if(monitor!=null) {
			synchronized (monitor) {
				try {
					monitor.wait();
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
