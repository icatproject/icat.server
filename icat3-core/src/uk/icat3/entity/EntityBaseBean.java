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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.EntityNotModifiableError;
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
    @ICAT(merge=false, nullable=true)
    protected Date modTime;
    
    @Column(name = "CREATE_ID", nullable = false)
    @ICAT(merge=false, nullable=true)
    protected String createId;
    
    @Column(name = "DELETED", nullable = false )
    @ICAT(merge=false, nullable=true)
    protected String deleted;
    
    @ICAT(merge=false, nullable=true)
    protected transient boolean deletedBoolean;
    
    @Column(name = "MOD_ID", nullable = false)
    @ICAT(merge=false, nullable=true)
    protected String modId;
    
    /**
     * Gets the modTime of this DatafileFormat.
     * @return the modTime
     */
    @XmlTransient
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
    @XmlTransient
    public String getCreateId() {
        return createId;
    }
    /**
     * Sets the createId of this entity to the specified value.
     * @param createId the new createId
     */
    public void setCreateId(String createId) {
        this.createId = createId;
        if(isModifiable()) modTime = new Date();
    }
    
    /**
     * To find out if they record can be modified
     */
    public boolean isModifiable(){
        //TODO will change to other colums here aswell as user supplied data and faility
        //at the moment if from props then cannot change
        if(createId != null){
            if(createId.contains("FROM SPREADSHEET") || createId.contains("PROPAGATION")){
                //user cannot modify this
                return false;
            } else return true;
        } else return true;
    }
    
    /**
     * Gets the modId of this Datafile.
     * @return the modId
     */
    @XmlTransient
    public String getModId() {
        return this.modId;
    }
    
    /**
     * Sets the modId of this Datafile to the specified value.
     * @param modId the new modId
     */
    public void setModId(String modId) {
        this.modId = modId;
        if(isModifiable()) modTime = new Date();
    }
    
    /**
     * Automatically updates modTime when entity is persisted or merged
     */
    @PreUpdate
    public void preUpdate() throws EntityNotModifiableError {
        //this runtime error should not happen, the application should check
        //isModifibale before trying to change the state
        //if(!isModifiable()) throw new EntityNotModifiableError(this +" cannot be modified");
        //this way better, so not changed if not modifiable
        if(isModifiable()) modTime = new Date();
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
    
    @XmlTransient
    public String getDeleted() {
        return deleted;
    }
    
    protected void setDeleted(String deleted) {
        this.deleted = deleted;
    }
    
    public void setDeleted(boolean deletedBoolean) {
        this.deletedBoolean = deletedBoolean;
        this.deleted = (deletedBoolean) ? "Y" : "N";
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
        
        //get public, private, protected, package fields in class, but not inherited ones.
        Field[] allFields = this.getClass().getDeclaredFields();
        log.trace("Checking validity of "+getClass().getSimpleName());
        
        //all subclasses should use this line below
        //Field[] allFields = getClass().getDeclaredFields();
        outer:
            for (int i = 0; i < allFields.length; i++) {
            //get name of field
            String fieldName = allFields[i].getName();
            
            //check if field is labeled id and generateValue (primary key, then it can be null)
            boolean id = false;
            boolean generateValue = false;
            
            for (Annotation a : allFields[i].getDeclaredAnnotations()) {
                if(a.annotationType().getName().equals(javax.persistence.Id.class.getName())){
                    id = true;
                }
                if(a.annotationType().getName().equals(javax.persistence.GeneratedValue.class.getName())){
                    generateValue = true;
                }
                if(generateValue && id) {
                    log.trace(getClass().getSimpleName()+": "+fieldName+" is auto generated id value, no need to check.");
                    continue outer;
                }
                if(a.annotationType().getName().equals(ICAT.class.getName()) && a.toString().contains("nullable=true") ){
                    log.trace(getClass().getSimpleName()+": "+fieldName+" is ICAT(nullable=true), no need to check.");                  
                    continue outer;
                }
            }
            
            //now check all annoatations
            for (Annotation a : allFields[i].getDeclaredAnnotations()) {
                //if this means its a none null column field
                if(a.annotationType().getName().contains("Column") && a.toString().contains("nullable=false") ){
                    
                    //now check if it is null, if so throw error
                    try {
                        //get value
                        Object result = getProperty(fieldName, this);
                        if(result == null){
                            throw new ValidationException(getClass().getSimpleName()+": "+fieldName+" cannot be null.");
                        } else {
                            log.trace(getClass().getSimpleName()+": "+fieldName+" is valid");
                        }
                    } catch (ValidationException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        log.warn(getClass().getSimpleName()+": "+fieldName+" cannot be accessed.",ex);
                    }
                }
            }
            }
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
        return isValid(manager, true);
    }
    
    /**
     * Method to be overriding if needed to check if the data held in the entity is valid.
     * This method should be used for search DB for foreign key constraints etc
     * Deep validation if all of its children need to be validated
     *
     * @return true if validation is correct,
     * @param manager if queries are needed
     * @param deepValidation if all of child entities need to be validated
     * @throws ValidationException if validation error.
     */
    public boolean isValid(EntityManager manager, boolean deepValidation)  throws ValidationException {
        return isValid();
    }
    
    public void merge(Object object){
        
        Field[] passsedFields = object.getClass().getDeclaredFields();
        Field[] thisFields = this.getClass().getDeclaredFields();
        
        outer: for (Field field : passsedFields) {
            //get name of field
            String fieldName = field.getName();
            //log.trace(fieldName);
            //now check all annoatations
            boolean fieldOk = false;
            for (Annotation a : field.getDeclaredAnnotations()) {
                //if this means its a none null column field
                //log.trace(a.annotationType().getName());
                if(a.annotationType().getName().equals(ICAT.class.getName()) && a.toString().contains("merge=false") ){
                    log.trace("not merging, icat(merge=false): "+fieldName);
                    continue outer;
                }
                if(a.annotationType().getName().contains("Id")){
                    log.trace("not merging, Id: " +fieldName);
                    continue outer;
                }
                if(a.annotationType().getName().contains("Column")){
                    log.trace("Passed, it contains column "+ fieldName);
                    fieldOk = true;
                }
            }
            //if get there and field contains column then merge
            if(!fieldOk) continue outer;
            try {
                for(Field thisField : thisFields) {
                    // log.trace(thisField);
                    if(thisField.getName().equals(fieldName)){
                        //now transfer the data
                        //log.trace("Setting "+fieldName+" to "+field.get(object));
                        //thisField.set(this, field.get(object));
                        //new way of using beadn properties
                        swapProperty(fieldName, this, object);
                        
                    }
                }
            }  catch (Exception ex) {
                log.warn("Error transferring data for field: "+fieldName, ex);
            }
        }
    }
    
    @SuppressWarnings("all")
    private Object getProperty(String name, Object from) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        String prop = Character.toUpperCase(name.charAt(0)) +
                name.substring(1);
        String mname = "get" + prop;
        
        Class[] types = new Class[]{};
        Method method = from.getClass().getMethod(mname, types);
        Object result = method.invoke(from, (Object[])types);
        
        return result;
    }
    
    @SuppressWarnings("all")
    private void swapProperty(String name, Object from, Object to) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        String prop = Character.toUpperCase(name.charAt(0)) +
                name.substring(1);
        String mname = "get" + prop;
        
        Class[] types = new Class[]{};
        Method method = from.getClass().getMethod(mname, types);
        Object result = method.invoke(from, (Object[])types);
        
        
        mname = "set" + prop;
        types = new Class[] { from.getClass().getDeclaredField(name).getType() };
        method = to.getClass().getMethod(mname, types);
        method.invoke(to, new Object[] { result });
    }
}
