CREATE OR REPLACE PACKAGE ICATDLS33."BATCH_MIGRATION_PUBLICATION" AS

PROCEDURE duodesk_pr(
  p_mod_id IN investigation.mod_id%TYPE);

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

END batch_migration_publication;
/


CREATE OR REPLACE PACKAGE BODY ICATDLS33."BATCH_MIGRATION_PUBLICATION" AS

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

--TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))

LV_CREATE_ID CONSTANT investigation.create_id%TYPE := 'FROM PROPAGATION';
LV_REMOVED_CREATE_ID CONSTANT investigation.create_id%TYPE
  := 'FROM PROPAGATION, SUBSEQUENTLY REMOVED';

--------------------------------------------------------------------------------

PROCEDURE set_lookup_deleted(
  p_mod_id IN investigation.mod_id%TYPE) IS
BEGIN
  -- set lookup table rows deleted if they don't exist on the external lookup
  -- tables and don't exist in the propagation source (if applicable).
log_pkg.write_log('setting lookup');
  UPDATE instrument SET
    deleted = 'Y', mod_time = systimestamp, mod_id = p_mod_id
    WHERE name NOT IN(
      SELECT Lower(name)
      FROM instrument@duodesk
      UNION
      SELECT Lower(name)
      FROM extern_instrument
      WHERE Upper(dls) = 'Y'
    );

  UPDATE parameter SET
    deleted = 'Y', mod_time = systimestamp, mod_id = p_mod_id
    WHERE (name, units) NOT IN(
      SELECT Lower(name), Nvl(units,'N/A')
      FROM sample_parameter@duodesk
      UNION
      SELECT Lower(name), Nvl(units,'N/A')
      FROM extern_parameter_list
      WHERE Upper(dls) = 'Y'
    );

  UPDATE investigation_type SET
    deleted = 'Y', mod_time = systimestamp, mod_id = p_mod_id
    WHERE (name) NOT IN(
      SELECT Lower(name)
      FROM extern_investigation_type
      WHERE Upper(dls) = 'Y'
    );

  UPDATE facility_cycle SET
    deleted = 'Y', mod_time = systimestamp, mod_id = p_mod_id
    WHERE (name) NOT IN(
      SELECT Lower(name)
      FROM extern_facility_cycle
      WHERE Upper(dls) = 'Y'
    );

  UPDATE study_status SET
    deleted = 'Y', mod_time = systimestamp, mod_id = p_mod_id
    WHERE (name) NOT IN(
      SELECT Lower(name)
      FROM extern_study_status
      WHERE Upper(dls) = 'Y'
    );

  UPDATE dataset_status SET
    deleted = 'Y', mod_time = systimestamp, mod_id = p_mod_id
    WHERE (name) NOT IN(
      SELECT Lower(name)
      FROM extern_dataset_status
      WHERE Upper(dls) = 'Y'
    );

  UPDATE dataset_type SET
    deleted = 'Y', mod_time = systimestamp, mod_id = p_mod_id
    WHERE (name) NOT IN(
      SELECT Lower(name)
      FROM extern_dataset_type
      WHERE Upper(dls) = 'Y'
    );

  UPDATE datafile_format SET
    deleted = 'Y', mod_time = systimestamp, mod_id = p_mod_id
    WHERE (name, version) NOT IN(
      SELECT Lower(name), version
      FROM extern_datafile_format
      WHERE Upper(dls) = 'Y'
    );
log_pkg.write_log('setting lookup complite');
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception('setting lookup  FAILED: '||SQLERRM);
    RAISE;

END set_lookup_deleted;

