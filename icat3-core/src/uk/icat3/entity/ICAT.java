/*
 * ICAT.java
 *
 * Created on 30 March 2007, 14:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This determines at runtime what information is merged to the database.
 * Anything with ICAT(merge=false) wont be merged/updated to DB
 * Anything with ICAT(nullable=true) wont be checked to see if valid as it can be null
 * Anything with ICAT(max=1243) will be checked if its a string for the max length allowed
 * 
 * @author gjd37
 */
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value=ElementType.FIELD)
public @interface ICAT {
    boolean merge() default true;
    boolean nullable() default false;
    int max() default 255; 
}
