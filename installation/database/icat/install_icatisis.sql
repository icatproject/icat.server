REM gathers the information required to run SQL*Plus scripts, then
REM runs those scripts.



PROMPT
PROMPT I C A T I S I S   I N S T A L L
PROMPT
PROMPT This script will create ICATISIS schema objects in a named schema in a
PROMPT specified database.  
PROMPT


define parameter_datafile_icat_type = isis

undefine database_name
undefine sis_password
REM undefine icatisis_username
undefine icatisis_password
undefine externaltable_location
undefine dataportal_password
undefine icatuser_password
define testicat_username = testicat
define testicat_password = password
set define ON

ACCEPT database_name CHAR prompt          'Enter Database Name             : '
ACCEPT sys_password CHAR hide prompt      'Enter SYS password              : '
REM ACCEPT icatisis_username CHAR prompt      'Enter ICATISIS schema name      : '
ACCEPT icatisis_password CHAR prompt      'Enter icat password       : '
ACCEPT dataportal_password CHAR prompt    'Enter dataportal password       : '
ACCEPT icatuser_password CHAR prompt      'Enter icatuser   password       : '
ACCEPT externaltables_location CHAR prompt 'Enter External tables location : '


prompt
prompt ====================================================================
prompt creating user icat
prompt


connect sys/&sys_password@&database_name as sysdba
REM CREATE USER icat PROFILE "DEFAULT" IDENTIFIED BY "&icatisis_password" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP" QUOTA UNLIMITED ON "USERS" ACCOUNT UNLOCK;
CREATE USER icat PROFILE "DEFAULT" IDENTIFIED BY "&icatisis_password" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP" QUOTA UNLIMITED ON "USERS" ACCOUNT UNLOCK;
GRANT CREATE DATABASE LINK TO icat;
GRANT CREATE LIBRARY TO icat;
GRANT CREATE MATERIALIZED VIEW TO icat;
GRANT CREATE OPERATOR TO icat;
GRANT CREATE PROCEDURE TO icat;
GRANT CREATE PUBLIC DATABASE LINK TO icat;
GRANT CREATE PUBLIC SYNONYM TO icat;
GRANT CREATE SEQUENCE TO icat;
GRANT CREATE SESSION TO icat;
GRANT CREATE SYNONYM TO icat;
GRANT CREATE TABLE TO icat;
GRANT CREATE TRIGGER TO icat;
GRANT CREATE TYPE TO icat;
GRANT CREATE VIEW TO icat;
GRANT UNLIMITED TABLESPACE TO icat;
GRANT EXECUTE ON "SYS"."DBMS_RLS" TO icat;
GRANT SELECT ON "SYS"."V_$SESSION" TO icat;
GRANT SELECT ON "SYS"."V_$SESSTAT" TO icat;
GRANT SELECT ON "SYS"."V_$STATNAME" TO icat;
GRANT "CONNECT" TO icat;
GRANT "PLUSTRACE" TO icat;
GRANT "RESOURCE" TO icat;
GRANT CREATE JOB TO icat;


CREATE OR REPLACE DIRECTORY external_tables as '&externaltables_location';
GRANT READ ON DIRECTORY external_tables TO icat;


prompt
prompt ====================================================================
prompt creating user dataportal
prompt

CREATE USER dataportal PROFILE "DEFAULT" IDENTIFIED BY "&dataportal_password" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP" QUOTA UNLIMITED ON "USERS" ACCOUNT UNLOCK;
GRANT CREATE DATABASE LINK TO dataportal;
GRANT CREATE LIBRARY TO dataportal;
GRANT CREATE MATERIALIZED VIEW TO dataportal;
GRANT CREATE OPERATOR TO dataportal;
GRANT CREATE PROCEDURE TO dataportal;
GRANT CREATE PUBLIC DATABASE LINK TO dataportal;
GRANT CREATE PUBLIC SYNONYM TO dataportal;
GRANT CREATE SEQUENCE TO dataportal;
GRANT CREATE SESSION TO dataportal;
GRANT CREATE SYNONYM TO dataportal;
GRANT CREATE TABLE TO dataportal;
GRANT CREATE TRIGGER TO dataportal;
GRANT CREATE TYPE TO dataportal;
GRANT CREATE VIEW TO dataportal;
GRANT UNLIMITED TABLESPACE TO dataportal;
GRANT "CONNECT" TO dataportal;
GRANT "PLUSTRACE" TO dataportal;
GRANT "RESOURCE" TO dataportal;
GRANT CREATE JOB TO dataportal;

