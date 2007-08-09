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
    /**
     * Read/View
     */
    READ,
    /**
     * Modify/Update
     */
    UPDATE,
    /**
     * Sets deleted in table
     */
    DELETE,
    /**
     * Removes it from DB
     */
    REMOVE,
    /**
     *  Creates new (INSERT)
     */
    CREATE,
    /**
     * Admin
     */
    ADMIN,
    /**
     * Download from SRB
     */
    DOWNLOAD,
     /**
     * If user has options to modify and 
     */
    MANAGE_USERS,
    /**
     * Set Facility acquired data.
     */
    SET_FA;
}
