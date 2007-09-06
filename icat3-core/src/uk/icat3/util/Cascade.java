/*
 * Cascade.java
 *
 * Created on 29 March 2007, 11:21
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

/**
 * For cacsading down the model tree for setting columns or removing values.
 *
 * @author gjd37
 */
public enum Cascade {
    /**
     * Delete (sets deleted column)
     */
    DELETE,
    /**
     * Mod Id
     */
    MOD_ID,
     /**
     * Facility Acquired
     */
    FACILITY_ACQUIRED,
    /**
     * Create Id
     */
    CREATE_ID,
    /**
     * Mod and create Ids
     */
    MOD_AND_CREATE_IDS,
    /**
     * Removes primary keys
     */
    REMOVE_ID,
    /**
     * Removes all with deleted set as Y from the collections
     */
    REMOVE_DELETED_ITEMS; 
   
}
