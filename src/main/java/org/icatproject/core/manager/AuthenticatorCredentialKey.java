package org.icatproject.core.manager;

public class AuthenticatorCredentialKey {

    private String name;
    private String pattern;
    private Boolean hide;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getPattern() {
	return pattern;
    }

    public void setPattern(String pattern) {
	this.pattern = pattern;
    }

    public Boolean getHide() {
	return hide;
    }

    public void setHide(Boolean hide) {
	this.hide = hide;
    }

}
