#!/bin/env python

import MySQLdb as mdb
from optparse import OptionParser
import sys

con = mdb.connect('localhost', 'icat', 'icat', 'icat');

parser = OptionParser()
parser.add_option("--facility", "-f", help="Name of facility to relate to applications - not needed if exactly one exists")

opts, args = parser.parse_args()

facility_name = opts.facility

def abort(msg):
    print >> sys.stderr, msg
    sys.exit(1)
    
def addForeignKeys():
    cur.execute("ALTER TABLE APPLICATION ADD CONSTRAINT FK_APPLICATION_FACILITY_ID FOREIGN KEY (FACILITY_ID) REFERENCES FACILITY (ID)")
    
    cur.execute("ALTER TABLE DATACOLLECTIONDATAFILE ADD CONSTRAINT FK_DATACOLLECTIONDATAFILE_DATACOLLECTION_ID FOREIGN KEY (DATACOLLECTION_ID) REFERENCES DATACOLLECTION (ID)")
    cur.execute("ALTER TABLE DATACOLLECTIONDATAFILE ADD CONSTRAINT FK_DATACOLLECTIONDATAFILE_DATAFILE_ID FOREIGN KEY (DATAFILE_ID) REFERENCES DATAFILE (ID)")

    cur.execute("ALTER TABLE DATACOLLECTIONDATASET ADD CONSTRAINT FK_DATACOLLECTIONDATASET_DATACOLLECTION_ID FOREIGN KEY (DATACOLLECTION_ID) REFERENCES DATACOLLECTION (ID)")
    cur.execute("ALTER TABLE DATACOLLECTIONDATASET ADD CONSTRAINT FK_DATACOLLECTIONDATASET_DATASET_ID FOREIGN KEY (DATASET_ID) REFERENCES DATASET (ID)")
    
    cur.execute("ALTER TABLE DATACOLLECTIONPARAMETER ADD CONSTRAINT FK_DATACOLLECTIONPARAMETER_DATACOLLECTION_ID FOREIGN KEY (DATACOLLECTION_ID) REFERENCES DATACOLLECTION (ID)")
    cur.execute("ALTER TABLE DATACOLLECTIONPARAMETER ADD CONSTRAINT FK_DATACOLLECTIONPARAMETER_PARAMETER_TYPE_ID FOREIGN KEY (PARAMETER_TYPE_ID) REFERENCES PARAMETERTYPE (ID)")

    cur.execute("ALTER TABLE DATASET ADD CONSTRAINT FK_DATASET_TYPE_ID FOREIGN KEY (TYPE_ID) REFERENCES DATASETTYPE (ID)")
    cur.execute("ALTER TABLE DATASET ADD CONSTRAINT FK_DATASET_INVESTIGATION_ID FOREIGN KEY (INVESTIGATION_ID) REFERENCES INVESTIGATION (ID)")
    cur.execute("ALTER TABLE DATASET ADD CONSTRAINT FK_DATASET_SAMPLE_ID FOREIGN KEY (SAMPLE_ID) REFERENCES SAMPLE (ID)")
    
    cur.execute("ALTER TABLE INVESTIGATIONINSTRUMENT ADD CONSTRAINT FK_INVESTIGATIONINSTRUMENT_INSTRUMENT_ID FOREIGN KEY (INSTRUMENT_ID) REFERENCES INSTRUMENT (ID)")
    cur.execute("ALTER TABLE INVESTIGATIONINSTRUMENT ADD CONSTRAINT FK_INVESTIGATIONINSTRUMENT_INVESTIGATION_ID FOREIGN KEY (INVESTIGATION_ID) REFERENCES INVESTIGATION (ID)")

    cur.execute("ALTER TABLE JOB ADD CONSTRAINT FK_JOB_INPUTDATACOLLECTION_ID FOREIGN KEY (INPUTDATACOLLECTION_ID) REFERENCES DATACOLLECTION (ID)")
    cur.execute("ALTER TABLE JOB ADD CONSTRAINT FK_JOB_OUTPUTDATACOLLECTION_ID FOREIGN KEY (OUTPUTDATACOLLECTION_ID) REFERENCES DATACOLLECTION (ID)")
    
    cur.execute("ALTER TABLE RULE_ ADD CONSTRAINT FK_RULE__GROUP_ID FOREIGN KEY (GROUP_ID) REFERENCES GROUP_ (ID)")
    
    cur.execute("ALTER TABLE USERGROUP ADD CONSTRAINT FK_USERGROUP_GROUP_ID FOREIGN KEY (GROUP_ID) REFERENCES GROUP_ (ID)")
    cur.execute("ALTER TABLE USERGROUP ADD CONSTRAINT FK_USERGROUP_USER_ID FOREIGN KEY (USER_ID) REFERENCES USER_ (ID)")
    
