package uk.icat3.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.bind.DatatypeConverter;

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

    public static Date parseDate(String s) {                              
        return DatatypeConverter.parseDateTime(s).getTime();
    }
  
    public static String printDate(Date dt) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(dt);
        return DatatypeConverter.printDate(cal);
    }    
    
     public static boolean parseBoolean(String yes_no_string){
        if(yes_no_string != null && yes_no_string.equalsIgnoreCase("Y")) return true;
        else return false;
    }
}
