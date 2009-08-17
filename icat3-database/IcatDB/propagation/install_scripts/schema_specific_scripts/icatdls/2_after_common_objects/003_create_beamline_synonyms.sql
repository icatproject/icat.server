PROMPT creating synonyms to enable the migration package to compile...

declare
PROCEDURE create_synonyms IS
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

    EXECUTE IMMEDIATE(
      'Create Synonym bl_'||lv_syn_name||' For '||p_table);
  END cre_syn;
BEGIN

  cre_syn('DATAFILE');
  cre_syn('DATAFILE_PARAMETER');
  cre_syn('DATASET_LEVEL_PERMISSION');
  cre_syn('DATASET_PARAMETER');
  cre_syn('ICAT_AUTHORISATION');
  cre_syn('RELATED_DATAFILES');
  cre_syn('SHIFT');
  cre_syn('STUDY');
  cre_syn('STUDY_INVESTIGATION');
  cre_syn('TOPIC');
  cre_syn('TOPIC_LIST');
  cre_syn('ICAT_ROLE');
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
END create_synonyms;

BEGIN
  create_synonyms;
END;
/
