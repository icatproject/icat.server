/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 22 nov. 2010
 */

package uk.icat3.restriction;

/**
 * Defines the restriction search type
 * 
 * @author cruzcruz
 */
public enum RestrictionType {
    /** Investigation restriction search */
    INVESTIGATION,
    /** Dataset restriction search */
    DATASET,
    /** Sample restriction search */
    SAMPLE,
    /** Datafile restriction search */
    DATAFILE,
    /** Facility user restriction search */
    FACILITY_USER,
    /** Parameter restriction search */
    PARAMETER;
}
