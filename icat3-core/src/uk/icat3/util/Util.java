package uk.icat3.util;

/**
 * Util.java
 * 
 * Created on 23-May-2007, 13:09:01
 * 
 * Utility class to handle common operations across project e.g. checking for null strings 
 * @author df01
 */
public class Util {

    public static boolean isEmpty(String str) {
        if ((str == null) || (str.length() == 0)) return true;
        else return false;
    }

}
