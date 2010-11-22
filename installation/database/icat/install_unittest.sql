undefine database_name
define testicat_username = testicat
define testicat_password = password
define TRUNCATE_TABLES   = truncate_tables
define INSERT_TEST_DATA  = insert_test_data
define INITIALISE_DATA   = initialise_data
define database_name     = XE

prompt
prompt Testing connection...
connect &testicat_username/&testicat_password@&database_name


VARIABLE separator varchar2(1);
DECLARE
   sepstring  CHAR(100);
BEGIN
  select dbms_utility.port_string into sepstring from dual;
  if substr(sepstring,0,5) = 'Linux' then
	:separator := '/';
  else
	:separator := '\\';
  end if;
END;
/
column vappidcol new_value SEPPATH noprint
SELECT :separator vappidcol from dual;

prompt
prompt Dropping the old unit test data...
prompt ==================================

@unit_test&SEPPATH&TRUNCATE_TABLES

prompt
prompt Inserting unit test data...
prompt ==================================
@data&SEPPATH&INITIALISE_DATA
@unit_test&SEPPATH&INSERT_TEST_DATA