def change():
    cur.execute("ALTER TABLE APPLICATION ADD FACILITY_ID BIGINT")   
    cur.execute("UPDATE APPLICATION SET FACILITY_ID = " + str(facility_id))
    cur.execute("ALTER TABLE APPLICATION CHANGE FACILITY_ID FACILITY_ID BIGINT NOT NULL")
    cur.execute("ALTER TABLE APPLICATION CHANGE NAME NAME VARCHAR(255) NOT NULL")
    cur.execute("ALTER TABLE APPLICATION CHANGE VERSION VERSION VARCHAR(255) NOT NULL")
    cur.execute("ALTER TABLE APPLICATION DROP KEY UNQ_APPLICATION_0")
    cur.execute("ALTER TABLE APPLICATION ADD CONSTRAINT UNQ_APPLICATION_0 UNIQUE (FACILITY_ID, NAME, VERSION)")

    cur.execute("CREATE TABLE DATACOLLECTIONDATAFILE (ID BIGINT NOT NULL, CREATE_ID VARCHAR(255) NOT NULL, CREATE_TIME DATETIME NOT NULL, MOD_ID VARCHAR(255) NOT NULL, MOD_TIME DATETIME NOT NULL, DATACOLLECTION_ID BIGINT NOT NULL, DATAFILE_ID BIGINT NOT NULL, PRIMARY KEY (ID))")
    cur.execute("ALTER TABLE DATACOLLECTIONDATAFILE ADD CONSTRAINT UNQ_DATACOLLECTIONDATAFILE_0 UNIQUE (DATACOLLECTION_ID, DATAFILE_ID)")
        
    cur.execute("CREATE TABLE DATACOLLECTIONDATASET (ID BIGINT NOT NULL, CREATE_ID VARCHAR(255) NOT NULL, CREATE_TIME DATETIME NOT NULL, MOD_ID VARCHAR(255) NOT NULL, MOD_TIME DATETIME NOT NULL, DATACOLLECTION_ID BIGINT NOT NULL, DATASET_ID BIGINT NOT NULL, PRIMARY KEY (ID))")
    cur.execute("ALTER TABLE DATACOLLECTIONDATASET ADD CONSTRAINT UNQ_DATACOLLECTIONDATASET_0 UNIQUE (DATACOLLECTION_ID, DATASET_ID)")
    
    cur.execute("CREATE TABLE DATACOLLECTION (ID BIGINT NOT NULL, CREATE_ID VARCHAR(255) NOT NULL, CREATE_TIME DATETIME NOT NULL, MOD_ID VARCHAR(255) NOT NULL, MOD_TIME DATETIME NOT NULL, PRIMARY KEY (ID))")
        
    cur.execute("CREATE TABLE DATACOLLECTIONPARAMETER (ID BIGINT NOT NULL, CREATE_ID VARCHAR(255) NOT NULL, CREATE_TIME DATETIME NOT NULL, DATETIME_VALUE DATETIME, ERROR DOUBLE, MOD_ID VARCHAR(255) NOT NULL, MOD_TIME DATETIME NOT NULL, NUMERIC_VALUE DOUBLE, RANGEBOTTOM DOUBLE, RANGETOP DOUBLE, STRING_VALUE VARCHAR(4000), DATACOLLECTION_ID BIGINT NOT NULL, PARAMETER_TYPE_ID BIGINT NOT NULL, PRIMARY KEY (ID))")
    cur.execute("ALTER TABLE DATACOLLECTIONPARAMETER ADD CONSTRAINT UNQ_DATACOLLECTIONPARAMETER_0 UNIQUE (DATACOLLECTION_ID, PARAMETER_TYPE_ID)")
    
    cur.execute("ALTER TABLE DATAFILEFORMAT CHANGE VERSION VERSION VARCHAR(255) NOT NULL")  
    
    cur.execute("ALTER TABLE DATAFILE CHANGE DATASET_ID DATASET_ID BIGINT NOT NULL")
    cur.execute("ALTER TABLE DATAFILE DROP KEY UNQ_DATAFILE_0")
    cur.execute("ALTER TABLE DATAFILE ADD CONSTRAINT UNQ_DATAFILE_0 UNIQUE (DATASET_ID, NAME)")
    
    cur.execute("ALTER TABLE DATASET DROP FOREIGN KEY FK_DATASET_TYPE")
    cur.execute("ALTER TABLE DATASET CHANGE INVESTIGATION_ID INVESTIGATION_ID BIGINT NOT NULL")
    cur.execute("ALTER TABLE DATASET CHANGE TYPE TYPE_ID BIGINT NOT NULL")
    cur.execute("ALTER TABLE DATASET CHANGE COMPLETE COMPLETE TINYINT(1) default 0 NOT NULL")
    cur.execute("ALTER TABLE DATASET DROP FOREIGN KEY FK_DATASET_INVESTIGATION_ID")
    cur.execute("ALTER TABLE DATASET DROP FOREIGN KEY FK_DATASET_SAMPLE_ID")
    cur.execute("ALTER TABLE DATASET DROP KEY UNQ_DATASET_0")
    cur.execute("ALTER TABLE DATASET ADD CONSTRAINT UNIQUE KEY UNQ_DATASET_0 (INVESTIGATION_ID, NAME)")
        
    cur.execute("CREATE TABLE INVESTIGATIONINSTRUMENT (ID BIGINT NOT NULL, CREATE_ID VARCHAR(255) NOT NULL, CREATE_TIME DATETIME NOT NULL, MOD_ID VARCHAR(255) NOT NULL, MOD_TIME DATETIME NOT NULL, INSTRUMENT_ID BIGINT NOT NULL, INVESTIGATION_ID BIGINT NOT NULL, PRIMARY KEY (ID))")
    cur.execute("SELECT CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, ID, INSTRUMENT_ID FROM INVESTIGATION WHERE INSTRUMENT_ID IS NOT NULL")
    rowsout = []
    rows = cur.fetchall()
    ID = 0L
    if rows:
        for CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, INVESTIGATION_ID, INSTRUMENT_ID in rows:
            rowsout.append((ID, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, INVESTIGATION_ID, INSTRUMENT_ID))
            ID += 1       
        cur.executemany("insert into INVESTIGATIONINSTRUMENT(ID, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, INVESTIGATION_ID, INSTRUMENT_ID) values (%s, %s, %s, %s, %s, %s, %s)", rowsout)
        con.commit()
    cur.execute("ALTER TABLE INVESTIGATIONINSTRUMENT ADD CONSTRAINT UNQ_INVESTIGATIONINSTRUMENT_0 UNIQUE (INVESTIGATION_ID, INSTRUMENT_ID)")

        
    cur.execute("ALTER TABLE INVESTIGATION DROP FOREIGN KEY FK_INVESTIGATION_FACILITY_CYCLE_ID")
    cur.execute("ALTER TABLE INVESTIGATION DROP FOREIGN KEY FK_INVESTIGATION_INSTRUMENT_ID")
    cur.execute("ALTER TABLE INVESTIGATION CHANGE VISIT_ID VISIT_ID VARCHAR(255) NOT NULL")
    cur.execute("ALTER TABLE INVESTIGATION DROP FACILITY_CYCLE_ID")
    cur.execute("ALTER TABLE INVESTIGATION DROP INSTRUMENT_ID")
    cur.execute("ALTER TABLE INVESTIGATION DROP KEY UNQ_INVESTIGATION_0")
    cur.execute("ALTER TABLE INVESTIGATION ADD CONSTRAINT UNQ_INVESTIGATION_0 UNIQUE (FACILITY_ID, NAME, VISIT_ID)")
        
    cur.execute("ALTER TABLE JOB ADD ARGUMENTS VARCHAR(255)")
    cur.execute("ALTER TABLE JOB ADD INPUTDATACOLLECTION_ID BIGINT")
    cur.execute("ALTER TABLE JOB ADD OUTPUTDATACOLLECTION_ID BIGINT")
    
    cur.execute("SELECT ID, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME FROM JOB")
    jobs = cur.fetchall();

    ID = 0L
    IDS = 0L
    IDF = 0L
    for job in jobs:
        jobId, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME = job
        cur.execute("SELECT DATASET_ID FROM INPUTDATASET WHERE JOB_ID =" + str(jobId))
        datasets = cur.fetchall()  
        cur.execute("SELECT DATAFILE_ID FROM INPUTDATAFILE WHERE JOB_ID =" + str(jobId))
        datafiles = cur.fetchall()
        if datasets or datafiles:
            cur.execute("INSERT INTO DATACOLLECTION(ID, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME) values (%s, %s, %s, %s, %s)", (ID, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME))
            cur.execute("UPDATE JOB SET INPUTDATACOLLECTION_ID = " + str(ID) + " WHERE ID = " + str(jobId)) 
            for dataset in datasets:
                dsid = dataset[0]
                cur.execute("INSERT INTO DATACOLLECTIONDATASET(ID, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, DATACOLLECTION_ID, DATASET_ID)  values (%s, %s, %s, %s, %s, %s, %s)", (IDS, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, ID, dsid))
                IDS += 1
            for datafile in datafiles:
                dfid = datafile[0]
                cur.execute("INSERT INTO DATACOLLECTIONDATAFILE(ID, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, DATACOLLECTION_ID, DATAFILE_ID)  values (%s, %s, %s, %s, %s, %s, %s)", (IDF, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, ID, dfid))
                IDF += 1
            ID += 1
    
        cur.execute("SELECT DATASET_ID FROM OUTPUTDATASET WHERE JOB_ID =" + str(jobId))
        datasets = cur.fetchall()
        cur.execute("SELECT DATAFILE_ID FROM OUTPUTDATAFILE WHERE JOB_ID =" + str(jobId))
        datafiles = cur.fetchall()
        if datasets or datafiles:
            cur.execute("INSERT INTO DATACOLLECTION(ID, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME) values (%s, %s, %s, %s, %s)", (ID, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME)) 
            cur.execute("UPDATE JOB SET OUTPUTDATACOLLECTION_ID = " + str(ID) + " WHERE ID = " + str(jobId))
            for dataset in datasets:
                dsid = dataset[0] 
                cur.execute("INSERT INTO DATACOLLECTIONDATASET(ID, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, DATACOLLECTION_ID, DATASET_ID)  values (%s, %s, %s, %s, %s, %s, %s)", (IDS, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, ID, dsid))
                IDS += 1
            for datafile in datafiles:
                dfid = datafile[0]
                cur.execute("INSERT INTO DATACOLLECTIONDATAFILE(ID, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, DATACOLLECTION_ID, DATAFILE_ID)  values (%s, %s, %s, %s, %s, %s, %s)", (IDF, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, ID, dfid))
                IDF += 1
            ID += 1
    
    cur.execute("CREATE TABLE LOG (ID BIGINT NOT NULL, CREATE_ID VARCHAR(255) NOT NULL, CREATE_TIME DATETIME NOT NULL, DURATION BIGINT, ENTITYID BIGINT, ENTITYNAME VARCHAR(255), MOD_ID VARCHAR(255) NOT NULL, MOD_TIME DATETIME NOT NULL, OPERATION VARCHAR(255), QUERY VARCHAR(255), PRIMARY KEY (ID))")
        
    cur.execute("ALTER TABLE PARAMETERTYPE ADD APPLICABLETODATACOLLECTION TINYINT(1) default 0")
    cur.execute("ALTER TABLE PARAMETERTYPE CHANGE UNITS UNITS VARCHAR(255) NOT NULL")
    
    cur.execute("ALTER TABLE RULE DROP FOREIGN KEY FK_RULE_GROUP_ID")
    cur.execute("ALTER TABLE RULE RENAME RULE_")
    cur.execute("ALTER TABLE RULE_ CHANGE WHAT WHAT VARCHAR(255) NOT NULL")
    cur.execute("ALTER TABLE RULE_ ")
    
    cur.execute("ALTER TABLE SAMPLE DROP KEY UNQ_SAMPLE_0")
    cur.execute("ALTER TABLE SAMPLE ADD CONSTRAINT UNQ_SAMPLE_0 UNIQUE (INVESTIGATION_ID, NAME)")
    
    cur.execute("ALTER TABLE SAMPLETYPE CHANGE MOLECULARFORMULA MOLECULARFORMULA VARCHAR(255) NOT NULL")
    cur.execute("ALTER TABLE SAMPLETYPE DROP KEY UNQ_SAMPLETYPE_0")
    cur.execute("ALTER TABLE SAMPLETYPE ADD CONSTRAINT UNQ_SAMPLETYPE_0 UNIQUE (FACILITY_ID, NAME, MOLECULARFORMULA)")
    
    cur.execute("ALTER TABLE USERGROUP DROP FOREIGN KEY FK_USERGROUP_GROUP_NAME")
    cur.execute("ALTER TABLE USERGROUP DROP FOREIGN KEY FK_USERGROUP_USER_NAME")
    cur.execute("ALTER TABLE USERGROUP CHANGE GROUP_NAME GROUP_ID BIGINT NOT NULL")
    cur.execute("ALTER TABLE USERGROUP CHANGE USER_NAME USER_ID BIGINT NOT NULL")

