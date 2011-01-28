CREATE OR REPLACE PACKAGE set_deleted_pkg AS

/*

This package contains procedures for setting the "deleted" column on child
tables when a parent record is marked for deletion.

*/


PROCEDURE do_investigation(p_id IN investigation.id%TYPE);
PROCEDURE do_sample(p_id IN sample.id%TYPE);
PROCEDURE do_datafile(p_id IN datafile.id%TYPE);
--PROCEDURE do_dataset_level_permission(p_id IN dataset_level_permission.id%TYPE);
--PROCEDURE do_investigation_level_perm(p_id IN investigation_level_permission.id%TYPE);

END set_deleted_pkg;
/


CREATE OR REPLACE PACKAGE BODY set_deleted_pkg AS

PROCEDURE do_investigation(p_id IN investigation.id%TYPE) IS
BEGIN
  UPDATE investigation SET deleted = 'Y' WHERE deleted = 'N' AND id = p_id;

  UPDATE keyword SET deleted = 'Y' WHERE deleted = 'N' AND investigation_id = p_id;

  UPDATE shift SET deleted = 'Y' WHERE deleted = 'N' AND investigation_id = p_id;

  UPDATE topic_list SET deleted = 'Y' WHERE deleted = 'N' AND investigation_id = p_id;

  UPDATE publication SET deleted = 'Y' WHERE deleted = 'N' AND investigation_id = p_id;

  UPDATE study_investigation SET deleted = 'Y' WHERE deleted = 'N' AND investigation_id = p_id;

  UPDATE sample SET deleted = 'Y' WHERE deleted = 'N' AND investigation_id = p_id;

  UPDATE sample_parameter SET deleted = 'Y'
    WHERE deleted = 'N' AND sample_id IN(SELECT id FROM sample WHERE investigation_id = p_id);

  UPDATE dataset SET deleted = 'Y' WHERE deleted = 'N' AND investigation_id = p_id;

  UPDATE dataset_parameter SET deleted = 'Y'
    WHERE deleted = 'N' AND dataset_id IN(SELECT id FROM dataset WHERE investigation_id = p_id);

  /* UPDATE dataset_level_permission SET deleted = 'Y'
    WHERE deleted = 'N' AND dataset_id IN(SELECT id FROM dataset WHERE investigation_id = p_id); */

 /* UPDATE access_group_dlp SET deleted = 'Y'
    WHERE deleted = 'N' AND access_group_id IN(
      SELECT id FROM dataset_level_permission WHERE dataset_id IN(
        SELECT id FROM dataset WHERE investigation_id = p_id));  */

  UPDATE datafile SET deleted = 'Y'
    WHERE deleted = 'N' AND dataset_id IN(SELECT id FROM dataset WHERE investigation_id = p_id);

  UPDATE datafile_parameter SET deleted = 'Y'
    WHERE deleted = 'N'
    AND datafile_id IN(
      SELECT df.id FROM datafile df, dataset ds
      WHERE df.dataset_id = ds.id AND ds.investigation_id = p_id);

  UPDATE related_datafiles SET deleted = 'Y'
    WHERE deleted = 'N'
    AND (
      source_datafile_id IN(
        SELECT df.id FROM datafile df, dataset ds
        WHERE df.dataset_id = ds.id AND ds.investigation_id = p_id)
      OR dest_datafile_id IN(
        SELECT df.id FROM datafile df, dataset ds
        WHERE df.dataset_id = ds.id AND ds.investigation_id = p_id)
    );

/*  UPDATE investigation_level_permission SET deleted = 'Y' WHERE deleted = 'N' AND investigation_id = p_id;  */

/*  UPDATE access_group_ilp SET deleted = 'Y'
    WHERE deleted = 'N' AND ilp_id IN(SELECT id FROM investigation_level_permission WHERE investigation_id = p_id);  */

END do_investigation;

--------------------------------------------------------------------------------

PROCEDURE do_sample(p_id IN sample.id%TYPE) IS
BEGIN
  UPDATE sample SET deleted = 'Y' WHERE deleted = 'N' AND id = p_id;

  UPDATE sample_parameter SET deleted = 'Y' WHERE deleted = 'N' AND sample_id = p_id;

  UPDATE dataset SET deleted = 'Y' WHERE deleted = 'N' AND sample_id = p_id;

  UPDATE dataset_parameter SET deleted = 'Y'
    WHERE deleted = 'N' AND dataset_id IN(SELECT id FROM dataset WHERE sample_id = p_id);

