/*
 * AccessType.java
 *
 * Created on 14 November 2006, 14:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

/**
 *
 * @author gjd37
 */
public enum AccessType {
    
    //DELETE marks the record as delete in the deleted column, remove removes the record from the DB
    READ, UPDATE, DELETE, REMOVE, CREATE, ADMIN, FINE_GRAINED_ACCESS;
}
