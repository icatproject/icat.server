CREATE OR REPLACE PACKAGE ICATDLS33.populate_beamlines_pkg AS

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
END populate_beamlines_pkg;
/

CREATE OR REPLACE PACKAGE BODY ICATDLS33.populate_beamlines_pkg AS

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

  cre_syn('ACCESS_GROUP');
  cre_syn('ACCESS_GROUP_DLP');
  cre_syn('ACCESS_GROUP_ILP');
  cre_syn('DATAFILE');
  cre_syn('DATAFILE_PARAMETER');
  cre_syn('DATASET_LEVEL_PERMISSION');
  cre_syn('DATASET_PARAMETER');
  cre_syn('INVESTIGATION_LEVEL_PERMISSION', 'INVESTIGATION_LP');
  cre_syn('RELATED_DATAFILES');
  cre_syn('SHIFT');
  cre_syn('STUDY');
  cre_syn('STUDY_INVESTIGATION');
  cre_syn('TOPIC');
  cre_syn('TOPIC_LIST');
  cre_syn('USER_ACCESS_GROUP');
  cre_syn('INSTRUMENT');
  cre_syn('DATASET_TYPE');
  cre_syn('PARAMETER');
  cre_syn('DATAFILE_FORMAT');
  cre_syn('DATASET_STATUS');
  cre_syn('FACILITY_CYCLE');
  cre_syn('SAMPLE');
  cre_syn('KEYWORD');
  cre_syn('INVESTIGATOR');
  cre_syn('FACILITY_USER');
  cre_syn('INVESTIGATION');
  cre_syn('DATASET');
  cre_syn('SAMPLE_PARAMETER');
  cre_syn('PUBLICATION');
  cre_syn('STUDY_STATUS');
  cre_syn('INVESTIGATION_TYPE');
  cre_syn('APPLICATIONS');
  cre_syn('USER_ROLES');
  cre_syn('THIS_ICAT');

  log_pkg.write_log('finished creating synonyms for database link '||p_dblink);
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception('Cannot create synonyms for '||p_dblink);
    RAISE;
END create_synonyms;

--------------------------------------------------------------------------------

PROCEDURE push_instrument IS
BEGIN
  log_pkg.write_log('populating instrument data');

  merge INTO bl_instrument target
  USING(
    SELECT *
    FROM instrument
    WHERE (name, mod_time) NOT IN(
      SELECT name, mod_time FROM bl_instrument
      )
    and deleted='N'
    ) source
  ON(target.name = source.name)
  WHEN matched THEN
    UPDATE SET
      type        = source.type        ,
      description = source.description ,
      mod_time    = source.mod_time    ,
      mod_id      = source.mod_id,
      create_id   = source.create_id,
      deleted     = source.deleted
  WHEN NOT matched THEN
    INSERT(
      name        ,
      short_name  ,
      type        ,
      description ,
      mod_time    ,
      mod_id,
      create_id,
      create_time,
      deleted,
      facility_acquired)
    VALUES(
      source.name        ,
      source.short_name  ,
      source.type        ,
      source.description ,
      source.mod_time    ,
      source.mod_id,
      source.create_id,
      source.create_time,
      source.deleted,
      source.facility_acquired);

  log_pkg.write_log('finished populating instrument data');
END push_instrument;

--------------------------------------------------------------------------------

PROCEDURE push_parameter IS
BEGIN
  log_pkg.write_log('populating parameter data');

  merge INTO bl_parameter target
  USING(
    SELECT *
    FROM parameter
    WHERE (name, units, mod_time) NOT IN(
      SELECT name, units, mod_time FROM bl_parameter
      )
      and deleted='N'
    ) source
  ON (target.name = source.name
      AND target.units = source.units
      )
  WHEN matched THEN
    UPDATE SET
      units_long_version       = source.units_long_version       ,
      searchable               = source.searchable               ,
      numeric_value            = source.numeric_value            ,
      non_numeric_value_format = source.non_numeric_value_format ,
      is_sample_parameter      = source.is_sample_parameter      ,
      is_dataset_parameter     = source.is_dataset_parameter     ,
      is_datafile_parameter    = source.is_datafile_parameter    ,
      description              = source.description              ,
      mod_id                   = source.mod_id                   ,
      mod_time                 = source.mod_time,
      deleted                  = source.deleted,
      verified                 = source.verified
  WHEN NOT matched THEN
    INSERT(
      name                     ,
      units                    ,
      units_long_version       ,
      searchable               ,
      numeric_value            ,
      non_numeric_value_format ,
      is_sample_parameter      ,
      is_dataset_parameter     ,
      is_datafile_parameter    ,
      description              ,
      mod_id                   ,
      mod_time,
      create_id,
      create_time,
      deleted,
      facility_acquired,
      verified)
    VALUES(
      source.name                     ,
      source.units                    ,
      source.units_long_version       ,
      source.searchable               ,
      source.numeric_value            ,
      source.non_numeric_value_format ,
      source.is_sample_parameter      ,
      source.is_dataset_parameter     ,
      source.is_datafile_parameter    ,
      source.description              ,
      source.mod_id                   ,
      source.mod_time,
      source.create_id,
            source.create_time,
      source.deleted,
      source.facility_acquired,
      source.verified);

  log_pkg.write_log('finished populating parameter data');