/*  UPDATE dataset_level_permission SET deleted = 'Y'
    WHERE deleted = 'N' AND dataset_id IN(SELECT id FROM dataset WHERE sample_id = p_id);  */

 /* UPDATE access_group_dlp SET deleted = 'Y'
    WHERE deleted = 'N' AND access_group_id IN(
      SELECT id FROM dataset_level_permission WHERE dataset_id IN(
        SELECT id FROM dataset WHERE investigation_id = p_id));  */

  UPDATE datafile SET deleted = 'Y'
    WHERE deleted = 'N' AND dataset_id IN(SELECT id FROM dataset WHERE sample_id = p_id);

  UPDATE datafile_parameter SET deleted = 'Y'
    WHERE deleted = 'N'
    AND datafile_id IN(
      SELECT df.id FROM datafile df, dataset ds
      WHERE df.dataset_id = ds.id AND ds.sample_id = p_id);

  UPDATE related_datafiles SET deleted = 'Y'
    WHERE deleted = 'N'
    AND (
      source_datafile_id IN(
        SELECT df.id FROM datafile df, dataset ds
        WHERE df.dataset_id = ds.id AND ds.sample_id = p_id)
      OR dest_datafile_id IN(
        SELECT df.id FROM datafile df, dataset ds
        WHERE df.dataset_id = ds.id AND ds.sample_id = p_id)
    );
END do_sample;

--------------------------------------------------------------------------------

PROCEDURE do_dataset(p_id IN dataset.id%TYPE) IS
BEGIN
  UPDATE dataset SET deleted = 'Y' WHERE deleted = 'N' AND id = p_id;

  UPDATE dataset_parameter SET deleted = 'Y' WHERE deleted = 'N' AND dataset_id = p_id;

 /* UPDATE dataset_level_permission SET deleted = 'Y' WHERE deleted = 'N' AND dataset_id = p_id;  */

  /*UPDATE access_group_dlp SET deleted = 'Y'
    WHERE deleted = 'N' AND access_group_id IN(
      SELECT id FROM dataset_level_permission WHERE dataset_id = p_id);  */

  UPDATE datafile SET deleted = 'Y' WHERE deleted = 'N' AND dataset_id = p_id;

  UPDATE datafile_parameter SET deleted = 'Y'
    WHERE deleted = 'N' AND datafile_id IN(
      SELECT id FROM datafile df WHERE dataset_id = p_id);

  UPDATE related_datafiles SET deleted = 'Y'
    WHERE deleted = 'N'
    AND (
      source_datafile_id IN(
        SELECT id FROM datafile df WHERE dataset_id = p_id)
      OR dest_datafile_id IN(
        SELECT id FROM datafile df WHERE dataset_id = p_id)
    );
END do_dataset;

--------------------------------------------------------------------------------

PROCEDURE do_datafile(p_id IN datafile.id%TYPE) IS
BEGIN
  UPDATE datafile SET deleted = 'Y' WHERE deleted = 'N' AND id = p_id;

  UPDATE datafile_parameter SET deleted = 'Y' WHERE deleted = 'N' AND datafile_id = p_id;

  UPDATE related_datafiles SET deleted = 'Y'
    WHERE deleted = 'N'
    AND (source_datafile_id = p_id OR dest_datafile_id = p_id);
END do_datafile;

--------------------------------------------------------------------------------

/* PROCEDURE do_dataset_level_permission(p_id IN dataset_level_permission.id%TYPE) IS
BEGIN
  UPDATE access_group_dlp SET deleted = 'Y' WHERE deleted = 'N' AND access_group_id = p_id;
END do_dataset_level_permission;

--------------------------------------------------------------------------------

PROCEDURE do_investigation_level_perm(p_id IN investigation_level_permission.id%TYPE) IS
BEGIN
  UPDATE access_group_ilp SET deleted = 'Y' WHERE deleted = 'N' AND ilp_id = p_id;
END do_investigation_level_perm;  */


END set_deleted_pkg;
/

