/*
 * Session.java
 *
 * Created on 27 June 2006, 10:03
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.userdefault.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import uk.icat3.exceptions.SessionException;
import uk.icat3.util.IcatRoles;


/**
 *
 * @author gjd37
 */
@Entity
@Table(name = "USER_SESSION")
@NamedQueries( {
    @NamedQuery(name = "Session.findById", query = "SELECT s FROM Session s WHERE s.id = :id"),
    @NamedQuery(name = "Session.findByUserSessionId", query = "SELECT s FROM Session s WHERE s.userSessionId = :userSessionId"),
    @NamedQuery(name = "Session.findByCredential", query = "SELECT s FROM Session s WHERE s.credential = :credential"),
    @NamedQuery(name = "Session.findByExpireDateTime", query = "SELECT s FROM Session s WHERE s.expireDateTime = :expireDateTime"),
    @NamedQuery(name = "Session.findByModTime", query = "SELECT s FROM Session s WHERE s.modTime = :modTime"),
    @NamedQuery(name = "Session.findAll", query = "SELECT s FROM Session s")}
)
public class Session implements Serializable {
    
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;
    
    @Column(name = "USER_SESSION_ID", nullable = false, unique=true)
    private String userSessionId;
    
    @Lob @Column(name = "CREDENTIAL", nullable = false)
    private String credential;
    
    @Column(name = "EXPIRE_DATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expireDateTime;
    
    @Column(name = "MOD_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modTime;
    
    @Column(name = "RUN_AS")
    private String runAs;
    
    @JoinColumn(name = "USER_ID")
    @ManyToOne
    private User userId;
    
   /**
     * Automatically updates deleted and modTime when entity is persisted
     */
    @PrePersist
    @PreUpdate
    public void prePersist(){
        modTime = new Date();
    }
    
    //@PostLoad
    public void isValid() throws SessionException {
        if(expireDateTime.before(new Date())) throw new SessionException("Session id:"+ getUserSessionId()+" has expired");
    }
    
    /** Creates a new instance of Session */
    public Session() {
    }
    
    public Session(Long id) {
        this.id = id;
    }
    
    public Session(Long id, String userSessionId, String credential, Date modTime) {
        this.id = id;
        this.userSessionId = userSessionId;
        this.credential = credential;
        this.modTime = modTime;
    }
    
    public Long getId() {
        return this.id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserSessionId() {
        return this.userSessionId;
    }
    
    public void setUserSessionId(String userSessionId) {
        this.userSessionId = userSessionId;
    }
    
    public String getCredential() {
        return this.credential;
    }
    
    public void setCredential(String credential) {
        this.credential = credential;
    }
    
    public Date getExpireDateTime() {
        return this.expireDateTime;
    }
    
    public void setExpireDateTime(Date expireDateTime) {
        this.expireDateTime = expireDateTime;
    }
    
    public Date getModTime() {
        return this.modTime;
    }
    
    public User getUserId() {
        return this.userId;
    }
    
    public void setUserId(User userId) {
        this.userId = userId;
    }
    
    public int hashCode() {
        int hash = 0;
        hash += (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
    public boolean equals(Object object) {
        if (object == null || !this.getClass().equals(object.getClass())) {
            return false;
        }
        Session other = (Session)object;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) return false;
        return true;
    }
    
    public String toString() {
        //TODO change toString() implementation to return a better display name
        return  "uk.icat3.userdefault.entity.Session[id=" + id + "]";
    }
    
    public boolean isAdmin(){
        return getUserId().isAdmin();        
    }
    
    public boolean isSuper(){
        return getUserId().isSuper();
    }
    
    public String getRunAs() {
        return runAs;
    }
    
    public void setRunAs(String runAs) {
        this.runAs = runAs;
    }
}
