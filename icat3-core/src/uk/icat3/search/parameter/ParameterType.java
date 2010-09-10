/*
 * This code is developed in Institut Laue-Langevin (France).
 * Its goal is the implementation of parameter search into ICAT Web Service
 * 
 * Created on 25 juin 2010
 */

package uk.icat3.search.parameter;

/**
 * Define the different Parameter Type that could exists
 * @author cruzcruz
 */
public enum ParameterType {
    /** Datafile parameter search */
    DATAFILE,
    /** Dataset parameter search */
    DATASET,
    /** Sample parameter search */
    SAMPLE
}
