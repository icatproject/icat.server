CREATE OR REPLACE PACKAGE BATCH_SINGLE_MIGRATION_PKG AS

PROCEDURE duodesk_pr(
  visit investigation.visit_id%TYPE, beamline investigation.instrument%TYPE,p_mod_id IN investigation.mod_id%TYPE default 'PROPAGATION');

--FUNCTION get_create_id RETURN investigation.create_id%TYPE;

test_wall EXCEPTION;


PROCEDURE set_record_ignored(
  p_inv_number IN VARCHAR2,
  p_instrument IN VARCHAR2,
  p_visit_id IN VARCHAR2,
  p_facility_cycle IN VARCHAR2);
  
FUNCTION write_proposal(
  p_inv_number IN VARCHAR2,
  p_instrument IN VARCHAR2,
  p_visit_id IN VARCHAR2,
  p_facility_cycle IN VARCHAR2)
  RETURN VARCHAR2;

END batch_single_migration_pkg;
/

CREATE OR REPLACE PACKAGE BODY BATCH_SINGLE_MIGRATION_PKG AS

--------------------------------------------------------------------------------

child_record_found_ex EXCEPTION;
PRAGMA EXCEPTION_INIT(child_record_found_ex, -2292);

bulk_errors EXCEPTION;
PRAGMA EXCEPTION_INIT(bulk_errors, -24381);

--------------------------------------------------------------------------------

g_bulk_warnings BOOLEAN := FALSE;

-- these hold lists of investigations which cannot be migrated in full.  when an
-- investigation cannot be migrated in full it is not migrated at all.
g_inv_unique_fields tt_inv_unique_fields;
g_ignore_by_id vc_array;

LV_CREATE_ID CONSTANT investigation.create_id%TYPE := 'FROM PROPAGATION';
LV_REMOVED_CREATE_ID CONSTANT investigation.create_id%TYPE
  := 'FROM PROPAGATION, SUBSEQUENTLY REMOVED';


--------------------------------------------------------------------------------

FUNCTION get_create_id RETURN investigation.create_id%TYPE IS
BEGIN
  RETURN LV_CREATE_ID;
END get_create_id;

--------------------------------------------------------------------------------

FUNCTION write_proposal(
  p_inv_number IN VARCHAR2,
  p_instrument IN VARCHAR2,
  p_visit_id IN VARCHAR2,
  p_facility_cycle IN VARCHAR2)
  RETURN VARCHAR2 IS
BEGIN
  RETURN
    'Proposal: '||p_inv_number||' | '||
    'Instrument: '||Nvl(p_instrument,'Unspecified')||' | '||
    'Visit: '||Nvl(p_visit_id,'Unspecified')||' | '||
    'Facility Cycle: '||Nvl(p_facility_cycle,'Unspecified');
END write_proposal;

--------------------------------------------------------------------------------

PROCEDURE set_record_ignored(
  p_inv_number IN VARCHAR2,
  p_instrument IN VARCHAR2,
  p_visit_id IN VARCHAR2,
  p_facility_cycle IN VARCHAR2) IS
BEGIN
  g_inv_unique_fields.extend;
  g_inv_unique_fields(g_inv_unique_fields.Count) :=
    tr_inv_unique_fields(
      p_inv_number, p_instrument, p_visit_id, p_facility_cycle);
END set_record_ignored;

--------------------------------------------------------------------------------
PROCEDURE migrate_instruments(
  p_mod_id IN investigation.mod_id%TYPE) IS
BEGIN
  log_pkg.write_log('migrating instrument data');

  log_pkg.write_log('upload instrument');

  -- load in instruments which exist in Duodesk but have not been entered in the
  -- official spreadsheet of lookup data.

  -- instrument records which are in the source spreadsheet (the
  -- authoritative source of lookup data) should not be modified by
  -- this process.  they are identified by
  -- "create_id = load_external_data_pkg.get_create_id".
  -- other records may be inserted/updated here.
  MERGE INTO instrument target
  USING(
    SELECT
      Lower(instr_nom)             AS name, -- lowercase lookup name!
      instr_lib                    AS type,
      instr_lib                    AS description,
      Nvl(Upper(instr_efface),'N') AS deleted
    FROM instrument@duodesk i1
    WHERE NOT EXISTS(
      SELECT NULL
      FROM instrument i2
      WHERE lower(i2.name) = Lower(i1.instr_nom)
      )
  ) source
  ON (source.name = target.name)
  WHEN matched THEN
    UPDATE SET
      type = source.type,
      description = source.description,
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = nvl(create_id,LV_CREATE_ID),
      create_time = nvl(create_time,systimestamp),
      facility_acquired = 'Y',
      deleted =nvl(source.deleted ,'N')
      WHERE (target.type != source.type)
    OR (target.type IS NULL AND source.type IS NOT NULL)
    OR (target.type IS NOT NULL AND source.type IS NULL)
    OR (target.description != source.description)
    OR (target.description IS NULL AND source.description IS NOT NULL)
    OR (target.description IS NOT NULL AND source.description IS NULL)
    OR target.description != source.description
    OR target.create_id != LV_CREATE_ID
    OR target.create_time is null
    OR target.facility_acquired is null
    OR target.deleted is null
  WHEN NOT matched THEN
    INSERT(
      name,
      SHORT_NAME,
      type,
      description,
      mod_time,
      mod_id,
      create_time,
      create_id,
      facility_acquired,
      deleted)
    VALUES(
      lower(source.name),
      lower(source.name),
      source.type,
      source.description,
      systimestamp,
      p_mod_id,
      systimestamp,
      LV_CREATE_ID,
      'Y',
      nvl(source.deleted,'N'));

  log_pkg.write_log(
    'instrument migration successful: '||SQL%ROWCOUNT||' rows inserted');
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception('instrument migration failed');
    RAISE;
END migrate_instruments;

--------------------------------------------------------------------------------

PROCEDURE migrate_parameters(
  p_mod_id IN investigation.mod_id%TYPE) IS
  cursor column_list is select * from user_tab_cols@duodesk
  where table_name = 'SAMPLESHEET'
  and column_name not in ('SMPS_EFFACE','UCR','DCR','DDM');
BEGIN
  log_pkg.write_log('migrating parameter data');

  -- parameters which exist in Duodesk and not in the spreadsheet of lookup data
  -- are inserted.
  -- parameters which exist in the spreadsheet and have a use in Duodesk which
  -- is different from that in the spreadsheet (eg if a parameter is used for
  -- Samples in duodesk but listed as a Dataset parameter in the spreadsheet or
  -- is listed as a numeric parameter in the spreadsheet but is used for
  -- character data in duodesk) will have the relevent is_xxx_parameter or
  -- numeric_value column updated.


  -- sample parameters
  MERGE INTO parameter target
  USING(
    SELECT name, units, units_long_version, searchable, numeric_value,
           non_numeric_value_format, is_sample_parameter, is_dataset_parameter,
           is_datafile_parameter, description
    FROM(
      SELECT
        Lower(name) AS name, -- lowercase lookup name!
        Nvl(units,'N/A') AS units,
        units AS units_long_version,
        'Y' AS searchable,
        'Y' AS numeric_value,
        NULL AS non_numeric_value_format,
        'Y' AS is_sample_parameter,
        'N' AS is_dataset_parameter,
        'N' AS is_datafile_parameter,
        comments AS description,
        Row_Number() over(PARTITION BY Lower(name), units
                          ORDER BY 1) AS rn
      FROM sample_parameter@duodesk p1
      WHERE name IS NOT NULL)
    WHERE rn = 1
    ) source
  ON (target.name = source.name
      AND target.units = source.units
      )
  WHEN matched THEN
  -- don't update the UNITS_LONG_VERSION, NUMERIC_VALUE, SEARCHABLE
  -- or NON_NUMERIC_VALUE_FORMAT columns: these are not specified in the
  -- duo_sample_parameter table and they may have been set to some value other
  -- than the default depending on their usage within the system
    UPDATE SET
      is_sample_parameter = 'Y',
      mod_id = p_mod_id,
      mod_time = systimestamp,
      facility_acquired = 'Y',
      deleted = 'N'
    WHERE target.is_sample_parameter != 'Y'
      OR target.create_id != LV_CREATE_ID
      OR target.create_time is null
      OR target.facility_acquired is null
      OR target.deleted is null
  WHEN NOT matched THEN
    INSERT(
      name,
      units,
      units_long_version,
      is_sample_parameter,
      is_dataset_parameter,
      is_datafile_parameter,
      searchable,
      numeric_value,
      mod_id,
      mod_time,
      create_id,
      create_time,
      facility_acquired,
      deleted,
      VERIFIED,
      description)
    VALUES(
      source.name,
      source.units,
      source.units_long_version,
      source.is_sample_parameter,
      source.is_dataset_parameter,
      source.is_datafile_parameter,
      source.searchable,
      source.numeric_value,
      p_mod_id,
      systimestamp,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N',
      'Y',
      source.description);
      
