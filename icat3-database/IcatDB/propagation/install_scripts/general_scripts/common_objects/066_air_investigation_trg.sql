CREATE OR REPLACE TRIGGER air_investigation_trg
  AFTER INSERT  ON INVESTIGATION   FOR EACH ROW
DECLARE
BEGIN

FOR row IN (SELECT  federal_id FROM    FACILITY_INSTRUMENT_SCIENTIST WHERE   INSTRUMENT_NAME =:NEW.INSTRUMENT AND DELETED = 'N') LOOP
-- if :NEW.INSTRUMENT_NAME is null the select statment returns no rows

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
    'INVESTIGATION',
    :NEW.id,
     null,
     null,
         systimestamp,
         :new.mod_id,
         systimestamp,
         :new.mod_id,
         'Y',
        'N');

END LOOP;

END;
/

