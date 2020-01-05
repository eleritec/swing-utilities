package net.eleritec.swing.dialog;

import static java.lang.String.format;
import static net.eleritec.swing.util.SwingUtils.centerOnScreen;
import static net.eleritec.swing.util.SwingUtils.invokeAndWait;
import static net.eleritec.swing.util.SwingUtils.setPreferredWidth;
import static net.eleritec.swing.util.SwingUtils.setSystemLookAndFeel;
import static net.eleritec.swing.util.event.EventListeners.actions;
import static net.eleritec.swing.util.event.EventListeners.keys;
import static net.eleritec.swing.util.event.EventListeners.onAction;
import static net.eleritec.swing.util.event.EventListeners.onDocument;
import static net.eleritec.swing.util.event.EventListeners.onMouse;
import static net.eleritec.swing.util.event.EventTypes.DocumentEvents.CHANGED;
import static net.eleritec.swing.util.event.EventTypes.DocumentEvents.INSERTED;
import static net.eleritec.swing.util.event.EventTypes.DocumentEvents.REMOVED;
import static net.eleritec.swing.util.event.EventTypes.MouseEvents.DRAGGED;
import static net.eleritec.swing.util.event.EventTypes.MouseEvents.PRESSED;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.eleritec.swing.util.SwingUtils;
import net.eleritec.swing.util.Utils;
import net.eleritec.swing.util.event.EventTypes.KeyEvents;

public class PasswordDialog extends JFrame {

	private static final long serialVersionUID = 5107376704543241707L;
	
	private String message;
	private String username;
	private String pwdDesc;
	
	private JLabel iconLabel;
	private JTextPane messageLabel;
	private JLabel promptLabel;
	private JLabel pwdLabel;
	private JPasswordField pwdField;
	private JButton cancel;
	private JButton accept;
	
	private JPanel titlebar;
	private Point offset;
	
	private Object lock = new Object();
	private char[] password;

	public PasswordDialog(String username, String message) {
		this(username, message, null);
	}
	
	public PasswordDialog(String username, String message, String pwdDescription) {
		this.message = message;
		this.username = username;
		this.pwdDesc = Utils.isBlank(pwdDescription)? "password": pwdDescription.trim();
		
		createComponents();
		initLayout();
		initEvents();
		initStyles();
	}

