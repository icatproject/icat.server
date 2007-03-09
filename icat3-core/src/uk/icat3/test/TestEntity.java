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

import uk.icat3.entity.EntityBaseBean;
import uk.icat3.exceptions.ValidationException;

/**
 *
 * @author gjd37
 */
public class TestEntity extends EntityBaseBean {
    
    /** Creates a new instance of TestEntity */
    public TestEntity(String purpose) {
        this.purpose = purpose;
    }
    
    @Column(name = "PURPOSE",nullable=   false)
    private String purpose;
    
    /**
     * Method to be overridden if needed to check if the data held in the entity is valid.
     * This method checks whether all the fields which are marked as not null are not null     
     *
     * @throws ValidationException if validation error.
     * @return true if validation is correct,
     */
    public boolean isValid() throws ValidationException {
        
         //get public the fields in class
        Field[] allFields = getClass().getDeclaredFields();
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
                        if(allFields[i].get(this) == null){
                            throw new ValidationException(getClass().getSimpleName()+": "+fieldName+" cannot be null.");
                        }
                    } catch (IllegalAccessException ex) {
                       log.warn(getClass().getSimpleName()+": "+fieldName+" cannot be accessed.");                      
                    }
                }                
            }
        }
        //ok here
        return super.isValid();
    }
}