END push_parameter;

--------------------------------------------------------------------------------

PROCEDURE push_investigation_type IS
BEGIN
  log_pkg.write_log('populating investigation_type data');

  merge INTO bl_investigation_type target
  USING(
    SELECT *
    FROM investigation_type
    WHERE (name, mod_time) NOT IN(
      SELECT name, mod_time FROM bl_investigation_type
      )
      and deleted='N'
    ) source
  ON(target.name = source.name)
  WHEN matched THEN
    UPDATE SET
      description = source.description ,
      mod_time    = source.mod_time    ,
      mod_id      = source.mod_id,
      deleted     = source.deleted
  WHEN NOT matched THEN
    INSERT(
      name        ,
      description ,
      mod_time    ,
      mod_id,
      create_id,
      create_time,
      deleted,
      facility_acquired)
    VALUES(
      source.name        ,
      source.description ,
      source.mod_time    ,
      source.mod_id,
      source.create_id,
      source.create_time  ,
      source.deleted,
      source.facility_acquired);

  log_pkg.write_log('finished populating investigation_type data');
END push_investigation_type;

--------------------------------------------------------------------------------

PROCEDURE push_facility_cycle IS
BEGIN
  log_pkg.write_log('populating facility_cycle data');

  merge INTO bl_facility_cycle target
  USING(
    SELECT *
    FROM facility_cycle
    WHERE (name, mod_time) NOT IN(
      SELECT name, mod_time FROM bl_facility_cycle
      )
      and deleted='N'
    ) source
  ON(target.name = source.name)
  WHEN matched THEN
    UPDATE SET
      start_date  = source.start_date  ,
      finish_date = source.finish_date ,
      description = source.description ,
      mod_time    = source.mod_time    ,
      mod_id      = source.mod_id,
      deleted     = source.deleted
  WHEN NOT matched THEN
    INSERT(
      name        ,
      start_date  ,
      finish_date ,
      description ,
      mod_time    ,
      mod_id,
      create_id,
      create_time,
      deleted,
      facility_acquired)
    VALUES(
      source.name        ,
      source.start_date  ,
      source.finish_date ,
      source.description ,
      source.mod_time    ,
      source.mod_id,
      source.create_id,
      source.create_time,
      source.deleted,
      source.facility_acquired);

  log_pkg.write_log('finished populating facility_cycle data');
END push_facility_cycle;

--------------------------------------------------------------------------------

PROCEDURE push_dataset_status IS
BEGIN
  log_pkg.write_log('populating dataset_status data');

  merge INTO bl_dataset_status target
  USING(
    SELECT *
    FROM dataset_status
    WHERE (name, mod_time) NOT IN(
      SELECT name, mod_time FROM bl_dataset_status
      )
    and deleted='N'
    ) source
  ON(target.name = source.name)
  WHEN matched THEN
    UPDATE SET
      description = source.description ,
      mod_time    = source.mod_time    ,
      mod_id      = source.mod_id,
            deleted     = source.deleted
  WHEN NOT matched THEN
    INSERT(
      name        ,
      description ,
      mod_time    ,
      mod_id,
      create_id,
      create_time,
      deleted,
      facility_acquired)
    VALUES(
      source.name        ,
      source.description ,
      source.mod_time    ,
      source.mod_id,
      source.create_id,
      source.create_time ,
      source.deleted,
      source.facility_acquired);

  log_pkg.write_log('finished populating dataset_status data');
END push_dataset_status;

--------------------------------------------------------------------------------

