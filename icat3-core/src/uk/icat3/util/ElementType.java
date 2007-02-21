package uk.icat3.util;

/*
 * ElementType.java
 *
 * Created on 13 February 2007, 09:43
 *
 * ElementType is simply an enum (data structure whose fields 
 * consist of a fixed set of constants) that contains the possible
 * elements / entities of the icat3 model that a user may wish to
 * interact with.  This enum is used in the GateKeeper (Security,
 * authorisation) service.
 *
 * @author df01
 * @version 1.0
 */
public enum ElementType {
    STUDY, INVESTIGATION, DATASET, DATAFILE, DATAFILE_PARAMETER, DATASET_PARAMETER, SAMPLE, SAMPLE_PARAMETER;
}
