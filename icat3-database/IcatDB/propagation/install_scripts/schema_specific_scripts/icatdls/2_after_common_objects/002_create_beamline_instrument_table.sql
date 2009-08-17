REM Table to store the names of beamline databases and the instruments each
REM uses.  This is used when pushing instrument-specific data out to the
REM beamline databases.

PROMPT Creating table BEAMLINE_INSTRUMENT, to store the link between instruments
PROMPT and their databases

CREATE TABLE beamline_instrument(
  beamline_name  VARCHAR2(255) NOT NULL,
  instrument     VARCHAR2(255) NOT NULL,
  dblink         VARCHAR2(30) NOT NULL,
  valid          CHAR(1)      NOT NULL,
  last_update    DATE         NOT NULL,   
  CONSTRAINT beamline_instrument_pk PRIMARY KEY(instrument)
);
COMMENT ON TABLE beamline_instrument IS
  'Remote Beamline databases are loaded with data pertaining to a single instrument';