	private void createComponents() {
		titlebar = new JPanel();
		
		iconLabel = new JLabel(new ImageIcon(getClass().getResource("pwd-dialog-lock-001.png")));
		messageLabel = new JTextPane();
		messageLabel.setText(message);
		messageLabel.setBackground(null);
		messageLabel.setEnabled(false);
		messageLabel.setDisabledTextColor(messageLabel.getForeground());
		messageLabel.setBorder(null);
		messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD));
		
		promptLabel = new JLabel(format("Please enter %s for %s.", pwdDesc, username));
		pwdLabel = new JLabel("Password:");
		pwdField = new JPasswordField();
		cancel = new JButton("Cancel");
		accept = new JButton("OK");
	}
	
	private void initLayout() {
		titlebar.setPreferredSize(new Dimension(20, 20));
		iconLabel.setPreferredSize(new Dimension(64, 64));

		FormLayout form = new FormLayout("pref, 5dlu, pref, 5dlu, pref:grow", "p:grow, $lg, p, $lg, p");
		CellConstraints cc = new CellConstraints();
		JPanel controls = new JPanel(form);
		controls.add(iconLabel, cc.xywh(1, 1, 1, 5));
		controls.add(messageLabel, cc.xyw(3, 1, 3));
		controls.add(promptLabel, cc.xyw(3, 3, 3));
		controls.add(pwdLabel, cc.xy(3, 5));
		controls.add(pwdField, cc.xy(5, 5));
		
		form = new FormLayout("pref:grow, 5dlu, pref, 5dlu, pref", "pref");
		JPanel buttons = new JPanel(form);
		cc = new CellConstraints();
		buttons.add(cancel, cc.xy(3, 1));
		buttons.add(accept, cc.xy(5, 1));
		setPreferredWidth(SwingUtils::getMaxPreferredWidth, accept, cancel);
		
		form = new FormLayout("pref:grow", "p, 10dlu, p");
		JPanel content = new JPanel(form);
		cc = new CellConstraints();
		content.add(controls, cc.xy(1, 1));
		content.add(buttons, cc.xy(1, 3));
		content.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JPanel master = new JPanel(new BorderLayout());
		master.add(titlebar, BorderLayout.NORTH);
		master.add(content, BorderLayout.CENTER);
		setContentPane(master);
	}
	
	private void initStyles() {
		JComponent content = (JComponent)getContentPane();
		content.setBorder(new LineBorder(Color.GRAY, 1));
		
		titlebar.setBackground(new Color(173, 173, 173));
		
		ImageIcon icon = new ImageIcon(getClass().getResource("pwd-dialog-lock-taskbar-001.png"));
		setIconImage(icon.getImage());
		
		setUndecorated(true);
		setResizable(false);
		pack();
	}
	
	private void initEvents() {
		onAction(cancel, this::cancel);
		actions(accept, pwdField).onAction(this::accept).listen();
		onMouse(titlebar, this::titlebarDragged, DRAGGED);
		onMouse(titlebar, this::titlebarPressed, PRESSED);
		onDocument(pwdField, this::updateButtonState, INSERTED, REMOVED, CHANGED);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		getContentPane().setFocusable(true);
		keys(getContentPane(), pwdField, cancel, accept, titlebar)
			.onKey(this::keyControls, KeyEvents.PRESSED).listen();
	}
	
	public synchronized char[] clearPassword() {
		char[] pwd = password;
		password = null;
		return pwd;
	}
	
	public synchronized boolean hasPassword() {
		return password!=null;
	}
	
	public synchronized char[] waitForPassword() {
		waitFor();
		return clearPassword();
	}
	
	private void keyControls(KeyEvent event) {
		if(event.getKeyCode()==KeyEvent.VK_ESCAPE) {
			cancel();
		}
	}
	
	private void cancel() {
		applyPassword(null);
	}

	private void accept() {
		if(hasPasswordText()) {
			applyPassword(pwdField.getPassword());
		}
	}
	
	private void applyPassword(char[] password) {
		this.password = password;
		pwdField.setText("");
		setVisible(false);
	}
	
	private void titlebarDragged(MouseEvent event) {
		Point screen = event.getLocationOnScreen();
		screen.translate(-offset.x, -offset.y);
		setLocation(screen);
	}
	
	private void titlebarPressed(MouseEvent event) {
		offset = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), this);
	}
	
	private void updateButtonState() {
		accept.setEnabled(hasPasswordText());
	}
	
	private boolean hasPasswordText() {
		return pwdField.getPassword().length > 0;
	}
	
	public void waitFor() {
		if(isVisible()) {
			Utils.waitFor(lock);
		}
	}

	@Override
	public void setVisible(boolean visible) {
		if(visible==isVisible()) {
			return;
		}
		
		if(visible) {
			this.password = null;
			pwdField.setText("");
		}
		
		updateButtonState();
		
		super.setVisible(visible);
		
		if(!visible) {
			Utils.notifyAll(lock);
		}
	}

	public static PasswordDialog show(String username, String message) {
		return show(username, message, null);
	}
	
	public static PasswordDialog show(String username, String message, String pwdDescription) {
		try {
			return invokeAndWait(()->showImpl(username, message, pwdDescription));
		} catch (InvocationTargetException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static PasswordDialog showImpl(String username, String message, String pwdDescription) {
		PasswordDialog dialog = new PasswordDialog(username, message, pwdDescription);
		centerOnScreen(dialog);
		dialog.setVisible(true);
		return dialog;
	}
	
	public static void main(String[] args) {
		setSystemLookAndFeel();
		
		PasswordDialog dialog = show("EXAMPLE\\username", "<Some Application> wants to use your AD Credentials \nto access system configurations.");
		char[] passwordChars = dialog.waitForPassword();
		
		String password = passwordChars==null? null: new String(passwordChars);
		System.out.println("Password: " + password);
		System.exit(0);
	}
}