--------------------------------------------------------------------------------
PROCEDURE cleaup_investigator IS
Begin
  -- remove investigators which were previously in duodesk but now removed from
  -- there.
  -- this took far too long (>10 mins) as a single statement, even with hints
  -- like materialize and push_subq to make the first subquery run first.
  -- that subquery is now populating a global temp table.  :-(
  log_pkg.write_log('cleaning up investigator');
  INSERT INTO dls_migration_tmp1 (id, usernumber)
    SELECT
        i.id, p.usernumber
      FROM
        investigation i,
        (SELECT
            DISTINCT
            proposc.proposc_sc_mat,
            --proposal.propos_no,
            proposal.propos_categ_code,
            proposal.propos_categ_cpt
          FROM proposc@duodesk proposc,
              proposal@duodesk proposal
          WHERE proposc.proposc_propos_no = proposal.propos_no
          AND Nvl(proposal.propos_efface,'N') != 'Y'
          AND Nvl(proposc.proposc_efface,'N') != 'Y'
        ) prop,
        tblpeople@duodesk p
      --WHERE i.inv_number = To_Char(prop.propos_no)
      WHERE i.inv_number = UPPER(prop.propos_categ_code || prop.propos_categ_cpt)
      AND p.entid = prop.proposc_sc_mat;

  DELETE
  FROM investigator
  WHERE create_id = LV_CREATE_ID -- ie, created by the propagation process
  AND (investigation_id, facility_user_id) NOT IN(
        SELECT id, usernumber
        FROM dls_migration_tmp1
      )
  AND investigation_id IN (
      SELECT
        id
      FROM investigation i2
    );
log_pkg.write_log('investigator cleanup complite');
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception('investigator cleanup  FAILED: '||SQLERRM);
    RAISE;

End cleaup_investigator;
----------------------------

PROCEDURE cleanup_shifts(
  p_mod_id IN investigation.mod_id%TYPE) IS

BEGIN
  log_pkg.write_log('cleanup shift data');


  -- if start or end dates are modified in duodesk then we cannot identify the
  -- ICAT record and update it accordingly, so we delete from ICAT if there is
  -- no match in Duodesk.
  -- *** only records created by the propagation procedure or which have
  -- previously had a corresponding entry in Duodesk will be deleted ***
  /*DELETE FROM shift
  WHERE create_id = LV_CREATE_ID -- ie, created by the propagation process
  AND (investigation_id, start_date, end_date) NOT IN(
    SELECT
      --To_Char(p.propos_no)    AS inv_number,
      --p.propos_categ_code  
      --|| p.propos_categ_cpt  AS inv_number, --bug
      inv.id,
      shift_time(pl.pl_date_deb,pl.pl_shifts_deb)          AS start_date,
      shift_time(pl.pl_date_fin,pl.pl_shifts_fin)          AS end_date
    FROM proposal@duodesk p,
         duo_proposal@duodesk dp,
         mesure@duodesk m,
         instrument@duodesk i,
         planning@duodesk pl,
         investigation inv
    WHERE dp.desk_propos_no = p.propos_no
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
    )
  AND investigation_id IN (
      SELECT id
      FROM investigation i2
      WHERE NOT EXISTS(
        SELECT NULL
        FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
        WHERE inv_number = i2.inv_number
        AND instrument = i2.instrument
        AND (lower(visit_id) = lower(i2.visit_id) OR (visit_id IS NULL AND i2.visit_id IS NULL))
        AND (facility_cycle = i2.facility_cycle
          OR (facility_cycle IS NULL AND i2.facility_cycle IS NULL))
        )
    ); */
    -- possible improvement
    DELETE FROM shift
where create_id = LV_CREATE_ID 
AND investigation_id in (
      (SELECT id
      FROM investigation i2
      WHERE NOT EXISTS(
        SELECT NULL
        FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
        WHERE inv_number = i2.inv_number
        AND instrument = i2.instrument
        AND (lower(visit_id) = lower(i2.visit_id) OR (visit_id IS NULL AND i2.visit_id IS NULL))
        AND (facility_cycle = i2.facility_cycle
          OR (facility_cycle IS NULL AND i2.facility_cycle IS NULL))
        ))
    MINUS
        ( SELECT investigation_id from shift where create_id = LV_CREATE_ID
        AND (investigation_id, start_date, end_date) NOT IN(
         SELECT
         inv.id,
        shift_time(pl.pl_date_deb,pl.pl_shifts_deb)          AS start_date,
        shift_time(pl.pl_date_fin,pl.pl_shifts_fin)          AS end_date
    FROM proposal@duodesk p,
         duo_proposal@duodesk dp,
         mesure@duodesk m,
         instrument@duodesk i,
         planning@duodesk pl,
         investigation inv
    WHERE dp.desk_propos_no = p.propos_no
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
    ))
    ); 
log_pkg.write_log('cleanup shift data complited');
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception('cleanup shift data FAILED: '||SQLERRM);
    RAISE;

END cleanup_shifts;
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



PROCEDURE update_keywords(
  p_mod_id IN investigation.mod_id%TYPE) IS

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
      WHERE i.inv_number=UPPER(p.propos_categ_code || p.propos_categ_cpt) 
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

  -- take the info in the Duodesk PROPOSAL.PROPOS_TITLE column and use it to
  -- populate the keyword table with values which are unique in lowercase.
  -- delete records which do not have a match in Duodesk and were created by
  -- the propagation process
  /*DELETE FROM keyword
    WHERE create_id = LV_CREATE_ID
    AND (investigation_id, Lower(name)) NOT IN
      (
      SELECT i.id, t.column_value
      FROM
        investigation i,
        proposal@duodesk p,
        TABLE(CAST(util_pkg.split_at_whitespace(Lower(p.propos_title)) AS vc_array)) t
      --WHERE i.inv_number = To_Char(p.propos_no)
      WHERE i.inv_number = UPPER(p.propos_categ_code|| p.propos_categ_cpt) 
      AND Nvl(p.propos_efface,'N') != 'Y'
      )
    AND investigation_id IN
        (
        SELECT id
        FROM investigation i2
        WHERE NOT EXISTS(
          SELECT NULL
          FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
          WHERE inv_number = i2.inv_number
          AND instrument = i2.instrument
          AND (lower(visit_id) = lower(i2.visit_id) OR (visit_id IS NULL AND i2.visit_id IS NULL))
          AND (facility_cycle = i2.facility_cycle
            OR (facility_cycle IS NULL AND i2.facility_cycle IS NULL))
          )
        );
          */    
    -- possible iprovement
    
    DELETE FROM keyword 
WHERE create_id = LV_CREATE_ID 
AND investigation_id in (
  (
        SELECT id
        FROM investigation i2
        WHERE NOT EXISTS(
          SELECT NULL
          FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
          WHERE inv_number = i2.inv_number
          AND instrument = i2.instrument
          AND (lower(visit_id) = lower(i2.visit_id) OR (visit_id IS NULL AND i2.visit_id IS NULL))
          AND (facility_cycle = i2.facility_cycle
            OR (facility_cycle IS NULL AND i2.facility_cycle IS NULL))
          )
        )
    MINUS
    (select investigation_id from keyword
    WHERE create_id = LV_CREATE_ID
    AND (investigation_id, Lower(name)) NOT IN
      (
      SELECT i.id, t.column_value
      FROM
        investigation i,
        proposal@duodesk p,
        TABLE(CAST(util_pkg.split_at_whitespace(Lower(p.propos_title)) AS vc_array)) t
      --WHERE i.inv_number = To_Char(p.propos_no)
      WHERE i.inv_number = UPPER(p.propos_categ_code|| p.propos_categ_cpt) 
      AND Nvl(p.propos_efface,'N') != 'Y'
      ))
      );
      

  OPEN c_keyword_data;
  FETCH c_keyword_data BULK COLLECT INTO
    l_inv_number,
    l_instrument,
    l_visit_id,
    l_facility_cycle,
    l_investigation_id,
    l_name;
  CLOSE c_keyword_data;

  /* testing error trapping.  cause a constraint violation
  Dbms_Output.put_line('testing bulk error collection: keywords');
  FOR i IN 1..20 LOOP
    IF g_inv_unique_fields.Count = 0 AND Mod(i,3) = 0 THEN
      Dbms_Output.put_line(
        'breaking investigation '||
        'Proposal: '||l_inv_number(i)||' | '||
        'Instrument: '||Nvl(l_instrument(i),'Unspecified')||' | '||
        'Visit: '||Nvl(l_visit_id(i),'Unspecified')||' | '||
        'Facility Cycle: '||Nvl(l_facility_cycle(i),'Unspecified'));

      l_investigation_id(i) := l_investigation_id(i) * -1;
    END IF;
  END LOOP;
  --*/

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
--      create_id = LV_CREATE_ID,
--      create_time = nvl(create_time, systimestamp),
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
  p_mod_id IN investigation.mod_id%TYPE) IS

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
    WHERE t.column_value IS NOT NULL
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

  -- delete records which do not have a match in Duodesk and were created by
  -- the propagation process
  /*DELETE FROM publication
    WHERE create_id = LV_CREATE_ID
    AND (investigation_id, Upper(full_reference)) NOT IN
      (
      SELECT i.id, t.column_value
      FROM duo_proposal@duodesk dp, investigation i,
           proposal@duodesk p,
           TABLE(CAST(util_pkg.string_to_table(
             RTrim(Upper(dp.exp_publications)),Chr(10)) AS vc_array)) t
      WHERE t.column_value IS NOT NULL
      AND i.inv_number = UPPER(p.propos_categ_code || p.propos_categ_cpt)
      AND p.propos_no=dp.desk_propos_no
      --AND To_Char(dp.desk_propos_no) = i.inv_number
      )
    AND investigation_id IN
        (
        SELECT id
        FROM investigation i2
        WHERE NOT EXISTS(
          SELECT NULL
          FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
          WHERE inv_number = i2.inv_number
          AND instrument = i2.instrument
          AND (lower(visit_id) = lower(i2.visit_id) OR (visit_id IS NULL AND i2.visit_id IS NULL))
          AND (facility_cycle = i2.facility_cycle
            OR (facility_cycle IS NULL AND i2.facility_cycle IS NULL))
          )
        );*/

  -- this is Eter inproved vestion of the deletion from publication