log_pkg.write_log('migrating parameter data first merge gone');
    --Now go through all the columns in sampleworksheet@duodesk and entre these
    --as entries into the parameter table.  Have to make sure that they are numeric
    --and update the numeric_value column if so
    for i in column_list loop
    execute immediate('insert into temp_samplesheet (select ''' || substr(i.column_name,instr(i.column_name,'_')+1) || ''',sum(is_number(' || i.column_name || ')) from samplesheet@duodesk)');
    end loop;

log_pkg.write_log('migrating parameter data first loop gone');


  MERGE INTO parameter target
  USING(
    SELECT * from temp_samplesheet
    ) source
  ON (lower(target.name) = lower(source.name))
  WHEN matched THEN
    UPDATE SET
      numeric_value=decode(source.value,0,'Y','N'),
      mod_id = p_mod_id,
      mod_time = systimestamp
    WHEN NOT matched THEN
    INSERT(
      name,
      units,
      units_long_version,
      is_sample_parameter,
      is_dataset_parameter,
      is_datafile_parameter,
      searchable,
      numeric_value,
      mod_id,
      mod_time,
      create_id,
      create_time,
      facility_acquired,
      deleted,
      VERIFIED)
    VALUES(
      lower(source.name),
      decode(source.value,0,'number','text'),
      decode(source.value,0,'number','text'),
      'Y',
      'N',
      'N',
      'Y',
      decode(source.value,0,'Y','N'),
      p_mod_id,
      systimestamp,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N',
      'Y');

log_pkg.write_log('migrating parameter data second merge gone');


    delete from temp_samplesheet;


  -- set the numeric_value field to 'N' if non-numeric data exists.  this field
  -- is set to 'Y' when records are inserted above.
  UPDATE parameter SET
    numeric_value = 'N',
    mod_id = p_mod_id,
    mod_time = systimestamp,
    facility_acquired = 'Y'
  WHERE name IN(
    SELECT Lower(name)
    FROM sample_parameter@duodesk
    WHERE NOT(regexp_like(value,'^-{0,1}\d*\.{0,1}\d+$'))
    OR value IS NULL
  )
  AND numeric_value = 'Y';

  log_pkg.write_log('parameter migration successful');
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception('parameter migration failed');
    RAISE;
END migrate_parameters;


--------------------------------------------------------------------------------

PROCEDURE migrate_facility_users(
  p_mod_id IN investigation.mod_id%TYPE) IS

  -- data to be loaded into the facility_user table.
  -- there is no direct link from a single investigation to a single user
  -- so there's no need to restrict the query to investigations which loaded
  -- successfully, and can't exclude investigations based on an unsuccessful
  -- load.  will log failed records instead.
  CURSOR c_facility_user_data IS
         SELECT facility_user_id, federal_id, title, initials, first_name,
                middle_name, last_name
           FROM (SELECT TO_CHAR (usernumber) AS facility_user_id,
                        fedid AS federal_id, title AS title,
                        initials AS initials, knownas AS first_name,
                        NULL AS middle_name, familyname AS last_name
                   FROM tblpeople@duodesk where thrudate is null);



  TYPE t_facility_user_id IS TABLE OF facility_user.facility_user_id%TYPE;
  TYPE t_federal_id IS TABLE OF facility_user.federal_id%TYPE;
  TYPE t_title IS TABLE OF facility_user.title%TYPE;
  TYPE t_initials IS TABLE OF facility_user.initials%TYPE;
  TYPE t_first_name IS TABLE OF facility_user.first_name%TYPE;
  TYPE t_middle_name IS TABLE OF facility_user.middle_name%TYPE;
  TYPE t_last_name IS TABLE OF facility_user.last_name%TYPE;

  l_facility_user_id t_facility_user_id;
  l_federal_id t_federal_id;
  l_title t_title;
  l_initials t_initials;
  l_first_name t_first_name;
  l_middle_name t_middle_name;
  l_last_name t_last_name;
BEGIN
  log_pkg.write_log('migrating facility_user data');

--   execute immediate 'ALTER TRIGGER air_facility_user_trg DISABLE';

  OPEN c_facility_user_data;
  FETCH c_facility_user_data BULK COLLECT INTO
    l_facility_user_id,
    l_federal_id,
    l_title,
    l_initials,
    l_first_name,
    l_middle_name,
    l_last_name;
  CLOSE c_facility_user_data;


  FORALL indx IN l_title.FIRST..l_title.LAST
  SAVE EXCEPTIONS
  MERGE INTO facility_user target
  USING(
    SELECT
      l_facility_user_id(indx)  AS facility_user_id,
      l_federal_id(indx)        AS federal_id,
/* testing error trapping:
case when l_facility_user_id(indx) BETWEEN '20320' AND '20330' THEN
  RPad(l_title(indx),260,'x') else l_title(indx) end as title,
--*/
      l_title(indx)             AS title,
      l_initials(indx)          AS initials,
      l_first_name(indx)        AS first_name,
      l_middle_name(indx)       AS middle_name,
      l_last_name(indx)         AS last_name
    FROM dual
    ) source
  ON(target.facility_user_id = source.facility_user_id)
  WHEN MATCHED THEN
    UPDATE SET
      federal_id = source.federal_id,
      title = source.title,
      initials = source.initials,
      first_name = source.first_name,
      middle_name = source.middle_name,
      last_name = source.last_name,
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = LV_CREATE_ID,
      create_time = nvl(mod_time,systimestamp),
      facility_acquired = 'Y',
      deleted = 'N'
    WHERE (target.title != source.title
            OR (target.title IS NULL AND source.title IS NOT NULL)
            OR (target.title IS NOT NULL AND source.title IS NULL)
          )
    OR (target.federal_id != source.federal_id
          OR (target.federal_id IS NULL AND source.federal_id IS NOT NULL)
          OR (target.federal_id IS NOT NULL AND source.federal_id IS NULL)
        )
    OR (target.initials != source.initials
          OR (target.initials IS NULL AND source.initials IS NOT NULL)
          OR (target.initials IS NOT NULL AND source.initials IS NULL)
        )
    OR (target.first_name != source.first_name
          OR (target.first_name IS NULL AND source.first_name IS NOT NULL)
          OR (target.first_name IS NOT NULL AND source.first_name IS NULL)
        )
    OR (target.middle_name != source.middle_name
          OR (target.middle_name IS NULL AND source.middle_name IS NOT NULL)
          OR (target.middle_name IS NOT NULL AND source.middle_name IS NULL)
        )
    OR (target.last_name != source.last_name
          OR (target.last_name IS NULL AND source.last_name IS NOT NULL)
          OR (target.last_name IS NOT NULL AND source.last_name IS NULL)
        )
      OR target.create_id != LV_CREATE_ID
      OR target.create_time is null
      OR target.facility_acquired is null
      OR target.deleted is null
  WHEN NOT MATCHED THEN
    INSERT(
      facility_user_id,
      federal_id,
      title,
      initials,
      first_name,
      middle_name,
      last_name,
      mod_time,
      mod_id,
      create_id,
      create_time,
      facility_acquired,
      deleted)
    VALUES(
      source.facility_user_id,
      source.federal_id,
      source.title,
      source.initials,
      source.first_name,
      source.middle_name,
      source.last_name,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N');
   --execute immediate 'ALTER TRIGGER air_facility_user_trg enable';
  log_pkg.write_log('facility_user migration successful');
EXCEPTION
  WHEN bulk_errors THEN
    DECLARE
      ln_errors PLS_INTEGER;
      ln_error_index PLS_INTEGER;
    BEGIN
   --execute immediate 'ALTER TRIGGER air_facility_user_trg enable';
      g_bulk_warnings := TRUE;
      ln_errors := SQL%bulk_exceptions.COUNT;
      log_pkg.write_exception('facility_user migration failed');
      log_pkg.write_exception('number of records that failed: ' || ln_errors,1);

      -- log details of the record and the error
      FOR i IN 1..ln_errors
      LOOP
        ln_error_index := SQL%bulk_exceptions(i).error_index;

        log_pkg.write_exception(
          'Facility User Id: '||l_facility_user_id(i)||' | '||
          'Name: '||l_first_name(i)||' '||l_last_name(i),2);

        log_pkg.write_exception(SQLERRM(-SQL%bulk_exceptions(i).error_code),3);
      END LOOP;
    END;
  WHEN OTHERS THEN
    log_pkg.write_exception('facility_user migration failed');
    RAISE;
END migrate_facility_users;

--------------------------------------------------------------------------------
-------------------------------------------------------------------------------

PROCEDURE migrate_sample_parameters(
  p_mod_id IN investigation.mod_id%TYPE) IS

  -- data to be loaded into the sample_parameter table
  CURSOR c_sample_parameter_data IS
    SELECT
      i.inv_number               AS inv_number,
      i.instrument               AS instrument,
      i.visit_id                 AS visit_id,
      i.facility_cycle           AS facility_cycle,
      s.id                       AS sample_id,
      Lower(dsp.name)            AS name, -- lowercase lookup name!
      Nvl(dsp.units,'N/A')       AS units,
      CASE param.numeric_value
        WHEN 'Y' THEN NULL
          ELSE dsp.Value
          END                    AS string_value,
      CASE param.numeric_value
        WHEN 'Y' THEN To_Number(dsp.Value)
        ELSE To_Number(NULL)
        END                      AS numeric_value,
      dsp.error                  AS error,
      dsp.range_top              AS range_top,
      dsp.range_bottom           AS range_bottom
    FROM sample_parameter@duodesk dsp,
         parameter param,
         investigation i,
         sample s
    WHERE s.investigation_id = i.id
    AND dsp.sample_id = s.proposal_sample_id
    AND dsp.name IS NOT NULL
    AND dsp.value IS NOT NULL
    AND param.name = Lower(dsp.name)
    AND param.units = Nvl(dsp.units,'N/A')
    AND NOT EXISTS(
      SELECT NULL
      FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
      WHERE inv_number = i.inv_number
      AND instrument = i.instrument
      AND (lower(visit_id) = lower(i.visit_id) OR (visit_id IS NULL AND i.visit_id IS NULL))
      AND (facility_cycle = i.facility_cycle
        OR (facility_cycle IS NULL AND i.facility_cycle IS NULL))
      )
    ;

  lva_safety_info vc_array := vc_array(
    'storage',
    'hazard details',
    'sensitive to air',
    'sensitive to vapour',
    'other hazards',
    'other sample prep hazards',
    'other equipment hazards',
    'prep lab required',
    'other special equipment requirements',
    'disposal method');


  -- more sample parameters, for safety information.
  -- there's some hard-coding here but all these parameters should be included
  -- in the spreadsheet from where lookup data are originally loaded (which are
  -- also hard-coded!)
  CURSOR c_safety_information_data IS
    SELECT
      i.inv_number       AS inv_number,
      i.instrument       AS instrument,
      i.visit_id         AS visit_id,
      i.facility_cycle   AS facility_cycle,
      s.id               AS sample_id,
      x.column_value     AS name,
      'N/A'              AS units,
      CASE x.column_value
        WHEN 'storage' THEN ds.storage_reqs
        WHEN 'hazard details' THEN ds.sfty_hazard_details
        WHEN 'sensitive to air' THEN ds.sfty_sensitive_to_air
        WHEN 'sensitive to vapour' THEN ds.sfty_sensitive_to_vapour
        WHEN 'other hazards' THEN ds.sfty_oth_exp_hazards
        WHEN 'other sample prep hazards' THEN ds.sfty_oth_sample_prep_hazards
        WHEN 'other equipment hazards' THEN ds.sfty_oth_equip_hazards
        WHEN 'prep lab required' THEN ds.sfty_prep_lab_required
        WHEN 'other special equipment requirements' THEN ds.sfty_oth_special_equip_reqs
        WHEN 'disposal method' THEN ds.sfty_disposal_method
        END              AS string_value,
      NULL               AS numeric_value,
      NULL               AS error,
      NULL               AS range_top,
      NULL               AS range_bottom
    FROM sample s, sample@duodesk ds, investigation i,
         TABLE(lva_safety_info) x
    WHERE ds.id = s.proposal_sample_id
    AND i.id = s.investigation_id
    AND NOT EXISTS(
      SELECT NULL
      FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
      WHERE inv_number = i.inv_number
      AND instrument = i.instrument
      AND (lower(visit_id) = lower(i.visit_id) OR (visit_id IS NULL AND i.visit_id IS NULL))
      AND (facility_cycle = i.facility_cycle
        OR (facility_cycle IS NULL AND i.facility_cycle IS NULL))
      )
    ;

  TYPE t_inv_number IS TABLE OF investigation.inv_number%TYPE;
  TYPE t_instrument IS TABLE OF investigation.instrument%TYPE;
  TYPE t_visit_id IS TABLE OF investigation.visit_id%TYPE;
  TYPE t_facility_cycle IS TABLE OF investigation.facility_cycle%TYPE;
  TYPE t_sample_id IS TABLE OF sample_parameter.sample_id%TYPE;
  TYPE t_name IS TABLE OF sample_parameter.name%TYPE;
  TYPE t_units IS TABLE OF sample_parameter.units%TYPE;
  TYPE t_string_value IS TABLE OF sample_parameter.string_value%TYPE;
  TYPE t_numeric_value IS TABLE OF sample_parameter.numeric_value%TYPE;
  TYPE t_error IS TABLE OF sample_parameter.error%TYPE;
  TYPE t_range_top IS TABLE OF sample_parameter.range_top%TYPE;
  TYPE t_range_bottom IS TABLE OF sample_parameter.range_bottom%TYPE;

  l_inv_number t_inv_number;
  l_instrument t_instrument;
  l_visit_id t_visit_id;
  l_facility_cycle t_facility_cycle;
  l_sample_id t_sample_id;
  l_name t_name;
  l_units t_units;
  l_string_value t_string_value;
  l_numeric_value t_numeric_value;
  l_error t_error;
  l_range_top t_range_top;
  l_range_bottom t_range_bottom;

  l_success BOOLEAN := TRUE;


  --This is the cursor that will update sample_parametr with a entries from the columns of samplesheet
  --in duodesk.
   CURSOR c1 IS
                  SELECT DISTINCT thissam.id "SAM_ID" ,sam.id "DUO_SAM_ID",
                   samsh.smps_no "SMPS"
            FROM   samplesheet@duodesk samsh,
                   proposal@duodesk prop,
                     duo_proposal@duodesk dprop,
                     requested_instrument@duodesk req,
                     sample@duodesk sam,
                   sample thissam
              WHERE  samsh.smps_propos_no = prop.propos_no
              AND    dprop.desk_propos_no = prop.propos_no
              AND    req.proposal_id = dprop.duo_propos_no
              AND    req.id = sam.requested_instrument_id
            And    nvl(samsh.smps_efface,'N') <> 'Y'
            and    thissam.proposal_sample_id = sam.id;

  PROCEDURE merge_data IS
  BEGIN
  /* testing error trapping.  cause a constraint violation
  Dbms_Output.put_line('testing bulk error collection: sample parameters');
  FOR i IN 1..20 LOOP
    IF g_inv_unique_fields.Count = 0 AND Mod(i,3) = 0 THEN
      Dbms_Output.put_line(
        'breaking investigation '||
        'Proposal: '||l_inv_number(i)||' | '||
        'Instrument: '||Nvl(l_instrument(i),'Unspecified')||' | '||
        'Visit: '||Nvl(l_visit_id(i),'Unspecified')||' | '||
        'Facility Cycle: '||Nvl(l_facility_cycle(i),'Unspecified'));

      l_sample_id(i) := l_sample_id(i) * -1;
    END IF;
  END LOOP;
  --*/

    FORALL indx IN l_inv_number.FIRST..l_inv_number.LAST
    SAVE EXCEPTIONS
    MERGE INTO sample_parameter target
    USING(
      SELECT
        l_sample_id(indx)      AS sample_id,
        l_name(indx)           AS name,
        l_units(indx)          AS units,
        l_string_value(indx)   AS string_value,
        l_numeric_value(indx)  AS numeric_value,
        l_error(indx)          AS error,
        l_range_top(indx)      AS range_top,
        l_range_bottom(indx)   AS range_bottom
      FROM dual
      ) source
    ON(target.sample_id = source.sample_id
       AND target.name = source.name
       AND target.units = source.units
     )
    WHEN matched THEN
      UPDATE SET
        string_value = source.string_value,
        numeric_value = source.numeric_value,
        error = source.error,
        range_top = source.range_top,
        range_bottom = source.range_bottom,
        mod_time = systimestamp,
        mod_id = p_mod_id,
        create_id = LV_CREATE_ID,
        create_time = nvl(mod_time,systimestamp),
        facility_acquired = 'Y',
        deleted = 'N'
      WHERE (target.target.string_value != source.string_value
            OR (target.string_value IS NULL AND source.string_value IS NOT NULL)
            OR (target.string_value IS NOT NULL AND source.string_value IS NULL)
            )
      OR (target.numeric_value != source.numeric_value
          OR (target.numeric_value IS NULL AND source.numeric_value IS NOT NULL)
          OR (target.numeric_value IS NOT NULL AND source.numeric_value IS NULL)
          )
      OR (target.error != source.error
          OR (target.error IS NULL AND source.error IS NOT NULL)
          OR (target.error IS NOT NULL AND source.error IS NULL)
          )
      OR (target.range_top != source.range_top
          OR (target.range_top IS NULL AND source.range_top IS NOT NULL)
          OR (target.range_top IS NOT NULL AND source.range_top IS NULL)
          )
      OR (target.range_bottom != source.range_bottom
          OR (target.range_bottom IS NULL AND source.range_bottom IS NOT NULL)
          OR (target.range_bottom IS NOT NULL AND source.range_bottom IS NULL)
          )
      OR target.create_id != LV_CREATE_ID
      OR target.create_time is null
      OR target.facility_acquired is null
      OR target.deleted is null
    WHEN NOT matched THEN
      INSERT(
        sample_id,
        name,
        units,
        string_value,
        numeric_value,
        error,
        range_top,
        range_bottom,
        mod_time,
        mod_id,
        create_id,
        create_time,
        facility_acquired,
        deleted)
    VALUES(
      source.sample_id,
      source.name,
      source.units,
      source.string_value,
      source.numeric_value,
      source.error,
      source.range_top,
      source.range_bottom,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N');
  EXCEPTION
    WHEN bulk_errors THEN
      DECLARE
        ln_errors PLS_INTEGER;
        ln_error_index PLS_INTEGER;
      BEGIN
        g_bulk_warnings := TRUE;

        ln_errors := SQL%bulk_exceptions.COUNT;
        log_pkg.write_exception('sample parameter migration failed');
        log_pkg.write_exception('number of records that failed: ' || ln_errors,1);

        -- log details of the record and the error
        FOR i IN 1..ln_errors
        LOOP
          ln_error_index := SQL%bulk_exceptions(i).error_index;
          set_record_ignored(
              l_inv_number(ln_error_index),
              l_instrument(ln_error_index),
              l_visit_id(ln_error_index),
              l_facility_cycle(ln_error_index));

          log_pkg.write_exception(
            write_proposal(
              l_inv_number(ln_error_index),
              l_instrument(ln_error_index),
              l_visit_id(ln_error_index),
              l_facility_cycle(ln_error_index)),
            2);

          log_pkg.write_exception(
            'Sample Id: '||Nvl(To_Char(l_sample_id(ln_error_index),'Unspecified'),2));

          log_pkg.write_exception('Parameter Name: '||l_name(ln_error_index),2);

          log_pkg.write_exception(SQLERRM(-SQL%bulk_exceptions(i).error_code),3);
        END LOOP;
      END;
  END;
BEGIN
  log_pkg.write_log('migrating sample parameter data');

  -- delete records where the Duodesk recrod has been removed
  DELETE FROM sample_parameter
  WHERE create_id = LV_CREATE_ID -- ie, created by the propagation process.
    -- Ignore safety info parameters, they don't come from duodesk in the
    -- first place...
  AND NOT(units = 'N/A' AND name MEMBER OF lva_safety_info)
  AND (sample_id, name, units) NOT IN(
    SELECT
      s.id,
      Lower(dsp.name),
      Nvl(dsp.units,'N/A')
    FROM sample_parameter@duodesk dsp,
         sample s
    WHERE dsp.sample_id = s.proposal_sample_id
    AND dsp.name IS NOT NULL
    AND dsp.value IS NOT NULL
    )
  AND sample_id IN (
      SELECT s.id
      FROM investigation i2, sample s
      WHERE s.investigation_id = i2.id
      AND NOT EXISTS(
        SELECT NULL
        FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
        WHERE inv_number = i2.inv_number
        AND instrument = i2.instrument
        AND (lower(visit_id) = lower(i2.visit_id) OR (visit_id IS NULL AND i2.visit_id IS NULL))
        AND (facility_cycle = i2.facility_cycle
          OR (facility_cycle IS NULL AND i2.facility_cycle IS NULL))
        )
    );


  log_pkg.write_log('migrating general sample parameters');

  OPEN c_sample_parameter_data;
  FETCH c_sample_parameter_data BULK COLLECT INTO
    l_inv_number,
    l_instrument,
    l_visit_id,
    l_facility_cycle,
    l_sample_id,
    l_name,
    l_units,
    l_string_value,
    l_numeric_value,
    l_error,
    l_range_top,
    l_range_bottom;
  CLOSE c_sample_parameter_data;

  merge_data;



  log_pkg.write_log('migrating safety information sample parameters');

  OPEN c_safety_information_data;
  FETCH c_safety_information_data BULK COLLECT INTO
    l_inv_number,
    l_instrument,
    l_visit_id,
    l_facility_cycle,
    l_sample_id,
    l_name,
    l_units,
    l_string_value,
    l_numeric_value,
    l_error,
    l_range_top,
    l_range_bottom;
  CLOSE c_safety_information_data;

  merge_data;


  log_pkg.write_log('migrating sample parameters from samplesheet');

FOR c1rec IN c1 LOOP

            begin

            merge into sample_parameter target
                   using(
                   select smps_no "SMPS_NO", thissam.id "SAM_ID",pam.units "UNITS",pam.numeric_value "NUMERIC_VAL", pam.name "NAME",
                   case pam.name
                   when 'user_phone' then smps_user_phone
                   when 'anom_scat_1' then smps_anom_scat_1
                   when 'cell_b' then to_char(smps_cell_b)
                   when 'cell_gamma' then to_char(smps_cell_gamma)
                    when 'is_virfactor' then smps_is_virfactor
                    when 'cell_c' then to_char(smps_cell_c)
                    when 'native_ds' then to_char(smps_native_ds)
                    when 'other_holder' then smps_other_holder
                    when 'laser_wavelength' then to_char(smps_laser_wavelength)
                    when 'cooler' then smps_cooler
                    when 'opmode_no' then to_char(smps_opmode_no)
                    when 'opmode_date' then to_char(smps_opmode_date)
                    when 'anom_scat_2' then smps_anom_scat_2
                    when 'sci_justif' then smps_sci_justif
                    when 'no' then to_char(smps_no)
                    when 'propsbm_no' then to_char(smps_propsbm_no)
                    when 'propos_no' then to_char(smps_propos_no)
                    when 'source' then smps_source
                    when 'is_recombinant' then smps_is_recombinant
                    when 'danger_txt' then smps_danger_txt
                    when 'frozen' then smps_frozen
                    when 'capillary' then smps_capillary
                    when 'user_name' then smps_user_name
                    when 'user_email' then smps_user_email
                    when 'anom_scat_3' then smps_anom_scat_3
                    when 'anom_scat_4' then smps_anom_scat_4
                    when 'cell_a' then to_char(smps_cell_a)
                    when 'exprhost_class' then to_char(smps_exprhost_class)
                    when 'laser_class' then smps_laser_class
                    when 'danger_reception_txt' then smps_danger_reception_txt
                    when 'cell_beta' then to_char(smps_cell_beta)
                    when 'order_in_prop' then to_char(smps_order_in_prop)
                    when 'is_powder' then smps_is_powder
                    when 'is_solution' then smps_is_solution
                    when 'source_class' then to_char(smps_source_class)
                    when 'is_virus' then smps_is_virus
                    when 'cryogenic_gas' then smps_cryogenic_gas
                    when 'space_group' then smps_space_group
                    when 'sad_ds' then to_char(smps_sad_ds)
                    when 'comment' then smps_comment
                    when 'is_crystal' then smps_is_crystal
                    when 'concentration' then smps_concentration
                    when 'is_prion' then smps_is_prion
                    when 'ligands' then smps_ligands
                    when 'danger_reception' then smps_danger_reception
                    when 'cell_alpha' then to_char(smps_cell_alpha)
                    when 'mad_ds' then to_char(smps_mad_ds)
                    when 'description' then smps_description
                    when 'is_toxin' then smps_is_toxin
                    when 'is_danger' then smps_is_danger
                    when 'crystal_tray' then smps_crystal_tray
                    when 'ligands_txt' then smps_ligands_txt
                    when 'laser' then smps_laser
                    when 'pressurized_cell' then smps_pressurized_cell
                    when 'propane' then smps_propane
                    when 'removed' then smps_removed
                    when 'stored_esrf' then smps_stored_esrf
                    when 'bag_shift_type' then smps_bag_shift_type
                    when 'acronym' then smps_acronym
                    when 'expr_host' then smps_expr_host
                   end "VALUE"
                   FROM
                   samplesheet@duodesk samsh,
                   sample thissam,
                   parameter pam
                   WHERE
                   smps_no=c1rec.smps
                   and thissam.proposal_sample_id = c1rec.duo_sam_id
                   and pam.name in (SELECT lower(substr(column_name,6))
                               FROM user_tab_cols@duodesk
                               WHERE table_name = 'SAMPLESHEET'
                               AND column_name NOT IN ('SMPS_EFFACE', 'UCR', 'DCR', 'DDM'))
                                                  ) source
                   on(target.sample_id = source.sam_id
                   and target.name=source.name)
                   when matched then
                   update set
                   mod_time = sysdate,
                   mod_id   = LV_CREATE_ID,
                   units    = source.units,
                   numeric_value = decode(source.numeric_val,'Y',source.value,''),
                   string_value  = decode(source.numeric_val,'N',source.value,'')
                   where
                   units <> source.units
                   or (numeric_value= decode(source.numeric_val,'Y',source.value,''))
                   or (string_value  = decode(source.numeric_val,'N',source.value,''))
                   when not matched then
                   insert (
                   sample_id,
                   name,
                   units,
                   create_time,
                   create_id,
                   mod_time,
                   mod_id,
                   facility_acquired,
                   numeric_value,
                   string_value,
                   deleted)
                   values
                   (source.sam_id,
                   source.name,
                   source.units,
                   sysdate,
                   LV_CREATE_ID,
                   sysdate,
                   LV_CREATE_ID,
                   'Y',
                   decode(source.numeric_val,'Y',source.value,''),
                   decode(source.numeric_val,'N',source.value,''),
                   'N');


                 exception when others then
                 log_pkg.write_log('PROBLEM migrating sample parameters from samplesheet');
                   end;
                   end loop;





  IF l_success THEN
    log_pkg.write_log('sample parameter data migration successful');
  END IF;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception('sample parameter data migration failed');
    RAISE;
END migrate_sample_parameters;

--------------------------------------------------------------------------------
PROCEDURE migrate_investigations(
   visit_id IN investigation.visit_id%TYPE, beamline investigation.instrument%TYPE, p_mod_id IN investigation.mod_id%TYPE default 'PROPAGATION') IS

  -- A proposal on the Duodesk database maps to an investigation in the icatdls
  -- schema, however a sequence is used to generate the primary key in the
  -- investigation table

  -- the src_hash column is used to store the primary keys of the 3 Duodesk
  -- tables which make up each ICAT Investigation record.


  -- data to be loaded into the investigation table.
  -- not yet got a value for release_date
  CURSOR c_investigation_data IS
    -- this query could be simplified, but simpler versions caused the remote
    -- data to be retrieved one row at a time in nested loops (this is caused
    -- by using the get_md5 function)
    SELECT * FROM(
      SELECT inv_number, visit_id, facility_cycle, instrument, title, inv_type,
             inv_abstract, prev_inv_number, bcat_inv_str, grant_id, release_date,
             util_pkg.get_md5(propos_no,instr_no,pl_no) src_hash
      FROM (
        SELECT DISTINCT 
--          To_Char(p.propos_no)     AS inv_number,
          UPPER(p.propos_categ_code
            || p.propos_categ_cpt) AS inv_number,
          UPPER(p.propos_categ_code
            || p.propos_categ_cpt
            || '-'
            || pl.pl_visit_no) AS visit_id,
            --Dense_Rank() over(PARTITION BY p.propos_no, Lower(i.instr_nom)
             --               ORDER BY pl.pl_date_deb, pl.pl_no)
              --                     AS visit_id,
          --Lower('DLS')              AS facility_cycle, -- lowercase lookup name!
          Lower(null)              AS facility_cycle, -- lowercase lookup name!
          Lower(i.instr_nom)       AS instrument, -- lowercase lookup name!
          Nvl(SubStr(p.propos_title,1,255), p.propos_no)  AS title,
          Lower('EXPERIMENT')      AS inv_type, -- lowercase lookup name!
          dp.exp_abstract          AS inv_abstract,
          NULL                     AS prev_inv_number,
          NULL                     AS bcat_inv_str,
          NULL                     AS grant_id,
          Add_Months(SYSDATE,24)   AS release_date,
          p.propos_no              AS propos_no,
          i.instr_no               AS instr_no,
          pl.pl_no                 AS pl_no,
          Nvl(p.propos_efface,'N') AS propos_efface,
          Nvl(m.mes_efface,'N')    AS mes_efface,
          Nvl(i.instr_efface,'N')  AS instr_efface,
          Nvl(pl.pl_efface,'N')    AS pl_efface
        FROM proposal@duodesk p,
            duo_proposal@duodesk dp,
            mesure@duodesk m,
            instrument@duodesk i,
            planning@duodesk pl
        WHERE Lower(i.instr_nom)= lower(beamline)
        AND UPPER(p.propos_categ_code|| p.propos_categ_cpt || '-' || pl.pl_visit_no)= UPPER(visit_id)
        AND dp.desk_propos_no = p.propos_no
        AND m.mes_propos_no = p.propos_no
        AND m.mes_instr_no = i.instr_no
        AND mes_uni_all > 0 -- indicates an approved proposal
        AND pl.pl_mes_no = m.mes_no
        AND pl.pl_date_deb IS NOT NULL
        AND pl.pl_date_fin IS NOT NULL
        )
      WHERE propos_efface != 'Y'
      AND mes_efface != 'Y'
      AND instr_efface != 'Y'
      AND pl_efface != 'Y'
    ) i
    WHERE NOT EXISTS(
      SELECT NULL
      FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
      WHERE inv_number = i.inv_number
      AND instrument = i.instrument
      AND (upper(visit_id) = upper(i.visit_id) OR (visit_id IS NULL AND i.visit_id IS NULL))
      AND (facility_cycle = i.facility_cycle
          OR (facility_cycle IS NULL AND i.facility_cycle IS NULL))
      );


  TYPE t_inv_number      IS TABLE OF investigation.inv_number%TYPE;
  TYPE t_visit_id        IS TABLE OF investigation.visit_id%TYPE;
  TYPE t_facility_cycle  IS TABLE OF investigation.facility_cycle%TYPE;
  TYPE t_instrument      IS TABLE OF investigation.instrument%TYPE;
  TYPE t_title           IS TABLE OF investigation.title%TYPE;
  TYPE t_inv_type        IS TABLE OF investigation.inv_type%TYPE;
  TYPE t_inv_abstract    IS TABLE OF investigation.inv_abstract%TYPE;
  TYPE t_prev_inv_number IS TABLE OF investigation.prev_inv_number%TYPE;
  TYPE t_bcat_inv_str    IS TABLE OF investigation.bcat_inv_str%TYPE;
  TYPE t_grant_id        IS TABLE OF investigation.grant_id%TYPE;
  TYPE t_release_date    IS TABLE OF investigation.release_date%TYPE;


  l_inv_number      t_inv_number;
  l_visit_id        t_visit_id;
  l_facility_cycle  t_facility_cycle;
  l_instrument      t_instrument;
  l_title           t_title;
  l_inv_type        t_inv_type;
  l_inv_abstract    t_inv_abstract;
  l_prev_inv_number t_prev_inv_number;
  l_bcat_inv_str    t_bcat_inv_str;
  l_grant_id        t_grant_id;
  l_release_date    t_release_date;
  l_src_hash        vc_array;
  t_facility        THIS_ICAT.FACILITY_SHORT_NAME%type;

  n NUMBER;
BEGIN
  log_pkg.write_log('migrating proposal data');

  select FACILITY_SHORT_NAME
  into t_facility
  from THIS_ICAT;



  -- fetch the non-duplicate rows
  OPEN c_investigation_data;
  FETCH c_investigation_data BULK COLLECT INTO
    l_inv_number,
    l_visit_id,
    l_facility_cycle,
    l_instrument,
    l_title,
    l_inv_type,
    l_inv_abstract,
    l_prev_inv_number,
    l_bcat_inv_str,
    l_grant_id,
    l_release_date,
    l_src_hash;
  CLOSE c_investigation_data;

  -- do the import/update
--  log_pkg.write_exception('beginning merge');
  FORALL indx IN l_visit_id.FIRST..l_visit_id.LAST
  SAVE EXCEPTIONS
  MERGE INTO investigation target
  USING(
    SELECT
      l_visit_id(indx)         AS visit_id,
      l_inv_number(indx)       AS inv_number,
      l_facility_cycle(indx)   AS facility_cycle,
      l_instrument(indx)       AS instrument,
      l_title(indx)            AS title,
      l_inv_type(indx)         AS inv_type,
      l_inv_abstract(indx)     AS inv_abstract,
      l_prev_inv_number(indx)  AS prev_inv_number,
      l_bcat_inv_str(indx)     AS bcat_inv_str,
      l_grant_id(indx)         AS grant_id,
      l_release_date(indx)     AS release_date,
      l_src_hash(indx)         AS src_hash
    FROM dual
    ) source
  ON (target.src_hash = source.src_hash)
  WHEN MATCHED THEN
    UPDATE SET
      inv_number      = source.inv_number,
      visit_id        = upper(source.visit_id),
      facility_cycle  = source.facility_cycle,
      instrument      = source.instrument,
      title           = source.title,
      inv_type        = source.inv_type,
      inv_abstract    = source.inv_abstract,
      prev_inv_number = source.prev_inv_number,
      bcat_inv_str    = source.bcat_inv_str,
      grant_id        = source.grant_id,
      facility        = t_facility,
      release_date = source.release_date,
      mod_time        = systimestamp,
      mod_id          = p_mod_id,
      create_id = LV_CREATE_ID,
      create_time = nvl(mod_time,systimestamp),
      facility_acquired = 'Y',
      deleted = 'N'
    WHERE (target.inv_number != source.inv_number
    OR upper(target.visit_id) != upper(source.visit_id)
    OR target.facility_cycle != source.facility_cycle
    OR(target.facility_cycle IS NULL AND source.facility_cycle IS NOT NULL)
    OR(target.facility_cycle IS NOT NULL AND source.facility_cycle IS NULL)
    OR target.instrument != source.instrument
    OR target.title != source.title
    OR target.inv_type != source.inv_type
    OR (target.inv_abstract != source.inv_abstract
         OR (target.inv_abstract IS NULL AND source.inv_abstract IS NOT NULL)
         OR (target.inv_abstract IS NOT NULL AND source.inv_abstract IS NULL)
        )
    OR (target.prev_inv_number != source.prev_inv_number
         OR (target.prev_inv_number IS NULL AND source.prev_inv_number IS NOT NULL)
         OR (target.prev_inv_number IS NOT NULL AND source.prev_inv_number IS NULL)
        )
    OR (target.bcat_inv_str != source.bcat_inv_str
         OR (target.bcat_inv_str IS NULL AND source.bcat_inv_str IS NOT NULL)
         OR (target.bcat_inv_str IS NOT NULL AND source.bcat_inv_str IS NULL)
        )
    OR (target.grant_id != source.grant_id
         OR (target.grant_id IS NULL AND source.grant_id IS NOT NULL)
         OR (target.grant_id IS NOT NULL AND source.grant_id IS NULL)
        )
    OR (target.release_date != source.release_date
         OR (target.release_date IS NULL AND source.release_date IS NOT NULL)
         OR (target.release_date IS NOT NULL AND source.release_date IS NULL)
        )
      OR target.create_id != LV_CREATE_ID
      OR target.create_time is null
      OR target.facility_acquired is null
      OR target.deleted is null)
  WHEN NOT MATCHED THEN
    INSERT(
      id,
      inv_number,
      visit_id,
      facility_cycle,
      facility,
      instrument,
      title,
      inv_type,
      inv_abstract,
      prev_inv_number,
      bcat_inv_str,
      grant_id,
      release_date,
      mod_time,
      mod_id,
      create_id,
      deleted,
      src_hash,
      create_time,
      facility_acquired)
    VALUES(
      investigation_id_seq.NEXTVAL,
      source.inv_number,
      source.visit_id,
      source.facility_cycle,
      t_facility,
      source.instrument,
      source.title,
      source.inv_type,
      source.inv_abstract,
      source.prev_inv_number,
      source.bcat_inv_str,
      source.grant_id,
      source.release_date,
      systimestamp,
      p_mod_id,
            p_mod_id,
     -- LV_CREATE_ID,
      'N',
      source.src_hash,
      systimestamp,
      'Y'
    );
--  log_pkg.write_exception('end merge');



  log_pkg.write_log('proposal migration successful');
EXCEPTION
  WHEN bulk_errors THEN
    DECLARE
      ln_errors PLS_INTEGER;
      ln_error_index PLS_INTEGER;
    BEGIN
      log_pkg.write_exception('error in  merge');

      g_bulk_warnings := TRUE;

      ln_errors := SQL%bulk_exceptions.COUNT;
      log_pkg.write_exception('proposal migration failed');
      log_pkg.write_exception('number of records that failed: ' || ln_errors,1);

      -- log details of the record and the error
      FOR i IN 1..ln_errors
      LOOP
        ln_error_index := SQL%bulk_exceptions(i).error_index;
        set_record_ignored(
            l_inv_number(ln_error_index),
            l_instrument(ln_error_index),
            l_visit_id(ln_error_index),
            l_facility_cycle(ln_error_index));

        log_pkg.write_exception(
          write_proposal(
            l_inv_number(ln_error_index),
            l_instrument(ln_error_index),
            l_visit_id(ln_error_index),
            l_facility_cycle(ln_error_index)),
          2);

        log_pkg.write_exception(SQLERRM(-SQL%bulk_exceptions(i).error_code),3);
      END LOOP;
    END;
  WHEN OTHERS THEN
    log_pkg.write_exception('proposal migration failed');
    RAISE;
END migrate_investigations;


--------------------------------------------------------------------------------

PROCEDURE migrate_shifts(
   inv_id IN investigation.id%TYPE, p_mod_id IN investigation.mod_id%TYPE default 'PROPAGATION') IS

  -- data to be loaded into the shift table
  CURSOR c_shift_data IS
    SELECT
--      To_Char(p.propos_no)    AS inv_number,
      UPPER(p.propos_categ_code|| p.propos_categ_cpt) AS inv_number, 
      Lower(i.instr_nom)      AS instrument, -- lowercase lookup name!
      Dense_Rank() over(PARTITION BY p.propos_no, Lower(i.instr_nom)
                            ORDER BY pl.pl_date_deb, pl.pl_no)
                              AS visit_id,
      Lower(NULL)             AS facility_cycle, -- lowercase lookup name!
      inv.id                  AS investigation_id,
      &icatdls_username..shift_time(pl.pl_date_deb,pl.pl_shifts_deb)          AS start_date,
      &icatdls_username..shift_time(pl.pl_date_fin,pl.pl_shifts_fin)          AS end_date,
      pl.pl_com               AS shift_comment
    FROM proposal@duodesk p,
         duo_proposal@duodesk dp,
         mesure@duodesk m,
         instrument@duodesk i,
         planning@duodesk pl,
         investigation inv
    WHERE inv.id=inv_id 
    AND dp.desk_propos_no = p.propos_no
    AND m.mes_propos_no = p.propos_no
    AND m.mes_instr_no = i.instr_no
    AND mes_uni_all > 0 -- indicates an approved proposal
    AND pl.pl_mes_no = m.mes_no
    AND pl.pl_date_deb IS NOT NULL
    AND pl.pl_date_fin IS NOT NULL
    AND inv.src_hash = util_pkg.get_md5(propos_no,instr_no,pl_no)
    -- need to include this bit if we have facility cycles in diamond...
    -- AND inv.facility_cyle = ?????
    AND Nvl(p.propos_efface,'N') != 'Y'
    AND Nvl(m.mes_efface,'N') != 'Y'
    AND Nvl(i.instr_efface,'N') != 'Y'
    AND Nvl(pl.pl_efface,'N') != 'Y'
    AND NOT EXISTS(
      SELECT NULL
      FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
      WHERE inv_number = inv.inv_number
      AND instrument = inv.instrument
      AND (lower(visit_id) = lower(inv.visit_id) OR (visit_id IS NULL AND inv.visit_id IS NULL))
      AND (facility_cycle = inv.facility_cycle
          OR (facility_cycle IS NULL AND inv.facility_cycle IS NULL))
      );


  TYPE t_inv_number IS TABLE OF investigation.inv_number%TYPE;
  TYPE t_instrument IS TABLE OF investigation.instrument%TYPE;
  TYPE t_visit_id IS TABLE OF investigation.visit_id%TYPE;
  TYPE t_facility_cycle IS TABLE OF investigation.facility_cycle%TYPE;
  TYPE t_investigation_id IS TABLE OF investigator.investigation_id%TYPE;
  TYPE t_start_date IS TABLE OF shift.start_date%TYPE;
  TYPE t_end_date IS TABLE OF shift.end_date%TYPE;
  TYPE t_shift_comment IS TABLE OF shift.shift_comment%TYPE;

  l_inv_number t_inv_number;
  l_instrument t_instrument;
  l_visit_id t_visit_id;
  l_facility_cycle t_facility_cycle;
  l_investigation_id t_investigation_id;
  l_start_date t_start_date;
  l_end_date t_end_date;
  l_shift_comment t_shift_comment;
BEGIN
  log_pkg.write_log('migrating shift data');




  OPEN c_shift_data;
  FETCH c_shift_data BULK COLLECT INTO
    l_inv_number,
    l_instrument,
    l_visit_id,
    l_facility_cycle,
    l_investigation_id,
    l_start_date,
    l_end_date,
    l_shift_comment;
  CLOSE c_shift_data;




  FORALL indx IN l_visit_id.FIRST..l_visit_id.LAST
  SAVE EXCEPTIONS
  MERGE INTO shift target
  USING(
    SELECT
      l_investigation_id(indx)  AS investigation_id,
      l_start_date(indx)        AS start_date,
      l_end_date(indx)          AS end_date,
      l_shift_comment(indx)     AS shift_comment
    FROM dual
    ) source
  ON(target.investigation_id = source.investigation_id
    AND target.start_date = source.start_date
    AND target.end_date = source.end_date)
  WHEN MATCHED THEN
    UPDATE SET
      shift_comment = source.shift_comment,
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = nvl(create_id,LV_CREATE_ID),
      create_time = nvl(mod_time,systimestamp),
      facility_acquired = 'Y',
      deleted = 'N'
    WHERE
       (target.shift_comment != source.shift_comment
         OR (target.shift_comment IS NULL AND source.shift_comment IS NOT NULL)
         OR (target.shift_comment IS NOT NULL AND source.shift_comment IS NULL)
        )
      OR target.create_id != LV_CREATE_ID
      OR target.create_time is null
      OR target.facility_acquired is null
      OR target.deleted is null
  WHEN NOT MATCHED THEN
    INSERT(
      investigation_id,
      start_date,
      end_date,
      shift_comment,
      mod_time,
      mod_id,
      create_id,
      create_time,
      facility_acquired,
      deleted)
    VALUES(
      source.investigation_id,
      source.start_date,
      source.end_date,
      source.shift_comment,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N'
    );

  log_pkg.write_log('shift migration successful');
EXCEPTION
  WHEN bulk_errors THEN
    DECLARE
      ln_errors PLS_INTEGER;
      ln_error_index PLS_INTEGER;
    BEGIN
      g_bulk_warnings := TRUE;

      ln_errors := SQL%bulk_exceptions.COUNT;
      log_pkg.write_exception('shift migration failed');
      log_pkg.write_exception('number of records that failed: ' || ln_errors,1);

      -- log details of the record and the error
      FOR i IN 1..ln_errors
      LOOP
        ln_error_index := SQL%bulk_exceptions(i).error_index;
        set_record_ignored(
            l_inv_number(ln_error_index),
            l_instrument(ln_error_index),
            l_visit_id(ln_error_index),
            l_facility_cycle(ln_error_index));

        log_pkg.write_exception(
          write_proposal(
            l_inv_number(ln_error_index),
            l_instrument(ln_error_index),
            l_visit_id(ln_error_index),
            l_facility_cycle(ln_error_index)),
          2);

        log_pkg.write_exception(SQLERRM(-SQL%bulk_exceptions(i).error_code),3);
      END LOOP;
    END;
  WHEN OTHERS THEN
    log_pkg.write_exception('shift migration failed');
    RAISE;
END migrate_shifts;

--------------------------------------------------------------------------------



PROCEDURE migrate_investigators(
  inv_id IN investigation.id%TYPE, p_mod_id IN investigation.mod_id%TYPE default 'PROPAGATION') IS

  -- data to be loaded into the investigator table
  CURSOR c_investigator_data
      IS
         SELECT DISTINCT *
                    FROM (SELECT i.inv_number, i.instrument, i.visit_id,
                                 i.facility_cycle,
                                 bp.usernumber AS facility_user_id,
                                 i.ID AS investigation_id,
                                 bp.userrole AS ROLE
                            FROM vwselectpeopleforproposal_ts@duodesk bp,
                                 investigation i
                           WHERE i.id=inv_id
                             AND lower(bp.visit || '-' || bp.visit_number) =
                                                            LOWER (i.visit_id)
                             AND i.inv_number = UPPER(bp.visit)
                             AND bp.fedid IS NOT NULL
                          UNION
                          SELECT i.inv_number, i.instrument, i.visit_id,
                                 i.facility_cycle,
                                 bp.usernumber AS facility_user_id,
                                 i.ID AS investigation_id,
                                 bp.userrole AS ROLE
                            FROM vwselectpeopleforplanning@duodesk bp,
                                 investigation i,
                                 proposal@duodesk p
                           WHERE i.id=inv_id
                             AND lower(bp.visit || '-' || bp.visit_number) =
                                                            LOWER (i.visit_id)
                             AND bp.propos_no=p.propos_no
                             AND i.inv_number = UPPER(p.propos_categ_code || p.propos_categ_cpt)
                             AND bp.fedid IS NOT NULL) inv
                   WHERE NOT EXISTS (
                            SELECT NULL
                              FROM TABLE
                                      (CAST
                                          (g_inv_unique_fields AS tt_inv_unique_fields
                                          )
                                      )
                             WHERE inv_number = inv.inv_number
                               AND instrument = inv.instrument
                               AND (   lower(visit_id) = lower(inv.visit_id)
                                    OR (    visit_id IS NULL
                                        AND inv.visit_id IS NULL
                                       )
                                   )
                               AND (   facility_cycle = inv.facility_cycle
                                    OR (    facility_cycle IS NULL
                                        AND inv.facility_cycle IS NULL
                                       )
                                   ));


  TYPE t_inv_number IS TABLE OF investigation.inv_number%TYPE;
  TYPE t_instrument IS TABLE OF investigation.instrument%TYPE;
  TYPE t_visit_id IS TABLE OF investigation.visit_id%TYPE;
  TYPE t_facility_cycle IS TABLE OF investigation.facility_cycle%TYPE;
  TYPE t_facility_user_id IS TABLE OF investigator.facility_user_id%TYPE;
  TYPE t_investigation_id IS TABLE OF investigator.investigation_id%TYPE;
  TYPE t_role IS TABLE OF investigator.role%TYPE;

  l_inv_number t_inv_number;
  l_instrument t_instrument;
  l_visit_id t_visit_id;
  l_facility_cycle t_facility_cycle;
  l_facility_user_id t_facility_user_id;
  l_investigation_id t_investigation_id;
  l_role t_role;


CURSOR c_experimentor_data is
SELECT inv_number, instrument, visit_id, facility_cycle,
           facility_user_id, investigation_id, role
    FROM(
      SELECT
        i.inv_number            AS inv_number,
        i.instrument            AS instrument,
        i.visit_id              AS visit_id,
        i.facility_cycle        AS facility_cycle,
        To_Char(p.usernumber)   AS facility_user_id,
        i.id                    AS investigation_id,
        p.entrolename           AS role,
        -- and allow the merge to work
        Row_Number() over(PARTITION BY p.usernumber, i.id
                          ORDER BY p.entrolename nulls last) rn
      FROM
        investigation i,
        (SELECT DISTINCT
            proposc.proposc_sc_mat,
            --proposal_ts.proposal_no
            p.propos_categ_code,
            p.propos_categ_cpt
          FROM proposc@duodesk proposc,
              proposal_ts@duodesk proposal_ts,
              proposal@duodesk p
          WHERE proposc.proposc_propos_no = proposal_ts.proposal_no
             AND p.propos_no = proposal_ts.proposal_no
        ) prop,
        tblpeople@duodesk p,
        facility_user fu
      WHERE i.id=inv_id
      AND i.inv_number = Upper(prop.propos_categ_code || prop.propos_categ_cpt) 
      AND p.entid = prop.proposc_sc_mat
      AND fu.facility_user_id = To_Char(p.usernumber)
      AND NOT EXISTS(
       SELECT NULL
        FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
        WHERE inv_number = i.inv_number
        AND instrument = i.instrument
        AND (lower(visit_id) = lower(i.visit_id) OR (visit_id IS NULL AND i.visit_id IS NULL))
        AND (facility_cycle = i.facility_cycle
          OR (facility_cycle IS NULL AND i.facility_cycle IS NULL))
       )
    ) WHERE rn = 1;

  l_exp_inv_number t_inv_number;
  l_exp_instrument t_instrument;
  l_exp_visit_id t_visit_id;
  l_exp_facility_cycle t_facility_cycle;
  l_exp_facility_user_id t_facility_user_id;
  l_exp_investigation_id t_investigation_id;
  l_exp_role t_role;

BEGIN
  log_pkg.write_log('migrating investigator data');



  OPEN c_investigator_data;
  FETCH c_investigator_data BULK COLLECT INTO
    l_inv_number,
    l_instrument,
    l_visit_id,
    l_facility_cycle,
    l_facility_user_id,
    l_investigation_id,
    l_role;
  CLOSE c_investigator_data;

  log_pkg.write_log('investigator cursor opened');




  FORALL indx IN l_inv_number.FIRST..l_inv_number.LAST
  SAVE EXCEPTIONS
  MERGE INTO investigator target
    USING(
    SELECT
      l_facility_user_id(indx)  AS facility_user_id,
      l_investigation_id(indx)  AS investigation_id,
      l_role(indx)              AS role
    FROM dual
    ) source
  ON(target.facility_user_id = source.facility_user_id
      AND target.investigation_id = source.investigation_id)
  WHEN MATCHED THEN
    UPDATE SET
      role = source.role,
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = LV_CREATE_ID,
      create_time = nvl(mod_time,systimestamp),
      facility_acquired = 'Y',
      deleted = 'N'
    WHERE ((target.role != source.role
          OR (target.role IS NULL AND source.role IS NOT NULL)
          OR (target.role IS NOT NULL AND source.role IS NULL)
        )
      OR target.create_id != LV_CREATE_ID
      OR target.create_time is null
      OR target.facility_acquired is null
      OR target.deleted is null)
  WHEN NOT MATCHED THEN
    INSERT(
      facility_user_id,
      investigation_id,
      role,
      mod_time,
      mod_id,
      create_id,
      create_time,
      facility_acquired,
      deleted)
    VALUES(
      source.facility_user_id,
      source.investigation_id,
      substr(source.role||' - PROPOSER',1,255),
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N');
 

  OPEN c_experimentor_data;
  FETCH c_experimentor_data BULK COLLECT INTO
    l_exp_inv_number,
    l_exp_instrument,
    l_exp_visit_id,
    l_exp_facility_cycle,
    l_exp_facility_user_id,
    l_exp_investigation_id,
    l_exp_role;
  CLOSE c_experimentor_data;


FORALL indx IN l_exp_inv_number.FIRST..l_exp_inv_number.LAST
  SAVE EXCEPTIONS
  MERGE INTO investigator target
  USING(
    SELECT
      l_exp_facility_user_id(indx)  AS facility_user_id,
      l_exp_investigation_id(indx)  AS investigation_id,
      l_exp_role(indx)              AS role
    FROM dual
    ) source
  ON(target.facility_user_id = source.facility_user_id
      AND target.investigation_id = source.investigation_id)
  WHEN MATCHED THEN
    UPDATE SET
      role = substr(source.role||' - PROPOSER-EXPERIMENTOR',1,255),
      mod_time = systimestamp,
      mod_id = p_mod_id
  WHERE target.role != substr(source.role||' - PROPOSER-EXPERIMENTOR',1,255)
  WHEN NOT MATCHED THEN
    INSERT(
      facility_user_id,
      investigation_id,
      role,
      mod_time,
      mod_id,
      create_id,
      create_time,
      facility_acquired,
      deleted)
    VALUES(
      source.facility_user_id,
      source.investigation_id,
      substr(source.role||' - EXPERIMENTOR',1,255),
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N');


  log_pkg.write_log('investigator merge done');


  log_pkg.write_log('investigator migration successful');
EXCEPTION
  WHEN bulk_errors THEN
    DECLARE
      ln_errors PLS_INTEGER;
      ln_error_index PLS_INTEGER;
    BEGIN
      g_bulk_warnings := TRUE;

      ln_errors := SQL%bulk_exceptions.COUNT;
      log_pkg.write_exception('investigator migration failed');
      log_pkg.write_exception('number of records that failed: ' || ln_errors,1);

      -- log details of the record and the error
      FOR i IN 1..ln_errors
      LOOP
        ln_error_index := SQL%bulk_exceptions(i).error_index;
        set_record_ignored(
            l_inv_number(ln_error_index),
            l_instrument(ln_error_index),
            l_visit_id(ln_error_index),
            l_facility_cycle(ln_error_index));

        log_pkg.write_exception(
          write_proposal(
            l_inv_number(ln_error_index),
            l_instrument(ln_error_index),
            l_visit_id(ln_error_index),
            l_facility_cycle(ln_error_index)),
          2);

        log_pkg.write_exception(
          'Investigator: '||l_facility_user_id(ln_error_index),2);

        log_pkg.write_exception(SQLERRM(-SQL%bulk_exceptions(i).error_code),3);
      END LOOP;
    END;
  WHEN OTHERS THEN
    log_pkg.write_exception('investigator migration failed');
    RAISE;
END migrate_investigators;

--------------------------------------------------------------------------------

PROCEDURE update_keywords(
  inv_id IN investigation.id%TYPE, p_mod_id IN investigation.mod_id%TYPE default 'PROPAGATION') IS

  -- data to be loaded into the keyword table.
  -- each word in the Duodesk propsal title is taken to be an ICAT keyword.
  CURSOR c_keyword_data IS
    SELECT
      inv_number,
      instrument,
      visit_id,
      facility_cycle,
      investigation_id,
      name
    FROM(
      SELECT
        i.inv_number                        AS inv_number,
        i.instrument                        AS instrument,
        i.visit_id                          AS visit_id,
        i.facility_cycle                    AS facility_cycle,
        i.id                                AS investigation_id,
        SubStr(t.column_value,1,255) AS name,
        -- for each investigation, keywords must be unique when in the same case
        Row_Number() over(
          PARTITION BY i.id, SubStr(Lower(t.column_value),1,255) ORDER BY 1) rn
      FROM
        investigation i,
        proposal@duodesk p,
        TABLE(CAST(util_pkg.split_at_whitespace(p.propos_title) AS vc_array)) t
      --WHERE i.inv_number = To_Char(p.propos_no)
      WHERE i.id=inv_id 
      AND i.inv_number=UPPER(p.propos_categ_code || p.propos_categ_cpt) 
      AND NOT EXISTS(
        SELECT NULL
        FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
        WHERE inv_number = i.inv_number
        AND instrument = i.instrument
        AND (lower(visit_id) = lower(i.visit_id) OR (visit_id IS NULL AND i.visit_id IS NULL))
        AND (facility_cycle = i.facility_cycle
          OR (facility_cycle IS NULL AND i.facility_cycle IS NULL))
        )
      AND Nvl(p.propos_efface,'N') != 'Y'
      AND t.column_value IS NOT NULL
      AND Lower(t.column_value) NOT IN(
  -- list of unuseful keywords selected from actual data
    'I',
    'a',
    'about',
    'an',
    'are',
    'as',
    'at',
    'be',
    'by',
    'etc',
    'for',
    'from',
    'how',
    'in',
    'is',
    'it',
    'of',
    'on',
    'or',
    'that',
    'the',
    'this',
    'to',
    'was',
    'what',
    'when',
    'where',
    'who',
    'will',
    'with',
    'the')
    )
    WHERE rn = 1;

  TYPE t_inv_number IS TABLE OF investigation.inv_number%TYPE;
  TYPE t_instrument IS TABLE OF investigation.instrument%TYPE;
  TYPE t_visit_id IS TABLE OF investigation.visit_id%TYPE;
  TYPE t_facility_cycle IS TABLE OF investigation.facility_cycle%TYPE;
  TYPE t_investigation_id IS TABLE OF keyword.investigation_id%TYPE;
  TYPE t_name IS TABLE OF keyword.name%TYPE;

  l_inv_number t_inv_number;
  l_instrument t_instrument;
  l_visit_id t_visit_id;
  l_facility_cycle t_facility_cycle;
  l_investigation_id t_investigation_id;
  l_name t_name;
BEGIN
  log_pkg.write_log('updating keyword data');


  OPEN c_keyword_data;
  FETCH c_keyword_data BULK COLLECT INTO
    l_inv_number,
    l_instrument,
    l_visit_id,
    l_facility_cycle,
    l_investigation_id,
    l_name;
  CLOSE c_keyword_data;


  FORALL indx IN l_inv_number.FIRST..l_inv_number.LAST
  SAVE EXCEPTIONS
  MERGE INTO keyword target
  USING(
    SELECT
      l_investigation_id(indx)  AS investigation_id,
      l_name(indx)              AS name
    FROM dual
    ) source
  ON(target.investigation_id = source.investigation_id
      AND Lower(target.name) = Lower(source.name))
  WHEN MATCHED THEN
    UPDATE SET
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = LV_CREATE_ID,
      create_time = nvl(mod_time, systimestamp),
      facility_acquired = 'Y',
      deleted = 'N'
    WHERE target.create_id != LV_CREATE_ID
      OR target.create_time is null
      OR target.facility_acquired is null
      OR target.deleted is null
  WHEN NOT MATCHED THEN
    INSERT(
      investigation_id,
      name,
      mod_time,
      mod_id,
      create_id,
      create_time,
      facility_acquired,
      deleted)
    VALUES(
      source.investigation_id,
      source.name,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N');

  -- the case of the name may have been changed but we can't update that
  -- within the merge so we do it here
  FORALL indx IN l_inv_number.FIRST..l_inv_number.LAST
  SAVE EXCEPTIONS
  UPDATE keyword
    SET name = l_name(indx)
    WHERE investigation_id = l_investigation_id(indx)
    AND Lower(name) = Lower(l_name(indx))
    AND name != l_name(indx);

  log_pkg.write_log('keyword update successful');
EXCEPTION
  WHEN bulk_errors THEN
    DECLARE
      ln_errors PLS_INTEGER;
      ln_error_index PLS_INTEGER;
    BEGIN
      g_bulk_warnings := TRUE;

      ln_errors := SQL%bulk_exceptions.COUNT;
      log_pkg.write_exception('keyword update failed');
      log_pkg.write_exception('number of records that failed: ' || ln_errors,1);

      -- log details of the record and the error
      FOR i IN 1..ln_errors
      LOOP
        ln_error_index := SQL%bulk_exceptions(i).error_index;
        set_record_ignored(
            l_inv_number(ln_error_index),
            l_instrument(ln_error_index),
            l_visit_id(ln_error_index),
            l_facility_cycle(ln_error_index));

        log_pkg.write_exception(
          write_proposal(
            l_inv_number(ln_error_index),
            l_instrument(ln_error_index),
            l_visit_id(ln_error_index),
            l_facility_cycle(ln_error_index)),
          2);

        log_pkg.write_exception('Keyword: '||l_name(ln_error_index),2);

        log_pkg.write_exception(SQLERRM(-SQL%bulk_exceptions(i).error_code),3);
      END LOOP;
    END;
  WHEN OTHERS THEN
    log_pkg.write_exception('keyword update failed');
    RAISE;
END update_keywords;

--------------------------------------------------------------------------------

PROCEDURE update_publications(
  inv_id IN investigation.id%TYPE, p_mod_id IN investigation.mod_id%TYPE default 'PROPAGATION') IS

  -- data to be loaded into the publication table.
  -- publications in Duopdesk are stored in a single field, delimited by
  -- carriage returns
  CURSOR c_publication_data IS
    SELECT
      i.inv_number                   AS inv_number,
      i.instrument                   AS instrument,
      i.visit_id                     AS visit_id,
      i.facility_cycle               AS facility_cycle,
      i.id                           AS investigation_id,
      SubStr(t.column_value,1,4000)  AS full_reference,
      NULL                           AS url,
      NULL                           AS repository_id,
      NULL                           AS repository
    FROM duo_proposal@duodesk dp, investigation i,
         proposal@duodesk p,
        TABLE(CAST(util_pkg.string_to_table(
                RTrim(dp.exp_publications),Chr(10)) AS vc_array)) t
    WHERE i.id=inv_id
    AND t.column_value IS NOT NULL
    --AND To_Char(dp.desk_propos_no) = i.inv_number
    AND i.inv_number = Upper(p.propos_categ_code || p.propos_categ_cpt)
    AND p.propos_no = dp.desk_propos_no
    AND NOT EXISTS(
      SELECT NULL
      FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
      WHERE inv_number = i.inv_number
      AND instrument = i.instrument
      AND (lower(visit_id) = lower(i.visit_id) OR (visit_id IS NULL AND i.visit_id IS NULL))
      AND (facility_cycle = i.facility_cycle
        OR (facility_cycle IS NULL AND i.facility_cycle IS NULL))
      )
    ;

  TYPE t_inv_number IS TABLE OF investigation.inv_number%TYPE;
  TYPE t_instrument IS TABLE OF investigation.instrument%TYPE;
  TYPE t_visit_id IS TABLE OF investigation.visit_id%TYPE;
  TYPE t_facility_cycle IS TABLE OF investigation.facility_cycle%TYPE;
  TYPE t_investigation_id IS TABLE OF publication.investigation_id%TYPE;
  TYPE t_full_reference IS TABLE OF publication.full_reference%TYPE;
  TYPE t_url IS TABLE OF publication.url%TYPE;
  TYPE t_repository_id IS TABLE OF publication.repository_id%TYPE;
  TYPE t_repository IS TABLE OF publication.repository%TYPE;

  l_inv_number t_inv_number;
  l_instrument t_instrument;
  l_visit_id t_visit_id;
  l_facility_cycle t_facility_cycle;
  l_investigation_id t_investigation_id;
  l_full_reference t_full_reference;
  l_url t_url;
  l_repository_id t_repository_id;
  l_repository t_repository;
BEGIN
  log_pkg.write_log('updating publication data');

  -- in Duodesk these are in duo_proposal.exp_publications field, separated by
  -- newlines.


  OPEN c_publication_data;
  FETCH c_publication_data BULK COLLECT INTO
    l_inv_number,
    l_instrument,
    l_visit_id,
    l_facility_cycle,
    l_investigation_id,
    l_full_reference,
    l_url,
    l_repository_id,
    l_repository;
  CLOSE c_publication_data;


  FORALL indx IN l_inv_number.FIRST..l_inv_number.LAST
  SAVE EXCEPTIONS
  MERGE INTO publication target
  USING(
    SELECT
      l_investigation_id(indx)  AS investigation_id,
      l_full_reference(indx)    AS full_reference,
      l_url(indx)               AS url,
      l_repository_id(indx)     AS repository_id,
      l_repository(indx)        AS repository
    FROM dual
    ) source
  ON (Upper(target.full_reference) = Upper(source.full_reference)
      AND target.investigation_id = source.investigation_id)
  WHEN matched THEN
    UPDATE SET
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = LV_CREATE_ID,
      create_time = nvl(mod_time, systimestamp),
      facility_acquired = 'Y',
      deleted = 'N'
    WHERE target.create_id != LV_CREATE_ID
      OR target.create_time is null
      OR target.facility_acquired is null
      OR target.deleted is null
  WHEN NOT matched THEN
    INSERT(
      id,
      investigation_id,
      full_reference,
      url,
      repository_id,
      repository,
      mod_time,
      mod_id,
      create_id,
      create_time,
      facility_acquired,
      deleted)
    VALUES(
      publication_id_seq.NEXTVAL,
      source.investigation_id,
      source.full_reference,
      source.url,
      source.repository_id,
      source.repository,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N');


  -- the case of the full_reference may have been changed but we can't update
  -- that within the merge so we do it here
  FORALL indx IN l_inv_number.FIRST..l_inv_number.LAST
  SAVE EXCEPTIONS
  UPDATE publication
    SET full_reference = l_full_reference(indx)
    WHERE investigation_id = l_investigation_id(indx)
    AND Lower(full_reference) = Lower(l_full_reference(indx))
    AND full_reference != l_full_reference(indx);


  log_pkg.write_log('publication update successful');
EXCEPTION
  WHEN bulk_errors THEN
    DECLARE
      ln_errors PLS_INTEGER;
      ln_error_index PLS_INTEGER;
    BEGIN
      g_bulk_warnings := TRUE;

      ln_errors := SQL%bulk_exceptions.COUNT;
      log_pkg.write_exception('publication update failed');
      log_pkg.write_exception('number of records that failed: ' || ln_errors,1);

      -- log details of the record and the error
      FOR i IN 1..ln_errors
      LOOP
        ln_error_index := SQL%bulk_exceptions(i).error_index;
        set_record_ignored(
            l_inv_number(ln_error_index),
            l_instrument(ln_error_index),
            l_visit_id(ln_error_index),
            l_facility_cycle(ln_error_index));

        log_pkg.write_exception(
          write_proposal(
            l_inv_number(ln_error_index),
            l_instrument(ln_error_index),
            l_visit_id(ln_error_index),
            l_facility_cycle(ln_error_index)),
          2);

        log_pkg.write_exception(
          'Publication: '||l_full_reference(ln_error_index),2);

        log_pkg.write_exception(SQLERRM(-SQL%bulk_exceptions(i).error_code),3);
      END LOOP;
    END;
  WHEN OTHERS THEN
    log_pkg.write_exception('publication update failed');
    RAISE;
END update_publications;

--------------------------------------------------------------------------------

PROCEDURE migrate_samples(
  inv_id IN investigation.id%TYPE, p_mod_id IN investigation.mod_id%TYPE default 'PROPAGATION') IS

  -- samples in Duodesk which exist in ICAT but were not created by the
  -- propagation proces (have null value for sample_proposal_id).
  -- importing these records would violate the unique constraint
  CURSOR c_duplicate_samples IS
    SELECT
      i.inv_number      AS inv_number,
      i.instrument      AS instrument,
      i.visit_id        AS visit_id,
      i.facility_cycle  AS facility_cycle,
      samp.name         AS sample_name
    FROM sample@duodesk s,
         requested_instrument@duodesk ri,
         duo_proposal@duodesk p,
         proposal@duodesk pp,
         investigation i,
         sample samp
    WHERE i.id=inv_id 
    AND ri.proposal_id = p.duo_propos_no
    AND s.requested_instrument_id = ri.id
    AND s.name = samp.name
    AND samp.instance IS NULL
    AND samp.proposal_sample_id IS NULL
    AND i.inv_number = UPPER(pp.propos_categ_code || pp.propos_categ_cpt)
    AND pp.propos_no= p.desk_propos_no
    --AND i.inv_number = To_Char(p.desk_propos_no)
    AND NOT EXISTS(
      SELECT NULL
      FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
      WHERE inv_number = i.inv_number
      AND instrument = i.instrument
      AND (lower(visit_id) = lower(i.visit_id) OR (visit_id IS NULL AND i.visit_id IS NULL))
      AND (facility_cycle = i.facility_cycle
        OR (facility_cycle IS NULL AND i.facility_cycle IS NULL))
      )
    ;



  -- duodesk data to be imported
  CURSOR c_sample_data IS
    SELECT 
      i.inv_number               AS inv_number,
      i.instrument               AS instrument,
      i.visit_id                 AS visit_id,
      i.facility_cycle           AS facility_cycle,
      i.id                       AS investigation_id,
      s.name                     AS name,
      NULL                       AS instance,
      s.chemical_formula         AS chemical_formula,
      'See sample parameters'    AS safety_information,
      s.id                       AS proposal_sample_id
    FROM sample@duodesk s,
         requested_instrument@duodesk ri,
         duo_proposal@duodesk p,
         proposal@duodesk pp,
         investigation i
    WHERE i.id=inv_id
    AND ri.proposal_id = p.duo_propos_no
    AND s.requested_instrument_id = ri.id
    AND s.name IS NOT NULL
    AND i.inv_number = upper(pp.propos_categ_code || pp.propos_categ_cpt)
    AND p.desk_propos_no = pp.propos_no
    --AND i.inv_number = To_Char(p.desk_propos_no)
    AND NOT EXISTS(
      SELECT NULL
      FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
      WHERE inv_number = i.inv_number
      AND instrument = i.instrument
      AND (lower(visit_id) = lower(i.visit_id) OR (visit_id IS NULL AND i.visit_id IS NULL))
      AND (facility_cycle = i.facility_cycle
        OR (facility_cycle IS NULL AND i.facility_cycle IS NULL))
      )
      and s.id <> 7448
    ;

  TYPE t_inv_number IS TABLE OF investigation.inv_number%TYPE;
  TYPE t_instrument IS TABLE OF investigation.instrument%TYPE;
  TYPE t_visit_id IS TABLE OF investigation.visit_id%TYPE;
  TYPE t_facility_cycle IS TABLE OF investigation.facility_cycle%TYPE;
  TYPE t_investigation_id IS TABLE OF sample.investigation_id%TYPE;
  TYPE t_name IS TABLE OF sample.NAME%TYPE;
  TYPE t_instance IS TABLE OF sample.instance%TYPE;
  TYPE t_chemical_formula IS TABLE OF sample.chemical_formula%TYPE;
  TYPE t_safety_information IS TABLE OF sample.safety_information%TYPE;
  TYPE t_proposal_sample_id IS TABLE OF sample.proposal_sample_id%TYPE;

  l_inv_number t_inv_number;
  l_instrument t_instrument;
  l_visit_id t_visit_id;
  l_facility_cycle t_facility_cycle;
  l_investigation_id t_investigation_id;
  l_name t_name;
  i_instance t_instance;
  l_chemical_formula t_chemical_formula;
  l_safety_information t_safety_information;
  l_proposal_sample_id num_array;
BEGIN
  log_pkg.write_log('migrating sample data');


  -- as with the Investigation table migration, if a sample has been entered in
  -- both ICAT and Duodesk then we don't import anything for that investigation
  FOR rec IN c_duplicate_samples LOOP
    -- setting this means that the propagation process will be rerun, ignoring
    -- the proposals that cannot be migrated
    g_bulk_warnings := TRUE;

    set_record_ignored(
      rec.inv_number,
      rec.instrument,
      rec.visit_id,
      rec.facility_cycle);

    log_pkg.write_log(
      write_proposal(
        rec.inv_number,
        rec.instrument,
        rec.visit_id,
        rec.facility_cycle));

    log_pkg.write_log(
      'Proposal Sample Id: '||rec.sample_name,1);

    log_pkg.write_log(
      'A Sample with the same Name as the '||
      'Sample above has already been entered in ICAT.  The proposal '||
      'has not been imported.',2);
  END LOOP;


  -- fetch Sample records to migrate into ICAT
  OPEN c_sample_data;
  FETCH c_sample_data BULK COLLECT INTO
    l_inv_number,
    l_instrument,
    l_visit_id,
    l_facility_cycle,
    l_investigation_id,
    l_name,
    i_instance,
    l_chemical_formula,
    l_safety_information,
    l_proposal_sample_id;
  CLOSE c_sample_data;


  -- remove samples which have been removed from Duodesk
  FOR rec IN (
    SELECT
      s.id,
      s.name,
      inv.inv_number,
      inv.visit_id,
      inv.instrument,
      inv.facility_cycle
    FROM sample s, investigation inv
    WHERE inv.id = s.investigation_id
    AND s.deleted = 'N'
    AND s.proposal_sample_id NOT IN(
      SELECT * FROM TABLE(l_proposal_sample_id)
      )
    AND NOT EXISTS(
      SELECT NULL
      FROM investigation i,
           TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields)) i2
      WHERE id = s.id
      AND i2.inv_number = i.inv_number
      AND i2.instrument = i.instrument
      AND (lower(i2.visit_id) = lower(i.visit_id) OR (i2.visit_id IS NULL AND i.visit_id IS NULL))
      AND (i2.facility_cycle = i.facility_cycle
        OR (i2.facility_cycle IS NULL AND i.facility_cycle IS NULL))
      )
    )
  LOOP
    BEGIN
      log_pkg.write_log(
        write_proposal(
          rec.inv_number,
          rec.instrument,
          rec.visit_id,
          rec.facility_cycle));

      log_pkg.write_log(
        'Proposal Sample Id: '||rec.id,2);

      set_deleted_pkg.do_sample(rec.id);

      log_pkg.write_log(
        'The above Sample was removed from Duodesk and has been '||
        'removed from ICAT', 3);
    EXCEPTION
      WHEN OTHERS THEN
        set_record_ignored(
          rec.inv_number,
          rec.instrument,
          rec.visit_id,
          rec.facility_cycle);

        log_pkg.write_exception(
          'The above Sample has been removed from Duodesk but could '||
          'be not removed from ICAT',3);
    END;
  END LOOP;


  -- migrate the data
  FORALL indx IN l_inv_number.FIRST..l_inv_number.LAST
  SAVE EXCEPTIONS
  MERGE INTO sample target
  USING(
    SELECT
      l_investigation_id(indx)    AS investigation_id,
      l_name(indx)                AS name,
      i_instance(indx)            AS instance,
      l_chemical_formula(indx)    AS chemical_formula,
      l_safety_information(indx)  AS safety_information,
      l_proposal_sample_id(indx)  AS proposal_sample_id
    FROM dual
    ) source
  ON(target.proposal_sample_id = source.proposal_sample_id
     AND target.investigation_id = source.investigation_id)
  WHEN matched THEN
    UPDATE SET
      name =source.name,
      instance = source.instance,
      chemical_formula = source.chemical_formula,
      safety_information = source.safety_information,
      mod_time = systimestamp,
      mod_id = p_mod_id,
      create_id = LV_CREATE_ID,
      create_time = nvl(mod_time,systimestamp),
      facility_acquired = 'Y',
      deleted = 'N'
    WHERE target.name = source.name
    AND (target.investigation_id != source.investigation_id
      OR (target.instance != source.instance
          OR (target.instance IS NULL AND source.instance IS NOT NULL)
          OR (target.instance IS NOT NULL AND source.instance IS NULL)
          )
      OR (target.chemical_formula != source.chemical_formula
          OR (target.chemical_formula IS NULL AND source.chemical_formula IS NOT NULL)
          OR (target.chemical_formula IS NOT NULL AND source.chemical_formula IS NULL)
          )
      OR target.safety_information != source.safety_information
      OR target.create_id != LV_CREATE_ID
      OR target.create_time is null
      OR target.facility_acquired is null
      OR target.deleted is null
    )
  WHEN NOT matched THEN
    INSERT(
      id,
      investigation_id,
      name,
      instance,
      chemical_formula,
      safety_information,
      proposal_sample_id,
      mod_time,
      mod_id,
      create_id,
      create_time,
      facility_acquired,
      deleted)
    VALUES(
      sample_id_seq.NEXTVAL,
      source.investigation_id,
      source.name,
      source.instance,
      source.chemical_formula,
      source.safety_information,
      source.proposal_sample_id,
      systimestamp,
      p_mod_id,
      LV_CREATE_ID,
      systimestamp,
      'Y',
      'N')
--     WHERE 1 =0 
         ;

  log_pkg.write_log('sample migration successful');
EXCEPTION
  WHEN bulk_errors THEN
    DECLARE
      ln_errors PLS_INTEGER;
      ln_error_index PLS_INTEGER;
    BEGIN
      g_bulk_warnings := TRUE;

      ln_errors := SQL%bulk_exceptions.COUNT;
      log_pkg.write_exception('sample migration failed' || SQLCODE || '  '  || SQLERRM);
      log_pkg.write_exception('number of records that failed: ' || ln_errors,1);

      -- log details of the record and the error
      FOR i IN 1..ln_errors
      LOOP
        ln_error_index := SQL%bulk_exceptions(i).error_index;
        set_record_ignored(
            l_inv_number(ln_error_index),
            l_instrument(ln_error_index),
            l_visit_id(ln_error_index),
            l_facility_cycle(ln_error_index));

        log_pkg.write_exception(
          write_proposal(
            l_inv_number(ln_error_index),
            l_instrument(ln_error_index),
            l_visit_id(ln_error_index),
            l_facility_cycle(ln_error_index)),
          2);

        log_pkg.write_exception(
          'Proposal Sample Id: '||
          l_proposal_sample_id(ln_error_index),2);

        log_pkg.write_exception(SQLERRM(-SQL%bulk_exceptions(i).error_code),3);
      END LOOP;
    END;
  WHEN OTHERS THEN
    log_pkg.write_exception('sample migration failed' || SQLCODE || '  '  || SQLERRM );
    RAISE;
END migrate_samples;



--------------------------------------------------------------------------------
PROCEDURE populate_icat_authorisation (inv_id IN investigation.id%TYPE, p_mod_id IN investigation.mod_id%TYPE default 'PROPAGATION')  IS


CURSOR     c_get_facility_user_details  is
  SELECT    a.federal_id federal_id,
     b.investigation_id investigation_id
  FROM      facility_user a, investigator b
  WHERE b.investigation_id=inv_id     
  AND a.facility_user_id = b.facility_user_id
  AND       a.federal_id is not null
--  AND       a.federal_id = p_federal_id
  ORDER BY  federal_id;

TYPE t_federal_id       IS TABLE OF facility_user.federal_id%TYPE;
TYPE t_investigation_id IS TABLE OF investigator.investigation_id%TYPE;

l_federal_id       t_federal_id;
l_investigation_id t_investigation_id;

BEGIN


OPEN c_get_facility_user_details;
FETCH c_get_facility_user_details BULK COLLECT INTO
l_federal_id,
l_investigation_id;
CLOSE c_get_facility_user_details;

FORALL indx IN l_federal_id.FIRST .. l_federal_id.LAST
SAVE EXCEPTIONS
MERGE INTO icat_authorisation target
--where target_element_type     = 'INVESTIGATION'
USING (SELECT
      l_federal_id(indx)      AS federal_id,
    l_investigation_id(indx)  AS investigation_id
    FROM dual) source
ON (    target.user_id          = source.federal_id
   AND  target.element_id       = source.investigation_id
   )
  WHEN NOT MATCHED THEN
   INSERT(
  ID,
  USER_ID,
  ROLE,
  ELEMENT_TYPE,
  ELEMENT_ID,
  PARENT_ELEMENT_TYPE,
  PARENT_ELEMENT_ID,
  MOD_TIME,
  MOD_ID,
  CREATE_TIME,
  CREATE_ID,
  FACILITY_ACQUIRED,
  DELETED)

   VALUES(icat_authorisation_id_seq.nextval,
          source.federal_id,
    'CREATOR',
    'INVESTIGATION',
    source.investigation_id,
    null,
    null,
    systimestamp,
    p_mod_id,
    systimestamp,
    p_mod_id,
    'Y',
    'N');



   --execute immediate 'ALTER TRIGGER air_facility_user_trg ENABLE';

EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception('icat authorisation population failed');
    RAISE;
END populate_icat_authorisation;







---------------------------------------------------------------------------------

PROCEDURE duodesk_pr(
  visit investigation.visit_id%TYPE, beamline investigation.instrument%TYPE,p_mod_id IN investigation.mod_id%TYPE default 'PROPAGATION') IS

  ln_iteration PLS_INTEGER := 0;
  investigation_id investigation.id%TYPE;
BEGIN
  log_pkg.init;
  SAVEPOINT migration_sp;

  log_pkg.write_log('Data Migration started');
  log_pkg.write_log('Modification: '||p_mod_id,1);

  -- first do the lookup data
  -- lookup data is loaded from external tables in the load_external_data_pkg
  -- package.
  -- diffs between the data in the source of the migration and in the
  -- spreadsheet of lookup tables can be reviewed in an ApEx application and
  -- applied to the spreadsheet manually.  the spreadsheet is the authority on
  -- lookup data so we don't update lookup tables here.
  -- however, we do insert any lookup data which is not already in the
  -- spreadsheet.
  -- parameter records may be updated, eg if they are used as a sample parameter
  -- but not recorded as such in the spreadsheet (the is_sample_parameter
  -- column would be updated to 'Y')
  migrate_instruments(p_mod_id);
  migrate_parameters(p_mod_id);


  -- these store unique key info for the proposals which cannot be migrated
  g_inv_unique_fields := tt_inv_unique_fields();
  g_ignore_by_id := vc_array();

  -- now the application data.
  -- if any part of an investigation cannot be migrated then no part is
  -- migrated.  we store the inv_ids for records which cannot be migrated and
  -- rerun the migration process, omitting those investigations.
  LOOP
    ln_iteration := ln_iteration + 1;
    log_pkg.write_log('*****');
    log_pkg.write_log( -- eg first iteration
      To_Char(Trunc(SYSDATE,'YYYY') + ln_iteration - 1,'fmdddTH')||
      ' iteration',1);
    log_pkg.write_log('*****');

    g_bulk_warnings := FALSE;
    log_pkg.write_log('populating investigation: '||visit||' '||beamline);
    migrate_investigations(visit_id=>visit,beamline=> beamline);
    
    log_pkg.write_log('getting investigation_id');
    select id into investigation_id from investigation where visit_id=upper(visit) and instrument= beamline;
    log_pkg.write_log('getting investigation_id DOne!');
    migrate_shifts(inv_id =>investigation_id);
    migrate_facility_users(p_mod_id);
    migrate_investigators(inv_id =>investigation_id);
    update_keywords(inv_id =>investigation_id);
    update_publications(inv_id =>investigation_id);
    migrate_samples(inv_id =>investigation_id);
    migrate_sample_parameters(p_mod_id);
    populate_icat_authorisation(inv_id =>investigation_id);

    --populate_icat_authorisation(p_mod_id);
    -- if there were no errors during the dml sections then we leave the loop.
    EXIT WHEN g_bulk_warnings = FALSE;
    ROLLBACK TO migration_loop_sp;  -- sp created in "migrate_investigations"
    -- this loop should never iterate more than 2 times because on each
    -- iteration we are working with a subset of the records used in the
    -- previous iteration, and that subset was successful before.
    -- to prevent an infinite loop, just in case:
    EXIT WHEN ln_iteration > 2;
  END LOOP;

  IF ln_iteration > 2 THEN
    log_pkg.write_log('Data Migration finished, UNSUCCESSFUL');
    log_pkg.write_log('Unable to find a set of valid proposals',1);
  ELSIF g_inv_unique_fields.Count > 0 THEN
    log_pkg.write_log('Data Migration finished with warnings');
    log_pkg.write_log('Could not migrate data for the following proposal(s).'||
                      '  See above for details:');
      FOR rec IN(
        SELECT DISTINCT
          inv_number, instrument, visit_id, facility_cycle
        FROM TABLE(g_inv_unique_fields) ORDER BY 1, 2, 3, 4
      )
      LOOP
          log_pkg.write_log(
            write_proposal(
              rec.inv_number, rec.instrument, rec.visit_id, rec.facility_cycle),
            1);
      END LOOP;
  ELSE
    log_pkg.write_log('Data Migration finished successfully');
    COMMIT;
  END IF;
EXCEPTION
  WHEN test_wall THEN
    log_pkg.write_log('hit the test wall');
    -- no rollback.  this exception lets us run to a certain point to examine
    -- the state of the data, for testing
    RAISE;
  WHEN OTHERS THEN
    ROLLBACK TO migration_sp;
    &icatdls_username..email_problem('ICAT',SQLERRM);
    log_pkg.write_exception(SQLERRM,1);
    log_pkg.write_log('Data Migration finished, UNSUCCESSFUL');
    RAISE;
END duodesk_pr;

--------------------------------------------------------------------------------

END batch_single_migration_pkg;
/

