REM James Healy, November 2006
REM
REM Script to create external tables for loading excel lookup data.
REM This is a generic script to be run for all ICAT version 3 schemas.
REM
REM The directory EXTERNAL_TABLES should exist before this script is run.


CREATE TABLE extern_parameter_list(
  clf                 VARCHAR2(5),
  dls                 VARCHAR2(5),
  isis                VARCHAR2(5),
  name                VARCHAR2(255),
  units               VARCHAR2(255),
  units_long_version  VARCHAR2(4000),
    SEARCHABLE                VARCHAR2(1),
    NUMERIC_VALUE             VARCHAR2(1),
  NON_NUMERIC_VALUE_FORMAT  VARCHAR2(255),
  IS_SAMPLE_PARAMETER       VARCHAR2(1),
  IS_DATASET_PARAMETER      VARCHAR2(1),
  IS_DATAFILE_PARAMETER     VARCHAR2(1),
  DESCRIPTION               VARCHAR2(4000),
  VERIFIED                  VARCHAR2(1)
)
ORGANIZATION EXTERNAL (
  TYPE ORACLE_LOADER
  DEFAULT DIRECTORY external_tables
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY NEWLINE
    BADFILE external_tables:'parameter_list.bad'
    LOGFILE external_tables:'parameter_list.log'
    FIELDS TERMINATED BY 0X'09' RTrim
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS (
      clf,
      dls,
      isis,
      name,
      units,
      units_long_version CHAR(4000),
      searchable,
      numeric_value,
      non_numeric_value_format,
      IS_SAMPLE_PARAMETER,
      IS_DATASET_PARAMETER,
      IS_DATAFILE_PARAMETER,
      description CHAR(4000),
      verified
    )
  )
  LOCATION ('parameter_list.tsv')
)
REJECT LIMIT 0;

CREATE TABLE extern_study_status(
  clf          VARCHAR2(5),
  dls          VARCHAR2(5),
  isis         VARCHAR2(5),
  name         VARCHAR2(255),
  description  VARCHAR2(4000),
  sequence_number VARCHAR2(255)
)
ORGANIZATION EXTERNAL (
  TYPE ORACLE_LOADER
  DEFAULT DIRECTORY external_tables
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY NEWLINE
    BADFILE external_tables:'study_status.bad'
    LOGFILE external_tables:'study_status.log'
    FIELDS TERMINATED BY 0X'09' RTrim
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS (
      clf,
      dls,
      isis,
      name,
      description CHAR(4000),
      sequence_number
    )
  )
  LOCATION ('study_status.tsv')
)
REJECT LIMIT 0;


CREATE TABLE extern_investigation_type(
  clf          VARCHAR2(5),
  dls          VARCHAR2(5),
  isis         VARCHAR2(5),
  name         VARCHAR2(255),
  description  VARCHAR2(4000),
  sequence_number VARCHAR2(255)
)
ORGANIZATION EXTERNAL (
  TYPE ORACLE_LOADER
  DEFAULT DIRECTORY external_tables
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY NEWLINE
    BADFILE external_tables:'investigation_type.bad'
    LOGFILE external_tables:'investigation_type.log'
    FIELDS TERMINATED BY 0X'09' RTrim
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS (
      clf,
      dls,
      isis,
      name,
      description CHAR(4000),
      sequence_number
    )
  )
  LOCATION ('investigation_type.tsv')
)
REJECT LIMIT 0;


CREATE TABLE extern_facility_cycle(
  clf           VARCHAR2(5),
  dls           VARCHAR2(5),
  isis          VARCHAR2(5),
  name          VARCHAR2(255),
  start_date    DATE,
  finish_date   DATE,
  description   VARCHAR2(4000),
  sequence_number VARCHAR2(255)
)
ORGANIZATION EXTERNAL (
  TYPE ORACLE_LOADER
  DEFAULT DIRECTORY external_tables
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY NEWLINE
    BADFILE external_tables:'facility_cycle.bad'
    LOGFILE external_tables:'facility_cycle.log'
    FIELDS TERMINATED BY 0X'09' RTrim
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS (
      clf,
      dls,
      isis,
      name,
      start_date DATE 'YYYY-MM-DD',
      finish_date DATE 'YYYY-MM-DD',
      description CHAR(4000),
      sequence_number
    )
  )
  LOCATION ('facility_cycle.tsv')
)
REJECT LIMIT 0;


