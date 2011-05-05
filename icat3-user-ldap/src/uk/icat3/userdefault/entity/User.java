/*
 * User.java
 *
 * Created on 20 March 2007, 08:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.userdefault.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import uk.icat3.util.IcatRoles;

/**
 *
 * @author gjd37
 */
@Entity
@Table(name = "USER_TABLE")
@NamedQueries( {
    @NamedQuery(name = "User.findById", query = "SELECT u FROM User u WHERE u.id = :id"),
    @NamedQuery(name = "User.findByDn", query = "SELECT u FROM User u WHERE u.dn = :dn"),
    @NamedQuery(name = "User.findByUserId", query = "SELECT u FROM User u WHERE u.userId = :userId"),
    @NamedQuery(name = "User.findByDnLike", query = "SELECT u FROM User u WHERE u.dn LIKE :dn"),
    @NamedQuery(name = "User.findAll", query = "SELECT u FROM User u"),
    @NamedQuery(name = "User.findByModTime", query = "SELECT u FROM User u WHERE u.modTime = :modTime")}
)
public class User implements Serializable {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "DN", nullable = false, unique=true)
    private String dn;
    
    @Column(name = "USER_ID", nullable = false)
    private String userId;
    
    @Column(name = "PASSWORD")
    private String password;
    
           @Column(name = "MOD_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modTime;
            
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userId",fetch=FetchType.LAZY)
    private java.util.Collection <uk.icat3.userdefault.entity.Session> session;
       
    @PrePersist
    @PreUpdate
    public void prePersist(){
        modTime = new Date();
    }
    
    /** Creates a new instance of User */
    public User() {
    }
    
    public User(Long id) {
        this.setId(id);
    }
    
    public User(Long id, String dn) {
        this.setId(id);
        this.setDn(dn);
    }
    
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getDn() {
        return this.dn;
    }
    
    public void setDn(String dn) {
        this.dn = dn;
    }
    
    /**
     * Gets the userId of this User.
     * @return the userId
     */
    public String getUserId() {
        return this.userId;
    }
    
    /**
     * Sets the userId of this User to the specified value.
     * @param userId the new userId
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    
    public Date getModTime() {
        return this.modTime;
    }
      
    public java.util.Collection <Session> getSession() {
        return this.session;
    }
    
    public void setSession(java.util.Collection <Session> session) {
        this.session = session;
    }
    
    public void addSession(Session session){
        session.setUserId(this);
        Collection<Session> sessions = this.getSession();
        if(sessions == null){
            sessions = new ArrayList<Session>();
        }
        sessions.add(session);
        this.setSession(sessions);
    }
          
    public int hashCode() {
        int hash = 0;
        hash += (this.getId() != null ? this.getId().hashCode() : 0);
        return hash;
    }
    
    public boolean equals(Object object) {
        if (object == null || !this.getClass().equals(object.getClass())) {
            return false;
        }
        User other = (User)object;
        if (this.getId() != other.getId() && (this.getId() == null || !this.getId().equals(other.getId()))) return false;
        return true;
    }
    
    public String toString() {
        //TODO change toString() implementation to return a better display name
         return  "uk.icat3.userdefault.entity.User[id=" + id + "]";
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    } 
    
    public boolean isAdmin(){
        if(getUserId().equals(IcatRoles.ADMIN_USER.toString())) return true;
        else return false;                
    }
    
    public boolean isSuper(){
        if(getUserId().equals(IcatRoles.SUPER_USER.toString())) return true;
        else return false;                 
    }           
}
