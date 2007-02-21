/*
 * Queries.java
 *
 * Created on 16 November 2006, 11:06
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uk.icat3.util;

/**
 *
 * @author gjd37
 */
public class Queries {
    
    public static final String INVESTIGATIONS_BY_KEYWORD = "Investigation.findByKeyword";
    public static final String INVESTIGATIONS_BY_USER_SURNAME = "Investigation.findByUserSurname";
    public static final String INVESTIGATION_LIST_BY_KEYWORD = "Investigation.findByKeword";
    
    
    public static final String INVESTIGATION_NATIVE_LIST_BY_KEYWORD = "Investigation.findByKewordNative";
    public static final String INVESTIGATION_NATIVE_LIST_BY_SURNAME = "Investigation.findBySurnameNative";
    public static final String INVESTIGATION_NATIVE_LIST_BY_USERID = "Investigation.findByUserIDNative";
    
    
    public static final String DATAFILE_NATIVE_BY_INSTRUMANT_AND_RUN_NUMBER = "Datafile.findByRunNumberNative";
    public static final String DATAFILE_BY_INSTRUMANT_AND_RUN_NUMBER = "Datafile.findByRunNumber";
    
    public static final String ADVANCED_SEARCH = "Investigation.findByAdvancedSearch";
}
