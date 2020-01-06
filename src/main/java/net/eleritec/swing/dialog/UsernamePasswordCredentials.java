package net.eleritec.swing.dialog;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsernamePasswordCredentials {
	private String username;
	private char[] password;
	
	public String getPasswordText() {
		return password==null? null: new String(password);
	}
	
	public boolean hasPassword() {
		return password!=null;
	}
}
