#!/bin/env python

import sys
try:
    import cx_Oracle
except:
    print "import cx_Oracle failed becuase shared library missing. Try something like: "
    print "export LD_LIBRARY_PATH=/u01/app/oracle/product/11.2.0/xe/lib/"
    sys.exit(1)

from optparse import OptionParser

username = 'CLF_ICAT_AG2'
password = 'icat'
db = '//localhost:1521/XE'

conString = username + "/" + password + '@' + db
try:
    con = cx_Oracle.connect(conString)
except:
    print "connect to " + conString + " failed";
    sys.exit(1) 

parser = OptionParser()
parser.add_option("--facility", "-f", help="Name of facility to relate to applications - not needed if exactly one exists")

opts, args = parser.parse_args()

facility_name = opts.facility

def abort(msg):
    print >> sys.stderr, msg
    sys.exit(1)
    
def change():
    cur.execute("ALTER TABLE APPLICATION ADD FACILITY_ID NUMBER(19)")   
    cur.execute("UPDATE APPLICATION SET FACILITY_ID = " + str(facility_id))
    cur.execute("ALTER TABLE APPLICATION MODIFY FACILITY_ID NUMBER(19) NOT NULL")
    cur.execute("ALTER TABLE APPLICATION MODIFY NAME VARCHAR2(255) NOT NULL")
    cur.execute("ALTER TABLE APPLICATION MODIFY VERSION VARCHAR2(255) NOT NULL")
    cur.execute("CREATE TABLE DATACOLLECTION (ID NUMBER(19) NOT NULL, CREATE_ID VARCHAR2(255) NOT NULL, CREATE_TIME TIMESTAMP NOT NULL, MOD_ID VARCHAR2(255) NOT NULL, MOD_TIME TIMESTAMP NOT NULL, PRIMARY KEY (ID))")
    cur.execute("CREATE TABLE DATACOLLECTIONDATAFILE (ID NUMBER(19) NOT NULL, CREATE_ID VARCHAR2(255) NOT NULL, CREATE_TIME TIMESTAMP NOT NULL, MOD_ID VARCHAR2(255) NOT NULL, MOD_TIME TIMESTAMP NOT NULL, DATACOLLECTION_ID NUMBER(19) NOT NULL, DATAFILE_ID NUMBER(19) NOT NULL, PRIMARY KEY (ID))")
    cur.execute("CREATE TABLE DATACOLLECTIONDATASET (ID NUMBER(19) NOT NULL, CREATE_ID VARCHAR2(255) NOT NULL, CREATE_TIME TIMESTAMP NOT NULL, MOD_ID VARCHAR2(255) NOT NULL, MOD_TIME TIMESTAMP NOT NULL, DATACOLLECTION_ID NUMBER(19) NOT NULL, DATASET_ID NUMBER(19) NOT NULL, PRIMARY KEY (ID))")
    cur.execute("CREATE TABLE DATACOLLECTIONPARAMETER (ID NUMBER(19) NOT NULL, CREATE_ID VARCHAR2(255) NOT NULL, CREATE_TIME TIMESTAMP NOT NULL, DATETIME_VALUE TIMESTAMP NULL, ERROR NUMBER(38,127) NULL, MOD_ID VARCHAR2(255) NOT NULL, MOD_TIME TIMESTAMP NOT NULL, NUMERIC_VALUE NUMBER(38,127) NULL, RANGEBOTTOM NUMBER(38,127) NULL, RANGETOP NUMBER(38,127) NULL, STRING_VALUE VARCHAR2(4000) NULL, DATACOLLECTION_ID NUMBER(19) NOT NULL, PARAMETER_TYPE_ID NUMBER(19) NOT NULL, PRIMARY KEY (ID))")
    cur.execute("ALTER TABLE DATAFILEFORMAT MODIFY VERSION VARCHAR2(255) NOT NULL")  
    cur.execute("ALTER TABLE DATAFILE MODIFY DATASET_ID NUMBER(19) NOT NULL")
    cur.execute("ALTER TABLE DATASET MODIFY INVESTIGATION_ID NUMBER(19) NOT NULL") 
    cur.execute("ALTER TABLE DATASET RENAME COLUMN TYPE TO TYPE_ID")
    cur.execute("ALTER TABLE DATASET MODIFY COMPLETE NUMBER(1) default 0 NOT NULL")
    cur.execute('ALTER TABLE GROUP_ RENAME TO GROUPING')     
    cur.execute("CREATE TABLE INVESTIGATIONINSTRUMENT (ID NUMBER(19) NOT NULL, CREATE_ID VARCHAR2(255) NOT NULL, CREATE_TIME TIMESTAMP NOT NULL, MOD_ID VARCHAR2(255) NOT NULL, MOD_TIME TIMESTAMP NOT NULL, INSTRUMENT_ID NUMBER(19) NOT NULL, INVESTIGATION_ID NUMBER(19) NOT NULL, PRIMARY KEY (ID))")
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
    cur.execute("ALTER TABLE INVESTIGATION MODIFY VISIT_ID VARCHAR2(255) NOT NULL")
    cur.execute("ALTER TABLE INVESTIGATION DROP COLUMN FACILITY_CYCLE_ID")
    cur.execute("ALTER TABLE INVESTIGATION DROP COLUMN INSTRUMENT_ID")
    cur.execute("ALTER TABLE JOB ADD ARGUMENTS VARCHAR(255)")
    cur.execute("ALTER TABLE JOB ADD INPUTDATACOLLECTION_ID NUMBER(19)")
    cur.execute("ALTER TABLE JOB ADD OUTPUTDATACOLLECTION_ID NUMBER(19)")
      
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
      
    cur.execute("CREATE TABLE LOG (ID NUMBER(19) NOT NULL, CREATE_ID VARCHAR2(255) NOT NULL, CREATE_TIME TIMESTAMP NOT NULL, DURATION NUMBER(19) NULL, ENTITYID NUMBER(19) NULL, ENTITYNAME VARCHAR2(255) NULL, MOD_ID VARCHAR2(255) NOT NULL, MOD_TIME TIMESTAMP NOT NULL, OPERATION VARCHAR2(255) NULL, QUERY VARCHAR2(255) NULL, PRIMARY KEY (ID))")
    cur.execute("ALTER TABLE PARAMETERTYPE ADD APPLICABLETODATACOLLECTION NUMBER(1) default 0")
    cur.execute("ALTER TABLE PARAMETERTYPE MODIFY UNITS VARCHAR2(255) NOT NULL")
    cur.execute("CREATE TABLE PUBLICSTEP (ID NUMBER(19) NOT NULL, CREATE_ID VARCHAR2(255) NOT NULL, CREATE_TIME TIMESTAMP NOT NULL, FIELD VARCHAR2(32) NOT NULL, MOD_ID VARCHAR2(255) NOT NULL, MOD_TIME TIMESTAMP NOT NULL, ORIGIN VARCHAR2(32) NOT NULL, PRIMARY KEY (ID))")
    cur.execute("DROP TABLE RULE")
    cur.execute("CREATE TABLE RULE_ (ID NUMBER(19) NOT NULL, BEAN VARCHAR2(255) NULL, C NUMBER(1) default 0 NULL, CREATE_ID VARCHAR2(255) NOT NULL, CREATE_TIME TIMESTAMP NOT NULL, CRUDFLAGS VARCHAR2(4) NOT NULL, CRUDJPQL VARCHAR2(1024) NULL, D NUMBER(1) default 0 NULL, FROMJPQL VARCHAR2(1024) NULL, INCLUDEJPQL VARCHAR2(1024) NULL, MOD_ID VARCHAR2(255) NOT NULL, MOD_TIME TIMESTAMP NOT NULL, R NUMBER(1) default 0 NULL, RESTRICTED NUMBER(1) default 0 NULL, U NUMBER(1) default 0 NULL, VARCOUNT NUMBER(10) NULL, WHAT VARCHAR2(255) NOT NULL, WHEREJPQL VARCHAR2(1024) NULL, GROUPING_ID NUMBER(19) NULL, PRIMARY KEY (ID))")
    cur.execute("ALTER TABLE SAMPLETYPE MODIFY MOLECULARFORMULA VARCHAR2(255) NOT NULL")
    cur.execute("ALTER TABLE USERGROUP RENAME COLUMN GROUP_NAME TO GROUP_ID")
    cur.execute("ALTER TABLE USERGROUP RENAME COLUMN USER_NAME TO USER_ID")

