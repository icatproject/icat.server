/*
 * UserDetails.java
 *
 * Created on 20 February 2007, 15:53
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.user;

import java.io.Serializable;

/**
 *
 * @author df01
 */
public class UserDetails implements Serializable {
    
    private String title;    
    private String initial;
    private String firstName;
    private String lastName;
    private String institution;
    private String department;
    private String email;
    private String credential;
    private String federalId;
    
     /** Creates a new instance of UserDetails */
    public UserDetails() {
    }
    
    public UserDetails(String title, String initial, String firstName, String lastName, String institution, String department, String email) {
        this.title = title;
        this.initial = initial;
        this.firstName = firstName;
        this.lastName = lastName;
        this.institution = institution;
        this.department = department;
        this.email = email;
    }
    
    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getInitial() {
        return initial;
    }

    public String getInstitution() {
        return institution;
    }

    public String getLastName() {
        return lastName;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setInitial(String initial) {
        this.initial = initial;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getDepartment() {
        return department;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }
    
    public String getFederalId() {
        return federalId;
    }

    public void setFederalId(String federalId) {
        this.federalId = federalId;
    }
}
