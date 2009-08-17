REM scripts required to bring the IKitten install from phase 1 to phase 2


REM gathers the information required to run SQL*Plus scripts, then
REM runs those scripts.


PROMPT
PROMPT I K i t t e n   U P G R A D E
PROMPT
PROMPT This script will upgrade a phase 1 IKitten schema to phase 2.  It should
PROMPT only be run against a phase 1 IKitten schema.
PROMPT


undefine database_name
undefine ikitten_username
undefine ikitten_password

set define ON

ACCEPT database_name CHAR prompt         'Enter Database Name       : '
ACCEPT ikitten_username CHAR prompt      'Enter ikitten schema name : '
ACCEPT ikitten_password CHAR hide prompt 'Enter ikitten password    : '


prompt
prompt Testing connection...
connect &ikitten_username/&ikitten_password@&database_name


prompt
prompt
prompt If there are any error messages above then please CLOSE SQL*PLus NOW
prompt and report the error...
pause ...or press <Return> to continue
prompt


prompt Creating log directory...
define log_dir = log\&database_name\&ikitten_username\

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


define logfile = UPGRADE_001_add_create_id.log
SPOOL &log_dir&logfile
set define OFF
@001_add_create_id.sql
set define ON
SPOOL OFF

define logfile = UPGRADE_002_IKITTEN_ONLY_populate_create_id.log
SPOOL &log_dir&logfile
set define OFF
@002_IKITTEN_ONLY_populate_create_id.sql
set define ON
SPOOL OFF

define logfile = UPGRADE_003_create_id_not_null.log
SPOOL &log_dir&logfile
set define OFF
@003_create_id_not_null.sql
set define ON
SPOOL OFF

define logfile = UPGRADE_004_deleted_column.log
SPOOL &log_dir&logfile
set define OFF
@004_deleted_column.sql
set define ON
SPOOL OFF

define logfile = UPGRADE_005_cascade_deletes.log
SPOOL &log_dir&logfile
set define OFF
@005_cascade_deletes.sql
set define ON
SPOOL OFF

define logfile = UPGRADE_006_user_roles.log
SPOOL &log_dir&logfile
set define OFF
@006_user_roles.sql
set define ON
SPOOL OFF

define logfile = UPGRADE_007_src_hash.log
SPOOL &log_dir&logfile
set define OFF
@007_src_hash.sql
set define ON
SPOOL OFF

define logfile = UPGRADE_009_partition_datafile_parameter.log
SPOOL &log_dir&logfile
set define OFF
@../install_scripts/general_scripts/schema_mods/003_partition_datafile_parameter.sql
set define ON
SPOOL OFF

define logfile = UPGRADE_013_investigation_app_form_blob.log
SPOOL &log_dir&logfile
set define OFF
@013_investigation_app_form_blob.sql
set define ON
SPOOL OFF

define logfile = UPGRADE_014_facility_user_deferrable.log
SPOOL &log_dir&logfile
set define OFF
@../install_scripts/general_scripts/schema_mods/004_facility_user_deferrable.sql
set define ON
SPOOL OFF

define logfile = UPGRADE_015_datafile_name_not_null.log
SPOOL &log_dir&logfile
set define OFF
@015_datafile_name_not_null.sql
set define ON
SPOOL OFF






define logfile = UPGRADE_097_v_investigation.log
SPOOL &log_dir&logfile
set define OFF
@../install_scripts/general_scripts/common_objects/010_v_investigation.sql
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
