REM calls the common object creaton scripts in the right order.
REM called from the schema install scripts in this directory.

REM the log directory (log_dir) is defined in the calling script



define logfile = C_001_types.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/001_types.sql
set define ON
SPOOL OFF

define logfile = C_003_log_table_and_view.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/003_log_table_and_view.sql
set define ON
SPOOL OFF

define logfile = C_004_log_pkg.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/004_log_pkg.sql
set define ON
SPOOL OFF

define logfile = C_002_util_pkg.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/002_util_pkg.sql
set define ON
SPOOL OFF

define logfile = C_006_create_external_tables.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/006_create_external_tables.sql
set define ON
SPOOL OFF

--define logfile = C_007_load_external_data_pkg.log
--SPOOL &log_dir&logfile
----REM set define OFF.  DEFINE IS USED IN THIS PACKAGE
--@general_scripts/common_objects/007_load_external_data_pkg.sql
--REM set define ON
--SPOOL OFF

define logfile = C_008_id_sequences.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/008_id_sequences.sql
set define ON
SPOOL OFF

define logfile = C_009_set_deleted_pkg.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/009_set_deleted_pkg.sql
set define ON
SPOOL OFF

define logfile = C_010_v_investigation.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/010_v_investigation.sql
set define ON
SPOOL OFF

define logfile = C_011_create_dls_migration_tmp1.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/011_create_dls_migration_tmp1.sql
set define ON
SPOOL OFF

define logfile = C_012_global_parameters_pkg.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/012_global_parameters_pkg.sql
set define ON
SPOOL OFF


define logfile = D_061_proc_set_icat_auth_data_entities.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/061_proc_set_icat_auth_data_entities.sql
set define ON
SPOOL OFF

define logfile = D_063_proc_set_investigation_dates.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/063_proc_set_investigation_dates.sql
set define ON
SPOOL OFF


define logfile = D_065_create_air_dataset_trg.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/065_create_air_dataset_trg.sql
set define ON
SPOOL OFF

define logfile = D_066_air_investigation_trg.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/066_air_investigation_trg.sql
set define ON
SPOOL OFF

define logfile = D_067_invesitgation_date_trg.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/067_invesitgation_date_trg.sql
set define ON
SPOOL OFF


define logfile = D_068_ldap_authorisation_fn.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/068_ldap_authorisation_fn.sql
set define ON
SPOOL OFF

define logfile = D_069_investigator_trg.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/069_investigator_trg.sql
set define ON
SPOOL OFF


define logfile = D_071_UPDATE_ICAT_AUTH_WITH_GUARDIAN_AND_SUPER.log
SPOOL &log_dir&logfile
set define OFF
@general_scripts/common_objects/071_UPDATE_ICAT_AUTH_WITH_GUARDIAN_AND_SUPER.sql
set define ON
SPOOL OFF
