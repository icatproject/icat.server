REM gathers the information required to run SQL*Plus scripts, then
REM runs those scripts.



PROMPT
PROMPT I C A T    I N S T A L L
PROMPT
PROMPT This script will create ICAT schema objects in a named schema in a
PROMPT specified database.  
PROMPT


define parameter_datafile_icat_type = isis

undefine database_name
undefine sys_password
undefine icat_username
undefine icat_password
undefine externaltable_location
undefine icatuser_password
define ICAT = icat
define ICATLOG = icatlog
define TESTICAT = testicat
define testicat_username = testicat
define testicat_password = password
define testicatuser_username = testicatuser
define testicatuser_password = password
set define ON

ACCEPT database_name CHAR prompt           'Enter Database Name             : '
ACCEPT sys_password CHAR hide prompt       'Enter SYS password              : '
REM ACCEPT icat_username CHAR prompt       'Enter ICAT schema name          : '
ACCEPT icat_password CHAR prompt           'Enter icat password             : '
ACCEPT icatuser_password CHAR prompt       'Enter icatuser   password       : '
ACCEPT icatlog_password CHAR prompt        'Enter icatlog password	    :'
ACCEPT externaltables_location CHAR prompt 'Enter External tables location  : '

connect sys/&sys_password@&database_name as sysdba
VARIABLE separator varchar2(1);
DECLARE
   sepstring  CHAR(100);
BEGIN
  select dbms_utility.port_string into sepstring from dual;
  if substr(sepstring,0,5) = 'Linux' then
	:separator := '/';
  else
	:separator := '\\';
  end if;
END;
/
column vappidcol new_value SEPPATH noprint
SELECT :separator vappidcol from dual;


prompt
prompt ====================================================================
prompt creating user icat
prompt


connect sys/&sys_password@&database_name as sysdba
REM CREATE USER icat PROFILE "DEFAULT" IDENTIFIED BY "&icat_password" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP" QUOTA UNLIMITED ON "USERS" ACCOUNT UNLOCK;
CREATE USER icat PROFILE "DEFAULT" IDENTIFIED BY "&icat_password" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP" QUOTA UNLIMITED ON "USERS" ACCOUNT UNLOCK;
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
prompt creating user icatlog
prompt


connect sys/&sys_password@&database_name as sysdba
REM CREATE USER icatlog PROFILE "DEFAULT" IDENTIFIED BY "&icatlog_password" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP" QUOTA UNLIMITED ON "USERS" ACCOUNT UNLOCK;
CREATE USER icatlog PROFILE "DEFAULT" IDENTIFIED BY "&icatlog_password" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP" QUOTA UNLIMITED ON "USERS" ACCOUNT UNLOCK;
GRANT CREATE DATABASE LINK TO icatlog;
GRANT CREATE LIBRARY TO icatlog;
GRANT CREATE MATERIALIZED VIEW TO icatlog;
GRANT CREATE OPERATOR TO icatlog;
GRANT CREATE PROCEDURE TO icatlog;
GRANT CREATE PUBLIC DATABASE LINK TO icatlog;
GRANT CREATE PUBLIC SYNONYM TO icatlog;

GRANT CREATE SEQUENCE TO icatlog;
GRANT CREATE SESSION TO icatlog;
GRANT CREATE SYNONYM TO icatlog;
GRANT CREATE TABLE TO icatlog;
GRANT CREATE TRIGGER TO icatlog;
GRANT CREATE TYPE TO icatlog;
GRANT CREATE VIEW TO icatlog;
GRANT UNLIMITED TABLESPACE TO icatlog;
GRANT EXECUTE ON "SYS"."DBMS_RLS" TO icatlog;
GRANT SELECT ON "SYS"."V_$SESSION" TO icatlog;
GRANT SELECT ON "SYS"."V_$SESSTAT" TO icatlog;
GRANT SELECT ON "SYS"."V_$STATNAME" TO icatlog;
GRANT "CONNECT" TO icatlog;
GRANT "PLUSTRACE" TO icatlog;
GRANT "RESOURCE" TO icatlog;
GRANT CREATE JOB TO icatlog;


CREATE OR REPLACE DIRECTORY external_tables as '&externaltables_location';
GRANT READ ON DIRECTORY external_tables TO icatlog;

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
prompt ====================================================================
prompt creating user testicatuser
prompt

