#!/usr/bin/env python
from __future__ import print_function

import MySQLdb as mdb
from optparse import OptionParser
import sys

if sys.version_info[0] > 2:
    long = int              # Python 3 does not have a long type as all ints a 'big'

username = 'icat'
password = 'icat'
schema = 'icat'
dbhost = 'localhost'

con = mdb.connect(dbhost, username, password, schema);

parser = OptionParser()
parser.add_option("--facility", "-f", help="Name of facility to relate to applications - not needed if exactly one exists")

opts, args = parser.parse_args()

facility_name = opts.facility

def abort(msg):
    print(msg, file=sys.stderr)
    sys.exit(1)
    
def change():
    cur.execute("ALTER TABLE APPLICATION ADD FACILITY_ID BIGINT")   
    cur.execute("UPDATE APPLICATION SET FACILITY_ID = " + str(facility_id))
    cur.execute("ALTER TABLE APPLICATION CHANGE FACILITY_ID FACILITY_ID BIGINT NOT NULL")
    cur.execute("ALTER TABLE APPLICATION CHANGE NAME NAME VARCHAR(255) NOT NULL")
    cur.execute("ALTER TABLE APPLICATION CHANGE VERSION VERSION VARCHAR(255) NOT NULL")
    cur.execute("CREATE TABLE DATACOLLECTIONDATAFILE (ID BIGINT NOT NULL, CREATE_ID VARCHAR(255) NOT NULL, CREATE_TIME DATETIME NOT NULL, MOD_ID VARCHAR(255) NOT NULL, MOD_TIME DATETIME NOT NULL, DATACOLLECTION_ID BIGINT NOT NULL, DATAFILE_ID BIGINT NOT NULL, PRIMARY KEY (ID))")
    cur.execute("CREATE TABLE DATACOLLECTIONDATASET (ID BIGINT NOT NULL, CREATE_ID VARCHAR(255) NOT NULL, CREATE_TIME DATETIME NOT NULL, MOD_ID VARCHAR(255) NOT NULL, MOD_TIME DATETIME NOT NULL, DATACOLLECTION_ID BIGINT NOT NULL, DATASET_ID BIGINT NOT NULL, PRIMARY KEY (ID))")
    cur.execute("CREATE TABLE DATACOLLECTION (ID BIGINT NOT NULL, CREATE_ID VARCHAR(255) NOT NULL, CREATE_TIME DATETIME NOT NULL, MOD_ID VARCHAR(255) NOT NULL, MOD_TIME DATETIME NOT NULL, PRIMARY KEY (ID))")
    cur.execute("CREATE TABLE DATACOLLECTIONPARAMETER (ID BIGINT NOT NULL, CREATE_ID VARCHAR(255) NOT NULL, CREATE_TIME DATETIME NOT NULL, DATETIME_VALUE DATETIME, ERROR DOUBLE, MOD_ID VARCHAR(255) NOT NULL, MOD_TIME DATETIME NOT NULL, NUMERIC_VALUE DOUBLE, RANGEBOTTOM DOUBLE, RANGETOP DOUBLE, STRING_VALUE VARCHAR(4000), DATACOLLECTION_ID BIGINT NOT NULL, PARAMETER_TYPE_ID BIGINT NOT NULL, PRIMARY KEY (ID))")
    cur.execute("ALTER TABLE DATAFILEFORMAT CHANGE VERSION VERSION VARCHAR(255) NOT NULL")  
    cur.execute("ALTER TABLE DATAFILE CHANGE DATASET_ID DATASET_ID BIGINT NOT NULL")
    cur.execute("ALTER TABLE DATASET CHANGE INVESTIGATION_ID INVESTIGATION_ID BIGINT NOT NULL")
    cur.execute("ALTER TABLE DATASET CHANGE TYPE TYPE_ID BIGINT NOT NULL")
    cur.execute("ALTER TABLE DATASET CHANGE COMPLETE COMPLETE TINYINT(1) default 0 NOT NULL")
    cur.execute("ALTER TABLE GROUP_ RENAME GROUPING")
    cur.execute("ALTER TABLE INSTRUMENT ADD URL VARCHAR(255)")    
    cur.execute("CREATE TABLE INVESTIGATIONINSTRUMENT (ID BIGINT NOT NULL, CREATE_ID VARCHAR(255) NOT NULL, CREATE_TIME DATETIME NOT NULL, MOD_ID VARCHAR(255) NOT NULL, MOD_TIME DATETIME NOT NULL, INSTRUMENT_ID BIGINT NOT NULL, INVESTIGATION_ID BIGINT NOT NULL, PRIMARY KEY (ID))")
    cur.execute("SELECT CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, ID, INSTRUMENT_ID FROM INVESTIGATION WHERE INSTRUMENT_ID IS NOT NULL")
    rowsout = []
    rows = cur.fetchall()
    ID = long(0)
    if rows:
        for CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, INVESTIGATION_ID, INSTRUMENT_ID in rows:
            rowsout.append((ID, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, INVESTIGATION_ID, INSTRUMENT_ID))
            ID += 1       
        cur.executemany("insert into INVESTIGATIONINSTRUMENT(ID, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME, INVESTIGATION_ID, INSTRUMENT_ID) values (%s, %s, %s, %s, %s, %s, %s)", rowsout)
        con.commit()
    cur.execute("ALTER TABLE INVESTIGATION CHANGE VISIT_ID VISIT_ID VARCHAR(255) NOT NULL")
    cur.execute("ALTER TABLE INVESTIGATION DROP FACILITY_CYCLE_ID")
    cur.execute("ALTER TABLE INVESTIGATION DROP INSTRUMENT_ID")
    cur.execute("ALTER TABLE JOB ADD ARGUMENTS VARCHAR(255)")
    cur.execute("ALTER TABLE JOB ADD INPUTDATACOLLECTION_ID BIGINT")
    cur.execute("ALTER TABLE JOB ADD OUTPUTDATACOLLECTION_ID BIGINT")
    
    cur.execute("SELECT ID, CREATE_ID, CREATE_TIME, MOD_ID, MOD_TIME FROM JOB")
    jobs = cur.fetchall();

    ID = long(0)
    IDS = long(0)
    IDF = long(0)
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
    cur.execute("CREATE TABLE PUBLICSTEP (ID BIGINT NOT NULL, CREATE_ID VARCHAR(255) NOT NULL, CREATE_TIME DATETIME NOT NULL, FIELD VARCHAR(32) NOT NULL, MOD_ID VARCHAR(255) NOT NULL, MOD_TIME DATETIME NOT NULL, ORIGIN VARCHAR(32) NOT NULL, PRIMARY KEY (ID))")
    cur.execute("DROP TABLE RULE")
    cur.execute("CREATE TABLE RULE_ (ID BIGINT NOT NULL, BEAN VARCHAR(255), C TINYINT(1) default 0, CREATE_ID VARCHAR(255) NOT NULL, CREATE_TIME DATETIME NOT NULL, CRUDFLAGS VARCHAR(4) NOT NULL, CRUDJPQL VARCHAR(1024), D TINYINT(1) default 0, FROMJPQL VARCHAR(1024), INCLUDEJPQL VARCHAR(1024), MOD_ID VARCHAR(255) NOT NULL, MOD_TIME DATETIME NOT NULL, R TINYINT(1) default 0, RESTRICTED TINYINT(1) default 0, U TINYINT(1) default 0, VARCOUNT INTEGER, WHAT VARCHAR(255) NOT NULL, WHEREJPQL VARCHAR(1024), GROUPING_ID BIGINT, PRIMARY KEY (ID))")
    cur.execute("ALTER TABLE SAMPLETYPE CHANGE MOLECULARFORMULA MOLECULARFORMULA VARCHAR(255) NOT NULL")
    cur.execute("ALTER TABLE USERGROUP CHANGE GROUP_NAME GROUP_ID BIGINT NOT NULL")
    cur.execute("ALTER TABLE USERGROUP CHANGE USER_NAME USER_ID BIGINT NOT NULL")

