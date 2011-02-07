alter table sample_parameter disable constraint sample_parameter_paramete_fk1;

alter table sample drop constraint sample_uk1;

alter table dataset_parameter disable constraint dataset_parameter_paramet_fk1;

alter table sample_parameter disable constraint sample_parameter_paramete_fk1;

alter table datafile_parameter disable constraint datafile_parameter_parame_fk1; 

alter table parameter disable primary key;

alter table DATAFILE disable constraint DATAFILE_DATAFILE_FORMAT_FK1;                        

alter table DATAFILE disable constraint DATAFILE_DATASET_FK1;                                

alter table DATASET disable constraint DATASET_DATASET_STATUS_FK1;                           

alter table DATASET disable constraint DATASET_DATASET_TYPE_FK1;                             

alter table DATASET disable constraint DATASET_INVESTIGATION_FK1;                            

alter table DATASET disable constraint DATASET_SAMPLE_FK1;                                   

alter table FACILITY_INSTRUMENT_SCIENTIST disable constraint FACILITY_INSTRUMENT_SCIEN_FK1;  

alter table DATASET_PARAMETER disable constraint FK_CP_C;                                    

alter table DATAFILE_PARAMETER disable constraint FK_DP_DF;                                  

alter table RELATED_DATAFILES disable constraint FK_FGDF_DF;                                 

alter table INVESTIGATOR disable constraint FK_I_I;                                          

alter table KEYWORD disable constraint FK_K_STU;                                             

alter table PUBLICATION disable constraint FK_P_I;                                           

alter table SAMPLE_PARAMETER disable constraint FK_S;                                        

alter table SAMPLE disable constraint FK_S_I;                                                

alter table TOPIC_LIST disable constraint FK_TL_STU;                                         

alter table TOPIC_LIST disable constraint FK_TL_T;                                           

alter table ICAT_AUTHORISATION disable constraint ICAT_AUTHORISATION_ICAT_R_FK1;             

alter table INVESTIGATOR disable constraint INVESTIGATOR_FACILITY_USER_FK1;                  

alter table RELATED_DATAFILES disable constraint RELATED_DATAFILES_DATAFIL_FK1;              

alter table SHIFT disable constraint SHIFT_INVESTIGATION_FK1;                                

alter table STUDY disable constraint STUDY_STUDY_STATUS_FK1;                                 

alter table STUDY_INVESTIGATION disable constraint STUD_INVESTIGATION_INVEST_FK1;            

alter table STUDY_INVESTIGATION disable constraint STUD_INVESTIGATION_STUDY_FK1;             

alter table USER_ROLES disable constraint USER_ROLES_APPLICATIONS_FK1;

alter table INVESTIGATION enable constraint INVESTIGATION_INSTRUMENT_FK1;                   

alter table INVESTIGATION enable constraint INVESTIGATION_INVESTIGATI_FK1;                  

alter table INVESTIGATION enable constraint INVESTIGATION_THIS_ICAT_FK1; 
CREATE INDEX DATAFILE_CREATETIME_IDX ON DATAFILE
(CREATE_TIME)
LOGGING
NOPARALLEL;

CREATE INDEX DATAFILE_INDEX_LOCATION ON DATAFILE
(LOCATION)
LOGGING
NOPARALLEL;

CREATE INDEX IDX_PUBLIC_INV_REF ON PUBLICATION
(INVESTIGATION_ID, FULL_REFERENCE)
LOGGING
NOPARALLEL;

CREATE INDEX INVESTIGATION_INDEX2 ON INVESTIGATION
(VISIT_ID, INSTRUMENT)
LOGGING
NOPARALLEL;

CREATE INDEX RUN_NUM_IDX ON LOG_TABLE
(RUN_NUM)
LOGGING
NOPARALLEL;


CREATE OR REPLACE TRIGGER "AIR_INVESTIGATION_TRG" 
  AFTER INSERT  ON INVESTIGATION   FOR EACH ROW
DECLARE
BEGIN

FOR row IN (SELECT  federal_id FROM    FACILITY_INSTRUMENT_SCIENTIST WHERE   INSTRUMENT_NAME =:NEW.INSTRUMENT AND DELETED = 'N') LOOP
-- if :NEW.INSTRUMENT_NAME is null the select statment returns no rows

MERGE INTO icat_authorisation D USING (
SELECT 'INVESTIGATION' ELEMENT_TYPE, :NEW.id ELEMENT_ID, row.federal_id USER_ID, null PARENT_ELEMENT_TYPE, null PARENT_ELEMENT_ID FROM DUAL) S
ON (D.ELEMENT_TYPE=S.ELEMENT_TYPE and  D.ELEMENT_ID=S.ELEMENT_ID  and D.USER_ID=S.USER_ID 
    AND (D.PARENT_ELEMENT_TYPE=S.PARENT_ELEMENT_TYPE or D.PARENT_ELEMENT_TYPE is null) 
    AND (D.PARENT_ELEMENT_ID=S.PARENT_ELEMENT_ID or D.PARENT_ELEMENT_ID is null))  
WHEN MATCHED THEN
UPDATE SET D.mod_time=sysdate,D.mod_id=:new.mod_id, D.FACILITY_ACQUIRED='Y'
WHEN NOT MATCHED THEN
INSERT (
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
SHOW ERRORS;
