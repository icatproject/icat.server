// $Id: Configuration.java 935 2011-08-09 13:25:38Z nab24562@FED.CCLRC.AC.UK $
package uk.icat.cmd.entity;

public class Configuration {

	String password;
	String user;
	String url;

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
