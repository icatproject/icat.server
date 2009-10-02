CREATE OR REPLACE PACKAGE log_pkg AS

/*

Name: log_pkg
Author: James Healy
Creation Date: November 2006

Purpose:
  logs info to a table.
  logs within each session are given the same run number unless it is reset by
    the init procedure.

*/


-- initialise the run number and sequence
PROCEDURE init;

-- return the current run number for the set of logs.  will be null if a log
-- has not yet been written or after a call to init().
FUNCTION get_run_num RETURN PLS_INTEGER;

-- write a log
PROCEDURE write_log(
  p_text IN VARCHAR2,
  p_indent PLS_INTEGER DEFAULT 0);

-- write an exception log
PROCEDURE write_exception(
  p_text IN VARCHAR2,
  p_indent PLS_INTEGER DEFAULT 0);

END log_pkg;
/
--##############################################################################

CREATE OR REPLACE PACKAGE BODY log_pkg AS

--------------------------------------------------------------------------------

gn_run_num PLS_INTEGER;
gn_seq_num PLS_INTEGER := 1;

--------------------------------------------------------------------------------

PROCEDURE init IS
BEGIN
  gn_run_num := Null;
  gn_seq_num := 1;
END init;

--------------------------------------------------------------------------------

-- return the current run number for the set of logs.  will be null if a log
-- has not yet been written or after a call to init().
FUNCTION get_run_num RETURN PLS_INTEGER IS
BEGIN
  RETURN gn_run_num;
END get_run_num;

--------------------------------------------------------------------------------

-- internal procedure which does the real work
PROCEDURE write_log_internal(
  p_text IN VARCHAR2,
  p_indent PLS_INTEGER,
  p_exception_log IN log_table.exception_log%TYPE) IS

  PRAGMA autonomous_transaction;
BEGIN
  INSERT INTO log_table(run_num, seq_num, text, exception_log)
    VALUES (CASE WHEN gn_run_num IS NULL
            -- with NVL both arguments are always evaluated so CASE is quicker
              THEN (SELECT Nvl(Max(run_num),0) + 1 FROM log_table)
              ELSE gn_run_num
              END,
            gn_seq_num,
            SubStr(RPad(' ',2 * p_indent,' ') || p_text,1,4000),
            p_exception_log)
    RETURNING run_num INTO gn_run_num;

  COMMIT;

  gn_seq_num := gn_seq_num + 1;
EXCEPTION
  WHEN OTHERS THEN
    NULL;
END write_log_internal;

--------------------------------------------------------------------------------

-- write a log
PROCEDURE write_log(
  p_text IN VARCHAR2,
  p_indent PLS_INTEGER DEFAULT 0) IS
BEGIN
  write_log_internal(p_text, p_indent, 'N');
END write_log;

--------------------------------------------------------------------------------

-- write an exception log
PROCEDURE write_exception(
  p_text IN VARCHAR2,
  p_indent PLS_INTEGER DEFAULT 0) IS
BEGIN
  write_log_internal(p_text, p_indent, 'Y');
END write_exception;

--------------------------------------------------------------------------------

END log_pkg;
/