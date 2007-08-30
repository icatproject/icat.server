/*
 * EntityPrimaryKeyBaseBean.java
 *
 * Created on 18 April 2007, 10:03
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
import org.apache.log4j.Logger;
import uk.icat3.exceptions.ValidationException;

/**
 *
 * @author gjd37
 */
public abstract class EntityPrimaryKeyBaseBean implements Serializable{
    
    protected static Logger log = Logger.getLogger(EntityPrimaryKeyBaseBean.class);
    
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
    
}
