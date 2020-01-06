package net.eleritec.swing.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

	public static boolean isBlank(String text) {
		return text==null || text.trim().isEmpty();
	}
	
	public static String trimOrEmpty(String text) {
		return text==null? "": text.trim();
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
	
	@SafeVarargs
	public static <T> Consumer<T> guard(Consumer<T> consumer, Predicate<T>...filters) {
		return filters.length==0? consumer: (t)->{
			if(all(t, filters)) {
				consumer.accept(t);
			}
		};
	}
	
	@SafeVarargs
	public static <T> boolean all(T candidate, Predicate<T>... predicates) {
		return all(candidate, Arrays.asList(predicates));
	}

	public static <T> boolean all(T candidate, Collection<Predicate<T>> predicates) {
		return all(predicates).test(candidate);
	}
	
	@SafeVarargs
	public static <T> Predicate<T> all(Predicate<T>... predicates) {
		return all(Arrays.asList(predicates));
	}

	public static <T> Predicate<T> all(Collection<Predicate<T>> predicates) {
		return new Predicate<T>() {
			@Override
			public boolean test(T t) {
				return predicates.stream().allMatch(p->p.test(t));
			}			
		};
	}
	
	@SafeVarargs
	public static <T> boolean any(T candidate, Predicate<T>... predicates) {
		return any(candidate, Arrays.asList(predicates));
	}

	public static <T> boolean any(T candidate, Collection<Predicate<T>> predicates) {
		return any(predicates).test(candidate);
	}
	
	@SafeVarargs
	public static <T> Predicate<T> any(Predicate<T>... predicates) {
		return all(Arrays.asList(predicates));
	}

	public static <T> Predicate<T> any(Collection<Predicate<T>> predicates) {
		return new Predicate<T>() {
			@Override
			public boolean test(T t) {
				return predicates.stream().anyMatch(p->p.test(t));
			}			
		};
	}
	
	public static <K, V> Map<K, V> keyValuePairs(Object... items) {
		return keyValuePairs(Arrays.asList(items));
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> keyValuePairs(Collection<?> items) {
		int count = 0;
		K key = null;
		V value = null;
		Map<K, V> map = new HashMap<K, V>();
		for(Object item: items) {
			if(count%2==0) {
				key = (K)item;
			}
			else {
				value = (V)item;
				if(key!=null && value!=null) {
					map.put(key, value);
				}
			}
			count++;
		}
		return map;
	}
	
	public static <T> T newInstance(Class<T> type) {
		try {
			return type==null? null: type.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			return null;
		}
	}
}
