CREATE OR REPLACE PACKAGE "POPULATE_BEAMLINES" AS

/*

Name: populate_beamlines_pkg
Author: James Healy
Creation Date: November 2006

Purpose:
  resides in the main icat database, pushes data to the beamline databases
    (which have the same table structure as the main icat database)

Description:
  the beamline databases each contain data for just one instrument.
  the beamline db/instrument combination is defined in the table
    BEAMLINE_INSTRUMENT.
  this package uses the data in that table to create synonyms to point at the
    right beamline database and then pushes data to it.

  when things go wrong:
    currently it just rolls back all DML for the instrument for which there was
    an error, and logs it.

*/

--------------------------------------------------------------------------------

-- Exceptions.
-- use numbers between -20100 and -20199 in this package

package_locked_exnum CONSTANT PLS_INTEGER := -20100;
package_locked_ex EXCEPTION;
PRAGMA EXCEPTION_INIT(package_locked_ex, -20100);

--------------------------------------------------------------------------------
        PROCEDURE propagate_data;
END populate_beamlines;
/

CREATE OR REPLACE PACKAGE BODY "POPULATE_BEAMLINES" AS

procedure close_db_link (p_dblink IN beamline_instrument.dblink%TYPE)
is
begin
execute immediate ('alter session close database link ' || p_dblink);
   EXCEPTION
      WHEN OTHERS
      THEN
         log_pkg.write_exception ('Closing DBLINK failure: ' || SQLERRM);
end;


PROCEDURE create_synonyms(
  p_dblink IN beamline_instrument.dblink%TYPE) IS

  PROCEDURE cre_syn(
    p_table IN VARCHAR2,
    p_syn_name IN VARCHAR2 DEFAULT NULL) IS

    lv_syn_name VARCHAR2(30) := Nvl(p_syn_name, p_table);
  BEGIN
    BEGIN
      EXECUTE IMMEDIATE('Drop Synonym bl_'||lv_syn_name);
    EXCEPTION
      WHEN OTHERS THEN
        IF SQLCODE != -1434 THEN
          -- ORA-01434: private synonym to be dropped does not exist
          NULL;
        END IF;
    END;

--/*
    EXECUTE IMMEDIATE(
      'Create Synonym bl_'||lv_syn_name||' For '||p_table||'@'||p_dblink);
--*/
/*
Dbms_Output.put_line('cre_syn modded for testing');
    EXECUTE IMMEDIATE(
      'Create Synonym bl_'||lv_syn_name||' For ikitten_i02_dev.'||p_table);
--*/
  END cre_syn;
BEGIN
  log_pkg.write_log('creating synonyms for database link '||p_dblink);

  cre_syn('SHIFT');
  cre_syn('INVESTIGATOR');
  cre_syn('FACILITY_USER');
  cre_syn('INVESTIGATION');
  cre_syn('THIS_ICAT');

  log_pkg.write_log('finished creating synonyms for database link '||p_dblink);
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception('Cannot create synonyms for '||p_dblink);
    RAISE;
END create_synonyms;

--------------------------------------------------------------------------------


procedure  push_this_icat is
begin
log_pkg.write_log('populating This Icat data');

merge INTO bl_this_icat target
  USING(
    SELECT *
    FROM this_icat
    WHERE (facility_short_name) NOT IN(
      SELECT facility_short_name from bl_this_icat
      )
      and deleted='N'
    ) source
  ON(target.facility_short_name = source.facility_short_name)
  WHEN matched THEN
    UPDATE SET
      facility_long_name = source.facility_long_name ,
      facility_url = source.facility_url,
      facility_description = source.facility_description,
      days_until_public_release = source.days_until_public_release,
      mod_time    = source.mod_time    ,
      mod_id      = source.mod_id,
          deleted     = source.deleted
  WHEN NOT matched THEN
    INSERT(
      facility_short_name        ,
      facility_long_name,
      facility_url,
      facility_description,
      days_until_public_release,
      seq_number ,
      mod_time    ,
      mod_id,
      create_id,
      create_time,
      deleted,
      facility_acquired)
    VALUES(
      source.facility_short_name        ,
      source.facility_long_name,
      source.facility_url,
      source.facility_description,
      source.days_until_public_release,
      source.seq_number ,
      source.mod_time    ,
      source.mod_id,
      source.create_id,
      source.create_time   ,
      source.deleted,
      source.facility_acquired);


log_pkg.write_log('Finished populating This Icat data');
end;


