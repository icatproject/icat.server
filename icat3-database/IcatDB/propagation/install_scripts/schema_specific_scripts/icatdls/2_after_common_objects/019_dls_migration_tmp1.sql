REM Unfortunately this global temp table is required in the migration package.


drop table "DLS_MIGRATION_TMP1"; 

CREATE TABLE DLS_MIGRATION_TMP1
(
  RUN_NAME    VARCHAR2(10 BYTE)                 NOT NULL,
  ID          NUMBER(38)                        NOT NULL,
  USERNUMBER  NUMBER(11)                        NOT NULL
);


comment on table dls_migration_tmp1 is 'Used in Duodesk-Diamond migration';
