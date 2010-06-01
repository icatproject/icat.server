CREATE OR REPLACE PACKAGE ICATDLS33."BATCH_MIGRATION_INVESTIGATION" AS

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

END batch_migration_investigation;
/

CREATE OR REPLACE PACKAGE BODY ICATDLS33."BATCH_MIGRATION_INVESTIGATION" AS

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
--      create_id = nvl(create_id,LV_CREATE_ID),
--      create_time = nvl(create_time,systimestamp),
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

PROCEDURE migrate_investigations(
  p_mod_id IN investigation.mod_id%TYPE) IS

  -- A proposal on the Duodesk database maps to an investigation in the icatdls
  -- schema, however a sequence is used to generate the primary key in the
  -- investigation table

  -- the src_hash column is used to store the primary keys of the 3 Duodesk
  -- tables which make up each ICAT Investigation record.


  CURSOR c_duplicate_props IS
    -- proposals in Duodesk which already exist in ICAT but have a different or
    -- null value for src_hash.
    -- importing these records would violate the unique constraint on
    -- inv_number, visit_id, instrument, facility_cycle
    WITH props_to_import AS(
      SELECT inv_number, visit_id, facility_cycle, instrument,
              util_pkg.get_md5(propos_no,instr_no,pl_no) src_hash
      FROM (
        SELECT
--          To_Char(p.propos_no)     AS inv_number,
            UPPER(p.propos_categ_code
            || p.propos_categ_cpt) AS inv_number,
            UPPER(p.propos_categ_code
            || p.propos_categ_cpt
            || '-'
            || pl.pl_visit_no) AS visit_id,
            --Dense_Rank() over(PARTITION BY p.propos_no, Lower(i.instr_nom)
            --                ORDER BY pl.pl_date_deb, pl.pl_no)
            --                       AS visit_id,
          --Lower('DLS')              AS facility_cycle, -- lowercase lookup name!
          Lower(null)              AS facility_cycle, -- lowercase lookup name!
          Lower(i.instr_nom)       AS instrument, -- lowercase lookup name!
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
        WHERE dp.desk_propos_no = p.propos_no
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
      )
    SELECT p.*
    FROM props_to_import p, investigation i
    WHERE i.inv_number = p.inv_number
    AND lower(i.visit_id) = lower(p.visit_id)
    AND i.instrument = p.instrument
    AND (i.facility_cycle = p.facility_cycle
      OR i.facility_cycle IS NULL AND p.facility_cycle IS NULL)
    AND (i.src_hash IS NULL OR i.src_hash != p.src_hash)
    AND NOT EXISTS(
      SELECT NULL
      FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
      WHERE inv_number = i.inv_number
      AND instrument = i.instrument
      AND (lower(visit_id) = lower(i.visit_id) OR (visit_id IS NULL AND i.visit_id IS NULL))
      AND (facility_cycle = i.facility_cycle
          OR (facility_cycle IS NULL AND i.facility_cycle IS NULL))
      );


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
        WHERE dp.desk_propos_no = p.propos_no
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

  -- Duplicate entries in Duodesk and ICAT
  -- log records which are in Duodesk and also in ICAT, but where the ICAT
  -- record was not created by the propagation process, and ignore the Duodesk
  -- record for the rest of the propagation.

  FOR rec IN c_duplicate_props LOOP
    set_record_ignored(
        rec.inv_number, rec.instrument, rec.visit_id, rec.facility_cycle);

    log_pkg.write_log(
      write_proposal(
        rec.inv_number, rec.instrument, rec.visit_id, rec.facility_cycle));

    log_pkg.write_log(
      'An investigation with the same distinguishing attributes as the '||
      'proposal above has already been entered in ICAT.  The proposal '||
      'has not been imported.',1);
  END LOOP;


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


  /*testing error trapping.  cause a constraint violation
  Dbms_Output.put_line('testing bulk error collection: investigations');
  FOR i IN 1..20 LOOP
    IF g_inv_unique_fields.Count = 0 AND Mod(i,3) = 0 THEN
      Dbms_Output.put_line(
        'breaking investigation '||
        'Proposal: '||l_inv_number(i)||' | '||
        'Instrument: '||Nvl(l_instrument(i),'Unspecified')||' | '||
        'Visit: '||Nvl(l_visit_id(i),'Unspecified')||' | '||
        'Facility Cycle: '||Nvl(l_facility_cycle(i),'Unspecified'));

      l_inv_type(i) := 'break me';
    END IF;
  END LOOP;
  --*/


  -- Delete records which no longer exist in Duodesk
  -- records which previously existed in duodesk will have a non-null value in
  -- src_hash.
  -- if datasets exist there will be a foreign key violation when we delete.
  -- we log this and update the src_hash to null so the record will be ignored
  -- for further propagations.
  -- we do not want to delete any datasets!

  FOR rec IN (
    SELECT id, inv_number, instrument,  visit_id, facility_cycle
    FROM investigation i
    WHERE src_hash IS NOT NULL
    AND src_hash NOT IN(
      SELECT * FROM TABLE(l_src_hash)
      )
    AND id NOT IN(
      SELECT * FROM TABLE(g_ignore_by_id)
      )
    AND NOT EXISTS(
      SELECT NULL
      FROM TABLE(CAST(g_inv_unique_fields AS tt_inv_unique_fields))
      WHERE inv_number = i.inv_number
      AND instrument = i.instrument
      AND (lower(visit_id) = lower(i.visit_id) OR (visit_id IS NULL AND i.visit_id IS NULL))
      AND (facility_cycle = i.facility_cycle
          OR (facility_cycle IS NULL AND i.facility_cycle IS NULL))
      )
    )
  LOOP
    BEGIN
      log_pkg.write_log(
        write_proposal(
          rec.inv_number, rec.instrument, rec.visit_id, rec.facility_cycle));

      DELETE FROM investigation WHERE id = rec.id;

      log_pkg.write_log(
        'The above proposal was removed from Duodesk and has been '||
        'removed from ICAT', 1);
    EXCEPTION
      WHEN child_record_found_ex THEN
        -- ignore this record in future
        g_ignore_by_id.extend;
        g_ignore_by_id(g_ignore_by_id.Count) := rec.id;

        BEGIN
          UPDATE investigation SET
            src_hash = NULL,
            create_id = LV_REMOVED_CREATE_ID
          WHERE id = rec.id;

          log_pkg.write_log(
            'The above proposal has been removed from Duodesk but has '||
            'datasets added so was not removed from ICAT',1);
        EXCEPTION
          WHEN OTHERS THEN
            log_pkg.write_exception(
              'The above proposal has been removed from Duodesk but could '||
              'be not removed from ICAT',1);
            log_pkg.write_exception(
              'Error: '||SQLERRM,1);
        END;
      WHEN OTHERS THEN
        -- ignore this record in future
        g_ignore_by_id.extend;
        g_ignore_by_id(g_ignore_by_id.Count) := rec.id;

        log_pkg.write_exception(
          'The above proposal has been removed from Duodesk but could '||
          'be not removed from ICAT',1);
        log_pkg.write_exception(
          'Error: '||SQLERRM,1);
    END;
  END LOOP;


  -- if there are errors in the propagation we will rollback and start
  -- again, but we don't want to lose the changes made above

  SAVEPOINT migration_loop_sp;



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
--      create_id = LV_CREATE_ID,
--      create_time = nvl(create_time,systimestamp),
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
  p_mod_id IN investigation.mod_id%TYPE) IS

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
      shift_time(pl.pl_date_deb,pl.pl_shifts_deb)          AS start_date,
      shift_time(pl.pl_date_fin,pl.pl_shifts_fin)          AS end_date,
      pl.pl_com               AS shift_comment
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
    /*DELETE FROM shift
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
    ); */


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


  /* testing error trapping.  cause a constraint violation
  Dbms_Output.put_line('testing bulk error collection: shifts');
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
--      create_id = nvl(create_id,LV_CREATE_ID),
--      create_time = nvl(create_time,systimestamp),
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
--      create_id = LV_CREATE_ID,
--      create_time = nvl(create_time,systimestamp),
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

PROCEDURE migrate_investigators(
  p_mod_id IN investigation.mod_id%TYPE) IS

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
                           WHERE lower(bp.visit || '-' || bp.visit_number) =
                                                            LOWER (i.visit_id)
                             --AND i.inv_number = to_char(bp.propos_no)
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
                           WHERE lower(bp.visit || '-' || bp.visit_number) =
                                                            LOWER (i.visit_id)
                             --AND i.inv_number = to_char(bp.propos_no)
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
      WHERE i.inv_number = Upper(prop.propos_categ_code || prop.propos_categ_cpt) 
      --i.inv_number = To_Char(prop.proposal_no)
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

  /* testing error trapping.  cause a constraint violation
  Dbms_Output.put_line('testing bulk error collection: investigators');
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
--      create_id = LV_CREATE_ID,
--      create_time = nvl(create_time,systimestamp),
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

PROCEDURE populate_icat_authorisation (p_mod_id IN investigation.mod_id%TYPE)  IS


CURSOR     c_get_facility_user_details  is
  SELECT    a.federal_id federal_id,
     b.investigation_id investigation_id
  FROM      facility_user a, investigator b
  WHERE     a.facility_user_id = b.facility_user_id
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



----------------------------------------------------------------------------------
PROCEDURE duodesk_pr(
  p_mod_id IN investigation.mod_id%TYPE) IS

  ln_iteration PLS_INTEGER := 0;
BEGIN
  log_pkg.init;
  SAVEPOINT migration_sp;

  log_pkg.write_log('Investigation Data Migration started');
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

  -- set the deleted flag on lookup tables where the record has been removed
  -- from duodesk


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

    migrate_investigations(p_mod_id);
    migrate_shifts(p_mod_id);
    migrate_facility_users(p_mod_id);
    migrate_investigators(p_mod_id);
    populate_icat_authorisation(p_mod_id);

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
    log_pkg.write_log('Investigation  Data Migration finished, UNSUCCESSFUL');
    log_pkg.write_log('Unable to find a set of valid proposals',1);
  ELSIF g_inv_unique_fields.Count > 0 THEN
    log_pkg.write_log('Investigation  Data Migration finished with warnings');
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
    log_pkg.write_log('Investigation Data Migration finished successfully');
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
    log_pkg.write_log('Investigation Data Migration finished, UNSUCCESSFUL');
    RAISE;
END duodesk_pr;

--------------------------------------------------------------------------------

END batch_migration_investigation;
/