def dropTables():
    cur.execute("DROP TABLE NOTIFICATIONREQUEST")
    cur.execute("DROP TABLE INPUTDATAFILE")
    cur.execute("DROP TABLE INPUTDATASET")
    cur.execute("DROP TABLE OUTPUTDATAFILE")
    cur.execute("DROP TABLE OUTPUTDATASET")
    
def dropKeys(): 
    query = "select table_name, constraint_name from information_schema.KEY_COLUMN_USAGE where table_schema = '" + schema + "' and referenced_table_name is not null;"
    cur.execute(query)
    rows = cur.fetchall()
    for row in rows:
        table = row[0]
        if table in tables:
            query = "ALTER TABLE " + table + " DROP FOREIGN KEY " + row[1]
            print(query)
            cur.execute(query)
                       
    for table in tables:
        cur.execute("SHOW INDEX IN " + table)   
        rows = cur.fetchall()
        names = {}
        for row in rows:
            names[row[2]] = None
        del names["PRIMARY"]
        for index in names.keys():          
            query = "ALTER TABLE " + table + " DROP INDEX " + index
            print(query)
            cur.execute(query)
            
    for table in tables:
        cur.execute("SHOW CREATE TABLE " + table) 
        rows = cur.fetchall()
        for row in rows:
            create = row[1]
            if not "InnoDB" in create:
                query = "ALTER TABLE " + table + " ENGINE InnoDB"
                print(query)
                cur.execute(query)
                           