DELETE FROM publication
    WHERE create_id = LV_CREATE_ID
    AND ID IN
      (SELECT ID FROM publication
        WHERE investigation_id IN
        (
            (
            SELECT id
            FROM investigation i2
            WHERE NOT EXISTS(
              SELECT NULL
              FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
              WHERE inv_number = i2.inv_number
              AND instrument = i2.instrument
              AND (lower(visit_id) = lower(i2.visit_id) OR (visit_id IS NULL AND i2.visit_id IS NULL))
              AND (facility_cycle = i2.facility_cycle
                OR (facility_cycle IS NULL AND i2.facility_cycle IS NULL))
              )
            ))        
       UNION
       (SELECT ID FROM publication
        WHERE (investigation_id, Upper(full_reference)) IN
          (
          SELECT i.id, t.column_value
          FROM (SELECT exp_publications, desk_propos_no 
                FROM duo_proposal@duodesk WHERE exp_publications IS NOT NULL) dp, investigation i,
               proposal@duodesk p,
               TABLE(CAST(util_pkg.string_to_table(
                 RTrim(Upper(dp.exp_publications)),Chr(10)) AS vc_array)) t
          WHERE t.column_value IS NOT NULL
          AND i.inv_number = UPPER(p.propos_categ_code || p.propos_categ_cpt)
          AND p.propos_no=dp.desk_propos_no
          ))
          );


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

  /* testing error trapping.  cause a constraint violation
  Dbms_Output.put_line('testing bulk error collection: keywords');
  FOR i IN 1..20 LOOP
    IF g_inv_unique_fields.Count = 0 AND Mod(i,3) = 0 THEN
      Dbms_Output.put_line(
        'breaking investigation '||
        'Proposal: '||l_inv_number(i)||' | '||
        'Instrument: '||Nvl(l_instrument(i),'Unspecified')||' | '||
        'Visit: '||Nvl(l_visit_id(i),'Unspecified')||' | '||
        'Facility Cycle: '||Nvl(l_facility_cycle(i),'Unspecified'));

      l_investigation_id(i) := l_investigation_id(i) * -1;
    END IF;
  END LOOP;
  --*/

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
--      create_id = LV_CREATE_ID,
--      create_time = nvl(create_time, systimestamp),
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
  p_mod_id IN investigation.mod_id%TYPE) IS

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
    WHERE ri.proposal_id = p.duo_propos_no
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
    WHERE ri.proposal_id = p.duo_propos_no
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

  /* testing error trapping.  cause a constraint violation
  Dbms_Output.put_line('testing bulk error collection: samples');
  FOR i IN 1..20 LOOP
    IF g_inv_unique_fields.Count = 0 AND Mod(i,3) = 0 THEN
      Dbms_Output.put_line(
        'breaking investigation '||
        'Proposal: '||l_inv_number(i)||' | '||
        'Instrument: '||Nvl(l_instrument(i),'Unspecified')||' | '||
        'Visit: '||Nvl(l_visit_id(i),'Unspecified')||' | '||
        'Facility Cycle: '||Nvl(l_facility_cycle(i),'Unspecified'));

      l_investigation_id(i) := l_investigation_id(i) * -1;
    END IF;
  END LOOP;
  --*/

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
--      create_id = LV_CREATE_ID,
--      create_time = nvl(create_time,systimestamp),
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
    log_pkg.write_log('sample parameter migration started');
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
--        create_id = LV_CREATE_ID,
--        create_time = nvl(create_time,systimestamp),
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
  log_pkg.write_log('sample parameter migration complited');
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

