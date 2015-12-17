package org.icatproject.core.manager;

import java.util.ArrayList;
import java.util.List;

import org.icatproject.core.manager.AuthenticatorCredentialKey;

public class AuthenticatorDescription {

    private List<AuthenticatorCredentialKey> keys;

    public AuthenticatorDescription(String description) {
	this.keys = new ArrayList<AuthenticatorCredentialKey>();
	AuthenticatorCredentialKey k = new AuthenticatorCredentialKey();
	k.setName("username");
	this.keys.add(k);
	k = new AuthenticatorCredentialKey();
	k.setName("password");
	k.setHide(true);
	this.keys.add(k);
    }

    public List<AuthenticatorCredentialKey> getKeys() {
	return keys;
    }

    public void setKeys(List<AuthenticatorCredentialKey> keys) {
	this.keys = keys;
    }

}
