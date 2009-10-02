CREATE OR REPLACE PACKAGE load_external_data_pkg AS

/*

Name: load_external_data_pkg
Author: James Healy
Creation Date: November 2006

******** NOTE>>>> **********

It is intended for there to be a single script for this package but for the
package to be created in each icat schema.  Where there are differences in how
data are treated for each schema (eg for the parameter data file) the
parameter_datafile_icat_type substitution variabl determines what code is
executed.  This should reduce the maintenance required.

******** <<<<NOTE **********

Purpose:
  loads data from external tables into icat version 3 lookup tables.
  the external tables' datafiles are created by copying and pasting data from
  the worksheets of the spreadsheet of lookup data which has been distributed.

Description:
  each procedure named "load_xxx" loads data into the table "xxx".
  all NAME fields are loaded in lower case.
  errors are written to the log table.
  uses the Row_Number() function to ensure that only one record is inserted
    where duplicates exist in the file.



CHANGES
Date: 21-Feb-2007
Who: James Healy
Description:
  Include the new column "create_id".  This will be set to the value of the
  constant "LV_CREATE_ID" for any record in a lookup table which has a match in
  the spreadsheet of lookup data which this package reads when it populates the
  tables.
  Also brought changes in from the 1.0 tag (setting is_dataset_parameter in the
  load_parameter procedure).


*/


-- loads all data
PROCEDURE load_all(p_mod_id IN  datafile_format.mod_id%TYPE);

-- loads specific data
PROCEDURE load_datafile_format(p_mod_id IN datafile_format.mod_id%TYPE);
PROCEDURE load_dataset_status(p_mod_id IN dataset_status.mod_id%TYPE);
PROCEDURE load_dataset_type(p_mod_id IN dataset_type.mod_id%TYPE);
PROCEDURE load_facility_cycle(p_mod_id IN facility_cycle.mod_id%TYPE);
PROCEDURE load_instrument(p_mod_id IN instrument.mod_id%TYPE);
PROCEDURE load_investigation_type(p_mod_id IN investigation_type.mod_id%TYPE);
PROCEDURE load_parameter(p_mod_id IN parameter.mod_id%TYPE);
PROCEDURE load_study_status(p_mod_id IN study_status.mod_id%TYPE);
PROCEDURE load_icat_role(p_mod_id IN icat_role.mod_id%TYPE);
PROCEDURE load_this_icat(p_mod_id IN this_icat.mod_id%TYPE);
PROCEDURE load_fac_inst_scientist(p_mod_id IN this_icat.mod_id%TYPE);

FUNCTION get_create_id RETURN instrument.create_id%TYPE;

END load_external_data_pkg;
/

--##############################################################################

CREATE OR REPLACE PACKAGE BODY load_external_data_pkg AS


null_value_ex EXCEPTION;
PRAGMA EXCEPTION_INIT(null_value_ex, -1400);

external_table_ex EXCEPTION;
PRAGMA EXCEPTION_INIT(external_table_ex, -29913);


LV_CREATE_ID CONSTANT instrument.create_id%TYPE := 'FROM SPREADSHEET';

n PLS_INTEGER;

--------------------------------------------------------------------------------

-- returns the create_id value which this package sets on new rows
FUNCTION get_create_id RETURN instrument.create_id%TYPE IS
BEGIN
  RETURN LV_CREATE_ID;
END get_create_id;

--------------------------------------------------------------------------------

PROCEDURE log_and_raise_ext_tab_error(
  p_sqlerrm IN VARCHAR2,
  p_table IN VARCHAR2) IS

  lv_error VARCHAR2(1000);
BEGIN
  IF InStr(p_sqlerrm,'ORA-30653') > 0 THEN
    -- reject limit reached
    lv_error :=
      'The datafile for '||p_table||' contains records in an invalid format.'||
      '  See log files for details.';
  ELSIF InStr(p_sqlerrm, 'ORA-29400') > 0 THEN
    -- data cartridge error
    lv_error :=
      'The datafile for '||p_table||' could not be found.'||
      '  See log files for details.';
  ELSE
    lv_error := 'Error reading datafile for '||p_table||': '||SQLERRM;
  END IF;

  log_pkg.write_exception(lv_error);
  Raise_Application_Error(-20000, lv_error);
END;

--------------------------------------------------------------------------------

PROCEDURE load_datafile_format(p_mod_id IN datafile_format.mod_id%TYPE) IS

