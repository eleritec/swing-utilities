package net.eleritec.swing.util;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.UIManager;

public class SwingUtils {

	public static void setSystemLookAndFeel() {
		invokeAndWait(()->setLookAndFeelImpl(UIManager.getSystemLookAndFeelClassName()));
	}
	
	private static void setLookAndFeelImpl(String lnfClassName) {
		try {
			UIManager.setLookAndFeel(lnfClassName);
		} catch (Exception e) {
		}
	}
	
	public static void setPreferredWidth(Function<JComponent[], Integer> widthFinder, JComponent...components) {
		setPreferredWidth(widthFinder.apply(components), components);
	}
	
	public static void setPreferredWidth(int width, JComponent...components) {
		Arrays.asList(components).forEach(c->{
			c.setPreferredSize(new Dimension(width, c.getPreferredSize().height));
		});
	}
	
	public static int getMaxPreferredWidth(JComponent...components) {
		return Utils.getMax(components, c->c.getPreferredSize().width);
	}
	
	public static void setMaxPreferredWidth(JComponent...components) {
		setPreferredWidth(SwingUtils::getMaxPreferredWidth, components);
	}
	
	public static void centerOnScreen(Window window) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		window.setLocation(dim.width/2-window.getSize().width/2, dim.height/2-window.getSize().height/2);
	}
	
	public static <T> T invokeAndWait(Supplier<T> supplier) {
		final List<T> ref = new ArrayList<T>();
		try {
			if(EventQueue.isDispatchThread()) {
				ref.add(supplier.get());
			}
			else {
				EventQueue.invokeAndWait(()->{
					ref.add(supplier.get());
				});	
			}
		} catch (InvocationTargetException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		return Utils.get(ref, 0);
	}

	public static void invokeAndWait(Runnable action) {
		invokeAndWait(()->{
			action.run();
			return null;
		});
	}
	
	public static boolean isFlag(HierarchyEvent event, long flag) {
		return (event.getChangeFlags() & flag) != 0;
	}
	
	public static Point subtract(Point point, Point tx) {
		return translate(point, tx, true);
	}
	
	public static Point translate(Point point, Point tx) {
		return translate(point, tx, false);
	}
	
	public static Point translate(Point point, Point tx, boolean negative) {
		point.translate(negative? -tx.x: tx.x, negative? -tx.y: tx.y);
		return point;
	}
	
}
