package net.eleritec.swing.dialog;

import static net.eleritec.swing.util.SwingUtils.centerOnScreen;
import static net.eleritec.swing.util.SwingUtils.invokeAndWait;
import static net.eleritec.swing.util.SwingUtils.isFlag;
import static net.eleritec.swing.util.SwingUtils.setMaxPreferredWidth;
import static net.eleritec.swing.util.SwingUtils.setSystemLookAndFeel;
import static net.eleritec.swing.util.SwingUtils.subtract;
import static net.eleritec.swing.util.Utils.isBlank;
import static net.eleritec.swing.util.Utils.newInstance;
import static net.eleritec.swing.util.event.EventListeners.actionEvents;
import static net.eleritec.swing.util.event.EventListeners.keyEvents;
import static net.eleritec.swing.util.event.EventListeners.onAction;
import static net.eleritec.swing.util.event.EventListeners.onDocument;
import static net.eleritec.swing.util.event.EventListeners.onHierarchy;
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
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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

import net.eleritec.swing.util.Utils;
import net.eleritec.swing.util.WindowWrapper;

public class CredentialsPanel extends JPanel {

	private static final long serialVersionUID = 5107376704543241707L;
	
	private AskCredentialsModel model;
	
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
	private UsernamePasswordCredentials credentials;

	public CredentialsPanel(AskCredentialsModel model) {
		this.model = model;
		
		createComponents();
		initLayout();
		initEvents();
		initStyles();
	}

	private void createComponents() {
		titlebar = new JPanel();
		
		iconLabel = new JLabel();
		messageLabel = new JTextPane();
		messageLabel.setText(model.getMessage());
		messageLabel.setBackground(null);
		messageLabel.setEnabled(false);
		messageLabel.setDisabledTextColor(messageLabel.getForeground());
		messageLabel.setBorder(null);
		messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD));
		
		promptLabel = new JLabel(model.getPasswordPrompt());
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
		setMaxPreferredWidth(accept, cancel);
		
		form = new FormLayout("pref:grow", "p, 10dlu, p");
		JPanel content = new JPanel(form);
		cc = new CellConstraints();
		content.add(controls, cc.xy(1, 1));
		content.add(buttons, cc.xy(1, 3));
		content.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		setLayout(new BorderLayout());
		add(titlebar, BorderLayout.NORTH);
		add(content, BorderLayout.CENTER);
	}
	
	private void initStyles() {
		setBorder(new LineBorder(Color.GRAY, 1));
		setMessageIcon(createDefaultMessageIcon());
		titlebar.setBackground(new Color(173, 173, 173));
	}
	
	private void initEvents() {
		onAction(cancel, this::cancel);
		actionEvents(accept, pwdField).onAction(this::accept).listen();
		onMouse(titlebar, this::titlebarDragged, DRAGGED);
		onMouse(titlebar, this::titlebarPressed, PRESSED);
		onDocument(pwdField, this::updateButtonState, INSERTED, REMOVED, CHANGED);
		
		setFocusable(true);
		keyEvents(this, pwdField, cancel, accept, titlebar)
			.onKeyPressed(this::cancel, e->e.getKeyCode()==KeyEvent.VK_ESCAPE).listen();
		
		onHierarchy(this, this::onShowingChanged, e->isFlag(e, HierarchyEvent.SHOWING_CHANGED));
	}
	
	private void onShowingChanged() {
		updateButtonState();
		
		if(getAncestorWindow().isVisible()) {
			this.credentials = null;
			pwdField.setText("");
		}
		else {
			Utils.notifyAll(lock);
		}
	}
	
	public synchronized UsernamePasswordCredentials clearCredentials() {
		UsernamePasswordCredentials creds = this.credentials;
		this.credentials = null;
		return creds;
	}
	
	public synchronized boolean hasCredentials() {
		return credentials!=null;
	}
	
	public synchronized UsernamePasswordCredentials waitForCredentials() {
		waitFor();
		return clearCredentials();
	}
	
	private void cancel() {
		applyCredentials(null);
	}

	private void accept() {
		if(areCredentialsPresent()) {
			applyCredentials(pwdField.getPassword());
		}
	}
	
	private void applyCredentials(char[] password) {
		this.credentials = new UsernamePasswordCredentials(model.getSelectedUsername(), password);
		pwdField.setText("");
		getAncestorWindow().setVisible(false);
	}
	
	private void titlebarDragged(MouseEvent event) {
		getAncestorWindow().setLocation(subtract(event.getLocationOnScreen(), offset));
	}
	
	private void titlebarPressed(MouseEvent event) {
		offset = SwingUtilities.convertPoint(event.getComponent(), event.getPoint(), getAncestorWindow());
	}
	
	private void updateButtonState() {
		accept.setEnabled(areCredentialsPresent());
	}
	
	private boolean areCredentialsPresent() {
		return pwdField.getPassword().length > 0 && !isBlank(model.getSelectedUsername());
	}
	
	public void waitFor() {
		if(getAncestorWindow().isVisible()) {
			Utils.waitFor(lock);
		}
	}
	
	public Window getAncestorWindow() {
		return SwingUtilities.getWindowAncestor(this);
	}
	
	public void setMessageIcon(Icon icon) {
		iconLabel.setIcon(icon);
	}
	
	protected Icon createDefaultMessageIcon() {
		return new ImageIcon(getClass().getResource("pwd-dialog-lock-001.png"));
	}
	
	public CredentialsPanel initialize(WindowWrapper window) {
		window.setContentPane(this);
		window.setUndecorated(true);
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.pack();
		
		ImageIcon icon = new ImageIcon(getClass().getResource("pwd-dialog-lock-taskbar-001.png"));
		window.setIconImage(icon.getImage());
		
		return this;
	}
	
	public static CredentialsPanel initialize(Window window, AskCredentialsModel model) {
		return invokeAndWait(()->create(window, model));
	}
	
	private static CredentialsPanel create(Window window, AskCredentialsModel model) {
		return new CredentialsPanel(model).initialize(WindowWrapper.wrap(window));
	}
	
	public static <W extends Window> CredentialsPanel initialize(Class<W> windowType, AskCredentialsModel model) {
		return initialize(newInstance(windowType), model);
	}
	
	public static void main(String[] args) {
		setSystemLookAndFeel();
		
		AskCredentialsModel model = new AskCredentialsModel("EXAMPLE\\username", "<Some Application> wants to use your AD Credentials \nto access system configurations.");
		CredentialsPanel prompt = initialize(JFrame.class, model);
		invokeAndWait(()->{
			centerOnScreen(prompt.getAncestorWindow());
			prompt.getAncestorWindow().setVisible(true);	
		});
		
		UsernamePasswordCredentials creds = prompt.waitForCredentials();
		System.out.println("Username: " + creds.getUsername());
		System.out.println("Password: " + creds.getPasswordText());
		System.exit(0);
	}
}
