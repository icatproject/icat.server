package org.icatproject.core.manager;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.icatproject.core.manager.AuthenticatorCredentialKey;

public class AuthenticatorDescription {

    private List<AuthenticatorCredentialKey> keys;

    public AuthenticatorDescription(String description) 
	throws RuntimeException {
	try (JsonReader jr = Json.createReader(new StringReader(description))) {
	    this.keys = new ArrayList<AuthenticatorCredentialKey>();
	    JsonArray jkeys = jr.readObject().getJsonArray("keys");
	    for (int i = 0; i < jkeys.size(); i++) {
		AuthenticatorCredentialKey key = new AuthenticatorCredentialKey();
		JsonObject jkey = jkeys.getJsonObject(i);
		key.setName(jkey.getString("name"));
		try {
		    key.setPattern(jkey.getString("pattern"));
		} catch (NullPointerException e) {
		    // pattern is not set.
		}
		try {
		    key.setHide(jkey.getBoolean("hide"));
		} catch (NullPointerException e) {
		    // hide is not set.
		}
		this.keys.add(key);
	    }
	}
    }

    public List<AuthenticatorCredentialKey> getKeys() {
	return keys;
    }

    public void setKeys(List<AuthenticatorCredentialKey> keys) {
	this.keys = keys;
    }

}