def dropTables():
    cur.execute("DROP TABLE NOTIFICATIONREQUEST")
    cur.execute("DROP TABLE INPUTDATAFILE")
    cur.execute("DROP TABLE INPUTDATASET")
    cur.execute("DROP TABLE OUTPUTDATAFILE")
    cur.execute("DROP TABLE OUTPUTDATASET")
    
def dropOld():
    for table in ['LOG', 'PUBLICSTEP', 'RULE_', 'GROUP', 'DATACOLLECTIONDATAFILE', 'DATACOLLECTIONDATASET', 'DATACOLLECTION',
                  'DATACOLLECTIONPARAMETER', 'INVESTIGATIONINSTRUMENT']:
        try:
            query = 'DROP TABLE ' + table
            cur.execute(query)
            print query
        except:
            pass
    
def dropKeys():       
    query = "select TABLE_NAME, CONSTRAINT_NAME, INDEX_NAME from USER_CONSTRAINTS where CONSTRAINT_TYPE IN ('R','U') and OWNER = '" + username + "'";
    cur.execute(query)
    rows = cur.fetchall()
    for row in rows:
        table, constraint, index = row
        if table in tables:
            query = "ALTER TABLE " + table + " DROP CONSTRAINT " + constraint
            print query
            cur.execute(query)
            if index:
                query = "DROP INDEX " + index
                print query
                cur.execute(query) 
            
    query = "select TABLE_NAME, INDEX_NAME from ALL_INDEXES where OWNER = 'CLF_ICAT_AG2' and not UNIQUENESS = 'UNIQUE'"
    cur.execute(query)
    rows = cur.fetchall()
    for row in rows:
        table = row[0]
        if table in tables:
            index = row[1]
            query = "DROP INDEX " + index
            print query
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
    cur.execute(query)
    rows = cur.fetchall()
    if rows:
        fail = True
        print "Please eliminate duplicates:"
        for row in rows:
            print row
      
