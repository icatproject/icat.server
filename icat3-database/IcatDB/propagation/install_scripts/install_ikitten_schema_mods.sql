REM calls the schema mod scripts in the right order.
REM called from the schema install scripts in this directory.

REM the log directory (log_dir) is defined in the calling script

REM Created on 19/06/2008
REM Author: Keir Hawker
REM Purpose: This is the same as a the install_schema_mod.sql
REM however it no longer had a reference to the partiioned tables
REM as this isa feature that does not work on Ikittens


define logfile = AA_001_unique_federal_id.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/schema_mods/001_unique_federal_id.sql
set define ON
SPOOL OFF


define logfile = AA_004_facility_user_deferrable.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/schema_mods/004_facility_user_deferrable.sql
set define ON
SPOOL OFF