prompt
prompt ====================================================================
prompt creating user icatuser
prompt

CREATE USER icatuser PROFILE "DEFAULT" IDENTIFIED BY "&icatuser_password" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP" QUOTA UNLIMITED ON "USERS" ACCOUNT UNLOCK;
GRANT CREATE DATABASE LINK TO icatuser;
GRANT CREATE LIBRARY TO icatuser;
GRANT CREATE MATERIALIZED VIEW TO icatuser;
GRANT CREATE OPERATOR TO icatuser;
GRANT CREATE PROCEDURE TO icatuser;
GRANT CREATE PUBLIC DATABASE LINK TO icatuser;
GRANT CREATE PUBLIC SYNONYM TO icatuser;
GRANT CREATE SEQUENCE TO icatuser;
GRANT CREATE SESSION TO icatuser;
GRANT CREATE SYNONYM TO icatuser;
GRANT CREATE TABLE TO icatuser;
GRANT CREATE TRIGGER TO icatuser;
GRANT CREATE TYPE TO icatuser;
GRANT CREATE VIEW TO icatuser;
GRANT UNLIMITED TABLESPACE TO icatuser;
GRANT "CONNECT" TO icatuser;
GRANT "PLUSTRACE" TO icatuser;
GRANT "RESOURCE" TO icatuser;
GRANT CREATE JOB TO icatuser;

prompt
prompt ====================================================================
prompt creating user testicat
prompt


connect sys/&sys_password@&database_name as sysdba
REM CREATE USER testicat PROFILE "DEFAULT" IDENTIFIED BY "&testicat_password" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP" QUOTA UNLIMITED ON "USERS" ACCOUNT UNLOCK;
CREATE USER testicat PROFILE "DEFAULT" IDENTIFIED BY "&testicat_password" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP" QUOTA UNLIMITED ON "USERS" ACCOUNT UNLOCK;
GRANT CREATE DATABASE LINK TO testicat;
GRANT CREATE LIBRARY TO testicat;
GRANT CREATE MATERIALIZED VIEW TO testicat;
GRANT CREATE OPERATOR TO testicat;
GRANT CREATE PROCEDURE TO testicat;
GRANT CREATE PUBLIC DATABASE LINK TO testicat;
GRANT CREATE PUBLIC SYNONYM TO testicat;
GRANT CREATE SEQUENCE TO testicat;
GRANT CREATE SESSION TO testicat;
GRANT CREATE SYNONYM TO testicat;
GRANT CREATE TABLE TO testicat;
GRANT CREATE TRIGGER TO testicat;
GRANT CREATE TYPE TO testicat;
GRANT CREATE VIEW TO testicat;
GRANT UNLIMITED TABLESPACE TO testicat;
GRANT EXECUTE ON "SYS"."DBMS_RLS" TO testicat;
GRANT SELECT ON "SYS"."V_$SESSION" TO testicat;
GRANT SELECT ON "SYS"."V_$SESSTAT" TO testicat;
GRANT SELECT ON "SYS"."V_$STATNAME" TO testicat;
GRANT "CONNECT" TO testicat;
GRANT "PLUSTRACE" TO testicat;
GRANT "RESOURCE" TO testicat;
GRANT CREATE JOB TO testicat;

CREATE OR REPLACE DIRECTORY external_tables as '&externaltables_location';
GRANT READ ON DIRECTORY external_tables TO testicat;



prompt
prompt Testing connection...
connect icat/&icatisis_password@&database_name


prompt
prompt
prompt If there are any error messages above then please CLOSE SQL*PLus NOW
prompt and report the error...
pause ...or press <Return> to continue
prompt


prompt Creating log directory for icat...
define log_dir = log\&database_name\icat\

host mkdir log
host mkdir log\&database_name
host mkdir &log_dir

prompt
prompt Log files will be written to &log_dir and ../dataportal/log/.
prompt If this directory does not exist then it should be created, or log files
prompt may not be written.
prompt