def dropTables():
    cur.execute("DROP TABLE NOTIFICATIONREQUEST")
    cur.execute("DROP TABLE INPUTDATAFILE")
    cur.execute("DROP TABLE INPUTDATASET")
    cur.execute("DROP TABLE OUTPUTDATAFILE")
    cur.execute("DROP TABLE OUTPUTDATASET")
        
def check():
    checkNotNull("APPLICATION", "NAME")
    checkNotNull("APPLICATION", "VERSION")
    
    checkNotNull("DATAFILEFORMAT", "VERSION")
    
    checkNotNull("DATAFILE", "DATASET_ID")
    
    checkNotNull("DATASET", "INVESTIGATION_ID")
    
    checkNotNull("INVESTIGATION", "VISIT_ID", "'N/A'")
    
    checkNotNull("PARAMETERTYPE", "UNITS", "'None'")
    
    checkNotNull("RULE", "WHAT")
    
    checkNotNull("SAMPLETYPE", "MOLECULARFORMULA", "Not specified")
    
    checkUnique("DATAFILE", "DATASET_ID", "NAME")
    checkUnique("DATASET", "INVESTIGATION_ID", "NAME")
    checkUnique("INVESTIGATION", "FACILITY_ID", "NAME", "VISIT_ID")
    checkUnique("SAMPLE", "INVESTIGATION_ID", "NAME")
    checkUnique("SAMPLETYPE", "FACILITY_ID", "NAME", "MOLECULARFORMULA")
    