PROCEDURE duodesk_pr(
  p_mod_id IN investigation.mod_id%TYPE) IS

  ln_iteration PLS_INTEGER := 0;
BEGIN
  log_pkg.init;
  SAVEPOINT migration_sp;

  log_pkg.write_log('Publication Data Migration started');
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
  
  set_lookup_deleted(p_mod_id);
  migrate_parameters(p_mod_id);
  cleaup_investigator;
  cleanup_shifts(p_mod_id);
  
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
    update_keywords(p_mod_id);
    update_publications(p_mod_id);
    migrate_samples(p_mod_id);
    migrate_sample_parameters(p_mod_id);

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
    log_pkg.write_log('Publication Data Migration finished, UNSUCCESSFUL');
    log_pkg.write_log('Unable to find a set of valid proposals',1);
  ELSIF g_inv_unique_fields.Count > 0 THEN
    log_pkg.write_log('Publication Data Migration finished with warnings');
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
    log_pkg.write_log('Publication Data Migration finished successfully');
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
    ICATDLS33.email_problem('ICAT',SQLERRM);
    log_pkg.write_exception(SQLERRM,1);
    log_pkg.write_log('Publication Data Migration finished, UNSUCCESSFUL');
    RAISE;
END duodesk_pr;

--------------------------------------------------------------------------------

END batch_migration_publication;
/
