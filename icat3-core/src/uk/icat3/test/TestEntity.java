/*
 * TestEntity.java
 *
 * Created on 09 March 2007, 09:25
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.entity.ICAT;
import uk.icat3.exceptions.ValidationException;
import uk.icat3.util.ElementType;

/**
 *
 * @author gjd37
 */
public class TestEntity extends EntityBaseBean {
    
    /** Creates a new instance of TestEntity */
    public TestEntity(String purpose) {
        this.setPurposeSeen(purpose);
    }
    
    /**
     * Gets the element type of the bean
     */
    public ElementType getRootElementType(){
        return ElementType.INVESTIGATION;
    }
    
    @Id
    @GeneratedValue()
    @Column(name = "PURPOSE",nullable=   false)
    private String purpose;
    
    @Column(name = "PURPOSE",nullable=   false)
    private String purposeSeen;
    
    /**
     * Method to be overridden if needed to check if the data held in the entity is valid.
     * This method checks whether all the fields which are marked as not null are not null
     *
     * @throws ValidationException if validation error.
     * @return true if validation is correct,
     */
    /*public boolean isValid() throws ValidationException {
        
        //get public the fields in class
        Field[] allFields = getClass().getDeclaredFields();
        //all subclasses should use this line below
        //Field[] allFields = getClass().getDeclaredFields();
        outer:
            for (int i = 0; i < allFields.length; i++) {
            //get name of field
            String fieldName = allFields[i].getName();
            
            boolean id = false;
            boolean generateValue = false;
            
            for (Annotation a : allFields[i].getDeclaredAnnotations()) {
                if(a.annotationType().getName().equals(javax.persistence.Id.class.getName())){
                    id = true;     }
                if(a.annotationType().getName().equals(javax.persistence.GeneratedValue.class.getName())){
                    generateValue = true;
                }
                if(generateValue && id) {
                    log.trace(getClass().getSimpleName()+": "+fieldName+" is auto generated value, no need to check.");
                    continue outer;
                }
            }
            
            //now check all annoatations
            for (Annotation a : allFields[i].getDeclaredAnnotations()) {
                //if this means its a none null column field
                if(a.annotationType().getName().equals(
                        javax.persistence.Column.class.getName()) && a.toString().contains("nullable=false") ){
                    
                    //now check if it is null, if so throw error
                    try {
                        //get value
                        if(allFields[i].get(this) == null){
                            throw new ValidationException(getClass().getSimpleName()+": "+fieldName+" cannot be null.");
                        } else {
                            log.trace(getClass().getSimpleName()+": "+fieldName+" is valid");
                        }
                    } catch (IllegalAccessException ex) {
                        log.warn(getClass().getSimpleName()+": "+fieldName+" cannot be accessed.");
                    }
                }
            }
            }
        //ok here
        return super.isValid();
    }*/
    
    public String getPurposeSeen() {
        return purposeSeen;
    }
    
    public void setPurposeSeen(String purposeSeen) {
        this.purposeSeen = purposeSeen;
    }
    
    /*public void merge(Object object){
        
        Field[] passsedFields = object.getClass().getDeclaredFields();
        Field[] thisFields = this.getClass().getDeclaredFields();
        
        outer: for (Field field : passsedFields) {
            //get name of field
            String fieldName = field.getName();
            //log.trace(fieldName);
            //now check all annoatations
            for (Annotation a : field.getDeclaredAnnotations()) {
                //if this means its a none null column field
                //log.trace(a.annotationType().getName());
                if(a.annotationType().getName().equals(ICAT.class.getName()) && a.toString().contains("merge=false") ){
                    log.trace("not merging, icat(merge=false) "+fieldName);
                    continue outer;
                }
                if(!a.annotationType().getName().contains("Column") ||
                        a.annotationType().getName().contains("Id")){
                    log.trace("not merging, not Column, or Id"+fieldName);
                    continue outer;
                }
            }
            try {
                for(Field thisField : thisFields) {
                    // log.trace(thisField);
                    if(thisField.getName().equals(fieldName)){
                        //now transfer the data
                        log.trace("Setting "+fieldName+" to "+field.get(object));
                        thisField.set(this, field.get(object));
                    }
                }
            }  catch (Exception ex) {
                log.warn("Error transferring data for field: "+fieldName);
            }
        }
    }*/
}
