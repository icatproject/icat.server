REM calls the schema mod scripts in the right order.
REM called from the schema install scripts in this directory.

REM the log directory (log_dir) is defined in the calling script



define logfile = AA_001_unique_federal_id.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/schema_mods/001_unique_federal_id.sql
set define ON
SPOOL OFF




define logfile = AA_003_partition_datafile_parameter.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/schema_mods/003_partition_datafile_parameter.sql
set define ON
SPOOL OFF



define logfile = AA_004_facility_user_deferrable.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/schema_mods/004_facility_user_deferrable.sql
set define ON
SPOOL OFF

define logfile = AA_005_partition_icat_authorisation.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/schema_mods/005_partition_icat_authorisation.sql
set define ON
SPOOL OFF