CREATE USER testicatuser PROFILE "DEFAULT" IDENTIFIED BY "&testicatuser_password" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP" QUOTA UNLIMITED ON "USERS" ACCOUNT UNLOCK;
GRANT CREATE DATABASE LINK TO testicatuser;
GRANT CREATE LIBRARY TO testicatuser;
GRANT CREATE MATERIALIZED VIEW TO testicatuser;
GRANT CREATE OPERATOR TO testicatuser;
GRANT CREATE PROCEDURE TO testicatuser;
GRANT CREATE PUBLIC DATABASE LINK TO testicatuser;
GRANT CREATE PUBLIC SYNONYM TO testicatuser;
GRANT CREATE SEQUENCE TO testicatuser;
GRANT CREATE SESSION TO testicatuser;
GRANT CREATE SYNONYM TO testicatuser;
GRANT CREATE TABLE TO testicatuser;
GRANT CREATE TRIGGER TO testicatuser;
GRANT CREATE TYPE TO testicatuser;
GRANT CREATE VIEW TO testicatuser;
GRANT UNLIMITED TABLESPACE TO testicatuser;
GRANT "CONNECT" TO testicatuser;
GRANT "PLUSTRACE" TO testicatuser;
GRANT "RESOURCE" TO testicatuser;
GRANT CREATE JOB TO testicatuser;

prompt
prompt Testing connection...
connect icat/&icat_password@&database_name


prompt
prompt
prompt If there are any error messages above then please CLOSE SQL*PLus NOW
prompt and report the error...
pause ...or press <Return> to continue
prompt


prompt Creating log directory for icat...
define log_dir = log&SEPPATH&database_name&SEPPATH&ICAT&SEPPATH

host mkdir log
host mkdir log&SEPPATH&database_name
host mkdir &log_dir

prompt
prompt Log files will be written to &log_dir
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
@general_scripts/script1.sql
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
@schema_specific_scripts/icatisis/2_after_common_objects/050_create_loq_extern_table.sql
set define ON
SPOOL OFF

define logfile = D_055_update_loq_flag.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts/icatisis/2_after_common_objects/055_update_loq_flag.sql
set define ON
SPOOL OFF


define logfile = D_062_proc_set_inv_run_number_range.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts/icatisis/2_after_common_objects/062_proc_set_inv_run_number_range.sql
set define ON
SPOOL OFF




define logfile = D_070_set_jobs.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts/icatisis/2_after_common_objects/070_set_jobs.sql
set define ON
SPOOL OFF


define logfile = D_071_initialise_data.log
SPOOL &log_dir&logfile
set define OFF
@data/initialise_data.sql
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
prompt Installing icatlog schema !!
prompt ====================================================================
prompt

connect icatlog/&icatlog_password@&database_name
set define OFF
@general_scripts/icat_logging.sql
set define ON
prompt  inserting default data
set define OFF
@data/initialise_logging.sql
set define ON
prompt
prompt icatlog schema installation done!!
prompt ====================================================================
prompt

prompt
prompt ====================================================================
prompt installing testicat schema for unit testing
prompt


connect testicat/&testicat_password@&database_name
undefine log_dir

rem define log_dir = ..&SEPPATHtesticat&SEPPATHlog&SEPPATH
rem host mkdir ..&SEPPATHtesticat&SEPPATHlog

prompt Creating log directory for testicat...
define log_dir = log&SEPPATH&database_name&SEPPATH&TESTICAT&SEPPATH

host mkdir log
host mkdir log&SEPPATH&database_name
host mkdir &log_dir

REM use script1.sql (as in icat schema installation) to create tables etc, common to all schemas
define logfile = testicat_schema_install.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/script1.sql
set define ON
SPOOL OFF

REM use script1.sql (as in icat schema installation) to create tables etc, common to all schemas
define logfile = testicat_install_sequences.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/008_id_sequences.sql
set define ON
SPOOL OFF


REM insert list table data (as in icat installation)
define logfile = testicat_initialise_data.log
SPOOL &log_dir&logfile
set define OFF
@data/initialise_data.sql
set define ON
SPOOL OFF

REM insert test data for unit testing...
define logfile = testicat_insert_test_data.log
SPOOL &log_dir&logfile
set define OFF
@unit_test/insert_test_data.sql
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


