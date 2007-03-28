/*
 * EntityBaseBean.java
 *
 * Created on 08 February 2007, 09:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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
public class EntityBaseBean {
    
    protected static Logger log = Logger.getLogger(EntityBaseBean.class);
    
    /** Creates a new instance of EntityBaseBean */
    public EntityBaseBean() {
    }
    
    @Column(name = "MOD_TIME", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    protected Date modTime;
    
    @Column(name = "CREATE_ID", nullable = false)
    protected String createId;
    
    @Column(name = "DELETED", nullable = false )
    protected String deleted;
    
     @Column(name = "MOD_ID", nullable = false)
    protected String modId;
    
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
     * Gets the createId of this entity.
     * @return the createId
     */
    public String getCreateId() {
        return createId;
    }
    /**
     * Sets the createId of this entity to the specified value.
     * @param createId the new createId
     */
    public void setCreateId(String createId) {
        this.createId = createId;
    }
    
    /**
     * To find out if they record can be modified    
     */
    public boolean isModifiable(){
        //TODO will change to other colums here aswell as user supplied data and faility 
        //at the moment if from props then cannot change
        if(createId.contains("FROM SPREADSHEET") || createId.contains("PROPAGATION")){
            //user cannot modify this
            return false;
        }
        else return true;
    }
    
    /**
     * Gets the modId of this Datafile.
     * @return the modId
     */
    public String getModId() {
        return this.modId;
    }
    
    /**
     * Sets the modId of this Datafile to the specified value.
     * @param modId the new modId
     */
    public void setModId(String modId) {
        this.modId = modId;
    }
    
    /**
     * Automatically updates modTime when entity is persisted or merged
     */
    @PreUpdate
    public void preUpdate(){
        modTime = new Date();
    }
    
    /**
     * Automatically updates deleted, modTime and modId when entity is created
     */
    @PrePersist
    public void prePersist(){
        deleted = "N";        
        if(modId != null){
            createId = modId;
        } else if(createId != null) modId = createId;
        modTime = new Date();
    }
    
    public String getDeleted() {
        return deleted;
    }
    
    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }
    
    public boolean isDeleted(){
        if(getDeleted() != null && getDeleted().equalsIgnoreCase("Y")) return true;
        else return false;
    }
    
    /**
     * Method to be overridden if needed to check if the data held in the entity is valid.
     * This method checks whether all the fields which are marked as not null are not null
     *
     * @throws ValidationException if validation error.
     * @return true if validation is correct,
     */
    public boolean isValid() throws ValidationException {
        
        //no need to check modTime, deleted and createId in validation cos always put in a create merge time
        //or create id done by application and not user
        
        //get public the fields in class
        /*Field[] allFields = EntityBaseBean.class.getDeclaredFields();
        //all subclasses should use this line below
        //Field[] allFields = getClass().getDeclaredFields();
        for (int i = 0; i < allFields.length; i++) {
         
            //get name of field
            String fieldName = allFields[i].getName();
            //now check all annoatations
            for (Annotation a : allFields[i].getDeclaredAnnotations()) {
                //if this means its a none null column field
                if(a.annotationType().getName().equals(
                        javax.persistence.Column.class.getName()) && a.toString().contains("nullable=false") ){
                    //now check if it is null, if so throw error
                    try {
                        //get value
                        //log.info(""+allFields[i]+"? "+allFields[i].isAccessible());
                        if(allFields[i].get(this) == null){
                            throw new ValidationException(getClass().getSimpleName()+": "+fieldName+" cannot be null.");
                        } else  log.trace(getClass().getSimpleName()+": "+fieldName+" is valid");
                    } catch (IllegalAccessException ex) {
                        log.warn(getClass().getSimpleName()+": "+fieldName+" cannot be accessed.");
                    }
                }
            }
        }*/
        //ok here
        return true;
    }
    
    /**
     * Method to be overriding if needed to check if the data held in the entity is valid.
     * This method xhould be used for search DB for foreign key constraints etc
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
