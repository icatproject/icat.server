REM calls the common object creaton scripts in the right order for an IKITTEN
REM installation.
REM called from the schema install scripts in this directory.

REM the log directory (log_dir) is defined in the calling script



define logfile = C_010_v_investigation.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/010_v_investigation.sql
set define ON
SPOOL OFF

