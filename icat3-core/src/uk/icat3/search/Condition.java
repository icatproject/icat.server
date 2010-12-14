/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter and restriction search into ICAT Web Service
 * 
 * Created on 9 dec. 2010
 */

package uk.icat3.search;

/**
 * This class is the parent class of ParameterCondition and RestrictionCondition.
 * This implements some common characteristics for both.
 * 
 * @author cruzcruz
 */
public class Condition {
    /** Indicates if restriction condition is negated (Not) */
    private boolean negate = false;
    /** Indicates if the condition is sensitive to lower or upper case or not. (Default = insensitive) */
    private boolean sensitive = false;

    /////////////////////////////////////////////////////////////////
    //              GETTERS and SETTERS                            //
    ////////////////////////////////////////////////////////////////


    public boolean isSensitive() {
        return sensitive;
    }

    public void setSensitive(boolean sensitive) {
        this.sensitive = sensitive;
    }

    public boolean isNegate() {
        return negate;
    }

    public void setNegate(boolean negate) {
        this.negate = negate;
    }
}