def check():
    checkNotNull("APPLICATION", "NAME")
    checkNotNull("APPLICATION", "VERSION")
    
    checkNotNull("DATAFILEFORMAT", "VERSION")
    
    checkNotNull("DATAFILE", "DATASET_ID")
    
    checkNotNull("DATASET", "INVESTIGATION_ID")
    
    checkNotNull("INVESTIGATION", "VISIT_ID", "'N/A'")
    
    checkNotNull("PARAMETERTYPE", "UNITS", "'None'")
    
    checkNotNull("RULE", "WHAT")
    
    checkNotNull("SAMPLETYPE", "MOLECULARFORMULA", "'Not specified'")
    
    checkUnique("DATAFILE", "DATASET_ID", "NAME")
    checkUnique("DATASET", "INVESTIGATION_ID", "NAME")
    checkUnique("INVESTIGATION", "FACILITY_ID", "NAME", "VISIT_ID")
    checkUnique("SAMPLE", "INVESTIGATION_ID", "NAME")
    checkUnique("SAMPLETYPE", "FACILITY_ID", "NAME", "MOLECULARFORMULA")
    
def checkUnique(table, *column):
    global fail
    columns = ",".join(column)
    query = "SELECT " + columns + ",count(*) FROM " + table + " GROUP BY " + columns + " HAVING COUNT(*) > 1"
    print("Looking for duplicates with", query)
    count = cur.execute(query)
    if count:
        fail = True
        print("Please eliminate duplicates:")
        rows = cur.fetchall()
        for row in rows:
            print(row)
      
def checkNotNull(table, column, replace=None): 
    global fail
    query = "SELECT * FROM " + table + " WHERE " + column + " IS NULL"
    print("Looking for nulls with", query)
    count = cur.execute(query) 
    rows = cur.fetchall()
    if rows:
        fail = True
        print("There are" , len(rows), "entries in table", table, "with null values for", column + ". Some are shown below:")
        for row in rows[:10]:
            print(row)
        if replace:
            print("Try: UPDATE", table, "SET", column, "=", replace, "WHERE", column, "IS NULL;")
  
  
cur = con.cursor()
    
cur.execute("SELECT VERSION()")
print("Database version", cur.fetchone()[0])

facility_id = None
count = cur.execute("SELECT ID, NAME FROM FACILITY")
if count == 1 and not facility_name:
    facility = cur.fetchone()
    facility_id = facility[0]
    print("Existing applications will be associated with Facility:", facility[1])
elif count == 0:
    abort("No Facilities defined - why are you you migrating this?")
else:
    facilities = cur.fetchall()
    if not facility_name:
        print("Available facilities are:",)
        for facility in facilities:
            print(" '" + facility[1] + "'",)
        print()
        abort ("More than one facility exists please specify one")
    else:
        for facility in facilities:
            if facility[1].upper() == facility_name.upper():
                facility_id = facility[0]
                break;
        if not facility_id:
            abort("Facility " + facility_name + " not found")        

tables = ["APPLICATION", "DATAFILE", "DATAFILEFORMAT", "DATAFILEPARAMETER", "DATASET", "DATASETPARAMETER", "DATASETTYPE",
          "FACILITY", "FACILITYCYCLE", "GROUP_", "INSTRUMENT", "INSTRUMENTSCIENTIST", "INVESTIGATION", "INVESTIGATIONPARAMETER",
          "INVESTIGATIONTYPE", "INVESTIGATIONUSER", "JOB", "KEYWORD", "PARAMETERTYPE", "PERMISSIBLESTRINGVALUE", "PUBLICATION",
          "RELATEDDATAFILE", "RULE", "SAMPLE", "SAMPLEPARAMETER", "SAMPLETYPE", "SEQUENCE", "SESSION_", "SHIFT", "STUDY",
          "STUDYINVESTIGATION", "USERGROUP", "USER_"]

print("Will now start checking")
fail = False
check()
if fail:
    print("Please fix above errors and try again ;-)")
if not fail: 
    print("All checks passed")
    dropKeys()
    change()
    dropTables()
    f = open("upgrade_mysql_4_2_5.sql")
    for line in f:
        line = line.strip()
        if line:
            print(line)
            cur.execute(line)
    f.close()

    print("Upgrade complete")







