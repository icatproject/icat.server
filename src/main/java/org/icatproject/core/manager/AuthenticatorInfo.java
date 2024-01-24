package org.icatproject.core.manager;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import org.icatproject.core.IcatException;
import org.icatproject.core.manager.PropertyHandler.ExtendedAuthenticator;

public class AuthenticatorInfo {

	private String mnemonic;
	private String friendly;
	private boolean admin;
	private List<AuthenticatorCredentialKey> keys = new ArrayList<>();

	public AuthenticatorInfo(String mnemonic, ExtendedAuthenticator auth) throws IcatException {
		this.mnemonic = mnemonic;

		try (JsonReader jr = Json.createReader(new StringReader(auth.getAuthenticator().getDescription()))) {
			JsonArray jkeys = jr.readObject().getJsonArray("keys");
			for (int i = 0; i < jkeys.size(); i++) {
				AuthenticatorCredentialKey key = new AuthenticatorCredentialKey();
				JsonObject jkey = jkeys.getJsonObject(i);
				key.setName(jkey.getString("name"));
				if (jkey.containsKey("pattern")) {
					key.setPattern(jkey.getString("pattern"));
				}
				if (jkey.containsKey("hide")) {
					key.setHide(jkey.getBoolean("hide"));
				}
				keys.add(key);
			}
		}
		friendly = auth.getFriendly();
		admin = auth.isAdmin();
	}

	public String getMnemonic() {
		return mnemonic;
	}

	public void setMnemonic(String mnemonic) {
		this.mnemonic = mnemonic;
	}

	public String getFriendly() {
		return friendly;
	}

	public void setFriendly(String friendly) {
		this.friendly = friendly;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public List<AuthenticatorCredentialKey> getKeys() {
		return keys;
	}

	public void setKeys(List<AuthenticatorCredentialKey> keys) {
		this.keys = keys;
	}

}
