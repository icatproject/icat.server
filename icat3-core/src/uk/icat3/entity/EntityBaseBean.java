/*
 * EntityBaseBean.java
 *
 * Created on 08 February 2007, 09:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.io.Serializable;
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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.log4j.Logger;
import uk.icat3.exceptions.EntityNotModifiableError;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.util.ElementType;
import static uk.icat3.util.Util.*;

/**
 *
 * @author gjd37
 */
@MappedSuperclass
public abstract class EntityBaseBean implements Serializable {

    /**
     * global static logger
     */
    protected static Logger log = Logger.getLogger(EntityBaseBean.class);

    /** Creates a new instance of EntityBaseBean */
    public EntityBaseBean() {
    }
    @Column(name = "MOD_TIME", nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    @ICAT(merge = false, nullable = true)
    protected Date modTime;
    @Column(name = "CREATE_TIME", nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    @ICAT(merge = false, nullable = true)
    protected Date createTime;
    @Column(name = "CREATE_ID", nullable = false)
    @ICAT(merge = false, nullable = true)
    protected String createId;
    @Column(name = "DELETED", nullable = false)
    @ICAT(merge = false, nullable = true)
    protected String markedDeleted;
    @Column(name = "MOD_ID", nullable = false)
    @ICAT(merge = false, nullable = true)
    protected String modId;
    @Column(name = "FACILITY_ACQUIRED", nullable = false)
    @ICAT(merge = false, nullable = true)
    protected String facilityAcquired;
    /**
     * Field to check string value of facility acquired
     */
    @Transient
    @ICAT(merge = false, nullable = true)
    protected transient boolean facilityAcquiredSet;
    /**
     * Field to check string value of deleted
     */
    @Transient
    @ICAT(merge = false, nullable = true)
    protected transient boolean deletedBoolean;
    /**
     * Field to allow users to add their own unique id, i.e. facility+id ?
     */
    @Transient
    @ICAT(merge = false, nullable = true)
    protected transient String uniqueId;
    /**
     * Field to allow users change value of a selected item on web page
     */
    @Transient
    @ICAT(merge = false, nullable = true)
    protected transient boolean selected;
    /**
     * Field to put from which facility this came from
     */
    /*@Transient
    @ICAT(merge=false, nullable=true)
    protected transient String facility;*/
    /**
     * The role of the user to the investigation
     */
    private transient IcatRole icatRole;

    /**
     * Gets the modTime of this entity.
     * @return the modTime
     */
    @XmlTransient
    public Date getModTime() {
        return this.modTime;
    }

    /**
     * Sets the modTime of this entity to the specified value.
     * @param modTime the new modTime
     */
    public void setModTime(Date modTime) {
        this.modTime = modTime;
    }

    /**
     * Gets the createTime of this entity.
     * @return the modTime
     */
    @XmlTransient
    public Date getCreateTime() {
        return this.createTime;
    }

    /**
     * Sets the createTime of this entity to the specified value.
     * @param modTime the new modTime
     */
    public void setCreateTime(Date createTime) {
        //not allowed to be set
        this.createTime = createTime;
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
        modTime = new Date();
    }

    /**
     * Gets the facilityAcquired of this entity.
     * @return the facilityAcquired
     */
    @XmlTransient
    public String getFacilityAcquired() {
        return facilityAcquired;
    }

    /**
     * Sets the facilityAcquired of this entity to the specified value.
     * @param facilityAcquired the new createId
     */
    public void setFacilityAcquired(String facilityAcquired) {
        this.facilityAcquired = facilityAcquired;
    }

    /**
     * To find out if they record can be modified
     */
    /*public boolean isModifiable(){
    //TODO will change to other colums here aswell as user supplied data and faility
    //at the moment if from props then cannot change
    if(parseBoolean(facilityAcquired)){
    return false;
    } else return true;
    }*/
    /**
     * Gets the modId of this entity.
     * @return the modId
     */
    @XmlTransient
    public String getModId() {
        return this.modId;
    }

    /**
     * Sets the modId of this entity to the specified value.
     * @param modId the new modId
     */
    public void setModId(String modId) {
        this.modId = modId;
        modTime = new Date();
    }

    /**
     * Gets the role of the user for this investigation
     * @return the icatRole for the user
     */
    //@XmlElement(name="role")
    public IcatRole getIcatRole() {
        return icatRole;
    }

    /**
     * Sets the role of the user for this investigation
     * @param icatRole the icatRole for the user
     */
    public void setIcatRole(IcatRole icatRole) {
        this.icatRole = icatRole;
    }

    /**
     * Automatically updates modTime when entity is persisted or merged
     * @throws uk.icat3.exceptions.EntityNotModifiableError
     */
    @PreUpdate
    public void preUpdate() throws EntityNotModifiableError {
        modTime = new Date();
    }

    /**
     * Automatically updates deleted, modTime, createTime and modId when entity is created
     */
    @PrePersist
    public void prePersist() {
        markedDeleted = "N";
        //set facility acquired if role is ICAT_ADMIN
        if (facilityAcquired == null) {
            facilityAcquired = "N";
        }
        if (modId != null) {
            createId = modId;
        } else if (createId != null) {
            modId = createId;
        }
        modTime = new Date();
        createTime = modTime;
    }

    //@PostLoad
    /*public void postLoad(){
    //setUniqueId(uniqueId);
    }*/
    @XmlElement(name = "facilityAcquiredData")
    public boolean isFacilityAcquiredSet() {
        return parseBoolean(getFacilityAcquired());
    }

    public void setFacilityAcquiredSet(boolean facilityAcquiredSet) {
        this.facilityAcquiredSet = facilityAcquiredSet;
        this.facilityAcquired = (facilityAcquiredSet) ? "Y" : "N";
    }

    @XmlTransient
    public String getMarkedDeleted() {
        return markedDeleted;
    }

    protected void setMarkedDeleted(String deleted) {
        this.markedDeleted = deleted;
        setDeleted(isDeleted());
    }

    public void setDeleted(boolean deletedBoolean) {
        this.deletedBoolean = deletedBoolean;
        this.markedDeleted = (deletedBoolean) ? "Y" : "N";
    }

    @XmlTransient
    public boolean isDeleted() {
        return parseBoolean(getMarkedDeleted());
    }

    /**
     * Gets the uniqueId of this entity.
     * @return the uniqueId
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * Sets the uniqueId of this entity to the specified value.
     * @param uniqueId the new modId
     */
    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    /**
     * Gets the selected of this entity.
     * @return the selected
     */
    public boolean getSelected() {
        return selected;
    }

    /**
     * Sets the selected of this entity to the specified value.
     * @param selected the new modId
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Gets the root element type of the bean
     */
    public abstract ElementType getRootElementType();

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
        log.trace("Checking validity of " + getClass().getSimpleName());

        //all subclasses should use this line below
        //Field[] allFields = getClass().getDeclaredFields();
        outer:
        for (int i = 0; i < allFields.length; i++) {
            //get name of field
            String fieldName = allFields[i].getName();

            //check if field is labeled id and generateValue (primary key, then it can be null)
            boolean id = false;
            boolean generateValue = false;
            //set default max string size
            int max = 255;
            
            for (Annotation a : allFields[i].getDeclaredAnnotations()) {
                if (a.annotationType().getName().equals(javax.persistence.Id.class.getName())) {
                    id = true;
                }
                if (a.annotationType().getName().equals(javax.persistence.GeneratedValue.class.getName())) {
                    generateValue = true;
                }
                if (generateValue && id) {
                    log.trace(getClass().getSimpleName() + ": " + fieldName + " is auto generated id value, no need to check.");
                    continue outer;
                }
                if (a.annotationType().getName().equals(ICAT.class.getName()) && a.toString().contains("nullable=true")) {
                    log.trace(getClass().getSimpleName() + ": " + fieldName + " is ICAT(nullable=true), no need to check.");
                    continue outer;
                }
                if (a.annotationType().getName().equals(ICAT.class.getName()) && a.toString().contains("max")) {
                    max = ((ICAT) a).max();
                    log.trace("Found ICAT(max=" + max + ") for " + fieldName);
                }
            }

            //now check all annoatations
            for (Annotation a : allFields[i].getDeclaredAnnotations()) {
                //Check whether the annotationn has Column Field.
                if (a.annotationType().getName().contains("Column")) {
                    //Check whether the annotation has nullable as false meaning
                    //whether the property cannot be null, test whether its null.
                    if (a.toString().contains("nullable=false")) {

                        //now check if it is null, if so throw error
                        try {
                            //get value
                            Object result = getProperty(fieldName, this);
                            if (result == null) {
                                throw new ValidationException(getClass().getSimpleName() + ": " + fieldName + " cannot be null.");
                            } else {
                                log.trace(getClass().getSimpleName() + ": " + fieldName + " is valid");
                            }
                        } catch (ValidationException ex) {
                            throw ex;
                        } catch (Exception ex) {
                            log.trace(getClass().getSimpleName() + ": " + fieldName + " cannot be accessed.", ex);
                        }
                    }
                    //check max value now of strings, In JPA default string max length
                    //is 255.
                    try {
                        //get value
                        Object result = getProperty(fieldName, this);
                        if (result instanceof String) {
                            log.trace("Checking maximum string length of: " + fieldName);

                            if (((String) result).length() > max) {
                                log.trace("ICAT(max=" + max + ") : length is " + ((String) result).length());
                                throw new ValidationException(getClass().getSimpleName() + ": " + fieldName + " cannot be more than " + max + " in length.");
                            }
                        }
                    } catch (ValidationException ex) {
                        throw ex;
                    } catch (Exception ex) {
                        log.trace(getClass().getSimpleName() + ": " + fieldName + " cannot be accessed.", ex);
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
    public boolean isValid(EntityManager manager, boolean deepValidation) throws ValidationException {
        return isValid();
    }

    /**
     * This current class is merged with another class of the same type.  Uses reflection from the two
     * classes to swap over the bean information.  Any field marked @ICAT(merge=false) is not merged.
     *
     * @param object object passed in to be merged with this class
     */
    public void merge(Object object) {

        Field[] passsedFields = object.getClass().getDeclaredFields();
        Field[] thisFields = this.getClass().getDeclaredFields();

        outer:
        for (Field field : passsedFields) {
            //get name of field
            String fieldName = field.getName();
            //log.trace(fieldName);
            //now check all annoatations
            boolean fieldOk = false;
            for (Annotation a : field.getDeclaredAnnotations()) {
                //if this means its a none null column field
                //log.trace(a.annotationType().getName());
                if (a.annotationType().getName().equals(ICAT.class.getName()) && a.toString().contains("merge=false")) {
                    log.trace("not merging, icat(merge=false): " + fieldName);
                    continue outer;
                }
                if (a.annotationType().getName().contains("Id")) {
                    log.trace("not merging, Id: " + fieldName);
                    continue outer;
                }
                if (a.annotationType().getName().contains("Column")) {
                    log.trace("Passed, it contains column " + fieldName);
                    fieldOk = true;
                }
            }
            //if get there and field contains column then merge
            if (!fieldOk) {
                continue outer;
            }
            try {
                for (Field thisField : thisFields) {
                    // log.trace(thisField);
                    if (thisField.getName().equals(fieldName)) {
                        //now transfer the data
                        //log.trace("Setting "+fieldName+" to "+field.get(object));
                        //thisField.set(this, field.get(object));
                        //new way of using beadn properties
                        swapProperty(fieldName, object, this);
                        //swapProperty(fieldName, this, object);
                    }
                }
            } catch (Exception ex) {
                log.warn("Error transferring data for field: " + fieldName, ex);
            }
        }
    }

    /**
     * Gets the value of the field from a passed in object using reflection
     *
     * @param name name of the field in the from object
     * @param from object wanting to get the bean property value from
     * @throws java.lang.NoSuchMethodException error
     * @throws java.lang.IllegalAccessException error
     * @throws java.lang.reflect.InvocationTargetException error
     * @throws java.lang.NoSuchFieldException error
     * @return object value of the field name
     */
    @SuppressWarnings(value = "all")
    private Object getProperty(String name, Object from) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {
        String prop = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        String mname = "get" + prop;

        Class[] types = new Class[]{};
        Method method = from.getClass().getMethod(mname, types);
        Object result = method.invoke(from, (Object[]) types);

        return result;
    }

    /**
     * This method merges the bean property name, from the from object to the to object
     *
     * @param name name of the field in the from object
     * @param from object wanting to get the bean property value from
     * @param to object wanting to pass the bean property value to
     * @throws java.lang.NoSuchMethodException error
     * @throws java.lang.IllegalAccessException error
     * @throws java.lang.reflect.InvocationTargetException error
     * @throws java.lang.NoSuchFieldException error
     */
    @SuppressWarnings(value = "all")
    private void swapProperty(String name, Object from, Object to) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {

        String prop = Character.toUpperCase(name.charAt(0)) + name.substring(1);
        String mname = "get" + prop;

        Class[] types = new Class[]{};
        Method method = from.getClass().getMethod(mname, types);
        Object result = method.invoke(from, (Object[]) types);

        log.trace("Swapping: " + name + ", setting to: " + result);

        mname = "set" + prop;
        types = new Class[]{from.getClass().getDeclaredField(name).getType()};
        method = to.getClass().getMethod(mname, types);
        method.invoke(to, new Object[]{result});
    }
}