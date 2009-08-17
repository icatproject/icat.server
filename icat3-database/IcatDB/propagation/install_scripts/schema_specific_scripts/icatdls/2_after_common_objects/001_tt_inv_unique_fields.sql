REM Types used in icatdls_batch_migration_pkg to record the investigations
REM which could not be propagated

PROMPT Creating TYPE to represent the unique columns in the INVESTIGATION table

CREATE OR REPLACE TYPE tr_inv_unique_fields AS object(
-- mirrors the unique columns on the investigations table
  inv_number VARCHAR2(255),
  instrument VARCHAR2(255),
  visit_id VARCHAR2(255),
  facility_cycle VARCHAR2(255));
/

CREATE TYPE tt_inv_unique_fields AS TABLE OF tr_inv_unique_fields;
/