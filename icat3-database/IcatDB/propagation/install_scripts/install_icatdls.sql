REM gathers the information required to run SQL*Plus scripts, then
REM runs those scripts.

REM Ensure the scripts in the "general_scripts" subdirectory are up to date
REM before running this script.  See the "~notes.txt" file in that directory for
REM details


PROMPT
PROMPT I C A T D L S   I N S T A L L
PROMPT
PROMPT This script will create ICATDLS schema objects in a named schema in a
PROMPT specified database.  The schema must already exist and have permission to
PROMPT create tables, packages, triggers, sequences, synonyms, external tables
PROMPT and materialized views.
PROMPT

Prompt this is to be initially run by the Sys user to grant correct permissions

REM the define here ensures we select the correct parameters from the datafile
REM in the load_external_data_pkg package. clf, dls and isis parameters are all
REM in the same datafile.
define parameter_datafile_icat_type = dls



undefine database_name
undefine icatdls_username
undefine icatdls_password

set define ON
set validate off

ACCEPT database_name CHAR prompt         'Enter Database Name       : '
ACCEPT icatdls_username CHAR prompt      'Enter ICATDLS schema name : '
ACCEPT icatdls_password CHAR hide prompt 'Enter ICATDLS password    : '

grant execute on dbms_lock to &icatdls_username;
grant read on directory external_tables to &icatdls_username;
grant write on directory external_tables to &icatdls_username;


prompt
prompt Testing connection...
connect &icatdls_username/&icatdls_password@&database_name

prompt
prompt
prompt If there are any error messages above then please CLOSE SQL*PLus NOW
prompt and report the error...
pause ...or press <Return> to continue
prompt


prompt Creating log directory...
define log_dir = log\&database_name\&icatdls_username\

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

define logfile = create_temp_samplesheet_table.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\create_temp_samplesheet_table.sql
set define ON
SPOOL OFF

define logfile = create_cleanup_sample_parameter_table.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\create_cleanup_sample_parameter_table.sql
set define ON
SPOOL OFF

define logfile = create_federal_investigation_view.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\create_federal_investigation_view.sql
set define ON
SPOOL OFF



define logfile = D_001_tt_inv_unique_fields.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\001_tt_inv_unique_fields.sql
set define ON
SPOOL OFF

define logfile = D_002_create_beamline_instrument_table.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\002_create_beamline_instrument_table.sql
set define ON
SPOOL OFF

define logfile = D_003_create_beamline_synonyms.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\003_create_beamline_synonyms.sql
set define ON
SPOOL OFF

define logfile = D_004_create_duodesk_DB_link.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\004_create_duodesk_DB_link.sql
set define ON
SPOOL OFF

define logfile = 019_dls_migration.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\019_dls_migration_tmp1.sql
set define ON
SPOOL OFF

define logfile = batch_single_migration.sql.log
SPOOL &log_dir&logfile
@schema_specific_scripts\icatdls\2_after_common_objects\batch_single_migration.sql
SPOOL OFF

define logfile = icatdls_batch_migration_investigation.log
SPOOL &log_dir&logfile
@schema_specific_scripts\icatdls\2_after_common_objects\icatdls_batch_migration_investigaton_pkg.sql
SPOOL OFF

define logfile = icatdls_batch_migration_publication.log
SPOOL &log_dir&logfile
@schema_specific_scripts\icatdls\2_after_common_objects\icatdls_batch_migration_publication_pkg.sql
SPOOL OFF

define logfile = populate_single_beamline.sql.log
SPOOL &log_dir&logfile
@schema_specific_scripts\icatdls\2_after_common_objects\populate_single_beamline.sql
SPOOL OFF

define logfile = icatdls_populate_beamlines.log
SPOOL &log_dir&logfile
@schema_specific_scripts\icatdls\2_after_common_objects\icatdls_populate_beamlines.sql
SPOOL OFF

define logfile = icatdls_cleanupicat.log
SPOOL &log_dir&logfile
@schema_specific_scripts\icatdls\2_after_common_objects\icatdls_cleanupicat.sql
SPOOL OFF

define logfile = icatdls_cleanupikitten.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\icatdls_cleanupikitten.sql
set define ON
SPOOL OFF

define logfile = pkg_icat.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\pkg_icat.sql
set define ON
SPOOL OFF



define logfile = D_070_set_jobs.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\070_set_jobs.sql
set define ON
SPOOL OFF

define logfile = DisableIkittenForeignConstraint.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\DisableIkittenForeignConstraint.sql
set define ON
SPOOL OFF

define logfile = alterschema.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\alterschema.sql
set define ON
SPOOL OFF

define logfile = email_problem.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\email_problem.sql
set define ON
SPOOL OFF

define logfile = createtrigger.log
SPOOL &log_dir&logfile
@schema_specific_scripts\icatdls\2_after_common_objects\createtrigger.sql
SPOOL OFF

define logfile = check_missing_instrument.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\check_missing_instrument.sql
set define ON
SPOOL OFF

define logfile = propagate_single_visit.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\propagate_single_visit.sql
set define ON
SPOOL OFF

define logfile = test_links.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\test_links.sql
set define ON
SPOOL OFF

define logfile = shift_time_fn.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\shift_time_fn.sql
set define ON
SPOOL OFF

define logfile = is_number_fn.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\is_number_fn.sql
set define ON
SPOOL OFF

define logfile = mytable_type.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\mytable_type.sql
set define ON
SPOOL OFF

define logfile = in_list_fn.log
SPOOL &log_dir&logfile
set define OFF
@schema_specific_scripts\icatdls\2_after_common_objects\in_list_fn.sql
set define ON
SPOOL OFF



REM remaining general scripts
REM log_prefix: E

--define logfile = E_01_dpal.log
--SPOOL &log_dir&logfile
--set define OFF
--@general_scripts\dpal.sql
--set define ON
--SPOOL OFF



prompt Compiling the schema...
BEGIN
  Dbms_Utility.compile_schema(schema => '&icatdls_username');
END;
/


prompt
prompt ====================================================================
prompt Installation complete.  Please check the log files.
prompt
exit;