pause Press <Return> if the directory exists; the installation will now begin
prompt =======================================================================
prompt


REM create tables etc, common to all schemas
REM log_prefix: A
define logfile = A_script1.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts\script1.sql
set define ON
SPOOL OFF



REM Database objects which cannot be handled by the JDeveloper schema designer
REM (this next one handles its own logging)
REM log_prefix: AA
@install_schema_mods.sql



REM objects specific to this schema, which modify objects on which the common
REM objects depend
REM log_prefix: B
REM <no scripts for this schema>



REM generic objects for all schemas
REM (this next one handles its own logging)
REM log_prefix: C
@install_common_objects.sql


define logfile = D_050_create_loq_extern_table.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatisis\2_after_common_objects\050_create_loq_extern_table.sql
set define ON
SPOOL OFF

define logfile = D_055_update_loq_flag.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatisis\2_after_common_objects\055_update_loq_flag.sql
set define ON
SPOOL OFF


define logfile = D_062_proc_set_inv_run_number_range.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatisis\2_after_common_objects\062_proc_set_inv_run_number_range.sql
set define ON
SPOOL OFF




define logfile = D_070_set_jobs.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatisis\2_after_common_objects\070_set_jobs.sql
set define ON
SPOOL OFF


define logfile = D_071_initialise_data.log
SPOOL &log_dir&logfile
set define OFF
@data\initialise_data.sql
set define ON
SPOOL OFF


REM remaining general scripts
REM log_prefix: E


prompt Compiling the schema...
BEGIN
  Dbms_Utility.compile_schema(schema => USER);
END;
/
prompt
prompt icat schema installation done!!
prompt ====================================================================
prompt


prompt
prompt ====================================================================
prompt installing dataportal schema
prompt

connect dataportal/&dataportal_password@&database_name
undefine log_dir

rem define log_dir = ../dataportal/log/
rem host mkdir ../dataportal/log

prompt Creating log directory for dataportal...
define log_dir = log\&database_name\dataportal\

host mkdir log
host mkdir log\&database_name
host mkdir &log_dir


define logfile = dataportal_schema_create_oracle.log
SPOOL &log_dir&logfile
set define OFF
@../dataportal/dataportal_schema_create_oracle.sql
set define ON
SPOOL OFF

define logfile = dataportal_schema_insert_oracle.log
SPOOL &log_dir&logfile
set define OFF
@../dataportal/dataportal_schema_insert_oracle.sql
set define ON
SPOOL OFF

define logfile = dataportal_schema_update_oracle.log
SPOOL &log_dir&logfile
set define OFF
@../dataportal/dataportal_schema_update_oracle.sql
set define ON
SPOOL OFF

prompt
prompt dataportal schema installation done!!
prompt ====================================================================
prompt


prompt
prompt ====================================================================
prompt installing testicat schema for unit testing
prompt


connect testicat/&testicat_password@&database_name
undefine log_dir

rem define log_dir = ../testicat/log/
rem host mkdir ../testicat/log

prompt Creating log directory for testicat...
define log_dir = log\&database_name\testicat\

host mkdir log
host mkdir log\&database_name
host mkdir &log_dir

REM use script1.sql (as in icat schema installation) to create tables etc, common to all schemas
define logfile = testicat_schema_install.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts\script1.sql
set define ON
SPOOL OFF

REM use script1.sql (as in icat schema installation) to create tables etc, common to all schemas
define logfile = testicat_install_sequences.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts\common_objects\008_id_sequences.sql
set define ON
SPOOL OFF


REM insert list table data (as in icat installation)
define logfile = testicat_initialise_data.log
SPOOL &log_dir&logfile
set define OFF
@data\initialise_data.sql
set define ON
SPOOL OFF

REM insert test data for unit testing...
define logfile = testicat_insert_test_data.log
SPOOL &log_dir&logfile
set define OFF
@unit_test\insert_test_data.sql
set define ON
SPOOL OFF


prompt
prompt ====================================================================
prompt unit testing schema installation done.
prompt


prompt
prompt ====================================================================
prompt Installation complete.  Please check the log files.
prompt
exit;


