CREATE OR REPLACE PACKAGE util_pkg AS

/*

Name: util_pkg
Author: James Healy
Creation Date: November 2006

Purpose:
  utility package, containing exception declarations and miscellaneous
  functionality

*/


--------------------------------------------------------------------------------

-- converting delimited strings into collections

-- converts a delimited string into a collection, using any delimiter
FUNCTION string_to_table(
  p_text IN VARCHAR2,
  p_delimiter IN VARCHAR2 DEFAULT ',')
  RETURN vc_array;

-- specialist function to converts a whitespace-delimited string into a
-- collection
FUNCTION split_at_whitespace(
  p_text IN VARCHAR2)
  RETURN vc_array;

-- creates a hash value using all the parameters
FUNCTION get_md5 (
  p1  IN VARCHAR2,
  p2  IN VARCHAR2 DEFAULT NULL,
  p3  IN VARCHAR2 DEFAULT NULL,
  p4  IN VARCHAR2 DEFAULT NULL,
  p5  IN VARCHAR2 DEFAULT NULL,
  p6  IN VARCHAR2 DEFAULT NULL,
  p7  IN VARCHAR2 DEFAULT NULL,
  p8  IN VARCHAR2 DEFAULT NULL,
  p9  IN VARCHAR2 DEFAULT NULL,
  p10 IN VARCHAR2 DEFAULT NULL,
  p11 IN VARCHAR2 DEFAULT NULL,
  p12 IN VARCHAR2 DEFAULT NULL
  ) RETURN RAW DETERMINISTIC;

-- return true or false depending on whether the user has the role in
-- the specified application
FUNCTION app_role(
  p_app IN user_roles.app_code%TYPE,
  p_role IN user_roles.role%TYPE,
  p_username IN user_roles.username%TYPE)
  RETURN BOOLEAN;

END util_pkg;
/

--##############################################################################
CREATE OR REPLACE PACKAGE BODY util_pkg AS


-- converts a delimited string into a collection, using any delimiter
FUNCTION string_to_table(
  p_text IN VARCHAR2,
  p_delimiter IN VARCHAR2 DEFAULT ',')
  RETURN vc_array IS

  lv_text VARCHAR2(4000);
  lv_num PLS_INTEGER;
  retval vc_array := vc_array();
BEGIN
  lv_text := p_text || p_delimiter;

  LOOP
    lv_num := InStr(lv_text, p_delimiter);
    EXIT WHEN (Nvl(lv_num, 0) = 0);
    retval.extend;
    retval(retval.Count) := LTrim(RTrim(SubStr(lv_text, 1, lv_num - 1)));
    lv_text := SubStr(lv_text, lv_num + 1);
  END LOOP;

  RETURN retval;
END string_to_table;

--------------------------------------------------------------------------------

-- specialist function to convert a whitespace-delimited string into a
-- collection
FUNCTION split_at_whitespace(
  p_text IN VARCHAR2)
  RETURN vc_array IS
BEGIN
  -- reduce all consecutive whitespace characters to single spaces

  RETURN string_to_table(
    REGEXP_REPLACE(RTrim(p_text),'[[:space:]]+',' '), ' ');
END split_at_whitespace;

--------------------------------------------------------------------------------

-- creates a hash value using all the parameters
FUNCTION get_md5 (
  p1  IN VARCHAR2,
  p2  IN VARCHAR2 DEFAULT NULL,
  p3  IN VARCHAR2 DEFAULT NULL,
  p4  IN VARCHAR2 DEFAULT NULL,
  p5  IN VARCHAR2 DEFAULT NULL,
  p6  IN VARCHAR2 DEFAULT NULL,
  p7  IN VARCHAR2 DEFAULT NULL,
  p8  IN VARCHAR2 DEFAULT NULL,
  p9  IN VARCHAR2 DEFAULT NULL,
  p10 IN VARCHAR2 DEFAULT NULL,
  p11 IN VARCHAR2 DEFAULT NULL,
  p12 IN VARCHAR2 DEFAULT NULL
  ) RETURN RAW DETERMINISTIC IS
BEGIN
  RETURN dbms_obfuscation_toolkit.md5(
    input =>
      utl_raw.cast_to_raw(
        p1||'-'||p2||'-'||p3||'-'||p4||'-'||p5||'-'||p6||'-'||
        p7||'-'||p8||'-'||p9||'-'||p10||'-'||p11||'-'||p12));
END get_md5;

--------------------------------------------------------------------------------

-- return true or false depending on whether the user has the role in
-- the specified application
FUNCTION app_role(
  p_app IN user_roles.app_code%TYPE,
  p_role IN user_roles.role%TYPE,
  p_username IN user_roles.username%TYPE)
  RETURN BOOLEAN IS

  n NUMBER;
BEGIN
  SELECT 1 INTO n
    FROM user_roles
    WHERE role = p_role
    AND username = p_username
    AND app_code = p_app;
  RETURN TRUE;
EXCEPTION
  WHEN No_Data_Found THEN
    RETURN FALSE;
  WHEN OTHERS THEN
    log_pkg.init;
    log_pkg.write_exception('util_pkg.app_role: '||SQLERRM);
    RETURN FALSE;
END app_role;

--------------------------------------------------------------------------------

END util_pkg;
/