CREATE TABLE extern_instrument(
  clf          VARCHAR2(50),
  dls          VARCHAR2(50),
  isis         VARCHAR2(50),
  name         VARCHAR2(255),
  short_name   VARCHAR2(255),
  type         VARCHAR2(4000),
  description  VARCHAR2(4000),
  sequence_number varchar2(4000)
)
ORGANIZATION EXTERNAL (
  TYPE ORACLE_LOADER
  DEFAULT DIRECTORY external_tables
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY NEWLINE
    BADFILE external_tables:'instrument.bad'
    LOGFILE external_tables:'instrument.log'
    FIELDS TERMINATED BY 0X'09' RTrim
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS (
      clf,
      dls,
      isis,
      name,
      short_name,
      type  CHAR(4000),
      description  CHAR(4000),
      sequence_number CHAR(4000)
    )
  )
  LOCATION ('instrument.tsv')
)
REJECT LIMIT 0;


CREATE TABLE extern_dataset_type(
  clf          VARCHAR2(5),
  dls          VARCHAR2(5),
  isis         VARCHAR2(5),
  name         VARCHAR2(255),
  description  VARCHAR2(4000),
  sequence_number  VARCHAR2(255)
)
ORGANIZATION EXTERNAL (
  TYPE ORACLE_LOADER
  DEFAULT DIRECTORY external_tables
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY NEWLINE
    BADFILE external_tables:'dataset_type.bad'
    LOGFILE external_tables:'dataset_type.log'
    FIELDS TERMINATED BY 0X'09' RTrim
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS (
      clf,
      dls,
      isis,
      name,
      description CHAR(4000),
      sequence_number
    )
  )
  LOCATION ('dataset_type.tsv')
)
REJECT LIMIT 0;


CREATE TABLE extern_dataset_status(
  clf          VARCHAR2(5),
  dls          VARCHAR2(5),
  isis         VARCHAR2(5),
  name         VARCHAR2(255),
  description  VARCHAR2(4000),
  sequence_number  VARCHAR2(255)
)
ORGANIZATION EXTERNAL (
  TYPE ORACLE_LOADER
  DEFAULT DIRECTORY external_tables
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY NEWLINE
    BADFILE external_tables:'dataset_status.bad'
    LOGFILE external_tables:'dataset_status.log'
    FIELDS TERMINATED BY 0X'09' RTrim
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS (
      clf,
      dls,
      isis,
      name,
      description CHAR(4000),
      sequence_number
    )
  )
  LOCATION ('dataset_status.tsv')
)
REJECT LIMIT 0;


CREATE TABLE extern_datafile_format(
  clf          VARCHAR2(5),
  dls          VARCHAR2(5),
  isis         VARCHAR2(5),
  name         VARCHAR2(255),
  version      VARCHAR2(255),
  format_type  VARCHAR2(255),
  description  VARCHAR2(4000),
  sequence_number  varchar2(255)
)
ORGANIZATION EXTERNAL (
  TYPE ORACLE_LOADER
  DEFAULT DIRECTORY external_tables
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY NEWLINE
    BADFILE external_tables:'datafile_format.bad'
    LOGFILE external_tables:'datafile_format.log'
    FIELDS TERMINATED BY 0X'09' RTrim
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS (
      clf,
      dls,
      isis,
      name,
      version,
      format_type,
      description CHAR(4000),
      sequence_number
    )
  )
  LOCATION ('datafile_format.tsv')
)
REJECT LIMIT 0;