PROCEDURE push_dataset_type IS
BEGIN
  log_pkg.write_log('populating dataset_type data');

  merge INTO bl_dataset_type target
  USING(
    SELECT *
    FROM dataset_type
    WHERE (name, mod_time) NOT IN(
      SELECT name, mod_time FROM bl_dataset_type
      )
    and deleted='N'
    ) source
  ON(target.name = source.name)
  WHEN matched THEN
    UPDATE SET
      description = source.description ,
      mod_time    = source.mod_time    ,
      mod_id      = source.mod_id,
          deleted     = source.deleted
  WHEN NOT matched THEN
    INSERT(
      name        ,
      description ,
      mod_time    ,
      mod_id,
      create_id,
      create_time,
      deleted,
      facility_acquired)
    VALUES(
      source.name        ,
      source.description ,
      source.mod_time    ,
      source.mod_id,
      source.create_id,
      source.create_time   ,
      source.deleted,
      source.facility_acquired);

  log_pkg.write_log('finished populating dataset_type data');
END push_dataset_type;

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
BEGIN
  log_pkg.write_log('populating shift data');


  -- we cannot link to a shift record in the main ICAT database if the start or
  -- end date has been changed, so we delete and reinsert.

  DELETE FROM bl_shift;

  INSERT INTO bl_shift
  SELECT s.*
    FROM shift s, investigation i
    WHERE s.investigation_id = i.id
    AND i.instrument = p_instrument;

  log_pkg.write_log('finished populating shift data');
END push_shift;

--------------------------------------------------------------------------------

PROCEDURE push_facility_user(p_instrument IN investigation.instrument%TYPE) IS
BEGIN
  log_pkg.write_log('populating facility_user data');

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

PROCEDURE push_keyword(p_instrument IN investigation.instrument%TYPE) IS
BEGIN
  log_pkg.write_log('populating keyword data');

  -- we cannot link to a keyword record in the main ICAT database if the name
  -- has been changed, so we delete and reinsert.

  DELETE FROM bl_keyword;

  INSERT INTO bl_keyword
  SELECT k.*
    FROM keyword k, investigation i
    WHERE k.investigation_id = i.id
    AND i.instrument = p_instrument;

  log_pkg.write_log('finished populating keyword data');
END push_keyword;

--------------------------------------------------------------------------------

PROCEDURE push_publication(p_instrument IN investigation.instrument%TYPE) IS
BEGIN
  log_pkg.write_log('populating publication data');

  -- publications may be removed from the main ICAT database by virtue of the
  -- exp_abstract field being changed in Duodesk.  so we delete records with no
  -- match in ICATDLS before doing the merge.

  DELETE FROM bl_publication
    WHERE id NOT IN(
      SELECT id FROM publication);

  merge INTO bl_publication target
  USING(
    SELECT p.*
    FROM publication p, investigation i
    WHERE p.investigation_id = i.id
    AND (p.id, p.mod_time) NOT IN(
      SELECT id, mod_time FROM bl_publication
      )
    AND i.instrument = p_instrument
    and p.deleted='N'
    ) source
  ON(target.id = source.id)
  WHEN matched THEN
    UPDATE SET
      investigation_id = source.investigation_id ,
      full_reference   = source.full_reference   ,
      url              = source.url              ,
      repository_id    = source.repository_id    ,
      repository       = source.repository       ,
      mod_time         = source.mod_time         ,
      mod_id           = source.mod_id,
      deleted          = source.deleted
  WHEN NOT matched THEN
    INSERT(
      id               ,
      investigation_id ,
      full_reference   ,
      url              ,
      repository_id    ,
      repository       ,
      mod_time         ,
      mod_id,
      create_id,
      create_time,
      deleted,
      facility_acquired)
    VALUES(
      source.id               ,
      source.investigation_id ,
      source.full_reference   ,
      source.url              ,
      source.repository_id    ,
      source.repository       ,
      source.mod_time         ,
      source.mod_id,
      source.create_id,
      source.create_time  ,
      source.deleted,
      source.facility_acquired);

  log_pkg.write_log('finished populating publication data');
END push_publication;

--------------------------------------------------------------------------------

PROCEDURE push_sample(p_instrument IN investigation.instrument%TYPE) IS
BEGIN
  log_pkg.write_log('populating sample data');

  merge INTO bl_sample target
  USING(
    SELECT s.*
    FROM sample s, investigation i
    WHERE s.investigation_id = i.id
    AND (s.id, s.mod_time) NOT IN(
      SELECT id, mod_time FROM bl_sample
      )
    AND i.instrument = p_instrument
    and s.deleted='N'
    ) source
  ON(target.id = source.id)
  WHEN matched THEN
    UPDATE SET
      investigation_id   = source.investigation_id   ,
