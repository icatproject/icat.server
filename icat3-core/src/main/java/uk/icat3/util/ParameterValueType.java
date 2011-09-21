/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.icat3.util;

/**
 * This enumeration gives the different types of values that a parameter can have.
 * @author Mr. Srikanth Nagella
 */
public enum ParameterValueType {
    /**
     * Parameter is of Numeric type
     */
    NUMERIC("Y"),
    /**
     * Parameter type  is String
     */
    STRING("N"),
    /**
     * Date Time Parameter type
     */
    DATE_AND_TIME("D");

    private String value;
    
    ParameterValueType(String value){
        this.value=value;
    }

    public String getValue(){
        return value;
    }

    public static ParameterValueType toParameterValueType(String pvalue){
        if(pvalue==null) return ParameterValueType.NUMERIC;
        if(pvalue.compareToIgnoreCase("Y")==0) return ParameterValueType.NUMERIC;
        if(pvalue.compareToIgnoreCase("N")==0) return ParameterValueType.STRING;
        if(pvalue.compareToIgnoreCase("D")==0) return ParameterValueType.DATE_AND_TIME;
        return ParameterValueType.NUMERIC;

    }

}
