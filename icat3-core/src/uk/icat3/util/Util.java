package uk.icat3.util;

/**
 * Util.java
 * 
 * Created on 23-May-2007, 13:09:01
 * 
 * Utility class to handle common operations across project e.g. checking for null strings.
 *  
 * @author df01
 */

public class Util {

    /**
     * Method that returns true if String parameter is empty (i.e. null or 0-length)
     * or false if String contains data.
     * 
     * @param str           String to be compared
     * @return boolean      result of test
     */ 
    public static boolean isEmpty(String str) {
        if ((str == null) || (str.length() == 0)) return true;
        else return false;
    }

}