PROCEDURE push_investigation(p_instrument IN investigation.instrument%TYPE) IS
BEGIN
  log_pkg.write_log('populating investigation data');

  delete from bl_investigation where id in (select id from investigation 
                                            minus 
                                            select id from bl_investigation); 

  merge INTO bl_investigation target
  USING(
    SELECT *
    FROM investigation
    WHERE (id, mod_time) NOT IN(
      SELECT id, mod_time FROM bl_investigation
      )
    and deleted='N'
    AND instrument = p_instrument
    ) source
  ON(target.id = source.id)
  WHEN matched THEN
    UPDATE SET
      inv_number                 = source.inv_number      ,
      visit_id                   = source.visit_id        ,
      facility                   = source.facility        ,
      facility_cycle             = source.facility_cycle  ,
      instrument                 = source.instrument      ,
      title                      = source.title           ,
      inv_type                   = source.inv_type        ,
      inv_abstract               = source.inv_abstract    ,
      prev_inv_number            = source.prev_inv_number ,
      bcat_inv_str               = source.bcat_inv_str    ,
      grant_id                   = source.grant_id        ,
      inv_param_name             = source.inv_param_name  ,
      inv_param_value            = source.inv_param_value ,
      inv_start_date             = source.inv_start_date  ,
      inv_end_date               = source.inv_end_date  ,
      release_date               = source.release_date    ,
      mod_time                   = source.mod_time        ,
      mod_id                     = source.mod_id,
      deleted                    = source.deleted,
      src_hash                   = source.src_hash
  WHEN NOT matched THEN
    INSERT(
      id              ,
      inv_number      ,
      visit_id        ,
      facility        ,
      facility_cycle  ,
      instrument      ,
      title           ,
      inv_type        ,
      inv_abstract    ,
      prev_inv_number ,
      bcat_inv_str    ,
      grant_id        ,
      inv_param_name  ,
      inv_param_value ,
      inv_start_date  ,
      inv_end_date    ,
      release_date    ,
      mod_time        ,
      mod_id,
      create_id,
      create_time,
      deleted,
      src_hash,
      facility_acquired)
    VALUES(
      source.id              ,
      source.inv_number      ,
      source.visit_id        ,
      source. facility       ,
      source.facility_cycle  ,
      source.instrument      ,
      source.title           ,
      source.inv_type        ,
      source.inv_abstract    ,
      source.prev_inv_number ,
      source.bcat_inv_str    ,
      source.grant_id        ,
      source.inv_param_name  ,
      source.inv_param_value ,
      source.inv_start_date  ,
      source.inv_end_date    ,
      source.release_date    ,
      source.mod_time        ,
      source.mod_id,
      source.create_id,
      source.create_time  ,
      source.deleted,
      source.src_hash,
      source.facility_acquired
);

  log_pkg.write_log('finished populating investigation data');
END push_investigation;

--------------------------------------------------------------------------------

PROCEDURE push_shift(p_instrument IN investigation.instrument%TYPE) IS
test_visit_id varchar2(255);
BEGIN
  log_pkg.write_log('populating shift data');

  if (instr(p_instrument,'-') !=0 )
  then
    test_visit_id := 'NT55'||(to_number(substr(p_instrument,2,2))+30)||'-1';
  else
      if  (instr(p_instrument,'b') !=0 )
      then
        test_visit_id :='NT55'||(to_number(substr(p_instrument,2))+50)||'-1';
      else
        test_visit_id :='NT55'||substr(p_instrument,2)||'-1';
      end if;
  end if;


  -- we cannot link to a shift record in the main ICAT database if the start or
  -- end date has been changed, so we delete and reinsert.
  

  DELETE FROM bl_shift;

  INSERT INTO bl_shift
  SELECT s.*
    FROM shift s, investigation i
    WHERE s.investigation_id = i.id
    AND i.instrument = p_instrument;

update bl_shift set end_date =to_timestamp('01-01-2051 00:00:00','DD-MM-YYYY HH24:MI:SS') 
  where investigation_id = (select id from investigation where investigation.visit_id=test_visit_id);



  log_pkg.write_log('finished populating shift data');
END push_shift;

--------------------------------------------------------------------------------

PROCEDURE push_facility_user(p_instrument IN investigation.instrument%TYPE) IS
BEGIN
  log_pkg.write_log('populating facility_user data');

  delete from bl_facility_user where facility_user_id in (select facility_user_id from facility_user 
                                            minus 
                                            select facility_user_id from bl_facility_user); 


  MERGE INTO bl_facility_user target
  USING(
    SELECT *
    FROM facility_user
    WHERE (facility_user_id, mod_time) NOT IN(
      SELECT facility_user_id, mod_time FROM bl_facility_user
      )
    and deleted='N'
    ) source
  ON(target.facility_user_id = source.facility_user_id)
  WHEN MATCHED THEN
    UPDATE SET
      federal_id  = source.federal_id,
      title       = source.title,
      initials    = source.initials,
      first_name  = source.first_name,
      middle_name = source.middle_name,
      last_name   = source.last_name,
      mod_time    = source.mod_time,
      mod_id      = source.mod_id,
      deleted     = source.deleted
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
      deleted,
      facility_acquired)
    VALUES(
      source.facility_user_id,
      source.federal_id,
      source.title,
      source.initials,
      source.first_name,
      source.middle_name,
      source.last_name,
      source.mod_time,
      source.mod_id,
      source.create_id,
      source.create_time   ,
      source.deleted,
      source.facility_acquired);

  log_pkg.write_log('facility_user populated successfully');
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception('facility_user population FAILED: '||SQLERRM);
    RAISE;