CREATE TABLE extern_icat_role(
  clf                 VARCHAR2(5),
  dls                 VARCHAR2(5),
  isis                VARCHAR2(5),
 ROLE                       VARCHAR2(255),
 ACTION_INSERT                VARCHAR2(5),
 ACTION_INSERT_WEIGHT       VARCHAR2(5),
 ACTION_SELECT              VARCHAR2(5),
 ACTION_SELECT_WEIGHT      VARCHAR2(5),
 ACTION_DOWNLOAD            VARCHAR2(5),
 ACTION_DOWNLOAD_WEIGHT     VARCHAR2(5),
 ACTION_UPDATE              VARCHAR2(5),
 ACTION_UPDATE_WEIGHT      VARCHAR2(5),
 ACTION_DELETE            VARCHAR2(5),
 ACTION_DELETE_WEIGHT     VARCHAR2(5),
 ACTION_REMOVE              VARCHAR2(5),
 ACTION_REMOVE_WEIGHT        VARCHAR2(5),
 ACTION_ROOT_INSERT           VARCHAR2(5),
 ACTION_ROOT_INSERT_WEIGHT   VARCHAR2(5),
 ACTION_ROOT_REMOVE          VARCHAR2(5),
 ACTION_ROOT_REMOVE_WEIGHT  VARCHAR2(5),
 ACTION_SET_FA               VARCHAR2(5),
 ACTION_SET_FA_WEIGHT        VARCHAR2(5),
 ACTION_MANAGE_USERS           VARCHAR2(5),
 ACTION_MANAGE_USERS_WEIGHT  VARCHAR2(5),
 ACTION_SUPER			VARCHAR2(5),
 ACTION_SUPER_WEIGHT		VARCHAR2(5),
 SEQUENCE_NUMBER             VARCHAR2(255)
)
ORGANIZATION EXTERNAL (
  TYPE ORACLE_LOADER
  DEFAULT DIRECTORY external_tables
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY NEWLINE
    BADFILE external_tables:'icat_role.bad'
    LOGFILE external_tables:'icat_role.log'
    FIELDS TERMINATED BY 0X'09' RTrim
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS (
  clf,
  dls,
  isis,
 ROLE ,
 ACTION_INSERT,
 ACTION_INSERT_WEIGHT ,
 ACTION_SELECT,
 ACTION_SELECT_WEIGHT,
 ACTION_DOWNLOAD ,
 ACTION_DOWNLOAD_WEIGHT ,
 ACTION_UPDATE ,
 ACTION_UPDATE_WEIGHT,
 ACTION_DELETE,
 ACTION_DELETE_WEIGHT,
 ACTION_REMOVE,
 ACTION_REMOVE_WEIGHT,
 ACTION_ROOT_INSERT,
 ACTION_ROOT_INSERT_WEIGHT ,
 ACTION_ROOT_REMOVE,
 ACTION_ROOT_REMOVE_WEIGHT ,
 ACTION_SET_FA,
 ACTION_SET_FA_WEIGHT,
 ACTION_MANAGE_USERS ,
 ACTION_MANAGE_USERS_WEIGHT,
 ACTION_SUPER,
 ACTION_SUPER_WEIGHT,
 SEQUENCE_NUMBER
    )
  )
  LOCATION ('icat_role.tsv')
)
REJECT LIMIT 0;

CREATE TABLE extern_this_icat(
  CLF                         VARCHAR2(5),
  DLS                         VARCHAR2(5),
  ISIS                        VARCHAR2(5),
  facility_short_name         VARCHAR2(30),
  facility_long_name          VARCHAR2(255),
  facility_url                VARCHAR2(255),
  facility_description        VARCHAR2(4000),
  sequence_number             VARCHAR2(255)
)
ORGANIZATION EXTERNAL (
  TYPE ORACLE_LOADER
  DEFAULT DIRECTORY external_tables
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY NEWLINE
    BADFILE external_tables:'this_icat.bad'
    LOGFILE external_tables:'this_icat.log'
    FIELDS TERMINATED BY 0X'09' RTrim
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS (
      clf,
      dls,
      isis,
      facility_short_name,
      facility_long_name,
      facility_url,
      facility_description char(4000),
      sequence_number
    )
  )
  LOCATION ('this_icat.tsv')
)
REJECT LIMIT 0;

CREATE TABLE extern_station_scienist(
  clf                 VARCHAR2(5),
  dls                 VARCHAR2(5),
  isis                VARCHAR2(5),
  instrument_name     VARCHAR2(255),
  federal_id          VARCHAR2(255),
  sequence_number     VARCHAR2(4000)
)
ORGANIZATION EXTERNAL (
  TYPE ORACLE_LOADER
  DEFAULT DIRECTORY external_tables
  ACCESS PARAMETERS (
    RECORDS DELIMITED BY NEWLINE
    BADFILE external_tables:'station_scientist.bad'
    LOGFILE external_tables:'station_scientist.log'
    FIELDS TERMINATED BY 0X'09' RTrim
    MISSING FIELD VALUES ARE NULL
    REJECT ROWS WITH ALL NULL FIELDS (
      clf,
      dls,
      isis,
      instrument_name,
      federal_id,
      sequence_number
    )
  )
  LOCATION ('station_scientist.tsv')
)
REJECT LIMIT 0;


