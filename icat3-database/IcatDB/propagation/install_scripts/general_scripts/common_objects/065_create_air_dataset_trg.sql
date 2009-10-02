
CREATE OR REPLACE TRIGGER air_dataset_trg
  AFTER INSERT  ON DATASET   FOR EACH ROW
DECLARE


BEGIN


FOR row IN (SELECT federal_id FROM    FACILITY_INSTRUMENT_SCIENTIST fis, investigation i
            where fis.INSTRUMENT_NAME = i.instrument
              and   i.id = :NEW.INVESTIGATION_ID
              and  i.instrument is not null and federal_id is not null) LOOP

INSERT INTO icat_authorisation
(
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
          row.federal_id,
          'ICAT_ADMIN',
    'DATASET',
    :NEW.id,
     'INVESTIGATION',
     :NEW.INVESTIGATION_ID,
         systimestamp,
         :new.mod_id,
         systimestamp,
         :new.mod_id,
         'Y',
        'N');

END LOOP;
END;
/