END push_facility_user;

--------------------------------------------------------------------------------

PROCEDURE push_investigator(p_instrument IN investigation.instrument%TYPE) IS
BEGIN
  log_pkg.write_log('populating investigator data');

  delete from bl_investigator where (INVESTIGATION_ID,FACILITY_USER_ID) in (select INVESTIGATION_ID,FACILITY_USER_ID from investigator 
                                            minus 
                                            select INVESTIGATION_ID,FACILITY_USER_ID from bl_investigator); 
  merge INTO bl_investigator target
  USING(
    SELECT ir.*
    FROM investigator ir, investigation i
    WHERE ir.investigation_id = i.id
    AND (ir.investigation_id, ir.facility_user_id, ir.mod_time) NOT IN(
      SELECT investigation_id, facility_user_id, mod_time
      FROM bl_investigator
      )
    AND i.instrument = p_instrument
    and ir.deleted='N'
    ) source
  ON(target.facility_user_id = source.facility_user_id
     AND target.investigation_id = source.investigation_id)
  WHEN matched THEN
    UPDATE SET
      role         = source.role         ,
      mod_time     = source.mod_time     ,
      mod_id       = source.mod_id,
      deleted      = source.deleted
  WHEN NOT matched THEN
    INSERT(
      facility_user_id ,
      investigation_id ,
      role             ,
      mod_time         ,
      mod_id,
      create_id,
      create_time,
      deleted,
      facility_acquired)
    VALUES(
      source.facility_user_id ,
      source.investigation_id ,
      source.role             ,
      source.mod_time         ,
      source.mod_id,
      source.create_id,
      source.create_time ,
      source.deleted,
      source.facility_acquired);

  log_pkg.write_log('finished populating investigator data');
END push_investigator;

--------------------------------------------------------------------------------



PROCEDURE propagate_data IS

  x INTEGER;
  LV_LOCKNAME CONSTANT VARCHAR2(50) := 'POPULATE_BEAMLINES_PKG';--.propagate_data';
  lv_lockhandle VARCHAR2(100);

  CURSOR c_instruments IS
    SELECT instrument instrument, dblink
    FROM beamline_instrument
    WHERE valid='Y';
BEGIN
  log_pkg.init;
  log_pkg.write_log('Propagating data to beamline databases');

  -- we're modifying synonyms within this package so have to serialise access
  log_pkg.write_log('Acquiring lock');
  Dbms_Lock.allocate_unique(LV_LOCKNAME,lv_lockhandle);
  x := Dbms_Lock.request(
    lockhandle        => lv_lockhandle,
    lockmode          => Dbms_Lock.X_MODE, -- exclusive
    timeout           => 0,
    release_on_commit => FALSE);
  log_pkg.write_log('Package locked');
  IF x != 0 THEN
    Raise_Application_Error(
      package_locked_exnum,
      'Package is locked ('||To_Char(x)||') try later');
      log_pkg.write_log('Package locked 2');
  ELSE
    FOR rec IN c_instruments
    LOOP
      BEGIN
        log_pkg.write_log('');
        log_pkg.write_log('***');
        log_pkg.write_log('Instrument: '||rec.instrument);

        -- synonyms
        create_synonyms(rec.dblink);

        SAVEPOINT propagate_data_sp;

        -- lookup data
        push_this_icat;

 
        -- application data
        push_investigation(rec.instrument);
        push_shift(rec.instrument);
        push_facility_user(rec.instrument);
        push_investigator(rec.instrument);


        log_pkg.write_log('Propagation successful for instrument '||rec.instrument);
      EXCEPTION
        WHEN OTHERS THEN
          log_pkg.write_exception(
            'Propagation failed for instrument '||rec.instrument||Chr(10)||SQLERRM);
            &icatdls_username..email_problem(rec.instrument,SQLERRM);
          ROLLBACK TO propagate_data_sp;
      END;
      commit;
      close_db_link(rec.dblink);
      commit;
    END LOOP;

    -- DML in all but the last iteration of the loop is committed by the
    -- synonym creation section
    COMMIT;

    x := Dbms_Lock.RELEASE(lv_lockhandle);
    log_pkg.write_log('Package lock released');
  END IF;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception('Propagation failed: '||SQLERRM);
    &icatdls_username..email_problem('ICAT',SQLERRM);

    BEGIN
      x := Dbms_Lock.RELEASE(lv_lockhandle);
      log_pkg.write_log('Package lock released');
    EXCEPTION
      WHEN OTHERS THEN
      log_pkg.write_exception('Can''t release lock '||SQLERRM);  
    END;

    RAISE;
END propagate_data;

--------------------------------------------------------------------------------

END populate_beamlines;
/