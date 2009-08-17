#!/bin/bash
# should be run as sys or system
# needs a corresponding cleanup script
# pre-requisites need to have DIRECTORY setup in Oracle 
# export external_dir=/home/oracle/external_tables_3_3
# mkdir $external_dir
# chmod 775 $external_dir
# chown -R oracle:dba $external_dir

export db_user=ICATISIS_TEST
export db_pass=fish4t
export db_external_dir_name=EXTERNAL_TABLES33 
export db_external_dir_location=/home/oracle/external_tables_3_3

sqlplus / as sysdba <<EOF

-- create directory

create or replace directory $db_external_dir_name as '$db_external_dir_location' ;

-- create user

create user $db_user identified by $db_pass ;

-- system privs

GRANT CREATE TYPE TO $db_user ;
GRANT CREATE TRIGGER TO $db_user ;
GRANT CREATE PROCEDURE TO $db_user ;
GRANT CREATE PUBLIC DATABASE LINK TO $db_user ;
GRANT CREATE DATABASE LINK TO $db_user ;
GRANT CREATE SEQUENCE TO $db_user ;
GRANT CREATE VIEW TO $db_user ;
GRANT CREATE SYNONYM TO $db_user ;
GRANT CREATE TABLE TO $db_user ;
GRANT EXECUTE ANY CLASS TO $db_user ;
GRANT UNLIMITED TABLESPACE TO $db_user ;

-- role privs

GRANT "CONNECT" TO $db_user ;
GRANT "PLUSTRACE" TO $db_user ;
GRANT "RESOURCE" TO $db_user ;

-- object privs

GRANT EXECUTE ON "SYS"."DBMS_LOCK" TO $db_user ;
GRANT WRITE ON DIRECTORY  $db_external_dir_name TO $db_user ;
GRANT READ ON DIRECTORY $db_external_dir_name TO $db_user ;

--  GRANT SELECT ON "ISISUSERDB"."PERSON" TO "ICATISIS"

exit
EOF


