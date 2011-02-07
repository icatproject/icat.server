REM James Healy, November 2006
REM
REM a table and view for logging status info, etc

CREATE TABLE log_table(
  oracle_id        VARCHAR2(30) DEFAULT USER,
  datestamp        DATE DEFAULT SYSDATE,
  run_num          NUMBER(38),
  seq_num          NUMBER(38),
  text             VARCHAR2(4000 CHAR),
  exception_log    VARCHAR2(1)
);

CREATE OR REPLACE VIEW v_log_table (
  oracle_id,
  datestamp,
  time,
  t_diff,
  run_num,
  seq_num,
  text,
  exception_log) AS
SELECT
  oracle_id,
  datestamp,
  To_Char(datestamp,'HH24:MI.SS'),
  Round(24*60*60*(Lead(datestamp)
    over(PARTITION BY run_num ORDER BY seq_num) - datestamp))
    AS t_diff,
  run_num,
  seq_num,
  text,
  exception_log
FROM log_table
ORDER BY run_num DESC, seq_num ASC
/