def checkUnique(table, *column):
    global fail
    columns = ",".join(column)
    query = "SELECT " + columns + ",count(*) FROM " + table + " GROUP BY " + columns + " HAVING COUNT(*) > 1"
    print "Looking for duplicates with", query
    count = cur.execute(query)
    if count:
        fail = True
        print "Please eliminate duplicates:"
        rows = cur.fetchall()
        for row in rows:
            print row
      
def checkNotNull(table, column, replace=None): 
    global fail
    count = cur.execute("SELECT * FROM " + table + " WHERE " + column + " IS NULL")
    if count:
        fail = True
        print "The following entries in table", table, "have null values for", column
        rows = cur.fetchall()
        for row in rows:
            print row
        if replace:
            print "Try: UPDATE", table, "SET", column, "=", replace, "WHERE", column, "IS NULL;"
        

cur = con.cursor()
    
cur.execute("SELECT VERSION()")
print "Database version", cur.fetchone()[0]

facility_id = None
count = cur.execute("SELECT ID, NAME FROM FACILITY")
if count == 1 and not facility_name:
    facility = cur.fetchone()
    facility_id = facility[0]
    print "Existing applications will be associated with Facility:", facility[1]
elif count == 0:
    abort("No Facilities defined - why are you you migrating this?")
else:
    facilities = cur.fetchall()
    if not facility_name:
        print "Available facilities are:",
        for facility in facilities:
            print " '" + facility[1] + "'",
        print
        abort ("More than one facility exists please specify one")
    else:
        for facility in facilities:
            if facility[1].upper() == facility_name.upper():
                facility_id = facility[0]
                break;
        if not facility_id:
            abort("Facility " + facility_name + " not found")        

print "Will now start checking"
fail = False
# check()
if fail:
    print "Please fix above errors and try again ;-)"
if not fail: 
    print "All checks passed"
#    change()
    addForeignKeys()
    dropTables
    print "Upgrade complete"







