PROMPT Populate create_id fields

/*

This is specific to DLS
we'll need a separate script for each installation type which has the phase 1
schema installed.

Populate the create_id columns with an initial value before
setting them as 'NOT NULL'

*/


-- lookup_tables
UPDATE datafile_format SET create_id = 'FROM SPREADSHEET'
  WHERE (name, version)
    IN(SELECT Lower(name), version FROM extern_datafile_format WHERE Upper(dls) = 'Y');
UPDATE datafile_format SET create_id = 'FROM PROPAGATION'
  WHERE create_id IS NULL;

UPDATE dataset_status SET create_id = 'FROM SPREADSHEET'
  WHERE name IN(SELECT Lower(name) FROM extern_dataset_status WHERE Upper(dls) = 'Y');
UPDATE dataset_status SET create_id = 'FROM PROPAGATION'
  WHERE create_id IS NULL;

UPDATE dataset_type SET create_id = 'FROM SPREADSHEET'
  WHERE name IN(SELECT Lower(name) FROM extern_dataset_type WHERE Upper(dls) = 'Y');
UPDATE dataset_type SET create_id = 'FROM PROPAGATION'
  WHERE create_id IS NULL;

UPDATE facility_cycle SET create_id = 'FROM SPREADSHEET'
  WHERE name IN(SELECT Lower(name) FROM extern_facility_cycle WHERE Upper(dls) = 'Y');
UPDATE facility_cycle SET create_id = 'FROM PROPAGATION'
  WHERE create_id IS NULL;

UPDATE instrument SET create_id = 'FROM SPREADSHEET'
  WHERE name IN(SELECT Lower(name) FROM extern_instrument WHERE Upper(dls) = 'Y');
UPDATE instrument SET create_id = 'FROM PROPAGATION'
  WHERE create_id IS NULL;

UPDATE investigation_type SET create_id = 'FROM SPREADSHEET'
  WHERE name IN(SELECT Lower(name) FROM extern_investigation_type WHERE Upper(dls) = 'Y');
UPDATE investigation_type SET create_id = 'FROM PROPAGATION'
  WHERE create_id IS NULL;

UPDATE parameter SET create_id = 'FROM SPREADSHEET'
  WHERE (name, units)
    IN(SELECT Lower(name), Nvl(units,'N/A') FROM extern_parameter_list WHERE Upper(dls) = 'Y');
UPDATE parameter SET create_id = 'FROM PROPAGATION'
  WHERE create_id IS NULL;

UPDATE study_status SET create_id = 'FROM SPREADSHEET'
  WHERE name IN(SELECT Lower(name) FROM extern_study_status WHERE Upper(dls) = 'Y');
UPDATE study_status SET create_id = 'FROM PROPAGATION'
  WHERE create_id IS NULL;



-- populated only by processes other than the Propagation
UPDATE datafile SET create_id = mod_id;
UPDATE datafile_parameter SET create_id = mod_id;
UPDATE dataset SET create_id = mod_id;
UPDATE dataset_parameter SET create_id = mod_id;


-- populated by the propagation and by the Sample Editor application.
-- records enetered via the app will not have a value for 'proposal_sample_id'
UPDATE sample SET create_id =
  CASE WHEN proposal_sample_id IS NULL
    THEN mod_id
    ELSE 'FROM PROPAGATION'
    END;

-- tables not touched by any process or only touched by the propagation
UPDATE access_group SET create_id = 'FROM PROPAGATION';
UPDATE access_group_dlp SET create_id = 'FROM PROPAGATION';
UPDATE access_group_ilp SET create_id = 'FROM PROPAGATION';
UPDATE dataset_level_permission SET create_id = 'FROM PROPAGATION';
UPDATE facility_user SET create_id = 'FROM PROPAGATION';
UPDATE investigation SET create_id = 'FROM PROPAGATION';
UPDATE investigation_level_permission SET create_id = 'FROM PROPAGATION';
UPDATE investigator SET create_id = 'FROM PROPAGATION';
UPDATE keyword SET create_id = 'FROM PROPAGATION';
UPDATE publication SET create_id = 'FROM PROPAGATION';
UPDATE related_datafiles SET create_id = 'FROM PROPAGATION';
UPDATE sample_parameter SET create_id = 'FROM PROPAGATION';
UPDATE shift SET create_id = 'FROM PROPAGATION';
UPDATE software_version SET create_id = 'FROM PROPAGATION';
UPDATE study SET create_id = 'FROM PROPAGATION';
UPDATE study_investigation SET create_id = 'FROM PROPAGATION';
UPDATE topic SET create_id = 'FROM PROPAGATION';
UPDATE topic_list SET create_id = 'FROM PROPAGATION';
UPDATE user_access_group SET create_id = 'FROM PROPAGATION';