def checkNotNull(table, column, replace=None): 
    global fail
    query = "SELECT * FROM " + table + " WHERE " + column + " IS NULL"
    print "Looking for nulls with", query
    cur.execute(query)
    rows = cur.fetchall()
    if rows:
        fail = True
        print "The following entries in table", table, "have null values for", column
        for row in rows:
            print row
        if replace:
            print "Try: UPDATE", table, "SET", column, "=", replace, "WHERE", column, "IS NULL;"
        

cur = con.cursor()
    
cur.execute("SELECT * FROM PRODUCT_COMPONENT_VERSION WHERE PRODUCT LIKE 'Oracle%'") 
print "Database version", cur.fetchone()

facility_id = None
cur.execute("SELECT ID, NAME FROM FACILITY")
facilities = cur.fetchall()
count = len(facilities)
if count == 1 and not facility_name:
    facility = facilities[0]
    facility_id = facility[0]
    print "Existing applications will be associated with Facility:", facility[1]
elif count == 0:
    abort("No Facilities defined - why are you you migrating this?")
else:
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

tables = ["APPLICATION", "DATAFILE", "DATAFILEFORMAT", "DATAFILEPARAMETER", "DATASET", "DATASETPARAMETER", "DATASETTYPE",
          "FACILITY", "FACILITYCYCLE", "GROUP_", "INSTRUMENT", "INSTRUMENTSCIENTIST", "INVESTIGATION", "INVESTIGATIONPARAMETER",
          "INVESTIGATIONTYPE", "INVESTIGATIONUSER", "JOB", "KEYWORD", "PARAMETERTYPE", "PERMISSIBLESTRINGVALUE", "PUBLICATION",
          "RELATEDDATAFILE", "RULE", "SAMPLE", "SAMPLEPARAMETER", "SAMPLETYPE", "SEQUENCE", "SESSION_", "SHIFT", "STUDY",
          "STUDYINVESTIGATION", "USERGROUP", "USER_"]

print "Will now start checking"
fail = False
# check()
if fail:
    print "Please fix above errors and try again ;-)"
if not fail: 
    print "All checks passed"
#     dropOld() 
#     dropKeys()
#     change()
    dropTables()
    f = open("upgrade_oracle_4_2_5.sql")
    for line in f:
        line = line.strip()
        if line:
            print line
            cur.execute(line)
    f.close()

    print "Upgrade complete"







