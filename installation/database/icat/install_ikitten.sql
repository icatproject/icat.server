REM gathers the information required to run SQL*Plus scripts, then
REM runs those scripts.

REM Ensure the scripts in the "general_scripts" subdirectory are up to date
REM before running this script.  See the "~notes.txt" file in that directory for
REM details


PROMPT
PROMPT I K I T T E N   I N S T A L L
PROMPT
PROMPT This script will create IKITTEN schema objects in a named schema in a
PROMPT specified database.  The schema must already exist and have permission
PROMPT to create tables.
PROMPT
PROMPT


undefine database_name
undefine ikitten_username
undefine ikitten_password

set define ON

ACCEPT database_name CHAR prompt         'Enter Database Name       : '
ACCEPT ikitten_username CHAR prompt      'Enter IKITTEN schema name : '
ACCEPT ikitten_password CHAR hide prompt 'Enter IKITTEN password    : '


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
@install_ikitten_schema_mods.sql



REM objects specific to this schema, which modify objects on which the common
REM objects depend
REM log_prefix: B
REM <no scripts for this schema>



REM generic objects for all schemas
REM (this next one handles its own logging)
REM log_prefix: C
@install_common_objects_ikitten.sql



REM objects specific to this schema which cannot be or do not need to be created
REM earlier in the install
REM log_prefix: D
REM <no scripts for this schema>



prompt ====================================================================
prompt Installation complete.  Please check the log files.
prompt

