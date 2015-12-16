package org.icatproject.core.manager;

import org.icatproject.core.manager.PropertyHandler.ExtendedAuthenticator;

public class AuthenticatorInfo {

    private String mnemonic;
    private String description;
    private String friendly;
    private boolean admin;

    public AuthenticatorInfo(String mnemonic, ExtendedAuthenticator auth) {
	this.mnemonic = mnemonic;
	this.description = auth.getAuthenticator().getDescription();
	this.friendly = auth.getFriendly();
	this.admin = auth.isAdmin();
    }

    public String getMnemonic() {
	return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
	this.mnemonic = mnemonic;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getFriendly() {
	return friendly;
    }

    public void setFriendly(String friendly) {
	this.friendly = friendly;
    }

    public boolean getAdmin() {
	return admin;
    }

    public void setAdmin(boolean admin) {
	this.admin = admin;
    }

}
