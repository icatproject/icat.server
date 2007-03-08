/*
 * EntityBaseBean.java
 *
 * Created on 08 February 2007, 09:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.ValidationException;

/**
 *
 * @author gjd37
 */
@MappedSuperclass
public abstract class EntityBaseBean {
    
    protected static Logger log = Logger.getLogger(EntityBaseBean.class);
    
    /** Creates a new instance of EntityBaseBean */
    public EntityBaseBean() {
    }
    
    @Column(name = "MOD_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    protected Date modTime;    
    
    /**
     * Gets the modTime of this DatafileFormat.
     * @return the modTime
     */
    public Date getModTime() {
        return this.modTime;
    }
    
    /**
     * Sets the modTime of this DatafileFormat to the specified value.
     * @param modTime the new modTime
     */
    public void setModTime(Date modTime) {
        this.modTime = modTime;
    }
    
    /**
     * Automatically updates modTime when entity is persisted or merged     
     */
    @PrePersist
    @PreUpdate
    public void prePersist(){       
        modTime = new Date();
    }
    
    /*@Column(name = "DELETE", nullable = true ) 
    private String deleted;
  
    public String getDeleted() {
        return deleted;
    }
  
    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }
  
    public isDeleted(){
        if(getDeleted() != null && getDeleted().equalsIgnoreCase("Y")) return true;
        else return fasle;
    }*/    
    
    /**
     * Method to be overriding if needed to check if the data held in the entity is valid.
     *
     * @throws ValidationException if validation error.
     * @return true if validation is correct,
     */
    public boolean isValid() throws ValidationException {
        return true;
    }
    
    /**
     * Method to be overriding if needed to check if the data held in the entity is valid.
     * 
     * @return true if validation is correct,
     * @param manager if queries are needed
     * @throws ValidationException if validation error.
     */
    public boolean isValid(EntityManager manager) throws ValidationException {
        //always call isValid()
        return isValid();
    }
    
}