--testing: name = CASE p_instrument WHEN 'i22' THEN 'xx' ELSE source.name END,
      name               = source.name               ,
      instance           = source.instance           ,
      chemical_formula   = source.chemical_formula   ,
      safety_information = source.safety_information ,
      proposal_sample_id = source.proposal_sample_id ,
      mod_time           = source.mod_time           ,
      mod_id             = source.mod_id,
      deleted            = source.deleted
  WHEN NOT matched THEN
    INSERT(
      id                 ,
      investigation_id   ,
      name               ,
      instance           ,
      chemical_formula   ,
      safety_information ,
      proposal_sample_id ,
      mod_time           ,
      mod_id,
      create_id,
      create_time,
      deleted,
      facility_acquired)
    VALUES(
      source.id                 ,
      source.investigation_id   ,
      source.name               ,
      source.instance           ,
      source.chemical_formula   ,
      source.safety_information ,
      source.proposal_sample_id ,
      source.mod_time           ,
      source.mod_id,
      source.create_id,
      source.create_time  ,
      source.deleted,
      source.facility_acquired);

  log_pkg.write_log('finished populating sample data');
END push_sample;

--------------------------------------------------------------------------------

PROCEDURE push_dataset(p_instrument IN investigation.instrument%TYPE) IS
BEGIN
  log_pkg.write_log('populating dataset data');

  merge INTO bl_dataset target
  USING(
    SELECT d.*
    FROM dataset d, investigation i
    WHERE d.investigation_id = i.id
    AND (d.id, d.mod_time) NOT IN(
      SELECT id, mod_time FROM bl_dataset
      )
    AND i.instrument = p_instrument
    and d.deleted='N'
    ) source
  ON(target.id = source.id)
  WHEN matched THEN
    UPDATE SET
      sample_id        = source.sample_id        ,
      investigation_id = source.investigation_id ,
      name             = source.name             ,
      dataset_type     = source.dataset_type     ,
      dataset_status   = source.dataset_status   ,
      description      = source.description      ,
      mod_time         = source.mod_time         ,
      mod_id           = source.mod_id,
      deleted          = source.deleted
  WHEN NOT matched THEN
    INSERT(
      id               ,
      sample_id        ,
      investigation_id ,
      name             ,
      dataset_type     ,
      dataset_status   ,
      description      ,
      mod_time         ,
      mod_id,
      create_id,
      create_time,
      deleted,
      facility_acquired)
    VALUES(
      source.id               ,
      source.sample_id        ,
      source.investigation_id ,
      source.name             ,
      source.dataset_type     ,
      source.dataset_status   ,
      source.description      ,
      source.mod_time         ,
      source.mod_id,
      source.create_id,
      source.create_time ,
      source.deleted,
      source.facility_acquired);

  log_pkg.write_log('finished populating dataset data');
END push_dataset;

--------------------------------------------------------------------------------

PROCEDURE push_sample_parameter(p_instrument IN investigation.instrument%TYPE) IS
BEGIN
  log_pkg.write_log('populating sample_parameter data');

  merge INTO bl_sample_parameter target
  USING(
    SELECT sp.*
    FROM sample_parameter sp, sample s, investigation i
    WHERE sp.sample_id = s.id
    AND (sp.sample_id, sp.name, sp.units, sp.mod_time) NOT IN(
      SELECT sample_id, name, units, mod_time
      FROM bl_sample_parameter
      )
    AND s.investigation_id = i.id
    AND i.instrument = p_instrument
    and sp.deleted='N'
    ) source
  ON(target.sample_id = source.sample_id
     AND target.name = source.name
     AND target.units = source.units
     )
  WHEN matched THEN
    UPDATE SET
      string_value  = source.string_value  ,
      numeric_value = source.numeric_value ,
      error         = source.error         ,
      range_top     = source.range_top     ,
      range_bottom  = source.range_bottom  ,
      mod_time      = source.mod_time      ,
      mod_id        = source.mod_id,
      deleted       = source.deleted
  WHEN NOT matched THEN
    INSERT(
      sample_id     ,
      name          ,
      string_value  ,
      numeric_value ,
      units         ,
      error         ,
      range_top     ,
      range_bottom  ,
      mod_time      ,
      mod_id,
      create_id,
      create_time,
      deleted,
      facility_acquired)
    VALUES(
      source.sample_id     ,
      source.name          ,
      source.string_value  ,
      source.numeric_value ,
      source.units         ,
      source.error         ,
      source.range_top     ,
      source.range_bottom  ,
      source.mod_time      ,
      source.mod_id,
      source.create_id,
      source.create_time ,
      source.deleted,
      source.facility_acquired);

  log_pkg.write_log('finished populating sample_parameter data');
