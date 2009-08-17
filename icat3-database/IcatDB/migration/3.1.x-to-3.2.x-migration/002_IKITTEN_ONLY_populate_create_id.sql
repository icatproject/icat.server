PROMPT Populate create_id fields

/*

This is specific to IKitten.  Set everything to 'FROM PROPAGATION' and have
the beamline population package update the rows next time it runs.

We'll need a separate script for each installation type which has the phase 1
schema installed.

Populate the create_id columns with an initial value before
setting them as 'NOT NULL'

*/


-- lookup_tables
UPDATE datafile_format SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE dataset_status SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE dataset_type SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE facility_cycle SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE instrument SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE investigation_type SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE parameter SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE study_status SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;




-- populated only by processes other than the Propagation
UPDATE datafile SET create_id = mod_id, mod_time = systimestamp;
UPDATE datafile_parameter SET create_id = mod_id, mod_time = systimestamp;
UPDATE dataset SET create_id = mod_id, mod_time = systimestamp;
UPDATE dataset_parameter SET create_id = mod_id, mod_time = systimestamp;


UPDATE sample SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE access_group SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE access_group_dlp SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE access_group_ilp SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE dataset_level_permission SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE facility_user SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE investigation SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE investigation_level_permission SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE investigator SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE keyword SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE publication SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE related_datafiles SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE sample_parameter SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE shift SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE software_version SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE study SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE study_investigation SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE topic SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE topic_list SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
UPDATE user_access_group SET create_id = 'FROM PROPAGATION', mod_time = systimestamp;
