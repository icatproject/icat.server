CREATE TABLE ACCESS_GROUP
(
ID NUMBER NOT NULL,
NAME VARCHAR2(255) NOT NULL,
DESCRIPTION VARCHAR2(4000),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE ACCESS_GROUP_DLP
(
ACCESS_GROUP_ID NUMBER NOT NULL,
DLP_ID NUMBER NOT NULL,
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE ACCESS_GROUP_ILP
(
ACCESS_GROUP_ID NUMBER NOT NULL,
ILP_ID NUMBER NOT NULL,
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE DATAFILE
(
ID NUMBER(38, 0) NOT NULL,
DATASET_ID NUMBER NOT NULL,
NAME VARCHAR2(255),
DESCRIPTION VARCHAR2(255),
DATAFILE_VERSION VARCHAR2(255),
DATAFILE_VERSION_COMMENT VARCHAR2(4000),
LOCATION VARCHAR2(4000),
DATAFILE_FORMAT VARCHAR2(255),
DATAFILE_FORMAT_VERSION VARCHAR2(255),
DATAFILE_CREATE_TIME DATE,
DATAFILE_MODIFY_TIME DATE,
FILE_SIZE NUMBER,
COMMAND VARCHAR2(4000),
CHECKSUM VARCHAR2(255),
SIGNATURE VARCHAR2(4000),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL

)
;

CREATE TABLE DATAFILE_FORMAT
(
NAME VARCHAR2(255) NOT NULL,
VERSION VARCHAR2(255) NOT NULL,
FORMAT_TYPE VARCHAR2(255),
DESCRIPTION VARCHAR2(4000),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE DATAFILE_PARAMETER
(
DATAFILE_ID NUMBER NOT NULL,
NAME VARCHAR2(255) NOT NULL,
UNITS VARCHAR2(255) NOT NULL,
STRING_VALUE VARCHAR2(4000),
NUMERIC_VALUE DOUBLE PRECISION,
RANGE_TOP VARCHAR2(255),
RANGE_BOTTOM VARCHAR2(255),
ERROR VARCHAR2(255),
DESCRIPTION VARCHAR2(4000),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE DATASET
(
ID NUMBER(38, 0) NOT NULL,
SAMPLE_ID NUMBER,
INVESTIGATION_ID NUMBER NOT NULL,
NAME VARCHAR2(255) NOT NULL,
DATASET_TYPE VARCHAR2(255) NOT NULL,
DATASET_STATUS VARCHAR2(255),
DESCRIPTION VARCHAR2(4000),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE DATASET_LEVEL_PERMISSION
(
ID NUMBER NOT NULL,
DATASET_ID NUMBER NOT NULL,
PRM_ADMIN NUMBER(1, 0) NOT NULL,
PRM_CREATE NUMBER(1, 0) NOT NULL,
PRM_READ NUMBER(1, 0) NOT NULL,
PRM_UPDATE NUMBER(1, 0) NOT NULL,
PRM_DELETE NUMBER(1, 0) NOT NULL,
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE DATASET_PARAMETER
(
DATASET_ID NUMBER NOT NULL,
NAME VARCHAR2(255) NOT NULL,
UNITS VARCHAR2(255) NOT NULL,
STRING_VALUE VARCHAR2(4000),
NUMERIC_VALUE DOUBLE PRECISION,
RANGE_TOP VARCHAR2(255),
RANGE_BOTTOM VARCHAR2(255),
ERROR VARCHAR2(255),
DESCRIPTION VARCHAR2(4000),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE DATASET_STATUS
(
NAME VARCHAR2(255) NOT NULL,
DESCRIPTION VARCHAR2(4000),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE DATASET_TYPE
(
NAME VARCHAR2(255) NOT NULL,
DESCRIPTION VARCHAR2(4000),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE FACILITY_CYCLE
(
NAME VARCHAR2(255) NOT NULL,
START_DATE TIMESTAMP(1),
FINISH_DATE TIMESTAMP(1),
DESCRIPTION VARCHAR2(4000),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE FACILITY_USER
(
FACILITY_USER_ID VARCHAR2(255) NOT NULL,
FEDERAL_ID VARCHAR2(255),
TITLE VARCHAR2(255),
INITIALS VARCHAR2(255),
FIRST_NAME VARCHAR2(255),
MIDDLE_NAME VARCHAR2(255),
LAST_NAME VARCHAR2(255),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE INSTRUMENT
(
NAME VARCHAR2(255) NOT NULL,
TYPE VARCHAR2(255),
DESCRIPTION VARCHAR2(4000),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE INVESTIGATION
(
ID NUMBER(38, 0) NOT NULL,
INV_NUMBER VARCHAR2(255) NOT NULL,
VISIT_ID VARCHAR2(255),
FACILITY_CYCLE VARCHAR2(255),
INSTRUMENT VARCHAR2(255),
TITLE VARCHAR2(255) NOT NULL,
INV_TYPE VARCHAR2(255) NOT NULL,
INV_ABSTRACT VARCHAR2(4000),
PREV_INV_NUMBER VARCHAR2(255),
BCAT_INV_STR VARCHAR2(255),
GRANT_ID NUMBER,
RELEASE_DATE TIMESTAMP(1),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE INVESTIGATION_LEVEL_PERMISSION
(
ID NUMBER NOT NULL,
INVESTIGATION_ID NUMBER NOT NULL,
PRM_ADMIN NUMBER(1, 0) NOT NULL,
PRM_FINE_GRAINED_ACCESS NUMBER(1, 0) NOT NULL,
PRM_CREATE NUMBER(1, 0) NOT NULL,
PRM_READ NUMBER(1, 0) NOT NULL,
PRM_UPDATE NUMBER(1, 0) NOT NULL,
PRM_DELETE NUMBER(1, 0) NOT NULL,
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE INVESTIGATION_TYPE
(
NAME VARCHAR2(255) NOT NULL,
DESCRIPTION VARCHAR2(4000),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE INVESTIGATOR
(
INVESTIGATION_ID NUMBER(38, 0) NOT NULL,
FACILITY_USER_ID VARCHAR2(255) NOT NULL,
ROLE VARCHAR2(255),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE KEYWORD
(
INVESTIGATION_ID NUMBER(38, 0) NOT NULL,
NAME VARCHAR2(255) NOT NULL,
MOD_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE PARAMETER
(
NAME VARCHAR2(255) NOT NULL,
UNITS VARCHAR2(255) NOT NULL,
UNITS_LONG_VERSION VARCHAR2(4000),
SEARCHABLE VARCHAR2(1) NOT NULL,
NUMERIC_VALUE VARCHAR2(1) NOT NULL,
NON_NUMERIC_VALUE_FORMAT VARCHAR2(255),
IS_SAMPLE_PARAMETER VARCHAR2(1) NOT NULL,
IS_DATASET_PARAMETER VARCHAR2(1) NOT NULL,
IS_DATAFILE_PARAMETER VARCHAR2(1) NOT NULL,
DESCRIPTION VARCHAR2(4000),
MOD_ID VARCHAR2(255) NOT NULL,
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE PUBLICATION
(
ID NUMBER NOT NULL,
INVESTIGATION_ID NUMBER(38, 0) NOT NULL,
FULL_REFERENCE VARCHAR2(4000) NOT NULL,
URL VARCHAR2(255),
REPOSITORY_ID VARCHAR(4000),
REPOSITORY VARCHAR2(255),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE RELATED_DATAFILES
(
SOURCE_DATAFILE_ID NUMBER NOT NULL,
DEST_DATAFILE_ID NUMBER NOT NULL,
RELATION VARCHAR2(255) NOT NULL,
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE SAMPLE
(
ID NUMBER(38, 0) NOT NULL,
INVESTIGATION_ID NUMBER NOT NULL,
NAME VARCHAR2(255) NOT NULL,
INSTANCE VARCHAR2(255),
CHEMICAL_FORMULA VARCHAR2(255),
SAFETY_INFORMATION VARCHAR2(4000) NOT NULL,
PROPOSAL_SAMPLE_ID NUMBER(38, 0),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE SAMPLE_PARAMETER
(
SAMPLE_ID NUMBER(38, 0) NOT NULL,
NAME VARCHAR2(255) NOT NULL,
UNITS VARCHAR2(255) NOT NULL,
STRING_VALUE VARCHAR2(4000),
NUMERIC_VALUE DOUBLE PRECISION,
ERROR VARCHAR2(255),
RANGE_TOP VARCHAR2(255),
RANGE_BOTTOM VARCHAR2(255),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE SHIFT
(
INVESTIGATION_ID NUMBER NOT NULL,
START_DATE TIMESTAMP(1) NOT NULL,
END_DATE TIMESTAMP(1) NOT NULL,
SHIFT_COMMENT VARCHAR2(4000),
MOD_ID VARCHAR2(255) NOT NULL,
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE SOFTWARE_VERSION
(
ID NUMBER(38, 0) NOT NULL,
NAME VARCHAR(4000),
SW_VERSION VARCHAR2(255),
FEATURES VARCHAR2(255),
DESCRIPTION VARCHAR2(255),
AUTHORS VARCHAR2(255),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE STUDY
(
ID NUMBER(38, 0) NOT NULL,
NAME VARCHAR2(255) NOT NULL,
PURPOSE VARCHAR2(4000),
STATUS VARCHAR2(255),
RELATED_MATERIAL VARCHAR2(4000),
STUDY_CREATION_DATE TIMESTAMP(1),
STUDY_MANAGER NUMBER,
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE STUDY_INVESTIGATION
(
STUDY_ID NUMBER NOT NULL,
INVESTIGATION_ID NUMBER NOT NULL,
INVESTIGATION_VISIT_ID VARCHAR2(255) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE STUDY_STATUS
(
NAME VARCHAR2(255) NOT NULL,
DESCRIPTION VARCHAR2(4000) NOT NULL,
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE TOPIC
(
ID NUMBER(38, 0) NOT NULL,
NAME VARCHAR2(255),
PARENT_ID NUMBER(38, 0),
TOPIC_LEVEL NUMBER(38, 0),
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE TOPIC_LIST
(
INVESTIGATION_ID NUMBER(38, 0) NOT NULL,
TOPIC_ID NUMBER(38, 0) NOT NULL,
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

CREATE TABLE USER_ACCESS_GROUP
(
USER_ID VARCHAR2(255) NOT NULL,
ACCESS_GROUP_ID NUMBER NOT NULL,
MOD_TIME TIMESTAMP(1) NOT NULL,
CREATE_TIME TIMESTAMP(1) NOT NULL,
MOD_ID VARCHAR2(255) NOT NULL,
CREATE_ID VARCHAR2(255) NOT NULL,
DELETED VARCHAR2(1) NOT NULL
)
;

ALTER TABLE ACCESS_GROUP 
ADD CONSTRAINT ACCESS_GROUP_PK PRIMARY KEY
(
ID
)
 ENABLE
;

ALTER TABLE ACCESS_GROUP 
ADD CONSTRAINT ACCESS_GROUP_UK1 UNIQUE
(
NAME
)
 ENABLE
;

ALTER TABLE ACCESS_GROUP_DLP 
ADD CONSTRAINT ACCESS_GROUP_DLP_PK PRIMARY KEY
(
ACCESS_GROUP_ID,
DLP_ID
)
 ENABLE
;

ALTER TABLE ACCESS_GROUP_ILP 
ADD CONSTRAINT ACCESS_GROUP_PERMISSIONS_PK PRIMARY KEY
(
ACCESS_GROUP_ID,
ILP_ID
)
 ENABLE
;

ALTER TABLE DATAFILE 
ADD CONSTRAINT PK_DF PRIMARY KEY
(
ID
)
 ENABLE
;

ALTER TABLE DATAFILE_FORMAT 
ADD PRIMARY KEY
(
NAME,
VERSION
)
 ENABLE
;

ALTER TABLE DATAFILE_PARAMETER 
ADD CONSTRAINT PK_DP PRIMARY KEY
(
DATAFILE_ID,
NAME,
UNITS
)
 ENABLE
;

ALTER TABLE DATASET 
ADD CONSTRAINT PK_DS PRIMARY KEY
(
ID
)
 ENABLE
;

ALTER TABLE DATASET 
ADD CONSTRAINT DATASET_UK1 UNIQUE
(
INVESTIGATION_ID,
SAMPLE_ID,
DATASET_TYPE,
NAME
)
 ENABLE
;

ALTER TABLE DATASET_LEVEL_PERMISSION 
ADD CONSTRAINT DATASET_PERMISSIONS_PK PRIMARY KEY
(
ID
)
 ENABLE
;

ALTER TABLE DATASET_PARAMETER 
ADD CONSTRAINT PK_CP PRIMARY KEY 
(
DATASET_ID,
NAME,
UNITS
)
 ENABLE
;

ALTER TABLE DATASET_STATUS 
ADD PRIMARY KEY
(
NAME
)
 ENABLE
;

ALTER TABLE DATASET_TYPE 
ADD PRIMARY KEY
(
NAME
)
 ENABLE
;

ALTER TABLE FACILITY_CYCLE 
ADD PRIMARY KEY
(
NAME
)
 ENABLE
;

ALTER TABLE FACILITY_USER 
ADD CONSTRAINT FACILITY_USER_PK PRIMARY KEY
(
FACILITY_USER_ID
)
 ENABLE
;

ALTER TABLE INSTRUMENT 
ADD CONSTRAINT INSTRUMENT_TYPE_PK PRIMARY KEY
(
NAME
)
 ENABLE
;

ALTER TABLE INVESTIGATION 
ADD CONSTRAINT PK_I PRIMARY KEY
(
ID
)
 ENABLE
;

ALTER TABLE INVESTIGATION 
ADD CONSTRAINT INVESTIGATION_UK2 UNIQUE
(
VISIT_ID,
INV_NUMBER,
FACILITY_CYCLE,
INSTRUMENT
)
 ENABLE
;

ALTER TABLE INVESTIGATION_LEVEL_PERMISSION 
ADD CONSTRAINT PERMISSION_PK PRIMARY KEY
(
ID
)
 ENABLE
;

ALTER TABLE INVESTIGATION_TYPE 
ADD PRIMARY KEY
(
NAME
)
 ENABLE
;

ALTER TABLE INVESTIGATOR 
ADD CONSTRAINT PK_INVR PRIMARY KEY
(
INVESTIGATION_ID,
FACILITY_USER_ID
)
 ENABLE
;

ALTER TABLE PARAMETER 
ADD CONSTRAINT PARAMETER_PK PRIMARY KEY
(
NAME,
UNITS
)
 ENABLE
;

ALTER TABLE PUBLICATION 
ADD CONSTRAINT PK_P PRIMARY KEY
(
ID
)
 ENABLE
;

ALTER TABLE RELATED_DATAFILES 
ADD CONSTRAINT PK_FGDF PRIMARY KEY
(
SOURCE_DATAFILE_ID,
DEST_DATAFILE_ID
)
 ENABLE
;

ALTER TABLE SAMPLE 
ADD CONSTRAINT PK_S PRIMARY KEY
(
ID
)
 ENABLE
;

ALTER TABLE SAMPLE 
ADD CONSTRAINT SAMPLE_UK1 UNIQUE
(
INVESTIGATION_ID,
NAME,
INSTANCE
)
 ENABLE
;

ALTER TABLE SAMPLE 
ADD CONSTRAINT SAMPLE_UK2 UNIQUE
(
ID,
INVESTIGATION_ID
)
 ENABLE
;

ALTER TABLE SAMPLE_PARAMETER 
ADD CONSTRAINT PK_SP PRIMARY KEY
(
SAMPLE_ID,
NAME,
UNITS
)
 ENABLE
;

ALTER TABLE SHIFT 
ADD CONSTRAINT SHIFT_PK PRIMARY KEY
(
INVESTIGATION_ID,
START_DATE,
END_DATE
)
 ENABLE
;

ALTER TABLE SOFTWARE_VERSION 
ADD PRIMARY KEY
(
ID
)
 ENABLE
;

ALTER TABLE STUDY 
ADD CONSTRAINT PK_STU PRIMARY KEY
(
ID
)
 ENABLE
;

ALTER TABLE STUDY 
ADD CONSTRAINT STUDY_UK1 UNIQUE
(
NAME
)
 ENABLE
;

ALTER TABLE STUDY_INVESTIGATION 
ADD CONSTRAINT STUD_INVESTIGATION_PK PRIMARY KEY
(
STUDY_ID,
INVESTIGATION_ID
)
 ENABLE
;

ALTER TABLE STUDY_STATUS 
ADD PRIMARY KEY
(
NAME
)
 ENABLE
;

ALTER TABLE TOPIC 
ADD CONSTRAINT PK_T PRIMARY KEY
(
ID
)
 ENABLE
;

ALTER TABLE TOPIC_LIST 
ADD CONSTRAINT PK_TL PRIMARY KEY
(
INVESTIGATION_ID,
TOPIC_ID
)
 ENABLE
;

ALTER TABLE USER_ACCESS_GROUP 
ADD CONSTRAINT USER_ACCESS_GROUP_PK PRIMARY KEY
(
USER_ID,
ACCESS_GROUP_ID
)
 ENABLE
;

ALTER TABLE ACCESS_GROUP_DLP 
ADD CONSTRAINT ACCESS_GROUP_DLP_ACCESS_G_FK1 FOREIGN KEY
(
ACCESS_GROUP_ID
)
REFERENCES ACCESS_GROUP
(
ID
) ENABLE
;

ALTER TABLE ACCESS_GROUP_DLP 
ADD CONSTRAINT ACCESS_GROUP_DLP_DATASET__FK1 FOREIGN KEY
(
DLP_ID
)
REFERENCES DATASET_LEVEL_PERMISSION
(
ID
) ENABLE
;

ALTER TABLE ACCESS_GROUP_ILP 
ADD CONSTRAINT ACCESS_GROUP_PERMISSIONS__FK1 FOREIGN KEY
(
ACCESS_GROUP_ID
)
REFERENCES ACCESS_GROUP
(
ID
) ENABLE
;

ALTER TABLE ACCESS_GROUP_ILP 
ADD CONSTRAINT ACCESS_GROUP_PERMISSION_P_FK1 FOREIGN KEY
(
ILP_ID
)
REFERENCES INVESTIGATION_LEVEL_PERMISSION
(
ID
) ENABLE
;

ALTER TABLE DATAFILE 
ADD CONSTRAINT DATAFILE_DATASET_FK1 FOREIGN KEY
(
DATASET_ID
)
REFERENCES DATASET
(
ID
) ENABLE
;

ALTER TABLE DATAFILE 
ADD CONSTRAINT DATAFILE_DATAFILE_FORMAT_FK1 FOREIGN KEY
(
DATAFILE_FORMAT,
DATAFILE_FORMAT_VERSION
)
REFERENCES DATAFILE_FORMAT
(
NAME,
VERSION
) ENABLE
;

ALTER TABLE DATAFILE_PARAMETER 
ADD CONSTRAINT FK_DP_DF FOREIGN KEY
(
DATAFILE_ID
)
REFERENCES DATAFILE
(
ID
)
ON DELETE CASCADE ENABLE
;

ALTER TABLE DATAFILE_PARAMETER 
ADD CONSTRAINT DATAFILE_PARAMETER_PARAME_FK1 FOREIGN KEY
(
NAME,
UNITS
)
REFERENCES PARAMETER
(
NAME,
UNITS
) ENABLE
;

ALTER TABLE DATASET 
ADD CONSTRAINT DATASET_SAMPLE_FK1 FOREIGN KEY
(
SAMPLE_ID,
INVESTIGATION_ID
)
REFERENCES SAMPLE
(
ID,
INVESTIGATION_ID
) ENABLE
;

ALTER TABLE DATASET 
ADD CONSTRAINT DATASET_DATASET_TYPE_FK1 FOREIGN KEY
(
DATASET_TYPE
)
REFERENCES DATASET_TYPE
(
NAME
) ENABLE
;

ALTER TABLE DATASET 
ADD CONSTRAINT DATASET_DATASET_STATUS_FK1 FOREIGN KEY
(
DATASET_STATUS
)
REFERENCES DATASET_STATUS
(
NAME
) ENABLE
;

ALTER TABLE DATASET 
ADD CONSTRAINT DATASET_INVESTIGATION_FK1 FOREIGN KEY
(
INVESTIGATION_ID
)
REFERENCES INVESTIGATION
(
ID
) ENABLE
;

ALTER TABLE DATASET_LEVEL_PERMISSION 
ADD CONSTRAINT DATASET_PERMISSIONS_DATAS_FK1 FOREIGN KEY
(
DATASET_ID
)
REFERENCES DATASET
(
ID
) ENABLE
;

ALTER TABLE DATASET_PARAMETER 
ADD CONSTRAINT FK_CP_C FOREIGN KEY
(
DATASET_ID
)
REFERENCES DATASET
(
ID
)
ON DELETE CASCADE ENABLE
;

ALTER TABLE DATASET_PARAMETER 
ADD CONSTRAINT DATASET_PARAMETER_PARAMET_FK1 FOREIGN KEY
(
NAME,
UNITS
)
REFERENCES PARAMETER
(
NAME,
UNITS
) ENABLE
;

ALTER TABLE INVESTIGATION 
ADD CONSTRAINT INVESTIGATION_INVESTIGATI_FK1 FOREIGN KEY
(
INV_TYPE
)
REFERENCES INVESTIGATION_TYPE
(
NAME
) ENABLE
;

ALTER TABLE INVESTIGATION 
ADD CONSTRAINT INVESTIGATION_FACILITY_CY_FK1 FOREIGN KEY
(
FACILITY_CYCLE
)
REFERENCES FACILITY_CYCLE
(
NAME
) ENABLE
;

ALTER TABLE INVESTIGATION 
ADD CONSTRAINT INVESTIGATION_INSTRUMENT_FK1 FOREIGN KEY
(
INSTRUMENT
)
REFERENCES INSTRUMENT
(
NAME
) ENABLE
;

ALTER TABLE INVESTIGATION_LEVEL_PERMISSION 
ADD CONSTRAINT PERMISSION_INVESTIGATION_FK1 FOREIGN KEY
(
INVESTIGATION_ID
)
REFERENCES INVESTIGATION
(
ID
) ENABLE
;

ALTER TABLE INVESTIGATOR 
ADD CONSTRAINT INVESTIGATOR_FACILITY_USER_FK1 FOREIGN KEY
(
FACILITY_USER_ID
)
REFERENCES FACILITY_USER
(
FACILITY_USER_ID
) ENABLE
;

ALTER TABLE INVESTIGATOR 
ADD CONSTRAINT FK_I_I FOREIGN KEY
(
INVESTIGATION_ID
)
REFERENCES INVESTIGATION
(
ID
)
ON DELETE CASCADE ENABLE
;

ALTER TABLE KEYWORD 
ADD CONSTRAINT FK_K_STU FOREIGN KEY
(
INVESTIGATION_ID
)
REFERENCES INVESTIGATION
(
ID
)
ON DELETE CASCADE ENABLE
;

ALTER TABLE PUBLICATION 
ADD CONSTRAINT FK_P_I FOREIGN KEY
(
INVESTIGATION_ID
)
REFERENCES INVESTIGATION
(
ID
)
ON DELETE CASCADE ENABLE
;

ALTER TABLE RELATED_DATAFILES 
ADD CONSTRAINT FK_FGDF_DF FOREIGN KEY
(
SOURCE_DATAFILE_ID
)
REFERENCES DATAFILE
(
ID
) ENABLE
;

ALTER TABLE RELATED_DATAFILES 
ADD CONSTRAINT RELATED_DATAFILES_DATAFIL_FK1 FOREIGN KEY
(
DEST_DATAFILE_ID
)
REFERENCES DATAFILE
(
ID
) ENABLE
;

ALTER TABLE SAMPLE 
ADD CONSTRAINT FK_S_I FOREIGN KEY
(
INVESTIGATION_ID
)
REFERENCES INVESTIGATION
(
ID
)
ON DELETE CASCADE ENABLE
;

ALTER TABLE SAMPLE_PARAMETER 
ADD CONSTRAINT FK_S FOREIGN KEY
(
SAMPLE_ID
)
REFERENCES SAMPLE
(
ID
)
ON DELETE CASCADE ENABLE
;

ALTER TABLE SAMPLE_PARAMETER 
ADD CONSTRAINT SAMPLE_PARAMETER_PARAMETE_FK1 FOREIGN KEY
(
NAME,
UNITS
)
REFERENCES PARAMETER
(
NAME,
UNITS
) ENABLE
;

ALTER TABLE SHIFT 
ADD CONSTRAINT SHIFT_INVESTIGATION_FK1 FOREIGN KEY
(
INVESTIGATION_ID
)
REFERENCES INVESTIGATION
(
ID
) ENABLE
;

ALTER TABLE STUDY 
ADD CONSTRAINT STUDY_STUDY_STATUS_FK1 FOREIGN KEY
(
STATUS
)
REFERENCES STUDY_STATUS
(
NAME
) ENABLE
;

ALTER TABLE STUDY_INVESTIGATION 
ADD CONSTRAINT STUD_INVESTIGATION_STUDY_FK1 FOREIGN KEY
(
STUDY_ID
)
REFERENCES STUDY
(
ID
) ENABLE
;

ALTER TABLE STUDY_INVESTIGATION 
ADD CONSTRAINT STUD_INVESTIGATION_INVEST_FK1 FOREIGN KEY
(
STUDY_ID
)
REFERENCES INVESTIGATION
(
ID
) ENABLE
;

ALTER TABLE TOPIC_LIST 
ADD CONSTRAINT FK_TL_STU FOREIGN KEY
(
INVESTIGATION_ID
)
REFERENCES INVESTIGATION
(
ID
) ENABLE
;

ALTER TABLE TOPIC_LIST 
ADD CONSTRAINT FK_TL_T FOREIGN KEY
(
TOPIC_ID
)
REFERENCES TOPIC
(
ID
) ENABLE
;

ALTER TABLE USER_ACCESS_GROUP 
ADD CONSTRAINT USER_ACCESS_GROUP_ACCESS__FK1 FOREIGN KEY
(
ACCESS_GROUP_ID
)
REFERENCES ACCESS_GROUP
(
ID
) ENABLE
;

CREATE INDEX DATAFILE_INDEX1 ON DATAFILE (dataset_id);

CREATE INDEX KEYWORD_INDEX1 ON KEYWORD (INVESTIGATION_ID);

COMMENT ON TABLE ACCESS_GROUP IS 'This is to support group permissions.  There is no direct user access for permissions - if a single user is needed he/she is mapped onto one group.  e.g. if you have an experiment you could have 2 groups for it - 1 for the PI and 1 for the rest   e.g. toms_experiment - with admin priv and toms_experiment_collab - without admin priv'
;

COMMENT ON TABLE DATAFILE IS 'this use to contain the uA hours column which gave the ISIS scientists an idea of how much data was collected. this has been moved to being a dataset parameter specific to ISIS.'
;

COMMENT ON TABLE DATASET_PARAMETER IS 'This can be used for  1) holding instrument configuration parameters relevent for this dataset (ISIS note- instrument configuration usually only happen once per cycle so this might attach to datasets associated with the calibration investigation) '
;

COMMENT ON TABLE FACILITY_USER IS 'Associates facility user ids with their federal ids'
;

COMMENT ON TABLE INVESTIGATOR IS 'A user can be in more than one investigation and and investigation can have more than one user. This is achieved by having facility_user_id and investigation_id as a composite primary key.'
;

COMMENT ON TABLE PARAMETER IS 'This table contains information about the valid parameters that can be used to describe samples, datasets and datafiles.  puposefully this table has been setup such that a paramater is only stored using one unit systems (all conversion should occur elsewhere).'
;

COMMENT ON TABLE PUBLICATION IS 'This can support pointing to supporting publications in any arbitary repository'
;

COMMENT ON TABLE RELATED_DATAFILES IS 'There can only be one relationship between any two specified files.'
;

COMMENT ON TABLE SAMPLE IS 'This table stores the sample information for both the experiment (abstract) and the datasets (instances) e.g. in the case of the abstraction the instance column would be set to null.  ---------  Samples are often subsituted if the stated samples instances do not produce good results. Scientists ususally have reserve samples or other samples which they want to try out (this usually happens 50% of the time as an estimate on ISIS) this can happen due to a variety of reasons-  1) the sample instances not producing good results 2) the experiment going better than expected and having time left to look at a reserve sample  This is not usually a problem if the sample safety characteristics are the same - usually the will just substitute the sample however if they are  1) have very different sample safety  2) are a totally different sample and they person is attempting to queue jump by analysing a particularly ''hot'' (from a research perspective) compound   then a station scientist must be asked/invovled  1) as can happen - sample can can explode and the people going in to clear up need to know if these are for example radioactive 2) this is queue jumping and can be attempted often but is considered to be unfair play and is actively stopped'
;

COMMENT ON TABLE SHIFT IS 'need by applications where icat will be on the beam line such that a match between who is logged in and what experiment they are performing can be accurately acertained - more operational than archive feature.'
;

COMMENT ON TABLE STUDY IS 'Study now used to aggregate investigations'
;

COMMENT ON COLUMN ACCESS_GROUP.MOD_ID IS 'last modification user_id'
;

COMMENT ON COLUMN DATAFILE.DATAFILE_VERSION IS 'e.g. a param divided by 4 and every calculation in the file was wrong so you correct this and then generate a new version of the file.  These types of errors could happen in the beginning - give people the chance to update the datafile metadata but this would necessitate the datafile.  Another case could be where the datafile points to the wrong sample (derived from the dataset) - this will require re-versioning such that people can compare there versions with that in the database.  This should be rare but could happen due to the above reasons. '
;

COMMENT ON COLUMN DATAFILE.DATAFILE_VERSION_COMMENT IS 'why there was a new version of the comment and what has changed - can inform the user to see if they really wanto update their files - i.e. was the change relevent for them.'
;

COMMENT ON COLUMN DATAFILE.LOCATION IS 'The location of the file - this could be an SRB URI/L for example. This should be the full qualified path and not relative.'
;

COMMENT ON COLUMN DATAFILE.DATAFILE_FORMAT IS 'format of the file e.g. NeXus e.g. the MIME type'
;

COMMENT ON COLUMN DATAFILE.DATAFILE_CREATE_TIME IS 'can search for all the files which were modified after they were created if you do not have a reference date. Note for new version this would create a new record in the datafile table the link from investigation would be cut but a link in related_datafiles should show you the progeny.'
;

COMMENT ON COLUMN DATAFILE.DATAFILE_MODIFY_TIME IS 'can search for the files that are last modified since you have downloaded a set.'
;

COMMENT ON COLUMN DATAFILE.FILE_SIZE IS 'this is the size in bytes (not on disk) but actual size. this column should be a parameter but it is needed often and checking with some systems for file sizes can take a lot of time'
;

COMMENT ON COLUMN DATAFILE.COMMAND IS 'stores the command line used to create this files.  It is here and not in related datafiles as there are instances where you would not reference to the source file locally.  As capturing the command is able to be made automatic but determining the source file is more difficult as one cannot necessarily automatically determine its provenance.'
;

COMMENT ON COLUMN DATAFILE.CHECKSUM IS 'for fixity'
;

COMMENT ON COLUMN DATAFILE.SIGNATURE IS 'for fixity'
;

COMMENT ON COLUMN DATASET.SAMPLE_ID IS 'this can be null as calibration datasets will not have a sample'
;

COMMENT ON COLUMN DATASET.NAME IS 'the basis of this should be e.g. the run_number for isis'
;

COMMENT ON COLUMN DATASET.DATASET_TYPE IS 'if Investigation.inv_type == EXPERIMENT or Investigation.inv_type == CALIBRATION  then dataset_type could be any of the following- 1.    PRE_EXPERIMENT_DATA 2.    DETECTOR_CALIBRATION (one detector) 3.    EXPERIMENT_CALIBRATION (with a calibration sample) 4.    EXPERIMENT_RAW 5.    LASER_SHOT (CLF specific) 6.    LASER_DIAGNOSTICS (CLF specific) 7.    TARGERT_DATA 8.    SIMULATION 9.    ANALYSIS '
;

COMMENT ON COLUMN DATASET.DATASET_STATUS IS 'empty ongoing (e.g. clf waiting for the glass plate to be analysed) complete'
;

COMMENT ON COLUMN DATASET.DESCRIPTION IS 'most of the time this will be empty.  ------------  However a description of the RAW data should/could be filled by GDA e.g. important parameters (e.g. temp might change a few degrees but the angle of the sample might be very important so that this is a place to highlight such facts) e.g. this could be used to describe how the data was ''cut''. E.g. in CLF they cut by shot - i.e. one dataset per shot.  -----------  also when dataset_type is pre_experiment_data this could be really important to describe why certain files have been added and what their purpost is.'
;

COMMENT ON COLUMN DATASET_LEVEL_PERMISSION.PRM_CREATE IS 'can the user create datafiles in this dataset'
;

COMMENT ON COLUMN DATASET_LEVEL_PERMISSION.PRM_READ IS 'can the user download the data in the dataset or make a copy e.g. for inclusion in another dataset'
;

COMMENT ON COLUMN DATASET_LEVEL_PERMISSION.PRM_UPDATE IS 'can the user update existing data files'
;

COMMENT ON COLUMN DATASET_LEVEL_PERMISSION.PRM_DELETE IS 'can the user delete existing files from this dataset'
;

COMMENT ON COLUMN DATASET_PARAMETER.UNITS IS 'SI name of the units need reference to a lookup table UNIT.  Use N/A when no Unit applies.'
;

COMMENT ON COLUMN DATASET_PARAMETER.RANGE_TOP IS 'work being done to see if these are necessary'
;

COMMENT ON COLUMN DATASET_PARAMETER.RANGE_BOTTOM IS 'work being done to see if these are necessary'
;

COMMENT ON COLUMN DATASET_PARAMETER.ERROR IS 'work being done to see if these are necessary'
;

COMMENT ON COLUMN DATASET_PARAMETER.DESCRIPTION IS 'from where the parameter was taken/extracted and a description of for example what type of temperature this is (temperature of sample, cold source, heating source, detector)  And/or a description of the category of error being recorded.'
;

COMMENT ON COLUMN FACILITY_CYCLE.DESCRIPTION IS 'should the facility go here or should we have another column'
;

COMMENT ON COLUMN FACILITY_USER.FACILITY_USER_ID IS 'facility_user_id should be self consistent across the database and usually refers to a user numbering system which is valid inside a particular facility in the case of ISIS and DLS this is a number based system but it need not be.'
;

COMMENT ON COLUMN FACILITY_USER.FEDERAL_ID IS 'federal_id should be self consistent across the database and usually refers to a user numbering system which is valid across an (virtual or real) organisation e.g. fed-id at CCLRC or DN schemes. Thus it is generalised as a string as this can accomodate a numbering system also.'
;

COMMENT ON COLUMN FACILITY_USER.TITLE IS 'taken from user database or cdr'
;

COMMENT ON COLUMN FACILITY_USER.INITIALS IS 'from user db or cdr'
;

COMMENT ON COLUMN FACILITY_USER.FIRST_NAME IS 'from user db or cdr'
;

COMMENT ON COLUMN FACILITY_USER.MIDDLE_NAME IS 'from user db or cdr'
;

COMMENT ON COLUMN FACILITY_USER.LAST_NAME IS 'from user db or cdr'
;

COMMENT ON COLUMN INVESTIGATION.INV_NUMBER IS 'this is the experiment number e.g. the rb number from isis. In the case ISIS this is usually the proposal number but proposals can be split into separate experiments each with their own number - in which case it actuall maps to the approved proposal number'
;

COMMENT ON COLUMN INVESTIGATION.VISIT_ID IS 'Sometimes (e.g. in the case of DLS) investigation are consortium based i.e. carried out by a range of people from different institutions who manage their own time slot on the instrument. So in effect the investigation is multi-faceted where different groups should not have access to the data from other groups. In the case of ISIS and CLF this can be set to null or a constant value'
;

COMMENT ON COLUMN INVESTIGATION.FACILITY_CYCLE IS 'facilities cycle - where this is defined as the time between two maintenance periods of the light/neutron/laser sources. this is usually a name - e.g. year and number'
;

COMMENT ON COLUMN INVESTIGATION.INSTRUMENT IS 'multimple instruments per approved proposal will now be different investigations with different instrument inside icat - this was requested by noth DLS and ISIS - rather than attach instrument at the dataset level. This is due in part to common searches being in terms of instrument and experiment number as oppose to title and drilling down..'
;

COMMENT ON COLUMN INVESTIGATION.TITLE IS 'the proposal is usually the source of the investigation.title however this could be modified by the user to reflect more accurately the real experiment being performed as oppose to the one specified in the proposal'
;

COMMENT ON COLUMN INVESTIGATION.INV_TYPE IS 'should match the investigation_type.name values - e.g. experiment or calibration  as calibrations can be adhoc not just at the beginning of a cycle but also when an instrument if fixed then these are  modelled as separate investigations. The linkage to these by experimental investigations is done via the range of time_stamps on the collected datafiles in that particular experiment.'
;

COMMENT ON COLUMN INVESTIGATION.INV_ABSTRACT IS 'description of the experiment e.g. based on the proposal'
;

COMMENT ON COLUMN INVESTIGATION.PREV_INV_NUMBER IS 'experiment number of a preceding and related experiment. e.g. in a chain of such experiments.'
;

COMMENT ON COLUMN INVESTIGATION.GRANT_ID IS 'funding grant identification this is taken from the proposal system. in addition the person and their role in the funding institution need to be know to identify completely where the money for this investigation has come from e.g. EPSRC and the person who signed it off'
;

COMMENT ON COLUMN INVESTIGATION.RELEASE_DATE IS 'This is the date in the future that the raw data will be made available to other users (or publically available) - this is informed by the data policy of the facility (e.g. 1 year or 2 years).'
;

COMMENT ON COLUMN INVESTIGATION_LEVEL_PERMISSION.PRM_ADMIN IS '0 for false and 1 for true.  True - the user/group have the right to grant authority to other users and groups or groups and users.  False - they don''t have this authority'
;

COMMENT ON COLUMN INVESTIGATION_LEVEL_PERMISSION.PRM_CREATE IS '0 for false, 1 for true'
;

COMMENT ON COLUMN INVESTIGATOR.FACILITY_USER_ID IS 'facility_user_id should be self consistent across the database and usually refers to a user numbering system which is valid inside a particular facility in the case of ISIS and DLS this is a number based system but it need not be.'
;

COMMENT ON COLUMN INVESTIGATOR.ROLE IS 'For example-  Principal Investigator, Collaborator'
;

COMMENT ON COLUMN PARAMETER.UNITS IS 'the unit for this parameter. note any given parameters can be at multiple units this is needed for added flexibility from different data sources e.g. user office systems and values collected at proposal time.  Use N/A when no Unit applies.'
;

COMMENT ON COLUMN PARAMETER.UNITS_LONG_VERSION IS 'long verson of units - unit_abreviation used in practice.'
;

COMMENT ON COLUMN PARAMETER.SEARCHABLE IS 'Y or y - for allowing searches using this parameter and anything else for N including null'
;

COMMENT ON COLUMN PARAMETER.NUMERIC_VALUE IS 'Y or y denote that the value of the parameter is a number - anything else denotes that it is a string'
;

COMMENT ON COLUMN PARAMETER.NON_NUMERIC_VALUE_FORMAT IS 'where the value is a string this allows that value to be documented according to the rules or a regular expression.'
;

COMMENT ON COLUMN PARAMETER.IS_SAMPLE_PARAMETER IS 'Y or y denote that the parameter is relevent for association with samples'
;

COMMENT ON COLUMN PARAMETER.IS_DATASET_PARAMETER IS 'Y or y denote that the parameter is relevent for association with datasets'
;

COMMENT ON COLUMN PARAMETER.IS_DATAFILE_PARAMETER IS 'Y or y denote that the parameter is relevent for association with datafiles'
;

COMMENT ON COLUMN PARAMETER.DESCRIPTION IS 'this describes the parameter'
;

COMMENT ON COLUMN PUBLICATION.REPOSITORY_ID IS 'this does no necessarily have to be a number'
;

COMMENT ON COLUMN PUBLICATION.REPOSITORY IS 'The name of the repository e.g. CCLRC ePubs'
;

COMMENT ON COLUMN RELATED_DATAFILES.RELATION IS 'e.g. the nature of the relation ship - i.e. <dest_file> a <relation> of <source_file>  where relation could be for example - subset newer version reduced used the configuration of - e.g. sample enviroment (configuration, temperature,pressure)   '
;

COMMENT ON COLUMN SAMPLE.NAME IS 'descriptive name of the sample'
;

COMMENT ON COLUMN SAMPLE.INSTANCE IS 'this is the instance of the sample e.g. 1,2,3 etc.  this will be null in the case where the abstract sample is being described'
;

COMMENT ON COLUMN SAMPLE.CHEMICAL_FORMULA IS 'this is the chemical formula of the sample. note this can be null in cases when there is a complex layered target or where the chemical formula is unknown or only partially known. or when trying to study the interface between two solids - in these cases more support maybe needed if deemded necessary.'
;

COMMENT ON COLUMN SAMPLE.SAFETY_INFORMATION IS 'This field holds sample safety / sample hazard information'
;

COMMENT ON COLUMN SAMPLE.PROPOSAL_SAMPLE_ID IS 'A copy of the sample_id from the database from which this record was imported.  This lets us propoagate changes made to a sample''s child tables, such as  sample_parameter, from the original database to this one.'
;

COMMENT ON COLUMN STUDY.ID IS 'primary key field'
;

COMMENT ON COLUMN STUDY.NAME IS 'This should be unique in the table'
;

COMMENT ON COLUMN STUDY.PURPOSE IS 'the reason for aggregating this particular set of investigations i.e. the aggregation criteria used'
;

COMMENT ON COLUMN STUDY.STATUS IS 'this could be ongoing or complete as there are additional investigations planned in the future which will be applicable to this study'
;

COMMENT ON COLUMN STUDY.RELATED_MATERIAL IS 'e.g. related studies in different facility databases - same or similar sample investigated at a different facility (e.g. DLS or CLF if work done at ISIS). To allow the connection of different sources of relevent information - at the moment this is freetext but this may become more structured in the future'
;

COMMENT ON COLUMN STUDY.STUDY_CREATION_DATE IS 'date the study was created'
;

COMMENT ON COLUMN STUDY.STUDY_MANAGER IS 'This should map to the USER.ID table.column, as the user who creates the study need not be an investigator but should be known to be a registered user of the facility.  This also acts as the authorisation to modify the study and links to associated investigations.'
;

COMMENT ON COLUMN STUDY.MOD_TIME IS 'time record modified'
;

COMMENT ON COLUMN STUDY.MOD_ID IS 'user.id of last modifying user'
;

COMMENT ON COLUMN STUDY_STATUS.DESCRIPTION IS 'a description of what the study status actually means'
;

COMMENT ON COLUMN USER_ACCESS_GROUP.USER_ID IS 'see investigator.user_id for explanation'
;