BEGIN
  SAVEPOINT load_datafile_format_sp;

  log_pkg.write_log('load_datafile_format: '||p_mod_id);

  MERGE INTO datafile_format target
  USING (
    SELECT name, version, format_type, description
    FROM(
      SELECT
        Lower(name) AS name, -- lowercase lookup name!
        version AS version,
        format_type AS format_type,
        description AS description,
        Row_Number() over(PARTITION BY Lower(name), version ORDER BY 1) rn
      FROM extern_datafile_format
      WHERE Upper(dls) = 'Y'
      )
    WHERE rn = 1) source
  ON(target.name = source.name
     AND target.version = source.version)
  WHEN matched THEN
    UPDATE SET
      format_type = source.format_type,
      description = source.description,
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = LV_CREATE_ID,
      create_time = nvl(mod_time,systimestamp),
      FACILITY_ACQUIRED = 'Y',
      deleted = 'N'
    WHERE (target.format_type != source.format_type
           OR (target.format_type IS NULL AND source.format_type IS NOT NULL)
           OR (target.format_type IS NOT NULL AND source.format_type IS NULL)
          )
    OR (target.description != source.description
         OR (target.description IS NULL AND source.description IS NOT NULL)
         OR (target.description IS NOT NULL AND source.description IS NULL)
        )
    OR target.create_id != LV_CREATE_ID
    OR target.create_time is null
    OR target.FACILITY_ACQUIRED is null
    OR target.deleted is null
  WHEN NOT matched THEN
    INSERT(
      name,
      version,
      format_type,
      description,
      mod_time,
      mod_id,
      create_id,
      create_time,
      FACILITY_ACQUIRED,
      deleted)
    VALUES(
      source.name,
      source.version,
      source.format_type,
      source.description,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N');

  log_pkg.write_log('load_datafile_format: finished');
EXCEPTION
  WHEN external_table_ex THEN
    ROLLBACK TO load_datafile_format_sp;
    log_and_raise_ext_tab_error(SQLERRM, 'DATAFILE_FORMAT');
  WHEN OTHERS THEN
    ROLLBACK TO load_datafile_format_sp;
    log_pkg.write_exception(
      'Could not populate DATAFILE_FORMAT: '||SQLERRM);
    RAISE;
END load_datafile_format;

--------------------------------------------------------------------------------

PROCEDURE load_dataset_status(p_mod_id IN dataset_status.mod_id%TYPE) IS
BEGIN
  SAVEPOINT load_dataset_status_sp;

  log_pkg.write_log('load_dataset_status: '||p_mod_id);

  MERGE INTO dataset_status target
  USING (
    SELECT name, description
    FROM(
      SELECT
        Lower(name) AS name, -- lowercase lookup name!
        description AS description,
        Row_Number() over(PARTITION BY Lower(name) ORDER BY 1) rn
      FROM extern_dataset_status
      WHERE Upper(dls) = 'Y'
      )
    WHERE rn = 1) source
  ON(target.name = source.name)
  WHEN matched THEN
    UPDATE SET
      description = source.description,
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = LV_CREATE_ID,
      create_time = nvl(mod_time, systimestamp),
      FACILITY_ACQUIRED = 'Y',
      deleted = 'N'
    WHERE (target.description != source.description
           OR (target.description IS NULL AND source.description IS NOT NULL)
           OR (target.description IS NOT NULL AND source.description IS NULL)
          )
   OR target.create_id != LV_CREATE_ID
    OR target.create_time is null
    OR target.FACILITY_ACQUIRED is null
    OR target.deleted is null
  WHEN NOT matched THEN
    INSERT(
      name,
      description,
      mod_time,
      mod_id,
      create_id,
      create_time,
      FACILITY_ACQUIRED,
      deleted)
    VALUES(
      source.name,
      source.description,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N');

  log_pkg.write_log('load_dataset_status: finished');
EXCEPTION
  WHEN external_table_ex THEN
    ROLLBACK TO load_dataset_status_sp;
    log_and_raise_ext_tab_error(SQLERRM, 'DATASET_STATUS');
  WHEN OTHERS THEN
    ROLLBACK TO load_dataset_status_sp;
    log_pkg.write_exception(
      'Could not populate DATASET_STATUS: '||SQLERRM);
    RAISE;
END load_dataset_status;

--------------------------------------------------------------------------------

PROCEDURE load_dataset_type(p_mod_id IN dataset_type.mod_id%TYPE) IS
BEGIN
  SAVEPOINT load_dataset_type_sp;

  log_pkg.write_log('load_dataset_type: '||p_mod_id);

  MERGE INTO dataset_type target
  USING (
    SELECT name, description
    FROM(
      SELECT
        Lower(name) AS name, -- lowercase lookup name!
        description AS description,
        Row_Number() over(PARTITION BY Lower(name) ORDER BY 1) rn
      FROM extern_dataset_type
      WHERE Upper(dls) = 'Y'
      )
    WHERE rn = 1) source
  ON(target.name = source.name)
  WHEN matched THEN
    UPDATE SET
      description = source.description,
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = LV_CREATE_ID,
      create_time = nvl(mod_time, systimestamp),
      FACILITY_ACQUIRED = 'Y',
      deleted = 'N'
    WHERE (target.description != source.description
           OR (target.description IS NULL AND source.description IS NOT NULL)
           OR (target.description IS NOT NULL AND source.description IS NULL)
          )
   OR target.create_id != LV_CREATE_ID
    OR target.create_time is null
    OR target.FACILITY_ACQUIRED is null
    OR target.deleted is null
  WHEN NOT matched THEN
    INSERT(
      name,
      description,
      mod_time,
      mod_id,
      create_id,
      create_time,
      FACILITY_ACQUIRED,
      deleted)
    VALUES(
      source.name,
      source.description,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N');

  log_pkg.write_log('load_dataset_type: finished');
EXCEPTION
  WHEN external_table_ex THEN
    ROLLBACK TO load_dataset_type_sp;
    log_and_raise_ext_tab_error(SQLERRM, 'DATASET_TYPE');
  WHEN OTHERS THEN
    ROLLBACK TO load_dataset_type_sp;
    log_pkg.write_exception(
      'Could not populate DATASET_TYPE: '||SQLERRM);
    RAISE;
END load_dataset_type;

--------------------------------------------------------------------------------

PROCEDURE load_facility_cycle(p_mod_id IN facility_cycle.mod_id%TYPE) IS
BEGIN
  SAVEPOINT load_facility_cycle_sp;

  log_pkg.write_log('load_facility_cycle: '||p_mod_id);

  MERGE INTO facility_cycle target
  USING (
    SELECT name, start_date, finish_date, description
    FROM(
      SELECT
        Lower(name) AS name, -- lowercase lookup name!
        CAST(start_date AS TIMESTAMP) start_date,
        CAST(finish_date AS TIMESTAMP) finish_date,
        description AS description,
        Row_Number() over(PARTITION BY Lower(name) ORDER BY 1) rn
      FROM extern_facility_cycle
      WHERE Upper(dls) = 'Y'
      )
    WHERE rn = 1) source
  ON(target.name = source.name)
  WHEN matched THEN
    UPDATE SET
      description = source.description,
      start_date = source.start_date,
      finish_date = source.finish_date,
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = LV_CREATE_ID,
      create_time = nvl(mod_time,systimestamp),
      FACILITY_ACQUIRED = 'Y',
      deleted = 'N'
    WHERE (target.description != source.description
           OR (target.description IS NULL AND source.description IS NOT NULL)
           OR (target.description IS NOT NULL AND source.description IS NULL)
          )
    OR (target.start_date != source.start_date
         OR (target.start_date IS NULL AND source.start_date IS NOT NULL)
         OR (target.start_date IS NOT NULL AND source.start_date IS NULL)
        )
    OR (target.finish_date != source.finish_date
         OR (target.finish_date IS NULL AND source.finish_date IS NOT NULL)
         OR (target.finish_date IS NOT NULL AND source.finish_date IS NULL)
        )
   OR target.create_id != LV_CREATE_ID
    OR target.create_time is null
    OR target.FACILITY_ACQUIRED is null
    OR target.deleted is null
  WHEN NOT matched THEN
    INSERT(
      name,
      description,
      start_date,
      finish_date,
      mod_time,
      mod_id,
      create_id,
      create_time,
      FACILITY_ACQUIRED,
      deleted)
    VALUES(
      source.name,
      source.description,
      source.start_date,
      source.finish_date,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N');

  log_pkg.write_log('load_facility_cycle: finished');
EXCEPTION
  WHEN external_table_ex THEN
    ROLLBACK TO load_facility_cycle_sp;
    log_and_raise_ext_tab_error(SQLERRM, 'FACILITY_CYCLE');
  WHEN OTHERS THEN
    ROLLBACK TO load_facility_cycle_sp;
    log_pkg.write_exception(
      'Could not populate FACILITY_CYCLE: '||SQLERRM);
    RAISE;
END load_facility_cycle;

--------------------------------------------------------------------------------

PROCEDURE load_instrument(p_mod_id IN instrument.mod_id%TYPE) IS
BEGIN
  SAVEPOINT load_instrument_sp;

  log_pkg.write_log('load_instrument: '||p_mod_id);

  MERGE INTO instrument target
  USING (
    SELECT name,short_name, type, description
    FROM(
      SELECT
        Lower(name) AS name, -- lowercase lookup name!
        type AS type,
        short_name AS short_name,
        description AS description,
        Row_Number() over(PARTITION BY Lower(name) ORDER BY 1) rn
      FROM extern_instrument
      WHERE Upper(dls) = 'Y'
      )
    WHERE rn = 1) source
  ON(target.name = source.name)
  WHEN matched THEN
    UPDATE SET
      description = source.description,
      type = source.type,
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = LV_CREATE_ID,
      create_time = nvl(mod_time, systimestamp),
      FACILITY_ACQUIRED = 'Y',
      deleted = 'N'
    WHERE (target.description != source.description
           OR (target.description IS NULL AND source.description IS NOT NULL)
           OR (target.description IS NOT NULL AND source.description IS NULL)
          )
    OR (target.type != source.type
         OR (target.type IS NULL AND source.type IS NOT NULL)
         OR (target.type IS NOT NULL AND source.type IS NULL)
        )
   OR target.create_id != LV_CREATE_ID
    OR target.create_time is null
    OR target.FACILITY_ACQUIRED is null
    OR target.deleted is null
  WHEN NOT matched THEN
    INSERT(
      name,
      short_name,
      description,
      type,
      mod_time,
      mod_id,
      create_id,
      create_time,
      FACILITY_ACQUIRED,
      deleted)
    VALUES(
      source.name,
      source.short_name,
      source.description,
      source.type,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N'
      );

  log_pkg.write_log('load_instrument: finished');
EXCEPTION
  WHEN external_table_ex THEN
    ROLLBACK TO load_instrument_sp;
    log_and_raise_ext_tab_error(SQLERRM, 'INSTRUMENT');
  WHEN OTHERS THEN
    ROLLBACK TO load_instrument_sp;
    log_pkg.write_exception(
      'Could not populate INSTRUMENT: '||SQLERRM);
    RAISE;
END load_instrument;

--------------------------------------------------------------------------------

PROCEDURE load_investigation_type(p_mod_id IN investigation_type.mod_id%TYPE) IS
BEGIN
  SAVEPOINT load_investigation_type_sp;

  log_pkg.write_log('load_investigation_type: '||p_mod_id);

  MERGE INTO investigation_type target
  USING (
    SELECT name, description
    FROM(
      SELECT
        Lower(name) AS name, -- lowercase lookup name!
        description AS description,
        Row_Number() over(PARTITION BY Lower(name) ORDER BY 1) rn
      FROM extern_investigation_type
      WHERE Upper(dls) = 'Y'
      )
    WHERE rn = 1) source
  ON(target.name = source.name)
  WHEN matched THEN
    UPDATE SET
      description = source.description,
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = LV_CREATE_ID,
      create_time = nvl(mod_time, systimestamp),
      FACILITY_ACQUIRED = 'Y',
      deleted = 'N'
    WHERE (target.description != source.description
           OR (target.description IS NULL AND source.description IS NOT NULL)
           OR (target.description IS NOT NULL AND source.description IS NULL)
          )
    OR target.create_id != LV_CREATE_ID
    OR target.create_time is null
    OR target.FACILITY_ACQUIRED is null
    OR target.deleted is null
  WHEN NOT matched THEN
    INSERT(
      name,
      description,
      mod_time,
      mod_id,
      create_id,
      create_time,
      FACILITY_ACQUIRED,
      deleted)
    VALUES(
      source.name,
      source.description,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N');

  log_pkg.write_log('load_investigation_type: finished');
EXCEPTION
  WHEN external_table_ex THEN
    ROLLBACK TO load_investigation_type_sp;
    log_and_raise_ext_tab_error(SQLERRM, 'INVESTIGATION_TYPE');
  WHEN OTHERS THEN
    ROLLBACK TO load_investigation_type_sp;
    log_pkg.write_exception(
      'Could not populate INVESTIGATION_TYPE: '||SQLERRM);
    RAISE;
END load_investigation_type;

--------------------------------------------------------------------------------

-- the columns SEARCHEABLE, NON_NUMERIC_VALUE_FORMAT, IS_SAMPLE_PARAMETER,
-- IS_DATASET_PARAMETER and IS_DATAFILE_PARAMETER are set the same as in an
-- earlier upload process.  Not sure of the provenance
PROCEDURE load_parameter(p_mod_id IN parameter.mod_id%TYPE) IS
BEGIN
  SAVEPOINT load_parameter_sp;

  log_pkg.write_log('load_parameter: '||p_mod_id);

  -- the 'parameter_datafile_icat_type' substitution variable should be set in
  -- the main install scripts.  it should take one of the values 'isis', 'clf'
  -- and 'dls' depending on the type of ICAT being installed.

  MERGE INTO parameter target
  USING (
    SELECT name, units, units_long_version, searchable, numeric_value,
           non_numeric_value_format, is_sample_parameter, is_dataset_parameter,
           is_datafile_parameter, description
    FROM(
      SELECT
        Lower(name) AS name, -- lowercase lookup name!
        Nvl(units,'N/A') AS units,
        units_long_version,
        'Y' AS searchable,
        Nvl(Upper(SubStr(numeric_value,1,1)),'N') AS numeric_value,
        NULL AS non_numeric_value_format,
        is_sample_parameter,
        is_dataset_parameter,
        is_datafile_parameter,
        description AS description,
        Row_Number() over(PARTITION BY Lower(name), units ORDER BY 1) rn
      FROM extern_parameter_list
      WHERE Upper(dls) = 'Y'
      )
    WHERE rn = 1) source
  ON (target.name = source.name
      AND target.units = source.units
      )
  WHEN matched THEN
  -- don't update the NON_NUMERIC_VALUE_FORMAT, SEARCHABLE or NUMERIC_VALUE
  -- columns: these are not specified in the datafile we're loading from and
  -- they may have been set to some value other than the default depending on
  -- their usage within the system.
  -- is_xxx_parameter columns may have been set to 'Y' by the main data
  -- propagation process, so we never update to 'N' here, nomatter what the
  -- value defined by the spreadsheet is
    UPDATE SET
      units_long_version = source.units_long_version,
      is_sample_parameter =
        Greatest(source.is_sample_parameter, is_sample_parameter),
      is_dataset_parameter =
        Greatest(source.is_dataset_parameter, is_dataset_parameter),
      is_datafile_parameter =
        Greatest(source.is_datafile_parameter, is_datafile_parameter),
      description = source.description,
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = LV_CREATE_ID,
      create_time =nvl(mod_time, systimestamp),
      FACILITY_ACQUIRED = 'Y',
      deleted = 'N'
    WHERE (target.units_long_version != source.units_long_version
            OR (target.units_long_version IS NULL AND source.units_long_version IS NOT NULL)
            OR (target.units_long_version IS NOT NULL AND source.units_long_version IS NULL)
           )
    OR target.is_sample_parameter < source.is_sample_parameter
    OR target.is_dataset_parameter < source.is_dataset_parameter
    OR target.is_datafile_parameter < source.is_datafile_parameter
    OR (target.description != source.description
         OR (target.description IS NULL AND source.description IS NOT NULL)
         OR (target.description IS NOT NULL AND source.description IS NULL)
        )
    OR target.create_id != LV_CREATE_ID
    OR target.create_time is null
    OR target.FACILITY_ACQUIRED is null
    OR target.deleted is null
  WHEN NOT matched THEN
    INSERT(
      name,
      units,
      units_long_version,
      searchable,
      numeric_value,
      non_numeric_value_format,
      is_sample_parameter,
      is_dataset_parameter,
      is_datafile_parameter,
      description,
      verified,
      seq_number,
      mod_time,
      mod_id,
      create_id,
      create_time,
      FACILITY_ACQUIRED,
      deleted)
    VALUES(
      source.name,
      source.units,
      source.units_long_version,
      source.searchable,
      source.numeric_value,
      source.non_numeric_value_format,
      source.is_sample_parameter,
      source.is_dataset_parameter,
      source.is_datafile_parameter,
      source.description,
      'Y',
      1,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N');

  log_pkg.write_log('load_parameter: finished');
EXCEPTION
  WHEN external_table_ex THEN
    ROLLBACK TO load_parameter_sp;
    log_and_raise_ext_tab_error(SQLERRM, 'PARAMETER');
  WHEN OTHERS THEN
    ROLLBACK TO load_parameter_sp;
    log_pkg.write_exception(
      'Could not populate PARAMETER: '||SQLERRM);
    RAISE;
END load_parameter;

--------------------------------------------------------------------------------

PROCEDURE load_study_status(p_mod_id IN study_status.mod_id%TYPE) IS
BEGIN
  SAVEPOINT load_study_status_sp;

  log_pkg.write_log('load_study_status: '||p_mod_id);

  MERGE INTO study_status target
  USING (
    SELECT name, description
    FROM(
      SELECT
        Lower(name) AS name, -- lowercase lookup name!
        description AS description,
        Row_Number() over(PARTITION BY Lower(name) ORDER BY 1) rn
      FROM extern_study_status
      WHERE Upper(dls) = 'Y'
      )
    WHERE rn = 1) source
  ON(target.name = source.name)
  WHEN matched THEN
    UPDATE SET
      description = source.description,
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = LV_CREATE_ID,
      create_time = nvl(mod_time, systimestamp),
      FACILITY_ACQUIRED = 'Y',
      deleted = 'N'
    WHERE (target.description != source.description
           OR (target.description IS NULL AND source.description IS NOT NULL)
           OR (target.description IS NOT NULL AND source.description IS NULL)
          )
    OR target.create_id != LV_CREATE_ID
    OR target.create_time is null
    OR target.FACILITY_ACQUIRED is null
    OR target.deleted is null
  WHEN NOT matched THEN
    INSERT(
      name,
      description,
      mod_time,
      mod_id,
      create_id,
      create_time,
      FACILITY_ACQUIRED,
      deleted)
    VALUES(
      source.name,
      source.description,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N');

  log_pkg.write_log('load_study_status: finished');
EXCEPTION
  WHEN external_table_ex THEN
    ROLLBACK TO load_study_status_sp;
    log_and_raise_ext_tab_error(SQLERRM, 'STUDY_STATUS');
  WHEN OTHERS THEN
    ROLLBACK TO load_study_status_sp;
    log_pkg.write_exception(
      'Could not populate STUDY_STATUS: '||SQLERRM);
    RAISE;
END load_study_status;

--------------------------------------------------------------------------------

-- loads all data
PROCEDURE load_all(p_mod_id IN datafile_format.mod_id%TYPE) IS
BEGIN
  SAVEPOINT load_all_sp;

  log_pkg.write_log('Loading all flat-file data');
  log_pkg.write_log('Modification: '||p_mod_id,1);

  load_datafile_format(p_mod_id);
  load_dataset_status(p_mod_id);
  load_dataset_type(p_mod_id);
  load_facility_cycle(p_mod_id);
  load_instrument(p_mod_id);
  load_investigation_type(p_mod_id);
  load_parameter(p_mod_id);
  load_study_status(p_mod_id);
  load_icat_role(p_mod_id);
  load_this_icat(p_mod_id);
  --load_fac_inst_scientist(p_mod_id);

  log_pkg.write_log('Loading all flat-file data: finished successfully');
EXCEPTION
  WHEN null_value_ex THEN
    ROLLBACK TO load_all_sp;
    log_pkg.write_log('Loading all flat-file data: UNSUCCESSFUL');
    log_pkg.write_exception('Empty field(s) in datafile prevented upload:');
    log_pkg.write_exception(SQLERRM,1);
    RAISE;
  WHEN OTHERS THEN
    ROLLBACK TO load_all_sp;
    log_pkg.write_log('Loading all flat-file data: UNSUCCESSFUL');
    log_pkg.write_exception(SQLERRM);
    RAISE;
END load_all;

--------------------------------------------------------------------------------
PROCEDURE load_icat_role(p_mod_id IN icat_role.mod_id%TYPE) IS
BEGIN
  SAVEPOINT load_icat_role_sp;

  log_pkg.write_log('load_icat_role: '||p_mod_id);

  MERGE INTO icat_role target
  USING (
      SELECT ROLE,                           
    ACTION_INSERT,                  
    ACTION_INSERT_WEIGHT,          
    ACTION_SELECT,                  
    ACTION_SELECT_WEIGHT,           
    ACTION_DOWNLOAD,                
    ACTION_DOWNLOAD_WEIGHT,         
    ACTION_UPDATE,                  
    ACTION_UPDATE_WEIGHT,           
    ACTION_DELETE,                  
    ACTION_DELETE_WEIGHT,           
    ACTION_REMOVE,                 
    ACTION_REMOVE_WEIGHT,           
    ACTION_ROOT_INSERT,             
    ACTION_ROOT_INSERT_WEIGHT,      
    ACTION_ROOT_REMOVE,             
    ACTION_ROOT_REMOVE_WEIGHT,      
    ACTION_SET_FA,                  
    ACTION_SET_FA_WEIGHT,           
    ACTION_MANAGE_USERS,            
    ACTION_MANAGE_USERS_WEIGHT
    from (
    SELECT upper(ROLE)  as role,                           
    ACTION_INSERT  as ACTION_INSERT,                  
    ACTION_INSERT_WEIGHT as ACTION_INSERT_WEIGHT,          
    ACTION_SELECT as ACTION_SELECT,                  
    ACTION_SELECT_WEIGHT as ACTION_SELECT_WEIGHT,           
    ACTION_DOWNLOAD as ACTION_DOWNLOAD,                
    ACTION_DOWNLOAD_WEIGHT as ACTION_DOWNLOAD_WEIGHT,         
    ACTION_UPDATE as ACTION_UPDATE,                  
    ACTION_UPDATE_WEIGHT as ACTION_UPDATE_WEIGHT,           
    ACTION_DELETE as ACTION_DELETE,                  
    ACTION_DELETE_WEIGHT as ACTION_DELETE_WEIGHT,           
    ACTION_REMOVE as ACTION_REMOVE,                 
    ACTION_REMOVE_WEIGHT as ACTION_REMOVE_WEIGHT,           
    ACTION_ROOT_INSERT as ACTION_ROOT_INSERT,             
    ACTION_ROOT_INSERT_WEIGHT as ACTION_ROOT_INSERT_WEIGHT,      
    ACTION_ROOT_REMOVE as ACTION_ROOT_REMOVE,             
    ACTION_ROOT_REMOVE_WEIGHT as ACTION_ROOT_REMOVE_WEIGHT,      
    ACTION_SET_FA as ACTION_SET_FA,                  
    ACTION_SET_FA_WEIGHT as ACTION_SET_FA_WEIGHT,           
    ACTION_MANAGE_USERS as ACTION_MANAGE_USERS,            
    ACTION_MANAGE_USERS_WEIGHT as ACTION_MANAGE_USERS_WEIGHT,         
        Row_Number() over(PARTITION BY lower(role) ORDER BY 1) rn
      FROM extern_icat_role
      WHERE Upper(dls) = 'Y')
      where  rn = 1) source
  ON(target.role = source.role)
/*  WHEN matched THEN
    UPDATE SET
       target.action_insert = source.action_insert,
       target.action_select = source.action_select,
       target.action_download = source.action_download,
       target.action_update = source.action_update,
       target.action_delete = source.action_delete,
       target.action_root_remove = source.action_root_remove,
       target.action_set_fa = source.action_set_fa, 
       target.create_id = LV_CREATE_ID,
       target.create_time = nvl(mod_time, systimestamp),
       target.FACILITY_ACQUIRED = 'Y',
       target.deleted = 'N'
    WHERE  target.action_insert != source.action_insert
    OR target.action_select != source.action_select
    OR target.action_download != source.action_download
    OR target.action_update != source.action_update
    OR target.action_delete != source.action_delete
    OR target.action_root_remove != source.action_root_remove
    OR target.action_set_fa != source.action_set_fa 
    OR target.create_id != LV_CREATE_ID
    OR target.create_time is null
    OR target.FACILITY_ACQUIRED is null
    OR target.deleted is null  */
  WHEN NOT matched THEN
    INSERT(
ROLE,
 ROLE_WEIGHT,
 ACTION_INSERT,
 ACTION_INSERT_WEIGHT,
 ACTION_SELECT,
 ACTION_SELECT_WEIGHT,
 ACTION_DOWNLOAD,
 ACTION_DOWNLOAD_WEIGHT,
 ACTION_UPDATE,
 ACTION_UPDATE_WEIGHT,
 ACTION_DELETE,
 ACTION_DELETE_WEIGHT,
 ACTION_REMOVE,
 ACTION_REMOVE_WEIGHT,
 ACTION_ROOT_INSERT,
 ACTION_ROOT_INSERT_WEIGHT,
 ACTION_ROOT_REMOVE,
 ACTION_ROOT_REMOVE_WEIGHT,
 ACTION_SET_FA,
 ACTION_SET_FA_WEIGHT,
 ACTION_MANAGE_USERS,
 ACTION_MANAGE_USERS_WEIGHT,
 MOD_TIME,
 MOD_ID,
 CREATE_TIME,
 CREATE_ID,
 FACILITY_ACQUIRED,
 DELETED)
    VALUES(
 source.ROLE,
 1,
 source.ACTION_INSERT,
 source.ACTION_INSERT_WEIGHT,
 source.ACTION_SELECT,
 source.ACTION_SELECT_WEIGHT,
 source.ACTION_DOWNLOAD,
 source.ACTION_DOWNLOAD_WEIGHT,
 source.ACTION_UPDATE,
 source.ACTION_UPDATE_WEIGHT,
 source.ACTION_DELETE,
 source.ACTION_DELETE_WEIGHT,
 source.ACTION_REMOVE,
 source.ACTION_REMOVE_WEIGHT,
 source.ACTION_ROOT_INSERT,
 source.ACTION_ROOT_INSERT_WEIGHT,
 source.ACTION_ROOT_REMOVE,
 source.ACTION_ROOT_REMOVE_WEIGHT,
 source.ACTION_SET_FA,
 source.ACTION_SET_FA_WEIGHT,
 source.ACTION_MANAGE_USERS,
 source.ACTION_MANAGE_USERS_WEIGHT,
       systimestamp,
      p_mod_id,
      systimestamp,
      LV_CREATE_ID,
      'Y',
      'N');
      


      
           

  log_pkg.write_log('load_icat_role: finished');
EXCEPTION
  WHEN external_table_ex THEN
    ROLLBACK TO load_icat_role_sp;
    log_and_raise_ext_tab_error(SQLERRM, 'ICAT_ROLE');
  WHEN OTHERS THEN
    ROLLBACK TO load_icat_role_sp;
    log_pkg.write_exception(
      'Could not populate ICAT_ROLE: '||SQLERRM);
    RAISE;
END load_icat_role;

--------------------------------------------------------------------------------

PROCEDURE load_this_icat(p_mod_id IN this_icat.mod_id%TYPE) IS
BEGIN
  SAVEPOINT load_this_icat_sp;

  log_pkg.write_log('load_this_icat: '||p_mod_id);
  

  MERGE INTO this_icat target
  USING (
    SELECT facility_short_name,
           facility_long_name,
           facility_url,
           facility_description
    FROM(
      SELECT
      upper(facility_short_name) AS facility_short_name,               
      facility_long_name AS  facility_long_name,   
      facility_url AS  facility_url,          
      facility_description AS facility_description,        
      Row_Number() over(PARTITION BY lower(facility_short_name) ORDER BY 1) rn
      FROM extern_this_icat
      WHERE Upper(dls) = 'Y'
      )
    WHERE rn = 1) source
  ON(target.facility_short_name = source.facility_short_name)
  WHEN matched THEN
    UPDATE SET
       target.facility_long_name = source.facility_long_name,
       target.facility_url = source.facility_url,
       target.facility_description = source.facility_description,
       target.mod_id = p_mod_id,
       target.mod_time = systimestamp
    WHERE  target.facility_long_name != source.facility_long_name
    OR target.facility_url != source.facility_url
    OR target.facility_description != source.facility_description
    OR target.mod_id != p_mod_id
    WHEN NOT matched THEN
    INSERT(
      facility_short_name,
      facility_long_name,
      facility_url,
      facility_description,
      mod_time,
      mod_id,
      create_time,
      create_id,
      FACILITY_ACQUIRED,
      deleted)
    VALUES(
      source.facility_short_name,
      source.facility_long_name,
      source.facility_url,
      source.facility_description,
      systimestamp,
      p_mod_id,
      systimestamp,
      LV_CREATE_ID,
      'Y',
      'N');
      
           

  log_pkg.write_log('load_this_icat: finished');
EXCEPTION
  WHEN external_table_ex THEN
    ROLLBACK TO load_this_icat_sp;
    log_and_raise_ext_tab_error(SQLERRM, 'THIS_ICAT');
  WHEN OTHERS THEN
    ROLLBACK TO load_this_icat_sp;
    log_pkg.write_exception(
      'Could not populate THIS_ICAT: '||SQLERRM);
    RAISE;
END load_this_icat;


--------------------------------------------------------------------------------

PROCEDURE load_fac_inst_scientist(p_mod_id IN this_icat.mod_id%TYPE) IS
BEGIN
  SAVEPOINT load_fac_inst_scientist_sp;

  log_pkg.write_log('load_fac_inst_scientist: '||p_mod_id);
  

  MERGE INTO FACILITY_INSTRUMENT_SCIENTIST target
  USING (
    SELECT instrument_name,
           federal_id,
           sequence_number
    FROM(
      SELECT
       instrument_name  AS  instrument_name,    
       federal_id   AS  federal_id,             
       sequence_number  AS sequence_number        
      FROM extern_station_scienist
      WHERE Upper(dls) = 'Y'
      )
       ) source
  ON(target.instrument_name = source.instrument_name
     AND target.federal_id = source.federal_id)
  WHEN matched THEN
    UPDATE SET
       target.seq_number = source.sequence_number
    WHERE  target.seq_number != source.sequence_number

    WHEN NOT matched THEN
    INSERT(
           instrument_name,       
           federal_id,             
           seq_number,             
           mod_time,               
           mod_id,                 
           create_time,            
           create_id,              
           facility_acquired,
           deleted)
    VALUES(
      source.instrument_name,
      nvl(source.federal_id,user),
      --source.sequence_number,
       1,
      systimestamp,
      p_mod_id,
      systimestamp,
      LV_CREATE_ID,
      'Y',
      'N');
      
           

  log_pkg.write_log('load_fac_inst_scientist: finished');
EXCEPTION
  WHEN external_table_ex THEN
    ROLLBACK TO load_this_icat_sp;
    log_and_raise_ext_tab_error(SQLERRM, ' FACILITY_INSTRUMENT_SCIENTIST');
  WHEN OTHERS THEN
    ROLLBACK TO load_this_icat_sp;
    log_pkg.write_exception(
      'Could not populate  FACILITY_INSTRUMENT_SCIENTIST: '||SQLERRM);
    RAISE;
END load_fac_inst_scientist;



--------------------------------------------------------------------------------


END load_external_data_pkg;
/