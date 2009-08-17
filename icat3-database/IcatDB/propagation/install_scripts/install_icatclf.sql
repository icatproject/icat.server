REM gathers the information required to run SQL*Plus scripts, then
REM runs those scripts.

REM Ensure the scripts in the "general_scripts" subdirectory are up to date
REM before running this script.  See the "~notes.txt" file in that directory for
REM details


PROMPT
PROMPT I C A T C L F   I N S T A L L
PROMPT
PROMPT This script will create ICATCLF schema objects in a named schema in a
PROMPT specified database.  The schema must already exist and have permission to
PROMPT create tables, packages, triggers, sequences, synonyms, external tables
PROMPT and materialized views.
PROMPT


REM the define here ensures we select the correct parameters from the datafile
REM in the load_external_data_pkg package. clf, dls and isis parameters are all
REM in the same datafile.
define parameter_datafile_icat_type = clf



undefine database_name
undefine icatclf_username
undefine icatclf_password

set define ON

ACCEPT database_name CHAR prompt         'Enter Database Name       : '
ACCEPT icatclf_username CHAR prompt      'Enter ICATCLF schema name : '
ACCEPT icatclf_password CHAR hide prompt 'Enter ICATCLF password    : '


prompt
prompt Testing connection...
connect &icatclf_username/&icatclf_password@&database_name


prompt
prompt
prompt If there are any error messages above then please CLOSE SQL*PLus NOW
prompt and report the error...
pause ...or press <Return> to continue
prompt


prompt Creating log directory...
define log_dir = log\&database_name\&icatclf_username\

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



REM objects specific to this schema which cannot be or do not need to be created
REM earlier in the install
REM log_prefix: D


define logfile = 019_dls_migration.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatclf\2_after_common_objects\019_dls_migration_tmp1.sql
set define ON
SPOOL OFF

define logfile = D_020_icatdls_batch_migration_pkg.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatclf\2_after_common_objects\020_icatdls_batch_migration_pkg.sql
set define ON
SPOOL OFF

define logfile = D_030_icat_authorisation_pkg.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatclf\2_after_common_objects\030_icat_authorisation_pkg.sql
set define ON
SPOOL OFF

define logfile = D_035_icat_authorisation_trigger.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatclf\2_after_common_objects\035_icat_authorisation_trigger.sql
set define ON
SPOOL OFF


define logfile = D_060_create_air_investigation_trg.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatclf\2_after_common_objects\060_create_air_investigation_trg.sql
set define ON
SPOOL OFF


define logfile = D_065_create_air_dataset_trg.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatclf\2_after_common_objects\065_create_air_dataset_trg.sql
set define ON
SPOOL OFF


define logfile = D_070_create_air_datafile_trg.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatclf\2_after_common_objects\070_create_air_datafile_trg.sql
set define ON
SPOOL OFF


REM remaining general scripts
REM log_prefix: E

define logfile = E_01_dpal.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts\dpal.sql
set define ON
SPOOL OFF



prompt Compiling the schema...
BEGIN
  Dbms_Utility.compile_schema(schema => USER);
END;
/


prompt
prompt ====================================================================
prompt Installation complete.  Please check the log files.
prompt