END push_sample_parameter;

--------------------------------------------------------------------------------

PROCEDURE push_applications IS
BEGIN
  log_pkg.write_log('populating applications data');

  merge INTO bl_applications target
  USING(SELECT * FROM applications WHERE deleted='N') source
  ON(target.app_code = source.app_code)
  WHEN matched THEN
    UPDATE SET
      app_name        = source.app_name,
      app_description = source.app_description,
      mod_time        = source.mod_time,
      mod_id          = source.mod_id
      WHEN NOT matched THEN
    INSERT(
      app_code,
      app_name,
      app_description,
      mod_time,
      mod_id,
      create_id,
      create_time,
      facility_acquired)
    VALUES(
      source.app_code,
      source.app_name,
      source.app_description,
      source.mod_time,
      source.mod_id,
      source.create_id,
      source.create_time,
      source.facility_acquired      );

  log_pkg.write_log('finished populating applications data');
END push_applications;

--------------------------------------------------------------------------------

PROCEDURE push_user_roles IS
BEGIN
  log_pkg.write_log('populating user_roles data');

  merge INTO bl_user_roles target
  USING(SELECT * FROM user_roles WHERE deleted='N') source
  ON(target.app_code = source.app_code
    AND target.username = source.username)
  WHEN matched THEN
    UPDATE SET
      role       = source.role,
      mod_time   = source.mod_time,
      mod_id     = source.mod_id
  WHEN NOT matched THEN
    INSERT(
      app_code,
      username,
      role,
      mod_time,
      mod_id,
      create_id,
      create_time,
      facility_acquired)
    VALUES(
      source.app_code,
      source.username,
      source.role,
      source.mod_time,
      source.mod_id,
      source.create_id,
      source.create_time,
      source.facility_acquired      );

  log_pkg.write_log('finished populating user_roles data');
END push_user_roles;

--------------------------------------------------------------------------------

PROCEDURE propagate_data IS

  x INTEGER;
  LV_LOCKNAME CONSTANT VARCHAR2(50) := 'POPULATE_BEAMLINES_PKG';--.propagate_data';
  lv_lockhandle VARCHAR2(100);

  CURSOR c_instruments IS
    SELECT instrument instrument, dblink
    FROM beamline_instrument;

BEGIN
  log_pkg.init;
  log_pkg.write_log('Propagating data to beamline databases');

  -- we're modifying synonyms within this package so have to serialise access
  Dbms_Lock.allocate_unique(LV_LOCKNAME,lv_lockhandle);
  x := Dbms_Lock.request(
    lockhandle        => lv_lockhandle,
    lockmode          => Dbms_Lock.X_MODE, -- exclusive
    timeout           => 0,
    release_on_commit => FALSE);

  IF x != 0 THEN
    Raise_Application_Error(
      package_locked_exnum,
      'Package is locked ('||To_Char(x)||') try later');
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
        push_instrument;
        push_parameter;
        push_investigation_type;
        push_facility_cycle;
        push_dataset_status;
        push_dataset_type;
        push_this_icat;

        -- application roles
        push_applications;
        push_user_roles;

        -- application data
        push_investigation(rec.instrument);
        push_shift(rec.instrument);
        push_facility_user(rec.instrument);
        push_investigator(rec.instrument);
        push_keyword(rec.instrument);
        push_publication(rec.instrument);
        push_sample(rec.instrument);
        push_dataset(rec.instrument);
        push_sample_parameter(rec.instrument);


        log_pkg.write_log('Propagation successful for instrument '||rec.instrument);
      EXCEPTION
        WHEN OTHERS THEN
          log_pkg.write_exception(
            'Propagation failed for instrument '||rec.instrument||Chr(10)||SQLERRM);
            ICATDLS33.email_problem(rec.instrument,SQLERRM);
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
  END IF;
EXCEPTION
  WHEN OTHERS THEN
    log_pkg.write_exception('Propagation failed: '||SQLERRM);
    ICATDLS33.email_problem('ICAT',SQLERRM);

    BEGIN
      x := Dbms_Lock.RELEASE(lv_lockhandle);
    EXCEPTION
      WHEN OTHERS THEN
        NULL;
    END;

    RAISE;
END propagate_data;

--------------------------------------------------------------------------------

END populate_beamlines_pkg;
/
