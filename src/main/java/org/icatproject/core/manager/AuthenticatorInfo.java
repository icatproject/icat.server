package org.icatproject.core.manager;

import org.icatproject.core.manager.AuthenticatorDescription;
import org.icatproject.core.manager.PropertyHandler.ExtendedAuthenticator;

public class AuthenticatorInfo {

    private String mnemonic;
    private AuthenticatorDescription description;
    private String friendly;
    private Boolean admin;

    public AuthenticatorInfo(String mnemonic, ExtendedAuthenticator auth) {
	this.mnemonic = mnemonic;
	String desc = auth.getAuthenticator().getDescription();
	this.description = new AuthenticatorDescription(desc);
	this.friendly = auth.getFriendly();
	if (auth.isAdmin()) {
	    this.admin = true;
	}
    }

    public String getMnemonic() {
	return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
	this.mnemonic = mnemonic;
    }

    public AuthenticatorDescription getDescription() {
	return description;
    }

    public void setDescription(AuthenticatorDescription description) {
	this.description = description;
    }

    public String getFriendly() {
	return friendly;
    }

    public void setFriendly(String friendly) {
	this.friendly = friendly;
    }

    public Boolean getAdmin() {
	return admin;
    }

    public void setAdmin(Boolean admin) {
	this.admin = admin;
    }

}
