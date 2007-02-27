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
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author gjd37
 */
public class EntityBaseBean {
    
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
    private String delete;
  
    public String getDelete() {
        return delete;
    }
  
    public void setDelete(String delete) {
        this.delete = delete;
    }
  
    public isDelete(){
        if(getDelete() != null && getDelete().equalsIgnoreCase("Y")) return true;
        else return fasle;
    }*/    
    
    //this is for the web interface so that i can use the 
    //returned entitys in selection, downloads etc
    private boolean selected;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
}
