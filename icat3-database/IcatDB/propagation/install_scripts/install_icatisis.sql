REM gathers the information required to run SQL*Plus scripts, then
REM runs those scripts.


REM Ensure the scripts in the "general_scripts" subdirectory are up to date
REM before running this script.  See the "~notes.txt" file in that directory for
REM details


PROMPT
PROMPT I C A T I S I S   I N S T A L L
PROMPT
PROMPT This script will create ICATISIS schema objects in a named schema in a
PROMPT specified database.  The schema must already exist and have permission to
PROMPT create tables, packages, triggers, sequences, synonyms, external tables
PROMPT and materialized views.
PROMPT


REM the define here ensures we select the correct parameters from the datafile
REM in the load_external_data_pkg package. clf, dls and isis parameters are all
REM in the same datafile.
define parameter_datafile_icat_type = isis



undefine database_name
undefine icatisis_username
undefine icatisis_password

set define ON

ACCEPT database_name CHAR prompt          'Enter Database Name        : '
ACCEPT icatisis_username CHAR prompt      'Enter ICATISIS schema name : '
ACCEPT icatisis_password CHAR hide prompt 'Enter ICATISIS password    : '

grant create job to &icatisis_username;

prompt
prompt Testing connection...
connect &icatisis_username/&icatisis_password@&database_name


prompt
prompt
prompt If there are any error messages above then please CLOSE SQL*PLus NOW
prompt and report the error...
pause ...or press <Return> to continue
prompt


prompt Creating log directory...
define log_dir = log\&database_name\&icatisis_username\

host mkdir log
host mkdir log\&database_name
host mkdir &log_dir

prompt
prompt Log files will be written to &log_dir.
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



REM remaining general scripts
REM log_prefix: E


prompt Compiling the schema...
BEGIN
  Dbms_Utility.compile_schema(schema => USER);
END;
/


prompt
prompt ====================================================================
prompt Installation complete.  Please check the log files.
prompt

