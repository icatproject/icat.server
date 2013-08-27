package org.icatproject.authentication;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

import static org.apache.commons.codec.digest.Crypt.crypt;

@Entity
public class Passwd implements Serializable
{

    @Id
    private String userName;

    private String encodedPassword;

    // Needed by JPA
    public Passwd() {
    }

    public Passwd(String user, String pass)
    {
	userName = pass;
	encodedPassword = pass;
    }

    public boolean verify(String pass)
    {
	if (encodedPassword == null || encodedPassword.length() == 0)
	    return false;

	if (encodedPassword.charAt(0) == '$') {
	    return encodedPassword.equals(crypt(pass, encodedPassword));
	}
	else {
	    return encodedPassword.equals(pass);
	}
    }

}

