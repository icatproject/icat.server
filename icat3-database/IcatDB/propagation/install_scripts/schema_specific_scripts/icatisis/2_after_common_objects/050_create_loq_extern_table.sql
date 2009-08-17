CREATE TABLE extern_loq_data(
  start_date   varchar2(50),
  rb_no        VARCHAR2(50),
  runs_from    VARCHAR2(50),
  runs_to      VARCHAR2(50),
  company      VARCHAR2(200),
  company_contacts  VARCHAR2(2000),
  other_contacts    varchar2(500),
  instruments_contacts varchar2(500)
)
ORGANIZATION EXTERNAL (
  TYPE ORACLE_LOADER
  DEFAULT DIRECTORY external_tables
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY NEWLINE
    BADFILE external_tables:'loq_data.bad'
    LOGFILE external_tables:'loq_data.log'
    FIELDS TERMINATED BY 0X'09' RTrim
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS (
      start_date,
      rb_no,
      runs_from,
      runs_to,
      company,
      company_contacts,
      other_contacts,
      instruments_contacts
    )
  )
  LOCATION ('loq_data.tsv')
)
REJECT LIMIT 0;
