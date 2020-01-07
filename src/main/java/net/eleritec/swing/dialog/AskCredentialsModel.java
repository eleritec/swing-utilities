package net.eleritec.swing.dialog;

import static net.eleritec.utils.StringUtil.isEmpty;
import static net.eleritec.utils.StringUtil.trimOrEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class AskCredentialsModel {

	public static final String DEFAULT_PASSWORD_PROMPT = "Please enter password for %s.";
	
	private Set<String> availableUsernames = new TreeSet<String>();
	private List<String> allowedUsernames = new ArrayList<String>();
	private String message;
	private String passwordPrompt;
	private boolean allowCustomUsername;	
	private String selectedUsername;
	
	public AskCredentialsModel(String username, String message) {
		this(username, message, null);
	}
	
	public AskCredentialsModel(String username, String message, String passwordPrompt) {
		addUsernames(username);
		this.message = trimOrEmpty(message);
		this.passwordPrompt = passwordPrompt;
		this.selectedUsername = availableUsernames.isEmpty()? null: availableUsernames.iterator().next();
	}
	
	public String getPasswordPrompt() {
		String prompt = passwordPrompt;
		if(prompt==null) {
			prompt = DEFAULT_PASSWORD_PROMPT;
		}
		
		if(prompt.contains("%s")) {
			prompt = String.format(prompt, trimOrEmpty(getSelectedUsername()));
		}
		
		return prompt;
	}

	public AskCredentialsModel setPasswordPrompt(String passwordPrompt) {
		this.passwordPrompt = passwordPrompt;
		return this;
	}

	public boolean isAllowCustomUsername() {
		return allowCustomUsername;
	}

	public AskCredentialsModel setAllowCustomUsername(boolean allowCustomUsername) {
		this.allowCustomUsername = allowCustomUsername;
		return this;
	}

	public String getSelectedUsername() {
		return selectedUsername;
	}

	public AskCredentialsModel setSelectedUsername(String selectedUsername) {
		selectedUsername = trimOrEmpty(selectedUsername);
		if(!isEmpty(selectedUsername)) {
			this.selectedUsername = selectedUsername.trim();
			if(!this.allowedUsernames.contains(this.selectedUsername)) {
				updateAllowedUsernames();
			}			
		}
		return this;
	}

	public List<String> getAvailableUsernames() {
		return allowedUsernames;
	}
	
	public AskCredentialsModel setAvailableUsernames(String...usernames) {
		return setAvailableUsernames(Arrays.asList(usernames));
	}

	public AskCredentialsModel setAvailableUsernames(List<String> usernames) {
		availableUsernames.clear();
		return addUsernames(usernames);
	}

	public String getMessage() {
		return message;
	}

	public final AskCredentialsModel addUsernames(String...usernames) {
		return addUsernames(Arrays.asList(usernames));
	}
	
	public final AskCredentialsModel addUsernames(Collection<String> usernames) {
		for(String username: usernames) {
			if(!isEmpty(username)) {
				availableUsernames.add(username.trim());
			}	
		}		
		updateAllowedUsernames();
		return this;
	}
	
	private void updateAllowedUsernames() {
		Set<String> allowed = new TreeSet<String>(availableUsernames);
		if(!isEmpty(selectedUsername)) {
			allowed.add(selectedUsername);
		}
		allowedUsernames = Collections.unmodifiableList(new ArrayList<String>(allowed)); 
	}
	
}
