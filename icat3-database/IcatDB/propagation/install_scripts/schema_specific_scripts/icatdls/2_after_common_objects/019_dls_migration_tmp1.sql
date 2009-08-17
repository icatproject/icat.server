REM Unfortunately this global temp table is required in the migration package.

CREATE GLOBAL TEMPORARY TABLE dls_migration_tmp1(
  id NUMBER(38) NOT NULL,
  usernumber NUMBER(11) NOT NULL)
;

comment on table dls_migration_tmp1 is 'Used in Duodesk-Diamond migration';
